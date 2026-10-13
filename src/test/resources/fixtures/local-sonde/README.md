# local-sonde fixtures

- `datagrams.jsonl`: **generated**, one datagram per line, by a small Python script written from the
  `PAYLOAD_SUMMARY` field list in prompt 077 (`station, callsign, latitude, longitude, altitude, speed` in km/h,
  `heading, time, datetime, model, freq, temp, frame`, optional `humidity, pressure, sats, batt, snr`). No
  radiosonde_auto_rx code (GPL-3.0) was read or used. Lines: two summaries of sonde `S1234567` five seconds apart, a
  datagram of another type (`MODEM_STATS`), malformed JSON, an `M10` summary with sentinel values (`temp` -273,
  `humidity` and `pressure` -1) and a numeric `freq`, a `DFM17` summary without a position, and a summary without a
  `callsign`.

The script:

```python
import json, sys

def summary(serial, model, freq, t, lat, lon, alt, speed_kmh, heading, temp, frame, **extra):
    d = {"type": "PAYLOAD_SUMMARY", "station": "N0CALL-RX", "callsign": serial, "latitude": lat, "longitude": lon,
         "altitude": alt, "speed": speed_kmh, "heading": heading, "time": t[11:19], "datetime": t, "model": model,
         "freq": freq, "temp": temp, "frame": frame}
    d.update(extra)
    return d

lines = [
    summary("S1234567", "RS41-SGP", "404.500 MHz", "2026-10-13T20:40:00.000Z", 52.20925, 14.12191, 5123.4, 36.0, 92.5,
            -21.3, 1001, humidity=45.2, pressure=532.11, sats=9, batt=2.8, snr=12.4),
    summary("S1234567", "RS41-SGP", "404.500 MHz", "2026-10-13T20:40:05.000Z", 52.20931, 14.12260, 5150.9, 37.8, 93.0,
            -21.5, 1006, humidity=44.9, pressure=529.80, sats=9, batt=2.8, snr=12.1),
    {"type": "MODEM_STATS", "station": "N0CALL-RX", "snr": 11.0},
    "{this is not json",
    summary("M1234567", "M10", 403.9, "2026-10-13T20:40:02Z", 51.5012, 13.9876, 812.0, 18.0, 270.0,
            -273.0, 77, humidity=-1.0, pressure=-1.0),
    summary("D1234567", "DFM17", "402.100 MHz", "2026-10-13T20:40:03Z", None, None, 150.0, 0.0, 0.0, 9.5, 12),
    {"type": "PAYLOAD_SUMMARY", "station": "N0CALL-RX", "latitude": 50.0, "longitude": 10.0,
     "datetime": "2026-10-13T20:40:04Z"},
]
for l in lines:
    sys.stdout.write((l if isinstance(l, str) else json.dumps(l)) + "\n")
```
