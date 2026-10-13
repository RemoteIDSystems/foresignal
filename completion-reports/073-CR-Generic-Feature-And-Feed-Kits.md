# 073-CR-Generic-Feature-And-Feed-Kits

| | |
|---|---|
| Prompt | 073-Generic-Feature-And-Feed-Kits.md |
| Status | Complete |
| Implementation commit | 4003ecb4cc68bab27bd643b7926c9156bb80eeed |
| Date | 2026-10-13 |

## Summary

Two protocol-level kits are in. A "feature service" or "feed" source is now a reviewed resource file instead of a
hand-written adapter. All kit sources ship disabled.

**1. Loading and validation** (`ai.foresignal.source.kit`):
- **`KitSpecs`** (`@Component`) loads `classpath:kits/features/*.json` and `classpath:kits/feeds/*.json`.
  - Each file is a JSON array bound by `KitSpecLoader`, which refuses unknown properties.
  - Both kits are validated together. Any problem stops startup, and every problem is listed with its file, index,
    id and field path.
- **`KitUrls`** holds the URL rules (https, no user info, no credential-looking query parameter or token-like value)
  and the test rebasing `foresignal.test.kits.base-url` → `/{host}{path}`.
- **`KitOverlay`** (operator overlay, off by default):
  - with `foresignal.kits.operator-specs.enabled=true` it also loads `${foresignal.data-dir}/kits/{features,feeds}/*.json`;
  - overlay ids must be `op-…`, and every URL must pass `OutboundUrlPolicy` (`PUBLIC_ONLY`);
  - without the policy the overlay is refused and the reason logged; links and directories are refused;
  - specs are never accepted over the API.

**2. Feature kit** (`ai.foresignal.source.kit.feature`). All clients go through `ctx.http()` only, are stateless
(state lives in `AdapterStateStore`) and return a `FeatureFetch` (an `Iterable` of GeoJSON Feature trees). The caps
are `maxPages` (default 10), `maxFeatures` (default and ceiling 50,000) and the descriptor's `maxResponseBytes`.
- **`ArcGisQueryClient`:**
  - **Metadata:** the layer document `{layer}?f=json` is cached under `arcgis:meta` (`maxRecordCount`,
    `supportsPagination`, `objectIdField`, fields, `editingInfo.dataLastEditDate` else `lastEditDate`).
  - **Query:** `where`, `outFields`, `returnGeometry=true`, `outSR=4326`, `f=geojson`, `resultOffset`,
    `resultRecordCount=min(pageSize, maxRecordCount)` and `orderByFields=<objectIdField>`, plus
    `maxAllowableOffset` and `geometryPrecision` when the spec sets them.
  - **Paging:** pages while `exceededTransferLimit` is true (at the top level or under `properties`) and the page is
    non-empty. A layer without pagination support gets one query.
  - **Short-circuit:** when the edit date equals the one stored after the last complete fetch (`arcgis:lastEdit`),
    the fetch is `unchanged()` and no query is sent.
  - **Errors:** an ArcGIS `{"error": …}` body, a 3xx, another non-200 status, truncation or non-JSON is an
    `UpstreamFormatException`.
  - **Esri JSON:** `format: esrijson` uses `f=json` and `EsriJsonConverter` (points, multipoints, paths, rings;
    clockwise = outer, holes joined by containment, output rings in RFC 7946 orientation).
- **`OgcFeaturesClient`:**
  - requests `{base}/collections/{id}/items?f=json&limit=…[&bbox][&datetime][&filters]` with
    `Accept: application/geo+json, application/json;q=0.9`;
  - follows `links[rel=next]` whatever its query shape (resolved, JSON types only, same scheme, host and port);
  - accepts `geo+json`, `json` or any `+json` type;
  - stops on a missing `next` or an empty page, and ignores `numberMatched`.
- **`WfsJsonClient`:**
  - requests GetFeature with `service=WFS&version=2.0.0&request=GetFeature&typeNames&outputFormat=application/json`,
    `count`, `startIndex` and `srsName=urn:ogc:def:crs:EPSG::4326`;
  - pages while `numberReturned == count`;
  - `axisOrder` is `lonlat` by default, or `latlon`. With a spec `bbox`, when the first 20 points fit only when
    swapped, every coordinate is swapped and one warning is logged per source.
