package ai.foresignal.trigger;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ask.AskPrompts;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Advisory relevance of the sources of a rule's dataset ({@code analyzeTriggerSources}): for each source, whether
 * it contributes to a dataset built from all of them, with a one-sentence reason written by the chat provider.
 *
 * <p>The model sees only catalog content: each source's name, description, kinds and entity identity types, and what
 * the set shares (kinds and identity types declared by two or more sources). It is asked through the versioned
 * template {@code source-relevance-system@1.0.0}, the chat role and {@link Purpose#ANALYST}, with the sources as an
 * untrusted data block, exactly like the social claim extraction. The answer is advice: nothing is enabled, omitted
 * or stored, and every requested source comes back with a verdict. A source the egress policy keeps from the provider
 * is not sent and comes back {@value #UNRATED}, as does one the model left out or rated with a value outside
 * {@link #VERDICTS}.
 */
@Service
public class SourceRelevanceAnalyzer {

    /** The verdicts the model may give. */
    public static final List<String> VERDICTS = List.of("relevant", "marginal", "not-meaningful");

    /** The verdict of a source that was not rated: withheld by egress, or left out by the model. */
    public static final String UNRATED = "unrated";

    /** The fewest sources a set takes. */
    public static final int MIN_SOURCES = 2;

    /** The most sources a set takes, as many as a rule ({@code TriggerRuleInput.sources}). */
    public static final int MAX_SOURCES = 50;

    /** The output cap of one call. */
    static final int MAX_TOKENS = 3_000;

    /** The longest description and reason passed on. */
    static final int MAX_TEXT = 600;

    private final SourceRegistry registry;
    private final LlmService llm;
    private final LlmSettingsService llmSettings;
    private final EgressPolicy egress;
    private final JsonMapper json;

    /** Creates the analyzer. */
    public SourceRelevanceAnalyzer(SourceRegistry registry, LlmService llm, LlmSettingsService llmSettings,
            EgressPolicy egress, JsonMapper json) {
        this.registry = registry;
        this.llm = llm;
        this.llmSettings = llmSettings;
        this.egress = egress;
        this.json = json;
    }

    /**
     * Parses a {@code SourceRelevanceRequest}: {@code sourceIds}, {@value #MIN_SOURCES} to {@value #MAX_SOURCES}
     * distinct registered source ids.
     *
     * @throws ValidationException 400 with a field error per problem
     */
    public List<String> parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!"sourceIds".equals(field)) {
                errors.add(new FieldError(field, "is not a SourceRelevanceRequest field"));
            }
        }
        JsonNode ids = body.get("sourceIds");
        Set<String> out = new LinkedHashSet<>();
        if (ids == null || !ids.isArray()) {
            errors.add(new FieldError("sourceIds", "is required, an array of source ids"));
        } else {
            for (int i = 0; i < ids.size(); i++) {
                JsonNode id = ids.get(i);
                if (!id.isString() || !registry.contains(id.asString())) {
                    errors.add(new FieldError("sourceIds[" + i + "]", "is not a registered source id"));
                } else if (!out.add(id.asString())) {
                    errors.add(new FieldError("sourceIds[" + i + "]", "repeats " + id.asString()));
                }
            }
            if (ids.size() < MIN_SOURCES || ids.size() > MAX_SOURCES) {
                errors.add(new FieldError("sourceIds", "must hold " + MIN_SOURCES + " to " + MAX_SOURCES
                        + " source ids"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The source relevance request is not valid.", errors);
        }
        return List.copyOf(out);
    }

    /**
     * Rates {@code sourceIds} against each other (a {@code SourceRelevanceAnalysis}).
     *
     * @throws ConflictException 409 {@code LLM_NOT_CONFIGURED} without a usable chat provider
     */
    public Map<String, Object> analyze(List<String> sourceIds) {
        String providerId = llmSettings.get().chatProvider();
        if (providerId == null) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED, "No chat provider is configured; add one in"
                    + " /v1/llm/providers and assign it in PUT /v1/llm/settings.");
        }
        EgressPolicy.LlmDestination destination;
        try {
            destination = llm.config(providerId).destination();
        } catch (RuntimeException e) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED, "The chat provider " + providerId
                    + " is not usable: " + e.getMessage());
        }
        Set<String> excluded = egress.llmExcludedSources(destination);
        List<SourceDescriptor> all = sourceIds.stream().map(registry::descriptor).toList();
        List<SourceDescriptor> sent = all.stream().filter(d -> !excluded.contains(d.id())).toList();
        Map<String, Object> shared = shared(sent);

        Map<String, Map<String, String>> rated = Map.of();
        String summary = null;
        String model = null;
        String failure = null;
        if (sent.size() < MIN_SOURCES) {
            failure = "Fewer than " + MIN_SOURCES + " of these sources may be sent to the chat provider, so the set"
                    + " was not rated.";
        } else {
            LlmClient client = llm.client(providerId);
            ChatRequest request = ChatRequest.simple(Purpose.ANALYST,
                    PromptTemplates.text(PromptTemplates.SOURCE_RELEVANCE, PromptTemplates.V1),
                    AskPrompts.wrap("dataset_sources", packet(sent, shared), json), MAX_TOKENS);
            ChatResponse response = client.chat(request);
            model = response.model();
            if (response.stopReason() == StopReason.REFUSAL) {
                failure = "The model declined to rate this set.";
            } else {
                Answer answer = parseAnswer(response.content(), json);
                rated = answer.verdicts();
                summary = answer.summary();
                if (rated.isEmpty()) {
                    failure = "The model's answer could not be read.";
                }
            }
        }

        List<Map<String, Object>> sources = new ArrayList<>();
        for (SourceDescriptor d : all) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("sourceId", d.id());
            s.put("name", d.name());
            s.put("kinds", new ArrayList<>(new TreeSet<>(d.kinds())));
            Map<String, String> verdict = rated.get(d.id());
            if (excluded.contains(d.id())) {
                s.put("verdict", UNRATED);
                s.put("reason", "Not sent to the chat provider: the egress policy keeps this source's details from"
                        + " it.");
            } else if (verdict != null) {
                s.put("verdict", verdict.get("verdict"));
                s.put("reason", verdict.get("reason"));
            } else {
                s.put("verdict", UNRATED);
                s.put("reason", failure != null ? failure : "The model gave no verdict for this source.");
            }
            sources.add(s);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("advisory", true);
        out.put("summary", summary);
        out.put("sources", sources);
        out.put("shared", shared);
        out.put("model", model);
        out.put("template", PromptTemplates.reference(PromptTemplates.SOURCE_RELEVANCE, PromptTemplates.V1));
        return out;
    }

    /** The kinds and entity identity types that two or more of {@code sources} declare, sorted. */
    static Map<String, Object> shared(List<SourceDescriptor> sources) {
        Map<String, Integer> kinds = new TreeMap<>();
        Map<String, Integer> identities = new TreeMap<>();
        for (SourceDescriptor d : sources) {
            for (String kind : d.kinds()) {
                kinds.merge(kind, 1, Integer::sum);
            }
            for (String key : d.entityKeys().keySet()) {
                identities.merge(key, 1, Integer::sum);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kinds", kinds.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList());
        out.put("entityTypes", identities.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey)
                .toList());
        return out;
    }

    /** The data block: each source's catalog entry and what the set shares. */
    JsonNode packet(List<SourceDescriptor> sources, Map<String, Object> shared) {
        ObjectNode data = json.createObjectNode();
        ArrayNode list = data.putArray("sources");
        for (SourceDescriptor d : sources) {
            ObjectNode s = list.addObject();
            s.put("sourceId", d.id());
            s.put("name", d.name());
            if (d.description() != null && !d.description().isBlank()) {
                s.put("description", clip(d.description().strip()));
            }
            ArrayNode kinds = s.putArray("kinds");
            new TreeSet<>(d.kinds()).forEach(kinds::add);
            ArrayNode identities = s.putArray("entityTypes");
            d.entityKeys().keySet().forEach(identities::add);
            if (!d.derivedFrom().isEmpty()) {
                ArrayNode derived = s.putArray("derivedFrom");
                d.derivedFrom().forEach(derived::add);
            }
        }
        data.set("shared", json.valueToTree(shared));
        return data;
    }

    /**
     * What the model answered.
     *
     * @param summary its one-sentence summary of the set, or null
     * @param verdicts verdict and reason by source id, for the entries that read
     */
    record Answer(String summary, Map<String, Map<String, String>> verdicts) {}

    /**
     * Reads the model's answer: the first JSON object in {@code content}. An entry counts when its source id is a
     * string, its verdict one of {@link #VERDICTS} and its reason non-blank; the first entry per source wins.
     */
    static Answer parseAnswer(String content, JsonMapper json) {
        Map<String, Map<String, String>> verdicts = new LinkedHashMap<>();
        if (content == null) {
            return new Answer(null, verdicts);
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return new Answer(null, verdicts);
        }
        JsonNode root;
        try {
            root = json.readTree(content.substring(start, end + 1));
        } catch (RuntimeException e) {
            return new Answer(null, verdicts);
        }
        JsonNode list = root.get("sources");
        if (list != null && list.isArray()) {
            for (JsonNode entry : list) {
                String id = text(entry.get("sourceId"));
                String verdict = text(entry.get("verdict"));
                String reason = text(entry.get("reason"));
                if (id != null && verdict != null && VERDICTS.contains(verdict) && reason != null
                        && !verdicts.containsKey(id)) {
                    verdicts.put(id, Map.of("verdict", verdict, "reason", clip(reason)));
                }
            }
        }
        String summary = text(root.get("summary"));
        return new Answer(summary == null ? null : clip(summary), verdicts);
    }

    private static String clip(String text) {
        return text.length() <= MAX_TEXT ? text : text.substring(0, MAX_TEXT - 1) + "…";
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String s = node.asString().strip();
        return s.isEmpty() ? null : s;
    }
}
