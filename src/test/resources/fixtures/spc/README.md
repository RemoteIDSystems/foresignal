# Fixtures: `spc-outlooks` (prompt 047)

The 17 layer files the adapter fetches, captured untrimmed:

- `day1otlk_{cat,torn,wind,hail,sigtorn,sigwind,sighail}.lyr.geojson`
- `day2otlk_{cat,torn,wind,hail,sigtorn,sigwind,sighail}.lyr.geojson`
- `day3otlk_{cat,prob,sigprob}.lyr.geojson`

Notable content on the capture date: day 1 had only a TSTM area (its probability layers are the empty
placeholder, one feature with an empty `GeometryCollection` and label `Less Than 2% All Areas` or empty);
day 2 had TSTM, MRGL and SLGT with 2 % tornado and 5/15 % wind and hail; day 3 had TSTM, MRGL and 5 %
any-severe. `day1otlk_sigtorn` is a placeholder dated 2026-03-03, and `day3otlk_sigprob` still holds a
**March 2026 polygon** (`EXPIRE` 202603061200), so the parser skips features that expired before the fetch.

## Capture procedure

- **Date:** 2026-10-13 00:01 UTC (day 1 issued 2026-10-12 19:48 UTC).
- **Commands:**

  ```sh
  UA="Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)"
  for d in 1 2; do for t in cat torn wind hail sigtorn sigwind sighail; do
    curl -sS -A "$UA" -o day${d}otlk_$t.lyr.geojson "https://www.spc.noaa.gov/products/outlook/day${d}otlk_$t.lyr.geojson"; done; done
  for t in cat prob sigprob; do
    curl -sS -A "$UA" -o day3otlk_$t.lyr.geojson "https://www.spc.noaa.gov/products/outlook/day3otlk_$t.lyr.geojson"; done
  ```

- **Response:** HTTP 200, `content-type: application/geo+json`, an `etag` and `last-modified` on every
  layer. `day3otlk_torn.lyr.geojson` answered **404** (day 3 has no torn/wind/hail layers).
