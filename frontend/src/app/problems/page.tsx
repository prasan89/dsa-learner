import Link from "next/link";

export default function ProblemsPage() {
  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-5xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold">Problems</h1>
          <div className="flex gap-2">
            {["All", "Easy", "Medium", "Hard"].map((d) => (
              <button
                key={d}
                className="px-3 py-1 rounded-full text-sm border border-gray-700 hover:border-brand-500 text-gray-300 hover:text-white transition-colors"
              >
                {d}
              </button>
            ))}
          </div>
        </div>

        {/* Problem table */}
        <div className="bg-gray-900 rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-gray-400">
                <th className="text-left px-4 py-3">#</th>
                <th className="text-left px-4 py-3">Title</th>
                <th className="text-left px-4 py-3">Pattern</th>
                <th className="text-left px-4 py-3">Difficulty</th>
                <th className="text-left px-4 py-3">Acceptance</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-gray-800 text-gray-500">
                <td colSpan={5} className="px-4 py-8 text-center">
                  Problems will load here once the API is connected.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
