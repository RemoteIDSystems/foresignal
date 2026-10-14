# inat-observations fixtures

`observations.json` — a trimmed iNaturalist v1 `/v1/observations` response.

- URL: `https://api.inaturalist.org/v1/observations?swlat=30&swlng=-98&nelat=31&nelng=-97&quality_grade=research&order_by=id&order=asc&per_page=200`
- Captured: synthetic — **synthetic: true — recapture with a real key per prompt 143**.
- Method: hand-built from the v1 API documentation (`https://api.inaturalist.org/v1/docs/`) and the
  `swagger.json` field list; trimmed to seven observations covering the parser's branches. No network
  call was made during the build.
- Note: iNaturalist asks that its API not be used for scraping; this fixture is a single trimmed
  response, not a corpus.
