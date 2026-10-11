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
        SpringApplication.run(ForesignalApplication.class, args);
    }
}
