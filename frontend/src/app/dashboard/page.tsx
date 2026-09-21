"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { userApi } from "@/lib/api/user";
import { authApi } from "@/lib/api/auth";
import { patternsApi } from "@/lib/api/patterns";
import api from "@/lib/api/client";
import type { MasteryStatus } from "@/types";

const MASTERY_COLOR: Record<MasteryStatus, string> = {
  NOT_STARTED: "bg-gray-700",
  LEARNING: "bg-blue-500",
  PRACTICED: "bg-yellow-500",
  MASTERED: "bg-green-500",
};

interface ReviewItem {
  problemId: string;
  slug: string;
  title: string;
  difficulty: string;
  dueDate: string;
  repetition: number;
}

export default function DashboardPage() {
  const [user, setUser] = useState<any>(null);
  const [progress, setProgress] = useState<any>(null);
  const [recentSubmissions, setRecentSubmissions] = useState<any[]>([]);
  const [masteryData, setMasteryData] = useState<any[]>([]);
  const [reviews, setReviews] = useState<ReviewItem[]>([]);
  const [wallet, setWallet] = useState<{ totalCredits: number } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      authApi.me(),
      userApi.progress(),
      userApi.recentSubmissions(),
      patternsApi.getMastery(),
      api.get<ReviewItem[]>("/api/users/me/reviews/today").catch(() => ({ data: [] })),
      api.get<any>("/api/ai/wallet").catch(() => ({ data: null })),
    ]).then(([u, p, s, m, r, w]) => {
      setUser(u.data);
      setProgress(p.data);
      setRecentSubmissions(s.data.slice(0, 5));
      setMasteryData(m.data);
      setReviews((r as any).data ?? []);
      setWallet((w as any).data);
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

  const masteredCount = masteryData.filter((m) => m.status === "MASTERED").length;
  const practicedCount = masteryData.filter((m) => m.status === "PRACTICED").length;

  const DIFF_COLOR: Record<string, string> = {
    EASY: "text-green-400",
    MEDIUM: "text-yellow-400",
    HARD: "text-red-400",
  };

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Welcome */}
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold">Welcome back, {user?.name?.split(" ")[0]} 👋</h1>
            <p className="text-gray-400 mt-1">Keep going. Patterns build mastery.</p>
          </div>
          {wallet !== null && (
            <Link href="/wallet"
              className="flex items-center gap-2 bg-gray-900 border border-gray-800 hover:border-indigo-500 rounded-lg px-4 py-2 text-sm transition-colors">
              <span className="text-indigo-400 font-bold">{wallet.totalCredits}</span>
              <span className="text-gray-400">credits</span>
            </Link>
          )}
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

        {/* Today's Revision */}
        {reviews.length > 0 && (
          <div className="bg-gray-900 rounded-xl p-5 border border-indigo-800">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-indigo-300">Today's Revision</h2>
              <span className="text-xs bg-indigo-900 text-indigo-300 px-2 py-0.5 rounded-full">
                {reviews.length} due
              </span>
            </div>
            <div className="space-y-2">
              {reviews.map((r) => (
                <Link
                  key={r.problemId}
                  href={`/problems/${r.slug}`}
                  className="flex items-center justify-between bg-gray-800 hover:bg-gray-700 rounded-lg px-4 py-3 transition-colors group"
                >
                  <span className="text-sm font-medium group-hover:text-white">{r.title}</span>
                  <div className="flex items-center gap-3">
                    <span className={`text-xs ${DIFF_COLOR[r.difficulty] ?? "text-gray-400"}`}>
                      {r.difficulty}
                    </span>
                    <span className="text-xs text-gray-500">Rep {r.repetition}</span>
                    <span className="text-xs bg-indigo-700 text-indigo-200 px-2 py-0.5 rounded">Review</span>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Quick actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link href="/problems"
            className="bg-gray-900 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group">
            <h3 className="font-semibold group-hover:text-brand-400 transition-colors">Practice Problems</h3>
            <p className="text-gray-400 text-sm mt-1">Browse all problems across patterns.</p>
          </Link>
          <Link href="/patterns"
            className="bg-gray-900 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group">
            <h3 className="font-semibold group-hover:text-brand-400 transition-colors">Study Patterns</h3>
            <p className="text-gray-400 text-sm mt-1">Learn recognition clues and templates.</p>
          </Link>
          <Link href="/detect-pattern"
            className="bg-gray-900 border border-gray-800 hover:border-purple-500/50 rounded-xl p-5 transition-all group">
            <h3 className="font-semibold group-hover:text-purple-400 transition-colors">Pattern Detector</h3>
            <p className="text-gray-400 text-sm mt-1">AI-powered: paste code to identify its pattern.</p>
          </Link>
        </div>

        {/* Pattern mastery grid */}
        {masteryData.length > 0 && (
          <div className="bg-gray-900 rounded-xl p-5 border border-gray-800">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold">Pattern Mastery</h2>
              <span className="text-xs text-gray-500">
                {masteredCount} mastered · {practicedCount} practiced · {masteryData.length - masteredCount - practicedCount} remaining
              </span>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2">
              {masteryData.map((m) => {
                const status = (m.status ?? "NOT_STARTED") as MasteryStatus;
                const score: number = m.masteryScore ?? 0;
                return (
                  <Link
                    key={m.patternId}
                    href={`/patterns/${m.patternSlug}`}
                    className="group rounded-lg p-3 bg-gray-800 hover:bg-gray-700 transition-colors"
                  >
                    <p className="text-xs text-gray-300 group-hover:text-white leading-tight truncate mb-2">
                      {m.patternName}
                    </p>
                    {/* Score bar */}
                    <div className="w-full h-1.5 bg-gray-700 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${MASTERY_COLOR[status]}`}
                        style={{ width: `${Math.round(score)}%` }}
                      />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">{Math.round(score)}%</p>
                  </Link>
                );
              })}
            </div>
            <div className="flex gap-4 mt-3 text-xs text-gray-500">
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-green-500 inline-block" /> Mastered</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-yellow-500 inline-block" /> Practiced</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-blue-500 inline-block" /> Learning</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-gray-700 inline-block" /> Not Started</span>
            </div>
          </div>
        )}

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
