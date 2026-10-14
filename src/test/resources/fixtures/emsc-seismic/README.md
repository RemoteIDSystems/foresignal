# `emsc-seismic` fixtures

Captured 2026-10-14 from the EMSC FDSN gap-fill endpoint:
<https://www.seismicportal.eu/fdsnws/event/1/query?format=json&limit=5> (`gapfill.json`), a GeoJSON
FeatureCollection with `unid`, `time`, `lastupdate`, `mag`, `magtype`, `depth`, `lat`, `lon`, `auth`,
`source_catalog`, `flynn_region` and `evtype` properties.

The WebSocket session itself is exercised by `EmscStreamTest` with hand-made messages in the documented
`{"action":"create"|"update","data":<GeoJSON Feature>}` shape; no live recording is committed because the stream
is continuous. Licence: EMSC data (CC BY 4.0), <https://www.emsc-csem.org/Page/legal.php>.

**Recapture (prompt 143):** the same URL to `gapfill.json`, and (optionally) record a `wss://` session.
