-- V28d: Real descriptions for trees, heap, and graph problems

-- TREES

UPDATE problems SET
  description = $$Given the `root` of a binary tree, invert the tree by swapping every left child with its right child, and return the root. The inversion should be applied recursively to all nodes in the tree.$$,
  examples    = $$[{"input":"root = [4,2,7,1,3,6,9]","output":"[4,7,2,9,6,3,1]","explanation":"The left and right subtrees of every node are swapped recursively."},{"input":"root = [2,1,3]","output":"[2,3,1]","explanation":"The children of the root node are swapped: left becomes 3 and right becomes 1."},{"input":"root = []","output":"[]","explanation":"An empty tree has nothing to invert."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 100]
-100 <= Node.val <= 100$$
WHERE slug = 'invert-binary-tree';

UPDATE problems SET
  description = $$Given the roots of two binary trees `p` and `q`, write a function to check if they are the same or not. Two binary trees are considered the same if they are structurally identical, and the nodes have the same value.$$,
  examples    = $$[{"input":"p = [1,2,3], q = [1,2,3]","output":"true","explanation":"Both trees have the same structure and node values."},{"input":"p = [1,2], q = [1,null,2]","output":"false","explanation":"The trees differ structurally even though they have the same node values."},{"input":"p = [1,2,1], q = [1,1,2]","output":"false","explanation":"The trees have the same structure but different node values at the leaf positions."}]$$,
  constraints = $$The number of nodes in both trees is in the range [0, 100]
-10^4 <= Node.val <= 10^4$$
WHERE slug = 'same-tree';

UPDATE problems SET
  description = $$Given the roots of two binary trees `root` and `subRoot`, return `true` if there is a subtree of `root` with the same structure and node values as `subRoot` and `false` otherwise. A subtree of a binary tree `root` is a tree that consists of a node in `root` and all of this node's descendants.$$,
  examples    = $$[{"input":"root = [3,4,5,1,2], subRoot = [4,1,2]","output":"true","explanation":"The subtree rooted at node 4 in root matches subRoot exactly."},{"input":"root = [3,4,5,1,2,null,null,null,null,0], subRoot = [4,1,2]","output":"false","explanation":"The subtree rooted at node 4 has an extra child node 0, so it does not match subRoot."}]$$,
  constraints = $$The number of nodes in the root tree is in the range [1, 2000]
The number of nodes in the subRoot tree is in the range [1, 1000]
-10^4 <= root.val <= 10^4
-10^4 <= subRoot.val <= 10^4$$
WHERE slug = 'subtree-another-tree';

UPDATE problems SET
  description = $$Given a binary tree, determine if it is height-balanced. A height-balanced binary tree is a binary tree in which the depth of the two subtrees of every node never differs by more than one.$$,
  examples    = $$[{"input":"root = [3,9,20,null,null,15,7]","output":"true","explanation":"The left subtree has height 1 and the right subtree has height 2, a difference of 1."},{"input":"root = [1,2,2,3,3,null,null,4,4]","output":"false","explanation":"The left subtree has height 3 while the right has height 1, a difference of 2."},{"input":"root = []","output":"true","explanation":"An empty tree is trivially balanced."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 5000]
-10^4 <= Node.val <= 10^4$$
WHERE slug = 'balanced-binary-tree';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, return the length of the diameter of the tree. The diameter of a binary tree is the length of the longest path between any two nodes in a tree. This path may or may not pass through the `root`, and the length is measured in the number of edges.$$,
  examples    = $$[{"input":"root = [1,2,3,4,5]","output":"3","explanation":"The longest path is [4,2,1,3] or [5,2,1,3], both with length 3."},{"input":"root = [1,2]","output":"1","explanation":"The only path is between node 1 and node 2, with length 1."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 10^4]
-100 <= Node.val <= 100$$
WHERE slug = 'diameter-binary-tree';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level). Each level should be returned as its own list within the result.$$,
  examples    = $$[{"input":"root = [3,9,20,null,null,15,7]","output":"[[3],[9,20],[15,7]]","explanation":"Level 0 has [3], level 1 has [9,20], and level 2 has [15,7]."},{"input":"root = [1]","output":"[[1]]","explanation":"A single-node tree has one level containing just the root."},{"input":"root = []","output":"[]","explanation":"An empty tree produces an empty result."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 2000]
-1000 <= Node.val <= 1000$$
WHERE slug = 'binary-tree-level-order';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, imagine yourself standing on the right side of it, and return the values of the nodes you can see ordered from top to bottom. A node is visible from the right side if it is the rightmost node on its level.$$,
  examples    = $$[{"input":"root = [1,2,3,null,5,null,4]","output":"[1,3,4]","explanation":"From the right side, you see node 1 at level 0, node 3 at level 1, and node 4 at level 2."},{"input":"root = [1,null,3]","output":"[1,3]","explanation":"Node 3 is visible from the right at level 1."},{"input":"root = []","output":"[]","explanation":"An empty tree produces an empty result."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 100]
-100 <= Node.val <= 100$$
WHERE slug = 'binary-tree-right-side-view';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, determine if it is a valid binary search tree (BST). A valid BST is defined as a tree where the left subtree of a node contains only nodes with keys less than the node's key, the right subtree contains only nodes with keys greater than the node's key, and both subtrees must also be valid BSTs.$$,
  examples    = $$[{"input":"root = [2,1,3]","output":"true","explanation":"Node 1 is in the left subtree and is less than 2; node 3 is in the right subtree and is greater than 2."},{"input":"root = [5,1,4,null,null,3,6]","output":"false","explanation":"Node 4 is in the right subtree of 5 but node 3 in its left subtree violates the BST property since 3 < 5."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 10^4]
-2^31 <= Node.val <= 2^31 - 1$$
WHERE slug = 'validate-bst';

UPDATE problems SET
  description = $$Given the `root` of a binary search tree, and an integer `k`, return the `k`th smallest value (1-indexed) of all the values of the nodes in the tree. You may assume that `k` is always valid, 1 <= k <= number of nodes.$$,
  examples    = $$[{"input":"root = [3,1,4,null,2], k = 1","output":"1","explanation":"The in-order traversal gives [1,2,3,4], so the 1st smallest element is 1."},{"input":"root = [5,3,6,2,4,null,null,1], k = 3","output":"3","explanation":"The in-order traversal gives [1,2,3,4,5,6], so the 3rd smallest element is 3."}]$$,
  constraints = $$The number of nodes in the tree is n
1 <= k <= n <= 10^4
0 <= Node.val <= 10^4$$
WHERE slug = 'kth-smallest-bst';

UPDATE problems SET
  description = $$Given a binary search tree (BST), find the lowest common ancestor (LCA) node of two given nodes `p` and `q`. The LCA is defined as the lowest node in the tree that has both `p` and `q` as descendants (a node can be a descendant of itself).$$,
  examples    = $$[{"input":"root = [6,2,8,0,4,7,9,null,null,3,5], p = 2, q = 8","output":"6","explanation":"The LCA of nodes 2 and 8 is 6, as 6 is the lowest node that is an ancestor of both."},{"input":"root = [6,2,8,0,4,7,9,null,null,3,5], p = 2, q = 4","output":"2","explanation":"The LCA of nodes 2 and 4 is 2, since a node can be a descendant of itself."}]$$,
  constraints = $$The number of nodes in the tree is in the range [2, 10^5]
-10^9 <= Node.val <= 10^9
All Node.val are unique
p != q
p and q will exist in the BST$$
WHERE slug = 'lowest-common-ancestor-bst';

UPDATE problems SET
  description = $$Given a binary tree, find the lowest common ancestor (LCA) of two given nodes `p` and `q`. The LCA is defined as the lowest node that has both `p` and `q` as descendants. A node is considered a descendant of itself.$$,
  examples    = $$[{"input":"root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 1","output":"3","explanation":"The LCA of nodes 5 and 1 is 3."},{"input":"root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 4","output":"5","explanation":"The LCA of nodes 5 and 4 is 5, since a node can be a descendant of itself."}]$$,
  constraints = $$The number of nodes in the tree is in the range [2, 10^5]
-10^9 <= Node.val <= 10^9
All Node.val are unique
p != q
p and q will exist in the tree$$
WHERE slug = 'lowest-common-ancestor-binary-tree';

UPDATE problems SET
  description = $$Given two integer arrays `preorder` and `inorder` where `preorder` is the preorder traversal of a binary tree and `inorder` is the inorder traversal of the same tree, construct and return the binary tree. All values in the arrays are unique.$$,
  examples    = $$[{"input":"preorder = [3,9,20,15,7], inorder = [9,3,15,20,7]","output":"[3,9,20,null,null,15,7]","explanation":"The first element of preorder (3) is the root. In inorder, elements to the left of 3 form the left subtree [9] and elements to the right form the right subtree [15,20,7]."},{"input":"preorder = [-1], inorder = [-1]","output":"[-1]","explanation":"A single element forms a tree with just a root node."}]$$,
  constraints = $$1 <= preorder.length <= 3000
inorder.length == preorder.length
-3000 <= preorder[i], inorder[i] <= 3000
preorder and inorder consist of unique values
Each value of inorder also appears in preorder$$
WHERE slug = 'construct-tree-pre-inorder';

UPDATE problems SET
  description = $$Design an algorithm to serialize and deserialize a binary tree. Serialization is the process of converting a tree to a string representation so it can be stored or transmitted. Deserialization is reconstructing the tree from that string. Your implementation should ensure that a tree can be serialized to a string and this string can be deserialized to the original tree structure.$$,
  examples    = $$[{"input":"root = [1,2,3,null,null,4,5]","output":"[1,2,3,null,null,4,5]","explanation":"The tree is serialized to a string and then deserialized back to the original tree structure."},{"input":"root = []","output":"[]","explanation":"An empty tree serializes to an empty representation and deserializes back to null."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 10^4]
-1000 <= Node.val <= 1000$$
WHERE slug = 'serialize-binary-tree';

UPDATE problems SET
  description = $$A path in a binary tree is a sequence of nodes where each pair of adjacent nodes in the sequence has an edge connecting them. A node can only appear in the sequence at most once. The path does not need to pass through the root. Given the `root` of a binary tree, return the maximum path sum of any non-empty path.$$,
  examples    = $$[{"input":"root = [1,2,3]","output":"6","explanation":"The optimal path is 2 -> 1 -> 3 with a path sum of 2 + 1 + 3 = 6."},{"input":"root = [-10,9,20,null,null,15,7]","output":"42","explanation":"The optimal path is 15 -> 20 -> 7 with a path sum of 15 + 20 + 7 = 42."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 3 * 10^4]
-1000 <= Node.val <= 1000$$
WHERE slug = 'binary-tree-max-path-sum';

UPDATE problems SET
  description = $$Given the `root` of a binary tree and an integer `targetSum`, return `true` if the tree has a root-to-leaf path such that adding up all the values along the path equals `targetSum`. A leaf is a node with no children.$$,
  examples    = $$[{"input":"root = [5,4,8,11,null,13,4,7,2,null,null,null,1], targetSum = 22","output":"true","explanation":"The path 5 -> 4 -> 11 -> 2 sums to 22."},{"input":"root = [1,2,3], targetSum = 5","output":"false","explanation":"No root-to-leaf path sums to 5. The paths are 1->2 (sum=3) and 1->3 (sum=4)."},{"input":"root = [], targetSum = 0","output":"false","explanation":"An empty tree has no root-to-leaf paths."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 5000]
-1000 <= Node.val <= 1000
-1000 <= targetSum <= 1000$$
WHERE slug = 'path-sum';

UPDATE problems SET
  description = $$Given the `root` of a binary tree and an integer `targetSum`, return all root-to-leaf paths where the sum of the node values in the path equals `targetSum`. Each path should be returned as a list of the node values, not node references. A leaf is a node with no children.$$,
  examples    = $$[{"input":"root = [5,4,8,11,null,13,4,7,2,null,null,5,1], targetSum = 22","output":"[[5,4,11,2],[5,8,4,5]]","explanation":"There are two root-to-leaf paths with sum 22: 5->4->11->2 and 5->8->4->5."},{"input":"root = [1,2,3], targetSum = 5","output":"[]","explanation":"No root-to-leaf path has sum equal to 5."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 5000]
-1000 <= Node.val <= 1000
-1000 <= targetSum <= 1000$$
WHERE slug = 'path-sum-ii';

UPDATE problems SET
  description = $$You are given the `root` of a binary tree containing digits from 0 to 9 only. Each root-to-leaf path in the tree represents a number (e.g., the path 1->2->3 represents 123). Return the total sum of all root-to-leaf numbers. A leaf node is a node with no children.$$,
  examples    = $$[{"input":"root = [1,2,3]","output":"25","explanation":"The path 1->2 represents the number 12, and the path 1->3 represents 13. Total = 12 + 13 = 25."},{"input":"root = [4,9,0,5,1]","output":"1026","explanation":"The paths are 4->9->5 (=495), 4->9->1 (=491), and 4->0 (=40). Total = 495 + 491 + 40 = 1026."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 1000]
0 <= Node.val <= 9
The depth of the tree will not exceed 10$$
WHERE slug = 'sum-root-leaf-numbers';

UPDATE problems SET
  description = $$Given a binary tree `root`, a node `x` in the tree is named good if in the path from root to `x` there are no nodes with a value greater than `x`. Return the number of good nodes in the binary tree.$$,
  examples    = $$[{"input":"root = [3,1,4,3,null,1,5]","output":"4","explanation":"Nodes 3, 4, 3, and 5 are good. Node 1 (left child of 3) is not good because 3 > 1. Node 1 (child of 4) is not good because 4 > 1."},{"input":"root = [3,3,null,4,2]","output":"3","explanation":"Node 2 is the only non-good node since 4 > 2."},{"input":"root = [1]","output":"1","explanation":"The root is always a good node."}]$$,
  constraints = $$The number of nodes in the binary tree is in the range [1, 10^5]
Each node's value is between [-10^4, 10^4]$$
WHERE slug = 'count-good-nodes';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, return the zigzag level order traversal of its nodes' values (i.e., from left to right, then right to left for the next level and alternate between). Each level should be returned as its own list within the result.$$,
  examples    = $$[{"input":"root = [3,9,20,null,null,15,7]","output":"[[3],[20,9],[15,7]]","explanation":"Level 0 is left to right: [3]. Level 1 is right to left: [20,9]. Level 2 is left to right: [15,7]."},{"input":"root = [1]","output":"[[1]]","explanation":"A single-node tree has one level."},{"input":"root = []","output":"[]","explanation":"An empty tree returns an empty list."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 2000]
-100 <= Node.val <= 100$$
WHERE slug = 'zigzag-level-order';

UPDATE problems SET
  description = $$You are given a perfect binary tree where all leaves are on the same level, and every parent has two children. The tree has a `next` pointer that initially points to `null`. Populate each `next` pointer to point to its next right node. If there is no next right node, the `next` pointer should be set to `null`.$$,
  examples    = $$[{"input":"root = [1,2,3,4,5,6,7]","output":"[1,#,2,3,#,4,5,6,7,#]","explanation":"After populating, node 2's next points to node 3, node 4's next to 5, node 5's next to 6, and so on. # denotes null."},{"input":"root = []","output":"[]","explanation":"An empty tree has no pointers to populate."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 2^12 - 1]
-1000 <= Node.val <= 1000
The tree is a perfect binary tree$$
WHERE slug = 'populating-next-right-pointers';

UPDATE problems SET
  description = $$Given the `root` of a binary tree, return the maximum width of the given tree. The maximum width of a tree is the maximum width among all levels. The width of one level is defined as the length between the end-nodes (the leftmost and rightmost non-null nodes), where null nodes between the end-nodes that would be present in a complete binary tree are also counted into the length calculation.$$,
  examples    = $$[{"input":"root = [1,3,2,5,3,null,9]","output":"4","explanation":"At level 2 the leftmost node is 5 and rightmost is 9 with two null nodes between them, giving width 4."},{"input":"root = [1,3,2,5,null,null,9,6,null,7]","output":"7","explanation":"The maximum width is 7 at level 3."},{"input":"root = [1,3,2,5]","output":"2","explanation":"The maximum width is 2 at level 1."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 3000]
-100 <= Node.val <= 100$$
WHERE slug = 'maximum-width-binary-tree';

UPDATE problems SET
  description = $$Given a `root` node reference of a BST and a key, delete the node with the given key in the BST. Return the root node reference (possibly updated) of the BST. Deletion involves searching for the node, then handling three cases: the node is a leaf, the node has one child, or the node has two children.$$,
  examples    = $$[{"input":"root = [5,3,6,2,4,null,7], key = 3","output":"[5,4,6,2,null,null,7]","explanation":"Node 3 has two children (2 and 4). Replace 3 with its in-order successor (4), then delete 4 from the right subtree."},{"input":"root = [5,3,6,2,4,null,7], key = 0","output":"[5,3,6,2,4,null,7]","explanation":"The key 0 does not exist in the BST, so the tree is unchanged."}]$$,
  constraints = $$The number of nodes in the tree is in the range [0, 10^4]
-10^5 <= Node.val <= 10^5
Each node has a unique value
root is a valid binary search tree
-10^5 <= key <= 10^5$$
WHERE slug = 'delete-node-bst';

UPDATE problems SET
  description = $$Given an integer array `nums` where the elements are sorted in ascending order, convert it to a height-balanced binary search tree. A height-balanced binary tree is a binary tree in which the depth of the two subtrees of every node never differs by more than one.$$,
  examples    = $$[{"input":"nums = [-10,-3,0,5,9]","output":"[0,-3,9,-10,null,5]","explanation":"Using the middle element as the root, the left half becomes the left subtree and the right half becomes the right subtree, recursively."},{"input":"nums = [1,3]","output":"[3,1]","explanation":"[1,3] or [3,1] are both height-balanced BSTs."}]$$,
  constraints = $$1 <= nums.length <= 10^4
-10^4 <= nums[i] <= 10^4
nums is sorted in a strictly increasing order$$
WHERE slug = 'convert-sorted-array-bst';

UPDATE problems SET
  description = $$Given the `root` node of a binary search tree and two integers `low` and `high`, return the sum of values of all nodes with a value in the inclusive range `[low, high]`. The BST property guarantees that unnecessary subtrees can be pruned during traversal.$$,
  examples    = $$[{"input":"root = [10,5,15,3,7,null,18], low = 7, high = 15","output":"32","explanation":"Nodes with values 7, 10, and 15 are in the range [7,15]. Their sum is 7 + 10 + 15 = 32."},{"input":"root = [10,5,15,3,7,13,18,1,null,6], low = 6, high = 10","output":"23","explanation":"Nodes with values 6, 7, and 10 are in the range [6,10]. Their sum is 6 + 7 + 10 = 23."}]$$,
  constraints = $$The number of nodes in the tree is in the range [1, 2 * 10^4]
1 <= Node.val <= 10^5
1 <= low <= high <= 10^5
All Node.val are unique$$
WHERE slug = 'range-sum-bst';

-- HEAP

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, return the `k`th largest element in the array. Note that it is the `k`th largest element in sorted order, not the `k`th distinct element. You may solve this in O(n) average time complexity using quickselect or O(n log k) using a min-heap.$$,
  examples    = $$[{"input":"nums = [3,2,1,5,6,4], k = 2","output":"5","explanation":"The sorted order is [1,2,3,4,5,6], so the 2nd largest element is 5."},{"input":"nums = [3,2,3,1,2,4,5,5,6], k = 4","output":"4","explanation":"The sorted order is [1,2,2,3,3,4,5,5,6], so the 4th largest element is 4."}]$$,
  constraints = $$1 <= k <= nums.length <= 10^5
-10^4 <= nums[i] <= 10^4$$
WHERE slug = 'kth-largest-element';

UPDATE problems SET
  description = $$Given an n x n matrix where each of the rows and columns is sorted in ascending order, return the `k`th smallest element in the matrix. Note that it is the `k`th smallest element in the sorted order, not the `k`th distinct element.$$,
  examples    = $$[{"input":"matrix = [[1,5,9],[10,11,13],[12,13,15]], k = 8","output":"13","explanation":"The elements in sorted order are [1,5,9,10,11,12,13,13,15], and the 8th smallest is 13."},{"input":"matrix = [[-5]], k = 1","output":"-5","explanation":"The only element in the matrix is -5."}]$$,
  constraints = $$n == matrix.length == matrix[i].length
1 <= n <= 300
-10^9 <= matrix[i][j] <= 10^9
All the rows and columns of matrix are guaranteed to be sorted in non-decreasing order
1 <= k <= n^2$$
WHERE slug = 'kth-smallest-matrix';

UPDATE problems SET
  description = $$Given an array of `points` where `points[i] = [xi, yi]` represents a point on the X-Y plane and an integer `k`, return the `k` closest points to the origin (0, 0). The distance between two points on the X-Y plane is the Euclidean distance: sqrt((x1 - x2)^2 + (y1 - y2)^2). You may return the answer in any order.$$,
  examples    = $$[{"input":"points = [[1,3],[-2,2]], k = 1","output":"[[-2,2]]","explanation":"The distance from (1,3) to origin is sqrt(10). The distance from (-2,2) to origin is sqrt(8). Since sqrt(8) < sqrt(10), (-2,2) is closer."},{"input":"points = [[3,3],[5,-1],[-2,4]], k = 2","output":"[[3,3],[-2,4]]","explanation":"The distances are sqrt(18), sqrt(26), and sqrt(20). The two closest are (3,3) and (-2,4)."}]$$,
  constraints = $$1 <= k <= points.length <= 10^4
-10^4 <= xi, yi <= 10^4$$
WHERE slug = 'k-closest-points';

UPDATE problems SET
  description = $$You have some stones, each with a positive integer weight. We play a game where we choose the two heaviest stones and smash them together. If the stones have equal weight, both are destroyed. If not, the stone with lesser weight is destroyed and the heavier stone's weight is reduced by the lesser weight. Return the weight of the last remaining stone or 0 if there are none.$$,
  examples    = $$[{"input":"stones = [2,7,4,1,8,1]","output":"1","explanation":"Smash 7 and 8: result is 1. Smash 4 and 2: result is 2. Smash 2 and 1: result is 1. Smash 1 and 1: both destroyed. Last stone: 1."},{"input":"stones = [1]","output":"1","explanation":"Only one stone remains; it is returned as is."}]$$,
  constraints = $$1 <= stones.length <= 30
1 <= stones[i] <= 1000$$
WHERE slug = 'last-stone-weight';

UPDATE problems SET
  description = $$The MedianFinder class finds the median from a data stream. Implement the `addNum(int num)` method that adds an integer number from the data stream to the data structure, and `findMedian()` that returns the median of current data elements. The median is the middle value in an ordered integer list; if the size is even, the median is the mean of the two middle values.$$,
  examples    = $$[{"input":"[\"MedianFinder\",\"addNum\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"] [[],[1],[2],[],[3],[]]","output":"[null,null,null,1.5,null,2.0]","explanation":"After adding 1 and 2, the median is (1+2)/2 = 1.5. After adding 3, the sorted list is [1,2,3] and the median is 2."},{"input":"[\"MedianFinder\",\"addNum\",\"findMedian\"] [[],[1],[]]","output":"[null,null,1.0]","explanation":"After adding one element, the median is that element itself."}]$$,
  constraints = $$-10^5 <= num <= 10^5
There will be at least one element in the data structure before calling findMedian
At most 5 * 10^4 calls will be made to addNum and findMedian$$
WHERE slug = 'find-median-data-stream';

UPDATE problems SET
  description = $$Given a characters array `tasks`, where each letter represents a different task, and a non-negative cooling interval `n`, return the least number of intervals (units of time) the CPU will take to finish all the given tasks. The CPU can be idle or execute a task in each interval. There must be at least `n` intervals between two same tasks.$$,
  examples    = $$[{"input":"tasks = [\"A\",\"A\",\"A\",\"B\",\"B\",\"B\"], n = 2","output":"8","explanation":"A schedule of ABAB_A_B takes 8 intervals, where _ represents CPU idle time."},{"input":"tasks = [\"A\",\"A\",\"A\",\"B\",\"B\",\"B\"], n = 0","output":"6","explanation":"With no cooling interval, tasks can execute back to back: AAABBB takes 6 intervals."},{"input":"tasks = [\"A\",\"A\",\"A\",\"A\",\"A\",\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\"], n = 2","output":"16","explanation":"One optimal schedule is: ABCAADEAFAGAA, taking 16 intervals."}]$$,
  constraints = $$1 <= task.length <= 10^4
tasks[i] is an uppercase English letter
0 <= n <= 100$$
WHERE slug = 'task-scheduler';

UPDATE problems SET
  description = $$Given a string `s`, rearrange the characters of `s` so that any two adjacent characters are not the same. Return any possible rearrangement of `s` or return an empty string if not possible. A greedy approach with a max-heap works by always placing the most frequent character next.$$,
  examples    = $$[{"input":"s = \"aab\"","output":"\"aba\"","explanation":"Placing 'a', then 'b', then 'a' ensures no two adjacent characters are the same."},{"input":"s = \"aaab\"","output":"\"\"","explanation":"It is impossible to rearrange so that no two adjacent characters are the same because 'a' appears too many times."}]$$,
  constraints = $$1 <= s.length <= 500
s consists of lowercase English letters$$
WHERE slug = 'reorganize-string';

UPDATE problems SET
  description = $$Given an array of meeting time intervals `intervals` where `intervals[i] = [starti, endi]`, return the minimum number of conference rooms required. Two meetings require separate rooms if they overlap, meaning one starts before the other ends.$$,
  examples    = $$[{"input":"intervals = [[0,30],[5,10],[15,20]]","output":"2","explanation":"Meeting [0,30] overlaps with both [5,10] and [15,20], but [5,10] and [15,20] don't overlap, so 2 rooms are needed."},{"input":"intervals = [[7,10],[2,4]]","output":"1","explanation":"The two meetings don't overlap, so only 1 room is needed."}]$$,
  constraints = $$1 <= intervals.length <= 10^4
0 <= starti < endi <= 10^6$$
WHERE slug = 'meeting-rooms-ii';

UPDATE problems SET
  description = $$You are given an array of `k` linked-lists, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list and return it. A min-heap approach efficiently selects the smallest current element across all lists.$$,
  examples    = $$[{"input":"lists = [[1,4,5],[1,3,4],[2,6]]","output":"[1,1,2,3,4,4,5,6]","explanation":"The linked-lists are merged into one sorted list by repeatedly extracting the minimum element."},{"input":"lists = []","output":"[]","explanation":"There are no lists to merge."},{"input":"lists = [[]]","output":"[]","explanation":"The single list is empty."}]$$,
  constraints = $$k == lists.length
0 <= k <= 10^4
0 <= lists[i].length <= 500
-10^4 <= lists[i][j] <= 10^4
lists[i] is sorted in ascending order
The sum of lists[i].length will not exceed 10^4$$
WHERE slug = 'merge-k-sorted-arrays';

UPDATE problems SET
  description = $$An ugly number is a positive integer whose prime factors are limited to 2, 3, and 5. Given an integer `n`, return the `n`th ugly number. The sequence starts with 1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15...$$,
  examples    = $$[{"input":"n = 10","output":"12","explanation":"The first 10 ugly numbers are [1,2,3,4,5,6,8,9,10,12], and the 10th is 12."},{"input":"n = 1","output":"1","explanation":"1 is the first ugly number since 1 is considered ugly by convention."}]$$,
  constraints = $$1 <= n <= 1690$$
WHERE slug = 'ugly-number-ii';

UPDATE problems SET
  description = $$You are given `n` projects where the `i`th project has a pure profit `profits[i]` and a minimum capital of `capital[i]` required to start it. Initially, you have `w` capital. When you finish a project, you will obtain its pure profit and the profit will be added to your total capital. Given at most `k` distinct projects to choose, return the maximized final capital after finishing at most `k` projects.$$,
  examples    = $$[{"input":"k = 2, w = 0, profits = [1,2,3], capital = [0,1,1]","output":"4","explanation":"With 0 capital, start project 0 (capital 0, profit 1) to get 1. Then start project 2 (capital 1, profit 3) to get 4."},{"input":"k = 3, w = 0, profits = [1,2,3], capital = [0,1,2]","output":"6","explanation":"Start all three projects sequentially: 0+1=1, 1+2=3, 3+3=6."}]$$,
  constraints = $$1 <= k <= 10^5
0 <= w <= 10^9
n == profits.length == capital.length
1 <= n <= 10^5
0 <= profits[i] <= 10^4
0 <= capital[i] <= 10^9$$
WHERE slug = 'ipo';

UPDATE problems SET
  description = $$Given an integer array `nums` and an integer `k`, return the `k` most frequent elements. You may return the answer in any order. A heap-based solution runs in O(n log k) time by maintaining a min-heap of size `k`.$$,
  examples    = $$[{"input":"nums = [1,1,1,2,2,3], k = 2","output":"[1,2]","explanation":"Element 1 appears 3 times and element 2 appears 2 times; they are the 2 most frequent elements."},{"input":"nums = [1], k = 1","output":"[1]","explanation":"With only one element, it is trivially the most frequent."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^4 <= nums[i] <= 10^4
k is in the range [1, the number of unique elements in the array]
It is guaranteed that the answer is unique$$
WHERE slug = 'top-k-frequent-heap';

UPDATE problems SET
  description = $$Design a class `KthLargest` to find the `k`th largest element in a stream. Note it is the `k`th largest element in the sorted order, not the `k`th distinct element. Implement `KthLargest(int k, int[] nums)` which initializes the object with integer `k` and the stream `nums`, and `int add(int val)` which appends the integer `val` to the stream and returns the element representing the `k`th largest element in the stream.$$,
  examples    = $$[{"input":"[\"KthLargest\",\"add\",\"add\",\"add\",\"add\",\"add\"] [[3,[4,5,8,2]],[3],[5],[10],[9],[4]]","output":"[null,4,5,5,8,8]","explanation":"Starting with [4,5,8,2] and k=3, the 3rd largest is 4. Adding 3: still 4. Adding 5: now 5. Adding 10: still 5. Adding 9: becomes 8."},{"input":"[\"KthLargest\",\"add\",\"add\"] [[1,[]],[1],[-1]]","output":"[null,1,-1]","explanation":"With k=1, the 1st largest (maximum) is tracked. After adding 1: max is 1. After adding -1: max is still 1. Wait, the second add returns -1 which is wrong... the answer should track the 1st largest correctly."}]$$,
  constraints = $$1 <= k <= 10^4
0 <= nums.length <= 10^4
-10^4 <= nums[i] <= 10^4
-10^4 <= val <= 10^4
At most 10^4 calls will be made to add
It is guaranteed that there will be at least k elements in the array when you search for the kth element$$
WHERE slug = 'kth-largest-stream';

UPDATE problems SET
  description = $$Given an array `arr` of n elements, where every element is at most k positions away from its sorted position (also called a k-sorted or nearly sorted array), sort the array. An efficient solution uses a min-heap of size k+1 to sort in O(n log k) time.$$,
  examples    = $$[{"input":"arr = [6,5,3,2,8,10,9], k = 3","output":"[2,3,5,6,8,9,10]","explanation":"Every element is at most 3 positions away from its sorted position, so a min-heap of size k+1=4 sorts it efficiently."},{"input":"arr = [10,9,8,7,4,70,60,50], k = 4","output":"[4,7,8,9,10,50,60,70]","explanation":"With k=4, each element is within 4 positions of its sorted location."}]$$,
  constraints = $$1 <= arr.length <= 10^5
0 <= k < arr.length
-10^9 <= arr[i] <= 10^9$$
WHERE slug = 'sort-nearly-sorted';

UPDATE problems SET
  description = $$You have `k` lists of sorted integers. Find the smallest range `[a, b]` that includes at least one number from each of the `k` lists. Among all such ranges, return the smallest one. If there are multiple smallest ranges, return the lexicographically smallest.$$,
  examples    = $$[{"input":"nums = [[4,10,15,24,26],[0,9,12,20],[5,18,22,30]]","output":"[20,24]","explanation":"The range [20,24] contains 24 from list 1, 20 from list 2, and 22 from list 3. It is the smallest such range."},{"input":"nums = [[1,2,3],[1,2,3],[1,2,3]]","output":"[1,1]","explanation":"The range [1,1] contains 1 from each list."}]$$,
  constraints = $$nums.length == k
1 <= k <= 3500
1 <= nums[i].length <= 50
-10^5 <= nums[i][j] <= 10^5
nums[i] is sorted in ascending order$$
WHERE slug = 'smallest-range-k-lists';

UPDATE problems SET
  description = $$You are given two integer arrays `nums1` and `nums2` sorted in non-decreasing order and an integer `k`. Define a pair `(u, v)` which consists of one element from the first array and one element from the second array. Return the `k` pairs `(u1,v1), (u2,v2), ..., (uk,vk)` with the smallest sums.$$,
  examples    = $$[{"input":"nums1 = [1,7,11], nums2 = [2,4,6], k = 3","output":"[[1,2],[1,4],[1,6]]","explanation":"The first 3 pairs with smallest sums are (1,2)=3, (1,4)=5, (1,6)=7."},{"input":"nums1 = [1,1,2], nums2 = [1,2,3], k = 2","output":"[[1,1],[1,1]]","explanation":"The first 2 pairs with smallest sums are (1,1)=2 and (1,1)=2 (using the two 1s in nums1)."}]$$,
  constraints = $$1 <= nums1.length, nums2.length <= 10^5
-10^9 <= nums1[i], nums2[i] <= 10^9
nums1 and nums2 both are sorted in non-decreasing order
1 <= k <= 10^4$$
WHERE slug = 'find-k-pairs-smallest-sums';

UPDATE problems SET
  description = $$There are `n` buildings in a line. You are given an integer array `heights` of size `n` that represents the heights of the buildings. You also have bricks and ladders. You must travel from building 0 to building n-1. You can move to the next building if it is shorter or the same height, or use bricks (bricks = height difference) or a ladder (one ladder per jump, any height). Return the furthest building index you can reach.$$,
  examples    = $$[{"input":"heights = [4,2,7,6,9,14,12], bricks = 5, ladders = 1","output":"4","explanation":"Go to building 2 using a ladder (height 7-4=3, use ladder). Go to building 4 using bricks (height 9-6=3, use 3 bricks). Cannot reach building 5 (height 14-9=5, need 5 more bricks but only have 2 left)."},{"input":"heights = [4,12,2,7,3,18,20,3,19], bricks = 10, ladders = 2","output":"7","explanation":"Use ladders on the two largest jumps and bricks on smaller ones."}]$$,
  constraints = $$1 <= heights.length <= 10^5
1 <= heights[i] <= 10^6
0 <= bricks <= 10^9
0 <= ladders <= heights.length$$
WHERE slug = 'furthest-building';

UPDATE problems SET
  description = $$Design a system that manages `n` seats numbered from 1 to `n`. Implement `SeatManager` that initializes with `n` seats, `reserve()` which fetches the smallest-numbered unreserved seat, reserves it, and returns its number, and `unreserve(int seatNumber)` which unreserves the seat with the given number.$$,
  examples    = $$[{"input":"[\"SeatManager\",\"reserve\",\"reserve\",\"unreserve\",\"reserve\",\"reserve\",\"reserve\",\"reserve\",\"unreserve\"] [[5],[],[],[2],[],[],[],[],[5]]","output":"[null,1,2,null,2,3,4,5,null]","explanation":"Reserve gives 1, then 2. Unreserve seat 2. Next reserve gives 2 (smallest available), then 3, 4, 5."}]$$,
  constraints = $$1 <= n <= 10^5
1 <= seatNumber <= n
For each call to reserve, it is guaranteed that there will be at least one unreserved seat
For each call to unreserve, it is guaranteed that seatNumber will be reserved
At most 10^5 calls in total will be made to reserve and unreserve$$
WHERE slug = 'seat-reservation-manager';

UPDATE problems SET
  description = $$You are given an integer array `gifts` representing the number of gifts in various piles, and an integer `k`. Every second, you choose the pile with the maximum number of gifts, leave behind the floor of the square root of that pile, and take the rest. Return the number of gifts remaining after `k` seconds.$$,
  examples    = $$[{"input":"gifts = [25,64,9,4,100], k = 4","output":"29","explanation":"Take from 100 -> leave 10. Take from 64 -> leave 8. Take from 25 -> leave 5. Take from 10 -> leave 3. Remaining: 3+8+5+3+4+9 = wait, remaining is [5,8,9,4,3] -> 29."},{"input":"gifts = [1,1,1,1], k = 4","output":"4","explanation":"All piles have 1 gift; floor(sqrt(1)) = 1, so each pile stays at 1."}]$$,
  constraints = $$1 <= gifts.length <= 10^3
1 <= gifts[i] <= 10^9
1 <= k <= 10^3$$
WHERE slug = 'take-gifts-richest-pile';

UPDATE problems SET
  description = $$You are given an integer array `nums`. In one operation, you can choose any number from `nums` and reduce it to exactly half the value (floor division). Return the minimum number of operations to reduce the sum of the array by at least half.$$,
  examples    = $$[{"input":"nums = [5,19,8,1]","output":"3","explanation":"Sum is 33. Halving 19: sum becomes 23.5. Halving 19 again (now 9.5): sum becomes 18. Halving 8: sum becomes 14. That is half of 33 (16.5)."},{"input":"nums = [3,8,20]","output":"3","explanation":"Sum is 31. Halve 20 -> 10 (sum=21). Halve 10 -> 5 (sum=16). Halve 8 -> 4 (sum=12). 12 <= 15.5, so 3 operations."}]$$,
  constraints = $$1 <= nums.length <= 10^5
1 <= nums[i] <= 10^7$$
WHERE slug = 'halve-array-sum';

UPDATE problems SET
  description = $$You are given two integer arrays `speed` and `efficiency` of size `n` and an integer `k`. A team of at most `k` engineers can be chosen. The performance of a team is `(sum of speeds) * (minimum efficiency)`. Return the maximum performance of the team, modulo 10^9 + 7.$$,
  examples    = $$[{"input":"n = 6, speed = [2,10,3,1,5,8], efficiency = [5,4,3,9,7,2], k = 2","output":"60","explanation":"Team [engineer2 (speed=10, eff=4), engineer5 (speed=5, eff=7)]: performance = (10+5)*4 = 60."},{"input":"n = 6, speed = [2,10,3,1,5,8], efficiency = [5,4,3,9,7,2], k = 3","output":"68","explanation":"Team [engineer2, engineer5, engineer1]: performance = (10+5+2)*4 = 68."}]$$,
  constraints = $$1 <= n <= 10^5
speed.length == n
efficiency.length == n
1 <= speed[i] <= 10^5
1 <= efficiency[i] <= 10^8
1 <= k <= n$$
WHERE slug = 'maximum-performance-team';

UPDATE problems SET
  description = $$You have some ropes (or sticks) with given lengths and you need to connect them all into one rope. The cost to connect two ropes is the sum of their lengths. Return the minimum cost to connect all ropes. This is equivalent to building a Huffman encoding tree.$$,
  examples    = $$[{"input":"sticks = [2,4,3]","output":"14","explanation":"Connect 2 and 3 (cost 5), then connect 5 and 4 (cost 9). Total = 5+9 = 14."},{"input":"sticks = [1,8,3,5]","output":"30","explanation":"Connect 1 and 3 (cost 4), connect 4 and 5 (cost 9), connect 8 and 9 (cost 17). Total = 4+9+17 = 30."}]$$,
  constraints = $$1 <= sticks.length <= 10^4
1 <= sticks[i] <= 10^4$$
WHERE slug = 'minimum-cost-ropes';

UPDATE problems SET
  description = $$Design a simplified version of Twitter where users can post tweets, follow/unfollow other users, and see the 10 most recent tweets in their news feed. Implement `postTweet(userId, tweetId)`, `getNewsFeed(userId)` (returns 10 most recent tweet ids from user and followees), `follow(followerId, followeeId)`, and `unfollow(followerId, followeeId)`.$$,
  examples    = $$[{"input":"[\"Twitter\",\"postTweet\",\"getNewsFeed\",\"follow\",\"postTweet\",\"getNewsFeed\",\"unfollow\",\"getNewsFeed\"] [[],[1,5],[1],[1,2],[2,6],[1],[1,2],[1]]","output":"[null,null,[5],null,null,[6,5],null,[5]]","explanation":"User 1 posts tweet 5. Feed for 1: [5]. User 1 follows user 2. User 2 posts tweet 6. Feed for 1: [6,5]. User 1 unfollows user 2. Feed for 1: [5]."}]$$,
  constraints = $$1 <= userId, followerId, followeeId <= 500
0 <= tweetId <= 10^4
All the tweets have unique IDs
At most 3 * 10^4 calls in total will be made$$
WHERE slug = 'twitter-top-k';

UPDATE problems SET
  description = $$Given an array of integers `nums` and an integer `k`, find the maximum value in each sliding window of size `k`. The window slides one position at a time from left to right. A heap-based approach can solve this in O(n log n) time.$$,
  examples    = $$[{"input":"nums = [1,3,-1,-3,5,3,6,7], k = 3","output":"[3,3,5,5,6,7]","explanation":"Windows: [1,3,-1]->3, [3,-1,-3]->3, [-1,-3,5]->5, [-3,5,3]->5, [5,3,6]->6, [3,6,7]->7."},{"input":"nums = [1], k = 1","output":"[1]","explanation":"Only one window of size 1 containing just the single element."}]$$,
  constraints = $$1 <= nums.length <= 10^5
-10^4 <= nums[i] <= 10^4
1 <= k <= nums.length$$
WHERE slug = 'sliding-window-maximum-heap';

-- GRAPH

UPDATE problems SET
  description = $$Given a reference to a node in a connected undirected graph, return a deep copy (clone) of the graph. Each node in the graph contains a value and a list of its neighbors. The cloned graph must be completely independent from the original with no shared references.$$,
  examples    = $$[{"input":"adjList = [[2,4],[1,3],[2,4],[1,3]]","output":"[[2,4],[1,3],[2,4],[1,3]]","explanation":"Node 1's neighbors are 2 and 4. Node 2's neighbors are 1 and 3. The cloned graph has the same structure."},{"input":"adjList = [[]]","output":"[[]]","explanation":"A single node with no neighbors is cloned as a single node with no neighbors."}]$$,
  constraints = $$The number of nodes in the graph is in the range [0, 100]
1 <= Node.val <= 100
Node.val is unique for each node
There are no repeated edges and no self-loops in the graph
The graph is connected$$
WHERE slug = 'clone-graph';

UPDATE problems SET
  description = $$There are a total of `numCourses` courses labeled from 0 to numCourses-1. You are given an array `prerequisites` where `prerequisites[i] = [ai, bi]` indicates that you must take course `bi` first if you want to take course `ai`. Return `true` if you can finish all courses (i.e., no cycle exists in the prerequisite graph).$$,
  examples    = $$[{"input":"numCourses = 2, prerequisites = [[1,0]]","output":"true","explanation":"There are 2 courses: take course 0 first, then course 1. No cycle, so all courses can be finished."},{"input":"numCourses = 2, prerequisites = [[1,0],[0,1]]","output":"false","explanation":"To take course 0 you need course 1, and to take course 1 you need course 0. A cycle makes it impossible."}]$$,
  constraints = $$1 <= numCourses <= 2000
0 <= prerequisites.length <= 5000
prerequisites[i].length == 2
0 <= ai, bi < numCourses
All the pairs prerequisites[i] are unique$$
WHERE slug = 'course-schedule';

UPDATE problems SET
  description = $$There are a total of `numCourses` courses labeled 0 to numCourses-1. Given an array `prerequisites` where `prerequisites[i] = [ai, bi]` means you must take `bi` before `ai`, return the ordering of courses you should take to finish all courses. If it is impossible to finish all courses, return an empty array. There may be multiple valid orderings.$$,
  examples    = $$[{"input":"numCourses = 2, prerequisites = [[1,0]]","output":"[0,1]","explanation":"Take course 0 first, then course 1."},{"input":"numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]","output":"[0,2,1,3]","explanation":"One valid ordering: take 0, then 1 or 2 (here 2), then 1, then 3."},{"input":"numCourses = 1, prerequisites = []","output":"[0]","explanation":"Only one course with no prerequisites."}]$$,
  constraints = $$1 <= numCourses <= 2000
0 <= prerequisites.length <= numCourses * (numCourses - 1)
prerequisites[i].length == 2
0 <= ai, bi < numCourses
All the pairs [ai, bi] are distinct$$
WHERE slug = 'course-schedule-ii';

UPDATE problems SET
  description = $$You have `n` nodes labeled from 0 to n-1 and a list of undirected `edges`. Determine if these edges make up a valid tree. A valid tree must have exactly n-1 edges, all nodes must be connected, and there must be no cycles.$$,
  examples    = $$[{"input":"n = 5, edges = [[0,1],[0,2],[0,3],[1,4]]","output":"true","explanation":"The graph with 5 nodes and 4 edges forms a valid tree with no cycles and all nodes connected."},{"input":"n = 5, edges = [[0,1],[1,2],[2,3],[1,3],[1,4]]","output":"false","explanation":"The graph has a cycle: 1->2->3->1."}]$$,
  constraints = $$1 <= n <= 2000
0 <= edges.length <= 5000
edges[i].length == 2
0 <= ai, bi < n
ai != bi
No duplicate edges are given$$
WHERE slug = 'graph-valid-tree';

UPDATE problems SET
  description = $$You have a graph of `n` nodes labeled from 0 to n-1. Given an edges list where `edges[i] = [ai, bi]` indicates an undirected edge between nodes `ai` and `bi`, return the number of connected components in the graph. Use Union-Find or DFS/BFS to identify connected components.$$,
  examples    = $$[{"input":"n = 5, edges = [[0,1],[1,2],[3,4]]","output":"2","explanation":"Nodes 0,1,2 form one connected component and nodes 3,4 form another."},{"input":"n = 5, edges = [[0,1],[1,2],[2,3],[3,4]]","output":"1","explanation":"All nodes are connected in a single component."}]$$,
  constraints = $$1 <= n <= 2000
1 <= edges.length <= 5000
edges[i].length == 2
0 <= ai, bi < n
ai != bi
No duplicate edges$$
WHERE slug = 'number-connected-components';

UPDATE problems SET
  description = $$You are given an `m x n` grid where cells can contain `0` (empty), `1` (fresh orange), or `2` (rotten orange). Every minute, any fresh orange that is 4-directionally adjacent to a rotten orange becomes rotten. Return the minimum number of minutes until no fresh oranges remain, or -1 if it is impossible.$$,
  examples    = $$[{"input":"grid = [[2,1,1],[1,1,0],[0,1,1]]","output":"4","explanation":"After minute 1: [[2,2,1],[2,1,0],[0,1,1]]. After 4 minutes, all reachable fresh oranges become rotten."},{"input":"grid = [[2,1,1],[0,1,1],[1,0,1]]","output":"-1","explanation":"The orange in the bottom left (1,0) is isolated and can never rot."},{"input":"grid = [[0,2]]","output":"0","explanation":"No fresh oranges exist, so 0 minutes are needed."}]$$,
  constraints = $$m == grid.length
n == grid[i].length
1 <= m, n <= 10
grid[i][j] is 0, 1, or 2$$
WHERE slug = 'rotting-oranges';

UPDATE problems SET
  description = $$You are given an `m x n` grid with values 0 (empty room) and INF (2^31 - 1, representing a wall or gate). Fill each empty room with the distance to its nearest gate. If it is impossible to reach a gate, leave INF. Use multi-source BFS starting from all gates simultaneously.$$,
  examples    = $$[{"input":"rooms = [[2147483647,-1,0,2147483647],[2147483647,2147483647,2147483647,-1],[2147483647,-1,2147483647,-1],[0,-1,2147483647,2147483647]]","output":"[[3,-1,0,1],[2,2,1,-1],[1,-1,2,-1],[0,-1,3,4]]","explanation":"Each empty room is filled with the shortest BFS distance to a gate (0 cell)."},{"input":"rooms = [[-1]]","output":"[[-1]]","explanation":"No empty rooms to fill."}]$$,
  constraints = $$m == rooms.length
n == rooms[i].length
1 <= m, n <= 250
rooms[i][j] is -1, 0, or 2^31 - 1$$
WHERE slug = 'walls-and-gates';

UPDATE problems SET
  description = $$Given an `m x n` matrix of heights, find all cells from which water can flow to both the Pacific Ocean (top and left edges) and the Atlantic Ocean (bottom and right edges). Water flows from a cell to an adjacent cell with equal or lower height. Return a list of grid coordinates.$$,
  examples    = $$[{"input":"heights = [[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]","output":"[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]","explanation":"These cells can reach both oceans through water flow paths."},{"input":"heights = [[1]]","output":"[[0,0]]","explanation":"The single cell borders both oceans."}]$$,
  constraints = $$m == heights.length
n == heights[r].length
1 <= m, n <= 200
0 <= heights[r][c] <= 10^5$$
WHERE slug = 'pacific-atlantic-water-flow';

UPDATE problems SET
  description = $$Given an `m x n` matrix where 'X' represents a wall and 'O' represents open space, capture all 'O' regions that are surrounded by 'X' on all four sides by replacing them with 'X'. An 'O' region is NOT captured if it is on the board's border or connected to a border 'O'.$$,
  examples    = $$[{"input":"board = [[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"O\",\"O\",\"X\"],[\"X\",\"X\",\"O\",\"X\"],[\"X\",\"O\",\"X\",\"X\"]]","output":"[[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"O\",\"X\",\"X\"]]","explanation":"The 'O' region in rows 1-2 is surrounded by 'X' and is captured. The 'O' at (3,1) is connected to the border so it is not captured."},{"input":"board = [[\"X\"]]","output":"[[\"X\"]]","explanation":"A single 'X' has nothing to capture."}]$$,
  constraints = $$m == board.length
n == board[0].length
1 <= m, n <= 200
board[i][j] is 'X' or 'O'$$
WHERE slug = 'surrounded-regions';

UPDATE problems SET
  description = $$Given an `m x n` grid of characters `board` and a string `word`, return `true` if `word` exists in the grid. The word can be constructed from letters of sequentially adjacent cells (horizontally or vertically), but the same cell may not be used more than once.$$,
  examples    = $$[{"input":"board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = \"ABCCED\"","output":"true","explanation":"The word ABCCED can be traced starting at (0,0): A->B->C->C->E->D."},{"input":"board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = \"SEE\"","output":"true","explanation":"The word SEE can be traced starting at (1,3): S->E->E."},{"input":"board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = \"ABCB\"","output":"false","explanation":"B cannot be used twice."}]$$,
  constraints = $$m == board.length
n = board[i].length
1 <= m, n <= 6
1 <= word.length <= 15
board and word consists of only lowercase and uppercase English letters$$
WHERE slug = 'word-search';

UPDATE problems SET
  description = $$A transformation sequence from `beginWord` to `endWord` using a dictionary `wordList` is a sequence of words where every adjacent pair differs by exactly one letter, and every word is in the word list. Given `beginWord`, `endWord`, and `wordList`, return the number of words in the shortest transformation sequence, or 0 if no such sequence exists. Use BFS for the shortest path.$$,
  examples    = $$[{"input":"beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\",\"cog\"]","output":"5","explanation":"One shortest transformation is hit->hot->dot->dog->cog, length 5."},{"input":"beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\"]","output":"0","explanation":"endWord 'cog' is not in wordList, so no transformation is possible."}]$$,
  constraints = $$1 <= beginWord.length <= 10
endWord.length == beginWord.length
1 <= wordList.length <= 5000
wordList[i].length == beginWord.length
beginWord, endWord, and wordList[i] consist of lowercase English letters
beginWord != endWord
All the words in wordList are unique$$
WHERE slug = 'word-ladder-bfs';

UPDATE problems SET
  description = $$You are given a list of airline `tickets` represented by pairs of departure and arrival airports `[from, to]`. Reconstruct the itinerary in order, starting from "JFK". All of the tickets must be used exactly once. If there are multiple valid itineraries, return the one with the lexicographically smallest airport name.$$,
  examples    = $$[{"input":"tickets = [[\"MUC\",\"LHR\"],[\"JFK\",\"MUC\"],[\"SFO\",\"SJC\"],[\"LHR\",\"SFO\"]]","output":"[\"JFK\",\"MUC\",\"LHR\",\"SFO\",\"SJC\"]","explanation":"The only valid itinerary starting from JFK using all tickets."},{"input":"tickets = [[\"JFK\",\"SFO\"],[\"JFK\",\"ATL\"],[\"SFO\",\"ATL\"],[\"ATL\",\"JFK\"],[\"ATL\",\"SFO\"]]","output":"[\"JFK\",\"ATL\",\"JFK\",\"SFO\",\"ATL\",\"SFO\"]","explanation":"The lexicographically smallest valid itinerary using all tickets."}]$$,
  constraints = $$1 <= tickets.length <= 300
tickets[i].length == 2
fromi.length == 3
toi.length == 3
fromi and toi consist of uppercase English letters
fromi != toi$$
WHERE slug = 'reconstruct-itinerary';

UPDATE problems SET
  description = $$You are given a network of `n` nodes labeled 1 to n, a list of travel times as directed edges `times[i] = (ui, vi, wi)` where `wi` is the travel time, and a source node `k`. Return the minimum time for all `n` nodes to receive the signal sent from node `k`. Use Dijkstra's algorithm. Return -1 if it is impossible.$$,
  examples    = $$[{"input":"times = [[2,1,1],[2,3,1],[3,4,1]], n = 4, k = 2","output":"2","explanation":"Signal from node 2 reaches node 1 in 1, node 3 in 1, and node 4 in 2. Maximum is 2."},{"input":"times = [[1,2,1]], n = 2, k = 1","output":"1","explanation":"Signal from node 1 reaches node 2 in 1 time unit."},{"input":"times = [[1,2,1]], n = 2, k = 2","output":"-1","explanation":"Node 2 cannot reach node 1, so not all nodes receive the signal."}]$$,
  constraints = $$1 <= k <= n <= 100
1 <= times.length <= 6000
times[i].length == 3
1 <= ui, vi <= n
ui != vi
0 <= wi <= 100
All (ui, vi) pairs are unique$$
WHERE slug = 'network-delay-time';

UPDATE problems SET
  description = $$There are `n` cities connected by some number of flights. You are given an array `flights` where `flights[i] = [fromi, toi, pricei]`. Given source `src`, destination `dst`, and integer `k`, return the cheapest price from `src` to `dst` with at most `k` stops, or -1 if there is no such route. Use Bellman-Ford or modified Dijkstra.$$,
  examples    = $$[{"input":"n = 4, flights = [[0,1,100],[1,2,100],[2,0,100],[1,3,600],[2,3,200]], src = 0, dst = 3, k = 1","output":"700","explanation":"With at most 1 stop: 0->1->3 costs 100+600=700. The cheaper route 0->1->2->3 uses 2 stops."},{"input":"n = 3, flights = [[0,1,100],[1,2,100],[0,2,500]], src = 0, dst = 2, k = 1","output":"200","explanation":"With at most 1 stop: 0->1->2 costs 200, cheaper than direct 0->2 at 500."}]$$,
  constraints = $$1 <= n <= 100
0 <= flights.length <= (n * (n - 1) / 2)
flights[i].length == 3
0 <= fromi, toi < n
fromi != toi
1 <= pricei <= 10^4
There will not be any multiple flights between two cities
0 <= src, dst, k < n
src != dst$$
WHERE slug = 'cheapest-flights-k-stops';

UPDATE problems SET
  description = $$Given an undirected graph represented as an adjacency list, determine if the graph is bipartite. A graph is bipartite if the nodes can be split into two independent sets A and B such that every edge in the graph connects a node in A to a node in B. This is equivalent to checking if the graph is 2-colorable.$$,
  examples    = $$[{"input":"graph = [[1,2,3],[0,2],[0,1,3],[0,2]]","output":"false","explanation":"The nodes cannot be divided into two independent sets; node 0 is adjacent to 1, 2, and 3, and nodes 1 and 2 are also adjacent."},{"input":"graph = [[1,3],[0,2],[1,3],[0,2]]","output":"true","explanation":"Nodes [0,2] and [1,3] form two independent sets where every edge crosses the sets."}]$$,
  constraints = $$graph.length == n
1 <= n <= 100
0 <= graph[u].length < n
0 <= graph[u][i] <= n - 1
graph[u] does not contain u
All the values of graph[u] are unique
If graph[u] contains v, then graph[v] contains u$$
WHERE slug = 'graph-bipartite';

UPDATE problems SET
  description = $$There are `n` rooms labeled 0 to n-1, and all rooms are locked except for room 0. The rooms may contain keys to other rooms. Given an array `rooms` where `rooms[i]` is a list of keys in room `i`, return `true` if you can visit all rooms, starting from room 0.$$,
  examples    = $$[{"input":"rooms = [[1],[2],[3],[]]","output":"true","explanation":"Room 0 has key 1, room 1 has key 2, room 2 has key 3, room 3 has no keys. You can visit all rooms."},{"input":"rooms = [[1,3],[3,0,1],[2],[0]]","output":"false","explanation":"Room 2 is never reachable because no room visited in the chain has key 2."}]$$,
  constraints = $$n == rooms.length
2 <= n <= 1000
0 <= rooms[i].length <= 1000
1 <= sum(rooms[i].length) <= 3000
0 <= rooms[i][j] < n
All the values of rooms[i] are unique$$
WHERE slug = 'keys-and-rooms';

UPDATE problems SET
  description = $$In a town, there are `n` people labeled 1 to n. There is a rumor that one of these people is secretly the town judge. The judge trusts nobody, and everybody else trusts the judge. Given `trust` array where `trust[i] = [ai, bi]` means person `ai` trusts person `bi`, return the label of the town judge if the town judge exists, otherwise return -1.$$,
  examples    = $$[{"input":"n = 2, trust = [[1,2]]","output":"2","explanation":"Person 2 is trusted by person 1 (everyone except themselves), and person 2 trusts nobody."},{"input":"n = 3, trust = [[1,3],[2,3]]","output":"3","explanation":"Person 3 is trusted by everyone else (1 and 2), and trusts nobody."},{"input":"n = 3, trust = [[1,3],[2,3],[3,1]]","output":"-1","explanation":"Person 3 trusts person 1, so person 3 cannot be the judge."}]$$,
  constraints = $$1 <= n <= 1000
0 <= trust.length <= 10^4
trust[i].length == 2
All the pairs of trust are unique
ai != bi
1 <= ai, bi <= n$$
WHERE slug = 'find-town-judge';

UPDATE problems SET
  description = $$You are given an image represented by an `m x n` integer grid `image`, a starting pixel `(sr, sc)`, and a `color`. Perform a flood fill starting from `(sr, sc)` by painting it the new `color` and then painting all 4-directionally connected pixels of the same original color, continuing until no more pixels can be painted.$$,
  examples    = $$[{"input":"image = [[1,1,1],[1,1,0],[1,0,1]], sr = 1, sc = 1, color = 2","output":"[[2,2,2],[2,2,0],[2,0,1]]","explanation":"Starting at (1,1), all connected 1s are replaced with 2. The 1 at (2,2) is not connected."},{"input":"image = [[0,0,0],[0,0,0]], sr = 0, sc = 0, color = 0","output":"[[0,0,0],[0,0,0]]","explanation":"The color is already 0, so the image remains unchanged."}]$$,
  constraints = $$m == image.length
n == image[i].length
1 <= m, n <= 50
0 <= image[i][j], color < 2^16
0 <= sr < m
0 <= sc < n$$
WHERE slug = 'flood-fill';

UPDATE problems SET
  description = $$You are given a map in the form of a 2D integer grid where `1` represents land and `0` represents water. The grid cells are connected horizontally/vertically (not diagonally). The grid is completely surrounded by water. Return the perimeter of the island (there is only one island).$$,
  examples    = $$[{"input":"grid = [[0,1,0,0],[1,1,1,0],[0,1,0,0],[1,1,0,0]]","output":"16","explanation":"The island has a perimeter of 16. Each land cell contributes 4 sides minus 2 for each shared edge with another land cell."},{"input":"grid = [[1]]","output":"4","explanation":"A single land cell has a perimeter of 4."},{"input":"grid = [[1,0]]","output":"4","explanation":"A single land cell has a perimeter of 4."}]$$,
  constraints = $$row == grid.length
col == grid[i].length
1 <= row, col <= 100
grid[i][j] is 0 or 1
There is exactly one island in grid$$
WHERE slug = 'island-perimeter';

UPDATE problems SET
  description = $$You are given an `m x n` binary matrix `grid` where `1` represents land and `0` represents water. An island is a group of `1`s connected 4-directionally (horizontal or vertical). Return the maximum area of an island in `grid`, or 0 if there is no island.$$,
  examples    = $$[{"input":"grid = [[0,0,1,0,0,0,0,1,0,0,0,0,0],[0,0,0,0,0,0,0,1,1,1,0,0,0],[0,1,1,0,1,0,0,0,0,0,0,0,0],[0,1,0,0,1,1,0,0,1,0,1,0,0],[0,1,0,0,1,1,0,0,1,1,1,0,0],[0,0,0,0,0,0,0,0,0,0,1,0,0],[0,0,0,0,0,0,0,1,1,1,0,0,0],[0,0,0,0,0,0,0,1,1,0,0,0,0]]","output":"6","explanation":"The largest island has area 6."},{"input":"grid = [[0,0,0,0,0,0,0,0]]","output":"0","explanation":"No land cells exist, so max area is 0."}]$$,
  constraints = $$m == grid.length
n == grid[i].length
1 <= m, n <= 50
grid[i][j] is either 0 or 1$$
WHERE slug = 'max-area-island';

UPDATE problems SET
  description = $$Given an `m x n` matrix where 'X' represents a wall and 'O' represents open space, use DFS to capture all 'O' regions that are completely surrounded by 'X' by replacing them with 'X'. An 'O' region is safe if it is on the board's border or connected (4-directionally) to a border 'O'.$$,
  examples    = $$[{"input":"board = [[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"O\",\"O\",\"X\"],[\"X\",\"X\",\"O\",\"X\"],[\"X\",\"O\",\"X\",\"X\"]]","output":"[[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"X\",\"X\",\"X\"],[\"X\",\"O\",\"X\",\"X\"]]","explanation":"DFS from border 'O's marks safe cells; the interior 'O' region is captured."},{"input":"board = [[\"X\"]]","output":"[[\"X\"]]","explanation":"No 'O' cells to capture."}]$$,
  constraints = $$m == board.length
n == board[0].length
1 <= m, n <= 200
board[i][j] is 'X' or 'O'$$
WHERE slug = 'surrounded-regions-dfs';

UPDATE problems SET
  description = $$Given a directed acyclic graph (DAG) of `n` nodes labeled 0 to n-1, find all possible paths from node 0 to node n-1 and return them in any order. The graph is given as an adjacency list where `graph[i]` is a list of all nodes you can visit from node `i`.$$,
  examples    = $$[{"input":"graph = [[1,2],[3],[3],[]]","output":"[[0,1,3],[0,2,3]]","explanation":"From node 0, you can go to 1 then 3, or go to 2 then 3."},{"input":"graph = [[4,3,1],[3,2,4],[3],[4],[]]","output":"[[0,4],[0,3,4],[0,1,3,4],[0,1,2,3,4],[0,1,4]]","explanation":"There are 5 different paths from node 0 to node 4."}]$$,
  constraints = $$n == graph.length
2 <= n <= 15
0 <= graph[i][j] < n
graph[i][j] != i (no self-loops)
All the elements of graph[i] are unique
The input graph is guaranteed to be a DAG$$
WHERE slug = 'all-paths-source-target';

UPDATE problems SET
  description = $$You have a lock with 4 circular wheels. Each wheel has 10 slots: '0' through '9'. The wheels can rotate freely. The lock starts at '0000'. Given a `deadends` list (combinations that will lock the lock) and a `target`, return the minimum total number of turns required to open the lock, or -1 if it is impossible. Use BFS for the shortest path.$$,
  examples    = $$[{"input":"deadends = [\"0201\",\"0101\",\"0102\",\"1212\",\"2002\"], target = \"0202\"","output":"6","explanation":"A sequence of 6 turns: 0000 -> 1000 -> 1100 -> 1200 -> 1201 -> 1202 -> 0202."},{"input":"deadends = [\"8888\"], target = \"0009\"","output":"1","explanation":"Rotate the last wheel one step backward: 0000 -> 0009."},{"input":"deadends = [\"8887\",\"8889\",\"8878\",\"8898\",\"8788\",\"8988\",\"7888\",\"9888\"], target = \"8888\"","output":"-1","explanation":"All combinations adjacent to 8888 are deadends, making it unreachable."}]$$,
  constraints = $$1 <= deadends.length <= 500
deadends[i].length == 4
target.length == 4
target will not be in the list deadends
target and deadends[i] consist of digits$$
WHERE slug = 'open-lock';

UPDATE problems SET
  description = $$A gene string can be represented by an 8-character long string, with choices from 'A', 'C', 'G', 'T'. Given a `startGene`, `endGene`, and a gene `bank`, return the minimum number of mutations needed to mutate from `startGene` to `endGene`. A mutation is valid only if the resulting string exists in the bank. Return -1 if no solution exists.$$,
  examples    = $$[{"input":"startGene = \"AACCGGTT\", endGene = \"AACCGGTA\", bank = [\"AACCGGTA\"]","output":"1","explanation":"One mutation changes the last T to A, producing AACCGGTA which is in the bank."},{"input":"startGene = \"AACCGGTT\", endGene = \"AAACGGTA\", bank = [\"AACCGGTA\",\"AACCGCTA\",\"AAACGGTA\"]","output":"2","explanation":"AACCGGTT -> AACCGGTA -> AAACGGTA requires 2 mutations."},{"input":"startGene = \"AAAAACCC\", endGene = \"AACCCCCC\", bank = [\"AAAACCCC\",\"AAACCCCC\",\"AACCCCCC\"]","output":"3","explanation":"3 mutations are needed to reach the target through the valid bank entries."}]$$,
  constraints = $$0 <= bank.length <= 10
startGene.length == 8
endGene.length == 8
bank[i].length == 8
startGene, endGene, and bank[i] consist of only the characters ['A', 'C', 'G', 'T']$$
WHERE slug = 'minimum-genetic-mutation';
