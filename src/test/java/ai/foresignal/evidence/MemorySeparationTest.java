package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MEM-1 (prompt 116): interpretation never edits observations. The evidence, observability and manifest layers
 * read {@code record_version}, {@code record_head} and {@code record_live} but never insert into, update or
 * delete from them; they write only their own tables.
 */
class MemorySeparationTest {

    private static final Pattern WRITE = Pattern.compile(
            "(?i)(insert\\s+into|update|delete\\s+from)\\s+(record_version|record_head|record_live|seq_clock)\\b");

    @Test
    @DisplayName("no class of the evidence, observability or manifest packages writes an observation table")
    void interpretationNeverWritesObservations() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (String pkg : List.of("evidence", "observability", "manifest")) {
            Path root = Path.of("src/main/java/ai/foresignal", pkg);
            assertThat(root).as(pkg + " exists").isDirectory();
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    if (WRITE.matcher(Files.readString(file)).find()) {
                        offenders.add(file.toString());
                    }
                }
            }
        }
        assertThat(offenders).as("classes writing observation tables").isEmpty();
    }

    @Test
    @DisplayName("the resolver reads record_version (observations are its input)")
    void resolverReadsObservations() throws IOException {
        String resolver = Files.readString(Path.of("src/main/java/ai/foresignal/evidence/EvidenceResolver.java"));
        assertThat(resolver).contains("FROM record_version");
    }
}
