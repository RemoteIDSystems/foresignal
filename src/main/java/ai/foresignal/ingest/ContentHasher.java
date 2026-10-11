package ai.foresignal.ingest;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Content hashes of observations (Core-Data-Model §4).
 *
 * <ul>
 *   <li><strong>Full hash</strong> (stored on {@code record_live}): kind, event time, explicit
 *       {@code validUntil}, {@code validFrom}, tombstone, the geometry as WKT at 1e-7 degrees
 *       (after carry-forward), and the payload minus volatile keys.
 *   <li><strong>Static hash</strong> (stored on {@code record_head}): the same scalars,
 *       {@code geom IS NULL} instead of the geometry, and only the significant keys (every
 *       non-volatile key when none are declared).
 * </ul>
 *
 * <p>Never hashed: observed, received and ingested times, the derived {@code valid_until}, quality,
 * observers, search text and title — so re-observing identical content hashes equal.
 *
 * <p>A hash is the first 8 bytes of SHA-256 over {@link CanonicalJson}, as a signed {@code long}.
 * Pure and thread-safe.
 */
public final class ContentHasher {

    /** Bumped whenever the hash inputs or their encoding change; part of {@link #recipe}. */
    public static final int HASHER_VERSION = 1;

    /** Geometry coordinates are rounded to this many decimals (1e-7 degrees, about 1 cm). */
    public static final int GEOMETRY_DECIMALS = 7;

    private static final double GEOMETRY_SCALE = 1e7;

    private ContentHasher() {}

    /** The full hash of a prepared observation under {@code profile}. */
    public static long fullHash(Observation observation, SourceIngestProfile profile) {
        return fullHash(
                observation.kind(),
                observation.eventTime(),
                observation.validUntilExplicit(),
                observation.validFrom(),
                observation.tombstone(),
                observation.geometry(),
                observation.payload(),
                profile.volatileFields());
    }

    /** The static hash of a prepared observation under {@code profile}. */
    public static long staticHash(Observation observation, SourceIngestProfile profile) {
        return staticHash(
                observation.kind(),
                observation.eventTime(),
                observation.validUntilExplicit(),
                observation.validFrom(),
                observation.tombstone(),
                observation.geometry() == null,
                observation.payload(),
                profile.volatileFields(),
                profile.significantFields());
    }

    /** The full hash from its inputs. */
    public static long fullHash(
            String kind,
            Instant eventTime,
            Instant validUntilExplicit,
            Instant validFrom,
            boolean tombstone,
            Geometry geometry,
            Map<String, ?> payload,
            Set<String> volatileFields) {
        Map<String, Object> input = scalars(kind, eventTime, validUntilExplicit, validFrom, tombstone);
        input.put("geom", geometry == null ? null : wkt(geometry));
        input.put("payload", withoutKeys(payload, volatileFields));
        return hash(input);
    }

    /** The static hash from its inputs. */
    public static long staticHash(
            String kind,
            Instant eventTime,
            Instant validUntilExplicit,
            Instant validFrom,
            boolean tombstone,
            boolean geometryIsNull,
            Map<String, ?> payload,
            Set<String> volatileFields,
            Set<String> significantFields) {
        Map<String, Object> input = scalars(kind, eventTime, validUntilExplicit, validFrom, tombstone);
        input.put("geomIsNull", geometryIsNull);
        input.put("payload", significantSubset(payload, volatileFields, significantFields));
        return hash(input);
    }

