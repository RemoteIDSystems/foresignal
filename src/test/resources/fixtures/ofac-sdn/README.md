# `ofac-sdn` fixtures

Captured 2026-10-14 from US Treasury OFAC Sanctions List Service, legacy XML export. Endpoint: `GET https://sanctionslistservice.ofac.treas.gov/api/PublicationPreview/exports/SDN.XML (302 to a pre-signed S3 object)`. Licence: US Government work (public domain).

sdn.xml = trimmed to 50 sdnEntry plus the first vessel, aircraft, multi-alias and IMO-carrying entries (the full export was 29,187,085 bytes, Publish_Date 10/14/2026, Record_Count 19430). sdn-short.xml = the first 15 entries (the shrink-guard fixture). xxe.xml = hand-made DOCTYPE/XXE document that must be refused.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
