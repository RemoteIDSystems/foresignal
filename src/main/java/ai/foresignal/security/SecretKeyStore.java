package ai.foresignal.security;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Owns the AES key that protects upstream credentials.
 *
 * <p>The key lives in a file on the data volume, never in the database (Architecture §7.2), so a
 * database dump on its own does not reveal upstream credentials. It is generated on first start and
 * is never logged.
 */
@Component
public class SecretKeyStore {

    private static final Logger log = LoggerFactory.getLogger(SecretKeyStore.class);

    /** AES-256. */
    static final int KEY_LENGTH_BYTES = 32;

    static final String KEY_FILE_NAME = "secret.key";
    static final String ALGORITHM = "AES";

    /** {@code rw-------}: readable only by the account running Foresignal. */
    private static final Set<PosixFilePermission> KEY_FILE_PERMISSIONS =
            PosixFilePermissions.fromString("rw-------");

    private final SecretKey key;
    private final Path keyFile;

    /**
     * Loads or creates the key in the configured data directory. Marked {@code @Autowired} because
     * the class has a second, test-only constructor, and Spring cannot choose between two otherwise.
     */
    @Autowired
    public SecretKeyStore(ForesignalProperties properties) {
        this(properties.dataDir());
    }

    SecretKeyStore(Path dataDir) {
        this.keyFile = dataDir.resolve(KEY_FILE_NAME);
        this.key = Files.exists(keyFile) ? load(keyFile) : create(dataDir, keyFile);
    }

    /** The key, for {@link CredentialCipher} only. */
    SecretKey key() {
        return key;
    }

    /** Where the key is stored. Exposed for diagnostics; the contents are never returned. */
    public Path keyFile() {
        return keyFile;
    }

    private static SecretKey load(Path keyFile) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(keyFile);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not read the credential encryption key at " + keyFile + ": " + e.getMessage(), e);
        }
        if (bytes.length != KEY_LENGTH_BYTES) {
            // Refuse rather than silently using a weak or truncated key. Deleting the file would
            // make every stored credential undecryptable, so that is the operator's call.
            Arrays.fill(bytes, (byte) 0);
            throw new IllegalStateException(
                    "The credential encryption key at "
                            + keyFile
                            + " is "
                            + bytes.length
                            + " bytes; expected "
                            + KEY_LENGTH_BYTES
                            + ". Restore the original file from backup, or delete it to generate a new "
                            + "key and re-enter every upstream credential.");
        }
        log.info("Loaded the credential encryption key from {}", keyFile);
        return toKey(bytes);
    }

    private static SecretKey create(Path dataDir, Path keyFile) {
        byte[] bytes = new byte[KEY_LENGTH_BYTES];
        new SecureRandom().nextBytes(bytes);
        try {
            Files.createDirectories(dataDir);
            Files.write(keyFile, bytes);
            restrictPermissions(keyFile);
        } catch (IOException e) {
            Arrays.fill(bytes, (byte) 0);
            throw new IllegalStateException(
                    "Could not create the credential encryption key at "
                            + keyFile
                            + ". Make sure the data directory is writable: "
                            + e.getMessage(),
                    e);
        }
        log.info("Generated a new credential encryption key at {}", keyFile);
        return toKey(bytes);
    }

    private static void restrictPermissions(Path keyFile) {
        try {
            Files.setPosixFilePermissions(keyFile, KEY_FILE_PERMISSIONS);
        } catch (UnsupportedOperationException | IOException e) {
            // Windows and some mounted volumes have no POSIX permissions. Worth knowing about,
            // but not worth refusing to start over.
            log.warn(
                    "Could not restrict permissions on {} to rw-------. Make sure the file is not "
                            + "readable by other users. ({})",
                    keyFile,
                    e.getMessage());
        }
    }

    private static SecretKey toKey(byte[] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, ALGORITHM);
        Arrays.fill(bytes, (byte) 0);
        return secretKey;
    }
}
