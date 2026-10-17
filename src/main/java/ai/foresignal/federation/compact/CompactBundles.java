package ai.foresignal.federation.compact;

import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * The store-and-forward bundle (prompt 113): a definite-length CBOR array whose elements are compact messages,
 * each exactly as {@link MeshCborProfile} encodes it, for sneakernet or DTN tooling (BPv7 bundle agents).
 */
public final class CompactBundles {

    /** The largest bundle accepted. */
    public static final int MAX_BYTES = 1024 * 1024;

    /** The most messages in a bundle. */
    public static final int MAX_MESSAGES = 5_000;

    private static final CBORMapper CBOR = CBORMapper.builder().build();

    private CompactBundles() {}

    /** A CBOR array of the encoded messages, in order. */
    public static byte[] encode(List<byte[]> encodedMessages) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeHeader(out, encodedMessages.size());
        for (byte[] message : encodedMessages) {
            out.writeBytes(message);
        }
        return out.toByteArray();
    }

    /**
     * Decodes a bundle. Elements that are not compact messages are skipped (counted by the caller as the difference).
     *
     * @throws IllegalArgumentException when {@code bytes} is not a CBOR array
     */
    public static List<MeshCborMessage> decode(byte[] bytes) {
        JsonNode root;
        try {
            root = CBOR.readTree(bytes);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("not a CBOR bundle: " + e.getMessage(), e);
        }
        if (root == null || !root.isArray()) {
            throw new IllegalArgumentException("a bundle is a CBOR array");
        }
        if (root.size() > MAX_MESSAGES) {
            throw new IllegalArgumentException("a bundle carries at most " + MAX_MESSAGES + " messages");
        }
        List<MeshCborMessage> out = new ArrayList<>();
        for (JsonNode element : root) {
            if (!element.isObject()) {
                continue;
            }
            try {
                out.add(MeshCborProfile.decode(CBOR.writeValueAsBytes(element)));
            } catch (RuntimeException e) {
                // not a compact message: skipped
            }
        }
        return out;
    }

    /** The CBOR major type 4 (array) header of {@code n} elements. */
    static void writeHeader(ByteArrayOutputStream out, int n) {
        if (n < 24) {
            out.write(0x80 | n);
        } else if (n < 0x100) {
            out.write(0x98);
            out.write(n);
        } else if (n < 0x10000) {
            out.write(0x99);
            out.write(n >>> 8);
            out.write(n & 0xff);
        } else {
            out.write(0x9a);
            out.write(n >>> 24);
            out.write((n >>> 16) & 0xff);
            out.write((n >>> 8) & 0xff);
            out.write(n & 0xff);
        }
    }
}
