# EFE-011-A — Platform Extraction & Reconciliation Separation (Implementation Bead)

> Documents the extraction of the single `enterprise-flow-engine` application into a reusable
> **EFE Platform** library plus **autonomous example applications**, and records every
> intentionally-changed behaviour so nothing is lost silently.

## 1. Goal

Extract the reusable, domain-neutral **EFE Platform** from the single-app repository and prove, via a
reference/demonstrator application, that an external business application can:

- depend on the EFE Platform artifact;
- define its own EFE/Ikasan-aligned **Module** and **Flows**;
- use the EFE APIs, components and connectivity limits;
- run as a standalone Spring Boot application; and
- be containerized independently.

## 2. Resulting Module Layout

```text
enterprise-flow-engine (pom reactor)
├── efe-platform                        <- reusable, domain-neutral library (JAR)
│   └── com.efe.traderecon
│       ├── ikasan/        flow model, engine, builder, consumers/components
│       ├── execution/     bounded executor service
│       ├── messaging/     Messaging SPI + in-memory provider
│       ├── intelligence/  AI provider boundary + sanitizer/parser/fallback
│       ├── reliability/   retry / backoff / DLQ / audit
│       ├── security/      OAuth2 resource-server foundation
│       ├── api/           generic HealthController + GlobalExceptionHandler + ErrorResponse
│       ├── configuration/ EsbProperties + JacksonConfiguration
│       └── management/    JMX MBeans (Module / Executor / Messaging)
└── examples/
    ├── platform-demo                    <- standalone demonstrator of pure EFE capabilities
    │   └── com.efe.traderecon
    │       ├── PlatformDemoApplication
    │       ├── module/DemoModule        <- wires 8 pure-platform demo flows
    │       └── flow/                    core, foundation, asyncdemo, asyncexec,
    │                                    intelligence, reliabilitydemo
    └── reconciliation-example           <- REFERENCE application (efe-reconciliation)
        └── com.efe.traderecon
            ├── ReconciliationApplication
            ├── module/ReconciliationModule  <- wires its 3 reconciliation flows
            ├── domain / processor / flow/{ingestion,dispatch,processing,dbdemo}
            ├── api/                       JobController, ReconciliationJobController, DTOs,
            │                              graphql, grpc, idempotency
            ├── configuration/  MessagingConfiguration, PersistenceConfiguration
            ├── persistence/    SPI + inmemory + h2/mongodb/postgres
            └── management/     EfeSchedulerMBean
```

### Dependency direction (enforced, never reversed)

```text
examples/reconciliation-example ──> efe-platform
examples/platform-demo           ──> efe-platform
efe-platform ──> (no dependency on any example / business module)
```

Enforced by a Maven Enforcer `bannedDependencies` rule in `efe-platform/pom.xml` that rejects any
dependency on `com.efe:efe-reconciliation` / `trade-recon`.

## 3. What Moved Where

| Area | efe-platform | platform-demo | reconciliation-example |
| :--- | :--- | :--- | :--- |
| Ikasan runtime/model/engine/builder | ✔ | | |
| Executor / Quartz / JMX platform MBeans | ✔ | | |
| Messaging SPI + in-memory provider | ✔ | | |
| Intelligence provider + sanitizer + parser | ✔ | | |
| Reliability service (retry/backoff/DLQ/audit) | ✔ | | |
| Security / generic API (ErrorResponse, GlobalExceptionHandler, HealthController) | ✔ | | |
| EsbProperties / JacksonConfiguration | ✔ | | |
| Demo flows (core/foundation/async/reliability/intelligence) | | ✔ | |
| PlatformDemoApplication + DemoModule | | ✔ | |
| Reconciliation domain/processors/flows | | | ✔ |
| ReconciliationModule (3 flows) | | ✔‑(none) | ✔ |
| ReconciliationApplication | | | ✔ |
| DB demo flow + persistence SPI/providers | | | ✔ |
| Recon REST/GraphQL/gRPC/DTOs/idempotency/JMX | | | ✔ |

All source was moved with `git mv`, preserving history. Java packages were **not** renamed
(all modules still use `com.efe.traderecon.*`) to keep the change focused on module topology.

