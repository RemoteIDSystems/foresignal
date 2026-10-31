# SG-001 — Custom sources (self-service)

**Context.** API **2.2.0**, authored contract-first in `docs/OpenAPI.json` (already at `info.version` 2.2.0
with the 2.2.0 changelog entry; these eight operations are present as `x-foresignal-status: planned`, tagged
`x-foresignal-prompt: SG-001`, `x-foresignal-permission: self.sources`). This prompt **implements** that
authored delta — it does not design it. Operations stay `planned`; `SG-004` activates them.

Builds on the existing **operator** custom-source stack in package `ai.foresignal.inbound` (prompt 024,
extended by 039/054/059/083) and the self-service pattern NP-008 established for `/v1/me/editions`. A custom
source is already a first-class, selectable source in the registry; this prompt lets a **signed-in user own
and manage their own** custom sources without operator access or agent scope.

**Goal.** A user holding `self.sources` defines, lists, reads, updates, deletes and re-keys their own custom
sources through `/v1/me/custom-sources`, tests a mapping against their captured samples, and reads those
samples — each operation scoped to sources they own, returning 404 for anyone else's. The operator surface
(`/v1/custom-sources`) is unchanged.

## Operations (authored `planned` — implement; `SG-004` activates)

| operationId | method + path | gating |
|---|---|---|
| `listMyCustomSources` | `GET /v1/me/custom-sources` | `self.sources` |
| `createMyCustomSource` | `POST /v1/me/custom-sources` | `self.sources` |
| `getMyCustomSource` | `GET /v1/me/custom-sources/{sourceId}` | `self.sources` |
| `updateMyCustomSource` | `PUT /v1/me/custom-sources/{sourceId}` | `self.sources` |
| `deleteMyCustomSource` | `DELETE /v1/me/custom-sources/{sourceId}` | `self.sources` |
| `rotateMyCustomSourceIngestKey` | `POST /v1/me/custom-sources/{sourceId}/ingest-key/rotate` | `self.sources` |
| `testMyCustomSourceMapping` | `POST /v1/me/custom-sources/{sourceId}/mapping/test` | `self.sources` |
| `listMyCustomSourceSamples` | `GET /v1/me/custom-sources/{sourceId}/samples` | `self.sources` |

Request/response bodies reuse the existing schemas: `CustomSourceInput` / `CustomSource` (create/update/get/
list — list is a page), `MappingTestRequest` / `MappingTestResult` (mapping test), and the sample page shape
the operator `listCustomSourceSamples` already returns. The created-source response carries the one-time
plaintext ingest key exactly as the operator `createCustomSource` does; thereafter only `ingestKeyPrefix` is
shown, never the key.

## Read first (the only source of truth is the contract + the code)

- The authored 2.2.0 delta in `docs/OpenAPI.json`: the eight `/v1/me/custom-sources*` operations and the
  `CustomSourceInput`/`CustomSource`/`MappingTestRequest`/`MappingTestResult` schemas they reference.
- `inbound/CustomSourceEntity.java` (`@Table("custom_source")`, `@Id` `source_id`; `origin` is only
  `custom`/`external`, **no owner column today**), `inbound/CustomSourceRepository.java`,
  `inbound/CustomSourceService.java` (`create`/`update`/`delete`/`get`/`list`/`rotateKey`; head-in-use and
  `observedAtMode`-lock 409s; registers the descriptor in `SourceRegistry` and creates dynamic settings),
  `inbound/IngestKeys.java` (`generate`/`hash`/`prefixOf`/`matches`), `inbound/CustomSourcesController.java`
  (the operator twin at `/v1/custom-sources`; copy the shape of its `view(row)` and its create/update/delete/
  rotate, scoped to the session user).
- `inbound/CustomSourceDescriptors.java`, `source/SourceRegistry.java` (`registerDynamic`/`redefineDynamic`/
  `unregisterDynamic`/`contains`), so you keep the registry in step when an owned source is created/updated/
  deleted.
- `mapping/MappingsController.java` (admin `previewMapping`, `testCustomSourceMapping`,
  `listCustomSourceSamples`), `mapping/MappingPreviewService.java`, `mapping/SampleCapture.java` — the backing
  services the self-service `.../mapping/test` and `.../samples` delegate to.
- `api/EditionsController.java` (the self-service template): `requireSession(request)` → 401 on an API token;
  `AuthenticatedSession.of(request).userId()`; owner forced to the caller; an un-owned or bad-id resource is
  **404 identical to missing** (`BriefingService.visibleTo`/`requireVisible`, `EditionViewer`).
- `security/AccessPolicy.java` `rules()` — the contiguous `/v1/me/*` block (each rule
  `AccessRequirement.READ_ALWAYS_AUTHENTICATED`); `security/AccessPermissions.java` (**generated** by
  `scripts/generate-access-permissions.py` from each op's `x-foresignal-permission`; verified by
  `AccessPolicySyncTest`); `security/user/Permissions.java` (`SELF_SOURCES` already exists — no new
  permission).
