# 067-CR-Hazard-Adapters

| | |
|---|---|
| Prompt | 067-Hazard-Adapters.md |
| Status | Complete |
| Implementation commit | f5efb29a1ccf1114343912273ecadadb1ef92d04 |
| Date | 2026-10-13 |

## Summary

Foresignal now has three hazard sources, `nhc-cyclones`, `gdacs` and `tsunami-warnings`, plus shared CAP 1.2
support in `ai.foresignal.source.formats.cap`. That package holds the only CAP parser in the codebase. 024's `cap`
ingest format and `tsunami-warnings` both use it, and 089's CAP family is documented to.

**What was verified live** (2026-10-13, no key needed):
- `https://www.nhc.noaa.gov/CurrentStorms.json` answered with `etag`, `last-modified` and `cache-control: max-age=300`
  (CloudFront). It held one in-season storm, Hurricane Simon (`ep202026`, `EP5`). `intensity` (`"80"`) and
  `pressure` (`"977"`) were strings, the coordinate keys were camelCase `latitudeNumeric`/`longitudeNumeric`, and
  `advNum` was a string (`"024"`, `"024a"`).
- The NHC JSON reference PDF says "movementSpeed … (miles per hour)" and lists the classifications TD, STD, TS, HU,
  STS, PTC ("Post-tropical Cyclone / Remnants"), TY and PC ("Potential Tropical Cyclone"). Its example links use
  `www.hurricanes.gov`.
- GDACS:
  - `…/geteventlist/MAP` with no `eventtype` answered **400** `{"message":"Eventtype is required."}`.
  - EQ returned 22 features (11 events) and TC returned 333 (5 events, with track lines and wind buffers).
  - FL, VO, DR and WF answered 200. FL lists `Point_Global`, `Point_Affected` and `Point_Centroid` points for one
    event. DR was 3.1 MB.
  - Times carry no offset (`2026-10-09T18:22:15`). No `ETag` or `Last-Modified` was sent.
  - The Swagger lists `/api/Events/geteventlist/map` with an `eventtype` query parameter not marked required, but
    the server requires it.
  - The March 2025 terms PDF contains no licence. It says EQ, tsunami and TC notifications "are automatic, produced
    by algorithms and not reviewed by human experts", and that GDACS is "not meant to substitute nor to override any
    official information or alert".
- tsunami.gov: `PAAQCAP.xml` and `PHEBCAP.xml` each held one signed CAP 1.2 `alert`: the latest message, an
  information statement for the M 6.8 Mid-Atlantic Ridge earthquake. They sent `Last-Modified` and no `ETag`. The
  Atom feeds' single entry links `rel="related" type="application/cap+xml"` to
  `https://www.tsunami.gov/events/{center}/2026/10/13/{incident}/1/{product}/{center}CAP.xml`.

**1. Shared CAP support** (`ai.foresignal.source.formats.cap`):
- **`CapParser.parse(InputStream, CapParseOptions)`** (also `parse(byte[])`):
  - Hardened StAX through the JDK's own factory (`XMLInputFactory.newDefaultFactory()`): DTDs off, external entities
    off, entity replacement off, `ACCESS_EXTERNAL_DTD` empty, and a resolver that refuses everything. A DTD or
    entity-reference event is refused.
  - Limits: a 2 MB cap (`CappedInputStream`), an element cap (50,000 by default) and a depth cap of 32.
  - Versions: CAP 1.2, or 1.1 with `capVersion` recorded; anything else is an `UpstreamFormatException`, including an
    un-namespaced `alert`. Only elements in the root's namespace are read, so tsunami.gov's enveloped XML Signature
    is skipped.
  - A missing `identifier, sender, sent, status, msgType, scope` is refused. Times are `OffsetDateTime` (`-00:00`
    reads as UTC).
  - `references` triples are parsed (an unreadable `sent` becomes null; a triple without an identifier is skipped).
  - `rootElementName(byte[])` and `secureInputFactory()` are public helpers.
  - Verified with a scratch JDK program, which ran no project code: predefined entities and character references
    still expand as text (`A&gt;B&amp;C` reads `A>B&C`), and an XXE DOCTYPE fails with "The entity "x" was
    referenced, but not declared".
- **`CapAlert`**: immutable records for the full model of the prompt (`alert`, `Reference`, `NamedValue`, `Info`,
  `Resource`, `Area`). An absent `language` defaults to `en-US`.
