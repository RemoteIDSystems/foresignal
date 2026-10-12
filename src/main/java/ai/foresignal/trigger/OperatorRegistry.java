package ai.foresignal.trigger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The fixed set of condition operators (prompt 017): the built-ins of {@link Operators} plus every
 * {@link Operator} bean, so prompt 036 adds {@code inWatchlist} by declaring a bean and nothing in
 * the engine, the SQL builder or the gate changes.
 */
@Component
public class OperatorRegistry {

    private final Map<String, Operator> operators;

    /** The built-ins and every operator bean; a bean may not reuse a built-in name. */
    @Autowired
    public OperatorRegistry(ObjectProvider<Operator> extra) {
        this(extra.orderedStream().toList());
    }

    /**
     * The built-ins plus {@code extra}.
     *
     * @throws IllegalStateException when two operators share a name
     */
    public OperatorRegistry(List<Operator> extra) {
        Map<String, Operator> map = new LinkedHashMap<>();
        for (Operator op : Operators.builtIns()) {
            map.put(op.name(), op);
        }
        for (Operator op : extra) {
            if (map.putIfAbsent(op.name(), op) != null) {
                throw new IllegalStateException("Two condition operators are named " + op.name());
            }
        }
        this.operators = Map.copyOf(map);
        this.names = List.copyOf(map.keySet());
    }

    private final List<String> names;

    /** A registry with only the built-ins, for tests and tools. */
    public static OperatorRegistry builtIn() {
        return new OperatorRegistry(List.of());
    }

    /** The operator named {@code name}, if registered. */
    public Optional<Operator> find(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(operators.get(name));
    }

    /** Every operator name, built-ins first. */
    public Collection<String> names() {
        return names;
    }
}
