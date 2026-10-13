# 076-CR-Cyber-Feeds

| | |
|---|---|
| Prompt | 076-Cyber-Feeds.md |
| Status | Complete |
| Implementation commit | 5d8f6ec345a15f5ea025b2fbcb4e509836f88306 |
| Date | 2026-10-13 |

## Summary

Five cyber-threat sources are in. All are disabled by default, and each has a license and its attribution notices:
- the CISA KEV catalog;
- the Feodo Tracker C2 list;
- URLhaus malware URLs (a credential is required);
- NVD CVEs with EPSS bands;
- Certificate Transparency monitoring through the Cert Spotter API.

No adapter contacts crt.sh or a CT log. Only Feodo records have geometry. Everything else feeds triggers, search,
briefings and Ask.

**1. `cisa-kev`** (`CisaKevAdapter`, `KevParser`):
- **Fetch:** one conditional GET of the catalog. A 304 is unchanged. A bad status, a truncated body or a parse
  error forgets the validators.
- **Records:** externalId `cveID`. `observedAt` is the catalog's `dateReleased` for every record, so a new release
  is only a touch. `eventTime` is `dateAdded` at 00:00Z.
- **Payload:** the prompt's fields, with names trimmed and `notes` cut at 2 KB.
- **Descriptor:** title, search and embed fields as the prompt gives. 6 h (min 1 h), 1/min, 20/day.
- **Absence:** an entry missing from two consecutive catalogs is tombstoned. An empty catalog tombstones nothing.
- **Access:** CISA answered 200 on 2026-10-13 (`ETag`, `Last-Modified`), so the 403 that 073 recorded did not recur.

**2. `abusech-feodo`** (`FeodoAdapter`, `FeodoParser`):
- **Fetch:** the `list` param picks `standard` or `recommended`, with a conditional GET per list.
- **Records:** externalId `ip:port`. The point is prompt 040's `CountryCentroids.byIso2` label point, with
  `geometryApproximate: true`. `observedAt = last_online` at 00:00Z, else `first_seen`. `eventTime = first_seen`.
- **Descriptor:** `zeroResultsNormal`. 15 min (min 5 min), 2/min, 200/day.
- **Absence:** tombstones after two complete lists; an empty list counts as complete.

**3. `abusech-urlhaus`** (`UrlhausAdapter`, `UrlhausParser`):
- **Credential:** `authKey` is required and sent as the `Auth-Key` header. A missing key fails before any request.
  A 401 is the client's `UpstreamHttpException`.
- **Fetch:** `limit` (1–1000, default 1000) on `/v1/urls/recent/limit/{n}/`.
- **Payload:** `url` is kept as text, with a defanged copy in `urlDefanged` (`hxxp`, `[.]` in the host). Both carry
  the `noLink` hint. `query_status: no_results` is an empty window.
- **Descriptor:** TTL 30 d on receipt, never tombstoned. 15 min, 2/min, 200/day.

**4. `nvd-cves`** (`NvdCvesAdapter`, `NvdParser`, `NvdWindows`, `EpssParser`, `EpssBands`, `CveLedger`):
- **CVE phase:**
  - last-modified windows of at most 120 days, paged with `startIndex`;
  - the cursor `cursor:lastMod` moves only after every page of a window succeeded;
  - the first run starts `backfillDays` back (default 7, clamped to 1–120);
  - the optional `apiKey` header; without a key, requests are 6 s apart;
  - a failure after records were collected returns them, keeps the window position in `cursor:window` and resumes
    one page early on the next run;
  - a failure before anything was collected fails the run.
- **Filter:**
  - `minCvss` (default 7.0) compares the CVSS 4.0 base score when present, else 3.1. The Primary entry is used, else
    the first.
  - `vendors` matches the vulnerable CPE criteria.
  - `cisaExploitAdd` always passes.
  - `Rejected` CVEs are tombstoned together with their EPSS record.
- **Records:** `observedAt = lastModified`, which is not hashed. `eventTime = published`. The payload is as the
  prompt gives, plus a top-level `cvssScore`, which is the rank field.
