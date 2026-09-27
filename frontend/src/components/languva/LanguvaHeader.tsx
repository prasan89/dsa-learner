"use client";

import { useState } from "react";
import Link from "next/link";
import { Search, Menu, X, BookOpen } from "lucide-react";

const NAV_LINKS = [
  { label: "Courses",   href: "#languages" },
  { label: "Features",  href: "#features"  },
  { label: "Pricing",   href: "#pricing"   },
  { label: "Blog",      href: "#"          },
  { label: "Community", href: "#"          },
];

export default function LanguvaHeader() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">

        {/* Logo */}
        <Link href="/" className="flex items-center gap-2 shrink-0">
          <div className="w-8 h-8 rounded-lg bg-languva-600 flex items-center justify-center shadow-sm">
            <BookOpen size={15} className="text-white" />
          </div>
          <span className="font-bold text-gray-900 text-lg tracking-tight">Languva</span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden md:flex items-center gap-7">
          {NAV_LINKS.map(({ label, href }) => (
            <a key={label} href={href}
              className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors">
              {label}
            </a>
          ))}
        </nav>

        {/* Right actions */}
        <div className="hidden md:flex items-center gap-2">
          <button aria-label="Search" className="p-2 text-gray-500 hover:text-gray-900 transition-colors">
            <Search size={18} />
          </button>
          <Link href="/login"
            className="text-sm font-semibold text-gray-700 hover:text-gray-900 px-4 py-2 rounded-lg transition-colors">
            Log in
          </Link>
          <Link href="/register"
            className="text-sm font-semibold text-white bg-languva-600 hover:bg-languva-700 px-4 py-2 rounded-lg transition-colors shadow-sm">
            Get Started Free
          </Link>
        </div>

        {/* Mobile hamburger */}
        <button className="md:hidden p-2 text-gray-600" onClick={() => setMobileOpen((v) => !v)}
          aria-label="Toggle menu">
          {mobileOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 px-4 py-4 space-y-1">
          {NAV_LINKS.map(({ label, href }) => (
            <a key={label} href={href} onClick={() => setMobileOpen(false)}
              className="block py-2.5 px-3 text-sm font-medium text-gray-700 hover:text-languva-600 hover:bg-languva-50 rounded-lg transition-colors">
              {label}
            </a>
          ))}
          <div className="pt-3 border-t border-gray-100 flex flex-col gap-2">
            <Link href="/login" onClick={() => setMobileOpen(false)}
              className="text-center py-2.5 text-sm font-semibold text-gray-700 border border-gray-200 rounded-lg hover:bg-gray-50">
              Log in
            </Link>
            <Link href="/register" onClick={() => setMobileOpen(false)}
              className="text-center py-2.5 text-sm font-semibold text-white bg-languva-600 rounded-lg hover:bg-languva-700">
              Get Started Free
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
