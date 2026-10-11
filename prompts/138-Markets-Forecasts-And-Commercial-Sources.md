# 138 — Markets Forecasts And Commercial Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/138-CR-Markets-Forecasts-And-Commercial-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/137-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Conventions.md` "Operator choice", `docs/Source-Catalog.md` "Opt-in restricted sources (prompts 137–138)", `docs/Intelligence-Design.md` §8.4 (ensemble), §8.6 (external forecasters), §14.3 (market blending research)
- Completion Reports of 004/005 (descriptors, budgets), 093 (series model; §8 prediction markets), 109 (licence metadata, egress overrides), 117 (market and attention series, attention topics), 119 (analysis-series registry), 120 (precursor candidates), 122 (forecasters, ledger, ensemble, external forecasters), 132 (rating providers), 136 (lead–lag), 137 (fetch modes, access metadata, proxies)
- Upstreams (verify each at fixture capture and record versions and dates): Kalshi trade API v2 market data; Polymarket Gamma API and CLOB prices-history; Metaculus API (operator token); VIEWS forecasting API; CME DataMine (operator credentials) and public settlement pages; ICE end-of-day data (operator subscription); Yahoo Finance chart endpoint; CNN Fear & Greed data endpoint; Google Trends (official alpha API with the operator's access, else the web endpoints); Electricity Maps API (operator key); NewsGuard API (operator key); Ad Fontes Media data (operator licence); Shodan API (operator key); Windy Webcams API v3 (operator key)

## Objective

Add the market, forecast, rating and commercial-key sources that earlier prompts excluded on terms. All are **off by default** and enabled by the operator with their own accounts. Prediction markets, Metaculus and VIEWS become **external forecasters** in the ledger (research shows market blending improves forecasts), scored like every other forecaster; market and attention series join the analysis-series registry so discovery, precursor tests and 136's lead–lag analysis can use them.

## Principles (binding)

- **Off by default, never forbidden** (as 137). Descriptors carry the 137 `access` block with route, terms summary and requirements.
- **Scored, not trusted.** External forecasts enter the ledger as forecasters with their own scores, calibration and ensemble weights; they never override base rates or the operator, and the ensemble includes them only as their skill earns.
- **Licensed data defaults local.** Data whose terms restrict redistribution defaults to `egress=LOCAL_ONLY`; the operator can override per source (109).

## Tasks

### 1. Prediction markets and forecast platforms (`ai.foresignal.markets`)

- **`kalshi`** (POLLING, keyless reads): markets in operator-chosen series or events and all markets linked to questions; `series-point` records per market (`yesBid`, `yesAsk`, `mid`, `lastPrice`, `volume`, `openInterest`; dollar and fixed-point strings parsed exactly), interval 15 min (5 min for linked markets).
- **`polymarket`** (POLLING): Gamma API market metadata plus CLOB prices history for operator-chosen tags and linked markets; same series fields; `direct` mode with 137 `browser` mode as fallback.
- **`metaculus`** (POLLING, operator token): community prediction (and quartiles) for linked questions.
- **`views-forecasts`** (POLLING): VIEWS monthly conflict-fatality forecasts per country and grid cell as series and as forecasts for matching ledger questions.
- **Market catalogue:** `market` table (`platform`, `marketId`, `title`, `rules`, `closeTime`, `resolution`, `status`, `lastMid`, `updatedAt`), searchable for linking.
- **Ledger links (122):** `market_link` (`questionId`, `platform`, `marketId`, `orientation` (`yes-means-yes | yes-means-no`), `createdBy`). External forecasters `market:kalshi`, `market:polymarket`, `metaculus`, `views` submit a forecast whenever the linked market's mid (or community prediction) moves by ≥ 0.02 and at least daily, with `made_at` = the upstream timestamp's knowledge time (never earlier than Foresignal saw it); resolution stays with the ledger's own resolver.
- **Ensemble (122):** external forecasters are ensemble candidates like any other, weighted by their ledger skill once ≥ 30 linked questions have resolved; until then they appear beside the ensemble, not in it.
- **Series:** prices and community predictions register in 119's analysis-series registry (`prediction-market` family) as precursor candidates (120) and lead–lag inputs (136).

### 2. Markets and finance

- **`cme-settlements`**: `official` with the operator's DataMine credentials, or `scrape` of public daily settlement pages for chosen contracts; settlement, volume and open interest series.
- **`ice-eod`**: the operator's ICE end-of-day subscription for chosen contracts.
- **`yahoo-finance`**: `unofficial-api` chart endpoint for operator-listed symbols (daily and intraday bars within the endpoint's limits).
- **`cnn-fear-greed`**: `unofficial-api` index and components, daily.
- All as `series-point` records with `knownAt`, release lags and `LOCAL_ONLY` default egress.

### 3. Attention, energy, ratings, exposure, cameras

- **`google-trends`**: official alpha API when the operator has access, else the web endpoints (`unofficial-api`); interest-over-time per attention topic (117 `google` block: terms, geo) as attention series; consistent scaling where the official API provides it, otherwise per-request normalisation recorded in the series metadata.
- **`electricity-maps`**: operator key; carbon intensity and power breakdown per zone as series (complements 093).
- **`newsguard`** and **`adfontes`**: rating providers for 132 (`media_provider` rows, operator key or licensed CSV import); never generated, as 132 requires.
- **`shodan`**: operator key; saved queries (`shodan_query`: query, region, cadence) produce daily count series and, for monitored networks, `exposed-service` records (host, port, product, first and last seen).
- **`windy-webcams`**: operator key; webcam catalogue records in the 072 model, with 137's opt-in frame snapshots.

### 4. API, MCP, spec, docs

- `GET /v1/markets?platform&q&status&cursor` (read), `GET /v1/markets/{platform}/{marketId}` (read; latest prices and series refs), `GET /v1/forecasts/questions/{questionId}/market-links` (read), `POST /v1/forecasts/questions/{questionId}/market-links` (agent; `MarketLinkInput`), `DELETE /v1/forecasts/questions/{questionId}/market-links/{linkId}` (agent), `GET/POST /v1/shodan/queries` and `GET/PUT/DELETE /v1/shodan/queries/{queryId}` (admin).
- MCP: `search_markets`, `get_market` (read); `link_market` (agent).
- `docs/OpenAPI.json`: as specified there (`Market`, `MarketPage`, `MarketLink`, `MarketLinkInput`, `ShodanQuery`, `ShodanQueryInput`; forecaster ids; `AttentionTopic.google`); status implemented; pending list updated.
- `docs/Source-Catalog.md` "Opt-in restricted sources": each adapter with route, requirements, terms summary and default egress. `docs/Operator-Guide.md` "Markets and external forecasters": linking markets to questions, how external forecasters are scored and when they join the ensemble.
- AccessPolicy rows. Problem codes: `UNKNOWN_MARKET` (404), `MARKET_ALREADY_LINKED` (409), `UNKNOWN_SHODAN_QUERY` (404).

## Tests to write (do not run)

- `KalshiAdapterTest`, `PolymarketAdapterTest`, `MetaculusAdapterTest`, `ViewsAdapterTest` (WireMock): parsing, fixed-point prices, intervals, linked-market cadence.
- `ExternalForecasterTest`: link orientation, ≥ 0.02 move and daily submissions, `made_at` never before Foresignal saw the price, resolution by the ledger's resolver, ensemble entry only after 30 resolutions.
- `FinanceAdaptersTest`: CME (both routes), ICE, Yahoo, CNN F&G series with `knownAt` and `LOCAL_ONLY` default.
- `AttentionAndRatingsAdaptersTest`: Google Trends both routes and scaling metadata, Electricity Maps, NewsGuard and Ad Fontes providers in 132.
- `ShodanAdapterTest`: saved queries, counts, exposed-service records.
- `MarketsControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Prediction markets, Metaculus, VIEWS, exchange and finance data, Google Trends, Electricity Maps, NewsGuard, Ad Fontes, Shodan and Windy webcams are available as opt-in adapters, off by default.
- Linked markets and forecast platforms contribute scored external forecasts to the ledger and join the ensemble only as their skill earns.
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
2. **Commit** all changes with the message `138: Markets Forecasts And Commercial Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/138-CR-Markets-Forecasts-And-Commercial-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `138: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
