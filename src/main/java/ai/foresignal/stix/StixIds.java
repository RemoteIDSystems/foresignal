package ai.foresignal.stix;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Deterministic STIX identifiers (prompt 141): {@code {type}--{UUIDv5}} (RFC 9562 §5.5, SHA-1 name-based).
 *
 * <p>The namespace is itself a UUIDv5 of {@value #NAMESPACE_NAME} in the RFC 9562 URL namespace, so it never changes
 * and needs no configuration. The name hashed for a record is its Client Contract object reference
 * ({@code record:{sourceId}/{externalId}}), so a record keeps its STIX id across exports, versions and instances.
 */
public final class StixIds {

    /** The RFC 9562 URL namespace. */
    public static final UUID URL_NAMESPACE = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

    /** The name the Foresignal STIX namespace is derived from. */
    public static final String NAMESPACE_NAME = "https://foresignal.ai/stix";

    /** The Foresignal STIX namespace. */
    public static final UUID NAMESPACE = uuid5(URL_NAMESPACE, NAMESPACE_NAME);

    private StixIds() {}

    /** {@code {type}--{UUIDv5(NAMESPACE, name)}}. */
    public static String id(String type, String name) {
        return type + "--" + uuid5(NAMESPACE, name);
    }

    /**
     * The UUIDv5 of {@code name} in {@code namespace}: SHA-1 of the namespace bytes followed by the UTF-8 name, the
     * first 16 bytes with the version nibble set to 5 and the RFC 4122 variant bits.
     */
    public static UUID uuid5(UUID namespace, String name) {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is unavailable", e);
        }
        ByteBuffer ns = ByteBuffer.allocate(16);
        ns.putLong(namespace.getMostSignificantBits());
        ns.putLong(namespace.getLeastSignificantBits());
        sha1.update(ns.array());
        sha1.update(name.getBytes(StandardCharsets.UTF_8));
        byte[] hash = sha1.digest();
        hash[6] = (byte) ((hash[6] & 0x0F) | 0x50);
        hash[8] = (byte) ((hash[8] & 0x3F) | 0x80);
        ByteBuffer bytes = ByteBuffer.wrap(hash, 0, 16);
        return new UUID(bytes.getLong(), bytes.getLong());
    }
}
