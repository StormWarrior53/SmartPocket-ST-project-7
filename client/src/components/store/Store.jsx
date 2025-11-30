import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import ItemCard from "./item/ItemCard.jsx";

// const storeItems = [
//   { id: 1, name: "Sticker Pack", price: 10, emoji: "✨" },
//   { id: 2, name: "Toy Car", price: 50, emoji: "🚗" },
//   { id: 3, name: "Story Book", price: 30, emoji: "📚" },
//   { id: 4, name: "Ice Cream", price: 20, emoji: "🍦" },
//   { id: 5, name: "Puzzle", price: 40, emoji: "🧩" },
// ];

export default function Store() {
  const [balance, setBalance] = useState(100); // Kid's coins
  const [cart, setCart] = useState([]);
  const [storeItems, setStoreItems] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchStoreItems = async() => {
      try {
        const response = await fetch('http://localhost:8080/api/store');
        if (!response.ok) throw new Error("Failed to fetch store items");
        const data = await response.json();
        setStoreItems(data);

      } catch (error) {
        alert(error.message);
      }
    }

    fetchStoreItems();
  }, [])

  const buyItem = (item) => {
    if (balance < item.price) {
      alert("Not enough coins! Keep learning and earning!");
      return;
    }

    const confirmed = window.confirm(
      `Do you want to buy "${item.name}" for ${item.price} 💰?`
    );

    if (confirmed) {
      setBalance(balance - item.price);
      setCart([...cart, item]);
      navigate("/"); // Redirect to home page
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
          <p className="text-lg font-semibold text-center mb-6">
            Your Balance: {balance} 💰
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {storeItems.map((item) => (
              <ItemCard key={item.id} item={item} buyItem={buyItem} />
            ))}
          </div>
        </div>

        {cart.length > 0 && (
          <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8">
            <h2 className="text-2xl font-bold text-blue-600 mb-4 text-center">
              Your Purchased Items 🎉
            </h2>
            <ul className="list-disc list-inside space-y-2 text-slate-700 text-center">
              {cart.map((item, index) => (
                <li key={index}>
                  {item.emoji} {item.name}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </section>
  );
}
