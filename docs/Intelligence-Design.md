# Foresignal Intelligence: analysis, rolling hypotheses and forecasting (v1.3 design)

Prepared 2026-10-11, from a survey of the four reference projects' code, a web scan of open-source and commercial tools, and a review of forecasting and intelligence-analysis research (§14).

This document is **authoritative for the intelligence layer**: situations, analytic findings, precursors, base rates and analogs, the forecast ledger, hypotheses and indicators, statistical forecasts and regional outlooks, impact projection, and the daily, weekly and monthly analysis cycles. It extends, and does not replace, prompts 098–104 (entities, derived layers, windowed rules and anomalies, intelligence events and investigations, the autonomous analyst, backtesting, infrastructure mapping). Where this document and an older prompt disagree on the intelligence layer, this document wins. `docs/Core-Data-Model.md` still wins on ingest, history, triggers and retention mechanics.

New prompts **116–139** implement it. The v1.2 prompts 116–120 are renumbered **140–144** (`docs/Roadmap.md` §5 has the map). §17 records how the October 2026 external reviews (ChatGPT, Gemini, Grok) were handled. The client-facing object contract (references, relations, workspaces, deep links) is specified in [Client Contract](Client-Contract.md).

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
| Evidence independence | Observations link to **canonical events**; observations that trace to one originator form one **evidence group**, and only evidence groups count as independent confirmation (116). | Twelve re-reports of one claim are one piece of evidence; otherwise duplicates inflate every probability downstream. |
| "Nothing happened" vs "nothing seen" | A time-versioned **observability model** estimates each source's detection probability per phenomenon, place and threshold (capture–recapture where independent sources exist, presence proxies or declared footprints otherwise). Absence conclusions require detectability, not just healthy sources (116). | Source health is not coverage: a working receiver may see nothing below 3,000 ft in a valley. |
| Reproducibility | Every analytic output points to an **analysis manifest** (component version, build, configuration, registry hashes, knowledge cutoff, inputs, model and prompt); deterministic outputs replay to identical results (116). | Findings and forecasts must be auditable against the exact machinery that made them. |
| Does the analysis work? | A **benchmark harness** with planted-truth scenarios (null periods, outages, echoes, late revisions, misleading correlations, true precursors) and a historical corpus measures false discoveries, recall, delay and calibration, and **gates** autonomous features (123). | Passing tests proves the code; only benchmarks show the analysis is useful. |
| Shared client contract | One object-reference grammar and envelope, a time-aware relation graph, annotations with human validation, and workspaces whose context syncs live across apps (129; [Client Contract](Client-Contract.md)). | Frontends (globe, analyst workstation, sensor consoles, agents) must not each rebuild entity resolution, correlation or investigation state. |
| Many people, many instances | Named **principals** (people and agents) hold scoped tokens; **groups**, per-object **access lists** and **TLP 2.0** labels govern work products (investigations, workspaces, chronicles, reports, notes, attachments). No passwords in the broker (130). | Shared servers and separate instances both need sharing without turning Foresignal into a user-directory product. |
| Long-running monitoring of an event | **Chronicles**: named bundles of layers that record their own history while active, independent of global retention; stop, replay, share, archive; proposed by the analysis cycles (131). | "Track the war in the Middle East, then replay it months later" must not depend on 3-day track retention. |
| Getting analysis to other people | **Reports** with pinned evidence and TLP banners, sent to principals here or as **signed packages** to other instances, imported read-only with provenance (132). | Analysis is only useful if it can be shared safely and verifiably. |
| News coverage and bias | Articles as metadata (bodies and bylines can come from EDP providers), outlets with lean, factuality and ownership from **declared, licensed providers** (never generated), stories clustered so reprints count once, coverage balance and blindspots fed into situations and hypotheses (133). | Ground News-style insight, without inventing ratings, and as an evidence-quality input rather than a truth signal. |
| Collaborative intelligence network | Chronicles discover related chronicles (other analysts', other instances') from **keyed fingerprints** of captured evidence, disclosed only as discoverability and TLP allow; shared evidence counts once; **circular reporting** is detected (134). | Independent monitoring threads become a network without leaking private work or double counting. |
| Social media | **Social signals** (135–137): public posts become versioned records, mention-volume series with coverage-aware baselines, **narratives** and structured **claims** counted by independent origin, image fingerprints, propagation and coordination indicators; claims are **compared with independent physical evidence** in both directions (corroborated with lead time, unobserved despite adequate coverage, attention gaps), physical findings **cue** targeted searches, and predictive value is scored out of sample. Built-in platforms use official APIs as **operator-enabled adapters** (Bluesky's open stream on by default; X, Threads, Reddit, YouTube, Mastodon and Instagram hashtags with the operator's own credentials and budget); any other platform (Telegram channels, Facebook, TikTok, forums) arrives through external EDP providers. | What people say and what sensors see are both intelligence; their agreement, disagreement and timing are findings in their own right. Foresignal is a single-operator tool, so platform choice belongs to the operator. |
| Fringe and specialist sources | **External providers, not built-ins.** Web maps, scrapers, browser automation, Telegram channels, prediction markets and niche feeds are separate projects that speak the [External Data Protocol](External-Data-Protocol.md) (024): a self-describing manifest, layer or atomic streams, push or pull. The operator binds atomic streams to series, evidence, indicators, forecasts, places, situations and chronicles (138). Nothing is excluded or policed. | Keeps the core small and stable, lets anyone add a source without changing Foresignal, and still aggregates everything with provenance and independence. |
| Where in the roadmap | A new block **116–139** after 115, starting with the evidence foundations (116), then analysis, a foundations checkpoint (124), the object contract (129), collaboration, chronicles, reports and news (130–133), the chronicle network (134), social signals (135–137), external data bindings (138) and a checkpoint (139), before the adapter doctor and SDKs (so the SDKs include the new API). Nothing earlier is renumbered. | The build takes days, so moving the block earlier gains little. **Collecting early** (§4.4) is what matters. |

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
| Story clustering with lifecycle | situations (118) with embedding similarity in the linker |
| Seasonal baselines, z-scores, term spikes | 100 anomalies; attention topics (117) |
| Geo-convergence and incident fusion | 101 recipes; co-occurrence mining (119) adds significance testing |
| Deception and jamming detection | 099 |
| Country instability index | regional indices (126), decomposed, **scored against outcomes** |
| Forecasts with resolution specs and Brier scorecard | ledger (122) |
| Markets tied to events, "silent divergence" | event studies, `pre-event-signal` and `unexplained-move` findings (119) |
| Self-regenerating briefs (Dataminr) | situation briefs (118) |
| Unprompted anomalies with suggested causes (Windward) | findings (119) + red team and analyst roles (128) |
| Challenge agents (Babel Street) | red team role (128) |
| Published accuracy next to forecasts (ACLED CAST) | ledger scorecards on every question and outlook (122, 126) |
| Synchronized operational picture with human validation of AI findings (Palantir Maven-style) | workspaces with live context sync, annotations and verdicts feeding precision metrics (129) |
| Sensor discovery and coverage visualisation (MetaConstellation-style) | observability model and coverage tiles (116), sensor discovery (129) |
| Edge processing and store-and-forward sync | federation and edge relay (113), MeshDTN compact messages (084, 113) |

Fringe and specialist sources (prediction markets, exchange data, unofficial and browser-based sources, Telegram channels) are external EDP providers (024, 138); built-in social platforms use official APIs (135). Person-level profiling is not built.

---

## 2. Principles (binding on prompts 116–139)

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
13. **Licensed data stays local by default.** The operator can override egress per source (109). Series whose terms forbid redistribution (FRED copyrighted series, EIA spot prices sourced from LSEG, ACLED) are `egress=LOCAL_ONLY`: Foresignal computes on them and serves derived signals, never the raw values, outside the operator's own tokens.
14. **Evidence independence.** Confidence, corroboration and hypothesis updates count independent evidence groups (116), never raw records or sources.
15. **Benchmarked before trusted.** Autonomous proposals and LLM ensemble weight depend on the latest benchmark status of the components involved (123).
16. **Social is reporting, not observation.** Social posts are reports by sources (accounts are analysed like outlets, never profiled as people). A narrative never corroborates a physical event or itself; amplification never adds independent evidence; "no corresponding change" needs detectability and "too little attention" needs social coverage; leading-indicator claims come only from out-of-sample tests (135–137).

---

