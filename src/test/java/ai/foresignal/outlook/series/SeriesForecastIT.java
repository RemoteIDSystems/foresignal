package ai.foresignal.outlook.series;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.baseline.BaselineRunner;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The series forecaster end to end (prompt 126): a registered daily series gets a method selected by rolling-origin
 * backtest and nightly quantile forecasts for 1, 7 and 28 days with a manifest; configured series become numeric
 * ledger questions forecast by {@code statistical}, then by the {@code climatology} and {@code persistence}
 * baselines; a realized value outside the 1-step band becomes a {@code forecast-exceedance} finding.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class SeriesForecastIT extends AnalyticsItSupport {

    static final String ID = "transits";

    static final String REF = "series:" + AnalyticsTestSources.SERIES + "/" + ID;

    static final Instant START = T0.minus(Duration.ofDays(400));

    static final int DAYS = 420;

    static final Instant NOW = START.plus(Duration.ofDays(DAYS)).plus(Duration.ofHours(3)).plus(
            Duration.ofMinutes(30));

    @Autowired SeriesForecastService service;
    @Autowired SeriesForecastModelRepository models;
    @Autowired SeriesForecastRepository forecasts;
    @Autowired AnalysisSeriesService registry;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry forecasters;
    @Autowired BaselineRunner baselines;
    @Autowired AnalyticFindingRepository findings;
    @Autowired JsonMapper json;

    @BeforeEach
    void seedSeries() throws Exception {
        forecasters.registerBuiltIns();
        double[] values = new double[DAYS];
        Random random = new Random(4);
        int[] weekly = {0, 4, 6, 5, 3, -8, -10};
        for (int i = 0; i < DAYS; i++) {
            values[i] = 60 + weekly[i % 7] + random.nextGaussian();
        }
        seedDaily(AnalyticsTestSources.SERIES, ID, START, values, SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LOG_RETURN, 1));
        clock.setInstant(NOW);
        registry.upsert(json.readTree("{\"ref\":\"" + REF + "\",\"frequency\":\"day\",\"detectors\":[\"forecast\"]}"));
        settings.put(SettingKeys.ANALYSIS_FORECASTS_QUESTION_SERIES, List.of(REF));
    }

    @Test
    @DisplayName("the nightly run selects a method, stores three monotone horizons and opens scored questions")
    void nightly() {
        SeriesForecastService.RunResult result = service.runNightly(NOW);
        assertThat(result.series()).isEqualTo(1);
        assertThat(result.errors()).isZero();
        SeriesForecastModelEntity model = models.findById(REF).orElseThrow();
        assertThat(MethodSelector.METHODS).extracting(ForecastMethod::id).contains(model.getMethod());
        assertThat(model.getMethod()).isNotEqualTo(NbGlmMethod.ID);
        assertThat(model.getBacktest()).containsKeys("origins", "results", "selectionHorizon");
        assertThat(model.getManifestId()).isNotNull();
        List<SeriesForecastEntity> rows = service.latest(REF);
        assertThat(rows).extracting(SeriesForecastEntity::getHorizon).containsExactly(1, 7, 28);
        for (SeriesForecastEntity row : rows) {
            assertThat(row.getQuantiles()).containsOnlyKeys(Ledger.QUANTILES);
            double previous = Double.NEGATIVE_INFINITY;
            for (String key : Ledger.QUANTILES) {
                assertThat(row.getQuantiles().get(key)).isGreaterThanOrEqualTo(previous);
                previous = row.getQuantiles().get(key);
            }
            assertThat(row.getTails()).containsKeys("p01", "p99");
            assertThat(row.getManifestId()).isNotNull();
            assertThat(row.getQuestionId()).isNotNull();
            assertThat(row.getTargetStart()).isAfterOrEqualTo(NOW.minus(Duration.ofDays(1)));
        }
        assertThat(result.questions()).isEqualTo(3);
        // A second run the same night writes nothing new.
        assertThat(service.runNightly(NOW).forecasts()).isZero();
    }

    @Test
    @DisplayName("ledger questions: numeric series-value, statistical forecast, then climatology and persistence")
    void ledgerQuestions() {
        service.runNightly(NOW);
        SeriesForecastEntity day7 = service.latest(REF).stream().filter(r -> r.getHorizon() == 7).findFirst()
                .orElseThrow();
        ForecastQuestionEntity q = ledger.find(day7.getQuestionId()).orElseThrow();
        assertThat(q.getType()).isEqualTo(Ledger.NUMERIC);
        assertThat(q.getResolverType()).isEqualTo("series-value");
        assertThat(q.getSeriesRef()).isEqualTo(REF);
        assertThat(q.getCategory()).isEqualTo("markets");
        assertThat(q.getOrigin()).isEqualTo("system");
        Map<String, ForecastEntity> latest = ledger.latestByForecaster(q.getId());
        assertThat(latest).containsKey(SeriesForecastService.FORECASTER);
        assertThat(latest.get(SeriesForecastService.FORECASTER).getQuantiles()).isEqualTo(day7.getQuantiles());
        baselines.runFor(q.getId());
        Map<String, ForecastEntity> after = ledger.latestByForecaster(q.getId());
        assertThat(after).containsKeys(ForecasterRegistry.CLIMATOLOGY, ForecasterRegistry.PERSISTENCE);
        assertThat(service.questionIds(REF)).contains(q.getId());
        assertThat(forecasters.find(SeriesForecastService.FORECASTER)).get()
                .satisfies(f -> assertThat(f.getType()).isEqualTo("statistical"));
    }

    @Test
    @DisplayName("a realized value outside p01–p99 of the 1-step forecast is a significant forecast-exceedance")
    void exceedance() {
        service.runNightly(NOW);
        SeriesForecastEntity day1 = service.latest(REF).stream().filter(r -> r.getHorizon() == 1).findFirst()
                .orElseThrow();
        Instant published = day1.getTargetEnd().plusSeconds(60);
        seed(AnalyticsTestSources.SERIES, published, dailyPoint(ID, day1.getTargetStart(), 600, published));
        Instant later = day1.getTargetEnd().plus(Duration.ofDays(2));
        clock.setInstant(later);
        assertThat(service.checkExceedances(later)).isEqualTo(1);
        AnalyticFindingEntity f = findings.findAll().stream()
                .filter(x -> "forecast-exceedance".equals(x.getFindingType())).findFirst().orElseThrow();
        assertThat(f.getSeverity()).isEqualTo("significant");
        assertThat(f.getStats()).containsEntry("direction", "above").containsEntry("band", "outside-p01-p99");
        assertThat(f.getSeriesRefs()).containsExactly(REF);
        assertThat(forecasts.findById(day1.getId()).orElseThrow().isExceedanceChecked()).isTrue();
        // Checked once only.
        assertThat(service.checkExceedances(later)).isZero();
    }

    @Test
    @DisplayName("a value inside the band writes nothing; series without the forecast detector are not forecast")
    void insideAndIneligible() throws Exception {
        service.runNightly(NOW);
        SeriesForecastEntity day1 = service.latest(REF).stream().filter(r -> r.getHorizon() == 1).findFirst()
                .orElseThrow();
        double median = day1.getQuantiles().get("p50");
        Instant published = day1.getTargetEnd().plusSeconds(60);
        seed(AnalyticsTestSources.SERIES, published, dailyPoint(ID, day1.getTargetStart(), median, published));
        Instant later = day1.getTargetEnd().plus(Duration.ofDays(2));
        clock.setInstant(later);
        assertThat(service.checkExceedances(later)).isZero();
        registry.upsert(json.readTree("{\"ref\":\"" + REF + "\",\"frequency\":\"day\",\"detectors\":"
                + "[\"changepoint-cusum\"]}"));
        assertThat(service.runNightly(later.plus(Duration.ofDays(1))).series()).isZero();
    }
}
