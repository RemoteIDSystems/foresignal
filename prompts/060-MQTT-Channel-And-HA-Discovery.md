# 060 — MQTT Channel And HA Discovery

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/060-CR-MQTT-Channel-And-HA-Discovery.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/059-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §2 "Home Assistant" row, §3 "MQTT + HA discovery" row, §8 item 28 ("028i" = this prompt)
- `docs/Architecture.md` §2 (the MQTT row, which this prompt changes), §17 (channel model, delivery pipeline, security)
- Completion Reports of:
  - 006/013: health views, `SourceHealthChangedEvent`
  - 017: alert lifecycle (fired, acknowledged, resolved), rule tags
  - 018: ChannelType SPI, delivery worker, rate limits, templates, the `homeassistant` channel type
  - 029: briefing runs
  - 035: outbound URL policy for channel destinations
  - 038: GlanceService
  - 039: private payload rules for notifications
- Home Assistant MQTT discovery, https://www.home-assistant.io/integrations/mqtt/ (verified 2026-10):
  - device-based discovery on `homeassistant/device/<object_id>/config` with mandatory `dev` and `o` (origin) blocks and per-component `cmps` entries, each with `p` (platform) and `unique_id`
  - birth message `online` on `homeassistant/status`
  - removal: an empty payload, or a component config with only `p`
  - HA prefers publishing discovery on the birth message over retained config, because retained config can leave ghost entities
- MQTT 5 spec (retain, Last Will, Message Expiry Interval, session expiry)
- **Client library decision (verified 2026-10 on Maven Central):**
  - Eclipse Paho `org.eclipse.paho.mqttv5.client`: last release 1.2.5 in July 2020. Treat it as unmaintained.
  - HiveMQ MQTT Client `com.hivemq:hivemq-mqtt-client`: 1.4.0 released 2026-08-21, Apache-2.0, MQTT 3.1.1 and 5, async API, WebSocket transport. It depends on Netty 4.1.x (4.1.137); 1.3.16 added Netty 4.2 hostname-verification compatibility. A `hivemq-mqtt-client-shaded` 1.4.0 artifact also exists.
  - **Use HiveMQ.** Check the resolved Netty version against the Spring Boot 4.1 BOM (`./gradlew dependencyInsight --dependency netty-handler`). If the versions clash, switch to the shaded artifact and record that in the CR.

## Objective

Publish Foresignal's alerts, briefings, system events, source health and a live summary to the operator's MQTT broker, with a Last Will so consumers know when Foresignal is down. Make Foresignal appear in Home Assistant automatically through MQTT discovery. This is a notification channel (durable deliveries for events) plus a state publisher (retained topics), built on one supervised client library that the later MQTT source adapters (083, 085) reuse.

## Tasks

### 1. Dependency and shared client (`ai.foresignal.mqtt`)

- Add `com.hivemq:hivemq-mqtt-client` (or `-shaded`) 1.4.0 to `gradle/libs.versions.toml` with a comment on the decision.
- Update `docs/Architecture.md` §2: MQTT → HiveMQ MQTT Client, replacing Paho; record why. Leave a note for 083 and 085 to use `MqttConnections`.
- `MqttConnections` holds one supervised `Mqtt5AsyncClient` (or `Mqtt3AsyncClient` when `protocolVersion=3.1.1`) per channel. Each connection has:
  - client id `foresignal-{installIdShort}-{channelIdShort}`
  - automatic reconnect with exponential backoff (1 s → 120 s, with jitter)
  - session expiry 1 h
  - keepalive 60 s
  - TLS for `mqtts://` and `wss://`: system trust store, plus an optional `caCertPem` secret; hostname verification always on
  - username and password from the channel's secrets
- **Last Will:** `{prefix}/status` = `offline`, retained, QoS 1. On every (re)connect, publish `online` retained. On graceful shutdown, publish `offline` before disconnecting.
- **Destination policy:** run the broker host through 035's outbound URL policy. Channels are operator-configured, so private hosts are allowed. A public host without TLS or without credentials → validation warning `MQTT_INSECURE_PUBLIC_BROKER`, returned in the channel response; it is not a refusal.
- Expose a `/v1/system` `mqtt` block (connections, connected, lastError) and a metric `foresignal.mqtt.connected{channel}`.

