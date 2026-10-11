# 013-CR-Sources-API

| | |
|---|---|
| Prompt | 013-Sources-API.md |
| Status | Complete |
| Implementation commit | 4c294506fd8af985389cefa3dde2d613a997ce9a |
| Date | 2026-10-11 |

## Summary

This prompt exposes source management over REST. All eleven `Sources` operations assigned to 013 in
`docs/OpenAPI.json` are implemented, with the spec's paths, methods, status codes, field names and
operationIds:

- `listSources`, `listSourceHealth`, `getSource`, `updateSource`
- `setSourceCredentials`, `deleteSourceCredentials`
- `fetchSourceNow`, `probeSource`
- `getSourceHealth`, `getSourceUsage`, `listSourceFetches`

Each one is now marked `x-foresignal-status: implemented`.

**Controller.** `ai.foresignal.api.SourcesController` (`/v1/sources`) is thin. It calls
`SourceService`, `HealthService`, `ManualFetchService`, `ProbeService`, `UsageMeter` and the new
`FetchLogQuery`, and leaves mapping to `ai.foresignal.api.mapper`. Its Javadoc lists the six
admin-protected operations. `/v1/sources/health` is a literal mapping, which Spring prefers over
`/{sourceId}`, and a test checks that it is not shadowed.

**DTOs** (`ai.foresignal.api.dto`) mirror the spec schemas:

- `SourceSummary`, `SourceDetail`, `SourceDescriptorDto`, `SourceSettingsDto`, `SourceSettingsUpdate`
- `CredentialsUpdate`, `CredentialFieldDto`, `ParamSpecDto`, `BudgetDto`
- `SourceHealthDto`, `StorageDto`, `CoverageDto`
- `UsageSeries`, `UsagePoint`
- `FetchOutcomeDto`, `ProbeResultDto`, `NormalizedRecordPreviewDto`, `QualityDto`, `ObserverDto`
- `IngestPolicyDto`, `MotionSpecDto`, `FieldThresholdDto`
- `FetchLogEntryDto`, `FetchPage`

Field contents:

- **`SourceDescriptorDto`** carries every v1.2 descriptor field and every 008 extra present on the
  descriptor record: `titleField`, `entityKeys`, `fieldSpecs`, `kindDisplay`, `aggBelowZoom`,
  `startField`, `endField`, `private` and `privatePrecisionDecimals`.
- **`SourceSettingsDto`** adds the `ingestPolicy` override and the read-only `effectiveIngestPolicy`
  and `effectiveRetention`.
- **`SourceHealthDto`** adds `coverage`, `storage`, `lastDataAt`, `budgetHoursRemaining`,
  `newestFrameAt` and `frameAgeSeconds`. All values are copied from `HealthService`'s
  `SourceHealthView`; nothing is recomputed.
- **`FetchOutcomeDto`** has all the counts and `fetchId`, plus `byReason` with all nine
  `PersistReason` names, zeros included. `recordsChanged` is `persisted`.
- **`UsagePoint`** has the eight ingest counters. `UsageSeries.totals` sums the points.

**Ingest policy.**

- `IngestPolicyCodec` is now the single JSON shape, for the API and for
  `source_settings.ingest_policy`. A missing keyframe is written as `"keyframe": "none"`. Reading
  accepts `"none"`, `null` or an absent key, so rows stored before this prompt still read.
- `SourceMapper` turns an `IngestPolicyDto` into the domain policy through the codec, after checking
  the shape: mode, duration syntax, SAMPLED without `minInterval`, and `ALL` + `none` on a TTL source.
- `SourceService` now reports `IngestPolicyValidator` violations as `400 INGEST_POLICY_INVALID`,
  with one `errors[]` entry per violation, and the field taken from the message (for example
  `ingestPolicy.keyframe`).
- An explicit `"ingestPolicy": null` clears the override; an omitted key leaves it alone. Retention
  follows the same rule.

**Retention on TRACK** still throws `RETENTION_TIER_FIXED` (400). The message now names
`retention.trackWindow`. `effectiveRetention` for TRACK reads `RetentionSettings.trackWindow`
(default 3d). For STANDARD it is the source's own window, else the global default for the source.

**Fetch log.**

- `ai.foresignal.store.query.FetchLogQuery` (`NamedParameterJdbcTemplate`) pages
  `select id from fetch_log where source_id = :s [and id < :after] [and status in (:st)]
  [and trigger in (:tr)] order by id desc limit :limit + 1`.
- It then loads the entities through 010's `FetchLogRepository`, reusing the entity mapping
  including `coverage`.
