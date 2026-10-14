# `meshtastic-local` fixtures

| File | What it is |
|---|---|
| `private-broker-5min.tsv` | **`synthetic: true`.** Five minutes of `msh/local/2/json/#` as a private Mosquitto broker would deliver it from the operator's node `!a0b1c2d3` (an unsigned id above 2^31) with JSON enabled and uplink encryption off. One line per message: topic, tab, JSON in the firmware's shape (`channel, from, id, payload, sender, timestamp, to, type`, plus `rssi`, `snr`, `hops_away` for heard packets). 16 lines: the own node's nodeinfo, position (32 bits), telemetry twice; the neighbor `!12345678`'s nodeinfo, two positions (16 bits) and three telemetry messages (one environment-only, battery falling to 12 and then 11 %); a broadcast text, a direct text to the own node, a `#E` text on channel 1 (`Ops`); a nodeinfo from old firmware with a **signed** `from` (`-1430532899` = `!aabbccdd`); an unknown type (`detection`); and a `/2/e/` protobuf topic line whose body is not JSON and must never be parsed. |

No node or broker was reachable during the build; the shapes follow the firmware's JSON serializer (field names checked
against `MeshPacketSerializer.cpp`, read only, not copied) and `../meshtastic-mqtt/public-json-sample.tsv`.

**Recapture** (prompt 143, on the operator's hardware): with the node's MQTT module pointed at the broker (JSON on,
encryption off), record five minutes as topic and hex payload, then convert the hex to JSON and replace names and
texts:

```sh
timeout 300 mosquitto_sub -h 192.168.1.10 -p 1883 -t 'msh/local/2/json/#' -F '%t	%x' > capture.tsv
```
