package ai.foresignal.source.adapters.nhc;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Active tropical cyclones from the US National Hurricane Center's {@code CurrentStorms.json} (prompt 067).
 *
 * <p>One conditional GET per run. Verified 2026-10: CloudFront answers with {@code ETag},
 * {@code Last-Modified} and {@code cache-control: max-age=300}, and a 304 becomes
 * {@link FetchResult#unchanged()}, which changes nothing.
 *
 * <p><strong>Tombstones from the complete set.</strong> The file is NHC's complete set of active storms, so a
 * storm missing from a later successful response is no longer advised on and gets an explicit tombstone
 * ({@link LiveIdSet}, scope {@value #ACTIVE_SCOPE}: the ids of the last successful response live in the adapter
 * state under {@code live:active}). This is the documented exception to "absence never closes an alert": the
 * other alert sources' feeds are not complete sets. A failed request, a truncated body or a document the
 * parser rejects as a whole throws before any comparison, so an outage never reads as "every storm
 * dissipated". Out of season the list is empty, which is normal ({@code zeroResultsNormal}).
 *
 * <p><strong>Coverage.</strong> The file repackages the Atlantic and eastern North Pacific feeds;
 * {@code binNumber} is defined only for AT and EP. Central Pacific ({@code cp}) ids are accepted when they
 * appear, but CPHC storms may be missing: the CPHC RSS ({@code https://www.nhc.noaa.gov/index-cp.xml}) is not
 * ingested here. Cones, tracks and forecast points (the GIS links) belong to prompt 075.
 */
@Component
public class NhcCyclonesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nhc-cyclones";

    /** The record kind. */
    public static final String KIND = NhcCurrentStormsParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://www.nhc.noaa.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nhc.base-url";

    /** The file's path. */
    public static final String PATH = "/CurrentStorms.json";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "storms";

    /** The {@link LiveIdSet} scope holding the ids of the last successful response. */
    public static final String ACTIVE_SCOPE = "active";

    /** Raw retention: cyclones are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public NhcCyclonesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The URL of {@code CurrentStorms.json}. */
    public URI stormsUri() {
        return URI.create(baseUrl + PATH);
    }

    /**
     * Fetches the file, maps every storm and tombstones the storms that left the complete set.
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the storms and the tombstones, complete
     * @throws AdapterException when the request fails; {@link UpstreamFormatException} when the body is
     *     truncated or rejected, in which case the validators are forgotten so the next run refetches
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(stormsUri(), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> storms;
        try {
            if (response.truncated()) {
                throw new UpstreamFormatException("NHC CurrentStorms.json exceeded maxResponseBytes; nothing is"
                        + " tombstoned", null);
            }
            storms = NhcCurrentStormsParser.parse(response.body());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        Set<String> live = new LinkedHashSet<>();
        storms.forEach(r -> live.add(r.externalId()));
        List<NormalizedRecord> out = new ArrayList<>(storms);
        for (String id : LiveIdSet.absentSince(ctx.state(), ACTIVE_SCOPE, live)) {
            out.add(NormalizedRecord.tombstone(id, KIND, ctx.now()));
        }
        ctx.log().debug("NHC: {} active storm(s), {} tombstone(s)", storms.size(), out.size() - storms.size());
        return FetchResult.complete(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("NHC Tropical Cyclones")
                .description("Active tropical cyclones advised on by the US National Hurricane Center"
                        + " (Atlantic and eastern North Pacific; Central Pacific storms may be missing): position,"
                        + " classification, intensity, pressure, motion and the latest advisory. A storm that"
                        + " leaves NHC's complete active set is tombstoned; a failed fetch tombstones nothing.")
                .kind(KIND)
                .homepage("https://www.nhc.noaa.gov/")
                .attribution("NOAA National Hurricane Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(10))
                .defaultBudget(2, 300)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("name")
                .searchFields("name", "classificationLabel")
                .tileProperties("name", "classification", "intensityKt", "pressureMb", "basin")
                .rankField("intensityKt")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{classificationLabel} {name}", "storm", "#7048E8", null));

        string(builder, "name", "Name", "Storm name");
        string(builder, "classification", "Classification",
                "NHC abbreviation: TD, STD, TS, HU, STS, PTC, TY or PC");
        string(builder, "classificationLabel", "Classification label",
                "The classification in words, for example Hurricane");
        string(builder, "basin", "Basin", "AL (Atlantic), EP (eastern North Pacific) or CP (Central Pacific)");
        string(builder, "binNumber", "Bin", "NHC product bin, AT1-AT5 or EP1-EP5");
        number(builder, "intensityKt", FieldType.INTEGER, "[kn_i]", "Intensity", "Maximum sustained wind");
        number(builder, "pressureMb", FieldType.INTEGER, "mbar", "Pressure", "Minimum central pressure");
        number(builder, "movementDirDeg", FieldType.INTEGER, "deg", "Movement direction",
                "Direction of motion, degrees from north");
        number(builder, "movementSpeedKt", FieldType.NUMBER, "[kn_i]", "Movement speed",
                "Speed of motion, converted from NHC's miles per hour, 1 decimal");
        string(builder, "advisoryNumber", "Advisory", "Latest forecast advisory number, without leading zeros");
        builder.fieldSpec("advisoryIssued", new FieldSpec(FieldType.TIMESTAMP, null, "Advisory issued",
                "Issuance time of the latest forecast advisory", null, Set.of()));
        string(builder, "publicAdvisoryUrl", "Public advisory", "Latest public advisory on www.nhc.noaa.gov");
        string(builder, "forecastAdvisoryUrl", "Forecast advisory", "Latest forecast advisory on www.nhc.noaa.gov");
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void number(SourceDescriptor.Builder b, String key, FieldType type, String unit, String label,
            String description) {
        b.fieldSpec(key, new FieldSpec(type, unit, label, description, null, Set.of()));
    }
}
