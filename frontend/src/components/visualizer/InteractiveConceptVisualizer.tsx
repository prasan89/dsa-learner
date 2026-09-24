"use client";

import { useState, useMemo, useCallback } from "react";
import { Eye, Swords, Code2 } from "lucide-react";
import { useInteractiveVisualizer } from "@/lib/visualizer/useInteractiveVisualizer";
import { CHALLENGE_GENERATORS } from "@/lib/visualizer/tracers/challengeTracers";
import { CODE_BINDINGS, traceLinearSearchWithCode } from "@/lib/visualizer/codeBinding";
import ArrayDisplay from "./ArrayDisplay";
import VisualizerControls from "./VisualizerControls";
import ChallengeOverlay from "./ChallengeOverlay";
import ScoreTracker from "./ScoreTracker";
import ConceptVisualizer from "./ConceptVisualizer";
import LinkedCodePanel from "./LinkedCodePanel";
import type { ArrayConcept } from "@/lib/visualizer/tracers/arrayConceptTracers";

const CODE_TRACERS: Record<string, (input: { array: number[]; target?: number }) => ReturnType<typeof traceLinearSearchWithCode>> = {
  "linear-search": traceLinearSearchWithCode,
};
void CODE_TRACERS;

function parseArray(raw: string): number[] | null {
  try {
    const cleaned = raw.replace(/[\[\]]/g, "").trim();
    if (!cleaned) return [];
    const nums = cleaned.split(",").map((s) => Number(s.trim()));
    return nums.some(isNaN) ? null : nums;
  } catch {
    return null;
  }
}

