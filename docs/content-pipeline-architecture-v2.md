# Academy Content Factory — Architecture v2

> **Status:** Proposed — awaiting implementation approval
> **Date:** 2026-09-25
> **Supersedes:** content-pipeline-architecture.md

---

## 1. Updated Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                        ACADEMY CONTENT FACTORY                                   │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                         SHARED CORE INFRASTRUCTURE                          │ │
│  │                                                                             │ │
│  │   Admin CMS ──► Pipeline API (REST + WebSocket)                            │ │
│  │                      │                                                     │ │
│  │                      ▼                                                     │ │
│  │              Workflow Orchestrator                                          │ │
│  │         (State Machine + Scheduler + Audit)                                │ │
│  │                      │                                                     │ │
│  │                      ▼                                                     │ │
│  │              Job Queue (BullMQ / Redis)                                    │ │
│  │         high: final-gate/publish  med: generation  low: improvement        │ │
│  │                                                                             │ │
│  │   Model Router ── Prompt Registry ── Cost Ledger ── Audit Log             │ │
│  │   Version Store ── Vector Store ── CDN ── Analytics Bus                   │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                               │                                                  │
│           ┌───────────────────┼───────────────────┐                             │
│           │                   │                   │                             │
│           ▼                   ▼                   ▼                             │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐                  │
│  │ LANGUAGE        │ │ DSA             │ │ AI SKILLS        │                  │
│  │ CONTENT PIPELINE│ │ CONTENT PIPELINE│ │ CONTENT PIPELINE │                  │
│  │                 │ │                 │ │  (future)        │                  │
│  │ Language Plugin │ │ DSA Plugin      │ └─────────────────┘                  │
│  │ ┌─────────────┐ │ │ ┌─────────────┐│                                       │
│  │ │ de / fr / ko│ │ │ │ algorithms  ││                                       │
│  │ │ es / ja /...│ │ │ │ data structs││                                       │
│  │ └─────────────┘ │ │ └─────────────┘│                                       │
│  │                 │ │                 │                                       │
│  │ Planning        │ │ Planning        │                                       │
│  │ Generation      │ │ Generation      │                                       │
│  │ Language QA     │ │ DSA QA          │                                       │
│  │ CEFR QA         │ │ Correctness QA  │                                       │
│  │ Pedagogy QA     │ │ Pedagogy QA     │                                       │
│  │ Exercise QA     │ │ Exercise QA     │                                       │
│  └─────────────────┘ └─────────────────┘                                       │
│                                                                                  │
│  ─────────────────── SHARED PIPELINE STAGES ─────────────────────────────────  │
│                                                                                  │
│  [Generated Content]                                                             │
│       │                                                                          │
│       ▼                                                                          │
│  ┌─────────────────────────────────────────┐                                    │
│  │  DETERMINISTIC VALIDATION LAYER         │  ← NO LLM cost here               │
│  │  Schema · References · Duplicates ·     │                                    │
│  │  Required Fields · CEFR enum · Audio    │                                    │
│  └──────────────┬──────────────────────────┘                                    │
│                 │ pass                  fail → back to REVISION                 │
│                 ▼                                                                │
│  ┌─────────────────────────────────────────┐                                    │
│  │  PARALLEL LLM QA CLUSTER                │                                    │
│  │                                         │                                    │
│  │  Linguistic QA ──┐                      │                                    │
│  │  CEFR QA ────────┤                      │                                    │
│  │  Pedagogy QA ────┼──► QA Aggregator     │                                    │
│  │  Exercise QA ────┤    (scores + issues) │                                    │
│  │  Consistency QA ─┘                      │                                    │
│  └──────────────┬──────────────────────────┘                                    │
│                 │                                                                │
│         ┌───────┴────────┐                                                      │
│         │ any fail       │ all pass                                             │
│         ▼                ▼                                                       │
│    REVISION         FINAL GATE                                                  │
│    (Editor)         (deterministic)                                             │
│         │                │                                                      │
│         │ max_revisions   ▼                                                     │
│         │ exceeded   APPROVED ──► Publisher ──► CDN + Index                   │
│         ▼                                                                       │
│   HUMAN_REVIEW                                                                  │
│   REQUIRED                                                                      │
│                                                                                  │
│  ────────────────── FEEDBACK LOOP ──────────────────────────────────────────── │
│  Learner Events → Analytics Agent → Improvement Agent → new DRAFT version      │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Domain / Language Abstraction

### 2.1 Three-layer model

```
Content Factory Core          — shared forever
    │
    ├── Domain Plugin         — one per domain (language / dsa / ai_skills)
    │       │
    │       └── Language Profile   — one per language (de / fr / ko / es / ja)
    │               │
    │               └── Agent Config — model, prompt_id, thresholds, rules
```

### 2.2 Domain Plugin contract

```typescript
interface DomainPlugin {
  domain_id:        string;          // 'language' | 'dsa' | 'ai_skills'
  display_name:     string;
  active_agents:    AgentType[];     // which agents run for this domain
  qa_thresholds:    QAThresholds;
  exercise_types:   string[];
  content_model:    ContentModelRef; // JSON schema for this domain's content
  validator_rules:  ValidatorRule[];
}
```

### 2.3 Language Profile contract

```typescript
interface LanguageProfile {
  language_code:     string;         // ISO 639-1: 'de', 'fr', 'ko', 'es', 'ja'
  display_name:      string;
  script:            string;         // 'latin' | 'hangul' | 'kanji+kana' | ...
  cefr_applicable:   boolean;        // false for DSA
  rtl:               boolean;
  linguistic_qa_agent: AgentType;    // 'de_linguistic_qa' | 'fr_linguistic_qa' | ...
  prompt_ids: {
    content_generator:  string;
    vocabulary:         string;
    grammar:            string;
    dialogue:           string;
    exercise_generator: string;
    linguistic_qa:      string;
    cefr_qa:            string;
  };
  char_validation_regex: string;     // language-specific char set check
  style_guide_ref:       string;
}
```

### 2.4 Agent resolution

When the orchestrator needs to run `LINGUISTIC_QA` for a German lesson, it resolves:

```
domain=language → language_code=de → linguistic_qa_agent=de_linguistic_qa
                                    → prompt_id=de_linguistic_qa_v3
                                    → model=claude-haiku-4-5
```

