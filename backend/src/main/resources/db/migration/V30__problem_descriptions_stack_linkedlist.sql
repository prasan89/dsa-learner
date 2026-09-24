-- V28c: Real descriptions for stack and linked-list problems

UPDATE problems SET
  description = $$Design a stack that supports push, pop, top, and retrieving the minimum element in constant time. Implement the `MinStack` class with operations `push(val)`, `pop()`, `top()`, and `getMin()`. All operations must run in O(1) time.$$,
  examples    = $$[{"input":"[\"MinStack\",\"push\",\"push\",\"push\",\"getMin\",\"pop\",\"top\",\"getMin\"] [[],[−2],[0],[−3],[],[],[],[]]","output":"[null,null,null,null,−3,null,0,−2]","explanation":"After pushing −2, 0, −3, the minimum is −3. After popping −3, the top is 0 and the new minimum is −2."},{"input":"[\"MinStack\",\"push\",\"push\",\"getMin\",\"pop\",\"getMin\"] [[],[0],[1],[],[],[]]","output":"[null,null,null,0,null,0]","explanation":"Pushing 0 then 1; minimum is 0. After popping 1, minimum is still 0."}]$$,
  constraints = $$-2^31 <= val <= 2^31 - 1
Methods pop, top and getMin operations will always be called on non-empty stacks
At most 3 * 10^4 calls will be made to push, pop, top, and getMin$$
WHERE slug = 'min-stack';

UPDATE problems SET
  description = $$Evaluate the value of an arithmetic expression in Reverse Polish Notation (postfix notation). Valid operators are `+`, `-`, `*`, and `/`. Each operand may be an integer or another expression. Division truncates toward zero.$$,
  examples    = $$[{"input":"tokens = [\"2\",\"1\",\"+\",\"3\",\"*\"]","output":"9","explanation":"((2 + 1) * 3) = 9"},{"input":"tokens = [\"4\",\"13\",\"5\",\"/\",\"+\"]","output":"6","explanation":"(4 + (13 / 5)) = 6"},{"input":"tokens = [\"10\",\"6\",\"9\",\"3\",\"+\",\"-11\",\"*\",\"/\",\"*\",\"17\",\"+\",\"5\",\"+\"]","output":"22","explanation":"((10 * (6 / ((9 + 3) * -11))) + 17) + 5 = 22"}]$$,
  constraints = $$1 <= tokens.length <= 10^4
tokens[i] is either an operator (+, -, *, /) or an integer in the range [-200, 200]
The input is a valid Reverse Polish Notation expression
The answer and all intermediate calculations fit in a 32-bit integer$$
WHERE slug = 'evaluate-reverse-polish';

UPDATE problems SET
  description = $$Given an array of integers `temperatures` representing daily temperatures, return an array `answer` such that `answer[i]` is the number of days you have to wait after the `i`-th day to get a warmer temperature. If there is no future day with a warmer temperature, set `answer[i]` to `0`.$$,
  examples    = $$[{"input":"temperatures = [73,74,75,71,69,72,76,73]","output":"[1,1,4,2,1,1,0,0]","explanation":"After day 0 (73°), day 1 (74°) is warmer — wait 1 day. After day 2 (75°), the next warmer day is day 6 (76°) — wait 4 days."},{"input":"temperatures = [30,40,50,60]","output":"[1,1,1,0]","explanation":"Each day is followed by a warmer day except the last."},{"input":"temperatures = [30,60,90]","output":"[1,1,0]","explanation":"Day 0 waits 1 day, day 1 waits 1 day, day 2 has no warmer future day."}]$$,
  constraints = $$1 <= temperatures.length <= 10^5
30 <= temperatures[i] <= 100$$
WHERE slug = 'daily-temperatures';

UPDATE problems SET
  description = $$You are given two arrays `nums1` and `nums2` where `nums1` is a subset of `nums2`. For each element in `nums1`, find its next greater element in `nums2`. The next greater element of `x` in `nums2` is the first element to the right of `x` in `nums2` that is greater than `x`. If no such element exists, return `-1`.$$,
  examples    = $$[{"input":"nums1 = [4,1,2], nums2 = [1,3,4,2]","output":"[-1,3,-1]","explanation":"For 4, there is no greater element to its right in nums2. For 1, the next greater element is 3. For 2, there is no greater element to its right."},{"input":"nums1 = [2,4], nums2 = [1,2,3,4]","output":"[3,-1]","explanation":"For 2, the next greater element is 3. For 4, there is no greater element."}]$$,
  constraints = $$1 <= nums1.length <= nums2.length <= 1000
0 <= nums1[i], nums2[i] <= 10^4
All integers in nums1 and nums2 are unique
All the integers of nums1 also appear in nums2$$
WHERE slug = 'next-greater-element-i';

UPDATE problems SET
  description = $$Given a circular integer array `nums`, return the next greater number for every element in `nums`. The next greater number of a number `x` is the first greater number to its traversing-order next in the array, which means you could search circularly to find its next greater number. If it doesn't exist, return `-1` for this number.$$,
  examples    = $$[{"input":"nums = [1,2,1]","output":"[2,-1,2]","explanation":"For index 0 (1), the next greater is 2. For index 1 (2), no greater exists so -1. For index 2 (1), wrapping around gives 2."},{"input":"nums = [1,2,3,4,3]","output":"[2,3,4,-1,4]","explanation":"For 4 at index 3, no greater element exists in a full circular pass so -1. For 3 at index 4, wrapping around gives 4."}]$$,
  constraints = $$1 <= nums.length <= 10^4
-10^9 <= nums[i] <= 10^9$$
WHERE slug = 'next-greater-element-ii';

