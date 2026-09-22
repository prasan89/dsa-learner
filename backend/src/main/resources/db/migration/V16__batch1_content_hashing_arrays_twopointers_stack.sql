-- V16: Batch 1 — Complete gold-standard content for 5 foundational problems.
--
-- Problems: Two Sum, Valid Anagram, Best Time to Buy and Sell Stock,
--           Container With Most Water, Valid Parentheses
--
-- Every problem follows the same structure as the canonical
-- Longest Substring Without Repeating Characters (V12).
-- Content status set to CONTENT_REVIEW (not READY — technical validation required).

-- ══════════════════════════════════════════════════════════════════════════════
-- 1. TWO SUM  (hashing)
-- ══════════════════════════════════════════════════════════════════════════════

-- Upgrade hints to gold-standard quality
UPDATE hints
SET content = E'For each number `x` in the array, you need to find whether `target - x` already exists somewhere earlier in the array.\n\nThe challenge is doing this without scanning the entire array again for every element.',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'two-sum')
  AND level = 1;

UPDATE hints
SET content = E'A HashMap maps value → index. As you scan left to right, ask: "Have I already seen the number I need?" before adding the current number to the map.\n\nThis turns the inner scan into an O(1) lookup.',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'two-sum')
  AND level = 2;

UPDATE hints
SET content = E'Map<Integer, Integer> map = new HashMap<>();  // value → index\nfor (int i = 0; i < nums.length; i++) {\n    int complement = target - nums[i];\n    if (map.containsKey(complement)) {\n        return new int[]{map.get(complement), i};\n    }\n    map.put(nums[i], i);\n}\nthrow new IllegalArgumentException("No solution");\n\nKey: check BEFORE inserting — this naturally handles the constraint that you cannot use the same element twice.',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'two-sum')
  AND level = 3;

INSERT INTO problem_content (
    problem_id,
    recognition_note, pattern_recognition_clues, when_to_use, when_not_to_use,
    intuition, guided_reasoning, solution,
    brute_force, brute_time, brute_space,
    optimal_approach, optimal_time, optimal_space,
    pseudocode, why_this_works, invariant,
    common_mistakes, senior_variations,
    content_status
)
SELECT
    p.id,

    'Two Sum is the canonical HashMap problem. The key insight: instead of searching for the complement, store what you have seen and check on arrival.',

    E'**Keywords to watch for:**\n- "find two numbers that add up to"\n- "return indices of the pair"\n- "exactly one solution guaranteed"\n- "pair with target sum"\n\n**The decisive question:**\nCan I transform the search "find y such that x + y = target" into a lookup "have I already seen target - x?"\n\nIf yes → HashMap.',

    E'- You need to find a pair (or more generally a set) of elements satisfying a sum condition\n- The array is unsorted and sorting would destroy index information you need to return\n- You want a single-pass O(n) solution\n- The problem guarantees exactly one solution (or asks to return all)',

    E'- The array is sorted — use Two Pointers instead (O(n) with O(1) space)\n- You need the actual subarray sum, not a pair lookup — use prefix sums\n- k is large and you are building k-tuples — nested hashing or sorting + two-pointers may be more appropriate',

    E'The naive approach checks every pair (i, j) where i < j, asking "does nums[i] + nums[j] == target?". That is O(n²).\n\nThe key observation: for any element `nums[i]`, the value you need is completely determined — it is `target - nums[i]`. You do not need to scan the rest of the array; you just need to know whether that value was seen before.\n\nA HashMap lets you answer "have I seen this value before, and at which index?" in O(1). One pass over the array is enough.',

    E'Ask yourself these questions:\n\n**1. What information do we need per element?**\nFor each `nums[i]`, we want to know: "Is `target - nums[i]` in the array at a different index?"\n\n**2. What data structure supports "have I seen X?" in O(1)?**\nA HashMap (value → index) does exactly this.\n\n**3. When do we insert vs. when do we check?**\nCheck first (before inserting). If we inserted first, an element could pair with itself (e.g., `nums = [3, 4], target = 6` — without check-before-insert, 3 might match itself).\n\n**4. Does the single-pass approach work?**\nYes: by the time we check for `complement`, all earlier elements are in the map. If `complement` was at index j < i, we find it immediately.',

    E'public int[] twoSum(int[] nums, int target) {\n    Map<Integer, Integer> map = new HashMap<>();  // value → index\n    for (int i = 0; i < nums.length; i++) {\n        int complement = target - nums[i];\n        if (map.containsKey(complement)) {\n            return new int[]{map.get(complement), i};\n        }\n        map.put(nums[i], i);  // store AFTER checking\n    }\n    throw new IllegalArgumentException("No valid pair");\n}',

    E'Check every pair (i, j) where i < j:\n\n```java\npublic int[] twoSum(int[] nums, int target) {\n    for (int i = 0; i < nums.length; i++) {\n        for (int j = i + 1; j < nums.length; j++) {\n            if (nums[i] + nums[j] == target) {\n                return new int[]{i, j};\n            }\n        }\n    }\n    throw new IllegalArgumentException("No solution");\n}\n```\n\n**Why this fails at scale:** For n = 10⁴, this executes ~5 × 10⁷ comparisons. HashMap reduces this to n lookups.',

    'O(n²)',
    'O(1)',

    E'Single-pass HashMap: iterate the array once, maintaining a map of {value → index}. For each element, check if its complement already exists in the map before inserting.\n\n- Lookup and insert are both O(1) amortized (HashMap expected case).\n- Total: one pass, no extra iteration.',

    'O(n)',
    'O(n)',

    E'FUNCTION twoSum(nums, target):\n    map ← empty HashMap {value → index}\n\n    FOR i FROM 0 TO len(nums) - 1:\n        complement ← target - nums[i]\n\n        IF map contains complement:\n            RETURN [map[complement], i]\n\n        map[nums[i]] ← i      // store after checking\n\n    THROW "No solution found"',

    E'When we arrive at index `i` and check for `complement = target - nums[i]`, the map contains exactly the elements at indices 0 through i-1. If `complement` exists there, we have our answer. If not, we store `nums[i]` for future lookups.\n\nThe single-pass approach works because: if the answer pair is (j, i) with j < i, then when we reach i, j''s value is already in the map. We find it immediately.',

    'After processing index i, every element at index < i is in the map. We never miss a valid earlier element.',

    E'1. **Inserting before checking (self-pairing bug)**\n   `map.put(nums[i], i)` before checking allows an element to match itself when `target == 2 * nums[i]`.\n   Fix: always check `containsKey(complement)` before `put`.\n\n2. **Returning values instead of indices**\n   The problem asks for indices, not values. The HashMap must store value → index, not value → value.\n\n3. **Assuming sorted input**\n   Two Sum on an unsorted array does not support the two-pointer approach without first sorting — which destroys the original indices.\n\n4. **Ignoring the guarantee of exactly one solution**\n   The problem guarantees one solution. In variants where multiple solutions exist, you would collect all pairs instead of returning immediately.',

    E'**Variation 1 — Two Sum (sorted array)**\nSee Two Sum II: use two pointers at left=0, right=n-1. Move left++ if sum < target, right-- if sum > target. O(n) time, O(1) space — sorting destroyed index info, but that is acceptable here.\n\n**Variation 2 — Two Sum (all pairs, no duplicates)**\nReturn all unique pairs. Use a Set to deduplicate complements; or sort first and use two pointers to skip duplicates.\n\n**Variation 3 — Three Sum**\nFor each element x, reduce to Two Sum on the remaining array. Sort first (to handle duplicates and use two pointers). O(n²).\n\n**Variation 4 — Streaming / online**\nElements arrive one at a time; answer queries "does any previous element pair with this one to sum to target?" Insert each incoming element into a HashSet after answering. O(1) per query.',

    'CONTENT_REVIEW'
