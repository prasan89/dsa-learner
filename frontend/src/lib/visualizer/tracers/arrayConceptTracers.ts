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

export const traceIndexing: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    "An array stores elements in contiguous memory slots. Each slot has a fixed index starting at 0.",
    {}, { length: arr.length }
  ));

  steps.push(step(
    arr, { 0: "active" }, { 0: "idx 0" },
    `Index 0 is always the first element. Here arr[0] = ${arr[0]}. Indices start at 0, not 1.`,
    { i: 0 }, { "arr[0]": arr[0] }
  ));

  const last = arr.length - 1;
  steps.push(step(
    arr, { [last]: "active" }, { [last]: `idx ${last}` },
    `Index ${last} is the last element (length - 1 = ${arr.length} - 1). arr[${last}] = ${arr[last]}.`,
    { i: last }, { [`arr[${last}]`]: arr[last], "last index": last }
  ));

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
    for (let j = 0; j < i; j++) stateMap[j] = "sorted";
    stateMap[i] = "active";
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

export const traceAccessByIndex: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const targetIdx = (target !== undefined && target >= 0 && target < arr.length)
    ? Math.floor(target)
    : Math.floor(arr.length / 2);

  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `We want arr[${targetIdx}]. Arrays sit in contiguous memory. The CPU computes the address directly: base + index × element_size.`,
    {}, { "target index": targetIdx }
  ));

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

export const traceUpdateElement: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
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

// ─── 6. Insert at Position ────────────────────────────────────────────────────
// Show how inserting at index pos requires shifting elements right.

export const traceInsertAtPosition: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const pos = (target !== undefined && target >= 0 && target <= arr.length)
    ? Math.floor(target)
    : Math.floor(arr.length / 2);
  const newVal = 99;
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `Insert value ${newVal} at index ${pos}. Elements from index ${pos} onward must shift right to make room.`,
    {}, { "insert at": pos, "new value": newVal, length: arr.length }
  ));

  // Highlight the insertion point
  const locMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { locMap[i] = i === pos ? "active" : "default"; });
  steps.push(step(
    arr, locMap, { [pos]: "pos" },
    `Index ${pos} is the target slot. Everything from here to the end must move one step right.`,
    { pos },
    { "insert at": pos }
  ));

  // Animate the shift: from right to left
  const shiftArr = [...arr];
  for (let i = shiftArr.length - 1; i >= pos; i--) {
    const sm: Record<number, CellState> = {};
    shiftArr.forEach((_, j) => {
      if (j < pos) sm[j] = "eliminated";
      else if (j === i) sm[j] = "active";
      else sm[j] = "sorted";
    });
    steps.push(step(
      shiftArr, sm, { [i]: "shift→" },
      `Shift arr[${i}] = ${shiftArr[i]} one position right (to index ${i + 1}).`,
      { i },
      { shifting: shiftArr[i] }
    ));
  }

  // Insert the value
  const resultArr = [...arr.slice(0, pos), newVal, ...arr.slice(pos)].slice(0, arr.length + 1);
  // Show only original length for visualization
  const displayArr = resultArr.slice(0, arr.length);
  displayArr[pos] = newVal;
  for (let i = pos; i < arr.length - 1; i++) displayArr[i + 1] = arr[i];

  const insertMap: Record<number, CellState> = {};
  displayArr.forEach((_, i) => { insertMap[i] = i === pos ? "found" : i > pos ? "sorted" : "eliminated"; });
  steps.push(step(
    displayArr, insertMap, { [pos]: "new" },
    `Inserted ${newVal} at index ${pos}. All subsequent elements moved right. Cost: O(n) — worst case shifts n elements.`,
    { pos },
    { "new value": newVal, "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── 7. Delete from Position ──────────────────────────────────────────────────
// Removing an element requires shifting everything after it left by one.

export const traceDeleteFromPosition: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const pos = (target !== undefined && target >= 0 && target < arr.length)
    ? Math.floor(target)
    : Math.floor(arr.length / 2);
  const deletedVal = arr[pos];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `Delete element at index ${pos} (value ${deletedVal}). Elements to the right must shift left to fill the gap.`,
    {}, { "delete at": pos, "delete value": deletedVal }
  ));

  // Highlight the element being deleted
  const locMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { locMap[i] = i === pos ? "comparing" : "default"; });
  steps.push(step(
    arr, locMap, { [pos]: "del" },
    `arr[${pos}] = ${deletedVal} will be removed. A gap opens here — we fill it by shifting left.`,
    { pos },
    { "deleting": deletedVal }
  ));

  // Animate left-shift
  const shiftArr = [...arr];
  for (let i = pos; i < shiftArr.length - 1; i++) {
    shiftArr[i] = shiftArr[i + 1];
    const sm: Record<number, CellState> = {};
    shiftArr.forEach((_, j) => {
      if (j < pos) sm[j] = "eliminated";
      else if (j === i) sm[j] = "active";
      else sm[j] = "default";
    });
    steps.push(step(
      [...shiftArr],
      sm,
      { [i]: "←shift" },
      `arr[${i + 1}] = ${shiftArr[i]} slides left to arr[${i}].`,
      { i },
      { shifting: shiftArr[i] }
    ));
  }

  // Final state — last slot is logically empty (show as eliminated)
  const finalArr = [...shiftArr];
  const finalMap: Record<number, CellState> = {};
  finalArr.forEach((_, i) => {
    if (i === finalArr.length - 1) finalMap[i] = "eliminated";
    else if (i < pos) finalMap[i] = "sorted";
    else finalMap[i] = "sorted";
  });
  steps.push(step(
    finalArr, finalMap, {},
    `Deletion complete. ${arr.length - 1} elements remain. Last slot is logically empty. Cost: O(n) — up to n shifts.`,
    {},
    { "deleted value": deletedVal, "new length": arr.length - 1, "time complexity": "O(n)" }
  ));

  return steps;
};

// ─── 8. Size vs Capacity ──────────────────────────────────────────────────────
// Distinguish between the number of elements stored and the allocated backing store.

