# 132-CR-Reports-And-Intelligence-Exchange

| | |
|---|---|
| Prompt | 132-Reports-And-Intelligence-Exchange.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `8818e13532feb8ef0c643469a7ef836734bf2b1e` |
| Date | 2026-10-17 |

## Summary

This prompt adds **reports**, finished intelligence products, and **intelligence exchange**, which sends signed
packages between Foresignal instances. The code lives in two new packages, `ai.foresignal.report` and
`ai.foresignal.exchange`, plus two controllers and four MCP tools. It compiles. **The tests are written but were not
run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is measured.

- **Reports** (`ReportEntity`, `ReportService`, `ReportBlocks`, `ReportVocabulary`):
  - A report is a 130 work product with an owner, an access list and a TLP label. Without a role it is
    `404 UNKNOWN_REPORT`. Display ids are `RPT-yyyy-nnnn`.
  - It holds 11 block types: `markdown`, `bluf`, `objects`, `map`, `timeline`, `hypotheses`, `forecasts`,
    `evidence`, `coverage`, `attachments` and `sources`. Every block carries a text origin (`author`,
    `deterministic` or `llm`).
  - A key judgment that carries a probability must cite a question or a hypothesis set, and its ICD 203 term is
    derived from the number.
  - Edits take the version from `If-Match` or `version`; a stale one is `409 REPORT_VERSION_CONFLICT`.
  - The TLP label inherits the strictest label of the cited envelopes, and asking for a weaker label is refused.
- **Drafting** (`ReportDrafter`, `ReportNarrativeWriter`, `ReportSnapshots`):
  - Five templates: `situation-brief`, `investigation-summary`, `chronicle-recap`, `weekly-assessment` and `custom`.
  - Six source types: `selection`, `tag`, `investigation`, `chronicle`, `situation` and `workspace`.
  - Drafts are deterministic. Coverage caveats include `CAPTURE_GAP`, `UNRESOLVED_REFERENCES` and
    `MEDIA_COVERAGE_UNAVAILABLE`.
  - `narrative=true` runs a 028 `ToolLoop` whose output tool is `write_report_narrative`. Its citations are verified
    and its text is linted. It needs agent scope (`403 INSUFFICIENT_SCOPE` otherwise) and a chat provider
    (`409 LLM_NOT_CONFIGURED` otherwise).
- **Publishing** (`ReportPublisher`, `ReportRenderer`, `ReportRevisionEntity`):
  - Publishing freezes a revision. Object references are pinned (records as `@seq`) and their envelopes are
    snapshotted.
  - The 055 static map is rendered and stored as an attachment, or the block gets a caveat when it cannot be.
  - The `sources` block is regenerated. A 116 manifest (`report` 1.0.0) is stored with the bundle's SHA-256.
  - Renditions come in Markdown, HTML and JSON. They carry the TLP banner at the top and bottom, the footer
    "Automated analysis inputs; verify before acting", and LLM labels. Map images are inlined as `data:` URIs.
  - Retraction withdraws the revision and sends `report.retracted` to everyone the report was sent to.
- **Sending and links** (`ReportSender`, `ReportShareTarget`, `ReportShareableSource`, `ReportObjectResolver`):
  - Sends go to principals and groups. Each recipient is checked against TLP, given a viewer grant, and told with
    `report.received` on their own channels.
  - Comments on a block use the reference `report:{id}#{blockId}`.
  - A 112 share target `report` is served as sandboxed HTML at `GET /v1/k/{key}/report` (CSP `default-src 'none'`,
    nosniff). It is allowed for `CLEAR`, and for `GREEN` only with `exchange.allowGreenLinks`.
- **Exchange** (`ExchangeService`, `ExchangeInbox`, `PackageCodec`, `PackageVerifier`, `ExchangeTlp`,
  `PeerExchangeClient`, `ExchangeRenditions`, `RemoteObjectResolver`):
  - Package types are `report`, `investigation`, `evidence` and `chronicle-extract` (through the new public
    `ChronicleExporter.writeExtract`), plus forwarding of a received package.
  - Content the egress policy keeps local is reduced to counts.
  - `manifest.json` is signed with a detached Ed25519 JWS (109, `b64:false`) by the instance key.
  - The size limit is `exchange.maxPackageMb` (200); a larger package is `413 PACKAGE_TOO_LARGE`.
  - Delivery to 113 peers is synchronous. Receipts and acknowledgements are signed.
  - On receipt, the inbox checks for loops (`PACKAGE_LOOP`) and duplicates (`PACKAGE_ALREADY_IMPORTED`), then tries
    the sender's keys in this order: a JWKS pinned at import, a stored sender key, the peer's pinned key, the peer's
    JWKS. It then verifies the hashes and the recipients (`422 PACKAGE_RECIPIENT_UNKNOWN`).
  - Received objects are stored read-only as `remote:{instanceId}/{type}:{id}` and re-rendered locally with an
    origin note.
