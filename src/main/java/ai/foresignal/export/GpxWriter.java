package ai.foresignal.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * A GPX 1.1 writer (prompt 111; history mode only).
 *
 * <ul>
 *   <li>A record of a TRACK-tier source ({@value ExportWriter#TRACK} true) becomes a {@code <trkpt>}
 *       of one {@code <trk>} per entity (the export query delivers an entity's versions together, in
 *       seq order), with {@code <ele>} from the payload altitude in meters when known and
 *       {@code <time>} from {@code observed_at}.
 *   <li>Any other record with a point geometry becomes a {@code <wpt>}.
 *   <li>Coordinates come from the point geometry; records without a point geometry are skipped
 *       ({@link #skipped()}), since GPX carries only points.
 * </ul>
 *
 * <p>The GPX 1.1 schema puts every {@code <wpt>} before every {@code <trk>}, so waypoints and tracks
 * are streamed to two temporary files and concatenated in schema order at close, after a
 * {@code <metadata>} block in schema order: {@code name}, {@code desc}, {@code copyright} (attribute
 * {@code author}, children {@code year} and {@code license}), {@code link}, {@code time},
 * {@code bounds}. Text and attributes are XML-escaped.
 */
public final class GpxWriter implements ExportWriter {

    /** The GPX 1.1 namespace. */
    public static final String NAMESPACE = "http://www.topografix.com/GPX/1/1";

    private Path file;
    private Path waypointsFile;
    private Path tracksFile;
    private BufferedWriter waypoints;
    private BufferedWriter tracks;
    private Map<String, String> metadata;
    private long rows;
    private long skipped;
    private String currentTrack;
    private double minLat = Double.NaN;
    private double minLon = Double.NaN;
    private double maxLat = Double.NaN;
    private double maxLon = Double.NaN;
    private boolean closed;

    @Override
    public void open(Path target, ExportSchema schema, Map<String, String> meta) throws IOException {
        this.file = target;
        this.metadata = meta == null ? Map.of() : meta;
        Path dir = target.toAbsolutePath().getParent();
        this.waypointsFile = Files.createTempFile(dir, "gpx-wpt-", ".part");
        this.tracksFile = Files.createTempFile(dir, "gpx-trk-", ".part");
        this.waypoints = Files.newBufferedWriter(waypointsFile, StandardCharsets.UTF_8);
        this.tracks = Files.newBufferedWriter(tracksFile, StandardCharsets.UTF_8);
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        double[] latLon = latLon(row.get(GEOMETRY));
        if (latLon == null) {
            skipped++;
            return;
        }
        double lat = latLon[0];
        double lon = latLon[1];
        Instant time = row.get("observed_at") instanceof Instant i ? i : null;
        Double ele = row.get(ELEVATION) instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : null;
        String sourceId = String.valueOf(row.getOrDefault("source_id", ""));
        String externalId = String.valueOf(row.getOrDefault("external_id", ""));
        if (Boolean.TRUE.equals(row.get(TRACK))) {
            String key = sourceId + "/" + externalId;
            if (!key.equals(currentTrack)) {
                endTrack();
                tracks.write("  <trk>\n    <name>" + escape(key) + "</name>\n");
                if (row.get("kind") != null) {
                    tracks.write("    <type>" + escape(String.valueOf(row.get("kind"))) + "</type>\n");
                }
                tracks.write("    <trkseg>\n");
                currentTrack = key;
            }
            tracks.write("      <trkpt lat=\"" + decimal(lat) + "\" lon=\"" + decimal(lon) + "\">");
            point(tracks, ele, time);
            tracks.write("</trkpt>\n");
        } else {
            waypoints.write("  <wpt lat=\"" + decimal(lat) + "\" lon=\"" + decimal(lon) + "\">");
            point(waypoints, ele, time);
            Object title = row.get("title");
            waypoints.write("<name>" + escape(title == null ? externalId : String.valueOf(title)) + "</name>");
            waypoints.write("<desc>" + escape(sourceId + "/" + externalId) + "</desc>");
            waypoints.write("<src>" + escape(sourceId) + "</src>");
            if (row.get("kind") != null) {
                waypoints.write("<type>" + escape(String.valueOf(row.get("kind"))) + "</type>");
            }
            waypoints.write("</wpt>\n");
        }
        expand(lat, lon);
        rows++;
    }

    private static void point(Writer out, Double ele, Instant time) throws IOException {
        if (ele != null) {
            out.write("<ele>" + decimal(ele) + "</ele>");
        }
        if (time != null) {
            out.write("<time>" + DateTimeFormatter.ISO_INSTANT.format(time.truncatedTo(ChronoUnit.MILLIS)) + "</time>");
        }
    }

    /**
     * {@code [lat, lon]} of a WKB point, with the longitude normalized into GPX's {@code [-180, 180)};
     * null for anything that is not a non-empty point.
     */
    static double[] latLon(Object geometry) {
        if (!(geometry instanceof byte[] wkb) || wkb.length == 0) {
            return null;
        }
        Wkb.Header header = Wkb.header(wkb);
        if (header == null || header.baseType() != 1) {
            return null;
        }
        Geometry parsed = Wkb.parse(wkb);
        if (!(parsed instanceof Point point) || point.isEmpty()) {
            return null;
        }
        double lon = point.getX();
        double lat = point.getY();
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || lat < -90 || lat > 90) {
            return null;
        }
        lon = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return new double[] {lat, lon};
    }

    private void endTrack() throws IOException {
        if (currentTrack != null) {
            tracks.write("    </trkseg>\n  </trk>\n");
            currentTrack = null;
        }
    }

    private void expand(double lat, double lon) {
        minLat = Double.isNaN(minLat) ? lat : Math.min(minLat, lat);
        maxLat = Double.isNaN(maxLat) ? lat : Math.max(maxLat, lat);
        minLon = Double.isNaN(minLon) ? lon : Math.min(minLon, lon);
        maxLon = Double.isNaN(maxLon) ? lon : Math.max(maxLon, lon);
    }

    /** Rows without a point geometry that were left out. */
    public long skipped() {
        return skipped;
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        if (closed || waypoints == null) {
            closed = true;
            return;
        }
        closed = true;
        endTrack();
        waypoints.close();
        tracks.close();
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<gpx version=\"1.1\" creator=\"Foresignal\" xmlns=\"" + NAMESPACE + "\">\n");
            writeMetadata(out);
            append(out, waypointsFile);
            append(out, tracksFile);
            out.write("</gpx>\n");
        } finally {
            Files.deleteIfExists(waypointsFile);
            Files.deleteIfExists(tracksFile);
        }
    }

    private void writeMetadata(Writer out) throws IOException {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        out.write("  <metadata>\n");
        out.write("    <name>" + escape(metadata.getOrDefault("name", "Foresignal export")) + "</name>\n");
        out.write("    <desc>" + escape("Foresignal history export. " + metadata.getOrDefault("attribution", ""))
                .strip() + "</desc>\n");
        String author = metadata.getOrDefault("attribution", "");
        out.write("    <copyright author=\"" + escape(author.isBlank() ? "Foresignal" : author) + "\">\n");
        out.write("      <year>" + now.atZone(ZoneOffset.UTC).getYear() + "</year>\n");
        String licenseUrl = metadata.get("licenseUrl");
        if (isUri(licenseUrl)) {
            out.write("      <license>" + escape(licenseUrl) + "</license>\n");
        }
        out.write("    </copyright>\n");
        if (isUri(licenseUrl)) {
            out.write("    <link href=\"" + escape(licenseUrl) + "\"><text>"
                    + escape(metadata.getOrDefault("licenseName", "License")) + "</text></link>\n");
        }
        out.write("    <time>" + DateTimeFormatter.ISO_INSTANT.format(now) + "</time>\n");
        if (!Double.isNaN(minLat)) {
            out.write("    <bounds minlat=\"" + decimal(minLat) + "\" minlon=\"" + decimal(minLon) + "\" maxlat=\""
                    + decimal(maxLat) + "\" maxlon=\"" + decimal(maxLon) + "\"/>\n");
        }
        out.write("  </metadata>\n");
    }

    private static boolean isUri(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return URI.create(value).isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void append(Writer out, Path part) throws IOException {
        try (Reader in = Files.newBufferedReader(part, StandardCharsets.UTF_8)) {
            in.transferTo(out);
        }
    }

    /** An {@code xsd:decimal}: plain notation, never an exponent. */
    static String decimal(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /** XML-escapes text and attribute values. */
    static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int k = 0; k < value.length(); k++) {
            char c = value.charAt(k);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> {
                    if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') {
                        out.append(' ');
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
