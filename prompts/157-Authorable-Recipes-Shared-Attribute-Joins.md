# 157 — Author-able correlation recipes + shared-attribute joins

**Goal.** Let an operator **author correlation recipes via the API** (today they are classpath-only —
`updateRecipe` merely toggles `enabled` + overrides params), add a **shared-attribute join** predicate
(the foundation for fraud/relationship recipes, pairing with 158's ownership edges), and lift a few
**hardcoded reliability/skew thresholds into settings**.

**Read first.**
- `src/main/java/ai/foresignal/intel/RecipeRegistry.java` (~L20-21, bundled classpath JSON synced at
  startup — **no create endpoint**).
- `api/IntelController.java` `updateRecipe` (~L175, enable + param overrides only), `testRecipe` (~L195,
  dry-run), `listIntelRecipes`.
- `intel/RecipeDefinition.java` `Join` predicates (~L38: `withinKm`, `sameRegion`, `sameEntity`,
  before/after) and `RecipeDefinitionValidator`; `CorrelationEngine` join handling; recipe JSON under
  `src/main/resources/intel/recipes/`.
- Thresholds: `news/NewsReliability.NUMERIC` (hardcoded Admiralty map; a `media.reliabilityMap` setting
  already exists — follow that pattern) and `news/CoverageAnalyzer.SKEW_SHARE` (hardcoded).

**Build.**
- **Author-able recipes:** add a **create-recipe** endpoint and extend `updateRecipe` to replace a full
  operator-authored `RecipeDefinition` (not just enable+params), persisted alongside the classpath
  recipes and validated by `RecipeDefinitionValidator`; keep `testRecipe` dry-run. Classpath recipes
  remain; operator recipes live beside them.
- **Shared-attribute join:** add a `Join` predicate matching records/entities that **share an
  attribute** (address / phone / officer / shared id) to `RecipeDefinition.Join` + `CorrelationEngine`
  — the join a fraud/relationship recipe needs (its data arrives with 158 + the deferred sourcing).
- **Thresholds → settings:** make the Admiralty `NUMERIC` map and `CoverageAnalyzer` skew thresholds
  settings-driven, mirroring the existing `media.reliabilityMap` pattern, so the admin app can tune
  them.

**API / docs.** No version bump (1.3.0 wave). Add the create/replace-recipe endpoint, the recipe schema
with the shared-attribute join, and the new settings to `docs/OpenAPI.json` under 1.3.0; regenerate
Postman. Keep `checkApiCompat` additive.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (an operator recipe
round-trips and dry-runs; a shared-attribute join matches records sharing an attribute; thresholds read
from settings) but do not run them. US English.

**Done when.** Operators can author/validate/test correlation recipes via the API; a shared-attribute
join exists; reliability/skew thresholds are settings; spec + Postman updated under 1.3.0; compile
passes.

**Commit.** `157: Author-able recipes + shared-attribute joins` + `completion-reports/157-CR-*.md`.
**Stage only this prompt's files.**
