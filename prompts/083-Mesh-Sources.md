# 083 — Mesh Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/083-CR-Mesh-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/082-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `meshtastic-mqtt` (the Mesh.md rules) and `meshtastic-map`
- `docs/Architecture.md` §5.2 "Streaming budgets" (`maxSubscriptions`, the Mesh.md lesson); `docs/Product-Charter.md` §1 problem 3 (ShadowBroker and Meshtastic)
- `docs/Core-Data-Model.md` §2, §3 (adapter rules 7–13), §5.3, §7, §14 (observers, coverage)
- `docs/Gap-Analysis.md` §4a rows "Own Meshtastic node" and "MeshCore companion", §8.C item 42 ("Extend 047" = this prompt)
- Completion Reports of 039 (private sources), 060 (MQTT client library) and 069 (socket transport, `LAN_URL`)
- Upstream: https://meshtastic.org/docs/software/integrations/mqtt/, https://meshtastic.org/docs/configuration/module/mqtt/, MeshCore `docs/companion_protocol.md` and `docs/payloads.md` (MIT; local reference copy at `~/Documents/GitLab/MeshCore/docs/`), https://github.com/meshcore-dev/meshcore_py (MIT)

## Objective

Bring mesh networks in without repeating ShadowBroker's mistake (it was reported to Meshtastic for 1–2 orders of magnitude excess broker traffic). Four sources: the public Meshtastic broker (strictly limited), the community Meshtastic map (daily), the operator's own Meshtastic node through a private broker, and the operator's own MeshCore companion radio. Meshtastic protobufs are GPL-3.0, so nothing in this tree decodes them: every Meshtastic path uses the firmware's JSON output.

## Tasks

### 1. Shared mesh model (`ai.foresignal.source.adapters.mesh`)

- Kind `mesh-node`. externalId: Meshtastic `!` + 8-hex node number (unsigned); MeshCore `mc:` + first 12 hex chars of the public key.
- Payload (allowlisted, no nulls): `network` (`meshtastic`/`meshcore`), `longName, shortName, role, hwModel, firmware, nodeType` (MeshCore chat/repeater/room/sensor), `positionPrecisionBits`, `batteryLevel, voltage, channelUtil, airUtilTx, uptimeS` and `lastHeardAt` (all volatile, so telemetry jitter only touches the live row; `lowBattery` = `batteryLevel < 15` is a computed significant field per adapter rule 12).
- Geometry: the node's last known position as a Point (kept in the merge state, so a telemetry-only message still carries it); null when the node never shared one.
- Observer: the hearing gateway or own node (`receiverId`), `rssi`, `snr`. Coverage: distinct gateways seen in the flush window → `Coverage.receivers`.
- Per-node merge (adapter rule 7): keep an in-memory `MeshNodeState` map (bounded 20k, LRU) merging nodeinfo, position and telemetry into one complete snapshot before `sink.accept`.
- Descriptor defaults for all four: STANDARD, ALL, `recordTtl` 6 h, `ttlAnchor` OBSERVED, `observedAtMode` UPSTREAM (JSON `timestamp`, else framework clamp), significant fields `longName, shortName, role, hwModel, nodeType, network, lowBattery, advertStatus`, tileProperties `shortName, role, nodeType, batteryLevel`, searchFields `longName, shortName`, no `embedFields`.
- **`MeshMessageBus`** (in-process, not persisted): every decoded text message from `meshtastic-local` and `meshcore-local` is published as `MeshTextMessage(sourceId, network, channelIndex, channelName, fromId, toId, text, receivedAt, rssi, snr)`. Prompt 084 consumes it for field reports. Text is persisted only when a source's `includeText=true` (kind `mesh-message`, never embedded, never sent to an LLM).
- **MQTT client:** use 060's HiveMQ MQTT Client through its `MqttConnections` supervisor (060 leaves a note for 083 and 085). If 060's CR shows something else, follow it. Each CONNECT and each SUBSCRIBE consumes one budget unit; add `StreamContext.consumeBudget(String reason)` if 063/069 have not added an equivalent.
- **LAN brokers:** extend 069's `LAN_URL` validation to the schemes `mqtt://` and `mqtts://` (explicit port, pinned IP, private address only) for `meshtastic-local` (and 086's Remote ID source).
- **`MeshtasticJsonParser`** (pure): `msh/<root>/2/json/<channel>/<gatewayId>` messages with fields `id, channel, from, to, type, sender, timestamp, payload, rssi, snr, hops_away`. Types `nodeinfo` (`longname, shortname, hardware, role`), `position` (`latitude_i`/`longitude_i` × 1e-7, `altitude`, `precision_bits`), `telemetry` (device metrics), `text`. Values ≥ 2^31 are unsigned (firmware ≥ 2.2.0). Unknown types are counted and dropped. Topics under `/2/e/` or `/2/map/` (protobuf) are never subscribed and, if received, dropped without parsing.

