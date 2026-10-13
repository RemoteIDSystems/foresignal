# 055-CR-Static-Maps-And-Notification-Attachments

| | |
|---|---|
| Prompt | 055-Static-Maps-And-Notification-Attachments.md |
| Status | Complete |
| Implementation commit | 8fa659eb3b6465892a027e626e78645b1cdf7173 |
| Date | 2026-10-13 |

## Summary

Foresignal now draws basemap-free static map PNGs on the server, with Java2D, from a bundled Natural Earth
asset and its own stored frames. One renderer serves three paths:
- `GET /v1/maps/static`;
- the MCP tool `get_map_image`;
- the map attachments of geolocated alerts.

The five native image channels (Discord, Telegram, ntfy, Pushover, email) send the image, and every other
type gets a link. Nothing in any of these paths fetches third-party tiles. `getStaticMap` is `implemented`
and off `pending-operations.txt`.

**1. Natural Earth basemap.**
- **Script:** `scripts/build-basemap.py` (standard library only) downloads five 1:50m layers as GeoJSON from
  the Natural Earth vector repository at tag v5.1.2:
  - `land`, `lakes` and `coastline`;
  - `admin0` (`ne_50m_admin_0_boundary_lines_land`);
  - `admin1` (`ne_50m_admin_1_states_provinces_lines`).

  It quantizes to int32 microdegrees, delta-encodes each ring as zigzag varints, and writes
  `src/main/resources/basemap/ne50m.bin`.
- **The file:** 1,136,306 bytes, within the 4 MB target. Its part counts are 1421, 412, 1429, 393 and 592.
  The header is magic `FSBM` (Deviation 1), a u16 version, a layer table and per-part bounding boxes. The
  build never downloads it again.
- **`BasemapStore`** (`raster.render`) loads it lazily, once, and builds a coarse 10° cell index per layer
  for culling. It also handles boxes across ±180°.
- **Notices:** a `THIRD_PARTY_NOTICES.md` section "Static map basemap (prompt 055)" covers Natural Earth
  1:50m (public domain, attribution appreciated).