UPDATE problems SET
  description = $$Given an array of integers `heights` representing the histogram bar heights where the width of each bar is 1, return the area of the largest rectangle in the histogram.$$,
  examples    = $$[{"input":"heights = [2,1,5,6,2,3]","output":"10","explanation":"The largest rectangle spans bars at indices 2 and 3 (heights 5 and 6), giving area 5*2=10."},{"input":"heights = [2,4]","output":"4","explanation":"The largest rectangle is a single bar of height 4, giving area 4."},{"input":"heights = [1,1]","output":"2","explanation":"A rectangle spanning both bars of height 1 gives area 2."}]$$,
  constraints = $$1 <= heights.length <= 10^5
0 <= heights[i] <= 10^4$$
WHERE slug = 'largest-rectangle-histogram';

UPDATE problems SET
  description = $$There are `n` cars at given miles away from the starting point going to the same destination. Each car `i` has a position `position[i]` and speed `speed[i]`. A car fleet is a non-empty set of cars driving at the same position and speed. A car cannot pass another car, but it can catch up and then travel at the slower car's speed. Return the number of car fleets that will arrive at the destination.$$,
  examples    = $$[{"input":"target = 12, position = [10,8,0,5,3], speed = [2,4,1,1,3]","output":"3","explanation":"Cars at positions 10 and 8 form one fleet. Car at position 0 is a fleet by itself. Cars at positions 5 and 3 form a fleet, making 3 total fleets."},{"input":"target = 10, position = [3], speed = [3]","output":"1","explanation":"There is only one car, so there is one fleet."},{"input":"target = 100, position = [0,2,4], speed = [4,2,1]","output":"1","explanation":"All cars eventually merge into one fleet."}]$$,
  constraints = $$n == position.length == speed.length
1 <= n <= 10^5
0 < target <= 10^6
0 <= position[i] < target
All positions are unique
0 < speed[i] <= 10^6$$
WHERE slug = 'car-fleet';

UPDATE problems SET
  description = $$Given an encoded string `s`, return its decoded string. The encoding rule is `k[encoded_string]`, where the `encoded_string` inside the brackets is repeated exactly `k` times. Note that `k` is guaranteed to be a positive integer and the input is always valid (no extra white spaces, square brackets are well-formed, etc.).$$,
  examples    = $$[{"input":"s = \"3[a]2[bc]\"","output":"\"aaabcbc\"","explanation":"3[a] decodes to aaa and 2[bc] decodes to bcbc, giving aaabcbc."},{"input":"s = \"3[a2[c]]\"","output":"\"accaccacc\"","explanation":"Inner 2[c] becomes cc, so 3[acc] becomes accaccacc."},{"input":"s = \"2[abc]3[cd]ef\"","output":"\"abcabccdcdcdef\"","explanation":"2[abc] gives abcabc, 3[cd] gives cdcdcd, concatenated with ef."}]$$,
  constraints = $$1 <= s.length <= 30
s consists of lowercase English letters, digits, and square brackets '[]'
s is guaranteed to be a valid input
All the integers in s are in the range [1, 300]$$
WHERE slug = 'decode-string';

UPDATE problems SET
  description = $$Given a string `num` representing a non-negative integer and an integer `k`, return the smallest possible integer after removing `k` digits from `num`. The result should not have leading zeros, and if the result is empty return `"0"`.$$,
  examples    = $$[{"input":"num = \"1432219\", k = 3","output":"\"1219\"","explanation":"Removing digits 4, 3, and 2 leaves 1219 which is the smallest possible result."},{"input":"num = \"10200\", k = 1","output":"\"200\"","explanation":"Removing the leading 1 gives 0200, which is 200 after stripping the leading zero."},{"input":"num = \"10\", k = 2","output":"\"0\"","explanation":"Removing both digits leaves an empty string, so we return 0."}]$$,
  constraints = $$1 <= k <= num.length <= 10^5
num consists of only digits
num does not have any leading zeros except for the zero itself$$
WHERE slug = 'remove-k-digits';

UPDATE problems SET
  description = $$You are given an array `asteroids` of integers representing asteroids in a row. For each asteroid, the absolute value represents its size and the sign represents its direction (positive means right, negative means left). Find out the state of the asteroids after all collisions. Two asteroids moving in opposite directions collide; the smaller one explodes. If equal, both explode. Asteroids moving in the same direction never meet.$$,
  examples    = $$[{"input":"asteroids = [5,10,-5]","output":"[5,10]","explanation":"10 and -5 collide; 10 survives. 5 and 10 never collide."},{"input":"asteroids = [8,-8]","output":"[]","explanation":"8 and -8 collide and both explode."},{"input":"asteroids = [10,2,-5]","output":"[10]","explanation":"2 and -5 collide; -5 wins. Then 10 and -5 collide; 10 wins."}]$$,
  constraints = $$2 <= asteroids.length <= 10^4
-1000 <= asteroids[i] <= 1000
asteroids[i] != 0$$
WHERE slug = 'asteroid-collision';

UPDATE problems SET
  description = $$Given a string `path` representing an absolute path for a Unix-style file system (starting with a slash `/`), convert it to the simplified canonical path. The canonical path must start with a single slash, have no trailing slash, and not contain double slashes or dot-directory segments.$$,
  examples    = $$[{"input":"path = \"/home/\"","output":"\"/home\"","explanation":"Trailing slash removed to give /home."},{"input":"path = \"/home//foo/\"","output":"\"/home/foo\"","explanation":"Double slash becomes a single slash."},{"input":"path = \"/home/user/Documents/../Pictures\"","output":"\"/home/user/Pictures\"","explanation":".. means go up one directory level, so Documents/.. cancels out."}]$$,
  constraints = $$1 <= path.length <= 3000
path consists of English letters, digits, period '.', slash '/' or '_'
path is a valid absolute Unix path$$
WHERE slug = 'simplify-path';

