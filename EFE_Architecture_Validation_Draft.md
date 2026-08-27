# Enterprise Flow Engine (EFE) — Architecture Validation Draft

**Document Version:** 1.0.0-DRAFT  
**Date:** 2026-08-27  
**Status:** Under Architectural Review  
**Target Reviewers:** Antigravity, Manus, OpenCode, Enterprise Integration Architects  

---

## Executive Summary

The **Enterprise Flow Engine (EFE)** is an enterprise integration and event-orchestration platform designed for mission-critical financial workflows (e.g., Corporate Actions, Trade Reconciliation, Elective Processing, AI-assisted Audit). 

This document defines the frozen architectural model separating:
1. **The Reusable EFE Platform / Foundation**: Standard runtime, multi-protocol ingress (REST, gRPC, GraphQL), Ikasan flow orchestration, execution worker pools, JMX management, persistence SPIs, and pluggable local AI capabilities.
2. **Autonomous Deployable Domain Modules**: Independently versioned, containerized Spring Boot applications running as standalone Kubernetes Pods (e.g., `efe-corporate-actions`, `efe-reconciliation`, `efe-electives`) sharing identical EFE/Ikasan conventions.

---

## 1. High-Level Target Architecture

```
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

### Architectural Principles:
* **Not a Monolith**: Every domain module compiles into its own executable Spring Boot JAR and container image.
* **Autonomous Scaling**: Corporate Actions pod count (e.g., 3 pods) scales independently from Reconciliation (e.g., 5 pods).
* **Single Orchestrator Rule**: Ikasan remains the sole flow engine. EFE never builds a competing execution engine.

---

## 2. Ikasan Flow Standard (Universal Pattern)

Every executable workflow inside any EFE module strictly adheres to the Ikasan component model:

```
                CONSUMER  (Exactly ONE per flow)
                   │
                   ▼
         CONVERTER / TRANSLATOR
                   │
                   ▼
     ROUTER / SPLITTER / FILTER / BROKER / PROCESSOR
                   │
                   ▼
                PRODUCER  (Terminal Endpoint)
```

### Component Roles & Custom Extensibility:
| Component | Ikasan Interface | Responsibility in EFE |
| :--- | :--- | :--- |
| **Consumer** | `IkasanConsumer<E>` | Single ingress endpoint (REST, Scheduled Quartz, Kafka, JMS, File) |
| **Converter** | `IkasanConverter<S, T>` | Structural format transformation (e.g., JSON → Domain DTO) |
| **Translator** | `IkasanTranslator<E>` | Content enrichment, business validation, header injection |
| **Splitter** | `IkasanSplitter<S, T>` | Partitioning batch payloads into discrete tasks |
| **Broker** | `IkasanBroker<S, T>` | State queries, DB interactions, external synchronous service lookups |
| **Processor** | `TaskProcessor` | Core financial/domain rule computation |
| **Router** | `IkasanRouter<E>` | Conditional dispatching (e.g., `PASS` vs `MANUAL_REVIEW`) |
| **Producer** | `IkasanProducer<E>` | Outbound delivery (Kafka, MQ, DB, HTTP, In-Memory) |

---

## 3. Platform Capability Pack (EFE Core)

The EFE platform provides modular, ready-made capabilities consumable by any domain flow:

```
EFE PLATFORM
│
├── API ADAPTERS
│   ├── REST (/api/v1/jobs)
│   ├── gRPC (Protobuf Service Adapter)
│   └── GraphQL (/graphql - Queries & Mutations)
│
├── FLOW ENGINE (Ikasan 5.0.x Runtime)
│   ├── Consumer, Converter, Translator
│   ├── Broker, Splitter, Filter, Router
│   └── Producer, Processor
│
├── EXECUTION RUNTIME
│   ├── Bounded Worker Pool (EfeExecutorService: core, max, queue capacity)
│   ├── Task Rejection & Backpressure Policy
│   └── Quartz-driven Scheduled Flow Consumer
│
├── PERSISTENCE & MESSAGING SPIS
│   ├── Pluggable Persistence (H2 In-Memory, JDBC, JPA)
│   └── Pluggable Messaging (In-Memory Queue, Kafka, RabbitMQ, JMS/AMQ, Redis)
│
├── MANAGEMENT & OPERATIONAL CONTROL (JMX Domain: com.efe)
│   ├── Module MBean (Module state, flow listing, start/stop)
│   ├── Flow MBean (Flow lifecycle management)
│   ├── Executor MBean (Pool size, active threads, queue depth, completed/rejected)
│   └── Scheduler MBean (Intervals, last/next execution, manual trigger)
│
└── INTELLIGENCE LAYER (Optional AI/ML Plugin)
    ├── Intelligence SPI (IntelligenceProvider, Registry, Router)
    ├── Local Runtime Client (Ollama / Local rules)
    ├── PII Data Sanitizer (Configurable field masking)
    └── Aggregator & Non-Crashing Error Propagation