export const traceSizeVsCapacity: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const capacity = Math.min(arr.length + 3, 12);
  const size = arr.length;
  const steps: VisualizationStep[] = [];

  // Show initial array — all used slots active
  const usedMap: Record<number, CellState> = {};
  const emptyMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { usedMap[i] = "sorted"; });
  for (let i = size; i < capacity; i++) emptyMap[i] = "eliminated";

  steps.push(step(
    arr, usedMap, {},
    `This array holds ${size} elements. That is its size — how many slots are actually occupied.`,
    {}, { size, capacity: "?" }
  ));

  // Extend visual to show capacity slots
  const paddedArr = [...arr, ...Array(capacity - size).fill(0)];
  const capacityMap: Record<number, CellState> = {};
  paddedArr.forEach((_, i) => { capacityMap[i] = i < size ? "sorted" : "eliminated"; });
  steps.push(step(
    paddedArr, capacityMap, {},
    `Under the hood, the backing array allocated ${capacity} slots. The extra ${capacity - size} slots are reserved but empty — that is the capacity.`,
    {}, { size, capacity, "empty slots": capacity - size }
  ));

  // Highlight the boundary
  const boundMap: Record<number, CellState> = {};
  paddedArr.forEach((_, i) => { boundMap[i] = i < size ? "active" : "comparing"; });
  steps.push(step(
    paddedArr, boundMap, { [size - 1]: "size-1", [capacity - 1]: "cap-1" },
    `Size = ${size} (blue). Capacity = ${capacity} (amber). Adding elements is O(1) amortized while size < capacity — no reallocation needed.`,
    {}, { size, capacity, "can add without realloc": capacity - size }
  ));

  // Show what happens when capacity is exceeded — reallocation
  const newCapacity = capacity * 2;
  steps.push(step(
    paddedArr, capacityMap, {},
    `When size reaches capacity, the runtime allocates a new backing array (typically 2× the old capacity = ${newCapacity} slots) and copies all elements. This O(n) copy is amortized O(1) per insertion.`,
    {},
    { "current capacity": capacity, "new capacity after resize": newCapacity, "copy cost": `O(${size})`, "amortized per push": "O(1)" }
  ));

  return steps;
};

// ─── 9. Fixed vs Dynamic Arrays ───────────────────────────────────────────────
// Contrast a static array (C-style) with a dynamic array (ArrayList/Python list).

export const traceFixedVsDynamic: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  // Fixed array — show all slots locked
  const fixedMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { fixedMap[i] = "sorted"; });
  steps.push(step(
    arr, fixedMap, {},
    `Fixed-size array: declared with a set length (here: ${arr.length}). Size can never change at runtime. Accessing is O(1) but overflow causes an error.`,
    {}, { type: "fixed", length: arr.length, "can grow": false }
  ));

  // Dynamic array — show initial allocation then growth
  const dynCapacity = arr.length + 4;
  const dynArr = [...arr, ...Array(4).fill(0)];
  const dynMap: Record<number, CellState> = {};
  dynArr.forEach((_, i) => { dynMap[i] = i < arr.length ? "active" : "eliminated"; });
  steps.push(step(
    dynArr, dynMap, {},
    `Dynamic array (ArrayList / Python list): same O(1) access but a larger backing store is pre-allocated. Grey slots are reserved but empty.`,
    {}, { type: "dynamic", size: arr.length, capacity: dynCapacity }
  ));

  // Append new elements
  let currentSize = arr.length;
  for (let round = 0; round < 3; round++) {
    const appendVal = 10 * (round + 1);
    dynArr[currentSize] = appendVal;
    currentSize++;
    const appendMap: Record<number, CellState> = {};
    dynArr.forEach((_, i) => {
      if (i < currentSize - 1) appendMap[i] = "sorted";
      else if (i === currentSize - 1) appendMap[i] = "found";
      else appendMap[i] = "eliminated";
    });
    steps.push(step(
      [...dynArr], appendMap, { [currentSize - 1]: "new" },
      `Append ${appendVal}: slot ${currentSize - 1} was free — O(1) write. No shifting required when adding to the end.`,
      { i: currentSize - 1 },
      { appended: appendVal, size: currentSize, capacity: dynCapacity }
    ));
  }

  // Show capacity exceeded → reallocation
  const fullMap: Record<number, CellState> = {};
  dynArr.forEach((_, i) => { fullMap[i] = i < dynCapacity ? "comparing" : "eliminated"; });
  steps.push(step(
    dynArr, fullMap, {},
    `When all ${dynCapacity} slots fill up, the runtime creates a new array (~2× size), copies all elements, and frees the old one. This O(n) copy happens rarely, giving O(1) amortized append.`,
    {}, { "old capacity": dynCapacity, "new capacity": dynCapacity * 2, "realloc cost": "O(n)", "amortized append": "O(1)" }
  ));

  return steps;
};

// ─── 10. Array Operation Complexity ───────────────────────────────────────────
// Walk through each core operation, lighting up the affected cells and showing Big-O.

