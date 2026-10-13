# local-receivers fixtures (prompt 069)

**Hand-made, `synthetic: true`.** No receiver, gpsd, Davis console or Ecowitt gateway was available to this
build on 2026-10-13, so every fixture was written by hand from the cited specifications. Recapture them on the
operator's hardware (device, firmware and date in this file) with the commands below and keep the hand-made
edge cases.

| File | Source of the shape | Recapture with |
|---|---|---|
| `readsb-aircraft-1090.json` | readsb `README-json.md` (dev branch, read 2026-10-13), section "aircraft.json" | `curl -s http://<host>/tar1090/data/aircraft.json` |
| `skyaware978-aircraft.json` | the same field set plus `uat_version` (dump978-fa / skyaware978 JSON) | `curl -s http://<host>:8978/data/aircraft.json` or `http://<host>/skyaware978/data/aircraft.json` |
| `readsb-stats.json` | readsb `README-json.md`, section "stats.json" (`noise` is dump1090-fa's; top-level `aircraft_with_pos` is undocumented) | `curl -s http://<host>/tar1090/data/stats.json` |
| `readsb-stats-silent.json` | hand-made: a minute with no messages | |
| `sbs-200.txt` | 200 BaseStation lines generated deterministically (8 message types over 10 aircraft, CRLF endings) | `nc <host> 30003 \| head -200` (readsb needs `--net-sbs-port 30003`) |
| `sbs-edge.txt` | hand-made edge cases | |
| `gpsd-session.jsonl` | gpsd JSON protocol (https://gpsd.gitlab.io/gpsd/gpsd_json.html): VERSION, DEVICES, WATCH, TPV, SKY, PPS | `gpspipe -w -n 20` |
| `davis-current-conditions.json` | WeatherLink Live Local API example (https://weatherlink.github.io/weatherlink-live-local-api/) with realistic values | `curl -s http://<console>/v1/current_conditions` |
| `ecowitt-livedata.json`, `ecowitt-livedata-imperial.json` | the shape Ecowitt's own Home Assistant integration reads (`Ecowitt/ha-ecowitt-iot`, `wittiot` 1.0.46; read only): `common_list[{id,val,unit}]`, `rain`, `piezoRain`, `wh25` | `curl -s http://<gateway>/get_livedata_info` |
| `ecowitt-garbage.html` | an HTML page instead of JSON (a firmware login page) | |

Edge cases on purpose:

- **readsb:** a `~` (non-ICAO) hex for every `*_other` type and `tisb_trackfile`; every readsb v2 `type`; an
  MLAT position (`type: mlat` and the `mlat` field list naming `lat`); a TIS-B position (the `tisb` list);
  `alt_baro: "ground"`; an entry without `hex` (skipped); the 978 MHz entries carry `uat_version`.
- **stats.json:** 11262 messages over 60 s (187.7/s, `ok`); the silent minute has 0 messages and no `signal`,
  `noise` or `aircraft_with_pos`.
- **SBS:** a line with a missing field (21 fields), an unknown type 9, a bad hex, a non-numeric ground speed, an
  emergency flag with squawk 7700, a `SEL` line and a `~` hex.
- **gpsd:** a mode-1 TPV (no fix, no position), a 2D fix, 3D fixes with the deprecated `alt` beside `altHAE`, a
  SKY before and after the fixes, a line that is not JSON and a PPS object.
- **Davis:** `rain_size` 2 (0.2 mm per count), an indoor record (type 4) and a leaf/soil record (type 2, ignored).
- **Ecowitt:** metric units, imperial units (°F, mph, inHg, in), a solar value in Klux (left out), known ids that
  are not stored and unknown ids (`0xF1`, `0x99`).