FROM problems p
WHERE p.slug = 'two-sum'
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id);

-- Follow-ups for Two Sum
DELETE FROM problem_followups WHERE problem_id = (SELECT id FROM problems WHERE slug = 'two-sum');
INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('Can you solve Two Sum in O(n) time with O(1) extra space? (Hint: what if the array were sorted?)', 'FOLLOWUP', 1),
    ('What changes if the input array is sorted? Use that to achieve O(1) space.', 'FOLLOWUP', 2),
    ('How would you find ALL pairs that sum to target, not just one?', 'VARIATION', 3),
    ('Extend this to Three Sum: find all unique triplets that sum to zero.', 'VARIATION', 4),
    ('What if the array is very large and cannot fit in memory? How would you find a pair summing to target?', 'SENIOR', 5),
    ('How would you design a data structure that supports add(number) and find(value) queries, where find returns true if any two numbers sum to value?', 'SENIOR', 6)
) AS q(question, type, sort_order)
WHERE p.slug = 'two-sum';

-- Test cases for Two Sum
INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, t.input, t.expected, t.hidden, t.ord
FROM problems p,
(VALUES
    ('4\n2 7 11 15\n9',   '[0,1]', false, 1),
    ('3\n3 2 4\n6',       '[1,2]', false, 2),
    ('2\n3 3\n6',         '[0,1]', true,  3),
    ('5\n1 5 3 7 2\n9',   '[1,4]', true,  4),
    ('2\n-1 -2\n-3',      '[0,1]', true,  5)
) AS t(input, expected, hidden, ord)
WHERE p.slug = 'two-sum'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id AND tc.input = t.input);


-- ══════════════════════════════════════════════════════════════════════════════
-- 2. VALID ANAGRAM  (hashing)
-- ══════════════════════════════════════════════════════════════════════════════

UPDATE hints
SET content = E'Two strings are anagrams if and only if they contain exactly the same characters with exactly the same frequencies.\n\nThe sorting approach (sort both, compare) is O(n log n). Can you do it in O(n)?',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-anagram')
  AND level = 1;

UPDATE hints
SET content = E'Use a frequency array of size 26 (for lowercase ASCII a-z). Increment for characters in `s`, decrement for characters in `t`.\n\nAfter processing both strings, if any count is non-zero, the strings are not anagrams.',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-anagram')
  AND level = 2;

UPDATE hints
SET content = E'if (s.length() != t.length()) return false;\n\nint[] freq = new int[26];\nfor (int i = 0; i < s.length(); i++) {\n    freq[s.charAt(i) - ''a'']++;\n    freq[t.charAt(i) - ''a'']--;\n}\n\nfor (int count : freq) {\n    if (count != 0) return false;\n}\nreturn true;\n\nThe length check short-circuits the most obvious non-anagram case before any work.',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-anagram')
  AND level = 3;

