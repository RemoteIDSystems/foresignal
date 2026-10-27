# R2-026-CR-Quotas-Rate-Limits

| | |
|---|---|
| Prompt | R2-026-Quotas-Rate-Limits.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `de54b129e17c6fe1693fe7cd4d1dcb4f3da8994e` |
| Date | 2026-10-26 |

## What was added

- **Migration `V25__user_quota.sql` (additive):** a `user_quota` table.
  - One row per user, deleted with the user (`on delete cascade`).
  - Five nullable limits, with a check constraint that each is non-negative, plus `updated_at`.
- **`security/quota/` package:**
  - **`Quota`**, the `Quota` schema as a record:
    - `over(default)` is the merge;
    - `parse` refuses negatives, non-integers, values out of range and unknown members (400);
    - `0` allows nothing.
  - **`QuotaLimit`:** `ALERTS`, `CHANNELS`, `SAVED_VIEWS` and `STORAGE`, each naming its `Quota` member.
  - **`UserQuotaEntity`** and **`UserQuotaRepository`**.
  - **`QuotaService`:**
    - `effective(userId)` merges the user's row over the instance default, stored in `app_setting` under the new key
      `quota.default` (`SettingKeys.QUOTA_DEFAULT`, added to `ALL`).
    - `get` and `replace` are the admin operations. An all-null quota deletes the row.
    - `usage` and `checkCount`/`checkStorage` measure usage by the user's principal:
      - `trigger_rule`, `notification_channel` and `saved_view` by `owner_principal_id`;
      - storage is `attachment.size` by `uploaded_by` plus `octet_length(context::text) + octet_length(layout::text)`
        of the workspaces the user owns.
    - Own quotas are cached per user and evicted on replace and after commit. It implements `IngestStateReset`, so
      the test cleaner clears it.
  - **`UserRateLimiter`:** one Bucket4j bucket per user, modeled on `ChannelRateLimiter` and driven by the
    application clock. It also counts each user's requests in the current clock minute.
  - **`UserRateLimitFilter`** (`TokenFilter.ORDER + 6`):
    - It charges every `/v1` and `/mcp` request of a session (`AuthenticatedSession`) or of a user's own token
      (`TokenOwner`, R2-025) to the user's one bucket.
    - Over the effective `apiRateLimitPerMinute`, the answer is `429 RATE_LIMITED` with `Retry-After`, `class: user`,
      `limit` and `quota`.
    - A user refused in every minute for 5 minutes raises the R2-014 operational alert `system.user_rate_limited`.
      `system.user_rate_limit_cleared` follows once a minute passes with no refusal.
- **Count and storage checks** (`409 QUOTA_EXCEEDED` with `limit`, `quota` and `usage`):
  - `TriggerRuleService.create(input, owner)` checks `maxAlerts`;
  - `ChannelService.create(body, owner)` checks `maxChannels`;
  - `SavedViewService.create(…, owner)` checks `maxSavedViews`;
  - `AttachmentService.upload` checks `maxStorageBytes` against the uploader;
  - `WorkspaceService.create`, `putLayout` and context patches check `maxStorageBytes` against the workspace
    **owner**, by the size change of context plus layout.
  - Each service gets `QuotaService` through an optional setter, so the constructors are unchanged.
- **`api/QuotasController`:**
  - `getMyQuota` works for a session or a user's own token; a token with no user gets 401.
  - `getUserQuota` and `updateUserQuota` work for an id that is not a UUID or not a user, which gives 404
    `UNKNOWN_USER`.
- **Error code:** `QUOTA_EXCEEDED` (409).
- **Tests (written, not run):**
  - `QuotaServiceTest` (Mockito): the merge over the default, null falling through, unlimited when unset, the count
    and storage checks, non-users never limited, and parsing.
  - `ApiRateLimitIT`:
    - 429 with `Retry-After` and `class: user` over the limit;
    - the bucket refills after a minute;
    - two sessions and the user's own token share one budget;
    - limits are per user;
    - an unset quota is unlimited;
    - the instance default applies.
  - `CountQuotaIT`:
    - `maxAlerts`, where an admin token's shared rule is not counted;
    - `maxChannels`;
    - `maxSavedViews`, including `0`;
    - the instance default;
    - `maxStorageBytes` on an upload and on a workspace create.
  - `QuotaUsageIT`:
    - effective limits with usage;
    - 401 for a machine token;
    - admin get and put round-trips, where PUT replaces the whole quota;
    - validation, with 400 and 404;
    - 403 without `users.manage`.
