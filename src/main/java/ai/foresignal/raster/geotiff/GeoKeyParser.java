package ai.foresignal.raster.geotiff;

import java.util.HashMap;
import java.util.Map;

/**
 * Reads a GeoTIFF's georeferencing (prompt 054; OGC GeoTIFF 1.1, 19-008r4) from a parsed
 * {@link TiffDirectory}.
 *
 * <ul>
 *   <li><b>CRS</b> from the GeoKeyDirectory (34735): {@code ProjectedCSTypeGeoKey} (3072) = 3857 gives
 *       EPSG:3857; otherwise a geographic model ({@code GTModelTypeGeoKey} 1024 = 2, or absent) with
 *       {@code GeographicTypeGeoKey} (2048) = 4326 gives EPSG:4326. Anything else is refused with
 *       {@link GeoTiffException#crsReason}; a user-defined CRS reports 32767.
 *   <li><b>Transform</b> from ModelTransformation (34264), axis-aligned only (a rotation or shear is
 *       refused), else from one ModelTiepoint (33922) plus ModelPixelScale (33550):
 *       {@code X = tieX + (i − tieI)·sx}, {@code Y = tieY − (j − tieJ)·sy}.
 *   <li><b>Raster type</b> ({@code GTRasterTypeGeoKey} 1025): PixelIsArea (1, the default) maps the
 *       raster coordinate (0, 0) to the corner of the first pixel; PixelIsPoint (2) maps it to the
 *       pixel's center, so the corner transform is shifted by half a pixel, as GDAL does.
 * </ul>
 *
 * <p>SatDump 1.2.2's equirectangular composite output writes one tie point at (0, 0), a positive
 * pixel scale, {@code GTModelTypeGeoKey = 2} and {@code GeographicTypeGeoKey = 4326}; its WebMercator
 * output writes {@code ProjectedCSTypeGeoKey = 3857} (read for format facts only, Weather-Imagery-Design
 * §3).
 */
public final class GeoKeyParser {

    /** {@code GTModelTypeGeoKey}. */
    public static final int GT_MODEL_TYPE = 1024;

    /** {@code GTRasterTypeGeoKey}. */
    public static final int GT_RASTER_TYPE = 1025;

    /** {@code GeographicTypeGeoKey}. */
    public static final int GEOGRAPHIC_TYPE = 2048;

    /** {@code ProjectedCSTypeGeoKey}. */
    public static final int PROJECTED_CS_TYPE = 3072;

    /** {@code ModelTypeProjected}. */
    public static final int MODEL_PROJECTED = 1;

    /** {@code ModelTypeGeographic}. */
    public static final int MODEL_GEOGRAPHIC = 2;

    /** {@code RasterPixelIsPoint}. */
    public static final int PIXEL_IS_POINT = 2;

    /** The GeoTIFF "user-defined" code. */
    public static final int USER_DEFINED = 32767;

    /** The largest off-axis term, relative to the axis terms, still read as axis-aligned. */
    static final double AXIS_TOLERANCE = 1e-9;

    private GeoKeyParser() {}

