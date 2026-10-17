# 115-CR-Exercise-Replay-And-Alert-Quality

| | |
|---|---|
| Prompt | 115-Exercise-Replay-And-Alert-Quality.md |
| Status | Complete (owner decisions listed under Issues) |
| Implementation commit | `de3afdc689d1224a0e74db797dd73f91a55d51fd` |
| Date | 2026-10-16 |

## Summary

Two operator workflows on top of the trigger engine and the 103 backtest layer: exercise replays and alert-quality
tuning. Compiles; tests written, not run.

- **Exercise flag (`ai.foresignal.exercise`):** `alert.exercise` (not null, default false) and
  `alert.exercise_run_id` (index `alert_exercise_run_idx`), `notification.exercise`, `notification_channel.accept_exercise`.
  `ExerciseMarking` is the one marking helper: title prefix `[EXERCISE] ` on the title and short title, the footer
  "This is an exercise replay of data from {from}–{to}. No action required.", webhook header
  `X-Foresignal-Exercise: true` and payload `exercise: true`, push `exercise: true` with urgency `normal` (APNs
  `active`, FCM `NORMAL`, never time-sensitive), CAP `status=Exercise` (the column, or 112's context flag), and
  `exercise`/`exerciseRunId` on `Alert` and `AlertEvent` (API, SSE, MCP). The router marks the stored message and the
  delivery worker marks again after the channel's templates re-render it.
- **Isolation:** the router routes an exercise alert only to channels with `acceptExercise` or named in the run (the
  channel's own filters still apply; the rule's mute does not). `AlertQuery.includeExercise` (default false) keeps
  exercise alerts out of `listAlerts`, `streamAlerts`, the events-stream `alerts` topic (`includeExercise=true` shows
  them), glance, chat, CAP feeds, share links, Ask, the analyst and quality statistics. `NOT exercise` was added to the
  cooldown seed (`RuleSqlBuilder`, `TriggerEngine`), edge seeds, the storm seed, snoozed entities, auto-resolve and
  inverse-edge resolution (`AlertStore`), rule statistics, window-rule cooldown and cap (`WindowEngine`), digests
  (`SqlDigestStatsProvider`), rollups and stats (`StatsService`), the timeline, the operator gauges, enrichment
  context, precursor tests and the 103 backtest's actual-alert counts. Escalation, MQTT state and the federation
  compact outbox skip exercise alerts; enrichment runs only with the run's `enrich=true`. Replays create no records,
  so trip briefings, pattern of life and 113's record-level exercise rule are untouched.
- **Replay runs:** `exercise_run` (`ExerciseRunEntity`). `ExerciseService` validates `ExerciseRunCreate` (rules by id
  or tag, `from`/`to` or `fromSeq`/`toSeq` through `seq_clock` `S(T)`, at most `exercise.maxWindow` 7 d, channels must
  exist), refuses a window below any involved source's floor (`RetentionStateStore.floors()`/`floorSeq`) with
  `400 REPLAY_BEYOND_RETENTION` naming the source and its earliest replayable time, and allows one active run
  (`409 EXERCISE_RUNNING`). `ExerciseReplay` reuses 103: `RuleBacktest.replayMatches` (new public wrapper, no cooldown
  seed, approach rules judged in Java) per 50k-seq chunk, then `AlertPass` in simulate mode with a fresh state per
  rule (cooldown per entity or rule on `observed_at`, `max_alerts_per_hour` on `observed_at`, active periods and
  windows). Count/absence rules replay at record level (`WINDOW_APPROXIMATE`), anomaly rules go to
  `skippedRules`, edge rules with versions whose predecessor before the window is gone get
  `PREDECESSOR_OUTSIDE_WINDOW`. `ReplayPacer`/`ExerciseSpeed` emit in seq order with the scaled `ingested_at` gap
  (single sleep ≤ 60 s ÷ factor, `max` none) on a virtual thread, checking cancellation between matches. Each match
  becomes an exercise alert (`AlertStore.Writer.insertExercise`, `observed_at` and snapshot of the version, context
  with run, window, named channels and enrich flag) published on the AlertBus. Dry runs project `alertsByRule` and
  `notificationsByChannel` without storing anything. Purge deletes alerts, notifications, deliveries, push
  dispatches, reads, capabilities, CAP messages and signals; `ExerciseRetentionStep` expires exercise alerts after
  `exercise.alertRetention` (7 d). Start, cancel and purge are audited; `system.exercise_started`/`_finished`
  notifications (marked exercise) go to channels subscribed to system events. Runs left active by a restart become
  `FAILED`.
- **Feedback capture:** signed actions `feedback:useful` and `feedback:noise` (`ActionsConfig.SUPPORTED`; on by
  default for Telegram and Matrix, listable on ntfy and Web Push), executed in any state without superseding the other
  capabilities; chat `/feedback <id> <value>` (agent chats); MCP `set_alert_feedback`; Ask `propose_feedback`
  (`set_alert_feedback` handler).
- **Implicit signals (`ai.foresignal.quality`):** `alert_signal` (unique per alert and type). `AlertSignalRecorder`
  records `ACK_FAST` (≤ 15 min), `SNOOZED` (alert snooze, or rule snooze for its open alerts through the new
  `TriggerRuleSnoozedEvent`) and `FLAP` (`inverse_edge` ≤ 10 min); the nightly job records `UNOPENED` (unread 24 h,
  only when read cursors or reads exist). Weights in `AlertSignalType` (+0.5, −0.5, −0.5, −0.25); `signalScore` is the
  weighted sum per alert. `AlertQualityService.forRule` gains `signals`, `signalScore`, `signalWeights` and `daily[]`.
- **Daily trends:** `rule_quality_daily` (unique rule/day), aggregated by `RuleQualityDailyJob` (after 02:00 UTC,
  15-minute tick, `foresignal.quality.scheduled`) for yesterday and today: fired, feedback counts, signals, median ack
  seconds, flaps; the trigger engine adds its cooldown and rate-cap suppressions after each committed pass. Retained 400
  days (`RuleQualityRetentionStep`). Optional briefing section `AlertQualitySection` with
  `scope.includeAlertQuality` (default false).
- **Tuning workflow:** `TuningSuggester` 1.1.0 adds `ACTIVE_WINDOW`, `SEVERITY_UP` and `SEVERITY_DOWN`
  (deterministic, sample minimums). `tuning_suggestion` (dedupe on rule, kind and patch hash) and `rule_tuning_log`.
  `TuningWorkflowService`: generation on `GET …/suggestions` and nightly; any rule change whose definition hash
  (`RuleRevisions`: the `TriggerRule` without read-only members, canonical JSON, SHA-256) differs marks open
  suggestions `STALE`; apply (`409 SUGGESTION_STALE` when not open or the rule changed) merges the patch into the
  current `TriggerRuleInput` and calls `TriggerRuleService.update`, then logs before/after; dismiss; undo while the
  rule is unchanged since the change. `SuggestionExplainer` (`explain=true`, chat role, budgeted, authenticated
  callers only). MCP `apply_tuning_suggestion`; Ask `propose_tuning` shows the patch verbatim.
- **API, spec, docs:** `ExercisesController` (5 operations) and `RuleAnalysisController` (apply, dismiss, undo,
  tuning log; `status` and `explain` on suggestions). The 9 operations are `implemented` and removed from
  `pending-operations.txt`; spec changelog 115 entry; `AccessPolicy` rules for `/v1/exercises` (agent, always
  authenticated) and `GET /v1/triggers/*/tuning-log` (read). Postman regenerated with BODIES for `createExercise`,
  `applyTuningSuggestion`, `dismissTuningSuggestion` and captures for `exerciseId`, `suggestionId`, `tuningLogId`.
  MCP `start_exercise`, `get_exercise`, `apply_tuning_suggestion`, `set_alert_feedback`. Operator Guide "Exercises and
  replay" and "Tuning alert quality"; Agent Guide tool rows and the two new `propose_*` tools.

## Files created / modified

**Created (main):** `exercise/{ExerciseMarking,ExerciseSpeed,ReplayPacer,ExerciseRunEntity,ExerciseRunRepository,
ExerciseReplay,ExerciseService,ExerciseRetentionStep,ExerciseViews,package-info}`,
`quality/{AlertSignalType,AlertSignalEntity,AlertSignals,AlertSignalStore,AlertSignalRecorder,RuleQualityDailyEntity,
RuleQualityDailyStore,RuleQualityDailyJob,RuleQualityRetentionStep,TuningSuggestionEntity,TuningSuggestionRepository,
RuleTuningLogEntity,RuleTuningLogRepository,RuleRevisions,TuningWorkflowService,SuggestionExplainer,
AlertQualitySection,package-info}`, `trigger/TriggerRuleSnoozedEvent`, `api/ExercisesController`,
`ask/TuningActionHandlers`, `mcp/tools/{StartExerciseTool,GetExerciseTool,ApplyTuningSuggestionTool,
SetAlertFeedbackTool}`.

**Modified (main):** `trigger/{AlertView,AlertStore,AlertQuery,RuleSqlBuilder,TriggerEngine,TriggerRuleService,
EnrichmentWorker,EnrichmentContextBuilder,window/WindowEngine}`, `store/{AlertEntity,NotificationEntity,
NotificationChannelEntity,timeline/RawTimelineBackend}`, `notify/{NotificationMessage,NotificationStore,
NotificationService,NotificationRouter,NotificationJson,NotificationChannel,NotificationAction,ChannelInput,
ChannelInputParser,ChannelService,DeliveryWorker,DigestBuilder,types/WebhookChannel,escalation/EscalationWorker,
action/{ActionsConfig,ActionService,ActionDurations},push/{PushMessage,PushContentFactory,PushOptions,PushPayloads,
apns/ApnsPayloads,fcm/FcmPayloads}}`, `backtest/{AlertQualityService,TuningSuggester,RuleBacktest,
PrecursorTestService}`, `api/{AlertsController,RuleAnalysisController,mapper/AlertMapper,mapper/ChannelMapper,
error/ErrorCodes}`, `stream/sse/AlertsTopic`, `cap/CapService`, `chatops/{ChatCommand,CommandRouter,
ChatCommandService,TelegramConnector}`, `ask/AskActionTools`, `mcp/tools/AlertItems`, `mqtt/channel/MqttStatePublisher`,
`federation/compact/CompactOutboxService`, `briefing/{BriefingScope,BriefingValidator,SqlDigestStatsProvider}`,
`stats/StatsService`, `system/OperatorGauges`, `retention/RetentionStateStore`, `security/AccessPolicy`,
`settings/SettingKeys`, `resources/application.yml`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−9), `src/test/resources/application-test.yml`.

