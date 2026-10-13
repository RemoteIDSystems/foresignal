# 060-CR-MQTT-Channel-And-HA-Discovery

| | |
|---|---|
| Prompt | 060-MQTT-Channel-And-HA-Discovery.md |
| Status | Complete |
| Implementation commit | 9b426889f83ecb12d5322d032ee054226abf4351 |
| Date | 2026-10-13 |

## Summary

Foresignal now publishes to the operator's MQTT broker and shows up in Home Assistant through MQTT discovery.
- **Event deliveries:** alerts, alert updates, briefings and system events go out as an `mqtt` notification
  channel through the 018 durable pipeline.
- **Retained state:** rule states, source health and the glance summary are published as retained topics.
- **Last Will:** consumers can see when Foresignal is down.
- **One supervised client** (`MqttConnections`) is meant to be reused by the MQTT source adapters (083, 085).
- `republishMqttChannel` is `implemented` and off `pending-operations.txt`.

**1. Library and shared client** (`ai.foresignal.mqtt`).
- **Library decision:** HiveMQ MQTT Client **1.4.0, shaded artifact** (`com.hivemq:hivemq-mqtt-client-shaded`,
  Apache-2.0).
  - Checked on Maven Central: 1.4.0 is the latest version of both artifacts (metadata `lastUpdated`
    20260821).
  - Paho (`org.eclipse.paho.mqttv5.client`, last release 1.2.5 in 2020) was not used.
- **Netty resolution:** `./gradlew dependencyInsight --dependency netty-handler --configuration runtimeClasspath`
  with the plain artifact printed `io.netty:netty-handler:4.1.137.Final -> 4.2.17.Final`: the Spring Boot 4.1.1
  BOM moves HiveMQ's declared Netty 4.1 to 4.2. That is a clash between Netty lines, so the shaded artifact is
  used, as the prompt says to.
  - The shaded jar relocates its own Netty 4.1.137 (including the WebSocket codec and the SSL handler), JCTools,
    Dagger and javax.inject under `com.hivemq.client.internal.shaded`.
  - After the switch, no `netty-handler` is on the runtime classpath. Only RxJava 2.2.21, Reactive Streams
    1.0.4 and JetBrains annotations remain as dependencies.
  - Recorded in `gradle/libs.versions.toml`, `build.gradle.kts` and Architecture §2.
- **`MqttConnection`** wraps an `Mqtt5AsyncClient`, or an `Mqtt3AsyncClient` for `3.1.1`.
  - Session: keepalive 60 s, clean start off, session expiry 1 h.
  - Reconnect: the client's disconnected listener schedules `MqttBackoff` (1 s doubling to 120 s, ±20 %
    jitter). The outbound URL policy is checked again before each attempt.
  - TLS: the system trust store plus the optional `caCertPem` (`MqttTls`). Hostname verification stays on;
    I checked by disassembling `MqttSslInitializer` that HiveMQ sets the `HTTPS` endpoint identification when
    no verifier is set.
  - Credentials come from the secrets.
  - Last Will: `{prefix}/status` = `offline`, retained, QoS 1. `online` is published retained on every
    (re)connect. A graceful close publishes `offline`, then a normal DISCONNECT.
  - Publishing while disconnected fails at once, so the delivery ladder decides when to retry.
- **`MqttConnections`:**
  - client id `foresignal-{installId}-{channelIdShort}`;
  - the policy check `LAN_ALLOWED` on the broker host;
  - the gauge `foresignal.mqtt.connected{channel}`;
  - the `/v1/system` `mqtt` block `{connections, connected, lastError}` (a `SystemBlockContributor`);
  - closes every connection gracefully on shutdown.

**2. `mqtt` channel type** (`notify.types.MqttChannel`, `ai.foresignal.mqtt.channel`).
- **Config** (`MqttChannelConfig`) has every field of the prompt with its default, and `validate` checks all of
  them: broker URL schemes, prefix pattern, QoS 0/1, durations, discovery caps, payload template, CA PEM, and
  that a 3.1.1 password has a username. The broker host also goes through the save-time outbound check in
  `ChannelInputParser`.
- **Deliveries** render a new `PreparedRequest.Mqtt` body. `ChannelSender` hands it to `MqttDeliveryGateway`
  (implemented by `MqttChannelManager`). PUBACK (QoS 1) or a completed write (QoS 0) is reported as 200; a
  missing connection, a refusal or a timeout is an `IOException` and goes onto the retry ladder. The rate limit
  is 50/s.
