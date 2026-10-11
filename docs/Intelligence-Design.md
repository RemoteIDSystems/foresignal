# Foresignal Intelligence: analysis, rolling hypotheses and forecasting (v1.3 design)

Prepared 2026-10-11, from a survey of the four reference projects' code, a web scan of open-source and commercial tools, and a review of forecasting and intelligence-analysis research (§14).

This document is **authoritative for the intelligence layer**: situations, analytic findings, precursors, base rates and analogs, the forecast ledger, hypotheses and indicators, statistical forecasts and regional outlooks, impact projection, and the daily, weekly and monthly analysis cycles. It extends, and does not replace, prompts 098–104 (entities, derived layers, windowed rules and anomalies, intelligence events and cases, the autonomous analyst, backtesting, infrastructure mapping). Where this document and an older prompt disagree on the intelligence layer, this document wins. `docs/Core-Data-Model.md` still wins on ingest, history, triggers and retention mechanics.

New prompts **116–126** implement it. The former 116–120 are renumbered **127–131** (`docs/Roadmap.md` §5 has the map).

---

## 0. Decisions at a glance

| Question | Decision | Why |
|---|---|---|
| What is the product claim? | Foresignal correlates across domains **over durable, versioned history**, keeps **rolling hypotheses**, and **measures its own accuracy** with leak-free replays. | World Monitor already has LLM briefs, an index, baselines and a Brier-scored forecast store, but 180 days of event history, hand-set weights and no demonstrated skill. Nobody has competing-hypothesis tracking, stored out-of-sample lead–lag tests or point-in-time evaluation (§1). |
| Who finds patterns? | **Deterministic detectors** (statistics over stored data) find and quantify. The LLM explains, challenges, proposes and forecasts, and is scored like any other forecaster. | Reproducible and testable. Research shows LLMs categorise conflict better than they forecast it, and statistical models win on counts (§14.3). |
| How are uncertain judgments expressed? | A numeric **probability** is the source of truth; the ICD 203 term is derived from it. **Confidence** (low, moderate, high) in the evidence is a separate field. Generated text never puts a likelihood term and a confidence level in the same sentence (ICD 203, analytic standard 2 on uncertainty). | The US intelligence community's analytic standard; separates "how likely" from "how good is the evidence". |
| What is a rolling hypothesis? | A **hypothesis set** (competing answers to one key question) whose probabilities move with observed **indicators** and periodic reviews, with every revision kept and explained against the previous one (ICD 203 standard 7). | Each review starts from the last one; the revision history is the audit trail. |
| How is "were we right?" answered? | A **forecast ledger**: questions with machine-checkable resolution, immutable timestamped forecasts from many forecasters (base rates, statistics, precursors, analogs, indicators, the LLM, the operator, an ensemble), automatic resolution, proper scoring (Brier, log, RPS, CRPS), CORP reliability diagrams, skill against base rates, and bias monitors. | Calibration is what makes a probability worth acting on. ACH alone has shown no accuracy benefit in studies, so its output is scored too. |
| Speculative links (unusual oil-futures positioning before a strike) | Allowed as **speculative-link hypotheses**, always shown with the **coincidence baseline** (how often such a move happens with nothing following) and alternatives; capped by evidence; never phrased as an accusation. | Interesting and useful, without overclaiming. Free daily and weekly data cannot see intraday trading, and the UI says so. |
| Outside view on day one | **Reference-history imports** (UCDP GED, ACLED history, USGS ComCat, GDACS, CFTC and PortWatch history) as region-week counts, so base rates exist before Foresignal has a year of its own data. | Explicit base rates are among the strongest accuracy levers measured for LLM and human forecasters. |
| How long is data kept? | **Analytic retention classes** (§4.1): event kinds 5 years, series 10 years, intelligence outputs indefinitely; dense telemetry keeps its short raw window plus rollups and derived records. | The 7-day default was right for storage and wrong for analysis. |
| No look-ahead | Every job and forecast records its **knowledge cutoff** (`asOfSeq`). Replays and retrospective evaluations read through `asOf`. Series points carry `knownAt`; publication lags (COT ≈ 3 days, PortWatch ≈ 7 days) are respected. | Transaction-time history makes leak-free evaluation possible. Competitors cannot do it. |
| LLM forecasts and training leakage | Retrospective evaluation of an LLM forecaster only uses questions opened **after** the model's knowledge cutoff; earlier ones are excluded as `LEAKAGE_RISK`. Live LLM forecasts are capped to [0.03, 0.97] and recalibrated (Platt) on the ledger once 50 have resolved. | Models cannot reliably pretend not to know outcomes; LLM forecasts hedge and lean toward "yes". |
| Statistical code | Apache Commons Statistics 1.3, Commons Numbers and Commons RNG, Hipparchus 4.0.x (all Apache-2.0). CUSUM, BOCPD, ETS, PAV, CRPS and Benjamini–Hochberg are written in-house with golden tests. No GPL (Smile, Weka). Commons Math 3.6.1 is unsupported and not used. | Small, auditable, licence-clean (§12). |
| Charter non-goal "no training models" | Unchanged for neural models and weights. **Fitting statistical models to the operator's own data** (baselines, forecasts, index weights, ensemble weights, recalibration) is in scope. | Clarification. |
| Where in the roadmap | A new block **116–126** after 115, before the adapter doctor and SDKs (so the SDKs include the new API). Nothing earlier is renumbered. | The build takes days, so moving the block earlier gains little. **Collecting early** (§4.4) is what matters. |

---

## 1. Competitive position

### 1.1 What exists (October 2026)

| Capability | World Monitor (AGPL, 88k★) | ShadowBroker (AGPL, 11k★) | Velocity (AGPL) | God's Eye View (MIT, 51k★) | Crucix (AGPL, 12k★) |
|---|---|---|---|---|---|
| LLM briefs | strong; server-checked citations and evidence ids; local-first provider chain | none built in (external agent channel; template summaries) | moderate; strict citation contract, uncited answers withheld; watch-officer agent | basic; 5-word HUD summary, voice agent | LLM triage FLASH/PRIORITY/ROUTINE |
| Baselines and anomalies | Welford mean/variance per type × region × weekday × month, 90 d, z-scores | hard-coded "normal" ranges | 24 h rolling z in memory; DBSCAN pattern of life | none | sweep-to-sweep delta |
| Correlation | 1° geo-convergence (≥ 3 types in 24 h); 11 emitted signal types incl. "prediction leads news", "silent divergence" | 1° grid rules (RF anomaly, military build-up, cascade, "possible contradiction") | incident fusion (≥ 2 domains); deception detection | none | none |
| Indices | Country Instability Index (hand-set weights, 40% editorial baseline), hotspot escalation | 0–100 threat level from sentiment, prediction markets and counts | CII clone; market stress index | none | none |
| Story clustering | Jaccard + embeddings, phase lifecycle | none | basic | none | none |
| Forecasting | deterministic probabilities with domain ceilings, market blending, resolution specs, Brier and Wilson scorecard; **track record withheld (< 30 families) and under audit; no demonstrated skill** | Bayesian per region × domain, weekly frozen predictions, Wilson backtest (off by default) | none | none | none |
| Markets | Finnhub, Alpha Vantage, FRED, EIA, COT, crypto, Polymarket, Kalshi | yfinance, prediction markets | Polymarket display | none | LLM trade ideas |
| History | 180 d of events (no positions), 5k-vector browser memory | ~7 d of snapshots, replay | hours of positions | ~24 h traces | none |
| Hypotheses (ACH), indicators and warnings | none | "possible contradiction" pin only | debias/fact-check pass | none | none |

