# `sec-8k` fixtures

Captured 2026-10-14 from SEC EDGAR current 8-K filings. Endpoint: `GET https://www.sec.gov/cgi-bin/browse-edgar?action=getcurrent&type=8-K&output=atom&count=100`. Licence: US Government work (public domain).

current-8k.xml = the first 30 Atom entries (ISO-8859-1). 8k-a.xml = hand-made 8-K/A with three items.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent; trim with the same tag-block selection so the XML structure stays intact.
