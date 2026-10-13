package ai.foresignal.mapping;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.client.ChatMessage;
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
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code suggestMapping} (prompt 059): asks the chat provider for a draft {@code RecordMapping}. The draft is
 * never stored or applied; saving it is the operator's own {@code PUT /v1/custom-sources/{id}}.
 *
 * <p>Order of checks: no chat provider is {@code 409 LLM_NOT_CONFIGURED}; a sample of a private source
 * ({@code hints.sourceId}) is refused for a remote provider ({@code 403}); a remote provider (035's
 * classification, {@link EgressPolicy#isLocal}) needs {@code confirmEgress: true}, else {@code 409
 * EGRESS_CONFIRMATION_REQUIRED}. The call is purpose {@code mapping-suggest} and spends the provider's budget
 * (026; {@code 429} when exhausted).
 *
 * <p>The sample travels inside {@code <sample>} delimiters as untrusted data, and the system prompt says to
 * ignore instructions in it. The model's output is parsed and compiled with {@link MappingCompiler}; up to
 * {@value #MAX_REPAIRS} repair rounds send the errors back. A draft that still fails is {@code 422
 * MAPPING_INVALID}. A valid draft is previewed over the sample and returned with that preview. The model gets no
 * tools, so nothing it writes can change anything.
 */
@Service
public class MappingSuggestService {

    /** The most repair rounds after the first answer. */
    public static final int MAX_REPAIRS = 2;

    /** The output cap of one call. */
    public static final int MAX_TOKENS = 4_000;

    private final LlmService llm;
    private final EgressPolicy egress;
    private final MappingCompiler compiler;
    private final MappingPreviewService preview;
    private final SourceRegistry registry;
    private final JsonMapper json;

    /** Creates the service. */
    public MappingSuggestService(LlmService llm, EgressPolicy egress, MappingCompiler compiler,
            MappingPreviewService preview, SourceRegistry registry, JsonMapper json) {
        this.llm = llm;
        this.egress = egress;
        this.compiler = compiler;
        this.preview = preview;
        this.registry = registry;
        this.json = json;
    }

    /**
     * {@code {sample, hints?, confirmEgress?}} → {@code MappingSuggestResult}.
     *
     * @throws ConflictException {@code LLM_NOT_CONFIGURED} or {@code EGRESS_CONFIRMATION_REQUIRED}
     * @throws ForesignalException 403 for a private source's sample and a remote provider; 422
     *     {@code MAPPING_INVALID} when the draft never validates
     */
    public Map<String, Object> suggest(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be {sample, hints?, confirmEgress?}.");
        }
        JsonNode hints = body.path("hints");
        SourceDescriptor descriptor = null;
        if (hints.hasNonNull("sourceId")) {
            String sourceId = hints.get("sourceId").asString();
            descriptor = registry.find(sourceId).map(SourceRegistry.Registration::descriptor)
                    .orElseThrow(() -> NotFoundException.of("Source", sourceId));
        }
        JsonNode doc = MappingInputs.fromSample(body.get("sample"), json);

        LlmClient client = llm.requireChatClient();
        LlmProviderConfig config = llm.config(client.providerId());
        boolean local = egress.isLocal(config.destination());
        if (!local && descriptor != null && descriptor.privateSource()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Samples of the private source " + descriptor.id() + " are never sent to a remote LLM provider.");
        }
        if (!local && !body.path("confirmEgress").asBoolean(false)) {
            throw new ConflictException(ErrorCodes.EGRESS_CONFIRMATION_REQUIRED, "The chat provider "
                    + config.id() + " is remote: the sample would leave this network. Repeat with confirmEgress: true.");
        }

        MappingContext context = descriptor == null ? MappingContext.none() : MappingContext.of(descriptor, true);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage.User(MappingSuggestPrompts.user(hintLines(hints, descriptor),
                json.writeValueAsString(doc))));
        List<FieldError> errors = List.of();
        String model = config.chatModel();
        for (int round = 0; round <= MAX_REPAIRS; round++) {
            ChatResponse response = client.chat(new ChatRequest(MappingSuggestPrompts.SYSTEM, messages, List.of(),
                    MAX_TOKENS, null, Purpose.MAPPING_SUGGEST, false, null));
            model = response.model() == null ? model : response.model();
            if (response.stopReason() == StopReason.REFUSAL) {
                throw new ForesignalException(HttpStatus.valueOf(422), ErrorCodes.MAPPING_INVALID,
                        "The model declined to draft a mapping.");
            }
            Draft draft = parse(response.content());
            if (draft.mapping() != null) {
                try {
                    CompiledMapping compiled = compiler.compile(draft.mapping(), context);
                    return result(draft, compiled, doc, descriptor, round, config, model);
                } catch (MappingInvalidException e) {
                    errors = e.getErrors();
                }
            } else {
                errors = List.of(new FieldError("output", draft.problem()));
            }
            messages.add(response.assistantMessage());
            messages.add(new ChatMessage.User(MappingSuggestPrompts.repair(errors)));
        }
        throw new ForesignalException(HttpStatus.valueOf(422), ErrorCodes.MAPPING_INVALID,
                "The model's draft did not validate after " + MAX_REPAIRS + " repair rounds.")
                .withProperty("errors", errors);
    }

    private Map<String, Object> result(Draft draft, CompiledMapping compiled, JsonNode doc,
            SourceDescriptor descriptor, int round, LlmProviderConfig config, String model) {
        Map<String, Object> previewResult = preview.preview(draft.mapping(), doc, descriptor);
        List<MappingWarning> warnings = new ArrayList<>(compiled.warnings());
        Object previewWarnings = previewResult.get("warnings");
        if (previewWarnings instanceof List<?> list) {
            for (Object w : list) {
                if (w instanceof MappingWarning warning && !warnings.contains(warning)) {
                    warnings.add(warning);
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("draft", draft.mapping());
        out.put("explanation", draft.explanation());
        out.put("warnings", warnings);
        out.put("preview", previewResult);
        out.put("repairRounds", round);
        out.put("provider", config.id());
        out.put("model", model == null ? "" : model);
        return out;
    }

    /** The hint lines of the user turn: the operator's hints, then the target source's facts. */
    static List<String> hintLines(JsonNode hints, SourceDescriptor descriptor) {
        List<String> out = new ArrayList<>();
        if (hints.hasNonNull("kind")) {
            out.add("kind: " + abbreviate(hints.get("kind").asString(), 64));
        }
        if (hints.hasNonNull("description")) {
            out.add("description: " + abbreviate(hints.get("description").asString(), 2000));
        }
        if (descriptor != null) {
            out.add("source kinds: " + descriptor.kinds());
            out.add("source time base (observedAtMode): " + descriptor.observedAtMode());
            if (descriptor.fieldSpecs() != null && !descriptor.fieldSpecs().isEmpty()) {
                out.add("declared fields: " + descriptor.fieldSpecs().keySet());
            }
            if (!descriptor.significantFields().isEmpty()) {
                out.add("significant fields: " + descriptor.significantFields());
            }
        }
        return out;
    }

    /**
     * A model answer: the mapping and its explanation, or why it could not be read.
     *
     * @param mapping the mapping, or null
     * @param explanation the explanation, never null
     * @param problem why no mapping could be read, or null
     */
    record Draft(JsonNode mapping, String explanation, String problem) {}

    /** Reads the first JSON object of {@code content}, with or without markdown fences. */
    Draft parse(String content) {
        if (content == null) {
            return new Draft(null, "", "the answer was empty");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return new Draft(null, "", "the answer held no JSON object; return only the JSON object");
        }
        JsonNode node;
        try {
            node = json.readTree(content.substring(start, end + 1));
        } catch (JacksonException e) {
            return new Draft(null, "", "the answer was not valid JSON: " + e.getOriginalMessage());
        }
        if (node == null || !node.isObject()) {
            return new Draft(null, "", "the answer was not a JSON object");
        }
        JsonNode mapping = node.get("mapping");
        if (mapping == null && node.has("version")) {
            mapping = node;
        }
        if (mapping == null || !mapping.isObject()) {
            return new Draft(null, "", "the answer had no \"mapping\" object");
        }
        String explanation = node.path("explanation").isString() ? node.get("explanation").asString() : "";
        return new Draft(mapping, explanation, null);
    }

    private static String abbreviate(String text, int max) {
        return text == null ? "" : text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
