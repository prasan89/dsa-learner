"use client";

import { useState } from "react";
import { Sparkles, Clock, Database, TrendingUp, Zap, Code2, AlertCircle } from "lucide-react";
import { aiApi } from "@/lib/api/ai";
import type { AiReview } from "@/types";

interface AiReviewPanelProps {
  problemSlug: string;
  code: string;
  remainingReviews: number;
  onReviewComplete: (remaining: number) => void;
}

export default function AiReviewPanel({
  problemSlug,
  code,
  remainingReviews,
  onReviewComplete,
}: AiReviewPanelProps) {
  const [review, setReview] = useState<AiReview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleReview() {
    if (!code.trim()) {
      setError("Write some code first.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await aiApi.review(problemSlug, code);
      setReview(res.data);
      const remaining = await aiApi.remaining();
      onReviewComplete(remaining.data.remaining);
    } catch (e: any) {
      if (e?.response?.status === 429) {
        setError("Daily limit reached (5 reviews/day). Try again tomorrow.");
      } else {
        setError("AI review failed. Try again.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex flex-col gap-3 p-3">
      <div className="flex items-center gap-2">
        <Sparkles size={16} className="text-purple-400" />
        <span className="text-sm font-semibold text-purple-400">AI Code Review</span>
        <span className="ml-auto text-xs text-gray-500">{remainingReviews}/5 remaining today</span>
      </div>

      {error && (
        <div className="flex items-center gap-2 text-xs text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg px-3 py-2">
          <AlertCircle size={13} />
          {error}
        </div>
      )}

      {!review && !loading && (
        <button
          onClick={handleReview}
          disabled={remainingReviews === 0}
          className="w-full py-2 rounded-lg bg-purple-600 hover:bg-purple-500 disabled:opacity-40 disabled:cursor-not-allowed text-sm font-medium transition-colors"
        >
          {remainingReviews === 0 ? "Limit reached" : "Review my code"}
        </button>
      )}

      {loading && (
        <div className="text-center py-6 text-sm text-gray-400">
          <Sparkles size={20} className="mx-auto mb-2 animate-pulse text-purple-400" />
          Analyzing your solution…
        </div>
      )}

      {review && (
        <div className="flex flex-col gap-3 text-xs">
          <div className="grid grid-cols-2 gap-2">
            <div className="bg-gray-800 rounded-lg p-2">
              <div className="flex items-center gap-1 text-gray-400 mb-1">
                <Clock size={11} /> Time
              </div>
              <div className="font-mono text-green-400">{review.timeComplexity}</div>
            </div>
            <div className="bg-gray-800 rounded-lg p-2">
              <div className="flex items-center gap-1 text-gray-400 mb-1">
                <Database size={11} /> Space
              </div>
              <div className="font-mono text-blue-400">{review.spaceComplexity}</div>
            </div>
          </div>

          {review.patternUsed && (
            <div className="bg-gray-800 rounded-lg p-2">
              <div className="flex items-center gap-1 text-gray-400 mb-1">
                <Code2 size={11} /> Pattern Used
              </div>
              <div className="text-purple-300">{review.patternUsed}</div>
            </div>
          )}

          <div className="bg-green-500/10 border border-green-500/20 rounded-lg p-2">
            <div className="flex items-center gap-1 text-green-400 mb-1 font-medium">
              <TrendingUp size={11} /> Strengths
            </div>
            <p className="text-gray-300 leading-relaxed">{review.strengths}</p>
          </div>

          <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-lg p-2">
            <div className="flex items-center gap-1 text-yellow-400 mb-1 font-medium">
              <Zap size={11} /> Improvements
            </div>
            <p className="text-gray-300 leading-relaxed">{review.improvements}</p>
          </div>

          {review.optimizedApproach && (
            <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-2">
              <div className="flex items-center gap-1 text-blue-400 mb-1 font-medium">
                <Sparkles size={11} /> Optimized Approach
              </div>
              <p className="text-gray-300 leading-relaxed">{review.optimizedApproach}</p>
            </div>
          )}

          <button
            onClick={() => setReview(null)}
            className="text-gray-500 hover:text-gray-300 text-center py-1 transition-colors"
          >
            Review again
          </button>
        </div>
      )}
    </div>
  );
}
