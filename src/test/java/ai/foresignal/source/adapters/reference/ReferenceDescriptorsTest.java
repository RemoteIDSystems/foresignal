package ai.foresignal.source.adapters.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.fema.NuclearReactorsUsAdapter;
import ai.foresignal.source.adapters.gvp.GvpVolcanoesAdapter;
import ai.foresignal.source.adapters.nps.NpsBoundariesAdapter;
import ai.foresignal.source.adapters.ourairports.OurAirportsAdapter;
import ai.foresignal.source.adapters.spaceports.SpaceportsAdapter;
import ai.foresignal.source.adapters.submarinecables.SubmarineCablesAdapter;
import ai.foresignal.source.adapters.wikidata.NuclearPlantsWikidataAdapter;
import ai.foresignal.source.adapters.worldportindex.WorldPortIndexAdapter;
import ai.foresignal.source.adapters.wri.WriPowerPlantsAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.support.ReferenceSupport;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The reference sources' shared descriptor defaults (prompt 094). */
class ReferenceDescriptorsTest {

    private static final List<SourceDescriptor> REFERENCE = List.of(
            new WriPowerPlantsAdapter("").descriptor(),
            new OurAirportsAdapter("").descriptor(),
            new SubmarineCablesAdapter("").descriptor(),
            new GvpVolcanoesAdapter("").descriptor(),
            new WorldPortIndexAdapter("").descriptor(),
            new NpsBoundariesAdapter("").descriptor(),
            new SpaceportsAdapter("").descriptor(),
            new NuclearReactorsUsAdapter("").descriptor(),
            new NuclearPlantsWikidataAdapter("").descriptor());

    @Test
    @DisplayName("every reference source is STANDARD/ALL/RECEIVED with no TTL and a 730-day retention")
    void defaults() {
        for (SourceDescriptor descriptor : REFERENCE) {
            assertThat(descriptor.historyTier()).as("%s tier", descriptor.id()).isEqualTo(HistoryTier.STANDARD);
            assertThat(descriptor.defaultIngestPolicy()).as("%s policy", descriptor.id())
                    .isInstanceOf(IngestPolicy.All.class);
            assertThat(descriptor.observedAtMode()).as("%s time base", descriptor.id())
                    .isEqualTo(ObservedAtMode.RECEIVED);
            assertThat(descriptor.recordTtl()).as("%s TTL", descriptor.id()).isNull();
            assertThat(descriptor.zeroResultsNormal()).as("%s zeroResultsNormal", descriptor.id()).isFalse();
            assertThat(descriptor.defaultRetention()).as("%s retention", descriptor.id())
                    .isEqualTo(ReferenceSupport.RETENTION);
            assertThat(descriptor.defaultInterval()).as("%s interval", descriptor.id())
                    .isEqualTo(Duration.ofDays(7));
            assertThat(descriptor.minInterval()).as("%s minInterval", descriptor.id())
                    .isEqualTo(Duration.ofDays(1));
            assertThat(descriptor.defaultBudget().perMinute()).as("%s budget/min", descriptor.id()).isEqualTo(2);
            assertThat(descriptor.defaultBudget().perDay()).as("%s budget/day", descriptor.id()).isEqualTo(20);
        }
    }

    @Test
    @DisplayName("every reference source declares dataset-release and a licence, terms and attribution")
    void releaseAndLicence() {
        for (SourceDescriptor descriptor : REFERENCE) {
            assertThat(descriptor.kinds()).as("%s kinds", descriptor.id()).contains("dataset-release");
            assertThat(descriptor.licence()).as("%s licence", descriptor.id()).isNotBlank();
            assertThat(descriptor.termsUrl()).as("%s termsUrl", descriptor.id()).isNotBlank();
            assertThat(descriptor.attribution()).as("%s attribution", descriptor.id()).isNotBlank();
        }
    }

    @Test
    @DisplayName("submarine-cables is non-commercial and local-only")
    void submarineCables() {
        SourceDescriptor descriptor = new SubmarineCablesAdapter("").descriptor();
        assertThat(descriptor.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(descriptor.licence()).contains("non-commercial");
        assertThat(descriptor.attribution()).contains("TeleGeography");
        descriptor.kindPresentation().values().forEach(hints ->
                assertThat(hints).containsEntry("nonCommercial", true));
    }

    @Test
    @DisplayName("the shrink guard default is 0.2 and is overridable per source")
    void shrinkGuard() {
        assertThat(SourceDescriptor.DEFAULT_MAX_ABSENT_RATIO).isEqualTo(0.2);
        assertThat(REFERENCE.get(0).reconcileMaxAbsentRatio()).isEqualTo(0.2);
        assertThat(SourceDescriptor.builder("x").reconcileMaxAbsentRatio(0.5).build().reconcileMaxAbsentRatio())
                .isEqualTo(0.5);
    }
}
