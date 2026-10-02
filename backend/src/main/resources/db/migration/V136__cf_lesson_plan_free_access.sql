ALTER TABLE cf_curriculum_lesson_plans
    ADD COLUMN free_access BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE cf_curriculum_lesson_plans clp
SET free_access = TRUE
WHERE clp.id IN (
    SELECT id
    FROM cf_curriculum_lesson_plans
    WHERE curriculum_id = clp.curriculum_id
    ORDER BY position ASC
    LIMIT 10
);
