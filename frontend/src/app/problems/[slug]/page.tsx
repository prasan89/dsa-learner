"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import dynamic from "next/dynamic";
import { toast } from "react-hot-toast";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  ChevronRight, ChevronDown, RotateCcw, Maximize2,
  Play, Send, Bot, CheckCircle2, XCircle, AlertTriangle
} from "lucide-react";
import { problemsApi } from "@/lib/api/problems";
import { hintsApi } from "@/lib/api/hints";
import { aiApi } from "@/lib/api/ai";
import HintPanel from "@/components/HintPanel";
import AiReviewPanel from "@/components/AiReviewPanel";
import type { RunResult, Submission, Hint } from "@/types";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

const DEFAULT_CODE = `class Solution {
    // Write your solution here

}`;

type LeftTab = "description" | "hints" | "submissions" | "solution";
type Lang = "Java 17" | "Java 21";

const DIFF_CHIP: Record<string, string> = {
  EASY:   "text-green-700 bg-green-100",
  MEDIUM: "text-yellow-700 bg-yellow-100",
  HARD:   "text-red-700 bg-red-100",
};

function StatusIcon({ status }: { status: string }) {
  if (status === "ACCEPTED")          return <CheckCircle2 size={16} className="text-green-500" />;
  if (status === "WRONG_ANSWER")      return <XCircle size={16} className="text-red-500" />;
  if (status === "COMPILATION_ERROR") return <AlertTriangle size={16} className="text-red-500" />;
  return <AlertTriangle size={16} className="text-yellow-500" />;
}

