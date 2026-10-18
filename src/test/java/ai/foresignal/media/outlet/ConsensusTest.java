package ai.foresignal.media.outlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Consensus} (prompt 133): median, disagreement, provider breakdown and the lean buckets at the boundaries. */
class ConsensusTest {

    private static Consensus.ProviderValue v(String provider, Double value) {
        return new Consensus.ProviderValue(provider, value, null, Instant.parse("2026-10-01T00:00:00Z"), "CC0 1.0",
                MediaVocabulary.OPEN);
    }

    @Test
    @DisplayName("the median of an odd and an even number of providers")
    void median() {
        assertThat(Consensus.compute("lean", List.of(v("wikidata", -2.0), v("partisan-web", -1.0),
                v("operator", 2.0))).median()).isEqualTo(-1.0);
        assertThat(Consensus.compute("lean", List.of(v("wikidata", -2.0), v("partisan-web", -1.0))).median())
                .isEqualTo(-1.5);
        assertThat(Consensus.compute("factuality", List.of(v("operator", 4.0))).median()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("disagreement is max minus min, and every provider stays in the breakdown")
    void disagreement() {
        Consensus.Result r = Consensus.compute("lean", List.of(v("operator", 2.0), v("wikidata", -2.0),
                v("partisan-web", 0.0)));
        assertThat(r.disagreement()).isEqualTo(4.0);
        assertThat(r.nProviders()).isEqualTo(3);
        assertThat(r.providers()).extracting(Consensus.ProviderValue::provider)
                .containsExactly("wikidata", "partisan-web", "operator");
    }

    @Test
    @DisplayName("label-only ratings count as providers but not in the median")
    void labelOnly() {
        Consensus.Result r = Consensus.compute("lean", List.of(v("wikidata", null), v("operator", 1.0)));
        assertThat(r.nProviders()).isEqualTo(2);
        assertThat(r.median()).isEqualTo(1.0);
        assertThat(r.disagreement()).isEqualTo(0.0);
        Consensus.Result none = Consensus.compute("lean", List.of(v("wikidata", null)));
        assertThat(none.median()).isNull();
        assertThat(none.bucket()).isNull();
        assertThat(none.disagreement()).isNull();
    }

    @Test
    @DisplayName("buckets: |median| < 0.75 is center; -0.75 is left and +0.75 is right")
    void buckets() {
        assertThat(Consensus.bucket(0.0)).isEqualTo("center");
        assertThat(Consensus.bucket(0.7499)).isEqualTo("center");
        assertThat(Consensus.bucket(-0.7499)).isEqualTo("center");
        assertThat(Consensus.bucket(0.75)).isEqualTo("right");
        assertThat(Consensus.bucket(-0.75)).isEqualTo("left");
        assertThat(Consensus.bucket(2.0)).isEqualTo("right");
        assertThat(Consensus.bucket(-2.0)).isEqualTo("left");
        assertThat(Consensus.bucket(null)).isNull();
    }

    @Test
    @DisplayName("only lean has a bucket")
    void bucketOnlyForLean() {
        assertThat(Consensus.compute("factuality", List.of(v("operator", 4.0))).bucket()).isNull();
        assertThat(Consensus.compute("lean", List.of(v("operator", -1.0))).bucket()).isEqualTo("left");
    }

    @Test
    @DisplayName("a caller without local egress gets the consensus of the OPEN providers only")
    void openOnly() {
        Consensus.ProviderValue local = new Consensus.ProviderValue("allsides", 2.0, "Right", null,
                "CC BY-NC 4.0", MediaVocabulary.LOCAL_ONLY);
        Consensus.Result all = Consensus.compute("lean", List.of(v("wikidata", -1.0), local));
        Consensus.Result open = OutletViews.openOnly(all);
        assertThat(all.median()).isEqualTo(0.5);
        assertThat(open.median()).isEqualTo(-1.0);
        assertThat(open.providers()).extracting(Consensus.ProviderValue::provider).containsExactly("wikidata");
    }
}
