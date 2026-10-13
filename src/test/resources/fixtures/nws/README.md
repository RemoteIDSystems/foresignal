# Fixtures: `nws-alerts` (prompt 047)

| File | What it is |
|---|---|
| `national.json` | A real capture of the national active-alerts feed, untrimmed: 217 alerts (148 `Alert`, 69 `Update`), 16 with a polygon, 201 without. 46 have an `expires` already in the past while `ends` is ahead; 40 have `onset` after `expires`. |
| `area-TX.json` | A real capture of `area=TX`: a Coastal Flood Advisory (`Update`, zones only, no geometry) and a Flood Warning (polygon, county UGCs). |
| `edge-cases.json` | **Synthetic**, built by a script from the two `area-TX.json` alerts: a polygon alert; the same alert with the geometry removed and county-only UGCs/SAME (`048199`, `048241`) for the county union; a zone-only alert; `test.old.1` plus an `Update` that references it; a `Cancel` that references `test.old.2` (not in the feed); a Severe Thunderstorm Warning with an 11,600-byte description and `maxHailSize`, `maxWindGust`, `tornadoDetection`, `thunderstormDamageThreat`, `eventMotionDescription`; an alert with `onset` after `expires` and no `ends`; one with `expires` before `ends`; and a `Minor` alert without `onset`. |

## Capture procedure

- **Date:** 2026-10-13 00:01 UTC (`date` header; the feed's `updated` was `2026-10-13T00:00:00+00:00`).
- **Commands** (an identifiable User-Agent with a contact; never a browser string):

  ```sh
  UA="Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)"
  curl -sS -A "$UA" --compressed -H "Accept: application/geo+json" -o national.json "https://api.weather.gov/alerts/active?status=actual"
  curl -sS -A "$UA" --compressed -H "Accept: application/geo+json" -o area-TX.json "https://api.weather.gov/alerts/active?status=actual&area=TX"
  ```

- **Response:** HTTP 200, `content-type: application/geo+json`, `content-encoding: gzip`,
  `cache-control: public, max-age=5, s-maxage=5`, a weak `etag` (not honored on a repeat, per the
  2026-10 verification), no pagination. `national.json` is 943,463 bytes inflated.

When recapturing, also capture during active convection if possible: the 2026-10-13 feed had no
county-only alert without a polygon and none with the storm parameters, which is why `edge-cases.json`
exists.
