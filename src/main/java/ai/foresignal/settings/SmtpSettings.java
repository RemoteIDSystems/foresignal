package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The global SMTP server of the transactional account emails (prompt R2-007), stored under {@link SettingKeys#SMTP}
 * (OpenAPI schema {@code SmtpSettings}). The password is kept only as AES-GCM ciphertext and nonce (base64), sealed by
 * {@code CredentialCipher}; the API never returns it, only {@code secretsSet}.
 *
 * @param enabled whether account emails are sent
 * @param host the SMTP host
 * @param port the SMTP port
 * @param security {@code starttls} (default), {@code tls} (implicit TLS) or {@code none}
 * @param username the SMTP user name, or null for no authentication
 * @param passwordCipher the sealed password, base64, or null
 * @param passwordNonce the password's nonce, base64, or null
 * @param fromAddress the sender address, default {@value #DEFAULT_FROM_ADDRESS}
 * @param fromName the sender display name, default {@value #DEFAULT_FROM_NAME}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SmtpSettings(boolean enabled, String host, Integer port, String security, String username,
        String passwordCipher, String passwordNonce, String fromAddress, String fromName) {

    /** The sender address when none is set. */
    public static final String DEFAULT_FROM_ADDRESS = "info@remoteidsystems.com";

    /** The sender name when none is set. */
    public static final String DEFAULT_FROM_NAME = "Foresignal";

    /** The port when none is set (submission with STARTTLS). */
    public static final int DEFAULT_PORT = 587;

    /** The TLS mode when none is set. */
    public static final String DEFAULT_SECURITY = "starttls";

    /** Off, with the shipped defaults. */
    public static SmtpSettings defaults() {
        return new SmtpSettings(false, null, DEFAULT_PORT, DEFAULT_SECURITY, null, null, null, DEFAULT_FROM_ADDRESS,
                DEFAULT_FROM_NAME);
    }

    /** The port in force. */
    public int effectivePort() {
        return port == null ? DEFAULT_PORT : port;
    }

    /** The TLS mode in force. */
    public String effectiveSecurity() {
        return security == null ? DEFAULT_SECURITY : security;
    }

    /** The sender address in force. */
    public String effectiveFromAddress() {
        return fromAddress == null || fromAddress.isBlank() ? DEFAULT_FROM_ADDRESS : fromAddress;
    }

    /** The sender name in force. */
    public String effectiveFromName() {
        return fromName == null || fromName.isBlank() ? DEFAULT_FROM_NAME : fromName;
    }

    /** True when a password is stored. */
    public boolean hasPassword() {
        return passwordCipher != null && passwordNonce != null;
    }

    /** True when emails can be attempted: enabled and a host set. */
    public boolean usable() {
        return enabled && host != null && !host.isBlank();
    }
}
