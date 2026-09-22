"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  Flame, CheckCircle2, Circle, ArrowRight, TrendingUp,
  Bell, Search, ChevronRight, Clock, BookOpen, Cpu, Target
} from "lucide-react";
import { userApi, type DashboardData } from "@/lib/api/user";
import { authApi } from "@/lib/api/auth";
import { patternsApi } from "@/lib/api/patterns";
import api from "@/lib/api/client";
import type { MasteryStatus } from "@/types";

interface ReviewItem {
  problemId: string;
  slug: string;
  title: string;
  difficulty: string;
  dueDate: string;
  repetition: number;
}

const DIFF_COLOR: Record<string, string> = {
  EASY: "badge-easy",
  MEDIUM: "badge-medium",
  HARD: "badge-hard",
};

const MASTERY_BG: Record<MasteryStatus, string> = {
  NOT_STARTED: "bg-gray-200",
  LEARNING:    "bg-blue-400",
  PRACTICED:   "bg-yellow-400",
  MASTERED:    "bg-green-500",
};

function ProgressBar({ value, max, color }: { value: number; max: number; color: string }) {
  const pct = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0;
  return (
    <div className="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
      <div className={`h-full ${color} rounded-full transition-all`} style={{ width: `${pct}%` }} />
    </div>
  );
}

function SkeletonCard() {
  return <div className="card p-5 animate-pulse h-32 bg-gray-100" />;
}

