package ai.foresignal.cli.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An ordered list of query parameters. Null and blank values are skipped; repeated parameters (arrays such as
 * {@code source=a&source=b}) are added one value at a time.
 */
public final class Query {

    private final List<Map.Entry<String, String>> params = new ArrayList<>();

    /**
     * An empty query.
     *
     * @return a new query
     */
    public static Query of() {
        return new Query();
    }

    /**
     * Adds one parameter unless its value is null or blank.
     *
     * @param name  the parameter name
     * @param value the value; {@link Object#toString()} is used
     * @return this query
     */
    public Query add(String name, Object value) {
        if (value != null && !value.toString().isBlank()) {
            params.add(Map.entry(name, value.toString()));
        }
        return this;
    }

    /**
     * Adds one parameter per value.
     *
     * @param name   the parameter name
     * @param values the values (may be null)
     * @return this query
     */
    public Query addAll(String name, Collection<?> values) {
        if (values != null) {
            for (Object value : values) {
                add(name, value);
            }
        }
        return this;
    }

    /**
     * Adds a boolean flag only when it is true.
     *
     * @param name the parameter name
     * @param flag the flag
     * @return this query
     */
    public Query flag(String name, boolean flag) {
        if (flag) {
            params.add(Map.entry(name, "true"));
        }
        return this;
    }

    /**
     * Replaces every value of a parameter.
     *
     * @param name  the parameter name
     * @param value the new value, or null to remove it
     * @return this query
     */
    public Query set(String name, Object value) {
        params.removeIf(e -> e.getKey().equals(name));
        return add(name, value);
    }

    /**
     * A copy of this query.
     *
     * @return a new query with the same parameters
     */
    public Query copy() {
        Query copy = new Query();
        copy.params.addAll(params);
        return copy;
    }

    /**
     * Whether the query has no parameters.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return params.isEmpty();
    }

    /**
     * The encoded query string without the leading {@code ?}.
     *
     * @return e.g. {@code source=a&kind=b%20c}
     */
    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(component(e.getKey())).append('=').append(component(e.getValue()));
        }
        return sb.toString();
    }

    /**
     * Percent-encodes one URL component (query value or path segment); spaces become {@code %20}.
     *
     * @param value the raw value
     * @return the encoded value
     */
    public static String component(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
