package ai.elaro.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.elaro.api.error.InvalidInputException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Confinement to the inbox, including the case that matters most: a path that was valid when it was
 * configured and has since been replaced by a symlink.
 */
class InboxFilesTest {

    @TempDir Path tempDir;

    private Path root;
    private InboxFiles inbox;

    @BeforeEach
    void setUp() throws IOException {
        root = Files.createDirectories(tempDir.resolve("inbox"));
        inbox = new InboxFiles(root);
    }

    @Test
    void exposesAnAbsoluteNormalizedRoot() {
        assertThat(inbox.root()).isAbsolute().isEqualTo(root.toAbsolutePath().normalize());
    }

    @Test
    void acceptsPathsInsideTheRoot() throws IOException {
        Path file = Files.writeString(root.resolve("pass.json"), "{}");

        assertThat(inbox.requireInside(file)).isEqualTo(file.toRealPath());
    }

    @Test
    void rejectsPathsOutsideTheRoot() {
        assertThatThrownBy(() -> inbox.requireInside(tempDir.resolve("elsewhere")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("outside the inbox directory");
    }

    @Test
    void rejectsTraversal() {
        assertThatThrownBy(() -> inbox.requireInside(root.resolve("../escape")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("outside the inbox");
    }

    @Test
    @DisplayName("reads a regular file inside the inbox")
    void readsRegularFile() throws IOException {
        Files.writeString(root.resolve("data.json"), "{\"a\":1}");

        try (var in = inbox.open(root.resolve("data.json"))) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("{\"a\":1}");
        }
    }

    @Test
    @DisplayName("refuses a symlink outright, even one pointing inside the inbox")
    @DisabledOnOs(OS.WINDOWS)
    void refusesToReadSymlinks() throws IOException {
        Files.writeString(root.resolve("real.json"), "{}");
        Files.createSymbolicLink(root.resolve("link.json"), root.resolve("real.json"));

        assertThatThrownBy(() -> inbox.open(root.resolve("link.json")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("symbolic link");
    }

    @Test
    @DisplayName("a symlink planted after validation cannot redirect a read")
    @DisabledOnOs(OS.WINDOWS)
    void recheckDefeatsTimeOfCheckTimeOfUse() throws IOException {
        // The realistic attack: the path validated fine, then something writable replaced it.
        Path secret = Files.writeString(tempDir.resolve("secret.key"), "sensitive");
        Path planted = root.resolve("innocent.json");
        Files.createSymbolicLink(planted, secret);

        assertThatThrownBy(() -> inbox.open(planted)).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> inbox.requireInside(planted))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("outside the inbox");
    }

    @Test
    @DisplayName("listing returns regular files, sorted, and skips symlinks")
    @DisabledOnOs(OS.WINDOWS)
    void listsRegularFilesOnly() throws IOException {
        Files.writeString(root.resolve("b.json"), "{}");
        Files.writeString(root.resolve("a.json"), "{}");
        Files.createDirectories(root.resolve("subdir"));
        Files.createSymbolicLink(root.resolve("link.json"), tempDir.resolve("outside.json"));

        assertThat(inbox.listRegularFiles(root))
                .extracting(path -> path.getFileName().toString())
                .containsExactly("a.json", "b.json");
    }

    @Test
    @DisplayName("listing a directory outside the inbox is refused")
    void refusesToListOutside() {
        assertThatThrownBy(() -> inbox.listRegularFiles(tempDir))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void listsNothingFromAnEmptyInbox() throws IOException {
        assertThat(inbox.listRegularFiles(root)).isEmpty();
    }
}
