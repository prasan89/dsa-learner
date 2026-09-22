"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { ChevronRight, Lightbulb } from "lucide-react";
import { problemsApi } from "@/lib/api/problems";

export default function SystemDesignQuestionPage({ params }: { params: { slug: string } }) {
  const [problem, setProblem]       = useState<any>(null);
  const [loading, setLoading]       = useState(true);
  const [showAnswer, setShowAnswer] = useState(false);
  const [parentTopic, setParentTopic] = useState<{ slug: string; name: string } | null>(null);

  useEffect(() => {
    problemsApi.get(params.slug).then((r) => {
      setProblem(r.data);
      const patterns: Array<{ slug: string; name: string }> = (r.data as any)?.patterns ?? [];
      if (patterns.length > 0) setParentTopic({ slug: patterns[0].slug, name: patterns[0].name });
    }).finally(() => setLoading(false));
  }, [params.slug]);

  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 text-sm">Loading…</p>
    </div>
  );
  if (!problem) return null;

  let exampleAnswer = "";
  let explanation   = "";
  try {
    const ex = JSON.parse(problem.examples ?? "[]")[0];
    exampleAnswer = ex?.output ?? "";
    explanation   = ex?.explanation ?? "";
  } catch {}

  const diff = problem.difficulty ?? "MEDIUM";

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-3">
            <Link href="/system-design" className="hover:text-gray-600">System Design</Link>
            {parentTopic && (
              <>
                <ChevronRight size={12} />
                <Link href={`/system-design/${parentTopic.slug}`} className="hover:text-gray-600">
                  {parentTopic.name}
                </Link>
              </>
            )}
            <ChevronRight size={12} />
            <span className="text-gray-600 font-medium">Question</span>
          </div>

          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{problem.title}</h1>
              {problem.constraints && (
                <p className="text-gray-400 text-sm mt-1">{problem.constraints}</p>
              )}
            </div>
            <span className={`shrink-0 text-xs font-semibold px-2.5 py-1 rounded-full mt-1 ${
              diff === "EASY"   ? "text-green-700 bg-green-100" :
              diff === "MEDIUM" ? "text-yellow-700 bg-yellow-100" :
                                  "text-red-700 bg-red-100"
            }`}>{diff[0] + diff.slice(1).toLowerCase()}</span>
          </div>
        </div>
      </div>

      {/* Body */}
      <div className="max-w-4xl mx-auto px-6 py-8 space-y-6">
        {/* Question */}
        <div className="card p-6 prose prose-sm max-w-none
          prose-headings:text-gray-900 prose-headings:font-bold
          prose-h2:text-lg prose-h2:border-b prose-h2:border-gray-100 prose-h2:pb-2
          prose-h3:text-brand-700
          prose-p:text-gray-600 prose-p:leading-relaxed
          prose-code:text-green-700 prose-code:bg-green-50 prose-code:px-1.5 prose-code:rounded prose-code:text-xs
          prose-pre:bg-gray-900 prose-pre:text-gray-100 prose-pre:rounded-xl
          prose-strong:text-gray-900 prose-li:text-gray-600">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {problem.description}
          </ReactMarkdown>
        </div>

        {/* Hint / Answer */}
        {exampleAnswer && (
          <div className="card overflow-hidden">
            <button onClick={() => setShowAnswer(v => !v)}
              className="w-full flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors text-left">
              <div className="flex items-center gap-2">
                <Lightbulb size={15} className="text-yellow-500" />
                <span className="font-medium text-sm text-gray-800">Key Answer / Hint</span>
                <span className="text-xs text-gray-400">(click to reveal)</span>
              </div>
              <ChevronRight size={15} className={`text-gray-400 transition-transform ${showAnswer ? "rotate-90" : ""}`} />
            </button>
            {showAnswer && (
              <div className="px-5 py-4 border-t border-gray-100 bg-gray-50">
                <p className="text-gray-700 text-sm leading-relaxed">{exampleAnswer}</p>
                {explanation && (
                  <p className="text-gray-400 text-xs mt-2 italic">{explanation}</p>
                )}
              </div>
            )}
          </div>
        )}

        {/* Navigation */}
        <div className="flex items-center justify-between pt-2">
          {parentTopic ? (
            <Link href={`/system-design/${parentTopic.slug}?tab=interview-qa`}
              className="text-sm text-gray-500 hover:text-gray-800 flex items-center gap-1 transition-colors">
              ← Back to {parentTopic.name}
            </Link>
          ) : (
            <Link href="/system-design"
              className="text-sm text-gray-500 hover:text-gray-800 flex items-center gap-1 transition-colors">
              ← System Design
            </Link>
          )}
          <Link href="/system-design"
            className="text-sm text-brand-600 hover:text-brand-700 font-medium transition-colors">
            All Topics →
          </Link>
        </div>
      </div>
    </div>
  );
}
