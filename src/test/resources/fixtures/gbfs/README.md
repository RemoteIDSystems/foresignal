# GBFS fixtures (prompt 071)

Recorded GBFS feeds for the `gbfs-*` tests. Each capture was a plain GET with
`User-Agent: Foresignal-fixture-capture/0.1 (+https://foresignal.ai)`, taken 2026-10-13 between 16:46 and 16:52 UTC,
then trimmed to the first 25 `station_status` entries whose `station_id` is also in `station_information`, with
`station_information` cut to the same 25 ids. Nothing else was edited (the trimmed files are re-indented JSON).

| Directory | System (`gbfs-{id}`) | GBFS | URLs (`station_information.json`, `station_status.json` under) |
|---|---|---|---|
| `citibike/` | Citi Bike (`nyc-citibike`), Lyft-operated | 2.3 | `https://gbfs.lyft.com/gbfs/2.3/bkn/en/` |
| `boulder-bcycle/` | Boulder BCycle (`boulder-bcycle`) | 1.1 | `https://gbfs.bcycle.com/bcycle_boulder/` |
| `austin-capmetro/` | CapMetro (`austin-capmetro`), publicbikesystem.net | 2.3 | `https://austin.publicbikesystem.net/customer/gbfs/v2/en/` |
| `austin-capmetro-v3/` | the same system's 3.0 feed (not in the reviewed list) | 3.0 | `https://austin.publicbikesystem.net/customer/gbfs/v3.0/` |
| `synthetic/` | hand-made (`synthetic: true`) | 1.1, 2.3, 3.0 | none |

The `citibike/` status keeps eleven Lyft placeholder stations with `last_reported: 86400` (not installed, never
reported); the parser treats a time before 2000-01-01 as missing, so they are dropped and counted. In the full Citi
Bike feed that day 54 of 2519 stations carried it.

## Licenses (`system_information.json`)

`system_information.json` was read for every system of the reviewed list on 2026-10-13. **None declares
`license_id` or `license_url`** (or `attribution_organization_name`): the Lyft, BIKETOWN and publicbikesystem.net
feeds publish GBFS 2.3 without them, and the BCycle feeds (1.1) and Reddy's (1.0) predate the fields. The austin
3.0 `system_information` declares none either. Every reviewed entry therefore carries `license = "operator terms"`
and the operator's data page, or its home page where it publishes none (see `GbfsSystems`).

## Hand-made files (`synthetic/`)

| File | What it exercises |
|---|---|
| `information.json` | 2.3 information for S1, S2, a station without a position (S3) and one at 0/0 (S4) |
| `status-only-station.json` | S1 plus S9, which `information.json` does not list |
| `bool-as-int.json` | GBFS 1.1 booleans as `0/1` and as strings, `last_reported` as a numeric string, BCycle's `num_bikes_available_types` |
| `ttl-future.json` | `ttl: 600` with `last_updated` 1791910000 (2026-10-13T16:46:40Z), so the next status request is skipped |
| `missing-last-reported.json` | one entry without `last_reported`, one with Lyft's `86400` placeholder |
| `localized-name-v3.json` | 3.0 information with localized names: the requested language present, absent (first entry), and only as a regional tag (`en-CA`) |

The redirect case is a WireMock `302` stubbed in `GbfsAdapterTest`, not a file.

## Recapture

`curl -s -A "<Foresignal UA>" -o station_information.json <base>station_information.json` (and the same for
`station_status.json` and `system_information.json`), then keep 25 matching ids as above. Recapturing changes ids,
counts and times, so the tests that name stations must be updated with it.