UPDATE problems SET
  description = $$Given a balanced parentheses string `s`, compute the score of the string. The score is calculated as follows: `()` has score 1, `AB` has score `A + B` where `A` and `B` are balanced parentheses strings, and `(A)` has score `2 * A` where `A` is a balanced parentheses string.$$,
  examples    = $$[{"input":"s = \"()\"","output":"1","explanation":"A single () has score 1."},{"input":"s = \"(())\"","output":"2","explanation":"(A) = 2 * score(A) = 2 * 1 = 2."},{"input":"s = \"()()\"","output":"2","explanation":"AB = score(()) + score(()) = 1 + 1 = 2."},{"input":"s = \"(()(()))\"","output":"6","explanation":"(()(())) = 2 * (score(()) + score((()))) = 2 * (1 + 2) = 6."}]$$,
  constraints = $$2 <= s.length <= 50
s consists of only '(' and ')'
s is a balanced parentheses string$$
WHERE slug = 'score-parentheses';

UPDATE problems SET
  description = $$Design an algorithm that collects daily price quotes for some asset and returns the span of that asset's price for the current day. The span of the stock's price today is defined as the maximum number of consecutive days (starting from today and going backward) for which the stock price was less than or equal to today's price.$$,
  examples    = $$[{"input":"[\"StockSpanner\",\"next\",\"next\",\"next\",\"next\",\"next\",\"next\",\"next\"] [[],[100],[80],[60],[70],[60],[75],[85]]","output":"[null,1,1,1,2,1,4,6]","explanation":"On day 5 with price 75, prices for the 4 previous consecutive days were all ≤ 75, so span is 4. On day 6 with price 85, all 6 previous days had prices ≤ 85."},{"input":"[\"StockSpanner\",\"next\",\"next\",\"next\"] [[],[50],[50],[50]]","output":"[null,1,2,3]","explanation":"Each day has the same price 50, so span grows by 1 each day."}]$$,
  constraints = $$1 <= price <= 10^5
At most 10^4 calls will be made to next$$
WHERE slug = 'online-stock-span';

UPDATE problems SET
  description = $$Given an array of `n` integers `nums`, a 132 pattern is a subsequence of three integers `nums[i]`, `nums[j]`, `nums[k]` such that `i < j < k` and `nums[i] < nums[k] < nums[j]`. Return `true` if there is a 132 pattern in `nums`, otherwise return `false`.$$,
  examples    = $$[{"input":"nums = [1,2,3,4]","output":"false","explanation":"No 132 pattern exists in a strictly increasing sequence."},{"input":"nums = [3,1,4,2]","output":"true","explanation":"nums[1]=1, nums[2]=4, nums[3]=2 form the pattern 1 < 2 < 4 (132 pattern)."},{"input":"nums = [-1,3,2,0]","output":"true","explanation":"nums[0]=-1, nums[1]=3, nums[3]=0 form the 132 pattern."}]$$,
  constraints = $$n == nums.length
1 <= n <= 2 * 10^5
-10^9 <= nums[i] <= 10^9$$
WHERE slug = '132-pattern';

UPDATE problems SET
  description = $$Implement a basic calculator to evaluate a simple expression string `s` containing non-negative integers, `+`, `-` operators, and parentheses. The string may contain spaces. Return the result of the evaluation.$$,
  examples    = $$[{"input":"s = \"1 + 1\"","output":"2","explanation":"Simple addition gives 2."},{"input":"s = \" 2-1 + 2 \"","output":"3","explanation":"2 - 1 + 2 = 3 (spaces are ignored)."},{"input":"s = \"(1+(4+5+2)-3)+(6+8)\"","output":"23","explanation":"Evaluating nested parentheses: (1+11-3)+(14) = 9+14 = 23."}]$$,
  constraints = $$1 <= s.length <= 3 * 10^5
s consists of digits, '+', '-', '(', ')', and ' '
s represents a valid expression
'+' is not used as a unary operator
'-' could be used as a unary operator (e.g., "-1" and "-(2 + 3)")
There will be no two consecutive operators in the input
Every number and running calculation will fit in a signed 32-bit integer$$
WHERE slug = 'basic-calculator';

UPDATE problems SET
  description = $$Implement a basic calculator to evaluate a simple expression string `s` containing non-negative integers and operators `+`, `-`, `*`, `/`. Note that integer division should truncate toward zero, operator precedence follows the standard rules, and there are no parentheses in the input.$$,
  examples    = $$[{"input":"s = \"3+2*2\"","output":"7","explanation":"Multiplication is done first: 3 + (2*2) = 3 + 4 = 7."},{"input":"s = \" 3/2 \"","output":"1","explanation":"3/2 truncates to 1."},{"input":"s = \" 3+5 / 2 \"","output":"5","explanation":"5/2 = 2, then 3+2 = 5."}]$$,
  constraints = $$1 <= s.length <= 3 * 10^5
s consists of integers and operators ('+', '-', '*', '/') separated by some number of spaces
s represents a valid expression
All the integers in the expression are non-negative and will fit in a 32-bit integer
The answer is guaranteed to fit in a 32-bit integer$$
WHERE slug = 'basic-calculator-ii';

UPDATE problems SET
  description = $$Given two sequences `pushed` and `popped` representing the order of push and pop operations on a stack, return `true` if this could have been the result of a sequence of push and pop operations on an initially empty stack, or `false` otherwise.$$,
  examples    = $$[{"input":"pushed = [1,2,3,4,5], popped = [4,5,3,2,1]","output":"true","explanation":"Push 1,2,3,4, pop 4. Push 5, pop 5. Pop 3, pop 2, pop 1."},{"input":"pushed = [1,2,3,4,5], popped = [4,3,5,1,2]","output":"false","explanation":"After popping 4 and 3, the next pop should be 2 (top of stack) but the sequence asks for 5 first."}]$$,
  constraints = $$1 <= pushed.length <= 1000
