# 142-CR-Full-Test-Run-And-Fix

| | |
|---|---|
| Prompt | 142-Full-Test-Run-And-Fix.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `93116efe45ca5df3069aaeb3f7beac889cfcc2a2` |
| Date | 2026-10-18 |

## Summary

**The full test run was NOT performed.** Under the owner directive of 2026-10-17 (the override banner at the top of
`prompts/142-*.md` and `prompts/000-Run-Sequence.md` step 4), no test was run.

- **Deferred to the owner at code-complete:**
  - `./gradlew clean test jacocoTestReport jacocoTestCoverageVerification`;
  - the two extra full runs that check for flaky tests;
  - the coverage gate (≥ 90% LINE, BUNDLE);
  - the `cli/` build's `test`;
  - the invariants check against `docs/Invariants.md`;
  - every focus-area check in the prompt's step 5.
- `./gradlew compileJava compileTestJava` succeeds on the committed tree: exit code 0.
- There are no pass/fail/skip counts, no coverage figures and no flaky-test list, because nothing ran.

The implementation fixes the two open defects that 139 carried forward. Both were found by reading the code:

| Path | Change |
|---|---|
| `src/main/java/ai/foresignal/backtest/BacktestJobEntity.java` | Modified: column `worker_instance` |
| `src/main/java/ai/foresignal/backtest/BacktestJobService.java` | Modified: restart recovery |
| `src/main/java/ai/foresignal/forecast/LedgerHooks.java` | Modified: `questionAnnulled` default method |
| `src/main/java/ai/foresignal/forecast/QuestionResolutionListener.java` | Modified: `questionAnnulled` default method |
| `src/main/java/ai/foresignal/forecast/LedgerCoordinator.java` | Modified: dispatches annulments after commit |
| `src/main/java/ai/foresignal/forecast/ForecastService.java` | Modified: `annul` calls the hook |
| `src/main/java/ai/foresignal/outlook/regional/RegionalOutlookService.java` | Modified: overrides `questionAnnulled` |
| `src/test/java/ai/foresignal/backtest/BacktestControllersIT.java` | Modified: `restartRecovery` |
| `src/test/java/ai/foresignal/outlook/regional/RegionalOutlookIT.java` | Modified: `annulledWindow` |

## Fixes

### 1. Interrupted backtest jobs were never recovered (121, open since 124)

- **Cause.** The 103 worker pool is an in-memory `ExecutorService`. After a restart, a job that was `running` stayed
  `running` forever, and a `queued` job was never handed to a worker. Both block the one-job-per-token rule
  (`BACKTEST_IN_PROGRESS`). The 121 import and base-rate refresh also return the stuck job on re-submit.
  139 confirmed this with `grep -rn "ApplicationReadyEvent\|stale\|recover"` on `BacktestJobService.java`, which
  printed nothing.
- **Change.**
  - `BacktestJobService.INSTANCE` (`BacktestJobService.java:107`) is a static random UUID per JVM.
  - It is stamped on submit (`:248`, `:318`) and on the conditional claim (`:536`), in the new nullable column
    `backtest_job.worker_instance` (`BacktestJobEntity.java:93`).
  - `recoverInterrupted()` (`BacktestJobService.java:734`) runs at `ApplicationReadyEvent`:
    - every `running` job whose `worker_instance IS DISTINCT FROM` this JVM's ends `failed`, with `result`
      `{"error": "interrupted by a restart"}` and `finished_at` set;
    - every such `queued` job is handed to the worker pool. The existing conditional claim keeps a job from running
      twice.
- **Why a per-JVM id and not a start-time cutoff.** `IntegrationTest`'s Javadoc says one Testcontainers database is
  shared by every cached context. A cutoff based on time would let a newly started context fail a job that another
  live context in the same JVM is running. Tests also use `MutableClock`s set to arbitrary instants. All contexts
  in one JVM share the static id, so none recovers another's jobs. A restarted broker gets a new id.
