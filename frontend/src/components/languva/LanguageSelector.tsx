"use client";

import { useState } from "react";
import Link from "next/link";
import { ChevronRight } from "lucide-react";
import { LANGUAGES, INDIAN_LANGUAGES, GLOBAL_LANGUAGES, LANGUVA_STATS, type Language } from "@/lib/languages/config";

type Tab = "indian" | "global";

export default function LanguageSelector() {
  const [activeTab, setActiveTab] = useState<Tab>("indian");
  const displayed = activeTab === "indian" ? INDIAN_LANGUAGES : GLOBAL_LANGUAGES;

  return (
    <section id="languages" className="py-14 md:py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3 mb-7">
          <h2 className="text-2xl sm:text-3xl font-black text-gray-900">
            Cho<span className="text-languva-600">O</span>Se Your Language
          </h2>
          <Link href="#" className="text-sm font-semibold text-languva-600 hover:text-languva-700 flex items-center gap-1 whitespace-nowrap">
            View all {LANGUVA_STATS.totalLanguages} languages <ChevronRight size={15} />
          </Link>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-6">
          <TabBtn active={activeTab === "indian"} onClick={() => setActiveTab("indian")}>
            Indian Languages ({LANGUVA_STATS.indianCount})
          </TabBtn>
          <TabBtn active={activeTab === "global"} onClick={() => setActiveTab("global")}>
            Global Languages ({LANGUVA_STATS.globalCount})
          </TabBtn>
        </div>

        {/* Cards row — horizontally scrollable on mobile */}
        <div className="flex gap-3 overflow-x-auto pb-3 snap-x snap-mandatory scrollbar-none">
          {displayed.map((lang) => (
            <LanguageCard key={lang.code} lang={lang} />
          ))}
        </div>
      </div>
    </section>
  );
}

function TabBtn({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
        active
          ? "bg-languva-600 text-white shadow-sm"
          : "bg-gray-100 text-gray-600 hover:bg-gray-200"
      }`}
    >
      {children}
    </button>
  );
}

function LanguageCard({ lang }: { lang: Language }) {
  return (
    <Link href={lang.route}
      className="snap-start shrink-0 w-[120px] sm:w-[130px] group cursor-pointer">
      {/* Image area */}
      <div className="w-full aspect-square rounded-xl overflow-hidden bg-gradient-to-br from-languva-100 to-blue-100 mb-2 relative border border-gray-100 group-hover:shadow-md transition-shadow">
        {/* Placeholder gradient with landmark icon */}
        <div className="absolute inset-0 flex items-center justify-center text-4xl opacity-60">
          {getLandmarkEmoji(lang.code)}
        </div>
        {/* Flag badge */}
        <div className="absolute bottom-1.5 left-1.5 text-xl leading-none">
          {lang.flag}
        </div>
      </div>

      <p className="text-sm font-bold text-gray-900 mb-0.5">{lang.name}</p>
      <p className="text-xs text-gray-500 mb-0.5">{lang.cefrRange}</p>
      <p className="text-[11px] text-gray-400">{lang.learnerCount}</p>
    </Link>
  );
}

function getLandmarkEmoji(code: string): string {
  const map: Record<string, string> = {
    hindi: "🕌", tamil: "🛕", telugu: "🏛️", marathi: "🕍", kannada: "🏯",
    english: "🎡", german: "🏰", french: "🗼", spanish: "💃", italian: "🏟️",
    portuguese: "⛵", japanese: "⛩️", korean: "🏮", mandarin: "🐲",
  };
  return map[code] ?? "🌍";
}
