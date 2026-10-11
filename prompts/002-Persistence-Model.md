# 002 — Persistence Model

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/002-CR-Persistence-Model.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/001-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §6.3 (sequence), §7 (data model, secrets, SchemaBootstrap), §11 (retention settings shape)
- `docs/OpenAPI.json` schemas `RetentionSettings`, `CorsSettings` (shapes of settings values)

## Objective

Define every table as a JPA entity so Hibernate creates the full schema. Add the bootstrap that handles what JPA cannot express (the PostGIS extension and GiST indexes), the repositories, a typed key/value settings service, and a duration parser used across the API.

## Tasks

### 1. Entities (package `ai.foresignal.store`, sub-packages allowed)

Implement exactly the tables in Architecture §7.1, with column names in snake_case as listed.

| Entity | Table | Key points |
|---|---|---|
| `SourceSettingsEntity` | `source_settings` | PK `source_id` varchar(64). `params` jsonb. `retention` varchar nullable. `@Version` optimistic lock column is allowed. |
| `SourceCredentialEntity` | `source_credential` | `ciphertext`, `nonce` as `bytea` |
| `SourceHealthEntity` | `source_health` | `recent_record_counts` jsonb (List<Integer>), `adapter_state` jsonb (Map<String,String>) |
| `SourceUsageEntity` | `source_usage` | Composite PK (`source_id`, `minute`) via `@EmbeddedId` |
| `RecordVersionEntity` | `record_version` | `@Immutable`. `seq` from `@SequenceGenerator(name="record_seq", sequenceName="record_seq", allocationSize=1)`. `geom` is JTS `Geometry` with `columnDefinition = "geometry(Geometry,4326)"`. `payload` jsonb. `content_hash` char(64). Unique (`source_id`,`external_id`,`version`). Indexes (`ingested_at`), (`source_id`,`observed_at`). |
| `RecordHeadEntity` | `record_head` | `@EmbeddedId` (`source_id`,`external_id`). Columns per §7.1 **plus** `search_text` text. Indexes (`kind`), (`valid_until`). |
| `TriggerRuleEntity` | `trigger_rule` | uuid PK. `conditions` jsonb (List of `{path, op, value}`). `geofence` `geometry(MultiPolygon,4326)`. `mode` varchar(8) (`match`/`enter`/`exit`, default `match`; map it as an enum). `cooldown_seconds` int (default 0). |
| `AlertEntity` | `alert` | identity bigint PK. Index (`rule_id`,`source_id`,`external_id`,`fired_at`) and (`fired_at`). |
| `WebhookEntity` | `webhook` | uuid PK. `secret_ciphertext`, `secret_nonce` bytea (nullable). `rule_ids` jsonb nullable. |
| `WebhookDeliveryEntity` | `webhook_delivery` | identity PK. `status` as `@Enumerated(STRING)`. Index (`status`,`next_attempt_at`). |
| `AppSettingEntity` | `app_setting` | PK `key` varchar(128). `value` jsonb. `updated_at`. |
| `SystemSecretEntity` | `system_secret` | PK `key`, `value` bytea |

- All timestamps are `Instant` mapped to `timestamptz`.
- **JSON mapping:** use `@JdbcTypeCode(SqlTypes.JSON)` for jsonb columns with plain Java types (`Map<String,Object>`, `List<…>`, small records). Spring Boot 4 uses **Jackson 3**. Make sure Hibernate's JSON `FormatMapper` works with it. If Hibernate cannot find a compatible mapper on its own, register a custom `FormatMapper` backed by Boot's `JsonMapper` via a `HibernatePropertiesCustomizer` (`hibernate.type.json_format_mapper`). Record what you did.

### 2. SchemaBootstrap (`ai.foresignal.config.SchemaBootstrap`)

**Phase 1 — before the `EntityManagerFactory` initializes:**

- A bean (e.g. `PostgisExtensionInitializer`, an `InitializingBean` using the `DataSource`) that runs `CREATE EXTENSION IF NOT EXISTS postgis`.
- Make the EMF depend on it with Spring Boot's `EntityManagerFactoryDependsOnPostProcessor`. The class's package moved in Boot 4; locate it. If it is unavailable, use an equivalent `BeanFactoryPostProcessor`.
- If the extension cannot be created (insufficient privilege), fail fast with a clear message telling the operator to install PostGIS.

