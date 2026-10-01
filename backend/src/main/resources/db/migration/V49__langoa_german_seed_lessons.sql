-- V49: Seed 5 German A1 lessons with full content, curriculum plans,
--      learner level progress, civilization, currency balances, and starter building
--      for the seed language-user (00000000-0000-0000-0000-000000000011).
--
-- All INSERTs use ON CONFLICT DO NOTHING for idempotency.
-- UUIDs are fixed so this migration is deterministic and re-runnable.

-- ─── cf_lessons ───────────────────────────────────────────────────────────────

INSERT INTO cf_lessons (
    id, stable_ref, title, domain_code, language_code, cefr_level,
    content_status, publication_status, current_version, active_version
) VALUES
    ('aa000000-0000-0000-0000-000000000001',
     'de-a1-l001',
     'Hallo! - Greetings and Introductions',
     'language', 'de', 'A1', 'APPROVED', 'PUBLISHED', 1, 1),
    ('aa000000-0000-0000-0000-000000000002',
     'de-a1-l002',
     'Wer bist du? - Names and Where You Are From',
     'language', 'de', 'A1', 'APPROVED', 'PUBLISHED', 1, 1),
    ('aa000000-0000-0000-0000-000000000003',
     'de-a1-l003',
     'Zahlen 1-20 - Numbers and Basic Counting',
     'language', 'de', 'A1', 'APPROVED', 'PUBLISHED', 1, 1),
    ('aa000000-0000-0000-0000-000000000004',
     'de-a1-l004',
     'Familie - Talking About Your Family',
     'language', 'de', 'A1', 'APPROVED', 'PUBLISHED', 1, 1),
    ('aa000000-0000-0000-0000-000000000005',
     'de-a1-l005',
     'Farben und Dinge - Colors and Everyday Objects',
     'language', 'de', 'A1', 'APPROVED', 'PUBLISHED', 1, 1)
ON CONFLICT (stable_ref) DO NOTHING;

-- ─── cf_lesson_versions ───────────────────────────────────────────────────────
-- Note: cf_lesson_versions has separate content/vocabulary/grammar/exercises JSONB columns.
-- The task spec's content_json is spread across these columns.
-- publication_status check: 'UNPUBLISHED','SCHEDULED','PUBLISHED','SUPERSEDED','ROLLED_BACK'
-- content_status check includes 'APPROVED'

