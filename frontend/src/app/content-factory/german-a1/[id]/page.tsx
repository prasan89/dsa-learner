"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ChevronDown, ChevronUp } from "lucide-react";
import { pipelineApi, type CfLesson, type CfLessonVersion, type CfAgentRun } from "@/lib/api/pipeline";

// ── Types inferred from the actual lesson JSON schema ─────────────────────────

interface VocabItem {
  german: string;
  english: string;
  example?: string;
  pronunciation?: string;
}

interface GrammarExample {
  german: string;
  english: string;
}

interface Grammar {
  title?: string;
  pattern?: string;
  explanation?: string;
  examples?: GrammarExample[];
}

interface ContentExample {
  german: string;
  english: string;
  context?: string;
}

interface ContentBlock {
  objectives?: string[];
  explanation?: { intro?: string; culturalNote?: string };
  examples?: ContentExample[];
}

interface Exercise {
  type: string;
  question?: string;
  source?: string;
  correctAnswer?: string;
  options?: string[];
  hint?: string;
  explanation?: string;
}

// ── Status helpers ────────────────────────────────────────────────────────────

const STATUS_COLOR: Record<string, string> = {
  QA_PASSED: "bg-green-100 text-green-800",
  QA_PENDING: "bg-yellow-100 text-yellow-800",
  QA_FAILED: "bg-red-100 text-red-800",
  REVISION: "bg-orange-100 text-orange-800",
  GENERATING: "bg-blue-100 text-blue-800",
  GENERATED: "bg-blue-100 text-blue-800",
  VALIDATION_PENDING: "bg-purple-100 text-purple-800",
  VALIDATION_FAILED: "bg-red-100 text-red-800",
  DRAFT: "bg-gray-100 text-gray-600",
  APPROVED: "bg-green-100 text-green-800",
  HUMAN_REVIEW_REQUIRED: "bg-red-100 text-red-800",
  SUCCEEDED: "bg-green-100 text-green-700",
  FAILED: "bg-red-100 text-red-700",
};

const QA_AGENT_LABELS: Record<string, string> = {
  linguistic_qa: "Linguistic QA",
  cefr_qa: "CEFR QA",
  exercise_qa: "Exercise QA",
  pedagogy_qa: "Pedagogy QA",
};

// ── Collapsible section ────────────────────────────────────────────────────────

function Section({ title, children, defaultOpen = true }: { title: string; children: React.ReactNode; defaultOpen?: boolean }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
      <button
        onClick={() => setOpen((o) => !o)}
        className="w-full flex items-center justify-between px-5 py-3.5 text-left hover:bg-gray-50 transition-colors"
      >
        <span className="text-sm font-semibold text-gray-700">{title}</span>
        {open ? <ChevronUp size={14} className="text-gray-400" /> : <ChevronDown size={14} className="text-gray-400" />}
      </button>
      {open && <div className="px-5 pb-5 pt-1">{children}</div>}
    </div>
  );
}

// ── QA section ────────────────────────────────────────────────────────────────

