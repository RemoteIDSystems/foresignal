# 129 — Principals Groups Sharing And Collaboration

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/129-CR-Principals-Groups-Sharing-And-Collaboration.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/128-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Client-Contract.md` §2–§6 and the collaboration sections (principals, sharing, TLP, tags, comments, attachments, investigations)
- `docs/Intelligence-Design.md` §2 (principles 9, 12, 13), §12.2 (collaboration)
- Completion Reports of 018 (channels, delivery), 023 (tokens, scopes, principals per its v1.3 note, audit log, pairing codes), 027 (embeddings), 033 (inbox, read cursors, presence), 039 (`private`), 043 (media store, ImageGuard), 101 (investigations), 109 (reliability grades, signing key), 112 (share links), 113 (federation peers), 124 (ACH evidence), 128 (object references, annotations, workspaces)

## Objective

Make Foresignal work for several people, on one shared server or across separate instances, without becoming a user-directory product. People and agents are **principals** that hold scoped tokens. Principals form **groups**. Shareable objects (investigations, workspaces, chronicles, reports, saved searches, notes) have an owner, an access list and a **TLP** sensitivity label. Anyone can **tag** many objects at once, **comment** with @mentions, attach **documents and images** as graded evidence, and keep **saved searches**. Investigations gain owners, members, human-readable ids and links to everything an analyst works with. Notifications reach the right person.

## Principles (binding)

- **No passwords in the broker.** Principals authenticate with scoped tokens, device pairing codes (023) or, for MCP, the 135 RFC 9728 flow. Scopes still decide which operations a token may call; access lists only narrow access to individual shareable objects.
- **Data stays instance-wide; work products are shareable.** Records, findings, situations, forecasts and other analytic objects remain readable per scope (and `private` per 039). Access lists apply to work products: investigations, workspaces, chronicles (130), reports (131), saved searches, notes and attachments.
- **TLP travels with the object** and bounds every share, export and exchange.
- **Uploaded files are data, never code.** They are stored content-addressed, served as attachments with safe content types, and never executed or rendered as active content.

## Tasks

### 1. Principals and groups (`ai.foresignal.collab`)

- If 023 created `principal`, extend it; otherwise create it: `principal` (`id`, `handle` `^[a-z0-9][a-z0-9_.-]{1,31}$`, `display_name`, `kind` person | agent | service, `email` (optional, used only for this principal's email notifications), `avatar_media_id`, `timezone`, `organization` (free text, used for TLP AMBER boundaries), `disabled`, timestamps). Every token belongs to one principal; the bootstrap admin token belongs to principal `owner`. Existing tokens are migrated to one principal each.
- `GET /v1/me` (read; principal, scopes, groups, notification preferences), `GET /v1/principals` (read; handles, display names, kinds, avatars only), `POST/PATCH /v1/principals[/{id}]` (admin), `POST /v1/principals/{id}/tokens` (admin; issues a token for that principal with a scope ≤ admin's choice; 023 pairing codes work per principal).
- `group` (`id`, `name`, `description`, `created_by`), `group_member` (`group_id`, `principal_id`, `role` member | manager). `GET/POST/PATCH/DELETE /v1/groups[/{id}]`, `POST/DELETE /v1/groups/{id}/members` (managers or admin).
- **Notification preferences** per principal: `principal_notification` (`principal_id`, `event_types` (mention, share, report, chronicle, assessment, comment-reply), `channel` (a 018 channel definition owned by the principal: their own ntfy topic, email address, Matrix room, webhook…), `quiet_hours`). Deliveries go through 018's worker; the 033 inbox gains a per-principal view (`GET /v1/inbox` returns the caller's items).

### 2. Ownership, access lists, visibility

- Shareable object types (Client-Contract §10): `investigation`, `workspace`, `chronicle` (130), `report` (131), `saved-search`, `annotation` (notes), `attachment`, `evidence-item`, `tag` (shared tags). Each has `owner` (principal) and `visibility` (private | shared | instance).
- `object_acl` (`object_ref`, `grantee` (`principal:{id}` | `group:{id}` | `instance`), `role` viewer | commenter | editor | owner, `granted_by`, `granted_at`, `expires_at`). Owner and editors may share; only owners may delete or transfer ownership.
- `AccessResolver.can(principal, objectRef, action)` used by every shareable type's API, the 128 object resolver (unshared objects are invisible, not 403, to avoid leaking existence) and the `actions` list in envelopes.
- API: `GET /v1/objects/{ref}/acl` (owner, editor), `PUT /v1/objects/{ref}/acl` (owner, editor), `POST /v1/objects/{ref}/share {grantees[], role, message?}` (notifies grantees), `GET /v1/shared-with-me?type&cursor` (read), `GET /v1/my?type&cursor` (read; owned objects).
- Admin principals see everything; every share and ACL change is written to the 023 audit log.

### 3. TLP labels (Traffic Light Protocol 2.0)

- `tlp` on every shareable object and on annotations and attachments: `CLEAR | GREEN | AMBER | AMBER+STRICT | RED`; instance default `collab.defaultTlp` (AMBER). Objects created from others inherit the strictest TLP of their inputs (a report built from a RED note is RED).
- **`EgressPolicy` extension** (Conventions): TLP bounds who may receive an object outside its ACL:
  - `RED`: only principals named in the ACL; never `instance` visibility, share links (112) or exchange to other instances except to named recipients there (131);
  - `AMBER+STRICT`: principals of the same `organization` and named recipients;
  - `AMBER`: as AMBER+STRICT plus recipients' organizations on other instances;
  - `GREEN`: the instance and federated peers (113), no public links;
  - `CLEAR`: no restriction (public share links allowed).
- Envelopes carry `tlp`; generated renditions (exports, reports, notifications) print the label prominently.

### 4. Tags

- `tag` (`id`, `name`, `namespace` personal | shared, `color`, `owner`, `description`), `tag_assignment` (`tag_id`, `object_ref`, `assigned_by`, `assigned_at`). Personal tags are visible only to their owner; shared tags follow their ACL (default `instance`).
- `POST /v1/tags/{id}/assign {refs[≤ 500]}` and `/unassign` (bulk), `GET /v1/tags/{id}/objects?type&cursor` (envelopes), `GET /v1/objects/{ref}/tags`, CRUD `/v1/tags`.
- Envelopes (128) include the caller's visible tags. 128's annotation `kind = tag` entries are migrated to tag assignments, and the `tag` annotation kind is removed.

### 5. Comments and mentions

- Extend 128 annotations: `parent_id` (threads), `mentions` (principals parsed from `@handle`), `resolved` (bool, for threads), `edited_at`. Mentions notify the mentioned principals (if they can see the target; otherwise the mention is dropped with a warning to the author).
- `GET /v1/objects/{ref}/annotations?thread=true` returns threads; `POST /v1/annotations/{id}/replies`; `POST /v1/annotations/{id}/resolve`.

### 6. Attachments and user-contributed evidence

- `attachment` (`id`, `media_id` (043 content-addressed), `filename`, `content_type`, `size`, `sha256`, `text` (extracted: PDF via Apache PDFBox (Apache-2.0; verify the version), plain text, HTML stripped to text, ≤ 1 MB), `image_meta` (dimensions; EXIF stripped unless `collab.keepExif=true`), `uploaded_by`, `tlp`, `created_at`). Upload: `POST /v1/attachments` (multipart; read scope; limit `collab.maxUploadMb` 50; allowed types: images (through 043 ImageGuard), PDF, plain text, CSV, JSON, GeoJSON, KML, Markdown); served by `GET /v1/attachments/{id}/content` with `Content-Disposition: attachment` (inline only for images) and `X-Content-Type-Options: nosniff`.
- `evidence_item` (object type `evidence-item`): `id`, `title`, `description`, `attachment_ids`, `url` (link evidence; stored as metadata only, never fetched), `source_description`, **source reliability** `A–F` and **information credibility** `1–6` (Admiralty / NATO grading), `geom` (optional), `event_time`, `tags`, `tlp`, `created_by`. Embedded (027) with attachment text for search. Usable as a member of investigations and situations, as 124 ACH evidence (`source=evidence-item`), and as relation endpoints (128).
- Evidence items count as originator `operator:{principal}` for 116 independence unless they cite a catalogued source.

### 7. Investigations (extends 101)

- Add to `investigation`: `owner`, `status` (open | active | closed | archived), `tlp`, `summary`, `workspace_id` (128), `hypothesis_set_ids`, `question_ids`, `chronicle_ids` (130), `report_ids` (131), and ACL sharing. `GET /v1/investigations?scope=mine|shared|all&status` lists what the caller can see; `display_id` lookups work in every investigation path.
- Investigation items accept `evidence-item`, `attachment` and any 128 object reference.

### 8. Saved searches

- `saved_search` (`id`, `name`, `owner`, `query` jsonb (object types, text, filters, area, time window, tags), `visibility`, `tlp`, `notify` (bool: new matches go to the owner's inbox, at most hourly)). `GET /v1/saved-searches/{id}/results?cursor` runs it (027 hybrid search plus structured filters over envelopes). CRUD under `/v1/saved-searches`.

### 9. API, MCP, spec, docs

- MCP: `whoami`, `list_shared_with_me`, `tag_objects`, `comment` (read); `share_object` (agent).
- SSE: the caller's personal topic `me` on `/v1/events/stream`: `share.received`, `mention`, `comment.reply`, `saved-search.matches`.
- `docs/OpenAPI.json`: `Principal`, `PrincipalInput`, `Group`, `GroupMember`, `NotificationPreference`, `ObjectAcl`, `ShareRequest`, `Tlp`, `Tag`, `TagAssignment`, `AnnotationThread`, `Attachment`, `EvidenceItem`, `SavedSearch`, investigation additions; tags `Collaboration`; `x-foresignal-scope`; Postman entries with BODIES for share, tag assign, upload, evidence item, saved search.
- AccessPolicy rows as listed. Problem codes: `UNKNOWN_PRINCIPAL`, `UNKNOWN_GROUP`, `UNKNOWN_TAG`, `UNKNOWN_ATTACHMENT` (404); `TLP_VIOLATION` (403, with the rule that blocked the share); `UPLOAD_TOO_LARGE` (413); `UPLOAD_TYPE_NOT_ALLOWED` (415).
- Update `docs/Client-Contract.md` §10 (collaboration) with anything that differs from the design; `docs/Operator-Guide.md` "People, sharing and TLP": creating principals and tokens, groups, what is shareable, TLP rules, uploads and their limits.

## Tests to write (do not run)

- `PrincipalMigrationIT`: existing tokens get principals; bootstrap token → `owner`; `/v1/me`.
- `AccessResolverTest`: owner, editor, commenter, viewer, group and instance grants; expiry; invisible-not-forbidden for unshared objects; admin override.
- `TlpPolicyTest`: every TLP level against share targets (principal, group, instance, link, other instance); inheritance of the strictest label.
- `TagIT`: bulk assign up to 500; personal tags invisible to others; envelopes show visible tags; migration from 128 tag annotations.
- `CommentThreadIT`: replies, mentions notify only principals who can see the target, resolve.
- `AttachmentIT`: PDF text extraction, image EXIF stripping, size and type limits, safe serving headers, content-addressed dedup.
- `EvidenceItemIT`: Admiralty grades, ACH use, embedding, originator for 116.
- `InvestigationSharingIT`: owner, members and statuses; `display_id` lookups; shared-with-me listing.
- `SavedSearchIT`: results, hourly match notifications.
- `CollaborationControllersIT`: endpoints, scopes, audit entries, MCP shapes.

## Acceptance criteria

- Several named people can use one instance with their own tokens, groups, private and shared work, and notifications.
- Investigations, workspaces, chronicles, reports, saved searches, notes and attachments obey their access lists and TLP everywhere, including envelopes, search, SSE and exports.
- Many objects can be tagged at once; comments thread with mentions; documents and images become graded evidence.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `129: Principals Groups Sharing And Collaboration`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/129-CR-Principals-Groups-Sharing-And-Collaboration.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `129: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
