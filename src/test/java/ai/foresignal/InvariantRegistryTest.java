package ai.foresignal;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.openapi.OpenApiSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The invariants registry ({@code docs/Invariants.md}) against the test tree (checkpoint 124).
 *
 * <p>Every invariant whose owner prompt is at or before the newest completed prompt must name at least one test class
 * that exists under {@code src/test/java}, and every test class a row names ({@code `SomethingTest`},
 * {@code `SomethingIT`}) must exist, except those listed in {@link #LATER} with the later prompt that writes them.
 */
class InvariantRegistryTest {

    private static final Path REGISTRY = Path.of("docs/Invariants.md");
    private static final Path TESTS = Path.of("src/test/java");

    /** A test-class name in backticks, optionally followed by {@code .method} or {@code #method}. */
    private static final Pattern TEST_NAME = Pattern.compile("`([A-Z][A-Za-z0-9]*(?:Test|Tests|IT))(?:[.#][A-Za-z0-9_]+)?`");

    /** Tests a row names that a later prompt writes: class name → that prompt. */
    static final Map<String, Integer> LATER = Map.of(
            // LED-3 (122): assessments are linted by the analysis cycles of prompt 128 (122's notes for 128).
            "AssessmentLintIT", 128);

    /**
     * One registry row.
     *
     * @param id the invariant id
     * @param owner the owner prompt
     * @param tests the test classes the row names
     */
    record Row(String id, int owner, List<String> tests) {}

    static List<Row> rows() throws IOException {
        List<Row> rows = new ArrayList<>();
        for (String line : Files.readAllLines(REGISTRY)) {
            if (!line.startsWith("| ") || line.startsWith("| Id ")) {
                continue;
            }
            String[] cells = line.substring(1).split("\\|");
            if (cells.length < 5) {
                continue;
            }
            String id = cells[0].trim();
            Matcher owner = Pattern.compile("^(\\d{3})").matcher(cells[2].trim());
            if (!id.matches("[A-Z]+-\\d+") || !owner.find()) {
                continue;
            }
            List<String> tests = new ArrayList<>();
            Matcher name = TEST_NAME.matcher(cells[4]);
            while (name.find()) {
                tests.add(name.group(1));
            }
            rows.add(new Row(id, Integer.parseInt(owner.group(1)), tests));
        }
        return rows;
    }

    static Set<String> testClasses() throws IOException {
        Set<String> names = new TreeSet<>();
        try (Stream<Path> files = Files.walk(TESTS)) {
            files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".java"))
                    .forEach(n -> names.add(n.substring(0, n.length() - ".java".length())));
        }
        return names;
    }

    @Test
    @DisplayName("the registry parses: every row has an id, an owner prompt and at least one named test")
    void registryParses() throws IOException {
        List<Row> rows = rows();
        assertThat(rows).as("invariant rows").hasSizeGreaterThan(50);
        assertThat(rows).extracting(Row::id).doesNotHaveDuplicates();
        assertThat(rows).filteredOn(r -> r.tests().isEmpty()).extracting(Row::id).as("rows naming no test").isEmpty();
    }

    @Test
    @DisplayName("every invariant owned by a completed prompt names at least one existing test")
    void ownedInvariantsHaveAnExistingTest() throws IOException {
        int current = OpenApiSpec.currentPrompt();
        Set<String> classes = testClasses();
        List<String> uncovered = new ArrayList<>();
        for (Row row : rows()) {
            if (row.owner() <= current && row.tests().stream().noneMatch(classes::contains)) {
                uncovered.add(row.id() + " (owner " + row.owner() + "): " + row.tests());
            }
        }
        assertThat(uncovered).as("invariants without an existing test (current prompt %d)", current).isEmpty();
    }

    @Test
    @DisplayName("every test a row of a completed prompt names exists, unless a later prompt writes it")
    void namedTestsExist() throws IOException {
        int current = OpenApiSpec.currentPrompt();
        Set<String> classes = testClasses();
        Map<String, List<String>> missing = new LinkedHashMap<>();
        for (Row row : rows()) {
            if (row.owner() > current) {
                continue;
            }
            for (String test : row.tests()) {
                boolean later = LATER.containsKey(test) && LATER.get(test) > current;
                if (!classes.contains(test) && !later) {
                    missing.computeIfAbsent(row.id(), k -> new ArrayList<>()).add(test);
                }
            }
        }
        assertThat(missing).as("named test classes that do not exist").isEmpty();
    }
}
