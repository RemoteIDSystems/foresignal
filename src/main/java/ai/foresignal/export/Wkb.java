package ai.foresignal.export;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

/**
 * Small readers of Well-Known Binary geometry for the export writers (prompt 111).
 *
 * <p>A WKB geometry starts with a byte-order flag ({@code 0} big-endian, {@code 1} little-endian)
 * followed by a {@code uint32} geometry type in that byte order. The type carries the dimension in
 * one of two conventions, both of which are decoded here:
 *
 * <ul>
 *   <li>ISO/OGC: {@code 1000} added for Z, {@code 2000} for M, {@code 3000} for ZM (for example
 *       {@code 1001} is {@code Point Z});
 *   <li>EWKB (PostGIS): the high bits {@code 0x80000000} (Z), {@code 0x40000000} (M) and
 *       {@code 0x20000000} (an SRID follows the type).
 * </ul>
 */
public final class Wkb {

    private static final int EWKB_Z = 0x80000000;
    private static final int EWKB_M = 0x40000000;
    private static final int EWKB_SRID = 0x20000000;

    private static final String[] NAMES = {
        null, "Point", "LineString", "Polygon", "MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection"
    };

    private Wkb() {}

    /**
     * The decoded header of a WKB geometry.
     *
     * @param baseType the OGC base type code (1 Point … 7 GeometryCollection), or 0 when unknown
     * @param hasZ whether the geometry has Z
     * @param hasM whether the geometry has M
     */
    public record Header(int baseType, boolean hasZ, boolean hasM) {

        /**
         * The GeoParquet 1.1 geometry type name: the base name plus {@code " Z"}, {@code " M"} or
         * {@code " ZM"}; null for an unknown base type.
         */
        public String geoParquetName() {
            if (baseType < 1 || baseType >= NAMES.length) {
                return null;
            }
            String suffix = hasZ && hasM ? " ZM" : hasZ ? " Z" : hasM ? " M" : "";
            return NAMES[baseType] + suffix;
        }
    }

    /**
     * Reads the header of {@code wkb}.
     *
     * @return the header, or null when {@code wkb} is shorter than 5 bytes or the byte-order flag is
     *     neither 0 nor 1
     */
    public static Header header(byte[] wkb) {
        if (wkb == null || wkb.length < 5 || (wkb[0] != 0 && wkb[0] != 1)) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(wkb, 1, 4).order(wkb[0] == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        int raw = buffer.getInt();
        boolean ewkbZ = (raw & EWKB_Z) != 0;
        boolean ewkbM = (raw & EWKB_M) != 0;
        int code = raw & ~(EWKB_Z | EWKB_M | EWKB_SRID);
        int thousands = code / 1000;
        int base = code % 1000;
        boolean isoZ = thousands == 1 || thousands == 3;
        boolean isoM = thousands == 2 || thousands == 3;
        if (thousands > 3) {
            return new Header(0, false, false);
        }
        return new Header(base >= 1 && base <= 7 ? base : 0, ewkbZ || isoZ, ewkbM || isoM);
    }

    /** The GeoParquet geometry type name of {@code wkb}, or null when it cannot be read. */
    public static String geoParquetType(byte[] wkb) {
        Header header = header(wkb);
        return header == null ? null : header.geoParquetName();
    }

    /**
     * Parses {@code wkb} into a JTS geometry with SRID 4326.
     *
     * @throws IllegalArgumentException when the bytes are not valid WKB
     */
    public static Geometry parse(byte[] wkb) {
        try {
            Geometry geometry = new WKBReader().read(wkb);
            geometry.setSRID(4326);
            return geometry;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a WKB geometry: " + e.getMessage(), e);
        }
    }
}
