package ai.foresignal.media.outlet;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.source.adapters.media.MediaRatingsAdapter;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Imports media ratings from the declared providers (prompt 133) and recomputes the consensus of every outlet it
 * touched.
 *
 * <ul>
 *   <li><strong>Gate:</strong> a provider whose license an admin has not acknowledged is refused with
 *       {@code 409 PROVIDER_LICENCE_NOT_ACKNOWLEDGED}.
 *   <li><strong>{@code wikidata}:</strong> registry outlets not asked within {@value #WIKIDATA_CACHE_DAYS} days (at most
 *       {@value #WIKIDATA_OUTLETS} per run, {@value #WIKIDATA_BATCH} domains per query) are looked up by official
 *       website ({@link WikidataParser}); facts go to the registry, alignment and state media to ratings.
 *   <li><strong>{@code partisan-web}:</strong> the organization files listed by the GitHub contents API are read and
 *       parsed ({@link PartisanWebParser}).
 *   <li><strong>{@code operator}, {@code allsides}, {@code mbfc}:</strong> rows the operator uploads (CSV or JSON,
 *       {@link OperatorRatingsParser}). The live MBFC API is not called: its endpoint could not be verified during
 *       construction, so MBFC ratings are imported from the operator's own export.
 * </ul>
 *
 * <p>Both upstream imports run through {@code ProbeService.probeCall} on the {@code media-ratings} source, so their
 * requests are budgeted and identified. Every rating is stored with its provider's license, attribution and egress
 * ({@code LOCAL_ONLY} for AllSides without a recorded permission and always for MBFC; operator rows by their declared
 * license). On schedule (with {@code foresignal.analytics.scheduled}) Wikidata runs daily and The Partisan Web weekly
 * when the provider and the {@code media-ratings} source are both enabled (so no upstream is contacted until the
 * operator enables the source); an admin's explicit import runs whenever the provider's license is acknowledged.
 */
@Component
public class RatingImportService {

    private static final Logger log = LoggerFactory.getLogger(RatingImportService.class);

    /** How long an outlet's Wikidata answer is kept before it is asked again, days. */
    public static final int WIKIDATA_CACHE_DAYS = 30;

    /** Outlets asked of Wikidata per run. */
    public static final int WIKIDATA_OUTLETS = 200;

    /** Domains per SPARQL query. */
    public static final int WIKIDATA_BATCH = 20;

    /** The production Wikidata Query Service endpoint. */
    public static final String WIKIDATA_ENDPOINT = "https://query.wikidata.org/sparql";

    /** The production listing of The Partisan Web organization files. */
    public static final String PARTISAN_WEB_LISTING =
            "https://api.github.com/repos/stockHuman/tpw-data/contents/organisations";

    /** The base of Wikidata item links. */
    public static final String WIKIDATA_ITEM = "https://www.wikidata.org/wiki/";

    /** Licenses (lower-case fragments) whose operator rows may be served beyond this instance. */
    static final List<String> OPEN_LICENSES = List.of("cc0", "cc-by-4", "cc by 4", "cc-by 4", "public domain",
            "pddl", "odbl", "cc-by-sa", "cc by-sa", "us-gov");

    /**
     * An import body: rows as JSON, or CSV text.
     *
     * @param json the JSON body, or null
     * @param csv the CSV text, or null
     */
    public record Body(JsonNode json, String csv) {

        /** No body. */
        public static final Body NONE = new Body(null, null);

        /** True when neither is given. */
        public boolean empty() {
            return json == null && (csv == null || csv.isBlank());
        }
    }

    private final OutletRegistry registry;
    private final MediaProviders providers;
    private final ObjectProvider<ProbeService> probes;
    private final JsonMapper json;
    private final Clock clock;
    private final String wikidataEndpoint;
    private final String partisanWebListing;
    private final boolean scheduled;
    private ObjectProvider<ai.foresignal.source.SourceService> sources;

    /** Installs the source settings (whether {@code media-ratings} is enabled for the scheduled imports). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setSources(ObjectProvider<ai.foresignal.source.SourceService> sources) {
        this.sources = sources;
    }

    /** Creates the service. */
    public RatingImportService(OutletRegistry registry, MediaProviders providers, ObjectProvider<ProbeService> probes,
            JsonMapper json, Clock clock,
            @Value("${foresignal.test.media.wikidata-endpoint:" + WIKIDATA_ENDPOINT + "}") String wikidataEndpoint,
            @Value("${foresignal.test.media.partisan-web-listing:" + PARTISAN_WEB_LISTING + "}")
            String partisanWebListing,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.registry = registry;
        this.providers = providers;
        this.probes = probes;
        this.json = json;
        this.clock = clock;
        this.wikidataEndpoint = wikidataEndpoint;
        this.partisanWebListing = partisanWebListing;
        this.scheduled = scheduled;
    }

    /** True when the {@code media-ratings} source is enabled: scheduled imports contact upstreams only then. */
    boolean sourceEnabled() {
        ai.foresignal.source.SourceService service = sources == null ? null : sources.getIfAvailable();
        if (service == null) {
            return false;
        }
        try {
            return service.view(MediaRatingsAdapter.ID).enabled();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** The daily Wikidata pass (when the provider and the {@code media-ratings} source are enabled). */
    @Scheduled(cron = "0 40 3 * * *", zone = "UTC")
    public void dailyWikidata() {
        if (scheduled && sourceEnabled() && providers.enabled().contains(MediaVocabulary.WIKIDATA)) {
            runQuietly(MediaVocabulary.WIKIDATA);
        }
    }

    /** The weekly Partisan Web pass (when the provider and the {@code media-ratings} source are enabled). */
    @Scheduled(cron = "0 10 4 * * MON", zone = "UTC")
    public void weeklyPartisanWeb() {
        if (scheduled && sourceEnabled() && providers.enabled().contains(MediaVocabulary.PARTISAN_WEB)) {
            runQuietly(MediaVocabulary.PARTISAN_WEB);
        }
    }

    private void runQuietly(String provider) {
        try {
            importRatings(provider, Body.NONE, "schedule");
        } catch (RuntimeException e) {
            log.warn("Scheduled {} ratings import failed: {}", provider, e.toString());
        }
    }

    /**
     * Runs one import ({@code RatingsImportResult}).
     *
     * @param provider the provider
     * @param body the operator rows (required for {@code operator}, {@code allsides} and {@code mbfc})
     * @param actor who asked
     * @throws ValidationException 400 {@code RATINGS_IMPORT_INVALID}
     * @throws ConflictException 409 {@code PROVIDER_LICENCE_NOT_ACKNOWLEDGED}
     */
    public Map<String, Object> importRatings(String provider, Body body, String actor) {
        if (provider == null || MediaProviders.info(provider) == null) {
            throw invalid("provider", "must be one of " + MediaVocabulary.PROVIDERS);
        }
        MediaProviders.State state = providers.get(provider);
        if (!state.licenseAcknowledged()) {
            throw new ConflictException(ErrorCodes.PROVIDER_LICENCE_NOT_ACKNOWLEDGED, "The license of '" + provider
                    + "' (" + state.info().license() + ") must be acknowledged by an admin before importing.");
        }
        Body in = body == null ? Body.NONE : body;
        Instant now = clock.instant();
        Outcome outcome = new Outcome();
        switch (provider) {
            case MediaVocabulary.WIKIDATA -> wikidata(state, now, outcome);
            case MediaVocabulary.PARTISAN_WEB -> partisanWeb(state, now, outcome);
            default -> {
                if (in.empty()) {
                    throw invalid("body", MediaVocabulary.MBFC.equals(provider)
                            ? "MBFC ratings are imported from rows retrieved with your own key (CSV or JSON); the live"
                            + " API call is not built because its endpoint could not be verified"
                            : "rows are required (CSV or {\"rows\": [...]})");
                }
                OperatorRatingsParser.Parsed parsed = in.json() != null ? OperatorRatingsParser.json(in.json(), provider)
                        : OperatorRatingsParser.csv(in.csv(), provider);
                outcome.rejected += parsed.rejected();
                outcome.errors.addAll(parsed.errors());
                if (parsed.rows().isEmpty()) {
                    throw new ValidationException(ErrorCodes.RATINGS_IMPORT_INVALID, "The import has no usable row.",
                            errorsOf(parsed.errors()));
                }
                store(state, parsed.rows(), now, outcome);
            }
        }
        Set<String> enabled = providers.enabled();
        for (String outletId : outcome.touched) {
            registry.recomputeConsensus(outletId, enabled);
        }
        String status = outcome.failure != null ? (outcome.imported + outcome.updated > 0 ? "partial" : "failed")
                : outcome.rejected > 0 ? "partial" : "ok";
        providers.recordImport(provider, status, outcome.failure, now);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", provider);
        out.put("imported", outcome.imported);
        out.put("updated", outcome.updated);
        out.put("rejected", outcome.rejected);
        out.put("outletsCreated", outcome.created);
        List<Map<String, Object>> errors = new ArrayList<>(outcome.errors);
        if (outcome.failure != null) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("row", null);
            e.put("message", outcome.failure);
            errors.add(0, e);
        }
        out.put("errors", errors.size() > 100 ? errors.subList(0, 100) : errors);
        out.put("importedAt", now.toString());
        return out;
    }

    /** Recomputes every outlet's consensus (after a provider was enabled or disabled). */
    public int recomputeAll() {
        return registry.recomputeAll(providers.enabled());
    }

    // ------------------------------------------------------------------------- providers

    private void wikidata(MediaProviders.State state, Instant now, Outcome outcome) {
        List<OutletRegistry.Outlet> due = registry.dueForWikidata(now.minus(Duration.ofDays(WIKIDATA_CACHE_DAYS)),
                WIKIDATA_OUTLETS);
        if (due.isEmpty()) {
            return;
        }
        Map<String, OutletRegistry.Outlet> byDomain = new LinkedHashMap<>();
        for (OutletRegistry.Outlet o : due) {
            for (String d : o.domains()) {
                byDomain.putIfAbsent(d, o);
            }
        }
        List<String> domains = new ArrayList<>(byDomain.keySet());
        Map<String, WikidataParser.DomainFacts> facts = new LinkedHashMap<>();
        Set<String> asked = new LinkedHashSet<>();
        String failure = upstream(ctx -> {
            for (int i = 0; i < domains.size(); i += WIKIDATA_BATCH) {
                List<String> batch = domains.subList(i, Math.min(domains.size(), i + WIKIDATA_BATCH));
                URI uri = URI.create(wikidataEndpoint + "?format=json&query=" + URLEncoder.encode(
                        WikidataParser.query(batch), StandardCharsets.UTF_8));
                UpstreamResponse response = ctx.http().get(uri, Map.of("Accept", "application/sparql-results+json"));
                if (response.status() != 200 || response.truncated()) {
                    throw new UpstreamFormatException("Wikidata answered HTTP " + response.status(), "");
                }
                facts.putAll(WikidataParser.parse(json.readTree(response.body()), batch, WIKIDATA_ITEM));
                for (String d : batch) {
                    asked.add(byDomain.get(d).id());
                }
            }
        });
        outcome.failure = failure;
        Set<String> applied = new LinkedHashSet<>();
        List<RatingRow> rows = new ArrayList<>();
        for (WikidataParser.DomainFacts f : facts.values()) {
            OutletRegistry.Outlet outlet = byDomain.get(f.domain());
            if (outlet == null || !applied.add(outlet.id())) {
                continue;
            }
            registry.applyWikidataFacts(outlet.id(), f.qid(), f.country(), f.ownerOrg(), f.parentOrg(), f.type());
            rows.addAll(f.ratings());
        }
        store(state, rows, now, outcome);
        registry.markWikidataChecked(asked, now);
    }

    private void partisanWeb(MediaProviders.State state, Instant now, Outcome outcome) {
        List<RatingRow> rows = new ArrayList<>();
        String failure = upstream(ctx -> {
            UpstreamResponse listing = ctx.http().get(URI.create(partisanWebListing),
                    Map.of("Accept", "application/vnd.github+json"));
            if (listing.status() != 200 || listing.truncated()) {
                throw new UpstreamFormatException("GitHub answered HTTP " + listing.status(), "");
            }
            for (JsonNode file : json.readTree(listing.body())) {
                String name = file.path("name").asString("");
                String download = file.path("download_url").asString(null);
                if (!name.endsWith(".md") || name.startsWith("_") || download == null || !allowedDownload(download)) {
                    continue;
                }
                UpstreamResponse md = ctx.http().get(URI.create(download), Map.of("Accept", "text/plain"));
                if (md.status() != 200 || md.truncated()) {
                    continue;
                }
                rows.addAll(PartisanWebParser.parse(new String(md.body(), StandardCharsets.UTF_8),
                        file.path("html_url").asString(null)));
            }
        });
        outcome.failure = failure;
        store(state, rows, now, outcome);
    }

    /** True for The Partisan Web's raw file host, or the configured test listing's host. */
    boolean allowedDownload(String url) {
        try {
            String host = URI.create(url).getHost();
            return "raw.githubusercontent.com".equals(host) || host != null
                    && host.equals(URI.create(partisanWebListing).getHost());
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** A step that talks to an upstream through a probe context. */
    @FunctionalInterface
    interface UpstreamStep {

        /**
         * Runs.
         *
         * @param ctx the probe's fetch context
         * @throws Exception when the upstream fails
         */
        void run(ai.foresignal.source.spi.FetchContext ctx) throws Exception;
    }

    /** Runs {@code step} on the {@code media-ratings} source; returns the failure message, or null. */
    String upstream(UpstreamStep step) {
        ProbeService service = probes.getIfAvailable();
        if (service == null) {
            return "the upstream path is not available";
        }
        ProbeResult result = service.probeCall(MediaRatingsAdapter.ID, (adapter, ctx) -> {
            try {
                step.run(ctx);
            } catch (ai.foresignal.source.spi.AdapterException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new UpstreamFormatException(e.toString(), "");
            }
            return FetchResult.of(List.of());
        });
        return result.parseError();
    }

    // ------------------------------------------------------------------------- storing

    private void store(MediaProviders.State state, List<RatingRow> rows, Instant now, Outcome outcome) {
        String provider = state.info().id();
        for (RatingRow row : rows) {
            OutletRegistry.Outlet outlet = registry.byDomain(row.domain()).orElse(null);
            if (outlet == null) {
                RatingRow.OutletFacts f = row.outlet();
                outlet = registry.register(row.domain(), f == null ? null : f.country(),
                        f == null || f.languages().isEmpty() ? null : f.languages().get(0));
                outcome.created++;
            }
            String license = MediaVocabulary.OPERATOR.equals(provider) ? row.license() : state.info().license();
            String attribution = MediaVocabulary.OPERATOR.equals(provider) ? row.attribution()
                    : state.info().attribution();
            String egress = MediaVocabulary.OPERATOR.equals(provider) ? operatorEgress(row.license()) : state.egress();
            boolean created = registry.upsertRating(new OutletRegistry.Rating(outlet.id(), provider, row.dimension(),
                    row.valueNorm(), row.valueRaw(), row.label(), row.ratedAt(), license, attribution, row.url(),
                    egress, now));
            if (created) {
                outcome.imported++;
            } else {
                outcome.updated++;
            }
            outcome.touched.add(outlet.id());
        }
    }

    /** The egress of an operator row: {@code OPEN} for a recognizable open license, else {@code LOCAL_ONLY}. */
    static String operatorEgress(String license) {
        String l = license == null ? "" : license.toLowerCase(java.util.Locale.ROOT);
        if (l.contains("-nc") || l.contains(" nc") || l.contains("noncommercial") || l.contains("non-commercial")
                || l.contains("-nd") || l.contains("all rights")) {
            return MediaVocabulary.LOCAL_ONLY;
        }
        for (String open : OPEN_LICENSES) {
            if (l.contains(open)) {
                return MediaVocabulary.OPEN;
            }
        }
        return MediaVocabulary.LOCAL_ONLY;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.RATINGS_IMPORT_INVALID, "The ratings import is not valid: " + message
                + ".", List.of(new FieldError(field, message)));
    }

    private static List<FieldError> errorsOf(List<Map<String, Object>> errors) {
        List<FieldError> out = new ArrayList<>();
        for (Map<String, Object> e : errors) {
            out.add(new FieldError(e.get("row") == null ? "body" : "rows[" + e.get("row") + "]",
                    String.valueOf(e.get("message"))));
            if (out.size() >= 20) {
                break;
            }
        }
        if (out.isEmpty()) {
            out.add(new FieldError("body", "no usable row"));
        }
        return out;
    }

    private static final class Outcome {
        int imported;
        int updated;
        int rejected;
        int created;
        String failure;
        final List<Map<String, Object>> errors = new ArrayList<>();
        final Set<String> touched = new LinkedHashSet<>();
    }
}
