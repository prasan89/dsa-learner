"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern, MasteryStatus } from "@/types";

const MASTERY_BADGE: Record<MasteryStatus, string> = {
  NOT_STARTED: "text-gray-500 bg-gray-800",
  LEARNING: "text-blue-400 bg-blue-500/10",
  PRACTICED: "text-yellow-400 bg-yellow-500/10",
  MASTERED: "text-green-400 bg-green-500/10",
};

const MASTERY_LABEL: Record<MasteryStatus, string> = {
  NOT_STARTED: "Not Started",
  LEARNING: "Learning",
  PRACTICED: "Practiced",
  MASTERED: "Mastered",
};

export default function PatternsPage() {
  const [patterns, setPatterns] = useState<Pattern[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patternsApi.list()
      .then((r) => setPatterns(r.data as Pattern[]))
      .finally(() => setLoading(false));
  }, []);

  const mastered = patterns.filter((p) => p.masteryStatus === "MASTERED").length;
  const practiced = patterns.filter((p) => p.masteryStatus === "PRACTICED").length;

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-4xl mx-auto space-y-6">
        <div>
          <h1 className="text-2xl font-bold">DSA Patterns</h1>
          <p className="text-gray-400 mt-1">Master these patterns to solve 95% of interview problems.</p>
        </div>

        {!loading && patterns.length > 0 && (
          <div className="flex gap-6 text-sm">
            <span className="text-green-400">{mastered} mastered</span>
            <span className="text-yellow-400">{practiced} practiced</span>
            <span className="text-gray-500">{patterns.length - mastered - practiced} remaining</span>
          </div>
        )}

        {loading ? (
          <p className="text-gray-500">Loading patterns...</p>
        ) : (
          <div className="grid gap-4">
            {patterns.map((p) => {
              const mastery = (p.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
              return (
                <Link
                  key={p.id}
                  href={`/patterns/${p.slug}`}
                  className="bg-gray-900 hover:bg-gray-800 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3">
                        <h2 className="font-semibold text-lg group-hover:text-brand-400 transition-colors">{p.name}</h2>
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${MASTERY_BADGE[mastery]}`}>
                          {MASTERY_LABEL[mastery]}
                        </span>
                      </div>
                      <p className="text-gray-400 text-sm mt-1">{p.summary}</p>
                    </div>
                    {(p.timeComplexity || p.spaceComplexity) && (
                      <div className="text-right shrink-0 ml-4 flex flex-col gap-1">
                        {p.timeComplexity && (
                          <span className="text-xs font-mono text-green-400">{p.timeComplexity}</span>
                        )}
                        {p.spaceComplexity && (
                          <span className="text-xs font-mono text-blue-400">{p.spaceComplexity}</span>
                        )}
                      </div>
                    )}
                  </div>
                  {p.recognitionClues.length > 0 && (
                    <div className="flex flex-wrap gap-2 mt-3">
                      {p.recognitionClues.slice(0, 4).map((clue, i) => (
                        <span key={i} className="text-xs bg-gray-800 text-gray-400 px-2 py-0.5 rounded-full">
                          {clue.trim()}
                        </span>
                      ))}
                    </div>
                  )}
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
