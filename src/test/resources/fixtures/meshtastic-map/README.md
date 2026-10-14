# `meshtastic-map` fixtures

| File | What it is |
|---|---|
| `nodes-200.json` | **Real, trimmed and anonymized.** `GET https://meshtastic.liamcottle.net/api/v1/nodes` on 2026-10-14 01:19 UTC returned 28,475,677 bytes (gzip on the wire, `ETag: W/"1b2811d-…"`) holding 30,557 nodes. Kept: the first 196 nodes plus four chosen edge cases (a node without a position, an unsigned node id of 2^31 or more with a position, a 32-bit precision position, a battery below 15 %), 200 in all: 138 with a position, 93 with ids of 2^31 or more, all with `updated_at`. Every field is as served except: `node_id`, `node_id_hex` and `id` are synthetic (`0x10000000 + i`, or `0xF0000000 + i` where the real id was 2^31 or more), `long_name` is `Synthetic Node NNN`, `short_name` is `SNNN`, `latitude` and `longitude` are rounded to 1e5 units (0.01°), and `neighbours` is null. |

**Coordinate scaling confirmed:** `latitude` and `longitude` are integers × 1e-7 (for example `537100288` is 53.71°), like
the firmware's `latitude_i`. `voltage`, `channel_utilization`, `air_util_tx` and `uptime_seconds` arrive as strings,
`battery_level` and `position_precision` as numbers. `hardware_model_name` and `role_name` give the names of the numeric
`hardware_model` and `role`.

**Recapture** (prompt 143): one request a day at most.

```sh
curl -sS --compressed -A "Foresignal/<version>" -o nodes.json https://meshtastic.liamcottle.net/api/v1/nodes
```

then trim and anonymize as above.