// ─── Variable pill ────────────────────────────────────────────────────────────
function VarPill({ k, v }: { k: string; v: number | string | boolean }) {
  return (
    <span className="inline-flex items-center gap-1 text-xs font-mono bg-white border border-gray-200 rounded-md px-2 py-1">
      <span className="text-brand-600 font-semibold">{k}</span>
      <span className="text-gray-300">=</span>
      <span className="text-gray-700">{String(v)}</span>
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
            className={`flex items-start gap-2.5 px-3 py-2 rounded-lg text-sm transition-all duration-150 ${
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
interface InteractiveConceptVisualizerProps {
  concept: ArrayConcept;
}

export default function InteractiveConceptVisualizer({ concept }: InteractiveConceptVisualizerProps) {
  const [mode, setMode]         = useState<"watch" | "challenge">("watch");
  const [showCode, setShowCode] = useState(false);
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

  const challengeSteps = useMemo(() => {
    if (!parsedArray || parsedArray.length === 0) return [];
    const gen = CHALLENGE_GENERATORS[concept.id];
    if (!gen) return [];
    try { return gen({ array: parsedArray, target: parsedTarget }); }
    catch { return []; }
  }, [parsedArray, parsedTarget, concept]);

  const viz = useInteractiveVisualizer(challengeSteps, 1);

  const activeAlgorithmStep = useMemo(() => {
    if (viz.totalSteps === 0 || concept.algorithmSteps.length === 0) return null;
    const n = concept.algorithmSteps.length;
    return Math.min(Math.floor(viz.currentStepIndex / (viz.totalSteps / n)), n - 1);
  }, [viz.currentStepIndex, viz.totalSteps, concept.algorithmSteps.length]);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => setRawInput(e.target.value), []
  );

  const cells = viz.currentStep?.cells ??
    (parsedArray ? parsedArray.map((v, i) => ({ value: v, index: i, state: "default" as const })) : []);

  const showTarget = concept.showTargetInput === "index" || concept.showTargetInput === "value";
  const hasChallengeGen = Boolean(CHALLENGE_GENERATORS[concept.id]);
  const codeBinding = CODE_BINDINGS[concept.id] ?? null;
  const hasVars = viz.currentStep && Object.keys(viz.currentStep.variables).length > 0;

  // ── Watch mode: delegate to ConceptVisualizer ──────────────────────────────
  if (mode === "watch") {
    return (
      <div className="space-y-0 divide-y divide-gray-100">
        {hasChallengeGen && (
          <div className="pb-4 flex items-center justify-end">
            <button
              onClick={() => setMode("challenge")}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 bg-white text-gray-500 text-xs font-semibold hover:border-brand-300 hover:text-brand-600 hover:bg-brand-50 transition-colors"
            >
              <Swords size={12} />
              Challenge mode
            </button>
          </div>
        )}
        <ConceptVisualizer concept={concept} />
      </div>
    );
  }

  // ── Challenge mode ─────────────────────────────────────────────────────────
  return (
    <div className="space-y-0 divide-y divide-gray-100">

      {/* Mode bar */}
      <div className="pb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setMode("watch")}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 bg-white text-gray-500 text-xs font-semibold hover:bg-gray-50 transition-colors"
          >
            <Eye size={12} />
            Watch mode
          </button>
          {codeBinding && (
            <button
              onClick={() => setShowCode((v) => !v)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                showCode
                  ? "bg-gray-900 border-gray-800 text-gray-100"
                  : "bg-white border-gray-200 text-gray-500 hover:border-gray-300 hover:text-gray-700"
              }`}
            >
              <Code2 size={12} />
              {showCode ? "Hide code" : "Show code"}
            </button>
          )}
        </div>
        <ScoreTracker score={viz.score} />
      </div>

      {/* Inputs */}
      <div className="py-5">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1">
            <label className="block text-xs font-medium text-gray-400 mb-1.5">Array</label>
            <input
              type="text"
              value={rawInput}
              onChange={handleInputChange}
              className={`w-full px-3 py-2 rounded-lg border text-sm font-mono focus:outline-none focus:ring-2 focus:ring-brand-400/30 transition-colors ${
                parseError ? "border-red-300 bg-red-50" : "border-gray-200 bg-white hover:border-gray-300 focus:border-brand-300"
              }`}
              placeholder="e.g. 10, 25, 31, 42"
              aria-label="Array values"
            />
            {parseError && <p className="text-xs text-red-500 mt-1">{parseError}</p>}
          </div>
          {showTarget && (
            <div className="w-32">
              <label className="block text-xs font-medium text-gray-400 mb-1.5">{concept.targetLabel}</label>
              <input
                type="number"
                value={rawTarget}
                onChange={(e) => setRawTarget(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-gray-200 bg-white text-sm font-mono focus:outline-none focus:ring-2 focus:ring-brand-400/30 hover:border-gray-300 focus:border-brand-300 transition-colors"
                placeholder={concept.showTargetInput === "index" ? "index" : "value"}
                aria-label={concept.targetLabel}
              />
            </div>
          )}
        </div>
      </div>

      {/* Visualization — primary focus */}
      <div className="py-6">
        <div className="overflow-x-auto">
          {cells.length > 0 ? (
            <ArrayDisplay cells={cells} />
          ) : (
            <div className="flex items-center justify-center h-28 text-sm text-gray-400">
              Enter an array above to begin.
            </div>
          )}
        </div>
      </div>

      {/* Controls */}
      <div className="py-4">
        <VisualizerControls
          status={viz.activeChallenge ? "paused" : viz.status}
          currentStep={viz.currentStepIndex}
          totalSteps={viz.totalSteps}
          speed={viz.speed}
          onPlay={viz.play}
          onPause={viz.pause}
          onStepBack={viz.stepBack}
          onStepForward={viz.stepForward}
          onReset={viz.reset}
          onSpeedChange={viz.setSpeed}
        />
      </div>

      {/* Challenge or narration */}
      <div className="py-5">
        {viz.activeChallenge ? (
          <ChallengeOverlay
            challenge={viz.activeChallenge}
            answerState={viz.answerState}
            onSubmit={viz.submitAnswer}
            onContinue={viz.dismissChallenge}
          />
        ) : (
          <div aria-live="polite" aria-atomic="true" className="space-y-3">
            {viz.currentStep ? (
              <>
                <p className="text-sm text-gray-800 leading-relaxed">{viz.currentStep.message}</p>
                {hasVars && (
                  <div className="flex flex-wrap gap-1.5">
                    {Object.entries(viz.currentStep.variables).map(([k, v]) => (
                      <VarPill key={k} k={k} v={v} />
                    ))}
                  </div>
                )}
              </>
            ) : (
              <p className="text-sm text-gray-400">
                Press <kbd className="kbd">Space</kbd> or{" "}
                <kbd className="kbd">→</kbd> to begin. Challenges will appear as you step through.
              </p>
            )}
          </div>
        )}
      </div>

      {/* Java code panel */}
      {showCode && codeBinding && (
        <div className="py-5 space-y-2">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest">Java source</p>
          <LinkedCodePanel
            source={codeBinding.source}
            language={codeBinding.language}
            highlightLine={viz.currentStep?.highlightLine}
          />
        </div>
      )}

      {/* Algorithm steps + Complexity */}
      <div className="py-5 grid grid-cols-1 lg:grid-cols-[1fr_auto] gap-6">
        {concept.algorithmSteps.length > 0 && (
          <div className="space-y-2">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest">Steps</p>
            <AlgorithmSteps steps={concept.algorithmSteps} activeStep={activeAlgorithmStep} />
          </div>
        )}
        <div className="space-y-2 lg:w-48">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest">Complexity</p>
          <div className="flex lg:flex-col gap-2">
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-green-50 border border-green-100 text-green-800 text-sm">
              <span className="text-xs opacity-60">Time</span>
              <span className="font-mono font-bold">{concept.timeComplexity}</span>
            </div>
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-blue-50 border border-blue-100 text-blue-800 text-sm">
              <span className="text-xs opacity-60">Space</span>
              <span className="font-mono font-bold">{concept.spaceComplexity}</span>
            </div>
          </div>
          <p className="text-xs text-gray-400 leading-relaxed">{concept.complexityNote}</p>
        </div>
      </div>

    </div>
  );
}
