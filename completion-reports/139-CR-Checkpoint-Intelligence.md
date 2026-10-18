# 139-CR-Checkpoint-Intelligence

| | |
|---|---|
| Prompt | 139-Checkpoint-Intelligence.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `94a6da0cd6ac7f8d63cb09d3cb3f266a4a6f4833` |
| Date | 2026-10-18 |

## Summary

**No test result in this report is a measured result.** Under the owner directive of 2026-10-17 (the override banner
at the top of `prompts/139-*.md` and `prompts/000-Run-Sequence.md` step 4), no test was run for this checkpoint.

- **Deferred to the owner at code-complete:**
  - the full suite (`./gradlew test`) for 001–138;
  - the 123 synthetic benchmark and its comparison with the 124 baseline (`SyntheticBaselineTest`, which writes
    `build/reports/benchmark/synthetic-baseline.json`; that file does not exist yet, because nothing has run);
  - the coverage pass (`./gradlew jacocoTestReport`) for the intelligence packages;
  - every checkpoint verification in focus areas 1–16.
- `./gradlew compileJava compileTestJava` succeeds on the committed tree: exit code 0.
- There are no pass/fail counts per run, no benchmark metrics and no coverage figures.

The implementation commit is small: 2 test files, no production change.

| Path | Change |
|---|---|
| `src/test/java/ai/foresignal/social/correlation/SocialDeletionIT.java` | New |
| `src/test/java/ai/foresignal/retention/ConsumerClampIT.java` | Modified |

## What was checked by reading the code

**The named tests.** Every test class the prompt names was searched for with `find src/test -name "<Name>.java"`. All
exist except one:

| Test | Location |
|---|---|
| `DeterminismTest` | `analytics/` and `trigger/window/` |
| `LookAheadTest` | `analytics/` and `trigger/window/` |
| `IntelligenceEgressTest` | root package |
| `DuplicateInflationTest` | `evidence/` |
| `ManifestReplayCheckTest` | `manifest/` |
| `InvariantRegistryTest` | root package |
| `CollaborationEgressTest` | `collab/` |
| `ChronicleRetentionIndependenceIT` | `chronicle/` |
| `ExchangeRoundTripIT` | `exchange/` |
| `NewsIndependenceTest` | `news/` |
| `DisclosureTest`, `CircularReportingTest` | `chronicle/network/` |
| `SocialIndependenceTest`, `SocialAbsenceTest`, `SocialCueLoopTest` | `social/correlation/` |
| `XSpendTest` | `social/` |
| `EdpIngestIT`, `EdpSchemaExportTest` | `inbound/edp/` |
| `EdpEvidenceIndependenceTest`, `EdpForecastBindingTest` | `inbound/edp/binding/` |
| `SocialDeletionIT` | **Missing.** The search printed nothing. Written by this commit (see below). |

**Durable cursors (focus area 6).**
- `grep -rln "implements RetentionConsumer"` lists 16 consumer beans. Among them are `SituationCursorConsumer`,
  `RegionRollupCursorConsumer`, `PrecursorCursorConsumer`, `ImpactCursorConsumer`, `RelationCursorConsumer` and
  `BindingCursorConsumer`. Chronicle cursors join through `ChronicleCursorConsumers` (a `RetentionConsumerProvider`).
- `ConsumerClampIT.intelligenceCursorsRegistered` asserted every one of these except `external.binding.cursor`
  (138). That assertion is now added.
- **`social.cursor` does not exist.** `grep -rn '"social.cursor"\|SOCIAL_CURSOR' src/main/java` printed nothing, and
  nothing under `ai.foresignal.social` subscribes to `ChangeBus`. This matches 135 Deviation 11: social counts happen at
  collection, with no durable cursor. The prompt's clamp list therefore cannot include it. See owner decisions.

## Changes

### Tests written (not run)

