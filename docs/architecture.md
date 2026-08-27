# Enterprise Flow Engine (EFE) — Architecture Overview
## IKASAN-001 Enterprise ESB Foundation

### 1. Architectural Mission
The **Trade Reconciliation Enterprise Service Bus (ESB)** is built on the native **Ikasan Enterprise Integration Platform (EIP)** component model. It establishes an asynchronous, resilient, and decoupled processing engine designed to ingest trade reconciliation requests from external sources (e.g. Custodians, Brokers, Clearing Houses), register them into stateful workflows, dispatch discrete tasks, and process reconciliation algorithms asynchronously.

---

### 2. Platform Architecture & Layering Model

The **Enterprise Flow Engine (EFE)** is partitioned into two distinct domains: the reusable **EFE Platform** infrastructure and the domain-specific **Project Application** (Trade Reconciliation):

```text
                                   EFE PLATFORM
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        │                               │                               │
     Runtime                        Connectors                      Operations
        │                               │                               │
        ├── Flow Engine                 ├── Kafka Adapter               ├── Audit Logging
        ├── Task Execution              ├── JMS / AMQ Adapter           ├── Metrics & KPIs
        ├── Job Orchestration           ├── REST / HTTP Adapter         ├── Wiretap & Trace
        ├── Quartz Scheduler            ├── SFTP Adapter                └── Health Checks
        ├── Retry & DLQ                 └── File / S3 Adapter
        └── In-Memory / Queue SPI
                                        │
                                        ▼
                               PROJECT APPLICATION
                            (Trade Reconciliation ESB)
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             │                          │                          │
          Domain                    Processor                    Rules
             │                          │                          │
        ├── Trade                  └── TaskProcessor SPI       └── Match / Break
        ├── ReconciliationJob           └── TradeReconProcessor    └── Tolerance & Diff
        └── ReconciliationResult
```

---

### 3. High-Level Flow Execution Diagram

```text
                                CLIENT / API CONSUMER
                                         │
                                         ▼
                            HTTP POST /api/v1/jobs/reconciliation
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 │       IKASAN ENTERPRISE MODULE: TRADE-RECON-ESB│
                 └───────────────────────┬───────────────────────┘
                                         │
       ┌─────────────────────────────────┼─────────────────────────────────┐
       │                                 │                                 │
       ▼                                 ▼                                 ▼
┌─────────────────────────┐   ┌─────────────────────────┐   ┌─────────────────────────┐
│  TRADE INGESTION FLOW   │   │ RECON DISPATCH FLOW     │   │ RECON PROCESSING FLOW   │
│                         │   │                         │   │                         │
│ 1. RestConsumer         │   │ 1. ScheduledConsumer    │   │ 1. MessagingConsumer    │
│ 2. JSON Converter       │   │ 2. Task Retrieval Broker│   │ 2. Task Event Translator│
│ 3. Validation Translator│   │ 3. Task Prep Splitter   │   │ 3. Business Proc Broker │
│ 4. Job Reg Broker       │   │ 4. Messaging Producer   │   │ 4. Result Persist Broker│
│ 5. Response Producer    │   │                         │   │ 5. Result Producer      │
└────────────┬────────────┘   └────────────┬────────────┘   └────────────▲────────────┘
             │                             │                             │
             │                             ▼                             │
             │                    MESSAGING SPI BOUNDARY ────────────────┘
             │                    (In-Memory / Future Kafka/Rabbit/AMQ/Redis)
             ▼
      PERSISTENCE SPI
 (In-Memory / Future H2/PostgreSQL/Mongo)
```

---

### 3. Core Architectural Boundaries

#### A. API Boundary (`com.efe.traderecon.api`)
- Exposes clean REST contracts (`POST /api/v1/jobs/reconciliation`, `GET /api/v1/jobs/{jobId}`).
- Acts as a thin HTTP entry adapter that immediately delegates incoming payloads into the Ikasan Ingestion Flow.
- Does **not** perform business calculations or run ad-hoc thread executors.

#### B. Ikasan Flow Orchestration (`com.efe.traderecon.flow`)
- Enforces the Ikasan component model:
  - **Module**: The deployable aggregate container (`TRADE-RECON-ESB`).
  - **Flow**: An integration pipeline with a single entry Consumer.
  - **Consumer**: Entry point (REST, Scheduled, Messaging).
  - **Converter**: Object type translation without business logic.
  - **Translator**: Object enrichment and field validation.
  - **Broker**: Stateful side effects (Database access, API calls).
  - **Splitter**: Fan-out partitioning of batched payloads into discrete units of work.
  - **Producer**: Outbound terminal emission.

