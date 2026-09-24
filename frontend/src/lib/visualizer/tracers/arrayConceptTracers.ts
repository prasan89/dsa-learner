import type {
  VisualizationStep,
  VisualizationCell,
  CellState,
  TracerFn,
  TracerInput,
} from "../types";

// ─── Shared helpers ───────────────────────────────────────────────────────────

function snap(
  arr: number[],
  stateMap: Record<number, CellState> = {},
  labelMap: Record<number, string> = {}
): VisualizationCell[] {
  return arr.map((v, i) => ({
    value: v,
    index: i,
    state: stateMap[i] ?? "default",
    label: labelMap[i],
  }));
}

function step(
  arr: number[],
  stateMap: Record<number, CellState>,
  labelMap: Record<number, string>,
  message: string,
  pointers: Record<string, number> = {},
  variables: Record<string, number | string | boolean> = {}
): VisualizationStep {
  return { cells: snap(arr, stateMap, labelMap), pointers, variables, message };
}

// ─── 1. Indexing ──────────────────────────────────────────────────────────────
// Show each slot in the array with its index, explaining how indices work.

export const traceIndexing: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  // Step 0 — all default, introduce the concept
  steps.push(step(
    arr, {}, {},
    "An array stores elements in contiguous memory slots. Each slot has a fixed index starting at 0.",
    {}, { length: arr.length }
  ));

  // Step 1 — highlight index 0
  steps.push(step(
    arr, { 0: "active" }, { 0: "idx 0" },
    `Index 0 is always the first element. Here arr[0] = ${arr[0]}. Indices start at 0, not 1.`,
    { i: 0 }, { "arr[0]": arr[0] }
  ));

  // Step 2 — highlight last index
  const last = arr.length - 1;
  steps.push(step(
    arr, { [last]: "active" }, { [last]: `idx ${last}` },
    `Index ${last} is the last element (length - 1 = ${arr.length} - 1). arr[${last}] = ${arr[last]}.`,
    { i: last }, { [`arr[${last}]`]: arr[last], "last index": last }
  ));

  // Steps — walk every index
  for (let i = 0; i < arr.length; i++) {
    const stateMap: Record<number, CellState> = {};
    for (let j = 0; j < i; j++) stateMap[j] = "eliminated";
    stateMap[i] = "active";
    steps.push(step(
      arr,
      stateMap,
      { [i]: `[${i}]` },
      `arr[${i}] = ${arr[i]}. To access any element you need its index — O(1) direct lookup.`,
      { i },
      { index: i, value: arr[i] }
    ));
  }

  // Final — all revealed
  const allActive: Record<number, CellState> = {};
  arr.forEach((_, i) => (allActive[i] = "sorted"));
  steps.push(step(
    arr, allActive, {},
    `Every element lives at a unique index from 0 to ${last}. Access by index is always O(1) — instant, regardless of array size.`,
    {}, { length: arr.length, "index range": `0–${last}` }
  ));

  return steps;
};

// ─── 2. Traversal ─────────────────────────────────────────────────────────────
// Visit every element exactly once from left to right.

export const traceTraversal: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    "Traversal means visiting every element once, in order, from index 0 to length-1.",
    {}, { i: "–", length: arr.length }
  ));

  for (let i = 0; i < arr.length; i++) {
    const stateMap: Record<number, CellState> = {};
    for (let j = 0; j < i; j++) stateMap[j] = "sorted";    // already visited
    stateMap[i] = "active";                                  // currently visiting
    steps.push(step(
      arr,
      stateMap,
      { [i]: "i" },
      `i = ${i}: visiting arr[${i}] = ${arr[i]}. We process it (e.g. print, sum, compare), then move right.`,
      { i },
      { i, "arr[i]": arr[i], visited: i }
    ));
  }

  const allDone: Record<number, CellState> = {};
  arr.forEach((_, i) => (allDone[i] = "sorted"));
  steps.push(step(
    arr, allDone, {},
    `All ${arr.length} elements visited. Traversal always takes O(n) time — you must touch every element. Space is O(1).`,
    {}, { visited: arr.length, total: arr.length }
  ));

  return steps;
};

// ─── 3. Access by index ───────────────────────────────────────────────────────
// Demonstrate that arr[i] jumps directly — no scanning required.

