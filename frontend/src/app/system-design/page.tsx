"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ChevronRight, CheckCircle2, Lock } from "lucide-react";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern, MasteryStatus } from "@/types";

const TOPIC_ICONS: Record<string, string> = {
  "url-shortener":       "🔗",
  "rate-limiter":        "🚦",
  "news-feed":           "📰",
  "consistent-hashing":  "🔄",
  "distributed-cache":   "⚡",
  "search-autocomplete": "🔍",
  "notification-system": "🔔",
  "message-queue":       "📨",
};

const DIFFICULTY: Record<string, { label: string; cls: string }> = {
  "url-shortener":       { label: "Beginner",     cls: "text-green-700 bg-green-100" },
  "rate-limiter":        { label: "Intermediate",  cls: "text-yellow-700 bg-yellow-100" },
  "news-feed":           { label: "Advanced",      cls: "text-red-700 bg-red-100" },
  "consistent-hashing":  { label: "Intermediate",  cls: "text-yellow-700 bg-yellow-100" },
  "distributed-cache":   { label: "Intermediate",  cls: "text-yellow-700 bg-yellow-100" },
  "search-autocomplete": { label: "Intermediate",  cls: "text-yellow-700 bg-yellow-100" },
  "notification-system": { label: "Intermediate",  cls: "text-yellow-700 bg-yellow-100" },
  "message-queue":       { label: "Advanced",      cls: "text-red-700 bg-red-100" },
};

const MASTERY_BORDER: Record<MasteryStatus, string> = {
  NOT_STARTED: "border-gray-200",
  LEARNING:    "border-blue-300",
  PRACTICED:   "border-yellow-300",
  MASTERED:    "border-green-300",
};

export default function SystemDesignPage() {
  const [topics, setTopics]   = useState<Pattern[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patternsApi.list({ category: "SYSTEM_DESIGN" })
      .then((r) => setTopics(r.data as Pattern[]))
      .finally(() => setLoading(false));
  }, []);

  const mastered  = topics.filter((t) => t.masteryStatus === "MASTERED").length;
  const practiced = topics.filter((t) => t.masteryStatus === "PRACTICED").length;
  const learning  = topics.filter((t) => t.masteryStatus === "LEARNING").length;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-5xl mx-auto">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-2">
            <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
            <ChevronRight size={12} />
            <span className="text-gray-600 font-medium">System Design</span>
          </div>
          <div className="flex items-start justify-between gap-6">
            <div>
              <h1 className="text-xl font-bold text-gray-900">System Design</h1>
              <p className="text-gray-500 text-sm mt-0.5 max-w-xl">
                Master the architectural patterns behind real-world systems. Each topic covers
                core concepts, trade-offs, and the exact questions asked at FAANG interviews.
              </p>
            </div>
          </div>

          {!loading && topics.length > 0 && (
            <div className="flex gap-5 mt-3 text-xs">
              <span className="flex items-center gap-1.5 text-green-600">
                <span className="w-2 h-2 rounded-full bg-green-500 inline-block" /> {mastered} mastered
              </span>
              <span className="flex items-center gap-1.5 text-yellow-600">
                <span className="w-2 h-2 rounded-full bg-yellow-400 inline-block" /> {practiced} practiced
              </span>
              <span className="flex items-center gap-1.5 text-blue-600">
                <span className="w-2 h-2 rounded-full bg-blue-400 inline-block" /> {learning} learning
              </span>
              <span className="text-gray-400">{topics.length - mastered - practiced - learning} not started</span>
            </div>
          )}
        </div>
      </div>

      {/* Topic grid */}
      <div className="max-w-5xl mx-auto px-6 py-6">
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="h-36 bg-gray-100 rounded-xl animate-pulse" />
            ))}
          </div>
        ) : topics.length === 0 ? (
          <div className="card p-12 text-center">
            <p className="text-4xl mb-3">🏗️</p>
            <p className="text-gray-500 text-sm">No topics found. Make sure you&apos;re signed in.</p>
          </div>
        ) : (
          <>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mb-4">
              Core Topics — {topics.length} systems
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {topics.map((topic) => {
                const mastery = (topic.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
                const icon    = TOPIC_ICONS[topic.slug] ?? "🏗️";
                const diff    = DIFFICULTY[topic.slug] ?? { label: "Intermediate", cls: "text-yellow-700 bg-yellow-100" };
                const clues   = topic.recognitionClues.filter(Boolean).slice(0, 3);

                return (
                  <Link key={topic.id} href={`/system-design/${topic.slug}`}
                    className={`card hover:border-brand-300 hover:shadow-md p-5 flex items-start gap-4 transition-all group border-2 ${MASTERY_BORDER[mastery]}`}>
                    <span className="text-3xl shrink-0">{icon}</span>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <h3 className="font-semibold text-gray-900 group-hover:text-brand-600 transition-colors">
                          {topic.name}
                        </h3>
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${diff.cls}`}>
                          {diff.label}
                        </span>
                        {mastery === "MASTERED" && (
                          <CheckCircle2 size={14} className="text-green-500" />
                        )}
                      </div>
                      <p className="text-gray-500 text-sm leading-relaxed line-clamp-2">{topic.summary}</p>

                      {clues.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 mt-2">
                          {clues.map((clue, i) => (
                            <span key={i} className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full">
                              {clue.trim()}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>

                    <ChevronRight size={16} className="text-gray-300 group-hover:text-brand-400 transition-colors shrink-0 mt-1" />
                  </Link>
                );
              })}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
