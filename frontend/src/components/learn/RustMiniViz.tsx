import React from "react";

interface MiniVizProps { animate?: boolean }

// ── Rust Basics ──────────────────────────────────────────────────────────────

export function RustFundamentalsViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">fn </span>
      <span className="text-yellow-300">main</span>
      <span className="text-gray-300">() {"{"}</span>
      <br />
      <span className="text-orange-400">  let </span>
      <span className="text-blue-300">x</span>
      <span className="text-gray-300">: </span>
      <span className="text-purple-400">i32</span>
      <span className="text-gray-300"> = </span>
      <span className="text-green-300">42</span>
      <span className="text-gray-300">;</span>
      <br />
      <span className="text-yellow-300">  println!</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">&quot;{"{}"}&quot;</span>
      <span className="text-gray-300">, x);</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function CargoViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">cargo</span>
      <span className="text-gray-300"> new my_app</span>
      <br />
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">cargo</span>
      <span className="text-gray-300"> build --release</span>
      <br />
      <span className="text-green-400">$ </span>
      <span className="text-yellow-300">cargo</span>
      <span className="text-gray-300"> test</span>
    </div>
  );
}

// ── Ownership ────────────────────────────────────────────────────────────────

export function OwnershipViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="2" width="60" height="20" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="9" fontWeight="700" fill="#92400e">Value</text>
      <line x1="90" y1="22" x2="90" y2="32" stroke="#6b7280" strokeWidth="1.2" markerEnd="url(#arr)" />
      <rect x="60" y="32" width="60" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="46" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Owner</text>
      <line x1="90" y1="52" x2="90" y2="60" stroke="#6b7280" strokeWidth="1.2" />
      <text x="90" y="70" textAnchor="middle" fontSize="8" fill="#ef4444">drop() ← scope ends</text>
      <defs>
        <marker id="arr" markerWidth="6" markerHeight="6" refX="3" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6b7280" />
        </marker>
      </defs>
    </svg>
  );
}

export function MoveSemanticsViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="20" width="60" height="22" rx="4" fill="#fee2e2" stroke="#ef4444" strokeWidth="1.2" />
      <text x="34" y="30" textAnchor="middle" fontSize="8" fill="#b91c1c">s1: String</text>
      <text x="34" y="40" textAnchor="middle" fontSize="9" fill="#ef4444">✗ moved</text>
      <text x="86" y="35" textAnchor="middle" fontSize="9" fontWeight="700" fill="#6366f1">move →</text>
      <rect x="116" y="20" width="60" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="146" y="30" textAnchor="middle" fontSize="8" fill="#166534">s2: String</text>
      <text x="146" y="40" textAnchor="middle" fontSize="8" fill="#16a34a">owns data</text>
    </svg>
  );
}

export function BorrowingViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="2" width="60" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="17" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Owner</text>
      <line x1="34" y1="50" x2="68" y2="24" stroke="#22c55e" strokeWidth="1.2" strokeDasharray="3,2" />
      <line x1="146" y1="50" x2="112" y2="24" stroke="#22c55e" strokeWidth="1.2" strokeDasharray="3,2" />
      <rect x="4" y="50" width="60" height="18" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="34" y="62" textAnchor="middle" fontSize="8" fill="#16a34a">&amp;ref1 (read)</text>
      <rect x="116" y="50" width="60" height="18" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="146" y="62" textAnchor="middle" fontSize="8" fill="#16a34a">&amp;ref2 (read)</text>
    </svg>
  );
}

export function MutableRefViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="2" width="60" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="17" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Owner</text>
      <line x1="90" y1="24" x2="90" y2="46" stroke="#f59e0b" strokeWidth="1.5" />
      <rect x="55" y="46" width="70" height="18" rx="3" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="58" textAnchor="middle" fontSize="8" fontWeight="600" fill="#92400e">&amp;mut ref (ONE only)</text>
      <text x="20" y="36" textAnchor="middle" fontSize="9" fill="#ef4444">✗</text>
      <text x="160" y="36" textAnchor="middle" fontSize="9" fill="#ef4444">✗</text>
      <text x="20" y="46" textAnchor="middle" fontSize="7" fill="#9ca3af">blocked</text>
      <text x="160" y="46" textAnchor="middle" fontSize="7" fill="#9ca3af">blocked</text>
    </svg>
  );
}

