# 137 — Browser Fetch And Web Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/137-CR-Browser-Fetch-And-Web-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/136-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Conventions.md` "Operator choice", `docs/Source-Catalog.md` "Opt-in restricted sources (prompts 137–138)"
- Completion Reports of 003 (secret storage), 004/005 (descriptors, budgets, `UpstreamClient`), 006 (health), 010 (scheduler), 043 (media store, `ImageGuard`), 066 (GDELT URLs), 072 (CCTV catalogues), 075 (InciWeb), 077 (ground stations), 088 (vessels), 062 (aircraft), 109 (licence metadata, egress), 132 (news articles), 134 (social post model and adapters)
- Libraries (verify licence and version): Playwright for Java (Apache-2.0) and its bundled Chromium; jsoup (MIT) for HTML

## Objective

Give the operator every source the earlier prompts left out because of terms, unofficial endpoints, scraping or headless browsers. Foresignal is self-hosted software for one operator's own use: these adapters ship **off by default**, each with its route, terms summary and fragility documented, and the operator decides. This prompt adds the collection machinery they need (an optional headless-browser fetch mode, per-source proxies and imported browser sessions) and the web-map, scrape and content adapters. Prompt 138 adds the API- and key-based ones (markets, forecasts, ratings, commercial keys).

## Principles (binding)

- **Off by default, never forbidden.** Every adapter here is disabled until the operator enables it. Descriptors carry an `access` block so the operator sees the route, terms summary and fragility before enabling.
- **Honest health.** Unofficial routes break. An adapter reports `BLOCKED` (403, 429 patterns, challenge pages), `LAYOUT_CHANGED` (selectors or JSON shape no longer match) or `GEO_RESTRICTED` instead of returning empty data, and the adapter doctor (140) can diagnose it.
- **No challenge solving.** When a site serves a CAPTCHA or bot challenge, the adapter stops and reports `BLOCKED` with reason `CHALLENGE`; the operator can import a browser session or configure a proxy. Foresignal does not solve challenges or spoof client fingerprints.
- **Same budgets and pacing.** Browser and scrape fetches count against 005 budgets like any request, with conservative defaults (one page at a time per host).

## Tasks

### 1. Fetch modes (`ai.foresignal.collect`)

- **`FetchMode`** per source: `direct` (existing `UpstreamClient`), `browser` (headless Chromium through Playwright), and an optional per-source **proxy** for either mode.
- **Browser runtime** (`BrowserFetcher`): disabled until `collect.browser.enabled=true`; Chromium path from `collect.browser.executablePath` or Playwright's managed install (`POST /v1/settings/browser/install` downloads it once, admin); one isolated browser context per source; `maxPages` (default 2 instance-wide), page timeout 60 s, memory guard (close the browser above `collect.browser.maxRssMb`, default 1,024). Adapters use it to load a page, wait for a selector or network idle, and either read the DOM or capture JSON responses the page fetches (`captureResponses: [urlPattern]`), which is the preferred extraction.
- **Browser sessions:** `PUT /v1/sources/{sourceId}/browser-session` (admin) stores a Playwright storage-state JSON (cookies and local storage the operator exported after logging in themselves), encrypted with the 003 secret key; `DELETE` removes it; `GET` returns only metadata (domains, expiry of the earliest cookie, imported at).
- **Proxies:** per-source `proxy` credential (`http://`, `https://` or `socks5://` URL with optional user and password, or a list used round-robin), applied to both modes; `GET /v1/sources/{sourceId}` shows that a proxy is set, never its value.
- **Access metadata:** `SourceDescriptor.access` = `{route: official | unofficial-api | scrape | browser, termsSummary, termsUrl, fragility: low | medium | high, requires: [key | session | proxy | browser], defaultEnabled}`; every existing descriptor defaults to `{route: official, fragility: low}`; exposed on `GET /v1/sources` and `/v1/sources/{sourceId}`.
- Health reasons `BLOCKED`, `LAYOUT_CHANGED`, `GEO_RESTRICTED`, `BROWSER_UNAVAILABLE` join 006's reasons.

### 2. Web-map and scrape adapters (all off by default)

