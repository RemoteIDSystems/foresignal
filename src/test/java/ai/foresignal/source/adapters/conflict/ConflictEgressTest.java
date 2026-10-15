package ai.foresignal.source.adapters.conflict;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.VisibilitySettings;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.acled.AcledAdapter;
import ai.foresignal.source.adapters.alertsinua.AlertsInUaAdapter;
import ai.foresignal.source.adapters.deepstate.DeepStateAdapter;
import ai.foresignal.source.adapters.uasiren.UaSirenAdapter;
import ai.foresignal.source.adapters.ucdp.UcdpGedAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Egress of the prompt 081 sources (EGR-2): {@code acled} is {@code LOCAL_ONLY}, so {@link EgressPolicy} refuses it on
 * every re-serving path (sinks, federation, share links, feeds and feed keys) and allows it only to the instance's own
 * clients; the other four sources are {@code OPEN}. The free-text license fields are the ones prompt 109's audit
 * resolves.
 */
class ConflictEgressTest {

    static final List<SourceAdapter> ADAPTERS = List.of(
            new DeepStateAdapter(""),
            new AlertsInUaAdapter("", new UaRegionIndex()),
            new UaSirenAdapter("", new UaRegionIndex()),
            new AcledAdapter("", new CountryCentroids()),
            new UcdpGedAdapter("", new CountryCentroids()));

    @SuppressWarnings("unchecked")
    private static EgressPolicy policy() {
        Map<String, SourceDescriptor> all = ADAPTERS.stream().map(SourceAdapter::descriptor)
                .collect(Collectors.toMap(SourceDescriptor::id, Function.identity()));
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenReturn(List.copyOf(all.values()));
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                .thenAnswer(i -> VisibilitySettings.defaults());
        ObjectProvider<OutboundUrlPolicy> outbound = mock(ObjectProvider.class);
        return new EgressPolicy(registry, new SourceVisibility(registry, settings, null), settings, outbound);
    }

    @Test
    @DisplayName("acled is LOCAL_ONLY: refused on sinks, federation, share links and feeds; exported to its own clients")
    void acledLocalOnly() {
        EgressPolicy policy = policy();
        assertThat(new AcledAdapter("", new CountryCentroids()).descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(policy.isLocalOnly(AcledAdapter.ID)).isTrue();
        for (EgressPath path : List.of(EgressPath.SINK, EgressPath.FEDERATION, EgressPath.SHARE, EgressPath.FEED)) {
            assertThat(policy.allows(AcledAdapter.ID, path)).as(path.name()).isFalse();
        }
        assertThat(policy.allows(AcledAdapter.ID, EgressPath.EXPORT)).isTrue();
    }

    @Test
    @DisplayName("deepstate-frontline, alerts-in-ua, ua-air-raid-siren and ucdp-ged are OPEN on every path")
    void othersOpen() {
        EgressPolicy policy = policy();
        for (String id : List.of(DeepStateAdapter.ID, AlertsInUaAdapter.ID, UaSirenAdapter.ID, UcdpGedAdapter.ID)) {
            assertThat(policy.isLocalOnly(id)).as(id).isFalse();
            for (EgressPath path : EgressPath.values()) {
                assertThat(policy.allows(id, path)).as(id + " " + path).isTrue();
            }
        }
    }

    @Test
    @DisplayName("license, terms and attribution as prompt 109's audit expects; none is overridable here")
    void licenses() {
        Map<String, SourceDescriptor> d = ADAPTERS.stream().map(SourceAdapter::descriptor)
                .collect(Collectors.toMap(SourceDescriptor::id, Function.identity()));
        assertThat(d.get("acled").licence()).isEqualTo("ACLED Terms of Use: no redistribution of raw data");
        assertThat(d.get("acled").attribution())
                .isEqualTo("Armed Conflict Location & Event Data (ACLED), acleddata.com");
        assertThat(d.get("alerts-in-ua").licence()).isEqualTo("unspecified (alerts.in.ua)");
        assertThat(d.get("alerts-in-ua").attribution()).isEqualTo("alerts.in.ua");
        assertThat(d.get("alerts-in-ua").description()).contains("critical infrastructure");
        assertThat(d.get("ua-air-raid-siren").licence())
                .isEqualTo("unspecified (siren.pp.ua wrapper of ukrainealarm.com)");
        assertThat(d.get("ua-air-raid-siren").description()).contains("alerts-in-ua is the preferred")
                .contains("api.ukrainealarm.com needs an application key");
        assertThat(d.get("deepstate-frontline").licence()).isEqualTo("GPL-3.0 mirror; DeepStateMap terms unspecified");
        assertThat(d.get("deepstate-frontline").attribution())
                .isEqualTo("DeepStateMap.Live via cyterat/deepstate-map-data");
        assertThat(d.get("ucdp-ged").licence()).isEqualTo("CC BY 4.0");
        assertThat(d.get("ucdp-ged").attribution())
                .isEqualTo("UCDP Georeferenced Event Dataset (candidate), Uppsala Conflict Data Program");
        assertThat(d.values()).allSatisfy(x -> {
            assertThat(x.egressOverridable()).as(x.id()).isFalse();
            assertThat(x.termsUrl()).as(x.id()).startsWith("https://");
        });
    }
}
