-- V22: Batch 5 — gold-standard content for 10 binary search problems.
-- Gold-standard content; all problems remain in CONTENT_REVIEW until technical validation.

-- 1 Binary Search
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='binary-search-problem'),1,$$Identify the binary-search signal: the input is sorted and you need to locate a target. Focus on the invariant that eliminates half the search space per step.$$,'Concept'),
((SELECT id FROM problems WHERE slug='binary-search-problem'),2,$$Maintain left and right boundaries. Compare the middle element to the target and narrow the window toward the correct half.$$,'Direction'),
((SELECT id FROM problems WHERE slug='binary-search-problem'),3,$$Trace the invariant: after each comparison, the target, if present, lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$Find the index of a target in a sorted array, or return -1.$$,$$Keywords: Binary Search; sorted array; target lookup. Ask what ordering guarantee lets you discard half the space.$$,$$Use when the input is sorted and you need exact-match index lookup.$$,$$Do not use when elements are unsorted or the search space is not monotonic.$$,$$Maintain left and right boundaries. Compare the middle element to the target and narrow the window toward the correct half.$$,$$Maintain left and right boundaries. Compare the middle element to the target and narrow the window toward the correct half.$$,$$Classic binary search with integer midpoint.$$,$$Linear scan is O(n).$$,'O(n)','O(1)',$$Classic binary search with integer midpoint.$$,'O(log n)','O(1)',$$left=0; right=n-1; while left<=right: mid=(left+right)/2; if nums[mid]==target return mid; elif nums[mid]<target left=mid+1; else right=mid-1; return -1$$,$$Each step halves the remaining candidates; after log n steps at most one candidate remains.$$,$$After each step, the target, if present, lies within [left, right].$$,$$Off-by-one in boundary update (mid vs mid±1); integer overflow with (left+right)/2; exiting loop one step early.$$,$$For real arrays use left+(right-left)/2 to avoid overflow. Extend to lower_bound/upper_bound for duplicate handling.$$,$$public int search(int[] nums,int target){int l=0,r=nums.length-1;while(l<=r){int m=l+(r-l)/2;if(nums[m]==target)return m;if(nums[m]<target)l=m+1;else r=m-1;}return -1;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$Why use left+(right-left)/2 instead of (left+right)/2?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$How would you find the first occurrence of a duplicate target?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$What changes if the array is sorted in descending order?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$How do you implement lower_bound (leftmost position ≥ target)?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='binary-search-problem'),$$How would you binary-search over a conceptual sorted space rather than a concrete array?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-search-problem'),$$6
9
-1 0 3 5 9 12$$,$$4$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-search-problem'),$$6
2
-1 0 3 5 9 12$$,$$-1$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-search-problem'),$$1
1
1$$,$$0$$,true,3);

-- 2 First Bad Version
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='first-bad-version'),1,$$A version is bad if and only if all versions after it are also bad. This monotonic property makes binary search applicable.$$,'Concept'),
((SELECT id FROM problems WHERE slug='first-bad-version'),2,$$Search for the leftmost true in a boolean sequence. When the middle version is bad, the boundary moves left; when good, it moves right.$$,'Direction'),
((SELECT id FROM problems WHERE slug='first-bad-version'),3,$$Trace the invariant: after each step, the first bad version lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='first-bad-version'),$$Given n versions 1..n where bad versions form a suffix, find the first bad version.$$,$$Keywords: First Bad Version; isBadVersion API; leftmost true. The bad/good split is a classic binary search on a boolean predicate.$$,$$Use when the predicate changes from false to true exactly once and stays true.$$,$$Do not use when the predicate is non-monotonic.$$,$$Search for the leftmost true in a boolean sequence. When the middle version is bad, the boundary moves left; when good, it moves right.$$,$$Search for the leftmost true in a boolean sequence. When the middle version is bad, the boundary moves left; when good, it moves right.$$,$$Binary search for the leftmost true predicate.$$,$$Calling isBadVersion(1)..isBadVersion(n) linearly is O(n).$$,'O(n)','O(1)',$$Binary search for the leftmost true predicate.$$,'O(log n)','O(1)',$$left=1; right=n; while left<right: mid=left+(right-left)/2; if isBadVersion(mid) right=mid; else left=mid+1; return left$$,$$When mid is bad, the first bad is mid or earlier so right converges to mid. When mid is good, the first bad is strictly after so left advances.$$,$$After each step, the first bad version lies within [left, right].$$,$$Using left<=right with wrong boundary updates; returning mid before loop ends; integer overflow in mid computation.$$,$$The template left<right with right=mid (not mid-1) finds the leftmost true for any monotonic predicate.$$,$$public int firstBadVersion(int n){int l=1,r=n;while(l<r){int m=l+(r-l)/2;if(isBadVersion(m))r=m;else l=m+1;}return l;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='first-bad-version'),$$Why use right=mid instead of right=mid-1 here?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='first-bad-version'),$$How many calls to isBadVersion does your solution make in the worst case?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='first-bad-version'),$$How would you adapt this to find the last good version?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='first-bad-version'),$$How does this template generalize to "find leftmost x satisfying f(x)" problems?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='first-bad-version'),$$How would you minimize API calls if each isBadVersion call has a network cost?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='first-bad-version'),$$5
4$$,$$4$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='first-bad-version'),$$1
1$$,$$1$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='first-bad-version'),$$10
7$$,$$7$$,true,3);

