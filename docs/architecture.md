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
