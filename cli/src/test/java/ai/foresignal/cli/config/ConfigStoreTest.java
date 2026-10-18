package ai.foresignal.cli.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.foresignal.cli.CliUsageException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Profiles in {@code config.json}, and the redaction of tokens. */
class ConfigStoreTest {

    @TempDir
    Path tmp;

    private final StringWriter err = new StringWriter();

    private ConfigStore store(Path dir) {
        return new ConfigStore(dir, new PrintWriter(err, true));
    }

    @Test
    void aMissingFileHasNoProfiles() {
        assertTrue(store(tmp.resolve("none")).profile(null).isEmpty());
    }

    @Test
    void theCurrentProfileIsUsedWhenNoneIsNamed() {
        ConfigStore store = store(tmp.resolve("c"));
        store.saveProfile("default", "http://a", "fsr_a");
        store.saveProfile("prod", "http://b/", "fsr_b");
        assertEquals("http://b", store.profile(null).orElseThrow().url());
        assertEquals("fsr_a", store.profile("default").orElseThrow().token());
    }

    @Test
    void savingWithoutATokenKeepsTheExistingOne() {
        ConfigStore store = store(tmp.resolve("c"));
        store.saveProfile("default", "http://a", "fsr_a");
        store.saveProfile("default", "http://a2", null);
        Profile p = store.profile("default").orElseThrow();
        assertEquals("http://a2", p.url());
        assertEquals("fsr_a", p.token());
    }

    @Test
    void aCorruptFileIsAUsageError() throws Exception {
        Path dir = Files.createDirectories(tmp.resolve("c"));
        Files.writeString(dir.resolve("config.json"), "[1]");
        assertThrows(CliUsageException.class, () -> store(dir).load());
        Files.writeString(dir.resolve("config.json"), "{nope");
        assertThrows(CliUsageException.class, () -> store(dir).load());
    }

    @Test
    void toStringNeverShowsTheToken() {
        assertFalse(new Profile("p", "http://a", "fsr_secret").toString().contains("fsr_secret"));
        assertFalse(new Connection("http://a", "fsr_secret").toString().contains("fsr_secret"));
        assertTrue(new Connection("http://a", null).toString().contains("token=none"));
    }

    @Test
    void connectionsNormalizeTheirUrlAndBlankTokens() {
        Connection c = new Connection(" http://a/// ", "  ");
        assertEquals("http://a", c.baseUrl());
        assertFalse(c.hasToken());
    }
}