For French, the same orchestrator resolves to `fr_linguistic_qa` with French-specific prompts. The orchestrator code never changes — only the profile.

---

## 3. Dual Status Model

Content status and publication status are separate fields on separate concerns.

### content_status

```
DRAFT
PLANNED
GENERATING
GENERATED
VALIDATION_PENDING   ← deterministic layer
VALIDATION_FAILED
QA_PENDING           ← LLM QA layer
QA_FAILED
REVISION
QA_PASSED
HUMAN_REVIEW_REQUIRED
APPROVED
ARCHIVED
```

### publication_status

```
UNPUBLISHED
SCHEDULED
PUBLISHED
SUPERSEDED           ← replaced by a newer version
ROLLED_BACK          ← manually reverted to older version
```

These are independent. A lesson can be `content_status=APPROVED, publication_status=SCHEDULED` or `content_status=APPROVED, publication_status=ROLLED_BACK`.

---

## 4. Workflow State Machine

### 4.1 Content status transitions

```
DRAFT
  │ plan()                     guard: blueprint valid, no dup unit
  ▼
PLANNED
  │ generate()                 guard: sub-agent slots available
  ▼
GENERATING ──(sub-job fails)──► DRAFT + error event
  │ all sub-jobs complete
  ▼
GENERATED
  │ submit_for_validation()    guard: content_model schema present
  ▼
VALIDATION_PENDING
  │                ┌─────────────────────────────────────┐
  │ pass           │ fail (any ERROR-severity check)      │
  ▼                ▼                                      │
QA_PENDING    VALIDATION_FAILED                          │
  │ (parallel QA)  │ route_to_revision()                 │
  │                ▼                           back to   │
  │            REVISION ─────────────────────────────────┘
  │ all pass   (Editor Agent)
  ▼
QA_PASSED
  │ submit_for_final_review()
  ▼
HUMAN_REVIEW_REQUIRED  ◄── (confidence < threshold OR revision_count ≥ max)
  │ approve() / reject()
  │
  ├─ approved ──► APPROVED
  └─ rejected ──► REVISION

APPROVED
  │ (publication_status transitions separately)
  ▼
  [stays APPROVED until archived]


QA_FAILED path:
  QA_PENDING
    │ any mandatory QA gate fails
    ▼
  QA_FAILED
    │ revision_count < max_revision_attempts
    ▼
  REVISION ──► QA_PENDING (loop)
    │ revision_count ≥ max_revision_attempts
    ▼
  HUMAN_REVIEW_REQUIRED  ← never auto-publishes
```

### 4.2 Publication status transitions

```
UNPUBLISHED
  │ schedule() or publish()
  ▼
SCHEDULED ──► PUBLISHED   (at scheduled_at time or immediately)
  │
  ▼
PUBLISHED
  │ newer version published
  ▼
SUPERSEDED                 (old version still readable by in-progress learners)
  │ rollback() triggered
  ▼
ROLLED_BACK               (this version becomes active again)
                           (the version that superseded it → SUPERSEDED)
```

### 4.3 Revision loop protection

```
max_revision_attempts: configurable per domain+level (default: 3)

Before routing to REVISION:
  IF lesson.revision_count >= max_revision_attempts:
    → content_status = HUMAN_REVIEW_REQUIRED
    → human_review_tasks INSERT (reason='REVISION_LIMIT_EXCEEDED')
    → STOP. No further automatic revision.
```

### 4.4 Human escalation triggers

| Trigger | Condition |
|---|---|
| Revision limit exceeded | `revision_count >= max_revision_attempts` |
| Low confidence | `overall_confidence < 0.70` |
| QA agent disagreement | Highest score − lowest score > 0.30 |
| Cultural flag | Any QA agent sets `cultural_flag=true` |
| Linguistic uncertainty | `linguistic_qa.confidence < 0.75` |
| Sensitive topic | Content filter sets `sensitive=true` |
| Curriculum change | Structural change affects > 20% of unit lessons |
| Manual trigger | Admin clicks "Request Human Review" |

---

## 5. Database Schema

### 5.1 Core infrastructure tables

