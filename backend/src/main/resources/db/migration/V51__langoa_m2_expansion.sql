-- M2: Civilization Expansion — WOOD currency, upgrade state, decorations,
--     city expansions, quests, achievements

-- ── 1. Currency type: add WOOD ────────────────────────────────────────────────
ALTER TABLE langoa_currency_balances
    DROP CONSTRAINT IF EXISTS langoa_currency_balances_currency_type_check;
ALTER TABLE langoa_currency_balances
    ADD CONSTRAINT langoa_currency_balances_currency_type_check
        CHECK (currency_type IN ('COINS','GEMS','XP','FOOD','MATERIALS','WOOD','CIVILIZATION_POWER'));

ALTER TABLE langoa_transactions
    DROP CONSTRAINT IF EXISTS langoa_transactions_currency_type_check;
-- No explicit CHECK on currency_type in transactions — currency_type is VARCHAR, unconstrained there intentionally.

-- ── 2. Expand transaction types ───────────────────────────────────────────────
ALTER TABLE langoa_transactions
    DROP CONSTRAINT IF EXISTS langoa_transactions_transaction_type_check;
ALTER TABLE langoa_transactions
    ADD CONSTRAINT langoa_transactions_transaction_type_check
        CHECK (transaction_type IN (
            'LESSON_COMPLETION','BUILDING_PURCHASE','BUILDING_UPGRADE',
            'DAILY_REWARD','INITIAL_GRANT','REFUND',
            'DECORATION_PURCHASE','QUEST_REWARD','ACHIEVEMENT_REWARD','EXPANSION_PURCHASE'
        ));

-- ── 3. Extend building instances with state, rotation, grid size ──────────────
ALTER TABLE langoa_building_instances
    ADD COLUMN IF NOT EXISTS build_state    VARCHAR(20) NOT NULL DEFAULT 'BUILT'
        CHECK (build_state IN ('CONSTRUCTING','UPGRADING','BUILT')),
    ADD COLUMN IF NOT EXISTS rotation_deg  INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS width_tiles   INT         NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS height_tiles  INT         NOT NULL DEFAULT 1;

-- ── 4. Decoration definitions ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS langoa_decoration_definitions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    decoration_type VARCHAR(50) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    description   TEXT,
    asset_ref     VARCHAR(200),
    coin_cost     BIGINT       NOT NULL DEFAULT 0,
    wood_cost     BIGINT       NOT NULL DEFAULT 0,
    required_civ_tier VARCHAR(20) NOT NULL DEFAULT 'VILLAGE',
    is_premium    BOOLEAN      NOT NULL DEFAULT FALSE,
    width_tiles   INT          NOT NULL DEFAULT 1,
    height_tiles  INT          NOT NULL DEFAULT 1,
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── 5. Decoration instances ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS langoa_decoration_instances (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    civilization_id UUID        NOT NULL REFERENCES langoa_civilizations(id) ON DELETE CASCADE,
    decoration_type VARCHAR(50) NOT NULL,
    position_x      INT         NOT NULL DEFAULT 0,
    position_y      INT         NOT NULL DEFAULT 0,
    rotation_deg    INT         NOT NULL DEFAULT 0,
    placed_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── 6. City expansions ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS langoa_city_expansion_definitions (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    expansion_slot      INT         NOT NULL UNIQUE,
    display_name        VARCHAR(100) NOT NULL,
    description         TEXT,
    grid_x_offset       INT         NOT NULL DEFAULT 0,
    grid_y_offset       INT         NOT NULL DEFAULT 0,
    grid_width          INT         NOT NULL DEFAULT 10,
    grid_height         INT         NOT NULL DEFAULT 10,
    coin_cost           BIGINT      NOT NULL DEFAULT 0,
    wood_cost           BIGINT      NOT NULL DEFAULT 0,
    required_lessons    INT         NOT NULL DEFAULT 0,
    required_xp         BIGINT      NOT NULL DEFAULT 0,
    required_civ_tier   VARCHAR(20) NOT NULL DEFAULT 'VILLAGE',
    display_order       INT         NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS langoa_city_expansion_instances (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    civilization_id UUID        NOT NULL REFERENCES langoa_civilizations(id) ON DELETE CASCADE,
    expansion_slot  INT         NOT NULL,
    unlocked_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (civilization_id, expansion_slot)
);

-- ── 7. Quest definitions ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS langoa_quest_definitions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    quest_key       VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(200) NOT NULL,
    description     TEXT,
    quest_type      VARCHAR(30)  NOT NULL DEFAULT 'DAILY'
        CHECK (quest_type IN ('DAILY','WEEKLY','ONE_TIME')),
    target_type     VARCHAR(50)  NOT NULL,   -- e.g. LESSONS_COMPLETED, LISTENING_COMPLETED
    target_count    INT          NOT NULL DEFAULT 1,
    xp_reward       BIGINT       NOT NULL DEFAULT 0,
    coin_reward     BIGINT       NOT NULL DEFAULT 0,
    food_reward     BIGINT       NOT NULL DEFAULT 0,
    material_reward BIGINT       NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS langoa_quest_progress (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code   VARCHAR(10) NOT NULL,
    quest_id        UUID        NOT NULL REFERENCES langoa_quest_definitions(id) ON DELETE CASCADE,
    current_count   INT         NOT NULL DEFAULT 0,
    completed       BOOLEAN     NOT NULL DEFAULT FALSE,
    reward_claimed  BOOLEAN     NOT NULL DEFAULT FALSE,
    quest_date      DATE        NOT NULL DEFAULT CURRENT_DATE,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, language_code, quest_id, quest_date)
);

-- ── 8. Achievement definitions ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS langoa_achievement_definitions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    achievement_key VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(200) NOT NULL,
    description     TEXT,
    icon            VARCHAR(10),
    trigger_type    VARCHAR(50)  NOT NULL,
    trigger_value   INT         NOT NULL DEFAULT 1,
    xp_reward       BIGINT      NOT NULL DEFAULT 0,
    coin_reward     BIGINT      NOT NULL DEFAULT 0,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS langoa_achievement_progress (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code   VARCHAR(10) NOT NULL,
    achievement_id  UUID        NOT NULL REFERENCES langoa_achievement_definitions(id) ON DELETE CASCADE,
    unlocked        BOOLEAN     NOT NULL DEFAULT FALSE,
    unlocked_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, language_code, achievement_id)
);

