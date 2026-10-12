/**
 * Personal-location ingest formats (prompt 039): OwnTracks HTTP mode, OsmAnd/Traccar Client, Overland
 * and GPSLogger custom URL. Each is an {@link ai.foresignal.inbound.format.IngestFormat} that owns its
 * field mapping and its native reply; every fix becomes a record of kind {@code tracker}.
 */
package ai.foresignal.inbound.formats.location;
