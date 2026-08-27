# EFE-003 — Platform Capability Pack

## Status: IMPLEMENTED

## Objective

Enhance the existing Enterprise Flow Engine (EFE) into a **single, runnable Spring Boot platform module** containing reusable integration capabilities that can be attached to Ikasan flows.

The platform must provide ready-made capabilities for:

* REST API
* gRPC
* GraphQL
* Database access
* JMX management/control
* asynchronous execution
* executor/worker pools
* scheduling
* messaging endpoints
* optional local AI/LLM processing

Every capability must integrate into the **Ikasan Module → Flow → Consumer → Components → Producer** architecture.

This bead must also contain **real executable examples and Cucumber acceptance tests** proving that the capabilities work.

---

# 1. Core Architecture

The application remains a **single Spring Boot application**.

```text
                         EFE
              Enterprise Flow Engine
                   Spring Boot
                        |
                  Ikasan Runtime
                        |
                  EFE Module
                        |
             +----------+----------+
             |          |          |
             v          v          v
           FLOW        FLOW       FLOW
             |          |          |
         Consumer    Consumer   Consumer
             |          |          |
        Components  Components Components
             |          |          |
         Producer    Producer   Producer
```

EFE must not create a second orchestration engine beside Ikasan.

---

# 2. Ikasan Flow Rule

Every executable integration path must follow the Ikasan model:

```text
CONSUMER
   |
   v
CONVERTER / TRANSLATOR
   |
   v
ROUTER / SPLITTER / FILTER / BROKER / PROCESSOR
   |
   v
PRODUCER / ENDPOINT
```

Not every flow must contain every component.

---

# 3. Platform Capability Groups

EFE exposes the following capabilities:

```text
EFE
│
├── API
│   ├── REST (/api/v1/jobs)
│   ├── gRPC (EfeJobGrpcAdapter)
│   └── GraphQL (/graphql)
│
├── FLOW
│   ├── Consumer (IkasanConsumer)
│   ├── Converter (IkasanConverter)
│   ├── Translator (IkasanTranslator)
│   ├── Router (IkasanRouter)
│   ├── Splitter (IkasanSplitter)
│   ├── Filter (IkasanFilter)
│   ├── Broker (IkasanBroker)
│   ├── Processor (TaskProcessor)
│   └── Producer (IkasanProducer)
│
├── EXECUTION
│   ├── Executor (EfeExecutorService)
│   ├── Worker (efe.execution.workers)
│   └── Scheduler (ScheduledTaskConsumer)
│
├── CONNECTIVITY
│   ├── Database (H2 / DatabaseAccessBroker)
│   ├── REST
│   ├── JMS/AMQ
│   ├── Kafka
│   ├── RabbitMQ
│   ├── Redis
│   ├── File
│   └── SFTP
│
├── MANAGEMENT (JMX Domain: com.efe)
│   ├── JMX (EfeModuleMBean, EfeExecutorMBean, EfeSchedulerMBean, EfeMessagingMBean)
│   ├── Health (/api/v1/health)
│   └── Metrics
│
└── INTELLIGENCE
    └── Optional AI/LLM (Intelligence SPI, Ollama local runtime, local anomaly & fraud rules)
```

---

# 4. Registered Demonstration Flows in Ikasan Module

```text
EFE Module (trade-recon-esb)
│
├── rest-demo-flow (trade-ingestion-flow)
│   REST Consumer → JSON Converter → Validation Translator → Job Registration Broker → Response Producer
│
├── async-demo-flow
│   Scheduled Consumer → Task Retrieval Broker → Splitter → Async Worker Processor → Result Producer
│
├── ai-demo-flow (intelligence-audit-flow)
│   Event Consumer → Intelligence Router Broker → Result Producer Adapter
│
└── db-demo-flow
    Scheduled Consumer → Database Access Broker → DB Result Producer
```

---

# 5. Executable Cucumber Acceptance Tests

Features located in `src/test/resources/features/`:
- `rest_api.feature` — REST Job submission & status query
- `grpc_api.feature` — gRPC SubmitJob & GetJob adapters
- `graphql_api.feature` — GraphQL job, tasks, results queries & submitJob mutation
- `jmx_management.feature` — Local MBeanServer module status & executor metrics query
- `database.feature` — H2 database broker persist and retrieve
- `async_execution.feature` — Bounded executor parallel processing
- `ai_component.feature` — Optional local AI integration and disabled-mode graceful pass

---

# 6. Container & Kubernetes Readiness

- `Dockerfile` — Multi-stage Eclipse Temurin 21 container image
- `deploy/k8s/deployment.yaml` — Kubernetes deployment with liveness & readiness probes
- `deploy/k8s/service.yaml` — ClusterIP service on port 8080
- `deploy/k8s/configmap.yaml` — ConfigMap for application configuration

---

# 7. Verification

```bash
mvn clean test
```
All 91 unit and Cucumber acceptance tests pass with 0 failures, 0 errors, and 0 skipped tests.
