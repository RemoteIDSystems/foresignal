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
   inference server** (its own container) that the Java backend calls over HTTP as a series
   forecaster — the same pattern as the local-first LLM providers (Ollama, etc.): operator-provisioned,
   not part of the Java service, so "backend is 100% Java" stays intact. In-JVM via ONNX/DJL would
   avoid the extra process, but TimesFM's architecture makes that export non-trivial; the
   external-server path is the pragmatic, rule-consistent one.

**Status.** Candidate for a spike on the backend. Not scheduled. Owner's call whether and when.
