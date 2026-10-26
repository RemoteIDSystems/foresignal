package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.SourceDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code satdump-aggregator} (prompt R2-022): a SatDump station under the reserved name {@code aggregator}, with
 * exactly a configured station's descriptor, which no configured station may take.
 */
class SatDumpAggregatorAdapterTest {

    private final SatDumpAggregatorAdapter adapter = new SatDumpAggregatorAdapter(null, null, null, null, null);

    @Test
    @DisplayName("the id is satdump-aggregator and the descriptor is a station's")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.id()).isEqualTo("satdump-aggregator").isEqualTo(SatDumpDescriptor.sourceId("aggregator"));
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.params()).isEqualTo(SatDumpDescriptor.build("home").params());
        assertThat(d.kinds()).isEqualTo(SatDumpDescriptor.build("home").kinds());
        assertThat(d.licence()).isEqualTo(SatDumpDescriptor.LICENSE);
        assertThat(adapter.station()).isEqualTo(SatDumpAggregatorAdapter.STATION);
    }

    @Test
    @DisplayName("the station list refuses the reserved name")
    void reservedName() {
        assertThatThrownBy(() -> SatDumpFamily.names("home,aggregator")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reserved");
        assertThat(SatDumpFamily.names("home,aggregators")).containsExactly("home", "aggregators");
    }
}
