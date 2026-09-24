import React from "react";

interface MiniVizProps { animate?: boolean }

// ── Foundations ───────────────────────────────────────────────────────────────

export function SdFundamentalsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {["Requirements", "High Level\nDesign", "Detailed\nDesign"].map((label, i) => (
        <g key={i}>
          <rect x={4 + i * 60} y="14" width="52" height="30" rx="4"
            fill={["#eef2ff", "#ede9fe", "#f0fdf4"][i]}
            stroke={["#6366f1", "#7c3aed", "#22c55e"][i]} strokeWidth="1.2" />
          {label.split("\n").map((line, j) => (
            <text key={j} x={30 + i * 60} y={30 + i * 0 + j * 10} textAnchor="middle"
              fontSize="7.5" fontWeight="600" fill={["#4f46e5", "#6d28d9", "#16a34a"][i]}>{line}</text>
          ))}
          {i < 2 && (
            <path d={`M${56 + i * 60} 29 L${64 + i * 60} 29`} stroke="#9ca3af" strokeWidth="1.5"
              markerEnd={`url(#arrow-sf${i})`} />
          )}
          <defs>
            <marker id={`arrow-sf${i}`} markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
              <path d="M0,0 L6,3 L0,6 Z" fill="#9ca3af" />
            </marker>
          </defs>
        </g>
      ))}
    </svg>
  );
}

export function CapacityViz() {
  return (
    <div className="w-full font-mono text-[9px] leading-relaxed bg-gray-900 rounded-lg px-3 py-2 select-none overflow-hidden">
      <span className="text-gray-400">QPS =</span>
      <span className="text-green-300"> 1M users</span>
      <span className="text-gray-300"> / </span>
      <span className="text-blue-300">86400s</span>
      <br />
      <span className="text-gray-400">     ≈</span>
      <span className="text-yellow-300"> ~12 req/s</span>
      <br />
      <br />
      <span className="text-gray-400">Storage =</span>
      <span className="text-green-300"> 1M</span>
      <span className="text-gray-300"> × </span>
      <span className="text-blue-300">1KB</span>
      <br />
      <span className="text-gray-400">        ≈</span>
      <span className="text-yellow-300"> 1 GB/day</span>
    </div>
  );
}

// ── Scalability ────────────────────────────────────────────────────────────────

export function LoadBalancerViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Client */}
      <rect x="4" y="24" width="36" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="22" y="37" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Client</text>
      {/* LB */}
      <rect x="58" y="24" width="36" height="20" rx="4" fill="#6366f1" />
      <text x="76" y="37" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">LB</text>
      {/* Servers */}
      {[0, 1, 2].map(i => (
        <g key={i}>
          <rect x="128" y={10 + i * 20} width="48" height="15" rx="3"
            fill={i === 1 ? "#eef2ff" : "#f9fafb"}
            stroke={i === 1 ? "#6366f1" : "#d1d5db"} strokeWidth="1" />
          <text x="152" y={21 + i * 20} textAnchor="middle" fontSize="7.5" fontWeight="600"
            fill={i === 1 ? "#4f46e5" : "#6b7280"}>Server {i + 1}</text>
          <path d={`M94 34 L128 ${17 + i * 20}`} stroke="#6366f1" strokeWidth="1"
            strokeDasharray={i === 1 ? "none" : "2,1.5"} />
        </g>
      ))}
      <path d="M40 34 L58 34" stroke="#6366f1" strokeWidth="1.5" markerEnd="url(#arrow-lb)" />
      <defs>
        <marker id="arrow-lb" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

