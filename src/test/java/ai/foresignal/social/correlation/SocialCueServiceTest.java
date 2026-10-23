package ai.foresignal.social.correlation;

import static ai.foresignal.social.correlation.AttentionGapDetectorTest.provider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.social.AuthorKeys;
import ai.foresignal.social.CueRequests;
import ai.foresignal.social.SocialBaseline;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialEvents;
import ai.foresignal.social.SocialLanguages;
import ai.foresignal.social.SocialPlatformCatalog;
import ai.foresignal.social.SocialPlatformService;
import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.TermExpansion;
import ai.foresignal.social.TermIndex;
import ai.foresignal.social.VolumeAccumulator;
import ai.foresignal.social.XSpend;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link SocialCueService} and {@link CueQueryBuilder} (prompt 137): query building from places, aliases, facilities
 * and family lexicons in the region's languages; the per-cue cost cap and the daily cap; X spend refusal; cue results
 * never feed baselines or region coverage (SOC-3); a cue loop does not self-corroborate.
 */
class SocialCueServiceTest {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    static SocialSettingsService.Cues cues(int maxPerDay, double maxCost, boolean xai) {
        return new SocialSettingsService.Cues(true, new SocialSettingsService.Triggers(true, true,
                List.of("change-point"), true, true, List.of("conflict"), 2), maxPerDay, maxCost, List.of(), xai, 20);
    }

    static SocialPlatformSettings xSettings() {
        return SocialPlatformSettings.defaults(SocialPlatformCatalog.get("x").orElseThrow());
    }

    record Harness(SocialCueService service, ComparisonStore store, SocialPlatformService platforms, XSpend spend,
            CueRequests requests) {}

    static Harness harness(SocialSettingsService.Cues c, int cuesToday, List<String> enabled) {
        ComparisonStore store = mock(ComparisonStore.class);
        when(store.cuesSince(any())).thenReturn(cuesToday);
        SocialSettingsService settings = mock(SocialSettingsService.class);
        when(settings.cues()).thenReturn(c);
        SocialPlatformService platforms = mock(SocialPlatformService.class);
        for (String p : CorrelationVocabulary.CUE_ADAPTERS) {
            if (!enabled.contains(p)) {
                doThrow(new ConflictException(ErrorCodes.PLATFORM_DISABLED, "off")).when(platforms).requireEnabled(p);
            }
        }
        when(platforms.settings("x")).thenReturn(xSettings());
        XSpend spend = mock(XSpend.class);
        TermExpansion names = mock(TermExpansion.class);
        when(names.region("YE")).thenReturn(List.of("Yemen"));
        when(names.region(anyString())).thenReturn(List.of("Yemen"));
        CueRequests requests = new CueRequests();
        SocialCueService service = new SocialCueService(store, settings, provider(platforms), provider(spend), requests,
                provider((FetchExecutor) null), names, mock(XaiXSearchBackend.class), mock(SocialEvents.class),
                provider(null), provider(null), mock(JdbcTemplate.class), Clock.fixed(NOW, ZoneOffset.UTC), false);
        return new Harness(service, store, platforms, spend, requests);
    }

    static SocialCueService.Target target(Double maxCost, List<String> platforms) {
        return new SocialCueService.Target(null, "YE", null, null, null, null, null, List.of("conflict"),
                List.of("port"), platforms, NOW.minus(Duration.ofHours(6)), NOW.plus(Duration.ofHours(24)), maxCost);
    }

    @Test
    @DisplayName("queries: place names, aliases in other scripts, facilities and lexicon terms per language and platform")
    void queryBuilding() {
        assertThat(CueQueryBuilder.languages("YE-AD")).containsExactly("en", "ar");
        assertThat(CueQueryBuilder.languages("ZZ")).containsExactly("en");
        assertThat(CueQueryBuilder.aliases("Aden")).contains("عدن");
        List<String> names = new java.util.ArrayList<>(List.of("Aden"));
        names.addAll(CueQueryBuilder.aliases("Aden"));
        List<CueQueryBuilder.Query> queries = CueQueryBuilder.build(new CueQueryBuilder.Spec(names,
                List.of("Port of Aden"), List.of(), List.of("conflict"), List.of("tanker"), List.of("en", "ar")),
                List.of("bluesky-search", "x", "threads"));
        assertThat(queries).extracting(CueQueryBuilder.Query::language).contains("en", "ar");
        CueQueryBuilder.Query x = queries.stream().filter(q -> q.platform().equals("x") && "ar".equals(q.language()))
                .findFirst().orElseThrow();
        assertThat(x.query()).contains("\"Aden\"").contains("\"عدن\"").contains("\"Port of Aden\"").contains("lang:ar")
                .contains("-is:retweet");
        CueQueryBuilder.Query bluesky = queries.stream().filter(q -> q.platform().equals("bluesky-search")
                && "en".equals(q.language())).findFirst().orElseThrow();
        assertThat(bluesky.query()).contains(" AND ").contains("\"tanker\"").contains("\"explosion\"");
        assertThat(queries.stream().filter(q -> q.platform().equals("threads")).map(CueQueryBuilder.Query::query))
                .allMatch(q -> !q.contains(" OR "));
        assertThat(CueQueryBuilder.build(new CueQueryBuilder.Spec(List.of(), List.of(), List.of(), List.of("conflict"),
                List.of(), List.of("en")), List.of("x"))).isEmpty();
    }

