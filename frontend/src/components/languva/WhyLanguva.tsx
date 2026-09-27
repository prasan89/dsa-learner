import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { ChevronRight } from "lucide-react";

const WHY_CARDS = [
  {
    icon: "⚡",
    bg: "bg-purple-50",
    iconBg: "bg-languva-100",
    title: "Learn Faster",
    desc: "AI-powered lessons personalized for you.",
  },
  {
    icon: "💬",
    bg: "bg-blue-50",
    iconBg: "bg-blue-100",
    title: "Real Conversations",
    desc: "Practice speaking with AI and native voices.",
  },
  {
    icon: "🎯",
    bg: "bg-rose-50",
    iconBg: "bg-rose-100",
    title: "From A1 to C2",
    desc: "Structured paths for every level.",
  },
  {
    icon: "🏆",
    bg: "bg-yellow-50",
    iconBg: "bg-yellow-100",
    title: "Stay Motivated",
    desc: "Streaks, XP and achievements.",
  },
];

export default function WhyLanguva() {
  return (
    <section id="features" className="py-14 md:py-20 bg-gray-50/50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3 mb-8">
          <h2 className="text-2xl sm:text-3xl font-black text-gray-900">
            Why Learners Love Languva
          </h2>
          <Link href="#" className="text-sm font-semibold text-languva-600 hover:text-languva-700 flex items-center gap-1">
            See all features <ChevronRight size={15} />
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {WHY_CARDS.map(({ icon, bg, iconBg, title, desc }) => (
            <div key={title}
              className={`${bg} rounded-2xl p-5 border border-white shadow-sm hover:shadow-md transition-shadow`}>
              <div className={`w-10 h-10 ${iconBg} rounded-xl flex items-center justify-center text-xl mb-4`}>
                {icon}
              </div>
              <h3 className="font-bold text-gray-900 mb-1.5">{title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