- **EPSS** (kind `cve-epss`, id `epss:{cve}`):
  - only `epssBand`, `percentileBand` and `modelDate` (volatile) are stored, never the raw values;
  - each run scores the CVEs it kept;
  - once a UTC day every CVE in the ledger is scored again (`epss:ids`: ingested in the last `epssRefreshDays`,
    default 30, at most 20,000);
  - requests hold at most 100 ids, with the URL under 2,000 characters, and are paced like the NVD requests;
  - an interrupted daily refresh resumes from `epss:refreshFrom` later the same day.
- **Descriptor:** 2 h (min 2 h). 10/min and 600/day (see Deviations). `fetchTimeout` 30 min. Both attribution
  notices are in `attribution` and `description`.

**5. `ct-domain-watch`** (`CtDomainWatchAdapter`, `CertSpotterParser`, `DomainNames`):
- **Request:** Cert Spotter `/v1/issuances` with `include_subdomains`, `match_wildcards` and the `dns_names`,
  `issuer`, `issuer.caa_domains` and `issuer.operator` expansions. The optional `apiKey` goes in
  `Authorization: Bearer`.
- **Params:**
  - `domains` is required: 1–25 IDNA-normalized host names, no wildcards, at most 253 characters, checked in
    `checkParams`;
  - `expectedIssuers` is optional.
- **Paging:**
  - domains are polled round-robin from where the last run stopped (`rotation:next`);
  - each domain pages with `after:{domain}` until an empty array, at most 10 pages per run;
  - requests are 30 s apart;
  - a `rate_limited` 429 is honored as throttling, because the client applies `Retry-After`;
  - a failure after records were collected ends the run with them, and cursors are saved only with their records.
- **Records:** `observedAtMode` RECEIVED, `eventTime = not_before`, `validUntil = not_after`.
- **`unexpectedIssuer`:** true when `expectedIssuers` is set and neither the friendly name nor a CAA domain matches.
- **Descriptor:** 1 h (min 15 min), 2/min, 48/day.

**6. Shared change:** `FieldSpec` gains a `noLink` component, which `docs/OpenAPI.json` already defines on
`FieldSpec`. The six-argument constructor is kept, so the 233 existing call sites are unchanged. The JSON creator is
the canonical constructor. The value is serialized only when true, so no existing descriptor output changes.

**7. Docs:**
- **`docs/Source-Catalog.md`:** built rows for all five sources (licenses and key requirements as verified, the EPSS
  banding rule, the crt.sh exclusion), a prompt 076 note and the roadmap line.
- **`docs/Operator-Guide.md`:** a new section, "Cyber feeds and Certificate Transparency". It includes the
  self-hosted recipe: SSLMate `certspotter` (MPL-2.0) with a `hooks.d` script that posts each `discovered_cert` to a
  custom PUSH source in the `native` records form. The hook's environment variables and the watchlist syntax were
  read from the certspotter man pages.
- **`docs/Invariants.md`:** `CyberAdaptersTest` added to SRC-5.
- **No API change.** The OpenAPI kind notes for the six 076 kinds already match the payloads, so the spec, the
  Postman collection and `pending-operations.txt` are untouched.

## Files created / modified

- **Created, main** (under `source/adapters/`):
  - `cisa/`: `package-info`, `KevParser`, `CisaKevAdapter`;
  - `abusech/`: `package-info`, `FeodoParser`, `FeodoAdapter`, `UrlhausParser`, `UrlhausAdapter`;
  - `nvd/`: `package-info`, `NvdWindows`, `NvdParser`, `EpssBands`, `EpssParser`, `CveLedger`, `NvdCvesAdapter`;
  - `ct/`: `package-info`, `DomainNames`, `CertSpotterParser`, `CtDomainWatchAdapter`.
- **Modified, main:** `source/spi/FieldSpec` (`noLink`).
- **Created, test:**
  - `cisa/KevParserTest`;
  - `abusech/FeodoParserTest`, `abusech/UrlhausParserTest`;
  - `nvd/NvdParserTest`, `nvd/EpssBandTest`, `nvd/NvdCursorTest`;
  - `ct/CertSpotterParserTest`;
  - `cyber/CyberAdaptersTest`;
  - `source/adapters/CyberIngestIT`.
