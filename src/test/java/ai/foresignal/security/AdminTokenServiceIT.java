package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.store.SystemSecretRepository;
import ai.foresignal.test.IntegrationTest;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class AdminTokenServiceIT {

    @Autowired private SystemSecretRepository secrets;
    @Autowired private TokenHasher hasher;

    private ListAppender<ILoggingEvent> logs;
    private Logger serviceLogger;

    @BeforeEach
    void captureLogs() {
        serviceLogger = (Logger) LoggerFactory.getLogger(AdminTokenService.class);
        logs = new ListAppender<>();
        logs.start();
        serviceLogger.addAppender(logs);
        serviceLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void releaseLogs() {
        serviceLogger.detachAppender(logs);
        logs.stop();
    }

    /** A service bound to a fresh, empty table, as if this were a first start. */
    private AdminTokenService service(boolean reset) {
        var properties = new ForesignalProperties(Path.of("./data"), null, reset, "test");
        var service = new AdminTokenService(secrets, hasher, properties);
        service.ensureToken();
        return service;
    }

    private String loggedToken() {
        return logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("FORESIGNAL ADMIN TOKEN"))
                .flatMap(message -> message.lines())
                .map(String::trim)
                .filter(line -> line.startsWith(TokenHasher.TOKEN_PREFIX))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No admin token was printed. Log was:\n" + messages()));
    }

    private List<String> messages() {
        return logs.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Test
    @DisplayName("first start generates a token, stores only its hash, and prints it once")
    void firstStartGeneratesAndPrints() {
        AdminTokenService service = service(false);

        String token = loggedToken();
        assertThat(token).startsWith(TokenHasher.TOKEN_PREFIX);
        assertThat(service.verify(token)).isTrue();

        // The banner appears exactly once.
        assertThat(messages().stream().filter(m -> m.contains("FORESIGNAL ADMIN TOKEN")).count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("the banner says the token is shown only once and explains recovery")
    void bannerIsActionable() {
        service(false);

        String banner =
                messages().stream()
                        .filter(m -> m.contains("FORESIGNAL ADMIN TOKEN"))
                        .findFirst()
                        .orElseThrow();

        assertThat(banner)
                .contains("shown once")
                .contains("first start")
                .contains("FORESIGNAL_RESET_ADMIN_TOKEN=true")
                .contains("Authorization: Bearer");
    }

    @Test
    @DisplayName("only the hash is stored; the plaintext is nowhere in the database")
    void storesOnlyTheHash() {
        AdminTokenService service = service(false);
        String token = loggedToken();

        String stored =
                new String(
                        secrets.findById(AdminTokenService.ADMIN_TOKEN_HASH_KEY).orElseThrow().getValue(),
                        StandardCharsets.UTF_8);

        assertThat(stored).isEqualTo(hasher.hash(token)).hasSize(64).isNotEqualTo(token);
        assertThat(service.verify(token)).isTrue();
    }

    @Test
    @DisplayName("a second start reuses the existing token and prints no banner")
    void secondStartReusesToken() {
        AdminTokenService first = service(false);
        String token = loggedToken();
        logs.list.clear();

        AdminTokenService second = service(false);

        assertThat(messages()).noneMatch(m -> m.contains("FORESIGNAL ADMIN TOKEN"));
        assertThat(second.verify(token)).isTrue();
        assertThat(first.verify(token)).isTrue();
    }

    @Test
    @DisplayName("the reset flag replaces the token and invalidates the old one")
    void resetFlagRegenerates() {
        service(false);
        String original = loggedToken();
        logs.list.clear();

        AdminTokenService afterReset = service(true);
        String replacement = loggedToken();

        assertThat(replacement).isNotEqualTo(original);
        assertThat(afterReset.verify(replacement)).isTrue();
        assertThat(afterReset.verify(original)).isFalse();
    }

    @Test
    @DisplayName("the reset banner says why it regenerated")
    void resetBannerExplainsReason() {
        service(false);
        logs.list.clear();

        service(true);

        assertThat(messages())
                .filteredOn(m -> m.contains("FORESIGNAL ADMIN TOKEN"))
                .allSatisfy(m -> assertThat(m).contains("FORESIGNAL_RESET_ADMIN_TOKEN=true"));
    }

    @Test
    @DisplayName("rotate issues a new token and the old one stops working")
    void rotateInvalidatesTheOldToken() {
        AdminTokenService service = service(false);
        String original = loggedToken();

        String rotated = service.rotate();

        assertThat(rotated).startsWith(TokenHasher.TOKEN_PREFIX).isNotEqualTo(original);
        assertThat(service.verify(rotated)).isTrue();
        assertThat(service.verify(original)).isFalse();
    }

    @Test
    @DisplayName("rotate never prints the new token; only the API response carries it")
    void rotateDoesNotPrintTheToken() {
        AdminTokenService service = service(false);
        logs.list.clear();

        String rotated = service.rotate();

        assertThat(messages()).noneMatch(m -> m.contains(rotated));
    }

    @Test
    @DisplayName("rotation survives a restart")
    void rotationPersists() {
        AdminTokenService service = service(false);
        String rotated = service.rotate();
        logs.list.clear();

        AdminTokenService restarted = service(false);

        assertThat(restarted.verify(rotated)).isTrue();
    }

    @Test
    void rejectsWrongAndMalformedTokens() {
        AdminTokenService service = service(false);
        String token = loggedToken();

        assertThat(service.verify("fsr_not-the-token")).isFalse();
        assertThat(service.verify(token + "x")).isFalse();
        assertThat(service.verify("")).isFalse();
        assertThat(service.verify("   ")).isFalse();
        assertThat(service.verify(null)).isFalse();
    }

    @Test
    void reportsThatItIsInitialized() {
        assertThat(service(false).isInitialized()).isTrue();
    }
}