```sql
-- ─── Domain & Language Configuration ──────────────────────────────────────

CREATE TABLE domains (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(20)  NOT NULL UNIQUE,   -- 'language', 'dsa', 'ai_skills'
    display_name VARCHAR(100) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    plugin_config JSONB       NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE language_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain_id             UUID NOT NULL REFERENCES domains(id),
    language_code         VARCHAR(10)  NOT NULL UNIQUE,  -- 'de', 'fr', 'ko'
    display_name          VARCHAR(100) NOT NULL,
    script                VARCHAR(30),
    cefr_applicable       BOOLEAN NOT NULL DEFAULT TRUE,
    rtl                   BOOLEAN NOT NULL DEFAULT FALSE,
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    linguistic_qa_agent   VARCHAR(60),
    char_validation_regex VARCHAR(500),
    style_guide_ref       VARCHAR(200),
    prompt_ids            JSONB NOT NULL DEFAULT '{}',
    qa_thresholds         JSONB NOT NULL DEFAULT '{}',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Prompt Registry ────────────────────────────────────────────────────────

CREATE TABLE agent_prompts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_key    VARCHAR(100) NOT NULL,   -- 'de_linguistic_qa', 'cefr_qa', ...
    agent_type    VARCHAR(60)  NOT NULL,
    domain_code   VARCHAR(20)  NOT NULL,
    language_code VARCHAR(10),            -- NULL = domain-wide
    version       INT          NOT NULL,
    prompt_text   TEXT         NOT NULL,
    system_prompt TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
                               -- DRAFT | ACTIVE | DEPRECATED | ARCHIVED
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    activated_at  TIMESTAMPTZ,
    deprecated_at TIMESTAMPTZ,
    created_by    VARCHAR(100),
    change_notes  TEXT,
    UNIQUE (prompt_key, version)
);

CREATE INDEX idx_agent_prompts_active
    ON agent_prompts (prompt_key, version DESC)
    WHERE status = 'ACTIVE';

-- ─── Model Provider Registry ────────────────────────────────────────────────

CREATE TABLE ai_model_configs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key    VARCHAR(100) NOT NULL UNIQUE, -- 'haiku_qa', 'sonnet_gen', ...
    provider      VARCHAR(50)  NOT NULL,         -- 'anthropic', 'openai', 'google'
    model_id      VARCHAR(100) NOT NULL,
    temperature   NUMERIC(3,2) NOT NULL DEFAULT 0.3,
    max_tokens    INT          NOT NULL DEFAULT 4096,
    timeout_ms    INT          NOT NULL DEFAULT 30000,
    cost_per_1k_input_usd  NUMERIC(8,6) NOT NULL,
    cost_per_1k_output_usd NUMERIC(8,6) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Curriculum ─────────────────────────────────────────────────────────────

CREATE TABLE curriculum_manifests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code   VARCHAR(10) NOT NULL,
    cefr_level      VARCHAR(2),   -- NULL for non-CEFR domains
    domain_code     VARCHAR(20)  NOT NULL,
    version         INT          NOT NULL DEFAULT 1,
    content_status  VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    manifest        JSONB        NOT NULL,
    agent_run_id    UUID,
    created_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (language_code, cefr_level, version)
);

CREATE TABLE curriculum_units (
    id                    UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    manifest_id           UUID    NOT NULL REFERENCES curriculum_manifests(id),
    unit_number           INT     NOT NULL,
    theme                 VARCHAR(255) NOT NULL,
    learning_objectives   JSONB   NOT NULL,
    cefr_level            VARCHAR(2),
    skill_focus           TEXT[],
    prerequisite_unit_ids UUID[],
    display_order         INT     NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (manifest_id, unit_number)
);

-- ─── Content Dependency Graph ───────────────────────────────────────────────

CREATE TABLE content_dependencies (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id        UUID        NOT NULL,   -- the dependent
    depends_on_type  VARCHAR(30) NOT NULL,   -- 'lesson' | 'grammar' | 'vocabulary' | 'skill'
    depends_on_id    UUID        NOT NULL,   -- the prerequisite
    dependency_kind  VARCHAR(30) NOT NULL,   -- 'required' | 'recommended'
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_content_deps_lesson ON content_dependencies (lesson_id);
CREATE INDEX idx_content_deps_prereq ON content_dependencies (depends_on_id);

-- ─── Lessons ────────────────────────────────────────────────────────────────

CREATE TABLE lessons (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id              UUID NOT NULL REFERENCES curriculum_units(id),
    lesson_number        INT  NOT NULL,
    stable_ref           VARCHAR(50) NOT NULL UNIQUE, -- 'de-a1-u01-l01' permanent
    title                VARCHAR(255),
    domain_code          VARCHAR(20) NOT NULL,
    language_code        VARCHAR(10),
    cefr_level           VARCHAR(2),
    skill_focus          TEXT[],
    content_status       VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    publication_status   VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',
    current_version      INT         NOT NULL DEFAULT 1,
    active_version       INT,               -- currently published version
    revision_count       INT         NOT NULL DEFAULT 0,
    max_revision_attempts INT        NOT NULL DEFAULT 3,
    human_review_flag    BOOLEAN     NOT NULL DEFAULT FALSE,
    human_review_reason  TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (unit_id, lesson_number)
);

-- Immutable version snapshots — INSERT ONLY once frozen
CREATE TABLE lesson_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID    NOT NULL REFERENCES lessons(id),
    version         INT     NOT NULL,
    content_status  VARCHAR(30) NOT NULL,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',

    -- Content blobs (structured per domain content model)
    blueprint       JSONB,
    content         JSONB,
    vocabulary      JSONB,
    grammar         JSONB,
    dialogues       JSONB,
    exercises       JSONB,
    audio_manifest  JSONB,

    -- Lineage (mandatory for published versions)
    curriculum_version  INT,
    manifest_id         UUID,
    prompt_versions     JSONB,  -- {agent_type: prompt_version, ...}
    model_configs       JSONB,  -- {agent_type: model_config_key, ...}
    generator_run_ids   JSONB,  -- {agent_type: agent_run_id, ...}
    qa_run_ids          JSONB,  -- {agent_type: agent_run_id, ...}
    revision_log        JSONB,  -- [{version, agent_run_id, issues_fixed}, ...]

    frozen              BOOLEAN     NOT NULL DEFAULT FALSE,
    checksum            VARCHAR(64),         -- SHA-256 of content blob
    content_embedding   vector(1536),        -- for semantic dedup (pgvector)
    published_at        TIMESTAMPTZ,
    superseded_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, version)
);

CREATE INDEX idx_lesson_versions_published
    ON lesson_versions (lesson_id, version DESC)
    WHERE frozen = TRUE AND publication_status = 'PUBLISHED';

-- ─── Agent Runs ─────────────────────────────────────────────────────────────

CREATE TABLE agent_runs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id         UUID         NOT NULL REFERENCES lessons(id),
    lesson_version    INT          NOT NULL,
    agent_type        VARCHAR(60)  NOT NULL,
    domain_code       VARCHAR(20)  NOT NULL,
    language_code     VARCHAR(10),
    job_id            UUID,
    status            VARCHAR(20)  NOT NULL DEFAULT 'QUEUED',
    input_hash        VARCHAR(64),         -- idempotency key
    prompt_id         UUID         REFERENCES agent_prompts(id),
    prompt_version    INT,
    model_config_key  VARCHAR(100),
    output            JSONB,
    confidence        NUMERIC(4,3),
    issues            JSONB,
    recommendations   JSONB,
    -- Cost tracking
    input_tokens      INT,
    output_tokens     INT,
    estimated_cost_usd NUMERIC(10,6),
    provider          VARCHAR(50),
    model_id          VARCHAR(100),
    latency_ms        INT,
    retry_count       INT          NOT NULL DEFAULT 0,
    error_message     TEXT,
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_runs_lesson  ON agent_runs (lesson_id, lesson_version, agent_type);
CREATE INDEX idx_agent_runs_status  ON agent_runs (status) WHERE status IN ('QUEUED','RUNNING','RETRYING');
CREATE INDEX idx_agent_runs_cost    ON agent_runs (created_at, estimated_cost_usd);

-- ─── QA Results ─────────────────────────────────────────────────────────────

CREATE TABLE qa_results (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID         NOT NULL REFERENCES lessons(id),
    lesson_version  INT          NOT NULL,
    agent_run_id    UUID         NOT NULL REFERENCES agent_runs(id),
    qa_agent        VARCHAR(60)  NOT NULL,
    gate_type       VARCHAR(20)  NOT NULL,  -- 'DETERMINISTIC' | 'LLM'
    passed          BOOLEAN      NOT NULL,
    score           NUMERIC(4,3),
    cultural_flag   BOOLEAN      NOT NULL DEFAULT FALSE,
    issues          JSONB        NOT NULL DEFAULT '[]',
    passed_checks   JSONB        NOT NULL DEFAULT '[]',
    failed_checks   JSONB        NOT NULL DEFAULT '[]',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- QA Aggregation result per lesson+version
CREATE TABLE qa_aggregations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id           UUID    NOT NULL REFERENCES lessons(id),
    lesson_version      INT     NOT NULL,
    overall_passed      BOOLEAN NOT NULL,
    overall_score       NUMERIC(4,3),
    overall_confidence  NUMERIC(4,3),
    mandatory_failed    JSONB,  -- which mandatory agents failed
    human_escalation_triggers JSONB,
    agent_scores        JSONB,  -- {agent: score, ...}
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, lesson_version)
);

-- ─── Workflow Audit Log (append-only, no deletes/updates) ───────────────────

CREATE TABLE workflow_events (
    id              BIGSERIAL    PRIMARY KEY,
    lesson_id       UUID         NOT NULL REFERENCES lessons(id),
    lesson_version  INT,
    from_status     VARCHAR(30),
    to_status       VARCHAR(30)  NOT NULL,
    status_type     VARCHAR(20)  NOT NULL,  -- 'content' | 'publication'
    trigger         VARCHAR(60)  NOT NULL,
    actor           VARCHAR(100),
    agent_run_id    UUID,
    metadata        JSONB,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workflow_events_lesson ON workflow_events (lesson_id, occurred_at DESC);

-- ─── Human Review ───────────────────────────────────────────────────────────

CREATE TABLE human_review_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID         NOT NULL REFERENCES lessons(id),
    lesson_version  INT          NOT NULL,
    reason          VARCHAR(60)  NOT NULL,
    priority        SMALLINT     NOT NULL DEFAULT 2,
    assigned_to     VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    resolution      VARCHAR(20),
    reviewer_notes  TEXT,
    qa_aggregation_id UUID       REFERENCES qa_aggregations(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ
);

-- ─── Published Versions Index ───────────────────────────────────────────────

CREATE TABLE published_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID         NOT NULL REFERENCES lessons(id),
    lesson_version  INT          NOT NULL,
    language_code   VARCHAR(10),
    domain_code     VARCHAR(20)  NOT NULL,
    cefr_level      VARCHAR(2),
    stable_ref      VARCHAR(50)  NOT NULL,  -- 'de-a1-u01-l01'
    content_url     VARCHAR(500),
    audio_urls      JSONB,
    published_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    superseded_at   TIMESTAMPTZ,
    superseded_by   INT,         -- lesson_version that replaced this
    rolled_back_at  TIMESTAMPTZ,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    FOREIGN KEY (lesson_id, lesson_version) REFERENCES lesson_versions(lesson_id, version)
);

-- ─── Learner Progress (compatibility) ───────────────────────────────────────

CREATE TABLE learner_lesson_progress (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    learner_id          UUID         NOT NULL,
    lesson_id           UUID         NOT NULL REFERENCES lessons(id),
    lesson_version      INT          NOT NULL,  -- version they started on
    started_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_activity_at    TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    completion_pct      NUMERIC(4,3),
    score               NUMERIC(4,3),
    -- migration: when new version published, learner can finish current
    -- then auto-migrate to latest on next lesson start
    migrated_to_version INT,
    migrated_at         TIMESTAMPTZ
);

CREATE INDEX idx_learner_progress ON learner_lesson_progress (learner_id, lesson_id);

-- ─── Cost Ledger ────────────────────────────────────────────────────────────

CREATE TABLE cost_ledger (
    id              BIGSERIAL    PRIMARY KEY,
    lesson_id       UUID         REFERENCES lessons(id),
    lesson_version  INT,
    agent_run_id    UUID         REFERENCES agent_runs(id),
    domain_code     VARCHAR(20),
    language_code   VARCHAR(10),
    provider        VARCHAR(50),
    model_id        VARCHAR(100),
    input_tokens    INT,
    output_tokens   INT,
    cost_usd        NUMERIC(10,6),
    budget_key      VARCHAR(100), -- 'daily' | 'per_lesson:{id}' | 'per_agent:{type}'
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cost_ledger_date   ON cost_ledger (recorded_at::date);
CREATE INDEX idx_cost_ledger_lesson ON cost_ledger (lesson_id);

-- ─── Semantic Dedup Store ────────────────────────────────────────────────────
-- Requires pgvector extension

CREATE TABLE content_embeddings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID         NOT NULL REFERENCES lessons(id),
    lesson_version  INT          NOT NULL,
    content_type    VARCHAR(30)  NOT NULL,  -- 'example' | 'exercise' | 'dialogue' | 'vocab'
    content_id      VARCHAR(100) NOT NULL,  -- item ID within the lesson
    content_text    TEXT         NOT NULL,
    embedding       vector(1536) NOT NULL,
    language_code   VARCHAR(10),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_embeddings_vec ON content_embeddings
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- ─── Analytics ──────────────────────────────────────────────────────────────

CREATE TABLE lesson_analytics (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id         UUID         NOT NULL REFERENCES lessons(id),
    lesson_version    INT          NOT NULL,
    period_start      DATE         NOT NULL,
    period_end        DATE         NOT NULL,
    learner_count     INT,
    completion_rate   NUMERIC(4,3),
    avg_score         NUMERIC(4,3),
    avg_time_sec      INT,
    drop_off_rate     NUMERIC(4,3),
    difficulty_rating NUMERIC(3,2),
    error_patterns    JSONB,
    computed_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, lesson_version, period_start)
);

-- ─── Regression QA Tracking ─────────────────────────────────────────────────

CREATE TABLE regression_qa_runs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trigger_type     VARCHAR(50) NOT NULL, -- 'prompt_change' | 'vocab_change' | 'curriculum_change'
    trigger_ref      VARCHAR(200),         -- e.g. prompt_key + version
    affected_lessons JSONB,               -- [{lesson_id, reason}, ...]
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 6. Agent Input / Output Contracts

### 6.1 Universal envelope (unchanged, extended)

```typescript
interface AgentInput<T> {
  agent_run_id:    string;
  lesson_id:       string;
  lesson_version:  number;
  input_hash:      string;           // SHA-256 — idempotency key
  payload:         T;
  context: {
    domain_code:   string;           // 'language' | 'dsa' | 'ai_skills'
    language_code: string | null;    // null for DSA
    cefr_level:    string | null;
    stable_ref:    string;           // 'de-a1-u01-l01'
    retry_count:   number;
    max_retries:   number;
    prompt_id:     string;           // UUID of agent_prompts row
    prompt_version: number;
    model_config_key: string;        // key into ai_model_configs
  };
}

