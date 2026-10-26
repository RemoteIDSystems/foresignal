# R2-011-CR-LLM-Shared-Personal

| | |
|---|---|
| Prompt | R2-011-LLM-Shared-Personal.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `b8e64ca058c6c4fdac75e1d3f0ce525ef1b9471b` |
| Date | 2026-10-26 |

## What was added

- **`V18__llm_ownership_and_user_budget.sql`** (V17 was the highest migration on disk):
  - `llm_provider.owner_principal_id` (null = shared; every existing provider stays shared), with a partial index.
  - `per_user_hourly_cap_usd`, `per_user_daily_cap_usd` and `per_user_monthly_cap_usd` on `llm_provider`.
  - A new table, `llm_user_usage (provider_id, principal_id, minute)`, with the same counters as `llm_usage`, plus an
    index on `(principal_id, minute)`.
- **Ownership:**
  - `llm/LlmProviderScope` follows the R2-009 channel scope.
  - `api/support/LlmProviderOwnershipInterceptor` covers `/v1/llm/providers/{id}` and everything under it. Another
    user's provider is 404. A shared provider is readable, but changing, deleting or testing it, listing its models
    and reading its budget need `llm.shared.manage`.
  - `LlmProviderService` gains:
    - `list(scope)`, `create(body, scope)` and the replace path, which keeps the kind;
    - `shared()`, `personal()` and `personalChatProvider()`;
    - rejection of per-user caps on a personal provider.
  - `LlmSettingsService` refuses a personal provider for any instance role.
  - The settings export now contains shared providers only.
- **The acting user:**
  - `llm/LlmActor` resolves, in order: an explicit actor (a background job run for a user), else the request's
    signed-in user, else the machine.
  - `LlmService` binds every governed client to the actor when the client is created. It adds `client(providerId,
    actor)` and the selection rule `chatProviderFor(actor, settings)`.
  - `AskService` captures the actor in `prepare()` on the request thread, because the answer may stream from another
    thread. `Prepared` gains `actor` and `providerId`.
  - `EnrichmentService` runs as `alert.ownerPrincipalId()`, both for admission and for the call.
- **Metering:** `LlmUsageMeter` records calls with an actor a second time, in `liveUser`, and flushes them to
  `llm_user_usage`. It adds `userTotals` and `userCostSince`. `llm_usage` is written exactly as before.
- **Budgets:**
  - `LlmBudgetGovernor.reserve(provider, tokens, cost, principal)` checks a shared provider's per-user caps against
    that user's own spend first. The per-user window is seeded from `llm_user_usage`. The provider's own caps and
    token and request budgets follow, and every hold is released on refusal.
  - `Reservation` gains `principalId` and `userWindows`, with a 5-argument compatible constructor. The governor also
    gains `userSpend()`.
  - Machine refusals still call the one-argument `recordRefused`.
- **Egress:** `LlmProviderConfig` gains `ownerPrincipalId` and `perUserSpendCaps`, with an 18-argument compatible
  constructor. `local()` is false for every personal provider, and the mapper's `local` field agrees.
- **`getMyLlmBudget`** is served by `api/MyLlmBudgetController` and `llm/LlmUserBudgetService`.
- **`llm/LlmProviderReassigner`** (`llmProviders`) moves providers to the receiving admin. With no admin they become
  shared and disabled. With `reassign=false` they are deleted.
- **Tests (written, not run):**
  - `llm/PerUserLlmIT` covers:
    - kinds, including a raw pre-V18-style row being shared;
    - explicit kind and its 403s;
    - listing;
    - 404 across users on all six per-provider paths, and the key never being returned;
    - a shared provider being read-only for a user;
    - shared-only per-user caps and roles, and personal providers never being local;
    - per-user metering and the `getMyLlmBudget` contents;
    - machine usage leaving `llm_user_usage` empty;
    - the budget gates (403 `self.llm`, 401 for a token);
    - reassign, orphan and delete.
  - `llm/LlmPerUserBudgetTest` covers:
    - a per-user cap stopping one user while another continues;
    - release on refusal;
    - seeding from the meter;
    - a personal provider's own caps;
    - the machine being unaffected;
    - personal providers never being local.
  - `llm/LlmProviderScopeTest` is a unit test.