### 2. `mqtt` channel type (018 SPI)

- Config:
  - `brokerUrl` (`mqtt://`, `mqtts://`, `ws://`, `wss://`)
  - `topicPrefix` (default `foresignal`, validated `^[A-Za-z0-9_\-/]{1,64}$`, no wildcards)
  - `qos` (0|1, default 1), `protocolVersion` (`5`|`3.1.1`, default 5)
  - `messageExpiry` (default 24h, used for non-retained event messages)
  - `payloadTemplate` (optional JSON with placeholders, like webhook `bodyTemplate`)
  - `publishHealth`, `publishSummary` (default true), `summaryInterval` (default 60 s)
  - `haDiscovery` (default false), `haPrefix` (default `homeassistant`), `haMaxRules` (50), `haMaxSources` (100), `haRetainDiscovery` (default false)
  - Secrets: `username`, `password`, `caCertPem`.
- **Event deliveries**, through the durable 018 pipeline (rows, retry ladder, rate limit about 50 msg/s per channel). A delivery succeeds on PUBACK (QoS 1) or on a completed write (QoS 0). The topics:

  | Event | Topic | Retained | Payload |
  |---|---|---|---|
  | alert fired | `{prefix}/alerts/{severity}` | no, with message expiry | `{event_type: severity, id, ruleId, ruleName, severity, state, title, shortTitle, body, url, sourceId, externalId, kind, firedAt, point: [lon,lat], tags, near?}` |
  | alert resolved or acknowledged | `{prefix}/alerts/updates` | no | `{id, state, resolvedAt, acknowledgedBy}` |
  | briefing run | `{prefix}/briefings/{briefingId}` | yes | `{runId, title, at, url, status, markdown (≤ 16 KB, truncated with link)}` |
  | system event | `{prefix}/system/events` | no | NotificationMessage JSON |

- Private-source alerts follow 039: rule name, severity and link only, unless the channel has `privatePayload`.
- **State publisher** (`MqttStatePublisher`, not deliveries). Every retained state is re-published on each reconnect.

  | Topic | Retained | Content | When |
  |---|---|---|---|
  | `{prefix}/rules/{ruleId}/state` | yes | `{state: firing\|resolved\|idle, alertId, severity, lastFiredAt}` | on fire and resolve; an empty retained payload clears it on rule delete |
  | `{prefix}/sources/{sourceId}/health` | yes | `{status, reason, lastSuccessAt, lastDataAt}` | on `SourceHealthChangedEvent`, plus a full refresh every 10 min; private sources are skipped |
  | `{prefix}/summary` | yes | 038 `GlanceService` full view (the channel is admin-configured) | every `summaryInterval`, only when the ETag changed |

- **Topic and payload rules:**
  - Topic segments built from ids or names replace `+`, `#`, `/`, NUL and control characters with `_`.
  - Payload ≤ 256 KB (truncate the markdown first).
  - UTF-8 JSON with the content-type property `application/json` on MQTT 5.
- Channel `test` publishes a `test` message to `{prefix}/test` and reports PUBACK latency.
- `POST /v1/channels/{id}/mqtt/republish` (admin) forces state and discovery re-publication.

### 3. Home Assistant discovery (when `haDiscovery=true`)

