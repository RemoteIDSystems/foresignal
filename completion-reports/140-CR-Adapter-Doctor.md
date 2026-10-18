# 140-CR-Adapter-Doctor

| | |
|---|---|
| Prompt | 140-Adapter-Doctor.md |
| Status | Complete (implemented and compiles; tests written and **not run** under the owner directive of 2026-10-17) |
| Implementation commit | `d524f57be3b905c44b9dcc8f30c33c90e8ace564` |
| Date | 2026-10-18 |

## Summary

When a source breaks, the **adapter doctor** (`ai.foresignal.doctor`) says what changed and how the parser could be
fixed. Everything except the explanation works without an LLM. Patches are never applied.

- **Runtime fixtures and adapter source (task 1):**
  - A new Gradle script, `gradle/doctor-resources.gradle.kts`, adds the task `packageDoctorResources`, which
    `processResources` depends on.
  - **Fixtures.** It copies `src/test/resources/fixtures/**` to `META-INF/foresignal/fixtures/` with a 256 KB cap per
    file:
    - JSON: the largest arrays are halved until the file fits, so it stays a valid document.
    - CSV/TSV: the header plus the first 500 rows, fewer if still too large.
    - JSONL: whole lines.
    - Anything else over the cap is skipped with a log line.
  - `fixtures-index.json` records, per source: the files, their sizes, SHA-256 and whether they were truncated; the
    capture date from the fixture README; and `synthetic`.
  - **Adapter sources.** It copies `src/main/java/ai/foresignal/source/adapters/**/*.java` to
    `META-INF/foresignal/adapter-src/`, with `adapter-src-index.json` (package → files).
  - `FixtureCatalog` and `AdapterSourceCatalog` read these at run time and are absent-safe.
  - **Verified by the compile.** `compileTestJava` runs `processResources`. The generated index lists 201 fixture
    sources, 18 files skipped (rasters and archives over the cap), 6 JSON files truncated, and 127 adapter packages
    with 33 files excluded.
- **Structure (task 2):**
  - `StructureExtractor` handles five kinds of body:
    - JSON: path trees with `[]`, types, presence per parent instance, ≤ 3 examples of ≤ 80 characters, numeric
      ranges and array lengths.
    - CSV/TSV: delimiter, header or `colN`, and per column the type and null ratio.
    - XML: element and `@attribute` paths, from a DOM factory with DOCTYPE disallowed, external entities off,
      XInclude off and secure processing on.
    - Binary: magic bytes, kind and ZIP entries.
    - HTML: detected, with its `<title>` kept.
  - `StructureDiff.compare` reports added, removed and retyped paths, presence drops of ≥ 50 points, emptied arrays,
    reordered columns, delimiter and content-type changes, and the size ratio.
  - It also reports scored rename candidates: snake↔camel (0.95), Levenshtein ≥ 0.6, or overlapping value
    distributions. Candidates are always under the same parent with compatible types, and each path joins at most one.
- **Deterministic diagnosis (task 3):**
  - `DiagnosisRules` produces all 15 codes of the spec's `DoctorFinding` enum, worst first. Its inputs are health
    facts, the last 20 `fetch_log` rows, budget and backoff, the probe, the diff and `ParserLiterals`.
  - `ParserLiterals` is the static string-literal scan. Findings that rely on it carry `heuristic: true` and an
    evidence line saying so.
  - `UA_BLOCKED`'s advice is the fixed `UA_BLOCKED_ACTION` ("…never spoof the User-Agent").
  - `COVERAGE_DROPPED` reuses the 006 rule through the new `HealthEvaluator.coverageDrop(List)`.
- **Doctor run (task 4):** `DoctorService.run/start(sourceId, DoctorOptions, trigger)`.
  - **Probe.** The new `ProbeService.capture` keeps the 010 probe and also returns the raw recorded exchanges, which
    are needed for a redirect's `Location`. The body is capped at 1 MB and held in memory only.
  - **Skipped probe.** The probe is skipped when the budget is exhausted (`budget`), in backoff (`backoff`), for
    push/derived sources (`no-upstream`) or when `probe=false`.
  - **Persistence.** The entity `doctor_report` holds `findings`, `diff`, `llm`, `probe`, `fixture` and
    `adapter_version`, with no raw body. `DoctorReportRetentionStep` (order 714) keeps it 90 days.
  - **Busy.** At most one run per source per 10 minutes, and never two at once: `409 DOCTOR_BUSY`.
