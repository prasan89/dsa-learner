-- V25: Add free_access column to problems and mark 20 problems as free tier.
-- Free-tier users can see and attempt these 20 problems without a subscription.

ALTER TABLE problems ADD COLUMN IF NOT EXISTS free_access BOOLEAN NOT NULL DEFAULT false;

-- 20 free problems: mix of easy/medium across common patterns
UPDATE problems SET free_access = true WHERE slug IN (
    -- arrays / hashing
    'two-sum',
    'valid-anagram',
    'contains-duplicate',
    'majority-element',
    'missing-number',
    -- binary search
    'binary-search',
    'search-insert-position',
    'first-bad-version',
    -- linked list
    'linked-list-cycle',
    'middle-linked-list',
    'merge-two-sorted-lists',
    -- trees
    'invert-binary-tree',
    'maximum-depth-binary-tree',
    'same-tree',
    -- stack
    'valid-parentheses',
    'min-stack',
    'baseball-game',
    -- sliding window
    'best-time-to-buy-sell-stock',
    'maximum-average-subarray',
    -- two pointers
    'valid-palindrome'
);
