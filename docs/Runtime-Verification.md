# Foresignal runtime verification

> **Status: not yet run.** Prompt 143 wrote this document, the measurement scripts and the metric-name
> check. Under the owner directive of 2026-10-17, no part of the runtime verification was performed: the
> application was not started, no source was enabled, nothing was measured, and no drill, soak, smoke run
> or Newman run took place. Every "Measured" cell below is empty until the owner runs the procedure.
> Fill in the cells, and replace this banner with the run's dates and environment.

## 1. Environment

Record before anything else. Every command here is Linux; nothing assumes macOS.

| Item | How to read it | Value |
|---|---|---|
| CPU model and cores | `lscpu` | |
| RAM | `free -h` | |
| Disk type and free space under Docker's data root | `lsblk -d -o NAME,ROTA,MODEL`; `df -h "$(docker info -f '{{.DockerRootDir}}')"` | |
| Kernel | `uname -r` | |
| Docker and Compose versions | `docker version -f '{{.Server.Version}}'`; `docker compose version` | |
| Clock synchronized (Core-Data-Model §16 clock guard) | `timedatectl` shows `System clock synchronized: yes` | |
| Foresignal commit | `git rev-parse HEAD` | |
| Memory profile | `FORESIGNAL_PROFILE` (`small` or `large`, Weather-Imagery-Design §2.6) | |
| Local model (Ollama) | the model tag pulled for `local-llm`, and why it was chosen | |

Credentials come only from `FORESIGNAL_VERIFY_*` variables the owner supplies (Slack webhook, AISStream key,
FIRMS key, Tailscale auth key, ntfy topic, and APNs or FCM keys if wanted). A step whose credential is
missing is recorded **SKIPPED (no credential)**. No other credential is reused.

## 2. Profiles: startup and footprint

Bring up `docker compose up -d`, then add one profile at a time. Startup to ready is measured from the moment
Postgres answers `pg_isready` to the first `200` from `/actuator/health/readiness`.

| Profile | Startup to ready (NFR-4: ≤ 20 s) | Steady RSS per container (`docker stats --no-stream`) | Verdict |
|---|---|---|---|
| default | | | |
| `local-llm` | | | |
| `satdump` (station, or the container, inbox mount and read-only bind with a recorded dataset) | | | |
| `web` (Caddy `tls internal`, HTTP/2 and HTTP/3) | | | |
| `tailscale` (section 9) | | | |

| Memory profile, one hour each | Projection (Weather §2.6) | Heap envelope (`jvm_memory_used_bytes{area="heap"}` on `/actuator/prometheus`) | Container RSS | Verdict |
|---|---|---|---|---|
| `small` | `-Xmx4g`, container limit 6 GB | | | |
| `large` | `-Xmx8g`, container limit 11 GB | | | |
| idle, 10 sources (NFR-1) | ≤ 512 MB heap | | | |

## 3. Sources and upstream etiquette (24 hours)

Enable through `PUT /v1/sources/{sourceId}`: `usgs-earthquakes`, `adsblol-mil` and one adsb.lol regional or
global source, `opensky-states`, `aisstream` (if keyed), `celestrak-gp`, `launch-library`, `nasa-firms` (if
keyed), `gdelt-events`, `nws-alerts`, `nhc-cyclones`, `gdacs`, `ioda-outages`, and the weather set `mrms`,
`goes-east-abi`, `goes-glm`, `goes-fdc`, `gfs`, `hrrr`, `mrms-probsevere`, `spc-outlooks`, `awc-metars`, plus
any local receivers.

Per source: budget use from `GET /v1/sources/{sourceId}/usage` against the descriptor (Charter G2); 429 and 503
from `scripts/verify/measure.sh` section 2 with the gap to the next fetch (section 2b) to show `Retry-After`
was honored; 304s from section 2; health from `GET /v1/sources/health`. Capture the User-Agent once, on one
keyless source, through a local logging proxy.

| Source | Budget kept | 429/503 honored | 304s seen | Health | Notes |
|---|---|---|---|---|---|
| | | | | | |

Known-risk upstreams, recorded and never worked around:

- **adsb.lol HTTP 451** to non-browser clients: the source must show `failing` with a clear reason. The
  User-Agent is never spoofed.
- **CelesTrak 403 "not updated"**: treated as not-modified, not as a failure.
- **OpenSky `x-rate-limit-remaining`**: the reserve is kept.

