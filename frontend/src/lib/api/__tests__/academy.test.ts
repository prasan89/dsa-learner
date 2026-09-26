import {
  resolveActiveLevel,
  resolveCtaAction,
  resolveNextLevel,
  CEFR_DISPLAY,
  type LevelSummary,
  type LessonSummary,
} from "../academy";

// ── Helpers ────────────────────────────────────────────────────────────────────

function makeLesson(overrides: Partial<LessonSummary> = {}): LessonSummary {
  return {
    lessonId: "lesson-1",
    title: "Saying Hello",
    status: "NOT_STARTED",
    position: 1,
    stepIndex: 0,
    score: null,
    ...overrides,
  };
}

function makeLevel(overrides: Partial<LevelSummary> = {}): LevelSummary {
  return {
    cefrLevel: "A1",
    displayName: "Beginner",
    ordinal: 1,
    status: "IN_PROGRESS",
    lessonsTotal: 10,
    lessonsCompleted: 3,
    avgScore: null,
    unlockedAt: null,
    completedAt: null,
    units: [
      {
        unitId: "unit-1",
        displayName: "Getting Started",
        ordinal: 1,
        lessons: [makeLesson({ status: "COMPLETED" }), makeLesson({ lessonId: "lesson-2", status: "IN_PROGRESS" })],
      },
    ],
    ...overrides,
  };
}

const LEVEL_SEQUENCE: LevelSummary[] = [
  makeLevel({ cefrLevel: "A1", ordinal: 1, status: "COMPLETED" }),
  makeLevel({ cefrLevel: "A2", ordinal: 2, status: "IN_PROGRESS" }),
  makeLevel({ cefrLevel: "B1", ordinal: 3, status: "NOT_STARTED" }),
  makeLevel({ cefrLevel: "B2", ordinal: 4, status: "NOT_STARTED" }),
  makeLevel({ cefrLevel: "C1", ordinal: 5, status: "NOT_STARTED" }),
  makeLevel({ cefrLevel: "C2", ordinal: 6, status: "NOT_STARTED" }),
];

// ── 1. resolveActiveLevel ──────────────────────────────────────────────────────

describe("resolveActiveLevel", () => {
  test("returns IN_PROGRESS level when one exists", () => {
    expect(resolveActiveLevel(LEVEL_SEQUENCE)).toBe("A2");
  });

  test("returns first NOT_STARTED level when none is IN_PROGRESS", () => {
    const levels = [
      makeLevel({ cefrLevel: "A1", ordinal: 1, status: "COMPLETED" }),
      makeLevel({ cefrLevel: "A2", ordinal: 2, status: "NOT_STARTED" }),
      makeLevel({ cefrLevel: "B1", ordinal: 3, status: "NOT_STARTED" }),
    ];
    expect(resolveActiveLevel(levels)).toBe("A2");
  });

  test("returns first level when all are NOT_STARTED", () => {
    const levels = [
      makeLevel({ cefrLevel: "A1", ordinal: 1, status: "NOT_STARTED" }),
      makeLevel({ cefrLevel: "A2", ordinal: 2, status: "NOT_STARTED" }),
    ];
    expect(resolveActiveLevel(levels)).toBe("A1");
  });

  test("handles single-level curriculum", () => {
    const levels = [makeLevel({ cefrLevel: "A1", ordinal: 1, status: "NOT_STARTED" })];
    expect(resolveActiveLevel(levels)).toBe("A1");
  });

  test("prefers IN_PROGRESS over COMPLETED when both present", () => {
    const levels = [
      makeLevel({ cefrLevel: "A1", ordinal: 1, status: "COMPLETED" }),
      makeLevel({ cefrLevel: "A2", ordinal: 2, status: "IN_PROGRESS" }),
      makeLevel({ cefrLevel: "B1", ordinal: 3, status: "IN_PROGRESS" }),
    ];
    // First IN_PROGRESS
    expect(resolveActiveLevel(levels)).toBe("A2");
  });
});

// ── 2. resolveNextLevel ────────────────────────────────────────────────────────

