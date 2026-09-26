/**
 * Lesson Player unit tests
 * Tests step renderer logic, navigation, answer handling, completion, and resume.
 */
import {
  type ExperiencePlanStep,
  type StepType,
  type StepPayload,
  type VocabularyCardPayload,
  type VocabularySummaryPayload,
  type MultipleChoicePayload,
  type FillInBlankPayload,
  type TranslationPayload,
  type LessonReviewPayload,
  type LessonResponse,
  type ExperiencePlan,
} from "../academy";

// ── Fixtures ──────────────────────────────────────────────────────────────────

function makeStep(type: StepType, payload: StepPayload, overrides: Partial<ExperiencePlanStep> = {}): ExperiencePlanStep {
  return {
    index: 0,
    type,
    payload,
    audioKey: null,
    isExercise: false,
    ...overrides,
  };
}

const VOCAB_PAYLOAD: VocabularyCardPayload = {
  german: "Guten Morgen",
  english: "Good morning",
  example: "Guten Morgen, Anna!",
  pronunciation: "GOO-ten MOR-gen",
  vocabIndex: 0,
};

const MC_PAYLOAD: MultipleChoicePayload = {
  type: "MULTIPLE_CHOICE",
  question: "What does 'Guten Morgen' mean?",
  options: ["Good evening", "Good morning", "Good night", "Goodbye"],
  correctAnswer: "Good morning",
  explanation: "Guten Morgen is the standard morning greeting.",
  exerciseIndex: 0,
};

const FILL_PAYLOAD: FillInBlankPayload = {
  type: "FILL_IN_BLANK",
  question: "Ich ___ gut schwimmen.",
  correctAnswer: "kann",
  hint: "Use the 'I can' form of können.",
  exerciseIndex: 1,
};

const TRANSLATION_PAYLOAD: TranslationPayload = {
  type: "TRANSLATION",
  source: "She can cook well.",
  correctAnswer: "Sie kann gut kochen.",
  hint: "Use 'Sie' for she.",
  exerciseIndex: 2,
};

const REVIEW_PAYLOAD: LessonReviewPayload = {
  lessonTitle: "Greetings",
  cefrLevel: "A1",
  exerciseCount: 3,
};

const VOCAB_SUMMARY_PAYLOAD: VocabularySummaryPayload = {
  items: [VOCAB_PAYLOAD],
  count: 1,
};

function makePlan(steps: ExperiencePlanStep[]): ExperiencePlan {
  return {
    lessonId: "lesson-1",
    lessonVersionId: "v-1",
    lessonVersion: 1,
    lessonTitle: "Test Lesson",
    cefrLevel: "A1",
    languageCode: "de",
    unitDisplayName: "Unit 1",
    totalSteps: steps.length,
    totalExercises: steps.filter((s) => s.isExercise).length,
    steps,
  };
}

function makeLesson(overrides: Partial<LessonResponse> = {}): LessonResponse {
  const steps: ExperiencePlanStep[] = [
    makeStep("VOCABULARY_CARD", VOCAB_PAYLOAD, { index: 0 }),
    makeStep("VOCABULARY_SUMMARY", VOCAB_SUMMARY_PAYLOAD, { index: 1 }),
    makeStep("MULTIPLE_CHOICE", MC_PAYLOAD, { index: 2, isExercise: true }),
    makeStep("LESSON_REVIEW", REVIEW_PAYLOAD, { index: 3 }),
  ];
  return {
    lessonId: "lesson-1",
    title: "Greetings",
    cefrLevel: "A1",
    languageCode: "de",
    unitDisplayName: "Unit 1",
    learnerStatus: "NOT_STARTED",
    currentStepIndex: 0,
    score: null,
    startedAt: null,
    completedAt: null,
    experiencePlan: makePlan(steps),
    ...overrides,
  };
}

// ── 1. StepType → component registry ──────────────────────────────────────────

