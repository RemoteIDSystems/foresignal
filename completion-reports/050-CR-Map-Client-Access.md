# 050-CR-Map-Client-Access

| | |
|---|---|
| Prompt | 050-Map-Client-Access.md |
| Status | Complete |
| Implementation commit | f216cd701953b1d81804dfc07eb2a2af8618551b |
| Date | 2026-10-12 |

## Summary

Map clients that cannot send an `Authorization` header (MapLibre Native/maplibre_gl, Cesium, browser
`EventSource`) can now read rasters, record tiles and change streams through **tile keys**. Tile keys
are not a new credential system: they are 035's feed-key model and 035's single `PathKeyFilter`, with
one new resolver.

**1. Tile keys** (`ai.foresignal.access`)
- They are `api_token` rows with `type = tile_key` (`ApiTokenEntity.TYPE_TILE_KEY`), next to 035's
  `feed_key`. There is no new table, hasher or enforcement path.
- `TileKeyPolicy` holds `KEY_PREFIX = "fsk_"`, `MAX_TTL = 30 d` and the nine `ALLOWED_PREFIXES`.
- `TileKeyService` is a thin facade over `FeedKeyService`, which gained shared methods: `mint`,
  `listOfType`, `revokeOfType` and `resolveOfType`. That last one is the one constant-time lookup, and
  035's own feed-key paths now go through it too.
- Validation of the create request:
  - `name` is 1–200 characters;
  - `prefixes` is a non-empty set taken exactly from the allowed list;
  - `ttl` is an ISO or shorthand duration (`Durations.parsePositive`), at most 30 days;
  - `restrictions` is optional, with the same members as a read token's. `endpoints`, `placeIds` and
    `excludePrivate: false` are refused.
- Every tile key has scope `read` and no private grant.
- `TileKeyService` is also the `PathKeyResolver` for `fsk_`. It binds a principal of the new kind
  `RequestPrincipal.Kind.TILE_KEY`, with private sources always excluded and sensitive alerts hidden.
- API (`TileKeysController`, tag `Tokens`, scope `agent`):
  - `GET /v1/tile-keys` returns a cursor page, newest first, without key values.
  - `POST /v1/tile-keys` returns 201 `TileKeyCreated` with the `fsk_…` key and
    `urlBase = {publicBaseUrl}/v1/k/{key}`, once.
  - `DELETE /v1/tile-keys/{id}` returns 204.
- Lookups are not cached, so a revoked key fails on the next request, and its open SSE streams are reset.

**2. The path-segment carrier.** Following Conventions, there is no separate `TileKeyFilter` (see
Deviation 1); 035's `PathKeyFilter` was extended instead.
- `PathKeyResolver` gained per-family refusal codes as default methods. For tile keys:
  - unknown, expired or revoked → `401 INVALID_TILE_KEY` (a new error code);
  - a path outside the prefixes → `403 INSUFFICIENT_SCOPE`;
  - feed keys keep `401 INVALID_FEED_KEY`, `410 FEED_KEY_GONE` and `403 RESTRICTED`.
- These are unchanged: GET/HEAD only (405 otherwise, including PATCH on the stream filter), the unsafe
  path checks, auth-failure backoff on every refusal, and the forward to the canonical path.
- `TokenFilter` accepts any path-key principal (`RequestPrincipal.isPathKey()`), skips bearer parsing,
  and allows only `read` operations on the canonical path.
- The tile-key principal is keyed `tilekey:<id>` (`RequestPrincipal.clientKey()`). The render queue's
  fairness key (`RasterTilesController.clientKey`), 035's rate limiter and the SSE hub all use it.
  `SseHub.onTokenRevoked` also resets `tilekey:` streams, and `ChangesTopic` treats them as non-token
  callers.
- `SourceVisibility.visible` and `HeaderStreamPrincipalResolver` treat tile keys like feed keys.
  `TokenService` keeps every path key out of the token APIs and out of bearer authentication.
- **Leak hygiene:**
  - `PathKeyFilter.redact` (the Tomcat access-log attribute) now keeps the key's first eight characters
    (`/v1/k/fsk_AbCd…/…`), which is the same prefix the listings show. Keys under 16 characters are
    replaced entirely.
  - The new `config/RedactingErrorAttributes` redacts `path` in Spring Boot's `/error` attributes.
  - `Referrer-Policy: no-referrer` and `X-Robots-Tag: noindex` are on every `/v1/k/**` response, as
    in 035.
