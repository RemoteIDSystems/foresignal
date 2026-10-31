# SG-002-CR-Source-Groups

| | |
|---|---|
| Prompt | SG-002-Source-Groups.md |
| Status | Complete (compile only; `compileJava`/`compileTestJava` green; `checkUsEnglish` green; no test run) |
| Implementation commit | `d5237ade70460f87ef4a7216d5c320ef2f63040c` |
| Date | 2026-10-30 |

A `self.sources` user creates named **source groups** through `/v1/me/source-groups`, and a group scopes an edition
(`BriefingScope.sourceGroupIds`) and a trigger (`TriggerRuleInput.sourceGroupId`). The five operations remain
`planned` — `SG-004` activates them.

## What was added

- **`source_group` store** (new package `ai.foresignal.source.group`): migration `V35__source_group.sql` creates
  `source_group` (`id` text PK, `owner_user_id uuid not null` FK `"user"(id)` `on delete cascade`, `name`,
  `description`, `source_ids`/`kinds` jsonb, `correlation`/`notification_threshold` jsonb, `edition_id` FK
  `briefing(id)` `on delete set null`, timestamps; index `(owner_user_id, id)`). `SourceGroupEntity`,
  `SourceGroupRepository` (`findByIdAndOwnerUserId`, `findByOwnerUserIdOrderByCreatedAtAscIdAsc`,
  `deleteByOwnerUserId`), and `SourceGroupReassigner` (`UserOwnedConfigReassigner`, audit name `sourceGroups`,
  delete-on-user-delete like `sourceViews`).
- **`SourceGroupService`** — create/replace/get/list/delete, all keyed on `session.userId()`. Each member source must
  be a catalog source (`SourceRegistry.contains`) or a custom source the caller owns (SG-001's store); any other id —
  unknown, or a custom/external source the caller does not own — is `400`. The id is server-generated (`sg-<uuid>`),
  `ownerUserId` forced to the caller, a group the caller does not own `404`. `correlation`/`notificationThreshold` are
  persisted as given, defaulting per the schema (`balanced`/`analytical`/`medium`; `warning`/false/null).
- **`MySourceGroupsController`** (`@Tag("Sources")`) for the five ops, session/401 like `SourceViewsController`,
  404-on-not-owned like `EditionsController`; `listMySourceGroups` is cursor-paged (`SourceGroupPage`), the cursor is
  the last group id.
- **Edition scope** — `BriefingScope` gained `sourceGroupIds` (record field + a pre-SG-002 convenience constructor +
  `toMap`/`fromMap`); `BriefingValidator` parses it. `PlaceBriefingScopeResolver` (the `@Primary` resolver; injected a
  `SourceGroupLookup`) resolves each group to its member sources and **unions** them into the scope's sources. A
  group that is unknown, deleted, or resolves to no members makes the scope **match nothing** (the fail-closed
  `nothing(...)` filter), never widening to all sources — the same shape as a deleted place or view.
- **Trigger scope** — `TriggerRuleEntity.source_group_id` (nullable column, entity field). `RuleValidator` gained a
  `validate(body, ownerPrincipalId)` overload (the old `validate(body)` delegates with null); it reads `sourceGroupId`,
  requires the group exists and is owned by the rule's owner (else `400`), and lets a `sourceGroupId` alone satisfy the
  "≥1 selector" check. `ValidatedRule` carries `sourceGroupId` and sets it in `applyTo`. `TriggerRuleService.create`
  passes the creating principal, `replace` the existing rule's owner. `RuleSpecs.compile` expands `sourceGroupId` to
  the group's member sources and **unions** them into the spec's source set (so `RuleSqlBuilder`/`TriggerEngine` are
  unchanged); an empty or dangling group sets the set to a single sentinel id no record has
  (`RuleSpecs.NO_SOURCE_SENTINEL`), failing the rule closed rather than becoming the empty "match any" set.
- **`SourceGroupLookup`** (the bean injected into both engines): `memberSources(groupId)` by id for the edition
  resolver (a group id is globally unique), and `existsForPrincipal`/`memberSourcesForPrincipal(groupId,
  ownerPrincipalId)` for the trigger path, mapping the rule's `owner_principal_id` (string) to the group's
  `owner_user_id` (uuid) through `UserRepository.findByPrincipalId`.
- **Access rules (two places)** — five `/v1/me/source-groups*` rules in `AccessPolicy.rules()`
  (`READ_ALWAYS_AUTHENTICATED`, in the `/v1/me/*` block). `AccessPermissions.java` already carried the `self.sources`
  rows for these paths (SG-001's regeneration transcribes the whole spec), so re-running the generator produces no
  diff.

## Tests written (not run)

- `SourceGroupCrudIT` — a `self.sources` session creates a group (owner forced, member validated against an owned
  custom source, unknown member `400`), reads/updates/deletes it, lists it cursor-paged; another user's group is
  `404`; an API token is `401`.
- `SourceGroupEditionScopeIT` — `sourceGroupIds` resolve to the union of member sources; an unknown or empty group
  yields the fail-closed filter and does not widen `sources`.
- `TriggerSourceGroupScopeIT` — `sourceGroupId` expands to the group's members in the compiled `RuleSpec` and tracks
  membership edits without rewriting the rule; a group not owned by the rule owner is `400`; an empty group compiles
  to the fail-closed sentinel.

## Decisions / owner questions

- **Rule-owner ↔ group-owner identity mapping.** Rules key on `owner_principal_id` (a string principal) and groups on
  `owner_user_id` (a uuid). `SourceGroupLookup.memberSourcesForPrincipal` resolves the principal to the user
  (`UserRepository.findByPrincipalId`) and then matches the group's owner, so a rule can reference only its own
  owner's groups. A shared rule (null principal) owns no group, so `sourceGroupId` on an imported/shared rule
  validates to `400` / fails closed on compile. Confirm this is the intended coupling.
- **Union vs strict AND.** The contract describes `sourceGroupId` as "ANDed with sources/kinds" and
  `sourceGroupIds` as "member sources bound the scope", but the prompt's build steps say to **union** the member
  sources into the source set (so the SQL builder and trigger engine need no change). I implemented the union: the
  effective source set is the rule's/scope's own sources together with the group's members. If strict intersection
  ("only records from a source that is both named and in the group") is wanted, that is a change to `RuleSqlBuilder`
  and the resolver, not just an expansion. Flagging for confirmation.
- **Fail-closed representation in the trigger path.** An empty source set means "match any" in the engine, so a
  dangling/empty group cannot fail closed by emptying the set. The spec's source set is instead set to a single
  sentinel id (`sg:no-members`) that no record carries, so the SQL `IN (...)` matches nothing. The edition resolver
  fails closed with the existing empty-area `nothing(...)` filter.
- **`editionId`.** `SourceGroup.editionId` is surfaced from the row and set when a group is turned into its own
  edition; this prompt adds the column and the read-only view field. No `/v1/me/source-groups` operation sets it (the
  contract has none); the edition-from-group flow that stamps it is out of this prompt's five operations.

## Gate

- `./gradlew compileJava compileTestJava` — green (only pre-existing deprecation/unchecked notes).
- `./gradlew checkUsEnglish` — green.
- No test suite, `lintOpenApi`, `checkApiCompat` or SDK/Postman generation (compile-only gate; `SG-004` runs those).