## 4. Measurements

Every number comes from `scripts/verify/measure.sh` (queries in `scripts/verify/measure.sql`) or from
`GET /v1/system`, run on the live instance. Record the method, the raw output file name and the window.

### 4.1 Ingest

| Measure | Method | Projection | Measured | Verdict |
|---|---|---|---|---|
| Observations/min, total and per source | `measure.sh report 24`, sections 1 and 1b | | | |
| Versions/min, total and peak | sections 1b and 1c | sustained 20,000/min (NFR-2); warn above 20,000 | | |
| `persistRatio` per source | section 1; `storage.persistRatio` in source health | | | |
| `triggerEdgeShare` | sections 1 and 1d | warn above 5% | | |
| Late and clamped per hour | section 1 | | | |
| ChangeBus lag | `GET /v1/system` `catchUp` | | | |
| SSE latency, commit to client | a timestamped client on `/v1/changes/stream` (or `/v1/events/stream`) comparing `ingestedAt` to receipt | ≤ 2 s (NFR-3) | | |

### 4.2 Storage

| Measure | Method | Projection | Measured | Verdict |
|---|---|---|---|---|
| History per tier every 6 h (partitions, rows, bytes, headroom) | `GET /v1/system` `history`; sections 3b and 3c | | | |
| `record_head`, `record_live` total size | section 3 | | | |
| Bloat | section 3g (only when `pgstattuple` is already installed) | | | |
| `record_live` HOT update ratio | section 3, `hot_update_ratio` | high (Core-Data-Model §8) | | |
| History bytes per day | section 1b, `version_bytes_per_day_projected` | 10–15 GB/day (§16) | | |
| 3-day TRACK window | section 3b over 3 days | ≈ 40M rows, ≈ 20 GB (§16) | | |
| 7-day projection | daily bytes × 7 | | | |

### 4.3 WAL and checkpoints

Run `scripts/verify/measure.sh wal-sample` hourly (for example from cron). Each run prints the WAL bytes and
checkpoints since the previous sample, and a per-day projection.

| Measure | Projection | Measured | Verdict |
|---|---|---|---|
| WAL per day | 15–20 GB/day (§16) | | |
| Checkpoints timed and requested per hour | `checkpoint_timeout=15min`; requested checkpoints mean `max_wal_size=8GB` is too small | | |

### 4.4 Weather (Weather-Imagery-Design §9, per memory profile)

| Product | Download/day projected | Measured | Decode CPU projected | Measured | Rolling disk projected | Measured |
|---|---|---|---|---|---|---|
| MRMS reflectivity, precipitation, MESH | 1.7–3.4 GB | | ~10 min | | ~2 GB | |
| MRMS second wave | ~0.5 GB | | ~3 min | | ~0.5 GB | |
| GOES-19 CMIPC C13 | 1.0–1.1 GB | | ~2 min | | ~1.2 GB | |
| GLM | ~1.5 GB | | ~2 min | | 0.4 GB | |
| FDC / ProbSevere | 0.15 / 0.19 GB | | < 1 min | | records only | |
| GFS | ~0.5 GB | | ~1 min | | ~0.5 GB | |
| HRRR | 0.4–0.8 GB | | 1–3 min | | ~0.4 GB | |
| **Total** | **5–8.5 GB/day** | | **0.05–0.1 core average** | | **10–12 GB** | |

Also record: render queue depth and `RENDER_BUSY` answers while a client animates `/loop`; cache hit and
eviction ratios from the `/v1/system` `rasters` block; and every cache size changed, with the evidence.

### 4.5 Retention

One full RetentionJob cycle: TRACK leaf drop time, STANDARD delete throughput, head purges, `seq_clock`
pruning (section 3e before and after), partition headroom after (section 3c), and the meters
`foresignal.retention.duration` and `foresignal.retention.deleted`.

| Measure | Measured |
|---|---|
| TRACK leaf drop duration | |
| STANDARD rows deleted per second | |
| Head purges | |
| `seq_clock` rows pruned | |
| Partition headroom after | |

## 5. Charter §14 success criteria

