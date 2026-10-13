package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.KitUrls;
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

/**
 * The vendored, reviewed snapshot of the USDOT WZDx Feed Registry (prompt 074), {@value #RESOURCE}.
 *
 * <p>The snapshot was taken from {@code data.transportation.gov} dataset {@code 69qe-yiui} and keeps only rows with
 * {@code active=true}, {@code format=geojson}, version 4.x and a URL that does not embed a token. A key-required row
 * is kept only when the registry URL shows the key's query parameter; the parameter is removed from the vendored URL
 * and recorded as the feed's {@link WzdxFeed.ApiKey}. Rows left out are listed under {@code excluded} with the
 * reason. <strong>The registry is never read at runtime</strong>, and callers cannot add feeds; re-vendoring is a
 * reviewed change (Source Catalog).
 */
public final class WzdxRegistry {

    /** The classpath resource of the snapshot. */
    public static final String RESOURCE = "/roads/wzdx-feeds.json";

    private static final Pattern ID = Pattern.compile("^wzdx-[a-z0-9]+(-[a-z0-9]+)*$");

    private WzdxRegistry() {}

    /**
     * Loads the snapshot's feeds.
     *
     * @throws IllegalStateException when the resource is missing or malformed
     */
    public static List<WzdxFeed> load() {
        try (InputStream in = WzdxRegistry.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The WZDx registry snapshot " + RESOURCE + " is missing");
            }
            return parse(RoadEvents.JSON.readTree(in));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /** The snapshot date recorded in the resource. */
    public static String snapshotDate() {
        try (InputStream in = WzdxRegistry.class.getResourceAsStream(RESOURCE)) {
            return in == null ? null : RoadEvents.text(RoadEvents.JSON.readTree(in), "snapshotDate");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /**
     * The feeds of a snapshot document.
     *
     * @throws IllegalStateException when a row is malformed
     */
    static List<WzdxFeed> parse(JsonNode root) {
        JsonNode feeds = root.get("feeds");
        if (feeds == null || !feeds.isArray()) {
            throw new IllegalStateException("The WZDx registry snapshot has no feeds array");
        }
        List<WzdxFeed> out = new ArrayList<>();
        for (JsonNode row : feeds) {
            URI url;
            try {
                url = new URI(String.valueOf(RoadEvents.text(row, "url")));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("WZDx feed " + RoadEvents.text(row, "feedname") + " has a bad URL", e);
            }
            JsonNode key = row.get("apiKey");
            WzdxFeed.ApiKey apiKey = key == null || key.isNull() ? null
                    : new WzdxFeed.ApiKey(RoadEvents.text(key, "placement"), RoadEvents.text(key, "name"));
            out.add(new WzdxFeed(RoadEvents.text(row, "feedname"), RoadEvents.text(row, "issuingorganization"),
                    RoadEvents.text(row, "state"), RoadEvents.text(row, "version"),
                    RoadEvents.text(row, "datafeed_frequency_update"), url, apiKey,
                    RoadEvents.text(row, "apikeyurl")));
        }
        return out;
    }

    /**
     * The problems of a feed list: unique ids, version 4.x, https URLs without credentials (the kit URL rules), and a
     * documented placement for every key.
     *
     * @return the problems; empty when the list is valid
     */
    public static List<String> problems(List<WzdxFeed> feeds) {
        List<String> out = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (WzdxFeed f : feeds) {
            String id = f.id();
            if (!ID.matcher(id).matches()) {
                out.add(id + ": the id must match " + ID.pattern());
            } else if (!ids.add(id)) {
                out.add(id + ": duplicate id");
            }
            if (f.version() == null || !f.version().startsWith("4")) {
                out.add(id + ": version " + f.version() + " is not 4.x");
            }
            out.addAll(KitUrls.problems(id + ".url", f.url() == null ? null : f.url().toString(), true));
            if (f.issuingOrganization() == null || f.issuingOrganization().isBlank()) {
                out.add(id + ": issuingorganization is required");
            }
            if (f.apiKey() != null) {
                String placement = f.apiKey().placement();
                if (!"query".equals(placement) && !"header".equals(placement)) {
                    out.add(id + ": apiKey.placement must be query or header");
                }
                if (f.apiKey().name() == null || f.apiKey().name().isBlank()) {
                    out.add(id + ": apiKey.name is required");
                }
            }
            if (f.frequency() == null) {
                out.add(id + ": datafeed_frequency_update '" + f.updateFrequency() + "' is unreadable");
            }
        }
        return out;
    }
}
