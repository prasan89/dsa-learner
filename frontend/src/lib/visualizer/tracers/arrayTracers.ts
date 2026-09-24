import type {
  VisualizationCell,
  VisualizationStep,
  TracerFn,
  TracerInput,
  CellState,
} from "../types";

// ── Helpers ───────────────────────────────────────────────────────────────────

function makeCell(
  arr: number[],
  index: number,
  overrides?: Partial<VisualizationCell>
): VisualizationCell {
  return {
    value: arr[index],
    index,
    state: "default",
    ...overrides,
  };
}

function snapshot(
  arr: number[],
  stateMap: Record<number, CellState> = {},
  labelMap: Record<number, string> = {}
): VisualizationCell[] {
  return arr.map((_, i) => ({
    value: arr[i],
    index: i,
    state: stateMap[i] ?? "default",
    label: labelMap[i],
  }));
}

function makeStep(
  arr: number[],
  stateMap: Record<number, CellState>,
  labelMap: Record<number, string>,
  message: string,
  pointers: Record<string, number> = {},
  variables: Record<string, number | string | boolean> = {}
): VisualizationStep {
  return {
    cells: snapshot(arr, stateMap, labelMap),
    pointers,
    variables,
    message,
  };
}

// ── Linear Scan ───────────────────────────────────────────────────────────────

export const traceLinearScan: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];
  const t = target ?? arr[0];

  steps.push(
    makeStep(arr, {}, {}, `Start linear scan for target ${t}.`, {}, { target: t })
  );

  for (let i = 0; i < arr.length; i++) {
    const stateMap: Record<number, CellState> = {};
    for (let j = 0; j < i; j++) stateMap[j] = "eliminated";
    stateMap[i] = "active";

    steps.push(
      makeStep(
        arr,
        stateMap,
        { [i]: "i" },
        `index ${i}: value is ${arr[i]}. Is ${arr[i]} === ${t}?`,
        { i },
        { target: t }
      )
    );

    if (arr[i] === t) {
      const foundMap: Record<number, CellState> = {};
      for (let j = 0; j < i; j++) foundMap[j] = "eliminated";
      foundMap[i] = "found";
      steps.push(
        makeStep(
          arr,
          foundMap,
          { [i]: "found" },
          `Found target ${t} at index ${i}!`,
          { i },
          { target: t, result: i }
        )
      );
      return steps;
    }
  }

  const allElim: Record<number, CellState> = {};
  arr.forEach((_, i) => (allElim[i] = "eliminated"));
  steps.push(
    makeStep(arr, allElim, {}, `Target ${t} not found in array.`, {}, { target: t, result: -1 })
  );
  return steps;
};

// ── Binary Search ─────────────────────────────────────────────────────────────

export const traceBinarySearch: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array].sort((a, b) => a - b);
  const steps: VisualizationStep[] = [];
  const t = target ?? arr[Math.floor(arr.length / 2)];

  let left = 0;
  let right = arr.length - 1;

  steps.push(
    makeStep(
      arr,
      {},
      { [left]: "left", [right]: "right" },
      `Binary search for ${t}. Array must be sorted.`,
      { left, right },
      { target: t }
    )
  );

  while (left <= right) {
    const mid = left + Math.floor((right - left) / 2);
    const stateMap: Record<number, CellState> = {};
    const labelMap: Record<number, string> = {};

    for (let i = 0; i < left; i++) stateMap[i] = "eliminated";
    for (let i = right + 1; i < arr.length; i++) stateMap[i] = "eliminated";
    for (let i = left; i <= right; i++) stateMap[i] = "default";
    stateMap[mid] = "comparing";
    labelMap[left] = "left";
    labelMap[right] = "right";
    if (mid !== left && mid !== right) labelMap[mid] = "mid";
    else labelMap[mid] = mid === left ? "left/mid" : "right/mid";

    steps.push(
      makeStep(
        arr,
        stateMap,
        labelMap,
        `mid=${mid} → arr[${mid}]=${arr[mid]}. Compare with target ${t}.`,
        { left, mid, right },
        { target: t }
      )
    );

    if (arr[mid] === t) {
      const foundMap = { ...stateMap };
      foundMap[mid] = "found";
      steps.push(
        makeStep(
          arr,
          foundMap,
          { ...labelMap, [mid]: "found" },
          `Found ${t} at index ${mid}!`,
          { left, mid, right },
          { target: t, result: mid }
        )
      );
      return steps;
    } else if (arr[mid] < t) {
      steps.push(
        makeStep(
          arr,
          stateMap,
          labelMap,
          `arr[${mid}]=${arr[mid]} < ${t}. Discard left half. left = mid+1 = ${mid + 1}.`,
          { left, mid, right },
          { target: t }
        )
      );
      left = mid + 1;
    } else {
      steps.push(
        makeStep(
          arr,
          stateMap,
          labelMap,
          `arr[${mid}]=${arr[mid]} > ${t}. Discard right half. right = mid-1 = ${mid - 1}.`,
          { left, mid, right },
          { target: t }
        )
      );
      right = mid - 1;
    }
  }

  const allElim: Record<number, CellState> = {};
  arr.forEach((_, i) => (allElim[i] = "eliminated"));
  steps.push(
    makeStep(arr, allElim, {}, `Target ${t} not found. Search space exhausted.`, {}, { target: t, result: -1 })
  );
  return steps;
};