INSERT INTO problem_content (
    problem_id,
    recognition_note, pattern_recognition_clues, when_to_use, when_not_to_use,
    intuition, guided_reasoning, solution,
    brute_force, brute_time, brute_space,
    optimal_approach, optimal_time, optimal_space,
    pseudocode, why_this_works, invariant,
    common_mistakes, senior_variations,
    content_status
)
SELECT
    p.id,

    'Valid Anagram is the canonical character-frequency problem. Any time you need to check equivalence of character distributions, a frequency array (for bounded alphabets) or HashMap (for Unicode) is the right tool.',

    E'**Keywords to watch for:**\n- "same characters"\n- "anagram / permutation of"\n- "rearrange the letters"\n- "same frequency"\n\n**The decisive question:**\nDo two sequences contain the same multiset of elements?\n\nIf yes → frequency counting with a HashMap or array.',

    E'- You need to verify that two strings (or arrays) contain the same elements with the same multiplicity\n- The alphabet/value range is bounded (use an array) or unbounded (use a HashMap)\n- Single-pass O(n) frequency check is preferable to O(n log n) sort',

    E'- The problem requires finding the actual positions of matching characters, not just existence\n- You are checking subsequence, not permutation — frequency matching alone is insufficient\n- Order matters in the problem definition (then frequency equality is not enough)',

    E'Two strings are anagrams iff they have identical character frequencies. Any two strings with the same character distribution are re-arrangements of each other.\n\nThe O(n log n) approach sorts both strings and compares character-by-character. But sorting throws away information we do not need.\n\nThe O(n) approach: use the 26-element frequency array as a "difference accumulator". Increment for s, decrement for t. If all counts are zero at the end, frequencies matched perfectly.',

    E'Ask yourself:\n\n**1. What does "anagram" mean precisely?**\nSame characters, same counts, any order. This is exactly "same character frequency distribution".\n\n**2. Can we short-circuit early?**\nYes. If `s.length() != t.length()`, return false immediately. Anagrams must have the same length.\n\n**3. What data structure for frequencies?**\n- Only lowercase a-z: int[26] — direct index by `char - ''a''`, zero allocation overhead.\n- Unicode: HashMap<Character, Integer> — handles any codepoint.\n\n**4. Single pass or two passes?**\nSingle pass: increment for s, decrement for t simultaneously (since lengths match). Any non-zero entry means mismatch.',

    E'public boolean isAnagram(String s, String t) {\n    if (s.length() != t.length()) return false;\n\n    int[] freq = new int[26];\n    for (int i = 0; i < s.length(); i++) {\n        freq[s.charAt(i) - ''a'']++;\n        freq[t.charAt(i) - ''a'']--;\n    }\n\n    for (int count : freq) {\n        if (count != 0) return false;\n    }\n    return true;\n}',

    E'Sort both strings, then compare:\n\n```java\npublic boolean isAnagram(String s, String t) {\n    if (s.length() != t.length()) return false;\n    char[] sc = s.toCharArray();\n    char[] tc = t.toCharArray();\n    Arrays.sort(sc);\n    Arrays.sort(tc);\n    return Arrays.equals(sc, tc);\n}\n```\n\nThis is correct and concise. The O(n log n) cost from sorting is the only reason to prefer the frequency approach when n is large.',

    'O(n log n)',
    'O(n)',

    E'Single-pass frequency array:\n\n- Use `int[26]` for lowercase ASCII (or a HashMap for arbitrary Unicode).\n- One pass: increment for s, decrement for t at the same index.\n- After the pass, verify all counts are zero.',

    'O(n)',
    'O(1) — alphabet size is fixed (26), independent of input',

    E'FUNCTION isAnagram(s, t):\n    IF len(s) != len(t): RETURN false\n\n    freq ← int[26] initialized to 0\n\n    FOR i FROM 0 TO len(s) - 1:\n        freq[s[i] - ''a'']++\n        freq[t[i] - ''a'']--\n\n    FOR count IN freq:\n        IF count != 0: RETURN false\n\n    RETURN true',

    E'The frequency array is a compact summary of a string''s character distribution. Incrementing for s and decrementing for t turns "are these distributions equal?" into "is the difference vector all zeros?"\n\nBecause the alphabet is finite (26 letters), the array has fixed size regardless of input length, giving true O(1) space.',

    'After processing all characters, freq[i] represents (count of char i in s) minus (count of char i in t). For anagrams, every entry is 0.',

    E'1. **Forgetting the length check**\n   "cat" and "cats" would produce a freq array with one non-zero entry, but the length check catches this O(1) before any work.\n\n2. **Assuming only lowercase ASCII**\n   The problem states "lowercase English letters" — int[26] is safe here. But a common follow-up asks about Unicode. Switching to HashMap<Character, Integer> handles the general case.\n\n3. **Two separate passes vs. one pass**\n   You can process s and t in two separate loops (increment then decrement) or one combined loop. One loop is faster in practice (cache locality); both are O(n).\n\n4. **Modifying the original strings**\n   Sorting `toCharArray()` mutates a copy. If you sort the original string (which is immutable in Java anyway), you need `toCharArray()` first.',

    E'**Variation 1 — Unicode input**\nReplace int[26] with HashMap<Character, Integer>. Use `getOrDefault(c, 0)` to avoid NPE. The rest of the logic is identical.\n\n**Variation 2 — Group Anagrams**\nFor a list of strings, sort each string''s characters to get a canonical key, then group by key in a HashMap. O(n * k log k) where k = average word length.\n\n**Variation 3 — Permutation of substring**\nGiven strings s and p, find all start indices in s where a permutation of p begins. Use a sliding window of size p.length() and a frequency map. O(n).\n\n**Variation 4 — Stream of characters**\nMaintain a running frequency map. Each time a character is added or removed, update the map and a "mismatch count". If mismatch count == 0, the current window is an anagram.',

    'CONTENT_REVIEW'
FROM problems p
WHERE p.slug = 'valid-anagram'
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id);

DELETE FROM problem_followups WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-anagram');
INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('How would you modify the solution to handle Unicode characters (not just lowercase ASCII)?', 'FOLLOWUP', 1),
    ('Can you solve this in O(n) with O(1) extra space? (Hint: is int[26] truly O(1)?)', 'FOLLOWUP', 2),
    ('Given a list of strings, group all anagrams together. What is the time complexity?', 'VARIATION', 3),
    ('Find all start indices in a string s where a permutation of pattern p begins (Permutation in String).', 'VARIATION', 4),
    ('A stream of characters is appended one at a time. After each character, report whether the last p.length() characters form an anagram of p.', 'SENIOR', 5),
    ('How would you check if two very large files (each 10 GB) are anagrams of each other without loading either fully into memory?', 'SENIOR', 6)
) AS q(question, type, sort_order)
WHERE p.slug = 'valid-anagram';

INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, t.input, t.expected, t.hidden, t.ord
FROM problems p,
(VALUES
    ('anagram\nnagaram',   'true',  false, 1),
    ('rat\ncar',           'false', false, 2),
    ('a\na',               'true',  true,  3),
    ('ab\nba',             'true',  true,  4),
    ('aa\nbb',             'false', true,  5),
    ('aacc\nccac',         'false', true,  6)
) AS t(input, expected, hidden, ord)
WHERE p.slug = 'valid-anagram'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id AND tc.input = t.input);


-- ══════════════════════════════════════════════════════════════════════════════
-- 3. BEST TIME TO BUY AND SELL STOCK  (arrays)
-- ══════════════════════════════════════════════════════════════════════════════

UPDATE hints
SET content = E'You are looking for the maximum value of `prices[j] - prices[i]` where `j > i` (must buy before selling).\n\nA nested loop checking every (i, j) pair works but is O(n²). Can you find the answer in a single pass?',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'best-time-to-buy-sell-stock')
  AND level = 1;

UPDATE hints
SET content = E'Think about what you need to track at each day `j` to compute the best profit ending on day `j`:\n- The cheapest price you have seen SO FAR (on any day 0 through j-1).\n- The profit if you sell today: `prices[j] - minPriceSoFar`.\n\nTrack these two values as you scan left to right.',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'best-time-to-buy-sell-stock')
  AND level = 2;

UPDATE hints
SET content = E'int minPrice = Integer.MAX_VALUE;\nint maxProfit = 0;\n\nfor (int price : prices) {\n    if (price < minPrice) {\n        minPrice = price;           // found a cheaper buy day\n    } else if (price - minPrice > maxProfit) {\n        maxProfit = price - minPrice; // found a better sell day\n    }\n}\nreturn maxProfit;\n\nNote the else-if: on the same day you find a new minimum, you cannot simultaneously sell for profit.',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'best-time-to-buy-sell-stock')
  AND level = 3;

INSERT INTO problem_content (
    problem_id,
    recognition_note, pattern_recognition_clues, when_to_use, when_not_to_use,
    intuition, guided_reasoning, solution,
    brute_force, brute_time, brute_space,
    optimal_approach, optimal_time, optimal_space,
    pseudocode, why_this_works, invariant,
    common_mistakes, senior_variations,
    content_status
)
SELECT
    p.id,

    'Best Time to Buy and Sell Stock is the canonical single-pass running minimum problem. The key recognition: the best sell day given buy day i is always the global minimum before i, so you only need to track one variable.',

    E'**Keywords to watch for:**\n- "maximize profit"\n- "buy before sell"\n- "single transaction"\n- "minimum so far"\n- "running maximum/minimum"\n\n**The decisive question:**\nDoes the optimal answer for position j depend only on the best possible previous position (the minimum before j)?\n\nIf yes → single-pass tracking of a running extreme.',

    E'- You are maximizing/minimizing a function of two positions (i, j) with i < j\n- The constraint j > i means you must scan left-to-right\n- The best i for any given j is the global minimum in positions 0..j-1\n- A single pass tracking the running minimum is sufficient',

    E'- You need to make multiple transactions (see Buy and Sell Stock II — greedy approach)\n- You must hold at most k transactions (see Buy and Sell Stock III/IV — DP approach)\n- You need to track the buy/sell pair, not just the profit (augment the tracking variables)',

    E'The brute force checks every pair (buy day i, sell day j) where i < j. That is O(n²) pairs.\n\nThe key observation: for any sell day j, the optimal buy day is the day with the lowest price among all days 0 through j-1. This is the running minimum — a single number that can be maintained in O(1) as you scan.\n\nSo at each day, ask: "If I sell today, what is the best profit?" = `prices[j] - runningMin`. Update the running maximum of this value.',

    E'Ask yourself:\n\n**1. What does the profit depend on?**\nProfit on day j = prices[j] - prices[i] for some i < j. For fixed j, profit is maximized by minimizing prices[i], i.e., choosing the cheapest possible buy day before j.\n\n**2. Can we compute this cheapest buy day incrementally?**\nYes: maintain `minPrice` as the minimum of prices[0..j-1]. This is updated in O(1) at each step.\n\n**3. When can we sell for profit?**\nOnly when `prices[j] > minPrice`. Track `maxProfit = max(maxProfit, prices[j] - minPrice)`.\n\n**4. Why can we not buy and sell on the same day?**\nThe problem requires selling AFTER buying. The `else-if` structure ensures we do not count same-day buy-sell.\n\n**5. What if prices are strictly decreasing?**\nWe never sell (maxProfit stays 0). The constraint "return 0 if no profit possible" is handled automatically.',

    E'public int maxProfit(int[] prices) {\n    int minPrice = Integer.MAX_VALUE;\n    int maxProfit = 0;\n\n    for (int price : prices) {\n        if (price < minPrice) {\n            minPrice = price;               // new cheapest buy day\n        } else {\n            maxProfit = Math.max(maxProfit, price - minPrice);  // best profit selling today\n        }\n    }\n    return maxProfit;\n}',

    E'Check every pair (i, j) where i < j:\n\n```java\npublic int maxProfit(int[] prices) {\n    int maxProfit = 0;\n    for (int i = 0; i < prices.length; i++) {\n        for (int j = i + 1; j < prices.length; j++) {\n            maxProfit = Math.max(maxProfit, prices[j] - prices[i]);\n        }\n    }\n    return maxProfit;\n}\n```\n\nFor n = 10⁵, this is ~5 × 10⁹ comparisons — clearly too slow.',

    'O(n²)',
    'O(1)',

    E'Single pass with two variables:\n- `minPrice`: the lowest price seen so far (initialized to MAX_VALUE)\n- `maxProfit`: the best profit seen so far (initialized to 0)\n\nAt each price:\n- If it is less than minPrice, update minPrice\n- Otherwise, compute profit if sold today and update maxProfit',

    'O(n)',
    'O(1)',

    E'FUNCTION maxProfit(prices):\n    minPrice ← MAX_INT\n    maxProfit ← 0\n\n    FOR price IN prices:\n        IF price < minPrice:\n            minPrice ← price\n        ELSE:\n            maxProfit ← MAX(maxProfit, price - minPrice)\n\n    RETURN maxProfit',

    E'At each index j, `minPrice` equals `min(prices[0..j])`. Therefore `prices[j] - minPrice` is the maximum profit achievable by buying at any point before or at j and selling at j.\n\nThe else-if ensures we do not compute profit on the same day we update the minimum (you cannot buy and sell on the same day for profit of 0).\n\nInitializing `minPrice = Integer.MAX_VALUE` means the first element will always become the initial minPrice — a clean sentinel value.',

    'At index j, minPrice = min(prices[0..j]) and maxProfit = max profit achievable using any buy day in [0..j] and corresponding sell day ≤ j.',

    E'1. **Initializing minPrice to prices[0] instead of MAX_VALUE**\n   Both are correct, but `Integer.MAX_VALUE` is safer for the general pattern and does not require special-casing an empty array.\n\n2. **Not returning 0 for all-decreasing input**\n   Initializing `maxProfit = 0` handles this: we never update it if all differences are negative.\n\n3. **Using a nested loop for "best sell day"**\n   This is the common first attempt. The insight is that the best buy day for any sell day j is always just the running minimum — no inner loop needed.\n\n4. **Confusing with "multiple transactions" variant**\n   This problem allows exactly one transaction. The greedy approach for multiple transactions (sum all positive day-to-day differences) does NOT apply here.',

    E'**Variation 1 — Best Time II (unlimited transactions)**\nAdd profit whenever `prices[i] > prices[i-1]`. This is equivalent to buying at every valley and selling at every peak. O(n), O(1).\n\n**Variation 2 — Best Time III (at most 2 transactions)**\nDynamic programming. Track states: hold1, sold1, hold2, sold2. Transition each state with one pass. O(n), O(1).\n\n**Variation 3 — Best Time IV (at most k transactions)**\nGeneralize: DP with states dp[transaction][day]. For k ≥ n/2, reduce to unlimited-transaction case. O(n*k) time.\n\n**Variation 4 — With cooldown (1 day rest after selling)**\nDP with states: held, sold, rest. Transition: held[i] = max(held[i-1], rest[i-1]-price); sold[i] = held[i-1]+price; rest[i] = max(rest[i-1], sold[i-1]).',

    'CONTENT_REVIEW'
