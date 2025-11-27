export default function Steps() {
    return (
        <section className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8">
            <h2 className="text-2xl font-bold text-blue-600 text-center">How It Works</h2>
            <div className="mt-6 grid grid-cols-1 md:grid-cols-4 gap-6 text-center">
                <div>
                    <img src="/images/step1.png" alt="Create Account" className="mx-auto mb-2 w-24 h-24" />
                    <p className="text-slate-700 font-medium">Parent creates account & adds child</p>
                </div>
                <div>
                    <img src="/images/step2.png" alt="Play Games" className="mx-auto mb-2 w-24 h-24" />
                    <p className="text-slate-700 font-medium">Child plays mini-games & earns XP</p>
                </div>
                <div>
                    <img src="/images/step3.png" alt="Unlock Badges" className="mx-auto mb-2 w-24 h-24" />
                    <p className="text-slate-700 font-medium">Unlock badges and rewards</p>
                </div>
                <div>
                    <img src="/images/step4.png" alt="Track Progress" className="mx-auto mb-2 w-24 h-24" />
                    <p className="text-slate-700 font-medium">Parent tracks progress & sets goals</p>
                </div>
            </div>
        </section>
    )
}