// ── Two Pointers ──────────────────────────────────────────────────────────────

export const traceTwoPointers: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array].sort((a, b) => a - b);
  const steps: VisualizationStep[] = [];
  const t = target ?? 0;

  let left = 0;
  let right = arr.length - 1;

  steps.push(
    makeStep(
      arr,
      { [left]: "left-pointer", [right]: "right-pointer" },
      { [left]: "left", [right]: "right" },
      `Two-pointer approach on sorted array. Find pair summing to ${t}.`,
      { left, right },
      { target: t }
    )
  );

  while (left < right) {
    const sum = arr[left] + arr[right];
    const stateMap: Record<number, CellState> = { [left]: "left-pointer", [right]: "right-pointer" };
    const labelMap: Record<number, string> = { [left]: "left", [right]: "right" };

    steps.push(
      makeStep(
        arr,
        stateMap,
        labelMap,
        `arr[${left}]+arr[${right}] = ${arr[left]}+${arr[right]} = ${sum}. Target is ${t}.`,
        { left, right },
        { target: t, sum }
      )
    );

    if (sum === t) {
      const foundMap: Record<number, CellState> = { [left]: "found", [right]: "found" };
      steps.push(
        makeStep(
          arr,
          foundMap,
          { [left]: "left", [right]: "right" },
          `Pair found! arr[${left}]=${arr[left]} + arr[${right}]=${arr[right]} = ${t}.`,
          { left, right },
          { target: t, result: `[${left}, ${right}]` }
        )
      );
      return steps;
    } else if (sum < t) {
      steps.push(
        makeStep(
          arr,
          stateMap,
          labelMap,
          `Sum ${sum} < ${t}. Move left pointer right to increase sum.`,
          { left, right },
          { target: t, sum }
        )
      );
      left++;
    } else {
      steps.push(
        makeStep(
          arr,
          stateMap,
          labelMap,
          `Sum ${sum} > ${t}. Move right pointer left to decrease sum.`,
          { left, right },
          { target: t, sum }
        )
      );
      right--;
    }
  }

  steps.push(
    makeStep(arr, {}, {}, `No pair found that sums to ${t}.`, {}, { target: t, result: "none" })
  );
  return steps;
};

// ── Sliding Window ────────────────────────────────────────────────────────────

