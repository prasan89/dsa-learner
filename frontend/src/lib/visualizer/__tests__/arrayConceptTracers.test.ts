import {
  traceIndexing,
  traceTraversal,
  traceAccessByIndex,
  traceUpdateElement,
  traceLinearSearch,
  ARRAY_CONCEPTS,
} from "../tracers/arrayConceptTracers";

const ARR = [10, 25, 31, 42, 57, 81];

function checkCellCount(steps: ReturnType<typeof traceIndexing>, expected: number) {
  for (const s of steps) {
    expect(s.cells).toHaveLength(expected);
  }
}

describe("traceIndexing", () => {
  it("does not mutate input", () => {
    const copy = [...ARR];
    traceIndexing({ array: ARR });
    expect(ARR).toEqual(copy);
  });
  it("has consistent cell counts", () => checkCellCount(traceIndexing({ array: ARR }), ARR.length));
  it("marks cells as sorted in the final step", () => {
    const steps = traceIndexing({ array: ARR });
    const last = steps[steps.length - 1];
    expect(last.cells.every((c) => c.state === "sorted")).toBe(true);
  });
  it("exposes length in first step variables", () => {
    const steps = traceIndexing({ array: ARR });
    expect(steps[0].variables.length).toBe(ARR.length);
  });
});

describe("traceTraversal", () => {
  it("does not mutate input", () => {
    const copy = [...ARR];
    traceTraversal({ array: ARR });
    expect(ARR).toEqual(copy);
  });
  it("has consistent cell counts", () => checkCellCount(traceTraversal({ array: ARR }), ARR.length));
  it("produces intro + n visit steps + final step", () => {
    const steps = traceTraversal({ array: ARR });
    expect(steps.length).toBe(ARR.length + 2); // intro + n + final
  });
  it("marks each visited cell as sorted in subsequent steps", () => {
    const steps = traceTraversal({ array: ARR });
    // step 2 visits index 1; step 1 (index 0) should be sorted
    const step2 = steps[2];
    expect(step2.cells[0].state).toBe("sorted");
  });
});

