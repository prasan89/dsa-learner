-- V23: Batch 6 — gold-standard content for 10 tree problems.
-- Gold-standard content; all problems remain in CONTENT_REVIEW until technical validation.

-- 1 Same Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='same-tree'),1,$$Two trees are identical when their roots have the same value and both subtrees are identical. Any structural or value mismatch breaks equality.$$,'Concept'),
((SELECT id FROM problems WHERE slug='same-tree'),2,$$Recurse simultaneously on both trees. Return false immediately when one node is null and the other is not, or values differ.$$,'Direction'),
((SELECT id FROM problems WHERE slug='same-tree'),3,$$Trace the invariant: at each call both subtrees are identical if and only if the function returns true.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='same-tree'),$$Determine whether two binary trees are structurally identical with the same node values.$$,$$Keywords: Same Tree; structural equality; recursive comparison. Simultaneous DFS on both trees terminates early on any mismatch.$$,$$Use when checking full structural and value equality of two trees.$$,$$Do not serialize both trees and compare strings — it is correct but less clear.$$,$$Recurse simultaneously on both trees. Return false immediately when one node is null and the other is not, or values differ.$$,$$Recurse simultaneously on both trees. Return false immediately when one node is null and the other is not, or values differ.$$,$$Simultaneous DFS with early termination.$$,$$No brute force is meaningfully different; any full traversal is O(n).$$,'O(n)','O(h)',$$Simultaneous DFS with early termination.$$,'O(n)','O(h)',$$if p null and q null return true; if one null or values differ return false; return isSame(p.left,q.left) and isSame(p.right,q.right)$$,$$Both trees must match at every node; any mismatch short-circuits the recursion.$$,$$At any call, both subtrees rooted at p and q are identical if and only if true is returned.$$,$$Forgetting to handle the case where one node is null and the other is not; comparing node references instead of values.$$,$$Extend to structural equality ignoring values, or value equality ignoring structure. Iterative version uses two stacks in lockstep.$$,$$public boolean isSameTree(TreeNode p,TreeNode q){if(p==null&&q==null)return true;if(p==null||q==null||p.val!=q.val)return false;return isSameTree(p.left,q.left)&&isSameTree(p.right,q.right);}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='same-tree'),$$How would you implement this iteratively?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='same-tree'),$$How does this differ from checking subtree containment?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='same-tree'),$$What is the worst-case recursion depth?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='same-tree'),$$How would you compare trees ignoring value but checking structure only?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='same-tree'),$$How would you make this thread-safe for concurrent trees?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='same-tree'),$$3
1 2 3
3
1 2 3$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='same-tree'),$$3
1 2
2
1 2$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='same-tree'),$$3
1 2 1
3
1 1 2$$,$$false$$,true,3);

