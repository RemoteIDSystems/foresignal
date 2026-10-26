# R2-011 — Shared + personal LLM, per-user budgets

**Goal.** Let the instance run **shared** LLM providers (admin-managed) and each user run **personal** providers
(their own keys), with **per-user USD budgets and metering**. A null owner means a shared provider under
`llm.shared.manage`; a non-null owner means a personal provider under `self.llm`. Expose a user's spend via
`getMyLlmBudget`.

**Dependency.** R2-001 (users), R2-003/R2-004 (session + `self.llm` / `llm.shared.manage` gates). Builds directly on
the existing LLM provider + USD spend-cap work (prompt 026 and the 1.5.0 pricing/caps in `V13`). R2-005's reassigner
registers the provider table.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:** `getMyLlmBudget` — `scope: read`,
`x-foresignal-permission: self.llm`. Schema `MeLlmBudget{providers, totalSpentUsd, asOf}`. The existing LLM provider
CRUD operations gain owner semantics (shared vs personal); add `x-foresignal-permission`
(`llm.shared.manage` for shared management, `self.llm` for personal) additively — confirm each op's current
`x-foresignal-scope` and layer the permission without changing scopes.

**Read first.**
- `docs/OpenAPI.json` — `getMyLlmBudget` + `MeLlmBudget`, and the existing LLM provider operations/schemas
  (`LlmProvider*`, the pricing + `*CapUsd` fields from 1.5.0).
- `src/main/java/ai/foresignal/llm/LlmProviderEntity.java` — **no owner column today; all providers are shared.**
  `src/main/resources/db/migration/V13__llm_pricing_and_spend_caps.sql` — the pricing + hourly/daily/monthly USD
  caps and `llm_usage.cost_usd`.
- `src/main/java/ai/foresignal/llm/` — `LlmProviderService`, `LlmBudgetGovernor`, `LlmSpendCaps`,
  `LlmUsageMeter`/`LlmUsageEntity`/`LlmUsageId`, `LlmProviderBalances`, `LlmSettingsService`, `LlmService` — how
  usage is metered per minute and how the caps/balance are computed today (currently per-provider, not per-user).

**Build.**

- **Ownership on `llm_provider`.** Add `owner_user_id uuid` (nullable; **null = shared**) — reconcile with R2-008's
  ownership representation (if the project standardizes on the user's principal id, use that consistently and record
  it; otherwise a direct `owner_user_id` fk is fine here since LLM providers have no existing owner column). Existing
  providers migrate to **shared** (null owner). A session with `self.llm` creates/edits/deletes its **own** personal
  providers; shared providers are managed under `llm.shared.manage`. Cross-user access to a personal provider is
  refused (403/404). Keys stay encrypted at rest as today; a personal key is never returned.
- **Provider resolution for a user.** When a user's workflow needs an LLM (alerts enrichment, ask, briefings, etc.
  run on behalf of a user), the available providers are the **shared** ones plus that user's **personal** ones.
  Personal providers take precedence where the user has configured one (document the selection rule); shared
  providers are the default.
- **Per-user metering.** Extend usage metering so each call is attributed to the **acting user** (and the provider).
  Add the user dimension to `llm_usage` (e.g. a `user_id` column, nullable for machine/token-driven usage) so
  `cost_usd` rolls up per user-per-provider. Keep the existing per-provider rollups working; the per-user rollup is
  additive.
- **Per-user budgets + caps.** The USD hourly/daily/monthly caps (1.5.0) become **per-user-per-provider**: a shared
  provider can carry a per-user cap so one user cannot exhaust the shared budget; a personal provider's caps are the
  user's own. Extend `LlmBudgetGovernor`/`LlmSpendCaps` to enforce the cap for the acting user against that user's
  spend, not the instance total. Record where a per-user cap is stored (a `llm_user_budget` table keyed
  `(user_id, provider_id)`, or per-user cap columns — pick the cleaner and document). A machine api_token's usage
  keeps the existing instance-level behavior.
- **`getMyLlmBudget`.** Returns the caller's `MeLlmBudget`: per-provider spend and remaining budget (shared providers
  with the user's share + the user's personal providers), `totalSpentUsd` across them, and `asOf`. Read-only,
  `self.llm`.

**Model / migration.** Add **`Vn__llm_ownership_and_user_budget.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive): `owner_user_id` on `llm_provider`
(backfill null = shared), a `user_id` dimension on `llm_usage` (nullable), and the per-user budget storage. Entities
validate under `ddl-auto: validate`.

**Contract / docs.** Implement `getMyLlmBudget` to its authored schema but **leave its `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only); add `x-foresignal-permission` to the
provider ops (shared vs personal) additively, and expose `owner`/`shared` on the provider schema where useful. Keep
`AccessPolicy`/spec-sync in agreement. Regenerate Postman only — do **not** regenerate the client SDKs (R2-027 does that once at cutover) and leave every op's authored `x-foresignal-status`. Update `docs/RBAC-Design.md` / the LLM section of
`docs/Architecture.md` (shared vs personal providers, per-user metering and budgets, the selection rule) and
`docs/Invariants.md` (personal providers/keys are owner-scoped; a user's spend is metered and capped per user).

**Tests to write (do not run).** existing providers migrate to shared (null owner); a session creates a personal
provider owned by it; cross-user access to a personal provider → 403/404; usage is attributed to the acting user and
rolls up per user-per-provider; a per-user cap on a shared provider stops that user at the cap while others continue;
a personal provider's caps are the user's own; `getMyLlmBudget` reports per-provider spend, `totalSpentUsd` and
`asOf`; machine-token usage keeps instance-level behavior. Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** LLM providers are shared (null owner) or personal (owner set); a user manages their own and uses
shared + personal; usage and USD caps are per-user-per-provider; `getMyLlmBudget` reports a user's spend; machine
tokens unaffected; the op is built to its authored schema and gated (status stays `planned` until R2-027); docs + Postman updated; `checkUsEnglish` green; compilation
passes. The CR records the ownership/budget storage choices.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-011: Shared and personal LLM` + `completion-reports/R2-011-CR-LLM-Shared-Personal.md`. **Stage only
this prompt's files.** End your reply with `Implementation commit: <hash>`.
