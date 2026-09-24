import React from "react";

interface MiniVizProps { animate?: boolean }

// ── Foundations ───────────────────────────────────────────────────────────────

export function AiEngineeringFundamentalsViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {["Python", "NumPy", "Model", "Predict"].map((label, i) => {
        const x = 8 + i * 43;
        const colors = ["#eef2ff","#f0fdf4","#fef9c3","#fdf4ff"];
        const strokes = ["#6366f1","#22c55e","#eab308","#a855f7"];
        const texts = ["#4f46e5","#16a34a","#ca8a04","#9333ea"];
        return (
          <g key={label}>
            <rect x={x} y="20" width="37" height="22" rx="4" fill={colors[i]} stroke={strokes[i]} strokeWidth="1.2" />
            <text x={x + 18.5} y="35" textAnchor="middle" fontSize="8" fontWeight="700" fill={texts[i]}>{label}</text>
            {i < 3 && <text x={x + 40} y="34" textAnchor="middle" fontSize="10" fill="#9ca3af">→</text>}
          </g>
        );
      })}
    </svg>
  );
}

export function PythonForAiViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">import </span>
      <span className="text-blue-300">numpy </span>
      <span className="text-purple-400">as </span>
      <span className="text-blue-300">np</span>
      <br />
      <span className="text-blue-300">df </span>
      <span className="text-gray-300">= pd.</span>
      <span className="text-yellow-300">DataFrame</span>
      <span className="text-gray-300">(data)</span>
      <br />
      <span className="text-blue-300">preds </span>
      <span className="text-gray-300">= model.</span>
      <span className="text-yellow-300">predict</span>
      <span className="text-gray-300">(X)</span>
    </div>
  );
}

export function MlFundamentalsViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="22" width="32" height="20" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="20" y="35" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#4f46e5">Data</text>
      <text x="40" y="34" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="45" y="16" width="28" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="59" y="26" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#16a34a">Train</text>
      <rect x="45" y="33" width="28" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="59" y="43" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#ca8a04">Val</text>
      <rect x="45" y="50" width="28" height="14" rx="3" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1" />
      <text x="59" y="60" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#db2777">Test</text>
      <text x="77" y="34" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="82" y="22" width="36" height="20" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="100" y="35" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#6d28d9">Model</text>
      <text x="122" y="34" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="128" y="22" width="46" height="20" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="151" y="35" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#059669">Inference</text>
    </svg>
  );
}

export function SupervisedLearningViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="38" height="22" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="23" y="33" textAnchor="middle" fontSize="7" fill="#6366f1">Input</text>
      <text x="23" y="43" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">X</text>
      <text x="46" y="37" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="52" y="20" width="52" height="30" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="78" y="33" textAnchor="middle" fontSize="7" fill="#7c3aed">learn(X→y)</text>
      <text x="78" y="44" textAnchor="middle" fontSize="8" fontWeight="700" fill="#6d28d9">Model</text>
      <text x="108" y="37" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="114" y="24" width="60" height="22" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="144" y="33" textAnchor="middle" fontSize="7" fill="#22c55e">Label</text>
      <text x="144" y="43" textAnchor="middle" fontSize="8" fontWeight="700" fill="#16a34a">ŷ</text>
    </svg>
  );
}

export function NeuralNetworkViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {/* Input layer */}
      {[14, 34, 54].map((cy, i) => (
        <g key={`in-${i}`}>
          <circle cx="28" cy={cy} r="9" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
          <text x="28" y={cy + 3} textAnchor="middle" fontSize="7" fill="#4f46e5">x{i+1}</text>
        </g>
      ))}
      {/* Hidden layer */}
      {[14, 34, 54].map((cy, i) => (
        <g key={`h-${i}`}>
          {[28, 90].map(cx => (
            <line key={cx} x1={cx === 28 ? 37 : 99} y1={cy} x2={cx === 28 ? 81 : 134} y2={[14,34,54][i]} stroke="#c4b5fd" strokeWidth="0.6" />
          ))}
          <circle cx="90" cy={cy} r="9" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
          <text x="90" y={cy + 3} textAnchor="middle" fontSize="7" fill="#6d28d9">h{i+1}</text>
        </g>
      ))}
      {/* Output layer */}
      <line x1="99" y1="14" x2="143" y2="34" stroke="#c4b5fd" strokeWidth="0.6" />
      <line x1="99" y1="34" x2="143" y2="34" stroke="#c4b5fd" strokeWidth="0.6" />
      <line x1="99" y1="54" x2="143" y2="34" stroke="#c4b5fd" strokeWidth="0.6" />
      <circle cx="152" cy="34" r="9" fill="#ecfdf5" stroke="#10b981" strokeWidth="1.2" />
      <text x="152" y="37" textAnchor="middle" fontSize="7" fill="#059669">out</text>
    </svg>
  );
}

