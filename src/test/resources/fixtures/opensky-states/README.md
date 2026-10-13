# `opensky-states` fixtures

| File | What it is |
|---|---|
| `states-bbox.json` | Anonymous `GET https://opensky-network.org/api/states/all?extended=1&lamin=45&lomin=5&lamax=48&lomax=10`, captured 2026-10-13T12:23:26Z, trimmed to 30 states |
| `headers.txt` | The response headers of that capture (`x-rate-limit-remaining: 395`) |
| `states-null.json` | Hand-made: `{"time": …, "states": null}`, the documented "no aircraft" answer |
| `states-edge.json` | Hand-made: every index exercised, null lat/lon, on ground, position sources 0–3, categories 0, 1, 2, 8, 9, 10, 13, 14, 16 and 20 |

**Capture command:**

```sh
curl -sA "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" -D headers.txt \
  "https://opensky-network.org/api/states/all?extended=1&lamin=45&lomin=5&lamax=48&lomax=10"
```

`200`, `content-type: application/json`, 30,464 bytes, 236 states, `time: 1791894198`. The box is
3° × 5° = 15 sq°, so it cost 1 credit. Anonymous states carry `sensors: null` and, in this capture,
`position_source: 0` and `category: 0` for every row; `states-edge.json` covers the other values.

**Trimming:** the first 24 states, then up to 6 more that are on the ground, positionless or not ADS-B
(only on-ground rows existed). States are unchanged.

The OpenSky terms page (`https://opensky-network.org/about/terms-of-use`) refused automated fetches when
the prompt was written; it was not fetched here.
