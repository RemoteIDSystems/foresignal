package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The sink egress gate (prompt 110). */
class SinkRecordGateTest {

    private final LicenceCatalog catalog = new LicenceCatalog();
    private SourceVisibility visibility = mock(SourceVisibility.class);
    private SourceRegistry registry = mock(SourceRegistry.class);

    private SinkRecordGate gate() {
        return new SinkRecordGate(new LicenceEgressGuard(), catalog, visibility, registry);
    }

    private SinkRecord record(String sourceId, Map<String, Object> properties, String licenceId) {
        return new SinkRecord(sourceId, "id", "aircraft", 1, 1, null, null, false, null, properties,
                catalog.terms(licenceId), null);
    }

    @Test
    @DisplayName("OpenSky, ACLED and aisstream are always denied")
    void forbiddenSources() {
        SinkRecordGate gate = gate();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("opensky-states", Map.of(), "opensky-terms")).allowed())
                .isFalse();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("acled", Map.of(), "acled-terms")).allowed())
                .isFalse();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("aisstream", Map.of(), "aisstream-terms")).allowed())
                .isFalse();
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("opensky-states", Map.of(), "opensky-terms")).allowed())
                .isFalse();
    }

    @Test
    @DisplayName("LOCAL_ONLY records reach only a local SERVE sink; MLAT is local-only per record")
    void localOnly() {
        SinkRecordGate gate = gate();
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("local-adsb-1", Map.of(), "local-only-operator")).allowed())
                .isTrue();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("local-adsb-1", Map.of(), "local-only-operator")).allowed())
                .isFalse();
        SinkRecord mlat = record("adsblol-mil", Map.of("posSource", "mlat"), "ODbL-1.0");
        assertThat(gate.allow(SinkMode.PUSH, false, false, mlat).allowed()).isFalse();
        assertThat(gate.allow(SinkMode.SERVE, true, false, mlat).allowed()).isTrue();
    }

    @Test
    @DisplayName("a private source needs includePrivate plus a local target")
    void privateSource() {
        when(visibility.isPrivate("private-1")).thenReturn(true);
        SinkRecordGate gate = gate();
        SinkRecord record = record("private-1", Map.of(), "CC-BY-4.0");
        assertThat(gate.allow(SinkMode.SERVE, true, false, record).allowed()).isFalse();
        assertThat(gate.allow(SinkMode.SERVE, true, true, record).allowed()).isTrue();
        assertThat(gate.allow(SinkMode.PUSH, false, true, record).allowed()).isFalse();
    }

    @Test
    @DisplayName("an openly licensed record is allowed everywhere")
    void allowed() {
        SinkRecordGate gate = gate();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("usgs-earthquakes", Map.of(), "us-gov-public-domain")).allowed())
                .isTrue();
    }
}