export function TransformerViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="28" width="30" height="16" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="19" y="39" textAnchor="middle" fontSize="7" fill="#64748b">Input</text>
      <text x="37" y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      {/* Block 1 */}
      <rect x="42" y="14" width="44" height="44" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <rect x="46" y="18" width="36" height="14" rx="3" fill="#c7d2fe" stroke="#6366f1" strokeWidth="0.8" />
      <text x="64" y="28" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#3730a3">Self-Attn</text>
      <rect x="46" y="36" width="36" height="14" rx="3" fill="#ddd6fe" stroke="#7c3aed" strokeWidth="0.8" />
      <text x="64" y="46" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#5b21b6">FFN</text>
      <text x="89" y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      {/* Block 2 */}
      <rect x="94" y="14" width="44" height="44" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <rect x="98" y="18" width="36" height="14" rx="3" fill="#c7d2fe" stroke="#6366f1" strokeWidth="0.8" />
      <text x="116" y="28" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#3730a3">Self-Attn</text>
      <rect x="98" y="36" width="36" height="14" rx="3" fill="#ddd6fe" stroke="#7c3aed" strokeWidth="0.8" />
      <text x="116" y="46" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#5b21b6">FFN</text>
      <text x="141" y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="146" y="28" width="30" height="16" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="161" y="39" textAnchor="middle" fontSize="7" fill="#059669">Out</text>
    </svg>
  );
}

export function LlmViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="24" width="32" height="22" rx="4" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="20" y="33" textAnchor="middle" fontSize="7" fill="#64748b">User</text>
      <text x="20" y="43" textAnchor="middle" fontSize="7" fill="#475569">Prompt</text>
      <text x="40" y="37" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="46" y="16" width="76" height="38" rx="6" fill="#4f46e5" />
      <text x="84" y="32" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">LLM</text>
      <text x="84" y="45" textAnchor="middle" fontSize="7" fill="#c7d2fe">Transformer</text>
      <text x="126" y="37" textAnchor="middle" fontSize="9" fill="#9ca3af">→</text>
      <rect x="132" y="24" width="44" height="22" rx="4" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="154" y="33" textAnchor="middle" fontSize="7" fill="#059669">Response</text>
      <text x="154" y="43" textAnchor="middle" fontSize="7" fill="#10b981">text</text>
    </svg>
  );
}

export function TokensViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-green-300">"Hello world"</span>
      <span className="text-gray-400"> → </span>
      <br />
      <span className="text-yellow-300">[Hello]</span>
      <span className="text-blue-300">[_world]</span>
      <br />
      <span className="text-gray-500">ids=</span>
      <span className="text-purple-300">[15496, 995]</span>
    </div>
  );
}

export function ContextWindowViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <text x="90" y="12" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#64748b">Context Window</text>
      <rect x="4" y="20" width="36" height="22" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="22" y="34" textAnchor="middle" fontSize="7" fill="#4f46e5">System</text>
      <rect x="42" y="20" width="44" height="22" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="64" y="34" textAnchor="middle" fontSize="7" fill="#ca8a04">History</text>
      <rect x="88" y="20" width="44" height="22" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="110" y="34" textAnchor="middle" fontSize="7" fill="#16a34a">New Msg</text>
      <rect x="134" y="20" width="42" height="22" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="155" y="34" textAnchor="middle" fontSize="7" fill="#64748b">Reserve</text>
      <text x="90" y="56" textAnchor="middle" fontSize="7" fill="#9ca3af">← max tokens →</text>
    </svg>
  );
}

export function TemperatureSamplingViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <text x="6" y="11" fontSize="7.5" fontWeight="700" fill="#64748b">Token probabilities</text>
      {[
        { label: "the", prob: 0.55, color: "#4f46e5", fill: "#eef2ff" },
        { label: "a",   prob: 0.25, color: "#7c3aed", fill: "#ede9fe" },
        { label: "an",  prob: 0.12, color: "#2563eb", fill: "#dbeafe" },
        { label: "one", prob: 0.08, color: "#0891b2", fill: "#cffafe" },
      ].map(({ label, prob, color, fill }, i) => {
        const barW = prob * 110;
        const y = 18 + i * 13;
        return (
          <g key={label}>
            <text x="6" y={y + 9} fontSize="7" fill="#64748b" fontFamily="monospace">{label}</text>
            <rect x="26" y={y} width={barW} height="10" rx="2" fill={fill} stroke={color} strokeWidth="0.8" />
            <text x={28 + barW} y={y + 8} fontSize="6.5" fill={color}>{(prob * 100).toFixed(0)}%</text>
          </g>
        );
      })}
      <text x="100" y="68" fontSize="7" fill="#6366f1" fontWeight="600">temp=0.8</text>
    </svg>
  );
}

