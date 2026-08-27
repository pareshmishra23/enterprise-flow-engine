# Ikasan Module Specification
## Module: `TRADE-RECON-ESB`

### 1. Overview
An **Ikasan Module** is the deployable unit of integration in the Ikasan architecture. It acts as the top-level container encapsulating related integration flows, shared resources, lifecycle hooks, and monitoring endpoints.

```text
MODULE: trade-recon-esb
  │
  ├── FLOW 1: trade-ingestion-flow
  │     ├── RestConsumer
  │     ├── ReconciliationJobJsonConverter
  │     ├── ValidationTranslator
  │     ├── JobRegistrationBroker
  │     └── JobRegistrationResponseProducer
  │
  ├── FLOW 2: reconciliation-dispatch-flow
  │     ├── ScheduledTaskConsumer
  │     ├── TaskRetrievalBroker
  │     ├── TaskPreparationSplitter
  │     └── MessagingDispatchProducer
  │
  └── FLOW 3: reconciliation-processing-flow
        ├── MessagingProcessingConsumer
        ├── TaskEventTranslator
        ├── TaskProcessingBroker -> TradeReconciliationProcessor
        ├── ResultPersistenceBroker
        └── ProcessingResultProducer
```

---

### 2. Component Role Definition

| Component Type | Responsibility | Examples in `TRADE-RECON-ESB` |
| :--- | :--- | :--- |
| **Consumer** | Single entry point for an Ikasan Flow. Listens for external triggers or incoming data. | `RestConsumer`, `ScheduledTaskConsumer`, `MessagingProcessingConsumer` |
| **Converter** | Converts an incoming event from one object type to another (e.g. JSON to DTO). | `ReconciliationJobJsonConverter`, `TaskEventTranslator` |
| **Translator** | Modifies, normalizes, or validates an event without changing its Java object type. | `ValidationTranslator` |
| **Broker** | Interacts with external resources (persistence, service invocation) or enriches data. | `JobRegistrationBroker`, `TaskRetrievalBroker`, `TaskProcessingBroker`, `ResultPersistenceBroker` |
| **Splitter** | Decomposes a single composite event into multiple individual item events. | `TaskPreparationSplitter` |
| **Producer** | Terminal component representing the outbound egress of a flow. | `JobRegistrationResponseProducer`, `MessagingDispatchProducer`, `ProcessingResultProducer` |

---

### 3. Module Lifecycle Management
The `IkasanModule` manages the startup and shutdown sequence across all registered flows:
1. **Module Initialization**: Spring Boot context loads beans and registers flows via `ModuleConfiguration`.
2. **Module Start**: `IkasanEngine` executes `module.start()`, sequentially bringing up each flow's consumer.
3. **Runtime Monitoring**: Flow states (`RUNNING`, `STOPPED`, `ERROR`) and invocation metrics are reported via `/api/v1/ikasan/module` and the web console at `/ikasan/`.
4. **Module Stop**: Gracefully unsubscribes consumers, halts schedulers, and completes in-flight message processing during application shutdown (`@PreDestroy`).