export default function ProblemPage({ params }: { params: { slug: string } }) {
  const [problem, setProblem]         = useState<any>(null);
  const [code, setCode]               = useState(DEFAULT_CODE);
  const [leftTab, setLeftTab]         = useState<LeftTab>("description");
  const [runResult, setRunResult]     = useState<RunResult | null>(null);
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [running, setRunning]         = useState(false);
  const [submitting, setSubmitting]   = useState(false);
  const [loading, setLoading]         = useState(true);
  const [hints, setHints]             = useState<Hint[]>([]);
  const [credits, setCredits]         = useState<number | null>(null);
  const [lang] = useState<Lang>("Java 17");
  const [resultOpen, setResultOpen]   = useState(false);

  useEffect(() => {
    problemsApi.get(params.slug)
      .then((r) => setProblem(r.data))
      .finally(() => setLoading(false));
    hintsApi.list(params.slug).then((r) => setHints(r.data)).catch(() => {});
    aiApi.wallet().then((r) => setCredits(r.data.totalCredits)).catch(() => {});
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
    setResultOpen(true);
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
      if (r.data.status === "ACCEPTED") {
        toast.success("Accepted!");
        setLeftTab("submissions");
      } else {
        toast.error(r.data.status.replace(/_/g, " "));
      }
      await loadSubmissions();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? "Submit failed");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return (
    <div className="h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 text-sm">Loading…</p>
    </div>
  );

  const diff = problem?.difficulty ?? "EASY";

  const LEFT_TABS: { id: LeftTab; label: string }[] = [
    { id: "description", label: "Description" },
    { id: "hints",       label: `Hints${hints.length > 0 ? ` (${hints.filter(h => h.unlocked).length}/${hints.length})` : ""}` },
    { id: "submissions", label: "Submissions" },
    { id: "solution",    label: "Solution" },
  ];

  return (
    <div className="flex flex-col bg-gray-100 h-screen overflow-hidden">

      {/* Top bar */}
      <header className="bg-white border-b border-gray-200 px-4 py-2.5 flex items-center gap-3 shrink-0 z-20">
        {/* Breadcrumb */}
        <div className="flex items-center gap-1.5 text-xs text-gray-400">
          <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
          <ChevronRight size={12} />
          {problem?.patterns?.[0] && (
            <>
              <Link href={`/patterns/${problem.patterns[0].slug}`} className="hover:text-gray-600">
                {problem.patterns[0].name}
              </Link>
              <ChevronRight size={12} />
            </>
          )}
          <span className="text-gray-700 font-medium truncate max-w-48">
            {problem?.title ?? params.slug}
          </span>
        </div>

        <div className="ml-auto flex items-center gap-2">
          {/* Difficulty badge */}
          <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${DIFF_CHIP[diff] ?? DIFF_CHIP.EASY}`}>
            {diff[0] + diff.slice(1).toLowerCase()}
          </span>
        </div>
      </header>

      {/* Split layout */}
      <div className="flex flex-1 gap-2 p-2 overflow-hidden">

        {/* ── Left panel ── */}
        <div className="w-[46%] flex flex-col bg-white rounded-xl border border-gray-200 shadow-sm min-w-0 overflow-hidden">
          {/* Tab bar */}
          <div className="flex border-b border-gray-100 shrink-0 px-1">
            {LEFT_TABS.map((t) => (
              <button key={t.id}
                onClick={() => { setLeftTab(t.id); if (t.id === "submissions") loadSubmissions(); }}
                className={`px-3 py-2.5 text-xs font-medium whitespace-nowrap transition-colors border-b-2 -mb-px ${
                  leftTab === t.id
                    ? "text-brand-600 border-brand-600"
                    : "text-gray-400 hover:text-gray-600 border-transparent"
                }`}>
                {t.label}
              </button>
            ))}
          </div>

          <div className="flex-1 overflow-y-auto">

            {/* Description */}
            {leftTab === "description" && problem && (
              <div className="p-5 space-y-5">
                <h1 className="text-lg font-bold text-gray-900">{problem.title}</h1>

                <div className="prose prose-sm max-w-none
                  prose-p:text-gray-700 prose-p:leading-relaxed
                  prose-code:text-green-700 prose-code:bg-green-50 prose-code:px-1 prose-code:rounded prose-code:text-xs
                  prose-strong:text-gray-900 prose-li:text-gray-700">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {problem.description}
                  </ReactMarkdown>
                </div>

                {/* Examples */}
                {problem.examples && (() => {
                  try {
                    const exs = JSON.parse(problem.examples);
                    return (
                      <div className="space-y-3">
                        {exs.map((ex: any, i: number) => (
                          <div key={i} className="bg-gray-50 rounded-xl p-4 text-sm border border-gray-100">
                            <p className="text-xs font-semibold text-gray-500 mb-2">Example {i + 1}</p>
                            <div className="font-mono space-y-1 text-xs">
                              <p><span className="text-gray-500">Input:</span> <span className="text-gray-800">{ex.input}</span></p>
                              <p><span className="text-gray-500">Output:</span> <span className="text-gray-800">{ex.output}</span></p>
                              {ex.explanation && <p className="text-gray-400 font-sans mt-1">{ex.explanation}</p>}
                            </div>
                          </div>
                        ))}
                      </div>
                    );
                  } catch { return null; }
                })()}

                {/* Constraints */}
                {problem.constraints && (
                  <div>
                    <p className="text-xs font-semibold text-gray-500 mb-2">Constraints:</p>
                    <ul className="space-y-1">
                      {problem.constraints.split("\n").filter(Boolean).map((c: string, i: number) => (
                        <li key={i} className="text-xs font-mono text-gray-600 flex items-start gap-1">
                          <span className="text-gray-400 mt-0.5">•</span> {c}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Tags */}
                {problem.tags?.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 pt-2 border-t border-gray-100">
                    {problem.tags.map((tag: string) => (
                      <span key={tag} className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full">
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Hints */}
            {leftTab === "hints" && (
              <HintPanel problemSlug={params.slug} hints={hints} onHintsUpdate={setHints} />
            )}

            {/* Submissions */}
            {leftTab === "submissions" && (
              <div className="p-4 space-y-2">
                {submissions.length === 0 ? (
                  <p className="text-gray-400 text-sm text-center py-8">No submissions yet.</p>
                ) : submissions.map((s) => (
                  <div key={s.id} className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3 text-sm">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <StatusIcon status={s.status} />
                        <span className={`font-semibold ${
                          s.status === "ACCEPTED" ? "text-green-600" :
                          s.status === "WRONG_ANSWER" ? "text-red-600" : "text-yellow-600"
                        }`}>{s.status.replace(/_/g, " ")}</span>
                      </div>
                      <span className="text-gray-400 text-xs">
                        {new Date(s.submittedAt).toLocaleDateString()}
                      </span>
                    </div>
                    {(s as any).runtimeMs && (
                      <p className="text-gray-400 text-xs mt-1">Runtime: {(s as any).runtimeMs}ms</p>
                    )}
                  </div>
                ))}
              </div>
            )}

            {/* Solution / AI Review */}
            {leftTab === "solution" && (
              <AiReviewPanel
                problemSlug={params.slug}
                code={code}
                remainingReviews={credits ?? 0}
                onReviewComplete={setCredits}
              />
            )}
          </div>
        </div>

        {/* ── Right panel (editor) ── */}
        <div className="flex-1 flex flex-col min-w-0 gap-2 overflow-hidden">

          {/* Editor card */}
          <div className="flex-1 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col overflow-hidden">
            {/* Editor toolbar */}
            <div className="flex items-center justify-between px-4 py-2 border-b border-gray-100 shrink-0">
              <div className="flex items-center gap-2">
                <span className="text-xs font-medium text-gray-600 bg-gray-100 px-2.5 py-1 rounded-md">
                  {lang}
                </span>
              </div>
              <div className="flex items-center gap-1">
                <button className="p-1.5 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
                  title="Reset code"
                  onClick={() => setCode(DEFAULT_CODE)}>
                  <RotateCcw size={13} />
                </button>
                <button className="p-1.5 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
                  title="Fullscreen">
                  <Maximize2 size={13} />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-hidden">
              <MonacoEditor
                height="100%"
                language="java"
                theme="light"
                value={code}
                onChange={(v) => setCode(v ?? "")}
                options={{
                  fontSize: 13,
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  tabSize: 4,
                  insertSpaces: true,
                  fontFamily: "JetBrains Mono, Fira Code, monospace",
                  lineNumbers: "on",
                  renderLineHighlight: "line",
                  automaticLayout: true,
                  padding: { top: 12, bottom: 12 },
                  scrollbar: { verticalScrollbarSize: 6 },
                }}
              />
            </div>

            {/* Action bar */}
            <div className="flex items-center gap-2 px-4 py-2.5 border-t border-gray-100 bg-gray-50/50 shrink-0">
              <button onClick={handleRun} disabled={running}
                className="flex items-center gap-1.5 btn-secondary disabled:opacity-50">
                <Play size={13} />
                {running ? "Running…" : "Run"}
              </button>
              <button onClick={handleSubmit} disabled={submitting}
                className="flex items-center gap-1.5 btn-primary disabled:opacity-50">
                <Send size={13} />
                {submitting ? "Submitting…" : "Submit"}
              </button>
              <button onClick={() => setLeftTab("solution")}
                className="flex items-center gap-1.5 ml-auto btn-ghost text-brand-600 hover:text-brand-700 hover:bg-brand-50">
                <Bot size={14} />
                AI Review
              </button>
            </div>
          </div>

          {/* Test result panel */}
          {resultOpen && (
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm shrink-0 max-h-52 overflow-hidden flex flex-col">
              <div className="flex items-center justify-between px-4 py-2.5 border-b border-gray-100 shrink-0">
                <div className="flex items-center gap-2">
                  {runResult && <StatusIcon status={runResult.status} />}
                  <span className={`text-sm font-semibold ${
                    !runResult ? "text-gray-400" :
                    runResult.status === "ACCEPTED" ? "text-green-600" :
                    runResult.status === "COMPILATION_ERROR" ? "text-red-600" : "text-yellow-600"
                  }`}>
                    {running ? "Running…" : runResult ? runResult.status.replace(/_/g, " ") : "Test Results"}
                  </span>
                  {runResult && (runResult as any).runtimeMs && (
                    <span className="text-xs text-gray-400">{(runResult as any).runtimeMs}ms</span>
                  )}
                </div>
                <button onClick={() => setResultOpen(false)}
                  className="p-1 rounded hover:bg-gray-100 text-gray-400">
                  <ChevronDown size={14} />
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-3 space-y-1.5 text-xs font-mono">
                {runResult?.errorMessage && (
                  <pre className="text-red-600 whitespace-pre-wrap bg-red-50 rounded-lg p-3">
                    {runResult.errorMessage}
                  </pre>
                )}
                {runResult?.testResults?.map((tr, i) => (
                  <div key={i} className={`flex items-center gap-2 px-3 py-2 rounded-lg ${
                    tr.passed ? "bg-green-50 text-green-700" : "bg-red-50 text-red-700"
                  }`}>
                    {tr.passed
                      ? <CheckCircle2 size={13} />
                      : <XCircle size={13} />
                    }
                    <span>Test {i + 1}</span>
                    {!tr.passed && (
                      <span className="text-gray-500 font-sans ml-1">
                        Expected <code className="bg-gray-100 px-1 rounded">{tr.expectedOutput}</code>
                        {" · "}Got <code className="bg-gray-100 px-1 rounded">{tr.actualOutput}</code>
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
