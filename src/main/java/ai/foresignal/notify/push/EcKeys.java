package ai.foresignal.notify.push;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.KeyAgreement;

/**
 * P-256 (secp256r1) key handling with JDK crypto only: generation, the uncompressed point form used
 * by Web Push and VAPID ({@code 0x04 ‖ X ‖ Y}, 65 bytes, X9.62), raw private scalars, PKCS#8 and
 * ECDH. Also base64url without padding, the encoding of every key on the wire.
 */
public final class EcKeys {

    /** Length of an uncompressed P-256 point. */
    public static final int POINT_LENGTH = 65;

    /** Length of one P-256 coordinate or scalar. */
    public static final int FIELD_LENGTH = 32;

    /** The P-256 domain parameters. */
    public static final ECParameterSpec P256 = p256();

    private static final SecureRandom RANDOM = new SecureRandom();

    private EcKeys() {}

    private static ECParameterSpec p256() {
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("P-256 is not available: " + e.getMessage(), e);
        }
    }

    /** A fresh P-256 key pair. */
    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"), RANDOM);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not generate a P-256 key pair: " + e.getMessage(), e);
        }
    }

    /** The uncompressed point of {@code key}: {@code 0x04 ‖ X ‖ Y}. */
    public static byte[] encodePoint(ECPublicKey key) {
        ECPoint w = key.getW();
        byte[] out = new byte[POINT_LENGTH];
        out[0] = 0x04;
        System.arraycopy(fixed(w.getAffineX()), 0, out, 1, FIELD_LENGTH);
        System.arraycopy(fixed(w.getAffineY()), 0, out, 1 + FIELD_LENGTH, FIELD_LENGTH);
        return out;
    }

    /**
     * The public key of an uncompressed point.
     *
     * @throws IllegalArgumentException when it is not 65 bytes starting {@code 0x04}, or not on the curve
     */
    public static ECPublicKey decodePoint(byte[] point) {
        if (point == null || point.length != POINT_LENGTH || point[0] != 0x04) {
            throw new IllegalArgumentException("a P-256 public key must be a 65-byte uncompressed point");
        }
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(point, 1, 1 + FIELD_LENGTH));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(point, 1 + FIELD_LENGTH, POINT_LENGTH));
        if (!onCurve(x, y)) {
            throw new IllegalArgumentException("the point is not on the P-256 curve");
        }
        try {
            return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), P256));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("not a valid P-256 public key: " + e.getMessage(), e);
        }
    }

    /** True when (x, y) satisfies {@code y² = x³ + ax + b (mod p)} with both coordinates in range. */
    static boolean onCurve(BigInteger x, BigInteger y) {
        BigInteger p = ((ECFieldFp) P256.getCurve().getField()).getP();
        if (x.signum() < 0 || y.signum() < 0 || x.compareTo(p) >= 0 || y.compareTo(p) >= 0) {
            return false;
        }
        BigInteger left = y.multiply(y).mod(p);
        BigInteger right = x.pow(3).add(P256.getCurve().getA().multiply(x)).add(P256.getCurve().getB()).mod(p);
        return left.equals(right);
    }

    /** The private key of a raw 32-byte scalar. */
    public static ECPrivateKey privateFromScalar(byte[] scalar) {
        try {
            return (ECPrivateKey) KeyFactory.getInstance("EC")
                    .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, scalar), P256));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("not a valid P-256 private scalar: " + e.getMessage(), e);
        }
    }

    /** The private key of PKCS#8 DER bytes. */
    public static ECPrivateKey privateFromPkcs8(byte[] der) {
        try {
            return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | ClassCastException e) {
            throw new IllegalArgumentException("not a P-256 PKCS#8 private key: " + e.getMessage(), e);
        }
    }

    /** The raw 32-byte scalar of {@code key}. */
    public static byte[] scalar(ECPrivateKey key) {
        return fixed(key.getS());
    }

    /** ECDH: the 32-byte shared secret (the X coordinate of the shared point). */
    public static byte[] ecdh(ECPrivateKey privateKey, ECPublicKey publicKey) {
        try {
            KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
            agreement.init(privateKey);
            agreement.doPhase(publicKey, true);
            return agreement.generateSecret();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("ECDH failed: " + e.getMessage(), e);
        }
    }

    /** {@code value} as exactly 32 unsigned big-endian bytes. */
    static byte[] fixed(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == FIELD_LENGTH) {
            return bytes;
        }
        byte[] out = new byte[FIELD_LENGTH];
        if (bytes.length > FIELD_LENGTH) {
            System.arraycopy(bytes, bytes.length - FIELD_LENGTH, out, 0, FIELD_LENGTH);
        } else {
            System.arraycopy(bytes, 0, out, FIELD_LENGTH - bytes.length, bytes.length);
        }
        return out;
    }

    /** Base64url without padding. */
    public static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Decodes base64url, with or without padding; whitespace is ignored. Standard base64 characters
     * ({@code +}, {@code /}) are accepted too, because some clients send them.
     *
     * @throws IllegalArgumentException when the text is not base64
     */
    public static byte[] unb64(String text) {
        String clean = text.replaceAll("\\s", "").replace('+', '-').replace('/', '_');
        while (clean.endsWith("=")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return Base64.getUrlDecoder().decode(clean);
    }
}
