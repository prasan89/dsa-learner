"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams, useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import {
  CheckCircle2,
  Circle,
  Lock,
  ArrowRight,
  RefreshCw,
  ChevronDown,
  ChevronUp,
  BookOpen,
  Layers,
  Clock,
  Target,
} from "lucide-react";
import {
  academyApi,
  resolveActiveLevel,
  resolveCtaAction,
  resolveNextLevel,
  CEFR_DISPLAY,
  type CurriculumResponse,
  type LevelSummary,
  type LessonSummary,
  type CtaAction,
  type Unit,
} from "@/lib/api/academy";
import { cn } from "@/lib/utils";

// ── Static data ───────────────────────────────────────────────────────────────

const CEFR_DESCRIPTION: Record<string, string> = {
  A1: "Build the foundations for everyday conversation.",
  A2: "Master common phrases and essential daily vocabulary.",
  B1: "Express yourself in most everyday situations.",
  B2: "Discuss complex topics and abstract ideas with confidence.",
  C1: "Communicate flexibly in social and professional settings.",
  C2: "Understand virtually everything heard and read with ease.",
};

const CEFR_GOALS: Record<string, string[]> = {
  A1: ["Introduce yourself and others", "Ask and answer basic questions", "Talk about family and daily life", "Handle simple everyday situations"],
  A2: ["Describe your environment", "Express routines and habits", "Give and follow directions", "Talk about past and future events"],
  B1: ["Handle travel and work situations", "Describe experiences and events", "Give reasons and explain plans", "Understand native speakers on familiar topics"],
  B2: ["Understand complex texts", "Discuss abstract topics", "Express opinions with nuance", "Interact with fluency and spontaneity"],
  C1: ["Use language flexibly and effectively", "Understand implicit meaning", "Express ideas fluently and precisely", "Navigate social and professional contexts"],
  C2: ["Understand everything heard or read", "Summarize information from any source", "Express fine shades of meaning", "Achieve full mastery"],
};

const CEFR_HOURS: Record<string, string> = {
  A1: "15–20 hours", A2: "20–30 hours", B1: "30–40 hours",
  B2: "40–60 hours", C1: "60–80 hours", C2: "80–100 hours",
};

// Language flag emojis — language-agnostic fallback is globe
const LANG_FLAGS: Record<string, string> = {
  de: "🇩🇪", fr: "🇫🇷", es: "🇪🇸", it: "🇮🇹", pt: "🇵🇹",
  ja: "🇯🇵", ko: "🇰🇷", zh: "🇨🇳", ru: "🇷🇺", ar: "🇸🇦",
  nl: "🇳🇱", pl: "🇵🇱", sv: "🇸🇪", da: "🇩🇰", fi: "🇫🇮",
  no: "🇳🇴", tr: "🇹🇷", hi: "🇮🇳",
};

// Lesson type tags derived from position within unit (simple heuristic)
function lessonTypeTag(lesson: LessonSummary, unitLessons: LessonSummary[]): string {
  const idx = unitLessons.indexOf(lesson);
  const total = unitLessons.length;
  if (total <= 1) return "Lesson";
  if (idx === 0) return "Vocabulary";
  if (idx === total - 1) return "Practice";
  if (idx % 2 === 1) return "Grammar";
  return "Vocabulary";
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

function Pulse({ className }: { className?: string }) {
  return <div className={cn("animate-pulse bg-gray-100 rounded", className)} />;
}

function PageSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero */}
      <Pulse className="w-full h-48 rounded-none" />
      <div className="max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex gap-8">
        <div className="flex-1 space-y-5">
          {/* Rail */}
          <div className="flex gap-6">
            {[...Array(6)].map((_, i) => <Pulse key={i} className="w-14 h-14 rounded-full" />)}
          </div>
          {/* CTA */}
          <Pulse className="h-32 w-full rounded-2xl" />
          {/* Units */}
          {[...Array(3)].map((_, i) => <Pulse key={i} className="h-16 w-full rounded-xl" />)}
        </div>
        {/* Sidebar */}
        <div className="hidden xl:block w-72 space-y-4">
          <Pulse className="h-56 rounded-2xl" />
          <Pulse className="h-40 rounded-2xl" />
        </div>
      </div>
    </div>
  );
}

