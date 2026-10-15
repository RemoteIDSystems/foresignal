package ai.foresignal.source.adapters.celestrak;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
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
import ai.foresignal.space.OmmElements;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code celestrak-gp}: GP element sets from CelesTrak (prompt 064), under CelesTrak's usage policy of
 * 2026-05-22 ({@link CelestrakPolicy}).
 *
 * <p><b>Fetch, strictly sequential:</b> one conditional GET of
 * {@code /NORAD/elements/gp.php?GROUP={group}&FORMAT={format}} (default {@code active}, CSV: the default format
 * became CSV on 2026-05-09, and TLE cannot carry catalog numbers ≥ 100000), then at most two member groups
 * whose membership is older than 24 h, oldest first ({@link Memberships}). A repeat request inside one update
 * gets CelesTrak's "not updated" 403, which is a not-modified result. Data is checked every 2 h, which is
 * both the minimum and the default interval.
 *
 * <p><b>Records:</b> kind {@code satellite}, {@code externalId} = {@code NORAD_CAT_ID} (up to nine digits),
 * {@code observedAt} = {@code EPOCH} (an older epoch is LATE and dropped), no event time and no geometry
 * (clients propagate with SGP4); the OMM keys verbatim plus {@code groups}. TTL 3 days anchored at receipt, so
 * an object missing from {@code active} for 3 days expires.
 *
 * <p><b>Bytes:</b> {@code active} as CSV is about 3 MB a run, under CelesTrak's 100 MB a day;
 * {@code ai.foresignal.space.CelestrakBytesHealthRule} makes health {@code degraded} above 60 MB a day.
 */
