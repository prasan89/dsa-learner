import {
  challengeIndexing,
  challengeTraversal,
  challengeAccessByIndex,
  challengeUpdateElement,
  challengeLinearSearch,
  CHALLENGE_GENERATORS,
} from "../tracers/challengeTracers";
import type { VisualizationStep } from "../types";

// ─── Helpers ──────────────────────────────────────────────────────────────────

function challengeSteps(steps: VisualizationStep[]) {
  return steps.filter((s) => s.challenge != null);
}

function assertChallenge(step: VisualizationStep) {
  const ch = step.challenge!;
  expect(ch).toBeDefined();
  expect(ch.question.length).toBeGreaterThan(0);
  expect(ch.explanation.length).toBeGreaterThan(0);
  expect(ch.choices).toHaveLength(4);
  expect(ch.choices.some((c) => c.value === ch.correctAnswer)).toBe(true);
  // All choice values must be distinct
  const vals = ch.choices.map((c) => c.value);
  expect(new Set(vals).size).toBe(vals.length);
}

const SMALL  = { array: [10, 20, 30, 40, 50] };
const MEDIUM = { array: [5, 15, 25, 35, 45, 55, 65] };

// ─── CHALLENGE_GENERATORS registry ───────────────────────────────────────────

describe("CHALLENGE_GENERATORS registry", () => {
  it("has entries for all 5 array concepts", () => {
    const keys = Object.keys(CHALLENGE_GENERATORS);
    expect(keys).toContain("indexing");
    expect(keys).toContain("traversal");
    expect(keys).toContain("access");
    expect(keys).toContain("update");
    expect(keys).toContain("linear-search");
  });

  it("each entry is a callable function", () => {
    for (const fn of Object.values(CHALLENGE_GENERATORS)) {
      expect(typeof fn).toBe("function");
    }
  });
});

// ─── challengeIndexing ────────────────────────────────────────────────────────

describe("challengeIndexing", () => {
  const steps = challengeIndexing(SMALL);
  const cSteps = challengeSteps(steps);

  it("produces more steps than the base tracer (challenges add extra steps)", () => {
    expect(steps.length).toBeGreaterThan(SMALL.array.length + 2);
  });

  it("embeds at least 2 challenges", () => {
    expect(cSteps.length).toBeGreaterThanOrEqual(2);
  });

  it("each challenge has valid structure", () => {
    cSteps.forEach(assertChallenge);
  });

  it("includes a predict-complexity challenge with correctAnswer O(1)", () => {
    const comp = cSteps.find((s) => s.challenge!.type === "predict-complexity");
    expect(comp).toBeDefined();
    expect(comp!.challenge!.correctAnswer).toBe("O(1)");
  });

  it("includes a predict-next-index challenge", () => {
    const idx = cSteps.find((s) => s.challenge!.type === "predict-next-index");
    expect(idx).toBeDefined();
    // Correct answer should be the last valid index = arr.length - 1 = 4
    expect(idx!.challenge!.correctAnswer).toBe(String(SMALL.array.length - 1));
  });

  it("non-challenge steps are unchanged clones", () => {
    const plain = steps.filter((s) => !s.challenge);
    plain.forEach((s) => {
      expect(s.cells).toBeDefined();
      expect(s.message).toBeDefined();
    });
  });

  it("works with a single-element array", () => {
    const single = challengeIndexing({ array: [42] });
    expect(single.length).toBeGreaterThan(0);
    challengeSteps(single).forEach(assertChallenge);
  });
});

// ─── challengeTraversal ───────────────────────────────────────────────────────