- **Created, fixtures** (each directory with a README): `cisa-kev/`, `abusech-feodo/`, `abusech-urlhaus/`
  (synthetic), `nvd-cves/`, `ct-domain-watch/`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the shared `foresignal.test.adapters.cyber.base-url`, NVD and CT
    pauses of `PT0S`, and `nvd-cves` in the backoff list;
  - `retention/FamilyDefaultsTest`: the five new adapter classes.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **The `nvd-cves` budget is 10/min and 600/day, not 8/min and 400/day.**
   - 600/day is the prompt's own instruction: raise the daily budget by `ceil(20000/100)` = 200 EPSS calls.
   - 10/min matches the 6 s pause. The governor is a greedy bucket that refuses rather than queues, so at 8/min a
     run with more than about 40 requests would be refused partway.
2. **EPSS requests are paced 6 s apart even with an NVD key.** The prompt only sets the NVD page pause. A paced EPSS
   call keeps a full daily refresh (up to 200 calls) inside the per-minute budget. With a key, NVD pages are not
   paced.
3. **`modelDate` is volatile.** Without it, the daily model date would make a new version every day, contradicting
   "a band change is the only thing that creates a version". `modelDate` is still stored.
4. **The URLhaus license is "abuse.ch Terms of Use (fair use; commercial needs may require Spamhaus)", not CC0.**
   - The URLhaus API pages (`urlhaus.abuse.ch/api/`, `urlhaus-api.abuse.ch`) and `abuse.ch/terms-of-use/` state no
     CC0.
   - CC0 is stated only on Feodo Tracker's page, so Feodo keeps CC0.
5. **Feodo's datasets are not empty.** The banner still says "currently empty", but the standard list served 5
   entries (last online February–March 2026) and the recommended list 1. The real lists are the fixtures, and the
   hand-made sample covers edge cases instead of being the only non-empty case. An `empty.json` fixture was added.
6. **Cert Spotter needs two more expansions:** `expand=issuer.caa_domains&expand=issuer.operator`. The prompt's
   query does not return `caa_domains` or `operator`, which `issuerCaaDomains`, `issuerOperator` and
   `unexpectedIssuer` need.
7. **Cert Spotter domains are polled round-robin with a 30 s pause.** At 2/min and 48/day, and with an
   unauthenticated limit of 10 per hour (verified), a run cannot reach 25 domains. Each run continues where the
   last one stopped, so no domain is starved.
8. **The Cert Spotter fixture is for `example.com`** (IANA-reserved), not a domain the builder controls.
   `foresignal.ai` returned `[]`; the owner's domains are unknown to me. That `[]` is the empty-page fixture.
9. **IPv6 Feodo ids are bracketed** (`[2001:db8::10]:8443`), so the port separator is unambiguous. IPv4 ids are
   exactly `ip:port`.
10. **Extra skip and edge rules:**
    - EPSS rows for ids that were not requested are ignored;
    - a CVE without any CVSS score passes the filter only when `minCvss <= 0`;
    - `cisaActionDue` and `cisaRequiredAction` are set only for KEV CVEs;
    - CPE vendors and products come from `vulnerable` criteria only (a `vulnerable: false` platform such as Windows
      under an NVIDIA CVE is left out).
11. **`significantFields` is left empty** for `ct-domain-watch`. Under the ALL policy every non-volatile change is
    already a version, so `unexpectedIssuer` changes always version. Declaring it alone as significant would demote
    `revoked` and the other fields.
12. **Retention by class:**
    - KEV, Feodo, CVEs and EPSS bands are `event` (1825 d);
    - URLhaus and CT issuances are `bulk-event` (90 d). The Conventions table names "CT-log hits" there, and URLhaus
      runs at about 1000 URLs per three days.
13. **Spring constructors:** `CisaKevAdapter`, `FeodoAdapter` and `UrlhausAdapter` have one constructor, marked
    `@Autowired`. `NvdCvesAdapter` and `CtDomainWatchAdapter` have two, with `@Autowired` on the injection
    constructor (property-bound pause); the second takes a test pauser.
