export type Difficulty = "Easy" | "Medium" | "Hard";
export type CategoryType = "data-structures" | "algorithms" | "advanced";
export type CategoryStatus = "not-started" | "in-progress" | "completed";

export interface LearningCategory {
  id: string;
  slug: string;
  name: string;
  description: string;
  type: CategoryType;
  difficulty: Difficulty;
  estimatedMinutes: number;
  conceptCount: number;
  available: boolean; // false = coming soon
}

export const LEARNING_CATEGORIES: LearningCategory[] = [
  // ── Data Structures ──────────────────────────────────────────────────────
  {
    id: "arrays",
    slug: "arrays",
    name: "Arrays",
    description: "Learn indexing, traversal, searching, two pointers and sliding windows.",
    type: "data-structures",
    difficulty: "Easy",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: true,
  },
  {
    id: "linked-lists",
    slug: "linked-lists",
    name: "Linked Lists",
    description: "Learn nodes, pointers, traversal, insertion, deletion and reversal.",
    type: "data-structures",
    difficulty: "Easy",
    estimatedMinutes: 60,
    conceptCount: 6,
    available: false,
  },
  {
    id: "stacks",
    slug: "stacks",
    name: "Stacks",
    description: "Learn LIFO operations, monotonic stacks and real-world applications.",
    type: "data-structures",
    difficulty: "Easy",
    estimatedMinutes: 45,
    conceptCount: 5,
    available: false,
  },
  {
    id: "queues",
    slug: "queues",
    name: "Queues",
    description: "Learn FIFO operations, deques, priority queues and common patterns.",
    type: "data-structures",
    difficulty: "Easy",
    estimatedMinutes: 45,
    conceptCount: 5,
    available: false,
  },
  {
    id: "hash-maps",
    slug: "hash-maps",
    name: "Hash Maps",
    description: "Learn hashing, collision handling, load factor and real-world usage.",
    type: "data-structures",
    difficulty: "Medium",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: false,
  },
  {
    id: "trees",
    slug: "trees",
    name: "Trees",
    description: "Binary trees, BST, traversals, height, and tree-based algorithms.",
    type: "data-structures",
    difficulty: "Medium",
    estimatedMinutes: 120,
    conceptCount: 12,
    available: false,
  },
  {
    id: "heaps",
    slug: "heaps",
    name: "Heaps",
    description: "Learn min/max heaps, heapify, priority queues and heap sort.",
    type: "data-structures",
    difficulty: "Medium",
    estimatedMinutes: 60,
    conceptCount: 6,
    available: false,
  },
  {
    id: "graphs",
    slug: "graphs",
    name: "Graphs",
    description: "Graph representations, BFS, DFS, shortest paths and traversal algorithms.",
    type: "data-structures",
    difficulty: "Medium",
    estimatedMinutes: 120,
    conceptCount: 12,
    available: false,
  },

  // ── Algorithms ───────────────────────────────────────────────────────────
  {
    id: "sorting",
    slug: "sorting",
    name: "Sorting Algorithms",
    description: "Master bubble, merge, quick and heap sort with step-by-step visuals.",
    type: "algorithms",
    difficulty: "Medium",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: false,
  },
  {
    id: "searching",
    slug: "searching",
    name: "Searching Algorithms",
    description: "Binary search and its many applications on sorted arrays and answer spaces.",
    type: "algorithms",
    difficulty: "Easy",
    estimatedMinutes: 60,
    conceptCount: 6,
    available: false,
  },
  {
    id: "two-pointers",
    slug: "two-pointers",
    name: "Two Pointers & Sliding Window",
    description: "Powerful techniques for array and string problems with O(n) complexity.",
    type: "algorithms",
    difficulty: "Medium",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: false,
  },
  {
    id: "recursion",
    slug: "recursion",
    name: "Recursion & Backtracking",
    description: "Understand recursion, call stacks, memoization and constraint satisfaction.",
    type: "algorithms",
    difficulty: "Medium",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: false,
  },
  {
    id: "dynamic-programming",
    slug: "dynamic-programming",
    name: "Dynamic Programming",
    description: "Solve optimization problems using overlapping subproblems and DP patterns.",
    type: "algorithms",
    difficulty: "Hard",
    estimatedMinutes: 120,
    conceptCount: 12,
    available: false,
  },
  {
    id: "greedy",
    slug: "greedy",
    name: "Greedy Algorithms",
    description: "Make optimal local choices at each step to solve global optimization problems.",
    type: "algorithms",
    difficulty: "Medium",
    estimatedMinutes: 90,
    conceptCount: 8,
    available: false,
  },
  {
    id: "graph-algorithms",
    slug: "graph-algorithms",
    name: "Graph Algorithms",
    description: "Shortest path (Dijkstra, Bellman-Ford), MST, topological sort and more.",
    type: "algorithms",
    difficulty: "Hard",
    estimatedMinutes: 120,
    conceptCount: 8,
    available: false,
  },

  // ── Advanced ─────────────────────────────────────────────────────────────
  {
    id: "advanced-ds",
    slug: "advanced-ds",
    name: "Advanced Data Structures",
    description: "Tries, Union Find, Segment Trees, Fenwick Trees and more.",
    type: "advanced",
    difficulty: "Hard",
    estimatedMinutes: 120,
    conceptCount: 6,
    available: false,
  },
];

export const LEARNING_PATH_STAGES = [
  { label: "Foundations",          count: 4,  types: ["data-structures"] as CategoryType[] },
  { label: "Core Data Structures", count: 12, types: ["data-structures"] as CategoryType[] },
  { label: "Algorithms",           count: 16, types: ["algorithms"] as CategoryType[] },
  { label: "Advanced",             count: 8,  types: ["advanced"] as CategoryType[] },
];

export function formatDuration(minutes: number): string {
  if (minutes < 60) return `~${minutes} mins`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m === 0 ? `~${h} hr${h > 1 ? "s" : ""}` : `~${h}.${Math.round(m / 6)} hours`;
}
