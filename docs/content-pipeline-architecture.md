# Academy Content Pipeline — Architecture

> **Status:** Proposed — awaiting implementation approval
> **Domain:** German A1–C2 (extensible to French, Korean, Spanish, Japanese, DSA, AI Skills)
> **Author:** Lead AI Architect
> **Date:** 2026-09-25

---

## 1. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          ACADEMY CONTENT PIPELINE                               │
│                                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────────────────┐  │
│  │  Admin UI /  │    │  Pipeline    │    │         Job Queue                │  │
│  │  CMS         │───▶│  API         │───▶│  (BullMQ / Redis Streams)        │  │
│  │              │    │  (REST+WS)   │    │                                  │  │
│  └──────────────┘    └──────┬───────┘    │  priority-high: final-qa         │  │
│                             │            │  priority-med:  generation        │  │
│                             ▼            │  priority-low:  improvement       │  │
│                    ┌────────────────┐    └───────────┬──────────────────────┘  │
│                    │  Workflow      │                 │                         │
│                    │  Orchestrator  │◀────────────────┘                         │
│                    │  (State Machine│                                           │
│                    │   + Scheduler) │                                           │
│                    └───────┬────────┘                                           │
│                            │                                                    │
│         ┌──────────────────┼──────────────────────────┐                        │
│         │                  │                          │                        │
│         ▼                  ▼                          ▼                        │
│  ┌─────────────┐  ┌────────────────┐       ┌──────────────────┐               │
│  │  PLANNING   │  │  GENERATION    │       │   QA CLUSTER     │               │
│  │  AGENTS     │  │  AGENTS        │       │                  │               │
│  │             │  │                │       │  Linguistic QA   │               │
│  │  Curriculum │  │  Content Gen   │       │  CEFR QA         │               │
│  │  Architect  │  │  Vocabulary    │       │  Pedagogy QA     │               │
│  │  Lesson     │  │  Grammar       │       │  Exercise QA     │               │
│  │  Planner    │  │  Dialogue      │       │  Consistency QA  │               │
│  └─────────────┘  │  Exercise Gen  │       │                  │               │
│                   │  Voice/Audio   │       │  [Deterministic] │               │
│                   └────────────────┘       │  [LLM-based]     │               │
│                                            └────────┬─────────┘               │
│                                                     │                         │
│                            ┌────────────────────────┘                         │
│                            │                                                   │
│                            ▼                                                   │
│                   ┌────────────────┐    ┌──────────────┐   ┌───────────────┐  │
│                   │  Editor /      │───▶│  Final QA    │──▶│  Publisher    │  │
│                   │  Revision      │    │  Gate        │   │  Agent        │  │
│                   │  Agent         │    │              │   └───────┬───────┘  │
│                   └────────────────┘    └──────────────┘           │          │
│                                                                     ▼          │
│  ┌──────────────────────────────────────────────────────┐  ┌──────────────┐  │
│  │                 HUMAN REVIEW QUEUE                    │  │  CDN /       │  │
│  │  (low-confidence, cultural flags, QA failures > 2x)  │  │  Content     │  │
│  └──────────────────────────────────────────────────────┘  │  Store       │  │
│                                                             └──────┬───────┘  │
│  ┌──────────────────────────────────────────────────────────────── │ ───────┐ │
│  │                    FEEDBACK LOOP                                │        │ │
│  │                                                                 ▼        │ │
│  │  Learner Analytics ──▶ Content Improvement Agent ──▶ New Draft Version  │ │
│  └──────────────────────────────────────────────────────────────────────────┘ │
│                                                                                 │
│  ════════════════════════════════════════════════════════════════════════════  │
│                        SHARED INFRASTRUCTURE                                   │
│                                                                                 │
│  PostgreSQL (content + workflow)   Redis (queue + cache + locks)               │
│  S3 (audio + media)                Prometheus + Grafana (observability)        │
│  Vector DB (semantic dedup)        Audit Log (append-only)                     │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Agent Responsibilities

Each agent owns exactly one concern. Cross-concern work is explicitly rejected at the orchestrator level.

