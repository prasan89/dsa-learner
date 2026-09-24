-- V10: problem_content (rich learning content per problem)
--      problem_followups (interview follow-up questions)
--      user_streaks (daily activity for streak calculation)

-- ─────────────────────────────────────────────────────────────
-- 1. problem_content  (1:1 with problems)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE problem_content (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id        UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    UNIQUE (problem_id),

    -- Why this problem belongs to the pattern
    intuition         TEXT,

    -- Brute force explanation
    brute_force       TEXT,
    brute_time        VARCHAR(100),
    brute_space       VARCHAR(100),

    -- Optimal approach
    optimal_approach  TEXT,
    optimal_time      VARCHAR(100),
    optimal_space     VARCHAR(100),

    -- Pseudocode (language-neutral)
    pseudocode        TEXT,

    -- Java 17/21 production-quality solution
    java_solution     TEXT,

    -- Common mistakes interviewees make
    common_mistakes   TEXT,

    -- Recognition clues specific to this problem (supplements pattern-level clues)
    recognition_note  TEXT,

    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- 2. problem_followups  (interview follow-up questions)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE problem_followups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id  UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    question    TEXT NOT NULL,
    type        VARCHAR(20) NOT NULL DEFAULT 'FOLLOWUP'  -- FOLLOWUP | SENIOR
                CHECK (type IN ('FOLLOWUP', 'SENIOR')),
    sort_order  INT NOT NULL DEFAULT 0
);

-- ─────────────────────────────────────────────────────────────
-- 3. user_activity  (one row per day a user was active)
--    Used to derive streak without scanning all submissions.
-- ─────────────────────────────────────────────────────────────
CREATE TABLE user_activity (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    UNIQUE (user_id, activity_date)
);

CREATE INDEX idx_user_activity_user_date ON user_activity (user_id, activity_date DESC);

-- Back-fill activity dates from existing submissions
INSERT INTO user_activity (user_id, activity_date)
SELECT DISTINCT user_id, DATE(submitted_at)
FROM submissions
ON CONFLICT DO NOTHING;

-- Back-fill from completed spaced repetition reviews
INSERT INTO user_activity (user_id, activity_date)
SELECT DISTINCT user_id, DATE(completed_at)
FROM spaced_repetition_reviews
WHERE completed_at IS NOT NULL
ON CONFLICT DO NOTHING;
