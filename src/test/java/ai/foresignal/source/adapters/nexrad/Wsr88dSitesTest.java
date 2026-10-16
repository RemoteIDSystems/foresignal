package ai.foresignal.source.adapters.nexrad;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The bundled WSR-88D site table (prompt 106). */
class Wsr88dSitesTest {

    @Test
    @DisplayName("loads the 160 US sites and picks the nearest")
    void nearest() {
        Wsr88dSites sites = Wsr88dSites.bundled();
        assertThat(sites.size()).isEqualTo(160);
        assertThat(sites.find("EWX")).isNotNull();
        var near = sites.nearest(-98.0, 30.5, 3);
        assertThat(near).hasSize(3);
        assertThat(near.get(0).id()).isIn("EWX", "GRK");
    }
}
