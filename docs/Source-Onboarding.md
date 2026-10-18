# Foresignal — Bringing data sources online, one at a time

This is the operator playbook for turning data providers on individually, proving each one
pulls → stores → is retrievable over the API, and running the service with any subset of
providers configured. Every call below is copied from the Postman collection at
`postman/Foresignal.postman_collection.json` or a controller in `src/main/java/ai/foresignal/api`.

Companion material: `docs/Source-Catalog.md` (the authoritative per-source list — tier, auth,
interval, budget), `docs/Runtime-Verification.md` (bring the service up), and `scripts/smoke.sh`
(the automated one-source end-to-end).

---

## The guarantee (and why it holds)

You can enable **1, 2, 10, 100 — any subset** of sources and the service runs cleanly on just
those. Nothing is thrown because other providers are unconfigured. This is designed in, not
incidental, and it is provable from three places in the code:

1. **Every source starts disabled.** At startup `SourceService.seedDefaults()` inserts one
   disabled settings row per registered source
   ([`SourceService.java:159-172`](../src/main/java/ai/foresignal/source/SourceService.java#L159),
   `entity.setEnabled(false)` at line 161). Its javadoc: *"installing Foresignal must not start
   fetching from a hundred upstreams. The operator opts each source in."*

2. **The collector only ever touches enabled, credentialed sources.** The scheduling loop queries
   literally `select source_id, interval_seconds from source_settings where enabled`, and then
   `if (descriptor.requiresCredentials() && !credentialsSet(id)) { continue; }`
   ([`CollectorScheduler.java:357-382`](../src/main/java/ai/foresignal/collect/CollectorScheduler.java#L357)).
   A disabled or keyless source is never in the work set — there is nothing to fail on. A push- or
   derived-mode source is never polled either.

3. **Startup does not require any provider.** With zero enabled sources the scheduler simply
   schedules nothing. The app only refuses to start if an adapter's *descriptor* is malformed — a
   code defect, never a missing key
   ([`SourceRegistry.java:64-98`](../src/main/java/ai/foresignal/source/SourceRegistry.java#L64)).

The only "errors" you will ever see from an unconfigured provider are **config-time 4xx guards**
returned to *you* when you ask to enable something that isn't ready (see
[Expected 4xx guards](#expected-4xx-guards-these-are-not-crashes)). They are refusals, not crashes,
and the rest of the system keeps running.

**Reading data never errors on an unconfigured source.** `GET /v1/records?source=<id>` for a
disabled (but registered) source returns an empty page, not an error; only an *unknown* id is a 404
([`RecordsController.java:974-982`](../src/main/java/ai/foresignal/api/RecordsController.java#L974)).

---

## One-time setup

### 1. Start the service

See `docs/Runtime-Verification.md`. In short, either:

```bash
docker compose up -d
```

or, for local development against just the database:

```bash
docker compose up -d db && ./gradlew bootRun
```

### 2. Grab the admin token (printed once, on first start)

The bootstrap admin token is generated on first start and written to the log **once**, then kept
only as a SHA-256 hash ([`AdminTokenService.java:177-184`](../src/main/java/ai/foresignal/security/AdminTokenService.java#L177)).
Capture it from the log:

```bash
docker compose logs foresignal | grep -A5 "FORESIGNAL ADMIN TOKEN"
```

Lost it? Restart with `FORESIGNAL_RESET_ADMIN_TOKEN=true` for a fresh one. The token looks like
`fsr_…` and goes in the `Authorization: Bearer <token>` header.

> **Reads vs. writes.** `security.openReads` defaults to **true**
> ([`SecuritySettings.java`](../src/main/java/ai/foresignal/settings/SecuritySettings.java)), so every
> *read* endpoint (list sources, health, records, changes, usage) works with **no token**. You only
> need the admin token for the *write* actions: enable/disable, set/remove credentials, fetch-now,
> and probe. For user testing it's simplest to put the admin token in Postman and use it for
> everything.

### 3. Import the Postman collection and environment

- Collection: `postman/Foresignal.postman_collection.json`
- Environment: `postman/Foresignal-Local.postman_environment.json`

The environment ships with `baseUrl = http://localhost:8080` and an empty, secret `adminToken`.
**Paste your token into `adminToken`** and select the "Foresignal — Local" environment. That's all
the setup; `readToken` defaults to `{{adminToken}}`.

---

## The onboarding loop (repeat per provider)

This is the whole procedure for bringing *any* source online. The Postman folder
**"00 Quickstart — USGS end to end"** walks it for a no-key source; the generalized steps are:

| # | Step | Call | Notes |
|---|------|------|-------|
| 1 | **Find the source** | `GET /v1/sources` | Lists every registered source with its glance status. Or look it up in `docs/Source-Catalog.md`. |
| 2 | **See what it needs** | `GET /v1/sources/{id}` | The descriptor tells you `requiresCredentials`, the credential field names, the mode (polling/streaming/push), and the valid params. |
| 3 | **Set credentials** *(only if it needs them)* | `PUT /v1/sources/{id}/credentials` | Body `{"values":{"<field>":"<secret>"}}`. Field names come from step 2 / the catalog's Auth column. 204 on success. |
| 4 | **Enable it** | `PUT /v1/sources/{id}` | Body `{"enabled":true}` (add `"params":{…}` / `"interval":"…"` / `"budget":{…}` if you want to override defaults). 200 returns the full source detail. |
| 5 | **Force a fetch now** | `POST /v1/sources/{id}/fetch` | Returns a `FetchOutcome`: `recordsSeen`, `recordsChanged`/`persisted`, `fromSeq`/`toSeq`, `error`. Immediate proof of pull → store. (Polling sources only; streams use `probe`.) |
| 6 | **Check health** | `GET /v1/sources/{id}/health` | `status`, `lastSuccessAt`, `lastDataAt`, `lastRecordsChanged`, `consecutiveFailures`, `lastError`, budget usage. |
| 7 | **Read the data back** | `GET /v1/records?source={id}` | The stored records. `format=geojson\|json\|ndjson\|csv`, filter by `kind`, `bbox`, `since`/`until`, `limit`≤5000. |

After step 4, the change reaches the collector within ~2 s, so even without step 5 the source will
fetch on its own interval. Step 5 just makes it immediate and gives you a synchronous result to
read.

---

## Worked example A — a source with no credentials (`usgs-earthquakes`)

The fastest possible proof the pipeline works end to end. No key, no params required (`feed`
defaults to `all_day`). This is exactly the Postman "00 Quickstart" folder.

```
# 1. Service is up
GET  {{baseUrl}}/v1/system

# 2. Confirm the source exists (disabled by default)
GET  {{baseUrl}}/v1/sources

# 3. Enable it
PUT  {{baseUrl}}/v1/sources/usgs-earthquakes
     Authorization: Bearer {{adminToken}}
     { "enabled": true, "params": { "feed": "all_day" } }

# 4. Pull now
POST {{baseUrl}}/v1/sources/usgs-earthquakes/fetch
     Authorization: Bearer {{adminToken}}
     -> { "success": true, "recordsSeen": N, "recordsChanged": N, "toSeq": ... }

# 5. Confirm health
GET  {{baseUrl}}/v1/sources/usgs-earthquakes/health
     -> { "status": "healthy", "lastSuccessAt": ..., "lastDataAt": ... }

# 6. Retrieve what it stored
GET  {{baseUrl}}/v1/records?source=usgs-earthquakes&format=geojson&limit=50

# 7. Incremental change feed (for live tailing)
GET  {{baseUrl}}/v1/changes?after=0&limit=100
```

`scripts/smoke.sh` automates exactly this (enable → poll `/v1/records` until `items > 0`) and is
the canonical check that a fresh install is healthy.

---

## Worked example B — a source that needs an API key

Same loop, with the credential step. Say the source declares a required `apiKey` field (check
`GET /v1/sources/{id}` or the catalog's Auth column):

```
# a. Store the key (write-only; never echoed back)
PUT  {{baseUrl}}/v1/sources/{{sourceId}}/credentials
     Authorization: Bearer {{adminToken}}
     { "values": { "apiKey": "YOUR_REAL_KEY" } }
     -> 204 No Content

# b. Enable
PUT  {{baseUrl}}/v1/sources/{{sourceId}}
     Authorization: Bearer {{adminToken}}
     { "enabled": true }
     -> 200

# c. Pull, check, read  (identical to steps 5-7 above)
POST {{baseUrl}}/v1/sources/{{sourceId}}/fetch
GET  {{baseUrl}}/v1/sources/{{sourceId}}/health
GET  {{baseUrl}}/v1/records?source={{sourceId}}
```

Credential field names vary by provider — common ones are `apiKey`, `token`, `authKey`, `key`,
`mapKey`, and `email`+`password`. The exact names for a given source are in that source's
descriptor (step 2) and in the Auth column of `docs/Source-Catalog.md`. Credentials are **encrypted
at rest** and the only decryptor lives inside the collector; the API will only ever tell you
*whether* a credential is set, never its value.

> **If you delete a key from a source that requires one, the source is auto-disabled** rather than
> left to fail on every fetch
> ([`SourceService.java:743-768`](../src/main/java/ai/foresignal/source/SourceService.java#L743)).

---

## Setting how often a source pulls

The poll frequency is per-source, on the same update call — the **`interval`** field:

```
PUT  {{baseUrl}}/v1/sources/{id}
     Authorization: Bearer {{adminToken}}
     { "interval": "5m" }
```

Combine it with enabling in one call if you like: `{"enabled": true, "interval": "30s"}`.

**Accepted formats** ([`Durations.java:18`](../src/main/java/ai/foresignal/settings/Durations.java#L18),
regex `^(\d+)\s*(s|m|h|d|w)$`, case-insensitive) plus ISO-8601:

| You write | Means |
|---|---|
| `"30s"` | 30 seconds |
| `"5m"` | 5 minutes |
| `"1h"`, `"12h"` | hours |
| `"1d"`, `"1w"` | days, weeks |
| `"PT30S"`, `"PT5M"` | ISO-8601 is also accepted |

Use single-letter units (`s m h d w`), not `min`. A space before the unit is tolerated (`"60 s"`).

**Rules** ([`SourceService.java:348-363`](../src/main/java/ai/foresignal/source/SourceService.java#L348)):

- Omit `interval` → the stored value is left unchanged. Each source already has a sensible default
  from its descriptor (e.g. `usgs-earthquakes` defaults to `60s`; see the "Default interval" column
  of `docs/Source-Catalog.md`).
- Must be **> 0**, else `400` with a field error on `interval`.
- Must be **≥ the source's `minInterval`** when the descriptor declares one (only a few sources do);
  going under it returns a `400` stating the minimum. Every other source accepts any positive value.

**Effect is near-immediate:** the change publishes a `SourceSettingsChangedEvent`, the collector
reconciles within ~2 s, and the source then polls on a fixed delay equal to your `interval`,
measured from the *end* of the previous fetch.

**To read the current interval:** `GET /v1/sources/{id}` echoes it back, normalized (e.g. `60s`,
`5m`). Intervals are fully independent per source — one source on `30s` and another on `6h` is
exactly the intended usage.

---

## Endpoint reference

Scopes: an **admin** token satisfies everything (`READ < AGENT < ADMIN`). "read" endpoints work
with no token while `openReads` is true.

### Managing a source

| Method / Path | Scope | Purpose |
|---|---|---|
| `GET /v1/sources` | read | List all registered sources + glance status. |
| `GET /v1/sources/{id}` | read | One source: descriptor + settings + health. Tells you what it needs. |
| `PUT /v1/sources/{id}` | **admin** | Enable/disable, set `interval`, `budget`, `params`. Body `SourceSettingsUpdate` (omitted fields unchanged). |
| `PUT /v1/sources/{id}/credentials` | **admin** | Store secrets. Body `{"values":{…}}`. → 204. |
| `DELETE /v1/sources/{id}/credentials` | **admin** | Remove secrets (auto-disables if required). → 204. |
| `POST /v1/sources/{id}/fetch` | **admin** | Force a poll now (even while disabled). Returns `FetchOutcome`. |
| `POST /v1/sources/{id}/probe` | **admin** | Diagnostic reach test without ingesting (use for streams). |

### Watching a source

| Method / Path | Scope | Purpose |
|---|---|---|
| `GET /v1/sources/{id}/health` | read | Full health view for one source. |
| `GET /v1/sources/health?status=…` | read | Health across all sources, filterable by status. |
| `GET /v1/sources/{id}/usage` | read | Budget and usage detail. |
| `GET /v1/sources/{id}/fetches` | **admin** | The fetch log, newest first; filter by `status`/`trigger`. |

Health `status` ∈ `healthy, degraded, failing, idle` (enabled, not yet attempted), `disabled`
([`HealthStatus.java:14-24`](../src/main/java/ai/foresignal/source/HealthStatus.java#L14)).

### Reading stored data

| Method / Path | Scope | Key params |
|---|---|---|
| `GET /v1/records` | read | `source` (multi), `kind`, `externalId`, `bbox`, `near`+`radiusKm`, `since`/`until`, `q`, `order`, `asOf`, `format`, `cursor`, `limit`≤5000, `count`. |
| `GET /v1/records/{sourceId}/{externalId}` | read | One record; `format`, `include`, `fields`. |
| `GET /v1/records/{sourceId}/{externalId}/versions` | read | Version history of one record. |
| `GET /v1/history` | read | Time-series query; `since` required; `format=czml` supported. |
| `GET /v1/changes?after=<seq>` | read | Incremental change feed (poll for live tailing). |
| `GET /v1/kinds` | read | Record kinds present, with counts and sources. |
| `GET /v1/kinds/{kind}/schema` | read | The schema for a kind. |

For live push instead of polling, `GET /v1/events/stream` (SSE) multiplexes a `changes` topic you
can filter by source and bbox.

The full, authoritative spec is `docs/OpenAPI.json` — open it in Postman/Swagger for every field.

---

## Expected 4xx guards (these are **not** crashes)

When *you* ask to enable something that isn't ready, the API refuses with a precise 4xx and the
service keeps running. Each has a clear fix:

| You'll see | When | Fix |
|---|---|---|
| **409 `CREDENTIALS_REQUIRED`** | Enabling a source that needs a key before you've stored one. | `PUT …/credentials` first, then enable. |
| **400 `VALIDATION_FAILED`** | Enabling with a missing/invalid required `param`. | Check valid params via `GET /v1/sources/{id}`; include them in the enable body. |
| **409 `NOT_SUPPORTED_FOR_MODE`** | `fetch`-now on a streaming/push/derived source. | Use `POST …/probe` for streams; push/derived aren't polled. |
| **429** (budget) | `fetch`-now after the day's budget is spent. | Wait, or raise `budget` via `PUT /v1/sources/{id}`. |
| **404** | `?source=<id>` for an id that isn't registered. | Fix the id (a disabled-but-registered id returns an empty page, not 404). |

None of these affect other sources or the running service.

---

## Immediate wins — 133 sources need no credentials at all

These are enabled with a single `PUT /v1/sources/{id}` `{"enabled":true}` and nothing else. A
representative starter set (full list: filter the Auth column of `docs/Source-Catalog.md` for
`none`):

- **Seismic / geophysical:** `usgs-earthquakes`, `emsc-seismic`, `fdsn-geofon`, `fdsn-ingv`
- **Aviation weather:** `awc-sigmets`, `awc-gairmets`, `awc-pireps`, `awc-tafs`, `awc-cwas`
- **Hydrology / flooding:** `ea-flood-warnings`, `eccc-hydrometric`, `copernicus-ems`
- **Wildfire:** `cwfis-hotspots`, `cwfis-perimeters`, `cwfis-fwi-stations`, `feature-wfigs-incidents`
- **Hurricanes / alerts (CAP):** `feed-nhc-atlantic`, `feed-nhc-eastpacific`, and the national CAP
  feeds `cap-de-dwd`, `cap-no-metno`, `cap-nz-metservice`, `cap-kr-kma`, … (many countries)
- **Cyber advisories:** `cisa-kev`, `feed-cisa-advisories`, `feed-ncsc-uk`, `feed-ecdc-threats`
- **Aviation / airspace:** `faa-nas-status`, `faa-tfr`, `faa-registry`, `adsbfi-mil`, `adsblol-mil`
- **Rail / transport:** `amtraker`, `digitraffic-rail`, `ais-digitraffic`, `ais-kystverket`
- **Traffic cameras:** `cctv-caltrans-d1`…`d12`, `cctv-drivebc`, `cctv-nsw`, `cctv-austin`

Start with `usgs-earthquakes` to prove the pipeline, then turn on as many no-credential sources as
you like; each one is independent.

---

## Confirming "no errors from unconfigured providers"

To satisfy yourself the launch is clean with only a handful of sources on:

1. **`GET /v1/sources/health`** — every source you haven't enabled reports `status: disabled`;
   enabled ones report `healthy`/`idle`. No source reports an error you didn't cause.
2. **The application log** — with sources disabled, the collector logs
   `Collector scheduler started (reconcile every 2s)` and then nothing about unconfigured
   providers. Missing keys produce **no** log lines (the source is simply never scheduled).
3. **`GET /v1/records?source=<a-disabled-source>`** — returns an empty page (200), not an error.
4. **`scripts/smoke.sh`** — passes with a single source enabled.

---

*Grounded in the code as of 2026-10-18. Endpoints cross-checked against the Postman collection and
the controllers in `src/main/java/ai/foresignal/api`. The per-source authority is
`docs/Source-Catalog.md`; the full API spec is `docs/OpenAPI.json`.*