## 3. The analytic stack

```
 L8  Analysis cycles ─ daily / weekly / monthly assessments (128)
          ▲ deterministic packet; LLM roles: analyst · red team · forecaster · proposer
 L7  Forecast ledger ─ questions · forecasts · resolution · scores · calibration (122)
          ▲ forecasters: base rate · climatology · persistence · statistical (126) · precursor (120)
          │              analog (121) · impact (127) · indicator-Bayes (125) · LLM (128) · operator · ensemble
 L6  Hypothesis sets ─ competing hypotheses · indicators → watch rules · ACH matrix · revisions (125)
 L5  Memory ─ situations / storylines (118) · base rates · historical analogs (121)
 L4  Discovery ─ change points · event studies · co-occurrence (119) · precursors and lead–lag (120)
          · regional outlooks and indices (126) · impact projection (127)
 L3  Correlation ─ intelligence events · investigations · exposure · dependency graph (101, 104)
 L2  Detection ─ derived layers · count / absence / anomaly rules (099, 100)
 L1  Signals ─ records and versions · rollups (038; region rollups 119) · series (093, 117)
 L0.5 Evidence ─ canonical events · evidence groups · observability · analysis manifests (116)
 L0  Collection ─ budgeted sources · health · history with analytic retention (001–097)

 Across all levels: object contract · relation graph · annotations · workspaces (129) · benchmark harness (123)
```

### 3.1 Three kinds of memory

Foresignal keeps three distinct memories, with different owners and rules. Keeping them separate is a design invariant.

| Memory | What it holds | Owner | Changes by | Retention |
|---|---|---|---|---|
| **Observations** | versioned records from sources and pushes (L0–L1): what the world reported, when Foresignal knew it | the ingest writer | new versions only; never edited | analytic retention classes (§4.1) |
| **Interpretation** | everything Foresignal concluded about the observations (L2–L8): anomalies, intel events, findings, situations, precursors, base rates, hypotheses and revisions, forecasts and scores, assessments | analytic components and analysts | versioned revisions with manifests; never rewrites observations | indefinitely (typed tables) |
| **Chronicles** | user-directed captures of chosen layers over a monitoring thread, plus snapshots of the interpretation at the time | their owners | capture while active; never altered after capture | the chronicle's own policy, independent of global retention |

Rules: interpretation cites observations by pinned `seq`; chronicles **copy** observations and **snapshot** interpretation; nothing downstream ever mutates an upstream memory; a chronicle replay shows the observations and the interpretation as they were captured, while today's interpretation of the same period is available separately for comparison.

Outputs are ordinary Foresignal objects: typed tables for structure, plus records of a DERIVED source so SSE, tiles, search, triggers, briefings and MCP work unchanged.

| DERIVED source | Kinds | Created by |
|---|---|---|
| `foresignal-intel` (101) | `intelligence-event`, `situation` (118), `hypothesis-set` (125) | 101, 118, 125 |
| `foresignal-analytics` (119) | `analytic-finding`, payload `findingType` ∈ `change-point`, `pre-event-signal`, `unexplained-move`, `co-occurrence`, `precursor-active`, `forecast-exceedance`, `outlook-exceedance`, `projected-impact` | 119, 120, 126, 127 |
| `foresignal-outlook` (126) | `regional-outlook`, `regional-index` | 126 |

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

Event-class sources are small (USGS ≈ 300 versions/day, ACLED ≈ 1–3k/day, IODA a few hundred). Five years of all `event` sources is expected in the low tens of GB; `/v1/system.history` reports the real projection and 143 measures it.

### 4.2 Region rollups

038's `activity_rollup` is keyed by H3 cell (res 4): hourly rows for 35 d, daily rows for 5 years. Prompt 119 adds region resolutions to the same table:

| `resolution` | Bucket | Key | Retention |
|---|---|---|---|
| 2 | hour | `(source_id, kind, region)`; `region` is the 040 country code, or `CC-admin1` when the source opts in (`statsRegionLevel=admin1`) | 1095 d |
| 3 | day | same | 3650 d |

A nullable `region_code` text column is added; `cell` holds a stable 64-bit hash of the region code for these rows. Same cursor discipline as 038 (durable cursor, closed buckets only, consumer clamp), with its own cursor `stats.region.cursor` that starts after 040 has tagged records.

### 4.3 Series

093 defines series as `series-point` records with `GET /v1/series` and `GET /v1/series/{sourceId}/{seriesId}`. The intelligence layer reads three families through one `SeriesReader` (119):

| `SeriesRef` | Backing data | Example |
|---|---|---|
| `series:{sourceId}/{seriesId}` | `series-point` records (093, 117) | `series:cftc-cot/067651.disagg.mm_net` |
| `rollup:{res}/{sourceId}/{kind}/{region\|cell}` | `activity_rollup` rows | `rollup:day/acled/conflict-event/YE` |
| `rule:{ruleId}/{groupKey}` | `activity_sample` (100) | `rule:01J…/YE` |

Every point carries `periodStart` (event time), `knownAt` (ingest time of the version that supplied it) and `coverageOk`. **Event studies align on event time; live detection and forecasting only use points whose `knownAt` ≤ the decision time.**

### 4.4 Collect early

History cannot be backfilled from live feeds. The roadmap recommends deploying a collecting instance on the bench server **after prompt 022** with event-class sources enabled, upgrading it as the build continues (`ddl-auto=update` handles additive schema). The intelligence block then lands on weeks of real history, and reference imports (§7.2) supply the long outside view.

---

### 4.5 Evidence identity and independence (prompt 116)

The same earthquake can arrive from USGS, EMSC, GDACS and a news feed. Four records are not four confirmations: GDACS republishes USGS, and the news item cites one of them.

