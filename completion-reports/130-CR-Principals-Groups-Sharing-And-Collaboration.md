# 130-CR-Principals-Groups-Sharing-And-Collaboration

| | |
|---|---|
| Prompt | 130-Principals-Groups-Sharing-And-Collaboration.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `31793a16041fb0e3c50166bba9db5343c9ad2558` |
| Date | 2026-10-17 |

## Summary

This prompt lets several named people and agents work on one instance. It adds principal administration and
profiles, groups, an owner, access list and TLP label on every work product, tags, comment threads with mentions,
document and image uploads, graded evidence items, saved searches, and a personal inbox with its own SSE topic and
personal notification channels. The code lives in the new package `ai.foresignal.collab`. It compiles. **The tests
are written but were not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this
report is measured.

- **Principals** (`PrincipalAdminService`, `PrincipalViews`, `PrincipalCallers`, `MeService`):
  - `PrincipalEntity` gains `email`, `avatar_media_id`, `timezone` and `organization`.
  - `GET /v1/me`, `GET /v1/principals`, admin create, merge-patch update and `POST /v1/principals/{id}/tokens`.
  - Disabling a principal stops every token at once: `TokenService` refuses disabled principals (the bootstrap
    token excepted) through a 30-second Caffeine cache in `PrincipalService` that updates clear.
  - Principals and tokens already existed from 023, so there is no migration beyond the new columns;
    `PrincipalMigrationIT` checks that pre-130 tokens keep working under their principals.
- **Groups** (`GroupEntity`, `GroupMemberEntity`, `GroupService`): tables `principal_group` and
  `group_member`; the creator is a manager; managers and admins change and delete; deleting a group removes its grants.
- **Access** (`ShareableObjectEntity`, `ObjectAclEntity`, `ShareableSource`, `ShareableSources`,
  `AccessResolver`, `ShareService`, `CollabAudit`):
  - One central registry, `shareable_object` (owner, visibility, TLP), and `object_acl` (viewer, commenter, editor,
    owner for a principal, a group or the instance, with optional expiry).
  - `AccessResolver` implements the new `object/ObjectGrants` SPI, so `WorkspaceAccess` and the object service ask
    it without depending on `collab`. Missing access is `404 UNKNOWN_OBJECT`; insufficient role is `403 FORBIDDEN`.
  - Pre-130 objects are registered lazily, on first touch, from a per-type `ShareableSource` default.
  - `GET|PUT /v1/objects/{ref}/acl`, `POST /v1/objects/{ref}/share`, `GET /v1/shared-with-me` and `GET /v1/my`.
    Every change is audited (target types `object_acl`, `principal`, `group`).
- **TLP** (`security/Tlp`, `security/TlpTarget`, `EgressPolicy.tlp`): TLP 2.0 rules as one static method on the
  existing egress policy, applied to grants, visibility, share links and federation targets, refusing with `403
  TLP_VIOLATION` and the rule. Derived products inherit the strictest input label. Exports print `TLP:<label>` and
  send `Foresignal-TLP`; personal notifications start with `[TLP:<label>]`; envelopes carry `tlp`
  (`ObjectEnvelope.withCollaboration`, through the new `EnvelopeDecorator` SPI, implemented by
  `CollabEnvelopeDecorator`).
- **Investigations** (`InvestigationsController` rewritten, `InvestigationService`, `InvestigationEntity`): access
  checks on every path, `scope=mine|shared|all`, status validation, display ids everywhere, links to workspace,
  hypothesis set, question, chronicle and report, object items (evidence items included), TLP raise on a stricter
  item, TLP in exports. MCP `create_investigation` registers ownership and `add_to_investigation` needs editor.
- **Workspaces**: roles come from the 129 member list plus the ACL; share links need TLP `CLEAR`.
- **Tags** (`TagEntity`, `TagAssignmentEntity`, `TagService`, `api/TagsController`): personal and shared
  namespaces, bulk assign and unassign of up to 500 refs (unseen refs skipped and listed), tags in envelopes. Existing
  `kind=tag` annotations are migrated once at startup.
- **Comments** (`CommentService`, `AnnotationEntity` with `parent_id`, `mentions`, `resolved`, `edited_at`):
  replies, resolve and reopen, `thread=true` listing, `@handle` mentions notified only when the target can see the
  object (others returned as `droppedMentions`), reply notifications to earlier participants.
