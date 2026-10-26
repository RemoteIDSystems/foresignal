package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The external-disk archive tier (prompt R2-016): checksummed offload, corruption caught, restore round trip. */
class ExternalDiskArchiveTierTest {

    @TempDir Path root;
    @TempDir Path work;

    ExternalDiskArchiveTier tier;
    final ArchiveSettings settings = ArchiveSettings.defaults();

    @BeforeEach
    void setUp() {
        ArchiveService archive = mock(ArchiveService.class);
        when(archive.root(any())).thenReturn(root);
        tier = new ExternalDiskArchiveTier(archive);
    }

    private ArchiveSpec spec(String key, String content) throws Exception {
        Path file = work.resolve(key.replace('/', '_'));
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return new ArchiveSpec(key, file, ExportManifests.sha256(file), Files.size(file));
    }

    @Test
    @DisplayName("offload writes the file under the root and returns a ref that verifies")
    void offload() throws Exception {
        ArchiveSpec spec = spec("segments/A/standard-src.ndjson.gz", "{\"seq\":1}\n");
        ArchiveRef ref = tier.offload(settings, spec);

        Path stored = root.resolve("segments/A/standard-src.ndjson.gz");
        assertThat(stored).exists();
        assertThat(Files.readString(stored)).isEqualTo("{\"seq\":1}\n");
        assertThat(ref.target()).isEqualTo("local");
        assertThat(ref.location()).isEqualTo(stored.toString());
        assertThat(ref.sha256()).isEqualTo(spec.sha256());
        assertThat(tier.verify(settings, ref)).isTrue();
        try (var leftovers = Files.list(stored.getParent())) {
            assertThat(leftovers.map(p -> p.getFileName().toString())).containsExactly("standard-src.ndjson.gz");
        }
    }

    @Test
    @DisplayName("verify catches a changed byte, a truncated file and a missing file")
    void verifyCatchesCorruption() throws Exception {
        ArchiveRef ref = tier.offload(settings, spec("segments/B/f.gz", "abcdef"));
        Path stored = root.resolve("segments/B/f.gz");

        Files.writeString(stored, "abcdeX");
        assertThat(tier.verify(settings, ref)).isFalse();
        Files.writeString(stored, "abc");
        assertThat(tier.verify(settings, ref)).isFalse();
        Files.delete(stored);
        assertThat(tier.verify(settings, ref)).isFalse();
        assertThatThrownBy(() -> tier.restore(settings, ref, work.resolve("out.gz")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("restore copies the file back and checks its SHA-256")
    void restoreRoundTrips() throws Exception {
        ArchiveRef ref = tier.offload(settings, spec("segments/C/f.gz", "payload"));
        Path into = work.resolve("restore/f.gz");

        ArchiveTier.Restored restored = tier.restore(settings, ref, into);

        assertThat(restored.ready()).isTrue();
        assertThat(restored.file()).isEqualTo(into);
        assertThat(Files.readString(into)).isEqualTo("payload");
        assertThat(ExportManifests.sha256(into)).isEqualTo(ref.sha256());
    }

    @Test
    @DisplayName("list finds the stored files under a prefix; delete removes one")
    void listAndDelete() throws Exception {
        ArchiveRef a = tier.offload(settings, spec("segments/D/a.gz", "a"));
        tier.offload(settings, spec("segments/D/b.gz", "bb"));
        tier.offload(settings, spec("segments/E/c.gz", "ccc"));

        List<ArchiveRef> listed = tier.list(settings, "segments/D/");
        assertThat(listed).extracting(ArchiveRef::key).containsExactly("segments/D/a.gz", "segments/D/b.gz");
        assertThat(listed).extracting(ArchiveRef::bytes).containsExactly(1L, 2L);

        tier.delete(settings, a);
        assertThat(tier.list(settings, "segments/D/")).extracting(ArchiveRef::key).containsExactly("segments/D/b.gz");
        tier.delete(settings, a);
    }

    @Test
    @DisplayName("a key that escapes the root is refused")
    void escapeRefused() throws Exception {
        ArchiveSpec spec = spec("../outside.gz", "x");
        assertThatThrownBy(() -> tier.offload(settings, spec)).isInstanceOf(IllegalArgumentException.class);
    }
}
