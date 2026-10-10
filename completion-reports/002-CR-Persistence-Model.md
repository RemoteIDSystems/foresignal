# 002-CR-Persistence-Model

| | |
|---|---|
| Prompt | 002-Persistence-Model.md |
| Status | Complete |
| Implementation commit | 8d2c4600e8c8d879b575e0d6b28abaaacfde6aea |
| Date | 2026-10-10 |

## Summary

Every table in Architecture §7.1 now exists as a JPA entity, so Hibernate creates
the whole schema, including the `record_seq` sequence at increment 1. `SchemaBootstrap`
covers what JPA cannot express, in two phases: the PostGIS extension before the
`EntityManagerFactory` is built, and the three GiST indexes after Hibernate's DDL.
Spring Data repositories are in place with the finders this prompt called for, plus
a typed `AppSettingService` over `jsonb` with an invalidating cache and an
after-commit change event, and `Durations` with Jackson 3 shorthand
(de)serializers. The most consequential discovery was that Hibernate silently
prefers Jackson 2 over Jackson 3 for `jsonb` when both are on the classpath — which
they are — so the format mapper is now pinned explicitly to Boot's Jackson 3
`JsonMapper`. The test profile moved from `ddl-auto: create-drop` to `update` with a
new `DatabaseCleaner`, which is the fix for the issue the 001 report raised.

## Files created / modified

**Entities (`ai.elaro.store`)**
- `SourceSettingsEntity` — subscription state; `params` jsonb, `@Version` optimistic lock
- `SourceCredentialEntity` — encrypted upstream credential (`bytea`)
- `SourceHealthEntity` — last attempt/success/error, `recent_record_counts` jsonb list, `adapter_state` jsonb map
- `SourceUsageEntity` + `SourceUsageId` — per-minute counters, `@EmbeddedId`
- `RecordVersionEntity` — append-only revision; `@Immutable`, `record_seq` generator, `geometry(Geometry,4326)`, `payload` jsonb, `char(64)` hash
- `RecordHeadEntity` + `RecordHeadId` — current state; `@EmbeddedId` plus `search_text`
- `TriggerRuleEntity`, `TriggerCondition`, `TriggerMode`, `TriggerModeConverter` — uuid PK, `conditions` jsonb, `geometry(MultiPolygon,4326)` geofence
- `AlertEntity` — identity PK, cooldown and `fired_at` indexes
- `WebhookEntity` — uuid PK, encrypted secret, `rule_ids` jsonb
- `WebhookDeliveryEntity` + `DeliveryStatus` — identity PK, `(status, next_attempt_at)` index
- `AppSettingEntity` — `key` PK, `value` jsonb
- `SystemSecretEntity` — `key` PK, `value` bytea

**Repositories (`ai.elaro.store`)**
- One per entity: `SourceSettingsRepository`, `SourceCredentialRepository`, `SourceHealthRepository`, `SourceUsageRepository`, `RecordVersionRepository`, `RecordHeadRepository`, `TriggerRuleRepository`, `AlertRepository`, `WebhookRepository`, `WebhookDeliveryRepository`, `AppSettingRepository`, `SystemSecretRepository`
- Projections: `UsageTotals`, `KindCount`

**Config (`ai.elaro.config`)**
- `SchemaBootstrap` — wires both phases and the `EntityManagerFactory` dependency
- `PostgisExtensionInitializer` — phase 1, `CREATE EXTENSION IF NOT EXISTS postgis`, fails fast with operator instructions
- `SpatialIndexInitializer` — phase 2, highest-precedence `ApplicationRunner`, one `STATEMENTS` list
- `HibernateJsonConfig` — pins the `jsonb` format mapper to Boot's Jackson 3 `JsonMapper`

**Settings (`ai.elaro.settings`)**
- `AppSettingService`, `AppSettingChangedEvent`, `SettingKeys`
- `RetentionSettings`, `CorsSettings`
- `Durations`, `DurationShorthandModule`, `JacksonDurationConfig`

**Test infrastructure**
- `ai.elaro.test.DatabaseCleaner` (+ nested `TestConfig`), `DatabaseCleanupExtension`
- `ai.elaro.test.IntegrationTest` — modified: imports the cleaner, registers the extension, documents the convention
- `src/test/resources/application-test.yml` — modified: `ddl-auto: update`

