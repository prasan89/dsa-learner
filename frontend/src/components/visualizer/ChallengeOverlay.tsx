"use client";

import { useEffect, useRef } from "react";
import { CheckCircle2, XCircle, ArrowRight } from "lucide-react";
import type { StepChallenge, ChallengeType } from "@/lib/visualizer/types";
import type { AnswerState } from "@/lib/visualizer/useInteractiveVisualizer";

const TYPE_LABEL: Record<ChallengeType, string> = {
  "predict-next-index":   "Predict next index",
  "predict-next-value":   "Predict next value",
  "predict-final-result": "Predict final result",
  "predict-complexity":   "Predict complexity",
  "identify-state":       "Identify state",
};

function ChoiceButton({
  choice,
  index,
  onSelect,
  answerState,
  correctAnswer,
}: {
  choice: { label: string; value: string };
  index: number;
  onSelect: () => void;
  answerState: AnswerState;
  correctAnswer: string;
}) {
  const isCorrect = choice.value === correctAnswer;
  const wasChosen =
    answerState.phase !== "unanswered" && answerState.chosen === choice.value;
  const answered = answerState.phase !== "unanswered";

  let base = "relative w-full text-left px-4 py-3 rounded-xl border-2 text-sm font-mono font-medium transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-brand-400/50 ";

  if (answered) {
    if (isCorrect) {
      base += "bg-green-50 border-green-400 text-green-800";
    } else if (wasChosen && answerState.phase === "wrong") {
      base += "bg-red-50 border-red-300 text-red-700";
    } else {
      base += "bg-white border-gray-100 text-gray-300 cursor-default";
    }
  } else {
    base += "bg-white border-gray-200 text-gray-800 hover:border-brand-400 hover:bg-brand-50 cursor-pointer active:scale-[0.98]";
  }

  return (
    <button
      className={base}
      onClick={onSelect}
      disabled={answered}
      aria-label={`Choice ${index + 1}: ${choice.label}`}
    >
      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[10px] font-sans font-bold text-gray-300 tabular-nums">
        {index + 1}
      </span>
      <span className="pl-4">{choice.label}</span>
    </button>
  );
}

interface ChallengeOverlayProps {
  challenge: StepChallenge;
  answerState: AnswerState;
  onSubmit: (value: string) => void;
  onContinue: () => void;
}

export default function ChallengeOverlay({
  challenge,
  answerState,
  onSubmit,
  onContinue,
}: ChallengeOverlayProps) {
  const isAnswered = answerState.phase !== "unanswered";
  const isCorrect  = answerState.phase === "correct";
  const isWrong    = answerState.phase === "wrong";

  // Stable ref so the effect only mounts once
  const cbRef = useRef({ isAnswered, isCorrect, choices: challenge.choices, onSubmit, onContinue });
  cbRef.current = { isAnswered, isCorrect, choices: challenge.choices, onSubmit, onContinue };

  useEffect(() => {
    function handleKey(e: KeyboardEvent) {
      if ((e.target as HTMLElement).matches("input,textarea,select")) return;
      const cb = cbRef.current;
      if (cb.isCorrect && (e.key === "Enter" || e.key === " ")) {
        e.preventDefault();
        cb.onContinue();
        return;
      }
      if (!cb.isAnswered) {
        const idx = parseInt(e.key, 10) - 1;
        if (idx >= 0 && idx < cb.choices.length) {
          e.preventDefault();
          cb.onSubmit(cb.choices[idx].value);
        }
      }
    }
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="rounded-2xl border border-brand-200 bg-white overflow-hidden">
      {/* Slim header */}
      <div className="flex items-center gap-2 px-5 py-3 border-b border-gray-100">
        <span className="text-[10px] font-bold uppercase tracking-widest text-brand-600">
          Challenge
        </span>
        <span className="text-gray-200">·</span>
        <span className="text-xs text-gray-400">{TYPE_LABEL[challenge.type]}</span>
        <span className="ml-auto text-[10px] text-gray-300">Press 1–4 to answer</span>
      </div>

      <div className="px-5 py-5 space-y-4">
        {/* Question */}
        <p className="text-sm font-semibold text-gray-900 leading-relaxed">
          {challenge.question}
        </p>

        {/* Choices */}
        <div className="grid grid-cols-2 gap-2">
          {challenge.choices.map((ch, i) => (
            <ChoiceButton
              key={ch.value}
              choice={ch}
              index={i}
              onSelect={() => onSubmit(ch.value)}
              answerState={answerState}
              correctAnswer={challenge.correctAnswer}
            />
          ))}
        </div>

        {/* Feedback */}
        {isAnswered && (
          <div
            className={`rounded-xl px-4 py-3 space-y-1.5 ${
              isCorrect ? "bg-green-50 border border-green-200" : "bg-red-50 border border-red-200"
            }`}
          >
            <div className="flex items-center gap-2">
              {isCorrect
                ? <CheckCircle2 size={15} className="text-green-500 shrink-0" />
                : <XCircle size={15} className="text-red-400 shrink-0" />}
              <span className={`text-sm font-semibold ${isCorrect ? "text-green-800" : "text-red-700"}`}>
                {isCorrect
                  ? "Correct!"
                  : isWrong && answerState.attempts > 1
                  ? `Answer: ${challenge.correctAnswer}`
                  : "Not quite — try again."}
              </span>
            </div>
            {(isCorrect || (isWrong && answerState.attempts > 1)) && (
              <p className="text-xs text-gray-600 leading-relaxed pl-[23px]">
                {challenge.explanation}
              </p>
            )}
          </div>
        )}

        {/* Continue */}
        {isCorrect && (
          <button
            onClick={onContinue}
            className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white text-sm font-semibold transition-colors"
            aria-label="Continue (Enter)"
          >
            Continue
            <ArrowRight size={13} strokeWidth={2.5} />
          </button>
        )}
      </div>
    </div>
  );
}
