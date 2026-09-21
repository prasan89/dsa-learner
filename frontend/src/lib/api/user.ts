import api from "./client";

export const userApi = {
  progress: () => api.get<{
    totalSolved: number;
    easySolved: number;
    mediumSolved: number;
    hardSolved: number;
  }>("/users/me/progress"),

  recentSubmissions: () => api.get<any[]>("/users/me/submissions"),
};
