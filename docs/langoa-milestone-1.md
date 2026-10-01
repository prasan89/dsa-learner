# Langoa — Milestone 1

## Overview

Milestone 1 delivers a vertical slice of Langoa: an Android-first language-learning game where
learners earn in-game currency and build a civilization as they complete language lessons.

The slice covers:
- Backend: new civilization/economy domain wired into the existing Spring Boot 3 backend
- Android: Jetpack Compose app with login, language selection, lesson flow, reward screen, and a
  Compose Canvas city view
- Database: two new Flyway migrations (V48, V49) adding seven economy tables and seeding five
  German A1 lessons

A user can register, choose German, complete five A1 lessons, receive XP and coin rewards after
each lesson, and spend those coins to build a FARM beside the starter HOUSE — all persisted across
app restarts.

---

## Architecture

### Backend

The backend reuses the existing Spring Boot 3 / Java 21 / PostgreSQL infrastructure that was built
for the DSA Learner.

New additions in this milestone:

- **`com.dsalearner.civilization` package** — self-contained domain for civilization state,
  currency balances, building definitions, and reward logic
- **`CivilizationService`** — transactional service that applies lesson rewards, manages currency
  balances with idempotency keys, handles building purchases, and advances civilization tiers
- **`CivilizationController`** — REST controller on `/api/v1/civilization/{language}`, activated
  only when `application.mode` is `language` or `all`
- **`AcademyService` integration** — `completeLesson` now calls `CivilizationService.applyLessonReward`
  and returns reward fields in `LessonCompletionResponse`
- **`DomainAuthorizationService`** — guards civilization endpoints behind the `ROLE_DOMAIN_LANGUAGE`
  authority so a DSA-only account cannot access language APIs
- **`application.mode`** — controlled via `APPLICATION_MODE` env var; defaults to `all`

Existing infrastructure reused without modification:
- JWT auth, Spring Security, Flyway migrations
- `academy` package (curriculum, lessons, lesson progress, level progress)
- `pipeline` package (content creation/review workflow)

### Android

- **Language**: Kotlin 2.x + Jetpack Compose
- **DI**: Hilt
- **Architecture**: MVVM — `ViewModel` + `StateFlow` per screen, `UseCase` objects in domain layer,
  `Repository` interfaces with `Impl` classes backed by Retrofit
- **Local DB**: Room (token storage + lesson progress cache)
- **Navigation**: Compose Navigation with a sealed `Screen` class

Key screens:
- `SplashScreen` — checks token validity and routes to login or home
- `LoginScreen` / `RegisterScreen` — JWT auth
- `LanguagePickerScreen` — choose German (Hindi, Kannada disabled in M1)
- `HomeScreen` — shows curriculum tree with locked/unlocked levels
- `LessonScreen` — exercise runner (multiple-choice, translate, vocabulary)
- `RewardScreen` — animated reward breakdown after lesson completion
- `WorldScreen` — Compose Canvas city view with placed buildings
- `BuildScreen` — building definitions list with affordability check
- `ProfileScreen` — basic user stats

---

## Database Schema

### V48 — Langoa Civilization & Economy (`V48__langoa_civilization_economy.sql`)

| Table | Purpose |
|---|---|
| `langoa_civilizations` | One row per (user, language) — tier, total XP, total lessons |
| `langoa_currency_balances` | Balance per (user, language, currency_type) — COINS, GEMS, XP, FOOD, MATERIALS, CIVILIZATION_POWER |
| `langoa_transactions` | Ledger of every balance change; idempotency_key prevents double-rewards |
| `langoa_building_definitions` | Master list of building types with display info |
| `langoa_building_level_configs` | Cost and unlock requirements per (building_type, level) |
| `langoa_building_instances` | Buildings placed in a user's civilization |
| `langoa_reward_definitions` | XP/coin/food/material rewards per (cefr_level, difficulty_tier) |

Also seeds initial building definitions (HOUSE, FARM, LEARNING_CENTER, SCHOOL, MARKET, WORKSHOP),
level configs for the first three buildings, and reward tables for A1–C1.

### V49 — German A1 Seed Lessons (`V49__langoa_german_seed_lessons.sql`)

Seeds five German A1 lessons with full exercise content, links them to the German A1 curriculum
level, creates a learner level progress row, and initializes civilization + currency balances for
the seed `language-user` account.

---

## API Reference

All routes require a valid JWT in the `Authorization: Bearer <token>` header, except auth routes.

### Authentication (existing)

