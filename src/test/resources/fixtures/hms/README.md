# Fixtures: `hms-smoke` and `hms-fire` (prompt 048)

| File | What it is |
|---|---|
| `hms_smoke20261012.kml` | The complete HMS smoke KML of 2026-10-12 (118,261 bytes, Last-Modified 2026-10-12 23:25:14 GMT, ETag `"1cdf5-65dad02904a80"`): 67 placemarks, 62 light and 5 medium, one `Polygon` each; `styleUrl` `#Smoke_Light_style` / `#Smoke_Medium_style`; a CDATA description `Start Time: 2026285 1200UTC<br>End Time: 2026285 1500UTC<br>Density: Light<br>Satellite: GOES-WEST`; no `ExtendedData`. |
| `hms_fire20261012.txt` | The complete HMS fire points of 2026-10-12 (1,627,466 bytes, ETag `"18d54a-65dad02aecf00"`): header `Lon, Lat, YearDay, Time, Satellite, Method, Ecosystem, FRP` padded with spaces, 16,777 points, 55 with FRP `-999.000`, 1,698 inside the default Texas box. |

SHA-256: smoke `0805d2a9be44a260db7afe9091e59bdec5b3c134f86f1955d6e7377f07468ea8`, fire
`b4045eb42915d618428bdcda382597a5303e10ffcb9dfae94713479ce475909d`.

## Capture procedure

- **Date:** 2026-10-13 00:38 UTC. Today's (2026-10-13) files answered **404** (no analysis yet), which
  is why the adapters treat a 404 for today as normal; yesterday's files were complete:

  ```sh
  H=https://satepsanone.nesdis.noaa.gov/pub/FIRE/web/HMS
  curl -sD - -o hms_smoke20261012.kml "$H/Smoke_Polygons/KML/2026/10/hms_smoke20261012.kml"
  curl -sD - -o hms_fire20261012.txt  "$H/Fire_Points/Text/2026/10/hms_fire20261012.txt"
  ```

- **Headers:** both answered 200 with `ETag` and `Last-Modified` (CloudFront in front of Apache), so
  the adapters use conditional GETs. The KML is `application/vnd.google-earth.kml+xml`, the text
  `text/plain`.

The XXE and heavy-density cases are synthetic, built in the test code.
