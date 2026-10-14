# `meshtastic-mqtt` fixtures

| File | What it is |
|---|---|
| `public-json-sample.tsv` | **Real, anonymized.** Three minutes of the public broker's JSON tree, captured on 2026-10-14 with one connection and one subscription within the Mesh.md rules: `mosquitto_sub -h mqtt.meshtastic.org -p 1883 -u meshdev -P large4cats -i foresignal-fixture-capture -k 120 -q 0 -t 'msh/US/2/json/#' -F '%t	%x'` (run in the `eclipse-mosquitto:2` image). 59 messages: 27 position, 11 telemetry, 10 nodeinfo, 1 text and 1 traceroute in the firmware's shape, plus third-party variants that are not firmware JSON (empty `type`, `sendtext` with a string payload, a topic without a gateway segment, string node ids without `!`, float `latitude`/`longitude`). One line per message: the topic, a tab, the payload re-serialized as compact JSON. |

**Anonymized:** every node number (in `from`, `to`, `sender`, `id`, `node_id` and the topic's gateway segment) maps to a
synthetic one, keeping the property of being at least 2^31 (`0xE00000nn`) or not (`0x200000nn`) and the sign of signed
values; names are `Synthetic Node NNN` / `NNNN`; texts and string payloads are `synthetic text N`; traceroute hops are
`Synthetic Hop N`; public keys are SHA-256 of a synthetic string; `latitude_i`/`longitude_i` are rounded to 1e5 units
(0.01°) and float coordinates to two decimals; channel names other than the presets (`LongFast`, `MediumFast`,
`Telemetry`, `Primary`, `mqtt`) are `SyntheticChanN`. Timestamps, RSSI, SNR, hops, telemetry and precision bits are as
received.

**Verified 2026-10:** JSON on the public broker is sparse (about 20 messages a minute across `msh/US`, against far more
protobuf on `/2/e/`), and many positions are reduced to 13–16 bits, though some nodes publish 32.

**Recapture** (prompt 143): the command above, at most once, for at most a few minutes.
