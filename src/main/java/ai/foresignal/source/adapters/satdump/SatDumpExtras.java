package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The DCS, EMWIN and 2.0-manifest extras of a {@code satdump-{station}} source (prompt 107): given a
 * ready pass folder it parses the {@code parse_dcs} JSON files, the EMWIN product files and an optional
 * {@code foresignal-manifest.json}, and returns the records they produce plus health reasons.
 *
 * <p>Everything stays on the far side of the GPL boundary: Foresignal reads SatDump's files only.
 */
@Component
public class SatDumpExtras {

    /** {@code dcsEnabled}. */
    public static final String DCS_ENABLED = "dcsEnabled";
    /** {@code dcsGlob}. */
    public static final String DCS_GLOB = "dcsGlob";
    /** The default DCS glob. */
    public static final String DEFAULT_DCS_GLOB = "**/DCS/**/*.json";
    /** {@code dcsBbox}. */
    public static final String DCS_BBOX = "dcsBbox";
    /** {@code dcsAddresses}. */
    public static final String DCS_ADDRESSES = "dcsAddresses";
    /** {@code dcsAgencies}. */
    public static final String DCS_AGENCIES = "dcsAgencies";
    /** {@code dcsMaxPerScan}. */
    public static final String DCS_MAX_PER_SCAN = "dcsMaxPerScan";
    /** The default {@code dcsMaxPerScan}. */
    public static final int DEFAULT_DCS_MAX_PER_SCAN = 500;
    /** {@code emwinEnabled}. */
    public static final String EMWIN_ENABLED = "emwinEnabled";
    /** {@code emwinGlob}. */
    public static final String EMWIN_GLOB = "emwinGlob";
    /** The default EMWIN glob. */
    public static final String DEFAULT_EMWIN_GLOB = "**/EMWIN/**/*";
    /** {@code emwinProducts}. */
    public static final String EMWIN_PRODUCTS = "emwinProducts";
    /** The default EMWIN products. */
    public static final List<String> DEFAULT_EMWIN_PRODUCTS =
            List.of("TOR", "SVR", "FFW", "SVS", "SMW", "EWW", "SQW", "FFS", "MWS", "SPS");
    /** {@code emwinWarnFallback}. */
    public static final String EMWIN_FALLBACK = "emwinWarnFallback";

    /** The {@code dcp-observation} kind. */
    public static final String DCP_KIND = "dcp-observation";
    /** The {@code emwin-product} kind. */
    public static final String EMWIN_PRODUCT_KIND = "emwin-product";
    /** The {@code emwin-warning} kind. */
    public static final String EMWIN_WARNING_KIND = "emwin-warning";

    private static final int MAX_DCS_FILE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_EMWIN_FILE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_DCS_ASCII = 1024;

    private final DcpLocationStore locations;
    private final CurrentRecords records;
    private final MeterRegistry meters;
    private final AreaOfInterest aoi;

    /** Creates the extras. */
    public SatDumpExtras(DcpLocationStore locations, CurrentRecords records,
            ObjectProvider<MeterRegistry> meters, ObjectProvider<AreaOfInterest> aoi) {
        this.locations = locations;
        this.records = records;
        this.meters = meters == null ? null : meters.getIfAvailable();
        this.aoi = aoi == null ? null : aoi.getIfAvailable();
    }

    /** The records and reasons of one pass folder. */
    public record Result(List<NormalizedRecord> records, Set<String> reasons) {}

    /** Processes the DCS and EMWIN files of {@code folder}. */
    public Result process(InboxScanner.Folder folder, InboxAccess access, ValidatedParams params, Instant now) {
        List<NormalizedRecord> out = new ArrayList<>();
        Set<String> reasons = new LinkedHashSet<>();
        if (params != null && params.bool(DCS_ENABLED, false)) {
            locations.load();
            try {
                dcs(folder, access, params, now, out, reasons);
            } catch (RuntimeException e) {
                reasons.add("DCS processing failed: " + e.getMessage());
            }
        }
        if (params != null && params.bool(EMWIN_ENABLED, false)) {
            try {
                emwin(folder, access, params, now, out, reasons);
            } catch (RuntimeException e) {
                reasons.add("EMWIN processing failed: " + e.getMessage());
            }
        }
        manifest(folder, access, reasons);
        return new Result(out, reasons);
    }