```
POST /api/auth/login
POST /api/auth/register
```

### Learning (existing, extended)

```
GET    /api/v1/academy/{language}/curriculum
GET    /api/v1/academy/{language}/lessons/{lessonId}
PATCH  /api/v1/academy/{language}/lessons/{lessonId}/step
POST   /api/v1/academy/{language}/lessons/{lessonId}/complete   -- now returns reward fields
GET    /api/v1/academy/{language}/progress
```

The `POST .../complete` response now includes:

```json
{
  "lessonId": "...",
  "lessonStatus": "COMPLETED",
  "score": 95,
  "completedAt": "...",
  "nextLevelUnlocked": false,
  "nextCefrLevel": null,
  "xpEarned": 100,
  "coinsEarned": 50,
  "foodEarned": 10,
  "materialsEarned": 5,
  "civilizationPowerEarned": 100,
  "newBalances": { "COINS": 250, "XP": 100, ... },
  "tierUpgraded": false,
  "newCivilizationTier": null
}
```

### Civilization (new)

```
GET  /api/v1/civilization/{language}
     Returns full civilization state (tier, balances, placed buildings).
     Creates the civilization on first call.

GET  /api/v1/civilization/{language}/buildings
     Returns the list of building instances for the user's city.

GET  /api/v1/civilization/{language}/buildings/definitions
     Returns all active building definitions with per-level costs and
     an affordability flag based on current balances.

POST /api/v1/civilization/{language}/buildings
     Body: { "buildingType": "FARM", "positionX": 3, "positionY": 4 }
     Deducts resources and creates a new building instance.
```

`{language}` accepts ISO 639-1 codes (`de`) or full English names (`german`).

Requires `ROLE_DOMAIN_LANGUAGE` authority (users enrolled in the language domain).

---

## Running Locally

### Backend

```bash
# Start infrastructure
docker compose up db redis -d

# Set env vars
export ANTHROPIC_API_KEY=your-key
export APPLICATION_MODE=language

# Run backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments="--application.mode=language"
```

Or via Docker Compose (defaults to `APPLICATION_MODE=language`):

```bash
APPLICATION_MODE=language docker compose up db redis backend -d
```

### Android

```bash
# Install Android Studio Koala or later
# Open /path/to/android/ in Android Studio
# Configure BASE_URL in app/build.gradle.kts if needed (default: http://10.0.2.2:8080)
# Run on emulator (API 26+)
# Emulator localhost maps to host machine at http://10.0.2.2:8080
```

### Test Credentials (seeded in V47 + V49)

| Role | Email | Password |
|---|---|---|
| Language learner | language-user@example.com | LangUser1! |
| DSA user | dsa-user@example.com | DsaUser1! |

---

## Milestone 1 Vertical Slice Verification

A step-by-step walkthrough to confirm the full slice works end-to-end:

1. **Register or login** as `language-user@example.com` / `LangUser1!`
2. **Choose German** on the Language Picker screen
3. **View curriculum** — A1 level should be IN_PROGRESS with 5 lessons visible
4. **Open Lesson 1** — "Hallo! - Greetings and Introductions"
5. **Complete all 4 exercises** in the lesson
6. **View Reward screen** — should show +100 XP, +50 Coins, +10 Food, +5 Materials,
   +100 Civilization Power
7. **Navigate to World screen** — city should show a HOUSE at position (2,2)
8. **Navigate to Build screen** — FARM should be listed as affordable
   (requires 1 lesson completed + 100 coins + 10 materials; user now has both)
9. **Build FARM** at any grid position
10. **Return to World screen** — FARM should now appear in the city
11. **Close and reopen the app**
12. **Verify persistence** — lesson progress, coin balance, and city state are all restored from
    the backend

---

## Known Limitations (Milestone 2 scope)

- Hindi and Kannada content not seeded
- Full offline mode not implemented (online-first; Room caches read state only)
- Speaking exercises not implemented
- Social and guild features deferred
- Unity game renderer deferred — Compose Canvas used for Milestone 1
- Only German Stage 1 (A1) content available (5 lessons)
- Gems and premium currency UI not implemented
- No push notifications or streak system yet
- Android tablet layout not optimized

---

## Next: Milestone 2

- 50+ German A1/A2 lessons
- City visual improvements (building artwork, animations)
- Hindi and Kannada content seeding
- Streak system and daily rewards
- Building upgrade flow (level 2/3 buildings)
- Offline lesson caching
- Register flow in-app (Milestone 1 uses seeded credentials)