0 <= pushed[i] <= 1000
All the elements of pushed are unique
popped.length == pushed.length
popped is a permutation of pushed$$
WHERE slug = 'validate-stack-sequences';

UPDATE problems SET
  description = $$Given a valid parentheses string `s`, remove the outermost parentheses of every primitive string in the primitive decomposition of `s`. A primitive string is a non-empty parentheses string that cannot be split into two non-empty parentheses strings.$$,
  examples    = $$[{"input":"s = \"(()())(())\"","output":"\"()()()\"","explanation":"The input has two primitive strings: (()()) and (()). Removing the outer parentheses of each gives ()() and (), concatenated as ()()()."},{"input":"s = \"(()())(())(((\"","output":"\"()()()\"","explanation":"Wait, (((  is invalid. For valid input (()())(())(()()):  primitives are (()()) -> ()(), (()) -> (), (()()) -> ()(). Result is ()()()()."},{"input":"s = \"()()\"","output":"\"\"","explanation":"Each () is a primitive; removing outer parentheses of each leaves empty strings, so the result is empty."}]$$,
  constraints = $$1 <= s.length <= 10^5
s[i] is either '(' or ')'
s is a valid parentheses string$$
WHERE slug = 'remove-outermost-parentheses';

UPDATE problems SET
  description = $$Given two strings `s` and `t`, return `true` if they are equal when both are typed into empty text editors. A `#` character means a backspace. Note that after backspacing an empty text, the text will remain empty.$$,
  examples    = $$[{"input":"s = \"ab#c\", t = \"ad#c\"","output":"true","explanation":"s becomes \"ac\" (b is deleted by #). t becomes \"ac\" (d is deleted by #). They are equal."},{"input":"s = \"ab##\", t = \"c#d#\"","output":"true","explanation":"s becomes \"\" (both chars deleted). t becomes \"\" (both chars deleted). Empty strings are equal."},{"input":"s = \"a#c\", t = \"b\"","output":"false","explanation":"s becomes \"c\". t is \"b\". They are not equal."}]$$,
  constraints = $$1 <= s.length, t.length <= 200
s and t only contain lowercase letters and '#' characters$$
WHERE slug = 'backspace-stack';

UPDATE problems SET
  description = $$Given a string `s` of lower and upper case English letters, make the string good by repeatedly removing any two adjacent characters where one is the lowercase version of the other (e.g., `aA` or `Aa`). Return the resulting string after making it good.$$,
  examples    = $$[{"input":"s = \"leEeetcode\"","output":"\"leetcode\"","explanation":"In the first step, either 'E' and 'e' are removed. Then the remaining string is \"leetcode\" which is good."},{"input":"s = \"abBAcC\"","output":"\"\"","explanation":"abBAcC -> aAcC -> cC -> \"\". All pairs cancel."},{"input":"s = \"s\"","output":"\"s\"","explanation":"A single character string is always good."}]$$,
  constraints = $$1 <= s.length <= 100
s contains only lower and upper case English letters$$
WHERE slug = 'make-good-string';

UPDATE problems SET
  description = $$You are keeping score for a baseball game with unusual rules. You are given a list of strings `ops` where each string is one of: an integer (a score for this round), `+` (sum of the previous two scores), `D` (double the previous score), or `C` (invalidate the previous score). Return the sum of all scores after all operations.$$,
  examples    = $$[{"input":"ops = [\"5\",\"2\",\"C\",\"D\",\"+\"]","output":"30","explanation":"Round 1: 5. Round 2: 2. C: remove 2, record=[5]. D: double 5=10, record=[5,10]. +: 5+10=15, record=[5,10,15]. Sum=30."},{"input":"ops = [\"5\",\"-2\",\"4\",\"C\",\"D\",\"9\",\"+\",\"+\"]","output":"27","explanation":"After all operations, valid scores are 5, -4, 9, 5, 14. Sum = 27... (wait, let me recalculate). Record: 5→[5], -2→[5,-2], 4→[5,-2,4], C→[5,-2], D→[5,-2,-4], 9→[5,-2,-4,9], +→[5,-2,-4,9,5], +→[5,-2,-4,9,5,14]. Sum = 5-2-4+9+5+14 = 27."}]$$,
  constraints = $$1 <= ops.length <= 1000
ops[i] is "C", "D", "+", or a string representing an integer in the range [-3 * 10^4, 3 * 10^4]
For operation "+", there will always be at least two previous scores on the record
For operation "D", there will always be at least one previous score on the record
For operation "C", there will always be at least one previous score on the record$$
WHERE slug = 'baseball-game';

UPDATE problems SET
  description = $$Given the head of a linked list, return a list of the nodes' values just before each node that has a value greater than the node itself. In other words, for each node, find the next node with a strictly greater value. If no such node exists, output 0 for that position.$$,
  examples    = $$[{"input":"head = [2,7,4,3,5]","output":"[7,0,5,5,0]","explanation":"2's next greater is 7. 7 has no greater node so 0. 4's next greater is 5. 3's next greater is 5. 5 has no greater node so 0."},{"input":"head = [1,7,5,1,9,2,5,1]","output":"[7,9,9,9,0,5,0,0]","explanation":"Each node's next greater value in the list."}]$$,
  constraints = $$The number of nodes in the list is n
1 <= n <= 10^4
1 <= Node.val <= 10^9$$
WHERE slug = 'next-greater-node';

