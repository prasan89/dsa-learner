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
 
