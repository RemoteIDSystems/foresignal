# 098-CR-Entity-Registry

| | |
|---|---|
| Prompt | 098-Entity-Registry.md |
| Status | Complete (post-review fixes applied 2026-10-16; see the last section) |
| Implementation commit | `65368a8f31d8eb2ede7bac046debcb9ef3028641` |
| Post-review fix commit | `1d50df1` (`098: fix post-review defects`) |
| Date | 2026-10-15 (fixes 2026-10-16) |

## Summary

Added a canonical identity layer for physical objects. Records link to one entity per real thing
through deterministic keys only (no fuzzy matching, no automatic merge of two anchored entities).
Entities cover vehicles and objects, never people or organizations. Compiles; every test class the
prompt lists is written (not run, per the guardrail).

- **Schema** (`ai.foresignal.entity`, Hibernate entities): `entity`, `entity_key`, `entity_alias`,
  `entity_link`. The partial unique indexes (`entity_key (key_type, key_value) WHERE valid_to IS NULL`,
  `entity_link (source_id, external_id) WHERE valid_to IS NULL`), the alias prefix index
  (`lower(alias) text_pattern_ops`) and the open-link index live in `SpatialIndexInitializer.STATEMENTS`
  (the SchemaBootstrap mechanism prompts 017/093 already use).
- **`EntityKeys`** (pure): normalization and validation for `airframe` (only with both the model code
  and the serial), `icao24`, `registration`, `imo` (check digit), `mmsi` (ship class only; sentinels and
  SAR/AtoN/coast excluded), `norad`, `cospar`, `callsign` (domain-prefixed; `ham:` is the base call with
  the SSID stripped), with roles and per-type confidence. Rejected values are reported
  (`foresignal.entity.invalid_keys{type}`).
- **`EntityResolver`**: anchored creation, provisional merge and weak-key re-assignment on the anchor
  path; a two-entity weak-key conflict links by precedence (`icao24`/`mmsi` > `registration` >
  callsign) and changes nothing else — no merge, no key moved, no key added — and counts
  `foresignal.entity.conflicts`; a key asserted by a stronger origin (operator > registry >
  observation) is never superseded or taken over by a weaker one; keys are considered in a fixed order,
  so the chosen anchor and link key never depend on descriptor or payload order. Callsign-only creates
  nothing. `EntityKeysChangedEvent` is published whenever an entity's keys change.
- **`EntityLinker`**: consumes `ChangeBus` (FIRST/REAPPEAR/SIGNIFICANT/TOMBSTONE), loads payloads by
  `seq`, upserts links, copies declared attributes (registry origin wins) and sets `display_name`;
  subscribes at application start and runs its cold-start backfill or catch-up on its own virtual
  thread (startup is never blocked); catch-up advances past every scanned row; closes stale `air:`
  flight-session links every 10 minutes.
- **`faa-registry` adapter** (`ai.foresignal.source.adapters.faaregistry`): weekly conditional fetch
  of the ~60 MB archive, safe zip open (zip-slip rejected, 500 MB uncompressed cap), `MASTER.txt` +
  `ACFTREF.txt` stream-parsed line by line with the owner-PII columns never copied, `entityKeys` for
  `airframe`/`registration`/`icao24`, and 094 complete-set reconciliation (deregistered N-numbers
  tombstoned).
- **Watchlist expansion**: `Identity.ENTITY` (`entity:<ULID>`), an in-memory `EntityKeyExpansion`
  snapshot refreshed on `EntityKeysChangedEvent`, and `Identity`-aware handling in `WatchlistOperator`
  so an `entity:` item expands to the entity's open keys, mapped per source to the field its
  `entityKeys` declares. The rule SQL and the persist gate read the same expansion snapshot.
- **API**: `EntitiesController` implements all seven operations; `AccessPolicy` grants reads `read`
  and writes `admin`; the operations are `implemented` in `docs/OpenAPI.json` (with a changelog entry
  and `WatchlistInput.identity` gaining `entity`) and removed from `pending-operations.txt`; Postman
  regenerated. `include=entity` on `/v1/records` and `getRecord` adds `entityId` from the record's open
  link.
- **MCP**: `get_entity` and `find_entity` (read scope, compact output).
- **Retention**: `EntitySettings` (`callsignGap` 6 h, `includePrivate` false), `retention.entityLinks`
  (365 d) with `EntityLinkRetentionStep`, and `EntityCursorConsumer` in the consumer clamp.
