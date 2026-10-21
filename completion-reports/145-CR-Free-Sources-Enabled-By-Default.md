# 145-CR-Free-Sources-Enabled-By-Default

| | |
|---|---|
| Prompt | 145-Free-Sources-Enabled-By-Default.md |
| Status | Partial (implemented and compiles; **no test, guard, lint or compatibility gate was run through Gradle** — see "What ran") |
| Implementation commit | `0ab64b21fc13e540d8740b902824d769ba3750bc` |
| Date | 2026-10-21 |

## What ran

The prompt asks for the touched tests, the US-English guard, `lintOpenApi` and `checkApiCompat`.
`prompts/000-Run-Sequence.md` says no test runs until the owner declares the project code-complete,
that this overrides any prompt file, and that the only Gradle allowed is `compileJava compileTestJava`.
The run sequence was followed. What ran:

| Check | Result |
|---|---|
| `./gradlew compileJava compileTestJava` on the committed tree | exit 0 |
| `check_us_english.py` (report only) on every touched file, HEAD vs. now | no new British spelling of this prompt's own; `SourceServiceIT` 9 → 11, both from new references to the existing constant `ParamAdapterConfig.PARAMETERISED_ID` (not renamed: outside this prompt); `Operator-Guide.md` 81 → 80; all others equal |
| `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py` (run by the docs sub-agent) | `Ran 14 tests in 170.262s — OK`. It sweeps the meshdtn repositories; whether it covers this one was not checked |
| `python3 scripts/generate-postman.py docs/OpenAPI.json postman` | 845 requests, 80 folders, every operation covered |
| Touched ITs, `lintOpenApi`, `checkApiCompat` | **not run** |

## What changed

| Area | Change |
|---|---|
| Seeding | `SourceService.seedDefaults` (`SourceService.java:139`) enables a new non-dynamic row (`:152`) when `runsWithoutOperatorInput` (`:188`) holds: no required credential, no required param, **and** `ParamValidator.validate(descriptor, Map.of())` accepts it. Gated sources seed disabled. Existing rows are never touched. |
| Endpoint | `POST /v1/sources/enable-free` (`EnableFreeSourcesController`, operation `enableFreeSources`), admin (`AccessPolicy.java:362`). `SourceService.enableFree` (`:703`) calls `update(id, enabled(true))` per source, so the enable guard runs and `SourceSettingsChangedEvent` fires. Answers `{enabled:[ids sorted], count, skipped}`. |
| Spec | `docs/OpenAPI.json`: path, `EnableFreeSourcesResult` schema, changelog entry at index 0. Postman regenerated. |
| Docs | `Source-Onboarding.md` "The guarantee" rewritten, "Immediate wins" replaced; `Operator-Guide.md` and `Source-Catalog.md`: every "starts disabled" statement checked against the code and split into free (enabled) and gated (disabled), with cites. |
| Tests (written, not run) | `SourceServiceIT`: free enabled, credentialed (AIS) disabled, required-param (`aprs-is`, a fake) disabled, `usgs-water` disabled, operator disable survives re-seeding, seeded-enabled rows pass the guard, `EnableFree` nested (only free, skipped count, enabled rows untouched, idempotent, event published, can still disable). `SourcesControllerIT`: endpoint 200 shape, idempotent, 401 / 403 (read token). `AccessPolicyTest.enableFreeIsAdmin`. `HealthServiceIT` disables all rows after seeding so its expectations stand. |

## Deviations

1. **The predicate is stricter than the prompt's.** The prompt says "no required credential and no required param" equals what the enable guard accepts. It does not: `local-acars-*` (`LocalAcarsAdapter.java:112`), `usgs-water` (`UsgsWaterAdapter.java:172`) and `snotel` (`SnotelAdapter.java:133`) declare nothing required but refuse an empty config. Seeding them enabled would leave rows the API itself refuses. The predicate therefore also runs the validator.
2. **`defaultRow` still builds a disabled row** (`SourceService.java:205`); the enable decision is made in `seedDefaults`. `update()` and the dynamic-source paths also call `defaultRow`, so enabling there would let an interval-only update silently turn on an unseeded source.
3. **`info.version` stays 1.0.0.** A new operation is a minor bump per `docs/Conventions.md`, but `ApiVersionTest` pins 1.0.0 in the spec, `/v1/system` and Gradle. The changelog entry is labeled 1.0.0. Owner: bump all three, or keep?
4. **`skipped`** counts disabled built-in sources left disabled (gated, or refused by the guard). Dynamic sources and already-enabled rows are in neither count.

## Owner decisions

- **Sources once disabled for terms, license or volume now seed enabled**, because the code has no such gate: `ua-air-raid-siren`, `gpsjam-daily`, `meshtastic-mqtt`, `openmhz-systems` (Cloudflare-blocked, will show failing), `wiki-edit-surge` (~3 GB/day), the non-commercial or unclear-terms feeds (`deepstate-frontline`, `meshtastic-map`, `kiwisdr`, `adsbfi-mil`, `airplaneslive-mil`, `submarine-cables`, `gvp-volcanoes`, free CCTV packs), `media-ratings`, and the raster sources (`mrms`, `goes-east-abi`, `gfs`, `hrrr`, `goes-glm`, `nexrad-l3`). The docs now say they start enabled. Keep, or add an opt-in flag to descriptors?
- **`bluesky-search` seeds enabled** (optional credentials only) while its social platform stays off (`SocialPlatformService.java:40-41, 127`), which contradicts `Operator-Guide.md` ("nothing is collected from a platform until you enable it, except Bluesky's Jetstream"). That line was left as is: code or doc?
- **Free sources that run but produce nothing useful by default:** `sondehub` fails at fetch without a center or place (`SondehubAdapter.java:180`); `gdelt-articles`, `gdelt-timeline` and the Wikimedia topic lists default to empty.
- **`enable-free` cannot tell a deliberately disabled free source from one never enabled**; it enables both. Documented in the Operator Guide.
- **Unrun tests most at risk:** `CollectorSchedulerIT` (runs with `scheduled=true`, so startup seeding now enables real free sources that may reach real upstreams); the social ITs (`bluesky-jetstream` now seeds enabled); the tests that call `seedDefaults` (`grep -rln seedDefaults src/test`: `SourceServiceIT`, `HealthServiceIT` — both adjusted — and `SocialObservabilityTest`, `SocialControllersIT`, `SocialDeletionIT`, not changed: they now start with every free source enabled, including `bluesky-jetstream`, and were only read, not run).
- `prompts/145-Free-Sources-Enabled-By-Default.md` is untracked and was not committed. `completion-reports/RUN-STATUS.md` (Finished, 2026-10-18) was left untouched.