export function LifetimesViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <text x="8" y="22" fontSize="8" fill="#6b7280">object:</text>
      <text x="50" y="22" fontSize="8" fontWeight="700" fill="#6366f1">&apos;a</text>
      <rect x="66" y="14" width="108" height="10" rx="2" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="8" y="44" fontSize="8" fill="#6b7280">reference:</text>
      <text x="58" y="44" fontSize="8" fontWeight="700" fill="#f59e0b">&apos;b</text>
      <rect x="66" y="36" width="68" height="10" rx="2" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1" />
      <text x="90" y="62" textAnchor="middle" fontSize="8" fill="#16a34a">&apos;a outlives &apos;b ✓</text>
    </svg>
  );
}

export function SlicesViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="10" width="172" height="22" rx="4" fill="#f3f4f6" stroke="#9ca3af" strokeWidth="1" />
      <text x="90" y="25" textAnchor="middle" fontSize="9" fontFamily="monospace" fill="#374151">&quot;hello world&quot;</text>
      <rect x="74" y="10" width="76" height="22" rx="0" fill="none" stroke="#6366f1" strokeWidth="2" />
      <text x="112" y="52" textAnchor="middle" fontSize="8" fill="#4f46e5">[6..11] = &quot;world&quot;</text>
      <line x1="112" y1="32" x2="112" y2="46" stroke="#6366f1" strokeWidth="1" strokeDasharray="2,2" />
    </svg>
  );
}

export function SmartPointersViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="4" width="56" height="20" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="32" y="17" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4338ca">Stack</text>
      <line x1="60" y1="14" x2="76" y2="14" stroke="#6366f1" strokeWidth="1.2" markerEnd="url(#ptr)" />
      <rect x="76" y="4" width="60" height="20" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="106" y="17" textAnchor="middle" fontSize="8" fontWeight="700" fill="#92400e">Heap: Box&lt;T&gt;</text>
      <rect x="4" y="44" width="80" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="44" y="57" textAnchor="middle" fontSize="8" fill="#166534">Rc&lt;T&gt; refcount=2</text>
      <defs>
        <marker id="ptr" markerWidth="6" markerHeight="6" refX="3" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

// ── Traits ───────────────────────────────────────────────────────────────────

export function TraitsViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="50" y="2" width="80" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="10" textAnchor="middle" fontSize="7" fill="#7c3aed" fontStyle="italic">«trait»</text>
      <text x="90" y="20" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Display</text>
      <line x1="46" y1="44" x2="74" y2="24" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="134" y1="44" x2="106" y2="24" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <rect x="4" y="44" width="84" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="46" y="57" textAnchor="middle" fontSize="9" fontWeight="600" fill="#16a34a">impl for String</text>
      <rect x="96" y="44" width="80" height="20" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="136" y="57" textAnchor="middle" fontSize="9" fontWeight="600" fill="#ca8a04">impl for i32</text>
    </svg>
  );
}

export function GenericsViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">fn </span>
      <span className="text-yellow-300">largest</span>
      <span className="text-gray-300">&lt;</span>
      <span className="text-purple-400">T</span>
      <span className="text-gray-300">: </span>
      <span className="text-blue-300">PartialOrd</span>
      <span className="text-gray-300">&gt;</span>
      <br />
      <span className="text-gray-300">  (list: &amp;[</span>
      <span className="text-purple-400">T</span>
      <span className="text-gray-300">]) -&gt; &amp;</span>
      <span className="text-purple-400">T</span>
      <span className="text-gray-300"> {"{"}</span>
      <br />
      <span className="text-gray-500">  // monomorphized</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function TraitObjectsViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="50" y="2" width="80" height="22" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="17" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">dyn Trait</text>
      <line x1="46" y1="44" x2="74" y2="24" stroke="#7c3aed" strokeWidth="1.2" />
      <line x1="134" y1="44" x2="106" y2="24" stroke="#7c3aed" strokeWidth="1.2" />
      <rect x="4" y="44" width="80" height="20" rx="4" fill="#f3f4f6" stroke="#9ca3af" strokeWidth="1" />
      <text x="44" y="57" textAnchor="middle" fontSize="8" fill="#374151">TypeA (runtime)</text>
      <rect x="96" y="44" width="80" height="20" rx="4" fill="#f3f4f6" stroke="#9ca3af" strokeWidth="1" />
      <text x="136" y="57" textAnchor="middle" fontSize="8" fill="#374151">TypeB (runtime)</text>
    </svg>
  );
}