## 4. Intentionally Changed Behaviour (recorded)

These are deliberate consequences of the extraction, not regressions:

1. **Module split.** The old single app booted one `enterprise-flow-engine` Module with **12 flows**.
   That is now two runnable Modules:
   - `reconciliation-example` → Module `trade-recon-esb` with **3 flows**
     (`trade-ingestion-flow`, `reconciliation-dispatch-flow`, `reconciliation-processing-flow`).
   - `platform-demo` → Module `enterprise-flow-engine` with **8 pure-platform demo flows**.
2. **Application class renamed.** `TradeReconApplication` → `ReconciliationApplication`.
3. **Module configuration renamed.** `ModuleConfiguration` → `ReconciliationModule` (recon) and
   `DemoModule` (platform-demo). The `ReconciliationModule` config class is named to match the module
   bean it produces; the `@Bean` factory method was renamed (`reconciliationModuleDefinition`) to avoid
   a `BeanDefinitionOverrideException` (config-class bean name vs. factory-method bean name collision).
4. **`ModuleAndFlowsTest` split.** The old test asserted 12 flows on `enterprise-flow-engine`. It was
   split into a recon version (3 flows, `trade-recon-esb`) and a platform-demo version (8 flows).
5. **`ReconciliationJobControllerTest.moduleName`/`flowCount`** now assert the recon module
   (`trade-recon-esb`, 3 flows) instead of the old single-app module.
6. **Resources.** `application.yml` was split per application. recon-example uses module name
   `trade-recon-esb`, port `8080`; platform-demo uses `enterprise-flow-engine` (to satisfy the JMX
   cucumber feature that asserts that MBean name), port `8090`. The `static/` web console was copied to
   both applications and the root `static/` removed.
7. **Cucumber split.** Previously one suite (24 features, `PlatformCapabilitySteps` straddling concerns).
   Now:
   - **recon-example** (17 scenarios): database, graphql_api, grpc_api, health,
     job-idempotency, job-status, job-submission, rest_api, result-query, task-query
     + `ReconciliationApiSteps` (split from the old `PlatformCapabilitySteps`).
   - **platform-demo** (41 scenarios): ai_anomaly, ai_component, ai_configuration, ai_failure,
     ai_fraud, ai_llm_audit, async_execution, async_execution_flow, core_flow, ikasan_foundation,
     ikasan_router, ikasan_scheduled_flow, jmx_management, reliability_flow
     + `PlatformCapabilitySteps` trimmed to async/AI/JMX.
8. **Docker / deploy relocated.** Root `Dockerfile` now builds the reactor and packages the
   reconciliation-example jar (`efe-reconciliation:dev`). `deploy/k8s/*` moved to
   `deploy/k8s/reconciliation/` with application-specific labels/names.

## 5. Verified

```bash
mvn clean test          # full reactor: efe-platform, platform-demo, recon-example all green

mvn -pl efe-platform clean validate   # enforcer isolation guard passes
```

| Module | Unit/Spring tests | Cucumber scenarios | Total |
| :--- | :--- | :--- | :--- |
| efe-platform | 34 | – | 34 |
| examples/platform-demo | 9 | 41 | 50 |
| examples/reconciliation-example | 14 | 17 | 31 |
| **Total** | **57** | **58** | **115** |

The reconciliation end-to-end round trip (submit job via REST → dispatch → process → query)
is covered by the recon-example cucumber suite (17 scenarios) and passes.

## 6. Future Apps Consume the Platform

Future autonomous applications — **Corporate Actions (`efe-corporate-actions`)**,
**Electives (`efe-electives`)**, **Settlement** — are separate applications that depend on
`efe-platform`, define their own Module/flows/domain/APIs, run and containerize independently.
They follow the `docs/module-template.md` pattern demonstrated by `examples/reconciliation-example`.
No production CA / Elective / Settlement code is introduced by EFE-011.

## 7. Final Validation / Merge Gates (verified)

The following were verified explicitly against PR #12 before merge.

### 7.1 efe-platform contains zero reconciliation / business leakage

