# Persistence SPI Specification & Separation of Concerns

## 1. Overview
The Persistence SPI abstracts data storage for all trade reconciliation entities (`Job`, `Task`, `Trade`, `ReconciliationResult`).

```text
                     PERSISTENCE SPI
                           │
             ┌─────────────┼─────────────┬─────────────┐
             │             │             │             │
             ▼             ▼             ▼             ▼
         inmemory          h2        postgres       mongodb
       (Active/Live)  (Embedded)    (Enterprise)   (Document)
```

---

## 2. Infrastructure vs. Business Persistence Distinction
A key architectural principle in Ikasan applications is the separation of **Ikasan Runtime Metadata Persistence** from **Business Entity Persistence**:

1. **Ikasan Infrastructure Persistence**:
   - Manages flow state, component configuration history, error categorization, and wiretaps.
   - Handled natively by Ikasan's internal persistence infrastructure (e.g. embedded H2 or runtime DB).
2. **Business Domain Persistence**:
   - Manages domain entities: `Job`, `Task`, `Trade`, `ReconciliationResult`.
   - Handled through `JobRepository`, `TaskRepository`, `TradeRepository`, and `ResultRepository` SPIs.
   - Completely independent from Ikasan's internal tables.

---

## 3. Active Provider: `inmemory`
- **Implementation**: `InMemoryPersistenceProvider`, backed by `ConcurrentHashMap` stores.
- Thread-safe, supports zero-setup local execution, and enables rapid testing.

---

## 4. Future Persistence Providers
- **H2**: Embedded relational database for single-instance persistence without external infrastructure.
- **PostgreSQL**: Production relational engine with ACID transactions, indexing on `jobId`, `taskId`, and partition tables for large trade volumes.
- **MongoDB**: Document database option for high-volume unstructured trade attributes and audit history.
