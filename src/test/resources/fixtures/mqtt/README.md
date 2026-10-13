# MQTT channel fixtures (prompt 060)

Golden publishes of the `mqtt` channel type (`MqttChannelTypeTest`) and the Home Assistant device config
(`HaDiscoveryPayloadTest`). Each publish fixture holds the topic, QoS, retain flag, MQTT 5 message expiry,
content type and the JSON payload; the test compares the payload as a JSON tree, so member order does not
matter. The values follow the payload tables of `prompts/060-MQTT-Channel-And-HA-Discovery.md` and the Home
Assistant MQTT discovery documentation (device-based discovery, verified 2026-10).
