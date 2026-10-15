package ai.foresignal.derive;

/**
 * A derived generator whose in-memory state has a size worth watching (prompt 099): the runtime
 * reads it after each invocation and publishes it as
 * {@code foresignal.derive.state.size{generator}}.
 */
public interface StatefulGenerator {

    /** The number of entities currently held in the generator's state. */
    int stateSize();
}
