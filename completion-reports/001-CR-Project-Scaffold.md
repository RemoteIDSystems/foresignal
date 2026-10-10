# 001-CR-Project-Scaffold

| | |
|---|---|
| Prompt | 001-Project-Scaffold.md |
| Status | Complete |
| Implementation commit | fddff0f798c1ef1104a17fff95609f58dae31eaa |
| Date | 2026-10-10 |

## Summary

Elaro now has a buildable skeleton. The Gradle 9.8.1 wrapper was generated from a
freshly downloaded distribution whose SHA-256 was verified against the published
checksum, and that checksum is pinned in `gradle-wrapper.properties` so the
wrapper validates every future download. The build uses a version catalog whose
every entry was confirmed to exist on Maven Central and to sit on a Spring Boot
4.1-compatible line. The application compiles: `ElaroApplication`,
`ElaroProperties`, `ClockConfig` and a `package-info.java` for each package in
Architecture §3. Bootstrap configuration, Docker Compose, a multi-stage
Dockerfile, the MIT licence, third-party notices and a rewritten README are in
place, along with the test infrastructure (shared PostGIS container, composed
`@IntegrationTest` annotation, WireMock base class, controllable clock) that
prompts 002 onward build on. `docs/` and `prompts/` were committed as instructed.
No domain logic, no Flyway, no Liquibase, no SQL migrations.

## Files created / modified

**Build**
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.8.1 wrapper, `distributionSha256Sum` pinned
- `settings.gradle.kts` — `rootProject.name = "elaro"`
- `gradle/libs.versions.toml` — version catalog
- `build.gradle.kts` — plugins, Java 21 toolchain, dependencies, JaCoCo gate, OpenAPI resource copy, `bootJar` naming

**Application**
- `src/main/java/ai/elaro/ElaroApplication.java` — `@SpringBootApplication`, `@EnableScheduling`, `@ConfigurationPropertiesScan`
- `src/main/java/ai/elaro/config/ElaroProperties.java` — `@ConfigurationProperties("elaro")` record with defaults
- `src/main/java/ai/elaro/config/ClockConfig.java` — UTC `Clock` bean
- `src/main/java/ai/elaro/{config,security,source,source/adapters,collect,ingest,store,stream,trigger,retention,settings,system,api}/package-info.java` — package skeleton, one-line description each
- `src/main/resources/application.yml` — bootstrap config plus a `docker` profile that switches to ECS JSON logging

**Packaging and housekeeping**
- `Dockerfile` — `eclipse-temurin:21-jdk` build stage, `eclipse-temurin:21-jre` non-root runtime
- `docker-compose.yml` — `db` (postgis/postgis:17-3.5) and `elaro` services
- `.dockerignore`, `.gitignore`, `.editorconfig`
- `LICENSE` — MIT, Copyright (c) 2026 Adam Allard
- `THIRD_PARTY_NOTICES.md` — reuse policy, God's Eye View MIT licence text, empty "Ported files" table
- `README.md` — replaced the GitLab template
- `completion-reports/.gitkeep`

**Tests (written, not run)**
- `src/test/java/ai/elaro/test/PostgisContainers.java`
- `src/test/java/ai/elaro/test/IntegrationTest.java`
- `src/test/java/ai/elaro/test/WireMockSupport.java`
- `src/test/java/ai/elaro/test/MutableClock.java`
- `src/test/java/ai/elaro/test/MutableClockTest.java`
- `src/test/java/ai/elaro/config/ElaroPropertiesTest.java`
- `src/test/java/ai/elaro/config/ClockConfigTest.java`
- `src/test/java/ai/elaro/ElaroApplicationTests.java`
- `src/test/resources/application-test.yml`

**Committed as planning artifacts**
- `docs/` (6 files), `prompts/` (18 files)

## Deviations from the prompt

1. **`springdoc-openapi` is 3.1.1, not a 2.x line.** The prompt said "latest line
   compatible with Boot 4". springdoc 3.x is that line: `springdoc-openapi:3.1.1`
   builds against `spring-boot-starter-parent:4.1.0`, whereas the 2.8/2.9 line
   targets Boot 3. Chose 3.1.1.

2. **Testcontainers `PostgreSQLContainer` comes from `org.testcontainers.postgresql`,
   not `org.testcontainers.containers`.** In Testcontainers 2.x the
   `org.testcontainers.containers.PostgreSQLContainer` class is marked
   `@Deprecated`; the replacement in `org.testcontainers.postgresql` is also
   non-generic, so the field is declared `PostgreSQLContainer` rather than
   `PostgreSQLContainer<?>`. Behaviour is unchanged.

3. **`elaro.version` is wired through `spring.application.version`.** The prompt
   asked for a `version` field on `ElaroProperties` "from build info". A
   `@ConfigurationProperties` record binds from the environment, not from the
   `BuildProperties` bean, so `application.yml` sets
   `elaro.version: ${spring.application.version:unknown}`.
   `spring.application.version` is a Boot-documented property that defaults to
   `Implementation-Version` from the jar manifest, which `bootJar` populates from
   the project version. `springBoot { buildInfo() }` is enabled as instructed and
   generates `META-INF/build-info.properties` (verified: `build.version=0.1.0-SNAPSHOT`),
   so a `BuildProperties` bean is also available if a later prompt prefers it.
   Running from exploded classes (IDE) has no manifest, so the value falls back to
   `unknown` there. If the planner wants the build-info value in every run mode, a
   later prompt should read `BuildProperties` directly instead.

