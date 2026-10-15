package ai.foresignal.source.adapters.celestrak;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code celestrak-socrates}: close approaches between catalogued objects from CelesTrak SOCRATES (prompt 064).
 *
 * <p>One conditional GET of {@code /SOCRATES/sort-minRange.csv} every 12 h (SOCRATES runs every 10–11 h; the
 * file carries an {@code ETag}, so an unchanged file is a 304), parsed row by row ({@link SocratesParser}).
 * Kind {@code conjunction}, {@code externalId} = {@code {lowNorad}-{highNorad}-{TCA epoch minute}},
 * {@code eventTime} = TCA, {@code observedAt} = receipt (the CSV carries no run time), no geometry, TTL 24 h
 * anchored at receipt. The CelesTrak usage policy and its error budget apply as for {@code celestrak-gp}
 * ({@link CelestrakPolicy}).
 */
@Component
public class CelestrakSocratesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "celestrak-socrates";

    /** The minimum-probability filter. */
    public static final String MIN_PROB_PARAM = "minProb";

    /** The maximum-range filter. */
    public static final String MAX_RANGE_PARAM = "maxRangeKm";

    /** The always-kept catalog numbers. */
    public static final String WATCH_PARAM = "watchNorad";

    /** The default minimum probability. */
    public static final double DEFAULT_MIN_PROB = 1e-5;

    /** The default maximum range, km. */
    public static final double DEFAULT_MAX_RANGE_KM = 1.0;

    /** The validator key. */
    static final String STATE_KEY = "socrates";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value CelestrakGpAdapter#BASE_URL_PROPERTY}; blank means CelesTrak
     */
    public CelestrakSocratesAdapter(@Value("${" + CelestrakGpAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? CelestrakGpAdapter.DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The SOCRATES CSV URL. */
    public URI csvUri() {
        return URI.create(baseUrl + "/SOCRATES/sort-minRange.csv");
    }

    /** Filters must be non-negative and watch entries catalog numbers. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        Double prob = params.number(MIN_PROB_PARAM);
        if (prob != null && (prob < 0 || prob > 1)) {
            problems.put(MIN_PROB_PARAM, "must be between 0 and 1, but was " + prob);
        }
        Double range = params.number(MAX_RANGE_PARAM);
        if (range != null && range < 0) {
            problems.put(MAX_RANGE_PARAM, "must not be negative, but was " + range);
        }
        if (params.has(WATCH_PARAM)) {
            for (String n : params.stringList(WATCH_PARAM)) {
                if (!n.strip().matches("\\d{1,9}")) {
                    problems.put(WATCH_PARAM, "'" + n + "' is not a catalog number of 1 to 9 digits");
                    break;
                }
            }
        }
        return problems;
    }

    /**
     * Fetches and filters the conjunction list.
     *
     * @throws ai.foresignal.source.spi.UpstreamHaltException on any status CelesTrak's policy says to stop on
     * @throws AdapterException when the network fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = CelestrakPolicy.get(ctx, csvUri(), STATE_KEY);
        if (response == null) {
            return FetchResult.unchanged();
        }
        if (response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException("SOCRATES CSV exceeded maxResponseBytes", null);
        }
        if (!SocratesParser.looksLike(response.body())) {
            throw CelestrakPolicy.unexpectedBody(ctx, STATE_KEY, "a body that is not the SOCRATES CSV");
        }
        SocratesParser.Parsed parsed;
        try {
            parsed = SocratesParser.parse(new ByteArrayInputStream(response.body()), filter(ctx.params()));
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        if (parsed.capped()) {
            ctx.log().info("{}: more than {} conjunctions matched the filters; kept the closest {}", ctx.sourceId(),
                    SocratesParser.MAX_RECORDS, SocratesParser.MAX_RECORDS);
        }
        if (parsed.skipped() > 0) {
            ctx.log().info("{}: skipped {} unreadable row(s) of {}", ctx.sourceId(), parsed.skipped(), parsed.rows());
        }
        return FetchResult.complete(parsed.records());
    }

    /** The filters from the parameters, defaults applied. */
    static SocratesParser.Filter filter(ValidatedParams params) {
        Double prob = params.number(MIN_PROB_PARAM);
        Double range = params.number(MAX_RANGE_PARAM);
        Set<Long> watch = new LinkedHashSet<>();
        if (params.has(WATCH_PARAM)) {
            for (String n : params.stringList(WATCH_PARAM)) {
                try {
                    watch.add(Long.parseLong(n.strip()));
                } catch (NumberFormatException ignored) {
                    // rejected by checkParams
                }
            }
        }
        return new SocratesParser.Filter(prob == null ? DEFAULT_MIN_PROB : prob,
                range == null ? DEFAULT_MAX_RANGE_KM : range, watch);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("CelesTrak SOCRATES conjunctions")
                .description("Predicted close approaches between catalogued objects from CelesTrak SOCRATES"
                        + " (sort-minRange.csv, about 23 MB, refreshed every 10–11 h). Rows are kept when any filter"
                        + " matches (minProb, maxRangeKm, watchNorad), at most 2,000 per run. Same CelesTrak usage"
                        + " policy as celestrak-gp: any unexpected status stops automatic retries for 24 h.")
                .kind(SocratesParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://celestrak.org/SOCRATES/")
                .attribution("CelesTrak SOCRATES (Dr. T.S. Kelso), https://celestrak.org/SOCRATES/")
                .licence("CelesTrak (data from 18th SDS via Space-Track)")
                .termsUrl("https://celestrak.org/NORAD/elements/usage-policy.php")
                .param(ParamSpec.optional(MIN_PROB_PARAM, ParamType.NUMBER,
                        "Keep conjunctions whose maximum probability is at least this", DEFAULT_MIN_PROB))
                .param(ParamSpec.optional(MAX_RANGE_PARAM, ParamType.NUMBER,
                        "Keep conjunctions whose miss distance is at most this, km", DEFAULT_MAX_RANGE_KM))
                .param(ParamSpec.optional(WATCH_PARAM, ParamType.STRING_LIST,
                        "Catalog numbers whose conjunctions are always kept", List.of()))
                .interval(Duration.ofHours(12), Duration.ofHours(12))
                .defaultBudget(1, 4)
                .maxResponseBytes(64 * 1024 * 1024)
                .fetchTimeout(Duration.ofSeconds(180))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                // Conjunctions are events (Conventions), 1825 days.
                .defaultRetention(Duration.ofDays(1825))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(Duration.ofHours(24))
                .ttlAnchor(TtlAnchor.RECEIVED)
                .titleField(null)
                .searchFields("name1", "name2")
                .followRedirects(false)
                .build();
    }
}
