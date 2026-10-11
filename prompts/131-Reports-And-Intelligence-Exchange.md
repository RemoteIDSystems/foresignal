# 131 — Reports And Intelligence Exchange

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/131-CR-Reports-And-Intelligence-Exchange.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/130-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §12.4 (reports and exchange), `docs/Client-Contract.md` §12 (reports, packages)
- Completion Reports of 018 (channels), 029 (renderers), 033 (inbox), 055 (static maps), 101 (investigations), 109 (instance signing key, JWS, licence policy), 111 (exports), 112 (share links), 113 (federation peers, provenance, loop prevention), 116 (manifests), 122 (ledger), 124 (hypotheses), 127 (assessments, export bundles), 128 (objects, relations, dossier), 129 (principals, groups, ACL, TLP, tags, attachments), 130 (chronicles)

## Objective

Let analysts turn what they collected into **reports** and send them to other people: on the same server, to principals on other Foresignal instances, or to anyone through a read-only link. A report is built from a selection, a tag, an investigation, a chronicle or a situation; it starts as a deterministic draft (with an optional LLM narrative), is edited by its authors, pins the exact versions it cites, and renders to Markdown, HTML and a JSON bundle. Sending to another instance uses a **signed intelligence package** that the receiver imports read-only with full provenance and TLP enforcement.

## Principles (binding)

- **Reports cite pinned evidence.** Every embedded object is a reference with `@seq` or a snapshot; a report reads the same tomorrow as today.
- **TLP bounds every send.** A send that would breach TLP (129) is refused with `TLP_VIOLATION`, naming the rule.
- **Signed and attributable.** Packages are signed with the instance key (109) and carry author principals, TLP and manifests; receivers verify before import.
- **No LLM required.** Drafts are deterministic; LLM narrative is optional, labelled and editable.

## Tasks

### 1. Reports (`ai.foresignal.report`)

- `report`: `id`, `display_id` (`RPT-{yyyy}-{nnnn}`), `title`, `subtitle`, `status` (draft | review | published | retracted), `owner`, `authors` (principals), `tlp` (at least the strictest TLP of its inputs), `visibility` + ACL (129), `source` (`{type: selection | tag | investigation | chronicle | situation | workspace, ref}`), `time_window`, `area`, `sections` jsonb (ordered blocks), `version` (int; each publish creates an immutable `report_revision`), `published_at`, `manifest_id`, timestamps.
- **Blocks:** `markdown` (author text; may contain `[[ref]]` object links), `bluf` (key judgments; probability-bearing judgments must reference a ledger question or hypothesis set and are rendered with ICD 203 terms through `EstimativeLanguage`), `objects` (a list of envelopes with pinned refs), `map` (a 055 static map of chosen layers, area and time, rendered and stored as an attachment), `timeline` (items from an investigation, situation or chronicle thread), `hypotheses` (a hypothesis set snapshot with revisions), `forecasts` (questions with ensemble, forecasters and scores), `evidence` (evidence items with Admiralty grades), `coverage` (observability and media-coverage caveats, 116 and 132), `attachments`, `sources` (auto-generated attribution and licences from 109).
- **Draft generation:** `POST /v1/reports/draft {source, template?}` (read scope; deterministic): builds blocks from the source (for a chronicle: its summary, timeline highlights, maps per major layer, linked hypotheses and forecasts; for a tag or selection: grouped envelopes, relations among them, a map, the related situations). Templates: `situation-brief`, `investigation-summary`, `chronicle-recap`, `weekly-assessment` (wraps a 127 assessment), `custom`. With an LLM and `narrative=true` (agent, `alwaysAuthenticated`, budgeted), a 028 tool loop writes a labelled narrative block with verified citations and lint-checked wording; it can be edited or deleted like any block.
- **Editing:** `PATCH /v1/reports/{id}` (editors; optimistic lock on `version` of the draft), block operations (`POST/PATCH/DELETE /v1/reports/{id}/blocks[/{blockId}]`), comments via 129 annotations on `report:{id}` or on a block (`report:{id}#{blockId}`).
- **Publish:** `POST /v1/reports/{id}/publish` freezes a `report_revision` (all refs pinned, maps and snapshots stored), renders **Markdown** and **HTML** (029 renderer; TLP banner on every page; footer "Automated analysis inputs; verify before acting" when any block is machine-generated) and a **JSON bundle** (report plus every cited envelope and snapshot, attachments by hash). `retract` marks a revision withdrawn and notifies recipients.

### 2. Sending on the same instance

- `POST /v1/reports/{id}/send {principals[], groups[], channels?: [principal channel ids], message}` (owner or editor): grants viewer ACL, notifies recipients (129 preferences; inbox item; optional delivery of the HTML rendition or a link through their channels), records the send. TLP is checked per recipient.

### 3. Exchange between instances (signed intelligence packages)

