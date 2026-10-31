# SG-004 — Activate API 2.2.0 (the mini-cutover)

**Context.** `SG-001`…`SG-003` implemented the fifteen 2.2.0 operations against the `planned` contract; the
code compiles and the tests are written. This prompt is the **cutover** (as `NP-007` / `R2-027` were for their
releases): flip every 2.2.0 operation to `implemented`, move the release to **2.2.0** in the guard spots, add
the 2.2.x activation guard, and regenerate the SDKs and Postman. After this prompt the version-agreement guard
goes green.

**Goal.** The API advertises 2.2.0 as a released, implemented surface; nothing is left `planned`; the
generated clients and Postman carry the new operations; the release version agrees everywhere.

## Read first

- `src/test/java/ai/foresignal/system/ApiVersionTest.java` — `RELEASE` (`"2.1.1"`, line 33); the version-
  agreement tests (`info.version`, `/v1/system` apiVersion, Gradle `version`, newest changelog entry all equal
  `RELEASE`); and `newspaperOperationsAreActive()` (lines 76-110) — the **model** for the new SG guard (it
  matches ops by `x-foresignal-prompt` starting `"NP-"`, concatenates every `2.1.` changelog note, and asserts
  none `planned` and each named).
- `build.gradle.kts:13` (`version = "2.1.1"`); `build.gradle.kts:408` (`generatePostman` →
  `python3 scripts/generate-postman.py docs/OpenAPI.json postman`); `build.gradle.kts:421`/`:452`
  (`lintOpenApi`, `checkApiCompat` against the frozen v1.0.0); `gradle/sdk.gradle.kts:194`/`:213`
  (`generateSdks`, `checkSdkGeneration`); `scripts/generate-postman.py` (its `BODIES` map).
- `docs/OpenAPI.json`: `info.version` is already `2.2.0`; the newest changelog entry is `2.2.0` (line ~736)
  and names all three paths (`/v1/me/custom-sources`, `/v1/me/source-groups`, `/v1/me/source-thresholds`); the
  fifteen SG ops are `x-foresignal-status: planned`, tagged `x-foresignal-prompt: SG-001`…`SG-003`.

## Build

1. **Activate the operations.** In `docs/OpenAPI.json`, set `x-foresignal-status` from `planned` to
   `implemented` on all fifteen SG operations (the eight `/v1/me/custom-sources*`, the five
   `/v1/me/source-groups*`, the two `/v1/me/source-thresholds`). Change nothing else in the spec (the schemas,
   paths, permissions and changelog are already authored).
2. **Move the release to 2.2.0 (three code spots; the spec is already there).**
   - `ApiVersionTest.RELEASE` → `"2.2.0"`.
   - `build.gradle.kts` `version = "2.2.0"`.
   - `/v1/system` apiVersion reads the classpath copy of `docs/OpenAPI.json` via `ApiVersion`, so it follows
     `info.version` automatically — confirm, don't hand-edit.
   The changelog's newest entry is already `2.2.0`, so `changelogEndsWithTheRelease` is satisfied.
3. **Add the 2.2.x activation guard.** Add a `sourcesOperationsAreActive()` test to `ApiVersionTest`, modeled
   on `newspaperOperationsAreActive()` but for the sources release: match operations whose `x-foresignal-prompt`
   starts `"SG-"`, concatenate every changelog note whose version starts `"2.2."`, and assert (a) at least
   fifteen such ops, (b) none is `planned`, and (c) each op's **path** appears in the concatenated note (the
   2.2.0 note documents the three `/v1/me/...` paths, not individual operationIds — so check the path, not the
   operationId). Leave `newspaperOperationsAreActive()` unchanged.
4. **Regenerate the clients and Postman.** Add `BODIES` entries for the fifteen new operations to
   `scripts/generate-postman.py` (as `017`/`NP-*` did), then run `./gradlew generatePostman` and
   `./gradlew generateSdks`, bumping the SDK package versions to **2.2.0** (the Dart/TypeScript/Python clients
   under `sdk/`). `AccessPermissions.java` was already regenerated in SG-001…003 (the generator has no status
   filter), so no permission change is needed here — but re-run `scripts/generate-access-permissions.py` and
   confirm no diff.

## What runs (the cutover gates — more than the per-prompt compile)

- `./gradlew compileJava compileTestJava` and `./gradlew checkUsEnglish` (as every prompt).
- `./gradlew lintOpenApi` and `./gradlew checkApiCompat` — the contract gates deferred through the campaign;
  2.2.0 is purely additive, so `checkApiCompat` must report no breaking change against the frozen v1.0.0.
- `./gradlew generatePostman` and `./gradlew generateSdks` (and `checkSdkGeneration`) — generation, run here.
- **Still do not run the full JUnit suite** — the campaign is compile-only (the NP campaign ended the same
  way). The written tests and the version-agreement/activation guards are run by the owner's separate full
  test pass after the campaign.

## Done when

All fifteen SG ops are `implemented`; `RELEASE` and the Gradle `version` are `2.2.0` (agreeing with
`info.version`, `/v1/system`, and the newest changelog entry); `sourcesOperationsAreActive()` exists and
(when run) passes; SDKs and Postman carry the new operations at version 2.2.0; `compileJava`/`compileTestJava`,
`checkUsEnglish`, `lintOpenApi`, `checkApiCompat` and SDK generation are green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; `.gitlab-ci.yml`; the SG-001…003
feature code (this prompt only activates and regenerates). **Stage only this prompt's files.**

## Commit

`SG-004: Activate API 2.2.0`, then
`completion-reports/SG-004-CR-Activate-2-2-0.md` with the 40-character commit hash, confirmation that the
release agrees in all spots, the `checkApiCompat`/`lintOpenApi`/SDK-generation results, and a note that the
Sources & Groups feature is at API 2.2.0 — plus any owner question (e.g. an SDK generation that needs a tool
the box lacks, logged rather than treated as a stop).
