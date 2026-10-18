/**
 * CZML output for CesiumJS (prompt 141): {@code GET /v1/history?format=czml},
 * {@code GET /v1/records/{sourceId}/{externalId}/track?format=czml} and the MCP {@code get_track} tool's
 * {@code format: czml} media link.
 *
 * <p>{@link ai.foresignal.czml.CzmlWriter} is pure: a document packet first (the clock from the requested range and
 * the attribution in its {@code description}), then one packet per entity with a sampled
 * {@code cartographicDegrees} position, its {@code availability}, a {@code label} carrying the title and a
 * {@code point} colored from the kind's {@code kindDisplay}. {@link ai.foresignal.czml.CzmlService} turns history rows
 * and tracks into those entities and caps them at {@value ai.foresignal.czml.CzmlWriter#MAX_ENTITIES}.
 */
package ai.foresignal.czml;
