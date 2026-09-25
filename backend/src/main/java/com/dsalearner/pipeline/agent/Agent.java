package com.dsalearner.pipeline.agent;

/**
 * Contract every agent must implement.
 *
 * @param <TInput>  domain-specific input payload
 * @param <TOutput> domain-specific output payload
 */
public interface Agent<TInput, TOutput> {

    /** Unique type identifier matching AgentType constants. */
    String agentType();

    /** Execute the agent. Implementations must NOT call AI directly — use ModelRouter. */
    AgentOutput<TOutput> execute(AgentInput<TInput> input);
}