-- 2 Balanced Binary Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),1,$$A tree is height-balanced when every node's left and right subtree heights differ by at most 1. An imbalance at any node makes the whole tree unbalanced.$$,'Concept'),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),2,$$Combine height computation with balance checking in one DFS pass. Return -1 as a sentinel when an imbalance is detected.$$,'Direction'),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),3,$$Trace the invariant: the helper returns the height of the subtree if balanced, or -1 if any node in it is unbalanced.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$Determine whether a binary tree is height-balanced.$$,$$Keywords: Balanced Binary Tree; height; subtree height difference. Combining height and balance in a single post-order traversal avoids redundant work.$$,$$Use when you need to verify height balance in a single pass.$$,$$Do not call a separate height function per node — that makes it O(n^2).$$,$$Combine height computation with balance checking in one DFS pass. Return -1 as a sentinel when an imbalance is detected.$$,$$Combine height computation with balance checking in one DFS pass. Return -1 as a sentinel when an imbalance is detected.$$,$$Post-order DFS returning height or -1 sentinel.$$,$$Computing height separately per node is O(n^2).$$,'O(n^2)','O(h)',$$Post-order DFS returning height or -1 sentinel.$$,'O(n)','O(h)',$$helper(node): if null return 0; l=helper(left); r=helper(right); if l==-1 or r==-1 or abs(l-r)>1 return -1; return max(l,r)+1; return helper(root)!=-1$$,$$The sentinel propagates upward immediately upon detecting an imbalance, pruning all further work in that subtree.$$,$$The helper returns the correct height for balanced subtrees and -1 for any unbalanced subtree.$$,$$Calling height() separately on each node causing O(n^2); forgetting to propagate the -1 sentinel; off-by-one in height.$$,$$The sentinel pattern generalises to any property that can short-circuit bottom-up: diameter, path sum, LCA.$$,$$public boolean isBalanced(TreeNode root){return h(root)!=-1;}int h(TreeNode n){if(n==null)return 0;int l=h(n.left),r=h(n.right);if(l==-1||r==-1||Math.abs(l-r)>1)return -1;return Math.max(l,r)+1;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$Why is calling height() per node O(n^2)?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$What other properties can be computed with the same sentinel pattern?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$How would you rebalance an unbalanced BST?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$How does AVL balance differ from the definition here?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$How would you track which node causes the imbalance?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$3
3 9 20 null null 15 7$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$7
1 2 2 3 3 null null 4 4$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='balanced-binary-tree'),$$0
$$,$$true$$,true,3);

-- 3 Diameter of Binary Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),1,$$The diameter passing through a node equals the depth of its left subtree plus the depth of its right subtree. The overall diameter is the maximum over all nodes.$$,'Concept'),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),2,$$In a post-order DFS, compute each node's left and right depths, update a global maximum, then return the deeper depth plus one.$$,'Direction'),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),3,$$Trace the invariant: when the DFS returns from a node, the global max already reflects every diameter passing through nodes in that subtree.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$Find the length of the longest path between any two nodes in the tree (path does not need to pass through the root).$$,$$Keywords: Diameter of Binary Tree; longest path; post-order. Updating a global max during a height DFS gives diameter in a single pass.$$,$$Use when the answer aggregates over all nodes but requires a sub-result (depth) per node.$$,$$Do not assume the diameter must pass through the root.$$,$$In a post-order DFS, compute each node's left and right depths, update a global maximum, then return the deeper depth plus one.$$,$$In a post-order DFS, compute each node's left and right depths, update a global maximum, then return the deeper depth plus one.$$,$$Post-order DFS updating a global max with left+right depth.$$,$$Computing depth for every node from scratch is O(n^2).$$,'O(n^2)','O(h)',$$Post-order DFS updating a global max with left+right depth.$$,'O(n)','O(h)',$$max=0; dfs(node): if null return 0; l=dfs(left); r=dfs(right); max=max(max,l+r); return max(l,r)+1; return max$$,$$The diameter through any node is the sum of the deepest paths in its two subtrees; the DFS computes depths bottom-up.$$,$$When DFS returns from a node, the global max reflects the maximum diameter over all nodes in that subtree.$$,$$Confusing diameter (edges) with node count; forgetting to update max before returning depth; assuming root is on the path.$$,$$The same pattern solves Binary Tree Maximum Path Sum when node values replace depths.$$,$$int d=0;public int diameterOfBinaryTree(TreeNode r){dep(r);return d;}int dep(TreeNode n){if(n==null)return 0;int l=dep(n.left),r=dep(n.right);d=Math.max(d,l+r);return Math.max(l,r)+1;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$Why does the diameter not need to pass through the root?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$How does this relate to Binary Tree Maximum Path Sum?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$How would you return the actual path, not just the length?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$How would you find the diameter of a general graph?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$How would you maintain the diameter dynamically as nodes are added?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$5
1 2 3 4 5$$,$$3$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$2
1 2$$,$$1$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='diameter-binary-tree'),$$1
1$$,$$0$$,true,3);

