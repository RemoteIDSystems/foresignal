package ai.foresignal.testsupport.inbound;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Reads the EDP conformance fixtures ({@code src/test/resources/edp/conformance/}) for integration tests. */
public final class EdpConformance {

    private EdpConformance() {}

    /** The fixture at {@code path} under {@code edp/conformance/}. */
    public static String text(String path) {
        try (InputStream in = EdpConformance.class.getClassLoader().getResourceAsStream("edp/conformance/" + path)) {
            if (in == null) {
                throw new IllegalStateException("Missing fixture " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A valid manifest. */
    public static String manifest(String name) {
        return text("manifests/valid/" + name);
    }

    /** A valid batch. */
    public static String batch(String name) {
        return text("batches/valid/" + name);
    }

    /** An invalid batch. */
    public static String invalid(String name) {
        return text("batches/invalid/" + name);
    }
}
