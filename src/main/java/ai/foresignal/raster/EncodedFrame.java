package ai.foresignal.raster;

import java.util.Map;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A frame after quantization: what the pipeline stores.
 *
 * @param header the {@code .fsg} header (deflate codec)
 * @param decoded the decoded, band-sequential payload
 * @param stats statistics in physical units ({@code FrameStats} in the spec); never null values
 * @param footprint the grid box clipped to rows with valid data, or null when no sample is valid
 */
public record EncodedFrame(FsgHeader header, byte[] decoded, Map<String, Object> stats, MultiPolygon footprint) {}
