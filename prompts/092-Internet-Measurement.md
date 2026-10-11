# 092 — Internet Measurement

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/092-CR-Internet-Measurement.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/091-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `cloudflare-radar-outages`, `ripe-atlas-probes`, `gpsjam-daily`, `ioda-outages` (country-centroid table)
- `docs/Gap-Analysis.md` §4b rows "BGP (Cloudflare Radar events, RIS Live)", "Status pages", "Grid ops … ODIN outages (terms unverified)"; §8 item 50 (labelled 054 = this prompt)
- `docs/Core-Data-Model.md` §2 (time base; "Catalogue and reference" row), §3 (descriptor, adapter rules 7–13), §5.3 (everything here is STANDARD, ALL), §7 (streaming sinks)
- `docs/Adapter-Guide.md`; Completion Reports for 068 (country centroids), 073 (RSS/Atom kit and reviewed feed list), 040 (gazetteer)
- Upstream docs: Cloudflare Radar outages `developers.cloudflare.com/api/resources/radar/subresources/annotations/subresources/outages/methods/get/`, BGP hijacks and leaks `…/radar/subresources/bgp/subresources/{hijacks,leaks}/subresources/events/methods/list/`, API limits `developers.cloudflare.com/fundamentals/api/reference/limits/`; RIPE Atlas `ftp.ripe.net/ripe/atlas/probes/archive/README` and Service Terms; gpsjam `gpsjam.org/faq`; Statuspage `<page>/api/v2`; RIS Live `ris-live.ripe.net/manual/`

## Objective

Add the internet-measurement sources: Cloudflare Radar outages and BGP events, RIPE Atlas probe status, gpsjam.org daily jamming cells, service status pages, and an optional narrow RIS Live prefix monitor. Together with IODA (068) they let rules and the 099 corroboration layer tell a national outage from a cable cut, a BGP hijack or a cloud-provider incident.

## Tasks

### 1. Shared pieces

- **H3:** add `com.uber:h3` 4.4.0 (latest confirmed on Maven Central, verified 2026-10; check for a newer 4.x) to `gradle/libs.versions.toml`. It is JNI with bundled natives; confirm linux-x86_64 and linux-aarch64 load in the Docker image and note it in `THIRD_PARTY_NOTICES.md` (Apache-2.0). Add `ai.foresignal.geo.H3Cells` (`cellToPolygon(String|long)`, `latLngToCell(lat, lon, res)`, `isValid`), shared with 099.
- **Country geometry:** reuse the 068 country-centroid table. Records located only by country get a Point (or MultiPoint) and payload `geometryApproximate: true`.
- All adapters: identifiable UA, conditional GET where the upstream supports it, `Retry-After` honoured, untrusted upstream text capped at 2 KB and listed in `embedFields` only when it is human prose.

### 2. `cloudflare-radar-outages`

