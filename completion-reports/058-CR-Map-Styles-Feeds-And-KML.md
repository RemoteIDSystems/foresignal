# 058-CR-Map-Styles-Feeds-And-KML

| | |
|---|---|
| Prompt | 058-Map-Styles-Feeds-And-KML.md |
| Status | Complete |
| Implementation commit | 775f11114492ea08c9d401a7b82fba0994c0d28a |
| Date | 2026-10-13 |

## Summary

Off-the-shelf clients can now use Foresignal with no custom code:
- MapLibre gets an overlay style and an SDF sprite generated from each kind's display hints;
- feed readers and automations get Atom and JSON Feed for alerts and briefing runs;
- Google Earth and Cesium get KML with self-refreshing NetworkLinks, `gx:Track` history and trigger geofences.

Every output is overlay-only, with no basemap and no glyphs. Every output honors `openReads`, feed keys and
tile keys, the private filter and `EgressPolicy.allows(FEED, …)`, and escapes all upstream text. All eleven
operations are `implemented` and off `pending-operations.txt`.

**1. Sprite.**
- **Script:** `scripts/build-sprite.sh` downloads three pinned releases:
  - Maki v8.0.1 and Temaki v5.13.0 (both CC0; the tags were checked through the GitHub API);
  - spreet v0.13.1 (MIT), a platform binary placed in a temporary directory, never shipped.

  It selects 75 icons plus 13 aliases, renames Temaki's underscores to hyphens, and runs `spreet --sdf` at 1×
  and `--retina`. It also renders one white 32 px PNG with a dark outline per icon with ImageMagick, for KML.
- **Aliases:** these give the names the adapters already use (`earthquake`, `warning`, `fire`,
  `lightning`, `smoke`, `storm`, `forecast`, `station`, `outlook`, `storm-report`) and three common EDP names
  (`plane`, `ship`, `camera`) an image from either set.
- **Committed output:** `src/main/resources/map/sprite/foresignal{,@2x}.{json,png}` (88 SDF entries) and
  `src/main/resources/map/icons/*.png` (88 files), 424 KB in all.
- **`SpriteCatalog`** (`ai.foresignal.map`):
  - it loads both sheets at startup;
  - `sha8` is the first 8 hex digits of the SHA-256 of the 1× PNG followed by the 2× PNG;
  - the names are also readable statically (`iconNames()`) for the validator.
- **Validation:** `DescriptorValidator` now fails startup for a `kindDisplay.icon` outside the sprite, and
  the message lists every valid name. It also fails for a `rotateField`, `sizeField` or `colorField` that is
  not one of the source's `tileProperties`.
- **`KindDisplay`** gained `rotateField`, `sizeField` and `colorField`, which the spec already declared. The
  four-argument constructor is kept.
- **Notices:** `THIRD_PARTY_NOTICES.md` has a "Map sprite and KML icons (prompt 058)" section for Maki,
  Temaki and spreet (tool only).

**2. MapLibre style** (`MapStyleService`, `MapController`).
- `GET /v1/map/style.json?layers&theme` returns a version-8 document:
  - one `vector` source per layer, whose `url` is the 016 TileJSON;
  - `sprite` = `<base>/v1/map/sprites/foresignal-{sha8}`;
  - no `glyphs` and no text layers;
  - `metadata.foresignal` = `{layers:[{id, kinds, attribution, minzoom, aggBelowZoom}], theme, generatedAt}`.
- **Layers per kind** (ids `<layer>--<kind>--point|line|fill|outline`):
  - a point with an icon is a `symbol` with SDF `icon-color`, `icon-rotate` from `rotateField`,
    `icon-rotation-alignment: map` and `icon-allow-overlap`;
  - a point without an icon is a `circle`, whose radius interpolates by zoom and grows with
    `sqrt(sizeField)`;
  - lines are a `line`;
  - polygons are a `fill` at 0.25 plus a `line` outline.

  Each layer is filtered by geometry type, and also by `["==",["get","kind"],kind]` when its layer carries
  several kinds.
- **Colors:** the kind's `colour`, else the theme default. With a `colorField`, or a `severity` tile
  property, the color is a `match` over common severity words that falls back to the kind's color.