-- 3 Sqrt(x)
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='sqrt-x'),1,$$The integer square root of x is the largest integer k such that k*k <= x. As k increases, k*k is monotone — binary search applies.$$,'Concept'),
((SELECT id FROM problems WHERE slug='sqrt-x'),2,$$Binary search over [0, x]. If mid*mid <= x, it is a candidate and move left boundary up; otherwise move right boundary down.$$,'Direction'),
((SELECT id FROM problems WHERE slug='sqrt-x'),3,$$Trace the invariant: after each step, the answer lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='sqrt-x'),$$Return the floor of the square root of x without using built-in pow/sqrt.$$,$$Keywords: Sqrt(x); floor square root; integer binary search. The monotone property k*k <= x defines a classic binary search boundary.$$,$$Use when looking for the largest integer satisfying a monotone inequality.$$,$$Do not use floating-point sqrt directly if integer result is required.$$,$$Binary search over [0, x]. If mid*mid <= x, it is a candidate and move left boundary up; otherwise move right boundary down.$$,$$Binary search over [0, x]. If mid*mid <= x, it is a candidate and move left boundary up; otherwise move right boundary down.$$,$$Binary search for the largest k with k*k <= x.$$,$$Incrementing k from 0 is O(sqrt(x)).$$,'O(sqrt(x))','O(1)',$$Binary search for the largest k with k*k <= x.$$,'O(log x)','O(1)',$$left=0; right=x; while left<=right: mid=left+(right-left)/2; if mid*mid<=x: ans=mid; left=mid+1; else right=mid-1; return ans$$,$$The search space is monotone: all k with k*k <= x form a prefix so the answer is the last true boundary.$$,$$When left<=right, the largest valid k seen so far is stored in ans.$$,$$Integer overflow when computing mid*mid for large x; off-by-one; using long to avoid overflow.$$,$$Use (long) cast for mid*mid. For Newton's method: start with x, iterate k=(k+x/k)/2 until stable — converges in O(log log x) steps.$$,$$public int mySqrt(int x){long l=0,r=x,a=0;while(l<=r){long m=l+(r-l)/2;if(m*m<=x){a=m;l=m+1;}else r=m-1;}return(int)a;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='sqrt-x'),$$Why must mid be cast to long before squaring?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='sqrt-x'),$$How would you implement Newton's method instead?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='sqrt-x'),$$How would you return a floating-point result with p decimal places?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='sqrt-x'),$$What is the convergence rate of Newton's method versus binary search?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='sqrt-x'),$$How would you compute the cube root?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sqrt-x'),$$4$$,$$2$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sqrt-x'),$$8$$,$$2$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sqrt-x'),$$2147395600$$,$$46340$$,true,3);