FROM problems p
WHERE p.slug = 'best-time-to-buy-sell-stock'
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id);

DELETE FROM problem_followups WHERE problem_id = (SELECT id FROM problems WHERE slug = 'best-time-to-buy-sell-stock');
INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('What if you are allowed to make as many transactions as you like (buy/sell multiple times)?', 'FOLLOWUP', 1),
    ('How would you also return the actual buy and sell days, not just the maximum profit?', 'FOLLOWUP', 2),
    ('What if you can make at most 2 transactions?', 'VARIATION', 3),
    ('What if there is a transaction fee for each sale?', 'VARIATION', 4),
    ('What if you must wait 1 day after selling before you can buy again (cooldown)?', 'VARIATION', 5),
    ('Design a real-time system that maintains the maximum profit as stock prices stream in. What data structure supports this with O(1) updates?', 'SENIOR', 6),
    ('How would you generalize to at most K transactions? Analyze the time and space complexity of your DP.', 'SENIOR', 7)
) AS q(question, type, sort_order)
WHERE p.slug = 'best-time-to-buy-sell-stock';

INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, t.input, t.expected, t.hidden, t.ord
FROM problems p,
(VALUES
    ('6\n7 1 5 3 6 4', '5',  false, 1),
    ('5\n7 6 4 3 1',   '0',  false, 2),
    ('1\n5',           '0',  true,  3),
    ('2\n1 2',         '1',  true,  4),
    ('4\n3 1 4 2',     '3',  true,  5),
    ('6\n2 4 1 7 3 8', '7',  true,  6)
) AS t(input, expected, hidden, ord)
WHERE p.slug = 'best-time-to-buy-sell-stock'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id AND tc.input = t.input);


-- ══════════════════════════════════════════════════════════════════════════════
-- 4. CONTAINER WITH MOST WATER  (two pointers)
-- ══════════════════════════════════════════════════════════════════════════════

UPDATE hints
SET content = E'The area of the container formed by lines i and j is:\n\n`area = min(height[i], height[j]) * (j - i)`\n\nTwo things affect area: the width (j - i) and the height (the shorter line). Starting with the widest possible container (i=0, j=n-1) gives the maximum width.',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'container-with-most-water')
  AND level = 1;

UPDATE hints
SET content = E'Start with pointers at both ends (maximum width). On each step, the width must shrink by 1 no matter what.\n\nThe area is limited by the SHORTER of the two lines. Moving the TALLER pointer inward cannot increase the height (still limited by the shorter one) while the width decreases — strictly worse or equal.\n\nTherefore: always move the pointer pointing to the SHORTER line.',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'container-with-most-water')
  AND level = 2;

UPDATE hints
SET content = E'int left = 0, right = height.length - 1;\nint maxArea = 0;\n\nwhile (left < right) {\n    int area = Math.min(height[left], height[right]) * (right - left);\n    maxArea = Math.max(maxArea, area);\n    if (height[left] < height[right]) {\n        left++;\n    } else {\n        right--;\n    }\n}\nreturn maxArea;\n\nWhen heights are equal, either pointer can move — the result is the same.',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'container-with-most-water')
  AND level = 3;