Commercial tools set the bar for "impressive": Dataminr's self-regenerating event briefs and investigative agents; Windward's unprompted anomaly detection with suggested causes; Babel Street's challenge agents; Primer's claim-level verification; ACLED CAST publishing its forecasts **with** its accuracy metrics.

### 1.2 Where Foresignal wins

1. **Versioned, transaction-time history** of raw and derived state, kept for years for event data: "what did we know at time T" for every replay and evaluation.
2. **Weights and probabilities learned from outcomes:** index weights, ensemble weights and recalibration are fitted to resolved questions; nothing important rests on a hand-set weight forever.
3. **Long-horizon baselines on every layer, with change-point detection,** coverage-aware.
4. **Stored, out-of-sample lead–lag and precursor tests** with false-discovery control, instead of ephemeral correlations.
5. **Competing-hypothesis tracking** with indicators that watch for themselves and revisions explained against the previous judgment.
6. **A forecast ledger with leak-free retrospective evaluation** and an operator-vs-machine scorecard.
7. **Measured alert precision** (103, 115) and measured detector false-alarm rates on every finding.
8. **MIT licence:** every competitor with comparable depth is AGPL.

### 1.3 Must-match list

Foresignal must also match what the best competitors already do. Where each lives:

| Competitor capability | Foresignal |
|---|---|
| Briefs with server-validated citations; local-first providers | 026, 028, 029, 102 |
| Uncited answers withheld; watch officer drafting briefs for ack | 028 verifier, 102 analyst |
| Story clustering with lifecycle | situations (117) with embedding similarity in the linker |
| Seasonal baselines, z-scores, term spikes | 100 anomalies; attention topics (116) |
| Geo-convergence and incident fusion | 101 recipes; co-occurrence mining (118) adds significance testing |
| Deception and jamming detection | 099 |
| Country instability index | regional indices (123), decomposed, **scored against outcomes** |
| Forecasts with resolution specs and Brier scorecard | ledger (121) |
| Markets tied to events, "silent divergence" | event studies, `pre-event-signal` and `unexplained-move` findings (118) |
| Self-regenerating briefs (Dataminr) | situation briefs (117) |
| Unprompted anomalies with suggested causes (Windward) | findings (118) + red team and analyst roles (125) |
| Challenge agents (Babel Street) | red team role (125) |
| Published accuracy next to forecasts (ACLED CAST) | ledger scorecards on every question and outlook (121, 123) |

Excluded on terms or principle: prediction-market prices (Polymarket, Kalshi; 093 §8), exchange data from CME/ICE (licence), social scraping, person-level analysis.

---

## 2. Principles (binding on prompts 116–126)

1. **Evidence, inference, speculation.** Every analytic object separates what was observed (cited records, pinned by `seq`), what a named detector derived, and what is possibility or narrative. Speculation is always labelled. (Extends 101 principle 5 and 102 principle 2.)
2. **Detectors find; models explain.** Statistical detectors are pure functions of stored data, a definition and a detector version. LLM output never creates evidence, never adds members to events or situations, and never changes a deterministic score. An LLM may *propose* (hypotheses, indicators, watches, questions) and may *forecast* as a named forecaster whose record is scored.
3. **Outside view first.** Every probability starts from a stated base rate or reference class (§7); inside-view adjustments are shown as adjustments.
4. **Numbers are the truth; words are derived.** Probabilities are stored as numbers; ICD 203 terms are derived and linted; confidence is a separate field with its basis; likelihood terms and confidence levels never share a sentence.
5. **Everything that predicts is scored.** Any component that outputs a probability about a resolvable future writes it to the ledger (§8). Unscored predictions are not presented as predictions.
6. **Coverage-aware, and coverage-bias-aware.** Absence of evidence counts only when the sources that would have seen it were healthy. Regions with thin reporting (media coverage differs by two orders of magnitude between regions) lower confidence and are flagged `LOW_REPORTING_DENSITY`.
7. **No look-ahead.** Jobs record `asOfSeq`; replays use `asOf`; live use of series respects `knownAt`.
8. **Multiple testing is accounted for.** Mining controls the false-discovery rate and validates survivors on a later, disjoint period.
9. **No accusations, no people.** Analysis covers places, events, vehicles and objects (098), markets in aggregate, and organisations' public actions. Market findings say "unusual positioning before the event", never "insider trading".
10. **Reproducible and versioned.** Every detector, scorer, updater and forecaster has a name and semantic version stored with its outputs.
11. **Budgeted and optional.** Deterministic analysis runs without an LLM. LLM roles are budgeted per run and per day (026) and fall back to deterministic output.
12. **Private stays private.** Anything built from a `private` source (039) is private and follows `EgressPolicy` everywhere, including LLM context.
13. **Licensed data stays local.** Series whose terms forbid redistribution (FRED copyrighted series, EIA spot prices sourced from LSEG, ACLED) are `egress=LOCAL_ONLY`: Foresignal computes on them and serves derived signals, never the raw values, outside the operator's own tokens.

---

## 3. The analytic stack

```
 L8  Analysis cycles ─ daily / weekly / monthly assessments (125)
          ▲ deterministic packet; LLM roles: analyst · red team · forecaster · proposer
 L7  Forecast ledger ─ questions · forecasts · resolution · scores · calibration (121)
          ▲ forecasters: base rate · climatology · persistence · statistical (123) · precursor (119)
          │              analog (120) · impact (124) · indicator-Bayes (122) · LLM (125) · operator · ensemble
 L6  Hypothesis sets ─ competing hypotheses · indicators → watch rules · ACH matrix · revisions (122)
 L5  Memory ─ situations / storylines (117) · base rates · historical analogs (120)
 L4  Discovery ─ change points · event studies · co-occurrence (118) · precursors and lead–lag (119)
          · regional outlooks and indices (123) · impact projection (124)
 L3  Correlation ─ intelligence events · cases · exposure · dependency graph (101, 104)
 L2  Detection ─ derived layers · count / absence / anomaly rules (099, 100)
 L1  Signals ─ records and versions · rollups (038; region rollups 118) · series (093, 116)
 L0  Collection ─ budgeted sources · health · history with analytic retention (001–097)
```

Outputs are ordinary Foresignal objects: typed tables for structure, plus records of a DERIVED source so SSE, tiles, search, triggers, briefings and MCP work unchanged.

| DERIVED source | Kinds | Created by |
|---|---|---|
| `foresignal-intel` (101) | `intelligence-event`, `situation` (117), `hypothesis-set` (122) | 101, 117, 122 |
| `foresignal-analytics` (118) | `analytic-finding`, payload `findingType` ∈ `change-point`, `pre-event-signal`, `unexplained-move`, `co-occurrence`, `precursor-active`, `forecast-exceedance`, `outlook-exceedance`, `projected-impact` | 118, 119, 123, 124 |
| `foresignal-outlook` (123) | `regional-outlook`, `regional-index` | 123 |