| # | Agent | Single Responsibility | Inputs | Outputs |
|---|-------|-----------------------|--------|---------|
| 1 | **Curriculum Architect** | Define the complete CEFR-aligned scope for a language/level: units, themes, learning objectives, competency targets | Language, level (A1–C2), domain config | Curriculum manifest with unit tree |
| 2 | **Lesson Planner** | Decompose a curriculum unit into concrete lesson blueprints with skill focus, vocab targets, grammar points | Curriculum unit, lesson count, CEFR constraints | Lesson blueprints (structured, no prose) |
| 3 | **Content Generator** | Generate lesson prose: explanations, cultural notes, worked examples. No vocabulary/grammar tables, no exercises | Lesson blueprint | Lesson body content |
| 4 | **Vocabulary Agent** | Build the vocabulary list for a lesson — headwords, definitions, examples, frequency, domain tags | Lesson blueprint + content | Vocabulary set (structured) |
| 5 | **Grammar Agent** | Produce grammar explanations and rule tables for the lesson's grammar targets | Blueprint grammar targets | Grammar notes + rule tables |
| 6 | **Dialogue Agent** | Write natural, level-appropriate conversations that demonstrate lesson targets in context | Blueprint + vocabulary set + grammar notes | Dialogue transcripts with speaker roles |
| 7 | **Exercise Generator** | Create exercises that test lesson objectives: fill-in, MCQ, translation, reordering, listening cues | Full assembled lesson content | Exercise set with answer keys |
| 8 | **Voice/Audio Agent** | Produce audio metadata and TTS-ready text for vocabulary, dialogues, and pronunciation examples | Vocabulary set + dialogue transcripts | Audio manifest + SSML scripts |
| 9 | **German Linguistic QA** | Verify grammatical correctness, natural phrasing, register appropriateness in German text | All German text in lesson | Pass/fail per item + annotated issues |
| 10 | **CEFR QA** | Validate that vocabulary frequency, grammar complexity, and text length match the declared CEFR level | Full lesson + CEFR target | CEFR compliance score + violations |
| 11 | **Pedagogy QA** | Evaluate learning objective coverage, skill progression, scaffolding, cognitive load | Lesson blueprint + full content | Pedagogy score + gaps |
| 12 | **Exercise QA** | Verify every exercise has a valid answer key, no ambiguous distractors, correct references to lesson content | Exercise set + lesson content | Exercise quality score + specific failures |
| 13 | **Consistency QA** | Check cross-lesson consistency: terminology, recurring characters, dialect choices, style guide adherence | Lesson + course history | Consistency violations |
| 14 | **Editor/Revision Agent** | Apply targeted edits based on QA issue reports. Makes minimum changes; does not regenerate from scratch | QA issue reports + current content | Revised content with change log |
| 15 | **Final QA Gate** | Run all deterministic checks + aggregate QA scores. Issues a binary PASS/FAIL with full audit | Complete lesson package | Final gate result |
| 16 | **Publisher Agent** | Freeze a version, write to content store, trigger CDN invalidation, update learner-facing indexes | Approved lesson package | Published content record |
| 17 | **Learning Analytics Agent** | Aggregate learner performance signals per lesson: completion, scores, drop-off, difficulty ratings | Raw events from learner platform | Analytics report per lesson version |
| 18 | **Content Improvement Agent** | Propose targeted content revisions based on analytics signals, generating a new draft version | Analytics report + current published version | Improvement proposal → new draft |

---

## 3. Workflow State Machine

```
                      ┌──────────┐
                      │  DRAFT   │  (created by Curriculum Architect or admin)
                      └────┬─────┘
                           │ plan()
                           ▼
                      ┌──────────┐
                      │ PLANNED  │  (Lesson Planner blueprint attached)
                      └────┬─────┘
                           │ generate()
                           ▼
                     ┌────────────┐
                     │ GENERATING │  (parallel: Content + Vocab + Grammar +
                     └─────┬──────┘   Dialogue + Exercises + Audio)
                           │ generation_complete()
                           ▼
                     ┌───────────┐
                     │ GENERATED │  (all sub-agents succeeded)
                     └─────┬─────┘
                           │ submit_for_qa()
                           ▼
                    ┌────────────┐
                    │ QA_PENDING │◀─────────────────────────────┐
                    └─────┬──────┘                              │
                          │                                     │
               ┌──────────┴──────────┐                         │
               │ (parallel QA agents)│                         │
               ▼                     ▼                         │
          all pass              any fail                       │
               │                     │                         │
               ▼                     ▼                         │
         ┌──────────┐        ┌────────────┐                    │
         │QA_PASSED │        │ QA_FAILED  │                    │
         └────┬─────┘        └─────┬──────┘                    │
              │                    │ route_to_revision()       │
              │                    ▼                           │
              │             ┌──────────┐                       │
              │             │ REVISION │ (Editor Agent applies │
              │             └─────┬────┘  targeted fixes)      │
              │                   │ revision_complete()        │
              │                   └───────────────────────────►┘
              │                         (back to QA_PENDING)
              │
              │ submit_for_final_review()
              ▼
       ┌──────────────┐
       │ FINAL_REVIEW │  (human optional, or auto if confidence ≥ threshold)
       └──────┬───────┘
              │
     ┌────────┴────────┐
     │                 │
     ▼                 ▼
 approved           rejected
     │                 │
     ▼                 └──────────► REVISION (human-annotated)
┌──────────┐
│ APPROVED │
└────┬─────┘
     │ publish()
     ▼
┌───────────┐
│ PUBLISHED │  (immutable — further changes create v+1 DRAFT)
└─────┬─────┘
      │
      ▼
 [analytics loop]
      │
      ▼
┌──────────────────┐
│ IMPROVEMENT_DRAFT│  (new version DRAFT → same pipeline)
└──────────────────┘
```

> **Revision cap:** After 3 QA_FAILED → QA_PENDING cycles, the lesson is automatically escalated to `HUMAN_REVIEW_REQUIRED` before further revision is permitted.

### State Transition Table