- **`FeatureGeometry`:** a geometry over 9,500 vertices is simplified with `TopologyPreservingSimplifier` from 0.0005°,
  doubling, until it fits. The payload then gets `geometrySimplified: true` and `simplifyToleranceDeg`.
- **`FeatureMapper`** (pure):
  - only the allowlisted fields are kept, renamed and value-mapped (by text), and the spec's constants are added;
  - typing: numbers are coerced from text, text is trimmed, booleans accept `true/1/yes/y/t` and their opposites;
  - timestamps: `auto` (numbers are epoch ms; text is ISO-8601 or `yyyy-MM-dd HH:mm:ss`), `epoch-ms`, `epoch-s`,
    `iso`, or a pattern, read in the spec's zone; they are stored as ISO-8601 instants;
  - nulls, blanks, `-999`, `-9999` and year-1 dates are removed;
  - `externalId` comes from `idField`; a feature without one is rejected (`missing-id`). A feature without its
    `observedAt` field is rejected too (`missing-observed-at`), never given another field.
- **Spec and descriptor:** `FeatureSourceSpec` holds the prompt's field list (see Deviations for `license`, `format`,
  `retention`, `maxResponseBytes`, `paging.maxFeatures` and `time.zone`). `FeatureDescriptors` gives:
  - POLLING, redirects refused, a 60 s timeout;
  - STANDARD, `IngestPolicy.All` (its keyframe derived from the TTL), `observedAtMode` UPSTREAM when
    `time.observedAt` names a field, else RECEIVED;
  - `fieldSpecs` for every target, constant and simplification flag.
- **`FeatureSpecValidator`:** the prompt's rules plus protocol, query, limit, header, credential and text checks (see
  the class Javadoc).
- **`KitFeatureAdapter`:**
  - mapper rejections are counted in `foresignal.kit.features.rejected{source,reason}`, and a duplicate id keeps
    its first feature;
  - `AbsenceTombstoner` runs only after a complete, non-empty fetch;
  - a truncated fetch is a partial `FetchResult.of` with outcome `TRUNCATED`;
  - credentials go in the query or a header and are redacted by the client.
- **`FeatureKitFamily`:** one adapter per spec.
- **Starter spec `kits/features/wfigs.json` → `feature-wfigs-incidents`:**
  - kind `fire-incident`, id `IrwinID`, observedAt `ModifiedOnDateTime_dt` (volatile `modifiedAt`), eventTime
    `FireDiscoveryDateTime`;
  - payload `irwinId, name, incidentType, sizeAcres ([acr_us]), percentContained (%), state, county, uniqueFireId,
    discoveredAt, modifiedAt`;
  - absence tombstones on, 10 min, 12/min and 3,000/day, page 2000 × 10, public domain.
  - Re-verified 2026-10-13: `maxRecordCount` 2000, `supportsPagination` true, all three `editingInfo` dates present,
    and the eight prompt fields plus `IncidentTypeCategory` and `POOCounty`.

**3. Feed kit** (`ai.foresignal.source.kit.feed`):
- **`FeedParser`** (pure) reads RSS 2.0, RSS 1.0 (RDF), Atom 1.0 and JSON Feed 1.0/1.1.
  - **Detection:** by content type, then by the first byte, then by the root element or `version`.
  - **XML:** through `XmlTree` on `CapParser.secureInputFactory()` (no DTD, no external entities, coalescing). A
    DOCTYPE or entity reference is refused outright, and the body cap is 8 MB.
  - **Text:** CDATA, `content:encoded` and Atom `text|html|xhtml` are handled (xhtml keeps mixed-content order).
- **`FeedText`:** jsoup `Safelist.none()`. Line breaks and block ends become spaces, entities are decoded and
  whitespace collapsed. Summaries are at most 4 KB, cut on a character boundary. Links must be absolute http(s)
  without user info.
- **`FeedDates`:**
  - RFC 822, with or without a day name, two-digit years (00–49 → 20xx, 50–99 → 19xx), the North American zone
    names and numeric offsets;
  - RFC 3339;
  - zone-less ISO-8601 is read as UTC with a parser warning.
