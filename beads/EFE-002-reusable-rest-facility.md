# EFE-002 — Reusable REST Facility with Cucumber Acceptance Tests

## 1. Objective
Build a generic, job-type independent REST facility under `/api/v1` that can be reused across any future EFE project without reconciliation-specific business logic.

## 2. API Contract
- `POST /api/v1/jobs` (201 Created, Location header, Idempotency-Key support, X-Correlation-ID)
- `GET /api/v1/jobs/{jobId}` (200 OK or 404 Not Found)
- `GET /api/v1/jobs/{jobId}/tasks` (Paginated task list)
- `GET /api/v1/jobs/{jobId}/results` (Paginated results)
- `GET /health` & `GET /ready` (Liveness and readiness checks)

## 3. Acceptance Tests (Cucumber / Gherkin)
- `job-submission.feature`
- `job-status.feature`
- `job-idempotency.feature`
- `task-query.feature`
- `result-query.feature`
- `health.feature`

## 4. Status
- **State**: `COMPLETED`
- **Tests**: 10/10 Gherkin acceptance scenarios passing (26 total test suite).
- **Branch**: `main`
