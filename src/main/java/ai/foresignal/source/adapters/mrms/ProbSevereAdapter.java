package ai.foresignal.source.adapters.mrms;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MRMS ProbSevere v3 storm objects inside the places AOI ({@code mrms-probsevere}, prompt 047).
 *
 * <p>Each run lists {@code noaa-mrms-pds/ProbSevere/{yyyyMMdd}/} with {@code start-after} the last
 * processed key and downloads <strong>only the newest</strong> file (verified 2026-10-12: about
 * 0.26 MB, one every 2 minutes, 719 on a full day). The files are snapshots, so skipping intermediate
 * ones loses nothing. A list and a download every 2 minutes is 1,440 requests a day, which the
 * design's 800/day could not cover; the budget is 4/min and 1,600/day.
 *
 * <p>Objects are kept when their polygon's bounding box intersects the AOI envelope. The AOI version
 * is stored with the last processed key; when the AOI moved ({@code PlacesAoiChangedEvent}) and no
 * newer file exists, the last file is processed again with the new envelope, so the filter refreshes
 * without waiting for the next file.
 *
 * <p><strong>Tombstones only from the newest successfully processed file:</strong> ids live in the
 * previous processed file (within the AOI) and absent from this one are tombstoned at the file's valid
 * time, which also retires an object that left the AOI. A failed listing or download, a truncated body,
 * an unparseable file, or a file whose valid time is older than the last processed one throws or
 * returns unchanged before anything is tombstoned.
 */
