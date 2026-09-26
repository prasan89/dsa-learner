-- ============================================================
-- Phase 2A — Curriculum Factory Schema
-- Conventions: UUID PK, TIMESTAMPTZ, snake_case, cf_ prefix
-- All curriculum tables are new; no existing tables modified.
--
-- Hierarchy:
--   cf_curricula (one per language program, e.g. "German Complete")
--     └── cf_curriculum_levels  (one per CEFR band A1–C2)
--           └── cf_curriculum_units  (thematic groupings within a level)
--                 └── cf_curriculum_lesson_plans  (atomic lesson slots)
--                       └── cf_lessons  (existing, linked after provisioning)
--
-- Supporting:
--   cf_curriculum_dependencies   (DAG prerequisite edges)
--   cf_curriculum_grammar_index  (grammar concept progression)
--   cf_curriculum_vocabulary_index (vocabulary progression)
--   cf_curriculum_versions       (immutable blueprint snapshots)
--   cf_curriculum_workflow_events (append-only audit trail)
--
-- DOWN (manual rollback):
--   DROP TABLE IF EXISTS cf_curriculum_workflow_events CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_grammar_index CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_vocabulary_index CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_versions CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_dependencies CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_lesson_plans CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_units CASCADE;
--   DROP TABLE IF EXISTS cf_curriculum_levels CASCADE;
--   DROP TABLE IF EXISTS cf_curricula CASCADE;
-- ============================================================

-- ─── cf_curricula ─────────────────────────────────────────────────────────
-- One row per complete curriculum (e.g. "German A1–C2").
-- language_code references cf_language_profiles.language_code (not FK to avoid
-- cross-schema coupling; enforced by application).