**Tests:** `exercise/{ExerciseMarkingTest,ReplayPacingTest,ReplayRunIT,ExerciseIsolationIT}`,
`quality/{FeedbackCaptureIT,AlertSignalTest,RuleQualityDailyJobIT,TuningSuggesterExtensionTest,TuningWorkflowIT}`;
updated `trigger/RuleSqlBuilderTest` (cooldown seed SQL), `backtest/BacktestControllersIT` (new parameters),
`openapi/PostmanCollectionContractTest` (validators for the apply and dismiss bodies; `createExercise` schema-only).

## Deviations from the prompt

1. **Exercise vs 112/113 reconciliation:** the alert column is the truth; `ExerciseMarking.isExercise` also honors
   112's context flag (`"exercise": true`), and exercise alerts carry that flag in their context, so CAP gets
   `Exercise` either way. 113's `"exercise": true` is a record payload rule; replays create no records, so it is
   unchanged. The federation compact outbox now skips exercise alerts.
2. **Named channels** still pass their own filters (events, minimum severity, rule filter); a muted rule's exercise
   alerts are routed (the mute is real routing state, the rehearsal is not).
3. **Delivery marking twice:** the stored `alert.fired` message is marked and the delivery worker marks again after
   the channel's templates re-render the text (idempotent), because templates replace the title and body.
