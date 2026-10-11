package ai.foresignal.ingest;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.RecordLimits;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

/**
 * Turns one batch of adapter records into accepted {@link Observation}s and {@link Rejection}s
 * (Core-Data-Model §2, §3, §14). Pure: no database, clock or Spring.
 *
 * <p>Per record, in order:
 *
 * <ol>
 *   <li>{@code externalId} non-blank and at most 256 characters; {@code kind} one of the
 *       descriptor's kinds.
 *   <li>Time base: under {@code observedAtMode=RECEIVED}, {@code observed_at = received_at} and
 *       {@link QaFlags#TIME_IMPUTED}, ignoring any adapter value; under UPSTREAM a missing
 *       {@code observedAt} is rejected ({@code MISSING_OBSERVED_AT}).
 *   <li>Clamp: a future {@code observed_at} is set to {@code received_at}, flagged
 *       {@link QaFlags#FUTURE_CLAMPED} and counted only when it exceeded {@code skewTolerance};
 *       {@code observed_at} before 1970-01-02 is rejected.
 *   <li>Limits ({@link RecordLimits}): payload size, depth, key count, string length, finite
 *       numbers, observers, and geometry vertices before repair. Null map values are dropped.
 *   <li>Geometry: SRID 4326, forced to 2D, out-of-range coordinates rejected, invalid geometry
 *       repaired by {@link GeometryFixer} ({@link QaFlags#GEOMETRY_FIXED}; empty becomes null),
 *       an exact (0,0) point removed ({@link QaFlags#NULL_ISLAND}) unless the source allows it.
 *   <li>Private sources: coordinates reduced to {@code privatePrecisionDecimals}, before hashing.
 *   <li>Quality (§14): {@code accuracyMeters ≥ 0}, {@code confidence ∈ [0,1]}, {@code extra} keys
 *       {@code ^[a-z0-9]+\.[a-z0-9_]+$} with scalar values.
 *   <li>Validity: {@code validFrom ≤ validUntil}; the explicit {@code validUntil} is dropped on
 *       TRACK sources (counted); effective {@code valid_until = explicit ?? anchor + ttl}.
 *   <li>Tombstones: no geometry, empty payload, kind kept.
 *   <li>Derived head fields: search text, title ({@link TitleResolver}) and entity keys
 *       ({@link EntityKeyExtractor}).
 * </ol>
 *
 * <p>A rejection never fails the batch. Accepted observations are stable-sorted by
 * {@code (externalId, observedAt, arrival)}; <strong>duplicates are kept</strong>, because the
 * decision table decides what a repeated observation means.
 *
 * <p>Tombstones are normalized (step 9) before the payload and geometry steps, so a tombstone is
 * never rejected for content it is about to discard.
 */
public final class Normalizer {

    /** The longest external id accepted. */
    public static final int MAX_EXTERNAL_ID_LENGTH = 256;

    /** The earliest acceptable {@code observed_at}. */
    public static final Instant EPOCH_FLOOR = Instant.parse("1970-01-02T00:00:00Z");

    /** The longest search text kept. */
    public static final int MAX_SEARCH_TEXT_LENGTH = 1_000;

    /** Keys of {@code quality.extra}: a namespace, a dot, a name. */
    public static final Pattern QUALITY_EXTRA_KEY = Pattern.compile("^[a-z0-9]+\\.[a-z0-9_]+$");

    private static final GeometryFactory FACTORY = Geo.factory();

    private static final Comparator<Observation> ORDER =
            Comparator.comparing(Observation::externalId)
                    .thenComparing(Observation::observedAt)
                    .thenComparingLong(Observation::arrival);

    private final EntityKeyExtractor entityKeyExtractor;

    /** A normalizer using {@link DescriptorEntityKeyExtractor}. */
    public Normalizer() {
        this(DescriptorEntityKeyExtractor.INSTANCE);
    }

    /** A normalizer with an explicit entity key extractor. */
    public Normalizer(EntityKeyExtractor entityKeyExtractor) {
        this.entityKeyExtractor =
                entityKeyExtractor == null ? DescriptorEntityKeyExtractor.INSTANCE : entityKeyExtractor;
    }

    /**
     * Normalizes {@code records} for the source of {@code profile}.
     *
     * @return accepted observations (sorted), rejections (in submission order) and counts
     */
    public NormalizedBatch normalize(SourceIngestProfile profile, List<IngestRecord> records) {
        List<Observation> accepted = new ArrayList<>(records == null ? 0 : records.size());
        List<Rejection> rejections = new ArrayList<>();
        Counts counts = new Counts();
        if (records != null) {
            for (int i = 0; i < records.size(); i++) {
                IngestRecord in = records.get(i);
                try {
                    accepted.add(normalizeOne(profile, in, i, counts));
                } catch (Rejected rejected) {
                    String externalId = in == null || in.record() == null ? null : in.record().externalId();
                    rejections.add(new Rejection(i, externalId, rejected.reason, rejected.getMessage()));
                }
            }
        }
        accepted.sort(ORDER);
        return new NormalizedBatch(accepted, rejections, counts.clamped, counts.validUntilIgnored);
    }

