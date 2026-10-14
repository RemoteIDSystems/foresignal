# `aprs-is` fixtures

| File | What it is |
|---|---|
| `stream-r-sf-bay.txt` | Real. 200 lines (CRLF) of APRS-IS output from `rotate.aprs2.net:14580` for the filter `r/37.77/-122.42/50`, captured 2026-10-14 from 00:26 to 00:32 UTC: the server banner, the `# logresp FSCAP unverified, server T2PANAMA` reply, keepalive comments about every 20 s, and positions, Mic-E, compressed positions, objects, weather, status, telemetry, beacons and igate capability packets |
| `formats.tsv` | One line per parser format, `label<TAB>TNC2 line`. Real lines from the capture where the format occurred (timestamped, ambiguous, compressed with an overlay, Mic-E with and without altitude, weather, object, telemetry); hand-made lines with `N0CALL` calls for the rest (uncompressed with course, speed and altitude, compressed with course/speed and with altitude, killed object, item, killed item, status, message, acknowledgement, malformed position, no header) |

**Capture** (prompt 082): a Python socket client connected to `rotate.aprs2.net:14580`, read the banner, sent
`user FSCAP pass -1 vers Foresignal-fixture-capture 0.1 filter r/37.77/-122.42/50` (passcode `-1`: receive-only;
`FSCAP` is an unverified non-call login), read 230 lines (about 400 s) and closed. Nothing else was written.

**Trimming:** all 12 message lines of the capture (`::ADDRESSEE:` packets: personal messages and their
acknowledgements between other stations; 9 of them fell inside the kept range) were removed, then the stream was cut
to its first 200 lines. Nothing else was stripped: APRS-IS is public.
The capture holds 19 comment lines and 181 packets, of which 6 are of formats the parser does not support (`T#`
telemetry, a `Winlink` beacon without a data type, and `<IGATE,...` capability packets).
