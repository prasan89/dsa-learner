-- V28b: Real descriptions for two-pointers, sliding-window, binary-search problems

-- TWO POINTERS

UPDATE problems SET
  description = $$Given a 1-indexed array `numbers` that is already sorted in non-decreasing order, find two numbers such that they add up to a specific `target`. Return the indices of the two numbers as an integer array `[index1, index2]` of length 2, where `1 <= index1 < index2 <= numbers.length`. You may not use the same element twice and there is exactly one solution.$$,
  examples    = $$[{"input":"numbers = [2,7,11,15], target = 9","output":"[1,2]","explanation":"The sum of 2 and 7 is 9. Therefore, index1 = 1, index2 = 2."},{"input":"numbers = [2,3,4], target = 6","output":"[1,3]","explanation":"The sum of 2 and 4 is 6."}]$$,
  constraints = $$2 <= numbers.length <= 3 * 10^4\n-1000 <= numbers[i] <= 1000\nnumbers is sorted in non-decreasing order\n-1000 <= target <= 1000\nThe tests are generated such that there is exactly one solution$$
WHERE slug = 'two-sum-ii';

UPDATE problems SET
  description = $$Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that `i != j`, `i != k`, and `j != k`, and `nums[i] + nums[j] + nums[k] == 0`. The solution set must not contain duplicate triplets.$$,
  examples    = $$[{"input":"nums = [-1,0,1,2,-1,-4]","output":"[[-1,-1,2],[-1,0,1]]","explanation":"nums[0] + nums[1] + nums[2] = (-1) + 0 + 1 = 0. nums[1] + nums[2] + nums[4] = 0 + 1 + (-1) = 0. The distinct triplets are [-1,-1,2] and [-1,0,1]."},{"input":"nums = [0,1,1]","output":"[]","explanation":"The only possible triplet does not sum up to 0."}]$$,
  constraints = $$3 <= nums.length <= 3000\n-10^5 <= nums[i] <= 10^5$$
WHERE slug = '3sum';

UPDATE problems SET
  description = $$Given an integer array `nums` of length `n` and an integer `target`, find three integers in `nums` such that the sum is closest to `target`. Return the sum of the three integers. You may assume that each input would have exactly one solution.$$,
  examples    = $$[{"input":"nums = [-1,2,1,-4], target = 1","output":"2","explanation":"The sum that is closest to the target is 2. (-1 + 2 + 1 = 2)."},{"input":"nums = [0,0,0], target = 1","output":"0","explanation":"The sum that is closest to the target is 0."}]$$,
  constraints = $$3 <= nums.length <= 500\n-1000 <= nums[i] <= 1000\n-10^4 <= target <= 10^4$$
WHERE slug = '3sum-closest';

UPDATE problems SET
  description = $$Given an array `nums` of `n` integers and an integer `target`, return all unique quadruplets `[nums[a], nums[b], nums[c], nums[d]]` such that `0 <= a, b, c, d < n`, `a`, `b`, `c`, and `d` are distinct, and `nums[a] + nums[b] + nums[c] + nums[d] == target`. The solution set must not contain duplicate quadruplets.$$,
  examples    = $$[{"input":"nums = [1,0,-1,0,-2,2], target = 0","output":"[[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]"},{"input":"nums = [2,2,2,2,2], target = 8","output":"[[2,2,2,2]]"}]$$,
  constraints = $$1 <= nums.length <= 200\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9$$
WHERE slug = '4sum';

UPDATE problems SET
  description = $$A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward. Given a string `s`, return `true` if it is a palindrome, or `false` otherwise.$$,
  examples    = $$[{"input":"s = \"A man, a plan, a canal: Panama\"","output":"true","explanation":"\"amanaplanacanalpanama\" is a palindrome."},{"input":"s = \"race a car\"","output":"false","explanation":"\"raceacar\" is not a palindrome."}]$$,
  constraints = $$1 <= s.length <= 2 * 10^5\ns consists only of printable ASCII characters$$
WHERE slug = 'valid-palindrome';

UPDATE problems SET
  description = $$Given a string `s`, return `true` if the `s` can be palindrome after deleting at most one character from it. Otherwise return `false`.$$,
  examples    = $$[{"input":"s = \"aba\"","output":"true"},{"input":"s = \"abca\"","output":"true","explanation":"You could delete the character 'c'."},{"input":"s = \"abc\"","output":"false"}]$$,
  constraints = $$1 <= s.length <= 10^5\ns consists of lowercase English letters$$
WHERE slug = 'valid-palindrome-ii';

UPDATE problems SET
  description = $$Given an integer array `nums` sorted in non-decreasing order, remove the duplicates in-place such that each unique element appears only once. The relative order of the elements should be kept the same. Return `k` after placing the final result in the first `k` slots of `nums`.$$,
  examples    = $$[{"input":"nums = [1,1,2]","output":"2, nums = [1,2,_]","explanation":"Your function should return k = 2, with the first two elements of nums being 1 and 2 respectively."},{"input":"nums = [0,0,1,1,1,2,2,3,3,4]","output":"5, nums = [0,1,2,3,4,_,_,_,_,_]"}]$$,
  constraints = $$1 <= nums.length <= 3 * 10^4\n-100 <= nums[i] <= 100\nnums is sorted in non-decreasing order$$
WHERE slug = 'remove-duplicates-sorted';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `val`, remove all occurrences of `val` in `nums` in-place. The order of the elements may be changed. Return `k`, the number of elements in `nums` which are not equal to `val`.$$,
  examples    = $$[{"input":"nums = [3,2,2,3], val = 3","output":"2, nums = [2,2,_,_]","explanation":"Your function should return k = 2, with the first two elements of nums being 2."},{"input":"nums = [0,1,2,2,3,0,4,2], val = 2","output":"5, nums = [0,1,4,0,3,_,_,_]"}]$$,
  constraints = $$0 <= nums.length <= 100\n0 <= nums[i] <= 50\n0 <= val <= 100$$
WHERE slug = 'remove-element';

UPDATE problems SET
  description = $$You are given two integer arrays `nums1` and `nums2`, sorted in non-decreasing order, and two integers `m` and `n`, representing the number of elements in `nums1` and `nums2` respectively. Merge `nums1` and `nums2` into a single array sorted in non-decreasing order, in-place into `nums1`.$$,
  examples    = $$[{"input":"nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3","output":"[1,2,2,3,5,6]","explanation":"The arrays we are merging are [1,2,3] and [2,5,6]."},{"input":"nums1 = [1], m = 1, nums2 = [], n = 0","output":"[1]"}]$$,
  constraints = $$nums1.length == m + n\nnums2.length == n\n0 <= m, n <= 200\n1 <= m + n <= 200\n-10^9 <= nums1[i], nums2[j] <= 10^9$$
WHERE slug = 'merge-sorted-array-pointers';

