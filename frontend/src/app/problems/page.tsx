"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import Link from "next/link";
import { problemsApi } from "@/lib/api/problems";
import { patternsApi } from "@/lib/api/patterns";
import type { Pattern } from "@/types";
import { difficultyBadge } from "@/lib/utils";

const DIFFICULTIES = ["All", "Easy", "Medium", "Hard"];
const PAGE_SIZE = 30;

export default function ProblemsPage() {
  const [problems, setProblems] = useState<any[]>([]);
  const [patterns, setPatterns] = useState<Pattern[]>([]);
  const [difficulty, setDifficulty] = useState("");
  const [patternId, setPatternId] = useState("");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const sentinelRef = useRef<HTMLDivElement>(null);
  const filtersRef = useRef({ difficulty: "", patternId: "" });

  useEffect(() => {
    patternsApi.list().then((r) => setPatterns((r.data as any) ?? []));
  }, []);

  const fetchPage = useCallback((pg: number, diff: string, pat: string) => {
    setLoading(true);
    problemsApi
      .list({ difficulty: diff || undefined, patternId: pat || undefined, page: pg, size: PAGE_SIZE })
      .then((r) => {
        const data = r.data as any;
        const items = data.content ?? data.problems ?? [];
        const total = data.totalElements ?? data.total ?? 0;
        setProblems((prev) => (pg === 0 ? items : [...prev, ...items]));
        setHasMore((pg + 1) * PAGE_SIZE < total);
      })
      .finally(() => setLoading(false));
  }, []);

  // Reset on filter change
  useEffect(() => {
    filtersRef.current = { difficulty, patternId };
    setPage(0);
    setProblems([]);
    setHasMore(true);
    fetchPage(0, difficulty, patternId);
  }, [difficulty, patternId, fetchPage]);

  // Infinite scroll via IntersectionObserver
  useEffect(() => {
    if (!hasMore || loading) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          setPage((prev) => {
            const next = prev + 1;
            fetchPage(next, filtersRef.current.difficulty, filtersRef.current.patternId);
            return next;
          });
        }
      },
      { threshold: 0.1 }
    );
    const el = sentinelRef.current;
    if (el) observer.observe(el);
    return () => { if (el) observer.unobserve(el); };
  }, [hasMore, loading, fetchPage]);

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6">
      <div className="max-w-5xl mx-auto space-y-6">
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="text-2xl font-bold mr-4">Problems</h1>

          {/* Difficulty filter */}
          <div className="flex gap-2">
            {DIFFICULTIES.map((d) => (
              <button
                key={d}
                onClick={() => setDifficulty(d === "All" ? "" : d.toUpperCase())}
                className={`px-3 py-1 rounded-full text-sm border transition-colors ${
                  (d === "All" && !difficulty) || difficulty === d.toUpperCase()
                    ? "border-brand-500 text-brand-400 bg-brand-500/10"
                    : "border-gray-700 text-gray-400 hover:border-gray-500"
                }`}
              >
                {d}
              </button>
            ))}
          </div>

          {/* Pattern filter */}
          <select
            value={patternId}
            onChange={(e) => setPatternId(e.target.value)}
            className="bg-gray-900 border border-gray-700 text-gray-300 text-sm rounded-lg px-3 py-1 focus:outline-none focus:border-brand-500"
          >
            <option value="">All Patterns</option>
            {patterns.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        <div className="bg-gray-900 rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-gray-400 text-left">
                <th className="px-4 py-3">#</th>
                <th className="px-4 py-3">Title</th>
                <th className="px-4 py-3">Pattern</th>
                <th className="px-4 py-3">Difficulty</th>
                <th className="px-4 py-3">Tags</th>
              </tr>
            </thead>
            <tbody>
              {loading && problems.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-gray-500">Loading...</td>
                </tr>
              ) : problems.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-10 text-center text-gray-500">No problems found.</td>
                </tr>
              ) : (
                problems.map((p, i) => (
                  <tr key={p.id} className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors">
                    <td className="px-4 py-3 text-gray-500">{i + 1}</td>
                    <td className="px-4 py-3">
                      <Link href={`/problems/${p.slug}`} className="text-white hover:text-brand-400 font-medium transition-colors">
                        {p.solved && <span className="text-green-400 mr-2">✓</span>}
                        {p.title}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-xs">
                      {p.patterns?.map((pat: any) => pat.name).join(", ")}
                    </td>
                    <td className="px-4 py-3">
                      <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-500 text-xs">
                      {p.tags?.slice(0, 2).join(", ")}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Sentinel for infinite scroll */}
        <div ref={sentinelRef} className="py-4 text-center text-gray-500 text-sm">
          {loading && problems.length > 0 && "Loading more..."}
          {!hasMore && problems.length > 0 && `All ${problems.length} problems loaded`}
        </div>
      </div>
    </div>
  );
}
