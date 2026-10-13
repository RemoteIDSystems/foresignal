# `space` fixtures (prompt 064)

| File | What it is |
|---|---|
| `iss-passes-skyfield.csv` | The reference table of `PassPredictorTest`: every ISS pass above 10° over two WGS84 sites (Austin, Texas at 150 m and Sydney at 50 m) for 48 h from 2026-10-13T06:00Z, computed **independently of Orekit** with Skyfield 1.55 and sgp4 2.27 (`EarthSatellite.find_events`, `altitude_degrees=10`) from CelesTrak's ISS TLE of 2026-10-13 (`../celestrak-gp/iss-tle.txt`). The same element set is in `../celestrak-gp/stations.csv` |

Generated with (Python, packages installed in a scratch directory, not in the repository):

```python
from skyfield.api import load, wgs84, EarthSatellite
ts = load.timescale(builtin=True)
sat = EarthSatellite(line1, line2, "ISS", ts)
site = wgs84.latlon(lat, lon, elevation_m=alt)
t, events = sat.find_events(site, ts.utc(2026, 10, 13, 6), ts.utc(2026, 10, 15, 6), altitude_degrees=10.0)
```