    private Observation normalizeOne(SourceIngestProfile p, IngestRecord in, int index, Counts counts) {
        if (in == null) {
            throw new Rejected(RejectionReason.EXTERNAL_ID_INVALID, "record is null");
        }
        NormalizedRecord record = in.record();
        RecordLimits limits = p.limits();
        int flags = 0;

        // 1. Identity.
        String externalId = record.externalId();
        if (externalId == null || externalId.isBlank() || externalId.length() > MAX_EXTERNAL_ID_LENGTH) {
            throw new Rejected(
                    RejectionReason.EXTERNAL_ID_INVALID,
                    "externalId must be non-blank and at most " + MAX_EXTERNAL_ID_LENGTH + " characters");
        }
        if (!p.kinds().contains(record.kind())) {
            throw new Rejected(
                    RejectionReason.UNKNOWN_KIND,
                    "kind '" + record.kind() + "' is not one of " + p.kinds());
        }

        // 2. Time base.
        Instant receivedAt = in.receivedAt();
        Instant observedAt;
        if (p.observedAtMode() == ObservedAtMode.RECEIVED) {
            observedAt = receivedAt;
            flags |= QaFlags.TIME_IMPUTED;
        } else {
            observedAt = record.observedAt();
            if (observedAt == null) {
                throw new Rejected(RejectionReason.MISSING_OBSERVED_AT, "observedAt is required");
            }
        }

        // 3. Clamp and floor.
        if (observedAt.isAfter(receivedAt)) {
            Duration excess = Duration.between(receivedAt, observedAt);
            observedAt = receivedAt;
            if (excess.compareTo(p.skewTolerance()) > 0) {
                flags |= QaFlags.FUTURE_CLAMPED;
                counts.clamped++;
            }
        }
        if (observedAt.isBefore(EPOCH_FLOOR)) {
            throw new Rejected(
                    RejectionReason.OBSERVED_AT_TOO_OLD, "observedAt " + observedAt + " is before " + EPOCH_FLOOR);
        }

        // 4. Limits (observers apply to tombstones too).
        if (record.observers().size() > limits.maxObservers()) {
            throw new Rejected(
                    RejectionReason.TOO_MANY_OBSERVERS,
                    record.observers().size() + " observers; the limit is " + limits.maxObservers());
        }

        boolean tombstone = record.tombstone();
        Map<String, Object> payload;
        Geometry geometry;
        if (tombstone) {
            // 9. Tombstone normalization, applied first: its content is discarded anyway.
            payload = Map.of();
            geometry = null;
        } else {
            payload = checkedPayload(record.payload(), limits);
            // 5–6. Geometry.
            Geometry[] holder = {record.geometry()};
            flags |= normalizeGeometry(holder, p);
            geometry = holder[0];
        }

        // 7. Quality.
        checkQuality(record.quality());

        // 8. Validity.
        Instant validFrom = record.validFrom();
        Instant explicit = record.validUntil();
        if (validFrom != null && explicit != null && validFrom.isAfter(explicit)) {
            throw new Rejected(
                    RejectionReason.VALID_RANGE_INVERTED,
                    "validFrom " + validFrom + " is after validUntil " + explicit);
        }
        if (p.isTrack() && explicit != null) {
            explicit = null;
            counts.validUntilIgnored++;
        }
        Instant validUntil = explicit;
        if (validUntil == null && p.ttl() != null) {
            validUntil = p.anchor(observedAt, receivedAt).plus(p.ttl());
        }

        // 10. Derived head fields.
        String searchText = tombstone ? null : searchText(p.searchFields(), payload);
        String title = tombstone ? null : TitleResolver.resolve(p, payload);
        Map<String, String> entityKeys = tombstone ? Map.of() : entityKeyExtractor.extract(p, payload);

        return new Observation(
                index,
                in.arrival(),
                externalId,
                record.kind(),
                observedAt,
                receivedAt,
                record.eventTime(),
                geometry,
                explicit,
                validUntil,
                validFrom,
                payload,
                tombstone,
                record.quality(),
                record.observers(),
                flags,
                searchText,
                title,
                entityKeys);
    }

    // ---------- payload ----------

