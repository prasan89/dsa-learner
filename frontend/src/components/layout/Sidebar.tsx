"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Home, Code2, Coffee, Layout, Bot, Dumbbell,
  RotateCcw, BarChart2, Users, CreditCard, ChevronRight, Zap, LogOut, Lock
} from "lucide-react";
import { authApi } from "@/lib/api/auth";
import { useSubscription } from "@/lib/useSubscription";

const NAV = [
  { href: "/dashboard",     label: "Home",          icon: Home },
  { href: "/problems",      label: "DSA",           icon: Code2 },
  { href: "/java",          label: "Java",          icon: Coffee },
  { href: "/system-design", label: "System Design", icon: Layout },
  { href: "/ai-mentor",     label: "AI Mentor",     icon: Bot },
  { href: "/patterns",      label: "Practice",      icon: Dumbbell },
  { href: "/revision",      label: "Revision",      icon: RotateCcw },
  { href: "/progress",      label: "Progress",      icon: BarChart2 },
  { href: "/community",     label: "Community",     icon: Users },
  { href: "/wallet",        label: "Billing",       icon: CreditCard },
];

export default function Sidebar() {
  const path   = usePathname();
  const router = useRouter();
  const { pro } = useSubscription();

  async function handleSignOut() {
    try { await authApi.logout(); } catch {}
    router.push("/login");
  }

  return (
    <aside className="fixed top-0 left-0 h-screen bg-white border-r border-gray-200 flex flex-col z-40"
      style={{ width: "var(--sidebar-width)" }}>

      {/* Logo */}
      <div className="flex items-center gap-2 px-4 py-4 border-b border-gray-100">
        <div className="w-7 h-7 rounded-lg bg-brand-600 flex items-center justify-center">
          <Zap size={14} className="text-white" />
        </div>
        <span className="font-bold text-gray-900 text-sm">DSA Expert</span>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-3 space-y-0.5">
        {NAV.map(({ href, label, icon: Icon }) => {
          const active = href === "/dashboard"
            ? path === "/dashboard"
            : path.startsWith(href);
          return (
            <Link
              key={href}
              href={href}
              className={`sidebar-link ${active ? "active" : ""}`}
            >
              <Icon size={16} />
              <span>{label}</span>
            </Link>
          );
        })}
      </nav>

      {/* Sign out */}
      <div className="px-3 mb-1">
        <button onClick={handleSignOut}
          className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors">
          <LogOut size={16} />
          <span>Sign Out</span>
        </button>
      </div>

      {/* Pro upsell — only shown for free users */}
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