- **`FeedGeometry`:** GeoRSS Simple point, line, polygon and box ("lat lon"); GeoRSS GML Point, LineString and Polygon
  with interiors (lat-lon unless the `srsName` contains CRS84); W3C Basic Geo, direct or in `geo:Point`; and the JSON
  Feed `_geo` extension. A bad geometry is left out with a warning; the item is kept.
- **`FeedItem` ids:** the guid, Atom id or JSON id, else the first 16 hex characters of SHA-256 of link + title. Up to
  20 categories and 5 enclosures.
- **`FeedSourceSpec` and `FeedSpecValidator`:** the prompt's fields and defaults, plus `description`, `homepage`,
  `websub`, `retention` and `maxResponseBytes`. The validator checks:
  - the WebSub safety net against the TTL;
  - that `itemMapper` names a reviewed mapper (`FeedItemMappers.REVIEWED`, empty until 075; interface
    `FeedItemMapper`).
- **`FeedRecords`** (pure, shared with the WebSub path):
  - applies the category filter, then the `maxItems` cap;
  - `externalId = item.id` (ids over 256 characters become `sha256:` plus 32 hex characters);
  - `eventTime = published`; `observedAt` per spec, and an item without the named date is skipped;
  - payload `title, summary, link, published, updated, categories, authors, enclosures, feedTitle`;
  - no tombstones.
- **`FeedDescriptors`:**
  - STANDARD, ALL, TTL 30 d on receipt, retention 1825 d;
  - title `title`, search `title, summary, categories`, embed `title, summary`, tiles `title, link, published`;
  - the `information` icon.
- **`KitFeedAdapter`:**
  - conditional GET (`etag:feed`, `lastmod:feed`);
  - a 3xx, another status, truncation or a parse failure is a format error and forgets the validators;
  - skips are counted in `foresignal.kit.feed.skipped{source,reason}`;
  - the result is always partial (`FetchResult.of`), because feeds are windows.
- **`FeedKitFamily`:** one adapter per spec. It resolves the WebSub coordinator lazily, because WebSub depends on the
  registry.

**4. Bulletin pack** (`kits/feeds/bulletins.json`, all disabled): `feed-cisa-advisories` (60 min), `feed-ncsc-uk`,
`feed-nhc-atlantic`, `feed-nhc-eastpacific`, `feed-ecdc-threats` and `feed-who-news`, each with its URL and license
as in the prompt table. `observedAt` is receipt; see Issues for CISA.

**5. WebSub** (`ai.foresignal.source.kit.websub`, off by default):
- **Table `websub_subscription`:** `source_id` PK, `hub`, `topic`, `token`, `secret_enc` (nonce ‖ AES-GCM via
  `CredentialCipher`), `state` (`pending|active|lost`), `lease_expires_at`, `last_verified_at`, `last_push_at`, and
  `requested_at`. Owned by Hibernate.
- **`WebSubService`:**
  - implements `WebSubCoordinator`; with `foresignal.kits.websub.enabled=false` (a bootstrap property) every answer
    is no;
  - **subscribe:** only for `websub: true`, an https `publicBaseUrl`, a feed with a hub and a self link, and a hub
    that passes `OutboundUrlPolicy`. It sends `POST hub` through the source's budgeted `ctx.http()` with
    `hub.mode=subscribe`, `hub.topic`, `hub.callback=${publicBaseUrl}/v1/websub/{id}/{token}`, `hub.secret` (32
    random bytes, base64url) and `hub.lease_seconds=86400`;
  - **verify:** echoes the challenge only for mode `subscribe` with a matching topic and token (constant time), then
    sets `active` and starts the lease;
  - **receive:** the per-source cap is 60/min (Bucket4j); `X-Hub-Signature` `sha256=` or `sha1=` is checked over the
    raw body with `MessageDigest.isEqual`; the body is parsed with `FeedParser`, mapped with `FeedRecords` and handed
    to `PushIngestService.submit`, which stamps `receivedAt`;
  - **upkeep:** the poll stretches to 6 h while a subscription is active (`KitFeedAdapter` makes no request within 6 h
    of its last real poll). Renewal happens at 80 % of the lease. A failed renewal, an expired lease or a denial sets
    `lost` and the health reason "WebSub lease lost", and the source polls again.