export function IteratorsViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-yellow-300">vec!</span>
      <span className="text-gray-300">[1,2,3]</span>
      <br />
      <span className="text-gray-300">  .</span>
      <span className="text-blue-300">iter</span>
      <span className="text-gray-300">()</span>
      <br />
      <span className="text-gray-300">  .</span>
      <span className="text-blue-300">filter</span>
      <span className="text-gray-300">(|x| *x &gt; </span>
      <span className="text-green-300">1</span>
      <span className="text-gray-300">)</span>
      <br />
      <span className="text-gray-300">  .</span>
      <span className="text-blue-300">collect</span>
      <span className="text-gray-300">::&lt;Vec&lt;_&gt;&gt;()</span>
    </div>
  );
}

export function ClosuresViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">add</span>
      <span className="text-gray-300"> = |a, b| a + b;</span>
      <br />
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">double</span>
      <span className="text-gray-300"> = |x| x * </span>
      <span className="text-green-300">2</span>
      <span className="text-gray-300">;</span>
      <br />
      <span className="text-gray-500">// Fn / FnMut / FnOnce</span>
    </div>
  );
}

// ── Collections ──────────────────────────────────────────────────────────────

export function VecViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="4" width="110" height="20" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="59" y="17" textAnchor="middle" fontSize="8" fontFamily="monospace" fill="#4338ca">len=3 | cap=5 | ptr</text>
      <line x1="114" y1="14" x2="126" y2="14" stroke="#6366f1" strokeWidth="1.2" />
      {[0,1,2,3,4].map((i) => (
        <rect key={i} x={126 + i * 10} y={8} width={9} height={12} rx="2"
          fill={i < 3 ? "#6366f1" : "#e5e7eb"} stroke="#9ca3af" strokeWidth="0.5" />
      ))}
      <text x="90" y="52" textAnchor="middle" fontSize="8" fill="#6b7280">dynamic array, heap-backed</text>
    </svg>
  );
}

export function HashMapViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="36" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="22" y="38" textAnchor="middle" fontSize="8" fill="#92400e">key</text>
      <rect x="56" y="26" width="44" height="18" rx="3" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1" />
      <text x="78" y="38" textAnchor="middle" fontSize="8" fill="#4338ca">hash(key)</text>
      <rect x="116" y="10" width="58" height="50" rx="4" fill="#f3f4f6" stroke="#9ca3af" strokeWidth="1" />
      <text x="145" y="23" textAnchor="middle" fontSize="7" fill="#6b7280">bucket</text>
      <rect x="120" y="26" width="50" height="10" rx="2" fill="#f0fdf4" stroke="#22c55e" strokeWidth="0.8" />
      <rect x="120" y="38" width="50" height="10" rx="2" fill="#f0fdf4" stroke="#22c55e" strokeWidth="0.8" />
      <rect x="120" y="50" width="50" height="6" rx="2" fill="#f0fdf4" stroke="#22c55e" strokeWidth="0.8" />
      <line x1="40" y1="35" x2="56" y2="35" stroke="#9ca3af" strokeWidth="1" />
      <line x1="100" y1="35" x2="116" y2="35" stroke="#9ca3af" strokeWidth="1" />
    </svg>
  );
}

export function StringsViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">s</span>
      <span className="text-gray-300">: &amp;</span>
      <span className="text-purple-400">str</span>
      <span className="text-gray-300"> = </span>
      <span className="text-green-300">&quot;hello&quot;</span>
      <span className="text-gray-300">;</span>
      <br />
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">owned</span>
      <span className="text-gray-300"> = s.</span>
      <span className="text-yellow-300">to_string</span>
      <span className="text-gray-300">();</span>
      <br />
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">slice</span>
      <span className="text-gray-300"> = &amp;owned[</span>
      <span className="text-green-300">0</span>
      <span className="text-gray-300">..</span>
      <span className="text-green-300">3</span>
      <span className="text-gray-300">];</span>
    </div>
  );
}

