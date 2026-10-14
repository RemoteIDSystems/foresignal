# `deepstate-frontline` fixtures

| File | What it is |
|---|---|
| `deepstatemap_data_20261012.geojson` | The real file `https://raw.githubusercontent.com/cyterat/deepstate-map-data/main/data/deepstatemap_data_20261012.geojson`, captured 2026-10-13 (77,160 bytes, sha256 `6908c173253e1107a7920306a0bdc63b2e5f1a1f199c731f40145966ef6f372d`) |
| `deepstatemap_data_20261013.geojson` | The next day's file (77,459 bytes, sha256 `f9cf7f507465d75a382afb31017caa50c38faf08b13d309f3c6876bb006bd93e`), captured 2026-10-13T23:36:35Z |

Capture (prompt 081): `curl -s -D - https://raw.githubusercontent.com/cyterat/deepstate-map-data/main/data/deepstatemap_data_{yyyyMMdd}.geojson`.
Headers: `200`, `content-type: text/plain; charset=utf-8`, `cache-control: max-age=300`, a strong `etag`. The file for
20261014 answered `404` (`404: Not Found`) at capture time, which exercises the fallback to yesterday.

Each file is a FeatureCollection (`crs` CRS84) with one feature, empty properties and a MultiPolygon of 21 polygons:
2,908 vertices on the 12th, 2,918 on the 13th. Their WGS84 areas are 117,147.4 km² and 117,154.9 km² (change +7.5 km²).
The mirror repository is GPL-3.0; only its data files are used here.
