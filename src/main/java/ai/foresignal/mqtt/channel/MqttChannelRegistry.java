package ai.foresignal.mqtt.channel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** The runtimes of the enabled {@code mqtt} channels, by channel id (prompt 060). */
@Component
public class MqttChannelRegistry {

    private final Map<UUID, MqttChannelRuntime> runtimes = new ConcurrentHashMap<>();

    /** The runtime of {@code channelId}, if the channel is enabled and connected or connecting. */
    public Optional<MqttChannelRuntime> find(UUID channelId) {
        return Optional.ofNullable(runtimes.get(channelId));
    }

    /** Every runtime. */
    public List<MqttChannelRuntime> all() {
        return List.copyOf(runtimes.values());
    }

    /** Registers {@code runtime}, returning the one it replaces, or null. */
    MqttChannelRuntime put(MqttChannelRuntime runtime) {
        return runtimes.put(runtime.channelId(), runtime);
    }

    /** Removes and returns the runtime of {@code channelId}, or null. */
    MqttChannelRuntime remove(UUID channelId) {
        return runtimes.remove(channelId);
    }
}
