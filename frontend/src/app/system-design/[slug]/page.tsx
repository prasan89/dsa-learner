"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { ChevronRight, CheckCircle2, BookOpen, MessageSquare } from "lucide-react";
import { patternsApi } from "@/lib/api/patterns";
import { problemsApi } from "@/lib/api/problems";
import type { Pattern, MasteryStatus } from "@/types";
import { toast } from "react-hot-toast";

const MASTERY_STYLE: Record<MasteryStatus, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-600 border-gray-200",
  LEARNING:    "bg-blue-50 text-blue-700 border-blue-200",
  PRACTICED:   "bg-yellow-50 text-yellow-700 border-yellow-200",
  MASTERED:    "bg-green-50 text-green-700 border-green-200",
};

const MASTERY_OPTIONS: { value: MasteryStatus; label: string }[] = [
  { value: "NOT_STARTED", label: "Not Started" },
  { value: "LEARNING",    label: "Learning" },
  { value: "PRACTICED",   label: "Practiced" },
  { value: "MASTERED",    label: "Mastered" },
];

type TabId = "overview" | "architecture" | "deep-dive" | "trade-offs" | "interview-qa" | "practice";

const TABS: { id: TabId; label: string }[] = [
  { id: "overview",      label: "Overview" },
  { id: "architecture",  label: "Architecture" },
  { id: "deep-dive",     label: "Deep Dive" },
  { id: "trade-offs",    label: "Trade-offs" },
  { id: "interview-qa",  label: "Interview Q&A" },
  { id: "practice",      label: "Practice" },
];

