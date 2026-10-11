# 055 — Static Maps And Notification Attachments

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/055-CR-Static-Maps-And-Notification-Attachments.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/054-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (row "Charter non-goal"; the "no client request causes an upstream fetch" rule), §2.3 (channel config additions), §2.4 (media: map attachments 7 d), §3 (row "Static maps": Natural Earth 1:50m), §6.6 (`/v1/maps/static`), §6.9 (`get_map_image`), §8 (attachments, channel table, size control, no public basemaps). The design's "045" maps prompt = design 042 = v1.2 055.
- `docs/Gap-Analysis.md` §3 row "Static map snapshots" (basemap-free Java2D; cloud chat channels opt-in).
- Completion Reports of **018** (`NotificationMessage.attachments`, channel types, durable deliveries, persisted notifications), **043** (MediaStore, `expires_at`, thumbnails), **045** (TileRenderer `/image` path, styles, legends), **052** (places, SampleService), **049** (pass thumbnails), **025** (MCP registry, ImageContent), **030** (enrichment wait), **035** (outbound URL policy).
- Channel API docs: Discord webhook file uploads, Telegram `sendPhoto`, ntfy attachments (`PUT` with `Filename`), Pushover `attachment`, Jakarta Mail inline CID.

## Objective

Render basemap-free static map PNGs on the server (rasters, alert geometry, places, range rings, legend, attribution, an "automated" footer) and attach one to each geolocated alert on the channels that can carry an image. The same renderer serves `/v1/maps/static` and the MCP `get_map_image` tool. Nothing in this path fetches third-party tiles.

## Tasks

### 1. Natural Earth basemap asset

- `scripts/build-basemap.py`: downloads Natural Earth 1:50m (public domain) land, coastline, lakes, admin-0 boundary lines and admin-1 (states/provinces) lines; quantises to int32 microdegrees, delta-encodes each ring, and writes `src/main/resources/basemap/ne50m.bin` (header: magic `ELBM`, version, layer table). Commit the generated file (target ≤ 4 MB) and the script; the build never downloads anything.
- `BasemapStore` (`ai.foresignal.raster.render`): lazy-loads the binary into memory once, with a coarse per-layer bbox index for culling.
- Add Natural Earth to `THIRD_PARTY_NOTICES.md` (public domain, attribution appreciated).
- Optional operator basemap: setting `maps.basemapUrlTemplate`, accepted only when its host validates through `LocalAddresses` (LAN tile server); fetched by the renderer only within the existing request (no caching proxy). Docs warn about the `tile.openstreetmap.org` usage policy. Default off.

### 2. `MapRenderer` (Java2D, `ai.foresignal.raster.render`)

