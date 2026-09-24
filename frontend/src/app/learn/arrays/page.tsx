"use client";

import { useState, useCallback } from "react";
import { ARRAY_CONCEPTS } from "@/lib/visualizer/tracers/arrayConceptTracers";
import InteractiveConceptVisualizer from "@/components/visualizer/InteractiveConceptVisualizer";
import ConceptCompletionPanel from "@/components/learn/ConceptCompletionPanel";
import ConceptPracticePanel from "@/components/learn/ConceptPracticePanel";
import { ChevronRight } from "lucide-react";

const FLOW_STEPS = ["Understand", "Visualize", "Practice", "Mastery"];

// Concepts that have a practice problem mapped
const HAS_PRACTICE = new Set(["indexing", "traversal", "access", "update", "linear-search"]);

type ViewMode = "learn" | "completion" | "practice";

export default function LearnArraysPage() {
  const [activeId, setActiveId]   = useState(ARRAY_CONCEPTS[0].id);
  const [viewMode, setViewMode]   = useState<ViewMode>("learn");
  const concept = ARRAY_CONCEPTS.find((c) => c.id === activeId) ?? ARRAY_CONCEPTS[0];

  const handleConceptComplete = useCallback(() => {
    setViewMode("completion");
  }, []);

  const handleSwitchConcept = useCallback((id: string) => {
    setActiveId(id);
    setViewMode("learn");
  }, []);

  const handleTabClick = useCallback((id: string) => {
    if (id !== activeId) {
      setActiveId(id);
      setViewMode("learn");
    }
  }, [activeId]);

  const nextConcept = ARRAY_CONCEPTS[ARRAY_CONCEPTS.findIndex((c) => c.id === activeId) + 1] ?? null;

  const handleMastered = useCallback(() => {
    if (nextConcept) {
      handleSwitchConcept(nextConcept.id);
    } else {
      setViewMode("learn");
    }
  }, [nextConcept, handleSwitchConcept]);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-10 space-y-8">

        {/* ── Page header ── */}
        <div className="space-y-4">
          <div className="flex items-center gap-1.5 text-xs text-gray-400">
            <span>Learn</span>
            <ChevronRight size={11} />
            <span className="text-gray-600 font-medium">Arrays</span>
          </div>

          <div>
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">Arrays</h1>
            <p className="text-sm text-gray-500 mt-1 max-w-lg">
              The foundational data structure. Master indexing, traversal, search, and in-place
              updates before tackling linked lists or hash maps.
            </p>
          </div>

          {/* Flow indicator */}
          <div className="flex items-center gap-1 text-xs text-gray-400 select-none">
            {FLOW_STEPS.map((step, i) => {
              const isActive =
                (step === "Understand" && viewMode === "learn") ||
                (step === "Visualize" && viewMode === "learn") ||
                (step === "Practice" && viewMode === "practice") ||
                (step === "Mastery" && viewMode === "practice");
              return (
                <span key={step} className="flex items-center gap-1">
                  {i > 0 && <ChevronRight size={10} className="text-gray-300" />}
                  <span className={`font-medium ${isActive ? "text-brand-600" : ""}`}>{step}</span>
                </span>
              );
            })}
          </div>
        </div>

        {/* ── Concept tabs ── */}
        <div className="flex flex-wrap gap-2" role="tablist" aria-label="Array concepts">
          {ARRAY_CONCEPTS.map((c) => (
            <button
              key={c.id}
              role="tab"
              aria-selected={activeId === c.id}
              onClick={() => handleTabClick(c.id)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all border ${
                activeId === c.id
                  ? "bg-brand-600 text-white border-brand-600 shadow-sm"
                  : "bg-white text-gray-500 border-gray-200 hover:border-brand-200 hover:text-brand-600"
              }`}
            >
              {c.title}
            </button>
          ))}
        </div>

        {/* ── Active concept card ── */}
        <div
          key={activeId}
          className="bg-white border border-gray-200 rounded-2xl shadow-sm overflow-hidden"
          role="tabpanel"
        >
          {/* Card header */}
          <div className="px-7 pt-6 pb-0">
            <h2 className="text-lg font-bold text-gray-900">{concept.title}</h2>
            <p className="text-sm text-gray-400 mt-0.5">{concept.tagline}</p>
          </div>

          {/* Card body */}
          <div className="px-7 pt-4 pb-7">
            <InteractiveConceptVisualizer
              key={activeId}
              concept={concept}
              onComplete={handleConceptComplete}
            />
          </div>
        </div>

        {/* ── Completion panel (shown after visualization finishes) ── */}
        {viewMode === "completion" && (
          <ConceptCompletionPanel
            concept={concept}
            hasPracticeSlug={HAS_PRACTICE.has(concept.id)}
            onPractice={() => setViewMode("practice")}
            onSkip={() => {
              if (nextConcept) handleSwitchConcept(nextConcept.id);
              else setViewMode("learn");
            }}
          />
        )}

        {/* ── Practice panel ── */}
        {viewMode === "practice" && (
          <ConceptPracticePanel
            key={`practice-${activeId}`}
            concept={concept}
            onMastered={handleMastered}
            onClose={() => {
              if (nextConcept) handleSwitchConcept(nextConcept.id);
              else setViewMode("learn");
            }}
          />
        )}

      </div>
    </div>
  );
}