// ── Concurrency ───────────────────────────────────────────────────────────────

export function ThreadsViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="2" width="60" height="20" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="15" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4338ca">main</text>
      <line x1="54" y1="42" x2="72" y2="22" stroke="#6366f1" strokeWidth="1" />
      <line x1="126" y1="42" x2="108" y2="22" stroke="#6366f1" strokeWidth="1" />
      <rect x="4" y="42" width="100" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="54" y="55" textAnchor="middle" fontSize="8" fill="#166534">thread::spawn()</text>
      <rect x="110" y="42" width="66" height="20" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="143" y="55" textAnchor="middle" fontSize="8" fill="#92400e">join handle</text>
    </svg>
  );
}

export function SendSyncViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="20" width="60" height="32" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="34" y="33" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4338ca">Thread 1</text>
      <text x="34" y="44" textAnchor="middle" fontSize="8" fill="#16a34a">Send ✓</text>
      <rect x="116" y="20" width="60" height="32" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="146" y="33" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4338ca">Thread 2</text>
      <text x="146" y="44" textAnchor="middle" fontSize="8" fill="#16a34a">Sync ✓</text>
      <rect x="70" y="26" width="40" height="20" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="39" textAnchor="middle" fontSize="8" fontWeight="700" fill="#92400e">Data</text>
    </svg>
  );
}

export function MutexViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="30" y="2" width="120" height="20" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="8" fontWeight="700" fill="#92400e">Arc&lt;Mutex&lt;T&gt;&gt;</text>
      <rect x="50" y="42" width="80" height="20" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="55" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">critical section</text>
      <line x1="30" y1="42" x2="70" y2="22" stroke="#22c55e" strokeWidth="1.2" />
      <text x="24" y="40" textAnchor="middle" fontSize="7" fill="#16a34a">T1 lock()</text>
      <line x1="150" y1="42" x2="110" y2="22" stroke="#ef4444" strokeWidth="1.2" strokeDasharray="3,2" />
      <text x="157" y="40" textAnchor="middle" fontSize="7" fill="#ef4444">T2 wait</text>
    </svg>
  );
}

export function ChannelsViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">let </span>
      <span className="text-gray-300">(</span>
      <span className="text-blue-300">tx</span>
      <span className="text-gray-300">, </span>
      <span className="text-blue-300">rx</span>
      <span className="text-gray-300">) = </span>
      <span className="text-yellow-300">mpsc::channel</span>
      <span className="text-gray-300">();</span>
      <br />
      <span className="text-blue-300">tx</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">send</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">42</span>
      <span className="text-gray-300">);</span>
      <br />
      <span className="text-orange-400">let </span>
      <span className="text-blue-300">val</span>
      <span className="text-gray-300"> = </span>
      <span className="text-blue-300">rx</span>
      <span className="text-gray-300">.</span>
      <span className="text-yellow-300">recv</span>
      <span className="text-gray-300">();</span>
    </div>
  );
}

// ── Async ────────────────────────────────────────────────────────────────────

export function AsyncFundamentalsViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="40" y="2" width="100" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">Async Runtime</text>
      {["Task1","Task2","Task3"].map((t, i) => (
        <g key={t}>
          <line x1={28 + i * 52} y1="42" x2={66 + i * 24} y2="22"
            stroke="#6366f1" strokeWidth="1" strokeDasharray="2,2" />
          <rect x={4 + i * 58} y="42" width="48" height="20" rx="4"
            fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
          <text x={28 + i * 58} y="55" textAnchor="middle" fontSize="8" fill="#166534">{t}</text>
        </g>
      ))}
    </svg>
  );
}

