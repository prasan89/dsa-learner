import Link from "next/link";
import {
  Code2, BookOpen, Cpu, BarChart3, Users, FileText, Layers, TrendingUp,
  Check, ArrowRight, Github, Linkedin, Twitter, Youtube, Share2, Trophy
} from "lucide-react";

export default function HomePage() {
  return (
    <div className="min-h-screen bg-white text-gray-900">

      {/* ── Nav ── */}
      <nav className="sticky top-0 z-50 bg-white border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2 font-bold text-gray-900">
            <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center text-white">
              <Code2 size={16} />
            </div>
            <span>DSA Learner</span>
          </Link>

          <div className="hidden md:flex items-center gap-7 text-sm text-gray-600 font-medium">
            <Link href="/problems" className="hover:text-gray-900 transition-colors">Problems</Link>
            <Link href="/patterns" className="hover:text-gray-900 transition-colors">Patterns</Link>
            <Link href="/problems" className="hover:text-gray-900 transition-colors">Practice</Link>
            <Link href="/patterns" className="hover:text-gray-900 transition-colors">Learning Path</Link>
            <Link href="/wallet" className="hover:text-gray-900 transition-colors">Pricing</Link>
          </div>

          <div className="flex items-center gap-3">
            <Link href="/login" className="text-sm font-semibold text-gray-700 hover:text-gray-900 px-3 py-2 transition-colors">
              Log in
            </Link>
            <Link href="/register" className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-semibold px-4 py-2 rounded-lg transition-colors">
              Get Started
            </Link>
          </div>
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="max-w-6xl mx-auto px-6 pt-16 pb-20">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">

          {/* Left */}
          <div className="space-y-7">
            <div className="inline-flex items-center gap-2 bg-brand-50 text-brand-700 text-xs font-semibold px-3 py-1.5 rounded-full border border-brand-100">
              Master DSA. Ace Your Interviews.
            </div>

            <h1 className="text-5xl font-extrabold text-gray-900 leading-tight tracking-tight">
              From Problems<br />
              to Product Careers
            </h1>

            <p className="text-lg text-gray-500 leading-relaxed max-w-lg">
              A structured, AI-powered platform to help you master Data Structures &amp; Algorithms,
              build problem-solving skills, and crack top tech interviews.
            </p>

            <div className="flex flex-wrap gap-3">
              <Link href="/register"
                className="flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold px-6 py-3 rounded-lg transition-colors">
                Start Learning Free <ArrowRight size={16} />
              </Link>
              <Link href="/problems"
                className="flex items-center gap-2 border border-gray-200 hover:border-gray-300 text-gray-700 font-semibold px-6 py-3 rounded-lg transition-colors bg-white">
                View Sample Problem
              </Link>
            </div>

            <div className="grid grid-cols-2 gap-2 text-sm text-gray-600">
              {["Curated content", "Step-by-step explanations", "AI code review", "Track your progress"].map((item) => (
                <div key={item} className="flex items-center gap-2">
                  <Check size={14} className="text-brand-600 shrink-0" />
                  {item}
                </div>
              ))}
            </div>
          </div>

          {/* Right — product preview card */}
          <div className="relative">
            {/* Handwritten label */}
            <div className="absolute -top-6 right-8 text-brand-400 text-sm italic font-medium rotate-3 hidden lg:block">
              Learn. Practice. Master.
            </div>

            <div className="bg-white border border-gray-200 rounded-2xl shadow-xl overflow-hidden">
              {/* Card tabs */}
              <div className="flex items-center gap-1 px-4 pt-4 pb-2 border-b border-gray-100">
                {["Learn", "Practice", "Hints", "Solution"].map((tab, i) => (
                  <button key={tab} className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                    i === 0 ? "bg-brand-600 text-white" : "text-gray-500 hover:text-gray-700"
                  }`}>
                    {tab}
                  </button>
                ))}
                <span className="ml-auto text-xs font-semibold bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full">Medium</span>
              </div>

              <div className="p-4">
                <h3 className="text-sm font-bold text-gray-900 mb-3">Longest Substring Without Repeating Characters</h3>

                <div className="grid grid-cols-5 gap-0">
                  {/* Steps */}
                  <div className="col-span-2 space-y-2.5 pr-3">
                    {[
                      { n: 1, t: "Understand the pattern", s: "Sliding Window" },
                      { n: 2, t: "Learn with examples", s: "" },
                      { n: 3, t: "Practice the problem", s: "" },
                      { n: 4, t: "Get AI review", s: "" },
                      { n: 5, t: "Track mastery", s: "" },
                    ].map((step, i) => (
                      <div key={step.n} className="flex items-start gap-2">
                        <div className={`w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold shrink-0 mt-0.5 ${
                          i === 0 ? "bg-brand-600 text-white" : "bg-gray-100 text-gray-500"
                        }`}>{step.n}</div>
                        <div>
                          <p className={`text-xs font-medium ${i === 0 ? "text-gray-900" : "text-gray-500"}`}>{step.t}</p>
                          {step.s && <p className="text-xs text-brand-500">{step.s}</p>}
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Code preview */}
                  <div className="col-span-3 bg-gray-900 rounded-xl p-3 overflow-hidden">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex gap-1">
                        <div className="w-2 h-2 rounded-full bg-red-400"></div>
                        <div className="w-2 h-2 rounded-full bg-yellow-400"></div>
                        <div className="w-2 h-2 rounded-full bg-green-400"></div>
                      </div>
                      <span className="text-xs text-gray-400 bg-gray-800 px-2 py-0.5 rounded text-[10px]">Java ▾</span>
                    </div>
                    <pre className="text-[9px] text-green-300 font-mono leading-relaxed overflow-hidden">
{`public int lengthOf
  LongestSubstring(String s) {
  Map<Character, Integer>
    lastSeen = new HashMap<>();
  int left = 0, max = 0;

  for (int right = 0;
       right < s.length(); right++) {
    char c = s.charAt(right);
    if (lastSeen.containsKey(c) &&
        lastSeen.get(c) >= left) {
      left = lastSeen.get(c) + 1;
    }
    lastSeen.put(c, right);
    max = Math.max(max,
          right - left + 1);
  }
  return max;
}`}
                    </pre>
                    <div className="mt-2 flex justify-end">
                      <Link href="/problems/longest-substring-without-repeating"
                        className="flex items-center gap-1.5 bg-white text-gray-800 text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors">
                        <ArrowRight size={11} /> Try it yourself
                      </Link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── Stats ── */}
      <section className="border-y border-gray-100 bg-gray-50">
        <div className="max-w-6xl mx-auto px-6 py-10 grid grid-cols-2 md:grid-cols-4 gap-6">
          {[
            { icon: <Users size={24} className="text-brand-600" />, value: "10,000+", label: "Learners" },
            { icon: <FileText size={24} className="text-brand-600" />, value: "250+", label: "Curated Problems" },
            { icon: <Layers size={24} className="text-brand-600" />, value: "10", label: "Core Patterns" },
            { icon: <BarChart3 size={24} className="text-brand-600" />, value: "3x", label: "Faster Interview Prep" },
          ].map((stat) => (
            <div key={stat.label} className="flex items-center gap-4">
              <div className="p-2 bg-brand-50 rounded-lg shrink-0">{stat.icon}</div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                <p className="text-sm text-gray-500">{stat.label}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Why DSA Learner ── */}
      <section className="max-w-6xl mx-auto px-6 py-20">
        <div className="mb-10">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Why DSA Learner?</h2>
          <p className="text-gray-500">Everything you need to go from confused to confident.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {[
            {
              icon: <BookOpen size={24} className="text-white" />,
              bg: "bg-brand-600",
              title: "Structured Learning",
              desc: "Learn patterns, not just solutions. Step-by-step explanations for every problem.",
            },
            {
              icon: <Code2 size={24} className="text-white" />,
              bg: "bg-emerald-500",
              title: "Hands-on Practice",
              desc: "250+ carefully curated problems with progressive difficulty.",
            },
            {
              icon: <Cpu size={24} className="text-white" />,
              bg: "bg-orange-500",
              title: "AI Code Review",
              desc: "Get instant feedback, improve your code, and learn better practices.",
            },
            {
              icon: <BarChart3 size={24} className="text-white" />,
              bg: "bg-blue-500",
              title: "Track Your Progress",
              desc: "Monitor your learning journey, build streaks, and achieve mastery.",
            },
          ].map((card) => (
            <div key={card.title} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow">
              <div className={`w-12 h-12 ${card.bg} rounded-xl flex items-center justify-center mb-4`}>
                {card.icon}
              </div>
              <h3 className="font-bold text-gray-900 mb-2">{card.title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{card.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── Learning Path ── */}
      <section className="bg-gray-50 border-y border-gray-100">
        <div className="max-w-6xl mx-auto px-6 py-20">
          <div className="mb-10">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Follow a proven learning path</h2>
            <p className="text-gray-500">From fundamentals to advanced, with real interview focus.</p>
          </div>

          <div className="flex flex-col md:flex-row items-start md:items-center gap-0">
            {[
              {
                icon: <BookOpen size={24} className="text-brand-600" />,
                step: "1. Learn the Basics",
                desc: "Arrays, Strings, Linked Lists, Trees, Graphs...",
              },
              {
                icon: <Share2 size={24} className="text-brand-600" />,
                step: "2. Master Patterns",
                desc: "Sliding Window, Two Pointers, DP, BFS/DFS...",
              },
              {
                icon: <Code2 size={24} className="text-brand-600" />,
                step: "3. Solve Problems",
                desc: "Practice with hints and AI feedback",
              },
              {
                icon: <Trophy size={24} className="text-yellow-500" />,
                step: "4. Crack Interviews",
                desc: "Be ready for Google, Amazon, Microsoft and more",
              },
            ].map((item, i) => (
              <div key={item.step} className="flex-1 flex items-start md:flex-col gap-4 md:gap-0 w-full">
                <div className="flex items-center gap-3 w-full">
                  <div className="w-12 h-12 bg-white border border-gray-200 rounded-xl flex items-center justify-center shadow-sm shrink-0">
                    {item.icon}
                  </div>
                  {i < 3 && (
                    <div className="hidden md:block flex-1 h-px border-t-2 border-dashed border-gray-300 mx-2" />
                  )}
                </div>
                <div className="md:mt-4">
                  <p className="font-bold text-gray-900 text-sm">{item.step}</p>
                  <p className="text-xs text-gray-500 mt-1 max-w-[160px]">{item.desc}</p>
                </div>
                {i < 3 && (
                  <ArrowRight size={16} className="text-gray-300 mt-3 shrink-0 hidden md:hidden" />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Testimonials ── */}
      <section className="max-w-6xl mx-auto px-6 py-20">
        <div className="mb-10">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Loved by learners</h2>
          <p className="text-gray-500">Join thousands of developers who are improving every day.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            {
              quote: "The pattern-based approach really helped me think like an interviewer. Got my offer from a top product company!",
              name: "Arjun K.",
              role: "Software Engineer @ Google",
              initials: "AK",
              color: "bg-orange-200 text-orange-700",
            },
            {
              quote: "Clean explanations, great problems and the AI review is a game changer. Highly recommended!",
              name: "Priya S.",
              role: "SDE @ Amazon",
              initials: "PS",
              color: "bg-pink-200 text-pink-700",
            },
            {
              quote: "Went from struggling with DSA to solving medium problems consistently in 2 months. Amazing platform!",
              name: "Rahul M.",
              role: "SDE @ Microsoft",
              initials: "RM",
              color: "bg-blue-200 text-blue-700",
            },
          ].map((t) => (
            <div key={t.name} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
              <div className="flex items-center gap-3 mb-4">
                <div className={`w-10 h-10 rounded-full ${t.color} flex items-center justify-center text-sm font-bold shrink-0`}>
                  {t.initials}
                </div>
                <div>
                  <p className="font-semibold text-sm text-gray-900">{t.name}</p>
                  <p className="text-xs text-gray-500">{t.role}</p>
                </div>
              </div>
              <p className="text-sm text-gray-600 leading-relaxed mb-4">"{t.quote}"</p>
              <div className="flex gap-0.5">
                {Array.from({ length: 5 }).map((_, i) => (
                  <span key={i} className="text-yellow-400 text-sm">★</span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA Banner ── */}
      <section className="max-w-6xl mx-auto px-6 pb-20">
        <div className="bg-brand-50 border border-brand-100 rounded-2xl px-8 py-10 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-5">
            <div className="w-14 h-14 bg-brand-600 rounded-2xl flex items-center justify-center shrink-0">
              <TrendingUp size={28} className="text-white" />
            </div>
            <div>
              <p className="text-xs font-bold text-brand-600 uppercase tracking-wider mb-1">Ready to start?</p>
              <h3 className="text-2xl font-bold text-gray-900">Invest in your future today</h3>
              <p className="text-sm text-gray-500 mt-1">Join DSA Learner and take the first step towards your dream tech career.</p>
            </div>
          </div>
          <div className="shrink-0 text-center">
            <Link href="/register"
              className="flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold px-6 py-3 rounded-lg transition-colors whitespace-nowrap">
              Start Learning Free <ArrowRight size={16} />
            </Link>
            <p className="text-xs text-gray-400 mt-2">No credit card required</p>
          </div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="border-t border-gray-100 bg-white">
        <div className="max-w-6xl mx-auto px-6 py-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 font-bold text-gray-900 mb-1">
              <div className="w-7 h-7 bg-brand-600 rounded-md flex items-center justify-center text-white">
                <Code2 size={13} />
              </div>
              <span className="text-sm">DSA Learner</span>
            </div>
            <p className="text-xs text-gray-400">Learn. Practice. Master.</p>
          </div>

          <div className="flex flex-wrap justify-center gap-5 text-sm text-gray-500">
            {["About", "Pricing", "Blog", "Contact", "Privacy", "Terms"].map((item) => (
              <Link key={item} href="#" className="hover:text-gray-700 transition-colors">{item}</Link>
            ))}
          </div>

          <div className="flex items-center gap-3 text-gray-400">
            <a href="#" className="hover:text-gray-700 transition-colors"><Github size={18} /></a>
            <a href="#" className="hover:text-gray-700 transition-colors"><Linkedin size={18} /></a>
            <a href="#" className="hover:text-gray-700 transition-colors"><Twitter size={18} /></a>
            <a href="#" className="hover:text-gray-700 transition-colors"><Youtube size={18} /></a>
          </div>
        </div>
      </footer>
    </div>
  );
}
