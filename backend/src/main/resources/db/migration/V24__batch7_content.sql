-- V24: Batch 7 — gold-standard content for 20 problems: 10 linked-list + 10 stack.
-- Gold-standard content; all problems remain in CONTENT_REVIEW until technical validation.

-- ══════════════════════════════════════════════════════════════════════════════
-- LINKED LIST (1–10)
-- ══════════════════════════════════════════════════════════════════════════════

-- 1 Middle of the Linked List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='middle-linked-list'),1,$$To find the middle without knowing the length, use two pointers moving at different speeds so the fast one reaches the end when the slow one is at the middle.$$,'Concept'),
((SELECT id FROM problems WHERE slug='middle-linked-list'),2,$$Move slow one step and fast two steps per iteration. When fast reaches null (or fast.next is null), slow is at the middle.$$,'Direction'),
((SELECT id FROM problems WHERE slug='middle-linked-list'),3,$$Trace the invariant: fast always leads slow by exactly the same ratio, so when fast is done, slow is at the midpoint.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$Return the middle node of a singly linked list (second middle for even length).$$,$$Keywords: Middle Linked List; slow-fast pointers; single pass. Two-pointer technique avoids a separate length pass.$$,$$Use when you need a midpoint in a single traversal without knowing the length.$$,$$Do not count length first — that requires two passes.$$,$$Move slow one step and fast two steps per iteration. When fast reaches null (or fast.next is null), slow is at the middle.$$,$$Move slow one step and fast two steps per iteration. When fast reaches null (or fast.next is null), slow is at the middle.$$,$$Slow-fast pointer technique.$$,$$Count nodes then traverse to n/2 requires two passes.$$,'O(n)','O(1)',$$Slow-fast pointer technique.$$,'O(n)','O(1)',$$slow=head; fast=head; while fast!=null and fast.next!=null: slow=slow.next; fast=fast.next.next; return slow$$,$$Fast moves twice as far as slow; when fast finishes, slow has covered exactly half the list.$$,$$At every step, fast is twice as far from head as slow.$$,$$Stopping when fast.next==null vs fast==null shifts the result by one node for even-length lists; choose based on problem definition.$$,$$Used as a subroutine in Sort List and Palindrome Linked List. The same 2:1 speed ratio detects cycles (Floyd's algorithm).$$,$$public ListNode middleNode(ListNode h){ListNode s=h,f=h;while(f!=null&&f.next!=null){s=s.next;f=f.next.next;}return s;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$Why does stopping at fast==null give the second middle for even-length lists?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$How would you find the node one step before the middle?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$How is this pattern reused in Sort List?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$How would you find the k/n-th node from the end?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='middle-linked-list'),$$How would you use this in a merge sort implementation on a linked list?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='middle-linked-list'),$$5
1 2 3 4 5$$,$$3$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='middle-linked-list'),$$6
1 2 3 4 5 6$$,$$4$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='middle-linked-list'),$$1
1$$,$$1$$,true,3);

-- 2 Delete Node in a Linked List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='delete-node-list'),1,$$You cannot reach the previous node because only the node to delete is given. Instead, overwrite the node's value with the next node's value and skip the next node.$$,'Concept'),
((SELECT id FROM problems WHERE slug='delete-node-list'),2,$$Copy node.next.val into node.val, then set node.next = node.next.next.$$,'Direction'),
((SELECT id FROM problems WHERE slug='delete-node-list'),3,$$The invariant: after the operation, the node effectively becomes its successor, and the successor is unlinked.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='delete-node-list'),$$Delete a given node from a singly linked list without access to the head.$$,$$Keywords: Delete Node; no head access; value copy. When the previous node is unreachable, copy-and-skip is the only O(1) option.$$,$$Use when only the target node is provided and it is guaranteed not to be the tail.$$,$$Do not attempt to reach the previous node — the head is unavailable.$$,$$Copy node.next.val into node.val, then set node.next = node.next.next.$$,$$Copy node.next.val into node.val, then set node.next = node.next.next.$$,$$Copy successor value and skip the successor.$$,$$There is no slower alternative; traversal from head is impossible without the head.$$,'N/A','N/A',$$Copy successor value and skip the successor.$$,'O(1)','O(1)',$$node.val=node.next.val; node.next=node.next.next$$,$$Copying the value makes the current node indistinguishable from its successor; unlinking the successor removes the duplicate.$$,$$After the operation, the current node holds the successor's value and the successor is removed.$$,$$Forgetting that the node cannot be the tail (problem guarantees this); modifying only val without updating next, leaving a duplicate.$$,$$This trick does not work for the tail node. For a doubly linked list the previous pointer allows standard deletion.$$,$$public void deleteNode(ListNode n){n.val=n.next.val;n.next=n.next.next;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='delete-node-list'),$$Why can you not delete the tail node with this technique?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='delete-node-list'),$$How would this differ in a doubly linked list?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='delete-node-list'),$$What happens if there are external references to node.next?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='delete-node-list'),$$How would you extend this to delete k consecutive nodes starting at the given node?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='delete-node-list'),$$Is it possible to truly delete the node (not just its value) without the head?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='delete-node-list'),$$4
1 2 3 4
2$$,$$[1,3,4]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='delete-node-list'),$$4
0 1 2 3
0$$,$$[1,2,3]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='delete-node-list'),$$2
1 2
1$$,$$[2]$$,true,3);

-- 3 Intersection of Two Linked Lists
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),1,$$If the two lists intersect, after the intersection point they share the same nodes. The key insight is to equalize the distances each pointer travels.$$,'Concept'),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),2,$$When a pointer reaches the end of its list, redirect it to the head of the other list. Both pointers will have traveled the same total distance when they meet.$$,'Direction'),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),3,$$Trace the invariant: after the redirect, both pointers have the intersection node at the same remaining distance.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$Find the node at which two singly linked lists intersect, or return null.$$,$$Keywords: Intersection Linked Lists; two pointers; equalize path length. Redirecting pointers at list ends makes them arrive at the intersection simultaneously.$$,$$Use when two lists may share a tail and you need the exact merge node.$$,$$Do not use a HashSet of visited nodes — the two-pointer approach is O(1) space.$$,$$When a pointer reaches the end of its list, redirect it to the head of the other list. Both pointers will have traveled the same total distance when they meet.$$,$$When a pointer reaches the end of its list, redirect it to the head of the other list. Both pointers will have traveled the same total distance when they meet.$$,$$Two-pointer redirect technique.$$,$$HashSet of visited nodes from list A; scan list B for first hit: O(m+n) time O(m) space.$$,'O(m+n)','O(m)',$$Two-pointer redirect technique.$$,'O(m+n)','O(1)',$$a=headA; b=headB; while a!=b: a=a!=null?a.next:headB; b=b!=null?b.next:headA; return a$$,$$Both pointers travel a+b+c or b+a+c total steps (where c is the shared tail length), so they arrive at the intersection simultaneously.$$,$$After at most m+n steps both pointers point to the same node (intersection or both null).$$,$$Comparing node values instead of references; infinite loop when lists don't intersect (both reach null simultaneously, which is also equal).$$,$$Length difference method: compute lengths, advance the longer list's pointer, then walk both together. Same O(m+n) time but explicit.$$,$$public ListNode getIntersectionNode(ListNode a,ListNode b){ListNode p=a,q=b;while(p!=q){p=p!=null?p.next:b;q=q!=null?q.next:a;}return p;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$Why do both pointers meet at the intersection (or both reach null)?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$How does the length-difference approach work?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$What if the lists intersect but share no tail (e.g., a cycle is involved)?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$How would you find the intersection of three lists?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$How would you detect if two lists share any node, not just a tail?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$4 1
1 9 1 2 4
3 2 4
2$$,$$2$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$2 3
0 9 1
3 2 4 2 4
-1$$,$$null$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='intersection-linked-lists'),$$1 1
2 6 4
1 5
-1$$,$$null$$,true,3);

