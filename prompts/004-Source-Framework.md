# 004 — Source Framework

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/004-CR-Source-Framework.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/003-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §4 (adapter SPI, adapter rules, adapter families)
- `docs/Product-Charter.md` §8.1 (FR-1 to FR-5)
- `docs/OpenAPI.json` schemas `SourceDescriptor`, `ParamSpec`, `CredentialField`, `Budget`, `SourceSettings`, `SourceSettingsUpdate`, `CredentialsUpdate`

## Objective

Build the adapter SPI that every source implements, the registry that discovers adapters, parameter validation, and `SourceService`, which owns per-source settings and credentials. This defines the "subscription" half of the system: a source is subscribed when `enabled = true`. No network or scheduling code yet.

## Tasks

### 1. SPI types (`ai.elaro.source.spi`)

Implement as specified in Architecture §4.1, using Java records where possible:

- **Adapter interfaces:** `SourceAdapter`, `PollingAdapter`, `StreamingAdapter`, `SourceAdapterFamily` (`List<SourceAdapter> adapters()`).
- **`SourceDescriptor`:** with all fields including `mode`, `credentialFields`, `params`, `minInterval`, `defaultInterval`, `defaultBudget`, `recordTtl`, `zeroResultsNormal`, `tileProperties` and `searchFields`.
  - Provide a fluent `SourceDescriptor.builder(id)` with sensible defaults: `searchFields` defaults to `name, title, callsign, place`; `zeroResultsNormal` false; `fetchTimeout` 60 s; `maxResponseBytes` 32 MB.
  - Add `fetchTimeout` and `maxResponseBytes` to the descriptor as well.
  - Also add `embedFields` (payload fields whose text is embedded for semantic search, prompt 023; default empty) and `rankField` (an optional numeric payload field used to rank "notable" records in briefings, prompt 025).
- **Value types:** `Mode` (POLLING, STREAMING, PUSH; PUSH sources are never polled and receive data through the inbound endpoint in prompt 020), `CredentialField(name, description, required)`, `ParamSpec(name, type, description, defaultValue, required, allowed)` with `ParamType` matching OpenAPI (`string, integer, number, boolean, duration, bbox, string-list`), and `Budget(int perMinute, int perDay)`.
- **`NormalizedRecord`:** a builder plus static helpers in `Geo`:
  - `Geo.point(lon, lat)`
  - `Geo.fromGeoJson(String|Map)` using JTS `GeoJsonReader`; add the `org.locationtech.jts.io:jts-io-common` dependency matching the JTS version from Hibernate
  - SRID 4326 is always set
- **`FetchResult`:** `records`, `complete`, and a `notModified` flag with a static `FetchResult.notModified()`.
- **Contexts:** `FetchContext`, `StreamContext`, `RecordSink`, `StreamHandle` (AutoCloseable), `AdapterStateStore` (`get/put/remove` of string keys, persisted later), and `ValidatedParams` (typed getters `string()`, `integer()`, `duration()`, `bbox()`, `stringList()`, `bool()`).
- **Exceptions:**
  - `AdapterException` (checked)
  - `UpstreamFormatException extends AdapterException`, for parse failures. Include a snippet of the offending content (≤ 500 chars).
- `FetchContext.http()` returns a `SourceHttp` interface. **Declare the interface here** (methods listed below) but implement it in prompt 005:
  - `UpstreamResponse get(URI uri)`
  - `get(URI, Map<String,String> headers)`
  - `getConditional(URI, String stateKey)`
  - `post(URI, String contentType, byte[] body, Map<String,String> headers)`
  - `WebSocket openWebSocket(URI, WebSocket.Listener, Map<String,String> headers)`
- Declare `UpstreamResponse` as a record: `status`, `headers`, `body` (byte[]), `contentType`, `truncated`, `notModified`, and `String bodyAsString()`.

### 2. SourceRegistry (`ai.elaro.source`)

- Collects all `SourceAdapter` beans plus every adapter returned by `SourceAdapterFamily` beans.
- Validates at startup and fails fast with a clear message listing every problem:
  - id matches `^[a-z0-9]+(-[a-z0-9]+)*$` and is ≤ 64 characters
  - ids are unique
  - kinds is non-empty, and each kind matches the id pattern
  - `minInterval ≤ defaultInterval` (for polling)
  - budgets are > 0
  - param names are unique, and defaults are valid for their type
  - the mode matches the implemented interface (PUSH descriptors need no fetch implementation; add a `PushAdapter` marker interface)
