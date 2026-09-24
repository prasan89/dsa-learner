import React from "react";

interface MiniVizProps { animate?: boolean }

// ── Core Java ────────────────────────────────────────────────────────────────

export function JavaFundamentalsViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed text-gray-500 bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">public static void </span>
      <span className="text-yellow-300">main</span>
      <span className="text-gray-300">(String[] args) {"{"}</span>
      <br />
      <span className="text-purple-400">  int </span>
      <span className="text-blue-300">x</span>
      <span className="text-gray-300"> = </span>
      <span className="text-green-300">42</span>
      <span className="text-gray-300">;</span>
      <br />
      <span className="text-blue-300">  System</span>
      <span className="text-gray-300">.out.</span>
      <span className="text-yellow-300">println</span>
      <span className="text-gray-300">(x);</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function MethodsViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">int </span>
      <span className="text-yellow-300">add</span>
      <span className="text-gray-300">(</span>
      <span className="text-purple-400">int </span>
      <span className="text-blue-300">a</span>
      <span className="text-gray-300">, </span>
      <span className="text-purple-400">int </span>
      <span className="text-blue-300">b</span>
      <span className="text-gray-300">) {"{"}</span>
      <br />
      <span className="text-purple-400">  return </span>
      <span className="text-blue-300">a </span>
      <span className="text-gray-300">+ </span>
      <span className="text-blue-300">b</span>
      <span className="text-gray-300">;</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
      <br />
      <span className="text-gray-500">// overloading</span>
      <br />
      <span className="text-purple-400">double </span>
      <span className="text-yellow-300">add</span>
      <span className="text-gray-300">(</span>
      <span className="text-purple-400">double </span>
      <span className="text-blue-300">a</span>
      <span className="text-gray-300">, </span>
      <span className="text-purple-400">double </span>
      <span className="text-blue-300">b</span>
      <span className="text-gray-300">) …</span>
    </div>
  );
}

export function OopViz() {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {/* Animal parent */}
      <rect x="60" y="2" width="60" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="17" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Animal</text>
      {/* Dog */}
      <rect x="10" y="44" width="52" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="36" y="59" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">Dog</text>
      {/* Cat */}
      <rect x="70" y="44" width="52" height="22" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="96" y="59" textAnchor="middle" fontSize="9" fontWeight="600" fill="#ca8a04">Cat</text>
      {/* Cat2 */}
      <rect x="128" y="44" width="46" height="22" rx="4" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1" />
      <text x="151" y="59" textAnchor="middle" fontSize="9" fontWeight="600" fill="#db2777">Bird</text>
      {/* Arrows */}
      <line x1="36" y1="44" x2="75" y2="24" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="96" y1="44" x2="91" y2="24" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="151" y1="44" x2="105" y2="24" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
    </svg>
  );
}

export function InterfacesViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* interface box */}
      <rect x="55" y="2" width="70" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="10" textAnchor="middle" fontSize="7" fill="#7c3aed" fontStyle="italic">«interface»</text>
      <text x="90" y="20" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Flyable</text>
      {/* concrete */}
      <rect x="8" y="44" width="62" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="39" y="58" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">Airplane</text>
      <rect x="110" y="44" width="62" height="20" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="141" y="58" textAnchor="middle" fontSize="9" fontWeight="600" fill="#ca8a04">Drone</text>
      <line x1="39" y1="44" x2="78" y2="24" stroke="#7c3aed" strokeWidth="1.2" strokeDasharray="3,2" />
      <line x1="141" y1="44" x2="105" y2="24" stroke="#7c3aed" strokeWidth="1.2" strokeDasharray="3,2" />
    </svg>
  );
}

