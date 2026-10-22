# 146-CR-Hold-Heavy-Free-Sources-And-Api-1-1-0

| | |
|---|---|
| Prompt | 146-Hold-Heavy-Free-Sources-And-Api-1-1-0.md |
| Status | Partial (implemented and compiles; **no test, `lintOpenApi` or `checkApiCompat` was run**, per the prompt and `prompts/000-Run-Sequence.md`) |
| Implementation commit | `426d8d7a51352fd66da6550ea50af2274e503827` |
| Date | 2026-10-21 |

## What ran

| Check | Result |
|---|---|
| `./gradlew compileJava compileTestJava` | exit 0 |
| `./gradlew generatePostman` (a generation step, allowed by the prompt) | "845 requests across 80 tag folders; quickstart 12 steps; covers all ops: True" |
| `check_us_english.py` on every touched file, HEAD vs. now | no new British spelling anywhere: every count equal; the new `AutoEnableDefaultsTest.java` has 0 |
| Tests, `lintOpenApi`, `checkApiCompat` | **not run** |

## What changed

| Change | Where |
|---|---|
| Opt-in flag | `SourceDescriptor.autoEnable` (record component and builder, default true, `SourceDescriptor.java:249`, `:480`); javadoc says a free, runnable source may be held off for bandwidth or terms. Internal: `SourceMapper` builds the API DTO, so the wire is unchanged. |
| `wiki-edit-surge` | `.autoEnable(false)` with the reason (`WikiStreamAdapter.java:247`); its byte budget is `Budget.of(2, 500, 3000)`, 3,000 MB a day. |
| One predicate | `SourceService.startsEnabled` (`:191`) = `autoEnable && runsWithoutOperatorInput`, used by `seedDefaults` (`:161`) and `enableFree` (`:723`), so `wiki-edit-surge` seeds disabled and is counted in `skipped`. The operator enables it with `PUT /v1/sources/wiki-edit-surge`. |
| API 1.1.0 | `docs/OpenAPI.json` `info.version`, `build.gradle.kts` `version`, `ApiVersionTest.RELEASE`. The set was found with a grep for `1.0.0` across the repo; the frozen `docs/api/v1.0.0/OpenAPI.json` and its test stay 1.0.0. Changelog: 145's entry is relabeled 1.1.0 (it added the operation) and a 146 entry is added. |
| Docs | Operator Guide: "Sources" names the exception and the new setting; the Social signals paragraph and platform table now say `bluesky-search`'s source is free and on, while its platform row reads disabled; the `wiki-edit-surge` bullet says opt-in. Source Catalog: the social intro, the `bluesky-search` Default cell and the `wiki-edit-surge` row. Source Onboarding: the guarantee, the immediate-wins list and the code citations, re-pointed to the edited lines. |
| Tests (written, not run) | `SourceServiceIT`: `heldOffSourceSeedsDisabled`, `heldOffSourceCanBeEnabled`, wiki added to `builtInSourcesFollowTheRule` and `enablesOnlyFreeSources`, `countsSkipped` uses `startsEnabled`. `SourcesControllerIT`: `skipsHeldOffSource`. `SourceDescriptorBuilderTest.autoEnableDefaultsToTrue`. New unit test `AutoEnableDefaultsTest` (`wiki-edit-surge` free yet held off; `usgs-earthquakes` free and auto-enabled). `CollectorSchedulerIT.seedingKeepsRealSourcesOffline`. `RetentionSettingsValidationTest` passes the new constructor argument. |

## Deviations

1. **A new setting, `foresignal.sources.default-enable`** (true in `application.yml`, false in `application-test.yml`). This is what keeps `CollectorSchedulerIT` offline. Every test context shares one database, and `CollectorScheduler` reconciles from it straight away on start and every 2 s (`CollectorScheduler.java:161-162`). Spring's test context cache pauses and restarts cached contexts, which restarts the scheduler (`isAutoStartup` javadoc, `:216-224`). With free sources seeded enabled, that reconcile would fetch real upstreams. The setting copies the existing `foresignal.social.default-enable`, which is also off in tests.
   - `SourceServiceIT` turns it on to test the default.
   - `SourceServiceIT` and `SourcesControllerIT`'s enable-free tests disable every row after each test, so no real free source is left enabled for a restarting scheduling context.
   - `enableFree` ignores the setting: it is an explicit operator call.
2. **The social seeding tests were not changed.** With the test profile seeding disabled, `bluesky-search` starts disabled there as it did before 145. A grep found no assertion about `bluesky-search` in `SocialObservabilityTest`, `SocialControllersIT` or `SocialDeletionIT`.
3. **`defaultRow` still builds a disabled row** (`:224`). The prompt says `defaultRow` seeds enabled when both conditions hold. As in 145, the decision is in `seedDefaults`, because `update()` also builds a `defaultRow`, and an interval-only update must not switch on an unseeded source.

## For the owner

- **The keyed-sources answer from the Shadowbroker question is not in 146.** That answer was "seed keyed sources enabled, drop the `CREDENTIALS_REQUIRED` refusal, let the collector skip them until a key is stored". 146 keeps 145's rule (keyed seed disabled). Should a prompt carry it?
- **`bluesky-search`'s platform row and its source disagree.** The platform reads `disabled` (its catalog `defaultEnabled` is false) while the source runs. The adapter does not check the platform row (`BlueskySearchAdapter.java:190` reads the settings only for deletion sync). The docs now say this. Two code comments are stale: the adapter's javadoc (`:46`) and description (`:334`) say "Off by default", and `SocialPlatformService.java:41` says Jetstream is the only adapter on by default. Fix the code or the comments?
- **Descriptor descriptions still say "disabled by default"** in 39 adapter files under `source/adapters` (`grep -rlc "Disabled by default\|disabled by default" src/main/java/ai/foresignal/source/adapters`). They reach the API as `description`. Left alone: outside this prompt's three docs.
- **The TypeScript SDK** (`sdk/typescript/src/generated`) was not regenerated in 145 or 146.
- `prompts/145-*.md` and `prompts/146-*.md` are untracked and were not committed.
