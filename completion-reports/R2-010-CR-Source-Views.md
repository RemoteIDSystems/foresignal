# R2-010-CR-Source-Views

| | |
|---|---|
| Prompt | R2-010-Source-Views.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `0749c3c4ee33dfd494bbbc1df6ccb7b4fefef2af` |
| Date | 2026-10-26 |

## What was added

- **`V17__user_source_pref.sql`** (V16 was the highest migration on disk). It adds `user_source_pref (user_id uuid,
  source_id varchar(64), enabled, display_name varchar(200), color varchar(7), updated_at)` with primary key
  `(user_id, source_id)`, a foreign key to `"user"` with `on delete cascade`, and the partial index
  `user_source_pref_disabled_idx (source_id) where not enabled` for the collector's query. The primary key's leading
  column serves the per-user lookups, so no separate `(user_id)` index was added. `source_settings` needed no new
  column. The entity is `source/view/UserSourcePrefEntity` (`@IdClass`, like `UserRoleEntity`).
- **The union pull gate.** In `collect/CollectorScheduler`, `desired()` now runs `DESIRED_SQL`:

  ```sql
  select s.source_id, s.interval_seconds, h.last_attempt_at from source_settings s
    left join source_health h on h.source_id = s.source_id
   where s.enabled and <CollectionUnion.WANTED_SQL>
  ```

  `CollectionUnion.WANTED_SQL` is true when no user is `active`, or when some `active` user has no
  `user_source_pref` row with `enabled = false` for the source. Every polling and streaming schedule decision comes
  from this one query, so the gate covers both. One row per `source_id` means one schedule entry and one pull, however
  many users want the source. The scheduler also listens for `SourceViewsChangedEvent` and calls `reconcileNow()`; the
  2-second reconcile tick catches user status changes.
- **`source/view/UserSourceViewService`** has four parts:
  - **Views:** `view` and `replace` serve `getMySources`/`updateMySources`.
  - **Instance set:** `collectionSet` and `replaceCollection` serve `getCollectionSources`/`updateCollectionSources`.
    `replaceCollection` is all or nothing and calls `SourceService.update(id, enabled(x))` only for sources whose state
    changes.
  - **Narrowing:** `narrow` is the `SessionReadNarrowing` hook, and `hiddenFor` keeps a per-user cache that is dropped
    on `SourceViewsChangedEvent`.
  - **Deletion:** `deleteView`.
- **`security/user/SessionReadNarrowing`** is a new interface. `TokenFilter.accept` applies every implementation to
  a session's restriction, after the R2-008 alert audience. Because of the interface, `security` does not depend on
  `source.view`.
- **`ReadRestriction.withSources`** narrows the existing source member, the one a restricted token's `sources` fill.
  That member is part of `canonical()`, so a user's cached responses and ETags are never shared with another view.
- **`source/view/UserSourcePrefReassigner`** has the audit name `sourceViews` and removes a deleted user's view in both
  modes.
- **`api/SourceViewsController`** serves the four operations.
- **Tests (written, not run):**
  - `collect/CollectionUnionIT` runs the exact `DESIRED_SQL`. It covers six cases:
    - with no users, the instance set is collected;
    - untouched views keep everything;
    - the union: a source stays while anyone wants it and goes when nobody does;
    - a source outside the instance set is never pulled;
    - a source wanted by many users is pulled once;
    - suspended users do not count.
  - `source/view/SourceViewsIT` covers:
    - the default view and the round trip;
    - five invalid bodies;
    - 401 for a token on the per-user operations;
    - the `self.sources`/`collection.manage` gates;
    - the collection set, including `enabledByUsers` counts and an invalid set changing nothing;
    - record view filtering (the user, another user, an admin token, an Operator);
    - deletion.
  - `source/view/CollectionUnionTest` is a unit test.
- **Docs:**
  - Architecture §5.1 has a new bullet, and the new §5.6 covers shared collection and per-user views.
  - RBAC-Design: "What exists today" gains a row, and §D gains "Built (R2-010)".
  - Invariants gains RBAC-15.

## Contract

- The four operations keep their authored schemas, permissions and `x-foresignal-status: planned`. No schema changed,
  so `AccessPermissions` did not change. The static check of the 900 rules against the spec reported 0 problems.
- The four descriptions were rewritten to state the opt-out default, the 401 for a token, and the "unlisted source
  keeps its state" rule.
- `updateCollectionSources` gained a `409` response (`CREDENTIALS_REQUIRED`), which `SourceService.update` already
  throws.
- Postman was regenerated (936 requests, all operations covered). SDKs were not regenerated, per the run sequence.

## Decisions and disagreements for the owner

1. **Views are opt-out, which differs from RBAC-Design's wording.** RBAC-Design says a source is pulled when "any
   user's view enables it (`user_source_pref.enabled`)". I made a missing row mean "enabled". Under the literal
   opt-in reading, creating the first user would stop all collection until that user listed every source, on the one
   PROD instance. Under opt-out, existing instances collect what they did before, and a source drops out only when
   every active user turns it off. A consequence: Viewers, Auditors and Operators lack `self.sources`, so they cannot
   edit a view, and while any of them is active the whole instance set stays collected. **Please decide whether a user
   without `self.sources` should count toward the union.**
2. **Only `active` users count.** Invited, suspended and disabled users neither keep a source collected nor stop the
   no-user fallback.
3. **Who is not filtered by a view.** A session holding `users.manage` or `collection.manage`, and every API token,
   reads the whole collection. My reasons:
   - A hidden source becomes invisible across `SourceVisibility`, so an Operator who hid a source would otherwise get
     `404` on that source's admin operations.
   - The prompt says "admin/global reads are unaffected".

   These users' views still count toward the union, and their labels and colors are still returned.
4. **`updateCollectionSources` is a partial replace.** Only the listed sources change, and the change is all or
   nothing. I did not read "replace the set" as "disable everything not listed": one short body would then stop every
   other source.
5. **`getMySources`/`updateMySources` with an API token return `401`.** This follows `AuthController`'s
   session-only operations. A token has no view.
6. **Deleted users' views are removed, not reassigned.** Moving them onto the admin would overwrite the admin's own
   view.
7. **A preference for a source outside the instance set is accepted and stored.** This is the prompt's recommendation.
   A source the caller cannot see (private, without `private:read`) is rejected as unregistered.
8. **Manual fetches and probes ignore views.** They are operator actions. `FetchExecutor`'s DISCARDED check still
   looks at `source_settings.enabled` only.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew checkUsEnglish`: green.
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- R2-011 and R2-012 add more owned configuration. `OwnerScope` and `ChannelScope` remain the scope helpers.
  `SessionReadNarrowing` is the hook if a later per-user choice must narrow reads. R2-013 completes data-access gating
  and can build on the narrowed source member.
- The next free migration is V18.
