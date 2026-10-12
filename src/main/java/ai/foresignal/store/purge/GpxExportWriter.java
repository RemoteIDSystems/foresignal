package ai.foresignal.store.purge;

import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code format=gpx}: GPX 1.1. For kind {@code tracker} the point versions become one
 * {@code trk/trkseg} of {@code trkpt}s (lat, lon, {@code ele} from {@code payload.altitudeM},
 * {@code time} from {@code observedAt}); the current state is appended when it is newer than the last
 * version. Other kinds are written as {@code wpt}s. Tombstones, missing and non-point geometries are
 * skipped, and alerts are not representable in GPX: the manifest lists their ids. The manifest is
 * JSON inside {@code <extensions><foresignal:manifest>} at the end of the document; the hash covers
 * every byte before {@code <extensions>}.
 */
public class GpxExportWriter extends ExportWriter {

    /** Opens the manifest. */
    public static final String MARKER = "<extensions>";

    /** The namespace of the manifest element. */
    public static final String NAMESPACE = "https://foresignal.ai/gpx/1";

    private final JsonMapper json;
    private boolean track;
    private String name;
    private boolean trackOpen;
    private ExportItem pendingCurrent;
    private Instant lastObserved;
    private int points;

    /** Creates a writer over {@code out}. */
    public GpxExportWriter(OutputStream out, JsonMapper json) {
        super(out);
        this.json = json;
    }

    @Override
    public String manifestMarker() {
        return MARKER;
    }

    @Override
    public void start(String sourceId, String externalId, String kind) {
        track = "tracker".equals(kind);
        name = sourceId + ":" + externalId;
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<gpx version=\"1.1\" creator=\"Foresignal\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n"
                + "<metadata><name>" + escape(name) + "</name></metadata>\n");
    }

    @Override
    public void current(ExportItem item) {
        pendingCurrent = item;
    }

    @Override
    public void version(ExportItem item) {
        point(item);
    }

    @Override
    public void alert(Map<String, Object> alert) {
        // GPX has no place for alerts; the manifest lists them.
    }

    /** How many points were written. */
    public int points() {
        return points;
    }

    @Override
    protected void endContent() {
        if (pendingCurrent != null && (lastObserved == null
                || (pendingCurrent.observedAt() != null && pendingCurrent.observedAt().isAfter(lastObserved)))) {
            point(pendingCurrent);
        }
        if (trackOpen) {
            write("</trkseg></trk>\n");
        }
    }

    @Override
    protected void writeManifest(Map<String, Object> manifest) {
        rawWrite(MARKER + "<foresignal:manifest xmlns:foresignal=\"" + NAMESPACE + "\">"
                + escape(json.writeValueAsString(manifest)) + "</foresignal:manifest></extensions>\n</gpx>\n");
    }

    private void point(ExportItem item) {
        if (item.tombstone() || item.geometryJson() == null) {
            return;
        }
        double[] lonLat = lonLat(item.geometryJson());
        if (lonLat == null) {
            return;
        }
        String element = track ? "trkpt" : "wpt";
        if (track && !trackOpen) {
            write("<trk><name>" + escape(name) + "</name><trkseg>\n");
            trackOpen = true;
        }
        StringBuilder out = new StringBuilder();
        out.append('<').append(element).append(" lat=\"").append(lonLat[1]).append("\" lon=\"").append(lonLat[0])
                .append("\">");
        if (item.altitudeM() != null) {
            out.append("<ele>").append(item.altitudeM()).append("</ele>");
        }
        if (item.observedAt() != null) {
            out.append("<time>").append(item.observedAt()).append("</time>");
        }
        out.append("</").append(element).append(">\n");
        write(out.toString());
        points++;
        if (item.observedAt() != null && (lastObserved == null || item.observedAt().isAfter(lastObserved))) {
            lastObserved = item.observedAt();
        }
    }

    private double[] lonLat(String geometry) {
        try {
            JsonNode node = json.readTree(geometry);
            if (!"Point".equals(node.path("type").asString(""))) {
                return null;
            }
            JsonNode c = node.path("coordinates");
            if (c.size() < 2 || !c.get(0).isNumber() || !c.get(1).isNumber()) {
                return null;
            }
            return new double[] {c.get(0).asDouble(), c.get(1).asDouble()};
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** XML text escaping. */
    static String escape(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> out.append(ch);
            }
        }
        return out.toString();
    }
}