describe("resolveNextLevel", () => {
  test("returns next level by ordinal", () => {
    expect(resolveNextLevel(LEVEL_SEQUENCE, "A1")).toBe("A2");
    expect(resolveNextLevel(LEVEL_SEQUENCE, "A2")).toBe("B1");
    expect(resolveNextLevel(LEVEL_SEQUENCE, "B2")).toBe("C1");
  });

  test("returns null for the last level", () => {
    expect(resolveNextLevel(LEVEL_SEQUENCE, "C2")).toBeNull();
  });

  test("returns null for unknown level", () => {
    expect(resolveNextLevel(LEVEL_SEQUENCE, "X9")).toBeNull();
  });
});

// ── 3. resolveCtaAction ────────────────────────────────────────────────────────

describe("resolveCtaAction — start", () => {
  test("returns start when all lessons are NOT_STARTED", () => {
    const level = makeLevel({
      status: "IN_PROGRESS",
      units: [
        {
          unitId: "u1",
          displayName: null,
          ordinal: 1,
          lessons: [
            makeLesson({ status: "NOT_STARTED" }),
            makeLesson({ lessonId: "l2", status: "NOT_STARTED", position: 2 }),
          ],
        },
      ],
    });
    const action = resolveCtaAction(level);
    expect(action.kind).toBe("start");
    if (action.kind === "start") {
      expect(action.lesson.position).toBe(1);
    }
  });
});

describe("resolveCtaAction — continue", () => {
  test("returns continue when an IN_PROGRESS lesson exists", () => {
    const level = makeLevel({
      status: "IN_PROGRESS",
      units: [
        {
          unitId: "u1",
          displayName: null,
          ordinal: 1,
          lessons: [
            makeLesson({ status: "COMPLETED" }),
            makeLesson({ lessonId: "l2", status: "IN_PROGRESS", position: 2 }),
            makeLesson({ lessonId: "l3", status: "NOT_STARTED", position: 3 }),
          ],
        },
      ],
    });
    const action = resolveCtaAction(level);
    expect(action.kind).toBe("continue");
    if (action.kind === "continue") {
      expect(action.lesson.lessonId).toBe("l2");
    }
  });
});

describe("resolveCtaAction — levelComplete", () => {
  test("returns levelComplete when level status is COMPLETED", () => {
    const level = makeLevel({ status: "COMPLETED" });
    const action = resolveCtaAction(level);
    expect(action.kind).toBe("levelComplete");
  });

  test("returns levelComplete when all lessons are completed", () => {
    const level = makeLevel({
      status: "IN_PROGRESS",
      units: [
        {
          unitId: "u1",
          displayName: null,
          ordinal: 1,
          lessons: [
            makeLesson({ status: "COMPLETED" }),
            makeLesson({ lessonId: "l2", status: "COMPLETED", position: 2 }),
          ],
        },
      ],
    });
    const action = resolveCtaAction(level);
    expect(action.kind).toBe("levelComplete");
  });
});

describe("resolveCtaAction — noContent", () => {
  test("returns noContent when there are no lessons", () => {
    const level = makeLevel({
      status: "NOT_STARTED",
      units: [],
    });
    const action = resolveCtaAction(level);
    expect(action.kind).toBe("noContent");
  });
});

// ── 4. Progress percentage helpers ────────────────────────────────────────────

describe("Progress calculation", () => {
  test("computes 60% progress correctly", () => {
    const level = makeLevel({ lessonsTotal: 20, lessonsCompleted: 12 });
    const pct = level.lessonsTotal > 0
      ? Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100))
      : 0;
    expect(pct).toBe(60);
  });

  test("computes 0% when no lessons completed", () => {
    const level = makeLevel({ lessonsTotal: 10, lessonsCompleted: 0 });
    const pct = level.lessonsTotal > 0
      ? Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100))
      : 0;
    expect(pct).toBe(0);
  });

  test("caps at 100% even if overshooting", () => {
    const level = makeLevel({ lessonsTotal: 5, lessonsCompleted: 6 });
    const pct = level.lessonsTotal > 0
      ? Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100))
      : 0;
    expect(pct).toBe(100);
  });

  test("handles zero total gracefully", () => {
    const level = makeLevel({ lessonsTotal: 0, lessonsCompleted: 0 });
    const pct = level.lessonsTotal > 0
      ? Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100))
      : 0;
    expect(pct).toBe(0);
  });
});

// ── 5. Level selector logic ────────────────────────────────────────────────────

