package ai.foresignal.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * A GPX 1.1 writer (prompt 111; history mode only): one {@code <trk>} per TRACK entity
 * ({@code <trkseg><trkpt>}) and a {@code <wpt>} for point records. XML is escaped.
 */
public final class GpxWriter implements ExportWriter {

    private BufferedWriter out;
    private long rows;
    private String currentTrack;
    private boolean inSegment;

    @Override
    public void open(Path file, ExportSchema schema, Map<String, String> metadata) throws IOException {
        out = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.write("<gpx version=\"1.1\" creator=\"Foresignal\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        out.write("  <metadata>\n");
        String copyright = metadata.getOrDefault("licence", "unknown");
        out.write("    <copyright author=\"" + esc(metadata.getOrDefault("attribution", "Foresignal")) + "\">\n");
        out.write("      <text>" + esc(copyright) + "</text>\n");
        out.write("    </copyright>\n");
        out.write("  </metadata>\n");
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        Double lat = row.get("lat") instanceof Number n ? n.doubleValue() : null;
        Double lon = row.get("lon") instanceof Number n ? n.doubleValue() : null;
        if (lat == null || lon == null) {
            return;
        }
        String sourceId = String.valueOf(row.getOrDefault("source_id", ""));
        String externalId = String.valueOf(row.getOrDefault("external_id", ""));
        Instant time = row.get("observed_at") instanceof Instant i ? i : null;
        Double ele = row.get("ele") instanceof Number n ? n.doubleValue() : null;
        boolean track = Boolean.TRUE.equals(row.get("track"));
        String key = sourceId + "/" + externalId;
        if (track) {
            if (!key.equals(currentTrack)) {
                closeSegment();
                if (currentTrack != null) {
                    out.write("    </trk>\n");
                }
                out.write("    <trk><name>" + esc(key) + "</name>\n");
                currentTrack = key;
            }
            if (!inSegment) {
                out.write("      <trkseg>\n");
                inSegment = true;
            }
            out.write("        <trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">");
            if (ele != null) {
                out.write("<ele>" + ele + "</ele>");
            }
            if (time != null) {
                out.write("<time>" + time + "</time>");
            }
            out.write("</trkpt>\n");
        } else {
            out.write("    <wpt lat=\"" + lat + "\" lon=\"" + lon + "\">");
            if (ele != null) {
                out.write("<ele>" + ele + "</ele>");
            }
            if (time != null) {
                out.write("<time>" + time + "</time>");
            }
            out.write("</wpt>\n");
        }
        rows++;
    }

    private void closeSegment() throws IOException {
        if (inSegment) {
            out.write("      </trkseg>\n");
            inSegment = false;
        }
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        closeSegment();
        if (currentTrack != null) {
            out.write("    </trk>\n");
        }
        out.write("</gpx>\n");
        out.close();
    }

    private static String esc(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
