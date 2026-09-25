package com.dsalearner.pipeline.provider;

import org.springframework.stereotype.Component;

/**
 * Mock LLM provider for automated tests.
 * Returns a realistic structured German A1 lesson without making real API calls.
 * Tests must NEVER use the real Anthropic provider.
 */
@Component
public class MockLlmProvider implements LlmProvider {

    @Override
    public String providerName() { return "mock"; }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String text = MOCK_GERMAN_A1_LESSON;
        return new LlmResponse(text, 150, 800, request.modelId(), providerName());
    }

    public static final String MOCK_GERMAN_A1_LESSON = """
            {
              "metadata": {
                "topic": "Greetings and Introductions",
                "cefrLevel": "A1",
                "language": "de",
                "estimatedMinutes": 20
              },
              "objectives": [
                "Greet someone in German formally and informally",
                "Introduce yourself and say your name",
                "Ask someone's name",
                "Say where you are from"
              ],
              "explanation": {
                "intro": "In this lesson you will learn the most important German greetings and how to introduce yourself.",
                "culturalNote": "Germans often shake hands when meeting someone for the first time."
              },
              "vocabulary": [
                {"german": "Hallo", "english": "Hello (informal)", "pronunciation": "HAH-loh", "example": "Hallo, wie geht es dir?"},
                {"german": "Guten Morgen", "english": "Good morning", "pronunciation": "GOO-ten MOR-gen", "example": "Guten Morgen! Wie heißt du?"},
                {"german": "Guten Tag", "english": "Good day (formal)", "pronunciation": "GOO-ten TAHK", "example": "Guten Tag, ich heiße Anna."},
                {"german": "Auf Wiedersehen", "english": "Goodbye (formal)", "pronunciation": "owf VEE-der-zayn", "example": "Auf Wiedersehen! Bis morgen."},
                {"german": "Tschüss", "english": "Bye (informal)", "pronunciation": "CHOOS", "example": "Tschüss! Bis später."},
                {"german": "Ich heiße", "english": "My name is", "pronunciation": "ich HY-seh", "example": "Ich heiße Maria."},
                {"german": "Wie heißt du?", "english": "What is your name? (informal)", "pronunciation": "vee HYSST doo", "example": "Wie heißt du? Ich heiße Tom."},
                {"german": "Ich komme aus", "english": "I come from", "pronunciation": "ich KOM-eh ows", "example": "Ich komme aus Deutschland."},
                {"german": "Woher kommst du?", "english": "Where are you from?", "pronunciation": "VOH-hair KOMST doo", "example": "Woher kommst du? Aus Indien."},
                {"german": "Freut mich", "english": "Nice to meet you", "pronunciation": "froyt mich", "example": "Freut mich, dich kennenzulernen."}
              ],
              "grammar": {
                "title": "Personal Pronouns + sein (to be)",
                "explanation": "In German, 'sein' means 'to be'. For A1 we use ich bin (I am), du bist (you are), er/sie ist (he/she is). This lets you make simple introductions.",
                "pattern": "Ich bin + [name/nationality]",
                "examples": [
                  {"german": "Ich bin Anna.", "english": "I am Anna."},
                  {"german": "Ich bin Studentin.", "english": "I am a student."},
                  {"german": "Du bist Thomas.", "english": "You are Thomas."},
                  {"german": "Sie ist aus Japan.", "english": "She is from Japan."}
                ]
              },
              "examples": [
                {"german": "Hallo! Ich heiße Lena.", "english": "Hello! My name is Lena.", "context": "Informal greeting when meeting someone your age"},
                {"german": "Guten Tag! Ich bin Herr Müller.", "english": "Good day! I am Mr. Müller.", "context": "Formal greeting in a professional setting"},
                {"german": "Woher kommst du? Ich komme aus Indien.", "english": "Where are you from? I come from India.", "context": "Asking about someone's origin"},
                {"german": "Freut mich! Ich heiße Felix.", "english": "Nice to meet you! My name is Felix.", "context": "Responding after being introduced"},
                {"german": "Auf Wiedersehen! Bis morgen.", "english": "Goodbye! See you tomorrow.", "context": "Formal farewell"}
              ],
              "exercises": [
                {
                  "type": "MULTIPLE_CHOICE",
                  "question": "How do you say 'Good morning' in German?",
                  "options": ["Guten Abend", "Guten Morgen", "Gute Nacht", "Guten Tag"],
                  "correctAnswer": "Guten Morgen",
                  "explanation": "Guten Morgen is used in the morning hours. Guten Tag is for daytime, Guten Abend for evening."
                },
                {
                  "type": "FILL_IN_BLANK",
                  "question": "Ich ___ aus Deutschland. (I come from Germany.)",
                  "correctAnswer": "komme",
                  "hint": "Use the verb 'kommen' conjugated for 'ich'"
                },
                {
                  "type": "TRANSLATION",
                  "source": "What is your name?",
                  "correctAnswer": "Wie heißt du?",
                  "hint": "Use 'heißen' - the verb for 'to be called'"
                }
              ]
            }
            """;
}
