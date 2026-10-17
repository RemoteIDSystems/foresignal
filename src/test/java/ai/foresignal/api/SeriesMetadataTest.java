package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.dto.SeriesInfoDto;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.cftc.CftcCotAdapter;
import ai.foresignal.source.adapters.cftc.CotParser;
import ai.foresignal.source.adapters.fred.FredSeriesAdapter;
import ai.foresignal.source.series.EnergySeriesMetadata;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The series metadata of prompt 117: {@code family}, {@code transform}, {@code releaseLagDays} and {@code egress}
 * reach the {@code /v1/series} catalog entry for 093 series (from the mapping table, before and after their next
 * poll) and for 117 series (from the {@code series-latest} payload), and {@link SeriesMeta} validates its values.
 */
class SeriesMetadataTest {

    private static final Instant T = Instant.parse("2026-10-13T00:00:00Z");

    private static SeriesQueryService service(Map<String, Boolean> localOnly) {
        SeriesQueryService service = new SeriesQueryService(mock(NamedParameterJdbcTemplate.class),
                JsonMapper.builder().build());
        EgressPolicy egress = mock(EgressPolicy.class);
        when(egress.isLocalOnly(anyString())).thenAnswer(i -> localOnly.getOrDefault(i.getArgument(0), false));
        SourceRegistry registry = mock(SourceRegistry.class);
        // Only the stubbed sources are registered: a real registry never contains an id without a descriptor.
        when(registry.contains(anyString())).thenAnswer(i -> java.util.Set.of(FredSeriesAdapter.ID, CftcCotAdapter.ID,
                "eia-petroleum", "gie-agsi", "eia-930").contains(i.<String>getArgument(0)));
        when(registry.descriptor(FredSeriesAdapter.ID)).thenReturn(new FredSeriesAdapter("").descriptor());
        when(registry.descriptor(CftcCotAdapter.ID)).thenReturn(new CftcCotAdapter("").descriptor());
        when(registry.descriptor("eia-petroleum")).thenReturn(
                new ai.foresignal.source.adapters.eia.EiaPetroleumAdapter("").descriptor());
        when(registry.descriptor("gie-agsi")).thenReturn(
                new ai.foresignal.source.adapters.gie.AgsiAdapter("").descriptor());
        when(registry.descriptor("eia-930")).thenReturn(
                new ai.foresignal.source.adapters.eia.Eia930Adapter("").descriptor());
        service.setEgressPolicy(egress);
        service.setRegistry(registry);
        return service;
    }

    private static Map<String, Object> payload(NormalizedRecord record) {
        return new LinkedHashMap<>(record.payload());
    }

    @Test
    @DisplayName("093 series written before 117: the mapping table supplies family, transform and lag")
    void energyFallback() {
        SeriesQueryService service = service(Map.of("eia-petroleum", true));
        NormalizedRecord rwtc = SeriesPoint.latest("eia:RWTC", "WTI", "petroleum/pri/spt", "P1D", "2026-10-13",
                61.2, "[USD]/[bbl_us]", null, "final", T);
        SeriesInfoDto wti = service.info("eia-petroleum", payload(rwtc));
        assertThat(wti.family()).isEqualTo("price");
        assertThat(wti.transform()).isEqualTo("log-return");
        assertThat(wti.releaseLagDays()).isEqualTo(5.0);
        assertThat(wti.egress()).isEqualTo("LOCAL_ONLY");

        NormalizedRecord agsi = SeriesPoint.latest("agsi:DE", "Germany", "agsi/storage", "P1D", "2026-10-13", 92.3,
                "%", "DE", "final", T);
        SeriesInfoDto storage = service.info("gie-agsi", payload(agsi));
        assertThat(storage.family()).isEqualTo("index");
        assertThat(storage.transform()).isEqualTo("difference");
        assertThat(storage.egress()).isEqualTo("OPEN");

        NormalizedRecord demand = SeriesPoint.latest("eia930:ERCO:D", "ERCOT demand", "electricity/rto/region-data",
                "PT1H", "2026-10-13T00", 41000.0, "MW.h", "ERCO", "final", T);
        SeriesInfoDto load = service.info("eia-930", payload(demand));
        assertThat(load.family()).isEqualTo("count");
        assertThat(load.transform()).isEqualTo("log-level");
    }