-- 4 Palindrome Linked List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),1,$$A palindrome reads the same forwards and backwards. For a linked list, find the middle, reverse the second half, then compare both halves.$$,'Concept'),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),2,$$Use slow-fast to find the middle, reverse the second half in-place, walk both halves comparing values, then optionally restore.$$,'Direction'),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),3,$$Trace the invariant: after reversal, the two halves mirror each other if the list is a palindrome.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$Determine whether a singly linked list is a palindrome in O(n) time and O(1) space.$$,$$Keywords: Palindrome Linked List; reverse second half; slow-fast pointer. Combining middle-finding and in-place reversal achieves O(1) space.$$,$$Use when O(1) space is required and mutation is acceptable.$$,$$Do not copy values to an array — that uses O(n) space.$$,$$Use slow-fast to find the middle, reverse the second half in-place, walk both halves comparing values, then optionally restore.$$,$$Use slow-fast to find the middle, reverse the second half in-place, walk both halves comparing values, then optionally restore.$$,$$Find middle → reverse second half → compare → restore.$$,$$Copy values to array and check palindrome: O(n) time O(n) space.$$,'O(n)','O(n)',$$Find middle → reverse second half → compare → restore.$$,'O(n)','O(1)',$$slow-fast to find mid; reverse from mid; compare head and reversed; restore (optional)$$,$$Reversing the second half lets you compare it directly with the first half using two pointers from each end meeting in the middle.$$,$$During comparison, first and second pointers advance together; a mismatch means it is not a palindrome.$$,$$Off-by-one in middle finding for even-length lists; not handling the middle node correctly when length is odd; forgetting restoration.$$,$$Stack-based comparison is simpler but O(n) space. Recursive approach also O(n) stack space. The in-place method is the canonical O(1) solution.$$,$$public boolean isPalindrome(ListNode h){ListNode s=h,f=h;while(f!=null&&f.next!=null){s=s.next;f=f.next.next;}ListNode r=rev(s);ListNode p=h,q=r;boolean ok=true;while(q!=null){if(p.val!=q.val){ok=false;break;}p=p.next;q=q.next;}rev(r);return ok;}ListNode rev(ListNode n){ListNode p=null;while(n!=null){ListNode nx=n.next;n.next=p;p=n;n=nx;}return p;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$Why restore the list after checking?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$How does the middle-finding affect odd vs even length?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$What is the stack-based approach and its trade-off?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$How would you check palindrome on a doubly linked list with O(1) space?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$How would you find the longest palindromic subsequence in a linked list?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$5
1 2 2 2 1$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$4
1 2 2 1$$,$$true$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='palindrome-linked-list'),$$4
1 2 3 4$$,$$false$$,true,3);

-- 5 Remove Nth Node From End of List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='remove-nth-node'),1,$$To find the n-th node from the end in one pass, use two pointers separated by n nodes. When the fast pointer reaches the end, the slow pointer is at the target.$$,'Concept'),
((SELECT id FROM problems WHERE slug='remove-nth-node'),2,$$Advance fast by n+1 steps first (using a dummy head), then move both together. When fast is null, slow.next is the node to remove.$$,'Direction'),
((SELECT id FROM problems WHERE slug='remove-nth-node'),3,$$Trace the invariant: the gap between fast and slow is always exactly n+1 nodes.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$Remove the n-th node from the end of a linked list in one pass.$$,$$Keywords: Remove Nth Node; two pointers; gap trick. A fixed gap between two pointers finds the n-th from end in one traversal.$$,$$Use when you need to find a position relative to the tail in one pass.$$,$$Do not count length first; that requires two passes.$$,$$Advance fast by n+1 steps first (using a dummy head), then move both together. When fast is null, slow.next is the node to remove.$$,$$Advance fast by n+1 steps first (using a dummy head), then move both together. When fast is null, slow.next is the node to remove.$$,$$Two-pointer gap trick with dummy head.$$,$$Count length, compute index from front, traverse: two passes O(n).$$,'O(n)','O(1)',$$Two-pointer gap trick with dummy head.$$,'O(n)','O(1)',$$dummy->head; fast=slow=dummy; advance fast n+1 steps; move both until fast==null; slow.next=slow.next.next$$,$$The n+1 gap means when fast falls off the end, slow is positioned one before the target, enabling clean deletion.$$,$$The gap between fast and slow is always exactly n+1 nodes.$$,$$Advancing fast by n instead of n+1 (slow lands on target, not before it); not using a dummy for head-deletion edge cases.$$,$$The dummy node removes the edge case where the head itself is deleted. The gap trick generalises to any k-th from end.$$,$$public ListNode removeNthFromEnd(ListNode h,int n){ListNode d=new ListNode(0,h),s=d,f=d;for(int i=0;i<=n;i++)f=f.next;while(f!=null){s=s.next;f=f.next;}s.next=s.next.next;return d.next;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$Why advance fast by n+1 rather than n?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$Why use a dummy head node?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$How would you find the k-th node from the end without deleting it?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$How would you remove all nodes at distance n from the end?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='remove-nth-node'),$$How would you adapt this for a doubly linked list?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-nth-node'),$$5
1 2 3 4 5
2$$,$$[1,2,3,5]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-nth-node'),$$1
1
1$$,$$[]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-nth-node'),$$2
1 2
1$$,$$[1]$$,true,3);