interface AgentOutput<T> {
  agent_run_id:    string;
  status:          'SUCCEEDED' | 'FAILED' | 'PARTIAL';
  output:          T | null;
  confidence:      number;           // 0.0–1.0
  issues:          Issue[];
  recommendations: string[];
  cultural_flag:   boolean;          // triggers human escalation
  metadata: {
    model_config_key:  string;
    model_id:          string;
    provider:          string;
    input_tokens:      number;
    output_tokens:     number;
    estimated_cost_usd: number;
    latency_ms:        number;
    prompt_id:         string;
    prompt_version:    number;
    agent_version:     string;       // semver of agent code
  };
}

interface Issue {
  code:        string;
  severity:    'ERROR' | 'WARNING' | 'INFO';
  field:       string;               // JSON path
  message:     string;
  suggestion?: string;
  cultural?:   boolean;
}
```

### 6.2 QA Aggregator contract

The QA Aggregator is not an LLM agent — it is deterministic logic that collects all parallel QA results and produces the routing decision.

```typescript
interface QAAggregatorInput {
  lesson_id:       string;
  lesson_version:  number;
  qa_results:      QAAgentResult[];     // all parallel QA outputs
  thresholds:      QAThresholds;        // from language_profile.qa_thresholds
  revision_count:  number;
  max_revisions:   number;
}

