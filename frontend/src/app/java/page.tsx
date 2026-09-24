"use client";

import { useState, useMemo } from "react";
import { Search, Grid3X3, List, Clock, ChevronRight, Lock, CheckCircle2 } from "lucide-react";
import {
  JAVA_TOPICS,
  JAVA_CATEGORY_TABS,
  javaFormatDuration,
  type JavaCategoryType,
  type CategoryStatus,
  type JavaTopic,
} from "@/lib/learn/java-catalog";
import { JAVA_VISUALIZATIONS } from "@/components/learn/JavaMiniViz";

// ── Types ─────────────────────────────────────────────────────────────────────
type TypeFilter = "all" | JavaCategoryType;
type SortMode = "recommended" | "difficulty" | "duration" | "alphabetical";

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

// ── Card ──────────────────────────────────────────────────────────────────────
function JavaTopicCard({
  topic, index, status = "not-started", completedCount = 0,
}: {
  topic: JavaTopic; index: number; status?: CategoryStatus; completedCount?: number;
}) {
  const [hovered, setHovered] = useState(false);
  const Viz = JAVA_VISUALIZATIONS[topic.id];
  const progress = topic.conceptCount > 0
    ? Math.round((completedCount / topic.conceptCount) * 100) : 0;

  return (
    <div
      className={`group relative bg-white rounded-2xl border transition-all duration-200 overflow-hidden ${
        hovered && topic.available
          ? "border-brand-200 shadow-md shadow-brand-600/5 -translate-y-0.5"
          : status === "completed" ? "border-green-200 shadow-sm" : "border-gray-200 shadow-sm"
      } ${!topic.available ? "opacity-75" : ""}`}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {status === "completed" && (
        <div className="absolute inset-x-0 top-0 h-0.5 bg-gradient-to-r from-green-400 to-emerald-500" />
      )}
      {status === "in-progress" && (
        <div className="absolute inset-x-0 top-0 h-0.5 bg-gradient-to-r from-brand-400 to-brand-600" />
      )}

      <div className="p-5">
        {/* Header */}
        <div className="flex items-start justify-between gap-2 mb-2">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-full bg-gray-100 text-gray-500 text-xs font-bold flex items-center justify-center shrink-0 font-mono">
              {index}
            </span>
            <h3 className={`text-sm font-bold leading-tight ${topic.available ? "text-gray-900" : "text-gray-500"}`}>
              {topic.name}
            </h3>
          </div>
          <div className="flex items-center gap-1.5 shrink-0">
            {status === "completed" && <CheckCircle2 size={14} className="text-green-500" />}
            {!topic.available && (
              <span className="flex items-center gap-1 text-[10px] font-semibold text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded-full">
                <Lock size={8} />Soon
              </span>
            )}
            {topic.available && status !== "completed" && (
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
        <p className="text-xs text-gray-500 leading-relaxed mb-4 line-clamp-2">{topic.description}</p>

        {/* Mini Viz */}
        <div className={`flex items-center justify-center min-h-[72px] mb-4 rounded-xl transition-colors ${
          status === "in-progress" ? "bg-brand-50/50" : "bg-gray-50"
        }`}>
          {Viz ? (
            <div className="py-2 px-2 w-full">
              <Viz animate={hovered} />
            </div>
          ) : (
            <div className="text-xs text-gray-300 font-mono">visualization</div>
          )}
        </div>

        {/* Progress bar */}
        {(status === "in-progress" || status === "completed") && (
          <div className="mb-3 space-y-1">
            <div className="flex justify-between text-[10px] text-gray-400">
              <span>{completedCount} / {topic.conceptCount} concepts</span>
              <span>{progress}%</span>
            </div>
            <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all ${status === "completed" ? "bg-green-500" : "bg-brand-600"}`}
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span className={`text-[10px] font-semibold px-1.5 py-0.5 rounded-full ${DIFFICULTY_STYLES[topic.difficulty]}`}>
              {topic.difficulty}
            </span>
            <span className="flex items-center gap-1 text-[10px] text-gray-400">
              <Clock size={10} />
              {javaFormatDuration(topic.estimatedMinutes)}
            </span>
          </div>
          {topic.available && (
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
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function JavaPage() {
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("all");
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState<SortMode>("recommended");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");

  const filtered = useMemo(() => {
    let items = JAVA_TOPICS;
    if (typeFilter !== "all") {
      items = items.filter(t => t.type === typeFilter);
    }
    if (search.trim()) {
      const q = search.toLowerCase();
      items = items.filter(t =>
        t.name.toLowerCase().includes(q) ||
        t.description.toLowerCase().includes(q)
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
    return items;
  }, [typeFilter, search, sort]);

  const countFor = (val: string) =>
    val === "all"
      ? JAVA_TOPICS.length
      : JAVA_TOPICS.filter(t => t.type === val).length;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-6 py-10 space-y-8">

        {/* ── Hero ─────────────────────────────────────────────────────── */}
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">Java Expert</h1>
          <p className="text-base text-gray-500 leading-relaxed max-w-xl">
            Master Java from fundamentals to advanced concepts with hands-on examples
            and real-world engineering practices.
          </p>
        </div>

        {/* ── Category Tabs ─────────────────────────────────────────────── */}
        <div className="flex flex-wrap gap-1.5">
          {JAVA_CATEGORY_TABS.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => setTypeFilter(value as TypeFilter)}
              className={`px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-all ${
                typeFilter === value
                  ? "bg-brand-600 text-white border-brand-600 shadow-sm"
                  : "bg-white text-gray-500 border-gray-200 hover:border-brand-200 hover:text-brand-600"
              }`}
            >
              {label}
              <span className="ml-1.5 opacity-70">({countFor(value)})</span>
            </button>
          ))}
        </div>

        {/* ── Toolbar ───────────────────────────────────────────────────── */}
        <div className="flex items-center gap-3 flex-wrap">
          {/* Sort */}
          <select
            value={sort}
            onChange={e => setSort(e.target.value as SortMode)}
            className="px-3 py-2 text-xs bg-white border border-gray-200 rounded-xl text-gray-600 focus:outline-none focus:ring-2 focus:ring-brand-400/30 pr-7"
          >
            <option value="recommended">Recommended</option>
            <option value="difficulty">Difficulty</option>
            <option value="duration">Duration</option>
            <option value="alphabetical">A–Z</option>
          </select>

          {/* View toggle */}
          <div className="flex items-center border border-gray-200 rounded-xl overflow-hidden bg-white">
            <button
              onClick={() => setViewMode("grid")}
              aria-pressed={viewMode === "grid"}
              aria-label="grid view"
              className={`p-2 transition-colors ${viewMode === "grid" ? "bg-brand-600 text-white" : "text-gray-400 hover:text-gray-600"}`}
            >
              <Grid3X3 size={14} />
            </button>
            <button
              onClick={() => setViewMode("list")}
              aria-pressed={viewMode === "list"}
              aria-label="list view"
              className={`p-2 transition-colors ${viewMode === "list" ? "bg-brand-600 text-white" : "text-gray-400 hover:text-gray-600"}`}
            >
              <List size={14} />
            </button>
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
              placeholder="Search topics..."
              className="pl-8 pr-4 py-2 text-xs bg-white border border-gray-200 rounded-xl w-48 focus:outline-none focus:ring-2 focus:ring-brand-400/30 focus:border-brand-300"
            />
          </div>
        </div>

        {/* ── Count label ───────────────────────────────────────────────── */}
        <p className="text-sm text-gray-500">
          <span className="font-semibold text-gray-900">{filtered.length}</span> topics
        </p>

        {/* ── Card grid ─────────────────────────────────────────────────── */}
        {filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <p className="text-2xl mb-3">🔍</p>
            <p className="text-sm font-semibold text-gray-700">No topics found</p>
            <p className="text-xs text-gray-400 mt-1">Try a different search or filter</p>
            <button
              onClick={() => { setSearch(""); setTypeFilter("all"); }}
              className="mt-4 text-xs font-semibold text-brand-600 hover:underline"
            >
              Clear all filters
            </button>
          </div>
        ) : (
          <ul className={
            viewMode === "grid"
              ? "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
              : "flex flex-col gap-3"
          }>
            {filtered.map((topic, i) => (
              <li key={topic.id}>
                <JavaTopicCard topic={topic} index={i + 1} />
              </li>
            ))}
          </ul>
        )}

      </div>
    </div>
  );
}
