# readsb mapper fixtures

| File | What it is |
|---|---|
| `readsb-edge.json` | Hand-made, local readsb shape (`aircraft`, not `ac`) with `now` in milliseconds |

Entries, in order: on the ground with `emergency: "none"` and every excluded counter (`messages`, `rssi`,
`rc`, `version`, `sil`, `gva`, `sda`, `nav_qnh`); an entry without `hex` (skipped); a `~` TIS-B trackfile
whose `tisb` list names `lat`; an MLAT entry with squawk 7700, emergency `general` and `dbFlags: 10`
(LADD + interesting); a Mode S entry with only `lastPosition` and `dbFlags: 5` (military + PIA); a Mode S
entry with no position at all; an ADS-B entry at the `navDegraded` boundary (`nac_p: 7`, `nic: 7`) with
`dbFlags: 8`.

Values follow the readsb field reference (`https://github.com/wiedehopf/readsb/blob/dev/README-json.md`)
and the adsb.lol v2 response model; no upstream was called.
