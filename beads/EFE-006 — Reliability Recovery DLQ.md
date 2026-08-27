# EFE-006 — Reliability, Recovery, Retry, DLQ, and Audit

## Status: COMPLETED — FOUNDATION

EFE-006 adds a reusable reliability service to the current flow platform. The implementation is intentionally an in-memory foundation; durable broker recovery and production DLQ persistence remain future hardening work.

## Implemented capabilities

| Capability | Implementation |
|---|---|
| Retry | Configurable maximum attempts with capped exponential backoff. |
| Failure classification | Permanent validation/state failures stop immediately; other failures are retryable by default. |
| DLQ | Bounded in-memory `DeadLetterQueue` stores terminal `ReliabilityRecord` events. |
| Wiretap/audit | `ReliabilityAuditTrail` records SUCCESS, RETRY, and DLQ transitions. |
| Async integration | EFE-005 worker processor submits a fresh worker operation for every reliability attempt. |
| Lifecycle | Bounded executor has Spring `@PreDestroy` shutdown behavior. |
| Configuration | `efe.reliability.max-retries`, `initial-delay-ms`, `backoff-multiplier`, `max-delay-ms`, and `dlq-capacity`. |

## Verification

`mvn -B clean test` on the feature branch passes with **107 tests, 0 failures, 0 errors**. Focused unit coverage verifies transient fail-once recovery, permanent failure without retry, exhausted transient failure to DLQ, and audit status transitions.

## Known limitations

The DLQ is currently process-local and must be replaced or backed by a durable endpoint before production use. Flow-level recovery, message acknowledgement, outbox/inbox, replay authorization, and broker-specific delivery semantics remain future work. EFE-005 still demonstrates bounded worker submission rather than a full durable parallel processing guarantee.