    @Test
    @DisplayName("the family terms come from the claim lexicon in each requested language")
    void familyTerms() {
        Map<String, List<String>> terms = CueQueryBuilder.familyTerms(List.of("outage"), List.of("en", "ru"));
        assertThat(terms.get("en")).contains("power outage");
        assertThat(terms.get("ru")).isNotEmpty();
        assertThat(CueQueryBuilder.familyTerms(List.of("outage"), List.of("xx"))).isEmpty();
    }

    @Test
    @DisplayName("daily cap: 429 CUE_LIMIT_REACHED with Retry-After until midnight; automatic cues are skipped")
    void dailyCap() {
        Harness h = harness(cues(50, 1.0, false), 50, List.of("bluesky-search"));
        assertThatThrownBy(() -> h.service().createManual(target(null, List.of()), "agent"))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.CUE_LIMIT_REACHED);
                    assertThat(e.getHeaders()).containsEntry("Retry-After", Long.toString(12 * 3600));
                });
        assertThat(h.service().createAutomatic("finding", "finding:F", target(null, List.of()))).isNull();
        verify(h.store(), never()).insertCue(any());
    }

    @Test
    @DisplayName("a free platform is queued at no cost; no enabled platform is 409 PLATFORM_DISABLED")
    void platforms() {
        Harness h = harness(cues(50, 1.0, false), 0, List.of("bluesky-search"));
        Map<String, Object> cue = h.service().createManual(target(null, List.of()), "agent");
        assertThat(cue.get("status")).isEqualTo(CorrelationVocabulary.CUE_QUEUED);
        assertThat(cue.get("platforms")).isEqualTo(List.of("bluesky-search"));
        assertThat((List<?>) cue.get("queries")).isNotEmpty();
        verify(h.store()).insertCue(any());
        Harness none = harness(cues(50, 1.0, false), 0, List.of());
        assertThatThrownBy(() -> none.service().createManual(target(null, List.of()), "agent"))
                .isInstanceOfSatisfying(ForesignalException.class, e -> assertThat(e.getCode())
                        .isEqualTo(ErrorCodes.PLATFORM_DISABLED));
    }

    @Test
    @DisplayName("per-cue cap: X queries that do not fit the cap are refused before any call (429 BUDGET_EXCEEDED)")
    void perCueCap() {
        Harness h = harness(cues(50, 1.0, false), 0, List.of("x"));
        assertThatThrownBy(() -> h.service().createManual(target(0.01, List.of("x")), "agent"))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.BUDGET_EXCEEDED);
                });
        assertThat(h.service().estimate("x", false, 50)).isEqualByComparingTo(new BigDecimal("0.25"));
        assertThat(h.service().estimate("bluesky-search", false, 50)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("X spend: the monthly cap's refusal is returned as is when X is the only platform (SOC-4)")
    void xSpendRefusal() {
        Harness h = harness(cues(50, 1.0, false), 0, List.of("x"));
        BudgetExceededProblem problem = new BudgetExceededProblem("x: the monthly cap of $25 would be exceeded");
        when(h.spend().refusal(eq("x"), any(), any())).thenReturn(problem);
        assertThatThrownBy(() -> h.service().createManual(target(1.0, List.of("x")), "agent")).isSameAs(problem);
        Harness auto = harness(cues(50, 1.0, false), 0, List.of("x"));
        when(auto.spend().refusal(eq("x"), any(), any())).thenReturn(problem);
        assertThat(auto.service().createAutomatic("finding", "finding:F", target(1.0, List.of("x")))).isNotNull();
        verify(auto.store()).insertCue(org.mockito.ArgumentMatchers.argThat(c -> CorrelationVocabulary
                .CUE_BUDGET_REFUSED.equals(c.status())));
    }

    @Test
    @DisplayName("a run hands each query to its adapter and records per-platform results")
    void run() {
        Harness h = harness(cues(50, 1.0, false), 0, List.of("bluesky-search"));
        ComparisonStore.Cue queued = new ComparisonStore.Cue("01JA8Q3V7S2K9W4T6Y1B5N0M3J", "manual", null,
                CorrelationVocabulary.BACKEND_PLATFORMS, List.of(Map.of("platform", "bluesky-search", "query",
                "\"Aden\"", "language", "en", "estimatedCostUsd", 0.0)), List.of("bluesky-search"),
                NOW.minusSeconds(3600), NOW, CorrelationVocabulary.CUE_QUEUED, null, List.of(), List.of(),
                BigDecimal.ZERO, BigDecimal.ONE, Map.of("maxResults", 50), "agent", NOW, null, null, null, List.of());
        when(h.store().cue(queued.id())).thenReturn(java.util.Optional.of(queued));
        h.service().run(queued.id());
        verify(h.store(), org.mockito.Mockito.atLeastOnce()).updateCue(org.mockito.ArgumentMatchers.argThat(c ->
                CorrelationVocabulary.CUE_FAILED.equals(c.status()) && c.results().size() == 1));
        assertThat(h.requests().waiting("bluesky-search")).isTrue();
    }

    @Test
    @DisplayName("cue posts are counted as sampled and add no region coverage, so they never feed baselines (SOC-3)")
    void cueResultsNeverFeedBaselines() {
        Map<String, Object> block = SocialTestSupport.block(List.of("explosion"), List.of());
        TermIndex index = new TermIndex(List.of(new TermIndex.TopicEntry("red-sea", SocialTopicBlock.parse(block, null),
                Map.of(), Map.of("YE", List.of("yemen")), Map.of())));
        SocialCollector collector = new SocialCollector(SocialTestSupport.terms(index),
                new SocialLanguages(List.of("en")), new AuthorKeys(SocialTestSupport.SALT), null,
                Clock.fixed(NOW, ZoneOffset.UTC));
        collector.observe(post("p1", SocialVocabulary.COLLECTION_STREAM, null), "bluesky-jetstream",
                SocialVocabulary.COMPLETE);
        SocialCollector.Observation cue = collector.observe(post("p2", SocialVocabulary.COLLECTION_CUE, "C1"),
                "bluesky-search", SocialVocabulary.SAMPLED);
        assertThat(cue.record()).isNotNull();
        VolumeAccumulator.Drain drain = collector.accumulator().drain(NOW.plusSeconds(60));
        assertThat(drain.coverage().values().stream().mapToLong(Long::longValue).sum()).isEqualTo(1);
        assertThat(drain.volumes()).anyMatch(v -> SocialVocabulary.SAMPLED.equals(v.key().collectionType()));
        assertThat(SocialBaseline.eligible(SocialVocabulary.SAMPLED)).isFalse();
    }

    @Test
    @DisplayName("a cue loop: a match with only the trigger is caveated and cue-found claims open no ledger question")
    void cueLoop() {
        Instant t0 = NOW.minus(Duration.ofDays(3));
        SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(t0,
                t0.plus(Duration.ofHours(24)), t0.plus(Duration.ofHours(48)), NOW, List.of(
                new PhysicalEvidenceSource.Evidence("canonical-event:T", "conflict", "acled", "aggregator", t0, 1.0,
                        false)), ai.foresignal.testsupport.social.CorrelationTestSupport.covered(), null, t0, null,
                "canonical-event:T"));
        assertThat(d.caveats()).anyMatch(c -> CorrelationVocabulary.CUE_TRIGGER_MATCH.equals(c.get("code")));
        ForecastService ledger = mock(ForecastService.class);
        ComparisonQuestions questions = new ComparisonQuestions(provider(ledger), provider(mock(ForecasterRegistry.class)),
                mock(ComparisonStore.class));
        ComparisonStore.Comparison cueFound = new ComparisonStore.Comparison("X", "C", "N", "explosion", "conflict",
                Map.of("name", "Aden"), null, null, "YE", t0, t0.plus(Duration.ofHours(24)), "pending", List.of(),
                Map.of(), Map.of(), null, List.of(), CorrelationVocabulary.CUE, "CUE", "canonical-event:T",
                List.of("bluesky"), 1, null, 1, t0.plus(Duration.ofHours(48)), null, NOW, NOW);
        assertThat(questions.open(cueFound, NOW)).isNull();
        verify(ledger, never()).openQuestion(any(), any(), any());
    }

    static SocialPost post(String id, String collection, String cueId) {
        return new SocialPost("bluesky", "at://did:plc:a/app.bsky.feed.post/" + id, null,
                new SocialPost.Author("alice.example", null, "did:plc:a", null, null, null),
                "Explosion reported near the port in Yemen", "en", NOW, null, null, null, List.of(), List.of(),
                List.of(), List.of(), null, null, null, collection, cueId);
    }
}
