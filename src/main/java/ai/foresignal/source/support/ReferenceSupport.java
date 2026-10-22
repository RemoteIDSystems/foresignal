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
import java.util.function.Consumer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

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

    /** Mean Earth radius, kilometers. */
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
     * Emits {@code records} to the executor in chunks of at most {@value #CHUNK}. The datasets' own adapters
     * stream through {@link #chunks} instead and never build the whole list; this remains for small fixed sets
     * and test adapters.
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
     * A record sink that buffers at most {@value #CHUNK} records and hands each full buffer to the executor's
     * {@link FetchContext#emitChunk}, so a streaming parser feeds the writer chunk by chunk and no adapter holds
     * the whole dataset as records (prompt 094). Call {@link ChunkEmitter#flush} once the parse is done.
     */
    public static ChunkEmitter chunks(FetchContext ctx) {
        return new ChunkEmitter(ctx::emitChunk);
    }

    /** Buffers records into chunks of at most {@value #CHUNK} and hands each to a chunk sink. */
    public static final class ChunkEmitter implements Consumer<NormalizedRecord> {

        private final Consumer<List<NormalizedRecord>> sink;
        private List<NormalizedRecord> buffer = new ArrayList<>();
        private int emitted;

        /**
         * Creates the emitter.
         *
         * @param sink receives each chunk, for example {@code ctx::emitChunk}
         */
        public ChunkEmitter(Consumer<List<NormalizedRecord>> sink) {
            this.sink = sink;
        }

        /** Buffers {@code record}, handing the buffer over once it holds {@value ReferenceSupport#CHUNK} records. */
        @Override
        public void accept(NormalizedRecord record) {
            buffer.add(record);
            emitted++;
            if (buffer.size() >= CHUNK) {
                flush();
            }
        }

        /** Hands over whatever is buffered. */
        public void flush() {
            if (!buffer.isEmpty()) {
                List<NormalizedRecord> chunk = buffer;
                buffer = new ArrayList<>();
                sink.accept(chunk);
            }
        }

        /** The records accepted so far. */
        public int emitted() {
            return emitted;
        }
    }

    /**
     * What one streaming parse of a reference file produced.
     *
     * @param records the records handed to the sink
     * @param skipped the rows or features skipped (no identifier)
     * @param badGeometry the records kept without geometry because theirs was malformed
     */
    public record ParseCounts(int records, int skipped, int badGeometry) {}

    /**
     * {@link #geodesicCircle} as an antimeridian-safe area: a circle that crosses longitude ±180 is split at
     * it into a MultiPolygon whose parts both lie within [-180, 180], instead of a polygon with vertices past
     * ±180 (2026-10 review). Used for the World Port Index {@code port-area} buffers.
     */
    public static Geometry geodesicArea(double lon, double lat, double radiusKm, int vertices) {
        Polygon circle = geodesicCircle(lon, lat, radiusKm, vertices);
        Envelope env = circle.getEnvelopeInternal();
        if (env.getMinX() >= -180 && env.getMaxX() <= 180) {
            return circle;
        }
        Polygon world = GEOMETRY.createPolygon(new Coordinate[] {
            new Coordinate(-180, -90), new Coordinate(180, -90), new Coordinate(180, 90),
            new Coordinate(-180, 90), new Coordinate(-180, -90)});
        double shift = env.getMaxX() > 180 ? -360 : 360;
        Geometry inside = circle.intersection(world);
        Geometry wrapped = AffineTransformation.translationInstance(shift, 0).transform(circle).intersection(world);
        List<Polygon> parts = new ArrayList<>();
        collect(inside, parts);
        collect(wrapped, parts);
        Geometry out = parts.size() == 1 ? parts.get(0) : GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
        out.setSRID(Geo.SRID);
        return out;
    }

    private static void collect(Geometry geometry, List<Polygon> parts) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            if (part instanceof Polygon polygon && !polygon.isEmpty()) {
                parts.add(polygon);
            } else if (part instanceof MultiPolygon nested) {
                collect(nested, parts);
            }
        }
    }

    /**
     * A geodesic circle of {@code vertices} points around {@code (lon, lat)}, radius {@code radiusKm}, in
     * SRID 4326. The center longitude is normalized into [-180, 180); the ring itself is continuous, so near
     * the antimeridian its vertices may pass ±180 (use {@link #geodesicArea} for a split area).
     */
    public static Polygon geodesicCircle(double lon, double lat, double radiusKm, int vertices) {
        int points = Math.max(8, vertices);
        double angular = radiusKm / EARTH_RADIUS_KM;
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(normalizeLongitude(lon));
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

    /** {@code lon} in [-180, 180): 190 becomes -170. */
    public static double normalizeLongitude(double lon) {
        if (lon >= -180 && lon < 180) {
            return lon;
        }
        return ((lon + 180) % 360 + 360) % 360 - 180;
    }
}
