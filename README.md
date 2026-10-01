# Langoa

An Android-first language-learning civilization game. Learners earn in-game currency (coins, food,
materials, XP) by completing language lessons, then spend those resources to grow a city — from a
Village all the way to an Empire.

Languages in Milestone 1: German (A1). Hindi and Kannada coming in Milestone 2.

## Monorepo Structure

```
dsa-learner/
├── android/           # Jetpack Compose Android app
├── backend/           # Spring Boot 3 + Java 21 — REST API + business logic
├── docs/              # Architecture and milestone documentation
├── frontend/          # Next.js 14 (DSA Learner UI — legacy, see note below)
├── execution-service/ # Sandboxed Docker Java code runner (DSA only)
└── docker-compose.yml # Local dev orchestration
```

> **Note on the DSA Learner backend:** The Spring Boot backend was originally built for a
> structured DSA learning platform. The Langoa language domain was added as a second mode on top
> of that infrastructure. Set `APPLICATION_MODE=language` to run Langoa-only endpoints, or `all`
> to run both domains side by side. The DSA frontend and execution service are still present but
> are not part of the Langoa product.

## Tech Stack

### Langoa (primary)

| Layer | Technology |
|---|---|
| Android | Kotlin 2.x, Jetpack Compose, Hilt, Room, Retrofit, Coroutines |
| Backend | Spring Boot 3, Java 21, Maven |
| Database | PostgreSQL 16 (Flyway migrations) |
| Cache | Redis 7 |
| Auth | Spring Security + JWT |
| AI | Claude API (lesson content generation, hint generation) |

### DSA Learner (legacy, same backend)

| Layer | Technology |
|---|---|
| Frontend | Next.js 14, TypeScript, Tailwind CSS, Monaco Editor |
| Code Execution | Docker (isolated Java 21 container) |

## Getting Started

### Prerequisites

- Android Studio Koala or later (for the Android app)
- Java 21 (for the backend)
- Docker + Docker Compose (for infrastructure)

### Run the backend

```bash
# Start infrastructure (PostgreSQL + Redis)
docker compose up db redis -d

# Set required env vars
export ANTHROPIC_API_KEY=your-key

# Start the backend in language mode
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments="--application.mode=language"
```

Or via Docker Compose:

```bash
docker compose up db redis backend -d
# Backend will start in language mode (APPLICATION_MODE=language is the default in docker-compose.yml)
```

Backend API: http://localhost:8080

### Run the Android app

1. Open `android/` in Android Studio
2. Create or start an emulator running API 26+
3. Run the app — the emulator will reach the backend at http://10.0.2.2:8080

### Test credentials (seeded)

| Role | Email | Password |
|---|---|---|
| Language learner | language-user@example.com | LangUser1! |
| DSA user | dsa-user@example.com | DsaUser1! |

## API Overview

Full API reference is in [docs/langoa-milestone-1.md](docs/langoa-milestone-1.md).

```
POST /api/auth/login
POST /api/auth/register

GET    /api/v1/academy/{language}/curriculum
POST   /api/v1/academy/{language}/lessons/{id}/complete   -- returns XP/coin rewards
GET    /api/v1/civilization/{language}                    -- city state
POST   /api/v1/civilization/{language}/buildings          -- build a new building
GET    /api/v1/civilization/{language}/buildings/definitions
```

## Milestone Status

| Milestone | Description | Status |
|---|---|---|
| 1 | Android app + backend economy, 5 German A1 lessons, city view | Done |
| 2 | 50+ German lessons, Hindi/Kannada seed, streak, building upgrades | Planned |
| 3 | City artwork, animations, Unity renderer spike | Planned |
| 4 | Social features, guilds | Planned |