- **Topics and payloads** follow the prompt's table (`MqttTopics`, `MqttPayloads`):
  - Topic segments built from ids or names are sanitized.
  - Payloads are UTF-8 JSON of at most 256 KB; the markdown or body is cut first. Briefing markdown is limited
    to 16 KB, with a link to the run.
  - The MQTT 5 content type is `application/json`, and the message expiry applies to non-retained events.
- **Private-source alerts** arrive already reduced by the 039 `DeliveryWorker.privacy` step.
- **Channel test** publishes `{prefix}/test` and reports the PUBACK latency in `durationMs`, with `status` null.
- **Warning `MQTT_INSECURE_PUBLIC_BROKER`** (`ChannelType.warnings`, `ChannelWarning`): returned in the channel
  response when the host resolves to a public address and the channel has no TLS or no username.
- **`MqttChannelManager`** opens a connection for each enabled channel at startup (`StartupOrder.MQTT` = 37)
  and after every new `ChannelChangedEvent`, published by `ChannelService` and handled after commit. A changed
  setting or secret reopens the connection; disabling or deleting the channel closes it.
- **`MqttStatePublisher`** publishes:
  - **Rule state** on fire, acknowledge and resolve (from the AlertBus): `firing`, `resolved` or `idle`. A
    deleted rule gets an empty retained payload.
  - **Health** on `SourceHealthChangedEvent` and every 10 min; private sources are skipped.
  - **Summary:** the admin glance, checked every `summaryInterval` and published only when its ETag (without
    `serverTime`) changed.
  - Everything again after each reconnect.
- **`POST /v1/channels/{id}/mqtt/republish`:**
  - 202 when the republish is queued;
  - 404 for an unknown or non-mqtt channel;
  - 409 `CHANNEL_DISABLED` for a disabled channel.

**3. Home Assistant discovery** (`HaDiscoveryPayloads`, `HaDiscoveryPublisher`).
- **Device config:** published on `{haPrefix}/device/foresignal_{installId}/config` with `dev`, `o`,
  `availability`, `qos` and these `cmps`:
  - the open-alert sensors per severity, `sources_failing`, `sources_degraded` and `last_briefing`
    (timestamp);
  - three `event` entities, one per severity;
  - a `problem` binary_sensor per enabled rule (critical first, then by name, up to `haMaxRules`);
  - a `problem` binary_sensor per non-private, non-disabled source (up to `haMaxSources`).
- **When it is published:**
  - on connect;
  - after `online` on `{haPrefix}/status` (subscribed), with a random 0–5 s delay;
  - 5 s after the last rule or source change (debounced).

  It is retained only with `haRetainDiscovery`.
- **Removal:** a dropped component is published as `{p}` first, then the config goes out without it. Turning
  discovery off, disabling the channel or deleting it publishes an empty retained payload.
- **`device_tracker`** is documented as deferred.

**4. Spec, Postman, docs.**
- **OpenAPI:**
  - `republishMqttChannel` is `implemented`, with a 409 description;
  - the `listChannelTypes` description and an `mqtt` response example;
  - `ChannelInput.type` docs;
  - `MqttChannelConfig` descriptions, plus maxima and a pattern;
  - `Channel.warnings` description;
  - error code `CHANNEL_DISABLED`;
  - a changelog entry.

  `SystemInfo.mqtt` and `MQTT_INSECURE_PUBLIC_BROKER` were already in the spec and match.
