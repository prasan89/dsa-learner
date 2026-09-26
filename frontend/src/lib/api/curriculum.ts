import api from "./client";

export interface CfCurriculum {
  id: string;
  stableRef: string;
  domainCode: string;
  languageCode: string;
  displayName: string;
  description: string | null;
  curriculumStatus: string;
  curriculumVersion: number;
  activeVersion: number | null;
  publishGatePassed: boolean;
  batchSize: number;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CfCurriculumLevel {
  id: string;
  curriculumId: string;
  cefrLevel: string;
  displayName: string;
  ordinal: number;
  targetLessonCount: number | null;
  levelStatus: string;
  blueprintAgentRunId: string | null;
  levelQaRunId: string | null;
  humanReviewFlag: boolean;
  humanReviewReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CfCurriculumLessonPlan {
  id: string;
  curriculumId: string;
  levelId: string;
  unitId: string;
  lessonId: string | null;
  stableRef: string;
  position: number;
  title: string;
  topic: string | null;
  lessonType: string;
  difficulty: string;
  planStatus: string;
  generationAttempt: number;
  createdAt: string;
  updatedAt: string;
}

export interface CfCurriculumJob {
  jobId: string;
  curriculumId: string;
  levelId: string | null;
  jobType: string;
  status: string;
  attempt: number;
  maxAttempts: number;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  resultReference: string | null;
  error: string | null;
}

export const curriculumApi = {
  list: (languageCode = "de") =>
    api.get<CfCurriculum[]>(`/v1/curriculum?languageCode=${languageCode}`),

  get: (id: string) =>
    api.get<CfCurriculum>(`/v1/curriculum/${id}`),

  create: (body: {
    stableRef: string;
    domainCode: string;
    languageCode: string;
    displayName: string;
    description?: string;
    batchSize?: number;
  }) => api.post<CfCurriculum>("/v1/curriculum", body),

  startBlueprint: (id: string) =>
    api.post<CfCurriculum>(`/v1/curriculum/${id}/start-blueprint`, {}),

  approve: (id: string) =>
    api.post<CfCurriculum>(`/v1/curriculum/${id}/approve`, {}),

  archive: (id: string) =>
    api.post<CfCurriculum>(`/v1/curriculum/${id}/archive`, {}),

  getLevels: (id: string) =>
    api.get<CfCurriculumLevel[]>(`/v1/curriculum/${id}/levels`),

  startLevelGeneration: (curriculumId: string, levelId: string) =>
    api.post<CfCurriculumLevel>(`/v1/curriculum/${curriculumId}/levels/${levelId}/start-generation`, {}),

  approveLevel: (curriculumId: string, levelId: string) =>
    api.post<CfCurriculumLevel>(`/v1/curriculum/${curriculumId}/levels/${levelId}/approve`, {}),

  submitLevelQa: (curriculumId: string, levelId: string) =>
    api.post<CfCurriculumJob>(`/v1/curriculum/${curriculumId}/levels/${levelId}/qa`, {}),

  getLevelPlans: (curriculumId: string, levelId: string) =>
    api.get<CfCurriculumLessonPlan[]>(`/v1/curriculum/${curriculumId}/levels/${levelId}/plans`),

  submitCoherenceQa: (curriculumId: string) =>
    api.post<CfCurriculumJob>(`/v1/curriculum/${curriculumId}/coherence-qa`, {}),

  getJob: (jobId: string) =>
    api.get<CfCurriculumJob>(`/v1/curriculum/jobs/${jobId}`),
};
