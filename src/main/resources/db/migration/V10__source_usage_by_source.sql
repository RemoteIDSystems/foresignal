-- Per-source usage windows (health, budgets, /v1/system): source_id = ? AND minute in [from, to).
-- The primary key leads with minute, so each such query scanned every source's rows in the window.
create index source_usage_source_minute_idx on source_usage (source_id, minute);