### 2. `meshtastic-mqtt` (STREAMING, public broker): the Mesh.md rules, enforced in code

- Broker `mqtt.meshtastic.org:1883` (descriptor constant; `allowedHosts`), public credentials `meshdev` / `large4cats` as descriptor constants, not operator credentials.
- **Disabled by default**; enabling logs a WARN summarising the rules.
- Params: `root` (string, default `msh/US`, pattern `^msh/[A-Za-z0-9_]+(/[A-Za-z0-9_]+)?$`), `extraRoots` (string-list, default empty), `maxSubscriptions` (integer, default 1, **hard maximum 3**; root + extraRoots must fit, else 400 `VALIDATION_FAILED` at PUT).
- Subscribe to `<root>/2/json/#` only, one subscription per root, QoS 0. **No wildcard above the root** (`msh/#` and `+/…` rejected).
- Client id `foresignal-<installId>` (stable, identifiable); keepalive 120 s; clean session; reconnect 15 s → 300 s exponential with jitter; budget 12 connects/hour, 100/day.
- **Inbound cap 100 messages/min** (sliding window, before parsing); excess dropped and counted `foresignal.mesh.dropped{source,reason=rate}`, with a WARN at most once per 10 min.
- **Verified 2026-10:** the docs still describe JSON output (not on nRF52), but most public-broker traffic is protobuf on `/2/e/`; expect sparse JSON. Set `zeroResultsNormal=true` and state this in the Source-Catalog row. Public positions are already reduced to 10–16 bits of precision; keep `positionPrecisionBits` and set `Quality.accuracyMeters` from it.
- `includeText` param (default false) for public text; public channel text is never posted anywhere else.

### 3. `meshtastic-map` (POLLING, daily)

- `https://meshtastic.liamcottle.net/api/v1/nodes`. **Verified 2026-10:** about 29 MB JSON `{"nodes":[...]}` with fields `node_id` (decimal), `long_name, short_name, hardware_model, role, firmware_version, region, modem_preset, position_precision, latitude, longitude, altitude, position_updated_at, battery_level, updated_at`. Confirm the coordinate scaling in the fixture.
- Stream-parse with Jackson `JsonParser` (never build the whole tree); `maxResponseBytes` 64 MB; interval 24 h, **minInterval 24 h**, budget 1/min, 2/day; conditional GET. `observedAt` = `updated_at` (fall back to `position_updated_at`; a record with neither is skipped).
- Community hobby service: attribution "Meshtastic Map by Liam Cottle"; licence unknown, so `licence` = "unspecified (community service)" and note it in the catalogue row.

### 4. `meshtastic-local` (STREAMING, own node via a private broker)

- The operator runs a broker (for example Mosquitto) and points their node's MQTT module at it with **JSON enabled** and **encryption disabled on uplink**. Params: `brokerUrl` (`LAN_URL mqtt://` or `mqtts://`, explicit port), `root` (default `msh/local`), `channels` (string-list, default all: `<root>/2/json/#`), `includeText` (default false). Credentials `username`, `password` optional.
- Same parser and merge as §2, no 100/min cap (own broker) but a 1,000/min safety cap. Descriptor `private=true` (039 enforcement), because it reveals neighbours' positions.
- Publish every `text` message to `MeshMessageBus`.
- Document that nRF52 nodes cannot emit JSON, and that the GPL protobuf path must stay outside this repository (an operator may run an out-of-tree bridge that republishes JSON to their broker).

### 5. `meshcore-local` (STREAMING, own MeshCore companion)

Two transports, one record mapper (`MeshCoreEventMapper`):

