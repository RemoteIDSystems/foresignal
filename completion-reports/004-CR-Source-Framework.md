# 004-CR-Source-Framework

| | |
|---|---|
| Prompt | 004-Source-Framework.md |
| Status | Complete |
| Implementation commit | 875bad28cc314c11d96adf885b7e2bb50b15b5ae |
| Date | 2026-10-10 |

## Summary

The adapter SPI, the registry, parameter validation and `SourceService` are in
place, so the subscription half of the system now exists: a source is subscribed
when its settings row says `enabled = true`, and nothing else can cause an
upstream request. The SPI carries every v1.2 field from `docs/Core-Data-Model.md`
§3 plus the weather hooks from `docs/Weather-Imagery-Design.md` §10.1, so later
prompts extend it rather than reshape it. `DescriptorValidator` holds the
validation rules separately from the registry, which is what lets a runtime
custom source face exactly the same checks as a compile-time adapter. Two
security-shaped parameter types landed: `LAN_URL` pins the resolved address so
DNS rebinding cannot redirect a later connection, and `INBOX_PATH` plus
`InboxFiles` confine reads to the inbox with a per-file realpath re-check. The
`IllegalArgumentException` mapping the 003 report flagged is fixed.

## Files created / modified

**SPI (`ai.elaro.source.spi`)** — 34 files
- Adapters: `SourceAdapter`, `PollingAdapter`, `StreamingAdapter`, `PushAdapter`, `SourceAdapterFamily`
- `SourceDescriptor` (+ `Builder`), `Mode`, `HistoryTier`, `ObservedAtMode`, `TtlAnchor`
- `ParamSpec`, `ParamType`, `CredentialField`, `Budget`
- `NormalizedRecord` (+ `Builder`), `Quality`, `PositionSource`, `Observer`, `Coverage`, `FetchResult`
- `IngestPolicy` (sealed), `IngestPolicyValidator`, `MotionSpec`, `FieldThreshold`, `RecordLimits`
- `ProductSpec`, `Geo`
- Contexts: `FetchContext`, `StreamContext`, `RecordSink`, `StreamHandle`, `AdapterStateStore`, `ValidatedParams`
- `SourceHttp`, `UpstreamResponse`
- `AdapterException`, `UpstreamFormatException`
- `package-info.java` carrying adapter rules 7–13

**Framework (`ai.elaro.source`)**
- `SourceRegistry`, `DescriptorValidator`, `SourceRegistryChangedEvent`
- `ParamValidator`, `LocalAddresses`, `InboxFiles`
- `SourceService`, `SourceView`, `SourceSettingsUpdate`, `CredentialsAccess`, `IngestPolicyCodec`
- `SourceSettingsChangedEvent`, `SourceCredentialsChangedEvent`, `package-info.java`

**Modified**
- `ai.elaro.api.error.InvalidInputException` (new), `GlobalExceptionHandler`, `Durations`, `DurationShorthandModule`, `AppSettingService` — the 003 error-mapping fix
- `ai.elaro.store.SourceSettingsEntity` — added the `ingest_policy` jsonb column
- `ai.elaro.config.ElaroProperties`, `application.yml` — added `elaro.inbox-dir`
- `gradle/libs.versions.toml`, `build.gradle.kts` — added `jts-io-common`

**Test fixtures (`ai.elaro.testsupport.adapters`)** — reused by prompts 005–008
- `FakePollingAdapter` (scripted results and failures), `FakeStreamingAdapter`, `FakePushAdapter`, `FakeFamily` (three feeds), `FakeAdapters` (`@TestConfiguration`)

**Tests written (not run)**
- `ai.elaro.source.spi`: `SourceDescriptorBuilderTest`, `GeoTest`, `IngestPolicyValidatorTest`
- `ai.elaro.source`: `SourceRegistryTest`, `ParamValidatorTest`, `SourceServiceIT`, `InboxFilesTest`, `LocalAddressesTest`
- Updated: `GlobalExceptionHandlerTest`, `DurationsTest`, `AppSettingServiceIT`, `ElaroPropertiesTest`, `ElaroVersionTest`, `AdminTokenServiceIT`