-- 6 Odd Even Linked List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),1,$$Collect all odd-indexed nodes followed by all even-indexed nodes by relinking in place — no extra space needed.$$,'Concept'),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),2,$$Maintain two pointers: one for the tail of the odd chain and one for the tail of the even chain. Advance both together, then connect odd tail to even head.$$,'Direction'),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),3,$$Trace the invariant: after each iteration odd points to the last odd-index node and even points to the last even-index node processed so far.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$Reorder a linked list so all odd-indexed nodes come before all even-indexed nodes, preserving relative order within each group.$$,$$Keywords: Odd Even Linked List; in-place relink; two chain pointers. Weaving two chains and connecting them at the end is the canonical O(1)-space approach.$$,$$Use when splitting a list into two interleaved groups that must be concatenated in O(1) space.$$,$$Do not use extra storage or copy values — relink pointers.$$,$$Maintain two pointers: one for the tail of the odd chain and one for the tail of the even chain. Advance both together, then connect odd tail to even head.$$,$$Maintain two pointers: one for the tail of the odd chain and one for the tail of the even chain. Advance both together, then connect odd tail to even head.$$,$$Two-chain weaving with final concatenation.$$,$$Collecting node values and rebuilding: O(n) space.$$,'O(n)','O(n)',$$Two-chain weaving with final concatenation.$$,'O(n)','O(1)',$$odd=head; even=head.next; evenHead=even; while even!=null and even.next!=null: odd.next=even.next; odd=odd.next; even.next=odd.next; even=even.next; odd.next=evenHead$$,$$The two chains are woven through the list simultaneously; connecting odd tail to even head gives the required ordering.$$,$$odd always points to the last processed odd-index node; even always points to the last processed even-index node.$$,$$Losing the even head reference before the final connection; off-by-one in loop termination; breaking the chain for lists of length 1 or 2.$$,$$The same two-chain technique applies to separating by value (Partition List) or any binary predicate.$$,$$public ListNode oddEvenList(ListNode h){if(h==null)return h;ListNode o=h,e=h.next,eh=e;while(e!=null&&e.next!=null){o.next=e.next;o=o.next;e.next=o.next;e=e.next;}o.next=eh;return h;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$Why save the even head before the loop?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$How would you group by a value predicate instead of index parity?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$What happens with a list of length 1?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$How would you generalise to k groups while preserving relative order?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$How would you do this if even nodes must come before odd nodes?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$5
1 2 3 4 5$$,$$[1,3,5,2,4]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$6
2 1 3 5 6 4 7$$,$$[2,3,6,7,1,5,4]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='odd-even-linked-list'),$$1
1$$,$$[1]$$,true,3);

-- 7 Reverse Linked List II
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),1,$$Only the sublist from position left to right needs reversing. Identify the node before left and the node at right to perform a bounded reversal.$$,'Concept'),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),2,$$Walk to the node just before position left. Reverse the sublist using the standard insertion-at-front trick. Reconnect the reversed segment.$$,'Direction'),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),3,$$Trace the invariant: after each insertion step, the portion from left up to the current node is reversed and connected to the pre-left node.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$Reverse the nodes of a linked list from position left to right in one pass.$$,$$Keywords: Reverse Linked List II; partial reversal; insertion at front. Insert each node of the sublist at the front of the reversed segment one at a time.$$,$$Use when reversing a bounded sublist in a single pass without extra space.$$,$$Do not extract the sublist, reverse separately, and reattach — it works but requires careful pointer bookkeeping.$$,$$Walk to the node just before position left. Reverse the sublist using the standard insertion-at-front trick. Reconnect the reversed segment.$$,$$Walk to the node just before position left. Reverse the sublist using the standard insertion-at-front trick. Reconnect the reversed segment.$$,$$Dummy head + insertion-at-front reversal within the bounded range.$$,$$Extract sublist, reverse, reattach: same O(n) but more pointer steps.$$,'O(n)','O(1)',$$Dummy head + insertion-at-front reversal within the bounded range.$$,'O(n)','O(1)',$$dummy->head; pre=dummy; advance pre left-1 steps; curr=pre.next; for i in 1..right-left: next=curr.next; curr.next=next.next; next.next=pre.next; pre.next=next$$,$$Each iteration takes the node immediately after curr and inserts it just after pre, effectively building the reversed segment front-to-back.$$,$$After k iterations, the k nodes following the original left position appear in reversed order after pre.$$,$$Off-by-one in advancing pre; mutating pointers in wrong order causing lost nodes; not using a dummy for left==1 edge case.$$,$$Reverse Nodes in k-Group uses the same bounded reversal as a subroutine applied repeatedly.$$,$$public ListNode reverseBetween(ListNode h,int l,int r){ListNode d=new ListNode(0,h),pre=d;for(int i=1;i<l;i++)pre=pre.next;ListNode cur=pre.next;for(int i=0;i<r-l;i++){ListNode nx=cur.next;cur.next=nx.next;nx.next=pre.next;pre.next=nx;}return d.next;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$Why use a dummy head node?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$How does the insertion-at-front trick work step by step?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$How would you reverse the entire list with left=1, right=n?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$How does this subroutine extend to Reverse Nodes in k-Group?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$How would you reverse every other group of k nodes?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$5
1 2 3 4 5
2 4$$,$$[1,4,3,2,5]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$1
5
1 1$$,$$[5]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reverse-linked-list-ii'),$$3
1 2 3
1 3$$,$$[3,2,1]$$,true,3);

-- 8 Add Two Numbers
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='add-two-numbers'),1,$$The digits are stored in reverse order, so the head of each list is the least significant digit — addition proceeds naturally from head to tail.$$,'Concept'),
((SELECT id FROM problems WHERE slug='add-two-numbers'),2,$$Walk both lists simultaneously, summing digits and a carry. Create a new node for each digit of the result. Handle remaining nodes and a final carry.$$,'Direction'),
((SELECT id FROM problems WHERE slug='add-two-numbers'),3,$$Trace the invariant: after processing position i, the carry holds the overflow from the sum of all digits up to position i.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$Add two non-negative integers represented as reversed linked lists and return the sum as a reversed linked list.$$,$$Keywords: Add Two Numbers; carry; simultaneous traversal. Digit-by-digit addition with carry is the natural simulation of column addition.$$,$$Use when simulating arithmetic on digit sequences stored in lists.$$,$$Do not convert to integers — the lists may represent numbers too large for 64-bit integers.$$,$$Walk both lists simultaneously, summing digits and a carry. Create a new node for each digit of the result. Handle remaining nodes and a final carry.$$,$$Walk both lists simultaneously, summing digits and a carry. Create a new node for each digit of the result. Handle remaining nodes and a final carry.$$,$$Simultaneous traversal with carry propagation.$$,$$No meaningfully worse approach; traversal is always O(max(m,n)).$$,'O(max(m,n))','O(max(m,n))',$$Simultaneous traversal with carry propagation.$$,'O(max(m,n))','O(max(m,n)) for output',$$carry=0; dummy; cur=dummy; while l1 or l2 or carry: sum=carry+(l1.val if l1 else 0)+(l2.val if l2 else 0); carry=sum//10; cur.next=new Node(sum%10); advance pointers$$,$$Each position's digit is (sum of both list digits + carry) mod 10; carry propagates to the next position exactly as in manual addition.$$,$$After processing position i, carry contains the overflow from all positions 0..i.$$,$$Forgetting to handle remaining nodes after one list ends; forgetting the final carry node when both lists are exhausted but carry==1.$$,$$Add Two Numbers II stores digits in forward order — reverse both lists first, add, then reverse the result; or use stacks.$$,$$public ListNode addTwoNumbers(ListNode l1,ListNode l2){ListNode d=new ListNode(0),c=d;int carry=0;while(l1!=null||l2!=null||carry>0){int s=carry+(l1!=null?l1.val:0)+(l2!=null?l2.val:0);carry=s/10;c.next=new ListNode(s%10);c=c.next;if(l1!=null)l1=l1.next;if(l2!=null)l2=l2.next;}return d.next;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$Why does the reversed storage make addition simpler?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$How would you handle Add Two Numbers II (forward order)?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$Why must you handle the case where only carry remains after both lists end?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$How would you multiply two numbers represented as linked lists?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='add-two-numbers'),$$How would you add three or more numbers simultaneously?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='add-two-numbers'),$$3
2 4 3
3
5 6 4$$,$$[7,0,8]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='add-two-numbers'),$$1
0
1
0$$,$$[0]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='add-two-numbers'),$$7
9 9 9 9 9 9 9
4
9 9 9 9$$,$$[8,9,9,9,0,0,0,1]$$,true,3);

