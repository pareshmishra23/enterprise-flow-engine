# Enterprise Flow Engine (EFE)
> Enterprise Integration & Asynchronous Processing Platform (Ikasan 5.0.x Architecture)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%20%2F%204.1-green.svg)](https://spring.io/projects/spring-boot)
[![Ikasan](https://img.shields.io/badge/Ikasan-5.0.x-blue.svg)](https://github.com/ikasanEIP/ikasan)

---

## 1. Overview

**EFE (Enterprise Flow Engine)** is a high-throughput, decoupled enterprise integration platform designed for complex asynchronous workflows such as **Trade Reconciliation** and **Corporate Actions**. Built upon the **Ikasan Enterprise Integration Platform (EIP)** component model, EFE establishes clean separation between platform runtime, transport connectors, observability, and domain processors.

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

## 2. Core Ikasan Flows (`trade-recon-esb-module`)

```text
                                  TRADE-RECON-ESB MODULE
                                             │
         ┌───────────────────────────────────┼───────────────────────────────────┐
         │                                   │                                   │
         ▼                                   ▼                                   ▼
┌─────────────────────────┐         ┌─────────────────────────┐         ┌─────────────────────────┐
│  TRADE INGESTION FLOW   │         │ RECON DISPATCH FLOW     │         │ RECON PROCESSING FLOW   │
│                         │         │                         │         │                         │
│ 1. RestConsumer         │         │ 1. ScheduledConsumer    │         │ 1. MessagingConsumer    │
│ 2. JSON Converter       │         │ 2. Task Retrieval Broker│         │ 2. Task Event Translator│
│ 3. Validation Translator│         │ 3. Task Prep Splitter   │         │ 3. Business Proc Broker │
│ 4. Job Reg Broker       │         │ 4. Messaging Producer   │         │ 4. Result Persist Broker│
│ 5. Response Producer    │         │                         │         │ 5. Result Producer      │
└────────────┬────────────┘         └────────────┬────────────┘         └────────────▲────────────┘
             │                                   │                                   │
             │                                   ▼                                   │
             │                          MESSAGING SPI BOUNDARY ──────────────────────┘
             │                          (In-Memory Active / Bounded Queue)
             ▼
      PERSISTENCE SPI
  (In-Memory Active Storage)
```

---

## 3. Documentation Suite

- [Architecture Overview](docs/architecture.md)
- [Ikasan Module Specification](docs/ikasan-module.md)
- [Flow Design Specification](docs/flow-design.md)
- [Messaging SPI & Adapter Roadmap](docs/messaging-spi.md)
- [Persistence SPI & Storage Model](docs/persistence-spi.md)
- [Configuration Architecture](docs/configuration.md)
- [Master Bead Roadmap (EFE-001 to EFE-018)](docs/future-roadmap.md)

---

## 4. Quick Start

### Build & Test
```bash
mvn clean test
```

### Run Locally
```bash
mvn spring-boot:run
```

- **Interactive Ikasan Dashboard**: [http://localhost:8080/ikasan/](http://localhost:8080/ikasan/)
- **Module Telemetry API**: `GET http://localhost:8080/api/v1/ikasan/module`
- **Submit Trade Job**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/jobs/reconciliation \
    -H "Content-Type: application/json" \
    -d '{
      "businessDate": "2026-08-27",
      "source": "CUSTODIAN",
      "records": [
        {"tradeId": "TR-1001", "accountId": "ACC-01", "securityId": "AAPL", "quantity": 100.0, "price": 220.50}
      ]
    }'
  ```

---

## 5. Master Roadmap

- **EFE-001** — Platform foundation / Ikasan module *(Completed)*
- **EFE-002** — Generic Flow definition
- **EFE-003** — Generic Job & Task execution
- **EFE-004** — Scheduler
- **EFE-005** — Messaging SPI + In-Memory
- **EFE-006** — Worker execution
- **EFE-007** — Retry / Idempotency / DLQ model
- **EFE-008** — Persistence SPI
- **EFE-009** — REST API framework
- **EFE-010** — Observability / Operations
- **EFE-011** — Kafka plugin
- **EFE-012** — RabbitMQ plugin
- **EFE-013** — AMQ/JMS plugin
- **EFE-014** — Redis Streams plugin
- **EFE-015** — Camel integration
- **EFE-016** — Project template / bootstrap
- **EFE-017** — Reconciliation example
- **EFE-018** — Corporate Action example
