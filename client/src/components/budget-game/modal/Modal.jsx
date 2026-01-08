export default function Modal({ children, onClose }) {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4" data-testid="modal">
            <div className="absolute inset-0 bg-black/40" onClick={onClose} />
            <div className="relative z-10 w-full max-w-lg bg-white rounded-2xl shadow-xl p-6">
                {children}
            </div>
        </div>
    );
}