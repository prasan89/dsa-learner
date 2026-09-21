import api from "./client";
import type { Pattern } from "@/types";

export const patternsApi = {
  list: () => api.get<Pattern[]>("/patterns"),
  get: (slug: string) => api.get<Pattern>(`/patterns/${slug}`),
};