- **Bridge (first, recommended for USB/BLE companions):** ship `contrib/meshcore-bridge/bridge.py` (MIT, depends on `meshcore` from PyPI, the `meshcore_py` package) that connects via serial, BLE or TCP and POSTs JSON events (`contact`, `advert`, `channel_msg`, `contact_msg`, `self_info`) to a PUSH source `meshcore-bridge` through 024's ingest endpoint with an IngestFormat `meshcore-json`. Include a README and a systemd unit.
- **Native TCP (WiFi companion firmware):** param `endpoint` (`LAN_URL tcp://`, required, **no default port**; the firmware example defaults to 5000 only when built with `TCP_PORT` unset). Framing: radio→app `'>'` + uint16 LE length + frame; app→radio `'<'` + length + frame; frames > 176 bytes skipped. Startup sequence: `CMD_APP_START` (0x01, app name "Foresignal"), `CMD_DEVICE_QUERY` (0x16 0x03), `CMD_GET_CONTACTS` (0x04), then `CMD_SYNC_NEXT_MESSAGE` (0x0A) on `PUSH_CODE_MSG_WAITING` (0x83). One command in flight, 5 s timeout. Transport: 069's `ctx.sockets().tcp(…, Framing.RAW, …)` with a codec `meshcore-companion` that reassembles `>`-frames. This is the one LAN source that must write after connect: extend 069's transport with `SocketOptions.commandWriter(CommandGuard)`, whose handle exposes `send(byte[])` only through the guard below (default handles stay receive-only, as 082 requires).
- Contact frame (`RESP_CODE_CONTACT` 0x03 / `PUSH_CODE_NEW_ADVERT` 0x8A): pubkey 32, type, flags, out_path_len, out_path 64, name 32 (NUL-terminated), last_advert uint32, lat int32 ×1e-6, lon int32 ×1e-6, lastmod uint32. `PUSH_CODE_ADVERT` (0x80) carries only the pubkey: re-query it with `CMD_GET_CONTACT_BY_KEY` (0x1E). Read channel names once with `CMD_GET_CHANNEL` (0x1F, indices 0–7) so messages carry `channelName`.
- **Allowed command set** (enforced by a `MeshCoreCommandGuard` allowlist): 0x01 (app start), 0x16 (device query), 0x04 (get contacts), 0x0A (sync next message), 0x05 (get time), 0x14 (battery and storage), 0x1E (contact by key), 0x1F (get channel), plus the send commands that 084 registers. Never `CMD_SET_*`, import/export key, reboot or factory reset. Never send login, status, telemetry or path-discovery requests to repeaters (receive-only health).
- **`MeshCoreCompanionLink`** (singleton per host:port): the companion accepts one TCP client, so the source and the 084 channel share this link. It owns reconnect (5 s → 5 min), the command queue and frame dispatch.
- **Repeater health from adverts:** for `nodeType=repeater`, compute `advertStatus` = `ok` / `late` / `silent` from age of `last_advert` against params `repeaterLateAfter` (default 13 h) and `repeaterSilentAfter` (default 26 h); re-evaluate every 15 min and emit only on change. `advertStatus` is significant; `lastAdvertAt` is volatile.
- `observedAtMode` RECEIVED (node clocks drift; advert timestamps go in the payload as `advertTimestamp`, volatile). `private=true`. Channel and contact messages go to `MeshMessageBus`.

### 6. Fixtures

Record: 5 minutes of `msh/<root>/2/json/#` from a private broker fed by a test node (nodeinfo, position, telemetry, text); one public-broker sample (JSON topics only); `meshtastic-map` trimmed to 200 nodes with the original size noted; a MeshCore TCP byte capture (hex) of the startup sequence, a contact list, an advert push and a channel message, plus the equivalent bridge JSON events. Replace real node names and keys with synthetic ones; document each capture in a `README.md`.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: add `meshcore-json` to the IngestFormat enum (if enumerated) and kinds `mesh-node`, `mesh-message` where kinds are listed. Regenerate Postman.
- `docs/Source-Catalog.md`: update both Meshtastic rows, add `meshtastic-local`, `meshcore-local`, `meshcore-bridge`. `docs/Operator-Guide.md` "Mesh networks": the public-broker rules and why they are fixed, private-broker setup, MeshCore bridge vs TCP firmware, the one-client-per-companion limit. `THIRD_PARTY_NOTICES.md`: MeshCore protocol docs (MIT) consulted; meshcore_py named as a bridge dependency (not vendored).

## Tests to write (do not run)

- `MeshtasticJsonParserTest`: fixtures per type, unsigned node ids, unknown type dropped, `/2/e/` topic ignored without parsing.
- `MeshtasticMqttRulesTest`: `maxSubscriptions` > 3 rejected, wildcard roots rejected, client id `foresignal-<installId>`, keepalive 120 s, reconnect bounds 15–300 s, the 100/min limiter drops the 101st message.
- `MeshtasticMqttAdapterTest`: against an embedded MQTT broker (HiveMQ CE or Moquette Testcontainer): exactly the configured subscriptions, merge of nodeinfo + position into one snapshot.
- `MeshtasticMapParserTest`: streaming parse of a trimmed real fixture and a 10k-node synthetic file without building a tree.
- `MeshCoreFrameCodecTest` and `MeshCoreEventMapperTest`: framing, contact decode (lat/lon scaling, NUL-padded names), advert push re-query, oversize frame skipped.
- `MeshCoreCommandGuardTest`: any non-allowlisted command code throws.
- `RepeaterHealthTest`: ok → late → silent transitions emit once each.
- `MeshIngestIT`: bridge PUSH ingest of `meshcore-json`; `meshtastic-local` records hidden from anonymous reads (private).

## Acceptance criteria

- `meshtastic-mqtt` is disabled by default and cannot be configured outside the Mesh.md limits.
- No Meshtastic protobuf classes or `.proto` files exist in the repository.
- The MeshCore link sends only allowlisted commands.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `083: Mesh Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/083-CR-Mesh-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `083: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