UPDATE problems SET
  description = $$Design a stack-like data structure that supports push, pop, and returning the most frequent element. Implement `FreqStack` with operations `push(val)` which pushes an integer `val` onto the top, and `pop()` which removes and returns the most frequent element. If there is a tie for most frequent, the element closest to the top of the stack wins.$$,
  examples    = $$[{"input":"[\"FreqStack\",\"push\",\"push\",\"push\",\"push\",\"push\",\"push\",\"pop\",\"pop\",\"pop\",\"pop\"] [[],[5],[7],[5],[7],[4],[5],[],[],[],[]]","output":"[null,null,null,null,null,null,null,5,7,5,4]","explanation":"Push 5,7,5,7,4,5. Frequencies: 5→3, 7→2, 4→1. pop() returns 5 (freq 3). pop() returns 7 (tied freq 2, more recent). pop() returns 5 (freq 2). pop() returns 4."},{"input":"[\"FreqStack\",\"push\",\"push\",\"push\",\"pop\"] [[],[1],[1],[2],[]]","output":"[null,null,null,null,1]","explanation":"1 has frequency 2, 2 has frequency 1. pop() returns 1."}]$$,
  constraints = $$0 <= val <= 10^9
At most 2 * 10^4 calls will be made to push and pop
It is guaranteed that there will be at least one element in the stack before calling pop$$
WHERE slug = 'maximum-frequency-stack';

UPDATE problems SET
  description = $$Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining. Use a stack-based approach to process the bars and accumulate trapped water.$$,
  examples    = $$[{"input":"height = [0,1,0,2,1,0,1,3,2,1,2,1]","output":"6","explanation":"The elevation map traps 6 units of water in the valleys between the bars."},{"input":"height = [4,2,0,3,2,5]","output":"9","explanation":"Between heights 4 and 5, valleys trap a total of 9 units of water."}]$$,
  constraints = $$n == height.length
1 <= n <= 2 * 10^4
0 <= height[i] <= 10^5$$
WHERE slug = 'trapping-rain-stack';

UPDATE problems SET
  description = $$Given the `head` of a singly linked list, return the middle node. If there are two middle nodes, return the second middle node.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5]","output":"[3,4,5]","explanation":"The middle node of the list has value 3. The returned node is 3 with the rest of the list."},{"input":"head = [1,2,3,4,5,6]","output":"[4,5,6]","explanation":"Since the list has two middles (3 and 4), we return the second middle node (4)."}]$$,
  constraints = $$The number of nodes in the list is in the range [1, 100]
1 <= Node.val <= 100$$
WHERE slug = 'middle-linked-list';

UPDATE problems SET
  description = $$Given `head`, the head of a linked list, determine if the linked list has a cycle in it. A cycle exists if some node can be reached again by continuously following the `next` pointer. Return `true` if there is a cycle, or `false` otherwise.$$,
  examples    = $$[{"input":"head = [3,2,0,-4], pos = 1","output":"true","explanation":"There is a cycle in the linked list, where the tail connects to the node at index 1 (0-indexed)."},{"input":"head = [1,2], pos = 0","output":"true","explanation":"The tail connects to the node at index 0."},{"input":"head = [1], pos = -1","output":"false","explanation":"There is no cycle in the linked list."}]$$,
  constraints = $$The number of the nodes in the list is in the range [0, 10^4]
-10^5 <= Node.val <= 10^5
pos is -1 or a valid index in the linked list
Do not modify the linked list$$
WHERE slug = 'linked-list-cycle';

UPDATE problems SET
  description = $$Given the `head` of a linked list that may contain a cycle, return the node where the cycle begins. If there is no cycle, return `null`. Do not modify the linked list.$$,
  examples    = $$[{"input":"head = [3,2,0,-4], pos = 1","output":"tail connects to node index 1","explanation":"The cycle starts at the node with value 2 (index 1)."},{"input":"head = [1,2], pos = 0","output":"tail connects to node index 0","explanation":"The cycle starts at the node with value 1."},{"input":"head = [1], pos = -1","output":"no cycle","explanation":"There is no cycle in the linked list so return null."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 10^4]
-10^5 <= Node.val <= 10^5
pos is -1 or a valid index in the linked-list$$
WHERE slug = 'linked-list-cycle-ii';

UPDATE problems SET
  description = $$You are given the heads of two sorted linked lists `list1` and `list2`. Merge the two lists into one sorted linked list by splicing together the nodes of the first two lists. Return the head of the merged linked list.$$,
  examples    = $$[{"input":"list1 = [1,2,4], list2 = [1,3,4]","output":"[1,1,2,3,4,4]","explanation":"Merge by comparing nodes: 1<=1, take list1's 1; then list2's 1; then 2<3; then 3<4; then list1's 4; then list2's 4."},{"input":"list1 = [], list2 = []","output":"[]","explanation":"Both empty lists merge to an empty list."},{"input":"list1 = [], list2 = [0]","output":"[0]","explanation":"An empty list merged with [0] gives [0]."}]$$,
  constraints = $$The number of nodes in both lists is in the range [0, 50]
-100 <= Node.val <= 100
Both list1 and list2 are sorted in non-decreasing order$$
WHERE slug = 'merge-two-sorted-lists';

UPDATE problems SET
  description = $$Given the `head` of a linked list, remove the `n`-th node from the end of the list and return its head.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5], n = 2","output":"[1,2,3,5]","explanation":"The 2nd node from the end is node with value 4, so it is removed."},{"input":"head = [1], n = 1","output":"[]","explanation":"The only node is also the 1st from the end; removing it gives an empty list."},{"input":"head = [1,2], n = 1","output":"[1]","explanation":"The 1st node from the end is 2; removing it leaves [1]."}]$$,
  constraints = $$The number of nodes in the list is sz
1 <= sz <= 30
0 <= Node.val <= 100
1 <= n <= sz$$
WHERE slug = 'remove-nth-node';