| From | To | Trigger | Guard |
|---|---|---|---|
| DRAFT | PLANNED | plan() | blueprint valid + no duplicate unit |
| PLANNED | GENERATING | generate() | all sub-agent slots available |
| GENERATING | GENERATED | all sub-jobs complete | no sub-job in ERROR state |
| GENERATED | QA_PENDING | submit_for_qa() | deterministic pre-checks pass |
| QA_PENDING | QA_PASSED | all QA agents pass | min score thresholds met |
| QA_PENDING | QA_FAILED | any QA agent fails | — |
| QA_FAILED | REVISION | route_to_revision() | revision_count < 3 |
| QA_FAILED | HUMAN_REVIEW_REQUIRED | — | revision_count ≥ 3 |
| REVISION | QA_PENDING | revision_complete() | diff log non-empty |
| QA_PASSED | FINAL_REVIEW | submit_for_final_review() | final_qa_gate passes |
| FINAL_REVIEW | APPROVED | approve() | human sign-off or auto-approve |
| FINAL_REVIEW | REVISION | reject() | human annotated feedback attached |
| APPROVED | PUBLISHED | publish() | publisher agent succeeds |
| PUBLISHED | DRAFT (v+1) | create_improvement() | analytics report attached |

---

## 4. Database Schema

```sql
-- ─── Language & Curriculum ─────────────────────────────────────────────────

CREATE TABLE languages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(10)  NOT NULL UNIQUE,  -- 'de', 'fr', 'ko'
    name         VARCHAR(100) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    config       JSONB        NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE curriculum_manifests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_id     UUID NOT NULL REFERENCES languages(id),
    cefr_level      VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1','A2','B1','B2','C1','C2')),
    version         INT  NOT NULL DEFAULT 1,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    manifest        JSONB NOT NULL,         -- full unit tree
    agent_run_id    UUID,
    created_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (language_id, cefr_level, version)
);

CREATE TABLE curriculum_units (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manifest_id         UUID NOT NULL REFERENCES curriculum_manifests(id),
    unit_number         INT  NOT NULL,
    theme               VARCHAR(255) NOT NULL,
    learning_objectives JSONB NOT NULL,
    cefr_level          VARCHAR(2) NOT NULL,
    skill_focus         VARCHAR(50)[],
    prerequisite_unit_ids UUID[],
    display_order       INT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (manifest_id, unit_number)
);

-- ─── Lessons ───────────────────────────────────────────────────────────────

CREATE TYPE lesson_status AS ENUM (
    'DRAFT', 'PLANNED', 'GENERATING', 'GENERATED',
    'QA_PENDING', 'QA_FAILED', 'REVISION',
    'QA_PASSED', 'FINAL_REVIEW', 'APPROVED',
    'PUBLISHED', 'ARCHIVED', 'HUMAN_REVIEW_REQUIRED'
);

CREATE TABLE lessons (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id             UUID NOT NULL REFERENCES curriculum_units(id),
    lesson_number       INT  NOT NULL,
    title               VARCHAR(255),
    cefr_level          VARCHAR(2) NOT NULL,
    skill_focus         VARCHAR(50)[],
    status              lesson_status NOT NULL DEFAULT 'DRAFT',
    current_version     INT NOT NULL DEFAULT 1,
    revision_count      INT NOT NULL DEFAULT 0,
    human_review_flag   BOOLEAN NOT NULL DEFAULT FALSE,
    human_review_reason TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (unit_id, lesson_number)
);

-- Immutable version snapshots — never UPDATE, only INSERT
CREATE TABLE lesson_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id),
    version         INT  NOT NULL,
    status          lesson_status NOT NULL,
    blueprint       JSONB,
    content         JSONB,
    vocabulary      JSONB,
    grammar         JSONB,
    dialogues       JSONB,
    exercises       JSONB,
    audio_manifest  JSONB,
    published_at    TIMESTAMPTZ,
    frozen          BOOLEAN NOT NULL DEFAULT FALSE,  -- TRUE once published
    checksum        VARCHAR(64),    -- SHA-256 of content blob
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, version)
);

CREATE INDEX idx_lesson_versions_published
    ON lesson_versions (lesson_id, version DESC)
    WHERE frozen = TRUE;

-- ─── Agent Runs ────────────────────────────────────────────────────────────

CREATE TABLE agent_runs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id         UUID NOT NULL REFERENCES lessons(id),
    lesson_version    INT  NOT NULL,
    agent_type        VARCHAR(60) NOT NULL,
    job_id            UUID,
    status            VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    input_hash        VARCHAR(64),    -- SHA-256 of input payload (idempotency)
    output            JSONB,
    confidence        NUMERIC(4,3),
    issues            JSONB,
    recommendations   JSONB,
    model             VARCHAR(80),
    prompt_tokens     INT,
    completion_tokens INT,
    cost_usd          NUMERIC(10,6),
    duration_ms       INT,
    retry_count       INT NOT NULL DEFAULT 0,
    error_message     TEXT,
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_runs_lesson ON agent_runs (lesson_id, lesson_version, agent_type);
CREATE INDEX idx_agent_runs_status ON agent_runs (status) WHERE status IN ('QUEUED','RUNNING','RETRYING');

-- ─── QA Records ────────────────────────────────────────────────────────────

CREATE TABLE qa_results (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id),
    lesson_version  INT  NOT NULL,
    agent_run_id    UUID NOT NULL REFERENCES agent_runs(id),
    qa_agent        VARCHAR(60) NOT NULL,
    passed          BOOLEAN NOT NULL,
    score           NUMERIC(4,3),
    gate_type       VARCHAR(20) NOT NULL,   -- 'DETERMINISTIC' | 'LLM'
    issues          JSONB NOT NULL DEFAULT '[]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Workflow Audit Log (append-only) ──────────────────────────────────────

CREATE TABLE workflow_events (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       UUID NOT NULL REFERENCES lessons(id),
    lesson_version  INT,
    from_status     lesson_status,
    to_status       lesson_status NOT NULL,
    trigger         VARCHAR(60) NOT NULL,
    actor           VARCHAR(100),
    agent_run_id    UUID,
    metadata        JSONB,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workflow_events_lesson ON workflow_events (lesson_id, occurred_at DESC);

-- ─── Human Review Queue ────────────────────────────────────────────────────

CREATE TABLE human_review_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id),
    lesson_version  INT  NOT NULL,
    reason          VARCHAR(60) NOT NULL,
    priority        SMALLINT NOT NULL DEFAULT 2,
    assigned_to     UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolution      VARCHAR(20),
    reviewer_notes  TEXT,
    qa_summary      JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ
);

-- ─── Published Content Index ───────────────────────────────────────────────

CREATE TABLE published_lessons (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id),
    lesson_version  INT  NOT NULL,
    language_code   VARCHAR(10) NOT NULL,
    cefr_level      VARCHAR(2)  NOT NULL,
    unit_number     INT NOT NULL,
    lesson_number   INT NOT NULL,
    content_url     VARCHAR(500),
    audio_urls      JSONB,
    published_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    superseded_by   INT,
    FOREIGN KEY (lesson_id, lesson_version) REFERENCES lesson_versions(lesson_id, version)
);

-- ─── Analytics ─────────────────────────────────────────────────────────────

CREATE TABLE lesson_analytics (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id         UUID NOT NULL REFERENCES lessons(id),
    lesson_version    INT  NOT NULL,
    period_start      DATE NOT NULL,
    period_end        DATE NOT NULL,
    learner_count     INT,
    completion_rate   NUMERIC(4,3),
    avg_score         NUMERIC(4,3),
    avg_time_sec      INT,
    drop_off_rate     NUMERIC(4,3),
    difficulty_rating NUMERIC(3,2),
    error_patterns    JSONB,
    computed_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, lesson_version, period_start)
);
```

