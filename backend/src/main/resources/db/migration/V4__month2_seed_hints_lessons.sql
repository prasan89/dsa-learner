-- Update patterns with lesson content
UPDATE patterns SET
    lesson_markdown  = '## Arrays Pattern\n\nArrays give you **O(1) random access** by index. Most array problems reduce to one of:\n- Iterate and track state (running sum, max, count)\n- Two indices scanning from both ends\n- Prefix sums for range queries\n- In-place swap/rotation tricks\n\n### When to use\nThe problem gives you a contiguous sequence and asks you to find, count, or transform elements.\n\n### Complexity\nMost array scans are O(n) time, O(1) extra space when done in-place.',
    time_complexity  = 'O(n)',
    space_complexity = 'O(1)'
WHERE slug = 'arrays';

UPDATE patterns SET
    lesson_markdown  = '## Hashing Pattern\n\nA **HashMap** turns O(n) search into O(1) lookup. The core trick:\n```\nfor each element x:\n    if complement(x) is in map → answer found\n    else store x in map\n```\nUsed for: two-sum variants, frequency counts, duplicate detection, grouping anagrams.\n\n### When to use\nYou need fast lookup of "have I seen X before?" or "how many times did X appear?"',
    time_complexity  = 'O(n)',
    space_complexity = 'O(n)'
WHERE slug = 'hashing';

UPDATE patterns SET
    lesson_markdown  = '## Two Pointers Pattern\n\nPlace one pointer at each end (or one slow + one fast) and **move them toward a goal**.\n\n```\nleft = 0; right = n - 1;\nwhile (left < right) {\n    if (condition met) return result;\n    else if (need bigger) left++;\n    else right--;\n}\n```\n\n### When to use\nSorted array + find pair with target sum. Or: remove duplicates, reverse in-place, check palindrome.',
    time_complexity  = 'O(n)',
    space_complexity = 'O(1)'
WHERE slug = 'two-pointers';

UPDATE patterns SET
    lesson_markdown  = '## Sliding Window Pattern\n\nMaintain a **window [left, right]** that expands right and shrinks left when a constraint is violated.\n\n```\nint left = 0, result = 0;\nfor (int right = 0; right < n; right++) {\n    // expand: add arr[right] to window\n    while (window invalid) {\n        // shrink: remove arr[left++] from window\n    }\n    result = Math.max(result, right - left + 1);\n}\n```\n\n### When to use\n"Longest/shortest subarray/substring with property X." Window size can be fixed or variable.',
    time_complexity  = 'O(n)',
    space_complexity = 'O(k)'
WHERE slug = 'sliding-window';

UPDATE patterns SET
    lesson_markdown  = '## Binary Search Pattern\n\nHalve the search space every iteration. Works on **any monotonic function**, not just sorted arrays.\n\n```\nint lo = 0, hi = n - 1;\nwhile (lo <= hi) {\n    int mid = lo + (hi - lo) / 2;\n    if (check(mid)) hi = mid - 1;   // go left\n    else            lo = mid + 1;   // go right\n}\n```\n\n### When to use\nSorted array, or "find minimum X such that condition holds." O(log n) is the giveaway.',
    time_complexity  = 'O(log n)',
    space_complexity = 'O(1)'
WHERE slug = 'binary-search';

UPDATE patterns SET
    lesson_markdown  = '## Stack Pattern\n\nA stack gives you **access to the most recently seen unresolved element**. Classic uses:\n- Matching brackets (push open, pop on close)\n- Monotonic stack: next greater/smaller element\n- Expression evaluation\n\n```\nDeque<Integer> stack = new ArrayDeque<>();\nfor (int x : arr) {\n    while (!stack.isEmpty() && stack.peek() < x)\n        stack.pop(); // resolve pending elements\n    stack.push(x);\n}\n```',
    time_complexity  = 'O(n)',
    space_complexity = 'O(n)'
WHERE slug = 'stack';

UPDATE patterns SET
    lesson_markdown  = '## Linked List Pattern\n\nLinked list problems almost always need one of:\n- **Fast + slow pointer** (cycle detection, find middle)\n- **Reverse in-place** (reverse list, reverse sub-list)\n- **Dummy head** (simplify edge cases on insertions/deletions)\n\n```\n// Reverse a linked list\nListNode prev = null, curr = head;\nwhile (curr != null) {\n    ListNode next = curr.next;\n    curr.next = prev;\n    prev = curr;\n    curr = next;\n}\nreturn prev;\n```',
    time_complexity  = 'O(n)',
    space_complexity = 'O(1)'
WHERE slug = 'linked-list';

UPDATE patterns SET
    lesson_markdown  = '## Trees Pattern\n\nTree problems decompose naturally into **recursion on subtrees**. Ask yourself:\n- What does my function return to its parent?\n- Base case: null node returns what?\n\n```\npublic int solve(TreeNode node) {\n    if (node == null) return BASE;\n    int left  = solve(node.left);\n    int right = solve(node.right);\n    return combine(left, right, node.val);\n}\n```\n\nBFS (queue) for level-order. DFS (recursion or stack) for path/depth problems.',
    time_complexity  = 'O(n)',
    space_complexity = 'O(h)'