-- 4 Peak Index in a Mountain Array
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='peak-index-mountain'),1,$$A mountain array rises to a peak and then falls. At any index, comparing arr[mid] to arr[mid+1] tells you which slope you are on.$$,'Concept'),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),2,$$If arr[mid] < arr[mid+1], the peak is to the right; otherwise the peak is at mid or to the left. Narrow accordingly.$$,'Direction'),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),3,$$Trace the invariant: the peak index lies within [left, right] after every step.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$Find the index of the maximum element in a mountain (bitonic) array.$$,$$Keywords: Peak Index Mountain Array; bitonic; slope comparison. Comparing adjacent elements reveals which side the peak is on.$$,$$Use when the array has a single peak and adjacent comparison determines the slope direction.$$,$$Do not use when the array has multiple peaks.$$,$$If arr[mid] < arr[mid+1], the peak is to the right; otherwise the peak is at mid or to the left. Narrow accordingly.$$,$$If arr[mid] < arr[mid+1], the peak is to the right; otherwise the peak is at mid or to the left. Narrow accordingly.$$,$$Binary search comparing arr[mid] to arr[mid+1].$$,$$Linear scan for maximum is O(n).$$,'O(n)','O(1)',$$Binary search comparing arr[mid] to arr[mid+1].$$,'O(log n)','O(1)',$$left=0; right=n-1; while left<right: mid=left+(right-left)/2; if arr[mid]<arr[mid+1] left=mid+1; else right=mid; return left$$,$$The slope comparison tells which half contains the peak, eliminating half the candidates each step.$$,$$After each step, the peak index lies within [left, right].$$,$$Accessing arr[mid+1] out of bounds; using left<right with wrong boundary updates.$$,$$The same template solves Find Peak Element for arbitrary arrays with local peaks. Generalize to 2D peak finding.$$,$$public int peakIndexInMountainArray(int[] a){int l=0,r=a.length-1;while(l<r){int m=l+(r-l)/2;if(a[m]<a[m+1])l=m+1;else r=m;}return l;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$Why is left<right the right loop condition rather than left<=right?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$How does this differ from searching for a target value?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$What if there are duplicate values at the peak?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$How would you extend this to find any local peak in an arbitrary array?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='peak-index-mountain'),$$How would you find a peak in a 2D matrix in O(n log m)?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='peak-index-mountain'),$$3
0 1 0$$,$$1$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='peak-index-mountain'),$$5
0 2 1 0 0$$,$$1$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='peak-index-mountain'),$$7
0 1 3 5 4 2 0$$,$$3$$,true,3);

-- 5 Valid Perfect Square
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='valid-perfect-square'),1,$$A perfect square num has an integer square root k. As k increases, k*k grows monotonically — binary search applies.$$,'Concept'),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),2,$$Binary search over [1, num]. If mid*mid equals num, return true. If it exceeds num, move right down; otherwise move left up.$$,'Direction'),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),3,$$Trace the invariant: if the integer square root exists, it lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$Return true if num is a perfect square without using sqrt().$$,$$Keywords: Valid Perfect Square; no sqrt; integer root. Binary search for k with k*k==num over a monotone range.$$,$$Use when checking exact equality of a square root as an integer.$$,$$Do not compare floating-point results directly due to precision issues.$$,$$Binary search over [1, num]. If mid*mid equals num, return true. If it exceeds num, move right down; otherwise move left up.$$,$$Binary search over [1, num]. If mid*mid equals num, return true. If it exceeds num, move right down; otherwise move left up.$$,$$Binary search for exact integer square root.$$,$$Incrementing k from 1 to sqrt(num) is O(sqrt(num)).$$,'O(sqrt(n))','O(1)',$$Binary search for exact integer square root.$$,'O(log n)','O(1)',$$left=1; right=num; while left<=right: mid=left+(right-left)/2; sq=mid*mid; if sq==num return true; elif sq<num left=mid+1; else right=mid-1; return false$$,$$The search space is a sorted sequence of squares; exact match detection terminates early.$$,$$If the answer exists it lies within [left, right] after each step.$$,$$Integer overflow when squaring mid for large num; not handling num==1 explicitly (though the general loop covers it).$$,$$Use long arithmetic. Alternatively check via the identity: sum of first k odd integers equals k*k.$$,$$public boolean isPerfectSquare(int num){long l=1,r=num;while(l<=r){long m=l+(r-l)/2,sq=m*m;if(sq==num)return true;if(sq<num)l=m+1;else r=m-1;}return false;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$What algebraic identity can check perfect squares without binary search?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$Why is num==1 handled correctly without a special case?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$How would you check if num is a perfect cube?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$How does this differ from the Sqrt(x) problem?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='valid-perfect-square'),$$Could bitwise tricks speed this up in practice?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='valid-perfect-square'),$$16$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='valid-perfect-square'),$$14$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='valid-perfect-square'),$$1$$,$$true$$,true,3);