---

## 5. Agent Input/Output Contracts

Every agent receives and returns this envelope:

```typescript
// ── Universal envelope ────────────────────────────────────────────────────

interface AgentInput<T> {
  agent_run_id:    string;           // UUID, pre-assigned by orchestrator
  lesson_id:       string;
  lesson_version:  number;
  input_hash:      string;           // SHA-256 of payload — for idempotency
  payload:         T;
  context: {
    language_code: string;           // 'de'
    cefr_level:    CEFRLevel;        // 'A1' | 'A2' | ... | 'C2'
    domain:        string;           // 'german' | 'dsa' | 'french'
    retry_count:   number;
    max_retries:   number;
  };
}

interface AgentOutput<T> {
  agent_run_id:    string;
  status:          'SUCCEEDED' | 'FAILED' | 'PARTIAL';
  output:          T | null;
  confidence:      number;           // 0.0–1.0
  issues:          Issue[];
  recommendations: string[];
  metadata: {
    model:             string;
    prompt_tokens:     number;
    completion_tokens: number;
    cost_usd:          number;
    duration_ms:       number;
    version:           string;       // agent code version e.g. "1.3.0"
  };
}

interface Issue {
  code:        string;               // e.g. "VOCAB_MISSING_GENDER"
  severity:    'ERROR' | 'WARNING' | 'INFO';
  field:       string;               // JSON path: "vocabulary[3].gender"
  message:     string;
  suggestion?: string;
}
```

### Per-Agent Payload Contracts