## Deviations from the prompt

1. **The `jts-io-common` coordinates in the prompt are wrong, and the artifact
   drags in `json-simple`.** The prompt says
   `org.locationtech.jts:jts-io-common`; the real groupId is
   `org.locationtech.jts.io`. Added `org.locationtech.jts.io:jts-io-common:1.19.0`,
   matching the `jts-core:1.19.0` the Boot BOM resolves through
   `hibernate-spatial`. It brings `com.googlecode.json-simple:json-simple:1.1.1`
   (compile scope) — a 2012 release that is unmaintained. **This is reachable from
   untrusted input**: `GeoJsonReader` parses upstream-supplied GeoJSON, so a
   parser defect in json-simple sits directly on the ingest path. I followed the
   prompt rather than hand-rolling a Jackson 3 GeoJSON reader, but this is worth a
   deliberate decision; see "Issues" item 1.

2. **`FetchResult.notModified()` is named `unchanged()`.** The prompt asks for a
   `notModified` flag *and* a static `FetchResult.notModified()`. That cannot
   compile: the record component generates a no-argument `notModified()` accessor,
   and Java forbids a static method with the same signature. Kept the accessor,
   since the flag is part of the documented shape, and named the factory
   `FetchResult.unchanged()`.

3. **`Geo.fromGeoJson` rejects an empty geometry.** `{"type":"Point"}` with no
   coordinates parses to `POINT EMPTY` rather than failing — verified against the
   library. Storing that would be worse than rejecting it: an empty geometry is a
   missing position, and it behaves surprisingly in spatial predicates and tiles.
   It now raises `UpstreamFormatException`.

4. **`UpstreamFormatException` is unchecked.** The prompt places it under
   `AdapterException`, which is checked. Parsers are required to be pure functions
   (adapter rule 3) so they can be fixture-tested; making every parser declare
   `throws` would push that checked exception through signatures that have no
   other reason to have one. It extends `RuntimeException` and the collector treats
   it identically. `AdapterException` itself stays checked, as specified.

5. **`AccessRequirement`-style modelling for the registry's return type.** The
   prompt describes `registerDynamic`/`unregisterDynamic` without saying which
   modes may be dynamic. Only `PUSH` and `DERIVED` are allowed, because anything
   else would need an implementation that a runtime descriptor cannot supply. A
   dynamic `POLLING` source is refused with a message saying so.

6. **`SourceSettingsUpdate` carries explicit `clearRetention` / `clearIngestPolicy`
   flags.** The prompt says "`retention: null` clears it" and "an explicit null
   clears it", but in a Java record null already means "field omitted". Two
   booleans distinguish the two intents. Prompt 010's controller will map a JSON
   null to the corresponding flag.

7. **Added `DescriptorValidator` as a separate class.** The prompt puts validation
   in `SourceRegistry`. Splitting it is what makes "dynamic sources get the same
   validation" verifiable rather than aspirational, and it lets the rules be unit
   tested without constructing a registry.

8. **`SourceSettingsUpdate` has no `embed` field.** The OpenAPI
   `SourceSettingsUpdate` schema includes `embed`, but this prompt's task list does
   not mention it and embeddings arrive in prompt 023. Left out deliberately; see
   "Notes" item 6.

9. **Added `LocalAddresses`, including a carrier-grade NAT check the prompt did not
   ask for.** `LAN_URL` validation must reject anything that is not the operator's
   own network. `InetAddress.isSiteLocalAddress()` alone would admit 100.64.0.0/10,
   which is routable carrier-grade NAT and includes at least one cloud metadata
   endpoint (100.100.100.200). IPv4-mapped IPv6 is also unwrapped, so
   `::ffff:169.254.169.254` is judged as the IPv4 address it is.

10. **Three extra test classes.** `InboxFilesTest` and `LocalAddressesTest` cover
    the two security-shaped helpers directly rather than only through
    `ParamValidatorTest`; `SourceDescriptorBuilderTest` is as the prompt asked.
    `FakePushAdapter` was added to the fixtures because the registry's mode-matching
    rules cannot be tested without a `PushAdapter`.

