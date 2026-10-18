package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.inbound.edp.EdpPeriods;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Series of bound observation streams (prompt 138): points from EDP periods, {@code knownAt} as the effective
 * availability (never before receipt, EXT-4), as-of exclusion, and the frequency and transform mappings.
 */
class ExternalSeriesReaderTest {

    private static final Instant FROM = Instant.parse("2026-10-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-11-01T00:00:00Z");

    /** A stored observation payload. */
    private static Map<String, Object> payload(String t, Object value, String knownAt) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("seriesKey", "tanker");
        p.put("t", t);
        p.put("value", value);
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", "2026-10-11T18:00:00Z");
        times.put("effectiveKnownAt", knownAt);
        p.put("edpTimes", times);
        return p;
    }

    @Test
    @DisplayName("point(): the period's interval, the value and knownAt = effectiveKnownAt")
    void point() {
        SeriesValue p = ExternalSeriesReader.point(payload("2026-10-10", 31, "2026-10-11T18:00:00Z"),
                Instant.parse("2026-10-11T18:00:01Z"), 1, FROM, TO, null);
        assertThat(p.periodStart()).isEqualTo(Instant.parse("2026-10-10T00:00:00Z"));
        assertThat(p.periodEnd()).isEqualTo(Instant.parse("2026-10-11T00:00:00Z"));
        assertThat(p.value()).isEqualTo(31.0);
        assertThat(p.knownAt()).isEqualTo(Instant.parse("2026-10-11T18:00:00Z"));
        assertThat(p.period()).isEqualTo("2026-10-10");
    }

    @Test
    @DisplayName("a claimed knownAt before receipt is lifted to receipt; as-of hides what was not known yet")
    void knownAt() {
        SeriesValue lifted = ExternalSeriesReader.point(payload("2026-10-10", 31, "2026-10-10T00:00:00Z"),
                Instant.parse("2026-10-11T18:00:01Z"), 1, FROM, TO, null);
        assertThat(lifted.knownAt()).isEqualTo(Instant.parse("2026-10-11T18:00:00Z"));
        assertThat(ExternalSeriesReader.point(payload("2026-10-10", 31, "2026-10-12T00:00:00Z"),
                Instant.parse("2026-10-11T18:00:01Z"), 1, FROM, TO, Instant.parse("2026-10-11T20:00:00Z"))).isNull();
    }

    @Test
    @DisplayName("unreadable periods, periods outside the range and non-numeric values")
    void edges() {
        assertThat(ExternalSeriesReader.point(payload("yesterday", 1, null), null, 1, FROM, TO, null)).isNull();
        assertThat(ExternalSeriesReader.point(payload("2026-09-30", 1, null), null, 1, FROM, TO, null)).isNull();
        assertThat(ExternalSeriesReader.point(payload("2026-10-05", "n/a", null), FROM, 1, FROM, TO, null).value())
                .isNull();
    }

    @Test
    @DisplayName("frequencies, registry frequencies and transform hints")
    void mappings() {
        assertThat(ExternalSeriesReader.frequency(EdpPeriods.parse("2026-10-10"))).isEqualTo(SeriesFrequency.DAY);
        assertThat(ExternalSeriesReader.frequency(EdpPeriods.parse("2026-W41"))).isEqualTo(SeriesFrequency.WEEK);
        assertThat(ExternalSeriesReader.frequency(EdpPeriods.parse("2026-10"))).isEqualTo(SeriesFrequency.MONTH);
        assertThat(ExternalSeriesReader.frequency(EdpPeriods.parse("2026-Q3"))).isEqualTo(SeriesFrequency.IRREGULAR);
        assertThat(ExternalSeriesReader.frequency(null)).isEqualTo(SeriesFrequency.IRREGULAR);
        assertThat(ExternalSeriesReader.registryFrequency(EdpPeriods.parse("2026-10-10"))).isEqualTo("day");
        assertThat(ExternalSeriesReader.registryFrequency(EdpPeriods.parse("2026-W41"))).isEqualTo("week");
        assertThat(ExternalSeriesReader.registryFrequency(EdpPeriods.parse("2026"))).isNull();
        assertThat(ExternalSeriesReader.transformHint(null)).isEqualTo("level");
        assertThat(ExternalSeriesReader.transformHint("none")).isEqualTo("level");
        assertThat(ExternalSeriesReader.transformHint("log1p")).isEqualTo("log-level");
        assertThat(ExternalSeriesReader.transformHint("diff")).isEqualTo("difference");
        assertThat(ExternalSeriesReader.ref("ext-hormuz-transits", "transit-count", "tanker"))
                .isEqualTo("series:ext-hormuz-transits/transit-count/tanker");
    }
}
