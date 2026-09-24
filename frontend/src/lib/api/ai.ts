import api from "./client";
import type { AiReview, PatternDetection } from "@/types";

export interface MentorMessage {
  role: "user" | "assistant";
  content: string;
}

export interface MentorContext {
  conceptId: string;
  conceptTitle: string;
  problemSlug?: string;
  currentCode?: string;
  executionResult?: string;
  compilerError?: string;
  attemptCount: number;
  hintsUsed: number;
  masteryLevel?: string;
}

export interface MentorResponse {
  message: string;
  type: "guidance" | "hint" | "question" | "encouragement";
}

export const aiApi = {
  review: (problemSlug: string, code: string) =>
    api.post<AiReview>("/ai/review", { problemSlug, code }),
  detectPattern: (code: string) =>
    api.post<PatternDetection>("/ai/detect-pattern", { code }),
  wallet: () => api.get<{ freeCredits: number; paidCredits: number; totalCredits: number }>("/ai/wallet"),
  mentor: (ctx: MentorContext, userMessage: string, previousMessages?: MentorMessage[]) =>
    api.post<MentorResponse>("/ai/mentor", {
      conceptId: ctx.conceptId,
      conceptTitle: ctx.conceptTitle,
      problemSlug: ctx.problemSlug,
      currentCode: ctx.currentCode,
      executionResult: ctx.executionResult,
      compilerError: ctx.compilerError,
      attemptCount: ctx.attemptCount,
      hintsUsed: ctx.hintsUsed,
      masteryLevel: ctx.masteryLevel,
      userMessage,
      previousMessages: previousMessages ?? [],
    }),
};