- **`CapGeometry`**:
  - `lat,lon` pairs become lon/lat, open rings are closed, and the shapes become a MultiPolygon.
  - A circle becomes a 64-segment great-circle polygon; radius 0 is a Point (one) or a MultiPoint (several). When
    both exist, polygons win.
  - A geocode-only area gives null geometry.
  - Shapes are not repaired here. The normalizer's `GeometryFixer` repairs them and sets `GEOMETRY_FIXED`.
- **`CapThreadResolver`**:
  - The thread key is the custom key; else the remembered thread of the earliest reference; else that reference's
    `{sender}:{identifier}`; else the message's own key.
  - The map is stored in adapter state `cap.threads`, one `identifier TAB threadKey TAB epochSecond` line per entry,
    with a 14-day TTL and at most 20,000 entries, least recently used first out. Referenced identifiers are
    remembered too.
- **`CapRecordMapper.toRecord(CapAlert[, threadKey], CapMappingProfile)`**:
  - Time base: `observedAt = sent`, `eventTime = onset`, `validFrom = onset ?? effective` (left out when it would fall
    after `validUntil`), `validUntil = expires`.
  - `state` is `cancelled` (Cancel, or the profile's rule), `updated` (Update) or `active`.
  - A cancellation is a version with `validUntil = info.expires ?? sent`. The mapper never produces a tombstone.
  - Payload: the prompt's keys. Text is capped at 4 KB UTF-8 with `…`. `geocodes` and allowlisted `parameters` are
    `{valueName: [values]}`; `references` is a count.
  - The info block is chosen by language (exact match, then primary subtag), else the first.
  - `EMBED_FIELDS = [headline, description, instruction]`.
- **`CapMappingProfile`**: the kind, languages, parameter allowlist, text cap, `CancellationRule` and
  `RecordExtender`.
- **024's `CapFormat`** now delegates to `CapParser` and `CapRecordMapper`. A parse failure is still a 400
  (`InvalidInputException`), and its identity and tombstone rules are unchanged (Deviation 2).
- **`validFrom` hook:** 008 already added `validFrom` to `NormalizedRecord`, the head and the hash inputs. `Record`
  and `Change` in `docs/OpenAPI.json` already carry it, and `RecordMapper` returns it. Nothing was added.

**2. `nhc-cyclones`** (`ai.foresignal.source.adapters.nhc`):
- **Fetch:** `getConditional(…/CurrentStorms.json, "storms")`, and a 304 is `unchanged()`. A truncated or rejected
  body forgets the validators.
- **Parser** (`NhcCurrentStormsParser`, a port of GEV `parseCycloneStatus`):
  - The whole document is rejected when: there are more than 32 storms, an id fails `^(al|ep|cp)\d{6}$`, an id
    repeats, or a `lastUpdate` is unreadable.
  - Numbers are read from strings or numbers, within ranges.
  - Strings are sanitized: control characters and `<>` removed, capped at 80 characters.
  - `movementSpeed` mph is converted to `movementSpeedKt` (× 0.868976, 1 dp).
  - `advisoryNumber` comes from `forecastAdvisory.advNum`, without leading zeros and upper-cased.
  - Links are kept only as `https://www.nhc.noaa.gov/…` with no credentials, port, query or fragment.
- **Record:** `cyclone`, Point, `observedAt = lastUpdate`, and the payload of the prompt.
- **Tombstones:** `LiveIdSet` scope `active` (`live:active`, `gone:active`): a storm absent from a later successful
  response gets an explicit tombstone at the run time.
- **Descriptor:** 10 min (min 5), 2/min and 300/day, STANDARD, ALL, no TTL, `zeroResultsNormal`, `US-PD`, retention
  1825 d.
- The Central Pacific caveat is in the Javadoc, the descriptor and the catalog.

**3. `gdacs`** (`ai.foresignal.source.adapters.gdacs`):
- **Fetch:** one `GET …/MAP?eventtype={T}` per configured type (`eventTypes`, allowed `EQ, TC, FL, VO, DR, WF`,
  default all), sequentially and in order. A 400 propagates as `UpstreamHttpException`. `checkParams` refuses an
  explicitly empty list.
- **Parser** (`GdacsEventParser`):
  - Only Point features are read. Per event the newest episode wins, and within it the `Point_Centroid` point.
  - externalId `{eventtype}:{eventid}`, `eventTime = fromdate`, `observedAt = datemodified` (read as UTC).
  - The payload of the prompt, `automated: true` included. Green alerts are kept.
- **Descriptor:**
  - 15 min (min 10), 6/min and 700/day, STANDARD, ALL, `recordTtl` 48 h anchored on receipt, retention 1825 d.
  - Embed `name, description`; tiles `alertLevel, eventType, alertScore`; rank `alertScore`.
  - The prompt's licence string and the March 2025 terms URL. The disclaimer is in the description and the Agent
    Guide.

**4. `tsunami-warnings`** (`ai.foresignal.source.adapters.tsunami`):
- **Fetch:**
  - `PAAQCAP.xml`, then `PHEBCAP.xml`, with `getConditional` (`cap:{center}`). Both 304s give `unchanged()`;
    otherwise the result is `FetchResult.of` (never complete).
  - If a CAP URL returns anything but an `alert` root, the adapter reads the center's Atom feed (`TsunamiAtom`,
    hardened StAX) and follows the first related CAP link allowed by `allowedCapLink`. Only
    `{base}/events/{center}/…/…CAP.xml` passes: no `..`, query, fragment, `@` or backslash.
  - A disallowed link is a format error. Format errors forget that center's validators.
