/**
 * The {@code transit-*} source families (prompt 070): GTFS-Realtime VehiclePositions as TRACK
 * {@code transit-vehicle} records and ServiceAlerts as {@code transit-alert} records, from the code-reviewed
 * {@link ai.foresignal.source.adapters.transit.TransitFeeds} list and from feeds the operator enables by id out of
 * the vendored Mobility Database snapshot ({@link ai.foresignal.source.adapters.transit.MobilityCatalog}).
 *
 * <p>No transit URL ever comes from an API caller: the feed list is code, the snapshot is a reviewed resource, and a
 * catalog feed is enabled only through a bootstrap property and a restart.
 */
package ai.foresignal.source.adapters.transit;