#### C. Messaging SPI Boundary (`com.efe.traderecon.messaging.spi`)
- Abstract interfaces (`MessagingProducer<T>`, `MessagingConsumer<T>`, `MessagingProvider`, `MessagingMessage<T>`).
- Completely decouples the dispatch and processing flows from specific queue vendors.
- Active provider: `inmemory` (bounded, thread-safe, non-blocking / backpressure supported).
- Future targets: `kafka` (IKASAN-004), `rabbitmq` (IKASAN-005), `amq` (IKASAN-006), `redis` (IKASAN-007).

#### D. Persistence SPI Boundary (`com.efe.traderecon.persistence.spi`)
- Clean repository contracts: `JobRepository`, `TaskRepository`, `TradeRepository`, `ResultRepository`.
- Active provider: `inmemory` (`ConcurrentHashMap` based thread-safe storage).
- Future targets: `h2`, `postgres`, `mongodb`.

#### E. Business Processor Boundary (`com.efe.traderecon.processor`)
- `TaskProcessor` interface: `boolean supports(String taskType)` and `TaskResult process(Task task)`.
- `TradeReconciliationProcessor`: contains pure business logic. Zero dependencies on HTTP, Messaging brokers, Quartz schedulers, or Ikasan internals.


---

### 4. EFE-006 Reliability, Recovery, DLQ & Audit Foundation

EFE-006 is now implemented as a reusable reliability foundation shared by EFE flows. It is deliberately separated from domain processors and is integrated at the asynchronous worker boundary.

```text
Flow event
   |
   v
ReliabilityService
   |
   +-- execute operation
   |      |
   |      +-- SUCCESS --> audit/wiretap --> result
   |      |
   |      +-- retryable failure --> capped exponential backoff --> retry
   |      |
   |      +-- permanent/exhausted failure --> audit/wiretap --> DeadLetterQueue
   |
   +-- ReliabilityAuditTrail
   +-- bounded DeadLetterQueue
```

#### Reliability responsibilities

| Capability | Current implementation | Architectural boundary |
|---|---|---|
| Retry | `ReliabilityService` with configurable maximum attempts and capped exponential backoff. | Platform; processors provide the operation, not retry loops. |
| Failure classification | Permanent validation/state failures stop immediately; other failures are retryable by default. Executor saturation is treated as transient. | Platform policy; can be replaced with domain-specific classification later. |
| Dead-letter handling | Bounded in-memory `DeadLetterQueue` stores terminal `ReliabilityRecord` entries. | Platform SPI today; durable broker/DLQ provider remains future work. |
| Wiretap/audit | `ReliabilityAuditTrail` records `SUCCESS`, `RETRY`, and `DLQ` transitions with event, flow, attempt, error, and timestamp data. | Operations/audit plane; never part of the business result payload. |
| Async integration | EFE-005 worker submits a fresh worker operation for each reliability attempt. | Worker boundary; avoids reusing a failed `Future`. |
| Executor lifecycle | `EfeExecutorService` performs Spring `@PreDestroy` shutdown. | Runtime lifecycle; prevents worker leakage during application/test shutdown. |

#### Configuration

```yaml
efe:
  reliability:
    max-retries: 3
    initial-delay-ms: 100
    backoff-multiplier: 2.0
    max-delay-ms: 10000
    dlq-capacity: 1000
```

The current implementation is a **foundation**, not a production delivery guarantee. The DLQ is process-local, and durable acknowledgement, outbox/inbox, broker-specific recovery, replay authorization, and persistent audit storage remain required before production deployment.

#### EFE-006 verification

The current branch verifies EFE-006 with focused tests for transient fail-once recovery, permanent failure without retry, exhausted transient failure to DLQ, and audit transition records. The full Maven suite passes with **107 tests, 0 failures, 0 errors, and 0 skipped tests**.

#### Operational and security implications

DLQ inspection and replay must be exposed only through a private management plane with OAuth2/OIDC authorization, privileged scopes, tenant controls, and immutable audit records. No public business API should directly mutate or replay DLQ entries. Future durable providers must preserve the same event ID, correlation ID, attempt count, causation ID, and audit semantics.

---

### 5. Current implementation status and next boundary

EFE-001 through EFE-006 are complete at the demonstrator/foundation level. EFE-007 is the next planned capability. The next reliability hardening boundary is durable delivery: persistent DLQ, transactional outbox/inbox, idempotent replay, broker acknowledgements, and restart recovery. These concerns must extend the existing reliability contracts rather than introducing a second orchestration or recovery framework.
