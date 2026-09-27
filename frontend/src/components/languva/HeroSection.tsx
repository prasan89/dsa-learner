import Link from "next/link";
import { ArrowRight, Play } from "lucide-react";

// Learner avatar placeholders
const AVATARS = ["🧑🏻", "👩🏽", "🧑🏿", "👩🏼", "🧑🏾"];

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden bg-gradient-to-br from-slate-50 via-white to-languva-50/30 pt-3 pb-2 md:pt-5 md:pb-4">
      {/* Background blobs */}
      <div className="absolute top-0 right-0 w-[600px] h-[600px] rounded-full bg-languva-100/40 blur-[120px] pointer-events-none -translate-y-1/3 translate-x-1/4" />
      <div className="absolute bottom-0 left-0 w-[400px] h-[400px] rounded-full bg-blue-100/40 blur-[100px] pointer-events-none translate-y-1/3 -translate-x-1/4" />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 lg:gap-7 items-center">

          {/* Left — text */}
          <div className="order-2 lg:order-1">
            {/* Badge */}
            <div className="inline-flex items-center gap-2 bg-languva-50 text-languva-700 text-xs font-semibold px-3 py-1.5 rounded-full border border-languva-200 mb-4">
              <span className="text-base">🌍</span>
              Learn 14 Languages. Explore the World.
            </div>

            {/* Headline */}
            <h1 className="text-4xl sm:text-5xl xl:text-[3.5rem] font-black text-gray-900 leading-[1.08] tracking-tight mb-4">
              Speak. Connect.<br />
              <span className="text-languva-600">Go Further.</span>
            </h1>

            <p className="text-base sm:text-lg text-gray-500 leading-relaxed max-w-md mb-5">
              Learn 5 Indian and 9 global languages with AI-powered lessons, real
              conversations and personalized learning paths — from A1 to C2.
            </p>

            {/* CTAs */}
            <div className="flex flex-wrap gap-3 mb-5">
              <Link href="/register"
                className="inline-flex items-center gap-2 bg-languva-600 hover:bg-languva-700 text-white font-bold px-6 py-3.5 rounded-xl text-sm transition-all shadow-lg shadow-languva-200">
                Start Learning Free <ArrowRight size={16} />
              </Link>
              <Link href="#pricing"
                className="inline-flex items-center gap-2 border border-gray-200 hover:border-gray-300 bg-white text-gray-700 font-semibold px-6 py-3.5 rounded-xl text-sm transition-colors">
                View Plans
              </Link>
            </div>

            {/* Social proof */}
            <div className="flex items-center gap-3">
              <div className="flex -space-x-2">
                {AVATARS.map((a, i) => (
                  <div key={i}
                    className="w-8 h-8 rounded-full border-2 border-white bg-languva-100 flex items-center justify-center text-base shadow-sm">
                    {a}
                  </div>
                ))}
              </div>
              <p className="text-sm text-gray-500 font-medium">
                Join <span className="text-gray-900 font-semibold">500,000+</span> learners worldwide
              </p>
            </div>
          </div>

          {/* Right — hero artwork */}
          <div className="order-1 lg:order-2 relative flex items-center justify-center">
            <div className="relative w-full max-w-2xl mx-auto">
              <div className="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-sky-100 via-blue-50 to-indigo-100 shadow-[0_24px_70px_rgba(79,70,229,0.16)]">
                <img
                  src="/images/languva-hero.webp"
                  alt="Languva learner exploring languages around the world"
                  className="block w-full h-auto object-cover"
                />

                {/* Demo CTA intentionally overlays the artwork, matching the reference composition. */}
                <div className="absolute bottom-5 right-5 flex items-center gap-3 bg-white/95 backdrop-blur-md shadow-xl rounded-2xl px-4 py-3 border border-white/80">
                  <div className="w-9 h-9 rounded-full bg-languva-600 flex items-center justify-center shrink-0">
                    <Play size={14} className="text-white ml-0.5" fill="white" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-gray-900 leading-tight">Watch How Languva Works</p>
                    <p className="text-[10px] text-gray-400 leading-tight mt-0.5">2 min</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function SpeechBubble({
  text, flag, className, text2,
}: { text: string; flag: string; className?: string; label?: string; text2?: string }) {
  return (
    <div className={`absolute z-20 ${className}`}>
      <div className="flex items-center gap-1.5 bg-white/90 backdrop-blur-sm shadow-md rounded-full px-3 py-1.5 border border-white/60 text-sm font-semibold text-gray-800 whitespace-nowrap">
        <span className="text-base">{flag}</span>
        <span>{text2 ?? text}</span>
      </div>
    </div>
  );
}

function GlobeSvg() {
  return (
    <svg viewBox="0 0 200 200" className="w-52 h-52 sm:w-56 sm:h-56 text-blue-300" fill="none" stroke="currentColor" strokeWidth="1">
      <circle cx="100" cy="100" r="90" />
      <ellipse cx="100" cy="100" rx="40" ry="90" />
      <ellipse cx="100" cy="100" rx="70" ry="90" />
      <line x1="10" y1="100" x2="190" y2="100" />
      <line x1="100" y1="10" x2="100" y2="190" />
      <path d="M 10 70 Q 100 55 190 70" />
      <path d="M 10 130 Q 100 145 190 130" />
    </svg>
  );
}

function LearnerIllustration() {
  return (
    <div className="w-full h-full flex items-end justify-center pb-0">
      {/* Stylized traveler figure with backpack */}
      <svg viewBox="0 0 220 280" className="w-40 h-52 sm:w-44 sm:h-56" fill="none">
        {/* Body */}
        <ellipse cx="110" cy="210" rx="35" ry="50" fill="#a78bfa" opacity="0.9" />
        {/* Head */}
        <circle cx="110" cy="130" r="32" fill="#fcd9b6" />
        {/* Hair */}
        <ellipse cx="110" cy="110" rx="32" ry="18" fill="#7c3aed" />
        <ellipse cx="80" cy="125" rx="10" ry="22" fill="#7c3aed" />
        <ellipse cx="140" cy="125" rx="10" ry="22" fill="#7c3aed" />
        {/* Eyes */}
        <ellipse cx="100" cy="133" rx="5" ry="6" fill="#1e1b4b" />
        <ellipse cx="120" cy="133" rx="5" ry="6" fill="#1e1b4b" />
        <circle cx="102" cy="131" r="2" fill="white" />
        <circle cx="122" cy="131" r="2" fill="white" />
        {/* Smile */}
        <path d="M 100 145 Q 110 155 120 145" stroke="#c2410c" strokeWidth="2" fill="none" strokeLinecap="round" />
        {/* Arms */}
        <path d="M 80 195 Q 60 175 65 155" stroke="#fcd9b6" strokeWidth="12" strokeLinecap="round" fill="none" />
        <path d="M 140 195 Q 160 175 155 155" stroke="#fcd9b6" strokeWidth="12" strokeLinecap="round" fill="none" />
        {/* Backpack */}
        <rect x="90" y="165" width="40" height="50" rx="8" fill="#5b21b6" />
        <rect x="95" y="175" width="30" height="20" rx="4" fill="#7c3aed" />
        {/* Legs */}
        <rect x="92" y="250" width="14" height="25" rx="7" fill="#4c1d95" />
        <rect x="114" y="250" width="14" height="25" rx="7" fill="#4c1d95" />
      </svg>
    </div>
  );
}
