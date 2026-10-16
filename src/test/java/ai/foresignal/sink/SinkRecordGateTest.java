package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.provenance.SourceLicences;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.VisibilitySettings;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The sink egress gate (prompt 110, task 2) against a real {@link EgressPolicy} with the 109 license table and
 * catalog wired: every record's terms are <em>resolved</em> from its source descriptor (free-text license,
 * descriptor {@code egress}, private flag), never handed in, so the test fails if the gate bypasses the policy.
 */
class SinkRecordGateTest {

    private final Map<String, SourceDescriptor> all = new LinkedHashMap<>();
    private SinkRecordGate gate;

    private static SourceDescriptor.Builder source(String id, String licence) {
        return SourceDescriptor.builder(id).name(id).kind("aircraft").mode(Mode.PUSH).licence(licence);
    }

    @BeforeEach
    void setUp() {
        add(source("opensky-states", "OpenSky Network terms (non-commercial research)").egress(Egress.LOCAL_ONLY)
                .build());
        add(source("acled", "ACLED Terms of Use: no redistribution of raw data").egress(Egress.LOCAL_ONLY).build());
        add(source("aisstream", "aisstream.io terms (unverified)").egress(Egress.LOCAL_ONLY).build());
        add(source("local-adsb-1", "Operator's own data (no upstream license)").egress(Egress.LOCAL_ONLY)
                .egressOverridable(true).fieldSpec("posSource", FieldSpec.of(FieldType.STRING, null)).build());
        add(source("eia-petroleum", "US-PD").egress(Egress.LOCAL_ONLY).build());
        add(source("adsblol-mil", "ODbL-1.0").fieldSpec("posSource", FieldSpec.of(FieldType.STRING, null)).build());
        add(source("usgs-earthquakes", "US-PD").build());
        add(source("weird", "a license nobody cataloged").build());
        add(source("family", "CC BY 4.0").privateSource(true).build());

        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenAnswer(i -> List.copyOf(all.values()));
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
        when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                .thenAnswer(i -> VisibilitySettings.defaults());
        @SuppressWarnings("unchecked")
        ObjectProvider<OutboundUrlPolicy> outbound = mock(ObjectProvider.class);
        EgressPolicy policy = new EgressPolicy(registry, new SourceVisibility(registry, settings, null), settings,
                outbound);
        policy.setLicenceGuard(new LicenceEgressGuard(), new SourceLicences(registry, new LicenceCatalog()));
        gate = new SinkRecordGate(policy);
    }

    private void add(SourceDescriptor descriptor) {
        all.put(descriptor.id(), descriptor);
    }

    private static SinkRecord record(String sourceId, Map<String, Object> properties) {
        return new SinkRecord(sourceId, "id", "aircraft", 1, 1, null, null, false, null, properties, null, null);
    }

    private SinkRecordGate.Decision push(String sourceId) {
        return gate.allow(SinkMode.PUSH, false, false, record(sourceId, Map.of()));
    }

    @Test
    @DisplayName("the mode maps to SINK_SERVE_LAN or SINK_PUSH")
    void paths() {
        assertThat(SinkRecordGate.path(SinkMode.SERVE)).isEqualTo(EgressPath.SINK_SERVE_LAN);
        assertThat(SinkRecordGate.path(SinkMode.PUSH)).isEqualTo(EgressPath.SINK_PUSH);
    }

    @Test
    @DisplayName("OpenSky, ACLED and aisstream are always denied, even on a local SERVE sink")
    void forbiddenSources() {
        for (String id : List.of("opensky-states", "acled", "aisstream")) {
            assertThat(push(id).allowed()).as(id).isFalse();
            assertThat(gate.allow(SinkMode.SERVE, true, false, record(id, Map.of())).allowed()).as(id).isFalse();
            assertThat(gate.allow(SinkMode.SERVE, true, true, record(id, Map.of())).allowed()).as(id).isFalse();
        }
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("opensky-states", Map.of())).reason())
                .isEqualTo(SinkRecordGate.REASON_LICENCE_FORBIDDEN);
    }

    @Test
    @DisplayName("a LOCAL_ONLY descriptor with an open license (eia-petroleum) never reaches a PUSH sink (EGR-2)")
    void descriptorEgressWins() {
        SinkRecordGate.Decision decision = push("eia-petroleum");
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).isEqualTo(SinkRecordGate.REASON_LOCAL_ONLY);
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("eia-petroleum", Map.of())).allowed()).isTrue();
        assertThat(gate.allow(SinkMode.SERVE, false, false, record("eia-petroleum", Map.of())).allowed()).isFalse();
    }

    @Test
    @DisplayName("LOCAL_ONLY receivers reach only a local SERVE sink; MLAT is local-only per record")
    void localOnly() {
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("local-adsb-1", Map.of())).allowed()).isTrue();
        assertThat(push("local-adsb-1").allowed()).isFalse();
        SinkRecord mlat = record("adsblol-mil", Map.of("posSource", "mlat"));
        SinkRecordGate.Decision pushed = gate.allow(SinkMode.PUSH, false, false, mlat);
        assertThat(pushed.allowed()).isFalse();
        assertThat(pushed.reason()).isEqualTo(SinkRecordGate.REASON_MLAT);
        assertThat(gate.allow(SinkMode.SERVE, true, false, mlat).allowed()).isTrue();
        assertThat(gate.allow(SinkMode.SERVE, false, false, mlat).allowed()).isFalse();
        assertThat(gate.allow(SinkMode.PUSH, false, false, record("adsblol-mil", Map.of("posSource", "adsb")))
                .allowed()).as("ODbL ADS-B positions may be re-served").isTrue();
    }

    @Test
    @DisplayName("an uncataloged license resolves to unknown and is treated as LOCAL_ONLY")
    void unknownLicense() {
        SinkRecordGate.Decision decision = push("weird");
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).isEqualTo(SinkRecordGate.REASON_LICENCE_LOCAL_ONLY);
        assertThat(gate.allow(SinkMode.SERVE, true, false, record("weird", Map.of())).allowed()).isTrue();
    }

    @Test
    @DisplayName("a private source needs includePrivate plus a local target")
    void privateSource() {
        SinkRecord record = record("family", Map.of());
        assertThat(gate.allow(SinkMode.SERVE, true, false, record).reason()).isEqualTo(SinkRecordGate.REASON_PRIVATE);
        assertThat(gate.allow(SinkMode.SERVE, true, true, record).allowed()).isTrue();
        assertThat(gate.allow(SinkMode.PUSH, false, true, record).allowed()).isFalse();
        assertThat(gate.allow(SinkMode.PUSH, true, true, record).allowed()).as("PUSH to a private address").isTrue();
    }

    @Test
    @DisplayName("an unregistered source is refused; an openly licensed record is allowed")
    void registeredAndOpen() {
        assertThat(push("gone").reason()).isEqualTo(SinkRecordGate.REASON_UNREGISTERED);
        assertThat(push("usgs-earthquakes").allowed()).isTrue();
    }

    @Test
    @DisplayName("per-record terms resolved from the batch are honored over the descriptor")
    void recordTerms() {
        LicenceCatalog catalog = new LicenceCatalog();
        SinkRecord forbidden = new SinkRecord("usgs-earthquakes", "id", "earthquake", 1, 1, null, null, false, null,
                Map.of(), catalog.terms("acled-terms"), null);
        assertThat(gate.allow(SinkMode.PUSH, false, false, forbidden).reason())
                .isEqualTo(SinkRecordGate.REASON_LICENCE_FORBIDDEN);
    }
}
