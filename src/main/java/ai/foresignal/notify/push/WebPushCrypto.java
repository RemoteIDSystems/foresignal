package ai.foresignal.notify.push;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Message encryption for Web Push (RFC 8291) in the {@code aes128gcm} content coding (RFC 8188),
 * with JDK crypto only.
 *
 * <ol>
 *   <li>An ephemeral P-256 key pair per message; {@code ecdh_secret = ECDH(as_private, ua_public)}.
 *   <li>{@code PRK_key = HKDF-Extract(auth_secret, ecdh_secret)};
 *       {@code IKM = HKDF-Expand(PRK_key, "WebPush: info" ‖ 0x00 ‖ ua_public ‖ as_public, 32)}.
 *   <li>A random 16-byte salt; {@code PRK = HKDF-Extract(salt, IKM)};
 *       {@code CEK = HKDF-Expand(PRK, "Content-Encoding: aes128gcm" ‖ 0x00, 16)};
 *       {@code NONCE = HKDF-Expand(PRK, "Content-Encoding: nonce" ‖ 0x00, 12)}.
 *   <li>The plaintext plus the {@code 0x02} last-record delimiter, AES-128-GCM, one record.
 *   <li>Body = {@code salt ‖ rs (4096, uint32) ‖ idlen (65) ‖ as_public ‖ ciphertext}.
 * </ol>
 *
 * <p>The whole body never exceeds {@value #RECORD_SIZE} bytes, so the plaintext is at most
 * {@value #MAX_PLAINTEXT} bytes (4096 − 86 header − 16 tag − 1 delimiter). {@link #decrypt} is the
 * receiver's side, used by tests and available to Java clients.
 */
public final class WebPushCrypto {

    /** The record size written into the header, and the most bytes a push message may have. */
    public static final int RECORD_SIZE = 4096;

    /** The header length: salt 16 + rs 4 + idlen 1 + key 65. */
    public static final int HEADER_LENGTH = 86;

    /** The AES-GCM tag length in bytes. */
    public static final int TAG_LENGTH = 16;

    /** The largest plaintext that fits one record. */
    public static final int MAX_PLAINTEXT = RECORD_SIZE - HEADER_LENGTH - TAG_LENGTH - 1;

    /** The salt length. */
    public static final int SALT_LENGTH = 16;

    /** The auth secret length a subscription must have. */
    public static final int AUTH_LENGTH = 16;

    private static final byte[] KEY_INFO = "WebPush: info\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CEK_INFO = "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NONCE_INFO = "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte DELIMITER = 0x02;
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebPushCrypto() {}

    /**
     * Encrypts {@code plaintext} for a subscription with a fresh ephemeral key and salt.
     *
     * @param uaPublic the subscription's {@code p256dh} (65-byte point)
     * @param authSecret the subscription's {@code auth} (16 bytes)
     * @throws IllegalArgumentException when the plaintext exceeds {@link #MAX_PLAINTEXT} or a key is invalid
     */
    public static byte[] encrypt(byte[] plaintext, byte[] uaPublic, byte[] authSecret) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return encrypt(plaintext, uaPublic, authSecret, EcKeys.generate(), salt);
    }

    /**
     * Encrypts with a given application-server key pair and salt (deterministic; the RFC 8291
     * Appendix A vector and tests). Production code uses {@link #encrypt(byte[], byte[], byte[])}.
     */
    public static byte[] encrypt(byte[] plaintext, byte[] uaPublic, byte[] authSecret, KeyPair asKeys, byte[] salt) {
        if (plaintext.length > MAX_PLAINTEXT) {
            throw new IllegalArgumentException("the plaintext is " + plaintext.length + " bytes; at most "
                    + MAX_PLAINTEXT + " fit one record");
        }
        if (authSecret == null || authSecret.length != AUTH_LENGTH) {
            throw new IllegalArgumentException("the auth secret must be 16 bytes");
        }
        if (salt == null || salt.length != SALT_LENGTH) {
            throw new IllegalArgumentException("the salt must be 16 bytes");
        }
        ECPublicKey ua = EcKeys.decodePoint(uaPublic);
        byte[] asPublic = EcKeys.encodePoint((ECPublicKey) asKeys.getPublic());
        byte[] ecdh = EcKeys.ecdh((ECPrivateKey) asKeys.getPrivate(), ua);
        Keys keys = keys(ecdh, authSecret, uaPublic, asPublic, salt);
        byte[] padded = Arrays.copyOf(plaintext, plaintext.length + 1);
        padded[plaintext.length] = DELIMITER;
        byte[] ciphertext = aesGcm(Cipher.ENCRYPT_MODE, keys.cek(), keys.nonce(), padded);
        ByteBuffer body = ByteBuffer.allocate(HEADER_LENGTH + ciphertext.length);
        body.put(salt).putInt(RECORD_SIZE).put((byte) asPublic.length).put(asPublic).put(ciphertext);
        return body.array();
    }

    /**
     * Decrypts a single-record {@code aes128gcm} push message (the user agent's side).
     *
     * @param uaPrivate the subscription's private key
     * @param uaPublic the subscription's public point
     * @param authSecret the subscription's auth secret
     * @return the plaintext without the delimiter
     * @throws IllegalArgumentException when the body is malformed or does not authenticate
     */
    public static byte[] decrypt(byte[] body, ECPrivateKey uaPrivate, byte[] uaPublic, byte[] authSecret) {
        if (body == null || body.length < HEADER_LENGTH + TAG_LENGTH + 1) {
            throw new IllegalArgumentException("the body is too short for an aes128gcm record");
        }
        ByteBuffer in = ByteBuffer.wrap(body);
        byte[] salt = new byte[SALT_LENGTH];
        in.get(salt);
        int rs = in.getInt();
        int idlen = in.get() & 0xFF;
        if (idlen != EcKeys.POINT_LENGTH || rs < 18) {
            throw new IllegalArgumentException("unexpected aes128gcm header (rs " + rs + ", idlen " + idlen + ")");
        }
        byte[] asPublic = new byte[idlen];
        in.get(asPublic);
        byte[] ciphertext = new byte[in.remaining()];
        in.get(ciphertext);
        byte[] ecdh = EcKeys.ecdh(uaPrivate, EcKeys.decodePoint(asPublic));
        Keys keys = keys(ecdh, authSecret, uaPublic, asPublic, salt);
        byte[] padded = aesGcm(Cipher.DECRYPT_MODE, keys.cek(), keys.nonce(), ciphertext);
        int end = padded.length - 1;
        while (end >= 0 && padded[end] == 0) {
            end--;
        }
        if (end < 0 || padded[end] != DELIMITER) {
            throw new IllegalArgumentException("the record does not end with the last-record delimiter");
        }
        return Arrays.copyOf(padded, end);
    }

    /**
     * The derived values of one message (exposed for the RFC 8291 Appendix A intermediate checks).
     *
     * @param prkKey PRK for key combining
     * @param ikm the input keying material
     * @param prk PRK for content encryption
     * @param cek the content encryption key
     * @param nonce the nonce
     */
    public record Keys(byte[] prkKey, byte[] ikm, byte[] prk, byte[] cek, byte[] nonce) {}

    /** Derives the RFC 8291 keys. */
    public static Keys keys(byte[] ecdhSecret, byte[] authSecret, byte[] uaPublic, byte[] asPublic, byte[] salt) {
        byte[] prkKey = Hkdf.extract(authSecret, ecdhSecret);
        byte[] keyInfo = new byte[KEY_INFO.length + uaPublic.length + asPublic.length];
        System.arraycopy(KEY_INFO, 0, keyInfo, 0, KEY_INFO.length);
        System.arraycopy(uaPublic, 0, keyInfo, KEY_INFO.length, uaPublic.length);
        System.arraycopy(asPublic, 0, keyInfo, KEY_INFO.length + uaPublic.length, asPublic.length);
        byte[] ikm = Hkdf.expand(prkKey, keyInfo, 32);
        byte[] prk = Hkdf.extract(salt, ikm);
        byte[] cek = Hkdf.expand(prk, CEK_INFO, 16);
        byte[] nonce = Hkdf.expand(prk, NONCE_INFO, 12);
        return new Keys(prkKey, ikm, prk, cek, nonce);
    }

    private static byte[] aesGcm(int mode, byte[] key, byte[] nonce, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("AES-GCM failed: " + e.getMessage(), e);
        }
    }
}