- **LLM (task 5):**
  - `DoctorLlm` uses the 026 chat role with `Purpose.DOCTOR` and `Effort.LOW`, and offers no tools. The system prompt
    is the template `doctor-system@1.0.0`.
  - `DoctorPrompt` builds the context:
    - compact ≤ 6k estimated tokens, full ≤ 30k;
    - the descriptor summary and parser files, trimmed to parse methods over 400 lines;
    - an untrusted block (`<<<UNTRUSTED_UPSTREAM_DATA>>>`) holding the findings, diff and a 2 KB (8 KB full) excerpt
      around the first divergent path, with every form of the delimiter stripped from that content first;
    - the excerpt and examples are withheld when `EgressPolicy` excludes the source for that provider.
  - `DoctorLlmOutput` extracts the first balanced JSON object and keeps only the schema members, each with its type
    and size enforced.
  - The patch is checked in memory by `UnifiedDiffApplier` (context must match, ±50 lines of fuzz, a single file) and
    `PatchSanity` (balanced braces, class name unchanged). It must target a catalog file. It is labeled
    `proposed, not applied`.
  - A 60 s timeout, budget refusal, failure, refusal or no provider each set `llm.status`.
- **Health and channels (task 6):**
  - `DoctorAutoRunner` listens to `SourceHealthChangedEvent`. A transition to `degraded` or `failing` with a
    format-change, coverage-dropped or parse-error reason starts a run with `trigger=AUTO`, a probe, and `llm=off`
    unless `doctor.autoLlm` is set.
  - The cool-off is `doctor.autoCooldown`, 6 h by default.
  - A `BROKEN` report publishes a `system` notification, subtype `system.doctor_broken`: "{source}: {top finding} —
    {action}", with the report URL.
  - `SourceHealth.lastDoctorReport` is filled on `getSourceHealth`, `listSourceHealth` and `getSource`.
- **Surface (task 7):**
  - `DoctorController`:
    - `runSourceDoctor`: 200, or 202 + `Location` after 90 s;
    - `listSourceDoctorReports`: keyset pages;
    - `getDoctorReport`: 202 while running, otherwise the report or 404 `UNKNOWN_DOCTOR_REPORT`.
  - MCP tools: `diagnose_source` (admin, cost-bearing) and `get_doctor_report` (agent). Output is compact; the patch,
    diff and probe come only with `detail=long`.
  - AccessPolicy rows; error codes `DOCTOR_BUSY` and `UNKNOWN_DOCTOR_REPORT`; app settings `doctor.autoLlm` and
    `doctor.autoCooldown`.
  - The three operations are `implemented` and their lines are removed from `pending-operations.txt`, which now has 18
    lines.
  - Postman is regenerated, with a BODIES entry for `runSourceDoctor`.

## Files created / modified

**Created (main):**
- `doctor/`: `package-info`, `StructureFormat`, `Structure`, `StructureExtractor`, `StructureDiff`, `ParserLiterals`,
  `FindingCode`, `DoctorFinding`, `DoctorTrigger`, `DoctorStatus`, `DiagnosisInput`, `DiagnosisRules`,
  `FixtureCatalog`, `AdapterSourceCatalog`, `UnifiedDiffApplier`, `PatchSanity`, `DoctorOptions`,
  `DoctorReportEntity`, `DoctorReportRepository`, `DoctorReportView`, `DoctorReportSummary`, `DoctorReportPage`,
  `DoctorLlmOutput`, `DoctorPrompt`, `DoctorLlm`, `DoctorService`, `DoctorAutoRunner`, `DoctorReportRetentionStep`,
  `DoctorController`.
- `mcp/tools/DiagnoseSourceTool`, `mcp/tools/GetDoctorReportTool`.
- `resources/llm/templates/doctor-system@1.0.0.md`.
- `gradle/doctor-resources.gradle.kts`.

**Modified (main):**
- `ProbeService` (`capture`, `Capture`) and `HealthEvaluator` (public `coverageDrop(List)`).
- `SourcesController` (`lastDoctorReport`) and `SourceHealthDto` (`lastDoctorReport`, the old constructor kept,
  `withLastDoctorReport`).
- `ErrorCodes`, `SettingKeys` (and `ALL`), `PromptTemplates` (`DOCTOR`, role `doctor`), `AccessPolicy`.
- `application.yml` (`foresignal.doctor.auto-enabled`, `auto-async`), `build.gradle.kts` (applies the script).