- `nextAfter` is the last id of a full page, and null on the last page.

**Access.** `AccessPolicy` has `GET /v1/sources/*/fetches → ADMIN` above the `GET /v1/sources/**`
READ rule. `AccessPolicyTest` covers it.

**Docs.**

- The spec has the status flips plus two reconciliations (Deviations 6 and 7), recorded in
  `info.x-foresignal-changelog`.
- Postman was regenerated. The `updateSource` example body now carries a SAMPLED `ingestPolicy`.
- New `docs/Operator-Guide.md` has a "Sources" section: tiers, policies and overrides, the fetch log,
  and reading `storage` and `budgetHoursRemaining`.

## Files created / modified

**Created (main)**
- `api/SourcesController.java`
- `api/dto/*` (24 records and classes, plus `package-info.java`)
- `api/mapper/SourceMapper.java`, `HealthMapper.java`, `FetchOutcomeMapper.java`, `UsageMapper.java`,
  `FetchLogMapper.java`, `package-info.java`
- `store/query/FetchLogQuery.java`, `package-info.java`

**Modified (main)**
- `api/error/ErrorCodes.java`: `INGEST_POLICY_INVALID`, `RETENTION_TIER_FIXED`
- `api/error/ValidationException.java`: constructor taking a code
- `source/SourceService.java`:
  - `INGEST_POLICY_INVALID` with field paths (`policyFieldOf`)
  - the `retention.trackWindow` message
  - new `ingestPolicyOverride(id)` and `retentionOverride(id)`
- `source/IngestPolicyCodec.java`: `toTree`/`fromTree`, and `keyframe: "none"`
- `security/AccessPolicy.java`: the fetches rule

**Tests**
- New: `api/SourcesControllerIT.java`, `api/SourceFetchesIT.java`,
  `api/mapper/FetchOutcomeMapperTest.java`, `api/mapper/SourceMapperTest.java`,
  `source/IngestPolicyCodecTest.java`, `source/PolicyFieldPathTest.java`
- Modified:
  - `security/AccessPolicyTest.java`
  - `security/AdminTokenFilterIT.java` (Deviation 5)
  - `source/SourceServiceIT.java`: the invalid-policy test now expects `INGEST_POLICY_INVALID` and
    `ingestPolicy.keyframe`; the TRACK test also checks for `retention.trackWindow`

**Docs and tooling**
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `scripts/generate-postman.py`
  (the `BODIES` entry), `docs/Operator-Guide.md` (new)