export function CacheViz() {
  const boxes = [
    { label: "Client", color: "#6366f1" },
    { label: "API", color: "#0ea5e9" },
    { label: "Cache", color: "#f59e0b" },
    { label: "DB", color: "#22c55e" },
  ];
  return (
    <svg viewBox="0 0 180 60" className="w-full" style={{ maxHeight: 60 }}>
      {boxes.map((b, i) => (
        <g key={b.label}>
          <rect x={4 + i * 44} y="18" width="40" height="22" rx="4"
            fill={`${b.color}22`} stroke={b.color} strokeWidth="1.2" />
          <text x={24 + i * 44} y="32" textAnchor="middle" fontSize="8" fontWeight="600" fill={b.color}>
            {b.label}
          </text>
          {i < 3 && (
            <path d={`M${44 + i * 44} 29 L${48 + i * 44} 29`} stroke={boxes[i + 1].color} strokeWidth="1.5"
              markerEnd={`url(#arrow-cv${i})`} />
          )}
          <defs>
            <marker id={`arrow-cv${i}`} markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
              <path d="M0,0 L6,3 L0,6 Z" fill={boxes[i + 1]?.color ?? b.color} />
            </marker>
          </defs>
        </g>
      ))}
      <text x="90" y="54" textAnchor="middle" fontSize="7" fill="#9ca3af">Cache-aside</text>
    </svg>
  );
}

export function ShardingViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Hash fn */}
      <rect x="64" y="4" width="52" height="18" rx="4" fill="#6366f1" />
      <text x="90" y="16" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">shard_key</text>
      {/* Shards */}
      {["Shard 0", "Shard 1", "Shard 2"].map((s, i) => (
        <g key={s}>
          <rect x={4 + i * 58} y="36" width="52" height="22" rx="4"
            fill={["#fef9c3", "#eef2ff", "#f0fdf4"][i]}
            stroke={["#eab308", "#6366f1", "#22c55e"][i]} strokeWidth="1.2" />
          <text x={30 + i * 58} y="50" textAnchor="middle" fontSize="8" fontWeight="600"
            fill={["#ca8a04", "#4f46e5", "#16a34a"][i]}>{s}</text>
          <line x1={30 + i * 58} y1="36" x2="90" y2="22" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
        </g>
      ))}
    </svg>
  );
}

// ── Databases ─────────────────────────────────────────────────────────────────

export function SqlVsNoSqlViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* SQL */}
      <rect x="4" y="4" width="80" height="58" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="44" y="18" textAnchor="middle" fontSize="9" fontWeight="700" fill="#4f46e5">SQL</text>
      <text x="44" y="30" textAnchor="middle" fontSize="7" fill="#374151">Structured</text>
      <text x="44" y="41" textAnchor="middle" fontSize="7" fill="#374151">ACID</text>
      <text x="44" y="52" textAnchor="middle" fontSize="7" fill="#374151">Relations</text>
      {/* NoSQL */}
      <rect x="96" y="4" width="80" height="58" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="136" y="18" textAnchor="middle" fontSize="9" fontWeight="700" fill="#ca8a04">NoSQL</text>
      <text x="136" y="30" textAnchor="middle" fontSize="7" fill="#374151">Flexible</text>
      <text x="136" y="41" textAnchor="middle" fontSize="7" fill="#374151">Scale-out</text>
      <text x="136" y="52" textAnchor="middle" fontSize="7" fill="#374151">Document/KV</text>
    </svg>
  );
}

export function ReplicationViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Primary */}
      <rect x="60" y="4" width="60" height="22" rx="4" fill="#6366f1" />
      <text x="90" y="19" textAnchor="middle" fontSize="9" fontWeight="700" fill="white">Primary</text>
      {/* Replicas */}
      {[0, 1].map(i => (
        <g key={i}>
          <rect x={18 + i * 96} y="42" width="60" height="22" rx="4"
            fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
          <text x={48 + i * 96} y="56" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">
            Replica {i + 1}
          </text>
          <path d={`M${48 + i * 96} 42 L${80 + i * 16} 26`} stroke="#6366f1" strokeWidth="1.2"
            strokeDasharray="2,2" />
        </g>
      ))}
      <text x="90" y="68" textAnchor="middle" fontSize="7" fill="#9ca3af">async replication</text>
    </svg>
  );
}