## Issues found / open questions

1. **`json-simple:1.1.1` is now on the compile and runtime classpath, reachable
   from upstream data.** It arrives only through `jts-io-common`, is unmaintained,
   and `GeoJsonReader` uses it to parse adapter-supplied GeoJSON. Three options for
   the planner: accept it; replace `Geo.fromGeoJson` with a ~60-line Jackson 3
   reader and drop the dependency; or keep `GeoJsonReader` but have adapters parse
   GeoJSON through Jackson and hand JTS only coordinate arrays. I would pick the
   second — it removes an unmaintained parser from the ingest path and leaves one
   JSON library in the build — but it contradicts the prompt's explicit
   instruction, so it is flagged rather than done.

2. **The §5.3 aircraft defaults constrain the poll interval, which the document
   does not state.** §5.3 pairs a 240s keyframe with a 5-minute TTL. Since §5.1
   requires `keyframe <= ttl - pollInterval - 30s`, those defaults are only valid
   when the poll interval is **30 seconds or less**; at 60s the ceiling is 210s and
   the documented default fails validation. This surfaced as six failures in my own
   test fixtures, which I had built with a 60-second interval. The Source Catalog's
   per-source columns should make the implied interval explicit for every TRACK
   source, or prompt 019's aircraft adapters will fail startup.

3. **`Mode.DERIVED` is not in the OpenAPI `SourceDescriptor.mode` enum.** The
   document lists only `polling`, `streaming`, `push`. The prompt and
   Weather-Imagery-Design §2.2 both require `DERIVED`, so the Java enum has it.
   Prompt 017's contract test will flag the gap; the spec needs the value added.

4. **`IngestPolicy` has no OpenAPI schema.** `source_settings.ingest_policy` is
   implemented and `SourceService.update` validates it, but neither
   `SourceSettings` nor `SourceSettingsUpdate` declares an `ingestPolicy` property
   in `docs/OpenAPI.json`. Prompt 010 cannot expose it over HTTP until the schema
   exists.

5. **`ParamType` has two values the OpenAPI enum does not.** `LAN_URL` and
   `INBOX_PATH` are required by Weather-Imagery-Design §10.1 but absent from the
   `ParamSpec.type` enum, which lists seven values. Either the spec gains them or
   `GET /v1/sources` must not surface them; prompt 017 will catch it.

6. **`InboxFiles.open` had a real bug that a probe caught.** The symlink guard
   tested `isSymbolicLink` on the path *after* resolving it to a realpath, which can
   never be a link, so the guard did nothing. It now tests the path as given and
   opens the realpath with `NOFOLLOW_LINKS`. Confinement was never broken — a
   symlink pointing outside the inbox was always rejected by the realpath check —
   but the "never follow a link" property was not actually enforced. Recording it
   because it is exactly the class of mistake that reads as correct.

7. **`SourceService.requireCredentials` decrypts on every enable.** It needs the
   stored field *names*, and the only way to get them is to decrypt. A credential
   that cannot be decrypted therefore makes enabling fail with a 500 rather than a
   409. That is the right outcome, but if prompt 010 wants a friendlier message it
   should catch `CredentialDecryptionException` at the controller.

8. **Nothing consumes `AdapterStateStore`, `SourceHttp` or `RecordSink` yet.** All
   three are declared here and implemented in prompts 005–008. `FetchContext` has
   no implementation either; prompt 008 builds it when the scheduler runs fetches.

9. **The prompt directory was renumbered mid-prompt.** Prompts 007–028 were deleted
   and replaced (Core-Data-Model's planner section inserts `006a`, renames 007 and
   inserts `007b`). Those changes were already staged in the index when this prompt
   started; I reset the index and committed only implementation, so the renumbering
   is still uncommitted for the planner to handle.

## Tests written (not run)

