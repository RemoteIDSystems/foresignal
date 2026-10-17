package ai.foresignal.situation;

import ai.foresignal.analyst.AnalystCitations;
import ai.foresignal.analyst.AnalystClaim;
import ai.foresignal.analyst.AnalystOutputTools;
import ai.foresignal.analyst.AnalystOutputValidator;
import ai.foresignal.analyst.AnalystRunner;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.ask.AskPrompts;
import ai.foresignal.ask.CitableExtractor;
import ai.foresignal.ask.ToolOutputFilter;
import ai.foresignal.intel.EventTypes;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.ToolLoop;
import ai.foresignal.llm.client.ToolSpec;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolCatalog;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;

/**
 * The optional, self-refreshing situation brief (prompt 118 §5). When a chat provider is configured and
 * {@code situation.briefs.enabled} is true (default false), a brief is regenerated when the situation becomes active,
 * when its severity changes, or when ≥ {@value #CORE_GROWTH} new core members arrived since the last brief,
 * debounced to at most one per {@code situation.briefMinInterval} (30 min) per situation and capped at
 * {@code situation.briefs.maxPerDay} (50) attempts per UTC day across all situations.
 *
 * <p>It runs the 028 {@link ToolLoop} with the read catalog and the {@code record_note} output tool, the situation's
 * members as the input packet, effort {@code low}, and validates the output with the 102
 * {@link AnalystOutputValidator} (typed claims; citations must name a packet item or a tool result). Egress follows
 * the provider's destination exactly as the analyst's does: a private situation is never sent to a destination that
 * may not receive private records, and members of excluded sources are left out of the packet. The brief is
 * labeled speculation-capable narrative: it is stored in {@code brief} with the model and time, the previous one
 * goes to {@code situation_log}, and it never changes members, status or severity. A budget refusal or a failure
 * leaves the previous brief in place and logs a warning. It never blocks linking: generation runs on its own
 * virtual thread.
 */
@Service
public class SituationBriefService {

    private static final Logger log = LoggerFactory.getLogger(SituationBriefService.class);

    /** The component name of brief manifests. */
    public static final String COMPONENT = "situation-brief";

    /** The brief version. */
    public static final String VERSION = "1.0.0";

    /** New core members that trigger a refresh. */
    public static final int CORE_GROWTH = 3;

    /** Members sent in the input packet at most (most recent first). */
    static final int PACKET_MEMBERS = 50;

    /** The tool loop limits of a brief. */
    static final ToolLoop.Limits LIMITS = new ToolLoop.Limits(6, Duration.ofSeconds(90), 30_000);

    /** The transport tag of brief tool calls. */
    static final String TRANSPORT = "situation-brief";

    /** What one generation attempt did. */
    public enum Outcome {
        /** A brief was stored. */
        STORED,
        /** Briefs are off or no chat provider is configured. */
        DISABLED,
        /** Within {@code briefMinInterval} of the previous brief; still pending. */
        DEBOUNCED,
        /** The daily cap was reached. */
        CAPPED,
        /** The budget refused, the model failed or refused, or egress forbade it; the previous brief stays. */
        FAILED,
        /** The situation does not exist. */
        MISSING
    }

    private final SituationService service;
    private final SituationRepository situations;
    private final SituationLogRepository logs;
    private final SituationSettings settings;
    private final ObjectProvider<LlmService> llm;
    private final ObjectProvider<ToolCatalog> catalog;
    private final ObjectProvider<ToolOutputFilter> outputFilter;
    private final List<CitableExtractor> extractors;
    private final ObjectProvider<EgressPolicy> egress;
    private final ObjectProvider<ManifestService> manifests;
    private final JsonMapper json;
    private final Clock clock;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    /** Creates the service. */
    public SituationBriefService(SituationService service, SituationRepository situations, SituationLogRepository logs,
            SituationSettings settings, ObjectProvider<LlmService> llm, ObjectProvider<ToolCatalog> catalog,
            ObjectProvider<ToolOutputFilter> outputFilter, List<CitableExtractor> extractors,
            ObjectProvider<EgressPolicy> egress, ObjectProvider<ManifestService> manifests, JsonMapper json,
            Clock clock) {
        this.service = service;
        this.situations = situations;
        this.logs = logs;
        this.settings = settings;
        this.llm = llm;
        this.catalog = catalog;
        this.outputFilter = outputFilter;
        this.extractors = List.copyOf(extractors);
        this.egress = egress;
        this.manifests = manifests;
        this.json = json;
        this.clock = clock;
    }