-- 6 Find Peak Element
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='find-peak-element'),1,$$A peak element is greater than its neighbours. Comparing arr[mid] to arr[mid+1] reveals whether a peak is to the left or right.$$,'Concept'),
((SELECT id FROM problems WHERE slug='find-peak-element'),2,$$If arr[mid] < arr[mid+1], the right side is ascending so a peak exists to the right. Otherwise a peak exists at mid or to the left.$$,'Direction'),
((SELECT id FROM problems WHERE slug='find-peak-element'),3,$$Trace the invariant: a peak index always lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='find-peak-element'),$$Find any index where nums[i] > nums[i-1] and nums[i] > nums[i+1] (treat boundaries as -infinity).$$,$$Keywords: Find Peak Element; local maximum; O(log n). Slope comparison eliminates half the candidates without needing a fully sorted array.$$,$$Use when any local maximum suffices and adjacent comparison reveals slope direction.$$,$$Do not use when all peaks are required.$$,$$If arr[mid] < arr[mid+1], the right side is ascending so a peak exists to the right. Otherwise a peak exists at mid or to the left.$$,$$If arr[mid] < arr[mid+1], the right side is ascending so a peak exists to the right. Otherwise a peak exists at mid or to the left.$$,$$Binary search using slope comparison.$$,$$Linear scan is O(n).$$,'O(n)','O(1)',$$Binary search using slope comparison.$$,'O(log n)','O(1)',$$left=0; right=n-1; while left<right: mid=left+(right-left)/2; if nums[mid]<nums[mid+1] left=mid+1; else right=mid; return left$$,$$When arr[mid]<arr[mid+1] the slope is still ascending so the peak lies to the right of mid. Otherwise a peak is at mid or to the left because arr[mid]>=arr[mid+1].$$,$$A peak index always lies within [left, right] after each step.$$,$$Off-by-one when checking mid+1; using left<=right causing infinite loop; assuming the array is fully sorted.$$,$$Identical template to peak-index-mountain. For 2D peak finding use a divide-and-conquer approach on columns.$$,$$public int findPeakElement(int[] nums){int l=0,r=nums.length-1;while(l<r){int m=l+(r-l)/2;if(nums[m]<nums[m+1])l=m+1;else r=m;}return l;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='find-peak-element'),$$Why is any local peak acceptable, and how does binary search guarantee finding one?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='find-peak-element'),$$What guarantees that a peak must exist?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='find-peak-element'),$$How would you find ALL peaks?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='find-peak-element'),$$How would you extend this to a 2D matrix?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='find-peak-element'),$$How does this template change when adjacent duplicates are allowed?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='find-peak-element'),$$3
1 2 3$$,$$2$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='find-peak-element'),$$4
1 2 1 3$$,$$1$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='find-peak-element'),$$5
5 4 3 2 1$$,$$0$$,true,3);

-- 7 Koko Eating Bananas
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),1,$$As the eating speed k increases, the hours needed decreases monotonically. Binary search over feasible speeds.$$,'Concept'),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),2,$$For a given speed, compute total hours as sum of ceil(pile/speed). Check if this is <= h and narrow the speed window accordingly.$$,'Direction'),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),3,$$Trace the invariant: after each step, the minimum valid speed lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$Find the minimum eating speed k (bananas/hour) such that all piles can be eaten in h hours.$$,$$Keywords: Koko Eating Bananas; minimum speed; feasibility check. Feasibility is monotone in speed — a classic binary search on the answer.$$,$$Use when a feasibility function is monotone and you need the minimum (or maximum) value satisfying it.$$,$$Do not use when feasibility is non-monotone.$$,$$For a given speed, compute total hours as sum of ceil(pile/speed). Check if this is <= h and narrow the speed window accordingly.$$,$$For a given speed, compute total hours as sum of ceil(pile/speed). Check if this is <= h and narrow the speed window accordingly.$$,$$Binary search on speed with O(n) feasibility check.$$,$$Trying every speed from 1 to max(piles) is O(max * n).$$,'O(max*n)','O(1)',$$Binary search on speed with O(n) feasibility check.$$,'O(n log max)','O(1)',$$left=1; right=max(piles); while left<right: mid=...; hours=sum(ceil(p/mid) for p); if hours<=h right=mid; else left=mid+1; return left$$,$$The feasibility function is non-increasing in speed. Binary search finds the exact transition from infeasible to feasible.$$,$$After each step, the minimum feasible speed lies within [left, right].$$,$$Using integer division instead of ceiling; forgetting that right should start at max(piles) not sum(piles).$$,$$The binary-search-on-answer template applies to Capacity to Ship, Minimum Days Bouquets, and similar minimization problems.$$,$$public int minEatingSpeed(int[] piles,int h){int l=1,r=0;for(int p:piles)r=Math.max(r,p);while(l<r){int m=l+(r-l)/2;long hrs=0;for(int p:piles)hrs+=(p+m-1)/m;if(hrs<=h)r=m;else l=m+1;}return l;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$Why is the upper bound max(piles) rather than sum(piles)?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$Why must you use ceiling division for each pile?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$How would you adapt this if Koko can eat fractions of bananas?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$How does this template generalize to all "minimum feasible value" problems?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$What if each pile takes a mandatory setup time per hour worked on it?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$4
8
3 6 7 11$$,$$4$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$5
5
30 11 23 4 20$$,$$30$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='koko-eating-bananas'),$$3
6
1 1 1$$,$$1$$,true,3);