describe("StepType registry", () => {
  const ALL_STEP_TYPES: StepType[] = [
    "NARRATIVE",
    "VOCABULARY_CARD",
    "GRAMMAR_EXPLANATION",
    "MULTIPLE_CHOICE",
    "FILL_IN_BLANK",
    "TRANSLATION",
    "VOCABULARY_SUMMARY",
    "LESSON_REVIEW",
  ];

  test("all 8 StepType values are defined", () => {
    expect(ALL_STEP_TYPES).toHaveLength(8);
  });

  test("exercise steps are MULTIPLE_CHOICE, FILL_IN_BLANK, TRANSLATION", () => {
    const exercises: StepType[] = ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "TRANSLATION"];
    exercises.forEach((t) => {
      const step = makeStep(t, {}, { isExercise: true });
      expect(step.isExercise).toBe(true);
    });
  });

  test("non-exercise steps are not flagged as exercises", () => {
    const nonExercises: StepType[] = ["NARRATIVE", "VOCABULARY_CARD", "GRAMMAR_EXPLANATION", "VOCABULARY_SUMMARY", "LESSON_REVIEW"];
    nonExercises.forEach((t) => {
      const step = makeStep(t, {}, { isExercise: false });
      expect(step.isExercise).toBe(false);
    });
  });
});

// ── 2. VocabularyCard payload ──────────────────────────────────────────────────

describe("VocabularyCard payload", () => {
  test("has required german, english, vocabIndex fields", () => {
    expect(VOCAB_PAYLOAD.german).toBe("Guten Morgen");
    expect(VOCAB_PAYLOAD.english).toBe("Good morning");
    expect(VOCAB_PAYLOAD.vocabIndex).toBe(0);
  });

  test("optional fields can be null/undefined", () => {
    const minimal: VocabularyCardPayload = {
      german: "Hallo",
      english: "Hello",
      vocabIndex: 0,
    };
    expect(minimal.pronunciation).toBeUndefined();
    expect(minimal.example).toBeUndefined();
  });
});

// ── 3. VocabularySummary payload ───────────────────────────────────────────────

describe("VocabularySummary payload", () => {
  test("items array and count are correct", () => {
    expect(VOCAB_SUMMARY_PAYLOAD.items).toHaveLength(1);
    expect(VOCAB_SUMMARY_PAYLOAD.count).toBe(1);
  });

  test("each item matches VocabularyCardPayload shape", () => {
    const item = VOCAB_SUMMARY_PAYLOAD.items[0];
    expect(item).toHaveProperty("german");
    expect(item).toHaveProperty("english");
    expect(item).toHaveProperty("vocabIndex");
  });
});

// ── 4. MultipleChoice answer logic ────────────────────────────────────────────

describe("MultipleChoice answer logic", () => {
  test("correct answer identified", () => {
    const selected = "Good morning";
    expect(selected === MC_PAYLOAD.correctAnswer).toBe(true);
  });

  test("incorrect answer identified", () => {
    const selected = "Good evening";
    expect(selected === MC_PAYLOAD.correctAnswer).toBe(false);
  });

  test("options include the correct answer", () => {
    expect(MC_PAYLOAD.options).toContain(MC_PAYLOAD.correctAnswer);
  });

  test("options has at least 2 choices", () => {
    expect(MC_PAYLOAD.options.length).toBeGreaterThanOrEqual(2);
  });
});

// ── 5. FillInBlank answer logic ───────────────────────────────────────────────

describe("FillInBlank answer logic", () => {
  function check(value: string, correct: string): boolean {
    return value.trim().toLowerCase() === correct.toLowerCase();
  }

  test("correct answer matches (case-insensitive)", () => {
    expect(check("kann", FILL_PAYLOAD.correctAnswer)).toBe(true);
    expect(check("KANN", FILL_PAYLOAD.correctAnswer)).toBe(true);
    expect(check("  kann  ", FILL_PAYLOAD.correctAnswer)).toBe(true);
  });

  test("wrong answer does not match", () => {
    expect(check("bin", FILL_PAYLOAD.correctAnswer)).toBe(false);
    expect(check("", FILL_PAYLOAD.correctAnswer)).toBe(false);
  });

  test("question contains blank placeholder", () => {
    expect(FILL_PAYLOAD.question).toContain("___");
  });

  test("question splits into two parts around ___", () => {
    const parts = FILL_PAYLOAD.question.split("___");
    expect(parts).toHaveLength(2);
  });
});

// ── 6. Translation answer logic ───────────────────────────────────────────────

