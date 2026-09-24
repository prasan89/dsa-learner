// ─── Cell / Node state ───────────────────────────────────────────────────────
// Extensible: add new states here as new data structures are added.
export type CellState =
  | "default"          // untouched
  | "active"           // currently being examined
  | "comparing"        // in a comparison operation
  | "pivot"            // pivot element (sorting)
  | "found"            // target located
  | "eliminated"       // ruled out / not in search space
  | "sorted"           // permanently placed (sorting)
  | "window-start"     // start of a sliding window
  | "window-end"       // end of a sliding window
  | "in-window"        // inside the current window
  | "left-pointer"     // left two-pointer position
  | "right-pointer"    // right two-pointer position
  | "min-tracked"      // tracking a minimum value
  | "max-tracked"      // tracking a maximum value
  // ── Batch 1 additions ──────────────────────────────────────
  | "prefix-sum"       // prefix-sum cell (cumulative sum)
  | "write-ptr"        // write-pointer position (in-place compaction)
  | "binary-low"       // binary search left boundary
  | "binary-high"      // binary search right boundary
  | "binary-mid"       // binary search midpoint
  | "swap-a"           // first element in a swap pair
  | "swap-b"           // second element in a swap pair
  | "partition-lt"     // less-than partition zone
  | "partition-gt"     // greater-than partition zone
  | "matrix-cell";     // matrix element (2-D context)

// ─── A single element in a visualization ────────────────────────────────────
export interface VisualizationCell {
  value: number;
  index: number;
  state: CellState;
  label?: string;   // shown above cell, e.g. "left", "right", "mid"
}

// ─── Named pointers (variable names → array index) ───────────────────────────
// e.g. { left: 0, right: 4, mid: 2 }
export type PointerMap = Record<string, number>;

// ─── Named scalar variables ──────────────────────────────────────────────────
// e.g. { maxProfit: 5, minPrice: 1, windowSize: 3 }
export type VariableMap = Record<string, number | string | boolean>;

// ─── Challenge system ────────────────────────────────────────────────────────

export type ChallengeType =
  | "predict-next-index"    // what index is examined next?
  | "predict-next-value"    // what value will be read next?
  | "predict-final-result"  // what will the algorithm return?
  | "predict-complexity"    // what is the time/space complexity?
  | "identify-state";       // what state is the algorithm in right now?

export interface ChallengeChoice {
  label: string;           // display text shown to the learner
  value: string;           // canonical answer key (compared against correctAnswer)
}

export interface StepChallenge {
  type: ChallengeType;
  question: string;        // "What index will be examined next?"
  choices: ChallengeChoice[];
  correctAnswer: string;   // must match one ChallengeChoice.value
  explanation: string;     // shown after the learner answers — explains the WHY
}

// ─── A single snapshot in the animation timeline ────────────────────────────
export interface VisualizationStep {
  cells: VisualizationCell[];
  pointers: PointerMap;
  variables: VariableMap;
  message: string;          // one-sentence narration
  highlightLine?: number;   // future: code line highlight
  challenge?: StepChallenge; // present → engine pauses here for the learner
}

// ─── Engine lifecycle ────────────────────────────────────────────────────────
export type VisualizerStatus = "idle" | "playing" | "paused" | "done";

// ─── The live state emitted to React on every tick ───────────────────────────
export interface VisualizationState {
  currentStep: VisualizationStep | null;
  currentStepIndex: number;
  totalSteps: number;
  status: VisualizerStatus;
  speed: number;             // steps per second (1–10)
}

// ─── Tracer function contract ────────────────────────────────────────────────
// A tracer is a pure function: input → ordered step snapshots.
// It must not mutate its input.
export type TracerInput = {
  array: number[];
  target?: number;
  [key: string]: unknown;
};

export type TracerFn = (input: TracerInput) => VisualizationStep[];

// ─── Engine listener ─────────────────────────────────────────────────────────
export type VisualizerListener = (state: VisualizationState) => void;
