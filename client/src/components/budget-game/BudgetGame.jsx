import { useEffect, useMemo, useState } from 'react';
import { useUser } from '../../context/UserContext';
import { budgetGameApi } from '../../services/api';

import Modal from './modal/Modal.jsx';
import Card from './card/Card.jsx';
import Progress from './progress/Progress.jsx';
import SummaryRow from './summary-row/SummaryRow.jsx';
import SliderRow from './slider-row/SliderRow.jsx';
import MiniSlider from './mini-slider/MiniSlider.jsx';
import { useNavigate } from 'react-router';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Fallback config with original hardcoded values
// const FALLBACK_CONFIG = {
//   minSavingsRate: 0.2,
//   mealCost: 5,
//   minMeals: 25,
//   minElectricity: 20,
//   minWater: 15,
//   minGas: 15,
//   minPlayBudget: 500,
//   prizePocketMoney: 25,
//   eventConfig: JSON.stringify({
//     negative: {
//       probability: 0.4,
//       minAmount: 30,
//       maxAmount: 100,
//       events: [
//         { title: "Phone screen broke", message: "Repair costs €{amount}" },
//         { title: "Unexpected bill", message: "Pay €{amount} for fees" }
//       ]
//     },
//     positive: {
//       probability: 0.3,
//       minAmount: 10,
//       maxAmount: 60,
//       events: [
//         { title: "Surprise gift", message: "You received €{amount}!" }
//       ]
//     },
//     neutral: {
//       probability: 0.3,
//       events: [
//         { title: "Quiet month", message: "No surprises this month" }
//       ]
//     }
//   })
// };

