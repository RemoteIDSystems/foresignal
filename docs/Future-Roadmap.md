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
