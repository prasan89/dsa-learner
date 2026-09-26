"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams, useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import { CheckCircle2, Circle, Lock, ArrowRight, ChevronRight, RefreshCw } from "lucide-react";
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
} from "@/lib/api/academy";
import { cn } from "@/lib/utils";

// ── Skeleton ──────────────────────────────────────────────────────────────────

function SkeletonPulse({ className }: { className?: string }) {
  return <div className={cn("animate-pulse bg-gray-100 rounded-lg", className)} />;
}

function CurriculumSkeleton() {
  return (
    <div className="max-w-2xl mx-auto px-6 py-8 space-y-8">
      {/* Header */}
      <div className="space-y-2">
        <SkeletonPulse className="h-8 w-48" />
        <SkeletonPulse className="h-4 w-32" />
      </div>
      {/* Level tabs */}
      <div className="flex gap-2">
        {[...Array(6)].map((_, i) => (
          <SkeletonPulse key={i} className="h-9 w-14 rounded-full" />
        ))}
      </div>
      {/* Progress */}
      <div className="space-y-2">
        <SkeletonPulse className="h-4 w-40" />
        <SkeletonPulse className="h-2.5 w-full rounded-full" />
        <SkeletonPulse className="h-3 w-24" />
      </div>
      {/* CTA */}
      <SkeletonPulse className="h-20 w-full rounded-xl" />
      {/* Units */}
      {[...Array(2)].map((_, u) => (
        <div key={u} className="space-y-2">
          <SkeletonPulse className="h-4 w-20" />
          {[...Array(4)].map((_, l) => (
            <SkeletonPulse key={l} className="h-11 w-full" />
          ))}
        </div>
      ))}
    </div>
  );
}

// ── Level selector ────────────────────────────────────────────────────────────

function LevelSelector({
  levels,
  activeCefr,
  onSelect,
}: {
  levels: LevelSummary[];
  activeCefr: string;
  onSelect: (cefr: string) => void;
}) {
  return (
    <nav aria-label="CEFR levels" className="flex gap-1.5 flex-wrap">
      {levels.map((level) => {
        const isActive = level.cefrLevel === activeCefr;
        const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
        const isCompleted = level.status === "COMPLETED";

        const label = isLocked
          ? `${level.cefrLevel} — locked`
          : isCompleted
          ? `${level.cefrLevel} — completed`
          : level.cefrLevel;

        return (
          <button
            key={level.cefrLevel}
            aria-label={label}
            aria-pressed={isActive}
            aria-disabled={isLocked}
            onClick={() => {
              if (isLocked) return;
              onSelect(level.cefrLevel);
            }}
            className={cn(
              "relative inline-flex items-center gap-1 px-3.5 py-1.5 rounded-full text-sm font-semibold transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-1",
              isActive && "bg-brand-600 text-white shadow-sm",
              !isActive && !isLocked && "bg-white border border-gray-200 text-gray-700 hover:border-brand-400 hover:text-brand-600",
              !isActive && isCompleted && !isActive && "bg-green-50 border border-green-200 text-green-700 hover:border-green-400",
              isLocked && "bg-gray-50 border border-gray-200 text-gray-400 cursor-not-allowed opacity-70"
            )}
          >
            {level.cefrLevel}
            {isLocked && <Lock size={11} aria-hidden="true" />}
            {isCompleted && !isActive && (
              <span className="w-1.5 h-1.5 rounded-full bg-green-500" aria-hidden="true" />
            )}
          </button>
        );
      })}
    </nav>
  );
}

// ── Progress bar ──────────────────────────────────────────────────────────────

function LevelProgress({ level }: { level: LevelSummary }) {
  const total = level.lessonsTotal;
  const completed = level.lessonsCompleted;
  const pct = total > 0 ? Math.min(100, Math.round((completed / total) * 100)) : 0;
  const displayName = CEFR_DISPLAY[level.cefrLevel] ?? "";

  return (
    <div className="space-y-2">
      <div className="flex items-baseline gap-2">
        <span className="text-lg font-bold text-gray-900">{level.cefrLevel}</span>
        {displayName && (
          <span className="text-sm text-gray-500">· {displayName}</span>
        )}
      </div>
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={total}
        aria-valuenow={completed}
        aria-label={`${completed} of ${total} lessons completed`}
        className="h-2 bg-gray-100 rounded-full overflow-hidden"
      >
        <div
          className="h-full bg-brand-600 rounded-full transition-[width] duration-500"
          style={{ width: `${pct}%` }}
        />
      </div>
      <p className="text-sm text-gray-500">
        <span className="font-medium text-gray-700">{completed}</span> of{" "}
        <span className="font-medium text-gray-700">{total}</span> lesson
        {total !== 1 ? "s" : ""} completed
        {pct > 0 && (
          <span className="ml-2 text-gray-400">· {pct}%</span>
        )}
      </p>
    </div>
  );
}

// ── Continue CTA ──────────────────────────────────────────────────────────────

