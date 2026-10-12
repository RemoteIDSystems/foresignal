package ai.foresignal.api.support;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.SequenceWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

/**
 * Writes one streaming response, item by item, with no paging:
 *
 * <ul>
 *   <li>{@code ndjson} ({@code application/x-ndjson}): one JSON object per line;
 *   <li>{@code geojsonseq} ({@code application/geo+json-seq}, RFC 8142): each Feature preceded by
 *       the record separator {@code 0x1E} and followed by a line feed;
 *   <li>{@code csv} ({@code text/csv}): a header row, then one row per item.
 * </ul>
 *
 * <p>I/O failures are rethrown as {@link UncheckedIOException}: the writer runs inside the read
 * transaction's row callback, which cannot throw checked exceptions.
 */
public final class StreamWriter implements AutoCloseable {

    /** The RFC 8142 record separator. */
    public static final int RECORD_SEPARATOR = 0x1E;

    private static final CsvMapper CSV = CsvMapper.builder().build();

    private final ResponseFormat format;
    private final OutputStream out;
    private final JsonMapper json;
    private final Writer csvWriter;
    private final SequenceWriter csvRows;

    /**
     * Opens a writer.
     *
     * @param format a streaming format
     * @param out the response body
     * @param json the application's JSON mapper
     * @param csvColumns the CSV header; ignored for other formats
     */
    public StreamWriter(ResponseFormat format, OutputStream out, JsonMapper json, List<String> csvColumns) {
        if (!format.streaming()) {
            throw new IllegalArgumentException(format + " is not a streaming format");
        }
        this.format = format;
        this.out = out;
        this.json = json;
        if (format == ResponseFormat.CSV) {
            CsvSchema.Builder schema = CsvSchema.builder().setUseHeader(true);
            for (String column : csvColumns) {
                schema.addColumn(column);
            }
            this.csvWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            this.csvRows = CSV.writer(schema.build()).writeValues(csvWriter);
        } else {
            this.csvWriter = null;
            this.csvRows = null;
        }
    }

    /** Writes one item: a JSON object (ndjson), a Feature (geojsonseq) or a CSV row map (csv). */
    public void write(Map<String, Object> item) {
        try {
            switch (format) {
                case NDJSON -> {
                    out.write(json.writeValueAsBytes(item));
                    out.write('\n');
                }
                case GEOJSONSEQ -> {
                    out.write(RECORD_SEPARATOR);
                    out.write(json.writeValueAsBytes(item));
                    out.write('\n');
                }
                case CSV -> csvRows.write(item);
                default -> throw new IllegalStateException("Not a streaming format: " + format);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Flushes and finishes the body (the CSV sequence is closed; the stream itself is not). */
    @Override
    public void close() {
        try {
            if (csvRows != null) {
                csvRows.flush();
                csvWriter.flush();
            }
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
