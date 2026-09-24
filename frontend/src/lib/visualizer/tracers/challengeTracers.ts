/**
 * Challenge generators for Array concepts.
 *
 * Each function takes the same TracerInput as the corresponding concept tracer
 * and returns a VisualizationStep[] where certain steps carry a `challenge`
 * field. The engine pauses at those steps so the learner must answer before
 * the visualization continues.
 *
 * Design rules:
 *  - Never mutate the steps produced by the base tracer.
 *  - Attach challenges at "just-before-reveal" moments — the learner sees the
 *    current state and predicts what comes next.
 *  - At most one challenge per logical group of steps (don't spam).
 *  - Keep choices short (≤ 4 options, always exactly 4 for multiple-choice).
 *  - Always include plausible distractors, not obviously wrong values.
 */

import type { VisualizationStep, StepChallenge } from "../types";
import {
  traceIndexing,
  traceTraversal,
  traceAccessByIndex,
  traceUpdateElement,
} from "./arrayConceptTracers";
import { traceLinearSearchWithCode } from "../codeBinding";
import type { TracerInput } from "../types";

// traceLinearSearch is replaced by traceLinearSearchWithCode so challenge steps
// automatically carry highlightLine — withChallenge spreads all step fields.

// ─── Utility: clone step with a challenge attached ───────────────────────────
function withChallenge(
  step: VisualizationStep,
  challenge: StepChallenge
): VisualizationStep {
  return { ...step, challenge };
}

// ─── Shuffle helper (Fisher-Yates) ───────────────────────────────────────────
function shuffle<T>(arr: T[]): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

/** Build 4 choices: one correct + up to 3 unique distractors, shuffled. */
function choices(
  correct: string,
  distractors: string[]
): { choices: { label: string; value: string }[]; correctAnswer: string } {
  const seen = new Set<string>([correct]);
  const pool: string[] = [correct];
  for (const d of distractors) {
    if (!seen.has(d) && pool.length < 4) {
      seen.add(d);
      pool.push(d);
    }
  }
  // Fill to exactly 4 with numeric fallbacks if distractors were too similar
  let fallback = 1;
  while (pool.length < 4) {
    const f = String(Number(correct) + fallback * 3 + 7);
    if (!seen.has(f)) { seen.add(f); pool.push(f); }
    fallback++;
  }
  const shuffled = shuffle(pool);
  return {
    choices: shuffled.map((v) => ({ label: v, value: v })),
    correctAnswer: correct,
  };
}

// ─── 1. Indexing challenge ────────────────────────────────────────────────────
// Challenge type: predict-next-index + identify-state + predict-complexity

export function challengeIndexing(input: TracerInput): VisualizationStep[] {
  const base = traceIndexing(input);
  const arr = input.array;
  const last = arr.length - 1;
  const result: VisualizationStep[] = [];

  // Step 0 — intro, no challenge
  result.push(base[0]);

  // Step 1 — after showing index 0, challenge: predict last valid index
  result.push(base[1]);
  const c1 = choices(String(last), [
    String(arr.length),
    String(last - 1),
    String(last + 1),
  ]);
  result.push(withChallenge(base[1], {
    type: "predict-next-index",
    question: `The array has ${arr.length} elements. What is the index of the LAST element?`,
    ...c1,
    explanation: `The last valid index is always length − 1 = ${arr.length} − 1 = ${last}. Using index ${arr.length} would cause an out-of-bounds error.`,
  }));

  // Step 2 — last element highlighted, continue
  result.push(base[2]);

  // Walk steps: challenge at the mid-point — predict next value
  const walkStart = 3; // base[3..3+arr.length-1] are the per-index walk steps
  const midWalk = walkStart + Math.floor(arr.length / 2);
  for (let i = walkStart; i < walkStart + arr.length; i++) {
    if (i === midWalk && i + 1 < walkStart + arr.length) {
      result.push(base[i]);
      const nextIdx = (i - walkStart) + 1;
      const cv = choices(String(arr[nextIdx]), [
        String(arr[nextIdx] + 10),
        String(arr[nextIdx] - 5),
        nextIdx > 0 ? String(arr[nextIdx - 1]) : String(arr[nextIdx] + 3),
      ]);
      result.push(withChallenge(base[i], {
        type: "predict-next-value",
        question: `arr[${i - walkStart}] = ${arr[i - walkStart]}. What is the value at arr[${nextIdx}]?`,
        ...cv,
        explanation: `arr[${nextIdx}] = ${arr[nextIdx]}. Each index maps directly to one value — you read it with a single O(1) operation.`,
      }));
    } else {
      result.push(base[i]);
    }
  }

  // Final step — complexity challenge
  result.push(base[base.length - 1]);
  const cComp = choices("O(1)", ["O(n)", "O(log n)", "O(n²)"]);
  result.push(withChallenge(base[base.length - 1], {
    type: "predict-complexity",
    question: "What is the time complexity of accessing arr[i] in an array?",
    ...cComp,
    explanation: "Arrays use index arithmetic (base + i × size) to jump directly to any element. This is one instruction — O(1) — regardless of array length.",
  }));

  return result;
}

