package ai.foresignal.briefing;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.ask.AskCitation;
import ai.foresignal.ask.CitationSet;
import ai.foresignal.ask.CitationVerifier;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Writes the narrative of an LLM-mode briefing (prompt 029, Architecture §22).
 *
 * <p>The context is the digest statistics, the notable records in short form and, when semantic
 * search is available and the briefing has instructions, a scope-filtered hybrid search on the
 * instructions (top {@value #SEARCH_LIMIT}). Every record passes {@link EgressPolicy} for the chat
 * provider first: a private source's records, counts and alerts never reach a non-local provider, and
 * sources hidden from the briefing's owner never reach any. The call uses the chat provider with
 * purpose {@code briefing}, effort {@code medium} and the constant {@link BriefingPrompts#SYSTEM}.
 * The answer is verified with 028's {@link CitationVerifier}; its warnings are kept.
 *
 * <p>Any failure (no provider, budget, provider error, refusal, empty answer) is a {@link Fallback},
 * and the caller renders the digest instead.
 */
@Component
public class LlmBriefingWriter {

    /** Search hits added to the context. */
    public static final int SEARCH_LIMIT = 20;

    /** The longest search query taken from the instructions. */
    static final int MAX_QUERY = 500;

    /** The output cap of one briefing. */
    static final int MAX_OUTPUT_TOKENS = 8000;

    private final LlmService llm;
    private final LlmSettingsService settings;
    private final EgressPolicy egress;
    private final SearchService search;
    private final SearchStatusService searchStatus;
    private final RecordQueryService records;
    private final RecordMapper recordMapper;
    private final McpOutputShaper shaper;
    private final CitationVerifier verifier;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the writer. */
    public LlmBriefingWriter(LlmService llm, LlmSettingsService settings, EgressPolicy egress, SearchService search,
            SearchStatusService searchStatus, RecordQueryService records, RecordMapper recordMapper,
            McpOutputShaper shaper, CitationVerifier verifier, JsonMapper json, Clock clock) {
        this.llm = llm;
        this.settings = settings;
        this.egress = egress;
        this.search = search;
        this.searchStatus = searchStatus;
        this.records = records;
        this.recordMapper = recordMapper;
        this.shaper = shaper;
        this.verifier = verifier;
        this.json = json;
        this.clock = clock;
    }

    /**
     * What to write about.
     *
     * @param name the briefing name
     * @param instructions the operator's instructions, or null
     * @param stats the digest statistics
     * @param scope the resolved scope (for the search)
     * @param hiddenSources sources the briefing may not list
     * @param zone the briefing's timezone
     * @param periodFrom the period start
     * @param periodTo the period end
     * @param restriction the read restriction (prompt 035)
     * @param language the edition's language (prompt NP-003), or null for the default
     */
    public record Request(String name, String instructions, DigestStats stats, ScopeResolver.Resolved scope,
            Set<String> hiddenSources, ZoneId zone, Instant periodFrom, Instant periodTo,
            ai.foresignal.security.restrict.ReadRestriction restriction, String language) {

        /** A null restriction becomes none. */
        public Request {
            restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
        }

        /** A request in the default language (the shape before prompt NP-003). */
        public Request(String name, String instructions, DigestStats stats, ScopeResolver.Resolved scope,
                Set<String> hiddenSources, ZoneId zone, Instant periodFrom, Instant periodTo,
                ai.foresignal.security.restrict.ReadRestriction restriction) {
            this(name, instructions, stats, scope, hiddenSources, zone, periodFrom, periodTo, restriction, null);
        }

        /** An unrestricted request (the shape before prompt 035). */
        public Request(String name, String instructions, DigestStats stats, ScopeResolver.Resolved scope,
                Set<String> hiddenSources, ZoneId zone, Instant periodFrom, Instant periodTo) {
            this(name, instructions, stats, scope, hiddenSources, zone, periodFrom, periodTo, null, null);
        }
    }

    /**
     * A verified narrative.
     *
     * @param markdown the narrative
     * @param citations the verified citations
     * @param warnings the verifier's warnings and notes
     * @param usage provider, model, tokens and duration
     */
    public record Result(String markdown, List<AskCitation> citations, List<String> warnings,
            Map<String, Object> usage) {}

    /** Why LLM mode fell back to the digest; the message is stored as the run's error. */
    public static final class Fallback extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final transient Map<String, Object> usage;

        /** A fallback with {@code message}. */
        public Fallback(String message, Map<String, Object> usage, Throwable cause) {
            super(message, cause);
            this.usage = usage;
        }

        /** The usage of the failed call, or null. */
        public Map<String, Object> usage() {
            return usage;
        }
    }

    /**
     * Writes the narrative.
     *
     * @throws Fallback when there is no provider or the model fails, refuses or answers nothing
     */
    public Result write(Request request) {
        String providerId = settings.get().chatProvider();
        if (providerId == null) {
            throw new Fallback("no chat provider is configured; the digest was rendered instead", null, null);
        }
        Instant started = clock.instant();
        LlmProviderConfig config;
        try {
            config = llm.config(providerId);
        } catch (RuntimeException e) {
            throw new Fallback("the chat provider " + providerId + " is not available: " + e.getMessage(), null, e);
        }
        EgressPolicy.LlmDestination destination = config.destination();
        Set<String> excluded = new TreeSet<>(request.hiddenSources());
        excluded.addAll(egress.llmExcludedSources(destination));
        Predicate<String> allowed = source -> source != null && !excluded.contains(source)
                && egress.allowsLlmContext(source, destination);

        List<String> warnings = new ArrayList<>();
        CitationSet citable = new CitationSet();
        ObjectNode data = context(request, allowed, citable);
        ArrayNode hits = searchHits(request, excluded, allowed, citable, warnings);
        if (hits != null) {
            data.set("search", hits);
        }
        String dataText = json.writeValueAsString(data);
        citable.addText(dataText);

        LlmClient client = llm.client(providerId);
        int maxTokens = Math.max(1, Math.min(config.effectiveMaxTokens(), MAX_OUTPUT_TOKENS));
        ChatRequest chat = new ChatRequest(BriefingPrompts.SYSTEM,
                List.of(new ChatMessage.User(BriefingPrompts.userTurn(request.name(), request.instructions(),
                        request.periodFrom(), request.periodTo(), request.zone(), dataText, request.language()))),
                List.of(), maxTokens, Effort.MEDIUM, Purpose.BRIEFING, false, null);
        ChatResponse response;
        try {
            response = client.chat(chat);
        } catch (RuntimeException e) {
            throw new Fallback("the LLM call failed: " + e.getMessage(), usage(providerId, null, started), e);
        }
        Map<String, Object> usage = usage(providerId, response, started);
        if (response.stopReason() == StopReason.REFUSAL) {
            throw new Fallback("the model refused to write the briefing"
                    + (response.refusalCategory() == null ? "" : " (" + response.refusalCategory() + ")"), usage, null);
        }
        String content = response.content() == null ? "" : response.content().strip();
        if (content.isEmpty()) {
            throw new Fallback("the model returned no text", usage, null);
        }
        if (response.stopReason() == StopReason.MAX_TOKENS) {
            warnings.add("the narrative was cut off at the output limit of " + maxTokens + " tokens");
        }
        CitationVerifier.Verification verification = verifier.verify(content, citable);
        warnings.addAll(verification.warnings());
        return new Result(content + "\n", verification.citations(), List.copyOf(warnings), usage);
    }

    /** The statistics and notable records the model may see, recording what it may cite. */
    ObjectNode context(Request request, Predicate<String> allowed, CitationSet citable) {
        DigestStats stats = request.stats();
        ObjectNode data = json.createObjectNode();
        ObjectNode period = data.putObject("period");
        period.put("from", request.periodFrom().toString());
        period.put("to", request.periodTo().toString());
        period.put("timezone", request.zone().getId());
        data.put("gap", stats.gap());
        if (stats.gapReason() != null) {
            data.put("gapReason", stats.gapReason());
        }
        ArrayNode counts = data.putArray("counts");
        for (DigestStats.Count c : stats.counts()) {
            if (allowed.test(c.sourceId())) {
                counts.add(shaper.objectTree(c.toMap()));
            }
        }
        ArrayNode active = data.putArray("active");
        for (DigestStats.Active a : stats.active()) {
            if (allowed.test(a.sourceId())) {
                active.add(shaper.objectTree(a.toMap()));
            }
        }
        ObjectNode alerts = data.putObject("alerts");
        Map<String, Object> alertMap = stats.alerts().toMap();
        alerts.set("bySeverity", shaper.tree(alertMap.get("bySeverity")));
        alerts.set("byStatus", shaper.tree(alertMap.get("byStatus")));
        alerts.set("byRule", shaper.tree(alertMap.get("byRule")));
        ArrayNode alertItems = alerts.putArray("items");
        for (DigestStats.AlertItem item : stats.alerts().items()) {
            var a = item.view();
            if (!allowed.test(a.sourceId())) {
                continue;
            }
            ObjectNode node = alertItems.addObject();
            node.put("cite", "[alert:" + a.id() + "]");
            node.put("id", a.id());
            node.put("rule", a.ruleName());
            node.put("severity", a.severity());
            node.put("state", a.state());
            node.put("message", a.message());
            node.put("record", a.sourceId() + ":" + a.externalId());
            node.put("firedAt", a.firedAt() == null ? null : a.firedAt().toString());
            citable.addAlert(Long.toString(a.id()), (a.ruleName() == null ? "Alert" : a.ruleName()) + ": "
                    + (a.message() == null ? "" : a.message()));
        }
        ArrayNode notable = data.putArray("notable");
        List<DigestStats.NotableItem> items = stats.notableItems().stream().filter(i -> allowed.test(i.sourceId()))
                .toList();
        Map<String, RecordRow> rows = items.isEmpty() ? Map.of()
                : records.currentByKeys(items.stream().map(i -> Map.entry(i.sourceId(), i.externalId())).toList(),
                        Set.of());
        for (DigestStats.NotableItem item : items) {
            ObjectNode node = notable.addObject();
            node.put("cite", "[" + item.id() + "@" + item.seq() + "]");
            node.put("id", item.id());
            node.put("seq", item.seq());
            node.put("kind", item.kind());
            node.put("title", item.title());
            if (item.rankField() != null && item.rankValue() != null) {
                node.put(item.rankField(), item.rankValue());
            }
            node.put("effectiveTime", item.effectiveTime() == null ? null : item.effectiveTime().toString());
            node.put("live", item.live());
            RecordRow row = rows.get(item.id());
            if (row != null) {
                node.set("record", shaper.record(recordMapper.record(row, FieldSelection.ALL, RecordMapper.Shape.CURRENT),
                        McpOutputShaper.Detail.SHORT));
            }
            citable.addRecord(item.id(), item.seq(), item.title() == null ? item.externalId() : item.title());
        }
        if (stats.health() != null) {
            ArrayNode health = data.putArray("health");
            for (DigestStats.HealthItem h : stats.health()) {
                if (allowed.test(h.sourceId())) {
                    health.add(shaper.objectTree(h.toMap()));
                }
            }
        }
        if (!stats.sections().isEmpty()) {
            ObjectNode sections = data.putObject("sections");
            for (BriefingSection section : stats.sections()) {
                sections.set(section.key(), shaper.tree(section.llmContext() != null ? section.llmContext()
                        : section.data()));
            }
        }
        return data;
    }

    /** The scope-filtered hybrid search on the instructions, or null when it does not apply. */
    private ArrayNode searchHits(Request request, Set<String> excluded, Predicate<String> allowed, CitationSet citable,
            List<String> warnings) {
        String instructions = request.instructions();
        if (instructions == null || instructions.isBlank()) {
            return null;
        }
        if (!searchStatus.availability().available()) {
            return null;
        }
        String q = instructions.length() > MAX_QUERY ? instructions.substring(0, MAX_QUERY) : instructions;
        ScopeResolver.Resolved scope = request.scope();
        Map<String, Object> results;
        try {
            results = search.search(new SearchRequest(q, SearchRequest.Mode.HYBRID, Set.of(DocInput.RECORD),
                    scope.sources(), scope.kinds(), scope.bbox(), request.periodFrom(), request.periodTo(), false,
                    SEARCH_LIMIT), excluded, request.restriction());
        } catch (RuntimeException e) {
            warnings.add("the search on the instructions was skipped: " + e.getMessage());
            return null;
        }
        ArrayNode out = json.createArrayNode();
        Object rawItems = results.get("items");
        if (!(rawItems instanceof List<?> list)) {
            return out;
        }
        for (Object raw : list) {
            if (!(raw instanceof Map<?, ?> item)) {
                continue;
            }
            Object source = item.get("source");
            Object docId = item.get("docId");
            Object seq = item.get("seq");
            if (source == null || docId == null || !allowed.test(source.toString())) {
                continue;
            }
            ObjectNode node = out.addObject();
            if (seq instanceof Number n) {
                node.put("cite", "[" + docId + "@" + n.longValue() + "]");
                citable.addRecord(docId.toString(), n.longValue(),
                        item.get("title") == null ? docId.toString() : item.get("title").toString());
            }
            for (String key : List.of("docId", "seq", "title", "snippet", "kind", "observedAt", "effectiveTime")) {
                Object value = item.get(key);
                if (value != null) {
                    node.set(key, shaper.tree(value));
                }
            }
            shaper.searchHitNotice(item, node);
        }
        return out;
    }

    private Map<String, Object> usage(String providerId, ChatResponse response, Instant started) {
        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("provider", providerId);
        usage.put("model", response == null ? null : response.model());
        TokenUsage tokens = response == null ? TokenUsage.ZERO : response.usage();
        usage.put("inputTokens", tokens.inputTokens());
        usage.put("outputTokens", tokens.outputTokens());
        usage.put("cachedTokens", tokens.cachedTokens());
        usage.put("durationMs", Duration.between(started, clock.instant()).toMillis());
        return usage;
    }
}