INSERT INTO problem_content (
    problem_id,
    recognition_note, pattern_recognition_clues, when_to_use, when_not_to_use,
    intuition, guided_reasoning, solution,
    brute_force, brute_time, brute_space,
    optimal_approach, optimal_time, optimal_space,
    pseudocode, why_this_works, invariant,
    common_mistakes, senior_variations,
    content_status
)
SELECT
    p.id,

    'Container With Most Water is the canonical two-pointer greedy problem on unsorted arrays. The key recognition: the area depends on the SHORTER of two lines, so we can safely eliminate the shorter-line candidate.',

    E'**Keywords to watch for:**\n- "two indices forming a container"\n- "area between two lines"\n- "maximize/minimize over pairs (i, j) with i < j"\n- "width matters"\n\n**The decisive question:**\nCan I start with the widest possible configuration and use a greedy argument to safely move one pointer inward at each step?\n\nIf yes → Two Pointers converging from both ends.',

    E'- You are optimizing over pairs (left, right) where the pair spans a range\n- The contribution of a pair depends on both position (width) and value (height)\n- A greedy argument exists to eliminate one pointer: the side that CANNOT improve the current constraint\n- Starting from the extreme (widest/largest) configuration is natural',

    E'- The array is sorted and you are looking for a sum — use the sum-based two-pointer approach instead\n- You need non-adjacent elements or non-contiguous spans\n- The greedy argument does not hold (no clear "which side to move" rule)',

    E'The brute force checks all O(n²) pairs (i, j) and computes area for each. The challenge: can we skip any pairs safely?\n\nThe two-pointer insight: start at (left=0, right=n-1) — the widest possible container. At each step, the width shrinks by 1. To potentially increase the area, we would need the height to compensate. The height is limited by the SHORTER line. Moving the taller line inward cannot increase the height constraint (still bounded by the shorter one), so area is guaranteed to not improve. Moving the shorter line might reveal a taller line — the only hope for improvement.',

    E'Ask yourself:\n\n**1. What determines the area?**\nArea = min(height[left], height[right]) × (right - left). Width is (right - left). Height is the shorter wall.\n\n**2. Why start at the widest point?**\nWe guarantee we check the maximum possible width. As we move inward, width decreases, so we need height to compensate.\n\n**3. Which pointer should we move?**\nWe MUST move one pointer (narrowing the window). Moving the taller pointer is always sub-optimal: the height is still limited by the shorter one, and width just decreased. So move the shorter pointer — at least there is a chance of finding something taller.\n\n**4. What if both heights are equal?**\nMove either. If there is a solution with greater area, it must involve at least one line taller than either current line — and that line is somewhere in the remaining range.',

    E'public int maxArea(int[] height) {\n    int left = 0, right = height.length - 1;\n    int maxArea = 0;\n\n    while (left < right) {\n        int area = Math.min(height[left], height[right]) * (right - left);\n        maxArea = Math.max(maxArea, area);\n\n        if (height[left] < height[right]) {\n            left++;\n        } else {\n            right--;  // move right when equal too — valid either way\n        }\n    }\n    return maxArea;\n}',

    E'Check all pairs (i, j) where i < j:\n\n```java\npublic int maxArea(int[] height) {\n    int maxArea = 0;\n    for (int i = 0; i < height.length; i++) {\n        for (int j = i + 1; j < height.length; j++) {\n            int area = Math.min(height[i], height[j]) * (j - i);\n            maxArea = Math.max(maxArea, area);\n        }\n    }\n    return maxArea;\n}\n```\n\nFor n = 10⁵ this is ~5 × 10⁹ operations — too slow.',

    'O(n²)',
    'O(1)',

    E'Two pointers starting at both ends, converging toward the center.\n\nAt each step: compute area, update maximum, then move the shorter pointer inward.\n\nTermination: when left == right, all valid pairs have been considered (by the greedy argument).',

    'O(n)',
    'O(1)',

    E'FUNCTION maxArea(height):\n    left ← 0\n    right ← len(height) - 1\n    maxArea ← 0\n\n    WHILE left < right:\n        area ← MIN(height[left], height[right]) × (right - left)\n        maxArea ← MAX(maxArea, area)\n\n        IF height[left] < height[right]:\n            left ← left + 1\n        ELSE:\n            right ← right - 1\n\n    RETURN maxArea',

    E'**Correctness argument (informal):** Consider any pair (i, j) with i < j. At some point in the algorithm, either left = i or right = j (or both). When left = i, if height[i] < height[j], we move left past i — meaning any pair (i, j'') for j'' < right is dominated by (i, right) in terms of width, and the height was already limited by height[i]. We did not miss a better pair.',

    'At each step, the unexamined pairs (i.e., pairs not yet computed) are all dominated by either the current pair or a pair already computed.',

    E'1. **Moving the TALLER pointer instead of the shorter**\n   The greedy argument only holds for moving the shorter pointer. Moving the taller one can miss the optimal answer.\n\n2. **Off-by-one on width**\n   Width = `right - left`, not `right - left + 1`. The width is the number of units between the lines, not the number of lines.\n\n3. **Confusing with Trapping Rain Water**\n   Container With Most Water asks for the area between two chosen lines. Trapping Rain Water asks for total water trapped across ALL bars. The algorithms are different.\n\n4. **Using `else if` when heights are equal**\n   When `height[left] == height[right]`, moving either pointer is correct. Using `else` (covering the equal case) is fine.',

    E'**Variation 1 — Return the pair of indices**\nTrack `bestLeft` and `bestRight` alongside `maxArea`. Update them whenever maxArea updates.\n\n**Variation 2 — 3D version: find max volume box**\nExtends to 3D (given heights in a 2D grid, find the box with maximum volume). No simple O(n) solution — typically O(n² log n) with monotonic stacks.\n\n**Variation 3 — Trapping Rain Water**\nRelated but different: compute total water trapped across all bars (not just between two). Uses two-pointer or monotonic stack approach; the area is computed differently for each bar.\n\n**Variation 4 — Streaming heights**\nIf heights arrive online (streaming), the two-pointer approach does not directly apply. Maintain a monotonic stack to efficiently answer "if I extend to position j, what is the best left boundary?"',

    'CONTENT_REVIEW'
