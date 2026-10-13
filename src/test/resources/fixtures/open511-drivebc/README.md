# open511-drivebc fixtures (prompt 074)

- `events.json`: captured 2026-10-13 18:40 UTC from
  `https://api.open511.gov.bc.ca/events?format=json&status=ACTIVE&limit=500` with
  `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, `Content-Encoding: gzip`, no `ETag` or `Last-Modified`; 785,871 bytes decompressed, 263 events in one page
  (`pagination` held only `offset`; DriveBC sent no `next_url` even with `limit=5`).
  Trimmed to 25 whole events with the envelope kept: every `INCIDENT` and `ROAD_CONDITION` type, five events with a
  `CLOSED` road, the `MAJOR` ones, events with several and with open-ended schedule intervals, then the first others.
  On capture day the schedule intervals were UTC without a zone (an event `created` at 11:02 PDT starts at
  `18:02`), so the parser reads them as UTC.
- `edge-cases.json`: hand-made. An `ARCHIVED` event, events without `id`, without `updated` and with a broken
  geometry, several intervals out of order, an open-ended last interval, an unknown type and severity, the dropped
  `+ivr_message` and `+linear_reference_km` extensions, and a relative `pagination.next_url`.

License: Open Government Licence – British Columbia (`https://api.open511.gov.bc.ca/help`, checked 2026-10-13).
Other Open511 servers: `api.511.org` (San Francisco Bay Area) answers `401` without a key and serves events on
`/traffic/events`, not the standard `/events` route, so it was not added. No other live server answering the standard
route with published terms was found.
