# cdc-nwss-detections fixtures

`detections.json` — a trimmed CDC NWSS detections dataset response.

- URL: `https://data.cdc.gov/resource/mtpu-urpp.json?$order=date_updated%20ASC&$limit=5000&$offset=0`
- Captured: synthetic — **synthetic: true — recapture with a real capture per prompt 143**.
- Method: hand-built from the published columns. Four rows cover an older and a newer sample of the
  same site and target (the newer must win), a `"No"` string detection, and a `WastewaterSCAN` row
  (CC BY-NC 4.0). Dataset ids: h5 `mtpu-urpp`, measles `akvg-8vrb`, mpox `xpxn-rzgz`,
  Influenza A `ymmh-divb`, RSV `45cq-cw4i`, SARS-CoV-2 `j9g8-acpt`.
