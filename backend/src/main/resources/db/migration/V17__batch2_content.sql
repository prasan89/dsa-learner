-- V17: Batch 2 — gold-standard content for 10 high-priority DSA problems.
-- All problems remain in CONTENT_REVIEW until technical validation.

-- 1 Maximum Subarray
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='maximum-subarray'),1,$$Think about the best contiguous subarray that must end at the current position. A negative running sum can only hurt a future extension.$$,'Concept'),
((SELECT id FROM problems WHERE slug='maximum-subarray'),2,$$At each value choose between starting a new subarray here or extending the best subarray ending at the previous index.$$,'Direction'),
((SELECT id FROM problems WHERE slug='maximum-subarray'),3,$$Use Kadane: current = max(value, current + value), and best = max(best, current). Initialize from the first element so all-negative input works.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='maximum-subarray'),
$$Maximum Subarray is the canonical contiguous-range optimization problem. A negative running prefix can never improve a future sum, so it can be discarded.$$,
$$Keywords: contiguous subarray, maximum sum, continuous range. Recognition question: can the best range ending here be derived from the best range ending one position earlier? If yes, think Kadane's running-state DP.$$,
$$Use when elements must be contiguous and the objective is additive over the chosen range.$$,
$$Do not use it when the selected elements may be non-contiguous or when the state depends on more than the best suffix sum.$$,
$$For each position, either start a new range at this value or extend the best range that ended immediately before it. Keep the best answer seen globally.$$,
$$Define current as the best non-empty subarray ending at i. It is either nums[i] alone or current plus nums[i]. Then take the maximum current value over all positions.$$,
$$Kadane's algorithm scans once. Maintain current and best. current = max(nums[i], current + nums[i]); best = max(best, current).$$,
$$Enumerate every start and end pair and calculate each range sum. Even with prefix sums, there are O(n squared) ranges.$$,
'O(n^2)','O(n)',
$$Scan once with the best suffix state. A negative current value is automatically discarded when the next element is larger than current plus that element.$$,
'O(n)','O(1)',
$$FUNCTION maxSubArray(nums)
    current = nums[0]
    best = nums[0]
    FOR i = 1..n-1
        current = MAX(nums[i], current + nums[i])
        best = MAX(best, current)
    RETURN best$$,
$$Every non-empty subarray ending at i either starts at i or extends a subarray ending at i-1. Therefore the recurrence considers every possible optimum without enumerating all ranges.$$,
$$After processing i, current is the maximum sum of a non-empty contiguous subarray whose right endpoint is i.$$,
$$Initialize best to zero instead of nums[0]; confuse subarray with subsequence; return current instead of global best; mishandle all-negative arrays.$$,
$$Track start/end indices to return the actual range. For a circular array combine maximum-subarray and minimum-subarray reasoning. The O(1)-state scan can also process an unbounded stream when only the best sum is needed.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$Can you return the start and end indices as well as the maximum sum?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$How would you solve the circular-array version?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$Why does the algorithm work for an all-negative array?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$Can you solve it using divide and conquer?$$,'VARIATION',4),
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$How would you process an unbounded stream?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='maximum-subarray'),$$How would you combine partial results across distributed partitions?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='maximum-subarray'),'9
-2 1 -3 4 -1 2 1 -5 4','6',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='maximum-subarray'),'5
5 4 -1 7 8','23',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='maximum-subarray'),'5
-2 -1 -3 -4 -1','-1',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='maximum-subarray'),'1
-5','-5',true,4);

