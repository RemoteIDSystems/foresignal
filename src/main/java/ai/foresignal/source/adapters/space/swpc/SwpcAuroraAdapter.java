package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code swpc-aurora} (prompt 077): the OVATION aurora nowcast sampled at the operator's places, one
 * {@code aurora-probability} record per place ({@link OvationSampler}). Rendering the OVATION grid as a raster is
 * prompt 106's.
 *
 * <ul>
 *   <li><strong>Places:</strong> every place from prompt 036's {@code PlaceService} (through {@link PlacePoints}:
 *       the point itself, or the representative point of a polygon or route), by id, at most {@code maxPlaces}
 *       (default 50). No place module or no place: an empty complete result, nothing is downloaded, and the
 *       health reason {@value #NO_PLACES}. A place that leaves the set is tombstoned.
 *   <li><strong>Each run:</strong> one conditional GET of {@value #OVATION_URL} (about 930 KB); a 304 is
 *       {@link FetchResult#unchanged()}.
 *   <li><strong>Records:</strong> {@code externalId} = the place id, geometry = the place point, payload
 *       {@code placeName, probability} (the cell at the rounded longitude modulo 360 and latitude),
 *       {@code maxWithin300Km}, {@code forecastTime}; {@code observedAt} = the file's {@code Observation Time},
 *       {@code eventTime} = its {@code Forecast Time}.
 *   <li><strong>Private:</strong> places reveal where the operator is, so the source is private (prompts 008
 *       and 039).
 * </ul>
 */
@Component
public class SwpcAuroraAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "swpc-aurora";

    /** The record kind. */
    public static final String KIND = "aurora-probability";

    /** The OVATION nowcast. */
    public static final String OVATION_URL = "https://services.swpc.noaa.gov/json/ovation_aurora_latest.json";

    /** The conditional-GET state key. */
    public static final String OVATION_KEY = "ovation";

    /** The {@code maxPlaces} parameter. */
    public static final String MAX_PLACES_PARAM = "maxPlaces";

    /** The default {@code maxPlaces}. */
    public static final int DEFAULT_MAX_PLACES = 50;

    /** The health reason when there is no place to sample. */
    public static final String NO_PLACES = "no places";

    /** The {@link LiveIdSet} scope of the sampled places. */
    static final String PLACES_SCOPE = "places";

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofHours(1);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofMinutes(40);

    /** Raw retention: SAMPLED state at places, the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    /** The response cap (the file was 919 KB on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    private final URI ovationUri;
    private final Supplier<PlacePoints> places;
    private final SourceDescriptor descriptor = buildDescriptor();
    private OvationSampler lastGrid;

    /**
     * Creates the adapter for Spring.
     *
     * @param places the places, when the place module is present
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SwpcAuroraAdapter(ObjectProvider<PlacePoints> places,
            @Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(() -> places == null ? null : places.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param places supplies the places (null or a null result means none)
     * @param baseUrlOverride a base URL, or blank for the real upstream
     */
    public SwpcAuroraAdapter(Supplier<PlacePoints> places, String baseUrlOverride) {
        this.places = places == null ? () -> null : places;
        this.ovationUri = SpaceSources.uri(OVATION_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code maxPlaces} must be between 1 and 500. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(MAX_PLACES_PARAM) && params.integer(MAX_PLACES_PARAM) != null) {
            int v = params.integer(MAX_PLACES_PARAM);
            if (v < 1 || v > 500) {
                problems.put(MAX_PLACES_PARAM, "must be between 1 and 500");
            }
        }
        return problems;
    }

    /**
     * Samples the nowcast at every place (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the file cannot be used
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        AdapterStateStore state = ctx.state();
        PlacePoints placePoints = places.get();
        List<PlacePoints.Summary> chosen = placePoints == null ? List.of()
                : placePoints.summaries().stream().sorted(Comparator.comparing(PlacePoints.Summary::id)).toList();
        int maxPlaces = ctx.params().integer(MAX_PLACES_PARAM, DEFAULT_MAX_PLACES);
        String warning = null;
        if (chosen.size() > maxPlaces) {
            warning = chosen.size() + " places; only the first " + maxPlaces + " by id are sampled (parameter"
                    + " maxPlaces)";
            chosen = chosen.subList(0, maxPlaces);
        }
        List<NormalizedRecord> out = new ArrayList<>();
        for (String gone : LiveIdSet.absentSince(state, PLACES_SCOPE, chosen.stream().map(PlacePoints.Summary::id)
                .toList())) {
            out.add(NormalizedRecord.tombstone(gone, KIND, ctx.now()));
        }
        if (chosen.isEmpty()) {
            state.degrade(NO_PLACES);
            // Something changed in the place set since the last run: a fresh grid is needed for the next place.
            SourceHttp.forgetValidators(state, OVATION_KEY);
            return FetchResult.complete(out);
        }
        byte[] body = SpaceSources.conditional(ctx, ovationUri, OVATION_KEY);
        OvationSampler grid;
        if (body != null) {
            try {
                grid = OvationSampler.parse(body);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(state, OVATION_KEY);
                throw e;
            }
            lastGrid = grid;
        } else if (!placesChanged(state, chosen)) {
            clearOr(state, warning);
            return out.isEmpty() ? FetchResult.unchanged() : FetchResult.of(out);
        } else if (lastGrid != null) {
            // The places changed but the grid did not: sample the grid read last time.
            grid = lastGrid;
        } else {
            // The places changed, the grid did not, and it is not in memory (a restart): the next run reads it
            // again unconditionally rather than spending a second request of a one-a-minute budget now.
            SourceHttp.forgetValidators(state, OVATION_KEY);
            clearOr(state, warning);
            return FetchResult.of(out);
        }
        for (PlacePoints.Summary place : chosen) {
            out.add(record(place, grid));
        }
        state.put(PLACES_KEY, fingerprint(chosen));
        clearOr(state, warning);
        return FetchResult.complete(out);
    }

    /** The state key of the fingerprint of the last sampled place set. */
    static final String PLACES_KEY = "aurora:places";

    /** True when the places (ids, versions) differ from those of the last sampled grid. */
    static boolean placesChanged(AdapterStateStore state, List<PlacePoints.Summary> chosen) {
        return !fingerprint(chosen).equals(state.get(PLACES_KEY).orElse(""));
    }

    /** {@code id@version} of every place, in order. */
    static String fingerprint(List<PlacePoints.Summary> chosen) {
        StringBuilder s = new StringBuilder();
        for (PlacePoints.Summary p : chosen) {
            s.append(p.id()).append('@').append(p.version()).append('\n');
        }
        return s.toString();
    }

    private static void clearOr(AdapterStateStore state, String warning) {
        if (warning == null) {
            state.clearDegraded();
        } else {
            state.degrade(warning);
        }
    }

    /** The record of one place (see the class note). */
    static NormalizedRecord record(PlacePoints.Summary place, OvationSampler grid) {
        return NormalizedRecord.builder(place.id(), KIND)
                .observedAt(grid.observationTime())
                .eventTime(grid.forecastTime())
                .point(place.lon(), place.lat())
                .put("placeName", place.name())
                .put("probability", grid.probability(place.lon(), place.lat()))
                .put("maxWithin300Km", grid.maxWithin(place.lon(), place.lat(), OvationSampler.NEIGHBORHOOD_KM))
                .put("forecastTime", grid.forecastTime().toString())
                .build();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA SWPC aurora probability at places")
                .description("The NOAA SWPC OVATION aurora nowcast sampled at each of the operator's places: the"
                        + " probability of visible aurora in the place's one-degree cell and the highest probability"
                        + " within 300 km, with the forecast time. Private: places reveal where the operator is.")
                .kind(KIND)
                .homepage("https://www.swpc.noaa.gov/products/aurora-30-minute-forecast")
                .attribution("NOAA Space Weather Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(1, 100, 100))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .privateSource(true)
                .titleField("placeName")
                .searchFields("placeName")
                .rankField("probability")
                .param(ParamSpec.optional(MAX_PLACES_PARAM, ParamType.INTEGER,
                        "The most places sampled, by id, 1 to 500", DEFAULT_MAX_PLACES));
        b.fieldSpec("placeName", new FieldSpec(FieldType.STRING, null, "Place", "The place's name", null, Set.of()));
        b.fieldSpec("probability", new FieldSpec(FieldType.INTEGER, "%", "Aurora probability",
                "OVATION probability of visible aurora in the place's one-degree cell, 0-100", null, Set.of()));
        b.fieldSpec("maxWithin300Km", new FieldSpec(FieldType.INTEGER, "%", "Highest within 300 km",
                "The highest OVATION probability of the cells within 300 km of the place", null, Set.of()));
        b.fieldSpec("forecastTime", new FieldSpec(FieldType.TIMESTAMP, null, "Forecast time",
                "The time the nowcast is valid for (UTC)", null, Set.of()));
        return b.build();
    }
}
