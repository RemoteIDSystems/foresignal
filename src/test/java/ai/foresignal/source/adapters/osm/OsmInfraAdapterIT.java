package ai.foresignal.source.adapters.osm;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** The {@code osm-infrastructure} adapter (prompt 104): no inbox file is unchanged, no network. */
@WebIntegrationTest
class OsmInfraAdapterIT {

    @Autowired OsmInfrastructureAdapter adapter;

    @Test
    @DisplayName("the descriptor is ODbL, RECEIVED, no TTL and disabled by default")
    void descriptor() {
        var descriptor = adapter.descriptor();
        assertThat(descriptor.licence()).isEqualTo("ODbL-1.0");
        assertThat(descriptor.mode().allowsUpstreamRequests()).isTrue();
        assertThat(descriptor.recordTtl()).isNull();
        assertThat(descriptor.kinds()).contains("substation", "hospital", "power-line");
    }
}