-- 2 Product of Array Except Self
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),1,$$The answer at index i is the product of everything to its left multiplied by everything to its right.$$,'Concept'),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),2,$$Use the output array for prefix products, then walk backwards with one running suffix product.$$,'Direction'),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),3,$$Forward: output[i] = prefix, then prefix *= nums[i]. Reverse: output[i] *= suffix, then suffix *= nums[i]. No division is required.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),
$$Each answer excludes one position and decomposes naturally into a prefix product and suffix product. The presence of zero makes division especially unsafe.$$,
$$Keywords: product of all other elements, exclude current index, no division, answer for every position. Ask whether each answer can be built from reusable information on both sides.$$,
$$Use when every output depends on all elements except the current one and prefix/suffix aggregation is possible.$$,
$$Do not use when the operation is not safely associative or when the problem specifically requires a different modular/inverse-based formulation.$$,
$$Split the array around each index. Everything before it contributes the prefix product and everything after it contributes the suffix product.$$,
$$First store the product to the left of each position. Then scan right-to-left while maintaining the product to the right and multiply it into the stored prefix.$$,
$$Use two linear passes. The output array stores prefix products, while a scalar suffix stores the product of elements to the right.$$,
$$For each index independently multiply every other element. This is quadratic. Division by the total product is shorter but fails with zeros and violates the common no-division requirement.$$,
'O(n^2)','O(1) auxiliary per answer',
$$Forward pass builds prefixes. Reverse pass multiplies each prefix by the suffix product.$$,
'O(n)','O(1) extra space excluding the required output array',
$$FUNCTION productExceptSelf(nums)
    output = array of ones
    prefix = 1
    FOR i = 0..n-1
        output[i] = prefix
        prefix *= nums[i]
    suffix = 1
    FOR i = n-1..0
        output[i] *= suffix
        suffix *= nums[i]
    RETURN output$$,
$$Before the reverse pass processes i, output[i] contains exactly all products left of i and suffix contains exactly all products right of i. Their product excludes nums[i].$$,
$$During the reverse pass, suffix equals the product of nums[i+1..n-1]. During the forward pass, output[i] equals the product of nums[0..i-1].$$,
$$Using division; including nums[i] in a prefix or suffix; claiming the required output array is not part of the space discussion; ignoring zero and numeric overflow.$$,
$$Handle zero counts explicitly when division is permitted. For streaming input, exact per-position results generally require retaining enough information to revisit one side. For very large products consider long or BigInteger according to the contract.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$Why is division unsafe when the array can contain zero?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$How do exactly one zero and two zeros change the result?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$Can you solve it with extra prefix and suffix arrays?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$How should overflow be handled for very large products?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$How would you distribute the computation across machines?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='product-of-array-except-self'),$$What exactly does O(1) auxiliary space mean when the output array is required?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='product-of-array-except-self'),'4
1 2 3 4','[24,12,8,6]',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='product-of-array-except-self'),'4
-1 1 0 -3 3','[0,0,9,0,0]',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='product-of-array-except-self'),'4
0 0 2 3','[0,0,0,0]',true,3);

-- 3 Group Anagrams
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='group-anagrams'),1,$$Anagrams have the same character frequencies. Find a canonical key that is identical for equivalent strings.$$,'Concept'),
((SELECT id FROM problems WHERE slug='group-anagrams'),2,$$For lowercase English letters, a 26-count frequency vector is a compact canonical signature.$$,'Direction'),
((SELECT id FROM problems WHERE slug='group-anagrams'),3,$$Build a map from serialized frequency signatures to lists of strings. Each word is counted once.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='group-anagrams'),
$$Group Anagrams is a canonical frequency-signature hashing problem. Order does not matter, but character multiplicity does.$$,
$$Keywords: anagram, rearrangement, group equivalent strings, same letters. Ask whether every item can be mapped to a canonical representation shared by equivalent items.$$,
$$Use when equality depends on a multiset of values and a stable signature can be computed efficiently.$$,
$$Do not use when order itself determines identity or when the proposed signature loses information required to distinguish non-equivalent inputs.$$,
$$Eat and tea look different as strings but have the same counts of a, e, and t. Their frequency signature can therefore be the map key.$$,
$$Sorting each word creates a canonical form but costs O(k log k). With lowercase letters, counting 26 characters takes O(k), so hashing the count signature is more efficient.$$,
$$For each word count its 26 letters, serialize the counts into a stable key, and append the word to the corresponding HashMap bucket.$$,
$$Compare each word against all existing groups and check whether character frequencies match. In the worst case this repeatedly compares many strings and approaches quadratic behavior.$$,
'O(n^2 k)','O(nk)',
$$Compute one fixed-size frequency signature per word and use a HashMap to group equal signatures.$$,
'O(S) expected, where S is total input characters','O(S) including output and stored groups',
$$FUNCTION groupAnagrams(words)
    groups = map
    FOR word in words
        count[26] = zero
        FOR c in word
            count[c-'a']++
        key = serialize(count)
        groups[key].add(word)
    RETURN groups.values()$$,
