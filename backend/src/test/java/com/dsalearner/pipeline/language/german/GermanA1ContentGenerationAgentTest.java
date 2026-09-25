package com.dsalearner.pipeline.language.german;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.language.ContentGenerationInput;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.provider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GermanA1ContentGenerationAgentTest {

    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock LlmProviderRegistry providerRegistry;
    @Mock LlmProvider mockProvider;

    GermanA1ContentGenerationAgent agent;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();
    private final UUID runId    = UUID.randomUUID();

    @BeforeEach
    void setup() {
        agent = new GermanA1ContentGenerationAgent(promptRegistry, modelRouter, providerRegistry);

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.content_generation", 1,
                "You are a German curriculum designer.",
                "Create a lesson on {{topic}} for {{stableRef}}.");

        ModelConfig modelConfig = new ModelConfig(
                "mock_v1", "mock", "mock-model", 0.3, 1000, 5000,
                BigDecimal.ZERO, BigDecimal.ZERO);

        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
        lenient().when(providerRegistry.get("mock")).thenReturn(mockProvider);
    }

    @Test
    void agentTypeIsContentGenerator() {
        assertEquals(AgentType.CONTENT_GENERATOR, agent.agentType());
    }

    @Test
    void generatesLessonFromMockProvider() {
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(MockLlmProvider.MOCK_GERMAN_A1_LESSON, 150, 800, "mock-model", "mock"));

        AgentOutput<LessonContent> output = agent.execute(buildInput("Greetings"));

        assertTrue(output.succeeded());
        assertNotNull(output.output());
        assertNotNull(output.output().vocabulary());
        assertFalse(output.output().vocabulary().isEmpty());
        assertNotNull(output.output().grammar());
        assertNotNull(output.output().exercises());
        assertEquals(3, output.output().exercises().size());
    }

    @Test
    void lessonContentMapConversionProducesCorrectKeys() {
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(MockLlmProvider.MOCK_GERMAN_A1_LESSON, 150, 800, "mock-model", "mock"));

        AgentOutput<LessonContent> output = agent.execute(buildInput("Greetings"));
        LessonContent content = output.output();

        Map<String, Object> contentMap = content.toContentMap();
        assertTrue(contentMap.containsKey("objectives"));
        assertTrue(contentMap.containsKey("explanation"));

        Map<String, Object> vocabMap = content.toVocabularyMap();
        assertTrue(vocabMap.containsKey("items"));

        Map<String, Object> exercisesMap = content.toExercisesMap();
        assertTrue(exercisesMap.containsKey("items"));

        Map<String, Object> blueprint = content.toBlueprintMap();
        assertEquals("A1", blueprint.get("cefrLevel"));
    }

    @Test
    void throwsOnInvalidModelResponse() {
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse("not json at all", 10, 10, "mock-model", "mock"));

        assertThrows(IllegalStateException.class,
                () -> agent.execute(buildInput("Greetings")));
    }

    @Test
    void throwsOnResponseMissingRequiredFields() {
        // JSON with missing 'vocabulary'
        String incomplete = """
                {
                  "metadata": {"topic": "test", "cefrLevel": "A1", "language": "de", "estimatedMinutes": 20},
                  "objectives": ["obj1"],
                  "explanation": {"intro": "intro"},
                  "grammar": {"title": "g", "explanation": "e", "pattern": "p", "examples": []},
                  "examples": [],
                  "exercises": []
                }
                """;
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(incomplete, 10, 100, "mock-model", "mock"));

        assertThrows(IllegalStateException.class,
                () -> agent.execute(buildInput("Greetings")));
    }

    @Test
    void providerFailureIsRethrown() {
        when(mockProvider.generate(any())).thenThrow(
                new LlmProviderException("timeout", true));

        assertThrows(LlmProviderException.class,
                () -> agent.execute(buildInput("Greetings")));
    }

    private AgentInput<ContentGenerationInput> buildInput(String topic) {
        ContentGenerationInput payload = new ContentGenerationInput(
                topic, "A1", "de", "de-a1-u01-l01");
        AgentInput.AgentContext ctx = new AgentInput.AgentContext(
                "language", "de", "A1", "de-a1-u01-l01", 0, 3,
                promptId, 1, "mock_v1");
        return new AgentInput<>(runId, lessonId, 1, "hash-abc", payload, ctx);
    }
}
