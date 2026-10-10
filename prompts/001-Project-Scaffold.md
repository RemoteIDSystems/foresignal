# 001 — Project Scaffold

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/001-CR-Project-Scaffold.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.

## Read first

- `docs/Architecture.md` §2 (stack), §3 (packages), §13 (configuration), §14 (deployment)
- `docs/Roadmap.md` §2–§3 (guardrails, Completion Report template)

## Objective

Create the buildable project skeleton: the Gradle wrapper and build, a Spring Boot application that compiles, bootstrap configuration, Docker packaging, licence and notices, the package layout, and the test infrastructure that later prompts build on. No domain logic yet.

The repository currently contains `README.md` (the GitLab template), `docs/`, `prompts/`, `postman/` (the generated Postman collection and environment) and `scripts/generate-postman.py`. These are planning artifacts written before this prompt and are not yet committed. Commit them as part of this prompt, unchanged.

## Tasks

### 1. Gradle wrapper (Gradle 9.8.1)

Gradle is **not** installed on this machine. Generate the wrapper as follows:

1. Download `https://services.gradle.org/distributions/gradle-9.8.1-bin.zip` into a fresh temporary directory outside the repo.
2. Verify its SHA-256 against `https://services.gradle.org/distributions/gradle-9.8.1-bin.zip.sha256`.
3. Unzip it and run `<tmp>/gradle-9.8.1/bin/gradle wrapper --gradle-version 9.8.1 --distribution-type bin` in the repo root.
4. Set `distributionSha256Sum` in `gradle/wrapper/gradle-wrapper.properties`.
5. Commit `gradlew`, `gradlew.bat` and `gradle/wrapper/*`, then delete the temp directory.

If a newer 9.x Gradle is current and Spring Boot 4.1 supports it, you may use it instead; record the choice.

### 2. Build files

- `settings.gradle.kts`: `rootProject.name = "elaro"`.
- `gradle/libs.versions.toml`: a version catalog. Verify every version on Maven Central. Starting points (confirm or update):

| Library | Version |
|---|---|
| Spring Boot plugin | 4.1.1 |
| `io.spring.dependency-management` | latest |
| Bucket4j (`com.bucket4j:bucket4j_jdk17-core`) | 8.21.0 |
| springdoc-openapi (`springdoc-openapi-starter-webmvc-ui`) | latest line compatible with Boot 4 |
| Testcontainers BOM | 2.0.5 |
| WireMock (`org.wiremock:wiremock-standalone`) | latest 3.x |
| Awaitility | latest |
| JaCoCo | latest, supporting Java 21 |

- `build.gradle.kts`:
  - **Plugins:** `java`, `org.springframework.boot`, `io.spring.dependency-management`, `jacoco`.
  - **Toolchain and compiler:** Java 21; compile with `-parameters`; group `ai.elaro`, version `0.1.0-SNAPSHOT`.
  - **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `org.hibernate.orm:hibernate-spatial` (version from the Boot BOM), `org.postgresql:postgresql`, `io.micrometer:micrometer-registry-prometheus`, Bucket4j, springdoc.
  - **Test dependencies:** `spring-boot-starter-test`, `spring-boot-testcontainers`, `org.testcontainers:testcontainers-postgresql`, `org.testcontainers:testcontainers-junit-jupiter`, WireMock standalone, Awaitility.
  - **Tests:** `tasks.test { useJUnitPlatform() }`.
  - **Coverage gate:** `jacocoTestCoverageVerification` requires LINE coverage ≥ 0.90 at BUNDLE level, excluding `ai/elaro/ElaroApplication*`. `check` depends on it, and `jacocoTestReport` produces XML and HTML.
  - **Spec on the classpath:** `processResources` copies `docs/OpenAPI.json` to `static/v1/openapi.json`, which is used in prompt 017.
  - **Packaging:** `bootJar` archive name `elaro.jar`.

### 3. Application

- `ai.elaro.ElaroApplication`: `@SpringBootApplication`, `@EnableScheduling`, `@ConfigurationPropertiesScan`.
- `ai.elaro.config.ElaroProperties`: `@ConfigurationProperties("elaro")` with `dataDir` (Path, default `./data`), `resetAdminToken` (boolean), `version` (from build info).
- `ai.elaro.config.ClockConfig`: a `Clock` bean (UTC). Always inject the `Clock`; never call `Instant.now()` directly, so tests can control time.
- Enable `springBoot { buildInfo() }` so the version is available.
- Create the package skeleton from Architecture §3, with a `package-info.java` in each package (`config`, `security`, `source`, `source.adapters`, `collect`, `ingest`, `store`, `stream`, `trigger`, `retention`, `settings`, `system`, `api`) containing a one-line description.

### 4. Configuration (`src/main/resources/application.yml`)