FROM problems p
WHERE p.slug = 'container-with-most-water'
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id);

DELETE FROM problem_followups WHERE problem_id = (SELECT id FROM problems WHERE slug = 'container-with-most-water');
INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('How would you also return the actual pair of indices that achieves the maximum area?', 'FOLLOWUP', 1),
    ('Why is it always safe to move the shorter pointer? Sketch a proof.', 'FOLLOWUP', 2),
    ('How does this differ from Trapping Rain Water? Why can''t you use the same algorithm?', 'VARIATION', 3),
    ('What if there are duplicate heights? Does the algorithm still work correctly?', 'VARIATION', 4),
    ('Extend to 3D: given a 2D grid of heights, find the maximum volume rectangular prism you can form using two cells as opposite corners.', 'SENIOR', 5),
    ('How would you handle this if heights arrive as a data stream and you must report the maximum area at each step?', 'SENIOR', 6)
) AS q(question, type, sort_order)
WHERE p.slug = 'container-with-most-water';

INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, t.input, t.expected, t.hidden, t.ord
FROM problems p,
(VALUES
    ('9\n1 8 6 2 5 4 8 3 7',    '49', false, 1),
    ('2\n1 1',                   '1',  false, 2),
    ('3\n1 2 1',                 '2',  true,  3),
    ('4\n4 3 2 4',               '16', true,  4),
    ('2\n10000 10000',           '10000', true, 5)
) AS t(input, expected, hidden, ord)
WHERE p.slug = 'container-with-most-water'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id AND tc.input = t.input);


-- ══════════════════════════════════════════════════════════════════════════════
-- 5. VALID PARENTHESES  (stack)
-- ══════════════════════════════════════════════════════════════════════════════

UPDATE hints
SET content = E'Brackets must be closed in LIFO (last-in, first-out) order: the most recently opened bracket must be the first to be closed.\n\nThis is the defining property of a stack. A stack remembers the history of unclosed brackets in the order they were opened.',
    label   = 'Concept'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-parentheses')
  AND level = 1;

UPDATE hints
SET content = E'Push every opening bracket onto the stack. When you see a closing bracket:\n1. If the stack is empty, there is no matching opening bracket — invalid.\n2. Pop the top of the stack. If it does not match the current closing bracket — invalid.\n\nAt the end, the string is valid only if the stack is empty (no unclosed brackets).',
    label   = 'Direction'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-parentheses')
  AND level = 2;

UPDATE hints
SET content = E'Map<Character, Character> matchFor = Map.of('')'', ''('', ''}'', ''{'', '']'', ''['');\n\nDeque<Character> stack = new ArrayDeque<>();\n\nfor (char c : s.toCharArray()) {\n    if (matchFor.containsKey(c)) {         // closing bracket\n        if (stack.isEmpty() || stack.peek() != matchFor.get(c)) {\n            return false;                  // no match\n        }\n        stack.pop();\n    } else {\n        stack.push(c);                     // opening bracket\n    }\n}\nreturn stack.isEmpty();',
    label   = 'Algorithm'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-parentheses')
  AND level = 3;

