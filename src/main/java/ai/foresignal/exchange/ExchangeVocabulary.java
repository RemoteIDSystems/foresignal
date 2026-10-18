package ai.foresignal.exchange;

import java.util.List;

/** The fixed vocabulary of intelligence exchange (prompt 132, OpenAPI {@code PackageManifest}). */
public final class ExchangeVocabulary {

    /** The package media type. */
    public static final String MEDIA_TYPE = "application/vnd.foresignal.package+zip";

    /** The package types. */
    public static final List<String> TYPES = List.of("report", "investigation", "chronicle-extract", "evidence");

    /** Being built. */
    public static final String BUILDING = "building";

    /** Built, kept for download. */
    public static final String READY = "ready";

    /** Being delivered to peers. */
    public static final String DELIVERING = "delivering";

    /** Delivered to every recipient instance. */
    public static final String DELIVERED = "delivered";

    /** The build or a delivery failed. */
    public static final String FAILED = "failed";

    /** The package statuses. */
    public static final List<String> STATUSES = List.of(BUILDING, READY, DELIVERING, DELIVERED, FAILED);

    /** The manifest's path in the ZIP. */
    public static final String MANIFEST = "manifest.json";

    /** The signature's path in the ZIP. */
    public static final String SIGNATURE = "signature.jws";

    /** The content folder. */
    public static final String CONTENT = "content/";

    /** The JSON bundle. */
    public static final String BUNDLE = "content/bundle.json";

    /** The Markdown rendition. */
    public static final String RENDITION_MD = "content/rendition.md";

    /** The HTML rendition. */
    public static final String RENDITION_HTML = "content/rendition.html";

    /** The chronicle extract. */
    public static final String EXTRACT = "content/extract.json";

    /** The attachments folder. */
    public static final String ATTACHMENTS = "content/attachments/";

    /** The default {@code exchange.maxPackageMb}. */
    public static final int DEFAULT_MAX_MB = 200;

    /** Most entries in a package. */
    public static final int MAX_ENTRIES = 10_000;

    /** Wildcard recipients (GREEN and CLEAR only). */
    public static final String EVERYONE = "*";

    private ExchangeVocabulary() {}

    /** The package type of an object reference type, or null when it cannot be packaged. */
    public static String typeOf(String objectType) {
        return switch (objectType) {
            case "report" -> "report";
            case "investigation" -> "investigation";
            case "chronicle" -> "chronicle-extract";
            case "evidence-item" -> "evidence";
            default -> null;
        };
    }
}
