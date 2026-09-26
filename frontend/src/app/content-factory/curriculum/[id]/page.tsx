"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { curriculumApi, type CfCurriculum, type CfCurriculumLevel } from "@/lib/api/curriculum";

const CURRICULUM_STATUS_COLOR: Record<string, string> = {
  DRAFT: "bg-gray-100 text-gray-600",
  BLUEPRINT_PENDING: "bg-purple-100 text-purple-700",
  BLUEPRINT_GENERATED: "bg-purple-100 text-purple-700",
  BLUEPRINT_VALIDATED: "bg-blue-100 text-blue-700",
  GENERATION_IN_PROGRESS: "bg-blue-100 text-blue-700",
  CURRICULUM_QA_PENDING: "bg-yellow-100 text-yellow-700",
  CURRICULUM_QA_FAILED: "bg-red-100 text-red-700",
  CURRICULUM_QA_PASSED: "bg-green-100 text-green-700",
  APPROVED: "bg-green-100 text-green-800",
  ARCHIVED: "bg-gray-100 text-gray-400",
};

const LEVEL_STATUS_COLOR: Record<string, string> = {
  PLANNED: "bg-gray-100 text-gray-600",
  GENERATION_IN_PROGRESS: "bg-blue-100 text-blue-700",
  LEVEL_QA_PENDING: "bg-yellow-100 text-yellow-700",
  LEVEL_QA_FAILED: "bg-red-100 text-red-700",
  LEVEL_QA_PASSED: "bg-green-100 text-green-700",
  APPROVED: "bg-green-100 text-green-800",
  ARCHIVED: "bg-gray-100 text-gray-400",
};

export default function CurriculumDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [curriculum, setCurriculum] = useState<CfCurriculum | null>(null);
  const [levels, setLevels] = useState<CfCurriculumLevel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMsg, setActionMsg] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!id) return;
    Promise.all([curriculumApi.get(id), curriculumApi.getLevels(id)])
      .then(([cr, lr]) => {
        setCurriculum(cr.data);
        setLevels(lr.data);
      })
      .catch((e) => setError(e.message ?? "Failed to load"))
      .finally(() => setLoading(false));
  }, [id]);

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

  if (loading) return <div className="p-8 text-sm text-gray-500 animate-pulse">Loading...</div>;
  if (error) return <div className="p-8 text-sm text-red-600">{error}</div>;
  if (!curriculum) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-6">
        {/* Breadcrumb */}
        <nav className="text-xs text-gray-400 flex items-center gap-1">
          <Link href="/content-factory/curriculum" className="hover:text-brand-600">Curricula</Link>
          <span>/</span>
          <span className="text-gray-700">{curriculum.displayName}</span>
        </nav>

        {/* Header */}
        <div className="bg-white border border-gray-200 rounded-lg px-5 py-4 space-y-3">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-0.5">
              <h1 className="text-xl font-bold text-gray-900">{curriculum.displayName}</h1>
              <p className="text-xs text-gray-500 font-mono">{curriculum.stableRef}</p>
            </div>
            <span
              className={`text-xs font-medium px-2 py-0.5 rounded-full shrink-0 ${
                CURRICULUM_STATUS_COLOR[curriculum.curriculumStatus] ?? "bg-gray-100 text-gray-600"
              }`}
            >
              {curriculum.curriculumStatus}
            </span>
          </div>

          <dl className="grid grid-cols-3 gap-x-4 gap-y-1 text-xs">
            <div>
              <dt className="text-gray-400">Language</dt>
              <dd className="font-medium text-gray-700">{curriculum.languageCode.toUpperCase()}</dd>
            </div>
            <div>
              <dt className="text-gray-400">Domain</dt>
              <dd className="font-medium text-gray-700">{curriculum.domainCode}</dd>
            </div>
            <div>
              <dt className="text-gray-400">Batch size</dt>
              <dd className="font-medium text-gray-700">{curriculum.batchSize}</dd>
            </div>
            <div>
              <dt className="text-gray-400">Publish gate</dt>
              <dd className={curriculum.publishGatePassed ? "text-green-600 font-medium" : "text-gray-500"}>
                {curriculum.publishGatePassed ? "OPEN" : "CLOSED"}
              </dd>
            </div>
            <div>
              <dt className="text-gray-400">Version</dt>
              <dd className="font-medium text-gray-700">v{curriculum.curriculumVersion}</dd>
            </div>
          </dl>

          {/* Actions */}
          <div className="flex flex-wrap gap-2 pt-1 border-t border-gray-100">
            {curriculum.curriculumStatus === "DRAFT" && (
              <button
                onClick={() => handleAction(() => curriculumApi.startBlueprint(id), "Start blueprint")}
                className="text-xs bg-brand-600 text-white px-3 py-1 rounded hover:bg-brand-700 transition-colors"
              >
                Start Blueprint
              </button>
            )}
            {curriculum.curriculumStatus === "CURRICULUM_QA_PASSED" && (
              <button
                onClick={() => handleAction(() => curriculumApi.approve(id), "Approve")}
                className="text-xs bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 transition-colors"
              >
                Approve
              </button>
            )}
            {(curriculum.curriculumStatus === "GENERATION_IN_PROGRESS" ||
              curriculum.curriculumStatus === "CURRICULUM_QA_FAILED") && (
              <button
                onClick={() => handleAction(() => curriculumApi.submitCoherenceQa(id), "Coherence QA")}
                className="text-xs bg-yellow-600 text-white px-3 py-1 rounded hover:bg-yellow-700 transition-colors"
              >
                Submit Coherence QA
              </button>
            )}
          </div>
          {actionMsg && (
            <p className={`text-xs px-3 py-1 rounded ${
              actionMsg.includes("failed") ? "bg-red-50 text-red-600" : "bg-green-50 text-green-700"
            }`}>
              {actionMsg}
            </p>
          )}
        </div>

        {/* Levels */}
        <div className="space-y-2">
          <h2 className="text-sm font-semibold text-gray-700 uppercase tracking-wide">
            CEFR Levels ({levels.length})
          </h2>
          {levels.length === 0 && (
            <p className="text-sm text-gray-500">No levels yet. Blueprint generation will create them.</p>
          )}
          {levels.map((level) => (
            <Link
              key={level.id}
              href={`/content-factory/curriculum/${id}/levels/${level.id}`}
              className="block bg-white border border-gray-200 rounded-lg px-5 py-3 hover:border-brand-400 transition-colors"
            >
              <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <span className="text-lg font-bold text-brand-600 w-8">{level.cefrLevel}</span>
                  <div>
                    <p className="text-sm font-medium text-gray-900">{level.displayName}</p>
                    {level.targetLessonCount != null && (
                      <p className="text-xs text-gray-500">{level.targetLessonCount} lessons planned</p>
                    )}
                  </div>
                </div>
                <span
                  className={`text-xs font-medium px-2 py-0.5 rounded-full shrink-0 ${
                    LEVEL_STATUS_COLOR[level.levelStatus] ?? "bg-gray-100 text-gray-600"
                  }`}
                >
                  {level.levelStatus}
                </span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