- **Caching:** `Cache-Control: private` stays the filter's default. A controller's own cache headers
  replace it: Spring 7's `ServletResponseHeadersAdapter.put` does `remove` then `addHeader`, which was
  verified in the `spring-web` 7.0.9 bytecode. So a keyed URL gets the canonical URL's rule, and is
  `public` only while `openReads` is on and the key has no data restriction (`ReadCaller.publicCache`).

**3. CORS** (`CorsFilter`; it runs before `PathKeyFilter`, so `/v1/k/**` is covered)
- Exposed headers: `Location`, `Mcp-Session-Id`, `Foresignal-Frame-Time`, `Foresignal-Image-Bounds`,
  `Foresignal-Frame-Expired`, `Retry-After`, `Link`, `ETag`.
- Allowed headers: `Authorization`, `Content-Type`, `Accept`, `Last-Event-ID`, `If-None-Match`,
  `Foresignal-Prefetch`, `Mcp-Session-Id`, `MCP-Protocol-Version`.
- `Access-Control-Max-Age: 86400`.
- This resolves 045's Issue 2.

**4. Compose profile `web`**
- `docker-compose.yml` has a `caddy` service: `caddy:2.11.7-alpine` (the tag was checked on Docker Hub),
  `profiles: [web]`, ports `80:80`, `443:443/tcp` and `443:443/udp`, volumes `caddy-data` and
  `caddy-config`, the read-only Caddyfile mount, and `./deploy/web:/srv/web:ro` (the directory is kept
  with a `.gitkeep`).
- `deploy/caddy/Caddyfile`:
  - site `{$FORESIGNAL_HOSTNAME:foresignal.local}` with `tls internal`, and a commented DNS-01 block
    with `xcaddy` instructions;
  - an access log that keeps eight characters of a path key and strips `?ticket=`;
  - `encode zstd gzip` with an explicit `match` list (no PNG, no SSE);
  - `/v1/* /mcp* /actuator/health*` go to `reverse_proxy foresignal:8080` with `flush_interval -1`;
  - everything else is `file_server` from `/srv/web`, with a single-page-app fallback.

**5. Frontend guide.** `docs/Frontend-Guide.md` is new. It covers:
- getting a tile key and using it, with a `keyed()` URL helper;
- the rules for every client: concrete templates and never `latest`, maxzoom and bounds, `@2x` with
  `tileSize: 256`, `503 RENDER_BUSY` retries honoring `Retry-After`, `?prefetch=1`, expiry, the resume
  pattern, CORS;
- MapLibre GL JS (`setTiles` per frame event; an `addProtocol` retry wrapper; `/loop` or N sources at
  opacity 0);
- maplibre_gl (tile-key URLs, raster source, `/loop` image sources with `LatLngQuad`, barb MVT);
- flutter_map (`{r}` with `RetinaMode.isHighDensity`, `maxNativeZoom`, `tileBounds`, a
  `RetryAfterClient` for `NetworkTileProvider`, `OverlayImageLayer` loops, avoiding
  `flutter_map_tile_caching`);
- Cesium (`UrlTemplateImageryProvider`, `WebMercatorTilingScheme`, `maximumLevel`, `rectangle`, a
  `Resource` `retryCallback`);
- legends and attribution, plain HTTP/1.1 (`/loop`, `SharedWorker`), tile-key operations, and a "Wind
  particles" heading reserved for 051.

The client APIs used in the guide were checked against the tagged sources: flutter_map v8.3.2
`TileLayer`, `NetworkTileProvider` and `RetinaMode`; maplibre_gl v0.27.1 `addImageSource`,
`updateImageSource`, `addImageLayer` and `addRasterLayer`. So were the versions (npm, pub.dev).

**6. Docs**
- `docs/Operator-Guide.md`:
  - "Remote access and TLS" now covers the `web` profile and exporting Caddy's local root. It also
    covers trusting that root on macOS, Windows, Linux (including Firefox), Android (with the API 24+
    `network_security_config` requirement for apps, including Flutter) and iOS (installing the profile,
    then full trust).
  - "Exposing Foresignal" gains a tile-key bullet and a log-redaction update.
  - "Trusted proxies" names the `web` profile.
- `docs/Architecture.md` §15.1: tile keys as the path-carrier feed-key variant, and `RequestPrincipal`.
- `docs/Invariants.md`: SEC-4 and EGR-1 list the new tests.
- `README.md`: the docs index links the Frontend Guide.
- `docker/caddy/Caddyfile.example` points at the profile.