    /**
     * Reads the georeferencing.
     *
     * @throws GeoTiffException {@code NO_GEOREFERENCE}, {@code UNSUPPORTED_CRS} or {@code ROTATED}
     */
    public static GeoReference parse(TiffDirectory dir) throws GeoTiffException {
        Map<Integer, Integer> keys = shortKeys(dir);
        int epsg = crs(keys);
        double[] gt = transform(dir);
        boolean point = Integer.valueOf(PIXEL_IS_POINT).equals(keys.get(GT_RASTER_TYPE));
        if (point) {
            // The raster coordinate (0, 0) is the first pixel's center: move the transform to its corner.
            gt[0] -= 0.5 * gt[1] + 0.5 * gt[2];
            gt[3] -= 0.5 * gt[4] + 0.5 * gt[5];
        }
        try {
            return new GeoReference(epsg, gt, point);
        } catch (IllegalArgumentException e) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "GeoTIFF transform is unusable: "
                    + e.getMessage());
        }
    }

    /** The EPSG code the keys declare, or throws for anything but 4326 and 3857. */
    static int crs(Map<Integer, Integer> keys) throws GeoTiffException {
        Integer projected = keys.get(PROJECTED_CS_TYPE);
        Integer model = keys.get(GT_MODEL_TYPE);
        Integer geographic = keys.get(GEOGRAPHIC_TYPE);
        if (projected != null) {
            if (projected == 3857) {
                return 3857;
            }
            throw unsupported(projected);
        }
        if (model != null && model == MODEL_PROJECTED) {
            throw unsupported(USER_DEFINED);
        }
        if (model != null && model != MODEL_GEOGRAPHIC) {
            throw unsupported(USER_DEFINED);
        }
        if (geographic == null) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE,
                    "GeoTIFF declares no geographic or projected CRS");
        }
        if (geographic == 4326) {
            return 4326;
        }
        throw unsupported(geographic);
    }

    /** The corner geotransform from ModelTransformation, or from a tie point and pixel scale. */
    static double[] transform(TiffDirectory dir) throws GeoTiffException {
        double[] m = dir.doubles(TiffDirectory.MODEL_TRANSFORMATION);
        if (m != null) {
            if (m.length < 16) {
                throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE,
                        "GeoTIFF ModelTransformation has " + m.length + " values, not 16");
            }
            double a = m[0];
            double b = m[1];
            double c = m[3];
            double d = m[4];
            double e = m[5];
            double f = m[7];
            double scale = Math.max(Math.abs(a), Math.abs(e));
            if (Math.abs(b) > AXIS_TOLERANCE * scale || Math.abs(d) > AXIS_TOLERANCE * scale) {
                throw new GeoTiffException(GeoTiffException.Kind.ROTATED,
                        "GeoTIFF ModelTransformation is rotated or sheared (not supported; build with WITH_GDAL=true)");
            }
            return new double[] {c, a, 0, f, 0, e};
        }
        double[] tie = dir.doubles(TiffDirectory.MODEL_TIEPOINT);
        double[] scale = dir.doubles(TiffDirectory.MODEL_PIXEL_SCALE);
        if (tie == null || tie.length < 6) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "GeoTIFF has no tie point or transformation");
        }
        if (scale == null || scale.length < 2) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE,
                    "GeoTIFF has tie points without a pixel scale (ground control points are not supported)");
        }
        double sx = scale[0];
        double sy = scale[1];
        double i = tie[0];
        double j = tie[1];
        double x = tie[3];
        double y = tie[4];
        return new double[] {x - i * sx, sx, 0, y + j * sy, 0, -sy};
    }

    /**
     * The SHORT-valued keys of the GeoKeyDirectory ({@code TIFFTagLocation = 0}); keys stored in the
     * double or ASCII parameter tags are not needed and are left out.
     *
     * @throws GeoTiffException {@code NO_GEOREFERENCE} when the directory is missing or malformed
     */
    static Map<Integer, Integer> shortKeys(TiffDirectory dir) throws GeoTiffException {
        long[] d = dir.longs(TiffDirectory.GEO_KEY_DIRECTORY);
        if (d == null || d.length < 4) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "GeoTIFF has no GeoKeyDirectory");
        }
        int count = (int) d[3];
        if (d.length < 4 + 4L * count) {
            throw new GeoTiffException(GeoTiffException.Kind.NO_GEOREFERENCE, "GeoTIFF GeoKeyDirectory is truncated");
        }
        Map<Integer, Integer> keys = new HashMap<>();
        for (int k = 0; k < count; k++) {
            int at = 4 + 4 * k;
            int id = (int) d[at];
            int location = (int) d[at + 1];
            if (location == 0) {
                keys.put(id, (int) d[at + 3]);
            }
        }
        return keys;
    }

    private static GeoTiffException unsupported(int epsg) {
        return new GeoTiffException(GeoTiffException.Kind.UNSUPPORTED_CRS, GeoTiffException.crsReason(epsg), epsg, null);
    }
}