- **Test written (not run).** `BacktestControllersIT.restartRecovery` (`:213`):
  - a `running` job owned by `old-process` is failed with the error;
  - a `queued` job with a null owner is re-dispatched and finishes `done`, stamped with `INSTANCE`;
  - a `running` job owned by this JVM is left alone.

### 2. An annulled window question left its outlook window `open` (126)

- **Cause.** `QuestionResolutionListener` was called only from `LedgerCoordinator.questionResolved`.
  `ForecastService.annul` called no hook, so `RegionalOutlookService` never learned of an annulment. Its
  `onResolved` already writes `annulled` for any question whose status is not `resolved`.
- **Change.**
  - `LedgerHooks.questionAnnulled` (`LedgerHooks.java:24`) is a default no-op. `ForecastService.annul` calls it
    inside the annulling transaction (`ForecastService.java:407`).
  - `LedgerCoordinator.questionAnnulled` (`LedgerCoordinator.java:90`) tells every listener after commit, through
    `ForecastEvents.afterCommit`. A failing listener is logged, as on the resolve path.
  - `QuestionResolutionListener.questionAnnulled` (`QuestionResolutionListener.java:24`) is a default no-op, so
    `HypothesisQuestionHook` behaves exactly as before. An annulment does not confirm or refute anything.
  - `RegionalOutlookService.questionAnnulled` (`RegionalOutlookService.java:682`) delegates to its existing
    `questionResolved` path.
- **Test written (not run).** `RegionalOutlookIT.annulledWindow` (`:190`): annulling window 0's question marks that
  window `annulled` with no `actual`, and window 1 stays `open`.

### Tests changed

None. Two test methods were added (above). No existing assertion changed.

### Coverage exclusions

None added. `build.gradle.kts` was not touched.

## Deviations from the prompt

1. **No test was run** (owner directive). None of the acceptance criteria that depend on running anything was
   checked:
   - the suite passes;
   - two more runs are green;
   - coverage ≥ 90%;
   - per-run counts are recorded.
2. **No fixes from failures.** The prompt's fixes come from the run. The only fixes here are the two defects that
   reading the code showed and that earlier reports carried as open.
3. **No new tests for coverage, and no new OpenAPI-scope walk test.**
   - The prompt adds coverage tests "from the JaCoCo XML", which does not exist.
   - It also asks for a test that walks every operation in `docs/OpenAPI.json` and compares its `x-foresignal-scope`
     with the enforced scope. One already exists:
     `AccessPolicySyncTest.implementedRoutesMatchTheirScope`. `grep -rln "x-foresignal-scope" src/test/java`
     lists it. It walks `OpenApiSpec.routes()`, but only operations whose status is `implemented`. Planned routes
     get a separate check for the always-authenticated flag. Whether to widen it to every operation is for the
     owner.
4. **`docs/Roadmap.md` was not updated** to mark 001–142 Done. Documents are replaced whole, and the §4 status column
   is stale (124). Left for the owner.
5. **The invariants check was not done.** It means confirming that a passing test asserts each invariant, which
   needs a run.

## Owner: check first when running the suite

Prioritized from the 124–141 reports. Every item is unverified because nothing has run.

1. **Context startup and bean wiring.** Context-load failures cascade, and no context has started since the
   directive. The newest wiring is the riskiest:
   - 141: `PluginRuntime`, the `wasm-feed` family, the MCP OAuth beans, and the `TokenFilter`/`McpStepUpFilter`
     order (`McpOAuthIT`);
   - 140: the doctor components;
   - 138: the binding router and its after-commit listeners;
   - 123/124: the five multi-constructor beans that gained `@Autowired`.
