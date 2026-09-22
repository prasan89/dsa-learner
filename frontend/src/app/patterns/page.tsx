"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ChevronRight, CheckCircle2 } from "lucide-react";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern, MasteryStatus } from "@/types";

const MASTERY_STYLE: Record<MasteryStatus, string> = {
  NOT_STARTED: "text-gray-500 bg-gray-100",
  LEARNING:    "text-blue-700 bg-blue-50",
  PRACTICED:   "text-yellow-700 bg-yellow-50",
  MASTERED:    "text-green-700 bg-green-50",
};

const MASTERY_BORDER: Record<MasteryStatus, string> = {
  NOT_STARTED: "border-gray-200",
  LEARNING:    "border-blue-200",
  PRACTICED:   "border-yellow-200",
  MASTERED:    "border-green-200",
};

export default function PatternsPage() {
  const [patterns, setPatterns] = useState<Pattern[]>([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    patternsApi.list({ category: "DSA" })
      .then((r) => setPatterns(r.data as Pattern[]))
      .finally(() => setLoading(false));
  }, []);

  const mastered  = patterns.filter((p) => p.masteryStatus === "MASTERED").length;
  const practiced = patterns.filter((p) => p.masteryStatus === "PRACTICED").length;
  const learning  = patterns.filter((p) => p.masteryStatus === "LEARNING").length;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-2">
            <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
            <ChevronRight size={12} />
            <span className="text-gray-600 font-medium">Patterns</span>
          </div>
          <h1 className="text-xl font-bold text-gray-900">DSA Pattern Learning Path</h1>
          <p className="text-gray-500 text-sm mt-0.5">Master these patterns to solve 95% of interview problems.</p>

          {!loading && patterns.length > 0 && (
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
              <span className="text-gray-400">{patterns.length - mastered - practiced - learning} not started</span>
            </div>
          )}
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-6 py-6">
        {loading ? (
          <div className="grid gap-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-20 bg-gray-100 rounded-xl animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="grid gap-3">
            {patterns.map((p, i) => {
              const mastery = (p.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
              return (
                <Link key={p.id} href={`/patterns/${p.slug}`}
                  className={`card hover:border-brand-300 hover:shadow-md p-5 flex items-start gap-4 transition-all group border-2 ${MASTERY_BORDER[mastery]}`}>
                  {/* Step number */}
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0 mt-0.5 ${
                    mastery === "MASTERED" ? "bg-green-100 text-green-700" :
                    mastery === "PRACTICED" ? "bg-yellow-100 text-yellow-700" :
                    mastery === "LEARNING" ? "bg-blue-100 text-blue-700" :
                    "bg-gray-100 text-gray-500"
                  }`}>
                    {mastery === "MASTERED" ? <CheckCircle2 size={16} /> : i + 1}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h2 className="font-semibold text-gray-900 group-hover:text-brand-600 transition-colors">
                        {p.name}
                      </h2>
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${MASTERY_STYLE[mastery]}`}>
                        {mastery.replace(/_/g, " ")}
                      </span>
                    </div>
                    <p className="text-gray-500 text-sm mt-0.5 leading-relaxed">{p.summary}</p>

                    {p.recognitionClues.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mt-2">
                        {p.recognitionClues.slice(0, 4).filter(Boolean).map((clue, j) => (
                          <span key={j} className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full">
                            {clue.trim()}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  {(p.timeComplexity || p.spaceComplexity) && (
                    <div className="text-right shrink-0 space-y-1">
                      {p.timeComplexity && (
                        <p className="text-xs font-mono text-green-600 bg-green-50 px-2 py-0.5 rounded">{p.timeComplexity}</p>
                      )}
                      {p.spaceComplexity && (
                        <p className="text-xs font-mono text-blue-600 bg-blue-50 px-2 py-0.5 rounded">{p.spaceComplexity}</p>
                      )}
                    </div>
                  )}

                  <ChevronRight size={16} className="text-gray-300 group-hover:text-brand-400 transition-colors shrink-0 mt-1" />
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
