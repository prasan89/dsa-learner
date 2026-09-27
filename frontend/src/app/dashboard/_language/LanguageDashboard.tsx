"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowRight, Globe, BookOpen } from "lucide-react";
import { authApi } from "@/lib/api/auth";
import api from "@/lib/api/client";

interface GermanProgress {
  completedLessons: number;
  totalLessons: number;
  currentUnit?: string;
}

export default function LanguageDashboard() {
  const [user, setUser]           = useState<any>(null);
  const [progress, setProgress]   = useState<GermanProgress | null>(null);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    Promise.all([
      authApi.me(),
      api.get<GermanProgress>("/academy/german/progress").catch(() => ({ data: null })),
    ]).then(([u, p]) => {
      setUser(u.data);
      setProgress((p as any).data);
    }).finally(() => setLoading(false));
  }, []);

  const firstName = user?.name?.split(" ")[0] ?? "there";
  const hour      = new Date().getHours();
  const greeting  = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-pulse w-64 h-10 bg-gray-100 rounded-xl" />
      </div>
    );
  }

  const pct = progress && progress.totalLessons > 0
    ? Math.round((progress.completedLessons / progress.totalLessons) * 100)
    : 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-8">

        <div>
          <h1 className="text-2xl font-bold text-gray-900">{greeting}, {firstName}!</h1>
          <p className="text-gray-500 text-sm mt-1">Continue your language journey.</p>
        </div>

        {/* German card */}
        <div className="card p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
              <Globe size={20} className="text-emerald-600" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">German</h2>
              {progress?.currentUnit && (
                <p className="text-xs text-gray-400">Current unit: {progress.currentUnit}</p>
              )}
            </div>
          </div>

          {progress && (
            <>
              <div className="flex items-end justify-between mb-1">
                <p className="text-3xl font-bold text-gray-900">{pct}%</p>
                <p className="text-xs text-gray-400">{progress.completedLessons} / {progress.totalLessons} lessons</p>
              </div>
              <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-emerald-500 rounded-full transition-all" style={{ width: `${pct}%` }} />
              </div>
            </>
          )}

          <Link href="/languages/german"
            className="mt-5 w-full btn-primary flex items-center justify-center gap-2">
            Continue Learning <ArrowRight size={14} />
          </Link>
        </div>

        {/* Quick actions */}
        <div className="card p-5">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Quick Actions</p>
          <div className="space-y-1.5">
            {[
              { label: "German Lessons",   href: "/languages/german",     color: "text-emerald-600 bg-emerald-50" },
              { label: "Vocabulary",        href: "/languages/vocabulary",  color: "text-blue-600 bg-blue-50" },
              { label: "Grammar",           href: "/languages/grammar",     color: "text-purple-600 bg-purple-50" },
              { label: "AI Tutor",          href: "/languages/ai-tutor",    color: "text-orange-600 bg-orange-50" },
            ].map(({ label, href, color }) => (
              <Link key={href} href={href}
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 transition-colors group">
                <span className={`w-7 h-7 rounded-md flex items-center justify-center ${color}`}>
                  <BookOpen size={13} />
                </span>
                <span className="text-sm text-gray-700 group-hover:text-gray-900">{label}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
