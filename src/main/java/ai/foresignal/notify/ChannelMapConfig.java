package ai.foresignal.notify;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.raster.ProductRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The map-attachment keys of a channel's {@code config} (prompt 055; OpenAPI {@code MapAttachmentConfig};
 * Weather-Imagery-Design §2.3, §8), accepted on every channel type:
 *
 * <ul>
 *   <li>{@code attachMap}: {@code none} (default) or {@code static}. Off by default everywhere: an image of
 *       an alert's surroundings leaves the LAN on a cloud chat channel, so the operator opts in per channel.
 *   <li>{@code mapProducts}: the products drawn; default {@code ["mrms-reflectivity"]}, or the product
 *       that matched the rule (an alert on a {@code raster-frame} record).
 *   <li>{@code mapRadiusKm}: the radius around the alert geometry, default {@value #DEFAULT_RADIUS_KM}.
 *   <li>{@code maxAttachmentBytes}: the largest image sent; the default depends on the type
 *       ({@link #defaultCap}). Over it the map degrades to 600 px, then to the 256 px thumbnail, then to text.
 * </ul>
 *
 * @param attach true for {@code attachMap: static}
 * @param products the configured products, or empty for the default
 * @param radiusKm the radius, km
 * @param maxBytes the size cap, bytes
 */
public record ChannelMapConfig(boolean attach, List<String> products, double radiusKm, long maxBytes) {

    /** The {@code attachMap} key. */
    public static final String ATTACH_MAP = "attachMap";

    /** The {@code mapProducts} key. */
    public static final String MAP_PRODUCTS = "mapProducts";

    /** The {@code mapRadiusKm} key. */
    public static final String MAP_RADIUS_KM = "mapRadiusKm";

    /** The {@code maxAttachmentBytes} key. */
    public static final String MAX_ATTACHMENT_BYTES = "maxAttachmentBytes";

    /** {@code attachMap: none}. */
    public static final String NONE = "none";

    /** {@code attachMap: static}. */
    public static final String STATIC = "static";

    /** The default product. */
    public static final String DEFAULT_PRODUCT = "mrms-reflectivity";

    /** The default radius, km. */
    public static final double DEFAULT_RADIUS_KM = 50;

    /** The most products on an attached map. */
    public static final int MAX_PRODUCTS = 4;

    private static final long MB = 1024L * 1024L;
    private static final Pattern PRODUCT_ID = Pattern.compile(ProductRegistry.ID_REGEX);

    /** Copies the products. */
    public ChannelMapConfig {
        products = products == null ? List.of() : List.copyOf(products);
    }

    /**
     * The default {@code maxAttachmentBytes} of a type: Discord 8 MB, Telegram 10 MB, ntfy 15 MB, Pushover
     * 5,242,880 bytes, email 10 MB; types that only link to the image have no cap that matters (25 MB, the
     * media store's own limit).
     */
    public static long defaultCap(String type) {
        return switch (type == null ? "" : type) {
            case "discord" -> 8 * MB;
            case "telegram", "email" -> 10 * MB;
            case "ntfy" -> 15 * MB;
            case "pushover" -> 5_242_880L;
            default -> 25 * MB;
        };
    }

    /** The settings of a channel of {@code type} with {@code config}; invalid values fall back to defaults. */
    public static ChannelMapConfig of(String type, Map<String, Object> config) {
        Map<String, Object> c = config == null ? Map.of() : config;
        boolean attach = STATIC.equals(c.get(ATTACH_MAP));
        List<String> products = new ArrayList<>();
        if (c.get(MAP_PRODUCTS) instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String s && PRODUCT_ID.matcher(s).matches() && !products.contains(s)) {
                    products.add(s);
                }
            }
        }
        double radius = c.get(MAP_RADIUS_KM) instanceof Number n && n.doubleValue() >= 1 && n.doubleValue() <= 2000
                ? n.doubleValue() : DEFAULT_RADIUS_KM;
        long cap = c.get(MAX_ATTACHMENT_BYTES) instanceof Number n && n.longValue() > 0 ? n.longValue()
                : defaultCap(type);
        return new ChannelMapConfig(attach, products.subList(0, Math.min(MAX_PRODUCTS, products.size())), radius, cap);
    }

    /** The products to draw: the configured ones, else {@code matched} when given, else the default. */
    public List<String> productsOr(String matched) {
        if (!products.isEmpty()) {
            return products;
        }
        return List.of(matched != null ? matched : DEFAULT_PRODUCT);
    }

    /** Adds an error for each invalid map key present in {@code config}. */
    public static void validate(Map<String, Object> config, List<FieldError> errors) {
        if (config == null) {
            return;
        }
        Object attach = config.get(ATTACH_MAP);
        if (attach != null && !NONE.equals(attach) && !STATIC.equals(attach)) {
            errors.add(new FieldError("config." + ATTACH_MAP, "must be none or static"));
        }
        Object products = config.get(MAP_PRODUCTS);
        if (products != null) {
            if (!(products instanceof List<?> list) || list.isEmpty() || list.size() > MAX_PRODUCTS
                    || !list.stream().allMatch(v -> v instanceof String s && PRODUCT_ID.matcher(s).matches())) {
                errors.add(new FieldError("config." + MAP_PRODUCTS,
                        "must be an array of 1 to " + MAX_PRODUCTS + " product ids"));
            }
        }
        Object radius = config.get(MAP_RADIUS_KM);
        if (radius != null && !(radius instanceof Number n && n.doubleValue() >= 1 && n.doubleValue() <= 2000)) {
            errors.add(new FieldError("config." + MAP_RADIUS_KM, "must be a number between 1 and 2000"));
        }
        Object cap = config.get(MAX_ATTACHMENT_BYTES);
        if (cap != null && !(cap instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())
                && n.longValue() >= 1024 && n.longValue() <= 100 * MB)) {
            errors.add(new FieldError("config." + MAX_ATTACHMENT_BYTES,
                    "must be an integer between 1024 and " + 100 * MB));
        }
    }
}