export function PromptEngineeringViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">System: </span>
      <span className="text-green-300">"You are..."</span>
      <br />
      <span className="text-blue-300">User: </span>
      <span className="text-yellow-300">{"{"}</span>
      <span className="text-gray-300">input</span>
      <span className="text-yellow-300">{"}"}</span>
      <br />
      <span className="text-green-400">Assistant: </span>
      <span className="text-gray-400">...</span>
    </div>
  );
}

export function ChainOfThoughtViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-yellow-300">Think step by step:</span>
      <br />
      <span className="text-gray-400">1. </span>
      <span className="text-blue-300">Parse the question</span>
      <br />
      <span className="text-gray-400">2. </span>
      <span className="text-blue-300">Reason through it</span>
      <br />
      <span className="text-green-400">Answer: </span>
      <span className="text-green-300">42</span>
    </div>
  );
}

export function StructuredOutputsViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-gray-300">{"{"}</span>
      <br />
      <span className="text-blue-300">  "name"</span>
      <span className="text-gray-300">: </span>
      <span className="text-green-300">"..."</span>
      <span className="text-gray-400">,</span>
      <br />
      <span className="text-blue-300">  "score"</span>
      <span className="text-gray-300">: </span>
      <span className="text-yellow-300">0.9</span>
      <span className="text-gray-400">,</span>
      <br />
      <span className="text-blue-300">  "tags"</span>
      <span className="text-gray-300">: [...]</span>
      <br />
      <span className="text-gray-300">{"}"}</span>
    </div>
  );
}

export function EmbeddingsViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="28" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="18" y="38" textAnchor="middle" fontSize="8" fontWeight="700" fill="#ca8a04">"cat"</text>
      <text x="36" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="42" y="22" width="62" height="26" rx="3" fill="#1e1b4b" />
      <text x="73" y="33" textAnchor="middle" fontSize="6.5" fill="#a5b4fc">[0.2, -0.1,</text>
      <text x="73" y="43" textAnchor="middle" fontSize="6.5" fill="#a5b4fc"> 0.8, 0.3...]</text>
      <text x="108" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      {/* Semantic space dots */}
      {[
        [118,18,"#4f46e5"],[128,24,"#6366f1"],[122,32,"#818cf8"],
        [136,16,"#4f46e5"],[142,28,"#6366f1"],[130,38,"#a5b4fc"],
        [148,20,"#818cf8"],[154,34,"#c7d2fe"],[160,14,"#4f46e5"],
        [156,44,"#818cf8"],[166,26,"#6366f1"],[170,38,"#a5b4fc"],
      ].map(([cx, cy, fill], i) => (
        <circle key={i} cx={cx as number} cy={cy as number} r="3.5" fill={fill as string} opacity="0.8" />
      ))}
    </svg>
  );
}

export function VectorDatabaseViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="44" height="20" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="26" y="35" textAnchor="middle" fontSize="7" fill="#4f46e5">Query vec</text>
      <text x="26" y="43" textAnchor="middle" fontSize="6.5" fill="#818cf8">cosine_sim</text>
      <text x="52" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="58" y="12" width="50" height="16" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="83" y="23" textAnchor="middle" fontSize="7.5" fill="#16a34a">Result 1  0.97</text>
      <rect x="58" y="30" width="50" height="16" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="83" y="41" textAnchor="middle" fontSize="7.5" fill="#ca8a04">Result 2  0.91</text>
      <rect x="58" y="48" width="50" height="16" rx="3" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1" />
      <text x="83" y="59" textAnchor="middle" fontSize="7.5" fill="#db2777">Result 3  0.87</text>
    </svg>
  );
}