- **Aggregation:** below `aggBelowZoom` a second source `<layer>--agg` (`tiles` = `…/{z}/{x}/{y}.mvt?agg=hex`,
  `maxzoom` = `aggBelowZoom`) gets a `fill` ramp on `count` with `maxzoom` = `aggBelowZoom`. The raw layers
  start at `minzoom` = `aggBelowZoom`, so the two sets hand over at one zoom.
- **Caching:** strong ETag, `max-age=300`, 304 on `If-None-Match`. The style is `private` when the caller
  sees private sources, is restricted, or came through a key.
- **Sprite files:** `GET /v1/map/sprites/foresignal-{sha8}[@2x].{json,png}` is
  `public|private, max-age=31536000, immutable`. An unknown hash is 404 with
  `Link: <…/foresignal-{current}.json>; rel="latest-version"`.
- **Icons:** `GET /v1/map/icons/{name}.png` returns the 32 px KML icon with `max-age=604800`.
- **URLs:** they come from `publicBaseUrl`, else the request base. With `forward-headers-strategy: native`
  (023), Tomcat's `RemoteIpValve` corrects that base only for trusted proxies; no raw `X-Forwarded-*` is
  read.
- **Keys:** a request that came through `/v1/k/{key}/…` gets keyed URLs:
  - `PathKeyFilter` now stores `/v1/k/{key}` in a request attribute (`keyBase`);
  - the new `PublicUrls` builds every generated URL.

**3. Atom and JSON Feed** (`ai.foresignal.feed`, `FeedsController`).
- **Endpoints:**
  - `GET /v1/alerts/feed.atom|json` with `rule`, `minSeverity`, `source`, `kind`, `state` and `limit`
    (default 50, max 200);
  - `GET /v1/briefings/{id}/runs/feed.atom|json`.
- **Alert order:** newest first by last lifecycle change, from the new `AlertStore.feed`:
  `ORDER BY GREATEST(fired_at, acknowledged_at, resolved_at) DESC, id DESC` with the same filters,
  exclusions and restriction clamp as `listAlerts`.
- **Writers:** Atom is written with StAX and JSON Feed 1.1 with Jackson.
- **Entries:**
  - `id`: `tag:foresignal,2026:{installId}/alert/{id}` (or `/briefing-run/{id}`);
  - `updated`: the last lifecycle change; `published`: `firedAt`;
  - `title`: the short title (the rule name, as notifications use it), else the message's first line;
  - link: the `clientLinks.alert` deep link when the operator set `clientLinks`, else
    `publicBaseUrl/v1/alerts/{id}`;
  - categories: the severity and the rule tags;
  - author: Foresignal.
- **Content:** `FeedSanitizer` renders markdown with commonmark-java (`escapeHtml`, `sanitizeUrls`), then
  applies the OWASP allowlist `p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]`
  (`http`/`https`, `rel="nofollow noopener"`). `content_text` and Atom `summary` hold plain text
  (`TextContentRenderer`). Briefing runs embed the full markdown.
- **GeoRSS:** a point is `georss:point "lat lon"`; a single polygon with at most 500 vertices is
  `georss:polygon`; anything else is the centroid. JSON Feed items carry
  `_foresignal: {severity, state, sourceId, externalId, geometry}`.
- **Conditional GET:** the ETag hashes the newest change, the highest entry id, the entry count and the
  filters (caller included). `Last-Modified` is sent too. `If-None-Match` or, without it,
  `If-Modified-Since` gives 304.
