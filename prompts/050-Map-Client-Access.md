# 050 — Map Client Access

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/050-CR-Map-Client-Access.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/049-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (rows "Immutable caching", "Map-client auth"), §6.1 (cache policy), §6.3 (render queue), §6.7 (access for map clients), §6.8 (SSE resume pattern), §6.10 (client recipes), §10.4 (client items to verify). Design prompt 037 = v1.2 050 (Roadmap §5).
- `docs/Gap-Analysis.md` §1 item 5, §6 rows "Stream tickets + feed keys", "Restricted tokens + anonymous restriction profile", "Remote access / TLS".
- Completion Reports of **023** (stream tickets, restricted tokens, trusted proxies, private gate), **035** (feed keys, the restriction enforcement point, per-client limits, auth-failure backoff), **045** (cache policy, render queue, TileJSON), **015** (SSE, `/v1/events/stream`) and **021** (Operator Guide remote-access section).
- `docs/Architecture.md` §14 (deployment), §15 (tokens and scopes); `src/main/java/ai/elaro/config/CorsFilter.java`, `security/TokenFilter.java`, `security/AccessPolicy.java`.

## Objective

Let every map client reach Elaro's rasters, record tiles and change streams when they cannot send an `Authorization` header (MapLibre Native/maplibre_gl, Cesium, browser `EventSource`), without weakening the access model or the cache model. Tile keys are **not a new credential system**: they are the path-segment variant of the feed keys from 035, enforced by 035's single restriction enforcement point. Add the CORS header set, a Caddy `web` compose profile for HTTP/2/3, and a frontend guide with per-client recipes.

## Tasks

### 1. Tile keys as a feed-key variant (`ai.elaro.access`)

