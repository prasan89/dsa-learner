-- Subscription plans
CREATE TABLE user_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan            VARCHAR(20) NOT NULL DEFAULT 'FREE',  -- FREE | PRO
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | CANCELLED | EXPIRED
    razorpay_subscription_id VARCHAR(100),
    current_period_start TIMESTAMPTZ,
    current_period_end   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

-- Payment records (Razorpay order → payment)
CREATE TABLE payment_orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    razorpay_order_id   VARCHAR(100) NOT NULL UNIQUE,
    razorpay_payment_id VARCHAR(100),
    amount_paise        INT NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'INR',
    credits_to_add      INT NOT NULL DEFAULT 0,
    plan_to_upgrade     VARCHAR(20),
    status              VARCHAR(20) NOT NULL DEFAULT 'CREATED', -- CREATED | PAID | FAILED
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed FREE subscription for all existing users
INSERT INTO user_subscriptions (user_id, plan, status)
SELECT id, 'FREE', 'ACTIVE' FROM users;
