-- M5.4: Resource Economy
-- Adds capacity to currency balances, building production configs, and lazy production tracking.
-- All text is English/Roman only.

-- 1. Add capacity to langoa_currency_balances for FOOD, MATERIALS, WOOD.
--    NULL = no cap (used for COINS, XP, GEMS, CIVILIZATION_POWER).
ALTER TABLE langoa_currency_balances
    ADD COLUMN IF NOT EXISTS capacity BIGINT DEFAULT NULL;

-- Set default capacity of 500 for all existing FOOD, MATERIALS, WOOD rows
UPDATE langoa_currency_balances
   SET capacity = 500
 WHERE currency_type IN ('FOOD', 'MATERIALS', 'WOOD');

-- 2. Production config table: one row per building_type + level + resource_type.
CREATE TABLE IF NOT EXISTS langoa_building_production_configs (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    building_type       VARCHAR(50)  NOT NULL,
    level               INT          NOT NULL CHECK (level >= 1),
    resource_type       VARCHAR(20)  NOT NULL,
    rate_per_hour       INT          NOT NULL CHECK (rate_per_hour >= 0),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (building_type, level, resource_type)
);

-- 3. Track when production was last collected per building instance.
ALTER TABLE langoa_building_instances
    ADD COLUMN IF NOT EXISTS last_production_at TIMESTAMPTZ DEFAULT NOW();

-- Seed: FARM produces FOOD
INSERT INTO langoa_building_production_configs
    (building_type, level, resource_type, rate_per_hour) VALUES
    ('FARM', 1, 'FOOD',  10),
    ('FARM', 2, 'FOOD',  20),
    ('FARM', 3, 'FOOD',  35),
    ('FARM', 4, 'FOOD',  55),
    ('FARM', 5, 'FOOD',  80)
ON CONFLICT (building_type, level, resource_type) DO NOTHING;

-- Seed: WORKSHOP produces MATERIALS and WOOD
INSERT INTO langoa_building_production_configs
    (building_type, level, resource_type, rate_per_hour) VALUES
    ('WORKSHOP', 1, 'MATERIALS',  8),
    ('WORKSHOP', 2, 'MATERIALS', 16),
    ('WORKSHOP', 3, 'MATERIALS', 28),
    ('WORKSHOP', 4, 'MATERIALS', 44),
    ('WORKSHOP', 5, 'MATERIALS', 65),
    ('WORKSHOP', 1, 'WOOD',       8),
    ('WORKSHOP', 2, 'WOOD',      16),
    ('WORKSHOP', 3, 'WOOD',      28),
    ('WORKSHOP', 4, 'WOOD',      44),
    ('WORKSHOP', 5, 'WOOD',      65)
ON CONFLICT (building_type, level, resource_type) DO NOTHING;