describe("traceAccessByIndex", () => {
  it("does not mutate input", () => {
    const copy = [...ARR];
    traceAccessByIndex({ array: ARR, target: 3 });
    expect(ARR).toEqual(copy);
  });
  it("has consistent cell counts", () => checkCellCount(traceAccessByIndex({ array: ARR, target: 3 }), ARR.length));
  it("marks the target index as found in final step", () => {
    const steps = traceAccessByIndex({ array: ARR, target: 3 });
    const last = steps[steps.length - 1];
    expect(last.cells[3].state).toBe("found");
  });
  it("final step variables include the result value", () => {
    const steps = traceAccessByIndex({ array: ARR, target: 3 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(ARR[3]); // 42
  });
  it("falls back to middle index when no target given", () => {
    const steps = traceAccessByIndex({ array: ARR });
    const last = steps[steps.length - 1];
    const midIdx = Math.floor(ARR.length / 2);
    expect(last.cells[midIdx].state).toBe("found");
  });
});

describe("traceUpdateElement", () => {
  it("does not mutate the original input array", () => {
    const copy = [...ARR];
    traceUpdateElement({ array: ARR, target: 2 });
    expect(ARR).toEqual(copy);
  });
  it("has consistent cell counts", () => checkCellCount(traceUpdateElement({ array: ARR, target: 2 }), ARR.length));
  it("shows old value in first step variables", () => {
    const steps = traceUpdateElement({ array: ARR, target: 2 });
    expect(steps[0].variables["old value"]).toBe(ARR[2]); // 31
  });
  it("final step shows updated cell as found", () => {
    const steps = traceUpdateElement({ array: ARR, target: 2 });
    const last = steps[steps.length - 1];
    expect(last.cells[2].state).toBe("found");
  });
  it("final step cell value equals oldValue + 50", () => {
    const steps = traceUpdateElement({ array: ARR, target: 2 });
    const last = steps[steps.length - 1];
    expect(last.cells[2].value).toBe(ARR[2] + 50); // 81
  });
});

describe("traceLinearSearch", () => {
  it("does not mutate input", () => {
    const copy = [...ARR];
    traceLinearSearch({ array: ARR, target: 42 });
    expect(ARR).toEqual(copy);
  });
  it("has consistent cell counts", () => checkCellCount(traceLinearSearch({ array: ARR, target: 42 }), ARR.length));
  it("finds the target and marks it as found", () => {
    const steps = traceLinearSearch({ array: ARR, target: 42 });
    const foundStep = steps.find((s) => s.cells.some((c) => c.state === "found"));
    expect(foundStep).toBeDefined();
    const foundIdx = foundStep!.cells.findIndex((c) => c.state === "found");
    expect(ARR[foundIdx]).toBe(42);
  });
  it("final step result variable holds the found index", () => {
    const steps = traceLinearSearch({ array: ARR, target: 42 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(3);
  });
  it("returns -1 result when target not in array", () => {
    const steps = traceLinearSearch({ array: ARR, target: 99 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(-1);
  });
  it("exhausts all elements when target is absent", () => {
    const steps = traceLinearSearch({ array: ARR, target: 99 });
    // all cells eliminated in last step
    const last = steps[steps.length - 1];
    expect(last.cells.every((c) => c.state === "eliminated")).toBe(true);
  });
  it("short-circuits when target is first element", () => {
    const steps = traceLinearSearch({ array: ARR, target: ARR[0] });
    // intro + compare + found = 3 steps
    expect(steps.length).toBe(3);
  });
});

describe("ARRAY_CONCEPTS registry", () => {
  it("has exactly 5 concepts", () => {
    expect(ARRAY_CONCEPTS).toHaveLength(5);
  });
  it("each concept has required fields", () => {
    for (const c of ARRAY_CONCEPTS) {
      expect(c.id).toBeTruthy();
      expect(c.title).toBeTruthy();
      expect(c.tracer).toBeInstanceOf(Function);
      expect(c.algorithmSteps.length).toBeGreaterThan(0);
      expect(c.timeComplexity).toBeTruthy();
      expect(c.spaceComplexity).toBeTruthy();
    }
  });
  it("every tracer produces valid steps on the default array", () => {
    for (const c of ARRAY_CONCEPTS) {
      const steps = c.tracer({
        array: c.defaultArray,
        target: c.defaultTarget,
      });
      expect(steps.length).toBeGreaterThan(0);
      for (const s of steps) {
        expect(s.cells).toHaveLength(c.defaultArray.length);
        expect(typeof s.message).toBe("string");
        expect(s.message.length).toBeGreaterThan(0);
      }
    }
  });
});

// ─── Edge cases ────────────────────────────────────────────────────────────────

describe("edge cases — single element", () => {
  it("traceIndexing handles [42]", () => {
    const steps = traceIndexing({ array: [42] });
    expect(steps.length).toBeGreaterThan(0);
    steps.forEach((s) => expect(s.cells).toHaveLength(1));
  });

  it("traceTraversal handles [42]", () => {
    const steps = traceTraversal({ array: [42] });
    expect(steps.length).toBeGreaterThan(0);
    steps.forEach((s) => expect(s.cells).toHaveLength(1));
  });

  it("traceLinearSearch finds single element", () => {
    const steps = traceLinearSearch({ array: [42], target: 42 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(0);
  });

  it("traceLinearSearch misses single element", () => {
    const steps = traceLinearSearch({ array: [42], target: 99 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(-1);
  });
});

describe("edge cases — negative numbers", () => {
  const NEG = [-10, -5, 0, 5, 10];

  it("traceIndexing handles negatives", () => {
    const steps = traceIndexing({ array: NEG });
    expect(steps.length).toBeGreaterThan(0);
    steps.forEach((s) => expect(s.cells).toHaveLength(NEG.length));
  });

  it("traceLinearSearch finds a negative target", () => {
    const steps = traceLinearSearch({ array: NEG, target: -5 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(1);
  });

  it("traceLinearSearch returns -1 when negative target absent", () => {
    const steps = traceLinearSearch({ array: NEG, target: -99 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(-1);
  });
});

describe("edge cases — large numbers", () => {
  const BIG = [1000000, 2000000, 3000000];

  it("traceLinearSearch finds a large-number target", () => {
    const steps = traceLinearSearch({ array: BIG, target: 2000000 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(1);
  });

  it("cells display large values unchanged", () => {
    const steps = traceIndexing({ array: BIG });
    expect(steps[0].cells.map((c) => c.value)).toEqual(BIG);
  });
});

describe("edge cases — duplicate values", () => {
  const DUPS = [5, 5, 10, 5, 20];

  it("traceLinearSearch finds FIRST occurrence of duplicate target", () => {
    const steps = traceLinearSearch({ array: DUPS, target: 5 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(0);
  });

  it("traceLinearSearch does not skip duplicates", () => {
    const steps = traceLinearSearch({ array: DUPS, target: 10 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(2);
  });
});

describe("edge cases — target at last index", () => {
  const ARR_LAST = [1, 2, 3, 4, 5];

  it("traceLinearSearch walks all elements when target is last", () => {
    const steps = traceLinearSearch({ array: ARR_LAST, target: 5 });
    const last = steps[steps.length - 1];
    expect(last.variables.result).toBe(4);
    // All cells before the last should be eliminated
    const beforeLast = last.cells.slice(0, 4);
    expect(beforeLast.every((c) => c.state === "eliminated")).toBe(true);
  });
});
