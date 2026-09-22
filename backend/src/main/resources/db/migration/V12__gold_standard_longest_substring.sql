-- Gold-standard content for: Longest Substring Without Repeating Characters
-- This is the canonical template. Every field is populated to the full standard.
-- Slug: longest-substring-without-repeating

-- ── 1. Update hints with proper pedagogical labels ─────────────────────────
UPDATE hints
SET label = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating')
  AND level = 1;

UPDATE hints
SET content = 'Think about maintaining a contiguous range of unique characters. Instead of checking every possible substring from scratch, ask: can we extend the current window one character at a time?',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating')
  AND level = 1;

UPDATE hints
SET content = 'You need to know whether the new character already exists somewhere inside the current window. What data structure lets you look that up in O(1) and also tells you exactly WHERE in the string the duplicate last appeared?',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating')
  AND level = 2;

UPDATE hints
SET content = E'Maintain two pointers (left, right) and a HashMap<Character, Integer> storing the last seen index of each character.\nWhen right encounters a duplicate, jump left to max(left, lastIndex + 1) — do NOT just left++ — to avoid re-entering the same duplicate.\nUpdate the map and compute max = Math.max(max, right - left + 1) on every step.',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating')
  AND level = 3;

-- ── 2. Insert gold-standard problem_content ────────────────────────────────
INSERT INTO problem_content (
    problem_id,
    -- Pattern recognition
    recognition_note,
    pattern_recognition_clues,
    when_to_use,
    when_not_to_use,
    -- 3-level explanation
    intuition,
    guided_reasoning,
    solution,
    -- Approach
    brute_force,
    brute_time,
    brute_space,
    optimal_approach,
    optimal_time,
    optimal_space,
    pseudocode,
    -- Why this works
    why_this_works,
    invariant,
    -- Mistakes & senior
    common_mistakes,
    senior_variations
)
SELECT
    p.id,

    -- recognition_note
    'This is a classic Sliding Window problem. The key signal is: you are asked for the longest contiguous substring satisfying a uniqueness constraint.',

    -- pattern_recognition_clues
    E'**Keywords to watch for:**\n- "longest substring"\n- "contiguous"\n- "without repeating"\n- "at most K distinct"\n- "window"\n- "subarray with condition"\n\n**The decisive question:**\nCan I expand/shrink a window over the input and maintain a condition cheaply?\n\nIf yes → Sliding Window.',

    -- when_to_use
    E'- The input is a contiguous sequence (string, array)\n- You are optimizing over subarrays or substrings\n- The condition on the current window can be checked/updated in O(1) as the window moves\n- You want a single-pass O(n) solution',

    -- when_not_to_use
    E'- The problem requires non-contiguous subsequences (use DP instead)\n- You need to consider arbitrary subsets, not windows\n- The order of elements does not matter\n- The window condition requires global state across multiple passes',

    -- intuition
    E'Scanning every possible substring from scratch is O(n²) or worse. The insight is that most of that work is redundant.\n\nWhen we extend the window to the right and hit a duplicate, we do not need to restart from scratch. The only thing that caused the window to become invalid was the **newly added character**. Everything before the duplicate position was already valid.\n\nSo we simply jump the left boundary past the previous occurrence of the duplicate — the window shrinks just enough to become valid again, and we continue forward.\n\nThis gives us a single left-to-right pass: O(n).',

    -- guided_reasoning
    E'Ask yourself these questions:\n\n**1. What do we need to remember?**\nWe need to know, for any character we encounter, whether it already exists in the current window — and if so, where.\n\n**2. Can we avoid re-scanning?**\nYes. Instead of re-scanning the window on each step, maintain a HashMap that stores the *last seen index* of each character.\n\n**3. What does "shrink the window" actually mean?**\nWhen `right` encounters character `c` that is already in the window at index `k`:\n- We must move `left` to at least `k + 1`\n- But `left` must never move backward (use `Math.max`)\n\n**4. When do we update the answer?**\nAfter each `right` step, `right - left + 1` is the current valid window size. Track the maximum.',

    -- solution (Java, with line-by-line comments)
    E'public int lengthOfLongestSubstring(String s) {\n    Map<Character, Integer> lastSeen = new HashMap<>(); // char → last index\n    int max = 0;\n    int left = 0;\n\n    for (int right = 0; right < s.length(); right++) {\n        char c = s.charAt(right);\n\n        // If c is in the window, jump left past its last occurrence\n        if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {\n            left = lastSeen.get(c) + 1;\n        }\n\n        lastSeen.put(c, right);          // record/update position\n        max = Math.max(max, right - left + 1); // update answer\n    }\n    return max;\n}',

    -- brute_force
    E'Check every possible substring and test whether it contains duplicate characters.\n\n```java\npublic int lengthOfLongestSubstring(String s) {\n    int max = 0;\n    for (int i = 0; i < s.length(); i++) {\n        Set<Character> seen = new HashSet<>();\n        for (int j = i; j < s.length(); j++) {\n            if (seen.contains(s.charAt(j))) break;\n            seen.add(s.charAt(j));\n            max = Math.max(max, j - i + 1);\n        }\n    }\n    return max;\n}\n```\n\n**Why this fails at scale:** For a string of length n, we examine O(n²) substrings and do O(n) work per substring → O(n³) in the worst case, or O(n²) with early exit. For n = 10⁵ this is far too slow.',

    -- brute_time
    'O(n²)',

    -- brute_space
    'O(min(n, m))',

    -- optimal_approach
    E'Use a **sliding window** with a HashMap storing each character''s last seen index.\n\n- `right` expands the window one character at a time.\n- When `right` encounters a duplicate **inside** the current window, `left` jumps to `lastSeen[c] + 1`.\n- The map always reflects the most recent position of every character.\n- Window size at each step: `right - left + 1`.\n\nCritical detail: use `Math.max(left, lastSeen[c] + 1)` when updating `left`. Without the max, you can accidentally move `left` backward when a character was last seen before the current window started.',

    -- optimal_time
    'O(n)',

    -- optimal_space
    'O(min(n, m)) — m = charset size',

    -- pseudocode
    E'FUNCTION lengthOfLongestSubstring(s):\n    map  ← empty HashMap {char → index}\n    max  ← 0\n    left ← 0\n\n    FOR right FROM 0 TO len(s) - 1:\n        c ← s[right]\n\n        IF map contains c AND map[c] >= left:\n            left ← map[c] + 1       // shrink window\n\n        map[c] ← right              // record latest position\n        max ← MAX(max, right - left + 1)\n\n    RETURN max',

    -- why_this_works
    E'When `right` moves forward, the window can only become invalid because of the **newly added character** — not because of any earlier position. This means we never need to re-examine anything to the left of `lastSeen[c] + 1`.\n\nThe HashMap gives us O(1) lookup of "where was this character last?", so every step is constant time. Combined with the single forward pass, the total is O(n).\n\nThe `Math.max(left, ...)` guard is essential: a character may have appeared in the string before the current window opened. Without the guard, we would incorrectly shrink `left` backward, allowing a stale duplicate back into the window.',

    -- invariant
    'At every step, the substring s[left..right] contains no duplicate characters.',

    -- common_mistakes
    E'1. **Forgetting Math.max when updating left**\n   `left = lastSeen.get(c) + 1` without `Math.max(left, ...)` lets left move backward, re-introducing a character that was already outside the window.\n\n2. **Off-by-one on window size**\n   Window size is `right - left + 1`, not `right - left`.\n\n3. **Not updating the map after adjusting left**\n   Always call `lastSeen.put(c, right)` AFTER adjusting left — the new position of `c` is `right`, not the old one.\n\n4. **Using a Set instead of a Map**\n   A Set tells you IF a character is in the window but not WHERE. You would need to move left one-by-one until the duplicate is evicted — still correct but O(n²) in the worst case.',

    -- senior_variations
    E'**Variation 1 — Exactly K distinct characters**\nChange the condition: instead of "no duplicates", maintain at most K distinct characters. Use a frequency map; shrink left when `map.size() > K`.\n\n**Variation 2 — Unicode / multi-byte input**\nJava''s `char` is UTF-16; characters outside the BMP (emoji, CJK extensions) are surrogate pairs. Use `codePoints()` stream or `Character.codePointAt()` and store `Integer → Integer` in the map.\n\n**Variation 3 — Streaming input**\nIf the string arrives as a stream you cannot index backward. Rewrite using a `Deque<Character>` as the window and a frequency map. `left` becomes the logical start; evict from the front when the condition breaks.\n\n**Variation 4 — At 1 TB scale**\nPartition the string into overlapping chunks. Each worker processes `chunk[i - maxWindowSize .. i]` to avoid missing windows that span a boundary. Reduce by taking the global maximum. The overlap size is bounded by the charset size (128 for ASCII).'

FROM problems p
WHERE p.slug = 'longest-substring-without-repeating'
  AND NOT EXISTS (
      SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id
  );

-- ── 3. Insert follow-ups (standard + variations + senior) ─────────────────
-- Clear existing follow-ups for this problem to avoid duplicates on re-run
DELETE FROM problem_followups
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating');

INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('What if the string contains Unicode characters beyond the Basic Multilingual Plane (emoji, CJK extensions)? How does your solution change?', 'FOLLOWUP', 1),
    ('Can you solve this without a HashMap — using only a fixed-size array?', 'FOLLOWUP', 2),
    ('What changes if we want the longest substring with at most K distinct characters?', 'FOLLOWUP', 3),
    ('What is the longest substring with exactly 2 distinct characters?', 'VARIATION', 4),
    ('Find the length of the longest subarray with sum at most K.', 'VARIATION', 5),
    ('What if the input arrives as a character stream and you cannot index backward?', 'SENIOR', 6),
    ('How would you parallelize this across multiple machines for a 1 TB string?', 'SENIOR', 7),
    ('Design a real-time system that continuously reports the longest unique-character window as characters are appended.', 'SENIOR', 8)
) AS q(question, type, sort_order)
WHERE p.slug = 'longest-substring-without-repeating';
