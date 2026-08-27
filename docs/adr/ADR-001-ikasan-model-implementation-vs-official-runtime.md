# ADR-001 — EFE Flow Engine: Ikasan-Model Implementation vs. Official Ikasan Runtime

- **Status**: Accepted
- **Date**: 2026-08-27
- **Deciders**: Paresh Mishra (owner), EFE reviewers (Manus, Antigravity, OpenCode)
- **Bead**: EFE-010 — Ikasan-Aligned Flow Engine Hardening

---

## Context

EFE's documentation and beads describe the platform as "built on Ikasan 5.x, native Ikasan
component model." However, the actual runtime under `com.efe.traderecon.ikasan.*`
(`IkasanEngine`, `IkasanFlow`, `IkasanModule`, builders, consumers) is **not** the official
`org.ikasan` library. It is a purpose-built engine that implements the *Ikasan component model*
(Module → Flow → single Consumer → Converter/Translator/Processor/Broker/Splitter/Filter/Router
→ Producer/Endpoint) but is maintained separately.

We must decide: keep and harden this custom engine while openly documenting it as an EFE
implementation of the Ikasan model, or migrate to the official Ikasan runtime.

Real Ikasan integration carries a learning curve and its own conventions and infrastructure
database, and would require a large, invasive refactor of paths, builders, GUI wiring, and
consumer/scheduler semantics. Meanwhile the current engine is proven: 11 flows, 107 passing
tests (55 Cucumber + unit), router branching, filter semantics, wiretap/audit hooks, JMX,
H2, AI providers, GraphQL/gRPC — all running in a single Spring Boot app.

## Decision

**EFE keeps its own Ikasan-model-compliant flow engine.** It is explicitly recognized and
documented as an **EFE implementation of the Ikasan architectural model**, NOT a fork or
transport of the official `org.ikasan` framework. Official Ikasan runtime integration remains
an **open, deferred ADR/upgrade path**.

Concretely:

1. Continue building and hardening the current engine to faithfully implement Ikasan's
   component semantics (EFE-010): real router branching with multiple named producers, real
   filter semantics, Quartz-backed scheduled consumers, retry/recovery/DLQ flow semantics,
   and wiretap/audit observability.
2. Mirror Ikasan's package and role naming (`com.efe.traderecon.ikasan.model.*`,
   `Consumer`, `Converter`, `Translator`, `Processor`, `Broker`, `Splitter`, `Filter`,
   `Router`, `Producer`) so that a future migration to `org.ikasan` is mechanical rather
   than structural.
3. Keep business processors infrastructure-agnostic — they must not depend on the flow
   engine internals, so domain modules remain portable regardless of engine origin.
4. Update public docs/README to state precisely: "EFE implements the Ikasan component model"
   rather than "EFE is built on Ikasan 5.x", removing the misleading framing.

## Consequences

**Positive**
- No throwaway of the proven, running implementation.
- No second orchestration engine; Ikasan-semantics compliance is preserved.
- Migration to official Ikasan stays possible later if a module needs native Ikasan features
  (e.g. clustered Quartz, native wiretap DB, production GUI) that the custom engine lacks.

**Negative / risks**
- We maintain the flow engine ourselves; that is the thing the project aimed to avoid buying
  into ("do not build a second orchestration framework"). This is mitigated by keeping the
  engine small, Ikasan-aligned, and generic (no domain coupling).
- Some native Ikasan capabilities (clustered scheduling, native infrastructure persistence,
  production Ikasan GUI) are not available without adoption. These are tracked as the future
  ADR path.

## Follow-ups

- EFE-010 hardens the engine per the "Ikasan-Aligned Flow Engine Hardening" scope.
- A future ADR (unresolved) will evaluate actual `org.ikasan` adoption when a real domain
  module needs a capability the custom engine cannot provide.