export function ExceptionHandlingViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">try </span>
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-blue-300">  riskyOp</span>
      <span className="text-gray-300">();</span>
      <br />
      <span className="text-gray-300">{"} "}</span>
      <span className="text-purple-400">catch </span>
      <span className="text-gray-300">(</span>
      <span className="text-yellow-300">Exception</span>
      <span className="text-blue-300"> e</span>
      <span className="text-gray-300">) {"{"}</span>
      <br />
      <span className="text-gray-500">  // handle</span>
      <br />
      <span className="text-gray-300">{"} "}</span>
      <span className="text-purple-400">finally </span>
      <span className="text-gray-300">{"{"}</span>
      <span className="text-blue-300"> close()</span>
      <span className="text-gray-300">; {"}"}</span>
    </div>
  );
}

export function StringsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* String pool box */}
      <rect x="4" y="4" width="172" height="30" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="10" y="14" fontSize="7" fill="#7c3aed" fontWeight="600">String Pool</text>
      <rect x="14" y="17" width="44" height="13" rx="3" fill="white" stroke="#7c3aed" strokeWidth="0.8" />
      <text x="36" y="27" textAnchor="middle" fontSize="8" fill="#4f46e5">"hello"</text>
      <rect x="64" y="17" width="44" height="13" rx="3" fill="white" stroke="#7c3aed" strokeWidth="0.8" />
      <text x="86" y="27" textAnchor="middle" fontSize="8" fill="#4f46e5">"world"</text>
      {/* StringBuilder */}
      <rect x="4" y="46" width="80" height="18" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="44" y="59" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">StringBuilder</text>
      {/* immutable tag */}
      <rect x="100" y="46" width="76" height="18" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="138" y="59" textAnchor="middle" fontSize="8" fontWeight="600" fill="#ca8a04">Immutable</text>
    </svg>
  );
}

// ── Collections ──────────────────────────────────────────────────────────────

export function CollectionsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {["List", "Set", "Map", "Queue"].map((label, i) => {
        const colors = ["#eef2ff:#6366f1:#4f46e5", "#f0fdf4:#22c55e:#16a34a", "#fef9c3:#eab308:#ca8a04", "#fdf2f8:#ec4899:#db2777"];
        const [bg, border, text] = colors[i].split(":");
        return (
          <g key={label}>
            <rect x={4 + i * 44} y="20" width="40" height="28" rx="4"
              fill={bg} stroke={border} strokeWidth="1.2" />
            <text x={24 + i * 44} y="38" textAnchor="middle" fontSize="9" fontWeight="700" fill={text}>{label}</text>
          </g>
        );
      })}
      <text x="90" y="12" textAnchor="middle" fontSize="8" fill="#9ca3af">Java Collections</text>
    </svg>
  );
}

export function HashMapViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* key → hash fn → bucket */}
      <rect x="4" y="24" width="38" height="20" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="23" y="37" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">"key"</text>
      <rect x="58" y="22" width="46" height="24" rx="3" fill="#6366f1" />
      <text x="81" y="36" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">hash()</text>
      {/* arrow */}
      <path d="M42 34 L58 34" stroke="#6366f1" strokeWidth="1.5" markerEnd="url(#arrow-j)" />
      {/* buckets */}
      {[0, 1, 2].map(i => (
        <rect key={i} x="120" y={12 + i * 18} width="56" height="14" rx="2"
          fill={i === 1 ? "#eef2ff" : "#f9fafb"} stroke="#d1d5db" strokeWidth="0.8" />
      ))}
      <text x="148" y="22" textAnchor="middle" fontSize="7" fill="#9ca3af">bucket 0</text>
      <text x="148" y="40" textAnchor="middle" fontSize="7" fill="#4f46e5" fontWeight="600">bucket 1 ✓</text>
      <text x="148" y="58" textAnchor="middle" fontSize="7" fill="#9ca3af">bucket 2</text>
      <path d="M104 34 L120 34" stroke="#6366f1" strokeWidth="1.5" />
      <defs>
        <marker id="arrow-j" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

// ── Modern Java ───────────────────────────────────────────────────────────────

