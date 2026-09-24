import React from "react";

interface MiniVizProps { animate?: boolean }

// ── Go Basics ────────────────────────────────────────────────────────────────

export function GoFundamentalsViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">package </span>
      <span className="text-blue-300">main</span>
      <br />
      <span className="text-purple-400">import </span>
      <span className="text-green-300">"fmt"</span>
      <br />
      <span className="text-purple-400">func </span>
      <span className="text-yellow-300">main</span>
      <span className="text-gray-300">() {"{"}</span>
      <br />
      <span className="text-blue-300">  fmt</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">Println</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">"Go!"</span>
      <span className="text-gray-300">)</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function GoToolchainViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">go build</span>
      <span className="text-gray-300"> ./...</span>
      <br />
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">go test</span>
      <span className="text-gray-300"> ./...</span>
      <br />
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">go mod tidy</span>
      <br />
      <span className="text-gray-500">// module ready</span>
    </div>
  );
}

export function PointersViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="8" y="22" width="60" height="28" rx="4" fill="#1e1e2e" stroke="#6366f1" strokeWidth="1.2" />
      <text x="38" y="34" textAnchor="middle" fontSize="8" fill="#a5b4fc" fontFamily="monospace">x</text>
      <text x="38" y="44" textAnchor="middle" fontSize="9" fontWeight="700" fill="#e2e8f0" fontFamily="monospace">42</text>
      <rect x="112" y="22" width="62" height="28" rx="4" fill="#1e1e2e" stroke="#22c55e" strokeWidth="1.2" />
      <text x="143" y="34" textAnchor="middle" fontSize="8" fill="#86efac" fontFamily="monospace">&amp;x</text>
      <text x="143" y="44" textAnchor="middle" fontSize="8" fontWeight="700" fill="#e2e8f0" fontFamily="monospace">0xc000</text>
      <line x1="68" y1="36" x2="112" y2="36" stroke="#6366f1" strokeWidth="1.2" markerEnd="url(#arrow-go)" />
      <defs>
        <marker id="arrow-go" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
      <text x="90" y="20" textAnchor="middle" fontSize="8" fill="#94a3b8">pointer</text>
    </svg>
  );
}

export function StructsViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">type </span>
      <span className="text-blue-300">User </span>
      <span className="text-purple-400">struct </span>
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-gray-300">  Name </span>
      <span className="text-yellow-300">string</span>
      <br />
      <span className="text-gray-300">  Age  </span>
      <span className="text-yellow-300">int</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
      <br />
      <span className="text-blue-300">u </span>
      <span className="text-gray-300">:= </span>
      <span className="text-blue-300">User</span>
      <span className="text-gray-300">{"{"}</span>
      <span className="text-green-300">"Ana"</span>
      <span className="text-gray-300">, </span>
      <span className="text-green-300">30</span>
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function InterfacesViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="50" y="2" width="80" height="26" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="11" textAnchor="middle" fontSize="7" fill="#7c3aed" fontStyle="italic">«interface»</text>
      <text x="90" y="22" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">io.Writer</text>
      <rect x="8" y="46" width="68" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="42" y="61" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">os.File</text>
      <rect x="104" y="46" width="70" height="22" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="139" y="61" textAnchor="middle" fontSize="9" fontWeight="600" fill="#ca8a04">bytes.Buffer</text>
      <line x1="42" y1="46" x2="76" y2="28" stroke="#7c3aed" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="139" y1="46" x2="104" y2="28" stroke="#7c3aed" strokeWidth="1" strokeDasharray="3,2" />
    </svg>
  );
}

export function ErrorHandlingViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-blue-300">val</span>
      <span className="text-gray-300">, </span>
      <span className="text-red-400">err </span>
      <span className="text-gray-300">:= </span>
      <span className="text-yellow-300">doWork</span>
      <span className="text-gray-300">()</span>
      <br />
      <span className="text-purple-400">if </span>
      <span className="text-red-400">err </span>
      <span className="text-gray-300">!= </span>
      <span className="text-blue-300">nil </span>
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-purple-400">  return </span>
      <span className="text-red-400">err</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
      <br />
      <span className="text-gray-500">// use val</span>
    </div>
  );
}

// ── Collections ──────────────────────────────────────────────────────────────

