# `un-sanctions` fixtures

Captured 2026-10-14 from UN Security Council consolidated sanctions list. Endpoint: `GET https://scsanctions.un.org/resources/xml/en/consolidated.xml (302 to an Azure Blob SAS URL)`. Licence: UN terms of use (public information).

consolidated.xml = the first 50 INDIVIDUAL and 25 ENTITY entries (dateGenerated 2026-10-13T23:00:01.949Z).

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
