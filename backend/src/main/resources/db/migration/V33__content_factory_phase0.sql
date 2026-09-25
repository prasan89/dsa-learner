-- ============================================================
-- Content Factory Phase 0
-- Conventions: UUID PK, TIMESTAMPTZ, snake_case, cf_ prefix
-- to avoid collision with existing DSA tables.
--
-- DOWN (manual rollback if needed):
--   DROP TABLE IF EXISTS cf_cost_ledger CASCADE;
--   DROP TABLE IF EXISTS cf_workflow_events CASCADE;
--   DROP TABLE IF EXISTS cf_agent_runs CASCADE;
--   DROP TABLE IF EXISTS cf_lesson_versions CASCADE;
--   DROP TABLE IF EXISTS cf_lessons CASCADE;
--   DROP TABLE IF EXISTS cf_ai_model_configs CASCADE;
--   DROP TABLE IF EXISTS cf_agent_prompts CASCADE;
--   DROP TABLE IF EXISTS cf_language_profiles CASCADE;
--   DROP TABLE IF EXISTS cf_domains CASCADE;
-- ============================================================

-- ─── Domains ──────────────────────────────────────────────────────────────

CREATE TABLE cf_domains (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(20)  NOT NULL UNIQUE,   -- 'language', 'dsa', 'ai_skills'
    display_name VARCHAR(100) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    plugin_config JSONB       NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Language Profiles ────────────────────────────────────────────────────

CREATE TABLE cf_language_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain_id             UUID         NOT NULL REFERENCES cf_domains(id),
    language_code         VARCHAR(10)  NOT NULL UNIQUE,  -- 'de', 'fr', 'ko'
    display_name          VARCHAR(100) NOT NULL,
    script                VARCHAR(30),
    cefr_applicable       BOOLEAN      NOT NULL DEFAULT TRUE,
    rtl                   BOOLEAN      NOT NULL DEFAULT FALSE,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    linguistic_qa_agent   VARCHAR(60),
    char_validation_regex VARCHAR(500),
    style_guide_ref       VARCHAR(200),
    prompt_ids            JSONB        NOT NULL DEFAULT '{}',
    qa_thresholds         JSONB        NOT NULL DEFAULT '{}',
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Prompt Registry ──────────────────────────────────────────────────────

CREATE TABLE cf_agent_prompts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_key    VARCHAR(100) NOT NULL,   -- e.g. 'de_linguistic_qa', 'cefr_qa'
    agent_type    VARCHAR(60)  NOT NULL,
    domain_code   VARCHAR(20)  NOT NULL,
    language_code VARCHAR(10),            -- NULL = domain-wide
    version       INT          NOT NULL,
    prompt_text   TEXT         NOT NULL,
    system_prompt TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                               CHECK (status IN ('DRAFT','ACTIVE','DEPRECATED','ARCHIVED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    activated_at  TIMESTAMPTZ,
    deprecated_at TIMESTAMPTZ,
    created_by    VARCHAR(100),
    change_notes  TEXT,
    UNIQUE (prompt_key, version)
);

CREATE INDEX idx_cf_prompts_active ON cf_agent_prompts (prompt_key, version DESC)
    WHERE status = 'ACTIVE';

-- ─── Model Configs ────────────────────────────────────────────────────────

CREATE TABLE cf_ai_model_configs (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key              VARCHAR(100) NOT NULL UNIQUE,
    provider                VARCHAR(50)  NOT NULL,   -- 'anthropic', 'openai'
    model_id                VARCHAR(100) NOT NULL,
    temperature             NUMERIC(3,2) NOT NULL DEFAULT 0.30,
    max_tokens              INT          NOT NULL DEFAULT 4096,
    timeout_ms              INT          NOT NULL DEFAULT 30000,
    cost_per_1k_input_usd   NUMERIC(8,6) NOT NULL DEFAULT 0.000000,
    cost_per_1k_output_usd  NUMERIC(8,6) NOT NULL DEFAULT 0.000000,
    active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Lessons ──────────────────────────────────────────────────────────────

CREATE TABLE cf_lessons (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stable_ref            VARCHAR(50)  NOT NULL UNIQUE, -- 'de-a1-u01-l01' permanent
    title                 VARCHAR(255),
    domain_code           VARCHAR(20)  NOT NULL,
    language_code         VARCHAR(10),
    cefr_level            VARCHAR(2),
    skill_focus           TEXT[],
    content_status        VARCHAR(30)  NOT NULL DEFAULT 'DRAFT'
                          CHECK (content_status IN (
                              'DRAFT','PLANNED','GENERATING','GENERATED',
                              'VALIDATION_PENDING','VALIDATION_FAILED',
                              'QA_PENDING','QA_FAILED','REVISION',
                              'QA_PASSED','HUMAN_REVIEW_REQUIRED','APPROVED','ARCHIVED'
                          )),
    publication_status    VARCHAR(20)  NOT NULL DEFAULT 'UNPUBLISHED'
                          CHECK (publication_status IN (
                              'UNPUBLISHED','SCHEDULED','PUBLISHED','SUPERSEDED','ROLLED_BACK'
                          )),
    current_version       INT          NOT NULL DEFAULT 1,
    active_version        INT,
    revision_count        INT          NOT NULL DEFAULT 0,
    max_revision_attempts INT          NOT NULL DEFAULT 3,
    human_review_flag     BOOLEAN      NOT NULL DEFAULT FALSE,
    human_review_reason   TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_lessons_domain_lang ON cf_lessons (domain_code, language_code);
CREATE INDEX idx_cf_lessons_content_status ON cf_lessons (content_status);

-- ─── Lesson Versions (immutable once frozen) ─────────────────────────────

CREATE TABLE cf_lesson_versions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id          UUID NOT NULL REFERENCES cf_lessons(id),
    version            INT  NOT NULL,
    content_status     VARCHAR(30) NOT NULL,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',

    -- Content blobs (domain-specific JSONB)
    blueprint          JSONB,
    content            JSONB,
    vocabulary         JSONB,
    grammar            JSONB,
    exercises          JSONB,
    audio_manifest     JSONB,

    -- Mandatory lineage (populated during pipeline execution)
    prompt_versions    JSONB,   -- {agent_type: prompt_version, ...}
    model_configs      JSONB,   -- {agent_type: model_config_key, ...}
    generator_run_ids  JSONB,   -- {agent_type: agent_run_id, ...}
    qa_run_ids         JSONB,   -- {agent_type: agent_run_id, ...}
    revision_log       JSONB,   -- [{version, agent_run_id, issues_fixed}, ...]

    frozen             BOOLEAN      NOT NULL DEFAULT FALSE,
    checksum           VARCHAR(64),

    published_at       TIMESTAMPTZ,
    superseded_at      TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    UNIQUE (lesson_id, version)
);

CREATE INDEX idx_cf_lesson_versions_published
    ON cf_lesson_versions (lesson_id, version DESC)
    WHERE frozen = TRUE AND publication_status = 'PUBLISHED';

-- ─── Agent Runs ───────────────────────────────────────────────────────────

CREATE TABLE cf_agent_runs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id         UUID         NOT NULL REFERENCES cf_lessons(id),
    lesson_version    INT          NOT NULL,
    agent_type        VARCHAR(60)  NOT NULL,
    domain_code       VARCHAR(20)  NOT NULL,
    language_code     VARCHAR(10),
    job_id            UUID,
    status            VARCHAR(20)  NOT NULL DEFAULT 'QUEUED'
                      CHECK (status IN ('QUEUED','RUNNING','SUCCEEDED','FAILED','RETRYING','SKIPPED_CACHED')),
    input_hash        VARCHAR(64),         -- SHA-256 for idempotency
    prompt_id         UUID         REFERENCES cf_agent_prompts(id),
    prompt_version    INT,
    model_config_key  VARCHAR(100),

    -- Output
    output            JSONB,
    confidence        NUMERIC(4,3),
    issues            JSONB,
    recommendations   JSONB,

    -- Cost tracking (every LLM call recorded)
    input_tokens      INT,
    output_tokens     INT,
    estimated_cost_usd NUMERIC(10,6),
    provider          VARCHAR(50),
    model_id          VARCHAR(100),
    latency_ms        BIGINT,

    retry_count       INT          NOT NULL DEFAULT 0,
    error_message     TEXT,
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_agent_runs_lesson    ON cf_agent_runs (lesson_id, lesson_version, agent_type);
CREATE INDEX idx_cf_agent_runs_status    ON cf_agent_runs (status) WHERE status IN ('QUEUED','RUNNING','RETRYING');
CREATE INDEX idx_cf_agent_runs_idempotent ON cf_agent_runs (lesson_id, lesson_version, agent_type, input_hash)
    WHERE status = 'SUCCEEDED';

-- ─── Workflow Events (append-only audit log) ─────────────────────────────

CREATE TABLE cf_workflow_events (
    id              BIGSERIAL    PRIMARY KEY,
    lesson_id       UUID         NOT NULL REFERENCES cf_lessons(id),
    lesson_version  INT,
    from_status     VARCHAR(30),
    to_status       VARCHAR(30)  NOT NULL,
    status_type     VARCHAR(20)  NOT NULL CHECK (status_type IN ('content','publication')),
    trigger         VARCHAR(60)  NOT NULL,
    actor           VARCHAR(100),         -- 'system', 'admin:uuid', 'agent:type'
    agent_run_id    UUID,
    metadata        JSONB,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_workflow_events_lesson ON cf_workflow_events (lesson_id, occurred_at DESC);

-- ─── Cost Ledger ──────────────────────────────────────────────────────────

CREATE TABLE cf_cost_ledger (
    id              BIGSERIAL    PRIMARY KEY,
    lesson_id       UUID         REFERENCES cf_lessons(id),
    lesson_version  INT,
    agent_run_id    UUID         REFERENCES cf_agent_runs(id),
    domain_code     VARCHAR(20),
    language_code   VARCHAR(10),
    provider        VARCHAR(50),
    model_id        VARCHAR(100),
    input_tokens    INT,
    output_tokens   INT,
    cost_usd        NUMERIC(10,6),
    budget_key      VARCHAR(100),  -- 'daily' | 'per_lesson:{id}'
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cf_cost_ledger_date   ON cf_cost_ledger (recorded_at);
CREATE INDEX idx_cf_cost_ledger_lesson ON cf_cost_ledger (lesson_id);

-- ─── Seed: language domain ────────────────────────────────────────────────

INSERT INTO cf_domains (code, display_name, active) VALUES
    ('language', 'Language Learning', TRUE),
    ('dsa',      'Data Structures & Algorithms', FALSE);  -- not active yet

-- ─── Seed: default model configs ─────────────────────────────────────────

INSERT INTO cf_ai_model_configs
    (config_key, provider, model_id, temperature, max_tokens, timeout_ms,
     cost_per_1k_input_usd, cost_per_1k_output_usd)
VALUES
    ('sonnet_gen_v1',  'anthropic', 'claude-sonnet-4-6',          0.30, 4096, 30000, 0.003000, 0.015000),
    ('haiku_qa_v1',    'anthropic', 'claude-haiku-4-5-20251001',   0.10, 2048, 15000, 0.000250, 0.001250),
    ('mock_v1',        'mock',       'mock-model',                  0.00, 9999, 1000,  0.000000, 0.000000);