- **Docs:** a `docs/Operator-Guide.md` bullet under "Signing in and sessions".

## Contract

- **`Quota`:** a member description each, `minimum: 0`, and `additionalProperties: false`. The schema description
  explains null and `0`.
- **`QuotaUsage`:** the description names the `usage` members: `alerts`, `channels`, `savedViews`, `storageBytes` and
  `apiRequestsThisMinute`.
- **Operation descriptions:**
  - `getMyQuota`, `getUserQuota` and `updateUserQuota` describe the rules above.
  - `updateResourceThresholds` lists the two new `system.*` subtypes.
- **`409` (`QUOTA_EXCEEDED`):**
  - added to `createChannel`, `createWorkspace`, `putWorkspaceLayout`, `patchWorkspaceContext` and `uploadAttachment`;
  - appended to the existing 409 of `createTrigger` and `createView`;
  - each of the seven descriptions names its limit.
  - Response order is preserved; the new `409` is appended.
- **Postman:** regenerated (936 requests). Statuses stay `planned`, and the SDKs were not regenerated.

## Decisions and disagreements for the owner

1. **The instance default has no API.** The prompt puts it in `app_setting`, and the contract has no operation to set
   it. `QuotaService.setInstanceDefault` exists and the tests use it, but an admin cannot set it over HTTP today. Say
   whether you want a `/v1/settings/quotas` pair. Until then, a fresh instance limits nobody.
2. **The user rate limit ignores `foresignal.ratelimit.enabled`.** That switch governs the per-client limits. A quota
   is set per user by an admin, so the limit is on exactly when a quota sets it. A side effect is that tests need no
   property override. Say if you want the global switch to govern it too.
3. **The limit is a separate filter, not part of `TokenFilter`.** It runs right after `TokenFilter` and after the
   per-client limits, which keep applying, so a token's own `maxRequestsPerMinute` still limits that token. The prompt
   named `TokenFilter`/`AccessPolicy`; a filter beside the per-client one is the same enforcement point, and
   `TokenFilter` stays as it was.
4. **"Alerts" means the trigger rules a user owns,** not the alerts those rules fire. Fired alerts are produced by the
   engine, not created by the user, so they are not something a quota at creation time can bound.
5. **What is counted, and what is not:**
   - Only the self-service create paths that set an owner are checked.
   - Rules, channels and views created by MCP tools, Ask actions, the analyst, trips or indicator watches are
     instance-owned (`owner` null). They are not counted against anyone and never refused.
   - The personal notification channel that `PUT /v1/me/notification-preferences` creates (`savePersonal`) is not
     refused, but it is counted in usage. Refusing would make a preferences save fail.
6. **Storage is measured as uploaded attachment bytes plus the workspace JSON text.**
   - `media` has no owner; it is ingested source media, not something a user uploads, so it is not counted.
   - A workspace's bytes count against its owner even when an editor makes the change.
   - The Java-side size of a change and Postgres's `jsonb::text` length differ by whitespace, so the measure is close
     but not byte-exact.
7. **`0` means "nothing".** Per the prompt, the only rule is non-negative. An `apiRateLimitPerMinute` of `0` refuses
   every API request, `getMyQuota` included, until an admin changes it.
8. **The checks are not transactional with the insert.** Two creates racing at the limit can both pass, and the next
   one is refused.
9. **`apiRequestsThisMinute` counts the current clock minute,** not a sliding window. It counts every request of the
   user, refused ones included.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- The three Quotas operations are implemented and still `planned`. R2-027 flips them.
- The next Flyway migration is **V26**.
