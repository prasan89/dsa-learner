package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;

import java.util.UUID;

/**
 * Queue abstraction for Content Factory pipeline jobs.
 * Decouples job submission from execution — the implementation may be
 * Redis, in-process, or any future broker without changing callers.
 */
public interface ContentJobQueue {

    /**
     * Enqueue a job for asynchronous execution.
     * Returns the job ID for status polling.
     */
    UUID enqueue(CfPipelineJob job);
}
