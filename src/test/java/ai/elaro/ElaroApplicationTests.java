package ai.elaro;

import static org.assertj.core.api.Assertions.assertThat;

import ai.elaro.config.ElaroProperties;
import ai.elaro.test.IntegrationTest;
import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@IntegrationTest
class ElaroApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ElaroProperties properties;

    @Autowired
    private Clock clock;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void bindsElaroProperties() {
        assertThat(properties.dataDir()).isNotNull();
        assertThat(properties.version()).isNotBlank();
    }

    @Test
    void clockBeanIsUtc() {
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
