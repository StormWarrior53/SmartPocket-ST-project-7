export default function Progress({ label, value, color = 'bg-blue-600' }) {
    return (
        <div className="mt-3">
            <div className="flex justify-between text-sm mb-1">
                <span className="text-slate-700">{label}</span>
                <span className="text-slate-800">{value}%</span>
            </div>
            <div className="w-full bg-slate-200 rounded-full h-2">
                <div className={`${color} h-2 rounded-full`} style={{ width: `${value}%` }} />
            </div>
        </div>
    );
}