- **Operator basemap:** `foresignal.maps.basemap-url-template` (`FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, off
  by default; Deviation 2).
  - **Host check:** the template's host must resolve to a loopback or private address, through
    `OutboundUrlPolicy` profile `LAN_ONLY`.
  - **Fetching:** tiles go through `PinnedHttp`, only while a map is drawn: at most 16 tiles, 0.8 s each, no
    redirects, no cache.
  - **Fallback:** a public host such as `tile.openstreetmap.org` is refused and never contacted, and
    Natural Earth is drawn instead.

**2. `MapRenderer` (Java2D).**
- **Input:** `MapSpec` has a box, a width (at most 1280) and a height, the product layers with the frame
  chosen for each, the alert geometry, the place, range rings, the theme, the units and the zone of the
  local times.
- **Projection:** WebMercator. The box is snapped once, to 0.01° as `/image` and `/loop` snap, and widened
  to the map area's aspect.
- **Layers, in order:**
  1. water, land and lakes;
  2. the product frames, through the new `RasterRenderService.drawDirect` (same index maps and compiled
     styles as `/image`), at the product's `defaultOpacity` (0.8 when unset);
  3. admin-1, admin-0 and the coastline;
  4. the alert geometry, in the severity color with a translucent fill;
  5. the place marker, and range rings at half and the full radius;
  6. the scale bar.
- **Strips and notes:**
  - a header with each product's **frame valid time** in UTC plus the place's local time, for example
    "2026-10-12 21:04 UTC (16:04 CDT)";
  - a legend strip from `LegendService`'s ticks and segments, with units;
  - the attribution line, each product's structured attribution once plus "Basemap: Natural Earth";
  - the footer "Automated — not an official warning";
  - a missing or stale frame becomes a boxed note, for example "Reflectivity unavailable — not evidence of
    clear weather".
- **Rendering:** fonts are JDK `SansSerif` with antialiasing. The output is PNG-8 when the image has 256
  colors or fewer, otherwise PNG-24.
- **Budget:** `render()` runs on the `RenderQueue` at `INTERACTIVE` priority with a 2-s wait, through a new
  `render(…, Duration)` overload. Under load the answer is `503 RENDER_BUSY`.

**3. `GET /v1/maps/static`** (`StaticMapController`, `StaticMapService`).
- **Box precedence:** `bbox`, else `center` with `radiusKm` (default 50), else the alert geometry (centered
  on its envelope, with the radius as a minimum), else the place.
- **Products and frames:**
  - at most 4 products, with `style` per product;
  - `time` is a `TimeSpec` (`latest`, `now`, ISO or an offset), resolved by `SampleService.choose`;
  - `width` defaults to 800 and `height` to width × 0.75;
  - `theme` and `units` are supported.
- **Callers:**
  - A `place` needs a token (401 otherwise); an unknown one is `UNKNOWN_PLACE`.
  - An `alert` outside the caller's sources or restriction is `404 NOT_FOUND`, as with `getAlert`; the
    method carries `@RestrictedRead`.
- **Errors:** `UNKNOWN_PRODUCT`; `UNKNOWN_STYLE`; `RUN_REQUIRED` for a concrete ISO time on a forecast
  without `run`; `UNKNOWN_FRAME` for a concrete ISO time with no frame (an enabled product); and
  `VALIDATION_FAILED` for oversize requests and bad boxes.
- **Headers:** `Foresignal-Image-Bounds`, plus `Foresignal-Frame-Time` on non-immutable responses.
- **Cache rule** (Deviation 3): the ETag covers the frame keys (with their sha8), styles and the whole spec,
  and is checked before rendering.
- **AccessPolicy:** `GET /v1/maps/**` is `READ`.

**4. Alert attachments** (`AlertMapService`, `ChannelMapConfig`).
- **Channel config:** `attachMap` (`none` by default), `mapProducts` (default `mrms-reflectivity`, or the
  product of a `raster-frame` alert), `mapRadiusKm` (default 50) and `maxAttachmentBytes`, which defaults
  per type (8/10/15 MB, 5,242,880, 10 MB). They are validated by `ChannelInputParser`.
- **When an alert fires,** `NotificationRouter.fired` calls `prepare`. For each distinct
  (products, radius) among the accepting channels with `attachMap: static`, it starts one background job:
  1. wait up to 5 s for a frame of each product newer than `observed_at`, woken by
     `RasterFrameCommittedEvent`;
  2. render at 800 px;
  3. store media with `role=map`, `expires_at = now + 7 d`, linked to the alert's record.
- **Satellite passes:** a `satellite-pass` alert uses the pass composite's `thumbMediaId` instead of a map.
- **Sending:** `DeliveryWorker` calls `attach` for a fired alert's `send`.
  - **Hold:** while the job runs, the send is rescheduled every 0.5 s, up to 8 s after the notification was
    created, and never past the channel's `waitForEnrichmentSeconds` when that is set. The job started at
    fire time, so it runs alongside 030's wait.
  - **Size control:** 800 → 600 px (re-rendered from the same spec and stored once) → the 256 px thumbnail
    (`ThumbnailService`) → text.
  - **Record:** the level (`map-800`, `map-600`, `thumbnail`, `text-only`) and the media references
    (`[{filename, contentType, bytes, mediaId, url}]`) go on the delivery, in the new columns
    `attachment_level` and `attachments` jsonb. A retry reloads the bytes from `MediaStore`.
  - **Private payloads:** a message redacted by the private-payload rule gets no map, and private-source
    products are dropped for channels without `privatePayload`.
- **Message model:** `NotificationMessage.Attachment` gains `size` and `url` (the 4-argument form remains),
  plus `withAttachments`, `withLinks` and `image()`.

**5. Channel delivery.**
- **New `ChannelType` hooks:** `attachmentRefused(response)` and `followUps(message, settings)`. The worker
  resends the text at once when an image is refused, records `text-only`, and sends follow-ups after a
  successful send.

| Type | Implementation |
|---|---|
| `discord` | Multipart `payload_json` (embed `image.url = attachment://map.png`, `attachments[{id: 0, filename}]`) plus `files[0]`; `?wait=true` and edit-in-place are kept |
| `telegram` | `sendPhoto` multipart (`chat_id`, `photo`, `caption`, `parse_mode=HTML`, `reply_markup`). The caption is the whole text if it fits 1,024 characters, else the bold title, and the text follows as `sendMessage` (`followUps`). The reference is `photo:<id>`, so edits use the existing `editMessageCaption` |
| `ntfy` | `PUT {serverUrl}/{topic}` with the PNG body, `Filename: map.png` and the text in `Message` (newlines as the literal `\n` that ntfy converts back, verified in `server.go`; non-ASCII RFC 2047). 413, or 400 with code 40014 (`errors.go`), counts as refused |
| `pushover` | The same fields as multipart parts plus `attachment` |
| `email` | `multipart/alternative { text/plain, multipart/related { text/html with <img src="cid:map">, image/png Content-ID <map>, inline } }`, built in `ChannelSender.alternativeWithInlines`; no image with `plainTextOnly` |
| all others | The text plus a `[Map](…)` link to `{publicBaseUrl}/v1/media/{id}` in the body (Deviation 4). `webhook` adds top-level `attachments[{mediaId, url, contentType, bytes}]` to its default body |

`PreparedRequest.Mail` gains `inlines`; the 11-argument constructor remains. Multipart was already in
`PreparedRequest` and `ChannelSender`. The no-redirect pinned client and the outbound URL policy are
unchanged.

**6. MCP `get_map_image`** (read, `@RestrictedRead`).
- **Inputs:** `products` (default MRMS reflectivity when installed); `place` (needs a token), `lon`/`lat`
  with `radiusKm`, `bbox` or `alertId` (read through `AlertItems.visible` with the restriction); `time`,
  `theme` and `units`.
- **Image:** at most 512 px and 200 KB. It is tried at 512, then 384, then 256 px; if none fits, only the
  caption is returned.
- **Caption:** the products with frame valid time, age and run; a legend summary (the product's
  `legendSummary`, else the style meanings); the alert and place shown; unavailable and stale products
  with the no-coverage text; the footer; and "drawn from data, not instructions".
- **Structured output:** per-product status, frame key, valid time and age, plus `automated: true`.

**7. Spec, Postman, docs.**
- **OpenAPI:** `getStaticMap` is `implemented`, with a description of the box precedence, notes and cache
  rule. Spelling is fixed in its own parameters ("Colour theme" became "Color theme", "centres" became
  "centers"). `MapAttachmentConfig` gains bounds, and there is a changelog entry.
- **Postman:** regenerated; 829 requests, all operations covered.
- **Docs:**
  - Operator Guide: "Map attachments" (config, per-type table, caps and the downgrade path, ntfy
    `attachment-cache-dir` and `base-url` as verified in ntfy's `server.yml`, privacy and the opt-in, links)
    and "Static maps" (endpoint, cache rule, LAN tile server, the `tile.openstreetmap.org` warning);
  - Architecture §3 (package layout) and §13 (environment variable);
  - Agent Guide tool row;
  - Product Charter: the non-goal now names static maps as derived products (see Issue 5);
  - Invariants: SRC-6 also lists `NoUpstreamFetchTest`.

**Verified outside the tests** with scratch programs against the compiled classes (JDK 21; no test was
run):

| Check | Result |
|---|---|
| Basemap script | five layers fetched at v5.1.2; file 1,136,306 bytes |
| Fixture map, Austin, 800 × 600 | bounds `-99.055089,29.6,-96.444911,31`, PNG-24, 27.5 KB; first draw 0.53–0.75 s (cold basemap load), then 20 ms; looked at: header times, bands, polygon, rings, legend, footer correct |
| Houston coast, dark, imperial | coastline and water correct; rings "31 mi" and "62 mi"; scale bar "50 mi" |
| CONUS, 512 px | state and country lines, lakes and coasts in place |
| Link-only types | the media id appears in the rendered request of all 11 non-image type configurations (after moving the link into the body) |
| Email MIME | `multipart/alternative` → `text/plain` + `multipart/related` → `text/html` + `image/png` (`Content-ID: <map>`, inline, `map.png`) |
| Goldens | `src/test/resources/maps/full-light.png`, `full-dark.png` and `unavailable.png` were generated from `MapFixtures` with the compiled renderer |

## Files created / modified

Production, created:
- `raster/render/`: `BasemapStore`, `MapSpec`, `MapRenderer`, `StaticMapService`, `OperatorBasemap`;
- `api/StaticMapController`;
- `notify/`: `AlertMapService`, `ChannelMapConfig`;
- `mcp/tools/GetMapImageTool`;
- `resources/basemap/ne50m.bin`;
- `scripts/build-basemap.py`.

Production, modified:
- `raster/render/`: `RenderQueue` (wait overload), `RasterRenderService` (`drawDirect`);
- `notify/`: `NotificationMessage`, `NotificationJson`, `PreparedRequest`, `ChannelType`, `ChannelSender`,
  `DeliveryWorker`, `NotificationRouter`, `ChannelInputParser`;
- `notify/types/`: `DiscordChannel`, `TelegramChannel`, `NtfyChannel`, `PushoverChannel`, `EmailChannel`,
  `WebhookChannel`;
- `store/NotificationDeliveryEntity`;
- `api/mapper/ChannelMapper`;
- `security/AccessPolicy`;
- `application.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Architecture.md`, `docs/Agent-Guide.md`,
`docs/Product-Charter.md`, `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`,
`postman/Foresignal.postman_collection.json`, `src/test/resources/openapi/pending-operations.txt`.

Tests:
- Created:
  - `raster/render/`: `BasemapStoreTest`, `MapRendererTest`, `NoUpstreamFetchTest`, `MapFixtures`;
  - `raster/StaticMapEndpointIT`;
  - `notify/`: `AlertMapServiceIT`, `AttachmentDowngradeTest`;
  - `notify/types/`: `DiscordAttachmentTest`, `TelegramPhotoTest`, `NtfyAttachmentTest`,
    `PushoverAttachmentTest`, `EmailInlineCidTest`, `NonImageChannelLinkTest`;
  - `mcp/GetMapImageToolIT`;
  - goldens `src/test/resources/maps/*.png`.
- Modified: `NotifyFixtures` (`mapPng`, `withMap`, `withMapLink`), `AccessPolicyTest`.

## Deviations from the prompt

1. **Magic `FSBM`, not `ELBM`.** The prompt's `ELBM` is the pre-rename Elaro name. It is translated by
   analogy with the Roadmap §5 rule (`ELRG` → `FSRG`).
2. **The operator basemap is a configuration property.** It is `foresignal.maps.basemap-url-template`, not
   an app setting, because the spec defines no settings key for it. Its host is checked with
   `OutboundUrlPolicy` `LAN_ONLY` rather than `LocalAddresses`: since prompt 035, `LocalAddresses` says
   "do not use it to vet a destination", and `LAN_ONLY` is its outbound successor (loopback, RFC 1918,
   unique-local).
3. **The static-map cache rule.** A static map URL cannot carry each frame's `~sha8`. "Pinned" is
   therefore defined as:
   - a concrete ISO `time`;
   - complete, current frames;
   - a pinned (ISO) `run` for every forecast;
   - an explicitly versioned `style` for every product;
   - no `place`.

   A pinned map gets `immutable`; anything else gets a short max-age. The ETag includes the frame keys, so a
   reprocessed frame changes it. A reprocessed observation frame at a pinned time could still be cached
   immutably by a client (Issue 1).
4. **The link goes in the body, not a link button.** The prompt asks for text plus an in-app link on
   non-image types. A `Map` button was tried first, and Mattermost, Gotify, Apprise and Home Assistant
   notify mode rendered no buttons (checked against the compiled types). The link is now appended as
   `[Map](url)` to the body, which every type renders. A very long body truncated by a type can lose it.
5. **The 030 limit.** A send waits for its map at most 8 s (5-s frame wait plus the 2-s render budget, plus
   slack), and at most `waitForEnrichmentSeconds` when the channel has an enrichment wait. The job starts
   at fire time, so the two waits overlap rather than add up.
6. **`UNKNOWN_FRAME` and `RUN_REQUIRED`** are errors only for a concrete ISO `time`. An alias (`latest`,
   `now`) or an offset with no usable frame draws the unavailable note instead, so maps never silently omit
   a product, as the prompt requires.
7. **Push (fan-out) channels** get the link in the message text like the other link types. The Web Push,
   APNs and FCM payload builders do not render it specially.
8. **Discord edits in place** send the JSON embed. Discord keeps the uploaded file, so an edited alert shows
   the map as a file rather than inside the embed.
9. **Range rings** are drawn at half and the full map radius around a place. The prompt names
   `rangeRingsKm[]` in `MapSpec`, but neither the endpoint nor the tool has a rings parameter in the spec.

## Issues found / open questions

**Needs the owner:**

1. **The immutable rule for static maps (Deviation 3).** Should pinned maps also require a frame token, for
   example by accepting a frame key in `time` when one product is drawn, or should they never be immutable?
2. **Telegram's caption limit is counted on the raw HTML length,** which is stricter than Telegram's count
   (entities excluded). Captions near 1,024 visible characters move to the follow-up earlier than strictly
   needed.

**Other findings:**

3. **`docs/OpenAPI.json` has British spellings in other operations' descriptions:** "Centre point" (four
   operations), "centre latitude", "Colour theme" (two other operations), "colour as aabbggrr" and
   "Radius centre". They are outside this prompt's operation and were left for the owner or the prompts that
   own them. The `telecom.datacentre` enum value is wire data and must not change.
4. **Map media is linked to the alert's record,** so `MediaRetentionStep`'s head-missing rule can delete it
   24 h after that record's head disappears, before the 7 days. This is the existing media rule, unchanged.
5. **Product Charter.** The prompt asked to confirm that the non-goal covers static maps. It did in
   substance ("hosts products derived from data it ingests; never proxies basemaps"); one sentence now names
   static maps explicitly. The Charter is the owner's document; revert it if unwanted.
6. **No instructions directed at an agent** appeared in the Natural Earth data, ntfy's source, or anything
   else read.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/render/BasemapStoreTest` | five layers and their part counts; decoded points inside their boxes; cell culling around Texas and the open ocean; antimeridian query without duplicates; a hand-built file decoded exactly; wrong magic, version and truncation refused |
| `raster/render/MapRendererTest` | light, dark and unavailable goldens (≤ 1 % of pixels differing by more than 48 per channel); unavailable and stale notes; footer pixels and the attribution text; header times UTC plus CDT; fitted box aspect and center; PNG-8 versus PNG-24; rendering through a real `RenderQueue`; destination, nice-scale and label helpers |
| `raster/render/NoUpstreamFetchTest` | unset operator basemap: zero interactions with `PinnedHttp` and the policy, and zero WireMock requests; `tile.openstreetmap.org` refused, never contacted, Natural Earth drawn; template checks; a LAN (WireMock) tile server fetched and drawn |
| `raster/StaticMapEndpointIT` | center map size, bounds, `max-age=60`, frame time and 304; pinned immutable versus unversioned; default 800 × 600 and the 1280 caps; bad boxes and parameters; `UNKNOWN_PRODUCT`, `RUN_REQUIRED`, `UNKNOWN_FRAME`, `UNKNOWN_STYLE`, unknown alert; an alias with no frame still 200; place needs a token, `private`, `UNKNOWN_PLACE`; 503 `RENDER_BUSY` with `Retry-After: 1` while the queue is saturated by blocking jobs |
| `notify/AlertMapServiceIT` | waits for a newer frame and is woken by the commit (under 5 s); media is `role=map`, 800 px, expires in 7 d, record-linked; one job and one media id for Discord, Telegram and a Slack link; satellite-pass thumbnail; held at +1 s and released at the 3-s enrichment wait, 8 s without one; a small cap gives a smaller image and 1 KB gives text only; no map without `attachMap`, for a redacted message, an update or a geometry-less alert |
| `notify/AttachmentDowngradeTest` | 800 → 600 → thumbnail → text by cap; failing variants give text; pass thumbnail; default caps; config defaults; validation per field; body link |
| `notify/types/DiscordAttachmentTest` | part order and names, `payload_json` embed image and `attachments`, `files[0]`; on the wire to WireMock (multipart parts, binary body); unchanged JSON without an image |
| `notify/types/TelegramPhotoTest` | `sendPhoto` parts, whole-text caption; long text gives a title caption and a `sendMessage` follow-up; `reply_markup`; the wire; `photo:<id>` reference and `editMessageCaption` |
| `notify/types/NtfyAttachmentTest` | PUT body, `Filename` and `Message`; header encoding; 413 and 40014 refusals; a 413 on the wire, then the text fallback with the link |
| `notify/types/PushoverAttachmentTest` | multipart fields equal the form fields, plus `attachment`; the wire |
| `notify/types/EmailInlineCidTest` | `cid:map` and the inline image; none for plain text only; the GreenMail structure alternative → related → html + png `<map>` |
| `notify/types/NonImageChannelLinkTest` | the media link in all 11 non-image configurations, no attachment capability; webhook `attachments` metadata; the five image types declare the capability |
| `mcp/GetMapImageToolIT` | image ≤ 512 px and ≤ 200 KB with a caption (product, valid time, age, legend, footer, data note); unavailable product in the caption; place needs a token; missing location is an error; read scope |
| `AccessPolicyTest` (added) | `GET /v1/maps/static` is `READ`; POST falls to admin |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered.
- No new dependency.

## Notes for the next prompt

1. **056 (weather settings, alert weather context):**
   - `AlertMapService.prepare` already runs at fire time from `NotificationRouter.fired`; a weather-context
     computation can follow the same pattern;
   - `StaticMapService.alertGeometry` parses an alert's snapshot geometry.
2. **057 checkpoint:**
   - Context sharing:
     - the new web ITs share the `RasterTestSources` context: `StaticMapEndpointIT`, `AlertMapServiceIT`,
       which builds `AlertMapService` by hand with Mockito stand-ins, and `GetMapImageToolIT`;
     - the attachment type tests are plain unit tests with WireMock or GreenMail, without Spring.
   - Behavior to check:
     - `StaticMapEndpointIT.renderBusy` blocks every render thread for about 2 s;
     - `AlertMapServiceIT.neverPastEnrichmentWait` relies on the job still waiting (it waits 5 s of real
       time), so the assertions must finish within 5 s;
     - a first map draw loads the basemap, about 0.5–0.8 s;
     - golden tolerance is 1 %; if fonts differ more on the Mac, regenerate the goldens from `MapFixtures`
       on purpose.
3. **058 (map style):** the MapLibre style is `/v1/map/style.json` (singular `map`). `/v1/maps/**` is only the
   static map rule.
4. **143:** measure:
   - static map render time under load with real MRMS frames (the 2-s budget);
   - the attachment sizes per channel against the default caps;
   - the operator basemap with a real LAN tile server, if the owner runs one.
5. **144 (Flyway):**
   - `notification_delivery` gains `attachment_level varchar(16)` and `attachments jsonb`;
   - `media` rows with `role = 'map'` expire after 7 d.
6. **Owner decisions pending:** Issues 1–2 and 5 above, plus those carried from 012–054.
