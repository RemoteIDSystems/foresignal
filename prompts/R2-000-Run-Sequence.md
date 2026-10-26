# R2-000 — Release 2 (v2.0) Run Sequence & Parallel-Build Plan

> The v2.0 (multi-user RBAC + platform) prompts. They implement the **already-authored** v2.0 contract (`docs/OpenAPI.json`, API 2.0.0, every new operation `x-foresignal-status: planned`, commit `8ca39153`). Run them **in order** for an unattended single track, **or** fan them out across several Claude Code sessions using the dependency graph below.

## How to run (same driver as `000-Run-Sequence.md`, applied to `R2-*.md`)

1. **Pull** `git pull --ff-only origin main`. If `completion-reports/RUN-STATUS.md` exists, stop (owner stop). Planner changes under `docs/`, `prompts/`, `postman/`, `scripts/` are committed as `Planner: update roadmap and prompts` first.
2. **Next prompt:** the lowest `R2-NNN-*.md` with no matching `completion-reports/R2-NNN-CR-*.md` whose dependencies (below) are all reported done.
3. **Execute it completely, exactly as written:** implement, write its tests in the same pass, then **compile only** — `./gradlew compileJava compileTestJava`. Commit the implementation as `R2-NNN: <Title>`, write `completion-reports/R2-NNN-CR-<Name>.md` with the 40-char hash, commit as `R2-NNN: completion report`, push.
4. **⚠ DO NOT RUN ANY TESTS until the owner declares R2 code-complete** (the standing 2026-10-17 directive applies to this release too). The only Gradle is `./gradlew compileJava compileTestJava`. Write tests; never execute them. The guards (`checkApiCompat`, `lintOpenApi`, `checkUsEnglish`) and the suites run at owner code-complete.
5. Verify `git status` clean + `origin/main` == `HEAD` + the report has a hash; continue. Stop conditions, `RUN-STATUS.md` format, and the always-apply rules are exactly those in `000-Run-Sequence.md`.

## Parallel-build plan (to run multiple sessions at once)

Each session takes one prompt; a prompt may start once **all its dependencies have a completion report and the tree is pulled clean**. Sessions coordinate only through `origin/main` (pull before, push after; merge planner/other R2 commits with `git pull --no-rebase`, never rebase).

**Tier 0 — the spine (serial, must finish first):**
`R2-001` Identity model → `R2-002` Password & lifecycle → `R2-003` Auth & sessions → `R2-004` Authorization.

**Tier 1 — fan out after `R2-004` (run concurrently, grouped only by subsystem to limit merge overlap):**
- *Identity:* `R2-005` Users, `R2-006` Roles, `R2-007` Transactional email.
- *Per-user config:* `R2-008` Alerts, `R2-009` Channels, `R2-010` Source views, `R2-011` LLM, `R2-012` Preferences/views, `R2-013` Data access + audit.
- *Storage & lifecycle:* `R2-014` Disk monitoring, `R2-015` Retention, `R2-016` Archival, `R2-017` Backup.
- *Network sourcing:* `R2-018` Egress, then `R2-019` ADS-B, `R2-020` AIS, `R2-021` Datalink, `R2-022` Satellite, `R2-023` Ground-sensors (R2-019 lands the shared aggregator surface; R2-020…023 add domains — run after R2-019).
- *Security:* `R2-024` MFA, `R2-025` Sessions/tokens, `R2-026` Quotas.

**Tier 2 — last (serial, after everything above):**
`R2-027` Cutover & migration — bootstrap-token→first-admin, migrate global→shared/admin, flip every implemented op `planned`→`implemented`, regenerate SDKs + Postman, finalize API 2.0.0, run the full guards.

## Prompts (dependencies in parentheses)
- **R2-001** Identity model — users/roles/permissions tables, built-ins seeded, Flyway V14. *(none)*
- **R2-002** Password & lifecycle. *(R2-001)*
- **R2-003** Auth & sessions. *(R2-001, R2-002)*
- **R2-004** Authorization — role→permission in AccessPolicy. *(R2-001, R2-003)*
- **R2-005** User management API. *(R2-004)*
- **R2-006** Role management API. *(R2-004)*
- **R2-007** Transactional email (SMTP settings + templates). *(R2-001, R2-002)*
- **R2-008** Per-user alerts. *(R2-004)*
- **R2-009** Per-user channels. *(R2-004)*
- **R2-010** Shared collection + per-user source views. *(R2-004)*
- **R2-011** Shared + personal LLM, per-user budgets. *(R2-004)*
- **R2-012** Per-user saved views, preferences, workspace ownership. *(R2-004)*
- **R2-013** Data access gating + audit log. *(R2-004)*
- **R2-014** Disk & resource monitoring + thresholds + alerts. *(R2-004)*
- **R2-015** Retention control. *(R2-004)*
- **R2-016** Cold-storage archival. *(R2-004; R2-015 helpful)*
- **R2-017** Backup & restore. *(R2-004)*
- **R2-018** Egress routing & IP rotation. *(R2-004)*
- **R2-019** ADS-B first-party aggregator (+ the shared aggregator API). *(R2-004)*
- **R2-020** AIS first-party aggregator. *(R2-019)*
- **R2-021** Aircraft datalink aggregators. *(R2-019)*
- **R2-022** Direct satellite reception. *(R2-019)*
- **R2-023** Ground-sensor aggregators. *(R2-019)*
- **R2-024** MFA/2FA. *(R2-003)*
- **R2-025** Session & API-token management. *(R2-003)*
- **R2-026** Per-user quotas & rate limits. *(R2-004)*
- **R2-027** Cutover & migration; API → 2.0.0 finalized. *(ALL above)*