- **Threads:** `{center}:{incidents}` (the first token), else `{center}:{eventId}` from `PAAQ-<seq>-<eventId>`, else
  the resolver.
- **Profile:**
  - Languages `en-US, en`.
  - The prompt's parameter allowlist.
  - The cancellation rule: Cancel, `(?i)cancel` in `event`, or a VTEC action `CAN`.
  - The extender adds `center`, `messageType` (Warning, Advisory, Watch, Information Statement, Threat; absent for a
    cancellation) and `epicenter {lat, lon}` from `EventLatLon`. Without area geometry, the epicenter is the
    geometry.
- **Descriptor:** 2 min (min 1), 4/min and 3,000/day, STANDARD, ALL, no TTL, `zeroResultsNormal`, `US-PD`, retention
  1825 d, embed `headline, description, instruction`.
- Nothing closes a thread on absence.

**5. Fixtures** (each directory has a README with dates and URLs):
- `nhc-cyclones`: the captured `CurrentStorms.json`, `empty.json`, `string-numbers.json`, `null-products.json`,
  `invalid-id.json`.
- `gdacs`: `MAP-EQ.json` and `MAP-TC.json` (captured, trimmed to 10 features), `MAP-EQ-next-episode.json`, and the
  captured `400-no-eventtype.json`.
- `tsunami-warnings`: the four captured CAP and Atom documents, the synthetic thread (`thread-1-warning.xml`,
  `thread-2-update.xml`, `thread-3-cancellation.xml`) and `unrelated-next-event.xml`.
- `cap` (the conformance set): `polygon.xml`, `circle.xml`, `geocode-only.xml`, `multi-language.xml`,
  `references.xml`, `cancel-no-info.xml`, `xxe.xml`, `billion-laughs.xml`, `cap11.xml`, `cap10.xml`,
  `missing-sender.xml`.

**6. Docs:**
- `docs/Source-Catalog.md`: the `nhc-cyclones` and `gdacs` rows were rewritten and a `tsunami-warnings` row added.
- `docs/Adapter-Guide.md` §18 "Alert sources: time mapping, threads, cancellation (prompt 067)".
- `docs/Agent-Guide.md`: a new section "Hazards: cyclones, disasters and tsunami warnings (prompt 067)", which
  carries the GDACS disclaimer.
- `THIRD_PARTY_NOTICES.md`: a GEV row for `NhcCurrentStormsParser`.

## Files created / modified

- **Created, main:**
  - `source/formats/cap/` `CapParseOptions`, `CapGeometry`, `CapThreadResolver`, `CapMappingProfile`,
    `CapRecordMapper`;
  - `source/adapters/nhc/` (`NhcCyclonesAdapter`, `NhcCurrentStormsParser`, `package-info`);
  - `source/adapters/gdacs/` (`GdacsAdapter`, `GdacsEventParser`, `package-info`);
  - `source/adapters/tsunami/` (`TsunamiWarningsAdapter`, `TsunamiAtom`, `package-info`).
- **Rewritten, main:** `source/formats/cap/CapParser.java` (StAX), `CapAlert.java` (full model), `package-info.java`.
- **Modified, main:** `inbound/format/CapFormat.java` (delegation).
- **Created, test:**
  - `source/formats/cap/` `CapGeometryTest`, `CapRecordMapperTest`, `CapThreadResolverTest`;
  - `inbound/format/CapIngestFormatTest`;
  - `source/adapters/nhc/` `NhcCurrentStormsParserTest`, `NhcAdapterTest`;
  - `source/adapters/gdacs/` `GdacsEventParserTest`, `GdacsAdapterTest`;
  - `source/adapters/tsunami/TsunamiAdapterTest`;
  - `source/adapters/HazardIngestIT`;
  - the fixture directories `cap`, `gdacs`, `nhc-cyclones`, `tsunami-warnings`.
