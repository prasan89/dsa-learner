import api from "./client";
import type { User, AuthTokens } from "@/types";

export const authApi = {
  register: (data: { name: string; email: string; password: string }) =>
    api.post<{ user: User; accessToken: string; refreshToken: string }>("/auth/register", data),

  login: (data: { email: string; password: string }) =>
    api.post<{ user: User; accessToken: string; refreshToken: string }>("/auth/login", data),

  logout: () => api.post("/auth/logout"),

  me: () => api.get<User>("/auth/me"),

  refresh: (refreshToken: string) =>
    api.post<AuthTokens>("/auth/refresh", { refreshToken }),
};