export const traceSlidingWindow: TracerFn = ({ array, target }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];
  const k = target ?? 3; // window size

  steps.push(
    makeStep(arr, {}, {}, `Sliding window of size ${k}. Find maximum sum subarray.`, {}, { k })
  );

  // Compute initial window
  let windowSum = 0;
  for (let i = 0; i < Math.min(k, arr.length); i++) windowSum += arr[i];

  let maxSum = windowSum;
  let maxStart = 0;
  let left = 0;
  let right = k - 1;

  const buildWindowMap = (l: number, r: number): Record<number, CellState> => {
    const m: Record<number, CellState> = {};
    for (let i = 0; i < arr.length; i++) {
      if (i < l || i > r) m[i] = "default";
      else if (i === l) m[i] = "window-start";
      else if (i === r) m[i] = "window-end";
      else m[i] = "in-window";
    }
    return m;
  };

  steps.push(
    makeStep(
      arr,
      buildWindowMap(left, right),
      { [left]: "L", [right]: "R" },
      `Initial window [${left}..${right}]: sum = ${windowSum}.`,
      { left, right },
      { windowSum, maxSum, k }
    )
  );

  for (let i = k; i < arr.length; i++) {
    windowSum += arr[i];
    windowSum -= arr[i - k];
    left = i - k + 1;
    right = i;

    steps.push(
      makeStep(
        arr,
        buildWindowMap(left, right),
        { [left]: "L", [right]: "R" },
        `Slide window: add arr[${right}]=${arr[right]}, remove arr[${left - 1}]=${arr[left - 1]}. sum = ${windowSum}.`,
        { left, right },
        { windowSum, maxSum, k }
      )
    );

    if (windowSum > maxSum) {
      maxSum = windowSum;
      maxStart = left;
      steps.push(
        makeStep(
          arr,
          buildWindowMap(left, right),
          { [left]: "L", [right]: "R" },
          `New maximum! sum = ${maxSum} at window [${left}..${right}].`,
          { left, right },
          { windowSum, maxSum, k }
        )
      );
    }
  }

  const finalMap: Record<number, CellState> = {};
  for (let i = 0; i < arr.length; i++) {
    finalMap[i] = i >= maxStart && i < maxStart + k ? "found" : "eliminated";
  }
  steps.push(
    makeStep(
      arr,
      finalMap,
      { [maxStart]: "L", [maxStart + k - 1]: "R" },
      `Maximum sum subarray: [${maxStart}..${maxStart + k - 1}], sum = ${maxSum}.`,
      {},
      { maxSum, maxStart }
    )
  );

  return steps;
};

// ── Best Time to Buy and Sell Stock ───────────────────────────────────────────

export const traceBestTimeToBuyStock: TracerFn = ({ array }: TracerInput) => {
  const prices = [...array];
  const steps: VisualizationStep[] = [];

  let minPrice = prices[0];
  let minIdx = 0;
  let maxProfit = 0;
  let buyIdx = 0;
  let sellIdx = 0;

  steps.push(
    makeStep(
      prices,
      { [0]: "min-tracked" },
      { [0]: "min" },
      `Start. Track minimum price. minPrice = ${minPrice}, maxProfit = 0.`,
      { minIdx: 0 },
      { minPrice, maxProfit }
    )
  );

  for (let i = 1; i < prices.length; i++) {
    const profit = prices[i] - minPrice;
    const stateMap: Record<number, CellState> = {};
    const labelMap: Record<number, string> = {};

    stateMap[minIdx] = "min-tracked";
    labelMap[minIdx] = "buy";
    stateMap[i] = "active";
    labelMap[i] = "sell?";

    steps.push(
      makeStep(
        prices,
        stateMap,
        labelMap,
        `Day ${i}: price=${prices[i]}. Profit if sold = ${prices[i]} - ${minPrice} = ${profit}.`,
        { i, minIdx },
        { minPrice, profit, maxProfit }
      )
    );

    if (profit > maxProfit) {
      maxProfit = profit;
      buyIdx = minIdx;
      sellIdx = i;

      const profitMap: Record<number, CellState> = { ...stateMap };
      profitMap[i] = "max-tracked";
      steps.push(
        makeStep(
          prices,
          profitMap,
          { [buyIdx]: "buy", [sellIdx]: "sell" },
          `New max profit! Buy at day ${buyIdx} (${prices[buyIdx]}), sell at day ${sellIdx} (${prices[sellIdx]}). Profit = ${maxProfit}.`,
          { minIdx, i },
          { minPrice, maxProfit, buyIdx, sellIdx }
        )
      );
    }

    if (prices[i] < minPrice) {
      minPrice = prices[i];
      minIdx = i;
      steps.push(
        makeStep(
          prices,
          { [minIdx]: "min-tracked" },
          { [minIdx]: "min" },
          `Day ${i}: price ${prices[i]} is new minimum. Update minPrice = ${minPrice}.`,
          { minIdx },
          { minPrice, maxProfit }
        )
      );
    }
  }

  const finalMap: Record<number, CellState> = {};
  prices.forEach((_, i) => (finalMap[i] = "eliminated"));
  if (maxProfit > 0) {
    finalMap[buyIdx] = "found";
    finalMap[sellIdx] = "found";
  }
  steps.push(
    makeStep(
      prices,
      finalMap,
      maxProfit > 0 ? { [buyIdx]: "buy", [sellIdx]: "sell" } : {},
      maxProfit > 0
        ? `Done. Max profit = ${maxProfit}. Buy day ${buyIdx}, sell day ${sellIdx}.`
        : `Done. Prices only decrease. Max profit = 0.`,
      {},
      { maxProfit, buyIdx, sellIdx }
    )
  );

  return steps;
};

