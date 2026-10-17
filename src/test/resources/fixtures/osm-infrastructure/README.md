# Fixtures: `osm-infrastructure` (checkpoint 124; adapter from prompt 104)

The adapter makes no network requests: it reads an operator-made GeoJSON text sequence. This fixture
is that file for Monaco, made 2026-10-17 from the official Geofabrik extract. No Overpass or
openstreetmap.org API was used.

**Data © OpenStreetMap contributors, ODbL 1.0** (https://www.openstreetmap.org/copyright).

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| (input, not committed) `monaco-261016.osm.pbf` | `https://download.geofabrik.de/europe/monaco-261016.osm.pbf` (what `monaco-latest.osm.pbf` redirected to; `Last-Modified` Fri, 16 Oct 2026 23:38:16 GMT; MD5 `85be1fc628f38b08665534d5ae00f5d1` matches Geofabrik's `.md5`) | 692,762 | `87d50ff07838e7cdb08e0704453b03545b4dcbf809eb33e1f6aa3687f42e6ec5` |
| `monaco-261016-infra.geojsonseq` | made from the input as described below (40 features) | 68,105 | `4e6f4f88bb1cefb1c0b47b12f42c478a43db5c4e0bdd7e26f3076c3aababd44a` |

Geofabrik no longer publishes `.osm.bz2` XML for small regions (`monaco-latest.osm.bz2` and
`liechtenstein-latest.osm.bz2` answered 404 on 2026-10-17), so the input is the PBF.

## How it was made (a stand-in for osmium)

`osmium` is not installed on the capture machine. A Python 3 standard-library script
(`osm_infra_export.py`, SHA-256 `4b7c7903d5e98bb64085cf175c093fda81eb943c1a46bf5494388447d24df75b`, `zlib` plus a minimal protobuf reader, kept in the session scratchpad) stands
in for the Operator Guide recipe (`osmium tags-filter … nwr/power=plant,substation,generator
nw/power=line,cable nwr/amenity=hospital nwr/healthcare=hospital nwr/telecom=data_center,exchange
nwr/aeroway=aerodrome nwr/landuse=port nwr/harbour=yes nwr/man_made=wastewater_plant,water_works`, then
`osmium export -f geojsonseq --add-unique-id=type_id`). What it does:

- Decodes every `OSMData` block (dense and plain nodes, ways; coordinates
  `1e-9 × (offset + granularity × value)`, rounded to 7 decimals).
- Keeps tagged nodes and ways that match one of the filter expressions above.
- Writes one RFC 8142 record per line: `0x1E`, the feature JSON, `\n`. `id` is `n<id>` or `w<id>`,
  `properties` are the OSM tags unchanged. Nodes are Points, open ways LineStrings, closed ways
  (first ref = last ref, at least 4 refs) Polygons. Nodes first, then ways, each by ascending id.

Known differences from real osmium output: relations (multipolygons) are not assembled, and closed
ways are written as `Polygon` rather than osmium's area handling (which can emit a `MultiPolygon` with
an area id). Recapture with osmium when it is available.

## What it holds (read with the production `OsmInfraParser`, `OsmInfraParserCaptureTest`)

- 40 features: 3 Points, 2 LineStrings, 35 Polygons.
- By tag: `power=generator` 29 (all rooftop solar, `generator:output:electricity=yes`, no
  `plant:output:electricity`), `amenity`/`healthcare=hospital` 7, `power=substation` 2, `power=cable` 2
  (RTE, `voltage=63000`). No telecom, aerodrome, port or water features in Monaco.
- Parsed at the default 110 kV threshold: 38 records, `power-plant-osm` 29, `hospital` 7,
  `substation` 2 (the 63 kV cables are dropped); at 60 kV, 40 records including 2 `power-line` with
  `voltageKv` 63.0. Sample: `w49209406`, "Centre hospitalier Princesse Grace (CHPG)", a 20-vertex
  Polygon near 7.410° E, 43.730° N.
