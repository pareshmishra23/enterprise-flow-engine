# EFE-012 — RabbitMQ Plugin

## 1. Objective
AMQP 0-9-1 exchange bindings, publisher confirms, channel pooling, and dead-letter exchanges (DLX).

## 2. Key Components
- `RabbitMqProducerAdapter` (Exchange & Routing Key routing).
- `RabbitMqConsumerAdapter` (Prefetch count, manual Ack/Nack).

## 3. Status
- **State**: `QUEUED`