-- 9 Reorder List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='reorder-list'),1,$$The reordered list interleaves from the front and back. Split at the middle, reverse the second half, then merge the two halves alternately.$$,'Concept'),
((SELECT id FROM problems WHERE slug='reorder-list'),2,$$Use slow-fast to find the middle, reverse the second half, then merge by taking one node from each half alternately.$$,'Direction'),
((SELECT id FROM problems WHERE slug='reorder-list'),3,$$Trace the invariant: during the merge phase, each step places exactly one node from the first half and one from the second half.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='reorder-list'),$$Reorder a linked list from L0→L1→…→Ln to L0→Ln→L1→Ln-1→L2→… in-place.$$,$$Keywords: Reorder List; split + reverse + merge; three linked-list primitives. Combining middle-finding, reversal, and alternating merge achieves O(1) extra space.$$,$$Use when the required ordering is a front-back interleaving of the list.$$,$$Do not use an array or deque to index nodes — that uses O(n) space.$$,$$Use slow-fast to find the middle, reverse the second half, then merge by taking one node from each half alternately.$$,$$Use slow-fast to find the middle, reverse the second half, then merge by taking one node from each half alternately.$$,$$Find middle → reverse second half → alternating merge.$$,$$Copy to array, use two-pointer relink: O(n) space.$$,'O(n)','O(n)',$$Find middle → reverse second half → alternating merge.$$,'O(n)','O(1)',$$find mid; second=reverse(mid.next); mid.next=null; merge first and second alternately$$,$$Reversing the second half makes its head the original tail. Alternating merge produces the required interleaving.$$,$$During merge, first always has one more node than second (or they are equal), so second runs out first.$$,$$Not nullifying mid.next before reversal (creating a cycle); failing to advance both pointers correctly during merge.$$,$$This problem combines three fundamental linked-list operations — the same subroutines reappear in Sort List and Palindrome Linked List.$$,$$public void reorderList(ListNode h){ListNode s=h,f=h;while(f.next!=null&&f.next.next!=null){s=s.next;f=f.next.next;}ListNode r=rev(s.next);s.next=null;ListNode p=h;while(r!=null){ListNode nx=p.next,rn=r.next;p.next=r;r.next=nx;p=nx;r=rn;}}ListNode rev(ListNode n){ListNode p=null;while(n!=null){ListNode nx=n.next;n.next=p;p=n;n=nx;}return p;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='reorder-list'),$$Why must you null-terminate the first half before reversing the second?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='reorder-list'),$$What are the three fundamental linked-list operations used here?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='reorder-list'),$$How would you verify the result without modifying the list?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='reorder-list'),$$How would you reorder into L0→L2→L4→…→L5→L3→L1?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='reorder-list'),$$How would you generalise to k-interleaved groups?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reorder-list'),$$4
1 2 3 4$$,$$[1,4,2,3]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reorder-list'),$$5
1 2 3 4 5$$,$$[1,5,2,4,3]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='reorder-list'),$$2
1 2$$,$$[1,2]$$,true,3);

-- 10 Sort List
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='sort-list'),1,$$Merge sort is naturally suited to linked lists: splitting at the middle is O(n) and merging two sorted lists is O(n), giving O(n log n) with O(log n) stack space.$$,'Concept'),
((SELECT id FROM problems WHERE slug='sort-list'),2,$$Use slow-fast to split at the middle, recursively sort each half, then merge the two sorted halves.$$,'Direction'),
((SELECT id FROM problems WHERE slug='sort-list'),3,$$Trace the invariant: each recursive call returns a sorted sublist; merging two sorted sublists produces a larger sorted list.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='sort-list'),$$Sort a linked list in O(n log n) time and O(1) space (or O(log n) for recursive stack).$$,$$Keywords: Sort List; merge sort; linked list. Merge sort is the canonical O(n log n) algorithm for linked lists because random access is not required.$$,$$Use when O(n log n) sorting of a linked list is required.$$,$$Do not use quicksort on a linked list — pivot selection and partitioning are awkward and worst case is O(n^2).$$,$$Use slow-fast to split at the middle, recursively sort each half, then merge the two sorted halves.$$,$$Use slow-fast to split at the middle, recursively sort each half, then merge the two sorted halves.$$,$$Top-down merge sort: split → sort → merge.$$,$$Insertion sort is O(n^2).$$,'O(n^2)','O(1)',$$Top-down merge sort: split → sort → merge.$$,'O(n log n)','O(log n) recursive stack',$$find mid; split; left=sort(first half); right=sort(second half); return merge(left,right)$$,$$Splitting halves the problem size; merging sorted halves takes O(n); recurrence T(n)=2T(n/2)+O(n) solves to O(n log n).$$,$$Each recursive call returns a sorted list; after merge, the combined list is sorted.$$,$$Not null-terminating the first half before the second recursive call; incorrect middle split causing infinite recursion on length-1 lists.$$,$$Bottom-up merge sort avoids recursion stack: start with pairs, then quads, doubling until done — O(1) extra space.$$,$$public ListNode sortList(ListNode h){if(h==null||h.next==null)return h;ListNode s=h,f=h.next;while(f!=null&&f.next!=null){s=s.next;f=f.next.next;}ListNode mid=s.next;s.next=null;return merge(sortList(h),sortList(mid));}ListNode merge(ListNode a,ListNode b){ListNode d=new ListNode(0),c=d;while(a!=null&&b!=null){if(a.val<=b.val){c.next=a;a=a.next;}else{c.next=b;b=b.next;}c=c.next;}c.next=a!=null?a:b;return d.next;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='sort-list'),$$Why is merge sort preferred over quicksort for linked lists?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='sort-list'),$$How does the bottom-up merge sort achieve O(1) space?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='sort-list'),$$What happens if you do not null-terminate the first half?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='sort-list'),$$How would you make this stable for equal-value nodes?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='sort-list'),$$How would you parallelise the merge sort across multiple threads?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sort-list'),$$4
4 2 1 3$$,$$[1,2,3,4]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sort-list'),$$5
-1 5 3 4 0$$,$$[-1,0,3,4,5]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='sort-list'),$$1
0$$,$$[0]$$,true,3);

-- ══════════════════════════════════════════════════════════════════════════════
-- STACK (11–20)
-- ══════════════════════════════════════════════════════════════════════════════