UPDATE problems SET
  description = $$Given an integer array `nums` sorted in non-decreasing order, return an array of the squares of each number sorted in non-decreasing order.$$,
  examples    = $$[{"input":"nums = [-4,-1,0,3,10]","output":"[0,1,9,16,100]","explanation":"After squaring, the array becomes [16,1,0,9,100]. After sorting, it becomes [0,1,9,16,100]."},{"input":"nums = [-7,-3,2,3,11]","output":"[4,9,9,49,121]"}]$$,
  constraints = $$1 <= nums.length <= 10^4\n-10^4 <= nums[i] <= 10^4\nnums is sorted in non-decreasing order$$
WHERE slug = 'squares-sorted-array';

UPDATE problems SET
  description = $$Given an array `nums` with `n` objects colored red, white, or blue (represented as 0, 1, and 2 respectively), sort them in-place so that objects of the same color are adjacent, with the colors in the order red, white, and blue. You must solve this problem without using the library's sort function.$$,
  examples    = $$[{"input":"nums = [2,0,2,1,1,0]","output":"[0,0,1,1,2,2]"},{"input":"nums = [2,0,1]","output":"[0,1,2]"}]$$,
  constraints = $$n == nums.length\n1 <= n <= 300\nnums[i] is either 0, 1, or 2$$
WHERE slug = 'sort-colors-pointers';

UPDATE problems SET
  description = $$You are given a string `s`. We want to partition the string into as many parts as possible so that each letter appears in at most one part. Return a list of integers representing the size of these parts.$$,
  examples    = $$[{"input":"s = \"ababcbacadefegdehijhklij\"","output":"[9,7,8]","explanation":"The partition is \"ababcbaca\", \"defegde\", \"hijhklij\". This is a partition so that each letter appears in at most one part."},{"input":"s = \"eccbbbbdec\"","output":"[10]"}]$$,
  constraints = $$1 <= s.length <= 500\ns consists of lowercase English letters$$
WHERE slug = 'partition-labels';

UPDATE problems SET
  description = $$You are given two lists of closed intervals, `firstList` and `secondList`, where `firstList[i] = [starti, endi]` and `secondList[j] = [startj, endj]`. Each list of intervals is pairwise disjoint and in sorted order. Return the intersection of these two interval lists.$$,
  examples    = $$[{"input":"firstList = [[0,2],[5,10],[13,23],[24,25]], secondList = [[1,5],[8,12],[15,24],[25,26]]","output":"[[1,2],[5,5],[8,10],[15,23],[24,24],[25,25]]"},{"input":"firstList = [[1,3],[5,9]], secondList = []","output":"[]"}]$$,
  constraints = $$0 <= firstList.length, secondList.length <= 1000\nfirstList[i].length == secondList[j].length == 2\n0 <= starti < endi <= 10^9\nenti < starti+1\n0 <= startj < endj <= 10^9\nendj < startj+1$$
WHERE slug = 'interval-list-intersections';

UPDATE problems SET
  description = $$Given two strings `s` and `t`, return `true` if they are equal when both are typed into empty text editors. `#` means a backspace character. Note that after backspacing an empty text, the text will continue empty.$$,
  examples    = $$[{"input":"s = \"ab#c\", t = \"ad#c\"","output":"true","explanation":"Both s and t become \"ac\"."},{"input":"s = \"ab##\", t = \"c#d#\"","output":"true","explanation":"Both s and t become \"\"."}]$$,
  constraints = $$1 <= s.length, t.length <= 200\ns and t only contain lowercase letters and '#' characters$$
WHERE slug = 'backspace-string-compare';

UPDATE problems SET
  description = $$You are given an array `people` where `people[i]` is the weight of the `i`-th person, and an infinite number of boats where each boat can carry a maximum weight of `limit`. Each boat carries at most two people at the same time, provided the sum of the weight of those people is at most `limit`. Return the minimum number of boats to carry every given person.$$,
  examples    = $$[{"input":"people = [1,2], limit = 3","output":"1","explanation":"1 boat (1, 2)"},{"input":"people = [3,2,2,1], limit = 3","output":"3","explanation":"3 boats (1, 2), (2) and (3)"}]$$,
  constraints = $$1 <= people.length <= 5 * 10^4\n1 <= people[i] <= limit <= 3 * 10^4$$
WHERE slug = 'boats-to-save-people';

UPDATE problems SET
  description = $$You have an initial power `power`, an initial score of `0`, and a bag of tokens where `tokens[i]` is the value of the `i`-th token. You can play tokens in two ways: face-up (spend `tokens[i]` power to gain 1 point) or face-down (spend 1 point to gain `tokens[i]` power). Return the largest number of points you can achieve after playing any number of tokens.$$,
  examples    = $$[{"input":"tokens = [100], power = 50","output":"0","explanation":"Playing the only token in the bag is impossible because you either have too little power or too little points."},{"input":"tokens = [200,100], power = 150","output":"1","explanation":"Play token1 (100) face-up, gaining 1 point. Your power is 50 now. You cannot play token0 face-up."}]$$,
  constraints = $$0 <= tokens.length <= 1000\n0 <= tokens[i], power < 10^4$$
WHERE slug = 'bag-of-tokens';

UPDATE problems SET
  description = $$The pair sum of a pair `(a,b)` is equal to `a + b`. The maximum pair sum is the largest pair sum in a list of pairs. Given an array `nums` of even length `n`, pair up the elements of `nums` into `n / 2` pairs such that the maximum pair sum is minimized. Return the minimized maximum pair sum after optimally pairing up the elements.$$,
  examples    = $$[{"input":"nums = [3,5,2,3]","output":"7","explanation":"The elements can be paired up into pairs (3,3) and (5,2). The maximum pair sum is max(3+3, 5+2) = max(6, 7) = 7."},{"input":"nums = [3,5,4,2,4,6]","output":"8","explanation":"The elements can be paired up into pairs (3,5), (4,4), and (2,6). The maximum pair sum is max(8,8,8) = 8."}]$$,
  constraints = $$n == nums.length\n2 <= n <= 10^5\nn is even\n1 <= nums[i] <= 10^5$$
WHERE slug = 'minimize-maximum-pair-sum';

UPDATE problems SET
  description = $$You are given an integer array `nums` of length `n` and an integer `target`. A subsequence of `nums` is beautiful if the sum of the first and last elements is less than or equal to `target`. Return the number of non-empty beautiful subsequences of `nums`. Since the answer may be very large, return it modulo `10^9 + 7`.$$,
  examples    = $$[{"input":"nums = [3,5,6,7], target = 9","output":"4","explanation":"There are 4 beautiful subsequences: [3], [5], [3,5] and [3,6]."},{"input":"nums = [3,3,6,8], target = 10","output":"6","explanation":"There are 6 beautiful subsequences."}]$$,
  constraints = $$1 <= nums.length <= 10^5\n1 <= nums[i] <= 10^6\n1 <= target <= 10^6$$
WHERE slug = 'number-of-subsequences';

