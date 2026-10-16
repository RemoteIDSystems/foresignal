package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guard ({@code docs/Conventions.md}: one {@code EgressPolicy}): {@link LicenceEgressGuard} is an internal
 * delegate of {@code ai.foresignal.security.EgressPolicy}, never an egress entry point. The 110 review found
 * {@code SinkRecordGate} calling it directly, which skipped the descriptor {@code LOCAL_ONLY} egress and the
 * operator overrides; this test fails when any other main class names it.
 *
 * <p>{@code ai.foresignal.export.ExportLicenceService} (prompt 111) is the one remaining caller, listed here
 * until it moves to {@code EgressPolicy.decide(EgressPath.EXPORT, …, EgressContext.export(…))}; remove it from
 * {@link #ALLOWED} in the same change.
 */
class LicenceEgressGuardUsageTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Pattern USE = Pattern.compile("\\bLicenceEgressGuard\\b");

    /** The files that may name the guard: itself, its one owner and the pending 111 caller. */
    private static final Set<Path> ALLOWED = Set.of(
            Path.of("src/main/java/ai/foresignal/provenance/LicenceEgressGuard.java"),
            Path.of("src/main/java/ai/foresignal/security/EgressPolicy.java"),
            Path.of("src/main/java/ai/foresignal/export/ExportLicenceService.java"));

    @Test
    @DisplayName("only EgressPolicy (and the pending 111 export caller) uses LicenceEgressGuard")
    void onlyEgressPolicyUsesTheGuard() throws IOException {
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            offenders = files.filter(f -> f.toString().endsWith(".java"))
                    .filter(f -> ALLOWED.stream().noneMatch(a -> a.normalize().equals(f.normalize())))
                    .filter(f -> USE.matcher(read(f)).find())
                    .toList();
        }
        assertThat(offenders).isEmpty();
        assertThat(USE.matcher(read(Path.of("src/main/java/ai/foresignal/security/EgressPolicy.java"))).find())
                .as("EgressPolicy owns the licence table").isTrue();
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
