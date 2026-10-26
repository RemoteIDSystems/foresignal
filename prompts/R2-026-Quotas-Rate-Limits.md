# R2-026 — Per-user quotas & rate limits

**Context.** v2.0 (R2) backend, Part D. Depends on the identity model (R2-001: `user`), authorization
(R2-004), and the per-user alerts (R2-008), channels (R2-009) and saved views (R2-010/R2-012) whose creation
this prompt bounds. Admin manages a user's quota; a user reads their own. Now that the instance is multi-user,
one user must not be able to exhaust it.

**Goal.** Per-user **API rate limits** plus optional per-user **storage / alert / channel / saved-view**
quotas, enforced and surfaced.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-026`)

| operationId | method + path | scope |
|---|---|---|
| `getMyQuota` | `GET /v1/me/quota` | read (self) → `QuotaUsage` |
| `getUserQuota` | `GET /v1/users/{id}/quota` | admin · `users.manage` |
| `updateUserQuota` | `PUT /v1/users/{id}/quota` | admin · `users.manage` |

Schemas authored: `Quota` (`apiRateLimitPerMinute`, `maxStorageBytes`, `maxAlerts`, `maxChannels`,
`maxSavedViews`; null = unset/instance default), `QuotaUsage` (`quota` + current `usage`).

## Read first

- `notify/ChannelRateLimiter.java` and `chatops/ChatRateLimiter.java` (existing token-bucket rate-limit
  patterns to model the per-user API limiter on).
- `security/TokenFilter.java` + `security/AccessPolicy.java` + `security/AuthenticatedToken.java` (where a
  request resolves to a user principal — the enforcement point for the API rate limit), and the per-user
  ownership on alerts (R2-008), channels (R2-009) and saved views (R2-010/R2-012) for the count quotas.
- `settings/AppSettingService.java` (instance-default quota), the user entity/repository (R2-001).

## Build

1. **Quota store.** A `user_quota` row per user (or nullable columns on `user`): `api_rate_limit_per_minute`,
   `max_storage_bytes`, `max_alerts`, `max_channels`, `max_saved_views`; any null means "use the instance
   default." An instance-default `Quota` under an `app_setting` key. `QuotaService.effective(userId)` merges
   user quota over the instance default.

2. **API rate limit.** A per-user token-bucket limiter (model on `ChannelRateLimiter`) checked in `TokenFilter`/
   `AccessPolicy` for session- and user-token-resolved principals: over the per-minute limit → `429` with a
   `Retry-After`. Requests carrying only a machine token with its own `rpm` restriction keep that restriction;
   the user limit applies to the owning user's aggregate where a token is user-owned (R2-025).

3. **Count quotas.** Enforce `maxAlerts`/`maxChannels`/`maxSavedViews` at creation time on the self-service
   paths (`self.alerts`/`self.channels`/`self.views`): a create that would exceed the user's effective quota →
   `409 QUOTA_EXCEEDED` naming the limit. `maxStorageBytes` bounds the user's own stored data (workspaces,
   uploaded media) measured by `QuotaService`.

4. **Surface it.** `getMyQuota` returns `QuotaUsage` (the caller's effective quota + current usage: alert/
   channel/view counts, bytes used, recent API rate). `getUserQuota`/`updateUserQuota` let an admin read and set
   any user's quota (PUT replaces; validate non-negative; null clears to the instance default). Optionally raise
   an R2-014 operational alert when a user is sustained at a hard limit.

**Migration.** Add the next free additive Flyway migration `V{n}__user_quota.sql` (`n` one past the highest
`V*` now present — read the directory; R2-001…R2-013 claim V14 upward): the `user_quota` table (or columns).
Instance default lives in `app_setting`. Additive only.

## Contract

The three ops are authored `planned`; implement them to `Quota`/`QuotaUsage`. Add the `QUOTA_EXCEEDED` /
`429` error details to the spec if missing, regenerate Postman (`python3 scripts/generate-postman.py`), but
**leave `x-foresignal-status: planned` and do not regenerate the SDKs** (R2-027 flips + regenerates).
`docs/OpenAPI.json` is the only contract.

## Tests to write (do not run)

- `QuotaServiceTest` — user quota merges over the instance default; null falls through to the default.
- `ApiRateLimitIT` — a user over `apiRateLimitPerMinute` gets `429` + `Retry-After`; under the limit passes;
  limits are per-user, not global.
- `CountQuotaIT` — creating past `maxAlerts`/`maxChannels`/`maxSavedViews` → `409 QUOTA_EXCEEDED`.
- `QuotaUsageIT` — `GET /v1/me/quota` reports effective limits with current usage; admin get/update round-trips.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

Admins can set a user's quota and a user can read their own with live usage; the per-user API rate limit returns
`429` over the cap; alert/channel/view/storage quotas are enforced at creation; nulls fall back to the instance
default; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-026: Per-user quotas & rate limits`, then
`completion-reports/R2-026-CR-Quotas-Rate-Limits.md` with the 40-character implementation hash.