No dependency changes. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` changes. Nothing was
ported from God's Eye View.

## Deviations from the prompt

1. **`SourceSettingsUpdate` (API) is a class with setters, not a record.** A record cannot tell an
   omitted key from an explicit `null`. Jackson calls a setter for `null` and does not call it for an
   absent key, so the setters for `retention` and `ingestPolicy` record that the key was present.
2. **Input durations are strings in the DTO.** The mapper parses them, so `"interval": "soon"`
   returns `400 VALIDATION_FAILED` with `errors[].field = "interval"`. With `Duration`-typed fields,
   Jackson would reject the whole body as `MALFORMED_REQUEST` with no `errors[]`. Output DTOs use
   `Duration` and the prompt 002 shorthand serializer. `IngestPolicyDto` uses strings throughout,
   because `keyframe` can be `"none"`.
3. **The fetch-log filters use `IN (:list)`, not `= ANY(:array)`.** This avoids binding a SQL array
   through `NamedParameterJdbcTemplate`, and the meaning is the same. The optional clauses are
   appended only when set, instead of `(:after IS NULL OR …)`, which PostgreSQL cannot type when the
   parameter is null. The page is chosen natively, and the rows are loaded through the 010 JPA
   entity, as the prompt allows.
4. **The probe `requests` reuse `collect.ProbeResult.Request`,** which 010 already built in the
   spec's request shape. `FieldSpec` and `KindDisplay` are also serialized from their SPI records,
   which already match the spec names, including `colour`.
5. **`AdminTokenFilterIT` lost two stub endpoints.** Its stub controller mapped `GET /v1/sources` and
   `PUT /v1/sources/{sourceId}`, which would be ambiguous with the real controller and stop that
   context from starting. The stubs are removed. `writeWithTokenSucceeds` now sends `{}` to the real
   `updateSource` and asserts `descriptor.id`.
6. **Spec reconciliation:** `updateSource`'s 400 description said
   `INGEST_POLICY_INVALID, RETENTION_TIER_FIXED (014)`. 014 is a pre-v1.3 number, so I changed it to
   `(013)`.
7. **Spec reconciliation (description only):** `SourceDescriptor.defaultIngestPolicy` now says it is
   reported with the §5.1 defaults filled in: the ALL keyframe, and `edgeMinInterval = minInterval/2`.
   That is what the API returns, so a client sees the keyframe actually in force.
8. **Combined errors.** When a body has both policy and non-policy problems, the response code is
   `INGEST_POLICY_INVALID` and `errors[]` holds all of them. Shape problems found before the service
   (for example a bad duration in `interval`) are reported first, as `VALIDATION_FAILED`.
9. **A validator message that names no field** gets the field `ingestPolicy.keyframe`. The one such
   message is "TTL too short for any keyframe", and the keyframe is what the operator would change.
10. **`params` entries with a JSON `null` value are dropped** before validation, so they fall back to
    the parameter's default. The service's `Map.copyOf` would otherwise throw a 500.
11. **`pending-operations.txt` does not exist yet;** prompt 020 creates it. There were no lines to
    delete.

## Issues found / open questions

1. **Spec fields with no backing in code yet:**
   - `SourceDescriptor`: `acceptsRemoteMedia`, `egress`, `egressOverridable`, `llmNotice`,
     `licenceTerms`, `reliability`, `derivedFrom`, `originator`, `upstreamRefFields`,
     `observability`, `access`
   - `SourceSummary`: `access`
   - `SourceSettings`: `embed`, `statsEnabled`, `statsIncludePrivate`, `private`, `regionTagging`,
     `downsampleInterval`, `egress`, `reliability`
   - `SourceHealth`: `details`, `lastDoctorReport`, `spentUsdThisMonth`
   - `FieldSpec`: `unitLabel`, `format`, `enumLabels`, `noLink`
   - `KindDisplay`: `rotateField`, `sizeField`, `colorField`

   The spec assigns each of these to a later prompt (024, 038–040, 044, 054, 062, 063, 069, 076,
   090, 109, 116, 135, 140). They are not emitted, and unknown keys in a PUT body are ignored. None
   of them belongs to 013. No 008 extra is missing from the descriptor record.
2. **`MotionSpec.courseField` is nullable in the spec, but the SPI record requires it.** The DTO
   passes the value through as it is. **Owner:** decide whether the spec or the SPI should change.
3. **`IngestPolicyLatest.minInterval` is in the spec, but `IngestPolicy.Latest` has no such
   component.** A `minInterval` sent with LATEST is ignored. **Owner:** decide whether LATEST should
   carry one.
4. **`Budget.mbPerDay` (005) is not in the `Budget` schema.** I left it out of the API. A PUT keeps
   the descriptor's value, as `SourceService` already did.
5. **Clearing retention on a TRACK source is still accepted as a no-op.** The spec's update
   description puts "400 RETENTION_TIER_FIXED on TRACK sources" right after the `retention=null`
   text. I read that as applying to setting a window, which is what Core-Data-Model §13 says.
   **Owner:** say whether `null` should also be refused.
6. **The prompt 012 owner decisions are still open:** `touched` semantics, and LGPL
   `geolatte-geom`. The `FetchOutcome.touched` description in the DTO Javadoc follows 009's
   behavior: observations, not rows.

No instructions directed at an agent were found in data, fixtures or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run.

- **`SourcesControllerIT`** (`@IntegrationTest`, MockMvc, `CollectorTestSources` and
  `TestIngestSources` fakes):
  - list:
    - sorted by id
    - summary fields, including tier, licence, status, `lastDataAt` and `credentialsRequired`
  - detail: TRACK descriptor fields (tier, time base, anchor, TTL, default policy with defaults,
    significant fields, motion, thresholds, skew, private, products), settings (`ingestPolicy`,
    effective policy, `effectiveRetention = 3d`) and health (`storage`, `coverage`, `lastDataAt`,
    `budgetHoursRemaining`, frame fields)
  - USGS: `fieldSpecs`, licence, `defaultRetention`/`effectiveRetention` 1825d, and the param
    default
  - policy fallback to the descriptor default, with `keyframe: none`
  - 404 `NOT_FOUND` on every operation
  - health: the list is not shadowed, the status filter works, a bad status is 400; single health
  - update:
    - partial change
    - retention set and cleared
    - interval below the minimum, bad durations, unknown param, negative budget: each 400 with
      `errors[]`
    - 409 `CREDENTIALS_REQUIRED`
    - policy round trip: set, read, omitted key, explicit null
    - invalid policies: 400 `INGEST_POLICY_INVALID` with field paths for minInterval/keyframe,
      mode without significant fields, a bad keyframe duration, an unknown mode
    - TRACK retention: 400 `RETENTION_TIER_FIXED` naming `retention.trackWindow`
    - malformed JSON
  - credentials:
    - set, enable, read through every GET, delete: the secret appears in no response body
    - an undeclared key is 400 without echoing the secret
    - a missing `values` is 400
  - fetch now:
    - outcome counts, `byReason` with 9 keys, `fetchId`, no `status`/`skipReason`
    - a push source is 409
  - probe:
    - previews with `eventTime`/`quality`/`observers`
    - no `fetch_log` rows
    - `maxBytes`/`seconds` out of range is 400
  - usage:
    - minute and day series with the ingest counters and totals
    - defaults: last 24 h at hour resolution
    - 400 for over 31 days at minute resolution, a reversed range, a bad resolution, a bad instant
  - auth:
    - 401 without the token on every write and on `listSourceFetches`
    - the other GETs are open
- **`SourceFetchesIT`**:
  - seeds seven rows through `FetchExecutor.execute` with a scripted adapter (OK, NOT_MODIFIED and
    FAILED; SCHEDULED and MANUAL)
  - keyset paging in pages of 3: exact newest-first order, no duplicates, `nextAfter` equal to the
    last id, null on the last page (also on an exact final page)
  - status and trigger filters, repeated and comma-separated, alone and combined
  - entry shape, with no body fields
  - 400 on `limit`, `after`, `status`, `trigger`
  - 401 without the token
  - scoped to its source
- **`FetchOutcomeMapperTest`**: `byReason` has all nine reasons in order, zeros included, and a
  null map still gives nine zeros; `recordsChanged == persisted`; every count copied; failure;
  previews with quality and observers; empty quality.
- **`SourceMapperTest`** (Mockito `AppSettingService`):
  - every descriptor field; JSON names (`private`, `colour`, `default`)
  - shorthand durations; no `mbPerDay`; `keyframe: none` on ALL without a TTL
  - summary; `credentialsRequired` with optional credentials only
  - settings for TRACK (`trackWindow`) and STANDARD (own window or global); the settings fallback
  - policy round trip (SAMPLED); `keyframe: none` both ways; lenient mode and ISO-8601 input
  - shape errors with paths; required pieces; nulls; the discriminated JSON omitting absent fields
  - update: field parsing, `mbPerDay` kept, null params dropped, explicit-null clears,
    multi-error `VALIDATION_FAILED`
- **`IngestPolicyCodecTest`**: shorthand writing, `none` both ways, legacy `null` rows, round trip
  of every mode through JSON and tree, and the error edges.
- **`PolicyFieldPathTest`**: validator messages mapped to `errors[].field`.
- Updated: `AccessPolicyTest` (the fetches rule and its neighbors), `AdminTokenFilterIT`,
  `SourceServiceIT`.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
4 actionable tasks: 3 executed, 1 up-to-date
```

