"use client";

import { useState, useMemo, useCallback, useEffect } from "react";
import { useVisualizer } from "@/lib/visualizer/useVisualizer";
import ArrayDisplay from "./ArrayDisplay";
import VisualizerControls from "./VisualizerControls";
import type { ArrayConcept } from "@/lib/visualizer/tracers/arrayConceptTracers";

function parseArray(raw: string): number[] | null {
  try {
    const cleaned = raw.replace(/[\[\]]/g, "").trim();
    if (cleaned === "") return [];
    const nums = cleaned.split(",").map((s) => Number(s.trim()));
    return nums.some(isNaN) ? null : nums;
  } catch {
    return null;
  }
}

// ─── Variable pill ────────────────────────────────────────────────────────────
function VarPill({ k, v }: { k: string; v: number | string | boolean }) {
  return (
    <span className="inline-flex items-center gap-1 text-xs font-mono bg-gray-50 border border-gray-200 rounded-md px-2.5 py-1">
      <span className="text-brand-600 font-semibold">{k}</span>
      <span className="text-gray-300">=</span>
      <span className="text-gray-700 font-medium">{String(v)}</span>
    </span>
  );
}

// ─── Algorithm step list ──────────────────────────────────────────────────────
function AlgorithmSteps({ steps, activeStep }: { steps: string[]; activeStep: number | null }) {
  return (
    <ol className="space-y-1">
      {steps.map((s, i) => {
        const isActive = activeStep === i;
        return (
          <li
            key={i}
            className={`flex items-start gap-2.5 px-3 py-2 rounded-lg text-[13px] transition-all duration-150 ${
              isActive ? "bg-brand-50 text-brand-800" : "text-gray-400"
            }`}
          >
            <span
              className={`shrink-0 mt-0.5 w-4 h-4 rounded-full flex items-center justify-center text-[10px] font-bold transition-colors ${
                isActive ? "bg-brand-600 text-white" : "bg-gray-100 text-gray-400"
              }`}
            >
              {i + 1}
            </span>
            <span className="leading-snug">{s}</span>
          </li>
        );
      })}
    </ol>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────
export default function ConceptVisualizer({ concept, onComplete }: { concept: ArrayConcept; onComplete?: () => void }) {
  const [rawInput, setRawInput] = useState(concept.defaultArray.join(", "));
  const [rawTarget, setRawTarget] = useState(
    concept.defaultTarget !== undefined ? String(concept.defaultTarget) : ""
  );
  const [parseError, setParseError] = useState("");

  const parsedArray = useMemo(() => {
    const r = parseArray(rawInput);
    setParseError(r === null ? "Enter numbers separated by commas." : "");
    return r;
  }, [rawInput]);

  const parsedTarget = useMemo(() => {
    if (concept.showTargetInput === "none" || rawTarget === "") return undefined;
    const n = Number(rawTarget);
    return isNaN(n) ? undefined : n;
  }, [rawTarget, concept.showTargetInput]);

  const steps = useMemo(() => {
    if (!parsedArray || parsedArray.length === 0) return [];
    try { return concept.tracer({ array: parsedArray, target: parsedTarget }); }
    catch { return []; }
  }, [parsedArray, parsedTarget, concept]);

  const {
    currentStep, currentStepIndex, totalSteps, status, speed,
    play, pause, stepForward, stepBack, reset, setSpeed,
  } = useVisualizer(steps, 1);

  useEffect(() => {
    if (status === "done" && onComplete) onComplete();
  }, [status, onComplete]);

  const activeAlgorithmStep = useMemo(() => {
    if (totalSteps === 0 || concept.algorithmSteps.length === 0) return null;
    const n = concept.algorithmSteps.length;
    return Math.min(Math.floor(currentStepIndex / (totalSteps / n)), n - 1);
  }, [currentStepIndex, totalSteps, concept.algorithmSteps.length]);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => setRawInput(e.target.value), []
  );

  const cells = currentStep?.cells ??
    (parsedArray ? parsedArray.map((v, i) => ({ value: v, index: i, state: "default" as const })) : []);

  const showTarget = concept.showTargetInput === "index" || concept.showTargetInput === "value";
  const hasVars = currentStep && Object.keys(currentStep.variables).length > 0;

  return (
    <div className="space-y-0 divide-y divide-gray-100">

      {/* ── Explanation ── */}
      <div className="pb-6">
        <p className="text-[14px] text-gray-600 leading-relaxed">{concept.explanation}</p>
      </div>

      {/* ── Inputs ── */}
      <div className="py-5">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1">
            <label className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wide mb-1.5">Array</label>
            <input
              type="text"
              value={rawInput}
              onChange={handleInputChange}
              className={`w-full px-3 h-10 rounded-lg border text-[13px] font-mono focus:outline-none focus:ring-2 focus:ring-brand-400/30 transition-colors ${
                parseError ? "border-red-300 bg-red-50" : "border-gray-200 bg-white hover:border-gray-300 focus:border-brand-400"
              }`}
              placeholder="e.g. 10, 25, 31, 42"
              aria-label="Array values"
            />
            {parseError && <p className="text-xs text-red-500 mt-1">{parseError}</p>}
          </div>
          {showTarget && (
            <div className="w-32">
              <label className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wide mb-1.5">{concept.targetLabel}</label>
              <input
                type="number"
                value={rawTarget}
                onChange={(e) => setRawTarget(e.target.value)}
                className="w-full px-3 h-10 rounded-lg border border-gray-200 bg-white text-[13px] font-mono focus:outline-none focus:ring-2 focus:ring-brand-400/30 hover:border-gray-300 focus:border-brand-400 transition-colors"
                placeholder={concept.showTargetInput === "index" ? "index" : "value"}
                aria-label={concept.targetLabel}
              />
            </div>
          )}
        </div>
      </div>

      {/* ── Visualization — the primary focus ── */}
      <div className="py-6">
        <div className="overflow-x-auto">
          {cells.length > 0 ? (
            <ArrayDisplay cells={cells} />
          ) : (
            <div className="flex items-center justify-center h-32 text-sm text-gray-400">
              Enter an array above to begin.
            </div>
          )}
        </div>
      </div>

      {/* ── Controls ── */}
      <div className="py-5">
        <VisualizerControls
          status={status}
          currentStep={currentStepIndex}
          totalSteps={totalSteps}
          speed={speed}
          onPlay={play}
          onPause={pause}
          onStepBack={stepBack}
          onStepForward={stepForward}
          onReset={reset}
          onSpeedChange={setSpeed}
        />
      </div>

      {/* ── Step narration ── */}
      <div className="py-5 space-y-3">
        <div aria-live="polite" aria-atomic="true">
          {currentStep ? (
            <>
              <p className="text-[14px] text-gray-800 leading-relaxed">{currentStep.message}</p>
              {hasVars && (
                <div className="flex flex-wrap gap-1.5 mt-3">
                  {Object.entries(currentStep.variables).map(([k, v]) => (
                    <VarPill key={k} k={k} v={v} />
                  ))}
                </div>
              )}
            </>
          ) : (
            <p className="text-[13px] text-gray-400">
              Press <kbd className="kbd">Space</kbd> or{" "}
              <kbd className="kbd">→</kbd> to step through the algorithm.
            </p>
          )}
        </div>
      </div>

      {/* ── Algorithm steps + Complexity side by side on wide screens ── */}
      <div className="py-5 grid grid-cols-1 lg:grid-cols-[1fr_auto] gap-6">
        {/* Algorithm steps */}
        {concept.algorithmSteps.length > 0 && (
          <div className="space-y-2">
            <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest">Steps</p>
            <AlgorithmSteps steps={concept.algorithmSteps} activeStep={activeAlgorithmStep} />
          </div>
        )}

        {/* Complexity */}
        <div className="space-y-2 lg:w-48">
          <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest">Complexity</p>
          <div className="flex lg:flex-col gap-2">
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg bg-green-50 border border-green-100 text-green-800 text-[13px]">
              <span className="text-[11px] text-green-600 font-medium opacity-80">Time</span>
              <span className="font-mono font-bold">{concept.timeComplexity}</span>
            </div>
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg bg-indigo-50 border border-indigo-100 text-indigo-800 text-[13px]">
              <span className="text-[11px] text-indigo-600 font-medium opacity-80">Space</span>
              <span className="font-mono font-bold">{concept.spaceComplexity}</span>
            </div>
          </div>
          <p className="text-[12px] text-gray-400 leading-relaxed">{concept.complexityNote}</p>
        </div>
      </div>

    </div>
  );
}
