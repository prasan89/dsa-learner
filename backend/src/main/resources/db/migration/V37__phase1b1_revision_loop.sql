-- ============================================================
-- Phase 1B.1: Automated Revision Loop
--
-- Schema changes:
--   1. cf_lesson_versions: add parent_version (source version that was revised)
--   2. cf_pipeline_jobs: REVISION_GENERATION job type (no DDL — free-text field)
--   3. cf_agent_prompts: German A1 revision generation prompt
-- ============================================================

-- ─── Add parent_version to cf_lesson_versions ────────────────────────────
-- Populated on revision versions so lineage is traceable:
--   version N+1 was produced by revising version N.
-- NULL for originally-generated versions.

ALTER TABLE cf_lesson_versions
    ADD COLUMN IF NOT EXISTS parent_version INT;

-- ─── Revision Generation Prompt ──────────────────────────────────────────

INSERT INTO cf_agent_prompts
    (prompt_key, agent_type, domain_code, language_code, version,
     system_prompt, prompt_text, status, created_by, change_notes)
VALUES (
    'language.german.a1.revision_generation',
    'revision_generator',
    'language',
    'de',
    1,
    'You are an expert German language curriculum designer revising a German A1 lesson based on quality assurance feedback.

Your task is to produce a corrected version of the lesson that fixes the specific issues identified by QA reviewers.

Rules:
- Fix EVERY ERROR-severity issue listed in the QA feedback.
- Address WARNING-severity issues where possible without sacrificing lesson quality.
- Do NOT change content that was not flagged — preserve the lesson topic, structure, and vocabulary list.
- Do NOT add new vocabulary that was not in the original unless fixing a genuine error requires it.
- Maintain A1 level throughout — no B1+ grammar structures.
- All German text must be grammatically correct with proper articles (der/die/das) and verb conjugation.

Output ONLY valid JSON with the same structure as the original lesson. No markdown fences, no explanation outside the JSON.',
    'Revise the following German A1 lesson to fix the QA issues listed below.

## Original Lesson
{{lessonJson}}

## QA Issues to Fix
{{revisionFeedback}}

Fix all ERROR-severity issues and address WARNING-severity issues where possible.
Preserve the overall lesson structure, topic, and any content that was NOT flagged as problematic.

Output the revised lesson as a JSON object with this EXACT structure:
{
  "metadata": {
    "topic": "string",
    "cefrLevel": "A1",
    "language": "de",
    "estimatedMinutes": number
  },
  "objectives": ["string"],
  "explanation": {
    "intro": "string",
    "rules": ["string"],
    "notes": "string"
  },
  "vocabulary": [
    {
      "german": "string",
      "english": "string",
      "article": "string or null",
      "example": "string",
      "exampleTranslation": "string"
    }
  ],
  "grammar": {
    "title": "string",
    "explanation": "string",
    "conjugationTable": {},
    "examples": [
      { "german": "string", "english": "string" }
    ]
  },
  "examples": [
    { "german": "string", "english": "string" }
  ],
  "exercises": [
    {
      "type": "MULTIPLE_CHOICE|FILL_IN_BLANK|TRANSLATION",
      "question": "string",
      "options": ["string"] or null,
      "correctAnswer": "string",
      "hint": "string or null"
    }
  ]
}',
    'ACTIVE',
    'system',
    'Phase 1B.1 German A1 revision generation prompt v1'
);
