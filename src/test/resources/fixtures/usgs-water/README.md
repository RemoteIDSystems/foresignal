# usgs-water fixtures

Captured 2026-10-13 22:10 UTC, anonymously (no `X-RateLimit-*` headers were sent back).

- `latest-page1.json`: `GET https://api.waterdata.usgs.gov/ogcapi/v1/collections/latest-continuous/items?f=json&limit=4&parameter_code=00060,00065&bbox=-105.5,39.5,-104.5,40.2`
  (four features; `links[rel=next]` carries `cursor=MDUxNGViMGItY2EzMi00NzJkLWEzOTYtZjJhYWQwYWE2NzU0`). Tests rewrite
  the absolute `https://api.waterdata.usgs.gov` links to the stub server.
- `latest-page2.json`: the `next` link of page 1, as captured, with its own `next` link removed so paging ends here.
  It holds the two `SEASONAL` readings with a null `value` and a 1996 reading of a gravel pit (`LK`).
- `monitoring-locations.json`: `GET …/collections/monitoring-locations/items?f=json&limit=1000&id={the 8 sites}&properties=monitoring_location_name,site_type_code,state_name&skipGeometry=true`.

A non-numeric `value` (`Ice`, `Eqp`) did not occur in October; the parser test builds that case inline. The legacy
`waterservices.usgs.gov/nwis/iv` service is not used. Recapture per prompt 143.