- API: `all()`, `find(id)`, `get(id)` (throws `NotFoundException`), `descriptors()`, `kinds()`.
- **Dynamic registration:** `registerDynamic(SourceDescriptor)` and `unregisterDynamic(id)` for runtime-defined PUSH sources (custom sources, prompt 020). These apply the same validation, and publish `SourceRegistryChangedEvent`. Use a thread-safe structure, because reads happen on hot paths.

### 3. ParamValidator

- `ValidatedParams validate(SourceDescriptor d, Map<String,Object> raw)`:
  - applies defaults and coerces types (numbers from JSON, durations via `Durations`, bbox as `minLon,minLat,maxLon,maxLat` with range checks, string-list from a list or a CSV string)
  - enforces `required` and `allowed`
  - rejects unknown keys
  - collects **all** errors into a `ValidationException` with field paths like `params.feed`

### 4. SourceService (`ai.elaro.source`)

- **Startup:** for every registered source with no `source_settings` row, insert defaults (`enabled=false`, `interval=defaultInterval`, `params={}`, budget = `defaultBudget`, `retention=null`). Rows for unregistered sources are left untouched and logged once as orphaned.
- **Views:** `SourceView view(id)` and `List<SourceView> list()` return the descriptor plus effective settings plus `credentialsSet`. Health is added in prompt 006, so leave a seam.
- **`update(id, SourceSettingsUpdate)`:** partial update where omitted fields are unchanged.
  - Validates `interval ≥ minInterval`, budget values ≥ 1, params via `ParamValidator`, and retention via `Durations.parsePositive`. `retention: null` clears it.
  - When enabling, every **required** credential field must be stored, else `ConflictException("CREDENTIALS_REQUIRED")` naming the missing fields.
  - After commit, publish `SourceSettingsChangedEvent(sourceId, before, after)`.
- **`setCredentials(id, Map<String,String> values)`:**
  - keys must be a subset of the declared `credentialFields`, and every required field must be present
  - values must be non-blank
  - encrypt with `CredentialCipher` and upsert `source_credential`
  - publish `SourceCredentialsChangedEvent`
- **`deleteCredentials(id)`:** removes the row. If the source requires credentials and is enabled, disable it (and publish the settings event).
- **`Map<String,String> credentials(id)`:** decrypted. **Package-private or internal API only**, never exposed to controllers. Use a dedicated `CredentialsAccess` bean that only `collect` can use, and document this.
- **Effective settings:** `effectiveParams(id)` returns validated params; `effectiveBudget(id)`.
- All writes are `@Transactional`; events are published after commit (`@TransactionalEventListener` consumers, or `TransactionSynchronization`).

### 5. Test fixtures (`src/test/java/ai/elaro/testsupport/adapters`)

- `FakePollingAdapter`: configurable descriptor and a scripted sequence of `FetchResult`s or exceptions.
- `FakeStreamingAdapter`: emits scripted records to the sink.
- `FakeFamily`: three feeds.
- A `@TestConfiguration` that registers them. These are reused by prompts 005–008.

## Tests to write (do not run)

- `SourceRegistryTest`: discovery including families, and every validation failure (bad id, duplicates, interval inversion, mode mismatch, invalid default); dynamic register and unregister, duplicates rejected, event published.
- `ParamValidatorTest`: each type, defaults, required, allowed, unknown keys, multiple errors aggregated, bbox range errors.
- `SourceServiceIT`:
  - default rows created, orphan handling
  - partial update semantics
  - enabling without credentials gives 409 `CREDENTIALS_REQUIRED`
  - credential set/delete, including auto-disable
  - events fire only after commit
  - stored credentials are encrypted (the raw DB bytes do not contain the plaintext)
- `GeoTest`: point and GeoJSON parsing, SRID 4326, invalid GeoJSON errors.
- `SourceDescriptorBuilderTest`: defaults.

## Acceptance criteria

- The SPI matches Architecture §4 (record any deviations).
- Credentials are never returned by any public method used by controllers.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `004: Source Framework`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/004-CR-Source-Framework.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `004: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
