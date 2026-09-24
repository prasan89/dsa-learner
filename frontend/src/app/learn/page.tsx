"use client";

import { useState, useMemo, useEffect } from "react";
import { Search, Grid3X3, List, Eye, Zap, Bot, CheckSquare } from "lucide-react";
import {
  LEARNING_CATEGORIES,
  LEARNING_PATH_STAGES,
  type CategoryType,
  type CategoryStatus,
  type LearningCategory,
} from "@/lib/learn/catalog";
import LearnCategoryCard from "@/components/learn/LearnCategoryCard";
import { userApi } from "@/lib/api/user";

// ── Learning-path stage component ─────────────────────────────────────────
function LearningPathBar({ currentStage }: { currentStage: number }) {
  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-sm font-bold text-gray-900">Your Learning Path</h2>
        <span className="text-xs text-brand-600 font-semibold cursor-pointer hover:underline">
          View Details →
        </span>
      </div>
      <div className="flex items-start gap-0">
        {LEARNING_PATH_STAGES.map((stage, i) => {
          const done = i < currentStage;
          const active = i === currentStage;
          return (
            <div key={i} className="flex items-start flex-1">
              <div className="flex flex-col items-center flex-1">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold border-2 transition-colors ${
                  done
                    ? "bg-brand-600 border-brand-600 text-white"
                    : active
                    ? "bg-white border-brand-600 text-brand-600"
                    : "bg-white border-gray-200 text-gray-400"
                }`}>
                  {done ? "✓" : i + 1}
                </div>
                <div className="mt-1.5 text-center">
                  <p className={`text-xs font-semibold leading-tight ${
                    active ? "text-brand-600" : done ? "text-gray-700" : "text-gray-400"
                  }`}>
                    {stage.label}
                  </p>
                  {active && (
                    <p className="text-[10px] text-brand-500 font-medium mt-0.5">In Progress</p>
                  )}
                  <p className="text-[10px] text-gray-400 mt-0.5">{stage.count} concepts</p>
                </div>
              </div>
              {i < LEARNING_PATH_STAGES.length - 1 && (
                <div className={`mt-4 flex-shrink-0 w-full max-w-[40px] h-0.5 mx-auto ${
                  done ? "bg-brand-600" : "bg-gray-200"
                }`} style={{ flexBasis: "40px", flexGrow: 0 }} />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ── Feature pills ─────────────────────────────────────────────────────────
const FEATURES = [
  { icon: Eye,         label: "Interactive Visualizations" },
  { icon: CheckSquare, label: "Guided Practice" },
  { icon: Bot,         label: "AI Mentor Support" },
  { icon: Zap,         label: "Track Your Progress" },
];

// ── Filter types ─────────────────────────────────────────────────────────
type TypeFilter = "all" | CategoryType;
type StatusFilter = "all" | CategoryStatus;
type SortMode = "recommended" | "difficulty" | "duration" | "alphabetical";

const TYPE_FILTERS: { value: TypeFilter; label: string }[] = [
  { value: "all",              label: "All" },
  { value: "data-structures",  label: "Data Structures" },
  { value: "algorithms",       label: "Algorithms" },
  { value: "advanced",         label: "Advanced" },
];

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: "all",          label: "All" },
  { value: "not-started",  label: "To Do" },
  { value: "in-progress",  label: "In Progress" },
  { value: "completed",    label: "Completed" },
];

// Mock: in a real implementation these would come from backend.
// Right now the only real page is /learn/arrays, so we show it as "in-progress"
// with hard-coded demo state while actual per-category progress API is built.
function useCategoryProgress() {
  const [dsaProgress, setDsaProgress] = useState<{ solved: number; total: number } | null>(null);

  useEffect(() => {
    userApi.dashboard()
      .then(r => setDsaProgress({ solved: r.data.dsa.solved, total: r.data.dsa.total }))
      .catch(() => setDsaProgress(null));
  }, []);

  // For now: only arrays has a live page. Give it a simulated in-progress state
  // based on whether the user has solved any problems.
  const categoryProgress: Record<string, { completed: number; status: CategoryStatus }> = useMemo(() => {
    const hasSolvedSome = dsaProgress && dsaProgress.solved > 0;
    return {
      arrays: hasSolvedSome
        ? { completed: Math.min(3, dsaProgress!.solved), status: "in-progress" }
        : { completed: 0, status: "not-started" },
    };
  }, [dsaProgress]);

  return categoryProgress;
}

// ── Page ──────────────────────────────────────────────────────────────────
export default function LearnPage() {
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("all");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState<SortMode>("recommended");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");

  const categoryProgress = useCategoryProgress();

  const filtered = useMemo(() => {
    let items = LEARNING_CATEGORIES;

    if (typeFilter !== "all") {
      items = items.filter(c => c.type === typeFilter);
    }

    if (statusFilter !== "all") {
      items = items.filter(c => {
        const st = categoryProgress[c.id]?.status ?? "not-started";
        return st === statusFilter;
      });
    }

    if (search.trim()) {
      const q = search.toLowerCase();
      items = items.filter(c =>
        c.name.toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q)
      );
    }

    if (sort === "difficulty") {
      const order: Record<string, number> = { Easy: 0, Medium: 1, Hard: 2 };
      items = [...items].sort((a, b) => order[a.difficulty] - order[b.difficulty]);
    } else if (sort === "duration") {
      items = [...items].sort((a, b) => a.estimatedMinutes - b.estimatedMinutes);
    } else if (sort === "alphabetical") {
      items = [...items].sort((a, b) => a.name.localeCompare(b.name));
    }
    // "recommended" = default catalog order

    return items;
  }, [typeFilter, statusFilter, search, sort, categoryProgress]);

  const totalConcepts = LEARNING_CATEGORIES.reduce((s, c) => s + c.conceptCount, 0);
  const completedConcepts = Object.values(categoryProgress).reduce((s, p) => s + p.completed, 0);
  const inProgressCount = Object.values(categoryProgress).filter(p => p.status === "in-progress").length;

  // Determine current stage (0-based index of LEARNING_PATH_STAGES)
  const currentStage = inProgressCount > 0 ? 0 : 0;

  const activeCount = (filter: TypeFilter) =>
    filter === "all"
      ? LEARNING_CATEGORIES.length
      : LEARNING_CATEGORIES.filter(c => c.type === filter).length;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-6 py-10 space-y-8">

        {/* ── Hero ─────────────────────────────────────────────────────── */}
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
          <div className="space-y-3 max-w-xl">
            <h1 className="text-3xl font-bold tracking-tight text-gray-900">Learn</h1>
            <p className="text-base text-gray-500 leading-relaxed">
              Master Data Structures &amp; Algorithms through visual, interactive learning.
            </p>
            {/* Feature pills */}
            <div className="flex flex-wrap gap-2 pt-1">
              {FEATURES.map(({ icon: Icon, label }) => (
                <div key={label} className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-gray-200 shadow-sm">
                  <Icon size={12} className="text-brand-500" />
                  <span className="text-xs font-medium text-gray-600">{label}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Learning path */}
          <div className="w-full lg:w-[460px] shrink-0">
            <LearningPathBar currentStage={currentStage} />
          </div>
        </div>

        {/* ── Filters + Search ─────────────────────────────────────────── */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-3 flex-wrap">
          {/* Type filters */}
          <div className="flex items-center gap-1.5 flex-wrap">
            {TYPE_FILTERS.map(({ value, label }) => (
              <button
                key={value}
                onClick={() => setTypeFilter(value)}
                className={`px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-all ${
                  typeFilter === value
                    ? "bg-brand-600 text-white border-brand-600 shadow-sm"
                    : "bg-white text-gray-500 border-gray-200 hover:border-brand-200 hover:text-brand-600"
                }`}
              >
                {label}
                {value !== "all" && (
                  <span className="ml-1.5 opacity-70">({activeCount(value)})</span>
                )}
                {value === "all" && (
                  <span className="ml-1.5 opacity-70">({LEARNING_CATEGORIES.length})</span>
                )}
              </button>
            ))}
          </div>

          {/* Divider */}
          <div className="hidden sm:block w-px h-5 bg-gray-200" />

          {/* Status filters */}
          <div className="flex items-center gap-1.5 flex-wrap">
            {STATUS_FILTERS.map(({ value, label }) => (
              <button
                key={value}
                onClick={() => setStatusFilter(value)}
                className={`px-3 py-1.5 rounded-full text-xs font-medium border transition-all ${
                  statusFilter === value
                    ? "bg-gray-900 text-white border-gray-900"
                    : "bg-white text-gray-500 border-gray-200 hover:border-gray-300 hover:text-gray-700"
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          {/* Spacer */}
          <div className="flex-1" />

          {/* Search */}
          <div className="relative">
            <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search concepts..."
              aria-label="Search learning concepts"
              className="pl-8 pr-3 py-1.5 rounded-xl border border-gray-200 bg-white text-xs text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-400/30 focus:border-brand-300 w-44 transition-all"
            />
          </div>

          {/* Sort */}
          <select
            value={sort}
            onChange={e => setSort(e.target.value as SortMode)}
            aria-label="Sort concepts"
            className="px-3 py-1.5 rounded-xl border border-gray-200 bg-white text-xs text-gray-600 focus:outline-none focus:ring-2 focus:ring-brand-400/30 focus:border-brand-300 cursor-pointer"
          >
            <option value="recommended">Recommended</option>
            <option value="difficulty">Difficulty</option>
            <option value="duration">Duration</option>
            <option value="alphabetical">A → Z</option>
          </select>

          {/* View toggle */}
          <div className="flex items-center gap-0.5 border border-gray-200 rounded-xl overflow-hidden bg-white p-0.5">
            <button
              onClick={() => setViewMode("grid")}
              aria-label="Grid view"
              aria-pressed={viewMode === "grid"}
              className={`p-1.5 rounded-lg transition-colors ${viewMode === "grid" ? "bg-brand-600 text-white" : "text-gray-400 hover:text-gray-600"}`}
            >
              <Grid3X3 size={13} />
            </button>
            <button
              onClick={() => setViewMode("list")}
              aria-label="List view"
              aria-pressed={viewMode === "list"}
              className={`p-1.5 rounded-lg transition-colors ${viewMode === "list" ? "bg-brand-600 text-white" : "text-gray-400 hover:text-gray-600"}`}
            >
              <List size={13} />
            </button>
          </div>
        </div>

        {/* ── Stats strip ──────────────────────────────────────────────── */}
        <div className="flex items-center gap-1.5 text-xs text-gray-400">
          <span className="font-semibold text-gray-600">{filtered.length}</span>
          <span>{filtered.length === 1 ? "topic" : "topics"}</span>
          {search && <span>matching &quot;{search}&quot;</span>}
          {completedConcepts > 0 && (
            <>
              <span className="mx-1">·</span>
              <span className="text-green-600 font-medium">{completedConcepts} concepts completed</span>
            </>
          )}
        </div>

        {/* ── Card grid ────────────────────────────────────────────────── */}
        {filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="w-12 h-12 rounded-2xl bg-gray-100 flex items-center justify-center mb-3">
              <Search size={20} className="text-gray-400" />
            </div>
            <p className="text-sm font-medium text-gray-700">No topics found</p>
            <p className="text-xs text-gray-400 mt-1">Try adjusting your filters or search terms.</p>
            <button
              onClick={() => { setSearch(""); setTypeFilter("all"); setStatusFilter("all"); }}
              className="mt-4 text-xs text-brand-600 hover:underline font-medium"
            >
              Clear all filters
            </button>
          </div>
        ) : (
          <div
            role="list"
            className={
              viewMode === "grid"
                ? "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
                : "flex flex-col gap-3"
            }
          >
            {filtered.map((category, i) => {
              const progress = categoryProgress[category.id];
              return (
                <div key={category.id} role="listitem">
                  <LearnCategoryCard
                    category={category}
                    index={LEARNING_CATEGORIES.indexOf(category) + 1}
                    completedCount={progress?.completed ?? 0}
                    status={progress?.status ?? "not-started"}
                  />
                </div>
              );
            })}
          </div>
        )}

        {/* ── Coming soon note ─────────────────────────────────────────── */}
        <p className="text-center text-xs text-gray-400 pb-4">
          {LEARNING_CATEGORIES.filter(c => !c.available).length} more topics coming soon.
          New content added weekly.
        </p>
      </div>
    </div>
  );
}