-- LESSON 1: Hallo! - Greetings and Introductions
INSERT INTO cf_lesson_versions (
    lesson_id, version, content_status, publication_status,
    content, vocabulary, grammar, exercises, frozen, published_at
) VALUES (
    'aa000000-0000-0000-0000-000000000001',
    1,
    'APPROVED',
    'PUBLISHED',
    '{
      "metadata": {
        "title": "Hallo! - Greetings and Introductions",
        "cefrLevel": "A1",
        "language": "de",
        "lessonNumber": 1,
        "estimatedMinutes": 15,
        "canDo": "Greet people and say goodbye in German"
      },
      "objectives": [
        "Use standard German greetings for different times of day",
        "Say goodbye in formal and informal situations",
        "Use basic polite words: please and thank you",
        "Respond appropriately to greetings"
      ],
      "explanation": {
        "english": "German has both formal and informal greetings. Use Guten Morgen in the morning, Guten Tag during the day, and Guten Abend in the evening. Hallo and Hi are informal. Tschüss is informal for goodbye, Auf Wiedersehen is formal.",
        "concept": "Formal vs informal register in German greetings"
      }
    }',
    '{
      "items": [
        {"german": "Hallo",          "english": "Hello",          "pronunciation": "HAL-oh"},
        {"german": "Guten Morgen",   "english": "Good morning",   "pronunciation": "GOO-ten MOR-gen"},
        {"german": "Guten Tag",      "english": "Good day",       "pronunciation": "GOO-ten TAHK"},
        {"german": "Guten Abend",    "english": "Good evening",   "pronunciation": "GOO-ten AH-bent"},
        {"german": "Auf Wiedersehen","english": "Goodbye",        "pronunciation": "owf VEE-der-zayn"},
        {"german": "Tschüss",        "english": "Bye",            "pronunciation": "CHOOS"},
        {"german": "Bitte",          "english": "Please / You are welcome", "pronunciation": "BIT-eh"},
        {"german": "Danke",          "english": "Thank you",      "pronunciation": "DAN-keh"},
        {"german": "Ja",             "english": "Yes",            "pronunciation": "YAH"},
        {"german": "Nein",           "english": "No",             "pronunciation": "NINE"}
      ]
    }',
    '{
      "concept": "Formal vs informal greetings",
      "explanation": "German distinguishes formal (Sie) and informal (du) address. Formal greetings like Guten Tag and Auf Wiedersehen are used with strangers and in professional settings. Hallo and Tschüss are friendly and informal, used with friends and family.",
      "examples": [
        {"german": "Guten Morgen, Herr Müller!", "english": "Good morning, Mr. Müller!"},
        {"german": "Hallo, Anna! Wie geht es dir?", "english": "Hello, Anna! How are you?"},
        {"german": "Auf Wiedersehen!", "english": "Goodbye!"},
        {"german": "Tschüss!", "english": "Bye!"}
      ]
    }',
    '{
      "items": [
        {
          "id": "ex-1",
          "type": "VOCABULARY",
          "question": "What does Hallo mean?",
          "options": ["Hello", "Goodbye", "Please", "Thank you"],
          "correctAnswer": "Hello",
          "explanation": "Hallo is the standard German greeting, equivalent to Hello in English."
        },
        {
          "id": "ex-2",
          "type": "VOCABULARY",
          "question": "Which greeting is used in the morning?",
          "options": ["Guten Morgen", "Guten Abend", "Guten Tag", "Tschüss"],
          "correctAnswer": "Guten Morgen",
          "explanation": "Guten Morgen means Good morning and is used before noon."
        },
        {
          "id": "ex-3",
          "type": "TRANSLATE_TO_TARGET",
          "question": "Translate: Good evening",
          "correctAnswer": "Guten Abend",
          "hint": "Think about which time of day this is"
        },
        {
          "id": "ex-4",
          "type": "MULTIPLE_CHOICE",
          "question": "Which word is the formal way to say goodbye?",
          "options": ["Tschüss", "Hallo", "Auf Wiedersehen", "Bitte"],
          "correctAnswer": "Auf Wiedersehen",
          "explanation": "Auf Wiedersehen literally means until we see each other again and is the formal farewell."
        }
      ]
    }',
    TRUE,
    NOW()
) ON CONFLICT (lesson_id, version) DO NOTHING;