// ─── 2. Traversal challenge ───────────────────────────────────────────────────
// Challenges: predict next index twice + final result + complexity

export function challengeTraversal(input: TracerInput): VisualizationStep[] {
  const base = traceTraversal(input);
  const arr = input.array;
  const result: VisualizationStep[] = [];

  // Intro step
  result.push(base[0]);

  // Visit steps are base[1..arr.length]
  for (let i = 0; i < arr.length; i++) {
    const stepIdx = i + 1;
    result.push(base[stepIdx]);

    // Challenge after step i=1: predict next index
    if (i === 1 && i + 1 < arr.length) {
      const cn = choices(String(i + 1), [
        String(i),
        String(i + 2 < arr.length ? i + 2 : 0),
        String(arr.length),
      ]);
      result.push(withChallenge(base[stepIdx], {
        type: "predict-next-index",
        question: `We just visited index ${i}. What index will be visited next?`,
        ...cn,
        explanation: `Traversal increments i by 1 each time. After index ${i}, the next is i + 1 = ${i + 1}.`,
      }));
    }

    // Challenge after step i=floor(n/2): predict next value
    const mid = Math.floor(arr.length / 2);
    if (i === mid && i + 1 < arr.length) {
      const cv = choices(String(arr[i + 1]), [
        String(arr[i]),
        String(arr[i + 1] + 5),
        String(arr[Math.max(0, i - 1)]),
      ]);
      result.push(withChallenge(base[stepIdx], {
        type: "predict-next-value",
        question: `Currently at index ${i} (value ${arr[i]}). What value will be visited next?`,
        ...cv,
        explanation: `arr[${i + 1}] = ${arr[i + 1]}. Traversal always moves left-to-right, so the next element is the one immediately to the right.`,
      }));
    }
  }

  // Final step + complexity challenge
  result.push(base[base.length - 1]);
  const cComp = choices("O(n)", ["O(1)", "O(log n)", "O(n²)"]);
  result.push(withChallenge(base[base.length - 1], {
    type: "predict-complexity",
    question: `The array has ${arr.length} elements. What is the time complexity of traversal?`,
    ...cComp,
    explanation: `Traversal visits every element exactly once. If the array has n elements, that's n operations — O(n). You can't skip any element without risking missing data.`,
  }));

  return result;
}

// ─── 3. Access-by-index challenge ────────────────────────────────────────────
// Challenges: predict the accessed value + predict-complexity

