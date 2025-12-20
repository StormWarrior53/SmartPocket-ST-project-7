export default function SummaryRow({ label, value, valueClass }) {
    return (
        <div className="flex items-center justify-between text-slate-800">
            <span>{label}</span>
            <span className={`font-semibold ${valueClass || ''}`}>{value}</span>
        </div>
    );
}