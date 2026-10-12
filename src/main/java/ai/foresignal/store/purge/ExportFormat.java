package ai.foresignal.store.purge;

import ai.foresignal.api.error.InvalidInputException;
import java.util.Locale;

/** The formats of {@code exportRecord} (prompt 039). */
public enum ExportFormat {
    /** A GeoJSON FeatureCollection with {@code alerts} and {@code manifest} members. */
    GEOJSON("geojson", "application/geo+json", "geojson"),
    /** Newline-delimited JSON, one typed line per item, the manifest last. */
    NDJSON("ndjson", "application/x-ndjson", "ndjson"),
    /** GPX 1.1: a {@code trk} for kind {@code tracker}, waypoints for other point kinds. */
    GPX("gpx", "application/gpx+xml", "gpx");

    private final String wire;
    private final String mediaType;
    private final String extension;

    ExportFormat(String wire, String mediaType, String extension) {
        this.wire = wire;
        this.mediaType = mediaType;
        this.extension = extension;
    }

    /** The query value. */
    public String wireValue() {
        return wire;
    }

    /** The response media type. */
    public String mediaType() {
        return mediaType;
    }

    /** The file extension of the download. */
    public String extension() {
        return extension;
    }

    /**
     * The format named {@code value}; {@link #GEOJSON} when null or blank.
     *
     * @throws InvalidInputException for an unknown name
     */
    public static ExportFormat parse(String value) {
        if (value == null || value.isBlank()) {
            return GEOJSON;
        }
        String v = value.strip().toLowerCase(Locale.ROOT);
        for (ExportFormat f : values()) {
            if (f.wire.equals(v)) {
                return f;
            }
        }
        throw new InvalidInputException("format must be one of geojson, ndjson or gpx.");
    }
}