```typescript
// Agent 1 — Curriculum Architect
type CurriculumInput = {
  language: string;
  cefr_level: CEFRLevel;
  target_unit_count: number;
  domain_config: DomainConfig;
};
type CurriculumOutput = {
  manifest_id: string;
  units: CurriculumUnit[];
  total_lessons_estimate: number;
};

// Agent 2 — Lesson Planner
type LessonPlannerInput = {
  unit: CurriculumUnit;
  lesson_number: number;
  lesson_count_in_unit: number;
  prior_lesson_summaries: LessonSummary[];
};
type LessonPlannerOutput = {
  blueprint: LessonBlueprint;
  // skill_focus, vocab_targets, grammar_targets,
  // exercise_types, difficulty_notes, duration_minutes
};

// Agent 3 — Content Generator
type ContentGenInput = {
  blueprint: LessonBlueprint;
  style_guide: StyleGuideRef;
};
type ContentGenOutput = {
  introduction:    string;
  cultural_note:   string | null;
  explanation:     string;
  worked_examples: WorkedExample[];
};

// Agent 4 — Vocabulary Agent
type VocabularyInput = {
  blueprint: LessonBlueprint;
  lesson_content: ContentGenOutput;
};
type VocabularyOutput = {
  vocabulary: VocabItem[];
  // headword, article, plural, definition,
  // example_sentence, frequency_band, domain_tags, cefr_band
};

// Agent 5 — Grammar Agent
type GrammarInput = {
  grammar_targets: GrammarTarget[];
  cefr_level: CEFRLevel;
};
type GrammarOutput = {
  grammar_notes: GrammarNote[];  // rule, table, exceptions, common_errors
};

// Agent 6 — Dialogue Agent
type DialogueInput = {
  blueprint: LessonBlueprint;
  vocabulary: VocabItem[];
  grammar_notes: GrammarNote[];
};
type DialogueOutput = {
  dialogues: Dialogue[];  // turns[], speakers[], context, setting
};

// Agent 7 — Exercise Generator
type ExerciseInput = {
  blueprint: LessonBlueprint;
  content: ContentGenOutput;
  vocabulary: VocabItem[];
  grammar_notes: GrammarNote[];
  dialogues: Dialogue[];
};
type ExerciseOutput = {
  exercises: Exercise[];
  // type, prompt, options?, answer_key, points, skill_tested, difficulty
};

// Agent 8 — Voice/Audio Agent
type AudioInput = {
  vocabulary: VocabItem[];
  dialogues: Dialogue[];
  pronunciation_targets: string[];
};
type AudioOutput = {
  audio_manifest: AudioManifest;
  ssml_scripts: SSMLScript[];
};

// Agents 9–13 — QA Agents (shared shape)
type QAInput = {
  lesson_package: LessonPackage;
  qa_scope: string[];
  prior_qa_results?: QAResult[];
};
type QAOutput = {
  passed:        boolean;
  score:         number;            // 0.0–1.0
  gate_type:     'DETERMINISTIC' | 'LLM';
  issues:        Issue[];
  passed_checks: string[];
  failed_checks: string[];
};

// Agent 14 — Editor/Revision Agent
type RevisionInput = {
  current_version: LessonPackage;
  qa_issues: Issue[];
  revision_instructions: string[];
};
type RevisionOutput = {
  revised_package:   LessonPackage;
  change_log:        ChangeLogEntry[];   // field, before, after, reason
  issues_addressed:  string[];
  issues_deferred:   string[];
};

// Agent 15 — Final QA Gate (no LLM)
type FinalGateInput = {
  lesson_package: LessonPackage;
  qa_results: QAResult[];
  revision_count: number;
};
type FinalGateOutput = {
  passed:               boolean;
  deterministic_checks: CheckResult[];
  qa_score_summary:     { agent: string; score: number; passed: boolean }[];
  blockers:             Issue[];
  overall_confidence:   number;
};

// Agent 16 — Publisher Agent
type PublisherInput = {
  approved_package: LessonPackage;
  lesson_version:   number;
  publish_config:   { cdn_prefix: string; index_rebuild: boolean };
};
type PublisherOutput = {
  published_record_id: string;
  content_url:         string;
  audio_urls:          Record<string, string>;
  published_at:        string;
};

// Agent 17 — Analytics Agent
type AnalyticsInput = {
  lesson_id:      string;
  lesson_version: number;
  period:         { start: string; end: string };
  raw_events:     LearnerEvent[];
};
type AnalyticsOutput = {
  report:               AnalyticsReport;
  improvement_signals:  ImprovementSignal[];
};

// Agent 18 — Content Improvement Agent
type ImprovementInput = {
  current_published:   LessonPackage;
  analytics_report:    AnalyticsReport;
  improvement_signals: ImprovementSignal[];
};
type ImprovementOutput = {
  proposed_changes:    ProposedChange[];
  rationale:           string;
  expected_impact:     string;
  new_version_draft:   Partial<LessonPackage>;
};
```

---

## 6. API Design

```
Base: /api/v1/pipeline

── Curriculum ──────────────────────────────────────────────────────────────
POST   /curriculum                       Create new curriculum manifest job
GET    /curriculum/:id                   Get manifest + status
GET    /curriculum/:id/units             List units in manifest
PUT    /curriculum/:id/units/:uid        Update unit (pre-generation only)

── Lessons ─────────────────────────────────────────────────────────────────
POST   /lessons                          Create lesson (DRAFT)
GET    /lessons/:id                      Get lesson + current version summary
GET    /lessons/:id/versions             List all versions
GET    /lessons/:id/versions/:v          Get full content of specific version
DELETE /lessons/:id                      Soft-delete (ARCHIVED)

── Workflow Triggers ────────────────────────────────────────────────────────
POST   /lessons/:id/plan                 Trigger → PLANNED
POST   /lessons/:id/generate             Trigger → GENERATING
POST   /lessons/:id/qa                   Trigger → QA_PENDING
POST   /lessons/:id/approve              Trigger → APPROVED
POST   /lessons/:id/reject               Trigger → REVISION  {body: {notes}}
POST   /lessons/:id/publish              Trigger → PUBLISHED
POST   /lessons/:id/improve              Create v+1 DRAFT from analytics

── Agent Runs ───────────────────────────────────────────────────────────────
GET    /lessons/:id/runs                 All agent runs for lesson
GET    /lessons/:id/runs/:run_id         Single run detail + full output
GET    /runs?status=FAILED               Global run query (ops dashboard)

── QA ───────────────────────────────────────────────────────────────────────
GET    /lessons/:id/qa-results           QA results for current version
GET    /lessons/:id/qa-results/:v        QA results for specific version
POST   /lessons/:id/qa/override          Human override a QA failure {reason}

── Human Review ─────────────────────────────────────────────────────────────
GET    /review-queue                     List pending human review tasks
GET    /review-queue/:task_id            Get task + full context
POST   /review-queue/:task_id/assign     Assign to reviewer
POST   /review-queue/:task_id/resolve    {resolution, notes}

── Publishing ───────────────────────────────────────────────────────────────
GET    /published                        List all published lessons
GET    /published/:lesson_id             Latest published version
GET    /published/:lesson_id/:v          Specific published version

── Analytics ────────────────────────────────────────────────────────────────
POST   /analytics/ingest                 Batch learner events
GET    /analytics/:lesson_id             Aggregated report
POST   /analytics/:lesson_id/trigger-improvement

── Observability ────────────────────────────────────────────────────────────
GET    /health
GET    /metrics                          Prometheus scrape endpoint
GET    /audit/:lesson_id                 Full workflow event log
```

