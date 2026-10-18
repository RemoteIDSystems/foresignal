package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code foresignal login}: the configuration file, its permissions, profiles and environment overrides. */
class LoginCommandTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();

    private static boolean posix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    @Test
    void loginWritesTheProfileWithOwnerOnlyPermissions() throws Exception {
        assumeTrue(posix(), "POSIX permissions only");
        Path dir = tmp.resolve("cfg");
        CliHarness cli = new CliHarness(dir);

        int code = cli.run("login", "--url", "https://foresignal.example/", "--token", "fsr_secret-token");

        assertEquals(ExitCodes.OK, code, cli.err());
        Path file = dir.resolve("config.json");
        assertTrue(Files.exists(file));
        assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
        assertEquals("rwx------", PosixFilePermissions.toString(Files.getPosixFilePermissions(dir)));
        JsonNode config = json.readTree(Files.readString(file));
        assertEquals("default", config.path("currentProfile").asString());
        assertEquals("https://foresignal.example", config.path("profiles").path("default").path("url").asString());
        assertEquals("fsr_secret-token", config.path("profiles").path("default").path("token").asString());
        assertFalse(cli.out().contains("fsr_secret-token"), "the token is never printed");
        assertFalse(cli.err().contains("fsr_secret-token"), "the token is never printed");
        assertTrue(cli.out().contains("Saved profile 'default'"));
        assertFalse(Files.exists(dir.resolve("config.json.tmp")));
    }

    @Test
    void reLoginTightensAFileThatWasTooOpen() throws Exception {
        assumeTrue(posix(), "POSIX permissions only");
        Path dir = Files.createDirectories(tmp.resolve("cfg"));
        Path file = dir.resolve("config.json");
        Files.writeString(file, "{\"profiles\":{}}");
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));
        CliHarness cli = new CliHarness(dir);

        assertEquals(ExitCodes.OK, cli.run("login", "--url", "http://localhost:1", "--token", "fsr_x"));

        assertTrue(cli.err().contains("readable by other users"), cli.err());
        assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
    }

    @Test
    void namedProfilesAreKeptSideBySideAndTheLastLoginIsCurrent() throws Exception {
        Path dir = tmp.resolve("cfg");
        CliHarness cli = new CliHarness(dir);
        assertEquals(ExitCodes.OK, cli.run("login", "--url", "http://a.example", "--token", "fsr_a"));
        assertEquals(ExitCodes.OK, cli.run("login", "--profile", "prod", "--url", "http://b.example",
                "--token", "fsr_b"));

        JsonNode config = json.readTree(Files.readString(dir.resolve("config.json")));
        assertEquals("prod", config.path("currentProfile").asString());
        assertEquals("http://a.example", config.path("profiles").path("default").path("url").asString());
        assertEquals("http://b.example", config.path("profiles").path("prod").path("url").asString());
    }

    @Test
    void jsonOutputOfLoginNamesTheFileButNotTheToken() throws Exception {
        CliHarness cli = new CliHarness(tmp.resolve("cfg"));
        assertEquals(ExitCodes.OK, cli.run("login", "-o", "json", "--url", "http://a.example", "--token", "fsr_q"));
        JsonNode out = json.readTree(cli.out());
        assertEquals("default", out.path("profile").asString());
        assertEquals("http://a.example", out.path("url").asString());
        assertFalse(out.has("token"));
        assertFalse(cli.out().contains("fsr_q"));
    }

    @Test
    void theSavedProfileIsUsedWithItsToken() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(okJson("{\"version\":\"1.2.3\",\"status\":\"ok\"}")));
        CliHarness cli = new CliHarness(tmp.resolve("cfg"));
        assertEquals(ExitCodes.OK, cli.run("login", "--url", wm.baseUrl(), "--token", "fsr_profile"));

        assertEquals(ExitCodes.OK, cli.run("system"), cli.err());

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/system"))
                .withHeader("Authorization", equalTo("Bearer fsr_profile")));
        assertTrue(cli.out().contains("1.2.3"));
        assertFalse(cli.out().contains("fsr_profile"));
    }

    @Test
    void environmentVariablesOverrideTheProfile() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(okJson("{\"version\":\"9\"}")));
        CliHarness cli = new CliHarness(tmp.resolve("cfg"));
        assertEquals(ExitCodes.OK, cli.run("login", "--url", "http://127.0.0.1:9", "--token", "fsr_profile"));
        cli.env(ForesignalCli.ENV_URL, wm.baseUrl()).env(ForesignalCli.ENV_TOKEN, "fsr_env");

        assertEquals(ExitCodes.OK, cli.run("system"), cli.err());

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/system")).withHeader("Authorization", equalTo("Bearer fsr_env")));
    }

    @Test
    void theUrlFlagBeatsTheEnvironment() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(okJson("{\"version\":\"9\"}")));
        CliHarness cli = CliHarness.against(tmp.resolve("cfg"), "http://127.0.0.1:9", null);

        assertEquals(ExitCodes.OK, cli.run("system", "--url", wm.baseUrl()), cli.err());

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/system")));
    }

    @Test
    void loginNeedsAnHttpUrlAndAToken() {
        CliHarness cli = new CliHarness(tmp.resolve("cfg"));
        assertEquals(ExitCodes.USAGE, cli.run("login", "--token", "fsr_x"));
        assertTrue(cli.err().contains("--url"));
        assertEquals(ExitCodes.USAGE, cli.run("login", "--url", "ftp://x", "--token", "fsr_x"));
        assertEquals(ExitCodes.USAGE, cli.run("login", "--url", "http://x"));
        assertFalse(Files.exists(tmp.resolve("cfg").resolve("config.json")));
    }

    @Test
    void anUnknownProfileIsAUsageError() {
        CliHarness cli = new CliHarness(tmp.resolve("cfg"));
        assertEquals(ExitCodes.USAGE, cli.run("system", "--profile", "nope"));
        assertTrue(cli.err().contains("No profile 'nope'"), cli.err());
    }
}