INSERT INTO problem_content (
    problem_id,
    recognition_note, pattern_recognition_clues, when_to_use, when_not_to_use,
    intuition, guided_reasoning, solution,
    brute_force, brute_time, brute_space,
    optimal_approach, optimal_time, optimal_space,
    pseudocode, why_this_works, invariant,
    common_mistakes, senior_variations,
    content_status
)
SELECT
    p.id,

    'Valid Parentheses is the canonical stack problem. The key recognition: any time you need to verify that nested structures are correctly matched and closed in the right order, think LIFO — that is a stack.',

    E'**Keywords to watch for:**\n- "matching brackets / parentheses"\n- "valid / well-formed expression"\n- "nested structure"\n- "balanced brackets"\n- "open must be closed in correct order"\n\n**The decisive question:**\nDo opening tokens need to be matched with closing tokens in last-in, first-out order?\n\nIf yes → Stack.',

    E'- You need to match opening structures with their corresponding closing structures\n- The matching must respect nesting order (inner brackets close before outer)\n- LIFO (last opened = first closed) is the invariant\n- A single pass over the input suffices',

    E'- You just need to count brackets (e.g., only one type: if opens == closes → valid). For a single bracket type, a counter is sufficient and simpler than a stack.\n- The problem is about a different structure (e.g., trees, graphs) where LIFO order is not the constraint',

    E'A naive approach might try removing adjacent matched pairs repeatedly until nothing more can be removed. This is O(n²) in the worst case.\n\nThe stack insight: we never need to look back further than the most recently opened, unmatched bracket. A stack naturally maintains this "most recent first" ordering.\n\nWhen we encounter a closing bracket, the only bracket it can validly match is the innermost unmatched opening bracket — the top of the stack.',

    E'Ask yourself:\n\n**1. What do I need to remember at each step?**\nThe sequence of unmatched opening brackets, in the order I opened them. I only care about the MOST RECENT unmatched opening bracket when I encounter a closing bracket.\n\n**2. Why is a counter not enough?**\nFor a single bracket type like `()`, a counter works: increment for `(`, decrement for `)`, valid if it never goes negative and ends at 0. But for multiple types, you need to know WHICH type is innermost — only a stack gives you this.\n\n**3. How do we map closing → opening brackets?**\nA static Map (or if-else) maps `)→(`, `}→{`, `]→[`. When we see `)`, we need to check that the stack top is `(`.\n\n**4. What are the failure modes?**\n- Closing bracket with empty stack: no opening bracket to match.\n- Closing bracket that does not match the stack top: wrong type.\n- Non-empty stack at the end: unmatched opening brackets remain.',

    E'public boolean isValid(String s) {\n    Deque<Character> stack = new ArrayDeque<>();\n\n    for (char c : s.toCharArray()) {\n        if (c == ''('' || c == ''{'' || c == ''['') {\n            stack.push(c);          // push opening brackets\n        } else {\n            if (stack.isEmpty()) return false;  // no opening bracket to match\n            char top = stack.pop();\n            if ((c == '')'' && top != ''('') ||\n                (c == ''}'' && top != ''{'') ||\n                (c == '']'' && top != ''['')) {\n                return false;       // type mismatch\n            }\n        }\n    }\n    return stack.isEmpty();  // all brackets matched\n}',

    E'A simple but O(n²) approach: repeatedly scan the string and remove the innermost matched pairs until no more can be removed. If the string becomes empty, it was valid.\n\n```java\npublic boolean isValid(String s) {\n    while (s.contains("()") || s.contains("{}") || s.contains("[]")) {\n        s = s.replace("()", "").replace("{}", "").replace("[]", "");\n    }\n    return s.isEmpty();\n}\n```\n\nEach pass removes at least 2 characters. In the worst case (n/2 passes × n replacements) this is O(n²). String creation on each pass is expensive.',

    'O(n²)',
    'O(n)',

    E'Single-pass with a stack:\n- Push every opening bracket.\n- On each closing bracket: check stack non-empty, pop, verify type matches.\n- After full scan: stack must be empty.',

    'O(n)',
    'O(n) — up to n/2 unmatched opening brackets on the stack',

    E'FUNCTION isValid(s):\n    stack ← empty stack\n\n    FOR c IN s:\n        IF c is an opening bracket:\n            PUSH c onto stack\n        ELSE:  // closing bracket\n            IF stack is empty:\n                RETURN false\n            top ← POP from stack\n            IF top does not match c:\n                RETURN false\n\n    RETURN stack is empty',

    E'The stack encodes exactly the set of opening brackets that have been opened but not yet closed, in the order they were opened. When a closing bracket arrives, it can only validly close the most recently opened unmatched bracket — the stack top.\n\nIf the stack top does not match: the inner nesting is broken (e.g., `([)` — the `[` was opened inside `(` but closed with `)`).\n\nThe final `stack.isEmpty()` check covers the case of unmatched opening brackets at the end.',

    'At every point in the scan, the stack contains the opening brackets that have been opened but not yet closed, in left-to-right order (bottom to top).',

    E'1. **Not checking `stack.isEmpty()` before popping**\n   Calling `stack.pop()` on an empty stack throws `EmptyStackException`. Always check `isEmpty()` first.\n\n2. **Returning true when the stack is non-empty**\n   A string like `"((("` passes all the per-bracket checks (no closing bracket ever causes a mismatch), but the stack is not empty at the end — invalid. Always check `stack.isEmpty()` at the end.\n\n3. **Using Stack<Character> instead of Deque<Character>**\n   `java.util.Stack` is a legacy class backed by `Vector` (synchronized). Prefer `ArrayDeque` for O(1) push/pop and no synchronization overhead.\n\n4. **Trying to use a counter for multiple bracket types**\n   A single counter works for one bracket type but fails for multiple. `"([)]"` has 2 opens and 2 closes (count = 0) but is invalid.',

    E'**Variation 1 — Score of Parentheses**\nAssign scores: `()` = 1, `(A)` = 2×score(A), `AB` = score(A) + score(B). Compute using a stack tracking running totals. O(n).\n\n**Variation 2 — Minimum add to make valid**\nCount unmatched opens (stack size) and unmatched closes (early pops from empty stack). Answer = sum of both counts.\n\n**Variation 3 — Longest valid parentheses substring**\nUse a stack of indices. Push index of `(`. On `)`, pop; if stack empty push current index as the "reset boundary". Answer = max(i - stack.top). O(n), O(n).\n\n**Variation 4 — Generate all valid parentheses combinations**\nBacktracking: at each step, add `(` if opens < n, add `)` if closes < opens. Pruning ensures correctness. Generates the nth Catalan number of strings.',

    'CONTENT_REVIEW'
FROM problems p
WHERE p.slug = 'valid-parentheses'
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id = p.id);

DELETE FROM problem_followups WHERE problem_id = (SELECT id FROM problems WHERE slug = 'valid-parentheses');
INSERT INTO problem_followups (problem_id, question, type, sort_order)
SELECT p.id, q.question, q.type, q.sort_order
FROM problems p,
(VALUES
    ('Can you solve this with O(1) extra space? (Hint: only if the input contains one type of bracket.)', 'FOLLOWUP', 1),
    ('How many additions are needed to make an invalid string valid?', 'FOLLOWUP', 2),
    ('Find the length of the longest valid parentheses substring.', 'VARIATION', 3),
    ('Generate all valid combinations of n pairs of parentheses.', 'VARIATION', 4),
    ('Given a string with wildcards (*) that can be (, ) or empty, determine if the string is valid.', 'VARIATION', 5),
    ('How would you compute the "score" of a valid parentheses string where () = 1 and (A) = 2*A and AB = A+B?', 'SENIOR', 6),
    ('Design a streaming validator: brackets arrive one at a time, and at any point you must be able to report whether the sequence seen so far COULD still become valid.', 'SENIOR', 7)
) AS q(question, type, sort_order)
WHERE p.slug = 'valid-parentheses';

INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, t.input, t.expected, t.hidden, t.ord
FROM problems p,
(VALUES
    ('()',          'true',  false, 1),
    ('()[]{}',      'true',  false, 2),
    ('(]',          'false', false, 3),
    ('{[]}',        'true',  true,  4),
    ('([)]',        'false', true,  5),
    ('{',           'false', true,  6),
    ('',            'true',  true,  7)
) AS t(input, expected, hidden, ord)
WHERE p.slug = 'valid-parentheses'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id AND tc.input = t.input);
