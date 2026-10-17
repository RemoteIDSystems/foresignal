package ai.foresignal.impact.rate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.baserate.BaseRateCalculator;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.impact.ImpactPairs;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link ImpactRateService} over seeded co-occurrence counts: the rate and its Jeffreys interval, the n &lt; 20 skip,
 * the region class of an admin1 region, caching, the history window, and the SQL the selectors render to.
 */
class ImpactRateServiceTest {

    static final Instant NOW = Instant.parse("2026-10-17T00:00:00Z");

    /** A counter that answers seeded counts and records its calls. */
    static final class SeededCounter implements CoOccurrenceCounter {
        long n;
        long impacts;
        final AtomicInteger calls = new AtomicInteger();
        final List<Object[]> seen = new ArrayList<>();

        SeededCounter(long n, long impacts) {
            this.n = n;
            this.impacts = impacts;
        }

        @Override
        public Counts count(ImpactPairs.HazardClass hazard, ImpactPairs.ImpactFamily family, int lagHours,
                Collection<String> countries, Instant from, Instant to) {
            calls.incrementAndGet();
            seen.add(new Object[] {hazard.id(), family.id(), lagHours, List.copyOf(countries), from, to});
            return new Counts(n, impacts);
        }
    }

    static RegionClasses regionClasses() {
        RegionClasses rc = mock(RegionClasses.class);
        when(rc.members("continent:NA")).thenReturn(List.of("CA", "MX", "US"));
        when(rc.country("US-TX")).thenReturn(new RegionClasses.Country("US", "NA", 330_000_000L, 70_000.0, "high"));
        when(rc.country("ZZ-XX")).thenReturn(null);
        return rc;
    }