interface QAAggregatorOutput {
  overall_passed:   boolean;
  overall_score:    number;
  overall_confidence: number;
  routing_decision: 'QA_PASSED' | 'QA_FAILED' | 'HUMAN_REVIEW_REQUIRED';
  mandatory_failed: string[];           // agent types that failed mandatory gates
  human_escalation_triggers: string[];  // reasons for human escalation
  agent_scores: Record<string, number>;
  all_issues:   Issue[];
}

interface QAThresholds {
  min_pass_scores: Record<string, number>;    // per agent type
  auto_approve_scores: Record<string, number>;
  combined_min_pass:    number;
  combined_auto_approve: number;
  confidence_min:       number;               // below = human escalation
  max_score_spread:     number;               // above = human escalation (disagreement)
}
```

---

## 7. Queue Architecture

### 7.1 Queue topology

```
pipeline:planning           concurrency=5   planning agents
pipeline:generation         concurrency=10  generation sub-agents (parallel per lesson)
pipeline:validation         concurrency=15  deterministic only — fast + cheap
pipeline:qa                 concurrency=8   LLM QA agents (parallel per lesson)
pipeline:qa-aggregation     concurrency=10  QA Aggregator (deterministic)
pipeline:revision           concurrency=5   Editor Agent
pipeline:final-gate         concurrency=3   Final QA Gate (deterministic)
pipeline:publishing         concurrency=2   Publisher Agent
pipeline:analytics          concurrency=3   Analytics Agent
pipeline:improvement        concurrency=2   Content Improvement Agent
pipeline:regression-qa      concurrency=4   Regression QA jobs
pipeline:human-notify       concurrency=1   Notifications only
pipeline:dlq                               Dead-letter — ops alert on insert
```

### 7.2 Per-lesson generation fan-out

When a lesson enters GENERATING, the orchestrator fans out 6 parallel sub-jobs:

```
generate(lesson) →
  ├── job: content_generator
  ├── job: vocabulary_agent
  ├── job: grammar_agent
  ├── job: dialogue_agent
  ├── job: exercise_generator
  └── job: voice_audio_agent

All 6 must complete before → GENERATED
Any 1 fails after max retries → GENERATING (held) + ops alert
```

### 7.3 Per-lesson QA fan-out

When a lesson enters QA_PENDING, the orchestrator fans out parallel QA jobs then waits for all:

```
qa(lesson) →
  ├── job: [language]_linguistic_qa   (resolved from language_profile)
  ├── job: cefr_qa
  ├── job: pedagogy_qa
  ├── job: exercise_qa
  └── job: consistency_qa

All complete → qa_aggregation job runs (deterministic, fast)
QAAggregator produces routing_decision
```

### 7.4 Idempotency

Before executing any job, the worker checks:

```sql
SELECT id FROM agent_runs
WHERE lesson_id = $1
  AND lesson_version = $2
  AND agent_type = $3
  AND input_hash = $4
  AND status = 'SUCCEEDED'