- **Postman:** an `mqtt` `createChannel` variant, then regenerated.
- **Docs:**
  - Architecture §2 (the MQTT row, with the note for 083 and 085), the package layout, the §17 row and the new
    §17.4 (topic tree);
  - Operator Guide "MQTT and Home Assistant": Mosquitto with an ACL, the topic table, an HA automation on the
    critical event entity, Node-RED, and displays such as AWTRIX;
  - `THIRD_PARTY_NOTICES.md`: HiveMQ and its relocated and transitive libraries, each license checked in its
    pom;
  - Invariants EGR-1 gains `MqttChannelTypeTest`.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/mqtt/`: `package-info`, `MqttProperties`, `MqttEndpoint`, `MqttMessage`,
  `MqttInbound`, `MqttConnectionSpec`, `MqttBackoff`, `MqttTls`, `MqttConnection`, `MqttConnections`
- `src/main/java/ai/foresignal/mqtt/channel/`:
  - `package-info`, `MqttChannelConfig`, `MqttTopics`, `MqttPayloads`, `HaDiscoveryPayloads`;
  - `MqttChannelRuntime`, `MqttChannelRegistry`, `MqttChannelManager`, `MqttStatePublisher`,
    `HaDiscoveryPublisher`.
- `src/main/java/ai/foresignal/notify/`: `MqttDeliveryGateway`, `ChannelWarning`, `ChannelChangedEvent`,
  `types/MqttChannel`
- Tests:
  - `notify/types/MqttChannelTypeTest`;
  - `mqtt/MqttConnectionsTest`, `mqtt/channel/HaDiscoveryPayloadTest`, `mqtt/channel/MqttChannelIT`,
    `mqtt/channel/HaDiscoveryIT`;
  - `api/ChannelTypesCatalogIT`;
  - `testsupport/mqtt/MosquittoSupport` and `MqttProbe`.
- Test resources:
  - `fixtures/mqtt/` (golden publishes, `ha-device.json`, README);
  - `mqtt/mosquitto.conf`, `mqtt/certs/` (a throwaway test CA certificate, plus a server certificate and key
    for localhost), README.

**Modified**
- `notify/`: `PreparedRequest` (the `Mqtt` body), `ChannelSender`, `ChannelService` (events, MQTT test status),
  `ChannelType` (`warnings`), `ChannelInputParser` (mqtt available, broker host policy), `ChannelCapabilities`
  (javadoc), `types/WebhookChannel` (`renderTemplate` public)
- `api/ChannelsController` (republish), `api/mapper/ChannelMapper` (`warnings` when non-empty),
  `api/error/ErrorCodes`, `config/StartupOrder`, `settings/SettingsTransferService` (`warnings` output-only)
- `build.gradle.kts`, `gradle/libs.versions.toml`, `THIRD_PARTY_NOTICES.md`
- `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
  `src/test/resources/openapi/pending-operations.txt`, `src/test/resources/application-test.yml`
  (`foresignal.mqtt`: unscheduled, keepalive 5 s, connect timeout 3 s)
- `api/ChannelsControllerIT`, `notify/ChannelInputParserTest`: mqtt is no longer a "later type" (they now use
  meshtastic, and mqtt validation was added)

## Deviations from the prompt

1. **Shaded HiveMQ artifact**, because of the Netty clash (Summary §1), as the prompt itself provides.
2. **Product Charter FR-35 was not edited.** It is the owner's document; see Open questions.
3. **`mqtt` is not a fan-out type.** The 018 `ChannelCapabilities` javadoc expected `fanOut` for mqtt, but the
   prompt requires the ordinary pipeline (rows, rate limit, ladder). Each delivery is one publish through
   `ChannelSender`, and the javadoc was corrected.
4. **Snoozed alerts** also publish on `{prefix}/alerts/updates`. The update renders every lifecycle follow-up;
   the payload is the prompt's `{id, state, resolvedAt, acknowledgedBy}`. A digest (`alert.digest`) goes to the
   severity topic like a fired alert.
5. **Spec amendments:**
   - `haMaxRules` and `haMaxSources` get `maximum: 500`, to keep the discovery config bounded;
   - `haPrefix` gets the `topicPrefix` pattern;
   - `summaryInterval` must be at least 10 s;
   - new error code `CHANNEL_DISABLED` for the republish 409 (the spec named the condition but no code).

   Recorded in the changelog.
6. **`Channel.warnings` is returned only when non-empty**, so existing channel responses and the settings
   export are unchanged. `SettingsTransferService` drops it as output-only.
7. **Summary ETag** ignores `serverTime`; otherwise every glance would count as changed.
8. **Discovery source list** leaves out disabled sources as well as private ones. The prompt says
   "non-private" only.
9. **Rule-state semantics:** `firing` while any alert is unresolved (acknowledged counts as firing),
   `resolved` when every alert is resolved, `idle` with none.
10. **Rule deletion** is detected from `TriggerRulesChangedEvent` by comparing the rules the channel published
    against the current rules (`TriggerRuleService` has no delete-specific event).
11. **LWT and "broker stopped" tests** cut the connection with Toxiproxy (`testcontainers-toxiproxy` 2.0.5, BOM)
    instead of stopping the broker. A restarted container would get a new mapped port.
12. **MQTT 3.1.1 client ids** are 28 characters. The 3.1.1 spec only guarantees 23, though Mosquitto and
    common brokers accept longer ids. The format is the prompt's.

## Issues found / open questions

1. **Owner: Product Charter FR-35** should list `mqtt` among the channel types (the prompt asks for it). It was
   not edited, per the rule that the Charter is the owner's.
