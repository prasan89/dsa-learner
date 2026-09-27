import Link from "next/link";
import { ArrowRight } from "lucide-react";

export default function JourneyCta() {
  return (
    <section className="py-14 md:py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        {/* Large scenic CTA panel */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-languva-600 via-purple-700 to-indigo-800 min-h-[280px] sm:min-h-[340px] flex flex-col justify-end p-8 sm:p-12">
          {/* Background scenic elements */}
          <div className="absolute inset-0 overflow-hidden pointer-events-none">
            {/* Mountain silhouette */}
            <svg className="absolute bottom-0 left-0 right-0 w-full opacity-20" viewBox="0 0 1440 200" preserveAspectRatio="none">
              <path d="M0 200 L240 80 L480 160 L720 40 L960 120 L1200 60 L1440 140 L1440 200 Z" fill="white" />
            </svg>
            {/* Cherry blossoms */}
            {["top-4 right-12", "top-10 right-32", "top-2 right-48"].map((pos, i) => (
              <div key={i} className={`absolute ${pos} text-3xl opacity-40`}>🌸</div>
            ))}
            {/* Traveler silhouette */}
            <div className="absolute bottom-8 right-8 sm:right-16 text-6xl sm:text-7xl opacity-70">
              🧳
            </div>
            {/* Cursive text decoration */}
            <div className="absolute top-8 right-6 sm:right-12 font-serif italic text-white/30 text-lg sm:text-2xl rotate-[-8deg]">
              Languages Bring Us Closer
            </div>
          </div>

          {/* Text content */}
          <div className="relative z-10 max-w-xl">
            <h2 className="text-3xl sm:text-4xl xl:text-5xl font-black text-white leading-tight mb-4">
              Learn a language.<br />
              Live a bigger life.
            </h2>
            <p className="text-white/80 text-sm sm:text-base leading-relaxed mb-6 max-w-sm">
              Open doors to new cultures, people and opportunities with Languva.
            </p>
            <Link href="/register"
              className="inline-flex items-center gap-2 bg-white text-languva-700 font-bold px-6 py-3 rounded-xl text-sm hover:bg-languva-50 transition-colors shadow-lg">
              Start Your Journey <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