**7. Spec and Postman**
- The three operations are `implemented` and off `pending-operations.txt`.
- `INVALID_TILE_KEY` (401) was added to `x-foresignal-error-codes`.
- The descriptions of `listTileKeys`, `createTileKey`, `revokeTileKey`, `info.description` (the tile-key
  carrier, its codes, cache rule, log redaction and the CORS set) and the `feedKey` scheme were updated.
- A changelog entry was added.
- Postman was regenerated, with a `BODIES` example and a `tileKeyId` capture for `createTileKey`.
- `lintOpenApi`: valid, 0 errors, 71 warnings.

## Files created / modified

**New, main:**
- `access/`: `package-info`, `TileKeyPolicy`, `TileKeyService`
- `api/TileKeysController`
- `api/dto/TileKeyDto`
- `config/RedactingErrorAttributes`

**Changed, main:**
- Security: `ApiTokenEntity` (`TYPE_TILE_KEY`, `isTileKey`, `isPathKey`), `TokenService`, `TokenFilter`,
  `AccessPolicy` (agent rules for `/v1/tile-keys`).
- `security/feed/FeedKeyService` (the shared methods).
- `security/restrict/`: `PathKeyFilter` (per-resolver codes, eight-character redaction),
  `PathKeyResolver` (code defaults), `RequestPrincipal` (`TILE_KEY`, `isTileKey`, `isPathKey`,
  `tilekey:` client key).
- `security/visibility/SourceVisibility`.
- `security/ratelimit/ClientRateLimiter` (documentation only).
- Streams: `stream/sse/HeaderStreamPrincipalResolver`, `SseHub`, `ChangesTopic`.
- API and config: `api/FeedKeysController` (shared `baseUrl`), `api/RasterTilesController`
  (documentation), `api/error/ErrorCodes`, `config/CorsFilter`.

**Deploy:** `docker-compose.yml`, `deploy/caddy/Caddyfile` (new), `deploy/web/.gitkeep` (new),
`docker/caddy/Caddyfile.example`.