UPDATE problems SET
  description = $$Given the `head` of a singly linked list, return `true` if it is a palindrome or `false` otherwise. A palindrome reads the same forwards and backwards.$$,
  examples    = $$[{"input":"head = [1,2,2,1]","output":"true","explanation":"The list reads 1,2,2,1 forward and backward — it is a palindrome."},{"input":"head = [1,2]","output":"false","explanation":"The list reads 1,2 forward but 2,1 backward — not a palindrome."}]$$,
  constraints = $$The number of nodes in the list is in the range [1, 10^5]
0 <= Node.val <= 9
Follow up: Could you do it in O(n) time and O(1) space?$$
WHERE slug = 'palindrome-linked-list';

UPDATE problems SET
  description = $$Given the heads of two singly linked lists `headA` and `headB`, return the node at which the two lists intersect. If the two linked lists have no intersection at all, return `null`. The intersection is defined based on reference, not value.$$,
  examples    = $$[{"input":"intersectVal = 8, listA = [4,1,8,4,5], listB = [5,6,1,8,4,5], skipA = 2, skipB = 3","output":"Intersected at node with val = 8","explanation":"Lists intersect at the node with value 8. listA has 2 nodes before intersection; listB has 3."},{"input":"intersectVal = 2, listA = [1,9,1,2,4], listB = [3,2,4], skipA = 3, skipB = 1","output":"Intersected at node with val = 2","explanation":"The intersection node has value 2."},{"input":"intersectVal = 0, listA = [2,6,4], listB = [1,5], skipA = 3, skipB = 2","output":"No intersection","explanation":"The two lists do not intersect."}]$$,
  constraints = $$The number of nodes of listA is m and listB is n
1 <= m, n <= 3 * 10^4
1 <= Node.val <= 10^5
0 <= skipA < m
0 <= skipB < n
Either intersectVal == 0 or listA[skipA] == listB[skipB]
Do not modify the linked list$$
WHERE slug = 'intersection-linked-lists';

UPDATE problems SET
  description = $$You are given two non-empty linked lists `l1` and `l2` representing two non-negative integers. The digits are stored in reverse order, and each node contains a single digit. Add the two numbers and return the sum as a linked list in reverse order.$$,
  examples    = $$[{"input":"l1 = [2,4,3], l2 = [5,6,4]","output":"[7,0,8]","explanation":"342 + 465 = 807, stored in reverse as [7,0,8]."},{"input":"l1 = [0], l2 = [0]","output":"[0]","explanation":"0 + 0 = 0."},{"input":"l1 = [9,9,9,9,9,9,9], l2 = [9,9,9,9]","output":"[8,9,9,9,0,0,0,1]","explanation":"9999999 + 9999 = 10009998, stored in reverse."}]$$,
  constraints = $$The number of nodes in each linked list is in the range [1, 100]
0 <= Node.val <= 9
It is guaranteed that the list represents a number that does not have leading zeros$$
WHERE slug = 'add-two-numbers';

UPDATE problems SET
  description = $$Given the `head` of a linked list, swap every two adjacent nodes and return its head. You must solve the problem without modifying the values in the list's nodes (i.e., only node swaps allowed).$$,
  examples    = $$[{"input":"head = [1,2,3,4]","output":"[2,1,4,3]","explanation":"Nodes 1 and 2 are swapped, then nodes 3 and 4 are swapped."},{"input":"head = []","output":"[]","explanation":"An empty list remains empty."},{"input":"head = [1]","output":"[1]","explanation":"A single node has no pair to swap with, so it stays."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 100]
0 <= Node.val <= 100$$
WHERE slug = 'swap-nodes-pairs';

UPDATE problems SET
  description = $$Given the `head` of a singly linked list and two integers `left` and `right` where `left <= right`, reverse the nodes of the list from position `left` to position `right`, and return the reversed list.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5], left = 2, right = 4","output":"[1,4,3,2,5]","explanation":"The sublist from position 2 to 4 (1-indexed) is reversed: [2,3,4] becomes [4,3,2]."},{"input":"head = [5], left = 1, right = 1","output":"[5]","explanation":"Reversing a single node yields the same list."}]$$,
  constraints = $$The number of nodes in the list is n
1 <= n <= 500
-500 <= Node.val <= 500
1 <= left <= right <= n$$
WHERE slug = 'reverse-linked-list-ii';

UPDATE problems SET
  description = $$Given the `head` of a linked list, rotate the list to the right by `k` places. Rotating right by one means the last node moves to the front, shifting all others one position to the right.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5], k = 2","output":"[4,5,1,2,3]","explanation":"Rotating right by 1: [5,1,2,3,4]. Rotating right by 2: [4,5,1,2,3]."},{"input":"head = [0,1,2], k = 4","output":"[2,0,1]","explanation":"After 4 rotations on a list of length 3, effective rotation is 4 mod 3 = 1. So [2,0,1]."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 500]
-100 <= Node.val <= 100
0 <= k <= 2 * 10^9$$
WHERE slug = 'rotate-list';

