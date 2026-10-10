package ai.elaro.settings;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link DurationShorthandModule} on the application's Jackson 3 {@code JsonMapper}.
 *
 * <p>Boot 4 builds its {@code JsonMapper} from a {@code JsonMapper.Builder} that it exposes to
 * every {@link JsonMapperBuilderCustomizer} bean, so adding the module here covers HTTP
 * (de)serialization and anything else that uses the shared mapper.
 */
@Configuration
public class JacksonDurationConfig {

    @Bean
    public JsonMapperBuilderCustomizer durationShorthandCustomizer() {
        return builder -> builder.addModule(new DurationShorthandModule());
    }
}
