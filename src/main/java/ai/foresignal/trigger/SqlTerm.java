package ai.foresignal.trigger;

/**
 * The SQL an {@link Operator} builds a condition from: expressions for one payload path of one table
 * alias, and a way to bind values. Paths and values are always bound parameters; nothing a client
 * sent is ever spliced into SQL text.
 */
public interface SqlTerm {

    /** {@code a.payload #> CAST(:cNp AS text[])}: the JSON value at the path, or SQL NULL. */
    String json();

    /** {@code a.payload #>> CAST(:cNp AS text[])}: the value as text. */
    String text();

    /** {@code jsonb_typeof(…)} of {@link #json()}. */
    String typeof();

    /** The value as {@code numeric} when its JSON type is number, else NULL. */
    default String number() {
        return "(CASE WHEN " + typeof() + " = 'number' THEN (" + text() + ")::numeric END)";
    }

    /** The value as {@code text} when its JSON type is string, else NULL. */
    default String string() {
        return "(CASE WHEN " + typeof() + " = 'string' THEN " + text() + " END)";
    }

    /** The value as {@code boolean} when its JSON type is boolean, else NULL. */
    default String bool() {
        return "(CASE WHEN " + typeof() + " = 'boolean' THEN (" + text() + ")::boolean END)";
    }

    /**
     * Binds {@code value} and returns {@code CAST(:name AS sqlType)}. The same {@code suffix} always
     * gives the same parameter, so a predicate rendered for two aliases binds each value once.
     *
     * @param suffix distinguishes several values of one condition ({@code "v"}, {@code "len"})
     * @param value the JDBC value
     * @param sqlType the SQL type to cast to, for example {@code numeric} or {@code text[]}
     */
    String bind(String suffix, Object value, String sqlType);
}
