package ai.foresignal.export;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import java.util.Locale;

/** The bulk export formats (prompt 111). */
public enum ExportFormat {

    /** GeoParquet 1.1 (the default). */
    GEOPARQUET("geoparquet", "application/vnd.apache.parquet"),
    /** OGC GeoPackage 1.4. */
    GEOPACKAGE("geopackage", "application/geopackage+sqlite3"),
    /** FlatGeobuf. */
    FLATGEOBUF("flatgeobuf", "application/flatgeobuf"),
    /** GPX 1.1 (history mode only). */
    GPX("gpx", "application/gpx+xml");

    private final String wire;
    private final String mediaType;

    ExportFormat(String wire, String mediaType) {
        this.wire = wire;
        this.mediaType = mediaType;
    }

    /** The wire value ({@code geoparquet}, ...). */
    public String wire() {
        return wire;
    }

    /** The media type. */
    public String mediaType() {
        return mediaType;
    }

    /** The file extension (without a dot). */
    public String extension() {
        return this == GEOPARQUET ? "parquet" : this == GEOPACKAGE ? "gpkg" : wire;
    }

    /** Parses a format, defaulting to {@code geoparquet}. */
    public static ExportFormat parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return GEOPARQUET;
        }
        String value = raw.strip().toLowerCase(Locale.ROOT);
        for (ExportFormat format : values()) {
            if (format.wire.equals(value)) {
                return format;
            }
        }
        throw ValidationException.forField("format", "must be geoparquet, geopackage, flatgeobuf or gpx");
    }
}
