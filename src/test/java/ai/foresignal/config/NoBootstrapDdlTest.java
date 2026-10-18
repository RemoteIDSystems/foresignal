package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guard on schema ownership (prompt 144, Architecture §25): Flyway owns the schema, so no class in
 * {@code src/main/java} may carry {@code CREATE TABLE}, {@code CREATE INDEX} or {@code ALTER TABLE} SQL, except the
 * dynamic DDL that is meant to run at runtime. A new table, column or index goes into a new
 * {@code db/migration/V{n}__{description}.sql}, never into Java.
 *
 * <p>Comments are stripped before scanning, so Javadoc may still describe DDL.
 */
class NoBootstrapDdlTest {

    /** Where the main sources live, relative to the project directory (Gradle's test working directory). */
    static final Path MAIN_SOURCES = Path.of("src", "main", "java");

    /** The DDL that may appear, by source path (with {@code /} separators) and why. */
    static final Map<String, String> ALLOWED = Map.of(
            "ai/foresignal/store/history/PartitionManager.java",
            "record_version leaves: create, attach, detach and drop (Architecture §9)",
            "ai/foresignal/retention/HistoryDownsampler.java",
            "the downsampling rewrite of a record_version leaf: copy, swap and re-index (prompt 044)",
            "ai/foresignal/chronicle/ChronicleDdl.java",
            "chronicle_version leaves, one per chronicle, created on activation (prompt 131)",
            "ai/foresignal/search/EmbeddingSchemaManager.java",
            "the reindex's vector slots, whose width follows the embedding model (prompt 027)",
            "ai/foresignal/reference/ReferenceDataLoader.java",
            "the reference loader's ref_stage_* bulk-load working tables (prompt 040)",
            "ai/foresignal/export/GeoPackageWriter.java",
            "SQLite tables inside a GeoPackage export file, not PostgreSQL (prompt 111)",
            "ai/foresignal/raster/export/MbtilesWriter.java",
            "SQLite tables inside an MBTiles export file, not PostgreSQL (prompt 108)");

    private static final Pattern DDL = Pattern.compile(
            "\\b(CREATE\\s+(UNIQUE\\s+)?INDEX|CREATE\\s+(UNLOGGED\\s+)?TABLE|ALTER\\s+TABLE)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private static final Pattern LINE_COMMENT = Pattern.compile("(?m)^\\s*//.*$");

    @Test
    @DisplayName("no main source carries table or index DDL outside the dynamic-DDL allowlist")
    void noBootstrapDdl() {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String relative = MAIN_SOURCES.relativize(path).toString().replace('\\', '/');
                if (ALLOWED.containsKey(relative)) {
                    return;
                }
                Matcher matcher = DDL.matcher(withoutComments(read(path)));
                if (matcher.find()) {
                    offenders.add(relative + ": " + matcher.group());
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertThat(offenders)
                .as("DDL in Java outside the allowlist; put it in a new db/migration/V{n}__{description}.sql")
                .isEmpty();
    }

    @Test
    @DisplayName("every allowlisted file still exists and still holds DDL, so the allowlist cannot go stale")
    void allowlistIsCurrent() {
        for (String relative : ALLOWED.keySet()) {
            Path path = MAIN_SOURCES.resolve(relative);
            assertThat(path).as(relative).exists();
            assertThat(DDL.matcher(withoutComments(read(path))).find()).as(relative + " still holds DDL").isTrue();
        }
    }

    @Test
    @DisplayName("the scanner sees DDL in string literals and ignores it in comments")
    void scannerSeesLiteralsNotComments() {
        assertThat(DDL.matcher(withoutComments("String s = \"CREATE INDEX x ON t (a)\";")).find()).isTrue();
        assertThat(DDL.matcher(withoutComments("/** Runs CREATE INDEX CONCURRENTLY. */ int x;")).find()).isFalse();
        assertThat(DDL.matcher(withoutComments("  // alter table t add column c\nint x;")).find()).isFalse();
        assertThat(DDL.matcher(withoutComments("String s = \"\"\"\n  alter table t add c int\n  \"\"\";")).find())
                .isTrue();
    }

    static String withoutComments(String source) {
        return LINE_COMMENT.matcher(BLOCK_COMMENT.matcher(source).replaceAll(" ")).replaceAll("");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
