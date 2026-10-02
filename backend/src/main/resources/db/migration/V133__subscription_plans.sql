CREATE TABLE lang_subscription_plans (
    plan_code       VARCHAR(30) PRIMARY KEY,
    display_name    VARCHAR(100) NOT NULL,
    price_paise     INT NOT NULL DEFAULT 0,
    currency        VARCHAR(10) NOT NULL DEFAULT 'INR',
    interval_days   INT NOT NULL DEFAULT 0,
    play_product_id VARCHAR(100),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO lang_subscription_plans (plan_code, display_name, price_paise, interval_days, play_product_id) VALUES
    ('FREE',        'Free',        0,      0,   NULL),
    ('PRO_MONTHLY', 'Pro Monthly', 49900,  30,  'langoa_pro_monthly'),
    ('PRO_ANNUAL',  'Pro Annual',  399900, 365, 'langoa_pro_annual');
