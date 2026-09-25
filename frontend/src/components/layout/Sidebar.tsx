"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Home, Code2, Coffee, Layout, Bot, Dumbbell,
  RotateCcw, BarChart2, Users, CreditCard, ChevronRight,
  Zap, LogOut, Lock, Terminal, Cpu, Braces, Globe, MessageSquare,
  BookOpen, Mic, GraduationCap,
} from "lucide-react";
import { useState, useEffect } from "react";
import { authApi } from "@/lib/api/auth";
import { useSubscription } from "@/lib/useSubscription";
import { getLearningPath, setLearningPath, type LearningPath } from "@/lib/learningPath";

// ── DSA sections ────────────────────────────────────────────────────────────

const DSA_LEARN = [
  { href: "/learn",          label: "DSA",           icon: Code2 },
  { href: "/java",           label: "Java",          icon: Coffee },
  { href: "/go",             label: "Go",            icon: Terminal },
  { href: "/rust",           label: "Rust",          icon: Cpu },
  { href: "/ai-engineering", label: "AI Engineering",icon: Bot },
  { href: "/system-design",  label: "System Design", icon: Layout },
];

const DSA_PRACTICE = [
  { href: "/problems",  label: "Problems",  icon: Dumbbell },
  { href: "/patterns",  label: "Practice",  icon: Braces },
];

const DSA_AI = [
  { href: "/ai-mentor", label: "AI Mentor", icon: Bot },
];

const DSA_PROGRESS = [
  { href: "/revision",  label: "Revision",  icon: RotateCcw },
  { href: "/progress",  label: "Progress",  icon: BarChart2 },
];

// ── Languages sections ───────────────────────────────────────────────────────

const LANG_LEARN = [
  { href: "/languages/german",  label: "German",   icon: Globe },
  { href: "/languages/french",  label: "French",   icon: Globe },
  { href: "/languages/korean",  label: "Korean",   icon: Globe },
  { href: "/languages/spanish", label: "Spanish",  icon: Globe },
];

const LANG_PRACTICE = [
  { href: "/languages/speaking",    label: "Speaking",    icon: Mic },
  { href: "/languages/vocabulary",  label: "Vocabulary",  icon: BookOpen },
  { href: "/languages/grammar",     label: "Grammar",     icon: GraduationCap },
];

const LANG_AI = [
  { href: "/languages/ai-tutor", label: "AI Tutor",      icon: MessageSquare },
];

const LANG_PROGRESS = [
  { href: "/progress", label: "Progress", icon: BarChart2 },
];

const SECTION_OTHER = [
  { href: "/community", label: "Community", icon: Users },
  { href: "/wallet",    label: "Billing",   icon: CreditCard },
];

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type NavItem = { href: string; label: string; icon: React.ComponentType<any> };

function NavSection({ title, items, path }: { title: string; items: NavItem[]; path: string }) {
  return (
    <div className="mb-1">
      {title && (
        <p className="px-3 pt-3 pb-1 text-[10px] font-semibold text-gray-400 uppercase tracking-wider">{title}</p>
      )}
      {items.map(({ href, label, icon: Icon }) => {
        const active = path === href || (href !== "/dashboard" && path.startsWith(href));
        return (
          <Link key={href} href={href} className={`sidebar-link ${active ? "active" : ""}`}>
            <Icon size={15} />
            <span>{label}</span>
          </Link>
        );
      })}
    </div>
  );
}

export default function Sidebar() {
  const path   = usePathname();
  const router = useRouter();
  const { pro } = useSubscription();
  const [activePath, setActivePath] = useState<LearningPath>("dsa");

  useEffect(() => {
    const saved = getLearningPath();
    if (saved) setActivePath(saved);
  }, []);

  function switchPath(p: LearningPath) {
    setActivePath(p);
    setLearningPath(p);
  }

  async function handleSignOut() {
    try { await authApi.logout(); } catch {}
    router.push("/login");
  }

  const isDSA = activePath === "dsa";

  return (
    <aside className="fixed top-0 left-0 h-screen bg-white border-r border-gray-200 flex flex-col z-40"
      style={{ width: "var(--sidebar-width)" }}>

      {/* Logo */}
      <div className="flex items-center gap-2 px-4 py-4 border-b border-gray-100">
        <div className="w-7 h-7 rounded-lg bg-brand-600 flex items-center justify-center">
          <Zap size={14} className="text-white" />
        </div>
        <div>
          <span className="font-bold text-gray-900 text-sm leading-none block">Academy</span>
          <span className="text-[10px] text-brand-600 font-semibold tracking-wide">LEARN · PRACTICE · BUILD</span>
        </div>
      </div>

      {/* Path switcher tabs */}
      <div className="px-3 pt-3 pb-1">
        <div className="flex rounded-lg bg-gray-100 p-0.5 gap-0.5">
          <button
            onClick={() => switchPath("dsa")}
            className={`flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-md text-xs font-semibold transition-all
              ${isDSA ? "bg-white text-brand-600 shadow-sm" : "text-gray-500 hover:text-gray-700"}`}
          >
            <span>💻</span> DSA
          </button>
          <button
            onClick={() => switchPath("languages")}
            className={`flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-md text-xs font-semibold transition-all
              ${!isDSA ? "bg-white text-emerald-600 shadow-sm" : "text-gray-500 hover:text-gray-700"}`}
          >
            <span>🌍</span> Languages
          </button>
        </div>
      </div>

      {/* Home */}
      <div className="px-3 pt-2">
        <Link href="/dashboard" className={`sidebar-link ${path === "/dashboard" ? "active" : ""}`}>
          <Home size={15} />
          <span>Home</span>
        </Link>
      </div>

      {/* Nav sections — switch based on path */}
      <nav className="flex-1 overflow-y-auto px-3 pb-2">
        {isDSA ? (
          <>
            <NavSection title="Learn"    items={DSA_LEARN}    path={path} />
            <NavSection title="Practice" items={DSA_PRACTICE} path={path} />
            <NavSection title="AI"       items={DSA_AI}       path={path} />
            <NavSection title="Progress" items={DSA_PROGRESS} path={path} />
          </>
        ) : (
          <>
            <NavSection title="Languages" items={LANG_LEARN}     path={path} />
            <NavSection title="Practice"  items={LANG_PRACTICE}  path={path} />
            <NavSection title="AI"        items={LANG_AI}        path={path} />
            <NavSection title="Progress"  items={LANG_PROGRESS}  path={path} />
          </>
        )}
        <NavSection title="" items={SECTION_OTHER} path={path} />
      </nav>

      {/* Sign out */}
      <div className="px-3 mb-1">
        <button onClick={handleSignOut}
          className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors">
          <LogOut size={16} />
          <span>Sign Out</span>
        </button>
      </div>

      {/* Pro upsell */}
      {pro === false && (
        <div className="mx-3 mb-3 p-3 rounded-xl bg-gradient-to-br from-amber-50 to-orange-50 border border-amber-200">
          <div className="flex items-center gap-1.5 mb-0.5">
            <Lock size={11} className="text-amber-600" />
            <p className="text-xs font-semibold text-amber-700">Free plan</p>
          </div>
          <p className="text-xs text-gray-500 leading-tight">20 problems unlocked. Upgrade for all 250+.</p>
          <Link href="/pricing"
            className="mt-2 w-full text-xs font-semibold text-amber-600 hover:text-amber-800 flex items-center gap-1">
            Upgrade to Pro ₹999/mo <ChevronRight size={12} />
          </Link>
        </div>
      )}
    </aside>
  );
}
