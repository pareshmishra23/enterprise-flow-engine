# EFE Master Execution Roadmap (EFE-001 to EFE-018)

## 1. Master Bead Sequence

| Bead ID | Title | Pillar | Scope & Milestone Description |
| :--- | :--- | :--- | :--- |
| **EFE-001** | **Platform foundation / Ikasan module** | *Runtime* | **[COMPLETED]** Foundational Ikasan module architecture, core flow lifecycle, In-Memory SPI skeletons, REST entrypoint, and UI console. |
| **EFE-002** | **Generic Flow definition** | *Runtime* | Universal DSL and metadata-driven declarative flow definition framework with dynamic element wiring. |
| **EFE-003** | **Generic Job & Task execution** | *Runtime* | Generic state machine for Jobs & Tasks, batch partitioning, task state transitions, and execution context. |
| **EFE-004** | **Scheduler** | *Runtime* | Quartz enterprise scheduler integration with cron triggers, calendar exclusions, and clustered job execution. |
| **EFE-005** | **Messaging SPI + In-Memory** | *Connectors* | Enhanced bounded concurrency model, backpressure thresholds, multi-topic routing, and priority queueing. |
| **EFE-006** | **Worker execution** | *Runtime* | Dynamic worker thread pool management, asynchronous task execution workers, and CPU/IO isolation. |
| **EFE-007** | **Retry / Idempotency / DLQ model** | *Runtime* | Exponential backoff, jittered retries, deterministic deduplication tokens, and Dead Letter Queue (DLQ) routing. |
| **EFE-008** | **Persistence SPI** | *Runtime* | Pluggable persistence providers with transactional semantics (H2, PostgreSQL, MongoDB). |
| **EFE-009** | **REST API framework** | *Connectors* | Generic REST ingestion framework, OpenAPI/Swagger 3 specs, async job submission, and webhook callbacks. |
| **EFE-010** | **Observability / Operations** | *Operations* | Distributed tracing (OpenTelemetry/W3C context), Prometheus metrics, audit trail wiretap, and alerts. |
| **EFE-011** | **Kafka plugin** | *Connectors* | Production Kafka producer/consumer adapters, partition key routing, consumer group rebalance handlers. |
| **EFE-012** | **RabbitMQ plugin** | *Connectors* | AMQP 0-9-1 exchange bindings, publisher confirms, channel pooling, and dead-letter exchanges. |
| **EFE-013** | **AMQ/JMS plugin** | *Connectors* | Jakarta JMS 3.x provider for Apache ActiveMQ / Artemis, transactional sessions, and XA support. |
| **EFE-014** | **Redis Streams plugin** | *Connectors* | Redis Streams consumer groups, `XACK`, pending entry list (`XPENDING`) claiming, and stream trimming. |
| **EFE-015** | **Camel integration** | *Connectors* | Apache Camel enterprise connector bridging SFTP, AS2, FIX protocol, and SWIFT MT/MX to EFE flows. |
| **EFE-016** | **Project template / bootstrap** | *Platform* | Maven archetype and CLI generator (`efe-starter`) for scaffolding new enterprise integration modules. |
| **EFE-017** | **Reconciliation example** | *Application* | Full high-volume Trade Reconciliation implementation (10K+ records, multi-source match, tolerance algorithms). |
| **EFE-018** | **Corporate Action example** | *Application* | Corporate Action processing engine (dividend payouts, stock splits, entitlement calculations, ledger postings). |

---

## 2. Apache Camel Integration Architecture (Target: IKASAN-008)

Apache Camel serves as an external transport adapter / protocol connectivity layer, while Ikasan orchestrates the core module and flow lifecycle:

```text
                  EXTERNAL CHANNELS (SFTP / AS2 / FIX / SWIFT)
                                        │
                                        ▼
                        APACHE CAMEL CONNECTIVITY LAYER
                                        │
                                        ▼
                           CANONICAL INTEGRATION EVENT
                                        │
                                        ▼
                       IKASAN TRADE INGESTION FLOW
                                        │
                                        ▼
                                DOMAIN WORKFLOW
```

**Constraint**: Camel is an edge transport integration layer and is **never** embedded directly within the business `TradeReconciliationProcessor`.
