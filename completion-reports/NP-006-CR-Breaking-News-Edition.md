# NP-006-CR-Breaking-News-Edition

| | |
|---|---|
| Prompt | NP-006-Breaking-News-Edition.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test was run) |
| Implementation commit | `df18ba6f8f3a55c93eb65be30554ba39e2287970` |
| Date | 2026-10-30 |

## What was added

- **Migration `V31__breaking_run.sql` (additive):**
  - `briefing.breaking_news jsonb`: the edition's `BreakingNewsConfig`, null for existing rows.
  - `briefing.last_breaking_at timestamptz`: the cooldown claim.
  - `briefing_run.breaking jsonb`: the alert that fired the run; null for every scheduled, manual and preview run.
- **Input and views:**
  - `BriefingInput.BreakingNews(enabled, ruleIds, minSeverity, cooldownMinutes)`. Defaults are `info` and 60
    minutes, the cooldown range is 1–10080, and there are at most 100 rules. Unknown members, a bad severity and an
    out-of-range cooldown are 400s.
  - A warning is given when it is enabled but neither `ruleIds` nor the scope's `ruleIds` lists a rule, because no
    alert could then fire it.
  - `Briefing.breakingNews` is returned on reads. `BriefingRun.breaking` is the trigger
    `{alertId, ruleId, ruleName, severity, firedAt, message}`.
- **`BreakingNewsListener`** subscribes to the `AlertBus` as `breaking-news`, with the other alert consumers at
  `StartupOrder.NOTIFY_ROUTER`. It is left out in edge mode, like the briefings. An alert fires an edition when all of
  these hold:
  - it is `fired`, not suppressed by a snooze, and not an exercise replay;
  - the edition is enabled and `breakingNews.enabled` is true;
  - its rule is in `breakingNews.ruleIds`, or, when that list is empty, in the scope's `ruleIds`;
  - its severity is at or above `minSeverity`;
  - for a user-owned edition (NP-003), the rule is shared or the owner's own, so one user's alert never fires
    another user's edition.
- **The run itself** (`BriefingService.runBreaking`) uses the same runner and the same claim:
  - **Cooldown:** one conditional update sets `last_breaking_at` only if the last breaking run is older than the
    cooldown. A burst, on one instance or several, therefore coalesces into one run per window.
  - **Claim:** it then takes the scheduler's `running_since` claim. While another run of the edition is in progress,
    the breaking run waits for it, up to 20 tries 15 seconds apart. If that run never finishes, the breaking run is
    dropped with a warning.
  - **Execution:** `execute(briefing, trigger)` → `BriefingRunner.run(briefing, trigger)`, the same code path as a
    scheduled run.
- **Marked as breaking:**
  - The title is prefixed "Breaking: ", and the content leads with `**Breaking:** <rule>: <message>`.
  - The LLM writer gets a breaking-news instruction.
  - Delivery to `channelIds` or to the NP-004 targets uses the alert's severity (`BriefingRunner.severity(run)`).
  - `execute` refreshes the NP-005 site, so the site shows the breaking run as soon as it succeeds.
- **The next scheduled run** computes its period from the previous non-breaking run
  (`findFirstByBriefingIdAndStatusInAndBreakingIsNull…`), so a breaking run does not shorten the regular edition's
  window.
- **Spec addition** (`docs/OpenAPI.json`): `BriefingRun.breaking` (read-only), with `NP-006` added to
  `BriefingRun.x-foresignal-extended-by`. `BreakingNewsConfig` was already authored by NP-001. Postman and
  `AccessPermissions` were regenerated with no change (907 rows). The changelog was not touched.
- **Test (written, not run):** `BreakingNewsIT`:
  - a matching alert gives one off-schedule run, flagged with the alert and leading with "Breaking:";
  - a second match inside the cooldown starts no run;
  - another rule, a severity below `minSeverity`, a suppressed alert and a disabled configuration start nothing;
  - empty `ruleIds` falls back to the scope's rules;
  - the breaking run delivers to an alert target;
  - validation.
  - The bus subscription is detached per test, and `fire` is called with the engine's real alert, so runs are
    awaited deterministically.

## Decisions and disagreements for the owner

1. **The run flag is a jsonb column `briefing_run.breaking` (the triggering alert), not the boolean the prompt
   suggests.** Null means "not breaking", and the alert's id, rule and severity are kept, which delivery and the
   heading need. Is a separate boolean also wanted?
2. **The cooldown is per edition and is claimed before the run starts.** An alert that arrives inside the window is
   dropped, not queued for after it. The alert of a coalesced burst that the run reports is the first one.
3. **A breaking run waits up to 5 minutes for a run already in progress.** After that it is dropped with a warning,
   and its cooldown stays claimed.
4. **Empty `ruleIds` means the scope's rules.** NP-001's schema does not define an empty list. The alternative is
   "never fires".
5. **Owned editions accept shared rules** (no owner principal) as well as the owner's own.
6. The 2.1.0 changelog was not touched. NP-007 should mention `BriefingRun.breaking` there.

## What runs

- `./gradlew compileJava compileTestJava checkUsEnglish` → `BUILD SUCCESSFUL` (exit 0).
- `generate-access-permissions.py` → "907 rows (555 with a permission)". `generate-postman.py` → "943 requests across
  90 tag folders … covers all ops: True".
- No test, `build`, `check`, `lintOpenApi`, `checkApiCompat` or SDK generation was run.

## Notes for the next prompt

- NP-007 flips the six NP-001 operations and `openEditionSiteShareLink` to `implemented`. Its changelog amendment
  should list `BriefingRun.deliveryResults` (NP-004), `openEditionSiteShareLink` with the `edition-site` share type
  (NP-005), and `BriefingRun.breaking` (NP-006).
- The next free migration is `V32`.