- **Descriptor updates**: `entityKeyPrefix` (`air`/`sea`/`ham`) and `entityAttribute` builder methods,
  `entityAttributes`/`entityKeyPrefixes` on `SourceDescriptor` with validation (all three entity maps
  keep their declaration order), applied to the readsb aircraft family, the AIS vessel family and
  `aisstream`, `celestrak-gp`, `aprs-is`, `psk-reporter` and the OFAC sanctions source.

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
`fixtures/faa-registry/README.md`, `fixtures/faa-registry/edge-cases.zip` (post-review).

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
  prompt's `{type, field|fields, role, prefix}`. Rather than change that shape, 098 adds two descriptor
  maps — `entityKeyPrefixes` (key type → callsign domain) and `entityAttributes` (payload field →
  attribute) — and `EntityKeys.extract` combines `airframe` from `mfrMdlCode` and `serial`. Roles and
  confidences are intrinsic to the key type in `EntityKeys`, not declared.
- **The `entity` cursor is stored in `app_setting` (`entity.cursor`), not in a `consumer_cursor`
  table.** The prompt says to reuse 038's `consumer_cursor(name, last_seq)` if it exists and otherwise
  create it here. It does not exist (`grep -rn consumer_cursor src/main/java` finds nothing); the trigger
  (`trigger.cursor`), region (`region.cursor`) and embedding cursors all live in `app_setting`, so the
  entity cursor follows them rather than introducing a one-off table. Not changed post-review; the owner
  may decide whether a shared `consumer_cursor` table is wanted.
- **`entity.callsignGap` and `foresignal.entity.includePrivate` are stored as an operator setting**
  (`EntitySettings`, `app_setting` key `entity`) read through `AppSettingService`, matching how every
  other operator window works (retention, stats), rather than a `foresignal.entity.*` bootstrap
  property. The semantics are unchanged.
- **The faa-registry archive is fetched with `ctx.http().getConditional`** (conditional on
  `Last-Modified`) and held as bytes rather than downloaded to a temp file: `SourceHttp.download` is
  unconditional, so a conditional check plus a download would transfer the 60 MB twice. The archive's
  entries are **stream-parsed** line by line (post-review): two sequential passes over the zip (the
  reference table first, then `MASTER.txt`), so neither file is ever held in memory whole. The zip-slip
  guard and the 500 MB uncompressed cap (a counting stream) are enforced either way.
- **Registry keys outrank observed ones** (post-review). The prompt says "registry origin winning over
  observations" for attributes; the resolver applies the same rule to keys, so an observed
  `registration` (adsb.lol `r`) never closes or takes over an FAA-asserted one. Such a disagreement is
  counted as a conflict and the observed key is not opened.
- **Watchlist expansion path choice:** an `entity:` item selects one registry identity per source (by
  precedence `icao24` > `mmsi` > `imo` > `registration`, the four the watch system knows) and matches
  the entity's open values of those types. Callsign keys are excluded (their `air:` value is
  domain-prefixed and never equals a raw payload callsign).
- **`FaaRegistryIngestIT`** seeds snapshots through the writer with `TestRecords` (the executor-driven
  complete-set reconciliation itself is covered by `TombstoneAbsentIT`), which keeps the changed-hex key
  closure and the deregistered-N-number tombstone assertions deterministic.
- **Sanctions sources** keep their existing `imo`/`mmsi` keys; OFAC's `aircraft-tail` was renamed to
  `registration`. `un-reference` on the UK/UN sources is not one of the eight registry key types and is
  therefore ignored by the resolver (left in place, harmless).
- **The FAA fixture is synthetic.** The prompt asks for a captured real archive cut to 30 `MASTER.txt`
  rows; none is committed. The parser tests build hand-made archives with the real column names, and
  `fixtures/faa-registry/README.md` says so (recapture at prompt 143). The hand-made `edge-cases.zip`
  the prompt asks for was missing and was added post-review.

## Issues found / open questions

- The resolver is a `@Service` with `@Transactional` rather than the prompt's "pure" resolver: it must
  consult and mutate the key/link tables. Its decision logic is deterministic.
- The `entity_link` unique index and `entity_key` unique index are created by
  `SpatialIndexInitializer` (ApplicationRunner, after Hibernate DDL). A same-transaction close-then-open
  on one key is safe; concurrent resolutions rely on the unique index and would need retry handling if
  ever run truly parallel (today the linker is a single change-bus subscriber).
- **Doc conflict left for the owner:** `docs/Invariants.md` RET-3 says intelligence tables are never
  touched by `RetentionJob`, while this prompt (line 88) requires `EntityLinkRetentionStep` to delete
  closed links older than `retention.entityLinks`. The step is kept as the prompt specifies.

## Tests written (not run)

- `EntityKeysTest` — every normalization and rejection row, the IMO check digit, MMSI class filtering,
  the `~` hex, roles/types; post-review: the `ham:` SSID strip, no airframe without the model code, and
  the invalid-key report.