    /** True when briefs are enabled and a chat provider is configured. */
    public boolean available() {
        LlmService service = llm.getIfAvailable();
        return settings.briefsEnabled() && service != null && service.chatConfigured();
    }

    /**
     * Considers a refresh after a change (called after commit by {@link SituationService}): when a trigger holds,
     * the brief is marked pending and an attempt starts on a virtual thread.
     *
     * @param activated the situation became active
     * @param severityChanged its severity changed
     * @param coreGrowth core members added since the last brief
     */
    public void consider(String situationId, boolean activated, boolean severityChanged, int coreGrowth) {
        if (!(activated || severityChanged || coreGrowth >= CORE_GROWTH) || !available()) {
            return;
        }
        try {
            service.markBriefPending(situationId, true);
        } catch (RuntimeException e) {
            log.debug("Could not mark the brief of {} pending: {}", situationId, e.toString());
            return;
        }
        async(situationId, false);
    }

    /**
     * {@code POST /v1/situations/{id}/brief}: queues an immediate regeneration (not debounced).
     *
     * @return the {@code SituationBriefStatus}
     * @throws ConflictException {@code LLM_NOT_CONFIGURED} when briefs are off or no chat provider is configured
     * @throws LlmBudgetExceededException when today's brief cap is reached
     */
    public Map<String, Object> request(String situationId, boolean privateAccess) {
        SituationEntity situation = service.visible(situationId, privateAccess);
        if (!available()) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                    "Situation briefs need a chat provider and situation.briefs.enabled=true.");
        }
        if (cappedToday()) {
            throw new LlmBudgetExceededException("situation-brief", "The daily situation brief cap ("
                    + settings.briefsMaxPerDay() + ") is reached.", untilTomorrow());
        }
        service.markBriefPending(situationId, true);
        boolean running = inFlight.contains(situationId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("situationId", situationId);
        out.put("status", running ? "running" : "queued");
        out.put("requestedAt", clock.instant().toString());
        out.put("previousBriefGeneratedAt", situation.getBriefGeneratedAt() == null ? null
                : situation.getBriefGeneratedAt().toString());
        async(situationId, true);
        return out;
    }

    /** Retries every pending brief (the scheduler's tick); debounced ones wait for their interval. */
    public int retryPending() {
        if (!available()) {
            return 0;
        }
        int attempted = 0;
        for (SituationEntity situation : situations.findByBriefPendingTrueOrderByIdAsc()) {
            if (generate(situation.getId(), false) != Outcome.DEBOUNCED) {
                attempted++;
            }
        }
        return attempted;
    }

    private void async(String situationId, boolean force) {
        if (!inFlight.add(situationId)) {
            return;
        }
        Thread.ofVirtual().name("situation-brief-" + situationId).start(() -> {
            try {
                generate(situationId, force);
            } catch (RuntimeException e) {
                log.warn("Situation brief of {} failed: {}", situationId, e.toString());
            } finally {
                inFlight.remove(situationId);
            }
        });
    }

    private boolean cappedToday() {
        Instant dayStart = clock.instant().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return logs.countByActionAndAtGreaterThanEqual("brief", dayStart) >= settings.briefsMaxPerDay();
    }

    private Duration untilTomorrow() {
        Instant now = clock.instant();
        Instant tomorrow = now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return Duration.between(now, tomorrow);
    }

    /**
     * Generates the brief of one situation now, synchronously.
     *
     * @param force true to skip the debounce (an explicit request)
     * @return what happened
     */
    public Outcome generate(String situationId, boolean force) {
        if (!available()) {
            return Outcome.DISABLED;
        }
        Optional<SituationEntity> found = situations.findById(situationId);
        if (found.isEmpty()) {
            return Outcome.MISSING;
        }
        SituationEntity situation = found.get();
        Instant now = clock.instant();
        if (!force && situation.getBriefGeneratedAt() != null
                && now.isBefore(situation.getBriefGeneratedAt().plus(settings.briefMinInterval()))) {
            service.markBriefPending(situationId, true);
            return Outcome.DEBOUNCED;
        }
        if (cappedToday()) {
            log.warn("Situation brief of {} skipped: the daily cap of {} briefs is reached", situationId,
                    settings.briefsMaxPerDay());
            service.markBriefPending(situationId, false);
            return Outcome.CAPPED;
        }
        LlmService llmService = llm.getIfAvailable();
        try {
            LlmClient client = llmService.chatClient().orElse(null);
            if (client == null) {
                return Outcome.DISABLED;
            }
            EgressPolicy policy = egress.getIfAvailable();
            EgressPolicy.LlmDestination destination = llmService.config(client.providerId()).destination();
            Set<String> excluded = policy == null ? Set.of() : policy.llmExcludedSources(destination);
            boolean privateAllowed = policy != null && policy.privateAllowedFor(destination);
            if (situation.isPrivateSituation() && !privateAllowed) {
                service.briefFailed(situationId, "the situation is private and the provider may not receive private"
                        + " records", false);
                log.warn("Situation brief of {} skipped: private situation, remote provider", situationId);
                return Outcome.FAILED;
            }
            return run(situation, client, policy, destination, excluded, privateAllowed);
        } catch (LlmBudgetExceededException e) {
            log.warn("Situation brief of {} refused by the LLM budget: {}; the previous brief stays", situationId,
                    e.getMessage());
            service.briefFailed(situationId, "budget: " + e.getMessage(), false);
            return Outcome.FAILED;
        } catch (RuntimeException e) {
            log.warn("Situation brief of {} failed: {}; the previous brief stays", situationId, e.toString());
            service.briefFailed(situationId, e.toString(), false);
            return Outcome.FAILED;
        }
    }

    private Outcome run(SituationEntity situation, LlmClient client, EgressPolicy policy,
            EgressPolicy.LlmDestination destination, Set<String> excluded, boolean privateAllowed) {
        String id = situation.getId();
        Predicate<String> allowed = source -> source == null
                || ((policy == null || policy.allowsLlmContext(source, destination)) && !excluded.contains(source));
        AnalystCitations citations = new AnalystCitations(extractors);
        JsonNode packet = json.valueToTree(packet(situation, allowed));
        citations.accept("get_situation", packet);
        McpCaller caller = AnalystRunner.toolCaller(excluded, privateAllowed);
        ToolCatalog tools = catalog.getIfAvailable();
        List<ToolSpec> specs = new ArrayList<>();
        if (tools != null) {
            for (ForesignalMcpTool tool : tools.readTools(caller)) {
                specs.add(new ToolSpec(tool.name(), tool.description(), tools.schema(tool)));
            }
        }
        for (ToolSpec spec : AnalystOutputTools.specs(json)) {
            if (AnalystOutputTools.RECORD_NOTE.equals(spec.name())) {
                specs.add(spec);
            }
        }
        List<AnalystOutputValidator.RawClaim> claims = Collections.synchronizedList(new ArrayList<>());
        List<AnalystOutputValidator.RawProposal> ignored = Collections.synchronizedList(new ArrayList<>());
        String[] note = new String[3];
        ToolLoop.ToolExecutor executor = call -> {
            if (AnalystOutputTools.RECORD_NOTE.equals(call.name())) {
                synchronized (note) {
                    AnalystOutputTools.capture(call.name(), call.input(), json, claims, ignored, note);
                }
                return AskPrompts.wrap(call.name(), StringNode.valueOf("recorded"), json);
            }
            if (tools == null) {
                return AskPrompts.wrap(call.name(), StringNode.valueOf("no tools are available"), json);
            }
            Optional<ForesignalMcpTool> tool = tools.readTool(call.name(), caller);
            if (tool.isEmpty()) {
                return AskPrompts.wrap(call.name(), StringNode.valueOf("unknown tool " + call.name()
                        + "; use only the tools you were given"), json);
            }
            ToolResult result = tools.invoke(tool.get(), call.input(), new ToolContext(caller, null, TRANSPORT));
            JsonNode raw = result.structured() != null ? result.structured() : StringNode.valueOf(text(result));
            ToolOutputFilter filter = outputFilter.getIfAvailable();
            JsonNode shown = filter == null ? raw : filter.filter(raw, allowed, null).output();
            citations.accept(call.name(), shown);
            return AskPrompts.wrap(call.name(), shown, json);
        };
        String system = PromptTemplates.text(PromptTemplates.SITUATION_BRIEF, PromptTemplates.V1);
        ChatRequest request = new ChatRequest(system,
                List.of(new ChatMessage.User(AskPrompts.wrap("get_situation", packet, json))), specs, null, Effort.LOW,
                Purpose.ANALYST, false, null);
        ToolLoop.Result result = new ToolLoop(client, executor, LIMITS, clock).run(request);
        ChatResponse last = result.lastResponse();
        if (last != null && last.stopReason() == StopReason.REFUSAL) {
            service.briefFailed(id, "model refused: " + last.refusalCategory(), false);
            return Outcome.FAILED;
        }
        String title = note[0] == null ? situation.getTitle() : note[0];
        String body = note[1] == null ? (last == null || last.content() == null ? "" : last.content()) : note[1];
        if (body.isBlank() && claims.isEmpty()) {
            service.briefFailed(id, "the model returned no brief", false);
            return Outcome.FAILED;
        }
        AnalystOutputValidator.Validated validated = AnalystOutputValidator.validate(title, body, "normal",
                List.copyOf(claims), List.of(), citations::valid, (type, members) -> false, Set.of(), EventTypes.ALL);
        String model = last == null ? null : last.model();
        String manifestId = manifest(situation, model, specs.size());
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("claims", validated.claims().size());
        details.put("citationsDropped", validated.citationsDropped());
        details.put("claimsDowngraded", validated.claimsDowngraded());
        details.put("warnings", validated.warnings());
        service.storeBrief(id, render(validated), model, manifestId, details);
        return Outcome.STORED;
    }

    /** The input packet: the situation's summary, corroboration and members (allowed sources only). */
    Map<String, Object> packet(SituationEntity situation, Predicate<String> allowed) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", situation.getId());
        out.put("title", situation.getTitle());
        out.put("theme", situation.getTheme());
        out.put("status", situation.getStatus());
        out.put("severity", situation.getSeverity());
        out.put("regionCodes", situation.getRegionCodes());
        out.put("summary", SituationMapper.stem(situation));
        out.put("corroborationShare", situation.getCorroborationShare());
        List<SituationMemberEntity> members = new ArrayList<>(service.members(situation.getId()));
        members.sort(SituationMapper.recentFirst());
        List<Map<String, Object>> items = new ArrayList<>();
        for (SituationMemberEntity member : members) {
            if (items.size() >= PACKET_MEMBERS) {
                break;
            }
            if (!allowed.test(member.getSourceId())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", member.getId());
            item.put("memberType", member.getMemberType());
            item.put("ref", member.getRef());
            item.put("cite", cite(member));
            citable(item, member);
            item.put("title", member.getTitle());
            item.put("signalFamily", member.getSignalFamily());
            item.put("severity", member.getSeverity());
            item.put("observedAt", member.getObservedAt() == null ? null : member.getObservedAt().toString());
            item.put("role", member.getRole());
            item.put("corroboration", member.getCorroboration());
            items.add(item);
        }
        out.put("members", items);
        return out;
    }

    /** Adds the fields that make a packet member citable by the 028 extractors and {@link AnalystCitations}. */
    static void citable(Map<String, Object> item, SituationMemberEntity member) {
        switch (member.getMemberType()) {
            case SituationMemberTypes.RECORD, SituationMemberTypes.ANOMALY -> {
                int slash = member.getRef().indexOf('/');
                if (slash > 0 && member.getSeq() != null) {
                    item.put("source", member.getRef().substring(0, slash));
                    item.put("externalId", member.getRef().substring(slash + 1));
                    item.put("seq", member.getSeq());
                }
            }
            case SituationMemberTypes.ALERT -> item.put("alert", Map.of("docType", "alert", "docId", member.getRef()));
            case SituationMemberTypes.INTEL_EVENT -> item.put("eventId", member.getRef());
            default -> {
                // Findings and investigations are cited by the member id.
            }
        }
    }

    /** The citation form of a member: {@code source:externalId@seq}, {@code alert:id} or {@code event:id}. */
    static String cite(SituationMemberEntity member) {
        return switch (member.getMemberType()) {
            case SituationMemberTypes.ALERT -> "alert:" + member.getRef();
            case SituationMemberTypes.INTEL_EVENT -> "event:" + member.getRef();
            case SituationMemberTypes.RECORD, SituationMemberTypes.ANOMALY -> {
                int slash = member.getRef().indexOf('/');
                String source = slash < 0 ? member.getRef() : member.getRef().substring(0, slash);
                String external = slash < 0 ? "" : member.getRef().substring(slash + 1);
                yield source + ":" + external + (member.getSeq() == null ? "" : "@" + member.getSeq());
            }
            default -> member.getId();
        };
    }

    /** The stored markdown: the body, then the typed claims with their basis and citations. */
    static String render(AnalystOutputValidator.Validated validated) {
        StringBuilder out = new StringBuilder();
        if (validated.title() != null && !validated.title().isBlank()) {
            out.append("### ").append(validated.title().strip()).append("\n\n");
        }
        out.append(validated.body() == null ? "" : validated.body().strip());
        if (!validated.claims().isEmpty()) {
            out.append("\n\n");
            for (AnalystClaim claim : validated.claims()) {
                out.append("- **").append(claim.basis()).append(":** ").append(claim.text());
                for (String citation : claim.citations()) {
                    out.append(" [").append(citation).append(']');
                }
                out.append('\n');
            }
        }
        out.append("\n\n_Speculation-capable narrative written by a model; members, status and severity are"
                + " deterministic._");
        return out.toString().strip();
    }

    private String manifest(SituationEntity situation, String model, int tools) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("situationId", situation.getId());
            config.put("packetMembers", PACKET_MEMBERS);
            config.put("tools", tools);
            Map<String, Object> modelMap = new LinkedHashMap<>();
            modelMap.put("model", model);
            modelMap.put("effort", "low");
            modelMap.put("templates", List.of(PromptTemplates.reference(PromptTemplates.SITUATION_BRIEF,
                    PromptTemplates.V1)));
            return service.record(ManifestRequest.of(COMPONENT, VERSION).config(config)
                    .inputs(List.of("situation:" + situation.getId())).model(modelMap));
        } catch (RuntimeException e) {
            log.warn("Could not record the brief manifest of {}: {}", situation.getId(), e.toString());
            return null;
        }
    }

    private static String text(ToolResult result) {
        StringBuilder out = new StringBuilder();
        for (ToolResult.Content block : result.content()) {
            if (block instanceof ToolResult.Text text) {
                if (!out.isEmpty()) {
                    out.append('\n');
                }
                out.append(text.text());
            }
        }
        return out.toString();
    }
}
