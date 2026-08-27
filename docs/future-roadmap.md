# EFE Master Execution Roadmap (EFE-001 to EFE-018)

## 1. Master Bead Sequence

| Bead ID | Title | Pillar | Scope & Milestone Description |
| :--- | :--- | :--- | :--- |
| **EFE-001** | **Platform foundation / Ikasan module** | *Runtime* | **[COMPLETED]** Foundational Spring Boot module, domain baseline, in-memory SPI skeletons, REST entrypoint, and UI console. |
| **EFE-002** | **REST + Cucumber facility** | *Connectors* | **[COMPLETED]** Generic REST job contract, idempotency/correlation headers, queries, health/readiness, and acceptance tests. |
| **EFE-003** | **Real Ikasan foundation** | *Runtime* | **[COMPLETED]** Ikasan component contracts, Module/Flow builders, foundation flows, router branching, and runtime console proof. |
| **EFE-004** | **Core flow demonstrator** | *Flows* | **[COMPLETED]** REST Consumer → Converter → Validator → Processor → Router → MATCH/BREAK Producers. |
| **EFE-005** | **Async execution** | *Execution* | **[COMPLETED]** Scheduled Consumer → Task Broker → Splitter → bounded worker processor → Producer; 104/104 tests passing. |
| **EFE-006** | **Reliability / recovery / DLQ** | *Operations* | **[COMPLETED — FOUNDATION]** Classified retry/backoff, in-memory DLQ, wiretap/audit, async worker integration, executor shutdown; 107/107 tests passing. |
| **EFE-007** | **Optional AI component** | *Intelligence* | **[NEXT]** Optional AI processor with local Ollama runtime, PII sanitizer, structured output, timeout, and fallback policy. |
| **EFE-008** | **JMX / operations** | *Operations* | Module, Flow, Scheduler, Executor, and Messaging management with a private management plane. |
| **EFE-009** | **GraphQL API** | *Connectors* | Optional query/mutation adapter delegating to common application services with query-complexity controls. |
| **EFE-010** | **gRPC API** | *Connectors* | Deferred machine-to-machine adapter using protobuf and common application services. |
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