- Read the 035 Completion Report and locate the feed-key model (on `api_token`, per 023/035) and the restriction enforcement point that every read path consults. **Reuse both.** Do not add a second token table, hasher or enforcement path. If 035 stored feed keys elsewhere, follow it and record the mapping in the CR.
- Extend the feed-key model (add columns only if missing):
  - `carrier`: `query | path` (035's feed keys are typically bound to one path and carried as a query parameter; a tile key uses `path`)
  - `prefixes text[]`: one or more canonical path prefixes the key may read
  - plaintext prefix `elk_` for path-carrier keys (035's own prefix stays for its keys), scope fixed to `read`, `expires_at` required and ≤ 30 days
- **Allowed prefixes** (`TileKeyPolicy.ALLOWED_PREFIXES`; anything else is 400 `VALIDATION_FAILED` at creation): `/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`, `/v1/maps/static`, `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream`, `/v1/events/stream`. Endpoints that ship later (`/v1/maps/static` in 055, `/v1/sample` in 052) are accepted now and resolve to 404 until they exist.
- Tile keys may also carry 023/035 restrictions (sources, kinds, bbox). The enforcement point applies them to MVT, records-derived responses and streams exactly as for a restricted token.
- **Private sources:** a tile key never grants access to `private` sources (039's gate treats it like the anonymous profile). Document this.
- **API** (tag `Tokens`, scope `agent`):
  - `POST /v1/tile-keys {name, prefixes[], ttl ≤ 30d, restrictions?}` → 201 `TileKeyCreated` with the plaintext `elk_…` key once, plus `urlBase` (`{publicBaseUrl}/v1/k/{key}`)
  - `GET /v1/tile-keys` (no plaintext; prefix, name, prefixes, expiresAt, lastUsedAt), `DELETE /v1/tile-keys/{id}` (204)
  - Implement these as a thin facade over 035's feed-key service filtered to `carrier=path`. Revocation and expiry behave like tokens (cached lookups are invalidated on revoke within 5 s).

### 2. `TileKeyFilter` (path-segment carrier)

- A servlet filter ordered **before** `TokenFilter`, matching `/v1/k/{key}/**`:
  1. Only `GET`/`HEAD` (and CORS `OPTIONS`); anything else → 405.
  2. Resolve the key through 035's feed-key lookup (constant-time hash compare). Unknown, revoked or expired → 401 `INVALID_TILE_KEY`; count it against 035's auth-failure backoff for the client IP.
  3. Canonical path = `/v1/` + remainder. It must start with one of the key's `prefixes`, else 403 `INSUFFICIENT_SCOPE`.
  4. Forward internally (`RequestDispatcher.forward` or a wrapped request) to the canonical path with an authenticated principal of type `TILE_KEY` (id, name, restrictions). Downstream code sees the canonical URI, so caching, ETags, controllers and the enforcement point are unchanged.
- `TokenFilter` must accept the forwarded principal and skip bearer parsing for it; `AccessPolicy` evaluates the canonical path (READ only).
- **Per-client fairness:** the render queue (045) and 035's per-client limits key on the tile-key id when present (then token, then IP). Verify this wiring; add it if missing.
- **Streams:** `/v1/k/{key}/changes/stream` and `/v1/k/{key}/events/stream` work for `EventSource` (resume via `Last-Event-ID` unchanged). `PATCH` on the multiplexed filter is not allowed through a tile key; clients reconnect with new query parameters instead. 023 stream tickets remain the option for clients that hold a bearer token and want short-lived URLs.
- **Leak hygiene:** responses on `/v1/k/**` carry `Referrer-Policy: no-referrer`. Access logs, request logging, `FetchTrace`-style redaction and error ProblemDetails replace the key segment with `elk_xxxx…` (first 8 characters).
- **Caching:** the response for `/v1/k/{key}/X` uses the cache rule of canonical `X` (045, design §6.1). `public` only when `openReads=true` covers `X`; otherwise `private`. The path segment keeps the cache key stable for the key's lifetime.

### 3. CORS set

Extend `CorsFilter` (keep the configurable origin list from 003):
- `Access-Control-Expose-Headers: Elaro-Frame-Time, Elaro-Image-Bounds, Elaro-Frame-Expired, Retry-After, Link, ETag`
- `Access-Control-Allow-Headers: Authorization, If-None-Match, Elaro-Prefetch` (plus any already allowed)
- `Access-Control-Max-Age: 86400`
- Applies to `/v1/k/**` too. Document that `?prefetch=1` avoids a preflight that the `Elaro-Prefetch` header would cause.

### 4. Compose profile `web` (Caddy)

- `docker-compose.yml`: service `caddy` (`caddy:2.11.7-alpine`, verified current 2026-10), `profiles: [web]`, ports `443:443/tcp`, `443:443/udp` (HTTP/3) and `80:80`, volumes `caddy-data`, `caddy-config`, `./deploy/caddy/Caddyfile:/etc/caddy/Caddyfile:ro`, and an optional `./deploy/web:/srv/web:ro` for a separately built web client.
- `deploy/caddy/Caddyfile`: site `{$ELARO_HOSTNAME:elaro.local}` with `tls internal` (default) and a commented DNS-01 block (needs a Caddy build with the DNS provider module; document `xcaddy`, do not ship it). `reverse_proxy elaro:8080` with `flush_interval -1` for SSE; `encode zstd gzip` excluding `image/png`; `handle /v1/* /mcp* /actuator/health*` → Elaro; everything else → `file_server` from `/srv/web` with SPA fallback, so the bundled client is same-origin (no preflights).
- Configure 023's trusted-proxy setting for the compose network in the docs so forwarded headers are honoured; plain `:8080` stays for agents and tools.

### 5. Frontend guide (`docs/Frontend-Guide.md`, new)

Write recipes exactly per design §6.10, with these verified facts (2026-10): MapLibre GL JS latest 6.13.0 (ESM-only, WebGL2); maplibre_gl (Flutter) 0.27.1; flutter_map 8.3.2 with built-in `NetworkTileProvider` caching since 8.2.0 (honours `Cache-Control`; tiles still load if caching fails); Cesium 1.146.
- **All clients:** take concrete templates from TileJSON, `frames/latest`, `/loop` or SSE; never put `latest` in a layer template; set source `maxzoom`/`bounds` from TileJSON; `@2x` when `devicePixelRatio ≥ 2` with `tileSize: 256`; honour 503 `RENDER_BUSY` + `Retry-After` with a retry wrapper (give the MapLibre `transformRequest`/fetch wrapper and the flutter_map provider wrapper as code); drop frames past `expiresAt`; the resume pattern (`GET /v1/changes?after=<seq>&kind=raster-frame`, then reconnect with `Last-Event-ID`).
- **Per client:** MapLibre (`setTiles` per frame event, `/loop` or N preloaded sources at opacity 0); maplibre_gl (tile-key URLs, barb MVT, `/loop` image sources with returned `coordinates`); flutter_map (`{r}` + `RetinaMode.isHighDensity`, `maxNativeZoom`, bounds-limited `TileLayer`, `OverlayImageLayer` loops, **avoid `flutter_map_tile_caching` (GPL-3.0)**); Cesium (`UrlTemplateImageryProvider`, `WebMercatorTilingScheme`, `maximumLevel` = native zoom, `rectangle` = bounds).
- Legends from the JSON legend (native) or PNG (web); attribution always from the structured object (`RichAttributionWidget` in flutter_map).
- **On plain HTTP/1.1:** use `/loop` for animation and one shared SSE connection per web client (SharedWorker).
- Tile-key operations: mint per device, rotate weekly, keys appear in proxy logs (read-only and prefix-limited by design). Leave a "Wind particles" heading for 051.

### 6. Operator Guide and docs

- `docs/Operator-Guide.md` "Remote access & TLS" (extend 021's section): the `web` profile, exporting Caddy's local root (`/data/caddy/pki/authorities/local/root.crt`) and installing it on macOS, Windows, Linux, Android (user CAs are not trusted by apps targeting API 24+ unless the app's `network_security_config` allows them; Flutter clients must ship that config) and iOS (install profile, then enable full trust under Certificate Trust Settings).
- `docs/Architecture.md` §15: tile keys as the path-carrier feed-key variant.

### 7. Spec, Postman

- `docs/OpenAPI.json`: `/v1/tile-keys` (GET, POST), `/v1/tile-keys/{tileKeyId}` (DELETE) with `x-elaro-scope: agent`; schemas `TileKey`, `TileKeyCreate`, `TileKeyCreated`; document the `/v1/k/{key}/…` carrier in the `info.description` and on the Products tag (it is a transport, not separate operations); problem codes `INVALID_TILE_KEY` (401).
- `AccessPolicy`: AGENT for POST/DELETE `/v1/tile-keys/**`, AGENT for GET `/v1/tile-keys`; `/v1/k/**` is handled by the filter (deny anything the filter has not forwarded).
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`); add a `BODIES` example for `POST /v1/tile-keys`.

## Tests to write (do not run)

- `TileKeyServiceIT`: create (plaintext once, `elk_` prefix, hash stored), TTL > 30 d rejected, disallowed prefix rejected, revoke, expiry; keys stored through 035's feed-key model (no new table).
- `TileKeyFilterIT`: valid key forwards to the canonical path and returns the same body and ETag as a bearer request; wrong prefix → 403; POST → 405; unknown/revoked/expired → 401 `INVALID_TILE_KEY` and auth-failure backoff increments; restrictions (bbox, kinds) applied to `/v1/tiles` MVT and `/changes/stream`; private-source records never returned.
- `TileKeyCacheHeadersIT`: content-addressed tile via key → `immutable` with `private` when `openReads=false` and `public` when true; alias URL gets the short max-age.
- `TileKeySseIT`: `EventSource`-style GET on `/v1/k/{key}/changes/stream` streams events and resumes with `Last-Event-ID`.
- `TileKeyRedactionTest`: logs and ProblemDetails never contain the full key; `Referrer-Policy` present.
- `CorsFilterTest` (extend): expose/allow headers and max-age on a preflight to `/v1/k/**` and `/v1/products/**`.
- `RenderQueueFairnessTest` (extend 045's): fairness key prefers tile-key id.
- `ComposeConfigTest`: parses `docker-compose.yml` and asserts the `web` profile, UDP 443 and the Caddyfile mount.

## Acceptance criteria

- A MapLibre or `EventSource` client with only a URL can read tiles and streams under `openReads=false`, limited to the key's prefixes and restrictions.
- Exactly one credential lookup and one enforcement point serve bearer tokens, feed keys and tile keys.
- Cache headers for keyed URLs match the canonical URL's rule.
- `docs/Frontend-Guide.md` exists with all §6.10 recipes; the `web` profile starts Caddy with `tls internal`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `050: Map Client Access`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/050-CR-Map-Client-Access.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `050: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
