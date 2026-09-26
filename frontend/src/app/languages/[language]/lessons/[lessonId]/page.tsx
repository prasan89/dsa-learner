"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import {
  ChevronLeft,
  RefreshCw,
  CheckCircle2,
  XCircle,
  ChevronRight,
  BookOpen,
} from "lucide-react";
import {
  academyApi,
  type LessonResponse,
  type ExperiencePlanStep,
  type StepType,
  type VocabularyCardPayload,
  type VocabularySummaryPayload,
  type MultipleChoicePayload,
  type FillInBlankPayload,
  type TranslationPayload,
  type LessonReviewPayload,
} from "@/lib/api/academy";
import { cn } from "@/lib/utils";

// ── Skeleton ──────────────────────────────────────────────────────────────────

function Pulse({ className }: { className?: string }) {
  return <div className={cn("animate-pulse bg-gray-100 rounded-lg", className)} />;
}

function LessonSkeleton() {
  return (
    <div className="flex flex-col min-h-screen bg-white">
      <div className="border-b border-gray-100 px-4 py-3 flex items-center gap-3">
        <Pulse className="h-8 w-8 rounded-full" />
        <Pulse className="h-2 flex-1 rounded-full" />
        <Pulse className="h-4 w-12" />
      </div>
      <div className="flex-1 flex flex-col items-center justify-center px-6 py-10 space-y-6 max-w-lg mx-auto w-full">
        <Pulse className="h-6 w-32" />
        <Pulse className="h-24 w-full rounded-2xl" />
        <Pulse className="h-5 w-48" />
        <Pulse className="h-5 w-40" />
      </div>
      <div className="border-t border-gray-100 px-6 py-4">
        <Pulse className="h-11 w-full rounded-xl" />
      </div>
    </div>
  );
}

// ── Lesson header ─────────────────────────────────────────────────────────────

function LessonHeader({
  language,
  cefrLevel,
  stepIndex,
  totalSteps,
  title,
}: {
  language: string;
  cefrLevel: string;
  stepIndex: number;
  totalSteps: number;
  title: string;
}) {
  const pct = totalSteps > 0 ? Math.round(((stepIndex) / totalSteps) * 100) : 0;

  return (
    <header className="bg-white border-b border-gray-100 px-4 sm:px-6 py-3 sticky top-0 z-30">
      <div className="max-w-lg mx-auto flex items-center gap-3">
        <Link
          href={`/languages/${language}?level=${cefrLevel}`}
          aria-label={`Back to ${language} curriculum`}
          className="shrink-0 w-8 h-8 flex items-center justify-center rounded-full text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          <ChevronLeft size={18} />
        </Link>

        <div className="flex-1 min-w-0">
          <div
            role="progressbar"
            aria-valuemin={0}
            aria-valuemax={totalSteps}
            aria-valuenow={stepIndex}
            aria-label={`Step ${stepIndex} of ${totalSteps}`}
            className="h-2 bg-gray-100 rounded-full overflow-hidden"
          >
            <div
              className="h-full bg-brand-600 rounded-full transition-[width] duration-500"
              style={{ width: `${pct}%` }}
            />
          </div>
        </div>

        <span className="shrink-0 text-xs text-gray-400 font-medium tabular-nums">
          {stepIndex} / {totalSteps}
        </span>
      </div>

      <div className="max-w-lg mx-auto mt-1 pl-11">
        <p className="text-xs text-gray-400 truncate">{title}</p>
      </div>
    </header>
  );
}

// ── Step components ───────────────────────────────────────────────────────────

