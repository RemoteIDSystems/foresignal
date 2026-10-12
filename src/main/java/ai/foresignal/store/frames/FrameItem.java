package ai.foresignal.store.frames;

import java.time.Instant;

/**
 * One entity in a keyframe or an upsert (the {@code FrameItem} schema).
 *
 * @param id {@code <sourceId>:<externalId>}
 * @param kind the kind
 * @param title the title resolved from the version payload, or null
 * @param observedAt the version's observation time
 * @param version the version number
 * @param lon the centroid longitude ({@code geometry=point}), or null
 * @param lat the centroid latitude, or null
 * @param geometry the GeoJSON geometry text ({@code geometry=full}), or null
 * @param props the version's tile properties as JSON object text
 */
public record FrameItem(String id, String kind, String title, Instant observedAt, int version, Double lon, Double lat,
        String geometry, String props) {}