-- 4 Path Sum
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='path-sum'),1,$$A root-to-leaf path exists with the target sum when the current node's value reduces the remaining target to zero exactly at a leaf.$$,'Concept'),
((SELECT id FROM problems WHERE slug='path-sum'),2,$$Subtract the current node's value from the target at each step. At a leaf, check whether the remaining target is zero.$$,'Direction'),
((SELECT id FROM problems WHERE slug='path-sum'),3,$$Trace the invariant: when the DFS reaches a node, the remaining target equals targetSum minus the sum of all ancestors.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='path-sum'),$$Determine whether the tree has a root-to-leaf path whose node values sum to targetSum.$$,$$Keywords: Path Sum; root-to-leaf; remaining target. Subtracting the current value and checking at leaves is the canonical DFS pattern.$$,$$Use when checking existence of a root-to-leaf path satisfying a sum constraint.$$,$$Do not return true at an internal node when the remaining sum is zero — a path must end at a leaf.$$,$$Subtract the current node's value from the target at each step. At a leaf, check whether the remaining target is zero.$$,$$Subtract the current node's value from the target at each step. At a leaf, check whether the remaining target is zero.$$,$$DFS subtracting node values, returning true at a leaf when remaining equals zero.$$,$$No meaningfully worse approach for a single existence check.$$,'O(n)','O(h)',$$DFS subtracting node values, returning true at a leaf when remaining equals zero.$$,'O(n)','O(h)',$$if node null return false; rem=target-node.val; if leaf return rem==0; return dfs(left,rem) or dfs(right,rem)$$,$$Each path from root to leaf is explored at most once; subtracting values accumulates the path sum bottom-up.$$,$$When DFS visits a node, the remaining target equals targetSum minus the sum of all nodes from the root to this node.$$,$$Returning true at a non-leaf when remaining is zero; treating null as a leaf; not handling negative values.$$,$$Extend to collect all valid paths (Path Sum II) or count paths with the target sum starting from any node (Subarray Sum variant on trees).$$,$$public boolean hasPathSum(TreeNode n,int t){if(n==null)return false;t-=n.val;if(n.left==null&&n.right==null)return t==0;return hasPathSum(n.left,t)||hasPathSum(n.right,t);}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='path-sum'),$$Why must you check for a leaf rather than just remaining==0?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='path-sum'),$$How would you collect all valid paths instead of just checking existence?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='path-sum'),$$How would you handle negative node values?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='path-sum'),$$How would you count all paths (not just root-to-leaf) summing to target?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='path-sum'),$$How would you implement this iteratively using a stack?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='path-sum'),$$22
5 4 8 11 null 13 4 7 2 null null null 1$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='path-sum'),$$5
1 2 3$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='path-sum'),$$0
$$,$$false$$,true,3);

-- 5 Subtree of Another Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='subtree-another-tree'),1,$$A subtree is an exact match rooted at some node of the main tree. Reuse the isSameTree check at every node of the main tree.$$,'Concept'),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),2,$$DFS over the main tree; at each node check isSameTree. If either child contains the subtree, return true.$$,'Direction'),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),3,$$Trace the invariant: the function returns true if and only if subRoot matches a subtree rooted at the current node or any of its descendants.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$Determine whether subRoot appears as a subtree of root.$$,$$Keywords: Subtree of Another Tree; tree matching; same-tree reuse. Calling isSameTree at every node of the main tree is the standard approach.$$,$$Use when checking whether one tree is an exact subtree of another.$$,$$Do not confuse subtree (exact copy of a whole subtree) with a path or partial structure.$$,$$DFS over the main tree; at each node check isSameTree. If either child contains the subtree, return true.$$,$$DFS over the main tree; at each node check isSameTree. If either child contains the subtree, return true.$$,$$DFS with isSameTree check at each node.$$,$$No fundamentally different approach at this scale; serialization-based KMP is asymptotically better but complex.$$,'O(m*n)','O(max(m,n))',$$DFS with isSameTree check at each node.$$,'O(m*n)','O(max(m,n))',$$if root null return subRoot null; if isSame(root,subRoot) return true; return isSubtree(root.left,subRoot) or isSubtree(root.right,subRoot)$$,$$Every node of the main tree is a candidate root for the subtree; isSameTree handles exact-match verification.$$,$$The function returns true if subRoot is rooted at the current node or exists in either subtree.$$,$$Confusing subtree with a node appearing somewhere; not checking null cases in isSameTree; partial matches at internal nodes.$$,$$For large trees, serialize both to strings with sentinels and use KMP for O(m+n) time.$$,$$public boolean isSubtree(TreeNode r,TreeNode s){if(r==null)return s==null;if(same(r,s))return true;return isSubtree(r.left,s)||isSubtree(r.right,s);}boolean same(TreeNode a,TreeNode b){if(a==null&&b==null)return true;if(a==null||b==null||a.val!=b.val)return false;return same(a.left,b.left)&&same(a.right,b.right);}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$Why is isSameTree called with root rather than its children?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$What is the worst-case time complexity and when does it occur?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$How would serialization + KMP reduce the complexity?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$How would you count the number of matching subtrees?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='subtree-another-tree'),$$How would you check if subRoot is isomorphic (structurally equal, values ignored) to any subtree?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='subtree-another-tree'),$$5
3 4 5 1 2
3
4 1 2$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='subtree-another-tree'),$$5
3 4 5 1 2 null null null null 0
3
4 1 2$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='subtree-another-tree'),$$1
1
1
1$$,$$true$$,true,3);

