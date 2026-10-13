# cctv-drivebc fixtures (prompt 072)

- `webcams.json`: captured 2026-10-13 17:17 UTC from `https://www.drivebc.ca/api/webcams/` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 1,240,361 bytes, 1067 webcams (27 with `is_on=false`); `ETag: W/"ebdd6a6dea3a9bab1334609a4138eca9"`.
  Trimmed to 21 switched-on cameras without a credit, one TransLink-credited camera, one with a solar-power note, and 2 switched-off cameras; rows unchanged.

Frame-access probe: `https://www.drivebc.ca/images/1006.jpg` → 200 `image/jpeg`, `Access-Control-Allow-Origin: *` → `direct`.
`update_period_mean` is a measured mean, not a published refresh interval, and is not read.