UPDATE problems SET
  description = $$Given the `head` of a linked list and a value `x`, partition it such that all nodes less than `x` come before nodes greater than or equal to `x`. Preserve the original relative order of the nodes in each of the two partitions.$$,
  examples    = $$[{"input":"head = [1,4,3,2,5,2], x = 3","output":"[1,2,2,4,3,5]","explanation":"Nodes less than 3: [1,2,2] (in original order). Nodes >= 3: [4,3,5] (in original order). Concatenated: [1,2,2,4,3,5]."},{"input":"head = [2,1], x = 2","output":"[1,2]","explanation":"Node 1 < 2 comes first, then node 2."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 200]
-100 <= Node.val <= 100
-200 <= x <= 200$$
WHERE slug = 'partition-list';

UPDATE problems SET
  description = $$You are given the `head` of a singly linked list. Reorder it in-place to: L0 → Ln → L1 → Ln-1 → L2 → Ln-2 → ... You may not modify the values in the list's nodes. Only node changes are allowed.$$,
  examples    = $$[{"input":"head = [1,2,3,4]","output":"[1,4,2,3]","explanation":"The first node, then last node, then second, then second-to-last: 1->4->2->3."},{"input":"head = [1,2,3,4,5]","output":"[1,5,2,4,3]","explanation":"1->5->2->4->3 interleaves from both ends."}]$$,
  constraints = $$The number of nodes in the list is in the range [1, 5 * 10^4]
1 <= Node.val <= 1000$$
WHERE slug = 'reorder-list';

UPDATE problems SET
  description = $$A linked list of length `n` is given such that each node contains an additional random pointer, which could point to any node in the list, or `null`. Construct a deep copy of the list. The deep copy should consist of exactly `n` brand new nodes with copied `val` and `random` pointers.$$,
  examples    = $$[{"input":"head = [[7,null],[13,0],[11,4],[10,2],[1,0]]","output":"[[7,null],[13,0],[11,4],[10,2],[1,0]]","explanation":"Each node is deep copied with val and random pointer correctly set."},{"input":"head = [[1,1],[2,1]]","output":"[[1,1],[2,1]]","explanation":"Both nodes have random pointers pointing to the second node."},{"input":"head = [[3,null],[3,0],[3,null]]","output":"[[3,null],[3,0],[3,null]]","explanation":"Three nodes with the same value; the middle node's random points to the first."}]$$,
  constraints = $$0 <= n <= 1000
-10^4 <= Node.val <= 10^4
Node.random is null or is pointing to some node in the linked list$$
WHERE slug = 'copy-list-random-pointer';

UPDATE problems SET
  description = $$Design a data structure that follows the constraints of a Least Recently Used (LRU) cache. Implement the `LRUCache` class with capacity `cap`, `get(key)` returning the value of the key if it exists (or -1), and `put(key, value)` updating or inserting a key-value pair, evicting the least recently used key if capacity is exceeded.$$,
  examples    = $$[{"input":"[\"LRUCache\",\"put\",\"put\",\"get\",\"put\",\"get\",\"put\",\"get\",\"get\",\"get\"] [[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]","output":"[null,null,null,1,null,-1,null,-1,3,4]","explanation":"Cache capacity 2. After put(1,1) and put(2,2), get(1)=1. put(3,3) evicts key 2 (LRU). get(2)=-1. put(4,4) evicts key 1. get(1)=-1, get(3)=3, get(4)=4."},{"input":"[\"LRUCache\",\"put\",\"get\"] [[1],[1,1],[1]]","output":"[null,null,1]","explanation":"Cache of capacity 1. put(1,1) then get(1) returns 1."}]$$,
  constraints = $$1 <= capacity <= 3000
0 <= key <= 10^4
0 <= value <= 10^5
At most 2 * 10^5 calls will be made to get and put
Both get and put must run in O(1) average time$$
WHERE slug = 'lru-cache';

UPDATE problems SET
  description = $$You are given an array of `k` linked lists where each list is sorted in ascending order. Merge all the linked lists into one sorted linked list and return it.$$,
  examples    = $$[{"input":"lists = [[1,4,5],[1,3,4],[2,6]]","output":"[1,1,2,3,4,4,5,6]","explanation":"Merging [1,4,5], [1,3,4], and [2,6] into one sorted list gives [1,1,2,3,4,4,5,6]."},{"input":"lists = []","output":"[]","explanation":"An empty array of lists merges to an empty list."},{"input":"lists = [[]]","output":"[]","explanation":"A single empty list merges to an empty list."}]$$,
  constraints = $$k == lists.length
0 <= k <= 10^4
0 <= lists[i].length <= 500
-10^4 <= lists[i][j] <= 10^4
lists[i] is sorted in ascending order
The sum of lists[i].length will not exceed 10^4$$
WHERE slug = 'merge-k-sorted-lists';

UPDATE problems SET
  description = $$Given the `head` of a linked list, reverse the nodes of the list `k` at a time, and return the modified list. `k` is a positive integer and is less than or equal to the length of the linked list. If the number of nodes is not a multiple of `k`, the remaining nodes stay in their original order.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5], k = 2","output":"[2,1,4,3,5]","explanation":"Groups of 2 reversed: [2,1], [4,3], and [5] stays unchanged."},{"input":"head = [1,2,3,4,5], k = 3","output":"[3,2,1,4,5]","explanation":"First group of 3 reversed: [3,2,1]. Remaining [4,5] stays unchanged."}]$$,
  constraints = $$The number of nodes in the list is n
1 <= k <= n <= 5000
0 <= Node.val <= 1000
Follow up: Can you solve the problem in O(1) extra memory space?$$
WHERE slug = 'reverse-nodes-k-group';

UPDATE problems SET
  description = $$Given the `head` of a linked list, return the list after sorting it in ascending order. Aim for O(n log n) time and O(1) space complexity.$$,
  examples    = $$[{"input":"head = [4,2,1,3]","output":"[1,2,3,4]","explanation":"The list sorted in ascending order is [1,2,3,4]."},{"input":"head = [-1,5,3,4,0]","output":"[-1,0,3,4,5]","explanation":"The list sorted in ascending order is [-1,0,3,4,5]."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 5 * 10^4]
-10^5 <= Node.val <= 10^5$$
WHERE slug = 'sort-list';

UPDATE problems SET
  description = $$Given the `head` of a singly linked list, group all the nodes with odd indices together followed by the nodes with even indices, and return the reordered list. The first node is considered odd, the second even, and so on. The relative order inside both groups should be preserved.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5]","output":"[1,3,5,2,4]","explanation":"Odd-indexed nodes (1-indexed): 1,3,5. Even-indexed: 2,4. Concatenated: [1,3,5,2,4]."},{"input":"head = [2,1,3,5,6,4,7]","output":"[2,3,6,7,1,5,4]","explanation":"Odd-indexed nodes: 2,3,6,7. Even-indexed: 1,5,4. Concatenated."}]$$,
  constraints = $$The number of nodes in the linked list is in the range [0, 10^4]
