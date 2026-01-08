export default function SliderRow({ label, value, onChange, min = 0, max = 1000, warningMin }) {
    const warn = typeof warningMin === 'number' && value < warningMin;
    return (
        <div className="mt-4">
            <div className="flex items-center justify-between">
                <span className="font-medium">{label}</span>
                <span className={`text-sm ${warn ? 'text-red-600' : 'text-slate-700'}`}>€{(value || 0).toFixed(0)}</span>
            </div>
            <input
                type="range"
                min={min}
                max={Math.max(min, max)}
                step={1}
                value={Number(value) || 0}
                onChange={(e) => onChange(e.target.value)}
                className="w-full accent-blue-600"
                aria-label={label}
            />
            <div className="mt-2 flex items-center gap-2">
                <input
                    type="number"
                    min={min}
                    max={max}
                    step={1}
                    value={Number(value) || 0}
                    onChange={(e) => onChange(e.target.value)}
                    className="w-32 px-3 py-1 rounded-lg border border-slate-300"
                    aria-label={label}
                />
                {typeof warningMin === 'number' && (
                    <span className={`text-xs ${warn ? 'text-red-600' : 'text-slate-500'}`}>
                        Minimum: €{warningMin}
                    </span>
                )}
            </div>
        </div>
    );
}