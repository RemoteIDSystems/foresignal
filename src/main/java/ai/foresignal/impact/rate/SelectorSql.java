package ai.foresignal.impact.rate;

import ai.foresignal.impact.ImpactPairs;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Renders an impact-pairs {@link ImpactPairs.Selector} as SQL over {@code record_version} (prompt 127). Pure.
 *
 * <p>Field names are checked against {@link ImpactPairs#PATH} when the pairs load and are never taken from a request;
 * every value is a bound parameter. {@code ==} compares the payload text, {@code in} the payload text against a list,
 * and {@code >=} the payload number (a non-numeric value never matches).
 */
public final class SelectorSql {

    private SelectorSql() {}

    /**
     * The predicate of a selector on the alias {@code alias}, starting with {@code AND}.
     *
     * @param alias the {@code record_version} alias
     * @param selector the selector
     * @param prefix the parameter-name prefix (unique per selector in one statement)
     * @param params receives the bound values
     */
    public static String where(String alias, ImpactPairs.Selector selector, String prefix,
            MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder();
        sql.append(" AND ").append(alias).append(".source_id IN (:").append(prefix).append("_sources)");
        params.addValue(prefix + "_sources", selector.sources());
        sql.append(" AND ").append(alias).append(".kind IN (:").append(prefix).append("_kinds)");
        params.addValue(prefix + "_kinds", selector.kinds());
        int i = 0;
        for (ImpactPairs.Condition c : selector.conditions()) {
            if (!ImpactPairs.PATH.matcher(c.path()).matches()) {
                throw new IllegalArgumentException("invalid payload path " + c.path());
            }
            String name = prefix + "_c" + i++;
            String field = alias + ".payload ->> '" + c.path() + "'";
            switch (c.op()) {
                case "==" -> {
                    sql.append(" AND ").append(field).append(" = :").append(name);
                    params.addValue(name, text(c.value()));
                }
                case "in" -> {
                    sql.append(" AND ").append(field).append(" IN (:").append(name).append(")");
                    params.addValue(name, c.value() instanceof List<?> list
                            ? list.stream().map(SelectorSql::text).toList() : List.of(text(c.value())));
                }
                case ">=" -> {
                    sql.append(" AND (CASE WHEN jsonb_typeof(").append(alias).append(".payload -> '").append(c.path())
                            .append("') = 'number' THEN (").append(field).append(")::double precision END) >= :")
                            .append(name);
                    params.addValue(name, ((Number) c.value()).doubleValue());
                }
                default -> throw new IllegalArgumentException("unsupported operator " + c.op());
            }
        }
        return sql.toString();
    }

    /** The payload text of a condition value: integral numbers without a fraction ({@code 34}, not {@code 34.0}). */
    static String text(Object value) {
        if (value instanceof Number n) {
            double d = n.doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
                return Long.toString((long) d);
            }
            return Double.toString(d);
        }
        return String.valueOf(value);
    }
}
