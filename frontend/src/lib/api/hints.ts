import api from "./client";
import type { Hint } from "@/types";

export const hintsApi = {
  list: (slug: string) => api.get<Hint[]>(`/problems/${slug}/hints`),
  unlock: (slug: string, level: number) =>
    api.post<Hint>(`/problems/${slug}/hints/${level}/unlock`, {}),
};