- **`WebSubController`:**
  - `GET` and `POST /v1/websub/{sourceId}/{token}`: 200 (`text/plain`) / 202, and 403, 404, 413 (2 MB), 415, 429
    problems;
  - the body is read raw, never decompressed, because the HMAC covers it;
  - 404 for everything while WebSub is disabled.
- **`AccessPolicy`:** `GET` and `POST /v1/websub/*/*` are `OPEN`. Every other method and depth stays admin.

**6. Spec, Postman, docs:**
- **`docs/OpenAPI.json`:**
  - both operations `implemented` and removed from `pending-operations.txt`;
  - the GET description documents `denied` and that `unsubscribe` is 404;
  - the `fire-incident` and `bulletin` kind notes, with `feature-wfigs-incidents` payload fields;
  - a changelog entry.
- **Postman:** an Atom example body for `receiveWebSubNotification` (in `RAW_BODIES`, the generator's map for
  non-JSON bodies), regenerated.
- **`docs/Adapter-Guide.md` §22 "Kit sources":** writing specs, validator rules, the time-base rule, paging and
  completeness, the ArcGIS short-circuit, WFS axis order, simplification, untrusted text, the overlay, WebSub and the
  test property.
- **`docs/Source-Catalog.md`:**
  - a "Generic feature and feed kits (prompt 073)" section with the kit rows and defaults;
  - the note that the 075, 079, 088 and 094 sources are kit-based;
  - the roadmap line.
- **`docs/Invariants.md`:** SEC-1 (`AccessPolicyTest`, the WebSub rules) and SEC-3 (`WebSubControllerIT`, the secret
  encrypted).
- **`THIRD_PARTY_NOTICES.md`:** jsoup.

## Files created / modified

- **Created, main** (`src/main/java/ai/foresignal/source/kit/`):
  - `package-info`, `KitSpecs`, `KitSpecLoader`, `KitUrls`, `KitOverlay`;
  - `feature/`: `package-info`, `FeatureSourceSpec`, `FeatureSpecValidator`, `FeatureFetch`, `FeatureHttp`,
    `ArcGisQueryClient`, `EsriJsonConverter`, `OgcFeaturesClient`, `WfsJsonClient`, `FeatureGeometry`,
    `FeatureMapper`, `FeatureDescriptors`, `KitFeatureAdapter`, `FeatureKitFamily`;
  - `feed/`: `package-info`, `FeedParser`, `XmlTree`, `FeedText`, `FeedDates`, `FeedGeometry`, `FeedItem`,
    `FeedDocument`, `FeedSourceSpec`, `FeedSpecValidator`, `FeedItemMapper`, `FeedItemMappers`, `FeedRecords`,
    `FeedDescriptors`, `WebSubCoordinator`, `KitFeedAdapter`, `FeedKitFamily`;
  - `websub/`: `package-info`, `WebSubSubscriptionEntity`, `WebSubSubscriptionRepository`, `WebSubService`,
    `WebSubController`.
- **Created, resources:** `src/main/resources/kits/features/wfigs.json`, `src/main/resources/kits/feeds/bulletins.json`.
- **Created, test:**
  - `source/kit/`: `KitTestSpecs`, `KitIngestIT`;
  - `source/kit/feature/`: `FeatureSpecValidatorTest`, `FeatureMapperTest`, `FeatureGeometryTest`,
    `ArcGisQueryClientTest`, `OgcFeaturesClientTest`, `WfsJsonClientTest`;
  - `source/kit/feed/`: `FeedParserTest`, `KitFeedAdapterTest`;
  - `source/kit/websub/`: `StubHubContext`, `WebSubControllerIT`, `WebSubDisabledIT`;
  - fixtures `fixtures/feature-wfigs-incidents/` (layer and two query pages, README) and `fixtures/kit-feeds/` (NHC
    and ECDC captured, seven hand-made, README).