-- 11 Baseball Game
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='baseball-game'),1,$$Each operation modifies only the top of the score history. A stack naturally supports adding, removing, and peeking at the top.$$,'Concept'),
((SELECT id FROM problems WHERE slug='baseball-game'),2,$$Push integer scores onto a stack. For C pop once; for D push twice the top; for + push the sum of the top two.$$,'Direction'),
((SELECT id FROM problems WHERE slug='baseball-game'),3,$$Trace the invariant: the stack always holds exactly the valid recorded scores after every operation.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='baseball-game'),$$Given a list of baseball record operations, return the sum of all valid scores.$$,$$Keywords: Baseball Game; stack simulation; top-of-stack operations. All four operations act on the most-recent scores, making a stack the natural choice.$$,$$Use when operations only reference the most-recently recorded elements.$$,$$Do not use an array with manual index tracking — a stack expresses the intent clearly.$$,$$Push integer scores onto a stack. For C pop once; for D push twice the top; for + push the sum of the top two.$$,$$Push integer scores onto a stack. For C pop once; for D push twice the top; for + push the sum of the top two.$$,$$Stack simulation.$$,$$No meaningfully worse approach; all operations are O(1).$$,'O(n)','O(n)',$$Stack simulation.$$,'O(n)','O(n)',$$stack=[]; for op: if int push; elif C pop; elif D push 2*top; elif + push top1+top2; return sum(stack)$$,$$Each operation directly maps to a stack operation; the stack invariant captures all valid recorded scores.$$,$$After each operation, the stack contains exactly the valid recorded scores.$$,$$Forgetting to peek (not pop) when computing + or D; confusing + (sum of top two) with D (double the top).$$,$$Apply the pattern to any problem where operations reference recent history: decode strings, expression evaluation, undo/redo.$$,$$public int calPoints(String[] ops){Deque<Integer>s=new ArrayDeque<>();for(String o:ops){if(o.equals("C"))s.pop();else if(o.equals("D"))s.push(s.peek()*2);else if(o.equals("+"))s.push(s.peek()+(int)((Deque<Integer>)((Object)s)).stream().skip(1).findFirst().orElse(0));else s.push(Integer.parseInt(o));}return s.stream().mapToInt(i->i).sum();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='baseball-game'),$$Why is a stack the natural data structure here?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='baseball-game'),$$How would you implement this with an ArrayList instead?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='baseball-game'),$$What is the maximum possible stack depth?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='baseball-game'),$$How would you extend this to support an UNDO for the most-recent C operation?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='baseball-game'),$$How would you support a MERGE operation that sums all current scores into one?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='baseball-game'),$$5
5 2 C D +$$,$$30$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='baseball-game'),$$5
5 -2 4 C D 9 + +$$,$$27$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='baseball-game'),$$1
1$$,$$1$$,true,3);

-- 12 Backspace String Compare (Stack)
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='backspace-stack'),1,$$Simulating backspace naturally maps to a stack: push characters and pop on #. Two stacks (or two processed strings) can then be compared.$$,'Concept'),
((SELECT id FROM problems WHERE slug='backspace-stack'),2,$$Build the final string for each input by pushing characters and popping on #. Compare the resulting stacks.$$,'Direction'),
((SELECT id FROM problems WHERE slug='backspace-stack'),3,$$Trace the invariant: the stack always holds the characters that would remain after all backspaces up to the current position.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='backspace-stack'),$$Determine whether two strings are equal after processing all # backspace characters.$$,$$Keywords: Backspace String Compare; stack simulation; # deletion. Simulating with a stack (or two-pointer backward scan) cleanly handles consecutive backspaces.$$,$$Use when characters must be deleted based on a symbol and you need the resulting string.$$,$$Do not replace # by removing characters in-place — that is O(n^2) for strings.$$,$$Build the final string for each input by pushing characters and popping on #. Compare the resulting stacks.$$,$$Build the final string for each input by pushing characters and popping on #. Compare the resulting stacks.$$,$$Stack simulation for each string, then comparison.$$,$$Two-pointer backward scan achieves O(1) space.$$,'O(n)','O(n)',$$Stack simulation for each string, then comparison.$$,'O(n)','O(n) stack or O(1) two-pointer',$$def process(s): stack=[]; for c in s: stack.append(c) if c!='#' else (stack.pop() if stack else None); return stack; return process(s)==process(t)$$,$$The stack faithfully simulates the text editor: each non-# character is appended and each # removes the most-recently typed character.$$,$$At any position, the stack contains exactly the characters that would remain in the editor buffer.$$,$$Popping from an empty stack on leading #; comparing raw strings with '#' still present.$$,$$Two-pointer O(1)-space variant: scan both strings right-to-left, counting # skips before comparing characters.$$,$$public boolean backspaceCompare(String s,String t){return proc(s).equals(proc(t));}String proc(String s){Deque<Character>k=new ArrayDeque<>();for(char c:s.toCharArray()){if(c=='#'){if(!k.isEmpty())k.pop();}else k.push(c);}StringBuilder b=new StringBuilder();for(char c:k)b.append(c);return b.toString();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='backspace-stack'),$$How does the two-pointer O(1)-space approach work?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='backspace-stack'),$$What happens when # appears at the beginning of the string?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='backspace-stack'),$$How would you extend this to support Ctrl+Z (undo the last word)?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='backspace-stack'),$$How would you handle a forward-delete character as well?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='backspace-stack'),$$How would you apply this to a stream of characters?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='backspace-stack'),$$ab#c
ad#c$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='backspace-stack'),$$ab##
c#d#$$,$$true$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='backspace-stack'),$$a#c
b$$,$$false$$,true,3);

-- 13 Next Greater Element I
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-i'),1,$$For each element, the next greater element is the first one to its right that is larger. A monotonic decreasing stack efficiently tracks elements that have not yet found their next greater.$$,'Concept'),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),2,$$Iterate nums2 with a stack. When a larger element arrives, pop the stack and record results. Store results in a map. Then look up each nums1 element.$$,'Direction'),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),3,$$Trace the invariant: the stack always holds elements in decreasing order, each waiting to find a greater element to its right.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$For each element in nums1 (a subset of nums2), find the next greater element in nums2.$$,$$Keywords: Next Greater Element; monotonic stack; decreasing. The monotonic stack pattern processes next-greater queries in O(n) amortized.$$,$$Use when finding the nearest element to the right that satisfies a comparison.$$,$$Do not scan right for every element — that is O(n^2).$$,$$Iterate nums2 with a stack. When a larger element arrives, pop the stack and record results. Store results in a map. Then look up each nums1 element.$$,$$Iterate nums2 with a stack. When a larger element arrives, pop the stack and record results. Store results in a map. Then look up each nums1 element.$$,$$Monotonic decreasing stack + hash map for lookup.$$,$$Nested loops scanning right for each element: O(m*n).$$,'O(m*n)','O(1)',$$Monotonic decreasing stack + hash map for lookup.$$,'O(m+n)','O(n)',$$map={}; stack=[]; for x in nums2: while stack and x>stack[-1]: map[stack.pop()]=x; stack.append(x); return [map.get(x,-1) for x in nums1]$$,$$Every element is pushed and popped at most once. When a larger element arrives it resolves all pending smaller elements on the stack.$$,$$The stack is always monotonically decreasing; elements stay until a greater value arrives from the right.$$,$$Using a min-stack instead of max (wrong monotonic direction); not defaulting to -1 for elements with no greater neighbour.$$,$$Next Greater Element II wraps around (circular array) — run the same loop twice. Daily Temperatures uses the same pattern with indices instead of values.$$,$$public int[] nextGreaterElement(int[] n1,int[] n2){Map<Integer,Integer>m=new HashMap<>();Deque<Integer>s=new ArrayDeque<>();for(int x:n2){while(!s.isEmpty()&&x>s.peek())m.put(s.pop(),x);s.push(x);}int[]r=new int[n1.length];for(int i=0;i<n1.length;i++)r[i]=m.getOrDefault(n1[i],-1);return r;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$Why is every element pushed and popped at most once?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$How would you find the next smaller element instead?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$How does this relate to Daily Temperatures?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$How do you handle a circular array (Next Greater Element II)?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='next-greater-element-i'),$$How would you find the previous greater element for each position?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-i'),$$2
4 1 2
4
1 3 4 2$$,$$[-1,3,-1]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-i'),$$2
2 4
4
1 2 3 4$$,$$[3,4]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-i'),$$1
1
3
1 2 3$$,$$[2]$$,true,3);

