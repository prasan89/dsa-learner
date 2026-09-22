"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { CheckCircle2, Lock, Play, ChevronRight, Clock, Code2 } from "lucide-react";
import { patternsApi } from "@/lib/api/patterns";
import { problemsApi } from "@/lib/api/problems";
import type { Pattern, MasteryStatus } from "@/types";
import { difficultyBadge } from "@/lib/utils";
import { toast } from "react-hot-toast";

const MASTERY_OPTIONS: { value: MasteryStatus; label: string }[] = [
  { value: "NOT_STARTED", label: "Not Started" },
  { value: "LEARNING",    label: "Learning" },
  { value: "PRACTICED",   label: "Practiced" },
  { value: "MASTERED",    label: "Mastered" },
];

const MASTERY_STYLE: Record<MasteryStatus, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-600 border-gray-200",
  LEARNING:    "bg-blue-50 text-blue-700 border-blue-200",
  PRACTICED:   "bg-yellow-50 text-yellow-700 border-yellow-200",
  MASTERED:    "bg-green-50 text-green-700 border-green-200",
};

type TabId = "overview" | "lesson" | "problems" | "notes";

interface JourneyStep {
  num: number;
  title: string;
  subtitle: string;
  tab: TabId;
  locked: boolean;
}

export default function PatternDetailPage({ params }: { params: { slug: string } }) {
  const [pattern, setPattern]       = useState<Pattern | null>(null);
  const [problems, setProblems]     = useState<any[]>([]);
  const [loading, setLoading]       = useState(true);
  const [savingMastery, setSaving]  = useState(false);
  const [activeTab, setActiveTab]   = useState<TabId>("overview");

  useEffect(() => {
    patternsApi.get(params.slug).then((r) => {
      setPattern(r.data);
      return problemsApi.list({ patternId: r.data.id });
    }).then((r) => {
      setProblems((r.data as any).content ?? []);
    }).finally(() => setLoading(false));
  }, [params.slug]);

  async function handleMastery(status: MasteryStatus) {
    if (!pattern) return;
    setSaving(true);
    try {
      await patternsApi.updateMastery(pattern.slug, status);
      setPattern((p) => p ? { ...p, masteryStatus: status } : p);
      toast.success(`Mastery updated: ${status.replace(/_/g, " ")}`);
    } catch {
      toast.error("Failed to update mastery");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 text-sm">Loading…</p>
    </div>
  );
  if (!pattern) return null;

  const mastery = (pattern.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
  const masteryIdx = MASTERY_OPTIONS.findIndex(o => o.value === mastery);
  const easyProblems = problems.filter(p => p.difficulty === "EASY");
  const hardProblems = problems.filter(p => p.difficulty !== "EASY");

  const JOURNEY: JourneyStep[] = [
    { num: 1, title: "Understand the Pattern", subtitle: "Concept, intuition and when to use", tab: "overview",  locked: false },
    { num: 2, title: "Learn with Examples",    subtitle: "Step by step explanation",           tab: "lesson",   locked: false },
    { num: 3, title: "Practice Problems",      subtitle: "Start with easy problems",           tab: "problems", locked: false },
    { num: 4, title: "Challenge Problems",     subtitle: "Test your understanding",            tab: "problems", locked: masteryIdx < 1 },
    { num: 5, title: "Pattern Mastery",        subtitle: "Track your progress",                tab: "overview", locked: masteryIdx < 2 },
  ];

  const activeStep = masteryIdx + 2; // rough mapping

  const TABS: { id: TabId; label: string }[] = [
    { id: "overview",  label: "Overview" },
    { id: "lesson",    label: "Lessons" },
    { id: "problems",  label: "Problems" },
    { id: "notes",     label: "Notes" },
  ];

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-5xl mx-auto">
          {/* Breadcrumb */}
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-3">
            <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
            <ChevronRight size={12} />
            <Link href="/patterns" className="hover:text-gray-600">Patterns</Link>
            <ChevronRight size={12} />
            <span className="text-gray-600">{pattern.name}</span>
          </div>

          <div className="flex items-start justify-between gap-6">
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-gray-900">{pattern.name}</h1>
              <p className="text-gray-500 mt-1 text-sm leading-relaxed max-w-2xl">{pattern.summary}</p>

              {(pattern.timeComplexity || pattern.spaceComplexity) && (
                <div className="flex gap-2 mt-3">
                  {pattern.timeComplexity && (
                    <span className="text-xs font-mono bg-gray-100 border border-gray-200 rounded px-2 py-1 text-gray-700">
                      ⏱ {pattern.timeComplexity}
                    </span>
                  )}
                  {pattern.spaceComplexity && (
                    <span className="text-xs font-mono bg-gray-100 border border-gray-200 rounded px-2 py-1 text-gray-700">
                      💾 {pattern.spaceComplexity}
                    </span>
                  )}
                </div>
              )}
            </div>

            {/* Mastery selector */}
            <div className="shrink-0">
              <p className="text-xs text-gray-400 mb-1.5 text-right">Your mastery</p>
              <select
                value={mastery}
                disabled={savingMastery}
                onChange={(e) => handleMastery(e.target.value as MasteryStatus)}
                className={`text-sm font-medium px-3 py-2 rounded-lg border cursor-pointer transition-colors outline-none ${MASTERY_STYLE[mastery]}`}
              >
                {MASTERY_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-1 mt-5 border-b border-gray-100 -mb-5">
            {TABS.map((t) => (
              <button key={t.id} onClick={() => setActiveTab(t.id)}
                className={`px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px ${
                  activeTab === t.id
                    ? "text-brand-600 border-brand-600"
                    : "text-gray-500 hover:text-gray-700 border-transparent"
                }`}>
                {t.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Body */}
      <div className="max-w-5xl mx-auto px-6 py-6">

        {/* Overview: 5-step learning journey */}
        {activeTab === "overview" && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

            {/* Journey steps */}
            <div className="lg:col-span-2 space-y-3">
              {JOURNEY.map((step) => {
                const done   = step.num < activeStep;
                const active = step.num === activeStep;
                return (
                  <div key={step.num}
                    className={`card p-4 flex items-center gap-4 transition-all ${
                      step.locked ? "opacity-50" : "hover:border-brand-200 cursor-pointer"
                    } ${active ? "border-brand-300 bg-brand-50/40" : ""}`}
                    onClick={() => !step.locked && setActiveTab(step.tab)}
                  >
                    {/* Step indicator */}
                    <div className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${
                      done   ? "bg-green-500 text-white" :
                      active ? "bg-brand-600 text-white" :
                      step.locked ? "bg-gray-200 text-gray-400" :
                               "bg-gray-100 text-gray-500"
                    }`}>
                      {done ? <CheckCircle2 size={18} /> : step.locked ? <Lock size={14} /> : step.num}
                    </div>

                    <div className="flex-1">
                      <p className={`font-semibold text-sm ${
                        active ? "text-brand-700" : done ? "text-gray-500" : "text-gray-800"
                      }`}>{step.title}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{step.subtitle}</p>
                    </div>

                    {active && (
                      <button onClick={() => setActiveTab(step.tab)}
                        className="btn-primary flex items-center gap-1.5 shrink-0">
                        <Play size={13} /> Start
                      </button>
                    )}
                    {!active && !done && !step.locked && (
                      <ChevronRight size={16} className="text-gray-300 shrink-0" />
                    )}
                  </div>
                );
              })}
            </div>

            {/* Recognition clues sidebar */}
            <div className="space-y-4">
              <div className="card p-4">
                <h3 className="text-sm font-semibold text-gray-700 mb-3">Recognition Clues</h3>
                <ul className="space-y-2">
                  {pattern.recognitionClues.filter(Boolean).map((clue, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-gray-600">
                      <CheckCircle2 size={14} className="text-green-500 mt-0.5 shrink-0" />
                      {clue.trim()}
                    </li>
                  ))}
                </ul>
              </div>

              {pattern.templateCode && (
                <div className="card p-4">
                  <h3 className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
                    <Code2 size={14} />Java Template
                  </h3>
                  <pre className="text-xs font-mono text-gray-600 bg-gray-50 rounded-lg p-3 overflow-x-auto whitespace-pre-wrap">
                    {pattern.templateCode.slice(0, 300)}{pattern.templateCode.length > 300 ? "…" : ""}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Lesson tab */}
        {activeTab === "lesson" && (
          <div className="max-w-3xl">
            {pattern.lessonMarkdown ? (
              <div className="card p-6 prose prose-sm max-w-none
                prose-headings:text-gray-900 prose-headings:font-bold
                prose-h2:text-lg prose-h2:border-b prose-h2:border-gray-100 prose-h2:pb-2
                prose-h3:text-brand-600 prose-h3:text-base
                prose-p:text-gray-600 prose-p:leading-relaxed
                prose-code:text-green-700 prose-code:bg-green-50 prose-code:px-1.5 prose-code:rounded prose-code:text-xs
                prose-pre:bg-gray-900 prose-pre:text-gray-100 prose-pre:rounded-xl
                prose-table:text-sm prose-th:text-gray-600 prose-td:text-gray-700
                prose-strong:text-gray-900 prose-li:text-gray-600">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {pattern.lessonMarkdown}
                </ReactMarkdown>
              </div>
            ) : (
              <div className="card p-12 text-center">
                <p className="text-gray-400 text-sm">Lesson coming soon.</p>
              </div>
            )}
          </div>
        )}

        {/* Problems tab */}
        {activeTab === "problems" && (
          <div className="space-y-6">
            {/* Easy problems */}
            {easyProblems.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <Play size={13} className="text-green-500" /> Practice Problems
                </h3>
                <div className="card overflow-hidden">
                  <table className="w-full text-sm">
                    <tbody>
                      {easyProblems.map((p, i) => (
                        <tr key={p.id}
                          className={`hover:bg-gray-50 transition-colors ${i < easyProblems.length - 1 ? "border-b border-gray-100" : ""}`}>
                          <td className="px-4 py-3">
                            {p.solved && <CheckCircle2 size={13} className="text-green-500 inline mr-2" />}
                            <Link href={`/problems/${p.slug}`}
                              className="font-medium text-gray-800 hover:text-brand-600 transition-colors">
                              {p.title}
                            </Link>
                          </td>
                          <td className="px-4 py-3 w-24">
                            <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                          </td>
                          <td className="px-4 py-3 w-16 text-right">
                            <Link href={`/problems/${p.slug}`} className="text-brand-600 hover:text-brand-700">
                              <ChevronRight size={16} />
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Challenge problems */}
            {hardProblems.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <Lock size={13} className={masteryIdx < 1 ? "text-gray-400" : "text-orange-500"} />
                  Challenge Problems
                  {masteryIdx < 1 && <span className="text-xs text-gray-400 font-normal normal-case">(unlock by reaching Learning level)</span>}
                </h3>
                <div className={`card overflow-hidden ${masteryIdx < 1 ? "opacity-50 pointer-events-none" : ""}`}>
                  <table className="w-full text-sm">
                    <tbody>
                      {hardProblems.map((p, i) => (
                        <tr key={p.id}
                          className={`hover:bg-gray-50 transition-colors ${i < hardProblems.length - 1 ? "border-b border-gray-100" : ""}`}>
                          <td className="px-4 py-3">
                            {p.solved && <CheckCircle2 size={13} className="text-green-500 inline mr-2" />}
                            <Link href={`/problems/${p.slug}`}
                              className="font-medium text-gray-800 hover:text-brand-600 transition-colors">
                              {p.title}
                            </Link>
                          </td>
                          <td className="px-4 py-3 w-24">
                            <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                          </td>
                          <td className="px-4 py-3 w-16 text-right">
                            <Link href={`/problems/${p.slug}`} className="text-brand-600 hover:text-brand-700">
                              <ChevronRight size={16} />
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {problems.length === 0 && (
              <div className="card p-12 text-center">
                <Code2 size={32} className="text-gray-300 mx-auto mb-2" />
                <p className="text-gray-400 text-sm">No problems linked yet.</p>
              </div>
            )}
          </div>
        )}

        {/* Notes tab */}
        {activeTab === "notes" && (
          <div className="card p-12 text-center">
            <Clock size={32} className="text-gray-300 mx-auto mb-2" />
            <p className="text-gray-400 text-sm">Notes coming soon.</p>
          </div>
        )}
      </div>
    </div>
  );
}
