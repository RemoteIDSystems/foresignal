package ai.foresignal.source.adapters.faa;

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
 * What the prompt 087 FAA sources share: the test base-URL property, the attribution and license, the {@code event}
 * retention, JSON reading, state packing and the field-spec helpers. Not an adapter.
 */
public final class FaaSources {

    /**
     * The test-only property moving every FAA URL under one base as {@code {base}/{host}{path}}; blank in
     * production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.faa.base-url";

    /** Raw retention of restrictions and delays: the {@code event} class (NOTAMs and TFRs, 1825 d). */
    public static final Duration EVENT_RETENTION = Duration.ofDays(1825);

    /** The license of every FAA source: US Government work. */
    public static final String LICENSE = "US-PD";

    /** The attribution of every FAA source. */
    public static final String ATTRIBUTION = "Federal Aviation Administration";

    /** The JSON mapper of FAA documents. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The prefix of a gzipped, base64-encoded state value. */
    static final String GZIP_PREFIX = "gz:";

    /** State values longer than this many characters are gzipped. */
    static final int GZIP_ABOVE = 16 * 1024;

    private FaaSources() {}

    /** {@code url}, moved under {@code baseOverride} as {@code {base}/{host}{path}} when the override is set. */
    public static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /**
     * The body of a 200 response that was not cut at the response cap.
     *
     * @param what what is read, for messages
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
     * @param what what is read, for messages
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

    /** A scalar's text, trimmed, or null when missing, null, a container or blank. */
    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        String text = node.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }

    /** {@code text} trimmed, or null when null or blank. */
    public static String trimmed(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }

    /** A state value, gzipped and base64-encoded behind {@value #GZIP_PREFIX} when longer than 16 KB. */
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

    /** The text of a state value written by {@link #pack}; null stays null, a damaged value reads as empty. */
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

    /** Declares a text field. */
    public static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    /** Declares an integer field with a UCUM unit, or null. */
    public static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }

    /** Declares a boolean field. */
    public static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }

    /** Declares an array field. */
    public static void array(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.ARRAY, null, label, description, null, Set.of()));
    }

    /** Declares a timestamp field. */
    public static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }
}
