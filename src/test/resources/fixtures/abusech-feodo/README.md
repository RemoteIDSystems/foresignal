# abusech-feodo fixtures

- `ipblocklist.json`: `GET https://feodotracker.abuse.ch/downloads/ipblocklist.json`, captured 2026-10-13 as
  served (keyless; `ETag` and `Last-Modified`; 5 entries although the site banner says the datasets are currently
  empty; every entry was last online in February or March 2026).
- `ipblocklist_recommended.json`: `GET https://feodotracker.abuse.ch/downloads/ipblocklist_recommended.json`,
  captured 2026-10-13 as served (1 entry).
- `sample.json`: hand-made (documentation addresses from RFC 5737 and RFC 3849): a full entry, an IPv6 entry with an
  unknown country and no `last_online`, port 0, a host name instead of an IP, an entry with neither time, and an
  exact repeat.
- `empty.json`: an empty list, the documented current state of the datasets.

Recapture per prompt 143.