4. **Window and dry run:** both bounds of one family are required; the default speed is `x60`; a dry run is answered
   with `200` and a fresh id that is not stored. `stats` adds `alertsRaised` and `error` (spec amended).
5. **Purge while active** answers `409 EXERCISE_RUNNING`, and cancelling a finished run `409 EXERCISE_NOT_RUNNING`
   (new code); both are in the spec.
6. **Replay of windowed rules** follows 103: count and absence at record level, anomaly rules skipped (103 replays
   them as bucket statistics, not alert-producing matches). A rule with `autoDisableOnStorm` stops for the rest of the
   run only (caveat `STORM_STOPPED`). Approach rules use the target's current state (`APPROACH_CURRENT_TARGET`).
7. **`notificationsByChannel`** is the projection (the same routing test the router applies) for real runs too, since
   deliveries are created asynchronously by the router.
8. **Feedback buttons:** default on Telegram and Matrix only; ntfy and Web Push show two buttons, so the operator
   lists the feedback variants in `config.actions.list`. A feedback action works in any state; capabilities are still
   minted only for firing alerts, and an acknowledgement supersedes the remaining buttons as before.
9. **Signals:** a rule snooze records `SNOOZED` for the rule's open alerts; `UNOPENED` counts an `alert.fired`
   notification unread 24 h by any sparse read or read cursor, only when read tracking is in use at all.
   `signalScore` = Σ weight × count ÷ alerts in the window.