function ContinueCta({
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
      <div className="rounded-xl border border-green-200 bg-green-50 px-5 py-4">
        <p className="text-xs font-semibold text-green-700 uppercase tracking-wide mb-1">
          Level complete
        </p>
        <p className="font-semibold text-gray-900 mb-3">
          You&apos;ve completed all lessons in {level.cefrLevel}.
        </p>
        {nextCefr ? (
          <button
            onClick={() => onSelectLevel(nextCefr)}
            className="inline-flex items-center gap-2 btn-primary text-sm"
          >
            Explore {nextCefr} <ArrowRight size={14} aria-hidden="true" />
          </button>
        ) : (
          <p className="text-sm text-gray-600 font-medium">You&apos;ve completed the entire curriculum!</p>
        )}
      </div>
    );
  }

  const lesson = cta.lesson;
  const isContinue = cta.kind === "continue";
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;

  return (
    <div className="rounded-xl border border-brand-200 bg-brand-50 px-5 py-4">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-1">
        {isContinue ? "Continue learning" : "Start learning"}
      </p>
      <p className="font-semibold text-gray-900 leading-snug">{lesson.title}</p>
      <p className="text-sm text-gray-500 mt-0.5 mb-3">
        Lesson {lesson.position}
      </p>
      <Link
        href={href}
        className="inline-flex items-center gap-2 btn-primary text-sm"
      >
        {isContinue ? "Continue" : "Start lesson"}{" "}
        <ArrowRight size={14} aria-hidden="true" />
      </Link>
    </div>
  );
}

// ── Lesson row ────────────────────────────────────────────────────────────────

function LessonStatusIcon({ status }: { status: LessonSummary["status"] }) {
  if (status === "COMPLETED") {
    return (
      <span aria-hidden="true" className="shrink-0 text-green-500">
        <CheckCircle2 size={17} />
      </span>
    );
  }
  if (status === "IN_PROGRESS") {
    return (
      <span aria-hidden="true" className="shrink-0 w-[17px] h-[17px] rounded-full border-2 border-brand-600 bg-brand-600/10 flex items-center justify-center">
        <span className="w-2 h-2 rounded-full bg-brand-600" />
      </span>
    );
  }
  return (
    <span aria-hidden="true" className="shrink-0 text-gray-300">
      <Circle size={17} />
    </span>
  );
}

function LessonRow({ lesson, language }: { lesson: LessonSummary; language: string }) {
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;
  const isCurrent = lesson.status === "IN_PROGRESS";
  const isCompleted = lesson.status === "COMPLETED";

  return (
    <Link
      href={href}
      className={cn(
        "group flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500",
        "hover:bg-gray-50",
        isCurrent && "bg-brand-50 hover:bg-brand-50/80"
      )}
      aria-label={`${lesson.title}, lesson ${lesson.position}${isCompleted ? ", completed" : isCurrent ? ", in progress" : ""}`}
    >
      <LessonStatusIcon status={lesson.status} />
      <span
        className={cn(
          "flex-1 min-w-0 text-sm font-medium truncate",
          isCompleted ? "text-gray-500" : isCurrent ? "text-brand-700" : "text-gray-700 group-hover:text-gray-900"
        )}
      >
        <span className="text-gray-400 mr-1.5 text-xs font-normal">{lesson.position}</span>
        {lesson.title}
      </span>
      {isCurrent && (
        <span className="shrink-0 text-xs font-semibold text-brand-600 bg-brand-100 px-2 py-0.5 rounded-full">
          Current
        </span>
      )}
      <ChevronRight
        size={14}
        aria-hidden="true"
        className="shrink-0 text-gray-300 group-hover:text-gray-400 transition-colors"
      />
    </Link>
  );
}

// ── Unit section ──────────────────────────────────────────────────────────────

function UnitSection({
  unit,
  index,
  language,
}: {
  unit: { unitId: string | null; displayName: string | null; ordinal: number; lessons: LessonSummary[] };
  index: number;
  language: string;
}) {
  if (unit.lessons.length === 0) return null;

  const completedCount = unit.lessons.filter((l) => l.status === "COMPLETED").length;
  const allDone = completedCount === unit.lessons.length;

  return (
    <section aria-label={unit.displayName ?? `Unit ${index + 1}`}>
      <div className="flex items-baseline gap-2 mb-1.5">
        <h3 className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">
          Unit {unit.ordinal > 0 ? unit.ordinal : index + 1}
        </h3>
        {unit.displayName && (
          <span className="text-sm font-medium text-gray-600">{unit.displayName}</span>
        )}
        {allDone && unit.lessons.length > 0 && (
          <span className="ml-auto text-[10px] font-semibold text-green-600 bg-green-50 px-1.5 py-0.5 rounded-full">
            Done
          </span>
        )}
        {!allDone && completedCount > 0 && (
          <span className="ml-auto text-[10px] text-gray-400">
            {completedCount}/{unit.lessons.length}
          </span>
        )}
      </div>
      <div className="space-y-0.5">
        {unit.lessons.map((lesson) => (
          <LessonRow key={lesson.lessonId} lesson={lesson} language={language} />
        ))}
      </div>
    </section>
  );
}

// ── Locked level overlay ──────────────────────────────────────────────────────

