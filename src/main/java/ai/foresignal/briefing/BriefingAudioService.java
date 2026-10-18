package ai.foresignal.briefing;

import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.TtsService;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.OpenAiSpeechClient;
import ai.foresignal.media.MediaStore;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRole;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Audio briefings (prompt 141): after a run completes, a briefing with an {@code audio} option has its plain-text
 * rendition ({@link SpeechText#of}) synthesized through the {@code tts} role ({@link TtsService}).
 *
 * <ul>
 *   <li>Each request carries at most {@value OpenAiSpeechClient#MAX_INPUT_CHARACTERS} characters. Longer text is
 *       split at paragraph or sentence boundaries and the MP3 parts concatenated ({@link Mp3Concat}); for Opus,
 *       whose Ogg streams do not concatenate, the text is truncated with a spoken note. MP3 is capped at
 *       {@value #MAX_CHUNKS} requests, beyond which it is truncated the same way.
 *   <li>The audio is stored in the 043 media store with {@code role=audio}, expiring with the briefing-run
 *       retention, and linked from the run's {@code audio.mediaId}; it is signed with prompt 109's detached JWS
 *       ({@code audio.signature}, {@code typ} the audio media type).
 *   <li>The outcome is the run's {@code BriefingRun.audio} object: {@code status} {@code ok}, {@code failed}
 *       (provider error, budget, storage) or {@code skipped} (no {@code tts} provider, nothing to say), with the
 *       error. Nothing here throws: a failure never blocks the run or its text delivery.
 * </ul>
 */
@Component
public class BriefingAudioService {

    private static final Logger log = LoggerFactory.getLogger(BriefingAudioService.class);

    /** The most speech requests of one MP3 briefing (100,000 characters). */
    public static final int MAX_CHUNKS = 25;

    /** {@code audio.status} of stored audio. */
    public static final String OK = "ok";

    /** {@code audio.status} of a synthesis or storage failure. */
    public static final String FAILED = "failed";

    /** {@code audio.status} when nothing was attempted. */
    public static final String SKIPPED = "skipped";

    /** The media expiry when the retention settings cannot be read. */
    static final Duration DEFAULT_RETENTION = Duration.ofDays(180);

    private final TtsService tts;
    private final MediaStore media;
    private final ObjectProvider<RetentionResolver> retention;
    private final ObjectProvider<JwsSigner> signer;
    private final Clock clock;

    /** Creates the service. */
    public BriefingAudioService(TtsService tts, MediaStore media, ObjectProvider<RetentionResolver> retention,
            ObjectProvider<JwsSigner> signer, Clock clock) {
        this.tts = tts;
        this.media = media;
        this.retention = retention;
        this.signer = signer;
        this.clock = clock;
    }

    /**
     * Synthesizes, stores and signs the audio of a completed run.
     *
     * @param title the spoken title (the notification title)
     * @param markdown the run's markdown
     * @param audio the briefing's audio option
     * @return the {@code BriefingRun.audio} object
     */
    public Map<String, Object> synthesize(String title, String markdown, BriefingInput.Audio audio) {
        Map<String, Object> out = result(SKIPPED, audio);
        if (!tts.configured()) {
            out.put("error", "no tts provider is assigned; set ttsProvider in PUT /v1/llm/settings");
            return out;
        }
        String text = SpeechText.of(title, markdown);
        if (text.isBlank()) {
            out.put("error", "the run has no text to speak");
            return out;
        }
        boolean mp3 = BriefingInput.Audio.MP3.equals(audio.format());
        int max = OpenAiSpeechClient.MAX_INPUT_CHARACTERS;
        boolean truncated = false;
        List<String> chunks;
        if (mp3) {
            if (SpeechText.exceeds(text, max * MAX_CHUNKS)) {
                text = SpeechText.truncate(text, max * MAX_CHUNKS);
                truncated = true;
            }
            chunks = SpeechText.chunks(text, max);
            if (chunks.size() > MAX_CHUNKS) {
                // Boundaries leave chunks under the limit, so the character cap alone can still need more requests.
                List<String> kept = new java.util.ArrayList<>(chunks.subList(0, MAX_CHUNKS));
                kept.set(MAX_CHUNKS - 1, SpeechText.withNote(kept.getLast(), max));
                chunks = List.copyOf(kept);
                truncated = true;
            }
        } else {
            if (SpeechText.exceeds(text, max)) {
                text = SpeechText.truncate(text, max);
                truncated = true;
            }
            chunks = List.of(text);
        }
        out.put("truncated", truncated);
        TtsService.Speech speech;
        try {
            speech = tts.synthesize(chunks, audio.voice(), audio.format());
        } catch (LlmBudgetExceededException | LlmProviderException | IllegalStateException
                | IllegalArgumentException e) {
            return failed(out, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Briefing audio synthesis failed unexpectedly", e);
            return failed(out, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        byte[] bytes = mp3 ? Mp3Concat.concat(speech.parts()) : speech.parts().getFirst();
        String contentType = mp3 ? "audio/mpeg" : "audio/ogg";
        String mediaId;
        try {
            mediaId = media.store(MediaInput.of(bytes, contentType, MediaRole.AUDIO)
                    .withExpiresAt(clock.instant().plus(retentionWindow()))).id();
        } catch (RuntimeException e) {
            log.warn("Briefing audio could not be stored: {}", e.getMessage());
            return failed(out, "the audio could not be stored: " + e.getMessage());
        }
        out.put("status", OK);
        out.put("mediaId", mediaId);
        out.put("characters", speech.characters());
        out.put("bytes", (long) bytes.length);
        out.put("contentType", contentType);
        out.put("signature", sign(bytes, contentType));
        out.put("error", null);
        return out;
    }

    /** A new {@code audio} object with {@code status}, the option's format and voice, and nothing synthesized yet. */
    static Map<String, Object> result(String status, BriefingInput.Audio audio) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", status);
        out.put("mediaId", null);
        out.put("format", audio.format());
        out.put("voice", audio.voice());
        out.put("characters", null);
        out.put("truncated", false);
        out.put("error", null);
        return out;
    }

    private static Map<String, Object> failed(Map<String, Object> out, String error) {
        out.put("status", FAILED);
        out.put("error", error == null ? "speech synthesis failed" : error);
        return out;
    }

    private Duration retentionWindow() {
        try {
            RetentionResolver resolver = retention.getIfAvailable();
            if (resolver != null) {
                return resolver.settings().briefings();
            }
        } catch (RuntimeException e) {
            log.debug("Briefing retention unavailable: {}", e.getMessage());
        }
        return DEFAULT_RETENTION;
    }

    /** The detached JWS over the audio bytes (prompt 109), or null when signing is unavailable or fails. */
    private String sign(byte[] bytes, String contentType) {
        try {
            JwsSigner jws = signer.getIfAvailable();
            return jws == null ? null : jws.signDetached(bytes, contentType);
        } catch (RuntimeException e) {
            log.warn("Briefing audio could not be signed: {}", e.getMessage());
            return null;
        }
    }
}
