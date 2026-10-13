# nvd-cves fixtures

- `cves-page.json`: one page of 16 CVEs assembled from two real NVD CVE API 2.0 responses captured 2026-10-13
  without a key:
  - `GET https://services.nvd.nist.gov/rest/json/cves/2.0?lastModStartDate=2026-10-12T00:00:00.000Z&lastModEndDate=2026-10-13T00:00:00.000Z&resultsPerPage=2000&startIndex=0`
    (1,025 results): 14 CVEs, with CVSS 4.0 and 3.1, 4.0 only, 3.1 only, scores on both sides of 7.0, the rejected
    `CVE-2026-97058` and the unscored `CVE-2026-98385`;
  - `GET …?hasKev&lastModStartDate=2026-10-01T00:00:00.000Z&lastModEndDate=2026-10-13T00:00:00.000Z&resultsPerPage=20`:
    `CVE-2010-0738` (score 5.3, carries `cisaExploitAdd`) and `CVE-2015-5477` (77 CPE products, 85 references).
  `resultsPerPage` and `totalResults` were set to 16; every CVE object is as served.
- `epss.json`: `GET https://api.first.org/data/v1/epss?cve=CVE-2015-5477,CVE-2010-0738`, captured 2026-10-13.
- `epss-missing.json`: `GET https://api.first.org/data/v1/epss?cve=CVE-2015-5477,CVE-2026-96951`, captured
  2026-10-13: the second id (added to KEV that day) is absent from `data`.

Recapture per prompt 143.