2. **Heap and context count.** The context cache is capped at 12 (124). Expect heap pressure on a full run.
3. **Shape guards, which run whatever was touched:**
   - the US English guard, `python3 style/test_us_english.py` (pre-existing British spellings were reported in the
     Operator Guide, the Agent Guide and the Source Catalog's `## Licence` heading; 141 notes the EDP schemas copy
     "metres");
   - `AccessPolicySyncTest`, `RestrictedReadCoverageTest`, `OpenApiContractIT`, `SpecAnnotationsTest`, `lintOpenApi`;
   - `PostmanCollectionContractTest` (56 entries added in 124);
   - `EventSchemaConformanceTest` (`intel.*` and `analyst.*` added in 124).
4. **The two fixes in this commit:**
   - `BacktestControllersIT.restartRecovery`;
   - `RegionalOutlookIT.annulledWindow`;
   - that `recoverInterrupted` at startup disturbs no other IT.
5. **After-commit and JSONB paths:**
   - 138: the binding SQL;
   - 140: `DoctorFinding` round-tripping through jsonb, the native `distinct on` query, and the synchronous auto run
     inside `HealthService` (`DoctorAutoRunIT`).
6. **WireMock-backed LLM and TTS ITs:**
   - `DoctorLlmIT` (the Anthropic SDK against a localhost `baseUrl`);
   - `AudioBriefingIT`;
   - `NotificationTranslationIT`;
   - the 136 LLM claim path, which has never called a model.
7. **Golden and fixture tests:**
   - the PDQ hashes depend on JDK ImageIO's JPEG decoding (136);
   - `FdsnEventIT`'s XSD validation (141);
   - `RtmaAdapterContractTest` needs `./gradlew downloadTestFixtures`, or its GRIB test skips (124).
8. **Timing-sensitive tests:**
   - `GdeltTimelineAdapterTest` waits on the real 5 s host gate;
   - `ExchangeRoundTripIT` uses one context and a recorded peer (132).
9. **Benchmark and coverage:**
   - `SyntheticBaselineTest` produces the first benchmark baseline; 124 never produced it;
   - then `./gradlew jacocoTestReport jacocoTestCoverageVerification` against the 90% gate.
10. **Separate toolchains** (outside the broker build):
    - `./gradlew -p cli test`;
    - `dart test`, `vitest` and `pytest` for the SDKs and EDP libraries;
    - the `generateSdks` and `checkSdkGeneration` tasks, which have never been run (141).

## Issues found / open questions

- **Owner decisions:** none new. The decisions carried in 139 and raised in 140 (a)–(d) and 141 (a)–(k) still
  stand.
- **Recovery policy.** Re-dispatching orphaned `queued` jobs, rather than failing them, is my choice. Say if they
  should fail instead.
- **Still open:**
  - "MCP has no admin tools" in the Agent Guide contradicts the tool table (125, 140);
  - `docs/Intelligence-Design.md` §12 omissions (127, 128);
  - the stale Roadmap §4 status column;
  - the Agent Guide's tool table lacks the two 138 tools.
- **Guards not run** (owner directive): the US English guard, `lintOpenApi` and every contract test. The diff was
  checked by grep for common British spellings; there were no hits.
- No upstream was fetched and no dependency was added. No file or tool output contained instructions directed at the
  agent.

**Schema via Hibernate, for prompt 144:** capture the new nullable column `backtest_job.worker_instance varchar(36)`.

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit code 0. The output contained only the existing
  unchecked-operations note.
- The `cli/` build was not touched by this prompt and was not rebuilt. 141 reports
  `./gradlew -p cli compileJava compileTestJava` BUILD SUCCESSFUL.

## Notes for the next prompt

- **143 (Runtime Verification):** not run. The owner does it at code-complete (`prompts/000`).
- **144 (Flyway):** add `backtest_job.worker_instance varchar(36) NULL` to everything carried from 124–141.
- **At code-complete:** start with the "check first" list above. Context startup comes first.

The full test run was **NOT performed**. Tests were written but **not run** (owner directive 2026-10-17; all test
execution is deferred to the owner at code-complete).

Implementation commit: `93116efe45ca5df3069aaeb3f7beac889cfcc2a2`