- **Wiring:**
  - `ObjectTypes` adds `report` and `remote`, and the contract version is now 1.3.
  - `ObjectRef` validates remote references.
  - `ObjectEnvelope.Builder.tlp` is added.
  - Ten new error codes.
  - Settings `exchange.maxPackageMb` and `exchange.allowGreenLinks`.
  - Me events `report.received`, `package.received` and `report.retracted`, in the preference category `report`.
  - Share-key sub-path `/report`.
  - Peers get an `exchangeKey`.
  - AccessPolicy rules for every new path.
  - `InvestigationsController` audits copies of received items (`copyReceivedItem`).
- **Docs and tooling:**
  - `docs/OpenAPI.json`: 27 operations implemented, removed from `pending-operations.txt` (79 lines left).
  - `scripts/generate-postman.py` and the collection were regenerated ("844 requests … covers all ops: True").
  - `docs/Client-Contract.md` was replaced whole.
  - `docs/Agent-Guide.md` was updated.
  - `docs/Operator-Guide.md` was read end to end and written whole by script, adding the section "Reports and exchange
    (prompt 132)".

## Files created / modified

**Created (main):**
- `report/`: `package-info`, `ReportVocabulary`, `ReportEntity`, `ReportRepository`, `ReportRevisionEntity`,
  `ReportRevisionRepository`, `ReportSendEntity`, `ReportSendRepository`, `ReportBlocks`, `ReportSnapshots`,
  `ReportService`, `ReportRenderer`, `ReportPublisher`, `ReportDrafter`, `ReportNarrativeWriter`, `ReportSender`,
  `ReportShareableSource`, `ReportObjectResolver`, `ReportShareTarget`.
- `exchange/`: `package-info`, `ExchangeVocabulary`, `ExchangePackageEntity`, `ExchangePackageRepository`,
  `ReceivedPackageEntity`, `ReceivedPackageRepository`, `ReceivedObjectEntity`, `ReceivedObjectRepository`,
  `ExchangeSenderKeyEntity`, `ExchangeSenderKeyRepository`, `PackageCodec`, `PackageVerifier`, `ExchangeFiles`,
  `PeerExchangeClient`, `ExchangeRenditions`, `ExchangeTlp`, `ExchangeService`, `ExchangeInbox`,
  `RemoteObjectResolver`.
- `api/ReportsController`, `api/ExchangeController`.
- `mcp/tools/ListReportsTool`, `GetReportTool`, `DraftReportTool`, `SendReportTool`.

**Created (tests):** `report/ReportItSupport`, `ReportDraftIT`, `ReportPublishIT`, `ReportTlpTest`,
`ShareLinkReportIT`, `ReportControllersIT`; `exchange/ExchangeTestPackages`, `PackageSigningTest`,
`ExchangeRoundTripIT`.

**Modified:** `object/ObjectTypes`, `ObjectRef`, `ObjectEnvelope`; `api/error/ErrorCodes`; `settings/SettingKeys`;
`collab/MeEvents`, `MeTopic`, `ShareableTypes`; `share/ShareKeyResolver`; `federation/PeerEntity`,
`PeerRepository`, `PeerService`; `security/AccessPolicy`; `chronicle/ChronicleExporter`;
`api/InvestigationsController`; tests `object/ObjectRefTest`, `stream/sse/EventSchemaConformanceTest`;
`src/test/resources/openapi/pending-operations.txt`; `docs/OpenAPI.json`, `docs/Client-Contract.md`,
`docs/Agent-Guide.md`, `docs/Operator-Guide.md`; `scripts/generate-postman.py`,
`postman/Foresignal.postman_collection.json`.

## Deviations from the prompt

1. **Peers get an `exchangeKey`.** This is the `fsp_` ingest key the remote instance issued to this one, stored
   encrypted (`exchange_key_ciphertext` and `exchange_key_nonce`) and shown only as `exchangeKeySet`. The existing
   peer record had no credential for pushing to the remote inbox.
2. **Extra error codes** beyond the six the prompt named: `REPORT_NOT_PUBLISHED`, `PACKAGE_NOT_READY`,
   `PACKAGE_ALREADY_IMPORTED` and `PACKAGE_LOOP` (all 409).
3. **Block comments use references of the form `report:{id}#{blockId}`**, which resolve through
   `ReportShareableSource` and `ReportObjectResolver`.
4. **Delivery to peers is synchronous** within the create call. There is no retry queue: a failed delivery is
   recorded on the package and can be retried by downloading the ZIP or sending it again.
5. **Received renditions are re-rendered locally** from the package's structured content, sanitized and marked with
   their origin. The sender's HTML is never served.
6. **RED received packages are hidden even from admins** unless the admin is a named recipient.
7. **For remote recipients, "same organization" means the peer's `sameOperator`** in the AMBER+STRICT rule.
8. **Coverage caveats are deterministic.** Media coverage reports `MEDIA_COVERAGE_UNAVAILABLE` until 133 exists.
9. **Spec additions:** the manifest gains a `title`, `ReportBundle` gains `missing` and `attachments[].attachmentId`,
   and the map block gets a caveat field.
