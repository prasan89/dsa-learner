"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams, useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import { CheckCircle2, Circle, Lock, ArrowRight, RefreshCw, ChevronRight } from "lucide-react";
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

// ── Static level descriptions ─────────────────────────────────────────────────

const CEFR_DESCRIPTION: Record<string, string> = {
  A1: "Start from zero. Learn greetings, numbers, and the most essential phrases.",
  A2: "Build on the basics. Master common phrases and essential daily vocabulary.",
  B1: "Express yourself. Handle most situations while traveling or conversing.",
  B2: "Discuss complex topics. Understand the main ideas of abstract subjects.",
  C1: "Near fluency. Communicate flexibly and effectively in social and professional settings.",
  C2: "Full mastery. Understand virtually everything heard and read with ease.",
};

// ── Skeleton ──────────────────────────────────────────────────────────────────

function SkeletonPulse({ className }: { className?: string }) {
  return <div className={cn("animate-pulse bg-gray-100 rounded-lg", className)} />;
}

function CurriculumSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-8">
      {/* Journey rail */}
      <div className="flex items-center gap-3">
        {[...Array(6)].map((_, i) => (
          <SkeletonPulse key={i} className="h-10 w-10 rounded-full" />
        ))}
      </div>
      {/* Level hero */}
      <div className="space-y-3">
        <SkeletonPulse className="h-7 w-32" />
        <SkeletonPulse className="h-4 w-72" />
        <SkeletonPulse className="h-2.5 w-full rounded-full" />
        <SkeletonPulse className="h-3 w-28" />
      </div>
      {/* CTA */}
      <SkeletonPulse className="h-28 w-full rounded-2xl" />
      {/* Units */}
      {[...Array(2)].map((_, u) => (
        <div key={u} className="space-y-2">
          <SkeletonPulse className="h-5 w-40" />
          {[...Array(4)].map((_, l) => (
            <SkeletonPulse key={l} className="h-12 w-full rounded-lg" />
          ))}
        </div>
      ))}
    </div>
  );
}

// ── Level journey rail ────────────────────────────────────────────────────────