$$Two strings are anagrams exactly when every character count is equal. Therefore equal signatures are exactly the equivalence classes required by the problem.$$,
$$After processing any prefix of the input, every processed word is in exactly one group identified by its complete frequency signature.$$,
$$Using the raw word as the key; forgetting repeated letters; using a mutable array directly as a HashMap key; confusing sorting complexity with counting complexity; assuming lowercase input when the contract does not guarantee it.$$,
$$For Unicode, use code-point frequency maps. For distributed processing, partition by the canonical signature so equal groups reach the same worker. If only counts are needed, avoid retaining all original strings.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='group-anagrams'),$$What changes if uppercase letters and punctuation are allowed?$$,'VARIATION',1),
((SELECT id FROM problems WHERE slug='group-anagrams'),$$What is the complexity when each string is sorted first?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='group-anagrams'),$$How would you handle Unicode safely?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='group-anagrams'),$$How would you distribute grouping across multiple machines?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='group-anagrams'),$$How would you avoid storing every string if only group counts are needed?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='group-anagrams'),$$How would you protect against accidental signature collisions in a custom encoding?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='group-anagrams'),'6
eat tea tan ate nat bat','[[eat,tea,ate],[tan,nat],[bat]]',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='group-anagrams'),'3
abc bca cab','[[abc,bca,cab]]',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='group-anagrams'),'4
listen silent enlist google','[[listen,silent,enlist],[google]]',true,3);

-- 4 3Sum
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='3sum'),1,$$Sort the numbers, fix one value, and turn the remaining problem into a two-sum search.$$,'Concept'),
((SELECT id FROM problems WHERE slug='3sum'),2,$$Use left and right pointers. If the sum is too small move left upward; if too large move right downward. Skip duplicates.$$,'Direction'),
((SELECT id FROM problems WHERE slug='3sum'),3,$$For every anchor i, scan i+1..n-1 with two pointers. After a match, move both pointers and skip equal values.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='3sum'),
$$3Sum is a fixed-anchor plus two-pointers pattern. Sorting turns pair-sum comparisons into safe monotonic pointer movement.$$,
$$Keywords: triplets, target sum, unique combinations. Ask whether fixing one element reduces the problem to two-sum over sorted remaining values.$$,
$$Use when sorting is allowed and unique pairs/triplets are required by value.$$,
$$Do not use when original indices must remain tied to unsorted positions or when sorting destroys required information.$$,
$$Sort first. Fix nums[i]. The other two values must sum to the negative of nums[i]. Because the rest is sorted, move pointers according to the current sum.$$,
$$Sorting costs O(n log n). There are O(n) anchors and each anchor performs an O(n) two-pointer scan, producing O(n squared) total time.$$,
$$Sort the array, skip duplicate anchors, then use left/right pointers. On a zero sum record the triplet and skip duplicate pointer values.$$,
$$Check all triples directly. This takes O(n cubed) time and is infeasible for large n.$$,
'O(n^3)','O(1) auxiliary excluding output',
$$Sort once and solve one sorted two-sum problem for every anchor.$$,
'O(n^2)','O(1) auxiliary excluding output; output may be O(n^2)',
$$FUNCTION threeSum(nums)
    sort nums
    result = []
    FOR i = 0..n-3
        IF i>0 AND nums[i]==nums[i-1] CONTINUE
        left=i+1; right=n-1
        WHILE left<right
            sum=nums[i]+nums[left]+nums[right]
            IF sum==0
                add triplet
                skip equal left and right values
                left++; right--
            ELSE IF sum<0
                left++
            ELSE
                right--
    RETURN result$$,
$$For a fixed anchor, sorted order means a smaller sum can only be increased by moving left rightward, while a larger sum can only be decreased by moving right leftward. Thus an entire set of pairs can be eliminated at each move.$$,
$$For each anchor, every pair outside the current pointer interval has already been proven unable to create a new valid triplet for that anchor.$$,
$$Forgetting to sort; returning duplicate triplets; skipping the wrong side after a match; confusing unique values with unique indices; overlooking integer overflow for extreme numeric constraints.$$,
$$Generalize to arbitrary target. 4Sum fixes two anchors and then uses two pointers. If output can be quadratic, output size itself is a lower bound. Independent anchor ranges can be parallelized with deterministic merge/deduplication.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='3sum'),$$How would you change the algorithm for an arbitrary target?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='3sum'),$$Why does sorting make the pointer movements safe?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='3sum'),$$How would you extend the idea to 4Sum?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='3sum'),$$How do you guarantee duplicate triplets are removed?$$,'VARIATION',4),
((SELECT id FROM problems WHERE slug='3sum'),$$What is the worst-case output size?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='3sum'),$$How could the anchor ranges be parallelized?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='3sum'),'6
-1 0 1 2 -1 -4','[[-1,-1,2],[-1,0,1]]',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='3sum'),'3
0 0 0','[[0,0,0]]',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='3sum'),'5
0 1 1 2 2','[]',true,3);