export function challengeAccessByIndex(input: TracerInput): VisualizationStep[] {
  const base = traceAccessByIndex(input);
  const arr = input.array;
  const targetIdx =
    input.target !== undefined && input.target >= 0 && input.target < arr.length
      ? Math.floor(input.target)
      : Math.floor(arr.length / 2);
  const result: VisualizationStep[] = [];

  // Intro
  result.push(base[0]);

  // Step 1 — "jumping" to index — challenge: what value is there?
  result.push(base[1]);
  const adjacentIdx = Math.min(targetIdx + 1, arr.length - 1);
  const prevIdx = Math.max(targetIdx - 1, 0);
  const cv = choices(String(arr[targetIdx]), [
    String(arr[adjacentIdx]),
    String(arr[prevIdx]),
    String(arr[targetIdx] + 7),
  ]);
  result.push(withChallenge(base[1], {
    type: "predict-next-value",
    question: `We are jumping directly to index ${targetIdx}. What value is stored there?`,
    ...cv,
    explanation: `arr[${targetIdx}] = ${arr[targetIdx]}. The CPU calculated the memory address in one step and read that value directly — no scanning was needed.`,
  }));

  // Step 2 — result revealed
  result.push(base[2]);

  // Complexity challenge
  const cComp = choices("O(1)", ["O(n)", "O(log n)", "O(n²)"]);
  result.push(withChallenge(base[2], {
    type: "predict-complexity",
    question: "How many operations does it take to access arr[i] regardless of array size?",
    ...cComp,
    explanation: "Exactly 1 arithmetic operation: base address + (i × element size). This is O(1). Array size is irrelevant.",
  }));

  return result;
}

// ─── 4. Update-element challenge ─────────────────────────────────────────────
// Challenges: identify state after update + predict new value + complexity

export function challengeUpdateElement(input: TracerInput): VisualizationStep[] {
  const base = traceUpdateElement(input);
  const arr = input.array;
  const idx =
    input.target !== undefined && input.target >= 0 && input.target < arr.length
      ? Math.floor(input.target)
      : Math.floor(arr.length / 2);
  const oldValue = arr[idx];
  const newValue = oldValue + 50;
  const result: VisualizationStep[] = [];

  // Intro
  result.push(base[0]);

  // Locate step — challenge: what value is currently at idx?
  result.push(base[1]);
  const cv = choices(String(oldValue), [
    String(oldValue + 10),
    String(oldValue - 10),
    String(arr[Math.min(idx + 1, arr.length - 1)]),
  ]);
  result.push(withChallenge(base[1], {
    type: "predict-next-value",
    question: `We jumped to index ${idx}. What is the CURRENT value stored there?`,
    ...cv,
    explanation: `arr[${idx}] = ${oldValue}. This is the value about to be overwritten by the update operation.`,
  }));

  // Write step
  result.push(base[2]);

  // After write — challenge: what is the NEW value?
  const cNew = choices(String(newValue), [
    String(oldValue),
    String(newValue + 10),
    String(newValue - 50),
  ]);
  result.push(withChallenge(base[2], {
    type: "predict-final-result",
    question: `The old value was ${oldValue}. We just wrote ${oldValue} + 50. What is arr[${idx}] now?`,
    ...cNew,
    explanation: `arr[${idx}] is now ${newValue} (= ${oldValue} + 50). The old value ${oldValue} is permanently gone — arrays are mutable and update is in-place.`,
  }));

  // Final step
  result.push(base[3]);

  // Complexity
  const cComp = choices("O(1)", ["O(n)", "O(log n)", "O(n²)"]);
  result.push(withChallenge(base[3], {
    type: "predict-complexity",
    question: "Updating a single element at a known index costs…",
    ...cComp,
    explanation: "Like reading, writing to arr[i] is a single memory write — O(1). No other elements are moved or shifted.",
  }));

  return result;
}

// ─── 5. Linear-search challenge ───────────────────────────────────────────────
// Challenges: predict next index + identify state + predict final result + complexity

