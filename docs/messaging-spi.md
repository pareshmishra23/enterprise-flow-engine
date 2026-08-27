# Messaging SPI Specification & Provider Roadmap

## 1. Overview
The Messaging Service Provider Interface (SPI) abstracts message brokers from the Ikasan flows. The flows depend strictly on `MessagingProducer<T>`, `MessagingConsumer<T>`, and `MessagingMessage<T>`.

```text
                     MESSAGING SPI
                           │
             ┌─────────────┼─────────────┬─────────────┬─────────────┐
             │             │             │             │             │
             ▼             ▼             ▼             ▼             ▼
         inmemory        kafka        rabbitmq        amq          redis
       (Active/Live) (IKASAN-004)  (IKASAN-005)  (IKASAN-006)  (IKASAN-007)
```

---

## 2. Active Provider: `inmemory`
- **Implementation**: `InMemoryMessagingProvider`, `InMemoryMessagingProducer`, `InMemoryMessagingConsumer`, `InMemoryQueue`.
- **Concurrency**: Backed by `LinkedBlockingQueue` with configurable bounded capacity (default `1000`) and a cached thread pool for async subscriber dispatch.
- **Safety**: Thread-safe concurrent operations, zero external software installation needed.

---

## 3. Future Adapter Boundaries

### A. Apache Kafka (IKASAN-004)
- **Target Package**: `com.efe.traderecon.messaging.kafka`
- **Producer Architecture**: `MessagingProducer` -> `KafkaProducerAdapter` -> `org.apache.kafka.clients.producer.KafkaProducer` -> Topic `trade.recon.tasks`.
- **Consumer Architecture**: Topic `trade.recon.tasks` -> `org.apache.kafka.clients.consumer.KafkaConsumer` -> `KafkaConsumerAdapter` -> `MessagingConsumer`.

### B. RabbitMQ (IKASAN-005)
- **Target Package**: `com.efe.traderecon.messaging.rabbitmq`
- **Architecture**: `RabbitMqProducerAdapter` publishes to Direct Exchange -> Queue `trade.recon.tasks` -> `RabbitMqConsumerAdapter` with AMQP ack/nack.

### C. AMQ / ActiveMQ / JMS (IKASAN-006)
- **Target Package**: `com.efe.traderecon.messaging.amq`
- **Architecture**: Leverages standard Jakarta JMS `ConnectionFactory`, `Session`, and `MessageProducer`/`MessageConsumer` via `JmsProducerAdapter` and `JmsConsumerAdapter`.

### D. Redis Streams (IKASAN-007)
- **Target Package**: `com.efe.traderecon.messaging.redis`
- **Architecture**: Uses Redis `XADD` for production and `XREADGROUP` / consumer groups for distributed task consumption via `RedisStreamsProducerAdapter` and `RedisStreamsConsumerAdapter`.