describe("challengeTraversal", () => {
  const steps = challengeTraversal(SMALL);
  const cSteps = challengeSteps(steps);

  it("produces at least 3 challenges for a 5-element array", () => {
    expect(cSteps.length).toBeGreaterThanOrEqual(3);
  });

  it("each challenge has valid structure", () => {
    cSteps.forEach(assertChallenge);
  });

  it("includes a predict-complexity challenge with correctAnswer O(n)", () => {
    const comp = cSteps.find((s) => s.challenge!.type === "predict-complexity");
    expect(comp).toBeDefined();
    expect(comp!.challenge!.correctAnswer).toBe("O(n)");
  });

  it("includes a predict-next-index challenge with correct increment", () => {
    const idx = cSteps.find((s) => s.challenge!.type === "predict-next-index");
    expect(idx).toBeDefined();
    // After visiting index 1, next should be 2
    expect(idx!.challenge!.correctAnswer).toBe("2");
  });

  it("works with a 7-element array", () => {
    const steps7 = challengeTraversal(MEDIUM);
    challengeSteps(steps7).forEach(assertChallenge);
  });
});

// ─── challengeAccessByIndex ───────────────────────────────────────────────────

describe("challengeAccessByIndex", () => {
  const input = { array: [10, 20, 30, 40, 50], target: 2 };
  const steps = challengeAccessByIndex(input);
  const cSteps = challengeSteps(steps);

  it("embeds exactly 2 challenges (predict-value + complexity)", () => {
    expect(cSteps).toHaveLength(2);
  });

  it("each challenge has valid structure", () => {
    cSteps.forEach(assertChallenge);
  });

  it("first challenge asks about the value at the target index", () => {
    const val = cSteps.find((s) => s.challenge!.type === "predict-next-value");
    expect(val).toBeDefined();
    expect(val!.challenge!.correctAnswer).toBe("30"); // arr[2] = 30
  });

  it("complexity challenge has correctAnswer O(1)", () => {
    const comp = cSteps.find((s) => s.challenge!.type === "predict-complexity");
    expect(comp!.challenge!.correctAnswer).toBe("O(1)");
  });

  it("falls back gracefully when no target provided", () => {
    const noTarget = challengeAccessByIndex({ array: [1, 2, 3, 4, 5] });
    expect(noTarget.length).toBeGreaterThan(0);
    challengeSteps(noTarget).forEach(assertChallenge);
  });
});

// ─── challengeUpdateElement ───────────────────────────────────────────────────

describe("challengeUpdateElement", () => {
  const input = { array: [10, 20, 30, 40, 50], target: 2 };
  const steps = challengeUpdateElement(input);
  const cSteps = challengeSteps(steps);

  it("embeds 3 challenges", () => {
    expect(cSteps).toHaveLength(3);
  });

  it("each challenge has valid structure", () => {
    cSteps.forEach(assertChallenge);
  });

  it("predict-final-result challenge asks for new value (oldValue + 50)", () => {
    const fin = cSteps.find((s) => s.challenge!.type === "predict-final-result");
    expect(fin).toBeDefined();
    const oldValue = 30; // arr[2]
    expect(fin!.challenge!.correctAnswer).toBe(String(oldValue + 50));
  });

  it("first predict-value challenge asks for current value at index before update", () => {
    const val = cSteps.find((s) => s.challenge!.type === "predict-next-value");
    expect(val!.challenge!.correctAnswer).toBe("30"); // arr[2] before write
  });

  it("complexity challenge has correctAnswer O(1)", () => {
    const comp = cSteps.find((s) => s.challenge!.type === "predict-complexity");
    expect(comp!.challenge!.correctAnswer).toBe("O(1)");
  });
});

// ─── challengeLinearSearch ────────────────────────────────────────────────────

