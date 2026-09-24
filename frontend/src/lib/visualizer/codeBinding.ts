/**
 * Code binding — connects a deterministic trace to a source listing.
 *
 * Design principles:
 *   - No execution engine. The tracer already knows every algorithmic event;
 *     line numbers are metadata, not derived from runtime.
 *   - One canonical source string per algorithm + a SourceMap that says which
 *     0-based line each semantic event lands on.
 *   - VisualizationStep.highlightLine (already in types.ts) carries the line
 *     number. Nothing else needs to change.
 *   - AlgorithmCode is the extension point: future algorithms add one entry to
 *     CODE_BINDINGS and a tracer that stamps highlightLine.
 *
 * Extending to actual execution tracing later:
 *   Replace the static SourceMap with a runtime-derived one (e.g. from JDWP
 *   breakpoint events) and pass it through the same AlgorithmCode.sourcemap.
 *   The LinkedCodePanel and the step interface stay untouched.
 */

import type { VisualizationStep, TracerInput } from "./types";
import { traceLinearSearch } from "./tracers/arrayConceptTracers";

// ─── Types ────────────────────────────────────────────────────────────────────

/**
 * A semantic label for an algorithmic event.
 * Concrete values live with each algorithm's binding below.
 */
export type SemanticLabel = string;

/**
 * Maps a semantic event label to the 0-based line index in the source listing.
 * The tracer references these labels so the actual line numbers live in one
 * place — change the source, change the map, tracer stays untouched.
 */
export type SourceMap = Record<SemanticLabel, number>;

/**
 * One algorithm = one source listing + one SourceMap.
 * The TracerWithCode field is the tracer that produces steps with highlightLine
 * already stamped.
 */
export interface AlgorithmCode {
  /** The canonical Java source shown to the learner. */
  source: string;
  /** Language identifier for Monaco. */
  language: "java" | "python" | "cpp";
  /** Semantic label → 0-based line index. */
  sourcemap: SourceMap;
}

// ─── Linear Search binding ────────────────────────────────────────────────────
//
// Source line index reference (0-based):
//
//  0  int linearSearch(int[] arr, int target) {
//  1      for (int i = 0; i < arr.length; i++) {
//  2          if (arr[i] == target) {
//  3              return i;
//  4          }
//  5      }
//  6      return -1;
//  7  }

const LINEAR_SEARCH_SOURCEMAP: SourceMap = {
  "fn-declaration": 0,
  "loop-init":      1,   // i = 0, condition check i < arr.length
  "loop-check":     1,   // same line — condition re-evaluated each iteration
  "comparison":     2,   // arr[i] == target
  "found":          3,   // return i
  "loop-increment": 1,   // i++ (same for-loop line)
  "not-found":      6,   // return -1
};

export const LINEAR_SEARCH_CODE: AlgorithmCode = {
  language: "java",
  sourcemap: LINEAR_SEARCH_SOURCEMAP,
  source: `int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;
        }
    }
    return -1;
}`,
};

// ─── Registry ─────────────────────────────────────────────────────────────────
// Maps concept id / algorithm slug → AlgorithmCode.
// Extend here when new algorithms get code bindings.

export const CODE_BINDINGS: Record<string, AlgorithmCode> = {
  "linear-search": LINEAR_SEARCH_CODE,
};

// ─── Annotating tracer ────────────────────────────────────────────────────────
//
// This wraps traceLinearSearch and stamps highlightLine on every step.
// The logic mirrors traceLinearSearch's step sequence exactly — we know
// the deterministic order, so we can map event kinds without re-running.
//
// Step sequence produced by traceLinearSearch:
//   step 0:          intro           → fn-declaration
//   step 1..n:       check at i      → comparison  (one per element until found)
//   step (found+1):  found result    → found
//   OR last step:    not-found       → not-found

export function traceLinearSearchWithCode(input: TracerInput): VisualizationStep[] {
  const base = traceLinearSearch(input);
  const sm = LINEAR_SEARCH_SOURCEMAP;

  return base.map((s, idx) => {
    let line: number | undefined;

    if (idx === 0) {
      // Intro step — show the function signature / loop declaration
      line = sm["loop-init"];
    } else {
      const hasResult = s.variables.result !== undefined;
      if (hasResult) {
        // Final step — either "found" or "not-found"
        line = s.variables.result === -1 ? sm["not-found"] : sm["found"];
      } else {
        // Comparison step (check at index i)
        line = sm["comparison"];
      }
    }

    return line !== undefined ? { ...s, highlightLine: line } : s;
  });
}
