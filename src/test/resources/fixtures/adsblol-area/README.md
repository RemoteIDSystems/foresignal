# `adsblol-area-*` fixtures

| File | What it is |
|---|---|
| `point-41.98--87.90-250.json` | `GET https://api.adsb.lol/v2/point/41.98/-87.90/250` (250 nm around Chicago O'Hare), captured 2026-10-13T12:24:03Z, trimmed to 30 aircraft |

**Capture command:**

```sh
curl -sA "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://api.adsb.lol/v2/point/41.98/-87.90/250
```

`200`, `content-type: application/json`, 278,763 bytes, 544 aircraft, `now: 1791894243001` (milliseconds).
The center was chosen because TIS-B traffic (`~` addresses) is common around US terminal areas; an earlier
capture around London Heathrow (`/v2/point/51.47/-0.45/250`, 743 aircraft) had none and is not kept.

**Trimming:** kept 2 `~` (TIS-B trackfile) addresses, 2 aircraft on the ground, 1 with `dbFlags: 8` (LADD),
1 Mode S, then plain `adsb_icao` entries up to 30. Entries are unchanged; `total` was rewritten to 30.