export function ChunkingViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="28" width="36" height="16" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="22" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">Document</text>
      <text x="44" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="50" y="20" width="26" height="14" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="63" y="30" textAnchor="middle" fontSize="7" fill="#4f46e5">chunk1</text>
      <rect x="50" y="36" width="26" height="14" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="63" y="46" textAnchor="middle" fontSize="7" fill="#6d28d9">chunk2</text>
      <rect x="50" y="52" width="26" height="14" rx="3" fill="#ddd6fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="63" y="62" textAnchor="middle" fontSize="7" fill="#5b21b6">chunk3</text>
      <text x="80" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <circle cx="100" cy="24" r="10" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="100" y="27" textAnchor="middle" fontSize="6" fill="#4f46e5">vec</text>
      <circle cx="100" cy="44" r="10" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="100" y="47" textAnchor="middle" fontSize="6" fill="#6d28d9">vec</text>
      <circle cx="100" cy="62" r="8" fill="#ddd6fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="100" y="65" textAnchor="middle" fontSize="6" fill="#5b21b6">vec</text>
      <text x="116" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="122" y="22" width="52" height="30" rx="4" fill="#1e1b4b" />
      <text x="148" y="34" textAnchor="middle" fontSize="7" fill="#a5b4fc">Vector DB</text>
      <text x="148" y="44" textAnchor="middle" fontSize="6.5" fill="#6366f1">pgvector</text>
    </svg>
  );
}

export function RagViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {[
        { label: "Query", x: 4, y: 4, fill: "#eef2ff", stroke: "#6366f1", textFill: "#4f46e5" },
        { label: "Embed", x: 36, y: 4, fill: "#ede9fe", stroke: "#7c3aed", textFill: "#6d28d9" },
        { label: "VecDB", x: 68, y: 4, fill: "#1e1b4b", stroke: "#4f46e5", textFill: "#a5b4fc" },
        { label: "Retrieve", x: 104, y: 4, fill: "#fef9c3", stroke: "#eab308", textFill: "#ca8a04" },
      ].map(({ label, x, fill, stroke, textFill }, i) => (
        <g key={label}>
          <rect x={x} y="8" width="30" height="16" rx="3" fill={fill} stroke={stroke} strokeWidth="1" />
          <text x={x + 15} y="19" textAnchor="middle" fontSize="6.5" fontWeight="700" fill={textFill}>{label}</text>
          {i < 3 && <text x={x + 33} y="18" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>}
        </g>
      ))}
      <rect x="4" y="36" width="72" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="40" y="46" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#16a34a">Context + Query</text>
      <text x="80" y="45" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="86" y="32" width="36" height="22" rx="4" fill="#4f46e5" />
      <text x="104" y="45" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">LLM</text>
      <text x="126" y="45" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="132" y="36" width="44" height="14" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="154" y="46" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#059669">Answer</text>
    </svg>
  );
}

export function HybridSearchViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="27" width="32" height="18" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="20" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">Query</text>
      <line x1="36" y1="33" x2="54" y2="22" stroke="#94a3b8" strokeWidth="1" />
      <line x1="36" y1="36" x2="54" y2="50" stroke="#94a3b8" strokeWidth="1" />
      <rect x="54" y="12" width="36" height="18" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="72" y="24" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#1d4ed8">BM25</text>
      <rect x="54" y="42" width="36" height="18" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="72" y="54" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#6d28d9">Vector</text>
      <line x1="90" y1="21" x2="108" y2="32" stroke="#94a3b8" strokeWidth="1" />
      <line x1="90" y1="51" x2="108" y2="40" stroke="#94a3b8" strokeWidth="1" />
      <rect x="108" y="27" width="32" height="18" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="124" y="39" textAnchor="middle" fontSize="7" fill="#ca8a04">Merge</text>
      <text x="144" y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="150" y="27" width="26" height="18" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="163" y="39" textAnchor="middle" fontSize="7" fill="#059669">Rerank</text>
    </svg>
  );
}

export function ToolCallingViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="34" height="20" rx="4" fill="#4f46e5" />
      <text x="21" y="39" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">LLM</text>
      <text x="42" y="37" textAnchor="middle" fontSize="7" fill="#6366f1">tool_call</text>
      <line x1="38" y1="33" x2="62" y2="18" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="38" y1="36" x2="62" y2="36" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <line x1="38" y1="39" x2="62" y2="54" stroke="#6366f1" strokeWidth="1" strokeDasharray="3,2" />
      <rect x="62" y="9" width="32" height="16" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="78" y="20" textAnchor="middle" fontSize="7" fill="#ca8a04">Search</text>
      <rect x="62" y="28" width="32" height="16" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="78" y="39" textAnchor="middle" fontSize="7" fill="#1d4ed8">DB</text>
      <rect x="62" y="47" width="32" height="16" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="78" y="58" textAnchor="middle" fontSize="7" fill="#16a34a">API</text>
      <line x1="94" y1="17" x2="114" y2="33" stroke="#10b981" strokeWidth="1" />
      <line x1="94" y1="36" x2="114" y2="36" stroke="#10b981" strokeWidth="1" />
      <line x1="94" y1="55" x2="114" y2="39" stroke="#10b981" strokeWidth="1" />
      <text x="107" y="29" fontSize="6" fill="#10b981">result</text>
      <rect x="114" y="26" width="34" height="20" rx="4" fill="#4f46e5" />
      <text x="131" y="39" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">LLM</text>
      <text x="152" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="156" y="30" width="22" height="12" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="167" y="39" textAnchor="middle" fontSize="6.5" fill="#059669">Resp</text>
    </svg>
  );
}

