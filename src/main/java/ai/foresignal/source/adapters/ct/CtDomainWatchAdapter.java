package ai.foresignal.source.adapters.ct;

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
import ai.foresignal.source.spi.NormalizedRecord;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Certificate issuances for operator-chosen domains (prompt 076) from the SSLMate Cert Spotter CT Search API v1.
 * crt.sh polling is rejected (Gap-Analysis §9) and no adapter contacts a CT log.
 *
 * <p><strong>Request:</strong> {@code GET https://api.certspotter.com/v1/issuances?domain={d}&include_subdomains=true
 * &match_wildcards=true&expand=dns_names&expand=issuer&expand=issuer.caa_domains&expand=issuer.operator&after=
 * {cursor}}. Unauthenticated queries are allowed for personal or evaluation use up to an hourly limit (verified
 * 2026-10-13: {@code X-RateLimit-Limit: 10}); production use needs an API key, the optional credential
 * {@value #API_KEY}, sent as {@code Authorization: Bearer}. A {@code rate_limited} answer is throttling: the upstream
 * client honors its {@code Retry-After}.
 *
 * <p><strong>Each run</strong> walks the watched domains round-robin, starting after the domain the previous run
 * stopped at ({@value #ROTATION_KEY}). Per domain it pages with {@code after} (cursor {@code after:{domain}}) until an
 * empty array, at most {@value #MAX_PAGES} pages; the first run of a domain lists every unexpired issuance. Requests
 * are paced {@value #DEFAULT_PAUSE_TEXT} apart. A failed request after records were collected ends the run with
 * those records (a refused budget or a {@code rate_limited} answer is the normal way a run ends without a key); a
 * failure before anything was collected fails the run. Cursors are saved only with the records they cover.
 */
@Component
public class CtDomainWatchAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ct-domain-watch";

    /** The optional credential, sent as {@code Authorization: Bearer}. */
    public static final String API_KEY = "apiKey";

    /** The watched-domains parameter. */
    public static final String DOMAINS_PARAM = "domains";

    /** The expected-issuers parameter. */
    public static final String EXPECTED_ISSUERS_PARAM = "expectedIssuers";

    /** The most watched domains. */
    public static final int MAX_DOMAINS = 25;

    /** The most pages per domain and run. */
    public static final int MAX_PAGES = 10;

    /** The API. */
    public static final String API_URL = "https://api.certspotter.com/v1/issuances";

    /** The fixed query after the domain. */
    static final String QUERY = "&include_subdomains=true&match_wildcards=true&expand=dns_names&expand=issuer"
            + "&expand=issuer.caa_domains&expand=issuer.operator";

    /** The state-key prefix of a domain's cursor. */
    public static final String CURSOR_PREFIX = "after:";

    /** The state key of the next domain to poll. */
    public static final String ROTATION_KEY = "rotation:next";

    /** The test-only property moving every cyber-source URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cyber.base-url";

    /** The test-only property of the pause between requests ({@value #DEFAULT_PAUSE_TEXT}). */
    public static final String PAUSE_PROPERTY = "foresignal.test.adapters.ct.pause";

    /** The default pause between requests: the default per-minute budget, 2/min. */
    public static final String DEFAULT_PAUSE_TEXT = "PT30S";

    /** Raw retention: CT-log hits are the {@code bulk-event} analytic class (Conventions), 90 days. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** Waits between requests; replaceable in tests. */
    @FunctionalInterface
    public interface Pauser {

        /**
         * Waits {@code duration}.
         *
         * @param duration how long
         * @throws InterruptedException when interrupted
         */
        void pause(Duration duration) throws InterruptedException;
    }

    private final String baseUrlOverride;
    private final Duration pause;
    private final Pauser pauser;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param pause the pause between requests (property {@value #PAUSE_PROPERTY})
     */
    @Autowired
    public CtDomainWatchAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + PAUSE_PROPERTY + ":" + DEFAULT_PAUSE_TEXT + "}") Duration pause) {
        this(baseUrlOverride, pause, d -> Thread.sleep(d.toMillis()));
    }

    /**
     * Creates the adapter with a chosen pauser (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param pause the pause between requests
     * @param pauser waits; records the pauses in tests
     */
    public CtDomainWatchAdapter(String baseUrlOverride, Duration pause, Pauser pauser) {
        this.baseUrlOverride = baseUrlOverride;
        this.pause = pause == null ? Duration.ofSeconds(30) : pause;
        this.pauser = pauser;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code domains}: 1–25 acceptable host names ({@link DomainNames}). */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> domains = params.has(DOMAINS_PARAM) ? params.stringList(DOMAINS_PARAM) : List.of();
        if (domains == null || domains.isEmpty()) {
            problems.put(DOMAINS_PARAM, "must name at least one domain to watch");
        } else if (domains.size() > MAX_DOMAINS) {
            problems.put(DOMAINS_PARAM, "must name at most " + MAX_DOMAINS + " domains, but names " + domains.size());
        } else {
            for (String d : domains) {
                if (DomainNames.normalize(d).isEmpty()) {
                    problems.put(DOMAINS_PARAM, "must be host names of at least two labels without wildcards, at"
                            + " most 253 characters, but contains '" + d + "'");
                    break;
                }
            }
        }
        return problems;
    }

    /**
     * The normalized watched domains, de-duplicated, in the configured order.
     *
     * @param params the parameters
     * @throws AdapterException when none is set or one is not acceptable
     */
    static List<String> domains(ValidatedParams params) throws AdapterException {
        List<String> raw = params != null && params.has(DOMAINS_PARAM) ? params.stringList(DOMAINS_PARAM) : null;
        if (raw == null || raw.isEmpty()) {
            throw new AdapterException(ID + ": the parameter " + DOMAINS_PARAM + " names no domain");
        }
        Set<String> out = new LinkedHashSet<>();
        for (String d : raw) {
            Optional<String> normalized = DomainNames.normalize(d);
            if (normalized.isEmpty()) {
                throw new AdapterException(ID + ": '" + d + "' is not an acceptable domain");
            }
            out.add(normalized.get());
        }
        if (out.size() > MAX_DOMAINS) {
            throw new AdapterException(ID + ": at most " + MAX_DOMAINS + " domains may be watched");
        }
        return List.copyOf(out);
    }

    /**
     * The URL of one page.
     *
     * @param domain the watched domain
     * @param after the cursor, or null for the first page
     */
    public URI pageUri(String domain, String after) {
        return URI.create(KitUrls.rebased(API_URL + "?domain=" + domain + QUERY
                + (after == null ? "" : "&after=" + KitUrls.encode(after)), baseUrlOverride));
    }

    /**
     * One run (see the class note).
     *
     * @throws AdapterException when the parameters are unusable or the first request fails
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<String> domains = domains(ctx.params());
        List<String> expected = ctx.params() != null && ctx.params().has(EXPECTED_ISSUERS_PARAM)
                ? ctx.params().stringList(EXPECTED_ISSUERS_PARAM) : List.of();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key != null && !key.isBlank()) {
            headers.put("Authorization", "Bearer " + key.strip());
        }
        int start = 0;
        try {
            start = Math.floorMod(Integer.parseInt(ctx.state().get(ROTATION_KEY).orElse("0")), domains.size());
        } catch (NumberFormatException e) {
            start = 0;
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Map<String, String> cursors = new LinkedHashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        int requests = 0;
        int next = start;
        boolean stopped = false;
        for (int n = 0; n < domains.size() && !stopped; n++) {
            int index = (start + n) % domains.size();
            String domain = domains.get(index);
            String after = ctx.state().get(CURSOR_PREFIX + domain).orElse(null);
            boolean finished = false;
            for (int page = 0; page < MAX_PAGES; page++) {
                CertSpotterParser.Page parsed;
                try {
                    if (requests > 0 && !pause.isZero()) {
                        pauser.pause(pause);
                    }
                    requests++;
                    UpstreamResponse response = ctx.http().get(pageUri(domain, after), headers);
                    if (response.status() != 200 || response.truncated()) {
                        throw new UpstreamFormatException(ID + (response.status() != 200 ? ": the API answered HTTP "
                                + response.status() : ": a page exceeds the response cap"), "");
                    }
                    parsed = CertSpotterParser.parse(response.body(), domain, expected);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AdapterException(ID + ": interrupted between requests", e);
                } catch (AdapterException | UpstreamFormatException e) {
                    if (records.isEmpty() && cursors.isEmpty()) {
                        throw e;
                    }
                    ctx.log().warn("{}: stopped at {} ({}); the next run continues there", ctx.sourceId(), domain,
                            e.getMessage());
                    stopped = true;
                    next = index;
                    break;
                }
                parsed.skipped().forEach((reason, c) -> skipped.merge(reason, c, Integer::sum));
                for (NormalizedRecord r : parsed.records()) {
                    if (ids.add(r.externalId())) {
                        records.add(r);
                    }
                }
                if (parsed.count() == 0 || parsed.lastId() == null) {
                    finished = true;
                    break;
                }
                after = parsed.lastId();
                cursors.put(domain, after);
            }
            if (!stopped) {
                next = finished ? (index + 1) % domains.size() : index;
                if (!finished) {
                    // The page cap was reached: the next run starts with this domain again.
                    stopped = true;
                }
            }
        }
        cursors.forEach((domain, after) -> ctx.state().put(CURSOR_PREFIX + domain, after));
        ctx.state().put(ROTATION_KEY, Integer.toString(next));
        ctx.log().debug("{}: {} issuance(s) from {} request(s), skipped {}", ctx.sourceId(), records.size(), requests,
                skipped);
        return FetchResult.of(records);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = CertSpotterParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Certificate Transparency domain watch")
                .description("Certificates issued for the domains you choose (and their subdomains and matching"
                        + " wildcards), from the SSLMate Cert Spotter CT Search API: DNS names, issuer, operator,"
                        + " CAA domains, validity, revocation and fingerprints, and whether the issuer is one you"
                        + " did not expect. No geometry. Unauthenticated use is limited to a few queries an hour for"
                        + " personal or evaluation use; set an API key (and raise the budget) for more. crt.sh is"
                        + " never polled and no CT log is contacted.")
                .kind(kind)
                .homepage("https://sslmate.com/ct_search_api/")
                .attribution("Certificate data from SSLMate Cert Spotter")
                .licence("SSLMate API terms (unauthenticated queries for personal or evaluation use; production use"
                        + " needs an account and API key)")
                .termsUrl("https://sslmate.com/terms")
                .mode(Mode.POLLING)
                .credential(CredentialField.optional(API_KEY, "Cert Spotter API key, sent as Authorization: Bearer;"
                        + " needed for production use"))
                .interval(Duration.ofMinutes(15), Duration.ofHours(1))
                .defaultBudget(Budget.of(2, 48))
                .fetchTimeout(Duration.ofMinutes(30))
                .zeroResultsNormal(true)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("watchedDomain")
                .searchFields("watchedDomain", "dnsNames", "issuerName", "issuerOperator")
                .param(ParamSpec.required(DOMAINS_PARAM, ParamType.STRING_LIST, "Domains to watch (1-25), with"
                        + " their subdomains and matching wildcards; host names, no wildcards"))
                .param(ParamSpec.optional(EXPECTED_ISSUERS_PARAM, ParamType.STRING_LIST, "Issuer friendly names or"
                        + " CAA domains you expect (for example Let's Encrypt, letsencrypt.org); another issuer sets"
                        + " unexpectedIssuer", null));
        text(b, "watchedDomain", "Watched domain", "The watched domain the issuance was found for");
        b.fieldSpec("dnsNames", new FieldSpec(FieldType.ARRAY, null, "DNS names", "The names the certificate is valid"
                + " for, at most 100 (Punycode)", null, Set.of()));
        text(b, "issuerName", "Issuer", "SSLMate's curated issuer name (friendly_name)");
        text(b, "issuerOperator", "Issuer operator", "The organization controlling the issuer's key");
        b.fieldSpec("issuerCaaDomains", new FieldSpec(FieldType.ARRAY, null, "Issuer CAA domains", "Domains that"
                + " authorize this issuer in a CAA record", null, Set.of()));
        b.fieldSpec("notBefore", new FieldSpec(FieldType.TIMESTAMP, null, "Not before", "The certificate's not-before"
                + " time", null, Set.of()));
        b.fieldSpec("notAfter", new FieldSpec(FieldType.TIMESTAMP, null, "Not after", "The certificate's expiry",
                null, Set.of()));
        b.fieldSpec("revoked", new FieldSpec(FieldType.BOOLEAN, null, "Revoked", "True when revoked; absent when"
                + " unknown", null, Set.of()));
        text(b, "certSha256", "Certificate SHA-256", "The certificate (or precertificate) fingerprint");
        text(b, "pubkeySha256", "Public key SHA-256", "The digest of the subject public key info");
        b.fieldSpec("unexpectedIssuer", new FieldSpec(FieldType.BOOLEAN, null, "Unexpected issuer", "True when"
                + " expectedIssuers is set and the issuer matches none of them", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