export function challengeLinearSearch(input: TracerInput): VisualizationStep[] {
  const base = traceLinearSearchWithCode(input);
  const arr = input.array;
  const target = input.target ?? arr[Math.floor(arr.length / 2)];
  const foundIndex = arr.indexOf(target);
  const result: VisualizationStep[] = [];

  // Intro
  result.push(base[0]);

  // Iteration steps: base[1..n] are "check index i" + optional found step
  let baseIdx = 1;
  let i = 0;
  let challengeCount = 0;

  while (baseIdx < base.length) {
    const s = base[baseIdx];
    result.push(s);

    // Challenge after index 0 — predict next index
    if (i === 0 && challengeCount === 0 && arr[i] !== target) {
      const cn = choices("1", ["0", "2", String(arr.length - 1)]);
      result.push(withChallenge(s, {
        type: "predict-next-index",
        question: `We just checked index 0. arr[0] = ${arr[0]} ≠ ${target}. What index do we check next?`,
        ...cn,
        explanation: "Linear search moves left to right, incrementing i by 1. After index 0 comes index 1.",
      }));
      challengeCount++;
    }

    // Challenge at the midpoint — identify state
    const mid = Math.floor(arr.length / 2);
    if (i === mid && arr[i] !== target && challengeCount === 1) {
      const halfDone = Array.from({ length: mid }, (_, k) => arr[k] !== target ? "eliminated" : "found");
      const allElim = halfDone.every((st) => st === "eliminated");
      const cState = choices(
        "Searching — target not yet found",
        ["Found — returning result", "Done — search complete", "Error — index out of bounds"]
      );
      result.push(withChallenge(s, {
        type: "identify-state",
        question: `We've checked ${mid + 1} elements (indices 0–${mid}). What is the current algorithm state?`,
        ...cState,
        explanation: allElim
          ? `We've checked ${mid + 1} elements and ${target} wasn't in any of them. The algorithm is still actively searching the remaining ${arr.length - mid - 1} elements.`
          : "The search continues until we find the target or exhaust the array.",
      }));
      challengeCount++;
    }

    // If this step is a "found" step (has result variable), challenge about result
    if (
      s.variables.result !== undefined &&
      s.variables.result !== -1 &&
      challengeCount >= 1
    ) {
      const ri = Number(s.variables.result);
      const cResult = choices(
        String(ri),
        [String(Math.max(ri - 1, 0)), String(ri + 1), "-1"]
      );
      result.push(withChallenge(s, {
        type: "predict-final-result",
        question: `The target ${target} was found. At what INDEX was it located?`,
        ...cResult,
        explanation: `arr[${ri}] = ${target}. Linear search returns the index of the first occurrence, or -1 if not found. Here it found ${target} at index ${ri}.`,
      }));
      challengeCount++;
      break;
    }

    // If not-found step
    if (
      s.variables.result === -1 &&
      challengeCount >= 1
    ) {
      const cNotFound = choices("-1", [
        String(arr.length),
        String(arr.length - 1),
        "0",
      ]);
      result.push(withChallenge(s, {
        type: "predict-final-result",
        question: `${target} was not found after checking all ${arr.length} elements. What does linear search return?`,
        ...cNotFound,
        explanation: "-1 is the conventional sentinel for 'not found'. It cannot be a valid index (indices start at 0), so it unambiguously signals absence.",
      }));
      challengeCount++;
      break;
    }

    baseIdx++;
    // Advance i for check steps (they have variable i set)
    if (s.variables.i !== undefined) i = Number(s.variables.i);
  }

  // Complexity challenge at end
  const last = base[base.length - 1];
  result.push(last);
  const cComp = choices("O(n)", ["O(1)", "O(log n)", "O(n log n)"]);
  result.push(withChallenge(last, {
    type: "predict-complexity",
    question: `The array has ${arr.length} elements. What is the worst-case time complexity of linear search?`,
    ...cComp,
    explanation: `Worst case: the target is the last element or absent. We check all n elements — O(n). Best case is O(1) (target at index 0). Average is O(n/2) = O(n).`,
  }));

  return result;
}

// ─── Registry ─────────────────────────────────────────────────────────────────
// Maps concept ID → challenge generator function.
// Extensible: add binary-search, sliding-window, etc. here.

export type ChallengeGeneratorFn = (input: TracerInput) => VisualizationStep[];

export const CHALLENGE_GENERATORS: Record<string, ChallengeGeneratorFn> = {
  indexing:      challengeIndexing,
  traversal:     challengeTraversal,
  access:        challengeAccessByIndex,
  update:        challengeUpdateElement,
  "linear-search": challengeLinearSearch,
};
