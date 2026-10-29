# R2-033 — Breaking-news special edition

**Context.** Automated-newspaper feature, after **R2-032**. Editions run on a cron
(`BriefingScheduler`, `BriefingInput.schedule` + timezone). `R2-028` authored `BreakingNewsConfig`
(`enabled`, `ruleIds`, `minSeverity`, `cooldownMinutes`) on the edition. The alert/trigger pipeline
already evaluates rules and fires alerts.

**Goal.** Fire an **off-schedule** edition run the moment a matching alert/trigger fires — a special
"breaking news" edition — in addition to the scheduled run, with a cooldown so a burst does not spam.

## Operations

No new HTTP op — breaking-news runs are driven by the edition's `breakingNews` config at alert time.

## Read first

- `briefing/BriefingScheduler.java` and `BriefingRunner.java` (how a run is claimed and executed) —
  the off-schedule run reuses the same runner, not a second code path.
- The alert/trigger pipeline (where a rule match fires an alert/notification) and how a listener
  subscribes to those events.
- `BriefingScope.ruleIds` (already ties a briefing to trigger rules).

## Build

1. A listener on the alert/trigger pipeline: when an alert fires whose rule is in an edition's
   `breakingNews.ruleIds` (and meets `minSeverity`), and the edition's `breakingNews.enabled`, run
   that edition off-schedule via `BriefingRunner`, honoring `cooldownMinutes` (suppress a second
   breaking run within the window; coalesce a burst into one).
2. Mark the run as a breaking-news edition (a flag on the run) so the heading/render and delivery can
   say "Breaking" and so it is distinguishable from the scheduled edition. Deliver to the edition's
   configured targets (R2-031) and refresh the hosted site (R2-032).
3. The same scheduler-claim discipline (no double-run across instances/ticks) applies to breaking
   runs.

**Migration.** If a run flag needs persisting, next free additive Flyway `V{n}__breaking_run.sql`
(a boolean on the briefing-run table). Additive only.

## Tests to write (do not run)

- `BreakingNewsIT` — a matching alert fires an off-schedule edition run; a second match within
  `cooldownMinutes` is suppressed; a non-matching alert, a below-`minSeverity` alert, or a disabled
  config does nothing; the breaking run delivers to the edition's targets.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English.

## Done when

A matching alert fires a cooldown-guarded off-schedule edition, marked and delivered as breaking, via
the same runner and claim discipline; compile and `checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; frozen v1.0.0 spec; `x-foresignal` changelog; `.gitlab-ci.yml`; other
prompts' files. Leave R2-028 ops `planned`. **Stage only this prompt's files.**

## Commit

`R2-033: Breaking-news special edition`, then
`completion-reports/R2-033-CR-Breaking-News-Edition.md` with the 40-character hash.