export const traceAccessByIndex: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  // target holds the desired index (not a value), fallback to middle
  const targetIdx = (target !== undefined && target >= 0 && target < arr.length)
    ? Math.floor(target)
    : Math.floor(arr.length / 2);

  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `We want arr[${targetIdx}]. Arrays sit in contiguous memory. The CPU computes the address directly: base + index × element_size.`,
    {}, { "target index": targetIdx }
  ));

  // Show that no scanning happens — grey everything except the target
  const directMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { directMap[i] = i === targetIdx ? "active" : "default"; });
  steps.push(step(
    arr,
    directMap,
    { [targetIdx]: "i" },
    `No loop needed. The CPU jumps straight to index ${targetIdx} — one operation regardless of array size.`,
    { i: targetIdx },
    { "arr[i]": arr[targetIdx], "target index": targetIdx }
  ));

  // Highlight the result
  const foundMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { foundMap[i] = i === targetIdx ? "found" : "eliminated"; });
  steps.push(step(
    arr,
    foundMap,
    { [targetIdx]: `[${targetIdx}]` },
    `arr[${targetIdx}] = ${arr[targetIdx]}. Access is O(1) — constant time. The size of the array doesn't matter.`,
    { i: targetIdx },
    { result: arr[targetIdx], "time complexity": "O(1)" }
  ));

  return steps;
};

// ─── 4. Update element ────────────────────────────────────────────────────────
// Show the before, the write, and the after in-place.

export const traceUpdateElement: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  // target = index to update; newValue = arr[target] + 99 (visible change)
  const idx = (target !== undefined && target >= 0 && target < arr.length)
    ? Math.floor(target)
    : Math.floor(arr.length / 2);
  const oldValue = arr[idx];
  const newValue = oldValue + 50;

  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `We want to update the element at index ${idx}. Current value: arr[${idx}] = ${oldValue}.`,
    {}, { "target index": idx, "old value": oldValue }
  ));

  // Locate the index
  const locateMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { locateMap[i] = i === idx ? "comparing" : "default"; });
  steps.push(step(
    arr,
    locateMap,
    { [idx]: `[${idx}]` },
    `Jump directly to index ${idx} — O(1) access. No scanning. Found value ${oldValue}.`,
    { i: idx },
    { index: idx, "current value": oldValue }
  ));

  // Perform the write
  arr[idx] = newValue;
  const writeMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { writeMap[i] = i === idx ? "active" : "default"; });
  steps.push(step(
    arr,
    writeMap,
    { [idx]: "write" },
    `Write new value ${newValue} into arr[${idx}]. The array is mutated in-place — old value ${oldValue} is gone.`,
    { i: idx },
    { index: idx, "new value": newValue, "old value": oldValue }
  ));

  // Show result
  const doneMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { doneMap[i] = i === idx ? "found" : "sorted"; });
  steps.push(step(
    arr,
    doneMap,
    { [idx]: `[${idx}]` },
    `Update complete. arr[${idx}] = ${newValue}. Like access, update is O(1) — direct write, no shifting required.`,
    {},
    { "arr[i]": newValue, "time complexity": "O(1)" }
  ));

  return steps;
};

// ─── 5. Linear Search ─────────────────────────────────────────────────────────
// Check each element left to right until target is found or the array is exhausted.

export const traceLinearSearch: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const t = target ?? arr[Math.floor(arr.length / 2)];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `Linear search for value ${t}. We must check every element from left to right — the array may be unsorted.`,
    {}, { target: t, i: "–" }
  ));

  for (let i = 0; i < arr.length; i++) {
    // Build state: previous are eliminated, current is comparing
    const stateMap: Record<number, CellState> = {};
    for (let j = 0; j < i; j++) stateMap[j] = "eliminated";
    stateMap[i] = "comparing";

    steps.push(step(
      arr,
      stateMap,
      { [i]: "i" },
      `Check index ${i}: arr[${i}] = ${arr[i]}. Is ${arr[i]} === ${t}? ${arr[i] === t ? "Yes!" : "No — keep going."}`,
      { i },
      { i, "arr[i]": arr[i], target: t }
    ));

    if (arr[i] === t) {
      const foundMap: Record<number, CellState> = {};
      for (let j = 0; j < i; j++) foundMap[j] = "eliminated";
      foundMap[i] = "found";
      steps.push(step(
        arr,
        foundMap,
        { [i]: "found!" },
        `Found ${t} at index ${i} after checking ${i + 1} element${i === 0 ? "" : "s"}. Best case is O(1) (index 0), worst case O(n) (last or not present).`,
        { i },
        { result: i, target: t, comparisons: i + 1 }
      ));
      return steps;
    }
  }

  // Not found
  const allElim: Record<number, CellState> = {};
  arr.forEach((_, i) => (allElim[i] = "eliminated"));
  steps.push(step(
    arr, allElim, {},
    `${t} not found after checking all ${arr.length} elements. Worst case: O(n). Space: O(1).`,
    {},
    { result: -1, target: t, comparisons: arr.length }
  ));

  return steps;
};

