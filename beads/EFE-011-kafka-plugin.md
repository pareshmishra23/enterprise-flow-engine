# EFE-011 — Kafka Plugin

## 1. Objective
Production Apache Kafka producer/consumer plugin, partition key routing, consumer group rebalance handlers, and schema registry integration.

## 2. Key Components
- `KafkaProducerAdapter` with partition hashing.
- `KafkaConsumerAdapter` with manual offset commit (`Acknowledgment`).

## 3. Status
- **State**: `QUEUED`