export default function BudgetGame() {
  const { authFetch, isAuthenticated } = useUser();

  // UI state
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');
  const [gameConfig, setGameConfig] = useState(null);
  const [configLoading, setConfigLoading] = useState(true);
  const [configError, setConfigError] = useState('');
  const [startingBudget, setStartingBudget] = useState(0);
  const [monthReady, setMonthReady] = useState(false);
  const navigate = useNavigate();

  // Game constants - loaded from config
  const MIN_SAVINGS_RATE = gameConfig?.minSavingsRate ?? 0.2;
  const MEAL_COST = gameConfig?.mealCost ?? 5;
  const MIN_MEALS = gameConfig?.minMeals ?? 25;
  const MIN_FOOD = MIN_MEALS * MEAL_COST;

  const MIN_ELECTRICITY = gameConfig?.minElectricit ?? 20;
  const MIN_WATER = gameConfig?.minWater ?? 15;
  const MIN_GAS = gameConfig?.minGas ?? 15;
  const MIN_PLAY_BUDGET = gameConfig?.minPlayBudget;
  const PRIZE_POCKET_MONEY = gameConfig?.prizePocketMoney ?? 25;

  // Other categories can be 0 by default
  const [alloc, setAlloc] = useState({
    food: MIN_FOOD,
    electricity: MIN_ELECTRICITY,
    water: MIN_WATER,
    gas: MIN_GAS,
    transportation: 0,
    clothing: 0,
    entertainment: 0,
    misc: 0,
  });

  // Event modal state
  const [modal, setModal] = useState({
    open: false,
    title: '',
    message: '',
    result: null, // 'win' | 'lose' | null
  });

  // Derivations
  const totalExpenses = useMemo(() => {
    return Object.values(alloc).reduce((a, b) => a + (Number(b) || 0), 0);
  }, [alloc]);

  const savings = useMemo(() => {
    return Math.max(0, startingBudget - totalExpenses);
  }, [startingBudget, totalExpenses]);

  const savingsRate = useMemo(() => {
    if (!startingBudget) return 0;
    return savings / startingBudget;
  }, [savings, startingBudget]);

  const essentialMinTotal = MIN_FOOD + MIN_ELECTRICITY + MIN_WATER + MIN_GAS;
  const requiredSavings = useMemo(() => Math.ceil(startingBudget * MIN_SAVINGS_RATE), [startingBudget]);
  const budgetTooLow = useMemo(() => {
    if (!startingBudget) return false;
    return (essentialMinTotal + requiredSavings) > startingBudget;
  }, [startingBudget]);

  const budgetBelowPlayMin = useMemo(() => startingBudget < MIN_PLAY_BUDGET, [startingBudget]);

  const format = (n) => `€${(Number(n) || 0).toFixed(0)}`;
  const toPercent = (rate) => Math.max(0, Math.round((rate || 0) * 100));

  const patchJson = async (url, payload) => {
    const res = await authFetch(url, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const ct = res.headers.get('content-type') || '';
    const isJson = ct.includes('application/json');
    const data = isJson ? await res.json() : await res.text();
    if (!res.ok) {
      const msg = isJson ? (data?.message || data?.error || 'Request failed') : 'Request failed';
      throw new Error(msg);
    }
    return data;
  };

  const persistResults = async (win, remainingAllowance, savingsRate) => {
    const messages = [];
    const safeRemaining = Math.max(0, Math.round(remainingAllowance));

    try {
      await patchJson(`${API_BASE_URL}/children/me/allowance`, { amount: safeRemaining });
      messages.push(`Saved allowance: ${format(safeRemaining)}.`);
    } catch (e) {
      messages.push(`Failed to save allowance: ${e.message}.`);
    }

    if (win) {
      const xpToAdd = toPercent(savingsRate); // percent saved => XP
      try {
        await patchJson(`${API_BASE_URL}/children/me/xp`, { xp: xpToAdd });
        messages.push(`XP gained: +${xpToAdd}.`);
      } catch (e) {
        messages.push(`Failed to add XP: ${e.message}.`);
      }

      try {
        await patchJson(`${API_BASE_URL}/children/me/pocket`, { amount: PRIZE_POCKET_MONEY });
        messages.push(`Prize added: ${format(PRIZE_POCKET_MONEY)} to pocket money.`);
      } catch (e) {
        messages.push(`Failed to add prize: ${e.message}.`);
      }
    }

    return messages.join(' ');
  };

  // Fetch child balance and prepare budget
  const loadBudget = async () => {
    if (!isAuthenticated) {
      setFetchError('Please log in to play the game.');
      setLoading(false);
      return;
    }
    setLoading(true);
    setFetchError('');
    try {
      const res = await authFetch(`${API_BASE_URL}/children/me`, { method: 'GET' });
      const contentType = res.headers.get('content-type') || '';
      const isJson = contentType.includes('application/json');
      const payload = isJson ? await res.json() : await res.text();

      if (!res.ok) {
        const msg = isJson
          ? payload?.message || payload?.error || 'Failed to load balance'
          : 'Failed to load balance';
        throw new Error(msg);
      }
      if (!isJson) throw new Error('Unexpected server response. Please try again later.');

      // Use allowance only for monthly budget
      const allowance = Number(payload?.allowanceMoney ?? 0);
      const budget = Math.max(0, allowance);
      setStartingBudget(budget);

      setAlloc({
        food: MIN_FOOD,
        electricity: MIN_ELECTRICITY,
        water: MIN_WATER,
        gas: MIN_GAS,
        transportation: 0,
        clothing: 0,
        entertainment: 0,
        misc: 0,
      });
      setMonthReady(true);
    } catch (e) {
      setFetchError(e?.message || 'Network error. Could not load balance.');
      setMonthReady(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBudget();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  // Load game configuration from API
  useEffect(() => {
    const loadGameConfig = async () => {
      try {
        setConfigLoading(true);
        setConfigError('');

        // Fetch active config from backend
        const config = await budgetGameApi.getActiveConfig();
        setGameConfig(config);

        console.log('Loaded game config:', config);

      } catch (error) {
        console.error('Failed to load game config:', error);
        setConfigError('Could not load game settings, using defaults');

        // Fallback to hardcoded values if API fails
        setGameConfig(FALLBACK_CONFIG);
      } finally {
        setConfigLoading(false);
      }
    };

    loadGameConfig();
  }, []);

  const setValue = (key, v) => {
    setAlloc((prev) => ({ ...prev, [key]: Math.max(0, Math.round(Number(v) || 0)) }));
  };

  const resetToMinimums = () => {
    setAlloc({
      food: MIN_FOOD,
      electricity: MIN_ELECTRICITY,
      water: MIN_WATER,
      gas: MIN_GAS,
      transportation: 0,
      clothing: 0,
      entertainment: 0,
      misc: 0,
    });
  };

  const validateMinimums = () => {
    const issues = [];
    if (alloc.food < MIN_FOOD) {
      issues.push(`Food is too low. You must afford at least ${MIN_MEALS} meals x ${format(MEAL_COST)} = ${format(MIN_FOOD)}.`);
    }
    if (alloc.electricity < MIN_ELECTRICITY) issues.push(`Electricity must be at least ${format(MIN_ELECTRICITY)}.`);
    if (alloc.water < MIN_WATER) issues.push(`Water must be at least ${format(MIN_WATER)}.`);
    if (alloc.gas < MIN_GAS) issues.push(`Gas must be at least ${format(MIN_GAS)}.`);
    if (totalExpenses > startingBudget) issues.push(`You overspent your budget by ${format(totalExpenses - startingBudget)}.`);
    return issues;
  };

  const runRandomEvent = (currentSavings) => {
    // Parse the event config JSON
    let eventConfigData;
    try {
      eventConfigData = JSON.parse(gameConfig?.eventConfig || '{}');
    } catch (e) {
      console.error('Failed to parse event config:', e);
      // Return neutral event if parsing fails
      return {
        type: 'none',
        title: 'Error',
        message: 'Could not load events',
        after: currentSavings
      };
    }

    const { negative, positive, neutral } = eventConfigData;

    // Generate random number between 0 and 1
    const r = Math.random();

    // Calculate probability thresholds
    const negThreshold = negative?.probability || 0.4;
    const posThreshold = negThreshold + (positive?.probability || 0.3);

    // Determine which type of event happens
    if (r < negThreshold && negative?.events) {
      // Negative event (e.g., phone breaks)
      const cost = Math.floor(
        negative.minAmount + Math.random() * (negative.maxAmount - negative.minAmount)
      );
      const event = negative.events[Math.floor(Math.random() * negative.events.length)];

      return {
        type: 'negative',
        amount: cost,
        after: currentSavings - cost,
        title: event.title,
        message: event.message.replace('{amount}', format(cost))
      };

    } else if (r < posThreshold && positive?.events) {
      // Positive event (e.g., gift)
      const bonus = Math.floor(
        positive.minAmount + Math.random() * (positive.maxAmount - positive.minAmount)
      );
      const event = positive.events[Math.floor(Math.random() * positive.events.length)];

      return {
        type: 'positive',
        amount: bonus,
        after: currentSavings + bonus,
        title: event.title,
        message: event.message.replace('{amount}', format(bonus))
      };

    } else {
      // Neutral event (nothing happens)
      const event = neutral?.events?.[0] || { title: 'Quiet month', message: 'No surprises this month' };

      return {
        type: 'none',
        amount: 0,
        after: currentSavings,
        title: event.title,
        message: event.message
      };
    }
  };

  const onDone = async () => {
    const issues = validateMinimums();
    if (issues.length) {
      setModal({
        open: true,
        title: 'Fix your plan',
        message: issues.join('\n'),
        result: null,
      });
      return;
    }

    if (startingBudget - totalExpenses < 0) {
      setModal({
        open: true,
        title: 'Overspent',
        message: 'Your expenses are higher than your budget. Reduce some categories.',
        result: null,
      });
      return;
    }

    const evt = runRandomEvent(startingBudget - totalExpenses);
    const finalSavings = evt.after;
    const finalRate = startingBudget ? finalSavings / startingBudget : 0;
    const win = finalSavings >= 0 && finalRate >= MIN_SAVINGS_RATE;

    // Persist to API
    const persistMsg = await persistResults(win, finalSavings, finalRate);

    setModal({
      open: true,
      title: evt.title,
      message:
        `${evt.message}\n\n` +
        `Final savings: ${format(finalSavings)} (${Math.round(finalRate * 100)}%).\n` +
        (win
          ? 'You met the 20% savings goal. You win!'
          : 'You did not meet the 20% savings goal. You lose.') +
        `\n\n${persistMsg}`,
      result: win ? 'win' : 'lose',
    });
  };

  const handleModalClose = () => {
    setModal((m) => ({ ...m, open: false }));
    if (modal.result === 'win' || modal.result === 'lose') {
      navigate('/games');
    }
  };

  const disabled = loading || configLoading || !!fetchError || !monthReady || budgetBelowPlayMin;
  const modalImage =
    modal.result === 'win'
      ? '/images/games-win.png'
      : modal.result === 'lose'
        ? '/images/games-lose.png'
        : null;

  return (
    <section
      className="min-h-screen bg-cover bg-center py-10 px-4 sm:px-8"
      style={{ backgroundImage: "url('/images/games2.png')" }}
    >
      <div className="max-w-5xl mx-auto">
        <div className="backdrop-blur-md bg-white/70 rounded-2xl shadow-lg p-6 sm:p-8">
          <h1 className="text-3xl font-extrabold text-blue-700">Monthly Budget Game</h1>
          <p className="text-slate-700 mt-2">Plan your month, cover essentials, and save at least 20% to win.</p>

          {configLoading && (
            <div className="mt-6 p-4 bg-blue-100 border border-blue-300 text-blue-900 rounded-xl">
              Loading game settings...
            </div>
          )}

          {configError && (
            <div className="mt-6 p-4 bg-amber-100 border border-amber-300 text-amber-900 rounded-xl">
              ⚠️ {configError}
            </div>
          )}

          {!isAuthenticated && (
            <div className="mt-6 p-4 bg-amber-100 border border-amber-300 text-amber-900 rounded-xl">
              Please log in to load your budget.
            </div>
          )}

          {loading && (
            <div className="mt-6 animate-pulse p-4 bg-slate-100 rounded-xl">Loading your budget…</div>
          )}

          {!!fetchError && (
            <div className="mt-6 p-4 bg-red-100 border border-red-300 text-red-800 rounded-xl flex items-center justify-between gap-3">
              <span>{fetchError}</span>
              <button
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                onClick={() => navigate('/')}
              >
                Back to Home
              </button>
            </div>
          )}

          {!loading && !fetchError && monthReady && budgetBelowPlayMin && (
            <div className="mt-6 p-4 bg-amber-100 border border-amber-300 text-amber-900 rounded-xl">
              Your starting budget is below €{MIN_PLAY_BUDGET}. You can’t play this time. Ask your parents to increase your allowance.
              <div className="mt-3">
                <button
                  className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white"
                  onClick={() => navigate('/games')}
                >
                  Back to Games
                </button>
              </div>
            </div>
          )}

          {!loading && !fetchError && monthReady && !budgetBelowPlayMin && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
                <div className="md:col-span-2">
                  <Card title="Your Plan">
                    <div className="text-sm text-slate-600 mb-3">
                      Essentials minimums: Food {format(MIN_FOOD)}, Electricity {format(MIN_ELECTRICITY)}, Water {format(MIN_WATER)}, Gas {format(MIN_GAS)}.
                      Food requires at least {MIN_MEALS} meals at {format(MEAL_COST)}/meal.
                    </div>

                    <SliderRow
                      label="Food"
                      min={0}
                      warningMin={MIN_FOOD}
                      value={alloc.food}
                      max={Math.max(startingBudget, MIN_FOOD)}
                      onChange={(v) => setValue('food', v)}
                    />

                    <h3 className="mt-6 font-semibold text-slate-800">Utilities</h3>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-2">
                      <MiniSlider
                        label="Electricity"
                        min={0}
                        warningMin={MIN_ELECTRICITY}
                        value={alloc.electricity}
                        max={Math.max(startingBudget, MIN_ELECTRICITY)}
                        onChange={(v) => setValue('electricity', v)}
                      />
                      <MiniSlider
                        label="Water"
                        min={0}
                        warningMin={MIN_WATER}
                        value={alloc.water}
                        max={Math.max(startingBudget, MIN_WATER)}
                        onChange={(v) => setValue('water', v)}
                      />
                      <MiniSlider
                        label="Gas"
                        min={0}
                        warningMin={MIN_GAS}
                        value={alloc.gas}
                        max={Math.max(startingBudget, MIN_GAS)}
                        onChange={(v) => setValue('gas', v)}
                      />
                    </div>

                    <h3 className="mt-6 font-semibold text-slate-800">Other</h3>
                    <SliderRow
                      label="Transportation"
                      min={0}
                      value={alloc.transportation}
                      max={startingBudget}
                      onChange={(v) => setValue('transportation', v)}
                    />
                    <SliderRow
                      label="Clothing"
                      min={0}
                      value={alloc.clothing}
                      max={startingBudget}
                      onChange={(v) => setValue('clothing', v)}
                    />
                    <SliderRow
                      label="Entertainment"
                      min={0}
                      value={alloc.entertainment}
                      max={startingBudget}
                      onChange={(v) => setValue('entertainment', v)}
                    />
                    <SliderRow
                      label="Miscellaneous"
                      min={0}
                      value={alloc.misc}
                      max={startingBudget}
                      onChange={(v) => setValue('misc', v)}
                    />

                    <div className="mt-6 flex items-center gap-3">
                      <button
                        disabled={disabled}
                        className={`px-5 py-2 rounded-xl text-white ${disabled ? 'bg-gray-400' : 'bg-blue-600 hover:bg-blue-700'}`}
                        onClick={onDone}
                      >
                        Done
                      </button>
                      <button
                        className="px-4 py-2 rounded-xl bg-slate-200 hover:bg-slate-300 text-slate-800"
                        onClick={resetToMinimums}
                      >
                        Reset to Minimums
                      </button>
                    </div>

                    {budgetTooLow && (
                      <div className="mt-4 p-3 rounded-lg bg-amber-100 text-amber-900 border border-amber-300">
                        Your budget is too low to cover essentials and save 20%. Ask your parents to increase your allowance.
                      </div>
                    )}
                  </Card>
                </div>

                <div>
                  <Card title="Summary">
                    <SummaryRow label="Monthly Budget" value={format(startingBudget)} />
                    <SummaryRow label="Required Savings (20%)" value={format(requiredSavings)} />
                    <hr className="my-2 border-slate-300" />
                    <SummaryRow label="Allocated to Expenses" value={format(totalExpenses)} />
                    <SummaryRow
                      label="Remaining (Savings)"
                      value={format(Math.max(0, startingBudget - totalExpenses))}
                      valueClass={(startingBudget - totalExpenses) < requiredSavings ? 'text-red-600' : 'text-green-700'}
                    />
                    <div className="mt-2 text-sm">
                      Savings rate: <b className={`${savingsRate >= MIN_SAVINGS_RATE ? 'text-green-700' : 'text-red-600'}`}>{Math.round(savingsRate * 100)}%</b>
                    </div>

                    {totalExpenses > startingBudget && (
                      <div className="mt-3 p-2 bg-red-100 border border-red-300 text-red-800 rounded-md">
                        You overspent by {format(totalExpenses - startingBudget)}.
                      </div>
                    )}

                    <div className="mt-4">
                      <Progress
                        label="Budget Used"
                        value={Math.min(100, Math.round((totalExpenses / (startingBudget || 1)) * 100))}
                        color="bg-blue-600"
                      />
                      <Progress
                        label="Savings"
                        value={Math.min(100, Math.round((savings / (startingBudget || 1)) * 100))}
                        color={savingsRate >= MIN_SAVINGS_RATE ? 'bg-green-600' : 'bg-red-600'}
                      />
                    </div>
                  </Card>

                  <Card title="Tips" className="mt-6">
                    <ul className="list-disc pl-6 text-sm text-slate-700 space-y-1">
                      <li>Cover Food and Utilities first.</li>
                      <li>Aim for at least 20% savings to handle surprises.</li>
                      <li>Entertainment can be fun, but don’t overspend.</li>
                    </ul>
                  </Card>

                  <div className="hidden md:flex items-center justify-center pt-5">
                    <img
                      src="/images/games-thinking.png"
                      alt="Boy thinking"
                      className="w-full h-auto rounded-2xl shadow-xl"
                    />
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {modal.open && (
        <Modal onClose={handleModalClose}>
          {modalImage && (
            <div className="flex items-start gap-4 mb-3">
              <img
                src={modalImage}
                alt={modal.result === 'win' ? 'Happy boy' : 'Sad boy'}
                className="w-28 h-auto rounded-xl shadow-sm"
              />
              <div className="flex-1">
                <h3 className="text-xl font-bold">{modal.title}</h3>
                <pre className="whitespace-pre-wrap mt-2 text-slate-800">{modal.message}</pre>
              </div>
            </div>
          )}
          {!modalImage && (
            <>
              <h3 className="text-xl font-bold">{modal.title}</h3>
              <pre className="whitespace-pre-wrap mt-2 text-slate-800">{modal.message}</pre>
            </>
          )}
          {modal.result === 'win' && (
            <div className="mt-3 inline-block px-3 py-1 rounded-full bg-green-100 text-green-800 border border-green-300">
              You Win
            </div>
          )}
          {modal.result === 'lose' && (
            <div className="mt-3 inline-block px-3 py-1 rounded-full bg-red-100 text-red-800 border border-red-300">
              You Lose
            </div>
          )}
          <div className="mt-4 flex justify-end">
            <button
              className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white"
              onClick={handleModalClose}
            >
              Close
            </button>
          </div>
        </Modal>
      )}
    </section>
  );
}

