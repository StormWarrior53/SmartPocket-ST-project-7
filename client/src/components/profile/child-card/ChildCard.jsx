export default function ChildCard({ child, onAddAllowance, onDelete }) {
    return (
        <div key={child.id} className="mb-6">
            <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
                <div>
                    <strong className="block">Name: {child.name}</strong>
                    <p className="text-sm text-slate-700">Age: {child.age}</p>
                    <p className="text-sm text-slate-700">Balance: {child.allowanceMoney}</p>
                </div>

                <div className="flex items-start space-x-2">
                    <button
                        onClick={() => onAddAllowance(child.id)}
                        className="bg-green-600 text-white px-3 py-1 rounded-md text-sm hover:bg-green-700"
                    >
                        Add Allowance
                    </button>
                    <button
                        onClick={() => onDelete(child.id)}
                        className="bg-red-600 text-white px-3 py-1 rounded-md text-sm hover:bg-red-700"
                    >
                        Delete
                    </button>
                </div>
            </div>

            {/* Inventory */}
            <div className="mt-4">
                <h4 className="font-semibold text-slate-800 mb-2 flex items-center gap-2">
                    🎒 Inventory
                </h4>

                {child.inventory && child.inventory.length > 0 ? (
                    <ul className="space-y-2">
                        {child.inventory.map(item => (
                            <li
                                key={item.inventoryItemId || item.storeItemId}
                                className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-3 shadow-sm hover:shadow-md transition"
                            >
                                {/* Left side */}
                                <div className="flex items-center gap-3">
                                    <span className="text-2xl">{item.emoji ?? "🎁"}</span>
                                    <div>
                                        <p className="font-medium text-slate-800">
                                            {item.name}
                                        </p>
                                        <p className="text-xs text-slate-500">
                                            Purchased on {new Date(item.purchasedAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                </div>

                                {/* Right side */}
                                <div className="text-right">
                                    <p className="text-xs text-slate-500">
                                        Paid: {item.pricePaid}
                                    </p>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <div className="rounded-lg border border-dashed border-slate-300 p-4 text-center text-sm text-slate-500">
                        💤 No purchased items yet
                    </div>
                )}
            </div>
        </div>
    );
}