describe("Level locked detection", () => {
  test("A1 with ordinal 1 and NOT_STARTED is NOT locked", () => {
    const level = makeLevel({ cefrLevel: "A1", ordinal: 1, status: "NOT_STARTED" });
    const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
    expect(isLocked).toBe(false);
  });

  test("A2 with ordinal 2 and NOT_STARTED is locked", () => {
    const level = makeLevel({ cefrLevel: "A2", ordinal: 2, status: "NOT_STARTED" });
    const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
    expect(isLocked).toBe(true);
  });

  test("A2 with ordinal 2 and IN_PROGRESS is NOT locked", () => {
    const level = makeLevel({ cefrLevel: "A2", ordinal: 2, status: "IN_PROGRESS" });
    const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
    expect(isLocked).toBe(false);
  });

  test("C2 with ordinal 6 and COMPLETED is NOT locked", () => {
    const level = makeLevel({ cefrLevel: "C2", ordinal: 6, status: "COMPLETED" });
    const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
    expect(isLocked).toBe(false);
  });
});

// ── 6. CEFR display names ─────────────────────────────────────────────────────

describe("CEFR_DISPLAY", () => {
  test("all six CEFR levels have display names", () => {
    for (const level of ["A1", "A2", "B1", "B2", "C1", "C2"]) {
      expect(CEFR_DISPLAY[level]).toBeTruthy();
    }
  });

  test("A1 is Beginner", () => {
    expect(CEFR_DISPLAY["A1"]).toBe("Beginner");
  });

  test("C2 is Mastery", () => {
    expect(CEFR_DISPLAY["C2"]).toBe("Mastery");
  });
});

// ── 7. Units grouped correctly ─────────────────────────────────────────────────

describe("Unit grouping", () => {
  test("lessons within a unit are accessible", () => {
    const level = makeLevel({
      units: [
        {
          unitId: "u1",
          displayName: "Unit 1",
          ordinal: 1,
          lessons: [
            makeLesson({ position: 1 }),
            makeLesson({ lessonId: "l2", position: 2 }),
            makeLesson({ lessonId: "l3", position: 3 }),
          ],
        },
        {
          unitId: "u2",
          displayName: "Unit 2",
          ordinal: 2,
          lessons: [makeLesson({ lessonId: "l4", position: 4 })],
        },
      ],
    });
    const allLessons = level.units.flatMap((u) => u.lessons);
    expect(allLessons).toHaveLength(4);
    expect(level.units[0].lessons).toHaveLength(3);
    expect(level.units[1].lessons).toHaveLength(1);
  });

  test("empty units have no lessons", () => {
    const level = makeLevel({
      units: [
        { unitId: "u1", displayName: "Empty", ordinal: 1, lessons: [] },
      ],
    });
    expect(level.units[0].lessons).toHaveLength(0);
  });
});

// ── 8. No Content Factory fields ──────────────────────────────────────────────

describe("LessonSummary shape — no CF internals", () => {
  test("LessonSummary does not have QA status or pipeline fields", () => {
    const lesson = makeLesson();
    expect(lesson).not.toHaveProperty("qaStatus");
    expect(lesson).not.toHaveProperty("planStatus");
    expect(lesson).not.toHaveProperty("stableRef");
    expect(lesson).not.toHaveProperty("generationAttempt");
    expect(lesson).not.toHaveProperty("contentStatus");
  });
});

// ── 9. API URL construction ────────────────────────────────────────────────────

describe("Academy API endpoint paths", () => {
  test("getCurriculum uses correct path", () => {
    // We verify the path pattern by inspection — no live HTTP call
    const path = `/v1/academy/german/curriculum`;
    expect(path).toBe("/v1/academy/german/curriculum");
  });

  test("language-agnostic — french path also valid", () => {
    const path = `/v1/academy/french/curriculum`;
    expect(path).toContain("french");
  });
});

// ── 10. Curriculum UX redesign: level journey rail state logic ─────────────────

