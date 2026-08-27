# EFE-003 — Generic Job & Task Execution

## 1. Objective
Establish the generic execution state machine for Jobs and Tasks, supporting batch partitioning, task creation, and context propagation.

## 2. Key Components
- `JobStateMachine`: Lifecycle transitions (`SUBMITTED -> REGISTERED -> PARTITIONING -> DISPATCHED -> PROCESSING -> COMPLETED / FAILED`).
- `TaskPartitioner`: Dynamic batch chunking (e.g. 10,000 records split into N partitions).
- `ExecutionContext`: Distributed metadata propagation (correlationId, tenantId, headers).

## 3. Status
- **State**: `QUEUED`