    static ImpactRateService service(SeededCounter counter) {
        return new ImpactRateService(new ImpactPairs(), counter, regionClasses(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("seeded history: 34 tropical-storm-wind days, 19 followed by an outage → 19/34 with a Jeffreys interval")
    void rateWithInterval() {
        SeededCounter counter = new SeededCounter(34, 19);
        ImpactRateService.ImpactRate r = service(counter).rate("tropical-wind-34", "outage", "continent:NA");
        assertThat(r.status()).isEqualTo(ImpactRateService.OK);
        assertThat(r.rate()).isEqualTo(19.0 / 34);
        double[] expected = BaseRateCalculator.jeffreys(19, 34);
        assertThat(r.lower()).isEqualTo(expected[0]);
        assertThat(r.upper()).isEqualTo(expected[1]);
        assertThat(r.lower()).isLessThan(r.rate());
        assertThat(r.upper()).isGreaterThan(r.rate());
        assertThat(r.n()).isEqualTo(34);
        assertThat(r.impacts()).isEqualTo(19);
        assertThat(r.lagHours()).isEqualTo(48);
        assertThat(r.source()).isEqualTo("own-history");
        assertThat(r.view()).containsKeys("hazardClass", "impactFamily", "regionClass", "rate", "interval", "n",
                "nImpacts", "status", "computedAt");
        Object[] call = counter.seen.get(0);
        assertThat(call[3]).isEqualTo(List.of("CA", "MX", "US"));
        assertThat(call[4]).isEqualTo(NOW.minus(Duration.ofDays(ImpactRateService.LOOKBACK_DAYS)));
        // History ends one lag before now, so every counted occurrence had its whole lag.
        assertThat(call[5]).isEqualTo(NOW.minus(Duration.ofHours(48)));
    }

    @Test
    @DisplayName("n < 20: insufficient, no rate and no interval, so the projector skips the pair")
    void insufficient() {
        ImpactRateService.ImpactRate r = service(new SeededCounter(19, 15)).rate("tornado", "outage", "continent:NA");
        assertThat(r.status()).isEqualTo(ImpactRateService.INSUFFICIENT);
        assertThat(r.usable()).isFalse();
        assertThat(r.rate()).isNull();
        assertThat(r.lower()).isNull();
        assertThat(r.view().get("interval")).isNull();
        assertThat(r.n()).isEqualTo(19);
        ImpactRateService.ImpactRate twenty = service(new SeededCounter(20, 0)).rate("tornado", "outage",
                "continent:NA");
        assertThat(twenty.usable()).isTrue();
        assertThat(twenty.rate()).isZero();
        assertThat(twenty.lower()).isZero();
    }

    @Test
    @DisplayName("no built-in pair or an invalid region class: null")
    void unknownPair() {
        ImpactRateService s = service(new SeededCounter(50, 10));
        assertThat(s.rate("hail", "outage", "continent:NA")).isNull();
        assertThat(s.rate("tornado", "outage", "admin1:coastal")).isNull();
    }

    @Test
    @DisplayName("region class of an admin1 region: its country's continent; unknown → null")
    void regionClass() {
        ImpactRateService s = service(new SeededCounter(0, 0));
        assertThat(s.regionClassOf("US-TX")).isEqualTo("continent:NA");
        assertThat(s.regionClassOf("ZZ-XX")).isNull();
    }

    @Test
    @DisplayName("rates are cached for a day; list covers every continent class of the matching pairs")
    void cacheAndList() {
        SeededCounter counter = new SeededCounter(40, 8);
        ImpactRateService s = service(counter);
        s.rate("tornado", "outage", "continent:NA");
        s.rate("tornado", "outage", "continent:NA");
        assertThat(counter.calls.get()).isEqualTo(1);
        s.clearCache();
        s.rate("tornado", "outage", "continent:NA");
        assertThat(counter.calls.get()).isEqualTo(2);
        List<ImpactRateService.ImpactRate> list = s.list("tornado", null, null);
        assertThat(list).hasSize(2 * RegionClasses.CONTINENTS.size());
        assertThat(list).extracting(ImpactRateService.ImpactRate::impactFamily).containsOnly("tornado-warning",
                "outage");
    }

    @Test
    @DisplayName("SQL: selectors become bound predicates; == compares text, in a list, >= a number")
    void selectorSql() {
        ImpactPairs pairs = new ImpactPairs();
        MapSqlParameterSource p = new MapSqlParameterSource();
        String sql = JdbcCoOccurrenceCounter.sql(pairs.hazardClass("tropical-wind-34"), pairs.family("outage"), p);
        assertThat(sql).contains("v.source_id IN (:h_sources)", "v.payload ->> 'thresholdKt' = :h_c0",
                "jsonb_typeof(v.payload -> 'pMin') = 'number'", ">= :h_c1", "i.payload ->> 'entityType' = :f_c0",
                "make_interval(hours => :lag)", "ST_Intersects(a.geom, v.geom)");
        assertThat(p.getValue("h_c0")).isEqualTo("34");
        assertThat(p.getValue("h_c1")).isEqualTo(0.5);
        assertThat(p.getValue("f_c0")).isEqualTo("region");
        assertThat(p.getValue("h_sources")).isEqualTo(List.of("nhc-wind-probs"));
        MapSqlParameterSource q = new MapSqlParameterSource();
        String ero = SelectorSql.where("v", pairs.hazardClass("excessive-rain").selector(), "x", q);
        assertThat(ero).contains("v.payload ->> 'category' IN (:x_c0)");
        assertThat(q.getValue("x_c0")).isEqualTo(List.of("MDT", "HIGH"));
    }

    @Test
    @DisplayName("the pairs file: every pair resolves; an unknown family or operator stops loading")
    void pairsFile() {
        ImpactPairs pairs = new ImpactPairs();
        assertThat(pairs.pairs()).isNotEmpty().allSatisfy(p -> {
            assertThat(pairs.hazardClass(p.hazardClass())).isNotNull();
            assertThat(pairs.family(p.impactFamily())).isNotNull();
        });
        assertThat(pairs.pairsFor("tropical-wind-34")).extracting(ImpactPairs.Pair::impactFamily)
                .containsExactly("outage", "ground-stop");
        assertThat(pairs.pairsFor("tropical-track")).isEmpty();
        assertThat(pairs.pairsFor("severe-categorical")).isEmpty();
        JsonMapper json = JsonMapper.builder().build();
        assertThatThrownBy(() -> new ImpactPairs(json.readTree("{\"hazardClasses\":{},\"impactFamilies\":{},"
                + "\"pairs\":[{\"hazardClass\":\"x\",\"impactFamily\":\"y\",\"lagHours\":5}]}")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new ImpactPairs(json.readTree("{\"hazardClasses\":{\"a\":{\"sources\":[\"s\"],"
                + "\"kinds\":[\"k\"],\"conditions\":[{\"path\":\"x\",\"op\":\"<\",\"value\":1}]}}}")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new ImpactPairs(json.readTree("{\"hazardClasses\":{\"a\":{\"sources\":[\"s\"],"
                + "\"kinds\":[\"k\"],\"conditions\":[{\"path\":\"x'; DROP\",\"op\":\"==\",\"value\":1}]}}}")))
                .isInstanceOf(IllegalStateException.class);
    }
}