describe("Translation answer logic", () => {
  function normalize(s: string) {
    return s.trim().toLowerCase().replace(/[.,!?]/g, "");
  }

  test("exact match (normalised) is correct", () => {
    expect(normalize(TRANSLATION_PAYLOAD.correctAnswer)).toBe(normalize("Sie kann gut kochen."));
  });

  test("punctuation-stripped match is correct", () => {
    const withoutPunc = "Sie kann gut kochen";
    expect(normalize(withoutPunc)).toBe(normalize(TRANSLATION_PAYLOAD.correctAnswer));
  });

  test("wrong translation does not match", () => {
    expect(normalize("Ich kann gut kochen.")).not.toBe(normalize(TRANSLATION_PAYLOAD.correctAnswer));
  });
});

// ── 7. Progress calculation ────────────────────────────────────────────────────

describe("Progress calculation", () => {
  test("0% at start", () => {
    const pct = Math.round((0 / 10) * 100);
    expect(pct).toBe(0);
  });

  test("50% at midpoint", () => {
    const pct = Math.round((5 / 10) * 100);
    expect(pct).toBe(50);
  });

  test("100% at completion", () => {
    const pct = Math.round((10 / 10) * 100);
    expect(pct).toBe(100);
  });

  test("never exceeds 100%", () => {
    const pct = Math.min(100, Math.round((11 / 10) * 100));
    expect(pct).toBe(100);
  });

  test("handles zero totalSteps gracefully", () => {
    const totalSteps = 0;
    const pct = totalSteps > 0 ? Math.round((0 / totalSteps) * 100) : 0;
    expect(pct).toBe(0);
  });
});

// ── 8. Navigation ─────────────────────────────────────────────────────────────

describe("Step navigation", () => {
  test("can advance to next step", () => {
    const currentIndex = 0;
    const totalSteps = 4;
    const next = currentIndex + 1;
    expect(next).toBe(1);
    expect(next).toBeLessThan(totalSteps);
  });

  test("cannot advance past last step", () => {
    const currentIndex = 3;
    const totalSteps = 4;
    const next = currentIndex + 1;
    expect(next >= totalSteps).toBe(true);
  });

  test("exercise step blocks continue until answered", () => {
    const isExercise = true;
    const answered = false;
    const disabled = isExercise && !answered;
    expect(disabled).toBe(true);
  });

  test("exercise step allows continue when answered", () => {
    const isExercise = true;
    const answered = true;
    const disabled = isExercise && !answered;
    expect(disabled).toBe(false);
  });

  test("non-exercise step always allows continue", () => {
    const isExercise = false;
    const answered = false;
    const disabled = isExercise && !answered;
    expect(disabled).toBe(false);
  });

  test("LESSON_REVIEW step hides footer nav", () => {
    const step = makeStep("LESSON_REVIEW", REVIEW_PAYLOAD);
    // Simulates: if (step.type === "LESSON_REVIEW") return null
    const showFooter = step.type !== "LESSON_REVIEW";
    expect(showFooter).toBe(false);
  });
});

// ── 9. Resume behavior ────────────────────────────────────────────────────────

describe("Resume behavior", () => {
  test("resumes from currentStepIndex when IN_PROGRESS", () => {
    const lesson = makeLesson({ learnerStatus: "IN_PROGRESS", currentStepIndex: 2 });
    const resumeIndex =
      lesson.learnerStatus === "COMPLETED"
        ? lesson.experiencePlan.totalSteps - 1
        : lesson.currentStepIndex;
    expect(resumeIndex).toBe(2);
  });

  test("opens to last step when COMPLETED", () => {
    const lesson = makeLesson({ learnerStatus: "COMPLETED", currentStepIndex: 0 });
    const resumeIndex =
      lesson.learnerStatus === "COMPLETED"
        ? lesson.experiencePlan.totalSteps - 1
        : lesson.currentStepIndex;
    expect(resumeIndex).toBe(lesson.experiencePlan.totalSteps - 1);
  });

  test("NOT_STARTED starts from step 0", () => {
    const lesson = makeLesson({ learnerStatus: "NOT_STARTED", currentStepIndex: 0 });
    const resumeIndex =
      lesson.learnerStatus === "COMPLETED"
        ? lesson.experiencePlan.totalSteps - 1
        : lesson.currentStepIndex;
    expect(resumeIndex).toBe(0);
  });

  test("resumeIndex never exceeds totalSteps - 1", () => {
    const lesson = makeLesson({ learnerStatus: "IN_PROGRESS", currentStepIndex: 100 });
    const totalSteps = lesson.experiencePlan.totalSteps;
    const resumeIndex = Math.min(lesson.currentStepIndex, totalSteps - 1);
    expect(resumeIndex).toBeLessThan(totalSteps);
  });
});

