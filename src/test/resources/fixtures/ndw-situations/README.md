# ndw-situations fixtures (prompt 074)

- `actueel_beeld.xml.gz`: captured 2026-10-13 18:44 UTC from `https://opendata.ndw.nu/actueel_beeld.xml.gz` with
  `curl -s -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>` and kept byte for byte. HTTP 200,
  `Content-Type: application/xml` without `Content-Encoding` (the body is the gzip file), `ETag: "5502e-65dbd2fa98755"`,
  `Last-Modified: Tue, 13 Oct 2026 18:43:10 GMT`; 348,206 bytes compressed, 4,059,835 decompressed; root
  `mc:messageContainer` with `sit:SituationPublication` (`modelBaseVersion="3"`), 995 situations.
- `actueel_beeld.xml`: the same document decompressed and trimmed to 30 whole situations (up to three per first
  record type and per further record type, then the first small ones), the envelope kept.
- `edge-cases.xml`: hand-made. A situation of three records (MaintenanceWorks with a public comment and a line, lane
  closures with a point and lane counts, a speed limit), fog with a one-pair `posList`, abnormal traffic without a
  location, a road closure, and situations without `id` and without `situationVersionTime`.
- `xxe.xml`: hand-made, a DOCTYPE with an external entity; it must be refused.

Axis order: every `posList` on capture day held "lat lon" pairs; all 24,549 coordinates of the whole file fall inside
lat 50.5–53.7, lon 3.2–7.3 once swapped.

License: CC0 1.0 per NDW's copyright notice (`https://www.ndw.nu/copyright`, checked 2026-10-13: "Tenzij anders vermeld
is op de inhoud van deze website de Creative Commons Zero (CC0) verklaring van toepassing", with an exception for
images only). The 17 MB planned-works feed `planningsfeed_wegwerkzaamheden_en_evenementen.xml.gz` is out of scope.
