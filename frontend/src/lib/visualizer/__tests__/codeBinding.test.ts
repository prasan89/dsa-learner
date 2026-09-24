import { traceLinearSearchWithCode, LINEAR_SEARCH_CODE, CODE_BINDINGS } from "../codeBinding";

// ─── Helpers ──────────────────────────────────────────────────────────────────

function runTrace(array: number[], target: number) {
  return traceLinearSearchWithCode({ array, target });
}

// ─── traceLinearSearchWithCode ────────────────────────────────────────────────

describe("traceLinearSearchWithCode", () => {
  describe("step count", () => {
    it("produces the same number of steps as the base tracer would", () => {
      const steps = runTrace([10, 20, 30, 40, 50], 30);
      // intro + 3 comparisons (indices 0,1,2) + found = 5
      expect(steps.length).toBe(5);
    });

    it("handles single-element array target found", () => {
      const steps = runTrace([42], 42);
      // intro + comparison at 0 + found = 3
      expect(steps.length).toBe(3);
    });

    it("handles not-found case", () => {
      const steps = runTrace([1, 2, 3], 99);
      // intro + 3 comparisons + not-found = 5
      expect(steps.length).toBe(5);
    });
  });

  describe("step 0 — intro", () => {
    it("highlights line 1 (loop-init)", () => {
      const steps = runTrace([5, 10, 15], 10);
      expect(steps[0].highlightLine).toBe(1);
    });

    it("preserves all other step fields on intro", () => {
      const steps = runTrace([5, 10, 15], 10);
      expect(steps[0].message).toBeDefined();
      expect(steps[0].cells).toBeDefined();
    });
  });

  describe("comparison steps", () => {
    it("highlights line 2 on each comparison step", () => {
      const steps = runTrace([1, 2, 3, 4, 5], 5);
      // steps 1..4 are comparisons (indices 0-3), step 5 is the found step
      const comparisonSteps = steps.slice(1, steps.length - 1);
      comparisonSteps.forEach((s, i) => {
        expect(s.highlightLine).toBe(2);
      });
    });

    it("comparison steps have no result variable", () => {
      const steps = runTrace([10, 20, 30], 30);
      // steps 1 and 2 are comparisons
      expect(steps[1].variables.result).toBeUndefined();
      expect(steps[2].variables.result).toBeUndefined();
    });
  });

  describe("found step", () => {
    it("highlights line 3 when target is found", () => {
      const steps = runTrace([10, 20, 30], 20);
      const lastStep = steps[steps.length - 1];
      expect(lastStep.variables.result).toBe(1);
      expect(lastStep.highlightLine).toBe(3);
    });

    it("highlights line 3 when target is the first element", () => {
      const steps = runTrace([42, 1, 2], 42);
      // intro + comparison at 0 + found = 3 steps
      expect(steps.length).toBe(3);
      expect(steps[2].highlightLine).toBe(3);
    });

    it("highlights line 3 when target is the last element", () => {
      const steps = runTrace([1, 2, 3], 3);
      const lastStep = steps[steps.length - 1];
      expect(lastStep.variables.result).toBe(2);
      expect(lastStep.highlightLine).toBe(3);
    });
  });

  describe("not-found step", () => {
    it("highlights line 6 when target is absent", () => {
      const steps = runTrace([1, 2, 3], 99);
      const lastStep = steps[steps.length - 1];
      expect(lastStep.variables.result).toBe(-1);
      expect(lastStep.highlightLine).toBe(6);
    });

    it("highlights line 6 for single-element not-found", () => {
      const steps = runTrace([7], 99);
      // intro + comparison + not-found = 3
      expect(steps.length).toBe(3);
      expect(steps[2].highlightLine).toBe(6);
    });
  });

  describe("immutability — base steps not mutated", () => {
    it("does not mutate cells arrays from base tracer", () => {
      const steps = runTrace([1, 2, 3], 2);
      steps.forEach((s) => {
        // Each step is a new object (spread) so modifying highlightLine
        // should not affect any sibling step
        expect(s).toHaveProperty("highlightLine");
      });
    });

    it("every step object is a distinct reference (spread creates new objects)", () => {
      const steps = runTrace([5, 10], 10);
      for (let i = 0; i < steps.length - 1; i++) {
        expect(steps[i]).not.toBe(steps[i + 1]);
      }
    });
  });

  describe("all steps have highlightLine defined", () => {
    it("no step has undefined highlightLine — target found", () => {
      const steps = runTrace([3, 6, 9, 12], 9);
      steps.forEach((s) => {
        expect(s.highlightLine).toBeDefined();
      });
    });

    it("no step has undefined highlightLine — target not found", () => {
      const steps = runTrace([3, 6, 9, 12], 100);
      steps.forEach((s) => {
        expect(s.highlightLine).toBeDefined();
      });
    });
  });
});

// ─── CODE_BINDINGS registry ───────────────────────────────────────────────────

describe("CODE_BINDINGS", () => {
  it("contains a linear-search entry", () => {
    expect(CODE_BINDINGS["linear-search"]).toBeDefined();
  });

  it("linear-search entry has java language", () => {
    expect(CODE_BINDINGS["linear-search"].language).toBe("java");
  });

  it("linear-search source contains the function signature", () => {
    expect(CODE_BINDINGS["linear-search"].source).toContain("linearSearch");
  });

  it("linear-search sourcemap maps comparison to line 2", () => {
    expect(CODE_BINDINGS["linear-search"].sourcemap["comparison"]).toBe(2);
  });

  it("linear-search sourcemap maps found to line 3", () => {
    expect(CODE_BINDINGS["linear-search"].sourcemap["found"]).toBe(3);
  });

  it("linear-search sourcemap maps not-found to line 6", () => {
    expect(CODE_BINDINGS["linear-search"].sourcemap["not-found"]).toBe(6);
  });
});
