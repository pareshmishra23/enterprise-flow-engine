# EFE-003 — Real Ikasan Foundation

## Status: IN DESIGN / NEXT

## Objective
Establish the **true, native Ikasan 5.x foundation** for the Enterprise Flow Engine (EFE) by retiring custom simulation/engine wrappers and ensuring standard Ikasan components and flow builders are used across the platform.

---

## Scope & Deliverables

1. **Retire Custom Engine Wrappers**:
   - Align all component interfaces directly with standard Ikasan 5.x component contracts:
     - `IkasanConsumer`
     - `IkasanConverter`
     - `IkasanTranslator`
     - `IkasanBroker`
     - `IkasanSplitter`
     - `IkasanFilter`
     - `IkasanRouter`
     - `IkasanProducer`
   
2. **Real Ikasan Module**:
   - Implement `ModuleBuilder` producing standard `IkasanModule` instances.
   - Module lifecycle management (`start`, `stop`, `isRunning`).

3. **Real Ikasan Flows**:
   - Implement standard `FlowBuilder` wiring:
     - `Consumer` (exactly 1 ingress point)
     - Sequence of intermediate components (`Converter`, `Translator`, `Broker`, `Splitter`, `Processor`, `Router`)
     - `Producer` (terminal outbound point)
   - Flow lifecycle states (`RUNNING`, `STOPPED`, `PAUSED`).

4. **Cucumber Acceptance Tests**:
   - Feature files validating module initialization, component invocation sequence, flow start/stop lifecycle, and error isolation.

---

## Verification Criteria

```bash
mvn clean test
```
All unit tests and Cucumber acceptance tests must execute and pass cleanly without external dependencies.