export function SlicesViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <text x="8" y="14" fontSize="8" fill="#94a3b8" fontFamily="monospace">len=3  cap=5</text>
      {/* backing array cells */}
      {[0,1,2,3,4].map(i => (
        <rect key={i} x={8 + i*34} y={22} width={30} height={24} rx="3"
          fill={i < 3 ? "#eef2ff" : "#f8fafc"}
          stroke={i < 3 ? "#6366f1" : "#cbd5e1"}
          strokeWidth="1.2" />
      ))}
      {["10","20","30","",""].map((v, i) => (
        <text key={i} x={23 + i*34} y={38} textAnchor="middle" fontSize="9"
          fontWeight={i < 3 ? "700" : "400"}
          fill={i < 3 ? "#4f46e5" : "#cbd5e1"}
          fontFamily="monospace">{v}</text>
      ))}
      <text x="8" y="60" fontSize="7" fill="#6366f1" fontFamily="monospace">← slice →</text>
      <text x="100" y="60" fontSize="7" fill="#94a3b8" fontFamily="monospace">extra cap</text>
    </svg>
  );
}

export function MapViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {/* keys */}
      {["name","age","city"].map((k,i) => (
        <g key={k}>
          <rect x="4" y={4+i*22} width="34" height="16" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
          <text x="21" y={15+i*22} textAnchor="middle" fontSize="8" fill="#92400e" fontFamily="monospace">{k}</text>
        </g>
      ))}
      {/* hash box */}
      <rect x="52" y="22" width="36" height="28" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="70" y="33" textAnchor="middle" fontSize="7" fill="#7c3aed">hash</text>
      <text x="70" y="44" textAnchor="middle" fontSize="8" fill="#4f46e5" fontWeight="700">(k)</text>
      {/* buckets */}
      {[0,1,2].map(i => (
        <g key={i}>
          <rect x="106" y={4+i*22} width="36" height="16" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
          <text x="124" y={15+i*22} textAnchor="middle" fontSize="8" fill="#16a34a" fontFamily="monospace">b{i}</text>
        </g>
      ))}
      {/* arrows keys→hash */}
      {[0,1,2].map(i => (
        <line key={i} x1="38" y1={12+i*22} x2="52" y2="36" stroke="#eab308" strokeWidth="0.8" opacity="0.6" />
      ))}
      {/* arrows hash→buckets */}
      {[0,1,2].map(i => (
        <line key={i} x1="88" y1="36" x2="106" y2={12+i*22} stroke="#22c55e" strokeWidth="0.8" opacity="0.6" />
      ))}
      <text x="148" y="14" fontSize="7" fill="#94a3b8">bucket</text>
    </svg>
  );
}

// ── Concurrency ──────────────────────────────────────────────────────────────

export function GoroutinesViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="65" y="2" width="50" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">main</text>
      {["G1","G2","G3"].map((g,i) => (
        <g key={g}>
          <rect x={8+i*58} y="46" width="44" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
          <text x={30+i*58} y="60" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">{g}</text>
          <line x1={30+i*58} y1="46" x2="90" y2="22" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
        </g>
      ))}
      <text x="90" y="40" textAnchor="middle" fontSize="7" fill="#94a3b8">go func()</text>
    </svg>
  );
}

export function ChannelsViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="48" height="24" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="28" y="40" textAnchor="middle" fontSize="9" fontWeight="600" fill="#4f46e5">Producer</text>
      {/* channel pipe */}
      <rect x="66" y="28" width="48" height="16" rx="3" fill="#1e1e2e" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="40" textAnchor="middle" fontSize="8" fill="#a5b4fc" fontFamily="monospace">ch</text>
      <line x1="52" y1="36" x2="66" y2="36" stroke="#6366f1" strokeWidth="1.5" markerEnd="url(#arr-ch)" />
      <rect x="128" y="24" width="48" height="24" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="152" y="40" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">Consumer</text>
      <line x1="114" y1="36" x2="128" y2="36" stroke="#22c55e" strokeWidth="1.5" markerEnd="url(#arr-ch2)" />
      <defs>
        <marker id="arr-ch" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
        <marker id="arr-ch2" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#22c55e" />
        </marker>
      </defs>
    </svg>
  );
}

