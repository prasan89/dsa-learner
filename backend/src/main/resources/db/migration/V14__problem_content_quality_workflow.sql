-- V14: Content quality workflow for problem_content.
--
-- Adds content_status with a DRAFT→CONTENT_REVIEW→TECHNICAL_VALIDATION→READY workflow
-- and a quality_flags JSONB column that tracks which checklist items are populated.
-- The flags are computed automatically by a trigger so they are always in sync.

ALTER TABLE problem_content
    ADD COLUMN IF NOT EXISTS content_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
        CONSTRAINT problem_content_status_check
        CHECK (content_status IN ('DRAFT','CONTENT_REVIEW','TECHNICAL_VALIDATION','READY')),
    ADD COLUMN IF NOT EXISTS quality_flags JSONB;

-- Populate quality_flags as a computed snapshot for every existing row
UPDATE problem_content SET quality_flags = (
    SELECT jsonb_build_object(
        'has_recognition_note',          recognition_note IS NOT NULL AND recognition_note <> '',
        'has_pattern_recognition_clues', pattern_recognition_clues IS NOT NULL AND pattern_recognition_clues <> '',
        'has_when_to_use',               when_to_use IS NOT NULL AND when_to_use <> '',
        'has_when_not_to_use',           when_not_to_use IS NOT NULL AND when_not_to_use <> '',
        'has_intuition',                 intuition IS NOT NULL AND intuition <> '',
        'has_guided_reasoning',          guided_reasoning IS NOT NULL AND guided_reasoning <> '',
        'has_solution',                  solution IS NOT NULL AND solution <> '',
        'has_brute_force',               brute_force IS NOT NULL AND brute_force <> '',
        'has_brute_complexity',          brute_time IS NOT NULL AND brute_space IS NOT NULL,
        'has_optimal_approach',          optimal_approach IS NOT NULL AND optimal_approach <> '',
        'has_optimal_complexity',        optimal_time IS NOT NULL AND optimal_space IS NOT NULL,
        'has_pseudocode',                pseudocode IS NOT NULL AND pseudocode <> '',
        'has_why_this_works',            why_this_works IS NOT NULL AND why_this_works <> '',
        'has_invariant',                 invariant IS NOT NULL AND invariant <> '',
        'has_common_mistakes',           common_mistakes IS NOT NULL AND common_mistakes <> '',
        'has_senior_variations',         senior_variations IS NOT NULL AND senior_variations <> ''
    )
);

-- Keep flags in sync whenever problem_content is updated
CREATE OR REPLACE FUNCTION problem_content_refresh_quality_flags()
RETURNS TRIGGER AS $$
BEGIN
    NEW.quality_flags := jsonb_build_object(
        'has_recognition_note',          NEW.recognition_note IS NOT NULL AND NEW.recognition_note <> '',
        'has_pattern_recognition_clues', NEW.pattern_recognition_clues IS NOT NULL AND NEW.pattern_recognition_clues <> '',
        'has_when_to_use',               NEW.when_to_use IS NOT NULL AND NEW.when_to_use <> '',
        'has_when_not_to_use',           NEW.when_not_to_use IS NOT NULL AND NEW.when_not_to_use <> '',
        'has_intuition',                 NEW.intuition IS NOT NULL AND NEW.intuition <> '',
        'has_guided_reasoning',          NEW.guided_reasoning IS NOT NULL AND NEW.guided_reasoning <> '',
        'has_solution',                  NEW.solution IS NOT NULL AND NEW.solution <> '',
        'has_brute_force',               NEW.brute_force IS NOT NULL AND NEW.brute_force <> '',
        'has_brute_complexity',          NEW.brute_time IS NOT NULL AND NEW.brute_space IS NOT NULL,
        'has_optimal_approach',          NEW.optimal_approach IS NOT NULL AND NEW.optimal_approach <> '',
        'has_optimal_complexity',        NEW.optimal_time IS NOT NULL AND NEW.optimal_space IS NOT NULL,
        'has_pseudocode',                NEW.pseudocode IS NOT NULL AND NEW.pseudocode <> '',
        'has_why_this_works',            NEW.why_this_works IS NOT NULL AND NEW.why_this_works <> '',
        'has_invariant',                 NEW.invariant IS NOT NULL AND NEW.invariant <> '',
        'has_common_mistakes',           NEW.common_mistakes IS NOT NULL AND NEW.common_mistakes <> '',
        'has_senior_variations',         NEW.senior_variations IS NOT NULL AND NEW.senior_variations <> ''
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_problem_content_quality_flags
BEFORE INSERT OR UPDATE ON problem_content
FOR EACH ROW EXECUTE FUNCTION problem_content_refresh_quality_flags();

-- Promote longest-substring to CONTENT_REVIEW now that the gold-standard content is in place
UPDATE problem_content
SET content_status = 'CONTENT_REVIEW'
WHERE problem_id = (SELECT id FROM problems WHERE slug = 'longest-substring-without-repeating');
