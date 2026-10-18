package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A stored EDP item as the router sees it (prompt 138): stream, item id, provenance and effective times. */
class BoundItemTest {

    @Test
    @DisplayName("stream and item id come from the provenance and the external id")
    void identity() {
        BoundItem item = BindingFixtures.item("levels", "gauge:2026-10-17", "observation", 9, Map.of());
        assertThat(item.streamId()).isEqualTo("levels");
        assertThat(item.itemId()).isEqualTo("gauge:2026-10-17");
        assertThat(item.edpItem()).isTrue();
        assertThat(item.itemType()).isEqualTo("observation");
        assertThat(item.originator()).isEqualTo("test-lab");
        assertThat(item.aggregates()).containsExactly("usgs-anss");
        assertThat(item.recordKey()).isEqualTo(BindingFixtures.SOURCE + "/levels:gauge:2026-10-17");
        assertThat(item.ref()).startsWith("record:" + BindingFixtures.SOURCE + "/").endsWith("@9");
        BoundItem converted = new BoundItem(10, BindingFixtures.SOURCE, "telegram:1", "social-post", 1, null, null,
                null, null, null, Map.of("platform", "telegram"));
        assertThat(converted.edpItem()).isFalse();
        assertThat(converted.streamId()).isEqualTo("telegram");
    }

    @Test
    @DisplayName("effective times are never earlier than receipt; receipt stands in when none is carried")
    void effective() {
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", BindingFixtures.RECEIVED.toString());
        times.put("effectiveKnownAt", BindingFixtures.RECEIVED.plusSeconds(3_600).toString());
        times.put("effectivePublishedAt", BindingFixtures.RECEIVED.minusSeconds(3_600).toString());
        BoundItem item = BindingFixtures.item("levels", "x", "observation", 1, Map.of("edpTimes", times));
        assertThat(item.receivedAt()).isEqualTo(BindingFixtures.RECEIVED);
        assertThat(item.effective("effectiveKnownAt")).isEqualTo(BindingFixtures.RECEIVED.plusSeconds(3_600));
        assertThat(item.effective("effectivePublishedAt")).isEqualTo(BindingFixtures.RECEIVED);
        assertThat(item.effective("effectiveMadeAt")).isEqualTo(BindingFixtures.RECEIVED);
        assertThat(BoundItem.time("not a time")).isNull();
        assertThat(BoundItem.time("2026-10-18T06:00:00+02:00")).isEqualTo(BindingFixtures.RECEIVED.minusSeconds(7_200));
    }
}
