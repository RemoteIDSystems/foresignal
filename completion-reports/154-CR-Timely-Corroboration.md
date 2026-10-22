# 154-CR-Timely-Corroboration

| | |
|---|---|
| Prompt | 154-Timely-Corroboration.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `83ae2470f8b75eb33b8025b00b51ef0638e78cac` |
| Date | 2026-10-22 |

## What was added

Three things, so a single claim surfaces at once and upgrades as evidence arrives, independence counts
modalities as well as originators, and the "promote to a trusted signal" gates are config, not constants. The
deterministic fact-grounding core (`SocialPhysicalComparator.decide`, `ComparisonService`) is **unchanged**.

### A — modality-aware independence

`Modality` (new) derives a sensing modality — `sensor`, `imagery`, `official`, `social`, `media`, `crowd`,
`operator`, `derived`, else `other` — deterministically from the originator class, a dimension distinct from the
signal family (what was observed) and the originator root (who observed it). `Originator.CLASSES` gains
`imagery` so overhead-imagery sources can be classed as such.

`EventScorer.MemberView` gains a `modality`; `EventScorer.score` counts the distinct modalities among the
independent (strongest-per-group) members and, when the quality policy's `requireModalities > 0` (or a recipe's
`activateAt.modalities` is set), withholds `strong` / `active` until a candidate spans that many modalities.
Modality is resolved server-side from the originator registry in `IntelEventService.rescore` and
`CorrelationEngine.view`, never taken from a client.

### B — tunable thresholds

The hardcoded gates became config, every one defaulting to its historical constant:

- `EventScorer.Quality(strongFamilies=3, strongGroups=2, moderateFamilies=2, requireModalities=0)` replaces the
  inline `3 && 2` / `2` in `EventScorer.quality`. `EventScorer.score(views, contradictions)` still exists and
  delegates to `Quality.DEFAULT`, so every other caller is unaffected.
- `SituationLifecycle.meetsActivation` / `afterMembers` gained `(minFamilies, minGroups)` overloads (defaults
  2 & 2); `SituationDeriver` reads them from the policy.
- `RecipeDefinition.ActivateAt` gained `modalities`; `CorrelationEngine` honours it in both the candidate
  (`build`) and the persisted (`write`) activation checks.

The quality thresholds are recorded in the score manifest (`scoreManifest`) and read back by
`EventScorerReplayer`, so a replay reproduces the evidence-quality label exactly; a manifest without them (every
pre-154 manifest, all written under the defaults) replays against the defaults, so none became
`not-replayable`.

### C — automatic corroboration of lone events

`AutoCorroboration` (new) searches `record_version` for independent observations near a lone event in space and
time, reusing the correlation engine's `ST_DWithin` presence predicate and `EvidenceService` for independence:
at most one corroborator per independent originator root, never a root the event's own members already
represent. `IntelEventService.create` runs it before the first score (policy-gated), attaches the found records
as supporting members (`addedBy = auto-corroboration`, family from `SignalFamilies.forKind`), re-scores, and —
for an analyst-proposed event, which starts `emerging` — auto-activates once the members span the policy's
activation families and independent groups. So the event surfaces immediately and corroborated, instead of
waiting for someone to pin members by hand.

### D — policy document and spec

- `IntelCorroborationService` (new) holds the `intel.corroboration` document (`eventQuality`,
  `situationActivation`, `autoCorroborate`) with typed accessors and validating `get()` / `put()`.
- `IntelController` exposes `getIntelCorroboration` (GET `/v1/intel/corroboration`, read) and
  `updateIntelCorroboration` (PUT, admin).
- OpenAPI 1.3.0: the two operations, the `IntelCorroboration` schema, `activateAt.modalities` on the recipe
  definition, and a 1.3.0 changelog entry. Postman regenerated (`scripts/generate-postman.py`): 852 requests,
  the two new operations added.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `EventScorerModalityTest` — the default thresholds keep the historical strong rule; a modality requirement
    withholds strong until enough distinct modalities are present; lowered thresholds make a two-family event
    strong.
  - `ModalityTest` — each originator class maps to its modality; an unmapped/null class is `other`; `imagery`
    is a recognized class.
  - `SituationActivationThresholdTest` — the default 2 & 2 rule, custom thresholds, and emerging → active under
    the configured thresholds.
  - `IntelCorroborationServiceTest` — defaults with no setting, a PUT/GET round-trip through the typed
    accessors, and rejection of an unknown member and an out-of-range threshold.
  - `AutoCorroborationSpecTest` — the pure parts (scan cap, snapshot shape).
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified manually
  against `gradle/us-english/british-words.txt` — no British inflection in the diff (the pre-existing
  `ais-behaviour` family ids are persisted wire values and were not touched). `docs/OpenAPI.json` validated as
  well-formed JSON.

## Owner decisions / notes

- **Modality is the originator class, coarsened.** The spec names "social vs sensor vs official/agency vs
  imagery"; the originator class already carries exactly this axis, so `Modality.of` maps the class rather than
  inventing a parallel taxonomy, with `imagery` added to the class set. A source's modality is therefore only
  as good as its originator mapping in `intel/originators.json`; an unmapped aggregator is `other` and does not
  help a cross-modality rule.
- **Auto-corroboration is a heuristic gatherer, not the arbiter.** It proposes nearby independent records as
  supporting members; whether they corroborate or contradict is still decided by the unchanged comparison core.
  It needs geometry on the event's members (it searches around their centroid); a member with no geometry
  contributes nothing to the search. It attaches at most `maxMembers` (default 10), one per independent root.
- **The end-to-end attach-and-upgrade path is DB-backed** (`record_version`, PostGIS `ST_DWithin`,
  `EvidenceService`), so its test is an integration test exercised by the intel suite, which the owner runs; the
  unit tests here cover the deterministic decision points (thresholds, modality counting, activation, policy
  validation) that do not need a database.
- **Recipe-level gates stay per-recipe.** `minSignalFamilies` and `activateAt` were already recipe config
  (defaults 1 and 2); the new policy covers the cross-cutting event-quality, situation-activation and
  auto-corroboration gates and the modality requirement. A global default for the recipe parse defaults was not
  added, to avoid threading settings through `RecipeDefinition.of`'s many call sites; flagged rather than built.
- **`checkApiCompat` (owner run):** two additive operations and one additive schema, plus an optional
  `activateAt.modalities` property — confirm oasdiff treats it as non-breaking when the gate runs.
- No tests, runtime, migration or soak verification performed; deferred to the owner at code-complete.