export function AgentViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="26" height="20" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="17" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">User</text>
      <text x="34" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="40" y="14" width="56" height="44" rx="5" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.5" />
      <text x="68" y="27" textAnchor="middle" fontSize="7" fontWeight="700" fill="#4f46e5">Agent Loop</text>
      <text x="68" y="38" textAnchor="middle" fontSize="6.5" fill="#818cf8">Observe</text>
      <text x="68" y="47" textAnchor="middle" fontSize="6.5" fill="#818cf8">Think · Act</text>
      <line x1="96" y1="22" x2="116" y2="16" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="96" y1="36" x2="116" y2="36" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="96" y1="50" x2="116" y2="56" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <rect x="116" y="8" width="26" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="129" y="18" textAnchor="middle" fontSize="6.5" fill="#ca8a04">Search</text>
      <rect x="116" y="29" width="26" height="14" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="129" y="39" textAnchor="middle" fontSize="6.5" fill="#1d4ed8">DB</text>
      <rect x="116" y="50" width="26" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="129" y="60" textAnchor="middle" fontSize="6.5" fill="#16a34a">API</text>
      <text x="147" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="152" y="28" width="24" height="16" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="164" y="39" textAnchor="middle" fontSize="6.5" fill="#059669">Resp</text>
    </svg>
  );
}

export function MultiAgentViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="60" y="4" width="60" height="18" rx="4" fill="#4f46e5" />
      <text x="90" y="16" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">Orchestrator</text>
      <line x1="76" y1="22" x2="32" y2="38" stroke="#6366f1" strokeWidth="1" />
      <line x1="90" y1="22" x2="90" y2="38" stroke="#6366f1" strokeWidth="1" />
      <line x1="104" y1="22" x2="148" y2="38" stroke="#6366f1" strokeWidth="1" />
      <rect x="4" y="38" width="56" height="18" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="32" y="50" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#4f46e5">Agent A</text>
      <rect x="62" y="38" width="56" height="18" rx="4" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1.2" />
      <text x="90" y="50" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#6d28d9">Agent B</text>
      <rect x="120" y="38" width="56" height="18" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="148" y="50" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#16a34a">Agent C</text>
      <line x1="32" y1="56" x2="74" y2="62" stroke="#6366f1" strokeWidth="1" />
      <line x1="90" y1="56" x2="90" y2="62" stroke="#6366f1" strokeWidth="1" />
      <line x1="148" y1="56" x2="106" y2="62" stroke="#6366f1" strokeWidth="1" />
      <rect x="62" y="62" width="56" height="8" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="90" y="69" textAnchor="middle" fontSize="6.5" fill="#059669">Synthesize</text>
    </svg>
  );
}

export function EvaluationViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {[
        { label: "Dataset", x: 2, fill: "#f1f5f9", stroke: "#94a3b8", text: "#475569" },
        { label: "Model",   x: 42, fill: "#eef2ff", stroke: "#6366f1", text: "#4f46e5" },
        { label: "Output",  x: 82, fill: "#fef9c3", stroke: "#eab308", text: "#ca8a04" },
        { label: "Evaluator", x: 122, fill: "#ede9fe", stroke: "#7c3aed", text: "#6d28d9" },
      ].map(({ label, x, fill, stroke, text }, i) => (
        <g key={label}>
          <rect x={x} y="26" width="36" height="20" rx="4" fill={fill} stroke={stroke} strokeWidth="1.2" />
          <text x={x + 18} y="39" textAnchor="middle" fontSize="7.5" fontWeight="700" fill={text}>{label}</text>
          {i < 3 && <text x={x + 39} y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>}
        </g>
      ))}
      <text x="162" y="38" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="166" y="30" width="12" height="16" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="172" y="42" textAnchor="middle" fontSize="6.5" fill="#059669">✓</text>
    </svg>
  );
}

