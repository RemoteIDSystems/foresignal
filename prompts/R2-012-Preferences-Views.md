# R2-012 — Per-user preferences, saved views, workspace ownership

**Goal.** Give each user their own preferences (timezone, locale, theme, notification + UI prefs) and align the
existing per-owner surfaces — saved views and workspaces — to the signed-in **user**, so a user's personalization is
their own. `getMyPreferences`/`updateMyPreferences` expose the preference bag; saved views and workspaces gain clear
user ownership under `self.views` / `self.workspaces`.

**Dependency.** R2-001 (users ↔ principals), R2-003/R2-004 (session + `self.preferences` / `self.views` /
`self.workspaces` gates). R2-005's reassigner registers the preferences/views/workspaces for reassign-on-delete.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:** `getMyPreferences`, `updateMyPreferences` —
`scope: read`, `x-foresignal-permission: self.preferences`. Schema
`UserPreferences{timezone, locale, theme, notifications, ui}` (same schema for request and response). The existing
saved-view and workspace operations gain user-ownership semantics; add `x-foresignal-permission`
(`self.views` / `self.workspaces`) additively — confirm current scopes and layer without changing them.

**Read first.**
- `docs/OpenAPI.json` — `getMyPreferences`/`updateMyPreferences` + `UserPreferences`, and the existing saved-view /
  workspace operations and schemas.
- `src/main/resources/db/migration/V1__baseline.sql` `create table saved_view` (~L4756) and `create table workspace`
  (~L3969, it has `owner varchar(200)`) — the existing per-owner surfaces to align to users.
- `src/main/java/ai/foresignal/view/`, `src/main/java/ai/foresignal/collab/`, `src/main/java/ai/foresignal/object/`
  — saved-view and workspace services and how `owner` is currently set (today it is a principal/handle string).
- `src/main/java/ai/foresignal/api/CollaborationController.java` (`getMe`, `listMyNotificationPreferences`,
  `updateMyNotificationPreferences`) and `src/main/java/ai/foresignal/settings/NotificationSettings*` — the existing
  notification-preference surface, which `UserPreferences.notifications` should align with (one source of truth, not
  two divergent stores).

**Build.**

- **`user_preferences` / `UserPreferencesEntity`.** Per-user preference bag keyed by `user_id`: `timezone`,
  `locale`, `theme`, plus `notifications` and `ui` as JSON blobs matching the `UserPreferences` schema. `getMyPreferences`
  returns the caller's prefs (with documented defaults when unset — e.g. timezone from `ServerSettings.timezone`,
  theme `system`); `updateMyPreferences` upserts them. Validate timezone against the JVM zone set; reject unknown
  themes/locales per the schema's allowed values.
- **Notification preferences alignment.** `UserPreferences.notifications` must not fork from the existing
  notification-preference store the `getMe`/`listMyNotificationPreferences` surface already uses. Make the preferences
  bag the per-user home for these, reading/writing the same underlying store (reconcile the existing principal-keyed
  notification prefs to the user, consistent with R2-008/R2-009 ownership). Record the reconciliation.
- **Saved views → user ownership.** Saved views are owned by the signed-in user (via the existing `owner` field,
  keyed to the user's principal per the R2-008 reconciliation). A session lists/creates/edits/deletes **its own**
  saved views under `self.views`; cross-user access is refused (403/404). Admin visibility is a platform concern,
  documented.
- **Workspace ownership alignment.** Align `workspace.owner` to the signed-in user (the user's principal), so a
  user's workspaces are their own under `self.workspaces`. Keep existing sharing/collaboration semantics intact
  (workspaces may be shared; this prompt only ensures the **owner** is the user). Do not break the collab/sharing
  model (prompts 129–130); align ownership, do not rewrite sharing.
- **Timezone use.** A user's timezone preference drives their view of timestamps where the API renders per-user
  (e.g. their alert/notification formatting), without changing stored UTC. Wire it where a user-facing rendering
  already consults a timezone (server settings today); the per-user timezone overrides for that user.

**Model / migration.** Add **`Vn__user_preferences.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive) for `user_preferences`. If saved_view/workspace
need an explicit `owner_user_id` rather than reusing `owner`, add it in the same migration and backfill; otherwise
reuse `owner` and record that. Entities validate under `ddl-auto: validate`.

**Contract / docs.** Implement `getMyPreferences`/`updateMyPreferences` to their authored schema but **leave their `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only); add
`x-foresignal-permission` to saved-view/workspace ops additively. Keep `AccessPolicy`/spec-sync in agreement.
Regenerate Postman (body for `updateMyPreferences`). Update `docs/RBAC-Design.md` (per-user preferences, saved views,
workspace ownership, notification-preference alignment) and `docs/Invariants.md` (preferences/views/workspaces are
owner-scoped; stored times stay UTC; per-user timezone affects rendering only).

**Tests to write (do not run).** `getMyPreferences` returns defaults when unset and the stored bag after update;
`updateMyPreferences` validates timezone/theme/locale and rejects invalid values; `notifications` reads/writes the
same store as the existing notification-preference surface (no fork); a user sees only their own saved views and
cannot touch another user's (403/404); a user's workspaces are owned by them while sharing still works; the per-user
timezone affects that user's rendered timestamps but not stored UTC. Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** `getMyPreferences`/`updateMyPreferences` work and are gated by `self.preferences`; notification prefs
are unified (no fork); saved views and workspaces are user-owned under `self.views`/`self.workspaces` without breaking
sharing; per-user timezone renders per user over UTC storage; ops built to their authored schemas (status stays `planned` until R2-027); docs + Postman updated;
`checkUsEnglish` green; compilation passes. The CR records the ownership + notification-store reconciliation.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-012: Per-user preferences, saved views, workspace ownership` +
`completion-reports/R2-012-CR-Preferences-Views.md`. **Stage only this prompt's files.** End your reply with
`Implementation commit: <hash>`.
