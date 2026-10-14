package ai.foresignal.source.adapters.conflict;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the prompt 081 conflict and civil-alert sources share: the test base-URL property, the {@code event}
 * retention (Conventions: conflict events and alerts, 1825 d), JSON, text and time helpers and the field-spec
 * helpers. Not an adapter.
 */
public final class ConflictSources {

    /**
     * The test-only property moving every prompt 081 URL under one base as {@code {base}/{host}{path}}; blank in
     * production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.conflict.base-url";

    /** Raw retention of every prompt 081 kind: the {@code event} class (1825 d). */
    public static final Duration EVENT_RETENTION = Duration.ofDays(1825);

    /** The JSON mapper of upstream documents. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    /** The prefix of a gzipped, base64-encoded state value. */
    public static final String GZIP_PREFIX = "gz:";

    /** State values longer than this many characters are gzipped. */
    static final int GZIP_ABOVE = 32 * 1024;

    private ConflictSources() {}

    /** A state value, gzipped and base64-encoded behind {@value #GZIP_PREFIX} when longer than 32 KB. */
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

    /**
     * {@code url} moved under {@code baseOverride} as {@code {base}/{host}{path}?{query}}, or unchanged when the
     * override is blank.
     */
    public static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /** {@code value} with surrounding whitespace removed, or null when null or blank. */
    public static String trimmed(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    /**
     * The text of a scalar node, trimmed, or null when the node is missing, null, a container or blank. Numbers and
     * booleans are returned as their text.
     */
    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainer()) {
            return null;
        }
        return trimmed(node.asString());
    }

    /** A number from a JSON number or numeric text, or null when missing, unreadable or not finite. */
    public static Double number(JsonNode node) {
        String text = text(node);
        if (text == null) {
            return null;
        }
        try {
            double value = node.isNumber() ? node.doubleValue() : new BigDecimal(text).doubleValue();
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** An integer from a JSON number or integral text, or null when missing or not integral. */
    public static Long integer(JsonNode node) {
        Double value = number(node);
        if (value == null || value != Math.rint(value) || Math.abs(value) > 9.0e15) {
            return null;
        }
        return value.longValue();
    }

    /**
     * An ISO-8601 instant with an offset ({@code Z} or {@code +00:00}), or null when blank or unreadable. A
     * date-time without an offset is read as UTC.
     */
    public static Instant instant(String text) {
        String value = trimmed(text);
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException e2) {
                try {
                    return java.time.LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException ignored) {
                    return null;
                }
            }
        }
    }

    /** The start (00:00 UTC) of an ISO date {@code yyyy-MM-dd}, or null when blank or unreadable. */
    public static Instant dayStart(String text) {
        String value = trimmed(text);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value)
                    .atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * {@code text} cut to at most {@code maxBytes} UTF-8 bytes, never inside a character, or null when blank. Upstream
     * free text (notes, headlines) is untrusted and stored as data only.
     */
    public static String truncateUtf8(String text, int maxBytes) {
        String value = trimmed(text);
        if (value == null) {
            return null;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }
        int end = maxBytes;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8).strip();
    }

    /**
     * Parses a JSON body.
     *
     * @param what what is being read, for messages
     * @throws UpstreamFormatException when the body is not JSON or is empty
     */
    public static JsonNode json(byte[] body, String what) {
        JsonNode root;
        try {
            root = JSON.readTree(body == null ? new byte[0] : body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || root.isMissingNode() || root.isNull()) {
            throw new UpstreamFormatException(what + " is empty", "");
        }
        return root;
    }

    /**
     * The body of a 200 response that was not cut at the response cap.
     *
     * @param stateKey the validator state key to forget on failure, or null for an unconditional request
     * @throws UpstreamFormatException otherwise
     */
    public static byte[] body(FetchContext ctx, UpstreamResponse response, String stateKey, URI uri) {
        if (response.status() != 200 || response.truncated()) {
            if (stateKey != null) {
                SourceHttp.forgetValidators(ctx.state(), stateKey);
            }
            throw new UpstreamFormatException(ctx.sourceId() + ": " + uri.getPath() + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    /** Declares a text field. */
    public static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    /** Declares a number field with a UCUM unit, or null. */
    public static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }

    /** Declares an integer field. */
    public static void integer(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, null, label, description, null, Set.of()));
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
