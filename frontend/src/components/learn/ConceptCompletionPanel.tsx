"use client";

import { CheckCircle2, ChevronRight, Trophy } from "lucide-react";
import type { ArrayConcept } from "@/lib/visualizer/tracers/arrayConceptTracers";

interface ConceptCompletionPanelProps {
  concept: ArrayConcept;
  hasPracticeSlug: boolean;
  onPractice: () => void;
  onSkip: () => void;
}

export default function ConceptCompletionPanel({
  concept,
  hasPracticeSlug,
  onPractice,
  onSkip,
}: ConceptCompletionPanelProps) {
  return (
    <div className="mt-6 rounded-2xl border border-brand-200 bg-gradient-to-br from-brand-50 to-white p-6 space-y-5">
      {/* Header */}
      <div className="flex items-start gap-3">
        <div className="shrink-0 w-9 h-9 rounded-full bg-brand-600 flex items-center justify-center">
          <Trophy size={18} className="text-white" />
        </div>
        <div>
          <p className="text-xs font-semibold text-brand-600 uppercase tracking-widest">Concept complete</p>
          <h3 className="text-base font-bold text-gray-900 mt-0.5">{concept.title}</h3>
          <p className="text-sm text-gray-500 mt-1">{concept.tagline}</p>
        </div>
      </div>

      {/* What you learned */}
      <div className="space-y-2">
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest">What you learned</p>
        <ul className="space-y-1.5">
          {concept.algorithmSteps.map((step, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
              <CheckCircle2 size={14} className="shrink-0 mt-0.5 text-green-500" />
              <span>{step}</span>
            </li>
          ))}
          <li className="flex items-start gap-2 text-sm text-gray-700">
            <CheckCircle2 size={14} className="shrink-0 mt-0.5 text-green-500" />
            <span>
              Time complexity: <span className="font-mono font-semibold text-brand-700">{concept.timeComplexity}</span> —{" "}
              {concept.complexityNote}
            </span>
          </li>
        </ul>
      </div>

      {/* CTA */}
      {hasPracticeSlug ? (
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 pt-1">
          <button
            onClick={onPractice}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-600 text-white text-sm font-semibold hover:bg-brand-700 transition-colors shadow-sm"
          >
            Practice this concept
            <ChevronRight size={14} />
          </button>
          <button
            onClick={onSkip}
            className="text-sm text-gray-400 hover:text-gray-600 transition-colors"
          >
            Skip practice for now
          </button>
        </div>
      ) : (
        <div className="pt-1">
          <button
            onClick={onSkip}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-600 text-white text-sm font-semibold hover:bg-brand-700 transition-colors shadow-sm"
          >
            Continue to next concept
            <ChevronRight size={14} />
          </button>
        </div>
      )}
    </div>
  );
}
