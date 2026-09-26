import api from "./client";

// ── Types ─────────────────────────────────────────────────────────────────────

export type LessonStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
export type LevelStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";

export interface LessonSummary {
  lessonId: string;
  title: string;
  status: LessonStatus;
  position: number;
  stepIndex: number;
  score: number | null;
}

export interface Unit {
  unitId: string | null;
  displayName: string | null;
  ordinal: number;
  lessons: LessonSummary[];
}

export interface LevelSummary {
  cefrLevel: string;
  displayName: string;
  ordinal: number;
  status: LevelStatus;
  lessonsTotal: number;
  lessonsCompleted: number;
  avgScore: number | null;
  unlockedAt: string | null;
  completedAt: string | null;
  units: Unit[];
}

export interface CurriculumResponse {
  curriculumId: string;
  languageCode: string;
  displayName: string;
  levels: LevelSummary[];
}

export interface LevelProgressItem {
  cefrLevel: string;
  status: LevelStatus;
  lessonsTotal: number;
  lessonsCompleted: number;
  avgScore: number | null;
}

export interface ProgressResponse {
  curriculumId: string;
  languageCode: string;
  totalLessonsCompleted: number;
  levels: LevelProgressItem[];
}

// ── API client ────────────────────────────────────────────────────────────────

export const academyApi = {
  getCurriculum: (language: string) =>
    api.get<CurriculumResponse>(`/v1/academy/${language}/curriculum`),

  getProgress: (language: string) =>
    api.get<ProgressResponse>(`/v1/academy/${language}/progress`),
};

// ── Derived helpers ───────────────────────────────────────────────────────────

/** The active level is the first IN_PROGRESS level, or A1 if none started. */
export function resolveActiveLevel(levels: LevelSummary[]): string {
  const inProgress = levels.find((l) => l.status === "IN_PROGRESS");
  if (inProgress) return inProgress.cefrLevel;
  const firstAvailable = levels.find((l) => l.status === "NOT_STARTED");
  return firstAvailable?.cefrLevel ?? levels[0]?.cefrLevel ?? "A1";
}

/** Determine the CTA action based on learner state. */
export type CtaAction =
  | { kind: "start"; lesson: LessonSummary }
  | { kind: "continue"; lesson: LessonSummary }
  | { kind: "levelComplete"; nextLevel: string | null }
  | { kind: "noContent" };

export function resolveCtaAction(level: LevelSummary): CtaAction {
  if (level.status === "COMPLETED") {
    // Find the next level (ordinal + 1 if it exists — caller passes full levels list separately if needed)
    return { kind: "levelComplete", nextLevel: null };
  }

  const allLessons = level.units.flatMap((u) => u.lessons);

  const inProgress = allLessons.find((l) => l.status === "IN_PROGRESS");
  if (inProgress) return { kind: "continue", lesson: inProgress };

  const firstAvailable = allLessons.find((l) => l.status === "NOT_STARTED");
  if (firstAvailable) return { kind: "start", lesson: firstAvailable };

  if (allLessons.length === 0) return { kind: "noContent" };

  // All completed
  return { kind: "levelComplete", nextLevel: null };
}

/** Resolve the next CEFR level after the given one (based on ordinal). */
export function resolveNextLevel(levels: LevelSummary[], currentCefr: string): string | null {
  const current = levels.find((l) => l.cefrLevel === currentCefr);
  if (!current) return null;
  const next = levels.find((l) => l.ordinal === current.ordinal + 1);
  return next?.cefrLevel ?? null;
}

export const CEFR_DISPLAY: Record<string, string> = {
  A1: "Beginner",
  A2: "Elementary",
  B1: "Intermediate",
  B2: "Upper Intermediate",
  C1: "Advanced",
  C2: "Mastery",
};
