# radio-browser fixtures (prompt 075)

Captured 2026-10-13 about 19:35 UTC with `curl -s -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai)" <url>`.

- `servers.json`: `https://all.api.radio-browser.info/json/servers`; HTTP 200. Only `de1.api.radio-browser.info`,
  once per address family (IPv4 and IPv6). Unchanged.
- `stations-page.json`: `https://de1.api.radio-browser.info/json/stations/search?has_geo_info=true&hidebroken=true
  &order=stationuuid&limit=1000&offset=0`; HTTP 200, 1000 rows. `order=stationuuid` is not a documented value (the
  docs list name, url, homepage, favicon, tags, country, state, language, votes, codec, bitrate, lastcheckok,
  lastchecktime, clicktimestamp, clickcount, clicktrend, changetimestamp, random) and the rows came back sorted by
  name, so the adapter sends `order=name`. **Edited:** trimmed to the first 25 rows (tab- and space-prefixed names
  sort first).
- `edge-cases.json` (hand-made): a tab-prefixed name at (0,0) with every excluded counter present, an invalid UUID,
  a row without `lastchangetime_iso8601`, an out-of-range latitude and a repeated id.

License statement (`https://api.radio-browser.info/`, read 2026-10-13): "This API … is completely free and open
source. Your freedoms are: You may use it in free and non free software. You may install it on your own server and
mirror all its data." The docs page also asks for "a descriptive User-Agent".