- `EntityResolverIT` (`@IntegrationTest`) — anchored creation/relink, provisional creation, provisional
  merge into an anchor, re-assignment of an MMSI co-asserted with a new IMO, callsign-only no-op;
  post-review: the two-entity conflict (precedence, no merge, no key moved — with the FAA-anchored
  registration case from the review), two provisional entities in conflict, an observed key never
  superseding a registry key, a later registry snapshot superseding its own key, and an order-independent
  link key.
- `EntityLinkerIT` (`@IntegrationTest`, `TestRecords`) — provisional merge, MMSI re-assignment, a
  tombstone close, a backfill from heads, the callsign-gap closure, a private source excluded;
  post-review: catch-up over chunks with no entity rows terminates, startup finishes.
- `FaaRegistryParserTest` — fixture rows joined to ACFTREF, PII columns absent from every payload,
  `nonIndividualOwnerNames`, zip-slip rejection; post-review: `MASTER.txt` before `ACFTREF.txt`, a
  missing `MASTER.txt`, and the `edge-cases.zip` fixture (rejected for its traversal entry; its blank-hex
  and individual-registrant rows).
- `FaaRegistryAdapterTest` (WireMock) — download and parse, and a `Last-Modified` 304.
- `FaaRegistryIngestIT` — a changed `modeSHex` supersedes the old `icao24` key; a missing N-number is
  tombstoned and its link closed.
- `EntityControllerIT` — search, resolve, records across three sources, 404, admin key assertion and
  retraction, a read token refused, merge.
- `EntityMcpToolsTest` — `get_entity`, `find_entity`, tool metadata.
- `WatchlistEntityExpansionIT` — an `entity:` item fires on any of its sources and follows a key
  re-assignment.
- `SourceDescriptorBuilderTest` (post-review) — the entity maps keep their declaration order.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (also after the post-review fixes).

## Notes for the next prompt

- The entity operations are implemented and off `pending-operations.txt`; the spec statuses are
  `implemented` and `WatchlistInput.identity` includes `entity`.
- `EntityKeysChangedEvent` is published on every key/alias/link change and drives `EntityKeyExpansion`.
- `EntitySettings` lives under `SettingKeys.ENTITY`; `retention.entityLinks` defaults to 365 d.
- Run `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

## Post-review fixes (2026-10-16)

Fix commit `1d50df1`. Every finding was re-verified against the code before it was changed.

1. **Conflict path merged and re-assigned (HIGH, fixed).** `EntityResolver.resolve` called
   `reconcileWeak` on the two-entity conflict path, so a provisional holder was merged and an anchored
   holder's key (for example an FAA `registration`) was closed and moved to the precedence winner. The
   conflict path now links by precedence and does nothing else; it counts
   `foresignal.entity.conflicts` and logs. The original `EntityResolverIT` "re-assignment between
   anchored entities" test could never have passed (its weak-only resolutions created provisional
   entities); it was rewritten to co-assert the MMSI with each IMO.
2. **Catch-up loop could hang startup (HIGH, fixed).** `EntityLinker.catchUp` only advanced on
   entity-source rows, so a full chunk of other rows was re-read forever inside the
   `ApplicationReadyEvent` listener. It now advances on every scanned row (and also skips non-relevant
   persist reasons), and `start()` subscribes first and runs the backfill/catch-up on a virtual thread;
   the listener waits for it and skips batches the startup already covered.
3. **`closeOpenLinks` had no transaction (HIGH, fixed).** Every `@Modifying` query of the entity
   repositories is now `@Transactional` on the repository method (no self-invocation pitfall).
4. **Observed keys overrode registry keys (MEDIUM, fixed).** See "Registry keys outrank observed ones"
   above.
5. **FAA files read whole into memory (MEDIUM, fixed).** See the faa-registry deviation above.
6. **`Map.copyOf` randomized entity map order (MEDIUM, fixed).** `entityKeys`, `entityKeyPrefixes` and
   `entityAttributes` are insertion-ordered unmodifiable copies, and the resolver sorts keys by a fixed
   order, so the link key and anchor are stable across JVMs.
7. **Bare-serial airframe key (LOW, fixed).** No `airframe` key is emitted without `mfrMdlCode`.
8. **`ham:` kept the SSID (fixed).** The prompt says "base call, SSID stripped"; normalization now
   strips it (the payload field is unchanged). Domain prefixes are normalized to lower case.
9. **Invalid-key metric was never incremented (fixed while there).** The linker reports rejected values.
10. **Cursor table:** not moved (see the deviation).

**Inaccuracies corrected in this report:** the original summary said a two-entity conflict links by
precedence and never merges, but the code merged and re-assigned on that path (now true after fix 1);
the original test list claimed a passing "re-assignment between anchored entities" test that could not
pass; the FAA fixture is now disclosed as synthetic and the missing edge-cases zip is added; the
"parsed from the response bytes" deviation now states the streaming parse.
