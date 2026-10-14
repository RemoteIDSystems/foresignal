# cdc-nwss-wval fixtures

`wval.json` — a trimmed CDC NWSS site viral-activity-level dataset (`atcp-73re`) response.

- URL: `https://data.cdc.gov/resource/atcp-73re.json?$limit=5000&$offset=0`
- Captured: synthetic — **synthetic: true — recapture with a real capture per prompt 143**.
- Method: hand-built from the dataset's published columns; four rows covering a CDC row, a row with a
  non-integer `wval` (rounded to one decimal), a `WastewaterSCAN` row (CC BY-NC 4.0), and a row with no
  `wval`. No network call was made during the build.
- The older datasets `2ew6-ywp6` and `g653-rqe2` were archived on 2025-09-12 and are not used.
