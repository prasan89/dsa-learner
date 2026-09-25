"use client";

import { useState, useCallback } from "react";
import { ARRAY_CONCEPTS, type ModuleId } from "@/lib/visualizer/tracers/arrayConceptTracers";
import InteractiveConceptVisualizer from "@/components/visualizer/InteractiveConceptVisualizer";
import ConceptCompletionPanel from "@/components/learn/ConceptCompletionPanel";
import ConceptPracticePanel from "@/components/learn/ConceptPracticePanel";
import { ChevronRight, ChevronDown, BookOpen, Lock } from "lucide-react";
import Link from "next/link";

type ViewMode = "learn" | "completion" | "practice";

const MODULE_META: Record<ModuleId, { label: string; description: string; color: string }> = {
  "foundations":       { label: "Foundations",          description: "Indexing, traversal, access, update, search",       color: "brand" },
  "core-operations":   { label: "Core Operations",       description: "Insert, delete, capacity, complexity overview",     color: "violet" },
  "two-pointers":      { label: "Two Pointers",          description: "Converging & parallel pointer techniques",          color: "blue" },
  "prefix-sum":        { label: "Prefix Sum",            description: "Range queries in O(1) after O(n) preprocessing",    color: "cyan" },
  "sliding-window":    { label: "Sliding Window",        description: "Fixed & variable window over contiguous subarrays", color: "teal" },
  "hashing":           { label: "Hashing",               description: "O(1) lookup with frequency maps & sets",            color: "emerald" },
  "kadane":            { label: "Kadane's Algorithm",    description: "Maximum subarray & variant problems",               color: "amber" },
  "binary-search":     { label: "Binary Search",         description: "O(log n) search on sorted arrays",                  color: "orange" },
  "sorting":           { label: "Sorting",               description: "Comparison & non-comparison sort algorithms",       color: "red" },
  "advanced":          { label: "Advanced Patterns",     description: "Merge intervals, next greater element, monotone stack", color: "rose" },
  "matrix":            { label: "Matrix (2-D Arrays)",   description: "Row/column traversal, spiral, rotation",            color: "pink" },
  "interview-thinking": { label: "Interview Thinking",  description: "Pattern recognition & problem-solving frameworks",  color: "indigo" },
};

const MODULE_ORDER: ModuleId[] = [
  "foundations", "core-operations", "two-pointers", "prefix-sum",
  "sliding-window", "hashing", "kadane", "binary-search",
  "sorting", "advanced", "matrix", "interview-thinking",
];

// Group concepts by module — computed once at module load time
const CONCEPTS_BY_MODULE: Partial<Record<ModuleId, typeof ARRAY_CONCEPTS>> = (() => {
  const map: Partial<Record<ModuleId, typeof ARRAY_CONCEPTS>> = {};
  for (const c of ARRAY_CONCEPTS) {
    if (!map[c.module]) map[c.module] = [];
    map[c.module]!.push(c);
  }
  return map;
})();

// Lessons that have a practice problem wired up
const HAS_PRACTICE = new Set(["linear-search", "remove-duplicates"]);

