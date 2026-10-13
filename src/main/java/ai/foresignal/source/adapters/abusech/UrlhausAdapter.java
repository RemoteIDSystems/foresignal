package ai.foresignal.source.adapters.abusech;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * URLhaus recent malware URLs (prompt 076): one {@code malware-url} record per URLhaus id, no geometry.
 *
 * <p><strong>Credential:</strong> {@value #AUTH_KEY} (free from {@code auth.abuse.ch}), sent as the
 * {@code Auth-Key} header; verified 2026-10-13, {@code /v1/urls/recent/limit/3/} answers 401 without one. A
 * missing key fails before any request; a 401 is the client's {@code UpstreamHttpException}. The key is a
 * credential value, so the upstream client redacts it from traces, recorded exchanges and logs.
 *
 * <p><strong>Each run:</strong> {@code GET https://urlhaus-api.abuse.ch/v1/urls/recent/limit/{limit}/} (param
 * {@value #LIMIT_PARAM}, default and maximum 1000: the API returns additions of the last three days, at most 1000).
 * The bulk {@code downloads/json_recent/} file is not used: the API page says every download needs an Auth-Key, and
 * it is about 17 MB. Records are a window: never tombstoned, they expire 30 days after they were last received.
 *
 * <p>The {@code url} is hostile: it is never fetched, never rendered as a link ({@code noLink}) and reaches an LLM only
 * inside an untrusted block.
 */
@Component
public class UrlhausAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "abusech-urlhaus";

    /** The credential name. */
    public static final String AUTH_KEY = "authKey";

    /** The request header carrying the key. */
    public static final String AUTH_HEADER = "Auth-Key";

    /** The limit parameter. */
    public static final String LIMIT_PARAM = "limit";

    /** The default and largest limit. */
    public static final int MAX_LIMIT = 1000;

    /** The API base. */
    public static final String API_URL = "https://urlhaus-api.abuse.ch/v1/urls/recent/limit/";

    /** The test-only property moving every cyber-source URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cyber.base-url";

    /** Raw retention: malware URLs are the {@code bulk-event} analytic class (Conventions), 90 days. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The record TTL, anchored on receipt. */
    static final Duration TTL = Duration.ofDays(30);

    private final String baseUrlOverride;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public UrlhausAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code limit} must be between 1 and 1000. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(LIMIT_PARAM)) {
            Integer limit = params.integer(LIMIT_PARAM);
            if (limit == null || limit < 1 || limit > MAX_LIMIT) {
                problems.put(LIMIT_PARAM, "must be between 1 and " + MAX_LIMIT + ", but was " + limit);
            }
        }
        return problems;
    }

    /**
     * The request URL for {@code limit} (clamped to 1–1000).
     *
     * @param limit the number of recent URLs to ask for
     */
    public URI recentUri(int limit) {
        int n = Math.max(1, Math.min(MAX_LIMIT, limit));
        return URI.create(KitUrls.rebased(API_URL + n + "/", baseUrlOverride));
    }

    /**
     * Fetches the recent URLs once (see the class note).
     *
     * @throws AdapterException when the key is missing or the request fails (a 401 included)
     * @throws UpstreamFormatException on a bad status, a truncated body or a response that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(AUTH_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + AUTH_KEY + " is not set (a free Auth-Key from"
                    + " auth.abuse.ch)");
        }
        int limit = ctx.params() != null && ctx.params().has(LIMIT_PARAM)
                ? ctx.params().integer(LIMIT_PARAM, MAX_LIMIT) : MAX_LIMIT;
        UpstreamResponse response = ctx.http().get(recentUri(limit), Map.of(AUTH_HEADER, key.strip(), "Accept",
                "application/json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the API answered HTTP " + response.status() : ": the response exceeds the cap"), "");
        }
        UrlhausParser.Recent recent = UrlhausParser.parse(response.body());
        ctx.log().debug("{}: {} recent URL(s), skipped {}", ctx.sourceId(), recent.records().size(),
                recent.skipped());
        return FetchResult.of(recent.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = UrlhausParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("abuse.ch URLhaus malware URLs")
                .description("Malware distribution URLs added to abuse.ch URLhaus in the last three days (at most"
                        + " 1000 per request): the URL and a defanged copy, host, status, threat, tags, blacklist"
                        + " statuses and reporter. No geometry. The URL is hostile: never fetched, never a link."
                        + " Needs a free Auth-Key; commercial or for-profit use may require a Spamhaus subscription."
                        + " Records expire 30 days after they were last received.")
                .kind(kind)
                .homepage("https://urlhaus.abuse.ch/")
                .attribution("abuse.ch URLhaus")
                .licence("abuse.ch Terms of Use: free under the fair use principles; \"Use of the API by companies,"
                        + " networks, or individuals with commercial or for-profit needs may require a paid"
                        + " subscription\" (Spamhaus)")
                .termsUrl("https://abuse.ch/terms-of-use/")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(AUTH_KEY, "abuse.ch Auth-Key (free from auth.abuse.ch), sent as"
                        + " the Auth-Key header"))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(2, 200))
                .maxResponseBytes(8 * 1024 * 1024)
                .zeroResultsNormal(true)
                .recordTtl(TTL)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("urlDefanged")
                .searchFields("urlDefanged", "host", "threat", "tags")
                .param(ParamSpec.optional(LIMIT_PARAM, ParamType.INTEGER, "How many recent URLs to request (1-1000;"
                        + " the API returns additions of the last three days)", MAX_LIMIT));
        b.fieldSpec("url", new FieldSpec(FieldType.STRING, null, "URL", "The malware URL; hostile: never fetched,"
                + " never rendered as a link, untrusted text", null, Set.of(), true));
        b.fieldSpec("urlDefanged", new FieldSpec(FieldType.STRING, null, "URL (defanged)", "The URL with hxxp and"
                + " [.] in the host, safe to display; never a link", null, Set.of(), true));
        text(b, "host", "Host", "The URL's host (IP address or domain name)");
        text(b, "urlStatus", "Status", "online, offline or unknown");
        text(b, "threat", "Threat", "The threat type, for example malware_download");
        b.fieldSpec("tags", new FieldSpec(FieldType.ARRAY, null, "Tags", "URLhaus tags, at most 20", null, Set.of()));
        b.fieldSpec("blacklists", new FieldSpec(FieldType.OBJECT, null, "Blacklists", "spamhaus_dbl and surbl"
                + " statuses", null, Set.of()));
        text(b, "reporter", "Reporter", "The reporter's handle, or anonymous");
        text(b, "reference", "URLhaus entry", "The URLhaus page of this entry");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
