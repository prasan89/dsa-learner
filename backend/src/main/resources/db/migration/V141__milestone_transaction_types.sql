-- M5.2: extend transaction_type CHECK to include milestone and exercise reward types
ALTER TABLE langoa_transactions
    DROP CONSTRAINT IF EXISTS langoa_transactions_transaction_type_check;
ALTER TABLE langoa_transactions
    ADD CONSTRAINT langoa_transactions_transaction_type_check
        CHECK (transaction_type IN (
            'LESSON_COMPLETION','BUILDING_PURCHASE','BUILDING_UPGRADE',
            'DAILY_REWARD','INITIAL_GRANT','REFUND',
            'DECORATION_PURCHASE','QUEST_REWARD','ACHIEVEMENT_REWARD',
            'EXPANSION_PURCHASE','COIN_PURCHASE',
            'LESSON_PERFECT','UNIT_COMPLETED','LEVEL_COMPLETED','EXERCISE_REWARD'
        ));

-- Milestone reward amounts (coins only — these are bonus grants on top of lesson rewards)
-- Easy to tune: UPDATE langoa_milestone_rewards SET coin_bonus = X WHERE milestone_type = 'Y'
CREATE TABLE IF NOT EXISTS langoa_milestone_rewards (
    milestone_type   VARCHAR(40) PRIMARY KEY,
    coin_bonus       BIGINT      NOT NULL CHECK (coin_bonus >= 0),
    description      VARCHAR(200)
);

INSERT INTO langoa_milestone_rewards (milestone_type, coin_bonus, description) VALUES
    ('LESSON_PERFECT',   20,   'Perfect score on a lesson — all exercises correct'),
    ('UNIT_COMPLETED',  200,   'All lessons in a unit completed'),
    ('LEVEL_COMPLETED', 500,   'All units in a CEFR level completed')
ON CONFLICT (milestone_type) DO NOTHING;
