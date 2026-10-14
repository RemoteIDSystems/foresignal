/**
 * The CDC National Wastewater Surveillance System sources (prompt 095): {@code cdc-nwss-wval} site viral-activity
 * levels and {@code cdc-nwss-detections} pathogen detections, read through a shared {@link
 * ai.foresignal.source.adapters.cdc.SodaClient}. The older datasets {@code 2ew6-ywp6} and {@code g653-rqe2} were
 * archived on 2025-09-12; the current datasets update weekly. Records are placed at county or state interior
 * points with {@code geometryApproximate}. WastewaterSCAN rows are CC BY-NC 4.0 and are dropped unless
 * {@code includeWastewaterScan} is set.
 */
package ai.foresignal.source.adapters.cdc;