export function LambdaViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-gray-500">// before</span>
      <br />
      <span className="text-purple-400">Runnable </span>
      <span className="text-blue-300">r</span>
      <span className="text-gray-300"> = </span>
      <span className="text-purple-400">new </span>
      <span className="text-yellow-300">Runnable</span>
      <span className="text-gray-300">() …</span>
      <br />
      <br />
      <span className="text-gray-500">// lambda</span>
      <br />
      <span className="text-purple-400">Runnable </span>
      <span className="text-blue-300">r</span>
      <span className="text-gray-300"> = () </span>
      <span className="text-purple-400">-&gt; </span>
      <span className="text-green-300">print</span>
      <span className="text-gray-300">();</span>
    </div>
  );
}

export function StreamApiViz() {
  const steps = ["source", "filter", "map", "collect"];
  const colors = ["#6b7280", "#6366f1", "#0ea5e9", "#22c55e"];
  return (
    <svg viewBox="0 0 180 60" className="w-full" style={{ maxHeight: 60 }}>
      {steps.map((s, i) => (
        <g key={s}>
          <rect x={4 + i * 44} y="18" width="40" height="22" rx="4"
            fill={`${colors[i]}22`} stroke={colors[i]} strokeWidth="1.2" />
          <text x={24 + i * 44} y="32" textAnchor="middle" fontSize="8" fontWeight="600" fill={colors[i]}>{s}</text>
          {i < 3 && (
            <path d={`M${44 + i * 44} 29 L${48 + i * 44} 29`} stroke={colors[i + 1]} strokeWidth="1.5"
              markerEnd={`url(#arrs${i})`} />
          )}
          <defs>
            <marker id={`arrs${i}`} markerWidth="5" markerHeight="5" refX="4" refY="2.5" orient="auto">
              <path d="M0,0 L5,2.5 L0,5 Z" fill={colors[i + 1] ?? colors[i]} />
            </marker>
          </defs>
        </g>
      ))}
      <text x="90" y="54" textAnchor="middle" fontSize="7" fill="#9ca3af">Stream pipeline</text>
    </svg>
  );
}

// ── Concurrency ──────────────────────────────────────────────────────────────

export function ThreadsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Executor box */}
      <rect x="60" y="2" width="60" height="20" rx="4" fill="#6366f1" />
      <text x="90" y="15" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">Executor</text>
      {/* Thread boxes */}
      {[0, 1, 2].map(i => (
        <g key={i}>
          <rect x={10 + i * 58} y="36" width="52" height="18" rx="4"
            fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
          <text x={36 + i * 58} y="48" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">
            Thread {i + 1}
          </text>
          <line x1={36 + i * 58} y1="36" x2="90" y2="22" stroke="#6366f1" strokeWidth="1" strokeDasharray="2,1.5" />
        </g>
      ))}
    </svg>
  );
}

export function JvmViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* ClassLoader */}
      <rect x="4" y="4" width="50" height="20" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="29" y="16" textAnchor="middle" fontSize="7.5" fontWeight="600" fill="#4f46e5">ClassLoader</text>
      {/* Runtime Data Areas */}
      <rect x="62" y="4" width="58" height="20" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="91" y="16" textAnchor="middle" fontSize="7" fontWeight="600" fill="#4f46e5">Runtime Areas</text>
      {/* Execution Engine */}
      <rect x="128" y="4" width="48" height="20" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="152" y="16" textAnchor="middle" fontSize="7" fontWeight="600" fill="#16a34a">JIT / GC</text>
      {/* Heap / Stack / Metaspace */}
      {["Heap", "Stack", "Metaspace"].map((label, i) => (
        <g key={label}>
          <rect x={4 + i * 60} y="34" width="52" height="28" rx="3"
            fill={["#fef9c3", "#f0fdf4", "#fdf2f8"][i]}
            stroke={["#eab308", "#22c55e", "#ec4899"][i]} strokeWidth="1" />
          <text x={30 + i * 60} y="52" textAnchor="middle" fontSize="8" fontWeight="600"
            fill={["#ca8a04", "#16a34a", "#db2777"][i]}>{label}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Design Patterns ───────────────────────────────────────────────────────────

export function DesignPatternsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* interface / abstract */}
      <rect x="60" y="4" width="60" height="18" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">«interface»</text>
      {/* Factory */}
      <rect x="4" y="36" width="52" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="30" y="48" textAnchor="middle" fontSize="8" fontWeight="600" fill="#ca8a04">Factory</text>
      {/* Product */}
      <rect x="124" y="36" width="52" height="18" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="150" y="48" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">Product</text>
      {/* arrows */}
      <path d="M30 36 L75 22" stroke="#7c3aed" strokeWidth="1" strokeDasharray="2,2" />
      <path d="M150 36 L108 22" stroke="#7c3aed" strokeWidth="1" strokeDasharray="2,2" />
      <path d="M56 45 L124 45" stroke="#eab308" strokeWidth="1.2" markerEnd="url(#arrow-dp)" />
      <defs>
        <marker id="arrow-dp" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#eab308" />
        </marker>
      </defs>
    </svg>
  );
}

