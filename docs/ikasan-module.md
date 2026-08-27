# Ikasan Module Specification
## Module: `enterprise-flow-engine`

### 1. Overview
An **Ikasan Module** is the deployable unit of integration in the Ikasan architecture. It acts as the top-level container encapsulating related integration flows, shared resources, lifecycle hooks, and monitoring endpoints. EFE implements this module model (see **ADR-001**); the module currently hosts **12 flows**.

```text
MODULE: enterprise-flow-engine
  │
  ├── FLOW 1: trade-ingestion-flow
  │     ├── RestConsumer
  │     ├── ReconciliationJobJsonConverter
  │     ├── ValidationTranslator
  │     ├── JobRegistrationBroker
  │     └── JobRegistrationResponseProducer
  │
  ├── FLOW 2: reconciliation-dispatch-flow
  │     ├── ScheduledTaskConsumer (Quartz-backed)
  │     ├── TaskRetrievalBroker
  │     ├── TaskPreparationSplitter
  │     └── MessagingDispatchProducer
  │
  ├── FLOW 3: reconciliation-processing-flow
  │     ├── MessagingProcessingConsumer
  │     ├── TaskEventTranslator
  │     ├── TaskProcessingBroker -> TradeReconciliationProcessor
  │     ├── ResultPersistenceBroker
  │     └── ProcessingResultProducer
  │
  ├── FLOW 12: reliability-demo-flow  (EFE-010 reliability showcase)
  │     ├── RELIABILITY-IN (consumer)
  │     ├── RELIABILITY-DEALER (processor; transiently fails, then succeeds)
  │     ├── RELIABILITY-OUT (producer)
  │     └── (flow-level reliability: retry/backoff/DLQ)
  │
  └── ... 9 further flows (core, async, foundations, router, intelligence, db)
```

---

### 2. Component Role Definition

| Component Type | Responsibility | Examples in `enterprise-flow-engine` |
| :--- | :--- | :--- |
| **Consumer** | Single entry point for an Ikasan Flow. Listens for external triggers or incoming data. | `RestConsumer`, `ScheduledTaskConsumer` (Quartz-backed), `MessagingProcessingConsumer`, `RELIABILITY-IN` |
| **Converter** | Converts an incoming event from one object type to another (e.g. JSON to DTO). | `ReconciliationJobJsonConverter`, `TaskEventTranslator` |
| **Translator** | Modifies, normalizes, or validates an event without changing its Java object type. | `ValidationTranslator` |
| **Broker** | Interacts with external resources (persistence, service invocation) or enriches data. | `JobRegistrationBroker`, `TaskRetrievalBroker`, `TaskProcessingBroker`, `ResultPersistenceBroker` |
| **Splitter** | Decomposes a single composite event into multiple individual item events. | `TaskPreparationSplitter` |
| **Filter** | Predicates on the event; non-matching events are dropped and terminate the route. | EFE filter components in foundation flows |
| **Router** | Selects a named producer route (multi-producer branching). | EFE router components with named route producers |
| **Producer** | Terminal component representing the outbound egress of a flow. | `JobRegistrationResponseProducer`, `MessagingDispatchProducer`, `ProcessingResultProducer`, `RELIABILITY-OUT` |
| **Reliability** | Optional flow-level retry/backoff/DLQ wrapper (`FlowBuilder.reliable`) executed around the pipeline. | `ReliabilityService`, `DeadLetterQueue` on `reliability-demo-flow` |
| **Wiretap** | Optional per-flow audit hook observing every event transiting the flow. | `FlowWiretapStore` (surfaced via `/api/v1/ikasan/observability`) |

---

### 3. Module Lifecycle Management
The `IkasanModule` manages the startup and shutdown sequence across all registered flows:
1. **Module Initialization**: Spring Boot context loads beans and registers flows via `ModuleConfiguration`.
2. **Module Start**: `IkasanEngine` executes `module.start()`, sequentially bringing up each flow's consumer.
3. **Runtime Monitoring**: Flow states (`RUNNING`, `STOPPED`, `ERROR`) and invocation metrics are reported via `/api/v1/ikasan/module` and the web console at `/ikasan/`.
4. **Module Stop**: Gracefully unsubscribes consumers, halts schedulers, and completes in-flight message processing during application shutdown (`@PreDestroy`).
