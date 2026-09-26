-- ============================================================
-- Phase 2A — Fix prompt_key to match AgentType constants
-- The V40 migration inserted prompt_key values with dots (e.g.
-- 'curriculum.blueprint_generator'), but AgentType constants use
-- underscores (e.g. 'curriculum_blueprint_generator').
-- PromptRegistry.resolve() looks up by prompt_key, so they must match.
-- ============================================================

UPDATE cf_agent_prompts
SET prompt_key = 'curriculum_blueprint_generator'
WHERE prompt_key = 'curriculum.blueprint_generator'
  AND agent_type = 'curriculum_blueprint_generator';

UPDATE cf_agent_prompts
SET prompt_key = 'curriculum_blueprint_validator'
WHERE prompt_key = 'curriculum.blueprint_validator'
  AND agent_type = 'curriculum_blueprint_validator';

UPDATE cf_agent_prompts
SET prompt_key = 'curriculum_level_qa'
WHERE prompt_key = 'curriculum.level_qa'
  AND agent_type = 'curriculum_level_qa';

UPDATE cf_agent_prompts
SET prompt_key = 'curriculum_coherence_qa'
WHERE prompt_key = 'curriculum.coherence_qa'
  AND agent_type = 'curriculum_coherence_qa';