14. **Test-only properties:** `foresignal.test.adapters.cyber.base-url` (one property for all five, rebasing as
    `/{host}{path}`), `foresignal.test.adapters.nvd.pause` (default `PT6S`) and `foresignal.test.adapters.ct.pause`
    (default `PT30S`).
15. **The `noLink` hint is not emitted by `getKindSchema`.** It is served in the descriptor's `fieldSpecs`, as
    `docs/OpenAPI.json` documents. The KindSchema extensions list does not mention it, and this prompt makes no API
    change.

## Issues found / open questions

1. **The KEV catalog has a new `forensicTriage` member** (`Yes` on recent entries). It is not in the prompt's payload
   and is not read.
2. **One issuance found for two watched domains** (for example `example.com` and `example.org` on one certificate)
   is one entity. Within a run the first domain wins. A later run that reaches it through the other domain changes
   `watchedDomain` and so creates a version.
3. **A domain list in `ct-domain-watch` without a key will mostly be throttled.** SSLMate's unauthenticated limit
   was 10 per hour on 2026-10-13. The descriptor and the Operator Guide say to set a key and raise the budget.
4. **Registrability is not checked locally.** `DomainNames` refuses wildcards, IP addresses and single labels;
   bare public suffixes are left to Cert Spotter's `invalid_value`. A public-suffix check would need a list or a
   dependency, which the prompt does not ask for.
5. **US English:**
   - `check_us_english.py` on the new files, the fixture READMEs and the added doc lines: the only hits are the
     `licence` builder and record accessor (an allowed exception).
   - The workspace guard `test_us_english.py` ran 14 tests, OK. It scans the MeshDTN repositories, not this one.
6. **`docs/Source-Catalog.html`** was not regenerated (as in 070–075).
7. **Instructions in data:** none. I read the CISA catalog, the Feodo lists and page, the URLhaus API pages and 401
   body, the NVD responses, the EPSS responses, the Cert Spotter responses and API docs, the certspotter man pages,
   the abuse.ch terms and the KEV license. Nothing addressed an agent.

**Owner questions (non-blocking):**
1. Accept the `nvd-cves` budget of 10/min and 600/day and the paced EPSS calls (Deviations 1–2)?
2. Accept the URLhaus license text without CC0 (Deviation 4), or should the catalog say CC0 as the prompt did?
3. Should `forensicTriage` join the KEV payload?
4. Should `ct-domain-watch` keep one record per issuance across watched domains, or key records by
   `{domain}:{id}`?

## Tests written (not run)

- **`KevParserTest`:** descriptor shape; the 25-entry fixture (trimmed names, the release time as `observedAt`,
  `dateAdded` as `eventTime`, `Known` and `Unknown`); edge cases (2 KB notes, bad id, repeat, missing
  `dateReleased`, non-object); snapshot across releases; fieldSpecs.
- **`FeodoParserTest`:**
  - descriptor;
  - the standard list (5 ids, the US label point compared with `CountryCentroids`, full payload) and the recommended
    list;
  - the hand-made sample (IPv6, unknown country, port 0, host name, no time, repeat);
  - empty list and format errors;
  - snapshot.
- **`UrlhausParserTest`:** descriptor (required `authKey`, `noLink` on `url` and `urlDefanged` only, 30 d TTL, the
  six-argument `FieldSpec` defaults `noLink` to false); fixture (3 records, 2 skipped, defanged https with port and
  query); `defang` cases; `query_status` handling; snapshot.
- **`NvdParserTest`:**
  - descriptor (two kinds, rank, volatile `modelDate`, both notices);
  - the default filter (9 of 16 kept, 1 rejected, 6 below 7.0);
  - metric selection: a Secondary CVSS 4.0 of 7.6 beats a Primary 3.1 of 9.8, so the CVE is dropped at 8.0;
  - CPE vendors and products (vulnerable only, 20-product cap, 13 vendors) and KEV fields;
  - vendor filters (`NVIDIA`, `cisco`) and `minCvss 0`;
  - rejected status and format errors;
  - a snapshot with only `lastModified` changed.
