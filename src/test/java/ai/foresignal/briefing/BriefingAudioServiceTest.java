package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.TtsService;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.media.MediaStore;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

/** {@link BriefingAudioService} (prompt 141) with a mocked {@code tts} role and media store. */
class BriefingAudioServiceTest {

    static final byte[] PART = {(byte) 0xFF, (byte) 0xFB, 1};

    final MutableClock clock = new MutableClock(Instant.parse("2026-10-18T07:00:00Z"));
    final TtsService tts = mock(TtsService.class);
    final MediaStore media = mock(MediaStore.class);
    @SuppressWarnings("unchecked")
    final ObjectProvider<RetentionResolver> retention = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    final ObjectProvider<JwsSigner> signer = mock(ObjectProvider.class);
    final JwsSigner jws = mock(JwsSigner.class);
    BriefingAudioService service;
    final List<List<String>> requested = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(tts.configured()).thenReturn(true);
        when(tts.synthesize(anyList(), any(), any())).thenAnswer(inv -> {
            List<String> chunks = inv.getArgument(0);
            requested.add(chunks);
            return new TtsService.Speech(chunks.stream().map(c -> PART).toList(), "speech", "kokoro",
                    inv.getArgument(1), inv.getArgument(2), chunks.stream().mapToLong(String::length).sum());
        });
        when(media.store(any(MediaInput.class))).thenReturn(new MediaRef("01JB", "ab".repeat(32), "audio/mpeg", 0, 0, 3));
        when(signer.getIfAvailable()).thenReturn(jws);
        when(jws.signDetached(any(byte[].class), eq("audio/mpeg"))).thenReturn("hdr..sig");
        service = new BriefingAudioService(tts, media, retention, signer, clock);
    }

    static String paragraphs(int count) {
        return String.join("\n\n", java.util.Collections.nCopies(count, "A calm sentence about the weather. ".repeat(20)));
    }

    @Test
    @DisplayName("mp3: long text in chunks of at most 4,000 characters, parts concatenated, stored as audio and signed")
    void mp3Chunks() {
        Map<String, Object> out = service.synthesize("Morning", paragraphs(20), new BriefingInput.Audio("alloy", "mp3"));

        assertThat(out).containsEntry("status", "ok").containsEntry("mediaId", "01JB").containsEntry("truncated", false)
                .containsEntry("signature", "hdr..sig").containsEntry("contentType", "audio/mpeg")
                .containsEntry("bytes", (long) PART.length * requested.getFirst().size());
        assertThat(requested.getFirst()).hasSizeGreaterThan(1)
                .allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(4000));
        assertThat(requested.getFirst().getFirst()).startsWith("Morning.");
        ArgumentCaptor<MediaInput> input = ArgumentCaptor.forClass(MediaInput.class);
        verify(media).store(input.capture());
        assertThat(input.getValue().role()).isEqualTo(MediaRole.AUDIO);
        assertThat(input.getValue().contentType()).isEqualTo("audio/mpeg");
        assertThat(input.getValue().expiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(180)));
        assertThat(input.getValue().bytes()).hasSize(PART.length * requested.getFirst().size());
    }

    @Test
    @DisplayName("opus: text over 4,000 characters is truncated to one request with the spoken note")
    void opusTruncated() {
        when(media.store(any(MediaInput.class))).thenReturn(new MediaRef("01JC", "cd".repeat(32), "audio/ogg", 0, 0, 3));
        when(jws.signDetached(any(byte[].class), eq("audio/ogg"))).thenReturn("o..sig");

        Map<String, Object> out = service.synthesize("Morning", paragraphs(20), new BriefingInput.Audio("alloy", "opus"));

        assertThat(out).containsEntry("status", "ok").containsEntry("truncated", true).containsEntry("format", "opus")
                .containsEntry("contentType", "audio/ogg").containsEntry("signature", "o..sig");
        assertThat(requested.getFirst()).singleElement().satisfies(c -> {
            assertThat(c.length()).isLessThanOrEqualTo(4000);
            assertThat(c).endsWith(SpeechText.TRUNCATION_NOTE);
        });
    }

    @Test
    @DisplayName("mp3 beyond 25 requests is truncated with the note")
    void mp3Cap() {
        service.synthesize("Long", paragraphs(200), new BriefingInput.Audio("alloy", "mp3"));
        assertThat(requested.getFirst()).hasSizeLessThanOrEqualTo(BriefingAudioService.MAX_CHUNKS);
        assertThat(requested.getFirst().getLast()).endsWith(SpeechText.TRUNCATION_NOTE);
    }

    @Test
    @DisplayName("skipped without a tts provider or text; failed on provider, budget and storage errors; never throws")
    void failures() {
        when(tts.configured()).thenReturn(false);
        assertThat(service.synthesize("T", "b", new BriefingInput.Audio("v", "mp3"))).containsEntry("status", "skipped");
        when(tts.configured()).thenReturn(true);
        assertThat(service.synthesize(null, "", new BriefingInput.Audio("v", "mp3"))).containsEntry("status", "skipped");

        doThrow(new LlmProviderException(LlmProviderException.Kind.OVERLOADED, "speech", "HTTP 503"))
                .when(tts).synthesize(anyList(), any(), any());
        assertThat(service.synthesize("T", "b", new BriefingInput.Audio("v", "mp3")))
                .containsEntry("status", "failed").containsEntry("error", "HTTP 503");

        doThrow(new LlmBudgetExceededException("speech", "no characters left", Duration.ofHours(1)))
                .when(tts).synthesize(anyList(), any(), any());
        assertThat(service.synthesize("T", "b", new BriefingInput.Audio("v", "mp3")))
                .containsEntry("status", "failed");

        doReturn(new TtsService.Speech(List.of(PART), "speech", "m", "v", "mp3", 1))
                .when(tts).synthesize(anyList(), any(), any());
        when(media.store(any(MediaInput.class))).thenThrow(new IllegalStateException("disk full"));
        Map<String, Object> stored = service.synthesize("T", "b", new BriefingInput.Audio("v", "mp3"));
        assertThat(stored).containsEntry("status", "failed").containsEntry("mediaId", null);
        assertThat((String) stored.get("error")).contains("disk full");
    }

    @Test
    @DisplayName("without a signer the audio is stored unsigned")
    void unsigned() {
        when(signer.getIfAvailable()).thenReturn(null);
        Map<String, Object> out = service.synthesize("T", "body", new BriefingInput.Audio("v", "mp3"));
        assertThat(out).containsEntry("status", "ok").containsEntry("signature", null);
        verify(jws, never()).signDetached(any(byte[].class), any());
    }
}
