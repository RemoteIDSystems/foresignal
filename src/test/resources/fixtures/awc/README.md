# Fixtures: the AWC sources (`awc-metars`, prompt 047; the others, prompt 087)

| File | What it is |
|---|---|
| `metars.cache.csv.gz` | A real capture of the AWC METAR cache file, untrimmed and still gzipped as served: 5,046 data rows, one per station, the header on the first line (no preamble). 25 rows have no position or time. |
| `preamble.csv` | **Synthetic**, plain text: the five-line preamble older cache files carried, the live header, two reports of `KTST` (a 00:00 METAR and a newer 00:53 SPECI with a gust, `10+` visibility, `OVC003` and `LIFR`) and a `VRB03KT` report of `KVRB` with `wind_dir_degrees = 0` and flight category `null`. |

## Capture procedure

- **Date:** 2026-10-13 00:02 UTC (`last-modified: Tue, 13 Oct 2026 00:01:07 GMT`).
- **Command:**

  ```sh
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -D headers.txt \
    -o metars.cache.csv.gz https://aviationweather.gov/data/cache/metars.cache.csv.gz
  ```

- **Response:** HTTP 200, `content-type: application/octet-stream` (no `Content-Encoding`: the body is
  the gzip file itself), `content-length: 270905`, `etag: "6acd74c3-42239"`, `last-modified`,
  `cache-control: max-age=30`.

Facts the parser relies on, from this file: a variable wind (`VRB02KT`) appears as
`wind_dir_degrees = 0`; visibility is `10+` (US) or `6+` (9999 m); a missing flight category is the
string `null`; the four `sky_cover,cloud_base_ft_agl` pairs repeat the same column names.

## Prompt 087 products

| File | Source | What it is |
|---|---|---|
| `airsigmet.geojson` | `awc-sigmets` | Real, `/api/data/airsigmet?format=geojson`: 7 convective SIGMETs (KKCI). |
| `isigmet.geojson` | `awc-sigmets` | Real, `/api/data/isigmet?format=geojson`, trimmed from 144 to 21 features (up to four per hazard, two KKCI). Three have open polygon rings, as served. |
| `gairmet.geojson` | `awc-gairmets` | Real, `/api/data/gairmet?format=geojson`: 27 snapshots, all `forecast` 3; seven freezing-level LineStrings share tag 1C at five levels. |
| `cwa.geojson` | `awc-cwas` | **Synthetic**, from AWC's OpenAPI `CwaGeoJSON` (epoch-second validity) plus one feature with text times: no CWA was in force at capture (an empty collection; `format=json` answered 204). |
| `pirep.geojson` | `awc-pireps` | Real, `/api/data/pirep?format=geojson&age=2&bbox=20,-130,55,-60` (lat-first CONUS): 88 reports. The same box longitude-first returned an empty collection, and no box answered 400. |
| `taf.json` | `awc-tafs` | Real, `/api/data/taf?ids=KAUS,KDFW,KJFK,EGLL&format=json`. |
| `tafs.cache.xml.gz` | `awc-tafs` | Real `/data/cache/tafs.cache.xml.gz` (2,924 TAFs, 340,154 bytes, `ETag "6acf0653-530ba"`, `Last-Modified`, `max-age=30`), **trimmed** to the 86 TAFs in the Texas box plus KJFK, EGLL, SBGR and PANC (90) and re-gzipped. |

Captured on 2026-10-14 between 04:33 and 04:35 UTC with
`curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)"`, two seconds apart. The API pages
(aviationweather.gov/data/api) state the 100 requests/minute limit and ask for a custom User-Agent.