-- 14 Make The String Great
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='make-good-string'),1,$$Two adjacent characters form a bad pair when they are the same letter but different cases. A stack lets you check the new character against the most-recent accepted character.$$,'Concept'),
((SELECT id FROM problems WHERE slug='make-good-string'),2,$$Push each character. Before pushing, if the stack top and the current character are the same letter in different cases, pop instead.$$,'Direction'),
((SELECT id FROM problems WHERE slug='make-good-string'),3,$$Trace the invariant: the stack always contains a valid (great) prefix after processing each character.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='make-good-string'),$$Remove all adjacent bad pairs (same letter, different case) repeatedly until none remain; return the result.$$,$$Keywords: Make String Great; stack; adjacent pair removal. Adjacent-pair cancellation is the classic stack pattern — each new character is checked against the stack top.$$,$$Use when adjacent-pair cancellations can cascade (removing a pair may create a new bad pair).$$,$$Do not scan repeatedly — that is O(n^2). A single pass with a stack handles cascades naturally.$$,$$Push each character. Before pushing, if the stack top and the current character are the same letter in different cases, pop instead.$$,$$Push each character. Before pushing, if the stack top and the current character are the same letter in different cases, pop instead.$$,$$Single-pass stack with pop-on-bad-pair.$$,$$Repeated scanning and replacement: O(n^2).$$,'O(n^2)','O(n)',$$Single-pass stack with pop-on-bad-pair.$$,'O(n)','O(n)',$$stack=[]; for c in s: if stack and c!=stack[-1] and c.lower()==stack[-1].lower(): stack.pop() else: stack.append(c); return ''.join(stack)$$,$$When a bad pair is detected the pop resolves it immediately, exposing the element below for potential cascading — exactly as repeated removal would.$$,$$After processing each character, the stack contains a valid great string for the prefix seen so far.$$,$$Checking only case without checking that the letter is the same; forgetting that Math.abs(a-b)==32 works for ASCII case difference.$$,$$The pattern generalises to any adjacent-pair cancellation: Asteroid Collision, Remove All Adjacent Duplicates.$$,$$public String makeGood(String s){Deque<Character>k=new ArrayDeque<>();for(char c:s.toCharArray()){if(!k.isEmpty()&&Math.abs(k.peek()-c)==32)k.pop();else k.push(c);}StringBuilder b=new StringBuilder();while(!k.isEmpty())b.append(k.pollLast());return b.toString();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='make-good-string'),$$Why does a single stack pass handle cascading removals?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='make-good-string'),$$How would you remove all adjacent duplicates (same character, any case)?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='make-good-string'),$$Why does Math.abs(a-b)==32 identify a case pair in ASCII?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='make-good-string'),$$How would you apply this pattern to Asteroid Collision?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='make-good-string'),$$How would you find the minimum number of characters to remove to make the string great without a stack?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='make-good-string'),$$leEeetcode$$,$$leetcode$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='make-good-string'),$$abBAcC$$,$$""$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='make-good-string'),$$s$$,$$s$$,true,3);

-- 15 Validate Stack Sequences
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),1,$$Simulate the push and pop operations greedily: push elements from pushed in order; after each push, pop from the simulated stack as long as the top matches the next element in popped.$$,'Concept'),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),2,$$Maintain a stack and a pointer into popped. Push each element; then while the stack top equals popped[pointer], pop and advance pointer.$$,'Direction'),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),3,$$Trace the invariant: after each push, all pops that could have occurred at this point are performed greedily.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$Given pushed and popped sequences, determine whether they could be the result of a valid push/pop sequence on an initially empty stack.$$,$$Keywords: Validate Stack Sequences; greedy simulation; stack invariant. Greedy popping is safe because delaying a pop can only make future pops harder.$$,$$Use when verifying the validity of a push/pop order for a stack.$$,$$Do not try all permutations — the greedy simulation is always sufficient.$$,$$Maintain a stack and a pointer into popped. Push each element; then while the stack top equals popped[pointer], pop and advance pointer.$$,$$Maintain a stack and a pointer into popped. Push each element; then while the stack top equals popped[pointer], pop and advance pointer.$$,$$Greedy simulation with a stack and a pointer.$$,$$Trying all orderings is exponential.$$,'O(2^n)','O(n)',$$Greedy simulation with a stack and a pointer.$$,'O(n)','O(n)',$$stack=[]; j=0; for x in pushed: stack.append(x); while stack and stack[-1]==popped[j]: stack.pop(); j++; return len(stack)==0$$,$$Greedy popping is safe: if the top matches the next expected pop, delaying it cannot help because no future push produces the same value at that position.$$,$$After each push, every pop that matches the current expected pop order is performed immediately.$$,$$Not popping greedily (waiting too long); off-by-one in pointer j; not checking that the stack is empty at the end.$$,$$The same greedy argument applies to queue simulation with two stacks and to browser-history validation.$$,$$public boolean validateStackSequences(int[] pushed,int[] popped){Deque<Integer>s=new ArrayDeque<>();int j=0;for(int x:pushed){s.push(x);while(!s.isEmpty()&&s.peek()==popped[j]){s.pop();j++;}}return s.isEmpty();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$Why is greedy popping always safe?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$What does a non-empty stack at the end signify?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$How would you generate all valid pop sequences for a given push sequence?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$How would you validate a deque (double-ended queue) sequence?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$How does this relate to the number of valid BST sequences?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$5
1 2 3 4 5
5
4 5 3 2 1$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$5
1 2 3 4 5
5
4 3 5 1 2$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-stack-sequences'),$$3
1 2 3
3
3 2 1$$,$$true$$,true,3);