**Tests written (not run)**
- `ai.elaro.settings.DurationsTest`, `DurationShorthandModuleTest`, `RetentionAndCorsSettingsTest`, `AppSettingServiceIT`, `JacksonDurationIT`
- `ai.elaro.store.EntityMappingIT`
- `ai.elaro.config.SchemaBootstrapIT`
- `ai.elaro.test.DatabaseCleanerIT`

## Deviations from the prompt

1. **The Hibernate JSON mapper had to be registered explicitly — the prompt's
   conditional resolved to "yes".** The prompt said to register a custom
   `FormatMapper` only "if Hibernate cannot find a compatible mapper on its own".
   Hibernate *can* find one, but it finds the wrong one. Decompiling
   `SessionFactoryOptionsBuilder.lambda$jsonFormatMapper$0` in Hibernate 7.4.5
   shows the default resolution order is OSON → **Jackson 2** → Jackson 3 → JSON-B,
   and `JacksonIntegration` selects Jackson 2 whenever
   `com.fasterxml.jackson.databind.ObjectMapper` is loadable. It is loadable here:
   `springdoc-openapi-starter-webmvc-ui` → `swagger-core-jakarta` brings Jackson
   2.21.5 onto the runtime classpath alongside Boot 4's Jackson 3.1.5. Left alone,
   every `jsonb` column would have been serialized by a private Jackson 2
   `ObjectMapper` with none of Boot's configuration (so no `Duration` shorthand),
   and the behaviour would have flipped silently if springdoc were ever removed.
   `HibernateJsonConfig` therefore sets `hibernate.type.json_format_mapper` to a
   `Jackson3JsonFormatMapper` built from the injected `JsonMapper` bean.

2. **`TriggerMode` uses an `AttributeConverter`, not `@Enumerated(STRING)`.** The
   prompt asked for `mode varchar(8)` with values `match`/`enter`/`exit` and to
   "map it as an enum". `@Enumerated(STRING)` writes the enum *name*, which is
   uppercase, so it would have stored `MATCH` and not matched the documented column
   values or the API representation. `TriggerModeConverter` keeps the Java type an
   enum while storing the lowercase wire value. `EntityMappingIT` asserts the stored
   text is `exit`. `DeliveryStatus` keeps `@Enumerated(STRING)` because the prompt
   explicitly asked for that and Architecture §7.1 does not pin its case.

3. **`Durations.format` never emits weeks.** `1w` parses (the prompt requires it),
   but seven days formats as `7d`. The prompt's own example says `7d`, and every
   duration in the OpenAPI document and Architecture §11 is expressed in days, so
   emitting `1w` would have contradicted the contract. `7d`/`1w` are the same length,
   so "shortest" did not decide it.

4. **Explicit `@JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)` on every `Instant`.** Hibernate
   already maps `Instant` to `timestamptz` on PostgreSQL by default, but only via the
   configurable `hibernate.type.preferred_instant_jdbc_type` preference. Since the
   prompt states all timestamps are `timestamptz`, the annotation pins it so the
   schema cannot drift with a settings change.

5. **`SourceUsageRepository.totalsForSource` uses `GROUP BY`, not `HAVING`.** A
   `HAVING` clause with no `GROUP BY` is valid SQL but poorly specified in JPQL, so
   the query groups by `u.id.sourceId`. The observable contract is unchanged: an
   empty window returns `Optional.empty()` rather than a row of zeroes, which keeps
   "no usage recorded" distinguishable from "zero usage".

6. **`AppSettingEntity.value` is a `String` of raw JSON.** This needed checking,
   because a `String` mapped to `jsonb` could plausibly be double-encoded into a
   JSON string literal. It is not: `AbstractJsonFormatMapper` special-cases
   `String.class` in both `toString` and `fromString` and passes the text through
   verbatim. Confirmed in the bytecode, and `AppSettingServiceIT` asserts
   `jsonb_typeof(value) = 'object'` to lock the behaviour in.

