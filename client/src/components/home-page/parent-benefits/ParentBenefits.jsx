export default function ParentBenefits() {
    return (
        <section className="bg-blue-50 border border-blue-100 shadow-sm rounded-2xl p-8">
            <div className="flex flex-col md:flex-row items-center gap-8">
                <div className="flex-1 text-center md:text-left">
                    <h2 className="text-2xl font-bold text-blue-600">Benefits for Parents</h2>
                    <ul className="mt-6 space-y-3 text-slate-700 max-w-3xl mx-auto md:mx-0 list-disc list-inside">
                        <li>Monitor your child's progress and achievements.</li>
                        <li>Set goals and rewards to encourage learning.</li>
                        <li>Safe, ad-free, and educational environment.</li>
                        <li>Help your child build lifelong financial skills through play.</li>
                    </ul>
                </div>

                <div className="flex-1 flex justify-center md:justify-end">
                    <img
                        src="/images/home2.png"
                        alt="Parent and child using the app"
                        className="w-full max-w-md rounded-2xl border border-blue-100 shadow-sm object-cover"
                    />
                </div>
            </div>
        </section>
    )
}