The only other output is the existing unchecked-operations note on `ParamValidatorTest`.

## Notes for the next prompt

1. **014 (Records/Changes API) and later controllers:**
   - Follow this layout: controller in `ai.foresignal.api`, records in `api.dto`, mappers in
     `api.mapper`, native read queries in `store.query`.
   - `ValidationException(code, detail, errors)` gives any 400 code with `errors[]`.
   - For a partial-update body that must tell omitted from `null`, copy the setter-presence pattern
     in `api.dto.SourceSettingsUpdate`.
2. **`IngestPolicyCodec` writes `"keyframe": "none"`.** Anything reading
   `source_settings.ingest_policy` directly must accept `"none"` as well as `null`.
3. **Test contexts.** `SourcesControllerIT` and `SourceFetchesIT` share one context
   (`@IntegrationTest` + `@AutoConfigureMockMvc` + `@Import(CollectorTestSources.class)`). It is new,
   so it adds one Hikari pool. `UsageMeter`'s unflushed counters outlive table cleanup, so usage
   tests use a minute no other test touches.
4. **020 (pending operations):** the eleven 013 operations are implemented. Do not list them in
   `pending-operations.txt`. `fetchSourceNow`'s `allowShrink` belongs to 094.
5. **025 (MCP):** `SourceService` (now with `ingestPolicyOverride` and `retentionOverride`) and
   `HealthService` have no web types. `SourceMapper` and the other mappers can be reused to give the
   same JSON shapes.
6. **Owner decisions pending:** Issues 2, 3 and 5 here, plus the two from 012.
