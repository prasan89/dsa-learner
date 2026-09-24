import type { VariableMap } from "@/lib/visualizer/types";

interface VisualizerMessageProps {
  message: string;
  variables?: VariableMap;
  currentStep: number;
  totalSteps: number;
}

export default function VisualizerMessage({
  message,
  variables,
  currentStep,
  totalSteps,
}: VisualizerMessageProps) {
  const hasVars = variables && Object.keys(variables).length > 0;

  return (
    <div className="border border-gray-200 rounded-xl bg-gray-50 px-4 py-3 space-y-2">
      {/* Step counter + message */}
      <div className="flex items-start gap-2">
        <span className="text-xs font-mono text-gray-400 shrink-0 mt-0.5">
          {currentStep + 1}/{totalSteps}
        </span>
        <p className="text-sm text-gray-700 font-mono leading-snug">{message}</p>
      </div>

      {/* Variables row */}
      {hasVars && (
        <div className="flex flex-wrap gap-2 pt-1 border-t border-gray-200">
          {Object.entries(variables!).map(([key, val]) => (
            <span
              key={key}
              className="inline-flex items-center gap-1 text-xs font-mono bg-white border border-gray-200 rounded px-2 py-0.5"
            >
              <span className="text-brand-600">{key}</span>
              <span className="text-gray-400">=</span>
              <span className="text-gray-800">{String(val)}</span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
