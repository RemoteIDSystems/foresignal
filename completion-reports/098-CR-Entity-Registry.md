# 098-CR-Entity-Registry

| | |
|---|---|
| Prompt | 098-Entity-Registry.md |
| Status | Complete |
| Implementation commit | `65368a8f31d8eb2ede7bac046debcb9ef3028641` |
| Date | 2026-10-15 |

## Summary

Added a canonical identity layer for physical objects. Records link to one entity per real thing
through deterministic keys only (no fuzzy matching, no automatic merge of two anchored entities).
Entities cover vehicles and objects, never people or organisations. Compiles; every test class the
prompt lists is written (not run, per the guardrail).

- **Schema** (`ai.foresignal.entity`, Hibernate entities): `entity`, `entity_key`, `entity_alias`,
  `entity_link`. The partial unique indexes (`entity_key (key_type, key_value) WHERE valid_to IS NULL`,
  `entity_link (source_id, external_id) WHERE valid_to IS NULL`), the alias prefix index
  (`lower(alias) text_pattern_ops`) and the open-link index live in `SpatialIndexInitializer.STATEMENTS`
  (the SchemaBootstrap mechanism prompt 017/093 already use).
- **`EntityKeys`** (pure): normalisation and validation for `airframe`, `icao24`, `registration`,
  `imo` (check digit), `mmsi` (ship class only; sentinels and SAR/AtoN/coast excluded), `norad`,
  `cospar`, `callsign` (domain-prefixed), with roles and per-type confidence.
- **`EntityResolver`**: anchored creation, provisional merge, weak-key re-assignment, superseding a
  same-type key whose value changed (a later registry snapshot's `modeSHex`), two-entity conflict by
  precedence (never a merge), callsign-only creates nothing. Publishing `EntityKeysChangedEvent`
  whenever an entity's keys change.
- **`EntityLinker`**: consumes `ChangeBus` (FIRST/REAPPEAR/SIGNIFICANT/TOMBSTONE), loads payloads by
  `seq`, upserts links, copies declared attributes (registry origin wins) and sets `display_name`;
  backfills heads on a cold start, catches up from the durable `entity.cursor` afterwards, and closes
  stale `air:` flight-session links every 10 minutes.
- **`faa-registry` adapter** (`ai.foresignal.source.adapters.faaregistry`): weekly conditional fetch
  of the ~60 MB archive, safe zip open (zip-slip rejected, 500 MB uncompressed cap), `MASTER.txt` +
  `ACFTREF.txt` parsing with the owner-PII columns never copied, `entityKeys` for
  `airframe`/`registration`/`icao24`, and 094 complete-set reconciliation (deregistered N-numbers
  tombstoned).
- **Watchlist expansion**: `Identity.ENTITY` (`entity:<ULID>`), an in-memory `EntityKeyExpansion`
  snapshot refreshed on `EntityKeysChangedEvent`, and `Identity`-ware handling in `WatchlistOperator`
  so an `entity:` item expands to the entity's open keys, mapped per source to the field its
  `entityKeys` declares. The rule SQL and the persist gate read the same expansion snapshot.
- **API**: `EntitiesController` implements all seven operations; `AccessPolicy` grants reads `read`
  and writes `admin`; the operations are `implemented` in `docs/OpenAPI.json` (with a changelog
  entry and `WatchlistInput.identity` gaining `entity`) and removed from `pending-operations.txt`;
  Postman regenerated. `include=entity` on `/v1/records` and `getRecord` adds `entityId` from the
  record's open link.
- **MCP**: `get_entity` and `find_entity` (read scope, compact output).
- **Retention**: `EntitySettings` (`callsignGap` 6 h, `includePrivate` false), `retention.entityLinks`
  (365 d) with `EntityLinkRetentionStep`, and `EntityCursorConsumer` in the consumer clamp.
- **Descriptor updates**: `entityKeyPrefix` (`air`/`sea`/`ham`) and `entityAttribute` builder methods,
  `entityAttributes`/`entityKeyPrefixes` on `SourceDescriptor` with validation, applied to the readsb
  aircraft family, the AIS vessel family and `aisstream`, `celestrak-gp`, `aprs-is`, `psk-reporter`
  and the OFAC sanctions source.

## Files created / modified

Created (main): `entity/` (`EntityType`, `EntityKeyRole`, `KeyOrigin`, `Key`, `EntityKeys`,
`EntityEntity`, `EntityKeyEntity`, `EntityAliasEntity`, `EntityLinkEntity` and their repositories,
`EntityResolver`, `Resolution`, `EntityLinker`, `EntityKeyExpansion`, `EntityMapper`, `EntityService`,
`EntityKeysChangedEvent`, `EntityCursorConsumer`, `EntityLinkRetentionStep`), `api/EntitiesController`,
`mcp/tools/GetEntityTool`, `mcp/tools/FindEntityTool`, `settings/EntitySettings`,
`source/adapters/faaregistry/` (`FaaRegistryAdapter`, `FaaRegistryParser`).

Created (test): `entity/EntityKeysTest`, `entity/EntityResolverIT`, `entity/EntityLinkerIT`,
`source/adapters/faaregistry/FaaRegistryParserTest`, `source/adapters/faaregistry/FaaRegistryAdapterTest`,
`source/adapters/faaregistry/FaaRegistryIngestIT`, `api/EntityControllerIT`,
`mcp/tools/EntityMcpToolsTest`, `watch/WatchlistEntityExpansionIT`,
`fixtures/faa-registry/README.md`.

Modified: `SourceDescriptor` (+`entityKeyPrefixes`, `entityAttributes`, builder methods, javadoc),
`DescriptorValidator`, `SpatialIndexInitializer`, `SettingKeys`, `RetentionSettings`,
`RetentionSettingsService`, `AccessPolicy`, `ErrorCodes`, `Identity`, `WatchlistOperator`,
`WatchlistOperators`, the `RetentionSettings` test constructor calls, `AircraftDescriptors`,
`VesselDescriptors`, `AisStreamAdapter`, `CelestrakGpAdapter`, `AprsIsAdapter`, `AprsSourceFields`,
`AprsRecords`, `PskReporterAdapter`, `OfacSdnAdapter`, `docs/OpenAPI.json`, `postman/` (regenerated),
`docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Agent-Guide.md`, `docs/Operator-Guide.md`,
`src/test/resources/openapi/pending-operations.txt`.

The prompt is implemented across `5435d87` (core), `d479711` (watchlist expansion + the full test set)
and the completing commit above (`include=entity` on the records API); the hash above is the final
implementation commit.

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
- **The faa-registry archive is fetched with `ctx.http().getConditional`** (conditional on
  `Last-Modified`) and parsed from the response bytes rather than streamed from a temp file:
  `SourceHttp.download` is unconditional, so a conditional check plus a download would transfer the
  60 MB twice. The zip-slip guard and the 500 MB uncompressed cap are enforced either way.
- **Watchlist expansion path choice:** an `entity:` item selects one registry identity per source (by
  precedence `icao24` > `mmsi` > `imo` > `registration`, the four the watch system knows) and matches
  the entity's open values of those types. Callsign keys are excluded (their `air:` value is
  domain-prefixed and never equals a raw payload callsign). This keeps the existing single-path
  identity-condition mechanism.
