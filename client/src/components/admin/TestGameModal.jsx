import BudgetGame from '../budget-game/BudgetGame.jsx';
import './TestGameModal.css';

export default function TestGameModal({ config, onClose }) {
    return (
        <div className="test-game-modal-overlay">
            <div className="test-game-modal">
                <div className="test-game-header">
                    <h2>Test Configuration: {config.name}</h2>
                    <button onClick={onClose} className="btn-close-modal">✕</button>
                </div>
                
                <div className="test-game-info">
                    <p>Play through the game to test this configuration</p>
                    <div className="config-preview">
                        <span>Savings Rate: {(config.minSavingsRate * 100).toFixed(0)}%</span>
                        <span>Meal Cost: €{config.mealCost}</span>
                        <span>Budget: €{config.minPlayBudget}</span>
                        <span>Prize: €{config.prizePocketMoney}</span>
                    </div>
                </div>

                <div className="test-game-container">
                    <BudgetGame testMode={true} testConfig={config} />
                </div>
            </div>
        </div>
    );
}
