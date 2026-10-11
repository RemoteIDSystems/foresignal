package ai.foresignal.testsupport.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Reads recorded upstream fixtures from {@code src/test/resources/fixtures/<source-id>/}
 * (Architecture §4.2 rule 4).
 */
public final class AdapterFixtures {

    private AdapterFixtures() {}

    /**
     * The bytes of one fixture.
     *
     * @param sourceId the source id, which is the fixture directory
     * @param name the file name inside it
     * @throws IllegalArgumentException when the fixture does not exist
     */
    public static byte[] bytes(String sourceId, String name) {
        String path = "/fixtures/" + sourceId + "/" + name;
        try (InputStream in = AdapterFixtures.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("No fixture at classpath:" + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read fixture " + path, e);
        }
    }

    /** One fixture decoded as UTF-8. */
    public static String text(String sourceId, String name) {
        return new String(bytes(sourceId, name), StandardCharsets.UTF_8);
    }
}
