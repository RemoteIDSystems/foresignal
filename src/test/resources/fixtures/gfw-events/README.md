# Fixtures: `gfw-events` (prompt 088)

**synthetic: true.** No Global Fishing Watch token was available (Conventions, "No credentials during construction"),
so every file is the example response printed in GFW's API documentation, unchanged. **Recapture with a real key**
at 143 (`FORESIGNAL_VERIFY_*`), one page per dataset, and check that `vessel.flag` is present (the examples show it
only on an encounter's other vessel).

| File | Source page (GFW API documentation, read 2026-10-14) |
|---|---|
| `fishing-events.json` | `docs/examples/events/get-example1` (GET, `public-global-fishing-events:latest`): one `fishing` event, `total` 10, `nextOffset` 1. |
| `encounters-events.json` | `docs/examples/events/get-example2` (`public-global-encounters-events:latest`): one `encounter` with `encounter.vessel.ssvid`. |
| `loitering-events.json` | `docs/examples/events/get-example3` (`public-global-loitering-events:latest`): one `loitering` event. |
| `port-visits-events.json` | `docs/examples/events/get-example4` (`public-global-port-visits-events:latest`): one `port_visit` with its anchorages. |
| `fishing-events-region.json` | `docs/examples/events/post-example1` (POST with a custom `geometry` polygon): one `fishing` event, `total` 2480. |

The pages were read as Markdown (`…/docs/examples/events/get-example1.md`; the documentation's `llms.txt` says every
page is available that way). The tests copy the single entry to build longer pages.

An unauthenticated request (`GET /v3/events?datasets[0]=…`) answers HTTP 401 `{"error":"invalid token"}`.
