-- Langoa Civilization & Economy domain tables

-- ── langoa_civilizations ────────────────────────────────────────────────────
CREATE TABLE langoa_civilizations (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code    VARCHAR(10)   NOT NULL,
    name             VARCHAR(100)  NOT NULL DEFAULT 'My Civilization',
    civilization_tier VARCHAR(20)  NOT NULL DEFAULT 'VILLAGE'
        CHECK (civilization_tier IN ('VILLAGE','TOWN','CITY','KINGDOM','EMPIRE')),
    tier_level       INT           NOT NULL DEFAULT 1,
    total_lessons_completed INT   NOT NULL DEFAULT 0,
    total_xp         BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, language_code)
);

-- ── langoa_currency_balances ─────────────────────────────────────────────────
CREATE TABLE langoa_currency_balances (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(10)  NOT NULL,
    currency_type VARCHAR(20)  NOT NULL
        CHECK (currency_type IN ('COINS','GEMS','XP','FOOD','MATERIALS','CIVILIZATION_POWER')),
    balance       BIGINT       NOT NULL DEFAULT 0 CHECK (balance >= 0),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, language_code, currency_type)
);

-- ── langoa_transactions ──────────────────────────────────────────────────────
CREATE TABLE langoa_transactions (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code    VARCHAR(10)  NOT NULL,
    transaction_type VARCHAR(40)  NOT NULL
        CHECK (transaction_type IN ('LESSON_COMPLETION','BUILDING_PURCHASE','DAILY_REWARD','INITIAL_GRANT','REFUND')),
    currency_type    VARCHAR(20)  NOT NULL,
    amount           BIGINT       NOT NULL,
    balance_after    BIGINT       NOT NULL,
    source_reference VARCHAR(100),
    idempotency_key  VARCHAR(100) UNIQUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── langoa_building_definitions ──────────────────────────────────────────────
CREATE TABLE langoa_building_definitions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    building_type VARCHAR(50)  NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    description   TEXT,
    max_level     INT          NOT NULL DEFAULT 5,
    asset_ref     VARCHAR(200),
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── langoa_building_level_configs ────────────────────────────────────────────
CREATE TABLE langoa_building_level_configs (
    id                     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    building_type          VARCHAR(50)  NOT NULL REFERENCES langoa_building_definitions(building_type) ON DELETE CASCADE,
    level                  INT          NOT NULL CHECK (level > 0),
    display_name           VARCHAR(100),
    coin_cost              BIGINT       NOT NULL DEFAULT 0,
    food_cost              BIGINT       NOT NULL DEFAULT 0,
    material_cost          BIGINT       NOT NULL DEFAULT 0,
    xp_cost                BIGINT       NOT NULL DEFAULT 0,
    required_lessons_completed INT      NOT NULL DEFAULT 0,
    required_xp            BIGINT       NOT NULL DEFAULT 0,
    required_building_type VARCHAR(50),
    required_building_level INT,
    UNIQUE (building_type, level)
);

-- ── langoa_building_instances ────────────────────────────────────────────────
CREATE TABLE langoa_building_instances (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    civilization_id UUID        NOT NULL REFERENCES langoa_civilizations(id) ON DELETE CASCADE,
    building_type   VARCHAR(50) NOT NULL,
    current_level   INT         NOT NULL DEFAULT 1,
    position_x      INT         NOT NULL DEFAULT 0,
    position_y      INT         NOT NULL DEFAULT 0,
    built_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    upgraded_at     TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── langoa_reward_definitions ────────────────────────────────────────────────
CREATE TABLE langoa_reward_definitions (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cefr_level               VARCHAR(4)  NOT NULL,
    difficulty_tier          VARCHAR(20) NOT NULL DEFAULT 'STANDARD'
        CHECK (difficulty_tier IN ('EASY','STANDARD','HARD','CHALLENGE')),
    xp_reward                BIGINT      NOT NULL DEFAULT 100,
    coin_reward              BIGINT      NOT NULL DEFAULT 50,
    food_reward              BIGINT      NOT NULL DEFAULT 10,
    material_reward          BIGINT      NOT NULL DEFAULT 5,
    civilization_power_reward BIGINT     NOT NULL DEFAULT 100,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (cefr_level, difficulty_tier)
);

-- ── Indexes ───────────────────────────────────────────────────────────────────
CREATE INDEX idx_langoa_civ_user        ON langoa_civilizations(user_id);
CREATE INDEX idx_langoa_bal_user        ON langoa_currency_balances(user_id, language_code);
CREATE INDEX idx_langoa_txn_user        ON langoa_transactions(user_id, language_code, created_at DESC);
CREATE INDEX idx_langoa_bi_civ          ON langoa_building_instances(civilization_id);
CREATE INDEX idx_langoa_txn_idempotency ON langoa_transactions(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- ── Seed: building definitions ───────────────────────────────────────────────
INSERT INTO langoa_building_definitions (building_type, display_name, max_level, display_order) VALUES
    ('HOUSE',           'Settler House',       5, 1),
    ('FARM',            'Community Farm',      5, 2),
    ('LEARNING_CENTER', 'Learning Center',     5, 3),
    ('SCHOOL',          'Village School',      5, 4),
    ('MARKET',          'Town Market',         5, 5),
    ('WORKSHOP',        'Craftsman Workshop',  5, 6);

-- ── Seed: building level configs ─────────────────────────────────────────────
-- HOUSE
INSERT INTO langoa_building_level_configs
    (building_type, level, coin_cost, food_cost, material_cost, xp_cost, required_lessons_completed) VALUES
    ('HOUSE', 1, 0,   0, 0,  0, 0),
    ('HOUSE', 2, 200, 0, 20, 0, 5),
    ('HOUSE', 3, 500, 0, 50, 0, 15);

-- FARM
INSERT INTO langoa_building_level_configs
    (building_type, level, coin_cost, food_cost, material_cost, xp_cost, required_lessons_completed) VALUES
    ('FARM', 1, 100, 0, 10, 0, 1),
    ('FARM', 2, 300, 0, 30, 0, 8),
    ('FARM', 3, 700, 0, 70, 0, 20);

-- LEARNING_CENTER
INSERT INTO langoa_building_level_configs
    (building_type, level, coin_cost, food_cost, material_cost, xp_cost, required_lessons_completed) VALUES
    ('LEARNING_CENTER', 1, 150, 0, 15, 0, 2),
    ('LEARNING_CENTER', 2, 400, 0, 40, 0, 12),
    ('LEARNING_CENTER', 3, 900, 0, 90, 0, 25);

-- ── Seed: reward definitions ─────────────────────────────────────────────────
INSERT INTO langoa_reward_definitions
    (cefr_level, difficulty_tier, xp_reward, coin_reward, food_reward, material_reward, civilization_power_reward) VALUES
    ('A1', 'STANDARD', 100, 50,  10, 5,  100),
    ('A1', 'EASY',      70, 35,   7, 3,   70),
    ('A1', 'HARD',     140, 70,  14, 7,  140),
    ('A2', 'STANDARD', 150, 75,  15, 8,  150),
    ('B1', 'STANDARD', 200, 100, 20, 10, 200),
    ('B2', 'STANDARD', 250, 125, 25, 12, 250),
    ('C1', 'STANDARD', 300, 150, 30, 15, 300);
