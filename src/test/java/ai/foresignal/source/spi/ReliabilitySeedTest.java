package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ReliabilitySeed} (prompt 109, task 5): official agencies {@code B}, the operator's receivers and other
 * built-in providers {@code C}, community and news-derived feeds {@code D}, custom, peer and DERIVED sources and
 * descriptors with no licence {@code F}; an explicit descriptor grade always wins.
 */
class ReliabilitySeedTest {

    @Test
    @DisplayName("the seed rules")
    void seeds() {
        assertThat(ReliabilitySeed.seed("usgs-quakes", "US-PD", Mode.POLLING, false)).isEqualTo("B");
        assertThat(ReliabilitySeed.seed("ea-floods", "Open Government Licence v3.0", Mode.POLLING, false))
                .isEqualTo("B");
        assertThat(ReliabilitySeed.seed("local-adsb-1", "Operator's own data (no upstream license)", Mode.PUSH, false))
                .isEqualTo("C");
        assertThat(ReliabilitySeed.seed("emsc", "EMSC data (CC BY 4.0)", Mode.POLLING, false)).isEqualTo("C");
        assertThat(ReliabilitySeed.seed("gdelt-events",
                "GDELT: unrestricted use and redistribution with citation and a link", Mode.POLLING, false))
                .isEqualTo("D");
        assertThat(ReliabilitySeed.seed("wiki-surge", "CC BY-SA 4.0", Mode.POLLING, false)).isEqualTo("D");
        assertThat(ReliabilitySeed.seed("meshtastic-mqtt", "unspecified (Meshtastic community broker)", Mode.STREAMING,
                false)).isEqualTo("D");
        assertThat(ReliabilitySeed.seed("custom-home", "US-PD", Mode.PUSH, true)).isEqualTo("F");
        assertThat(ReliabilitySeed.seed("peer-cabin", "mixed (per record)", Mode.STREAMING, false)).isEqualTo("F");
        assertThat(ReliabilitySeed.seed("derived-loiter", "derived", Mode.DERIVED, false)).isEqualTo("F");
        assertThat(ReliabilitySeed.seed("x", null, Mode.PUSH, false)).isEqualTo("F");
    }

    @Test
    @DisplayName("the builder seeds an unset grade and keeps an explicit one")
    void builder() {
        SourceDescriptor seeded = SourceDescriptor.builder("nws-alerts").name("n").kind("k").licence("US-PD").build();
        assertThat(seeded.reliability()).isEqualTo("B");
        SourceDescriptor explicit = SourceDescriptor.builder("nws-alerts").name("n").kind("k").licence("US-PD")
                .reliability("A").build();
        assertThat(explicit.reliability()).isEqualTo("A");
    }
}
