-- Edition output targets (prompt NP-004). Additive only.
--
-- briefing.delivery_targets: the edition's DeliveryTarget list (BriefingInput.deliveryTargets, NP-001), each
-- {kind channel|website|podcast, channelId, format email|push|alert|html|audio, options}; null or empty keeps the
-- briefing delivering as before (its channelIds, else every channel subscribed to briefing).
-- briefing_run.delivery_results: one outcome per target of the run (BriefingRun.deliveryResults); null for runs of
-- briefings without targets and for runs stored before NP-004.
alter table briefing add column delivery_targets jsonb;
alter table briefing_run add column delivery_results jsonb;