function QaSection({ runs }: { runs: CfAgentRun[] }) {
  const qaRuns = runs.filter((r) => r.agentType in QA_AGENT_LABELS);
  if (qaRuns.length === 0) {
    return <p className="text-xs text-gray-400 italic">No QA runs found for this version.</p>;
  }

  return (
    <div className="space-y-4">
      {qaRuns.map((run) => {
        const label = QA_AGENT_LABELS[run.agentType] ?? run.agentType;
        const output = run.output as { issues?: Array<{ error: boolean; field?: string; message: string; code?: string }>; recommendations?: string[]; overallAssessment?: string } | null;
        const issues = output?.issues ?? [];
        const errors = issues.filter((i) => i.error);
        const warnings = issues.filter((i) => !i.error);
        const decision = run.status === "FAILED" || errors.length > 0 ? "FAIL" : warnings.length > 0 ? "PASS_WITH_WARNINGS" : "PASS";

        return (
          <div key={run.id} className="border border-gray-100 rounded-lg p-4 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-700">{label}</span>
              <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                decision === "FAIL" ? "bg-red-100 text-red-700"
                : decision === "PASS_WITH_WARNINGS" ? "bg-yellow-100 text-yellow-700"
                : "bg-green-100 text-green-700"
              }`}>
                {decision.replace(/_/g, " ")}
              </span>
            </div>

            {errors.length > 0 && (
              <div className="space-y-1">
                {errors.map((issue, j) => (
                  <div key={j} className="text-xs text-red-700 bg-red-50 rounded px-2.5 py-1.5">
                    {issue.field && <span className="font-mono font-semibold">{issue.field}: </span>}
                    {issue.message}
                  </div>
                ))}
              </div>
            )}

            {warnings.length > 0 && (
              <div className="space-y-1">
                {warnings.map((issue, j) => (
                  <div key={j} className="text-xs text-yellow-700 bg-yellow-50 rounded px-2.5 py-1.5">
                    {issue.field && <span className="font-mono font-semibold">{issue.field}: </span>}
                    {issue.message}
                  </div>
                ))}
              </div>
            )}

            {output?.overallAssessment && issues.length === 0 && (
              <p className="text-xs text-gray-500 italic">{output.overallAssessment}</p>
            )}
          </div>
        );
      })}
    </div>
  );
}

// ── Main page ────────────────────────────────────────────────────────────────

export default function LessonDetailPage() {
  const params = useParams();
  const lessonId = params.id as string;

  const [lesson, setLesson] = useState<CfLesson | null>(null);
  const [versions, setVersions] = useState<CfLessonVersion[]>([]);
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null);
  const [versionData, setVersionData] = useState<CfLessonVersion | null>(null);
  const [agentRuns, setAgentRuns] = useState<CfAgentRun[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      pipelineApi.getLesson(lessonId),
      pipelineApi.listVersions(lessonId),
    ])
      .then(([lessonRes, versionsRes]) => {
        setLesson(lessonRes.data);
        const vList = versionsRes.data;
        setVersions(vList);
        // Default to current version; fallback to latest
        const current = lessonRes.data.currentVersion;
        const target = vList.find((v) => v.version === current) ?? vList[vList.length - 1];
        if (target) setSelectedVersion(target.version);
      })
      .catch((err) => setError(err.message ?? "Failed to load lesson"))
      .finally(() => setLoading(false));
  }, [lessonId]);

  const loadVersion = useCallback(
    (version: number) => {
      setSelectedVersion(version);
      Promise.all([
        pipelineApi.getVersion(lessonId, version),
        pipelineApi.getAgentRuns(lessonId, version),
      ]).then(([vRes, runsRes]) => {
        setVersionData(vRes.data);
        setAgentRuns(runsRes.data);
      });
    },
    [lessonId]
  );

  useEffect(() => {
    if (selectedVersion !== null) loadVersion(selectedVersion);
  }, [selectedVersion, loadVersion]);

  if (loading)
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-sm text-gray-400">Loading…</p>
      </div>
    );

  if (error || !lesson)
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-sm text-red-500">{error ?? "Lesson not found"}</p>
      </div>
    );

  const content = versionData?.content as ContentBlock | null;
  const vocab = (versionData?.vocabulary as { items?: VocabItem[] } | null)?.items ?? [];
  const grammar = versionData?.grammar as Grammar | null;
  const exercises = (versionData?.exercises as { items?: Exercise[] } | null)?.items ?? [];
  const qaRuns = agentRuns.filter((r) => r.agentType in QA_AGENT_LABELS);

  // Compute QA overall decision from runs
  const hasQaErrors = qaRuns.some((r) => {
    const out = r.output as { issues?: Array<{ error: boolean }> } | null;
    return r.status === "FAILED" || (out?.issues ?? []).some((i) => i.error);
  });
  const hasQaWarnings = qaRuns.some((r) => {
    const out = r.output as { issues?: Array<{ error: boolean }> } | null;
    return (out?.issues ?? []).some((i) => !i.error);
  });
  const qaDecision =
    qaRuns.length === 0 ? "—"
    : hasQaErrors ? "FAIL"
    : hasQaWarnings ? "PASS_WITH_WARNINGS"
    : "PASS";

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-10 space-y-6">

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-xs text-gray-400">
          <Link href="/content-factory/german-a1" className="hover:text-brand-600">Content Factory</Link>
          <span>/</span>
          <Link href="/content-factory/german-a1" className="hover:text-brand-600">German A1</Link>
          <span>/</span>
          <span className="text-gray-600">{lesson.title}</span>
        </div>

        {/* Title + badge */}
        <div className="space-y-2">
          <p className="text-xs font-semibold text-brand-600 uppercase tracking-widest">German A1</p>
          <h1 className="text-2xl font-bold text-gray-900">{lesson.title}</h1>
          <div className="flex items-center gap-2 flex-wrap">
            <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_COLOR[lesson.contentStatus] ?? "bg-gray-100 text-gray-600"}`}>
              {lesson.contentStatus.replace(/_/g, " ")}
            </span>
            <span className="text-xs text-gray-400">v{lesson.currentVersion}</span>
            {lesson.revisionCount > 0 && (
              <span className="text-xs text-gray-400">· {lesson.revisionCount} revision{lesson.revisionCount > 1 ? "s" : ""}</span>
            )}
          </div>
        </div>

        {/* Version selector */}
        {versions.length > 1 && (
          <Section title="Version" defaultOpen={true}>
            <div className="space-y-1.5">
              {[...versions].reverse().map((v) => (
                <button
                  key={v.version}
                  onClick={() => loadVersion(v.version)}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-colors ${
                    selectedVersion === v.version
                      ? "bg-brand-50 border border-brand-200"
                      : "hover:bg-gray-50 border border-transparent"
                  }`}
                >
                  <span className={`w-3 h-3 rounded-full border-2 flex-shrink-0 ${
                    selectedVersion === v.version ? "border-brand-600 bg-brand-600" : "border-gray-300"
                  }`} />
                  <span className="text-sm font-medium text-gray-800">
                    v{v.version}
                    {v.version === lesson.currentVersion && (
                      <span className="ml-2 text-xs font-normal text-gray-400">— Current</span>
                    )}
                    {v.parentVersion && (
                      <span className="ml-2 text-xs font-normal text-gray-400">— Revised from v{v.parentVersion}</span>
                    )}
                  </span>
                  <span className={`ml-auto text-xs px-2 py-0.5 rounded-full ${STATUS_COLOR[v.contentStatus] ?? "bg-gray-100 text-gray-500"}`}>
                    {v.contentStatus.replace(/_/g, " ")}
                  </span>
                </button>
              ))}
            </div>
          </Section>
        )}

        {/* Content Factory metadata */}
        <Section title="Content Factory" defaultOpen={true}>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {[
              { label: "Version", value: `v${selectedVersion ?? lesson.currentVersion}` },
              { label: "Parent Version", value: versionData?.parentVersion ? `v${versionData.parentVersion}` : "—" },
              { label: "Revision Count", value: String(lesson.revisionCount) },
              { label: "Content Status", value: (versionData?.contentStatus ?? lesson.contentStatus).replace(/_/g, " ") },
              { label: "QA Decision", value: qaDecision.replace(/_/g, " ") },
              { label: "Max Revisions", value: String(lesson.maxRevisionAttempts) },
            ].map(({ label, value }) => (
              <div key={label} className="bg-gray-50 rounded-lg px-3 py-2.5">
                <p className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">{label}</p>
                <p className="text-sm font-semibold text-gray-800 mt-0.5">{value}</p>
              </div>
            ))}
          </div>
        </Section>

        {/* No content warning */}
        {versionData && !content && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl px-5 py-4 text-sm text-yellow-700">
            No lesson content stored for this version (status: {versionData.contentStatus}).
          </div>
        )}

        {/* Lesson objective */}
        {content?.objectives && content.objectives.length > 0 && (
          <Section title="Lesson Objectives">
            <ul className="space-y-2">
              {content.objectives.map((obj, i) => (
                <li key={i} className="flex items-start gap-2.5 text-sm text-gray-700">
                  <span className="mt-0.5 w-4 h-4 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-[10px] font-bold flex-shrink-0">
                    {i + 1}
                  </span>
                  {obj}
                </li>
              ))}
            </ul>
            {content.explanation?.intro && (
              <p className="mt-3 text-sm text-gray-500 border-t border-gray-100 pt-3">
                {content.explanation.intro}
              </p>
            )}
            {content.explanation?.culturalNote && (
              <p className="mt-2 text-xs text-blue-600 bg-blue-50 rounded-lg px-3 py-2">
                <span className="font-semibold">Cultural note:</span> {content.explanation.culturalNote}
              </p>
            )}
          </Section>
        )}

        {/* Vocabulary */}
        {vocab.length > 0 && (
          <Section title={`Vocabulary (${vocab.length} items)`}>
            <div className="divide-y divide-gray-50">
              <div className="grid grid-cols-3 gap-2 pb-2 text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
                <span>German</span>
                <span>English</span>
                <span>Example</span>
              </div>
              {vocab.map((item, i) => (
                <div key={i} className="grid grid-cols-3 gap-2 py-2.5 text-sm">
                  <div>
                    <span className="font-semibold text-gray-900">{item.german}</span>
                    {item.pronunciation && (
                      <p className="text-[10px] text-gray-400 mt-0.5 font-mono">{item.pronunciation}</p>
                    )}
                  </div>
                  <span className="text-gray-600">{item.english}</span>
                  <span className="text-gray-400 text-xs">{item.example ?? "—"}</span>
                </div>
              ))}
            </div>
          </Section>
        )}

        {/* Grammar */}
        {grammar && (
          <Section title="Grammar">
            {grammar.title && (
              <h3 className="text-sm font-semibold text-gray-800">{grammar.title}</h3>
            )}
            {grammar.pattern && (
              <p className="mt-1.5 text-xs font-mono bg-gray-50 rounded px-3 py-2 text-gray-700">
                {grammar.pattern}
              </p>
            )}
            {grammar.explanation && (
              <p className="mt-3 text-sm text-gray-600 leading-relaxed">{grammar.explanation}</p>
            )}
            {grammar.examples && grammar.examples.length > 0 && (
              <div className="mt-3 space-y-1.5">
                {grammar.examples.map((ex, i) => (
                  <div key={i} className="flex items-start gap-3 text-sm">
                    <span className="font-semibold text-gray-900 min-w-0 flex-1">{ex.german}</span>
                    <span className="text-gray-500 min-w-0 flex-1">{ex.english}</span>
                  </div>
                ))}
              </div>
            )}
          </Section>
        )}

        {/* Examples */}
        {content?.examples && content.examples.length > 0 && (
          <Section title={`Examples (${content.examples.length})`}>
            <div className="space-y-3">
              {content.examples.map((ex, i) => (
                <div key={i} className="border-l-2 border-brand-200 pl-3">
                  <p className="text-sm font-semibold text-gray-900">{ex.german}</p>
                  <p className="text-sm text-gray-500">{ex.english}</p>
                  {ex.context && (
                    <p className="text-xs text-gray-400 mt-0.5 italic">{ex.context}</p>
                  )}
                </div>
              ))}
            </div>
          </Section>
        )}

        {/* Exercises */}
        {exercises.length > 0 && (
          <Section title={`Exercises (${exercises.length})`}>
            <div className="space-y-4">
              {exercises.map((ex, i) => (
                <div key={i} className="border border-gray-100 rounded-lg p-4 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-gray-400">Exercise {i + 1}</span>
                    <span className="text-[10px] font-mono bg-gray-100 text-gray-500 px-2 py-0.5 rounded">
                      {ex.type}
                    </span>
                  </div>

                  {ex.question && (
                    <p className="text-sm font-medium text-gray-800">{ex.question}</p>
                  )}
                  {ex.source && (
                    <p className="text-sm font-medium text-gray-800">Translate: <em>{ex.source}</em></p>
                  )}

                  {ex.options && (
                    <div className="flex flex-wrap gap-2">
                      {ex.options.map((opt, j) => (
                        <span
                          key={j}
                          className={`text-xs px-2.5 py-1 rounded-full border ${
                            opt === ex.correctAnswer
                              ? "border-green-300 bg-green-50 text-green-800 font-semibold"
                              : "border-gray-200 text-gray-600"
                          }`}
                        >
                          {opt}
                        </span>
                      ))}
                    </div>
                  )}

                  {ex.correctAnswer && !ex.options && (
                    <p className="text-xs text-green-700 bg-green-50 rounded px-2.5 py-1.5">
                      <span className="font-semibold">Answer:</span> {ex.correctAnswer}
                    </p>
                  )}

                  {ex.hint && (
                    <p className="text-xs text-blue-600 italic">{ex.hint}</p>
                  )}

                  {ex.explanation && (
                    <p className="text-xs text-gray-400">{ex.explanation}</p>
                  )}
                </div>
              ))}
            </div>
          </Section>
        )}

        {/* QA Results */}
        <Section title="QA Results" defaultOpen={false}>
          <QaSection runs={agentRuns} />
        </Section>

      </div>
    </div>
  );
}
