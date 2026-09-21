"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { patternsApi } from "@/lib/api/patterns";
import { problemsApi } from "@/lib/api/problems";
import type { Pattern, MasteryStatus } from "@/types";
import { difficultyBadge } from "@/lib/utils";
import { toast } from "react-hot-toast";

const MASTERY_OPTIONS: { value: MasteryStatus; label: string; color: string }[] = [
  { value: "NOT_STARTED", label: "Not Started", color: "text-gray-500 bg-gray-800 border-gray-700" },
  { value: "LEARNING", label: "Learning", color: "text-blue-400 bg-blue-500/10 border-blue-500/30" },
  { value: "PRACTICED", label: "Practiced", color: "text-yellow-400 bg-yellow-500/10 border-yellow-500/30" },
  { value: "MASTERED", label: "Mastered", color: "text-green-400 bg-green-500/10 border-green-500/30" },
];

export default function PatternDetailPage({ params }: { params: { slug: string } }) {
  const [pattern, setPattern] = useState<Pattern | null>(null);
  const [problems, setProblems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingMastery, setSavingMastery] = useState(false);

  useEffect(() => {
    patternsApi.get(params.slug).then((r) => {
      setPattern(r.data);
      return problemsApi.list({ patternId: r.data.id });
    }).then((r) => {
      setProblems((r.data as any).content ?? []);
    }).finally(() => setLoading(false));
  }, [params.slug]);

  async function handleMasteryChange(status: MasteryStatus) {
    if (!pattern) return;
    setSavingMastery(true);
    try {
      await patternsApi.updateMastery(pattern.slug, status);
      setPattern((p) => p ? { ...p, masteryStatus: status } : p);
      toast.success(`Mastery updated: ${status.replace(/_/g, " ")}`);
    } catch {
      toast.error("Failed to update mastery");
    } finally {
      setSavingMastery(false);
    }
  }

  if (loading) return <div className="min-h-screen bg-gray-950 text-gray-400 flex items-center justify-center">Loading...</div>;
  if (!pattern) return null;

  const currentMastery = pattern.masteryStatus ?? "NOT_STARTED";
  const masteryOption = MASTERY_OPTIONS.find((o) => o.value === currentMastery) ?? MASTERY_OPTIONS[0];

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-4xl mx-auto space-y-8">

        {/* Header */}
        <div className="flex items-start justify-between gap-4">
          <div>
            <Link href="/patterns" className="text-gray-500 text-sm hover:text-white">← Patterns</Link>
            <h1 className="text-3xl font-bold mt-2">{pattern.name}</h1>
            <p className="text-gray-400 mt-2">{pattern.summary}</p>

            {/* Complexity badges */}
            {(pattern.timeComplexity || pattern.spaceComplexity) && (
              <div className="flex gap-3 mt-3">
                {pattern.timeComplexity && (
                  <span className="text-xs font-mono bg-gray-800 border border-gray-700 rounded px-2 py-1 text-green-400">
                    Time: {pattern.timeComplexity}
                  </span>
                )}
                {pattern.spaceComplexity && (
                  <span className="text-xs font-mono bg-gray-800 border border-gray-700 rounded px-2 py-1 text-blue-400">
                    Space: {pattern.spaceComplexity}
                  </span>
                )}
              </div>
            )}
          </div>

          {/* Mastery selector */}
          <div className="shrink-0">
            <p className="text-xs text-gray-500 mb-2 text-right">Your mastery</p>
            <div className="flex flex-col gap-1">
              {MASTERY_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  onClick={() => handleMasteryChange(opt.value)}
                  disabled={savingMastery || opt.value === currentMastery}
                  className={`text-xs px-3 py-1.5 rounded-lg border font-medium transition-colors ${
                    opt.value === currentMastery
                      ? opt.color + " opacity-100 cursor-default"
                      : "text-gray-500 bg-gray-900 border-gray-800 hover:border-gray-600 hover:text-gray-300"
                  }`}
                >
                  {opt.value === currentMastery ? "● " : "○ "}{opt.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Lesson */}
        {pattern.lessonMarkdown && (
          <div className="bg-gray-900 rounded-xl p-5">
            <h2 className="font-semibold text-brand-400 mb-4">Lesson</h2>
            <div className="prose prose-invert prose-sm max-w-none prose-code:text-green-300 prose-pre:bg-gray-800">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {pattern.lessonMarkdown}
              </ReactMarkdown>
            </div>
          </div>
        )}

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
            {problems.length === 0 ? (
              <p className="px-4 py-6 text-gray-500 text-sm">No problems linked to this pattern yet.</p>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-800 text-gray-400 text-left">
                    <th className="px-4 py-3">Title</th>
                    <th className="px-4 py-3">Difficulty</th>
                  </tr>
                </thead>
                <tbody>
                  {problems.map((p) => (
                    <tr key={p.id} className="border-b border-gray-800 last:border-0 hover:bg-gray-800/50 transition-colors">
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
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
