# `adsblol-mil` fixtures

| File | What it is |
|---|---|
| `mil.json` | `GET https://api.adsb.lol/v2/mil`, captured 2026-10-13T12:23:13Z, trimmed to 30 aircraft |
| `adsblol-html-200.html` | Hand-made: an HTML page served with status 200, which the adapter must refuse as "non-JSON 200, treated as throttling" |

**Capture command** (prompt 062):

```sh
curl -sA "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://api.adsb.lol/v2/mil
```

The response was `200`, `content-type: application/json`, 124,761 bytes, 304 aircraft, with
`now: 1791894193500` (milliseconds) and `msg: "No error"`. No rate-limit headers were sent.

**Trimming:** kept, in this order, 2 aircraft on the ground (`alt_baro: "ground"`), 2 with only
`lastPosition`, 1 with no position at all, 2 MLAT positions (`type: "mlat"`), 1 ADS-R, 1 `adsb_icao_nt`,
1 Mode S with a position, 1 with `nac_p < 8`, then plain `adsb_icao` entries up to 30. Every `/v2/mil`
entry carries `dbFlags: 1` (military). The entries are unchanged; `total` was rewritten to 30 to match the
trimmed `ac`. The capture had no `~` (non-ICAO) address: that case is in `../adsblol-area/` and
`../readsb/readsb-edge.json`.

**The `/v2/point/0/0/20000` check** (prompt 062 asks for it once): the same command against
`https://api.adsb.lol/v2/point/0/0/20000` at 2026-10-13T12:23:22Z returned **HTTP 200** with 9,885
aircraft (5,563,639 bytes), spread to about 9,090 nm from (0, 0). The radius was **not** clamped to 250 nm
and no 422 was returned, although the published OpenAPI declares `maximum: 250`. That body is not kept, and
no global source is built on it: the behavior is undocumented.