CREATE TABLE cf_curricula (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    stable_ref          VARCHAR(80)  NOT NULL UNIQUE,   -- 'german-complete-v1'
    domain_code         VARCHAR(20)  NOT NULL,           -- 'language'
    language_code       VARCHAR(10)  NOT NULL,           -- 'de', 'fr', 'ko'
    display_name        VARCHAR(200) NOT NULL,
    description         TEXT,
    curriculum_status   VARCHAR(30)  NOT NULL DEFAULT 'DRAFT'
        CHECK (curriculum_status IN (
            'DRAFT',
            'BLUEPRINT_PENDING',
            'BLUEPRINT_GENERATED',
            'BLUEPRINT_VALIDATED',
            'GENERATION_IN_PROGRESS',
            'CURRICULUM_QA_PENDING',
            'CURRICULUM_QA_FAILED',
            'CURRICULUM_QA_PASSED',
            'APPROVED',
            'ARCHIVED'
        )),
    curriculum_version  INT          NOT NULL DEFAULT 1,
    active_version      INT,
    publish_gate_passed BOOLEAN      NOT NULL DEFAULT FALSE,
    batch_size          INT          NOT NULL DEFAULT 10
        CHECK (batch_size IN (10, 20, 50)),
    created_by          VARCHAR(100),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_curricula_domain_lang ON cf_curricula (domain_code, language_code);
CREATE INDEX idx_cf_curricula_status      ON cf_curricula (curriculum_status);

-- ─── cf_curriculum_levels ─────────────────────────────────────────────────
-- One row per CEFR band within a curriculum.
-- ordinal drives level-ordering gate (A1=1 must complete before A2=2 starts).

CREATE TABLE cf_curriculum_levels (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id       UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    cefr_level          VARCHAR(4)   NOT NULL,           -- 'A1','A2','B1','B2','C1','C2'
    display_name        VARCHAR(100) NOT NULL,
    ordinal             INT          NOT NULL,           -- 1=A1 … 6=C2
    target_lesson_count INT,                             -- NULL until set from blueprint output
    level_status        VARCHAR(30)  NOT NULL DEFAULT 'PLANNED'
        CHECK (level_status IN (
            'PLANNED',
            'GENERATION_IN_PROGRESS',
            'LEVEL_QA_PENDING',
            'LEVEL_QA_FAILED',
            'LEVEL_QA_PASSED',
            'APPROVED',
            'ARCHIVED'
        )),
    blueprint_agent_run_id  UUID,                        -- cf_agent_runs.id reference
    level_qa_run_id         UUID,
    human_review_flag       BOOLEAN      NOT NULL DEFAULT FALSE,
    human_review_reason     TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (curriculum_id, cefr_level),
    UNIQUE (curriculum_id, ordinal)
);

CREATE INDEX idx_cf_cl_curriculum ON cf_curriculum_levels (curriculum_id, ordinal);
CREATE INDEX idx_cf_cl_status     ON cf_curriculum_levels (curriculum_id, level_status);

-- ─── cf_curriculum_units ──────────────────────────────────────────────────
-- Thematic groupings within a level. curriculum_id is denormalised for
-- convenient filtering without always joining through level.

CREATE TABLE cf_curriculum_units (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    level_id      UUID         NOT NULL REFERENCES cf_curriculum_levels(id) ON DELETE CASCADE,
    curriculum_id UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    ordinal       INT          NOT NULL,                 -- unit number within the level
    label         VARCHAR(100) NOT NULL,                 -- 'Unit 2 – Daily Routines'
    theme         TEXT,
    learning_goal TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (level_id, ordinal)
);

CREATE INDEX idx_cf_cu_level      ON cf_curriculum_units (level_id);
CREATE INDEX idx_cf_cu_curriculum ON cf_curriculum_units (curriculum_id);

-- ─── cf_curriculum_lesson_plans ───────────────────────────────────────────
-- Atomic lesson slots. lesson_id is NULL until CurriculumService.provisionLesson()
-- creates the cf_lessons row and links it.
-- grammar_targets / vocab_targets are convenience denormalisations from the index
-- tables; the index tables remain the authoritative source of truth.

CREATE TABLE cf_curriculum_lesson_plans (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id       UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    level_id            UUID         NOT NULL REFERENCES cf_curriculum_levels(id) ON DELETE CASCADE,
    unit_id             UUID         REFERENCES cf_curriculum_units(id),
    stable_ref          VARCHAR(100) NOT NULL UNIQUE,   -- 'de-a1-u02-l03'
    position            INT          NOT NULL,           -- 1-based within the level
    title               VARCHAR(255) NOT NULL,
    topic               TEXT         NOT NULL,
    lesson_type         VARCHAR(40)  NOT NULL DEFAULT 'LEARN',
    difficulty          VARCHAR(30)  NOT NULL DEFAULT 'FOUNDATION',
    skill_focus         TEXT[],
    learning_objectives TEXT,
    communication_goals TEXT,
    grammar_targets     TEXT[],                          -- derived from blueprint grammarHints
    vocab_targets       TEXT[],                          -- derived from blueprint vocabHints
    plan_status         VARCHAR(30)  NOT NULL DEFAULT 'PLANNED'
        CHECK (plan_status IN (
            'PLANNED',
            'BLOCKED',
            'QUEUED',
            'GENERATING',
            'GENERATED',
            'QA_PENDING',
            'QA_FAILED',
            'REVISION',
            'QA_PASSED',
            'APPROVED',
            'PUBLISHED',
            'SKIPPED',
            'FAILED',
            'HUMAN_REVIEW'
        )),
    lesson_id           UUID,                            -- cf_lessons.id once provisioned
    generation_attempt  INT          NOT NULL DEFAULT 0,
    last_error          TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (level_id, position)
);

CREATE INDEX idx_cf_clp_level      ON cf_curriculum_lesson_plans (level_id, plan_status);
CREATE INDEX idx_cf_clp_curriculum ON cf_curriculum_lesson_plans (curriculum_id, plan_status);
CREATE INDEX idx_cf_clp_lesson_id  ON cf_curriculum_lesson_plans (lesson_id)
    WHERE lesson_id IS NOT NULL;
CREATE INDEX idx_cf_clp_stable_ref ON cf_curriculum_lesson_plans (stable_ref);

-- ─── cf_curriculum_dependencies ───────────────────────────────────────────
-- Directed prerequisite edges: lesson_plan_id depends on required_plan_id.
-- PREREQUISITE = hard block; SOFT_DEPENDENCY = advisory only.
-- Cycles are validated by CurriculumDependencyService before insertion.

CREATE TABLE cf_curriculum_dependencies (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id    UUID        NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    lesson_plan_id   UUID        NOT NULL REFERENCES cf_curriculum_lesson_plans(id) ON DELETE CASCADE,
    required_plan_id UUID        NOT NULL REFERENCES cf_curriculum_lesson_plans(id) ON DELETE CASCADE,
    dependency_type  VARCHAR(30) NOT NULL DEFAULT 'PREREQUISITE'
        CHECK (dependency_type IN ('PREREQUISITE', 'SOFT_DEPENDENCY')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_plan_id, required_plan_id)
);

CREATE INDEX idx_cf_cd_plan       ON cf_curriculum_dependencies (lesson_plan_id);
CREATE INDEX idx_cf_cd_required   ON cf_curriculum_dependencies (required_plan_id);
CREATE INDEX idx_cf_cd_curriculum ON cf_curriculum_dependencies (curriculum_id);

-- ─── cf_curriculum_grammar_index ──────────────────────────────────────────
-- Grammar concept progression: when first introduced, prerequisites, which plans
-- reinforce or depend on it. The deterministic level-QA check uses this table
-- to detect concepts used before introduction.

CREATE TABLE cf_curriculum_grammar_index (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id             UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    concept_key               VARCHAR(100) NOT NULL,    -- 'nominative_case', 'modal_verbs_darf'
    display_name              VARCHAR(200) NOT NULL,
    cefr_level                VARCHAR(4)   NOT NULL,
    introduction_plan_id      UUID         REFERENCES cf_curriculum_lesson_plans(id),
    prerequisite_concept_keys TEXT[],                   -- other concept_keys required first
    reinforcement_plan_ids    UUID[],                   -- plans that revisit this concept
    dependent_plan_ids        UUID[],                   -- plans that USE this concept
    notes                     TEXT,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (curriculum_id, concept_key)
);

CREATE INDEX idx_cf_cgi_curriculum ON cf_curriculum_grammar_index (curriculum_id, cefr_level);

-- ─── cf_curriculum_vocabulary_index ───────────────────────────────────────
-- Vocabulary term progression: first introduction, recycling lessons, CEFR level,
-- related terms. The deterministic level-QA check detects vocab used before introduction.

CREATE TABLE cf_curriculum_vocabulary_index (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id        UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    term                 VARCHAR(100) NOT NULL,
    language_code        VARCHAR(10)  NOT NULL,
    cefr_level           VARCHAR(4)   NOT NULL,
    introduction_plan_id UUID         REFERENCES cf_curriculum_lesson_plans(id),
    review_plan_ids      UUID[],                        -- plans that recycle this term
    related_terms        TEXT[],
    dependent_plan_ids   UUID[],                        -- plans assuming this term is known
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (curriculum_id, language_code, term)
);

CREATE INDEX idx_cf_cvi_curriculum ON cf_curriculum_vocabulary_index (curriculum_id, cefr_level);

-- ─── cf_curriculum_versions ───────────────────────────────────────────────
-- Immutable blueprint snapshots. Follows the same frozen-snapshot pattern as
-- cf_lesson_versions. blueprint_snapshot contains the full ordered JSON of all
-- levels, units, and lesson plan slots at snapshot time.

CREATE TABLE cf_curriculum_versions (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id      UUID        NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    version            INT         NOT NULL,
    blueprint_snapshot JSONB       NOT NULL DEFAULT '{}',
    change_summary     TEXT,
    frozen             BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (curriculum_id, version)
);

CREATE INDEX idx_cf_cv_curriculum ON cf_curriculum_versions (curriculum_id, version DESC);

-- ─── cf_curriculum_workflow_events ────────────────────────────────────────
-- Append-only audit trail for both curriculum-level and level-level transitions.
-- level_id IS NULL for curriculum-scoped events.
-- Mirrors cf_workflow_events (BIGSERIAL PK for append-only semantics).

CREATE TABLE cf_curriculum_workflow_events (
    id            BIGSERIAL    PRIMARY KEY,
    curriculum_id UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    level_id      UUID         REFERENCES cf_curriculum_levels(id),
    from_status   VARCHAR(30),
    to_status     VARCHAR(30)  NOT NULL,
    trigger       VARCHAR(60)  NOT NULL,
    actor         VARCHAR(100),
    agent_run_id  UUID,
    metadata      JSONB,
    occurred_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_cwe_curriculum ON cf_curriculum_workflow_events (curriculum_id, occurred_at DESC);
CREATE INDEX idx_cf_cwe_level      ON cf_curriculum_workflow_events (level_id, occurred_at DESC)
    WHERE level_id IS NOT NULL;

-- ─── Seed data ────────────────────────────────────────────────────────────
-- German Complete A1–C2 curriculum + 6 level rows.
-- curriculum_id is fixed so it can be referenced in application.yml or tests.

INSERT INTO cf_curricula (
    id, stable_ref, domain_code, language_code, display_name, description,
    curriculum_status, batch_size
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'german-complete-v1',
    'language',
    'de',
    'German Complete (A1–C2)',
    'Complete German language curriculum from beginner A1 to mastery C2',
    'DRAFT',
    10
) ON CONFLICT (stable_ref) DO NOTHING;

INSERT INTO cf_curriculum_levels (
    id, curriculum_id, cefr_level, display_name, ordinal, level_status
) VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'A1', 'German A1 – Beginner',          1, 'PLANNED'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'A2', 'German A2 – Elementary',         2, 'PLANNED'),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'B1', 'German B1 – Intermediate',       3, 'PLANNED'),
    ('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', 'B2', 'German B2 – Upper Intermediate', 4, 'PLANNED'),
    ('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', 'C1', 'German C1 – Advanced',           5, 'PLANNED'),
    ('10000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', 'C2', 'German C2 – Mastery',            6, 'PLANNED')
