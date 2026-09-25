-- ============================================================
-- Phase 1A: Async job table + German language seed data
-- ============================================================

-- ─── Async Pipeline Jobs ──────────────────────────────────────────────────
-- Tracks asynchronous execution of Content Factory pipeline operations.
-- Separate from ContentStatus (which tracks content lifecycle).
-- Separate from CfAgentRun (which tracks individual agent executions).

CREATE TABLE cf_pipeline_jobs (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id        UUID         NOT NULL REFERENCES cf_lessons(id),
    lesson_version   INT          NOT NULL,
    job_type         VARCHAR(50)  NOT NULL,   -- 'CONTENT_GENERATION'
    status           VARCHAR(20)  NOT NULL DEFAULT 'QUEUED'
                     CHECK (status IN ('QUEUED','RUNNING','SUCCEEDED','FAILED','RETRYING','CANCELLED')),
    attempt          INT          NOT NULL DEFAULT 0,
    max_attempts     INT          NOT NULL DEFAULT 3,
    payload          JSONB        NOT NULL DEFAULT '{}',
    result_reference VARCHAR(200),            -- e.g. agent_run_id that produced the result
    error            TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ
);

CREATE INDEX idx_cf_pipeline_jobs_lesson  ON cf_pipeline_jobs (lesson_id, status);
CREATE INDEX idx_cf_pipeline_jobs_status  ON cf_pipeline_jobs (status) WHERE status IN ('QUEUED','RUNNING','RETRYING');

-- ─── German Language Profile ──────────────────────────────────────────────

INSERT INTO cf_language_profiles
    (domain_id, language_code, display_name, script, cefr_applicable, rtl, active,
     linguistic_qa_agent, char_validation_regex, prompt_ids, qa_thresholds)
SELECT
    d.id,
    'de',
    'German',
    'Latin',
    TRUE,
    FALSE,
    TRUE,
    'de_linguistic_qa',
    '^[a-zA-ZäöüÄÖÜß0-9 .,!?;:()\-''"\n\r\t]+$',
    '{"content_generator": "language.german.a1.content_generation"}',
    '{"cefr_qa": 0.75, "pedagogy_qa": 0.70, "linguistic_qa": 0.80}'
FROM cf_domains d
WHERE d.code = 'language';

-- ─── Update language domain plugin_config with active agents ─────────────

UPDATE cf_domains
SET plugin_config = '{"activeAgents": ["content_generator", "cefr_qa", "pedagogy_qa", "exercise_qa", "consistency_qa", "linguistic_qa"]}'
WHERE code = 'language';

-- ─── German A1 Content Generation Prompt ─────────────────────────────────

INSERT INTO cf_agent_prompts
    (prompt_key, agent_type, domain_code, language_code, version,
     system_prompt, prompt_text, status, created_by, change_notes)
VALUES (
    'language.german.a1.content_generation',
    'content_generator',
    'language',
    'de',
    1,
    'You are an expert German language curriculum designer creating A1-level lessons for absolute beginners.

Your lessons must:
- Use only CEFR A1 vocabulary (most common ~500 words)
- Use only present tense and the simplest sentence structures
- Be culturally sensitive and use natural, everyday German
- Include clear English translations for ALL German content
- Avoid linguistic jargon learners would not understand
- Be encouraging and accessible to complete beginners

Output ONLY valid JSON matching the exact schema provided. No markdown, no explanation outside the JSON.',
    'Create a complete German A1 lesson on the topic: {{topic}}

Lesson reference: {{stableRef}}
CEFR level: A1
Language: German

Output a JSON object with this EXACT structure:
{
  "metadata": {
    "topic": "string",
    "cefrLevel": "A1",
    "language": "de",
    "estimatedMinutes": number
  },
  "objectives": [
    "string (what the learner will be able to do after this lesson)"
  ],
  "explanation": {
    "intro": "string (1-2 welcoming sentences in English explaining the topic)",
    "culturalNote": "string (optional cultural context, 1 sentence)"
  },
  "vocabulary": [
    {
      "german": "string",
      "english": "string",
      "pronunciation": "string (simplified phonetic guide, e.g. HAH-lo)",
      "example": "string (simple A1 German sentence using this word)"
    }
  ],
  "grammar": {
    "title": "string (grammar point name)",
    "explanation": "string (simple English explanation, 2-3 sentences max)",
    "pattern": "string (e.g. Ich + verb)",
    "examples": [
      {"german": "string", "english": "string"}
    ]
  },
  "examples": [
    {"german": "string", "english": "string", "context": "string (when to use this)"}
  ],
  "exercises": [
    {
      "type": "MULTIPLE_CHOICE",
      "question": "string",
      "options": ["string", "string", "string", "string"],
      "correctAnswer": "string",
      "explanation": "string"
    },
    {
      "type": "FILL_IN_BLANK",
      "question": "string (use ___ for the blank)",
      "correctAnswer": "string",
      "hint": "string"
    },
    {
      "type": "TRANSLATION",
      "source": "string (English sentence)",
      "correctAnswer": "string (German translation)",
      "hint": "string"
    }
  ]
}

Requirements:
- objectives: 3-4 items
- vocabulary: 8-12 words/phrases most useful for this topic
- grammar.examples: 3-4 examples
- examples: 4-6 natural conversational examples
- exercises: exactly 3 exercises (one of each type listed)
- All German must be correct and natural
- Keep everything at strict A1 level',
    'ACTIVE',
    'system',
    'Phase 1A initial German A1 content generation prompt'
);