export function LlmJudgeViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="42" height="20" rx="4" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="25" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">Response</text>
      <text x="50" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="56" y="20" width="52" height="32" rx="4" fill="#4f46e5" />
      <text x="82" y="33" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">Judge LLM</text>
      <text x="82" y="44" textAnchor="middle" fontSize="6.5" fill="#c7d2fe">rubric-based</text>
      <text x="112" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="118" y="18" width="58" height="36" rx="4" fill="#ecfdf5" stroke="#10b981" strokeWidth="1.2" />
      <text x="147" y="30" textAnchor="middle" fontSize="6.5" fontWeight="700" fill="#059669">score: 0.9</text>
      <text x="147" y="42" textAnchor="middle" fontSize="6" fill="#6b7280">reason: "..."</text>
    </svg>
  );
}

export function HallucinationViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="40" height="20" rx="4" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="24" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">Response</text>
      <text x="48" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="54" y="18" width="52" height="36" rx="4" fill="#fef2f2" stroke="#ef4444" strokeWidth="1.2" />
      <text x="80" y="30" textAnchor="middle" fontSize="7" fontWeight="700" fill="#dc2626">Fact Check</text>
      <text x="80" y="42" textAnchor="middle" fontSize="9" fill="#dc2626">✗ / ✓</text>
      <text x="110" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="116" y="22" width="60" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="146" y="32" textAnchor="middle" fontSize="7" fontWeight="600" fill="#16a34a">Grounded ✓</text>
      <rect x="116" y="40" width="60" height="14" rx="3" fill="#fef2f2" stroke="#ef4444" strokeWidth="1" />
      <text x="146" y="50" textAnchor="middle" fontSize="7" fontWeight="600" fill="#dc2626">Hallucinated ✗</text>
    </svg>
  );
}

export function AiArchitectureViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="28" width="30" height="16" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="19" y="39" textAnchor="middle" fontSize="7" fill="#475569">Client</text>
      <text x="38" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="44" y="22" width="38" height="28" rx="4" fill="#4f46e5" />
      <text x="63" y="33" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="white">Gateway</text>
      <text x="63" y="43" textAnchor="middle" fontSize="6.5" fill="#c7d2fe">route·auth</text>
      <line x1="82" y1="28" x2="102" y2="18" stroke="#6366f1" strokeWidth="0.8" />
      <line x1="82" y1="36" x2="102" y2="36" stroke="#6366f1" strokeWidth="0.8" />
      <line x1="82" y1="44" x2="102" y2="54" stroke="#6366f1" strokeWidth="0.8" />
      <rect x="102" y="10" width="36" height="14" rx="3" fill="#eef2ff" stroke="#6366f1" strokeWidth="1" />
      <text x="120" y="20" textAnchor="middle" fontSize="6.5" fill="#4f46e5">Model A</text>
      <rect x="102" y="29" width="36" height="14" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="120" y="39" textAnchor="middle" fontSize="6.5" fill="#6d28d9">Model B</text>
      <rect x="102" y="48" width="36" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="120" y="58" textAnchor="middle" fontSize="6.5" fill="#ca8a04">Cache</text>
      <text x="143" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="150" y="28" width="26" height="16" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="163" y="39" textAnchor="middle" fontSize="6.5" fill="#059669">Resp</text>
    </svg>
  );
}

export function StreamingViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-purple-400">for </span>
      <span className="text-blue-300">chunk </span>
      <span className="text-purple-400">in </span>
      <span className="text-yellow-300">stream</span>
      <span className="text-gray-300">:</span>
      <br />
      <span className="text-purple-400">  yield </span>
      <span className="text-blue-300">chunk</span>
      <br />
      <span className="text-gray-500"># SSE: </span>
      <span className="text-green-300">data: {"{"}"token"{"}"}</span>
    </div>
  );
}

export function AiObservabilityViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="32" height="20" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="20" y="39" textAnchor="middle" fontSize="7.5" fill="#475569">Request</text>
      <text x="40" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="46" y="22" width="34" height="28" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="63" y="39" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#4f46e5">Trace</text>
      <text x="84" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="90" y="10" width="32" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="106" y="20" textAnchor="middle" fontSize="6.5" fill="#ca8a04">Tokens</text>
      <rect x="90" y="26" width="32" height="14" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="106" y="36" textAnchor="middle" fontSize="6.5" fill="#1d4ed8">Latency</text>
      <rect x="90" y="42" width="32" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="106" y="52" textAnchor="middle" fontSize="6.5" fill="#16a34a">Cost</text>
      <rect x="90" y="58" width="32" height="12" rx="3" fill="#fdf2f8" stroke="#ec4899" strokeWidth="1" />
      <text x="106" y="67" textAnchor="middle" fontSize="6.5" fill="#db2777">Quality</text>
    </svg>
  );
}

