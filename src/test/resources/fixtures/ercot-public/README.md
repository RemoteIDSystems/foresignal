# `ercot-public` fixtures

Synthetic: built by hand from the ERCOT Public API report envelope `{_meta, report, fields[], data[][]}` (the repo
`github.com/ercot/api-specs`, `pubapi/pubapi-apim-api.json`, verified 2026-10-14); recapture with a registered
account.

- `load.json` — `np6-345-cd/act_sys_load_by_wzn`, two hourly rows with one column per weather zone.
- `spp.json` — `np6-905-cd/spp_node_zone_hub`, two 15-minute HB_HOUSTON rows.

The adapter reads columns by `fields[].name`, never by position. Registration:
<https://developer.ercot.com/applications/pubapi/>; access is blocked from outside the United States.
