"use client";

import { useEffect, useRef } from "react";
import { Play, Pause, ChevronLeft, ChevronRight, RotateCcw } from "lucide-react";
import type { VisualizerStatus } from "@/lib/visualizer/types";

interface VisualizerControlsProps {
  status: VisualizerStatus;
  currentStep: number;
  totalSteps: number;
  speed: number;
  onPlay: () => void;
  onPause: () => void;
  onStepBack: () => void;
  onStepForward: () => void;
  onReset: () => void;
  onSpeedChange: (speed: number) => void;
}

const SPEEDS = [1, 2, 4, 8];
const SPEED_LABELS: Record<number, string> = { 1: "1×", 2: "2×", 4: "4×", 8: "8×" };

export default function VisualizerControls({
  status,
  currentStep,
  totalSteps,
  speed,
  onPlay,
  onPause,
  onStepBack,
  onStepForward,
  onReset,
  onSpeedChange,
}: VisualizerControlsProps) {
  const isPlaying = status === "playing";
  const isDone    = status === "done";
  const isEmpty   = totalSteps === 0;
  const atStart   = currentStep <= 0;
  const progress  = totalSteps > 1
    ? (currentStep / (totalSteps - 1)) * 100
    : isDone ? 100 : 0;

  // Stable ref captures latest callbacks so the effect only mounts once
  const cbRef = useRef({ isPlaying, onPlay, onPause, onStepBack, onStepForward, onReset });
  cbRef.current = { isPlaying, onPlay, onPause, onStepBack, onStepForward, onReset };

  useEffect(() => {
    function handleKey(e: KeyboardEvent) {
      if ((e.target as HTMLElement).matches("input,textarea,select")) return;
      const cb = cbRef.current;
      if (e.key === " " || e.key === "k") {
        e.preventDefault();
        cb.isPlaying ? cb.onPause() : cb.onPlay();
      } else if (e.key === "ArrowRight" || e.key === "l") {
        e.preventDefault();
        cb.onStepForward();
      } else if (e.key === "ArrowLeft" || e.key === "j") {
        e.preventDefault();
        cb.onStepBack();
      } else if (e.key === "r" || e.key === "R") {
        e.preventDefault();
        cb.onReset();
      }
    }
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-3">
      {/* Progress track */}
      <div className="flex items-center gap-3">
        <div
          className="flex-1 h-[6px] bg-gray-100 rounded-full overflow-hidden"
          role="progressbar"
          aria-valuenow={currentStep + 1}
          aria-valuemin={1}
          aria-valuemax={Math.max(totalSteps, 1)}
          aria-label="Algorithm progress"
        >
          <div
            className="h-full bg-brand-500 rounded-full transition-all duration-300 ease-out"
            style={{ width: `${progress}%` }}
          />
        </div>
        <span className="text-[11px] font-mono text-gray-400 tabular-nums shrink-0 w-14 text-right">
          {isEmpty ? "—" : `${currentStep + 1} / ${totalSteps}`}
        </span>
      </div>

      {/* Controls */}
      <div className="flex items-center gap-1.5">

        {/* Reset */}
        <button
          onClick={onReset}
          disabled={isEmpty || (atStart && status === "idle")}
          className="p-2.5 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
          aria-label="Reset (R)"
          title="Reset (R)"
        >
          <RotateCcw size={14} strokeWidth={2.5} />
        </button>

        {/* Step back */}
        <button
          onClick={onStepBack}
          disabled={isEmpty || atStart}
          className="p-2.5 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
          aria-label="Step back (←)"
          title="Step back (←)"
        >
          <ChevronLeft size={16} strokeWidth={2.5} />
        </button>

        {/* Play / Pause — primary */}
        <button
          onClick={isPlaying ? onPause : onPlay}
          disabled={isEmpty}
          className="flex items-center gap-1.5 px-5 h-10 rounded-lg bg-brand-600 hover:bg-brand-700 active:bg-brand-800 disabled:opacity-40 disabled:cursor-not-allowed text-white font-semibold text-sm transition-colors"
          aria-label={isPlaying ? "Pause (Space)" : isDone ? "Replay (Space)" : "Play (Space)"}
        >
          {isPlaying
            ? <Pause size={14} strokeWidth={2.5} />
            : <Play size={14} strokeWidth={2.5} />}
          <span>{isPlaying ? "Pause" : isDone ? "Replay" : "Play"}</span>
        </button>

        {/* Step forward */}
        <button
          onClick={onStepForward}
          disabled={isEmpty || isDone}
          className="p-2.5 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
          aria-label="Step forward (→)"
          title="Step forward (→)"
        >
          <ChevronRight size={16} strokeWidth={2.5} />
        </button>

        <div className="flex-1" />

        {/* Speed */}
        <div className="flex items-center gap-0.5 bg-gray-100 rounded-lg p-0.5">
          {SPEEDS.map((s) => (
            <button
              key={s}
              onClick={() => onSpeedChange(s)}
              className={`text-xs font-mono font-semibold px-2.5 py-1.5 rounded-md transition-all ${
                speed === s
                  ? "bg-white text-gray-900 shadow-sm"
                  : "text-gray-500 hover:text-gray-700"
              }`}
              aria-label={`Speed ${s}x`}
              aria-pressed={speed === s}
            >
              {SPEED_LABELS[s]}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
