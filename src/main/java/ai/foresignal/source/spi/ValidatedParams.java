package ai.foresignal.source.spi;

import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Envelope;

/**
 * An adapter's parameters, already coerced and checked against its {@link ParamSpec}s.
 *
 * <p>Adapters read typed values and never parse strings, so a configuration mistake surfaces at the
 * API boundary with a field path rather than as an upstream request with a nonsense query string.
 *
 * @param values coerced values keyed by param name, with defaults applied
 * @param pinnedAddresses for each {@link ParamType#LAN_URL} param, the address resolved at
 *     validation time
 */
public record ValidatedParams(Map<String, Object> values, Map<String, InetAddress> pinnedAddresses) {

    public ValidatedParams {
        values = values == null ? Map.of() : Map.copyOf(values);
        pinnedAddresses = pinnedAddresses == null ? Map.of() : Map.copyOf(pinnedAddresses);
    }

    public static ValidatedParams empty() {
        return new ValidatedParams(Map.of(), Map.of());
    }

    public static ValidatedParams of(Map<String, Object> values) {
        return new ValidatedParams(values, Map.of());
    }

    /** True when {@code name} has a value. */
    public boolean has(String name) {
        return values.get(name) != null;
    }

    public String string(String name) {
        return value(name, String.class);
    }

    public Optional<String> optionalString(String name) {
        return Optional.ofNullable(value(name, String.class));
    }

    public Integer integer(String name) {
        Number number = value(name, Number.class);
        return number == null ? null : number.intValue();
    }

    public int integer(String name, int fallback) {
        Integer value = integer(name);
        return value == null ? fallback : value;
    }

    public Double number(String name) {
        Number number = value(name, Number.class);
        return number == null ? null : number.doubleValue();
    }

    public Boolean bool(String name) {
        return value(name, Boolean.class);
    }

    public boolean bool(String name, boolean fallback) {
        Boolean value = bool(name);
        return value == null ? fallback : value;
    }

    public Duration duration(String name) {
        return value(name, Duration.class);
    }

    /** The bounding box as a JTS envelope in 4326. */
    public Envelope bbox(String name) {
        return value(name, Envelope.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> stringList(String name) {
        Object value = values.get(name);
        return value == null ? List.of() : List.copyOf((List<String>) value);
    }

    /** A {@link ParamType#LAN_URL} parameter's URL. */
    public URI lanUrl(String name) {
        return value(name, URI.class);
    }

    /**
     * The address a {@link ParamType#LAN_URL} resolved to when it was validated.
     *
     * <p>The client must connect to this, not re-resolve the host, which is what makes DNS
     * rebinding ineffective.
     */
    public Optional<InetAddress> pinnedAddress(String name) {
        return Optional.ofNullable(pinnedAddresses.get(name));
    }

    /** An {@link ParamType#INBOX_PATH} parameter's validated path. */
    public Path inboxPath(String name) {
        return value(name, Path.class);
    }

    private <T> T value(String name, Class<T> type) {
        Object value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            // A validated param holding the wrong type means the validator and this accessor
            // disagree, which is a bug rather than bad configuration.
            throw new IllegalStateException(
                    "Param '"
                            + name
                            + "' is a "
                            + value.getClass().getSimpleName()
                            + ", not a "
                            + type.getSimpleName());
        }
        return type.cast(value);
    }
}
