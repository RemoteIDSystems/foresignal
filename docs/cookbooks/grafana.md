# Cookbook: Grafana

Three provisioned dashboards: **Foresignal Operations** (broker health from Prometheus metrics),
**Foresignal Live map** (current records on a Geomap) and **Foresignal Alerts** (a table and
annotations). Artifacts in [`integrations/grafana/`](../../integrations/grafana/):

| File | What |
|---|---|
| `provisioning/datasources/foresignal.yaml` | Prometheus and Infinity data sources |
| `provisioning/dashboards/foresignal.yaml` | Loads the dashboards folder |
| `dashboards/foresignal-operations.json`, `foresignal-live-map.json`, `foresignal-alerts.json` | The dashboards |
| `prometheus.yml` | Prometheus scrape configuration |
| `docker-compose.grafana.yml` | Optional override adding Prometheus and Grafana to the stack |

**Token scope:** a **read** token, stored in Grafana's `secureJsonData` for the Infinity data
source; give it a restriction (`sources`, `kinds`, `bbox` or `area`) when the dashboards need only
part of the data. Prometheus needs no token while `security.metricsOpen` is on (see the warning
below).

**With `openReads` off and no headers.** Infinity sends the bearer token, so it needs nothing else.
A panel or tool that cannot send headers (an image renderer, a static GeoJSON layer) can read
through a feed key instead: `POST /v1/feed-keys` with `prefixes: ["/v1/records"]` and a
restriction, then use `http://foresignal:8080/v1/k/fsf_your_feed_key/records?format=geojson`.

**URLs used:** `/actuator/prometheus` (scraped by Prometheus), `/v1/records` (`format=geojson`),
`/v1/alerts`, `/v1/system` (the Infinity health check).

## Versions (verified 2026-10-12)

| Component | Version | License |
|---|---|---|
| Grafana OSS | 13.2.3 (`grafana/grafana:13.2.3`) | AGPL-3.0, run as a separate service; nothing of it is linked into Foresignal |
| [Infinity data source](https://grafana.com/grafana/plugins/yesoreyeram-infinity-datasource/) | 4.1.1, pinned | Apache-2.0 |
| Prometheus | 3.15.0 (`prom/prometheus:v3.15.0`) | Apache-2.0 |

Infinity must be **3.4.1 or later** (CVE-2025-8341, fixed in 3.4.1). Infinity 4.1.1 supports
Grafana 11.6.11 and later 11.6 releases, 12.0.10+, 12.1.7+, 12.2.5+ and every later release; use
Grafana 11.6 or newer. The override installs the plugin with `GF_PLUGINS_PREINSTALL_SYNC`
(`yesoreyeram-infinity-datasource@4.1.1`), which installs it before Grafana starts so provisioning
finds it. (`GF_INSTALL_PLUGINS` is deprecated.)

## Run it

```sh
FORESIGNAL_GRAFANA_TOKEN=fsr_your_read_token \
  docker compose -f docker-compose.yml -f integrations/grafana/docker-compose.grafana.yml up -d
```

Run it from the repository root: relative paths in a compose override resolve against the first
file's folder. Grafana is then at `http://127.0.0.1:3000` (`FORESIGNAL_BIND` as for the broker).
With an existing Grafana, copy the provisioning files and dashboards instead, and change the URLs
`http://foresignal:8080` and `http://prometheus:9090` to yours.

## Data sources

- **Prometheus** (`uid: foresignal-prometheus`) reads a Prometheus that scrapes
  `/actuator/prometheus` every 30 s.

  **`/actuator/prometheus` is unauthenticated while `security.metricsOpen` is on (the default).**
  Scrape it only over an internal network: the override publishes no Prometheus port, and the
  broker's own port stays on `127.0.0.1` by default. If the broker is published on the LAN, set
  `metricsOpen: false` (`PUT /v1/settings/security`), after which a scrape needs an admin token
  (`authorization: {credentials_file: …}` in `prometheus.yml`), or block `/actuator/prometheus` at
  the reverse proxy. `metricsPerMinute` (`PUT /v1/settings/ratelimits`) limits scrapes per IP.
- **Infinity** (`uid: foresignal-infinity`) calls the REST API with `auth_method: bearerToken` and
  the read token in `secureJsonData.bearerToken` (from `$FORESIGNAL_GRAFANA_TOKEN`). `allowedHosts`
  lists only the broker, so a dashboard cannot send the token anywhere else.
- **No PostgreSQL data source.** Querying Foresignal's tables directly would bypass token scopes,
  private sources and license filtering, and tie dashboards to internal tables that change between
  releases.

## Dashboards

**Foresignal Operations.** Every metric name in it exists in the code (checked by
`GrafanaMetricNamesIT`):

| Panel | Metric (Micrometer name) |
|---|---|
| Current records | `foresignal_records_current` (`foresignal.records.current`) |
| Unresolved alerts | `foresignal_alerts_active` by `severity` |
| Ingest queue depth, writer state | `foresignal_ingest_queue_depth`, `foresignal_ingest_writer_state` |
| Open SSE clients | `foresignal_sse_clients{stream="all"}` |
| Persisted versions per second | `foresignal_ingest_records_persisted_total` by `source` |
| Records seen and rejected | `foresignal_ingest_records_seen_total`, `foresignal_ingest_records_rejected_total` |
| Writer batch time | `foresignal_ingest_batch_duration_seconds_sum` / `_count` by `phase` |
| Fetch time | `foresignal_fetch_duration_seconds_sum` / `_count` by `source` |
| Source data age | `foresignal_source_data_age_seconds` by `source` |
| Failing or degraded sources | `foresignal_source_status{status=~"failing\|degraded"} == 1` |
| Partition headroom | `foresignal_history_partition_headroom` by `tier` |
| JVM heap | `jvm_memory_used_bytes{area="heap"}` |

Per-source series (ingest counters, fetch timers, source status) appear only after a source has
fetched or ingested something.

**Foresignal Live map.** A Geomap panel over
`/v1/records?format=geojson&kind=$kind&bbox=$bbox&limit=2000` with Infinity's backend (JSONata)
parser. The root selector keeps point features and flattens them to `lat`, `lon`, `title`,
`source`, `externalId` and `observedAt`. Set the `kind` and `bbox` variables at the top; keep
`limit` at 2,000 or below (the API allows 5,000) so the browser stays responsive. Lines and polygons
are skipped.

**Foresignal Alerts.** A table of `/v1/alerts?since=${__from:date:iso}&limit=500` (the dashboard's
time range), a stat of firing alerts (`state=firing`), and annotations from the same query (time
`firedAt`, title the watch, text the message).

## Limits

- Infinity reads one page per query; it does not follow `nextCursor`.
- Each panel refresh is an API call with the read token; keep refresh intervals at a minute or
  more.
- Grafana caches nothing from Foresignal; a deleted record disappears on the next refresh.

## Privacy

- The read token sees every source that is not private; anyone with access to the Grafana
  dashboards sees what it sees. Restrict Grafana access, or the token.
- Prometheus metrics carry source ids and counts, not record contents.
