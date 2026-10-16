package ai.foresignal.source.adapters.satdump;

import ai.foresignal.orbit.SwathEstimator;
import ai.foresignal.raster.geotiff.Antimeridian;
import ai.foresignal.raster.geotiff.FootprintBuilder;
import ai.foresignal.raster.geotiff.GeoReference;
import ai.foresignal.raster.geotiff.GeoTiffException;
import ai.foresignal.raster.geotiff.GeoTiffReader;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.RasterSink;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Turns the GeoTIFFs of a ready polar pass folder into map overlays with real footprints, and gives a
 * pass without a usable GeoTIFF an approximate swath (prompt 054, Weather-Imagery-Design §4.5 items
 * 2–4).
 *
 * <p>For every file matching {@code polarGlob} (at most {@value #MAX_OVERLAYS} per folder):
 *
 * <ol>
 *   <li>{@link GeoTiffReader}: the image guard's limits from the header, EPSG:4326/3857 only. A file
 *       it refuses for its CRS, a rotation or BigTIFF goes through the optional GDAL warp
 *       ({@link RasterSink#warpToWgs84}) when that is available; otherwise its reason becomes a health
 *       reason ("GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)", "BigTIFF not
 *       supported.").
 *   <li>The footprint from the alpha mask ({@link FootprintBuilder}), split at ±180°.
 *   <li>The dynamic product {@code satdump-{station}-{composite}} ({@link #productId}): {@code rgba8},
 *       {@code temporal.type = pass}, coverage {@code swath}, style {@code rgb@1}, ladder
 *       {@value #LADDER_DAYS} days; registered through {@link RasterSink#registerProduct}.
 *   <li>One RGBA {@code affine} frame (grid parameters {@code crs} and {@code gt0 … gt5}) committed
 *       through {@code ctx.rasters()} with the footprint, at the product's timestamp, so re-processing a
 *       pass replaces the frame (a new {@code sha8}).
 * </ol>
 *
 * <p>The composite name is the file stem without SatDump's {@code _projected} suffix, so the overlay of
 * {@code rgb_MSU-MR_221_Composite_projected.tif} joins the {@code products[]} entry of
 * {@code rgb_MSU-MR_221_Composite.png}. Without any overlay, the approximate swath comes from
 * {@link SwathEstimator} (the TLE in {@code product.cbor}, between the first and last product
 * timestamps); it is a geometry only, never tiles. Not thread-safe; the station synchronizes.
 */
public final class PolarOverlays {

    private static final Logger log = LoggerFactory.getLogger(PolarOverlays.class);

    /** The most overlays read from one pass folder. */
    public static final int MAX_OVERLAYS = 8;

    /** The largest GeoTIFF read. */
    public static final long MAX_GEOTIFF_BYTES = 200L * 1024 * 1024;

    /** The SatDump polar ladder (Weather-Imagery-Design §2.4). */
    public static final int LADDER_DAYS = 30;

    /** The native max zoom of an overlay product (about 1 km pixels). */
    public static final int NATIVE_MAX_ZOOM = 8;

    /** The window around a lone product timestamp when nothing better bounds the swath. */
    public static final Duration LONE_TIMESTAMP_HALF_WINDOW = Duration.ofMinutes(5);

    /** {@code geometrySource} of a pass whose geometry is a GeoTIFF footprint. */
    public static final String FOOTPRINT = "footprint";

    /** {@code geometrySource} of a pass whose geometry is an approximate swath. */
    public static final String SWATH = "swath";

    /**
     * One overlay committed.
     *
     * @param composite the composite name (file stem without {@code _projected})
     * @param file the GeoTIFF's file name
     * @param instrument the instrument of the product directory holding it, or null
     * @param productId the dynamic product
     * @param frameKey the committed frame's key, or null when the sink did not return one
     * @param tilesTemplate the content-addressed tile template, or null
     * @param footprint the footprint, SRID 4326
     */
    public record Overlay(String composite, String file, String instrument, String productId, String frameKey,
            String tilesTemplate, MultiPolygon footprint) {}

    /**
     * What one pass folder gave.
     *
     * @param overlays the overlays committed
     * @param geometry the footprint union, else the approximate swath, else null
     * @param geometrySource {@value #FOOTPRINT}, {@value #SWATH} or null
     * @param records the {@code raster-frame} records to emit
     * @param reasons health reasons of files that were not read
     */
    public record Result(List<Overlay> overlays, MultiPolygon geometry, String geometrySource,
            List<NormalizedRecord> records, Set<String> reasons) {}

    private final String station;

    /**
     * Creates the processor.
     *
     * @param station the station name
     */
    public PolarOverlays(String station) {
        this.station = station;
    }

    /**
     * Processes the GeoTIFFs of a ready pass folder, then falls back to the approximate swath.
     *
     * @param folder the ready folder
     * @param contents its parsed {@code dataset.json} and products
     * @param pass the pass the folder joined
     * @param glob {@code polarGlob}
     * @param access where the folder's files are read (the inbox or the upload area)
     * @param rasters the raster sink
     */
    public Result process(InboxScanner.Folder folder, PolarPassProcessor.Contents contents, PassState pass,
            PolarGlob glob, InboxAccess access, RasterSink rasters) {
        List<Overlay> overlays = new ArrayList<>();
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> reasons = new LinkedHashSet<>();
        List<InboxAccess.FileStat> files = new ArrayList<>(folder.files());
        files.sort(Comparator.comparing(f -> f.path().toString()));
        for (InboxAccess.FileStat file : files) {
            String relative = InboxScanner.relative(folder.path(), file.path());
            if (!glob.matches(relative)) {
                continue;
            }
            if (overlays.size() >= MAX_OVERLAYS) {
                log.warn("{}: more than {} GeoTIFFs in {}; the rest are ignored", station, MAX_OVERLAYS, folder.relative());
                break;
            }
            if (file.size() > MAX_GEOTIFF_BYTES) {
                reasons.add("GeoTIFF " + file.path().getFileName() + " is larger than " + MAX_GEOTIFF_BYTES / (1024 * 1024)
                        + " MB");
                continue;
            }
            PolarPassProcessor.ProductDir product = productOf(contents, file.path());
            try {
                byte[] bytes = access.readAll(file.path(), MAX_GEOTIFF_BYTES);
                GeoTiffReader.Image image = decode(bytes, rasters, reasons, file.path().getFileName().toString());
                if (image == null) {
                    continue;
                }
                MultiPolygon footprint = FootprintBuilder.build(image);
                if (footprint.isEmpty()) {
                    log.info("{}: GeoTIFF {} has no valid pixel; no overlay", station, relative);
                    continue;
                }
                Instant validTime = validTime(product, contents);
                Overlay overlay = commit(image, footprint, file.path(), relative, null,
                        product == null ? null : product.product().instrument(), validTime, pass, rasters, records);
                if (overlay != null) {
                    overlays.add(overlay);
                }
            } catch (IOException | AdapterException | RuntimeException e) {
                log.warn("{}: GeoTIFF {} of {} not used: {}", station, relative, folder.relative(), e.getMessage());
            }
        }
        if (!overlays.isEmpty()) {
            List<Geometry> parts = new ArrayList<>();
            overlays.forEach(o -> parts.add(o.footprint()));
            return new Result(overlays, multi(UnaryUnionOp.union(parts)), FOOTPRINT, records, reasons);
        }
        MultiPolygon swath = swath(contents, pass).orElse(null);
        return new Result(overlays, swath, swath == null ? null : SWATH, records, reasons);
    }

    /** Reads the file, through GDAL when the reader refuses it and GDAL can help; null when unusable. */
    private GeoTiffReader.Image decode(byte[] bytes, RasterSink rasters, Set<String> reasons, String name)
            throws AdapterException {
        try {
            return GeoTiffReader.read(bytes);
        } catch (GeoTiffException e) {
            if (e.gdalCanHelp()) {
                Optional<byte[]> warped = rasters.warpToWgs84(bytes);
                if (warped.isPresent()) {
                    try {
                        return GeoTiffReader.read(warped.get());
                    } catch (GeoTiffException again) {
                        reasons.add("GeoTIFF " + name + " warped by GDAL is still unreadable: " + again.getMessage());
                        return null;
                    }
                }
            }
            reasons.add(reason(e));
            log.info("{}: GeoTIFF {} not read: {}", station, name, e.getMessage());
            return null;
        }
    }

    /** The health reason of a refused file. */
    static String reason(GeoTiffException e) {
        return switch (e.kind()) {
            case BIGTIFF -> PolarPassProcessor.BIGTIFF_REASON;
            case UNSUPPORTED_CRS, ROTATED -> e.getMessage();
            default -> e.getMessage() + ".";
        };
    }

    private Overlay commit(GeoTiffReader.Image image, MultiPolygon footprint, Path path, String relative,
            String compositeName, String instrument, Instant validTime, PassState pass, RasterSink rasters,
            List<NormalizedRecord> records) throws AdapterException {
        String file = path.getFileName().toString();
        String composite = compositeName == null || compositeName.isBlank() ? composite(file) : compositeName;
        String productId = productId(station, composite);
        try {
            rasters.registerProduct(spec(productId, station, composite));
        } catch (UnsupportedOperationException e) {
            log.debug("{}: this raster sink cannot register products", station);
        }
        GridDefinition grid = grid(image.width(), image.height(), image.georeference());
        FrameInput frame = FrameInput.builder(productId)
                .validTime(validTime)
                .grid(grid)
                .bands(DecodedBands.codes(image.rgba()))
                .footprint(footprint)
                .upstreamKey(pass.externalId + "/" + relative)
                .variable(composite)
                .build();
        NormalizedRecord record = rasters.commit(frame);
        String frameKey = null;
        String tiles = null;
        if (record != null) {
            records.add(record);
            frameKey = text(record.payload().get("frameKey"));
            tiles = text(record.payload().get("tiles"));
        }
        return new Overlay(composite, file, instrument, productId, frameKey, tiles, footprint);
    }

    /** The most bytes of a manifest-listed PNG read. */
    public static final long MAX_PNG_BYTES = 64L * 1024 * 1024;

    /**
     * Commits the products a {@code foresignal-manifest.json} lists (prompt 107, SatDump 2.0 hook,
     * version-agnostic), through the same pipeline as {@link #process}.
     *
     * <ul>
     *   <li>Each product's {@code file} is resolved against the pass folder and must be one of the
     *       folder's listed files (so it is inside the inbox and not a link).
     *   <li>A GeoTIFF already matched by {@code polarGlob} was committed by {@link #process} and is
     *       skipped; any other GeoTIFF is read with {@link GeoTiffReader} (and GDAL when available).
     *   <li>A PNG goes through the image guard ({@link MediaSink#decode}) and is georeferenced from the
     *       product's {@code bounds [w,s,e,n]}: {@code projection} must be {@code equirec}, and the
     *       image is an EPSG:4326 affine grid whose pixel corners span the bounds. A PNG without valid
     *       bounds is refused with a health reason.
     *   <li>The composite name is the product's {@code composite}, else its {@code name}; the valid time is
     *       the manifest's {@code timestamp}.
     * </ul>
     *
     * @param folder the ready pass folder
     * @param manifest the parsed manifest
     * @param pass the pass the folder joined
     * @param glob {@code polarGlob}
     * @param alreadyCommitted the overlays {@link #process} committed for this folder
     * @param access where the folder's files are read
     * @param rasters the raster sink
     * @param media the media sink whose decoder applies the image guard
     */
    public Result processManifest(InboxScanner.Folder folder, ForesignalManifest.Manifest manifest, PassState pass,
            PolarGlob glob, int alreadyCommitted, InboxAccess access, RasterSink rasters, MediaSink media) {
        List<Overlay> overlays = new ArrayList<>();
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> reasons = new LinkedHashSet<>();
        Map<Path, InboxAccess.FileStat> listed = new LinkedHashMap<>();
        for (InboxAccess.FileStat f : folder.files()) {
            listed.put(f.path().normalize(), f);
        }
        for (ForesignalManifest.Product product : manifest.products()) {
            if (alreadyCommitted + overlays.size() >= MAX_OVERLAYS) {
                log.warn("{}: more than {} overlays in {}; the rest of the manifest is ignored", station, MAX_OVERLAYS,
                        folder.relative());
                break;
            }
            Path path = folder.path().resolve(product.file()).normalize();
            InboxAccess.FileStat file = path.startsWith(folder.path()) ? listed.get(path) : null;
            if (file == null) {
                reasons.add("2.0 manifest product " + product.file() + " is not in the pass folder.");
                continue;
            }
            String relative = InboxScanner.relative(folder.path(), path);
            String lower = relative.toLowerCase(Locale.ROOT);
            String compositeName = product.composite() != null ? product.composite() : product.name();
            try {
                GeoTiffReader.Image image;
                if (PolarGlob.tiff(lower)) {
                    if (glob.matches(relative)) {
                        continue;
                    }
                    if (file.size() > MAX_GEOTIFF_BYTES) {
                        reasons.add("GeoTIFF " + path.getFileName() + " is larger than "
                                + MAX_GEOTIFF_BYTES / (1024 * 1024) + " MB");
                        continue;
                    }
                    image = decode(access.readAll(path, MAX_GEOTIFF_BYTES), rasters, reasons,
                            path.getFileName().toString());
                } else if (lower.endsWith(".png")) {
                    if (file.size() > MAX_PNG_BYTES) {
                        reasons.add("2.0 manifest PNG " + path.getFileName() + " is larger than "
                                + MAX_PNG_BYTES / (1024 * 1024) + " MB");
                        continue;
                    }
                    image = png(product, access.readAll(path, MAX_PNG_BYTES), media, reasons);
                } else {
                    reasons.add("2.0 manifest product " + product.file() + " is neither a GeoTIFF nor a PNG.");
                    continue;
                }
                if (image == null) {
                    continue;
                }
                MultiPolygon footprint = FootprintBuilder.build(image);
                if (footprint.isEmpty()) {
                    log.info("{}: manifest product {} has no valid pixel; no overlay", station, relative);
                    continue;
                }
                Overlay overlay = commit(image, footprint, path, relative, compositeName, product.instrument(),
                        manifest.timestamp(), pass, rasters, records);
                if (overlay != null) {
                    overlays.add(overlay);
                }
            } catch (IOException | AdapterException | RuntimeException e) {
                log.warn("{}: manifest product {} of {} not used: {}", station, relative, folder.relative(),
                        e.getMessage());
            }
        }
        MultiPolygon geometry = null;
        if (!overlays.isEmpty()) {
            List<Geometry> parts = new ArrayList<>();
            overlays.forEach(o -> parts.add(o.footprint()));
            geometry = multi(UnaryUnionOp.union(parts));
        }
        return new Result(overlays, geometry, geometry == null ? null : FOOTPRINT, records, reasons);
    }

    /**
     * Decodes a manifest PNG through the image guard and georeferences it from the product's
     * {@code bounds}, or returns null with a health reason.
     */
    GeoTiffReader.Image png(ForesignalManifest.Product product, byte[] bytes, MediaSink media, Set<String> reasons) {
        double[] b = product.bounds();
        if (product.projection() != null && !"equirec".equalsIgnoreCase(product.projection())) {
            reasons.add("2.0 manifest PNG " + product.file() + " has projection " + product.projection()
                    + "; only equirec is read.");
            return null;
        }
        if (!validBounds(b)) {
            reasons.add("2.0 manifest PNG " + product.file() + " has no valid bounds [w,s,e,n].");
            return null;
        }
        java.awt.image.BufferedImage decoded;
        try {
            decoded = media.decode(bytes, "image/png");
        } catch (UnsupportedOperationException e) {
            reasons.add("2.0 manifest PNG " + product.file() + " cannot be decoded here.");
            return null;
        } catch (RuntimeException e) {
            reasons.add("2.0 manifest PNG " + product.file() + " refused: " + e.getMessage());
            return null;
        }
        return georeferenced(decoded, b);
    }

    /**
     * The image of an equirectangular PNG whose pixel corners span {@code bounds [w,s,e,n]}: packed
     * {@code 0xRRGGBBAA} pixels and an EPSG:4326 geotransform {@code [w, (e−w)/width, 0, n, 0, −(n−s)/height]}.
     * An east bound below the west bound crosses ±180° and is unwrapped by 360°.
     */
    static GeoTiffReader.Image georeferenced(java.awt.image.BufferedImage decoded, double[] bounds) {
        int width = decoded.getWidth();
        int height = decoded.getHeight();
        int[] rgba = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = decoded.getRGB(x, y);
                rgba[y * width + x] = (argb << 8) | ((argb >>> 24) & 0xFF);
            }
        }
        double west = bounds[0];
        double east = bounds[2] <= bounds[0] ? bounds[2] + 360 : bounds[2];
        GeoReference georeference = new GeoReference(4326,
                new double[] {west, (east - west) / width, 0, bounds[3], 0, -(bounds[3] - bounds[1]) / height}, false);
        return new GeoTiffReader.Image(width, height, rgba, georeference, null);
    }

    /** True when {@code b} is {@code [w,s,e,n]} inside ±180°/±90° with {@code s < n} and {@code w != e}. */
    static boolean validBounds(double[] b) {
        if (b == null || b.length != 4) {
            return false;
        }
        for (double v : b) {
            if (!Double.isFinite(v)) {
                return false;
            }
        }
        return b[0] >= -180 && b[0] <= 180 && b[2] >= -180 && b[2] <= 180 && b[1] >= -90 && b[3] <= 90 && b[1] < b[3]
                && b[0] != b[2];
    }

    /**
     * The overlay product id, {@code satdump-{station}-{composite}}, kebab-case and at most 64
     * characters (Conventions "SatDump product ids").
     */
    public static String productId(String station, String composite) {
        String c = SatNames.slug(composite);
        return XritProducts.bounded("satdump-" + SatNames.slug(station) + "-" + (c.isEmpty() ? "overlay" : c));
    }

    /** The composite name of a GeoTIFF file: its stem without a trailing {@code _projected}. */
    public static String composite(String fileName) {
        String stem = fileName;
        int dot = stem.lastIndexOf('.');
        if (dot > 0) {
            stem = stem.substring(0, dot);
        }
        if (stem.toLowerCase(Locale.ROOT).endsWith("_projected") && stem.length() > "_projected".length()) {
            stem = stem.substring(0, stem.length() - "_projected".length());
        }
        return stem;
    }

    /** The spec of an overlay product. */
    public static ProductSpec spec(String productId, String station, String composite) {
        return ProductSpec.builder(productId)
                .title("SatDump " + station + " " + composite)
                .category("imagery")
                .dtype(RasterDtype.RGBA8)
                .temporal(new ProductSpec.Temporal(ProductSpec.Temporal.PASS, Duration.ofHours(12),
                        Duration.ofDays(LADDER_DAYS)))
                .spatial(new ProductSpec.Spatial("swath", NATIVE_MAX_ZOOM, 0))
                .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofDays(LADDER_DAYS)))
                .attribution(new ProductSpec.Attribution("Polar satellite imagery received by an operator-owned SatDump"
                        + " ground station", null, null, SatDumpDescriptor.LICENSE, null, false))
                .coverageMeaning("The pass's swath as SatDump projected it; transparent outside the swath")
                .defaultStyle("rgb@1")
                .legendSummary("A SatDump polar composite drawn in its own colors")
                .build();
    }

    /**
     * The {@code affine} grid of an image: {@code crs} and {@code gt0 … gt5}, and the WGS84 box of its
     * corners ({@code west > east} when it crosses ±180°).
     */
    public static GridDefinition grid(int width, int height, GeoReference georeference) {
        double west = Double.POSITIVE_INFINITY;
        double east = Double.NEGATIVE_INFINITY;
        double south = 90;
        double north = -90;
        for (double[] corner : new double[][] {{0, 0}, {width, 0}, {0, height}, {width, height}}) {
            double[] ll = georeference.lonLat(corner[0], corner[1]);
            west = Math.min(west, ll[0]);
            east = Math.max(east, ll[0]);
            south = Math.min(south, ll[1]);
            north = Math.max(north, ll[1]);
        }
        double w;
        double e;
        if (east - west >= 360) {
            w = -180;
            e = 180;
        } else {
            w = wrap(west);
            e = wrap(east);
            if (e == -180) {
                e = 180;
            }
            if (w == 180) {
                w = -180;
            }
        }
        if (!(south < north)) {
            north = Math.min(90, south + 1e-6);
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("crs", georeference.crs());
        double[] gt = georeference.gt();
        for (int k = 0; k < 6; k++) {
            params.put("gt" + k, gt[k]);
        }
        return new GridDefinition(GridDefinition.AFFINE, width, height, w, Math.max(-90, south), e, Math.min(90, north),
                params);
    }

    /** The approximate swath of a pass from its TLE, or empty. */
    Optional<MultiPolygon> swath(PolarPassProcessor.Contents contents, PassState pass) {
        Map<String, Object> tle = contents.tle() != null ? contents.tle() : pass.tle;
        if (tle == null || !(tle.get("line1") instanceof String line1) || !(tle.get("line2") instanceof String line2)) {
            return Optional.empty();
        }
        Double corr = null;
        String instrument = null;
        List<Double> times = new ArrayList<>();
        for (PolarPassProcessor.ProductDir p : contents.products()) {
            JsonNode projection = p.product().projection();
            if (instrument == null) {
                instrument = p.product().instrument();
            }
            if (projection != null) {
                if (corr == null && projection.path("corr_swath").isNumber()) {
                    corr = projection.path("corr_swath").asDouble();
                }
                for (JsonNode t : projection.path("timestamps")) {
                    if (t.isNumber()) {
                        times.add(t.asDouble());
                    }
                }
            }
        }
        Optional<Double> width = SwathEstimator.widthKm(corr, instrument);
        if (width.isEmpty()) {
            log.info("{}: no swath width for instrument {}; no approximate swath", station, instrument);
            return Optional.empty();
        }
        Instant[] window = window(times, contents.dataset().timestamp(), pass);
        try {
            return SwathEstimator.estimate(line1, line2, window[0], window[1], width.get()).map(SwathEstimator.Swath::footprint);
        } catch (RuntimeException | LinkageError e) {
            log.warn("{}: approximate swath failed: {}", station, e.toString());
            return Optional.empty();
        }
    }

    /**
     * The propagation window: the first and last plausible product timestamps (within a day of the
     * dataset timestamp); else the pass's AOS to LOS; else the dataset timestamp ±
     * {@link #LONE_TIMESTAMP_HALF_WINDOW}.
     */
    static Instant[] window(List<Double> epochSeconds, Instant datasetTime, PassState pass) {
        double reference = datasetTime.toEpochMilli() / 1000.0;
        double first = Double.POSITIVE_INFINITY;
        double last = Double.NEGATIVE_INFINITY;
        for (double t : epochSeconds) {
            if (Double.isFinite(t) && t > 0 && Math.abs(t - reference) <= 86_400) {
                first = Math.min(first, t);
                last = Math.max(last, t);
            }
        }
        if (Double.isFinite(first) && last - first >= 1) {
            return new Instant[] {Instant.ofEpochMilli(Math.round(first * 1000)), Instant.ofEpochMilli(Math.round(last * 1000))};
        }
        if (pass != null && pass.los != null && pass.los > pass.aos) {
            return new Instant[] {Instant.ofEpochMilli(pass.aos), Instant.ofEpochMilli(pass.los)};
        }
        Instant center = Double.isFinite(first) ? Instant.ofEpochMilli(Math.round(first * 1000)) : datasetTime;
        return new Instant[] {center.minus(LONE_TIMESTAMP_HALF_WINDOW), center.plus(LONE_TIMESTAMP_HALF_WINDOW)};
    }

    /** The product directory holding {@code file}, or null. */
    static PolarPassProcessor.ProductDir productOf(PolarPassProcessor.Contents contents, Path file) {
        PolarPassProcessor.ProductDir best = null;
        for (PolarPassProcessor.ProductDir p : contents.products()) {
            if (file.startsWith(p.directory())
                    && (best == null || p.directory().getNameCount() > best.directory().getNameCount())) {
                best = p;
            }
        }
        return best;
    }

    private static Instant validTime(PolarPassProcessor.ProductDir product, PolarPassProcessor.Contents contents) {
        if (product != null && product.product().timestamp() != null) {
            return product.product().timestamp();
        }
        return contents.dataset().timestamp();
    }

    /**
     * Merges overlays into the pass's product entries: an overlay whose composite matches the stem of a
     * stored composite image gains {@code productId}, {@code frameKey} and {@code tilesTemplate}; any other
     * becomes its own entry with the GeoTIFF's file name as {@code composite}.
     */
    public static List<Map<String, Object>> merge(List<Map<String, Object>> products, List<Overlay> overlays) {
        List<Map<String, Object>> out = new ArrayList<>();
        products.forEach(p -> out.add(new LinkedHashMap<>(p)));
        for (Overlay overlay : overlays) {
            Map<String, Object> target = null;
            for (Map<String, Object> p : out) {
                Object name = p.get("composite");
                if (name instanceof String s && !p.containsKey("productId")
                        && composite(s).equalsIgnoreCase(overlay.composite())) {
                    target = p;
                    break;
                }
            }
            if (target == null) {
                target = new LinkedHashMap<>();
                if (overlay.instrument() != null) {
                    target.put("instrument", overlay.instrument());
                }
                target.put("composite", overlay.file());
                out.add(target);
            }
            target.put("productId", overlay.productId());
            if (overlay.frameKey() != null) {
                target.put("frameKey", overlay.frameKey());
            }
            if (overlay.tilesTemplate() != null) {
                target.put("tilesTemplate", overlay.tilesTemplate());
            }
        }
        return out;
    }

    private static MultiPolygon multi(Geometry geometry) {
        Geometry fixed = GeometryFixer.fix(geometry);
        if (fixed instanceof MultiPolygon m) {
            return (MultiPolygon) Antimeridian.GEOMETRY.createGeometry(m);
        }
        List<Polygon> parts = new ArrayList<>();
        for (int i = 0; i < fixed.getNumGeometries(); i++) {
            if (fixed.getGeometryN(i) instanceof Polygon p && !p.isEmpty()) {
                parts.add(p);
            }
        }
        return Antimeridian.GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
    }

    private static double wrap(double lon) {
        double d = ((lon + 180) % 360 + 360) % 360 - 180;
        return d;
    }

    private static String text(Object value) {
        return value instanceof String s ? s : null;
    }
}
