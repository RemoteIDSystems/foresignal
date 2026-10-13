# ioda-outages fixtures (prompt 068)

Captured on 2026-10-13 at 15:18 UTC (no key needed; `synthetic: false` for the captured parts). Window: 12 h,
`from=1791861505`, `until=1791904705`.

```sh
NOW=1791904705
curl -s "https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from=$((NOW-43200))&until=$NOW&limit=500"          # page 0: 500 rows
curl -s "https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from=$((NOW-43200))&until=$NOW&limit=500&page=1"   # 186 rows
curl -s "https://api.ioda.inetintel.cc.gatech.edu/v2/outages/alerts?from=$((NOW-43200))&until=$NOW&limit=500&page=2"   # 0 rows
```

What the capture showed:
- The envelope is `{type: "outages.alerts", metadata, requestParameters, error: null, perf: null, data: [...],
  copyright: "This data is Copyright (c) 2021-2025 Georgia Tech Research Corporation. All Rights Reserved."}`.
  The response sent `Cache-Control: no-cache, private` and no `ETag` or `Last-Modified`.
- Pages are numbered from 0: `page=0` returned exactly the rows of the request without `page`. Rows come in
  ascending `time`.
- 686 alerts in the window: entity types `geoasn` (241 on page 0), `asn` (180), `region` (63), `country` (16);
  datasources `bgp`, `ping-slash24`, `merit-nt`, `gtr`; levels `critical` and `normal` only; methods `median`
  and `sarima`. `value` and `historyValue` were integers in every alert.
- Country codes are ISO 3166-1 alpha-2 (`SS`, `CD`, `HT`, `NF`, ...). Region codes are NetAcuity ids (`497` for
  Espírito Santo) with `attrs.country_code`, `country_name` and `ne_region_id`.
- `entityType=country` returned only country entities. `entityCode=BR` (with or without `entityType=country`)
  matched only the entity whose code is `BR`, never Brazil's regions. An unknown `entityType` returned 200 with
  an empty `data` array.

| File | What it is |
|---|---|
| `alerts-12h.json` | The page-0 envelope, verbatim, with `data` replaced by 40 alerts taken unmodified from pages 0 and 1 and sorted by time: 13 country, 16 region, 8 asn and 3 geoasn alerts; 19 bgp, 15 ping-slash24, 4 merit-nt and 2 gtr; 19 critical and 21 normal. Region `497` (Espírito Santo, merit-nt) appears four times, critical and normal in turn. |
| `critical-then-normal.json` | Hand-made: one key, `country:HT:bgp`, critical at 1791873600 (574 / 658, `< 0.99`) and normal at 1791876000 (658 / 658). |
| `unknown-region.json` | Hand-made: region `999999` "Atlantis Province" in `GR`, which no admin-1 table holds, with `historyValue` 0. |
| `not-envelope.html` | Hand-made: an nginx 502 page served with status 200, which is not the JSON envelope. |

`page-full-500.json` is not committed: `IodaAdapterTest` generates full pages of 500 alerts.