LIMIT 1;
```

If found → return cached output, mark job complete, zero LLM cost.

### 7.5 Retry policy

| Queue | Attempts | Backoff |
|---|---|---|
| planning | 3 | 5s / 25s / 125s |
| generation | 4 | 10s / 60s / 300s / 900s |
| validation | 2 | 2s / 10s |
| qa | 3 | 5s / 30s / 150s |
| revision | 3 | 10s / 60s / 300s |
| publishing | 5 | 2s / 10s / 30s / 60s / 120s |

---

## 8. Deterministic Validation Layer

Runs BEFORE any LLM QA. Zero LLM cost. Fast (< 200ms per lesson).

### Checks

| Code | Check | Severity |
|---|---|---|
| `SCHEMA_MISSING_FIELD` | Required JSON fields present per domain content model | ERROR |
| `SCHEMA_WRONG_TYPE` | Field types match declared schema | ERROR |
| `REF_BROKEN_EXERCISE` | Exercise answer refs point to real vocab IDs | ERROR |
| `REF_BROKEN_PREREQ` | Prerequisite lesson IDs exist in curriculum_units | ERROR |
| `DUP_EXERCISE_ID` | No duplicate exercise IDs within lesson | ERROR |
| `DUP_VOCAB_ID` | No duplicate vocabulary IDs | ERROR |
| `CEFR_INVALID` | CEFR level is valid enum value | ERROR |
| `AUDIO_MISSING` | vocab items with audio=true have audio_manifest entry | ERROR |
| `ANSWER_MISSING` | Every exercise has non-empty answer_key | ERROR |
| `TRANSLATION_MISSING` | All vocabulary items have ≥ 1 translation | ERROR |
| `WORD_COUNT_OOB` | Lesson body within min/max for CEFR level | WARNING |
| `CHAR_ENCODING` | Language-specific char validation regex passes | WARNING |
| `SEMANTIC_NEAR_DUP` | Content embedding similarity > 0.92 with existing lesson | WARNING |

If any ERROR → `VALIDATION_FAILED`, route to `REVISION` immediately, skip LLM QA.

---

## 9. Prompt & Model Management

### 9.1 Prompt lifecycle

```
DRAFT → ACTIVE → DEPRECATED → ARCHIVED

Only one ACTIVE version per prompt_key at any time.
Activation auto-deprecates the previous ACTIVE version.
```

### 9.2 Model Router

The Model Router resolves which provider/model to use per agent execution. Agents never reference providers or models directly — they reference a `model_config_key`.

```typescript
interface ModelRouter {
  resolve(agent_type: string, domain: string, language?: string): AIModelConfig;
  // Returns config from ai_model_configs table
  // Resolution order:
  //   1. agent_type + domain + language_code  (most specific)
  //   2. agent_type + domain
  //   3. agent_type
  //   4. domain default
}
```

### 9.3 Default model assignment

| Agent group | Default model | Rationale |
|---|---|---|
| Curriculum Architect, Lesson Planner | claude-sonnet-5 | Complex reasoning |
| Content Generator, Dialogue | claude-sonnet-5 | High quality prose |
| Vocabulary, Grammar, Exercise | claude-sonnet-5 | Structured output |
| All QA agents | claude-haiku-4-5 | Fast, cheap, structured |
| QA Aggregator, Final Gate, Validation | No LLM | Pure deterministic |
| Analytics, Improvement | claude-haiku-4-5 | Analysis, not generation |

---

## 10. Content Versioning & Lineage

### 10.1 Immutability contract

```
lesson_version.frozen = TRUE
  → NO UPDATE allowed on that row (enforced at DB role level)
  → Checksum verified on every CDN-facing read
  → Any change = new version INSERT
```

### 10.2 Mandatory lineage fields (required before publish)

Every published `lesson_version` must have non-null values for:

```json
{
  "curriculum_version":  1,
  "manifest_id":         "uuid",
  "prompt_versions": {
    "content_generator": 3,
    "vocabulary":        2,
    "grammar":           1,
    "dialogue":          4,
    "exercise_generator": 2,
    "de_linguistic_qa":  3,
    "cefr_qa":           2,
    "pedagogy_qa":       1
  },
  "model_configs": {
    "content_generator": "sonnet_gen_v2",
    "de_linguistic_qa":  "haiku_qa_v1"
  },
  "generator_run_ids": { ... },
  "qa_run_ids":        { ... },
  "revision_log":      [ ... ]
}
```

The Final QA Gate blocks publication if any lineage field is null.

### 10.3 Version lineage example

```
lesson: de-a1-u01-l01

v1 → PUBLISHED 2026-10-01  (frozen, checksum=abc)
v2 → PUBLISHED 2026-11-15  (frozen, checksum=def, supersedes v1)
     v1.publication_status = 'SUPERSEDED'
     learners mid-lesson-v1 continue on v1 until they finish
     new learners get v2

v3 → DRAFT  (analytics-triggered from v2 completion data)
     goes through full pipeline
     when v3 approved + published:
     v2.publication_status = 'SUPERSEDED'
```

### 10.4 Rollback

```
v2 PUBLISHED → problem discovered → rollback()
  v2.publication_status = 'ROLLED_BACK'
  v1.publication_status = 'PUBLISHED'  (reactivated)
  lessons.active_version = 1
  workflow_event: {trigger: 'rollback', actor: 'admin:user_id',
                   metadata: {from_v: 2, to_v: 1, reason: '...'}}
  CDN cache invalidated → serves v1 content_url
  New learners get v1 immediately
  In-progress v2 learners: grace period (default 24h) then migrated to v1
```

---

## 11. Learner-Content Compatibility Strategy

### Core principle

`lesson_id` is the permanent stable identity. `lesson_version` is the immutable content snapshot. Learner progress always records BOTH.

### Three learner states

| State | Behaviour |
|---|---|
| **Not started** | Always gets the latest `PUBLISHED` version |
| **In progress on vN** | Stays on vN until lesson complete. vN remains readable indefinitely. |
| **Completed on vN** | Progress recorded against vN. If re-attempting, offered latest version. |

### Version migration

When `active_version` advances from vN to vN+1:
- In-progress learners on vN: no disruption. Finish on vN.
- After completion, `migrated_to_version = N+1` is set passively.
- The learner-facing API always returns `MAX(version) WHERE publication_status='PUBLISHED'` for new starts.

### Progress continuity on rollback

If v2 → ROLLED_BACK:
- In-progress v2 learners: grace period (configurable, default 24h), then continue on v1 from their last checkpoint. Exercise progress is non-transferable (different content); completion status is preserved.

---

## 12. API Design

```
Base: /api/v1/pipeline

── Config / Plugins ────────────────────────────────────────────────────────
GET    /domains                              List domains
GET    /domains/:code/languages              List language profiles for domain
PUT    /domains/:code/languages/:lang        Update language profile
GET    /prompts                              List all prompts
GET    /prompts/:key                         List versions for prompt key
POST   /prompts/:key                         Create new prompt version
POST   /prompts/:key/:version/activate       Activate a prompt version
GET    /models                               List model configs
PUT    /models/:config_key                   Update model config

