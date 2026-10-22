package ai.foresignal.social.correlation;

import static ai.foresignal.testsupport.social.CorrelationTestSupport.covered;
import static ai.foresignal.testsupport.social.CorrelationTestSupport.negating;
import static ai.foresignal.testsupport.social.CorrelationTestSupport.physical;
import static ai.foresignal.testsupport.social.CorrelationTestSupport.social;
import static ai.foresignal.testsupport.social.CorrelationTestSupport.thin;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SocialPhysicalComparator} 1.0.0 (prompt 137): corroborated with lead times; unobserved only when
 * {@code canSeeAbsence} is ok; insufficient-coverage otherwise; contradicted by a normal-service record and by a reuse
 * finding; late evidence revises a comparison; social-only evidence never corroborates (SOC-1, SOC-2).
 */
class SocialPhysicalComparatorTest {

    static final Instant T0 = Instant.parse("2026-10-17T06:00:00Z");
    static final Instant START = T0.minus(Duration.ofHours(6));
    static final Instant END = T0.plus(Duration.ofHours(24));
    static final Instant FINAL = END.plus(Duration.ofHours(24));
    static final Instant AFTER = FINAL.plusSeconds(60);

    static SocialPhysicalComparator.Inputs inputs(Instant now, List<PhysicalEvidenceSource.Evidence> evidence,
            SocialPhysicalComparator.Detectability d, String reuse, String trigger) {
        return new SocialPhysicalComparator.Inputs(START, END, FINAL, now, evidence, d, reuse, T0, null, trigger);
    }

