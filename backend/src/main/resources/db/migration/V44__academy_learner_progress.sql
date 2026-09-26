-- ============================================================
-- Academy Phase 1 — Learner Progress Tables
--
-- learner_lesson_progress  : per-user, per-lesson state machine
-- learner_level_progress   : aggregate per-user, per-CEFR-level
--
-- Design invariants:
--   - user_id references users(id); lesson_id references cf_lessons(id)
--   - unique(user_id, lesson_id) enforced by constraint
--   - status transitions enforced by application (NOT NULL + CHECK)
--   - step_index = index within the ExperiencePlan steps array (0-based)
-- ============================================================

-- ─── learner_lesson_progress ──────────────────────────────────────────────
CREATE TABLE learner_lesson_progress (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id           UUID        NOT NULL REFERENCES cf_lessons(id) ON DELETE CASCADE,
    status              VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED'
        CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED','SKIPPED')),
    step_index          INT         NOT NULL DEFAULT 0
        CHECK (step_index >= 0),
    score               SMALLINT
        CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    last_interaction_at TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_learner_lesson UNIQUE (user_id, lesson_id)
);

CREATE INDEX idx_llp_user_id          ON learner_lesson_progress (user_id);
CREATE INDEX idx_llp_lesson_id        ON learner_lesson_progress (lesson_id);
CREATE INDEX idx_llp_user_status      ON learner_lesson_progress (user_id, status);

-- ─── learner_level_progress ───────────────────────────────────────────────
CREATE TABLE learner_level_progress (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    curriculum_id       UUID        NOT NULL REFERENCES cf_curricula(id) ON DELETE CASCADE,
    cefr_level          VARCHAR(4)  NOT NULL,           -- 'A1'…'C2'
    status              VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED'
        CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED')),
    lessons_total       INT         NOT NULL DEFAULT 0,
    lessons_completed   INT         NOT NULL DEFAULT 0,
    avg_score           NUMERIC(5,2),
    unlocked_at         TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_learner_level UNIQUE (user_id, curriculum_id, cefr_level),
    CONSTRAINT chk_llvp_counts CHECK (lessons_completed <= lessons_total)
);

CREATE INDEX idx_llvp_user_id         ON learner_level_progress (user_id);
CREATE INDEX idx_llvp_user_curriculum ON learner_level_progress (user_id, curriculum_id);
