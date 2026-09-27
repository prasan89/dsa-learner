import { Bot, MessageCircle, BookOpen, Globe, Trophy } from "lucide-react";

const FEATURES = [
  {
    icon: Bot,
    color: "bg-purple-50 text-purple-600",
    title: "AI Tutor",
    desc: "Personalized feedback",
  },
  {
    icon: MessageCircle,
    color: "bg-blue-50 text-blue-600",
    title: "Real Conversations",
    desc: "Speak from day 1",
  },
  {
    icon: BookOpen,
    color: "bg-emerald-50 text-emerald-600",
    title: "Structured Curriculum",
    desc: "A1 → C2",
  },
  {
    icon: Globe,
    color: "bg-orange-50 text-orange-600",
    title: "14 Languages",
    desc: "5 Indian + 9 global",
  },
  {
    icon: Trophy,
    color: "bg-yellow-50 text-yellow-600",
    title: "Gamified Learning",
    desc: "Streaks, XP, achievements",
  },
];

export default function FeatureStrip() {
  return (
    <section className="border-y border-gray-100 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-2.5">
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2">
          {FEATURES.map(({ icon: Icon, color, title, desc }) => (
            <div key={title}
              className="flex items-center gap-3 px-3 py-3 rounded-xl hover:bg-gray-50 transition-colors">
              <div className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${color}`}>
                <Icon size={17} />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-900 truncate">{title}</p>
                <p className="text-xs text-gray-500 truncate">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