── Curriculum ──────────────────────────────────────────────────────────────
POST   /curriculum                           Create manifest job
GET    /curriculum/:id                       Get manifest + status
GET    /curriculum/:id/units                 List units
PUT    /curriculum/:id/units/:uid            Update unit (pre-generation only)

── Lessons ─────────────────────────────────────────────────────────────────
GET    /lessons                              List (filter: domain, lang, level, status)
POST   /lessons                              Create DRAFT
GET    /lessons/:id                          Lesson + current version summary
GET    /lessons/:id/versions                 All versions
GET    /lessons/:id/versions/:v              Full content snapshot
GET    /lessons/:id/versions/:v/lineage      Full lineage for this version
GET    /lessons/:id/versions/:v/diff/:v2     Diff between two versions

── Workflow Triggers ────────────────────────────────────────────────────────
POST   /lessons/:id/plan
POST   /lessons/:id/generate
POST   /lessons/:id/validate
POST   /lessons/:id/qa
POST   /lessons/:id/approve
POST   /lessons/:id/reject                   {body: {notes}}
POST   /lessons/:id/publish                  {body: {scheduled_at?}}
POST   /lessons/:id/unpublish
POST   /lessons/:id/rollback                 {body: {target_version}}
POST   /lessons/:id/improve

── Admin Manual Controls ────────────────────────────────────────────────────
POST   /lessons/:id/regenerate               Force re-generate from current blueprint
POST   /lessons/:id/run-qa                   Re-run all QA
POST   /lessons/:id/run-qa/:agent_type       Run specific QA agent
POST   /lessons/:id/run-revision             Trigger Editor Agent
POST   /lessons/:id/compare/:v1/:v2          Side-by-side version comparison

── Agent Runs ───────────────────────────────────────────────────────────────
GET    /lessons/:id/runs
GET    /lessons/:id/runs/:run_id
GET    /runs?status=FAILED&domain=language   Ops query

── QA ───────────────────────────────────────────────────────────────────────
GET    /lessons/:id/qa-results
GET    /lessons/:id/qa-results/:v
POST   /lessons/:id/qa/override              {agent, reason, reviewer}

── Human Review ─────────────────────────────────────────────────────────────
GET    /review-queue                         Filter: priority, status, domain, lang
GET    /review-queue/:task_id
POST   /review-queue/:task_id/assign
POST   /review-queue/:task_id/resolve        {resolution, notes}

── Publishing ───────────────────────────────────────────────────────────────
GET    /published                            Filter: domain, lang, level
GET    /published/:stable_ref                Latest published for stable ref
GET    /published/:stable_ref/history        All published versions

── Analytics ────────────────────────────────────────────────────────────────
POST   /analytics/ingest                     Batch learner events
GET    /analytics/:lesson_id
POST   /analytics/:lesson_id/trigger-improvement

── Cost ─────────────────────────────────────────────────────────────────────
GET    /cost/daily                           Today's spend
GET    /cost/lesson/:id                      Total cost for one lesson
GET    /cost/breakdown                       By domain / language / agent / model

── Regression QA ────────────────────────────────────────────────────────────
POST   /regression/scan                      Scan affected lessons after a change
GET    /regression/:run_id                   Status of regression run

── Observability ────────────────────────────────────────────────────────────
GET    /health
GET    /metrics                              Prometheus scrape
GET    /audit/:lesson_id                     Full workflow event log
```

**WebSocket:** `ws /api/v1/pipeline/lessons/:id/stream`
Real-time state transition events for CMS.

---

## 13. Cost Controls

### Budget hierarchy

```
1. Global daily budget        — $X/day total AI spend (configurable)
2. Per-domain budget          — $Y/day for 'language' domain
3. Per-lesson generation budget — $0.60 default max per lesson+version
4. Per-agent type budget      — e.g. QA agents collectively ≤ $0.15/lesson
```

All budgets enforced via Redis counters, decremented per `agent_run`. Exceeded budget → pause queue dispatch + alert ops.

### Token limits per agent call

| Agent | Max input tokens | Max output tokens |
|---|---|---|
| Content Generator | 8,000 | 4,000 |
| QA agents | 12,000 | 2,000 |
| Revision Agent | 16,000 | 6,000 |
| Curriculum Architect | 4,000 | 8,000 |

### Estimated cost per published A1 lesson

| Stage | Agents | Est. cost |
|---|---|---|
| Planning | Curriculum Architect + Lesson Planner | $0.04 |
| Generation | 5 agents × Sonnet | $0.22 |
| Audio | Voice agent (no LLM — TTS API) | $0.02 |
| QA | 5 agents × Haiku | $0.06 |
| Revision (avg 1×) | Editor × Haiku | $0.03 |
| Final Gate | Deterministic | $0.00 |
| **Total** | | **~$0.37** |

### Retry cost cap

Max retries × estimated cost must not exceed 3× the per-lesson budget. If a single agent has already consumed > $0.20 across retries, it is moved to DLQ rather than retried again.

---

## 14. Admin Dashboard Architecture

### 14.1 Dashboard views

```
Content Factory Dashboard
│
├── Overview
│   ├── Total lessons by status (donut chart)
│   ├── QA pass rate (7-day trend)
│   ├── AI cost today / this month
│   ├── Cost per lesson (rolling avg)
│   ├── Generation time (p50/p95)
│   ├── Queue depth per queue
│   └── Failed agents (last 24h)
│
├── Drill-down tree
│   Language (German)
│   └── Level (A1)
│       └── Unit 1
│           └── Lesson 1
│               ├── Versions (v1 PUBLISHED, v2 DRAFT)
│               ├── Agent Runs
│               │   ├── content_generator — SUCCEEDED 1.2s $0.04
│               │   ├── de_linguistic_qa  — SUCCEEDED 0.8s $0.01
│               │   └── ...
│               ├── QA Reports
│               │   ├── Linguistic QA: 0.91 PASS
│               │   ├── CEFR QA: 0.88 PASS
│               │   └── ...
│               └── Publication History
│                   ├── v1 PUBLISHED 2026-10-01
│                   └── v2 SUPERSEDED 2026-11-01
│
├── Human Review Queue
│   ├── Pending tasks (priority sorted)
│   ├── My assigned tasks
│   └── Resolved (audit trail)
│
├── Prompts
│   ├── All prompts + active versions
│   ├── Activation history
│   └── Regression impact preview
│
└── Cost Explorer
    ├── By date, domain, language, agent, model
    ├── Per-lesson breakdown
    └── Budget vs actual
