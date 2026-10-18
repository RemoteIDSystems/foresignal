package ai.foresignal.llm.client;

import ai.foresignal.llm.LlmHttp;
import ai.foresignal.llm.LlmProviderConfig;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

/**
 * The OpenAI-compatible speech endpoint (prompt 141, provider role {@code tts}):
 * {@code POST {baseUrl}/audio/speech} with {@code {model, input, voice, response_format}}, answered with the audio
 * bytes. The base URL follows the chat convention of {@link OpenAiCompatibleLlmClient} (it already ends in
 * {@code /v1}), so OpenAI, Kokoro-FastAPI and openedai-speech all answer at {@code /v1/audio/speech}.
 *
 * <p>Requests go through {@link LlmHttp} (outbound URL policy, pinned address, no redirects). A non-2xx status
 * becomes an {@link LlmProviderException} of the usual kinds; a 2xx answer that is not audio (an error page,
 * JSON) is {@code INVALID_RESPONSE}. Not a budgeted client on its own: {@code TtsService} governs it.
 */
public class OpenAiSpeechClient {

    /** The longest input one request may carry (OpenAI's documented limit). */
    public static final int MAX_INPUT_CHARACTERS = 4_000;

    /** The request timeout of one synthesis. */
    static final Duration TIMEOUT = Duration.ofMinutes(3);

    /** The model used when the provider sets no {@code options.ttsModel}. */
    public static final String DEFAULT_MODEL = "tts-1";

    private final LlmProviderConfig config;
    private final LlmHttp http;
    private final JsonMapper json;
    private final String model;

    /**
     * Creates a client for one provider configuration.
     *
     * @param config the provider
     * @param http the shared provider HTTP factory
     * @param json the JSON mapper
     * @param model the speech model, or null for {@value #DEFAULT_MODEL}
     */
    public OpenAiSpeechClient(LlmProviderConfig config, LlmHttp http, JsonMapper json, String model) {
        this.config = Objects.requireNonNull(config, "config");
        this.http = Objects.requireNonNull(http, "http");
        this.json = Objects.requireNonNull(json, "json");
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model;
    }

    /** The speech model. */
    public String model() {
        return model;
    }

    /**
     * Synthesizes {@code input}.
     *
     * @param input at most {@value #MAX_INPUT_CHARACTERS} characters
     * @param voice the voice, for example {@code alloy} or {@code af_bella}
     * @param format {@code mp3} or {@code opus}
     * @return the audio bytes
     * @throws IllegalArgumentException when the input is empty or too long, or the format is unknown
     * @throws LlmProviderException when the provider fails or answers with something other than audio
     */
    public byte[] synthesize(String input, String voice, String format) {
        if (input == null || input.isBlank() || input.length() > MAX_INPUT_CHARACTERS) {
            throw new IllegalArgumentException("speech input must be 1 to " + MAX_INPUT_CHARACTERS + " characters");
        }
        String fmt = format == null ? "mp3" : format.toLowerCase(Locale.ROOT);
        if (!"mp3".equals(fmt) && !"opus".equals(fmt)) {
            throw new IllegalArgumentException("speech format must be mp3 or opus");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", input);
        body.put("voice", voice);
        body.put("response_format", fmt);
        Map<String, String> headers = new LinkedHashMap<>(config.extraHeaders());
        if (config.apiKey() != null) {
            headers.put("Authorization", "Bearer " + config.apiKey());
        }
        headers.put("Content-Type", "application/json");
        HttpRequest.Builder builder = HttpRequest.newBuilder(http.uri(config.id(), config.baseUrl(), "/audio/speech"))
                .timeout(TIMEOUT)
                .header("Accept", "mp3".equals(fmt) ? "audio/mpeg" : "audio/ogg, audio/opus");
        headers.forEach(builder::header);
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
        HttpResponse<byte[]> response = http.sendBytes(config.id(), request);
        if (response.statusCode() / 100 != 2) {
            String text = response.body() == null ? "" : new String(response.body(), StandardCharsets.UTF_8);
            throw HttpJson.failure(config.id(), response.statusCode(), text,
                    response.headers().firstValue("retry-after").orElse(null), json);
        }
        byte[] audio = response.body();
        String type = response.headers().firstValue("content-type").orElse("").toLowerCase(Locale.ROOT);
        if (audio == null || audio.length == 0 || type.startsWith("application/json") || type.startsWith("text/")) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, config.id(),
                    "the speech endpoint answered without audio (" + (type.isEmpty() ? "no content type" : type) + ")");
        }
        return audio;
    }
}
