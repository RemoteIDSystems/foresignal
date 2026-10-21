# Foresignal — Future Candidates (post-v1.0)

Ideas recorded for later consideration. **Nothing here is committed or scheduled** — each is a
candidate to evaluate, with the reasoning captured so it survives the conversation it came from. Add
an entry when an idea is worth not losing; promote one to the [Roadmap](Roadmap.md) only on the
owner's decision.

---

## TimesFM — time-series foundation model as an ensemble forecaster

_Added 2026-10-19._

**What.** [TimesFM](https://github.com/google-research/timesfm) is Google Research's pretrained
time-series foundation model (decoder-only, zero-shot, univariate and multivariate). It outputs
point and **quantile** (decile 0.1–0.9) forecasts and supports past/future dynamic covariates. It
ranks first on the public forecasting benchmarks (fev-bench, TIME, GIFT-Eval).

**Where it could fit.** As a forecaster behind the existing forecast ensemble (forecasters, ensemble
refit, calibration, scorecard), specifically for the numeric **series forecasts** (prompt 126,
currently ETS via Hipparchus): zero-shot forecasts across the many diverse series the broker tracks
(WTI, flood gauges, Kp, counts, …), with quantiles feeding the prediction intervals already
surfaced, and series trajectories driving threshold-crossing probabilities for ledger questions that
reduce to "will this series cross X by date Y." It does **not** forecast judgmental or geopolitical
questions — those stay LLM / analog / base-rate. It fits the "deterministic, budgeted,
works-without-an-LLM" stance better than an LLM forecaster.

**Two gates, both resolvable.**

1. **License — use 2.5, not 3.0.** TimesFM **2.5** (200M params, 16k context) ships **Apache-2.0**:
   self-hostable, commercial use allowed. TimesFM **3.0** weights carry a non-commercial,
   no-production license (pushing production use to Google Cloud), unsuitable for a self-hosted, open
   project. Pin 2.5; the inference code is Apache-2.0 in both.
2. **Runtime — keep the backend Java.** TimesFM is PyTorch/Python. Run it as an **optional external
   service** (its own container) integrated through the external-analyzer pattern below: an EDP
   provider that reads the series it needs, computes, and pushes `forecast` items back (scored as a
   forecaster). No Python enters the backend, so "backend is 100% Java" stays intact. In-JVM via
   ONNX/DJL would avoid the extra process, but TimesFM's architecture makes that export non-trivial;
   the external-service path is the pragmatic, rule-consistent one.

**Status.** Candidate for a spike on the backend. Not scheduled. Owner's call whether and when.

---

## External analyzer / on-demand analysis callback (EDP round-trip)

_Added 2026-10-19._

**Idea (owner).** Let an external compute service (e.g. the TimesFM server above) stand up
separately and integrate through a round-trip: Foresignal sends input data to the analyzer, the
analyzer computes, and the result returns to Foresignal through a callback/webhook — the general
pattern for any external analyzer or enricher, not just forecasting.

**Most of it already exists (EDP, prompts 024/138).**
- **Return path (inbound):** the EDP `forecast` item carries `probability` or `quantiles` (p05–p95)
  bound to a ledger `questionRef` and is **scored like any forecaster** (122); `atomic` items +
  operator **bindings** route a derived value/forecast to a series, indicator, question, place,
  situation or chronicle. An analyzer pushes results back via `POST /v1/external/sources/{id}/items`
  and they land as first-class forecasts/series.
- **Foresignal-initiated request (outbound):** `transport.pull` (Foresignal polls the provider's
  `itemsUrl`) and `transport.search` (Foresignal calls `GET {url}?q=&since=&bbox=…` and expects a
  batch, used for 137 cues) are already Foresignal-initiated calls to an external endpoint.
- **Outbound data:** sinks (110) stream records out; the read API lets the analyzer fetch the series
  it needs.

So **continuous** analysis needs **no new primitive**: the analyzer is an EDP provider that reads,
computes and pushes results back.

**What's new (the piece to design).** A **request-scoped, on-demand round-trip**: Foresignal sends a
specific analysis request (this series, this horizon, for this question) with a **correlation id**,
and the analyzer returns the correlated result to a **callback**. This is for synchronous-ish,
on-demand analysis ("forecast this ledger question now") rather than the provider's own schedule, and
generalizes `transport.search` into an **analysis-request** transport with correlation + callback,
usable by any external analyzer/enricher.

**Status.** Design candidate. The continuous path is usable today via EDP; the on-demand
request/callback is the new primitive to design. Owner's call.

---

## Relationship intelligence / link analysis (Benjis-style) — the engine is already here

_Added 2026-10-21._

**Reference.** [Benjis.ai](https://benjis.ai/waitlist/) (waitlist page, so partly inferred): entity
resolution + relationship discovery / link analysis + pattern detection + alerts + graph dashboards,
over aggregated **financial, corporate-ownership and public-records** data, for fraud, due diligence,
financial crime and compliance.

**What Foresignal already has (the capability stack, built).**
- **Aggregation:** ~300–500 sources via the adapter framework + the EDP for external providers.
- **Entity resolution:** the entity registry — `resolveEntity`, `mergeEntities`, entity keys; a
  merged entity resolves to its survivor (098).
- **Link analysis / relationship graphs:** `POST /v1/graph` (`queryGraph`), `findCommonDependencies`,
  operator `relations`, `listRelatedObjects`, dependency paths, the object/dossier `related` graph (129).
- **Correlation:** intel correlation recipes (`intel/recipes`, `testRecipe`), canonical/intel events,
  investigations (116, 101).
- **Pattern / anomaly detection:** precursors + mining, anomaly/pattern trigger operators, hypotheses
  (ACH), social narratives + claims (100, 120, 125, 135–137).
- **Alerts on new connections/patterns:** the trigger engine + alerts + notifications (017/100).
- **Predict + report:** forecasts/ensemble/calibration, LLM assessments, reports, briefings (122, 128, 132).

So the chain Benjis describes — aggregate → resolve entities → find connections → detect patterns →
alert → predict/report — is already first-class here.

**What differs is the data domain, not the capability.** Foresignal's sources are physical/geospatial/
security telemetry + news/social/sanctions; Benjis targets **financial transactions, corporate /
beneficial ownership and public records** for fraud/compliance. Foresignal overlaps on sanctions,
news/outlets and social, and on corporate facts via Wikidata, but lacks the financial-flow and
beneficial-ownership depth.

**To offer the same thing, add data — not an engine.** (Benjis's exact feeds aren't published; this
compares our Source-Catalog against the domain they operate in.)

_Already in the catalog (domain overlap), so we are not starting from zero:_ sanctions / OFAC SDN
screening (Source-Catalog ~L415–424, 701, 744), SEC EDGAR filings (L422, 745), CFTC COT + market /
attention / structural series (prompt 117; L835, 1052), news & media ratings (133), social platforms
(135–137), and corporate facts via Wikidata.

_Not yet — the entity-ownership + financial-crime public-records layer (via EDP / adapters):_
- **Corporate registries + officers/directors** — OpenCorporates-style company data, **GLEIF/LEI**
  entity hierarchies, Companies House, etc.
- **Beneficial-ownership (UBO) registers** — who really controls an entity.
- **Financial transaction / flow data** — we have market series, not transaction-level flows.
- **PEP lists** (politically exposed persons).
- **Public records for identity/fraud** — court records / dockets, property / land records,
  address / identity data, business associations.
- **Leak / shell-company network datasets** — ICIJ Offshore Leaks / Panama-Papers-style graphs.

Cheapest starting points are the extend-existing items — **sanctions → PEP** and **EDGAR →
ownership** — and several of the rest (registries, GLEIF, ICIJ) have open or bulk feeds.

**A few domain analytics on the existing graph:** ownership-chain and shared-attribute
(address / phone / officer) path queries, and a fraud-oriented recipe set — expressible on the
current relation graph and correlation-recipe engine.

**Status.** Capability already present; matching the fraud/compliance use case is a data-sourcing +
light-analytics effort, not a new engine. Owner's call whether to pursue that domain.
