package ai.elaro.config;

import org.hibernate.cfg.MappingSettings;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Makes Hibernate map {@code jsonb} columns with the application's Jackson 3 mapper.
 *
 * <p>This is not redundant. Hibernate picks a JSON {@code FormatMapper} by probing the classpath,
 * and it prefers Jackson 2 over Jackson 3 when both are present. Both <em>are</em> present here:
 * Spring Boot 4 uses Jackson 3, while springdoc's swagger-core dependency drags in Jackson 2. Left
 * alone, Hibernate would therefore serialize every {@code jsonb} column with a private Jackson 2
 * {@code ObjectMapper} that knows nothing about Boot's configuration — including Elaro's
 * {@code Duration} shorthand — and the choice would silently flip if springdoc were ever dropped.
 *
 * <p>Naming the mapper explicitly pins the behaviour to one mapper, the one the rest of the
 * application uses.
 */
@Configuration
public class HibernateJsonConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(JsonMapper jsonMapper) {
        return properties ->
                properties.put(
                        MappingSettings.JSON_FORMAT_MAPPER, new Jackson3JsonFormatMapper(jsonMapper));
    }
}