// VOCABULARY_CARD
function VocabularyCardStep({ payload }: { payload: VocabularyCardPayload }) {
  return (
    <div className="flex flex-col items-center text-center space-y-5 py-4">
      <div className="rounded-2xl border border-gray-100 bg-white shadow-sm px-8 py-10 w-full max-w-sm space-y-3">
        <p className="text-3xl font-bold text-gray-900 leading-tight">{payload.german}</p>
        {payload.pronunciation && (
          <p className="text-sm text-gray-400 font-mono">{payload.pronunciation}</p>
        )}
        <p className="text-lg text-brand-700 font-medium">{payload.english}</p>
      </div>
      {payload.example && (
        <div className="w-full max-w-sm rounded-xl bg-gray-50 px-5 py-4 text-left">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Example</p>
          <p className="text-sm text-gray-700 italic">{payload.example}</p>
        </div>
      )}
    </div>
  );
}

// VOCABULARY_SUMMARY
function VocabularySummaryStep({ payload }: { payload: VocabularySummaryPayload }) {
  return (
    <div className="space-y-5 py-4">
      <div className="text-center">
        <p className="text-xl font-bold text-gray-900">
          You&apos;ve learned {payload.count} word{payload.count !== 1 ? "s" : ""}
        </p>
        <p className="text-sm text-gray-500 mt-1">Here&apos;s a quick recap</p>
      </div>
      <ul className="space-y-2" aria-label="Vocabulary recap">
        {payload.items.map((item, i) => (
          <li
            key={i}
            className="flex items-center justify-between rounded-xl border border-gray-100 bg-white px-4 py-3 shadow-sm"
          >
            <span className="font-semibold text-gray-900">{item.german}</span>
            <span className="text-sm text-gray-500">{item.english}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

// MULTIPLE_CHOICE
type AnswerState = "idle" | "correct" | "incorrect";

function MultipleChoiceStep({
  payload,
  onAnswer,
}: {
  payload: MultipleChoicePayload;
  onAnswer: (correct: boolean) => void;
}) {
  const [selected, setSelected] = useState<string | null>(null);
  const [answerState, setAnswerState] = useState<AnswerState>("idle");

  function handleSelect(option: string) {
    if (answerState !== "idle") return;
    setSelected(option);
  }

  function handleCheck() {
    if (!selected || answerState !== "idle") return;
    const correct = selected === payload.correctAnswer;
    setAnswerState(correct ? "correct" : "incorrect");
    onAnswer(correct);
  }

  function handleRetry() {
    setSelected(null);
    setAnswerState("idle");
    onAnswer(false); // signal retry — parent resets
  }

  return (
    <div className="space-y-5 py-4">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">Practice</p>
      <p className="text-lg font-semibold text-gray-900 leading-snug">{payload.question}</p>

      <ul className="space-y-2.5" role="radiogroup" aria-label="Answer options">
        {payload.options.map((option) => {
          const isSelected = selected === option;
          const isCorrect = option === payload.correctAnswer;
          const showCorrect = answerState !== "idle" && isCorrect;
          const showWrong = answerState === "incorrect" && isSelected && !isCorrect;

          return (
            <li key={option}>
              <button
                role="radio"
                aria-checked={isSelected}
                onClick={() => handleSelect(option)}
                disabled={answerState !== "idle"}
                className={cn(
                  "w-full text-left px-4 py-3 rounded-xl border text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500",
                  answerState === "idle" && !isSelected && "border-gray-200 bg-white text-gray-700 hover:border-brand-300 hover:bg-brand-50",
                  answerState === "idle" && isSelected && "border-brand-500 bg-brand-50 text-brand-700",
                  showCorrect && "border-green-400 bg-green-50 text-green-800",
                  showWrong && "border-red-300 bg-red-50 text-red-700",
                  answerState !== "idle" && !showCorrect && !showWrong && "border-gray-100 bg-gray-50 text-gray-400",
                )}
              >
                {option}
              </button>
            </li>
          );
        })}
      </ul>

      {answerState === "idle" && (
        <button
          onClick={handleCheck}
          disabled={!selected}
          className="w-full py-3 rounded-xl bg-brand-600 text-white font-semibold text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-brand-700 active:scale-[0.98] transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          Check answer
        </button>
      )}

      {answerState === "correct" && (
        <div className="rounded-xl border border-green-200 bg-green-50 px-5 py-4 space-y-1" role="alert">
          <p className="flex items-center gap-2 font-semibold text-green-800">
            <CheckCircle2 size={16} aria-hidden="true" /> Correct!
          </p>
          {payload.explanation && (
            <p className="text-sm text-green-700">{payload.explanation}</p>
          )}
        </div>
      )}

      {answerState === "incorrect" && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-5 py-4 space-y-2" role="alert">
          <p className="flex items-center gap-2 font-semibold text-red-700">
            <XCircle size={16} aria-hidden="true" /> Not quite.
          </p>
          <button
            onClick={handleRetry}
            className="text-sm font-semibold text-red-700 underline underline-offset-2 focus-visible:outline-none"
          >
            Try again
          </button>
        </div>
      )}
    </div>
  );
}

// FILL_IN_BLANK
function FillInBlankStep({
  payload,
  onAnswer,
}: {
  payload: FillInBlankPayload;
  onAnswer: (correct: boolean) => void;
}) {
  const [value, setValue] = useState("");
  const [answerState, setAnswerState] = useState<AnswerState>("idle");
  const inputRef = useRef<HTMLInputElement>(null);

  const parts = payload.question.split("___");

  function handleCheck() {
    if (!value.trim() || answerState !== "idle") return;
    const correct = value.trim().toLowerCase() === payload.correctAnswer.toLowerCase();
    setAnswerState(correct ? "correct" : "incorrect");
    onAnswer(correct);
  }

  function handleRetry() {
    setValue("");
    setAnswerState("idle");
    onAnswer(false);
    setTimeout(() => inputRef.current?.focus(), 50);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter") handleCheck();
  }

  return (
    <div className="space-y-5 py-4">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">Fill in the blank</p>

      <div className="text-lg font-semibold text-gray-900 leading-relaxed flex flex-wrap items-baseline gap-x-2 gap-y-1">
        {parts[0] && <span>{parts[0]}</span>}
        <input
          ref={inputRef}
          type="text"
          value={value}
          onChange={(e) => answerState === "idle" && setValue(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={answerState !== "idle"}
          aria-label="Fill in the blank"
          placeholder="___"
          className={cn(
            "border-b-2 bg-transparent outline-none text-center font-semibold min-w-[80px] max-w-[160px] px-1 pb-0.5 transition-colors",
            answerState === "idle" && "border-brand-400 text-brand-700 focus:border-brand-600",
            answerState === "correct" && "border-green-500 text-green-700",
            answerState === "incorrect" && "border-red-400 text-red-600",
          )}
        />
        {parts[1] && <span>{parts[1]}</span>}
      </div>

      {payload.hint && answerState === "idle" && (
        <p className="text-xs text-gray-400 italic">{payload.hint}</p>
      )}

      {answerState === "idle" && (
        <button
          onClick={handleCheck}
          disabled={!value.trim()}
          className="w-full py-3 rounded-xl bg-brand-600 text-white font-semibold text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-brand-700 active:scale-[0.98] transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          Check
        </button>
      )}

      {answerState === "correct" && (
        <div className="rounded-xl border border-green-200 bg-green-50 px-5 py-4" role="alert">
          <p className="flex items-center gap-2 font-semibold text-green-800">
            <CheckCircle2 size={16} aria-hidden="true" /> Correct!
          </p>
        </div>
      )}

      {answerState === "incorrect" && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-5 py-4 space-y-2" role="alert">
          <p className="flex items-center gap-2 font-semibold text-red-700">
            <XCircle size={16} aria-hidden="true" /> Not quite.
          </p>
          <button onClick={handleRetry} className="text-sm font-semibold text-red-700 underline underline-offset-2">
            Try again
          </button>
        </div>
      )}
    </div>
  );
}

// TRANSLATION
function TranslationStep({
  payload,
  onAnswer,
}: {
  payload: TranslationPayload;
  onAnswer: (correct: boolean) => void;
}) {
  const [value, setValue] = useState("");
  const [answerState, setAnswerState] = useState<AnswerState>("idle");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  function normalize(s: string) {
    return s.trim().toLowerCase().replace(/[.,!?]/g, "");
  }

  function handleCheck() {
    if (!value.trim() || answerState !== "idle") return;
    const correct = normalize(value) === normalize(payload.correctAnswer);
    setAnswerState(correct ? "correct" : "incorrect");
    onAnswer(correct);
  }

  function handleRetry() {
    setValue("");
    setAnswerState("idle");
    onAnswer(false);
    setTimeout(() => textareaRef.current?.focus(), 50);
  }

  return (
    <div className="space-y-5 py-4">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">Translate</p>
      <div className="rounded-2xl border border-gray-100 bg-gray-50 px-6 py-5">
        <p className="text-xl font-semibold text-gray-900 text-center">&ldquo;{payload.source}&rdquo;</p>
      </div>

      <textarea
        ref={textareaRef}
        value={value}
        onChange={(e) => answerState === "idle" && setValue(e.target.value)}
        disabled={answerState !== "idle"}
        aria-label="Your translation"
        placeholder="Type your translation..."
        rows={3}
        className={cn(
          "w-full rounded-xl border px-4 py-3 text-sm font-medium resize-none outline-none transition-colors focus:ring-2 focus:ring-brand-500",
          answerState === "idle" && "border-gray-200 text-gray-800",
          answerState === "correct" && "border-green-400 bg-green-50 text-green-800",
          answerState === "incorrect" && "border-red-300 bg-red-50 text-red-700",
        )}
      />

      {payload.hint && answerState === "idle" && (
        <p className="text-xs text-gray-400 italic">{payload.hint}</p>
      )}

      {answerState === "idle" && (
        <button
          onClick={handleCheck}
          disabled={!value.trim()}
          className="w-full py-3 rounded-xl bg-brand-600 text-white font-semibold text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-brand-700 active:scale-[0.98] transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          Check
        </button>
      )}

      {answerState === "correct" && (
        <div className="rounded-xl border border-green-200 bg-green-50 px-5 py-4" role="alert">
          <p className="flex items-center gap-2 font-semibold text-green-800">
            <CheckCircle2 size={16} aria-hidden="true" /> Correct!
          </p>
        </div>
      )}

      {answerState === "incorrect" && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-5 py-4 space-y-2" role="alert">
          <p className="flex items-center gap-2 font-semibold text-red-700">
            <XCircle size={16} aria-hidden="true" /> Not quite — try again.
          </p>
          <p className="text-xs text-red-600">Hint: &ldquo;{payload.correctAnswer}&rdquo;</p>
          <button onClick={handleRetry} className="text-sm font-semibold text-red-700 underline underline-offset-2">
            Try again
          </button>
        </div>
      )}
    </div>
  );
}

// NARRATIVE
function NarrativeStep({ payload }: { payload: Record<string, unknown> }) {
  const title = (payload.title ?? payload.heading) as string | undefined;
  const body = (payload.body ?? payload.text ?? payload.content) as string | undefined;
  const objectives = payload.objectives as string[] | undefined;
  const intro = typeof payload.intro === "object" && payload.intro !== null
    ? (payload.intro as { text?: string }).text
    : undefined;

  return (
    <div className="space-y-5 py-6">
      {title && <h2 className="text-2xl font-bold text-gray-900 text-center leading-snug">{title}</h2>}
      {(body ?? intro) && (
        <p className="text-base text-gray-700 text-center leading-relaxed">{body ?? intro}</p>
      )}
      {objectives && objectives.length > 0 && (
        <div className="rounded-xl border border-brand-100 bg-brand-50 px-5 py-4 space-y-2">
          <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">In this lesson</p>
          <ul className="space-y-1">
            {objectives.map((obj, i) => (
              <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
                <span className="mt-1 shrink-0 w-1.5 h-1.5 rounded-full bg-brand-500" aria-hidden="true" />
                {obj}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

// GRAMMAR_EXPLANATION
function GrammarExplanationStep({ payload }: { payload: Record<string, unknown> }) {
  const title = (payload.title ?? payload.concept) as string | undefined;
  const explanation = (payload.explanation ?? payload.rule) as string | undefined;
  const pattern = payload.pattern as string | undefined;
  const examples = (payload.examples ?? []) as Array<{ german?: string; english?: string; translation?: string }>;

  return (
    <div className="space-y-5 py-4">
      <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide">Grammar</p>
      {title && <h2 className="text-xl font-bold text-gray-900">{title}</h2>}
      {explanation && <p className="text-sm text-gray-600 leading-relaxed">{explanation}</p>}
      {examples.length > 0 && (
        <ul className="space-y-3">
          {examples.map((ex, i) => (
            <li key={i} className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
              <p className="font-semibold text-gray-900">{ex.german}</p>
              <p className="text-sm text-gray-500">{ex.english ?? ex.translation}</p>
            </li>
          ))}
        </ul>
      )}
      {pattern && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3">
          <p className="text-xs font-semibold text-amber-700 uppercase tracking-wide mb-1">Pattern</p>
          <p className="text-sm font-mono text-amber-800">{pattern}</p>
        </div>
      )}
    </div>
  );
}

// LESSON_REVIEW
function LessonReviewStep({
  payload,
  vocabCount,
  language,
  onComplete,
  completing,
}: {
  payload: LessonReviewPayload;
  vocabCount: number;
  language: string;
  onComplete: () => void;
  completing: boolean;
}) {
  return (
    <div className="flex flex-col items-center text-center space-y-6 py-8">
      <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center">
        <CheckCircle2 size={32} className="text-green-600" aria-hidden="true" />
      </div>
      <div>
        <p className="text-2xl font-bold text-gray-900">Lesson complete</p>
        <p className="text-sm text-gray-500 mt-1">{payload.lessonTitle}</p>
      </div>

      <ul className="w-full max-w-xs text-left space-y-2">
        {vocabCount > 0 && (
          <li className="flex items-center gap-3 text-sm text-gray-700">
            <span className="w-1.5 h-1.5 rounded-full bg-brand-500 shrink-0" aria-hidden="true" />
            {vocabCount} vocabulary word{vocabCount !== 1 ? "s" : ""}
          </li>
        )}
        {payload.exerciseCount > 0 && (
          <li className="flex items-center gap-3 text-sm text-gray-700">
            <span className="w-1.5 h-1.5 rounded-full bg-brand-500 shrink-0" aria-hidden="true" />
            {payload.exerciseCount} practice exercise{payload.exerciseCount !== 1 ? "s" : ""}
          </li>
        )}
      </ul>

      <div className="w-full max-w-xs space-y-3 pt-2">
        <button
          onClick={onComplete}
          disabled={completing}
          className="w-full py-3 rounded-xl bg-brand-600 text-white font-semibold text-sm hover:bg-brand-700 active:scale-[0.98] transition-all disabled:opacity-60 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 flex items-center justify-center gap-2"
        >
          {completing ? (
            <>
              <RefreshCw size={14} className="animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Back to curriculum"
          )}
        </button>
        <Link
          href={`/languages/${language}`}
          className="block text-center text-sm text-gray-400 hover:text-gray-600 py-1"
        >
          Skip for now
        </Link>
      </div>
    </div>
  );
}

// UNSUPPORTED fallback
function UnsupportedStep({ type }: { type: StepType }) {
  return (
    <div className="flex flex-col items-center justify-center text-center py-12 space-y-3">
      <BookOpen size={28} className="text-gray-300" aria-hidden="true" />
      <p className="text-sm font-medium text-gray-500">This lesson step isn&apos;t available yet.</p>
      <p className="text-xs text-gray-300">{type}</p>
    </div>
  );
}

// ── Step Renderer (registry pattern) ──────────────────────────────────────────

interface StepRendererProps {
  step: ExperiencePlanStep;
  vocabCount: number;
  language: string;
  onAnswered: (correct: boolean) => void;
  onComplete: () => void;
  completing: boolean;
}

function StepRenderer({ step, vocabCount, language, onAnswered, onComplete, completing }: StepRendererProps) {
  const p = step.payload as Record<string, unknown>;

  switch (step.type) {
    case "VOCABULARY_CARD":
      return <VocabularyCardStep payload={p as unknown as VocabularyCardPayload} />;

    case "VOCABULARY_SUMMARY":
      return <VocabularySummaryStep payload={p as unknown as VocabularySummaryPayload} />;

    case "MULTIPLE_CHOICE":
      return (
        <MultipleChoiceStep
          key={step.index}
          payload={p as unknown as MultipleChoicePayload}
          onAnswer={onAnswered}
        />
      );

    case "FILL_IN_BLANK":
      return (
        <FillInBlankStep
          key={step.index}
          payload={p as unknown as FillInBlankPayload}
          onAnswer={onAnswered}
        />
      );

    case "TRANSLATION":
      return (
        <TranslationStep
          key={step.index}
          payload={p as unknown as TranslationPayload}
          onAnswer={onAnswered}
        />
      );

    case "NARRATIVE":
      return <NarrativeStep payload={p} />;

    case "GRAMMAR_EXPLANATION":
      return <GrammarExplanationStep payload={p} />;

    case "LESSON_REVIEW":
      return (
        <LessonReviewStep
          payload={p as unknown as LessonReviewPayload}
          vocabCount={vocabCount}
          language={language}
          onComplete={onComplete}
          completing={completing}
        />
      );

    default:
      return <UnsupportedStep type={step.type} />;
  }
}

// ── Error / Locked states ─────────────────────────────────────────────────────

function ErrorState({ language, onRetry }: { language: string; onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen text-center px-6 space-y-4">
      <p className="text-3xl" aria-hidden="true">⚠️</p>
      <p className="font-semibold text-gray-700">Something went wrong loading this lesson.</p>
      <p className="text-sm text-gray-400">Check your connection and try again.</p>
      <div className="flex gap-3">
        <button onClick={onRetry} className="btn-primary flex items-center gap-2">
          <RefreshCw size={14} aria-hidden="true" /> Try again
        </button>
        <Link href={`/languages/${language}`} className="btn-secondary">
          Back
        </Link>
      </div>
    </div>
  );
}

function LockedState({ language }: { language: string }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen text-center px-6 space-y-4">
      <p className="font-semibold text-gray-700">This lesson is locked.</p>
      <p className="text-sm text-gray-400">Complete earlier lessons to unlock it.</p>
      <Link href={`/languages/${language}`} className="btn-primary">
        Back to curriculum
      </Link>
    </div>
  );
}

// ── Navigation footer ─────────────────────────────────────────────────────────

function NavFooter({
  step,
  isAnswered,
  onContinue,
}: {
  step: ExperiencePlanStep;
  isAnswered: boolean;
  onContinue: () => void;
}) {
  // LESSON_REVIEW handles its own CTA — hide the footer nav
  if (step.type === "LESSON_REVIEW") return null;

  // Exercise steps: Continue only enabled after answering correctly
  const isExercise = step.isExercise;
  const disabled = isExercise && !isAnswered;

  return (
    <div className="border-t border-gray-100 bg-white px-6 py-4">
      <div className="max-w-lg mx-auto">
        <button
          onClick={onContinue}
          disabled={disabled}
          className={cn(
            "w-full py-3 rounded-xl font-semibold text-sm transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 flex items-center justify-center gap-2",
            disabled
              ? "bg-gray-100 text-gray-400 cursor-not-allowed"
              : "bg-brand-600 text-white hover:bg-brand-700 active:scale-[0.98]"
          )}
        >
          Continue <ChevronRight size={16} aria-hidden="true" />
        </button>
      </div>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function LessonPlayerPage() {
  const params = useParams<{ language: string; lessonId: string }>();
  const router = useRouter();
  const { language, lessonId } = params;

  const [lesson, setLesson] = useState<LessonResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<"generic" | "locked" | null>(null);
  const [stepIndex, setStepIndex] = useState(0);
  const [answered, setAnswered] = useState(false);
  const [completing, setCompleting] = useState(false);
  const persistTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    academyApi
      .getLesson(language, lessonId)
      .then((res) => {
        setLesson(res.data);
        const resumeIndex = res.data.learnerStatus === "COMPLETED"
          ? res.data.experiencePlan.totalSteps - 1
          : res.data.currentStepIndex;
        setStepIndex(Math.min(resumeIndex, res.data.experiencePlan.totalSteps - 1));
      })
      .catch((err) => {
        const status = err?.response?.status;
        if (status === 403 || status === 423) setError("locked");
        else setError("generic");
      })
      .finally(() => setLoading(false));
  }, [language, lessonId]);

  useEffect(() => {
    load();
  }, [load]);

  // Debounced step persistence — fires 1s after stepIndex settles
  useEffect(() => {
    if (!lesson || loading) return;
    if (lesson.learnerStatus === "COMPLETED") return;

    if (persistTimerRef.current) clearTimeout(persistTimerRef.current);
    persistTimerRef.current = setTimeout(() => {
      academyApi.updateStepProgress(language, lessonId, stepIndex).catch(() => {
        // Non-critical — progress not lost, just not persisted this tick
      });
    }, 1000);

    return () => {
      if (persistTimerRef.current) clearTimeout(persistTimerRef.current);
    };
  }, [stepIndex, lesson, language, lessonId, loading]);

  const vocabCount = lesson
    ? lesson.experiencePlan.steps.filter((s) => s.type === "VOCABULARY_CARD").length
    : 0;

  function handleAnswered(correct: boolean) {
    if (correct) setAnswered(true);
    // If incorrect, answered stays false — exercise remains blocked
  }

  function handleContinue() {
    if (!lesson) return;
    const next = stepIndex + 1;
    if (next >= lesson.experiencePlan.totalSteps) return;
    setStepIndex(next);
    setAnswered(false);
  }

  async function handleComplete() {
    if (!lesson || completing) return;
    setCompleting(true);

    // Score = percentage of exercises answered correctly (simple heuristic: 100 if reached review)
    const score = 100;

    try {
      await academyApi.completeLesson(language, lessonId, score);
    } catch {
      // Complete failed — still navigate back, backend is idempotent
    } finally {
      setCompleting(false);
      router.push(`/languages/${language}?level=${lesson.experiencePlan.cefrLevel}`);
    }
  }

  if (loading) return <LessonSkeleton />;
  if (error === "locked") return <LockedState language={language} />;
  if (error === "generic") return <ErrorState language={language} onRetry={load} />;
  if (!lesson) return <ErrorState language={language} onRetry={load} />;

  const steps = lesson.experiencePlan.steps;
  const currentStep = steps[stepIndex];

  if (!currentStep) return <ErrorState language={language} onRetry={load} />;

  return (
    <div className="flex flex-col min-h-screen bg-white">
      <LessonHeader
        language={language}
        cefrLevel={lesson.experiencePlan.cefrLevel}
        stepIndex={stepIndex}
        totalSteps={lesson.experiencePlan.totalSteps}
        title={lesson.title}
      />

      <main className="flex-1 overflow-y-auto">
        <div className="max-w-lg mx-auto px-4 sm:px-6 py-6">
          <StepRenderer
            step={currentStep}
            vocabCount={vocabCount}
            language={language}
            onAnswered={handleAnswered}
            onComplete={handleComplete}
            completing={completing}
          />
        </div>
      </main>

      <NavFooter
        step={currentStep}
        isAnswered={answered}
        onContinue={handleContinue}
      />
    </div>
  );
}