**Test resources:** `application-test.yml` turns the automatic hook off, so no health transition in another IT probes
an upstream in the background. `pending-operations.txt` loses 3 lines.

**Docs and tools:**
- `docs/OpenAPI.json`: 3 operations implemented, additive reconciliations, 2 error codes, changelog 140.
- `docs/Adapter-Guide.md`: §10 repair procedure rewritten around the doctor, and a new subsection "Adapter doctor
  (prompt 140)" covering fixtures, sources, findings, API, auto runs, the LLM step and applying a patch by hand.
- `docs/Agent-Guide.md`: `diagnose_source` and `get_doctor_report` in the MCP tool table, two scope rows, and recipe 9
  "The maintenance-agent loop".
- Both guides were read end to end and written whole.
- `scripts/generate-postman.py` (BODIES) and `postman/` (regenerated).

## Deviations from the prompt

1. **"A `HealthRule` listener":** the 006 `HealthRule` SPI is an evaluator, not a listener. The auto hook listens to
   `SourceHealthChangedEvent`, the transition event `HealthService` publishes (018). It runs off the publishing thread,
   so a health read never waits on a probe.
2. **Adapter source index:** built from the package-per-adapter convention, not an `@AdapterSource` annotation. The
   index maps package → files; a source id resolves at run time through its adapter class's package.
   **33 files are excluded**, all of them for the reason below. Their adapters are diagnosed without parser source.
   - Every file whose first 40 lines name God's Eye View or say "ported from", applying the prompt's "never include
     third-party code" strictly. The ported code is MIT and attributed.
   - Anything resembling private-key material would also be excluded. No such file exists.
3. **Fixture content type:** fixtures carry no headers, so `contentTypeChanged` can only fire when both sides declare
   one. A format change shows as `FORMAT_CHANGED` through the recognized format (json → xml, csv delimiter, and so on).
4. **Binary probes:** the 010 recorder keeps bodies as decoded text. Binary magic and ZIP entries are therefore exact
   for fixtures (bytes) and best effort for probe bodies.
5. **JSON mode:** `ChatRequest` has no JSON-mode flag for any provider, so the doctor always uses lenient extraction of
   the first JSON object.
6. **Metering "as role doctor":** usage is metered per provider by the governed client (026). Every call is logged and
   budgeted with `purpose=doctor`; there is no per-role usage table.
7. **Spec reconciliations (additive):**
   - `StructureDiff.format` gains `text`.
   - `DoctorReport.probe` gains `skipReason`, `records`, `requests` and `error`.
   - `DoctorReport.fixture` gains `truncated`.
   - `DoctorReport.llm` gains `contextTokens`, `detail` and `error`.
   - `ProposedPatch` gains `error`.
   - `getDoctorReport` answers 202 (`DoctorRunAccepted`) while the run is in progress.
8. **Test-profile switch:** `foresignal.doctor.auto-enabled=false` in `application-test.yml`. `DoctorAutoRunIT` turns
   it on, synchronously, in its own context.
9. **MCP argument names:** `diagnose_source` takes `context` (`compact|full`) for the LLM context size, because
   `detail` (`short|long`) is already the shared MCP output-size argument.
10. **Not done here:** the Operator Guide does not yet describe `doctor.autoLlm` and `doctor.autoCooldown` (the prompt
    did not name it). The CLI command is 141's.

## Issues found / open questions

Owner decisions:
- (a) **The 33 adapter files left out** of the packaged sources because they are ported from God's Eye View
  (Deviation 2). Ship them, since they are MIT and attributed, or keep them out?
- (b) **Admin token for agents.** `diagnose_source` needs admin, while the Agent Guide says never to give an agent the
  admin token. The new recipe has the agent read reports with an agent token and leaves running the doctor to the
  operator.
