package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.formats.csv.CsvReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The vendored Mobility Database GTFS-RT snapshot (prompt 070): {@code src/main/resources/transit/mobility-gtfs-rt.csv},
 * generated at development time by {@code scripts/update-mobility-catalog.py} from MobilityData's CC0
 * {@code feeds_v2.csv} (active, keyless, https GTFS-RT feeds only; the snapshot date is in the sibling README).
 *
 * <p>Read once at startup and never refreshed at runtime: the snapshot is the reviewed list of URLs an operator may
 * enable by id ({@code foresignal.transit.catalog-feeds}). Rows without an id, a provider or an https URL are
 * skipped; a repeated id keeps its first row.
 */
@Component
public class MobilityCatalog {

    private static final Logger log = LoggerFactory.getLogger(MobilityCatalog.class);

    /** The classpath location of the vendored snapshot. */
    public static final String DEFAULT_RESOURCE = "transit/mobility-gtfs-rt.csv";

    /** Test-only override of {@link #DEFAULT_RESOURCE} (a classpath location). */
    public static final String RESOURCE_PROPERTY = "foresignal.test.transit.catalog-resource";

    /** The GTFS-RT entity types: vehicle positions, trip updates, service alerts. */
    public static final List<String> ENTITY_TYPES = List.of("vp", "tu", "sa");

    /**
     * One snapshot row.
     *
     * @param id the Mobility Database id, for example {@code mdb-1602}
     * @param provider the operating agency as the catalog names it
     * @param countryCode ISO 3166-1 alpha-2, upper case, or null
     * @param entityTypes the GTFS-RT entity types the URL serves ({@code vp, tu, sa}), possibly empty
     * @param directDownloadUrl the feed URL (https)
     * @param licenseUrl the feed's license URL, or null when the catalog does not know it
     * @param staticReference the id of the matching static GTFS feed, or null
     */
    public record Entry(String id, String provider, String countryCode, Set<String> entityTypes, URI directDownloadUrl,
            String licenseUrl, String staticReference) {

        /** Copies the entity types. */
        public Entry {
            entityTypes = Collections.unmodifiableSet(new LinkedHashSet<>(entityTypes));
        }

        /** The entity types joined with {@code |}, as in the catalog. */
        public String entityType() {
            return String.join("|", entityTypes);
        }
    }

    private final Map<String, Entry> entries;

    /**
     * Loads the snapshot.
     *
     * @param resourceOverride the value of {@value #RESOURCE_PROPERTY}; blank means {@value #DEFAULT_RESOURCE}
     * @throws IllegalStateException when the resource is missing or lacks a required column
     */
    public MobilityCatalog(@Value("${" + RESOURCE_PROPERTY + ":}") String resourceOverride) {
        String resource = resourceOverride == null || resourceOverride.isBlank() ? DEFAULT_RESOURCE
                : resourceOverride.strip();
        this.entries = load(resource);
        log.debug("Mobility Database snapshot {}: {} GTFS-RT feeds", resource, entries.size());
    }

    /** Every row, by id. */
    public List<Entry> all() {
        return List.copyOf(entries.values());
    }

    /** The row with {@code id}, if the snapshot has one. */
    public Optional<Entry> get(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entries.get(id.strip()));
    }

    /**
     * The rows matching every given filter, by id.
     *
     * @param country an ISO 3166-1 alpha-2 code (case-insensitive), or null for any
     * @param query a case-insensitive substring of the provider or the id, or null for any
     * @param entityType {@code vp}, {@code tu} or {@code sa}, or null for any
     */
    public List<Entry> search(String country, String query, String entityType) {
        String c = blank(country) ? null : country.strip().toUpperCase(Locale.ROOT);
        String q = blank(query) ? null : query.strip().toLowerCase(Locale.ROOT);
        String t = blank(entityType) ? null : entityType.strip().toLowerCase(Locale.ROOT);
        List<Entry> out = new ArrayList<>();
        for (Entry e : entries.values()) {
            if (c != null && !c.equals(e.countryCode())) {
                continue;
            }
            if (t != null && !e.entityTypes().contains(t)) {
                continue;
            }
            if (q != null && !e.id().toLowerCase(Locale.ROOT).contains(q)
                    && !e.provider().toLowerCase(Locale.ROOT).contains(q)) {
                continue;
            }
            out.add(e);
        }
        return out;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static Map<String, Entry> load(String resource) {
        String path = resource.startsWith("/") ? resource.substring(1) : resource;
        InputStream in = MobilityCatalog.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Foresignal cannot start: the Mobility Database snapshot " + path
                    + " is missing from the classpath");
        }
        Map<String, Entry> out = new java.util.TreeMap<>();
        try (CsvReader csv = new CsvReader(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))) {
            List<String> header = csv.next();
            if (header == null) {
                return Map.of();
            }
            Map<String, Integer> index = CsvReader.index(header);
            for (String column : List.of("ID", "PROVIDER", "URLS.DIRECT_DOWNLOAD", "ENTITY_TYPE")) {
                if (!index.containsKey(column)) {
                    throw new IllegalStateException("Foresignal cannot start: the Mobility Database snapshot " + path
                            + " lacks the column " + column.toLowerCase(Locale.ROOT));
                }
            }
            List<String> row;
            while ((row = csv.next()) != null) {
                Entry entry = entry(row, index);
                if (entry != null) {
                    out.putIfAbsent(entry.id(), entry);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the Mobility Database snapshot " + path, e);
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(out));
    }

    /** One row as an entry, or null when it lacks an id, a provider or an https URL. */
    static Entry entry(List<String> row, Map<String, Integer> index) {
        String id = CsvReader.get(row, index, "ID");
        String provider = CsvReader.get(row, index, "PROVIDER");
        String url = CsvReader.get(row, index, "URLS.DIRECT_DOWNLOAD");
        if (id == null || provider == null || url == null || !url.toLowerCase(Locale.ROOT).startsWith("https://")) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String country = CsvReader.get(row, index, "LOCATION.COUNTRY_CODE");
        Set<String> types = new LinkedHashSet<>();
        String rawTypes = CsvReader.get(row, index, "ENTITY_TYPE");
        if (rawTypes != null) {
            for (String type : rawTypes.split("\\|")) {
                String t = type.strip().toLowerCase(Locale.ROOT);
                if (ENTITY_TYPES.contains(t)) {
                    types.add(t);
                }
            }
        }
        return new Entry(id, provider,
                country == null || !country.matches("^[A-Za-z]{2}$") ? null : country.toUpperCase(Locale.ROOT), types,
                uri, CsvReader.get(row, index, "URLS.LICENSE"), CsvReader.get(row, index, "STATIC_REFERENCE"));
    }
}