export function CapTheoremViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Triangle approximation with circles */}
      <circle cx="90" cy="16" r="14" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.5" />
      <text x="90" y="20" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">C</text>
      <circle cx="42" cy="56" r="14" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.5" />
      <text x="42" y="60" textAnchor="middle" fontSize="8" fontWeight="700" fill="#16a34a">A</text>
      <circle cx="138" cy="56" r="14" fill="#fef9c3" stroke="#eab308" strokeWidth="1.5" />
      <text x="138" y="60" textAnchor="middle" fontSize="8" fontWeight="700" fill="#ca8a04">P</text>
      <line x1="90" y1="30" x2="50" y2="42" stroke="#9ca3af" strokeWidth="1" />
      <line x1="90" y1="30" x2="130" y2="42" stroke="#9ca3af" strokeWidth="1" />
      <line x1="56" y1="56" x2="124" y2="56" stroke="#9ca3af" strokeWidth="1" />
    </svg>
  );
}

// ── Messaging ─────────────────────────────────────────────────────────────────

export function KafkaViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Producer */}
      <rect x="4" y="24" width="40" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="24" y="37" textAnchor="middle" fontSize="8" fontWeight="600" fill="#4f46e5">Producer</text>
      {/* Kafka */}
      <rect x="58" y="16" width="44" height="36" rx="4" fill="#1e1b4b" />
      <text x="80" y="29" textAnchor="middle" fontSize="7.5" fontWeight="700" fill="#a5b4fc">Kafka</text>
      <text x="80" y="41" textAnchor="middle" fontSize="6.5" fill="#6366f1">topic</text>
      <text x="80" y="50" textAnchor="middle" fontSize="6.5" fill="#6366f1">partitions</text>
      {/* Consumers */}
      {["C1", "C2"].map((c, i) => (
        <g key={c}>
          <rect x="128" y={16 + i * 24} width="48" height="18" rx="4"
            fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
          <text x="152" y={28 + i * 24} textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">
            Consumer {c.slice(1)}
          </text>
          <path d={`M102 ${27 + i * 6} L128 ${25 + i * 24}`} stroke="#22c55e" strokeWidth="1" strokeDasharray="2,2" />
        </g>
      ))}
      <path d="M44 34 L58 34" stroke="#6366f1" strokeWidth="1.5" markerEnd="url(#arrow-kafka)" />
      <defs>
        <marker id="arrow-kafka" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

// ── Microservices ─────────────────────────────────────────────────────────────

export function MicroservicesViz() {
  return (
    <svg viewBox="0 0 180 72" className="w-full" style={{ maxHeight: 72 }}>
      {/* API Gateway */}
      <rect x="60" y="2" width="60" height="18" rx="4" fill="#6366f1" />
      <text x="90" y="14" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">API Gateway</text>
      {/* Services */}
      {["Service A", "Service B", "Service C"].map((s, i) => (
        <g key={s}>
          <rect x={4 + i * 58} y="34" width="52" height="18" rx="4"
            fill={["#eef2ff", "#fef9c3", "#f0fdf4"][i]}
            stroke={["#6366f1", "#eab308", "#22c55e"][i]} strokeWidth="1.2" />
          <text x={30 + i * 58} y="46" textAnchor="middle" fontSize="8" fontWeight="600"
            fill={["#4f46e5", "#ca8a04", "#16a34a"][i]}>{s}</text>
          <line x1={30 + i * 58} y1="34" x2="90" y2="20" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
        </g>
      ))}
      {/* DB bubbles */}
      {[30, 88, 146].map((cx, i) => (
        <g key={cx}>
          <ellipse cx={cx} cy="63" rx="16" ry="6"
            fill={["#eef2ff", "#fef9c3", "#f0fdf4"][i]}
            stroke={["#6366f1", "#eab308", "#22c55e"][i]} strokeWidth="1" />
          <text x={cx} y="66" textAnchor="middle" fontSize="6.5" fill="#9ca3af">DB</text>
          <line x1={cx} y1="57" x2={cx} y2="52" stroke="#d1d5db" strokeWidth="1" />
        </g>
      ))}
    </svg>
  );
}

// ── Distributed Systems ────────────────────────────────────────────────────────