// ── Language hero banner ──────────────────────────────────────────────────────

function LanguageHero({
  language,
  displayName,
  level,
}: {
  language: string;
  displayName: string;
  level: LevelSummary;
}) {
  const flag = LANG_FLAGS[language.toLowerCase()] ?? "🌐";
  const cefrLabel = CEFR_DISPLAY[level.cefrLevel] ?? level.cefrLevel;
  const description = CEFR_DESCRIPTION[level.cefrLevel] ?? "";
  const hours = CEFR_HOURS[level.cefrLevel] ?? "";

  // Gradient backgrounds per language for visual identity (no external images needed)
  const heroBg: Record<string, string> = {
    de: "from-slate-700 via-slate-600 to-slate-500",
    fr: "from-blue-700 via-blue-600 to-indigo-500",
    es: "from-red-700 via-orange-600 to-yellow-500",
    it: "from-green-700 via-emerald-600 to-teal-500",
    ko: "from-indigo-700 via-purple-600 to-violet-500",
    ja: "from-rose-700 via-pink-600 to-red-500",
    zh: "from-red-800 via-red-700 to-orange-600",
  };
  const bg = heroBg[language.toLowerCase()] ?? "from-brand-700 via-brand-600 to-brand-500";

  return (
    <div className={cn("relative bg-gradient-to-r overflow-hidden", bg)}>
      {/* Decorative overlay */}
      <div className="absolute inset-0 bg-black/20" aria-hidden="true" />
      <div className="relative max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-start gap-4">
          <span className="text-5xl leading-none" aria-hidden="true">{flag}</span>
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-1">
              <h1 className="text-3xl font-bold text-white">{displayName}</h1>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white/20 text-white border border-white/30">
                {level.cefrLevel} · {cefrLabel}
              </span>
            </div>
            <p className="text-white/80 text-sm mb-4 leading-relaxed">{description}</p>
            <div className="flex flex-wrap items-center gap-4 text-white/70 text-sm">
              <span className="flex items-center gap-1.5">
                <BookOpen size={14} aria-hidden="true" />
                {level.lessonsTotal} lessons
              </span>
              <span className="flex items-center gap-1.5">
                <Layers size={14} aria-hidden="true" />
                {level.units.length} units
              </span>
              {hours && (
                <span className="flex items-center gap-1.5">
                  <Clock size={14} aria-hidden="true" />
                  ~{hours}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Level rail ────────────────────────────────────────────────────────────────

function LevelRail({
  levels,
  activeCefr,
  onSelect,
}: {
  levels: LevelSummary[];
  activeCefr: string;
  onSelect: (cefr: string) => void;
}) {
  return (
    <nav aria-label="CEFR levels" className="relative">
      <div className="absolute top-5 left-0 right-0 h-0.5 bg-gray-200" aria-hidden="true" />
      <ol className="relative flex items-start justify-between">
        {levels.map((level) => {
          const isActive = level.cefrLevel === activeCefr;
          const isCompleted = level.status === "COMPLETED";
          const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
          const isInProgress = level.status === "IN_PROGRESS";
          const label = CEFR_DISPLAY[level.cefrLevel] ?? level.cefrLevel;

          return (
            <li key={level.cefrLevel} className="flex flex-col items-center gap-1.5 flex-1">
              <button
                aria-label={`${level.cefrLevel} — ${label}${isCompleted ? " (completed)" : isLocked ? " (locked)" : isActive ? " (current)" : ""}`}
                aria-current={isActive ? "step" : undefined}
                aria-disabled={isLocked}
                onClick={() => !isLocked && onSelect(level.cefrLevel)}
                className={cn(
                  "relative z-10 w-10 h-10 rounded-full flex items-center justify-center transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 text-sm font-bold",
                  isActive && "bg-brand-600 text-white shadow-md ring-4 ring-brand-100 scale-110",
                  isCompleted && !isActive && "bg-green-500 text-white hover:bg-green-600",
                  isInProgress && !isActive && "bg-brand-100 text-brand-700 border-2 border-brand-400 hover:border-brand-600",
                  !isActive && !isCompleted && !isInProgress && !isLocked && "bg-white border-2 border-gray-300 text-gray-500 hover:border-brand-400 hover:text-brand-600",
                  isLocked && "bg-gray-100 border-2 border-gray-200 text-gray-400 cursor-not-allowed"
                )}
              >
                {isCompleted && !isActive ? (
                  <CheckCircle2 size={16} aria-hidden="true" />
                ) : isLocked ? (
                  <Lock size={12} aria-hidden="true" />
                ) : (
                  <span>{level.cefrLevel}</span>
                )}
              </button>
              <span className={cn(
                "text-[10px] font-medium text-center leading-tight px-0.5",
                isActive ? "text-brand-700 font-semibold" : isCompleted ? "text-green-600" : isLocked ? "text-gray-300" : "text-gray-500"
              )}>
                {label}
              </span>
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

// ── Continue CTA card ─────────────────────────────────────────────────────────

function CtaCard({
  cta,
  level,
  levels,
  language,
  onSelectLevel,
}: {
  cta: CtaAction;
  level: LevelSummary;
  levels: LevelSummary[];
  language: string;
  onSelectLevel: (cefr: string) => void;
}) {
  if (cta.kind === "noContent") return null;

  if (cta.kind === "levelComplete") {
    const nextCefr = resolveNextLevel(levels, level.cefrLevel);
    return (
      <div className="rounded-2xl border border-green-200 bg-gradient-to-br from-green-50 to-emerald-50 p-5 flex items-center justify-between gap-4">
        <div>
          <p className="text-xs font-semibold text-green-700 uppercase tracking-wide mb-1">Level complete</p>
          <p className="text-lg font-bold text-gray-900">You&apos;ve finished {level.cefrLevel}!</p>
          {nextCefr && (
            <p className="text-sm text-gray-500 mt-0.5">
              Ready to start {nextCefr} — {CEFR_DISPLAY[nextCefr]}?
            </p>
          )}
        </div>
        {nextCefr ? (
          <button
            onClick={() => onSelectLevel(nextCefr)}
            className="shrink-0 inline-flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white font-semibold text-sm px-5 py-2.5 rounded-xl transition-colors"
          >
            Begin {nextCefr} <ArrowRight size={14} aria-hidden="true" />
          </button>
        ) : (
          <span className="shrink-0 text-sm text-emerald-700 font-semibold">Curriculum complete!</span>
        )}
      </div>
    );
  }

  const lesson = cta.lesson;
  const isContinue = cta.kind === "continue";
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;
  const allLessons = level.units.flatMap((u) => u.lessons);
  const totalLessons = allLessons.length;
  const completedLessons = allLessons.filter((l) => l.status === "COMPLETED").length;
  const pct = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;

  // Find the unit this lesson belongs to
  const unitForLesson = level.units.find((u) => u.lessons.some((l) => l.lessonId === lesson.lessonId));

  return (
    <div className="rounded-2xl border border-gray-200 bg-white shadow-sm p-5">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-2">
        {isContinue ? "Continue Learning" : "Start Learning"}
      </p>
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <p className="text-lg font-bold text-gray-900 leading-snug">{lesson.title}</p>
          <p className="text-xs text-gray-500 mt-0.5">
            {unitForLesson?.displayName ? `${unitForLesson.displayName} · ` : ""}
            Lesson {lesson.position}
          </p>
          {/* Progress bar */}
          <div className="mt-3 space-y-1">
            <div className="flex items-center justify-between">
              <span className="text-xs text-gray-500">Your progress</span>
              <span className="text-xs font-semibold text-gray-700">{completedLessons} of {totalLessons} lessons</span>
            </div>
            <div
              role="progressbar"
              aria-valuemin={0}
              aria-valuemax={totalLessons}
              aria-valuenow={completedLessons}
              className="h-2 bg-gray-100 rounded-full overflow-hidden"
            >
              <div
                className="h-full bg-brand-600 rounded-full transition-[width] duration-500"
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>
        </div>
        {/* Mascot / decorative area */}
        <div className="shrink-0 flex flex-col items-center justify-center gap-3">
          <div className="w-14 h-14 rounded-full bg-brand-50 border-2 border-brand-100 flex items-center justify-center text-2xl" aria-hidden="true">
            🎯
          </div>
        </div>
      </div>
      <Link
        href={href}
        className="mt-4 inline-flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm px-5 py-2.5 rounded-xl transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        Continue <ArrowRight size={14} aria-hidden="true" />
      </Link>
    </div>
  );
}

// ── Lesson row ────────────────────────────────────────────────────────────────

function LessonStatusIcon({ status }: { status: LessonSummary["status"] }) {
  if (status === "COMPLETED") {
    return <span className="shrink-0 text-green-500" aria-hidden="true"><CheckCircle2 size={18} /></span>;
  }
  if (status === "IN_PROGRESS") {
    return (
      <span className="shrink-0 w-[18px] h-[18px] rounded-full border-2 border-brand-600 bg-brand-600/10 flex items-center justify-center" aria-hidden="true">
        <span className="w-2 h-2 rounded-full bg-brand-600" />
      </span>
    );
  }
  return <span className="shrink-0 text-gray-300" aria-hidden="true"><Circle size={18} /></span>;
}

const TAG_COLORS: Record<string, string> = {
  Vocabulary: "bg-blue-50 text-blue-700",
  Grammar: "bg-violet-50 text-violet-700",
  Practice: "bg-amber-50 text-amber-700",
  Lesson: "bg-gray-100 text-gray-600",
};

function LessonRow({
  lesson,
  language,
  tag,
}: {
  lesson: LessonSummary;
  language: string;
  tag: string;
}) {
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;
  const isCurrent = lesson.status === "IN_PROGRESS";
  const isCompleted = lesson.status === "COMPLETED";
  const isLocked = lesson.status === "NOT_STARTED";
  const scoreDisplay = isCompleted && lesson.score !== null ? `${Math.round(lesson.score)}%` : null;

  return (
    <Link
      href={href}
      className={cn(
        "group flex items-center gap-3 px-3 py-3 rounded-xl transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500",
        isCurrent ? "bg-brand-50 hover:bg-brand-50/80" : "hover:bg-gray-50"
      )}
      aria-label={`${lesson.title}${isCompleted ? `, completed${scoreDisplay ? `, ${scoreDisplay}` : ""}` : isCurrent ? ", in progress" : ""}`}
    >
      <LessonStatusIcon status={lesson.status} />

      <span className="shrink-0 text-xs text-gray-400 font-normal w-5 text-center">
        {lesson.position}
      </span>

      <span className={cn(
        "flex-1 min-w-0 text-sm font-medium truncate",
        isCompleted ? "text-gray-500" : isCurrent ? "text-brand-700" : "text-gray-700 group-hover:text-gray-900"
      )}>
        {lesson.title}
      </span>

      {/* Topic type tag */}
      <span className={cn("shrink-0 hidden sm:inline-flex text-[10px] font-semibold px-2 py-0.5 rounded-full", TAG_COLORS[tag] ?? TAG_COLORS.Lesson)}>
        {tag}
      </span>

      {isCurrent && (
        <span className="shrink-0 text-xs font-semibold text-brand-600 bg-brand-100 px-2 py-0.5 rounded-full">
          Current
        </span>
      )}

      {scoreDisplay && (
        <span className={cn(
          "shrink-0 text-xs font-semibold px-2 py-0.5 rounded-full",
          lesson.score !== null && lesson.score >= 80 ? "text-green-700 bg-green-50" : "text-gray-500 bg-gray-100"
        )}>
          {scoreDisplay}
        </span>
      )}

      {isLocked && (
        <Lock size={12} className="shrink-0 text-gray-400" aria-label="Locked" />
      )}
    </Link>
  );
}

// ── Unit accordion ────────────────────────────────────────────────────────────

function UnitAccordion({
  unit,
  unitIndex,
  language,
  defaultOpen,
}: {
  unit: Unit;
  unitIndex: number;
  language: string;
  defaultOpen: boolean;
}) {
  const [open, setOpen] = useState(defaultOpen);
  if (unit.lessons.length === 0) return null;

  const completed = unit.lessons.filter((l) => l.status === "COMPLETED").length;
  const total = unit.lessons.length;
  const pct = total > 0 ? Math.round((completed / total) * 100) : 0;
  const allDone = completed === total && total > 0;
  const hasActive = unit.lessons.some((l) => l.status === "IN_PROGRESS");
  const unitNumber = unit.ordinal > 0 ? unit.ordinal : unitIndex + 1;
  const isUnitLocked = unit.lessons.every((l) => l.status === "NOT_STARTED") && unitNumber > 1;

  const numberColors = [
    "bg-blue-500", "bg-purple-500", "bg-amber-500",
    "bg-green-500", "bg-rose-500", "bg-teal-500",
  ];
  const numColor = numberColors[(unitNumber - 1) % numberColors.length];

  return (
    <div className={cn(
      "rounded-2xl border bg-white overflow-hidden transition-shadow",
      hasActive ? "border-brand-200 shadow-sm" : "border-gray-200"
    )}>
      {/* Unit header */}
      <button
        onClick={() => setOpen((o) => !o)}
        className="w-full flex items-center gap-3 px-4 py-4 text-left hover:bg-gray-50 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-inset"
        aria-expanded={open}
      >
        {/* Number circle */}
        <span className={cn(
          "shrink-0 w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white",
          isUnitLocked ? "bg-gray-300" : numColor
        )}>
          {unitNumber}
        </span>

        {/* Ordinal + name */}
        <span className="text-xs text-gray-400 font-medium shrink-0">{unitNumber}</span>

        <div className="flex-1 min-w-0">
          {unit.displayName && (
            <p className="text-sm font-semibold text-gray-900 truncate">{unit.displayName}</p>
          )}
          <div className="flex items-center gap-2 mt-0.5">
            <div className="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden max-w-24">
              <div className="h-full bg-brand-500 rounded-full transition-[width] duration-500" style={{ width: `${pct}%` }} />
            </div>
            <span className="text-xs text-gray-400 shrink-0">
              {allDone ? "✓ Done" : `${completed} of ${total}`}
            </span>
          </div>
        </div>

        {isUnitLocked && <Lock size={14} className="shrink-0 text-gray-400" aria-hidden="true" />}
        {open
          ? <ChevronUp size={16} className="shrink-0 text-gray-400" aria-hidden="true" />
          : <ChevronDown size={16} className="shrink-0 text-gray-400" aria-hidden="true" />}
      </button>

      {/* Lesson list */}
      {open && (
        <div className="px-2 pb-2 border-t border-gray-100">
          {unit.displayName && (
            <p className="text-xs text-gray-500 px-3 pt-2 pb-1">{unit.displayName}</p>
          )}
          <div className="space-y-0.5 mt-1">
            {unit.lessons.map((lesson) => (
              <LessonRow
                key={lesson.lessonId}
                lesson={lesson}
                language={language}
                tag={lessonTypeTag(lesson, unit.lessons)}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

// ── Right sidebar ─────────────────────────────────────────────────────────────

function ProgressDonut({ pct, completed, total }: { pct: number; completed: number; total: number }) {
  const r = 38;
  const circ = 2 * Math.PI * r;
  const dash = (pct / 100) * circ;

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5">
      <h3 className="text-sm font-semibold text-gray-900 mb-4">
        {/* Level name comes from parent — just "Progress" here */}
        Progress
      </h3>
      <div className="flex items-center gap-4">
        <div className="relative w-24 h-24 shrink-0">
          <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
            <circle cx="50" cy="50" r={r} fill="none" stroke="#f3f4f6" strokeWidth="10" />
            <circle
              cx="50" cy="50" r={r} fill="none"
              stroke="#4f46e5"
              strokeWidth="10"
              strokeDasharray={`${dash} ${circ}`}
              strokeLinecap="round"
              className="transition-all duration-700"
            />
          </svg>
          <span className="absolute inset-0 flex items-center justify-center text-xl font-bold text-gray-900">
            {pct}%
          </span>
        </div>
        <div>
          <p className="text-sm text-gray-500 leading-relaxed">
            <span className="font-bold text-gray-900">{completed}</span> of{" "}
            <span className="font-bold text-gray-900">{total}</span> lessons completed
          </p>
          <Link href="#" className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-brand-600 hover:underline">
            View details <ArrowRight size={11} />
          </Link>
        </div>
      </div>
    </div>
  );
}

function LearningGoals({ level }: { level: LevelSummary }) {
  const goals = CEFR_GOALS[level.cefrLevel] ?? [];
  if (goals.length === 0) return null;

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5">
      <div className="flex items-center gap-2 mb-3">
        <Target size={15} className="text-brand-600" aria-hidden="true" />
        <h3 className="text-sm font-semibold text-gray-900">
          {level.cefrLevel} Learning Goals
        </h3>
      </div>
      <p className="text-xs text-gray-500 mb-3">After {level.cefrLevel} you&apos;ll be able to:</p>
      <ul className="space-y-2">
        {goals.map((g, i) => (
          <li key={i} className="flex items-start gap-2 text-xs text-gray-700">
            <CheckCircle2 size={13} className="text-green-500 shrink-0 mt-0.5" aria-hidden="true" />
            {g}
          </li>
        ))}
      </ul>
    </div>
  );
}

function MotivationalCard({ language }: { language: string }) {
  return (
    <div className="rounded-2xl bg-gradient-to-br from-brand-50 to-indigo-50 border border-brand-100 p-5 text-center">
      <div className="text-3xl mb-2" aria-hidden="true">🏔️</div>
      <p className="text-sm font-bold text-gray-800">Small steps. Big progress.</p>
      <p className="text-xs text-gray-500 mt-1">
        You&apos;re building real skills that open new opportunities.
      </p>
    </div>
  );
}

// ── Locked overlay ────────────────────────────────────────────────────────────

function LockedLevelOverlay({ cefr, prevCefr, onDismiss }: { cefr: string; prevCefr: string; onDismiss: () => void }) {
  return (
    <div role="alert" aria-live="polite" className="rounded-2xl border border-gray-200 bg-gray-50 px-6 py-8 text-center space-y-3">
      <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center mx-auto">
        <Lock size={22} className="text-gray-500" aria-hidden="true" />
      </div>
      <div>
        <p className="font-bold text-gray-800 text-base">{cefr} — {CEFR_DISPLAY[cefr]} is locked</p>
        <p className="text-sm text-gray-500 mt-1">Complete {prevCefr} to unlock this level.</p>
      </div>
      <button onClick={onDismiss} className="inline-flex items-center gap-1 text-sm font-semibold text-brand-600 hover:underline">
        ← Back to {prevCefr}
      </button>
    </div>
  );
}

// ── Error / empty ─────────────────────────────────────────────────────────────

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center space-y-4">
      <p className="text-3xl" aria-hidden="true">⚠️</p>
      <p className="font-semibold text-gray-700">Something went wrong loading your course.</p>
      <p className="text-sm text-gray-400">Check your connection and try again.</p>
      <button onClick={onRetry} className="inline-flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm px-5 py-2.5 rounded-xl transition-colors">
        <RefreshCw size={14} aria-hidden="true" /> Try again
      </button>
    </div>
  );
}

function EmptyState({ language }: { language: string }) {
  const name = language.charAt(0).toUpperCase() + language.slice(1);
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center space-y-2">
      <p className="font-semibold text-gray-700">Your {name} course isn&apos;t available yet.</p>
      <p className="text-sm text-gray-400">Check back soon — content is being prepared.</p>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function LanguageCurriculumPage() {
  const params = useParams<{ language: string }>();
  const searchParams = useSearchParams();
  const router = useRouter();
  const language = params.language;

  const [curriculum, setCurriculum] = useState<CurriculumResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [lockedMessage, setLockedMessage] = useState<string | null>(null);

  const urlLevel = searchParams.get("level");
  const [activeCefr, setActiveCefr] = useState<string | null>(urlLevel);

  const load = useCallback(() => {
    setLoading(true);
    setError(false);
    academyApi
      .getCurriculum(language)
      .then((res) => {
        setCurriculum(res.data);
        const resolved = urlLevel ?? resolveActiveLevel(res.data.levels);
        setActiveCefr(resolved);
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [language, urlLevel]);

  useEffect(() => { load(); }, [load]);

  function handleSelectLevel(cefr: string) {
    if (!curriculum) return;
    const level = curriculum.levels.find((l) => l.cefrLevel === cefr);
    if (!level) return;

    if (level.status === "NOT_STARTED" && level.ordinal > 1) {
      setLockedMessage(cefr);
      return;
    }

    setLockedMessage(null);
    setActiveCefr(cefr);

    const url = new URL(window.location.href);
    url.searchParams.set("level", cefr);
    router.replace(url.pathname + url.search, { scroll: false });
  }

  const displayName = curriculum?.displayName
    || (language.charAt(0).toUpperCase() + language.slice(1));

  if (loading) return <PageSkeleton />;

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8">
          <ErrorState onRetry={load} />
        </div>
      </div>
    );
  }

  if (!curriculum || curriculum.levels.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8">
          <EmptyState language={language} />
        </div>
      </div>
    );
  }

  const currentLevel =
    curriculum.levels.find((l) => l.cefrLevel === activeCefr) ?? curriculum.levels[0];
  const isLocked = currentLevel.status === "NOT_STARTED" && currentLevel.ordinal > 1;
  const cta = resolveCtaAction(currentLevel);
  const allLessons = currentLevel.units.flatMap((u) => u.lessons);
  const completedLessons = allLessons.filter((l) => l.status === "COMPLETED").length;
  const totalLessons = allLessons.length;
  const pct = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;

  const lockedLevel = lockedMessage ? curriculum.levels.find((l) => l.cefrLevel === lockedMessage) : null;
  const lockedPrevLevel = lockedLevel ? curriculum.levels.find((l) => l.ordinal === lockedLevel.ordinal - 1) : null;

  // Which units to open by default: the first unit with an active or not-started lesson
  const activeUnitIndex = currentLevel.units.findIndex(
    (u) => u.lessons.some((l) => l.status === "IN_PROGRESS" || l.status === "NOT_STARTED")
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero banner */}
      <LanguageHero language={language} displayName={displayName} level={currentLevel} />

      <div className="max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex gap-8 items-start">

          {/* ── Main column ─────────────────────────────────────────────────── */}
          <div className="flex-1 min-w-0 space-y-6">

            {/* Level rail */}
            <LevelRail
              levels={curriculum.levels}
              activeCefr={currentLevel.cefrLevel}
              onSelect={handleSelectLevel}
            />

            {/* Locked notice */}
            {lockedMessage && lockedPrevLevel && (
              <LockedLevelOverlay
                cefr={lockedMessage}
                prevCefr={lockedPrevLevel.cefrLevel}
                onDismiss={() => {
                  setLockedMessage(null);
                  handleSelectLevel(lockedPrevLevel.cefrLevel);
                }}
              />
            )}

            {!lockedMessage && (
              <>
                {/* CTA card */}
                {!isLocked && (
                  <CtaCard
                    cta={cta}
                    level={currentLevel}
                    levels={curriculum.levels}
                    language={language}
                    onSelectLevel={handleSelectLevel}
                  />
                )}

                {/* Journey header */}
                {!isLocked && currentLevel.units.length > 0 && (
                  <>
                    <div>
                      <h2 className="text-xl font-bold text-gray-900">
                        Your {currentLevel.cefrLevel} Journey
                      </h2>
                      <p className="text-sm text-gray-500 mt-0.5">
                        {currentLevel.units.length} unit{currentLevel.units.length !== 1 ? "s" : ""} · {totalLessons} lessons
                      </p>
                    </div>

                    {/* Unit accordions */}
                    <div className="space-y-3 pb-8">
                      {currentLevel.units.map((unit, i) => (
                        <UnitAccordion
                          key={unit.unitId ?? `unit-${i}`}
                          unit={unit}
                          unitIndex={i}
                          language={language}
                          defaultOpen={activeUnitIndex === -1 ? i === 0 : i === activeUnitIndex}
                        />
                      ))}
                    </div>
                  </>
                )}
              </>
            )}
          </div>

          {/* ── Right sidebar (xl+) ─────────────────────────────────────────── */}
          <aside className="hidden xl:flex flex-col gap-4 w-72 shrink-0 sticky top-6">
            <ProgressDonut
              pct={pct}
              completed={completedLessons}
              total={totalLessons}
            />
            <LearningGoals level={currentLevel} />
            <MotivationalCard language={language} />
          </aside>

        </div>
      </div>
    </div>
  );
}
