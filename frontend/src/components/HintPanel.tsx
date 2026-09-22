"use client";

import { useState } from "react";
import { Lightbulb, Lock, Unlock, ChevronDown, ChevronRight } from "lucide-react";
import { hintsApi } from "@/lib/api/hints";
import type { Hint } from "@/types";

const HINT_LABELS = ["Concept", "Direction", "Algorithm"];
const HINT_COLORS = [
  "border-yellow-500/50 bg-yellow-500/5",
  "border-orange-500/50 bg-orange-500/5",
  "border-red-500/50 bg-red-500/5",
];

interface HintPanelProps {
  problemSlug: string;
  hints: Hint[];
  onHintsUpdate: (hints: Hint[]) => void;
}

export default function HintPanel({ problemSlug, hints, onHintsUpdate }: HintPanelProps) {
  const [expanded, setExpanded] = useState<number | null>(null);
  const [unlocking, setUnlocking] = useState<number | null>(null);

  async function handleUnlock(level: number) {
    setUnlocking(level);
    try {
      const res = await hintsApi.unlock(problemSlug, level);
      const updated = hints.map((h) => (h.level === level ? res.data : h));
      onHintsUpdate(updated);
      setExpanded(level);
    } catch (e) {
      console.error(e);
    } finally {
      setUnlocking(null);
    }
  }

  return (
    <div className="flex flex-col gap-2 p-3">
      <div className="flex items-center gap-2 text-yellow-400 mb-1">
        <Lightbulb size={16} />
        <span className="text-sm font-semibold">Hints</span>
        <span className="text-xs text-gray-500 ml-auto">({hints.filter((h) => h.unlocked).length}/{hints.length} used)</span>
      </div>

      {hints.map((hint) => {
        const idx = hint.level - 1;
        const isOpen = expanded === hint.level;

        return (
          <div key={hint.id} className={`rounded-lg border ${HINT_COLORS[idx]} overflow-hidden`}>
            <button
              className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-white/5 transition-colors"
              onClick={() => {
                if (hint.unlocked) {
                  setExpanded(isOpen ? null : hint.level);
                } else {
                  handleUnlock(hint.level);
                }
              }}
              disabled={unlocking === hint.level}
            >
              {hint.unlocked ? (
                <Unlock size={13} className="text-green-400 shrink-0" />
              ) : (
                <Lock size={13} className="text-gray-500 shrink-0" />
              )}
              <span className={hint.unlocked ? "text-gray-200" : "text-gray-500"}>
                Hint {hint.level}: {hint.label ?? HINT_LABELS[idx]}
              </span>
              {hint.unlocked && (
                <span className="ml-auto text-gray-500">
                  {isOpen ? <ChevronDown size={13} /> : <ChevronRight size={13} />}
                </span>
              )}
              {!hint.unlocked && unlocking === hint.level && (
                <span className="ml-auto text-xs text-gray-500">Unlocking…</span>
              )}
              {!hint.unlocked && unlocking !== hint.level && (
                <span className="ml-auto text-xs text-yellow-600">Click to unlock</span>
              )}
            </button>

            {hint.unlocked && isOpen && (
              <div className="px-3 pb-3 text-xs text-gray-300 leading-relaxed border-t border-white/5 pt-2">
                {hint.content}
              </div>
            )}
          </div>
        );
      })}

      {hints.length === 0 && (
        <p className="text-xs text-gray-500 text-center py-2">No hints available</p>
      )}
    </div>
  );
}