export function ConsistentHashingViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Ring */}
      <circle cx="90" cy="34" r="28" fill="none" stroke="#d1d5db" strokeWidth="1.5" />
      {/* Nodes on ring */}
      {[0, 90, 200, 300].map((deg, i) => {
        const rad = (deg * Math.PI) / 180;
        const x = 90 + 28 * Math.cos(rad);
        const y = 34 + 28 * Math.sin(rad);
        const colors = ["#6366f1", "#22c55e", "#f59e0b", "#ec4899"];
        return (
          <g key={deg}>
            <circle cx={x} cy={y} r="7" fill={colors[i]} />
            <text x={x} y={y + 3} textAnchor="middle" fontSize="6.5" fontWeight="700" fill="white">N{i + 1}</text>
          </g>
        );
      })}
      <text x="90" y="38" textAnchor="middle" fontSize="7" fill="#9ca3af">hash ring</text>
    </svg>
  );
}

export function DistributedSystemsViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Node boxes in triangle */}
      {[
        { x: 70, y: 2, label: "Node A", fill: "#eef2ff", stroke: "#6366f1", text: "#4f46e5" },
        { x: 8,  y: 42, label: "Node B", fill: "#f0fdf4", stroke: "#22c55e", text: "#16a34a" },
        { x: 130, y: 42, label: "Node C", fill: "#fef9c3", stroke: "#eab308", text: "#ca8a04" },
      ].map(n => (
        <g key={n.label}>
          <rect x={n.x} y={n.y} width="44" height="18" rx="4" fill={n.fill} stroke={n.stroke} strokeWidth="1.2" />
          <text x={n.x + 22} y={n.y + 12} textAnchor="middle" fontSize="8" fontWeight="600" fill={n.text}>{n.label}</text>
        </g>
      ))}
      <line x1="92" y1="20" x2="32" y2="42" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
      <line x1="92" y1="20" x2="152" y2="42" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
      <line x1="52" y1="51" x2="130" y2="51" stroke="#9ca3af" strokeWidth="1" strokeDasharray="2,2" />
    </svg>
  );
}

// ── Storage ───────────────────────────────────────────────────────────────────

export function StorageViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* S3 */}
      <rect x="4" y="14" width="50" height="40" rx="4" fill="#fef9c3" stroke="#eab308" strokeWidth="1.2" />
      <text x="29" y="29" textAnchor="middle" fontSize="8" fontWeight="700" fill="#ca8a04">S3</text>
      <text x="29" y="41" textAnchor="middle" fontSize="6.5" fill="#9ca3af">Object</text>
      <text x="29" y="50" textAnchor="middle" fontSize="6.5" fill="#9ca3af">Storage</text>
      {/* HDFS */}
      <rect x="64" y="14" width="50" height="40" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="89" y="29" textAnchor="middle" fontSize="8" fontWeight="700" fill="#4f46e5">HDFS</text>
      <text x="89" y="41" textAnchor="middle" fontSize="6.5" fill="#9ca3af">Distributed</text>
      <text x="89" y="50" textAnchor="middle" fontSize="6.5" fill="#9ca3af">FS</text>
      {/* Block */}
      <rect x="124" y="14" width="52" height="40" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="150" y="29" textAnchor="middle" fontSize="8" fontWeight="700" fill="#16a34a">Block</text>
      <text x="150" y="41" textAnchor="middle" fontSize="6.5" fill="#9ca3af">EBS / NVMe</text>
      <text x="150" y="50" textAnchor="middle" fontSize="6.5" fill="#9ca3af">Low latency</text>
    </svg>
  );
}

// ── Reliability ───────────────────────────────────────────────────────────────

export function HighAvailabilityViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {/* Traffic */}
      <rect x="4" y="24" width="32" height="20" rx="4" fill="#eef2ff" stroke="#6366f1" strokeWidth="1.2" />
      <text x="20" y="37" textAnchor="middle" fontSize="7.5" fontWeight="600" fill="#4f46e5">Users</text>
      {/* Active */}
      <rect x="60" y="4" width="52" height="20" rx="4" fill="#6366f1" />
      <text x="86" y="18" textAnchor="middle" fontSize="8" fontWeight="700" fill="white">Active</text>
      {/* Standby */}
      <rect x="60" y="44" width="52" height="20" rx="4" fill="#f0fdf4" stroke="#22c55e" strokeWidth="1.2" />
      <text x="86" y="57" textAnchor="middle" fontSize="8" fontWeight="600" fill="#16a34a">Standby</text>
      {/* Failover arrows */}
      <path d="M36 34 L60 14" stroke="#6366f1" strokeWidth="1.2" markerEnd="url(#arrow-ha)" />
      <path d="M36 34 L60 54" stroke="#22c55e" strokeWidth="1" strokeDasharray="2,2" />
      <text x="130" y="36" textAnchor="start" fontSize="7" fill="#9ca3af">Failover →</text>
      <defs>
        <marker id="arrow-ha" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
        </marker>
      </defs>
    </svg>
  );
}