| # | Criterion | How it is checked | Result |
|---|---|---|---|
| 1 | 7 days with core sources, no budget exceeded | the soak (section 11) and section 3 | |
| 2 | `mag >= 6` (`enter`) delivered by SSE, Slack or Mattermost, ntfy and Web Push, with an LLM summary | section 6 | |
| 3 | A frontend renders live aircraft and earthquakes from the spec, GeoJSON/MVT and SSE | a client against the live instance | |
| 4 | All tests pass with ≥ 90% coverage | `./gradlew test jacocoTestReport jacocoTestCoverageVerification` at code-complete | |
| 5 | Health flags a simulated format change and the doctor explains it | section 7, format-change drill | |
| 6 | Daily briefing for 7 days, digest and LLM, with a local model | the soak | |
| 7 | An MCP agent with an `agent` token answers, creates a watch, cannot change sources or credentials | section 6 | |
| 8 | A script pushes records through the inbound webhook and they trigger alerts | section 6 | |
| 9 | 100 resolved forecast questions after 90 days with positive Brier skill | outside a 7-day run; recorded as pending | |
| 10 | Four consecutive weekly assessments | outside a 7-day run; recorded as pending | |

## 6. Features end to end

| Check | Procedure | Result |
|---|---|---|
| Trigger `mag >= 6` (`enter`) and a geofence rule | create both; add `mag >= 2.5` to force live alerts in the window | |
| SSE delivery | a client on the alert stream | |
| ntfy | a real topic (`FORESIGNAL_VERIFY_NTFY_TOPIC`) | |
| Slack, Mattermost | only when the webhook is given | |
| Web Push via UnifiedPush on ntfy | register a UnifiedPush endpoint | |
| Email | a local Mailpit container as the SMTP server | |
| Commit to channel latency | alert `createdAt` against the triggering version's commit; NFR-12 ≤ 10 s | |
| Enrichment with the local model | the alert carries the summary | |
| Daily digest and LLM briefing to ntfy | `POST /v1/briefings/{briefingId}/run`; the detached JWS verifies against `/v1/system/jwks` | |
| Ask | five fixed questions over live data on `/v1/ask`; every citation resolves; latency and tokens recorded | |
| MCP | an `agent` token; query data, create a watch; `update_source`-type and credential operations refused | |
| APNs, FCM | only with owner keys; otherwise SKIPPED | |
| Custom source | push records with a script to `/v1/ingest/{sourceId}` and trigger an alert | |
| Spot checks | OGC API Features in QGIS, CAP Atom, an iCalendar subscription, GeoParquet in DuckDB, `aircraft.json` in tar1090, a sink never emitting OpenSky or aisstream data | |

## 7. Failure and recovery drills

| Drill | Procedure | Timings | Result |
|---|---|---|---|
| Format change | point `usgs-earthquakes` at a local stub serving a renamed field through `foresignal.test.adapters.usgs.base-url`; expect `degraded` and an automatic doctor report naming the field (`GET /v1/sources/{sourceId}/doctor/reports`) | | |
| Readiness during forced catch-up | see the note below | | |
| Restore | below | | |
| Storm | below | | |
| Restart under load | `docker compose restart foresignal` at peak ingest: no duplicate versions, clock guard behavior, streaming reconnect ladder, no budget burst after restart | | |
| Disk full | below | | |

**Readiness during catch-up: the code and the prompt disagree.** Prompt 143 expects
`/actuator/health/readiness` to report not ready during a forced catch-up. As built, catch-up is a separate
probe group, `/actuator/health/catchup`, which the Operator Guide describes as "deliberately not part of
readiness", and `HealthProbesIT` asserts that independence. No trigger-evaluation pause exists either:
`grep -rln -i "pause" src/main/java/ai/foresignal/trigger` prints nothing. Until the owner decides, run the
drill by loading ingest until `catchup` reports DOWN (for example by enabling dense TRACK sources), and record
`catchup`, `readiness` and `liveness` over time. Liveness should stay UP.

**Restore drill** (scratch project `-p foresignal-restore`, never the live volumes):

1. Back up as the Operator Guide "Backups" section says: `pg_dump -Fc` plus `secret.key`.
2. `docker compose -p foresignal-restore up -d db`, then `pg_restore` into it, copy `secret.key` into the
   scratch `foresignal` container's data directory, and start it on another port.
3. Check: row counts per table against the source (`COMPOSE_PROJECT=foresignal-restore
   scripts/verify/measure.sh` section 3); `record_seq` ≥ max(seq) (section 3d); partitions attached (3c) and
   created ahead; credentials decrypt; the signing key and `/v1/system/jwks` unchanged; triggers fire on new
   data; an SSE resume from a pre-backup `after` gets `reset` or continues correctly.