-- 5 Search in Rotated Sorted Array
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='search-rotated-array'),1,$$Rotation breaks global ordering, but one half around the midpoint is still sorted when values are distinct.$$,'Concept'),
((SELECT id FROM problems WHERE slug='search-rotated-array'),2,$$Identify the sorted half using nums[left] <= nums[mid]. Check whether the target's value lies inside that half before deciding which side to discard.$$,'Direction'),
((SELECT id FROM problems WHERE slug='search-rotated-array'),3,$$Run binary search. If left half is sorted and target is inside its range, move right=mid-1; otherwise search the other half. Mirror the logic for a sorted right half.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='search-rotated-array'),
$$This is binary search with a modified decision rule. A rotated sorted array with distinct values always has at least one sorted half.$$,
$$Keywords: sorted then rotated, distinct values, O(log n), search target. Ask whether one half can be proven sorted and therefore used to eliminate half the search space.$$,
$$Use when the original sorted structure is preserved except for a rotation and values are distinct.$$,
$$Do not use ordinary this variant when duplicates can make both boundary comparisons ambiguous; use a duplicate-aware version instead.$$,
$$At each midpoint, either left..mid or mid..right is sorted. If the target lies inside the sorted half's value range, search there; otherwise discard it.$$,
$$Compare nums[left] and nums[mid]. If left <= mid, the left half is sorted. Otherwise the right half is sorted. Use inclusive range checks to decide where target can exist.$$,
$$Iterative modified binary search identifies the sorted half, checks whether target lies in that half, and eliminates the impossible half.$$,
$$Linear scan checks every element and takes O(n). It ignores the sorted structure.$$,
'O(n)','O(1)',
$$Use one binary-search iteration per eliminated half. Distinct values guarantee one half is sorted at every step.$$,
'O(log n)','O(1)',
$$FUNCTION search(nums,target)
    left=0; right=n-1
    WHILE left<=right
        mid=left+(right-left)/2
        IF nums[mid]==target RETURN mid
        IF nums[left]<=nums[mid]
            IF nums[left]<=target AND target<nums[mid]
                right=mid-1
            ELSE
                left=mid+1
        ELSE
            IF nums[mid]<target AND target<=nums[right]
                left=mid+1
            ELSE
                right=mid-1
    RETURN -1$$,
$$If the left half is sorted, range comparison tells whether target can be there; if not, the target must be in the other half. The right-sorted case is symmetric. Thus the target remains inside the interval until found or the interval becomes empty.$$,
$$If target exists, it remains inside [left,right] after every iteration.$$,
$$Using ordinary binary search; incorrect inclusive boundaries; forgetting the distinct-value assumption; mixing the left-sorted and right-sorted conditions.$$,
$$With duplicates, if nums[left]==nums[mid]==nums[right], shrinking both boundaries may be necessary and worst-case time can become O(n). The same structure can also find the rotation minimum.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$Why is at least one half sorted after rotation?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$How do duplicates change the algorithm?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$How can you find the minimum element in a rotated array?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$What is the worst case with duplicates?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$How would you test every possible rotation point?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='search-rotated-array'),$$How is this related to binary search over a monotonic predicate?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='search-in-rotated-array'),'7
4 5 6 7 0 1 2
0','4',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='search-in-rotated-array'),'7
4 5 6 7 0 1 2
3','-1',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='search-in-rotated-array'),'5
1 3 5 7 9
7','3',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='search-in-rotated-array'),'6
6 7 0 1 2 4
4','5',true,4);

