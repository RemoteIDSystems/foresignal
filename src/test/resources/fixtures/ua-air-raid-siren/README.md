# `ua-air-raid-siren` fixtures

| File | What it is |
|---|---|
| `alerts.json` | The real body of `GET https://siren.pp.ua/api/v3/alerts`, captured 2026-10-13T23:35:23Z (22,429 bytes, sha256 `c44ea958107769ee2a19d7c9183372a9694c847195e6904c5495d38a92255101`) |

Capture (prompt 081): `curl -s -D - https://siren.pp.ua/api/v3/alerts`. Headers: `200`, `Content-Type: application/json`,
`Cache-Control: public, max-age=1`, `ETag: "c44ea958…"`, `X-Snapshot-Age`, `X-Cache-Status: HIT`.

Contents: 45 regions (35 `District`, 6 `Community`, 4 `State`) with 52 active alerts (45 `AIR`, 6 `ARTILLERY`,
1 `URBAN_FIGHTS`). Two alerts are listed under a child region with their parent's `regionId` (for example `122:AIR`,
Chuhuivskyi district, inside community 1313), so the parser keeps 50 records. Luhansk (`16`) and Crimea (`9999`, blank
`regionEngName`) carry `AIR` alerts unchanged since 2022 (occupied areas). The `reason` texts are the wrapper's and are
not stored. Recapture at prompt 143.
