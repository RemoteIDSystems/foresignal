package ai.foresignal.source.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The in-memory form of the collection union (prompt R2-010). */
class CollectionUnionTest {

    @Test
    @DisplayName("with no active user every instance-enabled source is wanted")
    void noUsers() {
        assertThat(CollectionUnion.wanted(0, 0)).isTrue();
    }

    @Test
    @DisplayName("a source is wanted until every active user turned it off")
    void someoneWants() {
        assertThat(CollectionUnion.wanted(3, 0)).isTrue();
        assertThat(CollectionUnion.wanted(3, 2)).isTrue();
        assertThat(CollectionUnion.wanted(3, 3)).isFalse();
    }

    @Test
    @DisplayName("the SQL form is the same rule: active users, and a disabled row per user")
    void sqlShape() {
        assertThat(CollectionUnion.WANTED_SQL).contains("u.status = 'active'").contains("not p.enabled")
                .contains("s.source_id");
    }
}