    @Test
    @DisplayName("corroborated: a non-social observation matches; leads over physical, official and news are signed")
    void corroboratedWithLeadTimes() {
        Instant physical = T0.plus(Duration.ofMinutes(47));
        Instant official = T0.plus(Duration.ofHours(2));
        SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(
                START, END, FINAL, AFTER, List.of(physical("canonical-event:A", "sensor-network", physical),
                physical("finding:B", "agency", official)), covered(), null, T0, T0.minus(Duration.ofMinutes(10)),
                null));
        assertThat(d.status()).isEqualTo(CorrelationVocabulary.CORROBORATED);
        assertThat(d.leadTimes().get("leadOverPhysicalSeconds")).isEqualTo(47.0 * 60);
        assertThat(d.leadTimes().get("leadOverOfficialSeconds")).isEqualTo(7200.0);
        assertThat(d.leadTimes().get("leadOverNewsSeconds")).isEqualTo(-600.0);
        assertThat(d.leadTimes().get("physicalFirst")).isEqualTo(physical.toString());
        assertThat(d.caveats()).anyMatch(c -> CorrelationVocabulary.OBSERVED_LEAD_TIME.equals(c.get("code")));
    }

    @Test
    @DisplayName("corroboration may arrive while the window is open")
    void corroboratedEarly() {
        assertThat(SocialPhysicalComparator.decide(inputs(T0.plusSeconds(3600), List.of(physical("canonical-event:A",
                "sensor-network", T0.plusSeconds(600))), covered(), null, null)).status())
                .isEqualTo(CorrelationVocabulary.CORROBORATED);
    }

    @Test
    @DisplayName("unobserved only after the window closes and only when canSeeAbsence is ok")
    void unobservedOnlyWithCoverage() {
        assertThat(SocialPhysicalComparator.decide(inputs(T0.plusSeconds(3600), List.of(), covered(), null, null))
                .status()).isEqualTo(CorrelationVocabulary.PENDING);
        assertThat(SocialPhysicalComparator.decide(inputs(AFTER, List.of(), covered(), null, null)).status())
                .isEqualTo(CorrelationVocabulary.UNOBSERVED);
        assertThat(SocialPhysicalComparator.decide(inputs(AFTER, List.of(), thin(), null, null)).status())
                .isEqualTo(CorrelationVocabulary.INSUFFICIENT);
        assertThat(SocialPhysicalComparator.decide(inputs(AFTER, List.of(), null, null, null)).status())
                .isEqualTo(CorrelationVocabulary.INSUFFICIENT);
    }

    @Test
    @DisplayName("contradicted by an explicit normal-service record and by a media reuse finding")
    void contradicted() {
        SocialPhysicalComparator.Decision record = SocialPhysicalComparator.decide(inputs(AFTER,
                List.of(negating("record:statuspage/x", T0.plusSeconds(60))), covered(), null, null));
        assertThat(record.status()).isEqualTo(CorrelationVocabulary.CONTRADICTED);
        assertThat(record.contradiction()).containsEntry("reason", CorrelationVocabulary.NEGATING_RECORD)
                .containsEntry("ref", "record:statuspage/x");
        SocialPhysicalComparator.Decision reuse = SocialPhysicalComparator.decide(inputs(AFTER,
                List.of(physical("canonical-event:A", "sensor-network", T0.plusSeconds(60))), covered(),
                "finding:R", null));
        assertThat(reuse.status()).isEqualTo(CorrelationVocabulary.CONTRADICTED);
        assertThat(reuse.contradiction()).containsEntry("reason", CorrelationVocabulary.MEDIA_REUSE);
    }

    @Test
    @DisplayName("a negating record does not outweigh matching physical evidence")
    void negatingWithEvidence() {
        assertThat(SocialPhysicalComparator.decide(inputs(AFTER, List.of(negating("record:s/x", T0),
                physical("canonical-event:A", "sensor-network", T0.plusSeconds(60))), covered(), null, null))
                .status()).isEqualTo(CorrelationVocabulary.CORROBORATED);
    }

    @Test
    @DisplayName("late evidence revises: insufficient-coverage, then corroborated when an observation is published")
    void lateEvidenceRevises() {
        SocialPhysicalComparator.Decision first = SocialPhysicalComparator.decide(inputs(END.plusSeconds(60),
                List.of(), thin(), null, null));
        SocialPhysicalComparator.Decision later = SocialPhysicalComparator.decide(inputs(END.plusSeconds(7200),
                List.of(physical("canonical-event:L", "agency", T0.plusSeconds(3600))), thin(), null, null));
        assertThat(first.status()).isEqualTo(CorrelationVocabulary.INSUFFICIENT);
        assertThat(later.status()).isEqualTo(CorrelationVocabulary.CORROBORATED);
        assertThat(ComparisonService.refsOf(row(later))).containsExactly("canonical-event:L");
    }

    @Test
    @DisplayName("social-only evidence never corroborates and is not kept as physical evidence (SOC-1)")
    void socialNeverCorroborates() {
        SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(inputs(AFTER,
                List.of(social("narrative:N", T0), social("narrative:M", T0.plusSeconds(60))), covered(), null, null));
        assertThat(d.status()).isEqualTo(CorrelationVocabulary.UNOBSERVED);
        assertThat(d.evidence()).isEmpty();
        assertThat(d.leadTimes().get("physicalFirst")).isNull();
    }

    @Test
    @DisplayName("narrative status from its comparisons: pending, agreeing, insufficient does not split, mixed")
    void narrativeStatus() {
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of())).isEqualTo("unassessed");
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of("pending", "pending"))).isEqualTo("pending");
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of("corroborated", "pending")))
                .isEqualTo("corroborated");
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of("unobserved", "insufficient-coverage")))
                .isEqualTo("unobserved");
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of("corroborated", "unobserved"))).isEqualTo("mixed");
        assertThat(SocialPhysicalComparator.narrativeStatus(List.of("insufficient-coverage")))
                .isEqualTo("insufficient-coverage");
    }

    @Test
    @DisplayName("claims are compared when asserted with a locality place, admin-1 only for large-area families")
    void comparable() {
        NarrativeStore.Claim locality = claim("asserted", "locality");
        assertThat(ComparisonService.comparable(locality, CorrelationTestSupport.conflictRule())).isTrue();
        assertThat(ComparisonService.comparable(claim("denied", "locality"), CorrelationTestSupport.conflictRule()))
                .isFalse();
        assertThat(ComparisonService.comparable(claim("asserted", "admin1"), CorrelationTestSupport.conflictRule()))
                .isFalse();
        CompareRules.FamilyRule seismic = CompareRules.bundled().rule("seismic", null, null, null);
        assertThat(seismic.largeArea()).isTrue();
        assertThat(ComparisonService.comparable(claim("asserted", "admin1"), seismic)).isTrue();
        assertThat(ComparisonService.comparable(claim("asserted", "country"), seismic)).isFalse();
    }

    @Test
    @DisplayName("the bundled rules give every family a window, tolerance and lag; operator overrides win")
    void rules() {
        CompareRules rules = CompareRules.bundled();
        CompareRules.FamilyRule conflict = rules.rule("conflict", null, null, null);
        assertThat(conflict.before()).isEqualTo(Duration.ofHours(6));
        assertThat(conflict.after()).isEqualTo(Duration.ofHours(24));
        assertThat(conflict.canonicalFamilies()).containsExactly("conflict");
        CompareRules.FamilyRule overridden = rules.rule("conflict", null, null, Map.of("after", "48h",
                "toleranceKm", 10));
        assertThat(overridden.after()).isEqualTo(Duration.ofHours(48));
        assertThat(overridden.toleranceKm()).isEqualTo(10.0);
        assertThat(rules.rule("outage", null, null, null).negations()).isNotEmpty();
        assertThat(rules.rule("unknown-family", Duration.ofHours(1), Duration.ofHours(2), null).after())
                .isEqualTo(Duration.ofHours(2));
    }

    static NarrativeStore.Claim claim(String polarity, String precision) {
        Map<String, Object> place = new java.util.LinkedHashMap<>(CorrelationTestSupport.place("Aden", 45.03, 12.78,
                "YE"));
        place.put("precision", precision);
        return new NarrativeStore.Claim("C", "N", "k", "explosion", "conflict", polarity, false, place, 45.03, 12.78,
                "YE", List.of(), null, null, 3, 3, T0, "deterministic", null, 0.8, "unassessed", null, null, "F", 6,
                "CLEAR", T0, T0, null, null, null, null);
    }

    static ComparisonStore.Comparison row(SocialPhysicalComparator.Decision d) {
        return new ComparisonStore.Comparison("X", "C", "N", "explosion", "conflict", Map.of(), null, null, "YE",
                START, END, d.status(), SocialPhysicalComparator.evidenceView(d.evidence()), Map.of(), d.leadTimes(),
                d.contradiction(), List.of(), "organic", null, null, List.of("bluesky"), 3, null, 1, FINAL, null, T0, T0);
    }
}