    private static Map<String, Object> checkedPayload(Map<String, Object> raw, RecordLimits limits) {
        Map<String, Object> payload;
        try {
            payload = JsonValues.normalize(raw);
        } catch (JsonValues.UnsupportedValueException e) {
            throw new Rejected(RejectionReason.PAYLOAD_UNSUPPORTED_TYPE, e.getMessage());
        }
        int[] keys = {0};
        walk(payload, 1, limits, keys);
        int bytes = CanonicalJson.bytes(payload).length;
        if (bytes > limits.maxPayloadBytes()) {
            throw new Rejected(
                    RejectionReason.PAYLOAD_TOO_LARGE,
                    "payload is " + bytes + " bytes; the limit is " + limits.maxPayloadBytes());
        }
        return payload;
    }

    /** Checks depth, total keys, string lengths and finiteness; {@code depth} counts containers. */
    private static void walk(Object value, int depth, RecordLimits limits, int[] keys) {
        switch (value) {
            case Map<?, ?> map -> {
                checkDepth(depth, limits);
                keys[0] += map.size();
                if (keys[0] > limits.maxPayloadKeys()) {
                    throw new Rejected(
                            RejectionReason.PAYLOAD_TOO_MANY_KEYS,
                            "payload has more than " + limits.maxPayloadKeys() + " keys");
                }
                for (Object child : map.values()) {
                    walk(child, depth + 1, limits, keys);
                }
            }
            case Collection<?> list -> {
                checkDepth(depth, limits);
                for (Object child : list) {
                    walk(child, depth + 1, limits, keys);
                }
            }
            case String s -> {
                // Measured in UTF-8 bytes; the character count is a cheap first check.
                if (s.length() > limits.maxStringLength()
                        || s.getBytes(StandardCharsets.UTF_8).length > limits.maxStringLength()) {
                    throw new Rejected(
                            RejectionReason.PAYLOAD_STRING_TOO_LONG,
                            "a payload string exceeds " + limits.maxStringLength() + " bytes");
                }
            }
            case Double d -> {
                if (!Double.isFinite(d)) {
                    throw new Rejected(RejectionReason.PAYLOAD_NON_FINITE_NUMBER, "payload holds " + d);
                }
            }
            case null, default -> {
                // Booleans, Longs, BigIntegers, BigDecimals and JSON nulls inside arrays are fine.
            }
        }
    }

    private static void checkDepth(int depth, RecordLimits limits) {
        if (depth > limits.maxPayloadDepth()) {
            throw new Rejected(
                    RejectionReason.PAYLOAD_TOO_DEEP,
                    "payload nests deeper than " + limits.maxPayloadDepth());
        }
    }

    // ---------- geometry ----------

    /** Normalizes {@code holder[0]} in place and returns the flags it earned. */
    private static int normalizeGeometry(Geometry[] holder, SourceIngestProfile p) {
        Geometry geometry = holder[0];
        int flags = 0;
        if (geometry == null || geometry.isEmpty()) {
            holder[0] = null;
            return 0;
        }
        RecordLimits limits = p.limits();
        if (geometry.getNumPoints() > limits.maxGeometryVertices()) {
            throw new Rejected(
                    RejectionReason.GEOMETRY_TOO_MANY_VERTICES,
                    geometry.getNumPoints() + " vertices; the limit is " + limits.maxGeometryVertices());
        }
        if (geometry.getSRID() != 0 && geometry.getSRID() != Geo.SRID) {
            throw new Rejected(
                    RejectionReason.GEOMETRY_WRONG_SRID, "geometry SRID is " + geometry.getSRID() + ", not 4326");
        }
        geometry = force2d(geometry);
        if (!geometry.isValid()) {
            geometry = GeometryFixer.fix(geometry);
            flags |= QaFlags.GEOMETRY_FIXED;
            if (geometry == null || geometry.isEmpty()) {
                holder[0] = null;
                return flags;
            }
        }
        if (!p.allowNullIsland() && Geo.isNullIsland(geometry)) {
            holder[0] = null;
            return flags | QaFlags.NULL_ISLAND;
        }
        if (p.privateSource()) {
            PrecisionModel model = new PrecisionModel(Math.pow(10, p.privatePrecisionDecimals()));
            geometry = GeometryPrecisionReducer.reduce(geometry, model);
            if (geometry == null || geometry.isEmpty()) {
                holder[0] = null;
                return flags;
            }
        }
        geometry.setSRID(Geo.SRID);
        holder[0] = geometry;
        return flags;
    }