export default function LearnArraysPage() {
  const [activeId, setActiveId] = useState(ARRAY_CONCEPTS[0].id);
  const [viewMode, setViewMode] = useState<ViewMode>("learn");
  const [completedIds, setCompletedIds] = useState<Set<string>>(new Set());
  const [expandedModules, setExpandedModules] = useState<Set<ModuleId>>(new Set(["foundations"]));

  const concept = ARRAY_CONCEPTS.find((c) => c.id === activeId) ?? ARRAY_CONCEPTS[0];

  const currentIdx = ARRAY_CONCEPTS.findIndex((c) => c.id === activeId);
  const nextConcept = ARRAY_CONCEPTS[currentIdx + 1] ?? null;

  const totalAvailable = ARRAY_CONCEPTS.length;
  const completedCount = completedIds.size;

  // Unlock all modules that have at least one lesson built
  const unlockedModules = new Set<ModuleId>(
    MODULE_ORDER.filter((m) => (CONCEPTS_BY_MODULE[m]?.length ?? 0) > 0)
  );

  const toggleModule = useCallback((mod: ModuleId) => {
    setExpandedModules((prev) => {
      const next = new Set(prev);
      if (next.has(mod)) next.delete(mod);
      else next.add(mod);
      return next;
    });
  }, []);

  const handleSelectConcept = useCallback((id: string, module: ModuleId) => {
    if (!unlockedModules.has(module)) return;
    setActiveId(id);
    setViewMode("learn");
    // Auto-expand the module containing this concept
    setExpandedModules((prev) => new Set([...prev, module]));
  }, [unlockedModules]);

  const handleConceptComplete = useCallback(() => {
    setCompletedIds((prev) => new Set([...prev, activeId]));
    setViewMode("completion");
  }, [activeId]);

  const handleMastered = useCallback(() => {
    if (nextConcept) {
      setActiveId(nextConcept.id);
      setViewMode("learn");
      setExpandedModules((prev) => new Set([...prev, nextConcept.module]));
    } else {
      setViewMode("learn");
    }
  }, [nextConcept]);

  return (
    <div className="min-h-screen" style={{ background: "#FAFAFC" }}>
      <div className="max-w-6xl mx-auto px-6 py-10">

        {/* ── Page header ── */}
        <div className="mb-8">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-5">
            <Link href="/learn" className="hover:text-brand-600 transition-colors font-medium">Learn</Link>
            <ChevronRight size={11} className="text-gray-300" />
            <span className="text-gray-500 font-medium">Arrays Academy</span>
          </div>

          <div className="flex items-start justify-between gap-6">
            <div>
              <h1 className="text-[28px] font-bold tracking-tight text-gray-900 leading-none">Arrays Academy</h1>
              <p className="text-[14px] text-gray-500 mt-2 max-w-xl leading-relaxed">
                110 lessons across 12 modules — from memory fundamentals to advanced interview patterns.
                Master each module to unlock the next.
              </p>
            </div>
            <div className="flex-shrink-0 text-right">
              <div className="text-[28px] font-bold text-brand-600 leading-none tabular-nums">
                {completedCount}
                <span className="text-gray-400 text-sm font-normal ml-0.5">/{totalAvailable}</span>
              </div>
              <div className="text-xs text-gray-400 mt-1 font-medium">lessons done</div>
              <div className="text-xs font-semibold text-brand-500 mt-0.5 tabular-nums">
                {totalAvailable > 0 ? Math.round((completedCount / totalAvailable) * 100) : 0}%
              </div>
            </div>
          </div>

          {/* Overall progress bar */}
          <div className="mt-4 h-1.5 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-brand-500 rounded-full transition-all duration-500"
              style={{ width: totalAvailable > 0 ? `${(completedCount / totalAvailable) * 100}%` : "0%" }}
            />
          </div>
        </div>

        {/* ── Two-column layout ── */}
        <div className="flex gap-6 items-start">

          {/* ── Sidebar: module accordion ── */}
          <aside className="w-[240px] flex-shrink-0 space-y-1 sticky top-6">
            {MODULE_ORDER.map((modId) => {
              const meta = MODULE_META[modId];
              const lessons = CONCEPTS_BY_MODULE[modId] ?? [];
              const isUnlocked = unlockedModules.has(modId);
              const isExpanded = expandedModules.has(modId);
              const doneInModule = lessons.filter((l) => completedIds.has(l.id)).length;
              const pct = lessons.length > 0 ? (doneInModule / lessons.length) * 100 : 0;

              return (
                <div
                  key={modId}
                  className={`rounded-xl border overflow-hidden transition-all ${
                    isUnlocked
                      ? "border-gray-200 bg-white"
                      : "border-gray-100 bg-gray-50/40 opacity-50"
                  }`}
                >
                  {/* Module header */}
                  <button
                    onClick={() => isUnlocked && toggleModule(modId)}
                    disabled={!isUnlocked}
                    className={`w-full px-3 py-3 flex items-center gap-2.5 text-left ${
                      isUnlocked ? "hover:bg-gray-50/80 cursor-pointer" : "cursor-not-allowed"
                    }`}
                  >
                    {isUnlocked ? (
                      <ChevronDown
                        size={12}
                        className={`text-gray-400 flex-shrink-0 transition-transform duration-200 ${isExpanded ? "" : "-rotate-90"}`}
                      />
                    ) : (
                      <Lock size={11} className="text-gray-300 flex-shrink-0" />
                    )}
                    <div className="min-w-0 flex-1">
                      <div className="text-[13px] font-semibold text-gray-700 truncate leading-none">{meta.label}</div>
                      {isUnlocked && lessons.length > 0 && (
                        <div className="flex items-center gap-2 mt-1.5">
                          <div className="flex-1 h-[3px] bg-gray-100 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-brand-400 rounded-full transition-all"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                          <span className="text-[10px] text-gray-400 tabular-nums flex-shrink-0 font-medium">
                            {doneInModule}/{lessons.length}
                          </span>
                        </div>
                      )}
                      {!isUnlocked && (
                        <div className="text-[11px] text-gray-400 mt-0.5">Locked</div>
                      )}
                    </div>
                  </button>

                  {/* Lesson list */}
                  {isUnlocked && isExpanded && lessons.length > 0 && (
                    <div className="pb-1.5">
                      {lessons.map((lesson) => {
                        const isActive = lesson.id === activeId;
                        const isDone = completedIds.has(lesson.id);
                        return (
                          <button
                            key={lesson.id}
                            onClick={() => handleSelectConcept(lesson.id, modId)}
                            className={`w-full px-3 py-2 text-left flex items-center gap-2.5 transition-colors ${
                              isActive
                                ? "bg-brand-50 text-brand-700"
                                : "hover:bg-gray-50 text-gray-500"
                            }`}
                          >
                            <span className={`w-[18px] h-[18px] flex-shrink-0 rounded-full border flex items-center justify-center text-[9px] font-mono font-semibold ${
                              isDone
                                ? "bg-green-500 border-green-500 text-white"
                                : isActive
                                ? "border-brand-400 text-brand-500 bg-white"
                                : "border-gray-200 text-gray-400"
                            }`}>
                              {isDone ? "✓" : lesson.lessonNumber}
                            </span>
                            <span className="text-[13px] truncate font-medium">{lesson.title}</span>
                            {lesson.lessonType === "flagship" && (
                              <span className="ml-auto text-amber-400 flex-shrink-0 text-[11px]">★</span>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  )}

                  {/* Coming soon placeholder for locked modules */}
                  {!isUnlocked && (
                    <div className="px-3 pb-2.5 text-[11px] text-gray-400 leading-relaxed">
                      {meta.description}
                    </div>
                  )}
                </div>
              );
            })}
          </aside>

          {/* ── Main panel ── */}
          <div className="flex-1 min-w-0 space-y-5">

            {/* Concept card */}
            <div
              key={activeId}
              className="bg-white border border-gray-200 rounded-xl overflow-hidden"
              style={{ boxShadow: "0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)" }}
            >
              {/* Card header */}
              <div className="px-8 pt-7 pb-0">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-[11px] font-semibold text-brand-600 bg-brand-50 px-2.5 py-1 rounded-md uppercase tracking-wide">
                        {MODULE_META[concept.module].label}
                      </span>
                      <span className="text-[12px] text-gray-400 font-medium">Lesson {concept.lessonNumber}</span>
                      {concept.lessonType === "flagship" && (
                        <span className="text-[11px] font-semibold text-amber-500 bg-amber-50 px-2 py-0.5 rounded-md">★ Flagship</span>
                      )}
                    </div>
                    <h2 className="text-[20px] font-bold text-gray-900 leading-snug">{concept.title}</h2>
                    <p className="text-[14px] text-gray-500 mt-1 leading-relaxed">{concept.tagline}</p>
                  </div>
                  {completedIds.has(activeId) && (
                    <div className="flex-shrink-0 text-xs font-semibold text-green-600 bg-green-50 border border-green-100 px-3 py-1.5 rounded-lg">
                      ✓ Completed
                    </div>
                  )}
                </div>

                {/* Mental model callout */}
                <div className="mt-5 px-4 py-3 bg-brand-50 border border-brand-100 rounded-lg">
                  <div className="flex items-start gap-2.5">
                    <BookOpen size={13} className="text-brand-400 flex-shrink-0 mt-0.5" />
                    <p className="text-[13px] text-brand-700 leading-relaxed">{concept.mentalModel}</p>
                  </div>
                </div>
              </div>

              {/* Visualizer */}
              <div className="px-8 pt-5 pb-8">
                <InteractiveConceptVisualizer
                  key={activeId}
                  concept={concept}
                  onComplete={handleConceptComplete}
                />
              </div>

              {/* Why it matters */}
              <div className="mx-8 mb-6 px-4 py-3.5 bg-gray-50 rounded-lg border border-gray-100">
                <div className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-1.5">Why it matters</div>
                <p className="text-[13px] text-gray-600 leading-relaxed">{concept.whyItMatters}</p>
              </div>

              {/* Common mistakes */}
              {concept.commonMistakes.length > 0 && (
                <div className="mx-8 mb-6 space-y-2">
                  <div className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest">Common mistakes</div>
                  {concept.commonMistakes.map((m, i) => (
                    <div key={i} className="flex items-start gap-2.5 text-[13px] text-gray-600">
                      <span className="text-red-400 flex-shrink-0 mt-0.5 font-semibold">✗</span>
                      <span>{m}</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Pattern connection */}
              {concept.patternConnection && (
                <div className="mx-8 mb-7 px-4 py-3.5 bg-violet-50 border border-violet-100 rounded-lg">
                  <div className="text-[11px] font-semibold text-violet-500 uppercase tracking-widest mb-1.5">What comes next</div>
                  <p className="text-[13px] text-violet-700 leading-relaxed">{concept.patternConnection}</p>
                </div>
              )}
            </div>

            {/* Completion panel */}
            {viewMode === "completion" && (
              <ConceptCompletionPanel
                concept={concept}
                hasPracticeSlug={HAS_PRACTICE.has(concept.id)}
                onPractice={() => setViewMode("practice")}
                onSkip={() => {
                  if (nextConcept) {
                    setActiveId(nextConcept.id);
                    setViewMode("learn");
                    setExpandedModules((prev) => new Set([...prev, nextConcept.module]));
                  } else {
                    setViewMode("learn");
                  }
                }}
              />
            )}

            {/* Practice panel */}
            {viewMode === "practice" && (
              <ConceptPracticePanel
                key={`practice-${activeId}`}
                concept={concept}
                onMastered={handleMastered}
                onClose={() => {
                  if (nextConcept) {
                    setActiveId(nextConcept.id);
                    setViewMode("learn");
                  } else {
                    setViewMode("learn");
                  }
                }}
              />
            )}

          </div>
        </div>
      </div>
    </div>
  );
}