- **Docs:**
  - Architecture §19 gains three bullets.
  - RBAC-Design gains the "What exists today" row, the §C2 counts and §E "Built (R2-011)".
  - Invariants gains RBAC-16.

## Contract

- The 8 provider operations (list, create, get, update, delete, test, models and `getLlmProviderBudget`) keep the
  scope `admin` and the permission `llm.shared.manage`, and gain `x-foresignal-permission-alternatives: ["self.llm"]`.
  Their "v2.0" paragraph was rewritten: the old text described `owner_user_id` and "the shapes are unchanged".
- Probe, get/update settings and usage keep `llm.shared.manage` alone, with a rewritten paragraph saying they are
  shared-only.
- `AccessPermissions` was regenerated: 900 rows, 507 with a permission. The static check reported 0 problems.
- `LlmProvider` gains `shared`, `ownerPrincipalId` and `perUser{Hourly,Daily,Monthly}CapUsd`. `LlmProviderInput` gains
  `shared` and the three per-user caps. Both list `R2-011` in `x-foresignal-extended-by`. The changelog history was
  not touched.
- `getMyLlmBudget` stays `planned`. Its description was rewritten to state what is counted, the cap per kind, and the
  401 for a token.
- **`LlmBudgetProvider.providerId` had `format: uuid`, but provider ids are strings such as `claude` (V1
  `llm_provider.id varchar(64)`). I removed the `format` so the response matches the contract.** Please confirm.
- Postman was regenerated (936 requests, all operations covered). SDKs were not regenerated.

## Decisions and disagreements for the owner

1. **Ownership is the principal id, not `owner_user_id`.** This matches R2-008 and R2-009, as the prompt allowed.
2. **Per-user budget storage: columns on the provider, not a `llm_user_budget` table.** No authored operation sets a
   cap for one named user, so a table would have no writer. Each column is one cap that applies to every user's own
   spend on a shared provider. If you want caps per named user, that needs a new operation.
3. **Per-user metering is a separate `llm_user_usage` table, not a nullable `user_id` column on `llm_usage`.**
   `llm_usage`'s primary key `(minute, provider_id)` cannot take a nullable column, and changing that key would rewrite
   every reader. The separate table leaves the per-provider rollups untouched.
4. **Selection rule:**
   - **Ask and the enrichment of a user's alert** go to the user's first enabled personal provider with a chat model,
     by id, else the shared chat role. There is no fallback to shared when the personal provider refuses on budget.
   - **Every other chat use** (briefings, situation briefs, the doctor, translation, mapping suggestions and so on)
     stays on the shared role, though it is metered to the request's user. Those paths do not all re-check egress
     against the provider they get.
   - **Embeddings and TTS** stay shared: a personal embedding model would break the shared index.
5. **A personal provider is never `local` for egress.** Otherwise a user could create an `ollama`-typed or
   `options.local=true` provider pointing anywhere, and receive private-source context that the egress policy keeps
   on the instance.
6. **A user can read a shared provider's configuration** (never its key), but cannot test it, list its models or read
   its instance budget. Test and models spend or call the shared provider; the budget shows instance spend.
7. **Provider ids are one namespace.** A user creating an id that another user's personal provider already holds gets
   `409 LLM_PROVIDER_EXISTS`, which reveals that the id exists. Changing that would change the URL scheme.
8. **Orphaned personal providers become shared and disabled**, the same as R2-009's channels. A deleted user's usage
   rows stay as the record of what was spent.
9. **The settings export now omits personal providers.** They are users' own configuration.

## What runs

- `./gradlew compileJava compileTestJava checkUsEnglish`: BUILD SUCCESSFUL.
- `scripts/generate-access-permissions.py`: "900 rows (507 with a permission)".
- The static access check reported 0 problems.
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- R2-012 (views, preferences and workspaces) can follow `OwnerScope`, `ChannelScope` or `LlmProviderScope`, and
  register a `UserOwnedConfigReassigner`.
- `LlmActor.call(principal, work)` attributes background LLM work to a user.
- The next free migration is V19.
