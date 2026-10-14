# 084-CR-Mesh-Radio-Channels

| | |
|---|---|
| Prompt | 084-Mesh-Radio-Channels.md |
| Status | Complete |
| Implementation commit | 271690570866f7cd6ceaea89972552f87ad10b38 |
| Date | 2026-10-13 |

## Summary

Two new channel types, `meshtastic` and `meshcore`, send short alert lines over the operator's own LoRa mesh. They
share a compact message profile, an airtime governor and a field-report grammar. Neither can be configured for a
public or default channel. Meshtastic uses only the node's JSON downlink, with no protobufs. MeshCore uses the MIT
companion protocol over 083's shared link.

**1. Compact message profile** (`ai.foresignal.notify.mesh`):
- **`MeshShortTextFormatter`:**
  - the line is `{sev} {title} {where} {hhmm}Z #{code}`;
  - `sev` is `!!!`, `!!` or `i`; `title` is the short title (the rule name);
  - `where` is the region snapshot's `near.name`, else the record position as a 2-decimal `lat,lon`;
  - a resolved alert is `OK {ruleName} #{code}`; a digest is `{n} alerts, top: {title} #{code}`;
  - text is cut with 018's `Truncator` (grapheme-safe) to a character cap, then a byte cap, and the `#code`
    suffix is always kept whole;
  - caps: MeshCore channel 133 characters and 127 bytes, MeshCore direct message 133 characters and 160 bytes,
    Meshtastic 200 bytes.
- **`MeshShortCode`:** a 4-character Crockford base32 code from the low 20 bits of the alert id. It is stored on the
  delivery row (new column `notification_delivery.short_code`) after a successful send.
- **`notify.mesh.cbor.MeshCborProfile` / `MeshCborMessage`:**
  - a definite-length CBOR map with integer keys 0–9, keys in ascending order, written and read with Jackson 3
    CBOR (already a dependency);
  - at most 160 bytes: key 8 is dropped first, then key 9;
  - the decoder skips unknown integer keys;
  - the golden vectors were checked by hand against RFC 8949.
- **`LoRaAirtime`:**
  - Semtech time on air (low-data-rate optimization on above 16 ms per symbol);
  - per-packet overhead 32 bytes (Meshtastic) or 24 bytes (MeshCore), documented as conservative;
  - presets `meshtastic-longfast`, `meshtastic-mediumfast` and `custom` (`airtimeCustom`).

**2. Airtime governor:**
- **`AirtimeGovernor`:** a one-hour sliding window per channel.
  - Ceilings: 10 % for EU_868, UK_868 and EU_433 (`MeshRegion`). US, ANZ and OTHER have no ceiling, so the 10 %
    maximum applies there.
  - `dutyCyclePercent` defaults to 1.0 and must not exceed the ceiling (or 10).
  - `minSpacing` defaults to 30 s and `maxMessagesPerHour` to 20.
  - Critical alerts skip the operator's limits but never exceed the ceiling.
  - A message that could never fit is dropped.
  - A new config keeps the current window (`withLimits`).
- **`AirtimeGovernors`:** the per-channel registry, plus the counters `foresignal.channel.mesh.airtime_seconds`,
  `.deferred`, `.folded` and `.dropped`, each tagged with `channel`.
- **`MeshAirtimeService`:** serves `GET /v1/channels/{channelId}/airtime` (`ChannelAirtime`; `queued` is read from
  the database). It also:
  - raises the system event `system.mesh_airtime_backlog` (warning) when a channel's airtime backlog is older than
    1 h, once per episode, checked every minute;
  - detaches a channel from its MeshCore link when the channel changes.