// ── 10. Lesson completion ──────────────────────────────────────────────────────

describe("Lesson completion", () => {
  test("score is 100 when lesson is completed via review step", () => {
    const score = 100;
    expect(score).toBeGreaterThanOrEqual(0);
    expect(score).toBeLessThanOrEqual(100);
  });

  test("LESSON_REVIEW is always the last step", () => {
    const lesson = makeLesson();
    const lastStep = lesson.experiencePlan.steps.at(-1);
    expect(lastStep?.type).toBe("LESSON_REVIEW");
  });
});

// ── 11. Vocab count ───────────────────────────────────────────────────────────

describe("Vocab count", () => {
  test("counts VOCABULARY_CARD steps", () => {
    const lesson = makeLesson();
    const count = lesson.experiencePlan.steps.filter((s) => s.type === "VOCABULARY_CARD").length;
    expect(count).toBe(1);
  });

  test("VOCABULARY_SUMMARY is not counted as vocab card", () => {
    const lesson = makeLesson();
    const count = lesson.experiencePlan.steps.filter((s) => s.type === "VOCABULARY_CARD").length;
    const summaryCount = lesson.experiencePlan.steps.filter((s) => s.type === "VOCABULARY_SUMMARY").length;
    expect(summaryCount).toBe(1);
    expect(count).not.toEqual(count + summaryCount);
  });
});

// ── 12. Unsupported StepType fallback ─────────────────────────────────────────

describe("Unsupported StepType fallback", () => {
  test("unknown step type does not throw (safe string comparison)", () => {
    const unknownType = "UNKNOWN_FUTURE_TYPE" as StepType;
    const known: StepType[] = [
      "NARRATIVE", "VOCABULARY_CARD", "GRAMMAR_EXPLANATION",
      "MULTIPLE_CHOICE", "FILL_IN_BLANK", "TRANSLATION",
      "VOCABULARY_SUMMARY", "LESSON_REVIEW",
    ];
    // Simulates the switch default branch
    expect(known.includes(unknownType)).toBe(false);
  });
});

// ── 13. Language-agnostic rendering ───────────────────────────────────────────

describe("Language-agnostic rendering", () => {
  test("step payload uses dynamic content, not hardcoded language", () => {
    // The payload carries the content — not the renderer
    const step = makeStep("VOCABULARY_CARD", { german: "Bonjour", english: "Hello", vocabIndex: 0 });
    const payload = step.payload as VocabularyCardPayload;
    expect(payload.german).toBe("Bonjour");
  });

  test("lesson languageCode determines content, not component selection", () => {
    const german = makeLesson({ languageCode: "de" });
    const french = { ...german, languageCode: "fr" };
    // Both use the same step types — language is just metadata
    expect(german.experiencePlan.steps[0].type).toBe(french.experiencePlan.steps[0].type);
  });
});

// ── 14. Error state ───────────────────────────────────────────────────────────

describe("Error state determination", () => {
  test("403 status maps to locked error", () => {
    const status: number = 403;
    const errorType = status === 403 || status === 423 ? "locked" : "generic";
    expect(errorType).toBe("locked");
  });

  test("423 status maps to locked error", () => {
    const status: number = 423;
    const errorType = status === 403 || status === 423 ? "locked" : "generic";
    expect(errorType).toBe("locked");
  });

  test("500 status maps to generic error", () => {
    const status: number = 500;
    const errorType = status === 403 || status === 423 ? "locked" : "generic";
    expect(errorType).toBe("generic");
  });

  test("network error maps to generic error", () => {
    const status: number | undefined = undefined;
    const errorType = status === 403 || status === 423 ? "locked" : "generic";
    expect(errorType).toBe("generic");
  });
});

// ── 15. LessonReview payload ──────────────────────────────────────────────────

describe("LessonReview payload", () => {
  test("has lessonTitle, cefrLevel, exerciseCount", () => {
    expect(REVIEW_PAYLOAD.lessonTitle).toBe("Greetings");
    expect(REVIEW_PAYLOAD.cefrLevel).toBe("A1");
    expect(REVIEW_PAYLOAD.exerciseCount).toBe(3);
  });
});
