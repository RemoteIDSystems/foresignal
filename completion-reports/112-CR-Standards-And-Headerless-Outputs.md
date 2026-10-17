# 112-CR-Standards-And-Headerless-Outputs

| | |
|---|---|
| Prompt | 112-Standards-And-Headerless-Outputs.md |
| Status | Complete (one owner decision below: private share targets are refused, see Deviations 1) |
| Implementation commit | `842d927ca04521207cc2e69c6ce77f888c9ebf6d` |
| Date | 2026-10-16 |

## Summary

Foresignal is served through four standards that existing tools speak, and to clients that can only fetch a
URL. Compiles; tests written, not run.

- **OGC API – Features (`ai.foresignal.ogc`, `/v1/ogc`):** `OgcFeatureService` exposes one collection per declared
  kind for current state (`record_head ⋈ record_live`, effective time) and one `{kind}-history` collection whose
  items are versions (`record_version`, observed time). Collection metadata has `itemType: feature`, CRS84, the
  109 aggregate attribution, a spatial extent from `ST_Extent` of live geometry and a temporal extent from observed
  times (computed per kind and source in one query, cached 5 minutes, combined per caller so private sources never
  shape it) and, for history, `seq_clock`'s first minute. `OgcFeaturesController` serves `ogcItems`, `ogcItem` and
  `ogcQueryables`; `OgcController` (108) now adds the feature collections to `ogcCollections`/`ogcCollection`, the
  Features/Part 3/CQL2 classes to `ogcConformance` (no `oas30`) and mentions Features on the landing page. Items:
  GeoJSON with `self`/`next`/`collection` links, `numberReturned`, `timeStamp`, `numberMatched` only with
  `count=true` and ≤ 100,000, `attributions`, `licenceFiltered`; `limit` ≤ 10,000; keyset cursors through
  `CursorCodec` (`(source_id, external_id)` current, `seq` history); `bbox` with 4 or 6 numbers, `bbox-crs` and
  `filter-crs` CRS84 only; `datetime` instant/`a/b`/`../b`/`a/..` (`OgcDatetime`); `source`, `licencePolicy`;
  unknown parameters 400. Private sources, restrictions (`RestrictionClamp`), feed keys and the licence policy
  apply; `Foresignal-Attribution` and `Link rel="license"` headers.
- **CQL2 (`ai.foresignal.ogc.cql2`):** `Cql2TextParser` (hand-written recursive descent) and `Cql2JsonParser`
  (Jackson) build one tree (`Cql2`) in a normal form (flattened AND/OR, `IS NOT NULL` = `NOT(IS NULL)`, literal
  side turned around, numbers without trailing zeros), so both encodings give equal trees. Supported: `= <> < <= >
  >=`, `IS [NOT] NULL`, `AND/OR/NOT`, parentheses, string/number/boolean/`TIMESTAMP`/`DATE` literals, properties,
  `S_INTERSECTS` with `POINT`/`POLYGON`/`BBOX`; depth ≤ 20, ≤ 200 nodes; errors carry a character offset or JSON
  pointer. `Cql2SqlTranslator` emits parameterized SQL, reusing the 017 operators (`Operators` `>`… and `==`/`!=`
  through `OperatorRegistry.builtIn()` with a payload `SqlTerm`) for payload keys, typed columns for the built-ins,
  `COALESCE(…, false)` per atom as in rules; unknown property → `400 FILTER_INVALID` with `position`.