- **Datasource:** `${ELARO_DB_URL:jdbc:postgresql://localhost:5433/elaro}`, user and password from `ELARO_DB_USER` / `ELARO_DB_PASSWORD` (default `elaro`).
- **JPA:**
  - `ddl-auto: update`, `open-in-view: false`
  - Hibernate `jdbc.batch_size: 500`, `order_inserts: true`
  - `jdbc.time_zone: UTC`
- **Server:** `server.port: ${ELARO_PORT:8080}`
- **Threads and async:**
  - `spring.threads.virtual.enabled: true`
  - `spring.mvc.async.request-timeout: -1` (needed for SSE)
- **Elaro:** `elaro.data-dir: ${ELARO_DATA_DIR:./data}`, `elaro.reset-admin-token: ${ELARO_RESET_ADMIN_TOKEN:false}`
- **Actuator:** expose `health,info,prometheus`; health shows details only when authorized.
- **springdoc:** `springdoc.api-docs.path: /v1/openapi-generated.json`, `springdoc.swagger-ui.path: /docs`, `springdoc.swagger-ui.url: /v1/openapi.json`
- **Logging:** structured JSON console logging (`logging.structured.format.console: ecs`) behind a profile. Default to human-readable output locally, and have the `docker` profile switch to JSON.

### 5. Packaging and runtime

- **`docker-compose.yml`** (Architecture §14):
  - `db` service: `postgis/postgis:17-3.5`, env `POSTGRES_DB/USER/PASSWORD=elaro`, volume `pgdata`, ports `5433:5432`, healthcheck `pg_isready -U elaro -d elaro`.
  - `elaro` service: `build: .`, env pointing at `db:5432`, `SPRING_PROFILES_ACTIVE=docker`, `ELARO_DATA_DIR=/data`, volume `elaro-data:/data`, ports `8080:8080`, `depends_on: db: condition: service_healthy`, `restart: unless-stopped`.
  - Also support running only the DB with `docker compose up -d db` for local development (document this).
- **`Dockerfile`:**
  - Build stage: `eclipse-temurin:21-jdk` runs `./gradlew bootJar -x test --no-daemon`.
  - Runtime stage: `eclipse-temurin:21-jre` as a non-root user with `/data` owned by that user; `ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/elaro.jar"]`.
- **Ignore and editor files:** `.dockerignore`, `.gitignore` (Gradle, IDE, `data/`, `build/`, `.env`) and `.editorconfig`.

### 6. Licence, docs, housekeeping

- **`LICENSE`:** MIT, `Copyright (c) 2026 Adam Allard`.
- **`THIRD_PARTY_NOTICES.md`:** an intro plus a "God's Eye View" section with its MIT licence text and `Copyright (c) 2026 Bilawal Sidhu`. Copy the text from `~/Documents/GitLab/gods-eye-view/LICENSE`. Include an empty "Ported files" table (columns: Elaro file, GEV origin file) for later prompts to fill in.
- **`README.md`:** replace the GitLab template. Cover:
  - what Elaro is (2 paragraphs drawn from `docs/Executive-Summary.md`) and its status ("under construction")
  - requirements (Docker, or Java 21 plus Postgres/PostGIS)
  - a quickstart placeholder that prompt 018 will complete
  - a "Postman" section: import `postman/Elaro.postman_collection.json` and `postman/Elaro-Local.postman_environment.json`, then set `adminToken`
  - links to the docs, and the MIT licence
- **`completion-reports/`:** create it with a `.gitkeep`.

### 7. Test infrastructure (`src/test/java/ai/elaro/...`)

- **`ai.elaro.test.PostgisContainers`:** a shared static Testcontainers `PostgreSQLContainer` using `DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres")`, reused across test classes.
- **`ai.elaro.test.IntegrationTest`:** a composed annotation (`@SpringBootTest`, `@Testcontainers` or `@ImportTestcontainers`, a `@ServiceConnection` wiring, `@ActiveProfiles("test")`). Later prompts annotate integration tests with it.
- **`src/test/resources/application-test.yml`:** `elaro.data-dir` set to a temp directory, plus quiet logging.
- **`ai.elaro.test.WireMockSupport`:** a JUnit 5 extension or base class that starts WireMock on a dynamic port, for adapter tests.
- **`ai.elaro.test.MutableClock`:** a controllable `Clock` for tests, exposed as a `@TestConfiguration` primary bean when imported.
- **`ElaroApplicationTests`:** a `contextLoads` test using `@IntegrationTest`.
- **Unit tests:** cover `ElaroProperties` binding defaults and `MutableClock`.

## Acceptance criteria

- `./gradlew compileJava compileTestJava` succeeds using only the wrapper.
- `docker compose config` validates. **Do not** start containers in this prompt.
- The repository contains the wrapper, build files, app skeleton, config, Docker files, LICENSE, THIRD_PARTY_NOTICES.md, the new README, `docs/`, `prompts/` and `completion-reports/`.
- No Flyway or Liquibase dependency.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `001: Project Scaffold`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/001-CR-Project-Scaffold.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `001: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