-- 6 Validate Binary Search Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='validate-bst'),1,$$Every node in a BST must satisfy a range constraint inherited from its ancestors, not just a comparison with its immediate parent.$$,'Concept'),
((SELECT id FROM problems WHERE slug='validate-bst'),2,$$Pass down a valid range [min, max] at each recursive call. Going left tightens the max; going right tightens the min.$$,'Direction'),
((SELECT id FROM problems WHERE slug='validate-bst'),3,$$Trace the invariant: when visiting a node, the valid range is exactly the set of values that would keep the BST property for all ancestors.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='validate-bst'),$$Determine whether a binary tree is a valid BST.$$,$$Keywords: Validate BST; range constraint; ancestor bounds. Each node must lie strictly within a range derived from all its ancestors.$$,$$Use when verifying the BST property across an entire tree.$$,$$Do not only compare a node with its direct children — ancestor constraints also apply.$$,$$Pass down a valid range [min, max] at each recursive call. Going left tightens the max; going right tightens the min.$$,$$Pass down a valid range [min, max] at each recursive call. Going left tightens the max; going right tightens the min.$$,$$DFS with range bounds passed top-down.$$,$$In-order traversal and checking sorted order is O(n) and O(n) space, but also valid.$$,'O(n)','O(n)',$$DFS with range bounds passed top-down.$$,'O(n)','O(h)',$$validate(node, min, max): if null return true; if node.val<=min or node.val>=max return false; return validate(left,min,node.val) and validate(right,node.val,max)$$,$$Each node inherits a tightened range from its ancestors. Violating the range means the BST property is broken at or above this node.$$,$$When visiting a node, the range [min, max) contains exactly the values that are BST-valid given all ancestor constraints.$$,$$Comparing only with the parent; using Integer.MIN/MAX_VALUE without long when values equal INT boundaries; allowing equal values in strict BST.$$,$$In-order traversal stores the previously seen value; a BST produces strictly increasing in-order output. Use Long boundaries to handle Integer.MIN/MAX edge cases.$$,$$public boolean isValidBST(TreeNode r){return ok(r,Long.MIN_VALUE,Long.MAX_VALUE);}boolean ok(TreeNode n,long lo,long hi){if(n==null)return true;if(n.val<=lo||n.val>=hi)return false;return ok(n.left,lo,n.val)&&ok(n.right,n.val,hi);}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='validate-bst'),$$Why is comparing only with the immediate parent insufficient?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='validate-bst'),$$How does in-order traversal validate a BST?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='validate-bst'),$$Why use Long.MIN/MAX_VALUE instead of Integer?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='validate-bst'),$$How would you validate a BST that allows duplicate values?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='validate-bst'),$$How would you recover the tree if it has exactly two nodes swapped?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-bst'),$$3
2 1 3$$,$$true$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-bst'),$$5
5 1 4 null null 3 6$$,$$false$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='validate-bst'),$$3
2 2 2$$,$$false$$,true,3);