- **Rewritten, test:** `source/formats/cap/CapParserTest` (the conformance set; its 024 format cases moved to
  `CapIngestFormatTest`).
- **Modified, test:** `retention/FamilyDefaultsTest` (three `event` entries) and `testsupport/weather/WeatherIngestSupport`
  (three base-URL properties and the backoff list).
- **Docs:** `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Agent-Guide.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **024's parser was replaced, not extended.** 024 had a DOM `CapParser` (instance methods, `Instant` times,
   circles turned into polygons inside the model).
   - It is now the static StAX parser the prompt specifies, with the model the prompt lists.
   - Geometry moved to `CapGeometry`.
   - The parse failure type is `UpstreamFormatException`; `CapFormat` translates it to `InvalidInputException`, so
     the push API still answers 400.
2. **`CapFormat` keeps 024's identity and tombstone rules.** The record id is the CAP `identifier`. `Update`
   tombstones the referenced messages, and `Cancel` only tombstones them and creates no record. A push source has no
   adapter state for threads, and the prompt says to keep the format's API.
   - What changed for push callers: an un-namespaced `alert` and a document missing a required element are now
     400s (the old `noNamespace` test was inverted).
   - The record now carries `validFrom` and `state`.
   - Payload changes: `geocodes` became `{valueName: [values]}` instead of a list of `{valueName, value}`; text is
     capped at 4 KB; `sent`, `scope` and `references` were added.
   - Owner question 3.
3. **`nws-alerts` was not changed.** It still sets `validFrom = onset`, not `onset ?? effective`, following the
   047 report (Issue 3) and the instruction for this run to align with what exists. Owner question 1.
4. **NHC tombstone state** is `LiveIdSet`'s `live:active` (plus `gone:active`, which retries a lost tombstone once),
   not a plain key `active`.
5. **NHC all-or-nothing** also covers an unreadable `lastUpdate`, because it is the record's `observedAt` and a storm
   silently dropped would be tombstoned. GEV's freshness window (more than 5 minutes ahead or 12 hours old rejected)
   was not ported: the normalizer clamps future times, and an old `lastUpdate` is legitimate. Other unreadable fields
   are left out. `intensityKt`, `pressureMb` and `movementDirDeg` are integers.
6. **Map icons:** the sprite has no `cyclone` icon, so `nhc-cyclones` uses `storm`. `gdacs` uses `danger` and
   `tsunami-warnings` uses `water`.
7. **GDACS:**
   - One record per event from the point features, with the centroid and newest episode preferred. The MAP response
     has several features per event, and the prompt says "geometry from the feature (Point)".
   - An event without `datemodified` is skipped and counted.
   - An unknown type in `eventTypes` fails the run with `AdapterException`; the declared allowed values normally stop
     it at the API.
   - The descriptor's disclaimer says all alerts are automatic and unreviewed, as the prompt says. The terms say this
     of EQ, tsunami and TC notifications specifically.
8. **Tsunami:**
   - Messages whose `status` is not `Actual`, and `Ack`/`Error` messages, are not ingested; the prompt is silent.
   - Atom links must stay under the feed's own center.
   - The thread key takes the first `incidents` token.
   - `messageType` is absent for a cancellation, because "Cancellation" is not in the prompt's list.
   - The fallback runs only when the CAP URL's root element is not `alert`. The live documents were single alerts,
     so the fallback is exercised only by the WireMock test.
9. **The cancellation thread is synthetic.** No live cancellation existed on 2026-10-13. The three messages follow
   the captured PAAQ layout and the prompt's verified cancellation shape.
10. **The 3 MB oversize alert is generated in `CapParserTest`** rather than committed. `cap10.xml`,
    `billion-laughs.xml` and `missing-sender.xml` were added to the conformance set.
11. **Mapper details the prompt leaves open:**
    - `validFrom` is dropped when it would be after `validUntil`; the normalizer would otherwise reject the record as
      `VALID_RANGE_INVERTED`, as 047 found for NWS.
    - The text cap also applies to `senderName`, `event`, `web` and `areaDesc`.
    - Parameter values are capped at 1 KB.
12. **`CapGeometry` does not call `GeometryFixer` itself.** The normalizer already runs it on every geometry and
    flags the repair as `GEOMETRY_FIXED`; repairing here would hide that flag.
13. **No OpenAPI change.** `validFrom` is on `Record` and `Change`, and the `cyclone`, `disaster` and `tsunami-warning`
    kinds already list exactly the payload fields built here. Postman was not regenerated, and `lintOpenApi` was not
    needed.
14. **Retention 1825 d** (`event`) for all three: the prompt names none, and Conventions lists cyclones, disasters
    and CAP alerts as `event`.
15. **`HazardIngestIT`** lives in `ai.foresignal.source.adapters` and shares `WeatherIngestSupport`'s context (no
    new Spring context). `docs/Source-Catalog.html` was not regenerated; the prompt names only the Markdown.

## Issues found / open questions

1. **Two docs conflict with what exists.**
   - The acceptance criterion says "No alert source closes an alert because it is missing from a feed (NHC is the
     documented exception)". But 047's `nws-alerts` tombstones alerts that leave `/alerts/active`, which 047 treats
     as a complete set.
   - 067 does not ask for NWS to change, and changing it would reverse a 047 decision, so it was left as built. The
     new Adapter Guide §18 names NHC as the exception and records the NWS mapping as it stands.
2. **`FamilyDefaultsTest` is still red for 062–064** (carried from 065 and 066). The three new adapters were added as
   `event`.
3. **Volume:** NHC holds at most 32 storms, GDACS about 50 events across six requests, and tsunami.gov two messages:
   negligible.
4. **Instructions in data:** none. The NHC, GDACS and tsunami.gov responses, the NHC reference PDF, the GDACS terms PDF
   and the Swagger contained no instructions to an agent.

**Owner questions (non-blocking):**
1. Should `nws-alerts` use `validFrom = onset ?? effective` as 067 says, or keep `onset` (047 Issue 3, still open)?
2. Should `nws-alerts` keep tombstoning alerts that leave its complete active set? If so, §18 and the acceptance
   criterion should name it as a second exception. If not, NWS should stop tombstoning and rely on `validUntil`.
3. Should 024's push `cap` format keep tombstoning referenced alerts, or move to cancellation-as-version? The latter
   would need a thread store for push sources.
4. Should tsunami Test and Exercise messages be ingested (they are skipped now)?

## Tests written (not run)

- **`CapParserTest`:**
  - the header, info, parameters and areas of the polygon alert;
  - circles and geocodes kept raw;
  - multi-language order;
  - `references` parsing (two triples, the malformed one skipped, an unreadable `sent` becomes null);
  - Cancel without info;
  - XXE and billion laughs refused (also in `rootElementName`);
  - the 3 MB alert refused at 2 MB, plus smaller byte and element caps;
  - 1.1 accepted with its version; 1.0, un-namespaced and non-`alert` roots refused;
  - a missing `sender`, a bad `sent`, empty and malformed input refused;
  - the captured PAAQ and PHEB alerts (signature skipped, blank headline null, `NWSUGC` with `>`).
- **`CapGeometryTest`:**
  - the lat/lon swap and SRID;
  - ring closure;
  - a MultiPolygon from two areas;
  - a 10 km circle: 65 points, centroid, area within 1% of πr² (local equirectangular);
  - radius 0 as a Point, MultiPoint, and polygons winning;
  - geocode-only and null info giving null;
  - malformed input.
- **`CapRecordMapperTest`:**
  - the time base and every payload key;
  - `validFrom` from onset, then effective, then null, and the inverted case;
  - no `expires` means no `validUntil`;
  - Cancel without info is a cancelled version with `validUntil = sent`;
  - Cancel with info, `updated`, and a profile rule;
  - language selection (none, exact, primary subtag, fallback);
  - the 4 KB and 64 B text caps;
  - the parameter allowlist;
  - the extender;
  - `EMBED_FIELDS`.
- **`CapThreadResolverTest`:** the reference chain; the earliest reference; the custom key wins; save and load;
  LRU eviction at the cap and TTL expiry on load; corrupt lines dropped.
- **`CapIngestFormatTest`** (024, updated):
  - an Alert record keyed by identifier, now with `validFrom` and `state`;
  - a MultiPolygon;
  - Update and Cancel tombstones;
  - `acceptNonActual`;
  - XXE, non-CAP, empty and un-namespaced documents as `InvalidInputException`.
- **`NhcCurrentStormsParserTest`:**
  - the captured file (every payload key, mph to knots, string numerics, links);
  - `string-numbers.json` (sanitizing, `cp`, `012A`, dropped links, unreadable numerics);
  - null products;
  - the empty list;
  - rejection for an invalid id (`wp`), a duplicate id, no `lastUpdate`, 33 storms, no array, non-JSON and empty
    input;
  - the advisory and link helpers.
- **`NhcAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient`):
  - complete storms and the stored ETag;
  - a 304 is not modified;
  - absent storms tombstoned at the run time;
  - an empty file after storms;
  - an empty file from the start;
  - a rejected document tombstones nothing and forgets the ETag;
  - a 500 tombstones nothing;
  - the descriptor.
- **`GdacsEventParserTest`:**
  - EQ (7 events from 10 features, Green kept, every payload key of the Orange event, the empty fields of
    Macquarie);
  - TC (5 events, lines skipped);
  - type filtering, episode and centroid preference;
  - time, boolean and link helpers;
  - a missing `datemodified` and non-FeatureCollection bodies.
- **`GdacsAdapterTest`** (WireMock):
  - one request per configured type, in order;
  - all six by default;
  - a 400 surfaced after one request;
  - Green kept;
  - one externalId across episodes;
  - the params checks;
  - the descriptor.
- **`TsunamiAdapterTest`** (WireMock):
  - two CAP fetches and the captured mapping (thread keys, time base, epicenter, parameters, `validFrom` from onset);
  - both 304 is not modified;
  - the Atom fallback following the allowed link;
  - the allowlist (a captured production link refused against the stub, validators forgotten, path cases,
    production `https` only);
  - the cancellation thread as three versions of `PAAQ:tsthr01`: `active`, `updated`, `cancelled`, with
    `validUntil` 22:00;
  - an unrelated next event giving a new record and no tombstone;
  - Test messages skipped;
  - a malformed document;
  - the mapping rules;
  - the descriptor.
- **`HazardIngestIT`** (`WeatherIngestSupport`, real executor and writer):
  - the tsunami thread persists FIRST, SIGNIFICANT, SIGNIFICANT, with head `state` `cancelled`, not tombstoned, live
    `valid_until` at the cancellation's `expires` and head `valid_from` at its onset;
  - `validFrom` stored on the head and returned by `GET /v1/records`;
  - a cyclone that leaves the set is a TOMBSTONE version.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (an unchecked-operations note from
  `TsunamiAdapterTest`).
- No OpenAPI change, so `lintOpenApi` and the Postman generator were not run.
- No guard was run: this is a feature prompt and runs no tests.

## Notes for the next prompt

1. **068 and later adapters:**
   - feeds that are a complete set may tombstone through `LiveIdSet`, as `NhcCyclonesAdapter` does, only after a full
     and parsed response;
   - feeds that are not complete sets must let records expire (`recordTtl` or `validUntil`);
   - `CapParser.secureInputFactory()` is the hardened StAX factory for any XML upstream.
2. **075 (`nhc-cyclone-cones`):** `nhc-cyclones` does not parse the GIS links. Storm ids are the `externalId`s
   (`ep202026`).
3. **089 (CAP family):**
   - use `CapParser`, `CapThreadResolver.load/resolve/save` and `CapRecordMapper` with a `CapMappingProfile` per feed;
   - geocode-only areas have null geometry and `geocodes` `{valueName: [values]}` for boundary resolution;
   - never close on absence.
4. **097 checkpoint:**
   - first run of all the 067 tests;
   - `CapIngestFormatTest` replaces the format cases of the old `CapParserTest`;
   - `HazardIngestIT` shares `WeatherIngestSupport`'s context (three new base-URL properties);
   - `FamilyDefaultsTest` still fails on 062–064 only.
5. **101 and later (intel):** GDACS records are automatic and unreviewed (`automated: true`), and a GDACS TC and an
   NHC storm can describe the same cyclone: count evidence groups, not records.
6. **143:** no key is needed. Optionally recapture the fixtures per the READMEs; a live tsunami cancellation would
   replace the synthetic thread.
7. **144 (Flyway):** no schema change. Adapter state:
   - `nhc-cyclones`: `live:active`, `gone:active`, `etag:storms`, `lastmod:storms`;
   - `tsunami-warnings`: `cap.threads`, and `lastmod:cap:{PAAQ|PHEB}` and `lastmod:atom:{PAAQ|PHEB}` (plus `etag:…`
     if the server ever sends one);
   - `gdacs`: none.
8. **Owner decisions pending:** the four questions above, plus those carried from 012–066.
