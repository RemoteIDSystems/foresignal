-- Foresignal runtime measurements (prompt 143), for psql against the broker's database.
--
-- Run it through scripts/verify/measure.sh, which pipes it into `docker compose exec -T db psql`.
-- Every query is read-only. Nothing here creates an extension, a table or a setting: pgstattuple
-- bloat figures are printed only when the extension is already installed.
--
-- The window for rates is the psql variable `hours` (default 1); measure.sh passes -v hours=N.
-- Projections to compare against (docs/Core-Data-Model.md §15-§16, docs/Weather-Imagery-Design.md §9):
--   history writes 10-15 GB/day, WAL 15-20 GB/day, 3 d TRACK window about 40M rows / 20 GB,
--   TRIGGER_EDGE share warn above 5%, versions/min warn above 20,000 (NFR-2 sustains 20,000).

\set ON_ERROR_STOP on
\pset footer off
\if :{?hours}
\else
  \set hours 1
\endif

\echo '== 0. Server and time =='
SELECT now() AS measured_at, version() AS postgres, current_setting('max_wal_size') AS max_wal_size,
       current_setting('checkpoint_timeout') AS checkpoint_timeout, current_setting('shared_buffers') AS shared_buffers,
       current_setting('wal_compression') AS wal_compression;

\echo '== 1. Ingest per source over the window (source_usage) =='
-- observations/min, versions/min, persistRatio, triggerEdgeShare, late and clamped per hour.
SELECT u.source_id,
       round(sum(u.records_seen)::numeric / (:hours * 60), 1)                    AS observations_per_min,
       round(sum(u.versions)::numeric / (:hours * 60), 1)                        AS versions_per_min,
       round(sum(u.versions)::numeric / nullif(sum(u.records_seen), 0), 4)       AS persist_ratio,
       round(sum(u.trigger_edges)::numeric / nullif(sum(u.versions), 0), 4)      AS trigger_edge_share,
       round(sum(u.late)::numeric / :hours, 1)                                   AS late_per_hour,
       round(sum(u.clamped)::numeric / :hours, 1)                                AS clamped_per_hour,
       sum(u.requests)                                                           AS requests,
       sum(u.errors)                                                             AS errors,
       sum(u.refused)                                                            AS refused,
       pg_size_pretty(sum(u.bytes))                                              AS downloaded,
       pg_size_pretty(sum(u.version_bytes))                                      AS version_bytes
  FROM source_usage u
 WHERE u.minute >= now() - make_interval(hours => :hours)
 GROUP BY u.source_id
 ORDER BY versions_per_min DESC NULLS LAST;

\echo '== 1b. Ingest totals over the window =='
SELECT round(sum(records_seen)::numeric / (:hours * 60), 1)                AS observations_per_min,
       round(sum(versions)::numeric / (:hours * 60), 1)                    AS versions_per_min,
       round(sum(versions)::numeric / nullif(sum(records_seen), 0), 4)     AS persist_ratio,
       round(sum(trigger_edges)::numeric / nullif(sum(versions), 0), 4)    AS trigger_edge_share,
       pg_size_pretty(sum(version_bytes))                                  AS version_bytes_in_window,
       pg_size_pretty((sum(version_bytes) * 24.0 / :hours)::bigint)        AS version_bytes_per_day_projected
  FROM source_usage
 WHERE minute >= now() - make_interval(hours => :hours);

\echo '== 1c. Peak versions/min in the window =='
SELECT minute, sum(versions) AS versions
  FROM source_usage
 WHERE minute >= now() - make_interval(hours => :hours)
 GROUP BY minute
 ORDER BY versions DESC
 LIMIT 5;

\echo '== 1d. Persist reasons over the window (record_version) =='
-- 1 FIRST, 2 REAPPEAR, 3 SIGNIFICANT, 4 TOMBSTONE, 5 CHANGE, 6 SAMPLE, 7 KEYFRAME, 8 HEARTBEAT, 9 TRIGGER_EDGE
SELECT history_tier, persist_reason, count(*) AS versions
  FROM record_version
 WHERE seq >= coalesce((SELECT min(first_seq) FROM seq_clock WHERE minute >= now() - make_interval(hours => :hours)), 0)
 GROUP BY history_tier, persist_reason
 ORDER BY history_tier, persist_reason;

\echo '== 2. Fetch etiquette over the window (fetch_log) =='
-- 304s where conditional GET is supported; 429/503 should be followed by a backoff, not a retry storm.
SELECT source_id,
       count(*)                                              AS fetches,
       count(*) FILTER (WHERE status = 'NOT_MODIFIED')       AS not_modified,
       count(*) FILTER (WHERE http_status = 304)             AS http_304,
       count(*) FILTER (WHERE http_status = 429)             AS http_429,
       count(*) FILTER (WHERE http_status = 503)             AS http_503,
       count(*) FILTER (WHERE http_status = 451)             AS http_451,
       count(*) FILTER (WHERE http_status = 403)             AS http_403,
       count(*) FILTER (WHERE status = 'FAILED')             AS failed,
       count(*) FILTER (WHERE status = 'REFUSED')            AS refused
  FROM fetch_log
 WHERE started_at >= now() - make_interval(hours => :hours)
 GROUP BY source_id
 ORDER BY fetches DESC;

