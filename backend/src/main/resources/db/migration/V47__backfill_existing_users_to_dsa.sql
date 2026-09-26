-- Back-fill all existing users into the DSA domain.
-- Any user without any domain enrollment gets enrolled in DSA (the original product).
-- Skips users already enrolled in any domain.
INSERT INTO user_learning_domains (user_id, domain_code, is_active)
SELECT id, 'dsa', true
FROM users
WHERE id NOT IN (SELECT DISTINCT user_id FROM user_learning_domains)
ON CONFLICT DO NOTHING;
