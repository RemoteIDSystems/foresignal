package ai.foresignal.outlook.regional;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingLogEntity;
import ai.foresignal.analytics.finding.FindingLogRepository;
import ai.foresignal.analytics.math.NbSampling;
import ai.foresignal.baserate.RegionProfileEntity;
import ai.foresignal.baserate.RegionProfileRepository;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolutionService;
import ai.foresignal.outlook.series.SeriesForecastService;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Regional outlooks end to end (prompt 126): six 4-week windows per eligible region and family from reference
 * history, the admin-1 eligibility rule, a numeric ledger question per window forecast by {@code statistical}, the
 * skill and p10–p90 coverage carried on the outlook and its records, the {@code LOW_REPORTING_DENSITY} flag, the
 * {@code outlook.published} event, and an {@code outlook-exceedance} finding when a window resolves above p95.
 */
@IntegrationTest
class RegionalOutlookIT {

    /** A Monday, 06:00 UTC. */
    static final Instant NOW = Instant.parse("2026-10-19T06:00:00Z");

    static final LocalDate MONDAY = LocalDate.of(2026, 10, 19);

    static final int WEEKS = 200;

    @Autowired MutableClock clock;
    @Autowired RegionalOutlookService service;
    @Autowired RegionalOutlookRepository outlooks;
    @Autowired OutlookFamilies families;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry forecasters;
    @Autowired ResolutionService resolution;
    @Autowired ReferenceImportRepository imports;
    @Autowired RegionProfileRepository profiles;
    @Autowired AnalyticFindingRepository findings;
    @Autowired FindingLogRepository logs;
    @Autowired JdbcTemplate jdbc;
    @Autowired ai.foresignal.outlook.ForesignalOutlookGenerator generator;

    String importId;

    void history(String region, double mean, LocalDate from, LocalDate to, long seed) {
        NbSampling sampler = new NbSampling(seed);
        for (LocalDate w = from; w.isBefore(to); w = w.plusWeeks(1)) {
            insert(region, w, sampler.negBin(mean, 8));
        }
    }

