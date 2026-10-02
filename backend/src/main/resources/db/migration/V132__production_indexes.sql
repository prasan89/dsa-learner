-- Production indexes for Langoa
-- Note: CONCURRENTLY is not used here since Flyway runs migrations in a transaction.
-- These indexes are safe to run; they may briefly lock during creation on a live DB.

-- Language-only filter on lessons (for "all lessons in language X" queries)
CREATE INDEX IF NOT EXISTS idx_cf_lessons_language
    ON cf_lessons (language_code);

-- lesson_id + status for aggregate completion queries
CREATE INDEX IF NOT EXISTS idx_llp_lesson_status
    ON learner_lesson_progress (lesson_id, status);

-- job_id on agent_runs (currently unindexed)
CREATE INDEX IF NOT EXISTS idx_cf_agent_runs_job_id
    ON cf_agent_runs (job_id)
    WHERE job_id IS NOT NULL;

-- Drop duplicate index created in V50 (identical to idx_langoa_txn_user from V48)
DROP INDEX IF EXISTS idx_langoa_txn_user;