-- 8 Time Based Key-Value Store
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),1,$$Timestamps are set in strictly increasing order. For a given key, all (timestamp, value) pairs are sorted by timestamp — binary search applies.$$,'Concept'),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),2,$$On get, binary search for the largest timestamp <= the queried timestamp in that key's list. Return "" if none exists.$$,'Direction'),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),3,$$Trace the invariant: the answer is the rightmost entry with timestamp <= target.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$Design a key-value store where each key maps to multiple (timestamp, value) pairs; get returns the value with the largest timestamp <= the query timestamp.$$,$$Keywords: Time Based Key-Value Store; timestamp; floor search. Sorted timestamps per key make get a binary search for the rightmost valid entry.$$,$$Use when values are versioned by time and you need the floor value at a given time.$$,$$Do not use a linear scan per get if timestamps are pre-sorted.$$,$$On get, binary search for the largest timestamp <= the queried timestamp in that key's list. Return "" if none exists.$$,$$On get, binary search for the largest timestamp <= the queried timestamp in that key's list. Return "" if none exists.$$,$$HashMap to list of (timestamp, value); binary search in get.$$,$$Scanning all entries per get is O(n).$$,'O(n)','O(n)',$$HashMap to list of (timestamp, value); binary search in get.$$,'O(log n) per get; O(1) amortized per set','O(n)',$$set: map[key].append((ts, val)); get: list=map[key]; binary search for largest ts<=timestamp; return value or ""$$,$$Timestamps are inserted in order so the list is always sorted, enabling binary search for the floor timestamp.$$,$$The answer is the rightmost entry with timestamp <= the queried timestamp, or "" if the list is empty or all timestamps exceed the query.$$,$$Returning the wrong adjacent entry; forgetting to return "" when all timestamps exceed the query; using TreeMap when a simple list suffices.$$,$$TreeMap.floorEntry is cleaner in Java but uses O(log n) per set too. For multi-threaded use, consider read-write locks.$$,$$class TimeMap{Map<String,List<int[]>>m=new HashMap<>();public void set(String k,String v,int t){m.computeIfAbsent(k,x->new ArrayList<>()).add(new int[]{t,v.hashCode()});}public String get(String k,int t){List<int[]>l=m.getOrDefault(k,List.of());int lo=0,hi=l.size()-1,ans=-1;while(lo<=hi){int mid=lo+(hi-lo)/2;if(l.get(mid)[0]<=t){ans=mid;lo=mid+1;}else hi=mid-1;}return ans<0?"":vals.get(ans);}}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$Why can you guarantee timestamps are sorted at query time?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$What changes if timestamps are not guaranteed to be strictly increasing?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$How does Java's TreeMap.floorEntry compare?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$How would you make this thread-safe under concurrent set/get?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$How would you add expiry (TTL) to keys?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$set foo bar 1; get foo 1$$,$$bar$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$set foo bar 1; get foo 3$$,$$bar$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='time-based-key-value-store'),$$get foo 0$$,$$""$$,true,3);