10. **Envelope TLP passthrough:** `ObjectEnvelope.withCollaboration` keeps the resolver's TLP when the collaboration
    layer has none, so received and report envelopes keep their labels.
11. **The TLP rules for packages are pure functions in `ExchangeTlp`**, so `ReportTlpTest` covers them without a
    context.
12. **Edited by script, not rewritten by hand:**
    - The spec was edited by script, as in earlier prompts.
    - The Operator Guide was read end to end in chunks and written whole by script. One section was appended, and
      three sentences were updated: the contract version, "the reports of later prompts", and the share-link target
      list.
    - The Agent Guide was read in full, then edited by script.
    - `docs/Client-Contract.md` was replaced whole.
    - One row of the Operator Guide's new template table was corrected after the implementation commit (the
      weekly-assessment source is `assessmentId`, not "the latest weekly assessment"). The fix is in the
      completion-report commit.

## Issues found / open questions

**Owner decisions:**
- (a) the peer `exchangeKey` credential model (Deviation 1);
- (b) synchronous delivery with no retry queue (Deviation 4);
- (c) RED received packages hidden from admins (Deviation 6);
- (d) `sameOperator` standing for "same organization" across instances (Deviation 7);
- (e) the defaults `exchange.maxPackageMb` = 200 and `exchange.allowGreenLinks` = false.

**Known limits:**
- The weekly-assessment template wraps one 128 assessment named by `assessmentId` (required).
- Media-coverage caveats wait for 133.
- Received packages cannot be edited, only acknowledged, added to an investigation as copies, or forwarded within
  their TLP.
- `docs/Invariants.md` RPT-1 and RPT-2 already name `ReportPublishIT`, `PackageSigningTest` and
  `ExchangeRoundTripIT`, so it was not changed.

**Schema for prompt 144:**
- Hibernate tables: `report`, `report_revision`, `report_send`, `exchange_package`, `received_package`,
  `received_object` and `exchange_sender_key`.
- New columns: `peer.exchange_key_ciphertext` and `peer.exchange_key_nonce`.
- New app settings: `exchange.maxPackageMb` and `exchange.allowGreenLinks`.
- New files: `${FORESIGNAL_DATA_DIR}/exchange/{sent,received,tmp}`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `EventSchemaConformanceTest`).
- New prose and code were checked by grep for British spellings, and none were found. The existing wire key
  `licence` (attribution) was left alone.

**Risks for the owner's first test run:**
- `ExchangeRoundTripIT` uses one context plus a recorded peer (key `fs-peer-a` from `ExchangeTestPackages`), not two
  Spring contexts.
- The multipart import takes `jwks` as a `@RequestParam`. A MockMvc multipart request must send it as a parameter,
  not as a file part.
- The map rendering path in `ReportPublishIT` accepts either an attachment or a caveat, depending on whether
  rendering is available in the test environment.

**External data:** none was fetched. No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `report/ReportDraftIT` | Situation brief, investigation summary, chronicle recap, tag and selection drafts; the LLM narrative with verified citations (fake client), its scope and provider requirements; invalid sources and templates |
| `report/ReportPublishIT` | Pinned refs and frozen snapshots (RPT-1), the TLP banner and footer, immutable revisions and version conflicts, retraction notifications, the map attachment or caveat |
| `report/ReportTlpTest` | Inherited TLP, refusal of weaker labels, link rules, package recipient rules, judgment citations and ICD terms |
| `report/ShareLinkReportIT` | CLEAR served, AMBER refused, GREEN behind `exchange.allowGreenLinks`, links dropped after retraction or relabeling |
| `report/ReportControllersIT` | CRUD, blocks, ETags and the optimistic lock, scopes for send, links, packages, import and peers, block comments, MCP tool shapes |
| `exchange/PackageSigningTest` | The JWS verifies with the JWKS and the pinned key; tampered manifest, signature, header or content fails; unsafe ZIP paths and oversized packages are refused (RPT-2) |
| `exchange/ExchangeRoundTripIT` | Read-only import for named recipients with remote refs, RED visibility, inbox delivery with a peer key, unknown recipients and tampering, receipts, forwarding and loop prevention (RPT-2) |
| `object/ObjectRefTest` (added case) | Remote reference parsing |
| `stream/sse/EventSchemaConformanceTest` (added cases) | Report and package me events |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0, no errors.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests … covers all ops: True".

## Notes for the next prompt

- **133 (media):** replace the `MEDIA_COVERAGE_UNAVAILABLE` caveat in `ReportDrafter` with real coverage.
- **New exchange types:** add to `ExchangeVocabulary.TYPES` and to the content builder in `ExchangeService`.
- **144 (Flyway):** capture the seven tables and the two peer columns.
- **Tests:** use `ReportItSupport` (`person`, `report`, `publish`) and `ExchangeTestPackages` (`newKey`, `report`,
  `sign`).

Implementation commit: `8818e13532feb8ef0c643469a7ef836734bf2b1e`
