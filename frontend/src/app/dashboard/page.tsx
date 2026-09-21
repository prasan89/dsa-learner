"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { userApi } from "@/lib/api/user";
import { authApi } from "@/lib/api/auth";

export default function DashboardPage() {
  const [user, setUser] = useState<any>(null);
  const [progress, setProgress] = useState<any>(null);
  const [recentSubmissions, setRecentSubmissions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      authApi.me(),
      userApi.progress(),
      userApi.recentSubmissions(),
    ]).then(([u, p, s]) => {
      setUser(u.data);
      setProgress(p.data);
      setRecentSubmissions(s.data.slice(0, 5));
    }).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="text-gray-400 flex items-center justify-center h-64">Loading...</div>
    );
  }

  const STATS = [
    { label: "Solved", value: progress?.totalSolved ?? 0, color: "text-white" },
    { label: "Easy",   value: progress?.easySolved   ?? 0, color: "text-green-400" },
    { label: "Medium", value: progress?.mediumSolved ?? 0, color: "text-yellow-400" },
    { label: "Hard",   value: progress?.hardSolved   ?? 0, color: "text-red-400" },
  ];

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Welcome */}
        <div>
          <h1 className="text-2xl font-bold">Welcome back, {user?.name?.split(" ")[0]} 👋</h1>
          <p className="text-gray-400 mt-1">Keep going. Patterns build mastery.</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {STATS.map(({ label, value, color }) => (
            <div key={label} className="bg-gray-900 rounded-xl p-5 text-center border border-gray-800">
              <p className={`text-4xl font-bold ${color}`}>{value}</p>
              <p className="text-gray-400 text-sm mt-1">{label}</p>
            </div>
          ))}
        </div>

        {/* Quick actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Link href="/problems"
            className="bg-gray-900 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group">
            <h3 className="font-semibold group-hover:text-brand-400 transition-colors">Practice Problems</h3>
            <p className="text-gray-400 text-sm mt-1">Browse all 10 problems across 10 patterns.</p>
          </Link>
          <Link href="/patterns"
            className="bg-gray-900 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group">
            <h3 className="font-semibold group-hover:text-brand-400 transition-colors">Study Patterns</h3>
            <p className="text-gray-400 text-sm mt-1">Learn recognition clues and templates.</p>
          </Link>
        </div>

        {/* Recent submissions */}
        <div className="bg-gray-900 rounded-xl p-5 border border-gray-800">
          <h2 className="font-semibold mb-4">Recent Submissions</h2>
          {recentSubmissions.length === 0 ? (
            <p className="text-gray-500 text-sm">
              No submissions yet.{" "}
              <Link href="/problems" className="text-brand-500 hover:underline">Start a problem →</Link>
            </p>
          ) : (
            <div className="space-y-2">
              {recentSubmissions.map((s) => (
                <div key={s.id} className="flex items-center justify-between text-sm py-2 border-b border-gray-800 last:border-0">
                  <Link href={`/problems/${s.problemSlug}`}
                    className="text-white hover:text-brand-400 font-medium transition-colors">
                    {s.problemTitle}
                  </Link>
                  <div className="flex items-center gap-3">
                    {s.runtimeMs && <span className="text-gray-500 text-xs">{s.runtimeMs}ms</span>}
                    <span className={`text-xs font-semibold ${
                      s.status === "ACCEPTED" ? "text-green-400" :
                      s.status === "WRONG_ANSWER" ? "text-red-400" : "text-yellow-400"
                    }`}>
                      {s.status.replace(/_/g, " ")}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
