// Left visual panel for Languva login — illustration + language bubbles + stats
import { LANGUVA_STATS } from "@/lib/languages/config";
import { Globe, BookOpen, Users, Shield } from "lucide-react";

const SPEECH_BUBBLES = [
  { flag: "🇫🇷", text: "Bonjour!", top: "18%",  left: "8%"  },
  { flag: "🇩🇪", text: "Hallo!",   top: "32%",  left: "42%" },
  { flag: "🇪🇸", text: "Hola!",    top: "50%",  left: "12%" },
  { flag: "🇮🇳", text: "नमस्ते!",  top: "62%",  left: "44%" },
  { flag: "🇯🇵", text: "こんにちは!", top: "75%", left: "6%"  },
];

const STATS = [
  { icon: Globe,    value: `${LANGUVA_STATS.totalLanguages} Languages`, sub: `${LANGUVA_STATS.indianCount} Indian + ${LANGUVA_STATS.globalCount} global` },
  { icon: BookOpen, value: LANGUVA_STATS.cefrRange,                    sub: "All levels"             },
  { icon: Users,    value: LANGUVA_STATS.learnerCount,                  sub: "Learners worldwide"     },
  { icon: Shield,   value: "Safe & Secure",                             sub: "Your data is protected" },
];

export default function AuthVisualPanel() {
  return (
    <div className="hidden lg:flex lg:w-[45%] xl:w-[42%] relative flex-col overflow-hidden">
      {/* Full-height scenic background */}
      <div className="absolute inset-0 bg-gradient-to-br from-indigo-900 via-purple-900 to-slate-900" />

      {/* Scenic overlay illustration */}
      <div className="absolute inset-0 overflow-hidden">
        {/* Sky gradient */}
        <div className="absolute inset-0 bg-gradient-to-b from-blue-400/30 via-purple-500/20 to-transparent" />
        {/* Cityscape silhouette */}
        <svg className="absolute bottom-[200px] left-0 right-0 w-full opacity-25" viewBox="0 0 600 160" preserveAspectRatio="none">
          <path d="M0 160 L0 80 L30 80 L30 40 L60 40 L60 60 L90 60 L90 20 L120 20 L120 60 L150 60 L150 70 L180 70 L180 30 L210 30 L210 70 L240 70 L240 50 L280 50 L280 80 L320 80 L320 40 L360 40 L360 70 L400 70 L400 30 L440 30 L440 60 L480 60 L480 80 L520 80 L520 50 L560 50 L560 90 L600 90 L600 160 Z" fill="white" />
        </svg>
        {/* Mountain silhouette */}
        <svg className="absolute bottom-[190px] left-0 right-0 w-full opacity-15" viewBox="0 0 600 120" preserveAspectRatio="none">
          <path d="M0 120 L150 20 L300 80 L450 10 L600 70 L600 120 Z" fill="#e2e8f0" />
        </svg>
        {/* Cherry blossoms decoration */}
        <div className="absolute top-16 right-10 text-4xl opacity-40">🌸</div>
        <div className="absolute top-28 right-24 text-2xl opacity-30">🌸</div>
        <div className="absolute top-10 left-8 text-2xl opacity-30">🏛️</div>
      </div>

      {/* Logo */}
      <div className="relative z-10 px-8 pt-8 flex items-center gap-2.5">
        <div className="w-9 h-9 rounded-xl bg-languva-600 flex items-center justify-center shadow-lg">
          <BookOpen size={16} className="text-white" />
        </div>
        <span className="font-bold text-white text-lg tracking-tight">Languva</span>
      </div>

      {/* Main text */}
      <div className="relative z-10 flex-1 flex flex-col justify-center px-8 pb-4">
        <h2 className="text-3xl xl:text-4xl font-black text-white leading-tight mb-3">
          One Account.<br />
          A World of Languages.
        </h2>
        <p className="text-white/70 text-sm leading-relaxed max-w-xs">
          Log in to continue your learning journey and get closer to your goals.
        </p>

        {/* Learner illustration */}
        <div className="relative mt-6 h-[220px]">
          {/* Scenic background card */}
          <div className="absolute inset-0 rounded-2xl overflow-hidden bg-gradient-to-br from-blue-400/40 via-indigo-500/30 to-purple-600/40 border border-white/10">
            {/* Cobblestone street scene */}
            <div className="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-t from-stone-600/40 to-transparent" />
          </div>

          {/* Speech bubbles */}
          {SPEECH_BUBBLES.map(({ flag, text, top, left }) => (
            <div key={text}
              style={{ top, left }}
              className="absolute z-20 flex items-center gap-1.5 bg-white/90 backdrop-blur-sm shadow-md rounded-full px-3 py-1.5 text-xs font-semibold text-gray-800 whitespace-nowrap">
              <span className="text-sm">{flag}</span>
              {text}
            </div>
          ))}

          {/* Traveler figure */}
          <div className="absolute bottom-0 left-1/2 -translate-x-1/2 z-10">
            <div className="text-8xl leading-none select-none">🧑‍🎒</div>
          </div>
        </div>

        {/* Bottom motivational text */}
        <div className="mt-4">
          <p className="text-white font-black text-xl leading-tight">
            Learn Today.<br />
            Explore Tomorrow.
          </p>
        </div>
      </div>

      {/* Stats bar */}
      <div className="relative z-10 grid grid-cols-2 gap-px bg-white/10 border-t border-white/10">
        {STATS.map(({ icon: Icon, value, sub }) => (
          <div key={value} className="flex flex-col items-center justify-center py-4 px-3 text-center bg-black/20 hover:bg-black/10 transition-colors">
            <Icon size={18} className="text-white/70 mb-1" />
            <p className="text-white font-bold text-sm leading-tight">{value}</p>
            <p className="text-white/50 text-[10px] leading-tight mt-0.5">{sub}</p>
          </div>
        ))}
      </div>

      {/* Pagination dots */}
      <div className="relative z-10 flex justify-center gap-1.5 py-3">
        {[0, 1, 2].map((i) => (
          <div key={i} className={`rounded-full transition-all ${i === 0 ? "w-4 h-1.5 bg-white" : "w-1.5 h-1.5 bg-white/30"}`} />
        ))}
      </div>
    </div>
  );
}
