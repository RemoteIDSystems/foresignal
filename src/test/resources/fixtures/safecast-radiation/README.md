# safecast-radiation fixture

- `devices.json`: `GET https://tt.safecast.org/devices`, captured 2026-10-13 21:31 UTC (2,086 devices, 1.4 MB),
  trimmed to 19 entries of 18 devices:
  - LND 7318u tubes (`geigiecast-zen:65025`, `65079`, `65109`), 7318c Radnotes (`note:dev:860264050368886`,
    `…490136`, `…723098`), pointcasts with both 7128ec and 7318u (`pointcast:10012`, `10022`, `10036`) and Airnotes
    with a 712u tube (`note:dev:864475044186905`, `…208576`);
  - `note:dev:863740067201399` twice (a capture of 2026-10-03 and one of 2026-10-13: the list repeats devices);
  - `ngeigie:1` without `loc_lat`/`loc_lon`, `geigiecast:61099` with `when_captured` `2012-00-00T00:00:00Z`, and
    `safecast:1759788014` without `when_captured`;
  - three air-only Airnotes without any `lnd_*` field (`note:dev:863740067197266`, `…197423`, `…197506`).
- The `device_contact_name`, `device_contact_email` and `device_contact_org` members (present on some devices)
  were removed; nothing else was changed.

Recapture per prompt 143 (and remove the contact members again).