-- 16 Decode String
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='decode-string'),1,$$Encoded segments can be nested. A stack lets you save the outer context (partial string and repetition count) when entering a nested bracket.$$,'Concept'),
((SELECT id FROM problems WHERE slug='decode-string'),2,$$Push current string and count when seeing [; on ] pop the count and previous string, repeat the inner string, and append to the previous string.$$,'Direction'),
((SELECT id FROM problems WHERE slug='decode-string'),3,$$Trace the invariant: the stack holds the (partial string, repeat count) pairs for all unclosed brackets above the current nesting level.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='decode-string'),$$Decode a string encoded as k[encoded_string] where encoded_string may itself be encoded (nested).$$,$$Keywords: Decode String; nested brackets; stack context. Saving and restoring context at each bracket level is the classic nested-structure stack pattern.$$,$$Use when nested delimiters require saving and restoring outer context.$$,$$Do not use recursion without memoisation for deeply nested strings — stack is cleaner for iterative decoding.$$,$$Push current string and count when seeing [; on ] pop the count and previous string, repeat the inner string, and append to the previous string.$$,$$Push current string and count when seeing [; on ] pop the count and previous string, repeat the inner string, and append to the previous string.$$,$$Iterative stack storing (partial string, repeat count) pairs.$$,$$No meaningfully worse approach for this problem.$$,'O(output length)','O(output length)',$$Iterative stack storing (partial string, repeat count) pairs.$$,'O(output length)','O(stack depth * output length)',$$curr=""; k=0; stack=[]; for c in s: if digit: k=k*10+int(c); elif c=='[': stack.push((curr,k)); curr=""; k=0; elif c==']': prev,n=stack.pop(); curr=prev+n*curr; else curr+=c; return curr$$,$$Each [ saves the outer context; each ] restores it and expands the inner string, correctly handling nesting at any depth.$$,$$The stack holds context for every unclosed bracket in the current path from the outermost to the innermost scope.$$,$$Multi-digit numbers require k=k*10+digit before the [; forgetting to reset curr and k after pushing; building the repeated string in wrong order.$$,$$A recursive approach is equivalent but uses the call stack. For very deep nesting, the iterative version avoids stack overflow.$$,$$public String decodeString(String s){Deque<String>sc=new ArrayDeque<>();Deque<Integer>nc=new ArrayDeque<>();StringBuilder cur=new StringBuilder();int k=0;for(char c:s.toCharArray()){if(Character.isDigit(c))k=k*10+(c-'0');else if(c=='['){sc.push(cur.toString());nc.push(k);cur=new StringBuilder();k=0;}else if(c==']'){int n=nc.pop();String p=sc.pop();StringBuilder t=new StringBuilder(p);for(int i=0;i<n;i++)t.append(cur);cur=t;}else cur.append(c);}return cur.toString();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='decode-string'),$$Why must numbers be accumulated digit by digit before the [?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='decode-string'),$$How would a recursive approach work?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='decode-string'),$$What is the output length and why does it bound the time complexity?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='decode-string'),$$How would you encode a string to its shortest encoded form?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='decode-string'),$$How would you extend this to support variable-name substitution inside the brackets?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='decode-string'),$$3[a]2[bc]$$,$$aaabcbc$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='decode-string'),$$3[a2[c]]$$,$$accaccacc$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='decode-string'),$$2[abc]3[cd]ef$$,$$abcabccdcdcdef$$,true,3);

-- 17 Asteroid Collision
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='asteroid-collision'),1,$$Collisions only happen between a positive asteroid (moving right) on the stack and an incoming negative asteroid (moving left). Process outcomes until stability.$$,'Concept'),
((SELECT id FROM problems WHERE slug='asteroid-collision'),2,$$Push right-moving asteroids. For a left-moving asteroid, pop and resolve collisions: the larger survives; equal sizes both explode. Push only if it survives.$$,'Direction'),
((SELECT id FROM problems WHERE slug='asteroid-collision'),3,$$Trace the invariant: the stack always contains asteroids that will never collide with each other.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$Given a list of asteroids (positive = right, negative = left), return the state after all collisions. Equal-size asteroids both explode; larger survives.$$,$$Keywords: Asteroid Collision; stack; cascading collision. The stack holds right-movers waiting to collide with any future left-movers.$$,$$Use when elements interact pairwise with the most-recent element and interactions can cascade.$$,$$Do not use simulation with repeated passes — a single stack pass handles cascades in O(n) amortized.$$,$$Push right-moving asteroids. For a left-moving asteroid, pop and resolve collisions: the larger survives; equal sizes both explode. Push only if it survives.$$,$$Push right-moving asteroids. For a left-moving asteroid, pop and resolve collisions: the larger survives; equal sizes both explode. Push only if it survives.$$,$$Stack-based collision simulation.$$,$$Repeated passes until stable: O(n^2).$$,'O(n^2)','O(n)',$$Stack-based collision simulation.$$,'O(n) amortized','O(n)',$$for a in asteroids: alive=true; while alive and a<0 and stack and stack[-1]>0: if stack[-1]<-a: stack.pop() elif stack[-1]==-a: stack.pop(); alive=false; else alive=false; if alive: stack.append(a)$$,$$Each asteroid is pushed and popped at most once. All collisions between a left-mover and the stack's right-movers are resolved immediately.$$,$$The stack always contains asteroids that cannot collide with each other (all left-movers preceded by no right-movers, or all right-movers).$$,$$Forgetting to check alive before pushing; confusing equal-size explosion (both die) with larger-survives; left-left or right-right pairs never collide.$$,$$The same cascade pattern appears in Make String Great and Remove All Adjacent Duplicates.$$,$$public int[] asteroidCollision(int[] a){Deque<Integer>s=new ArrayDeque<>();for(int x:a){boolean alive=true;while(alive&&x<0&&!s.isEmpty()&&s.peek()>0){if(s.peek()<-x)s.pop();else if(s.peek()==-x){s.pop();alive=false;}else alive=false;}if(alive)s.push(x);}int[]r=new int[s.size()];int i=s.size()-1;for(int v:s)r[i--]=v;return r;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$Why can right-right or left-left asteroid pairs never collide?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$What does the alive flag represent?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$How would you count the total number of collisions?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$How would you extend this to 2D asteroids with velocity vectors?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='asteroid-collision'),$$How does this pattern relate to Make String Great?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='asteroid-collision'),$$4
5 10 -5 -10$$,$$[5]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='asteroid-collision'),$$3
8 -8 5$$,$$[5]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='asteroid-collision'),$$4
10 2 -5 -10$$,$$[-10]$$,true,3);

