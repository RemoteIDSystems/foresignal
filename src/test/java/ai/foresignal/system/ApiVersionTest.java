package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The release version agrees everywhere (prompt 144): {@code info.version} of {@code docs/OpenAPI.json}, the frozen
 * v1.0.0 contract the API-compatibility gate compares against, {@code GET /v1/system}'s {@code apiVersion} (read by
 * {@link ApiVersion} from the copy on the classpath) and the Gradle project version are all {@value #RELEASE}.
 *
 * <p>NP-001 moves the release to 2.1.0 (Automated Newspaper) and adds the check that the spec's changelog ends with an
 * entry for the release, so a version bump cannot land without saying what it added.
 *
 * <p>NP-007 activates the Automated Newspaper: every operation an {@code NP-} prompt authored is {@code implemented}
 * (none is left {@code planned}), and a {@code 2.1.x} changelog entry names each of them.
 *
 * <p>NP-008 moves the release to 2.1.1 (newspaper editions: ownership and self-service) and adds four more
 * {@code NP-} operations. Because those are named in the 2.1.1 changelog note while the first seven are named only in
 * the 2.1.0 note, the activation check concatenates every {@code 2.1.x} note, not just the release's.
 */
class ApiVersionTest {

    /** The release this build is. */
    static final String RELEASE = "2.1.1";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern GRADLE_VERSION = Pattern.compile("(?m)^version\\s*=\\s*\"([^\"]+)\"");

    @Test
    @DisplayName("docs/OpenAPI.json info.version is the release version")
    void authoredSpecVersion() throws IOException {
        assertThat(infoVersion(Path.of("docs", "OpenAPI.json"))).isEqualTo(RELEASE);
    }

    @Test
    @DisplayName("the frozen contract docs/api/v1.0.0/OpenAPI.json is version 1.0.0")
    void frozenSpecVersion() throws IOException {
        assertThat(infoVersion(Path.of("docs", "api", "v1.0.0", "OpenAPI.json"))).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("/v1/system apiVersion (ApiVersion, from the classpath copy) is the release version")
    void systemApiVersion() {
        assertThat(new ApiVersion(JSON).version()).isEqualTo(RELEASE);
    }

    @Test
    @DisplayName("the Gradle project version is the release version")
    void gradleVersion() throws IOException {
        Matcher matcher = GRADLE_VERSION.matcher(Files.readString(Path.of("build.gradle.kts")));
        assertThat(matcher.find()).as("a version = \"...\" line in build.gradle.kts").isTrue();
        assertThat(matcher.group(1)).isEqualTo(RELEASE);
    }

    @Test
    @DisplayName("the newest info.x-foresignal-changelog entry of docs/OpenAPI.json is the release version")
    void changelogEndsWithTheRelease() throws IOException {
        JsonNode changelog = JSON.readTree(Files.readString(Path.of("docs", "OpenAPI.json")))
                .get("info").get("x-foresignal-changelog");
        assertThat(changelog.isArray() && !changelog.isEmpty()).as("a non-empty changelog").isTrue();
        assertThat(changelog.get(changelog.size() - 1).get("version").asString()).isEqualTo(RELEASE);
    }

    @Test
    @DisplayName("2.1.x is active: no NP- operation is planned, and a 2.1.x changelog note names every one of them")
    void newspaperOperationsAreActive() throws IOException {
        JsonNode spec = JSON.readTree(Files.readString(Path.of("docs", "OpenAPI.json")));
        java.util.List<String> planned = new java.util.ArrayList<>();
        java.util.List<String> unnamed = new java.util.ArrayList<>();
        String note = "";
        for (JsonNode entry : spec.get("info").get("x-foresignal-changelog")) {
            // Every 2.1.x note, not just the release's: the 2.1.0 operations are named only in the 2.1.0 note, the
            // 2.1.1 operations only in the 2.1.1 note, and all eleven must validate at the 2.1.1 release.
            if (entry.get("version").asString().startsWith("2.1.")) {
                note += entry.get("note").asString();
            }
        }
        int count = 0;
        for (String path : spec.get("paths").propertyNames()) {
            JsonNode item = spec.get("paths").get(path);
            for (String method : item.propertyNames()) {
                JsonNode op = item.get(method);
                JsonNode prompt = op.isObject() ? op.get("x-foresignal-prompt") : null;
                if (prompt == null || !prompt.asString().startsWith("NP-")) {
                    continue;
                }
                count++;
                String id = op.get("operationId").asString();
                if (!"implemented".equals(op.get("x-foresignal-status").asString())) {
                    planned.add(id);
                }
                if (!note.contains(id)) {
                    unnamed.add(id);
                }
            }
        }
        assertThat(count).as("the newspaper operations").isGreaterThanOrEqualTo(11);
        assertThat(planned).as("NP- operations still planned").isEmpty();
        assertThat(unnamed).as("NP- operations no 2.1.x changelog note names").isEmpty();
    }

    private static String infoVersion(Path spec) throws IOException {
        return JSON.readTree(Files.readString(spec)).get("info").get("version").asString();
    }
}
