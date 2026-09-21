-- =============================================
-- AI Credit Wallet
-- =============================================
CREATE TABLE ai_credit_wallets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    free_credits    INT  NOT NULL DEFAULT 10,
    paid_credits    INT  NOT NULL DEFAULT 0,
    lifetime_used   INT  NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE credit_transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    delta       INT  NOT NULL,
    type        VARCHAR(30) NOT NULL
                CHECK (type IN ('FREE_GRANT','PURCHASE','AI_REVIEW','AI_HINT','AI_DETECT')),
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credit_tx_user ON credit_transactions(user_id);

-- Seed every existing user with a free wallet
INSERT INTO ai_credit_wallets (user_id)
SELECT id FROM users
ON CONFLICT (user_id) DO NOTHING;

-- =============================================
-- Spaced Repetition Reviews
-- =============================================
CREATE TABLE spaced_repetition_reviews (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id   UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    due_date     DATE NOT NULL,
    interval_days INT NOT NULL DEFAULT 1,
    repetition   INT NOT NULL DEFAULT 0,
    ease_factor  NUMERIC(4,2) NOT NULL DEFAULT 2.50,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, problem_id, due_date)
);

CREATE INDEX idx_sr_user_due ON spaced_repetition_reviews(user_id, due_date);
CREATE INDEX idx_sr_user_problem ON spaced_repetition_reviews(user_id, problem_id);

-- =============================================
-- Real Mastery Score on pattern_mastery
-- =============================================
ALTER TABLE pattern_mastery ADD COLUMN IF NOT EXISTS mastery_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE pattern_mastery ADD COLUMN IF NOT EXISTS problems_solved INT NOT NULL DEFAULT 0;
ALTER TABLE pattern_mastery ADD COLUMN IF NOT EXISTS problems_attempted INT NOT NULL DEFAULT 0;