- **Privacy:** private-source alerts are excluded unless the caller passes the private gate, and every
  entry must also pass `EgressPolicy.allows(FEED, …)`. Briefing runs that cite a private source are left
  out for callers without the gate (039's rule).
- **Not offered:** RSS 2.0 and WebSub (documented).

**4. KML / KMZ** (`ai.foresignal.kml`, `KmlController`).
- **`networklink.kml`:** one `NetworkLink` per layer to `…/v1/kml/records.kml?layer=…` (keyed when the
  request was), with:
  - `refreshMode=onInterval` and `refreshInterval` ≥ 30 (`refresh` 30–86400, default 60);
  - `viewRefreshMode=onStop` and `viewRefreshTime=2`;
  - the bbox `viewFormat`.
- **`records.kml`:**
  - selection by `layer`, `source` or `kind` (at least one); `bbox`, `asOf` and `format`;
  - one `Style` per kind with the icon href (`<base>/v1/map/icons/{name}.png`, or `icons/{name}.png` inside
    KMZ) and the color as `aabbggrr`;
  - an inline `IconStyle` `heading` from `rotateField`;
  - one `Placemark` per record: name = title, `TimeStamp/when` = `observedAt`, `ExtendedData` for the tile
    properties (else the field specs), and a plain-text `description` of `label: value unit` lines;
  - at most 5,000 Placemarks, newest first. When truncated, the document has the Snippet
    "truncated: showing 5000 of N" and `ExtendedData truncated=true` with the total;
  - the shared bbox parser (014) already splits an antimeridian bbox; asOf uses `AsOfQueryService`.
- **`tracks.kml`:** one entity (`sourceId`+`externalId`) or up to 100 entities (`kind`+`bbox`), each a
  `gx:Track`:
  - `altitudeMode`, then the `when` values, then the `gx:coord` values;
  - the track comes from `TrackService` with tolerance 0;
  - at most 5,000 points, the newest kept, with a `truncated` flag;
  - altitude comes from the first field spec with datum MSL, WGS84, AGL or PRESSURE in `m`, `km` or `[ft_i]`
    (Adapter Guide §5), converted to meters: `absolute`, or `relativeToGround` for AGL. Without one the
    track is `clampToGround` at 0.
- **`/v1/triggers/{ruleId}.kml`:** the rule's materialized fence, plus the route line for a corridor, or
  the 040 `geofenceRef` union through `RegionFences`. It is simplified (topology-preserving, growing
  tolerance) to at most 10,000 vertices. A rule without a fence is 404.
- **Escaping:** `KmlWriter` (StAX) escapes every name, description and data value for HTML (`<`, `>`, `&`,
  `"`) before the XML writer escapes it again. It drops characters XML 1.0 forbids (`XmlText`) and never
  writes CDATA.
- **KMZ** is `doc.kml` first, then `icons/<name>.png`, served as `application/vnd.google-earth.kmz` with a
  `Content-Disposition` attachment.
- **Document description:** the included sources' attributions.
- **Keys in logs:** they stay redacted by the existing `PathKeyFilter.redact` access-log attribute.

**5. Spec, policy, Postman, docs.**
- **`docs/OpenAPI.json`:**
  - the 11 operations are `implemented`;
  - the `TileKey`/`TileKeyCreate` prefix enums gain `/v1/map/`;
  - `info.description` lists the tile-key `/map/` carrier and the key-echo exception;
  - getMapStyle's and getKmlNetworkLink's `layers` descriptions and the `refresh` maximum (86400) are
    updated;
  - there is a changelog entry. `lintOpenApi` passes (71 warnings, the existing unused-component kind).
- **`AccessPolicy`:** READ for `GET /v1/map/**`, `/v1/kml/**`, `/v1/alerts/feed.*`,
  `/v1/briefings/*/runs/feed.*` and `/v1/triggers/*.kml`, placed before the broader trigger, alert and
  briefing rules.
- **Keys:** `TileKeyPolicy.ALLOWED_PREFIXES` gains `/v1/map/`. Feed keys already accept any read GET path
  in the spec.
- **Postman:** regenerated (829 requests; only descriptions changed).
- **Docs:**
  - Operator Guide: "Feeds, Google Earth and Cesium" (feed readers with feed keys, the Google Earth
    NetworkLink, Cesium `KmlDataSource`, KMZ, tracks, geofences), plus the tile-key prefix list;
  - Frontend Guide: "Overlay style and sprite", with recipes for MapLibre GL JS (`addSprite` plus prefixed
    `icon-image`, or the sprite array), maplibre_gl, flutter_map with vector_map_tiles, OpenLayers through
    `ol-mapbox-style` and QGIS, plus the prefix list;
  - Architecture: §29 "Map styles, feeds and KML" and the §15.1 tile-key prefixes;
  - Invariants: EGR-1 gains `FeedsControllerIT`, `KmlServiceTest` and `MapStyleServiceTest`.
- **Library APIs checked on 2026-10-13:**
  - MapLibre GL JS 6.14.0 `Map#addSprite`, read in the `main` source;
  - maplibre_gl 0.27.1 `addImage(name, bytes, [sdf])`;
  - ol-mapbox-style 13.5.1 `apply`/`applyStyle`;
  - vector_map_tiles 8.0.0 `StyleReader`/`VectorTileLayer`.

## Files created / modified

**Created**
- `scripts/build-sprite.sh`
- `src/main/resources/map/sprite/foresignal.json`, `foresignal.png`, `foresignal@2x.json`, `foresignal@2x.png`
- `src/main/resources/map/icons/*.png` (88)
- `src/main/java/ai/foresignal/map/`: `package-info`, `SpriteCatalog`, `MapStyleService`
- `src/main/java/ai/foresignal/feed/`: `package-info`, `Feed`, `FeedEntry`, `FeedSanitizer`, `FeedService`,
  `AtomFeedWriter`, `JsonFeedWriter`, `GeoRss`, `XmlText`
- `src/main/java/ai/foresignal/kml/`: `package-info`, `KmlModel`, `KmlWriter`, `KmlService`
- `src/main/java/ai/foresignal/api/`: `MapController`, `FeedsController`, `KmlController`,
  `support/PublicUrls`
- Tests:
  - `map/SpriteCatalogTest`, `map/MapStyleServiceTest`;
  - `feed/FeedSanitizerTest`, `feed/XmlTextTest`, `feed/AtomFeedWriterTest`, `feed/JsonFeedWriterTest`,
    `feed/FeedFixtures`;
  - `kml/KmlWriterTest`, `kml/KmlServiceTest`;
  - `api/MapControllerIT`, `api/FeedsControllerIT`, `api/KmlControllerIT`.

**Modified**
- `source/spi/KindDisplay` (three style fields), `source/DescriptorValidator` (icon and style-field checks),
  `inbound/edp/EdpDescriptorBuilder` (unknown icons dropped)
- `security/restrict/PathKeyFilter` (`KEY_BASE_ATTRIBUTE`, `keyBase`), `security/AccessPolicy`,
  `access/TileKeyPolicy`
- `trigger/AlertStore` (`feed`), `trigger/AlertService` (`feed`, `lastChange`)
- `briefing/BriefingRunRepository` (paged finished runs), `briefing/BriefingService` (`feedRuns`)
- `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Operator-Guide.md`, `docs/Frontend-Guide.md`,
  `docs/Invariants.md`
- `postman/Foresignal.postman_collection.json`, `src/test/resources/openapi/pending-operations.txt`,
  `THIRD_PARTY_NOTICES.md`

## Deviations from the prompt

1. **Sprite icon names and aliases.**
   - Neither Maki nor Temaki has `earthquake`, and the adapters' existing icon names (`warning`, `fire`,
     `lightning`, `smoke`, `storm`, `forecast`, `station`, `outlook`, `storm-report`) are not icon names in
     either set.
   - Instead of renaming the icons on the wire (`kindDisplay` is published), the build script adds these
     names, plus `plane`, `ship` and `camera`, as alias entries that reuse an icon of either set. For
     example, `earthquake` uses Maki `circle-stroked` and `lightning` uses Temaki `power`.
   - Temaki names are rewritten to kebab-case to fit `getMapIcon`'s `^[a-z0-9-]+$`.
