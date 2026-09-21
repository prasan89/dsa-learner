"use client";

import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import Cookies from "js-cookie";
import { authApi } from "@/lib/api/auth";

const NAV_LINKS = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/problems", label: "Problems" },
  { href: "/patterns", label: "Patterns" },
  { href: "/wallet", label: "Wallet" },
];

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();

  const handleLogout = async () => {
    try { await authApi.logout(); } catch {}
    Cookies.remove("accessToken");
    Cookies.remove("refreshToken");
    router.push("/login");
  };

  return (
    <nav className="bg-gray-900 border-b border-gray-800 px-6 py-3 flex items-center gap-6">
      <Link href="/dashboard" className="font-bold text-brand-400 text-lg mr-4">
        DSA Expert
      </Link>

      {NAV_LINKS.map(({ href, label }) => (
        <Link
          key={href}
          href={href}
          className={`text-sm transition-colors ${
            pathname.startsWith(href)
              ? "text-white font-medium"
              : "text-gray-400 hover:text-white"
          }`}
        >
          {label}
        </Link>
      ))}

      <button
        onClick={handleLogout}
        className="ml-auto text-sm text-gray-400 hover:text-white transition-colors"
      >
        Sign out
      </button>
    </nav>
  );
}
