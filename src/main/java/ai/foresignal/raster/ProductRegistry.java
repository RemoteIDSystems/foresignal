package ai.foresignal.raster;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The product catalog (prompt 042, Weather-Imagery-Design §2.3).
 *
 * <p>At startup it collects the {@link ProductSpec}s of every registered descriptor and validates
 * them all at once; any problem stops startup, with every problem in one message, exactly as
 * {@link SourceRegistry} does for descriptors. The rules:
 *
 * <ul>
 *   <li>the id matches {@value #ID_REGEX}, is not reserved ({@code timeline}, {@code loop}) and is
 *       unique across sources;
 *   <li>the owning source lists {@code raster-frame} in its kinds;
 *   <li>kind and category are known; a non-external product declares a dtype, a temporal type and a
 *       positive cadence, and {@code maxGap ≥ cadence};
 *   <li>ladder rungs ascend by {@code olderThan} with non-decreasing {@code keepEvery}, and
 *       {@code maxAge} lies beyond the last rung;
 *   <li>zooms are non-negative and {@code minZoom ≤ nativeMaxZoom}; the default style is one of the
 *       styles; the attribution has a text and a {@code licence}.
 * </ul>
 *
 * <p>The in-memory catalog is what the API reads; {@code raster_product} is its durable mirror,
 * upserted once the application is ready and on every change. {@linkplain #registerDynamic Dynamic
 * products} (SatDump, prompt 049) join at run time under the same rules. Every change publishes a
 * {@link ProductCatalogChangedEvent}.
 */
@Component
public class ProductRegistry implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ProductRegistry.class);

    /** The product id pattern (the spec's {@code productId} parameter). */
    public static final String ID_REGEX = "^[a-z0-9][a-z0-9-]{0,63}$";

    static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);

    /**
     * A product in the catalog.
     *
     * @param spec the spec
     * @param sourceId the owning source
     * @param dynamic true when registered at run time
     */
    public record Registered(ProductSpec spec, String sourceId, boolean dynamic) {

        /** The product id. */
        public String id() {
            return spec.id();
        }
    }

    private final Map<String, Registered> products = new ConcurrentHashMap<>();
    private final SourceRegistry sources;
    private final RasterProductRepository repository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Builds the catalog from every registered descriptor.
     *
     * @throws IllegalStateException listing every problem when any spec is invalid
     */
    public ProductRegistry(SourceRegistry sources, RasterProductRepository repository,
            ApplicationEventPublisher events, Clock clock) {
        this.sources = sources;
        this.repository = repository;
        this.events = events;
        this.clock = clock;
        List<Registered> declared = new ArrayList<>();
        for (SourceDescriptor descriptor : sources.descriptors()) {
            for (ProductSpec spec : descriptor.products()) {
                declared.add(new Registered(spec, descriptor.id(), false));
            }
        }
        List<String> problems = problems(declared, sources::find);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: " + problems.size()
                    + " product spec problem(s).\n  - " + String.join("\n  - ", problems));
        }
        declared.forEach(p -> products.put(p.id(), p));
        if (!products.isEmpty()) {
            log.info("Registered {} raster product(s)", products.size());
        }
    }

    /**
     * Every problem with {@code candidates}, checked against each other and their sources.
     *
     * @param lookup finds a source's registration by id
     */
    static List<String> problems(List<Registered> candidates,
            java.util.function.Function<String, Optional<SourceRegistry.Registration>> lookup) {
        List<String> problems = new ArrayList<>();
        Map<String, String> owners = new java.util.HashMap<>();
        for (Registered candidate : candidates) {
            String previous = owners.putIfAbsent(candidate.id(), candidate.sourceId());
            if (previous != null) {
                problems.add("Duplicate product id '" + candidate.id() + "' is declared by both " + previous + " and "
                        + candidate.sourceId());
            }
            Optional<SourceDescriptor> descriptor = lookup.apply(candidate.sourceId()).map(r -> r.descriptor());
            problems.addAll(problems(candidate.spec(), candidate.sourceId(), descriptor.orElse(null)));
        }
        return problems;
    }

    /** Every problem with one spec owned by {@code sourceId} ({@code descriptor} null when unknown). */
    static List<String> problems(ProductSpec spec, String sourceId, SourceDescriptor descriptor) {
        List<String> out = new ArrayList<>();
        String p = "product '" + spec.id() + "' (" + sourceId + ")";
        if (!ID_PATTERN.matcher(spec.id()).matches()) {
            out.add(p + ": the id must match " + ID_REGEX);
        }
        if (ProductSpec.RESERVED_IDS.contains(spec.id())) {
            out.add(p + ": the id is reserved");
        }
        if (descriptor == null) {
            out.add(p + ": the source is not registered");
        } else if (!descriptor.kinds().contains(ProductSpec.RECORD_KIND)) {
            out.add(p + ": the source must list '" + ProductSpec.RECORD_KIND + "' in its kinds");
        }
        if (spec.kind() == null || !ProductSpec.KINDS.contains(spec.kind())) {
            out.add(p + ": kind must be one of " + ProductSpec.KINDS + " (the product kind, not the record kind)");
        }
        if (spec.category() == null || !ProductSpec.CATEGORIES.contains(spec.category())) {
            out.add(p + ": category must be one of " + ProductSpec.CATEGORIES);
        }
        if (spec.attribution() == null || blank(spec.attribution().text()) || blank(spec.attribution().licence())) {
            out.add(p + ": an attribution with a text and a licence field is required");
        }
        if (!spec.isExternal()) {
            if (spec.dtype() == null) {
                out.add(p + ": a dtype is required");
            }
            temporalProblems(spec, p, out);
            spatialProblems(spec, p, out);
            ladderProblems(spec, p, out);
        }
        if (spec.defaultStyle() != null && !spec.styles().isEmpty() && !spec.styles().contains(spec.defaultStyle())) {
            out.add(p + ": the default style must be one of its styles");
        }
        if (!ProductSpec.RESAMPLES.contains(spec.resample())) {
            out.add(p + ": resample must be one of " + ProductSpec.RESAMPLES);
        }
        if (spec.defaultOpacity() != null && (spec.defaultOpacity() < 0 || spec.defaultOpacity() > 1)) {
            out.add(p + ": defaultOpacity must be between 0 and 1");
        }
        return out;
    }

    private static void temporalProblems(ProductSpec spec, String p, List<String> out) {
        ProductSpec.Temporal t = spec.temporal();
        if (t == null) {
            out.add(p + ": temporal facts are required");
            return;
        }
        if (t.type() == null || !ProductSpec.Temporal.TYPES.contains(t.type())) {
            out.add(p + ": temporal.type must be one of " + ProductSpec.Temporal.TYPES);
        }
        if (t.cadence() == null || !t.cadence().isPositive()) {
            out.add(p + ": temporal.cadence must be positive");
        } else if (t.maxGap() != null && t.maxGap().compareTo(t.cadence()) < 0) {
            out.add(p + ": temporal.maxGap must be at least the cadence");
        }
    }

    private static void spatialProblems(ProductSpec spec, String p, List<String> out) {
        ProductSpec.Spatial s = spec.spatial();
        if (s == null) {
            out.add(p + ": spatial facts are required");
            return;
        }
        if (s.coverage() == null || !ProductSpec.Spatial.COVERAGES.contains(s.coverage())) {
            out.add(p + ": spatial.coverage must be one of " + ProductSpec.Spatial.COVERAGES);
        }
        if (s.nativeMaxZoom() == null || s.nativeMaxZoom() < 0 || s.minZoom() == null || s.minZoom() < 0) {
            out.add(p + ": spatial.nativeMaxZoom and minZoom are required and non-negative");
        } else if (s.minZoom() > s.nativeMaxZoom()) {
            out.add(p + ": spatial.minZoom must not exceed nativeMaxZoom");
        }
        if (s.bounds() != null && s.bounds().size() != 4) {
            out.add(p + ": spatial.bounds must be [west, south, east, north]");
        }
    }

    private static void ladderProblems(ProductSpec spec, String p, List<String> out) {
        ProductSpec.RetentionLadder ladder = spec.retention();
        if (ladder == null) {
            return;
        }
        Duration previousAge = Duration.ZERO;
        Duration previousEvery = Duration.ZERO;
        for (ProductSpec.RetentionLadder.Rung rung : ladder.rungs()) {
            if (rung.olderThan() == null || rung.keepEvery() == null || !rung.olderThan().isPositive()
                    || !rung.keepEvery().isPositive()) {
                out.add(p + ": every ladder rung needs a positive olderThan and keepEvery");
                return;
            }
            if (rung.olderThan().compareTo(previousAge) <= 0) {
                out.add(p + ": ladder rungs must ascend by olderThan");
            }
            if (rung.keepEvery().compareTo(previousEvery) < 0) {
                out.add(p + ": ladder rungs must not shrink keepEvery");
            }
            previousAge = rung.olderThan();
            previousEvery = rung.keepEvery();
        }
        if (ladder.maxAge() != null && ladder.maxAge().compareTo(previousAge) <= 0) {
            out.add(p + ": ladder maxAge must lie beyond the last rung");
        }
        if (ladder.runs() != null && ladder.runs() < 1) {
            out.add(p + ": ladder runs must be at least 1");
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    /** Mirrors the catalog into {@code raster_product} once the schema exists. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.RASTER_PRODUCTS)
    public void onReady() {
        sync();
    }

    /** Upserts a row for every product. */
    public void sync() {
        for (Registered product : products.values()) {
            upsertRow(product);
        }
    }

    /** Re-mirrors the catalog after the test cleaner emptied the tables. */
    @Override
    public void resetState() {
        try {
            sync();
        } catch (RuntimeException e) {
            log.debug("Could not re-mirror the product catalog", e);
        }
    }

    private void upsertRow(Registered product) {
        Instant now = clock.instant();
        RasterProductEntity row = repository.findById(product.id()).orElseGet(() -> {
            RasterProductEntity created = new RasterProductEntity(product.id());
            created.setCreatedAt(now);
            return created;
        });
        row.setSourceId(product.sourceId());
        row.setCategory(product.spec().category());
        row.setKind(product.spec().kind());
        row.setSpec(ProductSpecJson.toMap(product.spec()));
        row.setDynamic(product.dynamic());
        row.setUpdatedAt(now);
        repository.save(row);
    }

    /**
     * Registers or redefines a dynamic product at run time (SatDump, prompt 049).
     *
     * @param sourceId the owning source, which must list {@code raster-frame}
     * @param spec the spec
     * @return the registered product
     * @throws ValidationException (400) when the spec breaks a rule
     * @throws ConflictException (409) {@code PRODUCT_EXISTS} when another source or a compile-time
     *     descriptor owns the id
     */
    public synchronized Registered registerDynamic(String sourceId, ProductSpec spec) {
        Registered existing = products.get(spec.id());
        if (existing != null && (!existing.dynamic() || !existing.sourceId().equals(sourceId))) {
            throw new ConflictException("PRODUCT_EXISTS",
                    "Product '" + spec.id() + "' is already declared by " + existing.sourceId() + ".");
        }
        List<String> problems = problems(spec, sourceId, sources.find(sourceId).map(r -> r.descriptor()).orElse(null));
        if (!problems.isEmpty()) {
            throw new ValidationException(String.join("; ", problems));
        }
        Registered product = new Registered(spec, sourceId, true);
        products.put(spec.id(), product);
        upsertRow(product);
        events.publishEvent(new ProductCatalogChangedEvent(spec.id(), sourceId, existing == null
                ? ProductCatalogChangedEvent.Change.REGISTERED : ProductCatalogChangedEvent.Change.UPDATED));
        log.info("Registered dynamic product {} of {}", spec.id(), sourceId);
        return product;
    }

    /**
     * Removes a dynamic product from the catalog (its frames age out through retention).
     *
     * @return true when it was registered
     */
    public synchronized boolean removeDynamic(String productId) {
        Registered existing = products.get(productId);
        if (existing == null || !existing.dynamic()) {
            return false;
        }
        products.remove(productId);
        repository.deleteById(productId);
        events.publishEvent(new ProductCatalogChangedEvent(productId, existing.sourceId(),
                ProductCatalogChangedEvent.Change.REMOVED));
        return true;
    }

    /** The product with {@code id}, if any. */
    public Optional<Registered> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(products.get(id));
    }

    /**
     * The product with {@code id}.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PRODUCT}
     */
    public Registered require(String id) {
        return find(id).orElseThrow(() -> unknownProduct(id));
    }

    /** The {@code 404 UNKNOWN_PRODUCT} problem. */
    public static ForesignalException unknownProduct(String id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_PRODUCT,
                "Product '" + id + "' does not exist.");
    }

    /** Every product, ordered by id. */
    public List<Registered> all() {
        return products.values().stream().sorted(Comparator.comparing(Registered::id)).toList();
    }

    /** The products of {@code sourceId}, ordered by id. */
    public List<Registered> forSource(String sourceId) {
        return all().stream().filter(p -> p.sourceId().equals(sourceId)).toList();
    }

    /** True when {@code sourceId} owns any product. */
    public boolean hasProducts(String sourceId) {
        return products.values().stream().anyMatch(p -> p.sourceId().equals(sourceId));
    }
}
