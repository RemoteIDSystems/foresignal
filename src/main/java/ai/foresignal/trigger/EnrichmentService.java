package ai.foresignal.trigger;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.ask.CitationSet;
import ai.foresignal.ask.CitationVerifier;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Enriches one alert (prompt 030, Architecture §23, FR-52): gathers its context, asks the chat
 * provider for a short cited summary, verifies the citations, stores the summary and publishes it.
 *
 * <p><strong>Skips</strong> ({@value #SKIPPED_METRIC}{@code {reason}}): {@code rule} (the rule does not
 * have {@code enrich=true}, or is gone), {@code disabled} ({@code llm.enrichmentEnabled} is off),
 * {@code no_provider} (no chat provider is assigned), {@code resolved} (the alert is no longer open),
 * {@code stale} ({@code fired_at} is older than {@link EnrichmentProperties#maxAge()}, 10 minutes:
 * catch-up after downtime), {@code private_source} (the alerted record's source may not be sent to
 * the provider) and {@code gone} (the alert was deleted).
 *
 * <p><strong>The call:</strong> the chat provider, purpose {@code enrichment}, effort {@code low},
 * {@link EnrichmentProperties#maxTokens()} (600) output tokens, no tools, the constant
 * {@link EnrichmentPrompts#SYSTEM}, and a {@link EnrichmentProperties#timeout()} (20 s) limit.
 *
 * <p><strong>Failures</strong> ({@value #FAILED_METRIC}{@code {reason}}): {@code budget},
 * {@code provider}, {@code refusal}, {@code timeout} and {@code empty}. They are logged and never
 * retried; the alert's deliveries keep their scheduled time and go out without a summary.
 *
 * <p><strong>Success:</strong> citations are verified with 028's {@link CitationVerifier}; citations
 * it drops are also removed from the text. {@code alert.summary}, {@code enriched_at} and
 * {@code summary_citations} are written through {@link AlertStore} (bumping {@code rev}), which
 * publishes {@link AlertEvent#ENRICHED}: the streams send {@code alert.enriched} and the notification
 * router brings waiting deliveries forward or edits messages already sent. The enriched alert is then
 * embedded as an {@code alert} search document when an embedding index is available.
 */
@Service
public class EnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentService.class);

    /** Enrichment duration, from dequeue to stored summary. */
    public static final String DURATION_METRIC = "foresignal.enrichment.duration";

    /** Summaries stored. */
    public static final String SUCCEEDED_METRIC = "foresignal.enrichment.succeeded";

    /** Enrichments that failed, tagged {@code reason}. */
    public static final String FAILED_METRIC = "foresignal.enrichment.failed";

    /** Alerts not enriched, tagged {@code reason}. */
    public static final String SKIPPED_METRIC = "foresignal.enrichment.skipped";

    /** A citation-shaped token, as {@link CitationVerifier} reads them. */
    static final Pattern CITATION = Pattern.compile("\\[([A-Za-z0-9][A-Za-z0-9._-]*):([^\\[\\]\\s]+)](?!\\()");

    private static final Pattern SEQ_SUFFIX = Pattern.compile("^(.+)@(\\d{1,18})$");

    static final String STORE_SQL = "UPDATE alert SET summary = :summary, enriched_at = :at,"
            + " summary_citations = CAST(:citations AS jsonb), rev = nextval('alert_rev')"
            + " WHERE id = :id RETURNING id";

    private final AlertStore alerts;
    private final TriggerRuleRepository rules;
    private final LlmService llm;
    private final LlmSettingsService llmSettings;
    private final EgressPolicy egress;
    private final EnrichmentContextBuilder contexts;
    private final CitationVerifier verifier;
    private final ObjectProvider<EmbeddingWorker> embeddings;
    private final SearchStatusService searchStatus;
    private final EnrichmentProperties properties;
    private final MeterRegistry meters;
    private final JsonMapper json;
    private final Clock clock;
    private final Timer duration;
    private final Counter succeeded;
    private final ExecutorService calls = Executors.newVirtualThreadPerTaskExecutor();
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the service. */
    public EnrichmentService(AlertStore alerts, TriggerRuleRepository rules, LlmService llm,
            LlmSettingsService llmSettings, EgressPolicy egress, EnrichmentContextBuilder contexts,
            CitationVerifier verifier, ObjectProvider<EmbeddingWorker> embeddings, SearchStatusService searchStatus,
            EnrichmentProperties properties, MeterRegistry meters, JsonMapper json, Clock clock) {
        this.alerts = alerts;
        this.rules = rules;
        this.llm = llm;
        this.llmSettings = llmSettings;
        this.egress = egress;
        this.contexts = contexts;
        this.verifier = verifier;
        this.embeddings = embeddings;
        this.searchStatus = searchStatus;
        this.properties = properties;
        this.meters = meters;
        this.json = json;
        this.clock = clock;
        this.duration = Timer.builder(DURATION_METRIC).register(meters);
        this.succeeded = Counter.builder(SUCCEEDED_METRIC).register(meters);
    }

    /** Stops the threads of in-flight LLM calls. */
    @PreDestroy
    public void close() {
        calls.shutdownNow();
    }

    /**
     * What happened to one alert.
     *
     * @param status {@code succeeded}, {@code skipped} or {@code failed}
     * @param reason the skip or failure reason, or null on success
     * @param alert the alert after enrichment (with its summary), or null when not stored
     * @param context the context sent to the model, or null when no call was made
     */
    public record Outcome(String status, String reason, AlertView alert, EnrichmentContext context) {

        /** A summary was stored. */
        public static final String SUCCEEDED = "succeeded";

        /** The alert was not enriched. */
        public static final String SKIPPED = "skipped";

        /** The enrichment failed. */
        public static final String FAILED = "failed";
    }

    /**
     * Admission before queueing: the checks that need no database read of the alert. Counts a skip
     * when it refuses.
     *
     * @return true when {@code alert} should be queued for {@link #run}
     */
    public boolean admit(AlertView alert) {
        Optional<String> reason = admission(alert);
        reason.ifPresent(this::skip);
        return reason.isEmpty();
    }

    /** Runs every check and, when they pass, the enrichment (synchronously). */
    public Outcome enrich(AlertView alert) {
        Optional<String> reason = admission(alert);
        if (reason.isPresent()) {
            skip(reason.get());
            return new Outcome(Outcome.SKIPPED, reason.get(), null, null);
        }
        return run(alert);
    }

    /**
     * Enriches an admitted alert: re-reads it, checks that it is still open and recent, then builds the
     * context, calls the model and stores the summary. Never throws for a model failure.
     */
    public Outcome run(AlertView admitted) {
        Instant started = clock.instant();
        Timer.Sample sample = Timer.start(meters);
        try {
            return doRun(admitted);
        } finally {
            sample.stop(duration);
            log.debug("Enrichment of alert {} took {} ms", admitted.id(),
                    Duration.between(started, clock.instant()).toMillis());
        }
    }

    private Outcome doRun(AlertView admitted) {
        Optional<AlertView> current = alerts.find(admitted.id());
        if (current.isEmpty()) {
            return skipped("gone");
        }
        AlertView alert = current.get();
        if (!alert.open()) {
            return skipped("resolved");
        }
        Instant now = clock.instant();
        if (alert.firedAt() != null && alert.firedAt().isBefore(now.minus(properties.maxAge()))) {
            return skipped("stale");
        }
        Optional<String> admission = admission(alert);
        if (admission.isPresent()) {
            return skipped(admission.get());
        }
        TriggerRuleEntity rule = rules.findById(alert.ruleId()).orElse(null);
        if (rule == null) {
            return skipped("rule");
        }
        // R2-011: a user's alert is enriched on their behalf: by their personal chat provider when they have one, and
        // metered and capped as theirs.
        String actor = alert.ownerPrincipalId();
        String providerId = llm.chatProviderFor(actor, llmSettings.get());
        LlmProviderConfig config;
        try {
            config = llm.config(providerId);
        } catch (RuntimeException e) {
            return failed(alert, "provider", "the chat provider " + providerId + " is not available: " + e.getMessage(),
                    null);
        }
        EgressPolicy.LlmDestination destination = config.destination();
        if (!egress.allowsLlmContext(alert.sourceId(), destination)) {
            return skipped("private_source");
        }

        EnrichmentContext context = contexts.build(alert, rule.getEnrichRadiusKm(),
                Duration.ofSeconds(rule.getEnrichWindowSeconds()), destination);
        LlmClient client = llm.client(providerId, actor);
        ChatRequest request = new ChatRequest(EnrichmentPrompts.SYSTEM,
                List.of(new ChatMessage.User(EnrichmentPrompts.userTurn(context.anchor(), context.json()))), List.of(),
                properties.maxTokens(), Effort.LOW, Purpose.ENRICHMENT, false, null);
        ChatResponse response;
        Future<ChatResponse> call = calls.submit(() -> client.chat(request));
        try {
            response = call.get(properties.timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            call.cancel(true);
            return failed(alert, "timeout", "the LLM call took longer than " + properties.timeout(), context);
        } catch (InterruptedException e) {
            call.cancel(true);
            Thread.currentThread().interrupt();
            return failed(alert, "timeout", "interrupted while waiting for the LLM call", context);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            String reason = cause instanceof LlmBudgetExceededException ? "budget" : "provider";
            return failed(alert, reason, cause.getMessage(), context);
        }
        if (response.stopReason() == StopReason.REFUSAL) {
            return failed(alert, "refusal", "the model refused"
                    + (response.refusalCategory() == null ? "" : " (" + response.refusalCategory() + ")"), context);
        }
        String content = response.content() == null ? "" : response.content().strip();
        if (content.isEmpty()) {
            return failed(alert, "empty", "the model returned no text", context);
        }
        CitationVerifier.Verification verification = verifier.verify(content, context.citable());
        String summary = stripUnverified(content, context.citable()).strip();
        if (summary.isEmpty()) {
            return failed(alert, "empty", "the model returned only unverifiable citations", context);
        }
        List<Map<String, Object>> citations = new ArrayList<>();
        for (AskCitation citation : verification.citations()) {
            citations.add(citation.toMap());
        }
        Optional<AlertView> stored = store(alert.id(), summary, citations, clock.instant());
        if (stored.isEmpty()) {
            return skipped("gone");
        }
        succeeded.increment();
        embed(stored.get(), context.anchor());
        if (!verification.warnings().isEmpty()) {
            log.debug("Enrichment of alert {}: {}", alert.id(), verification.warnings());
        }
        return new Outcome(Outcome.SUCCEEDED, null, stored.get(), context);
    }

    /** The admission checks: the rule's flag, the setting and the provider. */
    Optional<String> admission(AlertView alert) {
        if (alert.ruleId() == null) {
            return Optional.of("rule");
        }
        boolean enrich = rules.findById(alert.ruleId()).map(TriggerRuleEntity::isEnrich).orElse(false);
        if (!enrich) {
            return Optional.of("rule");
        }
        LlmSettings settings = llmSettings.get();
        if (!settings.enrichmentEnabled()) {
            return Optional.of("disabled");
        }
        if (llm.chatProviderFor(alert.ownerPrincipalId(), settings) == null) {
            return Optional.of("no_provider");
        }
        return Optional.empty();
    }

    /** Writes the summary through the alert store, publishing {@link AlertEvent#ENRICHED}. */
    Optional<AlertView> store(long alertId, String summary, List<Map<String, Object>> citations, Instant at) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", alertId)
                .addValue("summary", summary)
                .addValue("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .addValue("citations", json.writeValueAsString(citations));
        return alerts.write(writer -> writer.update(AlertEvent.ENRICHED, STORE_SQL, params));
    }

    /** Embeds the enriched alert as an {@code alert} document when an embedding index is available. */
    void embed(AlertView alert, EnrichmentAnchor anchor) {
        EmbeddingWorker worker = embeddings.getIfAvailable();
        if (worker == null) {
            return;
        }
        SearchStatusService.Availability availability = searchStatus.availability();
        if (!availability.available() && (availability.state() == null || availability.state().pending() == null)) {
            return;
        }
        Geometry geometry = null;
        if (anchor.geometryJson() != null) {
            try {
                geometry = geoJson.read(anchor.geometryJson());
            } catch (RuntimeException e) {
                geometry = null;
            }
        }
        String text = (alert.message() == null ? "" : alert.message()) + "\n\n" + alert.summary();
        try {
            worker.embedDocument(DocInput.ALERT, Long.toString(alert.id()), text.strip(), geometry, anchor.time(),
                    alert.sourceId(), alert.kind());
        } catch (RuntimeException e) {
            log.warn("Enriched alert {} could not be embedded: {}", alert.id(), e.getMessage());
        }
    }

    /** {@code content} without the citation tokens that {@code citable} cannot back. */
    static String stripUnverified(String content, CitationSet citable) {
        Matcher m = CITATION.matcher(content);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            boolean known = known(m.group(1), m.group(2), citable);
            m.appendReplacement(out, known ? Matcher.quoteReplacement(m.group()) : "");
        }
        m.appendTail(out);
        return out.toString().replaceAll("[ \\t]{2,}", " ").replaceAll(" +([.,;:])", "$1");
    }

    private static boolean known(String prefix, String rest, CitationSet citable) {
        Matcher seq = SEQ_SUFFIX.matcher(rest);
        if (AskCitation.ALERT.equalsIgnoreCase(prefix)) {
            return citable.hasAlert(seq.matches() ? seq.group(1) : rest);
        }
        if (AskCitation.BRIEFING.equalsIgnoreCase(prefix)) {
            return citable.hasBriefing(seq.matches() ? seq.group(1) : rest);
        }
        if (seq.matches()) {
            return citable.hasRecord(prefix + ":" + seq.group(1), Long.parseLong(seq.group(2)));
        }
        return citable.latestSeq(prefix + ":" + rest).isPresent();
    }

    private Outcome skipped(String reason) {
        skip(reason);
        return new Outcome(Outcome.SKIPPED, reason, null, null);
    }

    private void skip(String reason) {
        Counter.builder(SKIPPED_METRIC).tag("reason", reason).register(meters).increment();
    }

    private Outcome failed(AlertView alert, String reason, String message, EnrichmentContext context) {
        Counter.builder(FAILED_METRIC).tag("reason", reason).register(meters).increment();
        log.warn("Enrichment of alert {} failed ({}): {}; its deliveries go out without a summary", alert.id(), reason,
                Objects.toString(message, ""));
        return new Outcome(Outcome.FAILED, reason, null, context);
    }
}