-- 6 Daily Temperatures
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='daily-temperatures'),1,$$You need the first greater temperature to the right. That is a next-greater-element problem.$$,'Concept'),
((SELECT id FROM problems WHERE slug='daily-temperatures'),2,$$Keep indices waiting for a warmer day. When today's temperature is greater than the stack top's temperature, today's index resolves that earlier day.$$,'Direction'),
((SELECT id FROM problems WHERE slug='daily-temperatures'),3,$$Scan left-to-right with a decreasing stack of indices. Pop while current temperature is greater and store currentIndex-poppedIndex.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='daily-temperatures'),
$$Daily Temperatures is a monotonic-stack problem. The phrase first greater value to the right is the strongest recognition signal.$$,
$$Keywords: next greater, first future value satisfying a comparison, days until warmer. Ask whether unresolved previous positions can be held until a future value resolves them.$$,
$$Use for nearest or first greater/smaller queries where a monotonic stack can discard dominated candidates.$$,
$$Do not use when all future matches are required or when no monotonic elimination is available.$$,
$$Imagine each day waiting for a warmer future day. Keep waiting days in a stack. One current day can resolve several waiting days, so pop repeatedly.$$,
$$Store indices rather than temperatures because the answer is a distance. The stack remains monotonic by temperature, and every index is pushed and popped at most once.$$,
$$Scan left-to-right. For each i, pop while temperatures[i] is greater than temperatures[stack.peek()]. Set answer[popped]=i-popped, then push i.$$,
$$For every day scan forward until finding a warmer day. Decreasing input causes O(n squared) work.$$,
'O(n^2)','O(1) auxiliary excluding output',
$$Use a decreasing monotonic stack of indices. Each index is pushed once and popped at most once.$$,
'O(n)','O(n)',
$$FUNCTION dailyTemperatures(t)
    answer = zeros(n)
    stack = empty
    FOR i=0..n-1
        WHILE stack not empty AND t[i] > t[stack.top]
            j=stack.pop
            answer[j]=i-j
        PUSH i
    RETURN answer$$,
$$When index j is popped at i, no earlier index between j and i had a warmer temperature, otherwise j would already have been popped. Therefore i is its first warmer day.$$,
$$The stack contains unresolved indices in increasing index order and non-increasing temperature order.$$,
$$Storing values instead of indices; using >= when equal temperature is not warmer; forgetting unresolved answers stay zero; assuming the while loop makes the algorithm quadratic.$$,
$$Reverse scanning is an equivalent formulation. The same abstraction supports next smaller and previous greater/smaller. Streaming output is possible for resolved indices, while unresolved state must be retained.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$Why is the monotonic-stack algorithm O(n) despite the nested while loop?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$How would you solve next smaller instead of next greater?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$Can you scan from right to left?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$What changes if equal temperatures count as warmer?$$,'FOLLOWUP',4),
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$How would you expose answers incrementally in a stream?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='daily-temperatures'),$$How would you build one reusable monotonic-stack utility?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='daily-temperatures'),'8
73 74 75 71 69 72 76 73','[1,1,4,2,1,1,0,0]',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='daily-temperatures'),'5
30 40 50 60 70','[1,1,1,1,0]',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='daily-temperatures'),'5
70 69 68 67 66','[0,0,0,0,0]',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='daily-temperatures'),'4
30 30 31 30','[2,1,0,0]',true,4);

-- 7 Merge Two Sorted Lists
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),1,$$Both list heads are the smallest remaining candidates. Compare them and attach the smaller node.$$,'Concept'),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),2,$$Use a dummy head and tail pointer. Link the smaller head, advance that list, and repeat until one list ends.$$,'Direction'),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),3,$$After the loop, attach the remaining suffix directly. Reuse existing nodes instead of allocating a new node for every value.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),
$$Merging sorted linked lists is pointer manipulation. The next smallest output node must be one of the two current heads.$$,
$$Keywords: two sorted lists, merge, ordered result, linked-list pointers. Ask whether the next candidate is exposed at one of a few current pointers.$$,
$$Use when inputs are sorted and can be traversed sequentially, especially when nodes can be relinked.$$,
$$Do not use the in-place version if input nodes are shared and must remain immutable.$$,
$$Treat the two list heads as two queues. The smaller head is guaranteed to be the next node in sorted order.$$,
$$Compare heads, append the smaller, advance only that list, and repeat. A dummy node avoids a special case for the first result node.$$,
$$Iterative merge with a dummy node and tail pointer. When one list is exhausted, attach the remaining suffix.$$,
$$Copy values into an array, sort, and rebuild. This wastes the existing ordering and uses O((m+n) log(m+n)) time.$$,
'O((m+n) log(m+n))','O(m+n)',
$$Relink nodes in one pass without allocating replacement nodes.$$,
'O(m+n)','O(1)',
$$FUNCTION merge(a,b)
    dummy = new Node
    tail = dummy
    WHILE a != null AND b != null
        IF a.val <= b.val
            tail.next=a; a=a.next
        ELSE
            tail.next=b; b=b.next
        tail=tail.next
    tail.next = a if a != null else b
    RETURN dummy.next$$,
