"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { ChevronDown, ChevronRight, Lightbulb, ExternalLink } from "lucide-react";
import { problemsApi } from "@/lib/api/problems";
import { difficultyBadge } from "@/lib/utils";

export default function SystemDesignQuestionPage({ params }: { params: { slug: string } }) {
  const [problem, setProblem] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showAnswer, setShowAnswer] = useState(false);
  const [parentTopic, setParentTopic] = useState<{ slug: string; name: string } | null>(null);

  useEffect(() => {
    problemsApi.get(params.slug).then((r) => {
      setProblem(r.data);
      const patterns: Array<{ slug: string; name: string }> = (r.data as any)?.patterns ?? [];
      if (patterns.length > 0) {
        setParentTopic({ slug: patterns[0].slug, name: patterns[0].name });
      }
    }).finally(() => setLoading(false));
  }, [params.slug]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-400">
        Loading…
      </div>
    );
  }
  if (!problem) return null;

  let exampleAnswer = "";
  try {
    const examples = JSON.parse(problem.examples ?? "[]");
    if (examples[0]) {
      exampleAnswer = examples[0].output ?? "";
    }
  } catch {}

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Header */}
      <div className="border-b border-gray-800 bg-gray-900 px-6 py-5">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-2 text-sm text-gray-500 mb-3">
            <Link href="/system-design" className="hover:text-white transition-colors">System Design</Link>
            {parentTopic && (
              <>
                <ChevronRight size={14} />
                <Link href={`/system-design/${parentTopic.slug}`} className="hover:text-white transition-colors">
                  {parentTopic.name}
                </Link>
              </>
            )}
            <ChevronRight size={14} />
            <span className="text-gray-400">Question</span>
          </div>

          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold">{problem.title}</h1>
              {problem.constraints && (
                <p className="text-gray-500 text-sm mt-1">{problem.constraints}</p>
              )}
            </div>
            <span className={`shrink-0 mt-1 ${difficultyBadge(problem.difficulty)}`}>
              {problem.difficulty}
            </span>
          </div>
        </div>
      </div>

      {/* Body */}
      <div className="max-w-4xl mx-auto px-6 py-8 space-y-8">

        {/* Question */}
        <div className="prose prose-invert prose-sm max-w-none
          prose-headings:text-white prose-headings:font-bold
          prose-h2:text-xl prose-h2:border-b prose-h2:border-gray-800 prose-h2:pb-2
          prose-h3:text-brand-400 prose-h3:text-base
          prose-p:text-gray-300 prose-p:leading-relaxed
          prose-code:text-green-300 prose-code:bg-gray-800 prose-code:px-1 prose-code:rounded
          prose-pre:bg-gray-900 prose-pre:border prose-pre:border-gray-700 prose-pre:rounded-xl
          prose-table:text-sm prose-th:text-gray-400 prose-td:text-gray-300
          prose-strong:text-white prose-li:text-gray-300">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {problem.description}
          </ReactMarkdown>
        </div>

        {/* Answer toggle */}
        {exampleAnswer && (
          <div className="border border-gray-800 rounded-xl overflow-hidden">
            <button
              onClick={() => setShowAnswer((v) => !v)}
              className="w-full flex items-center justify-between px-5 py-4 bg-gray-900 hover:bg-gray-800 transition-colors text-left"
            >
              <div className="flex items-center gap-2">
                <Lightbulb size={16} className="text-yellow-400" />
                <span className="font-medium text-sm">Key Answer / Hint</span>
                <span className="text-xs text-gray-500">(click to reveal)</span>
              </div>
              {showAnswer
                ? <ChevronDown size={16} className="text-gray-400" />
                : <ChevronRight size={16} className="text-gray-400" />
              }
            </button>

            {showAnswer && (
              <div className="px-5 py-4 bg-gray-900/50 border-t border-gray-800">
                <p className="text-gray-300 text-sm leading-relaxed">{exampleAnswer}</p>
                {(() => {
                  try {
                    const ex = JSON.parse(problem.examples ?? "[]")[0];
                    return ex?.explanation ? (
                      <p className="text-gray-500 text-xs mt-2 italic">{ex.explanation}</p>
                    ) : null;
                  } catch { return null; }
                })()}
              </div>
            )}
          </div>
        )}

        {/* Navigation */}
        <div className="flex items-center justify-between pt-4 border-t border-gray-800">
          {parentTopic ? (
            <Link
              href={`/system-design/${parentTopic.slug}?tab=questions`}
              className="flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors"
            >
              ← Back to {parentTopic.name}
            </Link>
          ) : (
            <Link href="/system-design" className="flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors">
              ← System Design
            </Link>
          )}

          <Link
            href="/system-design"
            className="flex items-center gap-2 text-sm text-brand-400 hover:text-brand-300 transition-colors"
          >
            All Topics <ExternalLink size={13} />
          </Link>
        </div>
      </div>
    </div>
  );
}
