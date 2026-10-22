# 153-CR-Dehype-And-Partisan-Vetting

| | |
|---|---|
| Prompt | 153-Dehype-And-Partisan-Vetting.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `813d6b5ce11c6f0f1ed7a1396010939fea08c7be` |
| Date | 2026-10-22 |

## What was added

The owner's "strip the BS" requirement: surface the after-analysis, not the raw post. A social claim now
carries three new, deterministic signals, and partisan source bias finally lowers how much a claim is
trusted — not only for news. The LLM-free fact-grounding core (`SocialPhysicalComparator.decide`,
`ComparisonService`) is **unchanged**; this feeds it.

### A — the neutral, de-hyped restatement

`ClaimRestatement` (new) produces the "I don't need the selfie, tell me the checkable core" field: one short
who/what/where/when sentence stripped of outrage and partisan framing. The LLM extractor may *propose* one
(the template now emits it), but it is never the arbiter — the proposal is re-validated exactly like the
extracted tuples:

- rejected if it carries any `FramingScore` inflammatory/partisan word;
- rejected if it names a capitalized proper noun the gazetteer and entity index do not resolve and that is
  not the validated actor (an invented specific);
- rejected if it is empty or longer than 240 chars;
- otherwise kept verbatim.

When the proposal fails, a deterministic restatement is **synthesized** from the already-validated who/what/
where/when (`actor: type in place (when) — denied/questioned`). So the stored restatement is always checkable
prose, never hype and never an unvalidated specific. Surfaced in `NarrativeViews.claim` as `restatement`.

### B — the deterministic framing / partisan score

`FramingScore` (new) is a pure, LLM-free scorer parallel to `SocialEvidenceGrades`: it measures how much hype
the extraction stripped off. Markers are inflammatory/outrage words, partisan-loaded labels, intensifiers,
ALL-CAPS shouting and exclamation; the score in `[0, 1]` is the lightly-saturated marker density, banded
`neutral` (< 0.12) / `charged` (< 0.28) / `inflammatory` (≥ 0.28). It measures *how a claim is told*, never
whether it is true. Scored over the most central post of the claim and surfaced as `framingScore` +
`framingLabel`.

### C — outlet reputation into social-claim reliability (the partisan-bias lever)

`SocialEvidenceGrades.withReputation` (new) and `ClaimEnrichment` (new) let a claim's source, when it resolves
to a rated outlet, lower its Admiralty source-reliability the same way `NewsReliability` grades news:

- factuality **caps** the track-record grade (the worse of the two wins);
- a strongly partisan lean (`left`/`right`) **or** a state-affiliated outlet drops it one further step toward
  `F`.

`ClaimEnrichment.reputation(sourceKey)` resolves a **domain-like source key** (e.g. `channel:telegram:
example.com`) via `OutletRegistry.byDomain`, then reads the consensus factuality (new
`NewsReliability.gradeForOutlet`), `lean` and state-affiliation — the latter two previously carried but unused.
The reputation is surfaced as the claim's `reputation` member (new `ClaimReputation` schema). An ordinary
social account with no resolvable domain (the common case) maps to no outlet and leaves the grade unchanged;
the lever applies wherever a source does resolve to a rated outlet.

### D — model, store, extraction paths

- `NarrativeStore.Claim` gains `restatement`, `framingScore`, `framingLabel`, `reputation`; `CLAIM_COLUMNS`,
  `upsertClaim` and the row mapper updated. Migration `V4__social_claim_dehype.sql` adds the four nullable
  columns (null for pre-153 claims).
- Both extraction paths compute the three signals: `LlmClaimExtraction.store` (LLM proposal re-validated) and
  `NarrativeService`'s deterministic claim-build loop (synthesized restatement). Both fold reputation into
  the base track-record reliability before grading.
- Prompt template `claim-extraction-system` is now **1.1.0** (emits the `restatement` field while keeping the
  "reports, not facts / never judge whether a claim is true" constraint). `1.0.0` stays bundled;
  `LlmClaimExtraction` references `PromptTemplates.V1_1` and `VERSION` is `1.1.0`.

### E — spec (1.3.0, no version bump)

- `Claim` gains `restatement`, `framingScore`, `framingLabel` (enum neutral/charged/inflammatory) and
  `reputation`; new `ClaimReputation` schema (outletId, factuality, lean, stateAffiliated). 1.3.0 changelog
  entry. Postman regenerated with `scripts/generate-postman.py` — **output byte-identical** (no new operations
  or request shapes; the additions are response-schema only), so nothing to re-stage there.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `FramingScoreTest` — plain prose is neutral/0, outrage+partisan hype is inflammatory, a lone intensifier
    is charged, the same text always scores identically, empty/null is neutral.
  - `ClaimRestatementTest` — a clean proposal naming only a resolvable place is kept; a proposal carrying hype
    or an unresolvable proper noun or over length is rejected and synthesized instead; the synthesized form is
    neutral who/what/where/when with a denial suffix.
  - `SocialReputationTest` — worse factuality caps the grade; a strongly partisan lean or state-affiliation
    drops it one step toward F; a high-factuality centrist non-state outlet leaves it unchanged; only a
    domain-like source key resolves; a source with no outlet leaves reliability untouched.
  - Five existing tests updated for the new `Claim`/`LlmClaimExtraction` constructor arity
    (`EventSchemaConformanceTest`, `CorrelationTestSupport`, `SocialPhysicalComparatorTest`,
    `SocialEvidenceItemTest`, `LlmClaimExtractionTest`).
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive; the prompt says no other
  Gradle). US English verified manually against `gradle/us-english/british-words.txt` — no British inflection
  in the diff. (`ais-behaviour` in `EventSchemaConformanceTest` is a pre-existing, persisted signal-family id,
  not touched here.) `docs/OpenAPI.json` validated as well-formed JSON.

## Owner decisions / notes

- **Social sources rarely carry a domain.** The reputation lever is real but, in the current extraction path,
  only fires when a claim's source key is a resolvable web domain (`channel|account:{platform}:{domain}`). An
  ordinary handle maps to no outlet, so partisan/factuality adjustment applies today chiefly to channels keyed
  by domain. Lifting this to map handles → outlets would need a handle→outlet table in `OutletRegistry`;
  flagged rather than built, to stay within the prompt's "consult `OutletRegistry` consensus" scope.
- **No new `getSocialSettings` knobs.** The prompt allowed them ("any new knobs"); none were needed — framing
  thresholds and the reputation rule are deterministic constants, matching how `SocialEvidenceGrades` already
  ships its track-record thresholds (overridable via the existing `social.trackRecord` object, untouched).
- **Optional LLM extraction role skipped.** The prompt marked a dedicated `LlmSettings` extraction role
  optional; extraction still uses the shared `chatProvider`. No behavior needed it.
- **`checkApiCompat` (owner run):** `Claim` gains optional properties and a new schema is added — additive;
  confirm oasdiff treats it as non-breaking when the gate runs.
- **Existing claims:** the four columns are null for pre-153 rows; the signals populate on the next extraction
  / re-score. No back-fill performed (additive migration only).
- No tests, runtime, migration or soak verification performed; deferred to the owner at code-complete.
