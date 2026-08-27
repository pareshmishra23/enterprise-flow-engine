# EFE Platform — Building an Autonomous Module (Template)

> Reference guidance for creating a new standalone EFE application (e.g. Corporate Actions,
> Electives, Settlement) on top of the reusable **EFE Platform**. `examples/reconciliation-example`
> is the canonical worked example of this pattern.

## 1. What "autonomous module" means here

An **EFE Platform consumer** is a separate Spring Boot application (a separate Maven artifact and
container) that:

1. depends only on the `efe-platform` jar (never the reverse);
2. defines its own EFE/Ikasan-aligned **Module** and **Flows**;
3. contributes its own **domain**, **processors**, **APIs** and **configuration**;
4. runs independently and is containerized independently;
5. is wired through component-scan of `com.efe.traderecon` (platform beans auto-register) plus its own
   `@Configuration` module that wires its flows.

The EFE Platform provides the domain-neutral runtime, engine, components, and contracts; the consumer
supplies the business meaning.

## 2. Module skeleton

```text
my-application/
├── pom.xml                 <- depends on com.efe:efe-platform
└── src/main/java/com/efe/traderecon/
    ├── MyApplication.java
    ├── module/MyModule.java          (@Configuration, @Bean returning IkasanModule)
    └── flow/.                         your flows, each building a ModuleBuilder flow
```

### 2.1 pom.xml

```xml
<dependency>
    <groupId>com.efe</groupId>
    <artifactId>efe-platform</artifactId>
    <version>${efe.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<!-- spring-boot-maven-plugin for an executable jar -->
```

### 2.2 Application

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Spring's component scan of `com.efe.traderecon` picks up the platform's auto-registered beans
(in-memory messaging provider, `MessagingBrokerFactory`, executor, reliability service, JMX MBeans,
health controller, exception handler, etc.).

### 2.3 Module (wires your flows)

```java
@Configuration
public class MyModule {

    @Bean
    public IkasanModule myModule(
            BuilderFactory builderFactory,
            @Value("${esb.module-name:my-app}") String moduleName,
            @Value("${esb.description:My App Module}") String description,
            @Qualifier("myFlow") IkasanFlow myFlow) {

        return builderFactory.getModuleBuilder(moduleName)
                .withDescription(description)
                .addFlow(myFlow)
                .build();
    }
}
```

> Note: name the `@Bean` factory method so it does **not** equal the configuration class simple name
> (that would collide with the config-class bean name and raise `BeanDefinitionOverrideException`).

### 2.4 Flow

```java
IkasanFlow flow = flowBuilder
    .withConsumer(restConsumer(...))
    .withConverter(new MyConverter())
    .withTranslator(new MyValidator())
    .withBroker(new MyProcessor())     // or router/splitter/filter
    .withProducer(producerBuilder.simpleProducer())
    .build();
```

## 3. What the platform gives you for free

| Capability | Platform bean / contract |
| :--- | :--- |
| Flow engine & Ikasan component model | `ikasan/**` |
| Bounded async execution | `EfeExecutorService` (`execution/**`) |
| Messaging SPI + in-memory provider | `messaging/**`, `InMemoryQueue`, `MessagingBrokerFactory` |
| Retry / backoff / DLQ / audit | `ReliabilityService` (`reliability/**`) |
| AI provider boundary + sanitizer/parser | `intelligence/**` |
| OAuth2 resource-server security | `security/**` |
| Health + error envelope | `HealthController`, `GlobalExceptionHandler`, `ErrorResponse` |
| JMX management MBeans | `management/**` |
| Common config | `EsbProperties`, `JacksonConfiguration` |

Business-specific concerns stay in the consumer: **domain models, reconcilers/processors, REST/graphql/
grpc APIs, persistence providers, and the module/flows that carry them**.

## 4. Isolation contract

- The platform never depends on a consumer; consumers depend on the platform.
- Do **not** add business models or processors into `efe-platform`. Platform classes must remain
  domain-neutral, verified by the Maven Enforcer rule in `efe-platform/pom.xml`.
- Follow `examples/reconciliation-example` for the reference structure and
  `examples/platform-demo` for pure-platform capability demonstrations.

## 5. Containerizing

```bash
# from repo root (builds reactor + packages your app jar)
docker build -f Dockerfile -t my-app:dev .
```

Provide a `deploy/k8s/<app>/` with Deployment/Service/ConfigMap mirroring
`deploy/k8s/reconciliation/`.