$$Because each list is sorted, its head is the smallest remaining element in that list. Therefore the smaller of the two heads is globally safe to append.$$,
$$The list after dummy contains exactly the smallest nodes removed from the two inputs so far and remains sorted.$$,
$$Losing dummy.next; forgetting to advance a pointer; forgetting the remaining suffix; unnecessary node allocation; missing null-list cases.$$,
$$Merge k lists with a min-heap in O(N log k). Recursive two-list merge is elegant but consumes call-stack space. Preserve immutability by copying nodes when ownership requires it.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$Can you implement the merge recursively?$$,'VARIATION',1),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$How would you merge k sorted linked lists?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$What changes if input nodes must remain immutable?$$,'SENIOR',3),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$How would you test pointer correctness, not just output values?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$Can the same two-pointer idea merge sorted arrays?$$,'FOLLOWUP',5),
((SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),$$How many node visits does the iterative solution perform?$$,'FOLLOWUP',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),'3
1 2 4
3
1 3 4','1 1 2 3 4 4',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),'1
2
1
1','1 2',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='merge-two-sorted-lists'),'0

3
1 2 3','1 2 3',true,3);

-- 8 Binary Tree Level Order Traversal
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),1,$$Level order means process the tree depth by depth. FIFO queue processing matches that requirement.$$,'Concept'),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),2,$$At the start of a level, capture queue.size(). Process exactly that many nodes; their children belong to the next level.$$,'Direction'),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),3,$$BFS with ArrayDeque: pop levelSize nodes, collect their values, enqueue non-null children, and append the collected list.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),
$$Level-order traversal is the canonical tree BFS problem. Level by level and same depth are strong recognition clues.$$,
$$Keywords: level order, breadth, depth, same layer, closest level. Ask whether all nodes at depth d must be processed before depth d+1.$$,
$$Use when output or computation depends on tree depth or when breadth-first exploration is useful.$$,
$$Do not prefer BFS when only a recursive subtree aggregate is needed and depth grouping adds no value.$$,
$$The queue is the current frontier. Process the entire frontier, then its children become the next frontier.$$,
$$Capture the queue size before processing a level. That count separates current-level nodes from children added during processing.$$,
$$Use an ArrayDeque queue. For each level, process exactly the initial queue size and enqueue left/right children.$$,
$$A DFS solution can also collect values by depth and is still O(n), so it is not asymptotically worse. The important distinction is traversal order and auxiliary memory.$$,
'O(n)','O(h) auxiliary plus output for recursive DFS',
$$BFS processes every node exactly once and naturally exposes one level at a time.$$,
'O(n)','O(w) auxiliary, where w is maximum tree width',
$$FUNCTION levelOrder(root)
    result=[]
    IF root==null RETURN result
    queue=[root]
    WHILE queue not empty
        levelSize=queue.size()
        level=[]
        REPEAT levelSize times
            node=queue.removeFirst()
            add node.val to level
            enqueue node.left if non-null
            enqueue node.right if non-null
        add level to result
    RETURN result$$,