-- 7 Lowest Common Ancestor of a BST
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),1,$$In a BST the LCA is the first node where p and q split into different subtrees — one value is on each side, or one of them equals the current node.$$,'Concept'),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),2,$$If both p and q are less than the current node, descend left. If both are greater, descend right. Otherwise the current node is the LCA.$$,'Direction'),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),3,$$Trace the invariant: the LCA lies in the search path, and the split point is where p and q first go different directions.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$Find the lowest common ancestor of two nodes in a BST.$$,$$Keywords: LCA BST; split point; BST ordering. BST ordering lets you navigate directly to the split without exploring both subtrees.$$,$$Use when the tree is a BST and you can exploit its ordering property.$$,$$Do not use the general LCA algorithm — BST ordering gives an O(h) path-following approach.$$,$$If both p and q are less than the current node, descend left. If both are greater, descend right. Otherwise the current node is the LCA.$$,$$If both p and q are less than the current node, descend left. If both are greater, descend right. Otherwise the current node is the LCA.$$,$$Iterative or recursive descent to the split point.$$,$$General LCA using DFS is O(n) but ignores BST structure.$$,'O(n)','O(n)',$$Iterative or recursive descent to the split point.$$,'O(h)','O(1) iterative',$$node=root; while node: if p.val<node.val and q.val<node.val: node=node.left; elif p.val>node.val and q.val>node.val: node=node.right; else return node$$,$$In a BST, when p and q are on the same side we must go deeper; when they split the current node is the shallowest ancestor of both.$$,$$The LCA is reached exactly when p and q fall on opposite sides (or one equals the current node).$$,$$Confusing with the general binary tree LCA; forgetting that a node is its own ancestor; using recursive when iterative is cleaner.$$,$$For a general binary tree use the recursive DFS LCA. For a BST with parent pointers, collect ancestor sets and intersect.$$,$$public TreeNode lowestCommonAncestor(TreeNode r,TreeNode p,TreeNode q){while(r!=null){if(p.val<r.val&&q.val<r.val)r=r.left;else if(p.val>r.val&&q.val>r.val)r=r.right;else return r;}return null;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$Why is the iterative version preferred here?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$How does this differ from LCA in a general binary tree?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$What if p or q is not in the tree?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$How would you find the LCA of k nodes in a BST?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$How does LCA relate to range queries in a BST?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$6
6 2 8 0 4 7 9 null null 3 5
2
8$$,$$6$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$6
6 2 8 0 4 7 9 null null 3 5
2
4$$,$$2$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-bst'),$$3
2 1 3
1
3$$,$$2$$,true,3);

