# 084 — Mesh Radio Channels

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/084-CR-Mesh-Radio-Channels.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/083-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §7 row "Mesh radio channels", §4a row "Mesh field reports", §8.C item 43 ("047a" = this prompt)
- `docs/Architecture.md` §17 (notification channels: message model, routing, delivery, security)
- Completion Reports of 017 (alert lifecycle, ack), 018 (channel-type SPI, Truncator, folding, quiet hours), 036 (places), 060 (MQTT client), 083 (`MeshMessageBus`, `MeshCoreCompanionLink`, `MeshCoreCommandGuard`)
- MeshCore `docs/companion_protocol.md` (send channel message 0x03, send channel datagram 0x3E, `MAX_CHANNEL_DATA_LENGTH` 163, the 133-character text limit) and `docs/number_allocations.md`; https://meshtastic.org/docs/software/integrations/mqtt/ (JSON downlink); https://meshtastic.org/docs/overview/radio-settings/ (EU_868 band and duty cycle)
- `docs/OpenAPI.json` schemas `ChannelType`, `Channel`, `ChannelInput`

## Objective

Let Foresignal send alerts over the operator's own LoRa mesh, and accept short field reports back, without ever loading a public channel. Two new channel types, `meshtastic` and `meshcore`, share a compact message profile, an airtime (duty-cycle) governor and a small report grammar that only works over a private, keyed channel. Meshtastic stays on the JSON route (no GPL protobufs); MeshCore uses its MIT companion protocol through the link 083 built.

## Tasks

### 1. Compact message profile (`ai.foresignal.notify.mesh`)

- **`MeshShortTextFormatter`**: `{sev} {title} {where} {hhmm}Z #{code}`. `sev` is ASCII (`!!!` critical, `!!` warning, `i` info; no emoji, which cost 4 bytes each). `where` is `{{near.place}}` (040) or a 2-decimal lat/lon. `code` is a 4-character Crockford base32 short id of the alert, stored on the delivery row so `ACK` replies can find the alert. Truncate with 018's `Truncator` by **characters for MeshCore (cap 133)** and **UTF-8 bytes for Meshtastic (cap 200)**, never splitting a code point; the `#code` suffix is always kept. A resolved alert renders as `OK {title} #{code}`.
- **`MeshCborProfile`** (machine-readable, optional per channel, `profile=cbor`): CBOR map with integer keys `0` version (1), `1` type (1 alert, 2 resolved, 3 report-ack, 4 status), `2` short code, `3` severity (0–2), `4` kind (≤ 16 chars), `5`/`6` lat/lon as int ×1e5, `7` epoch seconds, `8` title (≤ 60 chars), `9` source id (≤ 24 chars). Encoded size ≤ 160 bytes; drop key 8, then 9, until it fits. Use Jackson's CBOR dataformat module (Boot-managed Jackson 3 coordinates). Prompt 113 reuses and extends this codec for MeshDTN messages, so keep it in its own package with golden vectors, and make the decoder ignore unknown integer keys (forward compatibility).
- **`LoRaAirtime`**: Semtech time-on-air: `Tsym = 2^SF/BW`; preamble `(nPreamble + 4.25)·Tsym`; payload symbols `8 + max(ceil((8·PL − 4·SF + 28 + 16·CRC − 20·IH) / (4·(SF − 2·DE)))·(CR + 4), 0)` with `DE=1` when `Tsym > 16 ms`. PL = message bytes + a per-network overhead constant (Meshtastic 32, MeshCore 24; document these as conservative estimates). Presets: `meshtastic-longfast` (SF11, BW 250 kHz, CR 4/5, preamble 16), `meshtastic-mediumfast` (SF9, 250 kHz, 4/5, 16), `custom` (operator SF/BW/CR/preamble). The operator confirms the preset matches the node.

### 2. Airtime governor

