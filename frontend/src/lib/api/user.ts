import api from "./client";

export interface DashboardData {
  streak: number;
  dsa: { total: number; solved: number; masteryAvg: number };
  systemDesign: { total: number; mastered: number; masteryAvg: number };
  plan: string;
  nextAction: { slug: string; title: string; patternName: string; difficulty: string } | null;
}

export const userApi = {
  progress: () => api.get<{
    totalSolved: number;
    easySolved: number;
    mediumSolved: number;
    hardSolved: number;
  }>("/users/me/progress"),

  dashboard: () => api.get<DashboardData>("/users/me/dashboard"),

  recentSubmissions: () => api.get<any[]>("/users/me/submissions"),
};

