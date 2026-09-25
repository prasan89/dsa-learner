"use client";

export type LearningPath = "dsa" | "languages";

const KEY = "academy_learning_path";

export function getLearningPath(): LearningPath | null {
  if (typeof window === "undefined") return null;
  const v = localStorage.getItem(KEY);
  return v === "dsa" || v === "languages" ? v : null;
}

export function setLearningPath(path: LearningPath): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(KEY, path);
}

export function clearLearningPath(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(KEY);
}

export function getDashboardForPath(path: LearningPath | null): string {
  if (path === "languages") return "/dashboard";
  return "/dashboard";
}
