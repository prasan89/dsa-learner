-- Langoa coin purchase packages — virtual coin bundles purchasable with real money

CREATE TABLE lang_coin_packages (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    package_code    VARCHAR(40)   NOT NULL UNIQUE,
    display_name    VARCHAR(100)  NOT NULL,
    coin_amount     BIGINT        NOT NULL CHECK (coin_amount > 0),
    price_paise     INT           NOT NULL CHECK (price_paise > 0),
    currency        VARCHAR(10)   NOT NULL DEFAULT 'INR',
    play_product_id VARCHAR(100),
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    display_order   INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

INSERT INTO lang_coin_packages
    (package_code, display_name, coin_amount, price_paise, play_product_id, display_order) VALUES
    ('COINS_250',  'Small Pouch',    250,  2900,  'langoa_coins_250',  1),
    ('COINS_1100', 'Coin Bag',      1100,  9900,  'langoa_coins_1100', 2),
    ('COINS_6500', 'Treasure Chest', 6500, 49900, 'langoa_coins_6500', 3);

CREATE INDEX idx_coin_packages_active ON lang_coin_packages (is_active) WHERE is_active = TRUE;

-- Extend transaction_type CHECK to include COIN_PURCHASE
ALTER TABLE langoa_transactions
    DROP CONSTRAINT IF EXISTS langoa_transactions_transaction_type_check;
ALTER TABLE langoa_transactions
    ADD CONSTRAINT langoa_transactions_transaction_type_check
        CHECK (transaction_type IN (
            'LESSON_COMPLETION','BUILDING_PURCHASE','BUILDING_UPGRADE',
            'DAILY_REWARD','INITIAL_GRANT','REFUND',
            'DECORATION_PURCHASE','QUEST_REWARD','ACHIEVEMENT_REWARD',
            'EXPANSION_PURCHASE','COIN_PURCHASE'
        ));
