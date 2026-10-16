package ai.foresignal.export;

/** Creates the format writer for an export (prompt 111). */
public final class ExportWriterFactory {

    private ExportWriterFactory() {}

    /** A new writer for {@code format}. */
    public static ExportWriter create(ExportFormat format) {
        return switch (format) {
            case GEOPARQUET -> new GeoParquetWriter();
            case GEOPACKAGE -> new GeoPackageWriter();
            case FLATGEOBUF -> new FlatGeobufWriter();
            case GPX -> new GpxWriter();
        };
    }
}
