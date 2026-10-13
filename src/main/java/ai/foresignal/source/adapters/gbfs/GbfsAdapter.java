/*
 * The fetch rules (no redirects followed, any 3xx refused with the redirect's host reported, the 5 MB body cap) are
 * ported from God's Eye View, server/providers/gbfs.js (fetchGbfsUpstream, gbfsRedirectHost),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java on Foresignal's budgeted upstream client (conditional GETs, the descriptor's cap and timeout); the
 * GBFS ttl is honored; station_information is cached in memory and refreshed after max(ttl, 1 h).
 */
package ai.foresignal.source.adapters.gbfs;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * One {@code gbfs-{id}} source (prompt 071): a reviewed docked bikeshare system ({@link GbfsSystem}) as
 * {@code bikeshare-station} records ({@link GbfsDescriptors#descriptor}, {@link GbfsParser}).
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>When the cached {@code station_information} is missing, older than max(its {@code ttl}, 1 h), or was read in
 *       another {@code language}, it is fetched with a conditional GET first. The parsed stations are kept in memory
 *       by {@code station_id}, so a restart refetches them: the stored validators are forgotten when nothing is
 *       cached, or the first request after a restart would get a 304 and no stations.
 *   <li>The status request is skipped entirely ({@link FetchResult#unchanged()}) while the previous status's
 *       {@code last_updated + ttl} is still in the future and later than the next scheduled run (now plus the poll
 *       interval). This honors GBFS {@code ttl} without polling faster than published; {@code ttl = 0} means
 *       "always refresh", and a {@code ttl} above {@value #MAX_HONORED_TTL_SECONDS} s is honored only up to that.
 *   <li>{@code station_status} is fetched with a conditional GET (a 304 is unchanged) and merged with the cached
 *       information into one complete snapshot.
 * </ol>
 *
 * <p><strong>Redirects are refused:</strong> the descriptor turns redirect following off, and any 3xx fails the run
 * with {@code UpstreamFormatException("redirect refused: {host}")}, a failure rather than a backoff; the redirect
 * target is never requested. A truncated or non-200 body is a format error too, and its validators are forgotten.
 *
 * <p>Status entries without a usable {@code last_reported} are counted in {@value #UNTIMED_METRIC} and entries whose
 * station is not (yet) in {@code station_information} in {@value #UNMATCHED_METRIC}, both tagged {@code source}.
 * A station missing from status is not tombstoned: it expires with the 30-minute TTL.
 */
public class GbfsAdapter implements PollingAdapter {

    /** The counter of status entries dropped for lack of a usable {@code last_reported}. */
    public static final String UNTIMED_METRIC = "foresignal.gbfs.untimed";

    /** The counter of status entries skipped because their station has no information. */
    public static final String UNMATCHED_METRIC = "foresignal.gbfs.unmatched";

    /** The {@code getConditional} state key of {@code station_information}. */
    public static final String INFORMATION_KEY = "information";

    /** The {@code getConditional} state key of {@code station_status}. */
    public static final String STATUS_KEY = "status";

    /** The shortest refresh interval of the cached {@code station_information}. */
    public static final Duration MIN_INFORMATION_REFRESH = Duration.ofHours(1);

    /** The longest status {@code ttl} honored, in seconds. */
    public static final long MAX_HONORED_TTL_SECONDS = 3_600;

    /** An accepted {@code language} parameter: a BCP 47 tag. */
    static final Pattern LANGUAGE = Pattern.compile("^[A-Za-z]{2,8}(-[A-Za-z0-9]{1,8})*$");

    private final GbfsSystem system;
    private final SourceDescriptor descriptor;
    private final Supplier<MeterRegistry> meters;

    private Map<String, GbfsParser.StationInfo> information;
    private String informationLanguage;
    private Instant informationFetchedAt;
    private Duration informationRefresh = MIN_INFORMATION_REFRESH;
    private Instant statusExpiresAt;

    /**
     * Creates the source.
     *
     * @param system the reviewed system
     * @param meters supplies the registry of the counters, or null
     */
    public GbfsAdapter(GbfsSystem system, Supplier<MeterRegistry> meters) {
        this.system = system;
        this.descriptor = GbfsDescriptors.descriptor(system);
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The system entry. */
    public GbfsSystem system() {
        return system;
    }

    /** {@code language} must be a BCP 47 tag. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String language = params.optionalString(GbfsDescriptors.LANGUAGE_PARAM).orElse(null);
        if (language != null && !LANGUAGE.matcher(language.strip()).matches()) {
            problems.put(GbfsDescriptors.LANGUAGE_PARAM, "must be a BCP 47 language tag such as en or fr-CA");
        }
        return problems;
    }

    /**
     * Fetches the system once (see the class note).
     *
     * @return {@link FetchResult#unchanged()} on a 304 or while the status {@code ttl} holds; otherwise the complete
     *     snapshot
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a redirect, a truncated or non-200 body, or a document that is not GBFS
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        String language = ctx.params().optionalString(GbfsDescriptors.LANGUAGE_PARAM)
                .map(String::strip).filter(s -> !s.isEmpty()).orElse(GbfsParser.DEFAULT_LANGUAGE);
        if (informationStale(now, language)) {
            refreshInformation(ctx, now, language);
        }
        Duration interval = ctx.descriptor() == null ? GbfsDescriptors.INTERVAL
                : ctx.descriptor().effectivePollInterval();
        if (statusStillValid(now, interval)) {
            ctx.log().debug("{}: station_status is valid until {}; not requested", ctx.sourceId(), statusExpiresAt);
            return FetchResult.unchanged();
        }
        UpstreamResponse response = request(ctx, system.stationStatusUrl(), STATUS_KEY);
        if (response == null) {
            return FetchResult.unchanged();
        }
        GbfsParser.Status status = parse(ctx, STATUS_KEY, () -> GbfsParser.parseStatus(response.body()));
        statusExpiresAt = expiry(status.lastUpdated(), status.ttlSeconds());
        GbfsParser.Snapshot snapshot = GbfsParser.merge(system.id(), information, status);
        count(ctx, UNTIMED_METRIC, snapshot.untimed(),
                "GBFS status entries dropped because they carry no usable last_reported");
        count(ctx, UNMATCHED_METRIC, snapshot.unmatched(),
                "GBFS status entries skipped because station_information does not list the station");
        ctx.log().debug("{}: {} station(s), {} untimed, {} without information, {} malformed", ctx.sourceId(),
                snapshot.records().size(), snapshot.untimed(), snapshot.unmatched(), status.skipped());
        return FetchResult.complete(snapshot.records());
    }

    /** True when the cached information must be (re)fetched. */
    synchronized boolean informationStale(Instant now, String language) {
        return information == null || informationFetchedAt == null || !language.equals(informationLanguage)
                || !now.isBefore(informationFetchedAt.plus(informationRefresh));
    }

    /** True when the previous status is still valid at the next scheduled run. */
    synchronized boolean statusStillValid(Instant now, Duration interval) {
        return statusExpiresAt != null && statusExpiresAt.isAfter(now) && statusExpiresAt.isAfter(now.plus(interval));
    }

    /** Forgets the in-memory caches, as a restart does (tests). */
    synchronized void forgetCache() {
        information = null;
        informationLanguage = null;
        informationFetchedAt = null;
        informationRefresh = MIN_INFORMATION_REFRESH;
        statusExpiresAt = null;
    }

    private void refreshInformation(FetchContext ctx, Instant now, String language) throws AdapterException {
        if (information == null || !language.equals(informationLanguage)) {
            // Nothing usable is cached (a restart, or another language): ask unconditionally.
            SourceHttp.forgetValidators(ctx.state(), INFORMATION_KEY);
        }
        UpstreamResponse response = request(ctx, system.stationInformationUrl(), INFORMATION_KEY);
        if (response == null) {
            if (information == null) {
                SourceHttp.forgetValidators(ctx.state(), INFORMATION_KEY);
                throw new UpstreamFormatException("station_information answered 304 with nothing cached", "");
            }
            informationFetchedAt = now;
            return;
        }
        GbfsParser.Information parsed =
                parse(ctx, INFORMATION_KEY, () -> GbfsParser.parseInformation(response.body(), language));
        information = parsed.stations();
        informationLanguage = language;
        informationFetchedAt = now;
        Duration ttl = Duration.ofSeconds(parsed.ttlSeconds());
        informationRefresh = ttl.compareTo(MIN_INFORMATION_REFRESH) > 0 ? ttl : MIN_INFORMATION_REFRESH;
        ctx.log().debug("{}: station_information lists {} station(s), {} skipped; refreshed again after {}",
                ctx.sourceId(), information.size(), parsed.skipped(), informationRefresh);
    }

    /**
     * One conditional GET.
     *
     * @return the 200 response, or null on a 304
     * @throws UpstreamFormatException on a 3xx, another non-200 status or a truncated body
     */
    private static UpstreamResponse request(FetchContext ctx, URI url, String stateKey) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(url, stateKey);
        if (response.notModified()) {
            return null;
        }
        int status = response.status();
        if (status >= 300 && status < 400) {
            String location = redirectHost(url, response.header("location"));
            throw new UpstreamFormatException("redirect refused: " + url.getHost()
                    + (location == null ? "" : " (to " + location + ")"), "");
        }
        if (status != 200) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw new UpstreamFormatException("GBFS " + stateKey + " answered HTTP " + status, "");
        }
        if (response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw new UpstreamFormatException("GBFS " + stateKey + " exceeds "
                    + GbfsDescriptors.MAX_RESPONSE_BYTES + " bytes", "");
        }
        return response;
    }

    /** The host a redirect points at (GEV {@code gbfsRedirectHost}), or null. */
    static String redirectHost(URI request, String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        try {
            return request.resolve(location.strip()).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static <T> T parse(FetchContext ctx, String stateKey, Supplier<T> parser) {
        try {
            return parser.get();
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw e;
        }
    }

    private static Instant expiry(Instant lastUpdated, long ttlSeconds) {
        if (lastUpdated == null || ttlSeconds <= 0) {
            return null;
        }
        return lastUpdated.plusSeconds(Math.min(ttlSeconds, MAX_HONORED_TTL_SECONDS));
    }

    private void count(FetchContext ctx, String metric, int amount, String description) {
        if (amount <= 0) {
            return;
        }
        MeterRegistry registry = meters.get();
        if (registry != null) {
            Counter.builder(metric).tag("source", ctx.sourceId()).description(description).register(registry)
                    .increment(amount);
        }
    }
}