    /**
     * Recognises a SatDump 2.0 {@code foresignal-manifest.json} in the pass folder (prompt 107): a
     * schema-valid manifest is accepted (its GeoTIFFs and PNGs are already ingested by the polar glob),
     * an unknown schema is rejected with a health reason. Version-agnostic.
     */
    private void manifest(InboxScanner.Folder folder, InboxAccess access, Set<String> reasons) {
        for (InboxAccess.FileStat file : folder.files()) {
            if (!file.path().getFileName().toString().equals(ForesignalManifest.FILE)) {
                continue;
            }
            try {
                ForesignalManifest.parse(access.readAll(file.path(), 1024 * 1024));
            } catch (FormatException e) {
                reasons.add("2.0 manifest rejected: " + e.getMessage());
            } catch (java.io.IOException | RuntimeException e) {
                reasons.add("2.0 manifest unreadable: " + e.getMessage());
            }
            return;
        }
    }

    // ------------------------------------------------------------------------- DCS

    private void dcs(InboxScanner.Folder folder, InboxAccess access, ValidatedParams params, Instant now,
            List<NormalizedRecord> out, Set<String> reasons) {
        PolarGlob glob = PolarGlob.compile(opt(params, DCS_GLOB, DEFAULT_DCS_GLOB));
        Envelope bbox = effectiveBbox(params);
        Set<String> allow = addresses(params);
        Set<String> agencies = new LinkedHashSet<>();
        for (String a : list(params, DCS_AGENCIES)) {
            agencies.add(a.toUpperCase(Locale.ROOT));
        }
        int cap = Math.max(1, atLeast(params.integer(DCS_MAX_PER_SCAN, DEFAULT_DCS_MAX_PER_SCAN), 1));
        Map<String, NormalizedRecord> newest = new LinkedHashMap<>();
        long noLocation = 0;
        long outside = 0;
        long notAllowed = 0;
        for (InboxAccess.FileStat file : folder.files()) {
            String relative = relative(folder.path(), file.path());
            if (!glob.matches(relative) || !relative.toLowerCase(Locale.ROOT).endsWith(".json")) {
                continue;
            }
            byte[] bytes;
            try {
                bytes = access.readAll(file.path(), MAX_DCS_FILE_BYTES);
            } catch (java.io.IOException | RuntimeException e) {
                continue;
            }
            List<DcsJsonParser.Message> messages;
            try {
                messages = DcsJsonParser.parse(bytes);
            } catch (FormatException e) {
                continue;
            }
            for (DcsJsonParser.Message message : messages) {
                Optional<DcpLocationStore.DcpLocation> location = locations.find(message.address());
                if (location.isEmpty() || location.get().lat() == null || location.get().lon() == null) {
                    noLocation++;
                    continue;
                }
                DcpLocationStore.DcpLocation loc = location.get();
                if (bbox != null && (loc.lon() < bbox.getMinX() || loc.lon() > bbox.getMaxX() || loc.lat() < bbox.getMinY()
                        || loc.lat() > bbox.getMaxY())) {
                    outside++;
                    continue;
                }
                if (!allow.isEmpty() && !allow.contains(DcpLocationStore.normalize(message.address()))) {
                    notAllowed++;
                    continue;
                }
                if (!agencies.isEmpty() && (loc.agency() == null || !agencies.contains(loc.agency().toUpperCase(Locale.ROOT)))) {
                    notAllowed++;
                    continue;
                }
                NormalizedRecord record = dcsRecord(message, loc);
                NormalizedRecord previous = newest.get(message.address());
                if (previous == null || later(record, previous)) {
                    newest.put(message.address(), record);
                }
            }
        }
        count("no_location", noLocation);
        count("outside_bbox", outside);
        count("not_allowlisted", notAllowed);
        long dropped = 0;
        List<Map.Entry<String, NormalizedRecord>> entries = new ArrayList<>(newest.entrySet());
        entries.sort((a, b) -> b.getValue().observedAt().compareTo(a.getValue().observedAt()));
        int kept = 0;
        for (Map.Entry<String, NormalizedRecord> entry : entries) {
            if (kept < cap) {
                out.add(entry.getValue());
                kept++;
            } else {
                dropped++;
            }
        }
        if (dropped > 0) {
            reasons.add("DCS volume over cap: " + dropped + " dropped; tighten dcsBbox or dcsAddresses");
        } else if (bbox == null) {
            reasons.add("DCS needs a bbox: set dcsBbox or add places");
        }
    }

