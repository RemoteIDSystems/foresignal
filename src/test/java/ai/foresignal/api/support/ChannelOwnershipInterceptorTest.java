package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The paths {@link ChannelOwnershipInterceptor} checks (prompt R2-009). */
class ChannelOwnershipInterceptorTest {

    static final UUID ID = UUID.fromString("0b6f3c1e-5d1a-4f6e-9a3b-2c4d5e6f7a8b");

    @Test
    @DisplayName("every single-channel path names its channel")
    void channelPaths() {
        for (String path : new String[] {"/v1/channels/" + ID, "/v1/channels/" + ID + "/test",
            "/v1/channels/" + ID + "/mqtt/republish", "/v1/channels/" + ID + "/deliveries"}) {
            assertThat(ChannelOwnershipInterceptor.channelId(path)).as(path).contains(ID);
        }
    }

    @Test
    @DisplayName("the collection and the type catalog are left to the controller")
    void otherPaths() {
        for (String path : new String[] {"/v1/channels", "/v1/channels/types", "/v1/channels/abc"}) {
            assertThat(ChannelOwnershipInterceptor.channelId(path)).as(path).isEmpty();
        }
    }
}