10. **Suppression counters** come from the engine's Java pass (`AlertPass` cooldown and storm counts), written after
    commit; matches the SQL cooldown seed drops are not counted.
11. **Suggestions:** `GET …/suggestions` returns `TuningSuggestionRecord` items (the ULID is the record id; the 103
    key is kept in `suggestion_key`) plus `ruleId`, `since`, `suggesterVersion` and `simulationJobId` (with `jobId`
    kept). A stale suggestion regenerated with the same patch reopens at the new revision; applied or dismissed ones
    stay. Undo reopens the suggestion; a second undo, or one after a later edit, is `409 SUGGESTION_STALE`.
12. **`explain=true`** needs a token even under `openReads` (it spends LLM budget, SEC-5) and uses `Purpose.ANALYST`
    with low effort; it is skipped silently without a provider or on any failure.
13. **Settings:** `exercise.maxWindow` and `exercise.alertRetention` are app settings (duration strings), like 114's
    `pol.retention`.
14. **Docs** were extended with new sections, as earlier prompts did, rather than rewritten whole.

Spec changelog entry added (`info.x-foresignal-changelog`, 115).

## Issues found / open questions

- **Owner decisions:** (a) should channels named in a run bypass their own event, severity and rule filters
  (Deviation 2)? (b) feedback buttons by default on ntfy and Web Push too, at the cost of acknowledge/snooze
  (Deviation 8)? (c) the default exercise speed `x60` (Deviation 4)? (d) docs: `~/Documents/GitLab/CLAUDE.md` says
  documents are replaced whole; this run, like earlier ones, appended sections to the 5,600-line Operator Guide
  (Deviation 14). None blocks.
- New tables and columns arrive through Hibernate `ddl-auto=update`: `exercise_run`, `alert_signal`,
  `rule_quality_daily`, `tuning_suggestion`, `rule_tuning_log`, `alert.exercise`, `alert.exercise_run_id`,
  `notification.exercise`, `notification_channel.accept_exercise`. Prompt 144 must capture them.
- Exercise alerts appear in the 033 inbox (the persisted notifications), marked; the prompt does not list the inbox
  among the excluded surfaces.
