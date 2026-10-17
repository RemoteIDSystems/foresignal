package ai.foresignal.baserate.analog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationMemberEntity;
import ai.foresignal.situation.SituationMemberRepository;
import ai.foresignal.situation.SituationRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Signatures and analogs end to end (prompt 121): a signature on activation and one per day; analog search that
 * excludes situations overlapping the target in time and the target's merges; ranking by similarity; the outcome
 * distribution shrunk toward the base rate; the feature-only fallback without embeddings; and reciprocal-rank fusion
 * when embeddings exist.
 */
@IntegrationTest
class SignatureAndAnalogIT {

    static final Instant T0 = Instant.parse("2026-06-01T12:00:00Z");

    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired SituationRepository situations;
    @Autowired SituationMemberRepository members;
    @Autowired SignatureService signatures;
    @Autowired SituationSignatureRepository signatureRows;
    @Autowired AnalogService analogs;
    @Autowired RegionClasses regionClasses;

    String target;
    String pastCritical;
    String pastSimilar;
    String pastDifferent;
    String overlapping;
    String merged;

    @BeforeEach
    void seed() {
        clock.setInstant(T0);
        signatures.clearCache();
        regionClasses.reset();
        pastCritical = situation("Clashes near Marib", T0.minus(Duration.ofDays(200)), T0.minus(Duration.ofDays(190)),
                "closed", "critical", null);
        members(pastCritical, T0.minus(Duration.ofDays(200)), "conflict", 4);
        pastSimilar = situation("Fighting in Taiz", T0.minus(Duration.ofDays(150)), T0.minus(Duration.ofDays(140)),
                "closed", "info", null);
        members(pastSimilar, T0.minus(Duration.ofDays(150)), "conflict", 3);
        members(pastSimilar, T0.minus(Duration.ofDays(150)), "aviation-ops", 1);
        pastDifferent = situation("Launch campaign", T0.minus(Duration.ofDays(100)), T0.minus(Duration.ofDays(95)),
                "closed", "info", null);
        members(pastDifferent, T0.minus(Duration.ofDays(100)), "space", 5);
        target = situation("Escalation in Hodeidah", T0.minus(Duration.ofDays(2)), null, "active", "warning", null);
        members(target, T0.minus(Duration.ofDays(2)), "conflict", 3);
        members(target, T0.minus(Duration.ofDays(1)), "conflict", 2);
        jdbc.update("INSERT INTO situation_log (situation_id, at, actor, action, event) VALUES (?, ?, 'linker',"
                + " 'status', 'situation.activated')", target, Timestamp.from(T0.minus(Duration.ofDays(2))));
        overlapping = situation("Overlapping clashes", T0.minus(Duration.ofDays(3)), null, "active", "warning", null);
        members(overlapping, T0.minus(Duration.ofDays(3)), "conflict", 3);
        merged = situation("Old clashes merged into the target", T0.minus(Duration.ofDays(300)),
                T0.minus(Duration.ofDays(290)), "closed", "critical", target);
        members(merged, T0.minus(Duration.ofDays(300)), "conflict", 4);
    }

    String situation(String title, Instant started, Instant closed, String status, String severity,
            String mergedInto) {
        String id = Ulids.next(started);
        SituationEntity s = new SituationEntity(id, title, "conflict", started);
        s.setStatus(status);
        s.setSeverity(severity);
        s.setRegionCodes(List.of("YE"));
        s.setLastActivityAt(closed != null ? closed : T0.minus(Duration.ofHours(1)));
        s.setClosedAt(closed);
        s.setMergedInto(mergedInto);
        s.setSummary(title);
        s.setCreatedAt(started);
        s.setUpdatedAt(closed != null ? closed : T0);
        situations.save(s);
        return id;
    }

    void members(String situationId, Instant at, String family, int n) {
        for (int i = 0; i < n; i++) {
            Instant when = at.plus(Duration.ofHours(1 + i));
            SituationMemberEntity m = new SituationMemberEntity(Ulids.next(when), situationId, "record",
                    "test/" + situationId + "/" + family + "/" + i + "/" + at.getEpochSecond(), (long) i + 1);
            m.setSignalFamily(family);
            m.setObservedAt(when);
            m.setAddedAt(when);
            m.setAddedBy("test");
            m.setRegionCodes(List.of("YE"));
            members.save(m);
        }
    }

