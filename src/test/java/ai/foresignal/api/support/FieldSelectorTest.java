package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class FieldSelectorTest {

    @Test
    void absentSelectsEverything() {
        assertThat(FieldSelector.parse(null)).isSameAs(FieldSelection.ALL);
        assertThat(FieldSelector.parse(" , ")).isSameAs(FieldSelection.ALL);
        assertThat(FieldSelection.ALL.includes("anything")).isTrue();
        assertThat(FieldSelection.ALL.projectsPayload()).isFalse();
        assertThat(FieldSelection.ALL.canonical()).isEqualTo("*");
    }

    @Test
    void splitsTopLevelAndPayloadPaths() {
        FieldSelection selection = FieldSelector.parse("externalId, title,geometry,payload.mag,payload.a.b");
        assertThat(selection.all()).isFalse();
        assertThat(selection.topLevel()).containsExactlyInAnyOrder("externalId", "title", "geometry");
        assertThat(selection.payloadPaths()).containsExactly("mag", "a.b");
        assertThat(selection.includes("payload")).isTrue();
        assertThat(selection.projectsPayload()).isTrue();
        assertThat(selection.includesGeometry()).isTrue();
        assertThat(selection.includes("seq")).isFalse();
    }

    @Test
    void wholePayloadWinsOverPaths() {
        FieldSelection selection = FieldSelector.parse("payload,payload.mag");
        assertThat(selection.includes("payload")).isTrue();
        assertThat(selection.projectsPayload()).isFalse();
    }

    @Test
    void geometryCanBeLeftOut() {
        assertThat(FieldSelector.parse("source").includesGeometry()).isFalse();
    }

    @Test
    void rejectsUnknownTopLevelNames() {
        assertThatThrownBy(() -> FieldSelector.parse("externalId,nonsense,bogus"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not valid")
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().getFirst().message())
                        .contains("nonsense", "bogus"));
    }

    @Test
    void rejectsBadPaths() {
        assertThatThrownBy(() -> FieldSelector.parse("payload.")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> FieldSelector.parse("payload.a..b")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> FieldSelector.parse("payload.a b")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> FieldSelector.parse("payload.{x}")).isInstanceOf(ValidationException.class);
    }

    @Test
    void capsTheEntryCount() {
        String many = String.join(",", Collections.nCopies(FieldSelector.MAX_ENTRIES + 1, "source"));
        assertThatThrownBy(() -> FieldSelector.parse(many)).isInstanceOf(ValidationException.class);
    }

    @Test
    void canonicalIsOrderInsensitiveForTopLevel() {
        assertThat(FieldSelector.parse("title,source").canonical())
                .isEqualTo(FieldSelector.parse("source,title").canonical());
    }
}
