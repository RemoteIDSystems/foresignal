# 011-CR-USGS-Earthquakes-Adapter

| | |
|---|---|
| Prompt | 011-USGS-Earthquakes-Adapter.md |
| Status | Complete |
| Implementation commit | 2ff62ce938fc367a90a485bbb3bc757e673c8a14 |
| Date | 2026-10-11 |

## Summary

This prompt adds the first real adapter on the v1.2 pipeline, `usgs-earthquakes`, plus the Adapter
Guide and the fieldSpecs contract test that later adapters follow. All new main code is in
`ai.foresignal.source.adapters.usgs`.

**`UsgsEarthquakesAdapter`** (`@Component`, `PollingAdapter`):
- The descriptor follows the prompt's table, with one change: `defaultRetention` is 1825 d (deviation 1).
- `feed` is an enumerated string param over the 20 published feeds, default `all_day`.
- `fetch` does `ctx.http().getConditional(baseUrl + "/earthquakes/feed/v1.0/summary/" + feed + ".geojson", "feed:" + feed)`:
  - a 304 → `FetchResult.unchanged()`
  - a truncated body → `UpstreamFormatException`
  - otherwise it parses, logs the `ParseDiagnostics` through `ctx.log()` (WARN when anything was
    skipped), and returns `FetchResult.complete(records)`
- `baseUrl` is `https://earthquake.usgs.gov`. The only override is the test property
  `foresignal.test.adapters.usgs.base-url`, read with `@Value` in the single constructor.

**`UsgsGeoJsonParser`** is a pure static parser using Jackson 3 trees:
- `parse(byte[])` and `parseWithDiagnostics(byte[])`, which returns `ParseResult(records, ParseDiagnostics)`.
- Time base: `eventTime = time` and `observedAt = updated`, always. A feature without `updated` is
  skipped and counted.
- Geometry is a 2D `Geo.point(lon, lat)`. `coordinates[2]` goes to payload `depth`.
- The payload is the 26-key allowlist (`PAYLOAD_TYPES`), with nulls and absent keys omitted and `tz`
  dropped.
- Values are coerced to their declared type: NUMBER → `Double` (so `2` and `2.0` hash equal),
  INTEGER/TIMESTAMP → `Long`. A value of an unexpected JSON type is kept verbatim, so the contract
  test flags the format change.
- `status: deleted` → `NormalizedRecord.tombstone(id, "earthquake", updated)`.
- These are skipped and counted:
  - malformed features (not an object, or no `properties` object)
  - a missing id
  - missing or non-numeric coordinates
  - out-of-range coordinates (so one bad feature cannot fail the whole fetch)
  - a missing `updated`
- `UpstreamFormatException` (snippet ≤ 500 characters) is thrown for an empty body, invalid JSON, a
  non-object, a document that is not a `FeatureCollection`, or a missing `features` array.

**Fixtures** in `src/test/resources/fixtures/usgs-earthquakes/`:
- `all_day.geojson`: a real capture, trimmed to 25 features.
- `revised.geojson`, `updated-only.geojson`, `edge-cases.geojson` and
  `not-a-feature-collection.json`.
- A `README.md` with the capture date, URL, command, headers and trimming method.

**Test support** in `testsupport/adapters`:
- `FieldSpecContract.assertPayloadKeysDeclared(descriptor, records)`. It lists every payload key not
  declared for the record's kind (respecting `FieldSpec.kinds`) and every value that contradicts its
  `FieldType`.
- `AdapterFixtures`, for reading fixtures from the classpath.

**Docs:**
- `docs/Adapter-Guide.md`, new, covers:
  - layout, parser purity, and rules 1–13
  - the descriptor checklist with the USGS values
  - the time-base table
  - the altitude convention and the fieldSpecs contract
  - etiquette, fixtures and the required tests, and the test base-URL property
  - porting, families, streaming, PUSH and DERIVED sources
  - the repair procedure
- `docs/Source-Catalog.md`: the heading is now "(prompt 011)", and the `usgs-earthquakes` row has the
  tier, policy, TTL, anchors, field sets, retention, license and the verified 2026-10
  conditional-GET note.

**Verified against the live upstream:**
- I made one fixture capture on 2026-10-11 at 22:32:45 UTC, with the User-Agent the prompt gives.
- HTTP 200, `last-modified` present, **no `etag`**, `cache-control: public, max-age=60`.
- `metadata.api` was `2.7.0`, with 174 features, and `tz` was null on all of them.
- I did not send a live `If-Modified-Since` request in this session. The 304 behavior comes from
  the prompt's 2026-10 verification. WireMock tests cover it.

