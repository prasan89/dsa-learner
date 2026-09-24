-- V28a: Real descriptions for arrays and hashing problems

-- ARRAYS

UPDATE problems SET
  description = $$Given an integer array `nums`, find a contiguous subarray that has the largest product, and return the product. The subarray must contain at least one element. Note that the product can be negative if the subarray contains an odd number of negative numbers.$$,
  examples    = $$[{"input":"nums = [2,3,-2,4]","output":"6","explanation":"The subarray [2,3] has the largest product 6."},{"input":"nums = [-2,0,-1]","output":"0","explanation":"The result cannot be 2 because [-2,-1] is not a contiguous subarray between them; the maximum product is 0."},{"input":"nums = [-2,3,-4]","output":"24","explanation":"The entire array [-2,3,-4] gives product 24."}]$$,
  constraints = $$1 <= nums.length <= 2 * 10^4
-10 <= nums[i] <= 10
The product of any subarray of nums is guaranteed to fit in a 32-bit integer.$$
WHERE slug = 'maximum-product-subarray';

UPDATE problems SET
  description = $$Given an integer array `nums`, return an array `answer` such that `answer[i]` is equal to the product of all elements of `nums` except `nums[i]`. You must solve it in O(n) time without using the division operation.$$,
  examples    = $$[{"input":"nums = [1,2,3,4]","output":"[24,12,8,6]","explanation":"answer[0]=2*3*4=24, answer[1]=1*3*4=12, answer[2]=1*2*4=8, answer[3]=1*2*3=6."},{"input":"nums = [-1,1,0,-3,3]","output":"[0,0,9,0,0]","explanation":"Any position with a zero forces the product to 0 unless there are two zeros."}]$$,
  constraints = $$2 <= nums.length <= 10^5
-30 <= nums[i] <= 30
The product of any prefix or suffix of nums is guaranteed to fit in a 32-bit integer.$$
WHERE slug = 'product-of-array-except-self';