- `GET https://api.cloudflare.com/client/v4/radar/annotations/outages?dateRange=7d&limit=100&format=JSON` (params `dateRange` default `7d`, optional `location` alpha-2 list and `asn`). Credential `apiToken` (Bearer; create a custom token with Radar read access; Cloudflare's docs word the permission inconsistently, so document both forms). No conditional GET support is documented.
- Interval 1 h (min 15 min), budget 2/min and 60/day. Cloudflare's global limit is 1,200 requests per 5 min per user; honour `retry-after` on 429.
- Kind `internet-outage`, `externalId = cf-outage:{id}`, STANDARD, ALL, no TTL, `observedAtMode=RECEIVED` (annotations carry no update time), `eventTime = startDate`.
- Payload: `startDate, endDate, ongoing` (endDate null), `locations[]`, `asns[]`, `asnNames[]` (from `asnsDetails`), `outageCause, outageType, scope, description, linkedUrl, eventType, dataSource`. Geometry: centroids of `locations`, else of `asnsDetails[].location`.
- `licence = "CC BY-NC 4.0"`, attribution "Cloudflare Radar". Explicit tombstones are not needed (annotations stay valid as history).

### 3. `cloudflare-radar-bgp`

- One source with kinds `bgp-hijack` and `bgp-leak`. Each run: `GET /radar/bgp/hijacks/events?dateRange=1d&minConfidence={p}&per_page=100&page=n&sortBy=TIME&sortOrder=DESC` and `GET /radar/bgp/leaks/events?dateRange=1d&per_page=100&page=n`, at most 5 pages each, stopping at `result_info.total_count`. Params: `minConfidence` (default 8, the "high" band; hijacks only), `involvedAsn` (string-list), `involvedCountry` (string-list).
- Credential `apiToken`, interval 15 min (min 5 min), budget 6/min and 600/day, `licence = "CC BY-NC 4.0"`.
- Hijack: `externalId = hijack:{id}`, `eventTime = min_hijack_ts`, `observedAt = max_msg_ts` (UPSTREAM; fall back to `max_hijack_ts` only if the field is absent in the whole response, decided per response shape and logged). Payload `confidence_score, event_type, hijacker_asn, hijacker_country, victim_asns, victim_countries, prefixes` (≤ 50), `is_stale, duration, tags[{name,score}]`, AS names from `result.asn_info`. `volatileFields = [hijack_msgs_count, on_going_count, peer_ip_count, max_msg_ts]`.
- Leak: `externalId = leak:{id}`, `eventTime = min_ts`, `observedAt = max_ts`. Payload `leak_asn, leak_type, leak_seg, countries, finished, origin_count, peer_count, prefix_count`; `volatileFields = [leak_count]`.
- Geometry: country centroids (hijacker plus victims; leak `countries`), approximate.

### 4. `ripe-atlas-probes`

- **Primary:** daily `GET https://ftp.ripe.net/ripe/atlas/probes/archive/meta-latest` (about 2.3 MB bzip2 JSON `{"objects":[…]}`, regenerated around 02:16 UTC, with ETag and Last-Modified, verified 2026-10). Decompress with Apache Commons Compress (add it, Apache-2.0). Interval 24 h (min 6 h), budget 1/min and 10/day.
- **Optional intraday:** param `intradayDisconnected` (default false) polls `https://atlas.ripe.net/api/v2/probes/?status=2&page_size=500&fields=id,status,status_since,last_connected` every 30 min (about 5 pages), updating only the status of probes in that list.
- Status codes (verified 2026-10): 0 Never Connected, 1 Connected, 2 Disconnected, 3 Abandoned, **4 Written Off**; about 61k probes in total. Param `statuses` (default `1,2`) filters the dump; probes leaving the filter are tombstoned (keep the previous emitted id set in adapter state).
- Kind `probe-status`, `externalId = probe:{id}`, Point from `longitude/latitude`, STANDARD, ALL, no TTL, `observedAtMode=UPSTREAM` with `observedAt = status_since` (constant per status; a status change moves it forward). Payload `status, statusName, statusSince, asnV4, asnV6, prefixV4, prefixV6, countryCode, isAnchor, isPublic, tags`. **Never store `address_v4`, `address_v6` or `description`** (host-identifying). `volatileFields = [lastConnected, totalUptime]`.
- `licence = "RIPE Atlas Service Terms v3.5 (attribution; commercial use by permission)"`, `termsUrl` the RIPE terms page, attribution "RIPE NCC / RIPE Atlas".

### 5. `gpsjam-daily`

- `GET https://gpsjam.org/data/manifest.csv` (columns `date,suspect,num_bad_aircraft_hexes,source`), pick the newest date after the adapter-state `lastDate`, then `GET https://gpsjam.org/data/{date}-h3_4.csv` (columns `hex,count_good_aircraft,count_bad_aircraft`, H3 res 4, about 1 MB, gzip). No ETag; the manifest date gates the fetch. Interval 6 h (min 1 h), budget 1/min and 8/day. Skip days with `suspect=true` unless `includeSuspect`.
- Compute the site's metric `pctBad = 100·(bad − 1)/(good + bad)` (gpsjam FAQ); emit cells with `pctBad ≥ minPct` (default 2) and `good + bad ≥ 3`. Level `low` (< 2), `medium` (2–10), `high` (> 10).
- Kind `jamming-cell`, `externalId = h3:{hex}`, geometry `H3Cells.cellToPolygon`, `eventTime` = the date (00:00Z), `observedAtMode=UPSTREAM` with `observedAt` = date + 24 h clamped by the normaliser. Payload `date, pctBad` (0.5 bands), `level, goodAircraft, badAircraft, upstreamSource`. Cells from the previous day absent today are tombstoned (id set in adapter state).
- **Terms:** none published; the site says only that it is free to use, and since 2026 merges airplanes.live and ADS-B Exchange data. Disabled by default; `licence = "unspecified — personal use, do not redistribute (gpsjam.org)"`; attribution "gpsjam.org (John Wiseman)". Mark egress local-only for 109 and 110.

### 6. `status-*` family (Statuspage v2) and cloud providers

- `StatusPageFamily` from a reviewed static list (no caller URLs), each polling `GET https://{host}/api/v2/summary.json` with conditional GET (weak ETag, verified 2026-10):

  | Source | Host |
  |---|---|
  | `status-github` | www.githubstatus.com |
  | `status-atlassian` | status.atlassian.com |
  | `status-discord` | discordstatus.com |
  | `status-reddit` | www.redditstatus.com |
  | `status-dropbox` | status.dropbox.com |
  | `status-twilio` | status.twilio.com |
  | `status-zoom` | www.zoomstatus.com (status.zoom.us redirects) |
  | `status-anthropic` | status.claude.com (status.anthropic.com redirects) |
  | `status-cloudflare` | www.cloudflarestatus.com |
  | `status-digitalocean` | status.digitalocean.com |
  | `status-openai` | status.openai.com (incident.io shim: `summary.json` only, ULID ids, no `shortlink`/`components`/`started_at`, no ETag) |

  Fastly is not on Statuspage (verified 2026-10); leave it out.
- Interval 2 min (min 60 s), budget 2/min and 1,500/day. Kinds: `service-status` (`externalId = page`, payload `indicator` none/minor/major/critical, `description`, `degradedComponents[]` names with status) and `service-incident` (`externalId = incident:{id}`, `eventTime = started_at ?? created_at`, payload `name, status, impact, shortlink, components[], latestUpdate` (≤ 2 KB, untrusted), `updateCount`, `resolvedAt`). `observedAt = updated_at` (page `updated_at` for the status record). `embedFields = [name, latestUpdate]`.
- An incident absent from `summary.json` gets one extra `GET /api/v2/incidents.json` to emit its final resolved version, then a tombstone (for the incident.io shim, filter unresolved incidents locally from `incidents.json`).
- `status-gcp` (`https://status.cloud.google.com/incidents.json`) and `status-google-workspace` (`https://www.google.com/appsstatus/dashboard/incidents.json`): kind `service-incident`, `externalId = id`, `eventTime = begin`, `observedAt = modified`, payload `number, externalDesc, severity, statusImpact, serviceName, affectedProducts[], currentlyAffectedLocations[], end, ongoing, latestUpdate` (from `most_recent_update.text`, ≤ 2 KB). Emit every incident in the file (a rolling ~12-month window), complete set. `If-Modified-Since` works but `Last-Modified` advances about every minute, so expect few 304s. Interval 5 min.
- **AWS and Azure:** add entries to the 073 RSS/Atom kit's reviewed feed list, not new adapters: `status-aws` → `https://status.aws.amazon.com/rss/all.rss` (its ETag changes on every request, so conditional GET is ineffective) and `status-azure` → `https://azure.status.microsoft/en-us/status/feed/` (usually empty; only broad incidents are public). Kind `service-incident`. Do not use the undocumented `health.aws.amazon.com/public/currentevents` JSON.
- Licence: provider content, `licence = "provider terms"`, no geometry.

### 7. `ris-live` (optional, streaming, disabled by default)

- WebSocket `wss://ris-live.ripe.net/v1/ws/?client=foresignal-{installId}` (one budget unit per connect; budget 6/hour). Params: `prefixes` (string-list, required, ≤ 50), `expectedOrigins` (map prefix → ASN list, optional), `hosts` (optional RRC list). Send one `{"type":"ris_subscribe","data":{"prefix":p,"moreSpecific":true,"type":"UPDATE"}}` per prefix, plus `{"type":"RIS_PEER_STATE"}`. **Never subscribe to the full firehose.**
- **Peer-visibility tracking** (`PrefixVisibilityTracker`, pure): per watched prefix keep, per `(host, peer)`, the last announced path or a withdrawal; peers count toward the denominator when seen in the last 24 h and not reported down by `RIS_PEER_STATE`. Derived state: `visibility = announcing/known` (0.1 bands), `origins[]` (last AS of each path; AS_SET entries flattened and flagged), `unexpectedOrigin` (origin ∉ `expectedOrigins`), `moreSpecifics[]` (≤ 20).
- Kind `bgp-prefix-state`, `externalId = prefix:{p}`, no geometry, STANDARD, ALL, `observedAtMode=UPSTREAM` (newest message `timestamp`). Push a record to the sink only when the banded state changes or every 10 min; identical snapshots become touches. `peer_asn` arrives as a string.
- Reconnect ladder 5 s → 15 s → 60 s → 300 s with jitter; a server "too slow" error triggers the next rung. Silence watchdog 5 min. While disconnected, set `status: no-data` in the payload rather than lowering visibility.
- `licence = "RIPE NCC RIS terms (attribution; commercial use by permission)"`.

### 8. ODIN

Not implemented. Verified 2026-10: ORNL's public `/odi` endpoint is anonymous and county-level, but no licence, terms of use or rate limit is published, so its terms are not clear. Add a "Considered" note to `docs/Source-Catalog.md` (contact `odin@ornl.gov` before adding it).

### 9. Spec, Postman, docs

- No new operations. Add the new kinds to the `fieldSpecs`-driven kind schemas; regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman` if any schema example changed.
- `docs/Source-Catalog.md`: rows for every source above (tier, policy, TTL, `observedAtMode`, `ttlAnchor`, volatile and significant fields, licence), including the verified 2026-10 differences (Atlas status 4 and the dump, gpsjam merged sources and no licence, new status hosts, OpenAI on incident.io, AWS ETag behaviour). `docs/Operator-Guide.md`: Cloudflare token creation, the RIS Live narrow-subscription rule, rule recipes (hijack `confidence_score >= 8` enter; `service-status.indicator in [major, critical]` enter).

## Tests to write (do not run)

- Parser contract tests with real fixtures (capture each once, trim, record URL and date in `README.md`): `CloudflareOutageParserTest`, `CloudflareBgpParserTest` (hijack and leak, AS names, volatile fields), `AtlasDumpParserTest` (bzip2 fixture, status 4, addresses dropped), `GpsjamParserTest` (pctBad formula, thresholds, suspect day), `StatusPageParserTest` (Statuspage and the incident.io shim), `GoogleStatusParserTest`, `PrefixVisibilityTrackerTest` (announce/withdraw/peer-down, AS_SET, unexpected origin, bands).
- WireMock adapter tests per source: Bearer token sent and redacted, pagination stop at `total_count`, 429 with `retry-after`, conditional GET 304 (Atlas dump, Statuspage), manifest gating (gpsjam fetches nothing when no new date), resolved-incident follow-up request.
- `RisLiveAdapterTest` against an embedded WebSocket server: subscribe messages per prefix, no firehose subscription, reconnect ladder with a controllable scheduler, touch on unchanged state.
- `InternetMeasurementIngestIT` (`@IntegrationTest`, WireMock): outage ingest and re-fetch gives 0 versions; gpsjam day 2 tombstones cleared cells; a resolved Statuspage incident gets a final version then a tombstone.
- `H3CellsTest`: polygon validity, antimeridian cells, invalid index rejected.

## Acceptance criteria

- Every source registers with defaults that pass `IngestPolicyValidator`; `gpsjam-daily` and `ris-live` are disabled by default.
- No stored payload contains RIPE Atlas probe addresses or descriptions.
- Licences are recorded on every descriptor, including the non-commercial and unspecified ones.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `092: Internet Measurement`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/092-CR-Internet-Measurement.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `092: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
