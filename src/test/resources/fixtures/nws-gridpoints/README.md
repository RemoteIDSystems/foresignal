# Fixtures: `nws-gridpoints` (prompt 053)

Real `api.weather.gov` responses, captured 2026-10-12 23:43 UTC with an identifiable User-Agent:

```sh
UA="Foresignal fixture capture (+https://foresignal.ai)"
curl -sS -A "$UA" -H "Accept: application/geo+json" -o points.json "https://api.weather.gov/points/37.6856,-117.3638"
curl -sS -A "$UA" -H "Accept: application/geo+json" -o grid.json  "https://api.weather.gov/gridpoints/VEF/55,177"
```

The point (central Nevada, office VEF) was chosen because its gridpoint carried a hazard (a Freeze Warning) at
capture time; a Dallas gridpoint (`FWD/89,104`) had none.

| File | What it is |
|---|---|
| `points-VEF-55-177.json` | `/points/37.6856,-117.3638`, re-indented: `gridId` VEF, `gridX` 55, `gridY` 177, `forecastGridData` `https://api.weather.gov/gridpoints/VEF/55,177`, `timeZone` America/Los_Angeles. `Cache-Control: public, max-age=86399`. |
| `gridpoints-VEF-55-177.json` | `/gridpoints/VEF/55,177`, **trimmed to the properties the adapter reads** (`updateTime`, `validTimes`, `elevation`, `forecastOffice`, `gridId`, `gridX`, `gridY`, `temperature`, `windSpeed`, `windGust`, `windDirection`, `probabilityOfPrecipitation`, `quantitativePrecipitation`, `skyCover`, `hazards`) from 249 KB to 47 KB; values unchanged. `updateTime` 2026-10-12T19:46:56+00:00, `validTimes` 2026-10-12T13:00:00+00:00/P7DT12H. |

Response headers seen on `/gridpoints`: `Last-Modified: Mon, 12 Oct 2026 19:46:56 GMT`,
`Cache-Control: public, max-age=3600, s-maxage=3600`, no `ETag`. A repeat with `If-Modified-Since` set to the
`Last-Modified` answered **304** (checked on `FWD/89,104`).

Units: `temperature` `wmoUnit:degC`, `windSpeed`/`windGust` `wmoUnit:km_h-1`, `windDirection`
`wmoUnit:degree_(angle)`, `probabilityOfPrecipitation`/`skyCover` `wmoUnit:percent`,
`quantitativePrecipitation` `wmoUnit:mm`. `hazards` values: `[{phenomenon: FZ, significance: W, event_number:
null}]` for `2026-10-13T07:00:00+00:00/PT10H`, and an empty list for the next night.