function LockedLevelMessage({
  cefr,
  prevCefr,
  onDismiss,
}: {
  cefr: string;
  prevCefr: string;
  onDismiss: () => void;
}) {
  return (
    <div
      role="alert"
      aria-live="polite"
      className="rounded-xl border border-gray-200 bg-gray-50 px-5 py-5 text-center space-y-2"
    >
      <Lock size={24} className="text-gray-400 mx-auto" aria-hidden="true" />
      <p className="font-semibold text-gray-700">
        {cefr} is locked
      </p>
      <p className="text-sm text-gray-500">
        Complete {prevCefr} to unlock {cefr}.
      </p>
      <button onClick={onDismiss} className="btn-secondary text-sm mt-1">
        Go back to {prevCefr}
      </button>
    </div>
  );
}

// ── Error state ───────────────────────────────────────────────────────────────

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center space-y-4">
      <p className="text-3xl" aria-hidden="true">⚠️</p>
      <p className="font-semibold text-gray-700">Something went wrong loading your course.</p>
      <p className="text-sm text-gray-400">Check your connection and try again.</p>
      <button
        onClick={onRetry}
        className="btn-primary flex items-center gap-2"
      >
        <RefreshCw size={14} aria-hidden="true" /> Try again
      </button>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────

function EmptyState({ language }: { language: string }) {
  const displayName =
    language.charAt(0).toUpperCase() + language.slice(1);
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center space-y-2">
      <p className="font-semibold text-gray-700">Your {displayName} course isn&apos;t available yet.</p>
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

  // Derive active level from URL or API default
  const urlLevel = searchParams.get("level");
  const [activeCefr, setActiveCefr] = useState<string | null>(urlLevel);

  const load = useCallback(() => {
    setLoading(true);
    setError(false);
    academyApi
      .getCurriculum(language)
      .then((res) => {
        setCurriculum(res.data);
        // Set active level: URL param → API default (IN_PROGRESS or first available)
        const resolved = urlLevel ?? resolveActiveLevel(res.data.levels);
        setActiveCefr(resolved);
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [language, urlLevel]);

  useEffect(() => {
    load();
  }, [load]);

  function handleSelectLevel(cefr: string) {
    if (!curriculum) return;
    const level = curriculum.levels.find((l) => l.cefrLevel === cefr);
    if (!level) return;

    // Locked = NOT_STARTED and ordinal > 1
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

  const displayName =
    language.charAt(0).toUpperCase() + language.slice(1);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white border-b border-gray-200 px-6 py-4">
          <div className="max-w-2xl mx-auto">
            <SkeletonPulse className="h-7 w-36" />
          </div>
        </header>
        <CurriculumSkeleton />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <PageHeader displayName={displayName} />
        <div className="max-w-2xl mx-auto px-6">
          <ErrorState onRetry={load} />
        </div>
      </div>
    );
  }

  if (!curriculum || curriculum.levels.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <PageHeader displayName={displayName} />
        <div className="max-w-2xl mx-auto px-6">
          <EmptyState language={language} />
        </div>
      </div>
    );
  }

  const currentLevel = curriculum.levels.find((l) => l.cefrLevel === activeCefr)
    ?? curriculum.levels[0];

  const isLocked = currentLevel.status === "NOT_STARTED" && currentLevel.ordinal > 1;

  const cta = resolveCtaAction(currentLevel);

  // Resolve locked message's previous level label
  const lockedLevel = lockedMessage
    ? curriculum.levels.find((l) => l.cefrLevel === lockedMessage)
    : null;
  const lockedPrevLevel = lockedLevel
    ? curriculum.levels.find((l) => l.ordinal === lockedLevel.ordinal - 1)
    : null;

  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader displayName={curriculum.displayName || displayName} />

      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-6 space-y-6">

        {/* Level selector */}
        <LevelSelector
          levels={curriculum.levels}
          activeCefr={currentLevel.cefrLevel}
          onSelect={handleSelectLevel}
        />

        {/* Locked notice — replaces content for locked selection */}
        {lockedMessage && lockedPrevLevel && (
          <LockedLevelMessage
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
            {/* Progress */}
            <LevelProgress level={currentLevel} />

            {/* Continue CTA */}
            {!isLocked && (
              <ContinueCta
                cta={cta}
                level={currentLevel}
                levels={curriculum.levels}
                language={language}
                onSelectLevel={handleSelectLevel}
              />
            )}

            {/* Lessons by unit */}
            {!isLocked && currentLevel.units.length > 0 && (
              <div className="space-y-6 pb-8">
                {currentLevel.units.map((unit, i) => (
                  <UnitSection
                    key={unit.unitId ?? `unassigned-${i}`}
                    unit={unit}
                    index={i}
                    language={language}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

// ── Shared header ─────────────────────────────────────────────────────────────

function PageHeader({ displayName }: { displayName: string }) {
  return (
    <header className="bg-white border-b border-gray-200 px-4 sm:px-6 py-4 sticky top-0 z-30">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-xl font-bold text-gray-900">{displayName}</h1>
      </div>
    </header>
  );
}
