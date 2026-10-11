# 076 — Cyber Feeds

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/076-CR-Cyber-Feeds.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/075-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `cisa-kev`, `abusech-feodo`, `abusech-urlhaus`
- `docs/Gap-Analysis.md` §4b row "Vulnerabilities (NVD + EPSS)" ("EPSS stored banded on ingested CVEs only"), §8.C item 37, §9 ("crt.sh polling" rejected)
- `docs/Core-Data-Model.md` §2 ("Catalogue and reference" row), §3 (adapter rules 7–13), §5.3
- Prompt 040 `CountryCentroids` (shared country label points); prompt 024 custom PUSH sources (for the self-hosted CT recipe)
- Upstream docs: CISA KEV (`www.cisa.gov/known-exploited-vulnerabilities-catalog`, `github.com/cisagov/kev-data`), abuse.ch Feodo Tracker (`feodotracker.abuse.ch/blocklist/`) and URLhaus API (`urlhaus.abuse.ch/api/`), NVD API 2.0 (`nvd.nist.gov/developers/vulnerabilities`, `/developers/start-here`), FIRST EPSS API (`www.first.org/epss/api`), SSLMate Cert Spotter API (`sslmate.com/help/reference/ct_search_api_v1`)

## Objective

Add the cyber-threat sources: the CISA Known Exploited Vulnerabilities catalogue, abuse.ch botnet C2 and malware-URL feeds, NVD CVEs with EPSS exploit-probability **bands**, and Certificate Transparency monitoring for operator-chosen domains through an allowed API (no crt.sh polling). Most records have no geometry; they feed triggers, search, briefings and Ask.

## Tasks

Common descriptor values unless stated: `historyTier=STANDARD`, policy ALL, `ttlAnchor=RECEIVED`, every text field untrusted for LLM use (prompt 028 rules), all sources **disabled** by default.

### 1. `cisa-kev` (`ai.foresignal.source.adapters.cisa`)

- `GET https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json` with conditional GET (verified 2026-10: ETag + Last-Modified; `catalogVersion` `2026.10.08`, `count` 1739).
- One record per entry: `kind = vulnerability`, `externalId = cveID`, no geometry.
- Time: `observedAt = dateReleased` of the catalogue (the publication date, UPSTREAM; fixed for every record), `eventTime = dateAdded` (date at 00:00Z).
- Payload: `cveId, vendorProject, product, vulnerabilityName` (trimmed; upstream has leading spaces), `shortDescription, requiredAction, dueDate, knownRansomwareCampaignUse` (`Known|Unknown`), `cwes, notes` (≤ 2 KB), `kev: true`.
- `titleField = vulnerabilityName`, `searchFields = cveId, vendorProject, product, vulnerabilityName`, `embedFields = vulnerabilityName, shortDescription`, `rankField` none. Interval 6 h (min 1 h), budget 1/min, 20/day. Entries removed from the catalogue (rare) → `AbsenceTombstoner` (prompt 072). Licence: CC0 1.0 (as published with `cisagov/kev-data`; confirm during capture).

### 2. `abusech-feodo` and `abusech-urlhaus` (`ai.foresignal.source.adapters.abusech`)

