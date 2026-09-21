"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Wand2, Sparkles, AlertCircle } from "lucide-react";
import { aiApi } from "@/lib/api/ai";
import type { PatternDetection } from "@/types";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

const CONFIDENCE_COLORS: Record<string, string> = {
  HIGH: "text-green-400 bg-green-500/10 border-green-500/30",
  MEDIUM: "text-yellow-400 bg-yellow-500/10 border-yellow-500/30",
  LOW: "text-red-400 bg-red-500/10 border-red-500/30",
};

const PLACEHOLDER = `// Paste any Java code snippet here
// For example:
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[] { map.get(complement), i };
        }
        map.put(nums[i], i);
    }
    return new int[0];
}`;

export default function DetectPatternPage() {
  const [code, setCode] = useState(PLACEHOLDER);
  const [result, setResult] = useState<PatternDetection | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleDetect() {
    if (!code.trim()) {
      setError("Paste some code first.");
      return;
    }
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const r = await aiApi.detectPattern(code);
      setResult(r.data);
    } catch {
      setError("Detection failed. Try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-3xl mx-auto space-y-6">
        <div>
          <Link href="/dashboard" className="text-gray-500 text-sm hover:text-white">← Dashboard</Link>
          <div className="flex items-center gap-3 mt-2">
            <Wand2 size={24} className="text-purple-400" />
            <h1 className="text-2xl font-bold">Pattern Detector</h1>
          </div>
          <p className="text-gray-400 mt-1 text-sm">
            Paste a Java code snippet and the AI will identify which DSA pattern it uses.
          </p>
        </div>

        {/* Editor */}
        <div className="rounded-xl overflow-hidden border border-gray-800" style={{ height: 320 }}>
          <MonacoEditor
            height="320px"
            language="java"
            theme="vs-dark"
            value={code}
            onChange={(v) => setCode(v ?? "")}
            options={{
              fontSize: 13,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              tabSize: 4,
              fontFamily: "JetBrains Mono, Fira Code, monospace",
              lineNumbers: "on",
              automaticLayout: true,
            }}
          />
        </div>

        {error && (
          <div className="flex items-center gap-2 text-sm text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-3">
            <AlertCircle size={15} />
            {error}
          </div>
        )}

        <button
          onClick={handleDetect}
          disabled={loading}
          className="w-full py-3 rounded-xl bg-purple-600 hover:bg-purple-500 disabled:opacity-50 disabled:cursor-not-allowed font-semibold flex items-center justify-center gap-2 transition-colors"
        >
          {loading ? (
            <>
              <Sparkles size={18} className="animate-pulse" />
              Detecting pattern…
            </>
          ) : (
            <>
              <Wand2 size={18} />
              Detect Pattern
            </>
          )}
        </button>

        {result && (
          <div className="bg-gray-900 rounded-xl border border-gray-800 p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">{result.patternName}</h2>
              <span className={`text-xs font-medium px-3 py-1 rounded-full border ${CONFIDENCE_COLORS[result.confidence] ?? CONFIDENCE_COLORS.LOW}`}>
                {result.confidence} confidence
              </span>
            </div>

            <p className="text-gray-300 text-sm leading-relaxed">{result.explanation}</p>

            {result.patternSlug && (
              <Link
                href={`/patterns/${result.patternSlug}`}
                className="inline-flex items-center gap-2 text-sm text-brand-400 hover:text-brand-300 bg-brand-400/10 px-4 py-2 rounded-lg transition-colors"
              >
                View {result.patternName} lesson →
              </Link>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