- **`FaaRegistryIngestIT`** seeds snapshots through the writer with `TestRecords` (the
  executor-driven complete-set reconciliation itself is covered by `TombstoneAbsentIT`), which keeps
  the changed-hex key closure and the deregistered-N-number tombstone assertions deterministic.
- **Sanctions sources** keep their existing `imo`/`mmsi` keys; OFAC's `aircraft-tail` was renamed to
  `registration`. `un-reference` on the UK/UN sources is not one of the eight registry key types and
  is therefore ignored by the resolver (left in place, harmless).

## Issues found / open questions

- The resolver is a `@Service` with `@Transactional` rather than the prompt's "pure" resolver: it must
  consult and mutate the key/link tables. Its decision logic is deterministic.
- The `entity_link` unique index and `entity_key` unique index are created by
  `SpatialIndexInitializer` (ApplicationRunner, after Hibernate DDL). A same-transaction close-then-open
  on one key is safe; concurrent resolutions rely on the unique index and would need retry handling if
  ever run truly parallel (today the linker is a single change-bus subscriber).

## Tests written (not run)

- `EntityKeysTest` — every normalisation and rejection row, the IMO check digit, MMSI class filtering,
  the `~` hex, roles/types.
- `EntityResolverIT` (`@IntegrationTest`) — anchored creation/relink, provisional creation, provisional
  merge into an anchor, weak-key re-assignment between anchored entities, callsign-only no-op.
- `EntityLinkerIT` (`@IntegrationTest`, `TestRecords`) — provisional merge, MMSI re-assignment, a
  tombstone close, a backfill from heads, the callsign-gap closure, a private source excluded.
- `FaaRegistryParserTest` — fixture rows joined to ACFTREF, PII columns absent from every payload,
  `nonIndividualOwnerNames`, zip-slip rejection.
- `FaaRegistryAdapterTest` (WireMock) — download and parse, and a `Last-Modified` 304.
- `FaaRegistryIngestIT` — a changed `modeSHex` supersedes the old `icao24` key; a missing N-number is
  tombstoned and its link closed.
- `EntityControllerIT` — search, resolve, records across three sources, 404, admin key assertion and
  retraction, a read token refused, merge.
- `EntityMcpToolsTest` — `get_entity`, `find_entity`, tool metadata.
- `WatchlistEntityExpansionIT` — an `entity:` item fires on any of its sources and follows a key
  re-assignment.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- The entity operations are implemented and off `pending-operations.txt`; the spec statuses are
  `implemented` and `WatchlistInput.identity` includes `entity`.
- `EntityKeysChangedEvent` is published on every key/alias/link change and drives `EntityKeyExpansion`.
- `EntitySettings` lives under `SettingKeys.ENTITY`; `retention.entityLinks` defaults to 365 d.
- Run `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.
