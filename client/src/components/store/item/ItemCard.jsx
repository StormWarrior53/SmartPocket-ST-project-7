export default function ItemCard({ item, buyItem, user }) {

    return (
        <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6 flex flex-col items-center justify-between hover:shadow-lg transition">
            <div className="text-5xl mb-4">{item.emoji}</div>
            <h2 className="font-bold text-xl text-blue-600 mb-2">{item.name}</h2>
            <p className="text-slate-700 mb-4">{item.price} 💰</p>
            {user?.role !== 'parent' && user?.role === 'child' &&
                <button
                    onClick={() => buyItem(item)}
                    className="bg-blue-500 text-white px-5 py-2 rounded-2xl hover:bg-blue-600 transition"
                >
                    Buy Now
                </button>
            }
        </div>
    );
}