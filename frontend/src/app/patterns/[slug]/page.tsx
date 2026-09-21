"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { patternsApi } from "@/lib/api/patterns";
import { problemsApi } from "@/lib/api/problems";
import type { Pattern } from "@/types";
import { difficultyBadge } from "@/lib/utils";

export default function PatternDetailPage({ params }: { params: { slug: string } }) {
  const [pattern, setPattern] = useState<Pattern | null>(null);
  const [problems, setProblems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patternsApi.get(params.slug).then((r) => {
      setPattern(r.data);
      return problemsApi.list({ patternId: r.data.id });
    }).then((r) => {
      setProblems(r.data.problems ?? []);
    }).finally(() => setLoading(false));
  }, [params.slug]);

  if (loading) return <div className="min-h-screen bg-gray-950 text-gray-400 flex items-center justify-center">Loading...</div>;
  if (!pattern) return null;

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-4xl mx-auto space-y-8">
        <div>
          <Link href="/patterns" className="text-gray-500 text-sm hover:text-white">← Patterns</Link>
          <h1 className="text-3xl font-bold mt-2">{pattern.name}</h1>
          <p className="text-gray-400 mt-2">{pattern.summary}</p>
        </div>

        {/* Recognition clues */}
        <div className="bg-gray-900 rounded-xl p-5">
          <h2 className="font-semibold text-brand-400 mb-3">Recognition Clues</h2>
          <ul className="space-y-1">
            {pattern.recognitionClues.map((clue, i) => (
              <li key={i} className="flex items-center gap-2 text-gray-300 text-sm">
                <span className="text-green-400">✓</span> {clue.trim()}
              </li>
            ))}
          </ul>
        </div>

        {/* Template */}
        {pattern.templateCode && (
          <div className="bg-gray-900 rounded-xl p-5">
            <h2 className="font-semibold text-brand-400 mb-3">Java Template</h2>
            <pre className="font-mono text-sm text-gray-300 overflow-x-auto whitespace-pre-wrap">
              {pattern.templateCode}
            </pre>
          </div>
        )}

        {/* Practice problems */}
        <div>
          <h2 className="font-semibold text-lg mb-3">Practice Problems</h2>
          <div className="bg-gray-900 rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-800 text-gray-400 text-left">
                  <th className="px-4 py-3">Title</th>
                  <th className="px-4 py-3">Difficulty</th>
                </tr>
              </thead>
              <tbody>
                {problems.map((p) => (
                  <tr key={p.id} className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors">
                    <td className="px-4 py-3">
                      <Link href={`/problems/${p.slug}`} className="text-white hover:text-brand-400 transition-colors">
                        {p.solved && <span className="text-green-400 mr-2">✓</span>}
                        {p.title}
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
