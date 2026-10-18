package ai.foresignal.llm;

import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.OpenAiSpeechClient;
import ai.foresignal.llm.client.TokenUsage;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The governed {@code tts} role (prompt 141): speech through the provider assigned as
 * {@code LlmSettings.ttsProvider}, an {@code openai-compatible} provider whose {@code /audio/speech} endpoint is
 * OpenAI itself or a local server such as Kokoro-FastAPI or openedai-speech.
 *
 * <p>Every synthesis reserves its characters with {@link TtsBudget} first (all chunks at once, so a long briefing
 * is refused whole rather than cut off half-way), then each request takes one of the provider's requests per
 * minute from {@link LlmBudgetGovernor} (with no tokens). Calls are metered (a call with zero tokens), update
 * provider health and are logged with provider, model, characters and duration; the text is never logged. Nothing
 * runs without a {@code tts} provider.
 */
@Service
public class TtsService {

    private static final Logger log = LoggerFactory.getLogger(TtsService.class);

    private final LlmSettingsService settings;
    private final LlmProviderService providers;
    private final LlmHttp http;
    private final JsonMapper json;
    private final LlmBudgetGovernor governor;
    private final TtsBudget budget;
    private final LlmUsageMeter usage;
    private final LlmHealthTracker health;
    private final Clock clock;

    /** Creates the service. */
    public TtsService(LlmSettingsService settings, LlmProviderService providers, LlmHttp http, JsonMapper json,
            LlmBudgetGovernor governor, TtsBudget budget, LlmUsageMeter usage, LlmHealthTracker health, Clock clock) {
        this.settings = settings;
        this.providers = providers;
        this.http = http;
        this.json = json;
        this.governor = governor;
        this.budget = budget;
        this.usage = usage;
        this.health = health;
        this.clock = clock;
    }

    /** The assigned {@code tts} provider, if any. */
    public Optional<String> providerId() {
        return Optional.ofNullable(settings.get().ttsProvider());
    }

    /** True when a {@code tts} provider is assigned. */
    public boolean configured() {
        return providerId().isPresent();
    }

    /**
     * Synthesizes {@code chunks} in order and returns each chunk's audio.
     *
     * @param chunks texts of at most {@link OpenAiSpeechClient#MAX_INPUT_CHARACTERS} characters each
     * @param voice the voice
     * @param format {@code mp3} or {@code opus}
     * @return the audio of each chunk, in order, with the provider and model used
     * @throws IllegalStateException when no {@code tts} provider is assigned or it is disabled
     * @throws LlmBudgetExceededException when the day's characters or the minute's requests are spent
     * @throws LlmProviderException when the provider fails
     */
    public Speech synthesize(List<String> chunks, String voice, String format) {
        String providerId = providerId().orElseThrow(() -> new IllegalStateException("no tts provider is assigned"));
        LlmProviderEntity entity = providers.get(providerId);
        if (!entity.isEnabled()) {
            throw new IllegalStateException("tts provider '" + providerId + "' is disabled");
        }
        LlmProviderConfig config = providers.config(entity);
        Map<String, Object> options = entity.getOptions() == null ? Map.of() : entity.getOptions();
        String model = options.get("ttsModel") instanceof String s ? s : null;
        long perDay = options.get("ttsCharactersPerDay") instanceof Number n ? n.longValue()
                : TtsBudget.DEFAULT_CHARACTERS_PER_DAY;
        long characters = chunks.stream().mapToLong(String::length).sum();
        budget.reserve(providerId, perDay, characters);
        OpenAiSpeechClient client = new OpenAiSpeechClient(config, http, json, model);
        return synthesize(providerId, config, client, chunks, voice, format, characters,
                chunk -> client.synthesize(chunk, voice, format));
    }

    /** The governed loop over the chunks; {@code call} performs one request. */
    Speech synthesize(String providerId, LlmProviderConfig config, OpenAiSpeechClient client, List<String> chunks,
            String voice, String format, long characters, Function<String, byte[]> call) {
        Instant start = clock.instant();
        List<byte[]> parts = new java.util.ArrayList<>();
        long done = 0;
        try {
            for (String chunk : chunks) {
                LlmBudgetGovernor.Reservation request = governor.reserve(config, 0);
                try {
                    parts.add(call.apply(chunk));
                    usage.recordCall(providerId, new TokenUsage(0, 0, 0));
                    done += chunk.length();
                } finally {
                    governor.settle(request, 0);
                }
            }
        } catch (LlmProviderException e) {
            usage.recordError(providerId);
            health.failure(providerId, e.getMessage());
            budget.settle(providerId, characters - done);
            log.warn("TTS call failed provider={} model={} durationMs={} kind={}: {}", providerId, client.model(),
                    Duration.between(start, clock.instant()).toMillis(), e.kind(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            budget.settle(providerId, characters - done);
            throw e;
        }
        health.success(providerId);
        log.info("TTS call provider={} model={} characters={} requests={} durationMs={}", providerId, client.model(),
                characters, chunks.size(), Duration.between(start, clock.instant()).toMillis());
        return new Speech(List.copyOf(parts), providerId, client.model(), voice, format, characters);
    }

    /**
     * Synthesized speech.
     *
     * @param parts the audio of each chunk, in order
     * @param providerId the provider used
     * @param model the speech model used
     * @param voice the voice
     * @param format {@code mp3} or {@code opus}
     * @param characters the characters synthesized
     */
    public record Speech(List<byte[]> parts, String providerId, String model, String voice, String format,
            long characters) {

        /** The parts joined byte for byte. */
        public byte[] joined() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (byte[] part : parts) {
                out.writeBytes(part);
            }
            return out.toByteArray();
        }
    }
}
