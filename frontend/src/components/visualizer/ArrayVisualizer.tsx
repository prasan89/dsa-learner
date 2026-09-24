"use client";

import { useState, useMemo, useCallback } from "react";
import { ARRAY_TRACERS, DEFAULT_TRACER } from "@/lib/visualizer/tracers/arrayTracers";
import { useVisualizer } from "@/lib/visualizer/useVisualizer";
import ArrayDisplay from "./ArrayDisplay";
import VisualizerControls from "./VisualizerControls";
import VisualizerMessage from "./VisualizerMessage";

// ─── Colour legend ────────────────────────────────────────────────────────────
const LEGEND = [
  { label: "Active",     className: "bg-brand-100 border-brand-500"  },
  { label: "Comparing",  className: "bg-yellow-100 border-yellow-500" },
  { label: "Found",      className: "bg-green-100 border-green-500"   },
  { label: "Eliminated", className: "bg-gray-100 border-gray-200"     },
  { label: "Window",     className: "bg-brand-50 border-brand-300"    },
];

// ─── Input parsing ────────────────────────────────────────────────────────────
function parseArray(raw: string): number[] | null {
  try {
    const cleaned = raw.replace(/[\[\]]/g, "").trim();
    if (cleaned === "") return [];
    const parts = cleaned.split(",").map((s) => s.trim());
    const nums = parts.map(Number);
    if (nums.some(isNaN)) return null;
    return nums;
  } catch {
    return null;
  }
}

// ─── Props ────────────────────────────────────────────────────────────────────
export interface ArrayVisualizerProps {
  algorithmSlug: string;
  defaultInput?: number[];
  target?: number;
}

// ─── Component ────────────────────────────────────────────────────────────────
export default function ArrayVisualizer({
  algorithmSlug,
  defaultInput = [10, 20, 30, 40, 50],
  target,
}: ArrayVisualizerProps) {
  const [rawInput, setRawInput] = useState(defaultInput.join(", "));
  const [rawTarget, setRawTarget] = useState(target !== undefined ? String(target) : "");
  const [parseError, setParseError] = useState("");

  // Derive parsed array — memo so it only recalculates when rawInput changes
  const parsedArray = useMemo(() => {
    const result = parseArray(rawInput);
    setParseError(result === null ? "Invalid input — enter numbers separated by commas." : "");
    return result;
  }, [rawInput]);

  const parsedTarget = useMemo(() => {
    if (rawTarget === "") return undefined;
    const n = Number(rawTarget);
    return isNaN(n) ? undefined : n;
  }, [rawTarget]);

  // Compute steps from tracer
  const steps = useMemo(() => {
    if (!parsedArray || parsedArray.length === 0) return [];
    const tracer = ARRAY_TRACERS[algorithmSlug] ?? DEFAULT_TRACER;
    try {
      return tracer({ array: parsedArray, target: parsedTarget });
    } catch {
      return [];
    }
  }, [parsedArray, parsedTarget, algorithmSlug]);

  const {
    currentStep,
    currentStepIndex,
    totalSteps,
    status,
    speed,
    play,
    pause,
    stepForward,
    stepBack,
    reset,
    setSpeed,
  } = useVisualizer(steps, 2);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setRawInput(e.target.value);
    },
    []
  );

  const hasTracer = algorithmSlug in ARRAY_TRACERS;

  return (
    <div className="space-y-4">
      {/* Input section */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <label className="block text-xs font-medium text-gray-500 mb-1">
            Array (comma-separated)
          </label>
          <input
            type="text"
            value={rawInput}
            onChange={handleInputChange}
            className={`w-full px-3 py-2 rounded-lg border text-sm font-mono focus:outline-none focus:ring-2 focus:ring-brand-500/30 transition-colors ${
              parseError
                ? "border-red-300 bg-red-50"
                : "border-gray-200 bg-white hover:border-gray-300"
            }`}
            placeholder="e.g. 10, 20, 30, 40, 50"
            aria-label="Array input"
            aria-invalid={Boolean(parseError)}
          />
          {parseError && (
            <p className="text-xs text-red-600 mt-1">{parseError}</p>
          )}
        </div>

        {/* Target input — only shown for algorithms that use one */}
        {(algorithmSlug.includes("binary-search") ||
          algorithmSlug.includes("two-sum") ||
          algorithmSlug.includes("linear-scan") ||
          algorithmSlug.includes("two-pointer")) && (
          <div className="w-32">
            <label className="block text-xs font-medium text-gray-500 mb-1">Target</label>
            <input
              type="number"
              value={rawTarget}
              onChange={(e) => setRawTarget(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-gray-200 bg-white text-sm font-mono focus:outline-none focus:ring-2 focus:ring-brand-500/30 hover:border-gray-300 transition-colors"
              placeholder="e.g. 30"
              aria-label="Target value"
            />
          </div>
        )}
      </div>

      {/* No-tracer notice */}
      {!hasTracer && (
        <div className="rounded-xl bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
          No algorithm visualization available for this problem. Showing generic linear scan.
        </div>
      )}

      {/* Array display */}
      <div className="border border-gray-200 rounded-xl bg-white min-h-[100px] overflow-x-auto">
        {currentStep ? (
          <ArrayDisplay cells={currentStep.cells} />
        ) : parsedArray && parsedArray.length > 0 ? (
          <ArrayDisplay
            cells={parsedArray.map((v, i) => ({
              value: v,
              index: i,
              state: "default" as const,
            }))}
          />
        ) : (
          <div className="flex items-center justify-center h-24 text-sm text-gray-400">
            Enter an array above to begin.
          </div>
        )}
      </div>

      {/* Step message */}
      {currentStep && (
        <VisualizerMessage
          message={currentStep.message}
          variables={currentStep.variables}
          currentStep={currentStepIndex}
          totalSteps={totalSteps}
        />
      )}

      {/* Playback controls */}
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

      {/* Legend */}
      <div className="flex flex-wrap gap-2 pt-1">
        {LEGEND.map(({ label, className }) => (
          <span
            key={label}
            className={`inline-flex items-center gap-1.5 text-xs px-2 py-1 rounded border ${className}`}
          >
            <span className={`w-2.5 h-2.5 rounded-sm border ${className}`} />
            {label}
          </span>
        ))}
      </div>
    </div>
  );
}
