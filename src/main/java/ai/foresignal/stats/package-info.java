/**
 * Activity statistics (prompt 038): the {@code activity_rollup} table and the job that fills it from a
 * durable seq cursor, the stats API (time series, grids, top-N, count mode), the rollup-backed
 * timeline backend, the glance document and the Prometheus metric projections.
 *
 * <p>Rollup buckets are on the <em>transaction axis</em>: hour {@code H} covers the seqs ingested in
 * {@code [H, H + 1 h)}, found through {@code seq_clock}. That axis is exact, prunes the seq-partitioned
 * history, and is the only one still computable after retention has deleted the raw versions; stats on
 * the observed, effective or event axes are served from raw history inside retention.
 */
package ai.foresignal.stats;
