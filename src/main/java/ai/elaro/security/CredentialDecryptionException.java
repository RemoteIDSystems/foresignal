package ai.elaro.security;

/**
 * Thrown when stored credential bytes cannot be decrypted.
 *
 * <p>Means the ciphertext or nonce was altered, or the key file no longer matches what encrypted
 * them. The message deliberately carries no detail about the plaintext.
 */
public class CredentialDecryptionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CredentialDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