**3. Channel SPI fit (018 and 060's non-HTTP path):**
- **Request body:** the new `PreparedRequest.Mesh` goes from `ChannelSender` to the new `MeshDeliveryGateway`,
  implemented by `MeshTransmitter`. Every transmission (delivery, channel test and report reply) passes the
  channel's governor there first.
- **New exception:** `SendDeferredException` (an `IOException`). `DeliveryWorker` turns it into
  `deliveries.defer(until, "airtime")` without counting an attempt.
- **Deferral reason:** new `DeferredReason.AIRTIME`, included in `RELEASED_AS_DIGEST`. Deliveries released together
  fold into one digest, which the mesh types render as one line.
- **New `ChannelType` defaults:**
  - `validationCode` (gives `MESH_PUBLIC_CHANNEL`);
  - `acceptsUpdate` (mesh types take only `alert.resolved`, enforced in `NotificationService.publishUpdate`);
  - `unacknowledged` (the worker settles the delivery `unacked` through `DeliveryStore.unacked`).
- **New statuses and field types:** `DeliveryStatus.UNACKED` (also settled by retention) and the field type
  `number`.
- **Channel `minSeverity`:** a mesh channel's `config.minSeverity` (default `warning`) also narrows
  `NotificationChannel.accepts`.
- **`DigestBuilder` data:** gains `topTitle` and `topAlertId`.
- **`ChannelInputParser`:**
  - `LATER_TYPES` is now empty;
  - mesh `allowedSenders` is an object, not 061's string array.

**4. `meshtastic`** (`notify.types.MeshtasticChannel`, `notify.mesh.MeshtasticDownlinks`):
- **Envelope:** `{"from":gatewayNodeNum,"type":"sendtext","payload":text,"channel":channelIndex[,"to":toNodeNum]}`
  is published at QoS 1 to `{root}/2/json/mqtt/` (with `!{gatewayHex}` appended when `topicSuffixNodeId`).
- **Connection:** one 060 `MqttConnections` client per broker and user, client id `foresignal-<installId>-mesh`,
  MQTT 3.1.1. It has no Will and no status topic. It is re-pinned to a LAN address on every connect and reopened
  after a drop.
- **CBOR text:** `~E1 ` plus base64url, for a CBOR message of at most 147 bytes.
- **Delivered:** on PUBACK.
- **Public refusals:** a broker under `meshtastic.org` or not on the LAN, index 0, an empty or preset channel name,
  `licensedMode`, or `privateChannelConfirmed` missing.

**5. `meshcore`** (`notify.types.MeshCoreChannel`, `notify.mesh.MeshCoreSender`):
- **Commands:** frames 0x03, 0x3E (`dataType` default 0xFF45) and 0x02, sent over 083's link, which is shared per
  `host:port`. The guard registers the three send commands.
- **Slot check:** done at create and update (`MeshCoreSlotProbe`) and on every connect (from the link's startup
  slot reads, or by asking directly). It refuses slot 0, an empty slot, `Public`, `#…` and an all-zero secret.
  Nothing is sent until the slot is verified private.
- **Direct-message acknowledgement:** `RESP_CODE_SENT` gives the expected ACK and the timeout (the wait is 2–60 s).
  The sender waits for 0x82, retries once with `attempt=1` (the governor admits the retry again), and otherwise
  answers `UNACKED`.

**6. Field reports** (`notify.mesh.report`):
- **`MeshReportGrammar`:** `#E R <TYPE> [lat,lon | @place] <text>`, `#E ACK <code>`, `#E STATUS`, `#E HELP`.
- **`MeshReportService`:** subscribes to `MeshMessageBus` at startup (`StartupOrder.MESH = 39`) and handles each
  message on a virtual thread.
  - **Filters:** only channel messages on an enabled mesh channel's own index with `reportsEnabled`, from senders in
    `allowedSenders`, carrying the `#E ` prefix, at most 10 per minute per sender. Extras are counted as dropped.
  - **Reports:** become `field-report` records in the new private PUSH source `mesh-reports`
    (`source.adapters.mesh.MeshReportsAdapter`), submitted to the ingest queue.
  - **ACK:** acknowledges as `mesh:<sender>` and audits with actor kind `mesh`.
  - **STATUS:** comes from `GlanceService`, else from the open-alert counts.
  - **Replies:** go through the governor; a reply the governor defers is dropped.

**7. Spec, Postman and docs:**
- **`docs/OpenAPI.json`:**
  - `getChannelAirtime` is implemented (and removed from `pending-operations.txt`);
  - new enum values: `airtime` in `deferredReason`, `unacked` in the deliveries status filter (084 added to
    `x-foresignal-extended-by`), `number` in the ChannelType field types, `mesh` in `AuditEvent.actorKind`;
  - `allowedSenders` is `oneOf` array or object;
  - `MESH_PUBLIC_CHANNEL` is listed under Problem, the `field-report` note is updated, and a changelog entry is added.
- **Postman:** the two example create bodies were added and the collection regenerated.
- **`docs/Operator-Guide.md`:** the new section "Mesh alert channels" (node prerequisites, why public channels are
  refused, the duty-cycle maths with a worked LongFast example, the report grammar card, and the FCC Part 97 note).
- **`docs/Source-Catalog.md`:** the `mesh-reports` row.
- **Also updated:** `docs/Invariants.md` (EGR-1 and SRC-7 test entries) and `THIRD_PARTY_NOTICES.md` (the MeshCore
  sources 084 consulted).

## Files created / modified

- **Created, main:**
  - `notify/{MeshDeliveryGateway, SendDeferredException}`;
  - `notify/mesh/{package-info, MeshNetworks, MeshRegion, LoRaAirtime, AirtimeGovernor, AirtimeGovernors,
    MeshShortCode, MeshShortTextFormatter, MeshRenderings, MeshChannels, MeshChannelType, MeshLanUrls,
    MeshCoreSlotProbe, MeshCoreSender, MeshChannelStreamContext, MeshtasticDownlinks, MeshTransmitter,
    MeshAirtimeService}`;
  - `notify/mesh/cbor/{package-info, MeshCborMessage, MeshCborProfile}`;
  - `notify/mesh/report/{package-info, MeshReportGrammar, MeshReportService}`;
  - `notify/types/{MeshtasticChannel, MeshCoreChannel}`; `source/adapters/mesh/MeshReportsAdapter`.
- **Modified, main:**
  - `notify/{ChannelType, ChannelFieldSpec, ChannelInputParser, ChannelSender, DeliveryWorker, DeliveryStore,
    DeliveryStatusConstraint, DigestBuilder, NotificationChannel, NotificationService, PreparedRequest}`;
  - `store/{DeliveryStatus, DeferredReason, NotificationDeliveryEntity, NotificationDeliveryRepository}`;
  - `api/{ChannelsController, error/ErrorCodes}`; `config/StartupOrder`;
  - `source/adapters/mesh/MeshCoreCompanionLink`; `source/formats/meshcore/MeshCoreFrames`.
- **Created, test:**
  - `notify/mesh/{MeshShortTextFormatterTest, LoRaAirtimeTest, AirtimeGovernorTest, MeshChannelsTest}`;
  - `notify/mesh/cbor/MeshCborProfileTest`;
  - `notify/mesh/report/{MeshReportGrammarTest, MeshReportIngestIT}`;
  - `notify/types/{MeshtasticChannelTest, MeshCoreChannelTest}`.
- **Modified, test:** `api/{ChannelTypesCatalogIT, ChannelsControllerIT}`, `notify/ChannelInputParserTest`,
  `retention/FamilyDefaultsTest`, `openapi/pending-operations.txt`.
- **Docs and other:** `docs/{OpenAPI.json, Operator-Guide.md, Source-Catalog.md, Invariants.md}`,
  `THIRD_PARTY_NOTICES.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.

## Deviations from the prompt

1. **The MeshCore text cap is 133 characters, not 134.** MeshCore's `docs/companion_protocol.md` says "Messages are
   limited to 133 characters", and the prompt's own test list and STATUS reply both say 133.
   - The firmware's real limit is `MAX_TEXT_LEN` = 160 bytes, including the `name: ` prefix it adds to channel
     text, and it truncates silently at the end, which is where the `#code` is.
   - So channel text is also capped at 127 bytes (160 less the longest name prefix), and direct messages at 160
     bytes.
2. **`{{near.place}}` does not exist.** 040's placeholder is `near.name`, so `where` uses the region snapshot's
   `near.name`.
3. **The firmware answers channel text with `RESP_CODE_OK`.** `MyMesh.cpp` (`writeOKFrame`) answers
   `CMD_SEND_CHANNEL_TXT_MSG` with OK, although the protocol doc says `PACKET_MSG_SENT`. 083's link waited for
   `RESP_SENT` only, which would have timed out every channel send. The 0x03 terminal codes are now OK or SENT.
4. **`PUSH_CODE_SEND_CONFIRMED` is read as the firmware writes it:** a 4-byte ACK code plus a 4-byte trip time. The
   doc says a 6-byte ACK code.
5. **083's link had a race, now fixed.** A quick detach followed by an attach (the save-time slot probe does this)
   could leave two reconnect loops running, and the old loop could close the new connection. Now:
   - the loop runs only while it is the current runner;
   - the runner is assigned before it starts;
   - each loop closes only its own handle.
6. **MeshCore channel senders are matched by `name:<advertised name>`.** MeshCore channel messages carry no sender
   key (083: `fromId` is null; the text is `name: …`), so `mc:` key-prefix entries can never match a channel
   report. The spec's `allowedSenders` description says so. Direct messages to the companion are not read as
   reports, because the prompt allows only the same private channel index.
7. **An unreachable companion does not block a save.** If the companion cannot be reached at create or update
   (5 s), the channel is saved. Nothing is sent until a connect has verified the slot.
8. **Meshtastic connection lifecycle.** The downlink connection has no status topic or Will (nothing but downlinks is
   ever published on the node's broker). It does not reconnect on its own: a dropped connection is closed and
   reopened by the next send, paced by the delivery retry ladder.
9. **The governor window is in memory.** A restart starts empty; the 30 s spacing and the hourly cap still bound
   the rate.
10. **Acknowledgements and snoozes are never sent to mesh channels** (`acceptsUpdate`), to save airtime. Only
    `alert.resolved` goes out, as `OK …`.
11. **The Observer of a `field-report` is the reporting node**, as the prompt says. 083 used the receiving node for
    its own kinds.
12. **The report reference is `R` + 4 Crockford characters** of the record hash (for example `OK R7K2Q FIRE`), not
    a running number, which would need persistent state.
13. **`airtimeCustom` is not a catalog field**, because the field types cannot express an object of numbers. It is
    documented in the `airtimePreset` description and checked by the type.
14. **The mesh channel `config.minSeverity` applies on top of the top-level `minSeverity`**, which the spec already
    had for every channel.
15. **Retention class `event` (1825 d) for `mesh-reports`.** The prompt names none, and reports are events.

## Issues found / open questions

1. **Spec and code disagreements, all reconciled in the spec:** `allowedSenders` was an array (061) and an object
   (084); the field type `number` and the delivery status filter `unacked` were missing.
2. **The `ChannelsControllerIT` type count was already stale.** It said 16 when 17 were registered (since 060). It
   now says 19.
3. **The LoRa reference numbers come from the AN1200.13 formula, computed by hand and in a scratch program.** The
   Semtech calculator GUI was not run. SF7/125 kHz with 10 bytes gives 41.216 ms, the commonly quoted 41.2 ms.
4. **Verified on the web 2026-10-13:**
   - the Meshtastic MQTT page gives the JSON downlink topic `msh/US/2/json/mqtt/`, the `from`, `to`, `channel`,
     `type` and `payload` fields, the `mqtt` channel with downlink, and "JSON is not supported on the nRF52
     platform";
   - the radio-settings page gives EU_868 as 869.40–869.65 MHz with a 10 % duty cycle, LongFast as SF11/250/4/5 and
     MediumFast as SF9/250/4/5.
   - MeshCore facts come from the local clone at `a366955c…`.
5. **Instructions in data:** none.
6. **Not built:** an operator-facing per-delivery view of the short code (the column is internal). The
   `docs/Source-Catalog.html` was not regenerated (as in 070–083).

**Owner questions (non-blocking):**
1. Accept `name:` senders for MeshCore channel reports (Deviation 6), or should MeshCore reports come only from
   direct messages authenticated by key?
2. Accept the 133-character and 127-byte MeshCore channel cap (Deviation 1)?
3. Should the airtime window survive a restart (Deviation 9)?

## Tests written (not run)

- **`MeshShortTextFormatterTest`:**
  - the line's parts and the ASCII severities, and position fallbacks (Point and polygon mean);
  - the MeshCore 133-character and 127-byte caps with emoji and umlauts, and the Meshtastic 200-byte cap with CJK
    text, with no split code point and the code always kept;
  - the resolved and digest forms, system lines, and the short codes and their normalization.
- **`MeshCborProfileTest`:** golden vectors (full, 69 bytes; minimal); reduction order (title, then source id) at
  100 and 60 bytes; refusal below the minimum; the 160-byte cap; skipping unknown keys; refusal of non-maps and
  text keys.
- **`LoRaAirtimeTest`:** SF7/125 with 10 bytes is 41.216 ms; LongFast with 132 bytes is 1214.464 ms and with 92 bytes
  927.744 ms; SF12 low-data-rate optimization gives 991.232 ms; presets and bad settings.
- **`AirtimeGovernorTest`:**
  - window accounting and expiry, and ceilings per region;
  - a share above the ceiling (or above 10, or at 0) refused;
  - spacing and count deferrals, and critical priority capped by the ceiling;
  - a deferral, then the fold and drop counts, and `withLimits` keeping the window.
- **`MeshChannelsTest`:** parsing, validation, sender keys, and `config.minSeverity` in `NotificationChannel.accepts`.
- **`MeshtasticChannelTest`:**
  - every `MESH_PUBLIC_CHANNEL` rule, through `ChannelInputParser`;
  - other problems stay `VALIDATION_FAILED`;
  - the envelope, the DM `to` and the topic suffix, and the CBOR `~E1 ` text;
  - Mosquitto (060's Testcontainer) receives the exact envelope on the exact topic, with one client per broker.
- **`MeshCoreChannelTest`** (a fake companion on loopback):
  - the exact 0x03, 0x3E and 0x02 bytes, and the guard registering the three sends;
  - DM ACKED, DM without acknowledgement → retry with `attempt=1` → UNACKED, a refused retry, and SENT without
    `requireAck`;
  - public-slot refusals at save and at send, the slot verdicts, an unreachable companion not blocking a save, and
    the config checks.
- **`MeshReportGrammarTest`:** each command; the missing prefix ignored; malformed coordinates, places, types and
  codes answered with `ERR`.
- **`MeshReportIngestIT`:**
  - a bus message creates a private `field-report` record;
  - `ACK` acknowledges the alert as `mesh:!a1b2c3d4` with an audit row, and the `report` role is refused;
  - unknown sender, wrong channel, index 0, direct message and missing prefix are ignored;
  - `ERR` replies, HELP and STATUS, the rate limit, and a MeshCore `name:` sender.
- **Updated:** `ChannelTypesCatalogIT` (the mesh catalog, a public channel is 400 `MESH_PUBLIC_CHANNEL`, the
  airtime endpoint returns 200 or 404), `ChannelsControllerIT` (19 types), `ChannelInputParserTest`,
  `FamilyDefaultsTest` (`MeshReportsAdapter` is `event`).

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 834 requests, covers all ops: True.
- No project tests were run (feature prompt).
- **Scratch check** (a throwaway `main` class in the scratchpad, run with Gradle's JDK 21) gave the CBOR golden
  bytes and sizes, the airtime numbers and the short codes used in the assertions.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **085 / 086:** nothing in 084 changes the MQTT source pattern. `MeshMessageBus` now has one built-in
   subscriber (`MeshReportService`).
2. **097 checkpoint:**
   - first run of every 084 test;
   - `MeshtasticChannelTest` needs Docker (`MosquittoSupport`);
   - `MeshCoreChannelTest` binds loopback sockets, and the unreachable-companion case waits 5 s;
   - `MeshReportIngestIT` uses a broker on `127.0.0.1:1` (refused) and a companion on `127.0.0.1:1` (5 s probe);
   - check the 083 `MeshCoreLinkTest` still passes after the runner fix (Deviation 5).
3. **113 (MeshDTN):** extend `notify.mesh.cbor.MeshCborProfile` (keys 10 and up are skipped by today's decoder).
   Register a MeshCore `dataType` before publishing (the default 0xFF45 is in the development range).
4. **143:**
   - verify against real radios: a Meshtastic JSON downlink (some firmware needs `topicSuffixNodeId`);
   - a MeshCore channel send answered OK, the 0x82 layout, and a slot read on current firmware;
   - the airtime presets against the nodes' settings.
5. **144 (Flyway):** new column `notification_delivery.short_code varchar(8)`; new status `UNACKED` (the status
   check constraint is still dropped at start); new deferred reason `airtime`.
6. **Owner decisions pending:** the three questions above, plus those carried from 012–083.