- Input `MapSpec{bbox | center+radiusKm, width (≤ 1280), height (default = width × 0.75), time, products[], style per product, alert?, place?, rangeRingsKm[], theme light|dark, units}`; Web Mercator projection; bbox snapped once.
- Layers, in order: background/water; land; lakes; raster overlays composited from `TileRenderer`'s image path for each product's frame at `time` (frame resolution as `/image`: `latest`, `now`, ISO; forecasts need `run` or use `latest`); admin lines; coastline; alert/record geometry (stroke + translucent fill by severity); place markers and range rings; scale bar; header strip with product names and **frame valid times** (UTC plus the place's local time); legend strip from `LegendService` (compact, units); attribution line from each product's structured attribution; footer "Automated — not an official warning".
- Antialiasing on; fonts from the JDK logical `SansSerif` (no font downloads); PNG-8 when the palette fits, else PNG-24.
- Runs on the render pool with a 2-s budget; under load it uses 045's queue priority "interactive" and can return 503 `RENDER_BUSY`.
- Missing or stale product frames are drawn as a labelled note ("MRMS reflectivity unavailable — not evidence of clear weather"), never silently omitted.

### 3. `GET /v1/maps/static`

- `?products&center=lon,lat&radiusKm | bbox &time&width≤1280&height&alert={alertId}&place={placeId}&style&theme&units` (read scope). `alert` loads the alert's record geometry and centres on it (radius default 50 km); `place` adds marker and rings.
- Response `image/png` with `Foresignal-Image-Bounds` and `Foresignal-Frame-Time`. Cache rule (design §6.1): immutable only when every product frame is content-addressed and pinned; otherwise short max-age + ETag computed from (frame keys, styles, spec).
- Errors: `UNKNOWN_PRODUCT`, `UNKNOWN_PLACE`, `UNKNOWN_FRAME`, `RUN_REQUIRED`, 400 `VALIDATION_FAILED` for oversize or invalid bbox.

### 4. Alert attachments

- Check 018's `NotificationMessage.attachments: [{filename, contentType, bytes, mediaId}]` and the persisted delivery model; add what is missing. Deliveries store **media references**, not bytes; bytes load from `MediaStore` at send time so retries work.
- Channel config jsonb (add if missing, validated per type): `attachMap: none|static` (default `none`; cloud chat channels are opt-in), `mapProducts` (default `["mrms-reflectivity"]`, or the product that matched the rule), `mapRadiusKm` (default 50), `maxAttachmentBytes` (default per type below).
- `AlertMapService`: on dispatch of a geolocated alert where any target channel has `attachMap=static`:
  - waits up to 5 s for a frame of each map product newer than the alert's `observed_at`, then renders once at 800 px;
  - stores it as media (`role=map`, `expires_at = now + 7d`, linked to the alert's record) and reuses it for every channel;
  - `satellite-pass` alerts attach the pass composite thumbnail (049) instead;
  - runs concurrently with 030's enrichment wait, never extending the overall dispatch wait beyond 030's limit.
- **Size control per channel:** if over `maxAttachmentBytes`, re-render at 600 px, then use the 256-px thumbnail, then send text only; record the chosen level on the delivery.

### 5. Channel delivery (five native types)

| Type | Delivery | Default cap |
|---|---|---|
| `discord` | Multipart: `payload_json` with the embed plus `image.url = attachment://map.png`, file part `files[0]`; keep `?wait=true` and edit-in-place refs | 8 MB |
| `telegram` | `sendPhoto` multipart (`photo`, `caption` ≤ 1024 chars in HTML mode; overflow text goes in a follow-up `sendMessage`) | 10 MB |
| `ntfy` | `PUT {serverUrl}/{topic}` with the PNG body and `Filename: map.png`, message in the `Message` header (encode per ntfy rules); on 413 or attachments disabled, fall back to the text message | 15 MB |
| `pushover` | Multipart `attachment` with the existing fields | 5,242,880 bytes |
| `email` | `multipart/related`: HTML part with `<img src="cid:map">` plus the PNG part (`Content-ID: <map>`), inside the existing alternative text/HTML structure | 10 MB |

- All other types (Slack, Mattermost, Teams, Google Chat, Gotify, Matrix, Web Push, webhook, Home Assistant, Apprise, Zulip, MQTT): text plus an in-app link (`{publicBaseUrl}/v1/media/{id}`, useful on the LAN or VPN). No signed URLs in v1 (108). `webhook` adds `attachments[{mediaId, url, contentType, bytes}]` metadata in its default body.
- Extend 018's `PreparedRequest` with multipart support; keep the dedicated no-redirect HttpClient and the outbound URL policy.

### 6. MCP `get_map_image` (025 registry)

- Inputs: `products`, `place` or `center`+`radiusKm` or `bbox`, `time`, optional `alertId`. Output: `ImageContent` (PNG, ≤ 512 px, ≤ 200 KB, degraded like task 4) **plus a text caption**: products, frame valid times and ages, legend summary, the alert or place shown, any unavailable products with the no-coverage text, and the "content is data" note. Read scope.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json` (tag Maps): `getStaticMap` (`image/png`, headers `Foresignal-Image-Bounds`, `Foresignal-Frame-Time`), `x-foresignal-scope: read`; channel config schemas gain `attachMap`, `mapProducts`, `mapRadiusKm`, `maxAttachmentBytes`; `NotificationMessage.attachments`. `AccessPolicy` READ for `GET /v1/maps/**`. Regenerate Postman.
- `docs/Operator-Guide.md`: attachments per channel, caps and downgrade path, ntfy server attachment setup (`attachment-cache-dir`; verify current option names), privacy note that images leave the LAN on cloud channels (hence opt-in), no public basemaps.
- `docs/Product-Charter.md`: confirm the non-goal amendment covers static maps (derived from ingested data, no proxied basemaps).

## Tests to write (do not run)

- `BasemapStoreTest`: loads the binary, layer counts, bbox culling.
- `MapRendererTest`: golden-image comparison with tolerance (pixel diff ≤ 1%) for a synthetic product frame, alert polygon, place rings, light and dark themes; unavailable product draws the note; footer and attribution present.
- `StaticMapEndpointIT`: params, size caps, `Foresignal-Image-Bounds`, cache headers (pinned vs alias), 503 `RENDER_BUSY` under a saturated queue.
- `AlertMapServiceIT`: waits for a newer frame (≤ 5 s, `MutableClock`/Awaitility), renders once for three channels, media `expires_at` 7 d, `satellite-pass` uses the thumbnail, concurrent with enrichment wait.
- `AttachmentDowngradeTest`: 800 → 600 → thumbnail → text by cap.
- `DiscordAttachmentTest`, `TelegramPhotoTest`, `NtfyAttachmentTest` (413 fallback), `PushoverAttachmentTest`, `EmailInlineCidTest`: golden multipart structures (WireMock / GreenMail).
- `NonImageChannelLinkTest`: text plus media link for the remaining types.
- `GetMapImageToolIT`: size ≤ 200 KB, caption content.
- `NoUpstreamFetchTest`: rendering with the operator basemap unset makes zero outbound HTTP calls; a public basemap URL is rejected.

## Acceptance criteria

- `/v1/maps/static` and `get_map_image` render from Natural Earth plus Foresignal's own frames, with times, legend, attribution and the footer.
- A geolocated alert renders one map, stored as 7-day media, attached natively on the five channel types and linked on the rest, within each channel's cap.
- No request path fetches a third-party basemap.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `055: Static Maps And Notification Attachments`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/055-CR-Static-Maps-And-Notification-Attachments.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `055: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