export const traceOperationComplexity: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    "Let's tour the time complexity of every core array operation. Understanding these is the key to choosing arrays wisely.",
    {}, {}
  ));

  // Access O(1)
  const midIdx = Math.floor(arr.length / 2);
  const accessMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { accessMap[i] = i === midIdx ? "active" : "default"; });
  steps.push(step(
    arr, accessMap, { [midIdx]: "idx" },
    `Read arr[${midIdx}]: jump directly by address. O(1) — constant time regardless of length.`,
    { i: midIdx },
    { operation: "read/write by index", complexity: "O(1)" }
  ));

  // Append O(1) amortized
  const appendMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { appendMap[i] = i === arr.length - 1 ? "found" : "sorted"; });
  steps.push(step(
    arr, appendMap, { [arr.length - 1]: "end" },
    `Append to end: write at size, increment size. O(1) amortized — occasional realloc is amortized over many appends.`,
    {},
    { operation: "append (push)", complexity: "O(1) amortized" }
  ));

  // Insert at front O(n)
  const insertFrontMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { insertFrontMap[i] = "active"; });
  insertFrontMap[0] = "swap-a";
  steps.push(step(
    arr, insertFrontMap, { 0: "pos 0" },
    `Insert at index 0 (front): every element must shift right by one — O(n). The more elements, the costlier.`,
    {},
    { operation: "insert at front", complexity: "O(n)" }
  ));

  // Delete at front O(n)
  const delFrontMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { delFrontMap[i] = "comparing"; });
  delFrontMap[0] = "swap-b";
  steps.push(step(
    arr, delFrontMap, { 0: "del" },
    `Delete at index 0: every remaining element shifts left — O(n). Same cost as insert at front.`,
    {},
    { operation: "delete at front", complexity: "O(n)" }
  ));

  // Search O(n) unsorted
  const searchMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { searchMap[i] = i < 3 ? "eliminated" : i === 3 ? "found" : "default"; });
  steps.push(step(
    arr, searchMap, { 3: "target" },
    `Search in unsorted array: must scan each element — O(n) worst case. Binary search on sorted arrays reduces this to O(log n).`,
    {},
    { operation: "search (unsorted)", complexity: "O(n)", "sorted version": "O(log n)" }
  ));

  // Summary
  const doneMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { doneMap[i] = "sorted"; });
  steps.push(step(
    arr, doneMap, {},
    "Summary: arrays excel at O(1) access and O(1) amortized appends. They struggle with O(n) insertions/deletions at arbitrary positions.",
    {},
    {
      "access/update": "O(1)",
      "append": "O(1) amortized",
      "insert/delete middle": "O(n)",
      "search unsorted": "O(n)",
      "search sorted": "O(log n)",
    }
  ));

  return steps;
};

// ─── 11. Reverse an Array ─────────────────────────────────────────────────────
// Two-pointer in-place reversal: swap symmetrically from both ends toward the center.

