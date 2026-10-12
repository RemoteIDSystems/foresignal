package ai.foresignal.store.purge;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code format=ndjson}: one JSON object per line, each typed: {@code {"type":"current","record":{…}}},
 * {@code {"type":"version","record":{…}}} in seq order, {@code {"type":"alert","alert":{…}}}, and last
 * {@code {"type":"manifest","manifest":{…}}}. The hash covers every line before the manifest line,
 * newlines included.
 */
public class NdjsonExportWriter extends ExportWriter {

    /** Opens the manifest line. */
    public static final String MARKER = "{\"type\":\"manifest\"";

    private final JsonMapper json;

    /** Creates a writer over {@code out}. */
    public NdjsonExportWriter(OutputStream out, JsonMapper json) {
        super(out);
        this.json = json;
    }

    @Override
    public String manifestMarker() {
        return MARKER;
    }

    @Override
    public void start(String sourceId, String externalId, String kind) {
        // No header line: every line is an item.
    }

    @Override
    public void current(ExportItem item) {
        line("current", "record", item.record());
    }

    @Override
    public void version(ExportItem item) {
        line("version", "record", item.record());
    }

    @Override
    public void alert(Map<String, Object> alert) {
        line("alert", "alert", alert);
    }

    @Override
    protected void endContent() {
        // Nothing closes the content.
    }

    @Override
    protected void writeManifest(Map<String, Object> manifest) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("type", "manifest");
        line.put("manifest", manifest);
        rawWrite(json.writeValueAsString(line) + "\n");
    }

    private void line(String type, String member, Map<String, Object> value) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("type", type);
        line.put(member, value);
        write(json.writeValueAsString(line) + "\n");
    }
}
