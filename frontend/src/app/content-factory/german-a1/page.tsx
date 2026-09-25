"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { pipelineApi, type CfLesson } from "@/lib/api/pipeline";

const STATUS_COLOR: Record<string, string> = {
  QA_PASSED: "bg-green-100 text-green-800",
  QA_PENDING: "bg-yellow-100 text-yellow-800",
  QA_FAILED: "bg-red-100 text-red-800",
  REVISION: "bg-orange-100 text-orange-800",
  GENERATING: "bg-blue-100 text-blue-800",
  GENERATED: "bg-blue-100 text-blue-800",
  VALIDATION_PENDING: "bg-purple-100 text-purple-800",
  VALIDATION_FAILED: "bg-red-100 text-red-800",
  DRAFT: "bg-gray-100 text-gray-600",
  PLANNED: "bg-gray-100 text-gray-600",
  APPROVED: "bg-green-100 text-green-800",
  HUMAN_REVIEW_REQUIRED: "bg-red-100 text-red-800",
};

const TERMINAL_STATUSES = new Set(["QA_PASSED", "APPROVED", "HUMAN_REVIEW_REQUIRED"]);
const IN_PROGRESS_STATUSES = new Set(["GENERATING", "GENERATED", "VALIDATION_PENDING", "QA_PENDING", "REVISION", "VALIDATION_FAILED", "QA_FAILED"]);

function fmt(n: number, decimals = 4) {
  return n.toFixed(decimals);
}

export default function GermanA1ListPage() {
  const [lessons, setLessons] = useState<CfLesson[]>([]);
  const [costs, setCosts] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [summaryOpen, setSummaryOpen] = useState(true);

  useEffect(() => {
    pipelineApi
      .listLessons("language")
      .then((res) => {
        const german = res.data
          .filter((l) => l.languageCode === "de" && l.cefrLevel === "A1")
          .sort((a, b) => a.stableRef.localeCompare(b.stableRef));
        setLessons(german);

        // Fetch costs for all lessons in parallel
        Promise.allSettled(german.map((l) => pipelineApi.getLessonCost(l.id))).then(
          (results) => {
            const map: Record<string, number> = {};
            results.forEach((r, i) => {
              if (r.status === "fulfilled") {
                map[german[i].id] = Number(r.value.data.total_usd ?? 0);
              }
            });
            setCosts(map);
          }
        );
      })
      .catch((err) => setError(err.message ?? "Failed to load lessons"))
      .finally(() => setLoading(false));
  }, []);

  const totalLessons = lessons.length;
  const completed = lessons.filter((l) => TERMINAL_STATUSES.has(l.contentStatus)).length;
  const inProgress = lessons.filter((l) => IN_PROGRESS_STATUSES.has(l.contentStatus)).length;
  const draft = totalLessons - completed - inProgress;
  const avgRevisions =
    totalLessons > 0
      ? (lessons.reduce((s, l) => s + l.revisionCount, 0) / totalLessons).toFixed(1)
      : "—";
  const totalCost = Object.values(costs).reduce((s, c) => s + c, 0);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-6">
        {/* Header */}
        <div className="space-y-1">
          <p className="text-xs font-semibold text-brand-600 uppercase tracking-widest">
            Content Factory
          </p>
          <h1 className="text-2xl font-bold text-gray-900">German A1</h1>
          <p className="text-sm text-gray-500">
            All German A1 lessons in the Content Factory pipeline.
            Click a lesson to view generated content and QA results.
          </p>
        </div>

        {loading && (
          <div className="py-12 text-center text-sm text-gray-400">Loading…</div>
        )}

        {error && (
          <div className="py-6 text-center text-sm text-red-500">{error}</div>
        )}

        {!loading && !error && (
          <>
            {/* Benchmark Summary */}
            <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
              <button
                onClick={() => setSummaryOpen((o) => !o)}
                className="w-full flex items-center justify-between px-5 py-3.5 text-left hover:bg-gray-50 transition-colors"
              >
                <span className="text-sm font-semibold text-gray-700">Benchmark Summary</span>
                <span className="text-xs text-gray-400">{summaryOpen ? "▲" : "▼"}</span>
              </button>
              {summaryOpen && (
                <div className="px-5 pb-5 pt-1 grid grid-cols-2 sm:grid-cols-4 gap-3">
                  {[
                    { label: "Total", value: String(totalLessons) },
                    { label: "Completed", value: String(completed) },
                    { label: "In Progress", value: String(inProgress) },
                    { label: "Not Started", value: String(draft) },
                    { label: "Avg Revisions", value: avgRevisions },
                    { label: "Total Cost (USD)", value: Object.keys(costs).length > 0 ? `$${fmt(totalCost)}` : "…" },
                  ].map(({ label, value }) => (
                    <div key={label} className="bg-gray-50 rounded-lg px-3 py-2.5">
                      <p className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">{label}</p>
                      <p className="text-sm font-semibold text-gray-800 mt-0.5">{value}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Lesson list */}
            {lessons.length === 0 ? (
              <div className="py-12 text-center text-sm text-gray-400">
                No German A1 lessons found.
              </div>
            ) : (
              <div className="divide-y divide-gray-100 bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
                {lessons.map((lesson, i) => (
                  <Link
                    key={lesson.id}
                    href={`/content-factory/german-a1/${lesson.id}`}
                    className="flex items-center gap-4 px-5 py-4 hover:bg-gray-50 transition-colors group"
                  >
                    <span className="w-6 text-sm font-semibold text-gray-400">{i + 1}</span>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold text-gray-900 group-hover:text-brand-700 truncate">
                        {lesson.title}
                      </p>
                      <p className="text-xs text-gray-400 mt-0.5">
                        {lesson.cefrLevel} · v{lesson.currentVersion}
                        {lesson.revisionCount > 0 && ` · ${lesson.revisionCount} revision${lesson.revisionCount > 1 ? "s" : ""}`}
                        {costs[lesson.id] !== undefined && ` · $${fmt(costs[lesson.id])}`}
                      </p>
                    </div>
                    <span
                      className={`text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_COLOR[lesson.contentStatus] ?? "bg-gray-100 text-gray-600"}`}
                    >
                      {lesson.contentStatus.replace(/_/g, " ")}
                    </span>
                    <span className="text-gray-300 group-hover:text-brand-400">→</span>
                  </Link>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
