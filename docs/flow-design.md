# Ikasan Flow Design Specification

> EFE implements the Ikasan component model (see **ADR-001**). The module runs 12 flows; the three reconciliation flows and the EFE-010 reliability demo are specified here.

## 1. Flow 1: `trade-ingestion-flow`

### Purpose
Accepts a trade reconciliation request via REST/HTTP, converts and validates the payload, generates a `jobId`, registers the job and initial task in persistence, and returns an HTTP 201 response.

```text
HTTP Request
    │
    ▼
RestConsumer (Entry Point)
    │
    ▼
ReconciliationJobJsonConverter (converts JSON/Map to ReconciliationJobRequest)
    │
    ▼
ValidationTranslator (validates businessDate, source, records)
    │
    ▼
JobRegistrationBroker (persists Job in SUBMITTED state, saves Trades, creates Task)
    │
    ▼
JobRegistrationResponseProducer (produces HTTP 201 ReconciliationJobResponse)
```

**Key Constraint**: The flow registers work asynchronously and does **not** execute reconciliation synchronously in the request thread.

---

## 2. Flow 2: `reconciliation-dispatch-flow`

### Purpose
Periodically checks for pending tasks that are ready for asynchronous processing and dispatches them across the Messaging SPI boundary.

```text
ScheduledTaskConsumer (Quartz / Scheduled trigger at configurable interval)
    │
    ▼
TaskRetrievalBroker (queries TaskRepository for status == PENDING)
    │
    ▼
TaskPreparationSplitter (marks task DISPATCHED, increments attempt count, splits into discrete items)
    │
    ▼
MessagingDispatchProducer (wraps in MessagingMessage<Task> and publishes to trade.recon.tasks destination)
```

**Key Constraint**: Dispatch interval is configurable via `scheduler.reconciliation-dispatch.interval-ms` (default 5000ms).

---

## 3. Flow 3: `reconciliation-processing-flow`

### Purpose
Consumes asynchronous tasks from the messaging boundary, invokes the decoupled business reconciliation processor, persists matching results, and updates job completion metrics.

```text
MessagingProcessingConsumer (subscribes to trade.recon.tasks)
    │
    ▼
TaskEventTranslator (unpacks message, marks task status = PROCESSING)
    │
    ▼
TaskProcessingBroker -> TradeReconciliationProcessor (pure business logic)
    │
    ▼
ResultPersistenceBroker (persists ReconciliationResults, updates Task & Job to COMPLETED)
    │
    ▼
ProcessingResultProducer (records final completion metrics)
```

**Key Constraint**: The business processor `TradeReconciliationProcessor` has zero awareness of messaging queues, threads, or Ikasan framework internals.

---

## 4. Flow 12: `reliability-demo-flow` (EFE-010)

### Purpose
Demonstrates flow-level reliability (retry/backoff/DLQ) end to end through the EFE flow engine, plus wiretap observability. Business processors stay infrastructure-agnostic; the flow wrapper owns reliability.

```text
RELIABILITY-IN (Consumer: REST trigger via /api/v1/reliability/messages)
    │
    ▼
onConsumerEvent --> ReliabilityService.execute (flow-level wrapper)
    │
    ▼
RELIABILITY-DEALER (Processor: transiently fails once, then succeeds)
    │                          │
    │  retryable failure       │  success
    │  --> capped backoff      ▼
    │  --> RETRY          RELIABILITY-OUT (Producer)
    │
    └-- permanent/exhausted --> DeadLetterQueue --> audit RETRY/DLQ
```

**Key Constraints**:
- Reliability is configured via `FlowBuilder.reliable(ReliabilityService, eventIdExtractor)`; the processor never runs its own retry loop.
- A `failingPermanent` payload is classified as a permanent failure, routed straight to the DLQ without retry.
- Every event forwards to the flow's wiretap hook; the `FlowWiretapStore` and `ReliabilityAuditTrail` are exposed via `GET /api/v1/ikasan/observability`.
