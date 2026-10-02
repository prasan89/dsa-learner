-- M5.6: Economy analytics events + suspicious activity detection

-- ── 1. Economy events (analytics) ────────────────────────────────────────────
CREATE TABLE langoa_economy_events (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_name      VARCHAR(60) NOT NULL,
    user_id         UUID        REFERENCES users(id) ON DELETE SET NULL,
    language_code   VARCHAR(10),
    amount          BIGINT,
    currency_type   VARCHAR(20),
    source_ref      VARCHAR(100),
    correlation_id  VARCHAR(100),
    extra           JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lee_user_id      ON langoa_economy_events (user_id);
CREATE INDEX idx_lee_event_name   ON langoa_economy_events (event_name);
CREATE INDEX idx_lee_created_at   ON langoa_economy_events (created_at DESC);

-- ── 2. Suspicious activity log ────────────────────────────────────────────────
CREATE TABLE langoa_suspicious_activity (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        REFERENCES users(id) ON DELETE SET NULL,
    reason      VARCHAR(80) NOT NULL,
    detail      TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lsa_user_id    ON langoa_suspicious_activity (user_id);
CREATE INDEX idx_lsa_created_at ON langoa_suspicious_activity (created_at DESC);

-- ── 3. Extend transaction_type CHECK to include resource types ────────────────
ALTER TABLE langoa_transactions
    DROP CONSTRAINT IF EXISTS langoa_transactions_transaction_type_check;
ALTER TABLE langoa_transactions
    ADD CONSTRAINT langoa_transactions_transaction_type_check
        CHECK (transaction_type IN (
            'LESSON_COMPLETION','BUILDING_PURCHASE','BUILDING_UPGRADE',
            'DAILY_REWARD','INITIAL_GRANT','REFUND',
            'DECORATION_PURCHASE','QUEST_REWARD','ACHIEVEMENT_REWARD',
            'EXPANSION_PURCHASE','COIN_PURCHASE',
            'LESSON_PERFECT','UNIT_COMPLETED','LEVEL_COMPLETED','EXERCISE_REWARD',
            'RESOURCE_PRODUCTION','RESOURCE_COLLECTION','RESOURCE_CONSUMPTION'
        ));

-- ── 4. Index on transactions for economy queries ──────────────────────────────
CREATE INDEX IF NOT EXISTS idx_langoa_txn_user_type_created
    ON langoa_transactions (user_id, transaction_type, created_at DESC);
