-- Hints per problem (3 levels: DIRECTION, APPROACH, PSEUDOCODE)
CREATE TABLE hints (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id   UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    level        INT NOT NULL CHECK (level IN (1, 2, 3)),
    content      TEXT NOT NULL,
    UNIQUE (problem_id, level)
);

-- Track which hints a user has unlocked
CREATE TABLE user_hints (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hint_id    UUID NOT NULL REFERENCES hints(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, hint_id)
);

-- Pattern mastery per user
CREATE TABLE pattern_mastery (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pattern_id  UUID NOT NULL REFERENCES patterns(id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                CHECK (status IN ('NOT_STARTED','LEARNING','PRACTICED','MASTERED')),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, pattern_id)
);

-- Add lesson content columns to patterns
ALTER TABLE patterns ADD COLUMN IF NOT EXISTS lesson_markdown TEXT;
ALTER TABLE patterns ADD COLUMN IF NOT EXISTS time_complexity  VARCHAR(50);
ALTER TABLE patterns ADD COLUMN IF NOT EXISTS space_complexity VARCHAR(50);

CREATE INDEX idx_hints_problem_id         ON hints(problem_id);
CREATE INDEX idx_user_hints_user_id       ON user_hints(user_id);
CREATE INDEX idx_pattern_mastery_user_id  ON pattern_mastery(user_id);