export function BufferedChannelsViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="42" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="25" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Producer</text>
      {[0,1,2].map(i => (
        <rect key={i} x={60+i*22} y="24" width="18" height="22" rx="2"
          fill={i < 2 ? "#eef2ff" : "#f8fafc"}
          stroke={i < 2 ? "#6366f1" : "#cbd5e1"}
          strokeWidth="1" />
      ))}
      <text x="90" y="38" textAnchor="middle" fontSize="7" fill="#94a3b8">buf</text>
      <rect x="134" y="24" width="42" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="155" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">Consumer</text>
      <line x1="46" y1="35" x2="60" y2="35" stroke="#6366f1" strokeWidth="1.2" />
      <line x1="126" y1="35" x2="134" y2="35" stroke="#22c55e" strokeWidth="1.2" />
    </svg>
  );
}

export function SelectViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">select </span>
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-purple-400">case </span>
      <span className="text-blue-300">v </span>
      <span className="text-gray-300">:= &lt;-</span>
      <span className="text-yellow-300">ch1</span>
      <span className="text-gray-300">:</span>
      <br />
      <span className="text-gray-500">  // handle v</span>
      <br />
      <span className="text-purple-400">case </span>
      <span className="text-gray-300">&lt;-</span>
      <span className="text-red-400">timeout</span>
      <span className="text-gray-300">:</span>
      <br />
      <span className="text-purple-400">  return</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function MutexViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="4" width="52" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="30" y="18" textAnchor="middle" fontSize="9" fontWeight="600" fill="#4f46e5">Thread 1</text>
      <rect x="4" y="48" width="52" height="20" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="30" y="62" textAnchor="middle" fontSize="9" fontWeight="600" fill="#92400e">Thread 2</text>
      {/* shared state */}
      <rect x="110" y="22" width="62" height="28" rx="4" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1.2" />
      <text x="141" y="33" textAnchor="middle" fontSize="7" fill="#db2777">🔒 Mutex</text>
      <text x="141" y="44" textAnchor="middle" fontSize="8" fontWeight="700" fill="#be185d">State</text>
      <line x1="56" y1="14" x2="110" y2="30" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="56" y1="58" x2="110" y2="42" stroke="#eab308" strokeWidth="1" strokeDasharray="3,2" />
    </svg>
  );
}

export function WaitGroupViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">var </span>
      <span className="text-blue-300">wg </span>
      <span className="text-yellow-300">sync.WaitGroup</span>
      <br />
      <span className="text-blue-300">wg</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">Add</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">3</span>
      <span className="text-gray-300">)</span>
      <br />
      <span className="text-purple-400">go </span>
      <span className="text-purple-400">func</span>
      <span className="text-gray-300">() {"{ "}</span>
      <span className="text-blue-300">wg</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">Done</span>
      <span className="text-gray-300">{"() }()"}</span>
      <br />
      <span className="text-blue-300">wg</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">Wait</span>
      <span className="text-gray-300">()</span>
    </div>
  );
}

export function ContextViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="2" width="60" height="18" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="14" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">Request</text>
      <rect x="55" y="30" width="70" height="18" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="42" textAnchor="middle" fontSize="8" fontWeight="700" fill="#7c3aed">ctx + timeout</text>
      <line x1="90" y1="20" x2="90" y2="30" stroke="#6366f1" strokeWidth="1.2" />
      {["Svc A","Svc B","DB"].map((s,i) => (
        <g key={s}>
          <rect x={6+i*58} y="56" width="44" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
          <text x={28+i*58} y="66" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">{s}</text>
          <line x1={28+i*58} y1="56" x2="90" y2="48" stroke="#7c3aed" strokeWidth="0.8" strokeDasharray="2,2" />
        </g>
      ))}
    </svg>
  );
}

export function WorkerPoolViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="2" y="24" width="38" height="24" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="21" y="35" textAnchor="middle" fontSize="7" fill="#92400e">Jobs</text>
      <text x="21" y="44" textAnchor="middle" fontSize="7" fill="#92400e">Queue</text>
      {["W1","W2","W3"].map((w,i) => (
        <g key={w}>
          <rect x={52+i*36} y="24" width="28" height="24" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
          <text x={66+i*36} y="39" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">{w}</text>
          <line x1="40" y1="36" x2={52+i*36} y2="36" stroke="#eab308" strokeWidth="0.8" opacity="0.7" />
        </g>
      ))}
      <rect x="158" y="24" width="20" height="24" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="168" y="39" textAnchor="middle" fontSize="7" fill="#16a34a">✓</text>
      <line x1="160" y1="36" x2="158" y2="36" stroke="#22c55e" strokeWidth="0.8" />
    </svg>
  );
}