- **Uploads** (`AttachmentEntity`, `AttachmentService`, `AttachmentBlobStore`, `UploadTypes`, `ExifStripper`,
  `TextExtractor`, `api/AttachmentsController`): types sniffed from the bytes (PNG and JPEG via 043 ImageGuard, PDF,
  UTF-8 text, CSV, JSON, GeoJSON, KML, Markdown); 50 MB cap (`413 UPLOAD_TOO_LARGE`); `415 UPLOAD_TYPE_NOT_ALLOWED`;
  EXIF, XMP and IPTC stripped from JPEG and PNG unless `keep-exif`; text extraction (Apache PDFBox 3.0.8, jsoup for
  KML) capped at 1 MB; content-addressed store under the data directory; content served as attachment (inline only for
  images) with `nosniff`, a sandboxing CSP and an ETag; `409 ATTACHMENT_IN_USE` when cited.
- **Evidence items** (`EvidenceItemEntity`, `EvidenceItemService`): Admiralty grade (default `F6`), attachments,
  link metadata, cited source and objects, geometry, event time; TLP inherited from attachments; embedded only when
  `CLEAR` or `GREEN` (`DocInput.EVIDENCE_ITEM`, excluded from general search unless asked); originator
  `operator:{handle}` or the cited source; usable as ACH evidence (`source: evidence-item`) and investigation items.
- **Saved searches** (`SavedSearchEntity`, `SavedSearchService`, `api/SavedSearchesController`): text, types,
  filters, area, window and tags; results over visible envelopes; hourly-at-most notification of new matches, checked
  every 15 minutes.
- **Personal notifications** (`PersonalNotifier`, `PersonalInbox`, `MeEvents`, `MeTopic`, `MeFilter`,
  `NotificationPreferenceService`, `PrincipalNotificationEntity`, `InboxItemEntity`): `GET /v1/inbox`, the SSE topic
  `me` (`share.received`, `mention`, `comment.reply`, `saved-search.matches`; cursor key `p`), and
  `PUT /v1/me/notification-preferences`, whose items own personal 018 channels (`owner_principal_id`, events
  `["personal"]`) delivered by `NotificationService.publishPersonal` after commit. Broadcast fan-out and the
  `notifications` topic skip personal channels and events.
- **Objects**: six new types (`principal`, `group`, `tag`, `attachment`, `evidence-item`, `saved-search`) with
  resolvers (`CollabObjectResolvers`), contract version `1.1`.
- **API, MCP, docs:**
  - `api/CollaborationController`, `TagsController`, `AttachmentsController`, `SavedSearchesController`; changes to
    `ObjectsController`, `WorkspacesController`, `HypothesisController`; `AccessPolicy` rows; 12 Problem codes.
  - MCP: `whoami`, `list_shared_with_me`, `tag_objects`, `comment` (read) and `share_object` (agent); `annotate`
    and `add_evidence` updated.
  - The spec marks 47 operations implemented and gains the investigation schemas, `ObjectAclInput.tlp`, the error
    codes and a changelog entry; `pending-operations.txt` loses 47 lines. The Postman generator and collection were
    updated.
  - `docs/Client-Contract.md` was replaced whole; the Agent Guide gains the `me` topic, five tool rows and
    "Collaboration, sharing and TLP"; the Operator Guide gains "People, sharing and TLP (prompt 130)".
- **Build:** `org.apache.pdfbox:pdfbox:3.0.8` (checked on Maven Central, Apache-2.0), with its notice in
  `THIRD_PARTY_NOTICES.md`.

## Files created / modified

**Created (main):**
- `collab/`: `AccessAction`, `AccessResolver`, `AclRole`, `AttachmentBlobStore`, `AttachmentEntity`, `AttachmentRepository`, `AttachmentService`, `CollabAudit`, `CollabEnvelopeDecorator`, `CollabInputs`, `CollabObjectResolvers`, `CollabProperties`, `CommentService`, `EvidenceItemEntity`, `EvidenceItemRepository`, `EvidenceItemService`, `ExifStripper`, `GroupEntity`, `GroupMemberEntity`, `GroupMemberRepository`, `GroupRepository`, `GroupService`, `InboxItemEntity`, `InboxItemRepository`, `MeEvents`, `MeFilter`, `MeService`, `MeTopic`, `NotificationPreferenceService`, `ObjectAclEntity`, `ObjectAclRepository`, `package-info`, `PersonalInbox`, `PersonalNotifier`, `PrincipalAdminService`, `PrincipalCallers`, `PrincipalNotificationEntity`, `PrincipalNotificationRepository`, `PrincipalViews`, `SavedSearchEntity`, `SavedSearchRepository`, `SavedSearchService`, `ShareableObjectEntity`, `ShareableObjectRepository`, `ShareableSource`, `ShareableSources`, `ShareableTypes`, `ShareService`, `TagAssignmentEntity`, `TagAssignmentRepository`, `TagEntity`, `TagRepository`, `TagService`, `TextExtractor`, `UploadTypes`, `Visibility`.
- `security/Tlp`, `security/TlpTarget`, `object/ObjectGrants`, `object/EnvelopeDecorator`.
- `api/{CollaborationController,TagsController,AttachmentsController,SavedSearchesController}`.
- `mcp/tools/{WhoamiTool,ListSharedWithMeTool,TagObjectsTool,CommentTool,ShareObjectTool}`.