-- 8 Count Good Nodes in Binary Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='count-good-nodes'),1,$$A node is good when its value is greater than or equal to every ancestor's value. Track the maximum value seen on the path from root to the current node.$$,'Concept'),
((SELECT id FROM problems WHERE slug='count-good-nodes'),2,$$DFS passing the running maximum. At each node, count it if its value >= max, then update max for child calls.$$,'Direction'),
((SELECT id FROM problems WHERE slug='count-good-nodes'),3,$$Trace the invariant: when visiting a node, the passed max equals the maximum value on the path from root to the parent.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$Count nodes where no ancestor has a greater value.$$,$$Keywords: Count Good Nodes; path maximum; top-down DFS. Passing a running max down the DFS naturally tracks the ancestor constraint.$$,$$Use when each node's validity depends on a running aggregate of its ancestors.$$,$$Do not recompute the path maximum from scratch at each node.$$,$$DFS passing the running maximum. At each node, count it if its value >= max, then update max for child calls.$$,$$DFS passing the running maximum. At each node, count it if its value >= max, then update max for child calls.$$,$$Pre-order DFS with running maximum.$$,$$No meaningfully worse alternative; any traversal is O(n).$$,'O(n)','O(h)',$$Pre-order DFS with running maximum.$$,'O(n)','O(h)',$$dfs(node,maxSoFar): if null return 0; count=1 if node.val>=maxSoFar else 0; newMax=max(maxSoFar,node.val); return count+dfs(left,newMax)+dfs(right,newMax)$$,$$The running maximum captures the ancestor constraint exactly; every node is visited once.$$,$$When visiting a node, the passed max equals the maximum value on the root-to-parent path.$$,$$Updating the max before the count check; not passing the updated max to children; forgetting the root is always good.$$,$$The same pattern applies to any problem where each node's validity depends on a path aggregate (min, sum, XOR) from the root.$$,$$public int goodNodes(TreeNode r){return dfs(r,Integer.MIN_VALUE);}int dfs(TreeNode n,int mx){if(n==null)return 0;int c=n.val>=mx?1:0;mx=Math.max(mx,n.val);return c+dfs(n.left,mx)+dfs(n.right,mx);}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$Why is the root always a good node?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$How would you count bad nodes instead?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$How would you adapt this for a running minimum?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$How would you return the list of good nodes rather than the count?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='count-good-nodes'),$$How would you handle this if the tree could have negative infinity as a value?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='count-good-nodes'),$$7
3 1 4 3 null 1 5$$,$$4$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='count-good-nodes'),$$3
3 3 null 4 2$$,$$3$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='count-good-nodes'),$$1
1$$,$$1$$,true,3);

-- 9 Binary Tree Right Side View
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),1,$$The right side view is the last node at each level when the tree is traversed level by level.$$,'Concept'),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),2,$$BFS level by level; at the end of each level, record the last node's value.$$,'Direction'),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),3,$$Trace the invariant: after processing a level, the result list contains the rightmost value at every level seen so far.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$Return the values of the nodes visible when viewing the tree from the right side (one node per level).$$,$$Keywords: Binary Tree Right Side View; level order; rightmost per level. BFS naturally groups nodes by level.$$,$$Use when you need one representative node per level.$$,$$Do not DFS without tracking depth — it requires extra care to capture only the rightmost node per level.$$,$$BFS level by level; at the end of each level, record the last node's value.$$,$$BFS level by level; at the end of each level, record the last node's value.$$,$$BFS with level-size tracking to identify the last node per level.$$,$$DFS with depth tracking works but is less intuitive.$$,'O(n)','O(w) max level width',$$BFS with level-size tracking to identify the last node per level.$$,'O(n)','O(w)',$$queue=[root]; while queue: size=len(queue); for i in range(size): node=dequeue; if i==size-1 add node.val; enqueue children$$,$$BFS processes all nodes at depth d before depth d+1; the last dequeued at each level is the rightmost.$$,$$After completing a level, the result list contains the rightmost value for every level processed so far.$$,$$Adding the first node per level instead of the last; not tracking level size separately; forgetting to enqueue non-null children only.$$,$$DFS variant: recurse right-first; add a value when the depth equals the result list size — right child is always recorded before left.$$,$$public List<Integer> rightSideView(TreeNode r){List<Integer>res=new ArrayList<>();if(r==null)return res;Queue<TreeNode>q=new LinkedList<>();q.add(r);while(!q.isEmpty()){int s=q.size();for(int i=0;i<s;i++){TreeNode n=q.poll();if(i==s-1)res.add(n.val);if(n.left!=null)q.add(n.left);if(n.right!=null)q.add(n.right);}}return res;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$How would you return the left side view instead?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$How does the DFS variant work (right-first traversal)?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$What is the maximum queue size and when does it occur?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$How would you return both the left and right side views in one pass?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$How would you return all visible nodes from an arbitrary viewing angle?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$5
1 2 3 null 5 null 4$$,$$[1,3,4]$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$1
1$$,$$[1]$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='binary-tree-right-side-view'),$$3
1 2 3 4$$,$$[1,3,4]$$,true,3);