-- 18 Next Greater Element II
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),1,$$The array is circular, so every element should look across the wrap-around boundary. Simulating two rounds of traversal handles the circular case.$$,'Concept'),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),2,$$Run the monotonic stack loop for 2*n iterations using index mod n. Only record results during the first pass.$$,'Direction'),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),3,$$Trace the invariant: the stack holds indices of elements still waiting for their next greater value, in decreasing order of their values.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$Find the next greater element for each element in a circular array.$$,$$Keywords: Next Greater Element II; circular array; monotonic stack twice. Two passes of the monotonic stack simulate the circular wrap-around.$$,$$Use when the next-greater search wraps around the end of the array.$$,$$Do not append the array to itself in memory — use modulo indexing.$$,$$Run the monotonic stack loop for 2*n iterations using index mod n. Only record results during the first pass.$$,$$Run the monotonic stack loop for 2*n iterations using index mod n. Only record results during the first pass.$$,$$Monotonic stack with 2*n iteration and mod indexing.$$,$$Nested linear scan: O(n^2).$$,'O(n^2)','O(1)',$$Monotonic stack with 2*n iteration and mod indexing.$$,'O(n)','O(n)',$$res=[-1]*n; stack=[]; for i in range(2*n): while stack and nums[stack[-1]]<nums[i%n]: res[stack.pop()]=nums[i%n]; if i<n: stack.append(i); return res$$,$$Two passes guarantee every element has seen all elements to its right including the wrap-around. The second pass only resolves remaining elements, never pushing new ones.$$,$$Elements in the stack are always in decreasing order; any arriving larger element resolves them.$$,$$Pushing indices in the second pass (causes overcounting); initialising result to 0 instead of -1; forgetting modulo.$$,$$The same 2-pass trick applies to any circular monotonic-stack problem. For non-circular arrays one pass suffices (Next Greater Element I).$$,$$public int[] nextGreaterElements(int[] nums){int n=nums.length;int[]res=new int[n];Arrays.fill(res,-1);Deque<Integer>s=new ArrayDeque<>();for(int i=0;i<2*n;i++){while(!s.isEmpty()&&nums[s.peek()]<nums[i%n])res[s.pop()]=nums[i%n];if(i<n)s.push(i);}return res;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$Why run 2*n iterations instead of appending the array to itself?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$Why only push indices during the first n iterations?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$How would you find the next smaller element in a circular array?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$How would you handle an infinite circular stream?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$How would you find the next greater element at distance at most k in a circular array?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$5
1 2 1 5 3$$,$$[2,5,5,-1,5]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$3
1 2 3$$,$$[2,3,-1]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='next-greater-element-ii'),$$4
5 4 3 2$$,$$[-1,5,5,5]$$,true,3);

-- 19 Online Stock Span
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='online-stock-span'),1,$$The span for today is 1 plus the sum of consecutive previous spans whose prices are at most today's price. A monotonic decreasing stack of (price, span) pairs computes this efficiently.$$,'Concept'),
((SELECT id FROM problems WHERE slug='online-stock-span'),2,$$Maintain a stack of (price, span) pairs. When a new price arrives, pop and accumulate spans while the stack top's price is <= the new price.$$,'Direction'),
((SELECT id FROM problems WHERE slug='online-stock-span'),3,$$Trace the invariant: the stack holds (price, span) pairs in decreasing price order; each represents a block of consecutive days that can be absorbed by a future higher price.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='online-stock-span'),$$Design a class that returns the span of the current stock price — the number of consecutive days (including today) the price has been <= today's price.$$,$$Keywords: Online Stock Span; monotonic stack; cumulative span. Accumulating spans on the stack compresses the history and gives O(1) amortized per query.$$,$$Use when you need the count of consecutive prior elements satisfying a comparison, processed online.$$,$$Do not scan back through all prices on each call — that is O(n) per call, O(n^2) total.$$,$$Maintain a stack of (price, span) pairs. When a new price arrives, pop and accumulate spans while the stack top's price is <= the new price.$$,$$Maintain a stack of (price, span) pairs. When a new price arrives, pop and accumulate spans while the stack top's price is <= the new price.$$,$$Monotonic decreasing stack of (price, cumulative span) pairs.$$,$$Scan back through all prices: O(n) per call.$$,'O(n) per call','O(n)',$$Monotonic decreasing stack of (price, cumulative span) pairs.$$,'O(1) amortized per call','O(n)',$$next(price): span=1; while stack and stack[-1][0]<=price: span+=stack.pop()[1]; stack.append((price,span)); return span$$,$$Popped elements are subsumed: their spans are absorbed into the current span and they will never affect future answers.$$,$$The stack holds prices in strictly decreasing order; each entry summarises a contiguous block of absorbed days.$$,$$Using strict < instead of <= (missing equal-price spans); not initialising span to 1 before the loop.$$,$$The same compressed-span technique applies to Sum of Subarray Minimums and other "contribution of each element" problems.$$,$$class StockSpanner{Deque<int[]>s=new ArrayDeque<>();public int next(int p){int sp=1;while(!s.isEmpty()&&s.peek()[0]<=p)sp+=s.pop()[1];s.push(new int[]{p,sp});return sp;}}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='online-stock-span'),$$Why are popped elements safe to discard permanently?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='online-stock-span'),$$How would you implement this without a stack (just scanning back)?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='online-stock-span'),$$Why use <= instead of < when comparing prices?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='online-stock-span'),$$How would you support querying the span for an arbitrary past day?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='online-stock-span'),$$How does this pattern relate to Sum of Subarray Minimums?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='online-stock-span'),$$7
100 80 60 70 60 75 85$$,$$[1,1,1,2,1,4,6]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='online-stock-span'),$$3
10 10 10$$,$$[1,2,3]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='online-stock-span'),$$5
1 2 3 4 5$$,$$[1,2,3,4,5]$$,true,3);

-- 20 Remove Outermost Parentheses
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),1,$$Track the nesting depth. Characters at depth > 0 when opening (or depth > 1 when closing) are inner characters and should be kept.$$,'Concept'),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),2,$$Maintain a counter. On ( if counter > 0, append it (it is not outermost), then increment. On ) decrement, then if counter > 0, append it.$$,'Direction'),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),3,$$Trace the invariant: the counter equals the current nesting depth; depth 0 marks the boundary of a primitive decomposition.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$Remove the outermost parentheses of every primitive decomposition in a valid parentheses string.$$,$$Keywords: Remove Outermost Parentheses; depth counter; primitive decomposition. A depth counter identifies which characters are outermost without needing an explicit stack.$$,$$Use when you need to strip the outermost layer of a valid parentheses string.$$,$$Do not use a full stack when a simple counter suffices.$$,$$Maintain a counter. On ( if counter > 0, append it (it is not outermost), then increment. On ) decrement, then if counter > 0, append it.$$,$$Maintain a counter. On ( if counter > 0, append it (it is not outermost), then increment. On ) decrement, then if counter > 0, append it.$$,$$Single-pass depth counter.$$,$$Identify primitive decompositions explicitly, then strip first and last character: O(n) but more code.$$,'O(n)','O(n)',$$Single-pass depth counter.$$,'O(n)','O(n)',$$depth=0; res=[]; for c in s: if c=='(': if depth>0 res.append(c); depth++; else depth--; if depth>0 res.append(c); return ''.join(res)$$,$$The outermost ( increments depth from 0 to 1; we skip it. The matching outermost ) decrements depth from 1 to 0; we skip it too. All inner characters are appended.$$,$$The counter equals the nesting depth; depth 0 marks the start and end of each primitive.$$,$$Appending before incrementing on ( (includes the outermost); decrementing before the check on ) (excludes a valid inner close paren).$$,$$The same depth-counter technique identifies balanced substrings and validates parentheses without an explicit stack.$$,$$public String removeOuterParentheses(String s){StringBuilder r=new StringBuilder();int d=0;for(char c:s.toCharArray()){if(c=='('){if(d>0)r.append(c);d++;}else{d--;if(d>0)r.append(c);}}return r.toString();}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$Why check depth > 0 before appending ( but > 0 after decrementing for )?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$How would you find the score (number of primitive decompositions)?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$How does this relate to validating balanced parentheses?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$How would you remove k outer layers instead of just one?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$How would you extend this to multiple bracket types ({ [ ( ) ] })?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$(()())(())$$,$$()()()$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$(()())(())(()(())))$$,$$()()()()$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='remove-outermost-parentheses'),$$()$$,$$""$$,true,3);
