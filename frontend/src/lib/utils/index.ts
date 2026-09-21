import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function difficultyColor(difficulty: string) {
  return {
    EASY:   "text-green-400",
    MEDIUM: "text-yellow-400",
    HARD:   "text-red-400",
  }[difficulty] ?? "text-gray-400";
}

export function difficultyBadge(difficulty: string) {
  return {
    EASY:   "badge-easy",
    MEDIUM: "badge-medium",
    HARD:   "badge-hard",
  }[difficulty] ?? "";
}

export function formatRuntime(ms: number) {
  return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(1)}s`;
}
