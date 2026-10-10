package ai.elaro.security;

/**
 * A ciphertext and the nonce it was produced with, matching the {@code (ciphertext, nonce)} column
 * pairs in {@code source_credential} and {@code channel}.
 *
 * @param ciphertext AES-GCM output, including the authentication tag
 * @param nonce the 12 random bytes used for this one encryption
 */
public record Encrypted(byte[] ciphertext, byte[] nonce) {}
