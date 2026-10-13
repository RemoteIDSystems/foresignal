# Foresignal Frontend Guide

How map clients read Foresignal's rasters, record tiles and change streams: MapLibre GL JS, maplibre_gl
(Flutter), flutter_map and Cesium. The rules come from `docs/Weather-Imagery-Design.md` §6 (cache policy
§6.1, tiles and loops §6.3, access §6.7, SSE §6.8, recipes §6.10); the operations are in
`docs/OpenAPI.json`.

Library versions were checked on 2026-10-12:

| Client | Version | Notes |
|---|---|---|
| MapLibre GL JS | 6.14.0 | ESM-only package; needs WebGL2 |
| maplibre_gl (Flutter) | 0.27.1 | Image sources (`addImageSource`) are not implemented on web |
| flutter_map | 8.3.2 | `NetworkTileProvider` has built-in caching since 8.2.0 and honors `Cache-Control`; tiles still load when caching fails (8.2.1) |
| Cesium | 1.146.0 | |

## Contents

1. [Getting in: tile keys, tokens and tickets](#getting-in-tile-keys-tokens-and-tickets)
2. [Rules for every client](#rules-for-every-client)
3. [MapLibre GL JS](#maplibre-gl-js)
4. [maplibre_gl (Flutter)](#maplibre_gl-flutter)
5. [flutter_map](#flutter_map)
6. [Cesium](#cesium)
7. [Legends and attribution](#legends-and-attribution)
8. [On plain HTTP/1.1](#on-plain-http11)
9. [Wind particles](#wind-particles)

## Getting in: tile keys, tokens and tickets

Most map engines cannot add an `Authorization` header to the tile, image and `EventSource` requests they
make themselves. Foresignal gives them three ways in:

- **Open reads.** While `security.openReads` is on, reads need no credential at all (LAN default).
- **Tile keys** (`fsk_…`) for header-less clients when reads are closed. A tile key is a read-only key
  carried in the path, limited to path prefixes and at most 30 days old. It is the same mechanism as
  the feed keys of the Operator Guide, for map clients.
- **Bearer tokens** for clients that can send headers, and **stream tickets** (`POST /v1/stream-tickets`,
  `?ticket=`) for short-lived `EventSource` URLs from a client that holds a token.

### Minting a tile key

An `agent` token mints one per device:

```sh
curl -s -X POST https://foresignal.local/v1/tile-keys \
  -H "Authorization: Bearer $FORESIGNAL_AGENT_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"kitchen-tablet","prefixes":["/v1/products/","/v1/styles","/v1/changes/stream"],"ttl":"7d"}'
```

The answer carries the key **once**, plus `urlBase` (`{publicBaseUrl}/v1/k/{key}`):

```json
{"id":"…","name":"kitchen-tablet","keyPrefix":"fsk_AbCd","prefixes":["/v1/products/","/v1/styles","/v1/changes/stream"],
 "expiresAt":"2026-10-19T12:00:00Z","key":"fsk_AbCd…","urlBase":"https://foresignal.local/v1/k/fsk_AbCd…"}
```

- **Prefixes** are chosen from `/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`,
  `/v1/maps/static`, `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream` and `/v1/events/stream`.
  `/v1/maps/static` and `/v1/sample` answer 404 until the prompts that build them (055, 052) ship.
- **Restrictions** are optional: `"restrictions": {"sources": [...], "kinds": [...], "bbox": [w,s,e,n]}`
  clamps record tiles and change streams exactly as for a restricted token.
- **A tile key never reads private sources**, whatever its restrictions say.
- `GET /v1/tile-keys` lists keys without their values; `DELETE /v1/tile-keys/{id}` revokes one at once
  and resets its open streams.

### Using a tile key

Put the key after `/v1/k/` and keep the rest of the path:

| Canonical URL | Keyed URL |
|---|---|
| `/v1/products/mrms-reflectivity/tilejson` | `/v1/k/{key}/products/mrms-reflectivity/tilejson` |
| `/v1/products/mrms-reflectivity/tiles/{frameKey}/{z}/{x}/{y}.png` | `/v1/k/{key}/products/mrms-reflectivity/tiles/{frameKey}/{z}/{x}/{y}.png` |
| `/v1/changes/stream?kind=raster-frame` | `/v1/k/{key}/changes/stream?kind=raster-frame` |

Foresignal forwards the keyed request to the canonical path, so the response, its ETag and its cache rule
are the canonical URL's. Only `GET` and `HEAD` work (others are 405). An unknown, expired or revoked key
is `401 INVALID_TILE_KEY`; a path outside the key's prefixes is `403 INSUFFICIENT_SCOPE`.

**URLs inside responses are canonical.** TileJSON `tiles`, frame `tiles`/`tiles2x`/`image`, `/loop`
frame `url`s and SSE payload templates point at `/v1/...` without the key. A keyed client rewrites them
with one helper before handing them to the map:

```js
// urlBase is TileKeyCreated.urlBase, e.g. "https://foresignal.local/v1/k/fsk_AbCd…"
const keyed = (url) => url.replace(/^(https?:\/\/[^/]+)?\/v1\//, urlBase + "/");
```

```dart
String keyed(String url) => url.replaceFirst(RegExp(r'^(https?://[^/]+)?/v1/'), '$urlBase/');
```

### Operating tile keys

- **Mint one key per device**, so revoking a lost tablet does not break the others, and the render queue
  and rate limits treat each device as its own client.
- **Rotate weekly.** A key can live 30 days; mint the next one before the old one expires.
- **Keys appear in proxy logs and browser history.** That is why they are read-only, prefix-limited and
  short-lived. Foresignal's own access log and Caddy's `web` profile log keep only the first eight
  characters (`fsk_AbCd…`), and responses carry `Referrer-Policy: no-referrer`, but a third-party proxy
  may log the full URL.

## Rules for every client

### Take concrete templates; never put `latest` in a layer

Get the template from one of:

- TileJSON: `GET /v1/products/{id}/tilejson` (`tiles`, `foresignal:tiles2x`, `bounds`, `minzoom`,
  `maxzoom`, `foresignal:frameKey`, `foresignal:expiresAt`);
- the newest frame: `GET /v1/products/{id}/frames/latest` (`tiles`, `tiles2x`, `bounds`, `expiresAt`);
- a loop: `GET /v1/products/loop?...` (`frames[].url`, `coordinates`, `bounds`);
- SSE: a `raster-frame` change on `/v1/changes/stream?kind=raster-frame&externalId={productId}`, whose
  payload (`productId`, `frameKey`, `time`, `timeKey`, `tiles`, `bounds`, `stats`, `expiresAt`) is always
  included. It carries the 256 px template only; the `@2x` template is the same path with `{y}@2x.png`.

These templates are content-addressed (`{timeKey}~{sha8}`, a versioned `style=name@v`), so they are cached
as `immutable` for a year. **Never put `latest` (or `now`) in a layer's tile template.** An alias is only
cached for `min(cadence/2, 60 s)`, so a map built on it refetches every tile every minute and can mix two
frames in one view. Swap the template when a new frame arrives instead.

### Zoom and bounds

- Set the source's `maxzoom` to TileJSON `maxzoom`, the product's native zoom (MRMS 8, GOES C13 7, GFS 4,
  HRRR 8). Above it the server answers `400 TILE_OUT_OF_RANGE`; the map overzooms the last level instead.
- Set the source's `bounds` from TileJSON `bounds`, so nothing is requested outside the coverage.
- Outside the data inside the bounds, Foresignal returns one shared transparent PNG (fixed ETag,
  immutable); `?empty=204` opts into `204` if a client prefers.

### Retina

Use the `@2x` template (TileJSON `foresignal:tiles2x`, frame `tiles2x`, or `{y}@2x.png` in an SSE template) when `devicePixelRatio >= 2`, **with
`tileSize: 256`**: the tile covers the same area at 512 pixels.

### `503 RENDER_BUSY` and `Retry-After`

Tiles render on demand behind a fair queue (per tile key, then token, then client address). When a request
waits more than 1.5 s, the answer is `503 RENDER_BUSY` with `Retry-After: 1`. Retry after that many
seconds, two or three times, then give up on the tile. The per-client wrappers below do this. `429
RATE_LIMITED` carries `Retry-After` too.

Mark tiles the user is not looking at yet (preloaded animation frames) as prefetch, so they queue behind
interactive tiles: add `?prefetch=1`. The `Foresignal-Prefetch: 1` header does the same, but from a browser
on another origin it costs a CORS preflight; the query parameter does not.

### Expiry

Every frame carries `expiresAt`. Drop frames past it from animations and caches: frame removals are not
streamed, and a removed frame answers `410 FRAME_EXPIRED` (or the transparent tile with
`Foresignal-Frame-Expired: true` when the URL has `?onExpired=transparent`).

### Resume after the background

A phone or a hidden tab misses events. On returning to the foreground:

1. `GET /v1/changes?after=<last seq>&kind=raster-frame` and apply what arrived;
2. reconnect the stream with `Last-Event-ID: <last seq>` (browsers' `EventSource` sends it automatically
   on its own reconnects).

A `410 RESYNC_REQUIRED` means the gap is older than retention: reload the products and frames.

### Cross-origin browsers

Foresignal's CORS answer exposes `Foresignal-Frame-Time`, `Foresignal-Image-Bounds`,
`Foresignal-Frame-Expired`, `Retry-After`, `Link` and `ETag`, allows `Authorization`, `If-None-Match` and
`Foresignal-Prefetch`, and lets the browser cache a preflight for a day. A web client served by the `web`
compose profile is same-origin and makes no preflights.

## MapLibre GL JS

MapLibre GL JS 6 ships as ES modules only and needs WebGL2.

### A raster source from TileJSON, swapped per frame event

```js
import maplibregl from "maplibre-gl";

const base = "https://foresignal.local";            // or a tile key's urlBase plus keyed()
const product = "mrms-reflectivity";
const retina = window.devicePixelRatio >= 2;

const tj = await (await fetch(`${base}/v1/products/${product}/tilejson`)).json();
// SSE payloads carry the 256 px template only; @2x is the same path with "@2x" before ".png".
const template = (frame) => retina ? frame.tiles.replace("{y}.png", "{y}@2x.png") : frame.tiles;

map.addSource("radar", {
  type: "raster",
  tiles: [`fsretry://${retina ? tj["foresignal:tiles2x"] : tj.tiles[0]}`],
  tileSize: 256,
  minzoom: tj.minzoom,
  maxzoom: tj.maxzoom,
  bounds: tj.bounds,
  attribution: tj.attribution,
});
map.addLayer({ id: "radar", type: "raster", source: "radar", paint: { "raster-opacity": 0.7 } });

// One change stream for the app; a new frame swaps the template.
const events = new EventSource(`${base}/v1/changes/stream?kind=raster-frame&externalId=${product}`);
events.addEventListener("change", (e) => {
  const change = JSON.parse(e.data);
  const frame = change.payload;                    // raster-frame changes always carry their payload
  if (!frame || new Date(frame.expiresAt) < new Date()) return;
  map.getSource("radar").setTiles([`fsretry://${template(frame)}`]);
});
```

### Retry on 503 with `Retry-After`

`transformRequest` can add headers but cannot retry. A custom protocol can: prefix the template with
`fsretry://` (as above) and register a loader that fetches the real URL.

```js
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

maplibregl.addProtocol("fsretry", async (params, abortController) => {
  const url = params.url.replace(/^fsretry:\/\//, "");
  for (let attempt = 0; ; attempt++) {
    const response = await fetch(url, { signal: abortController.signal });
    if ((response.status === 503 || response.status === 429) && attempt < 3) {
      const seconds = Number(response.headers.get("Retry-After")) || 1;
      await sleep(seconds * 1000);
      continue;
    }
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return {
      data: await response.arrayBuffer(),
      cacheControl: response.headers.get("Cache-Control"),
      expires: response.headers.get("Expires"),
    };
  }
});
```

With a bearer token, add `headers: { Authorization: "Bearer …" }` to the `fetch`. With a tile key, use
`keyed()` URLs and no header.

### Animation

- **`/loop`** (preferred on mobile and HTTP/1.1): one request returns up to 24 frame image URLs over a
  snapped box with its `coordinates`. Add each as an `image` source with those coordinates, or swap one
  source's URL with `source.updateImage({ url, coordinates })`.
- **N preloaded sources:** add one raster source per frame (from `/v1/products/{id}/frames`), all at
  `raster-opacity: 0` with `?prefetch=1` on their templates, and step the visible one to the target
  opacity. Remove a source when its frame passes `expiresAt`.

## maplibre_gl (Flutter)

Header support is not reliable on every platform, so use **tile-key URLs** (`keyed()` above).

```dart
final tj = await fetchJson(keyed('$base/v1/products/mrms-reflectivity/tilejson'));
final retina = MediaQuery.devicePixelRatioOf(context) >= 2;
final tiles = keyed(retina ? tj['foresignal:tiles2x'] : (tj['tiles'] as List).first);

await controller.addSource('radar', RasterSourceProperties(
  tiles: [tiles],
  tileSize: 256,
  minzoom: (tj['minzoom'] as num).toDouble(),
  maxzoom: (tj['maxzoom'] as num).toDouble(),
  bounds: (tj['bounds'] as List).map((v) => (v as num).toDouble()).toList(),
  attribution: tj['attribution'],
));
await controller.addRasterLayer('radar', 'radar', const RasterLayerProperties(rasterOpacity: 0.7));
```

- On a new frame, remove and re-add the source with the new template (or keep two sources and
  cross-fade).
- **Animation:** call `/loop`, download each frame's image bytes, and show them with
  `addImageSource(id, bytes, LatLngQuad(topLeft: …, topRight: …, bottomRight: …, bottomLeft: …))` from
  the returned `coordinates` (`[lon, lat]` pairs, so build `LatLng(lat, lon)`), plus `addImageLayer`. Step
  with `updateImageSource`. Image sources are not implemented on Flutter web; use MapLibre GL JS there.
- **Wind:** the barb MVT layer (`/v1/products/{id}/vectors/{frameKey}/{z}/{x}/{y}.mvt`, prompt 051) as a
  symbol layer rotated by `fromDeg`.

## flutter_map

```dart
final retina = RetinaMode.isHighDensity(context);

TileLayer(
  // Concrete template from TileJSON or frames/latest; Foresignal's {r} is "" or "@2x".
  urlTemplate: keyed(frame.tiles).replaceFirst('{y}.png', '{y}{r}.png'),
  retinaMode: retina,
  maxNativeZoom: tj.maxzoom,          // overzoom above the product's native zoom
  tileBounds: LatLngBounds(LatLng(tj.bounds[1], tj.bounds[0]), LatLng(tj.bounds[3], tj.bounds[2])),
  tileProvider: NetworkTileProvider(httpClient: RetryAfterClient(http.Client())),
  userAgentPackageName: 'com.example.app',
)
```

- `{r}` expands to `@2x` when `retinaMode` is on, which matches Foresignal's `@2x` tiles at the same
  `tileDimension` of 256.
- **Caching:** `NetworkTileProvider` caches tiles on its own since 8.2.0 and honors `Cache-Control`, so
  Foresignal's immutable, content-addressed tiles become an offline cache for free. Tiles still load if
  caching fails.
- **Avoid `flutter_map_tile_caching`:** it is GPL-3.0 (or commercial), which conflicts with an MIT client.
- **Headers:** `NetworkTileProvider(headers: {'Authorization': 'Bearer …'})` works on mobile; a tile key
  works everywhere.

### Retry wrapper honoring `Retry-After`

The provider's default client already retries 503 but ignores `Retry-After`. Pass this one instead:

```dart
import 'package:http/http.dart' as http;

class RetryAfterClient extends http.BaseClient {
  RetryAfterClient(this._inner, {this.attempts = 3});
  final http.Client _inner;
  final int attempts;

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) async {
    for (var attempt = 0; ; attempt++) {
      final copy = http.Request(request.method, request.url)..headers.addAll(request.headers);
      final response = await _inner.send(copy);
      final busy = response.statusCode == 503 || response.statusCode == 429;
      if (!busy || attempt >= attempts) return response;
      await response.stream.drain<void>();
      final seconds = int.tryParse(response.headers['retry-after'] ?? '') ?? 1;
      await Future<void>.delayed(Duration(seconds: seconds));
    }
  }

  @override
  void close() => _inner.close();
}
```

### Loops

`/loop` returns frames over one snapped box. Show them with `OverlayImageLayer`:

```dart
OverlayImageLayer(overlayImages: [
  OverlayImage(
    bounds: LatLngBounds(LatLng(loop.bounds[1], loop.bounds[0]), LatLng(loop.bounds[3], loop.bounds[2])),
    imageProvider: NetworkImage(keyed(loop.frames[index].url)),
    opacity: 0.7,
  ),
])
```

The images are Web Mercator (`crs=EPSG:3857`), the same projection flutter_map draws in, so the
`bounds` corners line up.

## Cesium

```js
const tj = await (await fetch(keyed(`${base}/v1/products/mrms-reflectivity/tilejson`))).json();

const resource = new Cesium.Resource({
  url: keyed(tj.tiles[0]),
  retryAttempts: 3,
  retryCallback: async (resource, error) => {
    if (error?.statusCode !== 503 && error?.statusCode !== 429) return false;
    const seconds = Number(/retry-after:\s*(\d+)/i.exec(error.responseHeaders ?? "")?.[1] ?? 1);
    await new Promise((r) => setTimeout(r, seconds * 1000));
    return true;
  },
});

viewer.imageryLayers.addImageryProvider(new Cesium.UrlTemplateImageryProvider({
  url: resource,
  tilingScheme: new Cesium.WebMercatorTilingScheme(),
  maximumLevel: tj.maxzoom,                                   // the native zoom
  rectangle: Cesium.Rectangle.fromDegrees(...tj.bounds),      // [west, south, east, north]
  credit: tj.attribution,
}));
```

- Use **tile-key URLs**; Cesium's imagery requests carry no custom headers by default.
- On a new frame, add a provider for the new template and remove the old layer.
- **Wind:** barb MVT points as billboards rotated by `fromDeg`, or the `wind-speed` raster style.

## Legends and attribution

- **Legends:** native clients draw from the JSON legend (`GET /v1/products/{id}/legend`: stops, ticks,
  categories, specials such as "no coverage", and `meaning` text). Web clients may use the PNG
  (`/legend.png?width&orientation&theme&units`).
- **Attribution:** always from the structured object (`{text, html, url, licence, licenceUrl,
  requiredDisplay}`) in products, TileJSON and `/loop`, never a hard-coded string. When
  `requiredDisplay` is true it must be visible on the map. flutter_map: `RichAttributionWidget` with one
  `TextSourceAttribution` per entry; MapLibre: the source's `attribution`; Cesium: a `Credit`.

## On plain HTTP/1.1

Browsers allow about six connections per origin over HTTP/1.1, and every open `EventSource` holds one.

- **Animate with `/loop`**, one request per animation, instead of hundreds of tile requests.
- **Share one SSE connection per web client** across tabs, for example in a `SharedWorker` that owns the
  `EventSource` and posts events to each tab.
- Better: run the `web` compose profile (Caddy with HTTP/2 and HTTP/3, Operator Guide "Remote access and
  TLS"), which multiplexes everything over one connection.

## Wind particles

Reserved for prompt 051 (the GFS field endpoints and the particle layer port).
