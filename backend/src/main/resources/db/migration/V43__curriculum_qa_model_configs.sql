INSERT INTO cf_ai_model_configs (config_key, provider, model_id, timeout_ms, max_tokens, temperature, active)
VALUES
  ('curriculum_level_qa',     'anthropic', 'claude-haiku-4-5-20251001', 120000, 4096, 0.30, true),
  ('curriculum_coherence_qa', 'anthropic', 'claude-haiku-4-5-20251001', 120000, 4096, 0.30, true)
ON CONFLICT (config_key) DO UPDATE
  SET timeout_ms = EXCLUDED.timeout_ms, model_id = EXCLUDED.model_id, active = true;