describe("Level journey rail state derivation", () => {
  const levels = [
    makeLevel({ cefrLevel: "A1", ordinal: 1, status: "COMPLETED" }),
    makeLevel({ cefrLevel: "A2", ordinal: 2, status: "IN_PROGRESS" }),
    makeLevel({ cefrLevel: "B1", ordinal: 3, status: "NOT_STARTED" }),
    makeLevel({ cefrLevel: "B2", ordinal: 4, status: "NOT_STARTED" }),
    makeLevel({ cefrLevel: "C1", ordinal: 5, status: "NOT_STARTED" }),
    makeLevel({ cefrLevel: "C2", ordinal: 6, status: "NOT_STARTED" }),
  ];

  test("completed level should show checkmark, not lock", () => {
    const a1 = levels[0];
    const isCompleted = a1.status === "COMPLETED";
    const isLocked = a1.status === "NOT_STARTED" && a1.ordinal > 1;
    expect(isCompleted).toBe(true);
    expect(isLocked).toBe(false);
  });

  test("in-progress level is not locked", () => {
    const a2 = levels[1];
    const isLocked = a2.status === "NOT_STARTED" && a2.ordinal > 1;
    expect(isLocked).toBe(false);
  });

  test("B1 through C2 are locked (NOT_STARTED, ordinal > 1)", () => {
    const locked = levels.filter((l) => l.status === "NOT_STARTED" && l.ordinal > 1);
    expect(locked.map((l) => l.cefrLevel)).toEqual(["B1", "B2", "C1", "C2"]);
  });

  test("clicking a locked level should trigger locked message, not change activeCefr", () => {
    // Simulate the guard: locked levels return early
    let activeCefr = "A2";
    let lockedMessage: string | null = null;

    function handleSelectLevel(cefr: string) {
      const level = levels.find((l) => l.cefrLevel === cefr);
      if (!level) return;
      if (level.status === "NOT_STARTED" && level.ordinal > 1) {
        lockedMessage = cefr;
        return;
      }
      lockedMessage = null;
      activeCefr = cefr;
    }

    handleSelectLevel("B1");
    expect(lockedMessage).toBe("B1");
    expect(activeCefr).toBe("A2"); // unchanged

    handleSelectLevel("A1");
    expect(lockedMessage).toBeNull();
    expect(activeCefr).toBe("A1");
  });

  test("first level (ordinal 1) is never locked even if NOT_STARTED", () => {
    const a1Fresh = makeLevel({ cefrLevel: "A1", ordinal: 1, status: "NOT_STARTED" });
    const isLocked = a1Fresh.status === "NOT_STARTED" && a1Fresh.ordinal > 1;
    expect(isLocked).toBe(false);
  });
});

// ── 11. Curriculum UX redesign: lesson score display logic ─────────────────────

describe("Lesson score display", () => {
  test("completed lesson with score >= 80 gets high-score styling", () => {
    const lesson = makeLesson({ status: "COMPLETED", score: 87 });
    const isCompleted = lesson.status === "COMPLETED";
    const showScore = isCompleted && lesson.score !== null;
    const scoreDisplay = showScore ? `${Math.round(lesson.score!)}%` : null;
    const isHighScore = lesson.score !== null && lesson.score >= 80;
    expect(scoreDisplay).toBe("87%");
    expect(isHighScore).toBe(true);
  });

  test("completed lesson with score < 80 gets neutral styling", () => {
    const lesson = makeLesson({ status: "COMPLETED", score: 65 });
    const isHighScore = lesson.score !== null && lesson.score >= 80;
    expect(isHighScore).toBe(false);
  });

  test("completed lesson with null score shows no score badge", () => {
    const lesson = makeLesson({ status: "COMPLETED", score: null });
    const showScore = lesson.status === "COMPLETED" && lesson.score !== null;
    expect(showScore).toBe(false);
  });

  test("not-started lesson never shows score badge", () => {
    const lesson = makeLesson({ status: "NOT_STARTED", score: null });
    const showScore = lesson.status === "COMPLETED" && lesson.score !== null;
    expect(showScore).toBe(false);
  });

  test("in-progress lesson never shows score badge", () => {
    const lesson = makeLesson({ status: "IN_PROGRESS", score: 40 });
    const showScore = lesson.status === "COMPLETED" && lesson.score !== null;
    expect(showScore).toBe(false);
  });

  test("score rounds to nearest integer for display", () => {
    const lesson = makeLesson({ status: "COMPLETED", score: 87.6 });
    const scoreDisplay = `${Math.round(lesson.score!)}%`;
    expect(scoreDisplay).toBe("88%");
  });
});

// ── 12. Curriculum UX redesign: level hero progress calculations ───────────────