- **`abusech-feodo`:** `GET https://feodotracker.abuse.ch/downloads/ipblocklist.json` (param `list` ∈ `standard` → `ipblocklist.json` (C2s active in the last 30 days, with metadata), `recommended` → `ipblocklist_recommended.json`; default `standard`). Verified 2026-10: keyless, ETag + Last-Modified, regenerated every 5 minutes, and the site banner states the datasets are **currently empty** → `zeroResultsNormal=true`. Record `kind = malware-c2`, `externalId = ip:port`; geometry = the country label point from prompt 040's `CountryCentroids.byIso2` (`ai.foresignal.reference`; check it exists, else bundle Natural Earth admin-0 label points and leave a note for 040 to switch it over) with payload `geometryApproximate: true`; payload `ip, port, status, hostname, asn, asName, country, malware, firstSeen, lastOnline`; `observedAt = last_online` (date at 00:00Z), `eventTime = first_seen`. Interval 15 min (min 5 min), budget 2/min, 200/day. Absence tombstones on. Licence CC0 (abuse.ch terms: commercial and non-commercial use without limitation).
- **`abusech-urlhaus`:** verified 2026-10: `https://urlhaus-api.abuse.ch/v1/urls/recent/limit/3/` answers **401 Unauthorized** without a key. Required credential `authKey` (free from `auth.abuse.ch`), sent as header `Auth-Key`. `GET https://urlhaus-api.abuse.ch/v1/urls/recent/limit/{n}/` with param `limit` (default 1000, max 1000). Do not use the bulk `downloads/json_recent/` file: it still answered without a key on 2026-10-10 but the API page states every download requires an Auth-Key, and it is 17 MB.
  - Record `kind = malware-url`, `externalId = id`, no geometry; payload `url, urlDefanged` (`hxxp://`, `[.]` in the host), `host, urlStatus, threat, tags, blacklists, reporter, reference` (`urlhaus_reference`); `observedAt = date_added` (fixed), `eventTime = date_added`.
  - Treat `url` as hostile: never render as a link (`fieldSpecs` hint `noLink: true`), never fetch it, never give it to an LLM outside an untrusted block.
  - Interval 15 min (dumps regenerate every 5 minutes; abuse.ch asks not to fetch more often), budget 2/min, 200/day, `recordTtl` 30 d. Licence CC0; note "commercial or for-profit needs may require a Spamhaus subscription" from the API page.

### 3. `nvd-cves` with EPSS bands (`ai.foresignal.source.adapters.nvd`)

- **CVE fetch:** `GET https://services.nvd.nist.gov/rest/json/cves/2.0?lastModStartDate={cursor}&lastModEndDate={now}&resultsPerPage=2000&startIndex={k}`. Optional credential `apiKey` sent as header `apiKey`.
  - The window may not exceed 120 days; split if the cursor is older.
  - Cursor (`cursor:lastMod`) is stored in `AdapterStateStore` only after every page of a window succeeded.
  - First run: cursor = now − `backfillDays` (param, default 7, max 120).
  - NVD asks automated clients to sync incrementally no more than once every two hours → interval 2 h (min 2 h).
  - Budget: published limits are 5 requests / 30 s without a key and 50 / 30 s with one; descriptor default 8/min, 400/day; sleep 6 s between pages when no key is set.
  - Attribution notice (required): "This product uses the NVD API but is not endorsed or certified by the NVD."
- **Filter** (params): `minCvss` (number, default 7.0; compares v4.0 base score if present, else v3.1), `vendors` (string-list, optional, matched against CPE vendor), and always include CVEs whose NVD record carries `cisaExploitAdd`. Skip `vulnStatus = Rejected` (and tombstone if previously ingested).
- **Record:** `kind = vulnerability` (same kind as KEV; different source), `externalId = cve.id`, no geometry, `observedAt = lastModified`, `eventTime = published`. Payload: `cveId, vulnStatus, description` (English, ≤ 4 KB), `cvssV40 {baseScore, baseSeverity, vectorString}`, `cvssV31 {…}`, `cwes` (≤ 10), `vendors`, `products` (from configuration CPE criteria, ≤ 20 each), `references` (≤ 10 URLs), `kev` (bool), `cisaActionDue, cisaRequiredAction`. `titleField = cveId`, `embedFields = description`, `rankField = cvssScore` (add a top-level `cvssScore` = the score used for filtering).
- **EPSS, banded and separate:** to keep daily score jitter from creating CVE versions and to respect the complete-snapshot rule, EPSS is a second kind in the same source:
  - After each CVE fetch, and once a day for every CVE ingested by this source in the last `epssRefreshDays` (param, default 30; the id list is kept in `AdapterStateStore`, capped at 20,000), call `GET https://api.first.org/data/v1/epss?cve=CVE-a,CVE-b,…` with ≤ 100 ids per request (keep the URL under 2,000 characters). Verified 2026-10 response: `{status, total, limit, data:[{cve, epss, percentile, date}]}`.
  - Emit `kind = cve-epss`, `externalId = "epss:" + cve`, payload `cveId`, `epssBand` (`<0.01 → very-low`, `<0.05 → low`, `<0.2 → medium`, `<0.5 → high`, else `very-high`), `percentileBand` (`<p50, p50, p90, p95, p99`), `modelDate`. Raw `epss`/`percentile` values are **not** stored. `observedAt = date` (UPSTREAM model date, fixed). A band change is the only thing that creates a version.
  - Attribution: "EPSS scores from FIRST.org (first.org/epss)".
