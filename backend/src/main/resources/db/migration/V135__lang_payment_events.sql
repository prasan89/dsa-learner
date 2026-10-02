CREATE TABLE lang_payment_events (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID REFERENCES users(id) ON DELETE SET NULL,
    play_purchase_token  VARCHAR(500),
    play_order_id        VARCHAR(100),
    event_type           VARCHAR(60) NOT NULL,
    plan_code            VARCHAR(30),
    status               VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    raw_payload          TEXT,
    processed_at         TIMESTAMPTZ,
    failure_reason       TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lpe_user_id   ON lang_payment_events (user_id);
CREATE INDEX idx_lpe_play_order ON lang_payment_events (play_order_id);
CREATE INDEX idx_lpe_status    ON lang_payment_events (status) WHERE status IN ('RECEIVED', 'FAILED');
