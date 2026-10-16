package ai.foresignal.raster.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.export.pmtiles.PmtilesWriter;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.source.spi.ProductSpec;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Builds the files of a raster export (prompt 108; Weather-Imagery-Design §11): MBTiles, PMTiles or
 * COG for one or more frames of a product over a box and zoom range. Caps: at most
 * {@value #MAX_TILES} tiles per tileset and {@value #MAX_FILE_BYTES} bytes per file.
 */
@Service
public class RasterExportService {

    /** The tile cap per tileset. */
    public static final long MAX_TILES = 50_000;

    /** The per-file size cap. */
    public static final long MAX_FILE_BYTES = 2L * 1024 * 1024 * 1024;

    /** The default maximum output side of a COG, pixels. */
    private static final int COG_MAX_SIDE = 2048;

    private final RasterRenderService renderer;
    private final ExportPaths paths;
    private final ai.foresignal.raster.FrameStore store;

    /** Creates the service. */
    public RasterExportService(RasterRenderService renderer, ExportPaths paths, FrameStore store) {
        this.renderer = renderer;
        this.paths = paths;
        this.store = store;
    }

    /** A parsed export request. */
    public record Request(String format, List<String> frameKeys, double[] bbox, int minZoom, int maxZoom, String style,
            TileMatrixSet tms) {}

    /** One written file. */
    public record File(String name, String path, long bytes, String sha256, String mediaType) {}

    /** The export result. */
    public record Result(List<File> files, long tileCount, long totalBytes) {}

    /** Builds the export files under {@code exports/{jobId}/}. */
    public Result export(ProductRegistry.Registered product, Request request, List<RasterFrameRow> frames, String jobId,
            String client) {
        ProductSpec spec = product.spec();
        StyleRegistry.Resolved style = renderer.style(spec, request.style());
        List<File> files = new ArrayList<>();
        long totalTiles = 0;
        long totalBytes = 0;
        for (RasterFrameRow frame : frames) {
            List<byte[]> tiles = new ArrayList<>();
            List<int[]> addresses = new ArrayList<>();
            countTiles(request);
            iterateTiles(request, (z, x, y) -> {
                byte[] bytes = renderer.tile(spec, frame, style, request.tms(), z, x, y, 256, "png",
                        RenderQueue.Priority.PREFETCH, client);
                if (bytes != TileCache.EMPTY && bytes.length > 0) {
                    tiles.add(bytes);
                    addresses.add(new int[] {z, x, y});
                }
            });
            File file = switch (request.format()) {
                case "mbtiles" -> writeMbtiles(spec, frame, request, tiles, addresses, jobId);
                case "pmtiles" -> writePmtiles(spec, frame, request, tiles, addresses, jobId);
                case "cog" -> writeCog(spec, frame, request, jobId);
                default -> throw ai.foresignal.api.error.ValidationException.forField("format",
                        "must be mbtiles, pmtiles or cog");
            };
            if (file.bytes() > MAX_FILE_BYTES) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED,
                        "The export file " + file.name() + " is " + file.bytes() + " bytes; at most " + MAX_FILE_BYTES);
            }
            files.add(file);
            totalTiles += tiles.size();
            totalBytes += file.bytes();
        }
        return new Result(files, totalTiles, totalBytes);
    }

    /** Counts the tiles a request covers and refuses above {@link #MAX_TILES}. */
    public long countTiles(Request request) {
        long total = 0;
        for (int z = request.minZoom(); z <= request.maxZoom(); z++) {
            int[] range = request.tms().tileRange(z, request.bbox()[0], request.bbox()[1], request.bbox()[2],
                    request.bbox()[3]);
            long rows = (long) range[3] - range[1] + 1;
            long cols = (long) range[2] - range[0] + 1;
            total += Math.max(0, rows) * Math.max(0, cols);
            if (total > MAX_TILES) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED,
                        "The export covers more than " + MAX_TILES + " tiles; raise minZoom or shrink bbox.");
            }
        }
        return total;
    }

    private interface TileVisitor {
        void tile(int z, int x, int y);
    }

    private void iterateTiles(Request request, TileVisitor visitor) {
        for (int z = request.minZoom(); z <= request.maxZoom(); z++) {
            int[] range = request.tms().tileRange(z, request.bbox()[0], request.bbox()[1], request.bbox()[2],
                    request.bbox()[3]);
            for (int y = range[1]; y <= range[3]; y++) {
                for (int x = range[0]; x <= range[2]; x++) {
                    visitor.tile(z, x, y);
                }
            }
        }
    }

    private File writeMbtiles(ProductSpec spec, RasterFrameRow frame, Request request, List<byte[]> tiles,
            List<int[]> addresses, String jobId) {
        List<MbtilesWriter.Tile> out = new ArrayList<>();
        for (int k = 0; k < tiles.size(); k++) {
            int[] a = addresses.get(k);
            out.add(new MbtilesWriter.Tile(a[0], a[1], a[2], tiles.get(k)));
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("name", spec.title());
        metadata.put("bounds", boundsText(request.bbox()));
        metadata.put("minzoom", String.valueOf(request.minZoom()));
        metadata.put("maxzoom", String.valueOf(request.maxZoom()));
        metadata.put("json", frameJson(frame));
        if (spec.attribution() != null) {
            metadata.put("attribution", spec.attribution().text());
        }
        String name = spec.id() + "-" + frame.frameKey() + ".mbtiles";
        Path target = paths.file(jobId, name);
        MbtilesWriter.write(target, out, metadata, "png");
        return file(name, target, "application/vnd.sqlite3");
    }

    private File writePmtiles(ProductSpec spec, RasterFrameRow frame, Request request, List<byte[]> tiles,
            List<int[]> addresses, String jobId) {
        List<PmtilesWriter.Tile> out = new ArrayList<>();
        for (int k = 0; k < tiles.size(); k++) {
            int[] a = addresses.get(k);
            out.add(new PmtilesWriter.Tile(a[0], a[1], a[2], tiles.get(k)));
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", spec.title());
        metadata.put("type", "overlay");
        if (spec.attribution() != null) {
            metadata.put("attribution", spec.attribution().text());
        }
        metadata.put("bounds", boundsText(request.bbox()));
        metadata.put("json", frameJson(frame));
        byte[] bytes = PmtilesWriter.write(out, metadata, request.bbox(), request.minZoom(), request.maxZoom(),
                PmtilesWriter.TYPE_PNG);
        String name = spec.id() + "-" + frame.frameKey() + ".pmtiles";
        Path target = paths.file(jobId, name);
        writeBytes(target, bytes);
        return new File(name, target.toString(), bytes.length, FrameStore.sha256Hex(bytes),
                "application/vnd.pmtiles");
    }

    private File writeCog(ProductSpec spec, RasterFrameRow frame, Request request, String jobId) {
        double[] box = request.bbox();
        double spanX = Math.max(box[2] - box[0], 1e-6);
        double spanY = Math.max(box[3] - box[1], 1e-6);
        int width = COG_MAX_SIDE;
        int height = Math.max(1, (int) Math.round(width * spanY / spanX));
        if (height > COG_MAX_SIDE) {
            height = COG_MAX_SIDE;
            width = Math.max(1, (int) Math.round(height * spanX / spanY));
        }
        float[] values = renderer.values(spec, frame, box[0], box[1], box[2], box[3], width, height, false);
        double nodata = spec.nodata() == null ? -9999.0 : spec.nodata();
        for (int k = 0; k < values.length; k++) {
            if (Float.isNaN(values[k])) {
                values[k] = (float) nodata;
            }
        }
        byte[] bytes = CogWriter.write(values, width, height, box, 4326, nodata, null);
        String name = spec.id() + "-" + frame.frameKey() + ".tif";
        Path target = paths.file(jobId, name);
        writeBytes(target, bytes);
        return new File(name, target.toString(), bytes.length, FrameStore.sha256Hex(bytes),
                "image/tiff; application=geotiff; profile=cloud-optimized");
    }

    private File file(String name, Path target, String mediaType) {
        byte[] bytes = read(target);
        return new File(name, target.toString(), bytes.length, FrameStore.sha256Hex(bytes), mediaType);
    }

    private static byte[] read(Path path) {
        try {
            return java.nio.file.Files.readAllBytes(path);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException("Could not read export file " + path, e);
        }
    }

    private static void writeBytes(Path target, byte[] bytes) {
        try {
            java.nio.file.Files.write(target, bytes);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException("Could not write export file " + target, e);
        }
    }

    private static String frameJson(RasterFrameRow frame) {
        return "{\"frameKey\":\"" + frame.frameKey() + "\",\"validTime\":\"" + frame.validTime() + "\"}";
    }

    static String boundsText(double[] bbox) {
        return bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3];
    }
}