WHERE slug = 'trees';

UPDATE patterns SET
    lesson_markdown  = '## Heap (Priority Queue) Pattern\n\nA **min-heap** always gives you the smallest element in O(log n). Use when:\n- "Top K largest/smallest" — keep a size-K min-heap\n- Merge K sorted lists\n- Dijkstra shortest path\n\n```\nPriorityQueue<Integer> minHeap = new PriorityQueue<>();\nfor (int num : nums) {\n    minHeap.offer(num);\n    if (minHeap.size() > k) minHeap.poll(); // evict smallest\n}\n// minHeap now holds the K largest elements\n```',
    time_complexity  = 'O(n log k)',
    space_complexity = 'O(k)'
WHERE slug = 'heap';

UPDATE patterns SET
    lesson_markdown  = '## Graph BFS/DFS Pattern\n\nModel the problem as a graph, then traverse.\n\n**BFS** (queue) → shortest path in unweighted graph, level-by-level processing.\n**DFS** (recursion/stack) → connected components, cycle detection, flood fill.\n\n```\n// BFS template\nQueue<int[]> q = new LinkedList<>();\nboolean[][] visited = new boolean[rows][cols];\nq.offer(start); visited[sr][sc] = true;\nwhile (!q.isEmpty()) {\n    int[] cur = q.poll();\n    for (int[] next : neighbors(cur)) {\n        if (!visited[next[0]][next[1]]) {\n            visited[next[0]][next[1]] = true;\n            q.offer(next);\n        }\n    }\n}\n```',
    time_complexity  = 'O(V + E)',
    space_complexity = 'O(V)'
WHERE slug = 'graph-bfs-dfs';

-- Seed hints for all 10 problems

-- 1. Two Sum
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'Think about what you need to find for each number: if the current number is `x`, you need to find `target - x` somewhere in the array. Can you check if a value exists in O(1) time?'
FROM problems WHERE slug = 'two-sum';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use a HashMap. As you iterate, store each number and its index. For each new number, check if its complement (`target - num`) is already in the map.'
FROM problems WHERE slug = 'two-sum';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'Map<Integer,Integer> map = new HashMap<>();\nfor (int i = 0; i < nums.length; i++) {\n    int complement = target - nums[i];\n    if (map.containsKey(complement))\n        return new int[]{map.get(complement), i};\n    map.put(nums[i], i);\n}'
FROM problems WHERE slug = 'two-sum';

-- 2. Best Time to Buy and Sell Stock
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'You want to maximize `prices[j] - prices[i]` where `j > i`. You only need one pass — track the minimum price seen so far as you go.'
FROM problems WHERE slug = 'best-time-to-buy-sell-stock';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Keep two variables: `minPrice` (lowest price seen so far) and `maxProfit`. At each price, update `minPrice` if lower, then check if `price - minPrice` beats `maxProfit`.'
FROM problems WHERE slug = 'best-time-to-buy-sell-stock';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'int minPrice = Integer.MAX_VALUE, maxProfit = 0;\nfor (int price : prices) {\n    minPrice = Math.min(minPrice, price);\n    maxProfit = Math.max(maxProfit, price - minPrice);\n}\nreturn maxProfit;'
FROM problems WHERE slug = 'best-time-to-buy-sell-stock';

-- 3. Valid Anagram
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'Two strings are anagrams if they contain the same characters with the same frequencies. How can you compare character frequencies efficiently?'
FROM problems WHERE slug = 'valid-anagram';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use an int array of size 26 (one per lowercase letter). Increment counts for `s`, decrement for `t`. If all counts are zero at the end, they are anagrams.'
FROM problems WHERE slug = 'valid-anagram';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'if (s.length() != t.length()) return false;\nint[] count = new int[26];\nfor (char c : s.toCharArray()) count[c - ''a'']++;\nfor (char c : t.toCharArray()) count[c - ''a'']--;\nfor (int n : count) if (n != 0) return false;\nreturn true;'
FROM problems WHERE slug = 'valid-anagram';

-- 4. Longest Substring Without Repeating Characters
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'Think about maintaining a "window" of characters with no repeats. When you add a character that already exists in the window, you need to shrink the window from the left.'
FROM problems WHERE slug = 'longest-substring-without-repeating';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use the sliding window pattern with a HashMap storing the last seen index of each character. When a repeat is found, move `left` to `max(left, lastIndex + 1)` to skip past the duplicate.'
FROM problems WHERE slug = 'longest-substring-without-repeating';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'Map<Character,Integer> map = new HashMap<>();\nint max = 0, left = 0;\nfor (int right = 0; right < s.length(); right++) {\n    char c = s.charAt(right);\n    if (map.containsKey(c))\n        left = Math.max(left, map.get(c) + 1);\n    map.put(c, right);\n    max = Math.max(max, right - left + 1);\n}\nreturn max;'
FROM problems WHERE slug = 'longest-substring-without-repeating';