4. Record the duration and every step; correct the Operator Guide where a step was wrong.

**Storm scenario:** on a day with SPC Day-1 Slight risk or higher over the operator's area, soak 12 hours
across the event: radar cells, ProbSevere, GLM clusters, NWS alerts, place-weather versions, render load,
versions/min peaks, and alert storm control (`max_alerts_per_hour`, folding). Without such a day, run the
reduced variant (the 115 exercise replay of a retained stormy window against opted-in test channels) and keep
the live storm soak on the checklist.

**Disk-full drill:** on a small dedicated volume for the database, fill to 95% and then 100%. Confirm the
readiness probe and the `/v1/system` warnings fire before the volume is full, the writer stops cleanly (no
partial batches, no damaged partitions), and recovery after freeing space needs no manual SQL. Record the
thresholds observed.

## 8. Capacity and the intelligence layer

| Job | Wall time | Peak heap | Database load | Tokens | Writer latency change (≤ 10%) |
|---|---|---|---|---|---|
| Daily cycle (128) | | | | | |
| Weekly cycle | | | | | |
| Monthly cycle | | | | | |
| Benchmark suites (123), synthetic and historical | | | | | |
| Precursor mining (120) | | | | | |
| Base-rate refresh (121) | | | | | |

From the measured persist ratios and versions/min, state the realistic limits for this hardware (sources,
dense TRACK feeds, retention years) and the disk needed for 1, 3 and 5 years with the analytic retention
defaults, then put them in the Operator Guide.

## 9. Remote access and metric names

- **Tailscale** (`FORESIGNAL_VERIFY_TS_AUTHKEY`): HTTPS through `tailscale serve`; `publicBaseUrl` is https
  with no warning; forwarded headers honored only from the trusted proxy; push actions and share links use the
  tailnet URL; 8080 and 5433 still bound to 127.0.0.1 (`ss -ltnp`); a phone pairs over the tailnet.
- **Metric names:** `FORESIGNAL_TOKEN=… scripts/verify/metric-drift.sh` compares the live
  `/actuator/prometheus` scrape and the Grafana dashboards against `docs/Metrics.md`. It exits 1 on drift.
  `MetricNamesDocumentedTest` makes the startup half of the same check in the suite.

| Check | Result |
|---|---|
| `metric-drift.sh` | |
| Tailscale HTTPS and `publicBaseUrl` | |
| Loopback binds (`ss -ltnp`) | |
| Phone pairing over the tailnet | |

## 10. API conformance

| Run | Command | Result |
|---|---|---|
| Smoke, live instance | `FORESIGNAL_TOKEN=… scripts/smoke.sh` | |
| Newman, scratch instance `-p foresignal-verify` | `npx newman run postman/Foresignal.postman_collection.json -e postman/Foresignal-Local.postman_environment.json --env-var adminToken=$TOKEN --reporters cli,junit` | |

## 11. Seven-day soak checklist

Soak start: _not started_ (record the UTC time when it begins).

| Day | Budgets within limits | No source failing without a reason | Briefings delivered (digest, LLM, local model) | Storage growth vs projection | WAL and checkpoints | Partition headroom | Retention ran | Backup succeeded | Alert latency sample | Memory (heap, RSS) |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | | | | | | | | | | |
| 2 | | | | | | | | | | |
| 3 | | | | | | | | | | |
| 4 | | | | | | | | | | |
| 5 | | | | | | | | | | |
| 6 | | | | | | | | | | |
| 7 | | | | | | | | | | |

Daily sources for each column: `GET /v1/sources/health?status=failing,degraded`; `measure.sh report 24`;
`measure.sh wal-sample`; `GET /v1/system` (`history`, `warnings`, `catchUp`); `GET /v1/briefings`; the backup
job's log; `docker stats --no-stream`.

## 12. Tuning applied

| Setting | Before | After | Evidence |
|---|---|---|---|
| | | | |

## 13. Defects found and fixed

| Defect | Fix commit | Regression test |
|---|---|---|
| | | |

## 14. Open risks

- Readiness does not include catch-up, contrary to prompt 143 (section 7). Owner decision.
- No trigger-evaluation pause exists for the catch-up drill (section 7).
- Criteria 9 and 10 need 90 days and four weeks; a 7-day soak cannot close them.
