package ai.foresignal.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * A bulk-export format writer (prompt 111). The job service opens one per output file and streams
 * rows; a writer never holds the whole result in memory (formats that must reorder rows, such as
 * FlatGeobuf with its spatial index or GPX with waypoints before tracks, spill to temporary files).
 *
 * <p>A row is a map holding the {@link ExportSchema} columns by name plus these extra keys:
 *
 * <ul>
 *   <li>{@value #GEOMETRY}: the geometry as WKB ({@code byte[]}), absent when the record has none;
 *   <li>{@value #BBOX}: its envelope {@code double[]{xmin, ymin, xmax, ymax}};
 *   <li>{@value #TRACK}: {@code Boolean.TRUE} when the record comes from a TRACK-tier source (GPX);
 *   <li>{@value #ELEVATION}: the record's altitude in meters ({@code Double}), when known (GPX).
 * </ul>
 */
public interface ExportWriter extends AutoCloseable {

    /** The row key of the WKB geometry. */
    String GEOMETRY = "geometry";

    /** The row key of the geometry envelope. */
    String BBOX = "bbox";

    /** The row key of the TRACK-tier flag. */
    String TRACK = "track";

    /** The row key of the altitude in meters. */
    String ELEVATION = "elevation";

    /**
     * Opens {@code file} and writes the header.
     *
     * @param metadata format-level metadata: {@code license} (the license summary JSON),
     *     {@code attribution} (one line per attribution), {@code name} and {@code licenseUrl} when known
     */
    void open(Path file, ExportSchema schema, Map<String, String> metadata) throws IOException;

    /** Writes one row. */
    void write(Map<String, Object> row) throws IOException;

    /** Rows written so far. */
    long rows();

    /** Finishes the file; a writer that was never opened closes without effect. */
    @Override
    void close() throws IOException;
}
