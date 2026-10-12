package ai.foresignal.notify.push;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Locale;

/**
 * VAPID (RFC 8292): an ES256 JWT over {@code {"aud", "exp", "sub"}} and the
 * {@code Authorization: vapid t=<jwt>, k=<publicKey>} header, with JDK crypto only.
 *
 * <p>The JDK signs ECDSA in ASN.1 DER ({@code SHA256withECDSA}); JWS needs the raw 64-byte
 * {@code R ‖ S} form (RFC 7518 §3.4), so {@link #derToRaw} and {@link #rawToDer} convert.
 */
public final class Vapid {

    /** The JWS header of every VAPID token. */
    static final String HEADER = "{\"typ\":\"JWT\",\"alg\":\"ES256\"}";

    private Vapid() {}

    /**
     * A signed VAPID JWT.
     *
     * @param aud the origin of the push resource URL
     * @param expEpochSeconds the expiry (at most 24 h ahead; Foresignal uses 12 h)
     * @param sub the contact, {@code mailto:} or {@code https:}
     */
    public static String jwt(ECPrivateKey key, String aud, long expEpochSeconds, String sub) {
        String claims = "{\"aud\":" + quote(aud) + ",\"exp\":" + expEpochSeconds + ",\"sub\":" + quote(sub) + "}";
        String input = EcKeys.b64(HEADER.getBytes(StandardCharsets.UTF_8)) + "."
                + EcKeys.b64(claims.getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(key);
            signature.update(input.getBytes(StandardCharsets.US_ASCII));
            return input + "." + EcKeys.b64(derToRaw(signature.sign()));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign the VAPID token: " + e.getMessage(), e);
        }
    }

    /** True when {@code jwt} is a well-formed ES256 JWT whose signature verifies with {@code key}. */
    public static boolean verify(String jwt, ECPublicKey key) {
        String[] parts = jwt == null ? new String[0] : jwt.split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            byte[] raw = EcKeys.unb64(parts[2]);
            if (raw.length != 2 * EcKeys.FIELD_LENGTH) {
                return false;
            }
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(key);
            signature.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            return signature.verify(rawToDer(raw));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /** The {@code Authorization} header value: {@code vapid t=<jwt>, k=<publicKey>}. */
    public static String authorization(String jwt, String publicKeyB64) {
        return "vapid t=" + jwt + ", k=" + publicKeyB64;
    }

    /**
     * The Unicode serialization of the origin of {@code endpoint} (RFC 6454 §6.1):
     * {@code scheme://host} plus {@code :port} when it is not the scheme's default.
     */
    public static String origin(URI endpoint) {
        String scheme = endpoint.getScheme().toLowerCase(Locale.ROOT);
        String host = endpoint.getHost().toLowerCase(Locale.ROOT);
        int port = endpoint.getPort();
        boolean defaultPort = port < 0 || (scheme.equals("https") && port == 443) || (scheme.equals("http") && port == 80);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }

    /**
     * Converts an ASN.1 DER ECDSA signature ({@code SEQUENCE {INTEGER r, INTEGER s}}) to raw
     * {@code R ‖ S}, each left-padded to 32 bytes.
     *
     * @throws IllegalArgumentException when it is not such a DER structure
     */
    public static byte[] derToRaw(byte[] der) {
        int[] pos = {0};
        if (der.length < 8 || der[pos[0]++] != 0x30) {
            throw new IllegalArgumentException("not a DER ECDSA signature");
        }
        int seqLength = length(der, pos);
        if (seqLength != der.length - pos[0]) {
            throw new IllegalArgumentException("DER sequence length mismatch");
        }
        BigInteger r = integer(der, pos);
        BigInteger s = integer(der, pos);
        if (pos[0] != der.length) {
            throw new IllegalArgumentException("trailing bytes after the DER signature");
        }
        byte[] out = new byte[2 * EcKeys.FIELD_LENGTH];
        System.arraycopy(EcKeys.fixed(r), 0, out, 0, EcKeys.FIELD_LENGTH);
        System.arraycopy(EcKeys.fixed(s), 0, out, EcKeys.FIELD_LENGTH, EcKeys.FIELD_LENGTH);
        return out;
    }

    /** Converts raw {@code R ‖ S} (64 bytes) to ASN.1 DER. */
    public static byte[] rawToDer(byte[] raw) {
        if (raw.length != 2 * EcKeys.FIELD_LENGTH) {
            throw new IllegalArgumentException("a raw P-256 signature is 64 bytes");
        }
        byte[] r = derInteger(new BigInteger(1, java.util.Arrays.copyOfRange(raw, 0, EcKeys.FIELD_LENGTH)));
        byte[] s = derInteger(new BigInteger(1, java.util.Arrays.copyOfRange(raw, EcKeys.FIELD_LENGTH, raw.length)));
        int contentLength = r.length + s.length;
        byte[] lengthBytes = contentLength < 128 ? new byte[] {(byte) contentLength}
                : new byte[] {(byte) 0x81, (byte) contentLength};
        byte[] out = new byte[1 + lengthBytes.length + contentLength];
        out[0] = 0x30;
        System.arraycopy(lengthBytes, 0, out, 1, lengthBytes.length);
        System.arraycopy(r, 0, out, 1 + lengthBytes.length, r.length);
        System.arraycopy(s, 0, out, 1 + lengthBytes.length + r.length, s.length);
        return out;
    }

    private static byte[] derInteger(BigInteger value) {
        byte[] content = value.toByteArray();
        byte[] out = new byte[2 + content.length];
        out[0] = 0x02;
        out[1] = (byte) content.length;
        System.arraycopy(content, 0, out, 2, content.length);
        return out;
    }

    private static int length(byte[] der, int[] pos) {
        if (pos[0] >= der.length) {
            throw new IllegalArgumentException("truncated DER length");
        }
        int first = der[pos[0]++] & 0xFF;
        if (first < 0x80) {
            return first;
        }
        int count = first & 0x7F;
        if (count == 0 || count > 2 || pos[0] + count > der.length) {
            throw new IllegalArgumentException("unsupported DER length");
        }
        int value = 0;
        for (int i = 0; i < count; i++) {
            value = (value << 8) | (der[pos[0]++] & 0xFF);
        }
        return value;
    }

    private static BigInteger integer(byte[] der, int[] pos) {
        if (pos[0] >= der.length || der[pos[0]++] != 0x02) {
            throw new IllegalArgumentException("expected a DER INTEGER");
        }
        int length = length(der, pos);
        if (length < 1 || pos[0] + length > der.length || length > EcKeys.FIELD_LENGTH + 1) {
            throw new IllegalArgumentException("bad DER INTEGER length");
        }
        BigInteger value = new BigInteger(1, java.util.Arrays.copyOfRange(der, pos[0], pos[0] + length));
        pos[0] += length;
        return value;
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
