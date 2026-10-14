package ai.foresignal.source.adapters.maritime;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the prompt 088 maritime sources share: the test base-URL property, the retention windows, JSON reading, state
 * packing and the field-spec helpers. Not an adapter.
 */
public final class MaritimeSources {

    /**
     * The test-only property moving every maritime URL under one base as {@code {base}/{host}{path}}; blank in
     * production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.maritime.base-url";

    /** Raw retention of warnings and vessel events: the {@code event} class (navigational warnings, 1825 d). */
    public static final Duration EVENT_RETENTION = Duration.ofDays(1825);

    /** Raw retention of daily activity counts: the {@code series} class (3650 d). */
    public static final Duration SERIES_RETENTION = Duration.ofDays(3650);

    /** Raw retention of the port catalog: the {@code reference} class (90 d of history). */
    public static final Duration REFERENCE_RETENTION = Duration.ofDays(90);

    /** The JSON mapper of maritime documents. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The prefix of a gzipped, base64-encoded state value. */
    static final String GZIP_PREFIX = "gz:";

    /** State values longer than this many characters are gzipped. */
    static final int GZIP_ABOVE = 16 * 1024;

    private MaritimeSources() {}

    /**
     * {@code url}, moved under {@code baseOverride} as {@code {base}/{host}{path}} when the override is set.
     *
     * @param url the production URL
     * @param baseOverride the test base, or blank
     * @return the URL to request
     */
    public static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /**
     * The body of a 200 response that was not cut at the response cap.
     *
     * @param response the response
     * @param what what is read, for messages
     * @return the body
     * @throws UpstreamFormatException otherwise
     */
    public static byte[] body(UpstreamResponse response, String what) {
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(what + (response.status() != 200 ? " answered HTTP "
                    + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    /**
     * Parses a JSON document.
     *
     * @param body the bytes
     * @param what what is read, for messages
     * @return the root node
     * @throws UpstreamFormatException when it is not JSON or is empty
     */
    public static JsonNode json(byte[] body, String what) {
        try {
            JsonNode root = JSON.readTree(body == null ? new byte[0] : body);
            if (root == null || root.isMissingNode() || root.isNull()) {
                throw new UpstreamFormatException(what + " is empty", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
    }

    /**
     * A scalar's text, trimmed.
     *
     * @param node the node, possibly null
     * @return the text, or null when missing, null, a container or blank
     */
    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        String text = node.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }

    /**
     * A numeric value.
     *
     * @param node the node, possibly null
     * @return the number as a double, or null when the node is not a finite number
     */
    public static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    /**
     * An integral value.
     *
     * @param node the node, possibly null
     * @return the number rounded to a long, or null when the node is not a finite number
     */
    public static Long integer(JsonNode node) {
        Double value = number(node);
        return value == null ? null : Math.round(value);
    }

    /**
     * A state value, gzipped and base64-encoded behind {@value #GZIP_PREFIX} when longer than 16 KB.
     *
     * @param text the value
     * @return the stored form
     */
    public static String pack(String text) {
        if (text == null || text.length() <= GZIP_ABOVE) {
            return text;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return GZIP_PREFIX + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * The text of a state value written by {@link #pack}.
     *
     * @param value the stored form
     * @return the text; null stays null, a damaged value reads as empty
     */
    public static String unpack(String value) {
        if (value == null || !value.startsWith(GZIP_PREFIX)) {
            return value;
        }
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(
                Base64.getDecoder().decode(value.substring(GZIP_PREFIX.length()))))) {
            return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            return "";
        }
    }

    /**
     * Declares a text field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param label the label
     * @param description what it holds
     */
    public static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    /**
     * Declares an integer field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param unit a UCUM unit, or null
     * @param label the label
     * @param description what it holds
     */
    public static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }

    /**
     * Declares a number field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param unit a UCUM unit, or null
     * @param label the label
     * @param description what it holds
     */
    public static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }

    /**
     * Declares a boolean field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param label the label
     * @param description what it holds
     */
    public static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }

    /**
     * Declares an array field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param label the label
     * @param description what it holds
     */
    public static void array(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.ARRAY, null, label, description, null, Set.of()));
    }

    /**
     * Declares a timestamp field.
     *
     * @param b the descriptor builder
     * @param key the payload key
     * @param label the label
     * @param description what it holds
     */
    public static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }
}
