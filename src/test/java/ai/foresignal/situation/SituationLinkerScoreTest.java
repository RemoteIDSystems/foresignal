package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * The situation linker's score (prompt 118 §2): every component and constant of {@link LinkerWeights}, theme
 * incompatibility, the recency decay, the prefilter and the threshold boundary at exactly 0.45.
 */
class SituationLinkerScoreTest {

    static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");
    static final LinkerWeights W = LinkerWeights.DEFAULT;

    static LinkerInput input(Set<String> regions, Geometry geom, Set<String> entities, String text, String theme,
            Instant at) {
        return new LinkerInput(10, SituationMemberTypes.INTEL_EVENT, "e1", "foresignal-intel", "e1", 1,
                "intelligence-event", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, "warning", "Event", "Maritime disruption", at, geom, regions,
                "Yemen", entities, text, theme, theme == null ? null : "table", true, "core", List.of(), null);
    }

    static SituationScorer.Candidate candidate(Set<String> regions, Geometry geom, Set<String> entities,
            List<String> keywords, String theme, Instant lastActivity) {
        return new SituationScorer.Candidate("S1", theme, SituationLifecycle.ACTIVE, regions, geom, entities, keywords,
                lastActivity);
    }

    @Test
    @DisplayName("the constants are the documented ones")
    void constants() {
        assertThat(W.sharedAdmin1()).isEqualTo(0.30);
        assertThat(W.sharedCountry()).isEqualTo(0.20);
        assertThat(W.distance()).isEqualTo(0.25);
        assertThat(W.maxDistanceKm()).isEqualTo(300);
        assertThat(W.maritimeMaxDistanceKm()).isEqualTo(500);
        assertThat(W.entity()).isEqualTo(0.25);
        assertThat(W.topic()).isEqualTo(0.15);
        assertThat(W.textSimilarity()).isEqualTo(0.20);
        assertThat(W.cosFloor()).isEqualTo(0.70);
        assertThat(W.themePenalty()).isEqualTo(-0.20);
        assertThat(W.recencyHalfLifeHours()).isEqualTo(72);
        assertThat(W.linkThreshold()).isEqualTo(0.45);
        assertThat(W.toMap()).containsKeys("sharedAdmin1", "linkThreshold");
        assertThat(SituationScorer.VERSION).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("region: 0.30 for a shared admin1, 0.20 for a shared country only, 0 otherwise")
    void region() {
        assertThat(SituationScorer.region(Set.of("YE", "YE-AD"), Set.of("YE", "YE-AD"), W)).isEqualTo(0.30);
        assertThat(SituationScorer.region(Set.of("YE", "YE-AD"), Set.of("YE", "YE-HD"), W)).isEqualTo(0.20);
        assertThat(SituationScorer.region(Set.of("YE-AD"), Set.of("YE"), W)).isEqualTo(0.20);
        assertThat(SituationScorer.region(Set.of("ER"), Set.of("YE"), W)).isZero();
        assertThat(SituationScorer.region(Set.of("ZZ"), Set.of("ZZ"), W)).isZero();
    }

    @Test
    @DisplayName("distance: 0.25 × max(0, 1 − d / 300) and 500 km when maritime")
    void distance() {
        Geometry point = Geo.point(45.0, 12.0);
        // About 150 km east of the point at that latitude (1° of longitude ≈ 108.9 km).
        Geometry far = Geo.point(45.0 + 150 / (111.32 * Math.cos(Math.toRadians(12.0))), 12.0);
        SituationScorer.Score land = SituationScorer.score(input(Set.of(), point, Set.of(), "", "conflict", T0),
                candidate(Set.of(), far, Set.of(), List.of(), "conflict", T0), null, W);
        double d = (Double) land.components().get("distanceKm");
        assertThat(d).isCloseTo(150, within(1.0));
        assertThat(land.score()).isCloseTo(0.25 * (1 - d / 300), within(1e-6));
        SituationScorer.Score sea = SituationScorer.score(input(Set.of(), point, Set.of(), "", "maritime", T0),
                candidate(Set.of(), far, Set.of(), List.of(), "maritime", T0), null, W);
        assertThat(sea.score()).isCloseTo(0.25 * (1 - d / 500), within(1e-6));
        Geometry beyond = Geo.point(45.0 + 400 / (111.32 * Math.cos(Math.toRadians(12.0))), 12.0);
        assertThat(SituationScorer.score(input(Set.of(), point, Set.of(), "", "conflict", T0),
                candidate(Set.of(), beyond, Set.of(), List.of(), "conflict", T0), null, W).score()).isZero();
        assertThat(SituationScorer.score(input(Set.of(), point, Set.of(), "", "conflict", T0),
                candidate(Set.of(), point, Set.of(), List.of(), "conflict", T0), null, W).score()).isEqualTo(0.25);
    }

    @Test
    @DisplayName("entity 0.25, topic keyword 0.15 (whole words, ignoring case) and text similarity")
    void entityTopicText() {
        SituationScorer.Candidate c = candidate(Set.of(), null, Set.of("ENT1"), List.of("Bab el-Mandeb"), "maritime",
                T0);
        assertThat(SituationScorer.score(input(Set.of(), null, Set.of("ENT1"), "", "maritime", T0), c, null, W)
                .score()).isEqualTo(0.25);
        assertThat(SituationScorer.score(input(Set.of(), null, Set.of(), "Tanker hit near BAB EL-MANDEB", "maritime",
                T0), c, null, W).score()).isEqualTo(0.15);
        assertThat(SituationScorer.topicMatch("Mandebx", List.of("Mandeb"))).isFalse();
        assertThat(SituationScorer.textTerm(0.70, W)).isZero();
        assertThat(SituationScorer.textTerm(0.85, W)).isCloseTo(0.10, within(1e-9));
        assertThat(SituationScorer.textTerm(1.0, W)).isCloseTo(0.20, within(1e-9));
        SituationScorer.Score withText = SituationScorer.score(input(Set.of(), null, Set.of(), "", "maritime", T0),
                c, 0.85, W);
        assertThat(withText.score()).isCloseTo(0.10, within(1e-9));
        assertThat(withText.components().get("textSimilarity")).isNotNull();
        assertThat(SituationScorer.score(input(Set.of(), null, Set.of(), "", "maritime", T0), c, null, W)
                .components().get("textSimilarity")).isNull();
    }

    @Test
    @DisplayName("an incompatible theme costs 0.20; mixed and unknown themes are compatible with all")
    void theme() {
        SituationScorer.Candidate maritime = candidate(Set.of("YE", "YE-AD"), null, Set.of(), List.of(), "maritime",
                T0);
        assertThat(SituationScorer.score(input(Set.of("YE", "YE-AD"), null, Set.of(), "", "cyber", T0), maritime,
                null, W).score()).isCloseTo(0.10, within(1e-9));
        assertThat(SituationScorer.score(input(Set.of("YE", "YE-AD"), null, Set.of(), "", "mixed", T0), maritime,
                null, W).score()).isEqualTo(0.30);
        assertThat(SituationScorer.score(input(Set.of("YE", "YE-AD"), null, Set.of(), "", null, T0), maritime,
                null, W).score()).isEqualTo(0.30);
        assertThat(SituationThemes.compatible("cyber", "mixed")).isTrue();
        assertThat(SituationThemes.compatible("cyber", "maritime")).isFalse();
        // The raw sum never goes below zero.
        assertThat(SituationScorer.score(input(Set.of(), null, Set.of(), "", "cyber", T0),
                candidate(Set.of(), null, Set.of(), List.of(), "maritime", T0), null, W).score()).isZero();
    }

    @Test
    @DisplayName("recency multiplies the raw sum by 0.5 + 0.5·exp(−h / 72)")
    void recency() {
        assertThat(SituationScorer.recency(0, W)).isEqualTo(1.0);
        assertThat(SituationScorer.recency(72, W)).isCloseTo(0.5 + 0.5 * Math.exp(-1), within(1e-12));
        assertThat(SituationScorer.recency(1e6, W)).isCloseTo(0.5, within(1e-9));
        SituationScorer.Score aged = SituationScorer.score(
                input(Set.of("YE", "YE-AD"), null, Set.of(), "", "maritime", T0.plus(Duration.ofHours(72))),
                candidate(Set.of("YE", "YE-AD"), null, Set.of(), List.of(), "maritime", T0), null, W);
        assertThat(aged.score()).isCloseTo(0.30 * (0.5 + 0.5 * Math.exp(-1)), within(1e-9));
        assertThat((Double) aged.components().get("recencyHours")).isEqualTo(72.0);
        // An input observed before the situation's last activity is not penalized.
        assertThat(SituationScorer.recencyHours(T0, T0.plusSeconds(3600))).isZero();
    }

    @Test
    @DisplayName("the threshold boundary: exactly 0.45 (0.30 + 0.15) reaches the threshold")
    void thresholdBoundary() {
        SituationScorer.Score score = SituationScorer.score(
                input(Set.of("YE", "YE-AD"), null, Set.of(), "Red Sea shipping", "maritime", T0),
                candidate(Set.of("YE", "YE-AD"), null, Set.of(), List.of("Red Sea"), "maritime", T0), null, W);
        assertThat(score.score()).isEqualTo(0.45);
        assertThat(score.score() >= W.linkThreshold()).isTrue();
        SituationScorer.Score below = SituationScorer.score(
                input(Set.of("YE"), null, Set.of(), "Red Sea shipping", "maritime", T0),
                candidate(Set.of("YE"), null, Set.of(), List.of("Red Sea"), "maritime", T0.minusSeconds(60)), null, W);
        assertThat(below.score()).isLessThan(W.linkThreshold());
    }

    @Test
    @DisplayName("the prefilter passes a shared region, a near geometry, a shared entity or a topic match only")
    void prefilter() {
        Geometry point = Geo.point(45.0, 12.0);
        Geometry far = Geo.point(100.0, 12.0);
        assertThat(SituationScorer.prefilter(input(Set.of("YE"), null, Set.of(), "", null, T0),
                candidate(Set.of("YE"), null, Set.of(), List.of(), "mixed", T0), W)).isTrue();
        assertThat(SituationScorer.prefilter(input(Set.of(), point, Set.of(), "", null, T0),
                candidate(Set.of(), point, Set.of(), List.of(), "mixed", T0), W)).isTrue();
        assertThat(SituationScorer.prefilter(input(Set.of(), point, Set.of("E"), "", null, T0),
                candidate(Set.of(), far, Set.of("E"), List.of(), "mixed", T0), W)).isTrue();
        assertThat(SituationScorer.prefilter(input(Set.of(), null, Set.of(), "Houthi attack", null, T0),
                candidate(Set.of(), null, Set.of(), List.of("houthi"), "mixed", T0), W)).isTrue();
        assertThat(SituationScorer.prefilter(input(Set.of("ER"), point, Set.of(), "", null, T0),
                candidate(Set.of("US"), far, Set.of(), List.of(), "mixed", T0), W)).isFalse();
    }

    @Test
    @DisplayName("the seeded title is '{type} — {region}', falling back to a region code or 'unlocated'")
    void seedTitle() {
        assertThat(SituationLinker.seedTitle(input(Set.of("YE"), null, Set.of(), "", "maritime", T0)))
                .isEqualTo("Maritime disruption — Yemen");
        LinkerInput unnamed = new LinkerInput(1, "alert", "7", "s", "x", null, "k", "f", "critical", "t", "Watch", T0,
                null, Set.of(), null, Set.of(), "", null, null, true, "core", List.of(), null);
        assertThat(SituationLinker.seedTitle(unnamed)).isEqualTo("Watch — unlocated");
    }
}
