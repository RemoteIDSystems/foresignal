# R2-009-CR-Per-User-Channels

| | |
|---|---|
| Prompt | R2-009-Per-User-Channels.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `a64e3ffb9a888e9c9ca560781127dc2c8e48d885` |
| Date | 2026-10-26 |

## What was added

- **Ownership/global reconciliation: no migration.** `notification_channel.owner_principal_id` (prompt 130's column)
  is the owning user's principal for every channel a session creates; **null = global** (the instance-wide channels,
  which every pre-R2-009 channel is, prompt 130's personal notice channels aside). No `global` column: the API's
  `global` and `ownerPrincipalId` are derived from the owner. The roadmap's `owner_user_id` wording yields to the
  existing column, as the prompt says.
- **`notify/ChannelScope`** — a session sees its own and the global channels; manages its own, and global ones only
  with `channels.global`; a session with `users.manage` and every API token see and manage all. On create,
  `ChannelInput.global` picks the kind (needs `channels.global` / `self.channels`); absent, a `self.channels` holder
  gets a personal channel and anyone else a global one; an API token always creates global channels.
- **`api/support/ChannelOwnershipInterceptor`** — every `/v1/channels/{channelId}…` operation: another user's channel
  `404`; a global channel changed, tested or deleted without `channels.global` `403 PERMISSION_DENIED`
  (`requiredPermission: channels.global`).
- **Permission alternatives** — `x-foresignal-permission-alternatives` (new, additive contract extension): further
  keys any one of which also satisfies an operation. Generator, `AccessPermissions.PermissionRule.alternatives`,
  `AccessPolicy.acceptedPermissions`, `TokenFilter` (any held key passes; the refusal names the primary key).
- `ChannelService.list(ChannelScope)`, `create(body, owner)`, `View.ownerPrincipalId`; `ChannelMapper` writes
  `ownerPrincipalId` and `global`; `ChannelInputParser` accepts `global`.
- **Delivery** — `NotificationService.publishOwned` now also enqueues a user's alert on global channels whose `ruleIds`
  name the rule. A global channel without `ruleIds` never takes a user's alert.
- **`notify/ChannelReassigner`** (`channels`) — reassign moves channels to the target admin; with no target they
  become global **and disabled**; `reassign=false` deletes them like `deleteChannel`.
- Tests (written, not run): `PerUserChannelsIT`, `ChannelScopeTest`, `ChannelOwnershipInterceptorTest`, a
  selected-global case in `OwnedAlertDeliveryIT`, `AccessPolicySyncTest.everyRouteAcceptsItsAlternatives`.
- Docs: RBAC-Design §C2 (alternatives, counts 507, the enforcement table) and §D "Built (R2-009)"; Invariants
  RBAC-14.

## Contract

- The 11 channel operations: `x-foresignal-permission` `channels.global` → `self.channels`, plus
  `x-foresignal-permission-alternatives: ["channels.global"]`. Scopes unchanged (`admin`, so API tokens are as before).
- The 5 escalation-policy operations gain `x-foresignal-permission: channels.global` (they had none).
- `Channel` gains read-only `ownerPrincipalId` and `global`; `ChannelInput` gains `global` (create only). The authored
  "v2.0 (per-user)" paragraph on the five channel CRUD operations replaced with what was built; `info.description`'s
  Permissions paragraph names the alternatives extension. Statuses untouched.
- `AccessPermissions` regenerated (900 rows, 507 with a permission). Postman regenerated ("936 requests … covers all
  ops: True"). Static policy emulation: 0 problems.

## Decisions and disagreements for the owner

1. **A new contract extension, `x-foresignal-permission-alternatives`.** The prompt asks for `self.channels` on the
   channel writes with global management under `channels.global`, but one operation serves both and a session is gated
   on one key: with `self.channels` alone the Operator (who holds only `channels.global`) would lose the channel API.
   Alternatives keep both roles working with the authored operations. The other option is separate global-channel
   operations, which the prompt rules out ("no new operations").
2. **API tokens create global channels, not channels owned by the token's principal** (the prompt says owned). Same
   reason as R2-008: an owned channel takes only its owner's alerts, so every machine-made channel would have stopped
   receiving instance alerts.
3. **"Global channels the owner is permitted to use" = global channels whose `ruleIds` name the user's rule.** The
   operator opts a global channel in per rule; a global channel with no `ruleIds` (today's default) takes shared alerts
   only. Is a per-rule opt-in what you want, or should a user be able to pick global channels themselves?
4. **Escalation policies now need `channels.global`.** They had no permission, so an Editor (derived scope `agent`)
   could create one naming another user's channel. Not asked for by the prompt; it closes that route.
5. **Orphaned channels are disabled** when reassigned with no admin to receive them, so they do not start receiving
   every shared alert as new global channels.
6. A user sees global channels' configuration (never secrets), as the prompt's "clearly marked" listing implies.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): personal/global kinds by caller; explicit `global` needs its permission (and a non-boolean
  is 400); listings per user, Operator and token; another user's channel `404` on get/put/test/deliveries/delete;
  global channel readable but `403 channels.global` to change for a user, manageable by an Operator, kind kept on
  replace; Viewer `PERMISSION_DENIED self.channels`; escalation policies need `channels.global`; reassign to admin;
  orphaned → global and disabled; `reassign=false` deletes; selected global channel receives a user's alert, others
  not; alternatives sync check; `ChannelScope` and interceptor unit tests.

## Notes for the next prompt

- `x-foresignal-permission-alternatives` is available for any operation that serves a self-service and a platform
  permission (LLM providers in R2-011: `self.llm` / `llm.shared.manage`).
- `OwnerScope` (R2-008) and `ChannelScope` are the two scope helpers; a new per-user table should follow one of them.
