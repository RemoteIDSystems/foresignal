# `ofac-sdn` fixtures

Captured 2026-10-14 from US Treasury OFAC Sanctions List Service, legacy XML export. Endpoint: `GET https://sanctionslistservice.ofac.treas.gov/api/PublicationPreview/exports/SDN.XML (302 to a pre-signed S3 object)`. Licence: US Government work (public domain).

sdn.xml = trimmed to 50 sdnEntry plus the first vessel, aircraft, multi-alias and IMO-carrying entries (the full export was 29,187,085 bytes, Publish_Date 10/14/2026, Record_Count 19430). sdn-short.xml = the first 15 entries (the shrink-guard fixture). xxe.xml = hand-made DOCTYPE/XXE document that must be refused.

**Added 2026-10-16 (post-review), real entries, not synthetic:** four `sdnEntry` blocks copied verbatim from the 2026-10-15 SDN.XML export (29,231,187 bytes, Publish_Date 10/15/2026, Record_Count 19457) and appended to sdn.xml: uid 10923 (entity with 95 aliases, the alias-cap test), uid 2676 (individual with three aliases and two passports), uid 15036 (vessel ARTAVIL with `IMO 9187629` and MMSI `572469210`) and uid 18150 (aircraft with an `Aircraft Tail Number` id, `EP-MMH`). The file's Publish_Date stays 10/14/2026.

consolidated.xml = the first 30 `sdnEntry` blocks of the Consolidated (Non-SDN) list in the legacy schema, `GET …/exports/CONSOLIDATED.XML` (302 to S3), captured 2026-10-16 (1,062,957 bytes, Publish_Date 09/14/2026, Record_Count 481). The prompt named `CONS_PRIM.XML`; on 2026-10-16 that export answered 200 with an empty body, so `CONSOLIDATED.XML` is what the adapter fetches.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
