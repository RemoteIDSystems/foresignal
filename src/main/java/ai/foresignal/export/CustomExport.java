package ai.foresignal.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * An export whose rows do not come from {@code record_version} (prompt 131: chronicle storage). It runs on the same
 * bounded {@link ExportWorker}, under the same quota, and is published like any other job: fsynced and renamed into
 * the job directory, with the signed manifest and the license and attribution sidecars written by
 * {@link ExportJobService}.
 */
public interface CustomExport {

    /** The file's media type. */
    String mediaType();

    /** The file extension, without a dot. */
    String extension();

    /**
     * Writes the export file.
     *
     * @param file the temporary file to write
     * @param canceled true once the job was canceled; checked between pages
     * @return what was written
     * @throws IOException when the file cannot be written
     */
    Result write(Path file, BooleanSupplier canceled) throws IOException;

    /**
     * What a custom export wrote.
     *
     * @param rows the rows written
     * @param rowsBySource rows per source, for the license summary
     * @param watermarkSeq the highest seq the export covers, or null
     */
    record Result(long rows, Map<String, Long> rowsBySource, Long watermarkSeq) {

        /** Copies the map. */
        public Result {
            rowsBySource = rowsBySource == null ? Map.of() : Map.copyOf(rowsBySource);
        }
    }
}
