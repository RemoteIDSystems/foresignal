package ai.foresignal.config;

import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets an encoded slash ({@code %2F}) reach the application inside one path segment (prompt 129): object references
 * are one segment of {@code /v1/objects/{ref}/…} with {@code /} percent-encoded ({@code record:adsblol-mil%2Fae1234},
 * docs/OpenAPI.json), and record external ids may contain slashes too. Tomcat rejects {@code %2F} by default; passed
 * through, Spring MVC decodes it into the path variable after matching segments, so it can never change which route
 * matches.
 */
@Configuration
public class EncodedSlashConfig {

    /** Tomcat connectors pass {@code %2F} through undecoded. */
    @Bean
    public TomcatConnectorCustomizer encodedSolidusPassThrough() {
        return connector -> connector.setEncodedSolidusHandling(EncodedSolidusHandling.PASS_THROUGH.getValue());
    }
}