\echo '== 2b. Requests after a 429 or 503, per source (should be spaced by Retry-After) =='
SELECT source_id, started_at, http_status,
       lead(started_at) OVER (PARTITION BY source_id ORDER BY started_at) - started_at AS gap_to_next_fetch
  FROM fetch_log
 WHERE started_at >= now() - make_interval(hours => :hours)
   AND http_status IN (429, 503)
 ORDER BY source_id, started_at
 LIMIT 50;

\echo '== 2c. Source health =='
SELECT * FROM source_health ORDER BY 1;

\echo '== 3. Storage: hot tables =='
SELECT relname,
       pg_size_pretty(pg_total_relation_size(relid))  AS total,
       pg_size_pretty(pg_relation_size(relid))        AS heap,
       n_live_tup, n_dead_tup,
       n_tup_upd, n_tup_hot_upd,
       round(n_tup_hot_upd::numeric / nullif(n_tup_upd, 0), 4) AS hot_update_ratio,
       last_autovacuum, last_autoanalyze
  FROM pg_stat_user_tables
 WHERE relname IN ('record_head', 'record_live', 'source_usage', 'fetch_log', 'seq_clock', 'activity_rollup')
 ORDER BY pg_total_relation_size(relid) DESC;

\echo '== 3b. Storage: history partitions per tier =='
SELECT p.relname                                 AS tier_parent,
       count(*)                                  AS leaves,
       sum(s.n_live_tup)                         AS live_rows_estimate,
       pg_size_pretty(sum(pg_total_relation_size(c.oid))) AS total
  FROM pg_inherits i
  JOIN pg_class c ON c.oid = i.inhrelid
  JOIN pg_class p ON p.oid = i.inhparent
  JOIN pg_namespace n ON n.oid = p.relnamespace
  LEFT JOIN pg_stat_user_tables s ON s.relid = c.oid
 WHERE n.nspname = current_schema()
   AND p.relname IN ('record_version_std', 'record_version_trk')
 GROUP BY p.relname
 ORDER BY p.relname;

\echo '== 3c. Storage: history leaves with bounds (newest last) =='
SELECT p.relname AS tier_parent, c.relname AS leaf, pg_get_expr(c.relpartbound, c.oid) AS bounds,
       i.inhdetachpending AS detach_pending, pg_size_pretty(pg_total_relation_size(c.oid)) AS total
  FROM pg_inherits i
  JOIN pg_class c ON c.oid = i.inhrelid
  JOIN pg_class p ON p.oid = i.inhparent
  JOIN pg_namespace n ON n.oid = p.relnamespace
 WHERE n.nspname = current_schema()
   AND p.relname IN ('record_version_std', 'record_version_trk')
 ORDER BY p.relname, c.relname;

\echo '== 3d. Seq allocator against the newest stored seq (record_seq must be >= max(seq)) =='
SELECT (SELECT last_value FROM record_seq) AS record_seq_last_value,
       (SELECT max(seq) FROM record_version) AS max_version_seq;

\echo '== 3e. seq_clock span (pruned by retention) =='
SELECT count(*) AS minutes, min(minute) AS oldest, max(minute) AS newest FROM seq_clock;

\echo '== 3f. Database size =='
SELECT pg_size_pretty(pg_database_size(current_database())) AS database_size;

SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pgstattuple') AS has_pgstattuple \gset
\if :has_pgstattuple
\echo '== 3g. Bloat (pgstattuple_approx) =='
SELECT 'record_head' AS relname, * FROM pgstattuple_approx('record_head')
UNION ALL
SELECT 'record_live', * FROM pgstattuple_approx('record_live');
\else
\echo '== 3g. Bloat: pgstattuple is not installed; skipped (this script never installs it) =='
\endif

\echo '== 4. WAL counters (take two samples and diff; measure.sh wal-sample does this) =='
SELECT now() AS sampled_at, pg_current_wal_lsn() AS lsn, wal_records, wal_fpi, wal_bytes,
       pg_size_pretty(wal_bytes) AS wal_bytes_pretty, stats_reset
  FROM pg_stat_wal;

\echo '== 4b. Checkpoints (pg_stat_checkpointer, PostgreSQL 17) =='
SELECT num_timed, num_requested, write_time, sync_time, buffers_written, stats_reset
  FROM pg_stat_checkpointer;

\echo '== 5. Activity rollup coverage =='
SELECT count(*) AS rows, pg_size_pretty(pg_total_relation_size('activity_rollup')) AS total
  FROM activity_rollup;