    /**
     * The hash recipe: the first 4 bytes of SHA-256 over the hasher version, the sorted volatile
     * list and the sorted significant list, as an {@code int}. Stored per head so a changed field
     * list is detected and migrated lazily (§4).
     */
    public static int recipe(Collection<String> volatileFields, Collection<String> significantFields) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("version", HASHER_VERSION);
        input.put("volatile", sorted(volatileFields));
        input.put("significant", sorted(significantFields));
        byte[] digest = sha256(CanonicalJson.bytes(input));
        return ByteBuffer.wrap(digest, 0, 4).getInt();
    }

    /**
     * The kinematic subset of {@code payload}: keys that are neither significant nor volatile.
     * Empty when {@code significantFields} is empty, because every non-volatile key is then
     * significant.
     */
    public static Map<String, Object> kinematicSubset(
            Map<String, ?> payload, Set<String> volatileFields, Set<String> significantFields) {
        if (payload == null || payload.isEmpty() || significantFields.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> subset = new LinkedHashMap<>();
        payload.forEach(
                (key, value) -> {
                    if (!significantFields.contains(key) && !volatileFields.contains(key)) {
                        subset.put(key, value);
                    }
                });
        return Collections.unmodifiableMap(subset);
    }

    /** The kinematic subset under {@code profile}. */
    public static Map<String, Object> kinematicSubset(Map<String, ?> payload, SourceIngestProfile profile) {
        return kinematicSubset(payload, profile.volatileFields(), profile.significantFields());
    }

    /**
     * The geometry as WKT-style text (collection members carry their own type tags) with every
     * ordinate rounded to {@value #GEOMETRY_DECIMALS} decimals and
     * written in plain form. Deterministic, and independent of JTS's own number formatting.
     */
    public static String wkt(Geometry geometry) {
        StringBuilder out = new StringBuilder();
        appendGeometry(out, geometry);
        return out.toString();
    }

    // ---------- internals ----------

    private static Map<String, Object> scalars(
            String kind, Instant eventTime, Instant validUntilExplicit, Instant validFrom, boolean tombstone) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("kind", kind);
        input.put("eventTime", eventTime == null ? null : eventTime.toString());
        input.put("validUntil", validUntilExplicit == null ? null : validUntilExplicit.toString());
        input.put("validFrom", validFrom == null ? null : validFrom.toString());
        input.put("tombstone", tombstone);
        return input;
    }

    private static Map<String, Object> withoutKeys(Map<String, ?> payload, Set<String> excluded) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (payload != null) {
            payload.forEach(
                    (key, value) -> {
                        if (!excluded.contains(key)) {
                            result.put(key, value);
                        }
                    });
        }
        return result;
    }

    private static Map<String, Object> significantSubset(
            Map<String, ?> payload, Set<String> volatileFields, Set<String> significantFields) {
        if (significantFields.isEmpty()) {
            return withoutKeys(payload, volatileFields);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        if (payload != null) {
            payload.forEach(
                    (key, value) -> {
                        if (significantFields.contains(key) && !volatileFields.contains(key)) {
                            result.put(key, value);
                        }
                    });
        }
        return result;
    }

    private static List<String> sorted(Collection<String> values) {
        List<String> list = new ArrayList<>(values == null ? List.of() : values);
        Collections.sort(list);
        return list;
    }

    private static long hash(Map<String, Object> input) {
        byte[] digest = sha256(CanonicalJson.bytes(input));
        return ByteBuffer.wrap(digest, 0, 8).getLong();
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    private static void appendGeometry(StringBuilder out, Geometry geometry) {
        switch (geometry) {
            case Point point -> {
                out.append("POINT");
                if (point.isEmpty()) {
                    out.append(" EMPTY");
                } else {
                    out.append('(');
                    appendCoordinate(out, point.getCoordinate());
                    out.append(')');
                }
            }
            case LineString line -> {
                out.append(line.getGeometryType().toUpperCase(java.util.Locale.ROOT));
                appendSequence(out, line.getCoordinates());
            }
            case Polygon polygon -> {
                out.append("POLYGON");
                appendPolygonBody(out, polygon);
            }
            case MultiPoint multi -> appendCollection(out, "MULTIPOINT", multi);
            case MultiLineString multi -> appendCollection(out, "MULTILINESTRING", multi);
            case MultiPolygon multi -> appendCollection(out, "MULTIPOLYGON", multi);
            case GeometryCollection collection -> appendCollection(out, "GEOMETRYCOLLECTION", collection);
            default -> throw new IllegalArgumentException("Unsupported geometry " + geometry.getGeometryType());
        }
    }

    private static void appendCollection(StringBuilder out, String name, GeometryCollection collection) {
        out.append(name);
        if (collection.isEmpty()) {
            out.append(" EMPTY");
            return;
        }
        out.append('(');
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            if (i > 0) {
                out.append(',');
            }
            appendGeometry(out, collection.getGeometryN(i));
        }
        out.append(')');
    }

    private static void appendPolygonBody(StringBuilder out, Polygon polygon) {
        if (polygon.isEmpty()) {
            out.append(" EMPTY");
            return;
        }
        out.append('(');
        appendSequence(out, polygon.getExteriorRing().getCoordinates());
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            out.append(',');
            appendSequence(out, polygon.getInteriorRingN(i).getCoordinates());
        }
        out.append(')');
    }

    private static void appendSequence(StringBuilder out, Coordinate[] coordinates) {
        if (coordinates.length == 0) {
            out.append(" EMPTY");
            return;
        }
        out.append('(');
        for (int i = 0; i < coordinates.length; i++) {
            if (i > 0) {
                out.append(',');
            }
            appendCoordinate(out, coordinates[i]);
        }
        out.append(')');
    }

    private static void appendCoordinate(StringBuilder out, Coordinate coordinate) {
        out.append(ordinate(coordinate.getX())).append(' ').append(ordinate(coordinate.getY()));
    }

    private static String ordinate(double value) {
        long scaled = Math.round(value * GEOMETRY_SCALE);
        return CanonicalJson.number(BigDecimal.valueOf(scaled, GEOMETRY_DECIMALS));
    }
}
