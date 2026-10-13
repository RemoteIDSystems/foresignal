# airq-local fixtures

No device was on the bench while building prompt 078; every sample comes from vendor documentation or follows it,
with the firmware noted. **synthetic: true** for all of them — recapture from real units (prompt 143) and scrub the
serial, MAC address, SSID and coordinates.

- `purpleair.json`: a PurpleAir PA-II local `/json?live=true` answer built from PurpleAir's "Sensor JSON
  documentation" (community.purpleair.com, current for firmware **7.04**, read 2026-10-13): `SensorId`,
  `DateTime`, `lat`, `lon`, `place`, `hardwareversion`, `current_temp_f`, `current_humidity`, `pressure`, the
  channel A fields and the `_b` channel B fields. Values, serial, SSID and coordinates (central Boulder, CO) are
  invented. Outdoor (`place: outside`); channels A and B agree.
- `purpleair-disagree.json`: the same device indoors (`place: inside`) with PM2.5 A 14.6 and B 3.2 µg/m³ (more than
  5 µg/m³ and more than 70% of their mean apart), trimmed to the fields the parser reads.
- `airrohr.json`: an airrohr (Sensor.Community firmware **NRZ-2020-133**) local `/data.json` with an SDS011 and a
  BME280, the value types of the 059 `airrohr` preset fixture (`fixtures/mapping/airrohr/sample.json`); pressure in
  pascals. No firmware code was read.
- `airgradient-indoor.json`: the `/measures/current` example of AirGradient's "Local Server API" document
  (`airgradienthq/arduino`, `docs/local-server.md`, read 2026-10-13), firmware **3.1.3**, model `I-9PSL` (indoor).
- `airgradient-outdoor.json`: the same shape for an Open Air monitor (model `O-1PST`, outdoor), values and serial
  invented.