export function FuturesViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">async fn </span>
      <span className="text-yellow-300">fetch</span>
      <span className="text-gray-300">() -&gt; </span>
      <span className="text-purple-400">Result</span>
      <span className="text-gray-300"> {"{"}</span>
      <br />
      <span className="text-orange-400">  let </span>
      <span className="text-blue-300">data</span>
      <span className="text-gray-300"> = http::</span>
      <span className="text-yellow-300">get</span>
      <span className="text-gray-300">(url)</span>
      <br />
      <span className="text-gray-300">    .</span>
      <span className="text-blue-300">await</span>
      <span className="text-gray-300">?;</span>
      <br />
      <span className="text-purple-400">  Ok</span>
      <span className="text-gray-300">(data)</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function TokioViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="30" y="2" width="120" height="20" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="16" textAnchor="middle" fontSize="9" fontWeight="700" fill="#92400e">Tokio Runtime</text>
      <rect x="4" y="32" width="52" height="18" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1" />
      <text x="30" y="44" textAnchor="middle" fontSize="8" fill="#4338ca">Worker 1</text>
      <rect x="64" y="32" width="52" height="18" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1" />
      <text x="90" y="44" textAnchor="middle" fontSize="8" fill="#4338ca">Worker 2</text>
      <rect x="124" y="32" width="52" height="18" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1" />
      <text x="150" y="44" textAnchor="middle" fontSize="8" fill="#4338ca">Worker 3</text>
      {[30,90,150].map((x, i) => (
        <line key={i} x1={x} y1="50" x2={x} y2="60" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
      ))}
      <text x="90" y="70" textAnchor="middle" fontSize="7" fill="#9ca3af">tasks scheduled cooperatively</text>
    </svg>
  );
}

export function AsyncNetworkingViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="70" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="39" y="37" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">TcpListener</text>
      <text x="84" y="37" textAnchor="middle" fontSize="8" fill="#6b7280">accept()</text>
      {["conn1","conn2","conn3"].map((c, i) => (
        <g key={c}>
          <line x1="110" y1="34" x2="116 " y2={18 + i * 18} stroke="#22c55e" strokeWidth="1" />
          <rect x={116} y={8 + i * 18} width="58" height="14" rx="3"
            fill="#f0fdf4" stroke="#22c55e" strokeWidth="0.8" />
          <text x="145" y={18 + i * 18} textAnchor="middle" fontSize="8" fill="#166534">{c} task</text>
        </g>
      ))}
    </svg>
  );
}

// ── Systems ───────────────────────────────────────────────────────────────────

export function UnsafeViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-orange-400">unsafe </span>
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-orange-400">  let </span>
      <span className="text-blue-300">ptr</span>
      <span className="text-gray-300"> = &amp;raw </span>
      <span className="text-purple-400">const </span>
      <span className="text-gray-300">x;</span>
      <br />
      <span className="text-gray-300">  *ptr </span>
      <span className="text-gray-500">// deref raw ptr</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function MemoryManagementViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="4" width="50" height="60" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="29" y="20" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4338ca">Stack</text>
      <text x="29" y="34" textAnchor="middle" fontSize="7" fill="#6b7280">frames</text>
      <text x="29" y="54" textAnchor="middle" fontSize="7" fill="#6b7280">fast</text>
      <rect x="66" y="4" width="80" height="60" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="106" y="20" textAnchor="middle" fontSize="9" fontWeight="700" fill="#92400e">Heap</text>
      <text x="106" y="34" textAnchor="middle" fontSize="7" fill="#6b7280">Box / Vec / String</text>
      <text x="106" y="54" textAnchor="middle" fontSize="8" fill="#16a34a">RAII: auto drop()</text>
    </svg>
  );
}

// ── Backend ───────────────────────────────────────────────────────────────────

export function AxumViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">Router</span>
      <span className="text-gray-300">::</span>
      <span className="text-yellow-300">new</span>
      <span className="text-gray-300">()</span>
      <br />
      <span className="text-gray-300">  .</span>
      <span className="text-yellow-300">route</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">&quot;/&quot;</span>
      <span className="text-gray-300">, </span>
      <span className="text-blue-300">get</span>
      <span className="text-gray-300">(handler))</span>
      <br />
      <span className="text-gray-300">  .</span>
      <span className="text-yellow-300">layer</span>
      <span className="text-gray-300">(middleware)</span>
    </div>
  );
}

export function PostgresViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-yellow-300">sqlx::query!</span>
      <span className="text-gray-300">(</span>
      <br />
      <span className="text-green-300">  &quot;SELECT * FROM users&quot;</span>
      <br />
      <span className="text-gray-300">)</span>
      <br />
      <span className="text-gray-300">.</span>
      <span className="text-blue-300">fetch_all</span>
      <span className="text-gray-300">(&amp;pool).</span>
      <span className="text-orange-400">await</span>
    </div>
  );
}

