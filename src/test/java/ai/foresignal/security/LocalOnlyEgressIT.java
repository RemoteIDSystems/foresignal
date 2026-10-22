package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.adapters.opensky.OpenSkyStatesAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EGR-2 (prompt 062): a {@code LOCAL_ONLY} source's records are served to this instance's own API clients but
 * never re-served. Asserted on the registered {@code opensky-states} (LOCAL_ONLY by OpenSky's terms) against
 * {@code adsblol-mil} (ODbL, OPEN), through the application's single {@link EgressPolicy}.
 */
@IntegrationTest
class LocalOnlyEgressIT {

    private static final String OPENSKY = OpenSkyStatesAdapter.ID;
    private static final String ADSBLOL = AdsbLolMilAdapter.ID;

    @Autowired private EgressPolicy egress;
    @Autowired private SourceRegistry registry;

    @Test
    @DisplayName("the registered descriptors carry their egress")
    void descriptors() {
        assertThat(registry.descriptor(OPENSKY).egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(registry.descriptor(ADSBLOL).egress()).isEqualTo(Egress.OPEN);
        assertThat(egress.isLocalOnly(OPENSKY)).isTrue();
        assertThat(egress.isLocalOnly(ADSBLOL)).isFalse();
    }

    @Test
    @DisplayName("LOCAL_ONLY never leaves on a sink, federation, a share link or a feed, for any viewer")
    void reServingPathsRefused() {
        for (Viewer viewer : new Viewer[] {Viewer.admin(), Viewer.anonymous(), null}) {
            assertThat(egress.allows(EgressPath.SINK, OPENSKY, viewer)).isFalse();
            assertThat(egress.allows(EgressPath.FEDERATION, OPENSKY, viewer)).isFalse();
            assertThat(egress.allows(EgressPath.SHARE, OPENSKY, viewer)).isFalse();
            assertThat(egress.allows(EgressPath.FEED, OPENSKY, viewer)).isFalse();
        }
        assertThat(egress.allows(OPENSKY, EgressPath.SINK)).isFalse();
        assertThat(egress.allows(OPENSKY, EgressPath.FEDERATION)).isFalse();
    }

    @Test
    @DisplayName("LOCAL_ONLY is still served to this instance's own clients: reads, and a personal-use export"
            + " (OpenSky's terms forbid redistribution, so a plain export is refused by the 109 license table)")
    void localPathsAllowed() {
        assertThat(egress.allows(EgressPath.EXPORT, OPENSKY, Viewer.admin())).isFalse();
        assertThat(egress.decide(EgressPath.EXPORT, OPENSKY, null, null,
                EgressPolicy.EgressContext.export(Viewer.admin(), false, true)).allowed()).isTrue();
        assertThat(egress.allows(EgressPath.ARCHIVE, OPENSKY, null)).as("operator-owned archive").isTrue();
        assertThat(egress.visible(OPENSKY, Viewer.anonymous())).isTrue();
        assertThat(egress.allowsEmbedding(OPENSKY)).isTrue();
    }

    @Test
    @DisplayName("an OPEN source leaves on every path; an unknown source on none")
    void openAndUnknown() {
        for (EgressPath path : EgressPath.values()) {
            assertThat(egress.allows(path, ADSBLOL, Viewer.anonymous())).as(path.name()).isTrue();
            assertThat(egress.allows(ADSBLOL, path)).as(path.name()).isTrue();
            assertThat(egress.allows(path, "no-such-source", Viewer.admin())).as(path.name()).isFalse();
        }
        assertThat(egress.isLocalOnly("no-such-source")).isTrue();
    }
}