// ── Spring ────────────────────────────────────────────────────────────────────

export function SpringViz() {
  const layers = ["Controller", "Service", "Repository", "Database"];
  const colors = ["#6366f1", "#0ea5e9", "#22c55e", "#f59e0b"];
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {layers.map((label, i) => (
        <g key={label}>
          <rect x={24 + i * 0} y={4 + i * 16} width={132 - i * 0} height="14" rx="3"
            fill={`${colors[i]}22`} stroke={colors[i]} strokeWidth="1.2" />
          <text x="90" y={14 + i * 16} textAnchor="middle" fontSize="8" fontWeight="600" fill={colors[i]}>
            {label}
          </text>
          {i < 3 && (
            <line x1="90" y1={18 + i * 16} x2="90" y2={20 + i * 16} stroke={colors[i + 1]} strokeWidth="1.2" />
          )}
        </g>
      ))}
    </svg>
  );
}

// ── Testing ───────────────────────────────────────────────────────────────────

export function TestingViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-400">@Test</span>
      <br />
      <span className="text-purple-400">void </span>
      <span className="text-yellow-300">testAdd</span>
      <span className="text-gray-300">() {"{"}</span>
      <br />
      <span className="text-blue-300">  int </span>
      <span className="text-gray-300">result = </span>
      <span className="text-yellow-300">add</span>
      <span className="text-gray-300">(2, 3);</span>
      <br />
      <span className="text-green-400">  assertEquals</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">5</span>
      <span className="text-gray-300">, result);</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

// ── Database / JPA ────────────────────────────────────────────────────────────

export function JpaViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Entity */}
      <rect x="4" y="4" width="60" height="56" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="34" y="16" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#4f46e5">@Entity</text>
      <line x1="8" y1="20" x2="60" y2="20" stroke="#6366f1" strokeWidth="0.5" />
      <text x="34" y="31" textAnchor="middle" fontSize="7" fill="#374151">@Id id</text>
      <text x="34" y="42" textAnchor="middle" fontSize="7" fill="#374151">name</text>
      <text x="34" y="53" textAnchor="middle" fontSize="7" fill="#374151">email</text>
      {/* Arrow */}
      <path d="M64 32 L90 32" stroke="#6366f1" strokeWidth="1.5" markerEnd="url(#arrow-jpa)" />
      <text x="77" y="28" textAnchor="middle" fontSize="7" fill="#9ca3af">JPA</text>
      {/* DB Table */}
      <rect x="90" y="4" width="86" height="56" rx="4" fill="#f9fafb" stroke="#d1d5db" strokeWidth="1.2" />
      <rect x="90" y="4" width="86" height="18" rx="4" fill="#6366f1" />
      <text x="133" y="16" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">users TABLE</text>
      {["id | name | email", "1  | Alice | …", "2  | Bob   | …"].map((row, i) => (
        <text key={i} x="96" y={33 + i * 11} fontSize="6.5" fill={i === 0 ? "#6b7280" : "#374151"}
          fontFamily="monospace">{row}</text>
      ))}
      <defs>
        <marker id="arrow-jpa" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

