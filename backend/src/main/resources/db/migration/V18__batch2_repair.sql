-- V18: Repair batch 2 data for databases where V17 was already applied.
-- V17 was corrected to include Java solutions and the canonical rotated-search slug.
-- This migration backfills Java solutions and ensures rotated-search test cases exist.

UPDATE problem_content pc
SET java_solution = CASE p.slug
    WHEN 'maximum-subarray' THEN $$
public int maxSubArray(int[] nums) {
    int current = nums[0], best = nums[0];
    for (int i = 1; i < nums.length; i++) {
        current = Math.max(nums[i], current + nums[i]);
        best = Math.max(best, current);
    }
    return best;
}$$
    WHEN 'product-of-array-except-self' THEN $$
public int[] productExceptSelf(int[] nums) {
    int[] result = new int[nums.length];
    int prefix = 1;
    for (int i = 0; i < nums.length; i++) {
        result[i] = prefix;
        prefix *= nums[i];
    }
    int suffix = 1;
    for (int i = nums.length - 1; i >= 0; i--) {
        result[i] *= suffix;
        suffix *= nums[i];
    }
    return result;
}$$
    WHEN 'group-anagrams' THEN $$
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();
    for (String word : strs) {
        int[] count = new int[26];
        for (char c : word.toCharArray()) count[c - 'a']++;
        StringBuilder key = new StringBuilder();
        for (int value : count) key.append('#').append(value);
        groups.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(word);
    }
    return new ArrayList<>(groups.values());
}$$
    WHEN '3sum' THEN $$
public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            long sum = (long) nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(List.of(nums[i], nums[left], nums[right]));
                int lv = nums[left], rv = nums[right];
                while (left < right && nums[left] == lv) left++;
                while (left < right && nums[right] == rv) right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}$$
    WHEN 'search-rotated-array' THEN $$
public int search(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) return mid;
        if (nums[left] <= nums[mid]) {
            if (nums[left] <= target && target < nums[mid]) right = mid - 1;
            else left = mid + 1;
        } else {
            if (nums[mid] < target && target <= nums[right]) left = mid + 1;
            else right = mid - 1;
        }
    }
    return -1;
}$$
    WHEN 'daily-temperatures' THEN $$
public int[] dailyTemperatures(int[] temperatures) {
    int[] answer = new int[temperatures.length];
    Deque<Integer> stack = new ArrayDeque<>();
    for (int i = 0; i < temperatures.length; i++) {
        while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
            int previous = stack.pop();
            answer[previous] = i - previous;
        }
        stack.push(i);
    }
    return answer;
}$$
    WHEN 'merge-two-sorted-lists' THEN $$
public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    ListNode dummy = new ListNode(0), tail = dummy;
    while (list1 != null && list2 != null) {
        if (list1.val <= list2.val) {
            tail.next = list1;
            list1 = list1.next;
        } else {
            tail.next = list2;
            list2 = list2.next;
        }
        tail = tail.next;
    }
    tail.next = list1 != null ? list1 : list2;
    return dummy.next;
}$$
    WHEN 'binary-tree-level-order' THEN $$
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        List<Integer> level = new ArrayList<>(levelSize);
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}$$
    WHEN 'kth-largest-element' THEN $$
public int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    for (int num : nums) {
        minHeap.offer(num);
        if (minHeap.size() > k) minHeap.poll();
    }
    return minHeap.peek();
}$$
    WHEN 'number-of-islands' THEN $$
public int numIslands(char[][] grid) {
    int rows = grid.length, cols = grid[0].length, islands = 0;
    int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            if (grid[r][c] != '1') continue;
            islands++;
            Deque<int[]> queue = new ArrayDeque<>();
            queue.offer(new int[]{r,c});
            grid[r][c] = '0';
            while (!queue.isEmpty()) {
                int[] cell = queue.poll();
                for (int[] d : directions) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == '1') {
                        grid[nr][nc] = '0';
                        queue.offer(new int[]{nr,nc});
                    }
                }
            }
        }
    }
    return islands;
}$$
END
WHERE p.id = pc.problem_id
  AND p.slug IN (
      'maximum-subarray',
      'product-of-array-except-self',
      'group-anagrams',
      '3sum',
      'search-rotated-array',
      'daily-temperatures',
      'merge-two-sorted-lists',
      'binary-tree-level-order',
      'kth-largest-element',
      'number-of-islands'
  );

INSERT INTO test_cases(id, problem_id, input, expected_output, is_hidden, display_order)
SELECT gen_random_uuid(), p.id, v.input, v.expected_output, v.is_hidden, v.display_order
FROM problems p
CROSS JOIN (
    VALUES
        ('7
4 5 6 7 0 1 2
0', '4', false, 1),
        ('7
4 5 6 7 0 1 2
3', '-1', false, 2),
        ('5
1 3 5 7 9
7', '3', true, 3),
        ('6
6 7 0 1 2 4
4', '5', true, 4)
) AS v(input, expected_output, is_hidden, display_order)
WHERE p.slug = 'search-rotated-array'
  AND NOT EXISTS (
      SELECT 1
      FROM test_cases tc
      WHERE tc.problem_id = p.id
        AND tc.input = v.input
  );

DO $$
DECLARE missing_java integer;
BEGIN
    SELECT COUNT(*) INTO missing_java
    FROM problems p
    JOIN problem_content pc ON pc.problem_id = p.id
    WHERE p.slug IN (
        'maximum-subarray',
        'product-of-array-except-self',
        'group-anagrams',
        '3sum',
        'search-rotated-array',
        'daily-temperatures',
        'merge-two-sorted-lists',
        'binary-tree-level-order',
        'kth-largest-element',
        'number-of-islands'
    )
    AND (pc.java_solution IS NULL OR pc.java_solution = '');

    IF missing_java > 0 THEN
        RAISE EXCEPTION 'V18 batch 2 repair: % Java solutions still missing', missing_java;
    END IF;
END $$;
