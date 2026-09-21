import api from "./client";
import type { Pattern, MasteryStatus } from "@/types";

export const patternsApi = {
  list: () => api.get<Pattern[]>("/patterns"),
  get: (slug: string) => api.get<Pattern>(`/patterns/${slug}`),
  getMastery: () => api.get<{ patternId: string; patternSlug: string; patternName: string; status: string }[]>("/patterns/mastery"),
  updateMastery: (slug: string, status: MasteryStatus) =>
    api.put(`/patterns/${slug}/mastery`, { status }),
};
