import api from "./client";
import type { AiReview, PatternDetection } from "@/types";

export const aiApi = {
  review: (problemSlug: string, code: string) =>
    api.post<AiReview>("/ai/review", { problemSlug, code }),
  detectPattern: (code: string) =>
    api.post<PatternDetection>("/ai/detect-pattern", { code }),
  remaining: () => api.get<{ remaining: number }>("/ai/review/remaining"),
};