- **Modified:**
  - `gradle/libs.versions.toml` and `build.gradle.kts` (jsoup 1.23.2);
  - `security/AccessPolicy`;
  - test: `security/AccessPolicyTest`, `testsupport/weather/WeatherIngestSupport` (the kit base-URL property and two
    ids in the backoff list), `openapi/pending-operations.txt`;
  - `docs/OpenAPI.json`, `docs/Adapter-Guide.md`, `docs/Source-Catalog.md`, `docs/Invariants.md`;
  - `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`;
  - `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **`license`, not `licence`, as the spec key** (`FeatureSourceSpec.license`, `FeedSourceSpec.license`, JSON
   `"license"`). The project's US English rule applies to new identifiers and file formats. The spec files are new,
   so nothing persisted is renamed, and the descriptor's existing wire field stays `licence`. This is the same choice
   072 made.
2. **ArcGIS metadata versus the short-circuit.** A layer that publishes `editingInfo` has its layer document re-read
   on every run, because that read is the change probe: with a 24 h cache, a day of edits would be hidden behind a
   stale edit date. The capability fields are still kept in `arcgis:meta`. A layer without `editingInfo` reads its
   metadata at most once per 24 h and is queried every run, as the prompt says. `?returnUpdates=true` was tried on
   WFIGS (2026-10-13) and returned `{}`, so it is not used.
3. **Geometry reading.** `FeatureGeometry` reads with `GeoJsonReader(500_000, 8)` rather than `Geo.fromGeoJson`.
   `Geo.fromGeoJson` enforces the 10,000-vertex store limit while reading, so it would reject a large perimeter
   before it could be simplified. The SRID is still 4326.
4. **WebSub records go to the shared push pipeline** (`PushIngestService.submit`, which stamps `receivedAt`), not to a
   streaming `RecordSink`. A polling source has no sink of its own, and the push pipeline is the existing path that
   accepts records for a source outside a fetch. Health, usage and the PUSH `fetch_log` row come with it.
5. **Additions to the spec formats:**
   - feature specs: `format` (`geojson|esrijson`, as the prompt describes but does not list), `query.datetime` (OGC),
     `paging.maxFeatures`, `time.zone`, `retention` (default 1825 d, the `event` class) and `maxResponseBytes`;
   - feed specs: `description`, `homepage`, `websub`, `retention` and `maxResponseBytes`.

   The prompt names no retention for kit sources, and the Conventions table requires one by analytic class. A
   reference dataset (094) can set 90 d.
6. **`websub_subscription.requested_at`** was added to the prompt's columns. It keeps a pending subscribe request from
   being re-sent on every poll, and keeps a renewal from being repeated while its verification is pending.
7. **Id uniqueness "against the registry":** `KitSpecs` checks uniqueness across both kits. The source registry
   already fails startup on a collision with any other family, and kit ids carry their own prefixes (`feature-`,
   `feed-`, `op-`).
8. **`hub.mode=denied`** answers 200 with an empty body and marks the subscription lost; `unsubscribe` is 404.
   Foresignal never unsubscribes. The spec's GET description was updated to say so.
9. **The disabled-by-default WebSub test is `WebSubDisabledIT`** (the shared default context). `WebSubControllerIT`
   needs `enabled=true`, which is a context of its own. The prompt named only `WebSubControllerIT`.
10. **The feed `observedAt` default is `RECEIVED`.** The prompt lists the options but not the default, and RSS items
    rarely carry `updated`. All six bulletin specs use it.
11. **The Postman example sits in the generator's `RAW_BODIES` map**, its map for non-JSON bodies, rather than
    `BODIES`. The `X-Hub-Signature` header comes from the spec's header parameter, disabled by default.

## Issues found / open questions

1. **CISA answered `403 Access Denied` (Akamai)** to this host on 2026-10-13, both with the fixture User-Agent and with
   `Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)`. Its homepage, terms and privacy pages answered 403 too. The
   prompt says it was reachable on 2026-10-10. The spec ships as the prompt lists it (disabled), with no `termsUrl`
   and an unverified homepage. No fixture was captured.
2. **Other bulletin checks** (2026-10-13): NCSC, NHC Atlantic and eastern Pacific, ECDC and WHO answered 200 with RSS.
   `https://www.ncsc.gov.uk/rss` and `https://www.who.int/rss-feeds` (index pages) were 404, so the specs use
   `https://www.ncsc.gov.uk/` and `https://www.who.int/news` as homepages.