7. **`DatabaseCleaner` is not a `@Component`.** It lives in `ai.elaro.test`, which is
   under the scanned base package, so `@Component` plus the imported `TestConfig`
   would have registered it twice and made injection by type ambiguous. It is
   provided only by `DatabaseCleaner.TestConfig`, which `@IntegrationTest` imports.

8. **Three extra test classes beyond the list.** `DurationShorthandModuleTest` and
   `RetentionAndCorsSettingsTest` are plain unit tests covering the module and the
   settings records without needing a container; `JacksonDurationIT` was kept as the
   integration test the prompt offered as an alternative, because it is the only
   thing that proves the Boot customizer is actually wired. Together they cover the
   same ground faster and more precisely than one class would.

9. **Prompt-document edits were committed separately.** `prompts/002`, `prompts/003`
   and `prompts/016` were edited in the working tree during this session (the
   002 edit added §6 and `DatabaseCleanerIT`, both of which are implemented here).
   Following the convention set in 001, the `002: Persistence Model` commit contains
   only implementation.

## Issues found / open questions

1. **Two Jackson majors are on the classpath, and that is now load-bearing.** Jackson
   2.21.5 arrives only through springdoc → swagger-core. Deviation 1 removes the
   silent-behaviour-change risk for Hibernate, but anything else that probes the
   classpath for Jackson could still pick the wrong one. If prompt 017 can pin
   springdoc to something that does not drag in Jackson 2, that would remove the
   hazard at the source; otherwise this is worth a note in the production-hardening
   prompt.

2. **`@Version` added a `lock_version` column.** The prompt allowed an optimistic
   lock column but Architecture §7.1 does not list one, so it is named
   `lock_version` rather than `version` to avoid colliding with the record
   `version` concept used elsewhere. Prompt 010 should expect it on
   `source_settings` and decide whether the API surfaces it as an ETag.

3. **`record_version` has no foreign key from `record_head.seq`.** Architecture §7.1
   describes `record_head.seq` as "FK → record_version", but the ingest writer
   inserts both in one batch and later prompts may prune versions while keeping
   heads (§11 deletes heads first, then unreferenced versions). A database-level FK
   would make retention ordering brittle, so `seq` is a plain column. Flag for
   prompt 042 if a constraint is wanted in production.

4. **`DeliveryStatus` retention ages rows by `coalesce(delivered_at, next_attempt_at)`.**
   Architecture §11 says "delivered or failed `webhook_delivery` older than 7 days"
   without saying which timestamp decides. A failed row never gets a `delivered_at`,
   so the query falls back to its last attempt. Prompt 016 should confirm that is the
   intended reading.

5. **Nothing writes `RetentionSettings` or `CorsSettings` yet.** `SettingKeys` and the
   records exist and round-trip, but no code seeds them. `getOrDefault` deliberately
   does not write the default back, so a setting the operator never touches keeps
   following the shipped default. Prompts 003 (CORS) and 016 (retention) own the
   seeding and the API.

6. **`stream.limits` has no value record yet.** `SettingKeys.STREAM_LIMITS` is
   declared because the prompt asked for the constant, but its shape belongs to
   prompt 012.

## Tests written (not run)

| Test class | Covers |
|---|---|
| `DurationsTest` | ~30 cases: table-driven parse of both spellings, case-insensitivity, whitespace, 14 rejection cases, negative and overflow handling, message content, `parsePositive` zero handling, `format` table, the no-weeks rule, sub-second ISO fallback, and both round-trip directions |
| `DurationShorthandModuleTest` | 9 cases: shorthand write/read, ISO input, bare number as seconds, negative rejection, map values, `RetentionSettings` shape |
| `RetentionAndCorsSettingsTest` | 9 cases: both records' defaults, null-field fallbacks, `forSource` override precedence, defensive copying and immutability |
| `AppSettingServiceIT` | 17 cases: unset key, put/get for records and scalars, shorthand in the stored column, `jsonb_typeof` is `object`, overwrite, `getOrDefault` not persisting, cache hit and invalidation on `put`, `invalidateCache`, change events, null rejection, unreadable value, `updated_at` |
| `SchemaBootstrapIT` | 8 cases: extension present and functions callable, all three GiST indexes exist and really are GiST, idempotent re-run, `record_seq` increments by 1, all 12 tables created, no migration-history table |
| `EntityMappingIT` | 30 cases: every mutable entity round-tripped, both jsonb shapes (list and map), point and multipolygon geometry with SRID 4326, null geometry, both composite keys, `record_version` inserted via `JdbcTemplate` and read through the repository, `seq` monotonicity, lowercase `mode`, `DELIVERED` enum text, identity ids, usage totals over a half-open window, `countByKind` |
| `DatabaseCleanerIT` | 8 ordered cases: rows left by one test are gone in the next, `record_seq` restarts at 1, identity columns restart, schema and GiST indexes survive, `spatial_ref_sys` untouched, double-clean safe, discovered table list covers the model |
| `JacksonDurationIT` | 6 cases: the application mapper writes and reads shorthand and ISO, `RetentionSettings` serializes to the exact OpenAPI shape, both settings records round-trip |