- **CAP 1.2 (`ai.foresignal.cap`):** `CapService` subscribes to the `AlertBus` and stores one message per state
  change in `cap_message` (`CapMessageEntity`): `fired` → `Alert`; acknowledge, snooze, update, enrich → `Update`;
  resolve → `Update` + `responseType AllClear`; never `Cancel`; `references` lists every prior message
  (`sender,identifier,sent`); identifier `{installId}-{alertId}-{n}`; sender setting `cap.sender` (default
  `foresignal@{installId}`); status `Exercise` when the alert context carries `"exercise": true` (115's flag),
  else `Actual`. `CapMessageWriter` (StAX) writes the CAP 1.2 element order with `scope Public`, the not-official
  note, `+00:00` times; `<info>` with `en-US`, category from `cap/categories.json` (`CapCategories`), event = rule
  name, urgency `Immediate` for critical ENTER/EXIT rules else `Unknown`, severity map, certainty
  `Observed`/`Likely` (DERIVED)/`Unknown`, effective/expires (record `valid_until`), `senderName "Foresignal
  ({instance.name})"`, headline ≤ 160, plain-text description, `web` (the `clientLinks.alert` link, or the upstream
  message's `web` for official CAP sources, whose upstream sender is named in the description), the five
  `foresignal*` parameters (grade from `CredibilityService`). `CapAreas`: polygon of `lat,lon` pairs (largest part,
  closed, Douglas–Peucker to ≤ 100 points; lines use their hull) or a circle with the rule buffer or 1 km;
  `areaDesc` = place names from the alert context, the payload's `areaDesc`, or `{lat},{lon}`. `CapController`:
  `capAtom` (058's `AtomFeedWriter`, entries link `application/cap+xml`; filters `ruleId`, `minSeverity`, `tags`,
  `placeId`, `since`), `getCapForAlert`, `getCapMessage`; ETag + 304; restriction, private gate and `FEED` egress as
  058. Retention: `OperationalTablesStep` deletes messages whose alert is gone.
- **iCalendar (`ai.foresignal.ical`):** `ICalWriter` (hand-written: CRLF, 75-octet folding that never splits a
  UTF-8 sequence or surrogate pair, TEXT escaping, UTC `Z`, `PRODID`/`VERSION`/`CALSCALE`/`METHOD`/`X-WR-CALNAME`/
  `REFRESH-INTERVAL`/`X-PUBLISHED-TTL`, `X-FORESIGNAL-TRUNCATED`). `CalendarService` builds `launches`, `tfr`, `nws`,
  `briefings` (30 days of cron occurrences per enabled visible briefing through `BriefingValidator.nextRun`,
  DST-correct, no RRULE) and `custom` (`400 ICAL_FIELDS_MISSING` without `startField`/`endField`); UID
  `{sourceId}:{externalId}@{instanceId}`, SEQUENCE = version, DTSTAMP/LAST-MODIFIED = ingest time of that version,
  URL = `clientLinks.record`, DESCRIPTION ends with the attribution; tombstoned or expired records stay 7 days as
  `CANCELLED`; ≤ 2,000 events. `CalendarsController` (`getCalendar`): filters, ETag + 304.
- **Share links (`ai.foresignal.share`):** `share_link` (`ShareLinkEntity`) stores the SHA-256 of
  `fss_` + 32 random bytes (base64url), the target, mask, precision, expiry (default 7 d, max 30 d), `max_uses`,
  `uses`, creator, revocation, last use and the creator's restriction snapshot. `ShareKeyResolver` plugs into the
  035 `PathKeyFilter` (prefix dispatch, backoff, hygiene headers); the new `PathKeyResolver.forwards()` lets the
  filter validate the key, method and sub-path (`""`, `/geojson`, `/tiles/`, `/download`) and pass the request on
  unforwarded to `ShareLinksController` at `/v1/k/{key}/…`; the key is never a bearer token (`TokenService` accepts
  only `fsr_`). `ShareLinkService`: create (target exists and is visible to the creator; `PUBLIC_SHARE` through
  `EgressPolicy.decide`; `409 SHARE_LICENCE_FORBIDDEN`), list (own, admin all), revoke, use (60/min per link
  through `ClientRateLimiter`, atomic `UPDATE … uses + 1` guarded by status, `410 SHARE_EXPIRED`), render (JSON
  `SharedContent` or GeoJSON, licence-filtered per record, `ShareMask` allowlist and precision, attributions).
  `?open=1` redirects to `clientLinks.share` (new). Audit on create, revoke and use (first use, then at most one
  per link per hour).
- **Spec/Postman/MCP/docs:** the 14 operations implemented and removed from `pending-operations.txt`;
  `AccessPolicy` rules; MCP `create_share_link` (agent) and its Agent Guide row; Postman BODIES and CAPTURE for
  `createShareLink`, collection regenerated (836 requests); Operator Guide "Standards outputs";
  THIRD_PARTY_NOTICES (CAP XSD); Invariants EGR-1 lists the new tests.

## Files created / modified

**Created (main):** `ogc/{OgcFeatureService,OgcFeatureDocuments,OgcDatetime,package-info}`,
`ogc/cql2/{Cql2,Cql2Exception,Cql2TextParser,Cql2JsonParser,Cql2Queryables,Cql2SqlTranslator,package-info}`,
`cap/{CapService,CapMessage,CapMessageWriter,CapAreas,CapCategories,CapMessageEntity,CapMessageRepository,package-info}`,
`ical/{ICalWriter,CalendarService,package-info}`,
`share/{ShareLinkService,ShareLinkEntity,ShareLinkRepository,ShareKeyResolver,ShareMask,package-info}`,
`api/{OgcFeaturesController,CapController,CalendarsController,ShareLinksController}`,
`mcp/tools/CreateShareLinkTool`, `src/main/resources/cap/categories.json`.

**Modified (main):** `api/OgcController` (feature collections, conformance, landing), `api/error/ErrorCodes`
(`FILTER_INVALID`, `ICAL_FIELDS_MISSING`, `SHARE_LICENCE_FORBIDDEN`, `SHARE_EXPIRED`, `INVALID_SHARE_KEY`),
`feed/FeedEntry` (optional `linkType`, old constructor kept) and `feed/AtomFeedWriter` (writes it),
`retention/OperationalTablesStep` (`cap_message`), `security/AccessPolicy`, `security/restrict/{PathKeyFilter,
PathKeyResolver}` (`forwards()`, pass-through), `settings/{ClientLinks,ServerSettingsService}` (`share`),
`settings/SettingKeys` (`cap.sender`, `instance.name`).

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md`,
`THIRD_PARTY_NOTICES.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−14).

**Tests:** `ogc/cql2/{Cql2TextParserTest,Cql2JsonParserTest,Cql2SqlTranslatorTest}`, `ogc/{OgcDatetimeTest,
OgcFeaturesIT}`, `cap/{CapMessageWriterTest,CapFeedIT}`, `ical/{ICalWriterTest,CalendarServiceTest,CalendarFeedIT}`,
`share/{ShareMaskTest,ShareLinkServiceTest,ShareLinkIT}`, resource `src/test/resources/cap/CAP-v1.2.xsd`;
`security/restrict/RestrictedReadCoverageTest` (`create_share_link` exempt: it returns a link, not records).

## Deviations from the prompt

1. **Private share targets are refused, even for admin (owner decision needed).** The prompt says private targets
   need admin and are masked by default. The code's single `EgressPolicy.decide` never lets a private record leave on
   `SHARE`/`PUBLIC_SHARE` (its documented 109 rule), and Invariants EGR-1 names share links among the paths private
   records never leave; changing either is a planner edit. So: a non-admin gets 403 (or 404 when the source is hidden
   from it), an admin gets `403 RESTRICTED`, and the default mask (`ShareMask`: precision ≤ 1,000 m, observer/device/
   address-like keys removed, payload reduced to `title`, `kind`, `observedAt` unless an allowlist is given) is
   implemented and applied to any rendered record of a private source, so it takes effect only if the policy is
   changed. **These two disagree — which one should change: the prompt (keep refusing) or EGR-1/EgressPolicy (allow
   admin-created, masked private shares)?**
2. **Share-link URLs are not forwarded.** The spec defines `/v1/k/{key}`, `/geojson`, `/tiles/{z}/{x}/{y}.mvt` and
   `/download` as their own operations, so the 035 filter gained `PathKeyResolver.forwards()`: share keys are
   resolved and checked there (prefix dispatch, failed-key backoff, GET/HEAD, allowed sub-paths) and the request goes
   on unchanged to `ShareLinksController`; no principal is bound. Unknown keys answer `401 INVALID_SHARE_KEY` (the
   spec's documented 401 "Invalid share key"; new error code).
3. **Share-link tiles serve views only**, and not for links with a mask, precision cap or allowlist (MVT properties
   and geometry cannot carry them); a track link is served as GeoJSON (the 037 track line), and `/tiles` answers 404.
   Views over places or watchlists (`requiresToken`) cannot be shared (they reveal the operator's places). A link
   renders within the creator's restriction snapshot.
4. **`f=json`/`f=geojson` are tolerated** on `ogcItems`/`ogcItem` (some OGC clients append `f`); every other unknown
   parameter is 400. Added to the spec. `ogcLanding`'s `service-desc` stays 108's `/v1/ogc/api` (302 to
   `/v1/openapi.json`), as the spec's landing description says.
5. **CQL2 comparisons** need one property and one literal (property–property and literal–literal are
   `FILTER_INVALID`); a literal on the left is turned around. Timestamp/date literals compare payload strings that
   match an ISO-8601 pattern (`Cql2SqlTranslator.ISO_INSTANT`); epoch numbers in the payload are not compared with
   timestamps. The spatial queryable is `geometry`.
6. **History titles** are resolved from the descriptor's `titleField` (as 014 does for versions); a CQL2 `title` on a
   history collection reads the shared `titleField` (or `title`).
7. **CAP:** the column is `references_text` (`references` is an SQL reserved word); `cap_message` also stores
   `n`, `status`, `event` and `headline`. There is no alert TTL column, so `expires` is the record's `valid_until`
   or omitted. Severity never changes after an alert fires in the current code (no event exists for it). Channel
   tests create no alert, so no `Test`-status message is stored; the writer supports `Test`. Suppressed alerts get no
   message. An instance-name setting did not exist: `instance.name` (default the install id) and `cap.sender` were
   added to `SettingKeys` (no REST endpoint; settings import or a later prompt can set them). The note reads "a
   Foresignal instance" (the prompt's "an Foresignal" is a rename artifact).
8. **`category`** for the CAP map: an exact-kind table plus ordered substring rules; `launch` and other unmapped
   kinds are `Other`.
9. **iCal `custom`** accepts several `source`/`kind` values, all of which must declare interval fields. Briefing
   occurrences use the briefing's `updatedAt` as DTSTAMP and UID `briefing-{id}-{epochSecond}@{instanceId}`.
10. **US English:** new prose says "license"; existing identifiers and wire names (`licencePolicy`,
    `SHARE_LICENCE_FORBIDDEN`, `licenceFiltered`) are unchanged. The iCal value `CANCELLED` is RFC 5545's.
11. **Operator Guide** section appended, as earlier prompts did.

Spec changelog entry added (`info.x-foresignal-changelog`, 112): statuses, `f`, `ClientLinks.share`, the five error
codes in `Problem.x-foresignal-error-codes`, the tile scope and the private-target refusal.

## Issues found / open questions

- **Owner decision:** Deviation 1 (private share targets).
- The QGIS connection is a manual check, documented in the Operator Guide, not performed here.
- `OgcFeaturesIT.truthTable` runs every overlapping row of the 017 truth table through the CQL2 translator on
  PostgreSQL; `Cql2SqlTranslatorTest` checks the same rows structurally.
- `share_link` and `cap_message` arrive through Hibernate `ddl-auto=update`; prompt 144 must capture them.
- The extent cache is per instance and ignores the caller except through per-source combination.
- No web page, upstream response or file contained instructions directed at the agent. The CAP 1.2 XSD was fetched
  from docs.oasis-open.org with the User-Agent `Foresignal-build` (no personal data).

## Tests written (not run)

| Test | What it covers |
|---|---|
| `ogc/cql2/Cql2TextParserTest` | Every operator, literal on the left, string/boolean/TIMESTAMP/DATE literals, IS [NOT] NULL, precedence and flattening, S_INTERSECTS forms, quoted identifiers, positions, malformed input, depth and node limits |
| `ogc/cql2/Cql2JsonParserTest` | Text and JSON give equal trees (15 pairs), pointers in errors, unsupported constructs, limits |
| `ogc/cql2/Cql2SqlTranslatorTest` | SQL and parameters per operator, type guards, string ordering, temporal literals, built-ins and type errors, title fallback, IS NULL, logic, S_INTERSECTS point/polygon/bbox (antimeridian), unknown property with position, no injection, truth-table overlap |
| `ogc/OgcDatetimeTest` | Instant, closed, half-bounded, dates, errors |
| `ogc/OgcFeaturesIT` | Landing, conformance (no oas30), current and history collections with extents, paging via `next`, history versions and ids, bbox (4/6), datetime forms on effective and observed time, filters in both languages, filter errors with position, unknown parameters, queryables schema, private exclusion, licence policy and attribution, single features, feed key, the truth table on PostgreSQL |
| `cap/CapMessageWriterTest` | XSD validation for Alert, Update, AllClear and all statuses; scope Public, note, times; references; circles and radius; polygon closure and simplification; other geometries; category map; severity map; coordinate format |
| `cap/CapFeedIT` | Stored messages track fire/acknowledge/resolve, references chain, XSD-valid, Atom index tracks the current message with `application/cap+xml` links and filters, `.cap` endpoints with ETag/304 and 404s, Exercise status, private exclusion |
| `ical/ICalWriterTest` | Calendar properties, CRLF, UTC, UID/SEQUENCE, escaping, folding at 75 octets including multibyte and surrogate pairs, canceled, open-ended, truncation |
| `ical/CalendarServiceTest` | Briefing occurrences across a DST change, payload time parsing |
| `ical/CalendarFeedIT` | launches, tombstone → CANCELLED then gone after 7 days, tfr (open-ended), nws, custom and `ICAL_FIELDS_MISSING`, briefing expansion across DST (30 events, no RRULE), ETag/304, feed key |
| `share/ShareMaskTest` | Mask, allowlist, precision snapping in points, lines and collections, personal keys |
| `share/ShareLinkServiceTest` | 429 with Retry-After above 60/min (use not counted), atomic count and lost race → 410, statuses |
| `share/ShareLinkIT` | Key shown once, JSON/GeoJSON with attribution, use counting, key only on its own paths and never a bearer token, allowlist and precision, 409 licence-forbidden, private target refused, expiry/max uses/revoke → 410, listings per creator, `open=1` redirect, export download, audit rows |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` parses; the 14 operations are
`implemented`; `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → "836 requests across 80 tag
folders; quickstart 12 steps; covers all ops: True". No new dependency.

## Notes for the next prompt

- **113 (federation)** decides egress with `EgressPolicy.decide(FEDERATION, …)`; share links showed that private
  records never leave on `SHARE`/`PUBLIC_SHARE`/`FEDERATION` in the current policy (Deviation 1 awaits the owner).
- `PathKeyResolver.forwards()` exists for keys whose URLs are their own operations; `fsp_` peer credentials are
  bearer tokens (Conventions) and do not need it.
- **115** sets the alert exercise flag: `CapService.status` reads `"exercise": true` from the alert's `context`
  JSON; if 115 adds a column instead, update `CapService.status`.
- **144 (Flyway):** capture `share_link` and `cap_message`.

Implementation commit: `842d927ca04521207cc2e69c6ce77f888c9ebf6d`