    /**
     * A two-dimensional copy in SRID 4326 (Z and M dropped; vertical values belong in the payload),
     * rejecting coordinates outside ±180 / ±90 or not finite.
     */
    static Geometry force2d(Geometry geometry) {
        return switch (geometry) {
            case Point point -> FACTORY.createPoint(xy(point.getCoordinate()));
            case LinearRing ring -> FACTORY.createLinearRing(xy(ring.getCoordinates()));
            case LineString line -> FACTORY.createLineString(xy(line.getCoordinates()));
            case Polygon polygon -> {
                LinearRing shell = FACTORY.createLinearRing(xy(polygon.getExteriorRing().getCoordinates()));
                LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
                for (int i = 0; i < holes.length; i++) {
                    holes[i] = FACTORY.createLinearRing(xy(polygon.getInteriorRingN(i).getCoordinates()));
                }
                yield FACTORY.createPolygon(shell, holes);
            }
            case MultiPoint multi -> FACTORY.createMultiPoint(parts(multi, Point[]::new));
            case MultiLineString multi -> FACTORY.createMultiLineString(parts(multi, LineString[]::new));
            case MultiPolygon multi -> FACTORY.createMultiPolygon(parts(multi, Polygon[]::new));
            case GeometryCollection collection ->
                    FACTORY.createGeometryCollection(parts(collection, Geometry[]::new));
            default ->
                    throw new Rejected(
                            RejectionReason.COORDINATES_OUT_OF_RANGE,
                            "unsupported geometry type " + geometry.getGeometryType());
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Geometry> T[] parts(
            GeometryCollection collection, java.util.function.IntFunction<T[]> array) {
        T[] parts = array.apply(collection.getNumGeometries());
        for (int i = 0; i < parts.length; i++) {
            parts[i] = (T) force2d(collection.getGeometryN(i));
        }
        return parts;
    }

    private static Coordinate[] xy(Coordinate[] coordinates) {
        Coordinate[] result = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = xy(coordinates[i]);
        }
        return result;
    }

    private static Coordinate xy(Coordinate c) {
        if (c == null) {
            return null;
        }
        double x = c.getX();
        double y = c.getY();
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < -180.0 || x > 180.0 || y < -90.0 || y > 90.0) {
            throw new Rejected(
                    RejectionReason.COORDINATES_OUT_OF_RANGE, "coordinate (" + x + ", " + y + ") is out of range");
        }
        return new CoordinateXY(x, y);
    }

    // ---------- quality ----------

    private static void checkQuality(Quality quality) {
        if (quality == null) {
            return;
        }
        Double accuracy = quality.accuracyMeters();
        if (accuracy != null && !(accuracy >= 0 && Double.isFinite(accuracy))) {
            throw new Rejected(RejectionReason.QUALITY_INVALID, "accuracyMeters must be >= 0, but was " + accuracy);
        }
        Double confidence = quality.confidence();
        if (confidence != null && !(confidence >= 0 && confidence <= 1)) {
            throw new Rejected(
                    RejectionReason.QUALITY_INVALID, "confidence must be within [0, 1], but was " + confidence);
        }
        for (Map.Entry<String, Object> entry : quality.extra().entrySet()) {
            if (entry.getKey() == null || !QUALITY_EXTRA_KEY.matcher(entry.getKey()).matches()) {
                throw new Rejected(
                        RejectionReason.QUALITY_INVALID,
                        "quality.extra key '" + entry.getKey() + "' must match " + QUALITY_EXTRA_KEY.pattern());
            }
            if (!isScalar(entry.getValue())) {
                throw new Rejected(
                        RejectionReason.QUALITY_INVALID,
                        "quality.extra '" + entry.getKey() + "' must be a string, finite number or boolean");
            }
        }
    }

    private static boolean isScalar(Object value) {
        return switch (value) {
            case String s -> true;
            case Boolean b -> true;
            case Double d -> Double.isFinite(d);
            case Float f -> Float.isFinite(f);
            case Number n -> true;
            case null, default -> false;
        };
    }

    // ---------- derived ----------

    /**
     * The lowercase, space-joined text of {@code fields} (dot paths allowed; list values contribute
     * their scalar elements), cut to {@value #MAX_SEARCH_TEXT_LENGTH} characters; null when empty.
     */
    static String searchText(List<String> fields, Map<String, Object> payload) {
        if (fields == null || fields.isEmpty() || payload.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (String field : fields) {
            Object value = JsonValues.at(payload, field);
            if (value instanceof Collection<?> list) {
                for (Object element : list) {
                    append(text, JsonValues.text(element));
                }
            } else {
                append(text, JsonValues.text(value));
            }
        }
        String result = text.toString().strip().toLowerCase(Locale.ROOT);
        if (result.isEmpty()) {
            return null;
        }
        return result.length() <= MAX_SEARCH_TEXT_LENGTH ? result : result.substring(0, MAX_SEARCH_TEXT_LENGTH);
    }

    private static void append(StringBuilder text, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!text.isEmpty()) {
            text.append(' ');
        }
        text.append(part.strip());
    }

    // ---------- plumbing ----------

    private static final class Counts {
        int clamped;
        int validUntilIgnored;
    }

    /** Control flow for one rejected record; never escapes {@link #normalize}. */
    private static final class Rejected extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final RejectionReason reason;

        Rejected(RejectionReason reason, String detail) {
            super(detail, null, false, false);
            this.reason = reason;
        }
    }
}
