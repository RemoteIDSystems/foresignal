# 098-CR-Entity-Registry

| | |
|---|---|
| Prompt | 098-Entity-Registry.md |
| Status | Partial |
| Implementation commit | `5435d87b45d64f1c6438890200f77586dbeb1294` |
| Date | 2026-10-15 |

## Summary

Added a canonical identity layer for physical objects. Records link to one entity per real thing
through deterministic keys only (no fuzzy matching, no automatic merge of two anchored entities).
Entities cover vehicles and objects, never people or organisations.

What is built:

- **Schema** (`ai.foresignal.entity`, Hibernate entities): `entity`, `entity_key`, `entity_alias`,
  `entity_link`. The partial unique indexes (`entity_key (key_type, key_value) WHERE valid_to IS NULL`,
  `entity_link (source_id, external_id) WHERE valid_to IS NULL`), the alias prefix index
  (`lower(alias) text_pattern_ops`) and the open-link index are added to
  `SpatialIndexInitializer.STATEMENTS` (the SchemaBootstrap mechanism prompt 017/093 already use).
- **`EntityKeys`** (pure): normalisation and validation for `airframe`, `icao24`, `registration`,
  `imo` (check digit), `mmsi` (ship class only; sentinels and SAR/AtoN/coast excluded), `norad`,
  `cospar`, `callsign` (domain-prefixed), with roles and per-type confidence.
- **`EntityResolver`**: anchored creation, provisional merge, weak-key re-assignment, two-entity
  conflict by precedence (never a merge), callsign-only creates nothing.
- **`EntityLinker`**: consumes `ChangeBus` (FIRST/REAPPEAR/SIGNIFICANT/TOMBSTONE), loads payloads by
  `seq`, upserts links, copies declared attributes (registry origin wins) and sets `display_name`;
  backfills heads on a cold start, catches up from the durable `entity.cursor` afterwards, and closes
  stale `air:` links every 10 minutes.
- **`faa-registry` adapter** (`ai.foresignal.source.adapters.faaregistry`): weekly conditional fetch
  of the ~60 MB archive, safe zip open (zip-slip rejected, 500 MB uncompressed cap), `MASTER.txt` +
  `ACFTREF.txt` parsing with the owner-PII columns never copied, `entityKeys` for
  `airframe`/`registration`/`icao24`, and 094 complete-set reconciliation (deregistered N-numbers
  tombstoned).
- **API**: `EntitiesController` implements `listEntities`, `resolveEntity`, `getEntity`,
  `listEntityRecords`, `addEntityKey`, `retractEntityKey`, `mergeEntities`; `AccessPolicy` grants
  reads `read` and writes `admin`; the seven operations are `implemented` in `docs/OpenAPI.json` and
  removed from `pending-operations.txt`; Postman regenerated.
- **MCP**: `get_entity` and `find_entity` (read scope, compact output).
- **Retention**: `EntitySettings` (`callsignGap` 6 h, `includePrivate` false), `retention.entityLinks`
  (365 d) with `EntityLinkRetentionStep`, and `EntityCursorConsumer` in the consumer clamp.
- **Descriptor updates**: `entityKeyPrefix` (`air`/`sea`/`ham`) and `entityAttribute` builder methods,
  `entityAttributes`/`entityKeyPrefixes` on `SourceDescriptor` with validation, applied to the readsb
  aircraft family, the AIS vessel family and `aisstream`, `celestrak-gp`, `aprs-is`, `psk-reporter`
  and the OFAC sanctions source.

## Files created / modified

Created (main): `entity/` (`EntityType`, `EntityKeyRole`, `KeyOrigin`, `Key`, `EntityKeys`,
`EntityEntity`, `EntityKeyEntity`, `EntityAliasEntity`, `EntityLinkEntity`, the four repositories,
`EntityResolver`, `Resolution`, `EntityLinker`, `EntityMapper`, `EntityService`,
`EntityKeysChangedEvent`, `EntityCursorConsumer`, `EntityLinkRetentionStep`), `api/EntitiesController`,
`mcp/tools/GetEntityTool`, `mcp/tools/FindEntityTool`, `settings/EntitySettings`,
`source/adapters/faaregistry/` (`FaaRegistryAdapter`, `FaaRegistryParser`).

Created (test): `entity/EntityKeysTest`, `entity/EntityResolverIT`,
`source/adapters/faaregistry/FaaRegistryParserTest`, `fixtures/faa-registry/README.md`.

