# 157-CR-Authorable-Recipes-Shared-Attribute-Joins

| | |
|---|---|
| Prompt | 157-Authorable-Recipes-Shared-Attribute-Joins.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `8f20e2845c7bc08b67936973079bf681c0a96bc1` |
| Date | 2026-10-22 |

## What was added

Three things: operators can author correlation recipes through the API, a shared-attribute join predicate
exists, and the reliability/skew thresholds are settings.

### A — author-able recipes

- `RecipeRegistry` gains `validate(id, definition)` (parse + `RecipeDefinitionValidator` against the installed
  kinds), `author(id, definition, enabled)` (persist a full operator definition, replacing any stored one of
  the same id, dropping stale param overrides, keeping run history), `isBundled(id)` and `exists(id)`.
- `IntelController.createRecipe` (POST `/v1/intel/recipes`, 201) authors a new operator recipe: the id is a
  slug not already in use and not a bundled recipe, and the definition is validated (invalid → 400).
  `updateRecipe` now also accepts a full `definition` to replace for an operator recipe; a bundled recipe's
  definition stays classpath-managed (replacing it is 400), while its `enabled` and `params` are still tunable.
  The recipe view gains `origin` (`bundled` / `operator`). `testRecipe` (dry-run) is unchanged.
- `AccessPolicy`: POST `/v1/intel/recipes` is `admin`, like `updateRecipe`.
- Bundled recipes are untouched: the startup `sync()` only processes `RecipeRegistry.BUNDLED` ids, so an
  operator recipe (any other id) is never overwritten, and `origin` is derived from that set — no schema
  migration was needed.

### B — shared-attribute join

- `RecipeDefinition.Join` gains `sharedAttribute` (a payload dot-path), parsed from the join JSON.
- `CorrelationEngine` joins a candidate whose value at that path equals a seed member's
  (`sharedAttributeMatch` / `sharedAttributeValues`, read with `JsonValues.at`). A shared-attribute join
  matches on the attribute alone — geometry is not required and the region/distance gates are skipped — though
  the clause's time window still applies. This is the join a fraud/relationship recipe needs (an address,
  phone, officer or shared id); its data arrives with 158 and the deferred sourcing.

### C — thresholds to settings

- `NewsReliability.numbers()` reads `media.reliabilityNumbers` merged over the Admiralty `NUMERIC` defaults
  (an entry outside `[0, 1]` or for a grade other than A–E is ignored); `reliability(...)` now uses it via the
  new instance `numberOf(grade)`. The static `NUMERIC` map and `numeric(grade)` remain.
- `CoverageAnalyzer` gains a `Thresholds(skewShare, skewMinGroups, blindspotShare, blindspotMinRated)` record
  (with `DEFAULT` = the historical constants) and an `analyze(..., Thresholds)` overload; the 5-arg `analyze`
  delegates to the defaults, so no behaviour changed by default. The skew/blindspot caveat texts now state the
  configured percentage rather than a hardcoded 75%.
- `NewsCoverageService` reads `news.coverageThresholds` into a `Thresholds` (`thresholds()`) and passes it to
  both `analyze` call sites (stories/topics/situations and hypotheses). Both new settings mirror the existing
  `media.reliabilityMap` pattern and are registered in `SettingKeys`.

### D — spec and Postman

- `docs/OpenAPI.json` (1.3.0, no version bump): the `createRecipe` operation and `CorrelationRecipeCreate`
  schema; `definition` on `CorrelationRecipeUpdate`; `origin` on `CorrelationRecipe`; `sharedAttribute` on the
  recipe join; a 1.3.0 changelog entry (the two new settings are described there, as settings keys are not
  individually schematized). Postman regenerated (`scripts/generate-postman.py`): 856 requests, `createRecipe`
  added.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `SharedAttributeJoinTest` — a candidate sharing the attribute value matches; a different or absent value
    does not; a nested dot-path is read; the predicate round-trips through `RecipeDefinition`.
  - `RecipeAuthoringTest` — a valid operator recipe validates, persists and round-trips as a parsed definition
    (with its shared-attribute join); a recipe reading an uninstalled kind is invalid; a bundled id is
    recognized.
  - `CoverageThresholdsTest` — a lowered skew threshold flips a 0.6-share coverage to skewed; `NewsReliability`
    reads its grade numbers from the setting over the defaults, ignoring out-of-range and unknown-grade entries.
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified manually
  over the changed non-excluded files against `gradle/us-english/british-words.txt` — none (the pre-existing
  `ais-behaviour` signal-family id in `docs/OpenAPI.json` is a persisted wire value and was not touched).
  `docs/OpenAPI.json` validated as well-formed JSON.

## Owner decisions / notes

- **Operator recipes need no new column.** An operator recipe is simply a stored recipe whose id is not in
  `RecipeRegistry.BUNDLED`; the startup sync only touches bundled ids, so operator recipes are safe, and
  `origin` is computed from that set. A migration was deliberately avoided.
- **A bundled recipe's definition is not replaceable via the API.** Its definition is the classpath's; the API
  still toggles `enabled` and overrides `params` (and those survive version syncs, as before). Full-definition
  authoring is for operator recipes only — a `PUT …/definition` on a bundled id is a 400 with that reason.
- **The shared-attribute join is deliberately non-geographic.** It matches on an equal attribute value across
  the cluster and does not require geometry or a distance/region gate, because fraud/relationship links are not
  spatial; the time window still applies and is the recipe author's to widen. The ownership-edge data it will
  join on arrives with 158 and the deferred sourcing, so no recipe ships using it yet.
- **Thresholds default to the historical constants.** `CoverageAnalyzer.analyze`'s old 5-arg signature and
  `NewsReliability`'s static `NUMERIC`/`numeric` are retained, so nothing changes until an operator sets the new
  settings; the caveat text now reflects the configured share.
- **`checkApiCompat` (owner run):** one additive operation (`createRecipe`), one new schema
  (`CorrelationRecipeCreate`), and additive properties (`origin`, join `sharedAttribute`, update `definition`)
  — confirm oasdiff treats them as non-breaking.
- No tests, runtime or soak verification performed; deferred to the owner at code-complete.