export function AiSecurityViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="26" width="28" height="20" rx="3" fill="#fef2f2" stroke="#ef4444" strokeWidth="1" />
      <text x="18" y="39" textAnchor="middle" fontSize="7" fill="#dc2626">Input</text>
      <text x="36" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="42" y="14" width="40" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="62" y="24" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#ca8a04">Inj. Check</text>
      <rect x="42" y="30" width="40" height="14" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="62" y="40" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#1d4ed8">PII Filter</text>
      <rect x="42" y="46" width="40" height="14" rx="3" fill="#ede9fe" stroke="#7c3aed" strokeWidth="1" />
      <text x="62" y="56" textAnchor="middle" fontSize="6.5" fontWeight="600" fill="#6d28d9">Guardrail</text>
      <text x="86" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="92" y="24" width="52" height="24" rx="4" fill="#ecfdf5" stroke="#10b981" strokeWidth="1.2" />
      <text x="118" y="34" textAnchor="middle" fontSize="7" fontWeight="700" fill="#059669">Safe Output</text>
      <text x="118" y="44" textAnchor="middle" fontSize="6.5" fill="#10b981">✓ validated</text>
    </svg>
  );
}

export function ChatbotProjectViz(_p: MiniVizProps = {}) {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-blue-300">User: </span>
      <span className="text-gray-300">"help me..."</span>
      <br />
      <span className="text-gray-500">[thinking...]</span>
      <br />
      <span className="text-green-400">Assistant: </span>
      <span className="text-green-300">"Here's..."</span>
    </div>
  );
}

export function RagProjectViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {[
        { label: "PDF", x: 4, fill: "#fef2f2", stroke: "#ef4444", text: "#dc2626" },
        { label: "Chunk", x: 38, fill: "#fef9c3", stroke: "#eab308", text: "#ca8a04" },
        { label: "Embed", x: 72, fill: "#ede9fe", stroke: "#7c3aed", text: "#6d28d9" },
        { label: "Store", x: 106, fill: "#1e1b4b", stroke: "#4f46e5", text: "#a5b4fc" },
      ].map(({ label, x, fill, stroke, text }, i) => (
        <g key={label}>
          <rect x={x} y="22" width="30" height="18" rx="3" fill={fill} stroke={stroke} strokeWidth="1" />
          <text x={x + 15} y="34" textAnchor="middle" fontSize="7.5" fontWeight="700" fill={text}>{label}</text>
          {i < 3 && <text x={x + 33} y="33" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>}
        </g>
      ))}
      <text x="56" y="56" textAnchor="middle" fontSize="8" fill="#9ca3af">Query →</text>
      <rect x="94" y="48" width="40" height="16" rx="3" fill="#ecfdf5" stroke="#10b981" strokeWidth="1" />
      <text x="114" y="59" textAnchor="middle" fontSize="7" fontWeight="600" fill="#059669">Answer</text>
    </svg>
  );
}

export function AgentProjectViz(_p: MiniVizProps = {}) {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      <rect x="4" y="28" width="28" height="16" rx="3" fill="#f1f5f9" stroke="#94a3b8" strokeWidth="1" />
      <text x="18" y="39" textAnchor="middle" fontSize="7" fill="#475569">Task</text>
      <text x="36" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="42" y="24" width="30" height="24" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="57" y="39" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#4f46e5">Plan</text>
      <line x1="72" y1="30" x2="90" y2="18" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="72" y1="36" x2="90" y2="36" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <line x1="72" y1="42" x2="90" y2="54" stroke="#6366f1" strokeWidth="0.8" strokeDasharray="2,2" />
      <rect x="90" y="10" width="28" height="14" rx="3" fill="#fef9c3" stroke="#eab308" strokeWidth="1" />
      <text x="104" y="20" textAnchor="middle" fontSize="6.5" fill="#ca8a04">Search</text>
      <rect x="90" y="29" width="28" height="14" rx="3" fill="#dbeafe" stroke="#3b82f6" strokeWidth="1" />
      <text x="104" y="39" textAnchor="middle" fontSize="6.5" fill="#1d4ed8">API</text>
      <rect x="90" y="48" width="28" height="14" rx="3" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1" />
      <text x="104" y="58" textAnchor="middle" fontSize="6.5" fill="#16a34a">DB</text>
      <text x="122" y="37" textAnchor="middle" fontSize="8" fill="#9ca3af">→</text>
      <rect x="128" y="26" width="36" height="20" rx="4" fill="#ecfdf5" stroke="#10b981" strokeWidth="1.2" />
      <text x="146" y="35" textAnchor="middle" fontSize="7" fontWeight="700" fill="#059669">Verify</text>
      <text x="146" y="44" textAnchor="middle" fontSize="7" fill="#10b981">→ Answer</text>
    </svg>
  );
}

