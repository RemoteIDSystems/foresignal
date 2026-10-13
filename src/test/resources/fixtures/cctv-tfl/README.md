# cctv-tfl fixtures (prompt 072)

- `jamcam.json`: captured 2026-10-13 17:17 UTC from `https://api.tfl.gov.uk/Place/Type/JamCam` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>` (no app key).
  HTTP 200, 1,146,691 bytes, 890 places (800 `available=true`), no `ETag` or `Last-Modified`.
  Trimmed to the first 22 available and the first 2 unavailable places, unchanged otherwise (re-indented JSON).
- `edge-cases.json`: hand-made (`synthetic: true`): a place without an id, an unavailable camera, an image on another S3 bucket, a camera without a position, a duplicate id.

Frame-access probe (HEAD, Foresignal User-Agent, no Referer): `https://s3-eu-west-1.amazonaws.com/jamcams.tfl.gov.uk/00002.00865.jpg` → 200 `image/jpeg` → `direct`.
The `view` property is mostly a free-text label; only labels that are wholly a compass direction (`West`) become `direction` and `headingDeg`.
