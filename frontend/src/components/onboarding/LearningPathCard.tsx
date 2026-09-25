"use client";

import { type LearningPath } from "@/lib/learningPath";

interface Feature {
  text: string;
}

interface PathConfig {
  id: LearningPath;
  emoji: string;
  title: string;
  tagline: string;
  features: Feature[];
  cta: string;
  accentFrom: string;
  accentTo: string;
  iconBg: string;
  ctaBg: string;
  ctaHover: string;
  selectedRing: string;
  checkColor: string;
}

export const PATH_CONFIGS: PathConfig[] = [
  {
    id: "dsa",
    emoji: "💻",
    title: "DSA & Coding",
    tagline: "Master problem solving and technical interviews",
    features: [
      { text: "Data Structures & Algorithms" },
      { text: "Coding Practice" },
      { text: "Interview Preparation" },
      { text: "System Design" },
      { text: "AI Coding Mentor" },
    ],
    cta: "Continue with DSA →",
    accentFrom: "from-indigo-50",
    accentTo: "to-blue-50",
    iconBg: "bg-indigo-100",
    ctaBg: "bg-brand-600",
    ctaHover: "hover:bg-brand-700",
    selectedRing: "ring-2 ring-brand-500 border-brand-400",
    checkColor: "text-brand-600",
  },
  {
    id: "languages",
    emoji: "🌍",
    title: "Languages",
    tagline: "Learn to speak confidently in real conversations",
    features: [
      { text: "German, French, Korean, Spanish" },
      { text: "AI Conversation Practice" },
      { text: "Pronunciation & Speaking" },
      { text: "Vocabulary & Grammar" },
      { text: "Native Audio Dialogues" },
    ],
    cta: "Continue with Languages →",
    accentFrom: "from-emerald-50",
    accentTo: "to-teal-50",
    iconBg: "bg-emerald-100",
    ctaBg: "bg-emerald-600",
    ctaHover: "hover:bg-emerald-700",
    selectedRing: "ring-2 ring-emerald-500 border-emerald-400",
    checkColor: "text-emerald-600",
  },
];

interface Props {
  config: PathConfig;
  selected: boolean;
  onSelect: (id: LearningPath) => void;
}

export default function LearningPathCard({ config, selected, onSelect }: Props) {
  return (
    <button
      onClick={() => onSelect(config.id)}
      className={`
        w-full text-left rounded-2xl border p-6 transition-all duration-200 cursor-pointer
        bg-gradient-to-br ${config.accentFrom} ${config.accentTo}
        ${selected
          ? `${config.selectedRing} shadow-lg scale-[1.02]`
          : "border-gray-200 hover:border-gray-300 hover:shadow-md hover:scale-[1.01]"
        }
      `}
    >
      {/* Header */}
      <div className="flex items-start gap-3 mb-4">
        <div className={`w-11 h-11 rounded-xl ${config.iconBg} flex items-center justify-center text-2xl shrink-0`}>
          {config.emoji}
        </div>
        <div>
          <h3 className="font-bold text-gray-900 text-lg leading-tight">{config.title}</h3>
          <p className="text-sm text-gray-500 mt-0.5 leading-snug">{config.tagline}</p>
        </div>
      </div>

      {/* Features */}
      <ul className="space-y-1.5 mb-5">
        {config.features.map((f) => (
          <li key={f.text} className="flex items-center gap-2 text-sm text-gray-600">
            <svg className={`w-4 h-4 shrink-0 ${config.checkColor}`} viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
            </svg>
            {f.text}
          </li>
        ))}
      </ul>

      {/* CTA */}
      <div className={`w-full ${config.ctaBg} ${config.ctaHover} text-white font-semibold py-2.5 px-4 rounded-xl text-sm text-center transition-colors`}>
        {config.cta}
      </div>
    </button>
  );
}
