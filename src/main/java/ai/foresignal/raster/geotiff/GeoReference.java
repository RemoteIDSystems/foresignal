package ai.foresignal.raster.geotiff;

/**
 * Where a GeoTIFF's pixels lie (prompt 054): its CRS and the GDAL-style geotransform of the pixel
 * <em>corners</em>, {@code X = gt0 + col·gt1 + row·gt2}, {@code Y = gt3 + col·gt4 + row·gt5}, where
 * {@code (0, 0)} is the top-left corner of the first pixel. This is the {@code affine} grid's
 * {@code gt0 … gt5} ({@code raster.AffineGrid}); a PixelIsPoint file is already shifted by half a
 * pixel.
 *
 * @param epsg 4326 (degrees) or 3857 (WebMercator meters)
 * @param gt the six geotransform coefficients
 * @param pixelIsPoint true when the file declared {@code GTRasterTypeGeoKey = PixelIsPoint}
 */
public record GeoReference(int epsg, double[] gt, boolean pixelIsPoint) {

    /** The WebMercator sphere radius, meters. */
    public static final double MERCATOR_RADIUS = 6_378_137.0;

    /** Copies the transform and checks it. */
    public GeoReference {
        if (epsg != 4326 && epsg != 3857) {
            throw new IllegalArgumentException("Only EPSG:4326 and EPSG:3857 are read, not EPSG:" + epsg);
        }
        if (gt == null || gt.length != 6) {
            throw new IllegalArgumentException("A geotransform has six coefficients");
        }
        gt = gt.clone();
        if (gt[1] * gt[5] - gt[2] * gt[4] == 0) {
            throw new IllegalArgumentException("The geotransform is singular");
        }
    }

    /** The CRS as {@code EPSG:n}, the {@code affine} grid's {@code crs} parameter. */
    public String crs() {
        return "EPSG:" + epsg;
    }

    /** True for WebMercator meters. */
    public boolean mercator() {
        return epsg == 3857;
    }

    /** A copy of the geotransform. */
    @Override
    public double[] gt() {
        return gt.clone();
    }

    /**
     * The longitude and latitude, in degrees, of the continuous pixel-corner coordinate
     * {@code (col, row)}. Longitudes are <em>not</em> wrapped, so a grid that runs past 180° stays
     * continuous; latitudes are clamped to ±90°.
     */
    public double[] lonLat(double col, double row) {
        double x = gt[0] + col * gt[1] + row * gt[2];
        double y = gt[3] + col * gt[4] + row * gt[5];
        if (mercator()) {
            x = Math.toDegrees(x / MERCATOR_RADIUS);
            y = Math.toDegrees(2 * Math.atan(Math.exp(y / MERCATOR_RADIUS)) - Math.PI / 2);
        }
        return new double[] {x, Math.max(-90, Math.min(90, y))};
    }

    /** The longitude span of one row of {@code width} pixels, in degrees (absolute). */
    public double lonSpan(int width) {
        return Math.abs(lonLat(width, 0)[0] - lonLat(0, 0)[0]);
    }
}
