# 021-CR-End-To-End-Milestone

| | |
|---|---|
| Prompt | 021-End-To-End-Milestone.md |
| Status | Complete |
| Implementation commit | cb13cc3e43c86aa17c567fcf8c7045936c81bca2 |
| Date | 2026-10-11 |

## Summary

One end-to-end test now proves the core (001–020). The project is also documented for a human
operator, and the deployment files exist.

**End-to-end test (Task 1).** `ai.foresignal.e2e.MilestoneE2EIT`:

- **Context:** `@WebIntegrationTest` (real HTTP on a random port) against the shared
  `postgis/postgis:17-3.5` Testcontainer, the same image compose uses.
- **Upstream:** WireMock serves the USGS fixtures through `foresignal.test.adapters.usgs.base-url`
  (the 011 override).
- **Receivers:** one separate WireMock server each for the webhook, Slack and Discord.
- **Seeding:** none by SQL. Records come from the adapter, and rules and channels come from the API.
- **One test method covers the 15 steps:**
  1. the token from the test hook `AdminTokenService.rotate()`; `PUT /v1/sources/…` and
     `POST /v1/triggers` without it are 401;
  2. `/v1/sources` lists `usgs-earthquakes`: disabled, status `disabled`, `STANDARD`,
     `licence = US-PD`;
  3. `POST /v1/triggers` with the prompt's body returns 201;
  4. three `POST /v1/channels` (webhook with `hmacSecret` and `events:["alert"]`, Slack, Discord)
     return 201;
  5. the change stream (`touches=true`, plus `payload=true`) and `/v1/alerts/stream` are opened;
  6. after `PUT {"enabled":true}`, within 5 s 25 distinct `change` events arrive, all `FIRST`.
     Then: the GeoJSON FeatureCollection has 25 features and a numeric `watermarkSeq`; health is
     `healthy`; usage shows ≥ 1 request; `/fetches` has one `OK` `SCHEDULED` row with
     `versionsWritten = 25`;
  7. an identical re-poll through `POST /fetch` gives `persisted = 0`, at least one `touch` event,
     no `change` event (checked 1.5 s after a heartbeat that follows the touch), a newest fetch row
     with `versionsWritten = 0` and `touches ≥ 1`, and every record still at version 1;
  8. T1 is recorded, and `us6000u1mj` becomes M 6.4. There is exactly one `change` (version 2,
     `SIGNIFICANT`, payload `mag` 6.4), and `/versions` lists 2. A `fired` alert event carries the
     rendered message `M6.4 151 km W of Gorontalo, Indonesia`. The webhook POST carries a valid
     `X-Foresignal-Signature`, recomputed in the test with `javax.crypto` rather than with
     `HmacSigner`, and an `X-Foresignal-Origin` header. Slack gets `blocks` containing the place.
     Discord gets a POST with `wait=true`. Notification rows exist;
  9. `asOf=T1` returns version 1 with `mag` 5.3, and echoes `asOf` and `horizon`;
  10. the alert appears in `/v1/alerts?ruleId=…`, and `POST /ack` answers `acknowledged`;
  11. M 5.8 writes version 3. `alert.resolved` arrives on SSE. Discord gets
      `PATCH …/messages/111` containing "Resolved", and never a PATCH of the other message (`110`).
      Slack gets a "Resolved" follow-up. There is exactly one Discord POST for the revised quake;
  12. `/v1/tiles/earthquake/0/0/0.mvt` returns 200 and decodes to features;
  13. `/v1/system` has no `history` anonymously. With the token, `history.tiers` has 2 entries
      and `warnings` is an array;
  14. after `PUT {"enabled":false}`, the WireMock request count stays the same for 5 s (Awaitility
      `during`);
  15. `POST /v1/settings/retention/run` returns counts with `versionsDeleted = 0` and
      `headsPurged = 0`. All 25 records are still readable, and the revised quake still has 3
      versions.
- **Robustness:**
  - Every wait uses Awaitility with an explicit timeout.
  - Each SSE stream is drained on a virtual thread into a bounded recorder (5,000 events;
    overflow fails the next assertion), so skipped events are kept in order.
  - Heartbeats are set to 1 s (`stream.limits`) so the "no event" window is fixed and bounded.
- **New fixtures:** `e2e-mag-6.4.geojson` and `e2e-mag-5.8.geojson` are `all_day` with only
  `us6000u1mj`'s `mag`, `updated` and `title` changed. They are documented in the fixture README.

**Smoke script (Task 2).** `scripts/smoke.sh` is POSIX `sh` using `curl` and `jq`, configured with
`FORESIGNAL_URL`, `FORESIGNAL_TOKEN` and `SMOKE_WAIT`. It:

- checks `/v1/system` (including that the token unlocks `history`) and readiness `UP`;
- lists sources and enables `usgs-earthquakes`;
- polls for records and asserts a numeric `watermarkSeq`;
- creates a disabled test trigger, deletes it (204) and confirms 404;
- prints a summary and exits non-zero on the first failure.

**Deployment (Task 3):**