ON CONFLICT (curriculum_id, cefr_level) DO NOTHING;

-- ─── cf_curriculum_pipeline_jobs ─────────────────────────────────────────
-- Separate job table for curriculum-level work (blueprint generation, QA).
-- Kept separate from cf_pipeline_jobs because curriculum jobs are not
-- lesson-scoped and the existing table has lesson_id NOT NULL.

CREATE TABLE cf_curriculum_pipeline_jobs (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_id    UUID         NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    level_id         UUID         REFERENCES cf_curriculum_levels(id) ON DELETE CASCADE,
    job_type         VARCHAR(50)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'QUEUED'
                     CHECK (status IN ('QUEUED','RUNNING','SUCCEEDED','FAILED','RETRYING','CANCELLED')),
    attempt          INT          NOT NULL DEFAULT 0,
    max_attempts     INT          NOT NULL DEFAULT 3,
    payload          JSONB        NOT NULL DEFAULT '{}',
    result_reference VARCHAR(200),
    error            TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ
);

CREATE INDEX idx_cf_cpj_curriculum ON cf_curriculum_pipeline_jobs (curriculum_id, status);
CREATE INDEX idx_cf_cpj_status     ON cf_curriculum_pipeline_jobs (status)
    WHERE status IN ('QUEUED','RUNNING','RETRYING');
