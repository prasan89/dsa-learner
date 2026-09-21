-- ─── 10 Patterns ─────────────────────────────────────────────────────────────
INSERT INTO patterns (id, slug, name, summary, recognition_clues, template_code, display_order) VALUES

('00000000-0000-0000-0000-000000000001', 'arrays',
 'Arrays',
 'Direct index access and in-place manipulation of contiguous memory.',
 'iterate elements
find element
rotate/reverse
find duplicate
matrix traversal',
'// Arrays pattern template
public int[] solve(int[] nums) {
    int n = nums.length;
    // iterate
    for (int i = 0; i < n; i++) {
        // process nums[i]
    }
    return nums;
}', 1),

('00000000-0000-0000-0000-000000000002', 'hashing',
 'Hashing',
 'Use a HashMap or HashSet for O(1) lookups to avoid nested loops.',
 'two sum / pair with target
frequency count
detect duplicate
group anagrams
first unique',
'// Hashing pattern template
public int[] solve(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>(); // val -> index
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) return new int[]{map.get(complement), i};
        map.put(nums[i], i);
    }
    return new int[]{};
}', 2),

('00000000-0000-0000-0000-000000000003', 'two-pointers',
 'Two Pointers',
 'Use left and right pointers that converge or move at different speeds to avoid O(n²) loops.',
 'sorted array
pair/triplet with target sum
remove duplicates in-place
palindrome check
container with most water',
'// Two Pointers template
public int solve(int[] nums) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        // process nums[left] and nums[right]
        if (/* condition */) left++;
        else right--;
    }
    return 0;
}', 3),

