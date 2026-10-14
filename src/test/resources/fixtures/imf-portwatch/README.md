# Fixtures: `imf-portwatch` (prompt 088)

Real captures of the five IMF PortWatch layers under
`https://services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services/`.

| File | What it is |
|---|---|
| `chokepoints-layer.json` | `PortWatch_chokepoints_database/FeatureServer/0?f=json`: points, `maxRecordCount` 1000, pagination, `editingInfo.dataLastEditDate` 1777549336329. |
| `chokepoints-page.json` | The full query page: all 28 chokepoints (`where=1=1`, `outFields=*`, GeoJSON). |
| `daily-chokepoints-layer.json` | `Daily_Chokepoints_Data/FeatureServer/0?f=json`: a table; `date` is `esriFieldTypeDateOnly`; edited 1791288541457 (2026-10-13, a Tuesday). |
| `daily-chokepoints-page.json` | The query `where=date > DATE '2026-09-14'`, ordered by `date,ObjectId` (560 rows, 2026-09-15 to 2026-10-04), trimmed to the last two days (56 rows). |
| `ports-layer.json` | `PortWatch_ports_database/FeatureServer/0?f=json`: points, 2,065 ports (`returnCountOnly`), edited 1789742026415. |
| `ports-page.json` | The first query page (1,000 ports, `exceededTransferLimit` true) trimmed to its first 20 ports. |
| `daily-ports-layer.json` | `Daily_Ports_Data/FeatureServer/0?f=json`: a table, edited 1791312087589. |
| `daily-ports-page.json` | The query `where=date > DATE '2026-09-30'` (first page of 1,000) trimmed to the 40 rows of the 20 ports above. |
| `disruptions-layer.json` | `portwatch_disruptions_database/FeatureServer/0?f=json`: polygons, `maxRecordCount` 2000, edited 1786789106191. |
| `disruptions-page.json` | The full query (132 events, 6.2 MB) trimmed to the three with the smallest geometries: HORMUZ-26 (no `todate`), US PORTS STRIKE and the Francis Scott Key Bridge collapse; `htmldescription` kept, to show it is not mapped. |

## Capture procedure

- **Date:** 2026-10-14 05:25 UTC, User-Agent `Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)`.
- **Commands** (`B=https://services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services`):

  ```sh
  curl -sS -A "$UA" -o meta.json "$B/{layer}/FeatureServer/0?f=json"
  curl -sS -A "$UA" -G "$B/{layer}/FeatureServer/0/query" --data-urlencode "where={where}" \
    --data-urlencode "outFields=*" -d returnGeometry=true -d outSR=4326 -d f=geojson -d resultOffset=0 \
    -d resultRecordCount=1000 --data-urlencode "orderByFields=ObjectId"
  ```

  The chokepoint daily query used `orderByFields=date,ObjectId`.

**Cadence:** the daily tables' newest day was 2026-10-04 and they were last edited on a Tuesday: weekly updates
with a lag of about ten days.

**Terms:** the PortWatch FAQ's footer links "Copyright & Usage" to `https://www.imf.org/external/terms.htm`, which
now redirects to an IMF 404 page; the current terms are at `https://www.imf.org/en/About/copyright-and-terms`
("Special Terms for Statistical Data": reuse with attribution to the IMF as the source). That URL is the sources'
`termsUrl`.