    @Test
    @DisplayName("an activated situation gets a signature on its current day, then one more each day")
    void signaturesOnActivationAndDaily() {
        signatures.computeDue();
        assertThat(signatureRows.existsById(new SituationSignatureEntity.Key(target, 2))).isTrue();
        SignatureBuilder.Signature s = signatureRows.findById(new SituationSignatureEntity.Key(target, 2))
                .orElseThrow().signature();
        assertThat(s.get("family:conflict")).isGreaterThan(0);
        assertThat(s.names()).contains("slope", "theme:conflict", "severity:max");

        clock.advance(Duration.ofDays(1));
        signatures.computeDue();
        assertThat(signatureRows.findBySituationIdOrderByDayNAsc(target)).extracting(SituationSignatureEntity::getDayN)
                .containsExactly(2, 3);
        // past situations are backfilled once and get outcomes; the merged one gets neither
        assertThat(signatureRows.findBySituationIdOrderByDayNAsc(pastCritical)).hasSize(1);
        assertThat(analogs.storedOutcome(pastCritical).orElseThrow().getPeakSeverity()).isEqualTo("critical");
        assertThat(signatureRows.findBySituationIdOrderByDayNAsc(merged)).isEmpty();
    }

    @Test
    @DisplayName("analogs exclude time overlap and merges, rank by similarity and fall back to features only")
    void analogsExcludeOverlap() {
        signatures.computeDue();
        Map<String, Object> result = analogs.forSituation(target, 10, null, false);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> found = (List<Map<String, Object>>) result.get("analogs");
        List<Object> ids = found.stream().map(a -> a.get("situationId")).toList();
        assertThat(ids).containsExactlyInAnyOrder(pastCritical, pastSimilar, pastDifferent);
        assertThat(ids).doesNotContain(overlapping, merged, target);
        assertThat(ids.get(ids.size() - 1)).isEqualTo(pastDifferent);
        assertThat(result.get("countFound")).isEqualTo(3);
        assertThat(result.get("method")).isEqualTo("feature-only");
        assertThat(result.get("methodNote")).asString().contains("embeddings unavailable");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> caveats = (List<Map<String, Object>>) result.get("caveats");
        assertThat(caveats.stream().map(c -> c.get("code")).toList()).contains("FEATURE_ONLY");
        assertThat(found.get(0).get("embeddingSimilarity")).isNull();
        assertThat(found.get(0).get("outcomes")).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("the outcome distribution is the analog frequency shrunk toward the base rate by k / (k + 10)")
    void outcomeDistribution() {
        signatures.computeDue();
        Map<String, Object> result = analogs.forSituation(target, 2, "critical", false);
        @SuppressWarnings("unchecked")
        Map<String, Object> d = (Map<String, Object>) result.get("outcomeDistribution");
        assertThat(d.get("analogCount")).isEqualTo(2);
        assertThat(d.get("hits")).isEqualTo(1);
        assertThat((Double) d.get("frequency")).isEqualTo(0.5);
        // base rate of critical among past situations with outcomes: 1 of 3
        assertThat((Double) d.get("baseRate")).isCloseTo(1.0 / 3, offset(1e-9));
        double w = 2 / 12.0;
        assertThat((Double) d.get("probability")).isCloseTo(w * 0.5 + (1 - w) / 3, offset(1e-9));
        assertThat(((Map<?, ?>) d.get("interval")).get("method")).isEqualTo("jeffreys");

        AnalogSearch.Distribution none = AnalogSearch.distribution(0, 0, 0.2);
        assertThat(none.probability()).isEqualTo(0.2);
        AnalogSearch.Distribution many = AnalogSearch.distribution(90, 90, 0.1);
        assertThat(many.probability()).isCloseTo(0.9 * 1 + 0.1 * 0.1, offset(1e-9));
    }

    @Test
    @DisplayName("fusion: reciprocal-rank fusion of feature and embedding rankings, never across time overlap")
    void fusionRanking() {
        SignatureBuilder.Signature t = new SignatureBuilder.Signature(List.of("family:conflict", "slope"),
                new double[] {3, 0.1});
        Instant past = T0.minus(Duration.ofDays(400));
        List<AnalogSearch.Candidate> candidates = List.of(
                new AnalogSearch.Candidate("a", new SignatureBuilder.Signature(List.of("family:conflict", "slope"),
                        new double[] {3, 0.1}), past, past.plus(Duration.ofDays(5)), "a"),
                new AnalogSearch.Candidate("b", new SignatureBuilder.Signature(List.of("family:conflict", "slope"),
                        new double[] {3, 1.5}), past, past.plus(Duration.ofDays(5)), "b"),
                new AnalogSearch.Candidate("c", new SignatureBuilder.Signature(List.of("family:space"),
                        new double[] {2}), past, past.plus(Duration.ofDays(5)), "c"),
                new AnalogSearch.Candidate("overlap", new SignatureBuilder.Signature(List.of("family:conflict",
                        "slope"), new double[] {3, 0.1}), T0.minus(Duration.ofDays(1)), null, "overlap"));
        AnalogSearch.Result featureOnly = AnalogSearch.search(t, T0, T0, "t", candidates, null, 10);
        assertThat(featureOnly.fused()).isFalse();
        assertThat(featureOnly.analogs()).extracting(AnalogSearch.Ranked::id).containsExactly("a", "b", "c");
        // Feature ranks a, b, c; embedding ranks b, c, a (the overlapping candidate is never ranked).
        AnalogSearch.Result fused = AnalogSearch.search(t, T0, T0, "t", candidates, Map.of("b", 0.99, "c", 0.95,
                "a", 0.10, "overlap", 1.0), 10);
        assertThat(fused.fused()).isTrue();
        assertThat(fused.countFound()).isEqualTo(3);
        assertThat(fused.analogs()).extracting(AnalogSearch.Ranked::id).doesNotContain("overlap");
        double a = 1.0 / 61 + 1.0 / 63;
        double b = 1.0 / 62 + 1.0 / 61;
        double c = 1.0 / 63 + 1.0 / 62;
        assertThat(fused.analogs().get(0).id()).isEqualTo("b");
        assertThat(fused.analogs().get(0).similarity()).isCloseTo(b, offset(1e-12));
        assertThat(fused.analogs()).extracting(AnalogSearch.Ranked::id).containsExactly("b", "a", "c");
        assertThat(fused.analogs().get(1).similarity()).isCloseTo(a, offset(1e-12));
        assertThat(fused.analogs().get(2).similarity()).isCloseTo(c, offset(1e-12));
        assertThat(fused.analogs().get(0).embeddingSimilarity()).isEqualTo(0.99);
        // the target's own merge group is excluded
        AnalogSearch.Result group = AnalogSearch.search(t, T0, T0, "a", candidates, null, 10);
        assertThat(group.analogs()).extracting(AnalogSearch.Ranked::id).doesNotContain("a");
    }

    @Test
    @DisplayName("signatures are deterministic; the escalation slope is the log-linear fit")
    void signatureMath() {
        assertThat(SignatureBuilder.slope(new long[] {0, 0, 0})).isEqualTo(0);
        assertThat(SignatureBuilder.slope(new long[] {1})).isEqualTo(0);
        double rising = SignatureBuilder.slope(new long[] {0, 1, 3, 7});
        assertThat(rising).isCloseTo((Math.log(8) - Math.log(1)) / 3 * 1.0, offset(0.2));
        SignatureBuilder.Inputs in = new SignatureBuilder.Inputs(Map.of("conflict", 6L), Map.of("conflict", 2.0),
                new long[] {2, 4}, "conflict", "AS", "lower-middle", Map.of("vessel", 2L), List.of("info", "warning"));
        SignatureBuilder.Signature a = SignatureBuilder.build(in);
        assertThat(a.get("family:conflict")).isEqualTo(3.0);
        assertThat(a.get("continent:AS")).isEqualTo(1.0);
        assertThat(a.get("entity:vessel")).isCloseTo(Math.log1p(2), offset(1e-12));
        assertThat(a.get("severity:elevated")).isEqualTo(0.5);
        assertThat(SignatureBuilder.build(in).asMap()).isEqualTo(a.asMap());
        assertThat(SignatureBuilder.cosine(a, a)).isCloseTo(1.0, offset(1e-12));
    }
}
