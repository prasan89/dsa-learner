import api from "./client";
import type { Problem, Submission, RunResult } from "@/types";

export const problemsApi = {
  list: (params?: { patternId?: string; difficulty?: string; page?: number }) =>
    api.get<{ problems: Problem[]; total: number }>("/problems", { params }),

  get: (slug: string) => api.get<Problem>(`/problems/${slug}`),

  run: (problemId: string, code: string) =>
    api.post<RunResult>(`/problems/${problemId}/run`, { code, language: "JAVA" }),

  submit: (problemId: string, code: string) =>
    api.post<Submission>(`/problems/${problemId}/submit`, { code, language: "JAVA" }),

  submissions: (problemId: string) =>
    api.get<Submission[]>(`/problems/${problemId}/submissions`),
};
