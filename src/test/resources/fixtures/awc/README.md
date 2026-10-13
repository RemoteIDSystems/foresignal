# Fixtures: `awc-metars` (prompt 047)

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