| Test | What it covers |
|---|---|
| `social/correlation/SocialDeletionIT` (new) | Deletion sync, 135–137. A post ingested into `bluesky-jetstream` and then deleted is stored as a tombstoned version through `SocialCollector.observeDelete`:<br>- `deleted: true`;<br>- text, links and media removed;<br>- author key and topics kept.<br>The record history keeps both versions. The post leaves `StoredPosts.deletionCandidates` and counts in `tombstonedSince`. A second delete, and a delete of a post never stored, produce nothing. The chronicle snapshot (`ChronicleSocialWriter.snapshot`, with `keepPostText`) of a narrative whose central post was deleted keeps its claim, with its evidence-group count, while `centralPostText` becomes null. 2 tests |

### Tests changed

| Test | Change and reason |
|---|---|
| `retention/ConsumerClampIT` | `intelligenceCursorsRegistered` also asserts `SettingKeys.EXTERNAL_BINDING_CURSOR`. The 138 binding router's cursor was registered by `BindingCursorConsumer` but not asserted by the clamp test. |

### Production changes

None.

## Deviations from the prompt

1. **Tests were not run** (owner directive). None of the acceptance criteria that depend on running anything was
   checked:
   - `./gradlew test` passes;
   - the named tests pass;
   - the 123 benchmark has no `fail`;
   - pass/fail counts, benchmark metrics and per-package coverage are recorded.
2. **No fixes from failures.** The prompt's fixes come from running the suite. Without a run, the only changes are
   the two that reading the code showed: the missing `SocialDeletionIT` and the unasserted binding cursor.
3. **Focus areas not statically verifiable** were left for the owner's run. These include golden values (1),
   determinism (2), look-ahead (3), wording across ICD bands (4), egress (5), ledger integrity (7), the API surface
   and `pending-operations.txt` (8), duplicate inflation and absence (9), manifest replay (10), the benchmark (11), and
   object and workspace sync (12).
4. **`docs/Invariants.md` was not changed.** No invariant row covers social deletion sync, so `SocialDeletionIT` is
   not named there. Documents are replaced whole, and adding a row is the owner's call.

## Issues found / open questions

**Owner decisions raised by this checkpoint:**
- (a) **`social.cursor`.** The prompt's focus area 6 expects a durable `social.cursor` in the retention clamp. None
  exists (135 Deviation 11). Should social collection gain a durable cursor, or should the prompt's list drop it?
- (b) Whether `SocialDeletionIT` should be named by a new invariant row in `docs/Invariants.md`.

