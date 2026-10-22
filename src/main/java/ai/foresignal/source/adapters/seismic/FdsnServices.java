package ai.foresignal.source.adapters.seismic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The committed, reviewed FDSN event-service list (prompt 089), {@value #RESOURCE}.
 *
 * <p>Every service was verified live on 2026-10-14. IRIS is excluded (its event service was retired in favor of
 * USGS). The list is code-reviewed and static; no caller can add a service.
 */
public final class FdsnServices {

    /** The classpath resource of the reviewed list. */
    public static final String RESOURCE = "/sources/fdsn-event-services.json";

    private static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FdsnServices() {}

    /**
     * Loads the reviewed services.
     *
     * @throws IllegalStateException when the resource is missing or malformed
     */
    public static List<FdsnService> load() {
        try (InputStream in = FdsnServices.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The FDSN service list " + RESOURCE + " is missing");
            }
            return parse(JSON.readTree(in));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    static List<FdsnService> parse(JsonNode root) {
        JsonNode services = root.get("services");
        if (services == null || !services.isArray()) {
            throw new IllegalStateException("The FDSN service list has no services array");
        }
        List<FdsnService> out = new ArrayList<>();
        for (JsonNode row : services) {
            out.add(new FdsnService(text(row, "id"), text(row, "name"), uri(row, "baseUrl"),
                    row.path("supportsUpdatedAfter").asBoolean(false), text(row, "licence"),
                    uri(row, "termsUrl"), text(row, "attribution"), uri(row, "homepage"), text(row, "notes")));
        }
        return out;
    }

    /** The problems of a service list. */
    public static List<String> problems(List<FdsnService> services) {
        List<String> out = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (FdsnService s : services) {
            String id = s.sourceId();
            if (s.id() == null || !ID.matcher(s.id()).matches()) {
                out.add(id + ": the id must be kebab-case");
            } else if (!ids.add(s.id())) {
                out.add(id + ": duplicate id");
            }
            if (s.name() == null || s.name().isBlank()) {
                out.add(id + ": name is required");
            }
            if (s.baseUrl() == null || !"https".equalsIgnoreCase(s.baseUrl().getScheme())) {
                out.add(id + ": baseUrl must be HTTPS");
            }
            if (s.licence() == null || s.licence().isBlank()) {
                out.add(id + ": licence is required");
            }
            if (s.termsUrl() == null) {
                out.add(id + ": termsUrl is required");
            }
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static URI uri(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("The FDSN service list has a bad " + field + ": " + value, e);
        }
    }
}
