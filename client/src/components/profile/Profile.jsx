export default function Profile() {
    const parent = {
        name: "John Doe",
        email: "john.doe@example.com",
        profilePic: "/images/profile.png",
        children: [
            { id: 1, name: "Alice", age: 7 },
            { id: 2, name: "Bob", age: 5 },
            { id: 3, name: "Charlie", age: 6 },
        ],
    };

    const handleCreateChild = () => {
        console.log("Create child clicked");
    };

    const handleRemoveChild = (id) => {
        console.log("Remove child", id);
    };

    const handleAllowance = (id) => {
        console.log("Add allowance to child", id);
    };

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-8">

                {/* Profile Header */}
                <div className="flex flex-col items-center space-y-4">
                    <img
                        src={parent.profilePic}
                        alt="Profile"
                        className="w-32 h-32 rounded-full border-4 border-blue-100 shadow-sm object-cover"
                    />
                    <h1 className="text-3xl font-bold text-blue-600">{parent.name}</h1>
                    <p className="text-slate-700">{parent.email}</p>
                </div>

                {/* Children Section */}
                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-blue-600">Children</h2>
                        <button
                            onClick={handleCreateChild}
                            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                        >
                            Create Child
                        </button>
                    </div>

                    {/* Children Cards */}
                    <div className="flex flex-wrap gap-4">
                        {parent.children.map((child) => (
                            <div
                                key={child.id}
                                className="flex flex-col items-center bg-blue-50 border border-blue-100 rounded-lg p-4 w-48"
                            >
                                <img
                                    src={parent.profilePic}
                                    alt={child.name}
                                    className="w-20 h-20 rounded-full border-2 border-blue-200 object-cover mb-2"
                                />
                                <p className="font-semibold text-slate-800">{child.name}</p>
                                <p className="text-slate-600 text-sm mb-2">Age: {child.age}</p>
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => handleAllowance(child.id)}
                                        className="bg-green-500 text-white px-3 py-1 rounded-lg hover:bg-green-600 text-sm"
                                    >
                                        Add Allowance
                                    </button>
                                    <button
                                        onClick={() => handleRemoveChild(child.id)}
                                        className="bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-600 text-sm"
                                    >
                                        Remove
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
}