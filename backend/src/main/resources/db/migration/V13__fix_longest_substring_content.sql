-- V13: Fix content quality issues in the gold-standard Longest Substring problem.
--
-- Fix 1: Brute-force prose said "O(n³) in the worst case" but the shown
--        implementation uses HashSet.contains() which is O(1), making it O(n²).
--        Removed the incorrect O(n³) claim.
--
-- Fix 2: Single \n in markdown fields collapses to a space in CommonMark.
--        All paragraph/list separators now use \n\n so ReactMarkdown renders
--        them as distinct blocks.

UPDATE problem_content
SET
    pattern_recognition_clues = E'**Keywords to watch for:**\n\n- "longest substring"\n- "contiguous"\n- "without repeating"\n- "at most K distinct"\n- "window"\n- "subarray with condition"\n\n**The decisive question:**\n\nCan I expand/shrink a window over the input and maintain a condition cheaply?\n\nIf yes → Sliding Window.',

    when_to_use = E'- The input is a contiguous sequence (string, array)\n\n- You are optimizing over subarrays or substrings\n\n- The condition on the current window can be checked/updated in O(1) as the window moves\n\n- You want a single-pass O(n) solution',

    when_not_to_use = E'- The problem requires non-contiguous subsequences (use DP instead)\n\n- You need to consider arbitrary subsets, not windows\n\n- The order of elements does not matter\n\n- The window condition requires global state across multiple passes',

    intuition = E'Scanning every possible substring from scratch is O(n²) or worse. The insight is that most of that work is redundant.\n\nWhen we extend the window to the right and hit a duplicate, we do not need to restart from scratch. The only thing that caused the window to become invalid was the **newly added character**. Everything before the duplicate position was already valid.\n\nSo we simply jump the left boundary past the previous occurrence of the duplicate — the window shrinks just enough to become valid again, and we continue forward.\n\nThis gives us a single left-to-right pass: O(n).',

    guided_reasoning = E'Ask yourself these questions:\n\n**1. What do we need to remember?**\n\nWe need to know, for any character we encounter, whether it already exists in the current window — and if so, where.\n\n**2. Can we avoid re-scanning?**\n\nYes. Instead of re-scanning the window on each step, maintain a HashMap that stores the *last seen index* of each character.\n\n**3. What does "shrink the window" actually mean?**\n\nWhen `right` encounters character `c` that is already in the window at index `k`:\n\n- We must move `left` to at least `k + 1`\n\n- But `left` must never move backward (use `Math.max`)\n\n**4. When do we update the answer?**\n\nAfter each `right` step, `right - left + 1` is the current valid window size. Track the maximum.',

    brute_force = E'Check every possible substring and test whether it contains duplicate characters.\n\n```java\npublic int lengthOfLongestSubstring(String s) {\n    int max = 0;\n    for (int i = 0; i < s.length(); i++) {\n        Set<Character> seen = new HashSet<>();\n        for (int j = i; j < s.length(); j++) {\n            if (seen.contains(s.charAt(j))) break;\n            seen.add(s.charAt(j));\n            max = Math.max(max, j - i + 1);\n        }\n    }\n    return max;\n}\n```\n\n**Why this fails at scale:** `seen.contains()` is O(1), so each inner loop is O(n). With n starting positions the total is **O(n²)**. For n = 10⁵ this is 10¹⁰ operations — far too slow.',

    brute_time = 'O(n²)',

    optimal_approach = E'Use a **sliding window** with a HashMap storing each character''s last seen index.\n\n- `right` expands the window one character at a time.\n\n- When `right` encounters a duplicate **inside** the current window, `left` jumps to `lastSeen[c] + 1`.\n\n- The map always reflects the most recent position of every character.\n\n- Window size at each step: `right - left + 1`.\n\nCritical detail: use `Math.max(left, lastSeen[c] + 1)` when updating `left`. Without the max, you can accidentally move `left` backward when a character was last seen before the current window started.',

    why_this_works = E'When `right` moves forward, the window can only become invalid because of the **newly added character** — not because of any earlier position. This means we never need to re-examine anything to the left of `lastSeen[c] + 1`.\n\nThe HashMap gives us O(1) lookup of "where was this character last?", so every step is constant time. Combined with the single forward pass, the total is O(n).\n\nThe `Math.max(left, ...)` guard is essential: a character may have appeared in the string before the current window opened. Without the guard, we would incorrectly shrink `left` backward, allowing a stale duplicate back into the window.',

    common_mistakes = E'1. **Forgetting Math.max when updating left**\n\n   `left = lastSeen.get(c) + 1` without `Math.max(left, ...)` lets left move backward, re-introducing a character that was already outside the window.\n\n2. **Off-by-one on window size**\n\n   Window size is `right - left + 1`, not `right - left`.\n\n3. **Not updating the map after adjusting left**\n\n   Always call `lastSeen.put(c, right)` AFTER adjusting left — the new position of `c` is `right`, not the old one.\n\n4. **Using a Set instead of a Map**\n\n   A Set tells you IF a character is in the window but not WHERE. You would need to move left one-by-one until the duplicate is evicted — still correct but O(n²) in the worst case.',

    senior_variations = E'**Variation 1 — Exactly K distinct characters**\n\nChange the condition: instead of "no duplicates", maintain at most K distinct characters. Use a frequency map; shrink left when `map.size() > K`.\n\n**Variation 2 — Unicode / multi-byte input**\n\nJava''s `char` is UTF-16; characters outside the BMP (emoji, CJK extensions) are surrogate pairs. Use `codePoints()` stream or `Character.codePointAt()` and store `Integer → Integer` in the map.\n\n**Variation 3 — Streaming input**\n\nIf the string arrives as a stream you cannot index backward. Rewrite using a `Deque<Character>` as the window and a frequency map. `left` becomes the logical start; evict from the front when the condition breaks.\n\n**Variation 4 — At 1 TB scale**\n\nPartition the string into overlapping chunks. Each worker processes `chunk[i - maxWindowSize .. i]` to avoid missing windows that span a boundary. Reduce by taking the global maximum. The overlap size is bounded by the charset size (128 for ASCII).'

WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating');
