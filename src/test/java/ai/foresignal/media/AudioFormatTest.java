package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.ImageRejectedException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** {@link AudioFormat} (prompt 141): declared types, magic bytes and the check of a staged file. */
class AudioFormatTest {

    @TempDir Path dir;

    @Test
    @DisplayName("declared types: audio/mpeg and audio/mp3 are MP3, audio/ogg and audio/opus are Ogg; others none")
    void declared() {
        assertThat(AudioFormat.ofContentType("audio/mpeg")).contains(AudioFormat.MP3);
        assertThat(AudioFormat.ofContentType("Audio/MP3; charset=x")).contains(AudioFormat.MP3);
        assertThat(AudioFormat.ofContentType("audio/ogg")).contains(AudioFormat.OGG);
        assertThat(AudioFormat.ofContentType("audio/opus")).contains(AudioFormat.OGG);
        assertThat(AudioFormat.ofContentType("image/png")).isEmpty();
        assertThat(AudioFormat.ofContentType(null)).isEmpty();
        assertThat(AudioFormat.MP3.extension()).isEqualTo("mp3");
        assertThat(AudioFormat.OGG.contentType()).isEqualTo("audio/ogg");
    }

    @Test
    @DisplayName("magic bytes: ID3, an MPEG frame sync, OggS")
    void sniff() {
        assertThat(AudioFormat.sniff(new byte[] {'I', 'D', '3', 4}, 4)).contains(AudioFormat.MP3);
        assertThat(AudioFormat.sniff(new byte[] {(byte) 0xFF, (byte) 0xF3, 0, 0}, 4)).contains(AudioFormat.MP3);
        assertThat(AudioFormat.sniff(new byte[] {'O', 'g', 'g', 'S'}, 4)).contains(AudioFormat.OGG);
        assertThat(AudioFormat.sniff(new byte[] {(byte) 0x89, 'P', 'N', 'G'}, 4)).isEmpty();
        assertThat(AudioFormat.sniff(new byte[] {'O', 'g'}, 2)).isEmpty();
    }

    @Test
    @DisplayName("inspect: the declared type must match the content; a mismatch is an ImageRejectedException")
    void inspect() throws Exception {
        Path mp3 = Files.write(dir.resolve("a.part"), new byte[] {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 0});
        Path ogg = Files.write(dir.resolve("b.part"), new byte[] {'O', 'g', 'g', 'S', 0, 2});
        Path html = Files.write(dir.resolve("c.part"), "<html>".getBytes());

        assertThat(AudioFormat.inspect(mp3, "audio/mpeg")).isEqualTo(AudioFormat.MP3);
        assertThat(AudioFormat.inspect(ogg, "audio/ogg")).isEqualTo(AudioFormat.OGG);
        assertThatThrownBy(() -> AudioFormat.inspect(ogg, "audio/mpeg")).isInstanceOf(ImageRejectedException.class);
        assertThatThrownBy(() -> AudioFormat.inspect(html, "audio/mpeg")).isInstanceOf(ImageRejectedException.class);
        assertThatThrownBy(() -> AudioFormat.inspect(mp3, "image/png")).isInstanceOf(ImageRejectedException.class);
    }
}
