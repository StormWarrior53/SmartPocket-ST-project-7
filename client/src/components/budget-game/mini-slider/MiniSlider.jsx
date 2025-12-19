export default function MiniSlider({ label, value, onChange, min = 0, max = 1000, warningMin }) {
    const warn = typeof warningMin === 'number' && value < warningMin;
    return (
        <div className={`p-3 rounded-xl ${warn ? 'bg-red-50' : 'bg-slate-50'}`}>
            <div className="flex items-center justify-between">
                <span className="text-sm font-medium">{label}</span>
                <span className={`text-xs ${warn ? 'text-red-600' : 'text-slate-700'}`}>€{(value || 0).toFixed(0)}</span>
            </div>
            <input
                type="range"
                min={min}
                max={Math.max(min, max)}
                step={1}
                value={Number(value) || 0}
                onChange={(e) => onChange(e.target.value)}
                className="w-full accent-blue-600 mt-2"
            />
            <input
                type="number"
                min={min}
                max={max}
                step={1}
                value={Number(value) || 0}
                onChange={(e) => onChange(e.target.value)}
                className="w-full mt-2 px-2 py-1 rounded-lg border border-slate-300 text-sm"
            />
            {typeof warningMin === 'number' && (
                <div className={`text-[11px] mt-1 ${warn ? 'text-red-600' : 'text-slate-500'}`}>
                    Minimum: €{warningMin}
                </div>
            )}
        </div>
    );
}