package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentType;
import com.dsalearner.pipeline.agent.ModelRouter;
import com.dsalearner.pipeline.agent.PromptRegistry;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import org.springframework.stereotype.Component;

/** Evaluates pedagogical quality (objectives, explanation, progression, learner-friendliness). */
@Component
public class PedagogyQaAgent extends BaseGermanQaAgent {

    public PedagogyQaAgent(PromptRegistry promptRegistry,
                            ModelRouter modelRouter,
                            LlmProviderRegistry providerRegistry) {
        super(promptRegistry, modelRouter, providerRegistry);
    }

    @Override
    public String agentType() { return AgentType.PEDAGOGY_QA; }

    @Override
    protected String promptKey() { return "language.german.a1.qa.pedagogy"; }
}
