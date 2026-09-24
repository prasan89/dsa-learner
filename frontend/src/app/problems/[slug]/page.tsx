"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import dynamic from "next/dynamic";
import { toast } from "react-hot-toast";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  ChevronRight, ChevronDown, RotateCcw, Maximize2,
  Play, Send, Bot, CheckCircle2, XCircle, AlertTriangle, Lock
} from "lucide-react";
import { problemsApi } from "@/lib/api/problems";
import { hintsApi } from "@/lib/api/hints";
import { aiApi } from "@/lib/api/ai";
import HintPanel from "@/components/HintPanel";
import AiReviewPanel from "@/components/AiReviewPanel";
import ArrayVisualizer from "@/components/visualizer/ArrayVisualizer";
import { ProblemMarkdown, ProblemExamples, ProblemConstraints } from "@/components/ProblemDescription";
import type { RunResult, Submission, Hint } from "@/types";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

const DEFAULT_CODE = `class Solution {
    // Write your solution here

}`;

type LeftTab = "description" | "learn" | "visualize" | "hints" | "submissions" | "solution";
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
  const [locked, setLocked]           = useState(false);
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
      .catch((err: any) => {
        if (err.response?.status === 402) setLocked(true);
      })
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

  if (locked) return (
    <div className="h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full text-center bg-white rounded-2xl shadow-sm border border-gray-200 p-10">
        <div className="w-14 h-14 rounded-full bg-amber-100 flex items-center justify-center mx-auto mb-5">
          <Lock size={26} className="text-amber-600" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 mb-2">Pro Problem</h2>
        <p className="text-gray-500 text-sm mb-6">
          This problem is part of the Pro plan. Upgrade to unlock all 250+ problems, gold-standard
          hints, and senior follow-ups.
        </p>
        <Link href="/pricing"
          className="inline-block w-full py-3 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-semibold text-sm transition-colors">
          Upgrade to Pro — ₹999/month
        </Link>
        <Link href="/problems" className="block mt-4 text-xs text-gray-400 hover:text-gray-600">
          ← Back to problems
        </Link>
      </div>
    </div>
  );

  const diff = problem?.difficulty ?? "EASY";

  // Check if this problem belongs to the arrays pattern
  const isArraysProblem = problem?.patterns?.some(
    (p: { slug: string }) => p.slug === "arrays" || p.slug === "binary-search" ||
      p.slug === "two-pointers" || p.slug === "sliding-window"
  ) ?? false;

  const LEFT_TABS: { id: LeftTab; label: string }[] = [
    { id: "description", label: "Description" },
    { id: "learn",       label: "Learn" },
    ...(isArraysProblem ? [{ id: "visualize" as LeftTab, label: "Visualize" }] : []),
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

                {/* Description body — markdown with normalization */}
                <div className="text-sm">
                  <ProblemMarkdown text={problem.description} />
                </div>

                {/* Examples — first-class structured blocks */}
                {problem.examples && (
                  <ProblemExamples examples={problem.examples} />
                )}

                {/* Constraints — bullet list with inline code */}
                {problem.constraints && (
                  <ProblemConstraints constraints={problem.constraints} />
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

            {/* Learn */}
            {leftTab === "learn" && (
              <div className="p-5 space-y-7">
                {!problem?.content ? (
                  <div className="text-center py-12 text-gray-400">
                    <p className="text-sm">Learning content coming soon for this problem.</p>
                  </div>
                ) : (<>

                  {/* ── Pattern Recognition ── */}
                  {(problem.content.recognitionNote || problem.content.patternRecognitionClues
                    || problem.content.whenToUse || problem.content.whenNotToUse) && (
                    <section className="bg-brand-50 border border-brand-100 rounded-2xl p-5 space-y-4">
                      <h3 className="text-sm font-bold text-brand-800 uppercase tracking-wide">Pattern Recognition</h3>

                      {problem.content.recognitionNote && (
                        <p className="text-sm text-brand-800">{problem.content.recognitionNote}</p>
                      )}

                      {problem.content.patternRecognitionClues && (
                        <div>
                          <p className="text-xs font-semibold text-brand-700 mb-1.5">Recognition Clues</p>
                          <div className="prose prose-sm max-w-none prose-p:text-brand-800 prose-li:text-brand-800 prose-code:text-brand-700 prose-code:bg-brand-100 prose-code:px-1 prose-code:rounded">
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.patternRecognitionClues}</ReactMarkdown>
                          </div>
                        </div>
                      )}

                      <div className="grid grid-cols-2 gap-3">
                        {problem.content.whenToUse && (
                          <div className="bg-green-50 border border-green-100 rounded-xl p-3">
                            <p className="text-xs font-semibold text-green-700 mb-1">✓ When to use</p>
                            <div className="prose prose-sm max-w-none prose-p:text-green-800 prose-li:text-green-800 prose-p:leading-relaxed">
                              <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.whenToUse}</ReactMarkdown>
                            </div>
                          </div>
                        )}
                        {problem.content.whenNotToUse && (
                          <div className="bg-red-50 border border-red-100 rounded-xl p-3">
                            <p className="text-xs font-semibold text-red-700 mb-1">✗ When NOT to use</p>
                            <div className="prose prose-sm max-w-none prose-p:text-red-800 prose-li:text-red-800 prose-p:leading-relaxed">
                              <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.whenNotToUse}</ReactMarkdown>
                            </div>
                          </div>
                        )}
                      </div>
                    </section>
                  )}

                  {/* ── Level 1: Intuition ── */}
                  {problem.content.intuition && (
                    <section>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-xs font-bold text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">Level 1</span>
                        <h3 className="text-sm font-semibold text-gray-800">Intuition</h3>
                      </div>
                      <div className="prose prose-sm max-w-none prose-p:text-gray-700 prose-p:leading-relaxed prose-strong:text-gray-900">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.intuition}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Level 2: Guided Reasoning ── */}
                  {problem.content.guidedReasoning && (
                    <section>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-xs font-bold text-yellow-700 bg-yellow-50 px-2 py-0.5 rounded-full">Level 2</span>
                        <h3 className="text-sm font-semibold text-gray-800">Guided Reasoning</h3>
                      </div>
                      <div className="prose prose-sm max-w-none prose-p:text-gray-700 prose-p:leading-relaxed prose-strong:text-gray-900 prose-blockquote:border-brand-400 prose-blockquote:text-gray-600">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.guidedReasoning}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Brute Force ── */}
                  {problem.content.bruteForce && (
                    <section>
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-sm font-semibold text-gray-800">Brute Force</h3>
                        <div className="flex gap-2 text-xs">
                          {problem.content.bruteTime && (
                            <span className="bg-orange-50 text-orange-700 px-2 py-0.5 rounded-full font-mono">
                              Time: {problem.content.bruteTime}
                            </span>
                          )}
                          {problem.content.bruteSpace && (
                            <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full font-mono">
                              Space: {problem.content.bruteSpace}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="prose prose-sm max-w-none prose-p:text-gray-700 prose-p:leading-relaxed">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.bruteForce}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Optimal Approach ── */}
                  {problem.content.optimalApproach && (
                    <section>
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-sm font-semibold text-gray-800">Optimal Approach</h3>
                        <div className="flex gap-2 text-xs">
                          {problem.content.optimalTime && (
                            <span className="bg-green-50 text-green-700 px-2 py-0.5 rounded-full font-mono">
                              Time: {problem.content.optimalTime}
                            </span>
                          )}
                          {problem.content.optimalSpace && (
                            <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full font-mono">
                              Space: {problem.content.optimalSpace}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="prose prose-sm max-w-none prose-p:text-gray-700 prose-p:leading-relaxed">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.optimalApproach}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Why This Works + Invariant ── */}
                  {(problem.content.whyThisWorks || problem.content.invariant) && (
                    <section className="bg-indigo-50 border border-indigo-100 rounded-2xl p-5 space-y-3">
                      <h3 className="text-sm font-bold text-indigo-800">Why This Works</h3>
                      {problem.content.whyThisWorks && (
                        <div className="prose prose-sm max-w-none prose-p:text-indigo-900 prose-p:leading-relaxed">
                          <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.whyThisWorks}</ReactMarkdown>
                        </div>
                      )}
                      {problem.content.invariant && (
                        <div className="bg-indigo-100 rounded-xl px-4 py-3">
                          <p className="text-xs font-semibold text-indigo-600 mb-1">Invariant</p>
                          <p className="text-sm text-indigo-900 font-mono">{problem.content.invariant}</p>
                        </div>
                      )}
                    </section>
                  )}

                  {/* ── Level 3: Solution ── */}
                  {problem.content.solution && (
                    <section>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-xs font-bold text-green-700 bg-green-50 px-2 py-0.5 rounded-full">Level 3</span>
                        <h3 className="text-sm font-semibold text-gray-800">Solution</h3>
                      </div>
                      <pre className="bg-gray-900 text-green-300 rounded-xl p-4 text-xs overflow-x-auto whitespace-pre-wrap font-mono leading-relaxed">
                        {problem.content.solution}
                      </pre>
                    </section>
                  )}

                  {/* ── Pseudocode ── */}
                  {problem.content.pseudocode && (
                    <section>
                      <h3 className="text-sm font-semibold text-gray-800 mb-2">Pseudocode</h3>
                      <pre className="bg-gray-50 border border-gray-100 rounded-xl p-4 text-xs text-gray-700 overflow-x-auto whitespace-pre-wrap font-mono leading-relaxed">
                        {problem.content.pseudocode}
                      </pre>
                    </section>
                  )}

                  {/* ── Common Mistakes ── */}
                  {problem.content.commonMistakes && (
                    <section>
                      <h3 className="text-sm font-semibold text-red-700 mb-2">Common Mistakes</h3>
                      <div className="bg-red-50 border border-red-100 rounded-xl p-4 prose prose-sm max-w-none prose-p:text-red-800 prose-li:text-red-800 prose-p:leading-relaxed">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.commonMistakes}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Senior Variations ── */}
                  {problem.content.seniorVariations && (
                    <section>
                      <h3 className="text-sm font-semibold text-purple-800 mb-2">Senior Variations</h3>
                      <div className="bg-purple-50 border border-purple-100 rounded-xl p-4 prose prose-sm max-w-none prose-p:text-purple-900 prose-li:text-purple-900 prose-p:leading-relaxed">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{problem.content.seniorVariations}</ReactMarkdown>
                      </div>
                    </section>
                  )}

                  {/* ── Follow-ups ── */}
                  {problem.followups?.length > 0 && (
                    <section>
                      <h3 className="text-sm font-semibold text-gray-800 mb-3">Interview Follow-ups</h3>
                      <div className="space-y-2">
                        {problem.followups.map((f: { question: string; type: string }, i: number) => (
                          <div key={i} className={`rounded-xl border px-4 py-3 text-sm ${
                            f.type === "SENIOR"
                              ? "border-purple-100 bg-purple-50 text-purple-800"
                              : f.type === "VARIATION"
                              ? "border-yellow-100 bg-yellow-50 text-yellow-800"
                              : "border-gray-100 bg-gray-50 text-gray-700"
                          }`}>
                            <span className={`text-xs font-semibold mr-2 ${
                              f.type === "SENIOR" ? "text-purple-500"
                              : f.type === "VARIATION" ? "text-yellow-600"
                              : "text-gray-400"
                            }`}>
                              {f.type === "SENIOR" ? "Senior" : f.type === "VARIATION" ? "Variation" : "Follow-up"}
                            </span>
                            {f.question}
                          </div>
                        ))}
                      </div>
                    </section>
                  )}
                </>)}
              </div>
            )}

            {/* Visualize */}
            {leftTab === "visualize" && (
              <div className="p-4">
                <ArrayVisualizer
                  algorithmSlug={params.slug}
                  defaultInput={(() => {
                    try {
                      const exs = JSON.parse(problem?.examples ?? "[]");
                      const first = exs[0]?.input ?? "";
                      const match = first.match(/\[([^\]]+)\]/);
                      if (match) return match[1].split(",").map((s: string) => parseInt(s.trim(), 10)).filter((n: number) => !isNaN(n));
                    } catch {}
                    return [10, 20, 30, 40, 50];
                  })()}
                />
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
