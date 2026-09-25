import type { VisualizationCell } from "@/lib/visualizer/types";
import ArrayCell from "./ArrayCell";

interface ArrayDisplayProps {
  cells: VisualizationCell[];
}

export default function ArrayDisplay({ cells }: ArrayDisplayProps) {
  if (cells.length === 0) {
    return (
      <div className="flex items-center justify-center h-32 text-sm text-gray-400">
        No array to display.
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center gap-2">
      <div
        className="flex items-end gap-3 justify-center py-6 px-4"
        role="grid"
        aria-label="Array visualization"
      >
        {cells.map((cell) => (
          <ArrayCell
            key={cell.index}
            value={cell.value}
            index={cell.index}
            state={cell.state}
            label={cell.label}
          />
        ))}
      </div>
      <p className="text-[11px] text-gray-400 font-medium tracking-wide">Index (0-based)</p>
    </div>
  );
}