**WebSocket:** `ws /api/v1/pipeline/lessons/:id/stream`
Pushes state transition events to CMS UI in real time.

**All write endpoints require:**
- `Authorization: Bearer <service-token>` for agent calls
- `Authorization: Bearer <user-jwt>` for CMS/admin calls
- `Idempotency-Key: <uuid>` header on all POSTs

---

## 7. Queue / Job Architecture

**Technology:** BullMQ on Redis (or Redis Streams for at-least-once guarantees)

### Queue Topology

| Queue | Concurrency | Notes |
|---|---|---|
| `pipeline:planning` | 5 | |
| `pipeline:generation` | 10 | Sub-divided per sub-agent |
| `pipeline:qa` | 8 | QA agents run in parallel |
| `pipeline:revision` | 5 | |
| `pipeline:final-gate` | 3 | |
| `pipeline:publishing` | 2 | |
| `pipeline:analytics` | 3 | |
| `pipeline:improvement` | 2 | |
| `pipeline:human-review-notify` | 1 | Notifications only |

### Per-Job Payload

```json
{
  "job_id":         "uuid",
  "lesson_id":      "uuid",
  "lesson_version": 1,
  "agent_type":     "content_generator",
  "input_hash":     "sha256hex",
  "priority":       2,
  "delay_ms":       0,
  "attempt":        1
}
```

### Idempotency

Before dequeuing a job, the worker checks `agent_runs` for an existing record with matching `(lesson_id, lesson_version, agent_type, input_hash)` and `status=SUCCEEDED`. If found, returns cached output — no LLM call made.

### Retry Policy

| Queue | Attempts | Backoff |
|---|---|---|
| planning | 3 | 5s / 25s / 125s |
| generation | 4 | 10s / 60s / 300s / 900s |
| qa | 3 | 5s / 30s / 150s |
| revision | 3 | 10s / 60s / 300s |
| publishing | 5 | 2s / 10s / 30s / 60s / 120s |

### Dead-Letter Queue

`pipeline:dlq` — any job exceeding max retries lands here. On DLQ insertion: workflow event logged, `human_review_tasks` record created, lesson status set to `HUMAN_REVIEW_REQUIRED`.

### Concurrency Guard

Redis `SETNX` lock keyed by `(lesson_id, agent_type)` with TTL = 2× max agent timeout. Prevents duplicate concurrent runs for the same lesson/agent pair.

---

## 8. QA Architecture

Two distinct layers — never mixed within a single agent.

### Layer 1 — Deterministic (fast, no LLM)

Runs first. If any `ERROR`-severity issue found, skip LLM QA entirely.

| Check | Description |
|---|---|
| `SCHEMA` | All required JSON fields present and typed correctly |
| `REFS` | Exercise answer references point to real vocabulary IDs |
| `IDS` | No duplicate IDs within vocabulary, exercises, dialogues |
| `CEFR_VALID` | Declared CEFR level is a valid enum value |
| `AUDIO_PRESENT` | Every vocab item with `audio=true` has an audio_manifest entry |
| `ANSWERS` | Every exercise has a non-empty `answer_key` |
| `TRANSLATIONS` | All vocabulary items have at least one target translation |
| `WORD_COUNT` | Lesson body within min/max bounds for CEFR level |
| `CHAR_ENCODING` | No malformed Unicode; ä ö ü ß present where expected |
| `DUPLICATE_EXID` | No exercise IDs repeated across the lesson |
| `BROKEN_REFS` | All cross-lesson prerequisite IDs exist in `curriculum_units` |

### Layer 2 — LLM-based (parallel specialist agents)

Runs only if Layer 1 passes.

| Agent | Checks |
|---|---|
| German Linguistic QA | Naturalness, grammatical correctness, register |
| CEFR QA | Difficulty calibration, vocabulary frequency match |
| Pedagogy QA | Objective coverage, scaffolding quality |
| Exercise QA | Distractor quality, ambiguity, skill alignment |
| Consistency QA | Cross-lesson style, character/terminology continuity |

