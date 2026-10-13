package ai.foresignal.source.codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Every {@link SocketCodec} bean, by id (prompt 069). A duplicate or malformed id fails startup rather than
 * letting one codec shadow another.
 */
@Component
public class CodecRegistry {

    /** A codec id: kebab-case letters and digits. */
    static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private final Map<String, SocketCodec> byId;

    /**
     * Collects the codecs.
     *
     * @throws IllegalStateException on a duplicate or malformed id
     */
    public CodecRegistry(List<SocketCodec> codecs) {
        Map<String, SocketCodec> map = new TreeMap<>();
        for (SocketCodec codec : codecs == null ? List.<SocketCodec>of() : codecs) {
            String id = codec.id();
            if (id == null || !ID.matcher(id).matches()) {
                throw new IllegalStateException("Socket codec " + codec.getClass().getName() + " has an invalid id '"
                        + id + "'; ids are kebab-case");
            }
            SocketCodec previous = map.putIfAbsent(id, codec);
            if (previous != null) {
                throw new IllegalStateException("Socket codec id '" + id + "' is declared by both "
                        + previous.getClass().getName() + " and " + codec.getClass().getName());
            }
        }
        this.byId = Map.copyOf(map);
    }

    /** The codec with {@code id}, if registered. */
    public Optional<SocketCodec> find(String id) {
        return Optional.ofNullable(id == null ? null : byId.get(id));
    }

    /**
     * The codec with {@code id}.
     *
     * @throws IllegalStateException when none is registered
     */
    public SocketCodec require(String id) {
        return find(id).orElseThrow(() -> new IllegalStateException("No socket codec '" + id + "' is registered; known: "
                + new TreeMap<>(byId).keySet()));
    }

    /** Every codec, ordered by id. */
    public Collection<SocketCodec> all() {
        return new TreeMap<>(byId).values();
    }
}
