# nhc-cyclones fixtures (prompt 067)

Captured on 2026-10-13 at 14:48 UTC (no key needed; `synthetic: false` for the captured file).

```sh
curl -s https://www.nhc.noaa.gov/CurrentStorms.json > CurrentStorms.json
```

The response had `etag: "135b-65db9b49d4134"`, `last-modified: Tue, 13 Oct 2026 14:34:00 GMT` and
`cache-control: max-age=300` (CloudFront).

| File | What it is |
|---|---|
| `CurrentStorms.json` | Captured, unmodified: one in-season storm, Hurricane Simon (`ep202026`, `EP5`), with `intensity` `"80"` and `pressure` `"977"` as strings, `latitudeNumeric` 20.4 / `longitudeNumeric` -107.2, `movementDir` 285, `movementSpeed` 3 (mph), `lastUpdate` 2026-10-13T12:00Z, forecast advisory `"024"` issued 09:00Z, public advisory `"024a"`, several null GIS products. |
| `empty.json` | Hand-made: `{"activeStorms":[]}`, the out-of-season file. |
| `string-numbers.json` | Hand-made: numerics as strings (`"55"`, `"1001"`, `"315"`, `"10"`), as numbers, and unreadable (`"n/a"`, `""`, `"abc"`); a name with `<script>` and a control character; a `cp` storm; an advisory `"012A"`; a non-NHC forecast advisory URL, an `http:` public advisory URL and a URL with a query (all dropped); an unknown classification `XX`; an invalid `advNum` and `issuance`; a null `publicAdvisory`. |
| `null-products.json` | Hand-made: one post-tropical storm whose every product object is null. |
| `invalid-id.json` | Hand-made: a storm id `wp012026`, which rejects the whole document. |
