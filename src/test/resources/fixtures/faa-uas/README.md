# Fixtures: `faa-uas-*` feature-kit sources (prompt 087)

For each FAA UAS restriction layer under `https://services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/`,
`{Service}-layer.json` is the layer document (`…/FeatureServer/0?f=json`) and `{Service}-page.json` its first three
features (`where=1=1&outFields=*&outSR=4326&f=geojson&resultRecordCount=3&orderByFields=OBJECTID`), captured on
2026-10-14 at 04:37 UTC with `curl -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)"`.

| Service | Source | Count | Id field | Notes |
|---|---|---|---|---|
| `DoD_Mar_13` (layer 0 `National_Security_UAS_Flight_Restrictions`) | `faa-uas-nsufr` | 2,284 | `OBJECTID` | No GlobalID; `FAA_ID` repeats for 10 rows. `Floor` is "Surface" on every row; `Ceiling` is text: "400' AGL" 2,217, "2,000' MSL" 57, "2,000' AGL" 3, "2,500' AGL" 4, "6,500' AGL" 1, "8,000' AGL" 2. `maxRecordCount` 1000. |
| `Part_Time_National_Security_UAS_Flight_Restrictions` | `faa-uas-nsufr-parttime` | 5 | `OBJECTID` | Same fields plus the alert fields. |
| `Recreational_Flyer_Fixed_Sites` | `faa-uas-recreational-sites` | 2,172 | `GLOBALID` (GUID) | `UNIT` is "Feet" on every row. |
| `Stadiums` | `faa-uas-stadiums` | 204 | `GLOBAL_ID` (text) | Points. |
| `Prohibited_Areas` | `faa-uas-prohibited` | 13 | `GLOBAL_ID` (text) | |
| `National_Defense_Airspace_TFR_Areas` | `faa-uas-defense-tfr` | 8 | `GLOBAL_ID` (text) | The page was captured with `maxAllowableOffset=0.002&geometryPrecision=5` to keep it small (the full first polygon is 243 KB). |

The `exceededTransferLimit` flags are left as served.