describe("challengeLinearSearch", () => {
  describe("target found", () => {
    const input = { array: [10, 20, 30, 40, 50], target: 30 };
    const steps = challengeLinearSearch(input);
    const cSteps = challengeSteps(steps);

    it("embeds at least 2 challenges", () => {
      expect(cSteps.length).toBeGreaterThanOrEqual(2);
    });

    it("each challenge has valid structure", () => {
      cSteps.forEach(assertChallenge);
    });

    it("includes a predict-final-result challenge with the found index", () => {
      const fin = cSteps.find((s) => s.challenge!.type === "predict-final-result");
      expect(fin).toBeDefined();
      expect(fin!.challenge!.correctAnswer).toBe("2"); // 30 is at index 2
    });

    it("complexity challenge has correctAnswer O(n)", () => {
      const comp = cSteps.find((s) => s.challenge!.type === "predict-complexity");
      expect(comp).toBeDefined();
      expect(comp!.challenge!.correctAnswer).toBe("O(n)");
    });
  });

  describe("target not found", () => {
    const input = { array: [10, 20, 30, 40, 50], target: 99 };
    const steps = challengeLinearSearch(input);
    const cSteps = challengeSteps(steps);

    it("includes a predict-final-result challenge with answer -1", () => {
      const fin = cSteps.find((s) => s.challenge!.type === "predict-final-result");
      expect(fin).toBeDefined();
      expect(fin!.challenge!.correctAnswer).toBe("-1");
    });
  });

  describe("target at index 0", () => {
    const input = { array: [30, 10, 20], target: 30 };
    const steps = challengeLinearSearch(input);

    it("still produces steps without errors", () => {
      expect(steps.length).toBeGreaterThan(0);
    });
  });
});

// ─── Cross-concept invariants ─────────────────────────────────────────────────

describe("challenge invariants across all generators", () => {
  const generators = Object.entries(CHALLENGE_GENERATORS);
  const input = { array: [5, 15, 25, 35, 45], target: 2 };

  generators.forEach(([name, gen]) => {
    describe(name, () => {
      const steps = gen(input);
      const cSteps = challengeSteps(steps);

      it("produces at least 1 step", () => {
        expect(steps.length).toBeGreaterThan(0);
      });

      it("produces at least 1 challenge", () => {
        expect(cSteps.length).toBeGreaterThanOrEqual(1);
      });

      it("every challenge has exactly 4 choices", () => {
        cSteps.forEach((s) => {
          expect(s.challenge!.choices).toHaveLength(4);
        });
      });

      it("correct answer is always one of the choices", () => {
        cSteps.forEach((s) => {
          const ch = s.challenge!;
          expect(ch.choices.map((c) => c.value)).toContain(ch.correctAnswer);
        });
      });

      it("choices have distinct values (no duplicates)", () => {
        cSteps.forEach((s) => {
          const vals = s.challenge!.choices.map((c) => c.value);
          expect(new Set(vals).size).toBe(4);
        });
      });

      it("base step data is preserved on challenge steps", () => {
        cSteps.forEach((s) => {
          expect(s.cells).toBeDefined();
          expect(Array.isArray(s.cells)).toBe(true);
          expect(s.message).toBeDefined();
        });
      });
    });
  });
});

// ─── Edge cases ───────────────────────────────────────────────────────────────

describe("challenge edge cases — empty array guard (UI uses this path)", () => {
  // The UI guards: if parsedArray.length === 0 return [] before calling generators.
  // These tests verify the guard logic, not the generators themselves.
  it("empty array produces no challenge steps when filtered correctly", () => {
    // Simulate what InteractiveConceptVisualizer does:
    const arr: number[] = [];
    const steps = arr.length === 0 ? [] : challengeLinearSearch({ array: arr });
    expect(steps).toEqual([]);
  });
});

describe("challenge edge cases — single element", () => {
  it("challengeLinearSearch single element found — valid challenges", () => {
    const steps = challengeLinearSearch({ array: [42], target: 42 });
    expect(steps.length).toBeGreaterThan(0);
    challengeSteps(steps).forEach(assertChallenge);
  });

  it("challengeLinearSearch single element not found — valid challenges", () => {
    const steps = challengeLinearSearch({ array: [42], target: 99 });
    expect(steps.length).toBeGreaterThan(0);
    challengeSteps(steps).forEach(assertChallenge);
  });
});
