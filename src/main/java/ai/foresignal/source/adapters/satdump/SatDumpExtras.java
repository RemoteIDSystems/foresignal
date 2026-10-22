package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The DCS, EMWIN and 2.0-manifest extras of a {@code satdump-{station}} source (prompt 107).
 *
 * <ul>
 *   <li><b>DCS and EMWIN</b> files are processed incrementally on every inbox scan
 *       ({@link #scanInbox}), independent of any pass folder: continuous GOES HRIT reception keeps
 *       writing them, so waiting for a "ready" pass folder would hold warnings back until reception
 *       stopped. Each file is processed once it has settled (its size and modification time unchanged
 *       since the previous scan; {@link ExtrasTracker}). The globs match paths relative to the station's
 *       inbox path; {@code IMAGES} trees are not walked.
 *   <li>EMWIN products of one scan are applied in issue-time order, and a warning is only updated by a
 *       product at least as new as the last one applied, so an older {@code NEW} never resurrects a
 *       canceled warning. A DCS message not newer than the last one emitted for its address is skipped.
 *   <li>DCS needs an effective bbox ({@link #effectiveBbox}); without one nothing is processed and the
 *       health says so.
 *   <li>A remote pass upload (prompt 054) has its DCS and EMWIN files processed with its pass
 *       ({@link #processUpload}); an upload is complete when it appears.
 *   <li>A {@code foresignal-manifest.json} in a pass folder is parsed by {@link #manifest}; the station
 *       commits its products through {@link PolarOverlays#processManifest}.
 * </ul>
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

    /** The health reason while DCS is enabled without an effective bbox. */
    public static final String NO_BBOX_REASON =
            "DCS is enabled but has no bbox: set dcsBbox or add places; no DCS message is processed.";

    /** The most EMWIN files processed per scan; the rest wait for the next scan. */
    public static final int MAX_EMWIN_FILES_PER_SCAN = 2_000;

    /** The most DCS files read per scan; the rest wait for the next scan. */
    public static final int MAX_DCS_FILES_PER_SCAN = 2_000;

    private static final int MAX_DCS_FILE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_EMWIN_FILE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_DCS_ASCII = 1024;

    private static final Pattern NWS_VTEC =
            Pattern.compile("/([OTEX])\\.([A-Z]{3})\\.([A-Z]{4})\\.([A-Z]{2})\\.([A-Z])\\.(\\d{4})\\.");

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

    /** The records and health reasons of one scan or one upload. */
    public record Result(List<NormalizedRecord> records, Set<String> reasons) {}

    /** The enabled parts of one run: null globs are off. */
    private record Plan(PolarGlob dcsGlob, PolarGlob emwinGlob, Envelope bbox, Set<String> reasons) {}

    private Plan plan(ValidatedParams params) {
        Set<String> reasons = new LinkedHashSet<>();
        boolean dcsOn = params != null && params.bool(DCS_ENABLED, false);
        boolean emwinOn = params != null && params.bool(EMWIN_ENABLED, false);
        Envelope bbox = dcsOn ? effectiveBbox(params) : null;
        if (dcsOn && bbox == null) {
            reasons.add(NO_BBOX_REASON);
        }
        PolarGlob dcsGlob = dcsOn && bbox != null ? PolarGlob.compile(opt(params, DCS_GLOB, DEFAULT_DCS_GLOB)) : null;
        PolarGlob emwinGlob = emwinOn ? PolarGlob.compile(opt(params, EMWIN_GLOB, DEFAULT_EMWIN_GLOB)) : null;
        return new Plan(dcsGlob, emwinGlob, bbox, reasons);
    }

    /**
     * Processes the settled DCS and EMWIN files under the station's inbox path (one inbox scan).
     *
     * @param access the inbox access, which re-checks every path
     * @param base the station's inbox path
     * @param params the source parameters
     * @param tracker the station's file and ordering memory
     * @param now the scan time
     */
    public Result scanInbox(InboxAccess access, Path base, ValidatedParams params, ExtrasTracker tracker, Instant now) {
        List<NormalizedRecord> out = new ArrayList<>();
        Plan plan = plan(params);
        Set<String> reasons = plan.reasons();
        if (plan.dcsGlob() == null && plan.emwinGlob() == null) {
            return new Result(out, reasons);
        }
        List<ExtrasTracker.Candidate> candidates;
        try {
            candidates = walk(access, base, plan.dcsGlob(), plan.emwinGlob());
        } catch (IOException | RuntimeException e) {
            reasons.add("DCS/EMWIN files could not be listed: " + e.getMessage());
            return new Result(out, reasons);
        }
        run(plan, tracker.ready(candidates), access, params, tracker, now, out);
        tracker.advance(now);
        tracker.prune(now);
        return new Result(out, reasons);
    }

    /**
     * Processes the DCS and EMWIN files of a remote pass upload (prompt 054). The upload is complete when
     * it appears, so every matching file is read at once; the globs match paths relative to the upload
     * folder, and the ordering memory of {@code tracker} still applies (upload files are never tracked
     * for settling).
     */
    public Result processUpload(InboxScanner.Folder folder, InboxAccess access, ValidatedParams params,
            ExtrasTracker tracker, Instant now) {
        List<NormalizedRecord> out = new ArrayList<>();
        Plan plan = plan(params);
        if (plan.dcsGlob() == null && plan.emwinGlob() == null) {
            return new Result(out, plan.reasons());
        }
        List<ExtrasTracker.Candidate> files = new ArrayList<>();
        for (InboxAccess.FileStat file : folder.files()) {
            files.add(new ExtrasTracker.Candidate(relative(folder.path(), file.path()), file));
        }
        ExtrasTracker ordering = tracker == null ? new ExtrasTracker() : tracker;
        run(plan, files, access, params, ordering, now, out);
        ordering.prune(now);
        return new Result(out, plan.reasons());
    }

    /** Splits {@code files} by glob and processes them. */
    private void run(Plan plan, List<ExtrasTracker.Candidate> files, InboxAccess access, ValidatedParams params,
            ExtrasTracker tracker, Instant now, List<NormalizedRecord> out) {
        List<ExtrasTracker.Candidate> dcsFiles = new ArrayList<>();
        List<ExtrasTracker.Candidate> emwinFiles = new ArrayList<>();
        for (ExtrasTracker.Candidate c : files) {
            if (plan.dcsGlob() != null && plan.dcsGlob().matches(c.key())
                    && c.key().toLowerCase(Locale.ROOT).endsWith(".json")) {
                dcsFiles.add(c);
            } else if (plan.emwinGlob() != null && plan.emwinGlob().matches(c.key())) {
                emwinFiles.add(c);
            }
        }
        if (plan.dcsGlob() != null) {
            locations.load();
            try {
                dcs(limit(dcsFiles, MAX_DCS_FILES_PER_SCAN), access, params, plan.bbox(), tracker, now, out,
                        plan.reasons());
            } catch (RuntimeException e) {
                plan.reasons().add("DCS processing failed: " + e.getMessage());
            }
        }
        if (plan.emwinGlob() != null) {
            try {
                emwin(limit(emwinFiles, MAX_EMWIN_FILES_PER_SCAN), access, params, tracker, now, out);
            } catch (RuntimeException e) {
                plan.reasons().add("EMWIN processing failed: " + e.getMessage());
            }
        }
    }

    /**
     * Parses the {@code foresignal-manifest.json} at the top of a pass folder (prompt 107,
     * version-agnostic), or returns null when there is none or it is rejected; a rejection adds a health
     * reason.
     */
    public ForesignalManifest.Manifest manifest(InboxScanner.Folder folder, InboxAccess access, Set<String> reasons) {
        for (InboxAccess.FileStat file : folder.files()) {
            if (!file.path().getFileName().toString().equals(ForesignalManifest.FILE)
                    || !folder.path().equals(file.path().getParent())) {
                continue;
            }
            try {
                return ForesignalManifest.parse(access.readAll(file.path(), 1024 * 1024));
            } catch (FormatException e) {
                reasons.add("2.0 manifest rejected: " + e.getMessage());
            } catch (IOException | RuntimeException e) {
                reasons.add("2.0 manifest unreadable: " + e.getMessage());
            }
            return null;
        }
        return null;
    }

    // ------------------------------------------------------------------------- the walk

    /**
     * The files below {@code base} matching either glob (relative to {@code base}), skipping
     * {@code IMAGES} trees; bounded like {@link InboxScanner}.
     *
     * @throws IOException when {@code base} itself cannot be listed
     */
    static List<ExtrasTracker.Candidate> walk(InboxAccess access, Path base, PolarGlob dcsGlob, PolarGlob emwinGlob)
            throws IOException {
        Path root = access.requireInside(base);
        List<ExtrasTracker.Candidate> out = new ArrayList<>();
        int[] entries = {0};
        walk(access, root, root, 0, dcsGlob, emwinGlob, out, entries);
        return out;
    }

    private static void walk(InboxAccess access, Path root, Path dir, int depth, PolarGlob dcsGlob, PolarGlob emwinGlob,
            List<ExtrasTracker.Candidate> out, int[] entries) throws IOException {
        if (depth > InboxScanner.MAX_DEPTH || entries[0] > InboxScanner.MAX_ENTRIES) {
            return;
        }
        List<Path> files;
        List<Path> dirs;
        try {
            files = access.listRegularFiles(dir);
            dirs = access.listDirectories(dir);
        } catch (IOException | RuntimeException e) {
            if (dir.equals(root)) {
                throw e instanceof IOException io ? io : new IOException(e.getMessage(), e);
            }
            return;
        }
        entries[0] += files.size() + dirs.size();
        if (entries[0] > InboxScanner.MAX_ENTRIES) {
            return;
        }
        for (Path file : files) {
            String relative = relative(root, file);
            boolean dcs = dcsGlob != null && dcsGlob.matches(relative);
            boolean emwin = emwinGlob != null && emwinGlob.matches(relative);
            if (!dcs && !emwin) {
                continue;
            }
            try {
                out.add(new ExtrasTracker.Candidate(relative, access.stat(file)));
            } catch (IOException | RuntimeException e) {
                // Vanished or replaced by a link between the listing and the stat: left out.
            }
        }
        for (Path child : dirs) {
            if (!child.getFileName().toString().equals(InboxScanner.IMAGES)) {
                walk(access, root, child, depth + 1, dcsGlob, emwinGlob, out, entries);
            }
        }
    }

    // ------------------------------------------------------------------------- DCS

    private void dcs(List<ExtrasTracker.Candidate> files, InboxAccess access, ValidatedParams params, Envelope bbox,
            ExtrasTracker tracker, Instant now, List<NormalizedRecord> out, Set<String> reasons) {
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
        for (ExtrasTracker.Candidate file : files) {
            byte[] bytes;
            try {
                bytes = access.readAll(file.file().path(), MAX_DCS_FILE_BYTES);
            } catch (IOException | RuntimeException e) {
                continue;
            }
            tracker.markDone(file);
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
                if (loc.lon() < bbox.getMinX() || loc.lon() > bbox.getMaxX() || loc.lat() < bbox.getMinY()
                        || loc.lat() > bbox.getMaxY()) {
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
                NormalizedRecord record = dcsRecord(message, loc, now);
                NormalizedRecord previous = newest.get(record.externalId());
                if (previous == null || later(record, previous)) {
                    newest.put(record.externalId(), record);
                }
            }
        }
        count("no_location", noLocation);
        count("outside_bbox", outside);
        count("not_allowlisted", notAllowed);
        List<NormalizedRecord> fresh = new ArrayList<>();
        for (NormalizedRecord record : newest.values()) {
            // A message not newer than the last one emitted for its address is not emitted again.
            Long last = tracker.dcsTime(record.externalId());
            if (last == null || record.observedAt().toEpochMilli() > last) {
                fresh.add(record);
            }
        }
        fresh.sort((a, b) -> b.observedAt().compareTo(a.observedAt()));
        long dropped = 0;
        int kept = 0;
        for (NormalizedRecord record : fresh) {
            if (kept < cap) {
                out.add(record);
                tracker.acceptDcs(record.externalId(), record.observedAt().toEpochMilli());
                kept++;
            } else {
                dropped++;
            }
        }
        if (dropped > 0) {
            reasons.add("DCS volume over cap: " + dropped + " dropped; tighten dcsBbox or dcsAddresses");
        }
    }

    private static NormalizedRecord dcsRecord(DcsJsonParser.Message message, DcpLocationStore.DcpLocation location,
            Instant now) {
        Instant at = message.time() == null ? now : message.time();
        String ascii = message.ascii();
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

    /** One parsed EMWIN file with its issue time ({@code issueKnown} false when it is the file time). */
    private record Product(EmwinTextParser.Parsed parsed, Instant issuedAt, boolean issueKnown, String key) {}

    private void emwin(List<ExtrasTracker.Candidate> files, InboxAccess access, ValidatedParams params,
            ExtrasTracker tracker, Instant now, List<NormalizedRecord> out) {
        List<String> products = list(params, EMWIN_PRODUCTS);
        if (products.isEmpty()) {
            products = DEFAULT_EMWIN_PRODUCTS;
        }
        boolean fallback = params.bool(EMWIN_FALLBACK, true);
        boolean nwsAvailable = fallback && records.available("nws-alerts");
        Envelope bbox = aoiBbox();
        Set<String> nwsVtec = nwsAvailable ? nwsVtec() : Set.of();
        List<Product> parsedFiles = new ArrayList<>();
        for (ExtrasTracker.Candidate file : files) {
            byte[] bytes;
            try {
                bytes = access.readAll(file.file().path(), MAX_EMWIN_FILE_BYTES);
            } catch (IOException | RuntimeException e) {
                continue;
            }
            tracker.markDone(file);
            EmwinTextParser.Parsed parsed;
            try {
                parsed = EmwinTextParser.parse(bytes);
            } catch (FormatException e) {
                continue;
            }
            if (parsed.category() == null || !EmwinTextParser.categoryAllowed(parsed.category(), products)) {
                continue;
            }
            Instant issued = EmwinTextParser.issuedAt(parsed.wmoHeading(), now);
            boolean known = issued != null;
            parsedFiles.add(new Product(parsed, known ? issued : file.file().modified(), known, file.key()));
        }
        // Apply in issue order, so the latest product of a warning is the one that stands.
        parsedFiles.sort(Comparator.comparing(Product::issuedAt).thenComparing(Product::key));
        for (Product product : parsedFiles) {
            emitProduct(product, bbox, nwsAvailable, nwsVtec, tracker, out);
        }
    }

    private static void emitProduct(Product product, Envelope bbox, boolean nwsAvailable, Set<String> nwsVtec,
            ExtrasTracker tracker, List<NormalizedRecord> out) {
        EmwinTextParser.Parsed parsed = product.parsed();
        Instant issued = product.issuedAt();
        // The heading's issue time, else the file's modification time (the spec requires issuedAt).
        String issuedText = issued.toString();
        out.add(NormalizedRecord.builder(productId(parsed, product.issueKnown() ? issued : null), EMWIN_PRODUCT_KIND)
                .observedAt(issued)
                .eventTime(issued)
                .put("heading", parsed.wmoHeading())
                .put("awipsId", parsed.awipsId())
                .put("office", parsed.office())
                .put("category", parsed.category())
                .put("issuedAt", issuedText)
                .put("text", parsed.text())
                .build());
        EmwinTextParser.Warning warning = parsed.warning();
        if (warning == null || !intersects(warning.polygon(), bbox)) {
            return;
        }
        String wid = warning.externalId();
        if (!tracker.acceptWarning(wid, issued.toEpochMilli())) {
            // A newer product of this warning was already applied: an older one never overrides it.
            return;
        }
        if (warning.canceled()) {
            out.add(NormalizedRecord.tombstone(wid, EMWIN_WARNING_KIND, issued));
            return;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(wid, EMWIN_WARNING_KIND)
                .observedAt(issued)
                .eventTime(warning.validFrom() != null ? warning.validFrom() : issued)
                .geometry(warning.polygon())
                .put("action", warning.action())
                .put("phenomena", warning.phenomena())
                .put("significance", warning.significance())
                .put("office", warning.office())
                .put("etn", warning.etnNumber())
                .put("headline", warning.headline())
                .put("issuedAt", issuedText)
                .put("fallback", true)
                .put("authoritativeSource", "nws-alerts");
        if (warning.validFrom() != null) {
            b.validFrom(warning.validFrom());
        }
        if (warning.validUntil() != null) {
            b.validUntil(warning.validUntil());
        }
        if (nwsAvailable && nwsVtec.contains(wid)) {
            b.put("duplicateOf", Map.of("source", "nws-alerts", "externalId", wid));
        }
        out.add(b.build());
    }

    /** The VTEC keys currently in {@code nws-alerts}. */
    private Set<String> nwsVtec() {
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

    /**
     * The {@code office.phen.sig.etn} key of a raw P-VTEC string, the same shape as
     * {@link EmwinTextParser.Warning#externalId()}, for any significance.
     */
    static Optional<String> vtecKey(String text) {
        Matcher m = NWS_VTEC.matcher(text);
        if (m.find()) {
            return Optional.of(m.group(3) + "." + m.group(4) + "." + m.group(5) + "." + m.group(6));
        }
        return Optional.empty();
    }

    /**
     * The {@code emwin-product} externalId {@code {wmoHeading}:{awipsId}:{issue}} (prompt 107), with the
     * issue time as ISO-8601; when the heading has no usable issue time, a SHA-256 prefix of the text
     * stands in, so the id is stable across reprocessing either way.
     */
    static String productId(EmwinTextParser.Parsed parsed, Instant issuedAt) {
        String issue = issuedAt != null ? issuedAt.toString() : sha256(parsed.text());
        return (parsed.wmoHeading() == null ? "EMWIN" : parsed.wmoHeading()) + ":"
                + (parsed.awipsId() == null ? "?" : parsed.awipsId()) + ":" + issue;
    }

    private static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static <T> List<T> limit(List<T> list, int max) {
        return list.size() <= max ? list : list.subList(0, max);
    }

    // ------------------------------------------------------------------------- helpers

    /**
     * The effective DCS bbox (prompt 107: mandatory): the {@code dcsBbox} parameter, else the places-AOI
     * envelope while there are places, else null. The {@code weather.defaultAoi} box in force without
     * places is not an operator choice, so it does not count; DCS is then not processed at all.
     */
    Envelope effectiveBbox(ValidatedParams params) {
        if (params != null && params.has(DCS_BBOX)) {
            Envelope box = params.bbox(DCS_BBOX);
            if (box != null) {
                return box;
            }
        }
        if (aoi != null && !aoi.isDefault()) {
            double[] env = aoi.envelope();
            if (env != null && env.length == 4) {
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

    private static String relative(Path base, Path file) {
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