3. **Authors are stored in feed payloads**, as the prompt lists. NEWS-2 ("no author names are stored") is scoped to
   news (prompt 133). The bulletin feeds name offices ("ECDC", "nhcwebmaster@noaa.gov (NHC Webmaster)"), not people,
   but an operator feed could carry bylines. Owner question 1.
4. **The 072 note about moving Vegvesen onto the OGC kit** was not acted on. The prompt does not ask for it, and the
   CCTV pack's camera mapping and allowed-origin checks are not expressible in a feature spec.
5. **US English check** (`check_us_english.py` on the new code, specs, fixtures and the added doc lines): the only hits
   are allowed exceptions:
   - the descriptor wire field `licence`;
   - license names ("Open Government Licence v3.0");
   - organization names ("National Cyber Security Centre", "European Centre for Disease Prevention and Control");
   - the OGL URL;
   - captured upstream text in `ecdc-1307.xml`.

   The workspace guard `test_us_english.py` passed (14 tests, OK). It scans the MeshDTN repositories, not this one.
6. **`docs/Source-Catalog.html`** was not regenerated (as in 070 to 072).
7. **Instructions in data:** none. The WFIGS layer and query responses, the six feeds and the ArcGIS service document
   were read, and nothing addressed an agent.

**Owner questions (non-blocking):**
1. Should feed payloads drop `authors` (as NEWS-2 does for news), or keep them for bulletins only?
2. Keep `feed-cisa-advisories` in the pack while CISA blocks this host, or drop it until 143 confirms access?
3. Is the 1825 d (`event`) default retention right for every feed bulletin, or should general news (WHO, NCSC blog
   posts) be shorter?

## Tests written (not run)

- **`FeatureSpecValidatorTest`:**
  - each rule fails with its field path: identity, URL, protocol, fields, references, time base, schedule and limits;
  - every problem is aggregated with file, index and id, including duplicates and an unknown property;
  - the shipped specs, descriptors and licenses are valid;
  - the overlay is off by default, refused without the policy, and checks `op-` ids and the outbound URL policy.
- **`FeatureMapperTest`:**
  - typing, value maps, constants, the allowlist, sentinels and the date formats (epoch-s and -ms, ISO with and
    without an offset, a date, a pattern, `auto`, unreadable);
  - the zone of local date-times;
  - a missing id, a missing `observedAt` field, a non-feature and a bad geometry are rejected;
  - a null geometry and RECEIVED;
  - the WFIGS fixture, and `SnapshotContract.assertStableStaticHash` across two parses.
- **`FeatureGeometryTest`:** 20,000 vertices with a hole simplified to ≤ 9,500, valid, the hole kept, the tolerance
  0.0005 · 2^k; the flags in the payload; small, null and malformed geometries.
- **`ArcGisQueryClientTest`** (WireMock, real client):
  - pagination with the documented query, until `exceededTransferLimit` is false;
  - `maxPages` makes the fetch incomplete with no tombstone baseline;
  - the `lastEditDate` short-circuit, and a new date queries again;
  - metadata reuse without `editingInfo` over 24 h;
  - no pagination support;
  - Esri JSON with a hole, a point, paths and NaN;
  - an error body, a redirect, non-JSON; ring orientation.
- **`OgcFeaturesClientTest`:** cursor and offset (relative) next links; the first page's query and `Accept`; content
  types; the caps; an off-host next link; non-JSON alternates.
- **`WfsJsonClientTest`:** `startIndex` paging with the GetFeature parameters; `latlon`; the swap with a single
  warning; the sanity check's cases.
- **`FeedParserTest`:**
  - RSS 2.0 (NHC and ECDC captured), RSS 1.0, Atom (self and hub, html and xhtml, enclosure, GML polygon with a hole,
    a CRS84 point, a dropped `javascript:` link), JSON Feed 1.1 (hubs, `_geo`, attachments, a stripped script, a
    numeric id, a zone-less date, first-byte detection);
  - GeoRSS Simple (point, line, unclosed polygon, box, out of range, `content:encoded`) and Basic Geo;
  - XXE refused; odd dates; the hash id; HTML stripping and the 4 KB cut; format errors.
