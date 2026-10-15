# faa-registry fixtures

`faa-registry` (prompt 098) downloads `https://registry.faa.gov/database/ReleasableAircraft.zip`
(about 60 MB, refreshed daily) and parses `MASTER.txt` joined to `ACFTREF.txt`.

- **Archive layout:** comma-delimited with a header row, trailing spaces, and a trailing empty
  column. `ACFTREF.txt` maps an `MFR MDL CODE` to a manufacturer, model and type.
- **Privacy:** owner `NAME`, `STREET`, `CITY`, `STATE`, `ZIP CODE`, `REGION`, `COUNTY`, `COUNTRY`,
  `OTHER NAMES(1–5)` and `UNIQUE ID` are never stored. Only a corporation, government, LLC or
  non-citizen-corporation name is kept, and only when the `nonIndividualOwnerNames` parameter is on.
- **Sample:** synthetic — hand-made rows with the real column names to exercise the parser's privacy
  guard, the `airframe`/`registration`/`icao24` keys and zip-slip rejection. No real archive is
  committed (60 MB).

synthetic: true — recapture a trimmed real slice (30 MASTER rows and the referenced ACFTREF codes)
once, re-zipped, and record the capture date here per prompt 143.
