# Local RF decoder fixtures (prompt 086)

**Hand-made, `synthetic: true`.** No acars_router, radiosonde_auto_rx, gpsd receiver, WarDragon kit or NOAA Weather
Radio receiver was available to this build on 2026-10-13, so every file was written from the upstream projects'
published field lists. Recapture each one on the operator's own decoders with the command given and keep the hand-made
edge cases. Every registration, flight number, callsign, serial and kit id is fictitious (`FS`/`FSTEST` marks them);
scrub real ones that are not public before committing a capture.

| File | Shape and source | Recapture |
|---|---|---|
| `acarsdec.json` | one acarsdec JSON line: `timestamp, station_id, channel, freq, level, error, mode, label, block_id, ack, tail, flight, msgno, text` | `nc <router> 15550 \| head -1` (acars_router's ACARS TCP server) |
| `dumpvdl2.json` | one dumpvdl2 JSON line: `vdl2.t`, `vdl2.freq` (Hz), `vdl2.sig_level`, `vdl2.avlc.src/dst`, `vdl2.avlc.acars.{reg, flight, label, msg_num, msg_text}` | `nc <router> 15555 \| head -1` |
| `dumphfdl.json` | one dumphfdl JSON line: `hfdl.t`, `hfdl.freq` (Hz), `hfdl.sig_level`, `hfdl.lpdu.src/dst`, `hfdl.lpdu.hfnpdu.acars`, `hfdl.lpdu.hfnpdu.pos` | `nc <router> 15556 \| head -1` |
| `autorx-summaries.jsonl` | ten radiosonde_auto_rx `PAYLOAD_SUMMARY` datagrams (the last has only `time`, no `datetime`) and one `LOG` datagram to ignore | `socat -u UDP-RECV:55673 - \| head -11` on the auto_rx host's broadcast |
| `gpsd-5min.jsonl` | a 5-minute gpsd session at 1 Hz: VERSION, DEVICES, WATCH, TPV and SKY (with `ss` per satellite), PPS every 10 s; 3D fixes for 150 s, no fix (mode 1, no used satellites) for 90 s, 3D again for 60 s | `gpspipe -w -x 300` |
| `dragonsync-session.tsv` | DragonSync MQTT messages, `topic<TAB>payload`: a Wi-Fi Remote ID drone on the aggregate and per-drone topics with pilot and home positions, its `pilot_attrs` and `home_attrs` topics, a Bluetooth drone without a fix, one DJI O4 detection, an aircraft and an availability message | `mosquitto_sub -h <broker> -t 'wardragon/#' -v` |
| `same-headers.txt` | multimon-ng EAS output: a tornado warning for two Texas counties repeated three times, `NNNN`, a whole-state (`048000`) civil emergency, and a winter storm warning issued on day 365 at 23:30 whose 45-minute purge rolls into the next year | `rtl_fm -f 162.550M -M fm -s 22050 - \| multimon-ng -a EAS -t raw -` |

## Notes

- **acars_router framing:** its README documents the TCP server ports (15550, 15555, 15556; verified 2026-10-13) but not
  the framing. The `acars-json` codec splits JSON objects whether they end in a newline or not; confirm the framing at
  capture.
- **DJI DroneID:** DragonSync's `docs/mqtt-schema.md` (read 2026-10-13) identifies DJI detections by the `description`
  (`DJI O4 (Decrypted)`, `DJI Mini 2 (O2)`) and an empty `transport` ("Empty for OcuSync — receiver doesn't tag link
  layer"). `RemoteIdMapper` drops a message with an empty `transport`, or a `DJI…` description on a link that is not
  Wi-Fi or Bluetooth Remote ID.
- **DragonSync topics:** drone state is published identically on `wardragon/drones` (aggregate, with `seen_by`) and
  `wardragon/drone/<id>`; pilot and home positions also have their own `…/pilot_attrs` and `…/home_attrs` topics. The
  source's default subscription is `wardragon/drones`.
- **gpsd UBX passthrough:** no u-blox capture exists; the UBX-MON-HW and UBX-MON-RF frames in the tests are built from
  the u-blox interface description's field offsets. Recapture with `ubxMonitor=true` against a u-blox receiver.
