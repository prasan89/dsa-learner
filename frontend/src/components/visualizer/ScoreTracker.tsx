import type { ChallengeScore } from "@/lib/visualizer/useInteractiveVisualizer";

interface ScoreTrackerProps {
  score: ChallengeScore;
}

export default function ScoreTracker({ score }: ScoreTrackerProps) {
  if (score.total === 0) return null;

  const pct = Math.round((score.correct / score.total) * 100);

  return (
    <div className="flex items-center gap-3 text-xs">
      <span className="text-gray-400">Score</span>
      <span className="font-mono font-semibold text-gray-700">
        {score.correct}
        <span className="text-gray-300">/{score.total}</span>
      </span>
      {score.total > 0 && (
        <div className="w-16 h-1 bg-gray-100 rounded-full overflow-hidden">
          <div
            className="h-full bg-brand-500 rounded-full transition-all duration-300"
            style={{ width: `${pct}%` }}
          />
        </div>
      )}
      {score.completed && (
        <span className="text-green-600 font-semibold">All done</span>
      )}
    </div>
  );
}
