"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern, MasteryStatus } from "@/types";

const TOPIC_ICONS: Record<string, string> = {
  "url-shortener": "🔗",
  "rate-limiter": "🚦",
  "news-feed": "📰",
  "consistent-hashing": "🔄",
  "distributed-cache": "⚡",
  "search-autocomplete": "🔍",
  "notification-system": "🔔",
  "message-queue": "📨",
};

const DIFFICULTY_LEVEL: Record<string, { label: string; color: string }> = {
  "url-shortener":       { label: "Beginner", color: "text-green-400 bg-green-500/10" },
  "rate-limiter":        { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" },
  "news-feed":           { label: "Advanced", color: "text-red-400 bg-red-500/10" },
  "consistent-hashing":  { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" },
  "distributed-cache":   { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" },
  "search-autocomplete": { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" },
  "notification-system": { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" },
  "message-queue":       { label: "Advanced", color: "text-red-400 bg-red-500/10" },
};

const MASTERY_COLORS: Record<MasteryStatus, string> = {
  NOT_STARTED: "text-gray-500",
  LEARNING:    "text-blue-400",
  PRACTICED:   "text-yellow-400",
  MASTERED:    "text-green-400",
};

const MASTERY_RING: Record<MasteryStatus, string> = {
  NOT_STARTED: "border-gray-700",
  LEARNING:    "border-blue-500",
  PRACTICED:   "border-yellow-500",
  MASTERED:    "border-green-500",
};

export default function SystemDesignPage() {
  const [topics, setTopics] = useState<Pattern[]>([]);
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
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Hero */}
      <div className="border-b border-gray-800 bg-gradient-to-b from-gray-900 to-gray-950 px-6 py-12">
        <div className="max-w-5xl mx-auto">
          <div className="flex items-center gap-3 mb-3">
            <span className="text-3xl">🏗️</span>
            <h1 className="text-3xl font-bold">System Design</h1>
          </div>
          <p className="text-gray-400 text-lg max-w-2xl">
            Master the architectural patterns behind real-world systems. Each topic covers core
            concepts, trade-offs, and the exact questions asked at FAANG interviews.
          </p>

          {!loading && topics.length > 0 && (
            <div className="flex gap-6 mt-6 text-sm">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-green-500" />
                <span className="text-green-400 font-medium">{mastered}</span>
                <span className="text-gray-500">mastered</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-yellow-500" />
                <span className="text-yellow-400 font-medium">{practiced}</span>
                <span className="text-gray-500">practiced</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-blue-500" />
                <span className="text-blue-400 font-medium">{learning}</span>
                <span className="text-gray-500">learning</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-gray-500">{topics.length - mastered - practiced - learning} not started</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Topic Grid */}
      <div className="max-w-5xl mx-auto px-6 py-10">
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="h-40 bg-gray-900 rounded-2xl animate-pulse" />
            ))}
          </div>
        ) : (
          <>
            <h2 className="text-xs font-semibold text-gray-500 uppercase tracking-widest mb-4">
              Core Topics — {topics.length} systems
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {topics.map((topic) => {
                const mastery = (topic.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
                const icon    = TOPIC_ICONS[topic.slug] ?? "🏗️";
                const level   = DIFFICULTY_LEVEL[topic.slug] ?? { label: "Intermediate", color: "text-yellow-400 bg-yellow-500/10" };
                const clues   = topic.recognitionClues.filter(Boolean).slice(0, 3);

                return (
                  <Link
                    key={topic.id}
                    href={`/system-design/${topic.slug}`}
                    className={`group relative bg-gray-900 hover:bg-gray-800 border-2 ${MASTERY_RING[mastery]} hover:border-brand-500 rounded-2xl p-6 transition-all duration-200`}
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-3">
                        <span className="text-3xl">{icon}</span>
                        <div>
                          <h3 className="font-semibold text-white group-hover:text-brand-400 transition-colors text-lg leading-tight">
                            {topic.name}
                          </h3>
                          <span className={`text-xs px-2 py-0.5 rounded-full font-medium mt-1 inline-block ${level.color}`}>
                            {level.label}
                          </span>
                        </div>
                      </div>
                      <span className={`text-xs font-medium ${MASTERY_COLORS[mastery]}`}>
                        {mastery === "NOT_STARTED" ? "" :
                         mastery === "LEARNING"    ? "● Learning" :
                         mastery === "PRACTICED"   ? "● Practiced" :
                                                     "✓ Mastered"}
                      </span>
                    </div>

                    <p className="text-gray-400 text-sm leading-relaxed mb-3 line-clamp-2">
                      {topic.summary}
                    </p>

                    {clues.length > 0 && (
                      <div className="flex flex-wrap gap-1.5">
                        {clues.map((clue, i) => (
                          <span key={i} className="text-xs bg-gray-800 text-gray-500 group-hover:text-gray-400 px-2 py-0.5 rounded-full transition-colors">
                            {clue.trim()}
                          </span>
                        ))}
                      </div>
                    )}

                    {(topic.timeComplexity || topic.spaceComplexity) && (
                      <div className="flex gap-3 mt-3 pt-3 border-t border-gray-800">
                        {topic.timeComplexity && (
                          <span className="text-xs font-mono text-green-400">{topic.timeComplexity}</span>
                        )}
                        {topic.spaceComplexity && (
                          <span className="text-xs font-mono text-blue-400">{topic.spaceComplexity}</span>
                        )}
                      </div>
                    )}
                  </Link>
                );
              })}
            </div>

            {/* Coming soon teaser */}
            <div className="mt-8 p-6 rounded-2xl border border-dashed border-gray-700 text-center">
              <p className="text-gray-600 text-sm">More topics coming: Database Sharding, API Gateway, Service Mesh, CDN, Load Balancer internals…</p>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
