package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.util.List;

/** Creates the format writer for an export (prompt 111). */
public final class ExportWriterFactory {

    private ExportWriterFactory() {}

    /** A writer for {@code format}. */
    public static ExportWriter create(ExportFormat format) {
        return switch (format) {
            case GEOPARQUET -> new GeoParquetWriter();
            case GEOPACKAGE -> new GeoPackageWriter();
            case GPX -> new GpxWriter();
            // FlatGeobuf's Java library exposes only low-level building blocks; deferred (see the CR).
            case FLATGEOBUF -> throw new ValidationException(ErrorCodes.VALIDATION_FAILED,
                    "The flatgeobuf format is not available on this build; use geoparquet, geopackage or gpx.",
                    List.of());
        };
    }
}
