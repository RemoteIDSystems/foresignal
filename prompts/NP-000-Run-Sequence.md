# NP-000 — Automated Newspaper backend run sequence (API 2.1.0)

> **Index only — not an executable prompt.** The run skips this file, as it skips `000`. It drives the
> **Automated Newspaper** campaign (`NP-001`…`NP-007`), a feature release at API **2.1.0** that extends
> the existing briefing engine. It is separate from the finished R2 (RBAC, API 2.0.0) campaign.

Run it the same way as `000-Run-Sequence.md` / the R2 campaign: **one** Claude Code session at the
repository root, prompt after prompt in order, pausing at a review gate after each so the owner can
look and reply "continue". The owner never runs a command. `docs/OpenAPI.json` is the **only**
contract.

## To start this campaign

The R2 campaign is finished (`completion-reports/RUN-STATUS.md` says "Finished"). Clear/replace that
marker so the run proceeds, then hand this session `NP-000`; it runs `NP-001` first (the lowest
`NP-*` prompt with no `completion-reports/NP-*-CR-*.md`).

## The prompts

| # | Title | What it does |
|---|---|---|
| NP-001 | Newspaper contract | Author the 2.1.0 OpenAPI surface (ops `planned`), bump the version in all three guard spots, add AccessPolicy entries |
| NP-002 | News translation at ingest | Translate foreign news into target language(s) via the LLM, beside the original, budget-governed |
| NP-003 | Per-user editions | Editions = user-owned briefings, scoped to the user's data, written & spoken in their language |
| NP-004 | Edition output targets | Deliver to email / push / alert channels in the right per-format shape |
| NP-005 | Hosted + shareable edition site | Stable always-current HTML edition URL + revocable read-only share link |
| NP-006 | Breaking-news special edition | A matching alert fires an off-schedule edition (cooldown-guarded) |
| NP-007 | Provenance & corroboration, then activation | Per-item provenance + corroboration badges; flip ops `planned`→active, regenerate SDKs/Postman |

`NP-001` is spec-first; everything compiles against it. `NP-007` activates the feature and is the
mini-cutover (as R2-027 was for the RBAC release).

## Standing rules (same as the R2 campaign)

- **OpenAPI is the only contract;** every op cites an `operationId`; gaps are owner questions, never
  invented. Ops authored `planned` with `x-foresignal-prompt: NP-00N`; `NP-007` activates.
- **Compile-only gate, every prompt:** `./gradlew compileJava compileTestJava`, and run
  `./gradlew checkUsEnglish`. **No** test suite, `lintOpenApi`, `checkApiCompat` or SDK generation
  until `NP-007`. Tests are written, not run.
- **Additive migrations** (next free `V{n}`), US English (wire keys and translated content excepted),
  version agreement held at 2.1.0.
- **Review gate after each prompt;** commit `NP-00N: <Title>`, write
  `completion-reports/NP-00N-CR-<Name>.md` (40-char hash, owner questions), pause.

## Downstream (not part of this campaign)

Once this campaign merges (spec at 2.1.0), the **admin** console (`foresignal-admin` `NP-001`…`NP-004`)
and the **client** edition view (`foresignal-client` `NP-001`…`NP-002`) vendor the 2.1.0 spec and build
on it. Each has its own `NP-000-Run-Sequence.md`.
