# 151 — Signal calibration + confidence-upgrade events

**Goal.** Turn intel-event / precursor **confidence from an uncalibrated score into a calibrated
probability**, and emit a first-class "confidence upgraded" signal when a signal crosses a trust
threshold. This is the core of the owner's "accurate" requirement: today confidence is explicitly *not*
a probability.

**Read first (the gap is real and stubbed).**
- `src/main/java/ai/foresignal/intel/EventScorer.java` — `c = 1 − Π_f(1 − w_f·r_f)`; the class doc says
  "a score, not a calibrated probability"; `calibrated` is hardcoded `false` (~L9-11).
- `IntelEventService.rescore` (~L455-491) recomputes confidence/label/evidenceQuality on every
  membership change; `CorrelationEngine.write` rescores on each recipe hit (~L838-864) and sets
  `calibrated=false` (`CorrelationEngine.java:500`); `IntelligenceEventEntity.calibrated` (~L75-76).
- `ListIntelEventsTool.java:63` ("uncalibrated score, not a probability") — the stub a never-built
  "prompt 103" was to replace.
- **Reuse, don't reinvent:** the forecast side already has real calibration —
  `src/main/java/ai/foresignal/forecast/score/Calibration.java` (CORP isotonic via PAV, Murphy
  decomposition, reliability bins). Factor or mirror it.
- Precursor probabilities: `PrecursorRules.probability` (shrink-to-base-rate) and `PrecursorLiveWatch`.

**Build.**
- Add an **outcome → calibration** layer: record the resolved real-world outcome for intel events and
  `precursor-active` findings (did the expected consequent occur in its window?), accumulate
  reliability per confidence bin, and map the raw `EventScorer` score to a **calibrated probability**
  using the existing `Calibration` PAV/CORP machinery. Set `IntelligenceEventEntity.calibrated=true`
  only where a calibrated mapping is actually applied (enough resolved history); otherwise leave the
  raw score and `calibrated=false` as today.
- Add a **first-class confidence-upgrade event**: when an event crosses `uncorroborated→corroborated`,
  `emerging→active`, or an `evidenceQuality` tier, emit a typed stream/notification signal (not just
  the generic `UPDATED`), so a consumer never has to diff successive summaries.
- Expose calibration state on the event view and/or a read endpoint (reliability table, Brier,
  sample size) so the admin app can show "how calibrated is this".
- **API bump to 1.3.0:** read the current `info.version` in `docs/OpenAPI.json` (1.2.0) and
  `ApiVersionTest.RELEASE`, set both (and any other version pin) to **1.3.0**. Add the new/changed
  response fields and endpoint to `docs/OpenAPI.json`; regenerate Postman (`scripts/generate-postman.py`
  task). Keep `checkApiCompat` additive (new fields/paths only).

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence.md`, run
**no** tests or other Gradle checks. Write tests (calibration maps scores→probabilities; `calibrated`
flips once there's enough resolved history; upgrade events fire on each crossing) but do not run them.
US English throughout.

**Done when.** Confidence can be reported as a calibrated probability backed by resolved outcomes;
`calibrated=true` appears where a mapping exists; threshold crossings emit a distinct upgrade signal;
API is 1.3.0; spec + Postman updated; compile passes.

**Commit.** `151: Signal calibration + confidence-upgrade events` + `completion-reports/151-CR-*.md`
(repo format, 40-char hash). **Stage only this prompt's files**; leave `Dockerfile`, `scripts/smoke.sh`
and unrelated uncommitted files untouched.