-- LESSON 2: Wer bist du? - Names and Where You Are From
INSERT INTO cf_lesson_versions (
    lesson_id, version, content_status, publication_status,
    content, vocabulary, grammar, exercises, frozen, published_at
) VALUES (
    'aa000000-0000-0000-0000-000000000002',
    1,
    'APPROVED',
    'PUBLISHED',
    '{
      "metadata": {
        "title": "Wer bist du? - Names and Where You Are From",
        "cefrLevel": "A1",
        "language": "de",
        "lessonNumber": 2,
        "estimatedMinutes": 15,
        "canDo": "Introduce yourself and ask someone their name"
      },
      "objectives": [
        "Introduce yourself by saying your name",
        "Ask someone else their name",
        "Say where you are from",
        "Ask someone where they are from",
        "Conjugate heißen and kommen for ich and du"
      ],
      "explanation": {
        "english": "To introduce yourself in German, say Ich heiße followed by your name. To ask someone their name, say Wie heißt du? For origin, use Ich komme aus followed by your country. Woher kommst du? asks where someone is from.",
        "concept": "German verb conjugation for heißen (to be called) and kommen (to come)"
      }
    }',
    '{
      "items": [
        {"german": "Ich heiße",       "english": "My name is / I am called", "pronunciation": "ICH HY-seh"},
        {"german": "Wie heißt du?",   "english": "What is your name?",       "pronunciation": "VEE hysst DOO"},
        {"german": "Er heißt",        "english": "His name is",              "pronunciation": "AIR hysst"},
        {"german": "Sie heißt",       "english": "Her name is",              "pronunciation": "ZEE hysst"},
        {"german": "Woher kommst du?","english": "Where are you from?",      "pronunciation": "VOH-hair KOMST doo"},
        {"german": "Ich komme aus",   "english": "I come from",              "pronunciation": "ICH KOM-eh owss"},
        {"german": "Freut mich",      "english": "Nice to meet you",         "pronunciation": "FROYT mikh"},
        {"german": "Und du?",         "english": "And you?",                 "pronunciation": "OONT doo"}
      ]
    }',
    '{
      "concept": "Verb conjugation: heißen and kommen",
      "explanation": "German verbs change their ending depending on the subject. For heißen (to be called): ich heiße (I am called), du heißt (you are called), er/sie/es heißt (he/she/it is called). For kommen (to come): ich komme (I come), du kommst (you come), er/sie/es kommt (he/she/it comes).",
      "examples": [
        {"german": "Ich heiße Maria.", "english": "My name is Maria."},
        {"german": "Wie heißt du?", "english": "What is your name?"},
        {"german": "Ich komme aus Deutschland.", "english": "I come from Germany."},
        {"german": "Woher kommst du?", "english": "Where are you from?"}
      ]
    }',
    '{
      "items": [
        {
          "id": "ex-1",
          "type": "VOCABULARY",
          "question": "What does Ich heiße mean?",
          "options": ["My name is", "His name is", "Where are you from?", "Nice to meet you"],
          "correctAnswer": "My name is",
          "explanation": "Ich heiße literally means I am called and is used to introduce your name."
        },
        {
          "id": "ex-2",
          "type": "TRANSLATE_TO_TARGET",
          "question": "Translate: What is your name?",
          "correctAnswer": "Wie heißt du?",
          "hint": "How is Wie, called is heißt, you is du"
        },
        {
          "id": "ex-3",
          "type": "MULTIPLE_CHOICE",
          "question": "How do you say I come from England in German?",
          "options": [
            "Ich komme aus England.",
            "Du kommst aus England.",
            "Woher kommst du?",
            "Ich heiße England."
          ],
          "correctAnswer": "Ich komme aus England.",
          "explanation": "Ich komme aus means I come from. The country name stays the same."
        },
        {
          "id": "ex-4",
          "type": "VOCABULARY",
          "question": "What does Freut mich mean?",
          "options": ["Nice to meet you", "Goodbye", "And you?", "Where are you from?"],
          "correctAnswer": "Nice to meet you",
          "explanation": "Freut mich literally means It pleases me and is the standard way to say nice to meet you."
        }
      ]
    }',
    TRUE,
    NOW()
) ON CONFLICT (lesson_id, version) DO NOTHING;