- `src/main/resources/db/migration/` — highest is `V33__self_editions.sql`; **next free is `V34`**. The owner
  column precedent is `V28` (editions): `alter table … add column owner_user_id uuid` + FK to `"user"(id)`
  `on delete cascade` + an index.

## Build

1. **Per-user ownership on custom sources.** Add `owner_user_id uuid` to `custom_source`
   (`CustomSourceEntity` field + additive Flyway `V34__custom_source_owner.sql`, mirroring `V28`: nullable
   so existing operator rows stay owner-less, FK to `"user"(id)` `on delete cascade`, index on
   `(owner_user_id, source_id)`). Flyway runs before Hibernate validates, so the entity field matches under
   `ddl-auto=validate`. Add repository finders `findBySourceIdAndOwnerUserId` and
   `findByOwnerUserIdOrderBySourceId`. **Verify** the migration applies cleanly against the
   Hibernate-era `custom_source` table (baseline strategy); if it does not, log it to the CR and stop only if
   truly blocked.
2. **Self-service controller** `inbound/MyCustomSourcesController.java` (`@Tag("Sources")` to match the
   contract tag) for the eight operations. Mirror `CustomSourcesController` and `EditionsController`:
   - `requireSession(request)` → 401 on an API token (self-service needs a signed-in session, not agent
     scope).
   - **Create** forces `owner_user_id` to `session.userId()`; reuse `CustomSourceService.create` (add an
     owner-aware overload, or set the owner on the entity the service persists) so the id/dup/validation/
     registry/one-time-key behavior is identical to the operator path. The custom-source id keeps its
     `^custom-[a-z0-9]+(-[a-z0-9]+)*$` contract pattern.
   - **List/get/update/delete/rotate/mapping-test/samples** resolve the row by id **and** owner; a row the
     caller does not own (or an unknown/bad id) is **404**, never 403, never another user's data. Update keeps
     the operator 409s (`KIND_IN_USE`, `OBSERVED_AT_MODE_LOCKED`). Delete unregisters the descriptor as the
     operator path does.
   - **Mapping test / samples** delegate to `MappingPreviewService.test` and `SampleCapture.list` after the
     ownership check — the same dry-run/sample behavior as the admin endpoints, scoped to an owned source.
3. **Access rules (two places).** Add the `/v1/me/custom-sources` and `/v1/me/custom-sources/*` entries to
   `AccessPolicy.rules()` inside the `/v1/me/*` block (`READ_ALWAYS_AUTHENTICATED`, first-match-wins order),
   and regenerate `AccessPermissions.java` with `scripts/generate-access-permissions.py` (the spec already
   carries `x-foresignal-permission: self.sources` on these ops) so `AccessPolicySyncTest` stays green. If the
   generator emits rows only for `implemented` operations, note it in the CR — `SG-004` regenerates after
   activation.
4. **Audit as the user.** Route self-service writes through the session/`AuditService` path (as editions do),
   not `Actors`/`McpCaller` (there is no `user:` actor). Ingest itself is unchanged: an owned source posts to
   the same `POST /v1/ingest/{sourceId}` with its ingest key.

## Tests to write (do not run)

- `SelfServiceCustomSourceIT` — a `self.sources` session user (no agent scope) creates a custom source
  (owner forced to the caller, one-time key returned), lists only their own, reads/updates/deletes/re-keys it;
  another user's source is 404; an API token is 401.
- `CustomSourceOwnershipIT` — the operator `/v1/custom-sources` still sees all rows; an owner-less (legacy
  operator) row is not returned by `/v1/me/custom-sources`; a rotated key invalidates the old one.
- `CustomSourceMappingSelfServiceIT` — mapping test and samples work for an owned source and are 404 for one
  the caller does not own.
- Extend `AccessPolicySyncTest` expectations if it enumerates `/v1/me/*` paths.

## What runs

Compile only — `./gradlew compileJava compileTestJava` — and `./gradlew checkUsEnglish`. **Write tests, do not
run them.** Regenerating `AccessPermissions.java` is code generation, not the test suite. No SDK/Postman
generation here (that is `SG-004`).

## Done when

A `self.sources` user manages their own custom sources through `/v1/me/custom-sources` (create/list/get/update/
delete/rotate/mapping-test/samples), owner forced to the caller, un-owned access 404, API token 401; the
operator surface is unchanged; `custom_source` has a nullable owner column via `V34` with a matching entity
field; access rules updated in both places; compile and `checkUsEnglish` green; the eight ops remain `planned`.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; `.gitlab-ci.yml`; other prompts'
files; the operator `/v1/custom-sources` behavior. The 2.2.0 contract is **authored** — implement to it; add a
missing error code or field only if the implementation needs one, and record it in the CR. **Stage only this
prompt's files.**

## Commit

`SG-001: Custom sources (self-service)`, then
`completion-reports/SG-001-CR-Custom-Sources.md` with the 40-character commit hash and any owner questions
(notably: whether the `V34` migration applied cleanly against the Hibernate-era `custom_source` table, and
whether `generate-access-permissions.py` emits rows for `planned` operations).
