# `digitraffic-rail` fixtures

| File | What it is |
|---|---|
| `latest.json` | `GET https://rata.digitraffic.fi/api/v1/train-locations/latest`, captured 2026-10-13T22:50:46Z, unchanged (32 trains) |
| `train-2026-10-14-2241.json` | `GET https://rata.digitraffic.fi/api/v1/trains/2026-10-14/2241`, captured the same minute; `timeTableRows` trimmed from 38 to 2 |
| `latest-edge.json` | Hand-made: one valid fix, then a train without `location`, one at (0, 0), one with an unreadable `timestamp` and one without `trainNumber` |

**Capture commands** (prompt 080):

```sh
curl -s --compressed -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" -H "Digitraffic-User: Foresignal/0.1.0-SNAPSHOT" https://rata.digitraffic.fi/api/v1/train-locations/latest
curl -s --compressed -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" -H "Digitraffic-User: Foresignal/0.1.0-SNAPSHOT" https://rata.digitraffic.fi/api/v1/trains/2026-10-14/2241
```

`latest`: `200`, `content-type: application/json`, gzip transfer encoding, 6,334 bytes decoded at night (854 when gzipped locally),
`cache-control: max-age=1, public`, served through CloudFront (`x-cache: Hit from cloudfront`). Every entry has
exactly `accuracy` (integer), `isGpsLocation`, `location {type: Point, coordinates: [lon, lat]}`, `speed` (km/h),
`trainNumber`, `departureDate`, `timestamp`; all 32 had `isGpsLocation: true` and `accuracy: 0`. Trains running
after midnight Finnish time already carry the next `departureDate` (`2026-10-14`).

The lookup returned one cargo train: `trainType: "T"`, `trainCategory: "Cargo"`, `operatorShortCode: "vr"`,
`commuterLineID: ""` (blank, so omitted).