```

---

## 4. Autonomous Domain Flow Examples

### 4.1 Corporate Actions Module (`efe-corporate-actions`)
```
[EVENT-INGEST-FLOW]
  SWIFT/ISO20022 Consumer ──► MT564 Converter ──► Validation Translator ──► CA Event Producer

[ENTITLEMENT-FLOW]
  Scheduled Consumer ──► Position Retrieval Broker ──► Entitlement Processor ──► Outbound Producer

[ELECTION-FLOW]
  REST/Kafka Consumer ──► Election Validator ──► Election Processor ──► Settlement Producer
```

### 4.2 Trade Reconciliation Module (`efe-reconciliation`)
```
[TRADE-INGEST-FLOW]
  REST Consumer ──► JSON Converter ──► Validation Translator ──► Job Registration Broker ──► Response Producer

[DISPATCH-FLOW]
  Scheduled Consumer ──► Task Retrieval Broker ──► Batch Splitter ──► Messaging Dispatch Producer

[PROCESSING-FLOW]
  Messaging Consumer ──► Normalizer ──► Matching Processor ──► Result DB Broker ──► Result Producer
```

### 4.3 AI-Assisted Audit Workflow (Pluggable Component)
```
  Trade/CA Event Consumer
             │
             ▼
     JSON Converter
             │
             ▼
     Business Processor
             │
             ▼
   AI Audit Processor  (Optional Ollama / Local Model)
             │
             ▼
      Decision Router
         ├── PASS     ──► Ingestion Producer
         └── REVIEW   ──► Manual Review Queue Producer
```

---

## 5. Externalized Connector Configuration

Business processors remain 100% agnostic of the underlying physical transport. Connectors are swapped purely via external YAML configuration:

#### Option A: Kafka Transport
```yaml
ca:
  outbound:
    provider: kafka
    kafka:
      topic: corporate-actions.entitlements
      bootstrap-servers: kafka-cluster:9092
```

#### Option B: ActiveMQ / JMS Transport
```yaml
ca:
  outbound:
    provider: amq
    amq:
      destination: corporate.actions.queue
      broker-url: tcp://activemq-broker:61616
```

---

## 6. Repository Layout & Target Monorepo Structure

```
enterprise-flow-engine/
│
├── efe-platform/                  # Core Reusable Framework
│   ├── efe-core/                  # Domain contracts, SPIs, Exceptions
│   ├── efe-ikasan/                # Ikasan engine, Module/Flow builders
│   ├── efe-api/                   # REST, gRPC, GraphQL adapters
│   ├── efe-execution/             # Bounded thread pools, Quartz schedulers
│   ├── efe-connectors/            # In-Memory, Kafka, JMS, RabbitMQ, Redis SPIs
│   ├── efe-ai/                    # Intelligence SPI, Ollama client, Sanitizer
│   └── efe-management/            # JMX MBeans (com.efe), Health, Metrics
│
├── modules/                       # Autonomous Deployable Services
│   ├── efe-corporate-actions/     # Corporate Actions Spring Boot application
│   ├── efe-reconciliation/        # Trade Reconciliation Spring Boot application
│   └── efe-electives/             # Electives Spring Boot application
│
├── templates/
│   └── efe-module-template/       # Bootstrap template for new microservices
│
├── deploy/                        # Deployment manifests
│   └── k8s/                       # Base & overlay Helm / K8s manifests
│
└── beads/                         # Architecture & Specification Beads
```

---

## 7. Verification & Acceptance Criteria

Every capability and domain module must be backed by **Cucumber/Gherkin acceptance tests** testing from the external boundary:

1. **REST API**: Status codes, JSON contracts, `Idempotency-Key`, `X-Correlation-ID`.
2. **gRPC API**: `SubmitJob`, `GetJob` adapters.
3. **GraphQL API**: Querying `job`, `tasks`, `results`, and `submitJob` mutations.
4. **JMX Management**: Querying real `com.efe` MBeans via `MBeanServer`.
5. **Database**: CRUD persistence and retrieval via Ikasan Brokers.
6. **Async Execution**: Verified bounded parallel execution with zero dropped tasks.
7. **AI Layer**: Correct routing, error code return on unreachable models, graceful pass when disabled.

---

## 8. Validation Questions for Reviewers

Please review the following architectural decisions and provide recommendations:

1. **Platform vs Module Isolation**: Is the boundary between `efe-platform` (reusable library/SDK) and `modules/` (independently deployable Spring Boot apps) cleanly isolated?
2. **Multi-Protocol Ingress**: Does delegating REST, gRPC, and GraphQL through the same underlying Ikasan Consumer / Application Service prevent domain logic duplication effectively?
3. **Operational Visibility**: Does the dedicated `com.efe` JMX domain and MBean structure provide sufficient runtime control for Kubernetes container orchestration?
4. **AI Insertion**: Is treating AI as an optional `IkasanBroker` / `IkasanProcessor` sufficient for governance, or should it be separated into an out-of-process sidecar pattern for heavy loads?
