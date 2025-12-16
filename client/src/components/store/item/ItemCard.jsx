export default function ItemCard({ item, buyItem, user, owned }) {

    return (
        <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6 flex flex-col items-center justify-between hover:shadow-lg transition">
            <div className="text-5xl mb-4">{item.emoji}</div>
            <h2 className="font-bold text-xl text-blue-600 mb-2">{item.name}</h2>
            <p className="text-slate-700 mb-4">{item.price} 💰</p>
            {user?.role !== 'parent' && user?.role === 'child' && (
                <button
                    onClick={() => !owned && buyItem(item)}
                    className={`px-5 py-2 rounded-2xl transition ${owned ? 'bg-gray-300 text-gray-700 cursor-not-allowed' : 'bg-blue-500 text-white hover:bg-blue-600'
                        }`}
                    disabled={owned}
                    title={owned ? 'You already own this item' : 'Buy Now'}
                >
                    {owned ? 'Owned' : 'Buy Now'}
                </button>
            )}
        </div>
    );
}