export function PipelineViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {["Stage1","Stage2","Stage3"].map((s,i) => (
        <g key={s}>
          <rect x={4+i*60} y="22" width="48" height="28" rx="4"
            fill={i===0?"#eef2ff":i===1?"#ede9fe":"#f0fdf4"}
            stroke={i===0?"#6366f1":i===1?"#7c3aed":"#22c55e"}
            strokeWidth="1.2" />
          <text x={28+i*60} y="38" textAnchor="middle" fontSize="8" fontWeight="600"
            fill={i===0?"#4f46e5":i===1?"#7c3aed":"#16a34a"}>{s}</text>
          {i < 2 && (
            <>
              <rect x={56+i*60} y="32" width="6" height="8" rx="1" fill="#1e1e2e" stroke="#6366f1" strokeWidth="0.8" />
              <text x={59+i*60} y="39" textAnchor="middle" fontSize="6" fill="#a5b4fc" fontFamily="monospace">ch</text>
              <line x1={52+i*60} y1="36" x2={56+i*60} y2="36" stroke="#6366f1" strokeWidth="1" />
              <line x1={62+i*60} y1="36" x2={64+i*60} y2="36" stroke="#6366f1" strokeWidth="1" />
            </>
          )}
        </g>
      ))}
    </svg>
  );
}

export function FanInOutViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="36" height="22" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="22" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#92400e">Work</text>
      {["G1","G2","G3"].map((g,i) => (
        <g key={g}>
          <rect x={52} y={4+i*22} width="28" height="16" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
          <text x="66" y={15+i*22} textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">{g}</text>
          <line x1="40" y1="35" x2="52" y2={12+i*22} stroke="#eab308" strokeWidth="0.8" opacity="0.7" />
          <line x1="80" y1={12+i*22} x2="100" y2="35" stroke="#22c55e" strokeWidth="0.8" opacity="0.7" />
        </g>
      ))}
      <rect x="100" y="24" width="36" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="118" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#7c3aed">Merge</text>
      <line x1="136" y1="35" x2="150" y2="35" stroke="#7c3aed" strokeWidth="1.2" />
      <rect x="150" y="24" width="28" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="164" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">Out</text>
    </svg>
  );
}

// ── Standard Library / Backend ────────────────────────────────────────────────

export function HttpServerViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="2" y="24" width="30" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="17" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Client</text>
      <line x1="32" y1="35" x2="46" y2="35" stroke="#6366f1" strokeWidth="1.2" />
      <rect x="46" y="24" width="36" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="64" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#7c3aed">Router</text>
      <line x1="82" y1="35" x2="96" y2="35" stroke="#7c3aed" strokeWidth="1.2" />
      <rect x="96" y="10" width="38" height="18" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="115" y="22" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">Handler A</text>
      <rect x="96" y="44" width="38" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="115" y="56" textAnchor="middle" fontSize="8" fontWeight="600" fill="#92400e">Handler B</text>
      <line x1="82" y1="35" x2="96" y2="19" stroke="#7c3aed" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="82" y1="35" x2="96" y2="53" stroke="#7c3aed" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="134" y1="36" x2="154" y2="36" stroke="#22c55e" strokeWidth="1.2" />
      <rect x="154" y="24" width="24" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="166" y="38" textAnchor="middle" fontSize="7" fontWeight="600" fill="#16a34a">Resp</text>
    </svg>
  );
}

export function RestApiViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-400">GET  </span>
      <span className="text-blue-300">/users</span>
      <br />
      <span className="text-yellow-300">POST </span>
      <span className="text-blue-300">/users</span>
      <br />
      <span className="text-green-400">GET  </span>
      <span className="text-blue-300">/users/</span>
      <span className="text-purple-400">{"{"}</span>
      <span className="text-orange-300">id</span>
      <span className="text-purple-400">{"}"}</span>
      <br />
      <span className="text-red-400">DEL  </span>
      <span className="text-blue-300">/users/</span>
      <span className="text-purple-400">{"{"}</span>
      <span className="text-orange-300">id</span>
      <span className="text-purple-400">{"}"}</span>
    </div>
  );
}

