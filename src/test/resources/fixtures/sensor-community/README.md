# sensor-community fixture

- `filter-box.json`: `GET https://data.sensor.community/airrohr/v1/filter/box=52.3,13.0,52.7,13.8` (Berlin),
  captured 2026-10-13 21:36 UTC (511,874 bytes, 1,075 readings of 437 sensors), cut to the 27 readings of 11
  sensors:
  - SDS011 `1847`, `3355`, `6438` (three readings each), BME280 `829` and `5356`, DHT22 `1377`, SPS30 `25211` (four
    readings, two of them a second apart);
  - indoor SDS011 `24729` and DHT22 `24730` (`location.indoor` 1);
  - a noise sensor (`DNMS (Laerm)` `18377`) and a radiation sensor (`Radiation Si22G` `46258`), which carry no value
    this source stores.
- Real quirks kept: BME280 `829` reports 162,372.63 Pa and BME280 `5356` −146.22 °C.

Recapture per prompt 143.
