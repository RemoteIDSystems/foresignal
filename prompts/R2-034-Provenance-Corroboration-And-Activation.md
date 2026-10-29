# R2-034 — Edition provenance & corroboration, then activation

**Context.** Final prompt of the automated-newspaper feature, after **R2-033**. Evidence cross-links
already exist (`evidence/FactsExtractor`, `EvidenceResolver`, `news/NewsStoryObjectResolver`); so do
intel corroboration and calibration (`getIntelCorroboration`, the calibration signal). `R2-028`
authored `includeProvenance`/`includeCorroboration` on the edition and per-item `provenance`/
`corroboration` fields. This prompt also **activates** the whole set, as R2-027 did at cutover.

**Goal.** Each edition item carries **why it made the paper** (its cross-linked evidence/objects) and
a **corroboration/calibration badge** (how well-supported it is) — the provenance + corroboration
extra. Then flip the feature's ops to active and regenerate the SDKs + Postman.

## Operations

No new ops. Activate the six R2-028 ops (`planned` → active).

## Read first

- `briefing/BriefingSectionContributor.java`, `BriefingSection`, `BriefingRunner`,
  `BriefingHtmlRenderer`, `BriefingAudioService` (where section content is assembled and rendered).
- `evidence/` (how an item resolves to its supporting evidence/objects) and the intel corroboration
  + calibration signal (`getIntelCorroboration`, the calibration schema).
- How R2-027 performed the cutover (status flip + SDK/Postman regeneration).

## Build

1. When `includeProvenance`, attach to each edition item the cross-linked evidence/object refs that
   put it in scope (from the evidence resolvers) — the "why this is in my paper" trail. When
   `includeCorroboration`, attach the item's corroboration/calibration signal.
2. `BriefingHtmlRenderer` renders these as compact **badges/asides** (source count, corroboration
   level, a "why included" line); `BriefingAudioService`'s script mentions corroboration briefly
   where it matters. Both are omitted cleanly when the flags are off.
3. **Activate.** Flip `x-foresignal-status` from `planned` to active for the R2-028..R2-034 ops in
   `docs/OpenAPI.json`, then regenerate the SDKs and Postman (`python3 scripts/generate-postman.py`
   and the SDK generation R2-027 used). Keep the `2.1.0` version agreement (the guard) green.

## Tests to write (do not run)

- `EditionProvenanceTest` — with the flags on, items carry evidence refs + a corroboration badge;
  with them off, neither appears; the HTML render shows the badges.
- Extend the contract/version guard so the activated 2.1.0 ops are consistent (no `planned` left for
  this feature; version agrees).

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English. (SDK/Postman regeneration is generation, not the test suite.)

## Done when

Edition items show provenance + corroboration badges when enabled; the R2-028 ops are active; SDKs +
Postman are regenerated; the 2.1.0 version agreement holds; compile and `checkUsEnglish` green. This
completes the automated-newspaper backend; the admin console (admin `R2-012+`) and a client edition
view follow.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; frozen v1.0.0 spec; `.gitlab-ci.yml`; other prompts' files. **Stage
only this prompt's files** (plus the regenerated SDK/Postman artifacts this prompt owns).

## Commit

`R2-034: Edition provenance & corroboration, then activation`, then
`completion-reports/R2-034-CR-Provenance-Corroboration-Activation.md` with the 40-character hash and
a note that the newspaper feature is active at API 2.1.0.
