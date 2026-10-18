package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Coordination indicators (prompt 136): a planted synchronous cluster is detected; an organic burst after a real event
 * (many authors, their own words, close in time) is not; Benjamini–Hochberg controls the day's narratives; the
 * statement never names an account.
 */
class CoordinationDetectorTest {

    static final Instant START = Instant.parse("2026-10-16T12:00:00Z");
    static final Instant END = START.plus(Duration.ofHours(24));

    private static String words(Random r, int n) {
        String[] vocabulary = {"port", "city", "smoke", "loud", "heard", "street", "near", "people", "video", "today",
            "night", "police", "market", "bridge", "river", "crowd", "school", "airport", "train", "road", "north",
            "south", "east", "west", "big", "small", "house", "car", "bus", "phone", "water", "power", "light"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(vocabulary[r.nextInt(vocabulary.length)]).append(r.nextInt(1000)).append(' ');
        }
        return sb.toString();
    }

    /** A narrative of independent authors posting their own words spread over the day. */
    static List<CoordinationDetector.Post> organic(Random r, int authors) {
        List<CoordinationDetector.Post> out = new ArrayList<>();
        for (int i = 0; i < authors; i++) {
            out.add(new CoordinationDetector.Post("o" + i, START.plusSeconds(r.nextInt(86_400)),
                    TextSignatures.signature(words(r, 12)), null, null));
        }
        return out;
    }

    /** 40 accounts posting one text within seconds of each other, three times a day, plus organic posts. */
    static List<CoordinationDetector.Post> planted(Random r) {
        List<CoordinationDetector.Post> out = new ArrayList<>(organic(r, 40));
        String script = "Breaking: the port attack was staged, share before they delete this, wake up";
        for (int wave = 0; wave < 3; wave++) {
            Instant t = START.plus(Duration.ofHours(4 + 6L * wave));
            for (int i = 0; i < 40; i++) {
                out.add(new CoordinationDetector.Post("c" + i, t.plusSeconds(r.nextInt(30)),
                        TextSignatures.signature(script), null, null));
            }
        }
        return out;
    }

    @Test
    @DisplayName("a planted synchronous cluster: synchrony far above its shuffled-time null")
    void plantedSynchrony() {
        CoordinationDetector.Indicators ind = CoordinationDetector.compute("P", planted(new Random(1)), START, END,
                null, 42);
        assertThat(ind.synchrony().available()).isTrue();
        assertThat(ind.synchrony().value()).isGreaterThan(0.4);
        assertThat(ind.synchrony().value()).isGreaterThan(ind.synchrony().nullExpectation());
        assertThat(ind.synchrony().pValue()).isLessThan(1e-4);
        assertThat(ind.textDuplication().value()).isGreaterThan(0.4);
        assertThat(ind.accountAgeSkew().available()).as("no creation dates").isFalse();
    }

    @Test
    @DisplayName("an organic burst after a real event (own words, close in time) is not synchronous")
    void organicBurstNotFlagged() {
        Random r = new Random(2);
        List<CoordinationDetector.Post> burst = new ArrayList<>();
        Instant event = START.plus(Duration.ofHours(10));
        for (int i = 0; i < 80; i++) {
            burst.add(new CoordinationDetector.Post("b" + i, event.plusSeconds(r.nextInt(600)),
                    TextSignatures.signature("explosion " + words(r, 10)), null, null));
        }
        CoordinationDetector.Indicators ind = CoordinationDetector.compute("O", burst, START, END, null, 7);
        assertThat(ind.synchrony().value()).isZero();
        assertThat(ind.synchrony().pValue()).isGreaterThan(0.01);
        assertThat(ind.textDuplication().value()).isZero();
    }