- One device config on `{haPrefix}/device/foresignal_{installIdShort}/config`:
  - `dev: {ids: ["foresignal_{installId}"], name: "Foresignal", mf: "Foresignal", mdl: "Foresignal broker", sw: <version>, cu: <publicBaseUrl>}`
  - `o: {name: "Foresignal", sw: <version>, url: <project URL>}`
  - `availability: [{topic: "{prefix}/status"}]`
  - `cmps`, all with `unique_id: "foresignal_{installId}_{key}"`:
    - sensors `alerts_open_critical|warning|info` (state topic `{prefix}/summary`, `value_template` into the glance JSON)
    - `sources_failing`, `sources_degraded`
    - `last_briefing` (`device_class: timestamp`)
    - three `event` entities, `alerts_info`, `alerts_warning` and `alerts_critical` (`p: event`, `state_topic: {prefix}/alerts/{severity}`, `event_types: [<severity>]`; the payload's `event_type` field satisfies HA). State topics cannot contain wildcards, which is why there is one entity per severity.
    - per rule (up to `haMaxRules`, enabled rules ordered by severity then name): `binary_sensor` `rule_{id}` with `device_class: problem`, `state_topic` the rule state, `value_template` `{{ 'ON' if value_json.state == 'firing' else 'OFF' }}`, `json_attributes_topic`
    - per source (up to `haMaxSources`, non-private): `binary_sensor` `source_{id}` with `device_class: problem` from the health topic
- Publish discovery on connect, when `online` arrives on `{haPrefix}/status` (subscribe; random delay of 0–5 s), and on rule or source changes (debounced 5 s). Retained only when `haRetainDiscovery`.
- Removal:
  - a component dropped from the set → publish the device config with that component reduced to `{p}`, then without it
  - `haDiscovery` off or channel deleted → an empty payload (retained) on the device topic
- Document that `device_tracker` for watched entities is deferred: HA only uses GPS trackers inside zones, so it adds little for aircraft and vessels.

### 4. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - the `mqtt` entry in the channel type catalogue (config and secret field specs)
  - `ChannelInput` docs
  - new operation `republishMqttChannel` (`POST /v1/channels/{channelId}/mqtt/republish`, admin, `x-foresignal-scope`)
  - `SystemInfo.mqtt`
  - warning code `MQTT_INSECURE_PUBLIC_BROKER`
- Add an `mqtt` channel `BODIES` example, then regenerate Postman. `AccessPolicy`: `/v1/channels/**` is already admin.
- Product Charter FR-35: add `mqtt`. Architecture §17: add the row and the topic tree.
- `docs/Operator-Guide.md` "MQTT and Home Assistant":
  - Mosquitto example with ACLs (Foresignal writes `foresignal/#` and `homeassistant/#` and reads `homeassistant/status`)
  - the topic table
  - an HA automation on the `alerts_critical` event entity
  - the Node-RED subscription example
  - a note on displays such as AWTRIX NG that subscribe to MQTT
- `THIRD_PARTY_NOTICES.md`: HiveMQ MQTT Client and its transitive libraries (Netty, RxJava, JCTools), all Apache-2.0; check each.

## Tests to write (do not run)

- `MqttChannelTypeTest`: config validation, topic sanitisation, payload golden fixtures per event, truncation at 256 KB, private payload reduction.
- `HaDiscoveryPayloadTest`: golden device config (`dev`, `o`, `cmps`, `unique_id`s, availability), rule and source caps, removal payloads.
- `MqttChannelIT` (Testcontainers `eclipse-mosquitto:2` with an anonymous-plus-password config):
  - an alert delivered on the severity topic with message expiry
  - retained rule state set and then cleared on delete
  - health and summary retained topics
  - LWT `offline` published when the container's network drops the client (use `toxiproxy` or pause the container) and `online` after reconnect
  - QoS 1 retries through the delivery ladder while the broker is stopped
  - TLS with a test CA
- `HaDiscoveryIT`: discovery published on connect and again after `online` on `homeassistant/status`; debounce; removal on disable.
- `MqttConnectionsTest`: backoff schedule, client id format, 3.1.1 fallback.
- `ChannelTypesCatalogIT`: `mqtt` listed with its field specs.

## Acceptance criteria

- With a Mosquitto broker and HA's MQTT integration, Foresignal appears as a device with alert sensors, per-severity alert event entities and rule problem sensors, without hand-written YAML.
- Killing Foresignal flips `{prefix}/status` to `offline`.
- Alert messages are delivered durably (QoS 1, retried).
- The library choice and Netty resolution are recorded in the CR and in Architecture §2.
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
2. **Commit** all changes with the message `060: MQTT Channel And HA Discovery`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/060-CR-MQTT-Channel-And-HA-Discovery.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `060: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
