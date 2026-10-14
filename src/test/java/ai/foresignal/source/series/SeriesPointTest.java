package ai.foresignal.source.series;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SeriesPoint} (prompt 093): the external id, UCUM units, numeric-string parsing, NaN rejection and the
 * {@code series-latest} selection.
 */
class SeriesPointTest {

    private static final Instant START = Instant.parse("2026-10-09T00:00:00Z");
    private static final Instant END = Instant.parse("2026-10-10T00:00:00Z");

    @Test
    @DisplayName("a point: externalId {series}:{period}, no geometry, eventTime = periodStart")
    void point() {
        NormalizedRecord record = SeriesPoint.point("agsi:DE", "Germany", "agsi/storage", "P1D", "2026-10-09",
                START, END, 92.31, "%", "DE", Map.of("gasInStorageTWh", 224.5), "final", END);
        assertThat(record.externalId()).isEqualTo("agsi:DE:2026-10-09");
        assertThat(record.kind()).isEqualTo("series-point");
        assertThat(record.geometry()).isNull();
        assertThat(record.eventTime()).isEqualTo(START);
        assertThat(record.observedAt()).isEqualTo(END);
        assertThat(record.payload()).containsEntry("series", "agsi:DE").containsEntry("seriesName", "Germany")
                .containsEntry("unit", "%").containsEntry("value", 92.31).containsEntry("status", "final")
                .containsEntry("periodStart", START.toString()).containsEntry("periodEnd", END.toString());
        assertThat(record.payload()).containsEntry("extra", Map.of("gasInStorageTWh", 224.5));
    }

    @Test
    @DisplayName("the payload has exactly the declared keys and no nulls")
    void payloadKeys() {
        NormalizedRecord record = SeriesPoint.point("eia:RWTC", "WTI", "petroleum/pri/spt", "P1D", "2026-10-09",
                START, END, 61.23, "[USD]/[bbl_us]", null, null, "final", END);
        assertThat(record.payload().keySet()).containsExactlyInAnyOrder("series", "seriesName", "dataset",
                "frequency", "period", "periodStart", "periodEnd", "value", "unit", "status");
        assertThat(record.payload()).doesNotContainKey("area").doesNotContainKey("extra");
    }

    @Test
    @DisplayName("a latest record is {series}:latest and skips forecasts")
    void latestSelection() {
        NormalizedRecord old = SeriesPoint.point("carbon:gb:national", "GB", "carbon-intensity", "PT30M",
                "2026-10-13T00:00Z", Instant.parse("2026-10-13T00:00:00Z"), Instant.parse("2026-10-13T00:30:00Z"),
                120.0, "g/(kW.h)", "GB", null, "final", Instant.parse("2026-10-13T00:00:00Z"));
        NormalizedRecord forecast = SeriesPoint.point("carbon:gb:national", "GB", "carbon-intensity", "PT30M",
                "2026-10-13T00:30Z", Instant.parse("2026-10-13T00:30:00Z"), Instant.parse("2026-10-13T01:00:00Z"),
                110.0, "g/(kW.h)", "GB", null, "forecast", Instant.parse("2026-10-13T00:30:00Z"));
        List<NormalizedRecord> latest = SeriesPoint.latestPerSeries(List.of(old, forecast));
        assertThat(latest).hasSize(1);
        NormalizedRecord record = latest.get(0);
        assertThat(record.externalId()).isEqualTo("carbon:gb:national:latest");
        assertThat(record.kind()).isEqualTo("series-latest");
        assertThat(record.payload()).containsEntry("period", "2026-10-13T00:00Z").containsEntry("value", 120.0)
                .containsEntry("status", "final");
    }

    @Test
    @DisplayName("a series with only forecast points gets no latest record")
    void forecastOnly() {
        NormalizedRecord forecast = SeriesPoint.point("eia930:ERCO:DF", "ERCOT Demand forecast", "region", "PT1H",
                "2026-10-13T00", START, END, 40900.0, "MW.h", "ERCO", null, "forecast", END);
        assertThat(SeriesPoint.latestPerSeries(List.of(forecast))).isEmpty();
    }

    @Test
    @DisplayName("periods: a daily label, an hourly label and a month all map to start and end")
    void periods() {
        assertThat(SeriesPeriods.parse("P1D", "2026-10-09").start()).isEqualTo(START);
        assertThat(SeriesPeriods.parse("P1D", "2026-10-09").end()).isEqualTo(END);
        SeriesPeriods.Period hour = SeriesPeriods.parse("PT1H", "2026-10-09T14");
        assertThat(hour.start()).isEqualTo(Instant.parse("2026-10-09T14:00:00Z"));
        assertThat(hour.end()).isEqualTo(Instant.parse("2026-10-09T15:00:00Z"));
        SeriesPeriods.Period month = SeriesPeriods.parse("P1M", "2026-08");
        assertThat(month.start()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
        assertThat(month.end()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(SeriesPeriods.parse("P1D", "not-a-date")).isNull();
    }
}