2. A test CA certificate and a server **private key for localhost** are committed under
   `src/test/resources/mqtt/certs/`. They are throwaway, test-only and protect nothing; the CA key was
   discarded. Say if test keys must not live in the repository.
3. Broker connections resolve the host themselves after the policy check (as SMTP does). They are not pinned to
   the checked address, because pinning would lose the TLS host name.
4. Home Assistant behavior checked against its docs (event entity: extra payload members become attributes).
   The rest is unverified against a live HA instance until 143.
5. Data contained no instructions directed at the agent.

## Tests written (not run)

- `MqttChannelTypeTest`:
  - golden publishes for alert fired, alert update, briefing and test;
  - the system event JSON;
  - prefix, QoS 0, expiry and template handling;
  - point and near absent;
  - the 256 KB cap (body and template), the 16 KB briefing cut, cutting on character boundaries;
  - private reduction;
  - topic sanitizing;
  - valid and invalid configs, broker URL rules, the 3.1.1 username rule;
  - the catalog entry;
  - the insecure-broker warning.
- `HaDiscoveryPayloadTest`:
  - the golden device config;
  - component order and `unique_id`s;
  - rule cap and ordering, source cap and sanitizing;
  - removal (`{p}` then without the component);
  - no `cu` without a base URL;
  - an unknown severity.
- `MqttConnectionsTest`:
  - the backoff schedule and jitter bounds;
  - the client id format;
  - the 3.1.1 fallback and a 3.1.1 client that builds;
  - policy refusal;
  - endpoint parsing, topic filter matching, TLS and message checks.
- `MqttChannelIT` (Mosquitto `eclipse-mosquitto:2` with anonymous, password and TLS listeners, and Toxiproxy
  `ghcr.io/shopify/toxiproxy:2.12.0`):
  - an alert on the severity topic with expiry and content type, then the update;
  - retained rule state set, then cleared on rule delete;
  - retained health and summary, and the summary not re-published when unchanged;
  - the LWT `offline` on cut, `online` after reconnect, `offline` on graceful close;
  - QoS 1 retries on the ladder while unreachable;
  - TLS with the test CA (and failure without it);
  - the password listener;
  - the `/v1/system` block and the metric;
  - the republish endpoint 202;
  - MQTT 3.1.1.
- `HaDiscoveryIT`:
  - discovery on connect (not retained) and after `online`;
  - the debounce, and component removal;
  - removal on disable and on delete (retained cleared).
- `ChannelTypesCatalogIT`: the catalog field specs, the warning on create and get, the republish 404/409/401.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "831 requests across 80 tag folders … covers
  all ops: True".
- `./gradlew dependencyInsight --dependency netty-handler --configuration runtimeClasspath`: no Netty after the
  switch to the shaded artifact (with the plain artifact: 4.1.137.Final -> 4.2.17.Final).

## Notes for the next prompt

1. **083 / 085 (MQTT sources):** open connections with
   `MqttConnections.open(new MqttConnectionSpec(key, label, clientId, endpoint, version, user, pass, caPem, statusTopic), onConnected)`.
   - Use a key other than `channel:<id>`, for example `source:<id>`.
   - Subscribe with `MqttConnection.subscribe(filter, handler)`; it re-subscribes after each reconnect, and
     handlers run on virtual threads.
   - The status topic and Will are mandatory in the spec. For a subscriber, pick a topic under the source's
     own prefix, or extend the spec to make it optional.
2. **097 checkpoint:**
   - The MQTT ITs need Docker images `eclipse-mosquitto:2` and `ghcr.io/shopify/toxiproxy:2.12.0`, started once
     per JVM by `MosquittoSupport`.
   - Each test uses its own random prefix, because retained messages outlive tests.
   - `HaDiscoveryIT.debounceAndComponentRemoval` and `onConnectAndBirth` depend on real time (5 s debounce,
     0–5 s birth jitter); widen the timeouts if they flake.
   - Possible fragility: the empty retained payload being forwarded to live subscribers (MQTT 5 says it is),
     and the summary-unchanged check if the glance changes between calls.
3. **143:** point a real Mosquitto and Home Assistant at a deployment. Confirm the device, the entity ids
   (`event.foresignal_critical_alert`), the LWT on `kill -9`, and AWTRIX or Node-RED consumption.
4. **144 (Flyway):** no schema change in this prompt.
5. **Owner decisions pending:** Issues 1–2 above, plus those carried from 012–059.
