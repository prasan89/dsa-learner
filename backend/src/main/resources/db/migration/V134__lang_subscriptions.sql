CREATE TABLE lang_subscriptions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_code            VARCHAR(30) NOT NULL REFERENCES lang_subscription_plans(plan_code),
    status               VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    play_purchase_token  VARCHAR(500),
    play_order_id        VARCHAR(100),
    current_period_start TIMESTAMPTZ,
    current_period_end   TIMESTAMPTZ,
    cancelled_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

INSERT INTO lang_subscriptions (user_id, plan_code, status)
SELECT id, 'FREE', 'ACTIVE' FROM users
ON CONFLICT (user_id) DO NOTHING;
