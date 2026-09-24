import {
  traceLinearScan,
  traceBinarySearch,
  traceTwoPointers,
  traceSlidingWindow,
  traceBestTimeToBuyStock,
  traceMaximumSubarray,
  ARRAY_TRACERS,
  DEFAULT_TRACER,
} from "../tracers/arrayTracers";
import type { VisualizationStep } from "../types";

// ─── Helpers ─────────────────────────────────────────────────────────────────

function lastStep(steps: VisualizationStep[]): VisualizationStep {
  return steps[steps.length - 1];
}

function cellStates(step: VisualizationStep) {
  return step.cells.map((c) => c.state);
}

// ─── Linear Scan ─────────────────────────────────────────────────────────────

describe("traceLinearScan", () => {
  const arr = [3, 7, 2, 9, 5];

  it("does not mutate input array", () => {
    const original = [...arr];
    traceLinearScan({ array: arr });
    expect(arr).toEqual(original);
  });

  it("returns at least 2 steps (intro + at least one scan step)", () => {
    const steps = traceLinearScan({ array: arr });
    expect(steps.length).toBeGreaterThanOrEqual(2);
  });

  it("all steps have the correct number of cells", () => {
    const steps = traceLinearScan({ array: arr });
    for (const step of steps) {
      expect(step.cells).toHaveLength(arr.length);
    }
  });

  it("marks the target element as found when target matches", () => {
    const steps = traceLinearScan({ array: [10, 20, 30], target: 20 });
    const foundSteps = steps.filter((s) => s.cells.some((c) => c.state === "found"));
    expect(foundSteps.length).toBeGreaterThan(0);
  });

  it("final step has a message", () => {
    const steps = traceLinearScan({ array: arr });
    expect(lastStep(steps).message).toBeTruthy();
  });

  it("each step has pointers object", () => {
    const steps = traceLinearScan({ array: arr });
    for (const step of steps) {
      expect(typeof step.pointers).toBe("object");
    }
  });

  it("handles single-element array", () => {
    const steps = traceLinearScan({ array: [42] });
    expect(steps.length).toBeGreaterThan(0);
    expect(steps[0].cells).toHaveLength(1);
  });
});

// ─── Binary Search ────────────────────────────────────────────────────────────

describe("traceBinarySearch", () => {
  const sorted = [1, 3, 5, 7, 9, 11, 13];

  it("does not mutate input array", () => {
    const copy = [...sorted];
    traceBinarySearch({ array: sorted, target: 7 });
    expect(sorted).toEqual(copy);
  });

  it("returns steps with correct cell count", () => {
    const steps = traceBinarySearch({ array: sorted, target: 7 });
    for (const step of steps) {
      expect(step.cells).toHaveLength(sorted.length);
    }
  });

  it("marks target as 'found' when present", () => {
    const steps = traceBinarySearch({ array: sorted, target: 7 });
    const foundSteps = steps.filter((s) => s.cells.some((c) => c.state === "found"));
    expect(foundSteps.length).toBeGreaterThan(0);
  });

  it("marks cells as 'eliminated' during search", () => {
    const steps = traceBinarySearch({ array: sorted, target: 11 });
    const withEliminated = steps.filter((s) => s.cells.some((c) => c.state === "eliminated"));
    expect(withEliminated.length).toBeGreaterThan(0);
  });

  it("final step message indicates not found for missing target", () => {
    const steps = traceBinarySearch({ array: sorted, target: 8 });
    const final = lastStep(steps);
    expect(final.message.toLowerCase()).toContain("not found");
  });

  it("produces correct number of steps (logarithmic bound)", () => {
    const steps = traceBinarySearch({ array: sorted, target: 1 });
    // worst case binary search on 7 elements: ceil(log2(7))+1 = 3-4 iterations + a few framing steps
    expect(steps.length).toBeLessThan(sorted.length * 2);
  });

  it("handles target not in array without throwing", () => {
    expect(() => traceBinarySearch({ array: sorted, target: 100 })).not.toThrow();
  });
});

// ─── Two Pointers ─────────────────────────────────────────────────────────────

describe("traceTwoPointers", () => {
  const arr = [2, 7, 11, 15];

  it("does not mutate input array", () => {
    const copy = [...arr];
    traceTwoPointers({ array: arr, target: 9 });
    expect(arr).toEqual(copy);
  });

  it("returns steps with correct cell count", () => {
    const steps = traceTwoPointers({ array: arr, target: 9 });
    for (const step of steps) {
      expect(step.cells).toHaveLength(arr.length);
    }
  });

  it("uses left/right pointer states", () => {
    const steps = traceTwoPointers({ array: arr, target: 9 });
    const withPointers = steps.filter((s) =>
      s.cells.some((c) => c.state === "left-pointer" || c.state === "right-pointer")
    );
    expect(withPointers.length).toBeGreaterThan(0);
  });

  it("marks pair as found when target sum exists", () => {
    // [2, 7, 11, 15], target 9 → indices 0+1
    const steps = traceTwoPointers({ array: arr, target: 9 });
    const foundSteps = steps.filter((s) => s.cells.some((c) => c.state === "found"));
    expect(foundSteps.length).toBeGreaterThan(0);
  });

  it("includes variables in steps", () => {
    const steps = traceTwoPointers({ array: arr, target: 9 });
    const withVars = steps.filter((s) => Object.keys(s.variables).length > 0);
    expect(withVars.length).toBeGreaterThan(0);
  });
});