**Owner decisions carried forward from 124–138** (each from that report's "Issues found"):

| From | Decision |
|---|---|
| 124 | Persisted tag `origin:analyst` → `origin-analyst`; benchmark threshold and scenario changes; new captured fixtures; the Agent Guide appended to rather than replaced |
| 125 | Watch-rule tag names (`indicator`, `origin-hypothesis`, `ind-{id}`); router change for `origin-hypothesis` alerts; exclusive-set update space; disabling rules after observation; band-change interpretation |
| 126 | `count-threshold` semantics; question volume (six per region and family a week); built-in components and `finding:` targets; per-index `autoApplyFittedWeights`; schedules |
| 127 | Forecast-threshold hazard classes; built-in pairs and families (Cloudflare Radar left out); no gauge probability under 10 samples; `spc-outlooks` (14 d) and WPC ERO (90 d) retention too short for n = 20; question volume per advisory; impact-family rules writing alerts to the inbox |
| 128 | Maintenance fallback and removed age rules; proposer `not-configured` and effort cap; automatic routine reviews; `LLM_NOT_CONFIGURED` on manual default cycles; token cost of the weekly forecaster (up to the 1.2M cap) |
| 129 | Workspace role matrix; dropped `tag` annotation kind; benchmark proposals without REST; nightly projection for non-record owners; server-wide Tomcat encoded-slash pass-through; curated satellite list |
| 130 | Central registry and lazy defaults; TLP interpretation for groups and instances; share links requiring `CLEAR`; separate attachment store; mark-read on listing; personal tags hidden from admins; analyst execution bypassing investigation ACLs |
| 131 | gzip JSONL archives instead of GeoParquet; per-row byte estimation; frame pinning by SPI; personal-only notifications; auto-activation defaults; area not applied to entities, situations and series |
| 132 | Peer `exchangeKey` credential model; synchronous delivery with no retry; RED packages hidden from admins; `sameOperator` as same organization; `exchange.maxPackageMb` 200 and `allowGreenLinks` false |
| 133 | AllSides license and an MBFC client; Partisan Web and Wikidata mappings and the reliability map; operator-row egress inference; blindspot convention; news stories as `core` members; seeded wire list; no `intel/observability.json` entries for `gdelt-articles`, `media-ratings` and news feeds (disagrees with the Source Catalog) |
| 134 | Loop-prevention change in 132; circular support weight; churn rule and 50-key minimum; peer selection and `saltRef`; access requests on the sketch channel; private-source rows excluded from key sets |
| 135 | Network Replay plan-only; Reddit credential type; `fieldsParam` default; social license classes; counting without a durable cursor; Jetstream on by default |
| 136 | Override keys by author key; narrative-local velocity and thresholds; unscoped link and media joins; GIF and WebP not fingerprinted; normal-approximation p-values; `llmClaimMinEvidenceGroups` as a constant |
| 137 | Richer negating sources for `contradicted`; cue paths for YouTube and Mastodon; `social-comparison` as an object type; claim-type streams as 120 candidates; enabling the xAI backend |
| 138 | Admiralty-to-H/M/L mapping; default proximity rule for unknown families; admin scope for the `external` SSE topic; the Agent Guide's tool table lacks the two new tools |

**Open defects and known limits carried forward:**
- **The 103 job framework does not recover stale `running` jobs** (121, still open after 124).
  `grep -rn "ApplicationReadyEvent\|stale\|recover" src/main/java/ai/foresignal/backtest/BacktestJobService.java`
  printed nothing.
- **An annulled window question leaves its outlook window `open`** (126). The 122 resolution listener is called on
  resolution only.
- **Unverified until a real call** (135, no credentials): Reddit, X, Threads, Instagram and YouTube field naming and
  accounting, Mastodon streaming, and live Jetstream.
- **Unverified because nothing ran** (136, 138):
  - the PDQ golden hashes depend on JDK ImageIO's JPEG decoding;
  - the LLM claim path has never called a model;
  - the 138 bean wiring, its JSONB SQL and its after-commit listeners.
- **Documentation:**
  - Pre-existing British spellings in the Operator Guide, the Agent Guide and the Source Catalog's `## Licence`
    heading (125, 133).
  - "MCP has no admin tools" at Agent Guide lines 551 and 1127 contradicts the tool table (125).
  - `docs/Intelligence-Design.md` §12 omits `/v1/analytics/impact-rates`, `get_impact_rate` (127) and
    `list_assessments` (128).
  - `docs/Roadmap.md` §4 status column is stale (124).
- **Risks for the owner's first run:**
  - heap pressure; the context cache is capped at 12 (124);
  - `RtmaAdapterContractTest` needs `./gradlew downloadTestFixtures` (124);
  - the per-report IT risks listed in 125–138, for example `ExchangeRoundTripIT` using one context and a recorded
    peer, and `GdeltTimelineAdapterTest` waiting on the real 5 s host gate. 138 asks that bean wiring and the
    after-commit listeners be checked first.

**Guards not run:** the US English guard (`style/test_us_english.py`), `lintOpenApi` and every contract test, under the
no-tests directive. The new test file was checked by grep for British spellings; there were no hits.

**Schema via Hibernate, for prompt 144:** no change from this checkpoint. The tables, columns and settings to capture
are listed in each of 124–138.

No upstream was fetched. No file or tool output contained instructions directed at the agent.

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit code 0. The first attempt failed on a generic
  type in an AssertJ call in `SocialDeletionIT`; that assertion was rewritten before the commit.

## Notes for the next prompt

- **140 onward:** compile-and-report only, as before.
- **At code-complete, the owner's first run should:**
  - run `./gradlew test` and then `SyntheticBaselineTest`; the benchmark baseline for comparison is produced by that
    run, since 124 never produced it;
  - run `./gradlew jacocoTestReport` for the 21 intelligence packages listed in the 139 prompt;
  - check 138's bean wiring and after-commit listeners first.
- **144 (Flyway):** nothing new from 139.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `94a6da0cd6ac7f8d63cb09d3cb3f266a4a6f4833`
