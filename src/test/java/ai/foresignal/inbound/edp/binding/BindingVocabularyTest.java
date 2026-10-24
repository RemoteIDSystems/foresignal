package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The binding vocabulary (prompt 138): targets in the spec's order, item-type requirements and forecaster ids. */
class BindingVocabularyTest {

    @Test
    @DisplayName("targets follow the spec's enum; each has item types; social and news need their document types")
    void targets() {
        assertThat(BindingVocabulary.TARGETS).containsExactly("series", "canonical-family", "social", "news",
                "evidence", "indicator", "forecast", "place", "situation", "chronicle");
        assertThat(BindingVocabulary.ITEM_TYPES).containsOnlyKeys(BindingVocabulary.TARGETS);
        assertThat(BindingVocabulary.ITEM_TYPES.get("series")).containsExactly("observation");
        assertThat(BindingVocabulary.ITEM_TYPES.get("forecast")).containsExactly("forecast");
        assertThat(BindingVocabulary.documentType("social")).isEqualTo("social-post");
        assertThat(BindingVocabulary.documentType("news")).isEqualTo("article");
        assertThat(BindingVocabulary.documentType("series")).isNull();
        assertThat(BindingVocabulary.requirement("series")).isEqualTo("a stream of observation items");
        assertThat(BindingVocabulary.requirement("news")).contains("documentType article");
        assertThat(BindingVocabulary.STATES).containsExactly("active", "disabled", "backfilling", "error");
        assertThat(BindingVocabulary.BACKFILL_CAP).isEqualTo(100_000);
        assertThat(BindingVocabulary.PREVIEW_ITEMS).isEqualTo(100);
        assertThat(BindingVocabulary.MIN_PROBABILITY_CHANGE).isEqualTo(0.02);
    }

    @Test
    @DisplayName("external forecasters are edp:{sourceId}:{streamId}, one path segment matching the contract pattern")
    void forecasterIds() {
        String id = BindingVocabulary.forecasterId("ext-markets", "kalshi");
        assertThat(id).isEqualTo("edp:ext-markets:kalshi");
        // Forecaster.id and the updateForecaster path parameter: ^[a-z0-9][a-z0-9._:-]{0,127}$ (no '/').
        assertThat(id).matches("^[a-z0-9][a-z0-9._:-]{0,127}$");
    }
}