// ─── Concept registry ─────────────────────────────────────────────────────────

export interface ArrayConcept {
  id: string;
  title: string;
  tagline: string;
  explanation: string;
  tracer: TracerFn;
  defaultArray: number[];
  defaultTarget?: number;          // index for access/update; value for search
  showTargetInput: "none" | "index" | "value";
  targetLabel: string;
  algorithmSteps: string[];
  timeComplexity: string;
  spaceComplexity: string;
  complexityNote: string;
}

export const ARRAY_CONCEPTS: ArrayConcept[] = [
  {
    id: "indexing",
    title: "Indexing",
    tagline: "How arrays locate any element instantly",
    explanation:
      "Arrays store elements in contiguous memory. Each element occupies a slot identified by its index — a zero-based integer. Because the memory address of any element can be computed as `base + index × size`, lookup is always O(1) regardless of array size.",
    tracer: traceIndexing,
    defaultArray: [10, 25, 31, 42, 57, 81],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "The array starts at a base memory address.",
      "Each element occupies the same amount of space (e.g., 4 bytes for int).",
      "Address of arr[i] = base + i × element_size.",
      "One arithmetic operation → direct memory access.",
      "Index 0 is first, index length-1 is last.",
    ],
    timeComplexity: "O(1)",
    spaceComplexity: "O(1)",
    complexityNote: "Index-based addressing is a single arithmetic operation — constant time no matter how large the array.",
  },
  {
    id: "traversal",
    title: "Traversal",
    tagline: "Visiting every element exactly once",
    explanation:
      "Traversal means iterating through the entire array from index 0 to index length−1, processing each element once. It's the foundation of most array algorithms — summing, printing, searching, mapping.",
    tracer: traceTraversal,
    defaultArray: [10, 25, 31, 42, 57, 81],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Start at index i = 0.",
      "Process arr[i] (print, sum, compare, etc.).",
      "Increment i.",
      "Repeat while i < length.",
      "Stop after visiting all n elements.",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "You must touch every element once — linear time. No extra memory is needed, so space is constant.",
  },
  {
    id: "access",
    title: "Access by Index",
    tagline: "Reading any value in constant time",
    explanation:
      "Random access lets you read arr[i] in one step, no matter where i is. This is what separates arrays from linked lists — you don't walk the structure; you jump straight to the address.",
    tracer: traceAccessByIndex,
    defaultArray: [10, 25, 31, 42, 57, 81],
    defaultTarget: 3,
    showTargetInput: "index",
    targetLabel: "Access index",
    algorithmSteps: [
      "Receive the target index i.",
      "Compute address: base + i × element_size.",
      "Read the value at that address.",
      "Return the value — done.",
    ],
    timeComplexity: "O(1)",
    spaceComplexity: "O(1)",
    complexityNote: "One arithmetic instruction. Array size is irrelevant — accessing index 0 or index 10,000,000 takes the same time.",
  },
  {
    id: "update",
    title: "Update Element",
    tagline: "Writing a new value at a given index",
    explanation:
      "Updating arr[i] = newValue is as cheap as reading: jump directly to the index, write the new value. The array is mutated in-place — no shifting, no copying, no reallocation.",
    tracer: traceUpdateElement,
    defaultArray: [10, 25, 31, 42, 57, 81],
    defaultTarget: 2,
    showTargetInput: "index",
    targetLabel: "Update index",
    algorithmSteps: [
      "Receive the target index i.",
      "Compute address: base + i × element_size.",
      "Overwrite the value at that memory address.",
      "Done — the old value is replaced.",
    ],
    timeComplexity: "O(1)",
    spaceComplexity: "O(1)",
    complexityNote: "A direct memory write, identical in cost to a read. No surrounding elements are affected.",
  },
  {
    id: "linear-search",
    title: "Linear Search",
    tagline: "Finding a value when the array is unsorted",
    explanation:
      "When an array is unsorted, there's no shortcut — you must check each element left to right until you find the target or exhaust the array. This is the simplest search algorithm and the baseline all others improve upon.",
    tracer: traceLinearSearch,
    defaultArray: [10, 25, 31, 42, 57, 81],
    defaultTarget: 42,
    showTargetInput: "value",
    targetLabel: "Search target",
    algorithmSteps: [
      "Set i = 0.",
      "Compare arr[i] with target.",
      "If equal → return i (found).",
      "If not equal → increment i.",
      "If i === length → return -1 (not found).",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote:
      "Best case O(1) — target is the first element. Worst case O(n) — target is last or absent. On average, n/2 comparisons.",
  },
];