- Budget accounting: EPSS calls count against the same source budget; raise the default per-day budget to cover `ceil(ids/100)` daily calls.

### 4. `ct-domain-watch` (`ai.foresignal.source.adapters.ct`)

- **Approach (verified 2026-10):** crt.sh polling is rejected (Gap-Analysis §9). Use the SSLMate **Cert Spotter** CT Search API v1: `GET https://api.certspotter.com/v1/issuances?domain={d}&include_subdomains=true&match_wildcards=true&expand=dns_names&expand=issuer&after={cursor}`. Unauthenticated queries are allowed for personal or evaluation use up to an unpublished hourly limit; production use needs an API key (optional credential `apiKey`, sent as `Authorization: Bearer`). The free tier may change without notice, so handle `rate_limited` errors as throttling (honour `Retry-After`).
- Params: `domains` (string-list, **required**, ≤ 25; each validated as a registrable hostname, IDNA-normalised, no wildcards, ≤ 253 chars), `expectedIssuers` (string-list of issuer friendly names or CAA domains, optional).
- Per domain: cursor `after:{domain}` in state; page until an empty array (cap 10 pages per run). First run without a cursor lists every unexpired issuance: ingest them, then continue incrementally.
- Record: `kind = certificate-issuance`, `externalId = id`, no geometry; `observedAtMode=RECEIVED` (the API gives no discovery time), `eventTime = not_before`, `validUntil = not_after` (an upstream-asserted expiry). Payload `watchedDomain, dnsNames` (≤ 100), `issuerName, issuerOperator, issuerCaaDomains, notBefore, notAfter, revoked, certSha256, pubkeySha256`, and `unexpectedIssuer` (true when `expectedIssuers` is set and the issuer matches none; significant, so triggers can fire on it).
- Interval 1 h (min 15 min), budget 2/min, 48/day without a key (raise when a key is set). Licence: SSLMate API terms (record `termsUrl`).
- Document the self-hosted alternative in `docs/Operator-Guide.md`: run SSLMate's open-source `certspotter` monitor (MPL-2.0) with a hook script that posts each discovery to a prompt 024 custom PUSH source. Foresignal ships no CT log client.

### 5. Fixtures, docs

- Real captures (KEV trimmed to 25 entries; Feodo as served, plus a hand-made non-empty sample; URLhaus with your own key, removed from the README; NVD one page of ≤ 20 CVEs incl. v4.0 and v3.1 metrics and a `cisaExploitAdd`; EPSS two ids; Cert Spotter for a domain you control). Edge cases: rejected CVE, NVD 120-day split, EPSS id missing from the response, Cert Spotter `rate_limited`.
- `docs/Source-Catalog.md`: rows for all five sources (licence, key requirements as verified, the EPSS banding rule, the crt.sh exclusion).
- No API change.

## Tests to write (do not run)

- `KevParserTest`, `FeodoParserTest`, `UrlhausParserTest` (defanging, `noLink`), `NvdParserTest` (metric selection, CPE vendor/product extraction, filters, rejected status), `EpssBandTest` (boundary values, no raw values stored), `CertSpotterParserTest` (`unexpectedIssuer`): real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- `NvdCursorTest`: window splitting, cursor only advances after a fully successful window, backfill clamp.
- Adapter tests (WireMock): KEV and Feodo conditional GET; URLhaus `Auth-Key` header present and redacted in traces, 401 → `UpstreamHttpException`; NVD `apiKey` header, page sleep without a key, `startIndex` paging; EPSS batching ≤ 100 ids; Cert Spotter pagination with `after`, `Retry-After` honoured.
- `CyberIngestIT` (`@IntegrationTest`): an EPSS score change inside a band creates no version; a band change creates exactly one; an NVD record whose only change is `lastModified` creates no version (it drives `observedAt`, which is not hashed), only a touch.

## Acceptance criteria

- `cisa-kev`, `abusech-feodo`, `abusech-urlhaus` (credential required), `nvd-cves` and `ct-domain-watch` register, disabled, with licences and attribution notices.
- No adapter contacts `crt.sh` or a CT log directly.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `076: Cyber Feeds`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/076-CR-Cyber-Feeds.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `076: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