    @Test
    @DisplayName("093 series after their next poll carry the metadata in the series-latest payload")
    void energyAfterPoll() {
        NormalizedRecord point = SeriesPoint.point("agsi:DE", "Germany", "agsi/storage", "P1D", "2026-10-13", T,
                T.plusSeconds(86_400), 92.3, "%", "DE", null, "final", T);
        List<NormalizedRecord> latest = SeriesPoint.latestPerSeries(List.of(point),
                EnergySeriesMetadata.forSource(new ai.foresignal.source.adapters.gie.AgsiAdapter("").descriptor()));
        assertThat(latest.get(0).payload()).containsEntry("family", "index").containsEntry("transform", "difference")
                .containsEntry("releaseLagDays", 1.0).containsEntry("egress", "OPEN");
        assertThat(EnergySeriesMetadata.lookup("ember-electricity", "ember:DE:carbon-intensity", null).family())
                .isEqualTo("index");
        assertThat(EnergySeriesMetadata.lookup("ercot-public", "ercot:spp:HB_HOUSTON", null).family())
                .isEqualTo("price");
        assertThat(EnergySeriesMetadata.lookup("gb-carbon-intensity", "carbon:gb:national", null).family())
                .isEqualTo("index");
        assertThat(EnergySeriesMetadata.lookup("eia-petroleum", "eia:WCESTUS1", "petroleum/stoc/wstk").family())
                .isEqualTo("volume");
        assertThat(EnergySeriesMetadata.lookup("unknown-source", "x", null)).isNull();
    }

    @Test
    @DisplayName("117 series: family, transform, lag, egress, topic, region and attribution from the payload")
    void marketSeries() {
        SeriesQueryService service = service(Map.of(FredSeriesAdapter.ID, true));
        NormalizedRecord fred = new SeriesMeta("rate", "difference", 1, "LOCAL_ONLY", "US", null).applyTo(
                SeriesPoint.withPayload(SeriesPoint.latest("fred.DGS10", "10-year", "fred/DGS10", "P1D",
                        "2026-10-14", 4.1, "%", "US", "final", T), "attribution", "This product uses the FRED"));
        SeriesInfoDto rate = service.info(FredSeriesAdapter.ID, payload(fred));
        assertThat(rate.family()).isEqualTo("rate");
        assertThat(rate.releaseLagDays()).isEqualTo(1.0);
        assertThat(rate.egress()).isEqualTo("LOCAL_ONLY");
        assertThat(rate.regionCode()).isEqualTo("US");
        assertThat(rate.attribution()).startsWith("This product uses the FRED");

        NormalizedRecord cot = CotParser.meta(SeriesPoint.point("cot.067651.legacy.oi", "WTI oi", "legacy", "P1W",
                "2026-10-13", T, T.plusSeconds(86_400), 1.9e6, "{contracts}", null, Map.of("measure", "oi"), "final",
                T)).withEgress(Egress.OPEN).applyTo(SeriesPoint.latest("cot.067651.legacy.oi", "WTI oi", "legacy",
                "P1W", "2026-10-13", 1.9e6, "{contracts}", null, "final", T));
        SeriesInfoDto oi = service.info(CftcCotAdapter.ID, payload(cot));
        assertThat(oi.family()).isEqualTo("open-interest");
        assertThat(oi.transform()).isEqualTo("difference");
        assertThat(oi.releaseLagDays()).isEqualTo(3.0);
        assertThat(oi.egress()).isEqualTo("OPEN");
        assertThat(oi.attribution()).contains("Commodity Futures Trading Commission");

        NormalizedRecord topic = new SeriesMeta("attention", "log-level", 0, "OPEN", null, "red-sea-shipping")
                .applyTo(SeriesPoint.latest("gdelt.red-sea-shipping.vol", "volume", "gdelt-doc", "PT15M",
                        "2026-10-16T00:00:00Z", 12.0, "{articles}", null, "final", T));
        assertThat(service.info("gdelt-timelines", payload(topic)).topicId()).isEqualTo("red-sea-shipping");
    }

    @Test
    @DisplayName("SeriesMeta refuses unknown families, transforms, negative lags and unknown egress")
    void validation() {
        assertThatThrownBy(() -> SeriesMeta.of("weather", "level", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SeriesMeta.of("price", "log", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SeriesMeta.of("price", "level", -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SeriesMeta("price", "level", 0, "PUBLIC", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(SeriesMeta.FAMILIES).hasSize(10);
        assertThat(SeriesMeta.fromPayload(Map.of("family", "price"))).isNull();
        SeriesMeta round = SeriesMeta.fromPayload(new SeriesMeta("price", "log-return", 5, "LOCAL_ONLY", "US",
                "topic-x").fields());
        assertThat(round).isEqualTo(new SeriesMeta("price", "log-return", 5, "LOCAL_ONLY", "US", "topic-x"));
    }
}
