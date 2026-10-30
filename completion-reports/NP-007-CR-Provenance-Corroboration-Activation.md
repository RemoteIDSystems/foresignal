# NP-007-CR-Provenance-Corroboration-Activation

| | |
|---|---|
| Prompt | NP-007-Provenance-Corroboration-And-Activation.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; `generateSdks` run; no test was run) |
| Implementation commit | `90ee78974bbed82ee9b7458e6b94c94b61424608` |
| Date | 2026-10-30 |

**The Automated Newspaper is active at API 2.1.0.** Every operation of the feature is `implemented`. The SDKs and
Postman are regenerated from the activated contract.

## What was added

- **Migration `V32__edition_provenance.sql` (additive):** `briefing.include_provenance` and
  `briefing.include_corroboration`, both `boolean not null default false`. No existing briefing's citations change.
- **The flags:** `BriefingInput.includeProvenance` and `includeCorroboration` (NP-001) are accepted. They are
  booleans, a non-boolean is a 400, and they default to false. They are stored and returned on `Briefing`.
  `BriefingInput.Edition` gains both, and its four-argument constructor is kept.
- **`briefing/EditionProvenance`** annotates each run's citations in `BriefingRunner.render`, so runs and previews
  alike. Record citations and alert citations (the alert's record) are annotated; briefing and weather citations are
  left as they are. A failure on one item leaves that item unannotated and never fails the run.
  - **`provenance`** (when `includeProvenance`):
    - `refs`: the item's `record:` ref (pinned to its seq), the `canonical-event:` refs its 114 observation links
      point to (at most 5), and `alert:<id>` for an alert;
    - `reason`: a one-line "why included", for example "In scope: earthquake from usgs, linked to 1 event" or
      "Alert of rule 'Big quakes' on earthquake from usgs".
  - **`corroboration`** (when `includeCorroboration`):
    - `independentSources`: the higher of the record's 099 corroboration cluster and the independent evidence groups
      of its canonical events, and at least 1;
    - `credibility` and `reliability`: the Admiralty grade from `CredibilityService` (109). A new
      `assess(sourceId, externalId, kind, qaFlags, lookup)` overload applies the same rule without a `RecordRow`, and
      `clusterOf` was added;
    - `evidenceQuality`, under the 154 policy (`IntelCorroborationService.quality()`):
      - `strong`: at least `strongGroups` (minimum 2) independent sources and credibility 1–2;
      - `moderate`: at least 2 sources, or credibility 3;
      - `weak`: a single uncorroborated source;
      - `insufficient`: the record is gone.
- **HTML** (`BriefingHtmlRenderer.page(…, citations)`): when any citation is annotated, a "Sources" aside lists each
  annotated item. It shows a level badge (`strong`/`moderate`/`weak`/`insufficient`), an "N sources" badge, the
  grade badge (for example `A2`) and "Why included: …". Without annotations the page is byte-for-byte the previous
  page. The edition email (NP-004) and the hosted site (NP-005) both pass the run's citations. The styles are inline,
  so the CSP is unchanged.
- **Audio:** with `includeCorroboration`, when at least one cited item is `weak` or `insufficient`, the spoken text
  ends with one sentence. For example: "A note on sourcing: 2 of the 3 items in this edition rest on a single,
  uncorroborated source." Nothing is added when every item is corroborated.
- **Activation** (`docs/OpenAPI.json`):
  - `x-foresignal-status` `planned` → `implemented` for `getTranslationSettings`, `updateTranslationSettings`,
    `getMyNewsSubscriptions`, `updateMyNewsSubscriptions`, `listMyEditions`, `getEditionSite` (NP-001) and
    `openEditionSiteShareLink` (NP-005). The spec now has 907 `implemented` operations and none `planned`.
  - **2.1.0 changelog amended:**
    - It now says the feature is activated, not "authored before implementation".
    - It records the additions made while implementing: `BriefingRun.deliveryResults` (NP-004),
      `openEditionSiteShareLink` and the `edition-site` share-link type (NP-005), and `BriefingRun.breaking` (NP-006).
- **Version agreement at 2.1.0:**
  - `info.version`, `build.gradle.kts` and `ApiVersionTest.RELEASE` were already 2.1.0.
  - The SDK manifests moved from 2.0.0 to 2.1.0, the same set R2-027 moved: `pubspec.yaml` ×2, `package.json`, the
    root of `package-lock.json`, `pyproject.toml`, `foresignal_sdk.__version__` and the five SDK READMEs.
  - The frozen v1.0.0 spec is untouched.
- **Regenerated:**
  - `./gradlew generateSdks` (R2-027's method: `sdkGenerateDart`, `sdkBuildRunnerDart`, `sdkGeneratePython`,
    `sdkGenerateTypescript`) → `BUILD SUCCESSFUL in 2m 6s`. The Dart, Python and TypeScript clients now carry every
    2.1.0 addition: 127 SDK files, 78 of them new.
  - `generate-postman.py` → "943 requests across 90 tag folders; quickstart 12 steps; covers all ops: True". There is
    no diff; Postman was regenerated in each earlier prompt.
  - `generate-access-permissions.py` → "907 rows (555 with a permission)". No diff.
- **Tests (written, not run):**
  - `EditionProvenanceTest`:
    - with the flags on, refs, reason and badge are set for records and alerts, `insufficient` for a removed record,
      and briefing citations are left alone;
    - with the flags off, nothing is added, and each flag adds only its own member;
    - the policy thresholds;
    - the HTML badges, aside and why-line, and an identical page without annotations;
    - the spoken note;
    - the flags round-trip through the entity.
  - `ApiVersionTest.newspaperOperationsAreActive` is the extended guard. No operation with an `NP-` prompt is
    `planned`, at least 7 exist, and the 2.1.0 changelog names each of them.

## Decisions and disagreements for the owner

1. **`SpecAnnotationsTest` and the `NP-` prompt ids.** `OpenApiSpec.promptNumber()` accepts only `\d{3}`, so
   `everyOperationIsAnnotated` flags all 7 `NP-` operations. It already flags the 72 `R2-` operations (counted in
   this session: `R2-` 72, `NP-` 7). This was not changed here, because it predates the campaign and affects R2 as
   well. Should the guard accept `R2-\d{3}` and `NP-\d{3}`?
2. **Corroboration is per item, not per intelligence event.** `getIntelCorroboration`'s event rule counts signal
   families, and an edition item is one record (one family). The label therefore uses the policy's `strongGroups`
   together with the Admiralty credibility, as described above. The calibration curve (151) is not applied: it
   calibrates intel-event and precursor scores, and an edition item has no such score.
3. **Provenance lists canonical events, not intelligence events or situations.** Those are one more hop away
   (event → intel event). They can be added to `refs` if wanted.
4. **News stories in the "Top stories" section are not citations**, so they get no badge. Their outlet count is
   already shown.
5. **The Postman collection had no diff**, because each earlier prompt regenerated it as its spec changed. The SDKs
   had not been regenerated since R2-027, so this commit carries every 2.1.0 SDK change.
6. **The SDK versions moved to 2.1.0** with the API, as R2-027 did for 2.0.0.

## What runs

- `./gradlew compileJava compileTestJava checkUsEnglish` → exit 0.
- `./gradlew generateSdks` → `BUILD SUCCESSFUL`. Postman and `AccessPermissions` generators: see above.
- No test, `build`, `check`, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next work

- The backend newspaper campaign (NP-001…NP-007) is complete at API 2.1.0. The admin console campaign (admin
  `NP-001+`) and the client edition view (client `NP-001`, which self-guards until the backend is 2.1.0) can run
  next.
- The next free migration is `V33`.
