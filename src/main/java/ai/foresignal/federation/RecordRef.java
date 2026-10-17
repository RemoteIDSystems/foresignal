package ai.foresignal.federation;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * A globally unique record reference (prompt 113): {@code urn:foresignal:{instanceId}:{sourceId}:{externalId}}.
 *
 * <p>The instance id is the install id (003) and the source id is kebab-case, so neither contains a colon; the
 * external id is percent-encoded (RFC 3986: every octet outside the unreserved set {@code A-Z a-z 0-9 - . _ ~} is
 * written as {@code %XX} of its UTF-8 bytes), so the reference parses back unambiguously. This class is the only
 * place that formats or parses a reference.
 *
 * <p>It also maps a federated record to its local external id ({@link #localExternalId}): {@code
 * {originSourceId}/{originExternalId}}, or {@code sha256:} plus the hex digest when that would exceed the 256
 * characters a stored external id may have (the original then travels in the payload).
 *
 * @param instanceId the origin instance's install id
 * @param sourceId the source id on that instance
 * @param externalId the external id within the source (not encoded)
 */
public record RecordRef(String instanceId, String sourceId, String externalId) {

    /** The URN prefix. */
    public static final String PREFIX = "urn:foresignal:";

    /** The longest stored external id (the {@code record_version.external_id} column). */
    public static final int MAX_EXTERNAL_ID = 256;

    /** The prefix of a hashed local external id. */
    public static final String HASH_PREFIX = "sha256:";

    private static final HexFormat HEX = HexFormat.of();

    /** Requires every part and refuses a colon in the instance or source id. */
    public RecordRef {
        Objects.requireNonNull(instanceId, "instanceId");
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(externalId, "externalId");
        if (instanceId.isBlank() || instanceId.indexOf(':') >= 0) {
            throw new IllegalArgumentException("an instance id is non-blank and has no colon");
        }
        if (sourceId.isBlank() || sourceId.indexOf(':') >= 0) {
            throw new IllegalArgumentException("a source id is non-blank and has no colon");
        }
    }

    /** The reference as a URN. */
    public String format() {
        return PREFIX + instanceId + ":" + sourceId + ":" + encode(externalId);
    }

    @Override
    public String toString() {
        return format();
    }

    /** {@code urn:foresignal:{instanceId}:{sourceId}:{encoded externalId}}. */
    public static String format(String instanceId, String sourceId, String externalId) {
        return new RecordRef(instanceId, sourceId, externalId).format();
    }

    /**
     * Parses a reference.
     *
     * @throws IllegalArgumentException when {@code urn} is not a well-formed reference
     */
    public static RecordRef parse(String urn) {
        if (urn == null || !urn.startsWith(PREFIX)) {
            throw new IllegalArgumentException("a record reference starts with " + PREFIX);
        }
        String rest = urn.substring(PREFIX.length());
        int first = rest.indexOf(':');
        int second = first < 0 ? -1 : rest.indexOf(':', first + 1);
        if (first <= 0 || second <= first + 1) {
            throw new IllegalArgumentException("a record reference is urn:foresignal:{instanceId}:{sourceId}:{externalId}");
        }
        String encoded = rest.substring(second + 1);
        if (encoded.isEmpty()) {
            throw new IllegalArgumentException("a record reference names an external id");
        }
        return new RecordRef(rest.substring(0, first), rest.substring(first + 1, second), decode(encoded));
    }

    /** Percent-encodes every octet of the UTF-8 form outside RFC 3986's unreserved set. */
    public static String encode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder out = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            int c = b & 0xff;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '.'
                    || c == '_' || c == '~') {
                out.append((char) c);
            } else {
                out.append('%').append(Character.toUpperCase(Character.forDigit(c >> 4, 16)))
                        .append(Character.toUpperCase(Character.forDigit(c & 0xf, 16)));
            }
        }
        return out.toString();
    }

    /**
     * Decodes {@link #encode}'s output (either hex case is accepted).
     *
     * @throws IllegalArgumentException on a malformed escape or bytes that are not UTF-8
     */
    public static String decode(String value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '%') {
                if (i + 2 >= value.length()) {
                    throw new IllegalArgumentException("a truncated percent escape");
                }
                int hi = Character.digit(value.charAt(i + 1), 16);
                int lo = Character.digit(value.charAt(i + 2), 16);
                if (hi < 0 || lo < 0) {
                    throw new IllegalArgumentException("a malformed percent escape");
                }
                out.write((hi << 4) | lo);
                i += 2;
            } else if (c > 0x7f) {
                throw new IllegalArgumentException("an encoded external id is ASCII");
            } else {
                out.write(c);
            }
        }
        byte[] bytes = out.toByteArray();
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException e) {
            throw new IllegalArgumentException("an external id is UTF-8", e);
        }
    }

    /**
     * The local external id of a federated record: {@code {originSourceId}/{originExternalId}}, or, when that is
     * longer than {@value #MAX_EXTERNAL_ID} characters, {@code sha256:} and the lowercase hex SHA-256 of it.
     */
    public static String localExternalId(String originSourceId, String originExternalId) {
        String plain = originSourceId + "/" + originExternalId;
        if (plain.length() <= MAX_EXTERNAL_ID) {
            return plain;
        }
        return HASH_PREFIX + sha256Hex(plain);
    }

    /** True when {@code localExternalId} is a hashed one (the original is in the payload). */
    public static boolean hashed(String localExternalId) {
        return localExternalId != null && localExternalId.startsWith(HASH_PREFIX);
    }

    /** The lowercase hex SHA-256 of the UTF-8 form of {@code value}. */
    static String sha256Hex(String value) {
        return HEX.formatHex(sha256(value));
    }

    /** The SHA-256 of the UTF-8 form of {@code value}. */
    static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** The compact message id (113, CBOR key 11): the first 8 bytes of the SHA-256 of this reference. */
    public byte[] messageId() {
        return messageId(format());
    }

    /** The compact message id of a formatted reference: the first 8 bytes of its SHA-256. */
    public static byte[] messageId(String formattedRef) {
        return java.util.Arrays.copyOf(sha256(formattedRef), 8);
    }
}
