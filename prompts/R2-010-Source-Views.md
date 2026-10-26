# R2-010 — Shared collection + per-user source views

**Goal.** Separate **what the instance collects** from **what each user sees**. The collector pulls each source
**once** — the **union** of every user's enabled sources intersected with the instance's enabled set — and each
user gets their own enabled-source *view* and display prefs on top. Admins manage the instance collection set;
users manage their own view.

**Dependency.** R2-001 (users), R2-003/R2-004 (session + `self.sources` / `collection.manage` gates). This is the
heart of the "pull once, view per user" decision — get the union logic right.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:**
- `getMySources`, `updateMySources` — `scope: read`, `x-foresignal-permission: self.sources`. Schemas
  `UserSourceView{sources}` / `UserSourceViewUpdate`.
- `getCollectionSources`, `updateCollectionSources` — `scope: admin`, `x-foresignal-permission: collection.manage`.
  Schemas `CollectionSet` / `CollectionSetUpdate`.
Confirm the exact schema shapes in `docs/OpenAPI.json` and match them.

**Read first.**
- `docs/OpenAPI.json` — the four operations and their `CollectionSet`/`UserSourceView` schemas.
- `src/main/resources/db/migration/V1__baseline.sql` `create table source_settings` (~L244) and
  `src/main/java/ai/foresignal/store/SourceSettingsEntity.java` — the **global** per-source config today
  (`enabled`, `interval_seconds`, budgets, params, egress…). This is the instance collection substrate; it is not
  per-user.
- `src/main/java/ai/foresignal/source/SourceService.java` (`defaultRow`, `seedDefaults`, enable/enable-core/
  enable-free, the `core` set from prompt 150) and `SourceRegistry` — how sources are enabled today.
- `src/main/java/ai/foresignal/collect/` — `CollectorScheduler`, `FetchExecutor`, `PullGovernor` (prompt 150/160):
  **where the set of sources to pull is decided.** The union gate plugs in here.

**Build.**

- **Instance collection set (`CollectionSet`, admin).** `getCollectionSources`/`updateCollectionSources` manage which
  sources the instance is **allowed** to collect — the existing global `source_settings` enablement, now framed as
  the instance set under `collection.manage`. Updating it is the operator action; it does not, by itself, mean every
  user sees every source.
- **`user_source_pref` / `UserSourcePrefEntity`.** Per-user, per-source view + display prefs: `user_id`, `source_id`,
  `enabled boolean`, plus display prefs the view needs (e.g. color/visibility/label ordering — keep to what the
  `UserSourceView` schema carries). PK `(user_id, source_id)`; index `(user_id)`. `getMySources` returns the user's
  view; `updateMySources` upserts it. A user can only enable a source that is in the instance set (enabling one the
  instance does not collect is either rejected or recorded as "wanted but not collected" — pick per the schema and
  document; recommended: allow the preference, but it only takes effect while the instance set includes the source).
- **The union pull gate (the core).** The collector pulls a source iff it is **enabled in the instance set**
  (`collection.manage`) **and** at least one user's `user_source_pref` enables it (`self.sources`) — the union of all
  users' enablement, intersected with the instance set. One pull feeds everyone; per-user views/alerts filter the
  shared results to each user's set. Implement this as the source-eligibility decision in the collector
  (`FetchExecutor`/scheduler), replacing "pull every globally-enabled source" with "pull every instance-enabled
  source that any user wants." **Keep the single-pull invariant:** a source wanted by ten users is pulled once.
  - **Backward compatibility / zero-user instance:** when there are no users yet (first run, token-only operation),
    fall back to the existing behavior — the instance-enabled set is collected as today — so nothing stops collecting
    before users exist. Document this fallback.
  - Streaming sources, pacing (`HostPacer`), the governor (`PullGovernor`) and budgets are unchanged beneath the gate.
- **View filtering.** Records/changes/tiles a user sees are filtered to the user's enabled view where the read path
  is user-scoped (reuse the existing visibility/restriction machinery — `security/visibility`, `restrict`). A user
  who has not enabled a source does not see its records in their view, even though the instance collected it for
  someone else. Admin/global reads are unaffected.

**Model / migration.** Add **`Vn__user_source_pref.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive) for `user_source_pref`. If `getCollectionSources`
needs any column beyond today's `source_settings`, add it in the same migration. Entities validate under
`ddl-auto: validate`.

**Contract / docs.** Implement the four ops to the authored schemas but **leave their `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only); keep the permissions above. Keep `AccessPolicy`/spec-sync in
agreement. Regenerate Postman (bodies for `updateMySources`, `updateCollectionSources`). Update `docs/RBAC-Design.md`
and a `docs/Network-Sourcing.md`/`docs/Architecture.md` section on **shared collection**: the instance set vs. the
per-user view, the union pull gate, the single-pull invariant, and view filtering. Update `docs/Invariants.md`
(collection is shared and pulled once; a user's view never causes a second pull; a user sees only their enabled view).

**Tests to write (do not run).** `getMySources`/`updateMySources` round-trip a user's view; `getCollectionSources`/
`updateCollectionSources` manage the instance set under `collection.manage`; the union gate pulls a source wanted by
any user once, and does not pull a source no user wants even if instance-enabled; a source outside the instance set is
never pulled regardless of user prefs; with no users, the instance set is collected (fallback); a user's record view
is filtered to their enabled sources; `self.sources` gates the per-user ops and `collection.manage` the instance ops.
Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the tests; do not
execute them. US English.

**Done when.** The instance collection set and per-user source views are distinct; the collector pulls the union
(instance-enabled ∩ any-user-wanted) exactly once per source; per-user views filter the shared results; the four ops
are built to their authored schemas and correctly gated (status stays `planned` until R2-027); the single-pull invariant holds; docs + Postman updated; `checkUsEnglish`
green; compilation passes. The CR shows the union gate's placement in the collector.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-010: Shared collection and per-user source views` +
`completion-reports/R2-010-CR-Source-Views.md`. **Stage only this prompt's files.** End your reply with
`Implementation commit: <hash>`.
