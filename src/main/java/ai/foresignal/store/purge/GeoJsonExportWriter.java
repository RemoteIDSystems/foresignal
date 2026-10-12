package ai.foresignal.store.purge;

import ai.foresignal.api.support.RawJson;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code format=geojson}: {@code {"type":"FeatureCollection","features":[…],"alerts":[…],"manifest":{…}}}.
 * Features are the current state, then every version in seq order; each feature's properties are the
 * {@code Record} object without its geometry, plus {@code exportPart} ({@code current} or
 * {@code version}). The hash covers every byte before {@code "manifest":}.
 */
public class GeoJsonExportWriter extends ExportWriter {

    /** Opens the manifest member. */
    public static final String MARKER = "\"manifest\":";

    private final JsonMapper json;
    private boolean firstFeature = true;
    private boolean inAlerts;
    private boolean firstAlert = true;

    /** Creates a writer over {@code out}. */
    public GeoJsonExportWriter(OutputStream out, JsonMapper json) {
        super(out);
        this.json = json;
    }

    @Override
    public String manifestMarker() {
        return MARKER;
    }

    @Override
    public void start(String sourceId, String externalId, String kind) {
        write("{\"type\":\"FeatureCollection\",\"features\":[");
    }

    @Override
    public void current(ExportItem item) {
        feature(item, "current");
    }

    @Override
    public void version(ExportItem item) {
        feature(item, "version");
    }

    @Override
    public void alert(Map<String, Object> alert) {
        openAlerts();
        if (!firstAlert) {
            write(",");
        }
        firstAlert = false;
        write(json.writeValueAsBytes(alert));
    }

    @Override
    protected void endContent() {
        openAlerts();
        write("],");
    }

    @Override
    protected void writeManifest(Map<String, Object> manifest) {
        rawWrite(MARKER + json.writeValueAsString(manifest) + "}");
    }

    private void openAlerts() {
        if (!inAlerts) {
            write("],\"alerts\":[");
            inAlerts = true;
        }
    }

    private void feature(ExportItem item, String part) {
        Map<String, Object> properties = new LinkedHashMap<>(item.record());
        properties.remove("geometry");
        properties.put("exportPart", part);
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", item.record().get("id"));
        feature.put("geometry", item.geometryJson() == null ? null : RawJson.of(item.geometryJson()));
        feature.put("properties", properties);
        if (!firstFeature) {
            write(",");
        }
        firstFeature = false;
        write(json.writeValueAsBytes(feature));
    }
}
