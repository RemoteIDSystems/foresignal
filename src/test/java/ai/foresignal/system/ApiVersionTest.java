package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The release version agrees everywhere (prompt 144): {@code info.version} of {@code docs/OpenAPI.json}, the frozen
 * v1.0.0 contract the API-compatibility gate compares against, {@code GET /v1/system}'s {@code apiVersion} (read by
 * {@link ApiVersion} from the copy on the classpath) and the Gradle project version are all {@value #RELEASE}.
 */
class ApiVersionTest {

    /** The release this build is. */
    static final String RELEASE = "1.3.0";

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

    private static String infoVersion(Path spec) throws IOException {
        return JSON.readTree(Files.readString(spec)).get("info").get("version").asString();
    }
}
