-- result_reference stores serialised LevelBlueprint JSON which can be several KB
ALTER TABLE cf_curriculum_pipeline_jobs
    ALTER COLUMN result_reference TYPE TEXT;
