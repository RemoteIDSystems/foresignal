# 153 — De-hype & partisan vetting (surface the fact, not the hype)

**Goal.** The owner's "strip the BS" requirement: surface the AI's after-analysis, not the raw post —
add a **neutral, de-hyped restatement** of a claim, a deterministic **framing/partisan score**, and
make **partisan source bias actually count**. The fact-grounding core already exists and is
deterministic; this feeds it, it does not replace it.

**Read first (vision is largely realized; these are the deltas).**
- Extraction: `src/main/java/ai/foresignal/social/narrative/ClaimExtractor.java` (`extract` ~L172,
  confidence ~L207) and `LlmClaimExtraction.java` (`run` ~L165, `parse`/re-validate ~L260, `store`
  ~L356). The LLM is explicitly **not** the arbiter — template
  `src/main/resources/llm/templates/claim-extraction-system@1.0.0.md` ("never judge whether a claim is
  true"). Keep that constraint.
- Claim model/view: `NarrativeStore.java` `record Claim` (~L206, structured, **no free-text/restatement
  field**); `NarrativeViews.claim` (~L117-144).
- Grades: `social/narrative/SocialEvidenceGrades.java` (source reliability A–F from track record,
  info credibility) — the deterministic pattern to parallel.
- Reputation: `news/NewsReliability.java` (~L75-96, outlet **factuality** → Admiralty grade, consumed
  by the scorer); `media/outlet/OutletRegistry` consensus carries factuality **and the currently-unused
  `lean` and state-affiliation**.
- **Do NOT change the fact-grounding core:** `social/correlation/ComparisonService.java` +
  `SocialPhysicalComparator.java` (`decide` ~L110) already mark claims corroborated/contradicted/
  unobserved against real sensor/agency evidence, LLM-free. Feed the new signals into it, don't alter
  it.

**Build.**
- **Neutral restatement:** add a short, de-hyped, human-readable who/what/where/when restatement to
  `NarrativeStore.Claim`, produced by `LlmClaimExtraction` (re-validated against the lexicon/gazetteer/
  entity index exactly like the tuples), keeping the "extractor-not-arbiter" template constraint;
  surface it in `NarrativeViews.claim`. This is the "I don't need the selfie, tell me the checkable
  core" field.
- **Framing/partisan score:** add a **deterministic, LLM-free** pure-function scorer (parallel to
  `SocialEvidenceGrades`) that measures emotional/inflammatory intensity and partisan/loaded framing of
  the post — i.e. how much hype the extraction stripped. Surface it on the claim.
- **Reputation into social scoring:** when a claim's source maps to a rated outlet, have
  `SocialEvidenceGrades` (and the claim store) consult `OutletRegistry` consensus — **factuality AND
  the currently-unused `lean` + state-affiliation** — mirroring `NewsReliability`, so partisan bias and
  factuality shape social-claim reliability/confidence, not only news. (This is the direct "strip
  Left/Right BS" lever: partisan lean finally influences how much a claim is trusted.)
- Optional: a dedicated LLM role for extraction/vetting in `llm/LlmSettings.java` + `LlmSettingsService`
  if extraction should use a different model/budget than the shared `chatProvider`.

**API / docs.** No version bump (1.3.0 wave). Add the new claim fields (restatement, framing/partisan
score, outlet-reputation inputs) and any new `getSocialSettings` knobs to `docs/OpenAPI.json` under
1.3.0; regenerate Postman.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (restatement
re-validates and drops uncheckable prose; framing score is deterministic; a rated-outlet source lowers
reliability for low-factuality / strongly-partisan sources) but do not run them. US English.

**Done when.** A claim carries a neutral restatement + a framing/partisan score, and outlet reputation
(incl. partisan lean) feeds social-claim reliability/confidence; the deterministic comparison core is
unchanged; spec + Postman updated under 1.3.0; compile passes.

**Commit.** `153: De-hype & partisan vetting` + `completion-reports/153-CR-*.md`. **Stage only this
prompt's files.**
