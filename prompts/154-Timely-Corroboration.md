# 154 — Timely corroboration (auto-corroborate, modality-aware, tunable)

**Goal.** Make a lone claim **surface immediately and upgrade as evidence arrives** (timely AND
accurate), count independence by **sensing modality** as well as originator, and make the "promote to
trusted" gate **tunable config** instead of hardcoded.

**Read first.**
- `src/main/java/ai/foresignal/intel/CorrelationEngine.java` — clustering (~L427), `write`/rescore
  (~L838-864), `dryRun`.
- `src/main/java/ai/foresignal/intel/IntelEventService.java` — `addMember` (~L388-436): recipe events
  auto-gather members, but agent/analyst/social claims only get **manually pinned** members.
- `src/main/java/ai/foresignal/evidence/EvidenceService.java` — `groupKeys` / independent groups by
  **originator root** (~L105-155).
- `EventScorer` `quality` (strong = ≥3 families from ≥2 independent groups); `RecipeDefinition.ActivateAt`;
  `SituationLifecycle` (~L54-79, ≥2 families & ≥2 groups hardcoded); `SignalFamilies`.

**Build.**
- **Auto-corroboration for lone claims:** when a new single-source claim/observation (social claim,
  agent/analyst event) arrives, automatically search independent records/canonical events (reuse
  `CorrelationEngine` clustering + `EvidenceService.groupKeys`) and attach any corroborating members,
  so it surfaces at once as `emerging`/`uncorroborated` and **auto-upgrades** to corroborated/active as
  independent groups accumulate — instead of waiting for someone to pin members.
- **Modality-aware independence:** add a **sensing-modality** dimension (distinct from signal family
  and originator root — e.g. social vs sensor vs official/agency vs imagery) and an optional activation
  rule requiring ≥N distinct modalities. Extend `SignalFamilies` / `RecipeDefinition.ActivateAt` /
  `EventScorer`. The owner's point: "ideally different modalities."
- **Tunable thresholds:** lift the hardcoded corroboration gates (event `minSignalFamilies` /
  `activateAt`; situation ≥2 families & ≥2 groups) into recipe- and settings-driven config so the
  "promote to trusted signal" policy is adjustable.

**API / docs.** No version bump (1.3.0 wave). Add the settings/recipe knobs and any modality fields to
`docs/OpenAPI.json` under 1.3.0; regenerate Postman.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (a lone claim
auto-attaches a corroborating independent member and upgrades; a cross-modality activation rule holds;
thresholds read from config) but do not run them. US English.

**Done when.** A single claim corroborates and upgrades automatically; independence can require
distinct modalities; corroboration thresholds are config, not constants; spec + Postman updated under
1.3.0; compile passes.

**Commit.** `154: Timely corroboration` + `completion-reports/154-CR-*.md`. **Stage only this prompt's
files.**
