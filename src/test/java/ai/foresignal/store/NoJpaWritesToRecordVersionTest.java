package ai.foresignal.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

/**
 * Invariant ING-6 (Core-Data-Model I9): {@code record_version} is changed only by native SQL, never
 * through JPA.
 */
class NoJpaWritesToRecordVersionTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");

    /** Calls that would write through a Spring Data repository. */
    private static final Pattern WRITE_CALL = Pattern.compile(
            "recordVersion\\w*\\s*\\.\\s*(save|saveAll|saveAndFlush|saveAllAndFlush|delete\\w*)\\s*\\(");

    @Test
    void theEntityIsImmutableAndHasNoSetters() {
        assertThat(RecordVersionEntity.class.isAnnotationPresent(Immutable.class)).isTrue();
        assertThat(Arrays.stream(RecordVersionEntity.class.getDeclaredMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .map(Method::getName))
                .noneMatch(name -> name.startsWith("set"));
        assertThat(Arrays.stream(RecordVersionEntity.class.getDeclaredConstructors())
                        .filter(c -> Modifier.isPublic(c.getModifiers())))
                .isEmpty();
    }

    @Test
    void noMainCodeWritesThroughTheRepository() throws IOException {
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            offenders = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(NoJpaWritesToRecordVersionTest::writesThroughRepository)
                    .toList();
        }

        assertThat(offenders).isEmpty();
    }

    @Test
    void noMainCodeUpdatesTheEntityWithJpql() throws IOException {
        Pattern jpqlWrite = Pattern.compile("(?i)(update|delete\\s+from)\\s+RecordVersionEntity");
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            offenders = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> jpqlWrite.matcher(read(p)).find())
                    .toList();
        }

        assertThat(offenders).isEmpty();
    }

    private static boolean writesThroughRepository(Path file) {
        return WRITE_CALL.matcher(read(file)).find();
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