-- 5. Container With Most Water
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'The area is determined by `min(height[left], height[right]) * (right - left)`. Start with the widest possible container and think about when to move a pointer inward.'
FROM problems WHERE slug = 'container-with-most-water';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use two pointers at both ends. Always move the pointer pointing to the shorter line inward — moving the taller line can only decrease or maintain the area, never increase it.'
FROM problems WHERE slug = 'container-with-most-water';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'int left = 0, right = height.length - 1, max = 0;\nwhile (left < right) {\n    max = Math.max(max, Math.min(height[left], height[right]) * (right - left));\n    if (height[left] < height[right]) left++;\n    else right--;\n}\nreturn max;'
FROM problems WHERE slug = 'container-with-most-water';

-- 6. Binary Search
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'The array is sorted. Instead of checking each element, can you eliminate half the remaining elements with each comparison?'
FROM problems WHERE slug = 'binary-search';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Maintain `lo` and `hi` bounds. Compute `mid = lo + (hi-lo)/2`. If `nums[mid] == target` return mid. If `nums[mid] < target` search right half, else search left half.'
FROM problems WHERE slug = 'binary-search';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'int lo = 0, hi = nums.length - 1;\nwhile (lo <= hi) {\n    int mid = lo + (hi - lo) / 2;\n    if (nums[mid] == target) return mid;\n    if (nums[mid] < target) lo = mid + 1;\n    else hi = mid - 1;\n}\nreturn -1;'
FROM problems WHERE slug = 'binary-search';

-- 7. Valid Parentheses
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'When you see a closing bracket, it must match the most recently opened (unmatched) bracket. Which data structure gives you the most recent item?'
FROM problems WHERE slug = 'valid-parentheses';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use a stack. Push every opening bracket. On a closing bracket, check if the stack top is the matching opener — if not (or stack is empty), return false. At the end, the stack must be empty.'
FROM problems WHERE slug = 'valid-parentheses';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'Deque<Character> stack = new ArrayDeque<>();\nfor (char c : s.toCharArray()) {\n    if (c == ''('' || c == ''['' || c == ''{'') stack.push(c);\n    else {\n        if (stack.isEmpty()) return false;\n        char top = stack.pop();\n        if (c == '')'' && top != ''('') return false;\n        if (c == '']'' && top != ''['') return false;\n        if (c == ''}'' && top != ''{'') return false;\n    }\n}\nreturn stack.isEmpty();'
FROM problems WHERE slug = 'valid-parentheses';

-- 8. Reverse Linked List
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'You need to reverse every `next` pointer. As you traverse, each node''s `next` should point back to its predecessor. How do you keep track of what comes next while modifying the pointer?'
FROM problems WHERE slug = 'reverse-linked-list';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use three pointers: `prev = null`, `curr = head`. At each step: save `curr.next`, set `curr.next = prev`, advance `prev = curr`, advance `curr = saved next`.'
FROM problems WHERE slug = 'reverse-linked-list';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'ListNode prev = null, curr = head;\nwhile (curr != null) {\n    ListNode next = curr.next;\n    curr.next = prev;\n    prev = curr;\n    curr = next;\n}\nreturn prev;'
FROM problems WHERE slug = 'reverse-linked-list';

-- 9. Maximum Depth of Binary Tree
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'Think recursively: the depth of a tree is 1 + the maximum depth of its subtrees. What is the depth of a null node?'
FROM problems WHERE slug = 'maximum-depth-binary-tree';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Base case: `null` node has depth 0. Recursive case: `1 + max(depth(left), depth(right))`. This is a classic post-order DFS where you combine results from both children.'
FROM problems WHERE slug = 'maximum-depth-binary-tree';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'public int maxDepth(TreeNode root) {\n    if (root == null) return 0;\n    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));\n}'
FROM problems WHERE slug = 'maximum-depth-binary-tree';

-- 10. Number of Islands
INSERT INTO hints (problem_id, level, content)
SELECT id, 1, 'Count the number of connected components of ''1''s. When you find an unvisited ''1'', that''s a new island. How do you mark all the land cells of that island as visited?'
FROM problems WHERE slug = 'number-of-islands';
INSERT INTO hints (problem_id, level, content)
SELECT id, 2, 'Use DFS or BFS. When you find a ''1'', increment your counter then flood-fill: recursively mark all connected ''1'' neighbors as ''0'' (or visited) so they are not counted again.'
FROM problems WHERE slug = 'number-of-islands';
INSERT INTO hints (problem_id, level, content)
SELECT id, 3, 'int count = 0;\nfor (int i = 0; i < grid.length; i++)\n    for (int j = 0; j < grid[0].length; j++)\n        if (grid[i][j] == ''1'') { dfs(grid, i, j); count++; }\nreturn count;\n\nvoid dfs(char[][] g, int i, int j) {\n    if (i<0||i>=g.length||j<0||j>=g[0].length||g[i][j]!=''1'') return;\n    g[i][j] = ''0'';\n    dfs(g,i+1,j); dfs(g,i-1,j); dfs(g,i,j+1); dfs(g,i,j-1);\n}'
FROM problems WHERE slug = 'number-of-islands';
