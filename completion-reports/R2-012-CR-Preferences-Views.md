# R2-012-CR-Preferences-Views

| | |
|---|---|
| Prompt | R2-012-Preferences-Views.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `943ef0f8ee3207b9385ebeba8bf86856c61b80f3` |
| Date | 2026-10-26 |

## What was added

- **`V19__user_preferences.sql`** (V18 was the highest migration on disk):
  - A new table, `user_preferences (user_id)`, holding `locale`, `theme` and a `ui` jsonb bag. Its foreign key to
    `"user"` is `on delete cascade`.
  - `saved_view.owner_principal_id` (null = shared; every existing view stays shared), with a partial index.
  - The `workspace` table is unchanged (see the reconciliation below).
- **Preferences** (new package `preference/`):
  - `UserPreferencesEntity` and `UserPreferencesRepository`.
  - `UserPreferencesService`:
    - get and replace;
    - validation: timezone must be in `ZoneId.getAvailableZoneIds()`; locale must be well-formed BCP 47 naming an ISO
      639 language, and is stored canonical; theme must be `system`, `light` or `dark`; `ui` must be an object of at
      most 64 KiB; unknown members are refused;
    - reads fill in defaults: the server timezone, theme `system`, and `notify.defaultLanguage` for the locale.
  - `UserZones`: the user's own zone, else the server's, else UTC.
  - `UserPreferencesReassigner`, audit name `preferences`; it removes the row in both modes.
  - `api/PreferencesController` serves `getMyPreferences` and `updateMyPreferences`. It needs a session; an API token
    gets 401.
- **Notification alignment:**
  - New `ChannelService.adjustPersonal`, which changes the enabled flag, quiet hours or timezone of one personal channel
    owned by the caller.
  - `UserPreferences.notifications` is computed from, and written to, `principal_notification` and its channels.
- **Saved views:**
  - `SavedViewEntity.ownerPrincipalId`.
  - `view/SavedViewScope`: a session sees its own and the shared views and manages its own; `users.manage` and API
    tokens see and manage all.
  - `api/support/SavedViewOwnershipInterceptor` covers `/v1/views/{id or slug}` and everything under it. Another user's
    view is `404 UNKNOWN_VIEW`; a shared view is read-only for a user (`403 PERMISSION_DENIED`, `requiredPermission`
    `users.manage`).
  - `SavedViewService` gains `list(…, scope)`, `create(body, owner)`, `allShared`, `reassignOwner` and `deleteOwned`.
  - `SavedViewMapper` adds `shared` and `ownerPrincipalId`; both are accepted, and ignored, as output-only on input.
  - `SavedViewReassigner`, audit name `savedViews`.
  - The settings export carries shared views only.
- **Workspaces:**
  - `collab/WorkspaceReassigner`, audit name `workspaces`. Reassigning sets the target admin's principal as owner on
    `workspace.owner` and on the 130 `shareable_object` record, and keeps members and grants. Deleting removes the
    workspaces.
  - `WorkspaceRepository.findByOwnerOrderByIdAsc`.
- **Per-user timezone in rendering:**
  - `AskService` writes "now" in the asking user's zone (`UserZones`) instead of always the server zone.
  - Changing the timezone moves the user's personal channels to it, so their quiet hours render in it.

## Contract

- **Permissions:**
  - `self.views` is now on the 9 saved-view operations. The 6 reads (`listViews`, `getView`, `getViewRecords`,
    `getViewTile`, `getViewTileJson`, `streamView`) also accept `["data.read"]` as alternatives.
  - `self.workspaces` is now on the 10 workspace operations. All but `createWorkspace` and `shareWorkspace` also accept
    `["data.read"]`.
  - Scopes are unchanged.
  - `AccessPermissions` was regenerated: 900 rows, 526 with a permission (was 507).
- **Descriptions:** the planned "v2.0 (per-user)" paragraphs on the view operations were rewritten to what is built; a
  v2.0 paragraph was added to each workspace operation; `getMyPreferences` and `updateMyPreferences` now describe
  their defaults and semantics.
- **Schema:** `SavedView` gains read-only `shared` and `ownerPrincipalId` (additive).
- **Status:** `getMyPreferences` and `updateMyPreferences` stay `x-foresignal-status: planned`. Nothing in the
  changelog history or the frozen v1.0.0 spec was touched.
- **Postman** was regenerated. `scripts/generate-postman.py` gains an `updateMyPreferences` example body; before this,
  the generated body was `{}`.
- **Docs:**
  - `docs/RBAC-Design.md`: the "What exists today" table, the C2 counts and table, and a new "Built (R2-012)" section
    in §D.
  - `docs/Invariants.md`: RBAC-17.