-- LESSON 3: Zahlen 1-20 - Numbers and Basic Counting
INSERT INTO cf_lesson_versions (
    lesson_id, version, content_status, publication_status,
    content, vocabulary, grammar, exercises, frozen, published_at
) VALUES (
    'aa000000-0000-0000-0000-000000000003',
    1,
    'APPROVED',
    'PUBLISHED',
    '{
      "metadata": {
        "title": "Zahlen 1-20 - Numbers and Basic Counting",
        "cefrLevel": "A1",
        "language": "de",
        "lessonNumber": 3,
        "estimatedMinutes": 15,
        "canDo": "Count from 1-20 and use numbers in basic sentences"
      },
      "objectives": [
        "Count from 1 to 20 in German",
        "Recognize written German number words",
        "Use numbers to state age: Ich bin X Jahre alt",
        "Identify compound number patterns for 13-19"
      ],
      "explanation": {
        "english": "German numbers 1-12 are irregular and must be memorized. Numbers 13-19 follow a pattern: the unit number plus zehn (ten). For example dreizehn means three-ten (13). Zwanzig (20) is its own word.",
        "concept": "German numbers 1-20 and the -zehn suffix pattern"
      }
    }',
    '{
      "items": [
        {"german": "ein / eins", "english": "one",       "pronunciation": "EYE-n / EYNS"},
        {"german": "zwei",       "english": "two",       "pronunciation": "TSVY"},
        {"german": "drei",       "english": "three",     "pronunciation": "DRY"},
        {"german": "vier",       "english": "four",      "pronunciation": "FEER"},
        {"german": "fünf",       "english": "five",      "pronunciation": "FUENF"},
        {"german": "sechs",      "english": "six",       "pronunciation": "ZEKS"},
        {"german": "sieben",     "english": "seven",     "pronunciation": "ZEE-ben"},
        {"german": "acht",       "english": "eight",     "pronunciation": "AKHT"},
        {"german": "neun",       "english": "nine",      "pronunciation": "NOYN"},
        {"german": "zehn",       "english": "ten",       "pronunciation": "TSAYN"},
        {"german": "elf",        "english": "eleven",    "pronunciation": "ELF"},
        {"german": "zwölf",      "english": "twelve",    "pronunciation": "TSVOLUF"},
        {"german": "dreizehn",   "english": "thirteen",  "pronunciation": "DRY-tsayn"},
        {"german": "vierzehn",   "english": "fourteen",  "pronunciation": "FEER-tsayn"},
        {"german": "fünfzehn",   "english": "fifteen",   "pronunciation": "FUENF-tsayn"},
        {"german": "sechzehn",   "english": "sixteen",   "pronunciation": "ZEKH-tsayn"},
        {"german": "siebzehn",   "english": "seventeen", "pronunciation": "ZEEP-tsayn"},
        {"german": "achtzehn",   "english": "eighteen",  "pronunciation": "AKHT-tsayn"},
        {"german": "neunzehn",   "english": "nineteen",  "pronunciation": "NOYN-tsayn"},
        {"german": "zwanzig",    "english": "twenty",    "pronunciation": "TSVAN-tsikh"}
      ]
    }',
    '{
      "concept": "Numbers and the -zehn suffix",
      "explanation": "Numbers 13-19 in German are formed by adding -zehn to the unit number, similar to the English -teen suffix. Exceptions: sechzehn (not sechszehn) and siebzehn (not siebenzehn). Use numbers with Ich bin ... Jahre alt to state your age.",
      "examples": [
        {"german": "Ich bin fünfzehn Jahre alt.", "english": "I am fifteen years old."},
        {"german": "Ich habe zwei Geschwister.", "english": "I have two siblings."},
        {"german": "Das kostet zehn Euro.", "english": "That costs ten euros."},
        {"german": "Wir sind zwanzig Schüler.", "english": "We are twenty students."}
      ]
    }',
    '{
      "items": [
        {
          "id": "ex-1",
          "type": "VOCABULARY",
          "question": "What is the German word for seven?",
          "options": ["sieben", "sechs", "acht", "neun"],
          "correctAnswer": "sieben",
          "explanation": "Sieben is the German word for seven. Remember: six=sechs, seven=sieben, eight=acht, nine=neun."
        },
        {
          "id": "ex-2",
          "type": "VOCABULARY",
          "question": "Which number does dreizehn represent?",
          "options": ["13", "30", "3", "31"],
          "correctAnswer": "13",
          "explanation": "Dreizehn = drei (three) + zehn (ten) = thirteen. Numbers 13-19 use this -zehn suffix pattern."
        },
        {
          "id": "ex-3",
          "type": "TRANSLATE_TO_TARGET",
          "question": "Translate: I am eighteen years old.",
          "correctAnswer": "Ich bin achtzehn Jahre alt.",
          "hint": "achtzehn = eighteen, Jahre alt = years old"
        },
        {
          "id": "ex-4",
          "type": "MULTIPLE_CHOICE",
          "question": "What is the correct German spelling for sixteen?",
          "options": ["sechszehn", "sechzehn", "sechzehn", "siebenzehn"],
          "correctAnswer": "sechzehn",
          "explanation": "Sechzehn drops the -s from sechs when combined with zehn. This is an irregular spelling to remember."
        }
      ]
    }',
    TRUE,
    NOW()
) ON CONFLICT (lesson_id, version) DO NOTHING;