- Grep of `efe-platform/src/main/java`: **no imports** of any reconciliation business package
  (`domain`, `processor`, `flow/ingestion|dispatch|processing|dbdemo`, `api/graphql|grpc|idempotency`,
  `persistence`, `configuration/Messaging|Persistence`).
- No reconciliation business **types** referenced (`Trade`, `ReconciliationJob`, `ReconciliationResult`,
  `Custodian`, `JobRepository`, `TradeReconciliationProcessor`, `ScheduledTaskConsumer`, etc.).
- The only textual hit is a **cosmetic prompt string** ("enterprise trade audit AI assistant") inside the
  optional local `PromptBuilder`. It is not a Java type, compile dependency, or SPI contract; the AI SPI
  (`IntelligenceProvider` / `IntelligenceRequest` / `IntelligenceResult`) is generic. There is no
  `TradeReconAIService`. This cosmetic wording is tracked for a future bead (move trade-specific prompt
  text to the business layer), out of scope for EFE-011.
- `mvn dependency:tree` – efe-platform has **zero** `com.efe` dependencies.

### 7.2 efe-platform is a library, not a server

- `<packaging>jar</packaging>`; **no** `spring-boot-maven-plugin` (present only on the two runnable
  apps).
- Built `efe-platform-1.0.0-SNAPSHOT.jar` manifest has **no** `Main-Class` / `Start-Class` → plain
  reusable library jar, does not start a server. Relationship is
  `efe-platform (library) → application`, not `platform server`.

### 7.3 Independent start — reconciliation-example (no root app)

```bash
mvn -pl examples/reconciliation-example clean package -DskipTests   # builds jar standalone
java -jar .../efe-reconciliation-1.0.0-SNAPSHOT.jar                  # starts independently
```
Verified with no other application running:
- `/health` → 200; `/ready` → module `trade-recon-esb`, 3 flows, READY.
- `POST /api/v1/jobs/reconciliation` → `201`, then job → **COMPLETED** (1 matched, 0 failed);
  flow counters confirm ingestion=1, dispatch=5, processing=1 (async round trip executed).

### 7.4 Independent start — platform-demo (no reconciliation)

```bash
mvn -pl examples/platform-demo clean package -DskipTests
java -jar .../efe-platform-demo-1.0.0-SNAPSHOT.jar   # port 8090
```
Verified with no other application running:
- `/health` → 200; `/ready` → module `enterprise-flow-engine`, **8 flows**, READY.
- 8 pure-platform flows registered: `efe-core-flow`, `efe-async-flow`, `efe-foundation-flow`,
  `efe-scheduled-foundation-flow`, `efe-router-foundation-flow`, `intelligence-audit-flow`,
  `async-demo-flow`, `reliability-demo-flow`.
- `POST /api/v1/core/events` → **202 ACCEPTED** into `efe-core-flow`.

Together 7.3 + 7.4 prove the platform is genuinely reusable and that reconciliation is a separate
autonomous consumer (the architectural-separation smoke test).

### 7.5 Dependency direction (no cycles / no leakage)

| Module | `com.efe` dependencies |
| :--- | :--- |
| efe-platform | **(none)** — root of the graph, pure library |
| examples/platform-demo | `efe-platform` only |
| examples/reconciliation-example | `efe-platform` only |

Dependency example → platform, never reversed. Enforced at build by the Maven Enforcer
`bannedDependencies` rule in `efe-platform/pom.xml`.

### 7.6 No duplicate beans / config / identity

- Each runnable app has exactly one `application.yml` + one `static/` console; the platform jar has
  neither. The earlier `BeanDefinitionOverrideException` (config-class name == factory-method bean name)
  was fixed by renaming the factory method.
- **Application identity** is distinct from the internal Ikasan **Module** id:
  - `reconciliation-example`: application `reconciliation-example`, Module `trade-recon-esb`.
  - `platform-demo`: application `efe-platform-demo`, Module `enterprise-flow-engine`.
- **Security** is configurable and off by default (`efe.security.enabled: false`) in both apps — no
  imposed authentication / Basic-auth lockout on reference apps.
- Deployment manifests are explicitly marked **example / reference** (`efe.example: "true"`,
  `efe.type: reference`) and are not presented as production CA / Elective / Reconciliation deployments.