### QA Score Thresholds (configurable per language/level)

| Agent | MIN_PASS | AUTO_APPROVE |
|---|---|---|
| Linguistic QA | 0.80 | 0.95 |
| CEFR QA | 0.85 | 0.95 |
| Pedagogy QA | 0.75 | 0.90 |
| Exercise QA | 0.80 | 0.90 |
| Consistency QA | 0.70 | 0.85 |
| **Combined (weighted)** | **0.80** | **0.92** |

- Any agent < `MIN_PASS` → `QA_FAILED`
- All agents ≥ `MIN_PASS` but combined < `AUTO_APPROVE` → `FINAL_REVIEW` (human required)
- Combined ≥ `AUTO_APPROVE` → auto-approve, no human needed

### QA Issue Severity Routing

| Severity | Impact |
|---|---|
| `ERROR` | Always triggers `QA_FAILED` |
| `WARNING` | Contributes to score reduction; 3+ warnings = `QA_FAILED` |
| `INFO` | Recorded, no impact on pass/fail |

---

## 9. Versioning Architecture

### Immutability Contract

A `lesson_version` row with `frozen=TRUE` must never be `UPDATE`d. The publisher sets `frozen=TRUE` atomically with writing to the CDN. The checksum (SHA-256 of content JSONB) is stored at freeze time and verified on every read from the CDN-facing API.

### Version Lineage

```
lesson v1 → PUBLISHED (frozen)
lesson v2 → PUBLISHED (frozen, supersedes v1)
            v1 remains readable — used by learners mid-lesson
lesson v3 → DRAFT (analytics-triggered improvement)
```

### Content Rollback

Any prior frozen version can be re-published by setting `published_lessons.superseded_by = NULL` and re-running the publisher agent. This creates an audit event without touching the frozen row.

### Draft Isolation

Draft versions are never served to learners. The learner-facing API queries only:

```sql
SELECT * FROM published_lessons
WHERE lesson_id = $1
ORDER BY lesson_version DESC
LIMIT 1;
```

---

## 10. Error / Retry Strategy

### Failure Classes

| Class | Description | Action |
|---|---|---|
| **A — Transient** | Network timeouts, rate limits, LLM 429/503 | Exponential backoff per queue policy; logged as `RETRYING` |
| **B — Content quality** | Valid JSON but QA scores below threshold | Not an error; triggers normal `QA_FAILED → REVISION` flow |
| **C — Structural** | Invalid JSON, missing required fields, schema violation | `FAILED` status; alert ops; after max retries → DLQ + `HUMAN_REVIEW_REQUIRED` |
| **D — Idempotency replay** | Same `(lesson_id, version, agent_type, input_hash)` already `SUCCEEDED` | Return cached output immediately, no job executed |

### Circuit Breaker

If an agent type has `failure_rate > 50%` over a 5-minute window, the circuit opens: new jobs for that `agent_type` are queued but not dispatched, and an ops alert fires. Resets after 10 minutes or manual override.

### Poison Pill Detection

A lesson that has consumed > 10 total `agent_runs` across all agents without reaching `QA_PASSED` is flagged for human review automatically.

---

## 11. Security Model

### Identity Layers

| Layer | Auth Method | Access |
|---|---|---|
| Human users (CMS/admin) | JWT, scoped roles: `ADMIN \| EDITOR \| REVIEWER` | Full pipeline read/write per role |
| Orchestrator service | mTLS client cert + service token | All pipeline state mutations |
| Agent workers | Per-agent API keys (Vault/AWS SSM) | Own queue + own DB writes only |
| Learner platform | Read-only API key | Published content only |

### Data Isolation

Published content (learner-facing) is served from a separate read replica with no write access and no access to draft/QA data.

### Secrets

- LLM API keys are per-agent-type — never shared between agents
- Rotation every 90 days; emergency revocation via Vault
- Keys stored in AWS SSM Parameter Store or HashiCorp Vault — never in env vars in code

### Audit Requirements

`workflow_events` is append-only. `INSERT` only role for pipeline service. No `DELETE` or `UPDATE` via application — only via a separate break-glass admin role with MFA + audit trail.

### Content Safety

All generated text passes through a synchronous content filter before leaving the Content Generator (not a separate agent — fast, deterministic). Flagged content → immediate `HUMAN_REVIEW_REQUIRED`, never advances automatically.

### Rate Limiting

- Pipeline API: 100 req/min per service token
- LLM agents: per-model token budget enforced at the job queue level (`token_budget_remaining` in Redis, decremented per run, refilled hourly)

---

## 12. Cost-Control Strategy

### 1. Idempotency-first

Every agent checks `input_hash` before calling an LLM. Repeated runs on unchanged input = zero cost.

### 2. Model Tiering

| Agent Group | Model |
|---|---|
| Planning (Curriculum Architect, Lesson Planner) | Claude Sonnet |
| Generation (Content, Vocab, Grammar, Dialogue, Exercises) | Claude Sonnet |
| QA agents | Claude Haiku |
| Deterministic checks / Final QA Gate | No LLM |
| Analytics Agent | Claude Haiku |