- `docker-compose.yml` has a `tailscale` service under the `tailscale` profile:
  - image `tailscale/tailscale:v1.102.5`, the current `stable` tag on Docker Hub (verified
    2026-10-11 through the Docker Hub tags API);
  - `TS_AUTHKEY` comes from the operator's environment (`${TS_AUTHKEY:-}`, so a default
    `docker compose up` still parses);
  - `TS_HOSTNAME=foresignal`, `TS_STATE_DIR` on the named volume `tailscale-state`, and
    `TS_SERVE_CONFIG=/config/serve.json`;
  - no published ports.

  The broker's bind is unchanged (`${FORESIGNAL_BIND:-127.0.0.1}`).
- `docker/tailscale/serve.json` serves HTTPS 443 on `${TS_CERT_DOMAIN}`, proxies to
  `http://foresignal:8080`, and leaves Funnel off. The format is the one in Tailscale's Docker guide.
- `docker/caddy/Caddyfile.example` has automatic HTTPS, a commented `tls internal` LAN variant,
  `flush_interval -1`, and a comment about 023's trusted-proxy setting.
- `docker/avahi/foresignal.service` advertises `_http._tcp` and `_foresignal._tcp` on 8080, with TXT
  `path=/v1` and `api=v1` only.
- `docker compose --profile tailscale config -q` validates. No container was started.

**Documentation (Task 4):**

- **README:**
  - the quickstart (compose, the token grep, the USGS enable curl, `curl -N` on the change stream,
    the smoke script);
  - local development, links to `/docs` and the Operator Guide, and Postman (import, `adminToken`,
    the quickstart folder, the `newman` command);
  - updated deployment notes and doc links;
  - the stale "Status" section (007 Issue 4) is fixed.
- **`docs/Operator-Guide.md`** keeps every 018 and 019 section and adds:
  - first start and the token (rotate, reset);
  - credentials and budgets, and health, usage and fetches at a glance;
  - why TRACK refuses a per-source window; keyframes, the retention keyframe, touches and
    §5.3 defaults;
  - triggers and the alert lifecycle (magnitude threshold, geofence, enter and exit with
    auto-resolve, cooldown);
  - streaming for frontends (snapshot, `watermarkSeq`, resume, `reset`, `resync`,
    `410 RESYNC_REQUIRED`), tiles and asOf;
  - "Running the host": binds, remote access and TLS, mDNS, clock and NTP, storage projection with
    the §16 flags, backups with a restore drill, and troubleshooting.
- **`docs/Roadmap.md` §4:** 001–021 are marked Done.

## Files created / modified

- Created:
  - `src/test/java/ai/foresignal/e2e/MilestoneE2EIT.java`
  - `src/test/java/ai/foresignal/deploy/ComposeFilesTest.java`
  - `src/test/resources/fixtures/usgs-earthquakes/e2e-mag-6.4.geojson`, `e2e-mag-5.8.geojson`
  - `scripts/smoke.sh` (executable)
  - `docker/tailscale/serve.json`, `docker/caddy/Caddyfile.example`, `docker/avahi/foresignal.service`
- Modified:
  - `docker-compose.yml` (the `tailscale` service and volume, and the header comment)
  - `README.md`, `docs/Operator-Guide.md`, `docs/Roadmap.md`
  - `src/test/resources/fixtures/usgs-earthquakes/README.md`
- No production code, spec, `AccessPolicy` or `pending-operations.txt` change: the spec assigns no
  operation to 021 (`grep -c '"021"' docs/OpenAPI.json` → 0).

## Deviations from the prompt

1. **The capture already holds an M 6.2 quake.** `us6000u1kv` (M 6.2) is in the real
   `all_day.geojson`, so the `enter` rule fires on its FIRST version in step 6. A new entity counts
   as entering, per `RuleSqlBuilder.enter`: `v.prev_seq IS NULL`.
   - I kept the real capture rather than edit it.
   - Step 6 also asserts that alert and its delivery to all three receivers.
   - The revised feature is `us6000u1mj` (M 5.3 → 6.4 → 5.8). Every later assertion selects by
     that external id.
   - The Discord stub returns `{"id":"111"}` for `?wait=true` posts whose body contains
     `us6000u1mj`, and `{"id":"110"}` for any other. The test asserts the resolve edits 111 and
     never 110.
2. **The clock is moved past `touchMin` before the identical re-poll.** Inside `touchMin` (1 h for
   USGS, which has no TTL) an identical observation is TOUCH_THROTTLED. The writer publishes no
   `touch` event for it (`IngestWriter` adds touches only for `plan.touches > 0`). So the
   `MutableClock` is advanced 61 minutes, which makes it a real TOUCH, as Core-Data-Model §5.2
   row 11 defines.
3. **Interval and budget are set before the enable.** One `PUT` with
   `{"interval":"1h","budget":{"perMinute":600,"perDay":100000}}` runs while the source is still
   disabled, so the enable itself is exactly `{"enabled":true}`.
   - The descriptor's default budget (4 per minute) would refuse the later manual fetches.
   - The 60 s interval could start a scheduled poll that races a manual fetch (`409
     ALREADY_RUNNING`).
