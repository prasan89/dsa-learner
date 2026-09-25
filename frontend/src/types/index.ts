// ─── Auth ────────────────────────────────────────────────────
export type LearningPath = "dsa" | "languages";

export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  learningPath?: LearningPath;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

// ─── Problem ─────────────────────────────────────────────────
export type Difficulty = "EASY" | "MEDIUM" | "HARD";

export interface Problem {
  id: string;
  slug: string;
  title: string;
  difficulty: Difficulty;
  description: string;
  constraints: string;
  examples: ProblemExample[];
  tags: string[];
  patternIds: string[];
  acceptanceRate: number;
}

export interface ProblemExample {
  input: string;
  output: string;
  explanation?: string;
}

export interface TestCase {
  id: string;
  input: string;
  expectedOutput: string;
  isHidden: boolean;
}

// ─── Pattern ─────────────────────────────────────────────────
export type MasteryStatus = "NOT_STARTED" | "LEARNING" | "PRACTICED" | "MASTERED";

export interface Pattern {
  id: string;
  slug: string;
  name: string;
  summary: string;
  recognitionClues: string[];
  templateCode: string;
  order: number;
  lessonMarkdown?: string;
  timeComplexity?: string;
  spaceComplexity?: string;
  masteryStatus?: MasteryStatus;
  category?: string;
}

// ─── Hints ───────────────────────────────────────────────────
export interface Hint {
  id: string;
  level: 1 | 2 | 3;
  label?: string;
  content: string;
  unlocked: boolean;
}

// ─── AI ──────────────────────────────────────────────────────
export interface AiReview {
  timeComplexity: string;
  spaceComplexity: string;
  strengths: string;
  improvements: string;
  patternUsed: string;
  optimizedApproach: string;
}

export interface PatternDetection {
  patternName: string;
  patternSlug: string;
  explanation: string;
  confidence: "HIGH" | "MEDIUM" | "LOW";
}

// ─── Submission ──────────────────────────────────────────────
export type SubmissionStatus =
  | "ACCEPTED"
  | "WRONG_ANSWER"
  | "TIME_LIMIT_EXCEEDED"
  | "COMPILATION_ERROR"
  | "RUNTIME_ERROR";

export interface Submission {
  id: string;
  problemId: string;
  userId: string;
  code: string;
  language: "JAVA";
  status: SubmissionStatus;
  runtime?: number;
  memory?: number;
  submittedAt: string;
}

export interface RunResult {
  status: SubmissionStatus;
  output?: string;
  errorMessage?: string;
  testResults: TestResult[];
  runtime?: number;
}

export interface TestResult {
  testCaseId: string;
  passed: boolean;
  input: string;
  expectedOutput: string;
  actualOutput: string;
  executionTimeMs?: number;
}

// ─── Progress ────────────────────────────────────────────────
export interface UserProgress {
  totalSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  totalAttempted: number;
  patternProgress: PatternProgress[];
}

export interface PatternProgress {
  patternId: string;
  patternName: string;
  solved: number;
  total: number;
  masteryScore: number;
}
