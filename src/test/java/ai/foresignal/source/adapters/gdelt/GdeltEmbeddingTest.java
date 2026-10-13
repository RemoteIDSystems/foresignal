package ai.foresignal.source.adapters.gdelt;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.search.EmbeddingText;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prompt 066 with prompt 027: only {@code summary} becomes embedded text; the source URL, actor codes and numbers do
 * not, so nothing taken from the linked article can reach an embedding.
 */
class GdeltEmbeddingTest {

    @Test
    @DisplayName("the embedded text of every record is kind | source name | summary, and nothing else")
    void onlySummary() {
        SourceDescriptor descriptor = new GdeltEventsAdapter("", null, null).descriptor();
        assertThat(descriptor.embedFields()).containsExactly("summary");
        List<NormalizedRecord> records = GdeltExportParser.parse(AdapterFixtures.bytes(GdeltEventsAdapter.ID,
                "export-40.tsv"), GdeltExportParser.Options.DEFAULTS, null).records();
        assertThat(records).isNotEmpty();
        for (NormalizedRecord r : records) {
            String text = EmbeddingText.of(r.kind(), descriptor.name(), r.payload(), descriptor.embedFields());
            assertThat(text).isEqualTo("conflict-event | GDELT events | " + r.payload().get("summary"));
            assertThat(text).doesNotContain("http");
        }
    }
}
