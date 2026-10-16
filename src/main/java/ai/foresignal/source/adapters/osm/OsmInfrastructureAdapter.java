package ai.foresignal.source.adapters.osm;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code osm-infrastructure} (prompt 104): an operator-supplied OpenStreetMap extract, processed
 * locally to an RFC 8142 GeoJSON text sequence under the inbox. It makes <strong>no network
 * requests</strong>: it never calls the Overpass API or any {@code openstreetmap.org} service, and
 * bulk use is the operator's own Geofabrik extract (ODbL 1.0). Each scan imports only when the file's
 * sha256 changed; absent features in a complete new file are tombstoned by the writer's complete-set
 * reconciliation. The file is streamed twice (hash, then parse) and never held whole in memory; a file
 * over {@value #MAX_FEATURES} features is refused with a health reason, and lines that are not valid
 * GeoJSON features are counted ({@code parse errors: n}).
 */
@Component
public class OsmInfrastructureAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "osm-infrastructure";

    /** The inbox path parameter. */
    public static final String INBOX_PATH = "inboxPath";

    /** The minimum voltage parameter. */
    public static final String MIN_VOLTAGE = "minVoltageKv";

    /** The most features one file may bring. */
    public static final int MAX_FEATURES = 2_000_000;

    private static final String SHA_STATE = "osm.sha";
    private static final String FILE_STATE = "osm.file";

    /** The state key of the imported file's modification time (read by the graph build's caveat). */
    public static final String MTIME_STATE = "osm.mtime";

    /** The state key of the last import's parse-error count. */
    public static final String PARSE_ERRORS_STATE = "osm.parseErrors";

    private final SourceDescriptor descriptor = buildDescriptor();

    /** The descriptor. */
    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("OSM infrastructure extract")
                .description("Power, telecom, transport, health and water facilities from an operator-supplied"
                        + " OpenStreetMap extract, processed locally. ODbL 1.0; no network access.")
                .kind("substation").kind("power-line").kind("power-plant-osm").kind("hospital")
                .kind("data-centre-osm").kind("telecom-exchange").kind("aerodrome-osm").kind("port-osm")
                .kind("water-facility")
                .licence("ODbL-1.0")
                .attribution("© OpenStreetMap contributors")
                .termsUrl("https://www.openstreetmap.org/copyright")
                .mode(Mode.POLLING)
                .interval(Duration.ofDays(1), Duration.ofDays(7))
                .defaultBudget(2, 20)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(365))
                .zeroResultsNormal(false)
                .titleField("name")
                .volatileFields("osmVersion")
                .significantFields("name", "operator", "voltageKv", "capacityMw", "beds", "emergency", "iata", "icao")
                .param(ParamSpec.optional(INBOX_PATH, ParamType.INBOX_PATH,
                        "The GeoJSONSeq file or directory under the inbox", "osm"))
                .param(ParamSpec.optional(MIN_VOLTAGE, ParamType.NUMBER,
                        "Power lines below this voltage (kV) are dropped", 110))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        double minVoltage = ctx.params().number(MIN_VOLTAGE) == null ? 110 : ctx.params().number(MIN_VOLTAGE);
        Path requested = ctx.params().inboxPath(INBOX_PATH);
        try {
            Path file = newestFile(ctx, requested);
            if (file == null) {
                ctx.state().degrade("no file in inbox");
                return FetchResult.unchanged();
            }
            String sha = sha256(ctx, file);
            if (sha != null && sha.equals(ctx.state().get(SHA_STATE).orElse(null))) {
                ctx.state().degrade("file unchanged since " + ctx.state().get(MTIME_STATE).orElse(
                        file.getFileName().toString()));
                return FetchResult.unchanged();
            }
            OsmInfraParser.Parsed parsed;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ctx.inbox().open(file), StandardCharsets.UTF_8))) {
                parsed = OsmInfraParser.parse(reader, minVoltage, ctx.now(), MAX_FEATURES);
            }
            if (parsed.capExceeded()) {
                ctx.state().degrade("feature cap exceeded: more than " + MAX_FEATURES + " features in "
                        + file.getFileName());
                return FetchResult.unchanged();
            }
            ctx.state().put(SHA_STATE, sha == null ? "" : sha);
            ctx.state().put(FILE_STATE, file.getFileName().toString());
            ctx.state().put(MTIME_STATE, java.time.Instant.ofEpochMilli(lastModified(file)).toString());
            ctx.state().put(PARSE_ERRORS_STATE, String.valueOf(parsed.parseErrors()));
            if (parsed.parseErrors() > 0) {
                ctx.state().degrade("parse errors: " + parsed.parseErrors());
            } else {
                ctx.state().clearDegraded();
            }
            return FetchResult.complete(parsed.records());
        } catch (AdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new AdapterException("osm-infrastructure read failed: " + e.getMessage(), e);
        }
    }

    private Path newestFile(FetchContext ctx, Path requested) throws java.io.IOException {
        if (java.nio.file.Files.isRegularFile(requested)) {
            return requested;
        }
        List<Path> files = new ArrayList<>();
        for (Path candidate : ctx.inbox().listRegularFiles(requested)) {
            String name = candidate.getFileName().toString();
            if (name.endsWith(".geojsonseq") || name.endsWith(".geojsonl")) {
                files.add(candidate);
            }
        }
        return files.stream().max(java.util.Comparator.comparingLong(this::lastModified)).orElse(null);
    }

    private long lastModified(Path path) {
        try {
            return java.nio.file.Files.getLastModifiedTime(path).toMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** The SHA-256 of the file, streamed (the file is never held in memory). */
    private static String sha256(FetchContext ctx, Path file) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        try (java.io.InputStream in = ctx.inbox().open(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                sha.update(buffer, 0, read);
            }
        }
        byte[] digest = sha.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /** The inbox file last imported, if any. */
    public Optional<String> lastFile(FetchContext ctx) {
        return ctx.state().get(FILE_STATE);
    }
}