export function ConcurrencyViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Shared resource */}
      <rect x="64" y="28" width="52" height="20" rx="4" fill="#fef2f2" stroke="#ef4444" strokeWidth="1.5" />
      <text x="90" y="41" textAnchor="middle" fontSize="8" fontWeight="700" fill="#dc2626">Shared State</text>
      {/* Thread 1 */}
      <rect x="4" y="4" width="50" height="18" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="29" y="16" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Thread 1</text>
      <path d="M54 13 C60 13 64 28 78 33" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="2,2" />
      {/* Thread 2 */}
      <rect x="126" y="4" width="50" height="18" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="151" y="16" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Thread 2</text>
      <path d="M126 13 C120 13 116 28 102 33" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="2,2" />
      {/* synchronized label */}
      <text x="90" y="62" textAnchor="middle" fontSize="7" fill="#9ca3af">synchronized block</text>
    </svg>
  );
}

export const JAVA_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "java-fundamentals":       JavaFundamentalsViz,
  "methods-program-structure": MethodsViz,
  "oop":                     OopViz,
  "interfaces-abstract":     InterfacesViz,
  "exception-handling":      ExceptionHandlingViz,
  "strings-immutability":    StringsViz,
  "collections-framework":   CollectionsViz,
  "arraylist-linkedlist":    CollectionsViz,
  "hashmap-hashset":         HashMapViz,
  "treemap-treeset":         HashMapViz,
  "queues-deques-pq":        CollectionsViz,
  "collections-performance": CollectionsViz,
  "generics-fundamentals":   InterfacesViz,
  "wildcards-bounded":       InterfacesViz,
  "generic-methods-erasure": InterfacesViz,
  "lambda-expressions":      LambdaViz,
  "functional-interfaces":   LambdaViz,
  "stream-api":              StreamApiViz,
  "stream-operations":       StreamApiViz,
  "optional":                LambdaViz,
  "date-time-api":           LambdaViz,
  "modern-java-features":    StreamApiViz,
  "threads-fundamentals":    ThreadsViz,
  "synchronization":         ConcurrencyViz,
  "locks-atomic":            ConcurrencyViz,
  "executor-framework":      ThreadsViz,
  "completable-future":      StreamApiViz,
  "concurrent-collections":  CollectionsViz,
  "java-memory-model":       JvmViz,
  "race-conditions-deadlocks": ConcurrencyViz,
  "virtual-threads":         ThreadsViz,
  "jvm-architecture":        JvmViz,
  "heap-stack":              JvmViz,
  "garbage-collection":      JvmViz,
  "g1gc-zgc":                JvmViz,
  "jit-compilation":         JvmViz,
  "jvm-diagnostics":         JvmViz,
  "reflection":              InterfacesViz,
  "annotations":             LambdaViz,
  "modules":                 InterfacesViz,
  "nio-networking":          StreamApiViz,
  "serialization":           JpaViz,
  "solid-principles":        DesignPatternsViz,
  "creational-patterns":     DesignPatternsViz,
  "structural-patterns":     DesignPatternsViz,
  "behavioral-patterns":     DesignPatternsViz,
  "spring-fundamentals":     SpringViz,
  "dependency-injection":    SpringViz,
  "spring-boot":             SpringViz,
  "spring-mvc-rest":         SpringViz,
  "spring-data-jpa":         JpaViz,
  "spring-security":         SpringViz,
  "spring-transactions":     JpaViz,
  "junit5":                  TestingViz,
  "mockito":                 TestingViz,
  "integration-testing":     TestingViz,
  "testcontainers":          TestingViz,
  "testing-strategy":        TestingViz,
  "jdbc":                    JpaViz,
  "jpa-hibernate":           JpaViz,
  "hibernate-performance":   JpaViz,
  "caching-logging":         SpringViz,
};
