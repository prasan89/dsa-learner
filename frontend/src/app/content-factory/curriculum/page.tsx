"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { curriculumApi, type CfCurriculum } from "@/lib/api/curriculum";

const STATUS_COLOR: Record<string, string> = {
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

export default function CurriculumListPage() {
  const [curricula, setCurricula] = useState<CfCurriculum[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    curriculumApi
      .list("de")
      .then((r) => setCurricula(r.data))
      .catch((e) => setError(e.message ?? "Failed to load curricula"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-6">
        <div className="space-y-1">
          <p className="text-xs font-semibold text-brand-600 uppercase tracking-widest">
            Content Factory
          </p>
          <h1 className="text-2xl font-bold text-gray-900">Curricula</h1>
          <p className="text-sm text-gray-500">
            Language-agnostic curriculum management — A1 through C2
          </p>
        </div>

        {loading && (
          <p className="text-sm text-gray-500 animate-pulse">Loading...</p>
        )}
        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded px-3 py-2">
            {error}
          </p>
        )}

        {!loading && !error && curricula.length === 0 && (
          <p className="text-sm text-gray-500">No curricula found. Create one via the API.</p>
        )}

        {curricula.map((c) => (
          <Link
            key={c.id}
            href={`/content-factory/curriculum/${c.id}`}
            className="block bg-white border border-gray-200 rounded-lg px-5 py-4 hover:border-brand-400 transition-colors"
          >
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-0.5 min-w-0">
                <p className="font-semibold text-gray-900 truncate">{c.displayName}</p>
                <p className="text-xs text-gray-500 font-mono">{c.stableRef}</p>
                {c.description && (
                  <p className="text-sm text-gray-600 mt-1">{c.description}</p>
                )}
              </div>
              <div className="flex flex-col items-end gap-1 shrink-0">
                <span
                  className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    STATUS_COLOR[c.curriculumStatus] ?? "bg-gray-100 text-gray-600"
                  }`}
                >
                  {c.curriculumStatus}
                </span>
                <span className="text-xs text-gray-400">
                  {c.languageCode.toUpperCase()} · v{c.curriculumVersion}
                </span>
                {c.publishGatePassed && (
                  <span className="text-xs text-green-600">gate open</span>
                )}
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