@Component
public class ProbSevereAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "mrms-probsevere";

    /** The record kind. */
    public static final String KIND = ProbSevereParser.KIND;

    /** The bucket. */
    public static final String BUCKET = MrmsAdapter.BUCKET;

    /** Key prefix before the date. */
    public static final String PREFIX = "ProbSevere/";

    /** Test-only override of the object download base (path style). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.mrms-probsevere.base-url";

    /** Adapter-state key of the last processed object key. */
    static final String LAST_KEY = "lastKey";

    /** Adapter-state key of the last processed file's valid time. */
    static final String LAST_VALID = "lastValidTime";

    /** Adapter-state key of the AOI version the last file was filtered with. */
    static final String AOI_VERSION = "aoiVersion";

    /** The {@link LiveIdSet} scope. */
    static final String SCOPE = "aoi";

    /** The longest listing followed in one run. */
    static final int MAX_PAGES = 3;

    /** A file larger than this is refused (the files are about 0.26 MB). */
    static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    /** The AOI used when no AOI service is wired (unit tests): the Texas box of {@code weather.defaultAoi}. */
    static final double[] DEFAULT_ENVELOPE = {-106.65, 25.84, -93.51, 36.50};

    static final Pattern KEY = Pattern.compile("^ProbSevere/\\d{8}/MRMS_PROBSEVERE_\\d{8}_\\d{6}\\.json$");

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code probsevere-object} for 3 days. */
    static final Duration RETENTION = Duration.ofDays(3);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final AreaOfInterest aoi;
    private final String baseUrl;

    /**
     * Creates the adapter for Spring.
     *
     * @param aoi the places AOI
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public ProbSevereAdapter(ObjectProvider<AreaOfInterest> aoi, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(aoi == null ? null : aoi.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param aoi the places AOI, or null for the default Texas box
     * @param baseUrlOverride a path-style base URL, or blank for the real bucket
     */
    public ProbSevereAdapter(AreaOfInterest aoi, String baseUrlOverride) {
        this.aoi = aoi;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        if (aoi != null) {
            aoi.registerConsumer(ID);
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The download URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    /** The day prefix of {@code day}. */
    static String prefix(LocalDate day) {
        return PREFIX + DAY.format(day) + "/";
    }

    private double[] envelope() {
        return aoi == null ? DEFAULT_ENVELOPE.clone() : aoi.envelope();
    }

    private String aoiVersion() {
        return aoi == null ? "default" : Long.toString(aoi.version());
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String lastKey = ctx.state().get(LAST_KEY).orElse(null);
        String version = aoiVersion();
        boolean aoiMoved = lastKey != null && !version.equals(ctx.state().get(AOI_VERSION).orElse(null));

        String newest = newestKey(ctx, lastKey);
        if (newest == null) {
            if (!aoiMoved) {
                return FetchResult.unchanged();
            }
            newest = lastKey;
        }
        UpstreamResponse response = ctx.http().get(objectUri(newest));
        if (response.truncated()) {
            throw new UpstreamFormatException("ProbSevere " + newest + " was truncated; nothing is tombstoned", null);
        }
        ProbSevereParser.ParseResult parsed = ProbSevereParser.parse(response.body(), envelope());
        Instant lastValid = ctx.state().get(LAST_VALID).map(Instant::parse).orElse(null);
        if (lastValid != null && parsed.validTime().isBefore(lastValid)) {
            ctx.log().warn("ProbSevere {} is valid at {}, older than the last processed file ({}); ignored", newest,
                    parsed.validTime(), lastValid);
            return FetchResult.unchanged();
        }
        ctx.log().debug("ProbSevere {}: {}", newest, parsed.diagnostics());

        // A complete, newer file was parsed: only now may absent objects be tombstoned.
        List<NormalizedRecord> out = new ArrayList<>(parsed.records());
        for (String id : LiveIdSet.absentSince(ctx.state(), SCOPE, parsed.ids())) {
            out.add(NormalizedRecord.tombstone(id, KIND, parsed.validTime()));
        }
        ctx.state().put(LAST_KEY, newest);
        ctx.state().put(LAST_VALID, parsed.validTime().toString());
        ctx.state().put(AOI_VERSION, version);
        return FetchResult.complete(out);
    }

    /**
     * The newest ProbSevere key after {@code lastKey}: today's prefix, and yesterday's too until a key
     * of today has been processed (the first files after UTC midnight).
     */
    String newestKey(FetchContext ctx, String lastKey) throws AdapterException {
        LocalDate today = LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC);
        String newest = newestUnder(ctx, prefix(today), lastKey);
        if (newest == null && (lastKey == null || lastKey.compareTo(prefix(today)) < 0)) {
            newest = newestUnder(ctx, prefix(today.minusDays(1)), lastKey);
        }
        return newest;
    }

    private static String newestUnder(FetchContext ctx, String prefix, String lastKey) throws AdapterException {
        // A key of an earlier day sorts before the prefix and lists the whole day.
        String startAfter = lastKey != null && lastKey.compareTo(prefix) > 0 ? lastKey : null;
        String newest = null;
        for (int page = 0; page < MAX_PAGES; page++) {
            S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
            for (S3Object object : listing.objects()) {
                if (KEY.matcher(object.key()).matches() && (newest == null || object.key().compareTo(newest) > 0)) {
                    newest = object.key();
                }
            }
            if (!listing.truncated() || listing.nextStartAfter() == null) {
                break;
            }
            startAfter = listing.nextStartAfter();
        }
        if (newest != null && lastKey != null && newest.compareTo(lastKey) <= 0) {
            return null;
        }
        return newest;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("MRMS ProbSevere")
                .description("NOAA MRMS ProbSevere v3 storm objects inside the places area of interest: automated"
                        + " probabilities of severe weather, tornado, hail and wind every 2 minutes. Guidance, not"
                        + " a warning; NWS alerts stay authoritative.")
                .kind(KIND)
                .homepage("https://registry.opendata.aws/noaa-mrms-pds/")
                .attribution("NOAA NSSL / NCEP, MRMS ProbSevere (University of Wisconsin CIMSS)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-mrms-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(4, 1_600, 300))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("label")
                .zeroResultsNormal(true)
                .volatileFields("muCape", "mlCape", "ebShearKt", "srh01km", "flashRate")
                .tileProperties("probSevere", "probTor", "probHail", "probWind", "meshIn", "label")
                .rankField("probSevere")
                .kindDisplay(KIND, new KindDisplay("{label}", "storm", "#E8590C", null));

        integer(builder, "probSevere", "%", "ProbSevere", "Probability of any severe weather in the next 60 minutes");
        integer(builder, "probTor", "%", "ProbTor", "Probability of a tornado");
        integer(builder, "probHail", "%", "ProbHail", "Probability of severe hail");
        integer(builder, "probWind", "%", "ProbWind", "Probability of severe wind");
        number(builder, "meshIn", "[in_i]", "MESH", "Maximum estimated hail size, to 0.25 in");
        integer(builder, "compRefDbz", "dB", "Composite reflectivity", "Maximum composite reflectivity, dBZ to 1 dBZ");
        number(builder, "vil", "kg/m2", "VIL", "Vertically integrated liquid");
        number(builder, "echoTop50Kft", "10*3.[ft_i]", "50 dBZ echo top", "Height of the 50 dBZ echo top, thousands of feet");
        number(builder, "flashRate", "/min", "Flash rate", "Total lightning flash rate (volatile)");
        number(builder, "muCape", "J/kg", "MUCAPE", "Most-unstable CAPE (volatile)");
        number(builder, "mlCape", "J/kg", "MLCAPE", "Mixed-layer CAPE (volatile)");
        number(builder, "ebShearKt", "[kn_i]", "Effective bulk shear", "Effective bulk shear (volatile)");
        number(builder, "srh01km", "m2/s2", "SRH 0-1 km", "Storm-relative helicity, 0-1 km (volatile)");
        builder.fieldSpec("motion", new FieldSpec(FieldType.OBJECT, null, "Motion",
                "speedKmh (to 5 km/h) and bearingDeg (toward, to 10°), from MOTION_EAST and MOTION_SOUTH", null,
                Set.of()));
        builder.fieldSpec("label", new FieldSpec(FieldType.STRING, null, "Label", "ProbSevere 44% (tor 26%)", null, Set.of()));
        builder.fieldSpec("automated", new FieldSpec(FieldType.BOOLEAN, null, "Automated", "Always true", null, Set.of()));
        builder.fieldSpec("notAWarning", new FieldSpec(FieldType.BOOLEAN, null, "Not a warning",
                "Always true: automated guidance; NWS alerts are authoritative", null, Set.of()));
        return builder.build();
    }

    private static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