// ── Observability ─────────────────────────────────────────────────────────────

export function ObservabilityViz() {
  return (
    <svg viewBox="0 0 180 68" className="w-full" style={{ maxHeight: 68 }}>
      {[
        { label: "Logs",    icon: "≡",  x: 4,  fill: "#eef2ff", stroke: "#6366f1", text: "#4f46e5" },
        { label: "Metrics", icon: "▲",  x: 66, fill: "#f0fdf4", stroke: "#22c55e", text: "#16a34a" },
        { label: "Tracing", icon: "~~", x: 128, fill: "#fef9c3", stroke: "#eab308", text: "#ca8a04" },
      ].map(p => (
        <g key={p.label}>
          <rect x={p.x} y="14" width="46" height="42" rx="4" fill={p.fill} stroke={p.stroke} strokeWidth="1.2" />
          <text x={p.x + 23} y="30" textAnchor="middle" fontSize="14" fill={p.text}>{p.icon}</text>
          <text x={p.x + 23} y="47" textAnchor="middle" fontSize="8" fontWeight="600" fill={p.text}>{p.label}</text>
        </g>
      ))}
    </svg>
  );
}

// ── Case Studies ──────────────────────────────────────────────────────────────

export function UrlShortenerViz() {
  const steps = [
    { label: "User",    color: "#6366f1" },
    { label: "API",     color: "#0ea5e9" },
    { label: "Hash",    color: "#f59e0b" },
    { label: "Store",   color: "#22c55e" },
  ];
  return (
    <svg viewBox="0 0 180 60" className="w-full" style={{ maxHeight: 60 }}>
      {steps.map((s, i) => (
        <g key={s.label}>
          <rect x={4 + i * 44} y="18" width="40" height="22" rx="4"
            fill={`${s.color}22`} stroke={s.color} strokeWidth="1.2" />
          <text x={24 + i * 44} y="32" textAnchor="middle" fontSize="8" fontWeight="600" fill={s.color}>
            {s.label}
          </text>
          {i < 3 && (
            <path d={`M${44 + i * 44} 29 L${48 + i * 44} 29`} stroke={steps[i + 1].color} strokeWidth="1.5"
              markerEnd={`url(#arrow-us${i})`} />
          )}
          <defs>
            <marker id={`arrow-us${i}`} markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
              <path d="M0,0 L6,3 L0,6 Z" fill={steps[i + 1]?.color ?? s.color} />
            </marker>
          </defs>
        </g>
      ))}
    </svg>
  );
}

export function CaseStudyViz({ labels }: { labels: string[] }) {
  return (
    <svg viewBox="0 0 180 60" className="w-full" style={{ maxHeight: 60 }}>
      {labels.slice(0, 4).map((label, i) => {
        const colors = ["#6366f1", "#0ea5e9", "#f59e0b", "#22c55e"];
        return (
          <g key={label}>
            <rect x={4 + i * 44} y="18" width="40" height="22" rx="4"
              fill={`${colors[i]}22`} stroke={colors[i]} strokeWidth="1.2" />
            <text x={24 + i * 44} y="32" textAnchor="middle" fontSize={label.length > 6 ? "7" : "8"}
              fontWeight="600" fill={colors[i]}>{label}</text>
            {i < labels.length - 1 && i < 3 && (
              <path d={`M${44 + i * 44} 29 L${48 + i * 44} 29`} stroke={colors[i + 1]} strokeWidth="1.5"
                markerEnd={`url(#arrow-cs${i})`} />
            )}
            <defs>
              <marker id={`arrow-cs${i}`} markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
                <path d="M0,0 L6,3 L0,6 Z" fill={colors[i + 1] ?? colors[i]} />
              </marker>
            </defs>
          </g>
        );
      })}
    </svg>
  );
}