Estimated coverage of this prompt's production code: high. `Durations`,
`DurationShorthandModule`, `RetentionSettings`, `CorsSettings` and
`AppSettingService` are covered line-by-line; the entities are covered through
`EntityMappingIT`; `SpatialIndexInitializer` and `PostgisExtensionInitializer`
have their success paths covered by `SchemaBootstrapIT`. The uncovered remainder
is the two initializers' failure branches, which need a database without PostGIS
or without privileges to reach.

**No tests were executed.** No `test`, `build`, `check` or `jacocoTestReport`
invocation was made, per guardrail 1.

One thing worth recording, since it is adjacent to that guardrail: to avoid
shipping assertions I could not check, I compiled two throwaway `main()` programs
against `build/classes` and ran them to probe *library behaviour* — Jackson's
treatment of a record component renamed with `@JsonProperty("default")`, and the
full `Durations` parse/format table. Both printed exactly what the tests assert
(including the precise JSON strings `JacksonDurationIT` pins), so those
expectations are known-good rather than hopeful. No test class was run, nothing
touched a database, and the probe files were deleted. The database-dependent
assertions remain unverified until prompt 040.

## Compile result

```
$ ./gradlew compileJava compileTestJava --no-daemon
> Task :compileJava
> Task :bootBuildInfo
> Task :processResources
> Task :classes
> Task :compileTestJava

BUILD SUCCESSFUL in 4s
```

No errors and no warnings.

## Notes for the next prompt

1. **Prompt 003 has its tables waiting.** `SystemSecretEntity` holds
   `admin_token_hash`; `SourceCredentialEntity` holds `ciphertext` + `nonce`.
   `ElaroProperties.dataDir()` is where `secret.key` belongs (Architecture §7.2),
   and `ElaroProperties.resetAdminToken()` is already bound.

2. **Use `AppSettingService` for CORS, and `SettingKeys.CORS`.** `CorsSettings.defaults()`
   is open reads / closed writes. Subscribe to `AppSettingChangedEvent` to rebuild a
   dynamic CORS configuration; the event fires after commit, so reading the setting
   back inside the listener is safe.

3. **`install.id` is `SettingKeys.INSTALL_ID`** and is unset; prompt 003 generates it.

4. **Inject `Clock`, not `Instant.now()`** — still true, and `AppSettingService` is the
   worked example. Entity timestamps are set by callers, not by lifecycle callbacks,
   so whoever saves an entity must set `updatedAt`/`createdAt` from the clock.

5. **Integration tests get empty tables for free** via `@IntegrationTest`; see its
   Javadoc. Do not add `@Transactional` to test classes expecting rollback-based
   isolation — cleaning is truncation-based, and the two do not need combining.

6. **`AppSettingService` caches across tests.** Its cache is a field on a singleton
   bean, so a test that writes settings should call `invalidateCache()` in
   `@BeforeEach`, as `AppSettingServiceIT` does. Truncation empties the table but
   cannot reach the cache.

7. **Hibernate's `jsonb` mapper is pinned** by `HibernateJsonConfig`. Any new jsonb
   column automatically uses Boot's Jackson 3 mapper, so `Duration` fields inside a
   jsonb payload will serialize as shorthand. Keep that in mind for payload shapes
   that should stay numeric.
