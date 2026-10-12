package ai.foresignal.security.visibility;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PrivateFilter} (prompt 039): the one helper query builders use to keep hidden sources out. */
class PrivateFilterTest {

    @Test
    @DisplayName("allows and isEmpty")
    void allows() {
        PrivateFilter filter = new PrivateFilter(Set.of("family", "home"));
        assertThat(filter.allows("quakes")).isTrue();
        assertThat(filter.allows("family")).isFalse();
        assertThat(filter.allows(null)).isFalse();
        assertThat(filter.isEmpty()).isFalse();
        assertThat(PrivateFilter.NONE.isEmpty()).isTrue();
        assertThat(PrivateFilter.NONE.allows("family")).isTrue();
        assertThat(new PrivateFilter(null).hidden()).isEmpty();
    }

    @Test
    @DisplayName("sql: TRUE when nothing is hidden, a sorted NOT IN list otherwise, with or without an alias")
    void sql() {
        assertThat(PrivateFilter.NONE.sql("h")).isEqualTo("TRUE");
        PrivateFilter filter = new PrivateFilter(Set.of("home", "family"));
        assertThat(filter.sql("h")).isEqualTo("h.source_id NOT IN ('family','home')");
        assertThat(filter.sql(null)).isEqualTo("source_id NOT IN ('family','home')");
        assertThat(filter.sql(" ")).isEqualTo("source_id NOT IN ('family','home')");
    }

    @Test
    @DisplayName("an id that is not safe to inline fails closed (FALSE)")
    void unsafeIdFailsClosed() {
        assertThat(new PrivateFilter(Set.of("a'); DROP TABLE x; --")).sql("h")).isEqualTo("FALSE");
    }

    @Test
    @DisplayName("plus adds hidden sources; an empty addition returns the same filter")
    void plus() {
        PrivateFilter filter = new PrivateFilter(Set.of("family"));
        assertThat(filter.plus(Set.of())).isSameAs(filter);
        assertThat(filter.plus(Set.of("home")).hidden()).containsExactly("family", "home");
    }
}
