# EFE-007 — Retry, Idempotency & DLQ Model

## 1. Objective
Exponential backoff, jittered retries, deterministic deduplication tokens, and Dead Letter Queue (DLQ) routing.

## 2. Status
- **State**: `SUPERSEDED`

This legacy numbering assigned retry/idempotency/DLQ to EFE-007. Retry and DLQ foundation work is now covered by canonical `EFE-006 — Reliability Recovery DLQ.md`. Durable outbox/inbox, replay, and broker recovery remain future hardening.