-- 9 Capacity To Ship Packages Within D Days
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),1,$$As ship capacity increases, the number of days needed decreases monotonically. Binary search over feasible capacities.$$,'Concept'),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),2,$$For a given capacity, simulate the greedy loading — greedily fill each trip as much as possible and count days. Check against d.$$,'Direction'),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),3,$$Trace the invariant: the minimum sufficient capacity lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$Find the minimum ship capacity to ship all packages in at most d days (order preserved).$$,$$Keywords: Capacity To Ship; minimum capacity; feasibility. Feasibility is monotone in capacity — binary search on the answer with a greedy feasibility check.$$,$$Use when you need the minimum value satisfying a monotone feasibility function.$$,$$Do not use when order can be rearranged or the problem has a non-greedy feasibility structure.$$,$$For a given capacity, simulate the greedy loading — greedily fill each trip as much as possible and count days. Check against d.$$,$$For a given capacity, simulate the greedy loading — greedily fill each trip as much as possible and count days. Check against d.$$,$$Binary search on capacity with O(n) greedy feasibility check.$$,$$Trying every capacity from max(weights) to sum(weights) is O(n * sum).$$,'O(n * sum)','O(1)',$$Binary search on capacity with O(n) greedy feasibility check.$$,'O(n log sum)','O(1)',$$left=max(weights); right=sum(weights); while left<right: mid=...; days=1; curr=0; for w: if curr+w>mid days++; curr=0; curr+=w; if days<=d right=mid; else left=mid+1; return left$$,$$The feasibility function is non-increasing in capacity. The lower bound max(weights) ensures every package can fit.$$,$$After each step, the minimum sufficient capacity lies within [left, right].$$,$$Setting lower bound to 0 instead of max(weights); forgetting to count the last day; not preserving package order.$$,$$Identical template to Koko Eating Bananas. Applies to Split Array Largest Sum and Minimum Days to Bloom.$$,$$public int shipWithinDays(int[] w,int d){int l=0,r=0;for(int x:w){l=Math.max(l,x);r+=x;}while(l<r){int m=l+(r-l)/2,days=1,cur=0;for(int x:w){if(cur+x>m){days++;cur=0;}cur+=x;}if(days<=d)r=m;else l=m+1;}return l;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$Why must the lower bound be max(weights) and not 1?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$Why is the greedy loading optimal for the feasibility check?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$How would you adapt this if packages could be split across days?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$How does this template apply to Split Array Largest Sum?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$What changes for multiple ships operating in parallel?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$5
10
1 2 3 4 5 6 7 8 9 10$$,$$15$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$3
3
3 2 2 4 1 4$$,$$6$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='capacity-to-ship-packages'),$$1
1
1 2 3 1 1$$,$$3$$,true,3);

-- 10 Single Element in a Sorted Array
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),1,$$In a sorted array where every element appears exactly twice except one, pairs are predictably positioned. An extra element disrupts the parity, and that disruption is detectable via index parity.$$,'Concept'),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),2,$$At an even index mid, if nums[mid]==nums[mid+1] the single element is to the right; otherwise to the left or at mid. Narrow accordingly.$$,'Direction'),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),3,$$Trace the invariant: the single element lies within [left, right].$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$Find the element that appears exactly once in a sorted array where all others appear twice.$$,$$Keywords: Single Element Sorted Array; parity; O(log n). Parity of the mid index determines which side the single element disrupts.$$,$$Use when the array is sorted, all elements appear in pairs except one, and O(log n) is required.$$,$$Do not use XOR trick here when O(log n) is required rather than O(n).$$,$$At an even index mid, if nums[mid]==nums[mid+1] the single element is to the right; otherwise to the left or at mid. Narrow accordingly.$$,$$At an even index mid, if nums[mid]==nums[mid+1] the single element is to the right; otherwise to the left or at mid. Narrow accordingly.$$,$$Binary search using parity of the mid index.$$,$$XOR of all elements is O(n).$$,'O(n)','O(1)',$$Binary search using parity of the mid index.$$,'O(log n)','O(1)',$$left=0; right=n-1; while left<right: mid=left+(right-left)/2; if mid is odd mid--; if nums[mid]==nums[mid+1] left=mid+2; else right=mid; return nums[left]$$,$$Before the single element, pairs occupy (even, odd) index positions. After it, the parity shifts. Detecting the shift reveals which half holds the single element.$$,$$After each step, the single element lies within [left, right].$$,$$Not rounding mid down to even; out-of-bounds access on mid+1 when mid is the last index; missing the left==right exit condition.$$,$$Equivalently compare nums[mid] and nums[mid^1] to always compare a mid with its expected pair partner. This handles even/odd uniformly.$$,$$public int singleNonDuplicate(int[] nums){int l=0,r=nums.length-1;while(l<r){int m=l+(r-l)/2;if((m&1)==1)m--;if(nums[m]==nums[m+1])l=m+2;else r=m;}return nums[l];}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$Why must mid be rounded to an even index?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$How does nums[mid^1] simplify the comparison?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$What would the XOR approach look like, and why is it O(n)?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$How does pair-parity generalize to "elements appear k times except one appears once"?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$How would you solve this if the array were not sorted?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$9
1 1 2 3 3 4 4 8 8$$,$$2$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$7
3 3 7 7 10 11 11$$,$$10$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='single-element-sorted-array'),$$1
5$$,$$5$$,true,3);
