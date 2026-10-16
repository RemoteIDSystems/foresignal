package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.testsupport.net.TestOutbound;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The small decisions behind the 110 fixes: secrets merged (not replaced) on update, denial counters merged into
 * the cursor row, which per-record license reaches the gate, and which destinations count as local targets.
 */
class SinkSupportUnitsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("a partial secrets update sets, keeps and removes fields; secrets: null removes all")
    void mergeSecrets() {
        Map<String, String> stored = Map.of("username", "fs", "password", "old");
        assertThat(SinkService.mergeSecrets(stored, JSON.readTree("{\"password\":\"new\"}")))
                .containsExactlyInAnyOrderEntriesOf(Map.of("username", "fs", "password", "new"));
        assertThat(SinkService.mergeSecrets(stored, JSON.readTree("{\"username\":null}")))
                .containsExactlyEntriesOf(Map.of("password", "old"));
        assertThat(SinkService.mergeSecrets(stored, JSON.readTree("null"))).isEmpty();
        assertThatThrownBy(() -> SinkService.mergeSecrets(stored, JSON.readTree("[1]")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("denial deltas add to the stored totals per reason and per source")
    @SuppressWarnings("unchecked")
    void mergeDenied() {
        Map<String, Object> stored = new LinkedHashMap<>();
        stored.put("total", 2L);
        stored.put("byReason", Map.of("private", 2L));
        stored.put("bySource", Map.of("family", 2L));
        Map<String, Object> merged = SinkSupervisor.mergeDenied(stored, Map.of("private", 1L, "mlat", 3L),
                Map.of("family", 1L, "adsblol", 3L));
        assertThat(merged.get("total")).isEqualTo(6L);
        assertThat((Map<Object, Object>) merged.get("byReason")).containsEntry("private", 3L).containsEntry("mlat", 3L);
        assertThat((Map<Object, Object>) merged.get("bySource")).containsEntry("family", 3L).containsEntry("adsblol", 3L);
        assertThat(SinkSupervisor.mergeDenied(null, Map.of(), Map.of()).get("total")).isEqualTo(0L);
    }

    @Test
    @DisplayName("descriptor-fallback licenses are left to EgressPolicy; batch and MLAT terms are handed on")
    void recordTerms() {
        LicenceCatalog catalog = new LicenceCatalog();
        assertThat(SinkSupervisor.recordTerms(null)).isNull();
        assertThat(SinkSupervisor.recordTerms(new LicenceResolver.Resolution(catalog.terms("CC-BY-4.0"),
                LicenceResolver.BASIS_DESCRIPTOR, null, null, null))).isNull();
        assertThat(SinkSupervisor.recordTerms(new LicenceResolver.Resolution(catalog.terms("ODbL-1.0"),
                LicenceResolver.BASIS_BATCH, 1L, null, null)).id()).isEqualTo("ODbL-1.0");
    }

    @Test
    @DisplayName("local targets: SERVE always; PUSH to a LAN host or a LAN-scoped multicast group; not the internet")
    void localTarget() {
        SinkSupervisor supervisor = new SinkSupervisor(null, null, null, null, null, null, null, null, null, null,
                TestOutbound.offlinePolicy(), null, null, null);
        assertThat(supervisor.localTarget(sink(Map.of()), SinkMode.SERVE)).isTrue();
        assertThat(supervisor.localTarget(sink(Map.of("url", "https://127.0.0.1/x")), SinkMode.PUSH)).isTrue();
        assertThat(supervisor.localTarget(sink(Map.of("brokerUrl", "mqtt://192.168.1.10:1883")), SinkMode.PUSH))
                .isTrue();
        assertThat(supervisor.localTarget(sink(Map.of("url", "https://8.8.8.8/x")), SinkMode.PUSH)).isFalse();
        assertThat(supervisor.localTarget(sink(Map.of("group", "239.2.3.1")), SinkMode.PUSH)).isTrue();
        assertThat(supervisor.localTarget(sink(Map.of("group", "230.2.3.1")), SinkMode.PUSH)).isFalse();
        assertThat(supervisor.localTarget(sink(Map.of()), SinkMode.PUSH)).isFalse();
    }

    static SinkEntity sink(Map<String, Object> config) {
        SinkEntity sink = new SinkEntity("s", "cot", "S", Instant.parse("2026-10-16T12:00:00Z"));
        sink.setConfig(new LinkedHashMap<>(config));
        return sink;
    }
}