export function GrpcViz(_: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="44" height="20" rx="4" fill="#e0e7ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="26" y="37" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4338ca">Client</text>
      <rect x="64" y="18" width="52" height="32" rx="4" fill="#fef3c7" stroke="#f59e0b" strokeWidth="1.2" />
      <text x="90" y="32" textAnchor="middle" fontSize="7" fill="#92400e">Protobuf</text>
      <text x="90" y="44" textAnchor="middle" fontSize="8" fontWeight="700" fill="#92400e">tonic</text>
      <rect x="132" y="24" width="44" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="154" y="37" textAnchor="middle" fontSize="8" fontWeight="700" fill="#166534">Service</text>
      <line x1="48" y1="34" x2="64" y2="34" stroke="#9ca3af" strokeWidth="1" />
      <line x1="116" y1="34" x2="132" y2="34" stroke="#9ca3af" strokeWidth="1" />
    </svg>
  );
}

export function TestingViz(_: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-blue-300">#[cfg(test)]</span>
      <br />
      <span className="text-orange-400">fn </span>
      <span className="text-yellow-300">test_add</span>
      <span className="text-gray-300">() {"{"}</span>
      <br />
      <span className="text-yellow-300">  assert_eq!</span>
      <span className="text-gray-300">(</span>
      <span className="text-yellow-300">add</span>
      <span className="text-gray-300">(</span>
      <span className="text-green-300">1</span>
      <span className="text-gray-300">,</span>
      <span className="text-green-300">2</span>
      <span className="text-gray-300">), </span>
      <span className="text-green-300">3</span>
      <span className="text-gray-300">);</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

// ── Visualization map ─────────────────────────────────────────────────────────

export const RUST_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "rust-fundamentals":       RustFundamentalsViz,
  "cargo-toolchain":         CargoViz,
  "variables-mutability":    RustFundamentalsViz,
  "primitive-types":         RustFundamentalsViz,
  "functions-expressions":   RustFundamentalsViz,
  "control-flow":            RustFundamentalsViz,
  "structs":                 OwnershipViz,
  "enums-pattern-matching":  ClosuresViz,
  "modules-crates":          CargoViz,
  "error-handling-rust":     IteratorsViz,
  "ownership-fundamentals":  OwnershipViz,
  "move-semantics":          MoveSemanticsViz,
  "borrowing":               BorrowingViz,
  "mutable-references":      MutableRefViz,
  "lifetimes":               LifetimesViz,
  "lifetime-annotations":    LifetimesViz,
  "slices":                  SlicesViz,
  "smart-pointers":          SmartPointersViz,
  "box":                     SmartPointersViz,
  "rc-arc":                  SmartPointersViz,
  "traits":                  TraitsViz,
  "trait-bounds":            GenericsViz,
  "generics":                GenericsViz,
  "trait-objects":           TraitObjectsViz,
  "derive-macros":           TraitsViz,
  "iterators":               IteratorsViz,
  "closures":                ClosuresViz,
  "vec":                     VecViz,
  "hashmap":                 HashMapViz,
  "strings-rust":            StringsViz,
  "collections-performance": VecViz,
  "threads":                 ThreadsViz,
  "send-sync":               SendSyncViz,
  "mutex-rust":              MutexViz,
  "arc-mutex":               MutexViz,
  "channels-rust":           ChannelsViz,
  "concurrent-patterns-rust":ThreadsViz,
  "async-fundamentals":      AsyncFundamentalsViz,
  "futures":                 FuturesViz,
  "async-await":             FuturesViz,
  "tokio":                   TokioViz,
  "async-channels":          ChannelsViz,
  "async-networking":        AsyncNetworkingViz,
  "async-error-handling":    FuturesViz,
  "memory-management-rust":  MemoryManagementViz,
  "unsafe-rust":             UnsafeViz,
  "ffi":                     UnsafeViz,
  "file-systems-rust":       PostgresViz,
  "performance-profiling":   RustFundamentalsViz,
  "http-servers-rust":       AxumViz,
  "axum":                    AxumViz,
  "actix-web":               AxumViz,
  "postgresql-rust":         PostgresViz,
  "redis-rust":              PostgresViz,
  "grpc-rust":               GrpcViz,
  "auth-rust":               AxumViz,
  "testing-rust":            TestingViz,
  "production-rust":         TokioViz,
};
