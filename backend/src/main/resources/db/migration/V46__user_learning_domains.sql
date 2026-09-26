-- User learning domain enrollment.
-- Supports future multi-domain users (one row per domain per user).
-- is_active marks the currently selected domain for UI/routing purposes.
CREATE TABLE user_learning_domains (
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    domain_code  VARCHAR(20)  NOT NULL REFERENCES cf_domains(code),
    is_active    BOOLEAN      NOT NULL DEFAULT true,
    enrolled_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_code)
);

CREATE INDEX idx_user_learning_domains_user ON user_learning_domains(user_id);

-- Development / QA seed users.
-- Passwords: DsaUser1! and LangUser1! (bcrypt rounds=10).
-- These users are created only if they do not already exist.

INSERT INTO users (id, email, name, password_hash, email_verified, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    'dsa-user@example.com',
    'DSA Learner',
    '$2a$10$WfjDQUUOYpSmTag8wSGi6ehw9Mw4PCbH4F0cFXgMkkPE1xUhW0/nW',
    true,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, email, name, password_hash, email_verified, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000011',
    'language-user@example.com',
    'Language Learner',
    '$2a$10$ok.gI7HOPxaFkCRmdfg4Ne6fL7ULHZERG/AlHjrbVPfOFzAg.PU6m',
    true,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

-- Enroll seed users into their domains.
-- Uses sub-select so it works whether the users were just inserted or already existed.
INSERT INTO user_learning_domains (user_id, domain_code, is_active)
SELECT id, 'dsa', true
FROM users WHERE email = 'dsa-user@example.com'
ON CONFLICT DO NOTHING;

INSERT INTO user_learning_domains (user_id, domain_code, is_active)
SELECT id, 'language', true
FROM users WHERE email = 'language-user@example.com'
ON CONFLICT DO NOTHING;