@Component
public class CelestrakGpAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "celestrak-gp";

    /** The production host. */
    public static final String DEFAULT_BASE_URL = "https://celestrak.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.celestrak.base-url";

    /** The main group parameter. */
    public static final String GROUP_PARAM = "group";

    /** The member groups parameter. */
    public static final String MEMBERS_PARAM = "memberGroups";

    /** The format parameter. */
    public static final String FORMAT_PARAM = "format";

    /** The record TTL. */
    public static final Duration TTL = Duration.ofDays(3);

    /** The poll interval: CelesTrak checks GP data every 2 h. */
    public static final Duration INTERVAL = Duration.ofHours(2);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public CelestrakGpAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = trimmed(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The GP URL of {@code group} in {@code format}. */
    public URI gpUri(String group, String format) {
        return URI.create(baseUrl + "/NORAD/elements/gp.php?GROUP=" + URLEncoder.encode(group, StandardCharsets.UTF_8)
                + "&FORMAT=" + format);
    }

    /** {@code group} must be {@code active} or an allowlisted group. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String group = params.string(GROUP_PARAM);
        if (group != null && !CelestrakGroups.allowed(group)) {
            problems.put(GROUP_PARAM, "must match " + CelestrakGroups.PATTERN_TEXT
                    + " and be a reviewed CelesTrak group, but was '" + group + "'");
        }
        return problems;
    }

    /**
     * Fetches the main group and up to two stale member groups, one request after another.
     *
     * @throws ai.foresignal.source.spi.UpstreamHaltException on any status CelesTrak's policy says to stop on
     * @throws AdapterException when the network fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        String group = params.optionalString(GROUP_PARAM).orElse(CelestrakGroups.ACTIVE);
        String format = params.optionalString(FORMAT_PARAM).orElse("csv");
        List<String> members = memberGroups(params);

        String mainKey = stateKey(group);
        UpstreamResponse main = CelestrakPolicy.get(ctx, gpUri(group, format), mainKey);
        CelestrakGpParser.Parsed parsed = main == null ? null : parse(ctx, main, group, format, mainKey);

        Instant now = ctx.now();
        for (String member : Memberships.due(ctx.state(), members, now)) {
            if (member.equals(group)) {
                continue;
            }
            String key = stateKey(member);
            UpstreamResponse response = CelestrakPolicy.get(ctx, gpUri(member, format), key);
            if (response == null) {
                Memberships.touch(ctx.state(), member, now);
                continue;
            }
            CelestrakGpParser.Parsed memberSet = parse(ctx, response, member, format, key);
            TreeSet<Long> norads = new TreeSet<>();
            memberSet.elements().forEach(e -> norads.add(e.norad()));
            Memberships.store(ctx.state(), member, norads, now);
            ctx.log().debug("{}: membership of {} refreshed ({} objects)", ctx.sourceId(), member, norads.size());
        }

        if (parsed == null) {
            return FetchResult.unchanged();
        }
        if (parsed.skipped() > 0) {
            ctx.log().info("{}: skipped {} unreadable element set(s) of {}", ctx.sourceId(), parsed.skipped(), group);
        }
        Map<Long, List<String>> index = Memberships.index(ctx.state(), members);
        List<NormalizedRecord> records = new ArrayList<>(parsed.elements().size());
        for (OmmElements e : parsed.elements()) {
            List<String> groups = new ArrayList<>(index.getOrDefault(e.norad(), List.of()));
            if (!CelestrakGroups.ACTIVE.equals(group) && !groups.contains(group)) {
                groups.add(group);
                groups.sort(null);
            }
            records.add(CelestrakGpParser.record(e, groups));
        }
        return FetchResult.complete(records);
    }

    private static CelestrakGpParser.Parsed parse(FetchContext ctx, UpstreamResponse response, String group,
            String format, String key) throws AdapterException {
        if (response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw new UpstreamFormatException("CelesTrak GROUP=" + group + " exceeded maxResponseBytes", null);
        }
        if (!CelestrakGpParser.looksLike(response.body(), format)) {
            throw CelestrakPolicy.unexpectedBody(ctx, key, "a body that is not " + format.toUpperCase(java.util.Locale.ROOT));
        }
        try {
            return "json".equals(format)
                    ? CelestrakGpParser.parseJson(response.body(), group)
                    : CelestrakGpParser.parseCsv(response.body(), group);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw e;
        }
    }

    /** The configured member groups, defaulting to {@link CelestrakGroups#DEFAULT_MEMBERS}. */
    static List<String> memberGroups(ValidatedParams params) {
        List<String> configured = params.has(MEMBERS_PARAM) ? params.stringList(MEMBERS_PARAM) : null;
        List<String> chosen = configured == null ? CelestrakGroups.DEFAULT_MEMBERS : configured;
        return chosen.stream().filter(CelestrakGroups::allowed).filter(g -> !CelestrakGroups.ACTIVE.equals(g))
                .distinct().toList();
    }

    /** The conditional-GET validator key of a group. */
    static String stateKey(String group) {
        return "gp:" + group;
    }

    private static String trimmed(String override) {
        String chosen = override == null || override.isBlank() ? DEFAULT_BASE_URL : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    private static SourceDescriptor buildDescriptor() {
        List<String> memberAllowed = CelestrakGroups.ALLOWED.stream()
                .filter(g -> !CelestrakGroups.ACTIVE.equals(g)).toList();
        return SourceDescriptor.builder(ID)
                .name("CelesTrak GP element sets")
                .description("General-perturbations element sets (CCSDS OMM) for every tracked object from CelesTrak,"
                        + " in CSV (CelesTrak's default since 2026-05-09; TLE cannot carry the six-digit catalog"
                        + " numbers in use since 2026-07-11). Fetched every 2 h, one request per group, strictly"
                        + " sequential; member groups about once a day. Any unexpected status stops automatic"
                        + " retries for 24 h, per CelesTrak's usage policy: investigate, then use fetch-now. No"
                        + " geometry: clients propagate with SGP4. Also served CelesTrak-compatibly at"
                        + " /compat/celestrak/gp.php.")
                .kind(CelestrakGpParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://celestrak.org")
                .attribution("CelesTrak (Dr. T.S. Kelso), https://celestrak.org; data from the 18th Space Defense"
                        + " Squadron via Space-Track")
                .licence("CelesTrak (data from 18th SDS via Space-Track)")
                .termsUrl("https://celestrak.org/NORAD/elements/usage-policy.php")
                .param(ParamSpec.optional(GROUP_PARAM, ParamType.STRING,
                        "The CelesTrak group fetched every run (default active: every object)", CelestrakGroups.ACTIVE))
                .param(new ParamSpec(MEMBERS_PARAM, ParamType.STRING_LIST,
                        "Groups whose membership fills each record's groups; at most two are fetched per run, so each"
                                + " about once a day", CelestrakGroups.DEFAULT_MEMBERS, false, memberAllowed))
                .param(ParamSpec.enumerated(FORMAT_PARAM, "csv (default) or json", "csv", List.of("csv", "json")))
                .interval(INTERVAL, INTERVAL)
                .defaultBudget(3, 48)
                .maxResponseBytes(32 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                // Orbital element sets are a catalogue; the reference class (Conventions), 90 days.
                .defaultRetention(Duration.ofDays(90))
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .titleField("OBJECT_NAME")
                .searchFields("OBJECT_NAME", "OBJECT_ID")
                .zeroResultsNormal(false)
                .followRedirects(false)
                .build();
    }
}