    private static NormalizedRecord dcsRecord(DcsJsonParser.Message message, DcpLocationStore.DcpLocation location) {
        Instant at = message.time() == null ? Instant.now() : message.time();
        String ascii = message.ascii() == null ? null : message.ascii();
        if (ascii != null && ascii.length() > MAX_DCS_ASCII) {
            ascii = ascii.substring(0, MAX_DCS_ASCII);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(location.address(), DCP_KIND)
                .observedAt(at)
                .eventTime(at)
                .point(location.lon(), location.lat())
                .put("address", location.address())
                .put("name", location.name())
                .put("agency", location.agency())
                .put("channel", message.channel())
                .put("failureCode", message.failureCode())
                .put("dataAscii", ascii);
        if (message.signalDbm() != null) {
            b.put("signalDbm", Math.round(message.signalDbm() * 10) / 10.0);
        }
        if (message.freqOffsetHz() != null) {
            b.put("freqOffsetHz", message.freqOffsetHz());
        }
        if (ascii == null && message.data().length > 0) {
            b.put("dataHex", hex(message.data(), 2048));
        }
        return b.build();
    }

    // ------------------------------------------------------------------------- EMWIN

    private void emwin(InboxScanner.Folder folder, InboxAccess access, ValidatedParams params, Instant now,
            List<NormalizedRecord> out, Set<String> reasons) {
        PolarGlob glob = PolarGlob.compile(opt(params, EMWIN_GLOB, DEFAULT_EMWIN_GLOB));
        List<String> products = list(params, EMWIN_PRODUCTS);
        if (products.isEmpty()) {
            products = DEFAULT_EMWIN_PRODUCTS;
        }
        boolean fallback = params.bool(EMWIN_FALLBACK, true);
        boolean nwsAvailable = fallback && records.available("nws-alerts");
        Envelope bbox = aoiBbox();
        Set<String> nwsVtec = nwsAvailable ? nwsVtec(now) : Set.of();
        for (InboxAccess.FileStat file : folder.files()) {
            String relative = relative(folder.path(), file.path());
            if (!glob.matches(relative)) {
                continue;
            }
            byte[] bytes;
            try {
                bytes = access.readAll(file.path(), MAX_EMWIN_FILE_BYTES);
            } catch (java.io.IOException | RuntimeException e) {
                continue;
            }
            EmwinTextParser.Parsed parsed;
            try {
                parsed = EmwinTextParser.parse(bytes);
            } catch (FormatException e) {
                continue;
            }
            if (parsed.category() == null || !EmwinTextParser.categoryAllowed(parsed.category(), products)) {
                continue;
            }
            String id = externalId(parsed);
            out.add(NormalizedRecord.builder(id, EMWIN_PRODUCT_KIND)
                    .observedAt(now)
                    .put("heading", parsed.wmoHeading())
                    .put("awipsId", parsed.awipsId())
                    .put("office", parsed.office())
                    .put("category", parsed.category())
                    .put("text", parsed.text())
                    .build());
            EmwinTextParser.Warning warning = parsed.warning();
            if (warning != null && intersects(warning.polygon(), bbox)) {
                String wid = warning.externalId();
                if (warning.cancelled()) {
                    out.add(NormalizedRecord.tombstone(wid, EMWIN_WARNING_KIND, now));
                } else {
                    NormalizedRecord.Builder b = NormalizedRecord.builder(wid, EMWIN_WARNING_KIND)
                            .observedAt(now)
                            .eventTime(warning.validFrom())
                            .geometry(warning.polygon())
                            .put("action", warning.action())
                            .put("phenomena", warning.phenomena())
                            .put("significance", warning.significance())
                            .put("office", warning.office())
                            .put("etn", warning.etn())
                            .put("headline", warning.headline())
                            .put("fallback", true)
                            .put("authoritativeSource", "nws-alerts");
                    if (warning.validFrom() != null) {
                        b.validFrom(warning.validFrom());
                    }
                    if (warning.validUntil() != null) {
                        b.validUntil(warning.validUntil());
                    }
                    if (nwsAvailable && nwsVtec.contains(vtecKey(warning))) {
                        b.put("duplicateOf", Map.of("source", "nws-alerts", "externalId", vtecKey(warning)));
                    }
                    out.add(b.build());
                }
            }
        }
    }

    /** The VTEC keys currently in {@code nws-alerts}. */
    private Set<String> nwsVtec(Instant now) {
        Set<String> out = new LinkedHashSet<>();
        for (CurrentRecords.Row row : records.current(List.of("nws-alerts"), List.of("nws-alert"), 5_000)) {
            Object vtec = row.payload().get("vtec");
            if (vtec instanceof List<?> list) {
                for (Object entry : list) {
                    vtecKey(entry.toString()).ifPresent(out::add);
                }
            } else if (vtec instanceof String s) {
                vtecKey(s).ifPresent(out::add);
            }
        }
        return out;
    }

    private static Optional<String> vtecKey(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("/([OTEX])\\.([A-Z]{3})\\.([A-Z]{4})\\.([A-Z]{2}\\.W)\\.(\\d{4})/").matcher(text);
        if (m.find()) {
            return Optional.of(m.group(3) + "." + m.group(4).substring(0, 2) + ".W." + m.group(5));
        }
        return Optional.empty();
    }

    private static String vtecKey(EmwinTextParser.Warning warning) {
        return warning.externalId();
    }

    // ------------------------------------------------------------------------- helpers

    /** The effective bbox: the parameter, else the AOI envelope, else null. */
    Envelope effectiveBbox(ValidatedParams params) {
        if (params != null && params.has(DCS_BBOX)) {
            Envelope box = params.bbox(DCS_BBOX);
            if (box != null) {
                return box;
            }
        }
        if (aoi != null) {
            double[] env = aoi.envelope();
            if (env != null && env.length == 4 && !(env[0] == -106.65 && env[1] == 25.84 && env[2] == -93.51
                    && env[3] == 36.50)) {
                return new Envelope(env[0], env[2], env[1], env[3]);
            }
        }
        return null;
    }

    /** The AOI envelope, or null when unknown. */
    Envelope aoiBbox() {
        if (aoi == null) {
            return null;
        }
        double[] env = aoi.envelope();
        return env == null || env.length != 4 ? null : new Envelope(env[0], env[2], env[1], env[3]);
    }

    private static boolean intersects(Geometry polygon, Envelope bbox) {
        if (polygon == null) {
            return false;
        }
        if (bbox == null) {
            return true;
        }
        return polygon.intersects(Geo.factory().toGeometry(bbox));
    }

    private static Set<String> addresses(ValidatedParams params) {
        Set<String> out = new LinkedHashSet<>();
        for (String a : list(params, DCS_ADDRESSES)) {
            out.add(DcpLocationStore.normalize(a));
        }
        return out;
    }

    private static List<String> list(ValidatedParams params, String name) {
        if (params == null || !params.has(name)) {
            return List.of();
        }
        List<String> list = params.stringList(name);
        return list == null ? List.of() : list.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
    }

    private static String opt(ValidatedParams params, String name, String fallback) {
        if (params == null || !params.has(name)) {
            return fallback;
        }
        String v = params.string(name);
        return v == null || v.isBlank() ? fallback : v;
    }

    private static int atLeast(Integer value, int min) {
        return value == null ? min : Math.max(min, value);
    }

    private static boolean later(NormalizedRecord a, NormalizedRecord b) {
        if (a.observedAt() == null) {
            return false;
        }
        return b.observedAt() == null || a.observedAt().isAfter(b.observedAt());
    }

    private static String externalId(EmwinTextParser.Parsed parsed) {
        return (parsed.wmoHeading() == null ? "EMWIN" : parsed.wmoHeading()) + ":"
                + (parsed.awipsId() == null ? "?" : parsed.awipsId()) + ":" + Integer.toHexString(parsed.text().hashCode());
    }

    private static String relative(java.nio.file.Path base, java.nio.file.Path file) {
        return base.relativize(file).toString().replace('\\', '/');
    }

    private static String hex(byte[] data, int max) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < data.length && i * 2 < max; i++) {
            s.append(String.format(Locale.ROOT, "%02x", data[i]));
        }
        return s.toString();
    }

    private void count(String reason, long n) {
        if (n <= 0) {
            return;
        }
        if (meters != null) {
            meters.counter("foresignal.satdump.dcs.dropped", "reason", reason).increment(n);
        }
    }

    /** The {@code dcp-observation} retention. */
    public static final Duration DCS_RETENTION = Duration.ofDays(3);

    /** True when the DCS JSON bytes look like a DCS file (for tests). */
    static boolean looksLikeDcs(byte[] bytes) {
        return bytes != null && bytes.length > 0 && bytes[0] == '{';
    }

    /** The UTF-8 of {@code text}, for tests. */
    static String utf8(String text) {
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
