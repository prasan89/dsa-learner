"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "react-hot-toast";
import Cookies from "js-cookie";
import { Code2 } from "lucide-react";
import { authApi } from "@/lib/api/auth";

const schema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const router = useRouter();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

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

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Minimal nav */}
      <nav className="bg-white border-b border-gray-100 px-6 h-14 flex items-center">
        <Link href="/" className="flex items-center gap-2 font-bold text-gray-900">
          <div className="w-7 h-7 bg-brand-600 rounded-md flex items-center justify-center text-white">
            <Code2 size={14} />
          </div>
          <span className="text-sm">DSA Learner</span>
        </Link>
      </nav>

      {/* Main */}
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-sm">
          {/* Card */}
          <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-8">
            <div className="text-center mb-6">
              <h1 className="text-2xl font-bold text-gray-900">Welcome back</h1>
              <p className="text-gray-500 text-sm mt-1">Sign in to your account to continue</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Email address
                </label>
                <input
                  {...register("email")}
                  type="email"
                  autoComplete="email"
                  className="w-full border border-gray-300 rounded-lg px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition"
                  placeholder="you@example.com"
                />
                {errors.email && (
                  <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>
                )}
              </div>

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-sm font-medium text-gray-700">Password</label>
                  <Link href="#" className="text-xs text-brand-600 hover:text-brand-700 font-medium">
                    Forgot password?
                  </Link>
                </div>
                <input
                  {...register("password")}
                  type="password"
                  autoComplete="current-password"
                  className="w-full border border-gray-300 rounded-lg px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition"
                  placeholder="••••••••"
                />
                {errors.password && (
                  <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>
                )}
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full bg-brand-600 hover:bg-brand-700 disabled:opacity-50 text-white font-semibold py-2.5 rounded-lg text-sm transition-colors mt-2"
              >
                {isSubmitting ? "Signing in..." : "Sign in"}
              </button>
            </form>

            <div className="mt-5 pt-5 border-t border-gray-100 text-center">
              <p className="text-sm text-gray-500">
                Don&apos;t have an account?{" "}
                <Link href="/register" className="text-brand-600 hover:text-brand-700 font-semibold">
                  Create one free
                </Link>
              </p>
            </div>
          </div>

          <p className="text-center text-xs text-gray-400 mt-4">
            By signing in you agree to our{" "}
            <Link href="#" className="underline hover:text-gray-600">Terms</Link>
            {" & "}
            <Link href="#" className="underline hover:text-gray-600">Privacy Policy</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
