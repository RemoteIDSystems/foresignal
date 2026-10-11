package ai.foresignal;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.test.IntegrationTest;
import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@IntegrationTest
class ForesignalApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ForesignalProperties properties;

    @Autowired
    private Clock clock;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void bindsForesignalProperties() {
        assertThat(properties.dataDir()).isNotNull();
        assertThat(properties.version()).isNotBlank();
    }

    @Test
    void clockBeanIsUtc() {
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
