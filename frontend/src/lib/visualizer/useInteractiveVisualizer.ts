"use client";

import { useState, useMemo, useCallback, useEffect, useRef } from "react";
import { useVisualizer } from "./useVisualizer";
import type { VisualizationStep, StepChallenge } from "./types";

// ─── Score tracking ───────────────────────────────────────────────────────────

export interface ChallengeScore {
  total: number;       // total challenges encountered
  correct: number;     // answered correctly on first try
  attempts: number;    // total answer attempts (including retries)
  mistakes: number;    // wrong answers
  completed: boolean;  // all challenges answered at least once
}

const INITIAL_SCORE: ChallengeScore = {
  total: 0,
  correct: 0,
  attempts: 0,
  mistakes: 0,
  completed: false,
};

// ─── Per-challenge answer state ───────────────────────────────────────────────

export type AnswerState =
  | { phase: "unanswered" }
  | { phase: "correct"; chosen: string }
  | { phase: "wrong"; chosen: string; attempts: number };

// ─── Hook return type ─────────────────────────────────────────────────────────

export interface UseInteractiveVisualizerReturn {
  // passthrough from useVisualizer
  currentStep: VisualizationStep | null;
  currentStepIndex: number;
  totalSteps: number;
  status: ReturnType<typeof useVisualizer>["status"];
  speed: number;
  play: () => void;
  pause: () => void;
  stepForward: () => void;
  stepBack: () => void;
  reset: () => void;
  setSpeed: (s: number) => void;

  // challenge state
  activeChallenge: StepChallenge | null;  // present → show the challenge overlay
  answerState: AnswerState;
  submitAnswer: (value: string) => void;
  dismissChallenge: () => void;           // advance past challenge after answering
  score: ChallengeScore;
  totalChallengesInSequence: number;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useInteractiveVisualizer(
  steps: VisualizationStep[],
  initialSpeed = 1
): UseInteractiveVisualizerReturn {
  const viz = useVisualizer(steps, initialSpeed);

  const [answerState, setAnswerState] = useState<AnswerState>({ phase: "unanswered" });
  const [score, setScore] = useState<ChallengeScore>(INITIAL_SCORE);
  // Track which step indices have been answered at least once (for completion)
  const answeredSteps = useRef<Set<number>>(new Set());

  // Count challenges in the whole sequence
  const totalChallengesInSequence = useMemo(
    () => steps.filter((s) => s.challenge != null).length,
    [steps]
  );

  // Update total challenges in score when steps change
  useEffect(() => {
    setScore((prev) => ({ ...prev, total: totalChallengesInSequence }));
    answeredSteps.current = new Set();
  }, [totalChallengesInSequence, steps]);

  // Detect the challenge on the current step
  const activeChallenge = useMemo<StepChallenge | null>(() => {
    if (!viz.currentStep) return null;
    const ch = viz.currentStep.challenge;
    if (!ch) return null;
    // Only present if this step hasn't already been dismissed
    if (answeredSteps.current.has(viz.currentStepIndex)) return null;
    return ch;
  }, [viz.currentStep, viz.currentStepIndex]);

  // Auto-pause when hitting a challenge step for the first time
  const prevStepIndex = useRef(-1);
  useEffect(() => {
    if (viz.currentStepIndex === prevStepIndex.current) return;
    prevStepIndex.current = viz.currentStepIndex;

    if (
      viz.currentStep?.challenge &&
      !answeredSteps.current.has(viz.currentStepIndex) &&
      viz.status === "playing"
    ) {
      viz.pause();
      setAnswerState({ phase: "unanswered" });
    }
  }, [viz.currentStepIndex, viz.currentStep, viz.status, viz.pause]);

  // Override stepForward so it clears the answer state for the new step
  const stepForward = useCallback(() => {
    setAnswerState({ phase: "unanswered" });
    viz.stepForward();
  }, [viz]);

  const stepBack = useCallback(() => {
    setAnswerState({ phase: "unanswered" });
    viz.stepBack();
  }, [viz]);

  const reset = useCallback(() => {
    setAnswerState({ phase: "unanswered" });
    setScore((prev) => ({ ...prev, correct: 0, attempts: 0, mistakes: 0, completed: false }));
    answeredSteps.current = new Set();
    viz.reset();
  }, [viz]);

  // Submit an answer to the active challenge
  const submitAnswer = useCallback(
    (chosen: string) => {
      if (!viz.currentStep?.challenge) return;
      const ch = viz.currentStep.challenge;
      const isCorrect = chosen === ch.correctAnswer;

      setScore((prev) => {
        const attempts = prev.attempts + 1;
        const mistakes = isCorrect ? prev.mistakes : prev.mistakes + 1;
        // Only award correct-on-first-try if no prior attempt on this step
        const firstTry = !answeredSteps.current.has(viz.currentStepIndex);
        const correct = isCorrect && firstTry ? prev.correct + 1 : prev.correct;
        return { ...prev, attempts, mistakes, correct };
      });

      if (isCorrect) {
        setAnswerState({ phase: "correct", chosen });
        answeredSteps.current.add(viz.currentStepIndex);
        // Update completion
        setScore((prev) => {
          const completed = answeredSteps.current.size >= totalChallengesInSequence;
          return { ...prev, completed };
        });
      } else {
        setAnswerState((prev) => ({
          phase: "wrong",
          chosen,
          attempts:
            prev.phase === "wrong" ? prev.attempts + 1 : 1,
        }));
      }
    },
    [viz.currentStep, viz.currentStepIndex, totalChallengesInSequence]
  );

  // Dismiss challenge (advance to next step) — only callable after correct answer
  const dismissChallenge = useCallback(() => {
    if (answerState.phase !== "correct") return;
    setAnswerState({ phase: "unanswered" });
    viz.stepForward();
  }, [answerState, viz]);

  return {
    ...viz,
    stepForward,
    stepBack,
    reset,
    activeChallenge,
    answerState,
    submitAnswer,
    dismissChallenge,
    score,
    totalChallengesInSequence,
  };
}
