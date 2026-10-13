package ai.foresignal.testsupport.raster;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Fixtures of the prompt 051 model ITs: synthetic global u/v frames for {@code gfs}, an in-memory
 * {@link SourceHttp} that serves the committed GFS 1° step (its {@code .idx} and four captured message
 * ranges) for whichever cycles and steps a test publishes, and cleanup of the model products.
 */
public final class ModelFixtures {

    /** The 1° template the ITs use (the fixtures come from the 1° file). */
    public static final String TEMPLATE_1P00 = "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.1p00.f{FFF}";

    /** The global 1° grid exactly as {@code Grib2Decoder} defines GFS's (lo1 0, la1 90). */
    public static final GridDefinition GLOBAL_1DEG = new GridDefinition(GridDefinition.LATLON, 360, 181, -180, -90, 180,
            90, Map.of("lo1", 0.0, "la1", 90.0, "dx", 1.0, "dy", 1.0));

    private ModelFixtures() {}

    /**
     * A synthetic {@code gfs} step on {@link #GLOBAL_1DEG}.
     *
     * @param u u in m/s at (column, row); column 0 is longitude 0, row 0 latitude 90
     * @param v v in m/s at (column, row)
     */
    public static FrameInput gfsStep(Instant run, int step, FieldFunction u, FieldFunction v) {
        float[] us = new float[360 * 181];
        float[] vs = new float[360 * 181];
        for (int r = 0; r < 181; r++) {
            for (int c = 0; c < 360; c++) {
                us[r * 360 + c] = u.apply(c, r);
                vs[r * 360 + c] = v.apply(c, r);
            }
        }
        return FrameInput.builder(GfsAdapter.WIND).forecast(run, step).grid(GLOBAL_1DEG)
                .bands(DecodedBands.physical(us, vs)).upstreamKey("synthetic/gfs/" + run + "/f" + step).build();
    }

    /** A float-valued function of (column, row). */
    @FunctionalInterface
    public interface FieldFunction {

        /** The value at {@code (column, row)}. */
        float apply(int column, int row);
    }

    /** A uniform field. */
    public static FieldFunction constant(float value) {
        return (c, r) -> value;
    }

    /** Removes every model frame row and file (the ITs share one context and database). */
    public static void clean(JdbcTemplate jdbc, RasterPaths paths) {
        jdbc.update("DELETE FROM raster_frame WHERE product_id IN (?, ?, ?, ?, ?, ?, ?, ?)", GfsAdapter.WIND,
                GfsAdapter.GUST, GfsAdapter.T2M, GfsAdapter.APCP, HrrrAdapter.WIND, HrrrAdapter.REFC, HrrrAdapter.GUST,
                HrrrAdapter.SMOKE);
        for (String product : List.of(GfsAdapter.WIND, GfsAdapter.GUST, GfsAdapter.T2M, GfsAdapter.APCP,
                HrrrAdapter.WIND)) {
            RasterFixtures.deleteTree(paths.productFrames(product));
            RasterFixtures.deleteTree(paths.fields().resolve(product));
        }
    }

    /**
     * The committed GFS 1° f006 step served for any published key: the f006 {@code .idx} (rewritten to
     * f003 wording for step 3) and the four captured ranges. Unpublished keys answer 404, like S3.
     */
    public static final class GfsStepHttp implements SourceHttp {

        private static final String IDX = new String(DecoderFixtures.gfsBytes("gfs.t12z.pgrb2.1p00.f006.idx"),
                StandardCharsets.US_ASCII);
        private final Map<String, byte[]> ranges = new LinkedHashMap<>();
        private final Set<String> published = new HashSet<>();
        private final List<String> requests = new ArrayList<>();

        /** Creates the stub. */
        public GfsStepHttp() {
            ranges.put("586148-641118", DecoderFixtures.gfsBytes("gfs-1p00-f006-gust.grib2"));
            ranges.put("35230937-35279061", DecoderFixtures.gfsBytes("gfs-1p00-f006-tmp2m.grib2"));
            ranges.put("35666549-35825559", DecoderFixtures.gfsBytes("gfs-1p00-f006-ugrd-vgrd.grib2"));
            ranges.put("36127136-36163721", DecoderFixtures.gfsBytes("gfs-1p00-f006-apcp.grib2"));
        }

        /** Publishes {@code key} (an object key without {@code .idx}). */
        public synchronized GfsStepHttp publish(String key) {
            published.add(key);
            return this;
        }

        /** Every request, as {@code GET path} or {@code RANGE path a-b}. */
        public synchronized List<String> requests() {
            return List.copyOf(requests);
        }

        @Override
        public synchronized UpstreamResponse get(URI uri) throws AdapterException {
            String path = uri.getPath().substring(1);
            requests.add("GET " + path);
            String key = path.endsWith(".idx") ? path.substring(0, path.length() - 4) : path;
            if (!path.endsWith(".idx") || !published.contains(key)) {
                throw new UpstreamHttpException(404, "NoSuchKey");
            }
            String idx = key.endsWith("f003") ? IDX.replace("0-6 hour acc", "0-3 hour acc")
                    .replace(":6 hour fcst", ":3 hour fcst") : IDX;
            return new UpstreamResponse(200, Map.of(), idx.getBytes(StandardCharsets.US_ASCII), "text/plain", false, false);
        }

        @Override
        public UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException {
            return get(uri);
        }

        @Override
        public UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException {
            return get(uri);
        }

        @Override
        public UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized UpstreamResponse getRange(URI uri, long start, long end, long maxBytes)
                throws AdapterException {
            String path = uri.getPath().substring(1);
            requests.add("RANGE " + path + " " + start + "-" + end);
            byte[] body = ranges.get(start + "-" + end);
            if (!published.contains(path) || body == null) {
                throw new UpstreamHttpException(404, "NoSuchKey");
            }
            return new UpstreamResponse(206, Map.of(), body, "application/octet-stream", false, false);
        }

        @Override
        public DownloadResult download(URI uri, Path target, long maxBytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public S3Listing listS3(String bucket, String prefix, String startAfter) {
            return new S3Listing(List.of(), false, null);
        }

        @Override
        public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers) {
            throw new UnsupportedOperationException();
        }
    }
}