| Test class | Covers |
|---|---|
| `SourceDescriptorBuilderTest` | 12 cases: every documented default, name falling back to the id, empty collections, the ALL default policy, tier-dependent `ttlAnchor`, replaceable `searchFields`, stream poll interval, required-credential reporting, param lookup, immutability, and a descriptor with every field set |
| `GeoTest` | 19 cases: SRID 4326 and lon/lat order, range boundaries and 5 out-of-range rejections, NaN, GeoJSON point/line/polygon/from-map, 9 invalid-GeoJSON rejections including the empty-geometry case, snippet truncation and retention, null-island detection |
| `IngestPolicyValidatorTest` | 22 cases across 6 nested groups: keyframe `none`-only-without-TTL, the TTL ceiling at and over the boundary, the stream 2-second poll, a TTL too short for its interval, `minInterval` floor and keyframe bound, `edgeMinInterval` default/bound/sign, `significantFields` required by SAMPLED and LATEST but not ALL, the derived ALL keyframe, mode shapes, deviation sign, and multi-problem reporting |
| `SourceRegistryTest` | 36 cases across 4 nested groups: bean and family discovery, kinds, find/get/contains, 404 on unknown; every validation failure (4 id forms, length, duplicates, kinds, interval inversion, missing intervals, 3 budget forms, duplicate params, bad default, required-with-default, 2 mode mismatches, DERIVED with a TTL); TRACK without TTL / wrong anchor / with retention / with kindRetention; SAMPLED and LATEST without significant fields; threshold, motion and volatile conflicts; 4 nested-path forms; warnings for non-significant search and tile fields; dynamic register/unregister with events, duplicates, shadowing, same-validation, non-PUSH refusal, null, built-in protection |
| `ParamValidatorTest` | 48 cases across 6 nested groups: every `ParamType` including coercions and rejections; defaults, supplied-beats-default, required, blank-not-required, `allowed` for scalars and list entries, unknown keys, error aggregation, invalid descriptor defaults; `LAN_URL` accepting 7 private forms with the address pinned, rejecting 3 public forms, carrier-grade NAT, 4 non-URLs, bad schemes and unresolvable hosts; `INBOX_PATH` accepting relative and absolute paths inside, rejecting outside, traversal and symlink escape, accepting an inside symlink; accessor type-mismatch and fallbacks |
| `SourceServiceIT` | 44 cases across 7 nested groups: seeding defaults from the descriptor, nothing enabled, idempotence, orphan retention, family feeds; list and view shape, 404; partial-update semantics, empty update, interval floor, zero interval, budget bounds, unknown params, error aggregation, atomicity; retention store/clear/shorthand/non-positive and the TRACK `RETENTION_TIER_FIXED` rejection; ingest-policy default, round trip, jsonb discriminator, clear, invalid rejection; credentials — 409 naming the field, enabling after storing, encryption at rest, decryption only through the accessor, no value in any view, undeclared keys, missing required, blank values, source taking none, replacement, auto-disable on delete; events before/after, scheduling relevance, none on failure |
| `InboxFilesTest` | 11 cases: absolute normalized root, inside/outside/traversal, reading a regular file, refusing a symlink, the time-of-check/time-of-use case with a planted symlink, sorted listing skipping symlinks and directories, refusing to list outside, empty inbox |
| `LocalAddressesTest` | 7 parameterized cases covering 12 local forms, 6 public forms, 3 carrier-grade NAT forms and its 3 boundaries, IPv4-mapped IPv6, and null |
| `GlobalExceptionHandlerTest` (updated) | `InvalidInputException` → 400 passing its message; a bare `IllegalArgumentException` → 500 with a correlation id; a library `IllegalArgumentException` not leaking its message |
| `ElaroPropertiesTest` (updated) | 3 new cases for `inboxDir`: defaulting under the data dir, an explicit override, and following the default data dir |

Estimated coverage of this prompt's production code: high. Every validator,
codec, helper and service method is exercised directly. The uncovered remainder
is defensive: `SourceService`'s two fallback branches for stored parameters that
no longer validate, `InboxFiles`' `realPath` fallback for an absent file, and
`ValidatedParams`' type-mismatch guard (which is covered, but only as the bug it
represents).

