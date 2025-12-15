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
      <div className="mt-3">
        <h4 className="font-medium">Inventory</h4>
        {child.inventory && child.inventory.length > 0 ? (
          <ul className="list-disc list-inside text-sm text-slate-700">
            {child.inventory.map(item => (
              <li key={item.inventoryItemId || item.storeItemId}>
                <span className="mr-2">{item.emoji ?? "🎁"}</span>
                {item.name} — qty: {item.quantity} — paid: {item.pricePaid}
                <span className="text-xs text-slate-500 ml-2">({new Date(item.purchasedAt).toLocaleString()})</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-slate-500">No purchased items</p>
        )}
      </div>
    </div>
  );
}