function LevelJourneyRail({
  levels,
  activeCefr,
  onSelect,
}: {
  levels: LevelSummary[];
  activeCefr: string;
  onSelect: (cefr: string) => void;
}) {
  return (
    <nav aria-label="CEFR level journey" className="relative">
      {/* Connection line */}
      <div
        className="absolute top-5 left-5 right-5 h-0.5 bg-gray-200"
        aria-hidden="true"
      />
      <ol className="relative flex items-start gap-0 justify-between">
        {levels.map((level, idx) => {
          const isActive = level.cefrLevel === activeCefr;
          const isCompleted = level.status === "COMPLETED";
          const isLocked = level.status === "NOT_STARTED" && level.ordinal > 1;
          const isInProgress = level.status === "IN_PROGRESS";

          return (
            <li key={level.cefrLevel} className="flex flex-col items-center gap-1.5 flex-1">
              <button
                aria-label={`${level.cefrLevel} — ${
                  isCompleted ? "completed" : isLocked ? "locked" : isActive ? "current" : "available"
                }`}
                aria-current={isActive ? "step" : undefined}
                aria-disabled={isLocked}
                onClick={() => {
                  if (!isLocked) onSelect(level.cefrLevel);
                }}
                className={cn(
                  "relative z-10 w-10 h-10 rounded-full flex items-center justify-center transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2",
                  isActive && "bg-brand-600 text-white shadow-md ring-4 ring-brand-100",
                  isCompleted && !isActive && "bg-green-500 text-white hover:bg-green-600",
                  isInProgress && !isActive && "bg-brand-100 text-brand-700 border-2 border-brand-400 hover:border-brand-600",
                  !isActive && !isCompleted && !isInProgress && !isLocked && "bg-white border-2 border-gray-300 text-gray-600 hover:border-brand-400 hover:text-brand-600",
                  isLocked && "bg-gray-100 border-2 border-gray-200 text-gray-400 cursor-not-allowed"
                )}
              >
                {isCompleted && !isActive ? (
                  <CheckCircle2 size={18} aria-hidden="true" />
                ) : isLocked ? (
                  <Lock size={14} aria-hidden="true" />
                ) : (
                  <span className="text-xs font-bold">{level.cefrLevel}</span>
                )}
              </button>
              <span
                className={cn(
                  "text-[10px] font-semibold text-center leading-tight",
                  isActive ? "text-brand-700" : isCompleted ? "text-green-600" : isLocked ? "text-gray-300" : "text-gray-500"
                )}
              >
                {isCompleted && !isActive ? level.cefrLevel : CEFR_DISPLAY[level.cefrLevel] ?? level.cefrLevel}
              </span>
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

// ── Level hero ────────────────────────────────────────────────────────────────

function LevelHero({ level }: { level: LevelSummary }) {
  const total = level.lessonsTotal;
  const completed = level.lessonsCompleted;
  const pct = total > 0 ? Math.min(100, Math.round((completed / total) * 100)) : 0;
  const displayName = level.displayName || CEFR_DISPLAY[level.cefrLevel] || "";
  const description = CEFR_DESCRIPTION[level.cefrLevel] ?? "";

  return (
    <div className="space-y-3">
      <div className="flex items-baseline gap-2">
        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-bold bg-brand-100 text-brand-700">
          {level.cefrLevel}
        </span>
        <h2 className="text-xl font-bold text-gray-900">{displayName}</h2>
      </div>
      {description && (
        <p className="text-sm text-gray-500 leading-relaxed">{description}</p>
      )}
      <div className="space-y-1.5">
        <div
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={total}
          aria-valuenow={completed}
          aria-label={`${completed} of ${total} lessons completed`}
          className="h-2.5 bg-gray-100 rounded-full overflow-hidden"
        >
          <div
            className="h-full bg-brand-600 rounded-full transition-[width] duration-500"
            style={{ width: `${pct}%` }}
          />
        </div>
        <p className="text-xs text-gray-500">
          <span className="font-semibold text-gray-700">{completed}</span> of{" "}
          <span className="font-semibold text-gray-700">{total}</span> lesson{total !== 1 ? "s" : ""} completed
          {pct > 0 && <span className="ml-1.5 text-gray-400">· {pct}%</span>}
        </p>
      </div>
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
      <div className="rounded-2xl border border-green-200 bg-gradient-to-br from-green-50 to-emerald-50 px-6 py-5">
        <p className="text-xs font-semibold text-green-700 uppercase tracking-wide mb-1">
          Level complete
        </p>
        <p className="text-lg font-bold text-gray-900 mb-1">
          You&apos;ve finished {level.cefrLevel}!
        </p>
        {nextCefr ? (
          <>
            <p className="text-sm text-gray-500 mb-4">
              Ready to start {nextCefr} — {CEFR_DISPLAY[nextCefr]}?
            </p>
            <button
              onClick={() => onSelectLevel(nextCefr)}
              className="inline-flex items-center gap-2 btn-primary"
            >
              Begin {nextCefr} <ArrowRight size={15} aria-hidden="true" />
            </button>
          </>
        ) : (
          <p className="text-sm text-emerald-700 font-semibold mt-2">
            You&apos;ve completed the entire curriculum!
          </p>
        )}
      </div>
    );
  }

  const lesson = cta.lesson;
  const isContinue = cta.kind === "continue";
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;
  const allLessons = level.units.flatMap((u) => u.lessons);
  const totalLessons = allLessons.length;

  return (
    <div className="rounded-2xl bg-gradient-to-br from-brand-600 to-brand-700 px-6 py-5 shadow-sm">
      <p className="text-xs font-semibold text-brand-200 uppercase tracking-wide mb-2">
        {isContinue ? "Continue learning" : "Start learning"}
      </p>
      <p className="text-lg font-bold text-white leading-snug mb-0.5">{lesson.title}</p>
      <p className="text-sm text-brand-200 mb-5">
        Lesson {lesson.position}{totalLessons > 0 ? ` of ${totalLessons}` : ""}
      </p>
      <Link
        href={href}
        className="inline-flex items-center gap-2 bg-white text-brand-700 hover:bg-brand-50 font-semibold text-sm px-5 py-2.5 rounded-xl transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-brand-600"
      >
        {isContinue ? "Continue" : "Start lesson"}{" "}
        <ArrowRight size={15} aria-hidden="true" />
      </Link>
    </div>
  );
}

// ── Lesson row ────────────────────────────────────────────────────────────────

function LessonStatusIcon({ status }: { status: LessonSummary["status"] }) {
  if (status === "COMPLETED") {
    return (
      <span aria-hidden="true" className="shrink-0 text-green-500">
        <CheckCircle2 size={18} />
      </span>
    );
  }
  if (status === "IN_PROGRESS") {
    return (
      <span aria-hidden="true" className="shrink-0 w-[18px] h-[18px] rounded-full border-2 border-brand-600 bg-brand-600/10 flex items-center justify-center">
        <span className="w-2 h-2 rounded-full bg-brand-600" />
      </span>
    );
  }
  return (
    <span aria-hidden="true" className="shrink-0 text-gray-300">
      <Circle size={18} />
    </span>
  );
}

function LessonRow({ lesson, language }: { lesson: LessonSummary; language: string }) {
  const href = `/languages/${language}/lessons/${lesson.lessonId}`;
  const isCurrent = lesson.status === "IN_PROGRESS";
  const isCompleted = lesson.status === "COMPLETED";
  const scoreDisplay =
    isCompleted && lesson.score !== null
      ? `${Math.round(lesson.score)}%`
      : null;

  return (
    <Link
      href={href}
      className={cn(
        "group flex items-center gap-3 px-3 py-3 rounded-xl transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500",
        "hover:bg-gray-50",
        isCurrent && "bg-brand-50 hover:bg-brand-50/80"
      )}
      aria-label={`${lesson.title}, lesson ${lesson.position}${
        isCompleted ? `, completed${scoreDisplay ? `, score ${scoreDisplay}` : ""}` : isCurrent ? ", in progress" : ""
      }`}
    >
      <LessonStatusIcon status={lesson.status} />
      <span className="text-xs text-gray-400 font-normal w-5 shrink-0 text-center">
        {lesson.position}
      </span>
      <span
        className={cn(
          "flex-1 min-w-0 text-sm font-medium truncate",
          isCompleted ? "text-gray-500" : isCurrent ? "text-brand-700" : "text-gray-700 group-hover:text-gray-900"
        )}
      >
        {lesson.title}
      </span>
      {isCurrent && (
        <span className="shrink-0 text-xs font-semibold text-brand-600 bg-brand-100 px-2 py-0.5 rounded-full">
          Current
        </span>
      )}
      {scoreDisplay && (
        <span className={cn(
          "shrink-0 text-xs font-semibold px-2 py-0.5 rounded-full",
          lesson.score !== null && lesson.score >= 80
            ? "text-green-700 bg-green-50"
            : "text-gray-500 bg-gray-100"
        )}>
          {scoreDisplay}
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
  const allDone = completedCount === unit.lessons.length && unit.lessons.length > 0;
  const unitNumber = unit.ordinal > 0 ? unit.ordinal : index + 1;

  return (
    <section aria-label={unit.displayName ?? `Unit ${unitNumber}`}>
      <div className="flex items-center gap-2.5 mb-2">
        <div className="flex items-baseline gap-2">
          <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
            Unit {unitNumber}
          </span>
          {unit.displayName && (
            <span className="text-sm font-semibold text-gray-700">{unit.displayName}</span>
          )}
        </div>
        <div className="flex-1 h-px bg-gray-100" aria-hidden="true" />
        {allDone ? (
          <span className="text-xs font-semibold text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
            Done
          </span>
        ) : completedCount > 0 ? (
          <span className="text-xs text-gray-400">
            {completedCount}/{unit.lessons.length}
          </span>
        ) : null}
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

function LockedLevelOverlay({
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
      className="rounded-2xl border border-gray-200 bg-gray-50 px-6 py-8 text-center space-y-3"
    >
      <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center mx-auto">
        <Lock size={22} className="text-gray-500" aria-hidden="true" />
      </div>
      <div>
        <p className="font-bold text-gray-800 text-base">
          {cefr} — {CEFR_DISPLAY[cefr]} is locked
        </p>
        <p className="text-sm text-gray-500 mt-1">
          Complete {prevCefr} to unlock this level.
        </p>
      </div>
      <button onClick={onDismiss} className="btn-secondary text-sm">
        Back to {prevCefr}
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
      <button onClick={onRetry} className="btn-primary flex items-center gap-2">
        <RefreshCw size={14} aria-hidden="true" /> Try again
      </button>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────

function EmptyState({ language }: { language: string }) {
  const displayName = language.charAt(0).toUpperCase() + language.slice(1);
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center space-y-2">
      <p className="font-semibold text-gray-700">Your {displayName} course isn&apos;t available yet.</p>
      <p className="text-sm text-gray-400">Check back soon — content is being prepared.</p>
    </div>
  );
}

// ── Page header ───────────────────────────────────────────────────────────────

function PageHeader({ displayName }: { displayName: string }) {
  return (
    <header className="bg-white border-b border-gray-200 px-4 sm:px-6 py-4 sticky top-0 z-30">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-xl font-bold text-gray-900">{displayName}</h1>
      </div>
    </header>
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

  useEffect(() => {
    load();
  }, [load]);

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

  const displayName = language.charAt(0).toUpperCase() + language.slice(1);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white border-b border-gray-200 px-4 sm:px-6 py-4 sticky top-0 z-30">
          <div className="max-w-3xl mx-auto">
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
        <div className="max-w-3xl mx-auto px-4 sm:px-6">
          <ErrorState onRetry={load} />
        </div>
      </div>
    );
  }

  if (!curriculum || curriculum.levels.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <PageHeader displayName={displayName} />
        <div className="max-w-3xl mx-auto px-4 sm:px-6">
          <EmptyState language={language} />
        </div>
      </div>
    );
  }

  const currentLevel =
    curriculum.levels.find((l) => l.cefrLevel === activeCefr) ?? curriculum.levels[0];

  const isLocked = currentLevel.status === "NOT_STARTED" && currentLevel.ordinal > 1;
  const cta = resolveCtaAction(currentLevel);

  const lockedLevel = lockedMessage
    ? curriculum.levels.find((l) => l.cefrLevel === lockedMessage)
    : null;
  const lockedPrevLevel = lockedLevel
    ? curriculum.levels.find((l) => l.ordinal === lockedLevel.ordinal - 1)
    : null;

  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader displayName={curriculum.displayName || displayName} />

      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-8">

        {/* Level journey rail */}
        <LevelJourneyRail
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
            {/* Level hero */}
            <LevelHero level={currentLevel} />

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
              <div className="space-y-8 pb-8">
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
