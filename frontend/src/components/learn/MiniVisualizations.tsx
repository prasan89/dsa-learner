"use client";

// Pure SVG/HTML mini visualizations — one per learning category.
// Lightweight: no canvas, no animation libraries, minimal DOM.

interface MiniVizProps {
  animate?: boolean; // subtle CSS animation on hover
}

// ── Arrays ────────────────────────────────────────────────────────────────
export function ArraysViz({ animate }: MiniVizProps) {
  const vals = [10, 25, 31, 42, 57];
  return (
    <div className="flex flex-col items-start gap-1">
      <div className="flex items-center gap-1">
        {vals.map((v, i) => (
          <div
            key={i}
            className={`w-9 h-9 rounded-lg border text-xs font-mono font-semibold flex items-center justify-center transition-colors ${
              i === 2
                ? "bg-brand-600 border-brand-600 text-white"
                : "bg-white border-gray-200 text-gray-700"
            } ${animate && i === 2 ? "scale-105" : ""}`}
          >
            {v}
          </div>
        ))}
      </div>
      <div className="flex items-center gap-1 pl-0.5">
        {vals.map((_, i) => (
          <span key={i} className="w-9 text-center text-[10px] text-gray-400 font-mono">{i}</span>
        ))}
      </div>
    </div>
  );
}

// ── Linked Lists ──────────────────────────────────────────────────────────
export function LinkedListsViz(_: MiniVizProps) {
  const nodes = [10, 20, 30];
  return (
    <div className="flex items-center gap-1">
      {nodes.map((v, i) => (
        <div key={i} className="flex items-center gap-1">
          <div className="w-9 h-9 rounded-lg border border-gray-200 bg-white text-xs font-mono font-semibold text-gray-700 flex items-center justify-center">
            {v}
          </div>
          <svg width="14" height="10" viewBox="0 0 14 10" className="text-brand-400 shrink-0">
            <path d="M0 5h10M7 2l4 3-4 3" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
      ))}
      <span className="text-[10px] font-mono text-gray-400 ml-1">null</span>
    </div>
  );
}

// ── Stacks ────────────────────────────────────────────────────────────────
export function StacksViz(_: MiniVizProps) {
  const items = [10, 20, 30];
  return (
    <div className="flex items-end gap-3">
      <div className="flex flex-col-reverse gap-0.5">
        {items.map((v, i) => (
          <div
            key={i}
            className={`w-14 h-7 rounded border text-xs font-mono font-semibold flex items-center justify-center ${
              i === items.length - 1
                ? "bg-brand-600 border-brand-600 text-white"
                : "bg-white border-gray-200 text-gray-700"
            }`}
          >
            {v}
          </div>
        ))}
      </div>
      <div className="flex flex-col items-start gap-0.5 pb-0.5">
        <svg width="14" height="10" viewBox="0 0 14 10" className="text-brand-500 rotate-90">
          <path d="M0 5h10M7 2l4 3-4 3" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
        <span className="text-[9px] font-mono text-brand-600 font-semibold">TOP</span>
      </div>
    </div>
  );
}

// ── Queues ────────────────────────────────────────────────────────────────
export function QueuesViz(_: MiniVizProps) {
  const items = [10, 20, 30, 40];
  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center gap-0.5">
        {items.map((v, i) => (
          <div
            key={i}
            className={`w-9 h-8 rounded border text-xs font-mono font-semibold flex items-center justify-center ${
              i === 0
                ? "bg-green-50 border-green-300 text-green-700"
                : i === items.length - 1
                ? "bg-brand-50 border-brand-300 text-brand-700"
                : "bg-white border-gray-200 text-gray-700"
            }`}
          >
            {v}
          </div>
        ))}
      </div>
      <div className="flex justify-between px-0.5">
        <span className="text-[9px] text-green-600 font-semibold font-mono">FRONT</span>
        <span className="text-[9px] text-brand-600 font-semibold font-mono">REAR</span>
      </div>
    </div>
  );
}

