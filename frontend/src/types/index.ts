// ─── Auth ────────────────────────────────────────────────────
export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
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
export interface Pattern {
  id: string;
  slug: string;
  name: string;
  summary: string;
  recognitionClues: string[];
  templateCode: string;
  order: number;
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
