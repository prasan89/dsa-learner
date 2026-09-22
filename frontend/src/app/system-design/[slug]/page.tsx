"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { toast } from "react-hot-toast";
import { patternsApi } from "@/lib/api/patterns";
import { problemsApi } from "@/lib/api/problems";
import { difficultyBadge } from "@/lib/utils";
import type { Pattern, MasteryStatus } from "@/types";

const MASTERY_OPTIONS: { value: MasteryStatus; label: string; color: string }[] = [
  { value: "NOT_STARTED", label: "Not Started", color: "text-gray-500 bg-gray-800 border-gray-700" },
  { value: "LEARNING",    label: "Learning",    color: "text-blue-400 bg-blue-500/10 border-blue-500/30" },
  { value: "PRACTICED",   label: "Practiced",   color: "text-yellow-400 bg-yellow-500/10 border-yellow-500/30" },
  { value: "MASTERED",    label: "Mastered",    color: "text-green-400 bg-green-500/10 border-green-500/30" },
];

const TOPIC_ICONS: Record<string, string> = {
  "url-shortener": "🔗", "rate-limiter": "🚦", "news-feed": "📰",
  "consistent-hashing": "🔄", "distributed-cache": "⚡",
  "search-autocomplete": "🔍", "notification-system": "🔔", "message-queue": "📨",
};

type ActiveTab = "lesson" | "questions";

export default function SystemDesignTopicPage({ params }: { params: { slug: string } }) {
  const [topic, setTopic]       = useState<Pattern | null>(null);
  const [problems, setProblems] = useState<any[]>([]);
  const [loading, setLoading]   = useState(true);
  const [activeTab, setActiveTab] = useState<ActiveTab>("lesson");
  const [saving, setSaving]     = useState(false);

  useEffect(() => {
    patternsApi.get(params.slug).then((r) => {
      setTopic(r.data);
      return problemsApi.list({ patternId: r.data.id });
    }).then((r) => {
      setProblems((r.data as any).content ?? []);
    }).finally(() => setLoading(false));
  }, [params.slug]);

  async function handleMasteryChange(status: MasteryStatus) {
    if (!topic) return;
    setSaving(true);
    try {
      await patternsApi.updateMastery(topic.slug, status);
      setTopic((t) => t ? { ...t, masteryStatus: status } : t);
      toast.success(`Marked as ${status.replace(/_/g, " ").toLowerCase()}`);
    } catch {
      toast.error("Failed to update mastery");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-400">
        Loading…
      </div>
    );
  }
  if (!topic) return null;

  const currentMastery = (topic.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
  const masteryOption  = MASTERY_OPTIONS.find((o) => o.value === currentMastery) ?? MASTERY_OPTIONS[0];
  const icon = TOPIC_ICONS[topic.slug] ?? "🏗️";

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Header */}
      <div className="border-b border-gray-800 bg-gray-900 px-6 py-6">
        <div className="max-w-5xl mx-auto">
          <Link href="/system-design" className="text-gray-500 text-sm hover:text-white transition-colors">
            ← System Design
          </Link>

          <div className="flex items-start justify-between mt-4 gap-6">
            <div className="flex items-start gap-4">
              <span className="text-5xl">{icon}</span>
              <div>
                <h1 className="text-3xl font-bold">{topic.name}</h1>
                <p className="text-gray-400 mt-1 max-w-2xl">{topic.summary}</p>
                {(topic.timeComplexity || topic.spaceComplexity) && (
                  <div className="flex gap-3 mt-3">
                    {topic.timeComplexity && (
                      <span className="text-xs font-mono bg-gray-800 border border-gray-700 rounded px-2 py-1 text-green-400">
                        Time: {topic.timeComplexity}
                      </span>
                    )}
                    {topic.spaceComplexity && (
                      <span className="text-xs font-mono bg-gray-800 border border-gray-700 rounded px-2 py-1 text-blue-400">
                        Space: {topic.spaceComplexity}
                      </span>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Mastery */}
            <div className="shrink-0">
              <p className="text-xs text-gray-500 mb-2 text-right">Your progress</p>
              <div className="flex flex-col gap-1">
                {MASTERY_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    onClick={() => handleMasteryChange(opt.value)}
                    disabled={saving || opt.value === currentMastery}
                    className={`text-xs px-3 py-1.5 rounded-lg border font-medium transition-colors ${
                      opt.value === currentMastery
                        ? opt.color + " cursor-default"
                        : "text-gray-500 bg-gray-900 border-gray-800 hover:border-gray-600 hover:text-gray-300"
                    }`}
                  >
                    {opt.value === currentMastery ? "● " : "○ "}{opt.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-1 mt-6">
            {([
              { id: "lesson" as const,    label: "📖 Lesson" },
              { id: "questions" as const, label: `💬 Interview Questions (${problems.length})` },
            ] as const).map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  activeTab === tab.id
                    ? "bg-brand-600 text-white"
                    : "text-gray-400 hover:text-white hover:bg-gray-800"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-5xl mx-auto px-6 py-8">

        {activeTab === "lesson" && topic.lessonMarkdown && (
          <div className="prose prose-invert prose-sm max-w-none
            prose-headings:text-white prose-headings:font-bold
            prose-h2:text-xl prose-h2:border-b prose-h2:border-gray-800 prose-h2:pb-2
            prose-h3:text-brand-400 prose-h3:text-base
            prose-p:text-gray-300 prose-p:leading-relaxed
            prose-code:text-green-300 prose-code:bg-gray-800 prose-code:px-1 prose-code:rounded prose-code:text-sm
            prose-pre:bg-gray-900 prose-pre:border prose-pre:border-gray-700 prose-pre:rounded-xl
            prose-table:text-sm prose-th:text-gray-400 prose-td:text-gray-300
            prose-strong:text-white
            prose-li:text-gray-300">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {topic.lessonMarkdown}
            </ReactMarkdown>
          </div>
        )}

        {activeTab === "lesson" && !topic.lessonMarkdown && (
          <div className="text-center py-16 text-gray-500">
            <span className="text-4xl">📝</span>
            <p className="mt-3">Lesson content coming soon.</p>
          </div>
        )}

        {activeTab === "questions" && (
          <div className="space-y-4">
            {problems.length === 0 ? (
              <div className="text-center py-16 text-gray-500">
                <span className="text-4xl">💬</span>
                <p className="mt-3">No questions yet.</p>
              </div>
            ) : (
              problems.map((p, i) => (
                <Link
                  key={p.id}
                  href={`/system-design/question/${p.slug}`}
                  className="block bg-gray-900 hover:bg-gray-800 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-3">
                      <span className="text-gray-600 text-sm font-mono mt-0.5 w-6 shrink-0">{i + 1}.</span>
                      <div>
                        <h3 className="font-semibold text-white group-hover:text-brand-400 transition-colors">
                          {p.title}
                        </h3>
                        <p className="text-gray-500 text-sm mt-1 line-clamp-2">
                          {p.description?.split("\n").find((l: string) => l.trim() && !l.startsWith("#"))?.replace(/[*#]/g, "").trim()}
                        </p>
                      </div>
                    </div>
                    <div className="shrink-0 flex flex-col items-end gap-2">
                      <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                      {p.solved && <span className="text-xs text-green-400">✓ Solved</span>}
                    </div>
                  </div>
                </Link>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
