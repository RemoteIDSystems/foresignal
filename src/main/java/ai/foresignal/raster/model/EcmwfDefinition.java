package ai.foresignal.raster.model;

import ai.foresignal.raster.decode.DecodeRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * What the ECMWF IFS source ingests (prompt 105; Weather-Imagery-Design §5.3): the {@code
 * ecmwf-forecasts} S3 mirror (or the {@code data.ecmwf.int} fallback), a configurable path template
 * with a {@code {res}} resolution segment and an unpadded {@code {step}} token, and the products
 * built from its {@code .index}-selected GRIB2 fields.
 *
 * <p>It is the ECMWF sibling of {@link ModelDefinition}: the field selector names an ECMWF
 * {@code param} and {@code levtype} (plus {@code levelist}) rather than an NCEP variable and level,
 * and a product may be a difference of two steps (ECMWF's {@code tp} is accumulated from the run
 * start, so 3-hourly and 24-hourly precipitation are the difference of two run totals).
 *
 * @param sourceId the source id ({@code ecmwf-ifs})
 * @param label the model's name in health reasons ({@code IFS})
 * @param bucket the public S3 mirror bucket ({@code ecmwf-forecasts})
 * @param defaultTemplate the path template, with {@code {yyyyMMdd}}, {@code {HH}}, {@code {res}} and {@code {step}}
 * @param defaultSteps the default step items ({@link ForecastSteps})
 * @param defaultRunEvery the default run cadence ({@code 12h}: 00z and 12z)
 * @param defaultLag the default availability lag
 * @param defaultResolution the default {@code {res}} value ({@code 0p25})
 * @param products the products, in commit order
 * @param probeProduct the product whose first field a probe reads
 */
public record EcmwfDefinition(String sourceId, String label, String bucket, String defaultTemplate,
        List<String> defaultSteps, Duration defaultRunEvery, Duration defaultLag, String defaultResolution,
        List<Product> products, String probeProduct) {

    /** Checks the parts. */
    public EcmwfDefinition {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(defaultTemplate, "defaultTemplate");
        defaultSteps = List.copyOf(defaultSteps);
        products = List.copyOf(products);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("An ECMWF model needs at least one product");
        }
        if (products.stream().noneMatch(p -> p.productId().equals(probeProduct))) {
            throw new IllegalArgumentException("The probe product " + probeProduct + " is not one of the products");
        }
    }

    /** The product {@code productId}. */
    public Product product(String productId) {
        return products.stream().filter(p -> p.productId().equals(productId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ECMWF product " + productId));
    }

    /**
     * One ECMWF field of a product.
     *
     * @param param the ECMWF short parameter name ({@code 10u}, {@code 2t}, {@code tp}, …)
     * @param levtype the level type ({@code sfc}, {@code pl}, …)
     * @param levelist the level, or null
     * @param request the GRIB2 selector for the decoder
     */
    public record Component(String param, String levtype, String levelist, DecodeRequest request) {}

    /**
     * One product.
     *
     * @param productId the product id
     * @param components one field, or two ({@code u}, {@code v}) for a wind product
     * @param required true when a missing field aborts the whole step
     * @param factor multiplies every decoded value; 1 for none
     * @param differenceHours 0 for none; otherwise the decoded value is the field at {@code step}
     *     minus the same field at {@code step − differenceHours} (ECMWF {@code tp} is a run total)
     * @param steps which configured steps the product has (no f000 for a difference)
     */
    public record Product(String productId, List<Component> components, boolean required, double factor,
            int differenceHours, IntPredicate steps) {

        /** Checks the parts. */
        public Product {
            components = List.copyOf(components);
            if (components.isEmpty() || components.size() > 2) {
                throw new IllegalArgumentException("An ECMWF product has one or two fields");
            }
            if (differenceHours < 0) {
                throw new IllegalArgumentException("differenceHours must not be negative");
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
