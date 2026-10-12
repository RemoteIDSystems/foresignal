package ai.foresignal.raster;

import ai.foresignal.source.spi.ProductSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The client-direct external catalog (prompt 045; Weather-Imagery-Design §5.1): GIBS WMTS and
 * nowCOAST WMS layers that map clients fetch <em>directly</em>, seeded from
 * {@code classpath:raster/external-catalog.json}.
 *
 * <p>Each entry becomes a {@code kind=external} product of the DERIVED source
 * {@value ExternalCatalogSource#SOURCE_ID} (listed by {@code /v1/products?kind=external}, with the
 * entry in the product's {@code external} member). Foresignal never fetches, proxies or renders these
 * layers: its tile and image routes answer {@code 400 VALIDATION_FAILED} ("external product: fetch
 * externalTemplate directly"), and nothing here holds an HTTP client.
 */
@Component
public class ExternalCatalog {

    /** Where the entries live. */
    public static final String LOCATION = "raster/external-catalog.json";

    /**
     * One entry.
     *
     * @param id the product id
     * @param title the title
     * @param category the product category
     * @param coverage the coverage value
     * @param document the {@code ExternalCatalogueEntry} member, as the API returns it
     */
    public record Entry(String id, String title, String category, String coverage, Map<String, Object> document) {}

    private final List<Entry> entries;
    private final Map<String, Entry> byId;

    /** Loads the shipped catalog. */
    public ExternalCatalog() {
        this(load());
    }

    /** A catalog over {@code entries} (tests). */
    public ExternalCatalog(List<Entry> entries) {
        this.entries = List.copyOf(entries);
        Map<String, Entry> map = new LinkedHashMap<>();
        for (Entry e : entries) {
            if (map.put(e.id(), e) != null) {
                throw new IllegalStateException("External catalog entry " + e.id() + " is listed twice");
            }
        }
        this.byId = Collections.unmodifiableMap(map);
    }

    /** Reads the catalog file. */
    @SuppressWarnings("unchecked")
    static List<Entry> load() {
        Map<String, Object> doc;
        try (InputStream in = new ClassPathResource(LOCATION).getInputStream()) {
            doc = JsonMapper.builder().build().readValue(in, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + LOCATION, e);
        }
        List<Entry> out = new ArrayList<>();
        for (Map<String, Object> raw : (List<Map<String, Object>>) doc.getOrDefault("entries", List.of())) {
            Map<String, Object> document = new LinkedHashMap<>();
            for (String key : List.of("provider", "layer", "protocol", "externalTemplate", "tilingScheme", "tileSize",
                    "maxNativeZoom", "format", "bounds", "timeDimension", "cors", "attribution")) {
                if (raw.containsKey(key)) {
                    document.put(key, raw.get(key));
                }
            }
            for (String required : List.of("externalTemplate", "tilingScheme", "tileSize", "maxNativeZoom", "bounds",
                    "attribution")) {
                if (document.get(required) == null) {
                    throw new IllegalStateException("External catalog entry " + raw.get("id") + " lacks " + required);
                }
            }
            out.add(new Entry((String) raw.get("id"), (String) raw.get("title"), (String) raw.get("category"),
                    (String) raw.get("coverage"), Collections.unmodifiableMap(document)));
        }
        return out;
    }

    /** Every entry, in file order. */
    public List<Entry> entries() {
        return entries;
    }

    /** The entry of product {@code id}. */
    public Optional<Entry> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** The products the entries define. */
    @SuppressWarnings("unchecked")
    public List<ProductSpec> specs() {
        List<ProductSpec> out = new ArrayList<>();
        for (Entry e : entries) {
            Map<String, Object> d = e.document();
            Map<String, Object> a = (Map<String, Object>) d.get("attribution");
            List<Double> bounds = ((List<Object>) d.get("bounds")).stream().map(v -> ((Number) v).doubleValue()).toList();
            ProductSpec.Builder b = ProductSpec.builder(e.id())
                    .title(e.title())
                    .kind(ProductSpec.KIND_EXTERNAL)
                    .category(e.category())
                    .spatial(new ProductSpec.Spatial(e.coverage(), ((Number) d.get("maxNativeZoom")).intValue(), 0, null,
                            bounds))
                    .attribution(new ProductSpec.Attribution((String) a.get("text"), (String) a.get("html"),
                            (String) a.get("url"), (String) a.get("licence"), (String) a.get("licenceUrl"),
                            Boolean.TRUE.equals(a.get("requiredDisplay"))));
            Duration period = period(d.get("timeDimension"));
            if (period != null) {
                b.temporal(ProductSpec.Temporal.observation(period, period));
            }
            out.add(b.build());
        }
        return out;
    }

    /** The time dimension's period as a duration, or null (none, or a calendar period such as P1Y). */
    @SuppressWarnings("unchecked")
    static Duration period(Object timeDimension) {
        if (!(timeDimension instanceof Map<?, ?> m) || !(m.get("period") instanceof String text)) {
            return null;
        }
        try {
            Duration d = Duration.parse(text);
            return d.isPositive() ? d : null;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
