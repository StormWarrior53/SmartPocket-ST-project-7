export default function Card({ title, children, className = '' }) {
    return (
        <div className={`bg-white/80 rounded-2xl p-5 shadow ${className}`}>
            <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
            <div className="mt-3">{children}</div>
        </div>
    );
}