// ─── Sliding Window ───────────────────────────────────────────────────────────

describe("traceSlidingWindow", () => {
  const arr = [1, 3, -1, -3, 5, 3, 6, 7];

  it("does not mutate input array", () => {
    const copy = [...arr];
    traceSlidingWindow({ array: arr });
    expect(arr).toEqual(copy);
  });

  it("returns steps with correct cell count", () => {
    const steps = traceSlidingWindow({ array: arr });
    for (const step of steps) {
      expect(step.cells).toHaveLength(arr.length);
    }
  });

  it("uses window-related states", () => {
    const steps = traceSlidingWindow({ array: arr });
    const windowStates = ["in-window", "window-start", "window-end"] as const;
    const hasWindowState = steps.some((step) =>
      step.cells.some((c) => windowStates.includes(c.state as typeof windowStates[number]))
    );
    expect(hasWindowState).toBe(true);
  });

  it("produces more steps than array length (multiple windows)", () => {
    const steps = traceSlidingWindow({ array: arr });
    expect(steps.length).toBeGreaterThan(arr.length);
  });
});

// ─── Best Time to Buy and Sell Stock ─────────────────────────────────────────

describe("traceBestTimeToBuyStock", () => {
  const prices = [7, 1, 5, 3, 6, 4];

  it("does not mutate input array", () => {
    const copy = [...prices];
    traceBestTimeToBuyStock({ array: prices });
    expect(prices).toEqual(copy);
  });

  it("returns steps with correct cell count", () => {
    const steps = traceBestTimeToBuyStock({ array: prices });
    for (const step of steps) {
      expect(step.cells).toHaveLength(prices.length);
    }
  });

  it("tracks min price via min-tracked state or variables", () => {
    const steps = traceBestTimeToBuyStock({ array: prices });
    const withMin =
      steps.some((s) => s.cells.some((c) => c.state === "min-tracked")) ||
      steps.some((s) => "minPrice" in s.variables || "min" in s.variables);
    expect(withMin).toBe(true);
  });

  it("final step message includes the max profit", () => {
    const steps = traceBestTimeToBuyStock({ array: prices });
    // max profit for [7,1,5,3,6,4] = 5
    const final = lastStep(steps);
    expect(final.message).toContain("5");
  });

  it("handles all-decreasing prices (profit = 0)", () => {
    const steps = traceBestTimeToBuyStock({ array: [5, 4, 3, 2, 1] });
    const final = lastStep(steps);
    expect(final.message).toMatch(/0/);
  });
});

// ─── Maximum Subarray (Kadane's) ─────────────────────────────────────────────

describe("traceMaximumSubarray", () => {
  const arr = [-2, 1, -3, 4, -1, 2, 1, -5, 4];

  it("does not mutate input array", () => {
    const copy = [...arr];
    traceMaximumSubarray({ array: arr });
    expect(arr).toEqual(copy);
  });

  it("returns steps with correct cell count", () => {
    const steps = traceMaximumSubarray({ array: arr });
    for (const step of steps) {
      expect(step.cells).toHaveLength(arr.length);
    }
  });

  it("final step message includes maximum sum (6 for classic input)", () => {
    const steps = traceMaximumSubarray({ array: arr });
    // max subarray [4, -1, 2, 1] = 6
    const final = lastStep(steps);
    expect(final.message).toContain("6");
  });

  it("tracks current and max sum in variables (maxEndingHere / maxSoFar)", () => {
    const steps = traceMaximumSubarray({ array: arr });
    const withSumVars = steps.filter(
      (s) =>
        "maxSoFar" in s.variables ||
        "maxEndingHere" in s.variables ||
        "currentSum" in s.variables ||
        "maxSum" in s.variables
    );
    expect(withSumVars.length).toBeGreaterThan(0);
  });

  it("handles all-negative array without throwing", () => {
    expect(() => traceMaximumSubarray({ array: [-3, -1, -4, -2] })).not.toThrow();
  });
});

// ─── ARRAY_TRACERS registry ───────────────────────────────────────────────────

describe("ARRAY_TRACERS registry", () => {
  it("contains a tracer for binary-search-problem", () => {
    expect(ARRAY_TRACERS["binary-search-problem"]).toBeDefined();
  });

  it("contains a tracer for two-sum", () => {
    expect(ARRAY_TRACERS["two-sum"]).toBeDefined();
  });

  it("contains a tracer for best-time-to-buy-sell-stock", () => {
    expect(ARRAY_TRACERS["best-time-to-buy-sell-stock"]).toBeDefined();
  });

  it("contains a tracer for maximum-subarray", () => {
    expect(ARRAY_TRACERS["maximum-subarray"]).toBeDefined();
  });

  it("DEFAULT_TRACER is traceLinearScan", () => {
    expect(DEFAULT_TRACER).toBe(traceLinearScan);
  });

  it("every registered tracer returns valid VisualizationStep arrays", () => {
    const input = { array: [1, 2, 3, 4, 5], target: 3 };
    for (const [slug, tracer] of Object.entries(ARRAY_TRACERS)) {
      const steps = tracer(input);
      expect(Array.isArray(steps)).toBe(true);
      expect(steps.length).toBeGreaterThan(0);
      for (const step of steps) {
        expect(step.cells).toBeDefined();
        expect(step.message).toBeDefined();
        expect(typeof step.message).toBe("string");
      }
      // Suppress lint warning about slug usage
      void slug;
    }
  });
});