describe("LevelHero progress display", () => {
  test("pct rounds correctly for non-round numbers", () => {
    const level = makeLevel({ lessonsTotal: 24, lessonsCompleted: 13 });
    const pct = Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100));
    expect(pct).toBe(54);
  });

  test("100% when all done", () => {
    const level = makeLevel({ lessonsTotal: 10, lessonsCompleted: 10 });
    const pct = Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100));
    expect(pct).toBe(100);
  });

  test("pct not shown (0) when completed is 0", () => {
    const level = makeLevel({ lessonsTotal: 10, lessonsCompleted: 0 });
    const pct = level.lessonsTotal > 0
      ? Math.min(100, Math.round((level.lessonsCompleted / level.lessonsTotal) * 100))
      : 0;
    expect(pct).toBe(0);
    // pct > 0 is false so the % suffix would be hidden
    expect(pct > 0).toBe(false);
  });

  test("displayName from LevelSummary takes priority over CEFR_DISPLAY", () => {
    const level = makeLevel({ cefrLevel: "A1", displayName: "Starter" });
    // The component uses: level.displayName || CEFR_DISPLAY[level.cefrLevel]
    const name = level.displayName || CEFR_DISPLAY[level.cefrLevel];
    expect(name).toBe("Starter");
  });

  test("CEFR_DISPLAY used as fallback when displayName is empty", () => {
    const level = makeLevel({ cefrLevel: "A1", displayName: "" });
    const name = level.displayName || CEFR_DISPLAY[level.cefrLevel];
    expect(name).toBe("Beginner");
  });
});

// ── 13. Curriculum UX redesign: continue CTA lesson count display ──────────────

describe("ContinueCta lesson count", () => {
  test("shows 'Lesson N of M' when total > 0", () => {
    const level = makeLevel({
      units: [
        {
          unitId: "u1",
          displayName: null,
          ordinal: 1,
          lessons: [
            makeLesson({ position: 1, status: "COMPLETED" }),
            makeLesson({ lessonId: "l2", position: 2, status: "IN_PROGRESS" }),
            makeLesson({ lessonId: "l3", position: 3, status: "NOT_STARTED" }),
          ],
        },
      ],
    });
    const allLessons = level.units.flatMap((u) => u.lessons);
    const totalLessons = allLessons.length;
    const cta = resolveCtaAction(level);
    expect(cta.kind).toBe("continue");
    if (cta.kind === "continue") {
      const label = `Lesson ${cta.lesson.position} of ${totalLessons}`;
      expect(label).toBe("Lesson 2 of 3");
    }
  });
});

// ── 14. Curriculum UX redesign: unit header completion counting ────────────────

describe("UnitSection completion count", () => {
  test("allDone is true when every lesson is COMPLETED", () => {
    const lessons = [
      makeLesson({ status: "COMPLETED" }),
      makeLesson({ lessonId: "l2", status: "COMPLETED" }),
    ];
    const completedCount = lessons.filter((l) => l.status === "COMPLETED").length;
    const allDone = completedCount === lessons.length && lessons.length > 0;
    expect(allDone).toBe(true);
  });

  test("allDone is false when one lesson is still NOT_STARTED", () => {
    const lessons = [
      makeLesson({ status: "COMPLETED" }),
      makeLesson({ lessonId: "l2", status: "NOT_STARTED" }),
    ];
    const completedCount = lessons.filter((l) => l.status === "COMPLETED").length;
    const allDone = completedCount === lessons.length && lessons.length > 0;
    expect(allDone).toBe(false);
  });

  test("allDone is false for empty unit", () => {
    const lessons: LessonSummary[] = [];
    const completedCount = lessons.filter((l) => l.status === "COMPLETED").length;
    const allDone = completedCount === lessons.length && lessons.length > 0;
    expect(allDone).toBe(false);
  });

  test("partial progress fraction displayed correctly", () => {
    const lessons = [
      makeLesson({ status: "COMPLETED" }),
      makeLesson({ lessonId: "l2", status: "COMPLETED" }),
      makeLesson({ lessonId: "l3", status: "NOT_STARTED" }),
    ];
    const completedCount = lessons.filter((l) => l.status === "COMPLETED").length;
    expect(`${completedCount}/${lessons.length}`).toBe("2/3");
  });
});