export const SYSDESIGN_VISUALIZATIONS: Record<string, React.ComponentType<MiniVizProps>> = {
  "sd-fundamentals":          SdFundamentalsViz,
  "requirements-gathering":   SdFundamentalsViz,
  "capacity-estimation":      CapacityViz,
  "api-design":               SdFundamentalsViz,
  "http-networking":          SdFundamentalsViz,
  "latency-throughput":       CapacityViz,
  "horizontal-vertical-scaling": LoadBalancerViz,
  "load-balancing":           LoadBalancerViz,
  "caching-fundamentals":     CacheViz,
  "cdn":                      CacheViz,
  "reverse-proxies":          LoadBalancerViz,
  "database-scaling":         ReplicationViz,
  "sharding":                 ShardingViz,
  "replication":              ReplicationViz,
  "sql-vs-nosql":             SqlVsNoSqlViz,
  "indexing":                 SqlVsNoSqlViz,
  "transactions-acid":        CapTheoremViz,
  "isolation-levels":         CapTheoremViz,
  "cap-theorem":              CapTheoremViz,
  "consistency-models":       CapTheoremViz,
  "distributed-databases":    ReplicationViz,
  "distributed-fundamentals": DistributedSystemsViz,
  "consistent-hashing":       ConsistentHashingViz,
  "leader-election":          DistributedSystemsViz,
  "consensus":                DistributedSystemsViz,
  "idempotency-retries":      CacheViz,
  "circuit-breakers":         DistributedSystemsViz,
  "distributed-transactions": DistributedSystemsViz,
  "message-queues":           KafkaViz,
  "kafka":                    KafkaViz,
  "event-driven-architecture": KafkaViz,
  "delivery-semantics":       KafkaViz,
  "monolith-vs-microservices": MicroservicesViz,
  "api-gateway":              MicroservicesViz,
  "service-discovery":        MicroservicesViz,
  "saga-cqrs":                MicroservicesViz,
  "object-storage":           StorageViz,
  "distributed-file-systems": StorageViz,
  "data-lakes":               StorageViz,
  "high-availability":        HighAvailabilityViz,
  "fault-tolerance":          HighAvailabilityViz,
  "disaster-recovery":        HighAvailabilityViz,
  "multi-region":             HighAvailabilityViz,
  "logging-metrics":          ObservabilityViz,
  "distributed-tracing":      ObservabilityViz,
  "slo-sla-sli":              ObservabilityViz,
  "authn-authz":              () => <CaseStudyViz labels={["User", "Auth", "Token", "API"]} />,
  "rate-limiting":            () => <CaseStudyViz labels={["Req", "Bucket", "Count", "Allow"]} />,
  "api-security":             () => <CaseStudyViz labels={["Client", "HTTPS", "Validate", "API"]} />,
  "case-url-shortener":       UrlShortenerViz,
  "case-instagram":           () => <CaseStudyViz labels={["Upload", "CDN", "Feed", "DB"]} />,
  "case-whatsapp":            () => <CaseStudyViz labels={["Client", "WS", "Queue", "Store"]} />,
  "case-twitter-feed":        () => <CaseStudyViz labels={["User", "Fan-out", "Cache", "Feed"]} />,
  "case-youtube":             () => <CaseStudyViz labels={["Upload", "Encode", "CDN", "Play"]} />,
  "case-uber":                () => <CaseStudyViz labels={["Rider", "GeoIdx", "Match", "Driver"]} />,
  "case-payment-system":      () => <CaseStudyViz labels={["API", "Ledger", "MQ", "Bank"]} />,
  "case-notification-system": () => <CaseStudyViz labels={["Event", "Queue", "Push", "User"]} />,
  "case-rate-limiter":        () => <CaseStudyViz labels={["Req", "Redis", "Slide", "Allow"]} />,
  "case-search-system":       () => <CaseStudyViz labels={["Query", "Index", "Rank", "Result"]} />,
};
