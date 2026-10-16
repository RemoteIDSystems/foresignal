package ai.foresignal.sink;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** CoT XML: affiliation, type map, stale and escaping (prompt 110). */
class CotEncoderTest {

    @Test
    @DisplayName("the CoT type maps aircraft, vessel and other to A/S/G and never leaves u/n")
    void type() {
        assertThat(CotEncoder.type("aircraft", "u")).isEqualTo("a-u-A");
        assertThat(CotEncoder.type("vessel", "n")).isEqualTo("a-n-S");
        assertThat(CotEncoder.type("quake", "u")).isEqualTo("a-u-G");
        // Even a military aircraft is neutral/unknown only.
        assertThat(CotEncoder.type("aircraft", "f")).isEqualTo("a-u-A");
        assertThat(CotEncoder.type("aircraft", "h")).isEqualTo("a-u-A");
    }

    @Test
    @DisplayName("the event carries uid, point, contact, track, remarks and escapted link")
    void event() {
        Instant time = Instant.parse("2026-10-15T12:00:00Z");
        String xml = CotEncoder.event("FORESIGNAL.abc.adsblol-mil.4CA1", "a-u-A", 44.5, -3.2, time,
                time.plusSeconds(120), "RCH123", 90.0, 250.0, "Title \u2022 attribution", "https://x/?a=1&b=2");
        assertThat(xml).contains("uid=\"FORESIGNAL.abc.adsblol-mil.4CA1\"");
        assertThat(xml).contains("type=\"a-u-A\"");
        assertThat(xml).contains("lat=\"44.5\" lon=\"-3.2\"");
        assertThat(xml).contains("<contact callsign=\"RCH123\"/>");
        assertThat(xml).contains("<track course=\"90\" speed=\"250\"/>");
        assertThat(xml).contains("stale=\"2026-10-15T12:02:00.000Z\"");
        assertThat(xml).contains("&amp;b=2");
        assertThat(xml).doesNotContain("&b=2\"");
    }
}
