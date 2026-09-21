"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import dynamic from "next/dynamic";
import { toast } from "react-hot-toast";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { problemsApi } from "@/lib/api/problems";
import { hintsApi } from "@/lib/api/hints";
import { aiApi } from "@/lib/api/ai";
import { difficultyBadge } from "@/lib/utils";
import HintPanel from "@/components/HintPanel";
import AiReviewPanel from "@/components/AiReviewPanel";
import type { RunResult, Submission, Hint } from "@/types";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

const DEFAULT_CODE = `import java.util.*;
import java.io.*;

public class Solution {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        // Read input and write your solution here
        // Print the result using System.out.println()
    }
}`;

type Tab = "description" | "hints" | "ai-review" | "submissions";

export default function ProblemPage({ params }: { params: { slug: string } }) {
  const [problem, setProblem] = useState<any>(null);
  const [code, setCode] = useState(DEFAULT_CODE);
  const [activeTab, setActiveTab] = useState<Tab>("description");
  const [runResult, setRunResult] = useState<RunResult | null>(null);
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [hints, setHints] = useState<Hint[]>([]);
  const [remainingReviews, setRemainingReviews] = useState(5);

  useEffect(() => {
    problemsApi.get(params.slug)
      .then((r) => setProblem(r.data))
      .finally(() => setLoading(false));

    hintsApi.list(params.slug)
      .then((r) => setHints(r.data))
      .catch(() => {});

    aiApi.remaining()
      .then((r) => setRemainingReviews(r.data.remaining))
      .catch(() => {});
  }, [params.slug]);

  const loadSubmissions = async () => {
    try {
      const r = await problemsApi.submissions(params.slug);
      setSubmissions(r.data);
    } catch {}
  };

  const handleRun = async () => {
    setRunning(true);
    setRunResult(null);
    try {
      const r = await problemsApi.run(params.slug, code);
      setRunResult(r.data);
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? "Run failed");
    } finally {
      setRunning(false);
    }
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      const r = await problemsApi.submit(params.slug, code);
      const status = r.data.status;
      if (status === "ACCEPTED") {
        toast.success("Accepted! Great job.");
        setActiveTab("ai-review");
      } else {
        toast.error(`${status.replace(/_/g, " ")}`);
      }
      await loadSubmissions();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? "Submission failed");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="h-screen bg-gray-950 flex items-center justify-center text-gray-400">
        Loading...
      </div>
    );
  }

  const TABS: { id: Tab; label: string }[] = [
    { id: "description", label: "Description" },
    { id: "hints", label: `Hints${hints.length > 0 ? ` (${hints.filter(h => h.unlocked).length}/${hints.length})` : ""}` },
    { id: "ai-review", label: "AI Review" },
    { id: "submissions", label: "Submissions" },
  ];

  return (
    <div className="flex flex-col bg-gray-950 text-white h-full overflow-hidden">
      {/* Top bar */}
      <header className="flex items-center gap-3 px-4 py-2 bg-gray-900 border-b border-gray-800 shrink-0">
        <Link href="/problems" className="text-gray-400 hover:text-white text-sm transition-colors">
          ← Problems
        </Link>
        <span className="text-gray-700">|</span>
        <span className="font-semibold text-sm">{problem?.title ?? params.slug}</span>
        {problem?.difficulty && (
          <span className={difficultyBadge(problem.difficulty)}>{problem.difficulty}</span>
        )}
        {problem?.patterns?.map((p: any) => (
          <Link key={p.id} href={`/patterns/${p.slug}`}
            className="text-xs text-brand-400 bg-brand-400/10 px-2 py-0.5 rounded-full hover:bg-brand-400/20 transition-colors">
            {p.name}
          </Link>
        ))}
      </header>

      {/* Split layout */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left panel */}
        <div className="w-[46%] flex flex-col border-r border-gray-800 min-w-0">
          <div className="flex border-b border-gray-800 shrink-0 overflow-x-auto">
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => { setActiveTab(tab.id); if (tab.id === "submissions") loadSubmissions(); }}
                className={`px-3 py-2 text-xs whitespace-nowrap capitalize transition-colors ${
                  activeTab === tab.id
                    ? "text-white border-b-2 border-brand-500"
                    : "text-gray-400 hover:text-white"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="flex-1 overflow-y-auto">
            {activeTab === "description" && problem && (
              <div className="p-4 space-y-4">
                <ReactMarkdown remarkPlugins={[remarkGfm]}
                  className="prose prose-invert prose-sm max-w-none">
                  {problem.description}
                </ReactMarkdown>

                {problem.constraints && (
                  <div>
                    <h3 className="text-sm font-semibold text-gray-300 mb-1">Constraints</h3>
                    <pre className="text-xs text-gray-400 whitespace-pre-wrap bg-gray-900 rounded p-3">
                      {problem.constraints}
                    </pre>
                  </div>
                )}

                {problem.examples && (() => {
                  try {
                    const examples = JSON.parse(problem.examples);
                    return (
                      <div className="space-y-3">
                        {examples.map((ex: any, i: number) => (
                          <div key={i} className="bg-gray-900 rounded-lg p-3 text-sm">
                            <p className="text-gray-400 font-medium mb-1">Example {i + 1}</p>
                            <p><span className="text-gray-500">Input:</span> <code className="text-gray-200">{ex.input}</code></p>
                            <p><span className="text-gray-500">Output:</span> <code className="text-gray-200">{ex.output}</code></p>
                            {ex.explanation && <p className="text-gray-400 text-xs mt-1">{ex.explanation}</p>}
                          </div>
                        ))}
                      </div>
                    );
                  } catch { return null; }
                })()}
              </div>
            )}

            {activeTab === "hints" && (
              <HintPanel
                problemSlug={params.slug}
                hints={hints}
                onHintsUpdate={setHints}
              />
            )}

            {activeTab === "ai-review" && (
              <AiReviewPanel
                problemSlug={params.slug}
                code={code}
                remainingReviews={remainingReviews}
                onReviewComplete={setRemainingReviews}
              />
            )}

            {activeTab === "submissions" && (
              <div className="p-4 space-y-3">
                {submissions.length === 0 ? (
                  <p className="text-gray-500 text-sm">No submissions yet.</p>
                ) : (
                  submissions.map((s) => (
                    <div key={s.id} className="bg-gray-900 rounded-lg p-3 text-sm">
                      <div className="flex items-center justify-between">
                        <span className={`font-semibold ${
                          s.status === "ACCEPTED" ? "text-green-400" :
                          s.status === "WRONG_ANSWER" ? "text-red-400" : "text-yellow-400"
                        }`}>
                          {s.status.replace(/_/g, " ")}
                        </span>
                        <span className="text-gray-500 text-xs">
                          {new Date(s.submittedAt).toLocaleDateString()}
                        </span>
                      </div>
                      {(s as any).runtimeMs && <p className="text-gray-500 text-xs mt-1">Runtime: {(s as any).runtimeMs}ms</p>}
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right panel */}
        <div className="flex-1 flex flex-col min-w-0">
          <div className="flex-1 overflow-hidden">
            <MonacoEditor
              height="100%"
              language="java"
              theme="vs-dark"
              value={code}
              onChange={(v) => setCode(v ?? "")}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                tabSize: 4,
                insertSpaces: true,
                fontFamily: "JetBrains Mono, Fira Code, monospace",
                lineNumbers: "on",
                renderLineHighlight: "line",
                automaticLayout: true,
              }}
            />
          </div>

          {runResult && (
            <div className="border-t border-gray-800 bg-gray-900 p-3 max-h-48 overflow-y-auto shrink-0">
              <div className="flex items-center gap-2 mb-2">
                <span className={`text-sm font-semibold ${
                  runResult.status === "ACCEPTED" ? "text-green-400" :
                  runResult.status === "COMPILATION_ERROR" ? "text-red-400" : "text-yellow-400"
                }`}>
                  {runResult.status?.replace(/_/g, " ")}
                </span>
                {(runResult as any).runtimeMs && <span className="text-gray-500 text-xs">{(runResult as any).runtimeMs}ms</span>}
              </div>

              {runResult.errorMessage && (
                <pre className="text-red-400 text-xs whitespace-pre-wrap mb-2">{runResult.errorMessage}</pre>
              )}

              {runResult.testResults?.map((tr, i) => (
                <div key={i} className={`text-xs mb-1 p-2 rounded ${tr.passed ? "bg-green-900/20" : "bg-red-900/20"}`}>
                  <span className={tr.passed ? "text-green-400" : "text-red-400"}>
                    {tr.passed ? "✓" : "✗"} Test {i + 1}
                  </span>
                  {!tr.passed && (
                    <span className="text-gray-400 ml-2">
                      Expected: <code>{tr.expectedOutput}</code>  Got: <code>{tr.actualOutput}</code>
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}

          <div className="flex items-center gap-2 px-4 py-2 bg-gray-900 border-t border-gray-800 shrink-0">
            <span className="text-xs text-gray-500 mr-auto">Java 21</span>
            <button
              onClick={handleRun}
              disabled={running}
              className="px-4 py-1.5 text-sm border border-gray-600 hover:border-gray-400 rounded text-gray-300 hover:text-white disabled:opacity-50 transition-colors"
            >
              {running ? "Running..." : "Run"}
            </button>
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="px-4 py-1.5 text-sm bg-brand-600 hover:bg-brand-700 rounded text-white font-medium disabled:opacity-50 transition-colors"
            >
              {submitting ? "Submitting..." : "Submit"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