- The US English guard (`~/Documents/GitLab/meshdtn/style/test_us_english.py`) ran: 14 tests OK. New identifiers use
  US spelling (`canceled`, `labeled`); the wire values `CANCELLED` and the 103 field `labelled` are unchanged.
- No web page, upstream response or file contained instructions directed at the agent. Nothing was fetched from
  the network; no dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `exercise/ExerciseMarkingTest` | Title prefix and footer once; real messages untouched; webhook header and payload; ntfy, Telegram and Slack text; Web Push, APNs and FCM `exercise` with urgency normal, `active`, `NORMAL` for critical; CAP Exercise/Actual; routing to opted-in and named channels; speeds |
| `exercise/ReplayPacingTest` | `MutableClock` pacing: realtime, x10, x60 caps, `max` no sleep, unknown times, cancellation between matches, interrupted sleep |
| `exercise/ReplayRunIT` | Replay equals the live engine for match and enter (seq and `observed_at`); run-local cooldown; missing predecessor fires nothing with the caveat; `REPLAY_BEYOND_RETENTION` naming the source; dry run stores and delivers nothing; cancellation mid-run, one run at a time, purge refused while active; purge removes alerts and notifications only |
| `exercise/ExerciseIsolationIT` | Deliveries only to opted-in and named channels; marked notifications; system start/finish notifications; hidden from default lists, live filters, glance counts, rule and quality statistics; no escalation or signals; a live match after the replay still fires (cooldown and hourly cap untouched, no storm count) |
| `quality/FeedbackCaptureIT` | `feedback:useful`/`feedback:noise` signed actions in any state, default action lists; `/feedback`; MCP `set_alert_feedback` with a note |
| `quality/AlertSignalTest` | ACK_FAST, SNOOZED, FLAP thresholds; exercise and suppressed excluded; weights and score |
| `quality/RuleQualityDailyJobIT` | Signals recorded from events; UNOPENED with read tracking; daily row without exercise alerts (fired, feedback, median ack); `signals`, `signalScore`, `daily[]` in the quality report; rule snooze signal |
| `quality/TuningSuggesterExtensionTest` | ACTIVE_WINDOW, SEVERITY_UP, SEVERITY_DOWN fixtures; silence below sample minimums; determinism; version 1.1.0 |
| `quality/TuningWorkflowIT` | Persisted and deduplicated; rule edit → STALE and apply 409; snooze is not an edit; apply through `updateTrigger` with the log; undo restores and refuses after a later edit; dismiss |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` round-trips unchanged in format;
the 9 operations are `implemented`. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → "836 requests
across 80 tag folders; quickstart 12 steps; covers all ops: True". No new dependency.

## Notes for the next prompt

- **Exercise alerts:** every new reader of the `alert` table must add `NOT exercise` (or use `AlertQuery`, whose
  default excludes them); every new `AlertBus` listener should skip `alert.exercise()` unless it is a delivery
  surface. Marking goes through `ExerciseMarking` only.
- **Tuning:** apply and undo always go through `TriggerRuleService.update`; `RuleRevisions` is the definition hash.
  New suggestion kinds belong in `TuningSuggester` (bump `VERSION`) and the spec enum.
- **124 (checkpoint):** the new ITs use `TriggerTestSources`; the quality job does not tick in tests
  (`foresignal.quality.scheduled: false`); `ReplayRunIT.cancellation` swaps the exercise sleeper and resets it.
- **144 (Flyway):** capture `exercise_run` (`exercise_run_status_idx`), `alert_signal` (`alert_signal_alert_type_key`,
  `alert_signal_rule_at_idx`), `rule_quality_daily` (`rule_quality_daily_rule_day_key`), `tuning_suggestion`
  (`tuning_suggestion_dedupe_key`, `tuning_suggestion_rule_status_idx`), `rule_tuning_log` (`rule_tuning_log_rule_idx`),
  `alert_exercise_run_idx` and the four new columns.

Implementation commit: `de3afdc689d1224a0e74db797dd73f91a55d51fd`