### 3. Per-Lesson Token Budget

Enforced in Redis per `(lesson_id, version)` across all agents. Default budget: **$0.50 per lesson**. Budget exceeded → alert + pause generation + require manual override.

### 4. Prompt Optimization

- Agents receive structured JSON input, not free-form prose
- System prompts are cached (Anthropic prompt caching)
- Output schemas are declared — models constrained to JSON output only

### 5. QA Short-Circuit

Deterministic checks run before any LLM QA. If they fail, LLM QA is skipped entirely. Typical A1 lesson failing field validation costs ~$0.00 vs ~$0.08.

### 6. Budget Tracking

- `cost_usd` stored per `agent_run`
- Monthly budget alerts at 70% and 90% spend
- Per-language, per-level cost dashboards in Grafana

**Estimated cost per published A1 lesson:** ~$0.35–$0.55 (with model tiering applied)

---

## 13. Scaling Strategy

### Stateless Agents

All agent workers are stateless — state lives in PostgreSQL + Redis. Workers scale horizontally; add pods, queue drains faster.

### Queue-Based Backpressure

Generation bursts (e.g. 50 A1 lessons at once) are absorbed by the queue. Workers process at controlled concurrency. No thundering herd against the LLM API.

### Database

- Read replicas for learner-facing content API
- Pipeline writes go to primary only
- `lesson_versions` and `agent_runs` partitioned by `created_at` (monthly)
- Old partitions cold-archived to S3 after 6 months

### CDN

Published content is edge-cached. `Cache-Control: immutable` for frozen versions (version-stamped URLs). Zero DB load for learner reads.

### Multi-Language / Multi-Domain

Language is a first-class field on every record. Agent worker pools can be language-specialized (German QA agent has German-specific prompts) or shared (Exercise Generator is domain-agnostic).

**Adding a new language = new curriculum manifest + language-specific QA agent config. Zero code changes to the orchestrator or schema.**

### Domain Config for DSA / AI Skills

The `languages.config` JSONB field specifies per-domain overrides:
- Which agents are active (DSA doesn't need Linguistic QA in German)
- Custom QA thresholds
- Custom exercise types
- Content model extensions

---

## 14. Implementation Phases

### Phase 1 — Foundation (Weeks 1–4)

- [ ] PostgreSQL schema (all tables)
- [ ] BullMQ + Redis setup
- [ ] Orchestrator state machine (transitions only, agents stubbed)
- [ ] Workflow event log + audit API
- [ ] Pipeline REST API skeleton
- [ ] Admin CMS: lesson list, status view, manual state transitions
- [ ] Deterministic QA layer (all 11 checks, no LLM)

### Phase 2 — Generation Pipeline (Weeks 5–8)

- [ ] Curriculum Architect Agent
- [ ] Lesson Planner Agent
- [ ] Content Generator Agent
- [ ] Vocabulary Agent
- [ ] Grammar Agent
- [ ] Dialogue Agent
- [ ] Exercise Generator Agent
- [ ] Idempotency + input_hash enforcement
- [ ] Cost tracking per agent_run

### Phase 3 — QA Pipeline (Weeks 9–12)

- [ ] German Linguistic QA Agent
- [ ] CEFR QA Agent
- [ ] Pedagogy QA Agent
- [ ] Exercise QA Agent
- [ ] Consistency QA Agent
- [ ] QA score aggregation + thresholds
- [ ] QA_FAILED → REVISION routing
- [ ] Editor/Revision Agent

### Phase 4 — Publishing + Review (Weeks 13–15)

- [ ] Final QA Gate Agent
- [ ] Publisher Agent (CDN write + index)
- [ ] Human Review Queue UI
- [ ] Version immutability enforcement + checksum verification
- [ ] Rollback mechanism

### Phase 5 — Feedback Loop (Weeks 16–18)

- [ ] Learner event ingestion API
- [ ] Analytics Agent
- [ ] Content Improvement Agent
- [ ] Improvement → new version DRAFT flow
- [ ] A/B version serving (v1 to half, v2 to other half)

### Phase 6 — Observability + Hardening (Weeks 19–20)

- [ ] Prometheus metrics for all agents (latency, cost, retries, QA scores)
- [ ] Grafana dashboards
- [ ] Circuit breaker implementation
- [ ] Poison pill detection
- [ ] Budget alerting
- [ ] Load testing (50-lesson batch)
- [ ] Security audit (secret rotation, mTLS, append-only audit enforcement)

### Phase 7 — Second Language: French (Weeks 21–22)

- [ ] French curriculum manifest
- [ ] French Linguistic QA Agent (swap German-specific prompts)
- [ ] Smoke test: generate + publish 5 A1 French lessons
- [ ] Verify zero code changes required in orchestrator or schema

---

## Summary

| Metric | Value |
|---|---|
| Schema objects | 11 tables, 1 enum, 7 indexes |
| Total agents | 18 |
| Estimated cost per A1 lesson | $0.35–$0.55 |
| Estimated Phase 1–6 duration | 20 weeks (2-engineer backend team) |
| Languages supportable without code change | Unlimited (config-driven) |