| Source id | Route and mode | Output |
|---|---|---|
| `flightradar24` | `official`: FR24 API with the operator's subscription key (preferred); `unofficial-api`: the web map's feed endpoint for the operator's bboxes | aircraft TRACK records (062 model, fused by 099 like other aircraft sources) |
| `marinetraffic`, `vesselfinder` | `official`: the providers' paid APIs with the operator's key; `unofficial-api` / `browser`: web-map position endpoints for the operator's bboxes | vessel TRACK records (088 model) |
| `myshiptracking`, `shipxplorer` | `unofficial-api` / `browser` | vessel TRACK records |
| `readsb-mirrors` | `unofficial-api`: `aircraft.json` from operator-listed mirror URLs | aircraft TRACK records |
| `liveuamap` | `official`: Liveuamap API with the operator's key; `browser`: the public map's event feed for chosen regions | `conflict-event` records with category, place, source link |
| `oref-alerts` | `unofficial-api`: Home Front Command current-alerts JSON; usually needs a proxy located in Israel (`GEO_RESTRICTED` otherwise) | `air-raid-alert` records per area, polygons from the area catalogue |
| `ukmto-advisories` | `scrape` / `browser`: UKMTO recent incidents and advisories | `maritime-incident` records (088 model) |
| `hormuz-tracker` | `browser`: the WM-listed Hormuz transit dashboard, capturing its data queries | `series-point` records (transits per day) |
| `nuforc` | `scrape` | `uap-report` records |
| `broadcastify` | `official` (premium API, operator key) or `scrape`: feed status and **listener counts** per feed | `radio-feed` STATE records; listener-count series (spikes are an event signal) |
| `tinygs` | `unofficial-api` with the web client's timestamp header, or `browser` | ground-station and packet records (077 model) |
| `txdot-cctv`, GEV unofficial camera loaders | `unofficial-api` (072's not-ported loaders) | camera catalogue records (072 model) |
| `inciweb-extra` | 075's not-ported GEV routes | fire incident records (075 model) |
| `facebook-pages` | `browser` with an imported session (or logged out for public pages): operator-listed pages and groups | `social-post` records (134 model; `collection = scrape`) |
| `tiktok` | `browser` / `unofficial-api`: operator-listed accounts, hashtags and search terms | `social-post` records |

- Each adapter has WireMock or recorded-HTML fixtures and contract tests like every other adapter, a fragility note in its descriptor, and selectors kept in one resource file per adapter so the adapter doctor (140) can propose fixes.

### 3. Content fetching (opt-in extensions)

- **CCTV frames (072):** setting `cctv.fetchFrames` per pack (default off) with an interval (min 5 min) and retention (default 7 d): frames go through `ImageGuard` into the 043 media store and appear as `frameMediaId` on the camera record; `GET /v1/media/{mediaId}` serves them.
- **GDELT URLs (066):** setting `gdelt.fetchUrlMetadata` (default off): fetch each new event's `sourceUrl` (budgeted, `direct` mode) and store `title`, `description` and `publishedAt` from page metadata on the record.
- **News bodies and bylines (132):** settings `news.fetchBodies` and `news.storeBylines` (default off): fetch article pages (budgeted, per-host pacing), extract main text (jsoup readability heuristics, ≤ 50 KB) and bylines; bodies feed embeddings, story clustering and 135 claim extraction when present.
- Fetched content is the operator's: stored under the source's retention, `LOCAL_ONLY` egress by default (109), overridable per source.

### 4. API, spec, docs

- `GET /v1/settings/browser` and `PUT /v1/settings/browser` (admin; `BrowserSettings`: enabled, executablePath, installed version, maxPages, pageTimeout, maxRssMb), `POST /v1/settings/browser/install` (admin; 202 with progress), `GET/PUT/DELETE /v1/sources/{sourceId}/browser-session` (admin; `BrowserSessionInfo`, `BrowserSessionInput`), `SourceDescriptor.access` and `Source.access` (`SourceAccess`).
- `docs/OpenAPI.json`: as specified there; status implemented; pending list updated.
- `docs/Source-Catalog.md` "Opt-in restricted sources": every adapter with route, modes, requirements, terms summary and fragility. `docs/Operator-Guide.md` "Restricted and unofficial sources": enabling the browser runtime, exporting a session from your own browser, proxies, what `BLOCKED`, `LAYOUT_CHANGED` and `GEO_RESTRICTED` mean, and that keeping these working is the operator's call.
- AccessPolicy rows. Problem codes: `BROWSER_UNAVAILABLE` (503), `SESSION_INVALID` (400), `PROXY_INVALID` (400).

## Tests to write (do not run)

- `BrowserFetcherTest` (local test page served by the test): DOM extraction, JSON response capture, timeouts, page limit, memory guard, disabled runtime → `BROWSER_UNAVAILABLE`.
- `BrowserSessionTest`: storage state encrypted at rest, metadata only on read, applied to the source's context only.
- `ProxyTest`: per-source proxy in both modes, credentials never logged or returned.
- `RestrictedAdaptersTest` (fixtures per adapter): parsing for every adapter in §2; `BLOCKED` on 403 and challenge pages, `LAYOUT_CHANGED` on fixture drift, `GEO_RESTRICTED` for OREF without a proxy; all adapters disabled by default.
- `ContentFetchTest`: CCTV frame snapshots through `ImageGuard` with retention; GDELT URL metadata; news bodies and bylines only when enabled; budgets and pacing respected.
- `SourceAccessTest`: `access` metadata on every descriptor and in the API.
- `RestrictedSourcesControllersIT`: endpoints, scopes.

## Acceptance criteria

- Every previously excluded web-map, scrape and content source is available as an opt-in adapter or setting, off by default, with its route, terms summary and fragility visible before enabling.
- Sources can use a headless browser, a proxy and an imported session; blocked, changed and geo-restricted upstreams are reported honestly.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `137: Browser Fetch And Web Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/137-CR-Browser-Fetch-And-Web-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `137: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
