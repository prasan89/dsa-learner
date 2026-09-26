"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { curriculumApi, type CfCurriculumLevel, type CfCurriculumLessonPlan } from "@/lib/api/curriculum";

const LEVEL_STATUS_COLOR: Record<string, string> = {
  PLANNED: "bg-gray-100 text-gray-600",
  GENERATION_IN_PROGRESS: "bg-blue-100 text-blue-700",
  LEVEL_QA_PENDING: "bg-yellow-100 text-yellow-700",
  LEVEL_QA_FAILED: "bg-red-100 text-red-700",
  LEVEL_QA_PASSED: "bg-green-100 text-green-700",
  APPROVED: "bg-green-100 text-green-800",
  ARCHIVED: "bg-gray-100 text-gray-400",
};

const PLAN_STATUS_COLOR: Record<string, string> = {
  PLANNED: "bg-gray-100 text-gray-500",
  BLOCKED: "bg-red-50 text-red-600",
  QUEUED: "bg-blue-50 text-blue-600",
  GENERATING: "bg-blue-100 text-blue-700",
  GENERATED: "bg-blue-100 text-blue-700",
  QA_PENDING: "bg-yellow-100 text-yellow-700",
  QA_FAILED: "bg-red-100 text-red-700",
  REVISION: "bg-orange-100 text-orange-700",
  QA_PASSED: "bg-green-100 text-green-700",
  APPROVED: "bg-green-100 text-green-800",
  PUBLISHED: "bg-green-200 text-green-900",
  SKIPPED: "bg-gray-100 text-gray-400",
};

export default function LevelDetailPage() {
  const { id, levelId } = useParams<{ id: string; levelId: string }>();
  const [level, setLevel] = useState<CfCurriculumLevel | null>(null);
  const [plans, setPlans] = useState<CfCurriculumLessonPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMsg, setActionMsg] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!id || !levelId) return;
    Promise.all([
      curriculumApi.getLevels(id),
      curriculumApi.getLevelPlans(id, levelId),
    ])
      .then(([lr, pr]) => {
        const found = lr.data.find((l) => l.id === levelId) ?? null;
        setLevel(found);
        setPlans(pr.data);
      })
      .catch((e) => setError(e.message ?? "Failed to load"))
      .finally(() => setLoading(false));
  }, [id, levelId]);

  useEffect(() => { load(); }, [load]);

  const handleAction = async (fn: () => Promise<unknown>, label: string) => {
    try {
      setActionMsg(null);
      await fn();
      setActionMsg(`${label} succeeded`);
      load();
    } catch (e: unknown) {
      setActionMsg(`${label} failed: ${(e as Error).message ?? "unknown error"}`);
    }
  };

  // Progress counts
  const total = plans.length;
  const passing = plans.filter((p) =>
    ["QA_PASSED", "APPROVED", "PUBLISHED"].includes(p.planStatus)
  ).length;
  const inProgress = plans.filter((p) =>
    ["QUEUED", "GENERATING", "GENERATED", "QA_PENDING", "REVISION", "QA_FAILED"].includes(p.planStatus)
  ).length;
  const progressPct = total > 0 ? Math.round((passing / total) * 100) : 0;

  if (loading) return <div className="p-8 text-sm text-gray-500 animate-pulse">Loading...</div>;
  if (error) return <div className="p-8 text-sm text-red-600">{error}</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-6">
        {/* Breadcrumb */}
        <nav className="text-xs text-gray-400 flex items-center gap-1">
          <Link href="/content-factory/curriculum" className="hover:text-brand-600">Curricula</Link>
          <span>/</span>
          <Link href={`/content-factory/curriculum/${id}`} className="hover:text-brand-600">Curriculum</Link>
          <span>/</span>
          <span className="text-gray-700">{level?.cefrLevel ?? levelId}</span>
        </nav>

        {/* Level header */}
        {level && (
          <div className="bg-white border border-gray-200 rounded-lg px-5 py-4 space-y-3">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h1 className="text-xl font-bold text-gray-900">
                  <span className="text-brand-600 mr-2">{level.cefrLevel}</span>
                  {level.displayName}
                </h1>
                {level.targetLessonCount != null && (
                  <p className="text-xs text-gray-500">{level.targetLessonCount} lessons planned</p>
                )}
              </div>
              <span
                className={`text-xs font-medium px-2 py-0.5 rounded-full shrink-0 ${
                  LEVEL_STATUS_COLOR[level.levelStatus] ?? "bg-gray-100 text-gray-600"
                }`}
              >
                {level.levelStatus}
              </span>
            </div>

            {/* Progress bar */}
            {total > 0 && (
              <div className="space-y-1">
                <div className="flex justify-between text-xs text-gray-500">
                  <span>{passing}/{total} passing</span>
                  <span>{progressPct}%</span>
                </div>
                <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-green-500 rounded-full transition-all"
                    style={{ width: `${progressPct}%` }}
                  />
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex flex-wrap gap-2 pt-1 border-t border-gray-100">
              {level.levelStatus === "PLANNED" && (
                <button
                  onClick={() =>
                    handleAction(
                      () => curriculumApi.startLevelGeneration(id, levelId),
                      "Start generation"
                    )
                  }
                  className="text-xs bg-brand-600 text-white px-3 py-1 rounded hover:bg-brand-700 transition-colors"
                >
                  Start Generation
                </button>
              )}
              {level.levelStatus === "GENERATION_IN_PROGRESS" && (
                <button
                  onClick={() =>
                    handleAction(() => curriculumApi.submitLevelQa(id, levelId), "Level QA")
                  }
                  className="text-xs bg-yellow-600 text-white px-3 py-1 rounded hover:bg-yellow-700 transition-colors"
                >
                  Submit Level QA
                </button>
              )}
              {level.levelStatus === "LEVEL_QA_PASSED" && (
                <button
                  onClick={() =>
                    handleAction(() => curriculumApi.approveLevel(id, levelId), "Approve level")
                  }
                  className="text-xs bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 transition-colors"
                >
                  Approve Level
                </button>
              )}
            </div>
            {actionMsg && (
              <p
                className={`text-xs px-3 py-1 rounded ${
                  actionMsg.includes("failed") ? "bg-red-50 text-red-600" : "bg-green-50 text-green-700"
                }`}
              >
                {actionMsg}
              </p>
            )}
          </div>
        )}

        {/* Plans table */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
              Lesson Plans ({total})
            </h2>
            <div className="flex gap-3 text-xs text-gray-500">
              <span>{inProgress} in progress</span>
              <span>{passing} passing</span>
            </div>
          </div>

          {plans.length === 0 && (
            <p className="text-sm text-gray-500">
              No lesson plans yet. Generate a blueprint to populate plans.
            </p>
          )}

          <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
            {plans.map((plan, i) => (
              <div
                key={plan.id}
                className={`px-4 py-3 flex items-center gap-3 ${
                  i > 0 ? "border-t border-gray-100" : ""
                }`}
              >
                <span className="text-xs text-gray-400 w-6 text-right shrink-0">
                  {plan.position}
                </span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{plan.title}</p>
                  <p className="text-xs text-gray-500 font-mono truncate">{plan.stableRef}</p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  {plan.lessonId && (
                    <Link
                      href={`/content-factory/german-a1/${plan.lessonId}`}
                      className="text-xs text-brand-600 hover:underline"
                    >
                      lesson
                    </Link>
                  )}
                  <span
                    className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                      PLAN_STATUS_COLOR[plan.planStatus] ?? "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {plan.planStatus}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
