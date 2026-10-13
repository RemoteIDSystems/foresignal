# `amtraker` fixtures

| File | What it is |
|---|---|
| `trains.json` | `GET https://api.amtraker.com/v3/trains`, captured 2026-10-13T22:50:58Z, trimmed to 10 trains across all three providers |
| `trains-edge.json` | Hand-made: a train without a position (`lat`/`lon` null), one with a sixteen-point heading (`NNE`), a `trainTimely` text, `stations[]`, `alerts[]` and a color, and one with an unreadable `lastValTS` and heading |

**Capture command** (prompt 080):

```sh
curl -s --compressed -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://api.amtraker.com/v3/trains
```

The response was `200`, `content-type: application/json`, gzip transfer encoding, 1,281,203 bytes decoded (111,572 bytes when gzipped locally),
202 train numbers holding 218 trains: 149 `Amtrak`, 60 `Via`, 9 `Brightline`; 195 `Active`, 19 `Predeparture`,
4 `Completed`. Every `trainTimely` was blank. `heading` used only the eight compass points. `Predeparture` trains
sit at their origin station with `lastValTS` set to the scheduled departure (`28-13`: `2026-10-13T16:30:00-07:00`,
40 minutes after the capture).

**Trimming:** kept `1-12` (Amtrak, NW, 77.5 mph), `28-13` (Predeparture), `4-11` (Completed), `90-13` (NE),
`43-13` (SW), `v29-13` (VIA, W, moving), `v14-11` (VIA, stopped), `b5151-13` and `b5348-13` (Brightline) and
`v22-13` (VIA, with an alert). `stations[]` and `alerts[]` were cut to their first element; nothing else changed.

**Terms** (read 2026-10-13 in `https://github.com/piemadd/amtrak`, README at commit
`126de6a6854df0b86bdfb9843d47e41369088a77`): ODC-By 1.0, attribution to Amtraker with a link to amtraker.com, and
an identifying User-Agent is required.
