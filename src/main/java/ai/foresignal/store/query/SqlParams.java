package ai.foresignal.store.query;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Builds named parameters for dynamically assembled SQL: each {@code add} binds a value under a
 * fresh name and returns the placeholder to splice into the statement, so values never enter SQL
 * text.
 */
public final class SqlParams {

    private final MapSqlParameterSource source = new MapSqlParameterSource();
    private int next;

    /** Binds {@code value} and returns its placeholder, for example {@code :p3}. */
    public String add(Object value) {
        String name = "p" + next++;
        source.addValue(name, value == null ? new SqlParameterValue(Types.VARCHAR, null) : value);
        return ":" + name;
    }

    /** Binds an instant as {@code timestamptz} and returns the cast placeholder. */
    public String instant(Instant value) {
        return "CAST(" + add(value == null ? null : OffsetDateTime.ofInstant(value, ZoneOffset.UTC)) + " AS timestamptz)";
    }

    /** Binds a {@code text[]} literal and returns the cast placeholder. */
    public String textArray(Collection<String> values) {
        return "CAST(" + add(PgArrays.text(values)) + " AS text[])";
    }

    /** Binds a {@code bigint[]} literal and returns the cast placeholder. */
    public String bigintArray(Collection<Long> values) {
        return "CAST(" + add(PgArrays.bigint(values)) + " AS bigint[])";
    }

    /** Binds a double and returns the cast placeholder. */
    public String dbl(double value) {
        return "CAST(" + add(value) + " AS double precision)";
    }

    /** Binds a long and returns the cast placeholder. */
    public String bigint(long value) {
        return "CAST(" + add(value) + " AS bigint)";
    }

    /** Binds a text value and returns the cast placeholder. */
    public String text(String value) {
        return "CAST(" + add(value) + " AS text)";
    }

    /** The parameters bound so far. */
    public MapSqlParameterSource source() {
        return source;
    }
}