export default function SystemDesignTopicPage({ params }: { params: { slug: string } }) {
  const [pattern, setPattern]     = useState<Pattern | null>(null);
  const [problems, setProblems]   = useState<any[]>([]);
  const [loading, setLoading]     = useState(true);
  const [saving, setSaving]       = useState(false);
  const [activeTab, setActiveTab] = useState<TabId>("overview");

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
      toast.success(`Mastery updated`);
    } catch { toast.error("Failed to update"); }
    finally { setSaving(false); }
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 text-sm">Loading…</p>
    </div>
  );
  if (!pattern) return null;

  const mastery = (pattern.masteryStatus ?? "NOT_STARTED") as MasteryStatus;
  const qaProblems = problems.filter(p => p.slug?.startsWith("sd-"));

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-5xl mx-auto">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-3">
            <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
            <ChevronRight size={12} />
            <Link href="/system-design" className="hover:text-gray-600">System Design</Link>
            <ChevronRight size={12} />
            <span className="text-gray-600 font-medium">{pattern.name}</span>
          </div>

          <div className="flex items-start justify-between gap-6">
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-gray-900">{pattern.name}</h1>
              <p className="text-gray-500 text-sm mt-1 max-w-2xl leading-relaxed">{pattern.summary}</p>
            </div>
            <div className="shrink-0 flex items-center gap-3">
              <select value={mastery} disabled={saving}
                onChange={(e) => handleMastery(e.target.value as MasteryStatus)}
                className={`text-sm font-medium px-3 py-2 rounded-lg border cursor-pointer outline-none transition-colors ${MASTERY_STYLE[mastery]}`}>
                {MASTERY_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
              {mastery === "MASTERED" && (
                <button className="btn-secondary text-green-700 border-green-200 bg-green-50 hover:bg-green-100">
                  ✓ Mark as Complete
                </button>
              )}
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-0 mt-5 border-b border-gray-100 -mb-5 overflow-x-auto">
            {TABS.map((t) => (
              <button key={t.id} onClick={() => setActiveTab(t.id)}
                className={`px-4 py-2.5 text-sm font-medium whitespace-nowrap transition-colors border-b-2 -mb-px ${
                  activeTab === t.id
                    ? "text-brand-600 border-brand-600"
                    : "text-gray-500 hover:text-gray-700 border-transparent"
                }`}>
                {t.label}
                {t.id === "interview-qa" && qaProblems.length > 0 && (
                  <span className="ml-1.5 text-xs bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded-full">
                    {qaProblems.length}
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Body */}
      <div className="max-w-5xl mx-auto px-6 py-6">

        {/* Overview / Lesson */}
        {(activeTab === "overview" || activeTab === "deep-dive") && (
          pattern.lessonMarkdown ? (
            <div className="card p-6 prose prose-sm max-w-none
              prose-headings:text-gray-900 prose-headings:font-bold
              prose-h2:text-lg prose-h2:border-b prose-h2:border-gray-100 prose-h2:pb-2 prose-h2:mt-8
              prose-h3:text-brand-700 prose-h3:text-base
              prose-p:text-gray-600 prose-p:leading-relaxed
              prose-code:text-green-700 prose-code:bg-green-50 prose-code:px-1.5 prose-code:rounded prose-code:text-xs
              prose-pre:bg-gray-900 prose-pre:text-gray-100 prose-pre:rounded-xl
              prose-table:text-sm prose-th:bg-gray-50 prose-th:text-gray-600 prose-td:text-gray-700
              prose-strong:text-gray-900 prose-li:text-gray-600
              prose-blockquote:border-brand-300 prose-blockquote:text-gray-500">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {pattern.lessonMarkdown}
              </ReactMarkdown>
            </div>
          ) : (
            <div className="card p-12 text-center">
              <BookOpen size={32} className="text-gray-300 mx-auto mb-2" />
              <p className="text-gray-400 text-sm">Lesson content coming soon.</p>
            </div>
          )
        )}

        {/* Architecture */}
        {activeTab === "architecture" && (
          <div className="card p-12 text-center">
            <p className="text-4xl mb-3">🏗️</p>
            <p className="text-gray-400 text-sm">Architecture diagrams coming soon.</p>
          </div>
        )}

        {/* Trade-offs */}
        {activeTab === "trade-offs" && (
          <div className="card p-12 text-center">
            <p className="text-4xl mb-3">⚖️</p>
            <p className="text-gray-400 text-sm">Trade-off tables coming soon.</p>
          </div>
        )}

        {/* Interview Q&A */}
        {activeTab === "interview-qa" && (
          <div>
            {qaProblems.length === 0 ? (
              <div className="card p-12 text-center">
                <MessageSquare size={32} className="text-gray-300 mx-auto mb-2" />
                <p className="text-gray-400 text-sm">No interview questions linked yet.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {qaProblems.map((p) => (
                  <Link key={p.id} href={`/system-design/question/${p.slug}`}
                    className="card hover:border-brand-200 hover:shadow-sm p-5 flex items-center gap-4 transition-all group">
                    <div className="w-8 h-8 rounded-lg bg-brand-50 flex items-center justify-center shrink-0">
                      <MessageSquare size={14} className="text-brand-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-gray-800 group-hover:text-brand-600 transition-colors">
                        {p.title}
                      </p>
                      {p.constraints && (
                        <p className="text-xs text-gray-400 mt-0.5 truncate">{p.constraints}</p>
                      )}
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      {p.solved && <CheckCircle2 size={14} className="text-green-500" />}
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                        p.difficulty === "EASY"   ? "text-green-700 bg-green-100" :
                        p.difficulty === "MEDIUM" ? "text-yellow-700 bg-yellow-100" :
                                                    "text-red-700 bg-red-100"
                      }`}>{p.difficulty}</span>
                      <ChevronRight size={14} className="text-gray-300 group-hover:text-brand-400" />
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Practice */}
        {activeTab === "practice" && (
          <div className="card p-12 text-center">
            <p className="text-4xl mb-3">💪</p>
            <p className="text-gray-400 text-sm">Practice problems coming soon.</p>
          </div>
        )}
      </div>
    </div>
  );
}
