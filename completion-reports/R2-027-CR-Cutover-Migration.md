# R2-027-CR-Cutover-Migration

| | |
|---|---|
| Prompt | R2-027-Cutover-Migration.md |
| Status | Complete (compile only; no JUnit test was run. `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` and `generateSdks` were run, as this prompt and R2-000's Tier 2 line direct.) |
| Implementation commit | `662c74473a68a4e534338c6a360131f1c4884ec1` |
| Date | 2026-10-26 |

## What was added

- **`getFirstRunStatus`** (`GET /v1/system/first-run`, open), in `api/SetupController` with `setup/FirstRunService`. It
  reports:
  - `firstRun`, which is true while no user exists, and `hasUsers`;
  - `smtpConfigured`: account email is enabled and has a host;
  - `sourcesEnabled`: `source_settings` rows with `enabled` set;
  - `apiVersion`: the contract's `info.version`.
- **`runSetup`** (`POST /v1/setup`), in `setup/SetupService`:
  - **Who may call:** only the bootstrap token (`AdminTokenService.bootstrapTokenId()`). Any other admin token, a
    session or a ticket gets `403 FORBIDDEN`; no token gets 401.
  - **First admin:** created by the new `UserAdminService.createFirstAdmin`:
    - It holds the user-admin advisory lock and refuses once any user exists (`409 ALREADY_SETUP`, a new code).
    - The user is active at once, with the built-in Admin role.
    - The password goes through the R2-002 policy and hashing (`UserLifecycleService.setPassword` / `activate`).
    - The step is audited as `first_admin_created`.
  - **Then, in order:**
    1. The cutover move, described below.
    2. The optional seeds: `smtp` through `SmtpSettingsService.update`, `sources` enabled through
       `SourceService.update`, `thresholds` through `ResourceThresholdsService`, and `retention` through
       `RetentionSettingsService.update`.
    3. A sign-in through `SessionService.login`, returned as a `SessionToken`. `AuthController.sessionToken` was
       extracted so both paths build it the same way.
  - **All of it is one transaction:** an unknown member or source id, a weak password, or a bad SMTP, threshold or
    retention body leaves the instance unchanged and still in first run.
- **`setup/CutoverMigrator` and `V26__v2_cutover.sql`:**
  - The migration adds a single-row `v2_cutover` record table: who, from which principal, when, and counts.
  - `migrate(adminUserId)` moves the rows owned by the instance `owner` principal (the bootstrap token's principal,
    meaning the v1 operator) to the first admin's principal, under an advisory lock. The rows are in `trigger_rule`
    (and `alert`), `notification_channel`, `llm_provider`, `saved_view`, and `workspace` with its `shareable_object`
    record.
  - It publishes `TriggerRulesChangedEvent` after commit.
  - It runs once: a second call returns the recorded result.
  - At `ApplicationReadyEvent`, an instance that has an active Admin but no record is migrated to the earliest such
    Admin. `foresignal.cutover.auto-migrate` (default true) controls this; the test profile sets it false.
- **API 2.0.0 is final:**
  - `info.version` was already 2.0.0.
  - These moved from 1.5.0 to 2.0.0:
    - `build.gradle.kts` `version`;
    - the `ApiVersionTest.RELEASE` pin;
    - the SDK manifests: `pubspec.yaml` ×2, `package.json`, the root of `package-lock.json`, `pyproject.toml` and
      `foresignal_sdk.__version__`;
    - the five SDK READMEs.
  - The frozen `docs/api/v1.0.0/OpenAPI.json` is untouched.
- **Tests (written, not run):**
  - `FirstRunStatusIT`: first run until a user exists; `smtpConfigured`; `sourcesEnabled` against the table.
  - `RunSetupIT`:
    - the bootstrap token creates the Admin plus SMTP, sources and thresholds, and the returned session works;
    - a second call is 409 `ALREADY_SETUP`;
    - another admin token is 403, and no token is 401;
    - five bad bodies change nothing.
  - `GlobalToSharedMigrationIT`:
    - a shared rule, a shared view and a global channel stay null-owner;
    - the v1 operator's personal channel and workspace move to the admin;
    - a rerun is a no-op, with one record;
    - a READ machine token still works;
    - a fresh instance records zero moves;
    - the startup run picks the earliest Admin.
  - `ApiVersionTest` pins 2.0.0.
- **Docs:** `docs/Operator-Guide.md` gained a "First-run setup" paragraph under the admin-token section.

## Contract

- **Status:** every operation whose `x-foresignal-prompt` is `R2-*` (72) is now `x-foresignal-status: implemented`.
  - I checked that all 72 operationIds have an `@Operation` mapping in `src/main/java`: none was missing.
  - No operation in the contract is `planned` any more.
- **`getFirstRunStatus` and `runSetup`:** the descriptions say what the code does. `runSetup`'s 400, 403 and 409
  descriptions name their codes.
- **Schemas:** `SetupRequest` and `SetupRequest.admin` are `additionalProperties: false`. The `sources` and
  `SetupResult.session` descriptions are sharper.
- **`docs/api/v1.0.0/oasdiff-err-ignore.txt`:** a new section, "BREAKING, accepted for API 2.0.0".
  - It holds the six errors oasdiff reports, each with its reason: `AuditEvent.actorKind` + `user`,
    `ArchiveSettings.target` + `glacier` (×2) and `ApiToken.createdVia` + `self` (×3).
  - It also lists, as comments, the semantic v1→v2 breaks no schema shows: ownership, read narrowing, permission, MFA
    and token gates, and quotas.
- **Regenerated:** Postman (936 requests) and the Dart, TypeScript and Python SDKs (`generateSdks`).

## Decisions and disagreements for the owner

1. **The prompt says the pre-existing trigger rules, saved views and personal LLM keys become the first admin's. I did
   not move the owner-less ones.**
   - In this code a null owner already means shared (V16, V18, V19).
   - An owned rule's alerts go only to its owner's channels, plus global channels whose `ruleIds` name it
     (`NotificationService.publishOwned`). Moving every v1 rule to the admin would silently stop alerts reaching the
     global channels they feed today.
   - A v1 instance has no "personal LLM keys"; all providers are shared.
   - What *is* personal in v1 is what the operator owned through the `owner` principal: their personal channels from
     prompt 130 and their workspaces. That is what moves.
   - Say if you want the rules and views moved anyway.
2. **The data move runs in Java, not in the SQL migration.** The first admin does not exist when Flyway runs, so
   `V26` only creates the record table. The move runs at setup, or at startup for an instance whose admin already
   exists.
3. **The guards were run despite R2-000 step 4.**
   - Step 4 says `checkApiCompat` and `lintOpenApi` run at owner code-complete. This prompt and R2-000's Tier 2 line
     say R2-027 runs them.
   - I ran them. Neither is a test suite. No JUnit test was run.
   - All the tools were already local: the oasdiff image `tufin/oasdiff:v1.33.0` was already pulled, and Java, npx,
     dart and uvx were on the PATH. Nothing was installed system-wide; npm, pub and uv used their user caches, and the
     openapi-generator jar went to `build/`.
4. **The enum additions are in the err-ignore file, not declared `x-extensible-enum`.**
   - The file's header says lists that grow with features are declared `x-extensible-enum`. These three
     (`actorKind`, `ArchiveSettings.target`, `createdVia`) arguably qualify.
   - This prompt says to record the breaks in the err-ignore file, and doing so keeps the SDKs' typed enums. Say if
     you prefer the extensible form; it would turn those SDK fields into plain strings.
5. **The changelog was not edited.**
   - The existing 2.0.0 entry in `info.x-foresignal-changelog` still says every new operation is `planned`, which is
     now stale.
   - The run's rules say the changelog is never hand-edited, so I left it. Say whether you want a 2.0.0 note added.
6. **`docs/Architecture.md` has no version note to update.** I searched for one and there is none, so it was not
   touched.
7. **The bootstrap token stays as it is.** It keeps working after setup as the break-glass credential, and its
   principal is not the admin's, so R2-025's ownership clamp never applies to it.
8. **The first admin's session is gated to MFA enrollment** (R2-024 enforcement) until they enroll. `runSetup` does
   not enroll them.
9. **A source that needs credentials makes setup fail with `409 CREDENTIALS_REQUIRED`,** and everything rolls back.
   The 409 description names it.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- `./gradlew lintOpenApi`: exit 0. Redocly said "Woohoo! Your API description is valid." with 70 warnings and no
  errors. Example: `no-unused-components` on `securitySchemes/feedKey`.
- `./gradlew checkApiCompat`:
  - Before the err-ignore entries it failed with 6 errors, all `response-property-enum-value-added`.
  - After them it printed "checkApiCompat: no breaking changes against docs/api/v1.0.0/OpenAPI.json" and exited 0.
- `./gradlew generateSdks`: exit 0 ("BUILD SUCCESSFUL in 1m 53s"). It ran `sdkGenerateDart`, `sdkBuildRunnerDart`,
  `sdkGeneratePython` and `sdkGenerateTypescript`.
- No JUnit test was run.

## Notes for the next prompt

- This is the last R2 prompt. The next Flyway migration is **V27**.
