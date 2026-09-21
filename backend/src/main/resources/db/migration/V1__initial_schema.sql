-- Users
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(500),
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Patterns
CREATE TABLE patterns (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug             VARCHAR(100) NOT NULL UNIQUE,
    name             VARCHAR(255) NOT NULL,
    summary          TEXT,
    recognition_clues TEXT,
    template_code    TEXT,
    display_order    INT NOT NULL DEFAULT 0
);

-- Problems
CREATE TABLE problems (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(100) NOT NULL UNIQUE,
    title        VARCHAR(255) NOT NULL,
    difficulty   VARCHAR(10) NOT NULL CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    description  TEXT NOT NULL,
    constraints  TEXT,
    examples     TEXT,
    active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Problem tags
CREATE TABLE problem_tags (
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    tag        VARCHAR(100) NOT NULL,
    PRIMARY KEY (problem_id, tag)
);

-- Problem <-> Pattern many-to-many
CREATE TABLE problem_patterns (
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    pattern_id UUID NOT NULL REFERENCES patterns(id) ON DELETE CASCADE,
    PRIMARY KEY (problem_id, pattern_id)
);

-- Test cases
CREATE TABLE test_cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id      UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    input           TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_hidden       BOOLEAN NOT NULL DEFAULT FALSE,
    display_order   INT NOT NULL DEFAULT 0
);

-- Submissions
CREATE TABLE submissions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id   UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    code         TEXT NOT NULL,
    language     VARCHAR(20) NOT NULL DEFAULT 'JAVA',
    status       VARCHAR(30) NOT NULL,
    runtime_ms   INT,
    memory_kb    INT,
    error_message TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- User progress per problem
CREATE TABLE user_progress (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id      UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    solved          BOOLEAN NOT NULL DEFAULT FALSE,
    attempts        INT NOT NULL DEFAULT 0,
    hints_used      INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    solved_at       TIMESTAMPTZ,
    UNIQUE (user_id, problem_id)
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_submissions_user_id    ON submissions(user_id);
CREATE INDEX idx_submissions_problem_id ON submissions(problem_id);
CREATE INDEX idx_user_progress_user_id  ON user_progress(user_id);
CREATE INDEX idx_problems_difficulty    ON problems(difficulty);
CREATE INDEX idx_problems_active        ON problems(active);