    @Test
    @DisplayName("BH across the day: only the planted narrative gets ≥ 2 significant indicators and the statement")
    void benjaminiHochberg() {
        Random r = new Random(3);
        List<CoordinationDetector.Indicators> day = new ArrayList<>();
        for (int n = 0; n < 20; n++) {
            day.add(CoordinationDetector.compute("N" + n, organic(r, 30 + r.nextInt(30)), START, END, null, n));
        }
        day.add(CoordinationDetector.compute("P", planted(r), START, END, null, 99));
        List<CoordinationDetector.Indicators> adjusted = CoordinationDetector.adjust(day);
        for (CoordinationDetector.Indicators ind : adjusted) {
            if (ind.narrativeId().equals("P")) {
                assertThat(ind.significant()).contains("synchrony", "textDuplication");
                assertThat(ind.detected()).isTrue();
                Map<String, Object> view = ind.toMap("F1", END);
                assertThat(view.get("statement")).isEqualTo(NarrativeVocabulary.COORDINATION_STATEMENT);
                assertThat(view.get("significantCount")).isEqualTo(ind.significant().size());
                assertThat(view.toString()).doesNotContain("c0", "c1");
            } else {
                assertThat(ind.detected()).as(ind.narrativeId()).isFalse();
                assertThat(ind.synchrony().qValue()).isNotNull();
            }
        }
    }

    @Test
    @DisplayName("account-age skew against the platform baseline (exact binomial tail)")
    void accountAge() {
        List<CoordinationDetector.Post> posts = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Instant created = i < 30 ? START.minus(Duration.ofDays(3)) : START.minus(Duration.ofDays(900));
            posts.add(new CoordinationDetector.Post("a" + i, START.plusSeconds(i * 60L), null, created, null));
        }
        CoordinationDetector.Indicator skew = CoordinationDetector.accountAge(posts, 0.05);
        assertThat(skew.available()).isTrue();
        assertThat(skew.value()).isEqualTo(0.6);
        assertThat(skew.nullExpectation()).isEqualTo(0.05);
        assertThat(skew.pValue()).isLessThan(1e-10);
        assertThat(CoordinationDetector.accountAge(posts, null).available()).isFalse();
        assertThat(CoordinationDetector.binomialUpperTail(0, 10, 0.5)).isEqualTo(1.0);
        assertThat(CoordinationDetector.binomialUpperTail(10, 10, 0.5)).isCloseTo(1 / 1024.0,
                org.assertj.core.data.Offset.offset(1e-12));
    }

    @Test
    @DisplayName("co-repost bursts: pairs sharing ≥ 5 reposts within 10 minutes beat the degree-preserving null")
    void coReposts() {
        Random r = new Random(4);
        List<CoordinationDetector.Post> posts = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            Instant at = START.plus(Duration.ofHours(t * 2L));
            for (int a = 0; a < 6; a++) {
                posts.add(new CoordinationDetector.Post("ring" + a, at.plusSeconds(r.nextInt(120)), null, null,
                        "bluesky:target" + t));
            }
        }
        for (int i = 0; i < 60; i++) {
            posts.add(new CoordinationDetector.Post("fan" + i, START.plusSeconds(r.nextInt(86_400)), null, null,
                    "bluesky:target" + r.nextInt(8)));
        }
        CoordinationDetector.Indicator ind = CoordinationDetector.coReposts(posts, 11);
        assertThat(ind.available()).isTrue();
        assertThat(ind.value()).isGreaterThan(ind.nullExpectation());
        assertThat(ind.pValue()).isLessThan(0.01);
    }

    @Test
    @DisplayName("the normal tail is monotone and bounded")
    void tail() {
        assertThat(CoordinationDetector.upperTail(0)).isCloseTo(0.5, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(CoordinationDetector.upperTail(3)).isLessThan(CoordinationDetector.upperTail(2));
        assertThat(CoordinationDetector.upperTail(10)).isGreaterThan(0).isLessThan(1e-20);
        assertThat(CoordinationDetector.upperTail(-3)).isGreaterThan(0.99);
    }
}
