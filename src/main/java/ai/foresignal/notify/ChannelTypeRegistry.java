package ai.foresignal.notify;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Every {@link ChannelType} bean, by id (prompt 018). */
@Component
public class ChannelTypeRegistry {

    private final Map<String, ChannelType> types = new LinkedHashMap<>();

    /** Collects the types; a duplicate id is a programming error. */
    public ChannelTypeRegistry(List<ChannelType> beans) {
        beans.stream().sorted(Comparator.comparing(ChannelType::id)).forEach(type -> {
            if (types.putIfAbsent(type.id(), type) != null) {
                throw new IllegalStateException("Two channel types claim the id " + type.id());
            }
        });
    }

    /** The type with {@code id}, if registered. */
    public Optional<ChannelType> find(String id) {
        return Optional.ofNullable(id == null ? null : types.get(id));
    }

    /** The type with {@code id}. */
    public ChannelType get(String id) {
        return find(id).orElseThrow(() -> new IllegalStateException("No channel type " + id));
    }

    /** Every type, ordered by id. */
    public List<ChannelType> all() {
        return List.copyOf(types.values());
    }
}
