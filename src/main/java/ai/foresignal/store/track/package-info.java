/**
 * Entity tracks (prompt 037): {@code GET /v1/records/{sourceId}/{externalId}/track} and the MCP
 * {@code get_track} tool. {@link ai.foresignal.store.track.TrackService} reads one entity's versions
 * on the {@code (source_id, external_id, seq)} index and appends the live position;
 * {@link ai.foresignal.store.track.TrackSimplifier} segments, simplifies and splits the trail at the
 * antimeridian.
 */
package ai.foresignal.store.track;
