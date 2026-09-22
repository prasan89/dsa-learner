-- V18: Batch 2 completeness fix — add Java 21 solutions to all ten V17 problems.
-- This is intentionally separate from V17 so the already-applied migration is immutable.

UPDATE problem_content SET java_solution=$$public int maxSubArray(int[] nums) {
    int current = nums[0];
    int best = nums[0];
    for (int i = 1; i < nums.length; i++) {
        current = Math.max(nums[i], current + nums[i]);
        best = Math.max(best, current);
    }
    return best;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='maximum-subarray');

UPDATE problem_content SET java_solution=$$public int[] productExceptSelf(int[] nums) {
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
WHERE problem_id=(SELECT id FROM problems WHERE slug='product-of-array-except-self');

UPDATE problem_content SET java_solution=$$public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();
    for (String word : strs) {
        int[] count = new int[26];
        for (char c : word.toCharArray()) {
            count[c - 'a']++;
        }
        StringBuilder key = new StringBuilder();
        for (int n : count) {
            key.append('#').append(n);
        }
        groups.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(word);
    }
    return new ArrayList<>(groups.values());
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='group-anagrams');

UPDATE problem_content SET java_solution=$$public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();

    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;

        int left = i + 1;
        int right = nums.length - 1;

        while (left < right) {
            long sum = (long) nums[i] + nums[left] + nums[right];

            if (sum == 0) {
                result.add(List.of(nums[i], nums[left], nums[right]));

                int leftValue = nums[left];
                int rightValue = nums[right];
                while (left < right && nums[left] == leftValue) left++;
                while (left < right && nums[right] == rightValue) right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='3sum');

UPDATE problem_content SET java_solution=$$public int search(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] == target) return mid;

        if (nums[left] <= nums[mid]) {
            if (nums[left] <= target && target < nums[mid]) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        } else {
            if (nums[mid] < target && target <= nums[right]) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
    }
    return -1;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='search-rotated-array');

UPDATE problem_content SET java_solution=$$public int[] dailyTemperatures(int[] temperatures) {
    int[] answer = new int[temperatures.length];
    Deque<Integer> stack = new ArrayDeque<>();

    for (int i = 0; i < temperatures.length; i++) {
        while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
            int j = stack.pop();
            answer[j] = i - j;
        }
        stack.push(i);
    }
    return answer;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='daily-temperatures');

UPDATE problem_content SET java_solution=$$public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;

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
WHERE problem_id=(SELECT id FROM problems WHERE slug='merge-two-sorted-lists');

UPDATE problem_content SET java_solution=$$public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.add(root);

    while (!queue.isEmpty()) {
        int size = queue.size();
        List<Integer> level = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            TreeNode node = queue.removeFirst();
            level.add(node.val);

            if (node.left != null) queue.addLast(node.left);
            if (node.right != null) queue.addLast(node.right);
        }

        result.add(level);
    }

    return result;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='binary-tree-level-order');

UPDATE problem_content SET java_solution=$$public int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();

    for (int n : nums) {
        minHeap.offer(n);
        if (minHeap.size() > k) {
            minHeap.poll();
        }
    }

    return minHeap.peek();
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='kth-largest-element');

UPDATE problem_content SET java_solution=$$public int numIslands(char[][] grid) {
    if (grid == null || grid.length == 0) return 0;

    int rows = grid.length;
    int cols = grid[0].length;
    int count = 0;
    int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
    Deque<int[]> queue = new ArrayDeque<>();

    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            if (grid[r][c] != '1') continue;

            count++;
            grid[r][c] = '0';
            queue.addLast(new int[]{r, c});

            while (!queue.isEmpty()) {
                int[] cell = queue.removeFirst();

                for (int[] d : directions) {
                    int nr = cell[0] + d[0];
                    int nc = cell[1] + d[1];

                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == '1') {
                        grid[nr][nc] = '0';
                        queue.addLast(new int[]{nr, nc});
                    }
                }
            }
        }
    }

    return count;
}$$
WHERE problem_id=(SELECT id FROM problems WHERE slug='number-of-islands');

DO $$
DECLARE missing_count integer;
BEGIN
  SELECT COUNT(*) INTO missing_count
  FROM problems p
  JOIN problem_content pc ON pc.problem_id=p.id
  WHERE p.slug IN (
    'maximum-subarray','product-of-array-except-self','group-anagrams','3sum',
    'search-rotated-array','daily-temperatures','merge-two-sorted-lists',
    'binary-tree-level-order','kth-largest-element','number-of-islands'
  )
  AND (pc.java_solution IS NULL OR btrim(pc.java_solution) = '');

  IF missing_count > 0 THEN
    RAISE EXCEPTION 'V18 Java solution missing for % batch-2 problems', missing_count;
  END IF;
END $$;