UPDATE problems SET
  description = $$You may recall that an array `arr` is a mountain array if and only if `arr.length >= 3`, there exists some index `i` with `0 < i < arr.length - 1` such that `arr[0] < arr[1] < ... < arr[i-1] < arr[i]` and `arr[i] > arr[i+1] > ... > arr[arr.length - 1]`. Given an integer array `nums`, return the length of the longest mountain subarray.$$,
  examples    = $$[{"input":"nums = [2,1,4,7,3,2,5]","output":"5","explanation":"The largest mountain is [1,4,7,3,2] which has length 5."},{"input":"nums = [2,2,2]","output":"0","explanation":"There is no mountain."}]$$,
  constraints = $$1 <= nums.length <= 10^4\n0 <= nums[i] <= 10^4$$
WHERE slug = 'longest-mountain';

UPDATE problems SET
  description = $$Given an array `nums` containing `n` integers where each integer is in the range `[1, n]` (inclusive), and each integer appears once or twice, find the one integer that appears twice and return it. You must solve it without modifying the array `nums` and using only constant extra space.$$,
  examples    = $$[{"input":"nums = [1,3,4,2,2]","output":"2"},{"input":"nums = [3,1,3,4,2]","output":"3"}]$$,
  constraints = $$1 <= n <= 10^5\nnums.length == n + 1\n1 <= nums[i] <= n\nAll the integers in nums appear only once except for precisely one integer which appears twice$$
WHERE slug = 'find-the-duplicate-number';

UPDATE problems SET
  description = $$You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two endpoints of the `i`-th line are `(i, 0)` and `(i, height[i])`. Find two lines that together with the x-axis form a container that contains the most water. Return the maximum amount of water a container can store.$$,
  examples    = $$[{"input":"height = [1,8,6,2,5,4,8,3,7]","output":"49","explanation":"The two lines above form a container, the area is min(8,7) * (8-1) = 49."},{"input":"height = [1,1]","output":"1"}]$$,
  constraints = $$n == height.length\n2 <= n <= 10^5\n0 <= height[i] <= 10^4$$
WHERE slug = 'container-rain-variant';

UPDATE problems SET
  description = $$Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining. Use the two-pointer approach to achieve O(n) time and O(1) space.$$,
  examples    = $$[{"input":"height = [0,1,0,2,1,0,1,3,2,1,2,1]","output":"6","explanation":"The elevation map is represented by array [0,1,0,2,1,0,1,3,2,1,2,1]. In this case, 6 units of rain water are being trapped."},{"input":"height = [4,2,0,3,2,5]","output":"9"}]$$,
  constraints = $$n == height.length\n1 <= n <= 2 * 10^4\n0 <= height[i] <= 10^5$$
WHERE slug = 'trapping-rain-two-pointers';

UPDATE problems SET
  description = $$Given an integer array `nums` of length `n` and an integer `target`, return the count of triplets that have a sum smaller than `target`. Given `i < j < k`, count triplets where `nums[i] + nums[j] + nums[k] < target`.$$,
  examples    = $$[{"input":"nums = [-2,0,1,3], target = 2","output":"2","explanation":"Because there are two triplets which sums are less than 2: [-2,0,1] and [-2,0,3]."},{"input":"nums = [], target = 0","output":"0"}]$$,
  constraints = $$n == nums.length\n0 <= n <= 3500\n-100 <= nums[i] <= 100\n-100 <= target <= 100$$
WHERE slug = 'three-sum-smaller';

UPDATE problems SET
  description = $$Given four integer arrays `nums1`, `nums2`, `nums3`, and `nums4` all of length `n`, return the number of tuples `(i, j, k, l)` such that `nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0`.$$,
  examples    = $$[{"input":"nums1 = [1,2], nums2 = [-2,-1], nums3 = [-1,2], nums4 = [0,2]","output":"2","explanation":"The two tuples are: (0,0,0,1) -> nums1[0] + nums2[0] + nums3[0] + nums4[1] = 1 + (-2) + (-1) + 2 = 0, (1,1,0,0) -> 1 + (-1) + (-1) + 0 + 0 = 0."},{"input":"nums1 = [0], nums2 = [0], nums3 = [0], nums4 = [0]","output":"1"}]$$,
  constraints = $$n == nums1.length == nums2.length == nums3.length == nums4.length\n1 <= n <= 200\n-2^28 <= nums1[i], nums2[i], nums3[i], nums4[i] <= 2^28$$
WHERE slug = 'four-sum-ii-pointers';

-- SLIDING WINDOW

UPDATE problems SET
  description = $$You are given an integer array `nums` consisting of `n` elements, and an integer `k`. Find a contiguous subarray whose length is equal to `k` that has the maximum average value and return this value.$$,
  examples    = $$[{"input":"nums = [1,12,-5,-6,50,3], k = 4","output":"12.75000","explanation":"Maximum average is (12 - 5 - 6 + 50) / 4 = 51 / 4 = 12.75."},{"input":"nums = [5], k = 1","output":"5.00000"}]$$,
  constraints = $$n == nums.length\n1 <= k <= n <= 10^5\n-10^4 <= nums[i] <= 10^4$$
WHERE slug = 'maximum-average-subarray';

UPDATE problems SET
  description = $$Given a binary array `nums` and an integer `k`, return the maximum number of consecutive `1`'s in the array if you can flip at most `k` `0`'s.$$,
  examples    = $$[{"input":"nums = [1,1,1,0,0,0,1,1,1,1,0], k = 2","output":"6","explanation":"[1,1,1,0,0,1,1,1,1,1,1]. Bolded numbers were flipped from 0 to 1. The longest subarray is underlined."},{"input":"nums = [0,0,1,1,0,0,1,1,1,0,1,1,0,0,0,1,1,1,1], k = 3","output":"10"}]$$,
  constraints = $$1 <= nums.length <= 10^5\nnums[i] is either 0 or 1\n0 <= k <= nums.length$$
WHERE slug = 'max-consecutive-ones';

