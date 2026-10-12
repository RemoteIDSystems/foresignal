package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A request without its {@code ticket} query parameter (prompt 023), so the ticket never reaches a
 * controller, a log line or a URL echoed in a response (TileJSON, problem documents).
 */
final class TicketStrippingRequest extends HttpServletRequestWrapper {

    static final String PARAMETER = "ticket";

    TicketStrippingRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return PARAMETER.equals(name) ? null : super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> out = new LinkedHashMap<>(super.getParameterMap());
        out.remove(PARAMETER);
        return Collections.unmodifiableMap(out);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return PARAMETER.equals(name) ? null : super.getParameterValues(name);
    }

    @Override
    public String getQueryString() {
        return strip(super.getQueryString());
    }

    /** {@code query} without any {@code ticket=} pair; null when nothing is left. */
    static String strip(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }
        StringJoiner out = new StringJoiner("&");
        for (String pair : query.split("&")) {
            String name = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
            if (!PARAMETER.equals(name)) {
                out.add(pair);
            }
        }
        String result = out.toString();
        return result.isEmpty() ? null : result;
    }

    /** The decoded value of the first {@code ticket=} pair in {@code query}, or null. */
    static String ticket(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String pair : query.split("&")) {
            if (pair.startsWith(PARAMETER + "=")) {
                try {
                    String value = java.net.URLDecoder.decode(pair.substring(PARAMETER.length() + 1),
                            java.nio.charset.StandardCharsets.UTF_8);
                    return value.isBlank() ? null : value.strip();
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
