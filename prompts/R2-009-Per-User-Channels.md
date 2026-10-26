# R2-009 — Per-user channels

**Goal.** Give users their own notification channels and let operators run **global** channels shared across the
instance. A user's alerts (R2-008) deliver to that user's channels plus any global channels an operator exposes.
Self-service channels start at Editor (`self.channels`); global channels are operator-managed (`channels.global`).

**Dependency.** R2-001 (users ↔ principals), R2-003/R2-004 (session principal + `self.channels`/`channels.global`
gates), R2-008 (alert ownership — channels are the delivery target). R2-005's reassigner registers the channel table.

**No new operations.** The existing channel operations gain owner semantics. Add
`x-foresignal-permission: self.channels` to channel write ops for self-service, and route **global** channel
management under `channels.global` (an operator capability). Confirm each channel op's current `x-foresignal-scope`
in `docs/OpenAPI.json` and layer the permission additively without changing scopes.

**Read first.**
- `src/main/resources/db/migration/V1__baseline.sql` `create table notification_channel` (~L481) — **it already
  has `owner_principal_id varchar(64)`.** So channel ownership largely exists; this prompt makes it mean "the owning
  **user**" (via the user's principal, per R2-001/R2-008's reconciliation) and adds the **global** distinction.
- `src/main/java/ai/foresignal/notify/` — `ChannelService`, `NotificationChannel`, `ChannelTypeRegistry`,
  `NotificationService`, how a channel is created/edited and how alerts resolve their channels for delivery.
- R2-008's alert→channel delivery path and owner convention.

**Build.**

- **Ownership meaning.** Treat `notification_channel.owner_principal_id` as the owning user's principal for channels
  created by a session (reuse it rather than adding a parallel `owner_user_id`; if the roadmap's `owner_user_id`
  phrasing conflicts with the existing column, the existing column wins — record the reconciliation). A session
  creating a channel stamps its own principal; update/delete enforce ownership (403/404 across users).
- **Global channels.** Add a `global boolean` (or a sentinel/null-owner convention — pick one and document) so an
  operator (`channels.global`) can create channels with **no personal owner** that are available instance-wide. A
  global channel is managed only by `channels.global` holders; a self-service user cannot edit or delete a global
  channel but their alerts may deliver through it when selected/allowed.
- **Delivery resolution.** When an alert fires (R2-008), the candidate channels are the **owner's own channels** plus
  the **global** channels the owner is permitted to use. Keep the one-pull/shared-stream posture: channels are a
  delivery concern, not a collection concern.
- **Scoping the channel API.** `ChannelService` lists/returns a session's own channels (and global channels,
  clearly marked) for `self.channels`; operators managing global channels use `channels.global`. An `agent`/`admin`
  api_token keeps its current behavior (machine path), with the token's principal as owner.
- **Reassignment.** R2-005's reassigner moves a deleted user's channels to an admin (or converts them to global —
  default: reassign to the admin, documented) so delivery for reassigned alerts keeps working.

**Model / migration.** Add **`Vn__channel_global.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive) for the `global` flag/index if a flag is used;
reuse `owner_principal_id` as-is for personal ownership (no new owner column). If no schema change is needed (e.g.
global is modeled as null-owner), add no migration and record that. Update the channel entity to validate.

**Contract / docs.** No new ops; add `x-foresignal-permission` (`self.channels` / `channels.global`) additively and
reflect owner/global semantics in the channel op descriptions and schema (expose `global` / `owner` where useful for
admins). Keep `AccessPolicy`/spec-sync in agreement. Regenerate Postman only — do **not** regenerate the client SDKs (R2-027 does that once at cutover) and leave every op's authored `x-foresignal-status`. Update `docs/RBAC-Design.md` (personal vs
global channels, who manages each, delivery resolution) and `docs/Invariants.md` (channels are owner-scoped; global
channels are operator-managed; delivery never crosses owners except through a global channel).

**Tests to write (do not run).** a session's created channel is owned by its principal; cross-user edit/delete →
403/404; an operator creates a global channel; a self-service user cannot edit/delete a global channel; an alert's
delivery set = owner's channels + permitted global channels; reassigning a deleted user's channels keeps delivery
working; machine tokens behave as before. Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** Channels are per-user (via the existing `owner_principal_id`) with operator-managed global channels;
delivery resolves to owner + permitted global channels; `self.channels`/`channels.global` gate the two surfaces;
owner/global semantics are in the spec and Postman; docs updated; `checkUsEnglish` green; compilation passes. The CR
records the ownership/global reconciliation.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-009: Per-user channels` + `completion-reports/R2-009-CR-Per-User-Channels.md`. **Stage only this
prompt's files.** End your reply with `Implementation commit: <hash>`.
