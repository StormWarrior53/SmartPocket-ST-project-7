import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import ItemCard from "./item/ItemCard.jsx";
import { useUser } from "../../context/UserContext.jsx";

export default function Store() {
  const [balance, setBalance] = useState(0);
  const [inventory, setInventory] = useState([]); // items owned by current child
  const [storeItems, setStoreItems] = useState([]);
  const navigate = useNavigate();

  const { user, loading, authFetch } = useUser();

  useEffect(() => {
    const fetchStoreItems = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/store');
        if (!response.ok) throw new Error("Failed to fetch store items");
        const data = await response.json();
        setStoreItems(data);
      } catch (error) {
        alert(error.message);
      }
    };

    const fetchChildData = async () => {
      if (!user || user.role !== 'child') return;

      try {
        // balance
        const meRes = await authFetch('http://localhost:8080/api/children/me', { method: 'GET' });
        if (!meRes.ok) throw new Error("Failed to fetch child profile");
        const me = await meRes.json();
        setBalance(me?.pocketMoney ?? 0);

        // inventory
        const invRes = await authFetch('http://localhost:8080/api/children/me/inventory', { method: 'GET' });
        if (!invRes.ok) throw new Error("Failed to fetch inventory");
        const inv = await invRes.json();
        setInventory(Array.isArray(inv) ? inv : []);
      } catch (error) {
        console.error(error);
        alert(error.message);
      }
    };

    fetchStoreItems();
    fetchChildData();
  }, [user, authFetch]);

  // Normalize owned ids from inventory entries
  const ownedItemIds = new Set(
    inventory
      .map((entry) => entry.storeItemId || entry.itemId || entry.id) // adapt if your API differs
      .filter(Boolean)
  );

  const buyItem = async (item) => {
    if (!user || user.role !== 'child') {
      alert("Only child accounts can purchase items.");
      return;
    }

    if (ownedItemIds.has(item.id)) {
      alert("You already own this item.");
      return;
    }

    const confirmed = window.confirm(`Do you want to buy "${item.name}" for ${item.price} 💰?`);
    if (!confirmed) return;

    try {
      const res = await authFetch('http://localhost:8080/api/children/me/inventory/purchase', {
        method: 'POST',
        body: JSON.stringify({
          storeItemId: item.id,
          quantity: 1, // enforce single quantity
        }),
      });

      // handleResponse pattern: check res.ok, parse body
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(body?.message || "Purchase failed");
      }

      // Update inventory locally
      setInventory((prev) => [...prev, body]);

      // Optionally refresh balance after purchase
      try {
        const meRes = await authFetch('http://localhost:8080/api/children/me', { method: 'GET' });
        const me = await meRes.json().catch(() => ({}));
        if (meRes.ok) setBalance(me?.pocketMoney ?? 0);
      } catch { }

      alert(`Purchased "${item.name}" 🎉`);
      // navigate("/"); // keep user on store to see "Owned" state
    } catch (error) {
      alert(error.message);
    }
  };

  return (
    <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
      <div className="max-w-5xl mx-auto space-y-10">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-blue-600">Kids Finance Store 🛍️</h1>
          <p className="mt-2 text-slate-600 text-lg">Use your earned coins to buy fun rewards and show your parents what you've learned!</p>
        </div>

        <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8">
          {!loading && user?.role === 'child' && (
            <p className="text-lg font-semibold text-center mb-6">
              Your Balance: {balance} 💰
            </p>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {storeItems.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                buyItem={buyItem}
                user={user}
                owned={ownedItemIds.has(item.id)}
              />
            ))}
          </div>
        </div>

        {inventory.length > 0 && (
          <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8">
            <h2 className="text-2xl font-bold text-blue-600 mb-4 text-center">
              Your Purchased Items 🎉
            </h2>
            <ul className="list-disc list-inside space-y-2 text-slate-700 text-center">
              {inventory.map((inv, index) => {
                const siId = inv.storeItemId || inv.itemId || inv.id;
                const storeItem = storeItems.find((si) => si.id === siId);
                const name = storeItem?.name || inv.name || 'Item';
                const emoji = storeItem?.emoji || inv.emoji || '🎁';
                return (
                  <li key={index}>
                    {emoji} {name}
                  </li>
                );
              })}
            </ul>
          </div>
        )}
      </div>
    </section>
  );
}