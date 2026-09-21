"use client";

import { useState } from "react";

const DEFAULT_CODE = `import java.util.*;

public class Solution {
    public int[] solve(int[] nums) {
        // Your solution here
        return new int[]{};
    }
}`;

export default function ProblemPage({ params }: { params: { slug: string } }) {
  const [code, setCode] = useState(DEFAULT_CODE);
  const [activeTab, setActiveTab] = useState<"description" | "submissions">("description");

  return (
    <div className="h-screen bg-gray-950 text-white flex flex-col">
      {/* Top bar */}
      <header className="flex items-center gap-4 px-4 py-2 bg-gray-900 border-b border-gray-800">
        <a href="/problems" className="text-gray-400 hover:text-white text-sm">← Problems</a>
        <span className="text-gray-600">|</span>
        <span className="font-semibold">Problem Title</span>
        <span className="badge-easy ml-2">Easy</span>
      </header>

      {/* Split layout */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left: problem description */}
        <div className="w-1/2 flex flex-col border-r border-gray-800">
          <div className="flex border-b border-gray-800">
            {(["description", "submissions"] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-2 text-sm capitalize transition-colors ${
                  activeTab === tab
                    ? "text-white border-b-2 border-brand-500"
                    : "text-gray-400 hover:text-white"
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
          <div className="flex-1 overflow-y-auto p-4 prose prose-invert prose-sm max-w-none">
            {activeTab === "description" && (
              <p className="text-gray-400">Problem description will load here once API is connected.</p>
            )}
            {activeTab === "submissions" && (
              <p className="text-gray-400">Submission history will load here.</p>
            )}
          </div>
        </div>

        {/* Right: code editor + output */}
        <div className="w-1/2 flex flex-col">
          {/* Editor area — Monaco will be wired in Phase 1 Week 3 */}
          <div className="flex-1 bg-gray-900 p-4">
            <textarea
              value={code}
              onChange={(e) => setCode(e.target.value)}
              className="w-full h-full bg-transparent font-mono text-sm text-gray-200 resize-none focus:outline-none"
              spellCheck={false}
            />
          </div>

          {/* Action bar */}
          <div className="flex items-center gap-2 px-4 py-2 bg-gray-900 border-t border-gray-800">
            <span className="text-xs text-gray-500 mr-auto">Java 21</span>
            <button className="px-4 py-1.5 text-sm border border-gray-600 hover:border-gray-400 rounded text-gray-300 hover:text-white transition-colors">
              Run
            </button>
            <button className="px-4 py-1.5 text-sm bg-brand-600 hover:bg-brand-700 rounded text-white font-medium transition-colors">
              Submit
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