4. **`@ImportTestcontainers`, not `@Testcontainers`.** The prompt allowed either.
   `@ImportTestcontainers(PostgisContainers.class)` plus a `@ServiceConnection`
   field is what wires the datasource automatically, and it keeps the container
   static so one container serves the whole run.

5. **Bucket4j and springdoc are declared but unused so far.** They are on the
   compile classpath as the prompt's dependency list requires; nothing imports
   them until prompts 005 and 017.

6. **`postman/` and `scripts/` were committed separately.** These appeared in the
   working tree during the session and are outside this prompt's scope, so they
   are not part of the implementation commit. Committed on their own so the
   scaffold commit contains exactly what prompt 001 specifies.

## Issues found / open questions

1. **Gradle 9.8.1 was current, so no substitution was needed.**
   `services.gradle.org/versions/current` reported 9.8.1 (released
   2026-10-07) and the downloaded zip's SHA-256 matched
   `dce76f55f8e251a3a1f130eb120f30b3d271de2b76c9b0729d316b5a1b6dc01f`.

2. **`hibernate-spatial` resolves to 7.4.5.Final from the Boot 4.1.1 BOM**, so no
   explicit version is pinned. JTS comes in transitively. Prompt 002 should confirm
   the dialect wiring it needs (`ddl-auto=update` against PostGIS) when entities land.

3. **The admin token is not implemented yet**, so
   `management.endpoint.health.show-details: when-authorized` currently has no
   authorization mechanism to consult and will behave as "never" for anonymous
   callers. Prompt 003 introduces the admin token; it should check whether that
   setting needs revisiting then.

4. **`docker compose config` validates, but no container was started**, per the
   prompt. Docker Desktop is running on the development machine, so prompt 002 can
   start the database with `docker compose up -d db` when it needs Postgres.

5. **Coverage currently excludes nothing but `ElaroApplication`.** The gate is at
   BUNDLE level and will only be meaningful from prompt 040. Note that the gate
   depends on `test`, so `./gradlew check` would run tests — keep using
   `compileJava compileTestJava` until prompt 040.

6. **`docs/OpenAPI.json` is a build input.** `processResources` copies it to
   `static/v1/openapi.json` (verified byte-identical in `build/resources/main`).
   The Dockerfile therefore copies `docs/` into the build stage, and
   `.dockerignore` deliberately does not exclude it. Prompt 017 depends on this
   path.

## Tests written (not run)

| Test class | Covers |
|---|---|
| `ElaroPropertiesTest` | 7 tests: binding all three properties, `./data` default when `data-dir` absent, `reset-admin-token` defaulting to false, `unknown` version fallback when absent and when blank, defaults applied via direct construction, relative data dir preserved |
| `ClockConfigTest` | 2 tests: the `Clock` bean is UTC and reports current time |
| `MutableClockTest` | 11 tests: default instant and zone, time does not advance on its own, forward/backward/seconds advance, absolute set, reset, `withZone` rezoning and identity case, `millis()` agreement, `TestConfig` bean |
| `ElaroApplicationTests` | 3 tests, `@IntegrationTest`: context loads, `ElaroProperties` binds, `Clock` bean is UTC |

Infrastructure written for later prompts: `PostgisContainers`, `IntegrationTest`,
`WireMockSupport`, `MutableClock`, `application-test.yml`.

Estimated coverage of this prompt's production code: `ElaroProperties` and
`ClockConfig` are fully covered by unit tests; `ElaroApplication` is excluded from
the gate. That is effectively 100% of non-excluded lines, though the figure is
nominal because there is no domain logic yet.

**No tests were executed.** No `test`, `build`, `check` or `jacocoTestReport`
invocation was made, per guardrail 1.

## Compile result

```
$ ./gradlew compileJava compileTestJava --no-daemon
> Task :compileJava
> Task :bootBuildInfo
> Task :processResources
> Task :classes
> Task :compileTestJava

BUILD SUCCESSFUL in 1m 36s
4 actionable tasks: 4 executed
```

Clean on the first attempt — no compile errors and no warnings. `docker compose
config` also validates.

## Notes for the next prompt

1. **Versions in place:** Gradle 9.8.1, Spring Boot 4.1.1, Hibernate 7.4.5.Final
   (via BOM), PostgreSQL driver 42.7.13 (via BOM), Testcontainers 2.0.5, WireMock
   3.13.2, springdoc 3.1.1, Bucket4j 8.21.0, JaCoCo 0.8.15, Awaitility 4.3.0.

2. **Inject the `Clock` bean.** `ClockConfig` provides it and `MutableClock.TestConfig`
   replaces it as `@Primary` in tests. Prompt 002 onward must not call
   `Instant.now()` directly.

3. **Annotate persistence integration tests with `@IntegrationTest`.** It already
   wires `@SpringBootTest`, the shared PostGIS container via `@ServiceConnection`
   and `@ActiveProfiles("test")`. The test profile uses `ddl-auto: create-drop`
   so each context starts from a clean schema; `application.yml` keeps `update`
   for development, as the prompt specified.

4. **`SchemaBootstrap` (prompt 002) has a home:** `ai.elaro.config`, per
   Architecture §3. Remember it must be deleted in prompt 042.

5. **Jackson 3 is the Boot 4 default** (`tools.jackson.*` packages). Prompt 002's
   `jsonb` payload handling should use Jackson 3 types, not `com.fasterxml.jackson.*`.

6. `THIRD_PARTY_NOTICES.md` has an empty "Ported files" table ready for the first
   God's Eye View port (prompt 009 onward).
