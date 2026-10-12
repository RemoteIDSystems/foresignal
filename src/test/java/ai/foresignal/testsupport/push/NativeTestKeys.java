package ai.foresignal.testsupport.push;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * Keys generated at test time for native push (prompt 032): an APNs-style EC P-256 key as a
 * {@code .p8} PEM, and a Firebase-style RSA service account. Nothing here is a real credential, and
 * nothing is written to disk.
 */
public final class NativeTestKeys {

    private NativeTestKeys() {}

    /** A P-256 key pair, like an APNs auth key. */
    public static KeyPair ec() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** A 2048-bit RSA key pair, like a service-account key. */
    public static KeyPair rsa() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The PKCS#8 PEM of a private key ({@code -----BEGIN PRIVATE KEY-----}). */
    public static String pem(KeyPair pair) {
        return "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(pair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
    }

    /** The EC public key of an APNs-style pair. */
    public static ECPublicKey ecPublic(KeyPair pair) {
        return (ECPublicKey) pair.getPublic();
    }

    /** The RSA public key of a service-account pair. */
    public static RSAPublicKey rsaPublic(KeyPair pair) {
        return (RSAPublicKey) pair.getPublic();
    }

    /** A service-account JSON document holding {@code pair}'s private key. */
    public static String serviceAccountJson(KeyPair pair, String projectId, String clientEmail, String tokenUri) {
        String pem = pem(pair).replace("\n", "\\n");
        return "{\"type\":\"service_account\",\"project_id\":\"" + projectId + "\",\"private_key_id\":\"test\","
                + "\"private_key\":\"" + pem + "\",\"client_email\":\"" + clientEmail + "\",\"client_id\":\"1\","
                + "\"token_uri\":\"" + tokenUri + "\"}";
    }
}