-10^6 <= Node.val <= 10^6
Follow up: Could you solve it in O(1) space complexity and O(n) time complexity?$$
WHERE slug = 'odd-even-linked-list';

UPDATE problems SET
  description = $$There is a singly-linked list and you are given a reference to a node to be deleted. Delete the given node. You will not be given access to the first node of the list. Note that by deleting the node, we mean removing it from the linked list, not removing the value.$$,
  examples    = $$[{"input":"head = [4,5,1,9], node = 5","output":"[4,1,9]","explanation":"The node with value 5 is deleted by copying the next node's value into it and bypassing the next node."},{"input":"head = [4,5,1,9], node = 1","output":"[4,5,9]","explanation":"The node with value 1 is removed by copying value 9 into it and skipping the last node."}]$$,
  constraints = $$The number of the nodes in the given list is in the range [2, 1000]
-1000 <= Node.val <= 1000
The value of each node in the list is unique
The node to be deleted is in the list and is not a tail node$$
WHERE slug = 'delete-node-list';

UPDATE problems SET
  description = $$Design your implementation of a singly linked list. The linked list should support `get(index)`, `addAtHead(val)`, `addAtTail(val)`, `addAtIndex(index, val)`, and `deleteAtIndex(index)` operations on a 0-indexed list.$$,
  examples    = $$[{"input":"[\"MyLinkedList\",\"addAtHead\",\"addAtTail\",\"addAtIndex\",\"get\",\"deleteAtIndex\",\"get\"] [[],[1],[3],[1,2],[1],[1],[1]]","output":"[null,null,null,null,2,null,3]","explanation":"Add 1 at head: [1]. Add 3 at tail: [1,3]. Add 2 at index 1: [1,2,3]. get(1)=2. deleteAtIndex(1): [1,3]. get(1)=3."},{"input":"[\"MyLinkedList\",\"addAtHead\",\"get\"] [[],[1],[0]]","output":"[null,null,1]","explanation":"Adding 1 at head and getting index 0 returns 1."}]$$,
  constraints = $$0 <= index, val <= 1000
Please do not use the built-in LinkedList library
At most 2000 calls will be made to get, addAtHead, addAtTail, addAtIndex and deleteAtIndex$$
WHERE slug = 'design-linked-list';

UPDATE problems SET
  description = $$You are given two non-empty linked lists `l1` and `l2` representing two non-negative integers. The digits are stored in normal order (most significant digit first). Add the two numbers and return the sum as a linked list in the same order. You may not reverse the input lists.$$,
  examples    = $$[{"input":"l1 = [7,2,4,3], l2 = [5,6,4]","output":"[7,8,0,7]","explanation":"7243 + 564 = 7807, stored in order as [7,8,0,7]."},{"input":"l1 = [2,4,3], l2 = [5,6,4]","output":"[8,0,7]","explanation":"243 + 564 = 807."},{"input":"l1 = [0], l2 = [0]","output":"[0]","explanation":"0 + 0 = 0."}]$$,
  constraints = $$The number of nodes in each linked list is in the range [1, 100]
0 <= Node.val <= 9
It is guaranteed that the list represents a number that does not have leading zeros$$
WHERE slug = 'add-two-numbers-ii';

UPDATE problems SET
  description = $$You are given a doubly linked list, which contains nodes that have a `next` pointer, a `prev` pointer, and an additional `child` pointer. The `child` pointer may point to a separate doubly linked list, also containing such nodes. Flatten the list so that all nodes appear in a single-level doubly linked list. The nodes from child lists should appear after the node that has the child pointer.$$,
  examples    = $$[{"input":"head = [1,2,3,4,5,6,null,null,null,7,8,9,10,null,null,11,12]","output":"[1,2,3,7,8,11,12,9,10,4,5,6]","explanation":"The child of node 3 is [7,8,9,10], and the child of node 8 is [11,12]. After flattening, all levels merge into one."},{"input":"head = [1,2,null,3]","output":"[1,3,2]","explanation":"Node 1 has a child [3]; inserting child list after node 1 gives [1,3,2]."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 1000]
1 <= Node.val <= 10^5
The depth of the nesting is in [1, 1000]$$
WHERE slug = 'flatten-multilevel-list';

UPDATE problems SET
  description = $$Given the `head` of a singly linked list and an integer `k`, split the linked list into `k` consecutive parts. The length of each part should be as equal as possible — no two parts should have sizes differing by more than one. Parts that come earlier should have larger size. Return an array of the `k` parts.$$,
  examples    = $$[{"input":"head = [1,2,3], k = 5","output":"[[1],[2],[3],[],[]]","explanation":"List of length 3 split into 5 parts: first 3 parts get one node each, last 2 are null/empty."},{"input":"head = [1,2,3,4,5,6,7,8,9,10], k = 3","output":"[[1,2,3,4],[5,6,7],[8,9,10]]","explanation":"10 nodes into 3 parts: sizes 4, 3, 3. The first part is larger since 10/3 has remainder 1."}]$$,
  constraints = $$The number of nodes in the list is in the range [0, 1000]
0 <= Node.val <= 1000
1 <= k <= 50$$
WHERE slug = 'split-linked-list-parts';