-- ── 9. Indexes ────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_langoa_di_civ ON langoa_decoration_instances(civilization_id);
CREATE INDEX IF NOT EXISTS idx_langoa_cei_civ ON langoa_city_expansion_instances(civilization_id);
CREATE INDEX IF NOT EXISTS idx_langoa_qp_user ON langoa_quest_progress(user_id, language_code, quest_date);
CREATE INDEX IF NOT EXISTS idx_langoa_ap_user ON langoa_achievement_progress(user_id, language_code);

-- ── 10. Seed: building definitions — M2 additions ─────────────────────────────
INSERT INTO langoa_building_definitions (building_type, display_name, description, max_level, display_order)
    VALUES
        ('SCHOOL',    'Village School',    'Advanced learning facility',  5,  4),
        ('MARKET',    'Town Market',        'Trade hub for resources',     5,  5),
        ('WORKSHOP',  'Craftsman Workshop', 'Produces materials and wood', 5,  6),
        ('PARK',      'City Park',          'Boosts civilization morale',  3,  7),
        ('LIBRARY',   'Grand Library',      'Accelerates XP gain',        5,  8)
    ON CONFLICT (building_type) DO UPDATE SET
        description  = EXCLUDED.description,
        display_order = EXCLUDED.display_order;

-- ── 11. Seed: building level configs — M2 buildings and higher levels ─────────
-- HOUSE levels 4-5
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('HOUSE', 4, 'Townhouse',   1200, 0, 120, 60, 0, 30, 5000),
    ('HOUSE', 5, 'Manor House', 2500, 0, 250, 120, 0, 60, 12000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- FARM levels 4-5
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('FARM', 4, 'Large Farm',   1500, 0, 150, 80, 0, 40, 6000),
    ('FARM', 5, 'Estate Farm',  3000, 0, 300, 150, 0, 80, 15000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- LEARNING_CENTER levels 4-5
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('LEARNING_CENTER', 4, 'Academy',     2000, 0, 200, 100, 0, 50, 8000),
    ('LEARNING_CENTER', 5, 'University',  4000, 0, 400, 200, 0, 100, 20000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- SCHOOL (new in M2)
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('SCHOOL', 1, 'Village School',  200, 0, 20, 10, 0, 5, 1000),
    ('SCHOOL', 2, 'Town School',     500, 0, 50, 30, 0, 12, 2500),
    ('SCHOOL', 3, 'District School', 1100, 0, 110, 60, 0, 25, 6000),
    ('SCHOOL', 4, 'City School',     2200, 0, 220, 110, 0, 50, 12000),
    ('SCHOOL', 5, 'Grand Academy',   5000, 0, 500, 250, 0, 100, 25000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- MARKET (new in M2)
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('MARKET', 1, 'Village Market',  150, 0, 15, 0, 0, 10, 1500),
    ('MARKET', 2, 'Town Market',     400, 0, 40, 20, 0, 20, 4000),
    ('MARKET', 3, 'City Market',     900, 0, 90, 45, 0, 40, 8000),
    ('MARKET', 4, 'Grand Bazaar',    1800, 0, 180, 90, 0, 70, 15000),
    ('MARKET', 5, 'Imperial Market', 4000, 0, 400, 200, 0, 120, 30000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- WORKSHOP (new in M2)
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('WORKSHOP', 1, 'Lumber Yard',    120, 0, 12, 0, 0, 15, 2000),
    ('WORKSHOP', 2, 'Carpenter Shop', 320, 0, 32, 16, 0, 25, 5000),
    ('WORKSHOP', 3, 'Workshop',       720, 0, 72, 36, 0, 45, 10000),
    ('WORKSHOP', 4, 'Factory',        1500, 0, 150, 75, 0, 80, 20000),
    ('WORKSHOP', 5, 'Industrial Hub', 3500, 0, 350, 175, 0, 150, 40000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- PARK (new in M2)
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('PARK', 1, 'Garden',     80, 0, 0, 20, 0, 5, 500),
    ('PARK', 2, 'City Park',  200, 0, 10, 50, 0, 15, 2000),
    ('PARK', 3, 'Grand Park', 500, 0, 20, 100, 0, 30, 5000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- LIBRARY (new in M2)
INSERT INTO langoa_building_level_configs
    (building_type, level, display_name, coin_cost, food_cost, material_cost, wood_cost, xp_cost, required_lessons_completed, required_xp)
    VALUES
    ('LIBRARY', 1, 'Village Archive',  300, 0, 30, 20, 0, 8, 1000),
    ('LIBRARY', 2, 'Town Library',     700, 0, 70, 40, 0, 18, 3000),
    ('LIBRARY', 3, 'City Library',     1500, 0, 150, 80, 0, 35, 7000),
    ('LIBRARY', 4, 'Grand Library',    3000, 0, 300, 150, 0, 65, 15000),
    ('LIBRARY', 5, 'Royal Library',    6000, 0, 600, 300, 0, 120, 30000)
    ON CONFLICT (building_type, level) DO NOTHING;

-- ── 12. Add wood_cost column to building_level_configs if not exists ───────────
ALTER TABLE langoa_building_level_configs
    ADD COLUMN IF NOT EXISTS wood_cost BIGINT NOT NULL DEFAULT 0;

-- ── 13. Seed: decoration definitions ─────────────────────────────────────────
INSERT INTO langoa_decoration_definitions
    (decoration_type, display_name, description, coin_cost, wood_cost, required_civ_tier, display_order)
    VALUES
    ('TREE',          'Oak Tree',        'A majestic oak',              30, 10, 'VILLAGE', 1),
    ('FLOWER_GARDEN', 'Flower Garden',   'Colorful flowers',             50, 5,  'VILLAGE', 2),
    ('FOUNTAIN',      'Stone Fountain',  'A decorative fountain',       150, 0,  'TOWN',    3),
    ('BENCH',         'Park Bench',      'A wooden bench',               20, 15, 'VILLAGE', 4),
    ('STATUE',        'Hero Statue',     'Honors a great learner',      400, 0,  'CITY',    5),
    ('LANTERN',       'Stone Lantern',   'Illuminates the city',         60, 5,  'VILLAGE', 6),
    ('FLOWER_BED',    'Flower Bed',      'A bed of wildflowers',         25, 5,  'VILLAGE', 7)
    ON CONFLICT (decoration_type) DO NOTHING;

-- ── 14. Seed: city expansion definitions ──────────────────────────────────────
INSERT INTO langoa_city_expansion_definitions
    (expansion_slot, display_name, description, grid_x_offset, grid_y_offset, grid_width, grid_height,
     coin_cost, wood_cost, required_lessons, required_xp, required_civ_tier, display_order)
    VALUES
    (1, 'Eastern Quarter', 'Expand your city eastward',   10, 0,  10, 10, 1000, 200, 100,  10000, 'TOWN',    1),
    (2, 'Northern Fields', 'Expand into the northern hills', 0, 10, 10, 10, 2500, 500, 300,  30000, 'CITY',    2),
    (3, 'Western Shore',   'Reach the western river',    -10, 0,  10, 10, 5000, 1000, 600, 60000, 'KINGDOM', 3)
    ON CONFLICT (expansion_slot) DO NOTHING;

-- ── 15. Seed: quest definitions ───────────────────────────────────────────────
INSERT INTO langoa_quest_definitions
    (quest_key, display_name, description, quest_type, target_type, target_count,
     xp_reward, coin_reward, food_reward, material_reward)
    VALUES
    ('daily_lesson_1',    'First Step',       'Complete 1 lesson today',      'DAILY', 'LESSONS_COMPLETED', 1,  100, 50,  10, 5),
    ('daily_lesson_3',    'Dedicated Learner','Complete 3 lessons today',     'DAILY', 'LESSONS_COMPLETED', 3,  250, 120, 25, 15),
    ('daily_lesson_5',    'Scholar',          'Complete 5 lessons today',     'DAILY', 'LESSONS_COMPLETED', 5,  500, 250, 50, 30),
    ('weekly_lesson_10',  'Weekly Warrior',   'Complete 10 lessons this week','WEEKLY','LESSONS_COMPLETED', 10, 1000,500, 100, 60),
    ('one_time_build',    'First Builder',    'Construct your first building','ONE_TIME','BUILDINGS_BUILT',  1,  200, 100, 20, 10),
    ('one_time_upgrade',  'Improver',         'Upgrade any building',         'ONE_TIME','BUILDINGS_UPGRADED',1, 300, 150, 30, 15)
    ON CONFLICT (quest_key) DO NOTHING;

-- ── 16. Seed: achievement definitions ────────────────────────────────────────
INSERT INTO langoa_achievement_definitions
    (achievement_key, display_name, description, icon, trigger_type, trigger_value,
     xp_reward, coin_reward)
    VALUES
    ('first_lesson',    'First Step',        'Complete your first lesson',     '📖', 'LESSONS_COMPLETED', 1,   200,  100),
    ('lessons_10',      'Getting Started',   'Complete 10 lessons',            '⭐', 'LESSONS_COMPLETED', 10,  500,  250),
    ('lessons_50',      'Committed Learner', 'Complete 50 lessons',            '🌟', 'LESSONS_COMPLETED', 50,  1500, 750),
    ('lessons_100',     'Century Scholar',   'Complete 100 lessons',           '💫', 'LESSONS_COMPLETED', 100, 3000, 1500),
    ('first_building',  'First Builder',     'Construct your first building',  '🏗',  'BUILDINGS_BUILT',   1,   300,  150),
    ('buildings_5',     'Urbanist',          'Build 5 buildings',              '🏙',  'BUILDINGS_BUILT',   5,   800,  400),
    ('first_upgrade',   'Improver',          'Upgrade any building',           '⬆',  'BUILDINGS_UPGRADED',1,   400,  200),
    ('first_expansion', 'Expander',          'Unlock a city expansion',        '🗺',  'EXPANSIONS_UNLOCKED',1,  1000, 500)
    ON CONFLICT (achievement_key) DO NOTHING;

-- ── 17. Seed: reward definitions — M2 CEFR levels + WOOD ─────────────────────
-- Update existing rows to include wood_reward once column added; use additional table column
ALTER TABLE langoa_reward_definitions
    ADD COLUMN IF NOT EXISTS wood_reward BIGINT NOT NULL DEFAULT 0;

UPDATE langoa_reward_definitions SET wood_reward = 3  WHERE cefr_level = 'A1' AND difficulty_tier = 'EASY';
UPDATE langoa_reward_definitions SET wood_reward = 5  WHERE cefr_level = 'A1' AND difficulty_tier = 'STANDARD';
UPDATE langoa_reward_definitions SET wood_reward = 7  WHERE cefr_level = 'A1' AND difficulty_tier = 'HARD';
UPDATE langoa_reward_definitions SET wood_reward = 8  WHERE cefr_level = 'A2' AND difficulty_tier = 'STANDARD';
UPDATE langoa_reward_definitions SET wood_reward = 10 WHERE cefr_level = 'B1' AND difficulty_tier = 'STANDARD';
UPDATE langoa_reward_definitions SET wood_reward = 12 WHERE cefr_level = 'B2' AND difficulty_tier = 'STANDARD';
UPDATE langoa_reward_definitions SET wood_reward = 15 WHERE cefr_level = 'C1' AND difficulty_tier = 'STANDARD';
