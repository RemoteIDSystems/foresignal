package ai.foresignal.raster.model;

import ai.foresignal.raster.decode.DecodeRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * What one forecast model source ingests (prompt 051): its bucket, default path template, steps, run
 * cadence and availability lag, and the products built from its GRIB2 fields.
 *
 * @param sourceId the source id ({@code gfs}, {@code hrrr})
 * @param label the model's name in health reasons ({@code GFS}, {@code HRRR})
 * @param bucket the public S3 bucket
 * @param defaultTemplate the object-key template ({@link ModelCycleSelector#objectKey})
 * @param defaultSteps the default step items ({@link ForecastSteps})
 * @param defaultRunEvery the default run cadence
 * @param defaultLag the default availability lag
 * @param products the products, in commit order
 * @param probeProduct the product whose first field a probe reads
 */
public record ModelDefinition(String sourceId, String label, String bucket, String defaultTemplate,
        List<String> defaultSteps, Duration defaultRunEvery, Duration defaultLag, List<Product> products,
        String probeProduct) {

    /** Checks the parts. */
    public ModelDefinition {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(defaultTemplate, "defaultTemplate");
        defaultSteps = List.copyOf(defaultSteps);
        products = List.copyOf(products);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("A model needs at least one product");
        }
        if (products.stream().noneMatch(p -> p.productId().equals(probeProduct))) {
            throw new IllegalArgumentException("The probe product " + probeProduct + " is not one of the products");
        }
    }

    /** The product {@code productId}. */
    public Product product(String productId) {
        return products.stream().filter(p -> p.productId().equals(productId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown model product " + productId));
    }

    /**
     * One GRIB2 field of a product: the {@code .idx} variable and level (and whether the run-total
     * accumulation of the step is wanted), plus the decode request that selects the same message by
     * parameter and level.
     *
     * @param variable the {@code .idx} variable, for example {@code UGRD}
     * @param level the {@code .idx} level, for example {@code 10 m above ground}
     * @param accumulation true for a run-total accumulation ({@code 0-N hour acc})
     * @param request the GRIB2 selector for the decoder
     */
    public record Component(String variable, String level, boolean accumulation, DecodeRequest request,
            Boolean probabilistic, int bucketHours) {

        /** A component with no probability requirement (the v1 shape). */
        public Component(String variable, String level, boolean accumulation, DecodeRequest request) {
            this(variable, level, accumulation, request, null, 0);
        }

        /** A component requiring a probability (or not) but no interval bucket (prompt 106). */
        public Component(String variable, String level, boolean accumulation, DecodeRequest request,
                Boolean probabilistic) {
            this(variable, level, accumulation, request, probabilistic, 0);
        }

        /** An interval bucket of {@code bucketHours} ending at the step (prompt 106: NBM APCP/PoP). */
        public static Component bucket(String variable, String level, int bucketHours, Boolean probabilistic,
                DecodeRequest request) {
            return new Component(variable, level, false, request, probabilistic, bucketHours);
        }

        /** The {@code .idx} selector at {@code step}. */
        public FieldSelector selector(int step) {
            FieldSelector selector;
            if (bucketHours > 0) {
                selector = FieldSelector.bucket(variable, level, step - bucketHours, step);
            } else {
                selector = accumulation ? FieldSelector.runTotal(variable, level, step)
                        : FieldSelector.of(variable, level);
            }
            return probabilistic == null ? selector : selector.withProbabilistic(probabilistic);
        }
    }

    /**
     * One product.
     *
     * @param productId the product id
     * @param components one field, or two ({@code u}, {@code v}) for a wind product
     * @param required true when a missing field aborts the whole step (no frame for any product);
     *     false drops only this product (the God's Eye View {@code loadOptionalScalar} pattern)
     * @param factor multiplies every decoded value (HRRR smoke: kg m⁻³ to µg m⁻³ is 1e9); 1 for none
     * @param steps which configured steps the product has (GFS APCP has no f000)
     */
    public record Product(String productId, List<Component> components, boolean required, double factor,
            IntPredicate steps) {

        /** Checks the parts. */
        public Product {
            components = List.copyOf(components);
            if (components.isEmpty() || components.size() > 2) {
                throw new IllegalArgumentException("A model product has one or two fields");
            }
            steps = steps == null ? s -> true : steps;
            factor = factor == 0 ? 1.0 : factor;
        }

        /** True for a two-component wind product. */
        public boolean wind() {
            return components.size() == 2;
        }

        /** The product's steps among {@code configured}. */
        public List<Integer> stepsOf(List<Integer> configured) {
            return configured.stream().filter(steps::test).toList();
        }
    }
}