## Files created / modified

**Created (main)**
- `source/adapters/usgs/UsgsEarthquakesAdapter.java`, `UsgsGeoJsonParser.java`, `package-info.java`

**Modified (main)**
- `source/spi/FieldType.java`: Javadoc for TIMESTAMP only (deviation 2)

**Created (tests)**
- `source/adapters/usgs/UsgsGeoJsonParserTest.java`, `UsgsEarthquakesAdapterTest.java`, `UsgsIngestIT.java`
- `testsupport/adapters/FieldSpecContract.java`, `FieldSpecContractTest.java`, `AdapterFixtures.java`
- `src/test/resources/fixtures/usgs-earthquakes/`: 5 fixtures and `README.md`

**Docs**
- `docs/Adapter-Guide.md` (new), `docs/Source-Catalog.md`

No dependency changes. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` changes. OpenAPI and
Postman are untouched, since this prompt has no API change. `THIRD_PARTY_NOTICES.md` is unchanged,
because nothing was ported from God's Eye View (deviation 6).

## Deviations from the prompt

1. **Default retention is 1825 days, not 7.** The prompt's catalog row says "retention default
   (7d)". `docs/Conventions.md` ("Analytic retention") puts earthquakes in the `event` class at
   1825 d and says that a shorter window an adapter prompt names is overridden. Conventions wins, so
   the descriptor sets `defaultRetention(1825 d)`, and the catalog row says so.
2. **TIMESTAMP accepts epoch integers.** `FieldType.TIMESTAMP` was documented as "an ISO-8601
   instant carried as a string". The prompt declares USGS `time`/`updated` as TIMESTAMP with unit
   `ms`, and the payload keeps them as epoch-millisecond integers (verbatim from the upstream). I
   widened the Javadoc to "an ISO-8601 string, or an integer epoch count in the declared unit", and
   `FieldSpecContract` accepts both. The OpenAPI `FieldSpec` enum is unchanged, since it has no
   per-value description.
3. **`tsunami` is INTEGER `1`, not a string.** The prompt says "strings for the rest", but the
   upstream sends `tsunami` as the integer 0 or 1. Declaring it STRING would make the contract test
   fail on real data.
4. **No live `If-Modified-Since` check in this session.** The capture confirmed `Last-Modified` and
   no `ETag`. The 304 claim rests on the prompt's own 2026-10 verification and is exercised in
   WireMock tests.
5. **`updated-only.geojson` advances `updated` by 2 h, on `us6000u1i6`.** The prompt asks that
   `record_live.observed_at` advance after the `updated`-only fetch. A `TOUCH_THROTTLED` writes
   nothing (009 deviation 7), so the advance has to exceed `touchMin` (1 h without a TTL) to become
   a TOUCH. I chose the oldest feature so the new `updated` is still before the test clock. A
   smaller advance would still give 0 versions, but `observed_at` would not move.
6. **Nothing ported from God's Eye View.** GEV's `records.js` filters to M2.5+ and rejects whole
   snapshots on any bad feature, and neither fits Foresignal. The adapter was written independently,
   so there is no origin header and no notices entry.
7. **Extra parser behavior beyond the prompt's list:**
   - out-of-range coordinates and malformed features are skipped and counted, rather than throwing
   - a truncated response throws `UpstreamFormatException` in the adapter
8. **`UsgsEarthquakesAdapterTest` is an `@IntegrationTest`,** like `UpstreamClientIT`. A real
   `UpstreamClient` needs the database for usage, backoff and adapter state, and the stored
   `Last-Modified` is what the conditional round trip tests. It shares the standard cached context.
9. **`UsgsIngestIT` is its own context.** It turns the scheduler on and registers the WireMock base
   URL with `@DynamicPropertySource`. Each test:
   - enables the source with a raised budget (the descriptor's 4 a minute would refuse later
     fetches)
   - waits for the scheduler's first fetch
   - disables the source, then drives later fetches with `FetchExecutor.execute(id, MANUAL)`
10. **"`touches_throttled` counted"** is asserted both on `FetchOutcome.touchesThrottled` and on the
    `foresignal.ingest.records.touches_throttled{source}` counter. There is no `fetch_log` column for
    it.

## Issues found / open questions

1. **`docs/Core-Data-Model.md` still says "009 USGS"** under *Per-prompt changes*, and calls the
   Sources API and Records API "010" and "011". That is pre-v1.3 numbering, which `docs/Roadmap.md`
   §5 translates. Left for the planner.
2. **The fixture is time-sensitive only through the test clock.** `UsgsIngestIT` sets the clock to
   2026-10-11T22:33:00Z, just after the capture. A refreshed fixture needs that constant and the ids
   in the tests updated, as the fixture README says.
3. **The persisted names stay as they are:** `licence` on the descriptor and the `fetch_log.licence`
   column. Prose uses "license".

No instructions directed at an agent were found in the upstream response, the fixtures or any tool
output.

## Tests written (not run)

As a feature prompt requires, no tests were run.

- `UsgsGeoJsonParserTest` (about 25 cases, some parameterized):
  - recorded feed: count and order; no skips; time base; 2D SRID 4326 point with `depth` in the
    payload (including a negative depth); spot-checked typed values with nulls omitted; integer
    magnitude → double
  - edge cases: skip counts for id, geometry and `updated`; omitted nulls; missing depth;
    deleted → tombstone
  - shape: not a FeatureCollection → `UpstreamFormatException` with snippet; missing or non-array
    `features`, a non-object and invalid JSON; empty body; snippet cap; empty `features` → empty
    list; malformed features; bad coordinates; numeric id
  - coercion rules
  - contracts: `FieldSpecContract` on all four parsed fixtures; the allowlist equals the `fieldSpecs`
    keys and types; `SnapshotContract.assertStableStaticHash` for `all_day` vs `updated-only`; the
    revision changes the static hash
- `UsgsEarthquakesAdapterTest` (WireMock, real `UpstreamClient`):
  - the default feed, and the URL per `feed`
  - User-Agent equal to `InstallIdService.userAgent()` and of the identifiable form
  - conditional GET round trip: 200 with `Last-Modified` stored, then `If-Modified-Since` → 304 →
    `unchanged()`
  - validators kept per feed
  - 500 → `UpstreamHttpException(500)`
  - format change → `UpstreamFormatException`
  - empty feed → a complete, empty result
  - descriptor passes `DescriptorValidator`, and every prompt value is asserted, along with the
    `feed` param, the units and datum, and the base-URL override
- `UsgsIngestIT` (5), through the scheduler and the real writer:
  - first fetch: 25 FIRST versions, one OK SCHEDULED `fetch_log` row with `versions_written = 25`,
    licence and HTTP status; health HEALTHY; head `event_time` and live `observed_at` checked
  - revised: exactly one version 2, SIGNIFICANT, `prev_seq` = the version-1 seq, `event_time`
    unchanged
  - identical re-serve: 0 versions, 25 throttled touches (outcome and counter), ChangeBus watermark
    unchanged and no batch delivered to a subscriber; after +61 min, 25 TOUCHes advance
    `record_live.last_received_at` to the clock
  - `updated-only`: 0 versions, one TOUCH, `record_live.observed_at` advanced on that entity only
  - 304: a NOT_MODIFIED row, `last_data_at` unchanged, `If-Modified-Since` sent
- `FieldSpecContractTest` (5): passes; undeclared keys, including a kind-restricted spec; mistyped
  values; tombstones skipped; type rules.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 545ms
4 actionable tasks: 2 executed, 2 up-to-date
```

## Notes for the next prompt

1. **012 (checkpoint)** runs these classes for the first time. The ones most likely to need
   adjustment:
   - `UsgsIngestIT`: scheduler timing, and whether `HealthService` reports HEALTHY right after the
     first OK fetch
   - `UsgsEarthquakesAdapterTest`: the adapter-state key `lastmod:feed:all_day` comes from
     `BoundSourceHttp.LAST_MODIFIED_STATE_PREFIX`
2. **Later adapters copy this pattern** (`docs/Adapter-Guide.md`):
   - a pure parser with a `PAYLOAD_TYPES` allowlist
   - the descriptor built in a static initializer
   - one constructor taking `@Value("${foresignal.test.adapters.<name>.base-url:}")`
   - `FieldSpecContract` and `SnapshotContract` in the parser test
   - `AdapterFixtures.bytes(sourceId, name)` for fixtures
3. **013 (Sources API):** `usgs-earthquakes` is the first real source in every context's registry.
   It is disabled by default, so lists that assert on `registry.ids()` include it.
4. **Retention (019):** the descriptor's `defaultRetention` is 1825 d (the `event` class), so 019's
   resolver should pick it up ahead of the global default.