Forecast questions and forecasts are **not** records: they are append-only ledger rows with their own API and SSE topic.

---

## 4. Data foundations

### 4.1 Analytic retention classes

`docs/Conventions.md` "Analytic retention" makes these the defaults adapters set through `defaultRetention` / `kindRetention` (the 019 resolver order is unchanged). An adapter prompt that names a longer window keeps it; the operator can always override.

| Class | Examples | Default raw retention | Kept beyond raw |
|---|---|---|---|
| `event` | earthquakes, weather alerts, conflict events (ACLED, UCDP), outages, NOTAMs and TFRs, sanctions changes, filings, disasters, cyclones, launches, KEV entries, travel advisories, nav warnings | **1825 d** | rollups |
| `bulk-event` | GDELT events, FIRMS pixels, GLM lightning, CT-log hits, PSKReporter spots, Wikipedia edit surges | 90 d | rollups, derived records |
| `series` | `series-point` (energy, markets, attention, chokepoint transits, gauges) | **3650 d** | n/a |
| `state` | SAMPLED status (gauges, sensors, station health) | 365 d | rollups |
| `track` | TRACK tier (aircraft, vessels, satellites) | `trackWindow` (3 d) plus 044 downsampling | rollups, PoL (114), derived layers (099), entity registry (098) |
| `reference` | catalogues (power plants, ports, cables) | 90 d of history; heads kept while current | n/a |
| `intel` | Foresignal's outputs: anomalies, intel events, situations, findings, outlooks | records 1825 d; **typed tables never auto-deleted** (admin purge only) | n/a |

Event-class sources are small (USGS ≈ 300 versions/day, ACLED ≈ 1–3k/day, IODA a few hundred). Five years of all `event` sources is expected in the low tens of GB; `/v1/system.history` reports the real projection and 130 measures it.

### 4.2 Region rollups

038's `activity_rollup` is keyed by H3 cell (res 4): hourly rows for 35 d, daily rows for 5 years. Prompt 118 adds region resolutions to the same table:

| `resolution` | Bucket | Key | Retention |
|---|---|---|---|
| 2 | hour | `(source_id, kind, region)`; `region` is the 040 country code, or `CC-admin1` when the source opts in (`statsRegionLevel=admin1`) | 1095 d |
| 3 | day | same | 3650 d |

A nullable `region_code` text column is added; `cell` holds a stable 64-bit hash of the region code for these rows. Same cursor discipline as 038 (durable cursor, closed buckets only, consumer clamp), with its own cursor `stats.region.cursor` that starts after 040 has tagged records.

### 4.3 Series

093 defines series as `series-point` records with `GET /v1/series` and `GET /v1/series/{sourceId}/{seriesId}`. The intelligence layer reads three families through one `SeriesReader` (118):

| `SeriesRef` | Backing data | Example |
|---|---|---|
| `series:{sourceId}/{seriesId}` | `series-point` records (093, 116) | `series:cftc-cot/067651.disagg.mm_net` |
| `rollup:{res}/{sourceId}/{kind}/{region\|cell}` | `activity_rollup` rows | `rollup:day/acled/conflict-event/YE` |
| `rule:{ruleId}/{groupKey}` | `activity_sample` (100) | `rule:01J…/YE` |

Every point carries `periodStart` (event time), `knownAt` (ingest time of the version that supplied it) and `coverageOk`. **Event studies align on event time; live detection and forecasting only use points whose `knownAt` ≤ the decision time.**

### 4.4 Collect early

History cannot be backfilled from live feeds. The roadmap recommends deploying a collecting instance on the bench server **after prompt 022** with event-class sources enabled, upgrading it as the build continues (`ddl-auto=update` handles additive schema). The intelligence block then lands on weeks of real history, and reference imports (§7.2) supply the long outside view.

---

## 5. Discovery (prompts 118, 119)

### 5.1 Analytic findings

`analytic_finding` is the shared table for what discovery produces:
- `id` (ULID), `finding_type`, `title`, `severity` (info | notable | significant), `status` (open | superseded | dismissed);
- `geom` (nullable), `region_codes`, `window_start`, `window_end`, `detected_at`;
- `series_refs`, `members` (pinned refs, as 101), `asof_seq`;
- `stats` (method numbers), `base_rate` (how often a finding this strong happens by chance in this series, and how that was measured), `caveats` (103 codes plus `LAGGED_PUBLICATION`, `FEW_EVENTS`, `MULTIPLE_TESTS`, `LOW_REPORTING_DENSITY`, `DAILY_RESOLUTION_ONLY`);
- `detector` `{name, version, params}`; links `situation_id`, `precursor_id`, `question_ids`;
- `explanation` (deterministic template), `private`.

Each finding is also an `foresignal-analytics` record, so ordinary rules can alert on it.

### 5.2 Change points

Two detectors on registered analysis series (§5.6):