-- LESSON 4: Familie - Talking About Your Family
INSERT INTO cf_lesson_versions (
    lesson_id, version, content_status, publication_status,
    content, vocabulary, grammar, exercises, frozen, published_at
) VALUES (
    'aa000000-0000-0000-0000-000000000004',
    1,
    'APPROVED',
    'PUBLISHED',
    '{
      "metadata": {
        "title": "Familie - Talking About Your Family",
        "cefrLevel": "A1",
        "language": "de",
        "lessonNumber": 4,
        "estimatedMinutes": 15,
        "canDo": "Talk about your family members in German"
      },
      "objectives": [
        "Name immediate and extended family members in German",
        "Use definite articles der, die, das correctly with family nouns",
        "State family relationships: Das ist meine Mutter",
        "Understand gender patterns for family members"
      ],
      "explanation": {
        "english": "Every German noun has a grammatical gender: masculine (der), feminine (die), or neuter (das). Family members follow natural gender in most cases: male relatives take der, female relatives take die. The word for child, das Kind, is neuter.",
        "concept": "German definite articles der, die, das"
      }
    }',
    '{
      "items": [
        {"german": "die Familie",      "english": "the family",       "pronunciation": "dee fah-MEE-lee-eh"},
        {"german": "die Mutter",       "english": "the mother",       "pronunciation": "dee MUT-ter"},
        {"german": "der Vater",        "english": "the father",       "pronunciation": "dair FAH-ter"},
        {"german": "die Schwester",    "english": "the sister",       "pronunciation": "dee SHVES-ter"},
        {"german": "der Bruder",       "english": "the brother",      "pronunciation": "dair BROO-der"},
        {"german": "die Großmutter",   "english": "the grandmother",  "pronunciation": "dee GROHS-mut-ter"},
        {"german": "der Großvater",    "english": "the grandfather",  "pronunciation": "dair GROHS-fah-ter"},
        {"german": "das Kind",         "english": "the child",        "pronunciation": "dahs KINT"}
      ]
    }',
    '{
      "concept": "German definite articles: der, die, das",
      "explanation": "German nouns have three genders: masculine (der), feminine (die), and neuter (das). Unlike English, where we use the for all nouns, German uses three different definite articles. Most male family members use der, most female family members use die. Das Kind (child) is neuter. You must learn each noun with its article.",
      "examples": [
        {"german": "Der Vater ist groß.", "english": "The father is tall."},
        {"german": "Die Mutter ist nett.", "english": "The mother is kind."},
        {"german": "Das Kind ist klein.", "english": "The child is small."},
        {"german": "Das ist mein Bruder.", "english": "This is my brother."}
      ]
    }',
    '{
      "items": [
        {
          "id": "ex-1",
          "type": "VOCABULARY",
          "question": "What is the correct article for Mutter (mother)?",
          "options": ["die", "der", "das", "ein"],
          "correctAnswer": "die",
          "explanation": "Die Mutter uses die because Mutter is a feminine noun. Female family members generally use the feminine article die."
        },
        {
          "id": "ex-2",
          "type": "VOCABULARY",
          "question": "What is the German word for grandfather?",
          "options": ["der Großvater", "die Großmutter", "der Vater", "der Bruder"],
          "correctAnswer": "der Großvater",
          "explanation": "Der Großvater means grandfather. Groß means big/great, and Vater means father."
        },
        {
          "id": "ex-3",
          "type": "MULTIPLE_CHOICE",
          "question": "Which article is used with Kind (child)?",
          "options": ["das", "der", "die", "ein"],
          "correctAnswer": "das",
          "explanation": "Das Kind is neuter. Even though a child can be male or female, the grammatical gender of Kind is neuter."
        },
        {
          "id": "ex-4",
          "type": "TRANSLATE_TO_TARGET",
          "question": "Translate: This is my sister.",
          "correctAnswer": "Das ist meine Schwester.",
          "hint": "Das ist = This is, meine = my (feminine), Schwester = sister"
        }
      ]
    }',
    TRUE,
    NOW()
) ON CONFLICT (lesson_id, version) DO NOTHING;

