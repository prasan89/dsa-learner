"use client";

import { useState, useEffect, useCallback } from "react";
import dynamic from "next/dynamic";
import {
  Play, CheckCircle2, XCircle, AlertTriangle, Loader2,
  ChevronRight, Star, RotateCcw, MessageSquare,
} from "lucide-react";
import { problemsApi } from "@/lib/api/problems";
import { aiApi, type MentorContext } from "@/lib/api/ai";
import AiMentorPanel from "./AiMentorPanel";
import type { Problem, RunResult, Submission } from "@/types";
import type { ArrayConcept } from "@/lib/visualizer/tracers/arrayConceptTracers";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), { ssr: false });

// Map concept ID → problem slug (V32 original slugs)
const CONCEPT_PROBLEM_MAP: Record<string, string> = {
  "linear-search":     "repeated-sensor-reading",
  "remove-duplicates": "log-deduplicator",
};

const DEFAULT_JAVA = `class Solution {
    public int[] solve(int[] nums) {
        // Your solution here
        return new int[]{};
    }
}`;

type Phase = "practice" | "mastery-reflection" | "mastered";

interface ConceptPracticePanelProps {
  concept: ArrayConcept;
  onMastered?: () => void;
  onClose?: () => void;
}

export default function ConceptPracticePanel({ concept, onMastered, onClose }: ConceptPracticePanelProps) {
  const slug = CONCEPT_PROBLEM_MAP[concept.id];
  const [problem, setProblem] = useState<Problem | null>(null);
  const [loadingProblem, setLoadingProblem] = useState(true);
  const [code, setCode] = useState(DEFAULT_JAVA);
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [runResult, setRunResult] = useState<RunResult | null>(null);
  const [submission, setSubmission] = useState<Submission | null>(null);
  const [attempts, setAttempts] = useState(0);
  const [hintsUsed, setHintsUsed] = useState(0);
  const [phase, setPhase] = useState<Phase>("practice");
  const [reflectionAnswer, setReflectionAnswer] = useState("");
  const [evalLoading, setEvalLoading] = useState(false);
  const [evalFeedback, setEvalFeedback] = useState("");
  const [showMentor, setShowMentor] = useState(false);

  useEffect(() => {
    if (!slug) { setLoadingProblem(false); return; }
    problemsApi.get(slug)
      .then((r) => {
        setProblem(r.data);
        setCode(DEFAULT_JAVA);
      })
      .catch(() => setProblem(null))
      .finally(() => setLoadingProblem(false));
  }, [slug]);

  const mentorCtx: MentorContext = {
    conceptId: concept.id,
    conceptTitle: concept.title,
    problemSlug: slug,
    currentCode: code,
    executionResult: runResult
      ? runResult.status === "ACCEPTED"
        ? "All tests passed"
        : `Status: ${runResult.status}. ${runResult.testResults.filter((t) => !t.passed).length} test(s) failed.`
      : undefined,
    compilerError: runResult?.status === "COMPILATION_ERROR"
      ? runResult.errorMessage
      : undefined,
    attemptCount: attempts,
    hintsUsed,
    masteryLevel: "PRACTICING",
  };

  const handleRun = useCallback(async () => {
    if (!slug || running) return;
    setRunning(true);
    setRunResult(null);
    try {
      const res = await problemsApi.run(slug, code);
      setRunResult(res.data);
      setAttempts((a) => a + 1);
    } catch {
      // ignore
    } finally {
      setRunning(false);
    }
  }, [slug, code, running]);

  const handleSubmit = useCallback(async () => {
    if (!slug || submitting) return;
    setSubmitting(true);
    setRunResult(null);
    setSubmission(null);
    try {
      const res = await problemsApi.submit(slug, code);
      setSubmission(res.data);
      setAttempts((a) => a + 1);
      if (res.data.status === "ACCEPTED") {
        setTimeout(() => setPhase("mastery-reflection"), 800);
      }
    } catch {
      // ignore
    } finally {
      setSubmitting(false);
    }
  }, [slug, code, submitting]);

  const handleReflectionSubmit = useCallback(async () => {
    if (!reflectionAnswer.trim() || evalLoading) return;
    setEvalLoading(true);
    try {
      const res = await aiApi.mentor(
        { ...mentorCtx, masteryLevel: "REFLECTION" },
        `Reflection answer: "${reflectionAnswer}". Please evaluate whether the student understands the core concept. Ask one follow-up question if needed, or affirm their understanding and congratulate them.`,
        []
      );
      setEvalFeedback(res.data.message);
      setTimeout(() => setPhase("mastered"), 2500);
    } catch {
      setEvalFeedback("Great work completing this concept! Keep practicing.");
      setTimeout(() => setPhase("mastered"), 2000);
    } finally {
      setEvalLoading(false);
    }
  }, [reflectionAnswer, evalLoading, mentorCtx]);

  if (!slug) {
    return (
      <div className="mt-6 p-6 rounded-2xl border border-gray-200 bg-gray-50 text-center">
        <p className="text-sm text-gray-500">No practice problem mapped for this concept yet.</p>
        {onClose && (
          <button onClick={onClose} className="mt-3 text-sm text-brand-600 hover:underline">
            Continue without practice
          </button>
        )}
      </div>
    );
  }

  if (loadingProblem) {
    return (
      <div className="mt-6 flex items-center justify-center h-32">
        <Loader2 size={20} className="animate-spin text-brand-400" />
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="mt-6 p-6 rounded-2xl border border-red-200 bg-red-50 text-center">
        <p className="text-sm text-red-600">Failed to load practice problem.</p>
        {onClose && (
          <button onClick={onClose} className="mt-3 text-sm text-red-500 hover:underline">
            Skip
          </button>
        )}
      </div>
    );
  }

  if (phase === "mastered") {
    return (
      <div className="mt-6 rounded-2xl border border-green-200 bg-gradient-to-br from-green-50 to-white p-6 space-y-4">
        <div className="flex items-start gap-3">
          <div className="w-9 h-9 rounded-full bg-green-500 flex items-center justify-center shrink-0">
            <Star size={18} className="text-white fill-white" />
          </div>
          <div>
            <p className="text-xs font-semibold text-green-700 uppercase tracking-widest">Concept mastered!</p>
            <h3 className="text-base font-bold text-gray-900 mt-0.5">{concept.title}</h3>
            {evalFeedback && (
              <p className="text-sm text-gray-600 mt-2 leading-relaxed">{evalFeedback}</p>
            )}
          </div>
        </div>
        <button
          onClick={onMastered}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-green-600 text-white text-sm font-semibold hover:bg-green-700 transition-colors"
        >
          Continue to next concept
          <ChevronRight size={14} />
        </button>
      </div>
    );
  }

  if (phase === "mastery-reflection") {
    const reflectionQuestion = `In your own words, why does **${concept.title}** work the way it does? Explain to a friend who doesn't know arrays.`;
    return (
      <div className="mt-6 rounded-2xl border border-brand-200 bg-brand-50 p-6 space-y-5">
        <div className="flex items-center gap-2">
          <CheckCircle2 size={20} className="text-green-500" />
          <span className="text-sm font-semibold text-gray-800">Problem solved! One final reflection...</span>
        </div>
        <div>
          <p className="text-sm font-medium text-gray-800 leading-relaxed">
            {reflectionQuestion.split("**").map((part, i) =>
              i % 2 === 1 ? <strong key={i}>{part}</strong> : part
            )}
          </p>
          <textarea
            value={reflectionAnswer}
            onChange={(e) => setReflectionAnswer(e.target.value)}
            placeholder="Type your explanation here..."
            rows={3}
            className="mt-3 w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-white text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-brand-400/30 resize-none"
          />
        </div>
        {evalFeedback && (
          <div className="px-4 py-3 rounded-xl bg-white border border-brand-200 text-sm text-gray-700 leading-relaxed">
            {evalFeedback}
          </div>
        )}
        <button
          onClick={handleReflectionSubmit}
          disabled={!reflectionAnswer.trim() || evalLoading}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-600 text-white text-sm font-semibold hover:bg-brand-700 transition-colors disabled:opacity-50"
        >
          {evalLoading ? <Loader2 size={14} className="animate-spin" /> : <MessageSquare size={14} />}
          Submit reflection
        </button>
      </div>
    );
  }

  // ── Practice phase ──────────────────────────────────────────────────────────
  const passedTests = runResult?.testResults.filter((t) => t.passed).length ?? 0;
  const totalTests = runResult?.testResults.length ?? 0;

  return (
    <div className="mt-6 space-y-4">
      {/* Problem header */}
      <div className="rounded-2xl border border-gray-200 bg-white p-5 space-y-3">
        <div className="flex items-start justify-between gap-2">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-gray-900">{problem.title}</h3>
              <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                problem.difficulty === "EASY"
                  ? "bg-green-100 text-green-700"
                  : problem.difficulty === "MEDIUM"
                  ? "bg-amber-100 text-amber-700"
                  : "bg-red-100 text-red-700"
              }`}>
                {problem.difficulty}
              </span>
            </div>
            <p className="text-xs text-gray-400 mt-0.5">Concept: {concept.title}</p>
          </div>
          <button
            onClick={() => setShowMentor((v) => !v)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
              showMentor
                ? "bg-brand-600 border-brand-600 text-white"
                : "bg-white border-gray-200 text-gray-500 hover:border-brand-300 hover:text-brand-600"
            }`}
          >
            <MessageSquare size={12} />
            AI Mentor
          </button>
        </div>
        <p className="text-sm text-gray-600 leading-relaxed">{problem.description}</p>
        {problem.examples.length > 0 && (
          <div className="space-y-2">
            {problem.examples.slice(0, 2).map((ex, i) => (
              <div key={i} className="rounded-lg bg-gray-50 border border-gray-100 px-3 py-2 text-xs font-mono">
                <span className="text-gray-400">Input: </span>
                <span className="text-gray-700">{ex.input}</span>
                <span className="text-gray-400 ml-3">Output: </span>
                <span className="text-gray-700">{ex.output}</span>
                {ex.explanation && (
                  <div className="mt-1 text-gray-400 font-sans">{ex.explanation}</div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* AI Mentor */}
      {showMentor && (
        <AiMentorPanel
          context={mentorCtx}
          onHintsUsed={setHintsUsed}
        />
      )}

      {/* Editor */}
      <div className="rounded-2xl border border-gray-200 overflow-hidden">
        <div className="bg-gray-900 px-4 py-2.5 flex items-center justify-between">
          <span className="text-xs text-gray-400 font-mono">Java</span>
          <button
            onClick={() => { setCode(DEFAULT_JAVA); setRunResult(null); setSubmission(null); }}
            className="flex items-center gap-1 text-xs text-gray-500 hover:text-gray-300 transition-colors"
          >
            <RotateCcw size={10} />
            Reset
          </button>
        </div>
        <MonacoEditor
          height="280px"
          language="java"
          theme="vs-dark"
          value={code}
          onChange={(v) => setCode(v ?? "")}
          options={{
            minimap: { enabled: false },
            fontSize: 13,
            lineNumbers: "on",
            scrollBeyondLastLine: false,
            wordWrap: "on",
            padding: { top: 12, bottom: 12 },
          }}
        />
      </div>

      {/* Controls */}
      <div className="flex items-center gap-2">
        <button
          onClick={handleRun}
          disabled={running || submitting}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-700 hover:border-gray-300 hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          {running ? <Loader2 size={14} className="animate-spin" /> : <Play size={14} />}
          Run
        </button>
        <button
          onClick={handleSubmit}
          disabled={running || submitting}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-brand-600 text-white text-sm font-semibold hover:bg-brand-700 transition-colors disabled:opacity-50"
        >
          {submitting ? <Loader2 size={14} className="animate-spin" /> : <CheckCircle2 size={14} />}
          Submit
        </button>
        <span className="text-xs text-gray-400 ml-auto">Attempt #{attempts + 1}</span>
      </div>

      {/* Run result */}
      {runResult && (
        <div className={`rounded-xl border px-4 py-3 space-y-2 ${
          runResult.status === "ACCEPTED"
            ? "border-green-200 bg-green-50"
            : runResult.status === "COMPILATION_ERROR"
            ? "border-red-200 bg-red-50"
            : "border-amber-200 bg-amber-50"
        }`}>
          <div className="flex items-center gap-2">
            {runResult.status === "ACCEPTED" ? (
              <CheckCircle2 size={14} className="text-green-600" />
            ) : runResult.status === "COMPILATION_ERROR" ? (
              <XCircle size={14} className="text-red-600" />
            ) : (
              <AlertTriangle size={14} className="text-amber-600" />
            )}
            <span className={`text-xs font-semibold ${
              runResult.status === "ACCEPTED" ? "text-green-700"
              : runResult.status === "COMPILATION_ERROR" ? "text-red-700"
              : "text-amber-700"
            }`}>
              {runResult.status === "ACCEPTED"
                ? `All ${totalTests} tests passed`
                : runResult.status === "COMPILATION_ERROR"
                ? "Compilation error"
                : `${passedTests}/${totalTests} tests passed`}
            </span>
          </div>
          {runResult.errorMessage && (
            <pre className="text-xs font-mono text-red-700 whitespace-pre-wrap leading-relaxed">
              {runResult.errorMessage}
            </pre>
          )}
          {runResult.testResults.filter((t) => !t.passed).slice(0, 2).map((t, i) => (
            <div key={i} className="text-xs font-mono space-y-0.5 text-amber-800">
              <div>Input: <span className="text-gray-700">{t.input}</span></div>
              <div>Expected: <span className="text-gray-700">{t.expectedOutput}</span></div>
              <div>Got: <span className="text-gray-700">{t.actualOutput}</span></div>
            </div>
          ))}
        </div>
      )}

      {/* Submission result */}
      {submission && submission.status !== "ACCEPTED" && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 flex items-center gap-2">
          <XCircle size={14} className="text-red-600" />
          <span className="text-xs font-semibold text-red-700">
            {submission.status.replace(/_/g, " ")} — keep trying!
          </span>
        </div>
      )}
    </div>
  );
}
