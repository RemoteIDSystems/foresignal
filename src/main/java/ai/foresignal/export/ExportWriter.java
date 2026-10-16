package ai.foresignal.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * A bulk-export format writer (prompt 111). The job service opens one per output file and streams rows;
 * a writer never holds the whole result in memory.
 */
public interface ExportWriter extends AutoCloseable {

    /** Opens {@code file} and writes the header/metadata. */
    void open(Path file, ExportSchema schema, Map<String, String> metadata) throws IOException;

    /** Writes one row; {@code row} holds the schema columns by name plus {@code geometry} (WKB) and {@code bbox}. */
    void write(Map<String, Object> row) throws IOException;

    /** Rows written. */
    long rows();

    @Override
    void close() throws IOException;
}
