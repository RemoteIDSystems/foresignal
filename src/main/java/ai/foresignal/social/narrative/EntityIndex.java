package ai.foresignal.social.narrative;

import java.util.List;

/**
 * Entity names and identifiers a claim can mention (prompt 136): 098 entities (vessels by IMO and MMSI, aircraft by
 * registration, multi-word names and aliases) and named 104 facilities (airports, ports, plants).
 */
public interface EntityIndex {

    /** An index that knows no entity. */
    EntityIndex EMPTY = new EntityIndex() {
        @Override
        public List<Hit> find(String folded) {
            return List.of();
        }

        @Override
        public List<String> byKey(String keyType, String value) {
            return List.of();
        }
    };

    /**
     * One entity name found in a text.
     *
     * @param start the start index in the folded text
     * @param end the end index (exclusive)
     * @param refs the object refs the name stands for ({@code entity:{id}} or a facility's {@code record:} ref)
     */
    record Hit(int start, int end, List<String> refs) {

        /** Copies the refs. */
        public Hit {
            refs = List.copyOf(refs);
        }
    }

    /**
     * Every entity name in a folded text, on word boundaries.
     *
     * @param folded the folded text
     */
    List<Hit> find(String folded);

    /**
     * The entities with a current, unretracted key.
     *
     * @param keyType {@code imo}, {@code mmsi}, {@code registration}, …
     * @param value the key value
     * @return {@code entity:{id}} refs
     */
    List<String> byKey(String keyType, String value);
}