$$At the beginning of each outer iteration, the queue contains exactly the unprocessed nodes at one depth. Children are added only for the next iteration, so the output is correctly partitioned by depth.$$,
$$At the beginning of each level iteration, every node in the queue has the same depth.$$,
$$Processing dynamically changing queue.size(); forgetting null root; using a recursive implementation without considering deep-tree stack depth; using a queue structure with unnecessary synchronization.$$,
$$DFS can use O(h) call stack. Zigzag order can reverse alternating level lists. Very wide trees can require O(n) BFS memory, and very deep trees may favor iterative traversal to avoid JVM stack overflow.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$Why must queue.size() be captured before processing the level?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$Can you implement level order using DFS?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$How would you return zigzag level order?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$What is the maximum BFS queue size for a complete binary tree?$$,'FOLLOWUP',4),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$How would you handle a tree too deep for recursive DFS on the JVM?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='binary-tree-level-order'),$$How would you process a tree too large for one machine's memory?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-level-order'),'3 9 20 null null 15 7','[[3],[9,20],[15,7]]',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-level-order'),'1 null 2','[[1],[2]]',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-level-order'),'','[]',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-level-order'),'1 2 3 4 5 6 7','[[1],[2,3],[4,5,6,7]]',true,4);

-- 9 Kth Largest Element
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='kth-largest-element'),1,$$You need one rank, not a complete ordering. Keep only the k largest candidates seen so far.$$,'Concept'),
((SELECT id FROM problems WHERE slug='kth-largest-element'),2,$$A min-heap of size k keeps the kth-largest boundary at its root. Values smaller than the root cannot enter the top k.$$,'Direction'),
((SELECT id FROM problems WHERE slug='kth-largest-element'),3,$$Offer every value into a min-heap. When size exceeds k, remove the minimum. The root at the end is the kth largest.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='kth-largest-element'),
$$Kth Largest is a top-k/order-statistic problem. The key clue is that a complete sort is unnecessary when only one rank is required.$$,
$$Keywords: kth largest, kth smallest, top k, rank. Ask whether maintaining a bounded set of the best k candidates is enough.$$,
$$Use when k largest/smallest values are needed, especially when k is small or data arrives as a stream.$$,
$$Do not use the heap simply because it is familiar when a complete sort is already required or a strict worst-case linear selection is the real requirement.$$,
$$Keep the k largest values. A min-heap exposes the smallest among those k, which is exactly the boundary that a new value must beat to enter the set.$$,
$$Every time the heap grows beyond k, remove its minimum. This maintains exactly the k largest values among everything processed so far.$$,
$$Use PriorityQueue as a min-heap, keep its size at most k, and return heap.peek() after the scan.$$,
$$Sort the entire array and return position n-k. This is correct but performs more ordering work than needed.$$,
'O(n log n)','O(log n) to O(n) depending on sort implementation',
$$Maintain a min-heap of size k. Each operation costs O(log k), giving O(n log k).$$,
'O(n log k)','O(k)',
$$FUNCTION kthLargest(nums,k)
    heap = empty minHeap
    FOR x in nums
        heap.add(x)
        IF heap.size > k
            heap.removeMin()
    RETURN heap.peek()$$,
$$After every processed prefix, the heap contains exactly the k largest values from that prefix, or all values if fewer than k have been processed. Therefore its minimum is the kth largest after the full scan.$$,
$$After processing any prefix, the heap contains the k largest processed values, bounded by the number processed when it is less than k.$$,
$$Using a max-heap and removing the maximum; forgetting duplicates count; claiming O(log n) rather than O(log k); not validating k range.$$,
$$Quickselect has expected O(n) time and O(1) auxiliary space but different worst-case behavior. The heap is naturally streaming. For distributed data, compute local top-k and merge those candidates into a global top-k.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$What happens when k is close to n?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$How would Quickselect change the complexity?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$How would you solve it for a stream of values?$$,'VARIATION',3),
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$How would you find the kth smallest instead?$$,'FOLLOWUP',4),
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$How would you compute global top-k across distributed partitions?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='kth-largest-element'),$$What guarantees would you require from a production selection algorithm?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='kth-largest-element'),'6
3 2 1 5 6 4
2','5',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='kth-largest-element'),'5
3 2 3 1 2
4','2',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='kth-largest-element'),'4
7 7 7 7
2','7',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='kth-largest-element'),'1
1
1','1',true,4);

