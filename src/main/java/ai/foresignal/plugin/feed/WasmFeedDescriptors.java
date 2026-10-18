package ai.foresignal.plugin.feed;

import ai.foresignal.plugin.PluginLimits;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.Locale;

/**
 * The descriptor of a {@code wasm-feed} source, built from its spec (prompt 141).
 *
 * <ul>
 *   <li><strong>Fixed:</strong> POLLING; redirects are never followed (a redirect could leave the URL the outbound
 *       policy checked); a 60 s fetch timeout, which covers the 5 s plugin call.
 *   <li><strong>Defaults:</strong> interval 15 min (floor 5 min), budget 4 a minute and 400 a day, response cap 8 MB
 *       (at most the plugin input cap, 16 MB), STANDARD tier, time base RECEIVED, TTL anchor RECEIVED, policy
 *       {@code ALL}, retention 1825 d (the {@code event} class), egress OPEN.
 *   <li><strong>The operator's:</strong> everything else in the spec, as written.
 * </ul>
 */
public final class WasmFeedDescriptors {

    /** The fetch timeout. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(60);

    /** The default poll interval. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(15);

    /** The default floor on the poll interval. */
    public static final Duration DEFAULT_MIN_INTERVAL = Duration.ofMinutes(5);

    /** The default requests per minute. */
    public static final int DEFAULT_PER_MINUTE = 4;

    /** The default requests per day. */
    public static final int DEFAULT_PER_DAY = 400;

    /** The default raw retention: the {@code event} class (Conventions). */
    public static final Duration DEFAULT_RETENTION = Duration.ofDays(1825);

    /** The default response cap. */
    public static final int DEFAULT_MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /** The largest response cap: what a plugin accepts as input. */
    public static final int MAX_RESPONSE_BYTES = PluginLimits.DEFAULTS.maxInputBytes();

    private WasmFeedDescriptors() {}

    /**
     * The descriptor of {@code spec}'s source.
     *
     * @param spec the spec, already checked by {@link WasmFeedSpecs}
     * @param policy the parsed ingest policy, or null for {@code ALL}
     * @throws ai.foresignal.api.error.InvalidInputException when a duration does not parse
     * @throws IllegalArgumentException when an enumerated value is unknown
     */
    public static SourceDescriptor descriptor(WasmFeedSpec spec, IngestPolicy policy) {
        HistoryTier tier = enumOr(spec.historyTier(), HistoryTier.class, HistoryTier.STANDARD);
        Duration retention = duration(spec.retention(), DEFAULT_RETENTION);
        SourceDescriptor.Builder b = SourceDescriptor.builder(spec.id())
                .name(spec.name())
                .description(spec.description() == null || spec.description().isBlank()
                        ? spec.name() + " (wasm-feed): records parsed from " + spec.url() + " by the plugin "
                                + spec.plugin() + "."
                        : spec.description())
                .homepage(spec.homepage())
                .attribution(spec.attribution())
                .licence(spec.license())
                .termsUrl(spec.termsUrl())
                .mode(Mode.POLLING)
                .interval(duration(spec.minInterval(), DEFAULT_MIN_INTERVAL), duration(spec.interval(), DEFAULT_INTERVAL))
                .defaultBudget(budget(spec.budget()))
                .maxResponseBytes(spec.maxResponseBytes() == null ? DEFAULT_MAX_RESPONSE_BYTES : spec.maxResponseBytes())
                .fetchTimeout(FETCH_TIMEOUT)
                .followRedirects(false)
                .historyTier(tier)
                .observedAtMode(enumOr(spec.observedAtMode(), ObservedAtMode.class, ObservedAtMode.RECEIVED))
                .ttlAnchor(enumOr(spec.ttlAnchor(), TtlAnchor.class, TtlAnchor.RECEIVED))
                .recordTtl(spec.recordTtl() == null || spec.recordTtl().isBlank() ? null
                        : Durations.parsePositive(spec.recordTtl()))
                .defaultIngestPolicy(policy == null ? new IngestPolicy.All(null) : policy)
                .egress(enumOr(spec.egress(), Egress.class, Egress.OPEN))
                .zeroResultsNormal(Boolean.TRUE.equals(spec.zeroResultsNormal()))
                .titleField(spec.titleField());
        if (spec.licenseTerms() != null) {
            b.licenceTerms(spec.licenseTerms());
        }
        spec.kinds().forEach(b::kind);
        if (tier == HistoryTier.STANDARD) {
            b.defaultRetention(retention);
            spec.kinds().forEach(kind -> b.kindRetention(kind, retention));
        }
        if (spec.searchFields() != null) {
            b.searchFields(spec.searchFields().toArray(String[]::new));
        }
        b.embedFields(spec.embedFields().toArray(String[]::new));
        b.tileProperties(spec.tileProperties().toArray(String[]::new));
        b.volatileFields(spec.volatileFields().toArray(String[]::new));
        b.significantFields(spec.significantFields().toArray(String[]::new));
        spec.fieldSpecs().forEach(b::fieldSpec);
        return b.build();
    }

    /** The spec's budget with defaults. */
    static Budget budget(WasmFeedSpec.Budget budget) {
        int perMinute = budget == null || budget.perMinute() == null ? DEFAULT_PER_MINUTE : budget.perMinute();
        int perDay = budget == null || budget.perDay() == null ? DEFAULT_PER_DAY : budget.perDay();
        return budget == null || budget.mbPerDay() == null ? Budget.of(perMinute, perDay)
                : Budget.of(perMinute, perDay, budget.mbPerDay());
    }

    /** {@code text} parsed, or {@code fallback} when blank. */
    static Duration duration(String text, Duration fallback) {
        return text == null || text.isBlank() ? fallback : Durations.parsePositive(text);
    }

    /** The enum constant named by {@code text} (case-insensitive), or {@code fallback} when blank. */
    static <E extends Enum<E>> E enumOr(String text, Class<E> type, E fallback) {
        return text == null || text.isBlank() ? fallback : Enum.valueOf(type, text.strip().toUpperCase(Locale.ROOT));
    }
}