2. **EDP icons.** An EDP stream's `presentation.icon` that is not in the sprite is no longer copied into
   `kindDisplay.icon`. It stays in `foresignal:presentation`. Otherwise every external provider with a free
   icon name would fail validation. `EdpManifestServiceTest`'s `ship` is an alias, so that test is unchanged.
3. **Default layer set.** Without `layers`, the style and the NetworkLink use every readable **source**
   layer, not every layer of the TileJSON index. The index also lists kind layers, which repeat the same
   records, so every feature would be drawn twice. Kind layers are included when named. A requested layer
   the caller cannot see, or that does not exist, is left out silently, like a private source. The spec's
   `layers` descriptions say so.
4. **Geometry-typed layers.** `kindDisplay` carries no geometry type. So every kind gets a point layer
   (symbol or circle), a line layer and a fill plus outline, each filtered by `["geometry-type"]`, and a kind
   shows whatever geometry its records have.
5. **`generatedAt`** is when `MapStyleService` started. A per-request timestamp would change the strong
   ETag on every request.
6. **Keys in response bodies.** Through a key-in-path URL, the follow-up URLs repeat the caller's key, as
   the prompt requires. These are the style's sources and sprite, the NetworkLink and icon hrefs, and a
   feed's own self link (entry links never carry it). Such responses are always `Cache-Control: private`.
   The spec's `info.description` ("the key is never echoed except by …") and the `PathKeyFilter` comment
   now state this exception.
