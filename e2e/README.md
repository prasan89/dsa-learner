# DSA Learner E2E QA Track

Independent end-to-end QA track. It is intentionally NOT connected to deployment.

## Setup
Copy .env.example to .env.e2e, set E2E_BASE_URL, E2E_EMAIL and E2E_PASSWORD for a disposable test account, then run npm install and npm run install:browsers.

## Commands
npm run test:smoke
npm run test:regression
npm test
npm run test:headed
npm run test:ui
npm run report

## Phases
Phase 1: Playwright foundation, environment, traces, screenshots, reports and helpers.
Phase 2: authentication, dashboard, DSA learning, problem learning, hints, editor, Run and Submit.
Phase 3: result/error surfaces plus session and protected-route checks.
Phase 4: mastery, revision, progress, system design, opt-in payments and security.

## Rules
Use a dedicated test account. Keep tests independent and repeatable. Payment tests are opt-in. Never store production credentials here. E2E is not a deployment gate yet.

## Next hardening
Deterministic API/database fixtures, cross-browser coverage, visual regression, accessibility assertions, API contract tests and dedicated malicious-code sandbox tests.