// ── Maximum Subarray (Kadane's) ───────────────────────────────────────────────

export const traceMaximumSubarray: TracerFn = ({ array }: TracerInput) => {
  const arr = [...array];
  const steps: VisualizationStep[] = [];

  let maxSoFar = arr[0];
  let maxEndingHere = arr[0];
  let start = 0;
  let end = 0;
  let tempStart = 0;

  steps.push(
    makeStep(
      arr,
      { [0]: "active" },
      { [0]: "i" },
      `Kadane's algorithm. maxSoFar = ${maxSoFar}, maxEndingHere = ${maxEndingHere}.`,
      { start: 0, end: 0 },
      { maxSoFar, maxEndingHere }
    )
  );

  for (let i = 1; i < arr.length; i++) {
    const extendPrev = maxEndingHere + arr[i];
    const startFresh = arr[i];

    const stateMap: Record<number, CellState> = {};
    const labelMap: Record<number, string> = {};
    for (let j = tempStart; j < i; j++) stateMap[j] = "in-window";
    stateMap[i] = "comparing";
    labelMap[tempStart] = "L";
    labelMap[i] = "i";

    steps.push(
      makeStep(
        arr,
        stateMap,
        labelMap,
        `i=${i}: extend (${maxEndingHere}+${arr[i]}=${extendPrev}) vs start fresh (${startFresh}).`,
        { i, tempStart, start, end },
        { maxSoFar, maxEndingHere }
      )
    );

    if (startFresh > extendPrev) {
      maxEndingHere = startFresh;
      tempStart = i;
      steps.push(
        makeStep(
          arr,
          { [i]: "active" },
          { [i]: "L/i" },
          `Starting fresh subarray at index ${i}. maxEndingHere = ${maxEndingHere}.`,
          { i, tempStart },
          { maxSoFar, maxEndingHere }
        )
      );
    } else {
      maxEndingHere = extendPrev;
      steps.push(
        makeStep(
          arr,
          { ...stateMap, [i]: "active" },
          { ...labelMap, [i]: "i" },
          `Extend subarray to index ${i}. maxEndingHere = ${maxEndingHere}.`,
          { i, tempStart },
          { maxSoFar, maxEndingHere }
        )
      );
    }

    if (maxEndingHere > maxSoFar) {
      maxSoFar = maxEndingHere;
      start = tempStart;
      end = i;
      steps.push(
        makeStep(
          arr,
          { ...stateMap, [i]: "max-tracked" },
          { [start]: "L", [end]: "R" },
          `New maximum! subarray [${start}..${end}] sums to ${maxSoFar}.`,
          { start, end },
          { maxSoFar, maxEndingHere }
        )
      );
    }
  }

  const finalMap: Record<number, CellState> = {};
  arr.forEach((_, i) => (finalMap[i] = i >= start && i <= end ? "found" : "eliminated"));
  steps.push(
    makeStep(
      arr,
      finalMap,
      { [start]: "L", [end]: "R" },
      `Maximum subarray is arr[${start}..${end}] with sum ${maxSoFar}.`,
      { start, end },
      { maxSoFar }
    )
  );

  return steps;
};

// ── Registry ──────────────────────────────────────────────────────────────────

export const ARRAY_TRACERS: Record<string, TracerFn> = {
  // by problem slug
  "linear-scan":                    traceLinearScan,
  "binary-search-problem":          traceBinarySearch,
  "two-sum":                        traceTwoPointers,
  "container-with-most-water":      traceTwoPointers,
  "best-time-to-buy-sell-stock":    traceBestTimeToBuyStock,
  "maximum-subarray":               traceMaximumSubarray,
  "find-maximum-subarray":          traceMaximumSubarray,
  "sliding-window-maximum":         traceSlidingWindow,
  "maximum-average-subarray-i":     traceSlidingWindow,
  // generic fallbacks by concept
  "binary-search":                  traceBinarySearch,
  "two-pointers":                   traceTwoPointers,
  "sliding-window":                 traceSlidingWindow,
};

export const DEFAULT_TRACER: TracerFn = traceLinearScan;
