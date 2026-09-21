"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern } from "@/types";

export default function PatternsPage() {
  const [patterns, setPatterns] = useState<Pattern[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patternsApi.list().then((r) => setPatterns(r.data)).finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-4xl mx-auto space-y-6">
        <h1 className="text-2xl font-bold">DSA Patterns</h1>
        <p className="text-gray-400">Master these patterns to solve 95% of interview problems.</p>

        {loading ? (
          <p className="text-gray-500">Loading patterns...</p>
        ) : (
          <div className="grid gap-4">
            {patterns.map((p) => (
              <Link
                key={p.id}
                href={`/patterns/${p.slug}`}
                className="bg-gray-900 hover:bg-gray-800 border border-gray-800 hover:border-brand-500/50 rounded-xl p-5 transition-all group"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="font-semibold text-lg group-hover:text-brand-400 transition-colors">{p.name}</h2>
                    <p className="text-gray-400 text-sm mt-1">{p.summary}</p>
                  </div>
                  <span className="text-gray-600 text-xs mt-1">#{p.order}</span>
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
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
