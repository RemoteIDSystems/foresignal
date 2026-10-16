package ai.foresignal.source.adapters.cap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The committed, human-reviewed CAP feed list (prompt 089), {@value #RESOURCE}.
 *
 * <p>Seeded offline from the WMO Register of Alerting Authorities by {@code scripts/build-cap-register.py},
 * then reviewed by hand (keyless, documented, HTTPS, re-usable terms, not already covered). <strong>The
 * register is never read at runtime</strong>, and callers cannot add feeds; adding one is a reviewed change
 * (Source Catalog).
 */
public final class CapFeeds {

    /** The classpath resource of the reviewed list. */
    public static final String RESOURCE = "/sources/cap-feeds.json";

    /** The accepted index formats. */
    public static final Set<String> FORMATS = Set.of("atom-index", "rss-index", "cap-inline");

    private static final Pattern ID = Pattern.compile("^cap-[a-z0-9]+(-[a-z0-9]+)*$");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CapFeeds() {}

    /**
     * Loads the reviewed feeds.
     *
     * @throws IllegalStateException when the resource is missing or malformed
     */
    public static List<CapFeed> load() {
        try (InputStream in = CapFeeds.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The CAP feed list " + RESOURCE + " is missing");
            }
            return parse(JSON.readTree(in));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /** The reviewed date recorded in the resource. */
    public static String reviewedOn() {
        try (InputStream in = CapFeeds.class.getResourceAsStream(RESOURCE)) {
            return in == null ? null : text(JSON.readTree(in), "reviewedOn");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /**
     * The feeds of a reviewed document.
     *
     * @throws IllegalStateException when a row is malformed
     */
    static List<CapFeed> parse(JsonNode root) {
        JsonNode feeds = root.get("feeds");
        if (feeds == null || !feeds.isArray()) {
            throw new IllegalStateException("The CAP feed list has no feeds array");
        }
        List<CapFeed> out = new ArrayList<>();
        for (JsonNode row : feeds) {
            List<String> languages = new ArrayList<>();
            if (row.get("languages") != null && row.get("languages").isArray()) {
                row.get("languages").forEach(l -> languages.add(l.asString()));
            }
            List<String> documentHosts = new ArrayList<>();
            if (row.get("documentHosts") != null && row.get("documentHosts").isArray()) {
                row.get("documentHosts").forEach(h -> documentHosts.add(h.asString()));
            }
            out.add(new CapFeed(text(row, "id"), text(row, "country"), text(row, "authority"),
                    uri(row, "feedUrl"), text(row, "format"), languages, text(row, "licence"),
                    uri(row, "termsUrl"), text(row, "reviewedOn"), text(row, "notes"), documentHosts));
        }
        return out;
    }

    /**
     * The problems of a feed list.
     *
     * @return the problems; empty when the list is valid
     */
    public static List<String> problems(List<CapFeed> feeds) {
        List<String> out = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> authorities = new HashSet<>();
        for (CapFeed f : feeds) {
            String id = f.id() == null ? "(null)" : f.id();
            if (f.id() == null || !ID.matcher(f.id()).matches()) {
                out.add(id + ": the id must match " + ID.pattern());
            } else if (!ids.add(f.id())) {
                out.add(id + ": duplicate id");
            }
            if (f.country() == null || !f.country().matches("[A-Z]{2}")) {
                out.add(id + ": country must be an ISO 3166-1 alpha-2 code");
            } else if (f.id() != null && !f.id().startsWith("cap-" + f.country().toLowerCase(Locale.ROOT) + "-")) {
                out.add(id + ": the id must be cap-<iso2>-<authority> for its country");
            }
            if (f.country() != null && f.authority() != null
                    && !authorities.add(f.country() + "|" + f.authority().strip().toLowerCase(Locale.ROOT))) {
                out.add(id + ": the authority already has a source (one source per authority; pick the info"
                        + " language with languages)");
            }
            for (String host : f.documentHosts()) {
                if (!host.matches("[a-z0-9.-]+")) {
                    out.add(id + ": documentHosts entry '" + host + "' is not a host name");
                }
            }
            if (f.authority() == null || f.authority().isBlank()) {
                out.add(id + ": authority is required");
            }
            if (f.feedUrl() == null || !"https".equalsIgnoreCase(f.feedUrl().getScheme())) {
                out.add(id + ": feedUrl must be HTTPS");
            }
            if (f.format() == null || !FORMATS.contains(f.format())) {
                out.add(id + ": format must be one of " + FORMATS);
            }
            if (f.languages().isEmpty()) {
                out.add(id + ": languages must name at least one language");
            }
            if (f.licence() == null || f.licence().isBlank()) {
                out.add(id + ": licence is required");
            }
            if (f.termsUrl() == null) {
                out.add(id + ": termsUrl is required");
            }
            if (f.reviewedOn() == null || f.reviewedOn().isBlank()) {
                out.add(id + ": reviewedOn is required");
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
            throw new IllegalStateException("The CAP feed list has a bad " + field + ": " + value, e);
        }
    }
}
