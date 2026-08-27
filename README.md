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
                     (Reusable Foundation)
                              │
                ┌─────────────┴─────────────┐
                │                           │
         EFE Runtime / SDK            EFE Components
                │                           │
         Ikasan Runtime             REST / SFTP / JMS
         Configuration              Kafka / Redis / etc.
         Operations / JMX           Common Utilities
                │
     ═════════════════════════════════════════════════════
     │              │               │            │
     ▼              ▼               ▼            ▼
 Corporate      Trade Recon     Elective      AI Audit
  Actions         Module         Module        Module
  Module            │               │            │
     │         Spring Boot     Spring Boot  Spring Boot
Spring Boot         │               │            │
     │         Ikasan Module   Ikasan Module Ikasan Module
Ikasan Module       │               │            │
     │            Flows           Flows        Flows
   Flows            │               │            │
     │        Container/Pod   Container/Pod Container/Pod
Container/Pod
```

---

## 2. Universal Ikasan Flow Pipeline

Every integration process in EFE follows the standard native Ikasan component graph:

```text
                CONSUMER  (Exactly ONE per flow)
                   │
                   ▼
         CONVERTER / TRANSLATOR
                   │
                   ▼
     ROUTER / SPLITTER / FILTER / BROKER / PROCESSOR
                   │
                   ▼
                PRODUCER  (Terminal Endpoint or Route Destinations)
```

---

## 3. Core Flow Demonstrator (`efe-core-flow`)

```text
                         EFE PLATFORM
                              │
                        efe-core-flow
                              │
                         EFE-CORE-IN (Consumer)
                              │
                              ▼
                     EFE-CORE-CONVERTER (JSON -> EfeCoreEvent)
                              │
                              ▼
                     EFE-CORE-VALIDATOR (Translator Validation)
                              │
                              ▼
                     EFE-CORE-PROCESSOR (Match vs Break Evaluation)
                              │
                              ▼
                       EFE-CORE-ROUTER (Dynamic Router)
                          /        \
                         /          \
                      MATCH        BREAK
                        │             │
                        ▼             ▼
                 EFE-MATCH-OUT   EFE-BREAK-OUT (Producers)
```

---

## 4. Asynchronous Execution Flow (`efe-async-flow`)

```text
                         EFE PLATFORM
                              │
                        efe-async-flow
                              │
                 EFE-ASYNC-SCHEDULED-IN (Scheduled Consumer)
                              │
                              ▼
                 EFE-ASYNC-TASK-BROKER (Batch Retrieval Broker)
                              │
                              ▼
                 EFE-ASYNC-SPLITTER (Batch Partitioning)
                              │
                              ▼
                 EFE-ASYNC-WORKER-PROCESSOR (Bounded EfeExecutorService)
                              │
                              ▼
                 EFE-ASYNC-OUT (Terminal Result Producer)
```

---

## 5. Quick Start

### Build & Run Tests
```bash
mvn clean test
```

### Run Locally
```bash
mvn spring-boot:run
```

- **Enterprise Flow Engine Console**: [http://localhost:8080/](http://localhost:8080/)
- **Module Telemetry API**: `GET http://localhost:8080/api/v1/ikasan/module`
- **Submit Core Event**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/core/events \
    -H "Content-Type: application/json" \
    -d '{
      "eventId": "E-1001",
      "type": "TRADE",
      "expectedQuantity": 100,
      "actualQuantity": 100
    }'
  ```
- **Submit Reconciliation Job**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/jobs \
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

## 6. Master Roadmap & Bead Status

| Bead | Title | Category | Status | Summary |
| :--- | :--- | :--- | :--- | :--- |
| **EFE-001** | Initial Scaffold | *Runtime* | **COMPLETED** | Spring Boot foundation, domain entities, web console. |
| **EFE-002** | REST + Cucumber | *Connectors* | **COMPLETED** | Reusable REST `/api/v1/jobs` contract, idempotency, Cucumber specs. |
| **EFE-003** | Real Ikasan Foundation | *Runtime* | **COMPLETED** | Real Ikasan Module, Flows, Consumers, Processors, Brokers, and Routers. |
| **EFE-004** | Core Flow Demonstrator | *Flows* | **COMPLETED** | Canonical `efe-core-flow` with MATCH/BREAK routing and REST trigger. |
| **EFE-005** | Async Execution | *Execution* | **COMPLETED** | Scheduled Consumer → Task Retrieval → Splitter → Bounded Worker Pool → Producer. |
| **EFE-006** | Reliability | *Operations* | **COMPLETED — FOUNDATION** | Classified retry/backoff, in-memory DLQ, wiretap/audit records, async worker integration, and executor shutdown; 107/107 tests passing. |
| **EFE-007** | Optional AI Component | *Intelligence* | **NEXT** | AI Processor → Ollama Local Runtime + PII Sanitizer. |
| **EFE-008** | JMX / Operations | *Management* | **PLANNED** | JMX `com.efe` management plane for Module, Flow, Scheduler, Executor. |
| **EFE-009** | GraphQL | *API* | **PLANNED** | GraphQL queries and mutations layer. |
| **EFE-010** | gRPC | *API* | **PLANNED** | gRPC Protobuf high-performance adapter. |
| **EFE-011** | Connector Pack | *Connectors* | **PLANNED** | Transport connectors: Kafka, RabbitMQ, AMQ/JMS, Redis Streams. |
| **EFE-012** | Camel Integration | *Integration* | **PLANNED** | Apache Camel component mediation in Ikasan flows. |
| **EFE-013** | Corporate Action Module | *Domain* | **PLANNED** | Autonomous `efe-corporate-actions` microservice. |
| **EFE-014** | Reconciliation Module | *Domain* | **PLANNED** | Autonomous `efe-reconciliation` microservice. |
| **EFE-015** | Electives Module | *Domain* | **PLANNED** | Autonomous `efe-electives` microservice. |
| **EFE-016** | Docker / K8s Production Packaging | *Deployment* | **PLANNED** | Multi-module containers, Helm charts, and K8s manifests. |


## 7. EFE-006 Reliability Foundation

EFE-006 provides reusable reliability behavior for asynchronous flow operations. It is integrated through `ReliabilityService` and keeps retry policy, failure classification, DLQ handling, and audit recording outside domain processors.

```text
Async worker operation
        |
        v
  ReliabilityService
    |       |       |
 SUCCESS  RETRY    DLQ
    |       |       |
  Audit  Backoff  Audit + DeadLetterQueue
```

Current defaults are configured in `src/main/resources/application.yml`:

```yaml
efe:
  reliability:
    max-retries: 3
    initial-delay-ms: 100
    backoff-multiplier: 2.0
    max-delay-ms: 10000
    dlq-capacity: 1000
```

The foundation includes classified retry behavior, capped exponential backoff, a bounded in-memory DLQ, `ReliabilityAuditTrail` records for `SUCCESS`, `RETRY`, and `DLQ`, fresh worker submission on each retry, and clean executor shutdown through Spring lifecycle management.

The verified suite currently passes with **107 tests, 0 failures, 0 errors, and 0 skipped tests**:

```bash
mvn -B clean test
```

This is a foundation rather than a production delivery guarantee. Durable broker acknowledgement, persistent DLQ storage, transactional outbox/inbox, restart recovery, authenticated replay, and persistent audit storage remain future hardening work under the reliability and connector roadmap. DLQ operations must eventually be isolated behind a private OAuth2/OIDC-protected management plane.
