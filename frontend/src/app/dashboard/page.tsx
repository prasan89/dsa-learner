export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-6xl mx-auto space-y-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>

        {/* Progress summary */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { label: "Solved", value: "0" },
            { label: "Easy", value: "0", color: "text-green-400" },
            { label: "Medium", value: "0", color: "text-yellow-400" },
            { label: "Hard", value: "0", color: "text-red-400" },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-gray-900 rounded-xl p-4 text-center">
              <p className={`text-3xl font-bold ${color ?? "text-white"}`}>{value}</p>
              <p className="text-gray-400 text-sm mt-1">{label}</p>
            </div>
          ))}
        </div>

        {/* Pattern progress */}
        <div className="bg-gray-900 rounded-xl p-6">
          <h2 className="text-lg font-semibold mb-4">Pattern Progress</h2>
          <p className="text-gray-500 text-sm">No patterns started yet. <a href="/patterns" className="text-brand-500 hover:underline">Browse patterns →</a></p>
        </div>

        {/* Recent activity */}
        <div className="bg-gray-900 rounded-xl p-6">
          <h2 className="text-lg font-semibold mb-4">Recent Submissions</h2>
          <p className="text-gray-500 text-sm">No submissions yet. <a href="/problems" className="text-brand-500 hover:underline">Start a problem →</a></p>
        </div>
      </div>
    </div>
  );
}