7. **Unsimplified tracks.** `TrackService` caps `maxPoints` at 10,000, so tracks are read with tolerance 0
   and `maxPoints` 10,000. Only an entity with more than 10,000 points in the window is thinned before the
   newest 5,000 are kept. A completely raw read would need a new `TrackService` mode.
8. **asOf ordering.** `AsOfQueryService` pages in id order. For `asOf`, up to 10 pages of 5,000 are read and
   sorted by `observedAt`. Beyond 50,000 matching entities the total is a lower bound, shown as
   `of N+` in the Snippet.
9. **Alert entry link.** It follows the notification rule: the `clientLinks.alert` template only when the
   operator has set `clientLinks`. The shipped default (`foresignal://…`) is not used, because feed readers
   cannot open it.
10. **Charter not edited.** The prompt asks for a Charter sentence ("default styles are overridable data,
    not UI"). The Charter is the owner's document, so the sentence went into the Architecture §29 and
    `ai.foresignal.map` package text, and is raised as Issue 1.
11. **Golden style.** `MapStyleServiceTest` asserts the generated structure field by field (sources, ids,
    expressions, zoom handover) rather than comparing against a stored golden file, because tests could not
    be run to produce one. `JsonFeedWriterTest` does compare against a golden document.

## Issues found / open questions

1. **Charter (owner):** add "Default map styles are overridable data, not UI" to the Charter? It was not
   edited (Deviation 10).
2. **Severity color words:** the color `match` uses a fixed severity vocabulary:
   - extreme/critical `#C92A2A`;
   - severe/warning/high `#E8590C`;
   - moderate/watch/medium `#F08C00`;
   - minor/advisory/low/info `#1971C2`.

   A `colorField` with another vocabulary, such as METAR `flightCategory`, falls back to the kind's color.
   Should `kindDisplay` gain a value-to-color map?
3. **Adapters declare no `rotateField` or `sizeField` yet.** Candidates are USGS `sizeField: mag`, GOES FDC
   `sizeField: frpMw`, and aircraft `rotateField: track` once an aircraft adapter exists. Left for the owner
   or a later adapter prompt.
4. **vector_map_tiles:** the latest pub.dev release, 8.0.0, is built against flutter_map 7.0.2, while the
   Frontend Guide pins flutter_map 8.3.2, and the 10.x line is in beta. The guide says to pin a pair that
   resolves together.
5. **Sprite reproducibility:** the PNG bytes, and so `sha8`, depend on the spreet and ImageMagick versions.
   Rebuilding on another machine may change the hash, which is harmless because the sprite is
   content-addressed.
6. Data contained no instructions directed at the agent. Temaki ships an `AGENTS.md`, which was not read or
   followed.

## Tests written (not run)

- `SpriteCatalogTest`:
  - both sheets parse, every entry is SDF with pixel ratios 1 and 2;
  - `sha8` is stable and equals the SHA-256 prefix;
  - the default set and the aliases are present, and each has a 32 px PNG;
  - every `kindDisplay.icon` in the adapter sources is in the sprite.
- `MapStyleServiceTest`:
  - version 8, the sprite URL, no glyphs and no text layers;
  - an icon with rotation, a circle with size, and line, fill and outline;
  - a multi-kind filter and the severity match;
  - the aggregation handover;
  - tile-key prefixing and private-source omission;
  - validation failures for a missing `rotateField`, `sizeField` or `colorField` and an unknown icon.
- `FeedSanitizerTest`: `<script>`, `javascript:` and `data:` links, event handlers, raw HTML, h1/h2 and
  images are all neutralized, while allowed markup survives.
- `XmlTextTest`: XML 1.0 cleaning and HTML escaping.
- `AtomFeedWriterTest`, checked with a namespace-aware DOM:
  - the elements RFC 4287 requires;
  - entry fields, escaping and no CDATA;
  - a GeoRSS point and polygon, the 500-vertex limit, and the centroid fallback;
  - control characters.
- `JsonFeedWriterTest`: a golden document, and optional members.
- `KmlWriterTest`:
  - double escaping of `<b>`, `&`, quotes and control characters;
  - colors and records structure;
  - the truncation Snippet with `ExtendedData`, and the inline heading;
  - `gx:Track` child order;
  - NetworkLink fields, the geofence, the KMZ zip layout and the number format.
- `KmlServiceTest`, with Mockito stand-ins:
  - the 5,000 cap with the counted total, and the egress filter;
  - KMZ relative hrefs and bundled icons;
  - track truncation to the newest 5,000 with feet-to-meter conversion, and `clampToGround`;
  - the altitude-field rules, geofence simplification, field selection and keyed NetworkLink hrefs.
- `MapControllerIT` (the `TileTestSources` context):
  - ETag/304, `max-age=300` and a bad theme;
  - a private layer omitted for anonymous callers and present, privately cached, for the admin;
  - immutable sprite headers and media types for 1× and 2×;
  - a stale `sha8` gives 404 with `Link`;
  - icons.
- `FeedsControllerIT` (the `TriggerTestSources` context):
  - Atom media type, tag ids, GeoRSS and categories;
  - every filter, and 400s;
  - the JSON Feed extension;
  - conditional GET with `If-None-Match` and `If-Modified-Since`, and the tag change after an
    acknowledgement;
  - feed-key access with `openReads=false` (self link keyed, other prefixes 403);
  - private exclusion;
  - briefing run feeds (order, sanitizing, 404).
- `KmlControllerIT` (the `TileTestSources` context):
  - NetworkLink fields and a `refresh` below 30;
  - newest-first records, attribution, and the antimeridian bbox split;
  - required selection and an unknown layer;
  - asOf passthrough and a future asOf;
  - KMZ media type and zip;
  - single and multi-entity tracks;
  - trigger geofence export and 404s;
  - media types.

The three ITs reuse existing test configurations (`TileTestSources`, `TriggerTestSources`), so they add no
Spring context.

## Compile result

`./gradlew compileJava compileTestJava` succeeded (only the existing deprecation note from
`McpServerConfiguration`). `./gradlew lintOpenApi` passed: "Your API description is valid", 71 warnings.
`python3 scripts/generate-postman.py docs/OpenAPI.json postman` reported "829 requests across 80 tag
folders … covers all ops: True".

## Notes for the next prompt

1. **059 (mapping profiles):** no interaction. `KindDisplay` now has seven members, but the
   four-argument constructor still works. A mapping-created custom source with a `kindDisplay.icon` must
   use a sprite name (`SpriteCatalog.iconNames()`), or validation fails.
2. **097 checkpoint:**
   - `MapControllerIT` and `KmlControllerIT` share `TileTestSources`; `FeedsControllerIT` shares
     `TriggerTestSources`.
   - `KmlControllerIT.asOf` depends on the asOf plan accepting `T+600` with the clock at `T+3600`.
   - `FeedsControllerIT.conditionalGet` acknowledges an alert by SQL, to move `acknowledged_at`.
   - `SpriteCatalogTest.adapterIcons` reads `src/main/java/…/adapters` relative to the project directory.
   - The `KindDisplay` JSON of `/v1/sources` and `/v1/kinds` now carries `rotateField`, `sizeField` and
     `colorField` (null when unset).
3. **Adding an icon:** add it to `MAKI_ICONS`, `TEMAKI_ICONS` or `ALIASES` in `scripts/build-sprite.sh`, run
   the script, and commit the sprite and icons. The `sha8` changes, and clients follow the style's new
   `sprite` URL.
4. **143:** check Google Earth Pro and Cesium against a real deployment (NetworkLink refresh on view stop,
   the 5,000 cap, KMZ icons), and MapLibre GL JS with `addSprite`.
5. **144 (Flyway):** no schema change in 058 (the feed query uses existing `alert` columns; `briefing_run`
   is unchanged).
6. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–057.