-- LESSON 5: Farben und Dinge - Colors and Everyday Objects
INSERT INTO cf_lesson_versions (
    lesson_id, version, content_status, publication_status,
    content, vocabulary, grammar, exercises, frozen, published_at
) VALUES (
    'aa000000-0000-0000-0000-000000000005',
    1,
    'APPROVED',
    'PUBLISHED',
    '{
      "metadata": {
        "title": "Farben und Dinge - Colors and Everyday Objects",
        "cefrLevel": "A1",
        "language": "de",
        "lessonNumber": 5,
        "estimatedMinutes": 15,
        "canDo": "Name colors and everyday objects in German"
      },
      "objectives": [
        "Name common colors in German",
        "Name common everyday objects",
        "Describe objects using colors: Das Auto ist rot",
        "Understand basic adjective-noun placement"
      ],
      "explanation": {
        "english": "German adjectives can be used predicatively (after the verb sein/to be) without any ending change, or attributively (before the noun) where they take endings based on gender and case. At A1, we focus on predicative use: Das Haus ist groß (The house is big).",
        "concept": "Predicative adjective use in German"
      }
    }',
    '{
      "items": [
        {"german": "rot",      "english": "red",      "pronunciation": "ROHT"},
        {"german": "blau",     "english": "blue",     "pronunciation": "BLOW"},
        {"german": "grün",     "english": "green",    "pronunciation": "GRUEN"},
        {"german": "gelb",     "english": "yellow",   "pronunciation": "GELP"},
        {"german": "schwarz",  "english": "black",    "pronunciation": "SHVARTS"},
        {"german": "weiß",     "english": "white",    "pronunciation": "VICE"},
        {"german": "groß",     "english": "big",      "pronunciation": "GROHS"},
        {"german": "klein",    "english": "small",    "pronunciation": "KLINE"},
        {"german": "das Haus", "english": "the house","pronunciation": "dahs HOWSS"},
        {"german": "das Auto", "english": "the car",  "pronunciation": "dahs OW-toh"},
        {"german": "der Tisch","english": "the table","pronunciation": "dair TISH"},
        {"german": "der Stuhl","english": "the chair","pronunciation": "dair SHTOOL"}
      ]
    }',
    '{
      "concept": "Predicative adjectives in German",
      "explanation": "When an adjective comes after the verb sein (to be), it does not change its form regardless of the noun gender. This is called predicative position. For example: Der Tisch ist groß (The table is big), Die Wand ist weiß (The wall is white), Das Auto ist rot (The car is red). All three use the same adjective form.",
      "examples": [
        {"german": "Das Auto ist rot.", "english": "The car is red."},
        {"german": "Der Tisch ist groß.", "english": "The table is big."},
        {"german": "Das Haus ist weiß.", "english": "The house is white."},
        {"german": "Der Stuhl ist klein.", "english": "The chair is small."}
      ]
    }',
    '{
      "items": [
        {
          "id": "ex-1",
          "type": "VOCABULARY",
          "question": "What color is blau?",
          "options": ["blue", "green", "yellow", "red"],
          "correctAnswer": "blue",
          "explanation": "Blau means blue in German. Remember: rot=red, blau=blue, grün=green, gelb=yellow."
        },
        {
          "id": "ex-2",
          "type": "VOCABULARY",
          "question": "What is the German word for chair?",
          "options": ["der Stuhl", "der Tisch", "das Haus", "das Auto"],
          "correctAnswer": "der Stuhl",
          "explanation": "Der Stuhl means the chair. Der Tisch means the table. Both use the masculine article der."
        },
        {
          "id": "ex-3",
          "type": "TRANSLATE_TO_TARGET",
          "question": "Translate: The house is big.",
          "correctAnswer": "Das Haus ist groß.",
          "hint": "Das Haus = the house, ist = is, groß = big"
        },
        {
          "id": "ex-4",
          "type": "MULTIPLE_CHOICE",
          "question": "Which sentence correctly describes a red car?",
          "options": [
            "Das Auto ist rot.",
            "Das Auto ist rote.",
            "Das rot Auto.",
            "Rot das Auto ist."
          ],
          "correctAnswer": "Das Auto ist rot.",
          "explanation": "In predicative position, adjectives do not take endings. Das Auto ist rot is correct. Rote would be an attributive form used before the noun."
        }
      ]
    }',
    TRUE,
    NOW()
) ON CONFLICT (lesson_id, version) DO NOTHING;

