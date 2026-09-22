"use client";

import Link from "next/link";
import { Check, Zap } from "lucide-react";
import { useSubscription } from "@/lib/useSubscription";

const FREE_FEATURES = [
  "20 curated problems (Easy & Medium)",
  "Problem descriptions & examples",
  "Basic streak tracking",
  "Dashboard with progress stats",
];

const PRO_FEATURES = [
  "All 250+ problems (Easy, Medium, Hard)",
  "Gold-standard 3-level hints",
  "Pattern recognition guides",
  "Brute → optimal walkthroughs",
  "Senior follow-up questions",
  "Spaced repetition reviews",
  "AI Mentor chat",
  "Pattern mastery heatmap",
  "Unlimited submissions",
];

export default function PricingPage() {
  const { pro, loading } = useSubscription();

  return (
    <div className="min-h-screen bg-gray-50 py-16 px-4">
      <div className="max-w-3xl mx-auto text-center mb-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-3">Simple, honest pricing</h1>
        <p className="text-gray-500">
          Start free. Upgrade when you&apos;re ready for the full DSA curriculum.
        </p>
      </div>

      <div className="max-w-3xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Free */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-8 flex flex-col">
          <div className="mb-6">
            <h2 className="text-lg font-bold text-gray-900 mb-1">Free</h2>
            <div className="flex items-end gap-1">
              <span className="text-4xl font-extrabold text-gray-900">₹0</span>
              <span className="text-gray-400 text-sm mb-1.5">/month</span>
            </div>
            <p className="text-gray-500 text-sm mt-2">Always free. No card required.</p>
          </div>
          <ul className="space-y-3 flex-1 mb-8">
            {FREE_FEATURES.map((f) => (
              <li key={f} className="flex items-start gap-2.5 text-sm text-gray-600">
                <Check size={15} className="text-green-500 mt-0.5 shrink-0" />
                {f}
              </li>
            ))}
          </ul>
          {!loading && !pro ? (
            <span className="w-full py-3 rounded-xl bg-gray-100 text-gray-500 text-sm font-semibold text-center block">
              Current plan
            </span>
          ) : (
            <Link href="/problems"
              className="w-full py-3 rounded-xl border border-gray-300 text-gray-700 text-sm font-semibold text-center block hover:bg-gray-50 transition-colors">
              Browse free problems
            </Link>
          )}
        </div>

        {/* Pro */}
        <div className="bg-gray-900 rounded-2xl border border-gray-800 shadow-lg p-8 flex flex-col relative overflow-hidden">
          <div className="absolute top-4 right-4">
            <span className="text-xs font-semibold bg-amber-500 text-white px-2.5 py-1 rounded-full flex items-center gap-1">
              <Zap size={11} className="fill-white" /> Most popular
            </span>
          </div>
          <div className="mb-6">
            <h2 className="text-lg font-bold text-white mb-1">Pro</h2>
            <div className="flex items-end gap-1">
              <span className="text-4xl font-extrabold text-white">₹999</span>
              <span className="text-gray-400 text-sm mb-1.5">/month</span>
            </div>
            <p className="text-gray-400 text-sm mt-2">Full access to everything.</p>
          </div>
          <ul className="space-y-3 flex-1 mb-8">
            {PRO_FEATURES.map((f) => (
              <li key={f} className="flex items-start gap-2.5 text-sm text-gray-300">
                <Check size={15} className="text-amber-400 mt-0.5 shrink-0" />
                {f}
              </li>
            ))}
          </ul>
          {!loading && pro ? (
            <span className="w-full py-3 rounded-xl bg-gray-700 text-gray-300 text-sm font-semibold text-center block">
              Current plan
            </span>
          ) : (
            <Link href="/dashboard"
              className="w-full py-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-white text-sm font-semibold text-center block transition-colors">
              Upgrade to Pro
            </Link>
          )}
        </div>
      </div>

      <p className="text-center text-xs text-gray-400 mt-8">
        Payments processed via Razorpay · Cancel anytime · INR pricing
      </p>
    </div>
  );
}
