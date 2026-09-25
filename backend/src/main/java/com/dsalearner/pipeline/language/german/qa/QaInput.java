package com.dsalearner.pipeline.language.german.qa;

/**
 * Input payload for all QA agents. Contains the lesson JSON to be evaluated.
 *
 * @param lessonJson  The full lesson content serialized as a JSON string.
 */
public record QaInput(String lessonJson) {}