// ── Visualization Map ──────────────────────────────────────────────────────────

export const AI_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "ai-engineering-fundamentals": AiEngineeringFundamentalsViz,
  "python-for-ai":               PythonForAiViz,
  "ml-fundamentals":             MlFundamentalsViz,
  "supervised-learning":         MlFundamentalsViz,
  "unsupervised-learning":       SupervisedLearningViz,
  "model-evaluation":            MlFundamentalsViz,
  "overfitting-underfitting":    MlFundamentalsViz,
  "feature-engineering":         MlFundamentalsViz,
  "neural-networks":             NeuralNetworkViz,
  "transformer-architecture":    TransformerViz,
  "embeddings-fundamentals":     EmbeddingsViz,
  "model-finetuning-concepts":   LlmViz,
  "large-language-models":       LlmViz,
  "tokens-tokenization":         TokensViz,
  "context-windows":             ContextWindowViz,
  "pretraining-instruction-tuning": TransformerViz,
  "temperature-sampling":        TemperatureSamplingViz,
  "model-selection":             LlmViz,
  "llm-inference":               StreamingViz,
  "prompt-engineering-fundamentals": PromptEngineeringViz,
  "system-prompts":              PromptEngineeringViz,
  "few-shot-prompting":          PromptEngineeringViz,
  "chain-of-thought":            ChainOfThoughtViz,
  "structured-outputs":          StructuredOutputsViz,
  "prompt-templates":            StructuredOutputsViz,
  "prompt-versioning":           StructuredOutputsViz,
  "prompt-security":             AiSecurityViz,
  "semantic-search":             EmbeddingsViz,
  "vector-databases":            VectorDatabaseViz,
  "chunking-strategies":         ChunkingViz,
  "hybrid-search":               HybridSearchViz,
  "rag-fundamentals":            RagViz,
  "document-ingestion":          ChunkingViz,
  "embedding-pipelines":         ChunkingViz,
  "rag-prompting":               RagViz,
  "rag-evaluation":              EvaluationViz,
  "advanced-rag":                RagViz,
  "production-rag":              RagViz,
  "tool-calling":                ToolCallingViz,
  "structured-tool-schemas":     ToolCallingViz,
  "api-database-tools":          ToolCallingViz,
  "tool-security":               AiSecurityViz,
  "ai-agent-fundamentals":       AgentViz,
  "agent-loops":                 AgentViz,
  "agent-memory":                AgentViz,
  "multi-agent-systems":         MultiAgentViz,
  "agent-workflows":             MultiAgentViz,
  "human-in-the-loop":           MultiAgentViz,
  "agent-reliability":           AgentViz,
  "ai-evaluation-fundamentals":  EvaluationViz,
  "golden-datasets":             EvaluationViz,
  "llm-as-judge":                LlmJudgeViz,
  "hallucination-detection":     HallucinationViz,
  "retrieval-evaluation":        EvaluationViz,
  "regression-testing-ai":       EvaluationViz,
  "evaluation-pipelines":        EvaluationViz,
  "ai-application-architecture": AiArchitectureViz,
  "model-apis":                  AiArchitectureViz,
  "streaming-responses":         StreamingViz,
  "conversation-memory":         StreamingViz,
  "ai-caching":                  AiArchitectureViz,
  "token-cost-optimization":     AiArchitectureViz,
  "ai-observability":            AiObservabilityViz,
  "prompt-injection-security":   AiSecurityViz,
  "ai-deployment":               AiArchitectureViz,
  "production-ai-architecture":  AiArchitectureViz,
  "build-chatbot":               ChatbotProjectViz,
  "build-rag-app":               RagProjectViz,
  "build-pdf-assistant":         RagProjectViz,
  "build-ai-agent":              AgentProjectViz,
  "build-coding-assistant":      AgentProjectViz,
  "build-ai-search":             AgentProjectViz,
  "build-multi-agent":           AgentProjectViz,
  "build-production-ai":         AgentProjectViz,
};
