package ai.foresignal.source.adapters.spc;

import ai.foresignal.source.spi.AdapterException;
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
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SPC convective outlooks ({@code spc-outlooks}, prompt 047; moved here from prompt 089).
 *
 * <p>Seventeen layer files under {@code https://www.spc.noaa.gov/products/outlook/}, each fetched
 * with a conditional GET (ETag and Last-Modified verified 2026-10-12): day 1 and day 2
 * {@code cat, torn, wind, hail, sigtorn, sigwind, sighail}, and day 3 {@code cat, prob, sigprob}
 * (day 3 has no torn/wind/hail layers; those URLs answer 404, verified 2026-10).
 *
 * <p><strong>Per-file completeness.</strong> A layer fetched with 200 and parsed is the complete set
 * for that day and type: labels live in the previous successful copy of that file and absent now are
 * tombstoned for that day and type only ({@link LiveIdSet}, one scope per file). A 304 changes nothing.
 * A 404 marks the source degraded with "SPC layer {file} missing" and tombstones nothing; any other
 * failure of one layer is reported the same way, and the run fails only when every layer failed. A
 * body that was truncated or could not be parsed forgets the file's validators, so the next run asks
 * again instead of receiving a 304.
 */
@Component
public class SpcOutlooksAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "spc-outlooks";

    /** The record kind. */
    public static final String KIND = SpcOutlookParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://www.spc.noaa.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.spc.base-url";

    /** The outlook directory. */
    static final String PATH = "/products/outlook/";

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code spc-outlook} for 14 days. */
    static final Duration RETENTION = Duration.ofDays(14);

    /**
     * One layer file.
     *
     * @param day 1, 2 or 3
     * @param type the layer type
     */
    public record Layer(int day, String type) {

        /** The file name, for example {@code day1otlk_cat.lyr.geojson}. */
        public String file() {
            return "day" + day + "otlk_" + type + ".lyr.geojson";
        }

        /** The adapter-state key of the file's validators and live set. */
        String stateKey() {
            return "spc:day" + day + ":" + type;
        }
    }

    /** The 17 layers fetched every run. */
    public static final List<Layer> LAYERS = layers();

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real site
     */
    public SpcOutlooksAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The URL of one layer. */
    public URI layerUri(Layer layer) {
        return URI.create(baseUrl + PATH + layer.file());
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        List<NormalizedRecord> out = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        Exception lastFailure = null;
        int failures = 0;
        int changed = 0;
        for (Layer layer : LAYERS) {
            UpstreamResponse response;
            try {
                response = ctx.http().getConditional(layerUri(layer), layer.stateKey());
            } catch (AdapterException e) {
                failures++;
                lastFailure = e;
                if (e.httpStatus().orElse(0) == 404) {
                    problems.add("SPC layer " + layer.file() + " missing");
                } else {
                    problems.add("SPC layer " + layer.file() + " failed: " + e.getMessage());
                }
                continue;
            }
            if (response.notModified()) {
                continue;
            }
            SpcOutlookParser.ParseResult parsed;
            try {
                if (response.truncated()) {
                    throw new UpstreamFormatException("SPC layer " + layer.file() + " was truncated", null);
                }
                parsed = SpcOutlookParser.parse(response.body(), layer.day(), layer.type(), now);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), layer.stateKey());
                failures++;
                lastFailure = e;
                problems.add("SPC layer " + layer.file() + " unreadable: " + e.getMessage());
                continue;
            }
            changed++;
            out.addAll(parsed.records());
            Instant issued = parsed.records().isEmpty() ? now : parsed.records().get(0).observedAt();
            for (String id : LiveIdSet.absentSince(ctx.state(), layer.stateKey(), parsed.ids())) {
                out.add(NormalizedRecord.tombstone(id, KIND, issued));
            }
        }
        if (failures == LAYERS.size()) {
            if (lastFailure instanceof AdapterException adapterFailure) {
                throw adapterFailure;
            }
            throw (RuntimeException) lastFailure;
        }
        if (problems.isEmpty()) {
            ctx.state().clearDegraded();
        } else {
            ctx.state().degrade(String.join("; ", problems));
            ctx.log().warn("spc-outlooks: {}", problems);
        }
        if (changed == 0 && out.isEmpty()) {
            return FetchResult.unchanged();
        }
        return FetchResult.complete(out);
    }

    private static List<Layer> layers() {
        List<Layer> out = new ArrayList<>();
        for (int day = 1; day <= 2; day++) {
            for (String type : List.of("cat", "torn", "wind", "hail", "sigtorn", "sigwind", "sighail")) {
                out.add(new Layer(day, type));
            }
        }
        for (String type : List.of("cat", "prob", "sigprob")) {
            out.add(new Layer(3, type));
        }
        return List.copyOf(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("SPC Convective Outlooks")
                .description("Storm Prediction Center day 1-3 convective outlooks: categorical risk (TSTM to HIGH)"
                        + " and tornado, wind, hail and significant-severe probability areas, from 17 layer files"
                        + " fetched with conditional GETs.")
                .kind(KIND)
                .homepage("https://www.spc.noaa.gov/gis/")
                .attribution("NOAA/NWS Storm Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(20, 2_000)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("title")
                .zeroResultsNormal(true)
                .searchFields("title", "label2")
                .tileProperties("day", "type", "label", "dn", "title")
                .rankField("dn")
                .kindDisplay(KIND, new KindDisplay("{title}", "outlook", "#F59F00", null));
        builder.fieldSpec("day", new FieldSpec(FieldType.INTEGER, "d", "Day", "Outlook day: 1, 2 or 3", null, Set.of()));
        string(builder, "type", "Layer", "cat, torn, wind, hail, sigtorn, sigwind, sighail, prob or sigprob");
        string(builder, "label", "Label", "SPC LABEL: MRGL/SLGT/ENH/MDT/HIGH/TSTM, a probability such as 0.10, or SIGN");
        string(builder, "label2", "Description", "SPC LABEL2, for example Moderate Risk or 10% Tornado Risk");
        builder.fieldSpec("dn", new FieldSpec(FieldType.INTEGER, "1", "Rank",
                "SPC numeric rank (categorical: TSTM 2, MRGL 3, SLGT 4, ENH 5, MDT 6, HIGH 8; probabilistic: the percent)",
                null, Set.of()));
        timestamp(builder, "issue", "Issued", "ISSUE (observedAt)");
        timestamp(builder, "valid", "Valid from", "VALID (eventTime)");
        timestamp(builder, "expire", "Expires", "EXPIRE (validUntil)");
        string(builder, "title", "Title", "Day 1 Moderate Risk");
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }
}
