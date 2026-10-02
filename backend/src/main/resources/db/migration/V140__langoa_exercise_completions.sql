-- Track per-user per-exercise first-completion for anti-farming (M5.2)
-- Prevents awarding bonus XP/coins multiple times for the same exercise.

CREATE TABLE IF NOT EXISTS langoa_exercise_completions (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    lesson_id       UUID        NOT NULL,
    exercise_id     VARCHAR(64) NOT NULL,
    language_code   VARCHAR(10) NOT NULL,
    completed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_langoa_exercise_completions PRIMARY KEY (id),
    CONSTRAINT uq_exercise_completion UNIQUE (user_id, exercise_id, language_code)
);

CREATE INDEX IF NOT EXISTS idx_exercise_completions_user_lesson
    ON langoa_exercise_completions (user_id, lesson_id, language_code);