UPDATE problems SET
  description = $$Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`, or `false` otherwise. In other words, return `true` if one of `s1`'s permutations is the substring of `s2`.$$,
  examples    = $$[{"input":"s1 = \"ab\", s2 = \"eidbaooo\"","output":"true","explanation":"s2 contains one permutation of s1 (\"ba\")."},{"input":"s1 = \"ab\", s2 = \"eidboaoo\"","output":"false"}]$$,
  constraints = $$1 <= s1.length, s2.length <= 10^4\ns1 and s2 consist of lowercase English letters$$
WHERE slug = 'permutation-in-string';

UPDATE problems SET
  description = $$Given two strings `s` and `p`, return an array of all the start indices of `p`'s anagrams in `s`. You may return the answer in any order. An anagram is a string that contains the same characters as another string but in different order.$$,
  examples    = $$[{"input":"s = \"cbaebabacd\", p = \"abc\"","output":"[0,6]","explanation":"The substring with start index 0 is \"cba\", which is an anagram of \"abc\". The substring with start index 6 is \"bac\", which is an anagram of \"abc\"."},{"input":"s = \"abab\", p = \"ab\"","output":"[0,1,2]"}]$$,
  constraints = $$1 <= s.length, p.length <= 3 * 10^4\ns and p consist of lowercase English letters$$
WHERE slug = 'find-all-anagrams';

UPDATE problems SET
  description = $$Given an array of positive integers `nums` and a positive integer `target`, return the minimal length of a contiguous subarray whose sum is greater than or equal to `target`. If there is no such subarray, return `0` instead.$$,
  examples    = $$[{"input":"target = 7, nums = [2,3,1,2,4,3]","output":"2","explanation":"The subarray [4,3] has the minimal length under the problem constraint."},{"input":"target = 4, nums = [1,4,4]","output":"1"}]$$,
  constraints = $$1 <= target <= 10^9\n1 <= nums.length <= 10^5\n1 <= nums[i] <= 10^4$$
WHERE slug = 'minimum-size-subarray-sum';

UPDATE problems SET
  description = $$You are given a string `s` and an integer `k`. You can choose any character of the string and change it to any other uppercase English character. You can perform this operation at most `k` times. Return the length of the longest substring containing the same letter you can get after performing the above operations.$$,
  examples    = $$[{"input":"s = \"ABAB\", k = 2","output":"4","explanation":"Replace the two 'A's with two 'B's or vice versa."},{"input":"s = \"AABABBA\", k = 1","output":"4","explanation":"Replace the one 'A' in the middle with 'B' and form \"AABBBBA\". The substring \"BBBB\" has the longest repeating letters, which is 4."}]$$,
  constraints = $$1 <= s.length <= 10^5\ns consists of only uppercase English letters\n0 <= k <= s.length$$
WHERE slug = 'longest-repeating-character-replacement';

UPDATE problems SET
  description = $$You are visiting a farm that has a single row of fruit trees arranged left to right. The trees are represented by an integer array `fruits` where `fruits[i]` is the type of fruit the `i`-th tree produces. You want to collect as much fruit as possible with two baskets (each basket holds only one type of fruit). Return the maximum number of fruits you can pick.$$,
  examples    = $$[{"input":"fruits = [1,2,1]","output":"3","explanation":"We can pick from all 3 trees."},{"input":"fruits = [0,1,2,2]","output":"3","explanation":"We can pick from trees [1,2,2]. If we had started at the first tree, we would only pick from trees [0,1]."}]$$,
  constraints = $$1 <= fruits.length <= 10^5\n0 <= fruits[i] <= fruits.length$$
WHERE slug = 'fruit-into-baskets';

UPDATE problems SET
  description = $$Given a binary array `nums`, you should delete one element from it. Return the size of the longest non-empty subarray containing only `1`'s in the resulting array. Return `0` if there is no such subarray.$$,
  examples    = $$[{"input":"nums = [1,1,0,1]","output":"3","explanation":"After deleting the number in position 2, [1,1,1] contains 3 numbers with value of 1's."},{"input":"nums = [0,1,1,1,0,1,1,0,1]","output":"5","explanation":"After deleting the number in position 4, [0,1,1,1,1,1,0,1] longest subarray with value of 1's is [1,1,1,1,1]."}]$$,
  constraints = $$1 <= nums.length <= 10^5\nnums[i] is either 0 or 1$$
WHERE slug = 'longest-subarray-after-deleting';

UPDATE problems SET
  description = $$Given a string `s` and an integer `k`, return the maximum number of vowel letters in any substring of `s` with length `k`. Vowel letters in English are 'a', 'e', 'i', 'o', and 'u'.$$,
  examples    = $$[{"input":"s = \"abciiidef\", k = 3","output":"3","explanation":"The substring \"iii\" contains 3 vowel letters."},{"input":"s = \"aeiou\", k = 2","output":"2","explanation":"Any substring of length 2 contains 2 vowels."}]$$,
  constraints = $$1 <= s.length <= 10^5\ns consists of lowercase English letters\n1 <= k <= s.length$$
WHERE slug = 'max-vowels-substring';

UPDATE problems SET
  description = $$You have a bomb to defuse, and your time is running out. Your informer will provide you with a circular array `code` of length `n` and a key `k`. To decrypt the code, you must replace every number. If `k > 0`, replace `code[i]` with the sum of the next `k` numbers. If `k < 0`, replace `code[i]` with the sum of the previous `k` numbers. If `k == 0`, replace `code[i]` with `0`. Return the decrypted code.$$,
  examples    = $$[{"input":"code = [5,7,1,4], k = 3","output":"[12,10,16,13]","explanation":"Each number is replaced by the sum of the next 3 numbers."},{"input":"code = [1,2,3,4], k = 0","output":"[0,0,0,0]"}]$$,
  constraints = $$n == code.length\n1 <= n <= 100\n1 <= code[i] <= 100\n-(n - 1) <= k <= n - 1$$
WHERE slug = 'defuse-the-bomb';

UPDATE problems SET
  description = $$Given a binary array `data`, return the minimum number of swaps required to group all `1`s present in the array together in any place in the array.$$,
  examples    = $$[{"input":"data = [1,0,1,0,1]","output":"1","explanation":"There are 3 ways to group all 1s together: [1,1,1,0,0] using 1 swap, [0,1,1,1,0] using 2 swaps, and [0,0,1,1,1] using 1 swap."},{"input":"data = [0,0,0,1,0]","output":"0","explanation":"Since there is only one 1 in the array, no swaps needed."}]$$,
  constraints = $$1 <= data.length <= 10^5\ndata[i] is either 0 or 1$$
WHERE slug = 'minimum-swaps-group-ones';

UPDATE problems SET
  description = $$A bookstore owner has a store open for `n` minutes. Every minute, some number of customers enter the store. All these customers leave after the end of that minute. On some minutes, the bookstore owner is grumpy. When the owner is grumpy, the customers of that minute are not satisfied; otherwise they are satisfied. The owner knows a secret technique to keep themselves not grumpy for `minutes` consecutive minutes. Return the maximum number of customers that can be satisfied throughout the day.$$,
  examples    = $$[{"input":"customers = [1,0,1,2,1,1,7,5], grumpy = [0,1,0,1,0,1,0,1], minutes = 3","output":"16","explanation":"The bookstore owner keeps themselves not grumpy for the last 3 minutes. The maximum number of customers that can be satisfied = 1 + 1 + 1 + 1 + 7 + 5 = 16."},{"input":"customers = [1], grumpy = [0], minutes = 1","output":"1"}]$$,
  constraints = $$n == customers.length == grumpy.length\n1 <= minutes <= n <= 2 * 10^4\n0 <= customers[i] <= 1000\ngrumpy[i] is either 0 or 1$$
WHERE slug = 'grumpy-bookstore-owner';

UPDATE problems SET
  description = $$Given a binary array `nums` and an integer `goal`, return the number of non-empty subarrays with a sum equal to `goal`. A subarray is a contiguous part of the array.$$,
  examples    = $$[{"input":"nums = [1,0,1,0,1], goal = 2","output":"4"},{"input":"nums = [0,0,0,0,0], goal = 0","output":"15"}]$$,
  constraints = $$1 <= nums.length <= 3 * 10^4\nnums[i] is either 0 or 1\n0 <= goal <= nums.length$$
WHERE slug = 'binary-subarrays-sum';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, return the number of good subarrays of `nums`. A good array is an array where the number of different integers in that array is exactly `k`. A good subarray is a subarray that is good.$$,
  examples    = $$[{"input":"nums = [1,2,1,2,3], k = 2","output":"7","explanation":"Subarrays formed with exactly 2 different integers: [1,2], [2,1], [1,2], [2,3], [1,2,1], [2,1,2], [1,2,1,2]."},{"input":"nums = [1,2,1,3,4], k = 3","output":"3"}]$$,
  constraints = $$1 <= nums.length <= 2 * 10^4\n1 <= nums[i], k <= nums.length$$
WHERE slug = 'subarrays-k-distinct';

UPDATE problems SET
  description = $$Given a string `s` and an array of strings `words`, return all starting indices of substring(s) in `s` that is a concatenation of each word in `words` exactly once, in any order, and without any intervening characters. You can return the answer in any order.$$,
  examples    = $$[{"input":"s = \"barfoothefoobarman\", words = [\"foo\",\"bar\"]","output":"[0,9]","explanation":"Substrings starting at index 0 and 9 are \"barfoo\" and \"foobar\" respectively."},{"input":"s = \"wordgoodgoodgoodbestword\", words = [\"word\",\"good\",\"best\",\"word\"]","output":"[]"}]$$,
  constraints = $$1 <= s.length <= 10^4\ns consists of lowercase English letters\n1 <= words.length <= 5000\n1 <= words[i].length <= 30\nwords[i] consists of lowercase English letters$$
WHERE slug = 'substring-concatenation-window';

UPDATE problems SET
  description = $$Given a string `s`, return the length of the longest substring that contains at most two distinct characters.$$,
  examples    = $$[{"input":"s = \"eceba\"","output":"3","explanation":"The substring is \"ece\" which its length is 3."},{"input":"s = \"ccaabbb\"","output":"5","explanation":"The substring is \"aabbb\" which its length is 5."}]$$,
  constraints = $$1 <= s.length <= 10^5\ns consists of English letters$$
WHERE slug = 'longest-substring-two-distinct';

UPDATE problems SET
  description = $$Given an array of integers `nums` and an integer `k`, return the number of continuous subarrays that have an odd number of elements equal to `k`.$$,
  examples    = $$[{"input":"nums = [1,1,2,1,1], k = 3","output":"2","explanation":"The only sub-arrays with 3 odd numbers are [1,1,2,1] and [1,2,1,1]."},{"input":"nums = [2,4,6], k = 1","output":"0"}]$$,
  constraints = $$1 <= nums.length <= 50000\n1 <= nums[i] <= 10^5\n1 <= k <= nums.length$$
WHERE slug = 'count-number-nice-subarrays';

UPDATE problems SET
  description = $$Given an array of integers `nums` and an integer `k`, return the number of contiguous subarrays where the product of all the elements in the subarray is strictly less than `k`.$$,
  examples    = $$[{"input":"nums = [10,5,2,6], k = 100","output":"8","explanation":"The 8 subarrays that have product less than 100 are: [10],[5],[2],[6],[10,5],[5,2],[2,6],[5,2,6]."},{"input":"nums = [1,2,3], k = 0","output":"0"}]$$,
  constraints = $$1 <= nums.length <= 3 * 10^4\n1 <= nums[i] <= 1000\n0 <= k <= 10^6$$
WHERE slug = 'number-subarrays-product-less-k';

UPDATE problems SET
  description = $$There are several cards arranged in a row, and each card has an associated number of points given in the integer array `cardPoints`. In one step, you can take one card from the beginning or from the end of the row. You have to take exactly `k` cards. Return the maximum score you can obtain.$$,
  examples    = $$[{"input":"cardPoints = [1,2,3,4,5,6,1], k = 3","output":"12","explanation":"After the first step, your score will always be 1. However, choosing the rightmost card first will maximize your total score. The optimal strategy is to take the three cards on the right, giving a final score of 1 + 6 + 5 = 12."},{"input":"cardPoints = [2,2,2], k = 2","output":"4"}]$$,
  constraints = $$1 <= cardPoints.length <= 10^5\n1 <= cardPoints[i] <= 10^4\n1 <= k <= cardPoints.length$$
WHERE slug = 'max-points-card';

UPDATE problems SET
  description = $$A dieter consumes `calories[i]` calories on the `i`-th day. Given an integer `k`, return the number of days where the dieter performs poorly (`-1`), well (`1`), or normally (`0`) based on their average calorie intake over a rolling window of `k` days compared to a `lower` and `upper` threshold.$$,
  examples    = $$[{"input":"calories = [1,2,3,4,5], k = 1, lower = 3, upper = 3","output":"[-1,-1,0,1,1]"},{"input":"calories = [3,2], k = 2, lower = 0, upper = 1","output":"[1]"}]$$,
  constraints = $$1 <= k <= calories.length <= 10^5\n0 <= calories[i] <= 20000\n0 <= lower <= upper$$
WHERE slug = 'diet-plan-performance';

UPDATE problems SET
  description = $$You are given a string `s` and you want to erase at most one substring. Return the maximum length of a substring after erasing exactly one substring such that the remaining string has no duplicate characters. More precisely, return the maximum number of unique characters in any contiguous subarray of `s`.$$,
  examples    = $$[{"input":"s = \"4fecdb\"","output":"6","explanation":"All characters are unique."},{"input":"s = \"abcabc\"","output":"3","explanation":"Erase \"abc\" from middle or ends."}]$$,
  constraints = $$1 <= s.length <= 10^5\ns consists of only lowercase English letters$$
WHERE slug = 'maximum-erasure-value';

UPDATE problems SET
  description = $$Given an array of integers `nums` and two integers `limit`, return the size of the longest non-empty subarray such that the absolute difference between any two elements of this subarray is less than or equal to `limit`.$$,
  examples    = $$[{"input":"nums = [8,2,4,7], limit = 4","output":"2","explanation":"All subarrays are: [8] with maximum absolute diff |8-8| = 0 <= 4, [8,2] with maximum absolute diff |8-2| = 6 > 4, [2,4] with maximum absolute diff |4-2| = 2 <= 4, [4,7] with maximum absolute diff |7-4| = 3 <= 4, and [2,4,7] with |7-2|=5>4. So the answer is 2."},{"input":"nums = [10,1,2,4,7,2], limit = 5","output":"4"}]$$,
  constraints = $$1 <= nums.length <= 10^5\n1 <= nums[i] <= 10^9\n0 <= limit <= 10^9$$
WHERE slug = 'longest-subarray-limit';

UPDATE problems SET
  description = $$Given a string `s` and a string `t`, return the minimum window substring of `s` such that every character in `t` (including duplicates) is included in the window. If there is no such substring, return the empty string `""`.$$,
  examples    = $$[{"input":"s = \"ADOBECODEBANC\", t = \"ABC\"","output":"\"BANC\"","explanation":"The minimum window substring \"BANC\" includes 'A', 'B', and 'C' from string t."},{"input":"s = \"a\", t = \"a\"","output":"\"a\""}]$$,
  constraints = $$m == s.length\nn == t.length\n1 <= m, n <= 10^5\ns and t consist of uppercase and lowercase English letters$$
WHERE slug = 'minimum-window-substring-window';

UPDATE problems SET
  description = $$Given a string `s` and an integer `k`, return the length of the longest substring of `s` such that the frequency of each character in this substring is greater than or equal to `k`. If no such substring exists, return `0`.$$,
  examples    = $$[{"input":"s = \"aaabb\", k = 3","output":"3","explanation":"The longest substring is \"aaa\", as 'a' is repeated 3 times."},{"input":"s = \"ababbc\", k = 2","output":"5","explanation":"The longest substring is \"ababb\", as both 'a' and 'b' repeat at least 2 times."}]$$,
  constraints = $$1 <= s.length <= 10^4\ns consists of only lowercase English letters\n1 <= k <= 10^5$$
WHERE slug = 'longest-substring-k-repeating';

-- BINARY SEARCH

UPDATE problems SET
  description = $$Given a sorted array of distinct integers `nums` and a target value, return the index if the target is found. If not, return the index where it would be if it were inserted in order.$$,
  examples    = $$[{"input":"nums = [1,3,5,6], target = 5","output":"2"},{"input":"nums = [1,3,5,6], target = 2","output":"1"},{"input":"nums = [1,3,5,6], target = 7","output":"4"}]$$,
  constraints = $$1 <= nums.length <= 10^4\n-10^4 <= nums[i] <= 10^4\nnums contains distinct values sorted in ascending order\n-10^4 <= target <= 10^4$$
WHERE slug = 'search-insert-position';

UPDATE problems SET
  description = $$You are a product manager and currently leading a team to develop a new product. Unfortunately, the latest version of your product fails the quality check. Since each version is developed based on the previous version, all the versions after a bad version are also bad. You have `n` versions `[1, 2, ..., n]` and you want to find out the first bad one. Implement a function to find the first bad version using the `isBadVersion` API with the minimum number of calls.$$,
  examples    = $$[{"input":"n = 5, bad = 4","output":"4","explanation":"call isBadVersion(3) -> false, call isBadVersion(5) -> true, call isBadVersion(4) -> true. Then 4 is the first bad version."},{"input":"n = 1, bad = 1","output":"1"}]$$,
  constraints = $$1 <= bad <= n <= 2^31 - 1$$
WHERE slug = 'first-bad-version';

UPDATE problems SET
  description = $$Given a non-negative integer `x`, return the square root of `x` rounded down to the nearest integer. The returned integer should be non-negative as well. You must not use any built-in exponent function or operator.$$,
  examples    = $$[{"input":"x = 4","output":"2","explanation":"The square root of 4 is 2, so we return 2."},{"input":"x = 8","output":"2","explanation":"The square root of 8 is 2.82842..., and since we round it down to the nearest integer, 2 is returned."}]$$,
  constraints = $$0 <= x <= 2^31 - 1$$
WHERE slug = 'sqrt-x';

UPDATE problems SET
  description = $$Given a positive integer `num`, return `true` if `num` is a perfect square or `false` otherwise. A perfect square is an integer that is the square of an integer. You must not use any built-in library function, such as `sqrt`.$$,
  examples    = $$[{"input":"num = 16","output":"true","explanation":"We return true because 4 * 4 = 16 and 4 is an integer."},{"input":"num = 14","output":"false","explanation":"We return false because 3.742 * 3.742 = 14 and 3.742 is not an integer."}]$$,
  constraints = $$1 <= num <= 2^31 - 1$$
WHERE slug = 'valid-perfect-square';

UPDATE problems SET
  description = $$Given an integer array `nums`, find a peak element, and return its index. If the array contains multiple peaks, return the index to any of the peaks. A peak element is an element that is strictly greater than its neighbors. You must write an algorithm that runs in `O(log n)` time.$$,
  examples    = $$[{"input":"nums = [1,2,3,1]","output":"2","explanation":"3 is a peak element and your function should return the index number 2."},{"input":"nums = [1,2,1,3,5,6,4]","output":"5","explanation":"Your function can return either index number 1 where the peak element is 2, or index number 5 where the peak element is 6."}]$$,
  constraints = $$1 <= nums.length <= 1000\n-2^31 <= nums[i] <= 2^31 - 1\nnums[i] != nums[i + 1] for all valid i$$
WHERE slug = 'find-peak-element';

UPDATE problems SET
  description = $$There is an integer array `nums` sorted in ascending order (with distinct values) that has been possibly rotated at an unknown pivot index `k`. Given the array `nums` after the possible rotation and an integer `target`, return the index of `target` if it is in `nums`, or `-1` if it is not in `nums`. You must write an algorithm with `O(log n)` runtime complexity.$$,
  examples    = $$[{"input":"nums = [4,5,6,7,0,1,2], target = 0","output":"4"},{"input":"nums = [4,5,6,7,0,1,2], target = 3","output":"-1"},{"input":"nums = [1], target = 0","output":"-1"}]$$,
  constraints = $$1 <= nums.length <= 5000\n-10^4 <= nums[i] <= 10^4\nAll values of nums are unique\nnums is an ascending array that is possibly rotated\n-10^4 <= target <= 10^4$$
WHERE slug = 'search-rotated-array';

UPDATE problems SET
  description = $$There is an integer array `nums` sorted in non-decreasing order (not necessarily with distinct values) that has been possibly rotated at an unknown pivot. Given `nums` after the possible rotation and an integer `target`, return `true` if `target` is in `nums`, or `false` if it is not in `nums`.$$,
  examples    = $$[{"input":"nums = [2,5,6,0,0,1,2], target = 0","output":"true"},{"input":"nums = [2,5,6,0,0,1,2], target = 3","output":"false"}]$$,
  constraints = $$1 <= nums.length <= 5000\n-10^4 <= nums[i] <= 10^4\nnums is an ascending array that is possibly rotated\n-10^4 <= target <= 10^4$$
WHERE slug = 'search-rotated-array-ii';

UPDATE problems SET
  description = $$Suppose an array of length `n` sorted in ascending order is rotated between `1` and `n` times. Given the sorted rotated array `nums` of unique elements, return the minimum element of this array. You must write an algorithm that runs in `O(log n)` time.$$,
  examples    = $$[{"input":"nums = [3,4,5,1,2]","output":"1","explanation":"The original array was [1,2,3,4,5] rotated 3 times."},{"input":"nums = [4,5,6,7,0,1,2]","output":"0"},{"input":"nums = [11,13,15,17]","output":"11","explanation":"The original array was [11,13,15,17] with no rotation."}]$$,
  constraints = $$n == nums.length\n1 <= n <= 5000\n-5000 <= nums[i] <= 5000\nAll the integers of nums are unique\nnums is sorted and rotated between 1 and n times$$
WHERE slug = 'find-min-rotated-array';

UPDATE problems SET
  description = $$Suppose an array of length `n` sorted in ascending order is rotated between `1` and `n` times. Given the sorted rotated array `nums` that may contain duplicates, return the minimum element of this array. You must decrease the overall operation steps as much as possible.$$,
  examples    = $$[{"input":"nums = [1,3,5]","output":"1"},{"input":"nums = [2,2,2,0,1]","output":"0"}]$$,
  constraints = $$n == nums.length\n1 <= n <= 5000\n-5000 <= nums[i] <= 5000\nnums is sorted and rotated between 1 and n times$$
WHERE slug = 'find-min-rotated-array-ii';

UPDATE problems SET
  description = $$Design a time-based key-value data structure that can store multiple values for the same key at different time stamps and retrieve the key's value at a certain timestamp. Implement the `TimeMap` class with `set(key, value, timestamp)` and `get(key, timestamp)` methods. The `get` method returns the value stored at the largest timestamp less than or equal to `timestamp`.$$,
  examples    = $$[{"input":"[\"TimeMap\",\"set\",\"get\",\"get\",\"set\",\"get\",\"get\"] [[],[\"foo\",\"bar\",1],[\"foo\",1],[\"foo\",3],[\"foo\",\"bar2\",4],[\"foo\",4],[\"foo\",5]]","output":"[null,null,\"bar\",\"bar\",null,\"bar2\",\"bar2\"]","explanation":"TimeMap timeMap = new TimeMap(); timeMap.set(\"foo\", \"bar\", 1); timeMap.get(\"foo\", 1) returns \"bar\"; timeMap.get(\"foo\", 3) returns \"bar\"; timeMap.set(\"foo\", \"bar2\", 4); timeMap.get(\"foo\", 4) returns \"bar2\"; timeMap.get(\"foo\", 5) returns \"bar2\"."},{"input":"[\"TimeMap\",\"set\",\"get\"]\n[[],[\"love\",\"high\",10],[\"love\",5]]","output":"[null,null,\"\"]"}]$$,
  constraints = $$1 <= key.length, value.length <= 100\nkey and value consist of lowercase English letters and digits\n1 <= timestamp <= 10^7\nAll the timestamps of set are strictly increasing\nAt most 2 * 10^5 calls will be made to set and get$$
WHERE slug = 'time-based-key-value-store';

UPDATE problems SET
  description = $$Koko loves to eat bananas. There are `n` piles of bananas, the `i`-th pile has `piles[i]` bananas. The guards have gone and will come back in `h` hours. Koko can decide her bananas-per-hour eating speed `k`. Each hour, she chooses some pile of bananas and eats `k` bananas from that pile. Return the minimum integer `k` such that she can eat all the bananas within `h` hours.$$,
  examples    = $$[{"input":"piles = [3,6,7,11], h = 8","output":"4"},{"input":"piles = [30,11,23,4,20], h = 5","output":"30"},{"input":"piles = [30,11,23,4,20], h = 6","output":"23"}]$$,
  constraints = $$1 <= piles.length <= 10^4\npiles.length <= h <= 10^9\n1 <= piles[i] <= 10^9$$
WHERE slug = 'koko-eating-bananas';

UPDATE problems SET
  description = $$A conveyor belt has packages that must be shipped from one port to another within `days` days. The `i`-th package on the conveyor belt has a weight of `weights[i]`. Each day, we load the ship with packages in the order given by `weights`. We may not load more weight than the maximum weight capacity of the ship. Return the least weight capacity of the ship that will result in all the packages on the conveyor belt being shipped within `days` days.$$,
  examples    = $$[{"input":"weights = [1,2,3,4,5,6,7,8,9,10], days = 5","output":"15","explanation":"A ship capacity of 15 is the minimum to ship all the packages in 5 days."},{"input":"weights = [3,2,2,4,1,4], days = 3","output":"6"}]$$,
  constraints = $$1 <= days <= weights.length <= 5 * 10^4\n1 <= weights[i] <= 500$$
WHERE slug = 'capacity-to-ship-packages';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, split `nums` into `k` non-empty subarrays such that the largest sum of any subarray is minimized. Return the minimized largest sum of the split.$$,
  examples    = $$[{"input":"nums = [7,2,5,10,8], k = 2","output":"18","explanation":"There are four ways to split nums into two subarrays. The best way is to split it into [7,2,5] and [10,8], where the largest sum among the two subarrays is only 18."},{"input":"nums = [1,2,3,4,5], k = 2","output":"9"}]$$,
  constraints = $$1 <= nums.length <= 1000\n0 <= nums[i] <= 10^6\n1 <= k <= min(50, nums.length)$$
WHERE slug = 'split-array-largest-sum';

UPDATE problems SET
  description = $$Given two sorted arrays `nums1` and `nums2` of size `m` and `n` respectively, return the median of the two sorted arrays. The overall run time complexity should be `O(log (m+n))`.$$,
  examples    = $$[{"input":"nums1 = [1,3], nums2 = [2]","output":"2.00000","explanation":"merged array = [1,2,3] and median is 2."},{"input":"nums1 = [1,2], nums2 = [3,4]","output":"2.50000","explanation":"merged array = [1,2,3,4] and median is (2 + 3) / 2 = 2.5."}]$$,
  constraints = $$nums1.length == m\nnums2.length == n\n0 <= m <= 1000\n0 <= n <= 1000\n1 <= m + n <= 2000\n-10^6 <= nums1[i], nums2[i] <= 10^6$$
WHERE slug = 'median-two-sorted-arrays';

UPDATE problems SET
  description = $$Given a sorted integer array `arr`, two integers `k` and `x`, return the `k` closest integers to `x` in the array. The result should also be sorted in ascending order. An integer `a` is closer to `x` than an integer `b` if `|a - x| < |b - x|`, or `|a - x| == |b - x|` and `a < b`.$$,
  examples    = $$[{"input":"arr = [1,2,3,4,5], k = 4, x = 3","output":"[1,2,3,4]"},{"input":"arr = [1,2,3,4,5], k = 4, x = -1","output":"[1,2,3,4]"}]$$,
  constraints = $$1 <= k <= arr.length\n1 <= arr.length <= 10^4\narr is sorted in ascending order\n-10^4 <= arr[i], x <= 10^4$$
WHERE slug = 'find-k-closest-elements';

UPDATE problems SET
  description = $$You are given a sorted array consisting of only integers where every element appears exactly twice, except for one element which appears exactly once. Return the single element that appears only once. Your solution must run in `O(log n)` time and `O(1)` space.$$,
  examples    = $$[{"input":"nums = [1,1,2,3,3,4,4,8,8]","output":"2"},{"input":"nums = [3,1,1]","output":"3"}]$$,
  constraints = $$1 <= nums.length <= 10^5\n0 <= nums[i] <= 10^5$$
WHERE slug = 'single-element-sorted-array';

UPDATE problems SET
  description = $$You are given two positive integer arrays `spells` and `potions`, of length `n` and `m` respectively, where `spells[i]` represents the strength of the `i`-th spell and `potions[j]` represents the strength of the `j`-th potion. You are also given an integer `success`. A spell and potion pair is considered successful if the product of their strengths is at least `success`. Return an integer array `pairs` of length `n` where `pairs[i]` is the number of potions that will form a successful pair with the `i`-th spell.$$,
  examples    = $$[{"input":"spells = [5,1,3], potions = [1,2,3,4,5], success = 7","output":"[4,0,3]","explanation":"- 0th spell: 5 * [1,2,3,4,5] = [5,10,15,20,25]. 4 pairs are successful."},{"input":"spells = [3,1,2], potions = [8,5,8], success = 16","output":"[2,0,2]"}]$$,
  constraints = $$n == spells.length\nm == potions.length\n1 <= n, m <= 10^5\n1 <= spells[i], potions[i] <= 10^5\n1 <= success <= 10^10$$
WHERE slug = 'successful-pairs';

UPDATE problems SET
  description = $$You are given a floating-point number `hour`, representing the amount of time you have to reach the office. To commute to the office, you must take `n` trains in order. You are also given an integer array `dist` of length `n`, where `dist[i]` describes the distance (in kilometers) of the `i`-th train ride. Return the minimum positive integer speed (in kilometers per hour) that all the trains must travel at for you to reach the office on time, or `-1` if it is impossible to be on time.$$,
  examples    = $$[{"input":"dist = [1,3,2], hour = 6","output":"1","explanation":"At speed 1: The first train ride takes 1/1 = 1 hour. The second train ride takes 3/1 = 3 hours. The third train ride takes 2/1 = 2 hours. Total time = 6 hours."},{"input":"dist = [1,3,2], hour = 2.7","output":"3"}]$$,
  constraints = $$n == dist.length\n1 <= n <= 10^5\n1 <= dist[i] <= 10^5\n1 <= hour <= 10^7\nThere will be at most two digits after the decimal point in hour$$
WHERE slug = 'minimum-speed-arrive-time';

UPDATE problems SET
  description = $$You are given an integer array `position` and an integer `m`. The integer `position[i]` gives the position of the `i`-th ball on a number line. Given `m` balls, you want to place them into `position` such that the minimum magnetic force between any two balls is maximized. Return this maximum minimum magnetic force.$$,
  examples    = $$[{"input":"position = [1,2,3,4,7], m = 3","output":"3","explanation":"Placing the 3 balls at positions 1, 4, and 7 yields a minimum magnetic force of 3."},{"input":"position = [5,4,3,2,1,1000000000], m = 2","output":"999999999"}]$$,
  constraints = $$2 <= n <= 10^5\n1 <= position[i] <= 10^9\nAll integers in position are distinct\n2 <= m <= position.length$$
WHERE slug = 'magnetic-force';

UPDATE problems SET
  description = $$You are given an integer array `bloomDay`, an integer `m` and an integer `k`. You want to make `m` bouquets. To make a bouquet, you need to use `k` adjacent flowers from the garden. The garden consists of `n` flowers, the `i`-th flower will bloom in the `bloomDay[i]` and then can be used in exactly one bouquet. Return the minimum number of days you need to wait to be able to make `m` bouquets from the garden. If it is impossible, return `-1`.$$,
  examples    = $$[{"input":"bloomDay = [1,10,3,10,2], m = 3, k = 1","output":"3","explanation":"After 3 days, bloomDay are [bloom, not-bloom, bloom, not-bloom, bloom]. 3 bouquets can be made."},{"input":"bloomDay = [1,10,3,10,2], m = 3, k = 2","output":"-1"}]$$,
  constraints = $$bloomDay.length == n\n1 <= n <= 10^5\n1 <= bloomDay[i] <= 10^9\n1 <= m <= 10^6\n1 <= k <= n$$
WHERE slug = 'minimum-days-bouquets';

UPDATE problems SET
  description = $$You are given `n` packages on a conveyor belt represented by integer array `weights`. A ship must transport all packages in the order given and return within `d` days. Return the minimum weight capacity of the ship. This is a variation of the capacity-to-ship problem focusing on the binary search on answer technique.$$,
  examples    = $$[{"input":"weights = [1,2,3,1,1], days = 4","output":"3","explanation":"Split [1,2], [3], [1], [1] — max is 3."},{"input":"weights = [1,2,3,4,5,6,7,8,9,10], days = 5","output":"15"}]$$,
  constraints = $$1 <= days <= weights.length <= 5 * 10^4\n1 <= weights[i] <= 500$$
WHERE slug = 'ship-packages';

UPDATE problems SET
  description = $$You are given an integer array `arr` that is guaranteed to be a mountain. A mountain array is defined as an array where the values increase to a peak and then decrease. Return the index of the peak element. The peak is an element strictly greater than its neighbors.$$,
  examples    = $$[{"input":"arr = [0,1,0]","output":"1"},{"input":"arr = [0,2,1,0]","output":"1"},{"input":"arr = [0,10,5,2]","output":"1"}]$$,
  constraints = $$3 <= arr.length <= 10^4\n0 <= arr[i] <= 10^6\narr is guaranteed to be a mountain array$$
WHERE slug = 'peak-index-mountain';

UPDATE problems SET
  description = $$Given two integer arrays `nums1` and `nums2`, return an array of their intersection. Each element in the result must appear as many times as it shows in both arrays, and you may return the result in any order. Use binary search for an efficient solution.$$,
  examples    = $$[{"input":"nums1 = [1,2,2,1], nums2 = [2,2]","output":"[2,2]"},{"input":"nums1 = [4,9,5], nums2 = [9,4,9,8,4]","output":"[4,9]","explanation":"[9,4] is also accepted."}]$$,
  constraints = $$1 <= nums1.length, nums2.length <= 1000\n0 <= nums1[i], nums2[i] <= 1000$$
WHERE slug = 'intersection-arrays';

UPDATE problems SET
  description = $$You have `n` coins and you want to build a staircase with these coins. The staircase consists of `k` rows where the `i`-th row has exactly `i` coins. The last row of the staircase may be incomplete. Given the integer `n`, return the number of complete rows of the staircase you will build.$$,
  examples    = $$[{"input":"n = 5","output":"2","explanation":"Because the 3rd row is incomplete, we return 2."},{"input":"n = 8","output":"3","explanation":"Because the 4th row is incomplete, we return 3."}]$$,
  constraints = $$1 <= n <= 2^31 - 1$$
WHERE slug = 'arranging-coins';