-- ─── cf_curriculum_lesson_plans ───────────────────────────────────────────────

INSERT INTO cf_curriculum_lesson_plans (
    id, curriculum_id, level_id, unit_id, stable_ref,
    position, title, topic, plan_status
) VALUES
    ('bb000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     NULL,
     'de-a1-u01-l001',
     1,
     'Hallo! - Greetings and Introductions',
     'German greetings, farewells, and basic polite expressions',
     'PUBLISHED'),
    ('bb000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     NULL,
     'de-a1-u01-l002',
     2,
     'Wer bist du? - Names and Where You Are From',
     'Introducing yourself and asking for names and origins',
     'PUBLISHED'),
    ('bb000000-0000-0000-0000-000000000003',
     '00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     NULL,
     'de-a1-u01-l003',
     3,
     'Zahlen 1-20 - Numbers and Basic Counting',
     'Counting from 1 to 20 and using numbers in sentences',
     'PUBLISHED'),
    ('bb000000-0000-0000-0000-000000000004',
     '00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     NULL,
     'de-a1-u01-l004',
     4,
     'Familie - Talking About Your Family',
     'Family vocabulary and German definite articles der, die, das',
     'PUBLISHED'),
    ('bb000000-0000-0000-0000-000000000005',
     '00000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001',
     NULL,
     'de-a1-u01-l005',
     5,
     'Farben und Dinge - Colors and Everyday Objects',
     'Colors and common objects, predicative adjective use',
     'PUBLISHED')
ON CONFLICT (stable_ref) DO NOTHING;

-- Link lesson plans to their lesson rows
UPDATE cf_curriculum_lesson_plans
   SET lesson_id = 'aa000000-0000-0000-0000-000000000001'
 WHERE stable_ref = 'de-a1-u01-l001' AND lesson_id IS NULL;

UPDATE cf_curriculum_lesson_plans
   SET lesson_id = 'aa000000-0000-0000-0000-000000000002'
 WHERE stable_ref = 'de-a1-u01-l002' AND lesson_id IS NULL;

UPDATE cf_curriculum_lesson_plans
   SET lesson_id = 'aa000000-0000-0000-0000-000000000003'
 WHERE stable_ref = 'de-a1-u01-l003' AND lesson_id IS NULL;

