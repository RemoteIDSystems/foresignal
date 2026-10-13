# Vendored Mobility Database GTFS-RT snapshot (prompt 070)

`mobility-gtfs-rt.csv` is a filtered copy of MobilityData's catalog export
`https://files.mobilitydatabase.org/feeds_v2.csv` (schema:
`https://github.com/MobilityData/mobility-feed-api/blob/main/docs/SpreadsheetSchemaV2.md`).

- **Snapshot date:** 2026-10-13
- **Rows:** 1350
- **License of the metadata:** CC0 1.0 (MobilityData publishes the catalog metadata under CC0). Each feed's own data
  keeps its own license, given in `urls.license` when the catalog knows it; an operator checks it before enabling a feed.
- **Filter:** `data_type = gtfs_rt`, `status = active`, `urls.authentication_type = 0` (no key) and an `https`
  `urls.direct_download`.
- **Columns:** `id, provider, location.country_code, entity_type, urls.direct_download, urls.license, static_reference`.
- **Regenerate:** `python3 scripts/update-mobility-catalog.py` (development only). Foresignal never downloads the catalog
  at runtime: `MobilityCatalog` reads this file, and `foresignal.transit.catalog-feeds` enables feeds from it by id,
  so a transit URL never comes from an API caller.
- The legacy `bit.ly/catalogs-csv` / `sources.csv` (with `mdb_source_id`) is deprecated and is not used.
