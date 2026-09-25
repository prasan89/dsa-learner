import api from "./client";

export interface CfLesson {
  id: string;
  stableRef: string;
  title: string;
  domainCode: string;
  languageCode: string;
  cefrLevel: string;
  contentStatus: string;
  publicationStatus: string;
  currentVersion: number;
  activeVersion: number | null;
  revisionCount: number;
  maxRevisionAttempts: number;
  humanReviewFlag: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CfLessonVersion {
  id: string;
  lessonId: string;
  version: number;
  contentStatus: string;
  publicationStatus: string;
  blueprint: Record<string, unknown> | null;
  content: Record<string, unknown> | null;
  vocabulary: Record<string, unknown> | null;
  grammar: Record<string, unknown> | null;
  exercises: Record<string, unknown> | null;
  revisionFeedback: Record<string, unknown> | null;
  parentVersion: number | null;
  frozen: boolean;
  createdAt: string;
}

export interface CfAgentRun {
  id: string;
  lessonId: string;
  lessonVersion: number;
  agentType: string;
  status: string;
  output: Record<string, unknown> | null;
  idempotencyKey: string;
  inputTokens: number | null;
  outputTokens: number | null;
  estimatedCostUsd: number | null;
  provider: string | null;
  modelId: string | null;
  latencyMs: number | null;
  createdAt: string;
  completedAt: string | null;
}

export const pipelineApi = {
  listLessons: (domain = "language") =>
    api.get<CfLesson[]>(`/v1/pipeline/lessons?domain=${domain}`),

  getLesson: (id: string) =>
    api.get<CfLesson>(`/v1/pipeline/lessons/${id}`),

  listVersions: (lessonId: string) =>
    api.get<CfLessonVersion[]>(`/v1/pipeline/lessons/${lessonId}/versions`),

  getVersion: (lessonId: string, version: number) =>
    api.get<CfLessonVersion>(`/v1/pipeline/lessons/${lessonId}/versions/${version}`),

  getAgentRuns: (lessonId: string, version: number) =>
    api.get<CfAgentRun[]>(`/v1/pipeline/lessons/${lessonId}/runs?version=${version}`),

  getLessonCost: (lessonId: string) =>
    api.get<{ lesson_id: string; total_usd: number }>(`/v1/pipeline/cost/lesson/${lessonId}`),
};