UPDATE problems SET
  description = $$Given an integer array `nums`, find the contiguous subarray (containing at least one number) which has the largest sum and return its sum. This is the classic Kadane's algorithm problem where you track the maximum subarray ending at each position.$$,
  examples    = $$[{"input":"nums = [-2,1,-3,4,-1,2,1,-5,4]","output":"6","explanation":"The subarray [4,-1,2,1] has the largest sum 6."},{"input":"nums = [1]","output":"1","explanation":"Single element, return it."},{"input":"nums = [5,4,-1,7,8]","output":"23","explanation":"The entire array is the maximum subarray with sum 23."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^4 <= nums[i] <= 10^4$$
WHERE slug = 'maximum-subarray';

UPDATE problems SET
  description = $$Given an integer array `nums`, rotate the array to the right by `k` steps, where `k` is non-negative. Rotating right by one step means the last element moves to the front. Return the rotated array in-place.$$,
  examples    = $$[{"input":"nums = [1,2,3,4,5,6,7], k = 3","output":"[5,6,7,1,2,3,4]","explanation":"Rotate right by 1: [7,1,2,3,4,5,6]. Rotate right by 2: [6,7,1,2,3,4,5]. Rotate right by 3: [5,6,7,1,2,3,4]."},{"input":"nums = [-1,-100,3,99], k = 2","output":"[3,99,-1,-100]","explanation":"Rotate right by 1: [99,-1,-100,3]. Rotate right by 2: [3,99,-1,-100]."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-2^31 <= nums[i] <= 2^31 - 1
0 <= k <= 10^5$$
WHERE slug = 'rotate-array';

UPDATE problems SET
  description = $$Given an integer array `nums`, move all zeroes to the end while maintaining the relative order of the non-zero elements. You must do this in-place without making a copy of the array.$$,
  examples    = $$[{"input":"nums = [0,1,0,3,12]","output":"[1,3,12,0,0]","explanation":"Non-zero elements [1,3,12] maintain their relative order and zeroes are moved to the end."},{"input":"nums = [0]","output":"[0]","explanation":"Single zero, nothing changes."}]$$,
  constraints = $$1 <= nums.length <= 10^4
-2^31 <= nums[i] <= 2^31 - 1$$
WHERE slug = 'move-zeroes';

UPDATE problems SET
  description = $$Given an integer array `nums`, return `true` if any value appears at least twice in the array, and return `false` if every element is distinct.$$,
  examples    = $$[{"input":"nums = [1,2,3,1]","output":"true","explanation":"1 appears at index 0 and index 3."},{"input":"nums = [1,2,3,4]","output":"false","explanation":"All elements are distinct."},{"input":"nums = [1,1,1,3,3,4,3,2,4,2]","output":"true","explanation":"Multiple duplicates exist."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^9 <= nums[i] <= 10^9$$
WHERE slug = 'contains-duplicate';

UPDATE problems SET
  description = $$Given an array `nums` containing `n` distinct numbers in the range `[0, n]`, return the only number in the range that is missing from the array. Your solution should run in O(n) time and use O(1) extra space.$$,
  examples    = $$[{"input":"nums = [3,0,1]","output":"2","explanation":"n = 3 since there are 3 numbers. The range [0,3] should contain 0,1,2,3 but 2 is missing."},{"input":"nums = [0,1]","output":"2","explanation":"n = 2, range [0,2], 2 is missing."},{"input":"nums = [9,6,4,2,3,5,7,0,1]","output":"8","explanation":"8 is the missing number in range [0,9]."}]$$,
  constraints = $$n == nums.length
1 <= n <= 10^4
0 <= nums[i] <= n
All the numbers of nums are unique.$$
WHERE slug = 'missing-number';

UPDATE problems SET
  description = $$Given an array `nums` of `n` integers where `nums[i]` is in the range `[1, n]`, return an array of all the integers in the range `[1, n]` that do not appear in `nums`. Some integers may appear twice and others may not appear at all.$$,
  examples    = $$[{"input":"nums = [4,3,2,7,8,2,3,1]","output":"[5,6]","explanation":"n=8, so range is [1,8]. Numbers 5 and 6 are absent from nums."},{"input":"nums = [1,1]","output":"[2]","explanation":"n=2, range is [1,2]. Number 2 is missing."}]$$,
  constraints = $$n == nums.length
1 <= n <= 10^5
1 <= nums[i] <= n$$
WHERE slug = 'find-all-numbers-disappeared';

UPDATE problems SET
  description = $$Given an array `nums` of size `n`, return the majority element — the element that appears more than `n / 2` times. You may assume the majority element always exists in the array. Try to solve in O(n) time and O(1) space using the Boyer-Moore Voting Algorithm.$$,
  examples    = $$[{"input":"nums = [3,2,3]","output":"3","explanation":"3 appears 2 times out of 3, which is more than 3/2 = 1.5."},{"input":"nums = [2,2,1,1,1,2,2]","output":"2","explanation":"2 appears 4 times out of 7, which is more than 7/2 = 3.5."}]$$,
  constraints = $$n == nums.length
1 <= n <= 5 * 10^4
-10^9 <= nums[i] <= 10^9
The majority element always exists.$$
WHERE slug = 'majority-element';

UPDATE problems SET
  description = $$You are given two integer arrays `nums1` and `nums2`, sorted in non-decreasing order, and two integers `m` and `n`, representing the number of elements in each array. Merge `nums1` and `nums2` into a single array sorted in non-decreasing order. The final sorted array should be stored inside `nums1` in-place, which has length `m + n` with the last `n` slots set to 0.$$,
  examples    = $$[{"input":"nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3","output":"[1,2,2,3,5,6]","explanation":"Merging [1,2,3] and [2,5,6] gives [1,2,2,3,5,6]."},{"input":"nums1 = [1], m = 1, nums2 = [], n = 0","output":"[1]","explanation":"nums2 is empty, nums1 stays [1]."},{"input":"nums1 = [0], m = 0, nums2 = [1], n = 1","output":"[1]","explanation":"nums1 is empty (the 0 is a placeholder), result is [1]."}]$$,
  constraints = $$nums1.length == m + n
nums2.length == n
0 <= m, n <= 200
1 <= m + n <= 200
-10^9 <= nums1[i], nums2[j] <= 10^9$$
WHERE slug = 'merge-sorted-array';

UPDATE problems SET
  description = $$Given two integer arrays `nums1` and `nums2`, return an array of their intersection. Each element in the result must be unique and you may return the result in any order.$$,
  examples    = $$[{"input":"nums1 = [1,2,2,1], nums2 = [2,2]","output":"[2]","explanation":"2 is the only element present in both arrays (returned once even though it appears multiple times)."},{"input":"nums1 = [4,9,5], nums2 = [9,4,9,8,4]","output":"[9,4]","explanation":"Both 9 and 4 appear in both arrays."}]$$,
  constraints = $$1 <= nums1.length, nums2.length <= 1000
0 <= nums1[i], nums2[i] <= 1000$$
WHERE slug = 'intersection-of-two-arrays';

UPDATE problems SET
  description = $$Given an array `nums` with `n` objects colored red, white, or blue (represented as 0, 1, and 2), sort them in-place so that objects of the same color are adjacent, in the order red, white, blue. You must solve this without using the library sort function, ideally in one pass using the Dutch National Flag algorithm.$$,
  examples    = $$[{"input":"nums = [2,0,2,1,1,0]","output":"[0,0,1,1,2,2]","explanation":"All 0s (red) come first, then 1s (white), then 2s (blue)."},{"input":"nums = [2,0,1]","output":"[0,1,2]","explanation":"Sorted by color order: 0, 1, 2."}]$$,
  constraints = $$n == nums.length
1 <= n <= 300
nums[i] is either 0, 1, or 2.$$
WHERE slug = 'sort-colors';

UPDATE problems SET
  description = $$Given an `m x n` integer matrix `matrix`, if an element is 0, set its entire row and column to 0. You must do it in-place. Try to use O(1) extra space by using the first row and first column as markers.$$,
  examples    = $$[{"input":"matrix = [[1,1,1],[1,0,1],[1,1,1]]","output":"[[1,0,1],[0,0,0],[1,0,1]]","explanation":"The 0 at position (1,1) causes row 1 and column 1 to be zeroed out."},{"input":"matrix = [[0,1,2,0],[3,4,5,2],[1,3,1,5]]","output":"[[0,0,0,0],[0,4,5,0],[0,3,1,0]]","explanation":"Zeros at (0,0) and (0,3) zero out row 0, column 0, and column 3."}]$$,
  constraints = $$m == matrix.length
n == matrix[0].length
1 <= m, n <= 200
-2^31 <= matrix[i][j] <= 2^31 - 1$$
WHERE slug = 'set-matrix-zeroes';

UPDATE problems SET
  description = $$Given an `m x n` matrix, return all elements of the matrix in spiral order, starting from the top-left corner and moving right, then down, then left, then up, repeatedly inward.$$,
  examples    = $$[{"input":"matrix = [[1,2,3],[4,5,6],[7,8,9]]","output":"[1,2,3,6,9,8,7,4,5]","explanation":"Traverse right along top row, down the right column, left along the bottom, up the left column, then the center."},{"input":"matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]]","output":"[1,2,3,4,8,12,11,10,9,5,6,7]","explanation":"Spiral traversal of a 3x4 matrix."}]$$,
  constraints = $$m == matrix.length
n == matrix[i].length
1 <= m, n <= 10
-100 <= matrix[i][j] <= 100$$
WHERE slug = 'spiral-matrix';

UPDATE problems SET
  description = $$Given an `n x n` 2D matrix representing an image, rotate the image by 90 degrees clockwise in-place. You must rotate the matrix in-place with O(1) extra memory. The standard approach is to first transpose the matrix, then reverse each row.$$,
  examples    = $$[{"input":"matrix = [[1,2,3],[4,5,6],[7,8,9]]","output":"[[7,4,1],[8,5,2],[9,6,3]]","explanation":"Transpose gives [[1,4,7],[2,5,8],[3,6,9]], then reverse each row gives [[7,4,1],[8,5,2],[9,6,3]]."},{"input":"matrix = [[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]","output":"[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]","explanation":"90-degree clockwise rotation of a 4x4 matrix."}]$$,
  constraints = $$n == matrix.length == matrix[i].length
1 <= n <= 20
-1000 <= matrix[i][j] <= 1000$$
WHERE slug = 'rotate-image';

UPDATE problems SET
  description = $$Given an array of integers `nums` and an integer `k`, return the total number of subarrays whose sum equals `k`. A subarray is a contiguous non-empty sequence of elements. Use a prefix sum with a hash map to achieve O(n) time complexity.$$,
  examples    = $$[{"input":"nums = [1,1,1], k = 2","output":"2","explanation":"Subarrays [1,1] (indices 0-1) and [1,1] (indices 1-2) both sum to 2."},{"input":"nums = [1,2,3], k = 3","output":"2","explanation":"Subarray [3] (index 2) and [1,2] (indices 0-1) both sum to 3."}]$$,
  constraints = $$1 <= nums.length <= 2 * 10^4
-1000 <= nums[i] <= 1000
-10^7 <= k <= 10^7$$
WHERE slug = 'subarray-sum-equals-k';

UPDATE problems SET
  description = $$Given an integer array `nums`, find the contiguous subarray within a circular array that has the largest sum. The circular part means you can consider a subarray that wraps around from the end to the beginning of the array.$$,
  examples    = $$[{"input":"nums = [1,-2,3,-2]","output":"3","explanation":"Subarray [3] has maximum sum 3."},{"input":"nums = [5,-3,5]","output":"10","explanation":"Wrap-around subarray [5,5] has maximum sum 10."},{"input":"nums = [-3,-2,-3]","output":"-2","explanation":"All negative, the maximum subarray is [-2] with sum -2."}]$$,
  constraints = $$n == nums.length
1 <= n <= 3 * 10^4
-3 * 10^4 <= nums[i] <= 3 * 10^4$$
WHERE slug = 'maximum-sum-circular-subarray';

UPDATE problems SET
  description = $$Given an integer array `nums` where `nums[i]` represents the maximum number of steps you can jump forward from index `i`, return `true` if you can reach the last index starting from index 0, or `false` otherwise.$$,
  examples    = $$[{"input":"nums = [2,3,1,1,4]","output":"true","explanation":"Jump 1 step from index 0 to 1, then 3 steps to the last index."},{"input":"nums = [3,2,1,0,4]","output":"false","explanation":"You will always arrive at index 3 no matter what. Its maximum jump length is 0, which makes it impossible to reach the last index."}]$$,
  constraints = $$1 <= nums.length <= 10^4
0 <= nums[i] <= 10^5$$
WHERE slug = 'jump-game';

UPDATE problems SET
  description = $$Given an array `nums` where `nums[i]` is the maximum jump length from index `i`, return the minimum number of jumps needed to reach the last index. You can assume it is always possible to reach the last index.$$,
  examples    = $$[{"input":"nums = [2,3,1,1,4]","output":"2","explanation":"Jump from index 0 to index 1 (jump 1), then to the last index (jump 2)."},{"input":"nums = [2,3,0,1,4]","output":"2","explanation":"Jump from index 0 to index 1, then to the last index."}]$$,
  constraints = $$1 <= nums.length <= 10^4
0 <= nums[i] <= 1000
It is guaranteed that you can reach nums[n-1].$$
WHERE slug = 'jump-game-ii';

UPDATE problems SET
  description = $$There are `n` gas stations around a circular route, where gas station `i` has `gas[i]` liters of gas. You have a car with an unlimited gas tank that costs `cost[i]` of gas to travel from station `i` to the next. Return the starting gas station's index if you can travel around the circuit once without running out of gas, or -1 if it is not possible.$$,
  examples    = $$[{"input":"gas = [1,2,3,4,5], cost = [3,4,5,1,2]","output":"3","explanation":"Start at station 3. Tank goes 0+4=4, travel costs 1, arrive station 4 with 3. Continue around — you can complete the circuit."},{"input":"gas = [2,3,4], cost = [3,4,3]","output":"-1","explanation":"Total gas = 9, total cost = 10. Impossible to complete the circuit."}]$$,
  constraints = $$n == gas.length == cost.length
1 <= n <= 10^5
0 <= gas[i], cost[i] <= 10^4$$
WHERE slug = 'gas-station';

UPDATE problems SET
  description = $$There are `n` children standing in a line. Each child is assigned a rating in `ratings[i]`. You must give each child at least one candy, and children with a higher rating than their adjacent neighbor must receive more candies than that neighbor. Return the minimum number of candies you need to distribute.$$,
  examples    = $$[{"input":"ratings = [1,0,2]","output":"5","explanation":"Give candies [2,1,2]. Child 0 has higher rating than child 1 so gets more; child 2 has higher rating than child 1 so gets more."},{"input":"ratings = [1,2,2]","output":"4","explanation":"Give candies [1,2,1]. Child 1 must get more than child 0, but child 2 doesn't need more than child 1 since equal ratings."}]$$,
  constraints = $$n == ratings.length
1 <= n <= 2 * 10^4
0 <= ratings[i] <= 2 * 10^4$$
WHERE slug = 'candy';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, return `true` if there are two distinct indices `i` and `j` such that `nums[i] == nums[j]` and `abs(i - j) <= k`. Use a sliding window hash set or hash map to check within the window of size `k`.$$,
  examples    = $$[{"input":"nums = [1,2,3,1], k = 3","output":"true","explanation":"nums[0] == nums[3] == 1, and abs(0-3) = 3 <= k = 3."},{"input":"nums = [1,0,1,1], k = 1","output":"true","explanation":"nums[2] == nums[3] == 1, and abs(2-3) = 1 <= k = 1."},{"input":"nums = [1,2,3,1,2,3], k = 2","output":"false","explanation":"Duplicates exist but no two identical elements are within distance 2."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^9 <= nums[i] <= 10^9
0 <= k <= 10^5$$
WHERE slug = 'contains-duplicate-ii';

UPDATE problems SET
  description = $$Given an array of strings `strs`, group the anagrams together. You can return the answer in any order. Two strings are anagrams of each other if they contain the same characters with the same frequencies, just in a different order.$$,
  examples    = $$[{"input":"strs = [\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]","output":"[[\"bat\"],[\"nat\",\"tan\"],[\"ate\",\"eat\",\"tea\"]]","explanation":"\"eat\", \"tea\", and \"ate\" are anagrams. \"tan\" and \"nat\" are anagrams. \"bat\" has no anagram."},{"input":"strs = [\"\"]","output":"[[\"\"]]","explanation":"Single empty string."},{"input":"strs = [\"a\"]","output":"[[\"a\"]]","explanation":"Single character."}]$$,
  constraints = $$1 <= strs.length <= 10^4
0 <= strs[i].length <= 100
strs[i] consists of lowercase English letters.$$
WHERE slug = 'group-anagrams';

UPDATE problems SET
  description = $$Given a string `s`, find the first non-repeating character and return its index. If it does not exist, return -1. Use a hash map to count character frequencies, then scan the string to find the first character with a count of 1.$$,
  examples    = $$[{"input":"s = \"leetcode\"","output":"0","explanation":"The first non-repeating character is 'l' at index 0."},{"input":"s = \"loveleetcode\"","output":"2","explanation":"'l' and 'o' both repeat. 'v' at index 2 is the first non-repeating character."},{"input":"s = \"aabb\"","output":"-1","explanation":"Every character repeats, so return -1."}]$$,
  constraints = $$1 <= s.length <= 10^5
s consists of only lowercase English letters.$$
WHERE slug = 'first-unique-character';

UPDATE problems SET
  description = $$Given two strings `s` and `t`, determine if they are isomorphic. Two strings are isomorphic if the characters in `s` can be replaced to get `t`, with a consistent one-to-one mapping where no two characters map to the same character and each character maps to exactly one other character.$$,
  examples    = $$[{"input":"s = \"egg\", t = \"add\"","output":"true","explanation":"'e' maps to 'a' and 'g' maps to 'd'. Consistent mapping exists."},{"input":"s = \"foo\", t = \"bar\"","output":"false","explanation":"'o' maps to 'a' but also would need to map to 'r', which is inconsistent."},{"input":"s = \"paper\", t = \"title\"","output":"true","explanation":"p->t, a->i, e->l, r->e with consistent bijection."}]$$,
  constraints = $$1 <= s.length <= 5 * 10^4
t.length == s.length
s and t consist of any valid ASCII character.$$
WHERE slug = 'isomorphic-strings';

UPDATE problems SET
  description = $$Given a `pattern` and a string `s`, find if `s` follows the same pattern. Here, "follows" means a full match, where each letter in `pattern` maps to exactly one word in `s` and vice versa. The words in `s` are separated by a single space.$$,
  examples    = $$[{"input":"pattern = \"abba\", s = \"dog cat cat dog\"","output":"true","explanation":"'a' maps to 'dog' and 'b' maps to 'cat' with a bijective mapping."},{"input":"pattern = \"abba\", s = \"dog cat cat fish\"","output":"false","explanation":"'a' would need to map to both 'dog' and 'fish'."},{"input":"pattern = \"aaaa\", s = \"dog cat cat dog\"","output":"false","explanation":"'a' maps to 'dog' but 'cat' would also need to map to 'a'."}]$$,
  constraints = $$1 <= pattern.length <= 300
pattern contains only lower-case English letters.
1 <= s.length <= 3000
s contains only lowercase English letters and spaces.
s does not contain any leading or trailing spaces.
All the words in s are separated by a single space.$$
WHERE slug = 'word-pattern';

UPDATE problems SET
  description = $$Write an algorithm to determine if a number `n` is a happy number. Starting from `n`, repeatedly replace it with the sum of squares of its digits. If this process eventually reaches 1, the number is happy. If it loops endlessly in a cycle that never reaches 1, it is not happy. Return `true` if `n` is happy, otherwise `false`.$$,
  examples    = $$[{"input":"n = 19","output":"true","explanation":"1^2 + 9^2 = 82, 8^2 + 2^2 = 68, 6^2 + 8^2 = 100, 1^2 + 0^2 + 0^2 = 1. Reached 1, so happy."},{"input":"n = 2","output":"false","explanation":"The sequence 2->4->16->37->58->89->145->42->20->4 enters a cycle that never reaches 1."}]$$,
  constraints = $$1 <= n <= 2^31 - 1$$
WHERE slug = 'happy-number';

UPDATE problems SET
  description = $$Given two integer arrays `nums1` and `nums2`, return an array of their intersection where each element appears as many times as it appears in both arrays. You may return the result in any order. Unlike the set-based intersection, duplicate elements count separately.$$,
  examples    = $$[{"input":"nums1 = [1,2,2,1], nums2 = [2,2]","output":"[2,2]","explanation":"2 appears twice in both arrays, so it appears twice in the result."},{"input":"nums1 = [4,9,5], nums2 = [9,4,9,8,4]","output":"[4,9]","explanation":"4 appears once in nums1 and twice in nums2 (min 1 time), 9 appears once in each."}]$$,
  constraints = $$1 <= nums1.length, nums2.length <= 1000
0 <= nums1[i], nums2[i] <= 1000$$
WHERE slug = 'intersection-of-two-arrays-ii';

UPDATE problems SET
  description = $$Given an unsorted array of integers `nums`, return the length of the longest consecutive elements sequence. For example, if the array contains 1, 2, 3, 4, the longest consecutive sequence has length 4. Your algorithm must run in O(n) time using a hash set.$$,
  examples    = $$[{"input":"nums = [100,4,200,1,3,2]","output":"4","explanation":"The longest consecutive sequence is [1,2,3,4] with length 4."},{"input":"nums = [0,3,7,2,5,8,4,6,0,1]","output":"9","explanation":"The longest consecutive sequence is [0,1,2,3,4,5,6,7,8] with length 9."}]$$,
  constraints = $$0 <= nums.length <= 10^5
-10^9 <= nums[i] <= 10^9$$
WHERE slug = 'longest-consecutive-sequence';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, return the `k` most frequent elements. You may return the answer in any order. Try to solve it in better than O(n log n) time using a hash map and bucket sort.$$,
  examples    = $$[{"input":"nums = [1,1,1,2,2,3], k = 2","output":"[1,2]","explanation":"1 appears 3 times and 2 appears 2 times; those are the top 2 most frequent."},{"input":"nums = [1], k = 1","output":"[1]","explanation":"Only one element, so it is the most frequent."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^4 <= nums[i] <= 10^4
k is in the range [1, the number of unique elements in the array].
It is guaranteed that the answer is unique.$$
WHERE slug = 'top-k-frequent-elements';

UPDATE problems SET
  description = $$Given an array of strings `words` and an integer `k`, return the `k` most frequent strings. Return the answer sorted by frequency from highest to lowest. If two words have the same frequency, sort them by their lexicographical order (alphabetical order).$$,
  examples    = $$[{"input":"words = [\"i\",\"love\",\"leetcode\",\"i\",\"love\",\"coding\"], k = 2","output":"[\"i\",\"love\"]","explanation":"'i' and 'love' each appear 2 times. They are the most frequent, sorted lexicographically."},{"input":"words = [\"the\",\"day\",\"is\",\"sunny\",\"the\",\"the\",\"the\",\"sunny\",\"is\",\"is\"], k = 4","output":"[\"the\",\"is\",\"sunny\",\"day\"]","explanation":"'the' appears 4 times, 'is' 3, 'sunny' 2, 'day' 1."}]$$,
  constraints = $$1 <= words.length <= 500
1 <= words[i].length <= 10
words[i] consists of lowercase English letters.
k is in the range [1, the number of unique words[i]].$$
WHERE slug = 'top-k-frequent-words';

UPDATE problems SET
  description = $$Given an array of integers `nums` and an integer `k`, return the total number of subarrays whose elements sum to `k`. Use a prefix sum combined with a hash map that tracks how many times each prefix sum has been seen to achieve O(n) time complexity.$$,
  examples    = $$[{"input":"nums = [1,1,1], k = 2","output":"2","explanation":"Subarrays [1,1] at indices 0-1 and 1-2 both sum to 2."},{"input":"nums = [1,2,3], k = 3","output":"2","explanation":"Subarray [3] at index 2 and [1,2] at indices 0-1 both equal 3."}]$$,
  constraints = $$1 <= nums.length <= 2 * 10^4
-1000 <= nums[i] <= 1000
-10^7 <= k <= 10^7$$
WHERE slug = 'subarray-sum-equals-k-hash';

UPDATE problems SET
  description = $$Given a string `s` and an integer `k`, return the length of the longest substring that contains at most `k` distinct characters. Use a sliding window with a hash map that tracks character frequencies within the current window.$$,
  examples    = $$[{"input":"s = \"eceba\", k = 2","output":"3","explanation":"The substring \"ece\" has 2 distinct characters and length 3."},{"input":"s = \"aa\", k = 1","output":"2","explanation":"The entire string has only 1 distinct character."}]$$,
  constraints = $$1 <= s.length <= 5 * 10^4
0 <= k <= 50$$
WHERE slug = 'longest-substring-at-most-k';

UPDATE problems SET
  description = $$Given four integer arrays `nums1`, `nums2`, `nums3`, and `nums4` all of length `n`, return the number of tuples `(i, j, k, l)` such that `nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0`. Use a hash map on the pair sums of the first two arrays, then count matching complement sums from the last two arrays.$$,
  examples    = $$[{"input":"nums1 = [1,2], nums2 = [-2,-1], nums3 = [-1,2], nums4 = [0,2]","output":"2","explanation":"Tuple (0,0,0,1): 1+(-2)+(-1)+2=0. Tuple (1,1,0,0): 2+(-1)+(-1)+0=0."},{"input":"nums1 = [0], nums2 = [0], nums3 = [0], nums4 = [0]","output":"1","explanation":"Only one tuple (0,0,0,0) with sum 0."}]$$,
  constraints = $$n == nums1.length == nums2.length == nums3.length == nums4.length
1 <= n <= 200
-2^28 <= nums1[i], nums2[i], nums3[i], nums4[i] <= 2^28$$
WHERE slug = 'four-sum-ii';

UPDATE problems SET
  description = $$Design a hash map without using any built-in hash table libraries. Implement the `MyHashMap` class with `put(key, val)` to insert or update a key-value pair, `get(key)` to return the value or -1 if not found, and `remove(key)` to remove the key-value pair if it exists.$$,
  examples    = $$[{"input":"[\"MyHashMap\",\"put\",\"put\",\"get\",\"get\",\"put\",\"get\",\"remove\",\"get\"] [[],[1,1],[2,2],[1],[3],[2,1],[2],[2],[2]]","output":"[null,null,null,1,-1,null,1,null,-1]","explanation":"put(1,1) stores key 1 with value 1. put(2,2) stores key 2. get(1) returns 1. get(3) returns -1 (not found). put(2,1) updates key 2. get(2) returns 1. remove(2) deletes key 2. get(2) returns -1."}]$$,
  constraints = $$0 <= key, value <= 10^6
At most 10^4 calls will be made to put, get, and remove.$$
WHERE slug = 'design-hashmap';

UPDATE problems SET
  description = $$Design a hash set without using any built-in hash set libraries. Implement the `MyHashSet` class with `add(key)` to insert a value, `contains(key)` to return whether the value is in the set, and `remove(key)` to remove the value if present.$$,
  examples    = $$[{"input":"[\"MyHashSet\",\"add\",\"add\",\"contains\",\"contains\",\"add\",\"contains\",\"remove\",\"contains\"] [[],[1],[2],[1],[3],[2],[2],[2],[2]]","output":"[null,null,null,true,false,null,true,null,false]","explanation":"add(1) and add(2). contains(1) is true. contains(3) is false. add(2) again (no-op). contains(2) is true. remove(2). contains(2) is false."}]$$,
  constraints = $$0 <= key <= 10^6
At most 10^4 calls will be made to add, remove, and contains.$$
WHERE slug = 'design-hashset';

UPDATE problems SET
  description = $$Design an algorithm to encode a list of strings to a single string, and decode that single string back to the original list. The encode and decode functions must handle any valid ASCII characters including special characters and empty strings. The encoded form should not use any external library.$$,
  examples    = $$[{"input":"strs = [\"Hello\",\"World\"]","output":"[\"Hello\",\"World\"]","explanation":"Encode to a single string (e.g. \"5#Hello5#World\"), then decode back to the original list."},{"input":"strs = [\"\"]","output":"[\"\"]","explanation":"Single empty string must be handled correctly."}]$$,
  constraints = $$1 <= strs.length <= 200
0 <= strs[i].length <= 200
strs[i] contains any possible characters out of 256 valid ASCII characters.$$
WHERE slug = 'encode-decode-strings';

UPDATE problems SET
  description = $$Determine if a `9 x 9` Sudoku board is valid. Only the filled cells need to be validated according to these rules: each row must contain the digits 1-9 without repetition, each column must contain the digits 1-9 without repetition, and each of the nine 3x3 sub-boxes must contain the digits 1-9 without repetition. Empty cells are represented by '.'.$$,
  examples    = $$[{"input":"board = [[\"5\",\"3\",\".\",\".\",\"7\",\".\",\".\",\".\",\".\"],[\"6\",\".\",\".\",\"1\",\"9\",\"5\",\".\",\".\",\".\"],[\".\",\"9\",\"8\",\".\",\".\",\".\",\".\",\"6\",\".\"],[\"8\",\".\",\".\",\".\",\"6\",\".\",\".\",\".\",\"3\"],[\"4\",\".\",\".\",\"8\",\".\",\"3\",\".\",\".\",\"1\"],[\"7\",\".\",\".\",\".\",\"2\",\".\",\".\",\".\",\"6\"],[\".\",\"6\",\".\",\".\",\".\",\".\",\"2\",\"8\",\".\"],[\".\",\".\",\".\",\"4\",\"1\",\"9\",\".\",\".\",\"5\"],[\".\",\".\",\".\",\".\",\"8\",\".\",\".\",\"7\",\"9\"]]","output":"true","explanation":"Board satisfies all three Sudoku constraints."},{"input":"board (with duplicate 8 in top-left box)","output":"false","explanation":"Two 8s appear in the top-left 3x3 box, violating Sudoku rules."}]$$,
  constraints = $$board.length == 9
board[i].length == 9
board[i][j] is a digit 1-9 or '.'.$$
WHERE slug = 'valid-sudoku';

UPDATE problems SET
  description = $$Given two strings `ransomNote` and `magazine`, return `true` if `ransomNote` can be constructed by using letters from `magazine`, otherwise return `false`. Each letter in `magazine` can only be used once in `ransomNote`. Use a frequency hash map for O(n + m) time.$$,
  examples    = $$[{"input":"ransomNote = \"a\", magazine = \"b\"","output":"false","explanation":"'a' is not in magazine."},{"input":"ransomNote = \"aa\", magazine = \"ab\"","output":"false","explanation":"ransomNote needs two 'a's but magazine only has one."},{"input":"ransomNote = \"aa\", magazine = \"aab\"","output":"true","explanation":"magazine has two 'a's which is enough to build ransomNote."}]$$,
  constraints = $$1 <= ransomNote.length, magazine.length <= 10^5
ransomNote and magazine consist of lowercase English letters.$$
WHERE slug = 'ransom-note';

UPDATE problems SET
  description = $$A transformation sequence from word `beginWord` to word `endWord` using a word list `wordList` is a sequence where each adjacent pair of words differs by exactly one letter and each intermediate word exists in the word list. Given `beginWord`, `endWord`, and `wordList`, return the number of words in the shortest transformation sequence, or 0 if no such sequence exists.$$,
  examples    = $$[{"input":"beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\",\"cog\"]","output":"5","explanation":"Shortest transformation: \"hit\" -> \"hot\" -> \"dot\" -> \"dog\" -> \"cog\" has 5 words."},{"input":"beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\"]","output":"0","explanation":"\"cog\" is not in wordList, so no transformation sequence exists."}]$$,
  constraints = $$1 <= beginWord.length <= 10
endWord.length == beginWord.length
1 <= wordList.length <= 5000
wordList[i].length == beginWord.length
beginWord, endWord, and wordList[i] consist of lowercase English letters.
beginWord != endWord
All the words in wordList are unique.$$
WHERE slug = 'word-ladder';

UPDATE problems SET
  description = $$Given two strings `s` and `t` of lengths `m` and `n` respectively, return the minimum window substring of `s` such that every character in `t` (including duplicates) is included in the window. If there is no such substring, return the empty string. Use a sliding window with two hash maps tracking required and window character counts.$$,
  examples    = $$[{"input":"s = \"ADOBECODEBANC\", t = \"ABC\"","output":"\"BANC\"","explanation":"The minimum window is \"BANC\" which contains all characters A, B, C from t."},{"input":"s = \"a\", t = \"a\"","output":"\"a\"","explanation":"The entire string is the minimum window."},{"input":"s = \"a\", t = \"aa\"","output":"\"\"","explanation":"t needs two a's but s only has one."}]$$,
  constraints = $$m == s.length
n == t.length
1 <= m, n <= 10^5
s and t consist of uppercase and lowercase English letters.$$
WHERE slug = 'minimum-window-substring';

UPDATE problems SET
  description = $$Given a string `s` and an array of strings `words` where all strings in `words` have the same length, return all starting indices of substrings in `s` that is a concatenation of each word in `words` exactly once, in any order, without any intervening characters. Use a sliding window with hash maps for each word-length window.$$,
  examples    = $$[{"input":"s = \"barfoothefoobarman\", words = [\"foo\",\"bar\"]","output":"[0,9]","explanation":"At index 0, substring \"barfoo\" is a concatenation of [\"bar\",\"foo\"]. At index 9, \"foobar\" is a concatenation of [\"foo\",\"bar\"]."},{"input":"s = \"wordgoodgoodgoodbestword\", words = [\"word\",\"good\",\"best\",\"word\"]","output":"[]","explanation":"No valid substring concatenation exists."}]$$,
  constraints = $$1 <= s.length <= 10^4
1 <= words.length <= 5000
1 <= words[i].length <= 30
s and words[i] consist of lowercase English letters.$$
WHERE slug = 'substring-concatenation';

UPDATE problems SET
  description = $$Given an array of integers `nums`, return the number of good pairs. A pair `(i, j)` is called good if `nums[i] == nums[j]` and `i < j`. Use a hash map to count how many times each number has appeared so far as you iterate through the array.$$,
  examples    = $$[{"input":"nums = [1,2,3,1,1,3]","output":"4","explanation":"Good pairs are (0,3), (0,4), (3,4) from the three 1s, and (2,5) from the two 3s."},{"input":"nums = [1,1,1,1]","output":"6","explanation":"Any two indices among the four 1s form a good pair: C(4,2) = 6."},{"input":"nums = [1,2,3]","output":"0","explanation":"No element appears more than once, so no good pairs."}]$$,
  constraints = $$1 <= nums.length <= 100
1 <= nums[i] <= 100$$
WHERE slug = 'number-of-good-pairs';

UPDATE problems SET
  description = $$Given an array of integers `arr`, return `true` if the number of occurrences of each value in the array is unique. In other words, no two values should have the same frequency. Use a hash map for frequencies and a hash set to check for duplicate frequency values.$$,
  examples    = $$[{"input":"arr = [1,2,2,1,1,3]","output":"true","explanation":"1 appears 3 times, 2 appears 2 times, 3 appears 1 time. All frequencies (1, 2, 3) are distinct."},{"input":"arr = [1,2]","output":"false","explanation":"1 appears once and 2 appears once — same frequency, so return false."},{"input":"arr = [-3,0,1,-3,1,1,1,-3,10,0]","output":"true","explanation":"Frequencies are all distinct."}]$$,
  constraints = $$1 <= arr.length <= 1000
-1000 <= arr[i] <= 1000$$
WHERE slug = 'unique-number-of-occurrences';

UPDATE problems SET
  description = $$Given a string `s` which consists of lowercase or uppercase letters, return the length of the longest palindrome that can be built with those letters. Letters are case sensitive. Use a hash map to count character frequencies — pairs of characters can always be used, and one odd-frequency character can sit in the middle.$$,
  examples    = $$[{"input":"s = \"abccccdd\"","output":"7","explanation":"One longest palindrome is \"dccaccd\" of length 7."},{"input":"s = \"a\"","output":"1","explanation":"Single character forms a palindrome of length 1."}]$$,
  constraints = $$1 <= s.length <= 2000
s consists of lowercase and/or uppercase English letters.$$
WHERE slug = 'longest-palindrome-hash';

UPDATE problems SET
  description = $$Given an integer array `nums`, find the subarray with the largest product among all possible contiguous subarrays and return the product. This is a variant focusing on handling large inputs and negative number edge cases with two-pass tracking of maximum and minimum products.$$,
  examples    = $$[{"input":"nums = [2,3,-2,4]","output":"6","explanation":"The subarray [2,3] has the largest product 6."},{"input":"nums = [-2,0,-1]","output":"0","explanation":"The result cannot be 2 since [-2,-1] is not contiguous in the full sense here; the best is 0."},{"input":"nums = [-4,-3,-2]","output":"12","explanation":"The subarray [-4,-3] gives product 12."}]$$,
  constraints = $$1 <= nums.length <= 2 * 10^4
-10 <= nums[i] <= 10
The product of any prefix of nums is guaranteed to fit in a 32-bit integer.$$
WHERE slug = 'maximum-product-subarray-2';

UPDATE problems SET
  description = $$Given an unsorted integer array `nums`, return the smallest missing positive integer. You must implement an algorithm that runs in O(n) time and uses O(1) auxiliary space. The key insight is to use the array itself as a hash map by placing each number `x` (where `1 <= x <= n`) at index `x - 1`.$$,
  examples    = $$[{"input":"nums = [1,2,0]","output":"3","explanation":"1 and 2 are present, so the smallest missing positive is 3."},{"input":"nums = [3,4,-1,1]","output":"2","explanation":"1 is present but 2 is missing."},{"input":"nums = [7,8,9,11,12]","output":"1","explanation":"1 is not in the array, so the smallest missing positive is 1."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-2^31 <= nums[i] <= 2^31 - 1$$
WHERE slug = 'first-missing-positive';

UPDATE problems SET
  description = $$Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water can be trapped after raining. For each position, the water level is determined by the minimum of the maximum heights to its left and right, minus the current bar height.$$,
  examples    = $$[{"input":"height = [0,1,0,2,1,0,1,3,2,1,2,1]","output":"6","explanation":"The elevation map traps 6 units of rain water total."},{"input":"height = [4,2,0,3,2,5]","output":"9","explanation":"Water is trapped in the valleys creating 9 units total."}]$$,
  constraints = $$n == height.length
1 <= n <= 2 * 10^4
0 <= height[i] <= 10^5$$
WHERE slug = 'trapping-rain-water';
