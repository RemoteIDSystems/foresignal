# `adsbfi-region-*` fixtures

| File | What it is |
|---|---|
| `region-39.86--104.67-25.json` | `GET https://opendata.adsb.fi/api/v3/lat/39.86/lon/-104.67/dist/25` (Denver International), captured 2026-10-13T22:50:37Z, trimmed to 25 aircraft |

**Capture command** (prompt 080):

```sh
curl -s --compressed -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://opendata.adsb.fi/api/v3/lat/39.86/lon/-104.67/dist/25
```

The response was `200`, `content-type: application/json`, gzip, 74 aircraft (70 `adsb_icao`, 4 `adsr_icao`) with
`now: 1791931836001` (milliseconds), `ctime`, `ptime: 0` and `msg: "No error"`: the same envelope as `/v2/mil`.
v3 entries add `dst` (nm from the query point) and `dir` (degrees from it), which the mapper leaves out because
they are relative to the query. A first try at Helsinki-Vantaa (`/v3/lat/60.32/lon/24.96/dist/60`) returned only
2 aircraft at night and was not kept.

**Trimming:** all 4 `adsr_icao` entries plus the first 21 others, in upstream order (11 on the ground). The
entries are unchanged; `total` was rewritten to 25.