- **`AirtimeGovernor`**, one per channel: 1-hour sliding window of estimated airtime.
- Config `region` (`EU_868`, `EU_433`, `UK_868`, `US`, `ANZ`, `OTHER`) gives the regulatory ceiling: 10 % for EU_868/UK_868 (869.40–869.65 MHz, the band Meshtastic EU_868 uses), 10 % for EU_433, no duty cycle for US/ANZ (dwell-time rules are the radio's job). `dutyCyclePercent` (default **1.0**, a polite share of the channel; must be ≤ the ceiling; maximum 10).
- Also enforce `minSpacing` (default 30 s between transmissions) and `maxMessagesPerHour` (default 20).
- A message that does not fit is deferred through 018's `next_attempt_at` and folded into one digest line (`{n} alerts, top: {title} #{code}`) when released. Critical alerts jump the queue but never exceed the regulatory ceiling.
- Counters: `foresignal.channel.mesh.airtime_seconds{channel}`, `deferred`, `folded`, `dropped`. Raise a system event when the deferral backlog is older than 1 h.

### 3. Channel SPI fit

Both types implement 018's `ChannelType` (`spec`, `validate`, `rateLimit`, capabilities without `editInPlace`). Like 060's `mqtt` type they do not send HTTP: use the same non-HTTP delivery path 060 introduced (the `fanOut`-style custom sender) so deliveries still get rows, retries, quiet hours and folding. Add `UNACKED` to `DeliveryStatus`. Render with `MeshShortTextFormatter` (018's markdown renderer is bypassed; titles are plain text).

### 4. `meshtastic` channel type (MQTT JSON downlink)

- Config: `brokerUrl` (`LAN_URL`, `mqtt://`/`mqtts://`), `root` (default `msh/local`), `gatewayNodeNum` (unsigned decimal, the `from` field), `channelIndex` (1–7), `channelName` (string), `toNodeNum` (optional, DM), `topicSuffixNodeId` (boolean, default false), `region`, `airtimePreset`, `dutyCyclePercent`, `profile` (`text`|`cbor`), `minSeverity` (default `warning`), `reportsEnabled`, `allowedSenders`. Secrets: `username`, `password`.
- Publish through 060's `MqttConnections` (one client per broker, client id `foresignal-<installId>-mesh`) at QoS 1 to `{root}/2/json/mqtt/` (append `!{gatewayHex}` when `topicSuffixNodeId`; community reports show some firmware needs it) with `{"from":gatewayNodeNum,"type":"sendtext","payload":text,"channel":channelIndex[,"to":toNodeNum]}`. CBOR profile is sent as text `~E1 ` + base64url (JSON downlink only carries text).
- Document the node prerequisites: JSON enabled (not available on nRF52), a channel named `mqtt` with downlink enabled, and MQTT encryption disabled toward the private broker.
- **Never public:** reject at create/update with 400 `MESH_PUBLIC_CHANNEL` when the broker host is `*.meshtastic.org` or not a LAN address, when `channelIndex` is 0, when `channelName` is empty or a modem-preset default (`LongFast, LongSlow, LongModerate, MediumFast, MediumSlow, ShortFast, ShortSlow, ShortTurbo, VLongSlow`), or when the operator sets `licensedMode=true` (ham mode disables encryption). Require `privateChannelConfirmed=true`.
- No delivery acknowledgement is visible over JSON; the delivery is `sent` once the broker PUBACKs.

### 5. `meshcore` channel type (companion protocol)

- Config: `companionUrl` (`tcp://` validated like 069's `LAN_URL`: private address, explicit port, pinned IP), `channelIndex` (1–7), `dmContactPrefix` (optional 12-hex public-key prefix), `requireAck` (boolean, default false; DM only), plus the shared `region`/`airtimePreset`/`dutyCyclePercent`/`profile`/`minSeverity`/`reportsEnabled`/`allowedSenders`. MeshCore radio settings vary by region, so `airtimePreset=custom` is the default here.
- Reuse 083's `MeshCoreCompanionLink` for the same host:port (the companion accepts one TCP client). Register the send commands with `MeshCoreCommandGuard`: 0x03 (channel text: `03 00 {idx} {ts u32 LE} {utf8}`), 0x3E (channel datagram: `3E {idx} FF {data_type u16 LE} {payload}`), 0x02 (direct text: `02 00 {attempt} {ts u32 LE} {pubkey prefix 6 bytes} {utf8}`).
- **Never public:** on create/update and on every link (re)connect, read the slot with `CMD_GET_CHANNEL` (0x1F) and refuse with `MESH_PUBLIC_CHANNEL` when the index is 0, the name is `Public` or starts with `#` (hashtag channels derive their key from the name), or the 16-byte secret is all zeros.
- CBOR profile goes out as a channel datagram (≤ 163 bytes) with `dataType` config (default `0xFF45`, in the 0xFF00–0xFFFE development range; note in the Operator Guide that a production deployment should register a value via MeshCore `number_allocations.md`).
- **DM ack:** `RESP_CODE_SENT` (0x06) returns `expected_ack` (u32) and `est_timeout` (ms); wait for `PUSH_CODE_SEND_CONFIRMED` (0x82) with the same code. On timeout retry once with `attempt=1`; then mark the delivery `unacked` (a new terminal delivery status, not `failed`, so it does not raise a failure event). Channel messages have no ack.

### 6. Field-report grammar (inbound)

- Subscribe to 083's `MeshMessageBus`. A message is considered only when it arrives on a channel that has a `meshtastic`/`meshcore` channel configured with `reportsEnabled=true` for the **same** private channel index, its sender is in `allowedSenders` (map of node id or MeshCore key prefix → `report`|`ack`), and its text starts with `#E `.
- Grammar (case-insensitive ASCII, one command per message):
  - `#E R <TYPE> [<lat>,<lon> | @<place>] <text>`: TYPE ∈ `FIRE SMOKE FLOOD POWER ROAD MED WX SEC OTHER`; `@place` resolves through 036 places.
  - `#E ACK <code>`: acknowledges the alert behind that delivery code (017), `acknowledged_by = mesh:<senderId>`, requires role `ack`, and writes an audit event (023).
  - `#E STATUS` replies with a ≤ 133-char summary from `/v1/glance` (038) if present, else open-alert counts.
  - `#E HELP`.
- Reports become records in a built-in PUSH source `mesh-reports` (kind `field-report`, `private=true`, `observedAtMode` RECEIVED, payload `type, text` (≤ 200 chars), `reporter`, `network`, `channel`; Observer = reporting node with rssi/snr). Report text is untrusted: never embedded; Ask and briefings quote it only as data.
- Replies (`OK R{n} {TYPE}`, `OK ACK #{code}`, `ERR {reason}`) go out on the same channel and are subject to the airtime governor. Rate limit 10 inbound commands/min per sender; extras are ignored and counted.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: add `meshtastic` and `meshcore` to the channel-type enums in `ChannelType`, `Channel` and `ChannelInput`; add `unacked` to the delivery status enum; add `GET /v1/channels/{channelId}/airtime` (`x-foresignal-scope: admin`, response `ChannelAirtime {windowSeconds, usedSeconds, limitSeconds, dutyCyclePercent, queued, foldedLastHour, droppedLastHour}`) and error code `MESH_PUBLIC_CHANNEL`. AccessPolicy: the new path is admin. Regenerate Postman (add example create bodies for both types in the script's BODIES map).
- `GET /v1/channels/types` lists both types with their config fields.
- `docs/Operator-Guide.md` "Mesh alert channels": node prerequisites, why public channels are refused, duty-cycle maths with a worked LongFast example, report grammar card, and a note that unattended transmission on amateur allocations is subject to local rules (for example FCC Part 97), so these channels are for licence-free ISM operation.
- Source-Catalog: add `mesh-reports`.

## Tests to write (do not run)

- `MeshShortTextFormatterTest`: 133-char and 200-byte caps with multi-byte input, code suffix always kept, resolved form.
- `MeshCborProfileTest`: golden vectors, size reduction order, ≤ 160 bytes.
- `LoRaAirtimeTest`: known values for LongFast and a SF7/125 kHz case (compare against the Semtech calculator; record the reference numbers in the test).
- `AirtimeGovernorTest`: window accounting, ceiling per region, `dutyCyclePercent` above ceiling rejected, deferral then fold, critical priority capped by the ceiling.
- `MeshtasticChannelTest`: embedded MQTT broker receives the exact JSON envelope and topic; every `MESH_PUBLIC_CHANNEL` rule.
- `MeshCoreChannelTest`: fake companion over TCP: frame bytes for 0x03/0x3E/0x02, public-slot refusal after `CMD_GET_CHANNEL`, DM ack success, timeout → retry → `unacked`.
- `MeshReportGrammarTest`: each command, unknown sender ignored, wrong channel ignored, missing prefix ignored, malformed coordinates → `ERR`.
- `MeshReportIngestIT`: a bus message creates a private `field-report` record; `ACK` acknowledges the alert and writes an audit row.

## Acceptance criteria

- Neither channel type can be configured to transmit on a public or default channel.
- Every transmission passes the airtime governor; no path bypasses it.
- `/v1/channels/types` and the spec list both types; Postman regenerated.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `084: Mesh Radio Channels`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/084-CR-Mesh-Radio-Channels.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `084: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