**Modified (main):**
- `security/`: `PrincipalEntity`, `PrincipalService`, `TokenService`, `EgressPolicy`, `ApiTokenRepository`,
  `AccessPolicy`.
- `object/`: `ObjectTypes`, `ObjectActions`, `ObjectEnvelope`, `ObjectService`, `ObjectContractResolvers`,
  `WorkspaceAccess`, `WorkspaceService`, `WorkspaceTopic`, `WorkspaceEntity`, `AnnotationEntity`,
  `AnnotationRepository`, `AnnotationService`.
- `intel/InvestigationEntity`, `intel/InvestigationService`, `store/NotificationChannelEntity`,
  `notify/ChannelService`, `notify/NotificationService`, `stream/sse/NotificationFilter`, `search/DocInput`,
  `search/SearchService`, `hypothesis/HypothesisVocabulary`, `api/error/ErrorCodes`, the API controllers above and
  the MCP tools `AddEvidenceTool`, `AnnotateTool`, `CreateInvestigationTool`, `AddToInvestigationTool`.
- `application.yml` (`foresignal.collab`), test `application-test.yml`.

**Build:** `gradle/libs.versions.toml`, `build.gradle.kts`, `THIRD_PARTY_NOTICES.md`.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Client-Contract.md`, `docs/Agent-Guide.md`,
`docs/Operator-Guide.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−47).

**Tests:** the new classes below, plus the helper `collab/CollabItSupport`. Modified:
- `stream/sse/EventSchemaConformanceTest`: the four `me` events and their payload factories.
- `object/ObjectControllersIT`: workspace share is `403 TLP_VIOLATION` at the default `AMBER`, then succeeds after
  `PUT acl` sets `CLEAR`.

## Deviations from the prompt

1. **Central registry.** Owner, visibility and TLP live in one `shareable_object` table, not in columns on every
   entity, so later work products (131 chronicles, 132 reports) register a `ShareableSource` instead of adding columns.
2. **Lazy registration** of pre-130 objects, with defaults: investigations `instance` (their pre-130 behavior),
   workspaces from their members, shared tags `instance`, everything else `private`.
3. **Attachments use their own content-addressed store** (`mediaId` `sha256:{hex}`), not the 043 MediaStore, which
   is images-only and tied to record retention.
4. **Images are PNG and JPEG only**, because TIFF metadata cannot be stripped safely by byte parsing.
5. **Inbox read state.** The spec has no mark-read call, so listing `GET /v1/inbox` marks the returned items read.
6. **Personal channels** are ordinary 018 channel rows with `owner_principal_id` and events `["personal"]`.
7. **TLP interpretation.** Locally named principals are always allowed; group and instance grants under `AMBER` and
   `AMBER+STRICT` need everyone reached in the owner's organization, and principals with no organization count as
   one organization.
8. **Workspace share links need `CLEAR`**, so the default `AMBER` blocks them; `ObjectControllersIT` was changed
   to match.
9. **`tlp` is set through `PUT /v1/objects/{ref}/acl`** (`ObjectAclInput.tlp`, a spec extension).
10. **Evidence items are embedded only at `CLEAR` or `GREEN`**, so stricter text never reaches an embedding
    provider; general search excludes evidence-item documents unless asked.
11. **Saved searches** need text or tags.
12. **Personal tags are owner-only**, even for admin tokens.
13. **Workspaces shared through the ACL** appear in `/v1/shared-with-me`, not in `/v1/workspaces`.
14. **Annotation listing** keeps the 129 page shape rather than the spec's bare array.
15. **Analyst execution** still adds investigation items without ACL checks (it runs as the system).
16. **Edited by script, not rewritten by hand:** the spec as in every earlier prompt; the Operator Guide was read end
    to end in chunks and written whole by script with one section appended and eight sentences extended; the Agent
    Guide was edited by script; `docs/Client-Contract.md` was replaced whole.