- **Originators.** Every source maps to an originator (`intel/originators.json`, descriptor override): an id, a class (sensor network, agency, aggregator, media, crowd, operator, derived), an **independence group**, and the originators it **aggregates**.
- **Canonical events.** `EvidenceResolver` links event-class observations to `canonical_event` rows using family-specific tolerances (reusing 099's corroboration rules) and upstream-id matches, with a link confidence and reason.
- **Evidence groups.** Within a canonical event, observations sharing an originator, an independence group, an aggregation relationship or a citation form one `evidence_group`. Only evidence groups count as independent.
- **Quality flags:** `ECHO` (many re-reports of one originator), `TIMESTAMP_CONFLICT`, `SINGLE_ORIGINATOR`, `LATE_REVISION`. These are the "adversarial evidence evaluator": they say "12 reports trace back to one unverified origin", without claiming anything is disinformation.
- **Consumers:** 099 corroboration credibility, 101 `EventScorer` (v1.1.0), 118 corroboration roll-up, 120 precursor occurrences, 121 base rates, 125 updater and ACH rows, 126 outlook counts.

### 4.6 Observability (prompt 116)

A source being healthy is not the same as it being able to see something.

- **Declared:** area, thresholds (e.g. magnitude 2.5 in the US, 4.5 globally) and latency per kind.
- **Estimated**, versioned in time (`observability_estimate`): **capture–recapture** for families with independent sources (`p_detect(A) = |A ∩ B| / |B|` over canonical events, per region and magnitude band); a **presence proxy** for track kinds from hourly rollups (labelled low confidence); **receiver range** for local stations; declared footprints otherwise.
- **Combination:** `1 − Π(1 − p)` across independence groups (max within one group).
- **Absence rule:** "nothing happened" requires healthy sources **and** `p_detect ≥ absenceMinDetect` (0.8). Otherwise the result is `unknown` / `LOW_DETECTABILITY`, in 100 (absence and downward anomalies), 119 (downward findings), 120 (precursor misses), 122 (negative resolutions) and 125 (absent indicators, whose likelihood ratio is also scaled by `p_detect`).
- **Coverage layer:** an API and tiles that show how well instrumented any area is for a family at a time.

### 4.7 Analysis manifests (prompt 116)

`analysis_manifest` records component and version, build (version and git commit), configuration and its hash, the hashes of registry files used, `asOfSeq`, inputs (explicit refs, or the query and a hash of the ordered refs), and model metadata for LLM steps. Identical manifests are stored once. Every analytic output from 100 onward stores `manifest_id`; `POST /v1/manifests/{id}/replay` re-runs deterministic components and reports `identical`, `differs` or `not-replayable`.

## 5. Discovery (prompts 119, 120)

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
- **Live `unexplained-move`:** a daily scan of watched market and attention series. A move with |z| ≥ 3 against the estimation window and **no** qualifying event in the linked regions or themes within ±2 days becomes an `unexplained-move` finding. If a qualifying event appears within the following 7 days, the finding is linked to it and re-labelled as an observed pre-event signal; that link feeds precursor mining (120).
- **Resolution limit:** Foresignal's market data is daily or weekly. The documented 2026 oil-futures episodes were visible minute by minute in licensed exchange data; Foresignal cannot see moves like those, and every market finding carries `DAILY_RESOLUTION_ONLY`.
- Batch studies run on the 103 job framework (`type = event-study`) with its caveats and no-look-ahead rules.

### 5.4 Co-occurrence mining

`CoOccurrenceMiner` (`cooccurrence` 1.0.0) looks for combinations nobody wrote a recipe for.

- Daily, over region-day rollups: for every region with activity and every pair of **signal families** (`signal_family` registry: `ais-behaviour`, `aviation-military`, `conflict`, `outage`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`), test whether both are above their own baselines on the same day.
- Each family's daily exceedance p-value comes from its NB baseline; the joint statistic is Fisher's combination; the expected joint exceedance rate comes from each family's historical exceedance frequency (independence baseline), and the reported lift is observed ÷ expected joint days over the trailing year.
- Benjamini–Hochberg at `q = 0.05` across all region × pair tests of the day.
- Survivors become `co-occurrence` findings and are offered to the situation linker (118).

### 5.5 Precursors and lead–lag (120)

103's "does X precede Y?" object is renamed **precursor test** in v1.3 (the word "hypothesis" is reserved for §6). Prompt 120 adds discovery and validation:

1. **Event-stream candidates:** operator-registered family pairs, plus automatic pairs among the top-K event families per region (intel event types, alert rule tags, anomaly classes, change points by series family, `unexplained-move` findings, reference-import families), capped by `analysis.precursors.maxTestsPerRun` (500). Tested with 103's circular-shift tester.
2. **Series candidates:** pairs of analysis series (e.g. attention ↔ conflict counts, positioning ↔ chokepoint transits). Pre-whiten each by its baseline residuals; statistic = max |cross-correlation| over lags `1…L` (antecedent leading only); null from circular block-shift surrogates with block length ≥ the residual autocorrelation length.
3. **FDR:** Benjamini–Hochberg at `q = 0.10` per batch (Benjamini–Yekutieli when the batch contains overlapping series).
4. **Validation:** survivors are re-tested on the disjoint later window (newest third). Only patterns that are again notable with lift ≥ 1.5 (event streams) or the same sign and p ≤ 0.05 (series) become `validated`.
5. **Library:** `precursor` rows hold selectors, spatial rule or series pair, lag distribution, hit rate with Wilson interval, base rate, lift, n, both windows' statistics, FDR batch size, `status` (candidate | validated | stale | retired), `last_tested_at`. A monthly re-test retires patterns whose lift falls below 1.2 or whose interval includes the base rate.
6. **Live watch:** when a validated precursor's antecedent occurs, record a `precursor-active` finding; 122 then opens a forecast question for the consequent with the precursor forecaster's probability (hit rate shrunk toward the base rate by sample size).

### 5.6 Analysis series registry

`analysis_series(ref, enabled, detectors[], frequency, origin auto|operator, watch_pairs, created_at)`. Nightly auto-registration: region-day rollups with ≥ 30 non-zero days in the last 90; every `series-point` series from 093 and 117; PortWatch chokepoints; anomaly rule groups. Caps keep runs bounded (`analysis.series.max`, default 2,000).

---

## 6. Memory and reasoning (prompts 118, 125)

### 6.1 Situations (storylines)

A **situation** is a persistent storyline grouping related evidence over days to months.

- `situation` (theme, status `emerging | active | dormant | closed`, severity, regions, geometry, topics, entities, deterministic summary, optional LLM brief), `situation_member` (intel events, findings, alerts, anomalies, records, investigations, each with `join_reason` and corroboration status), `situation_snapshot` (one per day, so "what changed since" is a diff), `situation_log`.
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

A probability crossing a band boundary is a **band change**, published on SSE and to channels (rate-limited per set). One `EstimativeLanguage` utility (122) maps numbers to terms and **lints** generated text: a term that disagrees with its number, a mix of term rows, or a likelihood term and a confidence level in one sentence is rejected and regenerated or downgraded.

### 6.5 Drafting

`POST /v1/hypotheses/sets/draft {text, situationId?}` has the LLM draft a set from plain language ("watch whether the Red Sea situation spreads to Hormuz"): competing hypotheses including "other", priors with their basis (it must call `get_base_rate` and `find_analogs`), indicators as validated rule specs, key assumptions. The draft is returned for confirmation, never created automatically.

---

### 6.6 Information value and collection gaps (prompt 125)

- **Expected information gain** ranks pending indicators: `EIG = H(P) − Σ_o P(o) · H(P | o)` with `P(observed)` including `p_detect`. This turns the ACH matrix into an ordered watch list ("what would have to be true").
- **Collection gaps:** indicators that would matter (high potential EIG) but cannot currently be observed (`p_detect < 0.5` or sources disabled) produce recommendations: enable a catalogued source, add a local receiver or sensor type, or "no catalogued source observes this". Recommendations never change configuration.

## 7. Base rates and analogs (prompt 121)

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
| CFTC COT history (Socrata, 117) | 1986– | public domain |
| IMF PortWatch history (088) | 2019– | IMF terms: reuse with attribution |

### 7.3 Historical analogs

- **Signature:** standardised member counts per signal family over the first N days (7), escalation slope, theme, region class (040), entity classes, plus the 027 embedding of the deterministic summary.
- **Search:** reciprocal-rank fusion of feature cosine and embedding cosine, excluding time overlap; top k (10) with **outcomes** (duration, peak severity, follow-on event families within 7 and 30 d).
- **Analog forecaster:** outcome frequencies among analogs, shrunk toward the base rate by analog count.

---

## 8. Forecast ledger (prompt 122)

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
- **External** forecasters (`type=external`): prediction markets, Metaculus, VIEWS or any other forecast source arrive as EDP `forecast` items from external providers and are bound to ledger questions (138, question links); they are recorded on material moves and at least daily, scored like every forecaster, and join the ensemble only after 30 resolved linked questions (market blending improves forecasts when its skill is earned).

---

### 8.7 Benchmark harness (prompt 123)

Software tests prove the code; benchmarks show whether the analysis is useful. Deterministic scenarios with planted truth (`null-seasonal`, `step-change`, `spike`, `coverage-outage`, `duplicate-echo`, `late-revision`, `common-driver`, `true-precursor`, `base-rate-drift`, `pre-event-move`) and a historical corpus (operator cases plus sampled null region-weeks) measure, per component: false-discovery rate, recall, precision, detection delay, correct `unknown` under outages, duplicate inflation (must be zero), precursor false validation, and forecaster calibration and skill against naive baselines. Benchmarks run components in pure or dry-run modes and never write production analytic tables. Results are `pass | warn | fail` against documented thresholds and **gate** 128's proposer auto-execution and the LLM forecaster's ensemble weight. Analyst verdicts (129) propose new historical cases.

## 9. Regional outlooks, indices and series forecasts (prompt 126)

### 9.1 Series forecaster

`SeriesForecaster` (`series-forecast` 1.0.0) for registered analysis series: seasonal naive (benchmark), ETS(A,N,N) and ETS(A,A,A) on `log1p` (parameters by maximum likelihood with Hipparchus), an NB GLM (`log μ = trend + day-of-week + Fourier(annual) + lagged counts`, prediction intervals from NB quantiles with a parameter bootstrap), and a damped trend for continuous series. Method choice per series by rolling-origin backtest over the last 8 origins, minimising mean pinball loss; choice and backtest score stored. Horizons 1, 7 and 28 days; quantiles p05–p95. Served at `GET /v1/analytics/series/forecast?ref=`; actuals outside the 1-step p05–p95 band become `forecast-exceedance` findings.

### 9.2 Regional outlooks (CAST-style)

For each enabled region (country, or admin1 where data supports it) and **event family** (battles, explosions/remote violence, violence against civilians, protests from ACLED/UCDP; outages; severe weather alerts; earthquakes M4.5+), a weekly job publishes quantile forecasts of event counts for the next six rolling 4-week windows. Each outlook is a `regional-outlook` record **and** a numeric ledger question per window, so its accuracy is published next to it automatically. Inputs: the region's own history (and reference imports), seasonality, recent trend, neighbouring regions' counts, and the region's index (§9.3).

### 9.3 Regional indices, scored

`regional_index` definitions (built-ins and operator-defined): named components (each a series ref with a transform and normalisation), weights, floors, and a target (the outlook family it should anticipate). Every index value is a `regional-index` record with its **decomposition** (each component's contribution) and input provenance.

Unlike every competitor's index, weights are not left hand-set: a monthly job fits component weights by regularised Poisson or NB regression of the next-4-week target count on the components (Hipparchus, L2 penalty, rolling origin), reports out-of-sample skill against the seasonal-naive outlook, and **proposes** the new weights (applied by an operator or an agent with `allowActions`). The index page always shows its current out-of-sample skill.

Optional structural covariates come from World Bank indicators (117).

---

## 10. Impact projection (prompt 127)

`ImpactProjector` (`impact-projection` 1.0.0) turns hazard forecasts Foresignal ingests into probabilistic, scored impact statements:

| Hazard input | Probability used |
|---|---|
| NHC wind-speed probabilities and cones (067, 075) | published probabilities; the cone is described as covering about two thirds of historical track errors and is never a probability on its own |
| SPC outlooks (047) | published probabilistic outlooks; categorical risk mapped to its published ranges |
| WPC excessive rainfall (089) | published category ranges |
| NWPS flood forecasts (079) | forecast category at the gauge, calibrated by the gauge's historical exceedance |
| Storm tracks (105) | extrapolation with the tracker's position error |

For each hazard forecast it intersects the area with places (036), infrastructure (104 paths with weakest-edge labels) and exposure (101), and estimates `P(impact) = P(hazard at asset) × P(impact | hazard class)`, with the conditional rate taken from base rates (121). Each statement becomes a `projected-impact` finding and a binary ledger question ("Will an outage be reported in Harris County, TX, by 18:00Z tomorrow?") resolved by the matching sources (068, 092, 087). Labels: "modelled; heuristic links may be wrong; not an official warning".

---

## 11. Analysis cycles and assessments (prompt 128)

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
| Monthly | plus `monthly` sets; base-rate refresh (121); precursor re-test (120); ensemble weights, extremizing and Platt refits (122); index weight proposals (126); stale set archiving; 90-day outlook; retrospective evaluation of the LLM forecaster on the past month where valid |

### 11.4 The intel board

`GET /v1/intel/board` (≤ 8 KB, ETag, like 038's glance): top situations, band changes in the last 24 h, indicators observed, forecasts due this week, the latest assessment of each cadence, and the headline calibration figure.

---

## 12. Interfaces and libraries

| Area | REST (new) | SSE topic | MCP (read / agent) |
|---|---|---|---|
| Markets and attention (117) | `/v1/attention/topics/**`; series via `/v1/series` | n/a | `list_attention_topics` |
| Situations (118) | `/v1/situations/**` | `situations` | `list_situations`, `get_situation`, `get_situation_changes` / `create_situation`, `link_to_situation`, `merge_situations` |
| Findings (119) | `/v1/analytics/findings/**`, `/v1/analytics/series/**`, `/v1/analytics/event-studies` | `analytics` | `list_findings`, `get_finding`, `get_series_data` / `run_event_study` |
| Precursors (120) | `/v1/precursors/**`, `/v1/precursor-tests/**` (103) | `analytics` | `list_precursors` / `mine_precursors` |
| Base rates, analogs (121) | `/v1/base-rates/**`, `/v1/situations/{id}/analogs`, `/v1/reference-imports/**` | n/a | `get_base_rate`, `find_analogs` |
| Ledger (122) | `/v1/forecasts/**` | `forecasts` | `list_questions`, `get_question`, `get_scorecard` / `create_question`, `submit_forecast` |
| Hypotheses (125) | `/v1/hypotheses/sets/**` | `hypotheses` | `list_hypothesis_sets`, `get_hypothesis_set` / `create_hypothesis_set`, `add_evidence`, `draft_hypothesis_set` |
| Outlooks and indices (126) | `/v1/analytics/series/forecast`, `/v1/outlooks/**`, `/v1/indices/**` | `analytics` | `get_series_forecast`, `get_outlook`, `get_index` |
| Impact (127) | `/v1/analytics/impacts/**` | `analytics` | `list_projected_impacts` |
| Evidence and observability (116) | `/v1/evidence/**`, `/v1/observability/**`, `/v1/manifests/**` | `evidence` | `get_evidence`, `get_observability` |
| Benchmarks (123) | `/v1/benchmarks/**` | n/a | `get_benchmark_status` / `run_benchmark` |
| Objects and workspaces (129) | `/v1/objects/**` (incl. `/dossier`), `/v1/context/summary`, `/v1/graph/**`, `/v1/relations/**`, `/v1/annotations/**`, `/v1/analytics/precision`, `/v1/workspaces/**`, `/v1/context-links`, `/v1/sensors`, `/v1/contract` | `workspace` | `get_object`, `get_related`, `get_workspace_context` / `set_workspace_focus`, `annotate` |
| Collaboration (130) | `/v1/me`, `/v1/principals/**`, `/v1/groups/**`, `/v1/objects/{ref}/acl`, `/v1/objects/{ref}/share`, `/v1/shared-with-me`, `/v1/tags/**`, `/v1/attachments/**`, `/v1/evidence-items/**`, `/v1/saved-searches/**`, `/v1/investigations/**` | `me` | `whoami`, `list_shared_with_me`, `tag_objects`, `comment` / `share_object` |
| Chronicles (131) | `/v1/chronicles/**` (thread, state, tiles, frames, tracks, series, summary, export) | `chronicle` | `list_chronicles`, `get_chronicle_thread`, `get_chronicle_state` / `create_chronicle`, `add_chronicle_layer` |
| Reports and exchange (132) | `/v1/reports/**`, `/v1/exchange/**` | `me` | `list_reports`, `get_report` / `draft_report`, `send_report` |
| News and media (133) | `/v1/news/**`, `/v1/media/**` | `analytics` | `list_news_stories`, `get_news_story`, `get_outlet` |
| Chronicle network (134) | `/v1/chronicles/{id}/related`, `/v1/chronicles/{id}/discoverability`, `/v1/chronicle-network/**`, `/v1/federation/chronicle-sketches` | `me` | `get_related_chronicles` |
| Cycles (128) | `/v1/analysis/cycles/**`, `/v1/assessments/**`, `/v1/intel/board` | `assessments` | `get_assessment`, `get_intel_board` / `run_cycle` |

All read tools join the read catalogue (025), so Ask, the analyst and the cycles can use them. Every LLM-spending path is `alwaysAuthenticated`.

Notification event types (018): `situation.activated`, `situation.escalated`, `hypothesis.band_changed`, `indicator.observed`, `assumption.broken`, `forecast.resolved`, `finding.significant`, `assessment.published`. Defaults: weekly assessment and band changes on, the rest off.

**Libraries** (verified 2026-10): Apache Commons Statistics 1.3 (distributions, descriptive statistics, inference), Commons Numbers and Commons RNG, Hipparchus 4.0.x (optimisers for ETS maximum likelihood and NB/Poisson regression; confirm the LICENSE file is Apache-2.0). Optional: ServiceNow `stl-decomp-4j` (Apache-2.0) for STL. Written in-house under `ai.foresignal.analytics.math` with golden-value tests: CUSUM, BOCPD, ETS, PAV, CRPS, RPS, Corrado rank test, block surrogates, Benjamini–Hochberg/Yekutieli, Wilson and Jeffreys intervals, Platt scaling. Excluded: Smile (GPL-3.0 / commercial), Weka, JSAT, jdistlib (GPL), Commons Math 3.6.1 (unsupported).

---

### 12.1 Objects, relations and workspaces (prompt 129)

Foresignal is the shared intelligence foundation for several frontends (a globe, an analyst workstation, satellite and RF consoles, mobile, agents), which may ship as one application with switchable workspaces or as separate applications. Prompt 129 gives them one contract ([Client Contract](Client-Contract.md)):
- **Object references** (`situation:01J…`, `record:adsblol-mil/ae1234@8812`) and a uniform **envelope** for every object type, resolvable singly or in batches, `asOf`-aware.
- A **relation graph** with validity intervals, confidence and provenance, materialised from situations, intel events, canonical events, entity links, the facility graph, hypotheses, the ledger and investigations; queries for neighbourhoods, subgraphs and **common dependencies** (the temporal knowledge graph, without a separate graph database).
- **Annotations** on any object, with **verdicts** (confirmed, rejected, needs review, duplicate) that feed precision metrics and benchmark cases: human validation of AI findings.
- **Workspaces** with a versioned shared context (selected objects, time range, viewport, active situation and hypothesis set) synchronised live over SSE, with presence, so several apps or monitors follow one investigation; plus deep links. Client layout is opaque JSON; Foresignal never interprets UI state.
- **Sensor discovery:** what can observe an area in a window (sources by detectability, local receivers, SatDump stations, predicted satellite passes, imagery footprints). No tasking.

### 12.2 Collaboration (prompt 130)

- **Principals** (person, agent, service) own tokens; **groups** collect principals. The broker never stores passwords.
- **Data vs work products:** records and analytic objects stay instance-wide per scope; **work products** (investigations, workspaces, chronicles, reports, saved searches, notes, attachments, evidence items) have an owner, `visibility` and an access list (viewer, commenter, editor, owner), and are invisible to principals without access.
- **TLP 2.0** (`CLEAR`, `GREEN`, `AMBER`, `AMBER+STRICT`, `RED`) on every work product; derived products inherit the strictest input label; `EgressPolicy` enforces it on shares, links, exports and exchange.
- **Tags** (personal and shared, bulk assignment), **threaded comments** with @mentions, **attachments** (PDF and image text extraction; EXIF stripped) and **evidence items** graded with the Admiralty system (source reliability A–F, information credibility 1–6), **saved searches** with match notifications, and **investigations** with owners, members, statuses and links to everything an analyst uses.

### 12.3 Chronicles (prompt 131)

A **chronicle** is a named monitoring thread: a bundle of layers (sources and kinds with conditions, saved views, watchlists, entities, situations, finding types, series, raster products, alert rules) over an area, with a capture mode per layer (full, downsampled, events-only, latest-per-interval, rollup-only, frames). While active it **copies** matching data into its own partition, so its history is independent of global retention; storage is estimated before activation and capped. It has a live **thread** of notable updates, a workspace whose layout the client arranges, replay endpoints (state at T, tiles, tracks, series, frame index), and a lifecycle: proposed → active ⇄ paused → stopped → archived (cold package, restorable) or deleted. The analysis cycles **propose** chronicles when situations emerge; people accept, edit, keep or delete them. Chronicles are shareable under TLP.

### 12.4 Reports and intelligence exchange (prompt 132)

Reports are built from a selection, a tag, an investigation, a chronicle or a situation; drafts are deterministic (templates: situation brief, investigation summary, chronicle recap, weekly assessment), with an optional labelled LLM narrative. Publishing freezes a revision with pinned refs and snapshots and renders Markdown, HTML (TLP banner) and a JSON bundle. Reports are sent to principals and groups on the instance, or packaged as **signed intelligence packages** (instance key, 109) for principals on other instances (via 113 peers or as a file). Receivers verify signatures and hashes, enforce TLP, and import read-only with `remote:` references and provenance; imported material never contaminates local evidence or the ledger unless an analyst copies it deliberately. CLEAR reports can also go out as read-only links (112).

### 12.5 News coverage and media bias (prompt 133)

- **Articles** (metadata by default: title, URL, outlet, language, country, times, feed summary; bodies and bylines only from EDP `article` documents bound with target `news`, 138) from GDELT ArtList for attention topics and operator-added outlet feeds.
- **Outlet registry** with ratings from **declared providers**: Wikidata (CC0: country, owner, state media, political alignment where present), The Partisan Web (CC BY 4.0), and, opt-in with the operator's own rights, AllSides (reported CC BY-NC 4.0; confirm) and Media Bias/Fact Check (paid API); operator CSV imports. A consensus per dimension (median, disagreement, provider breakdown); **no model ever assigns a rating**.
- **Stories** are 116 canonical events (`news-story`); wire reprints form one evidence group, so syndication never inflates support.
- **Coverage analytics:** balance by lean, country, outlet type and factuality; state-affiliated share; **blindspots** (≥ 75% of rated coverage from one side, n ≥ 8) and **coverage divergence** findings; coverage series in the analysis-series registry.
- **In analysis:** coverage sections in situations, dossiers and assessments; `SKEWED_COVERAGE` caveats on hypotheses, and a confidence step-down when skewed support has no non-news corroboration; outlet factuality maps to source reliability for news evidence. Coverage describes reporting, never truth.

### 12.6 Chronicle network (prompt 134)

- **Fingerprints, not content:** each chronicle maintains MinHash sketches over **global evidence keys** (upstream event ids per originator, entity keys per day, H3 area-time cells, topics), hashed with a per-instance salt (local discovery) or a per-network salt shared by consenting peers (federated discovery).
- **Discoverability** is the owner's choice (`none` by default, `instance`, `network`), capped by TLP (RED never; AMBER+STRICT within the instance at most), with optional reveal of title and owner.
- **Disclosure:** chronicles a viewer can see show full shared evidence; otherwise only an **opaque link** (strength band, overlapping dimensions, time overlap). Links require a minimum overlap; anti-probing limits apply. Analysts can **request access** without revealing their own work.
- **Counting once:** overlap and combined cluster timelines are deduplicated by global evidence key.
- **Circular reporting:** analytic products carry provenance chains; a judgment that returns through other analysts, or two supports with a shared origin, count as one evidence group in hypotheses and confidence.

### 12.7 Social signals (prompts 135–137)

What people are saying, where it starts, how it spreads, and whether it matches what Foresignal observes independently.

- **Sources (135), operator-enabled, official APIs:** Bluesky Jetstream v2 (complete public stream, open, on by default; replay with the operator's API token), Bluesky search, Mastodon (hashtag streams and trends per instance), Reddit, YouTube, X (hourly counts for standing topics, billed per request; recent search for cues, billed per post; a monthly spend cap), Threads keyword search and Instagram hashtag search (30 hashtags per 7 days). Every other platform (Telegram channels, Facebook pages, TikTok, forums) arrives through external EDP providers emitting `social-post` documents, bound into the pipeline with target `social` (138); providers that declare `transport.search` take part in cues.
- **Posts** are versioned `social-post` records (text, handle, links, media links, engagement, platform geo when given) with per-platform retention and deletion sync as settings. Reposts and quotes inherit the original's originator (116), so amplification is never independent evidence.
- **Volume and baselines (135):** hourly series per topic, place and entity per platform, scaled by platform denominators (all posts seen on complete streams) so platform growth and outages do not look like attention changes; `social-velocity` findings. Each path is labelled complete, paged or sampled; only complete and paged paths feed baselines. Collection health feeds a **social-attention observability** estimate, so silence is distinguished from not looking.
- **Narratives and claims (136):** posts cluster into narratives (reposts, quotes, text similarity, shared links or images) as 116 canonical events whose evidence groups count origins; deterministic claim extraction (multilingual event lexicons, gazetteer places, entities, times, negation and hedging) with optional budgeted LLM extraction for large narratives; claims are graded 125 evidence items (Admiralty), with source reliability earned from each account's or channel's corroboration record.
- **Media (136):** PDQ fingerprints of shared images; reuse findings when an image was first seen earlier or elsewhere (within the fingerprint window, stated with each finding); analysts can match an image on demand.
- **Propagation and coordination (136):** per-narrative graphs across reposts, platforms and news (133) with earliest observed post per platform (never "originator"), descent share (how much traces to the top three origins), and statistical coordination indicators against null models (no account labels).
- **Social versus physical (137):** each significant claim is compared with non-social evidence in the same place and window: `corroborated` (with lead times over physical, news and official first observation), `unobserved` (only when `canSeeAbsence` is ok), `contradicted`, `insufficient-coverage`, `pending`. **Attention gaps**: significant physical events with far less discussion than similar past events, reported only where social coverage is adequate.
- **Cues (137):** physical findings (situation escalation, change points in outage, vessel, flight and fire series, critical alerts) generate budgeted searches with place names and aliases, nearby facilities and event terms in the region's languages; cue results are a targeted sample that never feeds baselines or corroborates its own trigger.
- **Predictive value (137):** social series and claim types are 120 precursor candidates (FDR, later-period validation); each comparison opens a ledger question scored for the `social-claim` forecaster (122); lead-time statistics use only out-of-sample comparisons.
- **In analysis:** situations gain a social section; indicators can be `reported` by social claims but only `observed` from physical evidence; assessments get "What people are saying versus what we observe"; chronicles keep narrative, claim and comparison history even after posts are deleted.

## 13. Worked example: positioning, strike, response

The owner's scenario of 9–11 October 2026, used to illustrate the mechanics (Foresignal has not verified the events): heavy oil-futures buying on Friday 9 October, a Houthi strike on Riyadh on Saturday 10 October, and an expected Saudi or coalition response. A documented precedent exists: in March and April 2026 large oil-futures trades minutes before US–Iran announcements drew CFTC and DOJ inquiries (§14.4).

1. **Signals.** ACLED/UCDP and GDELT (081, 066) mark the strike; NOTAM/TFR changes (087), AIS behaviour near Bab el-Mandeb (099, 088) and GPS-jamming cells (099) add context; EIA daily prices (093), FRED volatility (117, local only) and CFTC managed-money positions (117) give the market side; GDELT timelines and Wikipedia pageviews for the topic (117) give the attention side.
2. **Correlation.** A 101 recipe creates an intel event; the 118 linker attaches it to the open "Red Sea / Gulf escalation" situation.
3. **Discovery.** The 119 event study checks the watch pairs for the conflict family. Daily price and volatility can show an abnormal Friday; the positioning check completes when COT data for the week lands, flagged `LAGGED_PUBLICATION`. Any finding carries the coincidence baseline and `DAILY_RESOLUTION_ONLY` (a one-minute burst of trading would be invisible at daily resolution). If the market moved first with no event yet, Friday's `unexplained-move` finding links to the strike when it happens.
4. **Hypotheses.** The weekly cycle (or the operator) opens two sets:
   - *Outcome:* "Saudi or coalition kinetic response against Houthi targets within 7 days?" (exclusive: within 72 h, 3–7 days, none in 7 days, other). Prior from reference-import base rates for responses to comparable strikes and from analogs; indicators: military air and tanker activity (099), airspace NOTAMs (087), naval co-occurrence (099), attention spikes (117). Resolver: `event-exists` on ACLED/UCDP events of the response type.
   - *Speculative link:* "The pre-strike positioning reflected advance knowledge", with alternatives "scheduled news or inventory data", "contract roll or rebalancing", "coincidence at the base rate"; bounded by the placebo baseline.
5. **Ledger.** The outcome question gets forecasts from base-rate, analog, indicator-Bayes, LLM and red-team forecasters, and the ensemble; when ACLED reports a response (or the window passes) it resolves and every forecaster is scored.
6. **Over months,** precursor mining (120) tests whether `unexplained-move` and `pre-event-signal` findings precede Gulf escalations at all, with FDR control and out-of-sample validation. A real pattern becomes a validated precursor and a live forecaster; a spurious one is retired, which is just as useful to know.

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
CFTC COT on publicreporting.cftc.gov (Socrata; public domain; datasets: legacy `6dca-aqww`, disaggregated `72hh-3qpy`, TFF `gpe5-46if`, and their futures-and-options variants) [V]. CME: personal non-commercial use only, automated access prohibited → external provider idea [S]. ICE: end-of-day data paid; ICE publishes a free weekly COT for Brent and gasoil (location to verify) [S]. FRED API: free key; must show the "not endorsed or certified" notice; series marked copyrighted (VIXCLS, OVXCLS (Cboe), DCOILBRENTEU) are personal use only → `LOCAL_ONLY` [V]. EIA API v2: public domain, but spot prices are sourced from LSEG → `LOCAL_ONLY` for those series [V]. Wikimedia Pageviews: CC0, User-Agent with contact required [V]. GDELT 2.0: open with citation; DOC API limited to one request per 5 s [V]. IMF PortWatch: ArcGIS, no key, reuse with attribution, about 7 days lag [V]. NGA MSI and State Department advisories: keyless official APIs [V]. UKMTO: blocks some automated clients → external provider idea [V]. Metaculus: terms restrict AI/ML use without permission → excluded pending permission [S]. World Bank and ECB APIs: keyless [V].

Social platforms (checked 2026-10-11; prices change often, so 135 seeds them as editable settings): Bluesky Protocol Services (launched 2026-08-13) runs Jetstream v2 with an open, unauthenticated live tail and Network Replay archives that need an API token [S]. X API is pay-per-use: post reads $0.005 each (billed per post returned), recent counts $0.005 per request, full-archive counts $0.010 per request; legacy Basic/Pro tiers closed in 2026; deleted content must be removed within 24 h [S]. xAI's X search tool moved to per-result billing on 2026-09-21 (about $5 per 1,000 posts plus model tokens) [S]. Threads keyword search needs the `threads_keyword_search` permission, 2,200 queries per rolling 24 h [V]. Instagram hashtag search needs App Review for Public Content Access, 30 unique hashtags per rolling 7 days [V]. Meta Content Library is for academic and non-profit researchers and runs in Meta's secure environments [V]. Reddit's free tier is non-commercial, 100 queries per minute, with manual app approval [S]. YouTube Data API: 10,000 units per day, `search.list` 100 units [S]. Mastodon `/api/v1/trends/tags` is public with daily `uses` and `accounts` history [V]. Telegram added a scraping prohibition to its terms on 2026-02-03; TDLib is Boost-licensed with Java (JNI) and JSON interfaces [S]. TikTok's Research API is limited to non-commercial researchers [S]. Google Trends API remains an application-gated alpha [S].

---

## 15. Prompt breakdown

### 15.1 New prompts 116–139

| # | Prompt | Scope | Depends on |
|---|---|---|---|
| 116 | Evidence-Identity-And-Observability | originators, canonical events, evidence groups, quality flags; observability (declared, capture–recapture, presence proxy, receiver range), coverage layer; analysis manifests and replay; retrofits 099, 100, 101, 103 | 003, 006, 038, 040, 069, 098–103 |
| 117 | Market-And-Attention-Signals | `cftc-cot` (+ history backfill), `fred-series` (local-only), `wikimedia-pageviews`, `gdelt-timelines`, `worldbank-indicators`; `attention_topic`; optional `ice-cot` | 093, 066, 095, 073 |
| 118 | Situations-And-Storylines | situations, members, snapshots, linker, lifecycle, themes, briefs, merge/split; corroboration from evidence groups | 101, 098, 040, 099, 116, 117 |
| 119 | Change-Points-And-Event-Studies | region rollups, `SeriesReader`, series registry, findings + `foresignal-analytics`, CUSUM and BOCPD, event studies, pre-event signals, unexplained moves, co-occurrence | 038, 040, 093, 100, 103, 116–118 |
| 120 | Precursor-Mining-And-Library | precursor tests (renamed), event and series candidates over canonical events, BH/BY, validation, library, live `precursor-active` | 103, 116, 119 |
| 121 | Base-Rates-And-Historical-Analogs | base-rate library, reference imports, signatures, analog search | 116, 118, 119, 027 |
| 122 | Forecast-Ledger-And-Scoring | questions, resolvers (detectability-aware), forecasts, forecasters, scoring, CORP, bias monitor, ensemble, Platt, retrospective evaluation, `EstimativeLanguage` | 116, 120, 121 |
| 123 | Analytic-Benchmark-Harness | planted-truth scenarios, historical corpus, metrics, thresholds, gating | 101, 116, 118–122 |
| 124 | ✅ Checkpoint-Intelligence-Foundations | run and fix all tests from 001–123; golden statistics, determinism, look-ahead, duplicate inflation, manifests, ledger integrity, egress, durable cursors, synthetic benchmark baseline, invariants |
| 125 | Hypotheses-Indicators-And-ACH | sets, hypotheses, revisions, indicators with watch rules, updater (evidence groups, detectability-scaled absence), ACH, key assumptions, speculative links, information value, collection gaps, drafting | 017, 100, 116, 118, 120–123 |
| 126 | Series-Forecasts-Regional-Outlooks-And-Indices | series forecaster, regional 4-week outlooks, scored indices with fitted weights | 116, 119, 121, 122 |
| 127 | Impact-Projection | hazards × places × infrastructure, `projected-impact`, ledger questions | 104, 105, 116, 121, 122 |
| 128 | Analysis-Cycles-And-Assessments | cycles, packet, LLM roles (value-of-information priorities, per-cycle token budgets), assessments, export bundles, intel board (operator vs machine), notifications | 029, 102, 116–127 |
| 129 | Object-Graph-Workspaces-And-Annotations | object references and envelopes, relation graph, annotations and verdicts, precision metrics, workspaces with live context sync and deep links, sensor discovery, `/v1/contract` | 036, 064, 096, 098, 101, 104, 112, 116–128 |
| 130 | Principals-Groups-Sharing-And-Collaboration | principals and groups, notification preferences, access lists, TLP 2.0, tags, threaded comments and mentions, attachments and Admiralty-graded evidence items, investigations (owners, members, links), saved searches | 018, 023, 033, 043, 101, 109, 112, 113, 129 |
| 131 | Chronicles | layer bundles with capture modes, own partitioned history, thread, replay (state, tiles, tracks, series), lifecycle, storage estimates and caps, proposals from cycles, sharing | 014, 016, 036, 038, 042, 044, 111, 116–130 |
| 132 | Reports-And-Intelligence-Exchange | reports from tags, selections, investigations, chronicles and situations; deterministic drafts and optional narrative; publish with pinned refs; send to principals; signed packages between instances; read-only links | 018, 029, 055, 109, 111–113, 116, 122–131 |
| 133 | News-Coverage-And-Media-Bias | articles (metadata by default), outlet registry, licensed rating providers and consensus, story clustering via canonical events, coverage balance, blindspots and divergence, use in situations, hypotheses and assessments | 027, 066, 073, 101, 109, 116–119, 125, 128, 129 |
| 134 | Chronicle-Network-And-Discovery | evidence keys and MinHash fingerprints, discoverability under TLP, opaque links and clusters, deduplicated cluster timelines, federated sketch exchange with network salts, access requests, circular-reporting detection | 098, 109, 113, 116, 118, 130–132 |
| 135 | Social-Sources-And-Volume | operator-enabled platform adapters (Bluesky Jetstream on by default; Bluesky search, Mastodon, Reddit, YouTube, X with spend cap, Threads, Instagram hashtags; other platforms through EDP providers), `social-post` records, topic `social` blocks, term index, deletion sync, denominator-scaled hourly volume series and baselines, `social-velocity` findings, social-attention observability | 005, 010, 024, 036, 040, 093, 098, 109, 116, 117, 119, 131 |
| 136 | Narratives-Claims-And-Media | narratives as canonical events counted by origin, multilingual deterministic claim extraction with optional LLM extraction, claims as graded evidence items, PDQ image fingerprints and reuse findings, propagation graphs across platforms and news, coordination indicators | 026, 027, 036, 043, 098, 116, 118, 119, 125, 129, 133, 134, 135 |
| 137 | Social-Physical-Correlation | claim-versus-physical comparisons with detectability, lead times, attention gaps, cue-driven searches, social precursor candidates, `social-claim` ledger forecaster, lead-time statistics, situation, hypothesis, assessment and chronicle integration | 017, 018, 100, 116, 118–125, 128, 131, 133–136 |
| 138 | External-Data-Bindings-And-Provider-Kit | bindings for atomic EDP streams (series, canonical families with declared originators and coverage, social and news pipelines, evidence, indicators, forecast questions as scored external forecasters, places, situations, chronicles), preview, suggested bindings, provider kit and provider-ideas catalogue | 024, 036, 093, 098, 116, 118, 119, 122, 125, 129, 131, 133, 135–137 |
| 139 | ✅ Checkpoint-Intelligence | run and fix all tests from 001–138; determinism, look-ahead, wording, egress, duplicate inflation, manifests, benchmark, collaboration and TLP, chronicle independence, exchange round trip, news independence, chronicle-network disclosure, circular reporting, social independence, absence and cue loops, external data (pending sources, bindings, independence, external forecasters), invariants |

### 15.2 Changes to unexecuted prompts

| Prompt | Change |
|---|---|
| 019 | Analytic retention classes come in through descriptors (Conventions); intelligence tables are outside RetentionJob |
| 038 | Pointer: 119 adds region resolutions 2 and 3 |
| 102 | Pointer: 128 reuses analyst profiles and the proposal pipeline |
| 103 | `hypothesis` renamed **precursor test** (`precursor_test`, `/v1/precursor-tests`, `test_precursor`); prompt renamed `103-Backtesting-And-Precursor-Tests` |
| Adapter prompts | Retention defaults per the class table unless the prompt names a longer window |
| 023 | Principals introduced (people and agents own tokens) |
| 101 | Case files renamed **investigations** (`/v1/investigations`, `display_id` INV-yyyy-nnnn); 130 extends them |
| 116 | Prompt templates are versioned resources exposed at `/v1/prompt-templates/{id}` |
| 122 | Forecast probability history for trend charts |
| 128 | On-demand per-situation assessments |
| 129 | Relation types `precedes` and `causes` (never machine-asserted); evidence facets; satellite capability catalogue for sensor discovery |
| 017 | `hysteresisMeters` for geofence `enter`/`exit` (no edges from boundary jitter) |
| 022 | Performance smoke: sustained ingest at NFR-2 and 2×, SSE fan-out, partition maintenance under load |
| 088 | Optional `gfw-sar-detections` (SAR vessel detections) |
| 099 | New generators: `derived-cable-risk` (anchor drag near submarine cables), `derived-dark-vessel-match` (AIS gaps × SAR detections), `derived-lightning-holdover` (holdover lightning ignitions), `derived-hf-blackout` (HF propagation anomalies) |
| 100, 101, 102, 103 | Retrofitted by 116 (detectability, evidence groups, manifests) |
| 012, 022, 041, 057, 097, 124, 139, 142 | Invariants check against `docs/Invariants.md` |
| 141 | SDKs include `ObjectRef`, the envelope and a `WorkspaceSync` helper |
| 143 | Disk-full drill, practical capacity bounds, intelligence job costs |
| 140–144 (v1.2 116–120) | Renumbered; test-run lists and cross-references updated |
| 117 | Attention topics gain a `social` block (terms per language, hashtags, accounts, channels, platforms, places, entities) used by 135 |
| 116 | Originator class `social`; canonical families `social-narrative`; observability family `social-attention` (135–136) |
| 125 | Evidence source `social-claim`; indicator state `reported` (social) distinct from `observed` (137) |
| 066, 072, 075, 077, 081, 093, 117, 133 | Former exclusions reworded: these prompts build the official routes; the rest are external EDP provider ideas (`docs/Source-Catalog.md`) |
| 024 | Builds the EDP core (manifest, registration, push and pull, validation, layer streams); 138 adds bindings |
| 141 | EDP provider libraries (Python, TypeScript) and `foresignal edp` CLI commands |
| 018, 131, 123 | Notification types `social.divergence`, `social.attention_gap`; chronicle layers `social-narratives`, `social-volume`; social benchmark scenarios (135–137) |
| 124–139 | Late 2026-10-11 changes: foundations checkpoint inserted as 124 (former 124–133 → 125–134); social signals (135–137) and external data bindings (138) inserted; the intelligence checkpoint moved 134 → 139 and the tail 135–139 → 140–144 |

---

## 16. Rejected or deferred

| Idea | Decision |
|---|---|
| Fringe and specialist adapters in the core (prediction markets, Metaculus, VIEWS, CME/ICE, Electricity Maps, Facebook, TikTok, Telegram, unofficial and browser-based sources) | **Moved out (2026-10-11):** external EDP providers in separate projects; nothing is excluded from use. |
| Person-level analysis, insider identification | Never. |
| Neural forecasting models | Charter non-goal; statistical fitting only. |
| Acting on a probability automatically | Forecasts drive only operator-written rules on `analytic-finding` records. |
| Federating hypotheses and forecasts across instances | After 1.0 (113 carries intel events only). |
| Counterfactual engine (matched periods, synthetic controls) | After the base-rate, event-study and benchmark machinery is validated in production (after 1.0). |
| Full temporal knowledge graph (graph database, reasoning) | 129's relation tables cover the need for 1.0; revisit if queries outgrow PostgreSQL. |
| Earth-observation change detection (VIIRS night lights, Sentinel-1 flood, Sentinel-2 change) | After 1.0; needs raster differencing beyond 042–046. STAC footprints (096) and GFW SAR detections (088) cover the first uses. |
| Public procurement (SAM.gov, USAspending) | Low signal for this product; revisit on demand. |
| Argo floats, GCN/IGWN multi-messenger alerts, local RF occupancy | External EDP provider ideas (`docs/Source-Catalog.md`), not built in. |
| ADS-B barometric-vs-geometric altitude pressure mapping | Research-grade; out of scope for 1.0. |
| Satellite tasking and commercial imagery ordering | Out of scope for the broker; a client or operator integration can push results back through custom sources (024). |
| Imagery change detection (building counts, disturbed area) | Requires computer-vision models; operators can run their own pipeline and push results as records through custom sources (024), where they become ordinary evidence. |
| Personal "my news bias" reading analytics | A client feature; Foresignal does not track what people read. |

---

## 17. External review log (2026-10-11)

Three external reviews (ChatGPT, Gemini, Grok) of the v1.3 design, and what was done:

| Recommendation | Source | Disposition |
|---|---|---|
| Canonical evidence identity; independence of evidence; adversarial evidence evaluation | ChatGPT (P0, E) | **Adopted:** 116 (canonical events, evidence groups, quality flags), consumed across the block |
| Model what could have been observed (coverage and detection probability) | ChatGPT (P0); Grok (visible coverage layer) | **Adopted:** 116 observability model and coverage tiles; absence rules everywhere |
| Reproducible analysis-run manifests | ChatGPT (P1) | **Adopted:** 116 manifests and replay |
| Analytic benchmark before autonomous features | ChatGPT (P1) | **Adopted:** 123 benchmark harness with gating |
| Run fast tests every prompt | ChatGPT (P1); Grok | **Not adopted** (owner decision): tests stay at checkpoints; checkpoints now also verify the invariants registry |
| Machine-readable decision and invariant registry; one source of truth per concern | ChatGPT; Grok | **Adopted:** `docs/Invariants.md` (ids, owners, tests), checked at every checkpoint; source-of-truth table in Conventions |
| Ingest, partition and SSE load benchmarks before dense feeds | ChatGPT; Gemini | **Adopted:** 022 performance smoke; 143 capacity and disk-full drill |
| Temporal knowledge graph | ChatGPT (A) | **Adopted in light form:** 129 relation graph with validity, confidence, provenance and common-dependency queries |
| Active information-gap detection; "what would have to be true" | ChatGPT (C); Grok | **Adopted:** 125 information value and collection gaps; 128 reports them |
| Counterfactual engine | ChatGPT (B) | **Deferred** (§16) |
| Operator-vs-machine scorecard as a first-class surface | Grok | **Adopted:** 128 intel board and assessments |
| Exports of scored objects | Grok | **Adopted:** 128 assessment export bundles (JSON, GeoParquet) |
| Budget by cycle and information value | ChatGPT | **Adopted:** 128 per-cycle token budgets and value-of-information priorities |
| OpenAPI missing intelligence endpoints | Gemini | **Not a defect:** the spec grows with each prompt by convention (Conventions "Spec drift"); 020 tracks unbuilt operations |
| Retention deadlock from lagging cursors | Gemini | **Already handled:** the consumer clamp releases after 2 × window and alarms (Core-Data-Model §13); the new cursors inherit it (Conventions) |
| Geofence flapping from position jitter | Gemini | **Adopted:** 017 `hysteresisMeters` |
| Single-writer ceiling, SSE memory | Gemini | **Measured:** 022 performance smoke (writer, SSE heap per emitter), 143 soak |
| Submarine-cable anchor-drag guard; dark vessels × SAR; holdover lightning ignitions; HF propagation anomalies | Gemini | **Adopted:** 099 generators; 088 optional SAR detections |
| BGP/outage × NOTAM/fire/conflict co-occurrence | Gemini | **Covered:** 119 co-occurrence mining over all signal families |
| RIS Live, OSM infrastructure extracts | Grok | **Already planned:** 092 (optional RIS Live), 104 (operator-supplied OSM extract) |
| Argo, GCN/LIGO, Electricity Maps, procurement, Earth-observation change detection | Grok; ChatGPT | **Deferred** (§16) |
| Shared object contract and cross-application context sync for the frontends | ChatGPT (frontend discussion) | **Adopted:** 129 and `docs/Client-Contract.md` |
| Selected-object dossier, context summary, permission-aware actions, forecast issuer labels | Mockup review | **Adopted:** 129 |
| Tag many items, build a report, send it to others (same server or other instances) | Owner | **Adopted:** 130 (tags, sharing, TLP), 132 (reports, signed packages) |
| Monitoring threads with their own history, replay, AI-proposed | Owner | **Adopted:** 131 chronicles |
| Ground News-style source lean and coverage analysis | Owner | **Adopted:** 133, with licensed providers only |
| Investigations, evidence graph, Admiralty grading, classification markings, multi-user notes (mockups) | Owner | **Adopted:** 101 rename, 129 relations, 130 evidence items, TLP and comments |
| Situation "view prompt" and "re-run" | Mockup | **Adopted:** 116 prompt templates, 128 on-demand assessments |
| Three kinds of memory (observations, interpretation, chronicles) | ChatGPT | **Adopted** as a design invariant (§3.1) |
| Chronicles discovering related chronicles without leaking or double counting | ChatGPT | **Adopted:** 134 chronicle network |
| Full OpenAPI before parallel client development | Owner | **Adopted:** the complete target spec is authored up front (Conventions "API-first") |
| Social media as an intelligence signal: velocity, narratives, propagation, claim contradiction, media reuse, coordination, attention divergence, cue-driven search, measured predictive value | Owner (with an external proposal) | **Adopted:** 135–137, with corrections: Facebook and Instagram have no general public-content route (Instagram hashtag search only); X is billed per post read and per counts request, so standing topics use counts and posts are fetched on cues; model-mediated search is a sample, not a baseline; text-derived places carry confidence; reuse detection is limited to fingerprinted media; coordination is reported as indicators, never account labels; silence requires social-coverage observability |
| A generic JSON protocol so third parties add sources without changing Foresignal; fringe inputs live outside the project | Owner | **Adopted:** External Data Protocol 1.0 ([External-Data-Protocol.md](External-Data-Protocol.md)); core in 024 (manifest, registration tokens, push and pull, validation, layer streams), bindings and provider kit in 138, provider libraries and CLI in 141; the fringe adapters drafted earlier the same day were moved out to provider ideas |
| Final pre-build review: EDP contract gaps (version policy, `sourceId`, empty transport, forecast values, quantile order, geometry, period formats, unique streams) | ChatGPT | **Adopted:** schemas and EDP §2–§3 tightened; server-side checks in 024 |
| Snapshot absence must never delete on partial, failed or stale data | ChatGPT | **Adopted:** EDP §3.2 snapshot pages, scopes, window and absent-ratio guard (EXT-5) |
| Provider times must not defeat transaction time | ChatGPT | **Adopted:** availability = max(provider time, receipt) (EDP §3.5, EXT-4) |
| Provider identity is not evidence independence | ChatGPT | **Adopted:** item-level `origin`; independence by originator only (EDP §3.4, 138) |
| External providers are an untrusted boundary (SSRF, DNS rebinding, schema DoS, media, private and licence propagation) | ChatGPT | **Adopted:** EDP §7, `EdpUrlGuard`, schema limits (EXT-6) |
| Full-lifecycle integration test (ingest, update, delete, reintroduce, as-of, cursor resume, retention, restart) | ChatGPT | **Adopted:** `RecordLifecycleIT` in checkpoint 022 |
| Run targeted tests earlier; mid-intelligence checkpoint | ChatGPT; Grok | **Adopted (owner decision):** sanctioned narrow runs in 007–009; new checkpoint 124 after the benchmark harness; other feature prompts stay checkpoint-only |
| Treat 022 and runtime verification as hard gates | Grok | **Adopted:** performance gate stop condition (000), Roadmap §2 |
| One authoritative prompt order | ChatGPT | **Adopted:** numbering banner in Core-Data-Model; Roadmap is authoritative |
| Argo, multi-messenger alerts, RF occupancy | Grok | **Adopted as external provider ideas** (no core change) |
| foresignal.com name collision | Grok | **Noted** in the Charter; first-start banner and docs name foresignal.ai (144) |
| Do not police the operator | Owner | **Adopted:** single-operator tool; official-API platforms are opt-in adapters with documented terms and costs, and everything else is an external provider the operator may connect (Conventions "Operator choice") |