4. **The delivery worker is driven with `DeliveryWorker.runOnce()`.** The test profile keeps
   `foresignal.notify.scheduled=false`. A scheduled worker in this cached context would claim
   deliveries that other notification ITs drive by hand.
   - Each delivery round advances the clock 1 s, so rate buckets refill and retry delays fall due.
   - The collector scheduler does run for real (`foresignal.collector.scheduled=true`, as in
     `UsgsIngestIT`), so step 6's fetch is the scheduler's reaction to the API enable.
5. **The change stream also sets `payload=true`,** to assert the revised magnitude on the event.
   Usage is read with an explicit `from`/`to` around the frozen clock, because the default range
   ends exactly at "now" and is exclusive.
6. **Heartbeat interval.** `stream.limits.heartbeatSeconds` is set to 1 through `AppSettingService`.
   There is no settings endpoint for stream limits yet. This is a setting, not a record.
7. **A `hostname:` was deliberately left off the tailscale service.** With `hostname: foresignal`,
   the name `foresignal` inside that container would resolve to itself, not to the broker that
   `serve.json` proxies to.

## Issues found / open questions

1. **`publicBaseUrl` has no endpoint yet.** The Operator Guide tells the operator to set it for
   remote access, but `GET`/`PUT /v1/settings/server` are planned for 023. The guide says so.
2. **Architecture §14 describes a `docker/db` image (PostGIS + pgvector).** The compose file and
   the tests use `postgis/postgis:17-3.5`, and no `docker/db` exists. **Owner:** which one should
   change? The E2E uses the image compose uses.
3. **The smoke script leaves `usgs-earthquakes` enabled,** as the prompt describes, and says so in
   its summary. 143 may want to disable it afterwards.
4. **`shellcheck` is not installed** on this host (`which shellcheck` found nothing).
   - The script was written to be clean and passes `sh -n` and `dash -n`.
   - Its failure paths were exercised against a closed port and without a token: exit 1, with clear
     messages.
5. **Unverified until checkpoint 022:**
   - the E2E timing, in particular "within 5 s" for the first scheduled fetch;
   - template rendering of `6.4` as `6.4`, as `MessageTemplateTest` shows for `6.1`;
   - the tile at z0 containing features.
6. **No instructions were found in data.**
7. Owner decisions carried from 012–020 are still open.

## Tests written (not run)

- **`MilestoneE2EIT`** (integration, its own cached context: web, `MutableClock`, collector
  scheduled, USGS base URL): one test covering the 15 steps above.
- **`ComposeFilesTest`** (unit; SnakeYAML 2.6, already on the test classpath through Spring Boot,
  and DOM with external DTD loading off):
  - every published port is `${VAR:-127.0.0.1}:…`, with exactly two mappings (8080 and 5433);
  - `tailscale` has `profiles: [tailscale]` and is the only service with a profile. It has a pinned
    `vX.Y.Z` image, no ports and no hostname, the three `TS_*` values and an env-sourced
    `TS_AUTHKEY`, and the `tailscale-state` volume exists;
  - `foresignal` has a readiness healthcheck;
  - `serve.json` has HTTPS 443, the `${TS_CERT_DOMAIN}` proxy to `http://foresignal:8080`, and
    Funnel false;
  - the Caddy example has `flush_interval -1`, `tls internal` and the 023 note;
  - the Avahi file has two services (`_http._tcp`, `_foresignal._tcp`) on 8080. Their TXT records
    are exactly `path=/v1` and `api=v1`, and no value matches the install-id shape
    (8 × `[a-z0-9]`) or names install, uuid or serial.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL in 591ms`. No new dependencies.

## Notes for the next prompt

1. **022 (checkpoint):**
   - `MilestoneE2EIT` adds one cached web context with the collector scheduler on.
   - It runs for roughly 15–20 s: 5 s stable-count wait, 1.5 s quiet window, and delivery rounds.
   - If step 6's 5 s wait proves tight, check whether `SourceSettingsChangedEvent` reaches the
     scheduler before or after the commit, before widening the timeout.
   - If the M 6.2 FIRST alert changes delivery counts unexpectedly, see Deviation 1.
   - `ComposeFilesTest` reads files relative to the project root, as `OpenApiSpec` does.
2. **023:** `PUT /v1/settings/server` (`publicBaseUrl`) and the trusted-proxy setting are referenced
   by the Operator Guide and the Caddy example. Update both sections when they land.
3. **050:** the Caddy compose profile should use `docker/caddy/Caddyfile.example` as its base.
4. **143:** `scripts/smoke.sh` takes `FORESIGNAL_URL` and `FORESIGNAL_TOKEN` and leaves USGS enabled.
5. **Refreshing the USGS capture:** the two `e2e-mag-*` fixtures and `MilestoneE2EIT` ids must be
   rebuilt too (fixture README).
6. **Owner decisions pending:** Issue 2, plus those carried from 012–020.