- (c) **Contradiction in the Agent Guide.** "MCP has no admin tools" (scopes table, Security section) is now wrong
  twice over (125's note). Not changed.
- (d) **Notification severity.** The auto notification uses severity `warning`. Should a `BROKEN` report be
  `critical`?

Unverified because nothing ran:
- bean wiring of the new components;
- JSON serialization of `DoctorFinding` round-tripping through the `findings` jsonb;
- the native `distinct on` query;
- the Anthropic and OpenAI-compatible providers against localhost WireMock in `DoctorLlmIT`;
- the synchronous auto run inside a health read.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `doctor/StructureExtractorTest` | JSON nested arrays and presence ratios, root arrays, syntax errors; CSV inference and TSV without a header; XML paths; XXE and internal entities refused; binary sniffing (GRIB2, PNG, NetCDF, gzip, ZIP entries); HTML detection; text |
| `doctor/StructureDiffTest` | Added, removed and retyped paths; numeric and null are not type changes; presence drop; emptied arrays (element paths are not removals); renames by snake↔camel, Levenshtein and value distribution (`mag` → `magnitude`); no rename across parents or incompatible types; best candidate only; CSV reorder, delimiter and content type; HTML; no fixture; path helpers |
| `doctor/DiagnosisRulesTest` | One fixture per finding code; `UA_BLOCKED` advice never suggests spoofing; history-only diagnosis; worst-first order |
| `doctor/UnifiedDiffApplierTest` | Clean apply, context mismatch, multiple hunks with fuzz, malformed patches, sanity checks, a file outside the catalog refused |
| `doctor/FixtureCatalogTest` | Packaged index and USGS fixtures (size, SHA-256, capture date); every file ≤ 256 KB; truncated JSON parses; primary-fixture choice; absent-safe; adapter source index and exclusions |
| `doctor/DoctorLlmTest` | FakeLlmClient: compact context ≤ 6k, full larger; injection stays inside the block (delimiter once, `[removed marker]`) and cannot add members; lenient JSON; patch applies (built against the packaged file) and does not; timeout; budget, failure, refusal and no-JSON fallbacks; purpose doctor, low effort, no tools; trimming of long files |
| `doctor/DoctorLlmIT` | WireMock Ollama, OpenAI-compatible and Anthropic providers assigned through the API: answer parsed, injection inside the block in the real request body, usage metered to the provider, effort `low`, no tools; provider error and no provider |
| `doctor/DoctorServiceIT` | WireMock USGS: `mag` → `magnitude` gives `FIELD_RENAMED` (score ≥ 0.9); HTML maintenance page; 451; out of budget and in backoff, with no request sent; report persisted without raw body; `DOCTOR_BUSY` and the 10-minute window; no change; REST scopes (401/403), 409, list, get, 404, health's `lastDoctorReport`; 202 + `Location` |
| `doctor/DoctorAutoRunIT` | Real fetches: a format change degrades the source, then an AUTO report (llm off, `PARSER_REJECTS`, removed `features`), a `system.doctor_broken` notification, and `lastDoctorReport`; cool-off and `doctor.autoCooldown`; non-qualifying transitions; `doctor.autoLlm` opt-in; no notification for OK |
| `doctor/DoctorMcpToolsTest` | `diagnose_source` (admin, cost-bearing, MCP trigger, options, hidden source) and `get_doctor_report` (compact versus long, the patch label, unknown, hidden, running) |

## Compile result

- `./gradlew compileJava compileTestJava`: exit 0 on the committed tree. It ran `packageDoctorResources` through
  `processResources`, and the packaged resources were inspected.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".
- **Guards not run** (owner directive): the US English guard, `lintOpenApi`, `AccessPolicySyncTest`,
  `OpenApiContractIT`, `RestrictedReadCoverageTest` (both new tools carry `@RestrictedRead`) and the rest. New text was
  checked by grep for British spellings. The spec's "labelled" in `getDoctorReport` became "labeled". The UA advice
  says "program", not the prompt's "programme".

## External data and instructions

- Nothing was fetched and no dependency was added. The doctor was not run against any live upstream.
- No file or tool output contained instructions directed at the agent.

## Notes for the next prompt

- **141 (SDKs/CLI):** `foresignal sources doctor <id>` should call `POST /v1/sources/{id}/doctor` with
  `DoctorOptions`. On 202, poll `Location` until it answers 200. Print the findings, and print the patch with its
  "proposed, not applied" label.
- **142/owner run:** check first:
  - bean wiring;
  - `DoctorAutoRunIT`'s synchronous run inside `HealthService` (nested evaluation is guarded by the busy map and
    cool-off);
  - the Anthropic SDK against a localhost `baseUrl` in `DoctorLlmIT`.
- **144 (Flyway):** capture the table `doctor_report` with indexes `(source_id, created_at)` and `(created_at)`, and the
  app settings `doctor.autoLlm` and `doctor.autoCooldown`.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `d524f57be3b905c44b9dcc8f30c33c90e8ace564`
