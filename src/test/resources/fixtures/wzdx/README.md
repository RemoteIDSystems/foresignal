# WZDx fixtures (prompt 074)

Captured 2026-10-13 18:41 UTC with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`, each trimmed
to whole features with the feed envelope kept:

| File | URL | Version | Original | Kept |
|---|---|---|---|---|
| `wsdot-4.2.json` | `https://wzdx.wsdot.wa.gov/api/v4/WorkZoneFeed` | 4.2 (`feed_info` and `road_event_feed_info`) | 1,127,910 bytes, 545 features under 539 ids | 20, including both entries of the repeated id `40583-S` |
| `udot-4.0.json` | `https://udottraffic.utah.gov/wzdx/udot/v40/data` | 4.0 (`road_event_feed_info` only) | 617,492 bytes, 744 features under 171 ids | 20, including both entries of the repeated id `1217689248` |
| `nddot-4.0.json` | `https://travelfiles.dot.nd.gov/geojson_nc/wzdx_geojson.json` | 4.0 | 274,789 bytes, 82 features | 15, with `some-lanes-closed` and `all-lanes-open` impacts and 3D coordinates |
| `quebec-3.1.json` | `https://quebec.gewi.com/wzdx/pull` | 3.1 (refused) | 855,488 bytes, 797 features | 3 |

UDOT's feed-info contact name and e-mail address were replaced with `redacted` / `redacted@example.invalid` before the
fixture was committed (personal data; the parser never reads them).

Hand-made: `edge-4.2.json` (a MultiPoint detour with all lanes closed, a numeric id, a restriction with a reduced speed
limit and a 4.2 `name`, a `completed` event, a feature without `update_date`, one without `id`, an exact repeat, and
unknown codes) and `cwz-1.0.json` (a CWZ 1.0 document, refused).

The vendored registry snapshot `src/main/resources/roads/wzdx-feeds.json` was taken the same day from
`https://data.transportation.gov/resource/69qe-yiui.json` (43 rows; registry rows last updated 2026-09-10 19:54 UTC).
