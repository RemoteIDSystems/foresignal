package ai.foresignal.source.formats.attention;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TopicParams} (prompt 117): the {@code topics} entries round-trip, invalid entries are reported and left
 * out, and the dry GDELT query check catches control characters and unbalanced quotes or parentheses.
 */
class TopicParamsTest {

    @Test
    @DisplayName("GDELT and Wikipedia entries round-trip through the parameter form")
    void roundTrip() {
        String gdelt = TopicParams.encode(new TopicParams.GdeltTopic("red-sea", "(\"Red Sea\" OR Houthi)"));
        assertThat(TopicParams.gdeltProblem(gdelt)).isNull();
        assertThat(TopicParams.gdelt(List.of(gdelt))).containsExactly(
                new TopicParams.GdeltTopic("red-sea", "(\"Red Sea\" OR Houthi)"));
        String wiki = TopicParams.encode(new TopicParams.WikiTopic("red-sea",
                List.of(new TopicParams.WikiArticle("en.wikipedia", "Red Sea"))));
        assertThat(TopicParams.wikiProblem(wiki)).isNull();
        assertThat(TopicParams.wiki(List.of(wiki)).get(0).articles()).containsExactly(
                new TopicParams.WikiArticle("en.wikipedia", "Red_Sea"));
    }

    @Test
    @DisplayName("invalid entries are reported and left out")
    void invalid() {
        assertThat(TopicParams.gdeltProblem("not json")).isNotNull();
        assertThat(TopicParams.gdeltProblem("{\"id\":\"X\",\"query\":\"a\"}")).contains("id");
        assertThat(TopicParams.gdeltProblem("{\"id\":\"abc\"}")).contains("query");
        assertThat(TopicParams.wikiProblem("{\"id\":\"abc\",\"articles\":[]}")).contains("article");
        assertThat(TopicParams.wikiProblem("{\"id\":\"abc\",\"articles\":[{\"project\":\"en.wikipedia\","
                + "\"article\":\"A|B\"}]}")).contains("cannot hold");
        assertThat(TopicParams.gdelt(List.of("{\"id\":\"abc\"}"))).isEmpty();
        assertThat(TopicParams.wiki(null)).isEmpty();
    }

    @Test
    @DisplayName("the dry query check")
    void queryCheck() {
        assertThat(TopicParams.queryProblem("\"Strait of Hormuz\"")).isNull();
        assertThat(TopicParams.queryProblem("(a OR b) (c OR d)")).isNull();
        assertThat(TopicParams.queryProblem("\"open")).contains("double quote");
        assertThat(TopicParams.queryProblem("(a OR b")).contains("'('");
        assertThat(TopicParams.queryProblem("a OR b)")).contains("')'");
        assertThat(TopicParams.queryProblem("a\u0007b")).contains("control");
        assertThat(TopicParams.queryProblem(" ")).contains("blank");
        assertThat(TopicParams.queryProblem("x".repeat(251))).contains("250");
        assertThat(TopicParams.queryProblem("\"(inside quotes\"")).isNull();
    }
}
