package ai.foresignal;

import ai.foresignal.collect.UpstreamClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the Foresignal intelligence broker. */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class ForesignalApplication {

    public static void main(String[] args) {
        // Before anything can create an HttpClient: the JDK reads the restricted-header property
        // only once, and a pinned LAN request needs to send its own Host header.
        UpstreamClient.allowHostHeaderForPinnedRequests();
        createTempDirectory();
        SpringApplication.run(ForesignalApplication.class, args);
    }

    /**
     * Creates {@code java.io.tmpdir} (and {@code jna.tmpdir}) before anything uses them (prompt 046).
     * The container sets both to {@code /data/tmp}, a directory on the data volume where executables
     * may run, so libaec's native library (through JNA) and H3's can be extracted there; the JVM does
     * not create the directory itself.
     */
    static void createTempDirectory() {
        for (String property : new String[] {"java.io.tmpdir", "jna.tmpdir"}) {
            String dir = System.getProperty(property);
            if (dir == null || dir.isBlank()) {
                continue;
            }
            try {
                java.nio.file.Files.createDirectories(java.nio.file.Path.of(dir));
            } catch (java.io.IOException | RuntimeException e) {
                System.err.println("Foresignal: could not create " + property + " " + dir + ": " + e.getMessage());
            }
        }
    }
}
