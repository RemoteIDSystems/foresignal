package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prompt 083's acceptance guard (PRC-2): Meshtastic's protobufs are GPL-3.0, so no {@code .proto} file and no Meshtastic
 * protobuf class or dependency exists anywhere in the repository; every Meshtastic path reads the firmware's JSON.
 */
class NoMeshtasticProtobufTest {

    private static final List<String> FORBIDDEN = List.of("org.meshtastic", "com.geeksville.mesh", "meshtastic.protobuf",
            "MeshProtos", "meshtastic-protobufs");

    @Test
    @DisplayName("no .proto file, and no Meshtastic protobuf package, class or artifact in sources or the build")
    void noProtobufs() throws IOException {
        assertThat(Files.isDirectory(Path.of("src", "main", "java"))).as("run from the repository root").isTrue();
        List<Path> roots = List.of(Path.of("src"), Path.of("contrib"), Path.of("gradle"), Path.of("build.gradle.kts"));
        try (Stream<Path> files = roots.stream().filter(Files::exists).flatMap(MeshtasticWalk::walk)) {
            List<Path> all = files.filter(Files::isRegularFile).toList();
            assertThat(all).filteredOn(p -> p.toString().endsWith(".proto")).isEmpty();
            for (Path file : all) {
                String name = file.toString();
                if (!(name.endsWith(".java") || name.endsWith(".kts") || name.endsWith(".toml") || name.endsWith(".py"))
                        || file.getFileName().toString().equals("NoMeshtasticProtobufTest.java")) {
                    continue;
                }
                String text = Files.readString(file);
                for (String token : FORBIDDEN) {
                    assertThat(text).as("%s mentions %s", file, token).doesNotContain(token);
                }
            }
        }
    }

    /** Walks a path, wrapping the checked exception. */
    private static final class MeshtasticWalk {

        static Stream<Path> walk(Path root) {
            try {
                return Files.walk(root);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
    }
}