('00000000-0000-0000-0000-000000000004', 'sliding-window',
 'Sliding Window',
 'Maintain a window over a contiguous subarray or substring, expanding and shrinking based on constraints.',
 'contiguous subarray / substring
longest / shortest with condition
maximum / minimum sum
k-size window
distinct characters',
'// Sliding Window — Variable size
public int solve(String s) {
    Map<Character, Integer> window = new HashMap<>();
    int left = 0, result = 0;
    for (int right = 0; right < s.length(); right++) {
        window.merge(s.charAt(right), 1, Integer::sum);
        while (/* invalid window */) {
            window.merge(s.charAt(left), -1, Integer::sum);
            if (window.get(s.charAt(left)) == 0) window.remove(s.charAt(left));
            left++;
        }
        result = Math.max(result, right - left + 1);
    }
    return result;
}', 4),

('00000000-0000-0000-0000-000000000005', 'binary-search',
 'Binary Search',
 'Halve the search space each iteration to achieve O(log n) on sorted or monotonic inputs.',
 'sorted array
find target / boundary
minimum/maximum satisfying condition
rotated sorted array
search in answer space',
'// Binary Search template
public int solve(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) return mid;
        else if (nums[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return -1;
}', 5),

('00000000-0000-0000-0000-000000000006', 'stack',
 'Stack',
 'LIFO structure for problems involving matching, monotonic order, or previous/next greater element.',
 'matching brackets / parentheses
next greater / smaller element
daily temperatures
valid expression evaluation
undo operations',
'// Stack template
public int[] solve(int[] nums) {
    Deque<Integer> stack = new ArrayDeque<>(); // stores indices
    int[] result = new int[nums.length];
    for (int i = 0; i < nums.length; i++) {
        while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
            result[stack.pop()] = nums[i];
        }
        stack.push(i);
    }
    return result;
}', 6),

('00000000-0000-0000-0000-000000000007', 'linked-list',
 'Linked List',
 'Pointer manipulation — reverse, find cycle, merge, find middle.',
 'reverse a list
detect cycle
find middle
merge two lists
remove nth from end',
'// Linked List — Reverse template
public ListNode reverse(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}', 7),

('00000000-0000-0000-0000-000000000008', 'trees',
 'Trees',
 'DFS (inorder/preorder/postorder) and BFS traversals on binary trees.',
 'tree traversal
lowest common ancestor
max depth / diameter
path sum
serialize/deserialize',
'// Tree DFS template
public int solve(TreeNode root) {
    if (root == null) return 0;
    int left = solve(root.left);
    int right = solve(root.right);
    // combine results
    return Math.max(left, right) + 1;
}', 8),

('00000000-0000-0000-0000-000000000009', 'heap',
 'Heap / Priority Queue',
 'Use a min/max heap to maintain the k-th largest/smallest element efficiently.',
 'kth largest / smallest
top k frequent
median of stream
merge k sorted lists
task scheduling',
'// Heap template — Top K
public int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    PriorityQueue<Integer> minHeap = new PriorityQueue<>(Comparator.comparingInt(freq::get));
    for (int num : freq.keySet()) {
        minHeap.offer(num);
        if (minHeap.size() > k) minHeap.poll();
    }
    return minHeap.stream().mapToInt(i -> i).toArray();
}', 9),

('00000000-0000-0000-0000-000000000010', 'graph-bfs-dfs',
 'Graph BFS / DFS',
 'Traverse or search a graph using breadth-first (level-by-level) or depth-first (recursive/stack) approaches.',
 'shortest path (unweighted)
connected components
cycle detection
topological sort
island counting',
'// Graph BFS template
public int bfs(int start, Map<Integer, List<Integer>> graph) {
    Queue<Integer> queue = new LinkedList<>();
    Set<Integer> visited = new HashSet<>();
    queue.offer(start);
    visited.add(start);
    int steps = 0;
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int node = queue.poll();
            for (int neighbor : graph.getOrDefault(node, List.of())) {
                if (visited.add(neighbor)) queue.offer(neighbor);
            }
        }
        steps++;
    }
    return steps;
}', 10);

-- ─── 10 Seed Problems ─────────────────────────────────────────────────────────
INSERT INTO problems (id, slug, title, difficulty, description, constraints, examples, active) VALUES

('10000000-0000-0000-0000-000000000001',
 'two-sum', 'Two Sum', 'EASY',
 'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.',
 '2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9',
 '[{"input":"nums = [2,7,11,15], target = 9","output":"[0,1]","explanation":"nums[0] + nums[1] = 2 + 7 = 9"},{"input":"nums = [3,2,4], target = 6","output":"[1,2]"}]',
 true),

('10000000-0000-0000-0000-000000000002',
 'best-time-to-buy-sell-stock', 'Best Time to Buy and Sell Stock', 'EASY',
 'You are given an array `prices` where `prices[i]` is the price of a given stock on the `i`-th day.\n\nYou want to maximize your profit by choosing a single day to buy one stock and choosing a different day in the future to sell that stock.\n\nReturn the maximum profit you can achieve from this transaction. If you cannot achieve any profit, return `0`.',
 '1 <= prices.length <= 10^5\n0 <= prices[i] <= 10^4',
 '[{"input":"prices = [7,1,5,3,6,4]","output":"5","explanation":"Buy on day 2 (price=1) and sell on day 5 (price=6), profit = 6-1 = 5."},{"input":"prices = [7,6,4,3,1]","output":"0"}]',
 true),

('10000000-0000-0000-0000-000000000003',
 'valid-anagram', 'Valid Anagram', 'EASY',
 'Given two strings `s` and `t`, return `true` if `t` is an anagram of `s`, and `false` otherwise.\n\nAn anagram is a word or phrase formed by rearranging the letters of a different word or phrase, using all the original letters exactly once.',
 '1 <= s.length, t.length <= 5 * 10^4\ns and t consist of lowercase English letters',
 '[{"input":"s = \"anagram\", t = \"nagaram\"","output":"true"},{"input":"s = \"rat\", t = \"car\"","output":"false"}]',
 true),

('10000000-0000-0000-0000-000000000004',
 'longest-substring-without-repeating', 'Longest Substring Without Repeating Characters', 'MEDIUM',
 'Given a string `s`, find the length of the longest substring without repeating characters.',
 '0 <= s.length <= 5 * 10^4\ns consists of English letters, digits, symbols and spaces',
 '[{"input":"s = \"abcabcbb\"","output":"3","explanation":"The answer is \"abc\", with the length of 3."},{"input":"s = \"bbbbb\"","output":"1"},{"input":"s = \"pwwkew\"","output":"3"}]',
 true),

('10000000-0000-0000-0000-000000000005',
 'container-with-most-water', 'Container With Most Water', 'MEDIUM',
 'You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two endpoints of the `i`-th line are `(i, 0)` and `(i, height[i])`.\n\nFind two lines that together with the x-axis form a container, such that the container contains the most water.\n\nReturn the maximum amount of water a container can store.',
 'n == height.length\n2 <= n <= 10^5\n0 <= height[i] <= 10^4',
 '[{"input":"height = [1,8,6,2,5,4,8,3,7]","output":"49"},{"input":"height = [1,1]","output":"1"}]',
 true),

('10000000-0000-0000-0000-000000000006',
 'binary-search-problem', 'Binary Search', 'EASY',
 'Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then return its index. Otherwise, return `-1`.\n\nYou must write an algorithm with `O(log n)` runtime complexity.',
 '1 <= nums.length <= 10^4\n-10^4 < nums[i], target < 10^4\nAll the integers in nums are unique\nnums is sorted in ascending order',
 '[{"input":"nums = [-1,0,3,5,9,12], target = 9","output":"4"},{"input":"nums = [-1,0,3,5,9,12], target = 2","output":"-1"}]',
 true),

('10000000-0000-0000-0000-000000000007',
 'valid-parentheses', 'Valid Parentheses', 'EASY',
 'Given a string `s` containing just the characters `(`, `)`, `{`, `}`, `[` and `]`, determine if the input string is valid.\n\nAn input string is valid if:\n1. Open brackets must be closed by the same type of brackets.\n2. Open brackets must be closed in the correct order.\n3. Every close bracket has a corresponding open bracket of the same type.',
 '1 <= s.length <= 10^4\ns consists of parentheses only',
 '[{"input":"s = \"()\"","output":"true"},{"input":"s = \"()[]{}\"","output":"true"},{"input":"s = \"(]\"","output":"false"}]',
 true),

('10000000-0000-0000-0000-000000000008',
 'reverse-linked-list', 'Reverse Linked List', 'EASY',
 'Given the `head` of a singly linked list, reverse the list, and return the reversed list.',
 'The number of nodes in the list is the range [0, 5000]\n-5000 <= Node.val <= 5000',
 '[{"input":"head = [1,2,3,4,5]","output":"[5,4,3,2,1]"},{"input":"head = [1,2]","output":"[2,1]"},{"input":"head = []","output":"[]"}]',
 true),

('10000000-0000-0000-0000-000000000009',
 'maximum-depth-binary-tree', 'Maximum Depth of Binary Tree', 'EASY',
 'Given the `root` of a binary tree, return its maximum depth.\n\nA binary tree''s maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.',
 'The number of nodes in the tree is in the range [0, 10^4]\n-100 <= Node.val <= 100',
 '[{"input":"root = [3,9,20,null,null,15,7]","output":"3"},{"input":"root = [1,null,2]","output":"2"}]',
 true),

('10000000-0000-0000-0000-000000000010',
 'number-of-islands', 'Number of Islands', 'MEDIUM',
 'Given an `m x n` 2D binary grid `grid` which represents a map of `''1''`s (land) and `''0''`s (water), return the number of islands.\n\nAn island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically. You may assume all four edges of the grid are all surrounded by water.',
 'm == grid.length\nn == grid[i].length\n1 <= m, n <= 300\ngrid[i][j] is ''0'' or ''1''',
 '[{"input":"grid = [[\"1\",\"1\",\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"0\",\"0\"]]","output":"1"},{"input":"grid = [[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"1\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"1\",\"1\"]]","output":"3"}]',
 true);

-- ─── Problem Tags ─────────────────────────────────────────────────────────────
INSERT INTO problem_tags (problem_id, tag) VALUES
('10000000-0000-0000-0000-000000000001', 'array'),
('10000000-0000-0000-0000-000000000001', 'hash-table'),
('10000000-0000-0000-0000-000000000002', 'array'),
('10000000-0000-0000-0000-000000000002', 'dynamic-programming'),
('10000000-0000-0000-0000-000000000003', 'hash-table'),
('10000000-0000-0000-0000-000000000003', 'string'),
('10000000-0000-0000-0000-000000000004', 'hash-table'),
('10000000-0000-0000-0000-000000000004', 'string'),
('10000000-0000-0000-0000-000000000005', 'array'),
('10000000-0000-0000-0000-000000000005', 'greedy'),
('10000000-0000-0000-0000-000000000006', 'array'),
('10000000-0000-0000-0000-000000000006', 'binary-search'),
('10000000-0000-0000-0000-000000000007', 'string'),
('10000000-0000-0000-0000-000000000007', 'stack'),
('10000000-0000-0000-0000-000000000008', 'linked-list'),
('10000000-0000-0000-0000-000000000009', 'tree'),
('10000000-0000-0000-0000-000000000009', 'depth-first-search'),
('10000000-0000-0000-0000-000000000010', 'matrix'),
('10000000-0000-0000-0000-000000000010', 'depth-first-search'),
('10000000-0000-0000-0000-000000000010', 'breadth-first-search');

-- ─── Problem <-> Pattern mappings ─────────────────────────────────────────────
INSERT INTO problem_patterns (problem_id, pattern_id) VALUES
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002'), -- two-sum -> hashing
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001'), -- stock -> arrays
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002'), -- anagram -> hashing
('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000004'), -- longest substr -> sliding window
('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000003'), -- container water -> two pointers
('10000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000005'), -- binary search -> binary search
('10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000006'), -- valid parens -> stack
('10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000007'), -- reverse list -> linked list
('10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000008'), -- max depth -> trees
('10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000010'); -- islands -> graph bfs/dfs

-- ─── Test Cases ───────────────────────────────────────────────────────────────
INSERT INTO test_cases (id, problem_id, input, expected_output, is_hidden, display_order) VALUES
-- Two Sum
(gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '4\n2 7 11 15\n9',   '[0,1]', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '3\n3 2 4\n6',        '[1,2]', false, 2),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '2\n3 3\n6',          '[0,1]', true,  3),

-- Best Time Stock
(gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '6\n7 1 5 3 6 4', '5', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '5\n7 6 4 3 1',    '0', false, 2),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '1\n5',            '0', true,  3),

-- Valid Anagram
(gen_random_uuid(), '10000000-0000-0000-0000-000000000003', 'anagram\nnagaram', 'true',  false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000003', 'rat\ncar',         'false', false, 2),

-- Longest Substring
(gen_random_uuid(), '10000000-0000-0000-0000-000000000004', 'abcabcbb', '3', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000004', 'bbbbb',    '1', false, 2),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000004', 'pwwkew',   '3', true,  3),

-- Container With Most Water
(gen_random_uuid(), '10000000-0000-0000-0000-000000000005', '9\n1 8 6 2 5 4 8 3 7', '49', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000005', '2\n1 1',                '1',  false, 2),

-- Binary Search
(gen_random_uuid(), '10000000-0000-0000-0000-000000000006', '6\n-1 0 3 5 9 12\n9',  '4',  false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000006', '6\n-1 0 3 5 9 12\n2',  '-1', false, 2),

-- Valid Parentheses
(gen_random_uuid(), '10000000-0000-0000-0000-000000000007', '()',     'true',  false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000007', '()[]{}', 'true',  false, 2),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000007', '(]',     'false', false, 3),

-- Reverse Linked List
(gen_random_uuid(), '10000000-0000-0000-0000-000000000008', '5\n1 2 3 4 5', '5 4 3 2 1', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000008', '2\n1 2',       '2 1',       false, 2),

-- Maximum Depth Binary Tree
(gen_random_uuid(), '10000000-0000-0000-0000-000000000009', '3 9 20 null null 15 7', '3', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000009', '1 null 2',              '2', false, 2),

-- Number of Islands
(gen_random_uuid(), '10000000-0000-0000-0000-000000000010', '4 5\n1 1 1 1 0\n1 1 0 1 0\n1 1 0 0 0\n0 0 0 0 0', '1', false, 1),
(gen_random_uuid(), '10000000-0000-0000-0000-000000000010', '4 5\n1 1 0 0 0\n1 1 0 0 0\n0 0 1 0 0\n0 0 0 1 1', '3', false, 2);
