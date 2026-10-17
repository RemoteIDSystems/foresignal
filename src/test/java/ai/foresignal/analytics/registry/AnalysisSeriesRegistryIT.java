package ai.foresignal.analytics.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.stats.RegionRollupResolution;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The analysis-series registry (prompt 119): auto-registration thresholds (30 non-zero region-days in 90, every daily
 * series), the cap keeping the busiest rows, operator rows kept, and the upsert and delete validation.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class AnalysisSeriesRegistryIT extends AnalyticsItSupport {

    @Autowired AnalysisSeriesRegistrar registrar;
    @Autowired AnalysisSeriesService service;
    @Autowired AnalysisSeriesRepository rows;
    @Autowired JsonMapper json;

    /** Writes {@code days} cross-source region-day rows of {@code kind} in {@code region} with {@code entities}. */
    private void regionDays(String kind, String region, int days, long entities, Instant end) {
        for (int i = 1; i <= days; i++) {
            jdbc.update("INSERT INTO activity_rollup (resolution, bucket_start, source_id, kind, cell, region_code,"
                    + " versions, entities, firsts, tombstones, edges, changes) VALUES (?, ?, '*', ?, ?, ?, ?, ?, 0, 0,"
                    + " 0, 0)", RegionRollupResolution.DAY.code(), Timestamp.from(end.minus(Duration.ofDays(i))), kind,
                    ai.foresignal.stats.RegionRollupComputer.regionCell(region), region, entities, entities);
        }
    }

    @Test
    @DisplayName("region-days need 30 non-zero days in 90; daily series register; operator rows are kept")
    void autoRegistration() {
        Instant now = T0.plus(Duration.ofDays(100));
        regionDays("earthquake", "JP", 31, 3, now);
        regionDays("earthquake", "CL", 29, 50, now);
        seedDaily(AnalyticsTestSources.SERIES, "oil", T0, constant(5, 70),
                SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 1));
        clock.setInstant(now);
        service.upsert(json.readTree("{\"ref\":\"series:" + AnalyticsTestSources.SERIES + "/oil\",\"frequency\":"
                + "\"day\",\"family\":\"markets\"}"));

        AnalysisSeriesRegistrar.Result result = registrar.run(now);

        assertThat(rows.findById("rollup:day/*/earthquake/JP")).get().satisfies(r -> {
            assertThat(r.getOrigin()).isEqualTo("auto");
            assertThat(r.getFamily()).isEqualTo("seismic");
            assertThat(r.getRegionCode()).isEqualTo("JP");
            assertThat(r.getDetectors()).containsExactly("changepoint-cusum", "changepoint-bocpd");
        });
        assertThat(rows.findById("rollup:day/*/earthquake/CL")).as("29 days are too few").isEmpty();
        assertThat(rows.findById("series:" + AnalyticsTestSources.SERIES + "/oil")).get()
                .satisfies(r -> assertThat(r.getOrigin()).as("the operator row is untouched").isEqualTo("operator"));
        assertThat(result.candidates()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("the cap keeps the busiest automatic rows and removes the rest; operator rows count against it")
    void cap() {
        Instant now = T0.plus(Duration.ofDays(100));
        regionDays("earthquake", "JP", 40, 30, now);
        regionDays("earthquake", "NZ", 40, 10, now);
        regionDays("earthquake", "PE", 40, 1, now);
        settings.put(SettingKeys.ANALYSIS_SERIES_MAX, 2);
        registrar.run(now);
        assertThat(rows.findByOrigin("auto")).extracting(AnalysisSeriesEntity::getRef)
                .containsExactlyInAnyOrder("rollup:day/*/earthquake/JP", "rollup:day/*/earthquake/NZ");

        clock.setInstant(now);
        service.upsert(json.readTree("{\"ref\":\"rollup:day/*/earthquake/PE\"}"));
        AnalysisSeriesRegistrar.Result second = registrar.run(now);
        assertThat(rows.findByOrigin("auto")).extracting(AnalysisSeriesEntity::getRef)
                .containsExactly("rollup:day/*/earthquake/JP");
        assertThat(second.removed()).isEqualTo(1);
        assertThat(rows.findById("rollup:day/*/earthquake/PE")).get()
                .satisfies(r -> assertThat(r.getOrigin()).isEqualTo("operator"));
    }

    @Test
    @DisplayName("upsert validates the reference, detectors, family, region and watch pairs; delete needs a row")
    void upsertAndDelete() {
        regionDays("earthquake", "JP", 2, 3, T0);
        clock.setInstant(T0);
        AnalysisSeriesService.Upsert created = service.upsert(json.readTree("{\"ref\":\"rollup:day/*/earthquake/JP\","
                + "\"watchPairs\":[{\"eventFamily\":\"aviation-behaviour\",\"regionCodes\":[\"JP\"]}]}"));
        assertThat(created.created()).isTrue();
        assertThat(WatchPair.of(created.row())).singleElement().satisfies(p -> {
            assertThat(p.eventFamily()).as("101 aliases resolve").isEqualTo("aviation-ops");
            assertThat(p.minEffect()).isEqualTo(2.0);
        });
        assertThat(service.upsert(json.readTree("{\"ref\":\"rollup:day/*/earthquake/JP\",\"enabled\":false}"))
                .created()).isFalse();
        assertThat(rows.findById("rollup:day/*/earthquake/JP").orElseThrow().isEnabled()).isFalse();
        assertThat(service.list(List.of("seismic"), List.of(), null, null, 10).items()).hasSize(1);
        assertThat(service.list(List.of(), List.of(), true, null, 10).items()).isEmpty();

        assertThatThrownBy(() -> service.upsert(json.readTree("{\"ref\":\"bogus\"}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.upsert(json.readTree("{\"ref\":\"series:" + AnalyticsTestSources.SERIES
                + "/missing\"}"))).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.upsert(json.readTree("{\"ref\":\"rollup:day/*/earthquake/JP\","
                + "\"detectors\":[\"magic\"],\"family\":\"nope\",\"regionCode\":\"jp\"}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).hasSize(3));

        service.delete("rollup:day/*/earthquake/JP");
        assertThatThrownBy(() -> service.delete("rollup:day/*/earthquake/JP")).isInstanceOf(NotFoundException.class);
    }
}