**Phase 2 — after Hibernate DDL:**

- An `ApplicationRunner` with highest precedence, or an equivalent hook that runs before the scheduler starts. It executes idempotent statements:
  - `CREATE INDEX IF NOT EXISTS record_version_geom_gist ON record_version USING GIST (geom)`
  - the same for `record_head`
  - `CREATE INDEX IF NOT EXISTS trigger_rule_geofence_gist …`
- Keep the statements in one list constant. Prompt 042 moves them into Flyway.

### 3. Repositories

Spring Data JPA repositories for each entity, with only obviously needed finders (later prompts add native queries where they need them). For example:

- `SourceUsageRepository`: sum of requests/bytes/errors/refused per source within `[from, to)`.
- `RecordHeadRepository`: count by kind.

### 4. AppSettingService (`ai.foresignal.settings`)

- Typed access: `<T> Optional<T> get(String key, Class<T> type)`, `<T> T getOrDefault(String key, Class<T> type, Supplier<T> def)` and `void put(String key, Object value)`, serialized via Jackson to jsonb.
- A small in-memory cache invalidated on `put`.
- Publishes `AppSettingChangedEvent(key)` after commit.
- `SettingKeys` constants: `retention`, `cors`, `trigger.cursor`, `stream.limits`, `install.id`.
- Settings value records: `RetentionSettings(Duration default, Duration alerts, Duration usage, Map<String,Duration> sources)` with defaults 7d/90d/30d/empty; `CorsSettings(List<String> readOrigins, List<String> writeOrigins)` with default `["*"]` and `[]`.

### 5. Durations (`ai.foresignal.settings.Durations`)

- `parse(String)` accepts ISO-8601 (`PT15M`, `P7D`) and shorthand (`30s`, `15m`, `12h`, `7d`, also `1w`). It rejects negatives, zero where inappropriate (expose `parsePositive`) and garbage, throwing `IllegalArgumentException` with a clear message.
- `format(Duration)` returns the shortest shorthand that is exact (e.g. `7d`, `90m`, `45s`).
- Provide Jackson (de)serializers so `Duration` fields in API DTOs and settings accept and emit shorthand. Register them via a Boot Jackson customizer (Jackson 3 API).

### 6. Test database lifecycle (fixes an issue from the 001 report)

`src/test/resources/application-test.yml` currently sets `ddl-auto: create-drop`. All integration tests share **one** static PostGIS container, and Spring's test-context cache can hold several application contexts at once. When one context closes, `create-drop` would drop the schema, including the GiST indexes from `SchemaBootstrap`, underneath the contexts still using it, causing intermittent failures in prompt 040.

- Change the test profile to `ddl-auto: update`, the same as development.
- Add `ai.foresignal.test.DatabaseCleaner`: a test-scoped bean or JUnit extension that runs `TRUNCATE <every Foresignal table> RESTART IDENTITY CASCADE` and `ALTER SEQUENCE record_seq RESTART WITH 1`. Discover the tables from `information_schema.tables` in the public schema, excluding PostGIS's `spatial_ref_sys`.
- Make cleaning the default for `@IntegrationTest` classes (e.g. `@BeforeEach` via an extension registered by the annotation), so every test starts from empty tables while the schema and indexes persist.
- Document the convention in the `IntegrationTest` Javadoc.

## Tests to write (do not run)

- `DurationsTest`: table-driven parse/format, including round trips and errors.
- `AppSettingServiceIT` (`@IntegrationTest`): put/get for records, the cache, the defaults and the change event.
- `SchemaBootstrapIT`: the PostGIS extension exists; the GiST indexes exist (`pg_indexes`); running the bootstrap twice is a no-op.
- `EntityMappingIT`: persist and reload each mutable entity, including jsonb, geometry (point and multipolygon round trip) and the composite keys. Insert a `record_version` row through `JdbcTemplate` (the entity is immutable) and read it back through the repository.
- `DatabaseCleanerIT`: rows from a previous test are gone, `record_seq` restarts at 1, GiST indexes still exist after cleaning.
- `JacksonDurationIT` (or a unit test with the configured mapper): shorthand serialization.

## Acceptance criteria

- With Postgres running, the application would create every table above plus the sequence `record_seq` (increment 1).
- No Flyway, Liquibase or `schema.sql`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `002: Persistence Model`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/002-CR-Persistence-Model.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `002: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