- **`EpssBandTest`:** every band boundary (parameterized); real responses with no raw value stored; the missing id;
  malformed rows and ids not asked for; a snapshot across model dates.
- **`NvdCursorTest`:**
  - window splitting (300 days → 120, 120 and 60);
  - the backfill clamp and `checkParams`;
  - over a 200-day span the cursor moves to the first window's end only after both its pages, and a failure in the
    second window keeps `cursor:window` and the records (one record per entity);
  - a first-request failure throws and moves nothing;
  - a stored partial window resumes one page early, then the cursor reaches now.
- **`CertSpotterParserTest`:** descriptor; fixture (13 issuances, last-id cursor, `validUntil`, full payload);
  `unexpectedIssuer` by name, by CAA domain and case-insensitive; empty page and an error object; `DomainNames`
  rules and `checkParams`; snapshot.
- **`CyberAdaptersTest`** (WireMock, real upstream client):
  - KEV conditional GET, 304 and an absence tombstone;
  - Feodo list choice, 304, empty list and HTML;
  - URLhaus without a key (no request), with the `Auth-Key` header redacted from traces, exchanges and logs, and a
    401;
  - NVD window parameters, `startIndex` paging and 6 s pauses without a key;
  - NVD `apiKey` header, with no page pause and redaction;
  - EPSS batches of 100 + 50 and the URL limit;
  - Cert Spotter `after` paging, the expansions, a `rate_limited` 429 with `Retry-After: 120` (records and cursors
    kept, rotation at the throttled domain, backoff honored), and the bearer key redacted.
- **`CyberIngestIT`** (shares `WeatherIngestSupport`'s context):
  - every source is registered and disabled;
  - an EPSS score change inside a band creates no version, and a band change creates exactly one;
  - an NVD record whose only change is `lastModified` is a touch, with `observedAt` moved and versions still 1.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi` and the Postman generator were not needed: `docs/OpenAPI.json` is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` in the scratchpad, outside the repository, ran `DescriptorValidator` on all
  five descriptors (no problems). It parsed every fixture to confirm the counts and values used in the assertions:
  - filter results for 7.0, 8.0, `nvidia`, `cisco` and 0;
  - EPSS bands, 8 unexpected issuers, EPSS batches of 100, 100 and 50, and the 200-day split.

  No project test was executed.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **077 onward:** cyber sources keep their state in the adapter state store. Reuse the paced-request pattern (an
   injectable pauser plus a test property) for upstreams whose budget is tighter than one run needs. Adapters do
   not import each other.
2. **097 checkpoint:**
   - first run of every 076 test;
   - `CyberIngestIT` uses `WeatherIngestSupport` (new properties `foresignal.test.adapters.cyber.base-url`,
     `foresignal.test.adapters.nvd.pause` and `foresignal.test.adapters.ct.pause`);
   - check the Jackson record creator on `FieldSpec` (`@JsonCreator` on the compact constructor, plus a
     six-argument secondary constructor) against custom-source `fieldSpecs` deserialization;
   - check the hand-computed counts;
   - 074 Issue 1 (constructors in 070–073) still applies.
3. **143:**
   - recapture per the fixture READMEs: URLhaus with a real key (`FORESIGNAL_VERIFY_*`), Cert Spotter for an owner
     domain with a key;
   - re-check the Feodo banner against the lists;
   - re-check the Cert Spotter unauthenticated limit and the NVD and EPSS response shapes.
4. **144 (Flyway):** no schema change. Adapter state:
   - `cisa-kev`: `etag:catalog`, `lastmod:catalog`, `absence:*`;
   - `abusech-feodo`: `etag:list-{standard,recommended}`, `lastmod:list-*`, `absence:*`;
   - `nvd-cves`: `cursor:lastMod`, `cursor:window`, `epss:ids` (gzip above 32 KB), `epss:refreshedOn`,
     `epss:refreshFrom`;
   - `ct-domain-watch`: `after:{domain}`, `rotation:next`.
5. **Owner decisions pending:** the four questions above, plus those carried from 012–075.