-- 10 Number of Islands
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='number-of-islands'),1,$$Each island is one connected component of land cells under four-direction adjacency.$$,'Concept'),
((SELECT id FROM problems WHERE slug='number-of-islands'),2,$$Scan every cell. When you find unvisited land, increment the count and flood-fill that entire component with BFS or DFS.$$,'Direction'),
((SELECT id FROM problems WHERE slug='number-of-islands'),3,$$Use an ArrayDeque for iterative BFS. Mark a land cell visited when enqueuing it so no cell enters the queue more than once.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,content_status)
VALUES
((SELECT id FROM problems WHERE slug='number-of-islands'),
$$Number of Islands is the canonical connected-components-on-a-grid problem. Count each connected region once.$$,
$$Keywords: grid, islands, connected land, components, flood fill, adjacent cells. Ask whether each answer unit is a connected component under a fixed neighborhood.$$,
$$Use for static grid connected components with local adjacency. BFS and DFS both work.$$,
$$Do not use plain component counting when the task is shortest path distance, dynamic connectivity, or a different adjacency definition.$$,
$$The first unvisited land cell of an island is enough to discover the entire island. Flood-fill it so later cells from that island cannot be counted again.$$,
$$The outer scan finds component roots. Each BFS/DFS marks every reachable land cell. Because cells are marked when discovered, every cell is processed at most once.$$,
$$Use iterative BFS with four directions. Mutate land to water when visited, or use a separate boolean matrix if the input must remain unchanged.$$,
$$Repeatedly traverse components without global visited state can revisit the same island many times and become quadratic or worse.$$,
'O((mn)^2) worst case without global visited','O(mn)',
$$Scan all cells and flood-fill every unvisited land component exactly once. Each cell has four constant-degree neighbors.$$,
'O(mn)','O(mn) worst case for queue or visited state',
$$FUNCTION numIslands(grid)
    count=0
    FOR every cell r,c
        IF grid[r][c]=='1'
            count++
            queue.add(r,c)
            grid[r][c]='0'
            WHILE queue not empty
                cell=queue.removeFirst()
                FOR four directions
                    if neighbor is in bounds and is land
                        mark visited
                        queue.add(neighbor)
    RETURN count$$,
$$A traversal never crosses water, so it stays inside one component. Marking immediately prevents repeated discovery. Therefore each island contributes exactly one count and every land cell is processed once.$$,
$$Every cell already marked visited belongs to a discovered component, and no unvisited land cell in the active component can be reached without being discovered by the traversal.$$,
$$Counting diagonal cells as connected when only four directions are allowed; marking after enqueue rather than before; recursive DFS stack overflow on huge grids; mutating input without permission.$$,
$$If land is added dynamically, Union-Find can maintain component counts. For huge distributed grids, partitioning needs boundary reconciliation because components can cross partition edges. Use a visited matrix when input mutation is forbidden.$$,
'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='number-of-islands'),$$Can you solve it with DFS instead of BFS?$$,'VARIATION',1),
((SELECT id FROM problems WHERE slug='number-of-islands'),$$What changes if diagonal adjacency counts?$$,'VARIATION',2),
((SELECT id FROM problems WHERE slug='number-of-islands'),$$How do you preserve the original grid?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='number-of-islands'),$$How would you support land being added dynamically?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='number-of-islands'),$$How would you process a grid too large for one machine?$$,'SENIOR',5),
((SELECT id FROM problems WHERE slug='number-of-islands'),$$How would you avoid JVM stack overflow for a huge connected component?$$,'SENIOR',6)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='number-of-islands'),'4 5
1 1 1 1 0
1 1 0 1 0
1 1 0 0 0
0 0 0 0 0','1',false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='number-of-islands'),'4 5
1 1 0 0 0
1 1 0 0 0
0 0 1 0 0
0 0 0 1 1','3',false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='number-of-islands'),'1 1
1','1',true,3),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='number-of-islands'),'3 3
1 0 1
0 1 0
1 0 1','5',true,4);

-- Final migration guard.
DO $$
DECLARE missing_count integer;
BEGIN
  SELECT COUNT(*) INTO missing_count
  FROM problems p
  WHERE p.slug IN (
    'maximum-subarray','product-of-array-except-self','group-anagrams','3sum',
    'search-rotated-array','daily-temperatures','merge-two-sorted-lists',
    'binary-tree-level-order','kth-largest-element','number-of-islands'
  )
  AND NOT EXISTS (SELECT 1 FROM problem_content pc WHERE pc.problem_id=p.id);

  IF missing_count > 0 THEN
    RAISE EXCEPTION 'V17 batch content missing for % problems', missing_count;
  END IF;
END $$;
