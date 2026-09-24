"use client";

import { useState } from "react";
import Link from "next/link";
import { Clock, ChevronRight, Lock, CheckCircle2 } from "lucide-react";
import type { LearningCategory, CategoryStatus } from "@/lib/learn/catalog";
import { formatDuration } from "@/lib/learn/catalog";
import { CATEGORY_VISUALIZATIONS } from "./MiniVisualizations";

interface LearnCategoryCardProps {
  category: LearningCategory;
  index: number;
  completedCount?: number;
  status?: CategoryStatus;
}

const DIFFICULTY_STYLES: Record<string, string> = {
  Easy:   "bg-green-100 text-green-700",
  Medium: "bg-amber-100 text-amber-700",
  Hard:   "bg-red-100 text-red-700",
};

const STATUS_STYLES: Record<CategoryStatus, string> = {
  "not-started": "text-gray-400",
  "in-progress": "text-brand-600",
  "completed":   "text-green-600",
};

const STATUS_LABELS: Record<CategoryStatus, string> = {
  "not-started": "Not Started",
  "in-progress": "In Progress",
  "completed":   "Completed",
};

export default function LearnCategoryCard({
  category,
  index,
  completedCount = 0,
  status = "not-started",
}: LearnCategoryCardProps) {
  const [hovered, setHovered] = useState(false);
  const Viz = CATEGORY_VISUALIZATIONS[category.id];
  const progress = category.conceptCount > 0
    ? Math.round((completedCount / category.conceptCount) * 100)
    : 0;

  const cardContent = (
    <div
      className={`group relative bg-white rounded-2xl border transition-all duration-200 overflow-hidden ${
        hovered && category.available
          ? "border-brand-200 shadow-md shadow-brand-600/5 -translate-y-0.5"
          : status === "completed"
          ? "border-green-200 shadow-sm"
          : "border-gray-200 shadow-sm"
      } ${!category.available ? "opacity-75" : ""}`}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {/* Completion shimmer on completed cards */}
      {status === "completed" && (
        <div className="absolute inset-x-0 top-0 h-0.5 bg-gradient-to-r from-green-400 to-emerald-500" />
      )}
      {/* In-progress accent */}
      {status === "in-progress" && (
        <div className="absolute inset-x-0 top-0 h-0.5 bg-gradient-to-r from-brand-400 to-brand-600" />
      )}

      <div className="p-5">
        {/* Header row */}
        <div className="flex items-start justify-between gap-2 mb-2">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-full bg-gray-100 text-gray-500 text-xs font-bold flex items-center justify-center shrink-0 font-mono">
              {index}
            </span>
            <h3 className={`text-sm font-bold leading-tight ${
              category.available ? "text-gray-900" : "text-gray-500"
            }`}>
              {category.name}
            </h3>
          </div>
          <div className="flex items-center gap-1.5 shrink-0">
            {status === "completed" && (
              <CheckCircle2 size={14} className="text-green-500" />
            )}
            {!category.available && (
              <span className="flex items-center gap-1 text-[10px] font-semibold text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded-full">
                <Lock size={8} />
                Soon
              </span>
            )}
            {category.available && status !== "completed" && (
              <span className={`text-[10px] font-semibold ${STATUS_STYLES[status]}`}>
                {STATUS_LABELS[status]}
              </span>
            )}
            {status === "completed" && (
              <span className="text-[10px] font-semibold text-green-600">Completed</span>
            )}
          </div>
        </div>

        {/* Description */}
        <p className="text-xs text-gray-500 leading-relaxed mb-4 line-clamp-2">
          {category.description}
        </p>

        {/* Mini visualization */}
        <div className={`flex items-center justify-center min-h-[72px] mb-4 rounded-xl transition-colors ${
          status === "in-progress" ? "bg-brand-50/50" : "bg-gray-50"
        }`}>
          {Viz ? (
            <div className="py-2 px-2">
              <Viz animate={hovered} />
            </div>
          ) : (
            <div className="text-xs text-gray-300 font-mono">visualization</div>
          )}
        </div>

        {/* Progress bar (only if started) */}
        {(status === "in-progress" || status === "completed") && (
          <div className="mb-3 space-y-1">
            <div className="flex justify-between text-[10px] text-gray-400">
              <span>{completedCount} / {category.conceptCount} concepts</span>
              <span>{progress}%</span>
            </div>
            <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all ${
                  status === "completed" ? "bg-green-500" : "bg-brand-600"
                }`}
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        )}

        {/* Footer row */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span className={`text-[10px] font-semibold px-1.5 py-0.5 rounded-full ${DIFFICULTY_STYLES[category.difficulty]}`}>
              {category.difficulty}
            </span>
            <span className="flex items-center gap-1 text-[10px] text-gray-400">
              <Clock size={10} />
              {formatDuration(category.estimatedMinutes)}
            </span>
          </div>

          {category.available && (
            <div className={`flex items-center gap-1 text-xs font-semibold transition-colors ${
              hovered ? "text-brand-600" : "text-gray-400"
            }`}>
              {status === "in-progress" ? "Continue" : status === "completed" ? "Review" : "Start"}
              <ChevronRight size={13} className={`transition-transform ${hovered ? "translate-x-0.5" : ""}`} />
            </div>
          )}
        </div>
      </div>
    </div>
  );

  if (!category.available) {
    return cardContent;
  }

  return (
    <Link
      href={`/learn/${category.slug}`}
      className="block focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400 focus-visible:ring-offset-2 rounded-2xl"
      aria-label={`${category.name} — ${STATUS_LABELS[status]}, ${category.conceptCount} concepts, ${formatDuration(category.estimatedMinutes)}`}
    >
      {cardContent}
    </Link>
  );
}
