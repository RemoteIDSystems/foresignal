# SG-001-CR-Custom-Sources

| | |
|---|---|
| Prompt | SG-001-Custom-Sources.md |
| Status | Complete (compile only; `compileJava`/`compileTestJava` green; `checkUsEnglish` green; no test run) |
| Implementation commit | `d5390970987901fb23c18667b810325e0eeb929b` |
| Date | 2026-10-30 |

A signed-in user holding `self.sources` owns and manages their own custom sources through `/v1/me/custom-sources`
(create/list/get/update/delete/rotate/mapping-test/samples). The eight operations remain `x-foresignal-status:
planned` — `SG-004` activates them. The operator `/v1/custom-sources` surface is unchanged.

## What was added

- **Per-user ownership on custom sources.** `custom_source.owner_user_id uuid` (nullable), added by additive migration
  `V34__custom_source_owner.sql` mirroring `V28` (editions): FK to `"user"(id)` `on delete cascade`, index
  `(owner_user_id, source_id)`. `CustomSourceEntity` gained the matching `ownerUserId` field (plus getter/setter), so
  the entity validates under `ddl-auto=validate` (Flyway runs first). `CustomSourceRepository` gained
  `findBySourceIdAndOwnerUserId` and `findByOwnerUserIdOrderBySourceId`.
- **Owner-aware service methods** on `CustomSourceService`:
  - `create(JsonNode, UUID ownerUserId)` — identical to the operator `create(JsonNode)` (now a null-owner delegate)
    except the row carries the owner; same id/dup/validation/registry/one-time-key behavior.
  - `listOwned(UUID)`, `getOwned(String id, UUID owner)` (an unknown id, another user's source, and an
    operator-created or external source are all `404` — identical to missing), and owner-checked `update`, `rotateKey`
    and `delete` overloads that run the ownership check then delegate to the operator path (so the 409s
    `KIND_IN_USE`/`OBSERVED_AT_MODE_LOCKED`, the descriptor re-registration and the delete unregister are identical).
- **`MyCustomSourcesController`** (`@Tag("Sources")`) for the eight operations. `requireSession(request)` → `401` for
  an API token (self-service needs a session, not agent scope). Create forces `owner_user_id` to `session.userId()`.
  List/get/update/delete/rotate/mapping-test/samples resolve by id **and** owner, so a source the caller does not own
  is `404`. Delete also calls `IngestRecordLimiter.forget`, as the operator path does. Mapping-test and samples do the
  ownership check (`getOwned`) and then delegate to the same `MappingPreviewService.test` / `SampleCapture.list` the
  admin endpoints use. The created-source response (`CustomSourceCreated`: the row view plus the one-time `ingestKey`
  and preset `setup`) is shared with the operator controller through an extracted package-private
  `CustomSourcesController.createdResponse(...)`, so the two surfaces return an identical shape.
- **Access rules (two places).** Eight `/v1/me/custom-sources*` rules added to `AccessPolicy.rules()` inside the
  `/v1/me/*` block (`READ_ALWAYS_AUTHENTICATED`), with the specific sub-paths (`.../*/ingest-key/rotate`,
  `.../*/mapping/test`, `.../*/samples`) before the single-segment `/v1/me/custom-sources/*`. `AccessPermissions.java`
  regenerated from the contract (`self.sources` already annotated). Audit is the normal session pipeline (the operator
  path's audit), no new audit code.

## Tests written (not run)

- `SelfServiceCustomSourceIT` — a `self.sources`/`data.read` read-scope session (no agent scope) is `403` on the
  operator `POST /v1/custom-sources` but creates through `/v1/me/custom-sources` (one-time key, `ingestKeyPrefix`
  matches), lists only its own, gets/updates/deletes/re-keys it; another user's source is `404` to get/update/delete/
  rotate; an API token is `401` for every operation.
- `CustomSourceOwnershipIT` — the operator `/v1/custom-sources` sees both an operator-created (owner-less) source and
  an owned one; the self-service surface returns only the caller's owned source; a self-service key rotation makes the
  old key `401` and the new key `200` on `/v1/ingest/{id}`.
- `CustomSourceMappingSelfServiceIT` — for an owned `ecowitt` source with a captured sample, the owner reads
  `/samples` (200) and runs `/mapping/test` (200, `mappingSource: stored`); another user gets `404` on both (the
  ownership check precedes any delegation).

## Decisions / owner questions

- **The `V34` migration applies cleanly.** `custom_source` is created in `V1__baseline.sql` (a normal Flyway-managed
  table, not Hibernate-era drift), so the additive `add column` + FK + index is a plain forward migration; no baseline
  concern. (Verified by inspection of `V1__baseline.sql`; not run against a live DB in this compile-only pass.)
- **`generate-access-permissions.py` emits a row for every operation, not only `implemented` ones.** Regenerating
  therefore added rows for all fifteen 2.2.0 ops (the eight SG-001 paths plus the five SG-002 and two SG-003 paths
  already authored in the contract), since the generator transcribes the whole spec. This is expected and matches how
  NP-008 handled it. `AccessPolicy`'s hand-written scope rules still cover only SG-001's eight paths; SG-002 and SG-003
  add theirs. `AccessPolicySyncTest` (which asserts both halves agree with the spec for every op) is part of the
  owner's post-campaign full test pass, by which point all fifteen scope rules exist, so the transient asymmetry is
  not observable under the compile-only gate.
- **Owner-less sources stay operator-only.** Operator-created sources and external (EDP) providers have a null
  `owner_user_id` and are never returned by the self-service surface (`getOwned` filters them out, identical to a
  `404`), so no existing source becomes visible to a user.

## Gate

- `./gradlew compileJava compileTestJava` — green (only pre-existing deprecation/unchecked notes).
- `./gradlew checkUsEnglish` — green.
- No test suite, `lintOpenApi`, `checkApiCompat` or SDK/Postman generation (per the campaign's compile-only gate;
  `SG-004` runs those).
