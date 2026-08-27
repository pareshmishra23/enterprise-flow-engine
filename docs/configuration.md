# Configuration Architecture & Provider Selection

## 1. Configuration Principles
- Provider selection is centralized at startup through Spring `@Configuration` classes and factories (`MessagingBrokerFactory`, `PersistenceProviderFactory`).
- **Strict Rule**: No conditional provider branching (`if ("kafka".equals(provider))`) is permitted in business processors or flow components.
- Unknown or unsupported providers fail fast at startup with clear diagnostic error messages.

---

## 2. Configuration Schema (`application.yml`)

```yaml
server:
  port: 8080

esb:
  module-name: trade-recon-esb
  description: Trade Reconciliation Enterprise Service Bus

messaging:
  provider: inmemory       # Options: inmemory, kafka, rabbitmq, amq, redis
  queue:
    capacity: 1000

persistence:
  provider: inmemory       # Options: inmemory, h2, postgres, mongodb

scheduler:
  reconciliation-dispatch:
    enabled: true
    interval-ms: 5000

worker:
  pool-size: 10

task:
  batch-size: 100
  max-retries: 3
```

---

## 3. Factory-Based Wiring Flow

```text
application.yml (messaging.provider: inmemory)
       │
       ▼
MessagingConfiguration (@Primary MessagingProvider activeMessagingProvider(...))
       │
       ▼
MessagingBrokerFactory.getProvider("inmemory")
       │
       ▼
InMemoryMessagingProvider
       │
       ▼
Injected into MessagingDispatchProducer & MessagingProcessingConsumer
```