// ── Hash Maps ─────────────────────────────────────────────────────────────
export function HashMapsViz(_: MiniVizProps) {
  return (
    <div className="flex items-start gap-2">
      <div className="flex flex-col gap-0.5 items-end">
        <span className="text-[10px] font-mono text-gray-500">hash(key)</span>
        <svg width="24" height="12" viewBox="0 0 24 12">
          <path d="M0 2 Q12 12 24 10" stroke="#6366f1" strokeWidth="1.5" fill="none" strokeLinecap="round" />
          <path d="M20 7l5 3-2-5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </div>
      <div className="flex flex-col gap-0.5">
        {[0, 2, 2].map((bucket, i) => (
          <div key={i} className="flex items-center gap-1">
            <div className="w-5 h-5 rounded border border-dashed border-gray-300 bg-gray-50 text-[9px] font-mono text-gray-400 flex items-center justify-center">{i}</div>
            {bucket > 0 && (
              <div className="flex gap-0.5">
                {Array.from({ length: bucket }).map((_, j) => (
                  <div key={j} className="w-9 h-5 rounded border border-brand-200 bg-brand-50 text-[9px] font-mono text-brand-700 flex items-center justify-center">k:v</div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Trees ─────────────────────────────────────────────────────────────────
export function TreesViz(_: MiniVizProps) {
  return (
    <svg width="120" height="72" viewBox="0 0 120 72" className="overflow-visible">
      {/* edges */}
      <line x1="60" y1="12" x2="30" y2="36" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="60" y1="12" x2="90" y2="36" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="30" y1="36" x2="15" y2="60" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="30" y1="36" x2="45" y2="60" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="90" y1="36" x2="75" y2="60" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="90" y1="36" x2="105" y2="60" stroke="#d1d5db" strokeWidth="1.5" />
      {/* root */}
      <circle cx="60" cy="12" r="11" fill="#4f46e5" />
      <text x="60" y="16" textAnchor="middle" fill="white" fontSize="9" fontFamily="monospace" fontWeight="600">50</text>
      {/* level 2 */}
      {[{cx:30,cy:36,v:"30"},{cx:90,cy:36,v:"70"}].map(({cx,cy,v},i) => (
        <g key={i}>
          <circle cx={cx} cy={cy} r="11" fill="white" stroke="#e5e7eb" strokeWidth="1.5" />
          <text x={cx} y={cy+4} textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">{v}</text>
        </g>
      ))}
      {/* level 3 */}
      {[{cx:15,cy:60,v:"20"},{cx:45,cy:60,v:"40"},{cx:75,cy:60,v:"60"},{cx:105,cy:60,v:"80"}].map(({cx,cy,v},i) => (
        <g key={i}>
          <circle cx={cx} cy={cy} r="11" fill="white" stroke="#e5e7eb" strokeWidth="1.5" />
          <text x={cx} y={cy+4} textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">{v}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Heaps ─────────────────────────────────────────────────────────────────
export function HeapsViz(_: MiniVizProps) {
  return (
    <svg width="100" height="64" viewBox="0 0 100 64" className="overflow-visible">
      <line x1="50" y1="12" x2="25" y2="36" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="50" y1="12" x2="75" y2="36" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="25" y1="36" x2="12" y2="58" stroke="#d1d5db" strokeWidth="1.5" />
      <line x1="25" y1="36" x2="38" y2="58" stroke="#d1d5db" strokeWidth="1.5" />
      <circle cx="50" cy="12" r="11" fill="#4f46e5" />
      <text x="50" y="16" textAnchor="middle" fill="white" fontSize="9" fontFamily="monospace" fontWeight="600">10</text>
      {[{cx:25,cy:36,v:"20"},{cx:75,cy:36,v:"30"}].map(({cx,cy,v},i)=>(
        <g key={i}><circle cx={cx} cy={cy} r="11" fill="white" stroke="#e5e7eb" strokeWidth="1.5"/><text x={cx} y={cy+4} textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">{v}</text></g>
      ))}
      {[{cx:12,cy:58,v:"40"},{cx:38,cy:58,v:"50"}].map(({cx,cy,v},i)=>(
        <g key={i}><circle cx={cx} cy={cy} r="11" fill="white" stroke="#e5e7eb" strokeWidth="1.5"/><text x={cx} y={cy+4} textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">{v}</text></g>
      ))}
    </svg>
  );
}

// ── Graphs ────────────────────────────────────────────────────────────────
export function GraphsViz(_: MiniVizProps) {
  const nodes = [{id:"A",cx:50,cy:12},{id:"B",cx:20,cy:40},{id:"C",cx:80,cy:40},{id:"D",cx:35,cy:65},{id:"E",cx:65,cy:65}];
  const edges = [["A","B"],["A","C"],["B","C"],["B","D"],["C","E"],["D","E"]] as [string,string][];
  const pos = Object.fromEntries(nodes.map(n => [n.id, {cx:n.cx,cy:n.cy}]));
  return (
    <svg width="100" height="78" viewBox="0 0 100 78" className="overflow-visible">
      {edges.map(([a,b],i) => (
        <line key={i} x1={pos[a].cx} y1={pos[a].cy} x2={pos[b].cx} y2={pos[b].cy} stroke="#d1d5db" strokeWidth="1.5"/>
      ))}
      {nodes.map(({id,cx,cy},i) => (
        <g key={i}>
          <circle cx={cx} cy={cy} r="11" fill={id==="A"?"#4f46e5":"white"} stroke={id==="A"?"#4f46e5":"#e5e7eb"} strokeWidth="1.5"/>
          <text x={cx} y={cy+4} textAnchor="middle" fill={id==="A"?"white":"#374151"} fontSize="10" fontFamily="monospace" fontWeight="700">{id}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Sorting ───────────────────────────────────────────────────────────────
export function SortingViz(_: MiniVizProps) {
  const bars = [{v:5,h:50},{v:2,h:20},{v:9,h:90},{v:1,h:10},{v:6,h:60}];
  const colors = ["#c7d2fe","#4f46e5","#c7d2fe","#c7d2fe","#c7d2fe"];
  return (
    <div className="flex items-end gap-1 h-14">
      {bars.map(({v,h},i) => (
        <div key={i} className="flex flex-col items-center gap-0.5">
          <div
            className="w-7 rounded-t-sm transition-all"
            style={{ height: `${h * 0.48}px`, backgroundColor: colors[i] }}
          />
          <span className="text-[9px] font-mono text-gray-400">{v}</span>
        </div>
      ))}
    </div>
  );
}

// ── Searching (Binary Search) ─────────────────────────────────────────────
export function SearchingViz(_: MiniVizProps) {
  const vals = [2,5,8,12,16,23,38];
  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center gap-0.5">
        {vals.map((v,i) => (
          <div key={i} className={`w-7 h-7 rounded border text-[9px] font-mono font-semibold flex items-center justify-center ${
            i===0?"bg-blue-50 border-blue-200 text-blue-700":
            i===3?"bg-brand-600 border-brand-600 text-white":
            i===6?"bg-red-50 border-red-200 text-red-700":
            "bg-white border-gray-200 text-gray-700"
          }`}>{v}</div>
        ))}
      </div>
      <div className="flex justify-between px-0.5">
        <span className="text-[9px] text-blue-500 font-mono font-semibold">left</span>
        <span className="text-[9px] text-brand-600 font-mono font-semibold">mid</span>
        <span className="text-[9px] text-red-400 font-mono font-semibold">right</span>
      </div>
    </div>
  );
}

// ── Two Pointers / Sliding Window ─────────────────────────────────────────
export function TwoPointersViz(_: MiniVizProps) {
  const vals = [1,2,3,4,5,6];
  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center gap-0.5">
        {vals.map((v,i) => (
          <div key={i} className={`w-8 h-8 rounded border text-xs font-mono font-semibold flex items-center justify-center ${
            i===1||i===2||i===3?"bg-brand-50 border-brand-300 text-brand-700":
            "bg-white border-gray-200 text-gray-600"
          }`}>{v}</div>
        ))}
      </div>
      <div className="flex gap-0.5">
        {vals.map((_,i) => (
          <div key={i} className="w-8 flex justify-center">
            {i===1&&<span className="text-[9px] text-brand-600 font-mono font-semibold">L</span>}
            {i===3&&<span className="text-[9px] text-brand-600 font-mono font-semibold">R</span>}
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Recursion / Backtracking ──────────────────────────────────────────────
export function RecursionViz(_: MiniVizProps) {
  return (
    <svg width="110" height="68" viewBox="0 0 110 68" className="overflow-visible">
      {/* f(3) */}
      <rect x="35" y="2" width="40" height="18" rx="4" fill="#4f46e5"/>
      <text x="55" y="15" textAnchor="middle" fill="white" fontSize="9" fontFamily="monospace" fontWeight="600">f(3)</text>
      {/* edges */}
      <line x1="45" y1="20" x2="22" y2="36" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="65" y1="20" x2="88" y2="36" stroke="#d1d5db" strokeWidth="1.2"/>
      {/* f(2) f(1) */}
      <rect x="2" y="36" width="40" height="18" rx="4" fill="white" stroke="#e5e7eb" strokeWidth="1.5"/>
      <text x="22" y="49" textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">f(2)</text>
      <rect x="68" y="36" width="40" height="18" rx="4" fill="white" stroke="#e5e7eb" strokeWidth="1.5"/>
      <text x="88" y="49" textAnchor="middle" fill="#374151" fontSize="9" fontFamily="monospace" fontWeight="600">f(1)</text>
      {/* f(1) f(0) under f(2) */}
      <line x1="12" y1="54" x2="8" y2="64" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="32" y1="54" x2="36" y2="64" stroke="#d1d5db" strokeWidth="1.2"/>
      <rect x="0" y="58" width="18" height="10" rx="3" fill="#f3f4f6" stroke="#e5e7eb" strokeWidth="1"/>
      <text x="9" y="66" textAnchor="middle" fill="#6b7280" fontSize="8" fontFamily="monospace">f(1)</text>
      <rect x="26" y="58" width="18" height="10" rx="3" fill="#f3f4f6" stroke="#e5e7eb" strokeWidth="1"/>
      <text x="35" y="66" textAnchor="middle" fill="#6b7280" fontSize="8" fontFamily="monospace">f(0)</text>
    </svg>
  );
}

// ── Dynamic Programming ───────────────────────────────────────────────────
export function DynamicProgrammingViz(_: MiniVizProps) {
  const table = [[0,1,2,3],[1,1,2,3],[2,1,2,3],[3,1,2,5]];
  const highlight = [[false,false,false,false],[false,false,false,false],[false,false,false,false],[false,false,false,true]];
  return (
    <div className="flex flex-col gap-0.5">
      {table.map((row,r) => (
        <div key={r} className="flex gap-0.5">
          {row.map((v,c) => (
            <div key={c} className={`w-7 h-6 rounded border text-[10px] font-mono font-semibold flex items-center justify-center ${
              highlight[r][c]?"bg-brand-600 border-brand-600 text-white":"bg-white border-gray-200 text-gray-700"
            }`}>{v}</div>
          ))}
        </div>
      ))}
    </div>
  );
}

// ── Greedy ────────────────────────────────────────────────────────────────
export function GreedyViz(_: MiniVizProps) {
  const tasks = [{label:"Task 1",w:40},{label:"Task 2",w:30},{label:"Task 3",w:55}];
  const colors = ["bg-gray-200","bg-brand-200","bg-brand-600"];
  return (
    <div className="flex flex-col gap-1.5 w-full">
      {tasks.map(({label,w},i) => (
        <div key={i} className="flex items-center gap-2">
          <span className="text-[9px] text-gray-400 font-mono w-10 shrink-0">{label}</span>
          <div className={`h-5 rounded ${colors[i]} text-[9px] font-mono flex items-center justify-center ${i===2?"text-white":"text-gray-600"}`}
            style={{width:`${w}px`}}/>
        </div>
      ))}
    </div>
  );
}

// ── Graph Algorithms ──────────────────────────────────────────────────────
export function GraphAlgorithmsViz(_: MiniVizProps) {
  const nodes=[{id:"A",cx:20,cy:30},{id:"B",cx:60,cy:10},{id:"C",cx:100,cy:30},{id:"D",cx:40,cy:60}];
  const edges=[{a:"A",b:"B",w:4},{a:"A",b:"D",w:3},{a:"B",b:"C",w:1},{a:"D",b:"C",w:5}];
  const pos=Object.fromEntries(nodes.map(n=>[n.id,{cx:n.cx,cy:n.cy}]));
  return (
    <svg width="120" height="75" viewBox="0 0 120 75" className="overflow-visible">
      {edges.map(({a,b,w},i)=>{
        const mx=(pos[a].cx+pos[b].cx)/2;
        const my=(pos[a].cy+pos[b].cy)/2;
        return (
          <g key={i}>
            <line x1={pos[a].cx} y1={pos[a].cy} x2={pos[b].cx} y2={pos[b].cy} stroke="#d1d5db" strokeWidth="1.5"/>
            <rect x={mx-6} y={my-6} width="12" height="12" rx="2" fill="white"/>
            <text x={mx} y={my+4} textAnchor="middle" fill="#6366f1" fontSize="8" fontFamily="monospace" fontWeight="600">{w}</text>
          </g>
        );
      })}
      {nodes.map(({id,cx,cy},i)=>(
        <g key={i}>
          <circle cx={cx} cy={cy} r="11" fill={id==="A"?"#4f46e5":"white"} stroke={id==="A"?"#4f46e5":"#e5e7eb"} strokeWidth="1.5"/>
          <text x={cx} y={cy+4} textAnchor="middle" fill={id==="A"?"white":"#374151"} fontSize="10" fontFamily="monospace" fontWeight="700">{id}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Advanced Data Structures (Trie) ───────────────────────────────────────
export function AdvancedDSViz(_: MiniVizProps) {
  return (
    <svg width="110" height="68" viewBox="0 0 110 68" className="overflow-visible">
      {/* root */}
      <circle cx="55" cy="10" r="10" fill="#4f46e5"/>
      <text x="55" y="14" textAnchor="middle" fill="white" fontSize="9" fontFamily="monospace" fontWeight="700">r</text>
      {/* level 2: a, c, t */}
      <line x1="55" y1="20" x2="22" y2="36" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="55" y1="20" x2="55" y2="36" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="55" y1="20" x2="88" y2="36" stroke="#d1d5db" strokeWidth="1.2"/>
      {[{cx:22,cy:36,v:"a"},{cx:55,cy:36,v:"c"},{cx:88,cy:36,v:"t"}].map(({cx,cy,v},i)=>(
        <g key={i}>
          <circle cx={cx} cy={cy} r="10" fill="white" stroke="#e5e7eb" strokeWidth="1.5"/>
          <text x={cx} y={cy+4} textAnchor="middle" fill="#374151" fontSize="10" fontFamily="monospace" fontWeight="600">{v}</text>
        </g>
      ))}
      {/* level 3 */}
      <line x1="22" y1="46" x2="10" y2="60" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="22" y1="46" x2="34" y2="60" stroke="#d1d5db" strokeWidth="1.2"/>
      <line x1="88" y1="46" x2="88" y2="60" stroke="#d1d5db" strokeWidth="1.2"/>
      {[{cx:10,cy:60,v:"n"},{cx:34,cy:60,v:"t"},{cx:88,cy:60,v:"r"}].map(({cx,cy,v},i)=>(
        <g key={i}>
          <circle cx={cx} cy={cy} r="8" fill="#f3f4f6" stroke="#e5e7eb" strokeWidth="1.2"/>
          <text x={cx} y={cy+3.5} textAnchor="middle" fill="#6b7280" fontSize="9" fontFamily="monospace">{v}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Registry ──────────────────────────────────────────────────────────────
export const CATEGORY_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "arrays":               ArraysViz,
  "linked-lists":         LinkedListsViz,
  "stacks":               StacksViz,
  "queues":               QueuesViz,
  "hash-maps":            HashMapsViz,
  "trees":                TreesViz,
  "heaps":                HeapsViz,
  "graphs":               GraphsViz,
  "sorting":              SortingViz,
  "searching":            SearchingViz,
  "two-pointers":         TwoPointersViz,
  "recursion":            RecursionViz,
  "dynamic-programming":  DynamicProgrammingViz,
  "greedy":               GreedyViz,
  "graph-algorithms":     GraphAlgorithmsViz,
  "advanced-ds":          AdvancedDSViz,
};
