package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentType;
import com.dsalearner.pipeline.agent.ModelRouter;
import com.dsalearner.pipeline.agent.PromptRegistry;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import org.springframework.stereotype.Component;

/** Evaluates exercise correctness (correct answers, plausible distractors, lesson alignment). */
@Component
public class ExerciseQaAgent extends BaseGermanQaAgent {

    public ExerciseQaAgent(PromptRegistry promptRegistry,
                            ModelRouter modelRouter,
                            LlmProviderRegistry providerRegistry) {
        super(promptRegistry, modelRouter, providerRegistry);
    }

    @Override
    public String agentType() { return AgentType.EXERCISE_QA; }

    @Override
    protected String promptKey() { return "language.german.a1.qa.exercise"; }
}
