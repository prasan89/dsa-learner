package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentType;
import com.dsalearner.pipeline.agent.ModelRouter;
import com.dsalearner.pipeline.agent.PromptRegistry;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import org.springframework.stereotype.Component;

/** Evaluates CEFR A1 appropriateness (vocabulary level, grammar difficulty, sentence complexity). */
@Component
public class CefrQaAgent extends BaseGermanQaAgent {

    public CefrQaAgent(PromptRegistry promptRegistry,
                        ModelRouter modelRouter,
                        LlmProviderRegistry providerRegistry) {
        super(promptRegistry, modelRouter, providerRegistry);
    }

    @Override
    public String agentType() { return AgentType.CEFR_QA; }

    @Override
    protected String promptKey() { return "language.german.a1.qa.cefr"; }
}
