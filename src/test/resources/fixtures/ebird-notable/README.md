# ebird-notable fixtures

`notable.json` — a trimmed eBird API 2.0 `/v2/data/obs/geo/recent/notable` response.

- URL: `https://api.ebird.org/v2/data/obs/geo/recent/notable?lat=30.27&lng=-97.74&dist=25&back=3&detail=simple`
- Captured: synthetic — **synthetic: true — recapture with a real key per prompt 143**.
- Method: hand-built from the eBird API 2.0 documentation
  (`https://documenter.getpostman.com/view/664302/S1ENwy59`) and the terms page. No network call was made
  during the build. Three sightings: one public location, one private location (name and exact
  coordinates must not be stored), and one with no `howMany`.