- **`changepoint-cusum` 1.0.0.** Counts: Poisson/NB CUSUM `S = max(0, S + x − k)` against a time-varying in-control mean μ₀ from the series baseline (100's slot model, or a Farrington-style NB fit on comparable past periods), with `k = (μ₁ − μ₀) / ln(μ₁/μ₀)` for a shift to `μ₁ = μ₀ · (1 + minRelShift)` (default 0.5). Continuous series: standardised CUSUM on robust residuals (median and MAD). The alarm threshold `h` is chosen per series by simulation for an in-control average run length of 365 buckets. Confirmation requires the post-change level to persist for `minPersistence` buckets (3). Coverage gaps cancel a candidate (state `unknown`).
- **`changepoint-bocpd` 1.0.0.** Bayesian online change-point detection (Adams & MacKay) with a Poisson–Gamma model for counts (Normal–Gamma for continuous), hazard `1/250`, run lengths truncated at 300. It reports the posterior probability of a change in the last `k` buckets; ≥ 0.8 confirms a CUSUM candidate as `significant`.
- Output: a `change-point` finding with the change time, before and after levels, effect size, detection delay, and the false-alarm rate of the same settings on the series' own history.

Example: Bab el-Mandeb daily transits (PortWatch, 088) fall from a 60-day level of 72 to 31 for five days → a significant change point: "a change this large was flagged once in the previous 700 days of this series".

### 5.3 Event studies, pre-event signals and unexplained moves

`EventStudy` (`event-study` 1.0.0) answers "did series S move unusually around events of type E?" and, live, "is S moving unusually right before or after this event, or with no event at all?".

- **Events:** a selector, as in 103 (intel event type, alert rule, anomaly classification, change point, situation activation, explicit timestamps).
- **Transforms:** prices → log returns (constant-mean model for futures); volume, open interest and attention → `log(1 + V)` minus the estimation-window mean, standardised; net positions → weekly differences, standardised; counts → Anscombe-transformed residuals against the baseline.
- **Windows** (in the series' own frequency): estimation `[−250, −11]` trading days (or as much as exists, minimum 60, else `FEW_EVENTS`); pre-event `[−5, −1]`; event `[0, +1]`. Weekly series (COT): estimation `[−52, −3]` weeks, pre-event `[−2, −1]`.
- **Statistics:** abnormal values, cumulative abnormal value per window (CAR), cross-event average (CAAR); significance by the standardised test, the **Corrado rank test**, and a **placebo test** (the same statistic at 500 deterministic pseudo-event dates from periods without qualifying events). Overlapping events are clustered before averaging. The placebo distribution is the **coincidence baseline** shown with every live signal.
- **Live `pre-event-signal`:** when a qualifying event arrives (default: intel events ≥ warning, situations turning active, significant change points), the configured **watch pairs** (event family × series) are checked over the pre-event window using only points known before the event. Placebo p ≤ 0.01 and |CAR| ≥ `minEffect` → finding: "WTI managed-money net long rose 2.8 SD in the 2 weeks before the event; moves this large occur before about 3% of comparable windows with no event."
- **Live `unexplained-move`:** a daily scan of watched market and attention series. A move with |z| ≥ 3 against the estimation window and **no** qualifying event in the linked regions or themes within ±2 days becomes an `unexplained-move` finding. If a qualifying event appears within the following 7 days, the finding is linked to it and re-labelled as an observed pre-event signal; that link feeds precursor mining (119).
- **Resolution limit:** Foresignal's market data is daily or weekly. The documented 2026 oil-futures episodes were visible minute by minute in licensed exchange data; Foresignal cannot see moves like those, and every market finding carries `DAILY_RESOLUTION_ONLY`.
- Batch studies run on the 103 job framework (`type = event-study`) with its caveats and no-look-ahead rules.

### 5.4 Co-occurrence mining

`CoOccurrenceMiner` (`cooccurrence` 1.0.0) looks for combinations nobody wrote a recipe for.

- Daily, over region-day rollups: for every region with activity and every pair of **signal families** (`signal_family` registry: `ais-behaviour`, `aviation-military`, `conflict`, `outage`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`), test whether both are above their own baselines on the same day.
- Each family's daily exceedance p-value comes from its NB baseline; the joint statistic is Fisher's combination; the expected joint exceedance rate comes from each family's historical exceedance frequency (independence baseline), and the reported lift is observed ÷ expected joint days over the trailing year.
- Benjamini–Hochberg at `q = 0.05` across all region × pair tests of the day.
- Survivors become `co-occurrence` findings and are offered to the situation linker (117).

### 5.5 Precursors and lead–lag (119)

103's "does X precede Y?" object is renamed **precursor test** in v1.3 (the word "hypothesis" is reserved for §6). Prompt 119 adds discovery and validation:

1. **Event-stream candidates:** operator-registered family pairs, plus automatic pairs among the top-K event families per region (intel event types, alert rule tags, anomaly classes, change points by series family, `unexplained-move` findings, reference-import families), capped by `analysis.precursors.maxTestsPerRun` (500). Tested with 103's circular-shift tester.
2. **Series candidates:** pairs of analysis series (e.g. attention ↔ conflict counts, positioning ↔ chokepoint transits). Pre-whiten each by its baseline residuals; statistic = max |cross-correlation| over lags `1…L` (antecedent leading only); null from circular block-shift surrogates with block length ≥ the residual autocorrelation length.
3. **FDR:** Benjamini–Hochberg at `q = 0.10` per batch (Benjamini–Yekutieli when the batch contains overlapping series).
4. **Validation:** survivors are re-tested on the disjoint later window (newest third). Only patterns that are again notable with lift ≥ 1.5 (event streams) or the same sign and p ≤ 0.05 (series) become `validated`.
5. **Library:** `precursor` rows hold selectors, spatial rule or series pair, lag distribution, hit rate with Wilson interval, base rate, lift, n, both windows' statistics, FDR batch size, `status` (candidate | validated | stale | retired), `last_tested_at`. A monthly re-test retires patterns whose lift falls below 1.2 or whose interval includes the base rate.
6. **Live watch:** when a validated precursor's antecedent occurs, record a `precursor-active` finding; 121 then opens a forecast question for the consequent with the precursor forecaster's probability (hit rate shrunk toward the base rate by sample size).

### 5.6 Analysis series registry

`analysis_series(ref, enabled, detectors[], frequency, origin auto|operator, watch_pairs, created_at)`. Nightly auto-registration: region-day rollups with ≥ 30 non-zero days in the last 90; every `series-point` series from 093 and 116; PortWatch chokepoints; anomaly rule groups. Caps keep runs bounded (`analysis.series.max`, default 2,000).

---

## 6. Memory and reasoning (prompts 117, 122)

### 6.1 Situations (storylines)

A **situation** is a persistent storyline grouping related evidence over days to months.

- `situation` (theme, status `emerging | active | dormant | closed`, severity, regions, geometry, topics, entities, deterministic summary, optional LLM brief), `situation_member` (intel events, findings, alerts, anomalies, records, cases, each with `join_reason` and corroboration status), `situation_snapshot` (one per day, so "what changed since" is a diff), `situation_log`.
- **Linker** (`situation-linker` 1.0.0), deterministic: a scored match on shared region, distance, shared entity, topic keywords, **embedding similarity** to the situation summary (027), theme compatibility and recency; below the threshold, a significant input seeds a new `emerging` situation. Every link stores its score components.
- **Self-refreshing brief** (optional): regenerated when a situation activates, escalates or gains ≥ 3 core members, debounced and budgeted, validated like 102 output; it never changes members or status.
- Corroboration (099 `derived-corroboration`) is rolled up per situation: a storyline built only on uncorroborated text reports says so.

### 6.2 Hypothesis sets (rolling hypotheses)

- `hypothesis_set`: the **key question**, `kind` (`exclusive`: probabilities sum to 1 with an "other / none of these" member; or `independent`), `cadence` (daily | weekly | monthly review), `horizon`, `status`, `situation_id`, `question_id` (the ledger question that resolves it), `origin`.
- `hypothesis`: `statement`, `type` (`outcome` | `explanation` | `speculative-link`), `prior` with `prior_basis` (base rate id, analog set, or operator judgment with rationale), `probability`, derived `estimative_term`, `confidence` (low | moderate | high) with `confidence_basis`, `key_assumptions`, `status`.
- `hypothesis_revision`, append-only: before and after, `cause` (indicator-observed | indicator-absent | evidence-added | base-rate-refresh | review | operator), `actor`, `asof_seq`, citations, and a **change rationale** stating what moved the judgment relative to the previous revision.

**Speculative-link hypotheses** need the linked findings, the coincidence baseline from the event study's placebo distribution, and at least two alternatives (for the oil case: "scheduled news or inventory data", "contract roll or rebalancing", "coincidence at the base rate"). Their probability may not exceed the bound implied by the evidence and the baseline without an operator override (logged).

### 6.3 Indicators, ACH and key assumptions

- `indicator`: description; `signal` (a trigger rule spec (017/100), a validated precursor, a series condition or change point, an intel event type in a region, a situation status, a forecast resolution, or `manual`); optional `expected_by`; `signal_family`; credibility and relevance (H/M/L); and per hypothesis a **likelihood ratio** `LR(H) = P(observed | H) / P(observed | not H)` from its diagnosticity class (high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0) or set explicitly with a rationale. `absence_lr` applies when `expected_by` passes with coverage ok and nothing observed.
- Each non-manual indicator owns a **watch rule** tagged `indicator:{id}`, so indicators watch for themselves through the existing trigger engine. States: `pending | emerging | observed | absent | unknown-coverage | retired`.
- **ACH matrix:** evidence × hypotheses with ratings CC, C, N, NA, I, II (derived from LRs or set manually). Inconsistency score per hypothesis `−Σ w·inc` with I = 1, II = 2 and `w` = credibility × relevance (L 0.5, M 1, H 2). Non-diagnostic rows (same rating everywhere) are flagged. The matrix is an audit trail; the probabilities it feeds are what get scored.
- **Key assumptions check:** text, `linchpin` flag, rationale, what would make it wrong, status (solid | caveated | unsupported | broken), linked indicators, `review_due`. A broken linchpin flags the set for review and lowers confidence one step.

### 6.4 Updating and wording

`HypothesisUpdater` (`indicator-bayes` 1.0.0), deterministic:
1. Work in log-odds; an observed or absent indicator adds `ln(LR)` per hypothesis.
2. Correlated evidence: within one `signal_family`, only the strongest indicator per day counts.
3. Damping: at most 1.5 logits of change per hypothesis per day; probabilities clamped to [0.02, 0.98] until resolution.
4. `exclusive` sets are renormalised; `independent` sets are not.
5. A revision is written and, for sets with a ledger question, a forecast from forecaster `indicator-bayes`.

ICD 203 likelihood scale (one row of terms only; never mixed):

| Term | Range |
|---|---|
| almost no chance | 1–5% |
| very unlikely | 5–20% |
| unlikely | 20–45% |
| roughly even chance | 45–55% |
| likely | 55–80% |
| very likely | 80–95% |
| almost certain | 95–99% |

A probability crossing a band boundary is a **band change**, published on SSE and to channels (rate-limited per set). One `EstimativeLanguage` utility (121) maps numbers to terms and **lints** generated text: a term that disagrees with its number, a mix of term rows, or a likelihood term and a confidence level in one sentence is rejected and regenerated or downgraded.

### 6.5 Drafting

`POST /v1/hypotheses/sets/draft {text, situationId?}` has the LLM draft a set from plain language ("watch whether the Red Sea situation spreads to Hormuz"): competing hypotheses including "other", priors with their basis (it must call `get_base_rate` and `find_analogs`), indicators as validated rule specs, key assumptions. The draft is returned for confirmation, never created automatically.

---

## 7. Base rates and analogs (prompt 120)

### 7.1 Base-rate library

`base_rate`: definition (event family, region, window length, optional condition), rate (events per window, or P(≥ 1 per window)), Jeffreys interval, `n_windows`, `n_events`, period, `source_mix` (own history vs reference import), caveats, `computed_at`, version. Rates for families with enough data are maintained automatically per region; operators and agents can define custom ones. Refreshed monthly by the monthly cycle.

### 7.2 Reference-history imports

`reference_history(family, region_code, week_start, count, source, licence)` keeps long histories in rollup form only:

| Dataset | Span | Licence / egress |
|---|---|---|
| UCDP GED (annual + candidate) | 1989– | CC BY 4.0 |
| ACLED history (API date ranges, operator credentials) | by region, 1997–/2018– | ACLED EULA; `LOCAL_ONLY`; never redistributed |
| USGS ComCat (FDSN bulk) | decades | public domain |
| GDACS archive | 2000s– | open, attribution |
| CFTC COT history (Socrata, 116) | 1986– | public domain |
| IMF PortWatch history (088) | 2019– | IMF terms: reuse with attribution |

### 7.3 Historical analogs

- **Signature:** standardised member counts per signal family over the first N days (7), escalation slope, theme, region class (040), entity classes, plus the 027 embedding of the deterministic summary.
- **Search:** reciprocal-rank fusion of feature cosine and embedding cosine, excluding time overlap; top k (10) with **outcomes** (duration, peak severity, follow-on event families within 7 and 30 d).
- **Analog forecaster:** outcome frequencies among analogs, shrunk toward the base rate by analog count.

---

## 8. Forecast ledger (prompt 121)

### 8.1 Objects

- `forecast_question`: `type` (binary | multiple | ordered | numeric), outcomes or bins, unit and bounds, `resolution_criteria` text **and** a machine `resolver` (or `manual`), `opens_at`, `closes_at`, `resolve_by`, `status` (open | closed | resolved | annulled), `resolution`, `category`, links (situation, hypothesis set, precursor, finding, series, outlook), `origin`, `private`.
- `forecast`, append-only: `forecaster_id`, `made_at`, `asof_seq`, `probability` / `distribution` / `quantiles` (p05…p95), `rationale` (typed claims for LLM forecasters), `citations`, `inputs` (base rate ids, analog ids, model metadata), `supersedes_id`.
- `forecaster`: `type` (`base-rate | climatology | persistence | statistical | precursor | analog | impact | indicator-bayes | llm | operator | external | ensemble`), `name`, `config`, `version`, `active`.

### 8.2 Resolution

Machine resolvers (hourly job; early resolution when an event makes the answer certain): `event-exists`, `count-threshold`, `series-value`, `alert-fired`, `situation-status`, `hypothesis-outcome`. A resolver that cannot evaluate because coverage was broken resolves `annulled` with the reason (not scored). Manual questions notify the operator at `resolve_by`. **Resolution criteria are shown verbatim to every forecaster**, because misreading them is the most common catastrophic forecasting error.

### 8.3 Scoring, calibration and bias

`ForecastScorer` (1.0.0):
- **Brier** (binary; multi-category as Σₖ(pₖ − oₖ)², range 0–2) with Murphy decomposition; **RPS** for ordered questions; **log score** clipped to [0.01, 0.99]; **CRPS** for numeric (sample form, or mean pinball loss over quantiles); interval coverage.
- **Time-averaged:** a forecaster's standing forecast is scored every day the question is open and carried forward until updated (Good Judgment convention).
- **Skill:** `1 − S / S_ref` against an **out-of-sample** base rate for the category (binary, multiple), or seasonal naive / climatology (numeric), on the same questions. Pairwise comparisons only on shared questions, with a paired bootstrap interval.
- **Calibration:** CORP reliability diagrams (isotonic regression by pool-adjacent-violators) with the CORP decomposition; 10-bin tables with counts as a fallback for small n; sharpness.
- **Bias monitor:** per forecaster and category, mean forecast minus observed frequency (catches LLM "acquiescence" and escalation bias), and the share of forecasts in [0.4, 0.6] (hedging).
- Labels: `insufficient` under 20 resolved questions.

### 8.4 Ensemble and recalibration

- Aggregation: weighted **mean of log-odds** (binary, multiple), quantile averaging (numeric).
- Weights `w ∝ exp(−η · relative Brier)` over the last 100 resolved questions in the category, shrunk toward equal by `n / (n + 20)`; stale forecasts decay.
- **Extremizing** off by default; enabled only when ≥ 5 forecasters with partly independent inputs are active and a fitted `a > 1` improves out-of-sample log score over ≥ 100 resolutions (monthly check). Output capped at 1%/99%.
- **Platt recalibration** per forecaster once it has ≥ 50 resolved forecasts in a category; the ensemble uses recalibrated inputs.
- **LLM caps:** LLM forecasts are clamped to [0.03, 0.97] before recalibration.

### 8.5 Retrospective evaluation

`POST /v1/forecasts/evaluations {forecasterId, from, to, categories}` replays question opening and forecasting over a past range on the 103 job framework, with every tool forced to `asOf` the question's open time. Statistical forecasters are fully valid. LLM forecasters only use questions opened after the provider's `knowledgeCutoff`; earlier ones are counted under `LEAKAGE_RISK` and skipped. Results are scorecards marked `retrospective`.

### 8.6 External and human forecasters

- The **operator** is a forecaster (API, MCP, chat commands from 061), scored on the same scale; the weekly scorecard shows operator vs machine.
- **External** forecasters (`type=external`) are allowed only from sources whose terms permit storage. Metaculus and prediction markets are excluded until written permission exists; VIEWS forecasts are a candidate once their data licence is confirmed (§16).

---

## 9. Regional outlooks, indices and series forecasts (prompt 123)

### 9.1 Series forecaster

`SeriesForecaster` (`series-forecast` 1.0.0) for registered analysis series: seasonal naive (benchmark), ETS(A,N,N) and ETS(A,A,A) on `log1p` (parameters by maximum likelihood with Hipparchus), an NB GLM (`log μ = trend + day-of-week + Fourier(annual) + lagged counts`, prediction intervals from NB quantiles with a parameter bootstrap), and a damped trend for continuous series. Method choice per series by rolling-origin backtest over the last 8 origins, minimising mean pinball loss; choice and backtest score stored. Horizons 1, 7 and 28 days; quantiles p05–p95. Served at `GET /v1/series/forecast?ref=`; actuals outside the 1-step p05–p95 band become `forecast-exceedance` findings.

### 9.2 Regional outlooks (CAST-style)

For each enabled region (country, or admin1 where data supports it) and **event family** (battles, explosions/remote violence, violence against civilians, protests from ACLED/UCDP; outages; severe weather alerts; earthquakes M4.5+), a weekly job publishes quantile forecasts of event counts for the next six rolling 4-week windows. Each outlook is a `regional-outlook` record **and** a numeric ledger question per window, so its accuracy is published next to it automatically. Inputs: the region's own history (and reference imports), seasonality, recent trend, neighbouring regions' counts, and the region's index (§9.3).

### 9.3 Regional indices, scored

`regional_index` definitions (built-ins and operator-defined): named components (each a series ref with a transform and normalisation), weights, floors, and a target (the outlook family it should anticipate). Every index value is a `regional-index` record with its **decomposition** (each component's contribution) and input provenance.

Unlike every competitor's index, weights are not left hand-set: a monthly job fits component weights by regularised Poisson or NB regression of the next-4-week target count on the components (Hipparchus, L2 penalty, rolling origin), reports out-of-sample skill against the seasonal-naive outlook, and **proposes** the new weights (applied by an operator or an agent with `allowActions`). The index page always shows its current out-of-sample skill.

Optional structural covariates come from World Bank indicators (116).

---

## 10. Impact projection (prompt 124)

`ImpactProjector` (`impact-projection` 1.0.0) turns hazard forecasts Foresignal ingests into probabilistic, scored impact statements:

| Hazard input | Probability used |
|---|---|
| NHC wind-speed probabilities and cones (067, 075) | published probabilities; the cone is described as covering about two thirds of historical track errors and is never a probability on its own |
| SPC outlooks (047) | published probabilistic outlooks; categorical risk mapped to its published ranges |
| WPC excessive rainfall (089) | published category ranges |
| NWPS flood forecasts (079) | forecast category at the gauge, calibrated by the gauge's historical exceedance |
| Storm tracks (105) | extrapolation with the tracker's position error |

For each hazard forecast it intersects the area with places (036), infrastructure (104 paths with weakest-edge labels) and exposure (101), and estimates `P(impact) = P(hazard at asset) × P(impact | hazard class)`, with the conditional rate taken from base rates (120). Each statement becomes a `projected-impact` finding and a binary ledger question ("Will an outage be reported in Harris County, TX, by 18:00Z tomorrow?") resolved by the matching sources (068, 092, 087). Labels: "modelled; heuristic links may be wrong; not an official warning".

---

## 11. Analysis cycles and assessments (prompt 125)

### 11.1 Cycles

`analysis_cycle`: `cadence` (daily | weekly | monthly), schedule (cron + IANA tz; defaults 06:00 daily, Monday 07:00 weekly, the 1st 08:00 monthly), `scope` (regions, themes, situations, hypothesis sets, sources), `analyst_profile_id` (102: model, effort, budgets, `allowActions`), `roles` (analyst, red-team, forecaster, proposer), `channel_ids`, `enabled`. Scheduling follows 029/102 (persisted `next_run_at`, missed runs run once).

### 11.2 A cycle run

1. **Deterministic packet** (always): since the previous run of the same cycle, situations new, escalated, dormant or closed (snapshot diffs); hypothesis revisions, band changes, indicators observed or absent, broken assumptions; findings by type with base rates and caveats; forecasts due, resolved and opened; scorecard changes; coverage caveats.
2. **LLM roles** (when configured and in budget), each a 028/102 tool loop with verified citations and typed claims:
   - **Analyst:** 3–7 key judgments, each with a probability (term derived), a separate confidence statement and citations; explains change against the previous assessment.
   - **Red team:** for the top hypothesis sets, the strongest case for the runner-up hypothesis, a search for disconfirming evidence, the assumptions most likely to be wrong, and a premortem; its probabilities are written to the ledger as forecaster `llm-redteam:{profile}` so the challenge is scored too.
   - **Forecaster:** for open questions in scope, an **outside-view** pass (base rate and analogs only) and an **inside-view** pass (adjustments with citations), with the resolution criteria quoted verbatim; `samples` independent runs (default 3) aggregated by median log-odds; written as forecaster `llm:{profile}`. Each configured provider can run as its own forecaster, so the ensemble can mix models.
   - **Proposer:** new hypothesis sets, indicators, watches, questions and situation merges, as 102 proposals.
3. **Assessment** (always; digest-only without an LLM), structured to the ICD 203 standards: bottom line up front; what changed since the previous assessment of this cadence (linked); situations and trajectories; hypotheses table (probabilities, terms, change, drivers); indicators to watch next period; forecasts and the scorecard; **speculation** separately; sources, coverage and caveats. Footer: "Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labelled. Verify before acting."
4. **Delivery:** channels (018), the 033 inbox, HTML and markdown (029 renderer), embedding (027), SSE topic `assessments`, MCP.

### 11.3 What each cadence adds

| Cadence | Work |
|---|---|
| Daily | situation and `daily` set reviews; new findings; forecasts due within 7 days |
| Weekly | plus `weekly` sets; calibration scorecard; precursor library changes; regional outlook summary; proposals of sets for active situations without one |
| Monthly | plus `monthly` sets; base-rate refresh (120); precursor re-test (119); ensemble weights, extremizing and Platt refits (121); index weight proposals (123); stale set archiving; 90-day outlook; retrospective evaluation of the LLM forecaster on the past month where valid |

### 11.4 The intel board

`GET /v1/intel/board` (≤ 8 KB, ETag, like 038's glance): top situations, band changes in the last 24 h, indicators observed, forecasts due this week, the latest assessment of each cadence, and the headline calibration figure.

---

## 12. Interfaces and libraries

| Area | REST (new) | SSE topic | MCP (read / agent) |
|---|---|---|---|
| Markets and attention (116) | `/v1/attention/topics/**`; series via `/v1/series` | n/a | `list_attention_topics` |
| Situations (117) | `/v1/situations/**` | `situations` | `list_situations`, `get_situation`, `get_situation_changes` / `create_situation`, `link_to_situation`, `merge_situations` |
| Findings (118) | `/v1/analytics/findings/**`, `/v1/analytics/series/**`, `/v1/analytics/event-studies` | `analytics` | `list_findings`, `get_finding`, `get_series_data` / `run_event_study` |
| Precursors (119) | `/v1/precursors/**`, `/v1/precursor-tests/**` (103) | `analytics` | `list_precursors` / `mine_precursors` |
| Base rates, analogs (120) | `/v1/base-rates/**`, `/v1/situations/{id}/analogs`, `/v1/reference-imports/**` | n/a | `get_base_rate`, `find_analogs` |
| Ledger (121) | `/v1/forecasts/**` | `forecasts` | `list_questions`, `get_question`, `get_scorecard` / `create_question`, `submit_forecast` |
| Hypotheses (122) | `/v1/hypotheses/sets/**` | `hypotheses` | `list_hypothesis_sets`, `get_hypothesis_set` / `create_hypothesis_set`, `add_evidence`, `draft_hypothesis_set` |
| Outlooks and indices (123) | `/v1/series/forecast`, `/v1/outlooks/**`, `/v1/indices/**` | `analytics` | `get_series_forecast`, `get_outlook`, `get_index` |
| Impact (124) | `/v1/analytics/impacts/**` | `analytics` | `list_projected_impacts` |
| Cycles (125) | `/v1/analysis/cycles/**`, `/v1/assessments/**`, `/v1/intel/board` | `assessments` | `get_assessment`, `get_intel_board` / `run_cycle` |

All read tools join the read catalogue (025), so Ask, the analyst and the cycles can use them. Every LLM-spending path is `alwaysAuthenticated`.

Notification event types (018): `situation.activated`, `situation.escalated`, `hypothesis.band_changed`, `indicator.observed`, `assumption.broken`, `forecast.resolved`, `finding.significant`, `assessment.published`. Defaults: weekly assessment and band changes on, the rest off.

**Libraries** (verified 2026-10): Apache Commons Statistics 1.3 (distributions, descriptive statistics, inference), Commons Numbers and Commons RNG, Hipparchus 4.0.x (optimisers for ETS maximum likelihood and NB/Poisson regression; confirm the LICENSE file is Apache-2.0). Optional: ServiceNow `stl-decomp-4j` (Apache-2.0) for STL. Written in-house under `ai.foresignal.analytics.math` with golden-value tests: CUSUM, BOCPD, ETS, PAV, CRPS, RPS, Corrado rank test, block surrogates, Benjamini–Hochberg/Yekutieli, Wilson and Jeffreys intervals, Platt scaling. Excluded: Smile (GPL-3.0 / commercial), Weka, JSAT, jdistlib (GPL), Commons Math 3.6.1 (unsupported).

---

## 13. Worked example: positioning, strike, response

The owner's scenario of 9–11 October 2026, used to illustrate the mechanics (Foresignal has not verified the events): heavy oil-futures buying on Friday 9 October, a Houthi strike on Riyadh on Saturday 10 October, and an expected Saudi or coalition response. A documented precedent exists: in March and April 2026 large oil-futures trades minutes before US–Iran announcements drew CFTC and DOJ inquiries (§14.4).

1. **Signals.** ACLED/UCDP and GDELT (081, 066) mark the strike; NOTAM/TFR changes (087), AIS behaviour near Bab el-Mandeb (099, 088) and GPS-jamming cells (099) add context; EIA daily prices (093), FRED volatility (116, local only) and CFTC managed-money positions (116) give the market side; GDELT timelines and Wikipedia pageviews for the topic (116) give the attention side.
2. **Correlation.** A 101 recipe creates an intel event; the 117 linker attaches it to the open "Red Sea / Gulf escalation" situation.
3. **Discovery.** The 118 event study checks the watch pairs for the conflict family. Daily price and volatility can show an abnormal Friday; the positioning check completes when COT data for the week lands, flagged `LAGGED_PUBLICATION`. Any finding carries the coincidence baseline and `DAILY_RESOLUTION_ONLY` (a one-minute burst of trading would be invisible at daily resolution). If the market moved first with no event yet, Friday's `unexplained-move` finding links to the strike when it happens.
4. **Hypotheses.** The weekly cycle (or the operator) opens two sets:
   - *Outcome:* "Saudi or coalition kinetic response against Houthi targets within 7 days?" (exclusive: within 72 h, 3–7 days, none in 7 days, other). Prior from reference-import base rates for responses to comparable strikes and from analogs; indicators: military air and tanker activity (099), airspace NOTAMs (087), naval co-occurrence (099), attention spikes (116). Resolver: `event-exists` on ACLED/UCDP events of the response type.
   - *Speculative link:* "The pre-strike positioning reflected advance knowledge", with alternatives "scheduled news or inventory data", "contract roll or rebalancing", "coincidence at the base rate"; bounded by the placebo baseline.
5. **Ledger.** The outcome question gets forecasts from base-rate, analog, indicator-Bayes, LLM and red-team forecasters, and the ensemble; when ACLED reports a response (or the window passes) it resolves and every forecaster is scored.
6. **Over months,** precursor mining (119) tests whether `unexplained-move` and `pre-event-signal` findings precede Gulf escalations at all, with FDR control and out-of-sample validation. A real pattern becomes a validated precursor and a live forecaster; a spurious one is retired, which is just as useful to know.

---

## 14. Research addendum (2026-10-11)

**[V]** verified against a primary source today; **[S]** secondary sources; **[U]** unverified.

### 14.1 Competitors
From the reference repositories' code (paths are in the survey notes kept outside the repo) and their public pages: World Monitor (github.com/koala73/worldmonitor, AGPL-3.0-only with a commercial option; its paid tier sells "intel memory" and similar-event lookup); ShadowBroker (github.com/BigBodyCobain/Shadowbroker, AGPL-3.0); Velocity (github.com/AndrewCTF/velocity, AGPL-3.0+); God's Eye View (github.com/bilawalsidhu/gods-eye-view, MIT); Crucix (github.com/calesthio/Crucix, AGPL-3.0). WorldWideView is Elastic-licensed (source-available). Commercial: Dataminr ReGenAI and Intel Agents (dataminr.com/press), Windward Early Detection (windward.ai), Babel Street Insights Investigator challenge agents (May 2026), Seerist (June 2026), ACLED CAST methodology (acleddata.com/methodology/cast-methodology), VIEWS (views_pipeline code is CC BY-NC-SA 4.0, so not reusable here).

### 14.2 Design references (MIT/BSD; reference only under Foresignal's reuse policy)
Intel Workbench (ACH with weighted evidence, ICD 203 bands; MIT), Fatebook (forecast ledger with Brier and calibration charts; MIT), Metaculus platform (scoring module; BSD-2-Clause), Metaculus forecasting-tools (bot framework; MIT), Open Synthesis (collaborative ACH; AGPL, archived).

### 14.3 Forecasting research
- Halawi et al. 2024 (arXiv 2402.18563): retrieval-augmented LLM forecasting near crowd level; blending with the crowd beats both; models hedge away from extremes.
- Schoenegger et al., "Wisdom of the Silicon Crowd" (arXiv 2402.19379): a 12-model ensemble matched 925 humans; acquiescence bias (mean forecast above 50%).
- ForecastBench and the Forecasting Research Institute (2026): the gap to superforecasters has narrowed to near parity on some question sets; disputed by Good Judgment on market questions.
- Metaculus FutureEval analyses (2026): frontier models at high effort, multiple research sources, ensembles, Platt scaling, **capping extreme forecasts** and **explicit base rates** correlated with accuracy; misreading resolution criteria caused the worst misses.
- AIA Forecaster (arXiv 2511.07678): agentic search plus calibration; blended with markets it beats markets alone.
- Leakage: Paleka et al. (ICLR 2026, arXiv 2506.00723) on logical leakage and unreliable date-restricted retrieval; "Simulated Ignorance Fails" (arXiv 2601.13717).
- Conflict: "LLMs do not forecast conflict, they categorize it" (arXiv 2607.00018): a simple statistical model beat GPT-4o and Llama-70B, and adding ACLED context hurt in under-covered zones; CSIS on model escalation bias.
- PRISM (ACL 2026): giving an LLM explicit hypothesis structure accounted for most of its accuracy gain, which supports feeding hypothesis sets to the LLM forecaster.

### 14.4 Methods
ICD 203 (archive.dni.gov/files/documents/ICD/ICD-203.pdf) [V]: nine tradecraft standards; the two-row likelihood table; no confidence level and likelihood term in one sentence. CORP reliability diagrams (Dimitriadis, Gneiting & Jordan 2021, arXiv 2008.03033) [V]. BOCPD (Adams & MacKay 2007, arXiv 0710.3742) [V]. Noufaily et al. 2013 (improved Farrington) [V]. Aggregation: Satopää et al. 2014; Baron et al. 2014; Atanasov et al. 2017 [S]. ACH: Heuer; Karvetski & Mandel 2020 found no accuracy benefit [S]. Event studies: Corrado 1989 rank test; Campbell & Wasley 1996 abnormal volume [U]. Pre-event trading: Poteshman 2006 (options before 9/11) [V]; Chesney, Crameri & Mancini 2015 [V]; the 2026 oil-futures episodes before US–Iran announcements (Bloomberg, 15 April 2026; Senate Banking minority letter) [S].

### 14.5 Data sources
CFTC COT on publicreporting.cftc.gov (Socrata; public domain; datasets: legacy `6dca-aqww`, disaggregated `72hh-3qpy`, TFF `gpe5-46if`, and their futures-and-options variants) [V]. CME: personal non-commercial use only, automated access prohibited → excluded [S]. ICE: end-of-day data paid; ICE publishes a free weekly COT for Brent and gasoil (location to verify) [S]. FRED API: free key; must show the "not endorsed or certified" notice; series marked copyrighted (VIXCLS, OVXCLS (Cboe), DCOILBRENTEU) are personal use only → `LOCAL_ONLY` [V]. EIA API v2: public domain, but spot prices are sourced from LSEG → `LOCAL_ONLY` for those series [V]. Wikimedia Pageviews: CC0, User-Agent with contact required [V]. GDELT 2.0: open with citation; DOC API limited to one request per 5 s [V]. IMF PortWatch: ArcGIS, no key, reuse with attribution, about 7 days lag [V]. NGA MSI and State Department advisories: keyless official APIs [V]. UKMTO: blocks automated clients → excluded [V]. Metaculus: terms restrict AI/ML use without permission → excluded pending permission [S]. World Bank and ECB APIs: keyless [V].

---

## 15. Prompt breakdown

### 15.1 New prompts 116–126

| # | Prompt | Scope | Depends on |
|---|---|---|---|
| 116 | Market-And-Attention-Signals | `cftc-cot` (+ history backfill), `fred-series` (public-domain defaults; copyrighted series local-only), `wikimedia-pageviews`, `gdelt-timelines`, `worldbank-indicators`; `attention_topic`; optional `ice-cot` | 093, 066, 095, 073 |
| 117 | Situations-And-Storylines | situations, members, snapshots, linker, lifecycle, themes, briefs, merge/split | 101, 098, 040, 099, 116 |
| 118 | Change-Points-And-Event-Studies | region rollups, `SeriesReader`, series registry, findings + `foresignal-analytics`, CUSUM and BOCPD, event studies, pre-event signals, unexplained moves, co-occurrence | 038, 040, 093, 100, 103, 116, 117 |
| 119 | Precursor-Mining-And-Library | precursor tests (renamed), event and series candidates, BH/BY, validation, library, live `precursor-active` | 103, 118 |
| 120 | Base-Rates-And-Historical-Analogs | base-rate library, reference imports, signatures, analog search | 117, 118, 027 |
| 121 | Forecast-Ledger-And-Scoring | questions, resolvers, forecasts, forecasters, scoring, CORP, bias monitor, ensemble, Platt, retrospective evaluation, `EstimativeLanguage` | 119, 120 |
| 122 | Hypotheses-Indicators-And-ACH | sets, hypotheses, revisions, indicators with watch rules, updater, ACH, key assumptions, speculative links, drafting | 017, 100, 117, 119–121 |
| 123 | Series-Forecasts-Regional-Outlooks-And-Indices | series forecaster, regional 4-week outlooks, scored indices with fitted weights | 118, 120, 121 |
| 124 | Impact-Projection | hazards × places × infrastructure, `projected-impact`, ledger questions | 104, 105, 120, 121 |
| 125 | Analysis-Cycles-And-Assessments | cycles, packet, LLM roles, assessments, intel board, notifications | 029, 102, 117–124 |
| 126 | ✅ Checkpoint-Intelligence | run and fix all tests from 001–125 | all |

### 15.2 Changes to unexecuted prompts

| Prompt | Change |
|---|---|
| 019 | Analytic retention classes come in through descriptors (Conventions); intelligence tables are outside RetentionJob |
| 038 | Pointer: 118 adds region resolutions 2 and 3 |
| 102 | Pointer: 125 reuses analyst profiles and the proposal pipeline |
| 103 | `hypothesis` renamed **precursor test** (`precursor_test`, `/v1/precursor-tests`, `test_precursor`); prompt renamed `103-Backtesting-And-Precursor-Tests` |
| Adapter prompts | Retention defaults per the class table unless the prompt names a longer window |
| 127–131 (former 116–120) | Renumbered; test-run lists and cross-references updated |

---

## 16. Rejected or deferred

| Idea | Decision |
|---|---|
| Prediction-market prices (Polymarket, Kalshi) | Excluded on terms (093 §8). Research shows market blending improves forecasts; revisit only with written consent. |
| Metaculus community forecasts as a baseline | Excluded until Metaculus grants permission for this use. |
| VIEWS forecasts as an external forecaster | Deferred until the data licence is confirmed. |
| CME/ICE exchange data, options flow | Licence terms; an operator may push their own through a custom source (024). |
| Social-media scraping (X, Telegram) | Excluded by charter. |
| Person-level analysis, insider identification | Never. |
| Neural forecasting models | Charter non-goal; statistical fitting only. |
| Acting on a probability automatically | Forecasts drive only operator-written rules on `analytic-finding` records. |
| Federating hypotheses and forecasts across instances | After 1.0 (113 carries intel events only). |
