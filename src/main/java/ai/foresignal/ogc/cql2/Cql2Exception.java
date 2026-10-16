package ai.foresignal.ogc.cql2;

/**
 * A CQL2 filter that cannot be parsed, exceeds the limits or names an unknown property. The API maps
 * it to {@code 400 FILTER_INVALID} with the position in the detail.
 */
public class Cql2Exception extends RuntimeException {

    private final String position;

    /**
     * Creates the exception.
     *
     * @param message what is wrong
     * @param position where: a character offset ({@code "12"}) in CQL2 text, a JSON pointer in CQL2 JSON,
     *     or null when the whole filter is at fault
     */
    public Cql2Exception(String message, String position) {
        super(position == null ? message : message + " (at " + position + ")");
        this.position = position;
    }

    /** The position, or null. */
    public String position() {
        return position;
    }
}
