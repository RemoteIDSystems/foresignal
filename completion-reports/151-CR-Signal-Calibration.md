# 151-CR-Signal-Calibration

| | |
|---|---|
| Prompt | 151-Signal-Calibration.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `10e068af9b4ac7de006263806f7936aa98429db5` |
| Date | 2026-10-22 |

## A — the outcome → calibration layer

A new generic layer turns the uncalibrated `EventScorer` score into a calibrated probability, reusing the
forecast side's existing `forecast/score/Calibration.java` (CORP isotonic via PAV, Murphy Brier, 10-bin
reliability table) rather than reinventing it.

- **`IntelOutcomeEntity` / table `intel_outcome`** — one resolved real-world outcome: `(scope, subjectKind,
  subjectId, rawScore, outcome 0|1, resolvedAt)`, unique on `(subjectKind, subjectId)` so re-resolving a
  subject updates in place and never double-counts.
- **`IntelCalibrationEntity` / table `intel_calibration`** — the fitted calibration of one scope: the CORP
  recalibration `curve` (jsonb), the reliability `bins` (jsonb), the `brier`, the `sampleN` and the
  `calibrated` flag.
- **`IntelCalibrationService`** —
  - `recordOutcome(scope, kind, subjectId, rawScore, outcome)` upserts the outcome and refits the scope;
    idempotent (an identical re-resolution writes nothing).
  - `refit(scope)` runs `Calibration.corp`/`bins`/`murphy` over the scope's outcomes. `calibrated` is true
    only once `sampleN >= Calibration.CORP_MIN` (50) **and** a non-empty curve exists; otherwise the raw
    score stands.
  - `calibrate(scope, rawScore)` linearly interpolates the fitted curve (clamped to its end points), or
    returns empty when the scope is not yet calibrated.
  - `reliability(scope)` / `all()` expose the state for the read endpoint.

**Scopes.** `intel-event` for intelligence events, `precursor-active` for precursor findings
(`IntelCalibrationService.SCOPE_EVENT` / `SCOPE_PRECURSOR`).

## B — applying calibration and recording outcomes

- **`IntelEventService.rescore`** now sets `rawConfidence` to the `EventScorer` score, then, where a mapping
  exists, reports the calibrated probability as `confidence`, recomputes the label from it and sets
  `calibrated=true`; otherwise it keeps the raw score and `calibrated=false` (the prior behavior). The new
  `raw_confidence` column (migration `V2`) keeps both visible. The calibration bean is injected
  `@Autowired(required = false)`, so the service still works where the layer is absent.
- **Intel-event outcomes** are recorded in `patch` when an operator or agent moves an event from a
  non-terminal status to `resolved` (outcome 1 — the finding was real) or `dismissed` (0 — a false positive).
  Auto-resolution on quiet asserts no ground truth and records nothing.
- **Precursor-active outcomes** flow through the forecast system, which already opens an `event-exists`
  question from each `precursor-active` finding (prompt 122) that resolves exactly "did the expected
  consequent occur in its window?". `PrecursorOutcomeResolver` (scheduled, off under
  `foresignal.analytics.scheduled=false`) reads resolved precursor questions, pairs the finding's stated
  probability with the binary outcome (`yes → 1`, `no → 0`) and records it. This reuses the system's own
  resolution rather than building a second, fragile consequent checker.

## C — the first-class confidence-upgrade event

- **`TrustCrossing`** computes the upgrade crossings between two trust snapshots: status `emerging → active`,
  the `confidenceLabel` tier (`low < moderate < high`), the `evidenceQuality` tier
  (`insufficient < weak < moderate < strong`), `uncorroborated → corroborated` (evidence quality reaching two
  or more families) and `calibrated` turning true. Only upgrades fire; downgrades and terminal status changes
  emit nothing.
- **`IntelEvent.UPGRADED`** (`intel.event.upgraded`) is published **in addition to** the `updated` event
  whenever a change crosses a threshold, with its own log entry (a distinct, strictly increasing cursor) and
  an `upgrade` member in the summary listing the crossings, so a consumer never has to diff successive
  summaries. Emitted from `IntelEventService` (`patch`, `addMember`, `removeMember`, `merge`) and from the
  recipe path in `CorrelationEngine.write` (whose `write` now returns a `Written(announce, upgrade)` pair that
  `persist` publishes after commit).

## D — read surface

- **`GET /v1/intel/calibration`** (`getIntelCalibration`) and **`GET /v1/intel/calibration/{scope}`**
  (`getIntelCalibrationScope`), scope `read`, return whether a scope is calibrated, the resolved sample size,
  the Brier score, the reliability table and the CORP curve. Covered by the existing `GET /v1/intel/**` read
  rule in `AccessPolicy`.
- `IntelEventMapper.full` now carries `rawConfidence`; the `list_intel_events` MCP tool description now says
  confidence is a calibrated probability where enough outcomes have resolved, otherwise the raw score.

## E — API 1.3.0

- `info.version` in `docs/OpenAPI.json`, `version` in `build.gradle.kts` and `ApiVersionTest.RELEASE` are all
  `1.3.0`, with a new changelog entry.
- `docs/OpenAPI.json`: added `rawConfidence` to `IntelEventSummary` and `IntelligenceEvent.inference`, updated
  the `confidence`/`calibrated` descriptions, raised the `confidence` `maximum` from `0.95` to `1` (a
  calibrated probability can exceed the raw cap), added the two calibration operations and the
  `IntelCalibration` schema. Postman regenerated with `scripts/generate-postman.py`.

## What runs

- `./gradlew compileJava compileTestJava` — passes.
- No tests run (owner directive, 2026-10-17). Tests written but **not** executed:
  `TrustCrossingTest` (each crossing fires; none on no-change or downgrade) and `IntelCalibrationServiceTest`
  (interpolation clamps/interpolates; `calibrated` flips at `CORP_MIN`; an uncalibrated scope maps nothing, a
  calibrated one returns a probability).
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (the prompt says run no other Gradle checks).
  US English verified manually against `gradle/us-english/british-words.txt`: no British inflection is
  introduced by the diff. `docs/OpenAPI.json` validated as well-formed JSON.

## Owner decisions / notes for the next prompt

- **`checkApiCompat` (owner run):** the only non-field-addition in the spec is `IntelligenceEvent` /
  `IntelEventSummary` `confidence.maximum` widening `0.95 → 1` on a **response** schema (needed because a
  calibrated probability can exceed the raw 0.95 cap). This is a response-side relaxation; confirm oasdiff
  treats it as non-breaking when the gate runs.
- **Calibration scope granularity:** outcomes are pooled per kind (`intel-event`, `precursor-active`), not per
  event type or per precursor, so a scope reaches the 50-outcome threshold sooner. If per-type calibration is
  wanted later, `IntelCalibrationService` already takes `scope` as a parameter.
- **Flyway `V2`:** first additive migration after the `V1` baseline; the owner's baseline-verification steps
  (144 CR) now have a second migration to apply.
- No tests, runtime, migration or soak verification was performed; all deferred to the owner at code-complete.
