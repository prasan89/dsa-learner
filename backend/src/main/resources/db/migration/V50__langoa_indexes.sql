-- Additional indexes for Langoa hot paths under concurrent load

-- Fast lookup when determining if a civilization already owns a building type
CREATE INDEX idx_langoa_bi_civ_type ON langoa_building_instances(civilization_id, building_type);

-- Speed up idempotency duplicate detection in applyLessonReward (key already has a UNIQUE constraint
-- which implicitly creates an index, but only for non-null values — this is additive for the
-- partial index on non-null keys already in V48; adding a composite lookup index for user+language
-- transaction history used by audit/admin queries)
CREATE INDEX idx_langoa_txn_user_lang ON langoa_transactions(user_id, language_code, created_at DESC);
