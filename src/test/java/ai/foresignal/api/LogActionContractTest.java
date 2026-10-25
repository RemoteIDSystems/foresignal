package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.social.narrative.NarrativeVocabulary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The log-action lists in docs/OpenAPI.json say every action the server writes. The admin app's generated client
 * failed a whole intelligence event on {@code upgraded}, an action the code wrote and the contract never listed.
 * This scans the code that writes each log for its action literals and checks that every one is declared, and that
 * each list is {@code x-extensible-enum}, since the lists grow as the server adds actions.
 */
class LogActionContractTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Path MAIN = Path.of("src", "main", "java", "ai", "foresignal");

    @Test
    @DisplayName("IntelEventLogEntry.action lists every action the intel package writes, upgraded included")
    void intelEventLog() throws IOException {
        Set<String> written = literals(MAIN.resolve("intel"),
                // new IntelEventLogEntity(id, at, actor, "action")
                Pattern.compile("new IntelEventLogEntity\\([^;]*?,\\s*\"([a-z_]+)\"\\s*\\)"),
                // log(event, now, actor, "action", after)
                Pattern.compile("\\blog\\(\\w+,\\s*now,\\s*actor,\\s*\"([a-z_]+)\""),
                // String action = created ? "created" : ...
                Pattern.compile("String action = [^;]*?\"([a-z_]+)\""),
                Pattern.compile("String action = [^;]*?\"[a-z_]+\"[^;]*?\"([a-z_]+)\""),
                Pattern.compile("String action = [^;]*?\"[a-z_]+\"[^;]*?\"[a-z_]+\"[^;]*?\"([a-z_]+)\""));

        assertThat(written).contains("created", "edited", "member_added", "merged", "reopened", "status", "upgraded");
        assertThat(declared("IntelEventLogEntry")).containsAll(written);
    }

    @Test
    @DisplayName("SituationTimelineEntry.action lists every action the situation package writes, edited included")
    void situationLog() throws IOException {
        Set<String> written = literals(MAIN.resolve("situation"),
                // new SituationLogEntity(id, at, actor, "action")
                Pattern.compile("new SituationLogEntity\\([^;]*?,\\s*\"([a-z_]+)\"\\s*\\)"),
                // entries.add(new String[] {"renamed", before, after})
                Pattern.compile("new String\\[\\] \\{\"([a-z_]+)\""),
                // finish(situation, before, actor, "action", ...)
                Pattern.compile("\\bfinish\\([^;\"]*?\"([a-z_]+)\""));

        assertThat(written).contains("created", "edited", "member_added", "member_removed", "merged", "split",
                "status", "severity", "renamed", "theme", "brief");
        assertThat(declared("SituationTimelineEntry")).containsAll(written);
    }

    @Test
    @DisplayName("NarrativeLogEntry.action is NarrativeVocabulary.LOG_ACTIONS, and every action written is in it")
    void narrativeLog() throws IOException {
        Set<String> written = literals(MAIN.resolve("social"),
                // store.log(id, "action", actor, ...) and narratives.log(n.id(), "action", ...)
                Pattern.compile("\\.log\\([\\w.()]+,\\s*\"([a-z-]+)\""));

        assertThat(written).contains("created", "merged", "merged-into", "split", "split-from", "state-changed",
                "status-changed", "title-changed");
        assertThat(NarrativeVocabulary.LOG_ACTIONS).containsAll(written);
        assertThat(declared("NarrativeLogEntry")).containsExactlyInAnyOrderElementsOf(NarrativeVocabulary.LOG_ACTIONS);
    }

    /** The string values of a schema's {@code action}, which must be an open ({@code x-extensible-enum}) list. */
    private static List<String> declared(String schema) throws IOException {
        JsonNode action = JSON.readTree(Files.readString(Path.of("docs", "OpenAPI.json"))).get("components")
                .get("schemas").get(schema).get("properties").get("action");
        assertThat(action.has("enum")).as(schema + ".action is not a closed enum").isFalse();
        assertThat(action.has("x-extensible-enum")).as(schema + ".action is x-extensible-enum").isTrue();
        List<String> values = new ArrayList<>();
        action.get("x-extensible-enum").forEach(v -> values.add(v.asString()));
        return values;
    }

    /** Every first capture of the patterns across the Java files under {@code dir}. */
    private static Set<String> literals(Path dir, Pattern... patterns) throws IOException {
        Set<String> out = new TreeSet<>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String text = Files.readString(file);
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        out.add(matcher.group(1));
                    }
                }
            }
        }
        return out;
    }
}
