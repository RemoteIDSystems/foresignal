/**
 * The eBird source {@code ebird-notable} (prompt 095): notable bird sightings near a point from the eBird API
 * 2.0, as {@code bird-sighting} records. Disabled by default, needs a per-account key, keeps local wall-clock
 * times as {@code obsDtLocal} (mapped to {@code eventTime} through the 040 timezone) and rounds private
 * locations. Non-commercial with attribution; {@code LOCAL_ONLY} for 109/110.
 */
package ai.foresignal.source.adapters.ebird;