- **Tests written, not run:**
  - `UserPreferencesIT`: defaults; the stored bag after an update; replace semantics; validation 400s; the
    `self.preferences` 403; the token 401; notifications read and write the same rows as
    `/v1/me/notification-preferences`; the timezone moves personal channels while stored instants stay unchanged;
    deletion.
  - `SavedViewOwnershipIT`: own and shared listing; another user's view is 404 by id and by slug, including on records
    and TileJSON; a shared view is 403 for a user; slugs stay global; the `self.views` / `data.read` gates; reassign and
    delete on user deletion; the export carries shared views only.
  - `WorkspaceOwnershipIT`: the owner is the user's principal; a Viewer member opens a shared workspace; a non-member
    gets 404; `self.workspaces` gates create; reassign keeps members; delete.
  - Unit tests: `UserPreferencesServiceTest`, `UserZonesTest` and `SavedViewScopeTest`.

## Decisions and disagreements for the owner

1. **The prompt and the code disagree on where the timezone is stored.** The prompt lists `timezone` as a
   `user_preferences` column. The code already had `principal.timezone`: `patchPrincipal` sets it, and
   `NotificationPreferenceService` copies it into each personal channel. A second column would mean two timezones per
   user, so `timezone` reads and writes the principal's column, and no `user_preferences.timezone` exists. Which should
   change?
2. **The notification switches have no store of their own.** `emailEnabled` and `pushEnabled` switch every email or push
   channel among the user's prompt 130 notification preferences, and `quietHours` writes one window onto each of them.
   - Reads are derived. A switch is false only when the user has channels of that kind and all are off. `quietHours`
     is null when the windows differ.
   - Consequence: a user with no notification preferences can "turn email off" and nothing is stored, so the next read
     says true again.
   - The alternative is a stored per-user default, which is the second store the prompt forbids.
   - Which channel types count as "push" (ntfy, gotify, pushover, webpush) is my choice; Slack, Matrix and webhooks are
     in neither switch.
3. **`updateMyPreferences` is a replace, except `notifications`.** `timezone`, `locale`, `theme` and `ui` are replaced:
   absent or null means the default. A missing `notifications` member, or a missing switch inside it, leaves the
   channels as they are. The alternative would re-enable every channel the user had turned off.
4. **GET returns effective values, so echoing a GET back pins the defaults.** Timezone, locale and theme come back
   filled in, as the prompt asks. A client that PUTs back what it GOT therefore stores the server timezone as the
   user's own, and later server timezone changes no longer follow. The schema's own wording ("falls back to the server
   setting when null") reads as if GET should return null for an unset timezone.
5. **Who manages a shared saved view.** No permission names this. I used `users.manage` (admins) and API tokens. The
   planned spec text said "an operator may also manage shared/global entries"; an Operator holds neither `users.manage`
   nor `self.views`. If Operators should manage shared views, a permission or an alternative is needed.
6. **Saved-view slugs stay globally unique.** A 409 on create therefore reveals that someone else's view holds the slug.
   This is the same disclosure as the shared LLM provider id namespace (R2-011).
7. **Only the REST surface is scoped.** Briefings, chronicles, share links, workspaces' `savedViewIds` and the MCP view
   tools resolve a view by id without owner narrowing; the MCP tools run as tokens. A user can still put another user's
   view id into a workspace or briefing they own. R2-013 (data access) may want to close that.
8. **Workspace ownership needed no change.** `workspace.owner` is `ObjectCallers.principalOf(token)`, and a session's
   token view carries the user's principal, so it was already the user's. The column is reused with no backfill.
   Existing workspaces created by API tokens keep `token:<id>` or the token's principal as owner.
9. **Workspaces cannot become shared when no admin exists.** `workspace.owner` is `not null`, so with no admin account a
   reassigned workspace stays with the deleted user's principal and is counted 0. An admin token can still transfer or
   delete it.
10. **A Viewer can no longer create a workspace.** `createWorkspace` and `shareWorkspace` require `self.workspaces`, and
    the Viewer role does not hold it. Before this, a Viewer session could create workspaces on its derived `read`
    scope. Everything else on a workspace also accepts `data.read`, so sharing with Viewers and Auditors still works.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (526 with a permission)".
- The static access check (`emulate.py`) reported "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- V19 is taken; the next migration is V20.
- `UserOwnedConfigReassigner` implementations now: `triggerRules`, `channels`, `sourceViews`, `llmProviders`,
  `preferences`, `savedViews`, `workspaces`.
- `UserZones.zoneFor(principalId)` is the per-user rendering zone. Use it wherever a user-facing rendering consults the
  server timezone. It is not yet used by briefings, `TripWatch` or `PolEngine`, which run for the machine.
- R2-013 should decide items 7 (id-based view references) and 5 (who manages shared views).