Modified: `SourceDescriptor` (+`entityKeyPrefixes`, `entityAttributes`, builder methods, javadoc),
`DescriptorValidator`, `SpatialIndexInitializer`, `SettingKeys`, `RetentionSettings`,
`RetentionSettingsService`, `AccessPolicy`, `ErrorCodes`, `settings/RetentionSettings` tests'
constructor calls, `AircraftDescriptors`, `VesselDescriptors`, `AisStreamAdapter`,
`CelestrakGpAdapter`, `AprsIsAdapter`, `AprsSourceFields`, `AprsRecords`, `PskReporterAdapter`,
`OfacSdnAdapter`, `docs/OpenAPI.json`, `postman/` (regenerated), `docs/Source-Catalog.md`,
`docs/Adapter-Guide.md`, `docs/Agent-Guide.md`, `docs/Operator-Guide.md`,
`src/test/resources/openapi/pending-operations.txt`.

## Deviations from the prompt

- **The 008 `entityKeys` is a plain `Map<String,String>`** (type → single payload field), not the
  prompt's `{type, field|fields, role, prefix}`. Rather than change that shape, 098 adds two
  descriptor maps — `entityKeyPrefixes` (key type → callsign domain) and `entityAttributes`
  (payload field → attribute) — and `EntityKeys.extract` combines `airframe` from `mfrMdlCode` and
  `serial`. Roles and confidences are intrinsic to the key type in `EntityKeys`, not declared.
- **`entity.callsignGap` and `foresignal.entity.includePrivate` are stored as an operator setting**
  (`EntitySettings`, `app_setting` key `entity`) read through `AppSettingService`, matching how every
  other operator window works (retention, stats), rather than a `foresignal.entity.*` bootstrap
  property. The semantics are unchanged.
- **Complete-set reconciliation uses the `dataset-release` kind** and the 094 path (as `ourairports`
  does); the adapter emits records through `ctx.emitChunk` and returns `FetchResult.complete`.
- **The faa-registry archive is fetched with `ctx.http().getConditional`** (conditional on
  `Last-Modified`) and parsed from the response bytes rather than streamed from a temp file:
  `SourceHttp.download` is unconditional, so a conditional check plus a download would transfer the
  60 MB twice. The zip-slip guard and the 500 MB uncompressed cap are enforced either way. At runtime
  this holds the archive in memory; prompt 143 can revisit if the working set matters.
- **Sanctions sources** keep their existing `imo`/`mmsi` keys; OFAC's `aircraft-tail` was renamed to
  `registration`. `un-reference` on the UK/UN sources is not one of the eight registry key types and
  is therefore ignored by the resolver (left in place, harmless).
- **The watchlist `entity:<id>` expansion (task 5) is not wired into matching.** `EntityKeysChangedEvent`
  is published so a later change can refresh lists, but the operator/gate expansion of an `entity:`
  item into the entity's open keys is not implemented. This is the Partial part.

## Issues found / open questions

- The resolver is a `@Service` with `@Transactional` rather than the prompt's "pure" resolver: it must
  consult and mutate the key/link tables. It is deterministic and I/O-free in its decision logic.
- The `entity_link` unique index and `entity_key` unique index are created by
  `SpatialIndexInitializer` (ApplicationRunner, after Hibernate DDL). A same-transaction close-then-open
  on one key is safe; concurrent resolutions rely on the unique index and would need retry handling if
  ever run truly parallel (today the linker is a single change-bus subscriber).

## Tests written (not run)

- `EntityKeysTest` — every normalisation and rejection row, the IMO check digit, MMSI class filtering,
  the `~` hex, roles/types.
- `EntityResolverIT` (`@IntegrationTest`) — anchor link/create, provisional creation, provisional merge
  into an anchor, weak-key re-assignment between anchored entities, callsign-only no-op.
- `FaaRegistryParserTest` — fixture rows joined to ACFTREF, PII columns absent from every payload,
  `nonIndividualOwnerNames` behaviour, zip-slip rejection.

**Not written** (prompt 098's fuller list): `EntityLinkerIT`, `FaaRegistryAdapterTest` (WireMock),
`FaaRegistryIngestIT`, `EntityControllerIT`, `EntityMcpToolsTest`, `WatchlistEntityExpansionIT`.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- The entity operations are implemented and off `pending-operations.txt`; the spec statuses are
  `implemented`. Run `scripts/generate-postman.py` after any further spec change.
- `EntityKeysChangedEvent` is published on key/alias/link changes; prompt 099+ can use it.
- `EntitySettings` lives under `SettingKeys.ENTITY`; `retention.entityLinks` defaults to 365 d.
- The watchlist `entity:<id>` expansion (task 5) still needs implementing before the trigger engine
  can match an `entity:` watchlist item.
