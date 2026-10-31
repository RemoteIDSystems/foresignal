# SG-000 — Sources & Groups backend run sequence (API 2.2.0)

> **Index only — not an executable prompt.** The run skips this file, as it skips `000`. It drives the
> **Self-Service Sources & Source Groups** campaign (`SG-001`…`SG-004`), a feature release at API **2.2.0**
> that extends the self-service surface the newspaper campaign (`NP-001`…`NP-008`) and the v2.0 identity
> model (`R2-*`) established. It is separate from, and builds on, those finished campaigns.

## What this campaign delivers

A signed-in user owns the **inputs** to their newspaper, not just its presentation:

1. **Custom sources** (`SG-001`) — a user defines their own feeds (air-quality, earthquake, any JSON/webhook
   stream) through `/v1/me/custom-sources`, a self-service layer over the existing operator custom-source
   machinery (prompts 024 / 059). Owned by the caller, with a rotatable ingest key, a testable field mapping,
   and captured samples. Folds into the pipeline like any catalog source.
2. **Source groups** (`SG-002`) — a user groups sources into named sets through `/v1/me/source-groups`, each
   with its own correlation and notification threshold, and uses a group as its own edition scope
   (`BriefingScope.sourceGroupIds`) and as a trigger scope (`TriggerRuleInput.sourceGroupId`).
3. **Per-source severity thresholds** (`SG-003`) — a user sets the minimum alert severity that notifies,
   per source, through `/v1/me/source-thresholds`.
4. **Activation** (`SG-004`) — the mini-cutover: flip all fifteen 2.2.0 operations `planned` → `implemented`,
   move the release to 2.2.0 in the three guard spots, regenerate the SDKs and Postman.

## The contract is already authored

`docs/OpenAPI.json` is **already at `info.version` 2.2.0** with the 2.2.0 changelog entry, the fifteen new
operations (all `x-foresignal-status: planned`, tagged `x-foresignal-prompt: SG-001`…`SG-003`), and the seven
new schemas plus `BriefingScope.sourceGroupIds` and `TriggerRuleInput.sourceGroupId`. **This campaign does not
design the contract — it implements it.** The contract is the only source of truth; a gap is a completion-report
question, never an invented endpoint, field or enum.

## The prompts

| # | Title | What it does |
|---|---|---|
| SG-001 | Custom sources (self-service) | `/v1/me/custom-sources` CRUD + ingest-key rotate + mapping test + samples, owned by the caller, `self.sources` |
| SG-002 | Source groups | `/v1/me/source-groups` CRUD + wire `sourceGroupIds` into the edition scope and `sourceGroupId` into the trigger evaluator, `self.sources` |
| SG-003 | Per-source thresholds | `/v1/me/source-thresholds` get/replace, per-source minimum notify severity/mute/cooldown, `self.alerts` |
| SG-004 | Activate 2.2.0 | Flip all SG ops `planned`→`implemented`; release 2.2.0 (`ApiVersionTest.RELEASE`, Gradle `version`); regenerate SDKs + Postman |

`SG-001`…`SG-003` implement against the `planned` contract and compile; `SG-004` is the cutover where the
version-agreement guard goes green (as `NP-007` / `R2-027` were for their releases).

## How to run it (owner's standing rule — read this)

- **Run straight through. Never pause.** Execute `SG-001` → `SG-004` in order, start to finish, without waiting
  for a review gate between prompts. The **only** reason to stop is a genuinely insurmountable problem (the disk
  is full, the machine cannot compile at all, a required tool is missing and cannot be installed). Any other
  issue — a confusing requirement, a contract gap, a question, a decision you would normally raise — is
  **written to the prompt's completion report and you carry on.** (This overrides any per-prompt "review gate /
  pause" wording inherited from earlier campaigns.)
- **Begin with the lowest `SG-*` prompt that has no `completion-reports/SG-*-CR-*.md`.** The owner never runs a
  command.

## Standing rules (same as the NP / R2 campaigns)

- **OpenAPI is the only contract;** every operation cites its `operationId`; gaps are completion-report
  questions, never invented. Operations stay `planned` until `SG-004` activates them.
- **Compile-only gate, every prompt:** `./gradlew compileJava compileTestJava` and `./gradlew checkUsEnglish`.
  **No** full test suite, `lintOpenApi`, `checkApiCompat` or SDK generation until `SG-004`. Tests are **written,
  not run**.
- **Additive migrations** (next free `V{n}`, Flyway owns the schema; Hibernate stays `validate`). US English
  everywhere except the five documented exceptions (license files/holders, a dependency's own API, direct
  quotations, other-language translations, and anything that crosses a wire or persists — JSON keys, preference
  keys, protocol fields). Version agreement held at 2.1.1 until `SG-004`, then 2.2.0.
- **Per prompt:** commit `SG-00N: <Title>`; write `completion-reports/SG-00N-CR-<Name>.md` with the
  40-character commit hash and any owner questions; **stage only that prompt's files**; move to the next.
- **Do not** add or modify `.gitlab-ci.yml`, `CLAUDE.md` or `CONVENTIONS.md`, and do not touch the frozen
  `docs/api/v1.0.0/OpenAPI.json`. Never run a history-destroying git operation.

## Downstream (not part of this campaign)

Once this campaign merges (spec active at 2.2.0), the **ForeSignal News** app (separate repo `foresignal-news`,
its own `prompts/000-Run-Sequence.md`) and any admin/client surface that wants these controls vendor the 2.2.0
spec and build on it. No `foresignal-admin` work is required for this release — the SG operations are all
`self.*` user-facing; the admin console regenerates its client from the spec at its next build if it wants them.