**No tests were executed.** No `test`, `build`, `check` or `jacocoTestReport`
invocation was made, per guardrail 1.

As in 002 and 003, I compiled throwaway `main()` probes against `build/classes`
and ran them to check logic I could not otherwise verify before prompt 061 — one
per package, to respect package-private visibility. They asserted the complete
`IngestPolicyValidator` matrix, every descriptor default, the ~60 registry
validation outcomes with their exact messages, all 48 `ParamValidator` cases
including real DNS resolution and real symlink creation, the `Geo` behaviours,
`InboxFiles` confinement, and the `LocalAddresses` tables. **Two defects were
found this way and fixed**: the empty-geometry acceptance (deviation 3) and the
ineffective symlink guard (issue 6). A third finding was in my own fixtures, not
the code (issue 2). All probes ended green; the probe files were deleted. The
database-backed `SourceServiceIT` assertions remain unverified until prompt 061.

## Compile result

```
$ ./gradlew compileJava compileTestJava --no-daemon   # from an empty build/
BUILD SUCCESSFUL in 4s
```

No errors and no warnings. Two intermediate failures were fixed along the way:
the `FetchResult.notModified()` signature collision (deviation 2) and an illegal
multi-catch in `Geo` where `IllegalArgumentException` was listed alongside
`RuntimeException`.

Acceptance criteria confirmed: the SPI matches Architecture §4 as amended by
Core-Data-Model §3 (deviations above); no public method reachable from a
controller returns a credential value — `SourceService.credentials(String)` is
package-private and `CredentialsAccess` is the documented internal path; no
Flyway, Liquibase or `.sql` files.

## Notes for the next prompt

1. **Prompt 005 implements `SourceHttp`.** The interface is declared with the five
   methods the prompt listed. Two things it must honour that are already encoded in
   the SPI: a `LAN_URL` parameter's connection must go to
   `ValidatedParams.pinnedAddress(name)` with the original `Host` header and
   redirects disabled, and `FetchContext.http()` must throw
   `UnsupportedOperationException` for a `DERIVED` source. The per-descriptor
   `fetchTimeout` and `maxResponseBytes` are on the descriptor.
   Weather-Imagery-Design §2.2 also wants `getRange`, `download`, `listS3` and
   `mbPerDay` added there.

2. **Use `InstallIdService.userAgent()`** for every upstream request, set once in
   `UpstreamClient` rather than by adapters.

3. **`FetchResult.unchanged()`, not `notModified()`** — see deviation 2.
   `getConditional` should return it on a 304.

4. **The fixtures are ready to drive the collector.** `FakeAdapters` registers
   `fake-polling`, `fake-streaming`, `fake-push` and three `fake-family-*` feeds;
   `FakePollingAdapter` scripts a sequence of results and failures and counts
   fetches; `FakeStreamingAdapter` tracks handle closes and lets a test emit
   mid-stream. Import `FakeAdapters` rather than relying on component scanning —
   the fixtures live under `ai.elaro.testsupport`, and scanning them would add
   these sources to every integration test's registry.

5. **`SourceSettingsChangedEvent.affectsScheduling()`** already distinguishes a
   scheduling-relevant change from a budget edit, which prompt 008's reconciling
   scheduler can use directly.

6. **Prompt 010 will need to add three things to the update path**: the `embed`
   flag from the OpenAPI schema (deviation 8), mapping a JSON `null` to
   `clearRetention` / `clearIngestPolicy` (deviation 6), and an `ingestPolicy`
   property once the spec defines one (issue 4).

7. **Input parsers must throw `InvalidInputException` or `ValidationException`.** A
   bare `IllegalArgumentException` is now a 500 with a logged stack trace. This
   applies to the cursor codec in prompt 011 and the rule parser in prompt 014.

8. **`DescriptorValidator.warnings` is advisory only.** It currently logs at
   startup. If a later prompt wants these surfaced in `GET /v1/sources`, the method
   is already separate from `problems`.
