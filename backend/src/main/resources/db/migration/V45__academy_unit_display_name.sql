-- ============================================================
-- Academy Phase 1 — Curriculum unit display name on lesson plans
--
-- cf_curriculum_lesson_plans already has unit_id (nullable).
-- We add unit_display_name so the Academy API can render "Unit 3: At the Restaurant"
-- without an additional join to cf_curriculum_units at read time.
-- Backfill from cf_curriculum_units.label where the FK is set.
-- ============================================================

ALTER TABLE cf_curriculum_lesson_plans
    ADD COLUMN IF NOT EXISTS unit_display_name VARCHAR(200);

UPDATE cf_curriculum_lesson_plans lp
SET    unit_display_name = u.label
FROM   cf_curriculum_units u
WHERE  lp.unit_id = u.id
  AND  lp.unit_display_name IS NULL;

COMMENT ON COLUMN cf_curriculum_lesson_plans.unit_display_name
    IS 'Denormalised copy of cf_curriculum_units.label for zero-join Academy reads';
