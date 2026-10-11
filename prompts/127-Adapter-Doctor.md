# 127 — Adapter Doctor

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/127-CR-Adapter-Doctor.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/126-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Product-Charter.md` G6, G8, G11, FR-7 (probe), FR-11 (silent format change), FR-57 (adapter doctor), NFR-13 (no LLM call without an explicit trigger), NFR-14 (prompt-injection containment), NFR-15 (local-first), §14 success criterion 5
- `docs/Core-Data-Model.md` §8 (`fetch_log` columns: status, http_status, bytes, `body_sha256`, `adapter_version`, records/rejected counts, `coverage`, `error`), §14 (coverage and the "coverage dropped" rule)
- Completion Reports of 005 (`RequestRecorder`, `FetchTrace`, redaction), 006 (health statuses, reasons, `HealthRule` SPI, format-change vs coverage), 010 (probe: fetch once, raw plus parsed, stores nothing; consumes budget), 011 (Adapter Guide, fixture layout, fieldSpecs contract test), 018 (system events), 025 (McpToolRegistry), 026 (LLM roles, budgets, usage, JSON output support per provider), 035 (alwaysAuthenticated on LLM-spending endpoints)
- `docs/Adapter-Guide.md` (fixture capture procedure, `src/test/resources/fixtures/{sourceId}/…`)

## Objective

When a source breaks, tell the operator or the maintaining agent what changed and how to fix it. The adapter doctor probes the live upstream, compares the response with the adapter's recorded fixture structurally, runs deterministic diagnosis rules, and optionally asks an LLM (frontier or local) to explain the likely change and propose a parser patch as text. Patches are never applied. Reports integrate with health, channels and MCP, and everything except the explanation works without an LLM.

## Tasks

### 1. Runtime access to fixtures and adapter source

- **Fixtures:** a Gradle `processResources` step copies `src/test/resources/fixtures/**` into the jar under `META-INF/foresignal/fixtures/` with a per-file cap of 256 KB (larger files are truncated to a structurally valid prefix where possible: first N JSON array elements, first 500 CSV rows; otherwise skipped with a log line). Record a `fixtures-index.json` (sourceId → files, captured date from the Adapter Guide fixture header if present, sha256).
- **Adapter source:** copy `src/main/java/ai/foresignal/source/adapters/**/*.java` into `META-INF/foresignal/adapter-src/` with an index (sourceId → files) built from a `@AdapterSource` annotation or the package-per-adapter convention. The project is MIT, so shipping its own sources is fine. Never include third-party code or secrets.
- **`FixtureCatalog`** and **`AdapterSourceCatalog`** read these at runtime, and are absent-safe (the report says "no fixture for this source").

### 2. Structure extraction and diff (`ai.foresignal.doctor`)

- **`StructureExtractor`** by content type or sniffing:
  - **JSON:** path tree with `[]` for arrays (e.g. `features[].properties.mag`). Per path: observed JSON types, presence ratio across array elements, example values (≤ 3, each ≤ 80 chars, numbers kept, strings truncated), array length stats.
  - **CSV/TSV:** delimiter, header columns (or column count), per-column inferred type and null ratio.
  - **XML:** element/attribute paths with counts (DOM via the secure factory: no DTD, no external entities).
  - **Binary** (GRIB2, NetCDF, PNG, ZIP): content type, size, magic bytes, ZIP entry names.
  - **HTML when JSON/CSV is expected:** flagged as `HTML_INSTEAD_OF_DATA`, with the `<title>` captured.
- **`StructureDiff`:**
  - `addedPaths`, `removedPaths`, `typeChanged` (`path`, `from`, `to`), `presenceDropped` (ratio fell ≥ 50 points), `arrayEmpty`, `columnsReordered`, `delimiterChanged`, `contentTypeChanged`, `sizeRatio`
  - **rename candidates:** pair removed and added paths with the same parent, compatible types and either name similarity (normalised Levenshtein ≥ 0.6, or snake↔camel equality) or matching value distributions (same type, overlapping example ranges). Score each pair.

### 3. Deterministic diagnosis

- **Inputs:** health (status, reasons, consecutive failures, coverage history; 006), the last 20 `fetch_log` rows (010), budget and backoff state (005), the probe result (task 4), and the diff.
- **Rules → `Finding(code, severity, evidence, suggestedAction)`:**
  - `AUTH_FAILED` (401/403 with no data body) → check credentials or key expiry.
  - `UA_BLOCKED` (451, or 403 with a block-page body) → "contact the upstream or join its key/feeder programme; never spoof the User-Agent".
  - `ENDPOINT_MOVED` (404/410, or 301/308 to another host).
  - `RATE_LIMITED` (429, or backoff active) → budget advice from the descriptor versus observed usage.
  - `UPSTREAM_ERROR` (5xx streak).
  - `HTML_INSTEAD_OF_DATA` (login wall, maintenance page or captcha; Foresignal never solves captchas).
  - `EMPTY_RESULT` (structure intact, zero items; consider `zeroResultsNormal` and params).
  - `FIELD_RENAMED` (rename candidate score ≥ 0.7 on a path the parser reads).
  - `FIELD_REMOVED` (a removed path that the parser reads).
  - `TYPE_CHANGED`.
  - `FORMAT_CHANGED` (content type or delimiter).
  - `PARSER_REJECTS` (probe parse throws `UpstreamFormatException`: include the snippet).
  - `COVERAGE_DROPPED` (006 coverage rule).
  - `NO_CHANGE_DETECTED`.
- **"Paths the parser reads":** a static scan of the adapter source for string literals that match diff path segments (cheap heuristic, marked as such in the evidence).

### 4. Doctor run

- **`DoctorService.run(sourceId, DoctorOptions{probe=true, llm=auto|on|off, detail=compact|full})`:**
  1. Run the 010 probe through the normal budgeted client (no budget, or backoff active → skip with finding `PROBE_SKIPPED_BUDGET` and diagnose from history only). Keep the raw body in memory only, capped at 1 MB; it is never persisted.
  2. Extract structure from the probe body and from the fixture, then diff.
  3. Run the deterministic rules.
  4. LLM step (task 5) when enabled and a chat provider exists.
  5. Persist a `doctor_report`.
- **Entity `doctor_report`:** `id`, `source_id`, `created_at`, `trigger` (`MANUAL | AUTO | MCP`), `status` (`OK | DEGRADED_INFO | BROKEN`), `findings` jsonb, `diff` jsonb (structure only, no upstream values beyond the capped examples), `llm` jsonb (provider, model, tokens, explanation, patch, `patchApplies`), `probe` jsonb (status, content type, bytes, redacted URL, duration), `adapter_version`. Retain 90 days (`RetentionStep`).
- **Rate limits:** at most one run per source per 10 min (`409 DOCTOR_BUSY`); manual runs bypass the auto cool-off but not the budget.

### 5. LLM explanation and patch proposal

- **Provider:** the 026 `chat` role, with low effort when supported. Budgeted and metered as role `doctor`. Fully works with local providers (Ollama, OpenAI-compatible).
- **Context, compact by default** (≤ 6k tokens: fits small local models):
  - descriptor summary
  - findings
  - the structure diff (paths and types, not bodies)
  - up to 2 KB of probe excerpt **only** around the first divergent path
  - the parser file(s) from `AdapterSourceCatalog`, trimmed to the parse method(s) when over 400 lines
- `detail=full` raises the limits for frontier models (≤ 30k tokens).
- **Prompt-injection containment** (NFR-14): upstream content goes inside an explicit untrusted-data block with a fixed delimiter that is stripped from the content first. The system prompt states that the data may contain instructions to ignore. No tools are offered to the model, and the output is parsed as data.
- **Output schema** (JSON mode where the provider supports it; else lenient extraction of the first JSON object): `{summary (≤ 80 words), likelyCause, confidence (0–1), evidence[], suggestedActions[], patch?: {file, unifiedDiff}}`.
- **Patch validation,** in memory only: the diff must target a file in `AdapterSourceCatalog` and apply cleanly with a small in-house unified-diff applier (context must match). Store `patchApplies` and the patched file's `javac`-free sanity checks (balanced braces, the class name unchanged). **Never write the patch to disk, never compile or load it**, and label it "proposed, not applied" in every rendering.
- Failure, timeout (60 s), budget refusal or no provider → the report keeps the deterministic findings, with `llm.status` explaining why (G11).

### 6. Health and channel integration

- **Auto runs:** a `HealthRule` listener (006). When a source transitions to `degraded` or `failing` with a format-change, coverage-dropped or parse-error reason, run the doctor with `trigger=AUTO`, `probe=true` and `llm=off` (setting `doctor.autoLlm`, default false: no LLM call without an explicit operator opt-in, NFR-13). At most once per source per 6 h (`doctor.autoCooldown`).
- Health (`GET /v1/sources/{id}/health`) gains `lastDoctorReport` (`id`, `status`, top finding, `createdAt`).
- A `system` notification (018) for `BROKEN` auto reports: "{source}: {top finding} — {suggestedAction}", with a link to the report.

### 7. API, MCP, CLI hook, docs

- `POST /v1/sources/{id}/doctor` (admin; `alwaysAuthenticated` per 035 because it can spend LLM budget): body `DoctorOptions`, returns the report (synchronous, ≤ 90 s, with a 202 + `Location` fallback when longer).
- `GET /v1/sources/{id}/doctor/reports` (agent) and `GET /v1/doctor/reports/{reportId}` (agent).
- MCP (025): `diagnose_source` (admin) and `get_doctor_report` (agent), with compact output (findings, summary, patch only with `detail=long`).
- `docs/OpenAPI.json`: schemas `DoctorOptions`, `DoctorReport`, `DoctorFinding`, `StructureDiff`, `ProposedPatch`; `lastDoctorReport` on `SourceHealth`; operations with `x-foresignal-scope`; error code `DOCTOR_BUSY`. Update `AccessPolicy`. Regenerate Postman with a BODIES entry for `runSourceDoctor`.
- `docs/Adapter-Guide.md` "Adapter doctor": how fixtures feed it (keep fixtures current), reading findings, applying a proposed patch by hand plus refreshing the fixture and contract test. `docs/Agent-Guide.md`: a maintenance-agent loop (watch health → `diagnose_source` → open a merge request with the patch after human review). 128's CLI adds `foresignal sources doctor <id>`.

## Tests to write (do not run)

- `StructureExtractorTest`: JSON nested arrays and presence ratios, CSV inference, XML with XXE attempts rejected, binary sniffing, HTML detection.
- `StructureDiffTest`: added/removed/type change, presence drop, rename candidates (snake↔camel, Levenshtein, value distribution), no false rename across parents.
- `DiagnosisRulesTest`: one fixture per finding code; `UA_BLOCKED` advice never suggests spoofing.
- `DoctorServiceIT` (WireMock upstream for the 011 USGS adapter): a renamed field (`mag` → `magnitude`) yields `FIELD_RENAMED` with high score; HTML maintenance page; 451; probe skipped when out of budget; the report is persisted without raw bodies; `DOCTOR_BUSY`.
- `DoctorLlmIT` (WireMock Anthropic, OpenAI-compatible and Ollama stubs, `FakeLlmClient`): compact context under the limit; an injection string in the upstream body stays inside the untrusted block and cannot alter the output schema; lenient JSON extraction; patch applies / does not apply; timeout falls back to deterministic; usage metered under `doctor`.
- `UnifiedDiffApplierTest`: clean apply, context mismatch, multiple hunks, a file not in the catalogue rejected.
- `DoctorAutoRunIT`: a health transition triggers an auto run with `llm=off`; cooldown; system notification for BROKEN; `lastDoctorReport` in health.
- `FixtureCatalogTest`: packaged fixtures and index present in the built resources, truncation keeps JSON valid.

## Acceptance criteria

- A simulated upstream format change marks the source degraded, triggers an automatic deterministic doctor report naming the changed field, and notifies (Charter success criterion 5).
- With a local Ollama model, a manual run adds an explanation and a proposed patch that is validated in memory and never applied.
- No raw upstream body is persisted; upstream content can't steer the model's output schema.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `127: Adapter Doctor`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/127-CR-Adapter-Doctor.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `127: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