- **`KitFeedAdapterTest`:** conditional GET and 304 with Foresignal's User-Agent; the item cap and its counter; the
  category filter; `observedAt: published` skipping and the payload; the descriptor; an HTML body forgets the
  validators.
- **`WebSubControllerIT`** (`@IntegrationTest`, WebSub enabled, `MutableClock`):
  - the subscribe request, with the secret stored encrypted;
  - no request without https, a hub, a self link or `websub`, or with a loopback hub;
  - verification: echo, topic mismatch, wrong token, unsubscribe;
  - signed delivery: sha256 and sha1 accepted (records reach `record_head`); a wrong, missing or md5 signature is 403;
    a non-feed 415; a wrong token 404; over 2 MB 413;
  - before verification or for a disabled source 404; 61 a minute 429;
  - the stretched poll;
  - renewal at 80 % (1152 min) keeps the token and secret and is not repeated while pending; a failed renewal sets
    lost and the health reason;
  - an expired lease and a denial; signature parsing.
- **`WebSubDisabledIT`:** both callbacks are 404 by default, and nothing is subscribed or maintained.
- **`KitIngestIT`** (shares `WeatherIngestSupport`'s context):
  - every kit source is registered and disabled;
  - WFIGS: 8 incidents from two pages, then no query for an unchanged layer, then one miss and a tombstone after two;
  - ECDC: 10 items, and a later 3-item window tombstones nothing.
- **`AccessPolicyTest`:** only `GET` and `POST /v1/websub/*/*` are open.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- `./gradlew lintOpenApi`: valid; 71 warnings, the same count as before.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests across 80 tag folders, covers all ops.
- No project tests were run, because this is a feature prompt.
- **Scratch check:** a throwaway `main` outside the repository loaded `KitSpecs`, validated every kit descriptor with
  `DescriptorValidator` (no problems) and parsed the fixtures, to get exact values for the assertions. No project test
  was executed.

## Notes for the next prompt

1. **074, 075, 079, 088, 094 (kit users):**
   - add specs to `src/main/resources/kits/{features,feeds}/*.json`; `KitSpecs` validates them at startup;
   - an InciWeb-style feed adds a `FeedItemMapper` to `FeedItemMappers.REVIEWED`;
   - OGC and WFS starters arrive with 075 and 079. Their clients are tested against WireMock only; capture real
     fixtures (CWFIS for the WFS axis order) when the specs land;
   - set `retention` for reference datasets (90 d) and `recordTtl` where a layer has no deletions.
2. **097 checkpoint:**
   - first run of every 073 test;
   - `KitIngestIT` shares `WeatherIngestSupport`'s context (one new property, `foresignal.test.kits.base-url`);
   - `WebSubControllerIT` is a context of its own (`foresignal.kits.websub.enabled=true`), and `WebSubDisabledIT`
     uses the plain `@IntegrationTest` context;
   - check `deliveryLimits` (60 accepted pushes through the writer) for runtime;
   - check `FeatureGeometryTest`'s simplification step count holds on the JTS version in use;
   - `FamilyDefaultsTest` scans only `source/adapters`, so the kit adapters are not classified there; their retention
     comes from the spec.
3. **143:**
   - recapture per the fixture READMEs;
   - re-check CISA access (Issue 1);
   - confirm WFIGS still publishes `editingInfo`;
   - if a deployment enables WebSub, verify a real hub round trip with an https `publicBaseUrl`.
4. **144 (Flyway):** new table `websub_subscription` (`source_id` PK, `hub`, `topic`, `token`, `secret_enc` bytea,
   `state`, `lease_expires_at`, `last_verified_at`, `last_push_at`, `requested_at`). Adapter state:
   - `feature-*`: `arcgis:meta`, `arcgis:lastEdit`, `absence:ids`, `absence:missed`;
   - `feed-*`: `etag:feed`, `lastmod:feed`, `feed:lastPollAt`, `health.degraded` (lost lease).
5. **Owner decisions pending:** the three questions above, plus those carried from 012–072.