export default function DashboardPage() {
  const [user, setUser]               = useState<any>(null);
  const [dash, setDash]               = useState<DashboardData | null>(null);
  const [masteryData, setMasteryData] = useState<any[]>([]);
  const [reviews, setReviews]         = useState<ReviewItem[]>([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(false);

  useEffect(() => {
    Promise.all([
      authApi.me(),
      userApi.dashboard(),
      patternsApi.getMastery(),
      api.get<ReviewItem[]>("/users/me/reviews/today").catch(() => ({ data: [] })),
    ]).then(([u, d, m, r]) => {
      setUser(u.data);
      setDash(d.data);
      setMasteryData(m.data);
      setReviews((r as any).data ?? []);
    }).catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const firstName = user?.name?.split(" ")[0] ?? "there";
  const hour      = new Date().getHours();
  const greeting  = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white border-b border-gray-200 px-6 py-3 h-14" />
        <div className="max-w-6xl mx-auto px-6 py-6 space-y-6">
          <div className="h-10 bg-gray-100 rounded-xl animate-pulse w-64" />
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            <SkeletonCard /> <SkeletonCard />
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
            <div className="lg:col-span-2"><SkeletonCard /></div>
            <SkeletonCard />
          </div>
        </div>
      </div>
    );
  }

  if (error || !dash) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl mb-2">⚠️</p>
          <p className="text-gray-600 font-medium">Could not load dashboard</p>
          <p className="text-gray-400 text-sm mt-1">Check your connection or sign out and back in.</p>
          <button onClick={() => window.location.reload()}
            className="mt-4 btn-primary">Retry</button>
        </div>
      </div>
    );
  }

  const { streak, dsa, systemDesign, plan, nextAction } = dash;
  const dsaPct = dsa.total > 0 ? Math.round((dsa.solved / dsa.total) * 100) : 0;
  const sdPct  = systemDesign.total > 0 ? Math.round((systemDesign.mastered / systemDesign.total) * 100) : 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top bar */}
      <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center gap-4 sticky top-0 z-30">
        <div className="flex-1 max-w-md relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            placeholder="Search for topics, problems, or concepts…"
            className="w-full pl-9 pr-4 py-2 bg-gray-100 rounded-lg text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500/30 focus:bg-white transition-colors"
          />
        </div>
        <button className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
          <Bell size={18} className="text-gray-500" />
        </button>
        <div className="flex items-center gap-2 ml-1">
          <div className="w-8 h-8 rounded-full bg-brand-600 flex items-center justify-center text-white text-xs font-bold">
            {firstName[0]?.toUpperCase()}
          </div>
          <div className="text-sm leading-tight">
            <p className="font-semibold text-gray-900">{user?.name}</p>
            <p className="text-gray-400 text-xs">{plan === "PRO" ? "Pro Plan" : "Free Plan"}</p>
          </div>
        </div>
      </header>

      <div className="max-w-6xl mx-auto px-6 py-6 space-y-6">

        {/* Welcome row */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {greeting}, {firstName}!
            </h1>
            <p className="text-gray-500 text-sm mt-0.5">Small steps. Big progress. Keep going!</p>
          </div>
          {streak > 0 && (
            <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 rounded-xl px-4 py-2">
              <Flame size={20} className="text-orange-500" />
              <div>
                <p className="text-sm font-bold text-orange-700">{streak} day streak</p>
                <p className="text-xs text-orange-500">You&apos;re doing great!</p>
              </div>
            </div>
          )}
        </div>

        {/* Next best action + due revision */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

          {/* Next action */}
          <div className="card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Your next best action</p>
            {nextAction ? (
              <>
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center shrink-0">
                    <Target size={22} className="text-brand-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs text-gray-400 mb-1">DSA › {nextAction.patternName}</p>
                    <h3 className="font-semibold text-gray-900 leading-snug">{nextAction.title}</h3>
                    <div className="flex items-center gap-2 mt-2">
                      <span className={DIFF_COLOR[nextAction.difficulty] ?? "badge-medium"}>
                        {nextAction.difficulty[0] + nextAction.difficulty.slice(1).toLowerCase()}
                      </span>
                    </div>
                  </div>
                </div>
                <Link href={`/problems/${nextAction.slug}`}
                  className="mt-4 w-full btn-primary flex items-center justify-center gap-2">
                  Start problem <ArrowRight size={14} />
                </Link>
              </>
            ) : (
              <div className="text-center py-6">
                <p className="text-3xl mb-2">🎉</p>
                <p className="text-gray-600 font-medium">All caught up!</p>
                <p className="text-gray-400 text-sm mt-1">You&apos;ve solved all available problems.</p>
              </div>
            )}
          </div>

          {/* Today's revision */}
          <div className="card p-5">
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Due for Revision</p>
              {reviews.length > 0 && (
                <span className="text-xs bg-orange-100 text-orange-700 px-2 py-0.5 rounded-full font-medium">
                  {reviews.length} due
                </span>
              )}
            </div>
            {reviews.length === 0 ? (
              <div className="text-center py-6">
                <CheckCircle2 size={28} className="text-green-400 mx-auto mb-2" />
                <p className="text-gray-500 text-sm font-medium">Nothing due today</p>
                <p className="text-gray-400 text-xs mt-1">Great — no reviews pending!</p>
              </div>
            ) : (
              <div className="space-y-2">
                {reviews.slice(0, 4).map((r) => (
                  <Link key={r.problemId} href={`/problems/${r.slug}`}
                    className="flex items-center justify-between px-3 py-2.5 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors group">
                    <span className="text-sm font-medium text-gray-700 group-hover:text-gray-900 truncate flex-1">
                      {r.title}
                    </span>
                    <div className="flex items-center gap-2 shrink-0 ml-2">
                      <span className={DIFF_COLOR[r.difficulty] ?? "badge-medium"}>
                        {r.difficulty[0] + r.difficulty.slice(1).toLowerCase()}
                      </span>
                    </div>
                  </Link>
                ))}
                {reviews.length > 4 && (
                  <Link href="/revision"
                    className="text-xs text-brand-600 hover:text-brand-700 font-medium flex items-center gap-1 pt-1">
                    +{reviews.length - 4} more <ChevronRight size={12} />
                  </Link>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Progress stats + Quick actions */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

          {/* Progress stats */}
          <div className="lg:col-span-2 card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-4">Your Progress</p>
            <div className="grid grid-cols-2 gap-6">

              {/* DSA */}
              <Link href="/problems" className="group">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
                    <BookOpen size={15} className="text-blue-600" />
                  </div>
                  <span className="text-sm font-medium text-gray-600">DSA Problems</span>
                </div>
                <p className="text-3xl font-bold text-gray-900">{dsaPct}%</p>
                <p className="text-xs text-gray-400 mt-0.5">
                  {dsa.solved} / {dsa.total} solved
                  {dsa.masteryAvg > 0 && (
                    <span className="ml-2 text-blue-500">· {dsa.masteryAvg}% mastery avg</span>
                  )}
                </p>
                <ProgressBar value={dsa.solved} max={dsa.total} color="bg-blue-500" />
              </Link>

              {/* System Design */}
              <Link href="/system-design" className="group">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-8 h-8 rounded-lg bg-purple-50 flex items-center justify-center">
                    <TrendingUp size={15} className="text-purple-600" />
                  </div>
                  <span className="text-sm font-medium text-gray-600">System Design</span>
                </div>
                <p className="text-3xl font-bold text-gray-900">{sdPct}%</p>
                <p className="text-xs text-gray-400 mt-0.5">
                  {systemDesign.mastered} / {systemDesign.total} mastered
                  {systemDesign.masteryAvg > 0 && (
                    <span className="ml-2 text-purple-500">· {systemDesign.masteryAvg}% mastery avg</span>
                  )}
                </p>
                <ProgressBar value={systemDesign.mastered} max={systemDesign.total} color="bg-purple-500" />
              </Link>
            </div>
          </div>

          {/* Quick actions */}
          <div className="card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Quick Actions</p>
            <div className="space-y-1.5">
              {[
                { label: "Start Practice",       href: "/problems",      color: "text-blue-600 bg-blue-50" },
                { label: "Open AI Mentor",        href: "/ai-mentor",     color: "text-purple-600 bg-purple-50" },
                { label: `Revision (${reviews.length} due)`, href: "/revision", color: "text-orange-600 bg-orange-50" },
                { label: "Pattern Learning Path", href: "/patterns",      color: "text-green-600 bg-green-50" },
              ].map(({ label, href, color }) => (
                <Link key={href} href={href}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 transition-colors group">
                  <span className={`w-7 h-7 rounded-md flex items-center justify-center ${color}`}>
                    <ArrowRight size={13} />
                  </span>
                  <span className="text-sm text-gray-700 group-hover:text-gray-900">{label}</span>
                </Link>
              ))}
            </div>
          </div>
        </div>

        {/* Pattern mastery heatmap */}
        {masteryData.length > 0 && (
          <div className="card p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-gray-900">Pattern Mastery</h2>
              <Link href="/patterns" className="text-xs text-brand-600 hover:text-brand-700 font-medium flex items-center gap-1">
                View all <ChevronRight size={12} />
              </Link>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2">
              {masteryData.map((m) => {
                const status = (m.status ?? "NOT_STARTED") as MasteryStatus;
                const score: number = m.masteryScore ?? 0;
                return (
                  <Link key={m.patternId} href={`/patterns/${m.patternSlug}`}
                    className="group rounded-xl p-3 bg-gray-50 hover:bg-gray-100 border border-gray-100 hover:border-gray-200 transition-all">
                    <p className="text-xs font-medium text-gray-700 group-hover:text-gray-900 leading-tight truncate mb-2">
                      {m.patternName}
                    </p>
                    <div className="w-full h-1.5 bg-gray-200 rounded-full overflow-hidden">
                      <div className={`h-full rounded-full ${MASTERY_BG[status]}`}
                        style={{ width: `${Math.round(score)}%` }} />
                    </div>
                    <p className="text-xs text-gray-400 mt-1">{Math.round(score)}%</p>
                  </Link>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