export function GrpcViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="36" height="24" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="22" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Client</text>
      <rect x="58" y="24" width="46" height="24" rx="4" fill="#1e1e2e" stroke="#6366f1" strokeWidth="1.2" />
      <text x="81" y="33" textAnchor="middle" fontSize="7" fill="#a5b4fc" fontFamily="monospace">Protobuf</text>
      <text x="81" y="43" textAnchor="middle" fontSize="7" fill="#6366f1">gRPC</text>
      <rect x="122" y="14" width="52" height="20" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="148" y="27" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">gRPC Server</text>
      <rect x="122" y="42" width="52" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="148" y="54" textAnchor="middle" fontSize="8" fontWeight="600" fill="#92400e">Service</text>
      <line x1="40" y1="36" x2="58" y2="36" stroke="#6366f1" strokeWidth="1.2" />
      <line x1="104" y1="36" x2="122" y2="24" stroke="#22c55e" strokeWidth="1" strokeDasharray="2,2" />
      <line x1="104" y1="36" x2="122" y2="51" stroke="#eab308" strokeWidth="1" strokeDasharray="2,2" />
    </svg>
  );
}

// ── Advanced ─────────────────────────────────────────────────────────────────

export function GoRuntimeViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <text x="8" y="12" fontSize="8" fill="#94a3b8" fontFamily="monospace">GOMAXPROCS=4</text>
      {[0,1,2,3].map(i => (
        <g key={i}>
          <rect x={4+i*44} y="18" width="38" height="48" rx="4"
            fill="#1e1e2e" stroke="#6366f1" strokeWidth="1.2" />
          <text x={23+i*44} y="30" textAnchor="middle" fontSize="7" fill="#a5b4fc" fontFamily="monospace">P{i}</text>
          <circle cx={23+i*44} cy="42" r="6" fill="#4f46e5" />
          <text x={23+i*44} y="45" textAnchor="middle" fontSize="6" fill="white" fontWeight="700">M</text>
          <circle cx={23+i*44} cy="56" r="5" fill="#22c55e" />
          <text x={23+i*44} y="59" textAnchor="middle" fontSize="6" fill="white" fontWeight="700">G</text>
        </g>
      ))}
    </svg>
  );
}

export function GcViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="8" y="8" width="58" height="24" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="37" y="23" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Stack</text>
      <rect x="8" y="42" width="58" height="24" rx="4" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1.2" />
      <text x="37" y="57" textAnchor="middle" fontSize="9" fontWeight="700" fill="#be185d">Heap</text>
      {/* GC sweep arrow */}
      <path d="M80,20 Q110,36 80,52" stroke="#f59e0b" strokeWidth="1.5" fill="none" strokeDasharray="3,2" />
      <text x="120" y="30" fontSize="8" fill="#f59e0b" fontWeight="700">GC</text>
      <text x="108" y="42" fontSize="7" fill="#f59e0b">sweep</text>
      {/* live/dead objects */}
      <circle cx="140" cy="30" r="6" fill="#22c55e" opacity="0.8" />
      <text x="152" y="33" fontSize="7" fill="#22c55e">live</text>
      <circle cx="140" cy="48" r="6" fill="#ef4444" opacity="0.5" />
      <text x="152" y="51" fontSize="7" fill="#ef4444">freed</text>
    </svg>
  );
}

export function ProfilingViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">go test -bench</span>
      <span className="text-gray-300">=.</span>
      <br />
      <span className="text-blue-300">BenchmarkSort</span>
      <span className="text-gray-300"> 1000000</span>
      <br />
      <span className="text-green-300">1200 ns/op</span>
      <br />
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">go tool pprof</span>
      <span className="text-gray-300"> cpu.prof</span>
    </div>
  );
}

export function DockerGoViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">FROM </span>
      <span className="text-blue-300">golang:1.22 </span>
      <span className="text-yellow-300">AS build</span>
      <br />
      <span className="text-purple-400">RUN </span>
      <span className="text-gray-300">go build -o app</span>
      <br />
      <span className="text-gray-500">---</span>
      <br />
      <span className="text-purple-400">FROM </span>
      <span className="text-green-300">scratch</span>
      <br />
      <span className="text-purple-400">COPY </span>
      <span className="text-gray-300">--from=build /app .</span>
    </div>
  );
}

