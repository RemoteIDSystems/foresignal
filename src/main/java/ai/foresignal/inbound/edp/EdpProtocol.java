package ai.foresignal.inbound.edp;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Constants of the External Data Protocol 1.x (docs/External-Data-Protocol.md). */
public final class EdpProtocol {

    /** The protocol this instance speaks natively. */
    public static final String VERSION = "foresignal-edp/1.0";

    /** The item types, in the order the specification lists them. */
    public static final List<String> ITEM_TYPES =
            List.of("feature", "event", "observation", "document", "forecast", "delete", "status");

    /** The item types a stream may declare as its {@code itemType}. */
    public static final List<String> STREAM_ITEM_TYPES =
            List.of("feature", "event", "observation", "document", "forecast");

    /** The stream modes. */
    public static final List<String> MODES = List.of("layer", "atomic");

    /** The exported JSON Schema names served at {@code /v1/external/protocol/schemas/{name}}. */
    public static final List<String> SCHEMA_NAMES = List.of(
            "manifest", "batch", "item", "feature", "event", "observation", "document", "forecast", "delete",
            "status");

    /** Registered external sources are {@code ext-{source.id}}. */
    public static final String SOURCE_PREFIX = "ext-";

    /** Registration tokens start with this. */
    public static final String REGISTRATION_TOKEN_PREFIX = "fsx_";

    /** Per batch: 5 MB. */
    public static final int MAX_BATCH_BYTES = 5 * 1024 * 1024;

    /** Per batch: 10,000 items. */
    public static final int MAX_ITEMS = 10_000;

    /** Per batch: JSON nesting depth 32. */
    public static final int MAX_DEPTH = 32;

    /** A document's {@code text}: 20,000 characters. */
    public static final int MAX_TEXT_LENGTH = 20_000;

    /** Any other string: 2,048 characters. */
    public static final int MAX_STRING_LENGTH = 2_048;

    /** Per item: 20 media entries. */
    public static final int MAX_MEDIA = 20;

    /** Per item: 10,000 geometry vertices. */
    public static final int MAX_VERTICES = 10_000;

    /** A payload schema: 64 KB. */
    public static final int MAX_SCHEMA_BYTES = 64 * 1024;

    /** A {@code pattern} or {@code patternProperties} key: 256 characters. */
    public static final int MAX_PATTERN_LENGTH = 256;

    private static final Pattern PROTOCOL = Pattern.compile("^foresignal-edp/1\\.(0|[1-9][0-9]{0,8})$");

    private EdpProtocol() {}

    /**
     * The minor version of a {@code foresignal-edp/1.N} string.
     *
     * @return N, or -1 when {@code protocol} is not a valid 1.x protocol string
     */
    public static int minor(String protocol) {
        if (protocol == null) {
            return -1;
        }
        Matcher matcher = PROTOCOL.matcher(protocol);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** {@code ext-{providerId}}. */
    public static String sourceId(String providerId) {
        return SOURCE_PREFIX + providerId;
    }
}
