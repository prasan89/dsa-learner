-- Extend problem_content with gold-standard learning columns.
-- Existing columns (intuition, brute_force, brute_time/space, optimal_approach,
-- optimal_time/space, pseudocode, java_solution, common_mistakes, recognition_note)
-- are kept as-is so existing data is preserved.

ALTER TABLE problem_content
    ADD COLUMN IF NOT EXISTS guided_reasoning        TEXT,
    ADD COLUMN IF NOT EXISTS solution                TEXT,
    ADD COLUMN IF NOT EXISTS pattern_recognition_clues TEXT,
    ADD COLUMN IF NOT EXISTS when_to_use             TEXT,
    ADD COLUMN IF NOT EXISTS when_not_to_use         TEXT,
    ADD COLUMN IF NOT EXISTS why_this_works          TEXT,
    ADD COLUMN IF NOT EXISTS invariant               TEXT,
    ADD COLUMN IF NOT EXISTS senior_variations       TEXT;

-- Add a human-readable label to problem_followups so the type column
-- supports the three content tracks: FOLLOWUP, VARIATION, SENIOR.
-- The existing CHECK already allows FOLLOWUP and SENIOR; add VARIATION.
ALTER TABLE problem_followups
    DROP CONSTRAINT IF EXISTS problem_followups_type_check;

ALTER TABLE problem_followups
    ADD CONSTRAINT problem_followups_type_check
        CHECK (type IN ('FOLLOWUP', 'VARIATION', 'SENIOR'));

-- Add a label column to hints so the UI can display the pedagogical name
-- (Concept / Direction / Algorithm) without hardcoding it in the frontend.
ALTER TABLE hints
    ADD COLUMN IF NOT EXISTS label VARCHAR(60);

UPDATE hints SET label = CASE level
    WHEN 1 THEN 'Concept'
    WHEN 2 THEN 'Direction'
    WHEN 3 THEN 'Algorithm'
END
WHERE label IS NULL;