-- 10 Lowest Common Ancestor of a Binary Tree
INSERT INTO hints(problem_id,level,content,label) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),1,$$The LCA is the deepest node that has both p and q as descendants (a node is a descendant of itself). Post-order DFS can detect whether p or q exists in each subtree.$$,'Concept'),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),2,$$Return p or q when found. If both left and right recursive calls return non-null, the current node is the LCA.$$,'Direction'),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),3,$$Trace the invariant: a non-null return from a subtree means p or q (or their LCA) was found within it.$$,'Algorithm')
ON CONFLICT(problem_id,level) DO UPDATE SET content=EXCLUDED.content,label=EXCLUDED.label;

INSERT INTO problem_content(problem_id,recognition_note,pattern_recognition_clues,when_to_use,when_not_to_use,intuition,guided_reasoning,solution,brute_force,brute_time,brute_space,optimal_approach,optimal_time,optimal_space,pseudocode,why_this_works,invariant,common_mistakes,senior_variations,java_solution,content_status) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$Find the lowest common ancestor of two nodes in a general binary tree.$$,$$Keywords: LCA Binary Tree; post-order; bubble up. Post-order DFS surfaces p and q upward; their meeting point is the LCA.$$,$$Use for a general binary tree without BST ordering.$$,$$Do not use the BST path-following shortcut — this tree has no ordering guarantee.$$,$$Return p or q when found. If both left and right recursive calls return non-null, the current node is the LCA.$$,$$Return p or q when found. If both left and right recursive calls return non-null, the current node is the LCA.$$,$$Post-order DFS returning found nodes; LCA is where both sides are non-null.$$,$$Collecting all ancestors per node and intersecting is O(n) but O(n) extra space.$$,'O(n)','O(n)',$$Post-order DFS returning found nodes; LCA is where both sides are non-null.$$,'O(n)','O(h)',$$if node null or node==p or node==q return node; l=lca(left); r=lca(right); if l and r return node; return l or r$$,$$If both subtrees return non-null, one contains p and the other q, making the current node the shallowest common ancestor.$$,$$A non-null return from a subtree signals that p, q, or their LCA was found within it.$$,$$Assuming the tree is a BST and using value comparisons; not handling the case where p is an ancestor of q.$$,$$With parent pointers, walk both nodes to the root recording ancestors, then find the deepest common one. For Euler tour + RMQ, LCA queries are O(1) after O(n log n) preprocessing.$$,$$public TreeNode lowestCommonAncestor(TreeNode r,TreeNode p,TreeNode q){if(r==null||r==p||r==q)return r;TreeNode l=lowestCommonAncestor(r.left,p,q),ri=lowestCommonAncestor(r.right,p,q);return l!=null&&ri!=null?r:l!=null?l:ri;}$$,'CONTENT_REVIEW');

INSERT INTO problem_followups(problem_id,question,type,sort_order) VALUES
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$What does it mean when only one side returns non-null?$$,'FOLLOWUP',1),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$How would you handle the case where p or q might not exist in the tree?$$,'FOLLOWUP',2),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$How does LCA differ for a BST vs a general binary tree?$$,'FOLLOWUP',3),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$How would you preprocess the tree to answer LCA queries in O(1)?$$,'SENIOR',4),
((SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$How would you find the LCA of more than two nodes?$$,'SENIOR',5)
ON CONFLICT DO NOTHING;

INSERT INTO test_cases(id,problem_id,input,expected_output,is_hidden,display_order) VALUES
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$3 5 1 6 2 0 8 null null 7 4
3
5$$,$$3$$,false,1),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$3 5 1 6 2 0 8 null null 7 4
5
4$$,$$5$$,false,2),
(gen_random_uuid(),(SELECT id FROM problems WHERE slug='lowest-common-ancestor-binary-tree'),$$1 2
1
2$$,$$1$$,true,3);
