/**
 * Metric projections (prompt 038): operator-defined payload fields exposed as Prometheus gauges
 * {@code foresignal_data_<name>} at {@code GET /v1/metrics}, from a registry of their own (never on
 * {@code /actuator/prometheus}).
 */
package ai.foresignal.stats.metrics;
