# Fixtures: `faa-tfr` (prompt 087)

| File | What it is |
|---|---|
| `exportTfrList.json` | A real capture of `https://tfr.faa.gov/tfrapi/exportTfrList`: 76 TFRs (`SECURITY` 41, `HAZARDS` 12, `VIP` 10, `AIR SHOWS/SPORTS` 6, `UAS PUBLIC GATHERING` 6, `SPECIAL` 1). |
| `detail_6_9375.xml` | Real. A polygon TFR (91.137(a)(1) hazards, Seneca, OR): one `abdMergedArea` of 5 GRC vertices, ALT 0–6100 ft, UTC times. |
| `detail_4_3634.xml` | Real. A circle TFR (99.7, Disney World): the FAA's merged outline is 37 GRC vertices and the `Abd` part a 3 NM `CIR`; HEI 0–3000 ft; **no `dateEffective` and no `dateExpire`** (until further notice). |
| `detail_6_8928.xml` | Real. A multi-area TFR (91.141 VIP, Atlanta): three areas (MultiPolygon), `codeTimeZone` EDT with UTC values, and `txtLocalName` "261014 Atlanta, GA VIP" instead of the NOTAM number. |
| `detail_5_2811.xml` | Real. A 99.7 TFR whose `Abd` part has a counter-clockwise arc (`CCA`); its merged outline is 58 GRC vertices. |
| `detail_9_0001.xml` | **Synthetic.** No `abdMergedArea`, so the `Abd` fallback is used: area A is two clockwise arcs (`CWA`) between two GRC points (a circle-like ring), FL 180 ceiling; area B is a lone 5 km `CIR`; no `dateExpire`; the text only in `txtDescrModern` (HTML). |
| `detail_doctype.xml` | **Synthetic.** An XNOTAM with a DOCTYPE declaring an external entity; the parser must refuse it. |

## Capture procedure

- **Date:** 2026-10-14 04:29–04:31 UTC.
- **Commands** (one request per second for the details):

  ```sh
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -D headers.txt \
    -o exportTfrList.json https://tfr.faa.gov/tfrapi/exportTfrList
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" \
    -o detail_6_9375.xml https://tfr.faa.gov/download/detail_6_9375.xml
  ```

- **List response:** HTTP 200, `Content-Type: application/json`, `Cache-Control: max-age=0, no-cache, no-store`, no
  `ETag` or `Last-Modified`.

Facts the parser relies on, from all 76 details: every area has an `abdMergedArea` of plain GRC vertices (arcs and
circles appear only in the `Abd` parts); `dateIssued`, `dateEffective` and `dateExpire` are UTC whatever `codeTimeZone`
says (for example 6/9370, `codeTimeZone` EDT, `dateEffective` 2026-10-16T20:30:00, text "EFFECTIVE 2610162030 UTC
(1630 LOCAL 10/16/26)"); 14 TFRs have no `dateExpire`.
