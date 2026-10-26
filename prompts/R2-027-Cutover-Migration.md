# R2-027 — Cutover: first-run bootstrap, global→shared migration, API 2.0.0 finalization

**Context.** v2.0 (R2) backend. **This is the LAST backend prompt and it depends on every prompt R2-001…R2-026
being implemented** — it flips the whole v2.0 contract live and regenerates the clients against the finished
surface. Run it only after all prior R2 prompts have landed.

**Goal.** Turn the built pieces into a usable, versioned platform: a first-run bootstrap that creates the first
admin from the break-glass admin token, a one-time migration of the existing global config into the shared/seed
substrate plus the first admin's personal config, the flip of the entire v2.0 operation surface from `planned`
to `implemented`, regenerated SDKs + Postman, and a finalized, honestly-documented API **2.0.0**.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-027`)

| operationId | method + path | scope |
|---|---|---|
| `getFirstRunStatus` | `GET /v1/system/first-run` | open |
| `runSetup` | `POST /v1/setup` | admin (break-glass bootstrap token) |

Schemas authored: `FirstRunStatus`, `SetupRequest`, `SetupResult`.

## Read first

- `security/AdminTokenService.java` (the rotatable bootstrap admin token — authorizes `runSetup` when no users
  exist), and the R2-001…R2-013 identity code: the `user`/`role`/`user_session` model, `/v1/auth/*`,
  `TokenFilter`/`AccessPolicy`, the SMTP settings (R2-007), and the per-user ownership added to trigger rules
  (R2-008, `owner_principal_id`), channels (R2-009, `owner_principal_id`), LLM providers (R2-011, `owner_user_id`)
  and saved views/workspaces (R2-012, `owner`), plus the audit log (R2-013).
- The owner-less global config to migrate: `source/SourceSettingsEntity`, `trigger/TriggerRuleEntity`,
  `llm/LlmProviderEntity`, `settings/ServerSettings` (none carry an owner today).
- `system/SystemInfoService.java` + `system/ApiVersion.java` and `src/test/java/ai/foresignal/system/ApiVersionTest.java`
  (the version pins), `docs/OpenAPI.json` (`info.version` already `2.0.0`), the frozen baseline
  `docs/api/v1.0.0/OpenAPI.json` + `docs/api/v1.0.0/oasdiff-err-ignore.txt` (the accepted-breaks file, each
  entry with its reason), `build.gradle.kts` `checkApiCompat`/`lintOpenApi`, `gradle/sdk.gradle.kts`
  (`generateSdks`), `scripts/generate-postman.py`.

## Build

1. **First-run status.** `getFirstRunStatus` (`FirstRunStatus`: `firstRun` = no users exist, `hasUsers`,
   `smtpConfigured`, `sourcesEnabled`, `apiVersion`). Stays `open` so the admin app can show setup before any
   account exists.

2. **Setup / bootstrap.** `runSetup` (`SetupRequest` → `SetupResult`), authorized **only** by the break-glass
   admin token and **only while `firstRun`** (once a user exists, it is `409 ALREADY_SETUP`). It creates the
   first admin (email + full name + argon2id password via the R2-001/R2-003 services), and optionally seeds SMTP
   (R2-007), the collection source set, and resource thresholds (R2-014). Returns the new `User` and, when
   issued, a `SessionToken` for immediate sign-in.

3. **Global → shared/admin migration.** A one-time migrator (a Flyway data migration and/or a first-run service
   step) that makes existing owner-less config v2-correct: source settings, collector settings, shared LLM,
   global channels and server settings stay **shared/global** (null owner); the first admin is attached as owner
   where a row is inherently personal (e.g. the pre-existing trigger rules / saved views / personal LLM keys
   become that admin's). Idempotent: safe to run once on an upgraded instance, a no-op on a fresh one. Scoped
   machine tokens keep working unchanged.

4. **Flip the whole v2 surface.** In `docs/OpenAPI.json`, set `x-foresignal-status: implemented` on **every**
   operation whose `x-foresignal-prompt` is an `R2-*` (the full v2 surface — identity/RBAC through this prompt),
   now that the code behind each is built. Do not touch the frozen `docs/api/v1.0.0/OpenAPI.json`.

5. **Finalize 2.0.0.** Confirm `info.version` = `2.0.0`; update the `ApiVersionTest` pins to `2.0.0`; keep the
   frozen v1.0.0 baseline. The per-user semantics of the pre-existing config endpoints are a deliberate,
   documented major break — **record each intentional v1→v2 break in `docs/api/v1.0.0/oasdiff-err-ignore.txt`
   with its reason** so the compatibility gate passes while documenting the breaks honestly (do not edit the
   frozen spec itself). Update the changelog / `docs/Architecture.md` version note accordingly.

6. **Regenerate clients.** Run `python3 scripts/generate-postman.py` (Postman) and `./gradlew generateSdks`
   (the Dart / TypeScript / Python clients, now that the whole surface is `implemented`). Commit the
   regenerated artifacts. If a client toolchain (openapi-generator, dart-dio, Node) is unavailable, generate
   what you can, mark the rest clearly, and record the prompt `Partial` per `000-Run-Sequence.md` "Missing
   access / External dependency unavailable" rather than failing the run.

**Migration.** Add the next free additive Flyway migration `V{n}__v2_cutover.sql` (`n` one past the highest
`V*` now present — read the directory) for the idempotent global→shared data migration. Additive only; never
drop or rewrite existing rows destructively.

## Contract

This prompt is where the v2 surface goes `planned`→`implemented`. `docs/OpenAPI.json` is the only contract;
the frozen `docs/api/v1.0.0/OpenAPI.json` and the `x-foresignal` changelog history are never hand-rewritten
(the err-ignore file is the sanctioned place for the accepted v2 breaks).

## Tests to write (do not run)

- `FirstRunStatusIT` — `firstRun` true with no users, false after the first admin; fields reflect SMTP/source state.
- `RunSetupIT` — the bootstrap token creates the first admin + optional SMTP/sources/thresholds and returns a
  session; a second call → `409 ALREADY_SETUP`; a non-bootstrap caller is rejected.
- `GlobalToSharedMigrationIT` — existing global config survives; shared stays shared; inherently-personal rows
  attach to the first admin; running the migrator twice is a no-op; scoped tokens still authenticate.
- `ApiVersionTest` — pins `2.0.0`.

## What runs

Compile — `./gradlew compileJava compileTestJava`. Write the tests above; **do not run the JUnit suite** (the
owner runs all tests at code-complete). This cutover prompt additionally runs the **contract and client
tooling**: `./gradlew checkUsEnglish`, `python3 scripts/generate-postman.py`, `./gradlew generateSdks`, and the
full `./gradlew checkApiCompat` + `./gradlew lintOpenApi` (each self-skips when its tool — oasdiff/docker/npx/
openapi-generator — is absent; record `Partial` if so). `checkApiCompat` must end green with the intentional
v2 breaks accounted for in the err-ignore file. US English everywhere.

## Done when

A fresh instance reports `firstRun`, accepts `POST /v1/setup` with the bootstrap token to create the first
admin (and optionally SMTP/sources/thresholds), and an upgraded instance's global config is migrated idempotently
to shared + first-admin; every `R2-*` operation in `docs/OpenAPI.json` is `implemented`; API is finalized at
`2.0.0` with the v2 breaks documented in the err-ignore file; Postman and the SDKs are regenerated; compile
passes; `checkUsEnglish` green and `checkApiCompat` green (or `Partial` with a missing tool noted).

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json` (only its `oasdiff-err-ignore.txt`);
the `x-foresignal` changelog history; `.gitlab-ci.yml`. **Stage only the files this prompt changes** (including
the regenerated Postman/SDK artifacts).

## Commit

`R2-027: v2.0 cutover — first-run bootstrap, global→shared migration, API 2.0.0`, then
`completion-reports/R2-027-CR-Cutover-Migration.md` with the 40-character implementation hash.
