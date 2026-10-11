# 058 — Map Styles Feeds And KML

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/058-CR-Map-Styles-Feeds-And-KML.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/057-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 10, §2 rows "Google Earth / Cesium" and "Local web / PWA", §3 rows "MapLibre style + CC0 sprite", "Atom / JSON Feed" and "KML/KMZ", §8 item 26 ("028g" = this prompt)
- `docs/Weather-Imagery-Design.md` §6.1 (cache policy), §6.3 (`/v1/styles` is already taken by **raster** styles from prompt 045, so vector map styles live under `/v1/map/`), §6.7 (tile keys, CORS), §6.10 (client recipes)
- Completion Reports of:
  - 008: `kindDisplay` (title template, icon, colour, optional SIDC), `fieldSpecs`, `aggBelowZoom`
  - 014: antimeridian bbox splitting
  - 016: TileJSON index and per-layer paths, `agg=grid|hex`
  - 017: alert lifecycle, `shortTitle`
  - 018: MarkdownRenderer
  - 023: `publicBaseUrl` and trusted proxies, installId
  - 029: briefing runs, HTML renditions
  - 035: feed keys bound to a path
  - 037: TrackService
  - 039: `EgressPolicy`, private filter
  - 050: tile keys `/v1/k/{key}/…`
- Specs:
  - MapLibre Style Spec (sources, layers, sprite, SDF icons)
  - TileJSON 3.0.0
  - RFC 4287 (Atom)
  - JSON Feed 1.1 (https://jsonfeed.org/version/1.1)
  - GeoRSS-Simple
  - OGC KML 2.2 and the Google `gx:` extensions (`gx:Track`)
- Libraries (check current versions and licences on Maven Central): `org.commonmark:commonmark` (BSD-2) and `com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer` (Apache-2.0). Icons: Maki (CC0) and Temaki (CC0). Sprite generator: `spreet` (MIT), used at development time only.

## Objective

Let off-the-shelf clients use Foresignal with no custom code:

- a MapLibre overlay style and sprite generated from each kind's display hints
- Atom and JSON Feed for alerts and briefing runs, so feed readers and automations can subscribe
- KML with self-refreshing NetworkLinks and `gx:Track` history, for Google Earth and Cesium

Every output is overlay-only (no basemap, no glyphs), honours `openReads`, feed and tile keys, and the private and egress rules, and escapes all upstream text.

## Tasks

### 1. Sprite (committed static resources)

- `scripts/build-sprite.sh` documents how to regenerate the sprite: download pinned Maki and Temaki releases (verify the tags), select the icons named by any `kindDisplay.icon` plus a default set (`marker`, `circle`, `triangle`, `airport`, `ferry`, `fire-station`, `danger`, `rail`, `bicycle`, `communications-tower`, `volcano`, `earthquake`…), and run `spreet --sdf` at 1× and 2×.
- Commit the outputs:
  - `src/main/resources/map/sprite/foresignal.{json,png}` and `foresignal@2x.{json,png}`
  - one 32 px PNG per icon under `map/icons/` (for KML)
- `SpriteCatalog` loads the JSON at startup and computes `sha8` over the PNG bytes.
- Registry validation (extend 008's `DescriptorValidator`): an unknown `kindDisplay.icon` fails startup, with the list of valid names.
- `THIRD_PARTY_NOTICES.md`: Maki and Temaki (CC0), spreet (tool only, not shipped).

### 2. MapLibre style (`ai.foresignal.map`)

- `GET /v1/map/style.json?layers=a,b&theme=light|dark` (read). A version-8 style document:
  - `sources`: one `vector` source per requested layer, with `url` = 016's per-layer TileJSON (concrete URL from `publicBaseUrl`, or a `/v1/k/{key}/…` prefix when the request itself came in through a tile key)
  - `sprite`: `<base>/v1/map/sprites/foresignal-{sha8}`
  - **no `glyphs`** and no text layers
  - `metadata.foresignal`: `{layers: [{id, kinds, attribution, minzoom, aggBelowZoom}], generatedAt}`
- Layers per kind, from `kindDisplay`, filtered with `["==", ["get","kind"], kind]` when the source carries several kinds:
  - **Points with an icon:** a `symbol` layer, `icon-image` = icon, SDF `icon-color` = colour (a data-driven `match` on `severity` or `colorField` when declared), `icon-rotate` from `rotateField` (e.g. `track`) with `icon-rotation-alignment: map`, `icon-allow-overlap: true`.
  - **Points without an icon:** a `circle` layer with radius `interpolate` by zoom and an optional `sizeField` (e.g. `frp`, `mag`).
  - **Lines:** `line`.
  - **Polygons:** `fill` (opacity 0.25) plus `line` outline.
  - **Below `aggBelowZoom`:** add a second source using 016's `agg=hex`, with a `fill` colour ramp on `count`, and set `maxzoom`/`minzoom` so the two layer sets hand over cleanly.
- If a property the style needs (`rotateField`, `sizeField`, `colorField`) is missing from the source's `tileProperties`, startup fails validation with a clear message.
- `GET /v1/map/sprites/foresignal-{sha8}[@2x].{json,png}` (read): `public|private, max-age=31536000, immutable` (it is content-addressed). An unknown `sha8` → 404 with `Link` to the current one.
- Style caching: strong ETag over the document, `max-age=300`.
- Private sources (039) are omitted unless the caller is allowed.
- Generated URLs use `publicBaseUrl` and never raw `X-Forwarded-*`, unless 023's trusted-proxy handling validated them.
- Docs (frontend recipes):
  - MapLibre GL JS: GL JS has no style merge, so add the sources and layers programmatically over your basemap, or use the multi-sprite `sprite` array.
  - maplibre_gl (Flutter mobile and web).
  - flutter_map + vector_map_tiles (desktop).
  - OpenLayers via `ol-mapbox-style`.
  - QGIS (MVT plus a style URL).

### 3. Atom and JSON Feed

- Endpoints (read; `openReads` applies; feed keys via `/v1/k/{key}/…` from 035):
  - `GET /v1/alerts/feed.atom` and `/v1/alerts/feed.json`: filters `rule`, `minSeverity`, `source`, `kind`, `state`, `limit` (default 50, max 200), newest first by last state change
  - `GET /v1/briefings/{id}/runs/feed.atom` and `.json`
- Write Atom with StAX (`XMLStreamWriter`), not Rome. Write JSON Feed with Jackson.
- Entry fields:
  - `id`: `tag:foresignal,2026:{installId}/alert/{id}` (or `/briefing-run/{id}`)
  - `updated` / `date_modified`: the last lifecycle change; `published` / `date_published`: `firedAt`
  - `title`: `shortTitle`, else the title
  - `link rel=alternate`: the deep link via `clientLinks`, else `publicBaseUrl/v1/alerts/{id}`
  - `category`: severity and rule tags
  - `author`: Foresignal
- Content:
  - Atom `content type="html"` and JSON Feed `content_html` hold the markdown rendered with commonmark-java, then sanitised by an OWASP allowlist policy (`p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]` with `http`/`https` only and `rel="nofollow noopener"`).
  - `content_text` holds plain text.
  - Briefing runs embed the full markdown, sanitised the same way.
- GeoRSS (`xmlns:georss="http://www.georss.org/georss"`): `georss:point "lat lon"` for points; `georss:polygon` for single polygons with ≤ 500 vertices; otherwise the centroid as a point. JSON Feed uses the extension `_foresignal: {severity, state, sourceId, externalId, geometry}`.
- Conditional GET: ETag = hash(max lifecycle timestamp, max id, filters), plus `Last-Modified`; `If-None-Match` and `If-Modified-Since` → 304.
- Media types `application/atom+xml` and `application/feed+json`. RSS 2.0 and WebSub are not offered; note this in the docs.
- Private-source alerts are excluded unless the key or token passes the private gate. Call `EgressPolicy.allows(FEED, …)`.

### 4. KML / KMZ

- `GET /v1/kml/networklink.kml?layers=&refresh=60` (read) returns a Document with one `NetworkLink` per layer:
  - `Link href` = `…/v1/kml/records.kml?layer={layer}` (with the feed-key prefix when one is used)
  - `refreshMode=onInterval`, `refreshInterval` ≥ 30
  - `viewRefreshMode=onStop`, `viewRefreshTime=2`
  - `viewFormat=bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]`
- `GET /v1/kml/records.kml?layer|source|kind&bbox&asOf&format=kml|kmz` (read):
  - one `Style` per kind: `IconStyle` with the icon href at `<base>/v1/map/icons/{name}.png` (relative inside KMZ, where the icons are bundled), colour converted to KML `aabbggrr`, `heading` from `rotateField`
  - one `Placemark` per record: `name` = title; `TimeStamp/when` = `observedAt`; `ExtendedData/Data` for the `fieldSpecs`/`tileProperties` fields
  - `description` is **plain escaped text** (field: value with units). Escape every string for XML and also for HTML (`<`, `>`, `&`, `"`), because Google Earth renders HTML in balloons. Never use CDATA with upstream text.
  - Cap: 5,000 Placemarks, newest `observedAt` first. When the cap is hit, the Document gets `Snippet` "truncated: showing 5000 of N" and `ExtendedData truncated=true`.
  - A bbox with `west > east` (antimeridian) is split into two envelopes with 014's splitter. A near-global bbox is allowed but still capped.
- `GET /v1/kml/tracks.kml?sourceId&externalId | kind&bbox&from&to` (read): one `Placemark` with `gx:Track` per entity (`when` plus `gx:coord "lon lat alt"`; altitude from the payload altitude field per the Adapter Guide convention, else 0 with `altitudeMode=clampToGround`). Data comes from 037's TrackService **unsimplified**. Limits: ≤ 100 entities and ≤ 5,000 points per track, with truncation flags.
- `GET /v1/triggers/{ruleId}.kml` (read): the rule's geofence as a `Polygon`, or a `LineString` plus a buffered `Polygon`, or its `geofenceRef` union from 040, simplified to ≤ 10,000 vertices.
- KMZ (`format=kmz`, `application/vnd.google-earth.kmz`): a zip of `doc.kml` plus the icons used. Otherwise `application/vnd.google-earth.kml+xml`.
- The Document `description` holds the attributions of the included sources (descriptor `attribution`; prompt 109 later aggregates).
- Private filtering and `EgressPolicy.allows(FEED, …)` apply.
- Keys in URLs are redacted from logs (reuse 039's redaction filter).

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`: operations `getMapStyle`, `getMapSprite`, `getMapIcon`, `getAlertsAtom`, `getAlertsJsonFeed`, `getBriefingRunsAtom`, `getBriefingRunsJsonFeed`, `getKmlNetworkLink`, `getKmlRecords`, `getKmlTracks`, `getTriggerKml`. Each is read with `x-foresignal-scope` and has the correct media types (`application/json`, `image/png`, `application/atom+xml`, `application/feed+json`, `application/vnd.google-earth.kml+xml`, `application/vnd.google-earth.kmz`). Add `KindDisplay` to the kind schema docs if 008 did not.
- `AccessPolicy`: READ for `GET /v1/map/**`, `/v1/kml/**`, `/v1/alerts/feed.*`, `/v1/briefings/*/runs/feed.*` and `/v1/triggers/*.kml`, ordered before any broader rule. Feed keys (035) accept these prefixes, and tile keys (050) accept `/v1/map/`.
- Regenerate Postman (GET only, so no `BODIES` changes).
- Docs:
  - `docs/Operator-Guide.md`: feed reader setup with feed keys, the Google Earth NetworkLink, Cesium `KmlDataSource`
  - frontend recipes: the style and sprite (see §2)
  - Charter: a sentence carving out "default styles are overridable data, not UI"
  - Architecture: a short "Map styles, feeds and KML" section

## Tests to write (do not run)

- `SpriteCatalogTest`: JSON and PNG parse, SDF flag, `sha8` stable, every `kindDisplay.icon` across registered descriptors exists.
- `MapStyleServiceTest`: golden style for a fixture registry (point icon with rotation, circle with size, polygon, agg handover); no `glyphs`; missing `rotateField` fails validation; tile-key prefixing; private source omitted.
- `MapControllerIT`: ETag/304, immutable sprite headers, 404 plus `Link` for a stale `sha8`.
- `AtomFeedWriterTest` / `JsonFeedWriterTest`: golden fixtures; schema-valid Atom; GeoRSS point and polygon; the vertex limit falls back to a point.
- `FeedSanitizerTest`: `<script>`, `javascript:` links, event-handler attributes and raw HTML in markdown are all neutralised.
- `FeedsControllerIT`: filters, conditional GET, feed key access with `openReads=false`, private exclusion.
- `KmlWriterTest`: escaping (`<b>`, `&`, quotes, control characters), colour conversion, cap and truncation flag, `gx:Track` ordering, KMZ zip contents.
- `KmlControllerIT`: NetworkLink fields, antimeridian bbox split, asOf passthrough, trigger geofence export, media types.

## Acceptance criteria

- Dropping `/v1/map/style.json`'s sources and layers onto a MapLibre basemap renders every kind with its icon and colour, with no glyph requests.
- A feed reader subscribed to `/v1/alerts/feed.atom` (with a feed key when `openReads=false`) receives sanitised entries with GeoRSS.
- Google Earth refreshes `networklink.kml` on view stop, and never receives more than 5,000 Placemarks per layer or unescaped upstream HTML.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `058: Map Styles Feeds And KML`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/058-CR-Map-Styles-Feeds-And-KML.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `058: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
