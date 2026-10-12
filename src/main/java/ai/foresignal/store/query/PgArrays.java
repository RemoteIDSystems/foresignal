package ai.foresignal.store.query;

import java.util.Collection;
import java.util.Iterator;

/**
 * PostgreSQL array literals, bound as one string parameter and cast in SQL
 * ({@code col = ANY(CAST(:p AS text[]))}).
 *
 * <p>The literal is a bound value, never SQL text, so it cannot inject; quoting only has to satisfy
 * the array input syntax: every element is double-quoted with {@code "} and {@code \} escaped.
 */
public final class PgArrays {

    private PgArrays() {}

    /** A {@code text[]} literal of {@code values}. */
    public static String text(Collection<String> values) {
        StringBuilder out = new StringBuilder("{");
        Iterator<String> it = values.iterator();
        while (it.hasNext()) {
            String value = it.next();
            out.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '"' || c == '\\') {
                    out.append('\\');
                }
                out.append(c);
            }
            out.append('"');
            if (it.hasNext()) {
                out.append(',');
            }
        }
        return out.append('}').toString();
    }

    /** A {@code bigint[]} literal of {@code values}. */
    public static String bigint(Collection<Long> values) {
        StringBuilder out = new StringBuilder("{");
        Iterator<Long> it = values.iterator();
        while (it.hasNext()) {
            out.append(it.next().longValue());
            if (it.hasNext()) {
                out.append(',');
            }
        }
        return out.append('}').toString();
    }
}