UPDATE cf_curriculum_lesson_plans
   SET lesson_id = 'aa000000-0000-0000-0000-000000000004'
 WHERE stable_ref = 'de-a1-u01-l004' AND lesson_id IS NULL;

UPDATE cf_curriculum_lesson_plans
   SET lesson_id = 'aa000000-0000-0000-0000-000000000005'
 WHERE stable_ref = 'de-a1-u01-l005' AND lesson_id IS NULL;

-- ─── learner_level_progress ───────────────────────────────────────────────────

INSERT INTO learner_level_progress (
    user_id, curriculum_id, cefr_level, status,
    lessons_total, lessons_completed, unlocked_at
) VALUES (
    '00000000-0000-0000-0000-000000000011',
    '00000000-0000-0000-0000-000000000001',
    'A1',
    'IN_PROGRESS',
    5,
    0,
    NOW()
) ON CONFLICT ON CONSTRAINT uq_learner_level DO NOTHING;

-- ─── langoa_civilizations ─────────────────────────────────────────────────────

INSERT INTO langoa_civilizations (
    id, user_id, language_code, name, civilization_tier, tier_level
) VALUES (
    'cc000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000011',
    'de',
    'My German Civilization',
    'VILLAGE',
    1
) ON CONFLICT (user_id, language_code) DO NOTHING;

-- ─── langoa_currency_balances ─────────────────────────────────────────────────

INSERT INTO langoa_currency_balances (
    id, user_id, language_code, currency_type, balance
) VALUES
    ('dd000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011', 'de', 'COINS',              200),
    ('dd000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011', 'de', 'GEMS',                 0),
    ('dd000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011', 'de', 'XP',                   0),
    ('dd000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000011', 'de', 'FOOD',                50),
    ('dd000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000011', 'de', 'MATERIALS',           30),
    ('dd000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000011', 'de', 'CIVILIZATION_POWER',   0)
ON CONFLICT (user_id, language_code, currency_type) DO NOTHING;

-- ─── langoa_transactions (initial grant records) ──────────────────────────────

INSERT INTO langoa_transactions (
    id, user_id, language_code, transaction_type, currency_type,
    amount, balance_after, source_reference, idempotency_key
) VALUES
    ('ee000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'COINS', 200, 200, 'seed_v49', 'seed-v49-coins-initial-grant'),
    ('ee000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'GEMS', 0, 0, 'seed_v49', 'seed-v49-gems-initial-grant'),
    ('ee000000-0000-0000-0000-000000000003',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'XP', 0, 0, 'seed_v49', 'seed-v49-xp-initial-grant'),
    ('ee000000-0000-0000-0000-000000000004',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'FOOD', 50, 50, 'seed_v49', 'seed-v49-food-initial-grant'),
    ('ee000000-0000-0000-0000-000000000005',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'MATERIALS', 30, 30, 'seed_v49', 'seed-v49-materials-initial-grant'),
    ('ee000000-0000-0000-0000-000000000006',
     '00000000-0000-0000-0000-000000000011', 'de',
     'INITIAL_GRANT', 'CIVILIZATION_POWER', 0, 0, 'seed_v49', 'seed-v49-civpower-initial-grant')
ON CONFLICT (idempotency_key) DO NOTHING;

-- ─── langoa_building_instances (starter HOUSE) ───────────────────────────────

INSERT INTO langoa_building_instances (
    civilization_id, building_type, current_level, position_x, position_y
)
SELECT
    'cc000000-0000-0000-0000-000000000001',
    'HOUSE',
    1,
    2,
    2
WHERE EXISTS (
    SELECT 1 FROM langoa_civilizations
     WHERE id = 'cc000000-0000-0000-0000-000000000001'
)
AND NOT EXISTS (
    SELECT 1 FROM langoa_building_instances
     WHERE civilization_id = 'cc000000-0000-0000-0000-000000000001'
       AND building_type = 'HOUSE'
);
