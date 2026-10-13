# `adsbfi-mil` fixtures

| File | What it is |
|---|---|
| `mil.json` | `GET https://opendata.adsb.fi/api/v2/mil`, captured 2026-10-13T22:50:14Z, trimmed to 30 aircraft |
| `edge.json` | Hand-made in the same envelope: a `~` (non-ICAO) TIS-B trackfile whose `tisb` list names `lat`; a Mode S entry with only `lastPosition` (`seen_pos` 350.4 s) and the rough receiver position `rr_lat`/`rr_lon`; an upper-case hex with a large `seen_pos` (58.9 s) and the v3 query fields `dst`/`dir` |

**Capture command** (prompt 080):

```sh
curl -s --compressed -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://opendata.adsb.fi/api/v2/mil
```

The response was `200`, `content-type: application/json`, `content-encoding: gzip`, `cache-control: no-store`,
103,952 bytes decoded, 228 aircraft (151 `adsb_icao`, 46 `mode_s`, 21 `mlat`, 10 `adsb_icao_nt`), with
`now: 1791931814001` (milliseconds), `ctime` equal to `now`, `ptime: 0` and `msg: "No error"`. No rate-limit
headers were sent. Entries carry fields the mapper leaves out (`rr_lat`, `rr_lon`, `ownOp`, `year`, `calc_track`,
`nav_*`, `messages`, `rssi`, ...).

**Trimming:** kept, in this order, 2 aircraft on the ground (`alt_baro: "ground"`), 2 with only `lastPosition`
(`ae4ff0`: `lastPosition.seen_pos` 761.286 s), 1 with no position at all (`3ea3b0`), 2 MLAT positions (`mlat`
list names `lat`), 1 `adsb_icao_nt` without a position, 1 with `nac_p < 8` (`ae1408`), the largest `seen_pos`
(`43c93a`, 46.854 s), then plain `adsb_icao` entries up to 30. The entries are unchanged; `total` was rewritten to
30. The capture had no `~` address: that case is in `edge.json`.
