import type { CellState } from "@/lib/visualizer/types";

const STATE_CLASSES: Record<CellState, string> = {
  default:         "bg-white border-gray-200 text-gray-800",
  active:          "bg-brand-50 border-brand-400 text-brand-800 shadow-sm shadow-brand-100",
  comparing:       "bg-amber-50 border-amber-400 text-amber-900 shadow-sm shadow-amber-100",
  pivot:           "bg-orange-50 border-orange-400 text-orange-900 shadow-sm shadow-orange-100",
  found:           "bg-green-50 border-green-400 text-green-800 shadow-sm shadow-green-100",
  eliminated:      "bg-gray-50 border-gray-200 text-gray-300",
  sorted:          "bg-green-50 border-green-300 text-green-700",
  "window-start":  "bg-brand-50 border-brand-500 text-brand-800 shadow-sm shadow-brand-100",
  "window-end":    "bg-brand-50 border-brand-500 text-brand-800 shadow-sm shadow-brand-100",
  "in-window":     "bg-brand-50/60 border-brand-300 text-brand-700",
  "left-pointer":  "bg-blue-50 border-blue-400 text-blue-900 shadow-sm shadow-blue-100",
  "right-pointer": "bg-violet-50 border-violet-400 text-violet-900 shadow-sm shadow-violet-100",
  "min-tracked":   "bg-indigo-50 border-indigo-400 text-indigo-900 shadow-sm shadow-indigo-100",
  "max-tracked":   "bg-emerald-50 border-emerald-400 text-emerald-900 shadow-sm shadow-emerald-100",
};

// Active states get a subtle scale bump
const SCALE_STATES = new Set<CellState>([
  "active", "comparing", "pivot", "found",
  "window-start", "window-end",
  "left-pointer", "right-pointer",
  "min-tracked", "max-tracked",
]);

interface ArrayCellProps {
  value: number;
  index: number;
  state: CellState;
  label?: string;
}

export default function ArrayCell({ value, index, state, label }: ArrayCellProps) {
  const stateClass = STATE_CLASSES[state] ?? STATE_CLASSES.default;
  const scale = SCALE_STATES.has(state) ? "scale-110" : "scale-100";

  return (
    <div className="flex flex-col items-center gap-1.5">
      {/* Pointer label — always reserve height so cells don't shift */}
      <div className="h-5 flex items-end justify-center">
        {label ? (
          <span className="text-[10px] font-semibold font-mono text-brand-500 leading-none tracking-wide">
            {label}
          </span>
        ) : null}
      </div>

      {/* Cell */}
      <div
        className={`
          w-14 h-14 flex items-center justify-center
          rounded-xl border-2 font-mono font-semibold text-sm
          transition-all duration-200 ease-out
          ${stateClass} ${scale}
        `}
        role="cell"
        aria-label={`index ${index}, value ${value}${label ? `, ${label}` : ""}, state: ${state}`}
      >
        {value}
      </div>

      {/* Index label */}
      <span className="text-[11px] text-gray-400 font-mono tabular-nums">{index}</span>
    </div>
  );
}
