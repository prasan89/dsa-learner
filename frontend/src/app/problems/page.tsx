"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import Link from "next/link";
import { CheckCircle2, ChevronRight } from "lucide-react";
import { problemsApi } from "@/lib/api/problems";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern } from "@/types";
import { difficultyBadge } from "@/lib/utils";

const DIFFICULTIES = ["All", "Easy", "Medium", "Hard"];
const PAGE_SIZE = 30;

const DIFF_DOT: Record<string, string> = {
  EASY: "bg-green-400", MEDIUM: "bg-yellow-400", HARD: "bg-red-400",
};

export default function ProblemsPage() {
  const [problems, setProblems]   = useState<any[]>([]);
  const [patterns, setPatterns]   = useState<Pattern[]>([]);
  const [difficulty, setDiff]     = useState("");
  const [patternId, setPatternId] = useState("");
  const [hasMore, setHasMore]     = useState(true);
  const [loading, setLoading]     = useState(false);
  const sentinelRef               = useRef<HTMLDivElement>(null);
  const filtersRef                = useRef({ difficulty: "", patternId: "" });
  const pageRef                   = useRef(0);
  const loadingRef                = useRef(false);
  const hasMoreRef                = useRef(true);

  useEffect(() => {
    patternsApi.list({ category: "DSA" }).then((r) => setPatterns((r.data as any) ?? []));
  }, []);

  const fetchPage = useCallback((pg: number, diff: string, pat: string) => {
    if (loadingRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    problemsApi
      .list({ difficulty: diff || undefined, patternId: pat || undefined, page: pg, size: PAGE_SIZE })
      .then((r) => {
        const data   = r.data as any;
        const items  = data.content ?? data.problems ?? [];
        const total  = data.totalElements ?? data.total ?? 0;
        const more   = (pg + 1) * PAGE_SIZE < total;
        setProblems((prev) => (pg === 0 ? items : [...prev, ...items]));
        setHasMore(more);
        hasMoreRef.current = more;
        pageRef.current = pg;
      })
      .finally(() => { loadingRef.current = false; setLoading(false); });
  }, []);

  useEffect(() => {
    filtersRef.current = { difficulty, patternId };
    pageRef.current    = 0;
    hasMoreRef.current = true;
    loadingRef.current = false;
    setProblems([]);
    setHasMore(true);
    fetchPage(0, difficulty, patternId);
  }, [difficulty, patternId, fetchPage]);

  useEffect(() => {
    const el = sentinelRef.current;
    if (!el) return;
    const io = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !loadingRef.current && hasMoreRef.current) {
        fetchPage(pageRef.current + 1, filtersRef.current.difficulty, filtersRef.current.patternId);
      }
    }, { threshold: 0.1 });
    io.observe(el);
    return () => io.disconnect();
  }, [fetchPage]);

  const solved = problems.filter(p => p.solved).length;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="max-w-5xl mx-auto">
          <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-2">
            <Link href="/dashboard" className="hover:text-gray-600">DSA</Link>
            <ChevronRight size={12} />
            <span className="text-gray-600 font-medium">Problems</span>
          </div>
          <div className="flex items-center justify-between gap-4 flex-wrap">
            <div>
              <h1 className="text-xl font-bold text-gray-900">Problems</h1>
              {problems.length > 0 && (
                <p className="text-sm text-gray-400 mt-0.5">{solved} / {problems.length} solved</p>
              )}
            </div>
            <div className="flex items-center gap-2 flex-wrap">
              {/* Difficulty pills */}
              <div className="flex gap-1">
                {DIFFICULTIES.map((d) => (
                  <button key={d}
                    onClick={() => setDiff(d === "All" ? "" : d.toUpperCase())}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                      (d === "All" && !difficulty) || difficulty === d.toUpperCase()
                        ? "bg-brand-50 border-brand-300 text-brand-700"
                        : "bg-white border-gray-200 text-gray-500 hover:border-gray-300"
                    }`}>
                    {d}
                  </button>
                ))}
              </div>
              {/* Pattern filter */}
              <select value={patternId} onChange={(e) => setPatternId(e.target.value)}
                className="bg-white border border-gray-200 text-gray-600 text-xs rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-brand-500/30">
                <option value="">All Patterns</option>
                {patterns.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-6 py-5">
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50/60">
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wide w-12">#</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wide">Title</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wide">Pattern</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wide w-24">Difficulty</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wide">Tags</th>
              </tr>
            </thead>
            <tbody>
              {loading && problems.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-12 text-center text-gray-400 text-sm">Loading…</td></tr>
              ) : problems.length === 0 ? (
                <tr><td colSpan={5} className="px-4 py-12 text-center text-gray-400 text-sm">No problems found.</td></tr>
              ) : problems.map((p, i) => (
                <tr key={p.id} className={`hover:bg-gray-50 transition-colors ${i < problems.length - 1 ? "border-b border-gray-100" : ""}`}>
                  <td className="px-4 py-3 text-gray-400 text-xs">{i + 1}</td>
                  <td className="px-4 py-3">
                    <Link href={`/problems/${p.slug}`}
                      className="font-medium text-gray-800 hover:text-brand-600 transition-colors flex items-center gap-2">
                      {p.solved && <CheckCircle2 size={13} className="text-green-500 shrink-0" />}
                      {p.title}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-gray-400 text-xs">
                    {p.patterns?.map((pat: any) => pat.name).join(", ")}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1.5">
                      <span className={`w-1.5 h-1.5 rounded-full ${DIFF_DOT[p.difficulty] ?? "bg-gray-300"}`} />
                      <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-1">
                      {p.tags?.slice(0, 2).map((tag: string) => (
                        <span key={tag} className="text-xs bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded">
                          {tag}
                        </span>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div ref={sentinelRef} className="py-4 text-center text-gray-400 text-xs">
          {loading && problems.length > 0 && "Loading more…"}
          {!hasMore && problems.length > 0 && `All ${problems.length} problems loaded`}
        </div>
      </div>
    </div>
  );
}
