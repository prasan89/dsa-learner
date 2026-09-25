-- Add cultural_flag to cf_agent_runs so the complete AgentOutput contract
-- can be persisted and reconstructed on idempotency cache hits.
-- cultural_flag is part of AgentOutput but was missing from the Phase 0 schema.

ALTER TABLE cf_agent_runs
    ADD COLUMN cultural_flag BOOLEAN NOT NULL DEFAULT FALSE;