export function ObservabilityViz(_props: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="38" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="23" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Request</text>
      <line x1="42" y1="35" x2="58" y2="35" stroke="#6366f1" strokeWidth="1.2" />
      <rect x="58" y="24" width="34" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="75" y="38" textAnchor="middle" fontSize="8" fontWeight="600" fill="#7c3aed">Trace</text>
      {["Metrics","Logs","Spans"].map((l,i) => (
        <g key={l}>
          <rect x={100+i*24} y="20" width="22" height="14" rx="2"
            fill={i===0?"#fef9c3":i===1?"#f0fdf4":"#fdf2f8"}
            stroke={i===0?"#eab308":i===1?"#22c55e":"#ec4899"}
            strokeWidth="1" />
          <text x={111+i*24} y="30" textAnchor="middle" fontSize="6" fontWeight="600"
            fill={i===0?"#92400e":i===1?"#16a34a":"#be185d"}>{l}</text>
          <line x1="92" y1="35" x2={111+i*24} y2="34" stroke="#7c3aed" strokeWidth="0.8" strokeDasharray="2,2" />
        </g>
      ))}
    </svg>
  );
}

export function TestingGoViz(_props: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">func </span>
      <span className="text-yellow-300">TestAdd</span>
      <span className="text-gray-300">(t *</span>
      <span className="text-blue-300">testing.T</span>
      <span className="text-gray-300">) {"{"}</span>
      <br />
      <span className="text-blue-300">  got </span>
      <span className="text-gray-300">:= </span>
      <span className="text-yellow-300">Add</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">1</span>
      <span className="text-gray-300">,</span>
      <span className="text-green-300">2</span>
      <span className="text-gray-300">)</span>
      <br />
      <span className="text-blue-300">  assert</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">Equal</span>
      <span className="text-gray-300">(t, </span>
      <span className="text-green-300">3</span>
      <span className="text-gray-300">, got)</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

// ── Visualization Map ─────────────────────────────────────────────────────────

export const GO_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "go-fundamentals":        GoFundamentalsViz,
  "go-toolchain":           GoToolchainViz,
  "variables-constants":    GoFundamentalsViz,
  "go-types":               InterfacesViz,
  "functions":              GoFundamentalsViz,
  "multiple-return-values": ErrorHandlingViz,
  "pointers":               PointersViz,
  "structs":                StructsViz,
  "interfaces":             InterfacesViz,
  "packages-modules":       GoToolchainViz,
  "error-handling":         ErrorHandlingViz,
  "defer-panic-recover":    ErrorHandlingViz,
  "arrays-slices":          SlicesViz,
  "slice-internals":        SlicesViz,
  "maps":                   MapViz,
  "strings-runes":          GoFundamentalsViz,
  "struct-design":          StructsViz,
  "json":                   StructsViz,
  "goroutines":             GoroutinesViz,
  "channels":               ChannelsViz,
  "buffered-channels":      BufferedChannelsViz,
  "select":                 SelectViz,
  "mutex-rwmutex":          MutexViz,
  "waitgroups":             WaitGroupViz,
  "context":                ContextViz,
  "worker-pools":           WorkerPoolViz,
  "pipelines":              PipelineViz,
  "fan-in-fan-out":         FanInOutViz,
  "concurrent-patterns":    FanInOutViz,
  "race-conditions":        MutexViz,
  "io-file-handling":       GoFundamentalsViz,
  "http-client":            HttpServerViz,
  "http-server":            HttpServerViz,
  "json-apis":              RestApiViz,
  "time":                   GoFundamentalsViz,
  "logging":                GoFundamentalsViz,
  "reflection":             GoRuntimeViz,
  "rest-apis":              RestApiViz,
  "http-middleware":        HttpServerViz,
  "authentication":         HttpServerViz,
  "postgresql-go":          HttpServerViz,
  "redis-go":               HttpServerViz,
  "grpc-go":                GrpcViz,
  "microservices-go":       GrpcViz,
  "go-runtime":             GoRuntimeViz,
  "garbage-collection-go":  GcViz,
  "memory-management-go":   GcViz,
  "profiling-go":           ProfilingViz,
  "testing-go":             TestingGoViz,
  "go-docker":              DockerGoViz,
  "go-kubernetes":          DockerGoViz,
  "go-observability":       ObservabilityViz,
  "distributed-systems-go": GrpcViz,
};
