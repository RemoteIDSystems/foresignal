# epa-radnet fixtures

- `CO_DENVER_2026.csv`: `GET https://radnet.epa.gov/cdx-radnet-rest/api/rest/csv/2026/fixed/CO/DENVER`, captured
  2026-10-13 21:33 UTC (614,783 bytes, 6,323 rows, `Content-Type: text/text`, no `ETag`), cut to the header and the
  last 50 rows. The dose column is empty on every row. Only the header line ends in CRLF, as captured.
- `HI_HONOLULU_2026.csv`: `…/2026/fixed/HI/HONOLULU`, captured 2026-10-13 21:34 UTC, header and last 50 rows; the
  dose column is filled.
- `GU_HAGATNA_2026.csv`: `…/2026/fixed/GU/HAGATNA` the same day: the header only (a station-year without data).
- Time base: the newest rows of Honolulu (20:19), Anchorage (20:41), Denver (20:41), San Francisco (19:48), Boston
  (20:18) and San Juan (20:12), fetched at 21:34 UTC, show that `SAMPLE COLLECTION TIME` is UTC.

Recapture per prompt 143.