- **Package format** (`application/vnd.foresignal.package+zip`): `manifest.json` (package id, type `report` | `investigation` | `chronicle-extract` | `evidence`, sender instance id and public key id (109 JWKS), author principals (handle, display name, instance), TLP, created_at, recipients (instance id + principal handles, or `*` for GREEN/CLEAR), content hashes), `content/` (the JSON bundle and attachments), `signature.jws` (detached JWS over the manifest with the instance key).
- `POST /v1/exchange/packages {objectRef, recipients[], message}` (agent): builds the package (TLP-checked; LOCAL_ONLY and `private` data excluded or reduced to derived statistics per `EgressPolicy`; chronicle extracts limited by `exchange.maxPackageMb`, default 200).
- **Delivery:** to peers from 113 (`POST {peer}/v1/exchange/inbox` with the peer credentials 113 manages), or as a downloadable file (`GET /v1/exchange/packages/{id}/download`) to hand over by other means.
- **Receiving:** `POST /v1/exchange/inbox` (peer credential) or `POST /v1/exchange/import` (admin; uploaded file): verify the signature against the sender's JWKS (fetched from the peer or pinned by the admin), verify hashes, check that the recipient principals exist here, enforce TLP locally (a RED package is visible only to the named recipients), and import as **read-only** objects: `received_report` (rendered plus bundle), foreign object refs as `remote:{instanceId}/{type}:{id}` with local snapshots, attachments into the media store. Imported items show sender, authors, signature status and TLP; they never merge into local analytic tables (no evidence-group or ledger contamination) unless an analyst explicitly copies an item into a local investigation (logged, keeps provenance).
- `GET /v1/exchange/received?cursor` and `/{id}` (read; per ACL), `POST /v1/exchange/received/{id}/acknowledge` (sends a receipt to the sender when the peer channel exists), `GET /v1/exchange/sent` (agent).
- Loop prevention and provenance follow 113 (a package received from A is not re-sent to A unchanged; re-sends keep the original authors and add the forwarder).

### 4. Read-only links

- When 112 share links exist: `POST /v1/reports/{id}/share-link {expiresAt}` (owner; only for TLP CLEAR, or GREEN with the instance setting `exchange.allowGreenLinks=true`) serves the published HTML rendition under `/v1/k/{key}/report`.

### 5. API, MCP, spec, docs

- CRUD `/v1/reports`; `GET /v1/reports/{id}/revisions`, `GET /v1/reports/{id}/rendition?format=md|html|json&revision`.
- MCP: `list_reports`, `get_report` (read); `draft_report`, `send_report` (agent).
- SSE `me` topic (129): `report.received`, `package.received`, `report.retracted`.
- `docs/OpenAPI.json`: `Report`, `ReportInput`, `ReportBlock` (oneOf by type), `ReportRevision`, `ReportSend`, `PackageManifest`, `ReceivedPackage`, `ExchangeReceipt`; tags `Reports`, `Exchange`; `x-foresignal-scope`; Postman BODIES for draft from a tag, publish, send, package create, import.
- AccessPolicy rows. Problem codes: `UNKNOWN_REPORT`, `UNKNOWN_PACKAGE` (404), `TLP_VIOLATION` (403), `PACKAGE_SIGNATURE_INVALID` (400), `PACKAGE_RECIPIENT_UNKNOWN` (422), `PACKAGE_TOO_LARGE` (413), `REPORT_VERSION_CONFLICT` (409).
- `docs/Operator-Guide.md` "Reports and exchange": templates, publishing, sending, peer setup, pinning a sender's key, what a receiver can and cannot do with imported material.

## Tests to write (do not run)

- `ReportDraftIT`: drafts from a tag, an investigation, a chronicle and a situation contain the expected blocks; deterministic without an LLM; LLM narrative labelled and cited (fake client).
- `ReportPublishIT`: pinned refs, frozen snapshots, renditions with the TLP banner, immutable revisions, retraction notifications.
- `ReportTlpTest`: inherited TLP; sends refused per rule.
- `PackageSigningTest`: manifest JWS verifies with the instance JWKS; tampered content or signature fails.
- `ExchangeRoundTripIT` (two Spring contexts, or one context plus a recorded peer): package from A imported at B read-only for the named recipients only; RED visibility; remote refs; acknowledgement receipt; loop prevention.
- `ShareLinkReportIT`: CLEAR allowed, AMBER refused.
- `ReportControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Reports can be drafted from tags, selections, investigations, chronicles and situations, edited, published with pinned evidence and TLP banners, and sent to principals here or on other instances.
- Packages are signed, verified, TLP-enforced and imported read-only with provenance.
- No LLM is needed for any of it.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `131: Reports And Intelligence Exchange`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/131-CR-Reports-And-Intelligence-Exchange.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `131: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