```

### 14.2 Manual control surface

All admin actions write a `workflow_events` row with `actor = 'admin:{user_id}'`.

Actions available per lesson:

| Action | Allowed states |
|---|---|
| Generate | DRAFT, PLANNED |
| Regenerate | Any (creates new sub-jobs, does not change version) |
| Run QA | GENERATED or later |
| Run specific QA agent | GENERATED or later |
| Request revision | QA_FAILED, QA_PASSED |
| Approve | FINAL_REVIEW |
| Reject | FINAL_REVIEW |
| Publish | APPROVED |
| Unpublish | PUBLISHED |
| Rollback to version N | Any published version exists |
| Compare versions | Any two versions |

---

## 15. Automated Regression QA

### Trigger events

| Change | Affected scope |
|---|---|
| Prompt activated for `de_linguistic_qa` | All DE lessons with `qa_run.prompt_version < new_version` |
| Vocabulary item edited in shared glossary | Lessons that reference that item |
| Grammar rule changed | Lessons whose `grammar_targets` include that rule |
| Curriculum manifest updated | Lessons in affected units |
| `ai_model_config` changed | All lessons using that `model_config_key` |

### Process

```
1. regression/scan triggered (manual or automatic on prompt activation)
2. System queries affected lessons via content_dependencies + agent_runs
3. Creates regression_qa_runs record with affected_lessons list
4. Enqueues targeted QA jobs for affected lessons (not regeneration)
5. QA results compared to previous run
6. If score drops > 0.10 → flag for human review
7. Regression report available in dashboard
```

---

## 16. MVP Implementation Sequence

**Goal:** ONE exceptional German A1 lesson through the complete pipeline.

### Phase 0 — Infrastructure (Week 1–2)

- [ ] PostgreSQL schema (all tables from §5)
- [ ] BullMQ + Redis setup
- [ ] Workflow Orchestrator skeleton (state transitions only)
- [ ] Workflow audit log (append-only)
- [ ] Prompt Registry (insert initial prompt rows)
- [ ] Model Router (resolve from `ai_model_configs`)
- [ ] Cost Ledger (record every agent_run)
- [ ] Pipeline API skeleton (endpoints defined, agents stubbed)

### Phase 1 — Single Lesson: German A1 (Weeks 3–5)

Scope: `de → A1 → Unit 1 → Lesson 1`

- [ ] Language Profile: German (`de`)
- [ ] Lesson Planner Agent (German A1)
- [ ] Content Generator Agent
- [ ] Vocabulary Agent
- [ ] Grammar Agent
- [ ] Dialogue Agent
- [ ] Exercise Generator Agent
- [ ] Deterministic Validation Layer (all checks in §8)
- [ ] German Linguistic QA Agent
- [ ] CEFR QA Agent
- [ ] Pedagogy QA Agent
- [ ] Exercise QA Agent
- [ ] Consistency QA Agent (trivial for first lesson)
- [ ] QA Aggregator
- [ ] Editor/Revision Agent
- [ ] Final QA Gate
- [ ] Publisher Agent (write to content store)

**Milestone:** `de-a1-u01-l01` reaches `publication_status=PUBLISHED` with full lineage.

### Phase 2 — Batch + Second Language (Weeks 6–9)

- [ ] Curriculum Architect Agent
- [ ] Batch generation (full A1 unit — 5–8 lessons)
- [ ] Semantic dedup detection (pgvector)
- [ ] Language Profile: French (`fr`)
- [ ] French Linguistic QA Agent
- [ ] Smoke test: 3 French A1 lessons through pipeline
- [ ] Verify zero orchestrator/schema changes needed

### Phase 3 — Human Review + Publishing Controls (Weeks 10–12)

- [ ] Human Review Queue API + UI
- [ ] Rollback mechanism
- [ ] Learner progress compatibility (version migration logic)
- [ ] Admin dashboard (overview + drill-down)

### Phase 4 — Observability + Cost Controls (Weeks 13–14)

- [ ] Prometheus metrics (latency, cost, QA scores, retry rate)
- [ ] Grafana dashboards
- [ ] Budget enforcement (Redis counters + alerts)
- [ ] Circuit breaker
- [ ] Regression QA (prompt change trigger)

### Phase 5 — DSA Domain (Weeks 15–17)

- [ ] DSA Domain Plugin
- [ ] DSA content model (separate from Language content model)
- [ ] DSA-specific agents (no linguistic QA, different exercise types)
- [ ] Verify complete domain isolation at agent layer

---

## Summary

| Concern | Decision |
|---|---|
| Language coupling | Eliminated — Language Profile resolves all language-specific behaviour |
| Domain coupling | Eliminated — Domain Plugin isolates DSA/Language/AI Skills agents |
| Status model | Two independent fields: `content_status` + `publication_status` |
| Validation order | Deterministic first (free) → LLM QA only on valid content |
| QA execution | Parallel fan-out + deterministic Aggregator |
| Revision loops | `max_revision_attempts` → `HUMAN_REVIEW_REQUIRED` |
| Human escalation | First-class state, 7 explicit triggers |
| Prompt management | `agent_prompts` table, versioned, activatable, linked to every run |
| Model management | `ai_model_configs` + Model Router — no hard-coded providers |
| Immutability | `frozen=TRUE` enforced at DB role level, checksum verified |
| Rollback | `publication_status` field on both `lesson_versions` and `published_versions` |
| Learner compatibility | Stable `lesson_id` + versioned progress, in-progress learners unaffected |
| Dependency graph | `content_dependencies` table, queried by Planner and regression scanner |
| Semantic dedup | `content_embeddings` + pgvector, WARNING severity |
| Cost tracking | `cost_ledger` + per-lesson/per-agent budgets in Redis |
| Lineage | Mandatory JSONB on `lesson_versions` — Final Gate blocks if incomplete |
| MVP | ONE German A1 lesson end-to-end before any batch work |
