package ai.foresignal.notify.push.provider;

import ai.foresignal.notify.push.EcKeys;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Parses the private keys operators upload (prompt 032), with the JDK only.
 *
 * <ul>
 *   <li>APNs: the {@code .p8} auth key, a PEM {@code PRIVATE KEY} (PKCS#8) holding an EC key on
 *       P-256 (ES256).
 *   <li>FCM: the service account's {@code private_key}, a PEM {@code PRIVATE KEY} (PKCS#8) holding an
 *       RSA key (RS256).
 * </ul>
 * Error messages describe the problem, never the key material.
 */
public final class ProviderKeys {

    private static final String BEGIN = "-----BEGIN PRIVATE KEY-----";
    private static final String END = "-----END PRIVATE KEY-----";

    private ProviderKeys() {}

    /**
     * The P-256 private key of an APNs {@code .p8} PEM.
     *
     * @throws IllegalArgumentException when it is not a PKCS#8 EC P-256 key
     */
    public static ECPrivateKey apnsKey(String pem) {
        PrivateKey key = pkcs8(pem, "EC");
        if (!(key instanceof ECPrivateKey ec)) {
            throw new IllegalArgumentException("is not an EC private key");
        }
        if (!ec.getParams().getCurve().equals(EcKeys.P256.getCurve())
                || !ec.getParams().getOrder().equals(EcKeys.P256.getOrder())) {
            throw new IllegalArgumentException("is not a P-256 key (APNs keys are ES256)");
        }
        return ec;
    }

    /**
     * The RSA private key of a service account's {@code private_key} PEM.
     *
     * @throws IllegalArgumentException when it is not a PKCS#8 RSA key
     */
    public static RSAPrivateKey rsaKey(String pem) {
        PrivateKey key = pkcs8(pem, "RSA");
        if (!(key instanceof RSAPrivateKey rsa)) {
            throw new IllegalArgumentException("is not an RSA private key");
        }
        if (rsa.getModulus().bitLength() < 2048) {
            throw new IllegalArgumentException("is an RSA key shorter than 2048 bits");
        }
        return rsa;
    }

    /** The PEM normalized to a header, 64-character base64 lines and a footer. */
    public static String normalize(String pem) {
        String b64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(der(pem));
        return BEGIN + "\n" + b64 + "\n" + END + "\n";
    }

    private static PrivateKey pkcs8(String pem, String algorithm) {
        byte[] der = der(pem);
        try {
            return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | ClassCastException e) {
            throw new IllegalArgumentException("is not a PKCS#8 " + algorithm + " private key");
        }
    }

    private static byte[] der(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("is empty");
        }
        String text = pem.strip();
        int begin = text.indexOf(BEGIN);
        int end = text.indexOf(END);
        if (begin < 0 || end < begin) {
            throw new IllegalArgumentException("must be a PEM 'PRIVATE KEY' (PKCS#8) block");
        }
        String body = text.substring(begin + BEGIN.length(), end).replaceAll("\\s", "");
        byte[] der;
        try {
            der = Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("is not valid base64 inside the PEM block");
        }
        if (der.length == 0) {
            throw new IllegalArgumentException("is empty");
        }
        return der;
    }
}
