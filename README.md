# DSA Pattern Expert

A structured DSA learning platform that takes students from "I don't know how to solve DSA" to confidently recognising patterns and cracking Java interviews — with an AI mentor, adaptive practice, spaced repetition, mock interviews, and a Java interview track.

## Monorepo Structure

```
dsa-learner/
├── frontend/          # Next.js 14 + TypeScript — student-facing UI
├── backend/           # Spring Boot 3 + Java 21 — REST API + business logic
├── execution-service/ # Sandboxed Docker Java code runner
└── docker-compose.yml # Local dev orchestration
```

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 14, TypeScript, Tailwind CSS, Monaco Editor |
| Backend | Spring Boot 3, Java 21, Maven |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Code Execution | Docker (isolated Java 21 container) |
| AI | Claude API (Haiku for hints, Sonnet for reviews) |
| Auth | Spring Security + JWT |

## Getting Started

### Prerequisites

- Node.js 20+
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Run locally

```bash
# Start infrastructure (PostgreSQL + Redis)
docker compose up db redis -d

# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev

# Execution service
cd execution-service
docker build -t dsa-execution .
```

### Full stack via Docker Compose

```bash
docker compose up
```

Frontend: http://localhost:3000
Backend API: http://localhost:8080
Execution Service: http://localhost:8081

## Development Phases

| Phase | Description | Status |
|---|---|---|
| 1 | Core MVP — auth, problems, Java editor, execution, submissions | 🔨 In progress |
| 2 | Pattern Learning System | ⏳ Planned |
| 3 | AI Mentor + Credits | ⏳ Planned |
| 4 | Adaptive Practice | ⏳ Planned |
| 5 | Spaced Repetition | ⏳ Planned |
| 6 | Content Expansion (150–250 problems) | ⏳ Planned |
| 7 | AI Mock Interviews | ⏳ Planned |
| 8 | Java Interview Track | ⏳ Planned |