## Issues found / open questions

**Owner decisions:**
- (a) the central registry and lazy defaults, especially investigations staying instance-wide (Deviations 1, 2);
- (b) the TLP interpretation for groups, instance and missing organizations (Deviation 7);
- (c) share links requiring `CLEAR` (Deviation 8);
- (d) the separate attachment store and PNG/JPEG-only images (Deviations 3, 4);
- (e) mark-read on listing (Deviation 5);
- (f) personal tags hidden from admins (Deviation 12);
- (g) analyst execution bypassing investigation ACLs (Deviation 15).

**Known limits:**
- A disabled principal's tokens may work for up to 30 seconds on another instance (the cache is per instance; updates
  clear it locally).
- Saved-search notification is at most hourly per search.

**Schema via Hibernate, for prompt 144:**
- New tables: `shareable_object`, `object_acl`, `principal_group`, `group_member`, `principal_notification`,
  `principal_inbox`, `tag`, `tag_assignment`, `attachment`, `evidence_item`, `saved_search`.
- New columns: `principal` (`email`, `avatar_media_id`, `timezone`, `organization`); `annotation` (`parent_id`,
  `mentions`, `resolved`, `edited_at`, with an index on `parent_id`); `investigation` (`workspace_id`,
  `hypothesis_set_id`, `question_id`, `chronicle_id`, `report_id`); `notification_channel`
  (`owner_principal_id`).
- New properties: `foresignal.collab.default-tlp`, `keep-exif`, `max-upload-mb`, `saved-search-scheduled`.

**Guards not run:** the US English guard, `lintOpenApi` and every contract test (`AccessPolicySyncTest`,
`OpenApiContractIT`, `EventSchemaConformanceTest`, `SkillFrontmatterTest`) were not run (no-tests directive). New
prose and code were checked by grep for British spellings; "catalogued" was corrected to "cataloged", and the existing
wire name `licence` was left alone.

**Risks for the owner's first test run:**
- `AttachmentIT` builds JPEG and PNG fixtures by hand, with EXIF and tEXt chunks, and a PDF with PDFBox.
- `SavedSearchIT` calls `notifyDue` directly, because scheduling is off in tests.
- `ObjectControllersIT` workspace share now expects the TLP step.

**External data:** the PDFBox version and license were checked on Maven Central as data only. No file, page or tool
output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `collab/AccessResolverTest` | Roles from owner, principal, group and instance grants; expiry and private; invisible rather than forbidden; admin override; workspace members; notes follow their target; batch roles |
| `collab/TlpPolicyTest` | Every TLP label against every target, organization boundaries, strictest-label inheritance |
| `collab/CollaborationEgressTest` | Invisible work products dropped from egress; editors may share |
| `collab/CollabUnitsTest` | Upload type sniffing, EXIF stripping, text extraction, mention parsing, cursors |
| `collab/PrincipalMigrationIT` | Every token has a principal; the bootstrap token belongs to `owner`; `GET /v1/me`; disabling stops tokens |
| `collab/TagIT` | Personal and shared tags, bulk assign with skipped refs, envelopes, annotation migration |
| `collab/CommentThreadIT` | Replies, resolve and reopen, threads, reply notifications; mentions on data objects notified or dropped |
| `collab/AttachmentIT` | PDF text, EXIF removal, size and type limits, serving headers, dedup and visibility, in-use delete |
| `collab/EvidenceItemIT` | Grades, TLP inheritance, embedding rule, ACH and investigation use, originator |
| `collab/InvestigationSharingIT` | Owners, members and statuses; display ids; private objects and listings; ACL, ownership transfer, TLP and audit |
| `collab/SavedSearchIT` | Execution over visible envelopes, notification of new matches, hourly limit |
| `collab/CollaborationControllersIT` | Principal and group endpoints, preferences and inbox, required tokens, the shapes of the MCP tools |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **131 (chronicles) and 132 (reports):** register a `ShareableSource` bean in `ShareableSources`, check access with
  `AccessResolver.require`, carry TLP with `EgressPolicy.tlp` and inherit the strictest input label; the
  investigation's `chronicle_id` and `report_id` columns are ready.
- **141 (SDK):** wrap the `me` topic, the inbox and `403 TLP_VIOLATION` with its `rule`.
- **144 (Flyway):** capture the eleven tables and the new columns above.
- **Tests:** create people with `CollabItSupport.person(handle, scope, org)`.

Implementation commit: `31793a16041fb0e3c50166bba9db5343c9ad2558`