**Docs, spec, Postman:** `docs/Frontend-Guide.md` (new), `docs/Operator-Guide.md`,
`docs/Architecture.md`, `docs/Invariants.md`, `docs/OpenAPI.json`, `README.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−3 lines).

**Tests:**
- New: `access/TileKeyServiceIT`, `access/TileKeyFilterIT`, `access/TileKeyCacheHeadersIT`,
  `access/TileKeySseIT`, `access/TileKeyRedactionTest`, `api/RenderQueueFairnessTest`,
  `config/CorsFilterTest`, `deploy/ComposeConfigTest`.
- Updated: `config/CorsFilterIT`, `deploy/ComposeFilesTest`, `openapi/PostmanCollectionContractTest`.

## Deviations from the prompt

1. **No separate `TileKeyFilter` class.**
   - Conventions ("Key-in-path URLs") says one filter (035) resolves every `/v1/k/{key}` key, and that
     050 and 112 reuse it. Conventions wins over the prompt text.
   - So the tile-key carrier is a `PathKeyResolver` bean (`TileKeyService`) on 035's `PathKeyFilter`,
     which already runs before `TokenFilter` and forwards to the canonical path.
   - The prompt's per-step behavior is all there. The test is still named `TileKeyFilterIT`.
2. **No `carrier` column.**
   - 035's feed keys are already carried in the path (`/v1/k/{key}/…`), not as a query parameter, so a
     `query | path` carrier would always read `path`.
   - The existing `type` column (`feed_key` / `tile_key`) is the discriminator, as 035's notes asked.
     `prefixes text[]` already existed.
   - The spec's `listTileKeys` description said "`carrier=path`"; it now says `type = tile_key`.
3. **Validation order on the carrier.** 035's filter resolves the key before checking the method, so
   POST with an invalid key is 401, not 405. With a valid key it is 405, as in the prompt.
4. **`TileKey.restrictions` hides the private exclusion.**
   - Tile keys always store `excludePrivate: true`. The DTO leaves that member out, and returns null
     when no other restriction is set.
   - This matches the spec's `oneOf [ReadRestriction, null]` and avoids suggesting the exclusion is
     optional.
5. **`/v1/tiles.json` (the layer index) is not reachable through a tile key.** The allowed prefix is
   `/v1/tiles/` with a trailing slash, as the spec enum fixes it. Per-layer TileJSON
   (`/v1/tiles/{layer}.json`) and MVT tiles are reachable.
6. **MapLibre GL JS version.** The prompt says the latest is 6.13.0. npm shows 6.14.0, published
   2026-10-11, the day before this run, so the guide states 6.14.0. The other three versions match the
   prompt: maplibre_gl 0.27.1, flutter_map 8.3.2 and Cesium 1.146.0.
7. **Caddy `encode` excluding PNG.** Caddy's `match` takes no negation, so the Caddyfile lists the
   compressible types explicitly: JSON family, MVT, JS, SVG and text except SSE. That leaves out PNG
   and `text/event-stream`. Caddy's documented default list also excludes PNG but includes `text/*`.
8. **Caddy ports bind all interfaces.** The prompt specifies `80:80` and `443:443`. The 021
   `ComposeFilesTest` rule "every port defaults to 127.0.0.1" now exempts the opt-in `caddy` service,
   whose purpose is LAN clients. `ComposeConfigTest` asserts its ports.
9. **`ComposeFilesTest` was missing the 049 `satdump` profile.** Its "only optional services carry a
   profile" check would have failed on `satdump`, so the `satdump` and `caddy` cases were added.
10. **`/docs` (Swagger UI) is not proxied by the `web` profile.** The prompt's route list is
    `/v1/* /mcp* /actuator/health*`. The Caddyfile comment says how to add `/docs`.

## Issues found / open questions

1. **Owner: keyed clients must rewrite URLs that come back in responses.**
   - TileJSON `tiles`, frame templates, `/loop` URLs and SSE payload templates are canonical
     (`/v1/...`), even when the request came through `/v1/k/{key}/…`, because everything after the
     forward sees the canonical path.
   - The Frontend Guide gives a one-line `keyed()` rewrite.
   - Should Foresignal instead emit keyed templates when the request arrived through a key? That would
     need the original prefix kept on the request and used by `ProductUrls`/`TileJsonService`. SSE
     payloads are written at ingest and cannot carry a per-client key.
2. **Owner: `Foresignal-Prefetch`** is allowed but not CORS-safelisted, so a cross-origin browser
   pays a preflight for it, now cached for 24 h. The guide recommends `?prefetch=1`.
3. **Not verified at run time:**
   - the Caddyfile was checked against Caddy's documentation (`encode`, `log` `filter`/`regexp`) and
     `docker compose --profile web config` parses it, but Caddy was not started;
   - the client recipes are written against the tagged library sources and were not run in a client;
   - the CA install steps follow each platform's documented UI and were not performed.
4. **Data:** no instructions directed at an agent were found in fetched pages or tool output. Fetched:
   - Docker Hub tag API (caddy);
   - the npm registry (maplibre-gl, cesium) and the pub.dev API (maplibre_gl, flutter_map,
     flutter_map_tile_caching);
   - raw GitHub files: flutter_map `CHANGELOG.md` and v8.3.2 sources, maplibre_gl v0.27.1
     `controller.dart`, Caddy website `encode.md` and `log.md`.
5. Owner decisions carried from 012–049 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `access/TileKeyServiceIT` | Created once with `fsk_` and a 43-character body; only the hash is stored; `type = tile_key` row of `api_token`, no `*tile_key*` table; scope read, no private grant, `excludePrivate` stored. Kept out of token and feed-key listings and bearer authentication. ISO and shorthand TTLs, and 31 d / zero / malformed refused. Disallowed, empty and non-string prefixes and unknown members refused; every allowed prefix accepted. Restrictions stored; endpoints, places, `excludePrivate: false` and unknown sources refused. `TILE_KEY` principal with `tilekey:` client key. Revoke, expiry, 404 on an unknown id. Paging and `INVALID_CURSOR`. |
| `access/TileKeyFilterIT` | MockMvc through the real filters with `openReads` off. Keyed MVT has the same bytes and ETag as a read-token bearer request, with `no-referrer` and `private`; HEAD works. Wrong prefix → 403 `INSUFFICIENT_SCOPE`, also `/v1/tiles.json`. POST and PATCH → 405 with `Allow`. Unknown, revoked (via the API) and expired keys → 401 `INVALID_TILE_KEY`; the `foresignal.auth.failures` meter rises by 3; a valid key resets the address. A restricted key clamps the MVT and gets 404 for a kind outside it. Private records never come back through an unrestricted key, while admin sees them. Agent-scope API: key and `urlBase` once, list without values, read token 403, invalid input 400, bad id 404, `/v1/k/{key}/tile-keys` 403. |
| `access/TileKeyCacheHeadersIT` | Content-addressed raster tile through a key: `private, max-age=31536000, immutable` with `openReads` off, `public, …, immutable` with it on. Same ETag and bytes as the canonical URL; 304. An alias gets `public` / `private, max-age=60` and `Foresignal-Frame-Time`. |
| `access/TileKeySseIT` | Real socket. An `EventSource` GET on `/v1/k/{key}/changes/stream` streams only the key's sources; the hub principal is `tilekey:`. Reconnecting with `Last-Event-ID` replays exactly the missed events, then goes live. Revoking the key sends a `reset` and ends the stream; it reopens with 401. |
| `access/TileKeyRedactionTest` | Real `TileKeyService` over a stubbed `FeedKeyService`. The access-log URI keeps eight characters; problem bodies never contain the key; `no-referrer` and `noindex` on every response; the tile-key codes (401 for unknown and gone, 403 `INSUFFICIENT_SCOPE`, 405 for PATCH). Backoff counts and resets, with 429 after the threshold. The `redact`/`keyHint` helpers. `RedactingErrorAttributes` redacts `path`. |
| `config/CorsFilterTest` (new, unit) | A preflight to `/v1/k/**` and `/v1/products/**` gets the allowed and exposed headers and `max-age=86400`. An actual keyed read exposes the headers without the preflight-only ones. Same-origin requests pass untouched. |
| `api/RenderQueueFairnessTest` | The fairness key prefers the tile-key id, then feed key, then token, then IP. Two keys behind one address are two clients. `ClientRateLimiter.clientKey` agrees. |
| `deploy/ComposeConfigTest` | `caddy` only under `web` with the pinned image; ports 80, 443/tcp and 443/udp; the volumes, the read-only Caddyfile mount and `./deploy/web`. The Caddyfile has `tls internal`, `xcaddy`, the route matcher, `flush_interval -1`, `encode zstd gzip` without PNG, SSE or `text/*`, the SPA fallback, the trusted-proxies note and the 8-character log filter. |

Updated:
- `CorsFilterIT`: max-age 86400 and the header constants.
- `ComposeFilesTest`: the `caddy` port exemption, and the `satdump` and `caddy` profiles.
- `PostmanCollectionContractTest`: `createTileKey` is schema-only, because its restriction check needs
  the context; `TileKeyServiceIT` covers it.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 0 errors, 71 warnings.

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all operations.

`docker compose --profile web config --quiet` → exit 0.

No new dependencies.

## Notes for the next prompt

1. **051 (wind fields, vectors):**
   - add the field and vector paths under `/v1/products/`; tile keys with that prefix cover them;
   - fill the Frontend Guide's "Wind particles" section;
   - the barb MVT recipes already point at `/v1/products/{id}/vectors/…`.
2. **052 (`/v1/sample`) and 055 (`/v1/maps/static`):**
   - both prefixes are already accepted on tile keys, and answer 404 until those endpoints exist;
   - classify `/v1/sample` as `expensive` in `ClientRateLimitFilter` (035 note 6). Tile keys are keyed
     `tilekey:<id>`.
3. **112 (share links):**
   - add a `PathKeyResolver` for `fss_`;
   - override `invalidCode`/`goneStatus`/`goneCode`/`outsidePrefixCode` for the share-link codes
     (`SHARE_EXPIRED` 410);
   - share links resolve `/v1/k/{key}` with no rest path, which `PathKeyFilter` currently refuses with
     400. Handle that in the filter for `fss_`.
4. **Any new principal-keyed code:** use `RequestPrincipal.isPathKey()` for "feed or tile key" and
   `clientKey()` for keys. Never compare to `"feed:"` alone; `ChangesTopic` and `SseHub` already check
   `tilekey:` too.
5. **057 checkpoint:**
   - run the 050 tests;
   - `TileKeyServiceIT` and `TileKeyFilterIT` share the `TriggerTestSources` context, `TileKeySseIT`
     the `StreamTestSources` web context, and `TileKeyCacheHeadersIT` the `RasterTestSources` web
     context, so no new contexts;
   - `ComposeFilesTest` had a latent failure from 049's `satdump` profile, fixed here.
6. **144 (Flyway):** no new tables or columns. `api_token.type` gains the value `tile_key`.
7. **Owner decisions pending:** Issues 1–2 above, plus those carried from 012–049.
