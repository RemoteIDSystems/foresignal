package ai.foresignal.source.adapters.wikistream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link RecentChangeEvent} against a trimmed recentchange payload (prompt 095). */
class RecentChangeEventTest {

    private static final String FIXTURE = AdapterFixtures.text("wiki-edit-surge", "recentchange-data.json");
    private static final Set<String> WIKIS = Set.of("enwiki");

    @Test
    @DisplayName("parses the envelope and preserves the verbatim meta.id array for Last-Event-ID")
    void parses() {
        RecentChangeEvent event = RecentChangeEvent.parse(FIXTURE).orElseThrow();
        assertThat(event.wiki()).isEqualTo("enwiki");
        assertThat(event.title()).isEqualTo("Example article");
        assertThat(event.type()).isEqualTo("edit");
        assertThat(event.namespace()).isZero();
        assertThat(event.dt()).isEqualTo(Instant.parse("2026-09-01T12:00:00Z"));
        assertThat(event.id()).isEqualTo("[{\"topic\":\"eqiad.mediawiki.recentchange\",\"partition\":0,\"offset\":123456}]");
    }

    @Test
    @DisplayName("keeps an edit on a watched wiki in the main namespace by a human")
    void kept() {
        assertThat(RecentChangeEvent.parse(FIXTURE).orElseThrow().kept(WIKIS)).isTrue();
    }

    @Test
    @DisplayName("drops the canary, bots, non-edit types, other namespaces and unwatched wikis")
    void filters() {
        assertThat(parse("canary", "\"en.wikipedia.org\"", "\"edit\"", 0, false, "enwiki").kept(WIKIS)).isFalse();
        assertThat(parse("en.wikipedia.org", "\"en.wikipedia.org\"", "\"edit\"", 0, true, "enwiki").kept(WIKIS))
                .isFalse();
        assertThat(parse("en.wikipedia.org", "\"en.wikipedia.org\"", "\"log\"", 0, false, "enwiki").kept(WIKIS))
                .isFalse();
        assertThat(parse("en.wikipedia.org", "\"en.wikipedia.org\"", "\"edit\"", 1, false, "enwiki").kept(WIKIS))
                .isFalse();
        assertThat(parse("en.wikipedia.org", "\"en.wikipedia.org\"", "\"edit\"", 0, false, "dewiki").kept(WIKIS))
                .isFalse();
        assertThat(parse("en.wikipedia.org", "\"en.wikipedia.org\"", "\"new\"", 0, false, "enwiki").kept(WIKIS))
                .isTrue();
    }

    private static RecentChangeEvent parse(String domain, String metaDomain, String type, int namespace, boolean bot,
            String wiki) {
        String json = "{\"meta\":{\"dt\":\"2026-09-01T12:00:00Z\",\"domain\":" + metaDomain + "},"
                + "\"type\":" + type + ",\"namespace\":" + namespace + ",\"title\":\"T\",\"wiki\":\"" + wiki + "\","
                + "\"bot\":" + bot + ",\"domain\":\"" + domain + "\"}";
        return RecentChangeEvent.parse(json).orElseThrow();
    }

    @Test
    @DisplayName("an empty payload is empty and a non-object payload is a format change")
    void edges() {
        assertThat(RecentChangeEvent.parse("")).isEmpty();
        assertThatThrownBy(() -> RecentChangeEvent.parse("{not json")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> RecentChangeEvent.parse("[]")).isInstanceOf(UpstreamFormatException.class);
        assertThat(Optional.ofNullable(RecentChangeEvent.parse("{\"meta\":{}}").orElse(null))).isEmpty();
    }
}
