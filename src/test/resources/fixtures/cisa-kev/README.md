# cisa-kev fixtures

- `catalog.json`: `GET https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json`,
  captured 2026-10-13 (`ETag` and `Last-Modified`; `catalogVersion` 2026.10.13, 1,741 entries, 1.8 MB), trimmed to
  25 entries: the first 24 and `CVE-2026-59310` (the first entry with `knownRansomwareCampaignUse: Known`). `count`
  was set to 25. The capture includes `CVE-2015-5477` and `CVE-2026-5430`, whose upstream names carry a leading and a
  trailing space. Entries also carry a `forensicTriage` member that the prompt's payload does not include.

Recapture per prompt 143.
