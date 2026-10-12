package ai.foresignal.notify.push;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RFC 8030 §5.4 topics (prompt 031). */
class TopicDerivationTest {

    @Test
    @DisplayName("at most 32 characters of the base64url alphabet")
    void shape() {
        for (String key : new String[] {"", "a", "alert:r:s:e", "x".repeat(10_000), "ünïcødé 😀"}) {
            String topic = PushTopic.derive(key);
            assertThat(topic).hasSizeLessThanOrEqualTo(32).matches("^[A-Za-z0-9_-]+$");
        }
    }

    @Test
    @DisplayName("stable per entity, distinct between entities, and keyed as specified")
    void stable() {
        assertThat(PushTopic.alert("r1", "usgs", "us1")).isEqualTo(PushTopic.alert("r1", "usgs", "us1"))
                .isEqualTo(PushTopic.derive("alert:r1:usgs:us1"));
        assertThat(PushTopic.alert("r1", "usgs", "us1")).isNotEqualTo(PushTopic.alert("r1", "usgs", "us2"));
        assertThat(PushTopic.briefing("b1")).isEqualTo(PushTopic.derive("briefing:b1"));
        assertThat(PushTopic.system("system.rule_storm")).isEqualTo(PushTopic.derive("system:system.rule_storm"));
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(PushTopic.alert("r", "s", Integer.toString(i)));
        }
        assertThat(seen).hasSize(1000);
        // base64url(SHA-256("")) = 47DEQpj8HBSa-_TImW-5JCeuQeRkm5NMpJWZG3hSuFU
        assertThat(PushTopic.derive("")).isEqualTo("47DEQpj8HBSa-_TImW-5JCeuQeRkm5NM");
    }
}
