package ai.foresignal.sink;

import ai.foresignal.api.error.NotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** The registered sink types (prompt 110). */
@Component
public class SinkTypeRegistry {

    private final Map<String, SinkType> byId = new LinkedHashMap<>();
    private final List<SinkType> all;

    /** Collects every {@link SinkType} bean. */
    public SinkTypeRegistry(List<SinkType> types) {
        this.all = types.stream().sorted(java.util.Comparator.comparing(SinkType::id)).toList();
        for (SinkType type : all) {
            byId.put(type.id(), type);
        }
    }

    /** Every type, by id. */
    public List<SinkType> all() {
        return all;
    }

    /** The type with {@code id}, or empty. */
    public Optional<SinkType> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** The type with {@code id}. */
    public SinkType get(String id) {
        SinkType type = byId.get(id);
        if (type == null) {
            throw NotFoundException.of("SinkType", id);
        }
        return type;
    }
}