export const traceReverseArray: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `Reverse an array in-place using two pointers. Left starts at index 0, right starts at the last index. We swap and walk inward.`,
    {}, { left: 0, right: arr.length - 1 }
  ));

  let l = 0;
  let r = arr.length - 1;

  while (l < r) {
    // Highlight current pair
    const highlightMap: Record<number, CellState> = {};
    arr.forEach((_, i) => {
      if (i < l || i > r) highlightMap[i] = "sorted";
      else if (i === l) highlightMap[i] = "swap-a";
      else if (i === r) highlightMap[i] = "swap-b";
      else highlightMap[i] = "default";
    });
    steps.push(step(
      arr, highlightMap, { [l]: "left", [r]: "right" },
      `Swap arr[${l}] = ${arr[l]} with arr[${r}] = ${arr[r]}.`,
      { left: l, right: r },
      { "arr[left]": arr[l], "arr[right]": arr[r] }
    ));

    // Perform swap
    [arr[l], arr[r]] = [arr[r], arr[l]];
    const swappedMap: Record<number, CellState> = {};
    arr.forEach((_, i) => {
      if (i < l || i > r) swappedMap[i] = "sorted";
      else if (i === l) swappedMap[i] = "found";
      else if (i === r) swappedMap[i] = "found";
      else swappedMap[i] = "default";
    });
    steps.push(step(
      arr, swappedMap, { [l]: "✓", [r]: "✓" },
      `Swapped. arr[${l}] is now ${arr[l]}, arr[${r}] is now ${arr[r]}. Move pointers inward.`,
      { left: l, right: r },
      { "arr[left]": arr[l], "arr[right]": arr[r] }
    ));

    l++;
    r--;
  }

  const doneMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { doneMap[i] = "sorted"; });
  steps.push(step(
    arr, doneMap, {},
    `Array reversed in-place. Every pair swapped once — O(n/2) = O(n) time, O(1) space. No extra array allocated.`,
    {},
    { "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── 12. Find Min / Max ───────────────────────────────────────────────────────
// Single linear scan, tracking the running minimum and maximum.

export const traceFindMinMax: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  let min = arr[0];
  let max = arr[0];

  steps.push(step(
    arr, { 0: "min-tracked" }, { 0: "start" },
    `Initialize min = max = arr[0] = ${arr[0]}. We'll update both as we scan right.`,
    { i: 0 },
    { min, max }
  ));

  for (let i = 1; i < arr.length; i++) {
    const checkMap: Record<number, CellState> = {};
    arr.forEach((_, j) => {
      if (j < i) checkMap[j] = arr[j] === min ? "min-tracked" : arr[j] === max ? "max-tracked" : "eliminated";
      else if (j === i) checkMap[j] = "comparing";
      else checkMap[j] = "default";
    });
    steps.push(step(
      arr, checkMap, { [i]: "i" },
      `Check arr[${i}] = ${arr[i]}. Is it < min (${min})? ${arr[i] < min ? "Yes — update min." : "No."} Is it > max (${max})? ${arr[i] > max ? "Yes — update max." : "No."}`,
      { i },
      { "arr[i]": arr[i], min, max }
    ));

    if (arr[i] < min) min = arr[i];
    if (arr[i] > max) max = arr[i];

    const minIdx = arr.indexOf(min);
    const maxIdx = arr.lastIndexOf(max);
    const afterMap: Record<number, CellState> = {};
    arr.forEach((_, j) => {
      if (j <= i) {
        if (j === minIdx) afterMap[j] = "min-tracked";
        else if (j === maxIdx) afterMap[j] = "max-tracked";
        else afterMap[j] = "eliminated";
      } else {
        afterMap[j] = "default";
      }
    });
    steps.push(step(
      arr, afterMap, { [minIdx]: "min", [maxIdx]: "max" },
      `Updated: min = ${min}, max = ${max}.`,
      { minIdx, maxIdx },
      { min, max, scanned: i + 1 }
    ));
  }

  const finalMinIdx = arr.indexOf(min);
  const finalMaxIdx = arr.lastIndexOf(max);
  const finalMap: Record<number, CellState> = {};
  arr.forEach((_, i) => {
    if (i === finalMinIdx) finalMap[i] = "min-tracked";
    else if (i === finalMaxIdx) finalMap[i] = "max-tracked";
    else finalMap[i] = "sorted";
  });
  steps.push(step(
    arr, finalMap, { [finalMinIdx]: "min", [finalMaxIdx]: "max" },
    `Scan complete. Minimum = ${min} (index ${finalMinIdx}), Maximum = ${max} (index ${finalMaxIdx}). One pass → O(n) time, O(1) space.`,
    {},
    { min, max, "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── 13. Second Largest ───────────────────────────────────────────────────────
// Find the second distinct largest without sorting — single scan with two trackers.

export const traceSecondLargest: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  let first = -Infinity;
  let second = -Infinity;

  steps.push(step(
    arr, {}, {},
    `Find the second largest element in one pass. Track 'first' (largest seen) and 'second' (second largest seen). Both start at -∞.`,
    {},
    { first: "−∞", second: "−∞" }
  ));

  for (let i = 0; i < arr.length; i++) {
    const scanMap: Record<number, CellState> = {};
    arr.forEach((_, j) => {
      if (j < i) scanMap[j] = "sorted";
      else if (j === i) scanMap[j] = "active";
      else scanMap[j] = "default";
    });
    steps.push(step(
      arr, scanMap, { [i]: "i" },
      `Examine arr[${i}] = ${arr[i]}.`,
      { i },
      { "arr[i]": arr[i], first: first === -Infinity ? "−∞" : first, second: second === -Infinity ? "−∞" : second }
    ));

    if (arr[i] > first) {
      second = first;
      first = arr[i];
      const updateMap: Record<number, CellState> = {};
      arr.forEach((_, j) => {
        if (j < i) updateMap[j] = "sorted";
        else if (j === i) updateMap[j] = "max-tracked";
        else updateMap[j] = "default";
      });
      steps.push(step(
        arr, updateMap, { [i]: "first" },
        `${arr[i]} > first (${second === -Infinity ? "−∞" : second}). Old first becomes second. New first = ${arr[i]}.`,
        { i },
        { first, second: second === -Infinity ? "−∞" : second }
      ));
    } else if (arr[i] > second && arr[i] !== first) {
      second = arr[i];
      const updateMap: Record<number, CellState> = {};
      arr.forEach((_, j) => {
        if (j < i) updateMap[j] = "sorted";
        else if (j === i) updateMap[j] = "comparing";
        else updateMap[j] = "default";
      });
      steps.push(step(
        arr, updateMap, { [i]: "second" },
        `${arr[i]} is between second and first. Update second = ${arr[i]}.`,
        { i },
        { first, second }
      ));
    }
  }

  const result = second === -Infinity ? "none (all equal)" : second;
  const allDone: Record<number, CellState> = {};
  arr.forEach((_, i) => { allDone[i] = arr[i] === first ? "max-tracked" : arr[i] === second ? "min-tracked" : "sorted"; });
  steps.push(step(
    arr, allDone, {},
    `Second largest = ${result}. Single scan, two variables — O(n) time, O(1) space. No sorting needed.`,
    {},
    { "largest": first, "second largest": result, "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── 14. Check Sorted ─────────────────────────────────────────────────────────
// Verify ascending order with one pass: if any adjacent pair is out of order, return false.

export const traceCheckSorted: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, {}, {},
    `Check if this array is sorted in ascending order. Compare each adjacent pair arr[i] vs arr[i+1] — one violation means "not sorted".`,
    {}, { sorted: "?" }
  ));

  for (let i = 0; i < arr.length - 1; i++) {
    const pairMap: Record<number, CellState> = {};
    arr.forEach((_, j) => {
      if (j < i) pairMap[j] = "sorted";
      else if (j === i || j === i + 1) pairMap[j] = "comparing";
      else pairMap[j] = "default";
    });
    steps.push(step(
      arr, pairMap, { [i]: "i", [i + 1]: "i+1" },
      `Compare arr[${i}] = ${arr[i]} and arr[${i + 1}] = ${arr[i + 1]}: ${arr[i]} <= ${arr[i + 1]}? ${arr[i] <= arr[i + 1] ? "Yes ✓" : "No ✗ — not sorted!"}`,
      { i },
      { "arr[i]": arr[i], "arr[i+1]": arr[i + 1] }
    ));

    if (arr[i] > arr[i + 1]) {
      const violationMap: Record<number, CellState> = {};
      arr.forEach((_, j) => {
        if (j < i) violationMap[j] = "sorted";
        else if (j === i || j === i + 1) violationMap[j] = "pivot";
        else violationMap[j] = "default";
      });
      steps.push(step(
        arr, violationMap, { [i]: "!", [i + 1]: "!" },
        `arr[${i}] = ${arr[i]} > arr[${i + 1}] = ${arr[i + 1]}. Sorted order violated! Return false immediately — no need to check the rest.`,
        {},
        { sorted: false, "violation at": i }
      ));
      return steps;
    }
  }

  const doneMap: Record<number, CellState> = {};
  arr.forEach((_, i) => { doneMap[i] = "sorted"; });
  steps.push(step(
    arr, doneMap, {},
    `All adjacent pairs satisfy arr[i] <= arr[i+1]. The array is sorted! O(n) time, O(1) space.`,
    {},
    { sorted: true, "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── 15. Remove Duplicates from Sorted ────────────────────────────────────────
// Classic write-pointer technique: read pointer scans, write pointer marks unique elements.

export const traceRemoveDuplicates: TracerFn = ({ array }: TracerInput) => {
  // Build a sorted array with some duplicates for demonstration
  const base = [...array].sort((a, b) => a - b);
  // Ensure duplicates exist
  const arr: number[] = [];
  base.forEach((v, i) => {
    arr.push(v);
    if (i % 2 === 0 && i + 1 < base.length) arr.push(v);
  });
  arr.splice(8); // keep reasonable length

  const steps: VisualizationStep[] = [];

  steps.push(step(
    arr, { 0: "write-ptr" }, { 0: "w" },
    `Remove duplicates in-place from a sorted array. Write pointer w starts at 0. Read pointer r scans forward.`,
    { w: 0, r: 1 },
    { w: 0, r: 1, "unique count": 1 }
  ));

  let w = 0;

  for (let r = 1; r < arr.length; r++) {
    const scanMap: Record<number, CellState> = {};
    arr.forEach((_, j) => {
      if (j < w) scanMap[j] = "sorted";
      else if (j === w) scanMap[j] = "write-ptr";
      else if (j === r) scanMap[j] = "active";
      else scanMap[j] = "default";
    });
    steps.push(step(
      arr, scanMap, { [w]: "w", [r]: "r" },
      `r = ${r}: read arr[${r}] = ${arr[r]}. Compare with arr[w] = arr[${w}] = ${arr[w]}.`,
      { w, r },
      { "arr[w]": arr[w], "arr[r]": arr[r] }
    ));

    if (arr[r] !== arr[w]) {
      w++;
      arr[w] = arr[r];
      const writeMap: Record<number, CellState> = {};
      arr.forEach((_, j) => {
        if (j < w) writeMap[j] = "sorted";
        else if (j === w) writeMap[j] = "write-ptr";
        else writeMap[j] = "eliminated";
      });
      steps.push(step(
        [...arr], writeMap, { [w]: "w", [r]: "r" },
        `${arr[r]} is new. Advance w to ${w} and copy value there. Unique element placed.`,
        { w, r },
        { "new unique": arr[w], "unique count": w + 1 }
      ));
    } else {
      const skipMap: Record<number, CellState> = {};
      arr.forEach((_, j) => {
        if (j < w) skipMap[j] = "sorted";
        else if (j === w) skipMap[j] = "write-ptr";
        else if (j === r) skipMap[j] = "eliminated";
        else skipMap[j] = "default";
      });
      steps.push(step(
        arr, skipMap, { [w]: "w", [r]: "r" },
        `${arr[r]} === arr[w] = ${arr[w]} — duplicate! Skip it. w stays at ${w}.`,
        { w, r },
        { skipped: arr[r] }
      ));
    }
  }

  const finalMap: Record<number, CellState> = {};
  arr.forEach((_, i) => {
    if (i <= w) finalMap[i] = "found";
    else finalMap[i] = "eliminated";
  });
  steps.push(step(
    arr, finalMap, {},
    `Done. First ${w + 1} slots hold all unique values. O(n) time, O(1) extra space — only two pointers used.`,
    {},
    { "unique count": w + 1, "original length": arr.length, "time complexity": "O(n)", "space complexity": "O(1)" }
  ));

  return steps;
};

// ─── Concept registry ─────────────────────────────────────────────────────────

export type ModuleId =
  | "foundations"
  | "core-operations"
  | "two-pointers"
  | "prefix-sum"
  | "sliding-window"
  | "hashing"
  | "kadane"
  | "binary-search"
  | "sorting"
  | "advanced"
  | "matrix"
  | "interview-thinking";

export type LessonType = "foundation" | "pattern" | "flagship";

export interface ArrayConcept {
  id: string;
  title: string;
  tagline: string;
  explanation: string;
  tracer: TracerFn;
  defaultArray: number[];
  defaultTarget?: number;
  showTargetInput: "none" | "index" | "value";
  targetLabel: string;
  algorithmSteps: string[];
  timeComplexity: string;
  spaceComplexity: string;
  complexityNote: string;
  // ── Module system ────────────────────────────────
  module: ModuleId;
  lessonType: LessonType;
  lessonNumber: number;            // 1-indexed within the full 110-lesson sequence
  // ── Richer content ──────────────────────────────
  mentalModel: string;             // one-sentence intuition hook
  whyItMatters: string;            // real-world relevance
  intuition: string;               // deeper conceptual explanation
  commonMistakes: string[];        // top 2-3 pitfalls
  patternConnection?: string;      // how this leads to the next module/pattern
  practiceSlug?: string;           // maps to a problem in the DB
  // ── Content provenance ──────────────────────────
  contentOrigin: "original";
  competitorDerived: false;
}

export const ARRAY_CONCEPTS: ArrayConcept[] = [
  // ── Module 1: Foundations ──────────────────────────────────────────────────
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
    module: "foundations",
    lessonType: "foundation",
    lessonNumber: 1,
    mentalModel: "An array is a numbered row of mailboxes — jump straight to box #42 without checking any other box.",
    whyItMatters: "O(1) access is why arrays underpin databases (heap files), image buffers, and network packet processing — anywhere random access speed matters.",
    intuition: "Memory is a long tape of bytes. An array claims a contiguous block. Since every element is the same size, computing the address of arr[i] takes exactly one multiplication and one addition — no matter if i is 0 or one million.",
    commonMistakes: [
      "Off-by-one: forgetting indices are 0-based (arr[length] is out of bounds).",
      "Confusing index with value: arr[3] means 'slot 3', not 'the value 3'.",
    ],
    patternConnection: "Once you internalize O(1) access, traversal (lesson 2) becomes obvious — you're just using that same access in a loop.",
    contentOrigin: "original",
    competitorDerived: false,
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
    module: "foundations",
    lessonType: "foundation",
    lessonNumber: 2,
    mentalModel: "Walking down the mailbox row, opening every door in sequence.",
    whyItMatters: "Every algorithm that needs to 'look at all the data' is a traversal at heart — sums, averages, validation, transformations.",
    intuition: "Since there is no shortcut to knowing every element without looking at it, traversal is irreducibly O(n). The good news: you never need more than a loop counter.",
    commonMistakes: [
      "Using i <= arr.length instead of i < arr.length — accesses one slot past the end.",
      "Modifying the array while traversing — can skip elements or cause index drift.",
    ],
    patternConnection: "Traversal is the skeleton of linear search (lesson 5) and every prefix-sum computation.",
    contentOrigin: "original",
    competitorDerived: false,
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
    module: "foundations",
    lessonType: "foundation",
    lessonNumber: 3,
    mentalModel: "Knowing a hotel room number: you go straight to floor 4, room 12 — you don't check every room.",
    whyItMatters: "Random access is why arrays beat linked lists for lookup-heavy workloads — no pointer chasing, just address arithmetic.",
    intuition: "The CPU doesn't know about 'arrays' — it knows about addresses. An array access is just an address calculation. That's why the language and hardware do it in one instruction.",
    commonMistakes: [
      "Accessing a negative index — wraps around in some languages (Python), crashes in others (Java).",
      "Forgetting bounds checking in C/C++ — silent memory corruption.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
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
    module: "foundations",
    lessonType: "foundation",
    lessonNumber: 4,
    mentalModel: "Erasing and rewriting a single mailbox label — neighbors are untouched.",
    whyItMatters: "In-place updates drive every space-efficient algorithm: sorting in-place, two-pointer techniques, and the write-pointer deduplication pattern all depend on cheap writes.",
    intuition: "A write is the same instruction set as a read, just with the data flow reversed. One address calculation, one store operation.",
    commonMistakes: [
      "Confusing mutation with returning a new array — in Java/Python, arr[i] = x modifies the original.",
      "Updating while iterating and then re-reading the updated value — logic errors in-place.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
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
    module: "foundations",
    lessonType: "foundation",
    lessonNumber: 5,
    mentalModel: "Scanning a shuffled card deck one card at a time looking for the ace of spades.",
    whyItMatters: "Real data is often unsorted or arrives in unknown order — logs, sensor feeds, user input. Linear search is your reliable baseline before you can sort or hash.",
    intuition: "Without any ordering guarantee, every element is equally likely to be the target or the next step to it. You have no choice but to check each one.",
    commonMistakes: [
      "Early-exit on duplicates: indexOf finds only the first; a custom loop is needed for all occurrences.",
      "Forgetting to return -1 — leaving the caller with undefined behavior when not found.",
    ],
    patternConnection: "If the array were sorted, you could cut the search to O(log n) using binary search (Module 8). Hashing (Module 6) gives O(1) average search at the cost of extra space.",
    practiceSlug: "repeated-sensor-reading",
    contentOrigin: "original",
    competitorDerived: false,
  },

  // ── Module 2: Core Operations ──────────────────────────────────────────────
  {
    id: "insert-at-position",
    title: "Insert at Position",
    tagline: "Making room by shifting elements right",
    explanation:
      "Inserting at an arbitrary index requires shifting every element from that index onward one step to the right to create space, then writing the new value. The cost grows with how many elements follow the insertion point.",
    tracer: traceInsertAtPosition,
    defaultArray: [10, 25, 31, 42, 57, 81],
    defaultTarget: 2,
    showTargetInput: "index",
    targetLabel: "Insert at index",
    algorithmSteps: [
      "Identify target index pos.",
      "Start from the last element and shift each element right by one (i from length-1 down to pos).",
      "Write the new value at arr[pos].",
      "Increment the logical size by 1.",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "Inserting at index 0 shifts all n elements — worst case O(n). Appending to the end is O(1) amortized.",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 6,
    mentalModel: "Inserting a new book into a shelf: you push every book to its right one slot, then slot in the new one.",
    whyItMatters: "Understanding insertion cost explains why ArrayList.add(0, x) is slow in Java — and why deques or linked lists are better for frequent front insertions.",
    intuition: "You can't teleport elements; they must move. The further left you insert, the more moves required. This is inherently O(n) — and it's why append-only patterns are so common in high-performance code.",
    commonMistakes: [
      "Shifting left-to-right (overwriting elements before moving them) — must shift from right to left.",
      "Forgetting to check capacity before inserting — arrays don't auto-grow in languages like Java arrays (not ArrayList).",
    ],
    patternConnection: "The cost of insertion motivates the write-pointer technique (lesson 15) — instead of shifting, you write only unique/valid values forward.",
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "delete-from-position",
    title: "Delete from Position",
    tagline: "Closing gaps by shifting elements left",
    explanation:
      "Deletion at an index removes one element and shifts everything after it left by one, closing the gap. Like insertion, the cost depends on how many elements follow the target index.",
    tracer: traceDeleteFromPosition,
    defaultArray: [10, 25, 31, 42, 57, 81],
    defaultTarget: 2,
    showTargetInput: "index",
    targetLabel: "Delete at index",
    algorithmSteps: [
      "Identify target index pos.",
      "Starting at pos, copy arr[i+1] into arr[i] for each i from pos to length-2.",
      "Decrement the logical size by 1.",
      "Optionally zero out the last slot.",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "Deleting from index 0 shifts n-1 elements — O(n). Deleting the last element is O(1).",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 7,
    mentalModel: "Removing a book from a shelf: slide every book to its right one slot left to close the gap.",
    whyItMatters: "Deletion cost matters for algorithms that remove 'processed' or 'invalid' elements in-place — knowing the cost helps you choose between shifting vs. swap-to-end vs. write-pointer approaches.",
    intuition: "The gap left by deletion is filled by moving the right-hand neighbors in sequence. Left-to-right shift is safe here — unlike insertion, we overwrite the gap, not the source.",
    commonMistakes: [
      "Shifting right-to-left (opposite of insertion) — accidentally overwrites elements.",
      "Not decrementing size: the 'deleted' value is still in memory at the old last slot, but logically the array is shorter.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "size-vs-capacity",
    title: "Size vs Capacity",
    tagline: "Understanding allocated space vs. used space",
    explanation:
      "Size is how many elements the array currently holds. Capacity is how many slots the backing store has allocated. Dynamic arrays grow by doubling capacity when size reaches capacity, amortizing the O(n) copy over many O(1) appends.",
    tracer: traceSizeVsCapacity,
    defaultArray: [10, 25, 31, 42, 57],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "size tracks how many elements are stored.",
      "capacity is the total allocated slots.",
      "Append: if size < capacity, write at arr[size] and increment size — O(1).",
      "If size === capacity, allocate new array (2× capacity), copy all elements, free old — O(n).",
      "Amortized cost of n appends = O(n), so each append is O(1) amortized.",
    ],
    timeComplexity: "O(1) amortized",
    spaceComplexity: "O(n)",
    complexityNote: "Individual appends are O(1) amortized. The backing store uses O(n) space — up to 2× the logical size.",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 8,
    mentalModel: "A hotel that pre-books double the rooms needed — cheap to check in until the floor fills up, then the whole hotel moves to a bigger building.",
    whyItMatters: "ArrayList.add() in Java looks O(1) in the docs — the asterisk is 'amortized'. Knowing this helps you reason about worst-case performance spikes and pre-size collections when you know the target size.",
    intuition: "Doubling strategy: after n appends, the total number of copies performed is n + n/2 + n/4 + ... = 2n. So 2n copies for n appends = O(1) amortized per append. The doubling ensures the expensive copy happens exponentially less often.",
    commonMistakes: [
      "Confusing size (logical) with length (backing array length) — Java's ArrayList.size() vs internal array.length.",
      "Not pre-sizing when you know the count — causes unnecessary reallocations.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "fixed-vs-dynamic",
    title: "Fixed vs Dynamic Arrays",
    tagline: "When static size is a feature, not a limitation",
    explanation:
      "Fixed arrays allocate exactly N slots at declaration time and never resize — zero overhead, no indirection. Dynamic arrays (ArrayList, Python list) manage their own growth. The right choice depends on whether the size is known upfront.",
    tracer: traceFixedVsDynamic,
    defaultArray: [10, 25, 31, 42, 57],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Fixed: declare with a compile-time or runtime constant size. No resize possible.",
      "Dynamic: backed by a fixed array internally; doubles when full.",
      "Fixed has zero overhead; dynamic trades ~2× memory for flexibility.",
      "Use fixed when: size is known, memory is tight, or you want stack allocation.",
      "Use dynamic when: size varies or is unknown upfront.",
    ],
    timeComplexity: "O(1) access for both",
    spaceComplexity: "O(n) for fixed; O(n) for dynamic (up to 2× n)",
    complexityNote: "Both offer O(1) access. Dynamic arrays spend up to 2× the memory for O(1) amortized append.",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 9,
    mentalModel: "Fixed = a parking lot with exactly 50 spaces. Dynamic = a valet lot that builds a new level when full.",
    whyItMatters: "Interview problems often use int[] (fixed) in Java for tight memory control or known-size outputs. Understanding the tradeoff explains why StringBuilder (dynamic) beats String concatenation (new fixed array each time).",
    intuition: "The CPU is happiest with fixed arrays on the stack — no heap allocation, no GC pressure. Dynamic arrays are a layer of abstraction that pays for flexibility with indirection and occasional copying.",
    commonMistakes: [
      "Declaring a fixed array larger than needed and treating size as capacity — wastes memory and complicates code.",
      "Using new int[n] in Java and then calling .add() — int[] is fixed, ArrayList is dynamic; these are not interchangeable.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "operation-complexity",
    title: "Operation Complexity",
    tagline: "Big-O for every core array operation at a glance",
    explanation:
      "Arrays offer O(1) access/update, O(1) amortized append, and O(n) insert/delete/search. Understanding these is the foundation for choosing the right data structure in every interview and production problem.",
    tracer: traceOperationComplexity,
    defaultArray: [10, 25, 31, 42, 57, 81],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Access arr[i]: O(1) — direct address.",
      "Update arr[i]: O(1) — direct write.",
      "Append to end: O(1) amortized.",
      "Insert at index: O(n) — shift right.",
      "Delete at index: O(n) — shift left.",
      "Search unsorted: O(n) — scan.",
      "Search sorted: O(log n) — binary search.",
    ],
    timeComplexity: "Varies by operation",
    spaceComplexity: "O(1) for in-place operations",
    complexityNote: "Use the O(1) access as an anchor; everything else flows from the cost of shifting elements.",
    module: "core-operations",
    lessonType: "flagship",
    lessonNumber: 10,
    mentalModel: "A reference card: commit these five numbers to memory and you can reason about any array algorithm instantly.",
    whyItMatters: "Every technical interview involves choosing between O(1) and O(n) operations. Misremembering insert cost is one of the top interview mistakes.",
    intuition: "The unifying principle: any operation that requires touching k elements costs O(k). Access touches 1 element. Append touches 1 (or amortizes n copies over n appends). Insert/delete touch up to n. Search touches up to n.",
    commonMistakes: [
      "Forgetting that ArrayList.add(index, value) is O(n), not O(1).",
      "Treating amortized O(1) as guaranteed O(1) — a single append can be O(n) in the worst case.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "reverse-array",
    title: "Reverse an Array",
    tagline: "In-place reversal with two symmetric pointers",
    explanation:
      "Reverse the array by swapping the first and last elements, then the second and second-to-last, continuing until the two pointers meet in the middle. Half the elements are touched — O(n) time, O(1) space.",
    tracer: traceReverseArray,
    defaultArray: [10, 25, 31, 42, 57, 81],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Set left = 0, right = length - 1.",
      "While left < right: swap arr[left] and arr[right].",
      "Increment left, decrement right.",
      "Stop when left >= right (pointers crossed or met).",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "n/2 swaps — O(n) time. Only two index variables needed — O(1) space.",
    module: "core-operations",
    lessonType: "pattern",
    lessonNumber: 11,
    mentalModel: "Flipping a card deck: swap the top and bottom card, then the second and second-to-last, working inward.",
    whyItMatters: "Reversal appears as a sub-step in rotate-array, palindrome checks, and zigzag problems. It's your first real two-pointer pattern — the same pointer framework scales to hundreds of harder problems.",
    intuition: "Symmetry is the key insight: arr[i] and arr[n-1-i] are mirror pairs. You only need to swap the mirrors — no scratch space, no extra passes.",
    commonMistakes: [
      "Using left <= right instead of left < right — middle element in odd-length arrays gets swapped with itself (harmless but unnecessary).",
      "Creating a new reversed array instead of in-place — costs O(n) extra space.",
    ],
    patternConnection: "This is the simplest two-pointer pattern. Module 3 (Two Pointers) extends the same left/right pointer logic to pair-sum, container problems, and merge operations.",
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "find-min-max",
    title: "Find Min / Max",
    tagline: "Single-pass tracking with two running variables",
    explanation:
      "Track the running minimum and maximum in one pass by updating two variables as you scan. No sorting, no extra data structure — just two comparisons per element.",
    tracer: traceFindMinMax,
    defaultArray: [34, 12, 67, 5, 89, 23, 45],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Initialize min = max = arr[0].",
      "For each arr[i] from index 1 onward:",
      "  If arr[i] < min → min = arr[i].",
      "  If arr[i] > max → max = arr[i].",
      "Return min and max.",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "One pass, two comparisons per element — 2n comparisons total. Still O(n), with a constant factor of 2.",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 12,
    mentalModel: "Walking along a price-tag aisle: keep a sticky note for 'cheapest seen' and 'most expensive seen', updating as you go.",
    whyItMatters: "Min/max is a building block for range queries, normalization, and tournament-bracket problems. It also teaches the 'running variable' pattern used in Kadane's algorithm.",
    intuition: "You can't know the minimum without seeing every element (any unseen element might be smaller). So O(n) is optimal. The trick is doing it with O(1) space by carrying two 'best so far' values.",
    commonMistakes: [
      "Initializing min = 0 or max = 0 — breaks on all-negative or all-positive arrays. Always initialize to arr[0] or ±Infinity.",
      "Returning before seeing all elements — no early exit is valid for min/max.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "second-largest",
    title: "Second Largest",
    tagline: "Two running trackers instead of one",
    explanation:
      "Find the second distinct largest element in one pass using two variables: the current largest and the second largest. Update both carefully as you scan — no sorting needed.",
    tracer: traceSecondLargest,
    defaultArray: [34, 12, 67, 5, 89, 23, 45],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Initialize first = second = -Infinity.",
      "For each arr[i]:",
      "  If arr[i] > first: second = first; first = arr[i].",
      "  Else if arr[i] > second and arr[i] !== first: second = arr[i].",
      "Return second (-Infinity means no second distinct element).",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "Single scan, two comparisons per element — O(n). The key is handling the update order correctly to avoid missing the second largest.",
    module: "core-operations",
    lessonType: "pattern",
    lessonNumber: 13,
    mentalModel: "Tracking the gold and silver medal winner as athletes finish: when someone beats the gold, demote gold to silver and crown the newcomer.",
    whyItMatters: "This pattern extends to 'k-th largest' problems and teaches you to maintain a small, ordered set of variables — the conceptual precursor to using a min-heap for top-k.",
    intuition: "Two trackers are enough because you only care about rank 1 and rank 2. When a new value beats rank 1, the old rank 1 becomes rank 2. When it only beats rank 2, you update rank 2 directly.",
    commonMistakes: [
      "Updating second before first — misses the case where first and second both need updating in one step.",
      "Not checking arr[i] !== first — equal values should not count as distinct second-largest.",
    ],
    patternConnection: "Scaling to top-k (Module: Heap) uses a min-heap of size k instead of k individual variables.",
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "check-sorted",
    title: "Check Sorted",
    tagline: "One pass, one invariant: each pair must be non-decreasing",
    explanation:
      "Verify ascending order by checking each adjacent pair (arr[i], arr[i+1]). If any pair violates arr[i] <= arr[i+1], the array is not sorted. Return false on the first violation — no need to scan further.",
    tracer: traceCheckSorted,
    defaultArray: [10, 25, 31, 42, 57, 81],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "For i from 0 to length - 2:",
      "  If arr[i] > arr[i+1] → return false.",
      "Return true (no violation found).",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "Best case O(1) — first pair violates. Worst case O(n) — array is sorted or has violation at the end.",
    module: "core-operations",
    lessonType: "foundation",
    lessonNumber: 14,
    mentalModel: "Checking exam scores from lowest to highest: the moment you see a score higher than the next one, the list isn't sorted.",
    whyItMatters: "Sorted-order checks appear as guards before binary search, in merge validation, and in streaming systems that verify data arrives in order.",
    intuition: "Sorted order is a global property, but it can be checked locally: if every adjacent pair is non-decreasing, the whole array is non-decreasing. One violated pair is sufficient to disprove it.",
    commonMistakes: [
      "Using strict < instead of <= — flags equal adjacent elements as violations when they are valid in non-decreasing arrays.",
      "Checking i < length instead of i < length - 1 — accesses arr[length] which is out of bounds.",
    ],
    contentOrigin: "original",
    competitorDerived: false,
  },
  {
    id: "remove-duplicates",
    title: "Remove Duplicates",
    tagline: "Write-pointer compaction on a sorted array",
    explanation:
      "Use a write pointer w and a read pointer r. For each element r reads, write it at w only if it differs from the current w value. This compacts unique elements to the front in one pass — O(n) time, O(1) space.",
    tracer: traceRemoveDuplicates,
    defaultArray: [1, 1, 2, 3, 3, 4, 5, 5],
    showTargetInput: "none",
    targetLabel: "",
    algorithmSteps: [
      "Set write pointer w = 0.",
      "For read pointer r from 1 to length - 1:",
      "  If arr[r] !== arr[w]: increment w, set arr[w] = arr[r].",
      "Return w + 1 (count of unique elements).",
    ],
    timeComplexity: "O(n)",
    spaceComplexity: "O(1)",
    complexityNote: "r scans every element once. w only advances on unique values. Total operations: O(n). No extra array needed.",
    module: "core-operations",
    lessonType: "flagship",
    lessonNumber: 15,
    mentalModel: "Packing a suitcase by only placing an item if it's different from the last one packed — duplicates are left behind.",
    whyItMatters: "The write-pointer pattern appears in dozens of interview problems: remove elements equal to a value, compress runs, partition in-place. Mastering this pattern unlocks a whole class of O(n)/O(1) solutions.",
    intuition: "Since the array is sorted, duplicates are always adjacent. You never need to look back more than one position. The write pointer is always <= read pointer, so reads never overwrite unread data — the algorithm is safe.",
    commonMistakes: [
      "Using on an unsorted array — duplicates might not be adjacent, so the algorithm would miss them.",
      "Returning w instead of w + 1 — w is the last valid index, but length is w + 1.",
    ],
    patternConnection: "This write-pointer pattern is a special case of the two-pointer family (Module 3) and generalizes to 'remove all elements satisfying condition X' problems.",
    practiceSlug: "log-deduplicator",
    contentOrigin: "original",
    competitorDerived: false,
  },
];
