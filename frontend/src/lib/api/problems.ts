import api from "./client";
import type { Problem, RunResult, Submission } from "@/types";

export const problemsApi = {
  list: (params?: { patternId?: string; difficulty?: string; page?: number }) =>
    api.get<{ problems: any[]; total: number; totalPages: number }>("/problems", { params }),

  get: (slug: string) => api.get<Problem>(`/problems/${slug}`),

  run: (slug: string, code: string) =>
    api.post<RunResult>(`/problems/${slug}/run`, { code, language: "JAVA" }),

  submit: (slug: string, code: string) =>
    api.post<Submission>(`/problems/${slug}/submit`, { code, language: "JAVA" }),

  submissions: (slug: string) =>
    api.get<Submission[]>(`/problems/${slug}/submissions`),
};
