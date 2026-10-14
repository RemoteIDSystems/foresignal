package ai.foresignal.source.support;

import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Shared helpers for the slow-changing reference datasets (prompt 094): the chunked emission into the ingest
 * writer, the descriptor defaults every reference source keeps, and the geodesic buffer circle of the World
 * Port Index.
 *
 * <p>A reference source is STANDARD with an {@code ALL} policy, no TTL, {@code observedAtMode=RECEIVED},
 * {@code zeroResultsNormal=false}, a weekly interval (min 1 day), a 2/min and 20/day budget and 730 days of
 * history. The long window keeps the retention keyframe ({@code window / 4} ≈ 6 months) from rewriting every
 * record each week.
 */
public final class ReferenceSupport {

    /** The largest chunk handed to {@link FetchContext#emitChunk}. */
    public static final int CHUNK = 5_000;

    /** The default reference retention: 730 days (the {@code reference} analytic class wants at least 90). */
    public static final Duration RETENTION = Duration.ofDays(730);

    /** The weekly reference interval. */
    public static final Duration INTERVAL = Duration.ofDays(7);

    /** The shortest reference interval allowed. */
    public static final Duration MIN_INTERVAL = Duration.ofDays(1);

    /** Mean Earth radius, kilometres. */
    public static final double EARTH_RADIUS_KM = 6371.0088;

    private static final GeometryFactory GEOMETRY = new GeometryFactory();

    private ReferenceSupport() {}

    /** Applies the shared reference descriptor defaults to {@code builder}. */
    public static SourceDescriptor.Builder defaults(SourceDescriptor.Builder builder) {
        return builder.mode(ai.foresignal.source.spi.Mode.POLLING)
                .interval(MIN_INTERVAL, INTERVAL)
                .defaultBudget(2, 20)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .zeroResultsNormal(false);
    }

    /**
     * Emits {@code records} to the executor in chunks of at most {@value #CHUNK}, so no adapter holds a whole
     * dataset in memory and the writer stays fed with back-pressure.
     */
    public static void emit(FetchContext ctx, List<NormalizedRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<NormalizedRecord> buffer = new ArrayList<>(CHUNK);
        for (NormalizedRecord record : records) {
            buffer.add(record);
            if (buffer.size() == CHUNK) {
                ctx.emitChunk(buffer);
                buffer = new ArrayList<>(CHUNK);
            }
        }
        if (!buffer.isEmpty()) {
            ctx.emitChunk(buffer);
        }
    }

    /**
     * A geodesic circle of {@code vertices} points around {@code (lon, lat)}, radius {@code radiusKm}, in
     * SRID 4326. Used for the World Port Index {@code port-area} buffers (prompt 094).
     */
    public static Polygon geodesicCircle(double lon, double lat, double radiusKm, int vertices) {
        int points = Math.max(8, vertices);
        double angular = radiusKm / EARTH_RADIUS_KM;
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        Coordinate[] ring = new Coordinate[points + 1];
        for (int i = 0; i < points; i++) {
            double bearing = 2 * Math.PI * i / points;
            double sinLat = Math.sin(latRad) * Math.cos(angular)
                    + Math.cos(latRad) * Math.sin(angular) * Math.cos(bearing);
            double pointLat = Math.asin(Math.max(-1, Math.min(1, sinLat)));
            double pointLon = lonRad + Math.atan2(
                    Math.sin(bearing) * Math.sin(angular) * Math.cos(latRad),
                    Math.cos(angular) - Math.sin(latRad) * Math.sin(pointLat));
            ring[i] = new Coordinate(Math.toDegrees(pointLon), Math.toDegrees(pointLat));
        }
        ring[points] = new Coordinate(ring[0]);
        LinearRing shell = GEOMETRY.createLinearRing(ring);
        Polygon polygon = GEOMETRY.createPolygon(shell);
        polygon.setSRID(Geo.SRID);
        return polygon;
    }
}