    void insert(String region, LocalDate week, long count) {
        jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                + " licence, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?) ON CONFLICT DO NOTHING",
                ReferenceFamilies.BATTLES, region, Date.valueOf(week), ReferenceFamilies.UCDP, count, "CC-BY-4.0",
                "OPEN", importId);
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        forecasters.registerBuiltIns();
        ReferenceImportEntity ucdp = new ReferenceImportEntity(Ulids.next(NOW), "ucdp-ged", "k-outlook", "test", NOW);
        ucdp.setStatus("done");
        ucdp.setLicense("CC-BY-4.0");
        ucdp.setEgress("OPEN");
        imports.save(ucdp);
        importId = ucdp.getId();
        LocalDate first = MONDAY.minusWeeks(WEEKS);
        history("YE", 12, first, MONDAY, 1);
        history("YE-SA", 9, first, MONDAY, 2);
        history("YE-AD", 1, first, MONDAY, 3);
    }

    OutlookFamilies.Family battles() {
        return families.find(ReferenceFamilies.BATTLES).orElseThrow();
    }

    @Test
    @DisplayName("six windows per eligible region, admin-1 only with 52 weeks of 5+ events, each a scored question")
    void publishes() {
        RegionalOutlookService.RunResult result = service.publishAll(NOW);
        assertThat(result.errors()).isZero();
        RegionalOutlookEntity ye = outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc("YE",
                ReferenceFamilies.BATTLES).orElseThrow();
        assertThat(ye.getWindows()).hasSize(RegionalOutlookModel.WINDOWS);
        assertThat(ye.getWindows().get(0)).containsEntry("windowStart", "2026-10-26")
                .containsEntry("windowEnd", "2026-11-22");
        assertThat(ye.getWindows().get(5)).containsEntry("windowStart", "2027-03-15");
        assertThat(ye.getRegionLevel()).isEqualTo("country");
        assertThat(ye.getSource()).isEqualTo(ReferenceFamilies.UCDP);
        assertThat(ye.getManifestId()).isNotNull();
        assertThat(ye.isLatest()).isTrue();
        assertThat(outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc("YE-SA", ReferenceFamilies.BATTLES))
                .get().satisfies(o -> assertThat(o.getRegionLevel()).isEqualTo("admin1"));
        assertThat(outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc("YE-AD", ReferenceFamilies.BATTLES))
                .as("an admin-1 region without 52 weeks of 5+ events").isEmpty();
        for (Map<String, Object> w : ye.getWindows()) {
            String questionId = String.valueOf(w.get("questionId"));
            ForecastQuestionEntity q = ledger.find(questionId).orElseThrow();
            assertThat(q.getType()).isEqualTo(Ledger.NUMERIC);
            assertThat(q.getResolverType()).isEqualTo("count-threshold");
            assertThat(q.getResolver()).doesNotContainKey("op");
            assertThat(q.getOutlookId()).isEqualTo(ye.getId());
            assertThat(q.getCategory()).isEqualTo("conflict");
            assertThat(ledger.latestByForecaster(questionId)).containsKey(SeriesForecastService.FORECASTER);
        }
        assertThat(logs.findAll()).extracting(FindingLogEntity::getEvent).contains(FindingEvent.OUTLOOK_PUBLISHED);
        // A second publication replaces the latest flag.
        clock.setInstant(NOW.plus(Duration.ofDays(7)));
        service.publishAll(NOW.plus(Duration.ofDays(7)));
        assertThat(outlooks.findByLatestTrueOrderByIdAsc()).filteredOn(o -> o.getRegion().equals("YE")).hasSize(1);
    }

    @Test
    @DisplayName("skill and coverage travel with the outlook and its records; low reporting density is flagged")
    void skillAndFlags() {
        profiles.save(new RegionProfileEntity("YE", 0.01, 1, NOW));
        service.publishAll(NOW);
        RegionalOutlookEntity ye = outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc("YE",
                ReferenceFamilies.BATTLES).orElseThrow();
        assertThat(ye.getSkill()).containsKeys("n", "label", "meanPinballLoss", "skillVsSeasonalNaive",
                "skillVsClimatology", "intervalCoverageP10P90", "evaluatedWeeks");
        assertThat(ye.getSkill()).containsEntry("label", "insufficient");
        assertThat(ye.isLowReportingDensity()).isTrue();
        assertThat(ye.getCaveats()).extracting(c -> c.get("code")).contains("LOW_REPORTING_DENSITY");
        Map<String, Object> payload = ai.foresignal.outlook.ForesignalOutlookGenerator.outlookPayload(ye,
                ye.getWindows().get(0));
        assertThat(payload).containsKeys("skill", "intervalCoverageP10P90", "lowReportingDensity", "quantiles",
                "questionId");
        assertThat(payload).containsEntry("lowReportingDensity", true);
        assertThat(service.skill("YE", ReferenceFamilies.BATTLES)).containsEntry("n", 0);
    }

    @Test
    @DisplayName("a window resolving above p95 records the actual and writes an outlook-exceedance finding")
    void exceedanceOnResolution() {
        service.publishAll(NOW);
        RegionalOutlookEntity ye = outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc("YE",
                ReferenceFamilies.BATTLES).orElseThrow();
        Map<String, Object> first = ye.getWindows().get(0);
        LocalDate start = LocalDate.parse(first.get("windowStart").toString());
        for (int k = 0; k < RegionalOutlookModel.WINDOW_WEEKS; k++) {
            insert("YE", start.plusWeeks(k), 400);
        }
        Instant after = start.plusWeeks(4).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().plus(Duration.ofDays(8));
        clock.setInstant(after);
        resolution.runOnce();
        ForecastQuestionEntity q = ledger.find(first.get("questionId").toString()).orElseThrow();
        assertThat(q.getStatus()).isEqualTo("resolved");
        assertThat(((Number) q.getResolution().get("value")).doubleValue()).isEqualTo(1600.0);
        service.onResolved(q, after);
        RegionalOutlookEntity updated = outlooks.findById(ye.getId()).orElseThrow();
        Map<String, Object> w = updated.getWindows().get(0);
        assertThat(w).containsEntry("status", "resolved").containsEntry("exceedance", "above-p95")
                .containsEntry("withinP10P90", false);
        assertThat(((Number) w.get("actual")).longValue()).isEqualTo(1600L);
        List<AnalyticFindingEntity> exceedances = findings.findAll().stream()
                .filter(f -> "outlook-exceedance".equals(f.getFindingType())).toList();
        assertThat(exceedances).hasSize(1);
        assertThat(exceedances.get(0).getRegionCodes()).containsExactly("YE");
    }

    @Test
    @DisplayName("foresignal-outlook records: one per window with externalId {region}:{family}:{windowStart}, on change")
    void records() {
        service.publishAll(NOW);
        ai.foresignal.derive.DerivedContext ctx = org.mockito.Mockito.mock(ai.foresignal.derive.DerivedContext.class);
        org.mockito.ArgumentCaptor<ai.foresignal.source.spi.NormalizedRecord> captor = org.mockito.ArgumentCaptor
                .forClass(ai.foresignal.source.spi.NormalizedRecord.class);
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW.plusSeconds(30), ctx);
        org.mockito.Mockito.verify(ctx, org.mockito.Mockito.atLeast(RegionalOutlookModel.WINDOWS)).emit(
                captor.capture());
        assertThat(captor.getAllValues()).extracting(ai.foresignal.source.spi.NormalizedRecord::externalId)
                .contains("YE:conflict:battles:2026-10-26", "YE:conflict:battles:2027-03-15");
        assertThat(captor.getAllValues()).allSatisfy(r -> assertThat(r.kind()).isEqualTo("regional-outlook"));
        int emitted = captor.getAllValues().size();
        generator.onTick(NOW.plusSeconds(90), ctx);
        org.mockito.Mockito.verify(ctx, org.mockito.Mockito.times(emitted)).emit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("history: past windows with actuals; list and detail reads; unknown outlooks are 404")
    void reads() {
        service.publishAll(NOW);
        RegionalOutlookService.Page page = service.list(ReferenceFamilies.BATTLES, null, null, 1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.nextCursor()).isNotNull();
        assertThat(service.list(ReferenceFamilies.BATTLES, null, page.nextCursor(), 10).items()).isNotEmpty();
        assertThat(service.history("YE", ReferenceFamilies.BATTLES, 10)).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.latest("FR", ReferenceFamilies.BATTLES))
                .isInstanceOf(ai.foresignal.api.error.NotFoundException.class);
        assertThat(battles().category()).isEqualTo("conflict");
    }
}
