"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  Flame, CheckCircle2, Circle, ArrowRight, TrendingUp,
  Bell, Search, ChevronRight, Clock, BookOpen, Cpu, Target
} from "lucide-react";
import { userApi } from "@/lib/api/user";
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

export default function DashboardPage() {
  const [user, setUser]               = useState<any>(null);
  const [progress, setProgress]       = useState<any>(null);
  const [masteryData, setMasteryData] = useState<any[]>([]);
  const [reviews, setReviews]         = useState<ReviewItem[]>([]);
  const [wallet, setWallet]           = useState<{ totalCredits: number } | null>(null);
  const [loading, setLoading]         = useState(true);
  const [streak] = useState(12); // TODO: wire from backend

  useEffect(() => {
    Promise.all([
      authApi.me(),
      userApi.progress(),
      patternsApi.getMastery(),
      api.get<ReviewItem[]>("/api/users/me/reviews/today").catch(() => ({ data: [] })),
      api.get<any>("/api/ai/wallet").catch(() => ({ data: null })),
    ]).then(([u, p, m, r, w]) => {
      setUser(u.data);
      setProgress(p.data);
      setMasteryData(m.data);
      setReviews((r as any).data ?? []);
      setWallet((w as any).data);
    }).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-400 text-sm">Loading…</div>
      </div>
    );
  }

  const firstName = user?.name?.split(" ")[0] ?? "there";
  const hour      = new Date().getHours();
  const greeting  = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

  const dsaSolved  = progress?.totalSolved ?? 0;
  const dsaTotal   = 25;
  const javaTopics = 8;
  const javaTotalT = 15;
  const sdTopics   = 8;
  const sdTotalT   = 20;

  const nextAction = {
    breadcrumb: "DSA › Sliding Window",
    title: "Longest Substring Without Repeating Characters",
    difficulty: "Medium",
    minutes: 15,
    href: "/problems/longest-substring-without-repeating-characters",
  };

  const todayPlan = [
    { label: "Solve 2 DSA problems",      done: false },
    { label: "Review 3 due problems",     done: false },
    { label: "Learn HashMap internals (Java)", done: false },
    { label: "System Design: Caching",    done: false },
  ];

  const completedPlan = todayPlan.filter(t => t.done).length;
  const planPct       = Math.round((completedPlan / todayPlan.length) * 100);

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
            {firstName[0]}
          </div>
          <div className="text-sm leading-tight">
            <p className="font-semibold text-gray-900">{user?.name}</p>
            <p className="text-gray-400 text-xs">Pro Plan</p>
          </div>
        </div>
      </header>

      <div className="max-w-6xl mx-auto px-6 py-6 space-y-6">

        {/* Welcome row */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {greeting}, {firstName}! 👋
            </h1>
            <p className="text-gray-500 text-sm mt-0.5">Small steps. Big progress. Keep going!</p>
          </div>
          <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 rounded-xl px-4 py-2">
            <Flame size={20} className="text-orange-500" />
            <div>
              <p className="text-sm font-bold text-orange-700">{streak} day streak</p>
              <p className="text-xs text-orange-500">You&apos;re doing great!</p>
            </div>
          </div>
        </div>

        {/* Main grid: next action + today's plan */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

          {/* Next best action */}
          <div className="card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Your next best action</p>
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center shrink-0">
                <Target size={22} className="text-brand-600" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gray-400 mb-1">{nextAction.breadcrumb}</p>
                <h3 className="font-semibold text-gray-900 leading-snug">{nextAction.title}</h3>
                <div className="flex items-center gap-2 mt-2">
                  <span className="badge-medium">{nextAction.difficulty}</span>
                  <span className="flex items-center gap-1 text-xs text-gray-500">
                    <Clock size={11} /> {nextAction.minutes} min
                  </span>
                </div>
              </div>
            </div>
            <Link href={nextAction.href}
              className="mt-4 w-full btn-primary flex items-center justify-center gap-2">
              Continue <ArrowRight size={14} />
            </Link>
          </div>

          {/* Today's plan */}
          <div className="card p-5">
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Today&apos;s plan</p>
              <span className="text-xs text-gray-500">{planPct}% complete</span>
            </div>
            <div className="space-y-2.5">
              {todayPlan.map((item, i) => (
                <div key={i} className="flex items-center gap-3">
                  {item.done
                    ? <CheckCircle2 size={17} className="text-green-500 shrink-0" />
                    : <Circle size={17} className="text-gray-300 shrink-0" />
                  }
                  <span className={`text-sm ${item.done ? "text-gray-400 line-through" : "text-gray-700"}`}>
                    {item.label}
                  </span>
                </div>
              ))}
            </div>
            <button className="mt-4 text-xs text-brand-600 hover:text-brand-700 flex items-center gap-1 font-medium">
              View full plan <ChevronRight size={13} />
            </button>
          </div>
        </div>

        {/* Progress stats + Quick actions */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

          {/* Progress stats */}
          <div className="lg:col-span-2 card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-4">Your Progress</p>
            <div className="grid grid-cols-3 gap-4">
              {/* DSA */}
              <Link href="/problems" className="group">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
                    <BookOpen size={15} className="text-blue-600" />
                  </div>
                  <span className="text-sm font-medium text-gray-600">DSA</span>
                </div>
                <p className="text-3xl font-bold text-gray-900">{Math.round((dsaSolved / dsaTotal) * 100)}%</p>
                <p className="text-xs text-gray-400 mt-0.5">{dsaSolved} / {dsaTotal} problems</p>
                <div className="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div className="h-full bg-blue-500 rounded-full transition-all"
                    style={{ width: `${Math.round((dsaSolved / dsaTotal) * 100)}%` }} />
                </div>
              </Link>

              {/* Java */}
              <Link href="/java" className="group">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-8 h-8 rounded-lg bg-orange-50 flex items-center justify-center">
                    <Cpu size={15} className="text-orange-600" />
                  </div>
                  <span className="text-sm font-medium text-gray-600">Java</span>
                </div>
                <p className="text-3xl font-bold text-gray-900">{Math.round((javaTopics / javaTotalT) * 100)}%</p>
                <p className="text-xs text-gray-400 mt-0.5">{javaTopics} / {javaTotalT} topics</p>
                <div className="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div className="h-full bg-orange-500 rounded-full transition-all"
                    style={{ width: `${Math.round((javaTopics / javaTotalT) * 100)}%` }} />
                </div>
              </Link>

              {/* System Design */}
              <Link href="/system-design" className="group">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-8 h-8 rounded-lg bg-purple-50 flex items-center justify-center">
                    <TrendingUp size={15} className="text-purple-600" />
                  </div>
                  <span className="text-sm font-medium text-gray-600">System Design</span>
                </div>
                <p className="text-3xl font-bold text-gray-900">{Math.round((sdTopics / sdTotalT) * 100)}%</p>
                <p className="text-xs text-gray-400 mt-0.5">{sdTopics} / {sdTotalT} topics</p>
                <div className="mt-2 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div className="h-full bg-purple-500 rounded-full transition-all"
                    style={{ width: `${Math.round((sdTopics / sdTotalT) * 100)}%` }} />
                </div>
              </Link>
            </div>
          </div>

          {/* Quick actions */}
          <div className="card p-5">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Quick Actions</p>
            <div className="space-y-1.5">
              {[
                { label: "Start Practice",       href: "/problems",    color: "text-blue-600 bg-blue-50" },
                { label: "Open AI Mentor",        href: "/ai-mentor",   color: "text-purple-600 bg-purple-50" },
                { label: `Today's Revision (${reviews.length})`, href: "/revision", color: "text-orange-600 bg-orange-50" },
                { label: "Take a Mock Interview", href: "/problems",    color: "text-green-600 bg-green-50" },
              ].map(({ label, href, color }) => (
                <Link key={label} href={href}
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

        {/* Today's revision (if any) */}
        {reviews.length > 0 && (
          <div className="card p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-gray-900">Due for Revision</h2>
              <span className="text-xs bg-orange-100 text-orange-700 px-2 py-0.5 rounded-full font-medium">
                {reviews.length} due today
              </span>
            </div>
            <div className="space-y-2">
              {reviews.slice(0, 4).map((r) => (
                <Link key={r.problemId} href={`/problems/${r.slug}`}
                  className="flex items-center justify-between px-4 py-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors group">
                  <span className="text-sm font-medium text-gray-700 group-hover:text-gray-900">{r.title}</span>
                  <div className="flex items-center gap-2">
                    <span className={DIFF_COLOR[r.difficulty] ?? "badge-medium"}>
                      {r.difficulty[0] + r.difficulty.slice(1).toLowerCase()}
                    </span>
                    <span className="text-xs bg-brand-600 text-white px-2 py-0.5 rounded-full font-medium">Review</span>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

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
