/**
 * The {@code gbfs-*} source family (prompt 071): docked bikeshare stations of the code-reviewed
 * {@link ai.foresignal.source.adapters.gbfs.GbfsSystems} list, merged from GBFS {@code station_information} and
 * {@code station_status} (v1.x, v2.x and v3.0 documents) into one {@code bikeshare-station} record per station.
 *
 * <p>Only reviewed URLs are requested: no {@code gbfs.json} discovery, no caller URLs, no redirects.
 */
package ai.foresignal.source.adapters.gbfs;
