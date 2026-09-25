"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "react-hot-toast";
import Cookies from "js-cookie";
import { Eye, EyeOff, ArrowLeft, Zap } from "lucide-react";
import { authApi } from "@/lib/api/auth";
import { getLearningPath, setLearningPath, type LearningPath } from "@/lib/learningPath";
import LearningPathCard, { PATH_CONFIGS } from "@/components/onboarding/LearningPathCard";

const schema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

type FormData = z.infer<typeof schema>;

type Stage = "path" | "form";

export default function LoginPage() {
  const router = useRouter();
  const [stage, setStage] = useState<Stage>("path");
  const [selectedPath, setSelectedPath] = useState<LearningPath | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [animating, setAnimating] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  // Returning user: skip selector if path already chosen
  useEffect(() => {
    const saved = getLearningPath();
    if (saved) {
      setSelectedPath(saved);
      // Show form directly for returning users, but keep a "switch" link
    }
  }, []);

  function handlePathSelect(path: LearningPath) {
    setSelectedPath(path);
    setLearningPath(path);
    setAnimating(true);
    setTimeout(() => {
      setStage("form");
      setAnimating(false);
    }, 220);
  }

  function handleBack() {
    setAnimating(true);
    setTimeout(() => {
      setStage("path");
      setAnimating(false);
    }, 180);
  }

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authApi.login(data);
      Cookies.set("accessToken", res.data.accessToken, { expires: 1 });
      Cookies.set("refreshToken", res.data.refreshToken, { expires: 7 });
      router.push("/dashboard");
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? "Invalid email or password");
    }
  };

  const activePath = PATH_CONFIGS.find((p) => p.id === selectedPath);
  const isLanguages = selectedPath === "languages";

  return (
    <div className="min-h-screen flex">
      {/* ── Left panel — Academy brand ─────────────────────────────── */}
      <div className="hidden lg:flex lg:w-[46%] xl:w-[42%] relative flex-col bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 overflow-hidden">
        {/* Subtle grid texture */}
        <div className="absolute inset-0 opacity-[0.04]"
          style={{ backgroundImage: "linear-gradient(rgba(255,255,255,.3) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,.3) 1px,transparent 1px)", backgroundSize: "40px 40px" }} />

        {/* Floating orbs */}
        <div className="absolute top-24 left-12 w-64 h-64 rounded-full bg-indigo-600/20 blur-[80px] pointer-events-none" />
        <div className="absolute bottom-32 right-8 w-48 h-48 rounded-full bg-violet-600/20 blur-[60px] pointer-events-none" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full bg-blue-600/10 blur-[100px] pointer-events-none" />

        {/* Logo */}
        <div className="relative z-10 px-10 pt-10">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-brand-600 flex items-center justify-center shadow-lg">
              <Zap size={16} className="text-white" />
            </div>
            <div>
              <span className="font-bold text-white text-base leading-none block">Academy</span>
              <span className="text-[10px] text-indigo-400 font-semibold tracking-widest uppercase">Learn · Practice · Build</span>
            </div>
          </div>
        </div>

        {/* Hero copy */}
        <div className="relative z-10 flex-1 flex flex-col justify-center px-10 pb-10">
          <h1 className="text-5xl font-black text-white leading-[1.1] tracking-tight mb-6">
            Learn.<br />
            Practice.<br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-violet-400">Build.</span>
          </h1>
          <p className="text-gray-300 text-lg leading-relaxed mb-10 max-w-xs">
            One learning platform for technical skills and languages.
          </p>

          {/* Feature bullets */}
          <div className="space-y-3">
            {[
              { icon: "🧠", text: "World-class DSA content" },
              { icon: "🤖", text: "AI-powered learning" },
              { icon: "💬", text: "Real practice & conversations" },
              { icon: "📈", text: "Track progress & achieve goals" },
            ].map(({ icon, text }) => (
              <div key={text} className="flex items-center gap-3">
                <span className="text-base">{icon}</span>
                <span className="text-gray-400 text-sm">{text}</span>
              </div>
            ))}
          </div>

          {/* Illustration — floating language badges */}
          <div className="mt-12 relative h-32">
            {/* DSA icon */}
            <div className="absolute left-0 bottom-0 w-20 h-20 rounded-2xl bg-white/8 backdrop-blur-sm border border-white/10 flex flex-col items-center justify-center gap-1 shadow-xl">
              <span className="text-3xl">💻</span>
              <span className="text-white/60 text-[9px] font-semibold tracking-wide">DSA</span>
            </div>
            {/* Language bubbles */}
            <div className="absolute left-24 bottom-8 bg-white/10 backdrop-blur-sm border border-white/15 rounded-full px-3 py-1.5 text-sm font-semibold text-white shadow-lg flex items-center gap-1.5">
              🇩🇪 <span>Hallo!</span>
            </div>
            <div className="absolute left-36 bottom-0 bg-white/10 backdrop-blur-sm border border-white/15 rounded-full px-3 py-1.5 text-sm font-semibold text-white shadow-lg flex items-center gap-1.5">
              🇫🇷 <span>Bonjour!</span>
            </div>
            <div className="absolute right-8 bottom-6 bg-white/10 backdrop-blur-sm border border-white/15 rounded-full px-3 py-1.5 text-sm font-semibold text-white shadow-lg flex items-center gap-1.5">
              🇰🇷 <span>안녕하세요</span>
            </div>
            <div className="absolute right-4 bottom-16 bg-white/10 backdrop-blur-sm border border-white/15 rounded-full px-3 py-1.5 text-sm font-semibold text-white shadow-lg flex items-center gap-1.5">
              🇪🇸 <span>¡Hola!</span>
            </div>
          </div>
        </div>
      </div>

      {/* ── Right panel ────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col bg-white">
        {/* Top bar */}
        <div className="flex items-center justify-between px-8 py-5 border-b border-gray-100">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 lg:hidden">
            <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center">
              <Zap size={14} className="text-white" />
            </div>
            <span className="font-bold text-gray-900 text-sm">Academy</span>
          </div>
          <div className="hidden lg:block" />

          <p className="text-sm text-gray-500">
            New to Academy?{" "}
            <Link href="/register" className="text-brand-600 hover:text-brand-700 font-semibold">
              Create account
            </Link>
          </p>
        </div>

        {/* Content */}
        <div className={`flex-1 flex items-center justify-center px-6 py-10 transition-opacity duration-200 ${animating ? "opacity-0" : "opacity-100"}`}>

          {/* ── Stage: path selector ── */}
          {stage === "path" && (
            <div className="w-full max-w-lg">
              <div className="mb-8">
                <h2 className="text-3xl font-black text-gray-900 tracking-tight">What do you want to learn?</h2>
                <p className="text-gray-500 mt-2">Choose your learning journey to get started. You can always explore the other path later.</p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {PATH_CONFIGS.map((config) => (
                  <LearningPathCard
                    key={config.id}
                    config={config}
                    selected={selectedPath === config.id}
                    onSelect={handlePathSelect}
                  />
                ))}
              </div>

              <p className="text-center text-xs text-gray-400 mt-6">
                Already have an account?{" "}
                <button
                  onClick={() => selectedPath ? setStage("form") : toast("Pick a path first", { icon: "👆" })}
                  className="text-brand-600 hover:text-brand-700 font-semibold underline-offset-2 hover:underline"
                >
                  Sign in
                </button>
              </p>
            </div>
          )}

          {/* ── Stage: login form ── */}
          {stage === "form" && (
            <div className="w-full max-w-sm">
              {/* Back + path badge */}
              <div className="flex items-center justify-between mb-8">
                <button
                  onClick={handleBack}
                  className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800 transition-colors"
                >
                  <ArrowLeft size={15} />
                  Back
                </button>
                {activePath && (
                  <div className={`flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full
                    ${isLanguages ? "bg-emerald-50 text-emerald-700" : "bg-indigo-50 text-indigo-700"}`}>
                    <span>{activePath.emoji}</span>
                    <span>{activePath.title}</span>
                    <span className="text-[10px] opacity-60 ml-0.5">·</span>
                    <button
                      onClick={handleBack}
                      className="opacity-60 hover:opacity-100 text-[10px] underline underline-offset-1"
                    >
                      switch
                    </button>
                  </div>
                )}
              </div>

              <div className="mb-7">
                <h2 className="text-2xl font-black text-gray-900 tracking-tight">Welcome back</h2>
                <p className="text-gray-500 text-sm mt-1">
                  Sign in to continue your {activePath?.title ?? "learning"} journey
                </p>
              </div>

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email address</label>
                  <input
                    {...register("email")}
                    type="email"
                    autoComplete="email"
                    className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500/40 focus:border-brand-400 transition bg-gray-50 focus:bg-white"
                    placeholder="you@example.com"
                  />
                  {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="block text-sm font-semibold text-gray-700">Password</label>
                    <Link href="#" className="text-xs text-brand-600 hover:text-brand-700 font-medium">Forgot?</Link>
                  </div>
                  <div className="relative">
                    <input
                      {...register("password")}
                      type={showPassword ? "text" : "password"}
                      autoComplete="current-password"
                      className="w-full border border-gray-200 rounded-xl px-4 py-3 pr-11 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500/40 focus:border-brand-400 transition bg-gray-50 focus:bg-white"
                      placeholder="••••••••"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((v) => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                  {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className={`w-full text-white font-bold py-3 rounded-xl text-sm transition-all mt-2 shadow-sm
                    ${isLanguages
                      ? "bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50"
                      : "bg-brand-600 hover:bg-brand-700 disabled:opacity-50"
                    }`}
                >
                  {isSubmitting ? "Signing in…" : "Sign in"}
                </button>
              </form>

              <div className="mt-6 pt-6 border-t border-gray-100 text-center">
                <p className="text-sm text-gray-500">
                  Don&apos;t have an account?{" "}
                  <Link href="/register" className="text-brand-600 hover:text-brand-700 font-semibold">
                    Create one free
                  </Link>
                </p>
              </div>

              <p className="text-center text-xs text-gray-400 mt-4">
                By signing in you agree to our{" "}
                <Link href="#" className="underline hover:text-gray-600">Terms</Link>
                {" & "}
                <Link href="#" className="underline hover:text-gray-600">Privacy Policy</Link>
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
