package ai.foresignal.llm.client;

import ai.foresignal.llm.LlmProviderConfig;
import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.BadRequestException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.NotFoundException;
import com.anthropic.errors.PermissionDeniedException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.errors.UnprocessableEntityException;
import com.anthropic.helpers.BetaMessageAccumulator;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.BetaCacheControlEphemeral;
import com.anthropic.models.beta.messages.BetaContentBlock;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaMessageParam;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaRawMessageStreamEvent;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.BetaTextBlockParam;
import com.anthropic.models.beta.messages.BetaTool;
import com.anthropic.models.beta.messages.BetaToolResultBlockParam;
import com.anthropic.models.beta.messages.BetaToolUseBlock;
import com.anthropic.models.beta.messages.BetaToolUseBlockParam;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.models.ModelInfo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Provider type {@code anthropic}: Claude through the official Anthropic Java SDK
 * ({@code com.anthropic:anthropic-java}), on the beta messages path so the server-side refusal
 * fallback can be enabled (Architecture §19).
 *
 * <p>Request shape, per the rules for {@code claude-opus-5-5}:
 * <ul>
 *   <li>{@code thinking} is never sent: thinking is adaptive and always on, and both
 *       {@code thinking: disabled} and {@code budget_tokens} are rejected. Depth is controlled by
 *       {@code output_config.effort}, always set explicitly (the model's own default is medium).
 *   <li>{@code tool_choice} is never sent, so it is {@code auto}: forced tool choice is rejected.
 *       Tools with closed schemas ({@code additionalProperties: false} plus {@code required}) carry
 *       {@code strict: true}.
 *   <li>The refusal fallback: beta {@code server-side-fallback-2026-07-01} and
 *       {@code fallbacks: "default"}. {@code stop_reason} is checked for {@code refusal} before content
 *       is read, and {@code stop_details.category} becomes {@link ChatResponse#refusalCategory()}.
 *   <li>Prompt caching: {@code cache_control: ephemeral} on the system block and on the last tool,
 *       so repeated calls reuse the stable prefix. {@code cache_read_input_tokens} is reported as
 *       cached tokens.
 *   <li>Conversation history is append-only: an assistant turn is replayed as the SDK message param
 *       built from the response ({@link ChatResponse#rawAssistantTurn()}), thinking blocks included,
 *       never edited. There is no assistant prefill.
 *   <li>Streaming (the SDK's streaming call, accumulated with {@link BetaMessageAccumulator}) is used
 *       when the request asks for it or the output cap exceeds 16000 tokens.
 * </ul>
 *
 * <p>No embeddings: Anthropic has no embeddings endpoint, so {@link #embed} throws.
 */
public class AnthropicLlmClient implements LlmClient {

    /** The beta that enables the server-side refusal fallback. */
    public static final AnthropicBeta FALLBACK_BETA = AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01;

    /** Output caps above this are always streamed. */
    public static final int STREAM_ABOVE_MAX_TOKENS = 16_000;

    private final LlmProviderConfig config;
    private final AnthropicClient client;
    private final JsonMapper json;

    /**
     * Creates the client.
     *
     * @param config the provider configuration
     * @param client the SDK client built for it by {@code LlmHttp}
     * @param json Foresignal's JSON mapper
     */
    public AnthropicLlmClient(LlmProviderConfig config, AnthropicClient client, JsonMapper json) {
        this.config = Objects.requireNonNull(config, "config");
        this.client = Objects.requireNonNull(client, "client");
        this.json = Objects.requireNonNull(json, "json");
    }

    @Override
    public String providerId() {
        return config.id();
    }

    /** The SDK client, for closing when the configuration changes. */
    public AnthropicClient sdkClient() {
        return client;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        MessageCreateParams params = params(request);
        int maxTokens = Math.toIntExact(params.maxTokens());
        BetaMessage message;
        try {
            message = request.stream() || maxTokens > STREAM_ABOVE_MAX_TOKENS
                    ? streamed(params, request.onDelta())
                    : client.beta().messages().create(params);
        } catch (RuntimeException e) {
            throw translate(e);
        }
        return response(message);
    }

    /** Builds the SDK request for {@code request}. Visible for tests of the request shape. */
    MessageCreateParams params(ChatRequest request) {
        if (config.chatModel() == null) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, config.id(),
                    "no chatModel is configured");
        }
        Effort effort = request.effort() != null ? request.effort()
                : config.effort() != null ? config.effort() : request.purpose().defaultEffort();
        long maxTokens = request.maxTokens() != null ? request.maxTokens() : config.effectiveMaxTokens();

        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(config.chatModel())
                .maxTokens(maxTokens)
                .outputConfig(BetaOutputConfig.builder().effort(effort(effort)).build())
                .addBeta(FALLBACK_BETA)
                .fallbacksDefault();
        if (!request.system().isEmpty()) {
            builder.systemOfBetaTextBlockParams(List.of(BetaTextBlockParam.builder()
                    .text(request.system())
                    .cacheControl(BetaCacheControlEphemeral.builder().build())
                    .build()));
        }
        List<ToolSpec> tools = request.tools();
        for (int i = 0; i < tools.size(); i++) {
            builder.addTool(tool(tools.get(i), i == tools.size() - 1));
        }
        builder.messages(messages(request.messages()));
        return builder.build();
    }

    private BetaMessage streamed(MessageCreateParams params, Consumer<String> onDelta) {
        BetaMessageAccumulator accumulator = BetaMessageAccumulator.create();
        try (StreamResponse<BetaRawMessageStreamEvent> stream = client.beta().messages().createStreaming(params)) {
            stream.stream().forEach(event -> {
                accumulator.accumulate(event);
                if (onDelta != null) {
                    event.contentBlockDelta()
                            .flatMap(delta -> delta.delta().text())
                            .ifPresent(text -> onDelta.accept(text.text()));
                }
            });
        }
        return accumulator.message();
    }

    private BetaTool tool(ToolSpec spec, boolean last) {
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = json.convertValue(spec.inputSchema(), LinkedHashMap.class);
        BetaTool.InputSchema.Builder input = BetaTool.InputSchema.builder();
        Object properties = schema.get("properties");
        if (properties instanceof Map<?, ?> props) {
            BetaTool.InputSchema.Properties.Builder p = BetaTool.InputSchema.Properties.builder();
            props.forEach((key, value) -> p.putAdditionalProperty(String.valueOf(key), JsonValue.from(value)));
            input.properties(p.build());
        }
        Object required = schema.get("required");
        if (required instanceof List<?> list) {
            input.required(list.stream().map(String::valueOf).toList());
        }
        schema.forEach((key, value) -> {
            if (!key.equals("type") && !key.equals("properties") && !key.equals("required")) {
                input.putAdditionalProperty(key, JsonValue.from(value));
            }
        });
        BetaTool.Builder tool = BetaTool.builder()
                .name(spec.name())
                .description(spec.description())
                .inputSchema(input.build());
        if (spec.strictEligible()) {
            tool.strict(true);
        }
        if (last) {
            tool.cacheControl(BetaCacheControlEphemeral.builder().build());
        }
        return tool.build();
    }

    private List<BetaMessageParam> messages(List<ChatMessage> conversation) {
        List<BetaMessageParam> out = new ArrayList<>(conversation.size());
        for (ChatMessage message : conversation) {
            switch (message) {
                case ChatMessage.User user -> out.add(BetaMessageParam.builder()
                        .role(BetaMessageParam.Role.USER)
                        .content(user.text())
                        .build());
                case ChatMessage.Assistant assistant -> out.add(assistant.rawTurn() instanceof BetaMessageParam raw
                        ? raw
                        : rebuiltAssistant(assistant));
                case ChatMessage.ToolResults results -> {
                    List<BetaContentBlockParam> blocks = new ArrayList<>();
                    for (ToolResult result : results.results()) {
                        blocks.add(BetaContentBlockParam.ofToolResult(BetaToolResultBlockParam.builder()
                                .toolUseId(result.toolCallId())
                                .content(result.content())
                                .isError(result.isError())
                                .build()));
                    }
                    out.add(BetaMessageParam.builder()
                            .role(BetaMessageParam.Role.USER)
                            .contentOfBetaContentBlockParams(blocks)
                            .build());
                }
            }
        }
        return out;
    }

    /** An assistant turn that did not come from this provider (for example a hand-built one): text and tool uses. */
    private BetaMessageParam rebuiltAssistant(ChatMessage.Assistant assistant) {
        List<BetaContentBlockParam> blocks = new ArrayList<>();
        if (!assistant.text().isEmpty()) {
            blocks.add(BetaContentBlockParam.ofText(assistant.text()));
        }
        for (ToolCall call : assistant.toolCalls()) {
            blocks.add(BetaContentBlockParam.ofToolUse(BetaToolUseBlockParam.builder()
                    .id(call.id())
                    .name(call.name())
                    .input(JsonValue.from(json.convertValue(call.input(), Object.class)))
                    .build()));
        }
        return BetaMessageParam.builder()
                .role(BetaMessageParam.Role.ASSISTANT)
                .contentOfBetaContentBlockParams(blocks)
                .build();
    }

    private ChatResponse response(BetaMessage message) {
        BetaStopReason reason = message.stopReason().orElse(null);
        StopReason stop = stopReason(reason);
        String refusalCategory = null;
        if (stop == StopReason.REFUSAL) {
            // Checked before any content is read: a refused turn carries no usable answer.
            refusalCategory = message.stopDetails()
                    .flatMap(details -> details.category())
                    .map(Object::toString)
                    .orElse(null);
        }
        StringBuilder text = new StringBuilder();
        List<ToolCall> calls = new ArrayList<>();
        if (stop != StopReason.REFUSAL) {
            for (BetaContentBlock block : message.content()) {
                block.text().ifPresent(t -> text.append(t.text()));
                block.toolUse().ifPresent(use -> calls.add(toolCall(use)));
            }
        }
        TokenUsage usage = new TokenUsage(message.usage().inputTokens(), message.usage().outputTokens(),
                message.usage().cacheReadInputTokens().orElse(0L));
        return new ChatResponse(text.toString(), calls, stop, refusalCategory, usage, message.model().asString(),
                message.toParam());
    }

    private ToolCall toolCall(BetaToolUseBlock use) {
        Object input = use._input().convert(Object.class);
        JsonNode node = input == null ? json.createObjectNode() : json.valueToTree(input);
        return new ToolCall(use.id(), use.name(), node);
    }

    static StopReason stopReason(BetaStopReason reason) {
        if (reason == null) {
            return StopReason.ERROR;
        }
        if (reason.equals(BetaStopReason.REFUSAL)) {
            return StopReason.REFUSAL;
        }
        if (reason.equals(BetaStopReason.TOOL_USE)) {
            return StopReason.TOOL_USE;
        }
        if (reason.equals(BetaStopReason.MAX_TOKENS) || reason.equals(BetaStopReason.MODEL_CONTEXT_WINDOW_EXCEEDED)) {
            return StopReason.MAX_TOKENS;
        }
        if (reason.equals(BetaStopReason.END_TURN) || reason.equals(BetaStopReason.STOP_SEQUENCE)
                || reason.equals(BetaStopReason.PAUSE_TURN)) {
            return StopReason.END;
        }
        return StopReason.ERROR;
    }

    private static BetaOutputConfig.Effort effort(Effort effort) {
        return switch (effort) {
            case LOW -> BetaOutputConfig.Effort.LOW;
            case MEDIUM -> BetaOutputConfig.Effort.MEDIUM;
            case HIGH -> BetaOutputConfig.Effort.HIGH;
            case XHIGH -> BetaOutputConfig.Effort.XHIGH;
            case MAX -> BetaOutputConfig.Effort.MAX;
        };
    }

    /** Maps the SDK's typed exceptions, most specific first. */
    LlmProviderException translate(RuntimeException e) {
        if (e instanceof LlmProviderException already) {
            return already;
        }
        String id = config.id();
        if (e instanceof RateLimitException rate) {
            String header = rate.headers().values("retry-after").stream().findFirst().orElse(null);
            return new LlmProviderException(LlmProviderException.Kind.RATE_LIMITED, id, "rate limited (429)",
                    LlmProviderException.parseRetryAfter(header), e);
        }
        if (e instanceof InternalServerException server) {
            return new LlmProviderException(LlmProviderException.Kind.OVERLOADED, id,
                    "overloaded or failing (" + server.statusCode() + ")", null, e);
        }
        if (e instanceof UnauthorizedException || e instanceof PermissionDeniedException) {
            return new LlmProviderException(LlmProviderException.Kind.AUTHENTICATION, id,
                    "the API key was rejected (" + ((AnthropicServiceException) e).statusCode() + ")", null, e);
        }
        if (e instanceof BadRequestException || e instanceof NotFoundException
                || e instanceof UnprocessableEntityException) {
            return new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, id,
                    "rejected the request (" + ((AnthropicServiceException) e).statusCode() + "): "
                            + serviceMessage((AnthropicServiceException) e), null, e);
        }
        if (e instanceof AnthropicServiceException service) {
            LlmProviderException.Kind kind = LlmProviderException.kindForStatus(service.statusCode());
            return new LlmProviderException(kind, id, "failed (" + service.statusCode() + ")", null, e);
        }
        if (e instanceof AnthropicIoException) {
            return new LlmProviderException(LlmProviderException.Kind.CONNECTION, id, "could not be reached", null, e);
        }
        return new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, id,
                e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()), null, e);
    }

    /** The {@code error.message} of an API error body, without any request content. */
    private static String serviceMessage(AnthropicServiceException e) {
        try {
            Object body = e.body().convert(Object.class);
            if (body instanceof Map<?, ?> map && map.get("error") instanceof Map<?, ?> error
                    && error.get("message") != null) {
                return String.valueOf(error.get("message"));
            }
        } catch (RuntimeException ignored) {
            // fall through to the generic message
        }
        return "invalid request";
    }

    @Override
    public EmbeddingResponse embed(List<String> texts) {
        throw new UnsupportedOperationException(
                "Anthropic has no embeddings endpoint; assign an openai-compatible or ollama provider to the embedding role");
    }

    @Override
    public EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec) {
        return embed(texts);
    }

    @Override
    public List<LlmModelInfo> models() {
        List<LlmModelInfo> out = new ArrayList<>();
        try {
            for (ModelInfo model : client.models().list().autoPager()) {
                out.add(new LlmModelInfo(model.id(), model.displayName(), model.maxInputTokens().orElse(null), true,
                        false));
            }
        } catch (RuntimeException e) {
            throw translate(e);
        }
        return out;
    }
}
