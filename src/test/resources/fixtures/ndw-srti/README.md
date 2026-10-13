# ndw-srti fixtures (prompt 074)

- `veiligheidsgerelateerde_berichten_srti.xml.gz`: captured 2026-10-13 18:44 UTC from
  `https://opendata.ndw.nu/veiligheidsgerelateerde_berichten_srti.xml.gz` with
  `curl -s -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>` and kept byte for byte. HTTP 200, `Content-Type:
  application/xml` without `Content-Encoding`, `ETag: "509c-65dbd2fa99bff"`, `Last-Modified: Tue, 13 Oct 2026 18:43:10
  GMT`; 20,636 bytes compressed, 466,335 decompressed; 169 situations of one record each (VehicleObstruction,
  GeneralObstruction, Accident), all point locations.
- `veiligheidsgerelateerde_berichten_srti.xml`: decompressed and trimmed to 20 whole situations, the envelope kept.

License: CC0 1.0 per NDW's copyright notice (`https://www.ndw.nu/copyright`, checked 2026-10-13).
